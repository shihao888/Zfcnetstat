package com.example.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.test.AndroidTestCase;
/*
 * http://blog.csdn.net/liuhe688/article/details/6425225
 */
public class HttpTest extends AndroidTestCase {
	
	private static final String PATH = "http://192.168.1.57:8080/web";
	
	public void testGet() throws Exception {
		HttpClient client = new DefaultHttpClient();
    	HttpGet get = new HttpGet(PATH + "/TestServlet?id=1001&name=john&age=60");
    	HttpResponse response = client.execute(get);
    	if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        	InputStream is = response.getEntity().getContent();
        	String result = inStream2String(is);
        	Assert.assertEquals(result, "GET_SUCCESS");
    	}
	}
	
	public void testPost() throws Exception {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(PATH + "/TestServlet");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("id", "1001"));
		params.add(new BasicNameValuePair("name", "john"));
		params.add(new BasicNameValuePair("age", "60"));
		HttpEntity formEntity = new UrlEncodedFormEntity(params);
		post.setEntity(formEntity);
		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        	InputStream is = response.getEntity().getContent();
        	String result = inStream2String(is);
        	Assert.assertEquals(result, "POST_SUCCESS");
    	}
	}
	
	public void testUpload() throws Exception {
		InputStream is = getContext().getAssets().open("books.xml");
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(PATH + "/UploadServlet");
		InputStreamBody isb = new InputStreamBody(is, "books.xml");
		MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart("file", isb);
		multipartEntity.addPart("desc", new StringBody("this is description."));
		post.setEntity(multipartEntity);
		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        	is = response.getEntity().getContent();
        	String result = inStream2String(is);
        	Assert.assertEquals(result, "UPLOAD_SUCCESS");
    	}
	}
	
	//将输入流转换成字符串
	private String inStream2String(InputStream is) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len = -1;
		while ((len = is.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		return new String(baos.toByteArray());
	}
}

