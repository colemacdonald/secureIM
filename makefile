all:
	javac GeneralHelper.java
	javac SecurityHelper.java
	javac UserInput.java
	javac WriteSocketThread.java
	javac ReadSocketThread.java
	javac Server.java
	javac Client.java
clean:
	rm *.class