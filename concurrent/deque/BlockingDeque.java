package concurrent.deque;
import concurrent.node.Node;

// Thread-safe FIFO queue implementation using blocking synchronization

public class BlockingDeque<T> implements Deque<T> {

    // Internal data
    private Node<T> head;
    private Node<T> tail;
    private int size;

    // Basic constructor
    public BlockingDeque() {
        this.head = null;
        this.tail = null;
    }

    // Add an object to the front of the deque
    @Override
    public synchronized void addFirst(T item) {
        if(isEmpty()) {
            head = tail = new Node<T>(item, null, null);
        } else {
            head = new Node<T>(item, head, null);
            head.getNext().setPrev(head);
        }
        size++;
        notifyAll(); // Wake any threads waiting for an addition
    }

    // Add an object to the back of the deque
    @Override
    public synchronized void addLast(T item) {
        if(isEmpty()) {
            head = tail = new Node<T>(item, null, null);
        } else {
            tail = new Node<T>(item, null, tail);
            tail.getPrev().setNext(tail);
        }
        size++;
        notifyAll(); // Wake any threads waiting for an addition
    }

    // Remove the object at the front of the deque
    @Override
    public synchronized T removeFirst() {
        while(isEmpty()) {
            try {
                wait(); // Wait until there is an object to remove
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for deque addition");
            }
        }

        Node<T> oldHead = head;
        head = head.getNext();
        if(isEmpty()) {
            tail = null; // If deque is now empty, make the tail null
        } else {
            head.setPrev(null);
        }
        size--;
        return oldHead.get();
    }

    // Remove the object at the back of the deque
    @Override
    public synchronized T removeLast() {
        while(isEmpty()) {
            try {
                wait(); // Wait until there is an object to remove
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for deque addition");
            }
        }

        Node<T> oldTail = tail;
        tail = tail.getPrev();
        if(isEmpty()) {
            head = null; // If deque is now empty, make the head null
        } else {
            tail.setNext(null);
        }
        size--;
        return oldTail.get();
    }

    // Get, but do not remove, the object at the front of the deque
    @Override
    public synchronized T getFirst() {
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
    @Override
    public synchronized T getLast() {
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
    @Override
    public synchronized boolean isEmpty() {
        return head == null || tail == null;
    }

    // Helper method for retrieving deque size
    @Override
    public int size() {
        return size;
    }
}