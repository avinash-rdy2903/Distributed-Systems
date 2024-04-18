JFLAGS = -cp
JC = javac
JVM= java 

runEventual: Main.class
	$(JVM) Main $(partitions)
Main.class: *.java
	$(JC) *.java
clean:
	rm *.class
