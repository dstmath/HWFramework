package com.android.server.mtm.iaware.appmng;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.rms.iaware.AwareNRTConstant;
import android.rms.iaware.LogIAware;
import android.util.ArraySet;
import com.android.server.mtm.iaware.appmng.rule.AppMngRule;
import java.util.ArrayList;

public class AppBatteryStrategy {
    private static final String APP_FREEZE_PROTECT = "frz_protect";
    private static final long DELAY_UPDATE_DATA = 500;
    private static final int EVENT_APPS_STANDBY_DB_CHANGED = 0;
    private static final int EVENT_REGISTER_DB_OBSERVER = 1;
    private static final int MAIN_USER_ID = 0;
    private static final Object SLOCK = new Object();
    private static final String SMART_POWER_URI = "content://com.huawei.android.smartpowerprovider/unifiedpowerapps";
    private static final String SPLIT_FLAG = "#";
    private static final String TAG = "AppBatteryStrategy";
    private static AppBatteryStrategy sAppBatteryStrategy;
    private AppStrategyHandle mAppStrategyHandle;
    private ArraySet<String> mBatteryOptimizeApps = new ArraySet<>();
    private Context mContext;
    private HandlerThread mHandlerThread;
    private ContentObserver mStandbyDbObserver = new ContentObserver(new Handler()) {
        /* class com.android.server.mtm.iaware.appmng.AppBatteryStrategy.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            AwareLog.d(AppBatteryStrategy.TAG, "Standby database changed!");
            if (AppBatteryStrategy.this.mAppStrategyHandle == null) {
                AppBatteryStrategy.this.initHandlerThread();
            }
            AppBatteryStrategy.this.mAppStrategyHandle.removeMessages(0);
            AppBatteryStrategy.this.mAppStrategyHandle.sendMessageDelayed(AppBatteryStrategy.this.mAppStrategyHandle.obtainMessage(0), 500);
        }
    };

    private AppBatteryStrategy() {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initHandlerThread() {
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mAppStrategyHandle = new AppStrategyHandle(this.mHandlerThread.getLooper());
    }

    public void init(Context context) {
        this.mContext = context;
        initHandlerThread();
        setAppBatteryMessage(0);
        setAppBatteryMessage(1);
    }

    public static AppBatteryStrategy getInstance() {
        AppBatteryStrategy appBatteryStrategy;
        synchronized (SLOCK) {
            if (sAppBatteryStrategy == null) {
                sAppBatteryStrategy = new AppBatteryStrategy();
            }
            appBatteryStrategy = sAppBatteryStrategy;
        }
        return appBatteryStrategy;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setBatteryOptimizeApps() {
        AwareLog.i(TAG, "setBatteryOptimizeApps.");
        try {
            Bundle standbyBundle = this.mContext.getContentResolver().call(Uri.parse("content://0@com.huawei.android.smartpowerprovider"), "hsm_get_freeze_list", AppMngRule.VALUE_ALL, (Bundle) null);
            if (standbyBundle == null) {
                AwareLog.i(TAG, "updateStandbyApps,  smcs provider does not implement call.");
                return;
            }
            ArrayList<String> standbyProtectedApps = standbyBundle.getStringArrayList(APP_FREEZE_PROTECT);
            if (standbyProtectedApps != null) {
                this.mBatteryOptimizeApps.clear();
                this.mBatteryOptimizeApps.addAll(standbyProtectedApps);
                sendAppStategyToNRT(standbyProtectedApps, APP_FREEZE_PROTECT);
                AwareLog.d(TAG, "get standbyProtectedApps success");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            AwareLog.e(TAG, "getStringArrayList failed:" + e.toString());
        } catch (IllegalArgumentException e2) {
            AwareLog.e(TAG, "updateStandbyApps failed:" + e2.toString());
        }
    }

    private void sendAppStategyToNRT(ArrayList<String> appList, String strategy) {
        if (appList.isEmpty() || strategy == null) {
            AwareLog.d(TAG, "sendAppStategyToNRT appList is empty or strategy is null");
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(strategy);
        stringBuffer.append("#");
        stringBuffer.append(String.join(",", (CharSequence[]) appList.toArray(new String[appList.size()])));
        LogIAware.report(AwareNRTConstant.APP_BATTERY_STRATEGY_EVENT_ID, stringBuffer.toString());
    }

    public boolean isAppIsNeverOptimized(String appName) {
        ArraySet<String> arraySet;
        if (appName == null || (arraySet = this.mBatteryOptimizeApps) == null || !arraySet.contains(appName)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerAppStrategyObserver() {
        Uri standbyUri = Uri.parse(SMART_POWER_URI);
        if (this.mContext.getContentResolver() == null) {
            AwareLog.e(TAG, "App standby database is not exist!");
        } else {
            this.mContext.getContentResolver().registerContentObserver(standbyUri, true, this.mStandbyDbObserver);
        }
    }

    public void setAppBatteryMessage(int msgId) {
        Message msg = Message.obtain();
        msg.what = msgId;
        this.mAppStrategyHandle.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    public class AppStrategyHandle extends Handler {
        public AppStrategyHandle(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                AwareLog.w(AppBatteryStrategy.TAG, "msg is null");
                return;
            }
            int i = msg.what;
            if (i == 0) {
                AppBatteryStrategy.this.setBatteryOptimizeApps();
            } else if (i == 1) {
                AppBatteryStrategy.this.registerAppStrategyObserver();
            }
        }
    }
}
