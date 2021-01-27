package com.huawei.server.bluetooth;

import com.android.server.HwLog;
import com.android.server.location.HwLocalLocationProvider;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.util.ArrayList;
import java.util.List;

public final class HwUtils {
    static final int BYTE_LENGTH_OF_MACADDR = 2;
    static final int STRING_FORMAT_HEX = 16;
    private static final String TAG = "BT-HwUtils";

    static boolean isValidMac(String macAddr) {
        if (macAddr == null || "".equals(macAddr)) {
            HwLog.e(TAG, "address is empty");
            return false;
        } else if (macAddr.matches("([A-Fa-f0-9]{2}[-,:]){5}[A-Fa-f0-9]{2}")) {
            return true;
        } else {
            HwLog.e(TAG, "invaild address");
            return false;
        }
    }

    static byte[] getBytesFromAddress(String address) {
        int outIndex = 0;
        byte[] output = new byte[6];
        int i = 0;
        while (i < address.length()) {
            try {
                if (address.charAt(i) != ':') {
                    output[outIndex] = (byte) Integer.parseInt(address.substring(i, i + 2), 16);
                    outIndex++;
                    i++;
                }
                i++;
            } catch (NumberFormatException e) {
                HwLog.e(TAG, "getBytesFromAddress: address parse failed");
                return null;
            }
        }
        return output;
    }

    static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte byteValue : bytes) {
            String tmpStr = Integer.toHexString(byteValue & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
            if (tmpStr.length() == 1) {
                tmpStr = "0" + tmpStr;
            }
            sb.append(tmpStr);
        }
        return sb.toString();
    }

    static String bytesToHexAddrString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder((bytes.length * 3) - 1);
        int len = bytes.length;
        for (byte byteValue : bytes) {
            String tmpStr = Integer.toHexString(byteValue & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
            if (tmpStr.length() == 1) {
                tmpStr = "0" + tmpStr;
            }
            sb.append(tmpStr);
            len--;
            if (len > 0) {
                sb.append(AwarenessInnerConstants.COLON_KEY);
            }
        }
        return sb.toString();
    }

    static List<Integer> stringToIntegers(String value) {
        String[] strArray = value.split(",");
        List<Integer> intArray = new ArrayList<>(strArray.length);
        for (String str : strArray) {
            try {
                intArray.add(Integer.valueOf(Integer.parseInt(str)));
            } catch (NumberFormatException e) {
                HwLog.w(TAG, "stringToIntegers: not int " + str);
            }
        }
        HwLog.i(TAG, "stringToIntegers: " + intArray.size());
        return intArray;
    }
}
