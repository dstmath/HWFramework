package com.android.server.mtm.iaware.appmng.appclean;

import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppMngConstant.AppCleanSource;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.content.Context;
import android.rms.iaware.AwareLog;

public class AppCleaner {
    private static final /* synthetic */ int[] -android-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues = null;
    private static final String TAG = "AppCleaner";
    private static volatile AppCleaner mAppCleaner;
    private Context mContext;

    private static /* synthetic */ int[] -getandroid-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues() {
        if (-android-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues != null) {
            return -android-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues;
        }
        int[] iArr = new int[AppCleanSource.values().length];
        try {
            iArr[AppCleanSource.COMPACT.ordinal()] = 4;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppCleanSource.CRASH.ordinal()] = 5;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppCleanSource.MEMORY.ordinal()] = 6;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppCleanSource.MEMORY_REPAIR.ordinal()] = 7;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppCleanSource.POWER_GENIE.ordinal()] = 1;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppCleanSource.SMART_CLEAN.ordinal()] = 2;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[AppCleanSource.SYSTEM_MANAGER.ordinal()] = 3;
        } catch (NoSuchFieldError e7) {
        }
        -android-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues = iArr;
        return iArr;
    }

    public static AppCleaner getInstance(Context context) {
        if (mAppCleaner == null) {
            synchronized (AppCleaner.class) {
                if (mAppCleaner == null && context != null) {
                    mAppCleaner = new AppCleaner(context);
                }
            }
        }
        return mAppCleaner;
    }

    private AppCleaner(Context context) {
        this.mContext = context;
    }

    public void requestAppClean(AppCleanSource config) {
        if (config == null) {
            AwareLog.e(TAG, "requestAppClean source = null");
            return;
        }
        switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues()[config.ordinal()]) {
            case 2:
                new SmartClean(this.mContext).clean();
                return;
            default:
                AwareLog.e(TAG, "bad request: no source for " + config);
                return;
        }
    }

    public void requestAppCleanWithCallback(AppCleanParam param, IAppCleanCallback callback) {
        if (param == null) {
            AwareLog.e(TAG, "bad request: param is null");
        } else if (param.getSource() >= 0) {
            CleanSource source;
            switch (-getandroid-app-mtm-iaware-appmng-AppMngConstant$AppCleanSourceSwitchesValues()[AppCleanSource.values()[param.getSource()].ordinal()]) {
                case 1:
                    source = new PGClean(param, callback, this.mContext);
                    break;
                case 3:
                    source = new HSMClean(param, callback, this.mContext);
                    break;
                default:
                    AwareLog.e(TAG, "bad request: no source for param.source!");
                    return;
            }
            source.clean();
        }
    }
}
