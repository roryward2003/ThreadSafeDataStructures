# ResizableArray

## Usage

    cd src/ResizableArray
    javac ResizableArraySimulation.java
    java ResizableArraySimulation k m
    // Where k = % chance of extending (0-100), m = number of accesses per thread

    This will output the execution time for both atomic array strctures,
    each tested using 4 threads.

##  BlockingResizableArray implementation

    My BlockingResizableArray implementation is very straightforward and requires o(n)
    data for synchronization. More specifically it requires O(log(n)) ReentrantLocks,
    where each lock protects a particular segment of the array. I found log base 2 to
    be a nice balance between performance and memory overheads.
    
    The lock for a particular section is acquired when accessing any element in that
    section, and all locks are acquired when resizing the array. When checking if a
    get or set is indexed out of bounds, threads synchronize on the BlockingResizableArray
    object itself. This doesn't prevent modification of individual elements, but does
    prevent the length from being checked while resizing is occuring.

    This approach performs solidly all around, and is great for extending large arrays
    as you only have to copy the locks when a new lock is needed, which becomes less
    frequent as the array grows. There are also only log(n) locks to copy even when
    this does occur.

## LockFreeResizableArray implementation

    My LockFreeResizableArray implementation has a few little complexities to it, but
    shouldn't be too hard to follow. The array is stored as AtomicStampedReference<Object[]>
    A compareAndSet() operation on this reference will only fail if the address of the
    Object[] has changed as compareAndSet() uses "==" and not deep-equality. For this reason
    we must clone the entire array, modify the clone, and compareAndSet the new array
    into the reference each time we execute a set() or extend() operation.

    A stamped reference allows us to elegantly avoid the ABA problem which could still
    occur if a sequence of changes results in the reallocated array appearing in the same
    memory location it was in at the beginning of an overarching compareAndSet(). This
    case is highly unlikely to ever occur but, but it is important to program defensively.

    This approach is miles faster than BlockingResizableArray in most scenarios. It is
    an elegant solution where nobody needs to be blocked at any point and every action
    appears atomic. In spite of this, it still has some flaws. The main flaw is that the
    cloning the array is going to take longer as the array grows, which allows more time
    for other threads to interfere and cause the CAS operation to fail and try again.
    Also, for very large arrays the threads will often end up extending the array only to
    realise that they didn't need to and then drop the changes.

# Stack

## Usage

    cd src/Stack
    javac StackSimulation.java
    java StackSimulation k m
    // Where k = % chance of pop (0-100), m = number of accesses per thread

    This will output the execution time for both atomic stack strctures,
    each tested using 4 threads.

## BlockingStack implementation

    My BlockingStack implementation is very straightforward. It simply synchronizes push
    and pop operations on the same object so that they appear atomic to all other threads.
    This ensures sequential consistency and requires minimal effort to implement or reason
    about. The efficiency is strong in pretty much all scenarios as there is not much
    overhead involved in the locking process.

## LockFreeStack implementation

    My LockFreeStack implementation is a little more nuanced, but still pretty easy to
    follow. Similarly to the ResizableArray above, I've used a StampedAtomicReference here,
    except this time it is a reference to the node that is atop the stack. A push operation
    allocates a new node, links it and increments the stamp. A pop returns null if the stack
    is empty, and otherwise it will remove the top node, increment the stamp and return the
    Object o contained in the previous top node. The ABA problem is avoided by the stamp.
    This solution is pretty easy to reason about, guarantees sequnetial consistency, and is
    very efficent.

# Queue

## Usage

    cd src/Queue
    javac QueueSimulation.java
    java QueueSimulation k m
    // Where k = % chance of remove/element (0-100), m = number of accesses per thread

    This will output the execution time for both atomic stack strctures,
    each tested using 4 threads. If remove/element is chosen there is
    then a 50/50 chance to choose either remove or element.

## BlockingQueue implementation

    This blocking queue implementation simply synchronizes on add, remove and element
    methods in order to ensure atomicity. Remove and Element methods will block by
    sleeping if the queue is empty and will be woken when something is added. The
    queue also exposes an isEmpty() method for the simulator to use, although this is
    not guaranteed to be thread-safe and is solely intended for avoiding a situation
    where all simulating threads attempt to remove from an empty queue and get stuck.
    This implementation is very restrictive and allows for minimal concurrency, but
    that is the best we can really do for a queue as it is an inherently sequential
    data structure.

## LockFreeQueue implementation

    This non-blocking queue implementation is complex but elegant. The head and tail
    Nodes are stored as an AtomicStampableReference<Node[]> where arr[0] = head and
    arr[1] = tail. The pair can be CAS'd atomically using this method. The complexity
    arises when the previousTail.next() and headAndTail[1] must be updated in unison.
    This is not acheivable directly, so instead the headAndTail[1] is updated first,
    and removals will force fail with a -1 expected stamp if head.next() hasn't been
    linked yet. This allows add() and element() to continue to execute concurrently
    even when all elements are lagged, but removals must wait until at least head.next()
    has at least been linked in correctly.

# Queue

## Usage

    cd src/Deque
    javac DequeSimulation.java
    java DequeSimulation k m
    // Where k = % chance of remove/element (0-100), m = number of accesses per thread

    This will output the execution time for both atomic stack strctures,
    each tested using 4 threads. There is always a 50/50 chance of using
    either the front or back of the deque. If remove is chosen there is a
    50/50 chance of using element or remove.

## BlockingDeque implementation

    This BlockingDeque implementation is very straightforward and almost identical to
    the BlockingQueue implementation, except that it uses a doubly-linked list instead
    of a singly-linked list. This allows all operations to be implemented in O(1) time.
    Remove() and Get() methods block on an empty deque and will sleep until notified
    by an Add() call.

## LockFreeDeque implementation

    This non-blocking queue implementation is complex but elegant. The head and tail
    // TODO