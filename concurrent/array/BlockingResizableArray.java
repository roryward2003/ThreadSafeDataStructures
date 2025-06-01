package concurrent.array;
import java.util.concurrent.locks.ReentrantLock;

// Resizable thread safe array implementation using blocking synchronisation,
// controlled by log(n) locks. The base for this log is the constant defined below.

// Elements in different chunks can be accessed concurrently, where chunks are
// each controlled by one lock. For example, for log base 2 the chunks would be
// [0-1] [2-3] [4-7] [8-15] [16-31]... This offers a nice trade off between
// performace and memory usage, whilst still remaining relatively simple.

public class BlockingResizableArray {

    // Private data
    private static final double LOG_BASE = 2.0;
    private Object[] arr;
    private ReentrantLock[] locks;
    private int size;

    // Basic contructor initialses an object array of length 20, and a lock
    // array of length log(20)(rounded down) + 1
    public BlockingResizableArray() {

        size = 20;
        arr = new Object[size];
        locks = new ReentrantLock[getNumLocks(size)];
        for(int i=0; i<getNumLocks(size); i++)
            locks[i] = new ReentrantLock();
    }

    // Thread safe read operation
    public Object get(int i) {
        
        // Ensure size check and potential extension appear atomic
        synchronized(this) {
            if(i == size)
                this.extend();
        }

        // Acquire the corresponding chunk's lock, then read
        locks[getLockIndex(i)].lock();
        try {
            return arr[i];
        } finally {
            locks[getLockIndex(i)].unlock();
        }
    }

    // Thread safe write operation
    public void set(int i, Object o) {

        // Ensure size check and potential extension appear atomic
        synchronized(this) {
            if(i == size)
                this.extend();
        }
        
        // Acquire the corresponding chunk's lock, then write
        locks[getLockIndex(i)].lock();
        try {
            this.arr[i] = o;
        } finally {
            locks[getLockIndex(i)].unlock();
        }
    }

    // Extend the array with 10 new null Objects, and extend the lock array if necessary
    private void extend() {

        // Acquire all locks in ascending order
        for(ReentrantLock l : locks)
            l.lock();

        try {
            this.size+=10;
            Object[] new_arr = new Object[size];

            // If necessary, extend the locks array. Locks are copied by reference
            // which prevents other threads waiting on the lock from getting lost.
            if(getLockIndex(size)+1 > locks.length) {
                ReentrantLock[] new_locks = new ReentrantLock[getNumLocks(size)];
                for(int i=0; i<locks.length; i++)                  // Copy old locks
                    new_locks[i] = locks[i];
                for(int i=locks.length; i<new_locks.length; i++) { // Pad with new locks
                    new_locks[i] = new ReentrantLock();
                }
                locks = new_locks;
            }

            // Extend the object arr
            for(int i=0; i<arr.length; i++)
                new_arr[i] = arr[i];
            this.arr = new_arr;
        } 
        
        // Release all old locks in descending order
        finally {
            for(int i=getNumLocks(size-10)-1; i>=0; i--) {
                locks[i].unlock();
            }
        }
    }

    // Returns the size of the array. This is useful to allow our simulator to
    // access outside of the array bounds, thus forcing a resize.
    public int getSize() { return size; }

    // Calculate the number of locks needed for a given number of Objects
    private static int getNumLocks(int n) {

        if(n==0) return 0;
        return (int)Math.ceil(Math.log(n)/Math.log(LOG_BASE));
    }

    // Calculate the lock index given an array index
    private static int getLockIndex(int i) {

        if(i==0) return 0;
        return (int)Math.floor(Math.log(i)/Math.log(LOG_BASE));
    }
}