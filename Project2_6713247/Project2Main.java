package Project2_6713247;

/**
 *
 * @author Rachapon, 6713247
 *         Ratchasin, 6713247
 *         Sayklang, 6713250
 *         Chayapol, 6713223
 *         Zabit, 6713116
 */

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Project2Main {

    // Config values (set when reading config)
    private static int days;
    private static int warehouseNum;
    private static int freightNum;
    private static int freightMax;
    private static int supplierNum;
    private static int supplierMin;
    private static int supplierMax;
    private static int factoryNum;
    private static int factoryMax;

    // Shared resources
    private static List<Warehouse> warehouses;
    private static List<Freight> freights;

    // Reusable barriers (parties = main + suppliers + factories)
    private static CyclicBarrier startDayBarrier;
    private static CyclicBarrier endDayBarrier;

    // Per-day latches (volatile so threads see updates)
    private static volatile CountDownLatch supplierDoneLatch;
    private static volatile CountDownLatch factoryGetDoneLatch;
    private static volatile CountDownLatch factoryCheckDoneLatch;
    private static volatile CountDownLatch factoryShipDoneLatch;

    // Thread lists
    private static final List<SupplierThread> supplierThreads = new ArrayList<>();
    private static final List<FactoryThread> factoryThreads = new ArrayList<>();

    // ---- static getters used by threads ----
    public static int getDays() { return days; }
    public static CountDownLatch getSupplierDoneLatch() { return supplierDoneLatch; }
    public static CountDownLatch getFactoryGetDoneLatch() { return factoryGetDoneLatch; }
    public static CountDownLatch getFactoryCheckDoneLatch() { return factoryCheckDoneLatch; }
    public static CountDownLatch getFactoryShipDoneLatch() { return factoryShipDoneLatch; }
    public static List<Warehouse> getWarehouses() { return warehouses; }
    public static List<Freight> getFreights() { return freights; }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
//String path =  "src/main/java/Ex7_6713247";//netbeans

            String path = "Project2_6713247/";

        // read config (loop until valid file)
        while (true) {
            System.out.println("New file name = ");
            

            String str = sc.nextLine().trim();

            String filename = path +str;
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("java.io.FileNotFoundException: " + file.getAbsolutePath() + " (The system cannot find the file specified)");
                continue;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split(",");
                    for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();

                    switch (parts[0]) {
                        case "days":
                            days = Integer.parseInt(parts[1]);
                            break;
                        case "warehouse_num":
                            warehouseNum = Integer.parseInt(parts[1]);
                            break;
                        case "freight_num_max":
                            freightNum = Integer.parseInt(parts[1]);
                            freightMax = Integer.parseInt(parts[2]);
                            break;
                        case "supplier_num_min_max":
                            supplierNum = Integer.parseInt(parts[1]);
                            supplierMin = Integer.parseInt(parts[2]);
                            supplierMax = Integer.parseInt(parts[3]);
                            break;
                        case "factory_num_max":
                            factoryNum = Integer.parseInt(parts[1]);
                            factoryMax = Integer.parseInt(parts[2]);
                            break;
                    }
                }

                System.out.println("Configuration loaded successfully!");
                System.out.println("days = " + days);
                System.out.println("warehouse_num = " + warehouseNum);
                System.out.println("freight_num_max = " + freightNum + ", " + freightMax);
                System.out.println("supplier_num_min_max = " + supplierNum + ", " + supplierMin + ", " + supplierMax);
                System.out.println("factory_num_max = " + factoryNum + ", " + factoryMax);

                break;
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected: " + e.toString());
            }
        }

        // initialize shared lists
        warehouses = new ArrayList<>();
        for (int i = 1; i <= warehouseNum; i++) warehouses.add(new Warehouse(i));

        freights = new ArrayList<>();
        for (int i = 1; i <= freightNum; i++) freights.add(new Freight(i, freightMax));

        // parties for barriers: main + suppliers + factories
        int parties = 1 + supplierNum + factoryNum;
        startDayBarrier = new CyclicBarrier(parties);
        endDayBarrier = new CyclicBarrier(parties);

        // create and start supplier threads
        for (int i = 1; i <= supplierNum; i++) {
            SupplierThread s = new SupplierThread(i, supplierMin, supplierMax, warehouses, startDayBarrier, endDayBarrier);
            supplierThreads.add(s);
            s.start();
        }

        // create and start factory threads
        for (int i = 1; i <= factoryNum; i++) {
            FactoryThread f = new FactoryThread(i, factoryMax, warehouses, freights, startDayBarrier, endDayBarrier);
            factoryThreads.add(f);
            f.start();
        }

        // MAIN loop through days
        for (int day = 1; day <= days; day++) {
            // create fresh per-day latches
            supplierDoneLatch = new CountDownLatch(supplierNum);
            factoryGetDoneLatch = new CountDownLatch(factoryNum);
            factoryCheckDoneLatch = new CountDownLatch(factoryNum);
            factoryShipDoneLatch = new CountDownLatch(factoryNum);

            // reset freight capacities at start of day
            for (Freight f : freights) f.reset();

            // print day header and current status
            System.out.printf("[%s] ---- Day %d ----%n", Thread.currentThread().getName(), day);
            System.out.print("[" + Thread.currentThread().getName() + "] Warehouses: ");
            for (Warehouse w : warehouses) System.out.print(w + " ");
            System.out.println();
            System.out.print("[" + Thread.currentThread().getName() + "] Freights:    ");
            for (Freight f : freights) System.out.print(f + " ");
            System.out.println();
            System.out.println("[" + Thread.currentThread().getName() + "] ----------------");

            // release suppliers & factories to start the day's actions
            try {
                startDayBarrier.await(); // main participates
            } catch (Exception e) {
                System.out.println("[" + Thread.currentThread().getName() + "] startDayBarrier error: " + e.toString());
            }

            // wait until end of day (main participates)
            try {
                endDayBarrier.await();
            } catch (Exception e) {
                System.out.println("[" + Thread.currentThread().getName() + "] endDayBarrier error: " + e.toString());
            }

            System.out.printf("[%s] ==== End of Day %d ====%n%n", Thread.currentThread().getName(), day);
        }

        // wait all threads finish
        for (SupplierThread s : supplierThreads) {
            try { s.join(); } catch (InterruptedException ignored) {}
        }
        for (FactoryThread f : factoryThreads) {
            try { f.join(); } catch (InterruptedException ignored) {}
        }

        // 5.3 Report factory performance sorted by producedTotal (desc) then name
        List<FactoryThread> reportList = new ArrayList<>(factoryThreads);
        reportList.sort((a, b) -> {
            int p = Integer.compare(b.getProducedTotal(), a.getProducedTotal()); // desc
            if (p != 0) return p;
            return a.getName().compareTo(b.getName()); // tie-breaker by name
        });

        System.out.println("[" + Thread.currentThread().getName() + "] Final report (sorted):");
        System.out.printf("[%s] %-12s %-10s %-10s %-10s%n", Thread.currentThread().getName(),
                "Thread", "Produced", "Shipped", "Shipped%");
        for (FactoryThread f : reportList) {
            int produced = f.getProducedTotal();
            int shipped = f.getShippedTotal();
            double perc = (produced == 0) ? 0.0 : (100.0 * shipped / produced);
            System.out.printf("[%s] %-12s %-10d %-10d %-9.2f%%%n",
                    Thread.currentThread().getName(), f.getName(), produced, shipped, perc);
        }

        System.out.println("[" + Thread.currentThread().getName() + "] Simulation complete.");
        sc.close();
    }
}
