package edu.berkeley.wtchoi.cc.driver;

import com.android.chimpchat.core.IChimpDevice;
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
        ViewInfo currentView = driver.getCurrentView();
        int id = currentView.findViewDbyPosition(x,y).getId();
        DriverPacket p = DriverPacket.getEnterEditText(id,content);
        driver.channel.sendPacket(p);
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
