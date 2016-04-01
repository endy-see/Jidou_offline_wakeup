package com.sogou.speech.wakeup.settings;

/**
 * configure variants
 */
public interface ISettingUtils {

    // pressed voice length for each request determined by HTTP server(byte)
    public static final int HTTP_PACKAGE_SIZE = 3000;

    // retry times for sending HTTP request, update by yuanbin, 2013-08-08
    public static final int HTTP_RETRY_TIMES = 3;

    // add by yuanbin on 2013-08-08
    public static final int HTTP_IO_ERROR_RETRY_TIMES = 2;

    // connection Timeout(ms), update by yuanbin, 2013-08-08
    public static final int CONNECTION_TIME_OUT = 20000;

    // read Timeout(ms) for package[0, n-1] in 2G and unknown, 2013-08-08
    public static final int READ_TIME_OUT_2G = 10000;

    // read Timeout(ms) for package[0, n-1] in 3G and 4G, 2013-08-08
    public static final int READ_TIME_OUT_3G = 9000;

    // read Timeout(ms) for package[0, n-1] in WiFi, 2013-08-08
    public static final int READ_TIME_OUT_WIFI = 8000;

    // read Timeout(ms) for the last package, created by hfy911, 2013-04-12
    public static final int FINAL_READ_TIME_OUT = 20000;

    // wait time(ms) for stopListening, 2014-04-01
    public static final int STOP_LISTENING_WAIT_TIME = 1000;
    
    // URL for HTTP server, update by hfy911, 2012-11-21
    public static final String POST_URL = "http://speech.sogou.com/index.cgi";
    // must use it for cantonese and mandarin switch test, 2013-10-28
    // public static final String POST_URL = "http://10.13.197.190/index.cgi";
    // must use it for continuous recognition test, 2013-09-23
    // public static final String POST_URL =
    // "http://10.12.11.41:8080/index.cgi";

    // thread amount in thread pool, update by hfy911, 2013-09-23
    public static final int MAX_THREAD_AMOUNT = 8;

    // length of reply determined by HTTP server(byte)
    public static final int REPLY_LENGTH = 2600;

    // max result amount, scope is [1, 5]
    public static final int MAX_RESULT_AMOUNT = 5;

    public static final boolean USE_DENOISE_AGC = false;

    /**
     * API version. <br>
     * 1003, add package_name, input_type, action_type when sending speech-reco
     * request. 2013/02/20. <br>
     * 1004, add retry feature for http timeout.<br>
     * 1005, add uploading log for network and audio error.<br>
     * 1006, fix bugs on VAD. 2013-12-26<br>
     * 1007, guarantee the last package has been sent. 2014-05-22<br>
     * 1050, add continuous speech recognition. 2013-12-26<br>
     */
    public static final int API_VERSION = 6050;

    // API version for old style recognition , update on 2014-05-22
    public static final int OLD_API_VERSION = 6000;
    // set value to 1000 for inner test, 2013-10-28
    // public static final int OLD_API_VERSION = 1000;

    // minimum buffer for AudioRecord, measured by Bytes, 2012-12-18
    public static final int MIN_BUFFER = 4480;

    // ERROR_NETWORK_IO sleep time, add by yuanbin on 2013-08-08
    public static final int ERROR_NETWORK_IO_SLEEP_TIME = 500;

    // max encrypt string length, add by yuanbin on 2013-08-08
    public static final int MAX_ENCRYPT_STR_LEN = 1025;

    // max error log size for uploading, unit: KB, update on 2013-08-15
    public static final int MAX_UPLOAD_LOG_SIZE = 10;

    // max error log size for storage, unit: KB, update on 2013-08-15
    public static final int MAX_ERROR_LOG_SIZE = 20;

    public static final String ALERT_SET_NETWORK_AUDIO_DIR = "Please set network_audio_err_file_dir";
}
