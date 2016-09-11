package androidapp.elec291group.smartsafe;

import android.content.Context;
import android.widget.Toast;

import java.util.Map;

import UBC.G8.Message.MessageType;
import UBC.G8.Message.ResponseCode;
import UBC.G8.Message.ResponseMessage;
import UBC.G8.Message.SettingType;
import UBC.G8.Message.SettingsResponse;

/**
 * Created by Ben on 2016-04-02.
 * class to parse the response codes and depending on the message type, provide a string that outline
 * the message. This string is then used to create a "toast"
 */
public class ResponseParse {
    public static String ParseMessage(ResponseMessage message){
       String toastTxt = new String("Parse error");
       MyDataBridges bridge = MyDataBridges.getInstance();
       switch (message.getResponse()){
           case SUCCESS : {
               if (message.getMessageType().equals(MessageType.UNLOCKREQUEST)) {
                   toastTxt = String.format("%s, was unlocked", bridge.getCurrSafeID());
               } else if (message.getMessageType().equals(MessageType.LOCKREQUEST)) {
                   toastTxt = String.format("%s, was locked", bridge.getCurrSafeID());
               } else if (message.getMessageType().equals(MessageType.MAPSAFETOAPPREQUEST)) {
                   toastTxt = String.format("%s was added", bridge.getCurrSafeID());
               } else if (message.getMessageType().equals(MessageType.CHANGESETTINGSREQUEST)){
                   toastTxt = String.format("%s was updated", bridge.getCurrSafeID());
               }
               break;
           }
           case WRONGRESPONSE : {
               toastTxt = String.format("Wrong safe accessed");
               break;
           }
           case PIERROR : {
               toastTxt = String.format("Internal safe error");
               break;
           }
           case LOCKOUT : {
               toastTxt = String.format("Internal safe error");
               break;
           }
           case ALREADYLOCKED : {
               toastTxt = String.format("%s already locked", bridge.getCurrSafeID());
               break;
           }
           case ALREADYUNLOCKED : {
               toastTxt = String.format("%s already unlocked");
               break;
           }
           case INTERNALERROR : {
               toastTxt = new String("Internal server error");
               break;
           }
           case APPALREADYEXISTS : {
               toastTxt = new String("App already exists");
               break;
           }
           case WRONGSAFEPASSWORD : {
               toastTxt = new String("Incorrect Password");
               break;
           }
           case WRONGARDUINOPASSWORD : {
               toastTxt = new String("Too many incorrect attempts");
               break;
           }
           case DOOROPEN: {
               toastTxt = new String("door open");
               break;
           }
        }
        return toastTxt;
    }


    /**
     * This method is used exclusively for the settings response due to the increased complexity of the resposnemessage
     * Multiple setting notifications can occur at once and thus they need to be parsed on a case by case basis.
     */
    public static void parseSettingResponse(ResponseMessage message, Context context) {
        Map<SettingType, ResponseCode> responses = ((SettingsResponse) message).getResponses();
        for (SettingType type : responses.keySet()) {
            String toastTxt = new String("Parse error");
            MyDataBridges bridge = MyDataBridges.getInstance();

            switch (responses.get(type)) {
                case SUCCESS: {
                    if (type.equals(SettingType.AUTOLOCKCHANGEREQUEST)) {
                        toastTxt = String.format("autolock was changed");
                    } else if (type.equals(SettingType.CHANGEALARMREQUEST)){
                        toastTxt = String.format("alarm was changed");
                    } else if (type.equals(SettingType.CHANGEPASSWORDREQUEST)) {
                        toastTxt = String.format("password changed");
                    } else if (type.equals(SettingType.PASSWORDATTEMPTSREQUEST)) {
                        toastTxt = String.format("password attempts set");
                    }
                    Toast.makeText(context, toastTxt, Toast.LENGTH_SHORT).show();
                    break;
                }
                case WRONGRESPONSE: {
                    toastTxt = String.format("Wrong safe accessed");
                    Toast.makeText(context, toastTxt, Toast.LENGTH_SHORT).show();
                    break;
                }
                case PIERROR: {
                    toastTxt = String.format("Internal safe error");
                    Toast.makeText(context, toastTxt, Toast.LENGTH_SHORT).show();
                    break;
                }
                case INTERNALERROR: {
                    toastTxt = new String("Internal server error");
                    Toast.makeText(context, toastTxt, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }

}
