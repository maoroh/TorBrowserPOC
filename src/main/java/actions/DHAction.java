package actions;

public class DHAction extends Action {

    private byte [] _publicKey;

    public DHAction(byte[] publicKey) {
        _publicKey = publicKey;
    }

    public static DHAction of(byte [] publicKey){
        return new DHAction(publicKey);
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
}
