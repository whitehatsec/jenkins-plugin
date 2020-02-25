package com.whitehat.sentinel.plugin.jenkins.controller;
import jenkins.model.Jenkins;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.TreeMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import javax.net.ssl.HostnameVerifier;

import hudson.EnvVars;
import hudson.ProxyConfiguration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.whitehat.sentinel.plugin.jenkins.shared.AppConstants;
import com.whitehat.sentinel.plugin.jenkins.utils.CustomException;
import com.whitehat.sentinel.plugin.jenkins.utils.GeneralUtility;
import com.whitehat.sentinel.plugin.jenkins.utils.MultipartFileUploader;
import com.whitehat.sentinel.plugin.jenkins.utils.SFTPUpload;
import com.whitehat.sentinel.plugin.jenkins.utils.ant.CustomAntUtility;
import com.whitehat.sentinel.plugin.jenkins.utils.ant.XMLFileUtility;

import java.io.PrintStream;

public class SentinelUtility {

	public static String apiKey = "";
	
	
	public static Map<Integer, String> getSites(final String apiKey, final String sentinelURL ) {
		Map<Integer, String> siteMap=new LinkedHashMap<Integer, String>();
		try {

			boolean loginFlag = false;
			if (!apiKey.isEmpty()) {
				SentinelUtility.apiKey = apiKey;
				loginFlag = true;
			}

			if (loginFlag) {
				String resultedJSON = getJSONData(AppConstants.SITE_API_CONSTANT,
						false, false, true, sentinelURL,"sites", true, null);
				siteMap = getJsonNode(resultedJSON, "sites",null);
			}
		} catch (Exception e1) {
		
		}

		return siteMap;
	}
 
	public static Map<Integer, String> getApps(final String apiKey, final String sentinelURL ) {
		Map<Integer, String> appsMap=new HashMap<Integer, String>();
		try {

			boolean loginFlag = false;
			if (!apiKey.isEmpty()) {
				SentinelUtility.apiKey = apiKey;
				loginFlag = true;
			}

			if (loginFlag) {
				String resultedJSON = getJSONData(AppConstants.APPLICATION_API_CONSTANT,
						false, false, true, sentinelURL, "applications",true , null);
				appsMap = getJsonNode(resultedJSON, "application",null);
			}
		} catch (Exception e1) {
			
		}

		return appsMap;
	}
	
	
	public static Map<Integer, String> getAppliances(final String apiKey, final String sentinelURL) {
		Map<Integer, String> appsMap=new HashMap<Integer, String>();
		try {

			boolean loginFlag = false;
			if (!apiKey.isEmpty()) {
				SentinelUtility.apiKey = apiKey;
				loginFlag = true;
			}

			if (loginFlag) {
				String resultedJSON = getJSONData(AppConstants.APPLIANCE_API_CONSTANT,
						false, false, true, sentinelURL, "appliance", true, null);
				appsMap = getJsonNode(resultedJSON, "application",null);
			}
		} catch (Exception e1) {
			
		}

		return appsMap;
	}
	
	
	
	public static Map<Integer, String> getCodebases(final String apiKey, final String sentinelURL, final String appID) {
		Map<Integer, String> codebaseMap=new HashMap<Integer, String>();
		try {

			boolean loginFlag = false;
		
			if (!apiKey.isEmpty()) {
				SentinelUtility.apiKey = apiKey;
				loginFlag = true;
			}

			if (loginFlag) {
				String apiCall = AppConstants.APPLICATION_API_CONSTANT + "/" + appID + "/codebase";
				String resultedJSON = getJSONData(apiCall,
						false, false, true, sentinelURL,"", true, null);
				codebaseMap = getJsonNode(resultedJSON, "codebase",null);
			}
		} catch (Exception e1) {
			
		}

		return codebaseMap;
	}
	
