import java.util.concurrent.ThreadLocalRandom;

// Driver class for testing my FIFO deque implementations

public class DequeSimulation {

    public static void main(String[] args) {

        // Local vars for timing and input params
        long timeBefore, timeAfter;
        int k = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);

        // Initialise two four-thread arrays, to test q2a and q2b respectively
        Thread[] tA = new Thread[4];
        Thread[] tB = new Thread[4];
        BlockingDequeTester    a = new BlockingDequeTester(new BlockingDeque(), k, m);
        LockFreeDequeTester b = new LockFreeDequeTester(new LockFreeDeque(), k, m);
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
        System.out.println("BlockingDeque execution time: "+(timeAfter-timeBefore)+"ms");
        
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
        System.out.println("LockFreeDeque execution time: "+(timeAfter-timeBefore)+"ms");
    }   
}

// This class tests a BlockingDeque implementation
class BlockingDequeTester implements Runnable {
    
    // Private variables
    private BlockingDeque deque;
    private ThreadLocalRandom rng;
    private int k;
    private int m;

    // Basic constructor with shared BlockingDeque reference
    public BlockingDequeTester(BlockingDeque deque, int k, int m) {
        this.deque = deque;
        this.rng   = ThreadLocalRandom.current();
        this.k     = k;
        this.m     = m;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        for(int i=0; i<m; i++) {
            if(rng.nextInt(100) >= k  || deque.isEmpty()) {
                if(rng.nextBoolean()) {
                    deque.addFirst(new Object());
                } else {
                    deque.addLast(new Object());
                }
            } else {
                if(rng.nextBoolean()) {
                    if(rng.nextBoolean()) {
                        deque.getFirst();
                    } else {
                        deque.getLast();
                    }
                } else {
                    if(rng.nextBoolean()) {
                        deque.removeFirst();
                    } else {
                        deque.removeLast();
                    }
                }
            }
        }
    }
}

// This class tests a LockFreeDeque implementation
class LockFreeDequeTester implements Runnable {

    // Private variables
    private LockFreeDeque deque;
    private ThreadLocalRandom rng;
    private int k;
    private int m;

    // Basic constructor with shared LockFreeDeque reference
    public LockFreeDequeTester(LockFreeDeque deque, int k, int m) {
        this.deque = deque;
        this.rng   = ThreadLocalRandom.current();
        this.k     = k;
        this.m     = m;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        for(int i=0; i<m; i++) {
            if(rng.nextInt(100) >= k) {
                if(rng.nextBoolean()) {
                    synchronized(this) { deque.addFirst(new Object()); }
                } else {
                    synchronized(this) { deque.addLast(new Object()); }
                }
            } else {
                if(rng.nextBoolean()) {
                    if(rng.nextBoolean()) {
                        synchronized(this) { deque.getFirst(); }
                    } else {
                        synchronized(this) { deque.getLast(); }
                    }
                } else {
                    if(rng.nextBoolean()) {
                        synchronized(this) { deque.removeFirst(); }
                    } else {
                        synchronized(this) { deque.removeLast(); }
                    }
                }
            }
        }
    }
}