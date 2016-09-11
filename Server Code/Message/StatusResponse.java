package UBC.G8.Message;

public class StatusResponse extends ResponseMessage {

	private static final long serialVersionUID = 1L;
	private boolean isLocked;
	private boolean motion;
	private int numTries;
	private boolean autolock;
	
	public StatusResponse(String appId, String safeId, ResponseCode response) {
		super(appId, safeId, MessageType.SAFESTATUSREQUEST, response);
		// TODO Auto-generated constructor stub
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public boolean isMotion() {
		return motion;
	}

	public void setMotion(boolean motion) {
		this.motion = motion;
	}

	public int getNumTries() {
		return numTries;
	}

	public void setNumTries(int numTries) {
		this.numTries = numTries;
	}

	public boolean isAutolock() {
		return autolock;
	}

	public void setAutolock(boolean autolock) {
		this.autolock = autolock;
	}
	
	

}
