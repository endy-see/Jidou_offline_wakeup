package com.sogou.speech.wakeup.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

// move here and remove useless files, 2013-08-15

public class FileOperator {

    public static final boolean deleteDir(File dir) {
        boolean bRet = false;
        if (dir != null && dir.isDirectory()) {
            File[] entries = dir.listFiles();
            int sz = entries.length;
            for (int i = 0; i < sz; i++) {
                if (entries[i].isDirectory()) {
                    deleteDir(entries[i]);
                } else {
                    entries[i].delete();
                }
            }
            dir.delete();
            bRet = true;
        }
        return bRet;
    }

    public static final boolean clearDir(File dir, FileFilter filter) {
        boolean bRet = false;
        if (dir != null && dir.isDirectory()) {
            File[] entries = dir.listFiles(filter);
            if (entries == null)
                return false;
            int sz = entries.length;
            for (int i = 0; i < sz; i++) {
                if (entries[i].isDirectory()) {
                    deleteDir(entries[i]);
                } else {
                    entries[i].delete();
                }
            }
            bRet = true;
        }
        return bRet;
    }

    public static final boolean deleteFile(File file) {
        if (file != null && file.isDirectory()) {
            return false;
        } else if (file != null && file.isFile()) {
            return file.delete();
        }
        return true;
    }

    public static final boolean deleteFile(String filePath) {
        if (filePath == null) {
            return true;
        }
        File file = new File(filePath);
        if (file != null && file.isDirectory()) {
            return false;
        } else if (file != null && file.isFile()) {
            return file.delete();
        }
        return true;
    }

    public static final long getDirectorySize(File dir) {
        long retSize = 0;
        if ((dir == null) || !dir.isDirectory()) {
            return retSize;
        }
        File[] entries = dir.listFiles();
        int count = entries.length;
        for (int i = 0; i < count; i++) {
            if (entries[i].isDirectory()) {
                retSize += getDirectorySize(entries[i]);
            } else {
                retSize += entries[i].length();
            }
        }
        return retSize;
    }

    public static final long getDirectorySize(File dir, FileFilter filter) {
        long retSize = 0;
        if ((dir == null) || !dir.isDirectory()) {
            return retSize;
        }
        File[] entries = dir.listFiles(filter);
        int count = entries.length;
        for (int i = 0; i < count; i++) {
            if (entries[i].isDirectory()) {
                retSize += getDirectorySize(entries[i]);
            } else {
                retSize += entries[i].length();
            }
        }
        return retSize;
    }

    public static final void createDirectory(String strDir,
            boolean authorization) {
        createDirectory(strDir, authorization, true);
    }

    public static final void createDirectory(String strDir,
            boolean authorization, boolean clearIfExist) {
        if (strDir == null)
            return;
        try {
            boolean mkdirOk = false;
            File file = new File(strDir);
            if (!file.isDirectory()) {
                file.delete();
                mkdirOk = file.mkdirs();
            } else {
                if (clearIfExist)
                    clearDir(file, null);
            }
            if (mkdirOk && authorization) {
                Runtime.getRuntime().exec("chmod 777 " + strDir);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final void moveFile(String strOriginal, String strDest) {
        try {
            File fileOriginal = new File(strOriginal);
            File fileDest = new File(strDest);
            fileOriginal.renameTo(fileDest);
        } catch (Exception e) {
        }
    }
}
