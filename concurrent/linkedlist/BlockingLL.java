package src.linkedlist;
import src.node.Node;

// Thread-safe Singly-Linked List implementation using blocking synchronization

public class BlockingLL {

    // Internal data
    private Node head;
    private Node tail;
    private int  size;

    // Basic constructor
    public BlockingLL() {
        head = null;
        tail = null;
        size = 0;
    }

    // Thread safe non-indexed insertion by appending
    public synchronized void add(Object o) {
        Node newNode = new Node(o, null);
        if(isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            tail = newNode;
        }
        size++;
    }

    // Thread safe indexed insertion
    public synchronized void add(int index, Object o) {
        if(index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index "+index+" invalid for size "+size+"");
        } else if(isEmpty() || index == size) {            // Empty case or any tail append
            add(o);
        } else if(index == 0) {                            // Head insert
            Node newNode = new Node(o, head);
            head = newNode;
            if(size == 0) tail = newNode;
            size++;
        } else {                                           // Non-empty and not an append
            Node prevNode = head;
            for(int i=0; i<index-1; i++) {
                prevNode = prevNode.getNext();
            }
            Node newNode = new Node(o, prevNode.getNext());
            prevNode.setNext(newNode);
            size++;                                        // Keep size consistent
        }
    }

    // Thread safe destructive indexed retrieval
    public synchronized Object remove(int index) {
        if(index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index "+index+" invalid for size "+size+"");
        } else if(size == 1) {                             // Tail and head removal
            Node targetNode = head;
            head = null;
            tail = null;
            size = 0;
            return targetNode.get();
        } else if(index == 0) {                            // Head removal only
            Node targetNode = head;
            head = head.getNext();
            size--;
            return targetNode.get();
        } else {                                           // Non-empty and safe index
            Node prevNode = head;
            for(int i=0; i<index-1; i++) {
                prevNode = prevNode.getNext();
            }
            Node targetNode = prevNode.getNext();
            prevNode.setNext(targetNode.getNext());
            if(index == --size) {
                tail = prevNode;                           // If tail removed, set new tail
            }
            return targetNode.get();
        }
    }

    // Thread safe non destructive indexed retrieval
    public synchronized Object get(int index) {
        if(index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index "+index+" invalid for size "+size+"");
        } else {
            Node targetNode = head;
            for(int i=0; i<index; i++) {
                targetNode = targetNode.getNext();
            }
            return targetNode.get();
        }
    }

    // Thread safe list search via traversal
    public synchronized boolean contains(Object o) {
        Node currentNode = head;
        if(o == null) {                                    // Search for null object
            while(currentNode != null) {
                if(currentNode.get() == null)
                    return true;
                currentNode = currentNode.getNext();
            }
        } else {                                           // Search for non-null object
            while(currentNode != null) {
                if(o.equals(currentNode.get()))
                    return true;
                currentNode = currentNode.getNext();
            }
        }
        return false;
    }

    // Thread safe size retrieval
    public synchronized int size() {
        return size;
    }

    // Thread safe empty check for readability and simulation
    public synchronized boolean isEmpty() {
        return size == 0;
    }
}
