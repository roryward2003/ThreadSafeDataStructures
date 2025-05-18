# Compile
all:
	make ResizableArray; make Stack; make Queue; Make Deque

ResizableArray:
	cd src/ResizableArray; javac ResizableArraySimulation.java
	
Stack:
	cd src/Stack; javac StackSimulation.java

Queue:
	cd src/Queue; javac QueueSimulation.java

Deque:
	cd src/Deque; javac DequeSimulation.java


# Run simulations
run_All:
	make run_ResizableArray; make run_Stack; make run_Queue; make run_Deque

run_ResizableArray:
	cd src/ResizableArray; java ResizableArraySimulation 15 5000

run_Stack:
	cd src/Stack; java StackSimulation 15 1000000

run_Queue:
	cd src/Queue; java QueueSimulation 60 1000000

run_Deque:
	cd src/Deque; java DequeSimulation 60 100000


# Tools
clean:
	rm -rf src/*/*.class; rm -rf *.zip