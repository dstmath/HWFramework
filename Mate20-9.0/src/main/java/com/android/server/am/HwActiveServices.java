package com.android.server.am;

public final class HwActiveServices extends ActiveServices {
    public static final boolean DEBUG = false;
    static final String EXCLUDE_PROCESS = "com.huawei.android.pushagent.PushService";
    static final String TAG = "HwActiveServices";

    public HwActiveServices(ActivityManagerService service) {
        super(service);
    }
}
