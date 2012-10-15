package edu.berkeley.wtchoi.cc.driver.drone;
//INSTRUMENTATION

import android.app.Activity;
import android.app.Application;
import android.util.Log;
import edu.berkeley.wtchoi.logger.Logger;
import edu.berkeley.wtchoi.logger.LoggerImp;

public abstract class Supervisor{
	 
	private static SupervisorImp supervisor;

    public static void appPrepare(Activity defaultActivity){
        Supervisor.prepare(defaultActivity);
        //Supervisor.logActivityCreated(defaultActivity);
        //Supervisor.logCall("onCreate", defaultActivity);
    }

    public static void appStart(){
        Supervisor.start();
    }

    private static LoggerImp logger = new LoggerImp(){
        public void log(String s){
            Log.d("wtchoi",s);
        };};

	private static void prepare(Activity defaultActivity){
        if(skipmode()) return;
        //If this is first execution of application,
        //initialize supervisor
        Logger.init(logger);

		if(supervisor == null){
			supervisor = new SupervisorImp();
            supervisor.prepare(defaultActivity);
		}
        clearData();
	}

    private static void clearData(){
        if(skipmode()) return;
        supervisor.clearData();
    }

	public static void start(){
        if(skipmode()) return;
        Logger.log("Supervisor Start");
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

    public static void logUnroll(int fid){
        if(skipmode()) return;
        supervisor.logUnroll(fid);
        Logger.log("UNROLL: "+ fid);
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


	public static void logActivityCreatedEnter(){
        if(skipmode()) return;
		supervisor.logActivityCreatedEnter();
	}

    public static void logActivityCreatedExit(Activity a){
        if(skipmode()) return;
        Logger.log("Activity(" + a.toString() + ") is Created");
        supervisor.logActivityCreatedExit(a);
    }
	
	public static void logStartEnter(){
        if(skipmode()) return;
		supervisor.logStartEnter();
	}

    public static void logStartExit(Activity a){
        if(skipmode()) return;
        Logger.log("Activity(" + a.toString() + ") is Started");
        supervisor.logStartExit(a);
    }


    public static void logStopEnter(){
        if(skipmode()) return;
		supervisor.logStopEnter();
	}

    public static void logStopExit(Activity a){
        if(skipmode()) return;
        Logger.log("Activity(" + a.toString() + ") is Stoped");
        supervisor.logStopExit(a);
    }

    public static boolean skipmode(){
        return false;
    }
}
