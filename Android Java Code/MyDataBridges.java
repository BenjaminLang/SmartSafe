package androidapp.elec291group.smartsafe;

import android.app.Application;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import UBC.G8.Message.*;

/**
 * Created by Ben on 28/03/2016.
 * singleton global variable class
 */
public class MyDataBridges {

    private static MyDataBridges instance;

    //global fields
    private ArrayBlockingQueue<Message> outGoing = new ArrayBlockingQueue<Message>(5);
    private ArrayBlockingQueue<Message> inComing = new ArrayBlockingQueue<Message>(5);
    private boolean threadRunning = false;
    private String currSafeID = "chief";
    private String IP = "128.189.252.56";
    private int port = 42060;
    private boolean connectionState = false;

    //various getter and setter methods

    private MyDataBridges(){} //should never be called more than once

    public boolean getThreadRunning(){
        return this.threadRunning;
    }

    public void setThreadRunning(){
        threadRunning = true;
    }

    public String getCurrSafeID(){
        return this.currSafeID;
    }

    public void setCurrSafeID(String id){
        currSafeID = id;
    }

    public String getIP(){ return this.IP; }

    public void setIP(String userIP) { IP = userIP; }

    public int getPort(){ return this.port; }

    public void setPort(int userPort){ port = userPort; }

    public boolean getConnectionState(){ return this.connectionState; }

    public void setConnectionState(boolean setState){ connectionState = setState; }

    public static synchronized MyDataBridges getInstance(){
        //returns the instance of this object
        //if once does not yet exist, initailize one
        if(instance == null){
            instance = new MyDataBridges();
        }
        return instance;
    }

    //various methods to send and receive message types on blocking queues
    public void sendOutGoing (Message message){
        try {
            outGoing.offer(message, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public Message retrieveOutGoing(){
        try {
            return outGoing.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        return null;
    }
    public boolean checkIfIncoming(){
        if(inComing.peek() == null)
            return false;
        else
            return true;
    }

    public void sendInComing (Message message){
        try {
            inComing.put(message);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public Message retrieveInComing(){
        try {
            return inComing.take();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        return null;
    }
}
