package edu.berkeley.wtchoi.cc.driver.drone;
//INSTRUMENTATION

import android.app.Activity;
import android.app.Application;
import android.util.Log;
import edu.berkeley.wtchoi.logger.Logger;
import edu.berkeley.wtchoi.logger.LoggerImp;

public abstract class Supervisor{
	 
	private static SupervisorImp supervisor;

    public static void appInit(Activity defaultActivity){
        Supervisor.init(defaultActivity);
        //Supervisor.logActivityCreated(defaultActivity);
        Supervisor.clearData();
        //Supervisor.logCall("onCreate", defaultActivity);
    }

    public static void appStart(){
        Supervisor.start();
    }

	private static void init(Activity defaultActivity){
        if(skipmode()) return;
        //If this is first execution of application,
        //initialize supervisor
        Logger.init(new LoggerImp(){
            public void log(String s){
                Log.d("wtchoi",s);
            }
        });

		if(supervisor == null){
			supervisor = new SupervisorImp();
            supervisor.init(defaultActivity);
		}
        else{
            ////If this is not the first run of application,
            ////there might be several options to choose.
            ////supervisor.sThread.stop();

            ////The first thing to determine is whether this is
            ////intended restart or not.
            //if(!supervisor.restartIntended){
            //    //If this is not intended, ERROR
            //    throw new RuntimeException("Unexpected stop/resume detected");
            //}
            //else{
            //   //Otherwise, we have to check what is the intention.
            //   //We will use is later.
            //   throw new RuntimeException("Unreachable Reached!");
            //}
            //supervisor.sThread.resume();
        }
	}

    private static void clearData(){
        if(skipmode()) return;
        supervisor.clearData();
    }

	public static void start(){
        if(skipmode()) return;
        Logger.log("Supervisor Starter");
		supervisor.start();
	}

    public static void logEnter(int fid){
        if(skipmode()) return;
        supervisor.logEnter(fid);
        Logger.log("ENTER: "+ fid);
    }

    public static void logExit(int fid){
        if(skipmode()) return;
        supervisor.logExit(fid);
        Logger.log("EXIT: " + fid);
    }


    public static void logCall(int fid){
        if(skipmode()) return;
		supervisor.logCall(fid);
        Logger.log("CALL: "+ fid);
	}

	
	public static void logReturn(int fid){
        if(skipmode()) return;
		supervisor.logReturn(fid);
        Logger.log("RETURN: "+ fid);
	}

    public static void logReceiver(Object o, int fid){
        if(skipmode()) return;
        supervisor.logReceiver(o,fid);
    }

    /*
	public static void logTrue(String fname, Object o){
        if(skipmode()) return;
		supervisor.logTrue(fname,o);
	}
	
	public static void logFalse(String f, Object o){
        if(skipmode()) return;
		supervisor.logFalse(f,o);
	}

    public static void logEndIf(String f, Object o){
        if(skipmode()) return;
        supervisor.logEndIf(f,o);
    }

	public static void logSwitch(String sname, Object o){
        if(skipmode()) return;
		supervisor.logSwitch(sname,o);
	}
	*/

    public static void logProgramPoint(int ppid, int fid){
        if(skipmode()) return;
        supervisor.logProgramPoint(ppid, fid);
    }


	public static void logActivityCreated(Activity a){
        if(skipmode()) return;
        Logger.log("Activity(" + a.toString() + ") is Created");
		supervisor.logActivityCreated(a);
	}
	
	public static void logStart(Activity a){
        if(skipmode()) return;
        Logger.log("Activity(" + a.toString() + ") is Started");
		supervisor.logStart(a);
	}

	public static void logStop(Activity a){
        if(skipmode()) return;
        Logger.log("Activity(" + a.toString() + ") is Stoped");
		supervisor.logStop(a);
	}

    public static boolean skipmode(){
        return false;
    }
}
