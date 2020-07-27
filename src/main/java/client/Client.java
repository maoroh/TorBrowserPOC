package client;

import actions.DHAction;
import actions.DirRequestAction;
import actions.RoutingAction;
import dir.DirectoryResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.DHUtils;
import node.NodeInfo;

import javax.crypto.KeyAgreement;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client {

    private final Logger logger = LogManager.getLogger();

    private Map<NodeInfo, ByteBuffer> _nodeKeys;

    public Client(){
        _nodeKeys = new HashMap<>();
    }

    public ByteBuffer replaceKey(NodeInfo nodeInfo) throws Throwable {

        Socket nodeSocket = new Socket(nodeInfo.getHost(), nodeInfo.getPort());
        ObjectOutputStream nodeOutputStream = new ObjectOutputStream(nodeSocket.getOutputStream());
        ObjectInputStream nodeInputStream = new ObjectInputStream(nodeSocket.getInputStream());
        logger.info("Client : Generate DH keypair ...");

        KeyPair clientKeyPair = DHUtils.generateKeyPair(1024);
        KeyAgreement keyAgreement = DHUtils.createKeyAgreement(clientKeyPair);
        ByteBuffer clientPublicKeyEncoded = ByteBuffer.wrap(clientKeyPair.getPublic().getEncoded());

        DHAction dhAction = DHAction.of(clientPublicKeyEncoded.array());
        nodeOutputStream.writeObject(dhAction);
        DHAction dhActionRec = (DHAction) nodeInputStream.readObject();

        logger.info("Client : Received Public Key from node ...");
        PublicKey nodePublicKey = DHUtils.getPublicKeyFromEncodedBytes(dhActionRec.getPublicKey());
        keyAgreement.doPhase(nodePublicKey, true);
        ByteBuffer secretSymmetricKey = ByteBuffer.wrap(keyAgreement.generateSecret());
        nodeSocket.close();
        logger.info("Client Symmetric key : " + secretSymmetricKey.get(0));
        return secretSymmetricKey;
    }

    public List<NodeInfo> getRoutersFromDir(int hops) throws Throwable {
        Socket dirSocket = new Socket("localhost", 30000);
        ObjectOutputStream dirOutputStream = new ObjectOutputStream(dirSocket.getOutputStream());
        ObjectInputStream dirInputStream = new ObjectInputStream(dirSocket.getInputStream());
        dirOutputStream.writeObject(new DirRequestAction(3));
        DirectoryResponse directoryResponse = (DirectoryResponse) dirInputStream.readObject();
        return directoryResponse.getNodes();
    }

    public void replaceKeysWithNodes() throws Throwable {
        List<NodeInfo> nodeInfos = getRoutersFromDir(3);
        for(NodeInfo nodeInfo : nodeInfos){
            ByteBuffer symmetricKey = replaceKey(nodeInfo);
            _nodeKeys.put(nodeInfo, symmetricKey);
        }
        logger.info("Finishing Replacing keys...");
    }

    public void sendAnonymousRequest(ClientRequest request){
        RoutingAction routingAction = RoutingAction.of(request, _nodeKeys.keySet().iterator().next());
    }

}
