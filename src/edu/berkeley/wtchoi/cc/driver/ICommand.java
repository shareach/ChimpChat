package edu.berkeley.wtchoi.cc.driver;

import com.android.chimpchat.core.IChimpDevice;
import edu.berkeley.wtchoi.cc.util.TcpChannel;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/24/12
 * Time: 4:30 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ICommand implements Comparable<ICommand>{
    public abstract void sendCommand(DriverImp driver) throws RuntimeException;

    public abstract Integer typeint();
    //This is for fast comparison between different implementation of ICommand interface
    //All different implementation should use different Integer number;

    //All implementation of command should obtain integer identifier from
    //private static final Integer tint = IdentifierPool.getFreshInteger();

    //Please call this function, after sending chimpchat command
    protected void sendCommandAck(TcpChannel<DriverPacket> channel){
        //1.2 Send command acknowledgement to App Supervisor
        DriverPacket ack = DriverPacket.getAckCommand();
        channel.sendPacket(ack);

        //1.3 Wait for App Supervisor response
        DriverPacket receivingPacket;
        receivingPacket = channel.receivePacket();
        if (receivingPacket.getType() != DriverPacket.Type.AckStable) {
            throw new RuntimeException("Application Execution is not guided correctly");
        }
    }

    public int compareTo(ICommand target){
        int f = (new Integer(typeint())).compareTo(target.typeint());
        if(f != 0) return f;

        return compareSameType(target);
    }

    protected abstract int compareSameType(ICommand target);
}