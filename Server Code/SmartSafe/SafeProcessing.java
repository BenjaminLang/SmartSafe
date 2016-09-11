package UBC.G8.SmartSafe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.soap.AddressingFeature.Responses;

import UBC.G8.Message.ChangeLockMessage;
import UBC.G8.Message.ChangeSettingsMessage;
import UBC.G8.Message.LockMessage;
import UBC.G8.Message.Message;
import UBC.G8.Message.MessageType;
import UBC.G8.Message.ResponseCode;
import UBC.G8.Message.ResponseMessage;
import UBC.G8.Message.SettingType;
import UBC.G8.Message.SettingsResponse;


public class SafeProcessing {

	private Database database;
	private Socket socket;
	private Safe safe;
	private AndroidApp app;
	
	private BufferedReader input;
	private BufferedWriter output;
	final int serverMessageLength = 6;
	
	String server_auth_success = "00100";
	String server_auth_incorrect_message = "00101";
	String server_auth_incorrect_length = "00102";
	String server_transmit_request_header = "002";
	String server_unlock_request_header = "010";
	String server_unlock_response_header = "011";
	String server_lock_request = "02000";
	String server_lock_response_header = "021";
	private final String server_change_setting_header = "04";
	
	private final int auto_lock = 0;
	private final int password_attempt = 1;
	private final int motion_alarm = 2;
	private final int change_password = 3;
	
	final int server_unlock_failed_safe_password = 1;
	final int server_unlock_failed_safe_timeout= 3;
	final int server_unlock_failed_already_unlocked= 4;
	final int server_unlock_failed_ardu_password= 2;
	
	final int server_lock_success= 0;
	final int server_lock_failed_already_locked= 4;
	final int server_lock_failed_door_open= 5;
	
	final int server_change_setting_success =  0;
	final int server_change_setting_failed = 1;
	final int server_change_setting_failed_locked = 2;
	final int server_change_setting_failed_value_malformed = 3;
	final int server_change_setting_failed_pasword_short = 4;
	
	private final int safe_door_open_message = 30;
	private final int safe_door_closed_message = 40;
	private final int safe_autolock= 10;
	private final int safe_message_alarm_enabled = 20;
	private final int safe_message_alarm_disabled = 50;
	
	
	

