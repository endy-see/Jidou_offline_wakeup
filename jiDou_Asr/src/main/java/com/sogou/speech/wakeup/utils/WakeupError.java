package com.sogou.speech.wakeup.utils;

public class WakeupError {
    /**
     * Audio Task Error List
     */
    public static final int ERROR_AUDIO_INITIALIZE_FAIL = -100;
    public static final int ERROR_AUDIO_START_FAIL = -101;
    
    /**
     * ASR Task Error List
     */
    public static final int ERROR_ASR_NO_WAKE_UP_WORD = -200;
	public static final int ERROR_ASR_ILLEGAL_ARGUMENT = -201;
	public static final int ERROR_ASR_MEMSET = -202;
	public static final int ERROR_ASR_INIT = -203;
	public static final int ERROR_ASR_UWORD_ADD = -204;
	public static final int ERROR_ASR_BUILD = -205;
	public static final int ERROR_ASR_START = -206;
		
	private int code;
	private String msg;
	
	public WakeupError(int code){
		setCode(code);
	}
	
	private void setCode(int code){
		this.code = code;
		switch(code) {
		case ERROR_AUDIO_INITIALIZE_FAIL:
			this.msg = "ERROR_AUDIO_INITIALIZE_FAIL";
			break;
		case ERROR_AUDIO_START_FAIL:
			this.msg = "ERROR_AUDIO_START_FAIL";
			break;
		case ERROR_ASR_NO_WAKE_UP_WORD:
			this.msg = "ERROR_ASR_NO_WAKE_UP_WORD";
			break;
		case ERROR_ASR_ILLEGAL_ARGUMENT:
			this.msg = "ERROR_ASR_ILLEGAL_ARGUMENT";
			break;
		case ERROR_ASR_MEMSET:
			this.msg = "ERROR_ASR_MEMSET";
			break;
		case ERROR_ASR_INIT:
			this.msg = "ERROR_ASR_INIT";
			break;
		case ERROR_ASR_UWORD_ADD:
			this.msg = "ERROR_ASR_UWORD_ADD";
			break;
		case ERROR_ASR_BUILD:
			this.msg = "ERROR_ASR_BUILD";
			break;
		case ERROR_ASR_START:
			this.msg = "ERROR_ASR_START";
			break;
		default:
			this.msg = "未知错误";
		}
	}
	
	public String getMsg(){
		return msg;
	}
	
	public int getCode(){
		return code;
	}
	
	@Override
	public String toString() {
		return code + ": " + msg;
	}
}
