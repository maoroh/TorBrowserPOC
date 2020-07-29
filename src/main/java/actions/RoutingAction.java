package actions;

import node.NodeInfo;
import utils.AESUtils.*;

import java.io.Serializable;
import java.util.UUID;


public class RoutingAction extends Action {

    private AESEncryptionResult _encryptedNodeData;

    public RoutingAction(AESEncryptionResult encryptedNodeData, UUID sessionId) {
        _encryptedNodeData = encryptedNodeData;
        _sessionId = sessionId;
    }

    public static RoutingAction of(AESEncryptionResult encryptedNodeData, UUID sessionId){
        return new RoutingAction(encryptedNodeData, sessionId);
    }

    @Override
    protected void setActionType() {
        _actionType = ActionType.ROUTING;
    }

    public AESEncryptionResult getEncryptedNodeData() {
        return _encryptedNodeData;
    }


    public static class NodeData implements Serializable {

        private NodeInfo _nextNode;
        private Action _nextLayerAction;

        public NodeData(NodeInfo encryptedData, Action nextLayerAction) {
            _nextNode = encryptedData;
            _nextLayerAction = nextLayerAction;
        }

        public static NodeData of(NodeInfo aesEncryptionResult, Action nextLayerAction){
            return new NodeData(aesEncryptionResult,nextLayerAction);
        }

        public NodeInfo getNextNode() {
            return _nextNode;
        }

        public Action getNextLayerAction() {
            return _nextLayerAction;
        }
    }
}
