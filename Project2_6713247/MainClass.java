package Project2_6713247;

/**
 * @author Rachapon, 6713247
 *         Ratchasin, 6713247
 */

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class MainClass {
    private String configFilename;
    private ArrayList<Warehouse> warehouses;
    private ArrayList<Freight> freights;
    private ArrayList<SupplierThread> suppliers;
    private ArrayList<FactoryThread> factories;
    
    private int days;
    private int warehouseNum;
    private int freightNum;
    private int freightMaxCapacity;
    private int supplierNum;
    private int supplierMin;
    private int supplierMax;
    private int factoryNum;
    private int factoryMax;
    
    public MainClass() {
        this.configFilename = "Project2_6713247/config_1.txt";//Githubs
        //this.configFilename = "src/main/java/Project2_6713247/config.txt"; //Netbeans
        this.warehouses = new ArrayList<>();
        this.freights = new ArrayList<>();
        this.suppliers = new ArrayList<>();
        this.factories = new ArrayList<>();
    }
    
    public static void main(String[] args) {
        MainClass app = new MainClass();
        try {
            app.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void run() throws Exception {
        readConfigFile();
        createObjects();
        createAndStartThreads();
        waitForThreads();
        printSummary();
    }
    
    private void readConfigFile() throws IOException {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            try {
                File file = new File(configFilename);
                if (!file.exists()) throw new FileNotFoundException();
                
                BufferedReader br = new BufferedReader(new FileReader(configFilename));
                parseConfigFile(br);
                br.close();
                break;
            } catch (FileNotFoundException e) {
                System.out.println("java.io.FileNotFoundException: " + configFilename);
                System.out.println("New file name = ");
                String newFilename = scanner.nextLine().trim();
                configFilename = "Project2_6713247/" + newFilename;//Github
                //configFilename = "src/main/java/Project2_6713247/" + newFilename;//Netbeans
            }
        }
    }
    
    private void parseConfigFile(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            String key = parts[0].trim();
            
            if (key.equals("days")) {
                days = Integer.parseInt(parts[1].trim());
            } else if (key.equals("warehouse_num")) {
                warehouseNum = Integer.parseInt(parts[1].trim());
            } else if (key.equals("freight_num_max")) {
                freightNum = Integer.parseInt(parts[1].trim());
                freightMaxCapacity = Integer.parseInt(parts[2].trim());
            } else if (key.equals("supplier_num_min_max")) {
                supplierNum = Integer.parseInt(parts[1].trim());
                supplierMin = Integer.parseInt(parts[2].trim());
                supplierMax = Integer.parseInt(parts[3].trim());
            } else if (key.equals("factory_num_max")) {
                factoryNum = Integer.parseInt(parts[1].trim());
                factoryMax = Integer.parseInt(parts[2].trim());
            }
        }
    }
    
    private void createObjects() {
        for (int i = 0; i < warehouseNum; i++) {
            warehouses.add(new Warehouse("Warehouse_" + i));
        }
        for (int i = 0; i < freightNum; i++) {
            freights.add(new Freight("Freight_" + i, freightMaxCapacity));
        }
    }
    
    private void printParameters() {
    
    System.out.printf("%s%s  >>  %s Parameters %s\n"," ".repeat(12), Thread.currentThread().getName(), "=".repeat(20), "=".repeat(20));
    System.out.printf("%s%s  >>  Days of simulation : %d\n"," ".repeat(12), Thread.currentThread().getName(), days);
    System.out.printf("%s%s  >>  Warehouses         : %s\n"," ".repeat(12), Thread.currentThread().getName(), warehouses);
    System.out.printf("%s%s  >>  Freights           : %s\n"," ".repeat(12), Thread.currentThread().getName(), freights);
    
    System.out.printf("%s%s  >>  Freight capacity   : max = %d\n"," ".repeat(12), Thread.currentThread().getName(), freightMaxCapacity);
    
    System.out.printf("%s%s  >>  SupplierThreads    : %s\n"," ".repeat(12), Thread.currentThread().getName(), 
        getThreadNames(suppliers));
    
    System.out.printf("%s%s  >>  Daily supply       : min = %d, max = %d\n"," ".repeat(12), Thread.currentThread().getName(), supplierMin, supplierMax);
    
    System.out.printf("%s%s  >>  FactoryThreads     : %s\n"," ".repeat(12), Thread.currentThread().getName(), 
        getThreadNames(factories));
    
    System.out.printf("%s%s  >>  Daily production   : max = %d\n"," ".repeat(12), Thread.currentThread().getName(), factoryMax);
    
    System.out.printf("%s%s  >>\n"," ".repeat(12), Thread.currentThread().getName());
}

// Helper function: ArrayList(Warehouse/Freight) to Array string
private String getArrayNames(ArrayList<?> list) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < list.size(); i++) {
        if (i > 0) sb.append(", ");
        sb.append(list.get(i).toString());
    }
    sb.append("]");
    return sb.toString();
}

