package com.sogou.speech.wakeup.utils;

import java.io.File;

import android.os.Environment;

public class SpeechConstant {
    /**
     * 婵繐绲块垾妯裤亹閺囥垻鍙鹃柣銊ュ缁绘氨锟芥稒锚濠�鎾锤閿燂拷
     */
//    public static final String SAVE_RECORD_PATH = "/mnt/sdcard/research/voice/";
    public static final String SAVE_RECORD_PATH = Environment.getExternalStorageDirectory()+File.separator+"JidouAudioRecord";
	
	/**
	 * 闁哄嫷鍨伴幆渚�鎳涢鍕楀ǎ鍥ㄧ箓閻°劌顫㈤敐鍥ｏ拷妯兼嫚閸℃鐒奸柣銊ュ缂嶅秹妫呴敓锟�
	 */
	public static boolean IS_SAVE_DIST = true;
	public static void setSaveAudioRecord(boolean flag){
		
	}
	
	/**
     * Audio Task Error List
     */
	public class AudioError{
		public static final int ERROR_AUDIO_INITIALIZE_FAIL = -100;
	    public static final int ERROR_AUDIO_START_FAIL = -101;
	}   
    
    /**
     * ASR Task Error List
     */
	public class AsrError{
		public static final int ERROR_ASR_NO_WAKE_UP_WORD = -200;
		public static final int ERROR_ASR_ILLEGAL_ARGUMENT = -201;
		public static final int ERROR_ASR_MEMSET = -202;
		public static final int ERROR_ASR_INIT = -203;
		public static final int ERROR_ASR_UWORD_ADD = -204;
		public static final int ERROR_ASR_BUILD = -205;
		public static final int ERROR_ASR_START = -206;
	}	
	
	public class VoiceWakeuperError{
		public static final int ERROR_SAVE_RAW_DATA_TO_DISK = -300;
		public static final int ERROR_START_LISTENING = -301;
		public static final int ERR_USE_SERVICE_FAILED = -302;
		public static final int ERR_NETWORK_IS_UNAVAILABLE = -303;
		public static final int ERR_KEY_IS_INVALID = -304;
		public static final int ERR_BIND_SERVICE = -305;
		public static final int ERR_PRE_AMOUNT_USED_UP = -306;
		public static final int ERR_VALIDATE_SIGN_NOT_MATCH = -307;
		public static final int ERR_WRITE_SHARED_PREFERENCE = -308;
		public static final int ERR_CONTEXT_NOT_INITIALIZED = -309;
		public static final int ERR_NO_VALIDATE_SIGN_OR_AVAIL_TIMES = -310;
		public static final int ERR_SERVICE_NOT_EXIST = -311;
		public static final int ERR_NO_AVAILABLE_TIMES = -312;
	}
	
	public final static String PARAMS = "params";
	/**
	 * 閻犱礁澧介悿鍡涘籍閵夈儳绠剁紒娑橆槺妤狅拷,1-5閻炴稏鍔庨妵姘憋拷鐟邦槸瀹曟粍绋夊鍛�辩紒鐙欏啫鐒奸柣銊ュ煇og閺夊牊鎸搁崵锟�</br> 0:濞戞挸绉风欢顓㈠礄閿燂拷;1:debug;5:error
	 */
	public final static String LOG_LEVEL = "log_level";
}
