package org.example;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import  java.util.UUID;
class Customer {
    private final String customerId;
    private String name;
    private String address;
    private final List<Account>accounts;
    public Customer(String name, String address){
        this.customerId=UUID.randomUUID().toString();
        this.name=name;
        this.address=address;
        this.accounts=new ArrayList<>();
    }
    public void addAccount(Account account){
        this.accounts.add(account);
    }
    public String getCustomerId(){
        return customerId;
    }
    public String getName(){
        return name;
    }

    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }


}

abstract class Account {
    private static final AtomicLong count = new AtomicLong(100000);
    protected long accountNumber;
    protected Customer accountHolder;
    protected double balance;
    protected List<Transaction> transactionhistory;

    public Account(Customer accountHolder, double initialBalance) {
        this.accountNumber = count.incrementAndGet();
        this.accountHolder = accountHolder;
        this.balance = initialBalance;
        this.transactionhistory = new ArrayList<>();
        if (initialBalance > 0) {
            this.transactionhistory.add(new Transaction(TransactionType.DEPOSIT, initialBalance));
        }
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            this.transactionhistory.add(new Transaction(TransactionType.DEPOSIT, amount));
            System.out.println("Deposit successful.");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            this.transactionhistory.add(new Transaction(TransactionType.WITHDRAWAL, amount));
            System.out.println("Withdrawal Successful.");
        } else {
            System.out.println("Withdraw Failed");
        }

    }

    public String toString() {
        return String.format("Account No : %d,Holder:%s,Balance:$%.2f", accountNumber, accountHolder.getName(), balance);
    }

    public List<Transaction> gettransactionhistory() {
        return new ArrayList<>(transactionhistory);
    }

    public double getbalance() {
        return balance;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

}

class SavingsAccount extends Account {
    private double interestRate;
    public  SavingsAccount(Customer accountHolder,double initialBalance){
        super(accountHolder,initialBalance);
        this.interestRate=0.02; //2%
    }
}

class CheckingAccount extends Account {
    private double overdraftlimit;
    public CheckingAccount(Customer accountHolder, double initialBalance){
        super(accountHolder,initialBalance);
        this.overdraftlimit=100.0; //$100 overdraft
    }
    @Override
    public void withdraw(double amount){
        if(amount>0&&(getbalance()+overdraftlimit)>=amount){
            this.balance-=amount;
            this.transactionhistory.add(new Transaction(TransactionType.WITHDRAWAL,amount));
            System.out.println("Withdrawal successful");
        }else{
            System.out.println("Withdrawal failed: Overdraft limit exceeded");
        }
    }
}

class Transaction {

    private final String transactionId;
    private final TransactionType type;
    private final double amount;
    private final LocalDateTime timestamp;

    public Transaction(TransactionType type,double amount){
        this.transactionId=UUID.randomUUID().toString();
        this.type=type;
        this.amount=amount;
        this.timestamp=LocalDateTime.now();
    }

    @Override
    public String toString()
    {
        return  String.format("Transaction[%s]-%s:$%.2f at %s",transactionId,type,amount,timestamp);
    }
}

enum AccountType {
    SAVINGS,

    CHECKING
}

enum TransactionType {
    DEPOSIT,


    WITHDRAWAL
}


class Bank {
    String bankname;
    Map<String, Customer> customers;
    Map<Long, Account> accounts;

    public Bank(String bankname) {
        this.bankname = bankname;
        this.customers = new HashMap<>();
        this.accounts = new HashMap<>();

    }

    public Customer createCustomer(String name, String addres) {
        Customer customer = new Customer(name, addres);
        customers.put(customer.getCustomerId(), customer);
        return customer;
    }

    public Account createAccount(Customer customer, AccountType type, double intialdeposit) {
        Account account = null;
        switch (type) {
            case SAVINGS:
                account = new SavingsAccount(customer, intialdeposit);
                break;
            case CHECKING:
                account = new CheckingAccount(customer, intialdeposit);
                break;
        }
        if (account != null) {
            accounts.put(account.getAccountNumber(), account);
            customer.addAccount(account);
        }
        return account;
    }

