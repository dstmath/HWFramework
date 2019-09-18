package com.android.server.hidata.appqoe;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AppTypeRecoManager;
import android.text.TextUtils;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;

public class HwAPPQoEActivityMonitor {
    private static final String[] DEFAULT_GAME_NAMES = {"com.huawei.gamebox"};
    private static final int EVENT_ACTIVITY_STATE_CHANGE = 2;
    private static final int EVENT_DELAY_TIMER_EXPIRE = 1;
    /* access modifiers changed from: private */
    public static String TAG = "HiData_HwAPPQoEActivityMonitor";
    private static HwAPPStateInfo lastAPPStateInfo = new HwAPPStateInfo();
    private static HwAPPQoEActivityMonitor mHwAPPQoEActivityMonitor = null;
    private int APP_USER_LEARNING_MONITOR;
    private long apkStartTime;
    private HwAPPQoEResourceManger hwResourceManger;
    private HwAPPQoEUserLearning hwUserLearning;
    private IHwActivityNotifierEx mActivityNotifierEx;
    private AppTypeRecoManager mAppTypeRecoManager;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private HandlerThread mHandlerThread;
    private int monitorAPPUID;

    public static class CachedAppInfo {
        public int mAppUID;
        public String mClassName;
        public String mPackageName;
    }

