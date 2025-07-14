package concurrent.queue;

// Generic Queue interface for concurrent queues

public interface Queue<T> {
    void add(T item);
    T remove();
    T element();
    boolean isEmpty();
}