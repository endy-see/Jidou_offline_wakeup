package com.sogou.speech.wakeup.auth;


public interface ISettingsUtils {
	// connection Timeout(ms)
	public static final int CONNECTION_TIME_OUT = 10000;

	// read Timeout(ms)
	public static final int READ_TIME_OUT = 10000;

	// add retry times
	public static final int RETRY_TIMES = 1;

	// URL for HTTP server
	// TODO:set value for POST_URL, just for debug!!!!!!!!!!!!!!!!!!!!2015-04-13
	public static final String POST_URL = "http://op.speech.sogou.com/index.cgi";

	// length of reply determined by HTTP server(byte)
	public static final int REPLY_LENGTH = 256;

	// max encrypt string length, add by yuanbin on 2013-08-08
	public static final int MAX_ENCRYPT_STR_LEN = 1025;

	// max available times , set to 10^5 , 2015-11-30
	// set MAX_AVAILABLE_TIMES to 5, just for debug!!!!!!!!!!!!! 2015-04-10
	public static final int MAX_AVAILABLE_TIMES = 100000;
//	public static final int MAX_AVAILABLE_TIMES = 3;
	public static final int PRE_AMOUNT_TIMES = 100000;
//	public static final int PRE_AMOUNT_TIMES = 3;

	// API Version, 2015-04-13
	public static final int API_VERSION = 2000;
}
