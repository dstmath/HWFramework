package com.huawei.ace.activity;

import android.app.Application;
import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.DeviceInfoHelper;
import com.huawei.ace.runtime.LibraryLoader;

public class AceApplication extends Application {
    private static final String LOG_TAG = "AceApplication";
    private static Thread loadSoThread;

    @Override // android.app.Application
    public void onCreate() {
        ALog.setLogger(new Logger());
        ALog.i(LOG_TAG, "AceApplication::onCreate called");
        if (DeviceInfoHelper.isWatchType(getApplicationContext())) {
            LibraryLoader.loadPreloadLibrary();
        }
        super.onCreate();
    }
}
