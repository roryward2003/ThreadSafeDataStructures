package concurrent.deque;
import concurrent.node.LockFreeNode;

// Thread-safe FIFO queue implementation using lock free synchronization

public class LockFreeDeque<T> implements Deque<T> {

    // Internal data and constants
    private final LockFreeNode<T> sentinelHead;
    private final LockFreeNode<T> sentinelTail;

    // Basic constructor
    public LockFreeDeque() {
        sentinelHead = new LockFreeNode<T>(null, null, null);
        sentinelTail = new LockFreeNode<T>(null, null, sentinelHead);
        sentinelHead.setNext(sentinelTail);
    }

    // Thread-safe add
    @Override
    public void addFirst(T item) {
        LockFreeNode<T> oldHead, newHead;

        do { // Left to Right then Right to Left 
            oldHead = sentinelHead.getNext();

            // Remove marked nodes
            while(oldHead.isMarked()) {
                sentinelHead.compareAndSetNext(oldHead, oldHead.getNext());
                oldHead = oldHead.getNext();
            }

            // Insert new node
            newHead = new LockFreeNode<T>(item, oldHead, sentinelHead);
        } while (!sentinelHead.compareAndSetNext(oldHead, newHead));

        // Catch up on the opposite link
        while(oldHead != null && !oldHead.compareAndSetPrev(sentinelHead, newHead)) {
            oldHead = oldHead.getPrev();
        }
    }

    // Thread-safe add
    @Override
    public void addLast(T item) {
        LockFreeNode<T> oldTail, newTail;
            
        do { // Right to Left then Left to Right
            oldTail = sentinelTail.getPrev();

            // Remove marked nodes
            while(oldTail.isMarked()) {
                sentinelTail.compareAndSetPrev(oldTail, oldTail.getPrev());
                oldTail = oldTail.getPrev();
            }

            // Insert new node
            newTail = new LockFreeNode<T>(item, sentinelTail, oldTail);
        } while (!sentinelTail.compareAndSetPrev(oldTail, newTail));
        
        // Catch up on the opposite link
        while(oldTail != null && !oldTail.compareAndSetNext(sentinelTail, newTail)) {
            oldTail = oldTail.getNext();
        }
    }

    // Thread-safe remove
    @Override
    public T removeFirst() {
        LockFreeNode<T> oldHead;
        
        do { // Remove marked nodes
            oldHead = sentinelHead.getNext();
            while(oldHead.isMarked()) {
                sentinelHead.compareAndSetNext(oldHead, oldHead.getNext());
                oldHead = oldHead.getNext();
            }

            // Logically remove head
            if (oldHead == sentinelTail) return null;
        } while (!oldHead.attemptMark(false, true));

        return oldHead.get();
    }

    // Thread-safe remove
    @Override
    public T removeLast() {
        LockFreeNode<T> oldTail;

        do { // Remove marked nodes
            oldTail = sentinelTail.getPrev();
            while(oldTail.isMarked()) {
                sentinelTail.compareAndSetPrev(oldTail, oldTail.getPrev());
                oldTail = oldTail.getPrev();
            }
            
            // Logically remove tail
            if (oldTail == sentinelHead) return null;
        } while (!oldTail.attemptMark(false, true));

        return oldTail.get();
    }

    // Thread-safe peek - Atomic at the point of reading
    @Override
    public T getFirst() {
        return sentinelHead.getNext().get();
    }

    // Thread-safe peek - Atomic at the point of reading
    @Override
    public T getLast() {
        return sentinelTail.getPrev().get();
    }

    // Helper method for readability of code and ease of simulation
    @Override
    public boolean isEmpty() {
        return sentinelHead.getNext() == sentinelTail || sentinelTail.getPrev() == sentinelHead;
    }

    // NON THREAD SAFE helper method for retrieving deque size, for simulation only
    @Override
    public int size() {
        LockFreeNode<T> curr = sentinelHead.getNext();
        int count = 0;
        while(curr != sentinelTail) {
            if(!curr.isMarked()) count++;
            curr = curr.getNext();
        }
        return count;
    }
}