package BankAccountSystem;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BankSystem {

    private static final String DB_FILE = "BankDatabase.txt";
    private static final Scanner sc = new Scanner(System.in);
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static Map<String, Account> accounts = new LinkedHashMap<>();
    private static int accCounter = 1000; 

    static class Account {
        String accNo;
        String firstName;
        String lastName;
        String gender;
        String dob;
        String email;
        String pan;
        String phone;
        String address;
        String accType;
        double balance;
        List<String> miniStatement = new ArrayList<>();

        
        LocalDateTime lastWithdrawDate = null;
        double todayWithdrawn = 0;

        Account(String accNo, String firstName, String lastName, String gender, String dob, String email,
                String pan, String phone, String address, String accType, double balance) {
            this.accNo = accNo;
            this.firstName = firstName;
            this.lastName = lastName;
            this.gender = gender;
            this.dob = dob;
            this.email = email;
            this.pan = pan;
            this.phone = phone;
            this.address = address;
            this.accType = accType;
            this.balance = balance;
            addStatement("Account Created | Amount: " + balance + " | Balance: " + balance);
        }

        void deposit(double amt) {
            balance += amt;
            addStatement("Deposit | Amount: " + amt + " | Balance: " + balance);
            System.out.println("✅ Deposited Successfully! Current Balance: " + balance);
        }

        void withdraw(double amt) {
            LocalDateTime now = LocalDateTime.now();
            String today = now.toLocalDate().toString();

           
            if (lastWithdrawDate == null || !lastWithdrawDate.toLocalDate().toString().equals(today)) {
                todayWithdrawn = 0;
            }

            if (amt > balance) {
                System.out.println("❌ Insufficient Balance!");
                return;
            }

            if (todayWithdrawn + amt > 100000) {
                System.out.println("❌ Withdraw limit exceeded! Daily limit is ₹1,00,000.");
                return;
            }

            balance -= amt;
            todayWithdrawn += amt;
            lastWithdrawDate = now;

            addStatement("Withdraw | Amount: " + amt + " | Balance: " + balance);
            System.out.println("✅ Withdrawn Successfully! Current Balance: " + balance);
        }

        void addStatement(String action) {
            String time = LocalDateTime.now().format(dtf);
            miniStatement.add(action + " | " + time);
            saveToFile();
        }

        void showAccountDetails() {
            System.out.println("\n📄 Account Details:");
            System.out.println("Name       : " + firstName + " " + lastName);
            System.out.println("Gender     : " + gender);
            System.out.println("DOB        : " + dob);
            System.out.println("Email      : " + email);
            System.out.println("PAN        : " + pan);
            System.out.println("Phone      : " + phone);
            System.out.println("Address    : " + address);
            System.out.println("Account No : " + accNo);
            System.out.println("Balance    : " + balance);
        }

        void showMiniStatement() {
            System.out.println("\n📜 Mini Statement for " + firstName + " | Account No: " + accNo);
            for (String s : miniStatement) {
                System.out.println(s);
            }
        }

        void saveToFile() {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(DB_FILE))) {
                for (Account acc : accounts.values()) {
                    bw.write("AccountNo: " + acc.accNo);
                    bw.newLine();
                    bw.write("FirstName: " + acc.firstName);
                    bw.newLine();
                    bw.write("LastName: " + acc.lastName);
                    bw.newLine();
                    bw.write("Gender: " + acc.gender);
                    bw.newLine();
                    bw.write("DOB: " + acc.dob);
                    bw.newLine();
                    bw.write("Email: " + acc.email);
                    bw.newLine();
                    bw.write("PAN: " + acc.pan);
                    bw.newLine();
                    bw.write("Phone: " + acc.phone);
                    bw.newLine();
                    bw.write("Address: " + acc.address);
                    bw.newLine();
                    bw.write("AccountType: " + acc.accType);
                    bw.newLine();
                    bw.write("Balance: " + acc.balance);
                    bw.newLine();
                    bw.write("MiniStatement:");
                    bw.newLine();
                    for (String stmt : acc.miniStatement) {
                        bw.write(stmt);
                        bw.newLine();
                    }
                    bw.write("------------------------");
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        static void loadFromFile() {
            File file = new File(DB_FILE);
            if (!file.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(DB_FILE))) {
                String line;
                Account acc = null;
                List<String> mini = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("AccountNo: ")) {
                        if (acc != null) { // save previous
                            acc.miniStatement = new ArrayList<>(mini);
                            accounts.put(acc.accNo, acc);
                            mini.clear();
                        }
                        String accNo = line.split(": ")[1];
                        acc = new Account(accNo, "", "", "", "", "", "", "", "", "", 0);
                        
                        int num = Integer.parseInt(accNo.substring(3));
                        if (num > accCounter) accCounter = num;
                    } else if (line.startsWith("FirstName: ")) acc.firstName = line.split(": ")[1];
                    else if (line.startsWith("LastName: ")) acc.lastName = line.split(": ")[1];
                    else if (line.startsWith("Gender: ")) acc.gender = line.split(": ")[1];
                    else if (line.startsWith("DOB: ")) acc.dob = line.split(": ")[1];
                    else if (line.startsWith("Email: ")) acc.email = line.split(": ")[1];
                    else if (line.startsWith("PAN: ")) acc.pan = line.split(": ")[1];
                    else if (line.startsWith("Phone: ")) acc.phone = line.split(": ")[1];
                    else if (line.startsWith("Address: ")) acc.address = line.split(": ")[1];
                    else if (line.startsWith("AccountType: ")) acc.accType = line.split(": ")[1];
                    else if (line.startsWith("Balance: ")) acc.balance = Double.parseDouble(line.split(": ")[1]);
                    else if (line.equals("MiniStatement:")) mini.clear();
                    else if (line.equals("------------------------")) {
                        if (acc != null) {
                            acc.miniStatement = new ArrayList<>(mini);
                            accounts.put(acc.accNo, acc);
                        }
                        acc = null;
                        mini.clear();
                    } else mini.add(line);
                }
                if (acc != null) accounts.put(acc.accNo, acc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

   
    private static void createAccount() {
        sc.nextLine(); 
        System.out.print("👤 Enter First Name: ");
        String fname = sc.nextLine();
        System.out.print("👤 Enter Last Name: ");
        String lname = sc.nextLine();

        String gender = "";
        while (true) {
            System.out.print("⚧ Enter Gender (1.Female/2.Male/3.Other): ");
            String g = sc.nextLine();
            if (g.equals("1")) { gender = "Female"; break; }
            else if (g.equals("2")) { gender = "Male"; break; }
            else if (g.equals("3")) { gender = "Other"; break; }
            else System.out.println("❌ Invalid input!");
        }

        System.out.print("📅 Enter DOB (yyyy-mm-dd): ");
        String dob = sc.nextLine();

        System.out.print("📧 Enter Email: ");
        String email = sc.nextLine();

        String pan;
        while (true) {
            System.out.print("🆔 Enter PAN (6 letters + 4 digits): ");
            pan = sc.nextLine();
            if (pan.matches("[A-Z]{6}[0-9]{4}")) break;
            else System.out.println("❌ Invalid PAN format!");
        }

        String phone;
        while (true) {
            System.out.print("📱 Enter Phone (10 digits): ");
            phone = sc.nextLine();
            if (phone.matches("\\d{10}")) break;
            else System.out.println("❌ Invalid phone!");
        }

        System.out.print("🏠 Enter Address: ");
        String address = sc.nextLine();

        System.out.print("💳 Account Type (Saving/Current): ");
        String accType = sc.nextLine();

        double openingBalance = 10000;
        String accNo = "ACC" + (++accCounter);

        Account acc = new Account(accNo, fname, lname, gender, dob, email, pan, phone, address, accType, openingBalance);
        accounts.put(accNo, acc);
        acc.saveToFile();

        System.out.println("🎉 Account created successfully!");
        System.out.println("Name: " + fname + " " + lname + " | Account No: " + accNo + " | Balance: " + openingBalance);
        System.out.println();
        acc.showAccountDetails();
        System.out.println();
    }

   
    public static void main(String[] args) {
        
        Account.loadFromFile();

        System.out.println("======================================");
        System.out.println("        Welcome to Bank");
        System.out.println("======================================");
        System.out.print("Enter Login ID: ");
        String id = sc.nextLine();
        System.out.print("Enter Password: ");
        String pwd = sc.nextLine();

        if (!(id.equals("admin890") && pwd.equals("pwd890"))) {
            System.out.println("❌ Invalid Login!");
            return;
        }

        System.out.println("✅ Login Successful!");
        System.out.println();

        while (true) {
            System.out.println("--------------- Menu -----------------");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Mini Statement");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> createAccount();
                case "2" -> {
                    System.out.print("Enter Account No: ");
                    String accNo = sc.nextLine();
                    if (accounts.containsKey(accNo)) {
                        System.out.print("Enter Deposit Amount: ");
                        double amt = sc.nextDouble();
                        sc.nextLine();
                        accounts.get(accNo).deposit(amt);
                    } else System.out.println("❌ Account not found!");
                }
                case "3" -> {
                    System.out.print("Enter Account No: ");
                    String accNo = sc.nextLine();
                    if (accounts.containsKey(accNo)) {
                        System.out.print("Enter Withdraw Amount: ");
                        double amt = sc.nextDouble();
                        sc.nextLine();
                        accounts.get(accNo).withdraw(amt);
                    } else System.out.println("❌ Account not found!");
                }
                case "4" -> {
                    System.out.print("Enter Account No: ");
                    String accNo = sc.nextLine();
                    if (accounts.containsKey(accNo)) {
                        accounts.get(accNo).showMiniStatement();
                    } else System.out.println("❌ Account not found!");
                }
                case "5" -> {
                    System.out.println("👋 Thank you for banking with us!");
                    return;
                }
                default -> System.out.println("❌ Invalid Option!");
            }
            System.out.println();
        }
    }
}
