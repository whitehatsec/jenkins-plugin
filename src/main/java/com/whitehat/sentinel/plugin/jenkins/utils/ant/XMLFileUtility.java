package com.whitehat.sentinel.plugin.jenkins.utils.ant;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.whitehat.sentinel.plugin.jenkins.shared.AppConstants;

public class XMLFileUtility {
	private XMLFileUtility() {
	}

	public static void createNewElement(Document doc, Node node,
			Map<String, String> attr) {
		Element property = doc.createElement("property");
		for (Map.Entry<String, String> entry : attr.entrySet()) {
			property.setAttribute(entry.getKey(), entry.getValue());
		}
		node.appendChild(property);
	}

	public static void write(String filePath, String content,
			Map<String, String> param,PrintStream logger) {

		try {

			AppConstants.logger(logger,"Started XML writing ");

			DocumentBuilderFactory newDocFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder newDocBuilder = newDocFactory.newDocumentBuilder();

			Document newDoc = newDocBuilder.parse(new InputSource(
					new ByteArrayInputStream(content.getBytes())));
			
			Node newNode = newDoc.getDocumentElement();
			Map<String, String> attr = new HashMap<String, String>();
			attr.put("name", "ArchiveName");
			attr.put("value", param.get("archiveName").replace(".tar.gz", ""));
			createNewElement(newDoc, newNode, attr);

			attr = new HashMap<String, String>();
			attr.put("name", "JobPath");
			attr.put("value", param.get("workspacePath"));
			createNewElement(newDoc, newNode, attr);

			attr = new HashMap<String, String>();
			attr.put("name", "workspacePath");
			attr.put("value", param.get("workspacePath"));
			createNewElement(newDoc, newNode, attr);

			attr = new HashMap<String, String>();
			attr.put("name", "zipFilePath");
			attr.put("value", "${ArchiveName}");
			createNewElement(newDoc, newNode, attr);
			
			attr = new HashMap<String, String>();
			attr.put("name", "excludeFile");
			attr.put("value", param.get("excludeFile"));
			createNewElement(newDoc, newNode, attr);
			
			attr = new HashMap<String, String>();
			attr.put("name", "includeFile");
			attr.put("value", param.get("includeFile"));
			createNewElement(newDoc, newNode, attr);
			
			attr = new HashMap<String, String>();
			attr.put("name", "BuildNo");
			attr.put("value", param.get("BuildNo"));
			createNewElement(newDoc, newNode, attr);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();

			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			// transformer.setOutputProperty("http://xml.apache.org/xsltd;indent-amount",
			// "4");
			// transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
			// "yes");
			DOMSource source = new DOMSource(newDoc);
			StreamResult result = new StreamResult(new File(filePath));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);

			AppConstants.logger(logger,"Ant modification done ");

		} catch (ParserConfigurationException pce) {
			AppConstants.logger(logger,pce);
		} catch (TransformerException tfe) {
			AppConstants.logger(logger,tfe);
		} catch (Exception tfe) {
			AppConstants.logger(logger,tfe);
		}
	}

}
