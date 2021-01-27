package com.android.server.hidata.appqoe;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AppTypeRecoManager;
import android.text.TextUtils;
import com.android.server.hidata.arbitration.HwAppTimeDetail;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import com.android.server.hidata.histream.HwHiStreamContentAware;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;

public class HwAppQoeActivityMonitor {
    private static final int DEFAULT_DELAY_TIME = 1000;
    private static final String[] DEFAULT_GAME_NAMES = {"com.huawei.gamebox"};
    private static final int DEFAULT_INVALID_VALUE = -1;
    private static final int DEFAULT_LIMIT_NUMBER = 2;
    private static final int DETECT_SPEED_INTERVAL = 3000;
    private static final int EVENT_ACTIVITY_STATE_CHANGE = 2;
    private static final int EVENT_DELAY_TIMER_EXPIRE = 1;
    private static final int EVENT_DETECT_SPEED = 3;
    private static String TAG = (HwArbitrationDefs.BASE_TAG + HwAppQoeActivityMonitor.class.getSimpleName());
    private static HwAppQoeActivityMonitor sHwAppQoeActivityMonitor = null;
    private static HwAppStateInfo sLastAppStateInfo = new HwAppStateInfo();
    private IHwActivityNotifierEx mActivityNotifierEx = new IHwActivityNotifierEx() {
        /* class com.android.server.hidata.appqoe.HwAppQoeActivityMonitor.AnonymousClass1 */

        public void call(Bundle extras) {
            if (extras == null) {
                HwAppQoeUtils.logD(HwAppQoeActivityMonitor.TAG, false, "AMS callback, extras=null", new Object[0]);
                return;
            }
            CachedAppInfo mCachedAppInfo = new CachedAppInfo();
            mCachedAppInfo.mAppUid = extras.getInt("uid");
            Object tempComp = extras.getParcelable("comp");
            if (!(tempComp instanceof ComponentName)) {
                HwAppQoeUtils.logD(HwAppQoeActivityMonitor.TAG, false, "AMS callback , tempComp is not instance of ComponentName", new Object[0]);
                return;
            }
            ComponentName componentName = (ComponentName) tempComp;
            mCachedAppInfo.mClassName = componentName.getClassName();
            mCachedAppInfo.mPackageName = componentName.getPackageName();
            if ("onResume".equals(extras.getString("state"))) {
                HwAppQoeActivityMonitor.this.mHandler.removeMessages(1);
                HwAppQoeActivityMonitor.this.mHandler.sendMessage(HwAppQoeActivityMonitor.this.mHandler.obtainMessage(2, mCachedAppInfo));
                HwHiStreamContentAware mHwHiStreamContentAware = HwHiStreamContentAware.getInstance();
                if (mHwHiStreamContentAware != null) {
                    mHwHiStreamContentAware.onActivityResume(extras);
                }
            }
        }
    };
    private AppTypeRecoManager mAppTypeRecoManager;
    private Context mContext;
    private int mDetectSpeed;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;
    private HwAppQoeResourceManager mHwAppQoeResourceManager = null;
    private String mLastPackageName;
    private int mLastUid;

    public static class CachedAppInfo {
        public int mAppUid;
        public String mClassName;
        public String mPackageName;
    }

    private HwAppQoeActivityMonitor(Context context) {
        this.mContext = context;
        this.mHwAppQoeResourceManager = HwAppQoeResourceManager.getInstance();
        this.mLastUid = -1;
        this.mLastPackageName = "";
        this.mDetectSpeed = 0;
        initProcess();
    }

    private void initProcess() {
        ActivityManagerEx.registerHwActivityNotifier(this.mActivityNotifierEx, "activityLifeState");
        this.mAppTypeRecoManager = AppTypeRecoManager.getInstance();
        this.mHandlerThread = new HandlerThread("HwAppQoeActivityMonitor Thread");
        this.mHandlerThread.start();
        this.mHandler = new MainHandler(this.mHandlerThread.getLooper());
    }

    protected static HwAppQoeActivityMonitor createHwAppQoeActivityMonitor(Context context) {
        if (sHwAppQoeActivityMonitor == null) {
            sHwAppQoeActivityMonitor = new HwAppQoeActivityMonitor(context);
        }
        return sHwAppQoeActivityMonitor;
    }

