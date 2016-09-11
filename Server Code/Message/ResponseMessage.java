package UBC.G8.Message;

/*
 * ResponseMessage class is only sent from server to app
 * Allows the server to communicate results back to app and provide status codes through the ResponseCode
 */
public class ResponseMessage extends Message {
	
	private static final long serialVersionUID = 2L;
	private  ResponseCode response;
	
	public ResponseMessage(String appId, String safeId, MessageType type, ResponseCode response)
	{
		super(appId, safeId, type);
		this.response = response;
	}
	
	public ResponseCode getResponse()
	
	{
		return response;
		
	}

}
