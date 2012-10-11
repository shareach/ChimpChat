package edu.berkeley.wtchoi.cc.driver.drone;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 6:08 PM
 * To change this template use File | Settings | File Templates.
 */

import android.app.Activity;
import android.app.Application;
import android.graphics.Point;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.WindowManager;
import edu.berkeley.wtchoi.cc.driver.DriverPacket;
import edu.berkeley.wtchoi.cc.learnerImp.ctree.TransitionInfo;
import edu.berkeley.wtchoi.util.TcpChannel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

class SupervisorImp extends Thread{

    HashMap<Long, LinkedList<SLog>> sLists;
    HashMap<Long, LinkedList<SLog>> sStacks;
    HashSet<Long> threads;
    long mainTid;

    private void checkInit(){
        long tid = Thread.currentThread().getId();

        boolean flag = threads.contains(tid);
        if(flag) return;

        LinkedList<SLog> list = new LinkedList<SLog>();
        LinkedList<SLog> stack = new LinkedList<SLog>();
        sLists.put(tid, list);
        sStacks.put(tid, stack);

    }

    private void pushToList(SLog slog){
        checkInit();
        long tid = Thread.currentThread().getId();
        sLists.get(tid).add(slog);
    }

    private void pushToBoth(SLog slog){
        checkInit();
        long tid = Thread.currentThread().getId();
        sStacks.get(tid).add(slog);
        sLists.get(tid).add(slog);
    }

    private void popFromStack(){
        checkInit();
        long tid = Thread.currentThread().getId();
        sStacks.get(tid).removeLast();
    }

    private SLog getStackTop(){
        checkInit();
        long tid = Thread.currentThread().getId();
        return sStacks.get(tid).getLast();
    }

    private int tickcount = 0;

    private HashMap<Activity,ActivityState> activityStates;
    private AbstractState state;
    private ApplicationWrapper app_wrapper;
    TcpChannel<DriverPacket> channel;

    int TICKCOUNT = 5;
    int TICKINTERVAL = 100;
    int TICKSNOOZE = 3;
    int STABLECOUNT = 1;

    TransitionInfo transitionInfo;

    //Application properties
    private int screen_x;
    private int screen_y;
    private WindowManager windowManager;
    private Display defaultDisplay;

    //internal flag variables
    private boolean restartIntended = false;

    public SupervisorImp(){}

    public void init(Activity defaultActivity){
        //If this is first execution of application,
        //initialize supervisor
        sLists = new HashMap<Long, LinkedList<SLog>>();
        sStacks = new HashMap<Long, LinkedList<SLog>>();
        mainTid = Thread.currentThread().getId();
        sLists.put(mainTid, new LinkedList<SLog>());
        sStacks.put(mainTid, new LinkedList<SLog>());

        activityStates = new HashMap<Activity,ActivityState>();

        Application app = defaultActivity.getApplication();
        app_wrapper = new ApplicationWrapper(app);

        windowManager = defaultActivity.getWindowManager();
        defaultDisplay = windowManager.getDefaultDisplay();

        Point size = new Point();

        //defaultDisplay.getSize(size);
        screen_x = defaultDisplay.getWidth();
        screen_y = defaultDisplay.getHeight();

        initiateChannel();
    }

    public void clearData(){
        app_wrapper.clearData();
    }

    @Override
    public void run(){
        while(true){
            //Establish Server Connection, at first
            //if(state == SupervisorState.INIT)
            //	initiateChannel();
            Activity activeActivity = getCurrentActivity();

            //Tick Sleep
            sleepTick();

            //Check whether App is active or note
            //finish this round, if App is inactive
            synchronized (sLists){
                if(activeActivity == null){
                    state.onStop();
                }

                if(tickcount == 0){
                    this.state.work();
                    this.state = state.next();
                    tickcount = TICKCOUNT;
                }
                else{
                    tickcount--;
                }
            }
        }
    }

    private void sleepTick(){
        try{
            Thread.sleep(TICKINTERVAL);
        }
        catch(InterruptedException e){}
    }

