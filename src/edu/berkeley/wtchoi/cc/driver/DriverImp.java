package edu.berkeley.wtchoi.cc.driver;

import com.android.chimpchat.ChimpChat;
import com.android.chimpchat.core.IChimpDevice;
import edu.berkeley.wtchoi.cc.util.TcpChannel;
import edu.berkeley.wtchoi.cc.driver.DriverPacket.OptionIndex;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 8:03 PM
 * To change this template use File | Settings | File Templates.
 */

public class DriverImp implements IDriver {
   
    private DriverImpOption option;
        
    private ChimpChat mChimpchat;
    private IChimpDevice mDevice;

    private TcpChannel<DriverPacket> channel;

    private boolean justRestarted = false;




    public DriverImp(DriverImpOption option) {
        super();
        option.assertComplete();
        this.option = option;
    }


    // Initiate application, connect chip, connect channel
    public boolean connectToDevice() {
        //3. Boot ChimpChat Instance
        TreeMap<String, String> options = new TreeMap<String, String>();
        options.put("backend", "adb");
        options.put("adbLocation", option.getADB());
        mChimpchat = ChimpChat.getInstance(options);

        //4. Initiate ChimpChat TcpChannel with a target device
        mDevice = mChimpchat.waitForConnection(option.getTimeout(), ".*");
        if (mDevice == null) {
            //throw new RuntimeException("Couldn't connect.");
            return false;
        }
        mDevice.wake();
        return true;
    }


    public boolean initiateApp() {
        //1. Initiate Communication TcpChannel (Asynchronous)
        channel = TcpChannel.getServerSide(13338);
        channel.connectAsynchronous();

        //2. Initiate ChimpChat connection
        String runComponent = option.getRunComponent();
        Collection<String> coll = new LinkedList<String>();
        Map<String, Object> extras = new HashMap<String, Object>();
        mDevice.startActivity(null, null, null, null, coll, extras, runComponent, 0);

        //3. Wait for communication channel initiation
        System.out.println("wait");
        channel.waitConnection();
        System.out.println("go");
        System.out.println("Waiting for application to be stable");


        //4. Wait for application to be ready for command
        {
            DriverPacket packet = channel.receivePacket();
            if (packet.getType() != DriverPacket.Type.AckStable) {
                //throw new RuntimeException("Application sent wrong packet. AckStable expected");
                return false;
            }

            justRestarted = true;
            System.out.println("Application Initiated");
        }


        //5. Setup testing parameters
        {
            int[] opt = new int[4];
            opt[OptionIndex.ITickInterval.ordinal()] = option.getTickInterval();
            opt[OptionIndex.ITickCount.ordinal()]    = option.getTickCount();
            opt[OptionIndex.ITickSnooze.ordinal()]   = option.getTickSnooze();
            opt[OptionIndex.IStableCount.ordinal()]  = option.getStableCount();

            DriverPacket packet = DriverPacket.getSetOptions(opt);

            System.out.println("Testing option sent");
        }


        return true;
        //NOTE: At this moment, we expect application to erase all user data when ever it starts.
        //Therefore, our protocol doesn't have anythings about resetting application data.
        //However, we may need more complex protocol to fine control an application.
    }

    public boolean restartApp() {
        if(justRestarted) return true;
        channel.sendPacket(DriverPacket.getReset());
        return initiateApp();
    }


    public void shutdown() {
        mChimpchat.shutdown();
        mDevice = null;
    }


    public ViewInfo getView() {
        ViewInfo mv;
        Object obj;

        DriverPacket sPacket = DriverPacket.getRequestView();
        channel.sendPacket(sPacket);

        DriverPacket rPacket = channel.receivePacket();
        mv = rPacket.getView();

        //DEBUG PRINT : whether received information is correct or not
        //System.out.println(mv);
        return mv;
    }

    public boolean go(List<? extends ICommand> clist) {
        //1. Send commands
        for (ICommand c : clist) {
            if (!go(c)) return false;
        }
        return true;
    }

    public boolean go(ICommand c) {
        justRestarted = false;

        //0. Assume application is waiting for command

        //1.1 Send command through ChimpChat.
        c.sendCommand(mDevice);

        //1.2 Send command acknowledgement to App Supervisor
        DriverPacket ack = DriverPacket.getAckCommand();
        channel.sendPacket(ack);

        //1.3 Wait for App Supervisor response
        DriverPacket receivingPacket = channel.receivePacket();
        if (receivingPacket.getType() != DriverPacket.Type.AckStable) {
            //throw new RuntimeException(Application Execution is not guided correctly);
            return false;
        }

        return true;
    }


}
