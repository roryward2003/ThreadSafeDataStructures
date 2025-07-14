package concurrent.deque;

// Generic thread safe deque interface

public interface Deque<T> {
    void addFirst(T item);
    void addLast(T item);
    T removeFirst();
    T removeLast();
    T getFirst();
    T getLast();
    boolean isEmpty();
    int size();
}