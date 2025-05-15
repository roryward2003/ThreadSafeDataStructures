# Compile
all:
	make ResizableArray; make Stack; make Queue

ResizableArray:
	cd src/ResizableArray; javac ResizableArraySimulation.java
	
Stack:
	cd src/Stack; javac StackSimulation.java

Queue:
	cd src/Queue; javac QueueSimulation.java

# Run simulations
run_ResizableArray:
	cd src/ResizableArray; java ResizableArraySimulation 15 5000

run_Stack:
	cd src/Stack; java StackSimulation 15 1000000

run_Queue:
	cd src/Queue; java QueueSimulation 50 1000000

# Tools
clean:
	rm -rf src/*/*.class; rm -rf *.zip

zip_preedit:
	zip -r concurrency_preedit.zip .

zip_da:
	zip -r concurrency_da_postedit.zip .

zip_sonnet:
	zip -r concurrency_sonnet_postedit.zip .

zip_rewrite:
	zip -r concurrency_rewrite.zip .