package edu.berkeley.wtchoi.cc.driver;

import com.android.chimpchat.core.TouchPressType;
import edu.berkeley.wtchoi.cc.util.IdentifierPool;
import edu.berkeley.wtchoi.cc.driver.Device.CannotSendCommand;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/12/12
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */
public final class EnterCommand extends ICommand {
    private int x;
    private int y;
    private String content;
    private String toEnter;
    private boolean hasFocus = false;

    //All implementation of command should obtain integer identifier from
    private static final Integer tint = IdentifierPool.getFreshInteger();

    public EnterCommand(int x, int y, String content, String toEnter, boolean mode){
        this.x = x;
        this.y = y;
        this.content = content;
        this.toEnter = toEnter;
        this.hasFocus = mode;
    }

    public void sendCommand(Driver driver) throws CannotSendCommand, ApplicationTerminated{

        boolean result = false;
        if(!hasFocus){
            driver.device.touch(x,y,TouchPressType.DOWN_AND_UP);
            super.sendCommandAck(driver.channel);
        }

        String temp = "";
        for(char c: toEnter.toCharArray()){
            if(c == ' ')
                driver.device.press("KEYCODE_SPACE", TouchPressType.DOWN_AND_UP);
            else
                driver.device.type(String.valueOf(c));
        }
        super.sendCommandAck(driver.channel);
    }

    public Integer typeint(){
        return tint;
    }

    protected int compareSameType(ICommand target){
        EnterCommand cmd = (EnterCommand)target;
        int f = (new Integer(x)).compareTo(cmd.x);
        if(f == 0){
            f = (new Integer(y)).compareTo(cmd.y);
            if(f == 0){
                //return content.compareTo(cmd.content);
                boolean f1 = content.equals("");
                boolean f2 = cmd.content.equals("");
                if(f1 == f2) return 0;
                if(f1 && !f2) return 1;
                else return -1;
            }
        }
        return f;
    }

    public String toString(){
        if(!content.equals("")) return "TE("+x+","+y+")";
        return "E("+x+","+y+")";
    }

}
