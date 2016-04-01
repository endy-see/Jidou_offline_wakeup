package com.sogou.speech.wakeup.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

import android.util.Log;

public class WavUtil {
    private static final String TAG = "WavUtil";

    public static void constructWav(OutputStream os, ByteOrder bo,
            byte[] byteData) throws IOException {
        WavUtil.addWavHeadChars(os, "RIFF".toCharArray());
        int file_size0 = byteData.length + 44 - 8;
        WavUtil.addWavHeadInt(os, bo, file_size0);
        WavUtil.addWavHeadChars(os, "WAVEfmt".toCharArray());
        WavUtil.addWavHeadByte(os, (byte) 0x20);
        WavUtil.addWavHeadInt(os, bo, 0x10);
        WavUtil.addWavHeadShort(os, bo, (short) 0x01);
        WavUtil.addWavHeadShort(os, bo, (short) 1);
        WavUtil.addWavHeadInt(os, bo, 16000);
        WavUtil.addWavHeadInt(os, bo, 32000);
        WavUtil.addWavHeadShort(os, bo, (short) 2);
        WavUtil.addWavHeadShort(os, bo, (short) 16);
        WavUtil.addWavHeadChars(os, "data".toCharArray());
        WavUtil.addWavHeadInt(os, bo, byteData.length);
        
        int i = 0;
        for (byte b : byteData) {
            i++;
            WavUtil.addWavHeadByte(os, b);
        }
        // Log.d(TAG,"pcm length ============================================================================ " + i);
    }

    public static void addWavHeadInt(OutputStream os, ByteOrder bo, int addone)
            throws IOException {
//        if (bo.equals(ByteOrder.BIG_ENDIAN)) {
            os.write((addone >> 0) & 0x000000ff);
            os.write((addone >> 8) & 0x000000ff);
            os.write((addone >> 16) & 0x000000ff);
            os.write((addone >> 24) & 0x000000ff);
//        } else {
//            os.write((addone >> 24) & 0x000000ff);
//            os.write((addone >> 16) & 0x000000ff);
//            os.write((addone >> 8) & 0x000000ff);
//            os.write((addone >> 0) & 0x000000ff);
//        }
    }

    public static void addWavHeadByte(OutputStream os, byte addone)
            throws IOException {
        os.write(addone);
    }

    public static void addWavHeadChar(OutputStream os, char addone)
            throws IOException {
        os.write(addone);
    }

    public static void addWavHeadChars(OutputStream os, char[] addone)
            throws IOException {
        for (char c : addone) {
            os.write(c);
        }
    }

    public static void addWavHeadShort(OutputStream os, ByteOrder bo,
            short addone) throws IOException {
//        if (bo.equals(ByteOrder.BIG_ENDIAN)) {
            os.write((addone >> 0) & 0x000000ff);
            os.write((addone >> 8) & 0x000000ff);
//        } else {
//            os.write((addone >> 8) & 0x000000ff);
//            os.write((addone >> 0) & 0x000000ff);
//        }
    }
}
