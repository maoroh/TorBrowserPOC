package client;

import actions.*;
import common.Configuration;
import dir.DirectoryResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.AESUtils;
import utils.AESUtils.*;
import utils.DHUtils;
import node.NodeInfo;

import javax.crypto.KeyAgreement;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;

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
        UUID sessionId = UUID.randomUUID();
        DHAction dhAction = DHAction.of(clientPublicKeyEncoded.array(), sessionId);
        nodeOutputStream.writeObject(dhAction);
        DHAction dhActionRec = (DHAction) nodeInputStream.readObject();

        logger.info("Client : Received Public Key from node ...");
        PublicKey nodePublicKey = DHUtils.getPublicKeyFromEncodedBytes(dhActionRec.getPublicKey());
        keyAgreement.doPhase(nodePublicKey, true);
        ByteBuffer secretSymmetricKey = ByteBuffer.wrap(keyAgreement.generateSecret());
        nodeSocket.close();
        logger.info("Client Symmetric key : " + secretSymmetricKey.get(0));
        nodeInfo.setSessionId(sessionId);
        _nodeKeys.put(nodeInfo, secretSymmetricKey);
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
        List<NodeInfo> nodes = getRoutersFromDir(3);
        for(NodeInfo nodeInfo : nodes){
           replaceKey(nodeInfo);
        }
    }

    public void sendAnonymousRequest(ClientRequest request) throws Throwable {
        Iterator<NodeInfo> iterator = _nodeKeys.keySet().iterator();
        List<NodeInfo> nodes = Arrays.asList(iterator.next(),iterator.next());
        Socket nodeSocket = new Socket(nodes.get(0).getHost(),nodes.get(0).getPort());
        ObjectOutputStream nodeOutputStream = new ObjectOutputStream(nodeSocket.getOutputStream());
        Action routingAction = createUnionEncryptedClientRequest(nodes, 0);
        nodeOutputStream.writeObject(routingAction);
    }


    public AESEncryptionResult encryptObject(ByteBuffer nodeKey , Object data) throws Throwable {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(data);
        return AESUtils.AESEncrypt(bos.toByteArray(), nodeKey.array());
    }


    public Action createEncryptedRoutingAction(ByteBuffer nodeKey, NodeInfo currentNode, NodeInfo nextNodeInfo, Action nextNodeAction) throws Throwable {
        return RoutingAction.of(encryptObject(nodeKey, RoutingAction.NodeData.of(nextNodeInfo,nextNodeAction)),currentNode.getSessionId());
    }

    public Action createEncryptedServiceAction(ByteBuffer nodeKey, NodeInfo nodeInfo) throws Throwable {
        return ServiceAction.of(encryptObject(nodeKey, new ServiceAction.ServerRequest("localhost",8000,"Maor")),nodeInfo.getSessionId());
    }

    public Action createUnionEncryptedClientRequest(List<NodeInfo> nodes, int i) throws Throwable {
        if(i == nodes.size() - 1){
            return createEncryptedServiceAction(_nodeKeys.get(nodes.get(i)),nodes.get(i));
        }
        return createEncryptedRoutingAction(_nodeKeys.get(nodes.get(i)), nodes.get(i),nodes.get(i + 1), createUnionEncryptedClientRequest(nodes, i + 1));
    }

}
