package edu.berkeley.wtchoi.cc.driver;

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.TouchPressType;
import edu.berkeley.wtchoi.cc.util.IdentifierPool;
import edu.berkeley.wtchoi.cc.util.TcpChannel;

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

    //All implementation of command should obtain integer identifier from
    private static final Integer tint = IdentifierPool.getFreshInteger();

    public EnterCommand(int x, int y, String content){
        this.x = x;
        this.y = y;
        this.content = content;
    }

    public void sendCommand(DriverImp driver){
        char[] contents = content.toCharArray();
        String temp = "";
        for(char c: contents){
            if(c == ' '){
                driver.mDevice.press("KEYCODE_SPACE", TouchPressType.DOWN_AND_UP);
                continue;
            }
            driver.mDevice.type(String.valueOf(c));
        }
        //driver.mDevice.type(content);
        //super.sendCommandAck(driver.channel);
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
                return content.compareTo(cmd.content);
            }
        }
        return f;
    }

    public String toString(){
        return "T("+x+","+y+")";
    }

}
