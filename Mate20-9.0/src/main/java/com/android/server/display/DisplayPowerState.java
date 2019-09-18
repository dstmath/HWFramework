package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.FloatProperty;
import android.util.Flog;
import android.util.IntProperty;
import android.util.Log;
import android.util.Slog;
import android.view.Choreographer;
import android.view.Display;
import com.android.server.FingerprintDataInterface;
import com.android.server.NsdService;
import java.io.PrintWriter;

public final class DisplayPowerState {
    private static final String AOD_BACK_LIGHT_KEY = "sys.current_backlight";
    public static final FloatProperty<DisplayPowerState> COLOR_FADE_LEVEL = new FloatProperty<DisplayPowerState>("electronBeamLevel") {
        public void setValue(DisplayPowerState object, float value) {
            object.setColorFadeLevel(value);
        }

        public Float get(DisplayPowerState object) {
            return Float.valueOf(object.getColorFadeLevel());
        }
    };
    /* access modifiers changed from: private */
    public static String COUNTER_COLOR_FADE = "ColorFadeLevel";
    private static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    /* access modifiers changed from: private */
    public static boolean DEBUG_Controller = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static boolean DEBUG_FPLOG = false;
    private static final int DEFAULT_MAX_BRIGHTNESS = 255;
    private static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    public static final IntProperty<DisplayPowerState> SCREEN_BRIGHTNESS = new IntProperty<DisplayPowerState>("screenBrightness") {
        public void setValue(DisplayPowerState object, int value) {
            object.setScreenBrightness(value);
        }

        public Integer get(DisplayPowerState object) {
            return Integer.valueOf(object.getScreenBrightness());
        }
    };
    private static final String TAG = "DisplayPowerState";
    /* access modifiers changed from: private */
    public static final boolean mSupportAod = "1".equals(SystemProperties.get("ro.config.support_aod", null));
    /* access modifiers changed from: private */
    public FingerprintDataInterface fpDataCollector;
    /* access modifiers changed from: private */
    public final DisplayBlanker mBlanker;
    private final Choreographer mChoreographer = Choreographer.getInstance();
    private Runnable mCleanListener;
    /* access modifiers changed from: private */
    public final ColorFade mColorFade;
    /* access modifiers changed from: private */
    public boolean mColorFadeDrawPending;
    private final Runnable mColorFadeDrawRunnable = new Runnable() {
        public void run() {
            boolean unused = DisplayPowerState.this.mColorFadeDrawPending = false;
            if (DisplayPowerState.this.mColorFadePrepared) {
                DisplayPowerState.this.mColorFade.draw(DisplayPowerState.this.mColorFadeLevel);
                Trace.traceCounter(131072, DisplayPowerState.COUNTER_COLOR_FADE, Math.round(DisplayPowerState.this.mColorFadeLevel * 100.0f));
            }
            boolean unused2 = DisplayPowerState.this.mColorFadeReady = true;
            DisplayPowerState.this.invokeCleanListenerIfNeeded();
        }
    };
    /* access modifiers changed from: private */
    public float mColorFadeLevel;
    /* access modifiers changed from: private */
    public boolean mColorFadePrepared;
    /* access modifiers changed from: private */
    public boolean mColorFadeReady;
    private final Handler mHandler = new Handler(true);
    /* access modifiers changed from: private */
    public final PhotonicModulator mPhotonicModulator;
    /* access modifiers changed from: private */
    public int mScreenBrightness = -1;
    /* access modifiers changed from: private */
    public boolean mScreenReady;
    /* access modifiers changed from: private */
    public int mScreenState;
    /* access modifiers changed from: private */
    public boolean mScreenUpdatePending;
    private final Runnable mScreenUpdateRunnable = new Runnable() {
        public void run() {
            int brightness = 0;
            boolean unused = DisplayPowerState.this.mScreenUpdatePending = false;
            if (DisplayPowerState.this.mScreenState != 1 && DisplayPowerState.this.mColorFadeLevel > 0.0f) {
                brightness = DisplayPowerState.this.mScreenBrightness;
            }
            if (DisplayPowerState.this.mPhotonicModulator.setState(DisplayPowerState.this.mScreenState, brightness)) {
                if (DisplayPowerState.DEBUG_Controller) {
                    Slog.d(DisplayPowerState.TAG, "Screen ready");
                }
                boolean unused2 = DisplayPowerState.this.mScreenReady = true;
                DisplayPowerState.this.invokeCleanListenerIfNeeded();
            } else if (DisplayPowerState.DEBUG_Controller) {
                Slog.d(DisplayPowerState.TAG, "Screen not ready");
            }
        }
    };

