package concurrent.set;
import concurrent.node.Node;

// Thread-safe set implementation using coarse grained blocking synchronization

public class CoarseBlockingSet<T> implements Set<T>{

    // Internal data
    private Node<T> head;
    private int size;

    // Basic constructor
    public CoarseBlockingSet() {
        head = null;
        size = 0;
    }

    // Thread-safe add
    @Override
    public synchronized boolean add(T item) {
        if (contains(item))
            return false;
        head = new Node<T>(item, head);                // Prepend o
        size++;
        return true;
    }

    // Thread-safe remove
    @Override
    public synchronized boolean remove(T item) {
        Node<T> curr = head;
        Node<T> prev = null;
        if(item == null) {                          // If o is null, use "=="
            while (curr != null) {
                if (curr.get() == null) {
                    if (prev == null)
                        head = curr.getNext();   // Head removal
                    else
                        prev.setNext(curr.getNext());
                    size--;
                    return true;
                }
                prev = curr;
                curr = curr.getNext();
            }
        } else {                                 // If o is not null, use ".equals()"
            while (curr != null) {
                if (curr.get().equals(item)) {
                    if (prev == null)
                        head = curr.getNext();   // Head removal
                    else
                        prev.setNext(curr.getNext());
                    size--;
                    return true;
                }
                prev = curr;
                curr = curr.getNext();
            }
        }
        return false;                            // o not present in the set
    }

    // Thread-safe search
    @Override
    public synchronized boolean contains(T item) {
        Node<T> curr = head;
        if(item == null) {                          // If o is null, use "=="
            while(curr != null) {
                if(curr.get() == null)
                    return true;
                curr = curr.getNext();
            }
        } else {                                 // If o is not null, use ".equals()"
            while(curr != null) {
                if(curr.get().equals(item))
                    return true;
                curr = curr.getNext();
            }
        }
        return false;                            // o not present in the set
    }

    // Thread-safe empty check
    @Override
    public synchronized boolean isEmpty() {
        return head == null;
    }

    // Thread-safe size check
    @Override
    public synchronized int size() {
        return size;
    }
}