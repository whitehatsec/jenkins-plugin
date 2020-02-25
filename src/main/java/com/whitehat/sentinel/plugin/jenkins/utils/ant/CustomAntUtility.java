package com.whitehat.sentinel.plugin.jenkins.utils.ant;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import com.whitehat.sentinel.plugin.jenkins.shared.AppConstants;

/**
 * @author srccodes.com
 * @version 1.0
 */
public class CustomAntUtility {
    /**
     * To execute the default target specified in the Ant build.xml file
     * 
     * @param buildXmlFileFullPath
     */
    public static boolean executeAntTask(String buildXmlFileFullPath,Map<String,String> param,PrintStream logger) {
        return executeAntTask(buildXmlFileFullPath, null,param,logger);
    }
    
    /**
     * To execute a target specified in the Ant build.xml file
     * 
     * @param buildXmlFileFullPath
     * @param target
     */
    public static boolean executeAntTask(String buildXmlFileFullPath, String target,Map<String,String> param,PrintStream logger) {
        boolean success = false;
        DefaultLogger consoleLogger = getConsoleLogger();

        // Prepare Ant project
        Project project = new Project();
        File buildFile = new File(buildXmlFileFullPath);
        project.setUserProperty("ant.file", buildFile.getAbsolutePath());
        
        for (Map.Entry<String, String> entry : param.entrySet()) {
        	project.setNewProperty(entry.getKey(),entry.getValue());
        	
    	}
        
        project.addBuildListener(consoleLogger);

        // Capture event for Ant script build start / stop / failure
        try {
        	
            project.fireBuildStarted();
            project.init();
            ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
            project.addReference("ant.projectHelper", projectHelper);
            projectHelper.parse(project, buildFile);
            
            // If no target specified then default target will be executed.
            String targetToExecute = (target != null && target.trim().length() > 0) ? target.trim() : project.getDefaultTarget();
            project.executeTarget(targetToExecute);
            project.fireBuildFinished(null);
            success = true;
            AppConstants.logger(logger,"Ant execution completed ");
        } catch (BuildException buildException) {
            project.fireBuildFinished(buildException);
            throw new RuntimeException(" while parsing ant file errors occured ", buildException);
        }
        
        return success;
    }
    
    /**
     * Logger to log output generated while executing ant script in console
     * 
     * @return
     */
    private static DefaultLogger getConsoleLogger() {
        DefaultLogger consoleLogger = new DefaultLogger();
        consoleLogger.setErrorPrintStream(System.err);
        consoleLogger.setOutputPrintStream(System.out);
        consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
        
        return consoleLogger;
    }
    
    /**
     * Main method to test code
     * 
     * @param args
     */
	public static void invokeAnt(String filePath,Map<String,String> param,PrintStream logger) {
	
		// Running default target of ant script
		executeAntTask(filePath,param,logger);
	}
	  


}
