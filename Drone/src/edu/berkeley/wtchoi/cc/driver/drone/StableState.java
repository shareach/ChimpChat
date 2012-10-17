package edu.berkeley.wtchoi.cc.driver.drone;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import edu.berkeley.wtchoi.cc.driver.DriverPacket;
import edu.berkeley.wtchoi.cc.driver.ViewInfo;
import edu.berkeley.wtchoi.cc.learnerImp.ctree.TransitionInfo;
import edu.berkeley.wtchoi.logger.Logger;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 5:54 PM
 * To change this template use File | Settings | File Templates.
 */

public class StableState extends AbstractState {
    private AbstractState next;

    public StableState(SupervisorImp s){
        super(s);
        next = this;
    }

    public void work(){
        Log.d("wtchoi", "Stable State");

        //get command packet
        DriverPacket packet = s.channel.receivePacket();
        //Log.d("wtchoi", "packet received:" + packet.getType().toString());

        //handle packet

        switch(packet.getType()){
            case AckCommand:
                next = new UnstableState(s);
                break;
            case RequestView:
            {
                ViewInfo info = getView();
                s.channel.sendPacket(DriverPacket.getViewInfo(info));
                break;
            }

            case RequestTI:
            {
                TransitionInfo info = s.transitionInfo;
                Logger.log("" + info.didNothing());
                s.channel.sendPacket(DriverPacket.getTransitionInfo(info));
                break;
            }

            case Reset:
                s.closeApplication();

            case SetOptions:
                int[] options  = packet.getDriverOption();
                s.TICKCOUNT    = options[DriverPacket.OptionIndex.ITickCount.ordinal()];
                s.TICKINTERVAL = options[DriverPacket.OptionIndex.ITickInterval.ordinal()];
                s.TICKSNOOZE   = options[DriverPacket.OptionIndex.ITickSnooze.ordinal()];
                s.STABLECOUNT  = options[DriverPacket.OptionIndex.IStableCount.ordinal()];
                s.channel.sendPacket(DriverPacket.getAck());
                break;

            default:
                throw new RuntimeException("Wrong Packet");
        }
    }

    private void enterText(int id, String content){
        try{
            View[] views = getViewRoots();

            View t = null;
            for(View v: views){
                t = v.findViewById(id);
                if(t != null) break;
            }
            if(t == null) return;

            if(t instanceof EditText){
                ((EditText) t).setText(content);
            }
        }catch(Exception e){

        }
    }

    private ViewInfo getView(){
        //View Hierarchy Analysis
        try{
            View[] views = getViewRoots();
            LinkedList<ViewInfo> vlist = new LinkedList<ViewInfo>();

            for(View v: views){
                //Log.d("wtchoi!","<<" + v.getWidth() + "," + v.getHeight() + ">>");
                vlist.addFirst(ViewTransformer.fromView(v));
            }

            //Embed view roots into virtual global view root
            ViewInfo root = new ViewInfo(0,0,s.getScreenX(),s.getScreenY(),vlist);

            return root;
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Cannot access view information");
        }
    }

    private View getActivityViewRoot(Activity a){
        Window rootW = a.getWindow();
        while(true){
            if(rootW.getContainer() == null) break;
            rootW = rootW.getContainer();
        }
        return rootW.getDecorView();
    }

    private View[] getViewRoots()
            throws ClassNotFoundException, SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        //Code snippet from ROBOTIUM
        Class<?> windowManagerImpl = Class.forName("android.view.WindowManagerImpl");
        Field viewsField = windowManagerImpl.getDeclaredField("mViews");

        String windowManagerString;
        if(android.os.Build.VERSION.SDK_INT >= 13)
            windowManagerString = "sWindowManager";
        else
            windowManagerString = "mWindowManager";

        Field instanceField = windowManagerImpl.getDeclaredField(windowManagerString);

        viewsField.setAccessible(true);
        instanceField.setAccessible(true);
        Object instance = instanceField.get(null);
        return (View[]) viewsField.get(instance);
    }

    public synchronized AbstractState next(){ return next; }
}
