# ResizableArray

### Usage

    cd src/ResizableArray
    javac ResizableArraySimulation.java
    java ResizableArraySimulation k m
    // Where k = % chance of extending (0-100), m = number of accesses per thread

    This will output the execution time for both atomic Array structures,
    each tested using 4 threads.

####  BlockingResizableArray implementation

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

#### LockFreeResizableArray implementation

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

### Usage

    cd src/Stack
    javac StackSimulation.java
    java StackSimulation k m
    // Where k = % chance of pop (0-100), m = number of accesses per thread

    This will output the execution time for both atomic Stack structures,
    each tested using 4 threads.

#### BlockingStack implementation

    My BlockingStack implementation is very straightforward. It simply synchronizes push
    and pop operations on the same object so that they appear atomic to all other threads.
    This ensures sequential consistency and requires minimal effort to implement or reason
    about. The efficiency is strong in pretty much all scenarios as there is not much
    overhead involved in the locking process.

#### LockFreeStack implementation

    My LockFreeStack implementation is a little more nuanced, but still pretty easy to
    follow. Similarly to the ResizableArray above, I've used a StampedAtomicReference here,
    except this time it is a reference to the node that is atop the stack. A push operation
    allocates a new node, links it and increments the stamp. A pop returns null if the stack
    is empty, and otherwise it will remove the top node, increment the stamp and return the
    Object o contained in the previous top node. The ABA problem is avoided by the stamp.
    This solution is pretty easy to reason about, guarantees sequnetial consistency, and is
    very efficent.

# Queue

### Usage

    cd src/Queue
    javac QueueSimulation.java
    java QueueSimulation k m
    // Where k = % chance of remove/element (0-100), m = number of accesses per thread

    This will output the execution time for both atomic Queue structures,
    each tested using 4 threads. If remove/element is chosen there is
    then a 50/50 chance to choose either remove or element.

#### BlockingQueue implementation

    This blocking queue implementation simply synchronizes on add, remove and element
    methods in order to ensure atomicity. Remove and Element methods will block by
    sleeping if the queue is empty and will be woken when something is added. The
    queue also exposes an isEmpty() method for the simulator to use, although this is
    not guaranteed to be thread-safe and is solely intended for avoiding a situation
    where all simulating threads attempt to remove from an empty queue and get stuck.
    This implementation is very restrictive and allows for minimal concurrency, but
    that is the best we can really do for a queue as it is an inherently sequential
    data structure.

#### LockFreeQueue implementation

    This non-blocking queue implementation is complex but elegant. The head and tail
    Nodes are stored as an AtomicStampableReference<Node[]> where arr[0] = head and
    arr[1] = tail. The pair can be CAS'd atomically using this method. The complexity
    arises when the previousTail.next() and headAndTail[1] must be updated in unison.
    This is not acheivable directly, so instead the headAndTail[1] is updated first,
    and removals will force fail with a -1 expected stamp if head.next() hasn't been
    linked yet. This allows add() and element() to continue to execute concurrently
    even when all elements are lagged, but removals must wait until at least head.next()
    has at least been linked in correctly.

# Deque

### Usage

    cd src/Deque
    javac DequeSimulation.java
    java DequeSimulation k m
    // Where k = % chance of remove/element (0-100), m = number of accesses per thread

    This will output the execution time for both atomic Deque structures,
    each tested using 4 threads. There is always a 50/50 chance of using
    either the front or back of the deque. If remove is chosen there is a
    50/50 chance of using element or remove.

#### BlockingDeque implementation

    This BlockingDeque implementation is very straightforward and almost identical to
    the BlockingQueue implementation, except that it uses a doubly-linked list instead
    of a singly-linked list. This allows all operations to be implemented in O(1) time.
    Remove() and Get() methods block on an empty deque and will sleep until notified
    by an Add() call.

