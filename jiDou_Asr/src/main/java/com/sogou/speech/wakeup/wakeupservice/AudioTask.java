package com.sogou.speech.wakeup.wakeupservice;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.sogou.speech.wakeup.settings.IChannelConfig;
import com.sogou.speech.wakeup.settings.INetworkType;
import com.sogou.speech.wakeup.settings.ISampleRate;
import com.sogou.speech.wakeup.settings.ISettingUtils;
import com.sogou.speech.wakeup.utils.CrashHandler;
import com.sogou.speech.wakeup.utils.WakeupError;

public class AudioTask implements Runnable, INetworkType, ISampleRate,
		IChannelConfig, ISettingUtils {

	private static final String TAG = "AudioTask";

	private int channelConfig = MONO;
	private int sampleRate = DEFAULT_HIGH_AUDIO_SAMPLE_RATE;
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

	// add static modifier 2015-11-27
	private static AudioRecord audioRecord;
	private int minBufferSize;
	private short[] audioBuffer;
	private int curWavSize;
	private Handler mMainProcessHandler;

	// add ConnectivityManager, 2013-04-12
	private boolean mRecorderAvailable = false;

	// add useStereo flag, 2014-07-31
	private boolean useStereo = false;

	private boolean isStopRecord = false;

	/**
	 * AudioTask Construct function
	 */
	public AudioTask(boolean isMonoChannel, Handler handler) {
		super();
		mMainProcessHandler = handler;

		// set channel type
		if (false == isMonoChannel) {
			// what will happen when STEREO is unavailable???
			this.channelConfig = STEREO;
		}

		// change position for calling prepareRecorder() and do not allow to
		// change sampleRate automatically, 2015-08-31

		// }
	}

	public void stopRecord() {
		isStopRecord = true;
	}

	public void resetRecord() {
		isStopRecord = false;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public Handler getmMainProcessHandler() {
		return mMainProcessHandler;
	}

	public void setmMainProcessHandler(Handler mMainProcessHandler) {
		this.mMainProcessHandler = mMainProcessHandler;
	}

	// add by meixiao, 2012-10-11
	// add synchronized and call stop when necessary, 2014-01-23
	// remove synchronized modifier , 2015-11-27
	public void releaseAudioRecorder() {
		if (audioRecord != null) {
			try {
				if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
					audioRecord.stop();
				}
				
			} catch (IllegalStateException ex) {
				ex.printStackTrace();
			}finally{
				audioRecord.release();
				audioRecord = null;
			}

			
		}
	}

	private boolean prepareRecorderForSpecialPhone() {

		useStereo = false;

		releaseAudioRecorder();

		this.channelConfig = MONO;
		minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
				this.channelConfig, this.audioFormat);

		if (minBufferSize <= 0) {
			sendErrorMsg(WakeupError.ERROR_AUDIO_INITIALIZE_FAIL);
			return false;
		}

		if (minBufferSize < MIN_BUFFER) {
			minBufferSize = MIN_BUFFER;
		}

		audioBuffer = new short[minBufferSize / 2];

		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
				sampleRate, MONO, this.audioFormat, this.minBufferSize);

		if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
			sendErrorMsg(WakeupError.ERROR_AUDIO_INITIALIZE_FAIL);
			return false;
		}

		try {
			audioRecord.startRecording();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			sendErrorMsg(WakeupError.ERROR_AUDIO_START_FAIL);
			releaseAudioRecorder();
			return false;
		}

		return true;
	}

	/**
	 * Handler with constructor function for initialization
	 */
	private boolean prepareRecorder() {
		releaseAudioRecorder();
		boolean res = true;
		if (sampleRate == 0) {
			return false;
		}

		try {

			// check whether minBufferSize is adapted to the sample rate
			// try to use STEREO at first, 2014-07-31
			if (this.channelConfig == MONO) {
				minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
						STEREO, this.audioFormat);
				if (minBufferSize <= 0) {
					// use MONO
					minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
							this.channelConfig, this.audioFormat);
				} else {
					useStereo = true;
				}
			} else {// getMinBufferSize for STEREO, 2014-08-01
				minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
						this.channelConfig, this.audioFormat);
			}

			if (minBufferSize <= 0) {
				if (sampleRate == DEFAULT_HIGH_AUDIO_SAMPLE_RATE) {
					// set audioFormat consistent to sampleRate, 2014-01-23
					// keep audioFormat ENCODING_PCM_16BIT, 2014-04-18
					// audioFormat = AudioFormat.ENCODING_PCM_8BIT;
					sampleRate = DEFAULT_LOW_AUDIO_SAMPLE_RATE;
					// minBufferSize is amount of bytes
					minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
							this.channelConfig, audioFormat);
					if (minBufferSize <= 0) {
						return false;
					}
				} else {
					return false;
				}
			}

			// set minBufferSize to MIN_BUFFER when it is less than it
			if (minBufferSize < MIN_BUFFER) {
				minBufferSize = MIN_BUFFER;
			}

			// check whether AudioRecord is adapted to the sample rate
			// try to use STEREO at first, 2014-07-31
			if (useStereo == true) {
				// enlarge minBufferSize for STEREO, 2014-11-20
				audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
						sampleRate, STEREO, this.audioFormat,
						this.minBufferSize * 2);
				// judge whether audioRecord is null, 2014-08-01
				if (audioRecord == null
						|| audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
					// call releaseAudioRecorder at first, 2014-08-01
					releaseAudioRecorder();
					audioRecord = new AudioRecord(
							MediaRecorder.AudioSource.MIC, sampleRate,
							this.channelConfig, this.audioFormat,
							this.minBufferSize);
					useStereo = false;
				}
			} else {// new AudioRecord when useStereo is false, 2014-08-01
				audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
						sampleRate, this.channelConfig, this.audioFormat,
						this.minBufferSize);
			}

			// judge whether audioRecord is null, 2014-08-01
			if (audioRecord == null
					|| audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
				if (sampleRate == DEFAULT_HIGH_AUDIO_SAMPLE_RATE) {
					// set audioFormat consistent to sampleRate and call
					// releaseAudioRecorder, 2014-01-23
					// keep audioFormat ENCODING_PCM_16BIT, 2014-04-18
					// audioFormat = AudioFormat.ENCODING_PCM_8BIT;
					releaseAudioRecorder();
					sampleRate = DEFAULT_LOW_AUDIO_SAMPLE_RATE;
					audioRecord = new AudioRecord(
							MediaRecorder.AudioSource.MIC, sampleRate,
							this.channelConfig, this.audioFormat,
							this.minBufferSize);
					if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
						return false;
					}
				} else {
					return false;
				}
			}

			// enlarge audioBuffer for STEREO, 2014-11-20
			if (useStereo == true) {
				audioBuffer = new short[minBufferSize];
			} else {
				audioBuffer = new short[minBufferSize / 2];
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			res = false;
		}
		return res;
	}

	/**
	 * start AudioTask thread
	 */
	public void run() {

		/* keep all operations concerning AudioTask in this class, to avoid
		 multi-thread problems. 2015-11-27 */
		synchronized (AudioTask.class) {
			// change whether it is Samsung I9103, 2015-01-11
			if ((Build.MODEL).equals("GT-I9103")) {
				mRecorderAvailable = prepareRecorderForSpecialPhone();
			} else {
				mRecorderAvailable = prepareRecorder();
			}

			if (!mRecorderAvailable && mMainProcessHandler != null) {
				sendErrorMsg(WakeupError.ERROR_AUDIO_INITIALIZE_FAIL);
				releaseAudioRecorder();
				return;
			}

			// store the length of current audio data
			int tmpTotalSize = 0;
			try {
				audioRecord.startRecording();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				sendErrorMsg(WakeupError.ERROR_AUDIO_START_FAIL);
				// call releaseAudioRecorder and return, 2014-01-23
				releaseAudioRecorder();
				return;
			}

			while (!isStopRecord && audioBuffer != null) {
				curWavSize = audioRecord.read(audioBuffer, 0,
						audioBuffer.length);

				// check whether audioBuffer is null, 2013-12-06
				if (audioBuffer != null && curWavSize > 0
						&& curWavSize <= audioBuffer.length) {

					// tmpWavData: store the real time audio data
					// feth left channel aduio when useStereo is true,
					// 2014-07-31
					short[] tmpWavData;
					if (useStereo == true) {
						tmpWavData = new short[curWavSize / 2];
						int i = 0;
						int j = 0;
						for (i = 0; i < curWavSize; i += 2) {
							// set aduio value to mean of two channels,
							// 2014-12-04
							tmpWavData[j] = (short) ((audioBuffer[i] + audioBuffer[i + 1]) / 2);
							j++;
						}
					} else {
						tmpWavData = new short[curWavSize];
						System.arraycopy(audioBuffer, 0, tmpWavData, 0,
								curWavSize);
					}

					// check whether mMainProcessHandler is null, 2013-12-06
					if (mMainProcessHandler == null) {
						break;
					}

					Message msg = mMainProcessHandler
							.obtainMessage(WakeupService.MSG_RECEIVE_RAW_VOICE);
					msg.obj = tmpWavData;
					msg.sendToTarget();

					// add half of curWavSize when useStereo is true, 2014-07-31
					if (useStereo == true) {
						tmpTotalSize += curWavSize / 2;
					} else {
						tmpTotalSize += curWavSize;
					}

				} else {
					sendErrorMsg(WakeupError.ERROR_AUDIO_INITIALIZE_FAIL);
					break;
				}
			}
			// replace destroy with releaseAudioRecorder, 2015-08-31
			releaseAudioRecorder();
			// destory();
			mMainProcessHandler = null;
		}

	}

	// set it public, 2013-12-06
	public void destory() {
		// move here, 2013-12-06

		// remove releaseAudioRecorder(), in order to forbid other threads to
		// call it, 2015-08-31
		// releaseAudioRecorder();

	}

	private void sendErrorMsg(int errorCode) {
		CrashHandler.logAndSendErrorMsg(errorCode, mMainProcessHandler);
	}
}
