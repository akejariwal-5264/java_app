BANK TRANSFER MVP (Java + JDBC + MySQL)

Files
-----
- sql/bank.sql: creates the bank database, account table, and two sample accounts.
- src/main/java/com/example/bank/BankTransferApp.java: console app with JDBC transaction.
- pom.xml: downloads MySQL Connector/J automatically through Maven.

IMPORTANT
---------
The app cannot connect until MySQL is running and you know the password for your MySQL user.
The code defaults to user "root" and a blank password. If your root account has a password,
set DB_PASSWORD to that password. Do not put your real password in a file you share.

SETUP
-----
1. Install JDK 17 or later and Maven.
2. Open MySQL Workbench and connect to your MySQL server.
3. Open sql/bank.sql in Workbench and execute it.
   This creates database `bank`, table `account`, and sample accounts:
   101 / Anu / 5000.00
   102 / Ravi / 3000.00
4. Set database credentials in the terminal before running the app.

Windows Command Prompt example:
   set DB_URL=jdbc:mysql://localhost:3306/bank
   set DB_USER=root
   set DB_PASSWORD=YOUR_MYSQL_PASSWORD

5. In the project folder, run:
   mvn compile
   mvn exec:java -Dexec.mainClass=com.example.bank.BankTransferApp

If the exec plugin command is not available in your Maven setup, use:
   mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.mainClass=com.example.bank.BankTransferApp

HOW IT WORKS
------------
- Checks that sender and receiver are different and amount is positive.
- Uses setAutoCommit(false) to begin a transaction.
- Checks both accounts and locks their rows with SELECT ... FOR UPDATE.
- Debits the sender and credits the receiver using PreparedStatement.
- Calls commit() only if both updates succeed.
- Calls rollback() if any transfer step fails.

NOTE
----
The SQL script uses INSERT ... ON DUPLICATE KEY UPDATE so rerunning it will not reset
existing account balances. To restore sample balances for practice, run:
   UPDATE account SET balance=5000.00 WHERE account_no=101;
   UPDATE account SET balance=3000.00 WHERE account_no=102;
