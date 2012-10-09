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
import android.view.Display;
import android.view.WindowManager;
import edu.berkeley.wtchoi.cc.driver.DriverPacket;
import edu.berkeley.wtchoi.cc.learnerImp.ctree.TransitionInfo;
import edu.berkeley.wtchoi.util.TcpChannel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

class SupervisorImp extends Thread{

    LinkedList<SLog> sList;
    LinkedList<SLog> sStack;

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

    public void init(Application app, Activity defaultActivity){
        //If this is first execution of application,
        //initialize supervisor
        sList = new LinkedList<SLog>();
        sStack = new LinkedList<SLog>();
        activityStates = new HashMap<Activity,ActivityState>();

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
        tickcount = (tickcount > TICKSNOOZE) ? tickcount : TICKSNOOZE;
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






    public void logCall(String fname, Object o){
        synchronized(sList){
            SLog log = new SLog(SLog.CALL,fname,o);
            sList.add(log);
            sStack.add(log);
            snooze();
        }
    }

    public void logReturn(String fname, Object o){
        synchronized(sList){
            SLog log = new SLog(SLog.RETURN,fname,o);
            sList.add(log);

            SLog stackTop = sStack.getLast();
            if(stackTop.type == SLog.CALL && stackTop.obj == o && stackTop.message.equals(fname)){
                sStack.removeLast();
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

    public void logTrue(String fname, Object o){
        synchronized(sList){
            sList.add(new SLog(SLog.TRUE,fname,o));
            snooze();
        }
    }

    public void logFalse(String fname, Object o){
        synchronized(sList){
            sList.add(new SLog(SLog.FALSE,fname,o));
            snooze();
        }
    }

    public void logEndIf(String fname, Object o){
        synchronized (sList){
            sList.add(new SLog(SLog.ENDIF,fname,o));
            snooze();
        }
    }

    public void logSwitch(String sname, Object o){
        synchronized(sList){
            sList.add(new SLog(SLog.SWITCH,sname,o));
            snooze();
        }
    }

    public void logProgramPoint(String fname, int offset, Object o){
        synchronized(sList){
            sList.add(new SLog(SLog.PP, fname + "::" + String.valueOf(offset), o));
            snooze();
        }
    }

    public void logActivityCreated(Activity a){
        activityStates.put(a, new ActivityState());
        snooze();
    }

    public void logStart(Activity a){
        activityStates.get(a).setActive(true);
        snooze();
    }

    public void logStop(Activity a){
        ActivityState t = activityStates.get(a);
        t.setActive(false);
        snooze();
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

