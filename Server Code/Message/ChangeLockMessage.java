package UBC.G8.Message;

public class ChangeLockMessage extends Message{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String passwordRequest;
	
	public ChangeLockMessage(String appId, String safeId, MessageType type, String passwordRequest) {
		super(appId, safeId, type);
		this.passwordRequest = passwordRequest;
	}

	public String getPasswordRequest() {
		return passwordRequest;
	}
	
}
