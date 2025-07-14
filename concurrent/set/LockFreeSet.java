package concurrent.set;
import concurrent.node.LockFreeKeyNode;
import java.util.concurrent.atomic.AtomicInteger;

// Thread-safe set implementation using lock free synchronization

public class LockFreeSet<T> implements Set<T> {

    // Custom window class for traversals
    class Window {

        // Internal node refs
        private final LockFreeKeyNode<T> prev, curr;

        // Basic constructor
        public Window(LockFreeKeyNode<T> prev, LockFreeKeyNode<T> curr) {
            this.prev = prev;
            this.curr = curr;
        }

        // Getters
        public LockFreeKeyNode<T> getPrev() { return prev; }
        public LockFreeKeyNode<T> getCurr() { return curr; }
    }

    // Internal data
    private final LockFreeKeyNode<T>  head, tail;
    private final AtomicInteger size;

    // Basic Constructor
    public LockFreeSet() {
        this.tail = new LockFreeKeyNode<T>(null, null, Integer.MAX_VALUE);
        this.head = new LockFreeKeyNode<T>(null, tail, Integer.MIN_VALUE);
        this.size = new AtomicInteger(0);
    }

    // Insertion iff this object is not already in the set
    @Override
    public boolean add(T item) {
        int key = item.hashCode();
        while (true) {
            // Search for the object's place in the set 
            Window window           = find(head, key);
            LockFreeKeyNode<T> prev = window.getPrev();
            LockFreeKeyNode<T> curr = window.getCurr();

            // If object alread contained in the set, return false
            while (curr != tail && curr.getKey() == key) {
                if ((item == null && curr.get() == null) || (item != null && curr.get() != null && curr.get().equals(item))) {
                    return false;
                }
                prev = curr;
                curr = curr.getNextReference();
            }

            // Try to physically insert the new node, retrying entirely on CAS failure
            LockFreeKeyNode<T> node = new LockFreeKeyNode<T>(item, curr);
            if (prev.compareAndSetNext(curr, node, false, false)) {
                size.incrementAndGet();
                return true;
            }
        }
    }

    // Thread-safe removal iff the object is already in the set. This guarantees
    // logical removal if o is present, but does not guarantee physical removal
    @Override
    public boolean remove(T item) {
        int key = item.hashCode();
        retry: while (true) {
            // Search for the object's place in the set 
            Window window           = find(head, key);
            LockFreeKeyNode<T> prev = window.getPrev();
            LockFreeKeyNode<T> curr = window.getCurr();

            // Traverse through key matches, searching for an object match
            while (curr != tail && curr.getKey() == key) {
                if ((item == null && curr.get() == null) || (item != null && curr.get() != null && curr.get().equals(item))) {
                    LockFreeKeyNode<T> next = curr.getNextReference();
                    if(!curr.compareAndSetNext(next, next, false, true))
                        continue retry;                // Retry on failure of logical removal
                    
                    // Attempt physical removal. This will be cleaned up at some point by another
                    // thread even if it fails now so there is no need to retry.
                    prev.compareAndSetNext(curr, next, false, false);
                    size.decrementAndGet();
                    return true;
                }
                curr = curr.getNextReference();
            }
            return false;
        }
    }

    // Lazy search need not remove marked nodes during traversal
    @Override
    public boolean contains(T item) {
        int key = item.hashCode();
        LockFreeKeyNode<T> curr = head;

        // Traverse until key match found
        while (curr != tail && curr.getKey() < key)
            curr = curr.getNextReference();

        // Traverse through key matches for an object match
        while (curr != tail && curr.getKey() == key) {
            if (item == null) {
                if (curr.get() == null && !curr.isMarked())
                    return true;
            } else {
                if (curr.get().equals(item) && !curr.isMarked())
                    return true;
            }
            curr = curr.getNextReference();
        }
        return false;
    }

    // Traverse all nodes searching for an unmakred, non-sentinel node
    @Override
    public boolean isEmpty() {
        LockFreeKeyNode<T> curr = head.getNextReference();
        boolean markHolder[] = {false};
        while (curr != tail)                               // Traverse to the tail sentinel node
            curr = curr.getNext(markHolder);
            if (!markHolder[0])
                return false;                              // Unmarked, non-sentinel node has been found
        return true;
    }
    
    // Approximate size retrieval. Used for simulation, not under contention.
    @Override
    public int size() {
        return size.get();
    }

    // This helper method searches for a particular key in the sorted list and
    // returns a window where prev has the largset key less than the search key
    // and next has the least key that is greater than or equal to the search key
    private Window find(LockFreeKeyNode<T> head, int key) {
        LockFreeKeyNode<T> prev = null;
        LockFreeKeyNode<T> curr = null;
        LockFreeKeyNode<T> next = null;
        boolean[] markHolder = {false};

        // Wrapped in a retry loop to ensure CAS failure tolerance
        retry: while (true) {
            prev = head;
            curr = prev.getNextReference();

            // Traverse until next's key is greater than or equal to search key
            while (true) {
                next = curr.getNext(markHolder);

                // Physically remove any marked nodes encountered
                while (markHolder[0]) {
                    if (!prev.compareAndSetNext(curr, next, false, false))
                        // Retry the whole loop if physical removal fails
                        continue retry;
                    curr = next;
                    next = curr.getNext(markHolder);
                }

                // Return condition is checked after mark to ensure invalid Nodes are not considered
                if (curr.getKey() >= key)
                    return new Window(prev, curr);

                // Slide the search window along
                prev = curr;
                curr = next;
            }
        }
    }
}