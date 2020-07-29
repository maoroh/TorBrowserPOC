package node;
import actions.*;
import common.TCPActionsServer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.AESUtils;
import utils.DHUtils;

import javax.crypto.KeyAgreement;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Node extends TCPActionsServer {

    private final Logger logger = LogManager.getLogger();
    private ConcurrentHashMap<UUID,ByteBuffer> _clientKeys;

    public Node(int port) throws IOException {
        super(port);
        _clientKeys = new ConcurrentHashMap<>();
    }

    @Override
    protected void init() {
        return;
    }

    @Override
    protected void processAction(Action action , ObjectOutputStream objectOutputStream) throws Throwable {
        if(action.getActionType().equals(ActionType.DH)){

            replaceKeysWithClient((DHAction)action,objectOutputStream);

        } else if(action.getActionType().equals(ActionType.ROUTING)){

            RoutingAction.NodeData nodeData = descryptAES(action.getSessionId(),((RoutingAction)action).getEncryptedNodeData());
            routeToNextNode(nodeData);

        } else if(action.getActionType().equals(ActionType.SERVICE)){

            ServiceAction.ServerRequest decryptedServerRequest = descryptAES(action.getSessionId(), ((ServiceAction)action).getEncryptedServerRequest());
            sendHttpRequest(decryptedServerRequest);

        }
    }

    private void sendHttpRequest(ServiceAction.ServerRequest decryptedServerRequest) throws Throwable {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        URIBuilder builder = new URIBuilder("http://localhost:8000/service");
        builder.setParameter("name", "Maor");
        HttpGet httpGet = new HttpGet(builder.build());
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        httpResponse.close();
        httpClient.close();
    }

    private void routeToNextNode(RoutingAction.NodeData nodeData) throws Throwable {
        Socket nodeSocket = new Socket(nodeData.getNextNode().getHost(),nodeData.getNextNode().getPort());
        new ObjectOutputStream(nodeSocket.getOutputStream()).writeObject(nodeData.getNextLayerAction());

    }


    private <T> T descryptAES(UUID clientIdentifier, AESUtils.AESEncryptionResult aesEncryptionResult) throws Throwable {
        byte [] decryptedLayer = AESUtils.AESDecrypt(aesEncryptionResult,_clientKeys.get(clientIdentifier).array());
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(decryptedLayer));
        return (T) objectInputStream.readObject();
    }


    private void replaceKeysWithClient(DHAction action,ObjectOutputStream objectOutputStream) throws Throwable {

        KeyPair keyPair = DHUtils.generateKeyPair(1024);
        DHAction dhAction = DHAction.of(keyPair.getPublic().getEncoded());
        objectOutputStream.writeObject(dhAction);
        KeyAgreement keyAgreement = DHUtils.createKeyAgreement(keyPair);
        PublicKey clientPublicKey = DHUtils.getPublicKeyFromEncodedBytes(action.getPublicKey());
        keyAgreement.doPhase(clientPublicKey, true);
        ByteBuffer secretSymmetricKey = ByteBuffer.wrap(keyAgreement.generateSecret());
        _clientKeys.put(action.getClientIdentifier(),secretSymmetricKey);
        logger.info("Server symmetric key : " + secretSymmetricKey.array()[0]);
    }


}
