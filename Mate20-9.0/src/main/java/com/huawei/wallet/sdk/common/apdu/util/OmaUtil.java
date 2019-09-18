package com.huawei.wallet.sdk.common.apdu.util;

import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.Locale;

public class OmaUtil {
    private static final String SELECT_COMMANDER = "00A40400";
    private static String logBuildType;

    public static String getSelectApdu(String aid) {
        String str;
        if (StringUtil.isEmpty(aid, true)) {
            return null;
        }
        String lenHex = Integer.toHexString(aid.length() / 2).toUpperCase(Locale.getDefault());
        StringBuilder sb = new StringBuilder();
        sb.append(SELECT_COMMANDER);
        if (lenHex.length() > 1) {
            str = lenHex;
        } else {
            str = "0" + lenHex;
        }
        sb.append(str);
        sb.append(aid);
        return sb.toString();
    }

    public static String getLogApdu(String apdu) {
        if (StringUtil.isEmpty(apdu, true) || apdu.length() < 16) {
            return apdu;
        }
        return apdu;
    }
}
