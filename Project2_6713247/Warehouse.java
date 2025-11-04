package Project2_6713247;

/**
 *
 * @author Rachapon, 6713247
 *         Ratchasin, 6713247
 *         Sayklang, 6713250
 *         Chayapol, 6713223
 *         Zabit, 6713116
 */

public class Warehouse {
    private final int id;
    private int materials;

    public Warehouse(int id) {
        this.id = id;
        this.materials = 0;
    }

    public int getId() {
        return id;
    }

    // Put materials into warehouse; synchronized for thread-safety
    public synchronized void put(int amount) {
        if (amount <= 0) return;
        materials += amount;
    }

    // Try to get up to 'amount' materials; return actual taken
    public synchronized int get(int amount) {
        if (amount <= 0) return 0;
        int taken = Math.min(amount, materials);
        materials -= taken;
        return taken;
    }

    public synchronized int getBalance() {
        return materials;
    }

    @Override
    public String toString() {
        return "W" + id + ":" + getBalance();
    }
}