package UBC.G8.Message;

public class MappingMessage extends Message {

	private static final long serialVersionUID = 1L;
	private boolean wasMapped;
	
	public MappingMessage(String appId, String safeId, MessageType type) {
		super(appId, safeId, type);
	}

	public boolean wasMapped() {
		return wasMapped;
	}

	public void setWasMapped(boolean wasMapped) {
		this.wasMapped = wasMapped;
	}
	
}
