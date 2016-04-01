package com.sogou.speech.wakeup.auth;


import org.json.JSONException;
import org.json.JSONObject;

public class DecodeResult {
	private int status = -911;
	private String message = "";
	private String sign = "";

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public DecodeResult() {
		// do nothing
	}

	public int parseResult(String originalResult) {
		if (originalResult == null || originalResult.length() == 0) {
			return -7;
		}

		try {
			JSONObject mObject = new JSONObject(originalResult);
			status = mObject.getInt("status");
			message = mObject.getString("message");
			sign = mObject.getString("sign");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -8;
		}

		return 0;
	}

}
