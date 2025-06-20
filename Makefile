# Suppress make verbosity
MAKEFLAGS += --no-print-directory

# Compile
all:
	$(MAKE) array   && $(MAKE) stack && $(MAKE) queue && $(MAKE) deque && \
	$(MAKE) barrier && $(MAKE) ll    && $(MAKE) set   && $(MAKE) hashtable

array:         ; javac concurrent/array/ArraySimulation.java
stack:         ; javac concurrent/stack/StackSimulation.java
queue:         ; javac concurrent/queue/QueueSimulation.java
deque:         ; javac concurrent/deque/DequeSimulation.java
barrier:       ; javac concurrent/barrier/BarrierSimulation.java
ll:            ; javac concurrent/linkedlist/LLSimulation.java
set:           ; javac concurrent/set/SetSimulation.java
hashtable:     ; javac concurrent/hashtable/HashTableSimulation.java

# Run simulations
run_all:
	$(MAKE) run_array   && $(MAKE) run_stack && $(MAKE) run_queue && $(MAKE) run_deque && \
	$(MAKE) run_barrier && $(MAKE) run_ll    && $(MAKE) run_set   && $(MAKE) run_hashtable
	
run_array:     ; java concurrent/array/ArraySimulation 15 5000
run_stack:     ; java concurrent/stack/StackSimulation 15 1000000
run_queue:     ; java concurrent/queue/QueueSimulation 60 1000000
run_deque:     ; java concurrent/deque/DequeSimulation 60 100000
run_barrier:   ; java concurrent/barrier/BarrierSimulation 10 10000
run_ll:        ; java concurrent/linkedlist/LLSimulation 50 10000
run_set:       ; java concurrent/set/SetSimulation 20 10000
run_hashtable: ; java concurrent/hashtable/HashTableSimulation 40 10000

# Tools
clean:         ; rm -rf concurrent/*/*.class *.zip