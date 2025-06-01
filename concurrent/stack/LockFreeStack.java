package concurrent.stack;
import java.util.concurrent.atomic.AtomicStampedReference;

import concurrent.node.Node;

// Thread-safe stack implementation using lock free synchronization

public class LockFreeStack {

    // Internal data
    private AtomicStampedReference<Node> top;

    // Basic constructor
    public LockFreeStack() {
        top = new AtomicStampedReference<Node>(null, 0);
    }

    // Thread-safe pop
    public Object pop() {
        int[] stampHolder = new int[1];
        Node expected;
        if((expected = top.get(stampHolder)) == null) { return null; } // Return null if stack empty
        
        // Use CAS logic to atomically update the top of the stack and stamp
        while(!top.compareAndSet(expected, expected.getNext(), stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE))
            expected = top.get(stampHolder);

        return expected.get();
    }

    // Thread-safe push
    public void push(Object o) {
        int[] stampHolder = new int[1];
        Node expected;
        Node newTop = new Node(o, (expected = top.get(stampHolder)));

        // Use CAS logic to atomically update the top of the stack and stamp
        while(!top.compareAndSet(expected, newTop, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE))
            newTop = new Node(o, (expected = top.get(stampHolder)));
    }
}