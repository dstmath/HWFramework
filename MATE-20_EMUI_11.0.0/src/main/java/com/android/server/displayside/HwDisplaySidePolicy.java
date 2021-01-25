package com.android.server.displayside;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioSystem;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.android.server.LocalServices;
import com.android.server.cust.utils.HwCustPkgNameConstant;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.huawei.android.hardware.input.HwInputManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.view.HwExtDisplaySizeUtil;
import com.huawei.sidetouch.HwSideStatusManager;
import com.huawei.sidetouch.HwSideTouchDataReport;
import com.huawei.sidetouch.HwSideTouchManager;
import com.huawei.sidetouch.HwSideVibrationManager;
import com.huawei.sidetouch.IHwSideTouchCallback;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HwDisplaySidePolicy {
    private static final boolean DEBUG;
    private static final String INCALLUI_PACKAGE_NAME = "com.android.incallui";
    private static final String INCALLUI_WINDOW_TITLE = "com.android.incallui.InCallActivity";
    private static final List<String> INTERCEPT_APP_LIST = Arrays.asList("com.huawei.camera/com.huawei.camera", "com.android.deskclock/com.android.deskclock.timer.TimerAlertActivity", "com.huawei.deskclock/com.android.deskclock.timer.TimerAlertActivity", "com.android.deskclock/com.android.deskclock.alarmclock.LockAlarmFullActivity", HwCustPkgNameConstant.HW_DESKLOCK_FULL_ACTIVITYNAME, "com.huawei.camera/com.huawei.camera.controller.SecureCameraActivity");
    public static final int PRODUCT_SIDE_TOUCH_ONLY = 1;
    public static final int PRODUCT_SIDE_WITH_SOLID = 2;
    private static final String STATUSBAR_WINDOW = "StatusBar";
    private static final String TAG = "HwDisplaySidePolicy";
    public static final int TARGET_ALL_SIDE_PRODUCT = 3;
    private AudioManager.AudioPlaybackCallback mAudioPlaybackCallback;
    private Context mContext;
    private boolean mIsAudioPlayingAtScreenOff;
    private boolean mIsDownVolumeKeyDownIntercept;
    private boolean mIsLandscape;
    private boolean mIsSideConfigEnabled;
    private boolean mIsSideTouchEvent;
    private boolean mIsUpVolumeKeyDownIntercept;
    private WindowManagerPolicy mPolicy;
    private int mProductMode = 1;
    private HwSideStatusManager mSideStatusManager;
    private HwSideTouchDataReport mSideTouchDataReport;
    private IHwSideTouchCallback mSideTouchHandConfigCallback;
    private HwSideTouchManager mSideTouchManager;
    private HwSideVibrationManager mSideVibrationManager;
    private boolean mSystemReady;

    static {
        boolean z = true;
        if (!("1".equals(SystemPropertiesEx.get("ro.debuggable", "0")) || SystemPropertiesEx.getInt("ro.logsystem.usertype", 1) == 3 || SystemPropertiesEx.getInt("ro.logsystem.usertype", 1) == 5)) {
            z = false;
        }
        DEBUG = z;
    }

    public HwDisplaySidePolicy(Context context) {
        this.mContext = context;
        this.mSideTouchManager = HwSideTouchManager.getInstance(context);
        this.mSideStatusManager = HwSideStatusManager.getInstance(context);
        this.mSideVibrationManager = HwSideVibrationManager.getInstance(context);
        this.mSideTouchDataReport = HwSideTouchDataReport.getInstance(context);
        HwExtDisplaySizeUtil util = HwExtDisplaySizeUtil.getInstance();
        if (util != null) {
            int mode = util.getSideTouchMode();
            if (mode == 1) {
                this.mProductMode = 1;
            } else if (mode == 2) {
                this.mProductMode = 2;
            }
        }
    }

    public void systemReady() {
        this.mSystemReady = true;
        this.mPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
        initSideConfigCallback();
        initAudioCallback();
        this.mIsSideConfigEnabled = this.mSideTouchManager.isSideConfigEnabled();
        Log.i(TAG, "systemReady enabled:" + this.mIsSideConfigEnabled + ", product:" + this.mProductMode);
        this.mSideTouchManager.registerCallback(this.mSideTouchHandConfigCallback);
        this.mSideTouchManager.systemReady(true);
    }

    public boolean interceptVolumeKey(KeyEvent event, boolean isInjected, boolean isScreenOn, int keyCode, boolean isDown) {
        return adjustInterceptState(keyCode, isDown, interceptVolumeKeyInternal(event, isInjected, isScreenOn, keyCode, isDown));
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
        boolean isAdjustState = isIntercepted;
        if (isDown) {
            if (keyCode == 25) {
                this.mIsDownVolumeKeyDownIntercept = isIntercepted;
            } else if (keyCode == 24) {
                this.mIsUpVolumeKeyDownIntercept = isIntercepted;
            }
        } else if (keyCode == 25) {
            isAdjustState = this.mIsDownVolumeKeyDownIntercept;
        } else if (keyCode == 24) {
            isAdjustState = this.mIsUpVolumeKeyDownIntercept;
        }
        if (isAdjustState != isIntercepted) {
            Log.i(TAG, "Focus may changed in down and up, keycode=" + keyCode + ", adjustState:" + isAdjustState);
        }
        return isAdjustState;
    }

    public boolean shouldSendToSystemMediaSession(KeyEvent event, boolean isInjected, boolean isScreenOn, boolean keyguardActive) {
        boolean shouldSend = false;
        if (!this.mSystemReady || !this.mIsSideConfigEnabled || isInjected || event == null || !this.mSideStatusManager.isSideTouchEvent(event) || isTalkbackEnable() || shouldSendVolumeKeyForWhitelistApp()) {
            return false;
        }
        if (isScreenOn) {
            if (DEBUG) {
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
        if (DEBUG) {
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
                this.mSideStatusManager.registerAudioPlaybackListener(this.mAudioPlaybackCallback, (Handler) null);
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

    private boolean isVolumeKey(int keyCode) {
        return keyCode == 25 || keyCode == 24;
    }

    private WindowManagerPolicy.WindowState getFocusWindow() {
        HwPhoneWindowManager policy = this.mPolicy;
        if (policy instanceof HwPhoneWindowManager) {
            return policy.getFocusedWindow();
        }
        return null;
    }

    private void initSideConfigCallback() {
        this.mSideTouchHandConfigCallback = new IHwSideTouchCallback() {
            /* class com.android.server.displayside.HwDisplaySidePolicy.AnonymousClass1 */

            public void notifySideConfig(String sideConfig, boolean isSelfChange) {
                boolean lastState = HwDisplaySidePolicy.this.mIsSideConfigEnabled;
                HwDisplaySidePolicy hwDisplaySidePolicy = HwDisplaySidePolicy.this;
                hwDisplaySidePolicy.mIsSideConfigEnabled = hwDisplaySidePolicy.mSideTouchManager.isSideConfigEnabled(sideConfig);
                Log.i(HwDisplaySidePolicy.TAG, "notifySideConfig config:" + sideConfig + ", isEnabled:" + HwDisplaySidePolicy.this.mIsSideConfigEnabled + ", lastState:" + lastState + ", self:" + isSelfChange);
                HwInputManager.setTouchscreenFeatureConfig(8, sideConfig);
                HwDisplaySidePolicy.this.updateAudioStatus();
                Log.i(HwDisplaySidePolicy.TAG, "notifySideConfig to touchscreen finished");
                if (HwDisplaySidePolicy.this.mIsSideConfigEnabled != lastState && !isSelfChange) {
                    HwDisplaySidePolicy.this.mSideTouchDataReport.reportSideTouchConfigChanged(HwDisplaySidePolicy.this.mIsSideConfigEnabled);
                }
            }
        };
    }

    private void initAudioCallback() {
        this.mAudioPlaybackCallback = new AudioManager.AudioPlaybackCallback() {
            /* class com.android.server.displayside.HwDisplaySidePolicy.AnonymousClass2 */

            @Override // android.media.AudioManager.AudioPlaybackCallback
            public void onPlaybackConfigChanged(List<AudioPlaybackConfiguration> configs) {
                super.onPlaybackConfigChanged(configs);
                if (configs != null) {
                    boolean isPlaying = false;
                    Iterator<AudioPlaybackConfiguration> it = configs.iterator();
                    while (true) {
                        if (it.hasNext()) {
                            if (it.next().getPlayerState() == 2) {
                                isPlaying = true;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    if (HwDisplaySidePolicy.this.mIsAudioPlayingAtScreenOff != isPlaying) {
                        HwDisplaySidePolicy.this.mIsAudioPlayingAtScreenOff = isPlaying;
                        Log.i(HwDisplaySidePolicy.TAG, "AudioManagerPlaybackListener isPlaying:" + isPlaying);
                        HwDisplaySidePolicy.this.updateAudioStatus();
                    }
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAudioStatus() {
        String status;
        if (this.mIsSideConfigEnabled) {
            status = this.mSideStatusManager.getAudioStatus(this.mIsAudioPlayingAtScreenOff);
        } else {
            status = "0";
        }
        int result = HwInputManager.setTouchscreenFeatureConfig(3, status);
        Log.i(TAG, "updateAudioStatus active:" + this.mIsAudioPlayingAtScreenOff + ", status:" + status + ", enabled:" + this.mIsSideConfigEnabled + ", result:" + result);
    }

    private boolean shouldSendVolumeKeyForWhitelistApp() {
        WindowManagerPolicy.WindowState focusedWindow = getFocusWindow();
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
        if (!DEBUG) {
            return true;
        }
        Log.i(TAG, "send volume key for current app:" + packageName);
        return true;
    }

    private boolean isForegroundAppIntercepted(boolean isScreenOn) {
        WindowManagerPolicy.WindowState focusedWindow = getFocusWindow();
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
        WindowManagerPolicy windowManagerPolicy;
        return STATUSBAR_WINDOW.equals(windowTitle) && (windowManagerPolicy = this.mPolicy) != null && windowManagerPolicy.isKeyguardShowingAndNotOccluded();
    }

    private boolean isAudioActive() {
        if (AudioSystem.isStreamActive(3, 0) || AudioSystem.isStreamActive(0, 0) || AudioSystem.isStreamActive(6, 0)) {
            return true;
        }
        return false;
    }

    private void notifyVolumeEventBeforeDispatchToUser(KeyEvent event, boolean isInjected, boolean isScreenOn, boolean isSideEvent) {
        WindowManagerPolicy windowManagerPolicy;
        if (event.getAction() == 0) {
            this.mSideTouchManager.onVolumeEvent(event, isSideEvent);
            if (isSideEvent) {
                this.mSideStatusManager.updateVolumeTriggerStatus(event);
                this.mSideVibrationManager.onSideVolumeEvent(event, this.mIsAudioPlayingAtScreenOff, isScreenOn, isScreenOn && (windowManagerPolicy = this.mPolicy) != null && windowManagerPolicy.isKeyguardShowingAndNotOccluded());
                this.mSideTouchDataReport.reportVolumeBtnKeyEvent(event);
                return;
            }
            this.mSideVibrationManager.onNormalVolumeEvent(event, isInjected, isScreenOn);
        }
    }

    private boolean isInCallUiForeground() {
        return isTargetWindowFocused(getFocusWindow(), INCALLUI_PACKAGE_NAME, INCALLUI_WINDOW_TITLE);
    }

    private boolean isTargetWindowFocused(WindowManagerPolicy.WindowState focusedWindow, String packageName, String windowTitle) {
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
