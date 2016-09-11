package UBC.G8.Message;

public class LockMessage extends Message{
	
	private boolean wasLocked;


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LockMessage(String appId, String safeId, MessageType type) {
		super(appId, safeId, type);
		// TODO Auto-generated constructor stub
	}

	public boolean wasLocked() {
		return wasLocked;
	}

	public void setWasLocked(boolean wasLocked) {
		this.wasLocked = wasLocked;
	}
	
	
	
}
