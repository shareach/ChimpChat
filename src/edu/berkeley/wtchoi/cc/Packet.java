package edu.berkeley.wtchoi.cc;

import java.io.Serializable;

public class Packet implements Serializable{

    private static final long serialVersionUID = -5186309675577891457L;

    public enum Type {
        Ack{
            public String toString(){
                return "Ack";
            }
        },

        // Packet from MonkeyControl
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

        // Packet from Application
        AckStable{
            public String toString(){
                return "AckStack";
            }
        },

        Reset{
            public String toString(){
                return "Reset";
            }
        };
    }

    private static int id_next = 0;
    private int id;
    private Type type;
    private Object piggyback;

    private Packet(Type t) {
        id = id_next++;
        type = t;
        piggyback = null;
    }

    private Packet(Type t, Object back){
        id = id_next++;
        type = t;
        piggyback = back;
    }


    public static Packet getAck() {
        return new Packet(Type.Ack);
    }

    public static Packet getReset() {
        return new Packet(Type.Reset);
    }

    public static Packet getRequestView() {
        return new Packet(Type.RequestView);
    }

    public static Packet getAckCommand() {
        return new Packet(Type.AckCommand);
    }

    public static Packet getAckStable() {
        return new Packet(Type.AckStable);
    }

    public static Packet getSetOptions(TestingOptions options){
        return new Packet(Type.SetOptions, options);
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public TestingOptions getTestingOption(){
        if(this.getType() == Type.SetOptions){
            return (TestingOptions)piggyback;
        }
        return null;
    }
}
