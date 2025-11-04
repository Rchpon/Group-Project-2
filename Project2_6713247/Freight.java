package Project2_6713247;

/**
 * @author Rachapon, 6713247
 *         Ratchasin, 6713247
 */

public class Freight {
    private String name;
    private int capacity;
    private int maxCapacity;
    
    public Freight(String name, int maxCapacity) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.capacity = maxCapacity;
    }
    
    public String getName() {
        return name;
    }
    
    public synchronized int getCapacity() {
        return capacity;
    }
    
    public synchronized void reset() {
        capacity = maxCapacity;
    }
    
    public synchronized int ship(int amount) {
        int actual = Math.min(capacity, amount);
        capacity -= actual;
        return actual;
    }
}
