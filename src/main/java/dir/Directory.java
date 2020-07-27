package dir;
import actions.Action;
import actions.ActionType;
import actions.DirRequestAction;
import common.Configuration;
import common.TCPActionsServer;
import node.Node;
import node.NodeInfo;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Directory extends TCPActionsServer {

    private List<NodeInfo> _nodes;

    public Directory() throws IOException {
        super(Configuration.getConfig()
                .getDirectoryServerConfig()
                .getPort());
    }

    @Override
    protected void init() throws IOException {
        loadingNodesAndStartAll();
    }

    protected void loadingNodesAndStartAll() throws IOException {
        List<NodeInfo> nodes = Configuration.getConfig().getNodes();
        _nodes = nodes;
        for(NodeInfo nodeInfo : nodes){
            Node node = new Node(nodeInfo.getPort());
            node.startListening();
        }
    }

    @Override
    protected void processAction(Action action, ObjectOutputStream objectOutputStream) throws Throwable {

        if(action.getActionType().equals(ActionType.DIR_REQ)){
            int hops = ((DirRequestAction)action).getHops();
            DirectoryResponse directoryResponse = new DirectoryResponse(randomizeNodes(hops));
            objectOutputStream.writeObject(directoryResponse);
        }
        else{
            throw new IllegalArgumentException();
        }
    }

    public List<NodeInfo> randomizeNodes(int hops){

        List<NodeInfo> nodes = new ArrayList<>();
        for(int i = 0; i < hops; i++){
            int hopIndex = new Random().nextInt(_nodes.size());
            nodes.add(_nodes.get(hopIndex));
        }
        return nodes;
    }
}
