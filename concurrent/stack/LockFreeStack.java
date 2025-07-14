package concurrent.stack;
import java.util.concurrent.atomic.AtomicStampedReference;

import concurrent.node.Node;

// Thread-safe stack implementation using lock free synchronization

public class LockFreeStack<T> implements Stack<T> {

    // Internal data
    private AtomicStampedReference<Node<T>> top;

    // Basic constructor
    public LockFreeStack() {
        top = new AtomicStampedReference<Node<T>>(null, 0);
    }

    // Thread-safe pop
    @Override
    public T pop() {
        int[] stampHolder = new int[1];
        Node<T> expected;
        if((expected = top.get(stampHolder)) == null) { return null; } // Return null if stack empty
        
        // Use CAS logic to atomically update the top of the stack and stamp
        while(!top.compareAndSet(expected, expected.getNext(), stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE))
            if((expected = top.get(stampHolder)) == null) return null; // Return null if stack empty

        return expected.get();
    }

    // Thread-safe push
    @Override
    public void push(T o) {
        int[] stampHolder = new int[1];
        Node<T> expected;
        Node<T> newTop = new Node<T>(o, (expected = top.get(stampHolder)));

        // Use CAS logic to atomically update the top of the stack and stamp
        while(!top.compareAndSet(expected, newTop, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE))
            newTop = new Node<T>(o, (expected = top.get(stampHolder)));
    }
}