    public Account getAccount(long accountNumber) {
        return accounts.get(accountNumber);
    }
    public Customer getCustomer(String customerId) {
        return customers.get(customerId);
    }

}
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Bank bank = new Bank("LOAN-FEDERATION");
        Customer smith=null;

        while (true) {
            System.out.print(
                    "\nWelcome to Banking services of LOAN - FEDERATION\n" +
                            "1. Enter 'create' for account creation\n" +
                            "2. Enter 'deposit' for cash deposit\n" +
                            "3. Enter 'checking' for view account details\n" +
                            "4. Enter 'history' for transaction history\n" +
                            "5. Enter 'withdraw' for withdrawal\n" +
                            "6. Enter 'balance' to view balance\n" +
                            "7. Enter 'exit' to exit\n" +
                            "Enter your choice: "
            );

            String inp = sc.nextLine().trim().toLowerCase();
            if (inp.equals("exit")) {
                System.out.println("Exiting banking services. Thank you!");
                break;
            }

            switch (inp) {
                case "create":
                    // consume newline
                    Account account = null;
                    System.out.println("Enter the name for account holder");
                    String name=sc.nextLine();
                    System.out.println("Enter the address of account holder");
                    String address=sc.nextLine();
                    System.out.print("Enter account type (savings/checking): ");
                    String type = sc.nextLine().trim().toLowerCase();
                    System.out.print("Enter initial deposit amount: ");
                    double deposit = sc.nextDouble(); sc.nextLine();
                    if (type.equals("savings")) {
                        smith= bank.createCustomer(name,address);
                        System.out.println("Customer created: " + smith.getName() + " --> (ID: " + smith.getCustomerId() + ")");
                        account = bank.createAccount(smith, AccountType.SAVINGS, deposit);
                    } else if (type.equals("checking")) {
                        smith=bank.createCustomer(name,address);
                        System.out.println("Customer created: " + smith.getName() + " --> (ID: " + smith.getCustomerId() + ")");
                        account = bank.createAccount(smith, AccountType.CHECKING, deposit);
                    }
                    if (account != null)
                        System.out.println("Account created: " + account);
                    else
                        System.out.println("Invalid account type!");
                    break;

                case "deposit":
                    System.out.print("Enter account number: ");
                    long depAccNo = sc.nextLong();
                    System.out.print("Enter deposit amount: ");
                    double amount = sc.nextDouble(); sc.nextLine();
                    Account depAcc = bank.getAccount(depAccNo);
                    if (depAcc != null) {
                        depAcc.deposit(amount);
                        System.out.println("Deposit successful. New Balance: $" + depAcc.getbalance());
                    } else {
                        System.out.println("Account not found.");
                    }
                    break;

                case "withdraw":
                    System.out.print("Enter account number: ");
                    long wAccNo = sc.nextLong();
                    System.out.print("Enter withdrawal amount: ");
                    double wAmount = sc.nextDouble(); sc.nextLine();
                    Account wAcc = bank.getAccount(wAccNo);
                    if (wAcc != null) {
                        wAcc.withdraw(wAmount);
                        System.out.println("Updated Balance: $" + wAcc.getbalance());
                    } else {
                        System.out.println("Account not found.");
                    }
                    break;

                case "balance":
                    System.out.print("Enter account number: ");
                    long bAccNo = sc.nextLong(); sc.nextLine();
                    Account bAcc = bank.getAccount(bAccNo);
                    if (bAcc != null) {
                        System.out.println("Current balance: $" + bAcc.getbalance());
                    } else {
                        System.out.println("Account not found.");
                    }
                    break;

                case "history":
                    System.out.print("Enter account number: ");
                    long hAccNo = sc.nextLong(); sc.nextLine();
                    Account hAcc = bank.getAccount(hAccNo);
                    if (hAcc != null) {
                        System.out.println("Transaction History:");
                        hAcc.gettransactionhistory().forEach(System.out::println);
                    } else {
                        System.out.println("Account not found.");
                    }
                    break;

                case "checking":
                    System.out.println("All accounts for customer: " + smith.getName());
                    smith.getAccounts().forEach(System.out::println);
                    break;

                default:
                    System.out.println("Invalid input. Please try again.");
            }
        }
        sc.close();
    }
}
