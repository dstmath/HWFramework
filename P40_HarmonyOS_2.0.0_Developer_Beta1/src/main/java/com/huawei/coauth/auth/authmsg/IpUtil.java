package com.huawei.coauth.auth.authmsg;

import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpUtil {
    private static final int BYTE_LEN = 8;
    private static final int BYTE_NUM = 4;
    private static final int INT_TO_STRING_MASK = 255;
    private static final String LOWER = "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])";
    private static final Pattern PATTERN = Pattern.compile("(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");
    private static final Pattern PATTERN_IP = Pattern.compile("\\d+");
    private static final int STRING_TO_INT_MASK = 0;
    private static final String TAG = IpUtil.class.getName();

    public static int ipString2Int(String ip) {
        if (!isIPv4Address(ip)) {
            Log.e(TAG, "Invalid param");
            return 0;
        }
        Matcher matcher = PATTERN_IP.matcher(ip);
        int result = 0;
        int start = 3;
        while (matcher.find()) {
            result |= Integer.parseInt(matcher.group()) << (start * 8);
            start--;
        }
        return result;
    }

    public static String ipInt2String(int ip) {
        StringBuilder sb = new StringBuilder();
        for (int start = 3; start >= 0; start--) {
            sb.append((ip >> (start * 8)) & 255);
            sb.append(".");
        }
        return sb.substring(0, sb.length() - 1);
    }

    private static boolean isIPv4Address(String ip) {
        return PATTERN.matcher(ip).matches();
    }
}
