// Thread-safe FIFO queue implementation using blocking synchronization

public class BlockingQueue {

    // Internal Data
    private Node head, tail;

    // Basic constructor
    public BlockingQueue() {
        head = null;
        tail = null;
    }

    // Thread-safe add
    public synchronized void add(Object o) {
        if(isEmpty()) {
            head = new Node(o, null);
            tail = head;
        } else {
            Node prevTail = tail;
            tail = new Node(o, tail);
            prevTail.setNext(tail);
        }
        notifyAll();
    }

    // Thread-safe remove
    public synchronized Object remove() {
        while(isEmpty()) {
            try {
                wait();
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread was interrupted while waiting to remove from queue", e);
            }
        }
        
        Node prevHead = head;
        head = head.getNext();

        // If the queue is now empty, update the tail
        if(head == null)
            tail = null;
        return prevHead.get();
    }

    // Thread-safe element
    public synchronized Object element() {
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
    public boolean isEmpty() { return head == null || tail == null; }
}