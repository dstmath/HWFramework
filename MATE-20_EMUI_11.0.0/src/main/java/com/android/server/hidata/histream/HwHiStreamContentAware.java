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
import android.media.AudioManager;
import android.media.AudioRecordingConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import com.android.server.hidata.appqoe.HwAPPQoEAPKConfig;
import com.android.server.hidata.appqoe.HwAPPQoEResourceManger;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.intellicom.common.SmartDualCardConsts;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class HwHiStreamContentAware {
    private static final String ACTION_AUDIO_RECORD_STATE_CHANGED = "huawei.media.AUDIO_RECORD_STATE_CHANGED_ACTION";
    private static final int APPS_INIT_NUMBER = 16;
    private static final int APP_MONITOR_INTERVAL = 1500;
    private static final int AUDIO_RECORD_STATE_RECORDING = 3;
    private static final int AUDIO_RECORD_STATE_STOPPED = 1;
    private static final int HW_KARAOKE_EFFECT_BIT = 2;
    private static final String INTENT_PACKAGENAME = "packagename";
    private static final String INTENT_STATE = "state";
    private static final int MINTIME_STALL_REPORT_ENTER_PLAY_ACT = 2000;
    private static final String PERMISSION_SEND_AUDIO_RECORD_STATE = "com.huawei.permission.AUDIO_RECORD_STATE_CHANGED_ACTION";
    private static final int PIDS_INIT_NUMBER = 4;
    private static final int RECORDING_SOURCE_VOIP = 7;
    private static final int SENSITIVE_APP_NUMBER = 4;
    private static final int SYSTEM_APP_UIDS = 10000;
    private static final int TIME_CHECK_APP_FIRST_VIDEO = 4000;
    private static final int TOP_ACTIVITY_OTHERS = -1;
    private static final int TOP_ACTIVITY_SENSITIVE_APP = 2;
    private static final int TOP_ACTIVITY_WECHAT_CALL = 1;
    private static final int WECHAT_CALL_ACTIVITY_PAUSE = 2;
    private static final int WECHAT_CALL_ACTIVITY_RESUME = 1;
    private static final String WECHAT_NAME = "com.tencent.mm";
    private static HwHiStreamContentAware mHwHiStreamContentAware;
    private int foregroundUid = -1;
    private boolean isPlayActivity;
    private IActivityManager mActivityManager;
    private List<Integer> mAliveAppList;
    private HwAPPStateInfo mAppConfig;
    private Map<Integer, List<Integer>> mAppMap;
    private HwAPPStateInfo mAppStateInfo;
    private AudioManager mAudioManager;
    private int mAudioRecordState = 1;
    private AudioManager.AudioRecordingCallback mAudioRecordingCallback;
    private CameraManager.AvailabilityCallback mAvailabilityCallback;
    private String mCameraId;
    private CameraManager mCameraManager;
    private Handler mContentAwareHandler;
    private Context mContext;
    private long mEnterPlayActTime;
    private IHwHiStreamCallback mHiStreamCallback;
    private HwAPPQoEResourceManger mHwAPPQoEResourceManger;
    private boolean mIsScreenOn = true;
    private boolean mIsUpgradeVideo;
    private long mLastNotityTime;
    private final Object mLock = new Object();
    private HiStreamProcessObserver mProcessObserver;
    private DynamicReceiver mReceiver;
    private int mTopActivity = -1;
    private HwAPPStateInfo mWechatConfig;
    private HwAPPStateInfo mWechatStateInfo;

    private HwHiStreamContentAware(Context context, IHwHiStreamCallback callback, Handler handler) {
        boolean isKaraokeEffectEnabled = false;
        this.mIsUpgradeVideo = false;
        this.isPlayActivity = false;
        this.mLastNotityTime = 0;
        this.mEnterPlayActTime = 0;
        this.mCameraId = "none";
        this.mWechatStateInfo = null;
        this.mAppStateInfo = null;
        this.mWechatConfig = null;
        this.mAppConfig = null;
        this.mAppMap = new ConcurrentHashMap(16);
        this.mAliveAppList = new ArrayList(4);
        this.mAudioRecordingCallback = new AudioManager.AudioRecordingCallback() {
            /* class com.android.server.hidata.histream.HwHiStreamContentAware.AnonymousClass1 */

            @Override // android.media.AudioManager.AudioRecordingCallback
            public void onRecordingConfigChanged(List<AudioRecordingConfiguration> configs) {
                boolean isRecording = HwHiStreamContentAware.this.isRecordingInVoip(configs);
                HwHiStreamUtils.logD(false, "onRecordingConfigChanged isRecording = %{public}s, mTopActivity = %{public}d, mAudioRecordState = %{public}d", String.valueOf(isRecording), Integer.valueOf(HwHiStreamContentAware.this.mTopActivity), Integer.valueOf(HwHiStreamContentAware.this.mAudioRecordState));
                if (!isRecording) {
                    if (HwHiStreamContentAware.this.mAudioRecordState == 3) {
                        HwHiStreamContentAware.this.mAudioRecordState = 1;
                        HwHiStreamContentAware.this.sendAppMonitorMessage(0);
                    }
                } else if (HwHiStreamContentAware.this.mAudioRecordState == 1 && HwHiStreamContentAware.this.mTopActivity == 1) {
                    HwHiStreamContentAware.this.mAudioRecordState = 3;
                    HwHiStreamContentAware.this.sendAppMonitorMessage(0);
                }
            }
        };
        this.mAvailabilityCallback = new CameraManager.AvailabilityCallback() {
            /* class com.android.server.hidata.histream.HwHiStreamContentAware.AnonymousClass2 */

            @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
            public void onCameraAvailable(String cameraId) {
                HwHiStreamUtils.logD(false, "onCameraAvailable cameraId = %{public}s", cameraId);
                if (cameraId != null && cameraId.equals(HwHiStreamContentAware.this.mCameraId)) {
                    HwHiStreamContentAware.this.mCameraId = "none";
                    HwHiStreamContentAware.this.mIsUpgradeVideo = false;
                    if (HwHiStreamContentAware.this.mWechatStateInfo != null && HwHiStreamContentAware.this.mWechatStateInfo.mAppState != 101 && HwHiStreamContentAware.this.mWechatStateInfo.mScenceId == 100106 && !HwHiStreamContentAware.this.mContentAwareHandler.hasMessages(1)) {
                        HwHiStreamContentAware.this.mContentAwareHandler.sendEmptyMessageDelayed(1, 500);
                    }
                }
            }

            @Override // android.hardware.camera2.CameraManager.AvailabilityCallback
            public void onCameraUnavailable(String cameraId) {
                boolean isAudio = false;
                HwHiStreamUtils.logD(false, "onCameraUnavailable cameraId = %{public}s", cameraId);
                if (cameraId != null) {
                    HwHiStreamContentAware.this.mCameraId = cameraId;
                    boolean isCallActivity = HwHiStreamContentAware.this.mTopActivity == 1;
                    if (HwHiStreamContentAware.this.mWechatStateInfo != null && HwHiStreamContentAware.this.mWechatStateInfo.mScenceId == 100105) {
                        isAudio = true;
                    }
                    if (isCallActivity && isAudio && HwHiStreamContentAware.this.mIsScreenOn) {
                        HwHiStreamContentAware.this.mIsUpgradeVideo = true;
                        HwHiStreamContentAware.this.sendAppMonitorMessage(0);
                    }
                }
            }
        };
        this.mContext = context;
        this.mHiStreamCallback = callback;
        this.mContentAwareHandler = handler;
        this.mHwAPPQoEResourceManger = HwAPPQoEResourceManger.getInstance();
        this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
        this.mCameraManager.registerAvailabilityCallback(this.mAvailabilityCallback, (Handler) null);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mActivityManager = ActivityManagerNative.getDefault();
        this.mProcessObserver = new HiStreamProcessObserver();
        this.mReceiver = new DynamicReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
        if (!((SystemProperties.getInt("ro.config.hw_media_flags", 0) & 2) != 0 ? true : isKaraokeEffectEnabled)) {
            this.mContext.registerReceiver(this.mReceiver, filter);
            this.mAudioManager.registerAudioRecordingCallback(this.mAudioRecordingCallback, this.mContentAwareHandler);
        } else {
            filter.addAction(ACTION_AUDIO_RECORD_STATE_CHANGED);
            this.mContext.registerReceiver(this.mReceiver, filter, PERMISSION_SEND_AUDIO_RECORD_STATE, null);
        }
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
        HwAPPStateInfo hwAPPStateInfo = this.mWechatStateInfo;
        if (hwAPPStateInfo == null || hwAPPStateInfo.mAppState == 101) {
            return false;
        }
        boolean isEndOrDegrade = false;
        if (((!isCameraOn() && this.mWechatStateInfo.mScenceId == 100106) && (this.mTopActivity == 1 && this.mIsScreenOn)) || this.mAudioRecordState == 1) {
            isEndOrDegrade = true;
        }
        return isEndOrDegrade;
    }

    private boolean isWechatCallUpgrade() {
        HwAPPStateInfo hwAPPStateInfo = this.mWechatStateInfo;
        if (hwAPPStateInfo == null || !this.mIsScreenOn) {
            return false;
        }
        boolean isAudioForground = true;
        if (!(hwAPPStateInfo.mAppState == 100 || this.mWechatStateInfo.mAppState == 103) || this.mWechatStateInfo.mScenceId != 100105) {
            isAudioForground = false;
        }
        if (!isAudioForground || !this.mIsUpgradeVideo) {
            return false;
        }
        this.mIsUpgradeVideo = false;
        return true;
    }

    public void handleAppMonotor() {
        HwHiStreamUtils.logD(false, "handleAppMonotor enter", new Object[0]);
        new Bundle();
        if (isWechatCallEndOrDegrade() || isWechatCallUpgrade()) {
            HwHiStreamUtils.logD(false, "notify wechat call end, mCurrWeChatType= %{public}d", Integer.valueOf(this.mWechatStateInfo.mScenceId));
            notifyAppStatChange(this.mWechatStateInfo.mScenceId, 101);
            sendAppMonitorMessage(1500);
            return;
        }
        int i = this.mTopActivity;
        if (i == 2) {
            handleSensitiveAppActivity();
        } else if (i == 1) {
            handleWechatActivity();
        } else {
            handleOtherActivity();
        }
    }

    public void handleNotifyAppStateChange(Message msg) {
        HwAPPStateInfo curStateInfo;
        if (msg != null && msg.obj != null) {
            Bundle bundle = (Bundle) msg.obj;
            int appScene = bundle.getInt("appScene");
            int appState = bundle.getInt("appState");
            boolean isAppInitial = true;
            HwHiStreamUtils.logD(false, "handleNotifyAppStateChange appScene=%{public}d,appState%{public}d", Integer.valueOf(appScene), Integer.valueOf(appState));
            if (-1 != appScene) {
                long currTime = System.currentTimeMillis();
                if (1500 > currTime - this.mLastNotityTime) {
                    HwHiStreamUtils.logD(false, "NOTIFY interval is too short,delay 1s", new Object[0]);
                    sendAppMonitorMessage(1500 - (currTime - this.mLastNotityTime));
                    return;
                }
                if (appScene == 100105 || appScene == 100106) {
                    if (!(this.mWechatStateInfo == null || appState == 100)) {
                        isAppInitial = false;
                    }
                    if (isAppInitial && this.mWechatConfig != null) {
                        this.mWechatStateInfo = new HwAPPStateInfo();
                        this.mWechatStateInfo.copyObjectValue(this.mWechatConfig);
                    }
                    curStateInfo = this.mWechatStateInfo;
                } else {
                    if (!(this.mAppStateInfo == null || appState == 100)) {
                        isAppInitial = false;
                    }
                    if (isAppInitial && this.mAppConfig != null) {
                        this.mAppStateInfo = new HwAPPStateInfo();
                        this.mAppStateInfo.copyObjectValue(this.mAppConfig);
                    }
                    curStateInfo = this.mAppStateInfo;
                }
                if (curStateInfo == null) {
                    HwHiStreamUtils.logD(false, "handleNotifyAppStateChange: curStateInfo is NULL ", new Object[0]);
                    return;
                }
                curStateInfo.mAppState = appState;
                curStateInfo.mScenceId = appScene;
                IHwHiStreamCallback iHwHiStreamCallback = this.mHiStreamCallback;
                if (iHwHiStreamCallback != null) {
                    iHwHiStreamCallback.onAPPStateChangeCallback(curStateInfo, appState);
                }
                this.mLastNotityTime = System.currentTimeMillis();
                sendCheckFirstVideoEvent(curStateInfo);
            }
        }
    }

    public HwAPPStateInfo getCurAPPStateInfo(int appSceneId) {
        HwAPPStateInfo hwAPPStateInfo = this.mWechatStateInfo;
        if (hwAPPStateInfo != null && appSceneId == hwAPPStateInfo.mScenceId) {
            return this.mWechatStateInfo;
        }
        HwAPPStateInfo hwAPPStateInfo2 = this.mAppStateInfo;
        if (hwAPPStateInfo2 == null || appSceneId != hwAPPStateInfo2.mScenceId) {
            return null;
        }
        return this.mAppStateInfo;
    }

    public void onNodataDetected() {
        HwHiStreamUtils.logD(false, "No data,call end", new Object[0]);
        this.mAudioRecordState = 1;
        sendAppMonitorMessage(0);
    }

    class DynamicReceiver extends BroadcastReceiver {
        DynamicReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action;
            if (intent != null && (action = intent.getAction()) != null) {
                String packageName = intent.getStringExtra(HwHiStreamContentAware.INTENT_PACKAGENAME);
                int state = intent.getIntExtra(HwHiStreamContentAware.INTENT_STATE, 0);
                HwHiStreamUtils.logD(false, "Intent received, action=%{public}s, package %{public}s, state %{public}d", action, packageName, Integer.valueOf(state));
                if (!HwHiStreamContentAware.ACTION_AUDIO_RECORD_STATE_CHANGED.equals(action) || packageName == null || !HwHiStreamContentAware.WECHAT_NAME.equals(packageName)) {
                    if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(action)) {
                        HwHiStreamContentAware.this.mIsScreenOn = true;
                        if (HwHiStreamContentAware.this.mTopActivity == 1) {
                            HwHiStreamContentAware.this.sendAppMonitorMessage(0);
                        }
                    } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                        HwHiStreamContentAware.this.mIsScreenOn = false;
                        if (HwHiStreamContentAware.this.mTopActivity == 1) {
                            HwHiStreamContentAware.this.sendAppMonitorMessage(0);
                        }
                    }
                } else if (1 == state && 3 == HwHiStreamContentAware.this.mAudioRecordState) {
                    HwHiStreamContentAware.this.mAudioRecordState = 1;
                    HwHiStreamContentAware.this.sendAppMonitorMessage(0);
                } else if (3 == state) {
                    HwHiStreamContentAware.this.mAudioRecordState = 3;
                    HwHiStreamContentAware.this.sendAppMonitorMessage(0);
                }
            }
        }
    }

    public void handleForeActivityChange(Message msg) {
        if (msg != null && msg.obj != null) {
            Bundle bundle = (Bundle) msg.obj;
            bundle.getInt("pid");
            int uid = bundle.getInt("uid");
            ComponentName componentName = (ComponentName) bundle.getParcelable("comp");
            String className = componentName != null ? componentName.getClassName() : "";
            String packageName = componentName != null ? componentName.getPackageName() : "";
            HwAPPQoEAPKConfig mHwAPPQoEAPKConfig = null;
            HwAPPStateInfo foregroundApp = null;
            this.isPlayActivity = false;
            HwAPPStateInfo curAppInfo = getCurStreamAppInfo();
            synchronized (this.mLock) {
                this.foregroundUid = uid;
            }
            HwAPPQoEResourceManger hwAPPQoEResourceManger = this.mHwAPPQoEResourceManger;
            if (hwAPPQoEResourceManger != null) {
                mHwAPPQoEAPKConfig = hwAPPQoEResourceManger.checkIsMonitorVideoScence(packageName, className);
            }
            if (mHwAPPQoEAPKConfig == null) {
                HwHiStreamUtils.logD(false, "mHwAPPQoEAPKConfig is null", new Object[0]);
                this.mTopActivity = -1;
                if (curAppInfo != null && curAppInfo.mAppState != 101) {
                    sendAppMonitorMessage(0);
                    return;
                }
                return;
            }
            if (mHwAPPQoEAPKConfig.mScenceId == 100105) {
                this.mTopActivity = 1;
                this.mWechatConfig = getAppStateInfo(mHwAPPQoEAPKConfig, uid);
                sendAppMonitorMessage(0);
                foregroundApp = this.mWechatConfig;
            } else if (isSensitiveApp(mHwAPPQoEAPKConfig.mScenceId)) {
                this.mTopActivity = 2;
                this.mAppConfig = getAppStateInfo(mHwAPPQoEAPKConfig, uid);
                if (mHwAPPQoEAPKConfig.getPlayActivity() != -1) {
                    this.isPlayActivity = true;
                    this.mEnterPlayActTime = System.currentTimeMillis();
                }
                sendAppMonitorMessage(0);
                foregroundApp = this.mAppConfig;
            } else if (curAppInfo == null || curAppInfo.mAppState == 101) {
                this.mTopActivity = -1;
            } else {
                this.mTopActivity = -1;
                sendAppMonitorMessage(0);
            }
            checkForegroundAppFirstStart(foregroundApp);
        }
    }

    private HwAPPStateInfo getAppStateInfo(HwAPPQoEAPKConfig hwAppQoeApkConfig, int uid) {
        if (hwAppQoeApkConfig == null) {
            return null;
        }
        HwAPPStateInfo appStateInfo = new HwAPPStateInfo();
        appStateInfo.mAppId = hwAppQoeApkConfig.mAppId;
        appStateInfo.mScenceId = hwAppQoeApkConfig.mScenceId;
        appStateInfo.mAppType = 4000;
        appStateInfo.mAppUID = uid;
        appStateInfo.setAppRegion(hwAppQoeApkConfig.getAppRegion());
        return appStateInfo;
    }

    private boolean isCameraOn() {
        String str = this.mCameraId;
        if (str == null || str.equals("none")) {
            return false;
        }
        return true;
    }

    public HwAPPStateInfo getCurStreamAppInfo() {
        HwAPPStateInfo hwAPPStateInfo = this.mAppStateInfo;
        if (hwAPPStateInfo != null && hwAPPStateInfo.mAppState != 101) {
            return this.mAppStateInfo;
        }
        HwAPPStateInfo hwAPPStateInfo2 = this.mWechatStateInfo;
        if (hwAPPStateInfo2 == null || hwAPPStateInfo2.mAppState == 101) {
            return null;
        }
        return this.mWechatStateInfo;
    }

    public boolean isInPlayActivity() {
        long curTime = System.currentTimeMillis();
        HwHiStreamContentAware hwHiStreamContentAware = mHwHiStreamContentAware;
        if (!hwHiStreamContentAware.isPlayActivity || curTime - hwHiStreamContentAware.mEnterPlayActTime < 2000) {
            return false;
        }
        return true;
    }

    public void onActivityResume(Bundle extras) {
        Handler handler = this.mContentAwareHandler;
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(13, extras));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAppMonitorMessage(long delayMillis) {
        if (this.mContentAwareHandler.hasMessages(1)) {
            return;
        }
        if (delayMillis == 0) {
            this.mContentAwareHandler.sendEmptyMessage(1);
        } else {
            this.mContentAwareHandler.sendEmptyMessageDelayed(1, 1500);
        }
    }

    private void notifyAppStatChange(int appScene, int appState) {
        Bundle bundle = new Bundle();
        bundle.putInt("appScene", appScene);
        bundle.putInt("appState", appState);
        Handler handler = this.mContentAwareHandler;
        handler.sendMessage(handler.obtainMessage(2, bundle));
    }

    private void handleSensitiveAppActivity() {
        if (this.mAppConfig != null) {
            HwAPPStateInfo hwAPPStateInfo = this.mAppStateInfo;
            if (hwAPPStateInfo == null || hwAPPStateInfo.mAppState != 100) {
                HwHiStreamUtils.logD(false, "notify sensitive App Start", new Object[0]);
                notifyAppStatChange(this.mAppConfig.mScenceId, 100);
            } else if (this.mAppConfig.mScenceId != this.mAppStateInfo.mScenceId) {
                HwHiStreamUtils.logD(false, "notify sensitive App End, mScenceId = %{public}d", Integer.valueOf(this.mAppStateInfo.mScenceId));
                notifyAppStatChange(this.mAppStateInfo.mScenceId, 101);
                sendAppMonitorMessage(1500);
            }
        }
    }

    private void handleWechatActivity() {
        HwAPPStateInfo hwAPPStateInfo = this.mAppStateInfo;
        if (hwAPPStateInfo == null || hwAPPStateInfo.mAppState != 100) {
            HwAPPStateInfo hwAPPStateInfo2 = this.mWechatStateInfo;
            int appScene = HwAPPQoEUtils.SCENE_VIDEO;
            if (hwAPPStateInfo2 == null || hwAPPStateInfo2.mAppState == 101) {
                if (this.mAudioRecordState == 3) {
                    if (!isCameraOn()) {
                        appScene = 100105;
                    }
                    if (appScene == 100105 || this.mIsScreenOn) {
                        HwHiStreamUtils.logD(false, "notify wechat call start, appscene = %{public}d", Integer.valueOf(appScene));
                        notifyAppStatChange(appScene, 100);
                    }
                }
            } else if (this.mWechatStateInfo.mAppState == 104) {
                HwHiStreamUtils.logD(false, "notify wechat forground ,appscene= %{public}d", Integer.valueOf(this.mWechatStateInfo.mScenceId));
                notifyAppStatChange(this.mWechatStateInfo.mScenceId, 103);
            } else if (!this.mIsScreenOn && this.mWechatStateInfo.mScenceId == 100106) {
                HwHiStreamUtils.logD(false, "-----notify wechat background ,mCurrWeChatType= %{public}d", Integer.valueOf(this.mWechatStateInfo.mScenceId));
                notifyAppStatChange(this.mWechatStateInfo.mScenceId, 104);
            }
        } else {
            HwHiStreamUtils.logD(false, "notify sensitive App End, mScenceId = %{public}d", Integer.valueOf(this.mAppStateInfo.mScenceId));
            notifyAppStatChange(this.mAppStateInfo.mScenceId, 101);
            sendAppMonitorMessage(1500);
        }
    }

    private void handleOtherActivity() {
        HwAPPStateInfo hwAPPStateInfo = this.mAppStateInfo;
        if (hwAPPStateInfo == null || hwAPPStateInfo.mAppState != 100) {
            HwAPPStateInfo hwAPPStateInfo2 = this.mWechatStateInfo;
            if (hwAPPStateInfo2 == null) {
                return;
            }
            if (hwAPPStateInfo2.mAppState == 103 || this.mWechatStateInfo.mAppState == 100) {
                HwHiStreamUtils.logD(false, "notify wechat background ,mCurrWeChatType= %{public}d", Integer.valueOf(this.mWechatStateInfo.mScenceId));
                notifyAppStatChange(this.mWechatStateInfo.mScenceId, 104);
                return;
            }
            return;
        }
        HwHiStreamUtils.logD(false, "notify sensitive App End, mScenceId = %{public}d", Integer.valueOf(this.mAppStateInfo.mScenceId));
        notifyAppStatChange(this.mAppStateInfo.mScenceId, 101);
        sendAppMonitorMessage(1500);
    }

    public HwAPPStateInfo getCurAppInfo() {
        HwAPPStateInfo hwAPPStateInfo = this.mAppStateInfo;
        if (hwAPPStateInfo != null) {
            return hwAPPStateInfo;
        }
        HwAPPStateInfo hwAPPStateInfo2 = this.mWechatStateInfo;
        if (hwAPPStateInfo2 != null) {
            return hwAPPStateInfo2;
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isRecordingInVoip(List<AudioRecordingConfiguration> configs) {
        if (configs != null && configs.stream().filter($$Lambda$HwHiStreamContentAware$FQCGMgJW34Tu20WvlPZRFZFDHG0.INSTANCE).count() > 0) {
            return true;
        }
        return false;
    }

    static /* synthetic */ boolean lambda$isRecordingInVoip$0(AudioRecordingConfiguration config) {
        return config != null && config.getClientAudioSource() == 7;
    }

    private void sendCheckFirstVideoEvent(HwAPPStateInfo curStateInfo) {
        if (curStateInfo != null && curStateInfo.mScenceId == 100501 && curStateInfo.getIsAppFirstStart()) {
            this.mContentAwareHandler.removeMessages(16);
            if (curStateInfo.mAppState == 100) {
                this.mContentAwareHandler.sendEmptyMessageDelayed(16, 4000);
            }
        }
    }

    private boolean isSensitiveApp(int appScene) {
        if (appScene == 100501 || appScene == 100901 || appScene == 100701 || appScene == 101101) {
            return true;
        }
        return false;
    }

    private void checkForegroundAppFirstStart(HwAPPStateInfo foregroundApp) {
        if (foregroundApp != null) {
            synchronized (this.mLock) {
                if (!this.mAliveAppList.contains(Integer.valueOf(foregroundApp.mAppUID))) {
                    HwHiStreamUtils.logD(false, "App first start, uid = %{public}d", Integer.valueOf(foregroundApp.mAppUID));
                    foregroundApp.setIsAppFirstStart(true);
                    this.mAliveAppList.add(Integer.valueOf(foregroundApp.mAppUID));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class HiStreamProcessObserver extends IProcessObserver.Stub {
        private HiStreamProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean isForeground) {
            if (isForeground && uid > 10000) {
                HwHiStreamContentAware.this.addPidToAppList(uid, pid);
            }
        }

        public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) {
        }

        public void onProcessDied(int pid, int uid) {
            if (uid > 10000) {
                HwHiStreamContentAware.this.delPidToAppList(uid, pid);
            }
        }
    }

    private void registerProcessStatusObserver() {
        try {
            if (this.mActivityManager != null) {
                this.mActivityManager.registerProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            HwHiStreamUtils.logD(false, "registerProcessStatusObserver failed!", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addPidToAppList(int uid, int pid) {
        List<Integer> pidList;
        if (this.mAppMap.containsKey(Integer.valueOf(uid))) {
            pidList = this.mAppMap.get(Integer.valueOf(uid));
        } else {
            pidList = new ArrayList<>(4);
        }
        if (pidList != null && !pidList.contains(Integer.valueOf(pid))) {
            pidList.add(Integer.valueOf(pid));
            this.mAppMap.put(Integer.valueOf(uid), pidList);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void delPidToAppList(int uid, int pid) {
        List<Integer> pidList;
        if (this.mAppMap.containsKey(Integer.valueOf(uid)) && (pidList = this.mAppMap.get(Integer.valueOf(uid))) != null && pidList.contains(Integer.valueOf(pid))) {
            pidList.removeIf(new Predicate(pid) {
                /* class com.android.server.hidata.histream.$$Lambda$HwHiStreamContentAware$UZOxZALRCMzMJHczNHIwgwI5P0E */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return HwHiStreamContentAware.lambda$delPidToAppList$1(this.f$0, (Integer) obj);
                }
            });
            if (pidList.size() == 0) {
                this.mAppMap.remove(Integer.valueOf(uid));
                onAppDied(uid);
            }
        }
    }

    static /* synthetic */ boolean lambda$delPidToAppList$1(int pid, Integer item) {
        return item.intValue() == pid;
    }

    private void onAppDied(int uid) {
        synchronized (this.mLock) {
            this.mAliveAppList.removeIf(new Predicate(uid) {
                /* class com.android.server.hidata.histream.$$Lambda$HwHiStreamContentAware$LopAkNfyaezEbtq9vzoumBVQ */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return HwHiStreamContentAware.lambda$onAppDied$2(this.f$0, (Integer) obj);
                }
            });
            HwHiStreamUtils.logD(false, "onAppDied, uid = %{public}d", Integer.valueOf(uid));
        }
    }

    static /* synthetic */ boolean lambda$onAppDied$2(int uid, Integer item) {
        return item.intValue() == uid;
    }
}
