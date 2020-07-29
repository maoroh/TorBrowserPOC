package actions;

import utils.AESUtils;

import java.io.Serializable;
import java.util.UUID;

public class ServiceAction extends Action {

    private AESUtils.AESEncryptionResult _encryptedServerRequest;

    public ServiceAction(AESUtils.AESEncryptionResult encryptedServerRequest,UUID sessionId) {
        _encryptedServerRequest = encryptedServerRequest;
        _sessionId = sessionId;
    }

    public static ServiceAction of(AESUtils.AESEncryptionResult encryptedServerRequest, UUID sessionId){
        return new ServiceAction(encryptedServerRequest, sessionId);
    }

    @Override
    protected void setActionType() {
        _actionType = ActionType.SERVICE;
    }

    public AESUtils.AESEncryptionResult getEncryptedServerRequest() {
        return _encryptedServerRequest;
    }

    public static class ServerRequest implements Serializable {
        private String _host;
        private int _port;
        private String _getParam;

        public ServerRequest(String host, int port, String getParam) {
            _host = host;
            _port = port;
            _getParam = getParam;
        }
    }

}

