package node;

import java.nio.ByteBuffer;

public class NodeDetails {
    ByteBuffer _key;
    NodeInfo _nodeInfo;

    public NodeDetails(ByteBuffer key, NodeInfo nodeInfo) {
        _key = key;
        _nodeInfo = nodeInfo;
    }

    public NodeDetails(ByteBuffer key) {
        _key = key;
    }

    public static NodeDetails of(ByteBuffer key, NodeInfo nodeInfo){
        return new NodeDetails(key,nodeInfo);
    }

    public static NodeDetails of(ByteBuffer key){
        return new NodeDetails(key);
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this._nodeInfo = nodeInfo;
    }

    public ByteBuffer getKey() {
        return _key;
    }

    public NodeInfo getNodeInfo() {
        return _nodeInfo;
    }
}
