package actions;

import dir.node.NodeInfo;
import utils.AESUtils.*;

import java.io.Serializable;
import java.util.UUID;


public class RoutingFromClientAction extends Action {

    private AESEncryptionResult _encryptedNodeData;

    public RoutingFromClientAction(AESEncryptionResult encryptedNodeData, UUID sessionId) {
        _encryptedNodeData = encryptedNodeData;
        _sessionId = sessionId;
    }

    public static RoutingFromClientAction of(AESEncryptionResult encryptedNodeData, UUID sessionId){
        return new RoutingFromClientAction(encryptedNodeData, sessionId);
    }

    @Override
    protected void setActionType() {
        _actionType = ActionType.ROUTING;
    }

    public AESEncryptionResult getEncryptedNodeData() {
        return _encryptedNodeData;
    }


    public static class NodeData implements Serializable {

        private NodeInfo _prevNode;
        private NodeInfo _nextNode;
        private Action _nextLayerAction;

        public NodeData(NodeInfo prevNode, NodeInfo nextNode, Action nextLayerAction) {
            _prevNode = prevNode;
            _nextNode = nextNode;
            _nextLayerAction = nextLayerAction;
        }

        public static NodeData of(NodeInfo prevNode, NodeInfo nextNode, Action nextLayerAction){
            return new NodeData(prevNode,nextNode,nextLayerAction);
        }

        public NodeInfo getNextNode() {
            return _nextNode;
        }

        public Action getNextLayerAction() {
            return _nextLayerAction;
        }

        public NodeInfo getPrevNode() {
            return _prevNode;
        }
    }
}
