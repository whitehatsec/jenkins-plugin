package com.whitehat.sentinel.plugin.jenkins.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.whitehat.sentinel.plugin.jenkins.controller.SentinelUtility;
import com.whitehat.sentinel.plugin.jenkins.shared.AppConstants;

public class MultipartFileUploader {

	private final static char[] CHARACTERS = new char[] {
			'0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};
	
	public static String hex(byte[] buffer) {
		char[] out = new char[buffer.length * 2];
		
		for(int i=0; i<buffer.length; i++) {
			out[2 * i] = CHARACTERS[(buffer[i] & 0xF0) >>> 4];
			out[2 * i + 1] = CHARACTERS[buffer[i] & 0x0F];
		}
		
		return new String(out);
	}

	public static String upload(Map<String, String> param, String myApiKey, String mySentinelURL,PrintStream logger) throws CustomException {
		
		String appliance_id = param.get("applianceIP");
		String file = param.get("workspacePath") + File.separator
				+ param.get("archiveName");
		String fileName =  param.get("archiveName");
		byte[] fileBytes = null;
		Path path = Paths.get(file);
		try {
			fileBytes = Files.readAllBytes(path);
		} catch (IOException e3) {
			AppConstants.logger(logger,"Error in getting archive to upload: " +  e3.getMessage()); 
		}
		
		if(fileBytes != null ) {
			MessageDigest digest = DigestUtils.getSha256Digest();
			digest.update(fileBytes);
			String sha256 = MultipartFileUploader.hex(digest.digest());

			HttpURLConnection postResponse = null;
			try {
				postResponse = SentinelUtility.generateToken(sha256, myApiKey, mySentinelURL,null);
			} catch (Exception e2) {
				AppConstants.logger(logger,"Error while sending a post for token generation: " + e2.getMessage());
			}
			String signedToken = null;
			BufferedReader rd;
			String line;
			String result = "";
			try {
				if(postResponse !=  null && postResponse.getResponseCode() < 302) {
					AppConstants.logger(logger,"Jenkins received valid token from Sentinel to upload file");
					rd = new BufferedReader(
							new InputStreamReader(postResponse.getInputStream()));
					while ((line = rd.readLine()) != null) {
						result += line;
					}
					ObjectMapper mapper = new ObjectMapper();
					JsonNode jsonNode = mapper.readTree(result);
					
					
					signedToken = jsonNode.get("token").getTextValue();
					AppConstants.logger(logger,"Received token from sentinel");
					
				}
				else {
					AppConstants.logger(logger,"Error in upload token post request");
				}
				
			if(signedToken != null) {
				
				String postURL = String.format(AppConstants.UPLOAD_TO_APPLIANCE, appliance_id);
				URL url;
				url = new URL(postURL);
				File binaryFile = new File(file);
				
				
				HttpURLConnection conn = SentinelUtility.getHttpURLConnection(url,logger);
				String boundary = Long.toHexString(System.currentTimeMillis());
				String CRLF = "\n"; // Line separator required by multipart/form-data
				String charset = "UTF-8";
				
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type",
						"multipart/form-data; boundary=" + boundary);
				
				try (
					    OutputStream output = conn.getOutputStream();
					    PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
					) {
					 writer.append("--" + boundary).append(CRLF);
					 writer.append("Content-Disposition: form-data; name=\"signature\"").append(CRLF);
					 writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
					 writer.append(CRLF).append(signedToken).append(CRLF).flush();
					 
					 writer.append("--" + boundary).append(CRLF);
					 writer.append("Content-Disposition: form-data; name=\"filename\"").append(CRLF);
					 writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
					 writer.append(CRLF).append(fileName).append(CRLF).flush();
					 
					 writer.append("--" + boundary).append(CRLF);
					 writer.append("Content-Disposition: form-data; name=\"attachment\"").append(CRLF);
					 String contentType = HttpURLConnection.guessContentTypeFromName(binaryFile.getName());
					 if(contentType == null){
						 contentType = "application/octet-stream";
					 }
					 writer.append("Content-Type: " + contentType).append(CRLF);
					 writer.append(CRLF).flush();
					 Files.copy(binaryFile.toPath(), output);
					 output.flush(); // Important before continuing with writer!
					 writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
					 // End of multipart/form-data.
					 writer.append("--" + boundary + "--").append(CRLF).flush();

				} catch(Exception e) {
					AppConstants.logger(logger,"File upload to appliance failed while creating a writter");
					AppConstants.logger(logger,e.getMessage());
					return null;
				}
				
				int responseCode = conn.getResponseCode();
				if(responseCode < 302){
					AppConstants.logger(logger,"File uploaded to appliance");
					
				} else {
					AppConstants.logger(logger,"Error in uploading file to appliance");
				}
				
				StringBuffer responseContent = new StringBuffer();
			    BufferedReader read = null;
			    
			    try
			    {
			        read = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
			        String applianceURL = read.readLine();
			        AppConstants.logger(logger, "URL from upload method: "+ applianceURL);
			        return applianceURL;
			    }
			    catch( Exception e )
			    {
			        read = new BufferedReader( new InputStreamReader( conn.getErrorStream() ) );
			        AppConstants.logger(logger,"Error in uploading file to appliance");
			        AppConstants.logger(logger,read.readLine());
			    }
				return null;
			}
					
			
		 } catch (IOException e) {
			AppConstants.logger(logger,"Exception in uploading file");
			try {
				AppConstants.logger(logger,postResponse.getResponseMessage());
			} catch (IOException e1) {
				AppConstants.logger(logger,"Exception while retriving post response");
				AppConstants.logger(logger,e1.getMessage());
		}
			AppConstants.logger(logger,e.getMessage());
	 }
	   }
	   return null;
		
	}
			
	
}