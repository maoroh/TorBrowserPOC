package main;
import client.Client;
import dir.Directory;

public class Main {
    public static void main(String[] args) throws Throwable {

        Directory dirServer = new Directory(30000);
        dirServer.startListening();
        Client client = new Client();
        client.replaceKeysWithNodes();
    }
}
