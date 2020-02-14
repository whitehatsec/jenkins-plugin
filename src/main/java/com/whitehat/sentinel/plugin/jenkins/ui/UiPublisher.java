
package com.whitehat.sentinel.plugin.jenkins.ui;

  

import hudson.util.FormValidation;
import hudson.util.HttpResponses;
import hudson.util.Secret;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import jenkins.model.JenkinsLocationConfiguration;
import jenkins.tasks.SimpleBuildStep;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.WorkspaceSnapshot;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.model.Descriptor.FormException;
import hudson.remoting.VirtualChannel;
import hudson.slaves.SlaveComputer;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;



import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import com.whitehat.sentinel.plugin.jenkins.controller.Connection;
import com.whitehat.sentinel.plugin.jenkins.controller.SentinelUtility;
import com.whitehat.sentinel.plugin.jenkins.controller.WhiteHatConnection;
import com.whitehat.sentinel.plugin.jenkins.shared.AppConstants;
import com.whitehat.sentinel.plugin.jenkins.utils.CustomException;
import com.whitehat.sentinel.plugin.jenkins.utils.GeneralUtility;
import com.whitehat.sentinel.plugin.jenkins.utils.MultipartFileUploader;
import com.whitehat.sentinel.plugin.jenkins.utils.RestrictFileSize;
import com.whitehat.sentinel.plugin.jenkins.utils.SFTPUpload;
import com.whitehat.sentinel.plugin.jenkins.utils.ant.CustomAntUtility;
import com.whitehat.sentinel.plugin.jenkins.utils.ant.XMLFileUtility;

import java.util.concurrent.TimeUnit;
/**
 * {@link Recorder} that archives a build's workspace (or subset thereof) as a
 * {@link WorkspaceSnapshot}, for use by another project using
 * {@link CloneWorkspaceSCM}.
 * 

 */


public class UiPublisher extends Recorder implements SimpleBuildStep  {

	private final String server;
	private String site,app,codebase,archiveMethodSelected, fileTypeSelected;
	private final String apiKey,sentinelURL,serverSelected, globalApiKey,globalSentinelURL;
	private final String archiveName,binaryName;
	private final String fileName;
	private final String applianceIP;
	private  String sFtpURL, sFtpUID, sFtpPass, sFtpFolderPath,jenkinsHosts;
	private boolean delArcUploadAppliance,delArcUploadSFTP,triggerScan,failBuild,archiveSelected,uselocalCredentials;
	private String workspacePath,satellitePort,uploadFileSize,clientId,excludeFileType, includeFileType,destPath,ant = "";
	private  String newVulnerabilityCritical,newVulnerabilityHigh,newVulnerabilityMedium,newVulnerabilityLow,newVulnerabilityNote,newVulnerabilityTotal;
	private  String openVulnerabilityCritical,openVulnerabilityHigh,openVulnerabilityMedium,openVulnerabilityLow,openVulnerabilityNote,openVulnerabilityTotal;
	private  String allVulnerabilityCritical,allVulnerabilityHigh,allVulnerabilityMedium,allVulnerabilityLow,allVulnerabilityNote,allVulnerabilityTotal;
	
	private boolean vulnerableFailBuildSelected,scanningTimeOutSelected;
	private  String scanWaitTimeMinutes;
	@DataBoundConstructor
	public UiPublisher(boolean uselocalCredentials,String serverSelected,String apiKey, String sentinelURL, String site, String app,String codebase,
			String archiveMethodSelected, String fileTypeSelected, String applianceIP,
			String archiveName, String binaryName, String ant,
			String destPath, String workspacePath, String server,
			String sFtpURL, String sFtpUID, String sFtpPass,
			String sFtpFolderPath, String fileName, String jenkinsHosts,boolean archiveSelected,
			String excludeFileType, String includeFileType,
			String uploadFileSize, String clientId,boolean delArcUploadAppliance,boolean delArcUploadSFTP,boolean triggerScan, boolean failBuild,boolean vulnerableFailBuildSelected,boolean scanningTimeOutSelected,
			String newVulnerabilityCritical, String newVulnerabilityHigh, String newVulnerabilityMedium,String newVulnerabilityLow, String newVulnerabilityNote, String newVulnerabilityTotal,
			String openVulnerabilityCritical, String openVulnerabilityHigh, String openVulnerabilityMedium, String openVulnerabilityLow,String openVulnerabilityNote,String openVulnerabilityTotal,
			String allVulnerabilityCritical,String allVulnerabilityHigh, String allVulnerabilityMedium, String allVulnerabilityLow,String allVulnerabilityNote,String allVulnerabilityTotal,
			String scanWaitTimeMinutes ) {
		this.serverSelected = serverSelected;
		this.uselocalCredentials = uselocalCredentials;
		this.site = site;
		this.app = app;
		this.archiveMethodSelected = archiveMethodSelected;
		this.fileTypeSelected = fileTypeSelected;
		this.codebase = codebase;
		this.clientId = clientId;
		this.uploadFileSize = uploadFileSize;
		this.excludeFileType = excludeFileType;
		this.includeFileType = includeFileType;
		this.globalApiKey = this.getDescriptor().getApiKey();
		this.globalSentinelURL = this.getDescriptor().getSentinelURL();
		if(apiKey != null && !apiKey.isEmpty()) {
			Secret secretKey = Secret.fromString(apiKey);
			String newApiKey = secretKey.getEncryptedValue();
			this.apiKey = newApiKey;
		}
		else {
			this.apiKey = apiKey;
		}
		this.sentinelURL = sentinelURL;
		
		this.applianceIP = applianceIP;

		
		this.archiveName = archiveName;
		this.binaryName = binaryName;
		this.ant = ant;
		this.workspacePath = workspacePath;
		this.destPath = destPath;
		this.server = server;
		this.sFtpURL = sFtpURL;
		this.sFtpUID = sFtpUID;
		this.sFtpPass = sFtpPass;
		this.sFtpFolderPath = sFtpFolderPath;
		this.archiveSelected = archiveSelected;
		this.fileName = fileName;
		this.jenkinsHosts = jenkinsHosts;
		this.delArcUploadAppliance = delArcUploadAppliance;
		this.delArcUploadSFTP = delArcUploadSFTP;
		this.triggerScan=triggerScan;
		this.failBuild = failBuild;
		this.newVulnerabilityCritical = newVulnerabilityCritical;
		this.newVulnerabilityHigh = newVulnerabilityHigh;
		this.newVulnerabilityMedium = newVulnerabilityMedium;
		this.newVulnerabilityLow = newVulnerabilityLow ;
		this.newVulnerabilityNote =newVulnerabilityNote;
		this.newVulnerabilityTotal =newVulnerabilityTotal;
		this.openVulnerabilityCritical = openVulnerabilityCritical;
		this.openVulnerabilityHigh = openVulnerabilityHigh;
		this.openVulnerabilityMedium = openVulnerabilityMedium;
		this.openVulnerabilityLow = openVulnerabilityLow;
		this.vulnerableFailBuildSelected = vulnerableFailBuildSelected;	
		this.scanningTimeOutSelected = scanningTimeOutSelected;
		this.openVulnerabilityNote = openVulnerabilityNote;
		this.openVulnerabilityTotal = openVulnerabilityTotal;
		this.allVulnerabilityCritical = allVulnerabilityCritical;
		this.allVulnerabilityHigh = allVulnerabilityHigh;
		this.allVulnerabilityMedium = allVulnerabilityMedium;
		this.allVulnerabilityLow = allVulnerabilityLow;
		this.allVulnerabilityNote = allVulnerabilityNote;
		this.allVulnerabilityTotal = allVulnerabilityTotal;
		this.scanWaitTimeMinutes = scanWaitTimeMinutes;
	}
	
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException {

		PrintStream logger = listener.getLogger();
		VirtualChannel v = launcher.getChannel();

		try {  

			EnvVars environment = build.getEnvironment(listener);
			AppConstants.logger(logger,"WhiteHat Sentinel Plugin V 2.3.3");

			if (Computer.currentComputer() instanceof SlaveComputer) {
				AppConstants.logger(logger,"Plugin Executing on Slave Host");
			} else {
				AppConstants.logger(logger,"Plugin Executing on Master Host");

			}
			
			Secret secretApiKey = Secret.fromString(globalApiKey);
			String globalApiKeyDecoded = Secret.toString(secretApiKey);
			
			secretApiKey = Secret.fromString(apiKey);
			String localApiKeyDecoded = Secret.toString(secretApiKey);
			
			Properties systemProperties = v.call(new GetSystemProperties(
					environment, listener, ant, build.getResult(),serverSelected, uselocalCredentials, localApiKeyDecoded,sentinelURL,
					globalApiKeyDecoded,globalSentinelURL,archiveSelected,
					site,app,codebase, archiveMethodSelected, fileTypeSelected,applianceIP,
					archiveName,binaryName, ant, destPath,
					workspacePath,server, sFtpURL, sFtpUID, sFtpPass,
					sFtpFolderPath, fileName,jenkinsHosts,
					excludeFileType, includeFileType, uploadFileSize, clientId,
					delArcUploadAppliance,delArcUploadSFTP,triggerScan,failBuild,
					newVulnerabilityCritical,newVulnerabilityHigh,newVulnerabilityMedium,newVulnerabilityLow,newVulnerabilityNote,newVulnerabilityTotal,
					openVulnerabilityCritical,openVulnerabilityHigh,openVulnerabilityMedium,openVulnerabilityLow,openVulnerabilityNote,openVulnerabilityTotal,
					allVulnerabilityCritical,allVulnerabilityHigh,allVulnerabilityMedium,allVulnerabilityLow,allVulnerabilityNote,allVulnerabilityTotal,
					scanWaitTimeMinutes,vulnerableFailBuildSelected,scanningTimeOutSelected
					));

			boolean result = Boolean.parseBoolean(systemProperties
					.get("result").toString());
			//if(failBuild) {
				return result;
			//}
				

		} catch (IOException e) {
			AppConstants.logger(logger,e.getMessage());
		} catch (RuntimeException e) {
			AppConstants.logger(logger,e.getMessage());
		}
		return false;
	}

	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		
		private static final String CLASS_NAME = DescriptorImpl.class.getName();
		private  String apiKey,sentinelURL,serverSelected;
		private Connection connection = new WhiteHatConnection();
		private Map<Integer, String>  appsArray = new  LinkedHashMap<Integer, String>();
		private Map<Integer, String>  siteArray = new LinkedHashMap<Integer, String>();
		private Map<Integer, String> codebaseArray = new HashMap<Integer,String>();
		private Map<Integer, String> applianceArray= new HashMap<Integer,String>();
		public DescriptorImpl() {
			super(UiPublisher.class);
		}
		
		
		public String getDisplayName() {
			return "WhiteHat Sentinel Plugin";// Messages.CloneWorkspacePublisher_DisplayName();
		}

		
		
