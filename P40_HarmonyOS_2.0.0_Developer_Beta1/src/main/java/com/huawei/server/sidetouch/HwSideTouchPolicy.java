package com.huawei.server.sidetouch;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.hardware.input.HwExtEventListener;
import com.huawei.android.hardware.input.HwInputManager;
import com.huawei.android.hardware.input.HwSideTouchManagerEx;
import com.huawei.android.media.AudioPlaybackConfigurationEx;
import com.huawei.android.media.AudioSystemEx;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HwSideTouchPolicy extends DefaultHwSideTouchPolicy {
    private static final String INCALLUI_PACKAGE_NAME = "com.android.incallui";
    private static final String INCALLUI_WINDOW_TITLE = "com.android.incallui.InCallActivity";
    private static final List<String> INTERCEPT_APP_LIST = Arrays.asList("com.huawei.camera/com.huawei.camera", "com.android.deskclock/com.android.deskclock.timer.TimerAlertActivity", "com.huawei.deskclock/com.android.deskclock.timer.TimerAlertActivity", "com.android.deskclock/com.android.deskclock.alarmclock.LockAlarmFullActivity", "com.huawei.deskclock/com.android.deskclock.alarmclock.LockAlarmFullActivity", "com.huawei.camera/com.huawei.camera.controller.SecureCameraActivity");
    public static final int PRODUCT_SIDE_TOUCH_ONLY = 1;
    public static final int PRODUCT_SIDE_WITH_SOLID = 2;
    private static final String STATUSBAR_WINDOW = "StatusBar";
    private static final String TAG = "HwSideTouchPolicy";
    public static final int TARGET_ALL_SIDE_PRODUCT = 3;
    private static HwSideTouchPolicy sInstance;
    private AudioManager.AudioPlaybackCallback mAudioPlaybackCallback;
    private Context mContext;
    private HwExtEventListener mExtEventListener;
    private boolean mIsAudioPlayingAtScreenOff;
    private boolean mIsDownVolumeKeyDownIntercept;
    private boolean mIsLandscape;
    private boolean mIsSideConfigEnabled;
    private boolean mIsSideTouchEvent;
    private boolean mIsUpVolumeKeyDownIntercept;
    private HwPhoneWindowManager mPolicy;
    private int mProductMode = 1;
    private HwSideStatusManager mSideStatusManager;
    private HwSideTouchDataReport mSideTouchDataReport;
    private IHwSideTouchCallback mSideTouchHandConfigCallback;
    private HwSideTouchManager mSideTouchManager;
    private HwSideVibrationManager mSideVibrationManager;
    private boolean mSystemReady;

    private HwSideTouchPolicy(Context context) {
        this.mContext = context;
        this.mSideTouchManager = HwSideTouchManager.getInstance(context);
        this.mSideStatusManager = HwSideStatusManager.getInstance(context);
        this.mSideVibrationManager = HwSideVibrationManager.getInstance(context);
        this.mSideTouchDataReport = HwSideTouchDataReport.getInstance(context);
        int mode = HwSideTouchManagerEx.getInstance().getSideTouchMode();
        if (mode == HwSideTouchManagerEx.SIDE_TOUCH_WITHOUT_SOLID) {
            this.mProductMode = 1;
        } else if (mode == HwSideTouchManagerEx.SIDE_TOUCH_WITH_SOLID) {
            this.mProductMode = 2;
        }
    }

    public static synchronized HwSideTouchPolicy getInstance(Context context) {
        HwSideTouchPolicy hwSideTouchPolicy;
        synchronized (HwSideTouchPolicy.class) {
            if (sInstance == null) {
                sInstance = new HwSideTouchPolicy(context);
            }
            hwSideTouchPolicy = sInstance;
        }
        return hwSideTouchPolicy;
    }

    public void systemReady() {
        this.mSystemReady = true;
        HwDisplaySideRegionConfig.getInstance().systemReady();
        this.mPolicy = WindowManagerPolicyEx.getInstance().getHwPhoneWindowManager();
        initSideConfigCallback();
        initAudioCallback();
        initExtEventListener();
        this.mIsSideConfigEnabled = this.mSideTouchManager.isSideConfigEnabled();
        Log.i(TAG, "systemReady enabled:" + this.mIsSideConfigEnabled + ", product:" + this.mProductMode);
        this.mSideTouchManager.registerCallback(this.mSideTouchHandConfigCallback);
        this.mSideTouchManager.systemReady();
        HwSideTouchManagerEx.getInstance().registerListener(this.mContext, this.mExtEventListener);
    }

    public boolean interceptVolumeKey(KeyEvent event, boolean isInjected, boolean isScreenOn, int keyCode, boolean isDown) {
        return adjustInterceptState(keyCode, isDown, interceptVolumeKeyInternal(event, isInjected, isScreenOn, keyCode, isDown));
    }

    public boolean shouldSendToSystemMediaSession(KeyEvent event, boolean isInjected, boolean isScreenOn, boolean keyguardActive) {
        boolean shouldSend = false;
        if (!this.mSystemReady || !this.mIsSideConfigEnabled || isInjected || event == null || !this.mSideStatusManager.isSideTouchEvent(event) || isTalkbackEnable() || shouldSendVolumeKeyForWhitelistApp()) {
            return false;
        }
        if (isScreenOn) {
            if (SideTouchConst.DEBUG) {
                Log.i(TAG, "send volume key to media session directly");
            }
            this.mIsSideTouchEvent = true;
            shouldSend = true;
            boolean isAudioPlaying = false;
            if (keyguardActive) {
                isAudioPlaying = this.mSideStatusManager.isAudioPlaybackActive();
            }
            if (isAudioPlaying || !keyguardActive) {
                this.mSideTouchManager.notifySendVolumeKeyToSystem(event);
            }
        }
        return shouldSend;
    }

    public boolean isSideTouchEvent(KeyEvent event, boolean isInjected) {
        if (!this.mSystemReady || !this.mIsSideConfigEnabled || isInjected || event == null || !this.mSideStatusManager.isSideTouchEvent(event)) {
            return false;
        }
        return true;
    }

    public void notifyVolumePanelStatus(boolean isVolumePanelVisible) {
        if (SideTouchConst.DEBUG) {
            Log.i(TAG, "notifyVolumePanelStatus visible:" + isVolumePanelVisible + ", enable:" + this.mIsSideConfigEnabled);
        }
        if (this.mIsSideConfigEnabled) {
            this.mSideVibrationManager.onVolumePanelVisibleChanged(isVolumePanelVisible);
        }
    }

    public void screenTurnedOn() {
        if (this.mIsSideConfigEnabled) {
            this.mIsAudioPlayingAtScreenOff = false;
            this.mSideStatusManager.unregisterAudioPlaybackListener(this.mAudioPlaybackCallback);
        }
    }

    public void screenTurnedOff(boolean isProximityPositive) {
        if (this.mIsSideConfigEnabled) {
            this.mIsAudioPlayingAtScreenOff = this.mSideStatusManager.isAudioPlaybackActive();
            updateAudioStatus();
            if (!isProximityPositive) {
                this.mSideStatusManager.registerAudioPlaybackListener(this.mAudioPlaybackCallback, null);
            }
        }
    }

    public boolean isTalkbackEnable() {
        return this.mSideTouchManager.getTalkBackEnableState();
    }

    public boolean isMusicOnly(boolean isScreenOn) {
        if (!this.mIsSideTouchEvent) {
            return true;
        }
        this.mIsSideTouchEvent = false;
        if (isScreenOn) {
            return false;
        }
        return true;
    }

    public void onRotationChanged(int rotation) {
        boolean z = true;
        if (!(rotation == 1 || rotation == 3)) {
            z = false;
        }
        this.mIsLandscape = z;
    }

    public boolean checkVolumeTriggerStatusAndReset() {
        return this.mSideStatusManager.checkVolumeTriggerStatusAndReset();
    }

    public int[] runSideTouchCommandByType(int type, Bundle bundle) {
        boolean isVolumePanelVisible = true;
        if (type == 1 && bundle != null) {
            if (bundle.getInt("guiState", 0) != 1) {
                isVolumePanelVisible = false;
            }
            notifyVolumePanelStatus(isVolumePanelVisible);
        }
        return this.mSideTouchManager.runSideTouchCommand(type, bundle);
    }

    private boolean interceptVolumeKeyInternal(KeyEvent event, boolean isInjected, boolean isScreenOn, int keyCode, boolean isDown) {
        boolean isIntercepted = false;
        if (!this.mSystemReady || isInjected || event == null || !isVolumeKey(keyCode)) {
            return false;
        }
        if (!this.mIsSideConfigEnabled) {
            if (isDown) {
                this.mSideTouchDataReport.reportVolumeCount(false, isInCallUiForeground(), this.mIsLandscape);
            }
            return false;
        }
        boolean isSideEvent = this.mSideStatusManager.isSideTouchEvent(event);
        if (isDown) {
            this.mSideTouchDataReport.reportVolumeCount(isSideEvent, isInCallUiForeground(), this.mIsLandscape);
        }
        if (isSideEvent && isForegroundAppIntercepted(isScreenOn)) {
            isIntercepted = true;
        }
        if (isIntercepted) {
            Log.i(TAG, "side volume key is intercepted for current focus app");
        } else {
            notifyVolumeEventBeforeDispatchToUser(event, isInjected, isScreenOn, isSideEvent);
        }
        return isIntercepted;
    }

    private boolean adjustInterceptState(int keyCode, boolean isDown, boolean isIntercepted) {
        boolean adjustState = isIntercepted;
        if (isDown) {
            if (keyCode == 25) {
                this.mIsDownVolumeKeyDownIntercept = isIntercepted;
            } else if (keyCode == 24) {
                this.mIsUpVolumeKeyDownIntercept = isIntercepted;
            }
        } else if (keyCode == 25) {
            adjustState = this.mIsDownVolumeKeyDownIntercept;
        } else if (keyCode == 24) {
            adjustState = this.mIsUpVolumeKeyDownIntercept;
        }
        if (adjustState != isIntercepted) {
            Log.i(TAG, "Focus may changed in down and up, keycode:" + keyCode + ", adjustState:" + adjustState);
        }
        return adjustState;
    }

    private boolean isVolumeKey(int keyCode) {
        return keyCode == 25 || keyCode == 24;
    }

    private WindowManagerPolicyEx.WindowStateEx getFocusWindow() {
        return WindowManagerPolicyEx.getFocusedWindow();
    }

    private void initSideConfigCallback() {
        this.mSideTouchHandConfigCallback = new IHwSideTouchCallback() {
            /* class com.huawei.server.sidetouch.HwSideTouchPolicy.AnonymousClass1 */

            @Override // com.huawei.server.sidetouch.IHwSideTouchCallback
            public void notifySideConfig(String sideConfig, boolean isSelfChange) {
                boolean lastState = HwSideTouchPolicy.this.mIsSideConfigEnabled;
                HwSideTouchPolicy hwSideTouchPolicy = HwSideTouchPolicy.this;
                hwSideTouchPolicy.mIsSideConfigEnabled = hwSideTouchPolicy.mSideTouchManager.isSideConfigEnabled(sideConfig);
                Log.i(HwSideTouchPolicy.TAG, "notifySideConfig config:" + sideConfig + ", isEnabled:" + HwSideTouchPolicy.this.mIsSideConfigEnabled + ", lastState:" + lastState + ", self:" + isSelfChange);
                HwInputManager.setTouchscreenFeatureConfig(8, sideConfig);
                HwSideTouchPolicy.this.updateAudioStatus();
                Log.i(HwSideTouchPolicy.TAG, "notifySideConfig to touchscreen finished");
                if (HwSideTouchPolicy.this.mIsSideConfigEnabled != lastState && !isSelfChange) {
                    HwSideTouchPolicy.this.mSideTouchDataReport.reportSideTouchConfigChanged(HwSideTouchPolicy.this.mIsSideConfigEnabled);
                }
            }
        };
    }

    private void initAudioCallback() {
        this.mAudioPlaybackCallback = new AudioManager.AudioPlaybackCallback() {
            /* class com.huawei.server.sidetouch.HwSideTouchPolicy.AnonymousClass2 */

            @Override // android.media.AudioManager.AudioPlaybackCallback
            public void onPlaybackConfigChanged(List<AudioPlaybackConfiguration> configs) {
                super.onPlaybackConfigChanged(configs);
                if (configs != null) {
                    boolean isPlaying = false;
                    Iterator<AudioPlaybackConfiguration> it = configs.iterator();
                    while (true) {
                        if (it.hasNext()) {
                            if (AudioPlaybackConfigurationEx.isActive(it.next())) {
                                isPlaying = true;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    HwSideTouchPolicy.this.mIsAudioPlayingAtScreenOff = isPlaying;
                    Log.i(HwSideTouchPolicy.TAG, "AudioManagerPlaybackListener isPlaying:" + isPlaying);
                    HwSideTouchPolicy.this.updateAudioStatus();
                }
            }
        };
    }

    private void initExtEventListener() {
        this.mExtEventListener = new HwExtEventListener() {
            /* class com.huawei.server.sidetouch.HwSideTouchPolicy.AnonymousClass3 */

            public void onHwTpEvent(int eventClass, int eventCode, String extraInfo) {
            }

            public void onHwSideEvent(int event) {
                HwSideTouchPolicy.this.mSideVibrationManager.notifyThpEvent(event);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAudioStatus() {
        String status = this.mIsSideConfigEnabled ? this.mSideStatusManager.getAudioStatus(this.mIsAudioPlayingAtScreenOff) : HwSideStatusManager.AUDIO_STATE_NONE;
        int result = HwInputManager.setTouchscreenFeatureConfig(3, status);
        Log.i(TAG, "updateAudioStatus active:" + this.mIsAudioPlayingAtScreenOff + ", status:" + status + ", enabled:" + this.mIsSideConfigEnabled + ", result:" + result);
    }

    private boolean shouldSendVolumeKeyForWhitelistApp() {
        WindowManagerPolicyEx.WindowStateEx focusedWindow = getFocusWindow();
        if (focusedWindow == null) {
            return false;
        }
        WindowManager.LayoutParams params = focusedWindow.getAttrs();
        String packageName = params != null ? params.packageName : null;
        if (packageName == null) {
            Log.i(TAG, "packageName is null");
            return false;
        }
        HwDisplaySideRegionConfig configInstance = HwDisplaySideRegionConfig.getInstance();
        if (configInstance == null || !configInstance.isAppShouldSendVolumeKey(packageName, this.mProductMode)) {
            return false;
        }
        if (!SideTouchConst.DEBUG) {
            return true;
        }
        Log.i(TAG, "send volume key for current app:" + packageName);
        return true;
    }

    private boolean isForegroundAppIntercepted(boolean isScreenOn) {
        WindowManagerPolicyEx.WindowStateEx focusedWindow = getFocusWindow();
        if (focusedWindow == null) {
            return false;
        }
        String winTitle = String.valueOf(focusedWindow.getAttrs().getTitle());
        if (TextUtils.isEmpty(winTitle)) {
            return false;
        }
        if (INTERCEPT_APP_LIST.contains(winTitle)) {
            return true;
        }
        if (this.mProductMode != 2 || !isScreenOn || !isInKeygaurdMainWindow(winTitle) || isAudioActive()) {
            return false;
        }
        return true;
    }

    private boolean isInKeygaurdMainWindow(String windowTitle) {
        HwPhoneWindowManager hwPhoneWindowManager;
        return STATUSBAR_WINDOW.equals(windowTitle) && (hwPhoneWindowManager = this.mPolicy) != null && hwPhoneWindowManager.isKeyguardShowingAndNotOccluded();
    }

    private boolean isAudioActive() {
        if (AudioSystemEx.isStreamActive(3, 0) || AudioSystemEx.isStreamActive(0, 0) || AudioSystemEx.isStreamActive(6, 0)) {
            return true;
        }
        return false;
    }

    private void notifyVolumeEventBeforeDispatchToUser(KeyEvent event, boolean isInjected, boolean isScreenOn, boolean isSideEvent) {
        if (event.getAction() == 0) {
            this.mSideTouchManager.onVolumeEvent(event, isSideEvent);
            if (isSideEvent) {
                this.mSideStatusManager.updateVolumeTriggerStatus(event);
                this.mSideVibrationManager.onSideVolumeEvent(event, this.mIsAudioPlayingAtScreenOff, isScreenOn);
                this.mSideTouchDataReport.reportVolumeBtnKeyEvent(event);
                return;
            }
            this.mSideVibrationManager.onNormalVolumeEvent(event, isInjected, isScreenOn);
        }
    }

    private boolean isInCallUiForeground() {
        return isTargetWindowFocused(getFocusWindow(), INCALLUI_PACKAGE_NAME, INCALLUI_WINDOW_TITLE);
    }

    private boolean isTargetWindowFocused(WindowManagerPolicyEx.WindowStateEx focusedWindow, String packageName, String windowTitle) {
        if (focusedWindow == null || focusedWindow.getAttrs() == null) {
            return false;
        }
        String focusPackageName = focusedWindow.getAttrs().packageName;
        String focusWindowTitle = focusedWindow.getAttrs().getTitle().toString();
        if (focusPackageName == null || focusWindowTitle == null || !focusPackageName.equals(packageName) || !focusWindowTitle.contains(windowTitle)) {
            return false;
        }
        return true;
    }
}
