package actions;

import node.HttpRequest;
import node.NodeInfo;
import utils.AESUtils;

import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

public class ServiceRequestAction extends Action {

    private AESUtils.AESEncryptionResult _encryptedServerRequest;
    private NodeInfo _prevNode;

    public ServiceRequestAction(AESUtils.AESEncryptionResult encryptedServerRequest, UUID sessionId, NodeInfo prevNode) {
        _encryptedServerRequest = encryptedServerRequest;
        _prevNode = prevNode;
        _sessionId = sessionId;
    }

    public static ServiceRequestAction of(AESUtils.AESEncryptionResult encryptedServerRequest, UUID sessionId, NodeInfo nodeInfo){
        return new ServiceRequestAction(encryptedServerRequest, sessionId, nodeInfo);
    }

    @Override
    protected void setActionType() {
        _actionType = ActionType.SERVICE;
    }

    public AESUtils.AESEncryptionResult getEncryptedServerRequest() {
        return _encryptedServerRequest;
    }

    public NodeInfo getPrevNode() {
        return _prevNode;
    }

    public static class ServerRequest implements Serializable {
        private HttpRequest _host;

        public ServerRequest(HttpRequest host) {
            _host = host;
        }

        public HttpRequest getRequest() {
            return _host;
        }
    }

}

