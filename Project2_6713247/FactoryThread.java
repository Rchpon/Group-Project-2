package Project2_6713247;

/**
 *
 * @author Rachapon, 6713247
 *         Ratchasin, 6713247
 *         Sayklang, 6713250
 *         Chayapol, 6713223
 *         Zabit, 6713116
 */

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;

public class FactoryThread extends Thread {
    private final int id;
    private final int maxDailyProduction;
    private final List<Warehouse> warehouses;
    private final List<Freight> freights;
    private final CyclicBarrier startDayBarrier;
    private final CyclicBarrier endDayBarrier;

    private int producedTotal = 0;
    private int shippedTotal = 0;
    private int unshipped = 0; // carried across days

    public FactoryThread(int id, int maxDailyProduction,
                         List<Warehouse> warehouses, List<Freight> freights,
                         CyclicBarrier startDayBarrier, CyclicBarrier endDayBarrier) {
        this.id = id;
        this.maxDailyProduction = maxDailyProduction;
        this.warehouses = warehouses;
        this.freights = freights;
        this.startDayBarrier = startDayBarrier;
        this.endDayBarrier = endDayBarrier;
        setName("Factory-" + id);
    }

    @Override
    public void run() {
        try {
            int days = Project2Main.getDays();
            for (int day = 1; day <= days; day++) {
                // 4.1 Wait until main prints and resets freights
                startDayBarrier.await();

                // 4.2 Wait until all suppliers finish putting materials
                CountDownLatch supplierDone = Project2Main.getSupplierDoneLatch();
                if (supplierDone != null) supplierDone.await();

                // 4.3 Get materials from 1 random warehouse (only one warehouse attempt)
                int want = maxDailyProduction;
                int wIdx = ThreadLocalRandom.current().nextInt(warehouses.size());
                Warehouse w = warehouses.get(wIdx);
                int got = w.get(want); // may be 0..want
                producedTotal += got;
                unshipped += got;

                System.out.printf("[%s] Day %d: Factory %d gets %d from Warehouse %d (balance=%d). Produced today=%d%n",
                        Thread.currentThread().getName(), day, id, got, w.getId(), w.getBalance(), got);

                // signal factory get done and wait for others
                CountDownLatch factoryGetDone = Project2Main.getFactoryGetDoneLatch();
                if (factoryGetDone != null) factoryGetDone.countDown();
                if (factoryGetDone != null) factoryGetDone.await();

                // 4.4 Check total products to ship and wait for all factories
                System.out.printf("[%s] Day %d: Factory %d checks products to ship: unshipped=%d%n",
                        Thread.currentThread().getName(), day, id, unshipped);

                CountDownLatch factoryCheckDone = Project2Main.getFactoryCheckDoneLatch();
                if (factoryCheckDone != null) factoryCheckDone.countDown();
                if (factoryCheckDone != null) factoryCheckDone.await();

                // 4.5 Send products to 1 random freight (one freight attempt only)
                int toShip = unshipped;
                int shipped = 0;
                if (toShip > 0 && freights.size() > 0) {
                    int fIdx = ThreadLocalRandom.current().nextInt(freights.size());
                    Freight f = freights.get(fIdx);
                    shipped = f.ship(toShip);
                    shippedTotal += shipped;
                    unshipped -= shipped;
                    System.out.printf("[%s] Day %d: Factory %d ships %d to Freight %d (freight remaining=%d). Unshipped now=%d%n",
                            Thread.currentThread().getName(), day, id, shipped, f.getId(), f.getRemaining(), unshipped);
                } else {
                    System.out.printf("[%s] Day %d: Factory %d ships 0 (no products or no freights). Unshipped=%d%n",
                            Thread.currentThread().getName(), day, id, unshipped);
                }

                // signal factory ship done and wait for others
                CountDownLatch factoryShipDone = Project2Main.getFactoryShipDoneLatch();
                if (factoryShipDone != null) factoryShipDone.countDown();
                if (factoryShipDone != null) factoryShipDone.await();

                // 4.6 Check unshipped and print
                System.out.printf("[%s] Day %d: Factory %d end-of-day unshipped=%d (producedTotal=%d, shippedTotal=%d)%n",
                        Thread.currentThread().getName(), day, id, unshipped, producedTotal, shippedTotal);

                // wait until end of day
                endDayBarrier.await();
            }

            // 4.7 After all days, print own summary (main will also aggregate later)
            double percent = (producedTotal == 0) ? 0.0 : 100.0 * shippedTotal / producedTotal;
            System.out.printf("[%s] Factory %d finished: produced=%d, shipped=%d, shipped%%=%.2f%%%n",
                    Thread.currentThread().getName(), id, producedTotal, shippedTotal, percent);

        } catch (Exception e) {
            System.out.printf("[%s] Factory %d error: %s%n",
                    Thread.currentThread().getName(), id, e.toString());
        }
    }

    // getters for main's final report
    public int getProducedTotal() {
        return producedTotal;
    }

    public int getShippedTotal() {
        return shippedTotal;
    }

    public String getFactoryName() {
    return Thread.currentThread().getName();
}

}
