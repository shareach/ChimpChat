package edu.berkeley.wtchoi.cc.driver;

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

    public static DriverPacket getRequestTI(){
        return new DriverPacket(Type.RequestTI);
    }

    public static DriverPacket getAckCommand() {
        return new DriverPacket(Type.AckCommand);
    }

    public static DriverPacket getAckStable() {
        return new DriverPacket(Type.AckStable);
    }

    public static DriverPacket getAckStop(){
        return new DriverPacket(Type.AckStop);
    }

    public static DriverPacket getSetOptions(int[] opt){
        return new DriverPacket(Type.SetOptions, opt);
    }

    public static DriverPacket getViewInfo(ViewInfo mv){
        return new DriverPacket(Type.ViewInfo, mv);
    }

    public static <T> DriverPacket getTransitionInfo(T ti){
        return new DriverPacket(Type.TI, ti);
    }

    public static DriverPacket getEnterEditText(int vid, String content){
        Object[] obj = new Object[2];
        obj[0] = vid;
        obj[1] = content;
        return new DriverPacket(Type.EnterEditText, obj);
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
        throw new RuntimeException("Wrong DriverPacket!:"+this.toString());
    }

    public ViewInfo getView(){
        if(this.getType() == Type.ViewInfo){
            return (ViewInfo) piggyback;
        }
        throw new RuntimeException("Wrong DriverPacket!:"+this.toString());
    }

    public int getViewId(){
        if(this.getType() == Type.EnterEditText){
            return (Integer) ((Object[]) piggyback)[0];
        }
        throw new RuntimeException("Wrong DriverPacket:"+this.toString());
    }

    public <T> T getTI(){
        if(this.getType() == Type.TI){
            return (T) piggyback;
        }
        throw new RuntimeException("Wrong DriverPacket:"+this.toString());
    }

    public String getString(){
        if(this.getType() == Type.EnterEditText){
            return (String) ((Object[]) piggyback)[1];
        }
        throw new RuntimeException("Wrong DriverPacket:"+this.toString());
    }

    public static enum OptionIndex{
        ITickCount,
        IStableCount,
        ITickInterval,
        ITickSnooze;
    }


    public static enum Type {
        Ack("Ack"),
        AckCommand("AckCommand"),
        RequestView("RequestView"),
        RequestTI("RequestTI"),
        SetOptions("SetOptions"),
        Reset("Reset"),
        EnterEditText("EnterEditText"),

        // DriverPacket from Application
        AckStable("AckStack"),
        ViewInfo("viewInfo"),
        TI("TransitionInfo"),
        AckStop("AckStop");

        final String name;

        private Type(String string){
            this.name = string;
        }

        @Override
        public String toString(){
            return name;
        }

    }

}
