package concurrent.stack;
import concurrent.node.Node;

// Thread-safe stack implementation using blocking synchronization

public class BlockingStack {

    // Internal data
    private Node top;

    // Basic constructor
    public BlockingStack() {
        top = null;
    }

    // Thread-safe pop
    public synchronized Object pop() {
        if(top != null) {
            Object o = top.get();
            top      = top.getNext();
            return o;
        }
        return null;
    }

    // Thread-safe push
    public synchronized void push(Object o) {
        Node n = new Node(o, top);
        top    = n;
    }
}