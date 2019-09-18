package com.android.server.hidata.histream;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import com.android.server.am.HwActivityManagerService;
import com.android.server.hidata.appqoe.HwAPPQoEAPKConfig;
import com.android.server.hidata.appqoe.HwAPPQoEResourceManger;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.mplink.MpLinkQuickSwitchConfiguration;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import java.util.ArrayList;

public class HwHiStreamContentAware {
    private static final String ACTION_AUDIO_RECORD_STATE_CHANGED = "huawei.media.AUDIO_RECORD_STATE_CHANGED_ACTION";
    private static final int APP_MONITOR_INTERVAL = 1500;
    private static final int AUDIO_RECORD_STATE_RECORDING = 3;
    private static final int AUDIO_RECORD_STATE_STOPPED = 1;
    private static final String INTENT_PACKAGENAME = "packagename";
    private static final String INTENT_STATE = "state";
    private static final int MINTIME_STALL_REPORT_ENTER_PLAY_ACT = 2000;
    private static final String PERMISSION_SEND_AUDIO_RECORD_STATE = "com.huawei.permission.AUDIO_RECORD_STATE_CHANGED_ACTION";
    private static final int TIME_CHECK_APP_FIRST_START = 4000;
    private static final int TOP_ACTIVITY_OTHERS = -1;
    private static final int TOP_ACTIVITY_SENSITIVE_APP = 2;
    private static final int TOP_ACTIVITY_WECHAT_CALL = 1;
    private static final int WECHAT_CALL_ACTIVITY_PAUSE = 2;
    private static final int WECHAT_CALL_ACTIVITY_RESUME = 1;
    private static final String WECHAT_NAME = "com.tencent.mm";
    private static HwHiStreamContentAware mHwHiStreamContentAware;
    /* access modifiers changed from: private */
    public int foregroundUid = -1;
    private final IActivityManager mActivityManager;
    private HwActivityManagerService mActivityManagerService;
    private IHwActivityNotifierEx mActivityNotifierEx = new IHwActivityNotifierEx() {
        public void call(Bundle extras) {
            if (extras == null) {
                HwHiStreamUtils.logD("AMS callback , extras=null");
                return;
            }
            if ("onResume".equals(extras.getString(HwHiStreamContentAware.INTENT_STATE))) {
                HwHiStreamContentAware.this.mContentAwareHandler.sendMessage(HwHiStreamContentAware.this.mContentAwareHandler.obtainMessage(13, extras));
            }
        }
    };
    private HwAPPStateInfo mAppConfig = null;
    private ArrayList<HwAPPStateInfo> mAppList = null;
    private HwAPPStateInfo mAppStateInfo = null;
    /* access modifiers changed from: private */
    public int mAudioRecordState = 1;
    private CameraManager.AvailabilityCallback mAvailabilityCallback = new CameraManager.AvailabilityCallback() {
        public void onCameraAvailable(String cameraId) {
            HwHiStreamUtils.logD("onCameraAvailable cameraId = " + cameraId);
            if (cameraId != null && cameraId.equals(HwHiStreamContentAware.this.mCameraId)) {
                String unused = HwHiStreamContentAware.this.mCameraId = "none";
                boolean unused2 = HwHiStreamContentAware.this.mIsUpgradeVideo = false;
                if (HwHiStreamContentAware.this.mWechatStateInfo != null && 101 != HwHiStreamContentAware.this.mWechatStateInfo.mAppState && 100106 == HwHiStreamContentAware.this.mWechatStateInfo.mScenceId && !HwHiStreamContentAware.this.mContentAwareHandler.hasMessages(1)) {
                    HwHiStreamContentAware.this.mContentAwareHandler.sendEmptyMessageDelayed(1, 500);
                }
            }
        }

        public void onCameraUnavailable(String cameraId) {
            HwHiStreamUtils.logD("onCameraUnavailable cameraId = " + cameraId);
            if (cameraId != null) {
                String unused = HwHiStreamContentAware.this.mCameraId = cameraId;
                if (1 == HwHiStreamContentAware.this.mTopActivity && HwHiStreamContentAware.this.mWechatStateInfo != null && 100106 == HwHiStreamContentAware.this.mWechatStateInfo.mScenceId && true == HwHiStreamContentAware.this.mIsScreenOn) {
                    boolean unused2 = HwHiStreamContentAware.this.mIsUpgradeVideo = true;
                    if (!HwHiStreamContentAware.this.mContentAwareHandler.hasMessages(1)) {
                        HwHiStreamContentAware.this.mContentAwareHandler.sendEmptyMessage(1);
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public String mCameraId = "none";
    private CameraManager mCameraManager;
    /* access modifiers changed from: private */
    public Handler mContentAwareHandler;
    private Context mContext;
    private long mEnterPlayActTime = 0;
    private IHwHiStreamCallback mHiStreamCallback;
    private HwAPPQoEResourceManger mHwAPPQoEResourceManger;
    private boolean mIsPlayActivity = false;
    /* access modifiers changed from: private */
    public boolean mIsScreenOn = true;
    /* access modifiers changed from: private */
    public boolean mIsUpgradeVideo = false;
    private long mLastNotityTime = 0;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private HiStreamProcessObserver mProcessObserver;
    private DynamicReceiver mReceiver;
    /* access modifiers changed from: private */
    public int mTopActivity = -1;
    private HwAPPStateInfo mWechatConfig = null;
    /* access modifiers changed from: private */
    public HwAPPStateInfo mWechatStateInfo = null;

    class DynamicReceiver extends BroadcastReceiver {
        DynamicReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                String packageName = intent.getStringExtra(HwHiStreamContentAware.INTENT_PACKAGENAME);
                int state = intent.getIntExtra(HwHiStreamContentAware.INTENT_STATE, 0);
                HwHiStreamUtils.logD("--->Intent received, action=" + action + ", packageName " + packageName + ",state" + state);
                if (!HwHiStreamContentAware.ACTION_AUDIO_RECORD_STATE_CHANGED.equals(action) || packageName == null || !HwHiStreamContentAware.WECHAT_NAME.equals(packageName)) {
                    if ("android.intent.action.SCREEN_ON".equals(action)) {
                        boolean unused = HwHiStreamContentAware.this.mIsScreenOn = true;
                        if (1 == HwHiStreamContentAware.this.mTopActivity && !HwHiStreamContentAware.this.mContentAwareHandler.hasMessages(1)) {
                            HwHiStreamContentAware.this.mContentAwareHandler.sendEmptyMessage(1);
                        }
                    } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                        boolean unused2 = HwHiStreamContentAware.this.mIsScreenOn = false;
                        if (1 == HwHiStreamContentAware.this.mTopActivity && !HwHiStreamContentAware.this.mContentAwareHandler.hasMessages(1)) {
                            HwHiStreamContentAware.this.mContentAwareHandler.sendEmptyMessage(1);
                        }
                    }
                } else if (1 == state && 3 == HwHiStreamContentAware.this.mAudioRecordState) {
                    int unused3 = HwHiStreamContentAware.this.mAudioRecordState = 1;
                    if (!HwHiStreamContentAware.this.mContentAwareHandler.hasMessages(1)) {
                        HwHiStreamContentAware.this.mContentAwareHandler.sendEmptyMessage(1);
                    }
                } else if (3 == state) {
                    int unused4 = HwHiStreamContentAware.this.mAudioRecordState = 3;
                    if (!HwHiStreamContentAware.this.mContentAwareHandler.hasMessages(1)) {
                        HwHiStreamContentAware.this.mContentAwareHandler.sendEmptyMessage(1);
                    }
                }
            }
        }
    }

    private class HiStreamProcessObserver extends IProcessObserver.Stub {
        private HiStreamProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        }

        public void onProcessDied(int pid, int uid) {
            if (HwHiStreamContentAware.this.foregroundUid != uid) {
                synchronized (HwHiStreamContentAware.this.mLock) {
                    HwHiStreamContentAware.this.removeCurAliveAppList(uid);
                }
            }
        }
    }

    private HwHiStreamContentAware(Context context, IHwHiStreamCallback callback, Handler handler) {
        this.mContext = context;
        this.mHiStreamCallback = callback;
        this.mContentAwareHandler = handler;
        this.mHwAPPQoEResourceManger = HwAPPQoEResourceManger.getInstance();
        this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
        this.mCameraManager.registerAvailabilityCallback(this.mAvailabilityCallback, null);
        ActivityManagerEx.registerHwActivityNotifier(this.mActivityNotifierEx, "activityLifeState");
        this.mReceiver = new DynamicReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_AUDIO_RECORD_STATE_CHANGED);
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        this.mContext.registerReceiver(this.mReceiver, filter, PERMISSION_SEND_AUDIO_RECORD_STATE, null);
        this.mAppList = new ArrayList<>();
        this.mProcessObserver = new HiStreamProcessObserver();
        this.mActivityManager = ActivityManagerNative.getDefault();
        registerProcessStatusObserver();
    }

