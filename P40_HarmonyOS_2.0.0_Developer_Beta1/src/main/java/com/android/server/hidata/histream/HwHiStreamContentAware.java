package com.android.server.hidata.histream;

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
import android.os.SystemProperties;
import com.android.server.hidata.appqoe.HwAppQoeApkConfig;
import com.android.server.hidata.appqoe.HwAppQoeResourceManager;
import com.android.server.hidata.appqoe.HwAppQoeUtils;
import com.android.server.hidata.appqoe.HwAppStateInfo;
import com.android.server.intellicom.common.SmartDualCardConsts;
import java.util.List;

public class HwHiStreamContentAware {
    private static final String ACTION_AUDIO_RECORD_STATE_CHANGED = "huawei.media.AUDIO_RECORD_STATE_CHANGED_ACTION";
    private static final int APP_MONITOR_INTERVAL = 1500;
    private static final int AUDIO_RECORD_STATE_RECORDING = 3;
    private static final int AUDIO_RECORD_STATE_STOPPED = 1;
    private static final int HW_KARAOKE_EFFECT_BIT = 2;
    private static final String INTENT_PACKAGE_NAME = "packagename";
    private static final String INTENT_STATE = "state";
    private static final String PERMISSION_SEND_AUDIO_RECORD_STATE = "com.huawei.permission.AUDIO_RECORD_STATE_CHANGED_ACTION";
    private static final int RECORDING_SOURCE_VOIP = 7;
    private static final int TOP_ACTIVITY_OTHERS = -1;
    private static final int TOP_ACTIVITY_SENSITIVE_APP = 2;
    private static final int TOP_ACTIVITY_WECHAT_CALL = 1;
    private static final String WECHAT_NAME = "com.tencent.mm";
    private static HwHiStreamContentAware sHwHiStreamContentAware;
    private boolean isScreenOn;
    private boolean isUpgradeVideo;
    private HwAppStateInfo mAppConfig;
    private HwAppStateInfo mAppStateInfo;
    private AudioManager mAudioManager;
    private int mAudioRecordState;
    private AudioManager.AudioRecordingCallback mAudioRecordingCallback;
    private CameraManager.AvailabilityCallback mAvailabilityCallback;
    private String mCameraId;
    private CameraManager mCameraManager;
    private Handler mContentAwareHandler;
    private Context mContext;
    private IHwHiStreamCallback mHiStreamCallback;
    private HwAppQoeResourceManager mHwAppQoeResourceManager;
    private long mLastNotifyTime;
    private DynamicReceiver mReceiver;
    private int mTopActivity = -1;
    private HwAppStateInfo mWechatConfig;
    private HwAppStateInfo mWechatStateInfo;

