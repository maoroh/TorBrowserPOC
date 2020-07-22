package actions;

import client.ClientRequest;
import router.NodeInfo;

public class RoutingAction extends Action {

    private ClientRequest _clientRequest;
    private NodeInfo _nextRouter;

    public RoutingAction(ClientRequest clientRequest, NodeInfo nextRouter) {
        _clientRequest = clientRequest;
        _nextRouter = nextRouter;
    }

    public static RoutingAction of(ClientRequest clientRequest , NodeInfo nextRouter){
        return new RoutingAction(clientRequest, nextRouter);
    }

    @Override
    protected void setActionType() {
        _actionType = ActionType.ROUTING;
    }

    public ClientRequest getClientRequest() {
        return _clientRequest;
    }

    public void setClientRequest(ClientRequest clientRequest) {
        _clientRequest = clientRequest;
    }

    public NodeInfo getNextRouter() {
        return _nextRouter;
    }

    public void setNextRouter(NodeInfo nextRouter) {
        _nextRouter = nextRouter;
    }
}