    public static HwHiStreamContentAware createInstance(Context context, IHwHiStreamCallback callback, Handler handler) {
        if (mHwHiStreamContentAware == null) {
            mHwHiStreamContentAware = new HwHiStreamContentAware(context, callback, handler);
        }
        return mHwHiStreamContentAware;
    }

    public static HwHiStreamContentAware getInstance() {
        return mHwHiStreamContentAware;
    }

    private boolean isWechatCallEndOrDegrade() {
        if (this.mWechatStateInfo == null || 101 == this.mWechatStateInfo.mAppState || ((isCameraOn() || 100106 != this.mWechatStateInfo.mScenceId || 1 != this.mTopActivity || true != this.mIsScreenOn) && 1 != this.mAudioRecordState)) {
            return false;
        }
        return true;
    }

    private boolean isWechatCallUpgrade() {
        if (this.mWechatStateInfo == null || ((100 != this.mWechatStateInfo.mAppState && 103 != this.mWechatStateInfo.mAppState) || 100105 != this.mWechatStateInfo.mScenceId || true != this.mIsScreenOn || true != this.mIsUpgradeVideo)) {
            return false;
        }
        this.mIsUpgradeVideo = false;
        return true;
    }

    private void notifyAppStatChange(int appScene, int appState) {
        Bundle bundle = new Bundle();
        bundle.putInt("appScene", appScene);
        bundle.putInt("appState", appState);
        this.mContentAwareHandler.sendMessage(this.mContentAwareHandler.obtainMessage(2, bundle));
    }

