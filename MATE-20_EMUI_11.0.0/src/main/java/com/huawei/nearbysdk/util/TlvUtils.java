package com.huawei.nearbysdk.util;

import com.huawei.nearbysdk.HwLog;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TlvUtils {
    public static final int ERROR_LENGTH = -1;
    public static final int ERROR_TAG = -1;
    public static final int ERROR_VALUE = -1;
    public static final String TAG = "TlvUtils";

    private TlvUtils() {
    }

    public static void writeTlv(DataOutputStream os, byte tag, int len, byte[] value) {
        try {
            os.write(tag);
            os.writeInt(len);
            os.write(value);
        } catch (IOException e) {
            HwLog.e(TAG, "IOException " + e.getLocalizedMessage());
        }
    }

    public static int getIntValue(DataInputStream in) {
        try {
            return in.readInt();
        } catch (IOException e) {
            HwLog.e(TAG, "IOException " + e.getLocalizedMessage());
            return -1;
        }
    }

    public static int getLen(DataInputStream in) {
        try {
            return in.readInt();
        } catch (IOException e) {
            HwLog.e(TAG, "IOException " + e.getLocalizedMessage());
            return -1;
        }
    }

    public static byte getTag(DataInputStream in) {
        try {
            return in.readByte();
        } catch (IOException e) {
            HwLog.e(TAG, "IOException " + e.getLocalizedMessage());
            return -1;
        }
    }

    private static int computeLenSize(int len) {
        if ((len & Integer.MIN_VALUE) == Integer.MIN_VALUE || len <= 0) {
            return 4;
        }
        return len & 255;
    }

    public static byte[] int2Bytes(int value, int len) {
        byte[] valueByte = new byte[computeLenSize(len)];
        valueByte[0] = (byte) (value >> 24);
        valueByte[1] = (byte) (value >> 16);
        valueByte[2] = (byte) (value >> 8);
        valueByte[3] = (byte) value;
        return valueByte;
    }
}
