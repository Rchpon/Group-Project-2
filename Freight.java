public class Freight {
    private int capacity;
    private int used = 0;

    public Freight(int capacity) { this.capacity = capacity; }

    public synchronized int ship(int amount) {
        int space = capacity - used;
        int toShip = Math.min(space, amount);
        used += toShip;
        return toShip;
    }

    public synchronized void reset() { used = 0; }

    public synchronized int getRemainingCapacity() { return capacity - used; }
}

