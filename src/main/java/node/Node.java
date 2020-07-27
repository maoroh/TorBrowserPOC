package node;
import actions.Action;
import actions.ActionType;
import actions.DHAction;
import common.TCPActionsServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.DHUtils;

import javax.crypto.KeyAgreement;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.security.*;

public class Node extends TCPActionsServer {

    private final Logger logger = LogManager.getLogger();

    public Node(int port) throws IOException {
        super(port);
    }

    @Override
    protected void init() throws IOException {
        return;
    }

    @Override
    protected void processAction(Action action , ObjectOutputStream objectOutputStream) throws Throwable {
        if(action.getActionType().equals(ActionType.DH)){
            replaceKeysWithClient((DHAction)action,objectOutputStream);
        } else if(action.getActionType().equals(ActionType.ROUTING)){
            //TODO:Routing
        }
    }


    private void replaceKeysWithClient(DHAction action,ObjectOutputStream objectOutputStream) throws Throwable {

        KeyPair keyPair = DHUtils.generateKeyPair(1024);
        DHAction dhAction = DHAction.of(keyPair.getPublic().getEncoded());
        objectOutputStream.writeObject(dhAction);
        KeyAgreement keyAgreement = DHUtils.createKeyAgreement(keyPair);
        PublicKey clientPublicKey = DHUtils.getPublicKeyFromEncodedBytes(action.getPublicKey());
        keyAgreement.doPhase(clientPublicKey, true);
        ByteBuffer secretSymmetricKey = ByteBuffer.wrap(keyAgreement.generateSecret());
        logger.info("Server symmetric key : " + secretSymmetricKey.array()[0]);
    }


}
