package hudson.plugin.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import hudson.plugins.controller.sentinel.SentinelUtility;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
public class GetApplicationsTest {
	
	@Test
	public void testApplicationsList(){
		
		Map<Integer, String> appsMap=new HashMap<Integer, String>();

		InputStream applicationJson = GetApplicationsTest.class.getResourceAsStream("hudson/plugin/test/fixtures/application.json");
		BufferedReader r = new BufferedReader(new InputStreamReader(applicationJson));
		String applicationString="";
		String l;
		try {
			while((l = r.readLine()) != null) {
				applicationString = applicationString + l;
			 }
				applicationJson.close();
			 } catch (IOException e) {
				e.printStackTrace();
			 }
		
		
		appsMap = SentinelUtility.getJsonNode(applicationString, "application", null);
		assertNotNull(appsMap);
		int appCountExpected = 3;
		assertEquals(appCountExpected,appsMap.size());
	
	}
}
	

