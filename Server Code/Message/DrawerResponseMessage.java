package UBC.G8.Message;

import java.util.ArrayList;

public class DrawerResponseMessage extends ResponseMessage {
    private static final long serialVersionUID = 49;
    private ArrayList<String> safes = new ArrayList<String>();

    public DrawerResponseMessage(String appId, String safeId, MessageType type){
      super(appId, safeId, type, ResponseCode.SUCCESS);
    }

    public void addSafes(String safeId){
      safes.add(safeId);
    }

    public ArrayList<String> getSafes(){
  		return safes;
  	}
}
