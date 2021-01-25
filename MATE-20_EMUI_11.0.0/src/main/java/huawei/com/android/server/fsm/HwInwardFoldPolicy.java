package huawei.com.android.server.fsm;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.display.DisplayViewport;
import android.hardware.display.HwFoldScreenState;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;
import android.view.DisplayCutout;
import android.view.SurfaceControl;
import com.android.server.DisplayThread;
import com.android.server.LocalServices;
import com.android.server.display.FoldPolicy;
import com.android.server.fsm.ReportMonitorProcess;
import com.android.server.wm.WindowManagerService;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.os.HwPowerManager;
import java.util.Optional;

public class HwInwardFoldPolicy extends FoldPolicy {
    private static final String DISPLAY_NOTCH_STATUS = "display_notch_status";
    private static final String DISPLAY_NOTCH_STATUS_BAK = "display_notch_status_bak";
    private static final int DISPLAY_NOTCH_STATUS_DEFAULT = 0;
    private static final int DISPLAY_NOTCH_STATUS_HIDE = 1;
    private static final int DISPLAY_NOTCH_STATUS_SHOW = 0;
    private static final int HW_FOLD_SCREEN_MSG_RESUME_BRIGHTNESS_CMD = 3;
    private static final int HW_FOLD_SCREEN_MSG_TIMEOUT_CMD = 1;
    private static final int HW_FOLD_SCREEN_MSG_UNFREEZ_CMD = 2;
    private static final int HW_FOLD_SCREEN_ON_UNBLOCKER_CMD = 4;
    private static final int HW_FOLD_SCREEN_ON_UNBLOCKER_TIMEOUT = 2000;
    private static final int HW_FOLD_SCREEN_RESUME_BRIGHTNESS_TIMEOUT = 4000;
    private static final int HW_FOLD_SCREEN_TIMEOUT = 3000;
    private static final int HW_FOLD_SCREEN_UNFREEZ_DELAY = 50;
    private static final boolean IS_PUNCH_DISABLE = SystemProperties.getBoolean("ro.config.punch_disable", false);
    private static final int REGION_FULL = 67;
    private static final int REGION_FULL_WAKEUP = 195;
    private static final int REGION_INNER_REFRESH = 90;
    private static final int REGION_MAIN_SCREEN_OFF = 72;
    private static final int REGION_MAIN_SCREEN_ON = 88;
    private static final int REGION_OUTER_REFRESH = 83;
    private static final int SUCCESS_RETURN_VALUE = 0;
    private static volatile HwInwardFoldPolicy sInstance;
    private int mDisplayModeReport = 0;
    private HwFoldScreenMsgHandler mHwFoldScreenMsgHandler;
    private boolean mIsBrightnessOff = false;
    private final boolean mIsRogScreen;
    private boolean mIsScreenFrozen = false;
    private int mLastWakeUpWakeReason = 0;
    private Rect mMainDispPhyRect = new Rect();
    private PowerManager mPowerManager;
    private PowerManagerInternal mPowerManagerInternal = ((PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class));
    private HwFoldScreenManagerInternal.ScreenOnUnblockerCallback mScreenOnUnblockerCallback = new HwFoldScreenManagerInternal.ScreenOnUnblockerCallback() {
        /* class huawei.com.android.server.fsm.HwInwardFoldPolicy.AnonymousClass2 */

        public void onScreenOnUnblocker() {
            Slog.i("FoldPolicy", "onScreenOnUnblocker");
            HwInwardFoldPolicy.this.mHwFoldScreenMsgHandler.removeMessages(4);
            HwInwardFoldPolicy.this.sendScreenUnfreezMsg();
        }
    };
    private FoldPolicy.ScreenUnfreezingCallback mScreenUnfreezingCallback = new FoldPolicy.ScreenUnfreezingCallback() {
        /* class huawei.com.android.server.fsm.HwInwardFoldPolicy.AnonymousClass1 */

        public void onScreenUnfreezing() {
            Slog.i("FoldPolicy", "onScreenUnfreezing");
            HwInwardFoldPolicy.this.resumeCurrentBrightness();
        }
    };
    private Rect mSubDispRect = new Rect();
    private WindowManagerService mWms;

