package dir.node;

import actions.*;
import dir.common.TCPActionsServer;
import dir.node.service.HttpServiceClient;
import dir.node.service.ServiceClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.AESUtils;
import utils.DHUtils;
import utils.SerializeUtils;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Node extends TCPActionsServer {

    private final Logger logger = LogManager.getLogger();
    private ServiceClient _httpClient;
    private ConcurrentHashMap<UUID,NodeDetails> _sidMap;

    public Node(int port) throws IOException {
        super(port);
        _httpClient = new HttpServiceClient();
        _sidMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void init() {
        return;
    }

    @Override
    protected void processAction(Action action, ObjectOutputStream objectOutputStream) throws Throwable {

        if(action.getActionType().equals(ActionType.DH)){

            replaceKeysWithClient((DHAction)action,objectOutputStream);

        } else if(action.getActionType().equals(ActionType.ROUTING)) {

            //Decrypt layer of encryption and route to next dir.node
            RoutingFromClientAction.NodeData nodeData = descryptAES(action.getSessionId(), ((RoutingFromClientAction) action).getEncryptedNodeData());
            updateNodeInfo(action.getSessionId(), nodeData.getPrevNode());
            ObjectInputStream objectInputStream = routeToNextNode(nodeData);
            //Waiting for response from node
            Action recAction = (Action) objectInputStream.readObject();

            //Response recieved from Node
            RoutingFromServiceAction routingAction = RoutingFromServiceAction.of(encryptObject(recAction), recAction.getSessionId());
            objectOutputStream.writeObject(routingAction);
            objectInputStream.close();

        } else if(action.getActionType().equals(ActionType.SERVICE)) {
            //Exit Node
            ServiceRequestAction.ServerRequest decryptedServerRequest = descryptAES(action.getSessionId(), ((ServiceRequestAction) action).getEncryptedServerRequest());
            updateNodeInfo(action.getSessionId(), ((ServiceRequestAction) action).getPrevNode());
            RoutingFromServiceAction serviceResponseAction = sendHttpRequest(action.getSessionId(), decryptedServerRequest);
            routeToPrevNode(serviceResponseAction, objectOutputStream);
        }

    }

    private void routeToPrevNode(RoutingFromServiceAction serviceResponseAction, ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeObject(serviceResponseAction);
    }

    private AESUtils.AESEncryptionResult encryptObject(Action action) throws Throwable {
        ByteBuffer symmetricKey = _sidMap.get(action.getSessionId()).getKey();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream actionStream = new ObjectOutputStream(bos);
        actionStream.writeObject(new RoutingFromServiceAction.PrevNodeData(action));
        return AESUtils.AESEncrypt(bos.toByteArray(), symmetricKey.array());
    }

    private void updateNodeInfo(UUID sessionId, NodeInfo prevNode) {
        NodeDetails nodeDetails = _sidMap.get(sessionId);
        nodeDetails.setNodeInfo(prevNode);
    }

    private RoutingFromServiceAction sendHttpRequest(UUID sessionId, ServiceRequestAction.ServerRequest decryptedServerRequest) throws Throwable {
       return _httpClient.createRequest(decryptedServerRequest,_sidMap.get(sessionId).getKey().array(),sessionId);
    }

    private ObjectInputStream routeToNextNode(RoutingFromClientAction.NodeData nodeData) throws Throwable {
        Socket nodeSocket = new Socket(nodeData.getNextNode().getHost(),nodeData.getNextNode().getPort());
        new ObjectOutputStream(nodeSocket.getOutputStream()).writeObject(nodeData.getNextLayerAction());
        return new ObjectInputStream(nodeSocket.getInputStream());
    }


    private <T> T descryptAES(UUID clientIdentifier, AESUtils.AESEncryptionResult aesEncryptionResult) throws Throwable {
        byte [] decryptedLayer = AESUtils.AESDecrypt(aesEncryptionResult,_sidMap.get(clientIdentifier).getKey().array());
        return SerializeUtils.deserializeObject(decryptedLayer);
    }


    private void replaceKeysWithClient(DHAction action,ObjectOutputStream objectOutputStream) throws Throwable {

        DHPublicKey dhParameterSpec = (DHPublicKey) DHUtils.getPublicKeyFromEncodedBytes(action.getPublicKey());
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
        keyPairGenerator.initialize(dhParameterSpec.getParams());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        DHAction dhAction = DHAction.of(keyPair.getPublic().getEncoded());
        objectOutputStream.writeObject(dhAction);
        KeyAgreement keyAgreement = DHUtils.createKeyAgreement(keyPair);
        PublicKey clientPublicKey = DHUtils.getPublicKeyFromEncodedBytes(action.getPublicKey());
        keyAgreement.doPhase(clientPublicKey, true);
        ByteBuffer secretSymmetricKey = ByteBuffer.wrap(keyAgreement.generateSecret());
        _sidMap.put(action.getSessionId(),NodeDetails.of(secretSymmetricKey));
        logger.info("Server symmetric key : " + secretSymmetricKey.array()[0]);
    }


}
