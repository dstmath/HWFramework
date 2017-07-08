package tmsdk.common;

import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class ErrorCode {
    public static final int ERR_ARGUMENT = -6;
    public static final int ERR_CANCEL = -3;
    public static final int ERR_CONNECT_FORBID = -64;
    public static final int ERR_CORRECTION_BAD_SMS = -10003;
    public static final int ERR_CORRECTION_FEEDBACK_UPLOAD_FAIL = -10002;
    public static final int ERR_CORRECTION_LOCAL_NO_TEMPLATE = -10005;
    public static final int ERR_CORRECTION_LOCAL_TEMPLATE_UNMATCH = -10007;
    public static final int ERR_CORRECTION_PROFILE_ILLEGAL = -10006;
    public static final int ERR_CORRECTION_PROFILE_UPLOAD_FAIL = -10004;
    public static final int ERR_FILE_OP = -7000;
    public static final int ERR_GENERAL = -2;
    public static final int ERR_GET = -3000;
    public static final int ERR_ILLEGAL_ACCESS = -60;
    public static final int ERR_ILLEGAL_ARG = -57;
    public static final int ERR_ILLEGAL_STATE = -61;
    public static final int ERR_IO_EXCEPTION = -56;
    public static final int ERR_LICENSE_EXPIRED = -99999;
    public static final int ERR_MERGE_DIFF_OP = -8000;
    public static final int ERR_NONE = 0;
    public static final int ERR_NOT_COMPLETED = -7;
    public static final int ERR_NOT_FOUND = -1;
    public static final int ERR_NO_CONNECTION = -52;
    public static final int ERR_OPEN_CONNECTION = -1000;
    public static final int ERR_POST = -2000;
    public static final int ERR_PROTOCOL = -51;
    public static final int ERR_RECEIVE = -5000;
    public static final int ERR_RESPONSE = -4000;
    public static final int ERR_SECURITY = -58;
    public static final int ERR_SHARKNET_BASE = -65;
    public static final int ERR_SOCKET = -54;
    public static final int ERR_SOCKET_TIMEOUT = -55;
    public static final int ERR_UNKNOWN_HOST = -62;
    public static final int ERR_UNSUPPORTED_OP = -59;
    public static final int ERR_URL_MALFORMED = -53;
    public static final int ERR_WIFI_AUTHENTICATION = -63;
    public static final int ERR_WUP = -6000;
    public static final int WIFICONN_CHECK_FAILED = -10101;
    public static final int WIFICONN_CHECK_SUCCESS = -10102;
    public static final int WIFICONN_CONFIG_INVALID = -10106;
    public static final int WIFICONN_CONNECT_SUCCESS = -10105;
    public static final int WIFICONN_CONNECT_TIMEOUT = -10110;
    public static final int WIFICONN_CONNECT_UNKNOWN_ERROR = -10111;
    public static final int WIFICONN_PASSWORD_ERROR = -10109;
    public static final int WIFICONN_ROUTER_ABNORMAL = -10107;
    public static final int WIFICONN_ROUTER_OVERLOAD = -10108;
    public static final int WIFICONN_WIFI_DISABLED = -10112;
    public static final int WIFICONN_WIFI_INTERRUPT_BY_NEW_CONNECTION = -10113;

    public static int fromESharkCode(int i) {
        d.g("QQPimSecure", "ESharkCod = " + i);
        return i != 0 ? ERR_SHARKNET_BASE : ERR_NONE;
    }
}
