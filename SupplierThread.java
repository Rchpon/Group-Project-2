import java.util.*;

public class SupplierThread extends Thread {
    private int min, max, days;
    private ArrayList<Warehouse> warehouses;
    private Random rand = new Random();

    public SupplierThread(int min, int max, ArrayList<Warehouse> warehouses, int days) {
        this.min = min;
        this.max = max;
        this.warehouses = warehouses;
        this.days = days;
    }

    public void run() {
        for (int d = 1; d <= days; d++) {
            synchronized (Project2.class) {
                try { Project2.class.wait(); } catch (InterruptedException e) {}
            }
            int amount = rand.nextInt(max - min + 1) + min;
            int target = rand.nextInt(warehouses.size());
            warehouses.get(target).put(amount);
            System.out.println(Thread.currentThread().getName() + " >> put " + amount + " materials Warehouse_" + target + " balance = " + warehouses.get(target).getBalance());
        }
    }
}

