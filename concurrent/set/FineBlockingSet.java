// concurrent/set/FineBlockingSet.java
package concurrent.set;
import concurrent.node.LockableKeyNode;
import java.util.concurrent.atomic.AtomicInteger;

// Thread-safe set implementation using fine grained blocking synchronization

public class FineBlockingSet<T> implements Set<T> {

    // Internal data
    private final LockableKeyNode<T> head, tail;
    private final AtomicInteger size;

    // Basic constructor with sentinel head and tail nodes
    public FineBlockingSet() {
        this.tail = new LockableKeyNode<T>(null, null, Integer.MAX_VALUE);
        this.head = new LockableKeyNode<T>(null, tail, Integer.MIN_VALUE);
        this.size = new AtomicInteger(0);
    }

    // Helper class for traversal via hand over hand locking
    private class Window {
        final LockableKeyNode<T> prev, curr;

        // Basic constructor
        Window(LockableKeyNode<T> prev, LockableKeyNode<T> curr) {
            this.prev = prev;
            this.curr = curr;
        }
    }

    // Helper method to traverse in search of a given key using hand over hand locking
    private Window find(int key) {
        LockableKeyNode<T> prev = head;
        prev.lock();
        LockableKeyNode<T> curr = prev.getNext();
        curr.lock();

        // Traverse until curr is the tail node, or the first node with key >= search key
        while (curr != tail && curr.getKey() < key) {
            prev.unlock();
            prev = curr;
            curr = curr.getNext();
            curr.lock();
        }
        return new Window(prev, curr);
    }

    // Thread safe insertion iff set doesn't contain item
    @Override
    public boolean add(T item) {
        int key  = (item == null) ? 0 : item.hashCode();
        Window w = find(key);
        LockableKeyNode<T> prev = w.prev;
        LockableKeyNode<T> curr = w.curr;

        // Iterate over key matches, searching for object match
        while (curr != tail && curr.getKey() == key) {
            if ((item == null && curr.get() == null) || (item != null && item.equals(curr.get()))) {

                // Element already in the set
                prev.unlock();
                curr.unlock();
                return false;
            }
            prev.unlock();
            prev = curr;
            curr = curr.getNext();
            curr.lock();
        }

        // Insert the new element into the set
        LockableKeyNode<T> newNode = new LockableKeyNode<T>(item, curr, key);
        prev.setNext(newNode);
        prev.unlock();
        curr.unlock();
        size.incrementAndGet();
        return true;
    }

    // Thread safe removal iff set contains item
    @Override
    public boolean remove(T item) {
        int key  = (item == null) ? 0 : item.hashCode();
        Window w = find(key);
        LockableKeyNode<T> prev = w.prev;
        LockableKeyNode<T> curr = w.curr;

        // Iterate over key matches, searching for object match
        while (curr != tail && curr.getKey() == key) {
            if ((item == null && curr.get() == null) || (item != null && item.equals(curr.get()))) {

                // Remove element from the set
                prev.setNext(curr.getNext());
                prev.unlock();
                curr.unlock();
                size.decrementAndGet();
                return true;
            }
            prev.unlock();
            prev = curr;
            curr = curr.getNext();
            curr.lock();
        }

        // Element was not found in the set
        prev.unlock();
        curr.unlock();
        return false;
}

    // Thread safe search
    @Override
    public boolean contains(T item) {
        int key  = (item == null) ? 0 : item.hashCode();
        Window w = find(key);
        LockableKeyNode<T> prev = w.prev;
        LockableKeyNode<T> curr = w.curr;
        
        // Iterate over key matches, searching for object match
        while (curr != tail && curr.getKey() == key) {
            if ((item == null && curr.get() == null) || (item != null && item.equals(curr.get()))) {

                // Element found
                prev.unlock();
                curr.unlock();
                return true;
            }
            prev.unlock();
            prev = curr;
            curr = curr.getNext();
            curr.lock();
        }

        // Element was not found in the set
        prev.unlock();
        curr.unlock();
        return false;
    }

    // Thread safe emptiness check
    @Override
    public synchronized boolean isEmpty() {
        return head.getNext() == tail;
    }

    // Approximate size retrieval. Used for simulation, not under contention.
    @Override
    public synchronized int size() {
        return size.get();
    }
}