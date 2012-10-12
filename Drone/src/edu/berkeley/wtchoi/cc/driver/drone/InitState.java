package edu.berkeley.wtchoi.cc.driver.drone;

import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 5:56 PM
 * To change this template use File | Settings | File Templates.
 */
class InitState extends AbstractState {
    public InitState(SupervisorImp s){ super(s); }
    public void work(){
        Log.d("wtchoi", "Init State");
    }

    public AbstractState next(){ return new UnstableState(s); }
}
