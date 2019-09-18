package com.huawei.nb.ai;

import com.huawei.nb.efs.EfsException;
import com.huawei.nb.efs.EfsRwChannel;
import com.huawei.nb.utils.logger.DSLog;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public final class AiModelWriter {
    public static final int TO_ENCRYPTED = 1;
    public static final int TO_NORMAL = 0;

    private AiModelWriter() {
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x005f  */
    public static boolean writeAiModel(String filePath, byte[] data, byte[] key) {
        String str;
        Throwable e;
        if (filePath == null || data == null) {
            StringBuilder append = new StringBuilder().append("Error: Invalid input");
            if (data == null) {
                str = ", data is null.";
            } else {
                str = ".";
            }
            DSLog.e(append.append(str).toString(), new Object[0]);
            return false;
        }
        EfsRwChannel channel = null;
        try {
            channel = EfsRwChannel.open(filePath, 6, key);
            channel.startTransaction(1, 1);
            channel.truncateFile(0);
            channel.write(0, data, 0, data.length);
            channel.endTransaction(true);
            if (key != null) {
                Arrays.fill(key, (byte) 0);
            }
            closeEfsRwChange(channel);
            return true;
        } catch (UnsatisfiedLinkError e2) {
            e = e2;
            try {
                DSLog.e("Failed to write AI model %s, error: %s.", filePath, e.getMessage());
                if (key != null) {
                    Arrays.fill(key, (byte) 0);
                }
                closeEfsRwChange(channel);
                return false;
            } catch (Throwable th) {
                if (key != null) {
                    Arrays.fill(key, (byte) 0);
                }
                closeEfsRwChange(channel);
                throw th;
            }
        } catch (EfsException e3) {
            e = e3;
            DSLog.e("Failed to write AI model %s, error: %s.", filePath, e.getMessage());
            if (key != null) {
            }
            closeEfsRwChange(channel);
            return false;
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0062 A[Catch:{ all -> 0x0078 }] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0075  */
    public static boolean changeStorageMode(String filePath, String key, int type) {
        String str;
        Throwable e;
        byte[] bArr;
        if ((type != 1 && type != 0) || key == null || filePath == null) {
            StringBuilder append = new StringBuilder().append("Error: Invalid input, type is ").append(type);
            if (key == null) {
                str = ",key is null.";
            } else {
                str = ".";
            }
            DSLog.e(append.append(str).toString(), new Object[0]);
            return false;
        }
        try {
            byte[] keyByte = key.getBytes("UTF-8");
            if (type == 1) {
                bArr = null;
            } else {
                bArr = keyByte;
            }
            EfsRwChannel channel = EfsRwChannel.open(filePath, 6, bArr);
            channel.startTransaction(1, 1);
            if (type != 1) {
                keyByte = null;
            }
            channel.setKey(keyByte);
            channel.endTransaction(true);
            closeEfsRwChange(channel);
            return true;
        } catch (UnsupportedEncodingException e2) {
            e = e2;
            try {
                Object[] objArr = new Object[3];
                objArr[0] = filePath;
                objArr[1] = type == 1 ? "encrypted" : "";
                objArr[2] = e.getMessage();
                DSLog.e("Failed to change AI model %s to %s mode, error: %s.", objArr);
                closeEfsRwChange(null);
                return false;
            } catch (Throwable th) {
                closeEfsRwChange(null);
                throw th;
            }
        } catch (UnsatisfiedLinkError e3) {
            e = e3;
            Object[] objArr2 = new Object[3];
            objArr2[0] = filePath;
            objArr2[1] = type == 1 ? "encrypted" : "";
            objArr2[2] = e.getMessage();
            DSLog.e("Failed to change AI model %s to %s mode, error: %s.", objArr2);
            closeEfsRwChange(null);
            return false;
        } catch (EfsException e4) {
            e = e4;
            Object[] objArr22 = new Object[3];
            objArr22[0] = filePath;
            objArr22[1] = type == 1 ? "encrypted" : "";
            objArr22[2] = e.getMessage();
            DSLog.e("Failed to change AI model %s to %s mode, error: %s.", objArr22);
            closeEfsRwChange(null);
            return false;
        }
    }

    private static void closeEfsRwChange(EfsRwChannel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (EfsException e) {
                DSLog.e("Failed to close EfsRwChannel.", new Object[0]);
            }
        }
    }
}