#### LockFreeDeque implementation

    This LockFreeDeque implementation has quite a few complexities to it, but is robust
    in the face of tail lag, head lag, the ABA problem and any other data races that
    could occur between additions, removals and gets at either end of the queue. The
    deque uses a doubly-linked list structure to implement O(1) operations. The CAS
    primitive is used with an atomic mark boolean to atomically update the references
    from the sentinel nodes. Marked nodes are cleaned up elegantly by other threads.

    Two stage linking of newly added nodes is handled seamlessly by allowing other
    threads to work around partially linked nodes, but not directly on them, and then
    catching up to their changes when making the second link.

# Barrier

### Usage

    cd src/Barrier
    javac BarrierSimulation.java
    java BarrierSimulation t n
    // Where t = number of threads, n = number of barrier re-uses

    This will output the execution time for both atomic Barrier structures,
    each tested using t threads.

#### BlockingBarrier implementation

    This BlockingBarrier is a sense reversing n-thread reusable barrier. The arrive()
    method is synchronised and wait() & notifyAll() are used to ensure there is no
    busy waiting. The constantly inverting phase allows threads to differentiate
    between subsequent barrier reuses and thus prevents starvation and data races.

#### LockFreeBarrier implementation

    This LockFreeBarrier is also a sense reversing n-thread reusable barrier, but it
    is far more efficient than the blocking version. It very simply uses an atomic
    integer to store the count, and an atomic boolean to store the sense. The count
    is atomically decremented and retrieved with the decrementAndGet() method. The
    final thread to arrive will observe this value as 0 and reset it to numThreads,
    then toggle the sense to release all waiting threads. These waiting threads will
    yield while they loop to prioritise threads that have not arrived at the barrier
    yet. We do not need to use compareAndSet for the count reset or sense reversal as
    there could not possibly be contention here as we can only reach this code if all
    other threads are looping on a yield() call within the barrier, and only reading
    the boolean value.

# Linked List

### Usage

    cd src/LinkedList
    javac LLSimulation.java
    java LLSimulation k n
    // Where k = %chance of retrieval, n = number of operations

    This will output the execution time for both atomic Linked List structures,
    each tested using 4 threads. Each operation has a k% chance of being a removal.
    If removal is chosen there is a 50/50 chance for remove or get, and similarly
    if insertion is chosen there is a 50/50 chance of indexed insertion and append.

#### BlockingLL implementation

    This BlockingLL is very straightforward. The head, tail and size are all tracked
    to allow for O(1) appends and size check as well as O(n) worst case for searching,
    and O(k) indexed insertions and retrievals at index k. Quite simply, all methods
    are synchronized to prevent multiple threads form modifying the state of the list
    in parallel. This is a very coarse-grained approach that allows for no paralellism
    and thus is very inefficient, but it is extremely easy to reason about.

#### LockFreeLL implementation

    This LockFreeLL implementation is very difficult to reason about if you are not
    well versed in lock-free data strutcure design. Essentially, I have used Harris's
    approach of logical removal before physical removal. Traversals skip marked/logically
    removed nodes. CAS operations fail if the node is marked, so parallel removals and
    insertions cannot both succeed. Similarly, two parallel removals will not both succeed.
    This solves the majority of our complexity. We cannot consistently track the size,
    so we retrieve it via traversal and avoid relying on it where possible. The head
    and tail references both reference the same sentinel node at all times.

# Set

### Usage

    cd src/Set
    javac SetSimulation.java
    java SetSimulation k n
    // Where k = %chance of insertion or retrieval, n = number of operations

    This will output the execution time for both atomic Set structures, each tested
    using 4 threads. Each operation has a k% chance of being a removal or insertion.
    If this is chosen there is a 50/50 chance for remove or insertion, and if this is
    not chosen then a search is performed. That is, there is a (1-k)% chance of search,
    a (k/2)% chance of insertion and a (k/2)% chance of retrieval. Typical real world
    set usage is about 80% search biased.

