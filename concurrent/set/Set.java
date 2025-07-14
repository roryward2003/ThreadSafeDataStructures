package concurrent.set;

// Interface for defining concurrent set operations

public interface Set<T> {

    // Thread safe insertion iff set doesn't contain item. Returns true if successful.
    public boolean add(T item);
    
    // Thread safe removal iff set contains item. Returns true if successful.
    public boolean remove(T item);

    // Thread safe item search. Returns true if found.
    public boolean contains(T item);

    // Thread safe emptiness check.
    public boolean isEmpty();

    // Approximate size retrieval. Used for simulation, not under contention.
    public int size();
}