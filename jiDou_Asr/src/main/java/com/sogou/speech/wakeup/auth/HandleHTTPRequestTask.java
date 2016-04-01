package com.sogou.speech.wakeup.auth;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.http.protocol.HTTP;

import android.util.Log;

public class HandleHTTPRequestTask implements ISettingsUtils {
	private String encryptContent = "";
//	private static final String TAG = "HandleHTTPRequestTask";
	private static final boolean DEBUG = false;

	private String replyContent = "";

	public String getReplyContent() {
		return replyContent;
	}

	public void setReplyContent(String replyContent) {
		this.replyContent = replyContent;
	}

	private String inputStream2String(InputStream in, String encoding)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		InputStreamReader inread = new InputStreamReader(in, encoding);
		char[] buf = new char[REPLY_LENGTH];
		int readLen = 0;
		while ((readLen = inread.read(buf)) != -1) {
			sb.append(buf, 0, readLen);
		}
		buf = null;
		inread.close();
		return sb.toString();
	}

	private HttpURLConnection openConnection(URL url) throws IOException {
		// use HttpURLConnection open the connection
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
		urlConn.setDoOutput(true);
		urlConn.setDoInput(true);

		// must set request method be POST
		urlConn.setRequestMethod("POST");

		// set connection Timeout
		urlConn.setConnectTimeout(CONNECTION_TIME_OUT);

		// set read Timeout, 2012-10-29
		urlConn.setReadTimeout(READ_TIME_OUT);

		// must set false for the attribute below
		urlConn.setUseCaches(false);

		// must set false for the attribute below
		urlConn.setInstanceFollowRedirects(false);

		urlConn.addRequestProperty("Accept-Charset", "UTF-8");
		urlConn.addRequestProperty("Accept-Encoding", HTTP.IDENTITY_CODING);

		// open the connect obviously
		urlConn.connect();
		return urlConn;
	}

	public HandleHTTPRequestTask(String encryptContent) {
		this.encryptContent = encryptContent;
	}

	public int getReplyStatus() {
		int statusCode = 0;
		// add retryTimes, 2015-02-09
		int retryTimes = 0;
		while (retryTimes <= RETRY_TIMES) {
			try {
				HttpURLConnection urlConn = openConnection(new URL(POST_URL
						+ "?cmd=ov&content=" + encryptContent));

				// DataOutputStream
				DataOutputStream tmpOut = new DataOutputStream(
						urlConn.getOutputStream());

				// submit the request
				tmpOut.flush();
				// close sending stream
				tmpOut.close();

				// TODO:retry 1 times when status code is not 200!!!

				statusCode = urlConn.getResponseCode();
				if (statusCode == 200) {
					// get response content
					InputStream tmpIn = urlConn.getInputStream();
					// set content encode to UTF-8
					replyContent = inputStream2String(tmpIn, "UTF-8");
					// close receiving stream
					tmpIn.close();
					// close connection
					urlConn.disconnect();

					// just for debug!!!
//					if (DEBUG) {
//						Log.e(TAG, "encryptContent[" + encryptContent
//								+ "], replyContent[" + replyContent + "]");
//					}
					return 0;
				}
			} catch (MalformedURLException e1) {
				// TODO: handle exception
				// just for debug!!!
				// Log.e(TAG, "-1");
				e1.printStackTrace();
				if (retryTimes < RETRY_TIMES) {
					retryTimes++;
					continue;
				}
				return -1;
			} catch (SocketTimeoutException e2) {
				// just for debug!!!
				// Log.e(TAG, "-2");
				e2.printStackTrace();
				if (retryTimes < RETRY_TIMES) {
					retryTimes++;
					continue;
				}
				return -2;
			} catch (ProtocolException e3) {
				// just for debug!!!
				// Log.e(TAG, "-3");
				e3.printStackTrace();
				if (retryTimes < RETRY_TIMES) {
					retryTimes++;
					continue;
				}
				return -3;
			} catch (UnsupportedEncodingException e4) {
				// just for debug!!!
				// Log.e(TAG, "-4");
				e4.printStackTrace();
				if (retryTimes < RETRY_TIMES) {
					retryTimes++;
					continue;
				}
				return -4;
			} catch (IOException e5) {
				// just for debug!!!
				// Log.e(TAG, "-5");
				e5.printStackTrace();
				if (retryTimes < RETRY_TIMES) {
					retryTimes++;
					continue;
				}
				return -5;
			}

			retryTimes++;
		}

		// Log.e(TAG, "statusCode[" + statusCode + "]");
		return -6;
	}
}