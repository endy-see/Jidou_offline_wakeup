package com.sogou.speech.wakeup.wakeuper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sogou.speech.wakeup.auth.Authentication;
import com.sogou.speech.wakeup.utils.SpeechConstant;
import com.sogou.speech.wakeup.utils.SpeechConstant.VoiceWakeuperError;
import com.sogou.speech.wakeup.wakeupservice.IWakeupCallback;
import com.sogou.speech.wakeup.wakeupservice.IWakeupService;

public class VoiceWakeuper {
	private Context mContext;
	private WakeupListener mWakeupListener;
	private IWakeupService wakeupBinder;
	private final String WAKEUP_SERVICE_ACTION = "com.sogou.speech.wakeupservice.WAKEUP_SERVICE";

	private String appId;
	private String accessKey;
	
	// boolean variable for use to switch on/off saveAudioToDisk function
	private static boolean saveAudioToDisk = false;

	private List<String> words = new ArrayList<String>();
	public Authentication authentication;
	private HashMap<String, String> paramHash = new HashMap<String, String>();

	public static final int MSG_ON_SUCCESS = 0;
	private static final int MSG_ON_RESULTS = 6;
	private static final int MSG_ON_ERROR = 7;

	private boolean isServiceConnected = false;

	private static final boolean DEBUG = false;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_ON_RESULTS:
				
