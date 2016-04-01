package com.sogou.speech.wakeup.wakeupservice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.sogou.speech.wakeup.ard;
import com.sogou.speech.wakeup.model.WakeUpResult;
import com.sogou.speech.wakeup.utils.CrashHandler;
import com.sogou.speech.wakeup.utils.FileOperator;
import com.sogou.speech.wakeup.utils.GlobalVars;
import com.sogou.speech.wakeup.utils.WakeupError;
import com.sogou.speech.wakeup.utils.WavUtil;

public class AsrTask implements Runnable{	
	public static final int MSG_ASR_WRITE_DATA_TO_DIST = 3;
	public static final int MSG_ASR_REC = 4;
	public static final int MSG_QUIT = 8;

	public static final String SAVE_BUNDLE_KEY_FOR_WORD = "WORD";
	public static final String SAVE_BUNDLE_KEY_FOR_FILEPATH = "FILEPATH";
	
	private static final short DEFAULT_FRAME_SIZE = 160;	
	private static final short MAX_CANDIDATE_NUM = 10;
	private static final short MAX_STR_SIZE = 1024;			
	private static final String TAG = "AsrTask";
	private static final String NO_RESULT = "";
	
	private final int oneMinute = 1000 * 60;  
	
	// remove final restriction on 2015-12-08
	private String LIB_PATH;
	private String DICT_FILE_PATH;
	private String HMM_BIN_PATH;
	private String PHN_BIN_PATH;
	private String CHN_BIN_PATH;

	private final ByteArrayOutputStream mWaveBuffer = new ByteArrayOutputStream();
	
	private Handler mLocalHandler;
	private Handler mMainProcessHandler;
	private AsrHandlerListener mAsrHandlerListener;
	private short[] tmpFrameData = new short[DEFAULT_FRAME_SIZE];
	private byte[][] mRecWord = new byte[MAX_CANDIDATE_NUM][MAX_STR_SIZE];	
	private List<String> wakeupWords;
	
	private final boolean DEBUG = false;
	
	@SuppressLint("NewApi")
	public AsrTask(AsrHandlerListener asrHandlerListener, Context cont, 
			Handler mainProcessHandler, List<String> words){
		
		if(ard.loadFromSysPath == false){
//			LIB_PATH = "/data/data/" + cont.getPackageName() + "/lib";
			LIB_PATH = cont.getApplicationInfo().nativeLibraryDir;
		}else{
			LIB_PATH = "/system/lib/";
		}
		
		DICT_FILE_PATH = LIB_PATH + GlobalVars.DICT_FILE_POST;
		HMM_BIN_PATH = LIB_PATH + GlobalVars.HMM_BIN_POST;
		PHN_BIN_PATH = LIB_PATH + GlobalVars.PHN_BIN_POST;
		CHN_BIN_PATH = LIB_PATH + GlobalVars.CHN_BIN_POST;

		mAsrHandlerListener = asrHandlerListener;
		mMainProcessHandler = mainProcessHandler;
		wakeupWords = words;
		
	}

	public boolean init(int logLevel){
		if(!asrInit(logLevel)){
			ard.ASREnd(null);
			return false;
		}
		
		return true;
	}
	
	private boolean asrInit(int logLevel) {
		boolean isInitSuccess = false;
		
		ard.ASRLogLevelSet(logLevel);
		int ret = ard.ASRMemSet();
		if(ret != ard.ASR_OK) {
			sendErrorMsg(WakeupError.ERROR_ASR_MEMSET);
			return isInitSuccess;
		}
		
		ret = ard.ASRInit(DICT_FILE_PATH, HMM_BIN_PATH, PHN_BIN_PATH, CHN_BIN_PATH);
		if (ret != ard.ASR_OK) {
			// try another directory, 2015-12-08
			if (ard.loadFromSysPath == false) {
				LIB_PATH = "/system/lib/";
				DICT_FILE_PATH = LIB_PATH + GlobalVars.DICT_FILE_POST;
				HMM_BIN_PATH = LIB_PATH + GlobalVars.HMM_BIN_POST;
				PHN_BIN_PATH = LIB_PATH + GlobalVars.PHN_BIN_POST;
				CHN_BIN_PATH = LIB_PATH + GlobalVars.CHN_BIN_POST;
				ret = ard.ASRInit(DICT_FILE_PATH, HMM_BIN_PATH, PHN_BIN_PATH, CHN_BIN_PATH);
				if (ret != ard.ASR_OK) {
					sendErrorMsg(WakeupError.ERROR_ASR_INIT);
					return isInitSuccess;
				}
			} else {
				sendErrorMsg(WakeupError.ERROR_ASR_INIT);
				return isInitSuccess;
			}
		}
		
		if(!addWakeupWord()){ 
			sendErrorMsg(WakeupError.ERROR_ASR_UWORD_ADD);
			return isInitSuccess;
		}
				
		ret = ard.ASRBuild();
		if(ret != ard.ASR_OK) {
			sendErrorMsg(WakeupError.ERROR_ASR_BUILD);
			return isInitSuccess;
		}
		
		ret = ard.ASRStart();
		if(ret != ard.ASR_OK) {
			sendErrorMsg(WakeupError.ERROR_ASR_START);
			return isInitSuccess;
		}
		
		isInitSuccess = true;
		return isInitSuccess;
	}

