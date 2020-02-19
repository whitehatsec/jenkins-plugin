package hudson.plugins.controller.sentinel;

public interface Connection  {
	boolean validate(String apiKey, String url) throws ValidateException;
}
