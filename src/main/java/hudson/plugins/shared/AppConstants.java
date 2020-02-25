package hudson.plugins.shared;

import hudson.plugins.utils.GeneralUtility;

import java.io.File;
import java.io.PrintStream;
import java.lang.Exception;

public class AppConstants {

	public static String OS_SPECFIC_SLASH ="/";//File.separator; // OSValidatorUtil.isWindows() ?
											// "/":
	
	public static String DOMAIN = "";// ResourceFileUtil.getValue("domain");
	public static String HOST_NAME = "";
	public static String SENTINEL_US = "https://sentinel.whitehatsec.com";
	public static String SENTINEL_EU = "https://sentinel.whitehatsec.eu";
	
	public static String JSON_FORMAT = "?display_steps=1&format=json";
	public static String API_METRICS = "source={\"source_name\":\"Jenkins\",\"plugin_version\":\"2.3.3\",\"source_version\":\"2.3.3\"}";
	public static String APP_ORDER_BY = "page:order_by=label_asc&";
	public static String SITE_ORDER_BY = "order_by=label:asc&";
	//public static PrintStream logger = null;
	public static String SENTINEL_USER_APPS;
	public static String APPLICATION_API_CONSTANT = "/api/application";
	public static String SITE_API_CONSTANT = "/api/site";
	public static String  FULL_STATISTICS ="full_statistics";
	
	public static String APPLIANCE_API_CONSTANT = "/api/appliance";
	public static String ONLY_SAST_APPLIANCE = "query_type=sast&";
	public static String GENERATE_TOKEN_API_CONSTANT = "/api/application/codebase/upload_token";
	public static String SCAN_NOW_APP = "{\"scan_schedule\":{\"specs\":[{\"type\":\"single\",\"duration\":null}],\"timezone\":\"America/Los_Angeles\",\"name\":\"scan_now\",\"start_date\":null}}";
	public static String SCAN_NOW_SITE = "{\"schedule\":{\"name\":\"Scan Once Now\",\"timezone\":\"America/Los_Angeles\",\"specs\":[{\"type\":\"scan-once-now\"}]}}";
	public static String CREATE_APPLICATION_POST_BODY = "{\"label\":\"%s\",\"language\":\"%s\",\"appliance\":{\"id\":%s}}";
	public static String CREATE_CODEBASE_POST_BODY = "{\"label\":\"%s\",\"repository_url\":\"New Path\",\"repository_type\":\"mock\",\"repository_revision\":\"HEAD\",\"exclude_dirs\":[],\"auth_type\":\"none\"}";
	public static String UPDATE_CODEBASE_SFTP = "{\"repository_url\":\"%s\",\"auth_type\":\"password\",\"username\":\"%s\",\"password\":\"%s\"}";
	public static String UPDATE_CODEBASE_HTTP = "{\"repository_url\":\"%s\",\"auth_type\":\"none\"}";
	public static String GENERATE_TOKEN_POST_BODY = "{\"sha_hex\":\"%s\"}";
	public static String UPLOAD_TO_APPLIANCE = "http://%s/api/upload/file";
	public static int FILE_UPLOAD_ERROR_CODE=100;
	
	public static String USER_API = "/api/user?";
	
	
	public static void logger(PrintStream logger,Exception msg){
		println(logger,msg);
	}
	public static void logger(PrintStream logger,String msg){
		println(logger,msg);
	}
	public static void logger(PrintStream logger,Object msg){
		println(logger,msg);
	}
	public static void println(PrintStream logger,Object obj){
		if(logger!=null){
			logger.println("WhiteHat: "+GeneralUtility.getCurrenTimeDtTime()+" "+obj);
		}
	}

}
