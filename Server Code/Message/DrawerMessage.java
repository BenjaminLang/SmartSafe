package UBC.G8.Message;

import java.util.ArrayList;

public class DrawerMessage extends Message {

	private static final long serialVersionUID = 101L;

	public DrawerMessage(String appId, String safeId, MessageType type) {
		super(appId, safeId, type);
	}

}
