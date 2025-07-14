package concurrent.stack;
import concurrent.node.Node;

// Thread-safe stack implementation using blocking synchronization

public class BlockingStack<T> implements Stack<T> {

    // Internal data
    private Node<T> top;

    // Basic constructor
    public BlockingStack() {
        top = null;
    }

    // Thread-safe pop
    @Override
    public synchronized T pop() {
        if(top != null) {
            T item = top.get();
            top    = top.getNext();
            return item;
        }
        return null;
    }

    // Thread-safe push
    @Override
    public synchronized void push(T item) {
        Node<T> n = new Node<T>(item, top);
        top       = n;
    }
}