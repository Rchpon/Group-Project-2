package Project2_6713247;
/**
 * @author Rachapon, 6713247
 *         Ratchasin, 6713247
 */

import java.util.*;
import java.util.concurrent.*;

public class FactoryThread extends Thread {
    private ArrayList<Warehouse> warehouses;
    private ArrayList<Freight> freights;
    private int maxProduction;
    private int days;
    private CyclicBarrier dayStartBarrier;
    private CyclicBarrier dayEndBarrier;
    private CyclicBarrier supplierDoneBarrier;
    private CyclicBarrier putDoneBarrier;      
    private CyclicBarrier getDoneBarrier;    
    private CyclicBarrier totalDoneBarrier;   
    private CyclicBarrier shipDoneBarrier;
    private Random random;
    
    private int unshippedProducts;
    private int totalCreated;
    private int totalShipped;
    
    public FactoryThread(String name, ArrayList<Warehouse> warehouses,
                         ArrayList<Freight> freights, int maxProduction, int days,
                         CyclicBarrier dayStartBarrier,
                         CyclicBarrier dayEndBarrier,
                         CyclicBarrier supplierDoneBarrier,
                         CyclicBarrier putDoneBarrier,    
                         CyclicBarrier getDoneBarrier,     
                         CyclicBarrier totalDoneBarrier, 
                         CyclicBarrier shipDoneBarrier) {
        super(name);
        this.warehouses = warehouses;
        this.freights = freights;
        this.maxProduction = maxProduction;
        this.days = days;
        this.dayStartBarrier = dayStartBarrier;
        this.dayEndBarrier = dayEndBarrier;
        this.supplierDoneBarrier = supplierDoneBarrier;
        this.putDoneBarrier = putDoneBarrier;        
        this.getDoneBarrier = getDoneBarrier;      
        this.totalDoneBarrier = totalDoneBarrier;  
        this.shipDoneBarrier = shipDoneBarrier;            
        this.random = new Random();
        this.unshippedProducts = 0;
        this.totalCreated = 0;
        this.totalShipped = 0;
    }
    
    public int getTotalCreated() {
        return totalCreated;
    }
    
    public int getTotalShipped() {
        return totalShipped;
    }
    
    public double getShipPercentage() {
        if (totalCreated == 0) return 0.0;
        return (totalShipped * 100.0) / totalCreated;
    }
    
    @Override
    public void run() {
        for (int day = 1; day <= days; day++) {
            try {
                dayStartBarrier.await();
                supplierDoneBarrier.await();
                
                int warehouseIndex = random.nextInt(warehouses.size());
                Warehouse warehouse = warehouses.get(warehouseIndex);
                int materialsGot = warehouse.get(maxProduction);
                
                int productsCreated = materialsGot;
                totalCreated += productsCreated;
                unshippedProducts += productsCreated;
                
                System.out.printf(" %s  >>  get %d materials %s balance = %d\n",
                                Thread.currentThread().getName(),
                                materialsGot,
                                warehouse.getName(),
                                warehouse.getBalance());
                putDoneBarrier.await();

                int freightIndex = random.nextInt(freights.size());
                Freight freight = freights.get(freightIndex);
                int productsShipped = freight.ship(unshippedProducts);
                
                totalShipped += productsShipped;
                unshippedProducts -= productsShipped;
                
                //System.out.printf("%s >>\n", Thread.currentThread().getName());

                System.out.printf(" %s  >>  total products to ship = %d\n",
                Thread.currentThread().getName(),
                unshippedProducts);

                totalDoneBarrier.await();

                System.out.printf(" %s  >>  ship %d products %s remaining capacity = %d\n",
                Thread.currentThread().getName(),
                productsShipped,
                freight.getName(),
                freight.getCapacity());
                                
                getDoneBarrier.await();

                System.out.printf(" %s  >>  unshipped products = %d\n",
                Thread.currentThread().getName(),
                unshippedProducts);

                shipDoneBarrier.await();
                
                dayEndBarrier.await();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