    private HwInwardFoldPolicy(Context context) {
        super(context);
        if (HwFoldScreenState.SCREEN_FOLD_MAIN_LOGICAL_HEIGHT == 0 && HwFoldScreenState.SCREEN_FOLD_MAIN_LOGICAL_WIDTH == 0) {
            this.mIsRogScreen = false;
            this.mMainDispRect.set(0, 0, HwFoldScreenState.SCREEN_FOLD_MAIN_WIDTH, HwFoldScreenState.SCREEN_FOLD_MAIN_HEIGHT);
        } else {
            this.mIsRogScreen = true;
            this.mMainDispRect.set(0, 0, HwFoldScreenState.SCREEN_FOLD_MAIN_LOGICAL_WIDTH, HwFoldScreenState.SCREEN_FOLD_MAIN_LOGICAL_HEIGHT);
        }
        this.mMainDispPhyRect.set(0, 0, HwFoldScreenState.SCREEN_FOLD_MAIN_WIDTH, HwFoldScreenState.SCREEN_FOLD_MAIN_HEIGHT);
        this.mFullDispRect.set(0, 0, HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH, HwFoldScreenState.SCREEN_FOLD_FULL_HEIGHT);
        this.mSubDispRect.set(0, 0, 0, 0);
        this.mHwFoldScreenMsgHandler = new HwFoldScreenMsgHandler(DisplayThread.get().getLooper());
        this.mWms = ServiceManager.getService("window");
    }

    public static HwInwardFoldPolicy getInstance(Context context) {
        if (sInstance == null) {
            synchronized (HwInwardFoldPolicy.class) {
                if (sInstance == null) {
                    sInstance = new HwInwardFoldPolicy(context);
                }
            }
        }
        return sInstance;
    }

    public Rect getDispRect(int mode) {
        Rect screenRect;
        if (mode == 1) {
            screenRect = this.mFullDispRect;
        } else if (mode == 2) {
            screenRect = this.mMainDispRect;
        } else {
            screenRect = this.mSubDispRect;
        }
        Slog.d("FoldPolicy", "getDispRect = " + screenRect);
        return screenRect;
    }

    public Rect getScreenDispRect(int orientation) {
        Rect dispRect = new Rect();
        if (orientation < 0 || orientation > 3) {
            return dispRect;
        }
        int currentDisplayMode = getCurrentDisplayMode();
        Rect tmpDispRect = HwFoldScreenState.getScreenPhysicalRect(currentDisplayMode);
        if (currentDisplayMode == 2) {
            if (orientation == 0) {
                dispRect.set(tmpDispRect);
            } else if (orientation == 1) {
                dispRect.left = tmpDispRect.top;
                dispRect.right = tmpDispRect.bottom;
                dispRect.top = HwFoldScreenState.SCREEN_FOLD_MAIN_WIDTH - tmpDispRect.right;
                dispRect.bottom = HwFoldScreenState.SCREEN_FOLD_MAIN_WIDTH - tmpDispRect.left;
            } else if (orientation == 2) {
                dispRect.left = HwFoldScreenState.SCREEN_FOLD_MAIN_WIDTH - tmpDispRect.right;
                dispRect.right = HwFoldScreenState.SCREEN_FOLD_MAIN_WIDTH - tmpDispRect.left;
                dispRect.top = HwFoldScreenState.SCREEN_FOLD_MAIN_HEIGHT - tmpDispRect.bottom;
                dispRect.bottom = HwFoldScreenState.SCREEN_FOLD_MAIN_HEIGHT - tmpDispRect.top;
            } else if (orientation == 3) {
                dispRect.left = HwFoldScreenState.SCREEN_FOLD_MAIN_HEIGHT - tmpDispRect.bottom;
                dispRect.right = HwFoldScreenState.SCREEN_FOLD_MAIN_HEIGHT - tmpDispRect.top;
                dispRect.top = tmpDispRect.left;
                dispRect.bottom = tmpDispRect.right;
            }
        } else if (orientation == 3) {
            dispRect.set(tmpDispRect);
        } else if (orientation == 0) {
            dispRect.left = tmpDispRect.top;
            dispRect.right = tmpDispRect.bottom;
            dispRect.top = HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH - tmpDispRect.right;
            dispRect.bottom = HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH - tmpDispRect.left;
        } else if (orientation == 1) {
            dispRect.left = HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH - tmpDispRect.right;
            dispRect.right = HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH - tmpDispRect.left;
            dispRect.top = HwFoldScreenState.SCREEN_FOLD_REAL_FULL_HEIGHT - tmpDispRect.bottom;
            dispRect.bottom = HwFoldScreenState.SCREEN_FOLD_REAL_FULL_HEIGHT - tmpDispRect.top;
        } else if (orientation == 2) {
            dispRect.left = HwFoldScreenState.SCREEN_FOLD_REAL_FULL_HEIGHT - tmpDispRect.bottom;
            dispRect.right = HwFoldScreenState.SCREEN_FOLD_REAL_FULL_HEIGHT - tmpDispRect.top;
            dispRect.top = tmpDispRect.left;
            dispRect.bottom = tmpDispRect.right;
        }
        Slog.d("FoldPolicy", "getScreenDispRect=" + dispRect + " currentDisplayMode " + currentDisplayMode);
        return dispRect;
    }

