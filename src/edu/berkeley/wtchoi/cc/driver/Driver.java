package edu.berkeley.wtchoi.cc.driver;

import edu.berkeley.wtchoi.cc.util.E;
import edu.berkeley.wtchoi.util.TcpChannel;
import edu.berkeley.wtchoi.cc.driver.DriverPacket.OptionIndex;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wtchoi
 * Date: 3/25/12
 * Time: 8:03 PM
 * To change this template use File | Settings | File Templates.
 */

public class Driver<TransitionInfo>{
   
    private DriverOption option;
        
    //private ChimpChat mChimpchat;

    //IChimpDevice mDevice;
    Device device;
    TcpChannel<DriverPacket> channel;

    private boolean justRestarted = false;
    private boolean justStoped = false;
    private boolean errorOccur = false;

    private Driver(DriverOption option, Device device) {
        super();
        this.option = option;
        this.device = device;
    }


    // Initiate application, connect chip, connect channel
    public static <T> Driver<T> connectToDevice(DriverOption option, String deviceID, int localport, Class<T> clazz){
        option.assertComplete();

        Device.init(option.getADB());
        Device device = Device.waitForConnection(option.getTimeout(), deviceID, localport, 13338);
        if (device == null) {
            //throw new RuntimeException("Couldn't connect.");
            return null;
        }
        device.wake();
        return new Driver<T>(option,device);
    }


    public boolean initiateApp() {
        ////1. Initiate Communication TcpChannel (Asynchronous)
        //channel = TcpChannel.getServerSide(13338);
        //channel.connectAsynchronous();
        channel = TcpChannel.getClientSide("127.0.0.1",13337);
        channel.setTryCount(5);
        channel.setTryInterval(1000);

        //1.5. Wait phone to clean up previously died-application instance
        long minimumWait = 1000;
        long elapsedTime = System.currentTimeMillis() - stopTimeStamp;
        if(elapsedTime < minimumWait){
            E.sleep(minimumWait - elapsedTime);
        }

        //2. Initiate ChimpChat connection
        String runComponent = option.getRunComponent();
        Collection<String> coll = new LinkedList<String>();
        Map<String, Object> extras = new HashMap<String, Object>();
        boolean successFlag = false;
        for(int i = 0; i<10 ; i++){
            System.out.println("send wake up!");
            successFlag = device.startActivity(null, null, null, null, coll, extras, runComponent, 0);
            E.sleep(100);
            if(successFlag)
                break;
        }



        //3. Wait for communication channel initiation
        System.out.println("wait");
        channel.connect();
        //channel.waitConnection();
        System.out.println("go");
        System.out.println("Waiting for application to be stable");


        //4. Wait for application to be ready for command
        {
            DriverPacket packet = channel.receivePacket();
            if (packet.getType() != DriverPacket.Type.AckStable) {
                System.out.println("received packet:" + packet.getType());
                throw new RuntimeException("Application sent wrong packet. AckStable expected");
                //return false;
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

        if(!justStoped){
            channel.sendPacket(DriverPacket.getReset());
            stopTimeStamp = System.currentTimeMillis();
        }

        justStoped = false;
        errorOccur = false;

        return initiateApp();
    }


    public void shutdown() {
        device = null;
    }


    ViewInfo getView() {
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

    public ViewInfo getCurrentView(){
        return getView();
    }

    public TransitionInfo getCurrentTransitionInfo(){
        TransitionInfo ti;

        DriverPacket sPacket = DriverPacket.getRequestTI();
        channel.sendPacket(sPacket);

        DriverPacket rPacket = channel.receivePacket();
        ti = rPacket.getTI();

        return ti;
    }

    public boolean go(List<? extends ICommand> clist) {
        //1. Send commands
        for (ICommand c : clist) {
            go(c);
            if(justStoped) return false;
        }
        return true;
    }

    private long stopTimeStamp = 0;
    public boolean go(ICommand c) {
        justRestarted = false;
        boolean result = false;
        try{
            c.sendCommand(this);
            result = true;
        }
        catch(ICommand.ApplicationTerminated e){
            justStoped = true;
            stopTimeStamp = System.currentTimeMillis();

        }
        catch(Device.CannotSendCommand ee){
            errorOccur = true;
        }

        return result;
    }

    public boolean isStopState(){
        return justStoped;
    }

    public boolean isErrorState(){
        return errorOccur;
    }
}
