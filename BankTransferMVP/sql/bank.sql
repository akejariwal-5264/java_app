CREATE DATABASE IF NOT EXISTS bank;
USE bank;

CREATE TABLE IF NOT EXISTS account (
    account_no INT PRIMARY KEY,
    holder_name VARCHAR(50) NOT NULL,
    balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT chk_account_balance CHECK (balance >= 0)
) ENGINE=InnoDB;

INSERT INTO account (account_no, holder_name, balance)
VALUES
    (101, 'Anu', 5000.00),
    (102, 'Ravi', 3000.00)
ON DUPLICATE KEY UPDATE
    holder_name = VALUES(holder_name);
