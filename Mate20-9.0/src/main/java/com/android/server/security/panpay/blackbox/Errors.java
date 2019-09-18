package com.android.server.security.panpay.blackbox;

import android.text.TextUtils;
import android.util.SparseArray;

public class Errors {
    public static final int ERR_APPLETAID_NULL = 100006;
    public static final int ERR_APPLET_PARAMS_NULL = 100002;
    public static final int ERR_APPLET_VERSION_NULL = 100007;
    public static final int ERR_COMMONREQUEST_NULL = 100001;
    public static final int ERR_CPLC_NULL = 100008;
    public static final int ERR_FUNCTIONID_NULL = 100004;
    public static final int ERR_NETWORK_ERR = 100010;
    public static final int ERR_NETWORK_NOT_AVAILABLE = 4;
    public static final int ERR_NFC_CLOSED = 6;
    public static final int ERR_NO_NETWORK = 3;
    public static final int ERR_PARA_ERR = 1;
    public static final int ERR_RESPONSE_ANALYSIS_ERR = 100012;
    public static final int ERR_SERVER_ERR = 100013;
    public static final int ERR_SERVICEID_NULL = 100003;
    public static final int ERR_SMARDCARD_OPERATION_ERR = 100009;
    public static final int ERR_SPECIAL_ERR = 100011;
    public static final int ERR_SSDAID_NULL = 100005;
    public static final int ERR_TSM_OPERATOR_WRONG = 5;
    private static SparseArray<String> errList = new SparseArray<>();

    static {
        errList.put(1, "params error");
        errList.put(3, "no network");
        errList.put(4, "network is not available");
        errList.put(5, "tsm operator wrong");
        errList.put(6, "nfc is closed");
        errList.put(ERR_COMMONREQUEST_NULL, "commonrequest is null");
        errList.put(ERR_APPLET_PARAMS_NULL, "applet params is null");
        errList.put(ERR_SERVICEID_NULL, "serviceId is null");
        errList.put(ERR_FUNCTIONID_NULL, "functionId is null");
        errList.put(ERR_SSDAID_NULL, "SSDAID is null");
        errList.put(ERR_APPLETAID_NULL, "APPLETAID is null");
        errList.put(ERR_APPLET_VERSION_NULL, "APPLET version is null");
        errList.put(ERR_CPLC_NULL, "cplc is null");
        errList.put(100009, "SMARDCARD operation error");
        errList.put(100010, "network error");
        errList.put(100011, "special error");
        errList.put(100012, "response analysis error");
        errList.put(100013, "server error");
    }

    public static String toInfo(int id) {
        return errList.get(id, "unexpected err");
    }

    public static String toInfo(int id, String info) {
        if (TextUtils.isEmpty(info)) {
            info = "null";
        }
        return errList.get(id, "unexpected err") + ": " + info;
    }
}
