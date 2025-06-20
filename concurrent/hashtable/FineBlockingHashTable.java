package concurrent.hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

// Thread-safe hash implementation using fine grained blocking synchronization

public class FineBlockingHashTable<K,V> {

    // Entry structure to hold key-value pairs
    private static class Entry<K,V> {
        final K key;
        V value;
        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    // Internal data
    private final float LOAD_FACTOR = 0.75f;
    private final int INITIAL_SIZE  = 16;
    private List<Entry<K,V>>[] table;
    private ReentrantLock[] locks;
    private ReentrantLock sizeLock;
    private AtomicInteger arraySize;
    private AtomicInteger numKeys;

    // Basic constructor
    @SuppressWarnings("unchecked")
    public FineBlockingHashTable() {
        arraySize  = new AtomicInteger(INITIAL_SIZE);
        numKeys    = new AtomicInteger(0);
        table      = new LinkedList[arraySize.get()];
        locks      = new ReentrantLock[arraySize.get()];
        sizeLock   = new ReentrantLock();

        for (int i=0; i<INITIAL_SIZE; i++) {
            table[i] = new LinkedList<Entry<K,V>>();
            locks[i] = new ReentrantLock();
        }
    }

    // Thread-safe insertion
    public V put(K key, V value) {

        // Both key and value must not be null
        if (key == null || value == null)
            return null;

        // Get the index of the correct bucket by hashing, and lock the corresponding lock
        V oldValue         = null;
        int localArraySize = arraySize.get();
        int index          = hash(key, localArraySize);
        ReentrantLock lock = locks[index];
        
        // Retry logic if the table has changed size while locking as our hashed index is now outdated
        lock.lock();
        while (localArraySize != arraySize.get()) {
            lock.unlock();
            localArraySize = arraySize.get();
            index          = hash(key, localArraySize);
            lock           = locks[index];
            lock.lock();
        } // The correct lock is now held for the current arraySize

        // Search bucket for a match
        List<Entry<K,V>> bucket = table[index];
        for (int i=0; i<bucket.size(); i++) {
            if (bucket.get(i).key.equals(key)) {
                oldValue = bucket.get(i).value;
                bucket.set(i, new Entry<K,V>(key, value));
                lock.unlock();
                return oldValue;
            }
        }
        
        // If not found, then add as anew entry and increment numKeys
        table[index].add(new Entry<K,V>(key, value));
        int localNumKeys = numKeys.incrementAndGet();
        lock.unlock();
        
        // Check for resize
        localArraySize = arraySize.get();
        if (localNumKeys > (localArraySize * LOAD_FACTOR)) {
            sizeLock.lock();
            // This will only succeed if nobody resized while we waited to obtain the resize lock
            if (localArraySize == arraySize.get())
                resize();
            sizeLock.unlock();
        }
        return oldValue;
    }

    // Thread-safe get
    public V get(K key) {

        // The key cannot be null
        if (key == null)
            return null;

        // Get the index of the correct bucket by hashing, and lock the corresponding lock
        int localArraySize = arraySize.get();
        int index          = hash(key, localArraySize);
        ReentrantLock lock = locks[index];
        
        // Retry logic if the table has changed size while locking as our hashed index is now outdated
        lock.lock();
        while (localArraySize != arraySize.get()) {
            lock.unlock();
            localArraySize = arraySize.get();
            index          = hash(key, localArraySize);
            lock           = locks[index];
            lock.lock();
        } // The correct lock is now held for the current arraySize

        // Search bucket for a match
        List<Entry<K,V>> bucket = table[index];
        for (int i=0; i<bucket.size(); i++) {
            if (bucket.get(i).key.equals(key)) {
                V value = bucket.get(i).value;
                lock.unlock();
                return value;
            }
        }
        
        // Return null if not found
        lock.unlock();
        return null;
    }

    // Thread-safe removal
    public V remove(K key) {

        // The key cannot be null
        if (key == null)
            return null;

        // Get the index of the correct bucket by hashing, and lock the corresponding lock
        int localArraySize = arraySize.get();
        int index          = hash(key, localArraySize);
        ReentrantLock lock = locks[index];
        
        // Retry logic if the table has changed size while locking as our hashed index is now outdated
        lock.lock();
        while (localArraySize != arraySize.get()) {
            lock.unlock();
            localArraySize = arraySize.get();
            index          = hash(key, localArraySize);
            lock           = locks[index];
            lock.lock();
        } // The correct lock is now held for the current arraySize

        // Search bucket for a match
        List<Entry<K,V>> bucket = table[index];
        for (int i=0; i<bucket.size(); i++) {
            if (bucket.get(i).key.equals(key)) {
                V value = bucket.get(i).value;
                bucket.remove(i);
                numKeys.decrementAndGet();
                lock.unlock();
                return value;
            }
        }
        
        // Return null if not found
        lock.unlock();
        return null;
    }

    // Thread-safe size retrieval
    public int size() {
        sizeLock.lock();
        int size = numKeys.get();
        sizeLock.unlock();
        return size;
    }

    // Thread-safe empty check
    public boolean isEmpty() {
        sizeLock.lock();
        boolean isEmpty = numKeys.get() == 0;
        sizeLock.unlock();
        return isEmpty;
    }

    // Private hashing function using modulo arithmetic
    private int hash(K key, int arrSize) {
        return Math.abs(key.hashCode() % arrSize);
    }

    // Private resize function when load factor is exceeded
    @SuppressWarnings("unchecked")
    private void resize() {

        // Acquire all locks to prevent data races (in order to prevent deadlock)
        for (ReentrantLock l : locks)
            l.lock();

        // Clone the old state of the hash table for future reference
        List<Entry<K,V>>[] oldTable = table.clone();
        ReentrantLock[]    oldLocks = locks;

        // Double the array size
        int localArraySize = arraySize.get() * 2;
        
        // Initialise the new buckets
        table = new LinkedList[localArraySize];
        for (int i=0; i<localArraySize; i++)
            table[i] = new LinkedList<Entry<K,V>>();
        
        // Initialise and lock the new lock array
        ReentrantLock[] newLocks = new ReentrantLock[localArraySize];
        for (int i=0; i<localArraySize; i++)
            (newLocks[i] = new ReentrantLock()).lock();
        
        // Update the global references now that we have all the new locks
        locks = newLocks;
        arraySize.set(localArraySize);

        // Copy over all the old data into the new table
        numKeys.set(0);
        for (List<Entry<K,V>> bucket : oldTable)
            for (Entry<K,V> e : bucket)
                put(e.key, e.value);
        
        // Release the old locks first, then the new locks
        for (ReentrantLock l : oldLocks)
            l.unlock();
        for (ReentrantLock l : locks)
            l.unlock();
    }
}