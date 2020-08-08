package dir;

import dir.node.NodeInfo;

import java.io.Serializable;
import java.util.List;

public class DirectoryResponse implements Serializable {

    List<NodeInfo> _nodes;

    public DirectoryResponse(List<NodeInfo> nodes){
        _nodes = nodes;
    }

    public List<NodeInfo> getNodes(){
        return _nodes;
    }

}

