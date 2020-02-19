package hudson.plugins.utils;

import hudson.plugins.shared.AppConstants;

import java.io.PrintStream;
import java.util.Map;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPUpload {
	public static boolean execute(Map<String, String> param, PrintStream logger) {

		AppConstants.logger(logger,"HOST : " + param.get("HOST"));
		AppConstants.logger(logger,"TARGET_DIR : "
				+ param.get("UPLOAD_FOLDER"));
		AppConstants.logger(logger,"SOURCE_FILE : "
				+ param.get("SOURCE_FILE_PATH").replace('\\', '/'));

		boolean success = putFile(param.get("USER"), param.get("HOST"), param.get("PASSWD"),
				param.get("UPLOAD_FOLDER"), param.get("SOURCE_FILE_PATH").replace('\\', '/'), logger);
		return success;
	}


	public static boolean putFile(String username, String host, String password,
			String remotefile, String localfile, PrintStream logger) {
		AppConstants.logger(logger,"SFTPUpload.putFile()");
		JSch jsch = new JSch();
		Session session = null;
		try {
			session = jsch.getSession(username, host, 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(password);
			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			sftpChannel.put(localfile, remotefile);
			
			session.disconnect();
			AppConstants.logger(logger,"SFTP File Upload Done Successfuly");
			return true;
		} catch (JSchException e) {
			AppConstants.logger(logger,"Exception while SFTP upload : " + e);
		} catch (SftpException e) {
			AppConstants.logger(logger,"Exception while SFTP upload -> " + e);
		}
		return false;
	}


}