		public FormValidation doGetVulnStats(@QueryParameter("apiKey") final String appSpecificApiKey,
		        @QueryParameter("sentinelURL") final String appSpecificSentinelURL,
		        @QueryParameter("uselocalCredentials") final boolean uselocalCredentials,
		        @QueryParameter("app") final String appSelected,
		        @QueryParameter("typeOfVulnerabilty") final String typeOfVulnerabilty,PrintStream logger) throws IOException, ServletException{
			String myApiKey;
			String mySentinelURL;
			Map<String, Integer> vulnerabilityDataMap=new HashMap<String, Integer>();
			
			if(!uselocalCredentials) {
				myApiKey = this.apiKey;
				mySentinelURL = this.sentinelURL;
			}
			else {
				myApiKey = appSpecificApiKey;
				mySentinelURL = appSpecificSentinelURL;
			}
			if(Integer.parseInt(appSelected) == 0) {
				FormValidation.error("Select a SAST asset");
			}
			if (myApiKey != null && (!myApiKey.isEmpty())) {
				
				Secret secretApiKey = Secret.fromString(myApiKey);
				myApiKey = Secret.toString(secretApiKey);
				vulnerabilityDataMap =  SentinelUtility.getVulnerabilitiesCount(myApiKey, mySentinelURL,Integer.parseInt(appSelected),logger);
				//Check Condition vulnerabilityDataMap is Empty. 
				
				String newVulnsData= "<table  width=\"78%\"> <col width=\"18%\"/>\n" + 
						"<col width=\"10%\"/>\n" + 
						"<col width=\"10%\"/>\n" + 
						"<col width=\"10%\"/>\n" + 
						"<col width=\"10%\"/>\n" + 
						"<col width=\"10%\"/>\n" + 
						"<col width=\"10%\"/>\n" + 
						"<thead>\n" +   
						"<tr>\n" + 
							"<td width=\"18%\" style=\"font-weight:bold\"></td>\n" + 
							"<td width=\"10%\"  style=\"font-weight:bold\">Critical</td>\n" + 
							"<td width=\"10%\"   style=\"font-weight:bold\">High</td>\n" + 
							"<td width=\"10%\"  style=\"font-weight:bold\">Medium</td>\n" + 
							"<td width=\"10%\"  style=\"font-weight:bold\">Low</td>\n" + 
							"<td width=\"10%\"  style=\"font-weight:bold\">Note</td>\n" + 
							"<td width=\"10%\"  style=\"font-weight:bold\">Total</td>\n" + 
						"<tbody><tr>\n" +  
							"<td align=\"left\" style=\"font-weight:bold\">Open</td>"+
					 		"<td >"+vulnerabilityDataMap.get("openCriticalVulnerability")+"</td>\n" + 
					 		"<td >"+vulnerabilityDataMap.get("openHighVulnerability")+"</td>\n" + 
					 		"<td >"+vulnerabilityDataMap.get("openMediumVulnerability")+"</td>\n" + 
					 		"<td >"+vulnerabilityDataMap.get("openLowVulnerability")+ "</td>\n" + 
					 		"<td >"+vulnerabilityDataMap.get("openNoteVulnerability")+"</td>\n" + 
					 		"<td >"+vulnerabilityDataMap.get("totalOpenVulnerability")+"</td>\n" + 
					 	"</tr>"+
					 	"<tr>"+
					 		"<td align=\"left\" style=\"font-weight:bold\">Pending Verification (Standard Edition Only)</td>"+
					 		"<td >"+vulnerabilityDataMap.get("newCriticalVulnerability")+"</td>\n" + 
					 		"<td >"+vulnerabilityDataMap.get("newHighVulnerability")+"</td>\n" + 
					 		"<td >"+vulnerabilityDataMap.get("newMediumVulnerability")+"</td>\n" + 
					 		"<td>"+vulnerabilityDataMap.get("newLowVulnerability")+"</td>\n" + 
					 		"<td >"+vulnerabilityDataMap.get("newNoteVulnerability")+"</td>\n" + 
					 		"<td >"+vulnerabilityDataMap.get("totalNewVulnerability")+"</td>\n" +
			 		    "</tr>  "+  
			 		    "<tr>  "+
					 		"<td align=\"left\" style=\"font-weight:bold\">All (Open and Pending Verification)</td>"+
					 		"<td >"+(vulnerabilityDataMap.get("openCriticalVulnerability")+ vulnerabilityDataMap.get("newCriticalVulnerability"))+"</td>\n" + 
					 		"<td >"+(vulnerabilityDataMap.get("openHighVulnerability")+ vulnerabilityDataMap.get("newHighVulnerability"))+"</td>\n" + 
					 		"<td >"+(vulnerabilityDataMap.get("openMediumVulnerability")+ vulnerabilityDataMap.get("newMediumVulnerability"))+"</td>\n" + 
					 		"<td >"+(vulnerabilityDataMap.get("openLowVulnerability")+ vulnerabilityDataMap.get("newLowVulnerability"))+"</td>\n" + 
					 		"<td >"+(vulnerabilityDataMap.get("openNoteVulnerability")+ vulnerabilityDataMap.get("newNoteVulnerability"))+"</td>\n" + 
					 		"<td >"+(vulnerabilityDataMap.get("totalOpenVulnerability")+ vulnerabilityDataMap.get("totalNewVulnerability"))+"</td>\n" + 
					 	"</tr>  "+
					 	"</tbody></table>";
				return FormValidation.respond(null, newVulnsData);
			}
		return FormValidation.error("Application key is not valid");
		}
		
		public FormValidation doAddApplication(@QueryParameter("apiKey") final String appSpecificApiKey,
		        @QueryParameter("sentinelURL") final String appSpecificSentinelURL,
		        @QueryParameter("uselocalCredentials") final boolean uselocalCredentials,
		        @QueryParameter("newAppName") final String appName,
		        @QueryParameter("language") final String language,
		        @QueryParameter("appliance") final String appliance
		        ) throws IOException, ServletException{
			
				String myApiKey;
				String mySentinelURL;
				
				
				if(Integer.parseInt(appliance) == 0) {
					FormValidation.error("No appliance selected");
				}
				
				if(!uselocalCredentials) {
					myApiKey = this.apiKey;
					mySentinelURL = this.sentinelURL;
				}
				else {
					myApiKey = appSpecificApiKey;
					mySentinelURL = appSpecificSentinelURL;
				}
				
				if (myApiKey != null && (!myApiKey.isEmpty())) {
					
					Secret secretApiKey = Secret.fromString(myApiKey);
					myApiKey = Secret.toString(secretApiKey);
					HttpURLConnection response = SentinelUtility.createApplication(appName, language, appliance, myApiKey, mySentinelURL);
					if(response == null) {
						FormValidation.error("Unknown Error");
					}
					if(response.getResponseCode() < 302) {
						
						return FormValidation.ok("Click done and then select newly created asset from the dropdown.");
					}
					else {
						return FormValidation.error(response.getResponseMessage());
					}
				}
				else {
					return FormValidation.error("No API Key found");
				}
		}
		
