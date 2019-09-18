package com.android.server.hidata.hicure;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.server.hidata.appqoe.HwAPPQoEResourceManger;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import java.util.Objects;

public class HwHiCureActivityObserver {
    public static final int APP_STATE_BACKGROUND = 1;
    public static final int APP_STATE_FOREGROUND = 0;
    private static final int EVENT_ACTIVITY_STATE_CHANGE = 0;
    public static final String TAG = "HwHiCureActivityObserver";
    private static HwHiCureActivityObserver mHwHiCureActivityObserver = null;
    private IHwActivityNotifierEx mActivityNotifierEx = new IHwActivityNotifierEx() {
        public void call(Bundle extras) {
            if (extras == null) {
                Log.d(HwHiCureActivityObserver.TAG, "AMS callback , extras=null");
            } else if ("onResume".equals(extras.getString("state"))) {
                HiCureAppInfo hiCureAppInfo = new HiCureAppInfo();
                ComponentName componentName = (ComponentName) extras.getParcelable("comp");
                hiCureAppInfo.mPackageName = componentName != null ? componentName.getPackageName() : "";
                hiCureAppInfo.mAppUID = extras.getInt("uid");
                HwHiCureActivityObserver.this.mHandler.sendMessage(HwHiCureActivityObserver.this.mHandler.obtainMessage(0, hiCureAppInfo));
            }
        }
    };
    private Context mContext = null;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(HwHiCureActivityObserver.TAG, "handleMessage: msg[" + msg.what + "]");
            if (msg.what == 0) {
                HwHiCureActivityObserver.this.processActivityStateChange((HiCureAppInfo) msg.obj);
            }
        }
    };
    private HwHiCureStateMonitor mHwHiCureStateMonitor = null;
    private HiCureAppInfo mLastAppInfo = null;

    public static class HiCureAppInfo {
        public int mAppUID;
        public String mPackageName;

        public String toString() {
            return "PackageName[" + this.mPackageName + "] mAppUID[" + this.mAppUID + "]";
        }
    }

    private HwHiCureActivityObserver(Context context) {
        this.mContext = context;
        this.mHwHiCureStateMonitor = HwHiCureStateMonitor.createHwHiCureStateMonitor(context);
        this.mHwHiCureStateMonitor.startMonitor();
        ActivityManagerEx.registerHwActivityNotifier(this.mActivityNotifierEx, "activityLifeState");
    }

    public static synchronized HwHiCureActivityObserver createHwHiCureActivityObserver(Context context) {
        HwHiCureActivityObserver hwHiCureActivityObserver;
        synchronized (HwHiCureActivityObserver.class) {
            if (mHwHiCureActivityObserver == null) {
                mHwHiCureActivityObserver = new HwHiCureActivityObserver(context);
            }
            hwHiCureActivityObserver = mHwHiCureActivityObserver;
        }
        return hwHiCureActivityObserver;
    }

    /* access modifiers changed from: private */
    public void processActivityStateChange(HiCureAppInfo hiCureAppInfo) {
        if (hiCureAppInfo == null || hiCureAppInfo.mPackageName == null) {
            Log.d(TAG, "processActivityStateChange: invalid app info");
            return;
        }
        String curPackageName = hiCureAppInfo.mPackageName;
        if (this.mLastAppInfo == null || !Objects.equals(curPackageName, this.mLastAppInfo.mPackageName)) {
            if (HwAPPQoEResourceManger.getInstance().checkIsMonitorHiCureAppScence(curPackageName)) {
                this.mHwHiCureStateMonitor.sendActvityStateChanged(0, hiCureAppInfo);
                this.mLastAppInfo = hiCureAppInfo;
            } else if (this.mLastAppInfo != null) {
                this.mHwHiCureStateMonitor.sendActvityStateChanged(1, this.mLastAppInfo);
                this.mLastAppInfo = null;
            }
            return;
        }
        Log.d(TAG, "processActivityStateChange: app is not changed");
    }
}
