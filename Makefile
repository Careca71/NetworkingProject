JC = javac
LIBS = -cp jackson-databind-2.9.5.jar:jackson-core-2.9.5.jar:jackson-annotations-2.9.0.jar
JE = java

.PHONY: server client jarserver jarclient
default:
		$(JC) $(LIBS):  ./main/java/Interfaces/*.java ./main/java/Classes/*.java ./main/java/Utils/*.java ./main/java/Server/*.java ./main/java/Client/*.java

server:
	        java -cp jackson-databind-2.9.5.jar:jackson-core-2.9.5.jar:jackson-annotations-2.9.0.jar: main.java.Server.MainClass main/java/Server/Server_Configfile.txt

client:
		java -cp jackson-databind-2.9.5.jar:jackson-core-2.9.5.jar:jackson-annotations-2.9.0.jar: main.java.Client.Mainclient  ./main/java/Client/Client_ConfigFIle.txt

jarserver:
		java -cp  jackson-databind-2.9.5.jar:jackson-core-2.9.5.jar:jackson-annotation -jar  winsomeServer_jar.jar ./main/java/Server/Server_Configfile.txt

jarclient:
		java -cp  jackson-databind-2.9.5.jar:jackson-core-2.9.5.jar:jackson-annotation -jar  winsomeClient_jar.jar  main/java/Client/Client_ConfigFIle.txt

clean:
		-rm -f main/java/Client/*.class main/java/Classes/*.class main/java/Interfaces/*.class main/java/Utils/*.class main/java/Server/*.class *AND*
