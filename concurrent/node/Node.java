package concurrent.node;

// Node helper class for building DSA implementations

public class Node<T> {

    // Internal data
    private T item;
    private Node<T> next;
    private Node<T> prev;

    // Basic constructor for Doubly Linked List
    public Node(T item, Node<T> next, Node<T> prev) {
        this.item = item;
        this.next = next;
        this.prev = prev;
    }

    // Basic constructor for Singly Linked List
    public Node(T item, Node<T> next) {
        this.item = item;
        this.next = next;
    }

    // Get and set T item
    public T get() { return item; }
    public void set(T item) { this.item = item; }

    // Get and Set next Node
    public Node<T> getNext() { return next; }
    public void setNext(Node<T> next) { this.next = next; }
    
    // Get and Set prev Node
    public Node<T> getPrev() { return prev; }
    public void setPrev(Node<T> prev) { this.prev = prev; }
}