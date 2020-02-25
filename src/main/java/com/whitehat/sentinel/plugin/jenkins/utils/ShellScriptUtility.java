package com.whitehat.sentinel.plugin.jenkins.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.whitehat.sentinel.plugin.jenkins.shared.AppConstants;

public class ShellScriptUtility {
	private ShellScriptUtility() {
	}

	public static void run(Map<String, String> param) {
		try {

			AppConstants.logger(null,"ShellScript.run() ");
			ProcessBuilder pb = null;
			Process p;
			String cmd2 = "";

			String path = GeneralUtility.getCleanPath();

			String workingDir = path; // "/plugin/WhiteHat1";
			String scriptloc = null;
			//Change by spatel
			if (param.get("FUNCTIONALITY").equalsIgnoreCase("ftp")) {
				scriptloc = workingDir + "ftp.sh";
			} else {
				scriptloc = workingDir + "sftp.sh";
			}
			
			//End
			AppConstants.logger(null,"path " + scriptloc);

			Runtime.getRuntime().exec("chmod 777 " + scriptloc);
			String cmd[] = { scriptloc };

			// String command = "ftp.sh";
			pb = new ProcessBuilder(cmd);
			pb.environment().put("HOST", param.get("HOST"));
			pb.environment().put("USER", param.get("USER"));
			pb.environment().put("PASSWORD", param.get("PASSWD"));
			pb.environment().put("TARGET_DIR", param.get("UPLOAD_FOLDER"));
			pb.environment().put("SOURCE_FILE", param.get("archiveName"));
			
			AppConstants.logger(null,"HOST : "+ param.get("HOST"));
			AppConstants.logger(null,"TARGET_DIR : "+ param.get("UPLOAD_FOLDER"));
			AppConstants.logger(null,"SOURCE_FILE : "+ param.get("archiveName"));
			
			// AppConstants.logger
			// .println("before workingDir ");
			pb.directory(new File(workingDir));

			p = null;
			try {
				// AppConstants.logger
				// .println("before start ");
				p = pb.start();
			} catch (IOException ex) {
				Logger.getLogger(Process.class.getName()).log(Level.SEVERE,
						null, ex);
			}
			// AppConstants.logger
			// .println("before bufferreader p "+p);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			// AppConstants.logger
			// .println("before bufferreader2222 ");
			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));

			
			String s = null;
			String output = "";
			while ((s = stdInput.readLine()) != null) {
				AppConstants.logger(null,s);

			}
			output = "";

			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				AppConstants.logger(null,s);
			}
		} catch (IOException ex) {
			Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null,
					ex);
		}
	}

}
