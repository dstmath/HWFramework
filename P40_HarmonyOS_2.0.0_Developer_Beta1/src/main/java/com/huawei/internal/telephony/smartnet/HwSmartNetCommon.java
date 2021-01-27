package com.huawei.internal.telephony.smartnet;

import android.text.TextUtils;
import com.huawei.android.telephony.RlogEx;
import com.huawei.hwparttelephonyfullnetwork.BuildConfig;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HwSmartNetCommon {
    private static final String ALGO_STR = "SHA-256";
    private static final String HEX = "0123456789ABCDEF";

    private HwSmartNetCommon() {
    }

    public static boolean isValidSlotId(int slotId) {
        return slotId >= 0 && slotId < HwSmartNetConstants.SIM_NUM;
    }

    public static String calcHash(String iccId) {
        try {
            return toHex(MessageDigest.getInstance(ALGO_STR).digest(toByte(iccId)));
        } catch (NoSuchAlgorithmException e) {
            RlogEx.e("HwSmartNetCommon", "NoSuchAlgorithmException.");
            return null;
        }
    }

    public static byte[] toByte(String hexString) {
        if (TextUtils.isEmpty(hexString)) {
            return new byte[0];
        }
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            try {
                result[i] = Integer.valueOf(hexString.substring(i * 2, (i * 2) + 2), 16).byteValue();
            } catch (NumberFormatException e) {
                return new byte[0];
            }
        }
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null) {
            return BuildConfig.FLAVOR;
        }
        StringBuffer result = new StringBuffer(buf.length * 2);
        for (byte b : buf) {
            appendHex(result, b);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte byteData) {
        sb.append(HEX.charAt((byteData >> 4) & 15));
        sb.append(HEX.charAt(byteData & 15));
    }
}