	public static HttpURLConnection createApplication(String newAppName, String language, String appliance, String apiKey, String sentinelURL){
		HttpURLConnection response = null;
		String createAppApi = sentinelURL + AppConstants.APPLICATION_API_CONSTANT + "?";
		String param = String.format(AppConstants.CREATE_APPLICATION_POST_BODY,newAppName,language,appliance);
		try {
			response = makePostRequest(createAppApi, apiKey, param,null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		
		return response;
	}
	
	
	
	
	public static HttpURLConnection createCodebase(String newCodebaseName, String appSelected, String apiKey, String sentinelURL){
		HttpURLConnection response = null;
		String apiCall = sentinelURL + AppConstants.APPLICATION_API_CONSTANT + "/" + appSelected + "/codebase" + "?";
		String param = String.format(AppConstants.CREATE_CODEBASE_POST_BODY,newCodebaseName);
		try {
			response = makePostRequest(apiCall, apiKey, param,null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		
		return response;
	}
	
	public static HttpURLConnection generateToken(String sha256, String apiKey, String sentinelURL,final PrintStream logger) throws Exception{
		HttpURLConnection response = null;
		String apiCall = sentinelURL + AppConstants.GENERATE_TOKEN_API_CONSTANT + "?";
		String param = String.format(AppConstants.GENERATE_TOKEN_POST_BODY,sha256);
		try{
			response = makePostRequest(apiCall, apiKey, param,logger);
		} catch (Exception e) {
			AppConstants.logger(logger,e.getMessage());
			return null;
		}
		return response;
	}
	
	
	public static HttpURLConnection updateCodebase(String codebaseID, String appSelected, String apiKey, String sentinelURL, Map<String, String> param, final PrintStream logger){
		HttpURLConnection response = null;
		String apiCall = sentinelURL + AppConstants.APPLICATION_API_CONSTANT + "/" + appSelected + "/codebase/" + codebaseID + "?";

		String putParam = new String();
		
		if(param.get("archiveMethodSelected").equalsIgnoreCase("SFTP")) {
			String sftpHost = param.get("SFTP_HOST");
			String sftpFolder =  param.get("UPLOAD_FOLDER");
			String repoURL;
			if(sftpHost!= null && sftpHost.length() > 0 && sftpHost.charAt(sftpHost.length()-1)=='/') {
				sftpHost =sftpHost.substring(0,sftpHost.length()-1);
			}
			
			if(sftpFolder!= null && sftpFolder.length() > 0 && sftpFolder.charAt(sftpFolder.length()-1)=='/') {
				sftpFolder =sftpFolder.substring(0,sftpFolder.length()-1);
			}
			
			if(sftpFolder!=null) {
				repoURL = sftpHost + "/" +  sftpFolder  + "/" + param.get("archiveName");
			} else {
				repoURL = sftpHost + "/"  + param.get("archiveName");	
			}
			
			putParam = String.format(AppConstants.UPDATE_CODEBASE_SFTP,repoURL,param.get("USER"), param.get("PASSWD"));
			try {
				response = makePutRequest(apiCall, apiKey, putParam,logger);
			} catch (Exception e) {
				AppConstants.logger(logger,e.getMessage());
				return null;
			}
		}
		
		else if (param.get("archiveMethodSelected").toString().equalsIgnoreCase("JENKINS")){
			putParam = String.format(AppConstants.UPDATE_CODEBASE_HTTP,param.get("jenkinsRepoUrl"));
			try {
				AppConstants.logger(logger,"Request to updated codebase");
				response = makePutRequest(apiCall, apiKey, putParam,logger);
			} catch (Exception e) {
				AppConstants.logger(logger,e.getMessage());
				return null;
			}
		}
		
		else if (param.get("archiveMethodSelected").toString().equalsIgnoreCase("APPLIANCE")){
			putParam = String.format(AppConstants.UPDATE_CODEBASE_HTTP,param.get("applianceRepoUrl"));
			try {
				AppConstants.logger(logger,"Request to updated codebase");
				response = makePutRequest(apiCall, apiKey, putParam,logger);
			} catch (Exception e) {
				AppConstants.logger(logger,e.getMessage());
				return null;
			}
		}

		return response;
	}
	
	public static HttpURLConnection setScanNow(int assetID, String assetType, String apiKey, String sentinelURL, String caller,final PrintStream logger){
		HttpURLConnection response = null;
		String scanNowApi;  
		if(caller.equalsIgnoreCase("app")) {
			scanNowApi = sentinelURL + AppConstants.APPLICATION_API_CONSTANT +"/"
													+ assetID + "?";
			try {
				AppConstants.logger(logger,"Sending scan request"); 
				response = makePutRequest(scanNowApi, apiKey, AppConstants.SCAN_NOW_APP,logger);
			} catch (Exception e) {
				AppConstants.logger(logger,e.getMessage());
				return null;
			}	
		}
		
		else if(caller.equalsIgnoreCase("site")){
			scanNowApi = sentinelURL + AppConstants.SITE_API_CONSTANT +"/"
					+ assetID + "/scan_schedule?";
			try {
				response = makePutRequest(scanNowApi, apiKey, AppConstants.SCAN_NOW_SITE,logger);
			} catch (Exception e) {
				AppConstants.logger(logger,e.getMessage());
				return null;
			}
		}
		
		return response;
	}

	public static String getJSONData(String urlConstant, boolean skipJsonLabel,
			boolean QAFlag, boolean sentinelLogin, String sentinelURL, String caller, boolean skipRetry, PrintStream logger) throws Exception {
		
		String getURL = sentinelURL + AppConstants.HOST_NAME + urlConstant + "?";
		
		if(caller.equalsIgnoreCase("applications")) {
			getURL = getURL + AppConstants.APP_ORDER_BY;
		}
		
		else if (caller.equalsIgnoreCase("sites")) {
			getURL = getURL + AppConstants.SITE_ORDER_BY;
		}
		
		else if(caller.equalsIgnoreCase("appliance")){
			getURL = getURL + AppConstants.ONLY_SAST_APPLIANCE;
		}
		try {
			String result=  makeGetRequest(getURL, skipJsonLabel, false, logger);
			if( !skipRetry && result.equals("")) {
				AppConstants.logger(logger, "No data receievd by get request, retry in 10 seconds");
				Thread.sleep(10000);
				result = makeGetRequest(getURL, skipJsonLabel, false, logger);
			}
			return result;
			
		} catch (Exception e) {
			AppConstants.logger(logger, "Exception in getting json data");
			return null;
		}

	}

	public static Map<Integer, String> getJsonNode(String result, String caller,final PrintStream logger) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj;
		JsonNode jsonNode;
		Map<Integer, String> applicationMap = new LinkedHashMap<Integer, String>();

		try {

			actualObj = mapper.readTree(result);
			if(caller.equalsIgnoreCase("sites")) {
				jsonNode = actualObj.path("sites");
			}
			else {
				jsonNode = actualObj.path("collection");
			}

			if (jsonNode.size() != 0) {
				Iterator<JsonNode> ite = jsonNode.getElements();

				while (ite.hasNext()) {
					JsonNode temp = ite.next();
					int id;
					if(caller.equalsIgnoreCase("codebase")) {
						id = Integer.parseInt(temp.get("id").toString().replace("\"", ""));
					}
					else {
						id = Integer.parseInt(temp.get("id").toString());
					}
					String value = temp.get("label").toString().replace("\"", "");
					
					if(caller.equalsIgnoreCase("codebase")){
						if(temp.get("repository_type").toString().replace("\"", "").equalsIgnoreCase("mock")) {
							applicationMap.put(id, value);
						}
					}
					else {
						applicationMap.put(id, value);
					}
					
				}

			}
		} catch (IOException e) {
			AppConstants.logger(logger,e);
		}
		
		return applicationMap;
	
	}
	
	public static String makeGetRequest(String urlToRead, boolean skipJsonLabel,
			boolean browserCookies, final PrintStream logger) throws Exception {
		URL url;
		HttpURLConnection conn = null;
		BufferedReader rd;
		String line;
		String result = "";
		try {
			
			url = new URL(urlToRead + (skipJsonLabel ? "" : "format=json&") + AppConstants.API_METRICS);
			setupsslnoverify();
			conn = SentinelUtility.getHttpURLConnection(url,logger);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("key", SentinelUtility.apiKey);
			
			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			if(conn != null && conn.getResponseCode() != 200) {
				AppConstants.logger(logger,"Response Code for Sentinel : " + conn.getResponseCode());
			}
			rd.close();
			conn.disconnect();
		} catch (Exception e) {
			AppConstants.logger(logger, "Exception in get request " + e.getMessage());
			if(conn != null && conn.getResponseCode() != 200) {
				AppConstants.logger(logger,"Response Code for Sentinel : " + conn.getResponseCode());
			}	
		}
		return result;
	}
	
	
	
	public static HttpURLConnection makePostRequest(final String urlToRead, String apiKey , final String urlParameters, final PrintStream logger)
			throws Exception {
		
		HttpURLConnection conn = null;

		try {

			URL url = null;
			

			url = new URL(urlToRead);
			setupsslnoverify();
			conn = SentinelUtility.getHttpURLConnection(url,logger);
			conn.setRequestMethod("POST");
			conn.addRequestProperty("User-Agent",
					"WHS-jenkins");
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("key", apiKey);
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoOutput(true);
			// Send post request
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
		} catch (Exception ex) {
			AppConstants.logger(logger,ex.getMessage());
			return null;
		}
		AppConstants.logger(logger,"Response message for post request : " + conn.getResponseMessage());
		return conn;
		
	}
	
	//Make a put request.
	
	public static HttpURLConnection makePutRequest(final String urlToRead, final String apiKey, final String urlParameters,final PrintStream logger)
			throws Exception {
		HttpURLConnection conn = null;
		try {

			URL url = null;
			url = new URL(urlToRead);
			setupsslnoverify();
			conn = SentinelUtility.getHttpURLConnection(url,logger);
			conn.setRequestMethod("PUT");
			conn.addRequestProperty("User-Agent",
					"WHS-jenkins");
			conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("key", apiKey);

			conn.setDoOutput(true);

			
			// Send post request
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

		} catch (Exception ex) {
			AppConstants.logger(logger,ex.getMessage());
			return null;
		}
		AppConstants.logger(logger,"Response code for post request : " + conn.getResponseCode());
		return conn;
	}
	
	//Setting up SSL no verify, so Jenkins can be set with local and QA boxes.
	
	public static void setupsslnoverify() throws NoSuchAlgorithmException, KeyManagementException  {

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs,
					String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs,
					String authType) {
			}
		} };

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		// set the  allTrusting verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}
	
	//Check for proxy setting on Jenkins and retun connection object accordingly.
	
	public static HttpURLConnection getHttpURLConnection(final URL url,final PrintStream logger) throws IOException {
		HttpURLConnection conn = null;
		Jenkins jenkins = Jenkins.getInstance();
		ProxyConfiguration proxyConfig = null;
	    if(jenkins != null) {
	    	proxyConfig = Jenkins.getInstance().proxy;
	    } 
		
		if (proxyConfig != null) {
		      Proxy proxy = proxyConfig.createProxy(url.getHost());
		      if (proxy != null && proxy.type() == Proxy.Type.HTTP) {
		    	  AppConstants.logger(logger,"Attempting to use the Jenkins proxy configuration");
		        conn = (HttpURLConnection) url.openConnection(proxy);
		        if (conn == null) {
		        	AppConstants.logger(logger,"Failed to use the Jenkins proxy configuration");
		        }
		      }
		    } else {
		    	AppConstants.logger(logger, "Jenkins does not have proxy setting");
		    }

		    if (conn == null) {
		      conn = (HttpURLConnection) url.openConnection();
		    }

		    return conn;
		}

	public static String getAssetScanStatus(final String apiKey,String sentinelURL,final int appID,String assetType,final PrintStream logger) {
		String asset_scan_status ="";
		try {
			boolean loginFlag = false;
			if (!apiKey.isEmpty()) {
				SentinelUtility.apiKey = apiKey;
				loginFlag = true;    
			} 
			if (loginFlag) {
				SentinelUtility.apiKey = apiKey;  
				String resultedJSON ="";
				if(assetType.equalsIgnoreCase("applications")) {
					resultedJSON  = getJSONData(AppConstants.APPLICATION_API_CONSTANT+"/"+appID,false, false, true, sentinelURL, "applications",false, logger);
					asset_scan_status = getJsonObjectData(resultedJSON,"asset_scan_status").toString();
				}
			}
		} catch (Exception ex) {
				AppConstants.logger(logger,ex);
				return null;
		}
		return asset_scan_status; 
		
	}

	public static Map<String, Integer> getVulnerabilitiesCount(final String apiKey, final String sentinelURL,final int appID,final PrintStream logger) {
		Map<String, Integer> vulnerabilityCountMap=new HashMap<String, Integer>();
		try {
			boolean loginFlag = false;
			if (!apiKey.isEmpty()) {
				SentinelUtility.apiKey = apiKey;
				loginFlag = true;    
			} 
			if (loginFlag) {
				String resultedJSON = getJSONData(AppConstants.APPLICATION_API_CONSTANT+"/"+appID+"/"+AppConstants.FULL_STATISTICS,
						false, false, true, sentinelURL, "", false, logger);
				JsonObject jsonObject = new JsonParser().parse(resultedJSON).getAsJsonObject();
				JsonObject rating =  jsonObject.get("rating").getAsJsonObject();
				JsonObject 	openVulnerabilities = 	rating.get("open").getAsJsonObject();
				int openLowVulnerability = openVulnerabilities.get("low") != null ? openVulnerabilities.get("low").getAsInt():0;
				int openMediumVulnerability = openVulnerabilities.get("medium") != null ? openVulnerabilities.get("medium").getAsInt():0;
				int openHighVulnerability = openVulnerabilities.get("high") != null ? openVulnerabilities.get("high").getAsInt():0;
				int openCriticalVulnerability = openVulnerabilities.get("critical") != null ? openVulnerabilities.get("critical").getAsInt():0;
				int openNoteVulnerability = openVulnerabilities.get("note") != null ? openVulnerabilities.get("note").getAsInt():0; 
				
				JsonObject 	discoveredVulnerabilities = rating.get("discovered").getAsJsonObject();
				int newLowVulnerability = discoveredVulnerabilities.get("low")!=null ?discoveredVulnerabilities.get("low").getAsInt():0;
				int newMediumVulnerability = discoveredVulnerabilities.get("medium")!=null?discoveredVulnerabilities.get("medium").getAsInt():0;
				int newHighVulnerability = discoveredVulnerabilities.get("high")!=null ?discoveredVulnerabilities.get("high").getAsInt():0;
				int newCriticalVulnerability = discoveredVulnerabilities.get("critical")!=null?discoveredVulnerabilities.get("critical").getAsInt():0;
				int newNoteVulnerability = discoveredVulnerabilities.get("note")!=null ? discoveredVulnerabilities.get("note").getAsInt():0;

				int totalNewVulnerability = jsonObject.get("total_discovered")!=null ? jsonObject.get("total_discovered").getAsInt():0;
				int totalOpenVulnerability = jsonObject.get("total_open")!=null ? jsonObject.get("total_open").getAsInt():0 ;
			
				vulnerabilityCountMap.put("openLowVulnerability", openLowVulnerability);
				vulnerabilityCountMap.put("openMediumVulnerability", openMediumVulnerability);
				vulnerabilityCountMap.put("openHighVulnerability", openHighVulnerability);
				vulnerabilityCountMap.put("openCriticalVulnerability", openCriticalVulnerability);
				vulnerabilityCountMap.put("openNoteVulnerability", openNoteVulnerability);
				
				vulnerabilityCountMap.put("newLowVulnerability", newLowVulnerability);
				vulnerabilityCountMap.put("newMediumVulnerability", newMediumVulnerability);
				vulnerabilityCountMap.put("newHighVulnerability", newHighVulnerability);
				vulnerabilityCountMap.put("newCriticalVulnerability", newCriticalVulnerability);
				vulnerabilityCountMap.put("newNoteVulnerability", newNoteVulnerability);
				
				vulnerabilityCountMap.put("totalNewVulnerability", totalNewVulnerability);
				vulnerabilityCountMap.put("totalOpenVulnerability", totalOpenVulnerability);	
			}
		} catch (Exception ex) {
			AppConstants.logger(logger,ex.getMessage());
			return null;
		}     
		return vulnerabilityCountMap;  
	}
	
	public static JsonElement getJsonObjectData(String resultedJSON, String searchString) {
		JsonElement element = null;
			if (resultedJSON != null && !resultedJSON.equals("") && searchString != null) {
				element = new JsonParser().parse(resultedJSON).getAsJsonObject().get(searchString).getAsJsonPrimitive();
			}
		return element;
	}
	public static boolean waitIfAssetScanStatusRunning(final String myApiKey,final String mySentinelURL,final int appID, 
			final  boolean codebaseUpdate,final boolean checkScanTimeout, final boolean scanningTimeOutSelected,final 
			String scanWaitTimeMinutes,final long startTime, final boolean previousScanFinish, final boolean vulnerableFailBuildSelected,final PrintStream logger) {
		  
		try {
			String asset_scan_status =SentinelUtility.getAssetScanStatus(myApiKey,mySentinelURL,appID,"applications",logger);
			if (asset_scan_status != null && asset_scan_status.toLowerCase().contains("running") ) {
				AppConstants.logger(logger,"Asset scan status is: "+asset_scan_status);
			}
			if(asset_scan_status == null || asset_scan_status.equals("")){
				AppConstants.logger(logger, "Scan status api did not return any value - will retry in 5 mins");
				Thread.sleep(60000 * 5);     
				asset_scan_status =SentinelUtility.getAssetScanStatus(myApiKey,mySentinelURL,appID,"applications",logger);
			}  
			while (asset_scan_status != null && asset_scan_status.toLowerCase().contains("running")) {
				if(codebaseUpdate == true) { 
					AppConstants.logger(logger,"Previous scan is running, waiting to update codebase");
				}
				if (scanningTimeOutSelected && checkScanTimeout && scanWaitTimeMinutes != null) {
					AppConstants.logger(logger,"Scan is in progress");
					if(scanWaitTimeMinutes != null && scanWaitTimeMinutes.trim().length()>0 && isInteger(scanWaitTimeMinutes.trim())) {
						boolean timeoutExceed =checkScantimeoutExceed(scanningTimeOutSelected,scanWaitTimeMinutes,startTime,vulnerableFailBuildSelected,logger);
						if(timeoutExceed) {
							return false;
						}
					}
				}	
				if(previousScanFinish) { 
					AppConstants.logger(logger,"Waiting for previous scan to finish");
				} 
				Thread.sleep(60000 * 5);     
				asset_scan_status =SentinelUtility.getAssetScanStatus(myApiKey,mySentinelURL,appID,"applications",logger);
				if(asset_scan_status == null || asset_scan_status.equals("")){
					AppConstants.logger(logger, "Scan status api did not return any value - will retry in 5 mins");
					Thread.sleep(60000 * 5);     
					asset_scan_status =SentinelUtility.getAssetScanStatus(myApiKey,mySentinelURL,appID,"applications",logger);
				}
			}
			if(asset_scan_status == null || asset_scan_status.equals("")){
				AppConstants.logger(logger, "Scan status api did not return any value - failing the build");
				return false;
			}
			
			if (asset_scan_status != null && asset_scan_status.toLowerCase().contains("whs updating configuration")) {
				AppConstants.logger(logger,"Asset scan status is: "+asset_scan_status);
				AppConstants.logger(logger,"Application build failed.");   
				return false;
			}
			if (asset_scan_status!= null && asset_scan_status.toLowerCase().contains("complete")) {
				AppConstants.logger(logger,"Asset scan status is: "+asset_scan_status);
				return true;
			}
			if (asset_scan_status!= null && asset_scan_status.toLowerCase().contains("failed")) {
				AppConstants.logger(logger,"Asset scan status is: "+asset_scan_status);
				return true;
			}
			if (asset_scan_status != null && asset_scan_status.trim().length() > 0) {
					AppConstants.logger(logger,"Asset scan status is: "+asset_scan_status);
			}
			
		}catch(Exception e) {
			AppConstants.logger(logger, "Exception during waiting for scan status " + e.getMessage());
		}
		return false;
	}
	
	public static boolean siteScanSelected(final int siteID,  final String myApiKey,final String mySentinelURL,final PrintStream logger) {
		AppConstants.logger(logger,"Trigger scan was selected for Site ID " + siteID);
		HttpURLConnection response = SentinelUtility.setScanNow(siteID, "",myApiKey, mySentinelURL,"site",logger);
		AppConstants.logger(logger,"Trying to run scan for site ");
		boolean isSuccess = true;
		try {
			if (response == null || response.getResponseCode() > 301) {
				AppConstants.logger(logger,"Scan now failed for site");
				isSuccess = false;
			}
		} catch (IOException e) {
			isSuccess = false;
			AppConstants.logger(logger,"Scan now failed for site - " + e.getMessage());
		}
		return isSuccess;
	}
	public static boolean checkScantimeoutExceed(final boolean scanningTimeOutSelected,final String scanWaitTimeMinutes,final long startTime, final boolean vulnerableFailBuildSelected,final PrintStream logger) {
		try {
			long scanWaitTime =0;
			if (scanningTimeOutSelected && scanWaitTimeMinutes != null && scanWaitTimeMinutes.trim().length()>0 ) {
				if(isInteger(scanWaitTimeMinutes.trim())){
					scanWaitTime =  Long.parseLong(scanWaitTimeMinutes);
					long endTime =  System.currentTimeMillis();
					long executeTimeMin = TimeUnit.MILLISECONDS.toMinutes(endTime - startTime);	
					if (vulnerableFailBuildSelected && scanningTimeOutSelected && scanWaitTimeMinutes != null && scanWaitTimeMinutes.trim().length()>0 && executeTimeMin > scanWaitTime){
						AppConstants.logger(logger,"Configured "+scanWaitTimeMinutes+" minutes as scan timeout.");
						AppConstants.logger(logger,"Scan exceeds the timeout set in build configuration." );
						return true;  
					}	
				}	
			}  
		}catch(Exception e) {
			AppConstants.logger(logger,e);
		}
		return false;
	}
	public static boolean  jenkinArchiveSelected(final String jenkinsHosts,EnvVars environment,Map<String, String> param,final PrintStream logger  ) {
		String JenkinsHostString =jenkinsHosts;
		boolean jenkinArchiveSelectedFlag = true;
		AppConstants.logger(logger,"Jenkins http server is selected");
		if(JenkinsHostString!= null && JenkinsHostString.length() > 0 && JenkinsHostString.charAt(JenkinsHostString.length()-1)=='/') {
			JenkinsHostString =JenkinsHostString.substring(0,JenkinsHostString.length()-1);
		}
		AppConstants.logger(logger,"Jenkins host is " + JenkinsHostString);
		String repositoryURL = JenkinsHostString + "/job/" 
				+ environment.get("JOB_NAME") + "/ws/" + param.get("archiveName");
		
		URL repoUrl = null;
		try {
			if (repositoryURL != null) {
				repoUrl = new URL(repositoryURL);
			}
		} catch (MalformedURLException e) {
			AppConstants.logger(logger,e.getMessage());
			return false;
		}
		if (repoUrl != null) {
			AppConstants.logger(logger,"URL to update " +repoUrl.toString());
			param.put("jenkinsRepoUrl", repoUrl.toString());
		}
		return jenkinArchiveSelectedFlag;
		//AppConstants.logger("URL to update " +repoUrl.toString());
		
	}
	
	public static String fileTypeArchieveSelected(final EnvVars environment,final  String archiveName, final String fileName, 
			final String excludeFileType, final String includeFileType , final String path,Map<String, String> param, final String workspacePath ,String sourcePath,
			final String ant, final String uploadFileSize,final PrintStream logger) {
		String finalArchive = new String();
		try {
			
			if (archiveName != null && (!archiveName.isEmpty())) {
				if (archiveName.indexOf(".tar.gz") == -1) {
					param.put("archiveName", archiveName + ".tar.gz");
				} else {
					param.put("archiveName", archiveName);
				}
	
			} else {
				param.put("archiveName", environment.get("JOB_NAME")
						+ ".tar.gz");
			}
			param.put("fileName", fileName);
			param.put("excludeFile", excludeFileType+",**/whs.xml");
			param.put("includeFile", includeFileType);
			param.put("fileName", environment.get("fileName"));
			param.put("myCustomString", environment.get("mycustomstring"));
	
			String antFilePath = path + File.separator + "build.xml";
			antFilePath = workspacePath + File.separator + "whs.xml";
			//AppConstants.logger("antFilePath=" + antFilePath);
			XMLFileUtility.write(antFilePath, ant, param,logger);
			CustomAntUtility.invokeAnt(antFilePath, param,logger);
			// String currentDir = GeneralUtility.getCleanPath();//
			// ExecuteShellComand.run("pwd",logger);
			sourcePath = path + File.separator
				+ param.get("archiveName");
			// tarPath = currentDir + param.get("archiveName");
			finalArchive = workspacePath + File.separator
				+ param.get("archiveName");
			AppConstants.logger(logger,"ZIP File Path ===> "
					+ workspacePath + File.separator
					+ param.get("archiveName"));
			AppConstants.logger(logger,"excludeFileType " + excludeFileType
						+ "  includeFileType " + includeFileType
						+ " uploadFileSize " + uploadFileSize);
			
			param.put("SOURCE_FILE_PATH", workspacePath
					+ File.separator + param.get("archiveName"));
		}catch(Exception ex) {
			AppConstants.logger(logger,ex);
		}
		return finalArchive;
		
	}
	
	public static String fileTypeBinarySelected(Map<String, String> param, String sourcePath, final String binaryName, String path, String workspacePath,final PrintStream logger) {
		String finalArchive = new String();
		try {
			param.put("archiveName", binaryName);
			sourcePath = path + File.separator
					+ param.get("archiveName");
			finalArchive = workspacePath + File.separator
					+ param.get("archiveName");
			AppConstants.logger(logger,"Binary File Path ===> "
					+ workspacePath + File.separator
					+ param.get("archiveName"));
			
			param.put("SOURCE_FILE_PATH", workspacePath
					+ File.separator + param.get("archiveName"));
			
		}catch(Exception ex) {
			AppConstants.logger(logger,ex);
		}
		return finalArchive;
	}
	
	
	public static boolean archieveMethodSFTPSelected(Map<String, String> param,final String sFtpURL, final String sFtpUID, final String sFtpPass, String sFtpFolderPath,PrintStream logger) {
		AppConstants.logger(logger,"SFTP is selected");
		//Host needed to be used for SFTP upload
		if(sFtpURL.indexOf('/') > -1) {
			param.put("HOST",
				sFtpURL.substring(sFtpURL.indexOf('/') + 2));
		}
		else {
			param.put("HOST", sFtpURL);
		}
		//Host needed to be updated in sentinel
		param.put("SFTP_HOST", sFtpURL);
		param.put("USER", sFtpUID);
		param.put("PASSWD", sFtpPass);
		param.put("UPLOAD_FOLDER", sFtpFolderPath);
		boolean isFTPSuccess = true;
		try {
			isFTPSuccess = SFTPUpload.execute(param, logger);
			if(!isFTPSuccess) {
				AppConstants.logger(logger,"SFTP Upload failed");
				return false;
			}
		} catch (Exception e) {
			AppConstants.logger(logger,e);
			return false;
		}
		return isFTPSuccess;
	}
	
	
	public static boolean archieveMethodAppliaceSelected(Map<String, String> param, final String myApiKey, final String mySentinelURL,final PrintStream logger) {
		boolean archieveApplianceSelectedFlag = true;
		String applianceRepoUrl = null;
		try {
			 applianceRepoUrl = MultipartFileUploader.upload(param,myApiKey,mySentinelURL,logger);
		} catch (CustomException e) {
			AppConstants.logger(logger,e);
		}
		
		if (applianceRepoUrl != null){
			URL repoUrl;
			try {
				repoUrl = new URL(applianceRepoUrl);
			} catch (Exception e) {
				AppConstants.logger(logger,e);
				return false;
			}
			AppConstants.logger(logger,"URL to update " +repoUrl.toString());
			param.put("applianceRepoUrl", repoUrl.toString());
		}
		else {
			return false;
		}
		return archieveApplianceSelectedFlag;
		
	}
	
	public static String archieveSelected(Map<String, String> param,final EnvVars environment, final String workspacePath,final PrintStream logger) {
		String path ="";
		try {
			path = environment.get("JENKINS_HOME") + File.separator
			+ "jobs" + File.separator + environment.get("JOB_NAME")
			+ File.separator + "builds" + File.separator
			+ environment.get("BUILD_NUMBER");
	
			param.put("BuildNo", environment.get("BUILD_NUMBER"));
			param.put("JobPath", path);
			param.put("workspacePath", workspacePath);
		}catch(Exception e) {
			AppConstants.logger(logger,e);
		}
		return path;
	}  
	public static boolean isInteger( String input )
	{
	   try{
	     int value = Integer.parseInt(input);
	      if (value< 0) {
	    	  return false;
	      }
	      return true;
	   }catch( Exception e) {
	      return false;
	   }
	}
}
