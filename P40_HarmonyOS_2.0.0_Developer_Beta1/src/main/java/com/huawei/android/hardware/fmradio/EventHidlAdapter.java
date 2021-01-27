package com.huawei.android.hardware.fmradio;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public final class EventHidlAdapter {
    public static final int FM_READY = 0;
    public static final int RDS_AVAL = 5;
    public static final int SEARCH_CANCELLED = 4;
    public static final int SEARCH_COMPLETE = 3;
    public static final int SEARCH_IN_PROGRESS = 2;
    public static final int TUNE = 1;

    private EventHidlAdapter() {
    }
}
