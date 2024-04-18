JFLAGS = -cp
JC = javac
JVM= java 
runKvServer: Server.class
	$(JVM) $(JFLAGS) .:jackson-core-2.13.3.jar:jackson-annotations-2.13.3.jar:jackson-databind-2.13.3.jar Server $(port) $(cache_path)

Server.class: Server.java ClientHandle.java LookUpCache.java
	$(JC) $(JFLAGS) .:jackson-core-2.13.3.jar:jackson-annotations-2.13.3.jar:jackson-databind-2.13.3.jar Server.java

clean:
	rm *.class 