    public void adjustViewportFrame(DisplayViewport viewport, Rect layerRect, Rect displayRect) {
    }

    public int getDisplayRotation() {
        if (getCurrentDisplayMode() == 2) {
            return 0;
        }
        return 3;
    }

    public boolean getDisplayCutoutFlag(Resources res) {
        if (getCurrentDisplayMode() == 2) {
            return res.getBoolean(17891480);
        }
        return false;
    }

    public Optional<DisplayCutout> getDisplayCutoutInfo(Resources res, int width, int height) {
        if (getCurrentDisplayMode() == 2) {
            return Optional.ofNullable(DisplayCutout.fromResourcesRectApproximation(res, width, height));
        }
        return Optional.empty();
    }

    private boolean isScreenOn() {
        if (this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
            if (this.mPowerManager == null) {
                Slog.e("FoldPolicy", "mPowerManager is null");
                return false;
            }
        }
        return this.mPowerManager.isScreenOn();
    }

    public int onPreDisplayModeChange(int newDisplayMode) {
        Slog.i("FoldPolicy", "onPreDisplayModeChange newDisplayMode " + newDisplayMode);
        if (this.mFsm == null) {
            this.mFsm = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
            if (this.mFsm == null) {
                Slog.e("FoldPolicy", "onPreDisplayModeChange mFsm is null");
                return 0;
            }
        }
        if (this.mFoldStateChanging || !"1".equals(SystemProperties.get("service.bootanim.exit"))) {
            this.mFsm.pauseDispModeChange();
            Slog.i("FoldPolicy", "onPreDisplayModeChange pauseDispModeChange " + newDisplayMode + " mFoldStateChanging " + this.mFoldStateChanging);
        }
        return 0;
    }

    public int onPostDisplayModeChange(int newDisplayMode) {
        Slog.i("FoldPolicy", "onPostDisplayModeChange newDisplayMode " + newDisplayMode);
        HwFoldScreenMsgHandler hwFoldScreenMsgHandler = this.mHwFoldScreenMsgHandler;
        if (hwFoldScreenMsgHandler != null) {
            hwFoldScreenMsgHandler.removeMessages(1);
            Slog.d("FoldPolicy", "remove HW_FOLD_SCREEN_MSG_TIMEOUT_CMD");
        }
        if (this.mIsRogScreen) {
            setRogSize(newDisplayMode);
        }
        if (registerScreenOnUnBlockerCallback()) {
            sendScreenOnUnblockerMsg();
            return 0;
        }
        sendScreenUnfreezMsg();
        return 0;
    }

