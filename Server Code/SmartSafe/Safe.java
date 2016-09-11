package UBC.G8.SmartSafe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import UBC.G8.Message.Message;

public class Safe {
	
	//multiple data fields to store its current state
	private String multipurposeMessage;
	private SafeRequestType type;
	private String id;
	private int numberOfTries = 0;
	private int maxTries = 3;
	private long endTime;
	
	private boolean lockedOut = false;
	private boolean lockSignal = false;
	private boolean isLocked = false;
	private boolean autolock = false;
	private boolean motionSensor = false;
	private boolean doorOpen = true;
	private boolean alarmed = false;
	
	//blocking queue and lock to facilitate multi-threaded operations
	public BlockingQueue<Message> recvQueue= new ArrayBlockingQueue<Message>(10);
	private Object mutexlock = new Object();
	
	//date variables used for data logging
	private DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private Calendar calendar = Calendar.getInstance();
	
	//IO types used for data logging
	FileReader in;
	BufferedReader input;
	FileWriter output;
	File file;
	String fileName; 
	
	/*
	 * constructor
	 */
	public Safe(String id){
		this.id = id;
		//upon creation of safe, assign it a file with name specified by Id
		fileName = String.format("%s.txt", this.getId());
		try {
			//initialize its IO types
			file = new File(fileName);
			file.createNewFile();
			output = new FileWriter(file);
			in = new FileReader(file);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void logRequests(String log){
		//use a string builder to create a string which takes in a logging message
		//which includes its timestamp
		StringBuilder builder = new StringBuilder();
		builder.append(log);
		builder.append("|");
		builder.append(format.format(calendar.getTime()));
		builder.append('\n');
		//write to file
		try {
			output.write(builder.toString());
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> retrieveLog() throws FileNotFoundException{
		ArrayList<String> logs = new ArrayList<String>();
		//create new input streams to reset read heads
		in = new FileReader(file);
		input = new BufferedReader(in);
		try {
			String toAdd;
			//until you reach the end of the file, keep on adding lines into ArrayList
			for(;;){
				toAdd = input.readLine();
				if(toAdd == null){
					break;
				}
				logs.add(toAdd);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//return this arraylist
		return logs;
	}
	
	
	//multiple getter and setter methods
	public synchronized  void setDoorOpen(boolean door)
	{
		this.doorOpen = door;
	}
	
	public synchronized  boolean getDoorOpen()
	{
		return doorOpen;
	}
	
	public synchronized  void setAlarmend(boolean alarmState)
	{
		this.alarmed = alarmState;
	}
	
	public synchronized  boolean getAlarmed()
	{
		return alarmed;
	}
	
	
	public String getId(){
		return id;
	}
	
	public boolean getLockSignal() {
		return lockSignal;
	}

	public void setLockSignal(boolean lockSignal) {
		this.lockSignal = lockSignal;
	}

	public int getNumberOfTries() {
		return numberOfTries;
	}

	public void incrementTries() {
		this.numberOfTries++;
	}
	
	public void resetTries(){
		this.numberOfTries = 0;
	}

	public int getMaxTries() {
		return maxTries;
	}

	public void setMaxTries(int maxTries) {
		this.maxTries = maxTries;
	}

	public Object getMutexlock() {
		return mutexlock;
	}

	public boolean hasLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public boolean getAutolock() {
		return autolock;
	}

	public void setAutolock(boolean autolock) {
		this.autolock = autolock;
	}

	public String getMultipurposeMessage() {
		return multipurposeMessage;
	}

	public void setMultipurposeMessage(String multipurposeMessage) {
		this.multipurposeMessage = multipurposeMessage;
	}

	public SafeRequestType getType() {
		return type;
	}

	public void setType(SafeRequestType type) {
		this.type = type;
	}

	public void lockoutSafe(){
		endTime = System.currentTimeMillis() + 300000;
	}

	public long getEndTime() {
		return endTime;
	}

	public boolean isLockedOut() {
		return lockedOut;
	}

	public void setLockedOut(boolean lockedOut) {
		this.lockedOut = lockedOut;
	}

	public boolean isMotionSensor() {
		return motionSensor;
	}

	public void setMotionSensor(boolean motionSensor) {
		this.motionSensor = motionSensor;
	}

}