    public void handleAppMonotor() {
        HwHiStreamUtils.logD("handleAppMonotor enter");
        new Bundle();
        if (isWechatCallEndOrDegrade() || isWechatCallUpgrade()) {
            HwHiStreamUtils.logD("-----notify wechat call end  mCurrWeChatType= " + this.mWechatStateInfo.mScenceId);
            notifyAppStatChange(this.mWechatStateInfo.mScenceId, 101);
            if (!this.mContentAwareHandler.hasMessages(1)) {
                this.mContentAwareHandler.sendEmptyMessageDelayed(1, 1500);
            }
            return;
        }
        if (2 == this.mTopActivity) {
            if ((this.mAppStateInfo == null || 100 != this.mAppStateInfo.mAppState) && this.mAppConfig != null) {
                HwHiStreamUtils.logD("-----notify sensitive App Start");
                notifyAppStatChange(this.mAppConfig.mScenceId, 100);
            } else if (!(this.mAppConfig == null || this.mAppStateInfo.mScenceId == this.mAppConfig.mScenceId)) {
                HwHiStreamUtils.logD("-----notify sensitive App End,  mScenceId = " + this.mAppStateInfo.mScenceId);
                notifyAppStatChange(this.mAppStateInfo.mScenceId, 101);
                if (!this.mContentAwareHandler.hasMessages(1)) {
                    this.mContentAwareHandler.sendEmptyMessageDelayed(1, 1500);
                }
            }
        } else if (1 == this.mTopActivity) {
            if (this.mAppStateInfo != null && 100 == this.mAppStateInfo.mAppState) {
                HwHiStreamUtils.logD("-----notify sensitive App End,  mScenceId = " + this.mAppStateInfo.mScenceId);
                notifyAppStatChange(this.mAppStateInfo.mScenceId, 101);
                if (!this.mContentAwareHandler.hasMessages(1)) {
                    this.mContentAwareHandler.sendEmptyMessageDelayed(1, 1500);
                }
            } else if ((this.mWechatStateInfo == null || 101 == this.mWechatStateInfo.mAppState) && 3 == this.mAudioRecordState) {
                int appScene = HwAPPQoEUtils.SCENE_AUDIO;
                if (isCameraOn()) {
                    appScene = HwAPPQoEUtils.SCENE_VIDEO;
                    if (!this.mIsScreenOn) {
                        return;
                    }
                }
                HwHiStreamUtils.logD("-----notify wechat call start appscene = " + appScene + "-----");
                notifyAppStatChange(appScene, 100);
            } else if (this.mWechatStateInfo != null && 104 == this.mWechatStateInfo.mAppState) {
                HwHiStreamUtils.logD("-----notify wechat forground ,mCurrWeChatType= " + this.mWechatStateInfo.mScenceId + "-----");
                notifyAppStatChange(this.mWechatStateInfo.mScenceId, 103);
            }
        }
        if (-1 == this.mTopActivity || !this.mIsScreenOn) {
            if (-1 == this.mTopActivity && this.mAppStateInfo != null && 100 == this.mAppStateInfo.mAppState) {
                HwHiStreamUtils.logD("-----notify sensitive App End,  mScenceId = " + this.mAppStateInfo.mScenceId);
                notifyAppStatChange(this.mAppStateInfo.mScenceId, 101);
                if (!this.mContentAwareHandler.hasMessages(1)) {
                    this.mContentAwareHandler.sendEmptyMessageDelayed(1, 1500);
                }
            } else if (this.mWechatStateInfo != null && ((103 == this.mWechatStateInfo.mAppState || 100 == this.mWechatStateInfo.mAppState) && 3 == this.mAudioRecordState && (100105 != this.mWechatStateInfo.mScenceId || 1 != this.mTopActivity))) {
                HwHiStreamUtils.logD("-----notify wechat background ,mCurrWeChatType=" + this.mWechatStateInfo.mScenceId + "-----");
                notifyAppStatChange(this.mWechatStateInfo.mScenceId, 104);
            }
        }
    }

