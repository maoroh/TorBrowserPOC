package common;
import actions.Action;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class TCPActionsServer {

    private final Logger logger = LogManager.getLogger();
    protected ServerSocket _serverSocket;
    protected int _port;
    private Executor executor;

    public TCPActionsServer(int port) throws IOException {

        _serverSocket = new ServerSocket(port);
        _port = port;
    }

    public void startListening() throws IOException {
        init();
        logger.info("Server " + this.getClass().getSimpleName() + " start listening on port " + _port);
        Executors.newSingleThreadExecutor().submit(new TCPListener());
        executor = Executors.newFixedThreadPool(20);
    }

    protected abstract void init() throws IOException;


    protected class TCPListener implements Callable{

        @Override
        public Object call()  {

            while(true){

                try {
                    Socket clientSocket = _serverSocket.accept();
                    logger.info("Client connected to server ...");

                    executor.execute(() -> {
                        try {
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                            ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
                            Action action = (Action) objectInputStream.readObject();
                            processAction(action, objectOutputStream);
                            objectOutputStream.close();
                            objectInputStream.close();
                            clientSocket.close();
                        } catch (Throwable e) {
                                e.printStackTrace();
                            }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    protected abstract void processAction(Action action  , ObjectOutputStream objectOutputStream) throws Throwable;


}
