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
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import edu.berkeley.wtchoi.cc.driver.DriverPacket;
import edu.berkeley.wtchoi.cc.learnerImp.ctree.TransitionInfo;
import edu.berkeley.wtchoi.util.TcpChannel;

import java.util.*;
import java.util.concurrent.*;

class SupervisorImp extends Thread{
    volatile ConcurrentHashMap<Long, BlockingDeque<SLog>> sLists;
    volatile ConcurrentHashMap<Long, BlockingDeque<SLog>> sStacks;
    volatile ConcurrentSkipListSet<Long> threads;
    volatile BlockingDeque<SLog> mainList;
    volatile BlockingDeque<SLog> mainStack;

    long mainTid;

    private void checkInit(long tid){
        if(tid == mainTid) return;

        boolean flag = threads.contains(tid);
        if(flag) return;

        BlockingDeque<SLog> list = new LinkedBlockingDeque<SLog>();
        BlockingDeque<SLog> stack = new LinkedBlockingDeque<SLog>();
        sLists.put(tid, list);
        sStacks.put(tid, stack);
        threads.add(tid);
    }

    private void pushToList(SLog slog){
        long tid = Thread.currentThread().getId();
        //Log.d("wtchoi","tid = " + tid + ", " + slog.toString());

        BlockingDeque<SLog> lst;
        if(tid == mainTid){
            lst = mainList;
        }
        else{
            checkInit(tid);
            lst = sLists.get(tid);
        }
        lst.add(slog);
        //Log.d("wtchoi", "list length = " + lst.size());
    }

    private void pushToBoth(SLog slog){
        long tid = Thread.currentThread().getId();

        BlockingDeque<SLog> lst;
        BlockingDeque<SLog> stk;
        if(tid == mainTid){
            lst = mainList;
            stk = mainStack;
        }
        else{
            checkInit(tid);
            lst = sLists.get(tid);
            stk = sStacks.get(tid);
        }
        lst.add(slog);
        stk.add(slog);

        Log.d("wtchoi", "list length = " + lst.size());
    }

    private void popFromStack(){
        long tid = Thread.currentThread().getId();

        BlockingDeque<SLog> stack;
        if(tid == mainTid){
            stack = mainStack;
        }
        else{
            checkInit(tid);
            stack = sStacks.get(tid);
        }
        stack.removeLast();
    }

    private SLog getStackTop(){
        long tid = Thread.currentThread().getId();
        BlockingDeque<SLog> stack;
        if(tid == mainTid){
            stack = mainStack;
        }
        else{
            checkInit(tid);
            stack = sStacks.get(tid);

        }
        return stack.getLast();
    }

    private int tickcount = 0;

    private ConcurrentHashMap<Activity,ActivityState> activityStates;
    private volatile AbstractState state;
    private ApplicationWrapper app_wrapper;

    volatile TcpChannel<DriverPacket> channel;

    //Application is activated only after method "onStart" is call of the default activity
    private boolean isBeforeActivated = true;


    volatile int TICKCOUNT = 5;
    volatile int TICKINTERVAL = 100;
    volatile int TICKSNOOZE = 3;
    volatile int STABLECOUNT = 1;

    public int getSTABLECOUNT(){
        return STABLECOUNT;
    }

    volatile TransitionInfo transitionInfo;

    //Application properties
    private int screen_x;
    private int screen_y;
    private WindowManager windowManager;
    private Display defaultDisplay;

    //internal flag variables
    volatile private boolean restartIntended = false;

    public SupervisorImp(){}

