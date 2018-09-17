package com.android.server.security.tsmagent.openapi;

public interface ITSMOperator {
    public static final String OPERATOR_TYPE_CREATE_SSD = "createSSD";
    public static final String OPERATOR_TYPE_DELETE_SSD = "deleteSSD";
    public static final int RETURN_FAILED_CONN_UNAVAILABLE = 4;
    public static final int RETURN_FAILED_NO_NETWORK = 3;
    public static final int RETURN_FAILED_UNKNOWN_ERROR = -99;
    public static final int RETURN_INVALID_CALLER_SIGN = 2;
    public static final int RETURN_INVALID_PARAMS = 1;
    public static final int RETURN_NFC_CLOSE = 6;
    public static final int RETURN_NOT_SUPPORT_FEATURE = -1;
    public static final int RETURN_PRE_CHECK_OK = 0;
    public static final int RETURN_SUCCESS = 0;
    public static final int RETURN_TSM_ERROR = 5;

    int createSSD(String str, String str2, String str3, String str4, String str5);

    int deleteSSD(String str, String str2, String str3, String str4, String str5);

    String getCplc(String str);

    int initEse(String str, String str2, String str3, String str4);
}
