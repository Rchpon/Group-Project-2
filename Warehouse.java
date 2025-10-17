public class Warehouse {
    private int balance = 0;

    public synchronized void put(int amount) { balance += amount; }

    public synchronized int get(int amount) {
        int available = Math.min(amount, balance);
        balance -= available;
        return available;
    }

    public synchronized int getBalance() { return balance; }
}
