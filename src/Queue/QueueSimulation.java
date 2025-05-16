import java.util.concurrent.ThreadLocalRandom;

// Driver class for testing my FIFO queue implementations

public class QueueSimulation {

    public static void main(String[] args) {

        // Local vars for timing and input params
        long timeBefore, timeAfter;
        int k = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);

        // Initialise two four-thread arrays, to test q2a and q2b respectively
        Thread[] tA = new Thread[4];
        Thread[] tB = new Thread[4];
        BlockingQueueTester    a = new BlockingQueueTester(new BlockingQueue(), k, m);
        LockFreeQueueTester b = new LockFreeQueueTester(new LockFreeQueue(), k, m);
        for(int i=0; i<4; i++) {
            tA[i] = new Thread(a);
            tB[i] = new Thread(b);
        }

        // Time the execution of all threads in tA
        timeBefore = System.currentTimeMillis();
        for(Thread t : tA)
            t.run();
        timeAfter = System.currentTimeMillis();
        System.out.println("BlockingQueue execution time: "+(timeAfter-timeBefore)+"ms");
        
        // Time the execution of all threads in tB
        timeBefore = System.currentTimeMillis();
        for(Thread t : tB)
            t.run();
        timeAfter = System.currentTimeMillis();
        System.out.println("LockFreeQueue execution time: "+(timeAfter-timeBefore)+"ms");
    }   
}

// This class tests a BlockingQueue implementation
class BlockingQueueTester implements Runnable {
    
    // Private variables
    private BlockingQueue queue;
    private ThreadLocalRandom rng;
    private int k;
    private int m;

    // Basic constructor with shared BlockingQueue reference
    public BlockingQueueTester(BlockingQueue queue, int k, int m) {
        this.queue = queue;
        this.rng   = ThreadLocalRandom.current();
        this.k     = k;
        this.m     = m;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        for(int i=0; i<m; i++) {
            if(rng.nextInt(100) >= k || queue.isEmpty()) {
                synchronized(this) { queue.add(new Object()); } // Add
            } else {
                if(rng.nextBoolean()) {
                    synchronized(this) { queue.element(); }     // Element
                } else {
                    synchronized(this) { queue.remove();  }     // Remove
                }
            }
        }
    }
}

// This class tests a LockFreeQueue implementation
class LockFreeQueueTester implements Runnable {

    // Private variables
    private LockFreeQueue queue;
    private ThreadLocalRandom rng;
    private int k;
    private int m;

    // Basic constructor with shared LockFreeQueue reference
    public LockFreeQueueTester(LockFreeQueue queue, int k, int m) {
        this.queue = queue;
        this.rng   = ThreadLocalRandom.current();
        this.k     = k;
        this.m     = m;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        for(int i=0; i<m; i++) {
            if(rng.nextInt(100) >= k || queue.isEmpty()) {
                synchronized(this) { queue.add(new Object()); } // Add
            } else {
                if(rng.nextBoolean()) {
                    synchronized(this) { queue.element(); }     // Element
                } else {
                    synchronized(this) { queue.remove();  }     // Remove
                }
            }
        }
    }
}