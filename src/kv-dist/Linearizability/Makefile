JFLAGS = -cp
JC = javac
JVM= java 

runLinear: Main.class
	$(JVM) Main $(partitions)
Main.class: *.java
	$(JC) *.java
clean:
	rm *.class
