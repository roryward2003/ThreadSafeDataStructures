package concurrent.node;
import java.util.concurrent.locks.ReentrantLock;

// Node helper class for building DSA implementations

public class LockableNode {

    // Internal data
    private final ReentrantLock lock;
    private LockableNode next;
    private LockableNode prev;
    private Object o;

    // Basic constructor for Doubly Linked List
    public LockableNode(Object o, LockableNode next, LockableNode prev) {
        this.lock = new ReentrantLock();
        this.next = next;
        this.prev = prev;
        this.o    = o;
    }

    // Basic constructor for Singly Linked List
    public LockableNode(Object o, LockableNode next) {
        this.lock = new ReentrantLock();
        this.next = next;
        this.o    = o;
    }

    // Get and set Object o
    public Object get() { return o; }
    public void set(Object o) { this.o = o; }

    // Get and Set next Node
    public LockableNode getNext() { return next; }
    public void setNext(LockableNode next) { this.next = next; }
    
    // Get and Set prev Node
    public LockableNode getPrev() { return prev; }
    public void setPrev(LockableNode prev) { this.prev = prev; }

    // Lock and unlock functions
    public void lock() { this.lock.lock(); }
    public void unlock() { this.lock.unlock(); }
}