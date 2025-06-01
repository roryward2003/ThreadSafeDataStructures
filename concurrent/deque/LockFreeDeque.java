package concurrent.deque;
import java.util.concurrent.atomic.AtomicStampedReference;

import concurrent.node.Node;

// Thread-safe FIFO queue implementation using lock free synchronization

public class LockFreeDeque {

    // Internal data and constants
    AtomicStampedReference<Node[]> headAndTail;
    private static final int HEAD = 0;
    private static final int TAIL = 1;
    private static final Node[] EMPTY = new Node[]{null, null};

    // Basic constructor
    public LockFreeDeque() {
        headAndTail = new AtomicStampedReference<Node[]>(EMPTY, 0);
    }

    // Thread-safe add - Can still succeed if head isn't linked yet
    public void addFirst(Object o) {
        int[] stampHolder = new int[1];
        Node[] hnt;
        Node newNode;
        do {
            hnt = headAndTail.get(stampHolder);
            newNode = new Node(o, hnt[HEAD], null);
            if(hnt[HEAD] == null && headAndTail.compareAndSet(hnt, new Node[]{newNode, newNode}, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE))
                return;                   // Deque was empty, new node added successfully

        } while(!headAndTail.compareAndSet(hnt, new Node[]{newNode, hnt[TAIL]}, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE));
        hnt[HEAD].setPrev(newNode);       // Deque had one or more elements, new head added successfully
    }

    // Thread-safe add - Can still succeed if tail isn't linked yet
    public void addLast(Object o) {
        int[] stampHolder = new int[1];
        Node[] hnt;
        Node newNode;
        do {
            hnt = headAndTail.get(stampHolder);
            newNode = new Node(o, null, hnt[TAIL]);
            if(hnt[HEAD] == null && headAndTail.compareAndSet(hnt, new Node[]{newNode, newNode}, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE))
                return;                   // Deque was empty, new node added successfully

        } while(!headAndTail.compareAndSet(hnt, new Node[]{hnt[HEAD], newNode}, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE));
        hnt[TAIL].setNext(newNode);       // Deque had one or more elements, new tail added successfully
    }

    // Thread-safe remove
    public Object removeFirst() {
        int[] stampHolder = new int[1];
        Node[] hnt;
        Node tempNext;
        do {
            hnt = headAndTail.get(stampHolder);
            if(hnt[HEAD] == null)
                return null;              // Deque was empty, return null

            if(hnt[HEAD] == hnt[TAIL] && headAndTail.compareAndSet(hnt, EMPTY, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE))
                return hnt[HEAD].get();   // Deque had one element, removed head successfully

        // Conditional force fails with stamp -1 if head.next() or head itself haven't been linked yet
        } while(!headAndTail.compareAndSet(hnt, new Node[]{hnt[HEAD].getNext(), hnt[TAIL]},
        ((( (tempNext = hnt[HEAD].getNext()) == null && hnt[HEAD] != hnt[TAIL] ) || tempNext.getPrev() == null)
        ? -1 : stampHolder[0]), (stampHolder[0]+1) % Integer.MAX_VALUE));

        return hnt[HEAD].get();           // Deque had more than one element, removed head successfully
    }

    // Thread-safe remove
    public Object removeLast() {
        int[] stampHolder = new int[1];
        Node[] hnt;
        Node tempPrev;
        do {
            hnt = headAndTail.get(stampHolder);
            if(hnt[HEAD] == null)
                return null;              // Deque was empty, return null

            if(hnt[HEAD] == hnt[TAIL] && headAndTail.compareAndSet(hnt, EMPTY, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE))
                return hnt[TAIL].get();   // Deque had one element, removed tail successfully

        // Conditional force fails with stamp -1 if tail.prev() or tail itself haven't been linked yet
        } while(!headAndTail.compareAndSet(hnt, new Node[]{hnt[HEAD], hnt[TAIL].getPrev()},
        ((( (tempPrev = hnt[TAIL].getPrev()) == null && hnt[TAIL] != hnt[HEAD] ) || tempPrev.getNext() == null)
        ? -1 : stampHolder[0]), (stampHolder[0]+1) % Integer.MAX_VALUE));

        return hnt[TAIL].get();           // Deque had more than one element, removed tail successfully
    }

    // Thread-safe element - Atomic at the point of reading
    public Object getFirst() {
        Node tail = headAndTail.getReference()[TAIL];
        if(tail == null) return null;
        return tail.get();
    }

    // Thread-safe element - Atomic at the point of reading
    public Object getLast() {
        Node head = headAndTail.getReference()[HEAD];
        if(head == null) return null;
        return head.get();
    }

    // Helper method for readability of code and ease of simulation
    public boolean isEmpty() {
        return headAndTail.getReference()[HEAD] == null;
    }
}