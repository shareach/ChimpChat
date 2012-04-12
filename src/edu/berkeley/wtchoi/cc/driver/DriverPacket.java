package edu.berkeley.wtchoi.cc.driver;

import edu.berkeley.wtchoi.cc.driver.ViewInfo;
import edu.berkeley.wtchoi.cc.driver.ViewInfo;

import java.io.Serializable;

public class DriverPacket implements Serializable{

    private static final long serialVersionUID = -5186309675577891457L;

    private static int id_next = 0;
    private int id;
    private Type type;
    private Object piggyback;

    private DriverPacket(Type t) {
        id = id_next++;
        type = t;
        piggyback = null;
    }

    private DriverPacket(Type t, Object back){
        id = id_next++;
        type = t;
        piggyback = back;
    }


    public static DriverPacket getAck() {
        return new DriverPacket(Type.Ack);
    }

    public static DriverPacket getReset() {
        return new DriverPacket(Type.Reset);
    }

    public static DriverPacket getRequestView() {
        return new DriverPacket(Type.RequestView);
    }

    public static DriverPacket getAckCommand() {
        return new DriverPacket(Type.AckCommand);
    }

    public static DriverPacket getAckStable() {
        return new DriverPacket(Type.AckStable);
    }

    public static DriverPacket getSetOptions(int[] opt){
        return new DriverPacket(Type.SetOptions, opt);
    }

    public static DriverPacket getViewInfo(ViewInfo mv){
        return new DriverPacket(Type.ViewInfo, mv);
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public int[] getDriverOption(){
        if(this.getType() == Type.SetOptions){
        return (int[]) piggyback;
    }
        throw new RuntimeException("Wrong DriverPacket!");
    }

    public ViewInfo getView(){
        if(this.getType() == Type.ViewInfo){
        return (ViewInfo) piggyback;
    }
        throw new RuntimeException("Wrong DriverPacket!");
    }

    public static enum OptionIndex{
        ITickCount,
        IStableCount,
        ITickInterval,
        ITickSnooze;
    }

    public static enum Type {
        Ack{
            public String toString(){
                return "Ack";
            }
        },

        // DriverPacket from IDriver
        AckCommand{
            public String toString(){
                return "AckCommand";
            }
        },

        RequestView{
            public String toString(){
                return "ReqeustView";
            }
        },

        SetOptions{
            public String toString(){
                return "SetOptions";
            }
        },

        Reset{
            public String toString(){
                return "Reset";
            }
        },

        // DriverPacket from Application
        AckStable{
            public String toString(){
                return "AckStack";
            }
        },

        ViewInfo{
            public String toString(){
                return "ViewInfo";
            }
        };
    }

}
