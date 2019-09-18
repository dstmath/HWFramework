package com.android.server.mtm.iaware.appmng.appclean;

import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.content.Context;
import android.rms.iaware.AwareLog;

public class AppCleaner {
    private static final String TAG = "AppCleaner";
    private static volatile AppCleaner mAppCleaner;
    private Context mContext;

    /* renamed from: com.android.server.mtm.iaware.appmng.appclean.AppCleaner$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource = new int[AppMngConstant.AppCleanSource.values().length];

        static {
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[AppMngConstant.AppCleanSource.SMART_CLEAN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[AppMngConstant.AppCleanSource.SYSTEM_MANAGER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[AppMngConstant.AppCleanSource.POWER_GENIE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[AppMngConstant.AppCleanSource.THERMAL.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
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

    public void requestAppClean(AppMngConstant.AppCleanSource config) {
        if (config == null) {
            AwareLog.e(TAG, "requestAppClean source = null");
        } else if (AnonymousClass1.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[config.ordinal()] != 1) {
            AwareLog.e(TAG, "bad request: no source for " + config);
        } else {
            new SmartClean(this.mContext).clean();
        }
    }

    public void requestAppCleanWithCallback(AppCleanParam param, IAppCleanCallback callback) {
        CleanSource source;
        if (param != null) {
            int sourceCode = param.getSource();
            if (sourceCode < 0 || sourceCode > AppMngConstant.AppCleanSource.values().length) {
                AwareLog.e(TAG, "bad request: invalid source = " + sourceCode);
                return;
            }
            switch (AnonymousClass1.$SwitchMap$android$app$mtm$iaware$appmng$AppMngConstant$AppCleanSource[AppMngConstant.AppCleanSource.values()[sourceCode].ordinal()]) {
                case 2:
                    source = new HSMClean(param, callback, this.mContext);
                    break;
                case 3:
                    source = new PGClean(param, callback, this.mContext);
                    break;
                case 4:
                    source = new ThermalClean(param, callback, this.mContext);
                    break;
                default:
                    AwareLog.e(TAG, "bad request: no source for " + sourceCode);
                    return;
            }
            source.clean();
        } else {
            AwareLog.e(TAG, "bad request: param is null");
        }
    }
}
