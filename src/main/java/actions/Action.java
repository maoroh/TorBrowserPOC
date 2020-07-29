package actions;

import java.io.Serializable;
import java.util.UUID;

public abstract class Action implements Serializable {

    protected ActionType _actionType;
    protected UUID _sessionId;


    public Action(){
        setActionType();
    }

    protected abstract void setActionType();

    public UUID getSessionId(){
        return _sessionId;
    }

    public ActionType getActionType(){
        return _actionType;
    }

}