#### CoarseBlockingSet implementation

    This blocking set uses a coarse grained blocking approach. All additions, removals
    and searches just lock on the set object itself using the synchronized keyword. This
    limits concurrency significantly but does ensure sequential consistency and thread
    safety by completely eliminating the potential for data races to occur. Null is
    treated the same as any other obhect in that it can be added, but only once.

#### FineBlockingSet implementation

    This blocking set uses a fine grained blocking approach with "hand over hand" locking
    on all traversals. Atomicity is guaranteed by maintaining a sorted list internal
    structure, where elements are sorted by their hashcode. This way insertions, removals
    and searches will all localize to one place for a given element, and as long as operations
    are guarded by the locks either side of this location then atomicity is guaranteed.

    Importantly, collisions in hashcodes can and will occur, and is resolved by iterating over
    key matches when needed, and inserting after the last key match currently in the set.
    This approach allows for much greater parallelism than the coarse grained blocking version
    becuase multiple threads can traverse the list, only locking at most two nodes at a time.
    The tradeoff is complexity, as this version is much harder to reason about than the coarse
    grained version. Similarly, the capacity for parallelism is lower than the lock free version,
    but this lock free version is immensely more difficult to reason about and understand.

    This approach is a nice balance of simplicity, elegance and parallelism, but it does involve
    a large amount of locking overhead which comes with an efficiency cost. Thus this approach
    shines brightest under high contention, when its parallelism can be exploited, and may
    struggle to match the coarse grained version's execution speeds under low contention.

#### LockFreeSet implementation

    This lock free set implementation maintains a sorted linked list internal structure
    with sentinel head and tail nodes. Nodes are sorted by the hashcode of their object,
    with the head and tail having keys Integer.MIN_VALUE and Integer.MAX_VALUE respectively.
    Collisions in hashcodes are handled elegantly by searching through the key matches,
    and then appending the new node after the last key match. Nodes are marked for logical
    removal, then attempted to be physically removed. Traversals from add() and remove()
    use a helper function called find() which also cleans up any marked nodes along the way.
    
    Interference can occasionally cause a retry of the entire operation which can reduce
    performance under high contention, but the benefits of lock free traversal and both
    optimistic and lazy synchronization push the performance of this implementation far
    beyond the capabilites of both blocking implementations.

# HashTable

### Usage

    cd src/HashTable
    javac HashTableSimulation.java
    java HashTableSimulation k n
    // Where k = %chance of insertion, n = number of operations

    This will output the execution time for both atomic HashTable structures, each tested
    using 4 threads. Each operation has a k% chance of being an insertion. If this is not
    chosen there is a 50/50 chance for removal or retrieval.

#### CoarseBlockingHashTable implementation

    This blocking hash table implementation is pretty straightforward as it only uses one
    lock to guard all hash table accesses. This is achieved by synchronizing all methods on
    the hash table object itself using the synchronized keyword. The hash table starts with
    an initial size of 16 buckets and doubles in size whenever the load factor exceeds 0.75.

#### FineBlockingHashTable implementation

    This blocking hash table implementation is a little more complicated as it uses N+1 locks
    for N buckets. One lock for each bucket, plus a sizeLock for the size related operations;
    resize(), size() and isEmpty(). The hash table starts with an initial size of 16 buckets
    and doubles in size whenever the load factor exceeds 0.75.
    
    The complexity here arises when a resize happens concurrently with insertions or removals
    because the hash index used to index into the locks and buckets arrays will become outdated
    as soon as the arraySize is doubled by the resizing thread. This is solved by ensuring that
    resizal requires not just the sizeLock but also all of the bucket locks. Insertions and
    removals must still use some retry logic to ensure that the hash table wasn't resized while
    they were waiting to acquire their chosen lock, and importantly they must store a local ref
    to the lock they acquired because they will no longer be able to locate it using hashing if
    the table has been resized, and as such they would not be able to unlock it.

#### LockFreeHashTable implementation

    // TODO