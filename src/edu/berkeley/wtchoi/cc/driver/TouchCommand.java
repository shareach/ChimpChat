package edu.berkeley.wtchoi.cc.driver;

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.TouchPressType;
import edu.berkeley.wtchoi.cc.util.IdentifierPool;
import edu.berkeley.wtchoi.cc.util.TcpChannel;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/26/12
 * Time: 1:27 PM
 * To change this template use File | Settings | File Templates.
 */
//Concrete ICommand Position. We are going to use this as an input character
public final class TouchCommand extends ICommand {//TODO
    private Integer x;
    private Integer y;
    private boolean isEditableAndHasText = false;

    //All implementation of command should obtain integer identifier from
    private static final Integer tint = IdentifierPool.getFreshInteger();

    protected int compareSameType(ICommand target){
        TouchCommand cmd = (TouchCommand) target;

        int c1 = x.compareTo(cmd.x);
        if (c1 == 0) {
            if(isEditableAndHasText == cmd.isEditableAndHasText)
                return y.compareTo(cmd.y);
            if(isEditableAndHasText && !cmd.isEditableAndHasText) return 1;
            else return -1;
        }
        return c1;
    }

    public TouchCommand(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public TouchCommand(Integer x, Integer y, boolean mode) {
        this.x = x;
        this.y = y;
        isEditableAndHasText = mode;
    }

    public void sendCommand(DriverImp driver) {
        driver.mDevice.touch(x, y, TouchPressType.DOWN_AND_UP);
        super.sendCommandAck(driver.channel);
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Integer typeint(){
        return tint;
    }
    
    public String toString(){
        if(isEditableAndHasText)
            return ("T(" + x + "," + y + ")");
        return ("(" + x + "," + y + ")");
    }
}
