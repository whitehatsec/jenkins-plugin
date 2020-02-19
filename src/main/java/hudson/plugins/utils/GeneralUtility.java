package hudson.plugins.utils;

import hudson.plugins.shared.AppConstants;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.lang.Exception;

public class GeneralUtility {
	public static String getCleanPath() {
		URL location = GeneralUtility.class.getProtectionDomain().getCodeSource()
				.getLocation();
		String path = location.getFile();
		path = new File(path).getParent();
		path = path.substring(0, (path.indexOf("WEB-INF")));
		return path;
	}
	public static void deleteFile(String filePath, PrintStream logger) {
		try {
			File file = new File(filePath);
			if(file.exists()){
				file.delete();
				AppConstants.logger(logger,"File is deleted : " + file.getAbsolutePath());
			}
		} catch (Exception e) {
			AppConstants.logger(logger,e);
		}
	}
	public static String getCurrenTimeDtTime() {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss a ");
		try {
			return formatter.format(new Date());

		} catch (Exception e) {
			AppConstants.logger(null,e);
		}

		return "";
	}
}