    private final class PhotonicModulator extends Thread {
        private static final int INITIAL_BACKLIGHT = -1;
        private static final int INITIAL_SCREEN_STATE = 1;
        private int mActualBacklight = -1;
        private int mActualState = 1;
        private boolean mBacklightChangeInProgress;
        private final Object mLock = new Object();
        private int mPendingBacklight = -1;
        private int mPendingState = 1;
        private boolean mStateChangeInProgress;

        public PhotonicModulator() {
            super("PhotonicModulator");
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x0051  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x005d  */
        /* JADX WARNING: Removed duplicated region for block: B:38:0x0068  */
        public boolean setState(int state, int backlight) {
            boolean z;
            boolean changeInProgress;
            boolean z2;
            synchronized (this.mLock) {
                boolean z3 = false;
                boolean stateChanged = state != this.mPendingState;
                boolean backlightChanged = backlight != this.mPendingBacklight;
                if (stateChanged || backlightChanged) {
                    if (DisplayPowerState.DEBUG_Controller) {
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
                z = !this.mStateChangeInProgress;
            }
            return z;
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
                        if (backlight != 0) {
                            if (this.mActualBacklight != 0) {
                                screenOnorOff = false;
                            }
                        }
                        this.mActualBacklight = backlight;
                        if (screenOnorOff) {
                            Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Updating screen state: state=" + Display.stateToString(state) + ", backlight=" + backlight);
                            if (2 == state && backlight > 0) {
                                if (DisplayPowerState.this.fpDataCollector != null) {
                                    DisplayPowerState.this.fpDataCollector.reportScreenStateOn("ON");
                                }
                                if (DisplayPowerState.mSupportAod) {
                                    SystemProperties.set(DisplayPowerState.AOD_BACK_LIGHT_KEY, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + backlight);
                                }
                            }
                        }
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

    static {
        boolean z = true;
        if (!DEBUG) {
            z = false;
        }
        DEBUG_FPLOG = z;
    }

    public DisplayPowerState(DisplayBlanker blanker, ColorFade colorFade) {
        this.mBlanker = blanker;
        this.mColorFade = colorFade;
        this.mPhotonicModulator = new PhotonicModulator();
        this.mPhotonicModulator.start();
        initialize(null);
    }

    public DisplayPowerState(Context context, DisplayBlanker blanker, ColorFade colorFade) {
        this.mBlanker = blanker;
        this.mColorFade = colorFade;
        this.mPhotonicModulator = new PhotonicModulator();
        this.mPhotonicModulator.start();
        initialize(context);
    }

    private void initialize(Context context) {
        this.mScreenState = 2;
        if (DEBUG) {
            Slog.i(TAG, ",init mScreenBrightness=" + this.mScreenBrightness);
        }
        scheduleScreenUpdate();
        this.mColorFadePrepared = false;
        this.mColorFadeLevel = 1.0f;
        this.mColorFadeReady = true;
        this.fpDataCollector = FingerprintDataInterface.getInstance();
    }

    public void setScreenState(int state) {
        if (this.mScreenState != state) {
            Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power setScreenState: state=" + state);
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
            if (DEBUG && DEBUG_Controller) {
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
        if (this.mColorFade == null || !this.mColorFade.prepare(context, mode)) {
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
        if (this.mColorFade != null) {
            this.mColorFade.dismiss();
        }
        this.mColorFadePrepared = false;
        this.mColorFadeReady = true;
    }

    public void dismissColorFadeResources() {
        if (this.mColorFade != null) {
            this.mColorFade.dismissResources();
        }
    }

    public void setColorFadeLevel(float level) {
        if (this.mColorFadeLevel != level) {
            Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power setColorFadeLevel: level=" + level);
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
        if (this.mColorFade != null) {
            this.mColorFade.dump(pw);
        }
    }

    private void scheduleScreenUpdate() {
        if (!this.mScreenUpdatePending) {
            this.mScreenUpdatePending = true;
            postScreenUpdateThreadSafe();
        }
    }

    /* access modifiers changed from: private */
    public void postScreenUpdateThreadSafe() {
        this.mHandler.removeCallbacks(this.mScreenUpdateRunnable);
        this.mHandler.post(this.mScreenUpdateRunnable);
    }

    private void scheduleColorFadeDraw() {
        if (!this.mColorFadeDrawPending) {
            this.mColorFadeDrawPending = true;
            this.mChoreographer.postCallback(2, this.mColorFadeDrawRunnable, null);
        }
    }

    /* access modifiers changed from: private */
    public void invokeCleanListenerIfNeeded() {
        Runnable listener = this.mCleanListener;
        if (listener != null && this.mScreenReady && this.mColorFadeReady) {
            this.mCleanListener = null;
            listener.run();
        }
    }
}
