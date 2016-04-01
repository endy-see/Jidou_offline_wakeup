package com.sogou.speech.wakeupclient;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sogou.speech.view.Title;
import com.sogou.speech.wakeup.utils.SpeechConstant;
import com.sogou.speech.wakeup.wakeuper.VoiceWakeuper;
import com.sogou.speech.wakeup.wakeuper.WakeupListener;

import org.json.JSONException;
import org.json.JSONObject;

public class WakeupClientActivity extends Activity {
	private VoiceWakeuper mVoiceWakeuper;
	private Button startBtn, stopBtn;
	private RelativeLayout resetLayout;
	private TextView resultTV;
	private EditText wakeupWordEditText;
	private Title title;
	private boolean isStartListening = false;

	private String appId = "FOGY2207";
	private String accessKey = "zXUY5bA4";

	private final static String FIRST_WAKEUP_WORD = "你好搜狗";
	private static Context context = null;

	private final boolean DEBUG = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.wakeup_activity);
		startBtn = (Button) findViewById(R.id.startTextView);
		stopBtn = (Button) findViewById(R.id.stopTextView);
		resultTV = (TextView) findViewById(R.id.result_tv);
		resetLayout = (RelativeLayout) findViewById(R.id.resetLayout);
		wakeupWordEditText = (EditText) findViewById(R.id.wakeupWordEditText);
		context = this;
		title = (Title) findViewById(R.id.title);

		title.titleTV.setText("搜狗语音唤醒");

		resetUI();

		// 开启录音功能
		VoiceWakeuper.enableSaveAudioToDisk(true);
		// 初始化VoiceWakeuper对象
		mVoiceWakeuper = new VoiceWakeuper(getApplicationContext(), appId,
				accessKey);
		// 清空参数
		mVoiceWakeuper.setParameter(SpeechConstant.PARAMS, null);
		// 设置日志等级,1-5表示安卓不同级别的log输出</br> 0:不输出;1:debug;5:error
		mVoiceWakeuper.setParameter(SpeechConstant.LOG_LEVEL, "0");
		// 初始化唤醒服务
		int res = mVoiceWakeuper.initializeService();
		if (res == -2) {
			Toast.makeText(this, "bind wakeup service failed",
					Toast.LENGTH_LONG).show();
			return;
		} else if (res == 0) {
			// Toast.makeText(this, "wakeup service exists",
			// Toast.LENGTH_LONG).show();
		}
		// 增加唤醒词
		mVoiceWakeuper.addWakeupWord(FIRST_WAKEUP_WORD);

		startBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Object mLock = new Object();

				if (!isStartListening) {
					// 开始识别
					mVoiceWakeuper.startListening(wakeupListener);
					initStartUI();
				}
			}
		});

		// 设置唤醒词，新设的唤醒词将覆盖旧的唤醒词
		stopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 停止识别
				mVoiceWakeuper.stopListening();
				if (isStartListening) {
					resetUI();
				}
			}
		});

		resetLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String newWakeupWord = wakeupWordEditText.getText().toString();

				if (TextUtils.isEmpty(newWakeupWord)) {
					showToast("唤醒词不能为空");
					return;
				}

				if (DEBUG) {
					Log.d("", "-->new wakeup word:" + newWakeupWord);
				}

				mVoiceWakeuper.stopListening();
				resetUI();
				// if(isStartListening){
				// resetUI();
				// }

				mVoiceWakeuper.destroy();
				mVoiceWakeuper = null;

				mVoiceWakeuper = new VoiceWakeuper(getApplicationContext(),
						appId, accessKey);
				mVoiceWakeuper.initializeService();
				mVoiceWakeuper.setParameter(SpeechConstant.PARAMS, null);
				mVoiceWakeuper.setParameter(SpeechConstant.LOG_LEVEL, "0");

				mVoiceWakeuper.addWakeupWord(newWakeupWord);
				showToast("设定唤醒词成功");
				// String secondWord = wakeupWordEditText.getText()+"";

			}

		});

		//

	}

	void showToast(String str) {
		Toast.makeText(WakeupClientActivity.this, str, Toast.LENGTH_SHORT)
				.show();
	}

	private void initStartUI() {
		startBtn.setEnabled(false);
		stopBtn.setEnabled(true);
		isStartListening = true;
		resultTV.setText("");
	}

	private void resetUI() {
		startBtn.setEnabled(true);
		stopBtn.setEnabled(false);
		isStartListening = false;
	}

	WakeupListener wakeupListener = new WakeupListener() {

		@Override
		public void onResult(String res, boolean accept) {
			// if(DEBUG){
			// Log.d("", "-->accept recognition result:"+accept);
			// }
			String resultString = null;
			if (TextUtils.isEmpty(res)) {
				StringBuffer buffer = new StringBuffer();
				buffer.append("【RAW】空\n【识别结果】空\n【似然度】\n【置信度】\n【拒绝】\n");
				resultString = buffer.toString();
			} else {
				try {
					JSONObject object;
					object = new JSONObject(res);
					StringBuffer buffer = new StringBuffer();
					buffer.append("【RAW】 " + res);
					buffer.append("\n");
					buffer.append("【识别结果】" + object.optString("word"));
					buffer.append("\n");
					buffer.append("【似然度】" + object.optString("like"));
					buffer.append("\n");
					buffer.append("【置信度】" + object.optString("confid"));
					buffer.append("\n");
					buffer.append(Double.parseDouble(object.optString("confid")) >= 0.5 ? "【接受】"
							: "【拒绝】");
					buffer.append("\n");
					resultString = buffer.toString();
				} catch (JSONException e) {
					// resultString = "结果解析出错";
					resultString = e.toString();
					Log.e("", "--> json exception:" + resultString);
				}
			}

			resultTV.setText(resultString);
		}

		@Override
		public void onError(String errorMsg, int errorCode) {
			switch (errorCode) {
			case -306:
				resultTV.setText("预留次数已用完，请重新启动监听以认证获取更多使用次数");
				stopBtn.performClick();
				break;
			case -312:
				resultTV.setText("可用次数已用完，请重启监听以获得更多使用次数");
				stopBtn.performClick();
				break;
			// add on 2015-12-15
			case -100:
				resultTV.setText("唤醒初始化失败\n请检查麦克风是否被其他程序占用\n请检查录音权限是否被禁用");
				stopBtn.performClick();

			default:
				Toast.makeText(WakeupClientActivity.this,
						"errorMsg:" + errorMsg + " errorCode:" + errorCode,
						Toast.LENGTH_LONG).show();
				break;
			}

		}

		@Override
		public void onBeginOfSpeech() {
			Toast.makeText(WakeupClientActivity.this, "开始说话",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onPassedValidation() {
			// 通过认证， 可以开始唤醒操作
			// if(DEBUG){
			// Log.d("","--> pass validation");
			// }
		}

	};

	@Override
	public void onDestroy() {
		super.onDestroy();

		// 销毁全部资源
		mVoiceWakeuper.destroy();
		mVoiceWakeuper = null;
	}

}
