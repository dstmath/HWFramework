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
import com.android.server.hidata.histream.HwHiStreamContentAware;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;

public class HwAPPQoEActivityMonitor {
    private static final String[] DEFAULT_GAME_NAMES = {"com.huawei.gamebox"};
    private static final int EVENT_ACTIVITY_STATE_CHANGE = 2;
    private static final int EVENT_DELAY_TIMER_EXPIRE = 1;
    private static String TAG = "HiData_HwAPPQoEActivityMonitor";
    private static HwAPPStateInfo lastAPPStateInfo = new HwAPPStateInfo();
    private static HwAPPQoEActivityMonitor mHwAPPQoEActivityMonitor = null;
    private int APP_USER_LEARNING_MONITOR;
    private long apkStartTime;
    private HwAPPQoEResourceManger hwResourceManger;
    private IHwActivityNotifierEx mActivityNotifierEx;
    private AppTypeRecoManager mAppTypeRecoManager;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private int monitorAPPUID;

    public static class CachedAppInfo {
        public int mAppUID;
        public String mClassName;
        public String mPackageName;
    }

    private HwAPPQoEActivityMonitor(Context context) {
        this.hwResourceManger = null;
        this.mHandler = null;
        this.mHandlerThread = null;
        this.apkStartTime = System.currentTimeMillis();
        this.monitorAPPUID = -1;
        this.APP_USER_LEARNING_MONITOR = 1;
        this.mActivityNotifierEx = new IHwActivityNotifierEx() {
            /* class com.android.server.hidata.appqoe.HwAPPQoEActivityMonitor.AnonymousClass1 */

            public void call(Bundle extras) {
                if (extras == null) {
                    HwAPPQoEUtils.logD(HwAPPQoEActivityMonitor.TAG, false, "AMS callback , extras=null", new Object[0]);
                    return;
                }
                CachedAppInfo mCachedAppInfo = new CachedAppInfo();
                mCachedAppInfo.mAppUID = extras.getInt("uid");
                ComponentName componentName = (ComponentName) extras.getParcelable("comp");
                String str = "";
                mCachedAppInfo.mClassName = componentName != null ? componentName.getClassName() : str;
                if (componentName != null) {
                    str = componentName.getPackageName();
                }
                mCachedAppInfo.mPackageName = str;
                if ("onResume".equals(extras.getString("state"))) {
                    HwAPPQoEActivityMonitor.this.mHandler.removeMessages(1);
                    HwAPPQoEActivityMonitor.this.mHandler.sendMessage(HwAPPQoEActivityMonitor.this.mHandler.obtainMessage(2, mCachedAppInfo));
                    HwHiStreamContentAware mHwHiStreamContentAware = HwHiStreamContentAware.getInstance();
                    if (mHwHiStreamContentAware != null) {
                        mHwHiStreamContentAware.onActivityResume(extras);
                    }
                }
            }
        };
        this.hwResourceManger = HwAPPQoEResourceManger.getInstance();
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

    /* access modifiers changed from: private */
    public class MainHandler extends Handler {
        MainHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null || msg.obj == null) {
                HwAPPQoEUtils.logD(HwAPPQoEActivityMonitor.TAG, false, "handleMessage -- invalid input", new Object[0]);
                return;
            }
            int i = msg.what;
            if (i == 1) {
                CachedAppInfo cachedAppInfo = (CachedAppInfo) msg.obj;
                HwAPPQoEActivityMonitor.this.handleActivityChange(cachedAppInfo.mPackageName, cachedAppInfo.mClassName, cachedAppInfo.mAppUID, true);
            } else if (i == 2) {
                CachedAppInfo newAppInfo = (CachedAppInfo) msg.obj;
                HwAPPQoEActivityMonitor.this.handleActivityChange(newAppInfo.mPackageName, newAppInfo.mClassName, newAppInfo.mAppUID, false);
            }
        }
    }

    public void handleActivityChange(String curPackage, String curClass, int curUid, boolean isCalledByTimerExpired) {
        if (curPackage == null || curClass == null) {
            HwAPPQoEUtils.logD(TAG, false, "handleActivityChange,  error input", new Object[0]);
            return;
        }
        HwAPPQoEUtils.logD(TAG, false, "handleActivityChange,  curPackage:%{public}s, curClass:%{public}s", curPackage, curClass);
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
            curAPPStateInfo.setAppRegion(tempAPKScence.getAppRegion());
        } else if (isSpecialGameApp(curPackage)) {
            HwAPPQoEUtils.logD(TAG, false, "handleActivityChange, it is a Special game", new Object[0]);
            HwAPPQoEGameConfig config = this.hwResourceManger.checkIsMonitorGameScence(curPackage);
            if (config != null) {
                curAPPStateInfo.mAppId = config.mGameId;
            }
            curAPPStateInfo.mAppType = 2000;
            curAPPStateInfo.mScenceId = 200001;
        } else if (isGeneralGameApp(curPackage)) {
            HwAPPQoEUtils.logD(TAG, false, "handleActivityChange, it is a general game", new Object[0]);
            curAPPStateInfo.mAppType = 3000;
            curAPPStateInfo.mScenceId = 200001;
        } else {
            HwAPPQoEUtils.logD(TAG, false, "handleActivityChange, it is not a care app or scence", new Object[0]);
        }
        if (1 == lastAPPStateInfo.mScenceType) {
            if (isCalledByTimerExpired) {
                HwAPPQoEUtils.logD(TAG, false, "handleActivityChange, delay timer expired", new Object[0]);
            } else if (this.hwResourceManger.checkIsMonitorGameScence(curPackage) != null) {
                HwAPPQoEUtils.logD(TAG, false, "handleActivityChange, app type is game", new Object[0]);
                this.mHandler.removeMessages(1);
            } else if (1 != curAPPStateInfo.mScenceType) {
                CachedAppInfo mCachedAppInfo = new CachedAppInfo();
                mCachedAppInfo.mAppUID = curUid;
                mCachedAppInfo.mClassName = curClass;
                mCachedAppInfo.mPackageName = curPackage;
                this.mHandler.removeMessages(1);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, mCachedAppInfo), 1000);
                HwAPPQoEUtils.logD(TAG, false, "handleActivityChange, delay 1s to update scence during pay scence", new Object[0]);
                return;
            } else {
                this.mHandler.removeMessages(1);
            }
        }
        if (tempAPKScence != null && tempAPKScence.mAppId == lastAPPStateInfo.mAppId && 255 == tempAPKScence.mScenceType) {
            HwAPPQoEUtils.logD(TAG, false, "handleActivityChange, ignore current scence:%{public}d", Integer.valueOf(tempAPKScence.mScenceType));
            return;
        }
        HwAPPQoEUtils.logD(TAG, false, "handleActivityChange, curr mAppType:%{public}d, lastType:%{public}d", Integer.valueOf(curAPPStateInfo.mAppType), Integer.valueOf(lastAPPStateInfo.mAppType));
        if (-1 != curAPPStateInfo.mAppType && -1 == lastAPPStateInfo.mAppType) {
            HwAPPQoEContentAware.sentNotificationToSTM(curAPPStateInfo, 100);
        } else if (-1 == curAPPStateInfo.mAppType && -1 != lastAPPStateInfo.mAppType) {
            HwAPPQoEContentAware.sentNotificationToSTM(lastAPPStateInfo, 101);
        } else if (-1 != curAPPStateInfo.mAppType || -1 != lastAPPStateInfo.mAppType) {
            HwAPPQoEUtils.logD(TAG, false, "handleActivityChange,  curr Scence:%{public}d, last Scence:%{public}d", Integer.valueOf(curAPPStateInfo.mScenceId), Integer.valueOf(lastAPPStateInfo.mScenceId));
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
        int i = this.monitorAPPUID;
        if (-1 == i) {
            HwAPPQoEAPKConfig tempAPKScence = this.hwResourceManger.checkIsMonitorAPKScence(packageName, null);
            if (tempAPKScence != null && this.APP_USER_LEARNING_MONITOR == tempAPKScence.monitorUserLearning) {
                HwAPPQoEUtils.logD(TAG, false, "notifyAPPStateChange, init app state", new Object[0]);
                this.monitorAPPUID = uid;
                this.apkStartTime = System.currentTimeMillis();
            }
        } else if (uid != i) {
            HwAPPQoEUtils.logD(TAG, false, "notifyAPPStateChange, prepare to notify userlearning", new Object[0]);
            System.currentTimeMillis();
            this.monitorAPPUID = -1;
        }
    }

    public boolean isGeneralGameApp(String packageName) {
        if (this.hwResourceManger.checkIsMonitorGameScence(packageName) != null) {
            HwAPPQoEUtils.logD(TAG, false, "it is a monitor game", new Object[0]);
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
            if (packageName.contains(AwarenessInnerConstants.COLON_KEY) && isGameTypeForRecoManager(this.mAppTypeRecoManager.getAppType(getRealAppName(packageName)))) {
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
        if (TextUtils.isEmpty(appName)) {
            return false;
        }
        int length = DEFAULT_GAME_NAMES.length;
        for (int i = 0; i < length; i++) {
            if (appName.startsWith(DEFAULT_GAME_NAMES[i])) {
                return true;
            }
        }
        return false;
    }

    private String getRealAppName(String appName) {
        if (TextUtils.isEmpty(appName) || !appName.contains(AwarenessInnerConstants.COLON_KEY)) {
            return "";
        }
        String[] appNames = appName.split(AwarenessInnerConstants.COLON_KEY, 2);
        if (appNames.length > 0) {
            return appNames[0];
        }
        return "";
    }

    public boolean isSpecialGameApp(String packageName) {
        if (this.hwResourceManger.checkIsMonitorGameScence(packageName) == null) {
            return false;
        }
        HwAPPQoEUtils.logD(TAG, false, "it is a monitor Special game", new Object[0]);
        return true;
    }
}
