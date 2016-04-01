package com.sogou.speech.wakeup.auth;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.R.integer;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.sogou.speech.wakeup.utils.SpeechConstant;
import com.sogou.speech.wakeup.utils.SpeechConstant.VoiceWakeuperError;
import com.sogou.speech.wakeup.utils.WakeupError;
import com.sogou.speech.wakeup.wakeuper.VoiceWakeuper;
import com.sohu.inputmethod.voice.encrypt.EncryptIMEInterface;

public class Authentication implements ISettingsUtils {

	public final String TAG = "Authentication";

	private Context mContext;
	private Handler cHandler;
	private TelephonyManager mTelephonyManager;
	private ConnectivityManager mConnectivityManager;

	private String appId;
	private String accessKey;
	private String packageName;
	private String imeiNo;
	private String startTime;
	private String encryptContent;
	private String validateSign;
	// private int availableTimes;
	private int responseStatus;
	private final boolean DEBUG = false;

	// private final String HAS_AUTHED = "hasAuthenticated";
	// private final String PRE_AMOUNT = "preAmount";
	// private final String AVAIL_TIMES = "availableTimes";
	// private final String SIGN_KEY = "sign";
	private final String HAS_AUTHED = "var1";
	private final String PRE_AMOUNT = "var2";
	private final String AVAIL_TIMES = "var3";
	private final String SIGN_KEY = "var4";

	private final int PASS_VALIDATION = 1000;
	private final int CONTEXT_NOT_INITIALIZED = 1001;
	private final int PRE_AMOUNT_USED_UP = 1002;
	private final int SING_NOT_MATCH = 1003;
	private final int WRITE_SHARE_PREFERENCE_ERROR = 1004;
	private final int NETWORK_NOT_AVAILABLE = 1005;
	private final int GET_VALIDATE_SIGN_ERROR = 1006;
	private final int NO_VALIDATE_SIGN_OR_AVAILABLE_TIMES_ERROR = 1007;

	private final String VALIDATION_SHARED_PREFERCE = "sORCSP";

	public Authentication(String appId, String accessKey, Handler handler,
			Context context) {
		this.appId = appId;
		this.accessKey = accessKey;
		cHandler = handler;
		mContext = context;
		packageName = getPackageName();
		startTime = getStartTime();
		imeiNo = getImeiNo();
	}

	public Authentication(String appId, String accessKey, Handler handler,
			Context context, int n) {
		this.appId = appId;
		this.accessKey = accessKey;
		cHandler = handler;
		mContext = context;
		packageName = getPackageName();
		startTime = getStartTime();
		imeiNo = getImeiNo();
	}

	public void startAuthenticating() {
		if (DEBUG) {
			Log.d(TAG, "-->start authentication ");
		}
		UpdateUIThread mThread = new UpdateUIThread();
		new Thread(mThread).start();
	}

