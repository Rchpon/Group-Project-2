package Project2_6713247;

/**
 * @author Rachapon, 6713247
 *         Ratchasin, 6713247
 */

public class Warehouse {
    private String name;
    private int balance;
    
    public Warehouse(String name) {
        this.name = name;
        this.balance = 0;
    }
    
    public String getName() {
        return name;
    }
    
    public synchronized int getBalance() {
        return balance;
    }
    
    public synchronized void put(int amount) {
        balance += amount;
    }
    
    public synchronized int get(int maxAmount) {
        int actual = Math.min(balance, maxAmount);
        balance -= actual;
        return actual;
    }
    public String toString() {
        return name;
    }
}