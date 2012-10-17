package edu.berkeley.wtchoi.cc.driver.drone;

import android.util.Log;
import edu.berkeley.wtchoi.cc.driver.DriverPacket;
import edu.berkeley.wtchoi.cc.learnerImp.ctree.TransitionInfo;
import edu.berkeley.wtchoi.collection.CVector;

import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;

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
    private int stablecount;

    public UnstableState(SupervisorImp s, boolean cf){
        super(s);
        prevsize = 0;
        stablecount = 0;
        this.communicationFlag = cf;
        next = this;
    }

    public UnstableState(SupervisorImp s){
        super(s);
        prevsize = 0;
        stablecount = 0;
        next = this;
    }

    //Inspect whether event handler execution is finished or not
    public void work(){
        Log.d("wtchoi", "Unstable State");

        BlockingDeque<SLog> sStack = s.mainStack;
        BlockingDeque<SLog> sList = s.mainList;

        //We assume that lock all function accessing s.sStack acquire lock of s.sList.
        synchronized (s.sLists)
        {
            if(prevsize == s.getTraceSize(s.mainTid) && s.getStackSize(s.mainTid) == 0){
                if(stablecount == s.STABLECOUNT){
                    BlockingDeque<SLog> trace = s.getTrace(s.mainTid);
                    CVector<SLog> lst = new CVector<SLog>(trace);
                    s.transitionInfo = new TransitionInfo(lst);
                    Log.d("wtchoii", String.valueOf(s.getTraceSize(s.mainTid)));

                    s.clearTrace(s.mainTid);

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
        Log.d("wtchoi", "stopstop");
        DriverPacket p = DriverPacket.getAckStop();
        s.channel.sendPacket(p);
        s.closeApplication();
    }

    public synchronized AbstractState next(){ return next; }
}