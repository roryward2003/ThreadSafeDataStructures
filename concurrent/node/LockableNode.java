package concurrent.node;
import java.util.concurrent.locks.ReentrantLock;

// Node helper class for building DSA implementations

public class LockableNode<T> {

    // Internal data
    private final ReentrantLock lock;
    private LockableNode<T> next;
    private LockableNode<T> prev;
    private T item;

    // Basic constructor for Doubly Linked List
    public LockableNode(T item, LockableNode<T> next, LockableNode<T> prev) {
        this.lock = new ReentrantLock();
        this.next = next;
        this.prev = prev;
        this.item = item;
    }

    // Basic constructor for Singly Linked List
    public LockableNode(T item, LockableNode<T> next) {
        this.lock = new ReentrantLock();
        this.next = next;
        this.item = item;
    }

    // Get and set T item
    public Object get() { return item; }
    public void set(T item) { this.item = item; }

    // Get and Set next Node
    public LockableNode<T> getNext() { return next; }
    public void setNext(LockableNode<T> next) { this.next = next; }
    
    // Get and Set prev Node
    public LockableNode<T> getPrev() { return prev; }
    public void setPrev(LockableNode<T> prev) { this.prev = prev; }

    // Lock and unlock functions
    public void lock() { this.lock.lock(); }
    public void unlock() { this.lock.unlock(); }
}