    public void handleNotifyAppStateChange(Message msg) {
        HwAPPStateInfo curStateInfo;
        if (msg != null && msg.obj != null) {
            Bundle bundle = (Bundle) msg.obj;
            int appScene = bundle.getInt("appScene");
            int appState = bundle.getInt("appState");
            HwHiStreamUtils.logD("handleNotifyAppStateChange appScene=" + appScene + ",appState" + appState);
            if (-1 != appScene) {
                long currTime = System.currentTimeMillis();
                if (1500 > currTime - this.mLastNotityTime) {
                    HwHiStreamUtils.logD("NOTIFY interval is too short,delay 1s");
                    if (!this.mContentAwareHandler.hasMessages(1)) {
                        this.mContentAwareHandler.sendEmptyMessageDelayed(1, 1500 - (currTime - this.mLastNotityTime));
                    }
                } else {
                    if (100105 == appScene || 100106 == appScene) {
                        if ((this.mWechatStateInfo == null || 100 == appState) && this.mWechatConfig != null) {
                            this.mWechatStateInfo = new HwAPPStateInfo();
                            this.mWechatStateInfo.copyObjectValue(this.mWechatConfig);
                        }
                        curStateInfo = this.mWechatStateInfo;
                    } else {
                        if ((this.mAppStateInfo == null || 100 == appState) && this.mAppConfig != null) {
                            this.mAppStateInfo = new HwAPPStateInfo();
                            this.mAppStateInfo.copyObjectValue(this.mAppConfig);
                        }
                        curStateInfo = this.mAppStateInfo;
                    }
                    if (curStateInfo == null) {
                        HwHiStreamUtils.logD("handleNotifyAppStateChange: curStateInfo is NULL ");
                        return;
                    }
                    curStateInfo.mAppState = appState;
                    curStateInfo.mScenceId = appScene;
                    if (this.mHiStreamCallback != null) {
                        this.mHiStreamCallback.onAPPStateChangeCallback(curStateInfo, appState);
                    }
                    this.mLastNotityTime = System.currentTimeMillis();
                    if (100501 == appScene && curStateInfo.mIsAppFirstStart) {
                        this.mContentAwareHandler.removeMessages(15);
                        if (100 == appState) {
                            this.mContentAwareHandler.sendEmptyMessageDelayed(15, 4000);
                        }
                    }
                }
            }
        }
    }

