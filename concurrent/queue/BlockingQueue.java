package concurrent.queue;
import concurrent.node.Node;

// Thread-safe FIFO queue implementation using blocking synchronization

public class BlockingQueue<T> implements Queue<T> {

    // Internal Data
    private Node<T> head, tail;

    // Basic constructor
    public BlockingQueue() {
        head = null;
        tail = null;
    }

    // Thread-safe add
    @Override
    public synchronized void add(T item) {
        if(isEmpty()) {
            head = new Node<T>(item, null);
            tail = head;
        } else {
            Node<T> prevTail = tail;
            tail = new Node<T>(item, null);
            prevTail.setNext(tail);
        }
        notifyAll();
    }

    // Thread-safe remove
    @Override
    public synchronized T remove() {
        while(isEmpty()) {
            try {
                wait();
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread was interrupted while waiting to remove from queue", e);
            }
        }
        
        Node<T> prevHead = head;
        head = head.getNext();

        // If the queue is now empty, update the tail
        if(head == null)
            tail = null;
        return prevHead.get();
    }

    // Thread-safe element
    @Override
    public synchronized T element() {
        while(isEmpty()) {
            try {
                wait();
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread was interrupted while waiting to remove from queue", e);
            }
        }
        return head.get();
    }

    // Helper method for readability of code
    @Override
    public boolean isEmpty() { return head == null || tail == null; }
}