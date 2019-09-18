package com.huawei.secure.android.common.util;

import android.text.TextUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipUtil {
    private static final int BUFFER = 4096;
    private static final String INVALID_STR = "../";
    private static final String TAG = "ZipUtil";
    private static final int TOP_FILE_NUM = 100;
    private static final int TOP_SIZE = 104857600;

    public static boolean unZip(String zipFile, String targetDir, boolean isDeleteOld) throws SecurityCommonException {
        return unZip(zipFile, targetDir, TOP_SIZE, 100, isDeleteOld);
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x010a  */
    public static boolean unZip(String zipFile, String targetDir, int topSize, int topFileNumbers, boolean isDeleteOld) throws SecurityCommonException {
        int len;
        String str = targetDir;
        if (!isFileOrDirSafe(zipFile, targetDir, topSize, topFileNumbers)) {
            return false;
        }
        if (str.endsWith(File.separator) && targetDir.length() > File.separator.length()) {
            str = str.substring(0, targetDir.length() - File.separator.length());
        }
        String targetDir2 = str;
        boolean result = true;
        int len2 = BUFFER;
        byte[] buf = new byte[BUFFER];
        BufferedOutputStream bos = null;
        FileInputStream fis = null;
        ZipInputStream zis = null;
        FileOutputStream fos = null;
        List<File> listFile = new ArrayList<>();
        try {
            try {
                fis = new FileInputStream(zipFile);
                zis = new ZipInputStream(new BufferedInputStream(fis));
                while (true) {
                    ZipEntry nextEntry = zis.getNextEntry();
                    ZipEntry entry = nextEntry;
                    if (nextEntry == null) {
                        break;
                    }
                    File file = new File(targetDir2 + File.separator + entry.getName());
                    if (file.exists() && isDeleteOld && !recursionDeleteFile(file)) {
                        LogsUtil.e(TAG, "delete existing file error");
                        result = false;
                        break;
                    }
                    if (entry.isDirectory()) {
                        file.mkdirs();
                        listFile.add(file);
                    } else {
                        File parent = file.getParentFile();
                        if (!parent.exists()) {
                            parent.mkdirs();
                        }
                        fos = new FileOutputStream(file);
                        bos = new BufferedOutputStream(fos);
                        while (true) {
                            int read = zis.read(buf, 0, len2);
                            len = read;
                            if (read == -1) {
                                break;
                            }
                            bos.write(buf, 0, len);
                            len2 = BUFFER;
                        }
                        listFile.add(file);
                        bos.flush();
                        IOUtil.closeSecure((OutputStream) bos);
                        IOUtil.closeSecure((OutputStream) fos);
                    }
                    zis.closeEntry();
                    len2 = BUFFER;
                }
                IOUtil.closeSecure((InputStream) zis);
                IOUtil.closeSecure((InputStream) fis);
            } catch (IOException e) {
                e = e;
                result = false;
                try {
                    LogsUtil.e(TAG, "Unzip IOException : " + e.getMessage());
                    closeStream(fis, bos, zis, fos);
                    if (!result) {
                    }
                    return result;
                } catch (Throwable th) {
                    th = th;
                    closeStream(null, null, null, null);
                    throw th;
                }
            }
        } catch (IOException e2) {
            e = e2;
            String str2 = zipFile;
            result = false;
            LogsUtil.e(TAG, "Unzip IOException : " + e.getMessage());
            closeStream(fis, bos, zis, fos);
            if (!result) {
            }
            return result;
        } catch (Throwable th2) {
            th = th2;
            String str3 = zipFile;
            closeStream(null, null, null, null);
            throw th;
        }
        closeStream(fis, bos, zis, fos);
        if (!result) {
            unZipFailDelete(listFile);
        }
        return result;
    }

    private static boolean isZipFileValid(String filePath, int topSize, int topFileNumbers) {
        boolean isValid = true;
        ZipFile zipFile = null;
        int fileNumbers = 0;
        long totalSize = 0;
        try {
            ZipFile zipFile2 = new ZipFile(filePath);
            Enumeration<? extends ZipEntry> entries = zipFile2.entries();
            while (true) {
                if (entries.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                    totalSize += zipEntry.getSize();
                    fileNumbers++;
                    if (!zipEntry.getName().contains(INVALID_STR) && fileNumbers < topFileNumbers && totalSize <= ((long) topSize)) {
                        if (zipEntry.getSize() == -1) {
                            break;
                        }
                    } else {
                        LogsUtil.e(TAG, "File name is invalid or too many files or too big");
                    }
                }
                break;
            }
            LogsUtil.e(TAG, "File name is invalid or too many files or too big");
            isValid = false;
            try {
                break;
                zipFile2.close();
            } catch (IOException e) {
                LogsUtil.e(TAG, "close zipFile IOException ");
            }
        } catch (IOException e2) {
            isValid = false;
            LogsUtil.e(TAG, "not a valid zip file" + e2.getMessage());
            if (zipFile != null) {
                zipFile.close();
            }
        } catch (Throwable th) {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e3) {
                    LogsUtil.e(TAG, "close zipFile IOException ");
                }
            }
            throw th;
        }
        return isValid;
    }

    private static boolean recursionDeleteFile(File file) {
        if (file == null) {
            LogsUtil.e(TAG, "File is null");
            return true;
        }
        if (!file.isFile()) {
            if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    file.delete();
                    return true;
                }
                for (File f : childFile) {
                    recursionDeleteFile(f);
                }
                file.delete();
            }
            return true;
        } else if (file.delete()) {
            return true;
        } else {
            LogsUtil.e(TAG, "delete file error");
            return false;
        }
    }

    private static boolean isFileOrDirSafe(String zipFile, String targetDir, int topSize, int topFileNumbers) throws SecurityCommonException {
        if (TextUtils.isEmpty(zipFile) || zipFile.contains(INVALID_STR)) {
            LogsUtil.e(TAG, "zip file is not valid");
            return false;
        } else if (TextUtils.isEmpty(targetDir) || targetDir.contains(INVALID_STR)) {
            LogsUtil.e(TAG, "target directory is not valid");
            return false;
        } else if (isZipFileValid(zipFile, topSize, topFileNumbers)) {
            return true;
        } else {
            LogsUtil.e(TAG, "zip file contains valid chars or too many files");
            throw new SecurityCommonException("unsecure zipfile!");
        }
    }

    private static boolean unZipFailDelete(List<File> listFile) {
        try {
            for (File file : listFile) {
                recursionDeleteFile(file);
            }
            return true;
        } catch (Exception e) {
            LogsUtil.e(TAG, "unzip fail delete file failed" + e.getMessage());
            return false;
        }
    }

    private static void closeStream(FileInputStream fis, BufferedOutputStream dest, ZipInputStream zis, FileOutputStream fos) {
        IOUtil.closeSecure((InputStream) fis);
        IOUtil.closeSecure((OutputStream) dest);
        IOUtil.closeSecure((InputStream) zis);
        IOUtil.closeSecure((OutputStream) fos);
    }
}