    private void getLastWakeUpWakeReason() {
        PowerManager.WakeData lastWakeUp = null;
        PowerManagerInternal powerManagerInternal = this.mPowerManagerInternal;
        if (powerManagerInternal != null) {
            lastWakeUp = powerManagerInternal.getLastWakeup();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("getLastWakeUpWakeReason lastWakeUp ");
        sb.append(lastWakeUp != null ? Integer.valueOf(lastWakeUp.wakeReason) : "null");
        Slog.i("FoldPolicy", sb.toString());
        if (lastWakeUp != null) {
            this.mLastWakeUpWakeReason = lastWakeUp.wakeReason;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resumeCurrentBrightness() {
        Slog.i("FoldPolicy", "resumeCurrentBrightness");
        if (!this.mIsBrightnessOff) {
            Slog.i("FoldPolicy", "resumeCurrentBrightness mIsBrightnessOff is false");
            startDawnAnimaiton();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("UpdateBrightnessEnable", true);
        int ret = HwPowerManager.setHwBrightnessData("ResetCurrentBrightnessFromOff", bundle);
        if (ret != 0) {
            Slog.w("FoldPolicy", "resumeCurrentBrightness failed, ret = " + ret);
        }
        this.mIsBrightnessOff = false;
        this.mHwFoldScreenMsgHandler.removeMessages(3);
        Slog.i("FoldPolicy", "resumeCurrentBrightness over");
        if (this.mDisplayModeReport != this.mDisplayMode) {
            this.mDisplayModeReport = this.mDisplayMode;
            ReportMonitorProcess.getInstance().updateSwitchEndTime(SystemClock.uptimeMillis());
        }
        startDawnAnimaiton();
        this.mLastWakeUpWakeReason = 0;
    }

    public void setDisplayStatus(IBinder token, int displayMode, int foldState, FoldPolicy.DisplayModeChangeCallback callback) {
        Rect displayRect;
        if (!isValidDisplayMode(displayMode)) {
            Slog.e("FoldPolicy", "setDisplayStatus not support mode " + displayMode);
            HwInwardFoldPolicy.super.setDisplayStatus(token, this.mDisplayMode, foldState, callback);
            return;
        }
        this.mFoldStateChanging = true;
        updateCutoutSettings(displayMode);
        if (isDisplayModeChange(displayMode)) {
            startFreezingScreen();
            if (!(displayMode == 5 || displayMode == 6)) {
                setCurrentBrightnessOff();
            }
            TouchscreenNotify.getInstance().setScreenStatusToTp(displayMode == 2);
        } else {
            Slog.i("FoldPolicy", "setDisplayStatus displayMode " + displayMode + " not change, don't freeze screen.");
        }
        HwFoldScreenMsgHandler hwFoldScreenMsgHandler = this.mHwFoldScreenMsgHandler;
        if (hwFoldScreenMsgHandler != null) {
            Message message = hwFoldScreenMsgHandler.obtainMessage(1);
            message.arg1 = displayMode;
            message.obj = callback;
            this.mHwFoldScreenMsgHandler.sendMessageDelayed(message, 3000);
            Slog.d("FoldPolicy", "send HW_FOLD_SCREEN_MSG_TIMEOUT_CMD");
        }
        int region = getDisplayRegionByMode(displayMode);
        if (displayMode == 2 || displayMode == 6) {
            displayRect = this.mIsRogScreen ? this.mMainDispPhyRect : this.mMainDispRect;
        } else {
            displayRect = this.mFullDispRect;
        }
        Slog.i("FoldPolicy", "setDisplayStatus region " + region + " foldState " + foldState + " displayRect " + displayRect + " subDisplRect " + this.mSubDispRect);
        SurfaceControl.setDisplayStatus(token, region, foldState, displayRect, this.mSubDispRect);
        if (displayMode == 5) {
            displayMode = 1;
        } else if (displayMode == 6) {
            displayMode = 2;
        }
        updateDisplayDeviceInfo(displayMode, callback);
        HwInwardFoldPolicy.super.setDisplayStatus(token, displayMode, foldState, callback);
        getLastWakeUpWakeReason();
    }

    private boolean isValidDisplayMode(int displayMode) {
        return displayMode == 1 || displayMode == 2 || displayMode == 5 || displayMode == 6;
    }

    public int setCurrentBrightnessOff() {
        Slog.i("FoldPolicy", "setCurrentBrightnessOff");
        Bundle bundle = new Bundle();
        bundle.putBoolean("UpdateBrightnessOffEnable", true);
        int ret = HwPowerManager.setHwBrightnessData("SetCurrentBrightnessOff", bundle);
        if (ret != 0) {
            Slog.w("FoldPolicy", "setCurrentBrightnessOff failed, ret = " + ret);
        } else {
            this.mIsBrightnessOff = true;
            this.mHwFoldScreenMsgHandler.sendEmptyMessageDelayed(3, 4000);
            Slog.i("FoldPolicy", "setCurrentBrightnessOff send HW_FOLD_SCREEN_MSG_RESUME_BRIGHTNESS_CMD");
        }
        return ret;
    }

    private int getDisplayRegionByMode(int displayMode) {
        if (displayMode == 2) {
            if (isScreenOn()) {
                return REGION_MAIN_SCREEN_ON;
            }
            return REGION_MAIN_SCREEN_OFF;
        } else if (displayMode == 5) {
            return REGION_OUTER_REFRESH;
        } else {
            if (displayMode == 6) {
                return REGION_INNER_REFRESH;
            }
            if (isScreenOn()) {
                return REGION_FULL;
            }
            return REGION_FULL_WAKEUP;
        }
    }

    /* access modifiers changed from: private */
    public final class HwFoldScreenMsgHandler extends Handler {
        public HwFoldScreenMsgHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                Slog.e("FoldPolicy", "process HW_FOLD_SCREEN_MSG_TIMEOUT_CMD");
                HwInwardFoldPolicy.this.sendScreenUnfreezMsg();
            } else if (i == 2) {
                Slog.i("FoldPolicy", "process HW_FOLD_SCREEN_MSG_UNFREEZ_CMD");
                HwInwardFoldPolicy.this.stopFreezingScreen();
            } else if (i == 3) {
                Slog.e("FoldPolicy", "process HW_FOLD_SCREEN_MSG_RESUME_BRIGHTNESS_CMD");
                HwInwardFoldPolicy.this.resumeCurrentBrightness();
            } else if (i == 4) {
                Slog.e("FoldPolicy", "process HW_FOLD_SCREEN_ON_UNBLOCKER_CMD");
                HwInwardFoldPolicy.this.sendScreenUnfreezMsg();
            }
        }
    }

    private void sendScreenOnUnblockerMsg() {
        this.mHwFoldScreenMsgHandler.sendEmptyMessageDelayed(4, 2000);
        Slog.i("FoldPolicy", "sendScreenOnUnblockerMsg delay 2000ms");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendScreenUnfreezMsg() {
        this.mHwFoldScreenMsgHandler.sendEmptyMessageDelayed(2, 50);
        Slog.i("FoldPolicy", "sendScreenUnfreezMsg delay 50ms");
    }

    private void startFreezingScreen() {
        this.mHwFoldScreenMsgHandler.removeMessages(2);
        if (this.mWms == null) {
            this.mWms = ServiceManager.getService("window");
        }
        this.mWms.startFreezingScreen(0, 0);
        this.mIsScreenFrozen = true;
        Slog.i("FoldPolicy", "startFreezingScreen");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopFreezingScreen() {
        this.mFoldStateChanging = false;
        if (!this.mIsScreenFrozen) {
            Slog.i("FoldPolicy", "stopFreezingScreen mIsScreenFrozen is false");
            resumeDisplayModeChange();
            resumeCurrentBrightness();
            return;
        }
        if (this.mWms == null) {
            this.mWms = ServiceManager.getService("window");
        }
        Slog.i("FoldPolicy", "stopFreezingScreen");
        this.mWms.stopFreezingScreen(this.mScreenUnfreezingCallback);
        this.mIsScreenFrozen = false;
        resumeDisplayModeChange();
    }

    private void resumeDisplayModeChange() {
        Slog.i("FoldPolicy", "resumeDisplayModeChange");
        if (this.mFsm == null) {
            this.mFsm = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
        }
        if (this.mFsm != null) {
            this.mFsm.resumeDispModeChange();
        } else {
            Slog.e("FoldPolicy", "get HwFoldScreenManagerInternal failed!");
        }
    }

    private void updateDisplayDeviceInfo(int displayMode, FoldPolicy.DisplayModeChangeCallback callback) {
        if (callback != null) {
            Slog.i("FoldPolicy", "updateDisplayDeviceInfo displayMode " + displayMode);
            callback.onDisplayModeChangeTimeout(0, displayMode, true);
            return;
        }
        Slog.e("FoldPolicy", "updateDisplayDeviceInfo callback is null");
    }

    private void updateCutoutSettings(int newDisplayMode) {
        if (!isDisplayModeChange(newDisplayMode)) {
            Slog.i("FoldPolicy", "updateCutoutSettings: displayMode not change");
        } else if (newDisplayMode != 2) {
            int notchStatus = Settings.Secure.getInt(this.mContext.getContentResolver(), "display_notch_status", 0);
            if (Settings.Secure.getInt(this.mContext.getContentResolver(), DISPLAY_NOTCH_STATUS_BAK, 0) != notchStatus) {
                Settings.Secure.putInt(this.mContext.getContentResolver(), DISPLAY_NOTCH_STATUS_BAK, notchStatus);
                Slog.i("FoldPolicy", "updateCutoutSettings: set display_notch_status_bak " + notchStatus);
            }
            if (notchStatus != 0) {
                Settings.Secure.putInt(this.mContext.getContentResolver(), "display_notch_status", 0);
                Slog.i("FoldPolicy", "updateCutoutSettings: set display_notch_status 0");
            }
        } else if (IS_PUNCH_DISABLE) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "display_notch_status", 1);
            Slog.i("FoldPolicy", "updateCutoutSettings: IS_PUNCH_DISABLE set display_notch_status 1");
        } else {
            int notchStatusBak = Settings.Secure.getInt(this.mContext.getContentResolver(), DISPLAY_NOTCH_STATUS_BAK, 0);
            Settings.Secure.putInt(this.mContext.getContentResolver(), "display_notch_status", notchStatusBak);
            Slog.i("FoldPolicy", "updateCutoutSettings: recovery display_notch_status " + notchStatusBak);
        }
    }

    private boolean isDisplayModeChange(int newDisplayMode) {
        return newDisplayMode != getCurrentDisplayMode();
    }

    private int getCurrentDisplayMode() {
        return this.mDisplayMode != 0 ? this.mDisplayMode : getPhysicalDisplayMode();
    }

    private int getPhysicalDisplayMode() {
        IBinder displayToken = SurfaceControl.getPhysicalDisplayToken(0);
        int phyDisplayMode = 0;
        if (displayToken != null) {
            SurfaceControl.PhysicalDisplayInfo[] configs = SurfaceControl.getDisplayConfigs(displayToken);
            if (configs == null) {
                Slog.e("FoldPolicy", "No valid configs found for display device");
                return 0;
            }
            int activeConfig = SurfaceControl.getActiveConfig(displayToken);
            if (activeConfig < 0) {
                Slog.e("FoldPolicy", "No active config found for display device");
                return 0;
            } else if (activeConfig >= configs.length) {
                Slog.e("FoldPolicy", "activeConfig " + activeConfig + " greater or equal to configs.length " + configs.length);
                return 0;
            } else {
                SurfaceControl.PhysicalDisplayInfo displayInfo = configs[activeConfig];
                Slog.i("FoldPolicy", "Current PhysicalDisplayInfo " + displayInfo);
                if (displayInfo.height == this.mMainDispPhyRect.height() && displayInfo.width == this.mMainDispPhyRect.width()) {
                    phyDisplayMode = 2;
                } else {
                    phyDisplayMode = 1;
                }
            }
        } else {
            Slog.e("FoldPolicy", "displayToken is null");
        }
        Slog.i("FoldPolicy", "current physicalDisplayMode " + phyDisplayMode);
        return phyDisplayMode;
    }

    private void setRogSize(int newDisplayMode) {
        int rogHeight;
        int rogWidth;
        Rect dispRect = getDispRect(newDisplayMode);
        Slog.d("FoldPolicy", "setRogSize " + dispRect);
        if (newDisplayMode == 1) {
            rogWidth = dispRect.height();
            rogHeight = dispRect.width();
        } else {
            rogWidth = dispRect.width();
            rogHeight = dispRect.height();
        }
        Slog.d("FoldPolicy", "setRogSize " + rogWidth + "x" + rogHeight);
        SurfaceControl.setRogSize(rogWidth, rogHeight);
        SystemProperties.set("persist.sys.rog.width", String.valueOf(rogWidth));
        SystemProperties.set("persist.sys.rog.height", String.valueOf(rogHeight));
    }

    private void startDawnAnimaiton() {
        Slog.i("FoldPolicy", "startDawnAnimaiton lastWakeUpWakeReason " + this.mLastWakeUpWakeReason);
        if (this.mLastWakeUpWakeReason == 103) {
            if (this.mFsm == null) {
                this.mFsm = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
                if (this.mFsm == null) {
                    Slog.e("FoldPolicy", "mFsm is null");
                    return;
                }
            }
            this.mFsm.startDawnAnimaiton();
        }
    }

    private boolean registerScreenOnUnBlockerCallback() {
        Slog.i("FoldPolicy", "registerScreenOnUnBlockerCallback lastWakeUpWakeReason " + this.mLastWakeUpWakeReason);
        if (this.mLastWakeUpWakeReason != 103) {
            return false;
        }
        if (this.mFsm == null) {
            this.mFsm = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
            if (this.mFsm == null) {
                Slog.e("FoldPolicy", "mFsm is null");
                return false;
            }
        }
        return this.mFsm.registerScreenOnUnBlockerCallback(this.mScreenOnUnblockerCallback);
    }
}