    private void initiateChannel(){
        channel = TcpChannel.getServerSide(13338);
        channel.connect();
        //channel = TcpChannel.getClientSide("10.0.2.2",13338);
        //channel.connect();

        Log.d("wtchoi", "stream initialized");
        state = new InitState(this);
    }

    public void closeApplication(){
        channel.close();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void start(){
        if(isAlive()) return;
        super.start();
    }

    private void snooze(){
        synchronized (sLists){
            tickcount = (tickcount > TICKSNOOZE) ? tickcount : TICKSNOOZE;
        }
    }

    private Activity getCurrentActivity(){
        for(Map.Entry<Activity, ActivityState> e:activityStates.entrySet()){
            if(e.getValue().isActive)
                return e.getKey();
        }
        return null;
    }

    public int getScreenX(){
        return screen_x;
    }

    public int getScreenY(){
        return screen_y;
    }

    public synchronized void logEnter(int fid){
        synchronized (sLists){
            pushToBoth(SLog.getEnter(fid));
            snooze();
        }
    }

    public synchronized void logReceiver(Object obj, int fid){
        synchronized (sLists){
            pushToList(SLog.getReceiver(obj.hashCode(), fid));
            snooze();
        }
    }

    public synchronized void logExit(int fid){
        synchronized (sLists){
            SLog log = SLog.getExit(fid);
            pushToList(log);

            SLog stackTop = getStackTop();
            if(stackTop.type == SLog.ENTER && stackTop.fid == fid){
                popFromStack();
            }
            else{
                Log.d("wtchoi","stack top:"+stackTop.toString());
                Log.d("wtchoi","new log:"+log.toString());
                Log.d("wtchoi","somethings is wrong! call return mismatch");
                throw new RuntimeException("stack trace failed!");
            }
            snooze();
        }
    }

    public synchronized void logUnroll(int fid){
        synchronized (sLists){
            SLog log = SLog.getUnroll(fid);
            pushToList(log);

            SLog stackTop = getStackTop();
            if(stackTop.type == SLog.ENTER && stackTop.fid == fid){
                popFromStack();
            }
            else{
                Log.d("wtchoi","stack top:"+stackTop.toString());
                Log.d("wtchoi","new log:"+log.toString());
                Log.d("wtchoi","somethings is wrong! call return mismatch");
                throw new RuntimeException("stack trace failed!");
            }
            snooze();
        }
    }

    public synchronized void logCall(int fid){
        synchronized (sLists){
            SLog log = SLog.getCall(fid);
            pushToBoth(log);
            snooze();
        }
    }

    public void logReturn(int fid){
        synchronized(sLists){
            SLog log = SLog.getReturn(fid);
            pushToList(log);

            SLog stackTop = getStackTop();
            if(stackTop.type == SLog.CALL && stackTop.fid == fid){
                popFromStack();
            }
            else{
                Log.d("wtchoi","stack top:"+stackTop.toString());
                Log.d("wtchoi","new log:"+log.toString());
                Log.d("wtchoi","somethings is wrong! call return mismatch");
                throw new RuntimeException("stack trace failed!");
            }
            snooze();
        }
    }

    public void logProgramPoint(int pp, int fid){
        synchronized (sLists){
            pushToList(SLog.getPP(pp, fid));
            snooze();
        }
    }

    public void logActivityCreated(Activity a){
        synchronized (sLists){
            activityStates.put(a, new ActivityState());
            snooze();
        }
    }

    public void logStart(Activity a){
        synchronized (sLists){
            activityStates.get(a).setActive(true);
            snooze();
        }
    }

    public void logStop(Activity a){
        synchronized (sLists){
            ActivityState t = activityStates.get(a);
            t.setActive(false);
            snooze();
        }
    }


    // To track whether activity is enabled or not
    // If all activity is disabled, supervisor will skip it's tick
    class ActivityState{
        public boolean isActive;

        public ActivityState(){
            isActive = false;
        }

        public void setActive(boolean b){
            isActive = b;
        }
    }
}

