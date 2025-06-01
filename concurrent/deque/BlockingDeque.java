package concurrent.deque;
import concurrent.node.Node;

// Thread-safe FIFO queue implementation using blocking synchronization

public class BlockingDeque {

    // Internal data
    private Node head;
    private Node tail;

    // Basic constructor
    public BlockingDeque() {
        this.head = null;
        this.tail = null;
    }

    // Add an object to the front of the deque
    public synchronized void addFirst(Object o) {
        if(isEmpty()) {
            head = tail = new Node(o, null, null);
        } else {
            head = new Node(o, head, null);
            head.getNext().setPrev(head);
        }
        notifyAll(); // Wake any threads waiting for an addition
    }

    // Add an object to the back of the deque
    public synchronized void addLast(Object o) {
        if(isEmpty()) {
            head = tail = new Node(o, null, null);
        } else {
            tail = new Node(o, null, tail);
            tail.getPrev().setNext(tail);
        }
        notifyAll(); // Wake any threads waiting for an addition
    }

    // Remove the object at the front of the deque
    public synchronized Object removeFirst() {
        while(isEmpty()) {
            try {
                wait(); // Wait until there is an object to remove
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for deque addition");
            }
        }

        Node oldHead = head;
        head = head.getNext();
        if(isEmpty()) {
            tail = null; // If deque is now empty, make the tail null
        } else {
            head.setPrev(null);
        }
        return oldHead.get();
    }

    // Remove the object at the back of the deque
    public synchronized Object removeLast() {
        while(isEmpty()) {
            try {
                wait(); // Wait until there is an object to remove
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for deque addition");
            }
        }

        Node oldTail = tail;
        tail = tail.getPrev();
        if(isEmpty()) {
            head = null; // If deque is now empty, make the head null
        } else {
            tail.setNext(null);
        }
        return oldTail.get();
    }

    // Get, but do not remove, the object at the front of the deque
    public synchronized Object getFirst() {
        while(isEmpty()) {
            try {
                wait();
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for deque addition");
            }
        }
        return head.get();
    }

    // Get, but do not remove, the object at the back of the deque
    public synchronized Object getLast() {
        while(isEmpty()) {
            try {
                wait();
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for deque addition");
            }
        }
        return tail.get();
    }

    // Helper method for readability of code and ease of simulation
    public synchronized boolean isEmpty() {
        return head == null || tail == null;
    }
}