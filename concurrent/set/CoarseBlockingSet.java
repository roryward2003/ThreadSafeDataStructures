package concurrent.set;
import concurrent.node.Node;

// Thread-safe set implementation using coarse grained blocking synchronization

public class CoarseBlockingSet {

    // Internal data
    private Node head;

    // Basic constructor
    public CoarseBlockingSet() {
        head = null;
    }

    // Thread-safe add
    public synchronized boolean add(Object o) {
        if (contains(o))
            return false;
        head = new Node(o, head);                // Prepend o
        return true;
    }

    // Thread-safe remove
    public synchronized boolean remove(Object o) {
        Node curr = head;
        Node prev = null;
        if(o == null) {                          // If o is null, use "=="
            while (curr != null) {
                if (curr.get() == null) {
                    if (prev == null)
                        head = curr.getNext();   // Head removal
                    else
                        prev.setNext(curr.getNext());
                    return true;
                }
                prev = curr;
                curr = curr.getNext();
            }
        } else {                                 // If o is not null, use ".equals()"
            while (curr != null) {
                if (curr.get().equals(o)) {
                    if (prev == null)
                        head = curr.getNext();   // Head removal
                    else
                        prev.setNext(curr.getNext());
                    return true;
                }
                prev = curr;
                curr = curr.getNext();
            }
        }
        return false;                            // o not present in the set
    }

    // Thread-safe search
    public synchronized boolean contains(Object o) {
        Node curr = head;
        if(o == null) {                          // If o is null, use "=="
            while(curr != null) {
                if(curr.get() == null)
                    return true;
                curr = curr.getNext();
            }
        } else {                                 // If o is not null, use ".equals()"
            while(curr != null) {
                if(curr.get().equals(o))
                    return true;
                curr = curr.getNext();
            }
        }
        return false;                            // o not present in the set
    }

    // Thread-safe empty check
    public synchronized boolean isEmpty() {
        return head == null;
    }
}