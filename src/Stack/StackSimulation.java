import java.util.concurrent.ThreadLocalRandom;

// Driver class for testing my stack implementations

public class StackSimulation {

    public static void main(String[] args) {

        // Local vars for timing and input params
        long timeBefore, timeAfter;
        int k = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);

        // Initialise two four-thread arrays, to test each stack implementation independently
        Thread[] tA = new Thread[4];
        Thread[] tB = new Thread[4];
        BlockingStackTester    a = new BlockingStackTester(new BlockingStack(), k, m);
        NonBlockingStackTester b = new NonBlockingStackTester(new NonBlockingStack(), k, m);
        for(int i=0; i<4; i++) {
            tA[i] = new Thread(a);
            tB[i] = new Thread(b);
        }

        // Time the execution of all threads in tA
        timeBefore = System.currentTimeMillis();
        for(Thread t : tA)
            t.run();
        timeAfter = System.currentTimeMillis();
        System.out.println("BlockingStack execution time: "+(timeAfter-timeBefore)+"ms");
        
        // Time the execution of all threads in tB
        timeBefore = System.currentTimeMillis();
        for(Thread t : tB)
            t.run();
        timeAfter = System.currentTimeMillis();
        System.out.println("NonBlockingStack execution time: "+(timeAfter-timeBefore)+"ms");
    }   
}

// This class tests a BlockingStack implementation
class BlockingStackTester implements Runnable {
    
    // Private variables
    private BlockingStack stack;
    private ThreadLocalRandom rng;
    private int k;
    private int m;

    // Basic constructor with shared BlockingStack reference
    public BlockingStackTester(BlockingStack stack, int k, int m) {
        this.stack = stack;
        this.rng   = ThreadLocalRandom.current();
        this.k     = k;
        this.m     = m;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        for(int i=0; i<m; i++) {
            if(rng.nextInt(100) >= k)
                synchronized(this) { stack.push(new Object()); } // Push
            else
                synchronized(this) { stack.pop(); }              // Pop
        }
    }
}

// This class tests a NonBlockingStack resizable array implementation
class NonBlockingStackTester implements Runnable {

    // Private variables
    private NonBlockingStack stack;
    private ThreadLocalRandom rng;
    private int k;
    private int m;

    // Basic constructor with shared NonBlockingStack reference
    public NonBlockingStackTester(NonBlockingStack stack, int k, int m) {
        this.stack = stack;
        this.rng   = ThreadLocalRandom.current();
        this.k     = k;
        this.m     = m;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        for(int i=0; i<m; i++) {
            if(rng.nextInt(100) >= k)
                synchronized(this) { stack.push(new Object()); } // Push
            else
                synchronized(this) { stack.pop(); }              // Pop
        }
    }
}