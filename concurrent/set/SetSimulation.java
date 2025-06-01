package src.set;
import java.util.concurrent.ThreadLocalRandom;

// Driver class for testing my set implementations

public class SetSimulation {

    public static void main(String[] args) {

        // Local vars for timing and input params
        long timeBefore, timeAfter;
        int k = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);

        // Initialise two four-thread arrays, to test the blocking and lock free implementations
        BlockingSet blockingSet = new BlockingSet();
        LockFreeSet lockFreeSet = new LockFreeSet();
        StringBuilder[] logsA   = new StringBuilder[4];
        StringBuilder[] logsB   = new StringBuilder[4];
        Thread[] tA             = new Thread[4];
        Thread[] tB             = new Thread[4];

        for (int i = 0; i < 4; i++) {
            logsA[i] = new StringBuilder();
            logsB[i] = new StringBuilder();
            tA[i]    = new Thread(new BlockingSetTester(blockingSet, k, m, logsA[i]));
            tB[i]    = new Thread(new LockFreeSetTester(lockFreeSet, k, m, logsB[i]));
        }

        // Time the execution of all threads in tA
        timeBefore = System.currentTimeMillis();
        for (Thread t : tA)
            t.start();
        for (Thread t : tA) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        timeAfter = System.currentTimeMillis();
        for (StringBuilder log : logsA)
            System.out.println(log);
        System.out.println("BlockingSet execution time: " + (timeAfter - timeBefore) + "ms");

        // Time the execution of all threads in tB
        timeBefore = System.currentTimeMillis();
        for (Thread t : tB)
            t.start();
        for (Thread t : tB) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        timeAfter = System.currentTimeMillis();
        for (StringBuilder log : logsB)
            System.out.println(log);
        System.out.println("LockFreeSet execution time: " + (timeAfter - timeBefore) + "ms");
    }
}

// This class tests a BlockingSet implementation
class BlockingSetTester implements Runnable {

    // Private variables
    private BlockingSet set;
    private ThreadLocalRandom rng;
    private int k;
    private int m;
    private StringBuilder log;

    // Basic constructor with shared BlockingSet reference
    public BlockingSetTester(BlockingSet set, int k, int m, StringBuilder log) {
        this.set = set;
        this.rng = ThreadLocalRandom.current();
        this.k   = k;
        this.m   = m;
        this.log = log;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        int insertions = 0;
        int retrievals = 0;
        int searches   = 0;
        for (int i = 0; i < m; i++) {
            if (rng.nextInt(100) >= k) {
                set.contains(new Object());                // Contains
                searches++;
            } else {
                if (rng.nextBoolean()) {
                    set.add(new Object());                 // Add
                    insertions++;
                } else {
                    set.remove(new Object());              // Remove
                    retrievals++;
                }
            }
        }
        log.append("Thread ").append(Thread.currentThread().threadId()).append(": ")
            .append(insertions).append(" insertions, ")
            .append(retrievals).append(" retrievals, ")
            .append(searches).append(" searches");
    }
}

// This class tests a LockFreeSet implementation
class LockFreeSetTester implements Runnable {

    // Private variables
    private LockFreeSet set;
    private ThreadLocalRandom rng;
    private int k;
    private int m;
    private StringBuilder log;

    // Basic constructor with shared LockFreeSet reference
    public LockFreeSetTester(LockFreeSet set, int k, int m, StringBuilder log) {
        this.set = set;
        this.rng = ThreadLocalRandom.current();
        this.k   = k;
        this.m   = m;
        this.log = log;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        int insertions = 0;
        int retrievals = 0;
        int searches   = 0;
        for (int i = 0; i < m; i++) {
            if (rng.nextInt(100) >= k) {
                set.contains(new Object());                // Contains
                searches++;
            } else {
                if (rng.nextBoolean()) {
                    set.add(new Object());                 // Add
                    insertions++;
                } else {
                    set.remove(new Object());              // Remove
                    retrievals++;
                }
            }
        }
        log.append("Thread ").append(Thread.currentThread().threadId()).append(": ")
            .append(insertions).append(" insertions, ")
            .append(retrievals).append(" retrievals, ")
            .append(searches).append(" searches");
    }
}
