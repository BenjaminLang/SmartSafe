package androidapp.elec291group.smartsafe;

import UBC.G8.Message.*;

/**
 * Created by Ben on 28/03/2016.
 */
public class MessageFactory {

    final static private String appId = "arbiter";

    /*
     * Class to construct different outgoing message types
     */
    static public Message buildLockMessage(String safeId){
        LockMessage message = new LockMessage(appId, safeId, MessageType.LOCKREQUEST);
        return message;
    }

    static public Message buildUnlockMessage(String password, String safeId){
        ChangeLockMessage message = new ChangeLockMessage(appId, safeId, MessageType.UNLOCKREQUEST, password);
        return message;
    }

    static public Message buildSafeMessageList(){
        DrawerMessage message = new DrawerMessage(appId, null, MessageType.DRAWERREQUEST);
        return message;
    }

    static public Message buildAddSafeMessage(String safeId){
        Message message = new Message(appId, safeId, MessageType.MAPSAFETOAPPREQUEST);
        return message;
    }

    static public Message buildHistoryMessage(String safeId){
        Message message = new Message(appId, safeId, MessageType.HISTORYREQUEST);
        return message;
    }

    static public ChangeSettingsMessage buildSettingMessage(String safeId){
        ChangeSettingsMessage message = new ChangeSettingsMessage(appId, safeId, MessageType.CHANGESETTINGSREQUEST);
        return message;
    }

    static public Message buildStatusRequest(String safeId){
        Message message = new Message(appId, safeId, MessageType.SAFESTATUSREQUEST);
        return message;
    }
}
