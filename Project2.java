import java.io.*;
import java.util.*;

public class Project2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String fileName;
        int days = 0, warehouseNum = 0, freightNum = 0, freightCap = 0;
        int supplierNum = 0, supplierMin = 0, supplierMax = 0;
        int factoryNum = 0, factoryMaxProd = 0;

        // ===== อ่าน config.txt =====
        while (true) {
            System.out.print("Enter config file name: ");
            fileName = sc.nextLine();
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileName));

                String[] l1 = br.readLine().split(",");
                days = Integer.parseInt(l1[1].trim());
                String[] l2 = br.readLine().split(",");
                warehouseNum = Integer.parseInt(l2[1].trim());
                String[] l3 = br.readLine().split(",");
                freightNum = Integer.parseInt(l3[1].trim());
                freightCap = Integer.parseInt(l3[2].trim());
                String[] l4 = br.readLine().split(",");
                supplierNum = Integer.parseInt(l4[1].trim());
                supplierMin = Integer.parseInt(l4[2].trim());
                supplierMax = Integer.parseInt(l4[3].trim());
                String[] l5 = br.readLine().split(",");
                factoryNum = Integer.parseInt(l5[1].trim());
                factoryMaxProd = Integer.parseInt(l5[2].trim());

                br.close();
                break;
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

        // ===== สร้าง warehouse =====
        ArrayList<Warehouse> warehouses = new ArrayList<>();
        for (int i = 0; i < warehouseNum; i++) {
            warehouses.add(new Warehouse());
        }

        // ===== สร้าง freight =====
        ArrayList<Freight> freights = new ArrayList<>();
        for (int i = 0; i < freightNum; i++) {
            freights.add(new Freight(freightCap));
        }

        System.out.println(Thread.currentThread().getName() + " >> ============== Parameters =============");
        System.out.println(Thread.currentThread().getName() + " >> Day of simulation : " + days);
        System.out.println(Thread.currentThread().getName() + " >> Warehouse number : " + warehouseNum);
        System.out.println(Thread.currentThread().getName() + " >> Freight number : " + freightNum + ", max capacity = " + freightCap);
        System.out.println(Thread.currentThread().getName() + " >> Supplier num,min,max: " + supplierNum + "," + supplierMin + "," + supplierMax);
        System.out.println(Thread.currentThread().getName() + " >> Daily production : max = " + factoryMaxProd);
        System.out.println(Thread.currentThread().getName() + " >> ========================================");

        // ===== สร้าง supplier threads =====
        ArrayList<SupplierThread> suppliers = new ArrayList<>();
        for (int i = 0; i < supplierNum; i++) {
            SupplierThread s = new SupplierThread(supplierMin, supplierMax, warehouses, days);
            s.setName("SupplierThread_" + i);
            suppliers.add(s);
            s.start();
        }

        // ===== สร้าง factory threads =====
        ArrayList<FactoryThread> factories = new ArrayList<>();
        for (int i = 0; i < factoryNum; i++) {
            FactoryThread f = new FactoryThread(factoryMaxProd, warehouses, freights, days);
            f.setName("FactoryThread_" + i);
            factories.add(f);
            f.start();
        }

        // ===== จำลองวัน =====
        for (int d = 1; d <= days; d++) {
            System.out.println(Thread.currentThread().getName() + " >> ================= Day " + d + " =================");

            synchronized (Project2.class) {
                Project2.class.notifyAll();
            }

            try { Thread.sleep(500); } catch (InterruptedException e) {}

            // แสดง warehouse balance
            for (int i = 0; i < warehouses.size(); i++) {
                System.out.println(Thread.currentThread().getName() + " >> Warehouse_" + i + " balance = " + warehouses.get(i).getBalance());
            }

            // reset freight หลังแต่ละวัน
            for (Freight f : freights) {
                f.reset();
            }
        }

        // ===== Summary หลัง simulation =====
        System.out.println(Thread.currentThread().getName() + " >> ============== Summary =============");
        factories.sort((a, b) -> {
            if (b.getTotalProduced() != a.getTotalProduced())
                return b.getTotalProduced() - a.getTotalProduced();
            else
                return a.getName().compareTo(b.getName());
        });

        for (FactoryThread f : factories) {
            double pct = f.getTotalProduced() == 0 ? 0.0 : (f.getTotalShipped() * 100.0 / f.getTotalProduced());
            System.out.printf("%s >> %s total product = %d shipped = %d (%.2f%%)%n",
                    Thread.currentThread().getName(),
                    f.getName(),
                    f.getTotalProduced(),
                    f.getTotalShipped(),
                    pct);
        }
    }
}


