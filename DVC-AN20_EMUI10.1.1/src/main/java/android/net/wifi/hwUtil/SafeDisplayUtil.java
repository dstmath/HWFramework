package android.net.wifi.hwUtil;

import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.HexDump;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SafeDisplayUtil {
    private static final int ASCII_DOUBLE_QUOTES = 34;
    private static final int BSSID_LENGTH_HAVE_SPLITTER = 17;
    private static final int BSSID_LENGTH_NO_SPLITTER = 12;
    private static final int EXTRACT_HAVE_SPLITTER_LENGTH = 6;
    private static final int EXTRACT_NO_SPLITTER_LENGTH = 4;
    private static final int HASH_DISPLAY_LENGTH = 8;
    private static final int SHA256_HASH_LENGTH = 64;
    private static final String SSID_HASH_METHOD = "SHA-256";
    private static final int SSID_HEAD_LENGTH = 2;
    private static final int SSID_IN_QUOTES_HEAD_LENGTH = 4;
    private static final int SSID_MAX_LENGTH = 32;
    private static final String TAG = "SafeDisplayUtil";
    private static MessageDigest sDigest = null;

    private SafeDisplayUtil() {
    }

    public static String safeDisplayBssid(String srcBssid) {
        if (srcBssid == null) {
            return "null";
        }
        int len = srcBssid.length();
        if (len == 12) {
            return srcBssid.substring(0, 4) + "****" + srcBssid.substring(len - 4, len);
        } else if (len != 17) {
            return "******";
        } else {
            return srcBssid.substring(0, 6) + "**:**" + srcBssid.substring(len - 6, len);
        }
    }

    public static String safeDisplaySsid(String srcSsid) {
        byte[] ssidHashBytes;
        String ssid = srcSsid;
        if (TextUtils.isEmpty(ssid) || ssid.length() >= 32) {
            return "null";
        }
        if (ssid.length() <= 2) {
            return ssid;
        }
        if (ssid.charAt(0) == '\"' && ssid.charAt(ssid.length() - 1) == '\"') {
            if (ssid.length() <= 4) {
                return ssid;
            }
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        try {
            sDigest = MessageDigest.getInstance("SHA-256");
            if (!(sDigest == null || (ssidHashBytes = sDigest.digest(ssid.getBytes("UTF-8"))) == null)) {
                if (ssidHashBytes.length != 0) {
                    String ssidHashString = HexDump.toHexString(ssidHashBytes);
                    if (ssidHashString != null) {
                        if (ssidHashString.length() == 64) {
                            return ssid.substring(0, 2) + ssidHashString.substring(0, 8);
                        }
                    }
                    return "null";
                }
            }
            return "null";
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "sDigest initialize fail");
            sDigest = null;
        } catch (UnsupportedEncodingException e2) {
            Log.e(TAG, "Encoding fail");
        }
        return "null";
    }
}