    public HwAPPStateInfo getCurAPPStateInfo(int appSceneId) {
        if ((100105 == appSceneId || 100106 == appSceneId) && this.mWechatStateInfo != null && appSceneId == this.mWechatStateInfo.mScenceId) {
            return this.mWechatStateInfo;
        }
        if (this.mAppStateInfo == null || this.mAppStateInfo.mScenceId != appSceneId) {
            return null;
        }
        return this.mAppStateInfo;
    }

    public void onNodataDetected() {
        HwHiStreamUtils.logD("No data,call end");
        this.mAudioRecordState = 1;
        if (!this.mContentAwareHandler.hasMessages(1)) {
            this.mContentAwareHandler.sendEmptyMessage(1);
        }
    }

    public void handleForeActivityChange(Message msg) {
        if (msg != null && msg.obj != null) {
            Bundle bundle = (Bundle) msg.obj;
            int pid = bundle.getInt("pid");
            int uid = bundle.getInt("uid");
            ComponentName componentName = (ComponentName) bundle.getParcelable("comp");
            if (componentName != null) {
                HwHiStreamUtils.logD("activityResumed:pid=" + pid + ", uid=" + uid + ", component=" + componentName);
            }
            HwAPPStateInfo foregroundApp = null;
            this.foregroundUid = uid;
            String className = componentName != null ? componentName.getClassName() : "";
            String packageName = componentName != null ? componentName.getPackageName() : "";
            HwAPPQoEAPKConfig mHwAPPQoEAPKConfig = null;
            this.mIsPlayActivity = false;
            HwAPPStateInfo curAppInfo = getCurStreamAppInfo();
            if (this.mHwAPPQoEResourceManger != null) {
                mHwAPPQoEAPKConfig = this.mHwAPPQoEResourceManger.checkIsMonitorVideoScence(packageName, className);
            }
            if (mHwAPPQoEAPKConfig == null) {
                HwHiStreamUtils.logD("mHwAPPQoEAPKConfig is null");
                this.mTopActivity = -1;
                if (!(curAppInfo == null || 101 == curAppInfo.mAppState || this.mContentAwareHandler.hasMessages(1))) {
                    this.mContentAwareHandler.sendEmptyMessage(1);
                }
                return;
            }
            if (100105 == mHwAPPQoEAPKConfig.mScenceId) {
                this.mTopActivity = 1;
                this.mWechatConfig = getAPPStateInfo(mHwAPPQoEAPKConfig, uid);
                if (!this.mContentAwareHandler.hasMessages(1)) {
                    this.mContentAwareHandler.sendEmptyMessage(1);
                }
                foregroundApp = this.mWechatConfig;
            } else if (100501 == mHwAPPQoEAPKConfig.mScenceId || 100701 == mHwAPPQoEAPKConfig.mScenceId) {
                this.mTopActivity = 2;
                this.mAppConfig = getAPPStateInfo(mHwAPPQoEAPKConfig, uid);
                if (-1 != mHwAPPQoEAPKConfig.mPlayActivity) {
                    this.mIsPlayActivity = true;
                    this.mEnterPlayActTime = System.currentTimeMillis();
                }
                if (!this.mContentAwareHandler.hasMessages(1)) {
                    this.mContentAwareHandler.sendEmptyMessage(1);
                }
                foregroundApp = this.mAppConfig;
            } else if (!(curAppInfo == null || 101 == curAppInfo.mAppState)) {
                this.mTopActivity = -1;
                if (!this.mContentAwareHandler.hasMessages(1)) {
                    this.mContentAwareHandler.sendEmptyMessage(1);
                }
            }
            if (foregroundApp != null) {
                synchronized (this.mLock) {
                    if (isAppFirstStart(foregroundApp.mAppUID)) {
                        HwHiStreamUtils.logD("handleForeActivityChange app first start,uid = " + foregroundApp.mAppUID);
                        foregroundApp.mIsAppFirstStart = true;
                        this.mAppList.add(foregroundApp);
                    }
                }
            }
        }
    }

