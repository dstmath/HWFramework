package com.huawei.server.security.pwdprotect.utils;

import android.text.TextUtils;
import android.util.Log;
import com.huawei.server.security.pwdprotect.model.PasswordIvsCache;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

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
        return readFile(PasswordIvsCache.FILE_E_PIN2, PUBLICKEY);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00c9, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00ce, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00cf, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00d2, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00d5, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00da, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00db, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00de, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00e1, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x00e6, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x00e7, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x00ea, code lost:
        throw r4;
     */
    public static byte[] readFile(File file, int valueType) {
        if (!file.exists()) {
            Log.e(TAG, "The file doesn't exist");
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            byte[] buf = new byte[4096];
            switch (valueType) {
                case 0:
                case 1:
                case 2:
                case 3:
                    while (bufferedInputStream.read(buf) != -1) {
                        byteArrayOutputStream.write(buf, 16 * valueType, 16);
                    }
                    break;
                case 4:
                    if (!file.getName().equals("E_SK2")) {
                        if (!file.getName().equals("E_PIN2")) {
                            while (true) {
                                int len = bufferedInputStream.read(buf);
                                if (len == -1) {
                                    break;
                                } else {
                                    byteArrayOutputStream.write(buf, 16 * 1, (len - 32) - (16 * 1));
                                }
                            }
                        } else {
                            while (true) {
                                int len2 = bufferedInputStream.read(buf);
                                if (len2 == -1) {
                                    break;
                                } else {
                                    byteArrayOutputStream.write(buf, 16 * 1, ((len2 - 32) - (16 * 1)) - PKLENGTH);
                                }
                            }
                        }
                    } else {
                        while (true) {
                            int len3 = bufferedInputStream.read(buf);
                            if (len3 == -1) {
                                break;
                            } else {
                                byteArrayOutputStream.write(buf, 16 * 4, (len3 - 32) - (16 * 4));
                            }
                        }
                    }
                case 5:
                    while (true) {
                        int len4 = bufferedInputStream.read(buf);
                        if (len4 == -1) {
                            break;
                        } else {
                            byteArrayOutputStream.write(buf, len4 - 32, 32);
                        }
                    }
                case PUBLICKEY /* 7 */:
                    while (true) {
                        int len5 = bufferedInputStream.read(buf);
                        if (len5 == -1) {
                            break;
                        } else {
                            byteArrayOutputStream.write(buf, (len5 - 32) - PKLENGTH, PKLENGTH);
                        }
                    }
                case 8:
                    while (true) {
                        int len6 = bufferedInputStream.read(buf);
                        if (len6 == -1) {
                            break;
                        } else {
                            byteArrayOutputStream.write(buf, 0, len6 - 32);
                        }
                    }
            }
            byte[] buffer = byteArrayOutputStream.toByteArray();
            bufferedInputStream.close();
            fileInputStream.close();
            byteArrayOutputStream.close();
            return buffer;
        } catch (IOException e) {
            Log.e(TAG, "read file exception!" + e.getMessage());
            return new byte[0];
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0021, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0026, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0027, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002a, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002d, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0032, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0033, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0036, code lost:
        throw r2;
     */
    public static boolean writeFile(byte[] values, File fileName) {
        mkdirHwSecurity();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            byteArrayOutputStream.write(values);
            byteArrayOutputStream.writeTo(fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            byteArrayOutputStream.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "write file exception! " + fileName.getName() + e.getMessage());
            return false;
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
        } catch (IOException e) {
            Log.e(TAG, "newFile: new file exception!");
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
