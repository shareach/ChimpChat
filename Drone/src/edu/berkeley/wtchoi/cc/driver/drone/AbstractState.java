package edu.berkeley.wtchoi.cc.driver.drone;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
abstract class AbstractState {
    protected SupervisorImp s;
    public AbstractState(SupervisorImp s){ this.s = s; }

    //work must be synchronized using s.sLists
    abstract public void work();
    abstract public AbstractState next();
    public void onStop(){}
}