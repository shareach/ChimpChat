package edu.berkeley.wtchoi.cc.driver.drone;
//INSTRUMENTATION


import android.content.ReceiverCallNotAllowedException;

import java.util.concurrent.Callable;

public class SLog{
    public static final int CALL = 1;
    public static final int RETURN = 2;
    public static final int ENTER = 3;
    public static final int EXIT = 4;
    public static final int UNROLL = 5;
    public static final int PP = 6;
    public static final int RECEIVER = 7;


    public int type;
	public int fid;
	public int aux;

    private SLog(int lt, int fid, int aux){
        this.type = lt;
        this.fid = fid;
        this.aux = aux;
    }


    private SLog(int lt, int fid){
		this.type = lt;
		this.fid = fid;
	}

    public static SLog getEnter(int fid){
        return new SLog(ENTER, fid);
    }

    public static SLog getExit(int fid){
        return new SLog(EXIT,  fid);
    }

    public static SLog getUnroll(int fid){
        return new SLog(UNROLL, fid);
    }

    public static SLog getCall(int fid){
        return new SLog(CALL, fid);
    }

    public static SLog getReturn(int fid){
        return new SLog(RETURN, fid);
    }

    public static SLog getReceiver(int oid, int fid){
        return new SLog(RECEIVER, fid, oid);
    }

    public static SLog getPP(int pid, int fid){
        return new SLog(PP, fid, pid);
    }
	
	public String toString(){
		String typ = null;
		switch(type){
			case CALL:
				typ = "CALL";
				break;
			case RETURN:
				typ = "RETURN";
				break;
            case ENTER:
                typ = "ENTER";
                break;
            case EXIT:
                typ = "EXIT";
                break;
			default:
				break;
		}
		return typ+"(fid = "+ fid +")";
	}
}