		public FormValidation doAddCodebase(@QueryParameter("apiKey") final String appSpecificApiKey,
		        @QueryParameter("sentinelURL") final String appSpecificSentinelURL,
		        @QueryParameter("uselocalCredentials") final boolean uselocalCredentials,
		        @QueryParameter("newCodebaseName") final String codebaseName,
		        @QueryParameter("app") final String appSelected) throws IOException, ServletException{
			
				String myApiKey;
				String mySentinelURL;
				if(!uselocalCredentials) {
					myApiKey = this.apiKey;
					mySentinelURL = this.sentinelURL;
				}
				else {
					myApiKey = appSpecificApiKey;
					mySentinelURL = appSpecificSentinelURL;
				}
				
				if(Integer.parseInt(appSelected) == 0) {
					FormValidation.error("Select a SAST asset");
				}
				
				if (myApiKey != null && (!myApiKey.isEmpty())) {
					
					Secret secretApiKey = Secret.fromString(myApiKey);
					myApiKey = Secret.toString(secretApiKey);
					HttpURLConnection response = SentinelUtility.createCodebase(codebaseName, appSelected, myApiKey, mySentinelURL);
					if(response == null) {
						FormValidation.error("Unknown Error");
					}
					if(response.getResponseCode() < 302) {
						//this.doFillAppItems(uselocalCredentials, appSpecificApiKey, appSpecificSentinelURL);
						return FormValidation.ok("Click done and then select newly created codebase from the dropdown.");
					}
					else {
						return FormValidation.error(response.getResponseMessage());
					}
				}
				else {
					return FormValidation.error("No API key found.");
				}
		}
		
		
		
		public FormValidation doValidateConnection(@QueryParameter("apiKey") final String apiKey,
		        @QueryParameter("sentinelURL") final String sentinelURL, @QueryParameter("serverSelected") final String serverSelected,
		        @QueryParameter("useLanguage") final String useLanguage) throws IOException, ServletException {
		    try {
		    	Secret myApiKey = Secret.fromString(apiKey);
		    	String apiKeyInput = Secret.toString(myApiKey); 
				boolean validateResult = connection.validate(apiKeyInput,sentinelURL);
				if(validateResult) {
					return FormValidation.ok("Successfully authenticated.");
				}
				else {
					return FormValidation.error("Please provide valid credentials.");
				}
		    	 	
		    }catch (Exception e) {
		        return FormValidation.error("Please provide valid credentials.");
		    }
		}

		public ListBoxModel doFillSiteItems(@QueryParameter final boolean uselocalCredentials,
				@QueryParameter("apiKey") final String appSpecificApiKey,
				@QueryParameter("sentinelURL") final String appSpecificSentinelURL) {
			String myApiKey;
			String mySentinelURL;
			if(!uselocalCredentials) {
				myApiKey = apiKey;
				mySentinelURL = sentinelURL;
			}
			else {
				myApiKey = appSpecificApiKey;
				mySentinelURL = appSpecificSentinelURL;
			}
			ListBoxModel items = new ListBoxModel();
			if (myApiKey != null && (!myApiKey.isEmpty())) {

				Secret secretApiKey = Secret.fromString(myApiKey);
				myApiKey = Secret.toString(secretApiKey);	
				siteArray =SentinelUtility.getSites(myApiKey,mySentinelURL);
				int siteLength = siteArray.size();
				if (siteLength > 0) {
					Iterator it = siteArray.entrySet().iterator();
					items.add(new Option("Select an Option" , "0" , false));
					while(it.hasNext()) {
						Map.Entry app = (Map.Entry)it.next();
						items.add(new Option(app.getValue().toString(),app.getKey().toString(),false));
					}
				}
				else{
					items.add(new Option("No DAST asset found.",
							"0", false));
				}
			}
			else {
				items.add(new Option("Please enter API key.",
						"0", false));
			}

			return items;
		}
	
public ListBoxModel doFillAppItems(@QueryParameter final boolean uselocalCredentials,
												@QueryParameter("apiKey") final String appSpecificApiKey,
												@QueryParameter("sentinelURL") final String appSpecificSentinelURL
												) {
			
			String myApiKey;
			String mySentinelURL;
			if(!uselocalCredentials) {
				myApiKey = apiKey;
				mySentinelURL = sentinelURL;
			}
			else {
				myApiKey = appSpecificApiKey;
				mySentinelURL = appSpecificSentinelURL;
			}
			ListBoxModel items = new ListBoxModel();
			if (myApiKey != null && (!myApiKey.isEmpty())) {
				
				Secret secretApiKey = Secret.fromString(myApiKey);
				myApiKey = Secret.toString(secretApiKey);	
				appsArray =SentinelUtility.getApps(myApiKey,mySentinelURL);
				int appsLength = appsArray.size();
				if (appsLength > 0) {
					Iterator it = appsArray.entrySet().iterator();
					items.add(new Option("Select an Option" , "0" , false));
					while(it.hasNext()) {
						Map.Entry app = (Map.Entry)it.next();
						items.add(new Option(app.getValue().toString(),app.getKey().toString(),false));
					}
				}
				else{
					items.add(new Option("No SAST asset found.",
						"0", false));
				}
			}
			else {
				items.add(new Option("Please enter API key.",
						"0", false));
			}
			
			
			return items;
	}



public ListBoxModel doFillApplianceItems(@QueryParameter final boolean uselocalCredentials,
		@QueryParameter("apiKey") final String appSpecificApiKey,
		@QueryParameter("sentinelURL") final String appSpecificSentinelURL
		) {

	String myApiKey;
	String mySentinelURL;
	if(!uselocalCredentials) {
		myApiKey = apiKey;
		mySentinelURL = sentinelURL;
	}
	else {
		myApiKey = appSpecificApiKey;
		mySentinelURL = appSpecificSentinelURL;
	}
	ListBoxModel items = new ListBoxModel();
	if (myApiKey != null && (!myApiKey.isEmpty())) {

		Secret secretApiKey = Secret.fromString(myApiKey);
		myApiKey = Secret.toString(secretApiKey);	
		applianceArray =SentinelUtility.getAppliances(myApiKey,mySentinelURL);
		int appsLength = applianceArray.size();
		if (appsLength > 0) {
			items.add(new Option("Select an Option" , "0" , false));
			Iterator it = applianceArray.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry app = (Map.Entry)it.next();
				items.add(new Option(app.getValue().toString(),app.getKey().toString(),false));
			}
		}
		else{
			items.add(new Option("No appliance found",
					"0", false));
		}
	}
	else {
		items.add(new Option("Please enter API Key",
				"0", false));
	}


