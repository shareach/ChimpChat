package edu.berkeley.wtchoi.cc.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 3:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class TcpChannel<Packet> {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Socket socket;

    private int port;
    private String ip = null; // -1 indicate server mode

    //used for asynchronous connection
    private ChannelInitiator __initiator;

    private TcpChannel(){}



    public static TcpChannel getServerSide(int port){
        TcpChannel ch = new TcpChannel();
        ch.port = port;
        return ch;
    }

    public void connect(){
        if(ip == null) listenForClient();
        else connectToServer();
    }

    private void listenForClient(){
        ServerSocket serverSocket;

        try {
            serverSocket = new java.net.ServerSocket(port);
        }
        catch(IOException e){return;}

        try{
            socket = serverSocket.accept();
            ois = new java.io.ObjectInputStream(socket.getInputStream());
            oos = new java.io.ObjectOutputStream(socket.getOutputStream());
        }
        catch(IOException e){
            try{serverSocket.close();} catch(Exception ee){}
        }
    }

    private void connectToServer(){
        try{
            socket = new Socket(ip,port);
            ois = new java.io.ObjectInputStream(socket.getInputStream());
            oos = new java.io.ObjectOutputStream(socket.getOutputStream());
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Cannot connect to server");
        }
    }

    public void connectAsynchronous(){
        __initiator = new ChannelInitiator(this);
        __initiator.start();
    }

    public void waitConnection(){
        try{
            __initiator.join();
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Cannot initiate channel");
        }
    }


    public static TcpChannel getClientSide(String ip, int port){
        if(ip == null)
            throw new RuntimeException("Client side channel cannot have empty target address");

        TcpChannel ch = new TcpChannel();
        ch.ip = ip;
        ch.port = port;

        return null;
    }


    public void sendPacket(Packet p){
        try{
            oos.writeObject(p);
        }
        catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot send an object");
        }
    }

    public Packet receivePacket(){
        try{
            Object obj = ois.readObject();
            return (Packet) obj;
        }
        catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot read an object");
        }
        catch (java.lang.ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot read an object");
        } catch(ClassCastException e){
            e.printStackTrace();
            throw new RuntimeException("Arrived object is not a packet");
        }
    }


    private static class ChannelInitiator extends Thread {
        private TcpChannel channel;

        public ChannelInitiator(TcpChannel ch){
            channel = ch;
        }

        public void run() {
            channel.connect();
        }
    }
}


