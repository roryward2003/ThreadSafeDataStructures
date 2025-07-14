package concurrent.node;
import java.util.concurrent.atomic.AtomicMarkableReference;

// Node helper class for building lock free linked list implementations

public class LockFreeKeyNode<T> {

    // Internal data
    private final Object o;
    private final int key;
    private final AtomicMarkableReference<LockFreeKeyNode<T>> next;

    // Basic constructor for Singly Linked List
    public LockFreeKeyNode(Object o, LockFreeKeyNode<T> next) {
        this.o    = o;
        this.key  = o.hashCode();
        this.next = new AtomicMarkableReference<LockFreeKeyNode<T>>(next, false);
    }

    // Alternate constructor for selecting a key
    public LockFreeKeyNode(Object o, LockFreeKeyNode<T> next, int key) {
        this.o    = o;
        this.key  = key;
        this.next = new AtomicMarkableReference<LockFreeKeyNode<T>>(next, false);
    }

    // Get Object o
    public Object get() { return o; }

    // Get key
    public int getKey() { return key; }

    // Get and Set next Node
    public LockFreeKeyNode<T> getNext(boolean[] markHolder) { return next.get(markHolder); }
    public LockFreeKeyNode<T> getNextReference() { return next.getReference(); }
    public void setNext(LockFreeKeyNode<T> newNode, boolean newMark) { this.next.set(newNode, newMark); }
    
    // Compare and set next Node
    public boolean compareAndSetNext(LockFreeKeyNode<T> expected, LockFreeKeyNode<T> newNode, boolean expectedMark, boolean mark) {
        return this.next.compareAndSet(expected, newNode, expectedMark, mark);
    }
    
    // Expose marked boolean
    public boolean isMarked() { return this.next.isMarked(); }
}