	return items;
}


	public ListBoxModel doFillCodebaseItems(@QueryParameter final boolean uselocalCredentials,
		@QueryParameter("apiKey") final String appSpecificApiKey,
		@QueryParameter("sentinelURL") final String appSpecificSentinelURL,
		@QueryParameter("app") final String appSelected
		) {

			String myApiKey;
			String mySentinelURL;
			ListBoxModel items = new ListBoxModel();
			if(appSelected == null || appSelected.isEmpty() || Integer.parseInt(appSelected) == 0) {
				items.add(new Option("Need SAST asset to be selected",
						"0", false));
				return items;
			}
			
			if(!uselocalCredentials) {
				myApiKey = apiKey;
				mySentinelURL = sentinelURL;
			}
			else {
				myApiKey = appSpecificApiKey;
				mySentinelURL = appSpecificSentinelURL;
			}
			
			if (myApiKey != null && (!myApiKey.isEmpty())) {
				Secret secretApiKey = Secret.fromString(myApiKey);
				myApiKey = Secret.toString(secretApiKey);
				codebaseArray =SentinelUtility.getCodebases(myApiKey,mySentinelURL,appSelected);
				int codebaseLength = codebaseArray.size();
				if (codebaseLength > 0) {
					Iterator it = codebaseArray.entrySet().iterator();
					items.add(new Option("Select an Option" , "0" , false));
					
					while(it.hasNext()) {
						Map.Entry app = (Map.Entry)it.next();
						items.add(new Option(app.getValue().toString(),app.getKey().toString(),false));
					}
				}
				else{
					items.add(new Option("No source code archive codebase found.",
							"0", false));
				}
				
				
			}else {
				items.add(new Option("Please enter API key.",
						"0", false));
			}
		return items;
	}
		
		@Override
		//Invoke when global conf is saved.
		public boolean configure(StaplerRequest req, JSONObject formData)
				throws FormException
		{
			final String METHOD_NAME = CLASS_NAME+".configure";

			String newApiKey = formData.getString("apiKey");
			if(newApiKey != null && !newApiKey.isEmpty()) {
				Secret secretKey = Secret.fromString(newApiKey);
				newApiKey = secretKey.getEncryptedValue();
				this.apiKey = newApiKey;
			}
			
			String sentinelURL = formData.getString("sentinelURL");
			if( null != sentinelURL && !sentinelURL.isEmpty() && !sentinelURL.equals(this.sentinelURL) )
				{
					this.sentinelURL = sentinelURL;
				}
			
			save();
			return super.configure(req,formData);
		}
		
		public String getJenkinsHost(){
			JenkinsLocationConfiguration globalConfig = new JenkinsLocationConfiguration();
			return globalConfig.getUrl();
		}
		
		public void setConnection(Connection connection){
			if(connection == null) {
				throw new IllegalArgumentException("No Connection object passed");
			}
			this.connection = connection;
		}
		
		public String getAntValue() {

			StringBuilder antContent = new StringBuilder();

			antContent
					.append("<project basedir=\".\" default=\"makeZip\" name=\"Workspace Zip\">\n");
			antContent
					.append("\t<target description=\"Create a zip for the workspace\" name=\"makeZip\">\n");
			antContent.append("\t\t<tstamp>\n");
			antContent
					.append("\t\t<format property=\"BUILD_TIME_STAMP\" pattern=\"yyyyMMddHHmm\"/>\n");
			antContent.append("\t\t</tstamp>\n");
			antContent
					.append("\t\t<property name=\"basefolderName\" value=\"${BuildNo}_${BUILD_TIME_STAMP}\"/>\n");
			antContent
					.append("\t\t<delete file=\"${workspacePath}/${zipFilePath}.tar.gz\"/>\n");
			antContent.append("\t\t<tar destfile=\"${zipFilePath}.tar\" >\n");
			antContent
					.append("\t\t<tarfileset dir=\"${workspacePath}\" prefix=\"/${basefolderName}\" preserveLeadingSlashes=\"false\" excludes=\"${excludeFile}\" includes=\"${includeFile}\">\n");
			antContent.append("\t\t</tarfileset>\n");
			antContent.append("\t\t</tar>\n");
			antContent
					.append("\t\t<gzip destfile=\"${zipFilePath}.tar.gz\" src=\"${zipFilePath}.tar\"/>\n");
			antContent
					.append("\t\t<delete file=\"${zipFilePath}.tar\" quiet=\"true\"/>\n");
			antContent
					.append("\t\t<echo> Zip FilePath:  ${zipFilePath}.tar.gz</echo>\n");
			antContent
					.append("\t\t<move file=\"${zipFilePath}.tar.gz\" todir=\"${workspacePath}\"/>\n");
			antContent.append("\t</target>\n");
			antContent.append("</project>\n");

			return antContent.toString();

		}

		public String getDefaultValue() {
			return "";
		}

		/**
		 * Performs on-the-fly validation on the file mask wildcard.
		 */
		public FormValidation doCheckWorkspaceGlob(
				@AncestorInPath AbstractProject project,
				@QueryParameter String value) throws IOException {

			return FilePath.validateFileMask(project.getSomeWorkspace(), value);
		}

		@Override
		public UiPublisher newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			return req.bindJSON(UiPublisher.class, formData);
		}

		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		public String getApiKey() {
			return apiKey;
		}

		public String getSentinelURL() {
			return sentinelURL;
		}
		
		public String getServerSelected() {
			return serverSelected;
		}
		
		public String isServerSelected(String serverSelectedName){
			if(serverSelected == null) {
				return "";
			}
			return this.serverSelected.equalsIgnoreCase(serverSelectedName) ? "true" : "";
		}
		
		public void setServerSelected(String serverSelected) {
			this.serverSelected = serverSelected;
		}

		public void setSentinelURL(String sentinelURL) {
			this.sentinelURL = sentinelURL;
		}

		public void setApiKey(String apiKey) {
			Secret secretKey = Secret.fromString(apiKey);
			String newApiKey = secretKey.getEncryptedValue();
			
			this.apiKey = newApiKey;
		}
	}

	private static final Logger LOGGER = Logger.getLogger(UiPublisher.class
			.getName());

	private static class GetSystemProperties implements
			hudson.remoting.Callable<Properties, RuntimeException> {
		
		Result result = null;
		EnvVars localenv = null;
		TaskListener locallistener = null;
		
		private final String server, site, app,codebase, apiKey,sentinelURL,archiveMethodSelected,fileTypeSelected,
							globalApiKey,globalSentinelURL,archiveName, binaryName,fileName,serverSelected;
		private final String applianceIP;
		private final String sFtpURL, sFtpUID, sFtpPass, sFtpFolderPath,jenkinsHosts;
		private boolean delArcUploadAppliance,delArcUploadSFTP,triggerScan,failBuild,archiveSelected,uselocalCredentials;
		private  String newVulnerabilityCritical,newVulnerabilityHigh,newVulnerabilityMedium,newVulnerabilityLow,newVulnerabilityNote,newVulnerabilityTotal;
		private  String openVulnerabilityCritical,openVulnerabilityHigh,openVulnerabilityMedium,openVulnerabilityLow,openVulnerabilityNote,openVulnerabilityTotal;
		private boolean vulnerableFailBuildSelected;
		private boolean scanningTimeOutSelected;
		
		private  String allVulnerabilityCritical,allVulnerabilityHigh,allVulnerabilityMedium,allVulnerabilityLow,allVulnerabilityNote,allVulnerabilityTotal;
		private String scanWaitTimeMinutes;
		private String workspacePath,uploadFileSize,excludeFileType, includeFileType,ant = "";
		public GetSystemProperties(EnvVars environment, TaskListener listener,
				String ant1, Result result, String serverSelected,boolean uselocalCredentials,String apiKey, String sentinelURL, 
				String globalApiKey,String globalSentinelURL,boolean archiveSelected, String site, String app,
				String codebase,String archiveMethodSelected,String fileTypeSelected,String applianceIP,
				String archiveName, String binaryName, String ant, String destPath,
				String workspacePath, String server, String sFtpURL,
				String sFtpUID, String sFtpPass, String sFtpFolderPath,
				String fileName,String jenkinsHosts,
				String excludeFileType, String includeFileType,
				String uploadFileSize, String clientId,boolean delArcUploadAppliance, boolean delArcUploadSFTP,boolean triggerScan,boolean failBuild,
				String newVulnerabilityCritical, String newVulnerabilityHigh, String newVulnerabilityMedium,String newVulnerabilityLow,String newVulnerabilityNote, String newVulnerabilityTotal,
				String openVulnerabilityCritical,String openVulnerabilityHigh,String openVulnerabilityMedium,String openVulnerabilityLow,String openVulnerabilityNote,String openVulnerabilityTotal,
				String allVulnerabilityCritical,String allVulnerabilityHigh, String allVulnerabilityMedium, String allVulnerabilityLow,String allVulnerabilityNote,String allVulnerabilityTotal,
				String scanWaitTimeMinutes,
				boolean vulnerableFailBuildSelected,
				boolean scanningTimeOutSelected) {
			localenv = environment;
			locallistener = listener;
			ant = ant1;
			if(ant == null || ant.isEmpty()){
				ant = getAntValue();
			}
			this.result = result;
			this.serverSelected = serverSelected;
			this.uselocalCredentials = uselocalCredentials;
			this.globalApiKey = globalApiKey;
			this.globalSentinelURL = globalSentinelURL;
			this.uploadFileSize = uploadFileSize;
			this.excludeFileType = excludeFileType;
			this.includeFileType = includeFileType;
			this.apiKey = apiKey;
			this.sentinelURL = sentinelURL;
			this.archiveSelected = archiveSelected;
			this.site = site;
			this.app = app;
			this.codebase = codebase;
			this.archiveMethodSelected = archiveMethodSelected;
			this.fileTypeSelected = fileTypeSelected;
			this.applianceIP = applianceIP;
			this.archiveName = archiveName;
			this.binaryName = binaryName;
			this.ant = ant;
			this.workspacePath = workspacePath;
			this.server = server;
			this.sFtpURL = sFtpURL;
			this.sFtpUID = sFtpUID;
			this.sFtpPass = sFtpPass;
			this.sFtpFolderPath = sFtpFolderPath;
			this.fileName = fileName;
			this.jenkinsHosts = jenkinsHosts;
			this.delArcUploadAppliance = delArcUploadAppliance;
			this.delArcUploadSFTP = delArcUploadSFTP;
			this.triggerScan=triggerScan;
			this.failBuild = failBuild;
			this.newVulnerabilityCritical = newVulnerabilityCritical;
			this.newVulnerabilityHigh = newVulnerabilityHigh;
			this.newVulnerabilityMedium = newVulnerabilityMedium;
			this.newVulnerabilityLow = newVulnerabilityLow ;
			this.openVulnerabilityCritical = openVulnerabilityCritical;
			this.openVulnerabilityHigh = openVulnerabilityHigh;
			this.openVulnerabilityMedium = openVulnerabilityMedium;
			this.openVulnerabilityLow = openVulnerabilityLow;
			this.vulnerableFailBuildSelected = vulnerableFailBuildSelected;	
			this.scanningTimeOutSelected = scanningTimeOutSelected;
			this.newVulnerabilityNote =newVulnerabilityNote;
			this.newVulnerabilityTotal =newVulnerabilityTotal;
			this.openVulnerabilityNote = openVulnerabilityNote;
			this.openVulnerabilityTotal = openVulnerabilityTotal;
			this.allVulnerabilityCritical = allVulnerabilityCritical;
			this.allVulnerabilityHigh = allVulnerabilityHigh;
			this.allVulnerabilityMedium = allVulnerabilityMedium;
			this.allVulnerabilityLow = allVulnerabilityLow;
			this.allVulnerabilityNote = allVulnerabilityNote;
			this.allVulnerabilityTotal = allVulnerabilityTotal;
			
			this.scanWaitTimeMinutes = scanWaitTimeMinutes;
			PrintStream logger = listener.getLogger();
		}
		public String getAntValue() {

			StringBuilder antContent = new StringBuilder();

			antContent
					.append("<project basedir=\".\" default=\"makeZip\" name=\"Workspace Zip\">\n");
			antContent
					.append("\t<target description=\"Create a zip for the workspace\" name=\"makeZip\">\n");
			antContent.append("\t\t<tstamp>\n");
			antContent
					.append("\t\t<format property=\"BUILD_TIME_STAMP\" pattern=\"yyyyMMddHHmm\"/>\n");
			antContent.append("\t\t</tstamp>\n");
			antContent
					.append("\t\t<property name=\"basefolderName\" value=\"${BuildNo}_${BUILD_TIME_STAMP}\"/>\n");
			antContent
					.append("\t\t<delete file=\"${workspacePath}/${zipFilePath}.tar.gz\"/>\n");
			antContent.append("\t\t<tar destfile=\"${zipFilePath}.tar\" >\n");
			antContent
					.append("\t\t<tarfileset dir=\"${workspacePath}\" prefix=\"/${basefolderName}\" preserveLeadingSlashes=\"false\" excludes=\"${excludeFile}\" includes=\"${includeFile}\">\n");
			antContent.append("\t\t</tarfileset>\n");
			antContent.append("\t\t</tar>\n");
			antContent
					.append("\t\t<gzip destfile=\"${zipFilePath}.tar.gz\" src=\"${zipFilePath}.tar\"/>\n");
			antContent
					.append("\t\t<delete file=\"${zipFilePath}.tar\" quiet=\"true\"/>\n");
			antContent
					.append("\t\t<echo> Zip FilePath:  ${zipFilePath}.tar.gz</echo>\n");
			antContent
					.append("\t\t<move file=\"${zipFilePath}.tar.gz\" todir=\"${workspacePath}\"/>\n");
			antContent.append("\t</target>\n");
			antContent.append("</project>\n");

			return antContent.toString();

		}
		

		public Properties call() {
			
			Properties pp = System.getProperties();
			PrintStream logger = locallistener.getLogger();
			try {
				boolean success = perform(localenv, locallistener, ant);
				pp.put("result", success);
			} catch (Exception e) {
				pp.put("Exception", "yes" + e.getMessage());
				pp.put("result",false);
				AppConstants.logger(logger,"Exception "
						+ e.getMessage());
			}
  
			return pp;
		}

		private static final long serialVersionUID = 1L;

		public boolean perform(EnvVars environment, TaskListener listener,
				String ant) throws InterruptedException {
			PrintStream logger = locallistener.getLogger();
			AppConstants.DOMAIN = server;
			boolean isSuccess = true;
			Map<String, String> param = new HashMap<String, String>();
			if (true) { // only execute this step if the
					
				AppConstants.logger(logger, environment.get("WORKSPACE"));
				
				if(archiveSelected){
					try {
						workspacePath = environment.get("WORKSPACE");
						environment.get("DEST_PATH");
					} catch (Exception e) {
						AppConstants.logger(logger,e);
						return false;
					}
					String path = SentinelUtility.archieveSelected(param,environment,workspacePath,logger);
					String sourcePath = new String();
					String finalArchive = new String();
					if(fileTypeSelected.equalsIgnoreCase("ARCHIVE")){
						try {
							finalArchive = SentinelUtility.fileTypeArchieveSelected(environment,archiveName,fileName,excludeFileType,
									includeFileType ,path,param,workspacePath ,sourcePath,ant,uploadFileSize,logger); 
						}catch(Exception e) {
							AppConstants.logger(logger,"Exception "
									+ e.getMessage());
						}
					}else if(fileTypeSelected.equalsIgnoreCase("BINARY")){
						try {
							finalArchive = SentinelUtility.fileTypeBinarySelected(param,sourcePath,binaryName,path,workspacePath,logger);
						}catch(Exception e) {
							AppConstants.logger(logger,"Exception "
									+ e.getMessage());
						}	
					}
					param.put("appID", app);
					param.put("applianceIP", applianceIP);
					File f = new File(finalArchive);
					if(!f.exists() || f.isDirectory()) { 
						AppConstants.logger(logger,param.get("archiveName") +  " does not exists");
						return false;
					}
						if (!RestrictFileSize.checkSize(
								Double.parseDouble(uploadFileSize), finalArchive)) {
							int codebaseID = Integer.parseInt(codebase);
							String myApiKey = "";
							String mySentinelURL = "";
							if(!uselocalCredentials){
								myApiKey = globalApiKey;
								mySentinelURL = globalSentinelURL;
							}else {
								myApiKey = apiKey;
								mySentinelURL = sentinelURL;
							}
							param.put("archiveMethodSelected", archiveMethodSelected);
							if (archiveMethodSelected != null && archiveMethodSelected.equalsIgnoreCase("SFTP")) {
								boolean isFTPSuccess = SentinelUtility.archieveMethodSFTPSelected(param,sFtpURL,sFtpUID,sFtpPass,sFtpFolderPath,logger);
								if(!isFTPSuccess) {
									return false;
								}
							}
							if (archiveMethodSelected != null && archiveMethodSelected.equalsIgnoreCase("JENKINS")) {
								boolean jenkinArchiveSelectedFlag = SentinelUtility.jenkinArchiveSelected(jenkinsHosts,environment,param,logger );
								if (!jenkinArchiveSelectedFlag) {
									return false;
								}
							}
							if (archiveMethodSelected != null && archiveMethodSelected.equalsIgnoreCase("APPLIANCE")) {
								boolean archieveApplianceSelectedFlag = SentinelUtility.archieveMethodAppliaceSelected( param,myApiKey,mySentinelURL,logger);
								if(!archieveApplianceSelectedFlag) {
									return false;
								}
							}
							if(codebaseID != 0) {
								if (myApiKey != null && (!myApiKey.isEmpty())) {
									HttpURLConnection response = null; 
									
										boolean statusFlag  = true;
										if (app != null) {  
											try {
												statusFlag = SentinelUtility.waitIfAssetScanStatusRunning(myApiKey, mySentinelURL, Integer.parseInt(app),true,false, false,null,0,false,vulnerableFailBuildSelected,logger);
											}catch(Exception ex) {
												AppConstants.logger(logger,ex);
												isSuccess = false;
											}
										}
											
										if (statusFlag == false) {
											return false;
										}
									
								AppConstants.logger(logger,"Codebase is selected to be updated");	  
								response = SentinelUtility.updateCodebase(codebase, app, myApiKey, mySentinelURL, param,logger);
								try {
									if (response == null || response.getResponseCode() > 301) {
										AppConstants.logger(logger,"Update to codebase failed - "+ response.getResponseMessage());
										return false;
									}
									} catch (IOException e) {
										AppConstants.logger(logger,e.getMessage());
										isSuccess = false;
									}
								}
								else {
									AppConstants.logger(logger,"No API Key found - Won't updated any codebase in Sentinel");
								}
							}
							else {
								AppConstants.logger(logger,"No codebase was selected to be updated");
							}
							
							
							if((delArcUploadAppliance || delArcUploadSFTP) && (archiveMethodSelected != null && !archiveMethodSelected.equalsIgnoreCase("JENKINS"))){
								GeneralUtility.deleteFile(finalArchive, logger);
							}
						} else {
							if (finalArchive != null && uploadFileSize != null) {
								AppConstants.logger(logger,"Upload aborted - File Size of "
												+ finalArchive
												+ " is greater then the upload limit of "
												+ uploadFileSize + " MB");
								isSuccess = false;
							}
						}
				}
				long startTime = 0;
				if(triggerScan) {
					int appID = 0;
					if(app != null && !app.isEmpty()){
							appID = Integer.parseInt(app);
					}
					int siteID = 0;
					if(site != null && !site.isEmpty()){
						siteID = Integer.parseInt(site);
					}
					
					if(appID == 0 && siteID == 0) {
						AppConstants.logger(logger,"No asset was selected to trigger a WhiteHat scan");
						return false;
					}
					String myApiKey = "";
					String mySentinelURL = "";
					if(!uselocalCredentials){
						myApiKey = globalApiKey;
						mySentinelURL = globalSentinelURL;
					}
					else {
						myApiKey = apiKey;
						mySentinelURL = sentinelURL;
					}
					if (myApiKey != null && (!myApiKey.isEmpty())) {
						HttpURLConnection response;  
						if(appID != 0) {
							AppConstants.logger(logger,"Trigger scan was selected for App ID " + app);
							try {    
								boolean statusFlag = false;
								try {
									statusFlag = SentinelUtility.waitIfAssetScanStatusRunning(myApiKey, mySentinelURL, appID,false,false, false, null, 0,true,vulnerableFailBuildSelected,logger);
								}catch (Exception e) {
									AppConstants.logger(logger,e);
									
								}
								if (statusFlag == false) {
									return false;
								}
							} catch (Exception ex) {       
								AppConstants.logger(logger,ex.getMessage());
								return false;
								
							} 
							AppConstants.logger(logger,"Triggering scan for App ID " + app);
							response = SentinelUtility.setScanNow(appID, "",myApiKey, mySentinelURL,"app",logger);
							try {
								if (response == null || response.getResponseCode() > 301) {
									AppConstants.logger(logger,"Scan now failed for application");
									return false;
								}
							} catch (IOException e) {
								AppConstants.logger(logger,"Scan now failed for application - " + e.getMessage());
								return false;
							}
							startTime = System.currentTimeMillis();
							Thread.sleep(60000);  
						}
						if(siteID != 0) {
							isSuccess = SentinelUtility.siteScanSelected(siteID,myApiKey,mySentinelURL,logger);
						}
					}
					else {
						AppConstants.logger(logger,"Trigger scan was selected, but no API Key provided.");
						return false;
					}
					if (vulnerableFailBuildSelected) {  
						try {
							boolean statusFlag =SentinelUtility.waitIfAssetScanStatusRunning (myApiKey, mySentinelURL, appID,false,true, scanningTimeOutSelected, scanWaitTimeMinutes, startTime,false,vulnerableFailBuildSelected,logger);		
							if(statusFlag == false) {
								return false;
							}
							
							boolean failBuildFlag = checkVulnerabilitiesCondition(myApiKey, mySentinelURL, appID,logger);
							boolean scanTimeoutExceed= false;
							if(scanWaitTimeMinutes != null && scanWaitTimeMinutes.trim().length()>0 && SentinelUtility.isInteger(scanWaitTimeMinutes.trim())) {
									scanTimeoutExceed = SentinelUtility.checkScantimeoutExceed(scanningTimeOutSelected, scanWaitTimeMinutes, startTime,vulnerableFailBuildSelected,logger);
							}
							if (failBuildFlag || scanTimeoutExceed) {
								AppConstants.logger(logger,"Build fail.");
								return false;
							}
						} catch (Exception ex) {    
							AppConstants.logger(logger,ex.getMessage());
							isSuccess = false;
							
						}  
					}  
				}
				return isSuccess;
			} else {
				AppConstants.logger(logger,"There was failure in previous build step");
				return false;
			}
		}
		
		
		
		public  boolean checkVulnerabilitiesCondition(String myApiKey, String mySentinelURL, int appID,PrintStream logger ) {
			Map<String, Integer> vulnerabilityDataMap=new HashMap<String, Integer>();
			boolean failBuildFlag  = false;
			try {
				vulnerabilityDataMap =  SentinelUtility.getVulnerabilitiesCount(myApiKey, mySentinelURL,appID,logger);
				
				//Total Open & New Vulnerabilities Check
				if ((openVulnerabilityTotal!= null && openVulnerabilityTotal.trim().length() > 0 && SentinelUtility.isInteger(openVulnerabilityTotal.trim()) &&  Integer.parseInt(openVulnerabilityTotal) < vulnerabilityDataMap.get("totalOpenVulnerability")) ||
				(newVulnerabilityTotal!= null && newVulnerabilityTotal.trim().length() > 0 && SentinelUtility.isInteger(newVulnerabilityTotal.trim()) && Integer.parseInt(newVulnerabilityTotal) < vulnerabilityDataMap.get("totalNewVulnerability"))){
					failBuildFlag = true;
				}
				
				//All Vulnerabilities Check
				if((allVulnerabilityCritical!= null && allVulnerabilityCritical.trim().length() > 0 && SentinelUtility.isInteger(allVulnerabilityCritical.trim()) && Integer.parseInt(allVulnerabilityCritical) < (vulnerabilityDataMap.get("newCriticalVulnerability")+ vulnerabilityDataMap.get("openCriticalVulnerability"))) ||
					(allVulnerabilityHigh!= null && allVulnerabilityHigh.trim().length() > 0 && SentinelUtility.isInteger(allVulnerabilityHigh.trim()) && Integer.parseInt(allVulnerabilityHigh) < (vulnerabilityDataMap.get("openHighVulnerability")+ vulnerabilityDataMap.get("newHighVulnerability")))||
					(allVulnerabilityMedium!= null && allVulnerabilityMedium.trim().length() > 0 && SentinelUtility.isInteger(allVulnerabilityMedium.trim()) && Integer.parseInt(allVulnerabilityMedium) < (vulnerabilityDataMap.get("openMediumVulnerability")+ vulnerabilityDataMap.get("newMediumVulnerability"))) ||
					(allVulnerabilityLow!= null && allVulnerabilityLow.trim().length() > 0 && SentinelUtility.isInteger(allVulnerabilityLow.trim()) && Integer.parseInt(allVulnerabilityLow) < (vulnerabilityDataMap.get("openLowVulnerability")+ vulnerabilityDataMap.get("newLowVulnerability"))) ||
					(allVulnerabilityNote!= null && allVulnerabilityNote.trim().length() > 0 && SentinelUtility.isInteger(allVulnerabilityNote.trim()) && Integer.parseInt(allVulnerabilityNote) < (vulnerabilityDataMap.get("openNoteVulnerability")+ vulnerabilityDataMap.get("newNoteVulnerability"))) ||
					(allVulnerabilityTotal!= null && allVulnerabilityTotal.trim().length() > 0 && SentinelUtility.isInteger(allVulnerabilityTotal.trim()) && Integer.parseInt(allVulnerabilityTotal) < (vulnerabilityDataMap.get("totalNewVulnerability")+ vulnerabilityDataMap.get("totalOpenVulnerability"))) ){
						failBuildFlag = true;
				}
				
				//Discovered Vulnerabilities Check
				if((newVulnerabilityCritical!= null && newVulnerabilityCritical.trim().length() > 0 && SentinelUtility.isInteger(newVulnerabilityCritical.trim()) && Integer.parseInt(newVulnerabilityCritical) < vulnerabilityDataMap.get("newCriticalVulnerability")) ||
					(newVulnerabilityHigh != null && newVulnerabilityHigh.trim().length() > 0 && SentinelUtility.isInteger(newVulnerabilityHigh.trim()) && Integer.parseInt(newVulnerabilityHigh) < vulnerabilityDataMap.get("newHighVulnerability")) ||
					(newVulnerabilityMedium != null && newVulnerabilityMedium.trim().length() >0 && SentinelUtility.isInteger(newVulnerabilityMedium.trim()) && Integer.parseInt(newVulnerabilityMedium) < vulnerabilityDataMap.get("newMediumVulnerability")) ||
					(newVulnerabilityLow != null && newVulnerabilityLow.trim().length() > 0 && SentinelUtility.isInteger(newVulnerabilityLow.trim()) && Integer.parseInt(newVulnerabilityLow) < vulnerabilityDataMap.get("newLowVulnerability")) ||
					(newVulnerabilityNote != null && newVulnerabilityNote.trim().length() > 0 && SentinelUtility.isInteger(newVulnerabilityNote.trim()) && Integer.parseInt(newVulnerabilityNote) < vulnerabilityDataMap.get("newNoteVulnerability"))){
						failBuildFlag = true;
				}
				
				// Open Vulnerabilities Check
				if((openVulnerabilityCritical != null && openVulnerabilityCritical.trim().length()> 0 && SentinelUtility.isInteger(openVulnerabilityCritical.trim()) && Integer.parseInt(openVulnerabilityCritical) < vulnerabilityDataMap.get("openCriticalVulnerability")) ||
					(openVulnerabilityHigh != null && openVulnerabilityHigh.trim().length()> 0 && SentinelUtility.isInteger(openVulnerabilityHigh.trim()) && Integer.parseInt(openVulnerabilityHigh) < vulnerabilityDataMap.get("openHighVulnerability")) ||
					(openVulnerabilityMedium != null && openVulnerabilityMedium.trim().length()>0 && SentinelUtility.isInteger(openVulnerabilityMedium.trim()) && Integer.parseInt(openVulnerabilityMedium) < vulnerabilityDataMap.get("openMediumVulnerability")) ||
					(openVulnerabilityLow != null && openVulnerabilityLow.trim().length()>0 && SentinelUtility.isInteger(openVulnerabilityLow.trim()) && Integer.parseInt(openVulnerabilityLow) < vulnerabilityDataMap.get("openLowVulnerability")) ||
					(openVulnerabilityNote != null && openVulnerabilityNote.trim().length() > 0 && SentinelUtility.isInteger(openVulnerabilityNote.trim()) && Integer.parseInt(openVulnerabilityNote) < vulnerabilityDataMap.get("openNoteVulnerability"))){
						failBuildFlag = true;
				} 
				//Display Total Vulnerability  
				AppConstants.logger(logger,"Configured "+(openVulnerabilityTotal == null || openVulnerabilityTotal.trim().length()== 0 || !SentinelUtility.isInteger(openVulnerabilityTotal.trim()) ?" - ":openVulnerabilityTotal)
						+" total open vulnerabilities, found "+vulnerabilityDataMap.get("totalOpenVulnerability"));
				AppConstants.logger(logger,"Configured "+ (newVulnerabilityTotal == null || newVulnerabilityTotal.trim().length()== 0 || !SentinelUtility.isInteger(newVulnerabilityTotal.trim()) ?" - ":newVulnerabilityTotal)
						+" total pending verification vulnerabilities, found "+vulnerabilityDataMap.get("totalNewVulnerability"));
				
				//Display All Vulnerability  
				AppConstants.logger(logger,"Configured "+(allVulnerabilityCritical == null || allVulnerabilityCritical.trim().length()== 0 || !SentinelUtility.isInteger(allVulnerabilityCritical.trim()) ?" - ":allVulnerabilityCritical)
						+" critical vulnerabilities, found "+ (vulnerabilityDataMap.get("newCriticalVulnerability")+ vulnerabilityDataMap.get("openCriticalVulnerability")));
				AppConstants.logger(logger,"Configured "+(allVulnerabilityHigh == null || allVulnerabilityHigh.trim().length()== 0  || !SentinelUtility.isInteger(allVulnerabilityHigh.trim())?" - ":allVulnerabilityHigh)
						+" high vulnerabilities, found "+ (vulnerabilityDataMap.get("newHighVulnerability")+ vulnerabilityDataMap.get("openHighVulnerability")));
				AppConstants.logger(logger,"Configured "+(allVulnerabilityMedium == null || allVulnerabilityMedium.trim().length()== 0 || !SentinelUtility.isInteger(allVulnerabilityMedium.trim()) ?" - ":allVulnerabilityMedium)
						+" medium vulnerabilities, found "+ (vulnerabilityDataMap.get("newMediumVulnerability")+ vulnerabilityDataMap.get("openMediumVulnerability")));
				AppConstants.logger(logger,"Configured "+(allVulnerabilityLow == null || allVulnerabilityLow.trim().length()== 0 || !SentinelUtility.isInteger(allVulnerabilityLow.trim()) ?" - ":allVulnerabilityLow)
						+" low vulnerabilities, found "+ (vulnerabilityDataMap.get("openLowVulnerability")+ vulnerabilityDataMap.get("newLowVulnerability")));
				AppConstants.logger(logger,"Configured "+(allVulnerabilityNote == null || allVulnerabilityNote.trim().length()== 0 || !SentinelUtility.isInteger(allVulnerabilityNote.trim()) ?" - ":allVulnerabilityNote)
						+" note vulnerabilities, found "+ (vulnerabilityDataMap.get("openNoteVulnerability")+ vulnerabilityDataMap.get("newNoteVulnerability")));
				AppConstants.logger(logger,"Configured "+(allVulnerabilityTotal == null || allVulnerabilityTotal.trim().length()== 0 || !SentinelUtility.isInteger(allVulnerabilityTotal.trim()) ?" - ":allVulnerabilityTotal)
						+" total vulnerabilities, found "+ (vulnerabilityDataMap.get("totalNewVulnerability")+ vulnerabilityDataMap.get("totalOpenVulnerability")));
				
				//Display new and unverified Vulnerability  
				AppConstants.logger(logger,"Configured "+ (newVulnerabilityCritical == null || newVulnerabilityCritical.trim().length()== 0 || !SentinelUtility.isInteger(newVulnerabilityCritical.trim()) ?" - ":newVulnerabilityCritical)
						+" critical pending verification vulnerabilities, found "+vulnerabilityDataMap.get("newCriticalVulnerability"));
				AppConstants.logger(logger,"Configured "+ (newVulnerabilityHigh == null || newVulnerabilityHigh.trim().length()== 0 || !SentinelUtility.isInteger(newVulnerabilityHigh.trim()) ?" - ":newVulnerabilityHigh)
						+" high pending verification vulnerabilities, found "+vulnerabilityDataMap.get("newHighVulnerability"));
				AppConstants.logger(logger,"Configured "+(newVulnerabilityMedium == null || newVulnerabilityMedium.trim().length()== 0 || !SentinelUtility.isInteger(newVulnerabilityMedium.trim()) ?" - ":newVulnerabilityMedium)
						+" medium pending verification vulnerabilities, found "+vulnerabilityDataMap.get("newMediumVulnerability"));
				AppConstants.logger(logger,"Configured "+(newVulnerabilityLow == null || newVulnerabilityLow.trim().length()== 0 || !SentinelUtility.isInteger(newVulnerabilityLow.trim()) ?" - ":newVulnerabilityLow)
						+" low pending verification vulnerabilities, found "+vulnerabilityDataMap.get("newLowVulnerability"));
				AppConstants.logger(logger,"Configured "+(newVulnerabilityNote == null || newVulnerabilityNote.trim().length()== 0 || !SentinelUtility.isInteger(newVulnerabilityNote.trim()) ?" - ":newVulnerabilityNote)
						+" note pending verification vulnerabilities, found "+vulnerabilityDataMap.get("newNoteVulnerability"));
				
				//Display open and verified Vulnerability  
				AppConstants.logger(logger,"Configured "+(openVulnerabilityCritical == null || openVulnerabilityCritical.trim().length()== 0 || !SentinelUtility.isInteger(openVulnerabilityCritical.trim()) ?" - ":openVulnerabilityCritical)
						+" critical open vulnerabilities, found "+vulnerabilityDataMap.get("openCriticalVulnerability"));
				AppConstants.logger(logger,"Configured "+(openVulnerabilityHigh == null || openVulnerabilityHigh.trim().length()== 0 || !SentinelUtility.isInteger(openVulnerabilityHigh.trim()) ?" - ":openVulnerabilityHigh)
						+" high open vulnerabilities, found "+vulnerabilityDataMap.get("openHighVulnerability"));
				AppConstants.logger(logger,"Configured "+(openVulnerabilityMedium == null || openVulnerabilityMedium.trim().length()== 0 || !SentinelUtility.isInteger(openVulnerabilityMedium.trim()) ?" - ":openVulnerabilityMedium)
						+" medium open vulnerabilities, found "+vulnerabilityDataMap.get("openMediumVulnerability"));
				AppConstants.logger(logger,"Configured "+(openVulnerabilityLow == null || openVulnerabilityLow.trim().length()== 0 || !SentinelUtility.isInteger(openVulnerabilityLow.trim())  ?" - ":openVulnerabilityLow)
						+ " low open vulnerabilities, found "+vulnerabilityDataMap.get("openLowVulnerability"));
				AppConstants.logger(logger,"Configured "+(openVulnerabilityNote == null || openVulnerabilityNote.trim().length()== 0 || !SentinelUtility.isInteger(openVulnerabilityNote.trim()) ?" - ":openVulnerabilityNote)
						+ " note open vulnerabilities, found "+vulnerabilityDataMap.get("openNoteVulnerability"));
			}catch(Exception ex) {
				AppConstants.logger(logger,ex);
			} 
			return failBuildFlag;
		}
		@Override
		public void checkRoles(RoleChecker arg0) throws SecurityException {
			// TODO Auto-generated method stub
			
		}
	}
	public boolean isTriggerScan() {
		return triggerScan;
	}
	  
	public boolean isFailBuild(){
		return failBuild;
	}

	public void setTriggerScan(boolean triggerScan) {
		this.triggerScan = triggerScan;
	}
	
	public void setFailBuild(boolean failBuild){
		this.failBuild = failBuild;
	}
	
	public void setArchiveMethodSelected(String archiveMethodSelected) {
		this.archiveMethodSelected = archiveMethodSelected;
	}
	
	public void setFileTypeSelected(String fileTypeSelected){
		this.fileTypeSelected = fileTypeSelected;
	}

	public String getServer() {
		return server;
	}

	public String getApp() {
		return app;
	}
	
	public String getSite(){
		return site;
	}
	
	public String getCodebase() {
		return codebase;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getSentinelURL(){
		return sentinelURL;
	}
	
	public String getArchiveName() {
		return archiveName;
	}
	
	public String getBinaryName() {
		return binaryName;
	}
	
	public String JenkinsHosts(){
		return jenkinsHosts;
	}
	
	public String getFileName() {
		return fileName;
	}

	public String getApplianceIP() {
		return applianceIP;
	}

	public String getsFtpURL() {
		return sFtpURL;
	}

	public String getsFtpUID() {
		return sFtpUID;
	}

	public String getsFtpPass() {
		return sFtpPass;
	}

	public String getsFtpFolderPath() {
		return sFtpFolderPath;
	}
	
	public String getJenkinsHosts(){
		return jenkinsHosts;
	}

	public String getAnt() {
		return ant;
	}

	public String getDestPath() {
		return destPath;
	}

	public String getArchiveMethodSelected() {
		return archiveMethodSelected;
	}
	
	public String getFileTypeSelected(){
		return fileTypeSelected;
	}
	
	public boolean isUselocalCredentials(){
		return uselocalCredentials;
	}
	
	public boolean isArchiveSelected(){
		return archiveSelected;
	}

	public String isArchiveMethodSelected(String archiveMethodSelectedName){
		if(archiveMethodSelected == null){
			return "";
		}
		return this.archiveMethodSelected.equalsIgnoreCase(archiveMethodSelectedName) ? "true" : "";
	}
	
	public String isFileTypeSelected(String fileTypeSelectedName){
		if(fileTypeSelected == null) {
			return "";
		}
		return this.fileTypeSelected.equalsIgnoreCase(fileTypeSelectedName) ? "true" : "";
	}

	public String getWorkspacePath() {
		return workspacePath;
	}

	public String getExcludeFileType() {
		return excludeFileType;
	}

	public String getIncludeFileType() {
		return includeFileType;
	}

	public String getUploadFileSize() {
		return uploadFileSize;
	}

	public String getClientId() {
		return clientId;
	}


	public String getSatellitePort() {
		return satellitePort;
	}

	public void setAnt(String ant) {
		this.ant = ant;
	}
	
	public void setApp(String app) {
		this.app = app;
	}
	
	public void setSite(String site){
		this.site = site;
	}
	public void setCodebase(String codebase){
		this.codebase = codebase;
	}
	
	public void setDestPath(String destPath) {
		this.destPath = destPath;
	}
	
	public void setUselocalCredentials(boolean uselocalCredentials){
		this.uselocalCredentials = uselocalCredentials;
	}
	
	public void setArchiveSelected(boolean archiveSelected){
		this.archiveSelected = archiveSelected;
	}

	public void setWorkspacePath(String workspacePath) {
		this.workspacePath = workspacePath;
	}

	public void setExcludeFileType(String excludeFileType) {
		this.excludeFileType = excludeFileType;
	}

	public void setIncludeFileType(String includeFileType) {
		this.includeFileType = includeFileType;
	}

	public void setUploadFileSize(String uploadFileSize) {
		this.uploadFileSize = uploadFileSize;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public void setSatellitePort(String satellitePort) {
		this.satellitePort = satellitePort;
	}
	
	public boolean isDelArcUploadAppliance() {
		return delArcUploadAppliance;
	}
	
	public boolean isDelArcUploadSFTP(){
		return delArcUploadSFTP;
	}

	public void setDelArcUploadAppliance(boolean delArcUploadAppliance) {
		this.delArcUploadAppliance = delArcUploadAppliance;
	}

	public void setDelArcUploadSFTP(boolean delArcUploadSFTP) {
		this.delArcUploadSFTP = delArcUploadSFTP;
	}
	public String getNewVulnerabilityCritical() {
		return newVulnerabilityCritical;
	}

	public void setNewVulnerabilityCritical(String newVulnerabilityCritical) {
		this.newVulnerabilityCritical = newVulnerabilityCritical;
	}

	public String getNewVulnerabilityHigh() {
		return newVulnerabilityHigh;
	}

	public void setNewVulnerabilityHigh(String newVulnerabilityHigh) {
		this.newVulnerabilityHigh = newVulnerabilityHigh;
	}

	public String getNewVulnerabilityMedium() {
		return newVulnerabilityMedium;
	}

	public void setNewVulnerabilityMedium(String newVulnerabilityMedium) {
		this.newVulnerabilityMedium = newVulnerabilityMedium;
	}

	public String getNewVulnerabilityLow() {
		return newVulnerabilityLow;
	}

	public void setNewVulnerabilityLow(String newVulnerabilityLow) {
		this.newVulnerabilityLow = newVulnerabilityLow;
	}

	public String getOpenVulnerabilityCritical() {
		return openVulnerabilityCritical;
	}

	public void setOpenlVulnerabilityCritical(String openVulnerabilityCritical) {
		this.openVulnerabilityCritical = openVulnerabilityCritical;
	}

	public String getOpenVulnerabilityHigh() {
		return openVulnerabilityHigh;
	}

	public void setOpenVulnerabilityHigh(String openVulnerabilityHigh) {
		this.openVulnerabilityHigh = openVulnerabilityHigh;
	}

	public String getOpenVulnerabilityMedium() {
		return openVulnerabilityMedium;
	}

	public void setOpenVulnerabilityMedium(String openVulnerabilityMedium) {
		this.openVulnerabilityMedium = openVulnerabilityMedium;
	}

	public String getOpenVulnerabilityLow() {
		return openVulnerabilityLow;
	}
	public void setOpenVulnerabilityLow(String openVulnerabilityLow) {
		this.openVulnerabilityLow = openVulnerabilityLow;
	}

	public boolean isVulnerableFailBuildSelected() {
		return vulnerableFailBuildSelected;
	}

	public void setVulnerableFailBuildSelected(boolean vulnerableFailBuildSelected) {
		this.vulnerableFailBuildSelected = vulnerableFailBuildSelected;
	}



	public String getNewVulnerabilityNote() {
		return newVulnerabilityNote;
	}


	public void setNewVulnerabilityNote(String newVulnerabilityNote) {
		this.newVulnerabilityNote = newVulnerabilityNote;
	}


	public String getNewVulnerabilityTotal() {
		return newVulnerabilityTotal;
	}


	public void setNewVulnerabilityTotal(String newVulnerabilityTotal) {
		this.newVulnerabilityTotal = newVulnerabilityTotal;
	}


	public String getOpenVulnerabilityNote() {
		return openVulnerabilityNote;
	}


	public void setOpenVulnerabilityNote(String openVulnerabilityNote) {
		this.openVulnerabilityNote = openVulnerabilityNote;
	}


	public String getOpenVulnerabilityTotal() {
		return openVulnerabilityTotal;
	}


	public void setOpenVulnerabilityTotal(String openVulnerabilityTotal) {
		this.openVulnerabilityTotal = openVulnerabilityTotal;
	}


	public String getAllVulnerabilityCritical() {
		return allVulnerabilityCritical;
	}


	public void setAllVulnerabilityCritical(String allVulnerabilityCritical) {
		this.allVulnerabilityCritical = allVulnerabilityCritical;
	}


	public String getAllVulnerabilityHigh() {
		return allVulnerabilityHigh;
	}


	public void setAllVulnerabilityHigh(String allVulnerabilityHigh) {
		this.allVulnerabilityHigh = allVulnerabilityHigh;
	}


	public String getAllVulnerabilityMedium() {
		return allVulnerabilityMedium;
	}


	public void setAllVulnerabilityMedium(String allVulnerabilityMedium) {
		this.allVulnerabilityMedium = allVulnerabilityMedium;
	}


	public String getAllVulnerabilityLow() {
		return allVulnerabilityLow;
	}


	public void setAllVulnerabilityLow(String allVulnerabilityLow) {
		this.allVulnerabilityLow = allVulnerabilityLow;
	}


	public String getAllVulnerabilityNote() {
		return allVulnerabilityNote;
	}


	public void setAllVulnerabilityNote(String allVulnerabilityNote) {
		this.allVulnerabilityNote = allVulnerabilityNote;
	}


	public String getAllVulnerabilityTotal() {
		return allVulnerabilityTotal;
	}


	public void setAllVulnerabilityTotal(String allVulnerabilityTotal) {
		this.allVulnerabilityTotal = allVulnerabilityTotal;
	}

	public String getScanWaitTimeMinutes() {
		return scanWaitTimeMinutes;
	}


	public void setOpenVulnerabilityCritical(String openVulnerabilityCritical) {
		this.openVulnerabilityCritical = openVulnerabilityCritical;
	}


	public void setScanWaitTimeMinutes(String scanWaitTimeMinutes) {
		this.scanWaitTimeMinutes = scanWaitTimeMinutes;
	}


	public boolean isScanningTimeOutSelected() {
		return scanningTimeOutSelected;
	}


	public void setScanningTimeOutSelected(boolean scanningTimeOutSelected) {
		this.scanningTimeOutSelected = scanningTimeOutSelected;
	}

	@Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		
		PrintStream logger = listener.getLogger();
		
		AppConstants.logger(logger,"WhiteHat Sentinel Plugin V 2.3.3");
		EnvVars environment = run.getEnvironment(listener);
		environment.put("WORKSPACE", workspace.toString());
		
	
		
		for (Entry<String, String> entry : environment.entrySet()) {
			System.out.println("Key: " + entry.getKey() + ". Value: " + entry.getValue());
		}
		VirtualChannel v = launcher.getChannel();
		if (Computer.currentComputer() instanceof SlaveComputer) {
			AppConstants.logger(logger, "Plugin Executing on Slave Host");
		} else {
			AppConstants.logger(logger, "Plugin Executing on Master Host");
		}
		
		Secret secretApiKey = Secret.fromString(globalApiKey);
		String globalApiKeyDecoded = Secret.toString(secretApiKey);
		secretApiKey = Secret.fromString(apiKey);
		String localApiKeyDecoded = Secret.toString(secretApiKey);
		Properties systemProperties = v.call(new GetSystemProperties(
				environment, listener, ant, run.getResult(),serverSelected, uselocalCredentials, localApiKeyDecoded,sentinelURL,
				globalApiKeyDecoded,globalSentinelURL,archiveSelected,
				site,app,codebase, archiveMethodSelected, fileTypeSelected,applianceIP,
				archiveName,binaryName, ant, destPath,
				workspacePath,server, sFtpURL, sFtpUID, sFtpPass,
				sFtpFolderPath, fileName,jenkinsHosts,
				excludeFileType, includeFileType, uploadFileSize, clientId,
				delArcUploadAppliance,delArcUploadSFTP,triggerScan,failBuild,
				newVulnerabilityCritical,newVulnerabilityHigh,newVulnerabilityMedium,newVulnerabilityLow,newVulnerabilityNote,newVulnerabilityTotal,
				openVulnerabilityCritical,openVulnerabilityHigh,openVulnerabilityMedium,openVulnerabilityLow,openVulnerabilityNote,openVulnerabilityTotal,
				allVulnerabilityCritical,allVulnerabilityHigh,allVulnerabilityMedium,allVulnerabilityLow,allVulnerabilityNote,allVulnerabilityTotal,
				scanWaitTimeMinutes,vulnerableFailBuildSelected,scanningTimeOutSelected
				));

		boolean result = Boolean.parseBoolean(systemProperties
				.get("result").toString());

		if(!result){
			run.setResult(Result.FAILURE);
		}
		
	    
	}
	
}
