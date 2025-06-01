package concurrent.barrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

// Reusable n-thread barrier using lock-free synchronization

public class LockFreeBarrier {
    
    // Internal data
    private final int numThreads;
    private final AtomicInteger count;
    private final AtomicBoolean phase;  // Differentiates between iterations
    
    // Basic constructor
    public LockFreeBarrier(int numThreads) {
        this.numThreads = numThreads;
        this.count = new AtomicInteger(numThreads);
        this.phase = new AtomicBoolean(true);
    }

    // Arrive at the barrier
    public void arrive() {
        // Create a local copy of the initial phase
        boolean myPhase = phase.get();
        
        // If last to arrive, release all and reset the barrier
        if(count.decrementAndGet() == 0) {
            count.set(numThreads);
            phase.set(!myPhase);
        } else { // Otherwise wait to be released
            while (phase.get() == myPhase)
                Thread.yield(); // Yield to other threads
        }
    }
}