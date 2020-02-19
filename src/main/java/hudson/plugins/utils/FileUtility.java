package hudson.plugins.utils;

import hudson.plugins.shared.AppConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * This class is used to upload a file to a FTP server.
 * 
 * @author Sunil Patel
 */
public class FileUtility {
	private FileUtility() {
	}

	// public static void main(String[] args) {
	// Map<String,String> param=new HashMap<String,String>();
	//
	// param.put("server", "http://192.168.180.218/api/misc/uploadCodebase/");
	// param.put("fileName", "sources");
	// param.put("archiveName", "sources");
	// param.put("JobPath", "C:\\sunil\\sources");
	// upload(param);
	//
	// }

	public static void upload(Map<String, String> param) throws CustomException {
		// String user, String password) {
		// String urlToConnect = param.get("server");

		String fileSource = param.get("workspacePath") + File.separator
				+ param.get("archiveName");
		AppConstants.logger(null,"Started file uploading fileSource "
				+ fileSource);

		String contentType = "application/x-gzip";

		String urlToConnect = (param.get("server").indexOf("http:") != -1) ? param
				.get("server") : "http://" + param.get("server");

		String paramToSend = "file";

		// String urlToConnect = "http://localhost:8000";
		// File fileToUpload = new
		// File("/Users/sunilpatel/Desktop/Archive.zip");
		String fileName =param.get("archiveName");
		File fileToUpload = new File(fileSource);
		String boundary = Long.toHexString(System.currentTimeMillis());

		try {
			URLConnection connection = new URL(urlToConnect).openConnection();
			connection.setDoOutput(true); // This sets request method to POST.
			connection.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + boundary);
			connection.setRequestProperty("Referer", urlToConnect);
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(new OutputStreamWriter(
						connection.getOutputStream(), "UTF-8"));
				writer.println("--" + boundary);
				writer.println("Content-Disposition: form-data; name=\"file\"; filename=\""
						+ fileName + "\"");
				writer.println("Content-Type:" + contentType);
				writer.println();
				writer.println(paramToSend);

				// The name of the file to open.
				String file = fileToUpload.getPath().toString();
				AppConstants.logger(null,"Started HTTP File Upload" );
				try {
					// Use this for reading the data.
					byte[] buffer = new byte[1000];

					FileInputStream inputStream = new FileInputStream(file);

					// read fills buffer with data and returns
					// the number of bytes read (which of course
					// may be less than the buffer size, but
					// it will never be more).
					int total = 0;
					int nRead = 0;
					while ((nRead = inputStream.read(buffer)) != -1) {
						// Convert to String so we can display it.
						// Of course you wouldn't want to do this with
						// a 'real' binary file.
						writer.print(buffer);
						total += nRead;
					}

					// Always close files.
					inputStream.close();

					AppConstants.logger(null,"Read " + total + " bytes");
					writer.println("--" + boundary + "--");
				} catch (FileNotFoundException ex) {
					AppConstants.logger(null,"Unable to open file '"
							+ fileName + "'");
				} catch (IOException ex) {
					AppConstants.logger(null,"Error reading file '"
							+ fileName + "'");
					
				}

			} finally {
				if (writer != null)
					writer.close();
			}

			int responseCode = ((HttpURLConnection) connection)
					.getResponseCode();
			if (responseCode != 200) {
				throw new CustomException(AppConstants.FILE_UPLOAD_ERROR_CODE,
						"File Uploading Failed");
			} else {

				AppConstants.logger(null,"File Uploaded Successfully. File Name ==> "
								+ fileName + " and Response Code ==> "
								+ responseCode);
			}
		} catch (Exception e) {
			AppConstants.logger(null,e);
			throw new CustomException(AppConstants.FILE_UPLOAD_ERROR_CODE,
					"File Uploading Failed");
		}

	}

	public static void copyFile(String sourcePath, String destinationPath) {

		InputStream inStream = null;
		OutputStream outStream = null;

		try {

			File sourcefile = new File(sourcePath);
			File destinationFile = new File(destinationPath);
			inStream = new FileInputStream(sourcefile);
			outStream = new FileOutputStream(destinationFile);

			byte[] buffer = new byte[1024];
			int length;
			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, length);
			}
			inStream.close();
			outStream.close();
			// delete the original file
			// afile.delete();
		} catch (IOException e) {
			AppConstants.logger(null,e);
		}
	}

	public static void delete(String filePath) {

		try {

			File file = new File(filePath);
			file.delete();

		} catch (Exception e) {
			AppConstants.logger(null,e);
		}
	}
}
