package actions;

import java.util.UUID;

public class DHAction extends Action {

    private byte [] _publicKey;

    public DHAction(byte[] publicKey, UUID clientIdentifier) {
        _publicKey = publicKey;
        _sessionId = clientIdentifier;
    }

    public static DHAction of(byte [] publicKey, UUID clientIdentifier){
        return new DHAction(publicKey, clientIdentifier);
    }

    public static DHAction of(byte [] publicKey){
        return new DHAction(publicKey, null);
    }

    public byte[] getPublicKey() {
        return _publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        _publicKey = publicKey;
    }

    @Override
    protected void setActionType() {
        _actionType = ActionType.DH;
    }

    public UUID getClientIdentifier() {
        return _sessionId;
    }
}
