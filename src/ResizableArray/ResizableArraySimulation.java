import java.util.concurrent.ThreadLocalRandom;

// Driver class for testing my resizable array implementations

public class ResizableArraySimulation {

    public static void main(String[] args) {

        // Local vars for timing and input params
        long timeBefore, timeAfter;
        int k = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);

        // Initialise two four-thread arrays, to test each array implementation independently
        Thread[] tA = new Thread[4];
        Thread[] tB = new Thread[4];
        BlockingResizableArrayTester    a = new BlockingResizableArrayTester(new BlockingResizableArray(), k, m);
        NonBlockingResizableArrayTester b = new NonBlockingResizableArrayTester(new NonBlockingResizableArray(), k, m);
        for(int i=0; i<4; i++) {
            tA[i] = new Thread(a);
            tB[i] = new Thread(b);
        }

        // Time the execution of all threads in tA
        timeBefore = System.currentTimeMillis();
        for(Thread t : tA)
            t.run();
        timeAfter = System.currentTimeMillis();
        System.out.println("BlockingResizableArray execution time: "+(timeAfter-timeBefore)+"ms");
        
        // Time the execution of all threads in tB
        timeBefore = System.currentTimeMillis();
        for(Thread t : tB)
            t.run();
        timeAfter = System.currentTimeMillis();
        System.out.println("NonBlockingResizableArray execution time: "+(timeAfter-timeBefore)+"ms");
    }   
}

// This class tests a BlockingResizableArray implementation
class BlockingResizableArrayTester implements Runnable {
    
    // Private variables
    private BlockingResizableArray arr;
    private ThreadLocalRandom rng;
    private int k;
    private int m;

    // Basic constructor with shared BlockingResizableArray reference
    public BlockingResizableArrayTester(BlockingResizableArray arr, int k, int m) {
        this.arr = arr;
        this.rng = ThreadLocalRandom.current();
        this.k = k;
        this.m = m;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        for(int i=0; i<m; i++) {
            if(rng.nextInt(100) >= k) {      // Access any normal part of the array
                if(rng.nextInt(2) == 0)
                    synchronized(this) { arr.set(rng.nextInt(arr.getSize()), new Object()); }
                else
                    synchronized(this) { arr.get(rng.nextInt(arr.getSize())); }
            } else {                               // Access one past the end of the array
                if(rng.nextInt(2) == 0)
                    synchronized(this) { arr.set(arr.getSize(), new Object()); }
                else
                    synchronized(this) { arr.get(arr.getSize()); }
            }
        }
    }
}

// This class tests a NonBlockingResizableArray implementation
class NonBlockingResizableArrayTester implements Runnable {

    // Private variables
    private NonBlockingResizableArray arr;
    private ThreadLocalRandom rng;
    private int k;
    private int m;

    // Basic constructor with shared NonBlockingResizableArray reference
    public NonBlockingResizableArrayTester(NonBlockingResizableArray arr, int k, int m) {
        this.arr = arr;
        this.rng = ThreadLocalRandom.current();
        this.k = k;
        this.m = m;
    }

    // Threads constructed using this runnable implementation will simulate usage as below
    @Override
    public void run() {
        for(int i=0; i<m; i++) {
            if(rng.nextInt(100) >= k) {      // Access any normal part of the array
                if(rng.nextInt(2) == 0)
                    arr.set(rng.nextInt(arr.getSize()), new Object());
                else
                    arr.get(rng.nextInt(arr.getSize()));
            } else {                               // Access one past the end of the array
                if(rng.nextInt(2) == 0)
                    arr.set(arr.getSize(), new Object());
                else
                    arr.get(arr.getSize());
            }
        }
    }
}