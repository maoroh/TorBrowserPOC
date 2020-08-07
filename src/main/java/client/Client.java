package client;

import actions.*;
import common.Configuration;
import dir.DirectoryResponse;
import node.HttpRequest;
import node.HttpResponse;
import node.NodeInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.AESUtils;
import utils.AESUtils.AESEncryptionResult;
import utils.DHUtils;

import javax.crypto.KeyAgreement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;

public class Client {

    private final Logger logger = LogManager.getLogger();

    private Map<NodeInfo, ByteBuffer> _nodeKeys;

    public Client() {
        _nodeKeys = new HashMap<>();
    }


    public ByteBuffer replaceKey(NodeInfo nodeInfo, UUID sessionId) throws Throwable {

        Socket nodeSocket = new Socket(nodeInfo.getHost(), nodeInfo.getPort());
        ObjectOutputStream nodeOutputStream = new ObjectOutputStream(nodeSocket.getOutputStream());
        ObjectInputStream nodeInputStream = new ObjectInputStream(nodeSocket.getInputStream());
        logger.info("Client : Generate DH keypair ...");

        KeyPair clientKeyPair = DHUtils.generateKeyPair(1024);
        KeyAgreement keyAgreement = DHUtils.createKeyAgreement(clientKeyPair);
        ByteBuffer clientPublicKeyEncoded = ByteBuffer.wrap(clientKeyPair.getPublic().getEncoded());
        DHAction dhAction = DHAction.of(clientPublicKeyEncoded.array(), sessionId);
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
        Socket dirSocket = new Socket(Configuration.getConfig().getDirectoryServerConfig().getHost(),
                Configuration.getConfig().getDirectoryServerConfig().getPort());
        ObjectOutputStream dirOutputStream = new ObjectOutputStream(dirSocket.getOutputStream());
        ObjectInputStream dirInputStream = new ObjectInputStream(dirSocket.getInputStream());
        dirOutputStream.writeObject(new DirRequestAction(hops));
        DirectoryResponse directoryResponse = (DirectoryResponse) dirInputStream.readObject();
        return directoryResponse.getNodes();
    }

    public void replaceKeysWithNodes() throws Throwable {
        UUID sessionId = UUID.randomUUID();
        List<NodeInfo> nodes = getRoutersFromDir(3);
        for (NodeInfo nodeInfo : nodes) {
            nodeInfo.setSessionId(sessionId);
            _nodeKeys.put(nodeInfo, replaceKey(nodeInfo, sessionId));
        }
    }

    public HttpResponse sendAnonymousRequest(HttpRequest req) throws Throwable {
        replaceKeysWithNodes();
        Iterator<NodeInfo> iterator = _nodeKeys.keySet().iterator();
        List<NodeInfo> nodes = Arrays.asList(iterator.next(), iterator.next(), iterator.next());
        Socket nodeSocket = new Socket(nodes.get(0).getHost(), nodes.get(0).getPort());
        ObjectOutputStream nodeOutputStream = new ObjectOutputStream(nodeSocket.getOutputStream());
        ObjectInputStream nodeInputStream = new ObjectInputStream(nodeSocket.getInputStream());
        Action routingAction = createUnionEncryptedClientRequest(req, nodes, 0);
        nodeOutputStream.writeObject(routingAction);
        RoutingFromServiceAction routingFromServiceAction = (RoutingFromServiceAction) nodeInputStream.readObject();
        Iterator<ByteBuffer> keysIterator = _nodeKeys.values().iterator();
        while (keysIterator.hasNext()) {
            byte[] currentKey = keysIterator.next().array();
            byte[] decryptedLayer = AESUtils.AESDecrypt(routingFromServiceAction.getEncryptedServerResponse(), currentKey);
            if (keysIterator.hasNext()) {
                ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(decryptedLayer));
                routingFromServiceAction = (RoutingFromServiceAction) ((RoutingFromServiceAction.PrevNodeData) objectInputStream.readObject()).getPrevLayer();
            } else {
                //Get HTTP
                ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(decryptedLayer));
                return (HttpResponse) objectInputStream.readObject();
            }
        }

        return null;

    }


    public AESEncryptionResult encryptObject(ByteBuffer nodeKey, Object data) throws Throwable {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(data);
        return AESUtils.AESEncrypt(bos.toByteArray(), nodeKey.array());
    }


    public Action createEncryptedRoutingAction(ByteBuffer nodeKey, NodeInfo prevNode, NodeInfo currentNode, NodeInfo nextNodeInfo, Action nextNodeAction) throws Throwable {
        return RoutingFromClientAction.of(encryptObject(nodeKey, RoutingFromClientAction.NodeData.of(prevNode, nextNodeInfo, nextNodeAction)), currentNode.getSessionId());
    }

    public Action createEncryptedServiceAction(HttpRequest req, ByteBuffer nodeKey, NodeInfo nodeInfo, NodeInfo prevNode) throws Throwable {
        return ServiceRequestAction.of(encryptObject(nodeKey, new ServiceRequestAction.ServerRequest(req)), nodeInfo.getSessionId(), prevNode);
    }

    public Action createUnionEncryptedClientRequest(HttpRequest req, List<NodeInfo> nodes, int i) throws Throwable {
        if (i == nodes.size() - 1) {
            return createEncryptedServiceAction(req, _nodeKeys.get(nodes.get(i)), nodes.get(i), nodes.get(i - 1));
        }
        return createEncryptedRoutingAction(_nodeKeys.get(nodes.get(i)), i != 0 ? nodes.get(i - 1) : null, nodes.get(i), nodes.get(i + 1), createUnionEncryptedClientRequest(req, nodes, i + 1));
    }

}
