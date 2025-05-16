import java.util.concurrent.atomic.AtomicStampedReference;

// Thread-safe FIFO queue implementation using lock free synchronization

public class LockFreeQueue {

    // Internal data and constants
    AtomicStampedReference<Node[]> headAndTail;
    private static final int HEAD = 0;
    private static final int TAIL = 1;
    private static final Node[] EMPTY = new Node[]{null, null};

    // Basic constructor
    public LockFreeQueue() {
        headAndTail = new AtomicStampedReference<Node[]>(EMPTY, 0);
    }

    // Thread-safe add - Can still succeed if tail isn't linked yet
    public void add(Object o) {
        int[] stampHolder = new int[1];
        Node[] hnt;
        Node newNode = new Node(o, null);
        do {
            hnt = headAndTail.get(stampHolder);
            if(hnt[HEAD] == null && headAndTail.compareAndSet(hnt, new Node[]{newNode, newNode}, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE))
                return;                   // Queue was empty, new node added successfully

        } while(!headAndTail.compareAndSet(hnt, new Node[]{hnt[HEAD], newNode}, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE));
        hnt[TAIL].setNext(newNode);       // Queue had one or more elements, new tail added successfully
    }

    // Thread-safe remove
    public Object remove() {
        int[] stampHolder = new int[1];
        Node[] hnt;
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
    public Object element() {
        return headAndTail.getReference()[HEAD].get(); // null if empty queue
    }

    // Atomic at the point of reading
    public boolean isEmpty() {
        return headAndTail.getReference()[HEAD] == null;
    }
}