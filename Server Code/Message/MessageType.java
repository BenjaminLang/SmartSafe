package UBC.G8.Message;

/*
 * Enum class to provide different requests a unique type.
 * This allows for easier server and app side processing.
 */
public enum MessageType {
	UNLOCKREQUEST, LOCKREQUEST,  IMAGEDATA, DRAWERREQUEST, MAPSAFETOAPPREQUEST, RESPONSE, CHANGESETTINGSREQUEST, SAFESTATUSREQUEST, HISTORYREQUEST, HANDSHAKE
}

