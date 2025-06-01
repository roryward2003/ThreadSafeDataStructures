package src.barrier;

// Reusable n-thread barrier using blocking synchronization

public class BlockingBarrier {
    
    // Internal data
    private final int numThreads;
    private int count;
    private boolean phase;  // Differentiates between iterations

    // Basic constructor
    public BlockingBarrier(int numThreads) {
        this.numThreads = numThreads;
        this.count      = numThreads;
        this.phase      = true;
    }

    // Arrive at the barrier and wait to be released
    public synchronized void arrive() {
        if(--count == 0) {  // If last to arrive
            phase = !phase;
            count = numThreads;
            notifyAll();    // Release any waiting threads
        } else {
            boolean localPhase = phase;
            while(localPhase == phase) {
                try {
                    wait(); // Wait until released
                } catch (Exception e) {
                    throw new RuntimeException("Thread interrupted while waiting at barrier", e);
                }
            }
        }
    }
}