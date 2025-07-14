package concurrent.set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

// Driver class for testing my set implementations

public class SetSimulation {

    public static void main(String[] args) {

        // Constants
        final int NUM_THREADS        = 4;
        final int NUM_UNIQUE_OBJECTS = 500;
        final int DEADLOCK_TIMEOUT   = 5000;

        // Local vars for timing and input params
        long timeBefore, timeAfter;
        boolean deadlocked = false;
        int k = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);

        // Initialise two four-thread arrays, to test the blocking and lock free implementations
        CoarseBlockingSet<Object> coarseBlockingSet = new CoarseBlockingSet<Object>();
        FineBlockingSet<Object> fineBlockingSet     = new FineBlockingSet<Object>();
        LockFreeSet<Object> lockFreeSet             = new LockFreeSet<Object>();

        // Initialise a pool discrete pool of objects for simulating set usage
        Object[] uniqueObjects = new Object[NUM_UNIQUE_OBJECTS];
        for(int i=0; i<NUM_UNIQUE_OBJECTS; i++) {
            uniqueObjects[i] = new Object();
        }

        // Initialise the thread arrays and results objects
        SimResults rA = new SimResults("Coarse Blocking Set");
        SimResults rB = new SimResults("Fine Blocking Set");
        SimResults rC = new SimResults("Lock Free Set");
        Thread[] tA   = new Thread[NUM_THREADS];
        Thread[] tB   = new Thread[NUM_THREADS];
        Thread[] tC   = new Thread[NUM_THREADS];

        // Instantiate the threads in each thread array
        for (int i = 0; i < NUM_THREADS; i++) {
            tA[i] = new Thread(new SetTester<Object>(coarseBlockingSet, k, m, rA, uniqueObjects));
            tB[i] = new Thread(new SetTester<Object>(fineBlockingSet, k, m, rB, uniqueObjects));
            tC[i] = new Thread(new SetTester<Object>(lockFreeSet, k, m, rC, uniqueObjects));
        }

        // Print starting info
        System.out.printf("%d threads\n", NUM_THREADS);
        System.out.printf("%d operations per thread\n", m);
        System.out.printf("%d%% chance of search\n", k);
        System.out.printf("%d%% chance of insertion\n", (100-k)/2);
        System.out.printf("%d%% chance of retrieval\n\n", (100-k)/2);

