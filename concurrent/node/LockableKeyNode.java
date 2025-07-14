package concurrent.node;
import java.util.concurrent.locks.ReentrantLock;

// Node helper class for building DSA implementations

public class LockableKeyNode<T> {

    // Internal data
    private final ReentrantLock lock;
    private LockableKeyNode<T> next;
    private Object o;
    private int key;

    // Basic constructor for Singly Linked List with designated key
    public LockableKeyNode(Object o, LockableKeyNode<T> next, int key) {
        this.lock = new ReentrantLock();
        this.next = next;
        this.o    = o;
        this.key  = key;
    }

    // Get key
    public int getKey() { return this.key; }

    // Get and set Object o
    public Object get() { return o; }
    public void set(Object o) { this.o = o; }

    // Get and Set next Node
    public LockableKeyNode<T> getNext() { return next; }
    public void setNext(LockableKeyNode<T> next) { this.next = next; }

    // Lock and unlock functions
    public void lock() { this.lock.lock(); }
    public void unlock() { this.lock.unlock(); }
}