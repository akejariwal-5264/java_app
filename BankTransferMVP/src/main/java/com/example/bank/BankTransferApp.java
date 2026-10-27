package com.example.bank;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

public class BankTransferApp {
    private static final String URL =
            System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/bank");
    private static final String USER =
            System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD =
            System.getenv().getOrDefault("DB_PASSWORD", "");

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== BANK MONEY TRANSFER ===");
            System.out.print("Sender account number: ");
            int sender = scanner.nextInt();

            System.out.print("Receiver account number: ");
            int receiver = scanner.nextInt();

            System.out.print("Amount to transfer: ");
            BigDecimal amount = scanner.nextBigDecimal();

            transfer(sender, receiver, amount);
        } catch (Exception e) {
            System.out.println("Input error: " + e.getMessage());
        }
    }

    private static void transfer(int sender, int receiver, BigDecimal amount) {
        if (sender == receiver) {
            System.out.println("Transfer failed: sender and receiver must be different.");
            return;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Transfer failed: amount must be greater than zero.");
            return;
        }

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
            con.setAutoCommit(false);

            try {
                BigDecimal senderBalance = getBalanceForUpdate(con, sender);
                getBalanceForUpdate(con, receiver); // Confirms receiver exists and locks its row.

                if (senderBalance.compareTo(amount) < 0) {
                    throw new SQLException("Insufficient balance.");
                }

                String debitSql =
                        "UPDATE account SET balance = balance - ? " +
                        "WHERE account_no = ? AND balance >= ?";
                try (PreparedStatement ps = con.prepareStatement(debitSql)) {
                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, sender);
                    ps.setBigDecimal(3, amount);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Could not debit sender account.");
                    }
                }

                String creditSql =
                        "UPDATE account SET balance = balance + ? WHERE account_no = ?";
                try (PreparedStatement ps = con.prepareStatement(creditSql)) {
                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, receiver);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Could not credit receiver account.");
                    }
                }

                con.commit();
                System.out.println("Transfer successful!");
                System.out.println("Amount transferred: " + amount.setScale(2));
                printBalance(con, sender);
                printBalance(con, receiver);

            } catch (Exception e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                System.out.println("Transfer failed; transaction rolled back: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            System.out.println("Check that MySQL is running, the 'bank' database exists, and DB_USER/DB_PASSWORD are correct.");
        }
    }

    private static BigDecimal getBalanceForUpdate(Connection con, int accountNo)
            throws SQLException {
        String sql = "SELECT balance FROM account WHERE account_no = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Account not found: " + accountNo);
                }
                return rs.getBigDecimal("balance");
            }
        }
    }

    private static void printBalance(Connection con, int accountNo) throws SQLException {
        String sql = "SELECT holder_name, balance FROM account WHERE account_no = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println(rs.getString("holder_name") + " (" + accountNo +
                            ") balance: " + rs.getBigDecimal("balance").setScale(2));
                }
            }
        }
    }
}
