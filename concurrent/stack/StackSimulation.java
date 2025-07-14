package concurrent.stack;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

// Driver class for testing my stack implementations

public class StackSimulation {

    public static void main(String[] args) {

        // Constants
        final int NUM_THREADS = 4;
        final int DEADLOCK_TIMEOUT = 5000;

        // Local vars for timing and input params
        long timeBefore, timeAfter;
        boolean deadlocked = false;
        int k = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);

        // Initialise two four-thread arrays, to test each stack implementation independently
        Thread[] tA   = new Thread[NUM_THREADS];
        Thread[] tB   = new Thread[NUM_THREADS];
        SimResults rA = new SimResults("Blocking Stack");
        SimResults rB = new SimResults("Lock Free Stack");

        // Initialise each stack implementation, and their respective testers
        BlockingStack<Object> sA = new BlockingStack<Object>();
        LockFreeStack<Object> sB = new LockFreeStack<Object>();
        StackTester<Object> a    = new StackTester<Object>(sA, k, m, rA);
        StackTester<Object> b    = new StackTester<Object>(sB, k, m, rB);
        for(int i=0; i<NUM_THREADS; i++) {
            tA[i] = new Thread(a);
            tB[i] = new Thread(b);
        }

        // Print starting info
        System.out.printf("%d threads\n", NUM_THREADS);
        System.out.printf("%d operations per thread\n", m);
        System.out.printf("%d%% chance of pop\n", k);
        System.out.printf("%d%% chance of push\n\n", (100-k));

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
            int actualSize = 0;
            while(sA.pop() != null) actualSize++;
            rA.actualSize.set(actualSize);
            rA.expectedSize.addAndGet(rA.successfulPushes.get() - rA.successfulPops.get());
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
            int actualSize = 0;
            while(sB.pop() != null) actualSize++;
            rB.actualSize.set(actualSize);
            rB.expectedSize.addAndGet(rB.successfulPushes.get() - rB.successfulPops.get());
            rB.executionTime.set((int)(timeAfter - timeBefore));
            rB.printInfo();
        } else {
            deadlocked = false;
            System.out.printf("Deadlock detected, forcing continuation\n\n");
        }
    }   
}

// This class tests a Stack implementation
class StackTester<T> implements Runnable {
    
    // Private variables
    private Stack<T> stack;
    private ThreadLocalRandom rng;
    private int k;
    private int m;
    private SimResults results;

    // Basic constructor with shared Stack reference
    public StackTester(Stack<T> stack, int k, int m, SimResults results) {
        this.stack   = stack;
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
            if(rng.nextInt(100) >= k) {
                stack.push((T)new Object());                    // Push
                results.successfulPushes.incrementAndGet();
            } else {
                if (stack.pop() != null)                        // Pop
                    results.successfulPops.incrementAndGet();
                else
                    results.failedPops.incrementAndGet();

            }
        }
    }
}

// Helper class for returning simulation results
class SimResults {
    public String name;
    public AtomicInteger successfulPushes;
    public AtomicInteger failedPushes;
    public AtomicInteger successfulPops;
    public AtomicInteger failedPops;
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
        this.successfulPushes     = new AtomicInteger(0);
        this.failedPushes         = new AtomicInteger(0);
        this.successfulPops       = new AtomicInteger(0);
        this.failedPops           = new AtomicInteger(0);
        this.numSearches          = new AtomicInteger(0);
        this.actualSize           = new AtomicInteger(0);
        this.expectedSize         = new AtomicInteger(0);
        this.descrepancies        = new AtomicInteger(0);
        this.executionTime        = new AtomicInteger(0);
    }

    public void printInfo() {
        System.out.printf("<< %s >>\n\n", name);
        System.out.printf("Execution time: %dms\n", executionTime.get());
        System.out.printf("Successful pushes: %d\n", successfulPushes.get());
        System.out.printf("Failed pushes: %d\n", failedPushes.get());
        System.out.printf("Successful pops: %d\n", successfulPops.get());
        System.out.printf("Failed pops: %d\n", failedPops.get());
        System.out.printf("Expected final size: %d\n", expectedSize.get());
        System.out.printf("Actual final size: %d\n", actualSize.get());
        System.out.printf("Discrepancies detected: %d\n\n", Math.abs(actualSize.get()-expectedSize.get()));
    }
}