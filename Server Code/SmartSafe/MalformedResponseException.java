package UBC.G8.SmartSafe;


/*
 * Custom exception for safe side errors
 */
public class MalformedResponseException extends Exception {

	private final int errorCode;

	
	public MalformedResponseException(int errorCode, String errorMessage)
	{
		super(errorMessage);
		this.errorCode = errorCode;
	}
	
	public int getErrorCode()
	{
		return errorCode;
	}
}
