package edu.berkeley.wtchoi.cc.driver.drone;

import android.util.Log;
import edu.berkeley.wtchoi.cc.driver.DriverPacket;
import edu.berkeley.wtchoi.cc.learnerImp.ctree.TransitionInfo;

import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 5:55 PM
 * Current Implementation is a temporary.
 */
public class UnstableState extends AbstractState {
    private AbstractState next;
    private int prevsize;
    private boolean communicationFlag = true;
    private int stablecount = 0;

    public UnstableState(SupervisorImp s, boolean cf){
        super(s);
        prevsize = 0;
        this.communicationFlag = cf;
        next = this;
    }

    public UnstableState(SupervisorImp s){
        super(s);
        prevsize = 0;
        next = this;
    }

    //Inspect whether event handler execution is finished or not
    public void work(){
        Log.d("wtchoi", "Unstable State");

        LinkedList<SLog> sStack = s.mainStack;
        LinkedList<SLog> sList = s.mainList;

        //We assume that lock all function accessing s.sStack acquire lock of s.sList.
        synchronized (s.sLists){
            if(prevsize == sList.size() && sStack.size() == 0){
                if(stablecount == s.STABLECOUNT){
                    s.transitionInfo = new TransitionInfo();
                    s.transitionInfo.setDidNothing(sList.isEmpty());

                    sList.clear();
                    DriverPacket p = DriverPacket.getAckStable();
                    s.channel.sendPacket(p);

                    next = new StableState(s);
                }
                else{
                    prevsize = sList.size();
                    stablecount++;
                }
            }
            else{
                prevsize = sList.size();
                stablecount = 0;
            }
        }
    }

    public void onStop(){
        DriverPacket p = DriverPacket.getAckStop();
        s.channel.sendPacket(p);
        s.closeApplication();
    }

    public AbstractState next(){ return next; }
}