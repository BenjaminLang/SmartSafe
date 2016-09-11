package UBC.G8.SmartSafe;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import UBC.G8.Message.*;


public class AppProcessing {
	
	private Database database;
	private Socket socket;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private AndroidApp app;
	
	//constructor
	public AppProcessing(Database database, Socket socket){
		this.database = database;
		this.socket = socket;
	}
	
	public void process() {
	
		try {
			//creates new input and output streams to communicate with app
			output = new ObjectOutputStream(socket.getOutputStream());
			input = new ObjectInputStream(socket.getInputStream());
			Message message;
			MessageType type;
			
			//gets verification from the app and adds the app into the database
			message = (Message)input.readObject();
			System.out.println("READ HANDSHAKE");
			String appId = message.getAppId();
			this.app = database.addApp(appId);
			
			if(this.app.getInUse()){	//if app already exists, provide feedback to user
				output.writeObject(new ResponseMessage(appId, null, MessageType.HANDSHAKE, ResponseCode.APPALREADYEXISTS));
				output.flush();
				System.out.println("FAILED");
				return;
			
			}else{ //otherwise tell the user it was a success
				output.writeObject(new ResponseMessage(appId, null, MessageType.HANDSHAKE, ResponseCode.SUCCESS));
				output.flush();
				System.out.println("SUCCESS");
			}
			
			System.out.println("ADDED");
			this.app.setInUse(true);
			
			//run loop forever, read incoming messages from input stream and carry out operations
			//once completed, return result back to app
			for(;;){
				try {
					message = (Message)input.readObject(); //blocks until new message arrives
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					continue;
				}
				//based on the message type, perform different operations
				type = message.getMessageType();
				switch(type){
					case UNLOCKREQUEST : {
						database.getSafe(message.getSafeId()).logRequests("Unlock attempt");
						this.processLockMessage((ChangeLockMessage)message);
						break;
						}
					case MAPSAFETOAPPREQUEST : {
						this.processMapSafeRequestApp(message.getAppId(), message.getSafeId());
						database.getSafe(message.getSafeId()).logRequests("Safe connected");
						output.writeObject(new ResponseMessage(message.getAppId(), message.getSafeId(), type, ResponseCode.SUCCESS));
						break;
					}
					case DRAWERREQUEST : {
						this.processDrawerReqestApp(message);
						break;
						}
					case CHANGESETTINGSREQUEST : {
						database.getSafe(message.getSafeId()).logRequests("Settings change");
						this.proscessChangeSettings(message.getAppId(), message.getSafeId() , (ChangeSettingsMessage) message);
						break;
						}
					case LOCKREQUEST : {
						database.getSafe(message.getSafeId()).logRequests("Lock attempt");
						this.processLockMessage((LockMessage)message);
						break;
					}
					case SAFESTATUSREQUEST : {
						this.processSafeStatusApp(message);
						break;
					}
					case HISTORYREQUEST : {
						this.processHistoryRequestApp(message);
						break;
					}
					default:
						break;
					};
					
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			this.app.setInUse(false);
		} catch (ClassNotFoundException e1) {
			this.app.setInUse(false);
			e1.printStackTrace();
		} catch (NullPointerException e2) {
			this.app.setInUse(false);
			e2.printStackTrace();
		}
		//if an exception occurs, tell the database that the app is no longer in use
		this.app.setInUse(false);
	}
	
	/**
	 * Method to change the settings on the Safe 
	 */
	private void proscessChangeSettings(String appId, String safeId, ChangeSettingsMessage message) throws IOException {
		//Find the safe to put the message in
		Safe target = database.getSafe(safeId);
		ResponseMessage response = null;
		
		try {
			//place the request in the safe's queue
			target.recvQueue.put(message);
			//wait for a response
			response = database.getApp(appId).recvQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//send the response back
		output.writeObject(response);
		output.flush();
	}

	/**
	 * Moves the safe from the unmapped list to a mapping between app and multiple safes.
	 */
	private void processMapSafeRequestApp(String appId, String safeToMap){
		for (Safe s : database.getSafesFromApp(appId)) {
			if (s.getId().equals(safeToMap)) {
				//mapping already exists
				return;
			}
		}
		//otherwise, move the safe to the mapping
		database.moveUnmappedSafeToMapped(safeToMap, appId);
		return;
	}
	
	private void processLockMessage(Message message) throws IOException{
		ResponseMessage response;
		//This message is to be sent to the safe processing thread
		//place the message into the designated safe's blocking queue
		for (Safe s : database.getSafesFromApp(message.getAppId())) {
			if (s.getId().equals(message.getSafeId())) {
				s.recvQueue.add(message);
			}
		}
		try {
			
			//checks to see if safe side of server was interrupted
			if (database.getApp(message.getAppId()).isSafeInterrupted()){
				throw new InterruptedException();
			}
			
			//put my message, set request enum, wait 
			response = database.getApp(message.getAppId()).recvQueue.take();		
			output.writeObject(response);
			output.flush();
			
		} catch (InterruptedException e) {
			//clear blocking queue if this exception occurs and send back appropriate response
			AndroidApp app = database.getApp(message.getAppId());
			app.setSafeInterrupted(false);
			app.recvQueue.clear(); //if internal error occurs, flush out the block queue
			response = new ResponseMessage(app.getId(), message.getSafeId(), MessageType.RESPONSE, ResponseCode.INTERNALERROR);
			output.writeObject(response);
			output.flush();
		}
		return;
	}
	/*
	 * Method to help the app populate its "drawer"
	 * Returns an message containing an arraylist of the mapped safes
	 */
	private void processDrawerReqestApp(Message message){
		DrawerResponseMessage response = new DrawerResponseMessage(null, null, MessageType.DRAWERREQUEST);
		//places an arraylist of safe IDs into the message
		for(Safe s : database.getSafesFromApp(message.getAppId())){
			response.addSafes(s.getId());
		}
		try {
			//message gets sent back
			output.writeObject(response);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * Method to process status update request from the App
	 */
	private void processSafeStatusApp(Message message){
		//find the desired safe
		Safe s = database.getSafe(message.getSafeId());
		StatusResponse response = new StatusResponse(null, null, ResponseCode.SUCCESS);
		//set the fields to hold the current state of the safe
		response.setLocked(s.hasLocked());
		response.setMotion(s.isMotionSensor());
		response.setNumTries(s.getMaxTries());
		response.setAutolock(s.getAutolock());
		try {
			//write back
			output.writeObject(response);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * Method to process history request from the App
	 * Gets the logging data as an ArrayList of strings and returns this to the app via a message.
	 */
	private void processHistoryRequestApp(Message message) throws FileNotFoundException{
		HistoryResponse response = new HistoryResponse(null, null, MessageType.HISTORYREQUEST, ResponseCode.SUCCESS);
		//place all the logging data into the message.
		response.setLogs(database.getSafe(message.getSafeId()).retrieveLog());
		try {
			output.writeObject(response);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
