package concurrent.set;
import concurrent.node.LockableNode;

// Thread-safe set implementation using fine grained blocking synchronization

public class FineBlockingSet {

    // Internal data
    private LockableNode head;

    // Basic constructor
    public FineBlockingSet() {
        this.head = new LockableNode(null, null);
    }

    // Thread safe insertion iff set doesn't contain o
    public boolean add(Object o) {
        LockableNode localHead = head;
        localHead.lock();
        while (localHead != head) {
            localHead.unlock();
            localHead = head;
            localHead.lock();
        } // Head is now locked and up to date

        if (contains(o))
            return false;
        
        head.setNext(new LockableNode(o, localHead.getNext()));
        localHead.unlock();
        return true;
    }

    // Thread safe removal iff set contains o via hand over hand locking traversal
    public boolean remove(Object o) {
        LockableNode prev = head;
        prev.lock();
        LockableNode curr = prev.getNext();
        while (curr != null) {
            curr.lock();
            if (o == null ? curr.get() == null : o.equals(curr.get())) {
                prev.setNext(curr.getNext());
                curr.unlock();
                prev.unlock();
                return true;
            }
            prev.unlock();
            prev = curr;
            curr = curr.getNext();
        }
        prev.unlock();
        return false;                            // Return false if not found for removal
    }

    // Thread safe search via hand over hand locking traversal
    public boolean contains(Object o) {
        LockableNode prev = head;
        prev.lock();
        LockableNode curr = prev.getNext();
        while (curr != null) {
            curr.lock();
            if (o == null ? curr.get() == null : o.equals(curr.get())) {
                curr.unlock();
                prev.unlock();
                return true;
            }
            prev.unlock();
            prev = curr;
            curr = curr.getNext();
        }
        prev.unlock();
        return false;                            // Return false if not found
    }
    
    // Thread safe emptiness check
    public synchronized boolean isEmpty() {
        return head.getNext() == null;
    }
}