package concurrent.hashtable;
import java.util.concurrent.ThreadLocalRandom;

// Driver class for testing my set implementations

public class HashTableSimulation {

    public static void main(String[] args) {

        // Local vars for timing and input params
        long timeBefore, timeAfter;
        int k = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);

        CoarseBlockingHashTable<String, Integer> coarseBlockingHashTable = new CoarseBlockingHashTable<String, Integer>();
        FineBlockingHashTable<String, Integer> fineBlockingHashTable     = new FineBlockingHashTable<String, Integer>();
        LockFreeHashTable<String, Integer> lockFreeHashTable             = new LockFreeHashTable<String, Integer>();

        // Initialise two four-thread arrays, to test the blocking and lock free implementations
        StringBuilder[] logsA = new StringBuilder[4];
        StringBuilder[] logsB = new StringBuilder[4];
        StringBuilder[] logsC = new StringBuilder[4];
        Thread[] tA           = new Thread[4];
        Thread[] tB           = new Thread[4];
        Thread[] tC           = new Thread[4];

        for (int i = 0; i < 4; i++) {
            logsA[i] = new StringBuilder();
            logsB[i] = new StringBuilder();
            logsC[i] = new StringBuilder();
            tA[i]    = new Thread(new CoarseBlockingHashTableTester(coarseBlockingHashTable, k, m, logsA[i]));
            tB[i]    = new Thread(new FineBlockingHashTableTester(fineBlockingHashTable, k, m, logsB[i]));
            tC[i]    = new Thread(new LockFreeHashTableTester(lockFreeHashTable, k, m, logsC[i]));
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
        System.out.println("CoarseBlockingHashTable execution time: " + (timeAfter - timeBefore) + "ms");

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
        System.out.println("FineBlockingHashTable execution time: " + (timeAfter - timeBefore) + "ms");

        // Time the execution of all threads in tC
        timeBefore = System.currentTimeMillis();
        for (Thread t : tC)
            t.start();
        for (Thread t : tC) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        timeAfter = System.currentTimeMillis();
        for (StringBuilder log : logsC)
            System.out.println(log);
        System.out.println("LockFreeHashTable execution time: " + (timeAfter - timeBefore) + "ms");
    }
}

// This class tests a CoarseBlockingHashTable implementation
class CoarseBlockingHashTableTester implements Runnable {

    // Private variables
    private final String[] sampleKeys = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t"};
    private CoarseBlockingHashTable<String, Integer> table;
    private ThreadLocalRandom rng;
    private int k;
    private int m;
    private StringBuilder log;

    // Basic constructor with shared CoarseBlockingHashTable reference
    public CoarseBlockingHashTableTester(CoarseBlockingHashTable<String, Integer> table, int k, int m, StringBuilder log) {
        this.table = table;
        this.rng   = ThreadLocalRandom.current();
        this.k     = k;
        this.m     = m;
        this.log   = log;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        int insertions = 0;
        int retrievals = 0;
        int removals   = 0;
        for (int i = 0; i < m; i++) {
            if (rng.nextInt(100) >= k) {
                table.put(sampleKeys[rng.nextInt(20)], rng.nextInt(20)); // Contains
                insertions++;
            } else {
                if (rng.nextBoolean()) {
                    table.get(sampleKeys[rng.nextInt(20)]);                    // Add
                    retrievals++;
                } else {
                    table.remove(sampleKeys[rng.nextInt(20)]);                 // Remove
                    removals++;
                }
            }
        }
        log.append("Thread ").append(Thread.currentThread().threadId()).append(": ")
            .append(insertions).append(" insertions, ")
            .append(retrievals).append(" retrievals, ")
            .append(removals).append(" removals");
    }
}

// This class tests a FineBlockingHashTable implementation
class FineBlockingHashTableTester implements Runnable {

    // Private variables
    private final String[] sampleKeys = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t"};
    private FineBlockingHashTable<String, Integer> table;
    private ThreadLocalRandom rng;
    private int k;
    private int m;
    private StringBuilder log;

    // Basic constructor with shared CoarseBlockingHashTable reference
    public FineBlockingHashTableTester(FineBlockingHashTable<String, Integer> table, int k, int m, StringBuilder log) {
        this.table = table;
        this.rng   = ThreadLocalRandom.current();
        this.k     = k;
        this.m     = m;
        this.log   = log;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        int insertions = 0;
        int retrievals = 0;
        int removals   = 0;
        for (int i = 0; i < m; i++) {
            if (rng.nextInt(100) >= k) {
                table.put(sampleKeys[rng.nextInt(20)], rng.nextInt(20)); // Contains
                insertions++;
            } else {
                if (rng.nextBoolean()) {
                    table.get(sampleKeys[rng.nextInt(20)]);              // Add
                    retrievals++;
                } else {
                    table.remove(sampleKeys[rng.nextInt(20)]);           // Remove
                    removals++;
                }
            }
        }
        log.append("Thread ").append(Thread.currentThread().threadId()).append(": ")
            .append(insertions).append(" insertions, ")
            .append(retrievals).append(" retrievals, ")
            .append(removals).append(" removals");
    }
}

// This class tests a LockFreeHashTable implementation
class LockFreeHashTableTester implements Runnable {

    // Private variables
    private final String[] sampleKeys = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t"};
    private LockFreeHashTable<String, Integer> table;
    private ThreadLocalRandom rng;
    private int k;
    private int m;
    private StringBuilder log;

    // Basic constructor with shared LockFreeHashTable reference
    public LockFreeHashTableTester(LockFreeHashTable<String, Integer> table, int k, int m, StringBuilder log) {
        this.table = table;
        this.rng   = ThreadLocalRandom.current();
        this.k     = k;
        this.m     = m;
        this.log   = log;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        int insertions = 0;
        int retrievals = 0;
        int removals   = 0;
        for (int i = 0; i < m; i++) {
            if (rng.nextInt(100) >= k) {
                table.put(sampleKeys[rng.nextInt(20)], rng.nextInt(20)); // Contains
                insertions++;
            } else {
                if (rng.nextBoolean()) {
                    table.get(sampleKeys[rng.nextInt(20)]);              // Add
                    retrievals++;
                } else {
                    table.remove(sampleKeys[rng.nextInt(20)]);           // Remove
                    removals++;
                }
            }
        }
        log.append("Thread ").append(Thread.currentThread().threadId()).append(": ")
            .append(insertions).append(" insertions, ")
            .append(retrievals).append(" retrievals, ")
            .append(removals).append(" removals");
    }
}