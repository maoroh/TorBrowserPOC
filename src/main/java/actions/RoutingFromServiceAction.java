package actions;

import utils.AESUtils;

import java.io.Serializable;
import java.util.UUID;

public class RoutingFromServiceAction extends Action {

    private AESUtils.AESEncryptionResult _encryptedServerResponse;

    public RoutingFromServiceAction(AESUtils.AESEncryptionResult encryptedServerResponse, UUID sessionId) {
        _encryptedServerResponse = encryptedServerResponse;
        _sessionId = sessionId;
    }

    public static RoutingFromServiceAction of(AESUtils.AESEncryptionResult encryptedServerResponse, UUID sessionId){
        return new RoutingFromServiceAction(encryptedServerResponse, sessionId);
    }

    @Override
    protected void setActionType() {
        _actionType = ActionType.ROUTING_FROM_SERVICE;
    }

    public AESUtils.AESEncryptionResult getEncryptedServerResponse() {
        return _encryptedServerResponse;
    }

    public static class PrevNodeData implements Serializable {

        private Action _prevLayer;

        public PrevNodeData(Action prevLayer){
            _prevLayer = prevLayer;
        }

        public Action getPrevLayer() {
            return _prevLayer;
        }
    }
}
