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

    /* JADX WARNING: Removed duplicated region for block: B:62:0x012c  */
    public static boolean unZip(String zipFile, String targetDir, int topSize, int topFileNumbers, boolean isDeleteOld) throws SecurityCommonException {
        String targetDir2;
        Throwable th;
        IOException e;
        String targetDir3;
        String targetDir4;
        String str = null;
        if (!isFileOrDirSafe(zipFile, targetDir, topSize, topFileNumbers)) {
            return false;
        }
        if (!targetDir.endsWith(File.separator) || targetDir.length() <= File.separator.length()) {
            targetDir2 = targetDir;
        } else {
            targetDir2 = targetDir.substring(0, targetDir.length() - File.separator.length());
        }
        boolean result = true;
        byte[] buf = new byte[4096];
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
                    ZipEntry entry = zis.getNextEntry();
                    if (entry == null) {
                        break;
                    }
                    File file = new File(targetDir2 + File.separator + entry.getName());
                    if (isDeleteOld) {
                        try {
                            if (file.exists()) {
                                recursionDeleteFile(file);
                            }
                        } catch (IOException e2) {
                            e = e2;
                            result = false;
                            try {
                                LogsUtil.e(TAG, "Unzip IOException : " + e.getMessage());
                                closeStream(fis, bos, zis, fos);
                                if (!result) {
                                }
                                return result;
                            } catch (Throwable th2) {
                                th = th2;
                                closeStream(fis, bos, zis, fos);
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            closeStream(fis, bos, zis, fos);
                            throw th;
                        }
                    }
                    if (entry.isDirectory()) {
                        mkdirs(file);
                        listFile.add(file);
                        targetDir3 = targetDir2;
                        targetDir4 = str;
                    } else {
                        File parent = file.getParentFile();
                        if (parent != null && !parent.exists()) {
                            mkdirs(parent);
                        }
                        fos = new FileOutputStream(file);
                        bos = new BufferedOutputStream(fos);
                        int curTotalSize = 0;
                        while (true) {
                            if (curTotalSize + 4096 > topSize) {
                                targetDir3 = targetDir2;
                                targetDir4 = null;
                                break;
                            }
                            targetDir3 = targetDir2;
                            targetDir4 = null;
                            try {
                                int len = zis.read(buf, 0, 4096);
                                if (len == -1) {
                                    break;
                                }
                                bos.write(buf, 0, len);
                                curTotalSize += len;
                                targetDir2 = targetDir3;
                            } catch (IOException e3) {
                                e = e3;
                                result = false;
                                LogsUtil.e(TAG, "Unzip IOException : " + e.getMessage());
                                closeStream(fis, bos, zis, fos);
                                if (!result) {
                                }
                                return result;
                            }
                        }
                        listFile.add(file);
                        bos.flush();
                        IOUtil.closeSecure((OutputStream) bos);
                        IOUtil.closeSecure((OutputStream) fos);
                    }
                    zis.closeEntry();
                    str = targetDir4;
                    targetDir2 = targetDir3;
                }
                IOUtil.closeSecure((InputStream) zis);
                IOUtil.closeSecure((InputStream) fis);
                closeStream(fis, bos, zis, fos);
            } catch (IOException e4) {
                e = e4;
                result = false;
                LogsUtil.e(TAG, "Unzip IOException : " + e.getMessage());
                closeStream(fis, bos, zis, fos);
                if (!result) {
                }
                return result;
            } catch (Throwable th4) {
                th = th4;
                closeStream(fis, bos, zis, fos);
                throw th;
            }
        } catch (IOException e5) {
            e = e5;
            result = false;
            LogsUtil.e(TAG, "Unzip IOException : " + e.getMessage());
            closeStream(fis, bos, zis, fos);
            if (!result) {
            }
            return result;
        } catch (Throwable th5) {
            th = th5;
            closeStream(fis, bos, zis, fos);
            throw th;
        }
        if (!result) {
            unZipFailDelete(listFile);
        }
        return result;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0054, code lost:
        com.huawei.secure.android.common.util.LogsUtil.e(com.huawei.secure.android.common.util.ZipUtil.TAG, "File name is invalid or too many files or too big");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0059, code lost:
        r3 = false;
     */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00a8 A[SYNTHETIC, Splitter:B:45:0x00a8] */
    private static boolean isZipFileValid(String filePath, int topSize, int topFileNumbers) {
        IOException e;
        boolean isValid = true;
        ZipFile zipFile = null;
        int fileNumbers = 0;
        long totalSize = 0;
        long currentEntrySize = 0;
        try {
            try {
                zipFile = new ZipFile(filePath);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (true) {
                    if (!entries.hasMoreElements()) {
                        break;
                    }
                    ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                    currentEntrySize = zipEntry.getSize();
                    totalSize += currentEntrySize;
                    fileNumbers++;
                    if (!zipEntry.getName().contains(INVALID_STR)) {
                        if (fileNumbers < topFileNumbers) {
                            if (totalSize > ((long) topSize)) {
                                break;
                            }
                            try {
                                if (zipEntry.getSize() == -1) {
                                    break;
                                }
                            } catch (IOException e2) {
                                e = e2;
                                isValid = false;
                                try {
                                    LogsUtil.e(TAG, "not a valid zip file, IOException : " + e.getMessage());
                                    if (zipFile != null) {
                                        zipFile.close();
                                    }
                                    return isValid;
                                } catch (Throwable th) {
                                    e = th;
                                    if (zipFile != null) {
                                        try {
                                            zipFile.close();
                                        } catch (IOException e3) {
                                            LogsUtil.e(TAG, "close zipFile IOException ");
                                        }
                                    }
                                    throw e;
                                }
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                try {
                    zipFile.close();
                } catch (IOException e4) {
                    LogsUtil.e(TAG, "close zipFile IOException ");
                }
            } catch (IOException e5) {
                e = e5;
                isValid = false;
                LogsUtil.e(TAG, "not a valid zip file, IOException : " + e.getMessage());
                if (zipFile != null) {
                }
                return isValid;
            } catch (Throwable th2) {
                e = th2;
                if (zipFile != null) {
                }
                throw e;
            }
        } catch (IOException e6) {
            e = e6;
            isValid = false;
            LogsUtil.e(TAG, "not a valid zip file, IOException : " + e.getMessage());
            if (zipFile != null) {
            }
            return isValid;
        } catch (Throwable th3) {
            e = th3;
            if (zipFile != null) {
            }
            throw e;
        }
        return isValid;
    }

    private static void recursionDeleteFile(File file) {
        if (file != null) {
            if (file.isFile()) {
                deleteFile(file);
            } else if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    deleteFile(file);
                    return;
                }
                for (File f : childFile) {
                    recursionDeleteFile(f);
                }
                deleteFile(file);
            }
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

    private static void deleteFile(File file) {
        if (file != null && !file.delete()) {
            LogsUtil.e(TAG, "delete file error");
        }
    }

    private static void mkdirs(File file) {
        if (file != null && !file.exists() && !file.mkdirs()) {
            LogsUtil.e(TAG, "mkdirs error , files exists or IOException.");
        }
    }
}
