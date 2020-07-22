package actions;

import java.io.Serializable;

public abstract class Action implements Serializable {

    protected ActionType _actionType;

    public Action(){
        setActionType();
    }

    protected abstract void setActionType();

    public ActionType getActionType(){
        return _actionType;
    }
}
