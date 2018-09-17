package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.FloatProperty;
import android.util.Flog;
import android.util.IntProperty;
import android.util.Log;
import android.util.Slog;
import android.view.Choreographer;
import android.view.Display;
import com.android.server.FingerprintUnlockDataCollector;
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
    private static boolean DEBUG = false;
    private static boolean DEBUG_Controller = false;
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
    private static final boolean mSupportAod = "1".equals(SystemProperties.get("ro.config.support_aod", null));
    private FingerprintUnlockDataCollector fpDataCollector;
    private final DisplayBlanker mBlanker;
    private final Choreographer mChoreographer = Choreographer.getInstance();
    private Runnable mCleanListener;
    private final ColorFade mColorFade;
    private boolean mColorFadeDrawPending;
    private final Runnable mColorFadeDrawRunnable = new Runnable() {
        public void run() {
            DisplayPowerState.this.mColorFadeDrawPending = false;
            if (DisplayPowerState.this.mColorFadePrepared) {
                DisplayPowerState.this.mColorFade.draw(DisplayPowerState.this.mColorFadeLevel);
            }
            DisplayPowerState.this.mColorFadeReady = true;
            DisplayPowerState.this.invokeCleanListenerIfNeeded();
        }
    };
    private float mColorFadeLevel;
    private boolean mColorFadePrepared;
    private boolean mColorFadeReady;
    private final Handler mHandler = new Handler(true);
    private final PhotonicModulator mPhotonicModulator;
    private int mScreenBrightness = -1;
    private boolean mScreenReady;
    private int mScreenState;
    private boolean mScreenUpdatePending;
    private final Runnable mScreenUpdateRunnable = new Runnable() {
        public void run() {
            DisplayPowerState.this.mScreenUpdatePending = false;
            int brightness = (DisplayPowerState.this.mScreenState == 1 || DisplayPowerState.this.mColorFadeLevel <= 0.0f) ? 0 : DisplayPowerState.this.mScreenBrightness;
            if (DisplayPowerState.this.mPhotonicModulator.setState(DisplayPowerState.this.mScreenState, brightness)) {
                if (DisplayPowerState.DEBUG_Controller) {
                    Slog.d(DisplayPowerState.TAG, "Screen ready");
                }
                DisplayPowerState.this.mScreenReady = true;
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

        public boolean setState(int state, int backlight) {
            boolean z = true;
            synchronized (this.mLock) {
                boolean stateChanged = state != this.mPendingState;
                boolean backlightChanged = backlight != this.mPendingBacklight;
                if (stateChanged || backlightChanged) {
                    boolean z2;
                    if (DisplayPowerState.DEBUG_Controller) {
                        Slog.d(DisplayPowerState.TAG, "Requesting new screen state: state=" + Display.stateToString(state) + ", backlight=" + backlight);
                    }
                    this.mPendingState = state;
                    this.mPendingBacklight = backlight;
                    boolean changeInProgress = !this.mStateChangeInProgress ? this.mBacklightChangeInProgress : true;
                    if (stateChanged) {
                        z2 = true;
                    } else {
                        z2 = this.mStateChangeInProgress;
                    }
                    this.mStateChangeInProgress = z2;
                    if (!backlightChanged) {
                        z = this.mBacklightChangeInProgress;
                    }
                    this.mBacklightChangeInProgress = z;
                    if (!changeInProgress) {
                        this.mLock.notifyAll();
                    }
                }
                z = this.mStateChangeInProgress ^ 1;
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
                    if (stateChanged || (backlightChanged ^ 1) == 0) {
                        this.mActualState = state;
                        boolean screenOnorOff = backlight == 0 || this.mActualBacklight == 0;
                        this.mActualBacklight = backlight;
                        if (screenOnorOff) {
                            Flog.i(NativeResponseCode.SERVICE_FOUND, "DisplayPowerState Updating screen state: state=" + Display.stateToString(state) + ", backlight=" + backlight);
                            if (2 == state && backlight > 0 && DisplayPowerState.mSupportAod) {
                                SystemProperties.set(DisplayPowerState.AOD_BACK_LIGHT_KEY, "" + backlight);
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
        boolean z;
        boolean z2 = true;
        if (Log.HWINFO) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, 4);
        } else {
            z = false;
        }
        DEBUG = z;
        if (Log.HWLog) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, 3);
        } else {
            z = false;
        }
        DEBUG_Controller = z;
        if (!DEBUG) {
            z2 = false;
        }
        DEBUG_FPLOG = z2;
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
        this.fpDataCollector = FingerprintUnlockDataCollector.getInstance();
    }

    public void setScreenState(int state) {
        if (this.mScreenState != state) {
            Flog.i(NativeResponseCode.SERVICE_FOUND, "DisplayPowerStatesetScreenState: state=" + state);
            this.mScreenState = state;
            this.mScreenReady = false;
            if (DEBUG_FPLOG) {
                String stateStr = Display.stateToString(state);
                if (this.fpDataCollector != null) {
                    this.fpDataCollector.reportScreenStateOn(stateStr);
                }
            }
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
        if (this.mColorFade.prepare(context, mode)) {
            this.mColorFadePrepared = true;
            this.mColorFadeReady = false;
            scheduleColorFadeDraw();
            return true;
        }
        this.mColorFadePrepared = false;
        this.mColorFadeReady = true;
        return false;
    }

    public void dismissColorFade() {
        this.mColorFade.dismiss();
        this.mColorFadePrepared = false;
        this.mColorFadeReady = true;
    }

    public void dismissColorFadeResources() {
        this.mColorFade.dismissResources();
    }

    public void setColorFadeLevel(float level) {
        if (this.mColorFadeLevel != level) {
            Flog.i(NativeResponseCode.SERVICE_FOUND, "DisplayPowerStatesetColorFadeLevel: level=" + level);
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
        if (this.mScreenReady && (this.mColorFadeReady ^ 1) == 0) {
            this.mCleanListener = null;
            return true;
        }
        this.mCleanListener = listener;
        return false;
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
        this.mColorFade.dump(pw);
    }

    private void scheduleScreenUpdate() {
        if (!this.mScreenUpdatePending) {
            this.mScreenUpdatePending = true;
            postScreenUpdateThreadSafe();
        }
    }

    private void postScreenUpdateThreadSafe() {
        this.mHandler.removeCallbacks(this.mScreenUpdateRunnable);
        this.mHandler.post(this.mScreenUpdateRunnable);
    }

    private void scheduleColorFadeDraw() {
        if (!this.mColorFadeDrawPending) {
            this.mColorFadeDrawPending = true;
            this.mChoreographer.postCallback(2, this.mColorFadeDrawRunnable, null);
        }
    }

    private void invokeCleanListenerIfNeeded() {
        Runnable listener = this.mCleanListener;
        if (listener != null && this.mScreenReady && this.mColorFadeReady) {
            this.mCleanListener = null;
            listener.run();
        }
    }
}