	public SafeProcessing(Database database, Socket socket, BufferedReader input, BufferedWriter output)
	{
		this.database = database;
		this.socket = socket;
		this.input = input;
		this.output = output;
	}
	
public void processSafe () throws IOException {
		
		//Receive the authentication token
		try {
			authenticateSafe();
		} catch (AuthenticationFailedException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			return;
		}
		
		
		//inform the safe that it can now transmit data
		transmitRequest();
		
		//set timeout of the socket to 1000ms for each operation
		socket.setSoTimeout(1000);
		
		for(;;)
		{
			
			socket.setSoTimeout(1000);
			//creates the message buffer
			char recvBuffer[] = new char[serverMessageLength];
			
			
			//handle receiving Alert Data from the safe
			try
			{
				//check buffer for messages from PI
				int recieved_data = input.read(recvBuffer, 0, serverMessageLength);
				
				if (recieved_data  < serverMessageLength)
				{
					//some error code
				}
				socket.setSoTimeout(0);


			}
			catch (SocketTimeoutException e)
			{
				//System.out.println("TIMEOUT!");
			}
			
			
			//handle receiving Messages from the APP

			try 
			{
				socket.setSoTimeout(0);
				handleRequests();
			} 
			catch (InterruptedException e) 
			{
				//set flag in android object that an exception was thrown
				//server can relay info back to device
				database.getCorrespondingApp(safe.getId()).setSafeInterrupted(true);
			} catch (MalformedResponseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			
		}
		
	}
	
	private boolean handleSafe(String string) {
		
		int message = Integer.parseInt(string.substring(0,2).trim());
		
		switch (message)
		{
			case safe_door_open_message:
				safe.setDoorOpen(true);
				safe.logRequests("Door Open");
				break;
			case safe_door_closed_message:
				safe.setDoorOpen(false);
				safe.logRequests("Door Closed");
				break;
			case safe_autolock:
				safe.setLocked(true);
				safe.logRequests("Safe has autolocked");
				break;
			case safe_message_alarm_enabled:
				safe.setAlarmend(true);
				safe.logRequests("SAFE HAS TRIGGERED THE ALARM");
				break;
			case safe_message_alarm_disabled:
				safe.setAlarmend(false);
				safe.logRequests("SAFE HAS STOPPED THE ALARM");
				break;
		}
		
	return true;
	
}

	private boolean authenticateSafe() throws IOException, AuthenticationFailedException{
		
		
		char recvMessage[] = new char[serverMessageLength];
		
		input.read(recvMessage, 0, serverMessageLength);
		
		String message = new String(recvMessage);
		//check that we received the correct message
		if ( !message.startsWith("00"))
		{
			//TODO LOG
			output.write(server_auth_incorrect_message);
			output.flush();
			throw(new AuthenticationFailedException(0, "Did not recv correct authenticcation token"));
		}
		
		//check length of name		
		int IDLength = Integer.parseInt(message.substring(3,5).trim());
		
		char safeID[] = new char[IDLength];
		
		//read the ID
		int safeIDLength = input.read(safeID, 0, IDLength);
		
		if ( safeIDLength != IDLength)
		{
			//TODO LOG
			output.write(server_auth_incorrect_length);
			output.flush();
			throw(new AuthenticationFailedException(1, "Did not recv correct length safe name"));
		}
		
		//tell the safe that it has been accepted
		output.write(server_auth_success);
		output.flush();
		//create the new safe
		String safeIdTrimmed = new String(safeID).trim();
		
		System.out.println(safeIdTrimmed);
		
		safe = database.getSafe(safeIdTrimmed);
		
		if(safe != null){
			return true;
		}
		
		
		safe = new Safe(safeIdTrimmed);
		//add safe to list
		database.addToUnmapped(safe);
		
		//wait for the safe to be added
		synchronized (safe) {
		    try {
				safe.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	

	private void transmitRequest() throws IOException {
		
		//build the transmit message
		StringBuilder transmitMessage = new StringBuilder();
		transmitMessage.append(server_transmit_request_header);
		//get our app's ID
		app = database.getCorrespondingApp(safe.getId());
		String appID = app.getId();;
		int appIDLength = appID.length() + 1;
		if (appIDLength < 8)
			transmitMessage.append("0");
		transmitMessage.append(appIDLength);
		output.write(transmitMessage.toString());
		output.flush();
		output.write(appID.trim());
		output.flush();
	}


	private void handleRequests() throws IOException, InterruptedException, MalformedResponseException {
			socket.setSoTimeout(0);
			Message appMessage= safe.recvQueue.poll(100, TimeUnit.MILLISECONDS);
			if (appMessage == null)
				return;
			
			switch (appMessage.getMessageType()) 
			{
				case UNLOCKREQUEST:
					if (safe.hasLocked())
						unlockRequest((ChangeLockMessage) appMessage);
					else
						app.recvQueue.put(new ResponseMessage(app.getId(), safe.getId(), appMessage.getMessageType(), ResponseCode.ALREADYUNLOCKED));
					
				break;
				case LOCKREQUEST:
					if (!safe.hasLocked())
						lockRequest((LockMessage) appMessage);
					else
						app.recvQueue.put(new ResponseMessage(app.getId(), safe.getId(), appMessage.getMessageType(), ResponseCode.ALREADYLOCKED));
				break;
				case CHANGESETTINGSREQUEST:
					if (!safe.hasLocked())
						this.proscessChangeSettings(app.getId(), safe.getId(), (ChangeSettingsMessage) appMessage);
					else
						app.recvQueue.put(new ResponseMessage(app.getId(), safe.getId(), appMessage.getMessageType(), ResponseCode.ALREADYLOCKED));
				break;
				default:
				break;
			}
	}

	
	private void lockRequest(LockMessage appMessage) throws IOException, InterruptedException, MalformedResponseException {
		//check that the app id + safe id are correct!
		if ( !appMessage.getAppId().equals(app.getId())  &&  !appMessage.getSafeId().equals(safe.getId()) )
		{
			//tell the app that it is targeting the wrong safe
			app.recvQueue.put(new ResponseMessage(app.getId(), safe.getId(), appMessage.getMessageType(), ResponseCode.WRONGTARGET));
			return;
		}
		
		//send request
		output.write(server_lock_request);
		output.flush();	
		
		//await reply  
		char lockRequestResponse[] = new char[serverMessageLength];
		
		int responseLength = input.read(lockRequestResponse, 0, serverMessageLength);
		
		//check if we received the correct length message
		if (responseLength < serverMessageLength)
		{
			app.recvQueue.put(new ResponseMessage(app.getId(), safe.getId(), appMessage.getMessageType(), ResponseCode.PIERROR));
			throw (new MalformedResponseException(0, "unexpected message length"));
		}
		
		String RequestResponse = new String(lockRequestResponse);
		
		if (!RequestResponse.startsWith(server_lock_response_header))
		{
			//TODO LOG
			app.recvQueue.put(new ResponseMessage(app.getId(), safe.getId(), appMessage.getMessageType(), ResponseCode.WRONGRESPONSE));
			return;
		}
		
		int piResponse = Integer.parseInt(RequestResponse.substring(3,5).trim());
		app.recvQueue.put(new ResponseMessage(app.getId(), safe.getId(), appMessage.getMessageType(), translateLock(piResponse)));
	}

	private void unlockRequest(ChangeLockMessage appMessage) throws IOException, InterruptedException {
		//check that the app id + safe id are correct!
		if ( !appMessage.getAppId().equals(app.getId())  &&  !appMessage.getSafeId().equals(safe.getId()) )
		{
			//tell the app that it is targeting the wrong safe
			app.recvQueue.put(new ResponseMessage(app.getId(), safe.getId(), appMessage.getMessageType(), ResponseCode.WRONGTARGET));
			return;
		}
		
		
		
		//Handle locking the user out of for x amount of time if they input the password wrong one too many times
		if(!safe.isLockedOut()){
		//compare number of tries
			if(safe.getNumberOfTries() > safe.getMaxTries()){
				//if client has exceeded max number of tries, lock out safe for time period
				safe.lockoutSafe();
				safe.logRequests("Safe locked out");
				this.sendLockoutResponse();
			}
		}
		else{
			
			if(System.currentTimeMillis() > safe.getEndTime()){
				//lockout is over
				safe.resetTries();
				safe.logRequests("lockout over");
			}
			else {
				this.sendLockoutResponse();
			}
		}
		
		
		//get the password length + null byte
		int passwordLength = appMessage.getPasswordRequest().length() + 1;
		System.out.println(passwordLength);
		//prepare initial request message to send
		StringBuilder passwordRequest = new StringBuilder();
		//add the message header
		passwordRequest.append(server_unlock_request_header);
		//add the password length to the message
		if (passwordLength <= 9)
			passwordRequest.append("0");
		passwordRequest.append(passwordLength);
		System.out.println(passwordRequest.toString());
		System.out.println(appMessage.getPasswordRequest());
		//write the message to the safe
		output.write(passwordRequest.toString());
		output.flush();
		//sends the password
		//Thread.sleep(2000);
		output.write(appMessage.getPasswordRequest());
		output.flush();
		System.out.println("send");

		
		//Wait for a for a reply 
		char passwordRequestResponse[] = new char[serverMessageLength];
		
		int responseLength = input.read(passwordRequestResponse, 0, serverMessageLength);
		
		if (responseLength < serverMessageLength)
		{
			app.recvQueue.put(new ResponseMessage(app.getId(), safe.getId(), appMessage.getMessageType(), ResponseCode.PIERROR));
			//TODO: Add throw exception to reset server
		}
		
		String RequestResponse = new String(passwordRequestResponse);
		
		if (!RequestResponse.startsWith(server_unlock_response_header))
		{
			//error code here
		}
		int responseCode = Integer.parseInt(RequestResponse.substring(4,5).trim());
		app.recvQueue.put(new ResponseMessage(app.getId(), safe.getId(), MessageType.RESPONSE, translateUnlock(responseCode)));
	}
	
	private void sendLockoutResponse() throws InterruptedException
	{
		ResponseMessage response = new ResponseMessage(null, null, MessageType.RESPONSE, ResponseCode.LOCKOUT);
		app.recvQueue.put(response);
	}
	
	private void proscessChangeSettings(String appId, String safeId, ChangeSettingsMessage appMessage) throws IOException, InterruptedException {
		
		//check origin and destination
		if ( !appMessage.getAppId().equals(app.getId())  &&  !appMessage.getSafeId().equals(safe.getId()) )
		{
			//tell the app that it is targeting the wrong safe
			app.recvQueue.put(new ResponseMessage(app.getId(), safe.getId(), appMessage.getMessageType(), ResponseCode.WRONGTARGET));
			return;
		}
		
		
		Map<SettingType, ResponseCode> responses = new HashMap<SettingType , ResponseCode>();
		for(SettingType setting : appMessage.getSettings())
		{
			ResponseCode response = changeSetting(appMessage, setting);
			responses.put(setting, response);
		}
		
		
		//construct the message
		
		SettingsResponse settingsResponse = new SettingsResponse(appId, safeId, appMessage.getMessageType(), responses);
		
		
		app.recvQueue.put(settingsResponse);
		return;
		
		
	}
	

	private ResponseCode changeSetting(ChangeSettingsMessage appMessage , SettingType setting) throws IOException, InterruptedException {
		//prepare setting change  message
				StringBuilder settingsMessage = new StringBuilder();
				
				settingsMessage.append(server_change_setting_header);
				
				switch(setting)
				{
					case AUTOLOCKCHANGEREQUEST:
						settingsMessage.append(auto_lock);
						settingsMessage.append("0");
						settingsMessage.append(appMessage.isAutolock() ? 1 : 0);
						break;
					case CHANGEPASSWORDREQUEST:
						settingsMessage.append(change_password);
						if (appMessage.getNewPassword().length() + 1 <= 9)
							settingsMessage.append("0");
						settingsMessage.append(appMessage.getNewPassword().length() + 1);
						break;
					case PASSWORDATTEMPTSREQUEST:
						settingsMessage.append(password_attempt);
						if (appMessage.getNewAttempts() <= 9)
							settingsMessage.append("0");
						settingsMessage.append(appMessage.getNewAttempts());
						break;
					case CHANGEALARMREQUEST:
						settingsMessage.append(motion_alarm);
						settingsMessage.append("0");
						settingsMessage.append(appMessage.isAlarmEnable() ? 1 : 0);
						break;
				}
				
				output.write(settingsMessage.toString());
				output.flush();
				if(setting == SettingType.CHANGEPASSWORDREQUEST)
				{
					output.write(appMessage.getNewPassword());
					output.flush();
				}
				
				//now wait for apply
				char settingsResponseRequest[] = new char[serverMessageLength];
				
				int responseLength = input.read(settingsResponseRequest, 0, serverMessageLength);
				
				if (responseLength < serverMessageLength)
				{
					return ResponseCode.PIERROR;
				}
				
				String responseRequest = new String(settingsResponseRequest);
				
				if (!responseRequest.startsWith(server_change_setting_header))
				{
					//error code here
				}
				
				
				int responseCode = Integer.parseInt(responseRequest.substring(3, 5).trim());
				
				return translateSettings(responseCode);
		
	}

	private ResponseCode translateSettings(int responseCode) {
		
		switch (responseCode)
		{
		case server_change_setting_success:
				return ResponseCode.SUCCESS;
		}
		return null;
	}

	private ResponseCode translateLock(int response) {
		switch (response)
		{
			case server_lock_success :
				safe.setLocked(true);
				safe.logRequests("Lock Succsess");
				return ResponseCode.SUCCESS;
			case server_lock_failed_already_locked:
				safe.setLocked(true);
				safe.logRequests("safe already locked");
				return ResponseCode.ALREADYLOCKED;
			case server_lock_failed_door_open:
				safe.setLocked(false);
				safe.logRequests("safe already locked");
				return ResponseCode.DOOROPEN;
				
		}
		return null;
	}

	private ResponseCode translateUnlock(int response) {
		
		switch (response)
		{
			case 0 :
				safe.setLocked(false);
				safe.logRequests("Unlock Succsess");
				return ResponseCode.SUCCESS;
			case server_unlock_failed_safe_password:
				safe.setLocked(true);
				safe.logRequests("Wrong Safe password");
				return ResponseCode.WRONGSAFEPASSWORD;
			case server_unlock_failed_safe_timeout:
				safe.setLocked(true);
				safe.logRequests("Safe Didnt recieve password in time");
				return ResponseCode.TIMEOUT;
			case server_unlock_failed_already_unlocked:
				safe.setLocked(true);
				safe.logRequests("Safe already locked");
				return ResponseCode.ALREADYUNLOCKED;
			case server_unlock_failed_ardu_password:
				safe.setLocked(true);
				safe.logRequests("Wrong password on the arduino");
				return ResponseCode.WRONGARDUINOPASSWORD;
		}
		return null;
	}
}
