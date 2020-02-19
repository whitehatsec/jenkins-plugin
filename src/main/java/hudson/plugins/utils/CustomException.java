package hudson.plugins.utils;

public class CustomException extends Exception {

	private static final long serialVersionUID = 1L;
	private int errorCode;

	public CustomException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}