    private class MainHandler extends Handler {
        MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg == null || msg.obj == null) {
                HwAPPQoEUtils.logD(HwAPPQoEActivityMonitor.TAG, "handleMessage -- invalid input");
                return;
            }
            switch (msg.what) {
                case 1:
                    CachedAppInfo cachedAppInfo = (CachedAppInfo) msg.obj;
                    HwAPPQoEActivityMonitor.this.handleActivityChange(cachedAppInfo.mPackageName, cachedAppInfo.mClassName, cachedAppInfo.mAppUID, true);
                    break;
                case 2:
                    CachedAppInfo newAppInfo = (CachedAppInfo) msg.obj;
                    HwAPPQoEActivityMonitor.this.handleActivityChange(newAppInfo.mPackageName, newAppInfo.mClassName, newAppInfo.mAppUID, false);
                    break;
            }
        }
    }

    private HwAPPQoEActivityMonitor(Context context) {
        this.hwResourceManger = null;
        this.hwUserLearning = null;
        this.mHandler = null;
        this.mHandlerThread = null;
        this.apkStartTime = System.currentTimeMillis();
        this.monitorAPPUID = -1;
        this.APP_USER_LEARNING_MONITOR = 1;
        this.mActivityNotifierEx = new IHwActivityNotifierEx() {
            public void call(Bundle extras) {
                if (extras == null) {
                    HwAPPQoEUtils.logD(HwAPPQoEActivityMonitor.TAG, "AMS callback , extras=null");
                    return;
                }
                CachedAppInfo mCachedAppInfo = new CachedAppInfo();
                mCachedAppInfo.mAppUID = extras.getInt("uid");
                ComponentName componentName = (ComponentName) extras.getParcelable("comp");
                mCachedAppInfo.mClassName = componentName != null ? componentName.getClassName() : "";
                mCachedAppInfo.mPackageName = componentName != null ? componentName.getPackageName() : "";
                if ("onResume".equals(extras.getString("state"))) {
                    HwAPPQoEActivityMonitor.this.mHandler.removeMessages(1);
                    HwAPPQoEActivityMonitor.this.mHandler.sendMessage(HwAPPQoEActivityMonitor.this.mHandler.obtainMessage(2, mCachedAppInfo));
                }
            }
        };
        this.hwResourceManger = HwAPPQoEResourceManger.getInstance();
        this.hwUserLearning = HwAPPQoEUserLearning.createHwAPPQoEUserLearning(context);
        initProcess();
    }

    private void initProcess() {
        ActivityManagerEx.registerHwActivityNotifier(this.mActivityNotifierEx, "activityLifeState");
        this.mAppTypeRecoManager = AppTypeRecoManager.getInstance();
        this.mHandlerThread = new HandlerThread("HwAPPQoEActivityMonitor Thread");
        this.mHandlerThread.start();
        this.mHandler = new MainHandler(this.mHandlerThread.getLooper());
    }

    protected static HwAPPQoEActivityMonitor createHwAPPQoEActivityMonitor(Context context) {
        if (mHwAPPQoEActivityMonitor == null) {
            mHwAPPQoEActivityMonitor = new HwAPPQoEActivityMonitor(context);
        }
        return mHwAPPQoEActivityMonitor;
    }

    public void handleActivityChange(String curPackage, String curClass, int curUid, boolean isCalledByTimerExpired) {
        if (curPackage == null || curClass == null) {
            HwAPPQoEUtils.logD(TAG, "handleActivityChange,  error input");
            return;
        }
        String str = TAG;
        HwAPPQoEUtils.logD(str, "handleActivityChange,  curPackage:" + curPackage + ", curClass:" + curClass);
        HwAPPStateInfo curAPPStateInfo = new HwAPPStateInfo();
        curAPPStateInfo.mAppUID = curUid;
        HwAPPQoEAPKConfig tempAPKScence = this.hwResourceManger.checkIsMonitorAPKScence(curPackage, curClass);
        notifyAPPStateChange(curPackage, curUid, tempAPKScence);
        if (tempAPKScence != null) {
            curAPPStateInfo.mAppId = tempAPKScence.mAppId;
            curAPPStateInfo.mScenceId = tempAPKScence.mScenceId;
            curAPPStateInfo.mScenceType = tempAPKScence.mScenceType;
            curAPPStateInfo.mAppPeriod = tempAPKScence.mAppPeriod;
            curAPPStateInfo.mAppType = 1000;
            curAPPStateInfo.mAction = tempAPKScence.mAction;
        } else if (isSpecialGameApp(curPackage)) {
            HwAPPQoEUtils.logD(TAG, "handleActivityChange, it is a Special game");
            HwAPPQoEGameConfig config = this.hwResourceManger.checkIsMonitorGameScence(curPackage);
            if (config != null) {
                curAPPStateInfo.mAppId = config.mGameId;
            }
            curAPPStateInfo.mAppType = 2000;
            curAPPStateInfo.mScenceId = 200001;
        } else if (isGeneralGameApp(curPackage)) {
            HwAPPQoEUtils.logD(TAG, "handleActivityChange, it is a general game");
            curAPPStateInfo.mAppType = 3000;
            curAPPStateInfo.mScenceId = 200001;
        } else {
            HwAPPQoEUtils.logD(TAG, "handleActivityChange, it is not a care app or scence");
        }
        if (1 == lastAPPStateInfo.mScenceType) {
            if (isCalledByTimerExpired) {
                HwAPPQoEUtils.logD(TAG, "handleActivityChange, delay timer expired");
            } else if (this.hwResourceManger.checkIsMonitorGameScence(curPackage) != null) {
                HwAPPQoEUtils.logD(TAG, "handleActivityChange, app type is game");
                this.mHandler.removeMessages(1);
            } else if (1 != curAPPStateInfo.mScenceType) {
                CachedAppInfo mCachedAppInfo = new CachedAppInfo();
                mCachedAppInfo.mAppUID = curUid;
                mCachedAppInfo.mClassName = curClass;
                mCachedAppInfo.mPackageName = curPackage;
                this.mHandler.removeMessages(1);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, mCachedAppInfo), 1000);
                HwAPPQoEUtils.logD(TAG, "handleActivityChange, delay 1s to update scence during pay scence");
                return;
            } else {
                this.mHandler.removeMessages(1);
            }
        }
        if (tempAPKScence != null && tempAPKScence.mAppId == lastAPPStateInfo.mAppId && 255 == tempAPKScence.mScenceType) {
            String str2 = TAG;
            HwAPPQoEUtils.logD(str2, "handleActivityChange, ignore current scence:" + tempAPKScence.mScenceType);
            return;
        }
        String str3 = TAG;
        HwAPPQoEUtils.logD(str3, "handleActivityChange,  curr mAppType:" + curAPPStateInfo.mAppType + ", lastType:" + lastAPPStateInfo.mAppType);
        if (-1 != curAPPStateInfo.mAppType && -1 == lastAPPStateInfo.mAppType) {
            HwAPPQoEContentAware.sentNotificationToSTM(curAPPStateInfo, 100);
        } else if (-1 == curAPPStateInfo.mAppType && -1 != lastAPPStateInfo.mAppType) {
            HwAPPQoEContentAware.sentNotificationToSTM(lastAPPStateInfo, 101);
        } else if (-1 != curAPPStateInfo.mAppType || -1 != lastAPPStateInfo.mAppType) {
            String str4 = TAG;
            HwAPPQoEUtils.logD(str4, "handleActivityChange,  curr Scence:" + curAPPStateInfo.mScenceId + ", last Scence:" + lastAPPStateInfo.mScenceId);
            if (lastAPPStateInfo.mAppUID != curAPPStateInfo.mAppUID || lastAPPStateInfo.mAppId != curAPPStateInfo.mAppId) {
                HwAPPQoEContentAware.sentNotificationToSTM(lastAPPStateInfo, 101);
                HwAPPQoEContentAware.sentNotificationToSTM(curAPPStateInfo, 100);
            } else if (lastAPPStateInfo.mScenceId != curAPPStateInfo.mScenceId) {
                HwAPPQoEContentAware.sentNotificationToSTM(curAPPStateInfo, 102);
            }
        } else {
            return;
        }
        lastAPPStateInfo.copyObjectValue(curAPPStateInfo);
    }

    private void notifyAPPStateChange(String packageName, int uid, HwAPPQoEAPKConfig tempAPKConfig) {
        if (tempAPKConfig != null) {
            this.hwUserLearning.setLatestAPPScenceId(tempAPKConfig.mScenceId);
        }
        if (-1 == this.monitorAPPUID) {
            HwAPPQoEAPKConfig tempAPKScence = this.hwResourceManger.checkIsMonitorAPKScence(packageName, null);
            if (tempAPKScence != null && this.APP_USER_LEARNING_MONITOR == tempAPKScence.monitorUserLearning) {
                HwAPPQoEUtils.logD(TAG, "notifyAPPStateChange, init app state");
                this.monitorAPPUID = uid;
                this.apkStartTime = System.currentTimeMillis();
            }
        } else if (uid != this.monitorAPPUID) {
            HwAPPQoEUtils.logD(TAG, "notifyAPPStateChange, prepare to notify userlearning");
            this.hwUserLearning.notifyAPPStateChange(this.apkStartTime, System.currentTimeMillis(), lastAPPStateInfo.mAppId);
            this.monitorAPPUID = -1;
        }
    }

    public boolean isGeneralGameApp(String packageName) {
        if (this.hwResourceManger.checkIsMonitorGameScence(packageName) != null) {
            HwAPPQoEUtils.logD(TAG, "it is a monitor game");
            return false;
        } else if (isGameTypeForRecoManager(this.mAppTypeRecoManager.getAppType(packageName))) {
            return true;
        } else {
            if (TextUtils.isEmpty(packageName)) {
                return false;
            }
            if (isDefaultGameType(packageName)) {
                return true;
            }
            if (packageName.contains(":") && isGameTypeForRecoManager(this.mAppTypeRecoManager.getAppType(getRealAppName(packageName)))) {
                return true;
            }
            if (HwAPPQoEUtils.GAME_ASSISIT_ENABLE) {
                return ActivityManagerEx.isInGameSpace(packageName);
            }
            return false;
        }
    }

    private boolean isGameTypeForRecoManager(int type) {
        if (305 == type || 9 == type) {
            return true;
        }
        return false;
    }

    private boolean isDefaultGameType(String appName) {
        if (!TextUtils.isEmpty(appName)) {
            for (String startsWith : DEFAULT_GAME_NAMES) {
                if (appName.startsWith(startsWith)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getRealAppName(String appName) {
        if (TextUtils.isEmpty(appName) || !appName.contains(":")) {
            return "";
        }
        String[] appNames = appName.split(":", 2);
        if (appNames.length > 0) {
            return appNames[0];
        }
        return "";
    }

    public boolean isSpecialGameApp(String packageName) {
        if (this.hwResourceManger.checkIsMonitorGameScence(packageName) == null) {
            return false;
        }
        HwAPPQoEUtils.logD(TAG, "it is a monitor Special game");
        return true;
    }
}