        // Time the execution of all threads in tA
        timeBefore = System.currentTimeMillis();
        for (Thread t : tA)
            t.start();
        for (Thread t : tA) {
            try {
                if(!deadlocked)
                    t.join(DEADLOCK_TIMEOUT);
                if (t.isAlive()) {
                    deadlocked = true;
                    t.interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(!deadlocked) {
            // Stop timer and print logs
            timeAfter = System.currentTimeMillis();
            rA.actualSize.set(coarseBlockingSet.size());
            rA.expectedSize.addAndGet(rA.successfulInsertions.get() - rA.successfulRetrievals.get());
            rA.executionTime.set((int)(timeAfter - timeBefore));
            rA.printInfo();
        } else {
            deadlocked = false;
            System.out.printf("Deadlock detected, forcing continuation\n\n");
        }

        // Time execution of all threads in tB
        timeBefore = System.currentTimeMillis();
        for (Thread t : tB)
            t.start();
        for (Thread t : tB) {
            try {
                if(!deadlocked)
                    t.join(DEADLOCK_TIMEOUT);
                if (t.isAlive()) {
                    deadlocked = true;
                    t.interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(!deadlocked) {
            // Stop timer and print logs
            timeAfter = System.currentTimeMillis();
            rB.actualSize.set(fineBlockingSet.size());
            rB.expectedSize.addAndGet(rB.successfulInsertions.get() - rB.successfulRetrievals.get());
            rB.executionTime.set((int)(timeAfter - timeBefore));
            rB.printInfo();
        } else {
            deadlocked = false;
            System.out.printf("Deadlock detected, forcing continuation\n\n");
        }

        // Time the execution of all threads in tC
        timeBefore = System.currentTimeMillis();
        timeBefore = System.currentTimeMillis();
        for (Thread t : tC)
            t.start();
        for (Thread t : tC) {
            try {
                if(!deadlocked)
                    t.join(DEADLOCK_TIMEOUT);
                if (t.isAlive()) {
                    deadlocked = true;
                    t.interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(!deadlocked) {
            // Stop timer and print logs
            timeAfter = System.currentTimeMillis();
            rC.actualSize.set(lockFreeSet.size());
            rC.expectedSize.addAndGet(rC.successfulInsertions.get() - rC.successfulRetrievals.get());
            rC.executionTime.set((int)(timeAfter - timeBefore));
            rC.printInfo();
        } else {
            deadlocked = false;
            System.out.printf("Deadlock detected, forcing continuation\n\n");
        }
    }
}

// This class tests a Set implementation
class SetTester<T> implements Runnable {
    
    // Private variables
    private Set<T> set;
    private ThreadLocalRandom rng;
    private int k;
    private int m;
    private SimResults results;
    Object[] uniqueObjects;

    // Basic constructor with shared CoarseBlockingSet reference
    public SetTester(Set<T> set, int k, int m, SimResults results, Object[] uniqueObjects) {
        this.set           = set;
        this.rng           = ThreadLocalRandom.current();
        this.k             = k;
        this.m             = m;
        this.results       = results;
        this.uniqueObjects = uniqueObjects;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        for (int i = 0; i < m; i++) {
            if (rng.nextInt(100) >= k) {
                set.contains((T)uniqueObjects[rng.nextInt(uniqueObjects.length)]);      // Contains
                results.numSearches.incrementAndGet();
            } else {
                if (rng.nextBoolean()) {
                    if(set.add((T)uniqueObjects[rng.nextInt(uniqueObjects.length)]))    // Add
                        results.successfulInsertions.incrementAndGet();
                    else
                        results.failedInsertions.incrementAndGet();
                } else {
                    if(set.remove((T)uniqueObjects[rng.nextInt(uniqueObjects.length)])) // Remove
                        results.successfulRetrievals.incrementAndGet();
                    else
                        results.failedRetrievals.incrementAndGet();
                }
            }
        }
    }
}

// Helper class for returning simulation results
class SimResults {
    public String name;
    public AtomicInteger successfulInsertions;
    public AtomicInteger failedInsertions;
    public AtomicInteger successfulRetrievals;
    public AtomicInteger failedRetrievals;
    public AtomicInteger numSearches;
    public AtomicInteger actualSize;
    public AtomicInteger expectedSize;
    public AtomicInteger descrepancies;
    public AtomicInteger executionTime;

    // Basic constructor
    public SimResults(String name) {
        // Set name
        this.name = name;

        // Init all results to 0
        this.successfulInsertions = new AtomicInteger(0);
        this.failedInsertions     = new AtomicInteger(0);
        this.successfulRetrievals = new AtomicInteger(0);
        this.failedRetrievals     = new AtomicInteger(0);
        this.numSearches          = new AtomicInteger(0);
        this.actualSize           = new AtomicInteger(0);
        this.expectedSize         = new AtomicInteger(0);
        this.descrepancies        = new AtomicInteger(0);
        this.executionTime        = new AtomicInteger(0);
    }

    public void printInfo() {
        System.out.printf("<< %s >>\n\n", name);
        System.out.printf("Execution time: %d\n", executionTime.get());
        System.out.printf("Successful insertions: %d\n", successfulInsertions.get());
        System.out.printf("Failed insertions: %d\n", failedInsertions.get());
        System.out.printf("Successful retrievals: %d\n", successfulRetrievals.get());
        System.out.printf("Failed retrievals: %d\n", failedRetrievals.get());
        System.out.printf("Expected final size: %d\n", expectedSize.get());
        System.out.printf("Actual final size: %d\n", actualSize.get());
        System.out.printf("Discrepancies detected: %d\n\n", Math.abs(actualSize.get()-expectedSize.get()));
    }
}
