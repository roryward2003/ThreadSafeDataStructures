// Node helper class for building the FIFO queue implementations

class Node {

    // Internal data
    private Object o;
    private Node next;

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
}