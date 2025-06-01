package concurrent.linkedlist;
import java.util.concurrent.ThreadLocalRandom;

// Driver class for testing my linked list implementations

public class LLSimulation {

    public static void main(String[] args) {

        // Local vars for timing and input params
        long timeBefore, timeAfter;
        int k = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);

        // Initialise two four-thread arrays, to test the blocking and lock free lists respectively
        BlockingLLTester a = new BlockingLLTester(new BlockingLL(), k, m);
        LockFreeLLTester b = new LockFreeLLTester(new LockFreeLL(), k, m);
        Thread[] tA        = new Thread[4];
        Thread[] tB        = new Thread[4];

        for(int i=0; i<4; i++) {
            tA[i] = new Thread(a);
            tB[i] = new Thread(b);
        }
        
        // Time the execution of all threads in tA
        timeBefore = System.currentTimeMillis();
        for(Thread t : tA)
            t.start();
        for(Thread t : tA) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        timeAfter = System.currentTimeMillis();
        System.out.println("BlockingLL execution time: "+(timeAfter-timeBefore)+"ms");
        
        // Time the execution of all threads in tB
        timeBefore = System.currentTimeMillis();
        for(Thread t : tB)
            t.start();
        for(Thread t : tB) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        timeAfter = System.currentTimeMillis();
        System.out.println("LockFreeLL execution time: "+(timeAfter-timeBefore)+"ms");
    }   
}

// This class tests a BlockingLL implementation
class BlockingLLTester implements Runnable {
    
    // Private variables
    private BlockingLL list;
    private ThreadLocalRandom rng;
    private int k;
    private int m;

    // Basic constructor with shared BlockingLL reference
    public BlockingLLTester(BlockingLL list, int k, int m) {
        this.list = list;
        this.rng  = ThreadLocalRandom.current();
        this.k    = k;
        this.m    = m;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    // Size check and subsequent usage is not atomic but this is just a simulation so that
    // is okay, and also incidentally provides testing for out of bounds accesses.
    @Override
    public void run() {
        int bound         = 0;
        int oobExceptions = 0;
        int insertions    = 0;
        int retrievals    = 0;
        for(int i=0; i<m; i++) {
            bound = Integer.max(1, bound);
            try {
                if(rng.nextInt(100) >= k) {
                    if(rng.nextBoolean()) {
                        list.add(new Object());                // Add
                    } else {
                        list.add(rng.nextInt(bound), new Object()); // Add
                    }
                    insertions++;
                } else {
                    if(rng.nextBoolean()) {
                        list.get(rng.nextInt(bound));          // Get
                    } else {
                        list.remove(rng.nextInt(bound));       // Remove
                    }
                    retrievals++;
                }
            } catch(IndexOutOfBoundsException e) {
                oobExceptions++;
            }
        }
        System.out.println("Thread "+Thread.currentThread().threadId()+": "+insertions+" insertions, "+
            retrievals+" retrievals, "+oobExceptions+" out of bounds exceptions");
    }
}

// This class tests a LockFreeLL implementation
class LockFreeLLTester implements Runnable {

    // Private variables
    private LockFreeLL list;
    private ThreadLocalRandom rng;
    private int k;
    private int m;

    // Basic constructor with shared LockFreeLL reference
    public LockFreeLLTester(LockFreeLL list, int k, int m) {
        this.list = list;
        this.rng  = ThreadLocalRandom.current();
        this.k    = k;
        this.m    = m;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    // Size check and subsequent usage is not atomic but this is just a simulation so that
    // is okay, and also incidentally provides testing for out of bounds accesses.
    @Override
    public void run() {
        int bound         = 0;
        int oobExceptions = 0;
        int insertions    = 0;
        int retrievals    = 0;
        for(int i=0; i<m; i++) {
            bound = Integer.max(1, bound);
            try {
                if(rng.nextInt(100) >= k) {
                    if(rng.nextBoolean()) {
                        list.add(new Object());                // Add
                    } else {
                        list.add(rng.nextInt(bound), new Object()); // Add
                    }
                    insertions++;
                } else {
                    if(rng.nextBoolean()) {
                        list.get(rng.nextInt(bound));          // Get
                    } else {
                        list.remove(rng.nextInt(bound));       // Remove
                    }
                    retrievals++;
                }
            } catch(IndexOutOfBoundsException e) {
                oobExceptions++;
            }
        }
        System.out.println("Thread "+Thread.currentThread().threadId()+": "+insertions+" insertions, "+
            retrievals+" retrievals, "+oobExceptions+" out of bounds exceptions");
    }
}