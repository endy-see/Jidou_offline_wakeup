package com.sogou.speech.wakeup.wakeupservice;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.util.Log;

import com.sogou.speech.wakeup.utils.CrashHandler;
import com.sogou.speech.wakeup.utils.WakeupError;
import com.sogou.speech.wakeup.wakeupservice.IWakeupCallback;
import com.sogou.speech.wakeup.wakeupservice.IWakeupService;

public class WakeupService extends Service implements AsrHandlerListener {
	public static final int MSG_ERROR = 3;
	public static final int MSG_RECEIVE_RAW_VOICE = 4;
	public static final int MSG_RECOG_RESULT = 5;

	private AudioTask audioTask;
	private AsrTask asrTask;
	private Handler asrHandler;
	private final String TAG = "com.sogou.speech.service.AsrService";
	private IWakeupCallback mCallBack;
	private final boolean DEBUG = false;

	private WakeLock wakeLock = null;

	private Handler mainProcessHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_RECEIVE_RAW_VOICE:
				if (msg.obj != null)
					handleReceiveRawData((short[]) msg.obj);
				break;

			case MSG_RECOG_RESULT:
				if (msg.obj != null){
					handleRecResult((String) msg.obj);
				}else{
					handleRecResult("");
				}
					
				break;

			case MSG_ERROR:
				WakeupError we = (WakeupError) msg.obj;
				try {
					mCallBack.onError(we.getMsg(), we.getCode());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	};

	private void handleRecResult(String result) {

		try {
			if (mCallBack != null)
				if (DEBUG) {
					Log.d("", "-->recognize result:" + result);
				}
			mCallBack.onResult(result);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopRecordAndAsrThread();
	}

	private void stopRecordAndAsrThread() {
		stopRecord();

		if (asrHandler != null) {
			Message asrStopMsg = asrHandler.obtainMessage(AsrTask.MSG_QUIT);
			asrStopMsg.sendToTarget();
			asrHandler = null;
		}
	}

	private void stopRecord() {
		if (audioTask != null) {
			audioTask.stopRecord();
			audioTask = null;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void setAsrHandler(Handler handler) {
		asrHandler = handler;
	}

	private void handleReceiveRawData(short[] rawData) {
		if (asrHandler != null) {
			Message asrRecMsg = asrHandler.obtainMessage(AsrTask.MSG_ASR_REC);
			asrRecMsg.obj = rawData;
			asrRecMsg.sendToTarget();
		}
	}

	private final IWakeupService.Stub mBinder = new IWakeupService.Stub() {
		int logLevel = 0;

		@Override
		public void setLogLevel(int level) throws RemoteException {
			if (level < 0 || logLevel > 5)
				return;
			logLevel = level;
		}

		@Override
		public void initWakupArd(IWakeupCallback callBack)
				throws RemoteException {
			mCallBack = callBack;
		}

		@Override
		public void startListening(List<String> words) throws RemoteException {
			if (words.size() == 0) {
				WakeupError we = new WakeupError(
						WakeupError.ERROR_ASR_NO_WAKE_UP_WORD);
				mCallBack.onError(we.getMsg(), we.getCode());
				return;
			}

			if (asrTask == null) {
				asrTask = new AsrTask(WakeupService.this,
						WakeupService.this.getApplicationContext(),
						mainProcessHandler, words);
				if (!asrTask.init(logLevel)) {
					asrTask = null;
					return;
				}
				new Thread(asrTask).start();
			}

			if (audioTask == null) {
				audioTask = new AudioTask(true, mainProcessHandler);
				new Thread(audioTask).start();
			}

			mCallBack.onBeginOfSpeech();
			acquireWakeLock();
		}

		@Override
		public void stopListening() throws RemoteException {
			stopRecord();
			releaseWakeLock();
		}

		@Override
		public void saveRawDataToDisk(String filePath, String word)
				throws RemoteException {
			if(DEBUG){
				Log.d("","--> saveRawDataToDisk in service:"+filePath+",word:"+word);
			}
			Message asrSaveToDistMsg = asrHandler
					.obtainMessage(AsrTask.MSG_ASR_WRITE_DATA_TO_DIST);
			Bundle bundle = new Bundle();
			bundle.putString(AsrTask.SAVE_BUNDLE_KEY_FOR_FILEPATH, filePath);
			bundle.putString(AsrTask.SAVE_BUNDLE_KEY_FOR_WORD, word);
			asrSaveToDistMsg.obj = bundle;
			asrSaveToDistMsg.sendToTarget();
		}

		@Override
		public void setErrorLogPath(String path) throws RemoteException {
			CrashHandler.getInstance().setErrorLogPath(path);
		}

		@Override
		public void destroy() throws RemoteException {
			stopRecordAndAsrThread();
		}
	};

	/**
	 * 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
	 */
	private void acquireWakeLock() {
		if (null == wakeLock) {
			PowerManager pm = (PowerManager) this
					.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, "PostLocationService");
			if (null != wakeLock) {
				wakeLock.acquire();
			}
		}
	}

	/**
	 * 释放设备电源锁
	 */
	private void releaseWakeLock() {
		if (null != wakeLock) {
			wakeLock.release();
			wakeLock = null;
		}
	}
}