// Helper function: ArrayList(Thread) to Array string
private String getThreadNames(ArrayList<? extends Thread> list) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < list.size(); i++) {
        if (i > 0) sb.append(", ");
        sb.append(list.get(i).getName());
    }
    sb.append("]");
    return sb.toString();
}

    
    private void createAndStartThreads() throws Exception {
        int dayStartParties = 1 + supplierNum + factoryNum;
        CyclicBarrier dayStartBarrier = new CyclicBarrier(dayStartParties);
        CyclicBarrier dayEndBarrier = new CyclicBarrier(dayStartParties);
        CyclicBarrier supplierDoneBarrier = new CyclicBarrier(supplierNum + factoryNum);
        CyclicBarrier putDoneBarrier = new CyclicBarrier(factoryNum);    
        CyclicBarrier getDoneBarrier = new CyclicBarrier(factoryNum);   
        CyclicBarrier totalDoneBarrier = new CyclicBarrier(factoryNum);   
        CyclicBarrier shipDoneBarrier = new CyclicBarrier(factoryNum);

        for (int i = 0; i < supplierNum; i++) {
            SupplierThread s = new SupplierThread(
                "SupplierThread_" + i, warehouses, supplierMin, supplierMax, days,
                dayStartBarrier, dayEndBarrier, supplierDoneBarrier
            );
            suppliers.add(s);
            s.start();
        }
        
        for (int i = 0; i < factoryNum; i++) {
            FactoryThread f = new FactoryThread(
                "FactoryThread_" + i, warehouses, freights, factoryMax, days,
                dayStartBarrier, dayEndBarrier, supplierDoneBarrier,
                putDoneBarrier,    
            getDoneBarrier, 
            totalDoneBarrier, 
            shipDoneBarrier
            );
            factories.add(f);
            f.start();
        }
        printParameters();

        for (int day = 1; day <= days; day++) {
            System.out.printf("%s%s  >>  %s\n"," ".repeat(12), Thread.currentThread().getName(),"=".repeat(52));
            System.out.printf("%s%s  >>  Day %d\n"," ".repeat(12), Thread.currentThread().getName(), day);
            
            for (Warehouse w : warehouses) {
                System.out.printf("%s%s  >>  %s balance = %d\n"," ".repeat(12), Thread.currentThread().getName(), w.getName(), w.getBalance());
            }
            
            for (Freight f : freights) {
                f.reset();
                System.out.printf("%s%s  >>  %s capacity = %d\n"," ".repeat(12), Thread.currentThread().getName(), f.getName(), f.getCapacity());
            }
            System.out.printf("%s%s  >>  \n"," ".repeat(12), Thread.currentThread().getName());
            
            dayStartBarrier.await();
            dayEndBarrier.await();
        }
    }
    
    private void waitForThreads() throws InterruptedException {
        for (SupplierThread s : suppliers) s.join();
        for (FactoryThread f : factories) f.join();
    }
    
    private void printSummary() {
        Collections.sort(factories, (f1, f2) -> {
            int cmp = Integer.compare(f2.getTotalCreated(), f1.getTotalCreated());
            if (cmp != 0) return cmp;
            return f1.getName().compareTo(f2.getName());
        });
        
        System.out.printf("%s%s  >>  \n"," ".repeat(12), Thread.currentThread().getName());
        System.out.printf("%s%s  >>  %s\n"," ".repeat(12), Thread.currentThread().getName(),"=".repeat(52));
        System.out.printf("%s%s  >>  Summary\n"," ".repeat(12), Thread.currentThread().getName());
        
        for (FactoryThread f : factories) {
            System.out.printf("%s%s  >>  %s    total = %5d    shipped = %5d (%6.2f%%)\n"," ".repeat(12),
                Thread.currentThread().getName(),f.getName(),
                f.getTotalCreated(), f.getTotalShipped(), f.getShipPercentage());
        }
    }
}