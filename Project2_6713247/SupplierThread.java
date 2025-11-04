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

public class SupplierThread extends Thread {
    private final int id;
    private final int minDaily;
    private final int maxDaily;
    private final List<Warehouse> warehouses;
    private final CyclicBarrier startDayBarrier;
    private final CyclicBarrier endDayBarrier;

    public SupplierThread(int id, int minDaily, int maxDaily,
                          List<Warehouse> warehouses,
                          CyclicBarrier startDayBarrier, CyclicBarrier endDayBarrier) {
        this.id = id;
        this.minDaily = minDaily;
        this.maxDaily = maxDaily;
        this.warehouses = warehouses;
        this.startDayBarrier = startDayBarrier;
        this.endDayBarrier = endDayBarrier;
        setName("Supplier-" + id);
    }

    @Override
    public void run() {
        try {
            int days = Project2Main.getDays();
            for (int day = 1; day <= days; day++) {
                // 3.1 Wait until main prints day header and resets freights
                startDayBarrier.await();

                // 3.2 Put materials in 1 random warehouse
                int supply = ThreadLocalRandom.current().nextInt(minDaily, maxDaily + 1);
                int wIdx = ThreadLocalRandom.current().nextInt(warehouses.size());
                Warehouse w = warehouses.get(wIdx);
                w.put(supply);

                System.out.printf("[%s] Day %d: Supplier %d puts %d into Warehouse %d (balance=%d)%n",
                        Thread.currentThread().getName(), day, id, supply, w.getId(), w.getBalance());

                // signal supplier done for this day
                CountDownLatch supplierDone = Project2Main.getSupplierDoneLatch();
                if (supplierDone != null) supplierDone.countDown();

                // wait until end of day
                endDayBarrier.await();
            }
        } catch (Exception e) {
            System.out.printf("[%s] Supplier %d error: %s%n",
                    Thread.currentThread().getName(), id, e.toString());
        }
    }
}