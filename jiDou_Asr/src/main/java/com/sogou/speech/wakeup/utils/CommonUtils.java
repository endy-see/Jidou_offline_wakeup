package com.sogou.speech.wakeup.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.zip.GZIPOutputStream;

import android.media.AudioTrack;

import com.sogou.speech.wakeup.settings.IChannelConfig;
import com.sogou.speech.wakeup.settings.ISampleRate;
import com.sogou.speech.wakeup.settings.ISettingUtils;

public final class CommonUtils implements ISettingUtils, ISampleRate,
        IChannelConfig {

    public static String sNetworkAudioErrFilePath = null;
    private static AudioTrack audioTrack = null;

    public static void setDirPathForNetworkAudioErrFile(String dirPath,
            String packageName) {
        if (dirPath == null)
            return;
        FileOperator.createDirectory(dirPath, true, false);
        sNetworkAudioErrFilePath = dirPath + "/" + packageName
                + "_network_audio_err.gz";
    }

    // the functions below is used
    private static void writeShorts(short[] shorts, String filename) {
        File file = openFile(filename);
        try {
            // set second variant true means add data at end
            FileOutputStream fos = new FileOutputStream(file, true);
            for (short aShort : shorts) {
                fos.write(shortToByte(aShort));
            }
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }

    private static void writeBytes(byte[] bytes, String filename) {
        File file = openFile(filename);
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            for (short aByte : bytes) {
                fos.write(aByte);
            }
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }

    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();
            temp = temp >> 8;
        }
        return b;
    }

    // add by yuanbin on 2013-08-08
    private static final File openFile(String filePath) {
        if (filePath == null)
            return null;
        File file = new File(filePath);
        if (!file.exists()) {
            String parent = file.getParent();
            if (parent != null) {
                File parentFile = new File(parent);
                if (parentFile.exists()) {
                    if (parentFile.isFile()) {
                        parentFile.delete();
                        parentFile.mkdirs();
                    }
                } else {
                    parentFile.mkdirs();
                }
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

    // store in Gzip format, 2013-08-15
    public static final void storeGzip(FileOutputStream fos,
            StringBuffer errMsgBuf) {
        try {
            int len = -1;
            char[] cbuf = new char[1024];
            StringReader sr = new StringReader(errMsgBuf.toString());
            GZIPOutputStream gos = new GZIPOutputStream(fos);

            while ((len = sr.read(cbuf)) != -1) {
                gos.write(String.valueOf(cbuf, 0, len).getBytes());
            }

            gos.flush();
            gos.close();
            sr.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // add by yuanbin on 2013-08-08
    // add synchronized, 2014-01-07
    public synchronized static final boolean writeErrorLog(String filePath,
            int errorNo, String imeiNo, String startTime, int sequenceNo,
            Exception ex) {
        if (filePath == null)
            return false;
        File logFile = openFile(filePath);
        // add judge for no sdcard or no write sdcard permission exception by
        // yuanbin on 2013-08-08
        if (null == logFile) {
            return false;
        }

        try {
            // set limit for log file size, 2013-08-08
            long logFileLen = logFile.length();
            if (logFileLen > 1024 * MAX_ERROR_LOG_SIZE) {
                return false;
            }

            FileOutputStream fos = new FileOutputStream(logFile, true);
            StringBuffer errMsgBuf = new StringBuffer();
            errMsgBuf.append("error_no=" + errorNo + "&imei_no=" + imeiNo
                    + "&start_time=" + startTime + "&sequence_no=" + sequenceNo
                    + "&err_msg=");
            errMsgBuf.append(ex.getMessage() + ";");
            StackTraceElement[] errStack = ex.getStackTrace();

            int stackLen = errStack.length;
            int recStart = 0;
            for (int i = recStart; i < stackLen; i++) {
                errMsgBuf.append(errStack[i].toString());
                if (i < stackLen - 1) {
                    errMsgBuf.append(";");
                }
            }

            errMsgBuf.append("\n");

            // store in Gzip format, 2013-08-15
            storeGzip(fos, errMsgBuf);
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // add by yuanbin on 2013-08-08
    // add synchronized, 2014-01-07
    public synchronized static final boolean writeErrorLog(String filePath,
            int errorNo, String imeiNo, String startTime, int sequenceNo,
            String errMsg) {
        if (filePath == null)
            return false;
        File logFile = openFile(filePath);
        // add judge for no sdcard or no write sdcard permission exception by
        // yuanbin on 2013-08-08
        if (null == logFile) {
            return false;
        }

        try {
            // set limit for log file size, 2013-08-08
            long logFileLen = logFile.length();
            if (logFileLen > 1024 * MAX_ERROR_LOG_SIZE) {
                return false;
            }

            FileOutputStream fos = new FileOutputStream(logFile, true);
            StringBuffer errMsgBuf = new StringBuffer();
            errMsgBuf.append("error_no=" + errorNo + "&imei_no=" + imeiNo
                    + "&start_time=" + startTime + "&sequence_no=" + sequenceNo
                    + "&err_msg=" + errMsg + "\n");

            // store in Gzip format, 2013-08-15
            storeGzip(fos, errMsgBuf);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // get file size, 2013-08-15
    public static final long getFileSize(String filePath) {
        if (filePath == null || filePath.equals("")) {
            return -1;
        }

        File logFile = openFile(filePath);
        if (logFile == null) {
            return -1;
        }

        return logFile.length();
    }
}
