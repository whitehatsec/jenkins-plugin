package hudson.plugins.utils;

import hudson.plugins.shared.AppConstants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
 
public class ExecuteShellComand {
 
	public static String run(String command,PrintStream logger  ) {
 
		ExecuteShellComand obj = new ExecuteShellComand();
  
		String output = obj.executeCommand(command);
 
		AppConstants.logger(logger,output);
		
		return output;
 
	}
 
	private String executeCommand(String command) {
 
		StringBuffer output = new StringBuffer();
 
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(p.getInputStream()));
 
                        String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}
 
		} catch (Exception e) {
			e.printStackTrace();
		}
 
		return output.toString();
 
	}
 
}