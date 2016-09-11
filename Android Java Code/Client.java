package androidapp.elec291group.smartsafe;


import android.content.Context;
import android.os.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import UBC.G8.Message.*;
import UBC.G8.Message.Message;

/**
 * Created by Ben on 22/03/2016.
 */
public class Client {

    //IO types used for communication
    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private BufferedWriter outputString;
    private InetAddress ipAddress;
    private int portNumber;

    //constructor
    public Client() throws IOException {
        MyDataBridges bridge = MyDataBridges.getInstance();
        ipAddress = InetAddress.getByName(bridge.getIP());
        portNumber = bridge.getPort();

        //starts the communication thread upon construction of object
        Thread thread = new Thread(new Communicate());
        thread.start();
    }

    class Communicate implements Runnable {
        @Override
        public void run() {
            try {
                //creates the new socket
                System.out.println("enter thread");
                clientSocket = new Socket(ipAddress, portNumber);

                System.out.println("created socket");
                outputString = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                System.out.println("connected");

                //writes a string containing app to the server so that the server can choose the correct
                //for this client type
                String app = "app\n";
                outputString.write(app, 0, 4);
                outputString.flush();

                //initializes the IO streams for the remainder of the communication
                output = new ObjectOutputStream(clientSocket.getOutputStream());
                input = new ObjectInputStream(clientSocket.getInputStream());
                
                MyDataBridges bridge = MyDataBridges.getInstance();

                //sends out the handshake message
                output.writeObject(new Message("asdf1234", bridge.getCurrSafeID(), MessageType.HANDSHAKE));
                output.flush();
                input.readObject();

                for(;;){
                    System.out.println("got to loop");
                    Message message = null;
                    ResponseMessage response = null;
                    //polls the outgoign blocking queue for messages
                    message = bridge.retrieveOutGoing();
                    if(!(message == null)){
                        //when it gets a message, it processes it and then waits for a response
                        //which it places into the incoming blocking queue to the UI thread
                        response = communicateMessage(message);
                        bridge.sendInComing(response);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    //method to send and receive message types to the server through socket connection
    public ResponseMessage communicateMessage(Message message) {
        ResponseMessage response = null;
        try{
            //sends the outgoing message
            output.writeObject(message);
            output.flush();
            //blocks until it receives a response
            response = (ResponseMessage) input.readObject();
        } catch (IOException e) {
            System.out.println("verifying did not occur");
            return response;
        } catch(ClassNotFoundException e){
            System.out.println("class not found");
            return response;
        }
        return response;
    }

}