    private HwAPPStateInfo getAPPStateInfo(HwAPPQoEAPKConfig mHwAPPQoEAPKConfig, int uid) {
        if (mHwAPPQoEAPKConfig == null) {
            return null;
        }
        HwAPPStateInfo mAPPStateInfo = new HwAPPStateInfo();
        mAPPStateInfo.mAppId = mHwAPPQoEAPKConfig.mAppId;
        mAPPStateInfo.mScenceId = mHwAPPQoEAPKConfig.mScenceId;
        mAPPStateInfo.mAppType = 4000;
        mAPPStateInfo.mAppUID = uid;
        MpLinkQuickSwitchConfiguration switchConfiguration = new MpLinkQuickSwitchConfiguration();
        if (100501 == mHwAPPQoEAPKConfig.mScenceId) {
            switchConfiguration.setNetworkStrategy(0);
            switchConfiguration.setSocketStrategy(3);
        } else {
            switchConfiguration.setNetworkStrategy(0);
            switchConfiguration.setSocketStrategy(3);
        }
        mAPPStateInfo.setMpLinkQuickSwitchConfiguration(switchConfiguration);
        return mAPPStateInfo;
    }

    private boolean isCameraOn() {
        if (this.mCameraId == null || this.mCameraId.equals("none")) {
            return false;
        }
        return true;
    }

    public HwAPPStateInfo getCurStreamAppInfo() {
        if (this.mAppStateInfo != null && 101 != this.mAppStateInfo.mAppState) {
            return this.mAppStateInfo;
        }
        if (this.mWechatStateInfo == null || 101 == this.mWechatStateInfo.mAppState) {
            return null;
        }
        return this.mWechatStateInfo;
    }

    public boolean isInPlayActivity() {
        long curTime = System.currentTimeMillis();
        if (!mHwHiStreamContentAware.mIsPlayActivity || 2000 > curTime - mHwHiStreamContentAware.mEnterPlayActTime) {
            return false;
        }
        return true;
    }

    private void registerProcessStatusObserver() {
        try {
            if (this.mActivityManager != null) {
                this.mActivityManager.registerProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            HwHiStreamUtils.logD("registerProcessStatusObserver failed!");
        }
    }

    /* access modifiers changed from: private */
    public void removeCurAliveAppList(int uid) {
        int listSize = this.mAppList.size();
        int i = 0;
        while (i < listSize) {
            HwAPPStateInfo appInfo = this.mAppList.get(i);
            if (appInfo == null || uid != appInfo.mAppUID) {
                i++;
            } else {
                HwHiStreamUtils.logD("removeCurAliveAppList:  uid= " + uid);
                this.mAppList.remove(i);
                return;
            }
        }
    }

    private boolean isAppFirstStart(int uid) {
        int listSize = this.mAppList.size();
        for (int i = 0; i < listSize; i++) {
            HwAPPStateInfo appInfo = this.mAppList.get(i);
            if (appInfo != null && uid == appInfo.mAppUID) {
                return false;
            }
        }
        return true;
    }
}
