all:
	javac Helper.java
	javac InputThread.java
	javac Server.java
	javac Client.java
clean:
	rm *.class