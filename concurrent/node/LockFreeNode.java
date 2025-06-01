package concurrent.node;
import java.util.concurrent.atomic.AtomicMarkableReference;

// Node helper class for building lock free linked list implementations

public class LockFreeNode {

    // Internal data
    private final Object o;
    private final AtomicMarkableReference<LockFreeNode> next;

    // Basic constructor for Singly Linked List
    public LockFreeNode(Object o, LockFreeNode next) {
        this.o    = o;
        this.next = new AtomicMarkableReference<LockFreeNode>(next, false);
    }

    // Get Object o
    public Object get() { return o; }

    // Get and Set next Node
    public LockFreeNode getNext() { return next.getReference(); }
    public void setNext(LockFreeNode newNode) { this.next.set(newNode, false); }

    // Compare and set next Node
    public boolean compareAndSetNext(LockFreeNode expected, LockFreeNode newNode, boolean mark) {
        boolean expectedMark = this.next.isMarked();
        return this.next.compareAndSet(expected, newNode, expectedMark, mark);
    }

    // Expose marked boolean
    public boolean isMarked() {
        return this.next.isMarked();
    }
}
