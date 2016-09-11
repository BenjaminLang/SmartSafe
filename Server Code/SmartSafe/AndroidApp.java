package UBC.G8.SmartSafe;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import UBC.G8.Message.*;

/*
 * Class to represent the Android app
 */
public class AndroidApp {
	
	private String id;
	private boolean unlockReqest = false;
	private boolean	lockRequest = false;
	private Object mutexlock = new Object();
	private boolean SafeInterrupted = false;
	private boolean inUse = false;
	
	//blocking queue allows for inter-thread communication between App and Safe processing threads
	public BlockingQueue<ResponseMessage> recvQueue= new ArrayBlockingQueue<ResponseMessage>(10);

	//multiple getter and setter methods
	public AndroidApp(String id){
		this.id = id;
	}
	
	public String getId(){
		return id;
	}
	
	public boolean hasModifiedUnlockRequest(){
		return unlockReqest;
	}
	
	public void setRequestUnlock(){
		this.unlockReqest = true;
	}
	
	public void clearRequestUnlock(){
		this.unlockReqest = false;
	}
	
	public boolean hasModifiedLockRequest(){
		return lockRequest;
	}
	
	public void setRequestLock(){
		this.lockRequest = true;
	}
	
	public void clearRequestLock(){
		this.lockRequest = false;
	}
	
	public Object getMutexlock() {
		return mutexlock;
	}

	public boolean isSafeInterrupted() {
		return SafeInterrupted;
	}

	public void setSafeInterrupted(boolean safeInterrupted) {
		SafeInterrupted = safeInterrupted;
	}
	
	public synchronized void setInUse(boolean toSet){
		this.inUse = toSet;
	}
	
	public synchronized boolean getInUse(){
		return this.inUse;
	}
}
