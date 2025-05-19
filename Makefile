# Compile
all:
	make ResizableArray; make Stack; make Queue; make Deque; make Barrier

ResizableArray:
	cd src/ResizableArray; javac ResizableArraySimulation.java
	
Stack:
	cd src/Stack; javac StackSimulation.java

Queue:
	cd src/Queue; javac QueueSimulation.java

Deque:
	cd src/Deque; javac DequeSimulation.java

Barrier:
	cd src/Barrier; javac BarrierSimulation.java


# Run simulations
run_All:
	make run_ResizableArray; make run_Stack; make run_Queue; make run_Deque; make run_Barrier

run_ResizableArray:
	cd src/ResizableArray; java ResizableArraySimulation 15 5000

run_Stack:
	cd src/Stack; java StackSimulation 15 1000000

run_Queue:
	cd src/Queue; java QueueSimulation 60 1000000

run_Deque:
	cd src/Deque; java DequeSimulation 60 100000
	
run_Barrier:
	cd src/Barrier; java BarrierSimulation 10 10000


# Tools
clean:
	rm -rf src/*/*.class; rm -rf *.zip