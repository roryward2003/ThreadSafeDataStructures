package concurrent.queue;
import java.util.concurrent.atomic.AtomicStampedReference;

import concurrent.node.Node;

// Thread-safe FIFO queue implementation using lock free synchronization

@SuppressWarnings("unchecked")
public class LockFreeQueue<T> implements Queue<T> {

    // Internal data and constants
    AtomicStampedReference<Node<T>[]> headAndTail;
    private static final int HEAD = 0;
    private static final int TAIL = 1;
    private final Node<T>[] EMPTY = new Node[]{null, null};

    // Basic constructor
    public LockFreeQueue() {
        headAndTail = new AtomicStampedReference<Node<T>[]>(EMPTY, 0);
    }

    // Thread-safe add - Can still succeed if tail isn't linked yet
    @Override
    public void add(T item) {
        int[] stampHolder = new int[1];
        Node<T>[] hnt;
        Node<T> newNode = new Node<T>(item, null);
        do {
            hnt = headAndTail.get(stampHolder);
            if(hnt[HEAD] == null && headAndTail.compareAndSet(hnt, new Node[]{newNode, newNode}, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE))
                return;                   // Queue was empty, new node added successfully

        } while(!headAndTail.compareAndSet(hnt, new Node[]{hnt[HEAD], newNode}, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE));
        hnt[TAIL].setNext(newNode);       // Queue had one or more elements, new tail added successfully
    }

    // Thread-safe remove
    @Override
    public T remove() {
        int[] stampHolder = new int[1];
        Node<T>[] hnt;
        do {
            hnt = headAndTail.get(stampHolder);
            if(hnt[HEAD] == null)
                return null;              // Queue was empty, return null

            if(hnt[HEAD] == hnt[TAIL] && headAndTail.compareAndSet(hnt, EMPTY, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE))
                return hnt[HEAD].get();   // Queue had one element, removed head successfully

        // Force fail with stamp -1 if head.next() hasn't been linked yet
        } while(!headAndTail.compareAndSet(hnt, new Node[]{hnt[HEAD].getNext(), hnt[TAIL]},
        (hnt[HEAD].getNext() == null && hnt[HEAD] != hnt[TAIL] ? -1 : stampHolder[0]), (stampHolder[0]+1) % Integer.MAX_VALUE));
        return hnt[HEAD].get();           // Queue had more than one element, removed head successfully
    }

    // Thread-safe element - Atomic at the point of reading
    @Override
    public T element() {
        return headAndTail.getReference()[HEAD].get(); // null if empty queue
    }

    // Atomic at the point of reading
    @Override
    public boolean isEmpty() {
        return headAndTail.getReference()[HEAD] == null;
    }
}