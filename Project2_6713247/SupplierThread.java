package Project2_6713247;

/**
 * @author Rachapon, 6713247
 *         Ratchasin, 6713247
 */

import java.util.*;
import java.util.concurrent.*;

public class SupplierThread extends Thread {
    private ArrayList<Warehouse> warehouses;
    private int minSupply;
    private int maxSupply;
    private int days;
    private CyclicBarrier dayStartBarrier;
    private CyclicBarrier dayEndBarrier;
    private CyclicBarrier supplierDoneBarrier;
    private Random random;
    
    public SupplierThread(String name, ArrayList<Warehouse> warehouses, 
                          int minSupply, int maxSupply, int days,
                          CyclicBarrier dayStartBarrier,
                          CyclicBarrier dayEndBarrier,
                          CyclicBarrier supplierDoneBarrier) {
        super(name);
        this.warehouses = warehouses;
        this.minSupply = minSupply;
        this.maxSupply = maxSupply;
        this.days = days;
        this.dayStartBarrier = dayStartBarrier;
        this.dayEndBarrier = dayEndBarrier;
        this.supplierDoneBarrier = supplierDoneBarrier;
        this.random = new Random();
    }
    
    @Override
    public void run() {
        for (int day = 1; day <= days; day++) {
            try {
                dayStartBarrier.await();
                
                int warehouseIndex = random.nextInt(warehouses.size());
                Warehouse warehouse = warehouses.get(warehouseIndex);
                int amount = minSupply + random.nextInt(maxSupply - minSupply + 1);
                
                warehouse.put(amount);
                
                System.out.printf("%s >> put %d materials %s balance = %d\n",
                                Thread.currentThread().getName(),
                                amount,
                                warehouse.getName(),
                                warehouse.getBalance());
                
                supplierDoneBarrier.await();
                dayEndBarrier.await();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}