package com.huawei.nearbysdk;

import com.huawei.nearbysdk.NearbyConfig;
import java.io.FileDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class NearbySDKUtils {
    public static final String DEFAULT_SOURCE = "source";
    private static final int DataInt = 1;
    private static final int InstantMessageInt = 3;
    public static final int NORMAL_SEND_FILE_SIZE = 500;
    public static final String SEND_DIR = "sendDir";
    public static final String SEND_TYPE = "sendType";
    public static final int SEND_TYPE_ALBUM = 2;
    public static final int SEND_TYPE_DIR = 3;
    public static final int SEND_TYPE_INVALID = -1;
    public static final int SEND_TYPE_NORMAL = 1;
    private static final int StreamingInt = 4;
    private static final String TAG = "NearbySDKUtils";
    private static final int TokenInt = 2;

    public static NearbyConfig.BusinessTypeEnum getEnumFromInt(int businessType) {
        NearbyConfig.BusinessTypeEnum businessTypeEnum = NearbyConfig.BusinessTypeEnum.Token;
        switch (businessType) {
            case 1:
                return NearbyConfig.BusinessTypeEnum.Data;
            case 2:
                return NearbyConfig.BusinessTypeEnum.Token;
            case 3:
                return NearbyConfig.BusinessTypeEnum.InstantMessage;
            case 4:
                return NearbyConfig.BusinessTypeEnum.Streaming;
            default:
                return businessTypeEnum;
        }
    }

    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public static byte[] jointByteArrays(byte[]... args) {
        if (args == null || args.length == 0) {
            return new byte[0];
        }
        if (args.length == 1) {
            return args[0];
        }
        int lengthSum = 0;
        for (byte[] barr : args) {
            if (barr == null) {
                barr = new byte[0];
            }
            lengthSum += barr.length;
        }
        byte[] res = new byte[lengthSum];
        int offset = 0;
        for (byte[] barr2 : args) {
            arraycopy(barr2, 0, res, offset, barr2.length);
            offset += barr2.length;
        }
        return res;
    }

    public static byte[] Int2Byte(int value) {
        byte[] buffer = new byte[4];
        for (int i = 0; i < 4; i++) {
            buffer[i] = (byte) ((value >> (32 - ((i + 1) * 8))) & 255);
        }
        return buffer;
    }

    public static int Byte2Int(byte[] buffer) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value = (value << 8) | (buffer[i] & 255);
        }
        return value;
    }

    public static boolean protectFromVpn(Socket socket) {
        if (socket == null) {
            HwLog.e(TAG, "socket is null.");
            return false;
        }
        try {
            Class networkUtilsClz = Class.forName("android.net.NetworkUtils");
            if (networkUtilsClz == null) {
                HwLog.e(TAG, "networkUtilsClz is null.");
                return false;
            }
            Method protectFromVpn = networkUtilsClz.getMethod("protectFromVpn", FileDescriptor.class);
            if (protectFromVpn == null) {
                HwLog.e(TAG, "protectFromVpn is null.");
                return false;
            }
            Class socketClz = Class.forName("java.net.Socket");
            if (socketClz == null) {
                HwLog.e(TAG, "socketClz is null.");
                return false;
            }
            Method getFileDescriptor = socketClz.getMethod("getFileDescriptor$", new Class[0]);
            if (getFileDescriptor == null) {
                HwLog.e(TAG, "getFileDescriptor is null.");
                return false;
            }
            FileDescriptor fd = (FileDescriptor) getFileDescriptor.invoke(socket, new Object[0]);
            if (fd == null) {
                HwLog.e(TAG, "fd is null.");
                return false;
            } else if (((Boolean) protectFromVpn.invoke(null, fd)).booleanValue()) {
                return true;
            } else {
                HwLog.e(TAG, "invoke protectFromVpn failed.");
                return false;
            }
        } catch (ClassNotFoundException e) {
            HwLog.e(TAG, "ClassNotFoundException: " + e.getLocalizedMessage());
            return false;
        } catch (NoSuchMethodException e2) {
            HwLog.e(TAG, "NoSuchMethodException: " + e2.getLocalizedMessage());
            return false;
        } catch (IllegalAccessException e3) {
            HwLog.e(TAG, "IllegalAccessException: " + e3.getLocalizedMessage());
            return false;
        } catch (InvocationTargetException e4) {
            HwLog.e(TAG, "InvocationTargetException: " + e4.getLocalizedMessage());
            return false;
        }
    }
}
