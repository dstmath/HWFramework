package com.huawei.networkit.grs.common;

import android.text.TextUtils;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class StringUtils {
    private static final String TAG = "StringUtils";

    public static boolean strEquals(String first, String second) {
        return first == second || (first != null && first.equals(second));
    }

    public static String byte2Str(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.w("StringUtils.byte2str error: UnsupportedEncodingException", e);
            return "";
        }
    }

    public static byte[] str2Byte(String text) {
        if (TextUtils.isEmpty(text)) {
            return new byte[0];
        }
        try {
            return text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Logger.w("StringUtils.str2Byte error: UnsupportedEncodingException", e);
            return new byte[0];
        }
    }

    public static String anonymizeMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            return message;
        }
        char[] messageChars = message.toCharArray();
        for (int i = 0; i < messageChars.length; i++) {
            if (i % 2 == 1) {
                messageChars[i] = '*';
            }
        }
        return new String(messageChars);
    }

    public static byte[] getBytes(long content) {
        return getBytes(String.valueOf(content));
    }

    public static byte[] getBytes(String content) {
        byte[] bytes = new byte[0];
        if (content == null) {
            return bytes;
        }
        try {
            return content.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            Logger.w(TAG, "the content has error while it is converted to bytes");
            return bytes;
        }
    }

    public static String format(String format, Object... objects) {
        if (format == null) {
            return "";
        }
        return String.format(Locale.ROOT, format, objects);
    }

    public static String toLowerCase(String source) {
        if (source == null) {
            return source;
        }
        return source.toLowerCase(Locale.ROOT);
    }
}
