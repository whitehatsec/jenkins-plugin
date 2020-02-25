package com.whitehat.sentinel.plugin.jenkins.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.whitehat.sentinel.plugin.jenkins.shared.AppConstants;

public class BigUpload {

	public static String UploadBig(String url, String file, String title)
			throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(url);
		InputStream inputStream = new FileInputStream(file);
		String ret = null;

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		builder.addTextBody("title", title, ContentType.TEXT_PLAIN);

		builder.addBinaryBody("file", new File(file),
				ContentType.APPLICATION_OCTET_STREAM, file);

		HttpEntity entity = builder.build();
		post.setEntity(entity);
		HttpResponse response = client.execute(post);
		HttpEntity resEntity = response.getEntity();
		inputStream.close();
		//AppConstants.logger.println(response.getStatusLine());

		if (resEntity != null) {

			ret = EntityUtils.toString(resEntity);

		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
		// HttpClient client = new DefaultHttpClient();
		String url = "http://192.168.180.218/api/misc/uploadCodebase/";
		String file = "C:/Program Files (x86)/Jenkins/jobs/b/builds/16/WhiteHatZip.tar.gz";// mybigarchive3.tar.gz";//bigarchive2.tar.gz";//myarchive.tar.gz"
																			// ;
		String title = "prabhu333SuperBIG2.tar.gz";

		String ret = BigUpload.UploadBig(url, file, title);

		int idx = ret.indexOf("File Upload Succeeded.");
		String ret1 = ret.substring(idx, 200);
		AppConstants.logger(null,ret1);

	}
}