    private HwHiStreamContentAware(Context context, IHwHiStreamCallback callback, Handler handler) {
        boolean isKaraokeEffectEnabled = true;
        this.mAudioRecordState = 1;
        this.isScreenOn = true;
        this.isUpgradeVideo = false;
        this.mLastNotifyTime = 0;
        this.mCameraId = "none";
        this.mWechatStateInfo = null;
        this.mAppStateInfo = null;
        this.mWechatConfig = null;
        this.mAppConfig = null;
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
                    HwHiStreamContentAware.this.isUpgradeVideo = false;
                    if (HwHiStreamContentAware.this.mWechatStateInfo != null && HwHiStreamContentAware.this.mWechatStateInfo.mAppState != 101 && HwHiStreamContentAware.this.mWechatStateInfo.mScenesId == 100106 && !HwHiStreamContentAware.this.mContentAwareHandler.hasMessages(1)) {
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
                    if (HwHiStreamContentAware.this.mWechatStateInfo != null && HwHiStreamContentAware.this.mWechatStateInfo.mScenesId == 100105) {
                        isAudio = true;
                    }
                    if (isCallActivity && isAudio && HwHiStreamContentAware.this.isScreenOn) {
                        HwHiStreamContentAware.this.isUpgradeVideo = true;
                        HwHiStreamContentAware.this.sendAppMonitorMessage(0);
                    }
                }
            }
        };
        this.mContext = context;
        this.mHiStreamCallback = callback;
        this.mContentAwareHandler = handler;
        this.mHwAppQoeResourceManager = HwAppQoeResourceManager.getInstance();
        this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
        this.mCameraManager.registerAvailabilityCallback(this.mAvailabilityCallback, (Handler) null);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mReceiver = new DynamicReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
        if (!((SystemProperties.getInt("ro.config.hw_media_flags", 0) & 2) == 0 ? false : isKaraokeEffectEnabled)) {
            this.mContext.registerReceiver(this.mReceiver, filter);
            this.mAudioManager.registerAudioRecordingCallback(this.mAudioRecordingCallback, this.mContentAwareHandler);
            return;
        }
        filter.addAction(ACTION_AUDIO_RECORD_STATE_CHANGED);
        this.mContext.registerReceiver(this.mReceiver, filter, PERMISSION_SEND_AUDIO_RECORD_STATE, null);
    }

    public static HwHiStreamContentAware createInstance(Context context, IHwHiStreamCallback callback, Handler handler) {
        if (sHwHiStreamContentAware == null) {
            sHwHiStreamContentAware = new HwHiStreamContentAware(context, callback, handler);
        }
        return sHwHiStreamContentAware;
    }

    public static HwHiStreamContentAware getInstance() {
        return sHwHiStreamContentAware;
    }

    private boolean isWechatCallEndOrDegrade() {
        HwAppStateInfo hwAppStateInfo = this.mWechatStateInfo;
        if (hwAppStateInfo == null || hwAppStateInfo.mAppState == 101) {
            return false;
        }
        boolean isEndOrDegrade = false;
        if (((!isCameraOn() && this.mWechatStateInfo.mScenesId == 100106) && (this.mTopActivity == 1 && this.isScreenOn)) || this.mAudioRecordState == 1) {
            isEndOrDegrade = true;
        }
        return isEndOrDegrade;
    }

    private boolean isWechatCallUpgrade() {
        HwAppStateInfo hwAppStateInfo = this.mWechatStateInfo;
        if (hwAppStateInfo == null || !this.isScreenOn) {
            return false;
        }
        boolean isAudioForeground = true;
        if (!(hwAppStateInfo.mAppState == 100 || this.mWechatStateInfo.mAppState == 103) || this.mWechatStateInfo.mScenesId != 100105) {
            isAudioForeground = false;
        }
        if (!isAudioForeground || !this.isUpgradeVideo) {
            return false;
        }
        this.isUpgradeVideo = false;
        return true;
    }

    public void handleAppMonitor() {
        HwHiStreamUtils.logD(false, "handleAppMonitor enter", new Object[0]);
        if (isWechatCallEndOrDegrade() || isWechatCallUpgrade()) {
            HwHiStreamUtils.logD(false, "notify wechat call end, mCurrWeChatType= %{public}d", Integer.valueOf(this.mWechatStateInfo.mScenesId));
            notifyAppStatChange(this.mWechatStateInfo.mScenesId, 101);
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
        HwAppStateInfo curStateInfo;
        if (msg != null && msg.obj != null) {
            Bundle bundle = (Bundle) msg.obj;
            int appScene = bundle.getInt("appScene");
            int appState = bundle.getInt("appState");
            boolean isAppInitial = true;
            HwHiStreamUtils.logD(false, "handleNotifyAppStateChange appScene=%{public}d,appState%{public}d", Integer.valueOf(appScene), Integer.valueOf(appState));
            if (appScene != -1) {
                long currTime = System.currentTimeMillis();
                if (currTime - this.mLastNotifyTime < 1500) {
                    HwHiStreamUtils.logD(false, "notify interval is too short, delay 1s", new Object[0]);
                    sendAppMonitorMessage(1500 - (currTime - this.mLastNotifyTime));
                    return;
                }
                if (appScene == 100105 || appScene == 100106) {
                    if (!(this.mWechatStateInfo == null || appState == 100)) {
                        isAppInitial = false;
                    }
                    if (isAppInitial && this.mWechatConfig != null) {
                        this.mWechatStateInfo = new HwAppStateInfo();
                        this.mWechatStateInfo.copyObjectValue(this.mWechatConfig);
                    }
                    curStateInfo = this.mWechatStateInfo;
                } else {
                    if (!(this.mAppStateInfo == null || appState == 100)) {
                        isAppInitial = false;
                    }
                    if (isAppInitial && this.mAppConfig != null) {
                        this.mAppStateInfo = new HwAppStateInfo();
                        this.mAppStateInfo.copyObjectValue(this.mAppConfig);
                    }
                    curStateInfo = this.mAppStateInfo;
                }
                if (curStateInfo == null) {
                    HwHiStreamUtils.logD(false, "handleNotifyAppStateChange: curStateInfo is null", new Object[0]);
                    return;
                }
                curStateInfo.mAppState = appState;
                curStateInfo.mScenesId = appScene;
                IHwHiStreamCallback iHwHiStreamCallback = this.mHiStreamCallback;
                if (iHwHiStreamCallback != null) {
                    iHwHiStreamCallback.onAppStateChangeCallback(curStateInfo, appState);
                }
                this.mLastNotifyTime = System.currentTimeMillis();
            }
        }
    }

    public void onNoDataDetected() {
        HwHiStreamUtils.logD(false, "no data, call end", new Object[0]);
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
                String packageName = intent.getStringExtra(HwHiStreamContentAware.INTENT_PACKAGE_NAME);
                int state = intent.getIntExtra(HwHiStreamContentAware.INTENT_STATE, 0);
                HwHiStreamUtils.logD(false, "Intent received, action=%{public}s, package %{public}s, state %{public}d", action, packageName, Integer.valueOf(state));
                if (!HwHiStreamContentAware.ACTION_AUDIO_RECORD_STATE_CHANGED.equals(action) || !HwHiStreamContentAware.WECHAT_NAME.equals(packageName)) {
                    if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(action)) {
                        HwHiStreamContentAware.this.isScreenOn = true;
                        if (HwHiStreamContentAware.this.mTopActivity == 1) {
                            HwHiStreamContentAware.this.sendAppMonitorMessage(0);
                        }
                    } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                        HwHiStreamContentAware.this.isScreenOn = false;
                        if (HwHiStreamContentAware.this.mTopActivity == 1) {
                            HwHiStreamContentAware.this.sendAppMonitorMessage(0);
                        }
                    }
                } else if (state == 1 && HwHiStreamContentAware.this.mAudioRecordState == 3) {
                    HwHiStreamContentAware.this.mAudioRecordState = 1;
                    HwHiStreamContentAware.this.sendAppMonitorMessage(0);
                } else if (state == 3) {
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
            String packageName = "";
            String className = componentName != null ? componentName.getClassName() : packageName;
            if (componentName != null) {
                packageName = componentName.getPackageName();
            }
            HwAppQoeApkConfig mHwAppQoeApkConfig = null;
            HwAppStateInfo curAppInfo = getCurStreamAppInfo();
            HwAppQoeResourceManager hwAppQoeResourceManager = this.mHwAppQoeResourceManager;
            if (hwAppQoeResourceManager != null) {
                mHwAppQoeApkConfig = hwAppQoeResourceManager.checkIsMonitorVideoScenes(packageName, className);
            }
            if (mHwAppQoeApkConfig == null) {
                HwHiStreamUtils.logD(false, "mHwAppQoeApkConfig is null", new Object[0]);
                this.mTopActivity = -1;
                if (curAppInfo != null && curAppInfo.mAppState != 101) {
                    sendAppMonitorMessage(0);
                }
            } else if (mHwAppQoeApkConfig.mScenesId == 100105) {
                this.mTopActivity = 1;
                this.mWechatConfig = getAppStateInfo(mHwAppQoeApkConfig, uid);
                sendAppMonitorMessage(0);
            } else if (isSensitiveApp(mHwAppQoeApkConfig.mScenesId)) {
                this.mTopActivity = 2;
                this.mAppConfig = getAppStateInfo(mHwAppQoeApkConfig, uid);
                sendAppMonitorMessage(0);
            } else if (curAppInfo == null || curAppInfo.mAppState == 101) {
                this.mTopActivity = -1;
            } else {
                this.mTopActivity = -1;
                sendAppMonitorMessage(0);
            }
        }
    }

    private HwAppStateInfo getAppStateInfo(HwAppQoeApkConfig hwAppQoeApkConfig, int uid) {
        if (hwAppQoeApkConfig == null) {
            return null;
        }
        HwAppStateInfo appStateInfo = new HwAppStateInfo();
        appStateInfo.mAppId = hwAppQoeApkConfig.mAppId;
        appStateInfo.mScenesId = hwAppQoeApkConfig.mScenesId;
        appStateInfo.mAppType = HwAppQoeUtils.APP_TYPE_STREAMING;
        appStateInfo.mAppUid = uid;
        appStateInfo.setAppRegion(hwAppQoeApkConfig.getAppRegion());
        return appStateInfo;
    }

    private boolean isCameraOn() {
        String str = this.mCameraId;
        if (str == null || "none".equals(str)) {
            return false;
        }
        return true;
    }

    public HwAppStateInfo getCurStreamAppInfo() {
        HwAppStateInfo hwAppStateInfo = this.mAppStateInfo;
        if (hwAppStateInfo != null && hwAppStateInfo.mAppState != 101) {
            return this.mAppStateInfo;
        }
        HwAppStateInfo hwAppStateInfo2 = this.mWechatStateInfo;
        if (hwAppStateInfo2 == null || hwAppStateInfo2.mAppState == 101) {
            return null;
        }
        return this.mWechatStateInfo;
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
            HwAppStateInfo hwAppStateInfo = this.mAppStateInfo;
            if (hwAppStateInfo == null || hwAppStateInfo.mAppState != 100) {
                HwHiStreamUtils.logD(false, "notify sensitive App Start", new Object[0]);
                notifyAppStatChange(this.mAppConfig.mScenesId, 100);
            } else if (this.mAppConfig.mScenesId != this.mAppStateInfo.mScenesId) {
                HwHiStreamUtils.logD(false, "notify sensitive App End, mScenesId = %{public}d", Integer.valueOf(this.mAppStateInfo.mScenesId));
                notifyAppStatChange(this.mAppStateInfo.mScenesId, 101);
                sendAppMonitorMessage(1500);
            }
        }
    }

    private void handleWechatActivity() {
        HwAppStateInfo hwAppStateInfo = this.mAppStateInfo;
        if (hwAppStateInfo == null || hwAppStateInfo.mAppState != 100) {
            HwAppStateInfo hwAppStateInfo2 = this.mWechatStateInfo;
            int appScene = HwAppQoeUtils.SCENE_VIDEO;
            if (hwAppStateInfo2 == null || hwAppStateInfo2.mAppState == 101) {
                if (this.mAudioRecordState == 3) {
                    if (!isCameraOn()) {
                        appScene = 100105;
                    }
                    if (appScene == 100105 || this.isScreenOn) {
                        HwHiStreamUtils.logD(false, "notify wechat call start, app scenes = %{public}d", Integer.valueOf(appScene));
                        notifyAppStatChange(appScene, 100);
                    }
                }
            } else if (this.mWechatStateInfo.mAppState == 104) {
                HwHiStreamUtils.logD(false, "notify wechat foreground, app scenes = %{public}d", Integer.valueOf(this.mWechatStateInfo.mScenesId));
                notifyAppStatChange(this.mWechatStateInfo.mScenesId, 103);
            } else if (!this.isScreenOn && this.mWechatStateInfo.mScenesId == 100106) {
                HwHiStreamUtils.logD(false, "notify wechat background, app scenes = %{public}d", Integer.valueOf(this.mWechatStateInfo.mScenesId));
                notifyAppStatChange(this.mWechatStateInfo.mScenesId, 104);
            }
        } else {
            HwHiStreamUtils.logD(false, "notify sensitive App End, mScenesId = %{public}d", Integer.valueOf(this.mAppStateInfo.mScenesId));
            notifyAppStatChange(this.mAppStateInfo.mScenesId, 101);
            sendAppMonitorMessage(1500);
        }
    }

    private void handleOtherActivity() {
        HwAppStateInfo hwAppStateInfo = this.mAppStateInfo;
        if (hwAppStateInfo == null || hwAppStateInfo.mAppState != 100) {
            HwAppStateInfo hwAppStateInfo2 = this.mWechatStateInfo;
            if (hwAppStateInfo2 == null) {
                return;
            }
            if (hwAppStateInfo2.mAppState == 103 || this.mWechatStateInfo.mAppState == 100) {
                HwHiStreamUtils.logD(false, "notify wechat background ,mCurrWeChatType= %{public}d", Integer.valueOf(this.mWechatStateInfo.mScenesId));
                notifyAppStatChange(this.mWechatStateInfo.mScenesId, 104);
                return;
            }
            return;
        }
        HwHiStreamUtils.logD(false, "notify sensitive App End, mScenesId = %{public}d", Integer.valueOf(this.mAppStateInfo.mScenesId));
        notifyAppStatChange(this.mAppStateInfo.mScenesId, 101);
        sendAppMonitorMessage(1500);
    }

    public HwAppStateInfo getCurAppInfo() {
        HwAppStateInfo hwAppStateInfo = this.mAppStateInfo;
        if (hwAppStateInfo != null) {
            return hwAppStateInfo;
        }
        HwAppStateInfo hwAppStateInfo2 = this.mWechatStateInfo;
        if (hwAppStateInfo2 != null) {
            return hwAppStateInfo2;
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

    private boolean isSensitiveApp(int appScene) {
        if (appScene == 100501 || appScene == 100901 || appScene == 100701 || appScene == 101101) {
            return true;
        }
        return false;
    }
}
