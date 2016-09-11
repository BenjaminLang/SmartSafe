package UBC.G8.Message;

/*
 * Enum class to provide diferent response codes for response messages
 * Allows the App to react to server operations and provide feedback to user
 */
public enum ResponseCode {
	SUCCESS, WRONGTARGET, PIERROR, LOCKOUT, WRONGRESPONSE, ALREADYLOCKED, ALREADYUNLOCKED, INTERNALERROR, 
	APPALREADYEXISTS, WRONGSAFEPASSWORD, WRONGARDUINOPASSWORD, TIMEOUT,DOOROPEN
}
