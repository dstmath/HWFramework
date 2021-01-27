package com.huawei.dmsdpsdk2.devicevirtualization;

public class ErrorCode {
    public static final int DEVICE_INIT_FAILED = -15;
    public static final int DEVICE_NOT_CONNECTED = -14;
    public static final int DEVICE_SERVICE_START_FAILED = -12;
    public static final int DEVICE_SERVICE_STOP_FAILED = -13;
    public static final int DEVICE_SET_FAILED = -11;
    public static final int INVALID_ARGUMENT = -2;
    public static final int LISTENER_ALREADY_REGISTERED = -6;
    public static final int LISTENER_NOT_REGISTERED = -4;
    public static final int REMOTE_EXCEPTION = -3;
    public static final int SERVICE_EXECUTE_FAILED = -1;
    public static final int SERVICE_NOT_EXIST = -9;
    public static final int SERVICE_NOT_IMPLEMENT = -7;
    public static final int SERVICE_UNAVAILABLE = -10;
    public static final int SUCCESS = 0;

    private ErrorCode() {
    }
}
