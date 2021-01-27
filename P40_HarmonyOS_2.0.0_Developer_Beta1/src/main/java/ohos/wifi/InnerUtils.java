package ohos.wifi;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class InnerUtils {
    private static final int ASCII_DOUBLE_QUOTES = 34;
    private static final int BSSID_LENGTH_HAVE_SPLITTER = 17;
    private static final int BSSID_LENGTH_NO_SPLITTER = 12;
    public static final String ENHANCER_INTERFACE_TOKEN = "ohos.wifi.IWifiEnhancer";
    public static final int ERROR_SECURITY_PERM = 73465857;
    private static final int EXTRACT_HAVE_SPLITTER_LENGTH = 6;
    private static final int EXTRACT_NO_SPLITTER_LENGTH = 4;
    private static final int HASH_DISPLAY_LENGTH = 8;
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    public static final String HOTSPOT_INTERFACE_TOKEN = "ohos.wifi.IWifiHotspot";
    private static final HiLogLabel LABEL = new HiLogLabel(3, LOG_ID_WIFI, "InnerUtils");
    public static final int LOG_ID_ENHANCER = 218109282;
    public static final int LOG_ID_HOTSPOT = 218109281;
    public static final int LOG_ID_WIFI = 218109280;
    private static final int MAX_2G_FREQUENCY = 2500;
    private static final int MAX_5G_FREQUENCY = 5900;
    private static final int MIN_2G_FREQUENCY = 2400;
    private static final int MIN_5G_FREQUENCY = 4900;
    public static final int MSG_ADD_DEVICE_CONFIG = 9;
    public static final int MSG_CONNECT_TO_DEVICE = 5;
    public static final int MSG_GET_IP_INFO = 6;
    public static final int MSG_GET_LINKED_INFO = 4;
    public static final int MSG_GET_POWER_STATE = 1;
    public static final int MSG_GET_SCAN_INFO = 3;
    public static final int MSG_REMOVE_DEVICE = 7;
    public static final int MSG_SET_POWER_STATE = 0;
    public static final int MSG_WIFI_DISCONNECT = 8;
    public static final int MSG_WIFI_SCAN = 2;
    public static final String P2P_INTERFACE_TOKEN = "ohos.wifi.p2p.IWifiP2p";
    private static final int RSSI_LEVEL_1_2G = -88;
    private static final int RSSI_LEVEL_1_5G = -85;
    private static final int RSSI_LEVEL_2_2G = -82;
    private static final int RSSI_LEVEL_2_5G = -79;
    private static final int RSSI_LEVEL_3_2G = -75;
    private static final int RSSI_LEVEL_3_5G = -72;
    private static final int RSSI_LEVEL_4_2G = -65;
    private static final int RSSI_LEVEL_4_5G = -65;
    private static final int SHA256_HASH_LENGTH = 64;
    private static final String SSID_HASH_METHOD = "SHA-256";
    private static final int SSID_HEAD_LENGTH = 2;
    private static final int SSID_IN_QUOTES_HEAD_LENGTH = 4;
    private static final int SSID_MAX_LENGTH = 32;
    public static final String WIFI_INTERFACE_TOKEN = "ohos.wifi.IWifiDevice";
    public static final int WIFI_STATE_INACTIVE = 3;
    public static final int WIFI_STATE_UNKNOWN = 4;
    private static MessageDigest sDigest = null;

    public static int calBand(int i) {
        if (i <= 2400 || i >= 2500) {
            return (i <= MIN_5G_FREQUENCY || i >= MAX_5G_FREQUENCY) ? -1 : 1;
        }
        return 0;
    }

    public static int getSignalLevel(int i, int i2) {
        int i3;
        int i4;
        int i5;
        if (i2 == 1) {
            i3 = RSSI_LEVEL_3_5G;
            i5 = RSSI_LEVEL_2_5G;
            i4 = RSSI_LEVEL_1_5G;
        } else {
            i3 = RSSI_LEVEL_3_2G;
            i5 = RSSI_LEVEL_2_2G;
            i4 = RSSI_LEVEL_1_2G;
        }
        if (-65 <= i) {
            return 4;
        }
        if (-66 >= i && i3 <= i) {
            return 3;
        }
        if (i3 - 1 < i || i5 > i) {
            return (i5 - 1 < i || i4 > i) ? 0 : 1;
        }
        return 2;
    }

    private InnerUtils() {
    }

    public static int calSecurityType(String str) {
        if (str.contains("SAE")) {
            return 4;
        }
        if (str.contains("PSK")) {
            return 2;
        }
        if (str.contains("SUITE_B_192")) {
            return 5;
        }
        if (str.contains("EAP")) {
            return 3;
        }
        if (str.contains("WEP")) {
            return 1;
        }
        return str.contains("OWE") ? 6 : 0;
    }

    public static String safeDisplayBssid(String str) {
        if (str == null) {
            return "null";
        }
        int length = str.length();
        if (length == 12) {
            return str.substring(0, 4) + "****" + str.substring(length - 4, length);
        } else if (length != 17) {
            return "******";
        } else {
            return str.substring(0, 6) + "**:**" + str.substring(length - 6, length);
        }
    }

    public static String safeDisplaySsid(String str) {
        byte[] digest;
        if (!(str == null || str.length() == 0 || str.length() >= 32)) {
            if (str.length() <= 2) {
                return str;
            }
            if (str.charAt(0) == '\"' && str.charAt(str.length() - 1) == '\"') {
                if (str.length() <= 4) {
                    return str;
                }
                str = str.substring(1, str.length() - 1);
            }
            try {
                sDigest = MessageDigest.getInstance("SHA-256");
                if (!(sDigest == null || (digest = sDigest.digest(str.getBytes("UTF-8"))) == null)) {
                    if (digest.length != 0) {
                        String covertByteToHexString = covertByteToHexString(digest);
                        if (covertByteToHexString != null) {
                            if (covertByteToHexString.length() == 64) {
                                return str.substring(0, 2) + covertByteToHexString.substring(0, 8);
                            }
                        }
                    }
                }
                return "null";
            } catch (NoSuchAlgorithmException unused) {
                HiLog.warn(LABEL, "sDigest initialize fail", new Object[0]);
                sDigest = null;
            } catch (UnsupportedEncodingException unused2) {
                HiLog.warn(LABEL, "Encoding fail", new Object[0]);
            }
        }
        return "null";
    }

    private static String covertByteToHexString(byte[] bArr) {
        char[] cArr = new char[(bArr.length * 2)];
        int i = 0;
        for (byte b : bArr) {
            int i2 = i + 1;
            char[] cArr2 = HEX_DIGITS;
            cArr[i] = cArr2[(b >>> 4) & 15];
            i = i2 + 1;
            cArr[i2] = cArr2[b & 15];
        }
        return new String(cArr);
    }

    public static String getCaller(Context context) {
        return (context == null || context.getAbilityInfo() == null) ? "ohos" : context.getAbilityInfo().getBundleName();
    }
}
