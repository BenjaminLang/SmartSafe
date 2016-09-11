package UBC.G8.Message;

import java.util.ArrayList;

public class HistoryResponse extends ResponseMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<String> logs;

	public HistoryResponse(String appId, String safeId, MessageType type, ResponseCode response) {
		super(appId, safeId, type, response);
		// TODO Auto-generated constructor stub
	}

	public ArrayList<String> getLogs() {
		return logs;
	}

	public void setLogs(ArrayList<String> logs) {
		this.logs = logs;
	}
}
