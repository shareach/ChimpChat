package edu.berkeley.wtchoi.cc;

import java.io.Serializable;

public class Packet implements Serializable{

    private static final long serialVersionUID = -5186309675577891457L;

    public enum Type {
        Ack, Reset,
        // Packet from MonkeyControl
        AckCommand, RequestView,

        // Packet from Application
        AckStable;

        public String toString(){
            switch(this){
                case Ack: return "Ack";
                case Reset: return "Reset";
                case AckCommand: return "AckCommand";
                case RequestView: return "RequestView";
                case AckStable: return "AckStable";
                default:
                    return null;
            }
        }
    }

    private static int id_next = 0;
    private int id;
    private Type type;

    private Packet(Type t) {
        id = id_next++;
        type = t;
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

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }
}
