package concurrent.node;
import java.util.concurrent.atomic.AtomicMarkableReference;

// Node helper class for building lock free linked list implementations

public class LockFreeKeyNode {

    // Internal data
    private final Object o;
    private final int key;
    private final AtomicMarkableReference<LockFreeKeyNode> next;

    // Basic constructor for Singly Linked List
    public LockFreeKeyNode(Object o, LockFreeKeyNode next) {
        this.o    = o;
        this.key  = o.hashCode();
        this.next = new AtomicMarkableReference<LockFreeKeyNode>(next, false);
    }

    // Alternate constructor for selecting a key
    public LockFreeKeyNode(Object o, LockFreeKeyNode next, int key) {
        this.o    = o;
        this.key  = key;
        this.next = new AtomicMarkableReference<LockFreeKeyNode>(next, false);
    }

    // Get Object o
    public Object get() { return o; }

    // Get key
    public int getKey() { return key; }

    // Get and Set next Node
    public LockFreeKeyNode getNext(boolean[] markHolder) { return next.get(markHolder); }
    public LockFreeKeyNode getNextReference() { return next.getReference(); }
    public void setNext(LockFreeKeyNode newNode, boolean newMark) { this.next.set(newNode, newMark); }
    
    // Compare and set next Node
    public boolean compareAndSetNext(LockFreeKeyNode expected, LockFreeKeyNode newNode, boolean expectedMark, boolean mark) {
        return this.next.compareAndSet(expected, newNode, expectedMark, mark);
    }
    
    // Expose marked boolean
    public boolean isMarked() { return this.next.isMarked(); }
}