	private boolean addWakeupWord() {
		for(String word : wakeupWords){
			int ret;
			byte[] wakeUpByteArr = null;
			try {
				wakeUpByteArr = word.getBytes("GBK");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			ret = ard.ASRWUWordAdd(wakeUpByteArr);		
			if(ret != ard.ASR_OK) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void run() {
		Looper.prepare();
        mLocalHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {                           	
                	switch (msg.what) {
       				case MSG_ASR_REC:
       					short[] rawData = (short[])msg.obj;
       					writeToWaveBuffer(rawData);
       					WakeUpResult ret = recRawDate(rawData); 
       					break;
       					
       				case MSG_ASR_WRITE_DATA_TO_DIST:
       					if(msg.obj != null){
       						Bundle bundle = (Bundle)msg.obj;
       						String filePath = bundle.getString(SAVE_BUNDLE_KEY_FOR_FILEPATH);
       						String word = bundle.getString(SAVE_BUNDLE_KEY_FOR_WORD);
       						if(filePath == null || word == null){
       							
       							sendErrorMsg(WakeupError.ERROR_ASR_ILLEGAL_ARGUMENT);
       						}else{
       							writeDataToDist(filePath, word);
       						}
       							
       						
       					}          						
       					break;
       					
       				case MSG_QUIT:       	      			       					
       		            Looper.myLooper().quit();       		            
//       		            ard.ASREnd(null);
       		            ard.ASREnd("");
       		            mLocalHandler = null;
       		            mWaveBuffer.reset();
       		            break;
	       			}              	                  	           	
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

			private void writeToWaveBuffer(short[] rawData) {
				for (short singlebuf : rawData) {
					try {
				    	// if (bo.equals(ByteOrder.BIG_ENDIAN)) {
				        mWaveBuffer.write((byte) (singlebuf & 0x00ff));
				        mWaveBuffer.write((byte) ((singlebuf >> 8) & 0x00ff));
				        // } else {
				        // mWaveBuffer.write((byte)((singlebuf >> 8) & 0x00ff));
				        // mWaveBuffer.write((byte)(singlebuf & 0x00ff));
				        // }
				    } catch (Exception e) {
				    }
				}
			}
			
			private void writeDataToDist(final String filePath, final String word) {
				if(DEBUG){
					Log.d("","-->asr task,writeDataToDist filepath:"+filePath+",word:"+word);
				}
				final byte[] byteData0 = mWaveBuffer.toByteArray();
                mWaveBuffer.reset();
				new Thread() {
				    public void run() {
				    	boolean isRight = false;
				    	for(String word : wakeupWords){
					    	if(word.contains(word))
					    		isRight = true;
				    	}
				        saveDataToSDCardForDebug(filePath, byteData0, isRight);
				    };
				}.start();
			}

			private WakeUpResult recRawDate(short[] rawData) {
				WakeUpResult ret = null;
				
				final int dataLength = rawData.length;
				for(int processedLen = 0, cnt = 0; processedLen < dataLength; 
						processedLen += DEFAULT_FRAME_SIZE, cnt++){
					System.arraycopy(rawData, processedLen, tmpFrameData, 0,
							DEFAULT_FRAME_SIZE);
					final int recRet = ard.ASRRec(tmpFrameData, DEFAULT_FRAME_SIZE);
					if(recRet > 0){
						short wordIndex = 0;
						ard.ASRResultGet(mRecWord, MAX_CANDIDATE_NUM);
						String word = null;
						try {
							word = (new String(mRecWord[wordIndex], "GBK")).trim();
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						float confid = ard.ASRConfGet(wordIndex);
						float like = ard.ASRLikeGet(wordIndex);
						WakeUpResult result = new WakeUpResult(word, confid, like);			
						if(DEBUG){
							Log.d("", "-->INITIAL RESULT:"+(result==null?"":result.toString()));
						}
						//InfolineElement element = gson.fromJson(jsonStr, InfolineElement.class);
						sentResultToMainProcess(result.toString());	
						ret = result;
					}
					else if(recRet == ard.ASR_REC_WR){
						throw new RuntimeException("ASR_REC_WR Error!");
					}
					else if(recRet == ard.ASR_EPD_WR || recRet == ard.ASR_NO_RES){
						sentResultToMainProcess(NO_RESULT);
						mWaveBuffer.reset();
					}
					else if(recRet < 0){
//						if(recRet != ard.ASR_EPD_WR && recRet != ard.ASR_NO_RES)
//							Log.e(TAG, recRet+"");
					}
				}
				
				return ret;
			}

			private void sentResultToMainProcess(String word) {
				if(DEBUG){
					Log.d("", "-->asr task, result:"+word);
				}
				Message msg = mMainProcessHandler
				        .obtainMessage(WakeupService.MSG_RECOG_RESULT);
				msg.obj = word;
				msg.sendToTarget(); 
			}
        };
        
        mAsrHandlerListener.setAsrHandler(mLocalHandler);
        mLocalHandler.postDelayed(resetWaveBufferTimerTask, oneMinute); 
        Looper.loop();      
	}
	
	/**
	 * 定期清除录音缓存
	 */
	Runnable resetWaveBufferTimerTask = new Runnable() {  		 
	    @Override  
	    public void run() {  
	       try {  
	    	   mWaveBuffer.reset();
	    	   if(mLocalHandler != null)
	    		   mLocalHandler.postDelayed(this, oneMinute);     
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	    }  
	};  

	private void sendErrorMsg(int errorCode){	
		CrashHandler.logAndSendErrorMsg(errorCode, mMainProcessHandler);	
	}
	
	// add flag for whether it is successful, 2013-08-19
    public void saveDataToSDCardForDebug(String filePath, byte[] byteData, boolean isSuccessful) {
        FileOutputStream fos = null;
        String suf = null;
        if (isSuccessful == true) {
            suf = "right_";
        } else {
            suf = "wrong_";
        }
        FileOperator.createDirectory(filePath, true, false);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String currentTime = formatter.format(new Date());
        
        String filename = filePath +File.separator+ suf + currentTime + ".pcm";
//        Log.e(TAG, filename);
        if(DEBUG){
        	Log.d("", "-->stored file name:"+filename);
        }
        
        File outputFile = new File(filename);
        try {
            if (!outputFile.exists()) {
                boolean createNewFile = outputFile.createNewFile();
                fos = new FileOutputStream(outputFile);
                if(DEBUG){
                	Log.d("","-->create new file success:"+createNewFile);
                }
            } else {
                if (outputFile.isFile()) {
                    FileOperator.deleteFile(outputFile);
                } else {
                    FileOperator.deleteDir(outputFile);
                }
                boolean createNewFile = outputFile.createNewFile();
                fos = new FileOutputStream(outputFile);
                
                if(DEBUG){
                	Log.d("","-->create new file success:"+createNewFile);
                }
            }
//            WavUtil.constructWav(fos, ByteOrder.nativeOrder(), byteData);
            // construct wave

            
            fos.write(byteData,0,byteData.length);
//            WavUtil.addWavHeadChars(fos, "RIFF".toCharArray());
//            int file_size0 = byteData.length + 44 - 8;
//            WavUtil.addWavHeadInt(fos, ByteOrder.nativeOrder(), file_size0);
//            WavUtil.addWavHeadChars(fos, "WAVEfmt".toCharArray());
//            WavUtil.addWavHeadByte(fos, (byte) 0x20);
//            WavUtil.addWavHeadInt(fos, ByteOrder.nativeOrder(), 0x10);
//            WavUtil.addWavHeadShort(fos, ByteOrder.nativeOrder(), (short) 0x01);
//            WavUtil.addWavHeadShort(fos, ByteOrder.nativeOrder(), (short) 1);
//            WavUtil.addWavHeadInt(fos, ByteOrder.nativeOrder(), 16000);
//            WavUtil.addWavHeadInt(fos, ByteOrder.nativeOrder(), 32000);
//            WavUtil.addWavHeadShort(fos, ByteOrder.nativeOrder(), (short) 2);
//            WavUtil.addWavHeadShort(fos, ByteOrder.nativeOrder(), (short) 16);
//            WavUtil.addWavHeadChars(fos, "data".toCharArray());
//            WavUtil.addWavHeadInt(fos, ByteOrder.nativeOrder(), byteData.length);
//            
//            int i = 0;
//            for (byte b : byteData) {
//                i++;
//                WavUtil.addWavHeadByte(fos, b);
//            }
            // Log.d(TAG,"pcm length ============================================================================ " + i);
        
            
            
//            fos.flush();
//            fos.close();
        } catch (Exception e) {
           if(DEBUG){
        	   Log.e("", "--> exception in writing data to disk:"+e.toString());
           }
            
        }finally{
        	if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e1) {
                	 if(DEBUG){
                  	   Log.e("", "--> exception in writing data to disk:"+e1.toString());
                     }
                }
                fos = null;
            }
        }
    }
}
