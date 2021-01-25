package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.util.FloatProperty;
import android.util.Flog;
import android.util.IntProperty;
import android.util.Log;
import android.util.Slog;
import android.view.Choreographer;
import android.view.Display;
import com.android.server.FingerprintDataInterface;
import com.android.server.LocalServices;
import com.android.server.NsdService;
import com.android.server.policy.WindowManagerPolicy;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import java.io.PrintWriter;

public final class DisplayPowerState {
    public static final FloatProperty<DisplayPowerState> COLOR_FADE_LEVEL = new FloatProperty<DisplayPowerState>("electronBeamLevel") {
        /* class com.android.server.display.DisplayPowerState.AnonymousClass1 */

        public void setValue(DisplayPowerState object, float value) {
            object.setColorFadeLevel(value);
        }

        public Float get(DisplayPowerState object) {
            return Float.valueOf(object.getColorFadeLevel());
        }
    };
    private static String COUNTER_COLOR_FADE = "ColorFadeLevel";
    private static boolean DEBUG = false;
    private static boolean DEBUG_HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int FACE_DETECTING_STATE = 1;
    private static final int FACE_NOT_DETECTING_STATE = 0;
    private static final long FACE_WAITING_DELAY_TIME = 50;
    private static final int FACE_WAITING_TIMES = 10;
    private static final long QUICK_BACKLIGHT_ONOFF_SLEEP_TIME = 2;
    private static final long QUICK_BACKLIGHT_ONOFF_TIME = 5;
    public static final IntProperty<DisplayPowerState> SCREEN_BRIGHTNESS = new IntProperty<DisplayPowerState>("screenBrightness") {
        /* class com.android.server.display.DisplayPowerState.AnonymousClass2 */

        public void setValue(DisplayPowerState object, int value) {
            object.setScreenBrightness(value);
        }

        public Integer get(DisplayPowerState object) {
            return Integer.valueOf(object.getScreenBrightness());
        }
    };
    private static final String TAG = "DisplayPowerState";
    private FingerprintDataInterface fpDataCollector;
    private long mBacklightOffTime = 0;
    private long mBacklightOnTime = 0;
    private final DisplayBlanker mBlanker;
    private final Choreographer mChoreographer = Choreographer.getInstance();
    private Runnable mCleanListener;
    private final ColorFade mColorFade;
    private boolean mColorFadeDrawPending;
    private final Runnable mColorFadeDrawRunnable = new Runnable() {
        /* class com.android.server.display.DisplayPowerState.AnonymousClass4 */

        @Override // java.lang.Runnable
        public void run() {
            DisplayPowerState.this.mColorFadeDrawPending = false;
            if (DisplayPowerState.this.mColorFadePrepared) {
                DisplayPowerState.this.mColorFade.draw(DisplayPowerState.this.mColorFadeLevel);
                Trace.traceCounter(131072, DisplayPowerState.COUNTER_COLOR_FADE, Math.round(DisplayPowerState.this.mColorFadeLevel * 100.0f));
            }
            DisplayPowerState.this.mColorFadeReady = true;
            DisplayPowerState.this.invokeCleanListenerIfNeeded();
        }
    };
    private float mColorFadeLevel;
    private boolean mColorFadePrepared;
    private boolean mColorFadeReady;
    private int mFaceWaitingTimes = 0;
    private final Handler mHandler = new Handler(true);
    private final PhotonicModulator mPhotonicModulator;
    private boolean mQuickBacklightOnOffDelayEnable = false;
    private int mScreenBrightness;
    public int mScreenChangedReason = 0;
    private boolean mScreenReady;
    private int mScreenState;
    private int mScreenStateFace = 0;
    private boolean mScreenUpdatePending;
    private final Runnable mScreenUpdateRunnable = new Runnable() {
        /* class com.android.server.display.DisplayPowerState.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            int brightness = 0;
            DisplayPowerState.this.mScreenUpdatePending = false;
            if (DisplayPowerState.this.mScreenState != 1 && DisplayPowerState.this.mColorFadeLevel > 0.0f) {
                brightness = DisplayPowerState.this.mScreenBrightness;
            }
            if (DisplayPowerState.this.isSetBrightness(brightness)) {
                if (DisplayPowerState.this.mPhotonicModulator.setState(DisplayPowerState.this.mScreenState, brightness)) {
                    if (DisplayPowerState.DEBUG) {
                        Slog.d(DisplayPowerState.TAG, "Screen ready");
                    }
                    DisplayPowerState.this.mScreenReady = true;
                    DisplayPowerState.this.invokeCleanListenerIfNeeded();
                } else if (DisplayPowerState.DEBUG) {
                    Slog.d(DisplayPowerState.TAG, "Screen not ready");
                }
            }
        }
    };
    private final WindowManagerPolicy mWindowManagerPolicy;

    public DisplayPowerState(DisplayBlanker blanker, ColorFade colorFade) {
        this.mBlanker = blanker;
        this.mColorFade = colorFade;
        this.mPhotonicModulator = new PhotonicModulator();
        this.mPhotonicModulator.start();
        this.mScreenState = 2;
        Slog.i(TAG, "init mScreenBrightness=" + this.mScreenBrightness);
        scheduleScreenUpdate();
        this.mColorFadePrepared = false;
        this.mColorFadeLevel = 1.0f;
        this.mColorFadeReady = true;
        this.fpDataCollector = FingerprintDataInterface.getInstance();
        this.mQuickBacklightOnOffDelayEnable = getQuickBacklightOnOffDelayEnable();
        this.mWindowManagerPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
    }

    private boolean getQuickBacklightOnOffDelayEnable() {
        boolean hbmAheadEnable = SystemProperties.getBoolean("ro.config.fp_hbm_ahead", false);
        Slog.i(TAG, "hbmAheadEnable = " + hbmAheadEnable);
        return hbmAheadEnable;
    }

    public void setScreenState(int state) {
        if (this.mScreenState != state) {
            Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power setScreenState: state=" + state);
            this.mScreenState = state;
            this.mScreenReady = false;
            scheduleScreenUpdate();
        }
    }

    public int getScreenState() {
        return this.mScreenState;
    }

    public void setScreenBrightness(int brightness) {
        if (this.mScreenBrightness != brightness) {
            if (DEBUG) {
                Slog.d(TAG, "setScreenBrightness: brightness=" + brightness);
            }
            this.mScreenBrightness = brightness;
            if (this.mScreenState != 1) {
                this.mScreenReady = false;
                scheduleScreenUpdate();
            }
        }
    }

    public int getScreenBrightness() {
        return this.mScreenBrightness;
    }

    public boolean prepareColorFade(Context context, int mode) {
        ColorFade colorFade = this.mColorFade;
        if (colorFade == null || !colorFade.prepare(context, mode)) {
            this.mColorFadePrepared = false;
            this.mColorFadeReady = true;
            return false;
        }
        this.mColorFadePrepared = true;
        this.mColorFadeReady = false;
        scheduleColorFadeDraw();
        return true;
    }

    public void dismissColorFade() {
        Trace.traceCounter(131072, COUNTER_COLOR_FADE, 100);
        ColorFade colorFade = this.mColorFade;
        if (colorFade != null) {
            colorFade.dismiss();
        }
        this.mColorFadePrepared = false;
        this.mColorFadeReady = true;
    }

    public void dismissColorFadeResources() {
        ColorFade colorFade = this.mColorFade;
        if (colorFade != null) {
            colorFade.dismissResources();
        }
    }

    public void clearColorFadeSurface() {
        ColorFade colorFade = this.mColorFade;
        if (colorFade != null) {
            colorFade.clearColorFadeSurface();
        }
    }

    public void prepareDawnAnimation(Context context, int mode) {
        ColorFade colorFade = this.mColorFade;
        if (colorFade == null) {
            return;
        }
        if (colorFade.prepare(context, mode)) {
            this.mColorFade.draw(0.0f);
            this.mColorFade.dismissResources();
            return;
        }
        Slog.e(TAG, "prepareDawnAnimation fail!");
    }

    public void setColorFadeLevel(float level) {
        if (this.mColorFadeLevel != level) {
            Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power setColorFadeLevel: level=" + level);
            this.mColorFadeLevel = level;
            if (this.mScreenState != 1) {
                this.mScreenReady = false;
                scheduleScreenUpdate();
            }
            if (this.mColorFadePrepared) {
                this.mColorFadeReady = false;
                scheduleColorFadeDraw();
            }
        }
    }

    public float getColorFadeLevel() {
        return this.mColorFadeLevel;
    }

    public boolean waitUntilClean(Runnable listener) {
        if (!this.mScreenReady || !this.mColorFadeReady) {
            this.mCleanListener = listener;
            return false;
        }
        this.mCleanListener = null;
        return true;
    }

    public void dump(PrintWriter pw) {
        pw.println();
        pw.println("Display Power State:");
        pw.println("  mScreenState=" + Display.stateToString(this.mScreenState));
        pw.println("  mScreenBrightness=" + this.mScreenBrightness);
        pw.println("  mScreenReady=" + this.mScreenReady);
        pw.println("  mScreenUpdatePending=" + this.mScreenUpdatePending);
        pw.println("  mColorFadePrepared=" + this.mColorFadePrepared);
        pw.println("  mColorFadeLevel=" + this.mColorFadeLevel);
        pw.println("  mColorFadeReady=" + this.mColorFadeReady);
        pw.println("  mColorFadeDrawPending=" + this.mColorFadeDrawPending);
        this.mPhotonicModulator.dump(pw);
        ColorFade colorFade = this.mColorFade;
        if (colorFade != null) {
            colorFade.dump(pw);
        }
    }

    private void scheduleScreenUpdate() {
        if (!this.mScreenUpdatePending) {
            this.mScreenUpdatePending = true;
            postScreenUpdateThreadSafe();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postScreenUpdateThreadSafe() {
        this.mHandler.removeCallbacks(this.mScreenUpdateRunnable);
        this.mHandler.post(this.mScreenUpdateRunnable);
    }

    private void scheduleColorFadeDraw() {
        if (!this.mColorFadeDrawPending) {
            this.mColorFadeDrawPending = true;
            this.mChoreographer.postCallback(3, this.mColorFadeDrawRunnable, null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void invokeCleanListenerIfNeeded() {
        Runnable listener = this.mCleanListener;
        if (listener != null && this.mScreenReady && this.mColorFadeReady) {
            this.mCleanListener = null;
            listener.run();
        }
    }

    /* access modifiers changed from: private */
    public final class PhotonicModulator extends Thread {
        private static final int INITIAL_BACKLIGHT = -1;
        private static final int INITIAL_SCREEN_STATE = 1;
        private int mActualBacklight = -1;
        private int mActualState = 1;
        private boolean mBacklightChangeInProgress;
        private HwFoldScreenManagerInternal mFoldScreenManagerService = ((HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class));
        private final Object mLock = new Object();
        private int mPendingBacklight = -1;
        private int mPendingState = 1;
        private boolean mStateChangeInProgress;

        public PhotonicModulator() {
            super("PhotonicModulator");
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x0056  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0062  */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x006e  */
        public boolean setState(int state, int backlight) {
            boolean z;
            boolean changeInProgress;
            boolean z2;
            boolean z3;
            addDelayForQuickBacklightOnOff(this.mPendingBacklight, backlight);
            synchronized (this.mLock) {
                z = true;
                boolean stateChanged = state != this.mPendingState;
                boolean backlightChanged = backlight != this.mPendingBacklight;
                if (stateChanged || backlightChanged) {
                    if (DisplayPowerState.DEBUG) {
                        Slog.d(DisplayPowerState.TAG, "Requesting new screen state: state=" + Display.stateToString(state) + ", backlight=" + backlight);
                    }
                    this.mPendingState = state;
                    this.mPendingBacklight = backlight;
                    if (!this.mStateChangeInProgress) {
                        if (!this.mBacklightChangeInProgress) {
                            changeInProgress = false;
                            if (!stateChanged) {
                                if (!this.mStateChangeInProgress) {
                                    z2 = false;
                                    this.mStateChangeInProgress = z2;
                                    if (!backlightChanged) {
                                        if (!this.mBacklightChangeInProgress) {
                                            z3 = false;
                                            this.mBacklightChangeInProgress = z3;
                                            if (!changeInProgress) {
                                                this.mLock.notifyAll();
                                            }
                                        }
                                    }
                                    z3 = true;
                                    this.mBacklightChangeInProgress = z3;
                                    if (!changeInProgress) {
                                    }
                                }
                            }
                            z2 = true;
                            this.mStateChangeInProgress = z2;
                            if (!backlightChanged) {
                            }
                            z3 = true;
                            this.mBacklightChangeInProgress = z3;
                            if (!changeInProgress) {
                            }
                        }
                    }
                    changeInProgress = true;
                    if (!stateChanged) {
                    }
                    z2 = true;
                    this.mStateChangeInProgress = z2;
                    if (!backlightChanged) {
                    }
                    z3 = true;
                    this.mBacklightChangeInProgress = z3;
                    if (!changeInProgress) {
                    }
                }
                if (this.mStateChangeInProgress) {
                    z = false;
                }
            }
            return z;
        }

        private void addDelayForQuickBacklightOnOff(int pendingBacklight, int backlight) {
            if (DisplayPowerState.this.mQuickBacklightOnOffDelayEnable) {
                if (pendingBacklight > 0 && backlight == 0) {
                    DisplayPowerState.this.mBacklightOffTime = SystemClock.elapsedRealtime();
                }
                if (pendingBacklight == 0 && backlight > 0) {
                    DisplayPowerState.this.mBacklightOnTime = SystemClock.elapsedRealtime();
                    long timeDelta = DisplayPowerState.this.mBacklightOnTime - DisplayPowerState.this.mBacklightOffTime;
                    if (timeDelta < DisplayPowerState.QUICK_BACKLIGHT_ONOFF_TIME) {
                        if (DisplayPowerState.DEBUG_HWFLOW) {
                            Slog.i(DisplayPowerState.TAG, "QuickBacklightOnOff backlight delay 2ms,backlight=" + backlight + ",timeDelta=" + timeDelta + ",timeDeltaTH=" + DisplayPowerState.QUICK_BACKLIGHT_ONOFF_TIME + ",mBacklightOnTime=" + DisplayPowerState.this.mBacklightOnTime + ",mBacklightOffTime=" + DisplayPowerState.this.mBacklightOffTime);
                        }
                        try {
                            Thread.sleep(2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        public void dump(PrintWriter pw) {
            synchronized (this.mLock) {
                pw.println();
                pw.println("Photonic Modulator State:");
                pw.println("  mPendingState=" + Display.stateToString(this.mPendingState));
                pw.println("  mPendingBacklight=" + this.mPendingBacklight);
                pw.println("  mActualState=" + Display.stateToString(this.mActualState));
                pw.println("  mActualBacklight=" + this.mActualBacklight);
                pw.println("  mStateChangeInProgress=" + this.mStateChangeInProgress);
                pw.println("  mBacklightChangeInProgress=" + this.mBacklightChangeInProgress);
            }
        }

        private void setScreenChangeReason(int state, boolean screenOnorOff, int backlight) {
            if (screenOnorOff) {
                if (DisplayPowerState.this.mWindowManagerPolicy == null) {
                    Slog.e(DisplayPowerState.TAG, "mWindowManagerPolicy is null");
                } else if (state == 2 && backlight > 0) {
                    if (DisplayPowerState.this.mScreenChangedReason == 100) {
                        DisplayPowerState.this.mScreenChangedReason = 65636;
                    }
                    Slog.i(DisplayPowerState.TAG, "set screen turn on reason mScreenChangedReason: " + DisplayPowerState.this.mScreenChangedReason);
                    DisplayPowerState.this.mWindowManagerPolicy.setScreenChangedReason(DisplayPowerState.this.mScreenChangedReason);
                    HwFoldScreenManagerInternal hwFoldScreenManagerInternal = this.mFoldScreenManagerService;
                    if (hwFoldScreenManagerInternal != null) {
                        hwFoldScreenManagerInternal.notifyScreenOnFinished();
                    }
                } else if (state == 1 && backlight == 0) {
                    Slog.i(DisplayPowerState.TAG, "set screen turn off reason mScreenChangedReason: " + DisplayPowerState.this.mScreenChangedReason);
                    DisplayPowerState.this.mWindowManagerPolicy.setScreenChangedReason(DisplayPowerState.this.mScreenChangedReason);
                }
            }
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (true) {
                synchronized (this.mLock) {
                    int state = this.mPendingState;
                    boolean screenOnorOff = true;
                    boolean stateChanged = state != this.mActualState;
                    int backlight = this.mPendingBacklight;
                    boolean backlightChanged = backlight != this.mActualBacklight;
                    if (!stateChanged) {
                        DisplayPowerState.this.postScreenUpdateThreadSafe();
                        this.mStateChangeInProgress = false;
                    }
                    if (!backlightChanged) {
                        this.mBacklightChangeInProgress = false;
                    }
                    if (stateChanged || backlightChanged) {
                        this.mActualState = state;
                        if (!(backlight == 0 || this.mActualBacklight == 0)) {
                            screenOnorOff = false;
                        }
                        this.mActualBacklight = backlight;
                        if (screenOnorOff) {
                            Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Updating screen state: state=" + Display.stateToString(state) + ", backlight=" + backlight);
                            if (2 == state && backlight > 0 && DisplayPowerState.this.fpDataCollector != null) {
                                DisplayPowerState.this.fpDataCollector.reportScreenStateOn("ON");
                            }
                        }
                        setScreenChangeReason(state, screenOnorOff, backlight);
                        DisplayPowerState.this.mBlanker.requestDisplayState(state, backlight);
                    } else {
                        try {
                            this.mLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSetBrightness(int brightness) {
        if (brightness > 0 && this.mScreenStateFace == 1) {
            Slog.d(TAG, "setBiometricDetectState mScreenStateFace=" + this.mScreenStateFace + ",mFaceWaitingTimes=" + this.mFaceWaitingTimes + ",brightness=" + brightness);
            if (this.mFaceWaitingTimes < 10) {
                this.mHandler.postDelayed(this.mScreenUpdateRunnable, FACE_WAITING_DELAY_TIME);
                this.mFaceWaitingTimes++;
                return false;
            }
            this.mScreenStateFace = 0;
        }
        this.mFaceWaitingTimes = 0;
        return true;
    }

    public void setBiometricDetectState(int state) {
        if (UserHandle.myUserId() == 0 && state != this.mScreenStateFace) {
            Slog.d(TAG, "setBiometricDetectState mScreenStateFace=" + state);
            if (state == 1) {
                this.mScreenStateFace = 1;
                return;
            }
            this.mScreenStateFace = 0;
            this.mHandler.removeCallbacks(this.mScreenUpdateRunnable);
            this.mHandler.postAtFrontOfQueue(this.mScreenUpdateRunnable);
        }
    }
}
