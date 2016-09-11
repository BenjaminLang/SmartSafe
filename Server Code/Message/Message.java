package UBC.G8.Message;

import java.io.Serializable;

/*
 * Message Class which is used to communicate between the app and the server
 * It is sub-typed to provide varying types messages which are used to update the App, server and safe
 */
public class Message implements Serializable{
	
	
	private static final long serialVersionUID = 1L;
	private String appId;
	private String safeId;
	private MessageType type;
	
	public Message(String appId, String safeId, MessageType type){
		this.type = type;
		this.appId = appId;
		this.safeId = safeId;
	}

	public String getAppId() {
		return appId;
	}

	public String getSafeId() {
		return safeId;
	}

	public MessageType getMessageType() {
		return type;
	}

}
