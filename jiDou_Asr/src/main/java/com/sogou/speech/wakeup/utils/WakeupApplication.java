/**
 * 
 */
package com.sogou.speech.wakeup.utils;

import android.app.Application;

/**
 * 
 * @author liukeang
 *
 */
public class WakeupApplication extends Application {
	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		// init CrashHandler, put getApplicationContext() into mContext
		CrashHandler.getInstance().init(getApplicationContext());
		
	}
}
