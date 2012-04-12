package edu.berkeley.wtchoi.cc.driver;

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;

import edu.berkeley.wtchoi.cc.util.IdentifierPool;
import edu.berkeley.wtchoi.cc.util.TcpChannel;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/26/12
 * Time: 8:50 PM
 * To change this template use File | Settings | File Templates.
 */
public final class PushCommand extends ICommand {

    public enum Type{
        MENU;
    }

    //All implementation of command should obtain integer identifier from
    private static final Integer typeint = IdentifierPool.getFreshInteger();

    private Type type;

    public void sendCommand(DriverImp driver){
        switch(this.type){
            case MENU:
                //Code fragment for push MENU button
                driver.mDevice.press(PhysicalButton.MENU, TouchPressType.DOWN_AND_UP);
                break;
            default:
                return;
        }
        super.sendCommandAck(driver.channel);
    }

    private PushCommand(Type t){
        type = t;
    }

    public static PushCommand getMenu(){
        return new PushCommand(Type.MENU);
    }
    
    protected int compareSameType(ICommand target){
        PushCommand cmd = (PushCommand) target;
        return type.compareTo(cmd.type);
    }
    
    public Integer typeint(){
        return typeint;
    }

    public String toString(){
        return "MENU";
    }
}
