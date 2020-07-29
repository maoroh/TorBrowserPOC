package main;
import client.Client;
import dir.Directory;
import service.HTTPService;

public class Main {
    public static void main(String[] args) throws Throwable {

        HTTPService HTTPService = new HTTPService();
        HTTPService.startServer();
        Directory dirServer = new Directory();
        dirServer.startListening();
        Client client = new Client();
        client.replaceKeysWithNodes();
        client.sendAnonymousRequest(null);
    }
}