	public int handleUpdateTimesRequest() {

		SharedPreferences var1 = mContext.getSharedPreferences(
				VALIDATION_SHARED_PREFERCE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = var1.edit();
		boolean hasAuthed = var1.getBoolean(HAS_AUTHED, false);
		EncryptDecrypt encryptDecrypt = new EncryptDecrypt();
		if (!hasAuthed) {
			int preAmount = encryptDecrypt.decryptTimes(var1.getString(
					PRE_AMOUNT, ""));
			if (preAmount > 0) {
				preAmount--;
				if (DEBUG) {
					Log.d("", "-->preAmount -- , now is:" + preAmount);
				}
				boolean res = editor.putString(PRE_AMOUNT,
						encryptDecrypt.encryptTimes(preAmount)).commit();
				if (res) {
					return 0;
				} else {
					return -1;
				}

			} else {
				// preAmount used up , onlineValidate
				if (DEBUG) {
					Log.w("",
							"-->preAmount used up, please onlineValidate.");
				}

				cHandler.obtainMessage(
						VoiceWakeuperError.ERR_PRE_AMOUNT_USED_UP)
						.sendToTarget();
				// int res = onlineValidate();
				// if (res == PASS_VALIDATION) {
				// return 0;
				// } else {
				// return -1;
				// }
				return -1;
			}
		} else {
			int availableTimes = encryptDecrypt.decryptTimes(var1.getString(
					AVAIL_TIMES, null));
			if (DEBUG) {
				Log.w(TAG, "-->available times, before -- is:" + availableTimes);
			}
			if (availableTimes <= 0) {
				if (DEBUG) {
					Log.w("",
							"-->avail times used up, please onlineValidate.");
				}
				cHandler.obtainMessage(
						VoiceWakeuperError.ERR_NO_AVAILABLE_TIMES)
						.sendToTarget();
				return -1;
			} else {
				if (setTimesToSharedPreferences(--availableTimes) < 0) {
					return -1;
				}

			}
			return 0;

		}

	}

	private String getStartTime() {
		long currentTime = System.currentTimeMillis();
		startTime = String.valueOf(currentTime);
		return startTime;
	}

	/**
	 * if return-value is null, then ERROR may be happened!
	 * 
	 * @author xiantao
	 * @since 2012-08-07 <uses-permission
	 *        android:name="android.permission.READ_PHONE_STATE"/>
	 */
	private String getImeiNo() {
		String imeiNum = null;
		// call getDeviceId once, 2012-11-23
		// judge whether mTelephonyManager is null to avoid
		// NullpointerException, 2013-12-27
		boolean isNull = true;
		String tmpIMEI = null;
		if (mTelephonyManager != null) {
			tmpIMEI = mTelephonyManager.getDeviceId();
			isNull = false;
		}

		// change true to false for isNull, 2013-12-30
		if (isNull == false && isValidIMEI(tmpIMEI)) {
			imeiNum = tmpIMEI;
		} else {
			// if length of IMEI is less than 15, create a random string for it
			Random ram = new Random(System.nanoTime());
			StringBuffer sb = new StringBuffer();

			for (int i = 0; i < 15; ++i) {
				String str = String.valueOf(Math.abs(ram.nextInt()) % 10);
				sb.append(str);
			}
			imeiNum = sb.toString();
		}

		return imeiNum;
	}

	/**
	 * judge IMEI validity
	 * 
	 * @author xiantao
	 * @since 2012-08-08
	 * 
	 */
	private boolean isValidIMEI(String imei) {

		boolean flag = false;

		// if its length is 15 and all are digit, set flag true
		// remove judging "" for imei, 2012-11-23
		if (imei != null) {
			// judge whether it is IMEI for GSM
			Pattern p = Pattern.compile("\\d{15}");
			Matcher m = p.matcher(imei);
			flag = m.matches();

			// judge whether it is MEID for CDMA
			if (flag == false) {
				Pattern p2 = Pattern.compile("[A-F][0-9A-F]{13}",
						Pattern.CASE_INSENSITIVE);
				Matcher m2 = p2.matcher(imei);
				flag = m2.matches();

				// judge whether it is ESN for CDMA
				if (flag == false) {
					Pattern p3 = Pattern.compile("([0-9A-F]{8})|\\d{11}",
							Pattern.CASE_INSENSITIVE);
					Matcher m3 = p3.matcher(imei);
					flag = m3.matches();
				}
			}

		}

		return flag;
	}

	private String getEncryptContent() {
		EncryptIMEInterface encInterface = EncryptIMEInterface.getInterface();
		byte[] encryptBytes = new byte[MAX_ENCRYPT_STR_LEN];

		// add v, 2015-04-13
		String wholeString = "id=" + appId + "&key=" + accessKey + "&name="
				+ packageName + "&ts=" + startTime + "&in=" + imeiNo + "&v="
				+ API_VERSION;
		// just for debug!!!
		if (DEBUG) {
			Log.d("getEncryptContent", "-->online validate sign wholeString:"
					+ wholeString);
		}
		int encryptLength = encInterface.encryptSource(wholeString.getBytes(),
				encryptBytes);
		encryptContent = (new String(encryptBytes)).substring(0, encryptLength);
		encInterface.destroy();
		return encryptContent;
	}

	private String getSignFromServer() {
		String finalSign = null;
		encryptContent = getEncryptContent();
		HandleHTTPRequestTask mTask = new HandleHTTPRequestTask(encryptContent);
		responseStatus = mTask.getReplyStatus();
		if (responseStatus < 0) {
			return finalSign;
		}

		// parse finalSign
		DecodeResult mResult = new DecodeResult();
		responseStatus = mResult.parseResult(mTask.getReplyContent());
		if (responseStatus < 0) {
			return finalSign;
		}

		responseStatus = mResult.getStatus();
		finalSign = mResult.getSign();

		return finalSign;
	}

	/**
	 * check whether network is available
	 * 
	 * @return true when available, false when unavailable
	 * @author songchunwei
	 * @since 2013-08-08
	 */
	private boolean isNetworkAvailable() {
		if (mContext == null) {
			// just for debug!!!
			// if (DEBUG) {
			// Log.e("isNetworkAvailable", "mContext:" + mContext);
			// }
			return false;
		}

		mConnectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (mConnectivityManager == null) {
			// just for debug!!!
			// if (DEBUG) {
			// Log.e("isNetworkAvailable", "mConnectivityManager:"
			// + mConnectivityManager);
			// }
			return false;
		} else {
			try {
				NetworkInfo[] info = mConnectivityManager.getAllNetworkInfo();
				if (info != null) {
					for (int i = 0; i < info.length; i++) {
						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
							return true;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				// just for debug!!!
				// if (DEBUG) {
				// Log.e("isNetworkAvailable", e.toString());
				// }
				return false;
			}
		}

		// just for debug!!!
		// if (DEBUG) {
		// Log.e("isNetworkAvailable", "false.");
		// }
		return false;
	}

	private int handleValidateRequest() {

		/*
		 * Modified for JiDou, change validate logic
		 */
		if (mContext == null) {
			return CONTEXT_NOT_INITIALIZED;
		}
		SharedPreferences var1 = mContext.getSharedPreferences(
				VALIDATION_SHARED_PREFERCE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = var1.edit();
		EncryptDecrypt encryptDecrypt = new EncryptDecrypt();
		// String tempSign = encryptDecrypt.generateSign(appId,
		// accessKey,packageName);

		if (!var1.contains(HAS_AUTHED)) {
			if (DEBUG) {
				Log.e(TAG, "-->sp does not have auth key");
			}
			/* sp does not contain key HAS_AUTHED */
			editor.putBoolean(HAS_AUTHED, false);
			editor.putString(PRE_AMOUNT,
					encryptDecrypt.encryptTimes(PRE_AMOUNT_TIMES)); // maybe
																	// PRE_AMOUNT_TIMES-1
																	// is more
																	// suitable?
			if (DEBUG) {
				Log.e(TAG, "-->set pre_amount_times:" + PRE_AMOUNT_TIMES);
			}
			boolean flag1 = editor.commit();
			if (flag1) {
				return PASS_VALIDATION;
			} else {
				return WRITE_SHARE_PREFERENCE_ERROR;
			}

		} else {
			/* sp contains key HAS_AUTHED */
			if (DEBUG) {
				Log.e(TAG, "-->sp already has auth key");
			}
			boolean hasAuthed = var1.getBoolean(HAS_AUTHED, false);
			String encryptString = var1.getString(PRE_AMOUNT, null);
			int preAmount = encryptDecrypt.decryptTimes(encryptString);
			if (DEBUG) {
				Log.e(TAG, "--> hasAuthed:" + hasAuthed + ",preAmount:"
						+ preAmount);
			}

			if (hasAuthed == false) {
				if (preAmount > 0) {
					// preAmount--;
					return PASS_VALIDATION;

				} else {

					// return PRE_AMOUNT_USED_UP;
					if (DEBUG) {
						Log.d(TAG, "-->pre amount used up");
					}
					int res = onlineValidate();
					if (DEBUG) {
						Log.d("", "-->online validate res:" + res);
					}
					return res;
				}
			} else {
				/*
				 * has online validated before
				 */
				int availableTimes = queryAvailableTimes();
				if (availableTimes <= 0) {
					int res = onlineValidate();
					return res;
				}
				if (getSignAndTimesFromSharedPreferences()) {
					String tempSign = encryptDecrypt.generateSign(appId,
							accessKey, packageName);
					if (DEBUG) {
						Log.e("handleValidateRequest", "-->tempSign["
								+ tempSign + "], validateSign[" + validateSign
								+ "]");
					}
					if (tempSign != null && tempSign.equals(validateSign)) {
						return PASS_VALIDATION;
					} else {
						return SING_NOT_MATCH;
					}

				} else {
					return NO_VALIDATE_SIGN_OR_AVAILABLE_TIMES_ERROR;
				}

			}
		}

	}

	private String getPackageName() {
		if (mContext == null) {
			return "";
		}

		return mContext.getPackageName();
	}

	private int setTimesToSharedPreferences(int times) {

		if (mContext == null) {
			return -1;
		}

		SharedPreferences var1 = mContext.getSharedPreferences(
				VALIDATION_SHARED_PREFERCE, Context.MODE_PRIVATE);
		String encryptedAvailableTimes = null;
		EncryptDecrypt mEncryptDecrypt = new EncryptDecrypt();
		encryptedAvailableTimes = mEncryptDecrypt.encryptTimes(times);
		Editor mEditor = var1.edit();
		mEditor.putString(AVAIL_TIMES, encryptedAvailableTimes);
		// just for debug!!!
		// if (DEBUG) {
		// Log.e("setTimesToSharedPreferences", "times[" + times
		// + "], finalTimes[" + encryptedAvailableTimes + "]");
		// }
		if (mEditor.commit() == false) {
			return -1;
		}

		return 0;

	}

	private int setSignToSharedPreferences(String sign) {

		if (sign == null || sign.equals("") || mContext == null) {
			return -1;
		}

		SharedPreferences var1 = mContext.getSharedPreferences(
				VALIDATION_SHARED_PREFERCE, Context.MODE_PRIVATE);
		Editor mEditor = var1.edit();
		mEditor.putString(SIGN_KEY, sign);
		if (mEditor.commit() == false) {
			return -1;
		}

		return 0;

	}

	/*
	 * Query remaining available times after online validation. If not yet
	 * validated, return 0.
	 */
	public int queryAvailableTimes() {

		SharedPreferences var1 = mContext.getSharedPreferences(
				VALIDATION_SHARED_PREFERCE, Context.MODE_PRIVATE);
		if (var1.contains(AVAIL_TIMES)) {
			EncryptDecrypt encryptDecrypt = new EncryptDecrypt();
			String encryptAvailTimes = var1.getString(AVAIL_TIMES, null);
			int availableTimes = encryptDecrypt.decryptTimes(var1.getString(
					AVAIL_TIMES, null));
			if (DEBUG) {
				// Log.d(TAG, "-->encrypt available times:" +
				// encryptAvailTimes);
				Log.d(TAG, "-->query left available times:" + availableTimes);
			}
			if (availableTimes <= 0) {
				return 0;
			} else {
				return availableTimes;
			}
		} else {
			return 0;
		}

	}

	/*
	 * Perform online validation. If success , set availableTime = 1000,
	 * hasAuthenticated = true, preAmount = 0, return passed flag. Else return
	 * error code.
	 */
	public int onlineValidate() {

		if (isNetworkAvailable() == false) {
			if (DEBUG) {
				Log.w("", "-->network not available , cannot online validate.");
			}
			return NETWORK_NOT_AVAILABLE;
		}

		validateSign = getSignFromServer();
		if (DEBUG) {
			Log.d(TAG, "-->validateSign:" + validateSign);
		}

		if (validateSign == null || validateSign.length() == 0) {
			return GET_VALIDATE_SIGN_ERROR;
		} else {
			/* online validation success */
			if (mContext == null) {
				return CONTEXT_NOT_INITIALIZED;
			}

			SharedPreferences sp = mContext.getSharedPreferences(
					VALIDATION_SHARED_PREFERCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sp.edit();
			EncryptDecrypt encryptDecrypt = new EncryptDecrypt();
			editor.putBoolean(HAS_AUTHED, true);
			editor.putString(AVAIL_TIMES,
					encryptDecrypt.encryptTimes(MAX_AVAILABLE_TIMES));
			editor.putString(PRE_AMOUNT, encryptDecrypt.encryptTimes(0));
			/* set validate sign to sp */
			editor.putString(SIGN_KEY, validateSign);
			boolean flag = editor.commit();
			if (DEBUG) {
				Log.d(TAG, "-->available times:" + MAX_AVAILABLE_TIMES
						+ ",validateSign:" + validateSign + ",commit flag:"
						+ flag);
			}

			if (!flag) {
				return WRITE_SHARE_PREFERENCE_ERROR;
			}
			return PASS_VALIDATION;
		}

	}

	private boolean getSignAndTimesFromSharedPreferences() {

		if (mContext == null) {
			return false;
		}

		SharedPreferences var1 = mContext.getSharedPreferences(
				VALIDATION_SHARED_PREFERCE, Context.MODE_PRIVATE);

		String sign = var1.getString(SIGN_KEY, "");
		String encyptedAvailableTimes = var1.getString(AVAIL_TIMES, "");

		// just for debug!!!
		if (DEBUG) {
			Log.e("getSignAndTimesFromSharedPreferences", "-->sign[" + sign
					+ "], encyptedAvailableTimes[" + encyptedAvailableTimes
					+ "]");
		}
		if (sign.equals("") || encyptedAvailableTimes.equals("")) {
			return false;
		}

		// set value for validateSign
		validateSign = sign;
		// set value for availableTimes
		EncryptDecrypt mEncryptDecrypt = new EncryptDecrypt();
		int availableTimes = mEncryptDecrypt
				.decryptTimes(encyptedAvailableTimes);

		// just for debug!!!
		if (DEBUG) {
			Log.e("getSignAndTimesFromSharedPreferences", "-->validateSign["
					+ validateSign + "], availableTimes[" + availableTimes
					+ "]");
		}
		if (availableTimes > 0) {
			return true;
		} else {
			return false;
		}

	}

	private class UpdateUIThread implements Runnable {
		@Override
		public void run() {
			int returnValue = 0;
			returnValue = handleValidateRequest();
			if (DEBUG) {
				Log.e("", "-->handleValidateRequest return value:"
						+ returnValue);
			}
			switch (returnValue) {
			case PASS_VALIDATION:
				cHandler.obtainMessage(VoiceWakeuper.MSG_ON_SUCCESS)
						.sendToTarget();
				break;

			case CONTEXT_NOT_INITIALIZED:
				cHandler.obtainMessage(
						VoiceWakeuperError.ERR_CONTEXT_NOT_INITIALIZED)
						.sendToTarget();
				break;

			case PRE_AMOUNT_USED_UP:
				cHandler.obtainMessage(
						VoiceWakeuperError.ERR_PRE_AMOUNT_USED_UP)
						.sendToTarget();
				break;

			case SING_NOT_MATCH:
			case GET_VALIDATE_SIGN_ERROR:
				cHandler.obtainMessage(
						VoiceWakeuperError.ERR_VALIDATE_SIGN_NOT_MATCH)
						.sendToTarget();
				break;

			case WRITE_SHARE_PREFERENCE_ERROR:
				cHandler.obtainMessage(
						VoiceWakeuperError.ERR_WRITE_SHARED_PREFERENCE)
						.sendToTarget();
				break;

			case NETWORK_NOT_AVAILABLE:
				cHandler.obtainMessage(
						VoiceWakeuperError.ERR_NETWORK_IS_UNAVAILABLE)
						.sendToTarget();
				return;

			case NO_VALIDATE_SIGN_OR_AVAILABLE_TIMES_ERROR:
				cHandler.obtainMessage(
						VoiceWakeuperError.ERR_NO_VALIDATE_SIGN_OR_AVAIL_TIMES)
						.sendToTarget();
			default:
				cHandler.obtainMessage(VoiceWakeuperError.ERR_KEY_IS_INVALID)
						.sendToTarget();
				return;
			}

		}
	}

}
