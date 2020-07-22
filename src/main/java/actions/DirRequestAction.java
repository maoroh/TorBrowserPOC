package actions;

public class DirRequestAction extends Action {
    private int _hops;

    public DirRequestAction(int hops){
        _hops = hops;
    }

    @Override
    protected void setActionType() {
        _actionType = ActionType.DIR_REQ;
    }

    public int getHops(){
        return _hops;
    }
}
