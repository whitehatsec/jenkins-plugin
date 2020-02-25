package com.whitehat.sentinel.plugin.jenkins.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

import com.whitehat.sentinel.plugin.jenkins.shared.AppConstants;
import com.whitehat.sentinel.plugin.jenkins.utils.CustomException;

public class WhiteHatConnection implements Connection {
	
	public  boolean validate(String apiKey, String sentinelURL) throws ValidateException{
		
		
		String validation_api = sentinelURL + AppConstants.USER_API;
		boolean validation = true;
		int responseCode;
		
		URL url;
		try {
			url = new URL(validation_api);
			SentinelUtility.setupsslnoverify();
			
			HttpURLConnection conn = SentinelUtility.getHttpURLConnection(url,null);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("key", apiKey);
			
			responseCode = conn.getResponseCode();
		} catch (KeyManagementException | NoSuchAlgorithmException | IOException  e ) {
			
			throw new ValidateException(e);
		} 
		
		
		
		if(responseCode > 300) {
			validation = false;
		}
		
		return validation;
	}

}
