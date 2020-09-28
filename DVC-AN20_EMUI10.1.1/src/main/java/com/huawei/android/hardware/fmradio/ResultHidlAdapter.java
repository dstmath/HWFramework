package com.huawei.android.hardware.fmradio;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public final class ResultHidlAdapter {
    public static final int FAILED = 1;
    public static final int INVALID_ARGUMENTS = 3;
    public static final int INVALID_STATE = 4;
    public static final int NOT_INITIALIZED = 2;
    public static final int OK = 0;
    public static final int TIMEOUT = 5;

    private ResultHidlAdapter() {
    }
}
