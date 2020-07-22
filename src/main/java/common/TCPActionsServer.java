package common;
import actions.Action;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public abstract class TCPActionsServer {

    protected ServerSocket _serverSocket;
    protected int _port;

    public TCPActionsServer(int port) throws IOException {

        _serverSocket = new ServerSocket(port);
        _port = port;
    }

    public void startListening() throws IOException {
        init();
        System.out.println("Server " + this.getClass().getSimpleName() + " start listening on port " + _port);
        Executors.newSingleThreadExecutor().submit(new TCPListener());
    }

    protected abstract void init() throws IOException;


    protected class TCPListener implements Callable{

        @Override
        public Object call()  {

            while(true){

                try {
                    Socket clientSocket = _serverSocket.accept();
                    System.out.println("Client connected to server ...");
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
                    Action action = (Action) objectInputStream.readObject();
                    processAction(action ,objectOutputStream);
                    objectOutputStream.close();
                    objectInputStream.close();
                    clientSocket.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }
        }
    }

    protected abstract void processAction(Action action , ObjectOutputStream objectOutputStream) throws Throwable;


}
