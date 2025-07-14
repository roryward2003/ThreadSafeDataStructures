package concurrent.node;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

// Node helper class for building lock free linked list implementations

public class LockFreeNode<T> {

    // Internal data
    private T item;
    private AtomicReference<LockFreeNode<T>> next;
    private AtomicReference<LockFreeNode<T>> prev;
    private AtomicBoolean marked;

    // Basic constructor for Singly Linked List
    public LockFreeNode(T item, LockFreeNode<T> next) {
        this.item = item;
        this.next = new AtomicReference<LockFreeNode<T>>(next);
        this.marked = new AtomicBoolean(false);
    }

    // Basic constructor for Doubly Linked List
    public LockFreeNode(T item, LockFreeNode<T> next, LockFreeNode<T> prev) {
        this.item = item;
        this.next = new AtomicReference<LockFreeNode<T>>(next);
        this.prev = new AtomicReference<LockFreeNode<T>>(prev);
        this.marked = new AtomicBoolean(false);
    }

    // Get T item
    public T get() { return item; }

    // Get and Set next Node
    public LockFreeNode<T> getNext() { return next.get(); }
    public void setNext(LockFreeNode<T> newNode) { this.next.set(newNode); }
    
    // Compare and set next Node
    public boolean compareAndSetNext(LockFreeNode<T> expected, LockFreeNode<T> newNode) {
        return this.next.compareAndSet(expected, newNode);
    }

    // Get and Set prev Node
    public LockFreeNode<T> getPrev() { return prev.get(); }
    public void setPrev(LockFreeNode<T> newNode) { this.prev.set(newNode); }
    
    // Compare and set prev Node
    public boolean compareAndSetPrev(LockFreeNode<T> expected, LockFreeNode<T> newNode) {
        return this.prev.compareAndSet(expected, newNode);
    }

    // CAS mark
    public boolean attemptMark(boolean expectedMark, boolean mark) {
        return this.marked.compareAndSet(expectedMark, mark);
    }

    // Expose marked boolean
    public boolean isMarked() {
        return this.marked.get();
    }
}