				String result = (String) msg.obj;
				if(DEBUG){
					Log.d("","-->result in voice wakeuper:"+result);
				}
				if (mWakeupListener != null) {
					if(TextUtils.isEmpty(result)){
						mWakeupListener.onResult(result,false);
					}else{
						JSONObject jsonObject = null;
						try {
							jsonObject = new JSONObject(result);
							double confidence = jsonObject.optDouble("confid");
							if(confidence >= 0.5){
								mWakeupListener.onResult(result,true);
							}else{
								mWakeupListener.onResult(result,false);
							}
							
							// result 不为空时，将音频存储为pcm文件
							try {
								if(DEBUG){
									Log.d("", "--> save path:"+SpeechConstant.SAVE_RECORD_PATH+",result:"+result);
								}
								if (saveAudioToDisk)
									wakeupBinder.saveRawDataToDisk(
											SpeechConstant.SAVE_RECORD_PATH, result);
							} catch (RemoteException e) {
								if(DEBUG){
									Log.e("", "--> error during record to file:"+e.toString());
								}
								mWakeupListener.onError("ERROR_SAVE_RAW_DATA_TO_DISK",
										VoiceWakeuperError.ERROR_SAVE_RAW_DATA_TO_DISK);
								return;
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				

				break;

			case MSG_ON_ERROR:
				if (mWakeupListener != null) {
					String errorMsg = (String) msg.obj;
					int errorCode = msg.arg1;
					mWakeupListener.onError(errorMsg, errorCode);
				}
				break;

			case MSG_ON_SUCCESS:
				if (!isServiceConnected) {
					return;
				}

				if (wakeupBinder == null) {

					/*
					 * Check if wake up service package is exsit. If not , promt
					 * user to install it.
					 */
					mWakeupListener.onError("ERR_BIND_SERVICE",
							VoiceWakeuperError.ERR_BIND_SERVICE);
					return;
				}

				mWakeupListener.onPassedValidation();
				try {
					wakeupBinder.startListening(words);
				} catch (RemoteException e) {
					e.printStackTrace();
					mWakeupListener.onError("ERROR_START_LISTENING",
							VoiceWakeuperError.ERROR_START_LISTENING);
					return;

				}
				break;

			case VoiceWakeuperError.ERR_NETWORK_IS_UNAVAILABLE:
				mWakeupListener
						.onError(
								"ERR_NETWORK_IS_UNAVAILABLE",
								SpeechConstant.VoiceWakeuperError.ERR_NETWORK_IS_UNAVAILABLE);
				break;

			case VoiceWakeuperError.ERR_KEY_IS_INVALID:
				mWakeupListener.onError("ERR_KEY_IS_INVALID",
						VoiceWakeuperError.ERR_KEY_IS_INVALID);
				break;

			case VoiceWakeuperError.ERR_PRE_AMOUNT_USED_UP:
				mWakeupListener.onError("ERR_PRE_AMOUNT_USED_UP",
						VoiceWakeuperError.ERR_PRE_AMOUNT_USED_UP);
				break;

			case VoiceWakeuperError.ERR_VALIDATE_SIGN_NOT_MATCH:
				mWakeupListener.onError("ERR_VALIDATE_SIGN_NOT_MATCH",
						VoiceWakeuperError.ERR_VALIDATE_SIGN_NOT_MATCH);
				break;

			case VoiceWakeuperError.ERR_WRITE_SHARED_PREFERENCE:
				mWakeupListener.onError("ERR_WRITE_SHARED_PREFERENCE",
						VoiceWakeuperError.ERR_WRITE_SHARED_PREFERENCE);
				break;

			case VoiceWakeuperError.ERR_CONTEXT_NOT_INITIALIZED:
				mWakeupListener.onError("ERR_CONTEXT_NOT_INITIALIZED",
						VoiceWakeuperError.ERR_CONTEXT_NOT_INITIALIZED);
				break;

			case VoiceWakeuperError.ERR_NO_VALIDATE_SIGN_OR_AVAIL_TIMES:
				mWakeupListener.onError("ERR_NOT_ENOUGH_AVAIL_TIMES",
						VoiceWakeuperError.ERR_NO_VALIDATE_SIGN_OR_AVAIL_TIMES);
				break;
			case VoiceWakeuperError.ERR_NO_AVAILABLE_TIMES:
				mWakeupListener.onError("ERR_NOT_ENOUGH_AVAIL_TIMES",
						VoiceWakeuperError.ERR_NO_AVAILABLE_TIMES);

			default:
				break;
			}
		}
	};

	public VoiceWakeuper(Context context, String appId, String accessKey) {
		mContext = context;
		this.appId = appId;
		this.accessKey = accessKey;
	}

	public void setParameter(String key, String value) {
		if (key.equals(SpeechConstant.PARAMS) && value == null) {
			paramHash.clear();
			return;
		}

		paramHash.put(key, value);
	}

	// public int initializeService(){
	// /*
	// * check if wakeup service has been installed
	// * return value:
	// * 0 -- initialize success
	// * negative value -- initialize fail
	// * */
	//
	// // if(!isServiceExist(mContext)){
	// // isServiceConnected = false;
	// // return -1;
	// // }
	//
	// /* implicit way of starting service */
	//
	// final Intent wakeupServiceIntent = new Intent(WAKEUP_SERVICE_ACTION);
	// boolean res = mContext.bindService(wakeupServiceIntent, conn,
	// Context.BIND_AUTO_CREATE);
	// if(!res){
	// isServiceConnected = false;
	// return -2;
	// }
	// return 0;
	// }

	public int initializeService() {
		// from LOLLIPOP on, start a service with an implicit intent will throw
		// exception.
		// It's adviced to use an explit intent (set a component name)
		final Intent explicitWakeupServiceIntent = createExplicitFromImplicitIntent(
				mContext, new Intent(WAKEUP_SERVICE_ACTION));
		boolean res = mContext.bindService(explicitWakeupServiceIntent, conn,
				Context.BIND_AUTO_CREATE);

		if (!res) {
			isServiceConnected = false;
			return -2;
		}
		return 0;
	}

	public void addWakeupWord(String wakeupWord) {
		words.add(wakeupWord);
	}

	public void startListening(WakeupListener wakeupListener) {
		if (!isServiceConnected) {
			if(DEBUG){
				Log.w("","-->service not connected in start listening in VoiceWakeuper, return directly");
			}
			return;
		}
		mWakeupListener = wakeupListener;

		authentication = new Authentication(appId, accessKey, mHandler,
				mContext);
		authentication.startAuthenticating();
	}

	public void stopListening() {
		if (!isServiceConnected) {
			return;
		}
		try {
			if (wakeupBinder != null)
				wakeupBinder.stopListening();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public boolean setErrorLogPath(String path) {
		if (!isServiceConnected) {
			return false;
		}
		try {
			wakeupBinder.setErrorLogPath(path);
		} catch (RemoteException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void destroy() {
		if (!isServiceConnected) {
			return;
		}
		try {
			if (wakeupBinder != null)
				wakeupBinder.destroy();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		if (mContext != null)
			mContext.unbindService(conn);
		mContext = null;// 防止用户Context泄露
		mWakeupListener = null;
		authentication = null;
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			wakeupBinder = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			isServiceConnected = true;
			wakeupBinder = IWakeupService.Stub.asInterface(service);
			int logLevel = parseLogLevelParam();
			try {
				wakeupBinder.setLogLevel(logLevel);
				wakeupBinder.initWakupArd(mCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
				mWakeupListener.onError("ERR_USE_SERVICE_FAILED",
						VoiceWakeuperError.ERR_USE_SERVICE_FAILED);
				return;
			}
		}

		private int parseLogLevelParam() {
			int logLevel = 0;
			String logLevelStr = paramHash.get(SpeechConstant.LOG_LEVEL);
			if (logLevelStr != null) {
				try {
					logLevel = Integer.valueOf(logLevelStr);
					if (logLevel < 0 || logLevel > 5)
						logLevel = 0;
				} catch (Exception e) {
					logLevel = 0;
				}
			}

			if (DEBUG) {
				Log.d("", "-->LOG level:" + logLevel);
			}
			return logLevel;
		}
	};

	private final IWakeupCallback mCallback = new IWakeupCallback.Stub() {

		@Override
		public void onResult(String result) throws RemoteException {
			if(!TextUtils.isEmpty(result)){
				int res = authentication.handleUpdateTimesRequest();
				if(res == 0){
					Message errMsg = mHandler.obtainMessage(MSG_ON_RESULTS);
					errMsg.obj = result;
					errMsg.sendToTarget();
					if(DEBUG){
						Log.d("","-->update use times success");
					}
				}else{
					if(DEBUG){
						Log.e("","-->update use times fail");
					}
					
				}
			}else{
				// empty result
				Message errMsg = mHandler.obtainMessage(MSG_ON_RESULTS);
				errMsg.obj = result;
				errMsg.sendToTarget();
			}
			
		}

		@Override
		public void onError(String errorMsg, int errorCode)
				throws RemoteException {
			Message errMsg = mHandler.obtainMessage(MSG_ON_ERROR);
			errMsg.obj = errorMsg;
			errMsg.arg1 = errorCode;
			errMsg.sendToTarget();
		}

		@Override
		public void onBeginOfSpeech() throws RemoteException {
			if (mWakeupListener != null)
				mWakeupListener.onBeginOfSpeech();
		}
	};

	public boolean isServiceExist(Context mContext) {
		if (mContext == null) {
			return false;
		}

		String packageName = "com.sogou.speech.wakeup";
		try {
			PackageInfo mPackageInfo = mContext.getPackageManager()
					.getPackageInfo(packageName, 0);
			if (mPackageInfo != null) {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return false;
	}

	// add application package name comparison logic ,  to solve the problem of conflits
	// of  several service with same package name and class name ,
	// 2016-3-29
	public static Intent createExplicitFromImplicitIntent(Context context,
			Intent implicitIntent) {

		// if context is null, then return null Intent , 2016-3-29
		if(context == null){
			return null;
		}
		
		
		// Retrieve all services that can match the given intent
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent,
				0);

		// Make sure only one match was found
		// namely that the same package name of the service only appears once
//		if (resolveInfo == null || resolveInfo.size() != 1) {
		if (resolveInfo == null ) {
			return null;
		}

		String curPackageName = context.getPackageName();
//		Log.d("", "-->package name:"+context.getPackageName());
		// Get component info and create ComponentName
		for(int i=0;i<resolveInfo.size();i++){
//			Log.d("", "-->resolveInfo.get("+i+"):"+resolveInfo.get(i).serviceInfo.packageName+"/"+resolveInfo.get(i).serviceInfo.name);
			if(TextUtils.equals(curPackageName, resolveInfo.get(i).serviceInfo.packageName)){
				ResolveInfo serviceInfo = resolveInfo.get(i);
				String packageName = serviceInfo.serviceInfo.packageName;
				String className = serviceInfo.serviceInfo.name;
				ComponentName component = new ComponentName(packageName, className);
				// Create a new intent. Use the old one for extras and such reuse
				Intent explicitIntent = new Intent(implicitIntent);

				// Set the component to be explicit
				explicitIntent.setComponent(component);

				return explicitIntent;
			}
		}
		
		return null;
		
	}
	
	public static void enableSaveAudioToDisk(boolean flag){
		saveAudioToDisk = flag;
	}

}
