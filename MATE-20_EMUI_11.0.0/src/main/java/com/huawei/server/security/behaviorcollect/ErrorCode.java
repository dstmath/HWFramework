package com.huawei.server.security.behaviorcollect;

public interface ErrorCode {
    public static final int CODE_SUCCESS = 0;
    public static final int ERROR_AUTH_REMOTE_EXCEPTION = -5;
    public static final int ERROR_BEHAVIOR_DATA = -6;
    public static final int ERROR_BEHAVIOR_MODEL = -7;
    public static final int ERROR_BIND_ATUH_SERVICE = -4;
    public static final int ERROR_FAILED_BIND_SERVICE = -10;
    public static final int ERROR_ILLEGAL_PARAMETER = -1;
    public static final int ERROR_NOT_INITIAL = -9;
    public static final int ERROR_PACKAGE_INCLUDE = -2;
    public static final int ERROR_PACKAGE_NOT_INCLUDE = -3;
    public static final int ERROR_UNMARSHAL_DATA = -8;
}