    /* access modifiers changed from: private */
    public class MainHandler extends Handler {
        MainHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null || msg.obj == null) {
                HwAppQoeUtils.logD(HwAppQoeActivityMonitor.TAG, false, "handleMessage -- invalid input", new Object[0]);
                return;
            }
            int i = msg.what;
            if (i != 1) {
                if (i != 2) {
                    if (i == 3) {
                        HwAppTimeDetail.getInstance().handleTimeThread();
                        Message msgCycle = HwAppQoeActivityMonitor.this.mHandler.obtainMessage();
                        msgCycle.what = 3;
                        msgCycle.obj = HwAppQoeActivityMonitor.this.mLastPackageName;
                        HwAppQoeActivityMonitor.this.mHandler.sendMessageDelayed(msgCycle, 3000);
                    }
                } else if (msg.obj instanceof CachedAppInfo) {
                    HwAppQoeActivityMonitor.this.handleActivityChange((CachedAppInfo) msg.obj, false);
                }
            } else if (msg.obj instanceof CachedAppInfo) {
                HwAppQoeActivityMonitor.this.handleActivityChange((CachedAppInfo) msg.obj, true);
            }
        }
    }

    public void handleActivityChange(CachedAppInfo appInfo, boolean isCalledByTimerExpired) {
        if (appInfo == null || TextUtils.isEmpty(appInfo.mPackageName) || TextUtils.isEmpty(appInfo.mClassName)) {
            HwAppQoeUtils.logD(TAG, false, "handleActivityChange, appInfo is null", new Object[0]);
            return;
        }
        HwAppQoeUtils.logD(TAG, false, "handleActivityChange,  curPackage:%{public}s, curClass:%{public}s", appInfo.mPackageName, appInfo.mClassName);
        processAppTimeChr(appInfo);
        HwAppStateInfo appStateInfo = new HwAppStateInfo();
        appStateInfo.mAppUid = appInfo.mAppUid;
        HwAppQoeApkConfig tempAppConfig = this.mHwAppQoeResourceManager.checkIsMonitorApkScenes(appInfo.mPackageName, appInfo.mClassName);
        processGameAppConfig(appStateInfo, tempAppConfig, appInfo.mPackageName);
        if (!isNeedTimeExpired(appInfo, appStateInfo, isCalledByTimerExpired)) {
            if (tempAppConfig != null && tempAppConfig.mAppId == sLastAppStateInfo.mAppId && tempAppConfig.mScenesType == 255) {
                HwAppQoeUtils.logD(TAG, false, "handleActivityChange, ignore current scenes:%{public}d", Integer.valueOf(tempAppConfig.mScenesType));
                return;
            }
            HwAppQoeUtils.logD(TAG, false, "handleActivityChange, curr mAppType:%{public}d, last mAppType:%{public}d", Integer.valueOf(appStateInfo.mAppType), Integer.valueOf(sLastAppStateInfo.mAppType));
            if (!isAppTypeInvalid(appStateInfo)) {
                sLastAppStateInfo.copyObjectValue(appStateInfo);
            }
        }
    }

    private void processAppTimeChr(CachedAppInfo appInfo) {
        if (this.mLastUid != appInfo.mAppUid) {
            if (!isSystemServiceOrApp(appInfo.mPackageName)) {
                HwAppTimeDetail.getInstance().startAppTime(appInfo.mPackageName, appInfo.mAppUid);
                if (this.mDetectSpeed == 0) {
                    this.mDetectSpeed = 1;
                    Message msgCycle = Message.obtain();
                    msgCycle.what = 3;
                    msgCycle.obj = appInfo.mPackageName;
                    this.mHandler.sendMessageDelayed(msgCycle, 3000);
                }
            }
            if (!isSystemServiceOrApp(this.mLastPackageName)) {
                HwAppTimeDetail.getInstance().getAppUseTime(this.mLastPackageName, this.mLastUid);
            }
            this.mLastUid = appInfo.mAppUid;
            this.mLastPackageName = appInfo.mPackageName;
        }
    }

    private boolean isSystemServiceOrApp(String packageName) {
        try {
            return TextUtils.isEmpty(packageName) || (this.mContext.getPackageManager().getApplicationInfo(packageName, 0).flags & 1) == 1 || "".equals(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void processGameAppConfig(HwAppStateInfo appStateInfo, HwAppQoeApkConfig appConfig, String curPackage) {
        if (appConfig != null) {
            appStateInfo.mAppId = appConfig.mAppId;
            appStateInfo.mScenesId = appConfig.mScenesId;
            appStateInfo.mScenesType = appConfig.mScenesType;
            appStateInfo.mAppPeriod = appConfig.mAppPeriod;
            appStateInfo.mAppType = 1000;
            appStateInfo.mAction = appConfig.mAction;
            appStateInfo.setAppRegion(appConfig.mAppRegion);
        } else if (isSpecialGameApp(curPackage)) {
            HwAppQoeUtils.logD(TAG, false, "handleActivityChange, it is a Special game", new Object[0]);
            HwAppQoeGameConfig config = this.mHwAppQoeResourceManager.checkIsMonitorGameScenes(curPackage);
            if (config != null) {
                appStateInfo.mAppId = config.mGameId;
            }
            appStateInfo.mAppType = 2000;
            appStateInfo.mScenesId = 200001;
        } else if (isGeneralGameApp(curPackage)) {
            HwAppQoeUtils.logD(TAG, false, "handleActivityChange, it is a general game", new Object[0]);
            appStateInfo.mAppType = 3000;
            appStateInfo.mScenesId = 200001;
        } else {
            HwAppQoeUtils.logD(TAG, false, "handleActivityChange, it is not a care app or scenes", new Object[0]);
        }
    }

    private boolean isNeedTimeExpired(CachedAppInfo appInfo, HwAppStateInfo appStateInfo, boolean isTimerExpired) {
        if (sLastAppStateInfo.mScenesType == 1) {
            if (isTimerExpired) {
                HwAppQoeUtils.logD(TAG, false, "handleActivityChange, delay timer expired", new Object[0]);
            } else if (this.mHwAppQoeResourceManager.checkIsMonitorGameScenes(appInfo.mPackageName) != null) {
                HwAppQoeUtils.logD(TAG, false, "handleActivityChange, app type is game", new Object[0]);
                this.mHandler.removeMessages(1);
            } else if (appStateInfo.mScenesType != 1) {
                this.mHandler.removeMessages(1);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, appInfo), 1000);
                HwAppQoeUtils.logD(TAG, false, "handleActivityChange, delay 1s to update scenes during pay scenes", new Object[0]);
                return true;
            } else {
                this.mHandler.removeMessages(1);
            }
        }
        return false;
    }

    private boolean isAppTypeInvalid(HwAppStateInfo appStateInfo) {
        if (appStateInfo.mAppType != -1 && sLastAppStateInfo.mAppType == -1) {
            HwAppQoeContentAware.sentNotificationToStm(appStateInfo, 100);
        } else if (appStateInfo.mAppType == -1 && sLastAppStateInfo.mAppType != -1) {
            HwAppQoeContentAware.sentNotificationToStm(sLastAppStateInfo, 101);
        } else if (appStateInfo.mAppType == -1 && sLastAppStateInfo.mAppType == -1) {
            return true;
        } else {
            HwAppQoeUtils.logD(TAG, false, "handleActivityChange,  curr scenes:%{public}d, last scenes:%{public}d", Integer.valueOf(appStateInfo.mScenesId), Integer.valueOf(sLastAppStateInfo.mScenesId));
            if (sLastAppStateInfo.mAppUid != appStateInfo.mAppUid || sLastAppStateInfo.mAppId != appStateInfo.mAppId) {
                HwAppQoeContentAware.sentNotificationToStm(sLastAppStateInfo, 101);
                HwAppQoeContentAware.sentNotificationToStm(appStateInfo, 100);
            } else if (sLastAppStateInfo.mScenesId != appStateInfo.mScenesId) {
                HwAppQoeContentAware.sentNotificationToStm(appStateInfo, 102);
            } else {
                HwAppQoeUtils.logD(TAG, false, "app type and app scenes were both same as before", new Object[0]);
            }
        }
        return false;
    }

    public boolean isGeneralGameApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            HwAppQoeUtils.logD(TAG, false, "isGeneralGameApp, packageName is null", new Object[0]);
            return false;
        } else if (this.mHwAppQoeResourceManager.checkIsMonitorGameScenes(packageName) != null) {
            HwAppQoeUtils.logD(TAG, false, "isGeneralGameApp, config is null", new Object[0]);
            return false;
        } else if (isGameTypeForRecoManager(this.mAppTypeRecoManager.getAppType(packageName)) || isDefaultGameType(packageName)) {
            return true;
        } else {
            if (packageName.contains(AwarenessInnerConstants.COLON_KEY) && isGameTypeForRecoManager(this.mAppTypeRecoManager.getAppType(getRealAppName(packageName)))) {
                return true;
            }
            if (HwAppQoeUtils.GAME_ASSISIT_ENABLE) {
                return ActivityManagerEx.isInGameSpace(packageName);
            }
            return false;
        }
    }

    private boolean isGameTypeForRecoManager(int type) {
        if (type == 305 || type == 9) {
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
        if (this.mHwAppQoeResourceManager.checkIsMonitorGameScenes(packageName) == null) {
            return false;
        }
        HwAppQoeUtils.logD(TAG, false, "it is a monitor Special game", new Object[0]);
        return true;
    }
}
