package Project2_6713247;

/**
 *
 * @author Rachapon, 6713247
 *         Ratchasin, 6713247
 *         Sayklang, 6713250
 *         Chayapol, 6713223
 *         Zabit, 6713116
 */

public class Freight {
    private final int id;
    private final int maxCapacity;
    private int remaining;

    public Freight(int id, int maxCapacity) {
        this.id = id;
        this.maxCapacity = maxCapacity;
        this.remaining = maxCapacity;
    }

    public int getId() {
        return id;
    }

    // reset capacity at the start of each day
    public synchronized void reset() {
        remaining = maxCapacity;
    }

    // Try to ship up to 'amount' into this freight, return actual shipped
    public synchronized int ship(int amount) {
        if (amount <= 0) return 0;
        int shipped = Math.min(amount, remaining);
        remaining -= shipped;
        return shipped;
    }

    public synchronized int getRemaining() {
        return remaining;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    @Override
    public String toString() {
        return "F" + id + ":" + getRemaining() + "/" + getMaxCapacity();
    }
}