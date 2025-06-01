package concurrent.linkedlist;
import java.util.concurrent.atomic.AtomicReference;

import concurrent.node.LockFreeNode;

// Thread-safe Singly-Linked List implementation using lock-free synchronization

public class LockFreeLL {

    // Internal data
    private final AtomicReference<LockFreeNode> head;
    private final AtomicReference<LockFreeNode> tail;

    // Basic constructor
    public LockFreeLL() {
        LockFreeNode sentinel = new LockFreeNode(null, null);

        // Head and Tail both point to a single sentinel node
        head = new AtomicReference<LockFreeNode>(sentinel);
        tail = new AtomicReference<LockFreeNode>(sentinel);
    }

    // Thread-safe append
    public void add(Object o) {
        LockFreeNode newNode = new LockFreeNode(o, null);

        while(true) {
            LockFreeNode last = tail.get();
            LockFreeNode next = last.getNext();

            // If tail really is the last node
            if(next == null) {
                if(last.compareAndSetNext(null, newNode, false)) {
                    tail.compareAndSet(last, newNode);     // Try to swing tail to new node
                    return;
                }
            } else {
                tail.compareAndSet(last, next);            // Help advance the tail
            }
        }
    }

    // Thread-safe indexed insertion
    public void add(int index, Object o) {
        if(index < 0) {
            throw new IndexOutOfBoundsException("Index "+index+" out of bounds");
        }

        LockFreeNode prevNode, nextNode;
        do {
            prevNode = head.get();
            for(int n=0; n < index; n++) {                 // Traverse to index - 1
                prevNode = prevNode.getNext();
                if(prevNode == null)
                    throw new IndexOutOfBoundsException("Index "+index+" out of bounds");
            }
            nextNode = prevNode.getNext();

        // If CAS fails, we must redo the whole index search :(
        } while(!prevNode.compareAndSetNext(nextNode, new LockFreeNode(o, nextNode), false));

        if(prevNode == tail.get())
            tail.set(prevNode.getNext());                  // If tail changed, chase it
    }

    // Thread-safe destructive retrieval
    public Object remove(int index) {
        if(index < 0) {
            throw new IndexOutOfBoundsException("Index "+index+" out of bounds");
        } else if(index == 0 && isEmpty()) {
            return null;
        }

        LockFreeNode prevNode, targetNode, nextNode;
        do {
            prevNode = head.get();
            for(int n=0; n < index; n++) {                 // Traverse to index - 1
                prevNode = prevNode.getNext();
                if(prevNode == null)
                    throw new IndexOutOfBoundsException("Index "+index+" out of bounds");
            }

            targetNode = prevNode.getNext();
            if(targetNode == null)
                return null;

            nextNode   = targetNode.getNext();

        // If CAS fails, we must redo the whole index search :(
        } while(!targetNode.compareAndSetNext(nextNode, nextNode, true));

        prevNode.setNext(nextNode);                        // Physically remove after logical removal

        // If tail was removed, update tail
        if(tail.get() == targetNode) {
            tail.set(prevNode);
        }

        return targetNode.get();
    }

    // Thread-safe non-destructive retrieval
    public Object get(int index) {
        if(index < 0) {
            throw new IndexOutOfBoundsException("Index "+index+" out of bounds");
        }

        LockFreeNode targetNode = head.get().getNext();
        for(int n=0; n < index; n++) {                     // Traverse to index - 1
            if(targetNode == null)
                throw new IndexOutOfBoundsException("Index "+index+" out of bounds");
            targetNode = targetNode.getNext();
        }

        if(targetNode == null || targetNode.isMarked())
            return null;

        return targetNode.get();
    }

    // Thread-safe list search
    public boolean contains(Object o) {
        LockFreeNode targetNode = head.get().getNext();

        if(o == null) {
            while(targetNode != null) {
                if(!targetNode.isMarked() && targetNode.get() == null)
                    return true;
                targetNode = targetNode.getNext();
            }
        } else {
            while(targetNode != null) {
                if(!targetNode.isMarked() && o.equals(targetNode.get()))
                    return true;
                targetNode = targetNode.getNext();
            }
        }
        return false;
    }

    // Thread-safe size retrieval
    public int size() {
        LockFreeNode targetNode = head.get().getNext();
        int size = 0;
        while(targetNode != null) {
            if(!targetNode.isMarked())
                size++;
            targetNode = targetNode.getNext();
        }
        return size;
    }

    // Thread-safe empty check
    public boolean isEmpty() {
        LockFreeNode first = head.get().getNext();
        return (first == null || first.isMarked());
    }
}