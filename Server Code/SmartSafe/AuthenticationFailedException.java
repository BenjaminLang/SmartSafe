package UBC.G8.SmartSafe;

/*
 * Custom exception for safe side errors
 */
@SuppressWarnings("serial")
public class AuthenticationFailedException extends Exception {
	
	private final int errorCode;
	
	
	public AuthenticationFailedException(int errorCode, String errorMessage)
	{
		super(errorMessage);
		this.errorCode = errorCode;
	}
	
	public int getErrorCode()
	{
		return errorCode;
	}

}
