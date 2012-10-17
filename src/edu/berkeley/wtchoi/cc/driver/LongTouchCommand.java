package edu.berkeley.wtchoi.cc.driver;

import com.android.chimpchat.core.TouchPressType;
import edu.berkeley.wtchoi.cc.util.IdentifierPool;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 10/16/12
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */
public final class LongTouchCommand extends ICommand{

    private Integer x, y;

    public LongTouchCommand(int x, int y){
        this.x = x;
        this.y = y;
    }

    private static final Integer tint = IdentifierPool.getFreshInteger();

    @Override
    public Integer typeint(){
        return tint;
    }

    @Override
    protected int compareSameType(ICommand target){
        LongTouchCommand cmd = (LongTouchCommand) target;

        int c1 = x.compareTo(cmd.x);
        if (c1 == 0) {
            return y.compareTo(cmd.y);
        }
        return c1;
    }

    public void sendCommand(Driver driver) throws ApplicationTerminated, Device.CannotSendCommand {
        driver.device.longTouch(x, y);
        super.sendCommandAck(driver.channel);
    }


    public String toString(){
        return ("L(" + x + "," + y + ")");
    }
}
