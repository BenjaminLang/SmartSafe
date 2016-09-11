package UBC.G8.Message;

import java.util.HashMap;
import java.util.Map;

public class SettingsResponse extends ResponseMessage {

	private static final long serialVersionUID = 12310L;
	Map<SettingType, ResponseCode> responses = new HashMap<SettingType , ResponseCode>();
	
	public SettingsResponse(String appId, String safeId, MessageType type,  Map<SettingType,ResponseCode> responses)
	{
		super(appId, safeId, type, null);
		this.responses = responses;
	}
	
	public Map<SettingType, ResponseCode> getResponses()
	{
		return responses;
	}
}
