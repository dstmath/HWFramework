package com.huawei.server.security.pwdprotect.utils;

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
    private static final int BUFFER_SIZE = 4096;
    private static final int ERROR_FLAG = -1;
    private static final String E_PIN2_FILE_NAME = "E_PIN2";
    private static final String E_SK2_FILE_NAME = "E_SK2";
    private static final int HASH_LENGTH = 32;
    private static final int HASH_VALUES = 5;
    private static final int IV_FIRST_INDEX = 0;
    private static final int IV_FOURTH_INDEX = 3;
    private static final int IV_OFF_SET = 1;
    private static final int IV_SECOND_INDEX = 1;
    private static final int IV_THIRD_INDEX = 2;
    private static final int KEY_VALUES = 4;
    private static final int KEY_VALUE_INDEX_E_SK2 = 4;
    private static final int NO_HASH_VALUES = 8;
    private static final int OFF_SET_LENGTH = 16;
    private static final int PK_LENGTH = 294;
    private static final int PUBLIC_KEY = 7;
    private static final String TAG = "PwdProtectService";

    public static byte[] readKeys(File file) {
        if (file == null) {
            return new byte[0];
        }
        return readFile(file, 4);
    }

    public static byte[] readIvs(File file, int ivNo) {
        if (file == null) {
            return new byte[0];
        }
        if (ivNo < 0 || ivNo > 3) {
            return new byte[0];
        }
        return readFile(file, ivNo);
    }

    public static byte[] readPublicKey() {
        return readFile(PasswordIvsCache.FILE_E_PIN2, PUBLIC_KEY);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0045, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004a, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x004b, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004e, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0051, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0056, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0057, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x005a, code lost:
        throw r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x005d, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0062, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0063, code lost:
        r3.addSuppressed(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0066, code lost:
        throw r4;
     */
    public static byte[] readFile(File file, int valueType) {
        if (file == null || !file.exists()) {
            Log.e(TAG, "readFile: The file doesn't exist!");
            return new byte[0];
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            switch (valueType) {
                case 0:
                case 1:
                case 2:
                case 3:
                    ivValues(valueType, byteArrayOutputStream, bufferedInputStream);
                    break;
                case 4:
                    keyValues(file, byteArrayOutputStream, bufferedInputStream);
                    break;
                case 5:
                    hashValues(byteArrayOutputStream, bufferedInputStream);
                    break;
                case PUBLIC_KEY /* 7 */:
                    publicKey(byteArrayOutputStream, bufferedInputStream);
                    break;
                case 8:
                    noHashValues(byteArrayOutputStream, bufferedInputStream);
                    break;
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            bufferedInputStream.close();
            fileInputStream.close();
            byteArrayOutputStream.close();
            return byteArray;
        } catch (IOException e) {
            Log.e(TAG, "readFile: Read file exception!");
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
    public static boolean write(byte[] values, File file) {
        mkdirHwSecurity();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byteArrayOutputStream.write(values);
            byteArrayOutputStream.writeTo(fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            byteArrayOutputStream.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "write: Write file exception!");
            return false;
        }
    }

    public static boolean verifyFile() {
        if (!verifyFile(PasswordIvsCache.FILE_E_PWDQANSWER) || !verifyFile(PasswordIvsCache.FILE_E_PIN2) || !verifyFile(PasswordIvsCache.FILE_E_SK2) || !verifyFile(PasswordIvsCache.FILE_E_PWDQ)) {
            Log.e(TAG, "verifyFile: Verify File is false!");
            return false;
        }
        Log.i(TAG, "verifyFile: Verify File is true.");
        return true;
    }

    private static boolean verifyFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (Arrays.equals(readHashes(file), DeviceEncryptUtils.signWithHmac(readNoHashes(file)))) {
            Log.i(TAG, "verifyFile: Verify File is true.");
            return true;
        }
        Log.e(TAG, "verifyFile: Verify File is false!");
        return false;
    }

    private static byte[] readNoHashes(File file) {
        return readFile(file, 8);
    }

    private static byte[] readHashes(File file) {
        return readFile(file, 5);
    }

    private static void mkdirHwSecurity() {
        File file = PasswordIvsCache.PWDPROTECT_DIR_PATH;
        if (!file.exists() && !file.mkdirs()) {
            Log.e(TAG, "mkdirHwSecurity: Mkdirs file failed!");
        }
    }

    private static void keyValues(File file, ByteArrayOutputStream byteArrayOutputStream, BufferedInputStream bufferedInputStream) {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            if (E_SK2_FILE_NAME.equals(file.getName())) {
                while (true) {
                    int len = bufferedInputStream.read(buffer);
                    if (len != -1) {
                        byteArrayOutputStream.write(buffer, 64, (len - 32) - 64);
                    } else {
                        return;
                    }
                }
            } else if (E_PIN2_FILE_NAME.equals(file.getName())) {
                while (true) {
                    int len2 = bufferedInputStream.read(buffer);
                    if (len2 != -1) {
                        byteArrayOutputStream.write(buffer, 16, ((len2 - 32) - 16) - 294);
                    } else {
                        return;
                    }
                }
            } else {
                while (true) {
                    int len3 = bufferedInputStream.read(buffer);
                    if (len3 != -1) {
                        byteArrayOutputStream.write(buffer, 16, (len3 - 32) - 16);
                    } else {
                        return;
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "keyValues: Failed!");
        }
    }

    private static void ivValues(int valueType, ByteArrayOutputStream byteArrayOutputStream, BufferedInputStream bufferedInputStream) {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (bufferedInputStream.read(buffer) != -1) {
                byteArrayOutputStream.write(buffer, valueType * 16, 16);
            }
        } catch (IOException e) {
            Log.e(TAG, "ivValues: Failed!");
        }
    }

    private static void noHashValues(ByteArrayOutputStream byteArrayOutputStream, BufferedInputStream bufferedInputStream) {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                int len = bufferedInputStream.read(buffer);
                if (len != -1) {
                    byteArrayOutputStream.write(buffer, 0, len - 32);
                } else {
                    return;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "noHashValues: Failed!");
        }
    }

    private static void publicKey(ByteArrayOutputStream byteArrayOutputStream, BufferedInputStream bufferedInputStream) {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                int len = bufferedInputStream.read(buffer);
                if (len != -1) {
                    byteArrayOutputStream.write(buffer, (len - 32) - PK_LENGTH, PK_LENGTH);
                } else {
                    return;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "publicKey: Failed!");
        }
    }

    private static void hashValues(ByteArrayOutputStream byteArrayOutputStream, BufferedInputStream bufferedInputStream) {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                int len = bufferedInputStream.read(buffer);
                if (len != -1) {
                    byteArrayOutputStream.write(buffer, len - 32, 32);
                } else {
                    return;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "hashValues: Failed!");
        }
    }
}
