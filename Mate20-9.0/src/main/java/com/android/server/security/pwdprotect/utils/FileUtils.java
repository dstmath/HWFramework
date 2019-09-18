package com.android.server.security.pwdprotect.utils;

import android.text.TextUtils;
import android.util.Log;
import com.android.server.security.pwdprotect.model.PasswordIvsCache;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import libcore.io.IoUtils;

public class FileUtils {
    private static final int HASHLENGTH = 32;
    private static final int HASHVALUES = 5;
    private static final int IV_FIRST = 0;
    private static final int IV_FOUR = 3;
    private static final int IV_SECOND = 1;
    private static final int IV_THIRD = 2;
    private static final int KEYVALUES = 4;
    private static final int NOHASHVALUES = 8;
    private static final int PKLENGTH = 294;
    private static final int PUBLICKEY = 7;
    private static final String TAG = "PwdProtectService";

    public static byte[] readKeys(File file) {
        return readFile(file, 4);
    }

    private static byte[] readHashs(File file) {
        return readFile(file, 5);
    }

    public static byte[] readIvs(File file, int ivNo) {
        return readFile(file, ivNo);
    }

    private static byte[] readNoHashs(File file) {
        return readFile(file, 8);
    }

    public static byte[] readPublicKey() {
        return readFile(PasswordIvsCache.FILE_E_PIN2, 7);
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0041, code lost:
        if (r8 == -1) goto L_0x00ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004f, code lost:
        if (r6 == -1) goto L_0x00ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b2, code lost:
        if (r6 == -1) goto L_0x00ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0034, code lost:
        if (r6 == -1) goto L_0x00ba;
     */
    public static byte[] readFile(File file, int valueType) {
        if (!file.exists()) {
            Log.e(TAG, "The file doesn't exist");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] buf = new byte[4096];
            switch (valueType) {
                case 0:
                case 1:
                case 2:
                case 3:
                    while (true) {
                        int read = bis.read(buf);
                        int i = read;
                        baos.write(buf, 16 * valueType, 16);
                        break;
                    }
                case 4:
                    if (!file.getName().equals("E_SK2")) {
                        if (!file.getName().equals("E_PIN2")) {
                            while (true) {
                                int read2 = bis.read(buf);
                                int len = read2;
                                if (read2 == -1) {
                                    break;
                                } else {
                                    baos.write(buf, 16 * 1, (len - 32) - (16 * 1));
                                }
                            }
                        } else {
                            while (true) {
                                int read3 = bis.read(buf);
                                int len2 = read3;
                                if (read3 == -1) {
                                    break;
                                } else {
                                    baos.write(buf, 16 * 1, ((len2 - 32) - (16 * 1)) - PKLENGTH);
                                }
                            }
                        }
                    } else {
                        while (true) {
                            int read4 = bis.read(buf);
                            int len3 = read4;
                            if (read4 == -1) {
                                break;
                            } else {
                                baos.write(buf, 16 * 4, (len3 - 32) - (16 * 4));
                            }
                        }
                    }
                case 5:
                    while (true) {
                        int read5 = bis.read(buf);
                        int len4 = read5;
                        baos.write(buf, len4 - 32, 32);
                        break;
                    }
                case 7:
                    while (true) {
                        int len5 = bis.read(buf);
                        baos.write(buf, (len - 32) - PKLENGTH, PKLENGTH);
                        break;
                    }
                case 8:
                    while (true) {
                        int read6 = bis.read(buf);
                        int len6 = read6;
                        baos.write(buf, 0, len6 - 32);
                        break;
                    }
            }
            byte[] buffer = baos.toByteArray();
            IoUtils.closeQuietly(fis);
            IoUtils.closeQuietly(bis);
            IoUtils.closeQuietly(baos);
            return buffer;
        } catch (IOException e) {
            Log.e(TAG, "read file exception!" + e.getMessage());
            IoUtils.closeQuietly(null);
            IoUtils.closeQuietly(null);
            IoUtils.closeQuietly(baos);
            return new byte[0];
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
            IoUtils.closeQuietly(null);
            IoUtils.closeQuietly(baos);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public static boolean writeFile(byte[] values, File fileName) {
        mkdirHwSecurity();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        FileOutputStream fileOutputStream = null;
        try {
            byteArrayOutputStream.write(values);
            fileOutputStream = new FileOutputStream(fileName);
            byteArrayOutputStream.writeTo(fileOutputStream);
            fileOutputStream.flush();
            IoUtils.closeQuietly(byteArrayOutputStream);
            IoUtils.closeQuietly(fileOutputStream);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "write file exception! " + fileName.getName() + e.getMessage());
            IoUtils.closeQuietly(byteArrayOutputStream);
            IoUtils.closeQuietly(fileOutputStream);
            return false;
        } catch (Throwable th) {
            IoUtils.closeQuietly(byteArrayOutputStream);
            IoUtils.closeQuietly(fileOutputStream);
            throw th;
        }
    }

    public static File newFile(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        File screenFile = new File(fileName);
        try {
            boolean result = screenFile.createNewFile();
            Log.i(TAG, "new file " + screenFile.getName() + " result is :" + result);
            return screenFile;
        } catch (Exception e) {
            Log.e(TAG, "new file exception!" + e.getMessage());
            return null;
        }
    }

    private static Boolean verifyFile(File file) {
        if (Arrays.equals(readHashs(file), DeviceEncryptUtils.hmacSign(readNoHashs(file)))) {
            Log.i(TAG, "verify File " + file.getName() + " result is true");
            return true;
        }
        Log.e(TAG, "verify File " + file.getName() + " result is false");
        return false;
    }

    public static Boolean verifyFile() {
        if (!verifyFile(PasswordIvsCache.FILE_E_PWDQANSWER).booleanValue() || !verifyFile(PasswordIvsCache.FILE_E_PIN2).booleanValue() || !verifyFile(PasswordIvsCache.FILE_E_SK2).booleanValue() || !verifyFile(PasswordIvsCache.FILE_E_PWDQ).booleanValue()) {
            Log.e(TAG, "verify File is false");
            return false;
        }
        Log.i(TAG, "verify File is true");
        return true;
    }

    private static void mkdirHwSecurity() {
        File file = PasswordIvsCache.PWDPROTECT_DIR_PATH;
        if (!file.exists() && !file.mkdirs()) {
            Log.e(TAG, "mkdirs file failed");
        }
    }
}
