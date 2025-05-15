// Node helper class for building the FIFO queue implementations

class Node {

    // Internal data
    private Object o;
    private Node next;
    private Node prev;

    // Basic constructor for Doubly Linked List
    public Node(Object o, Node next, Node prev) {
        this.o    = o;
        this.next = next;
        this.prev = prev;
    }

    // Basic constructor for Singly Linked List
    public Node(Object o, Node next) {
        this.o    = o;
        this.next = next;
    }

    // Get and set Object o
    public Object get() { return o; }
    public void set(Object o) { this.o = o; }

    // Get and Set next Node
    public Node getNext() { return next; }
    public void setNext(Node next) { this.next = next; }

    // Get and Set prev Node
    public Node getPrev() { return prev; }
    public void setPrev(Node prev) { this.prev = prev; }
}