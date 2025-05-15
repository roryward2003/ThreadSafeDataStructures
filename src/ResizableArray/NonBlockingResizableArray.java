import java.util.concurrent.atomic.AtomicStampedReference;

// Resizable thread safe array implementation, using only lock-free methods.

public class NonBlockingResizableArray {

    // Atomic stamped reference to the array allows for atomic get, set and extend
    private AtomicStampedReference<Object[]> arrayRef;

    // Constructor
    public NonBlockingResizableArray() {

        // Initialize with size 20 to match q1a implementation
        Object[] initialArray = new Object[20];
        arrayRef = new AtomicStampedReference<>(initialArray, 0);
    }

    // Get object from index i, extending by 10 if i is one beyond the array limit
    public Object get(int i) {

        // Loop until we successfully get the value or extend the array
        while (true) {
            Object[] currentArray = arrayRef.getReference();
            int currentSize = currentArray.length;
            
            // Check if we need to extend
            if (i == currentSize) {
                // Try to extend the array and retry the get operation
                attemptExtend(currentSize);
            } else if (i < currentSize) {
                // Index is valid, return the value
                return currentArray[i];
            } else {
                // Index is out of bounds
                throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + currentSize);
            }
        }
    }

    // Set an object at index i, extending by 10 if i is one beyond the array limit
    public void set(int i, Object o) {

        // Loop until we successfully set the value or extend the array
        while (true) {
            int[] stampHolder = new int[1];
            Object[] currentArray = arrayRef.get(stampHolder);
            int currentSize = currentArray.length;
            
            // Check if we need to extend
            if (i == currentSize) {
                // Try to extend the array and retry the set operation
                attemptExtend(currentSize);
            } else if (i < currentSize) {
                // Create a new array with the updated value
                Object[] newArray = currentArray.clone();
                newArray[i] = o;
                
                // Try to atomically update the array reference
                if (arrayRef.compareAndSet(currentArray, newArray, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE)) {
                    return; // Success
                }
                // If CAS fails, retry the loop with the latest array
            } else {
                // Index is out of bounds
                throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + currentSize);
            }
        }
    }

    // Get the current array size (for the q1.java driver program to use)
    public int getSize() { return arrayRef.getReference().length; }
    
    // Extend the array by 10 elements
    private void attemptExtend(int currentSize) {

        // Get current array and stamp for the CAS operation
        int[] stampHolder = new int[1];
        Object[] currentArray = arrayRef.get(stampHolder);
        
        // Create a new array with 10 more elements
        Object[] newArray = new Object[currentSize + 10];
        
        // Copy elements from the old array
        System.arraycopy(currentArray, 0, newArray, 0, currentSize);

        // Try to atomically update both the array reference and size
        arrayRef.compareAndSet(currentArray, newArray, stampHolder[0], (stampHolder[0]+1) % Integer.MAX_VALUE);
    }
}