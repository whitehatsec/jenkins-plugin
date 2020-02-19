package hudson.plugin.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

import hudson.plugins.controller.sentinel.Connection;
import hudson.plugins.controller.sentinel.ValidateException;
import hudson.plugins.ui.UiPublisher.DescriptorImpl;
import hudson.util.FormValidation;

public class ValidateConnectionTest {
	
	public String apiKey = "65ee5a89-62e1-4696-8cd8-8178da12cffc";
	public String sentinelUrl = "https://sentinel.localdomain.lan";
	
	@Test
	public void testSuccessfullConnection() throws IOException, ServletException{
		DescriptorImpl descriptorImpl = new DescriptorImpl();
		descriptorImpl.setConnection(new Connection() {
			
			@Override
			public boolean validate(String apiKey, String url) throws ValidateException {
				return true;
			}
		});
		FormValidation formValidation = descriptorImpl.doValidateConnection(apiKey,sentinelUrl, null, null);
		assertEquals("OK", formValidation.kind.name());
	}
	
	@Test
	public void testFailedConnection() throws IOException, ServletException{
		DescriptorImpl descriptorImpl = new DescriptorImpl();
		descriptorImpl.setConnection(new Connection() {
			
			@Override
			public boolean validate(String apiKey, String url) throws ValidateException {
				return false;
			}
		});
		FormValidation formValidation = descriptorImpl.doValidateConnection(apiKey,sentinelUrl, null, null);
		assertEquals("ERROR", formValidation.kind.name());
	}
}
