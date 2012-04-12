package edu.berkeley.wtchoi.cc.util;

import android.util.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

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
    private String ip = null; // null indicate server mode

    private int tryCount = 1;
    private int tryInterval = 100;

    //used for asynchronous connection
    private Thread __initiator;

    private TcpChannel(){}



    public static TcpChannel getServerSide(int port){
        TcpChannel ch = new TcpChannel();
        ch.port = port;
        return ch;
    }

    public void connect(){
        if(ip == null){
            listenForClient();
        }
        else{
            connectToServer();
        }
    }

    private void listenForClient(){
        ServerSocket serverSocket;

        try {
            serverSocket = new java.net.ServerSocket(port);
        }
        catch(IOException e){return;}

        try{
            System.out.println("TcpChannel is Listening");
            socket = serverSocket.accept();
            System.out.println("TcpChannel connected");

            OutputStream os = socket.getOutputStream();
            os.flush();
            oos = new java.io.ObjectOutputStream(os);

            InputStream is = socket.getInputStream();
            ois = new java.io.ObjectInputStream(is);

            System.out.println("Closing Server Socket");
            serverSocket.close();
        }
        catch(IOException e){
            try{serverSocket.close();} catch(Exception ee){}
        }
    }

    private void connectToServer() {
        //Log.d("wtchoi", "connectToServer:" + ip);
        try {
            int i;
            for(i = 0 ; i < tryCount ; i++){
                try {
                    Thread.sleep(tryInterval);
                    System.out.println(Integer.toString(i+1)+ "trial.");
                    socket = new Socket(ip,port);
                }
                catch(UnknownHostException e){
                    e.printStackTrace();
                    throw new RuntimeException("Wrong ip address");
                }
                catch(IOException e){
                    continue;
                }
                break;
            }
            if(i == tryCount) throw new RuntimeException("Connection timeout!");

            OutputStream os = socket.getOutputStream();
            os.flush();
            oos = new ObjectOutputStream(os);

            ois = new ObjectInputStream(socket.getInputStream());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot connect to server");
        }
    }

    public void connectAsynchronous(){
        __initiator = new Thread(new ChannelInitiator(this));
        __initiator.start();
    }

    public void waitConnection(){
        try{
            System.out.println("Channel Initiating");
            __initiator.join();
            System.out.println("Channel Initiated");
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

        return ch;
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


    private static class ChannelInitiator implements Runnable {
        private TcpChannel channel;

        public ChannelInitiator(TcpChannel ch){
            channel = ch;
        }

        public void run() {
            System.out.println("Channel Initiation Start");
            channel.connect();
            System.out.println("Channel Initiation Done");
            return;
        }
    }

    public void setTryCount(int i){
        tryCount = i;
    }

    public void setTryInterval(int i){
        tryInterval = i;
    }
}


