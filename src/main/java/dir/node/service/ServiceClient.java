package dir.node.service;

import actions.RoutingFromServiceAction;
import actions.ServiceRequestAction.*;

import java.util.UUID;

public interface ServiceClient {

    RoutingFromServiceAction createRequest(ServerRequest clientRequest, byte[] key, UUID sessionId)
                                            throws Throwable;
}
