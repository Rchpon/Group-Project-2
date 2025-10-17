import java.util.*;

public class FactoryThread extends Thread {
    private int maxProd, days;
    private ArrayList<Warehouse> warehouses;
    private ArrayList<Freight> freights;
    private Random rand = new Random();
    private int totalProduced = 0;
    private int totalShipped = 0;

    public FactoryThread(int maxProd, ArrayList<Warehouse> warehouses, ArrayList<Freight> freights, int days) {
        this.maxProd = maxProd;
        this.warehouses = warehouses;
        this.freights = freights;
        this.days = days;
    }

    public void run() {
        for (int d = 1; d <= days; d++) {
            synchronized (Project2.class) {
                try { Project2.class.wait(); } catch (InterruptedException e) {}
            }

            int wh = rand.nextInt(warehouses.size());
            int requested = rand.nextInt(maxProd + 1);
            int got = warehouses.get(wh).get(requested);
            totalProduced += got;
            System.out.println(Thread.currentThread().getName() + " >> get " + got + " materials Warehouse_" + wh + " balance = " + warehouses.get(wh).getBalance());

            int shipIndex = rand.nextInt(freights.size());
            int shipped = freights.get(shipIndex).ship(got);
            totalShipped += shipped;
            System.out.println(Thread.currentThread().getName() + " >> ship " + shipped + " products Freight_" + shipIndex + " remaining capacity = " + freights.get(shipIndex).getRemainingCapacity());
        }
    }

    public int getTotalProduced() { return totalProduced; }
    public int getTotalShipped() { return totalShipped; }
}

