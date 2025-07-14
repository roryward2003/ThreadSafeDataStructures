package concurrent.deque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

// Driver class for testing my FIFO deque implementations

public class DequeSimulation {

    public static void main(String[] args) {

        // Constants
        final int NUM_THREADS = 4;
        final int DEADLOCK_TIMEOUT = 5000;

        // Local vars for timing and input params
        long timeBefore, timeAfter;
        boolean deadlocked = false;
        int k = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);

        // Initialise the results objects
        SimResults rA = new SimResults("Blocking Deque");
        SimResults rB = new SimResults("Lock Free Deque");
        
        // Initialise the deques and deque testers
        BlockingDeque<Object> dA = new BlockingDeque<Object>();
        LockFreeDeque<Object> dB = new LockFreeDeque<Object>();
        DequeTester<Object> a    = new DequeTester<Object>(dA, k, m, rA);
        DequeTester<Object> b    = new DequeTester<Object>(dB, k, m, rB);
        
        // Initialise the thread arrays and threads
        Thread[] tA = new Thread[NUM_THREADS];
        Thread[] tB = new Thread[NUM_THREADS];
        for(int i=0; i<NUM_THREADS; i++) {
            tA[i] = new Thread(a);
            tB[i] = new Thread(b);
        }

        // Print starting info
        System.out.printf("%d threads\n", NUM_THREADS);
        System.out.printf("%d operations per thread\n", m);
        System.out.printf("%d%% chance of addition\n", (100-k));
        System.out.printf("%d%% chance of peek\n", (k/2));
        System.out.printf("%d%% chance of removal\n\n", (k/2));
        
        // Time the execution of all threads in tA
        timeBefore = System.currentTimeMillis();
        for (Thread t : tA)
            t.start();
        for (Thread t : tA) {
            try {
                if (!deadlocked)
                    t.join(DEADLOCK_TIMEOUT);
                if (t.isAlive()) {
                    deadlocked = true;
                    t.interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        if (!deadlocked) {
            // Stop timer and print logs
            timeAfter = System.currentTimeMillis();
            rA.actualSize.set(dA.size());
            rA.expectedSize.addAndGet(rA.successfulAdditions.get() - rA.successfulRemovals.get());
            rA.executionTime.set((int)(timeAfter - timeBefore));
            rA.printInfo();
        } else {
            deadlocked = false;
            System.out.printf("Deadlock detected, forcing continuation\n\n");
        }
        
        // Time the execution of all threads in tB
        timeBefore = System.currentTimeMillis();
        for (Thread t : tB)
            t.start();
        for (Thread t : tB) {
            try {
                if (!deadlocked)
                    t.join(DEADLOCK_TIMEOUT);
                if (t.isAlive()) {
                    deadlocked = true;
                    t.interrupt();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!deadlocked) {
            // Stop timer and print logs
            timeAfter = System.currentTimeMillis();
            rB.actualSize.set(dB.size());
            rB.expectedSize.addAndGet(rB.successfulAdditions.get() - rB.successfulRemovals.get());
            rB.executionTime.set((int)(timeAfter - timeBefore));
            rB.printInfo();
        } else {
            deadlocked = false;
            System.out.printf("Deadlock detected, forcing continuation\n\n");
        }
    }   
}

// This class tests a BlockingDeque implementation
class DequeTester<T> implements Runnable {
    
    // Private variables
    private Deque<T> deque;
    private ThreadLocalRandom rng;
    private int k;
    private int m;
    private SimResults results;

    // Basic constructor with shared BlockingDeque reference
    public DequeTester(Deque<T> deque, int k, int m, SimResults results) {
        this.deque   = deque;
        this.rng     = ThreadLocalRandom.current();
        this.k       = k;
        this.m       = m;
        this.results = results;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        for(int i=0; i<m; i++) {
            if(rng.nextInt(100) >= k  || deque.isEmpty()) {
                if(rng.nextBoolean()) {                              // Add
                    deque.addFirst((T)new Object());
                } else {
                    deque.addLast((T)new Object());
                }
                results.successfulAdditions.incrementAndGet();
            } else {
                if(rng.nextBoolean()) {                              // Peek/Get
                    if(rng.nextBoolean()) {
                        deque.getFirst();
                    } else {
                        deque.getLast();
                    }
                    results.successfulPeeks.incrementAndGet();
                } else { 
                    if(rng.nextBoolean()) {                          // Remove
                        if(deque.removeFirst() != null) {
                            results.successfulRemovals.incrementAndGet();
                        } else {
                            results.failedRemovals.incrementAndGet();
                        }
                    } else {
                        if(deque.removeLast() != null) {
                            results.successfulRemovals.incrementAndGet();
                        } else {
                            results.failedRemovals.incrementAndGet();
                        }
                    }
                }
            }
        }
    }
}

// Helper class for returning simulation results
class SimResults {
    public String name;
    public AtomicInteger successfulAdditions;
    public AtomicInteger failedAdditions;
    public AtomicInteger successfulRemovals;
    public AtomicInteger failedRemovals;
    public AtomicInteger successfulPeeks;
    public AtomicInteger failedPeeks;
    public AtomicInteger actualSize;
    public AtomicInteger expectedSize;
    public AtomicInteger descrepancies;
    public AtomicInteger executionTime;

    // Basic constructor
    public SimResults(String name) {
        // Set name
        this.name = name;

        // Init all results to 0
        this.successfulAdditions = new AtomicInteger(0);
        this.failedAdditions     = new AtomicInteger(0);
        this.successfulRemovals  = new AtomicInteger(0);
        this.failedRemovals      = new AtomicInteger(0);
        this.successfulPeeks     = new AtomicInteger(0);
        this.failedPeeks         = new AtomicInteger(0);
        this.actualSize          = new AtomicInteger(0);
        this.expectedSize        = new AtomicInteger(0);
        this.descrepancies       = new AtomicInteger(0);
        this.executionTime       = new AtomicInteger(0);
    }

    public void printInfo() {
        System.out.printf("<< %s >>\n\n", name);
        System.out.printf("Execution time: %dms\n", executionTime.get());
        System.out.printf("Successful insertions: %d\n", successfulAdditions.get());
        System.out.printf("Failed insertions: %d\n", failedAdditions.get());
        System.out.printf("Successful removals: %d\n", successfulRemovals.get());
        System.out.printf("Failed removals: %d\n", failedRemovals.get());
        System.out.printf("Successful peeks: %d\n", successfulPeeks.get());
        System.out.printf("Failed peeks: %d\n", failedPeeks.get());
        System.out.printf("Expected final size: %d\n", expectedSize.get());
        System.out.printf("Actual final size: %d\n", actualSize.get());
        System.out.printf("Discrepancies detected: %d\n\n", Math.abs(actualSize.get()-expectedSize.get()));
    }
}