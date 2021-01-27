package com.huawei.nb.coordinator.helper.http;

public final class ResultStatusCodeEnum {
    public static final int CONNECT_CLOUD_ERROR = -9;
    public static final int CONTENT_LENGTH_NOT_VALID = -18;
    public static final int DEFAULT_STATUS_CODE = -1;
    public static final int DISTRICT_OVERSEA = -14;
    public static final int DOWNLOAD_BREAKPOINT_FILE_ERROR = -15;
    public static final int DOWNLOAD_ERROR = -5;
    public static final int DUPLICATED_REQUEST = -17;
    public static final int EMPTY_SIGNATURE_ERROR_CODE = -8;
    public static final int GET_HEADER_ERROR = -16;
    public static final int GET_SIGNATURE_ERROR = -12;
    public static final int INTERRUPT_CODE = -7;
    public static final int INVALID_PARAMS_CODE = -2;
    public static final int NETWORK_FAULT = -11;
    public static final int NETWORK_NOT_CONNECTED = -6;
    public static final int NOT_ALLOWED_TO_CONNECT = -4;
    public static final int OPERATE_FILE_ERROR = -19;
    public static final int PARTIAL_DOWNLOAD_SUCCESS = 206;
    public static final int REQUEST_RANGE_NOT_SATISFIABLE = 416;
    public static final int SHIELD_CLOUD_ERROR = -10;
    public static final int SUCCESS = 200;
    public static final int TIME_OUT = -13;
    public static final int VERIFICATION_ERROR_CODE = -3;

    private ResultStatusCodeEnum() {
    }
}
