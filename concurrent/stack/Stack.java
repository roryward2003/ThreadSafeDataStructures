package concurrent.stack;

// Generic stack interface
public interface Stack<T> {
    T pop();
    void push(T item);
}