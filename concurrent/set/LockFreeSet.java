package concurrent.set;
import concurrent.node.LockFreeKeyNode;

// Thread-safe set implementation using lock free synchronization

public class LockFreeSet {

    // Custom window class for traversals
    class Window {

        // Internal node refs
        private final LockFreeKeyNode prev, curr;

        // Basic constructor
        public Window(LockFreeKeyNode prev, LockFreeKeyNode curr) {
            this.prev = prev;
            this.curr = curr;
        }

        // Getters
        public LockFreeKeyNode getPrev() { return prev; }
        public LockFreeKeyNode getCurr() { return curr; }
    }

    // Internal data
    final LockFreeKeyNode head, tail;

    // Basic Constructor
    public LockFreeSet() {
        this.tail = new LockFreeKeyNode(null, null, Integer.MAX_VALUE);
        this.head = new LockFreeKeyNode(null, tail, Integer.MIN_VALUE);
    }

    // Insertion iff this object is not already in the set
    public boolean add(Object o) {
        int key = o.hashCode();
        while (true) {
            // Search for the object's place in the set 
            Window window        = find(head, key);
            LockFreeKeyNode prev = window.getPrev();
            LockFreeKeyNode curr = window.getCurr();

            // If object alread contained in the set, return false
            while (curr != tail && curr.getKey() == key) {
                if ((o == null && curr.get() == null) || (o != null && curr.get() != null && curr.get().equals(o))) {
                    return false;
                }
                prev = curr;
                curr = curr.getNextReference();
            }

            // Try to physically insert the new node, retrying entirely on CAS failure
            LockFreeKeyNode node = new LockFreeKeyNode(o, curr);
            if (prev.compareAndSetNext(curr, node, false, false))
                return true;
        }
    }

    // Thread-safe removal iff the object is already in the set. This guarantees
    // logical removal if o is present, but does not guarantee physical removal
    public boolean remove(Object o) {
        int key = o.hashCode();
        retry: while (true) {
            // Search for the object's place in the set 
            Window window        = find(head, key);
            LockFreeKeyNode prev = window.getPrev();
            LockFreeKeyNode curr = window.getCurr();

            // Traverse through key matches, searching for an object match
            while (curr != tail && curr.getKey() == key) {
                if ((o == null && curr.get() == null) || (o != null && curr.get() != null && curr.get().equals(o))) {
                    LockFreeKeyNode next = curr.getNextReference();
                    if(!curr.compareAndSetNext(next, next, false, true))
                        continue retry;                // Retry on failure of logical removal
                    
                    // Attempt physical removal. This will be cleaned up at some point by another
                    // thread even if it fails now so there is no need to retry.
                    prev.compareAndSetNext(curr, next, false, false);
                    return true;
                }
                curr = curr.getNextReference();
            }
            return false;
        }
    }

    // Lazy search need not remove marked nodes during traversal
    public boolean contains(Object o) {
        int key = o.hashCode();
        LockFreeKeyNode curr = head;

        // Traverse until key match found
        while (curr != tail && curr.getKey() < key)
            curr = curr.getNextReference();

        // Traverse through key matches for an object match
        while (curr != tail && curr.getKey() == key) {
            if (o == null) {
                if (curr.get() == null && !curr.isMarked())
                    return true;
            } else {
                if (curr.get().equals(o) && !curr.isMarked())
                    return true;
            }
            curr = curr.getNextReference();
        }
        return false;
    }

    // Traverse all nodes searching for an unmakred, non-sentinel node
    public boolean isEmpty() {
        LockFreeKeyNode curr = head.getNextReference();
        boolean markHolder[] = {false};
        while (curr != tail)                               // Traverse to the tail sentinel node
            curr = curr.getNext(markHolder);
            if (!markHolder[0])
                return false;                              // Unmarked, non-sentinel node has been found
        return true;
    }

    // This helper method searches for a particular key in the sorted list and
    // returns a window where prev has the largset key less than the search key
    // and next has the least key that is greater than or equal to the search key
    private Window find(LockFreeKeyNode head, int key) {
        LockFreeKeyNode prev = null;
        LockFreeKeyNode curr = null;
        LockFreeKeyNode next = null;
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