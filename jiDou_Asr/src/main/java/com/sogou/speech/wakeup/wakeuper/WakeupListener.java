package com.sogou.speech.wakeup.wakeuper;

public interface WakeupListener {
	void onResult(String result, boolean accept);
	
	void onError(String errorMsg, int errorCode);
	
	void onBeginOfSpeech();
	
	void onPassedValidation();
}
