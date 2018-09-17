package android.nfc;

import android.net.wifi.WifiManager;

public class ErrorCodes {
    public static final int ERROR_BUFFER_TO_SMALL = -12;
    public static final int ERROR_BUSY = -4;
    public static final int ERROR_CANCELLED = -2;
    public static final int ERROR_CONNECT = -5;
    public static final int ERROR_DISCONNECT = -5;
    public static final int ERROR_INSUFFICIENT_RESOURCES = -9;
    public static final int ERROR_INVALID_PARAM = -8;
    public static final int ERROR_IO = -1;
    public static final int ERROR_NFC_ON = -16;
    public static final int ERROR_NOT_INITIALIZED = -17;
    public static final int ERROR_NOT_SUPPORTED = -21;
    public static final int ERROR_NO_SE_CONNECTED = -20;
    public static final int ERROR_READ = -6;
    public static final int ERROR_SAP_USED = -13;
    public static final int ERROR_SERVICE_NAME_USED = -14;
    public static final int ERROR_SE_ALREADY_SELECTED = -18;
    public static final int ERROR_SE_CONNECTED = -19;
    public static final int ERROR_SOCKET_CREATION = -10;
    public static final int ERROR_SOCKET_NOT_CONNECTED = -11;
    public static final int ERROR_SOCKET_OPTIONS = -15;
    public static final int ERROR_TIMEOUT = -3;
    public static final int ERROR_WRITE = -7;
    public static final int SUCCESS = 0;

    public static boolean isError(int code) {
        if (code < 0) {
            return true;
        }
        return false;
    }

    public static String asString(int code) {
        switch (code) {
            case -21:
                return "NOT_SUPPORTED";
            case -20:
                return "NO_SE_CONNECTED";
            case -19:
                return "SE_CONNECTED";
            case -18:
                return "SE_ALREADY_SELECTED";
            case -17:
                return "NOT_INITIALIZED";
            case -16:
                return "NFC_ON";
            case -15:
                return "SOCKET_OPTIONS";
            case -14:
                return "SERVICE_NAME_USED";
            case -13:
                return "SAP_USED";
            case -12:
                return "BUFFER_TO_SMALL";
            case -11:
                return "SOCKET_NOT_CONNECTED";
            case -10:
                return "SOCKET_CREATION";
            case -9:
                return "INSUFFICIENT_RESOURCES";
            case -8:
                return "INVALID_PARAM";
            case -7:
                return "WRITE";
            case -6:
                return "READ";
            case -5:
                return "CONNECT/DISCONNECT";
            case -4:
                return "BUSY";
            case -3:
                return "TIMEOUT";
            case -2:
                return "CANCELLED";
            case -1:
                return "IO";
            case 0:
                return WifiManager.PPPOE_RESULT_SUCCESS;
            default:
                return "UNKNOWN ERROR";
        }
    }
}
