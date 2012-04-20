package edu.berkeley.wtchoi.cc.driver;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/26/12
 * Time: 8:46 PM
 * To change this template use File | Settings | File Templates.
 */

import edu.berkeley.wtchoi.cc.util.E;

public class DriverImpOption {
    private String mainActivity;
    private String applicationPackage;
    private long   timeout = 5000;
    private String adb;

    private int mTickCount;
    private int mTickInterval;
    private int mTickSnooze;
    private int mStableCount;


    public void fillFromEnvironmentVariables(){
        adb = System.getenv("ADB_DIR");
        applicationPackage = System.getenv("PACKAGE");//("com.android.demo.notepad3")
        mainActivity = System.getenv("MAIN_ACTIVITY");//("com.android.demo.notepad3.Notepadv3")


        mTickCount    = E.getenv_as_int("TICK_COUNT", 4);
        mTickInterval = E.getenv_as_int("TICK_INTERVAL",10);
        mTickSnooze   = E.getenv_as_int("TICK_SNOOZE",2);
        mStableCount  = E.getenv_as_int("STABLE_COUNT",1);
    }

    //to check whether all basic information is there
    public boolean isComplete(){
        if(mainActivity == null) return false;
        if(applicationPackage == null) return false;
        if(adb == null) return false;
        return true;
    }

    //to check and raise exception
    public void assertComplete(){
        if(mainActivity == null) throw new RuntimeException(__msg1);
        if(applicationPackage == null) throw new RuntimeException(__msg2);
        if(adb == null) throw new RuntimeException(__msg3);
    }
    private static String __msg1 = "Main Activity is not specified";
    private static String __msg2 = "Application Package is not specified";
    private static String __msg3 = "ADB path is not specified";


    //get methods
    public String getApplicationPackage(){
        return applicationPackage;
    }

    public String getRunComponent(){
        return applicationPackage + '/' + mainActivity;
    }

    public String getADB(){
        return adb;
    }

    public long getTimeout(){
        return timeout;
    }

    public int getTickCount(){
        return mTickCount;
    }

    public int getTickInterval(){
        return mTickInterval;
    }

    public int getTickSnooze(){
        return mTickSnooze;
    }

    public int getStableCount(){
        return mStableCount;
    }


    //set methods
    public void setMainActivity(String s){
        mainActivity = s;
    }

    public void setApplicationPackage(String s){
        applicationPackage = s;
    }

    public void setTimeout(long t){
        timeout = t;
    }

    public void setADB(String s){
        adb = s;
    }
}