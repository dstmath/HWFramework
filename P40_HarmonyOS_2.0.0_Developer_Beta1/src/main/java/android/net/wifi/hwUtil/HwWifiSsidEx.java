package android.net.wifi.hwUtil;

import android.os.Parcel;
import android.os.SystemProperties;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

public class HwWifiSsidEx {
    private static final boolean IS_ENC_KR = SystemProperties.getBoolean("ro.config.wifi_use_euc-kr", false);
    public static final String NONE = "<unknown ssid>";
    private static final String TAG = "HwWifiSsidEx";

    public static String encodingWithCharset(byte[] ssidBytes) {
        if (ssidBytes == null) {
            return "";
        }
        String[] charsets = {"UTF-8", "GBK", "US-ASCII", "UTF-16"};
        if (IS_ENC_KR) {
            charsets[1] = "EUC-KR";
        }
        for (int i = 0; i < charsets.length; i++) {
            if (isEncodedWithCharset(ssidBytes, charsets[i])) {
                return encodingWithCharset(ssidBytes, charsets[i]);
            }
        }
        return encodingWithCharset(ssidBytes, "UTF-8");
    }

    public static String decodeWithCharsets(byte[] hexByteArray) {
        if (hexByteArray == null) {
            return "";
        }
        String[] encodeCharsets = {"UTF-8", "GBK", "US-ASCII", "UTF-16"};
        if (IS_ENC_KR) {
            encodeCharsets[1] = "EUC-KR";
        }
        for (String currentCharsets : encodeCharsets) {
            if (isEncodedWithCharset(hexByteArray, currentCharsets)) {
                return encodingWithCharset(hexByteArray, currentCharsets);
            }
        }
        return "";
    }

    private static boolean isEncodedWithCharset(byte[] buff, String charsetName) {
        try {
            if (Charset.isSupported(charsetName)) {
                byte[] newBuff = new String(buff, charsetName).getBytes(charsetName);
                if (buff.length == newBuff.length) {
                    for (int i = 0; i < buff.length; i++) {
                        if (buff[i] != newBuff[i]) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "get bytes fail");
        }
        return false;
    }

    private static String encodingWithCharset(byte[] buff, String charsetName) {
        try {
            CharsetDecoder decoder = Charset.forName(charsetName).newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
            CharBuffer out = CharBuffer.allocate(buff.length * 5);
            CoderResult result = decoder.decode(ByteBuffer.wrap(buff), out, true);
            out.flip();
            if (result.isError()) {
                return "<unknown ssid>";
            }
            return out.toString();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "encodingWithCharset, illegal argument");
            return "<unknown ssid>";
        }
    }

    public static void writeOriSsid(String oriSsid, Parcel dest) {
        try {
            byte[] buff = oriSsid.getBytes("UTF-8");
            dest.writeInt(buff.length);
            if (buff.length > 0) {
                dest.writeByteArray(buff);
            }
        } catch (UnsupportedEncodingException e) {
            dest.writeInt(0);
        }
    }

    public static String readOriSsid(Parcel in) {
        int oriSsidLen = in.readInt();
        if (oriSsidLen <= 0) {
            return "";
        }
        byte[] buff = new byte[oriSsidLen];
        in.readByteArray(buff);
        try {
            return new String(buff, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
