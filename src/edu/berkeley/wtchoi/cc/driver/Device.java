package edu.berkeley.wtchoi.cc.driver;

import com.android.chimpchat.ChimpChat;
import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: cusgadmin
 * Date: 4/19/12
 * Time: 10:22 PM
 * To change this template use File | Settings | File Templates.
 */

//Device class wrap AdbChimpChat class to provide interface with success/failure return
public class Device{

    private static TreeMap<Long, LinkedList<String>> logMap;
    private static ChimpChat mChimpChat;
    private static AndroidDebugBridge bridge;

    public static void init(String adbPath){
        logMap = new TreeMap<Long, LinkedList<String>>();

        TreeMap<String, String> options = new TreeMap<String, String>();
        options.put("backend", "adb");
        options.put("adbLocation", adbPath);
        mChimpChat = ChimpChat.getInstance(options);

        bridge = AndroidDebugBridge.getBridge();


        Logger LOG = Logger.getLogger(AdbChimpDevice.class.getName());
        LOG.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                long tid = Thread.currentThread().getId();
                if(!logMap.containsKey(tid))
                    logMap.put(tid,new LinkedList<String>());
                logMap.get(tid).add(record.getMessage());
            }

            @Override
            public void flush() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void close() throws SecurityException {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }


    private IChimpDevice mDevice;

    private Device(IChimpDevice d){
        mDevice = d;
    }

    public static Device waitForConnection(long timeout, String identifier, int port){

        Pattern pattern = Pattern.compile(identifier);

        while(!bridge.isConnected() || !bridge.hasInitialDeviceList()){
            try{
                Thread.sleep(200);
            }
            catch(Exception e){}
        }

        IDevice target = null;
        for(IDevice device : bridge.getDevices()){
            String serialNumber = device.getSerialNumber();
            if(pattern.matcher(serialNumber).matches()){
                    target = device;
                    break;
            }
        }
        if(target == null){
            throw new RuntimeException("Cannot find device!");
        }
        try{
            target.createForward(port,13338);
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Cannot make port forwarding!");
        }

        IChimpDevice device = mChimpChat.waitForConnection(timeout, identifier);
        if(device == null) return null;
        return new Device(device);
    }

    public boolean touch(int x, int y, TouchPressType type){
        mDevice.touch(x,y,type);
        LinkedList<String> log = pollLog();
        for(String s:log){
            System.out.println(s);
        }
        return true;
    }

    public boolean press(PhysicalButton button, TouchPressType type){
        mDevice.press(button, type);
        LinkedList<String> log = pollLog();
        for(String s:log){
            System.out.println(s);
        }
        return true;
    }

    public boolean type(String string){
        mDevice.type(string);
        LinkedList<String> log = pollLog();
        for(String s:log){
            System.out.println(s);
        }
        return true;
    }

    public boolean press(String key, TouchPressType type){
        mDevice.press(key,type);
        LinkedList<String> log = pollLog();
        //TODO
        return true;
    }

    public boolean wake(){
        mDevice.wake();
        LinkedList<String> log = pollLog();
        //TODO
        return true;
    }

    public boolean startActivity(String s1, String s2, String s3, String s4, Collection<String> opt1, Map<String, Object> opt2, String s5, int i){
        mDevice.startActivity(s1,s2,s3,s4,opt1,opt2,s5,i);
        LinkedList<String> log = pollLog();
        for(String s:log){
            System.out.println(s);
        }
        return true;
    }

    private LinkedList<String> pollLog(){
        long tid = Thread.currentThread().getId();
        LinkedList<String> log = logMap.get(tid);
        logMap.put(tid,new LinkedList<String>());
        return log;
    }
}
