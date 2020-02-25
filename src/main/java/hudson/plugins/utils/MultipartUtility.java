package hudson.plugins.utils;

import hudson.plugins.shared.AppConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * This utility class provides an abstraction layer for sending multipart HTTP
 * POST requests to a web server.
 * 
 * @author www.codejava.net
 * 
 */
public class MultipartUtility {
	private final String boundary;
	private static final String LINE_FEED = "\r\n";
	private HttpURLConnection httpConn;
	private String charset;
	private OutputStream outputStream;
	private PrintWriter writer;

	/**
	 * This constructor initializes a new HTTP POST request with content type is
	 * set to multipart/form-data
	 * 
	 * @param requestURL
	 * @param charset
	 * @throws IOException
	 */
	public MultipartUtility(String requestURL, String charset)
			throws IOException {
		this.charset = charset;

		// creates a unique boundary based on time stamp
		boundary = "===" + System.currentTimeMillis() + "===";

		URL url = new URL(requestURL);
		httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setUseCaches(false);
		httpConn.setDoOutput(true); // indicates POST method
		httpConn.setDoInput(true);
		httpConn.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		httpConn.setRequestProperty("User-Agent", "CodeJava Agent");
		httpConn.setRequestProperty("Test", "Bonjour");
		outputStream = httpConn.getOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
				true);
	}

	/**
	 * Adds a form field to the request
	 * 
	 * @param name
	 *            field name
	 * @param value
	 *            field value
	 */
	public void addFormField(String name, String value) {
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
				.append(LINE_FEED);
		writer.append("Content-Type: text/plain; charset=" + charset).append(
				LINE_FEED);
		writer.append(LINE_FEED);
		writer.append(value).append(LINE_FEED);
		writer.flush();
	}

	/**
	 * Adds a upload file section to the request
	 * 
	 * @param fieldName
	 *            name attribute in <input type="file" name="..." />
	 * @param uploadFile
	 *            a File to be uploaded
	 * @throws IOException
	 */
	public void addFilePart(String fieldName, File uploadFile)
			throws IOException, CustomException {
		String fileName = uploadFile.getName();
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append(
				"Content-Disposition: form-data; name=\"" + fieldName
						+ "\"; filename=\"" + fileName + "\"")
				.append(LINE_FEED);
		writer.append(
				"Content-Type: "
						+ URLConnection.guessContentTypeFromName(fileName))
				.append(LINE_FEED);
		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();

		FileInputStream inputStream = new FileInputStream(uploadFile);
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		// while ((bytesRead = inputStream.read(buffer)) != -1) {
		// outputStream.write(buffer, 0, bytesRead);
		// }
		int ct = 0;
		try {

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
				ct++;
				// outputStream.flush();

				if (ct % 100 == 0) {
					System.out.println("cnt " + ct);
					outputStream.flush();
				}
			}

			outputStream.flush();
			inputStream.close();

			writer.append(LINE_FEED);
			writer.flush();
		} catch (Exception e) {
			throw new CustomException(AppConstants.FILE_UPLOAD_ERROR_CODE,
					"The file upload was not successfull");
		}

		// try
		// {
		// FileInputStream fileInputStream = new FileInputStream(filePath);
		// byte[] buf=new byte[8192];
		// int bytesread = 0, bytesBuffered = 0;
		//
		// while( (bytesread = fileInputStream.read( buf )) > -1 ) {
		// outputStream.write(buf, 0, bytesread);
		// bytesBuffered += bytesread;
		// if (bytesBuffered > 1024 * 1024) { //flush after 1MB
		//
		// bytesBuffered = 0;
		// outputStream.flush();
		// }
		// }
		//
		// }
		// finally {
		// if (outputStream != null) {
		// outputStream.flush();
		// }
		// }
		// System.out.println("Started Reading");
		// allocate the stream ... only for example
		// final InputStream input = new FileInputStream(filePath);
		// //final OutputStream output = new FileOutputStream(outputFile);
		// // get an channel from the stream
		// final ReadableByteChannel inputChannel = Channels.newChannel(input);
		// final WritableByteChannel outputChannel =
		// Channels.newChannel(outputStream);
		// // copy the channels
		// ChannelTools.fastChannelCopy(inputChannel, outputChannel);
		// // closing the channels
		// inputChannel.close();
		// outputChannel.close();
		// System.out.println("Completed Reading");
		// BigFile file;
		// try {
		// file = new BigFile(filePath);
		//
		// for (String line : file){
		// writer.write(line);
		// //writer.print(line);
		// //outputStream.write(line.getBytes());
		// ct++;
		// System.out.println(ct);
		// if(ct%10 ==0){
		// //writer.flush();
		// }
		// }
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	/**
	 * Adds a header field to the request.
	 * 
	 * @param name
	 *            - name of the header field
	 * @param value
	 *            - value of the header field
	 */
	public void addHeaderField(String name, String value) {
		writer.append(name + ": " + value).append(LINE_FEED);
		writer.flush();
	}

	/**
	 * Completes the request and receives response from the server.
	 * 
	 * @return a list of Strings as response in case the server returned status
	 *         OK, otherwise an exception is thrown.
	 * @throws IOException
	 */
	public List<String> finish() throws IOException {
		List<String> response = new ArrayList<String>();

		writer.append(LINE_FEED).flush();
		writer.append("--" + boundary + "--").append(LINE_FEED);
		writer.close();

		// checks server's status code first
		int status = httpConn.getResponseCode();
		if (status == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					httpConn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				response.add(line);
			}
			reader.close();
			httpConn.disconnect();
		} else {
			throw new IOException("Server returned non-OK status: " + status);
		}

		return response;
	}
}