    public void prepare(Activity defaultActivity){
        //If this is first execution of application,
        //initialize supervisor
        sLists = new ConcurrentHashMap<Long, BlockingDeque<SLog>>();
        sStacks = new ConcurrentHashMap<Long, BlockingDeque<SLog>>();
        threads = new ConcurrentSkipListSet<Long>();
        mainStack = new LinkedBlockingDeque<SLog>();
        mainList = new LinkedBlockingDeque<SLog>();
        mainTid = Thread.currentThread().getId();
        //Log.d("wtchoi","mainTid = " + mainTid);

        activityStates = new ConcurrentHashMap<Activity,ActivityState>();

        Application app = defaultActivity.getApplication();
        app_wrapper = new ApplicationWrapper(app);


        windowManager = defaultActivity.getWindowManager();
        defaultDisplay = windowManager.getDefaultDisplay();

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
            Activity activeActivity;
            synchronized (sLists){
                activeActivity = getCurrentActivity();
            }

            //Tick Sleep
            //Log.d("wtchoi", "try sleepTick in run");
            sleepTick();

            //Wait for the first activity to be started
            //TODO: catch the case when application dies before activated


            //Check whether App is active or note
            //finish this round, if App is not yet activated
            if(isBeforeActivated) continue;

            //finish application is App is become inactive
            if(activeActivity == null){
                //Log.d("wtchoi", "try stop!!!!");
                state.onStop();
            }

            //Log.d("wtchoi", "try run main");
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

    private void sleepTick(){
        try{
            Thread.sleep(TICKINTERVAL);
        }
        catch(InterruptedException e){}
    }

    private void initiateChannel(){
        channel = TcpChannel.getServerSide(13338);
        channel.connect();

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
        tickcount = (tickcount > TICKSNOOZE) ? tickcount : TICKSNOOZE;
    }

    private Activity getCurrentActivity(){
        synchronized (sLists){
            for(Map.Entry<Activity, ActivityState> e:activityStates.entrySet()){
                if(e.getValue().isActive)
                    return e.getKey();
            }
            return null;
        }
    }

    public int getScreenX(){
        return screen_x;
    }

    public int getScreenY(){
        return screen_y;
    }

    public synchronized void logEnter(int fid){
        SLog log = SLog.getEnter(fid);
        synchronized (sLists){
            pushToBoth(log);
            snooze();
        }
    }

    public synchronized void logReceiver(Object obj, int fid){
        SLog log = SLog.getReceiver(obj.hashCode(), fid);
        synchronized (sLists){
            pushToList(log);
            snooze();
        }
    }

    public synchronized void logExit(int fid){
        SLog log = SLog.getExit(fid);
        synchronized (sLists){
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
        SLog log = SLog.getUnroll(fid);
        synchronized (sLists){
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
        SLog log = SLog.getCall(fid);
        synchronized (sLists){
            pushToBoth(log);
            snooze();
        }
    }

    public void logReturn(int fid){
        SLog log = SLog.getReturn(fid);
        synchronized (sLists){
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
        SLog log = SLog.getPP(pp, fid);
        synchronized (sLists){
            pushToList(log);
            snooze();
        }
    }

    private int activityMethodCallCount = 0;
    public void logActivityCreatedEnter(){
        synchronized (sLists){
            activityMethodCallCount++;
            snooze();
        }
    }

    public void logActivityCreatedExit(Activity a){
        synchronized (sLists){
            activityMethodCallCount--;
            if(activityMethodCallCount == 0){
                activityStates.put(a, new ActivityState());
            }
            snooze();
        }
    }

    public void logStartEnter(){
        synchronized (sLists)
        {
            activityMethodCallCount++;
            //isBeforeActivated = false;
            //activityStates.get(a).setActive(true);
            snooze();
        }
    }

    public void logStartExit(Activity a){
        synchronized (sLists)
        {
            activityMethodCallCount--;
            if(activityMethodCallCount == 0){
                isBeforeActivated = false;
                activityStates.get(a).setActive(true);
            }
            snooze();
        }
    }

    public void logStopEnter(){
        synchronized (sLists)
        {
            activityMethodCallCount++;
            snooze();
        }
    }

    public void logStopExit(Activity a){
        synchronized (sLists)
        {
            activityMethodCallCount--;
            if(activityMethodCallCount == 0){
                ActivityState t = activityStates.get(a);
                t.setActive(false);
            }
            snooze();
        }
    }


    // To track whether activity is enabled or not
    // If all activity is disabled, supervisor will skip it's tick
    private class ActivityState{
        public boolean isActive;

        public ActivityState(){
            isActive = false;
        }

        public synchronized void setActive(boolean b){
            isActive = b;
        }
    }

    int getTraceSize(long tid){
        synchronized (sLists){
            if(tid == mainTid)
                return mainList.size();
            else
                return sLists.get(tid).size();
        }
    }

    int getStackSize(long tid){
        synchronized (sLists){
            if(tid == mainTid)
                return mainStack.size();
            else
                return sStacks.get(tid).size();
        }
    }

    void clearTrace(long tid){
        synchronized (sLists){
            if(tid == mainTid)
                mainList.clear();
            else
                sLists.get(tid).clear();
        }
    }

    BlockingDeque<SLog> getTrace(long tid){
        synchronized (sLists){
            if(tid == mainTid)
                return mainList;
            else
                return sLists.get(tid);
        }
    }
}

