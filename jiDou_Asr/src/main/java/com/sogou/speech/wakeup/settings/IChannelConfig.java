package com.sogou.speech.wakeup.settings;

import android.media.AudioFormat;

/**
 * an interface of channel type
 * 
 * @author xiantao
 * @since 2012-08-11
 */
public interface IChannelConfig {
	public static final int MONO = AudioFormat.CHANNEL_IN_MONO;
	public static final int STEREO = AudioFormat.CHANNEL_IN_STEREO;
	public static final int OUT_MONO = AudioFormat.CHANNEL_OUT_MONO;
	public static final int OUT_STEREO = AudioFormat.CHANNEL_OUT_STEREO;
}
