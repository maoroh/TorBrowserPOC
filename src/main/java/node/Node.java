package node;

import actions.*;
import common.TCPActionsServer;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.AESUtils;
import utils.DHUtils;

import javax.crypto.KeyAgreement;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Node extends TCPActionsServer {

    private final Logger logger = LogManager.getLogger();
    private ConcurrentHashMap<UUID,Object> _locks;
    private ConcurrentHashMap<UUID,NodeDetails> _sidMap;
    private ConcurrentHashMap<UUID,Action> _clientResponses;

    public Node(int port) throws IOException {
        super(port);
        _sidMap = new ConcurrentHashMap<>();
        _clientResponses = new ConcurrentHashMap<>();
        _locks = new ConcurrentHashMap<>();
    }

    @Override
    protected void init() {
        return;
    }

    @Override
    protected void processAction(Action action, ObjectOutputStream objectOutputStream) throws Throwable {

        if(action.getActionType().equals(ActionType.DH)){

            replaceKeysWithClient((DHAction)action,objectOutputStream);

        } else if(action.getActionType().equals(ActionType.ROUTING_FROM_CLIENT)){


                //Decrypt layer of encryption and route to next node
                RoutingFromClientAction.NodeData nodeData = descryptAES(action.getSessionId(),((RoutingFromClientAction)action).getEncryptedNodeData());
                updateNodeInfo(action.getSessionId(), nodeData.getPrevNode());
                routeToNextNode(nodeData);

                //First Node : Socket need to be open
                if(_sidMap.get(action.getSessionId()).getNodeInfo() == null){

                        synchronized (_locks.get(action.getSessionId())) {
                            while (_clientResponses.get(action.getSessionId()) == null) {
                                System.out.println(Thread.currentThread().getName() + " is waiting...");
                                _locks.get(action.getSessionId()).wait();
                                System.out.println(Thread.currentThread().getName() + " is released...");
                            }

                            objectOutputStream.writeObject(_clientResponses.get(action.getSessionId()));
                        }
                }

        } else if(action.getActionType().equals(ActionType.ROUTING_FROM_SERVICE)){
            //Encrypt with the symmetric key session id
            RoutingFromServiceAction routingAction = RoutingFromServiceAction.of(encryptObject(action), action.getSessionId());
            if(_sidMap.get(action.getSessionId()).getNodeInfo() != null){
                routeToPrevNode(action.getSessionId(), routingAction);
            } else {
                synchronized (_locks.get(action.getSessionId())){
                    _clientResponses.put(action.getSessionId(), routingAction);
                    _locks.get(action.getSessionId()).notifyAll();
                }

            }

        } else if(action.getActionType().equals(ActionType.SERVICE)) {
            ServiceRequestAction.ServerRequest decryptedServerRequest = descryptAES(action.getSessionId(), ((ServiceRequestAction) action).getEncryptedServerRequest());
            updateNodeInfo(action.getSessionId(), ((ServiceRequestAction) action).getPrevNode());
            RoutingFromServiceAction serviceResponseAction = sendHttpRequest(action.getSessionId(), decryptedServerRequest);
            routeToPrevNode(action.getSessionId(), serviceResponseAction);
        }

    }

    private void routeToPrevNode(UUID sessionId,RoutingFromServiceAction serviceResponseAction) throws IOException {
        NodeInfo nodeInfo = _sidMap.get(sessionId).getNodeInfo();
        Socket socket = new Socket(nodeInfo.getHost(), nodeInfo.getPort());
        new ObjectOutputStream(socket.getOutputStream()).writeObject(serviceResponseAction);

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

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            URIBuilder builder = new URIBuilder(decryptedServerRequest.getRequest().getUrl());

            HttpGet httpGet = new HttpGet(builder.build());
            final RequestConfig params = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();
            httpGet.setConfig(params);
            decryptedServerRequest.getRequest().getHeaders().forEach(h ->
                httpGet.setHeader(h.getName(),h.getValue()));

            CloseableHttpResponse httpRealResponse = httpClient.execute(httpGet);
            byte [] data ;
            if(httpRealResponse.getEntity() != null){
                data = EntityUtils.toByteArray(httpRealResponse.getEntity());
            } else {
                data = new byte []{};
            }

            HttpResponse httpResponse = new HttpResponse(data,Arrays.stream(httpRealResponse.getAllHeaders()).map(header ->
                    new HttpResponse.Header(header.getName(),header.getValue()))
                    .collect(Collectors.toList()));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(bos);
            objectOutputStream.writeObject(httpResponse);
            httpRealResponse.close();
            httpClient.close();
            return RoutingFromServiceAction.of(AESUtils
                    .AESEncrypt(bos.toByteArray(),
                            _sidMap.get(sessionId).getKey().array()),sessionId);
        } catch(Throwable e){
            return RoutingFromServiceAction.of(AESUtils
                    .AESEncrypt("Error".getBytes(),
                            _sidMap.get(sessionId).getKey().array()),sessionId);
        }

    }

    private void routeToNextNode(RoutingFromClientAction.NodeData nodeData) throws Throwable {
        Socket nodeSocket = new Socket(nodeData.getNextNode().getHost(),nodeData.getNextNode().getPort());
        new ObjectOutputStream(nodeSocket.getOutputStream()).writeObject(nodeData.getNextLayerAction());

    }


    private <T> T descryptAES(UUID clientIdentifier, AESUtils.AESEncryptionResult aesEncryptionResult) throws Throwable {
        byte [] decryptedLayer = AESUtils.AESDecrypt(aesEncryptionResult,_sidMap.get(clientIdentifier).getKey().array());
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
        _sidMap.put(action.getSessionId(),NodeDetails.of(secretSymmetricKey));
        _locks.put(action.getSessionId(),new Object());
        logger.info("Server symmetric key : " + secretSymmetricKey.array()[0]);
    }


}
