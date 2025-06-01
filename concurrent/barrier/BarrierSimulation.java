package src.barrier;

// Simulation for the Blocking and Lock Free barrier implementations

public class BarrierSimulation {

    public static void main(String[] args) {

        // Local vars for timing and input params
        long timeBefore, timeAfter;
        int numThreads = Integer.parseInt(args[0]);
        int numOps = Integer.parseInt(args[1]);

        // Initialise two Thread arrays to test each barrier implementation
        Thread[] tA = new Thread[numThreads];
        Thread[] tB = new Thread[numThreads];
        BlockingBarrierTester a = new BlockingBarrierTester(new BlockingBarrier(numThreads), numOps);
        LockFreeBarrierTester b = new LockFreeBarrierTester(new LockFreeBarrier(numThreads), numOps);
        for(int i=0; i<numThreads; i++) {
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
        System.out.println("BlockingBarrier execution time: "+(timeAfter-timeBefore)+"ms");
        
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
        System.out.println("LockFreeBarrier execution time: "+(timeAfter-timeBefore)+"ms");
    }   
}

// This class tests a BlockingBarrier implementation
class BlockingBarrierTester implements Runnable {
    
    // Private variables
    private BlockingBarrier barrier;
    private int numOps;

    // Basic constructor with shared BlockingBarrier reference
    public BlockingBarrierTester(BlockingBarrier barrier, int numOps) {
        this.barrier = barrier;
        this.numOps  = numOps;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        for(int i=0; i<numOps; i++) {
            barrier.arrive();
        }
    }
}

// This class tests a BlockingBarrier implementation
class LockFreeBarrierTester implements Runnable {
    
    // Private variables
    private LockFreeBarrier barrier;
    private int numOps;

    // Basic constructor with shared BlockingBarrier reference
    public LockFreeBarrierTester(LockFreeBarrier barrier, int numOps) {
        this.barrier = barrier;
        this.numOps  = numOps;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        for(int i=0; i<numOps; i++) {
            barrier.arrive();
        }
    }
}