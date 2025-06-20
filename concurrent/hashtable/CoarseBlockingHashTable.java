package concurrent.hashtable;
import java.util.List;
import java.util.LinkedList;

// Thread-safe hash table implementation using coarse grained blocking synchronization

public class CoarseBlockingHashTable<K,V> {

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
    private int arraySize;
    private int numKeys;

    // Basic constructor
    @SuppressWarnings("unchecked")
    public CoarseBlockingHashTable() {
        arraySize = INITIAL_SIZE;
        numKeys   = 0;
        table     = new LinkedList[arraySize];

        for(int i=0; i<INITIAL_SIZE; i++)
            table[i] = new LinkedList<Entry<K,V>>();
    }

    // Thread-safe insertion
    public synchronized V put(K key, V value) {
        int index = hash(key);
        Entry<K,V> target = null;

        // Search bucket for a match
        for(Entry<K,V> e : table[index]) {
            if(e.key.equals(key)) {
                target = e;
                break;
            }
        }

        // If match found, remove it
        if(target != null) {
            table[index].remove(target);
        } else if(++numKeys > (arraySize * LOAD_FACTOR)) {
            // Increase numKeys and check for resize only if new insertion, not an update
            resize();
        }

        // Then insert the new entry
        table[index].add(new Entry<K,V>(key, value));
        return value;
    }

    // Thread-safe get
    public synchronized V get(K key) {
        int index = hash(key);
        for(Entry<K,V> e : table[index]) {
            if(e.key.equals(key))
                return e.value;
        }
        return null;
    }

    // Thread-safe removal
    public synchronized V remove(K key) {
        Entry<K,V> target = null;
        int index         = hash(key);

        // Search bucket for a match
        for(Entry<K,V> e : table[index]) {
            if(e.key.equals(key)) {
                target = e;
                break;
            }
        }

        // If a match was found, remove it and return the value
        if(target != null) {
            table[index].remove(target);
            numKeys--;
            return target.value;
        }
        return null;
    }

    // Thread-safe size retrieval
    public synchronized int size() {
        return numKeys;
    }

    // Thread-safe empty check
    public synchronized boolean isEmpty() {
        return numKeys == 0;
    }

    // Private hashing function using modulo arithmetic
    private int hash(K key) {
        return Math.abs(key.hashCode() % arraySize);
    }

    // Private resize function when load factor is exceeded
    @SuppressWarnings("unchecked")
    private void resize() {
        arraySize *= 2;
        numKeys    = 0;
        List<Entry<K,V>>[] oldTable = table.clone();
        table = new LinkedList[arraySize];
        for(int i=0; i<arraySize; i++)
            table[i] = new LinkedList<Entry<K,V>>();

        // Copy over all the old data into the new table
        for(List<Entry<K,V>> bucket : oldTable)
            for(Entry<K,V> e : bucket)
                put(e.key, e.value);
    }
}