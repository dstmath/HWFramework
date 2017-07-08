package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.FloatProperty;
import android.util.Flog;
import android.util.IntProperty;
import android.util.Slog;
import android.view.Choreographer;
import android.view.Display;
import com.android.server.FingerprintUnlockDataCollector;
import java.io.PrintWriter;

public final class DisplayPowerState {
    private static final String AOD_BACK_LIGHT_KEY = "sys.current_backlight";
    public static final FloatProperty<DisplayPowerState> COLOR_FADE_LEVEL = null;
    private static boolean DEBUG = false;
    private static boolean DEBUG_Controller = false;
    private static boolean DEBUG_FPLOG = false;
    private static final int DEFAULT_MAX_BRIGHTNESS = 255;
    private static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    public static final IntProperty<DisplayPowerState> SCREEN_BRIGHTNESS = null;
    private static final String TAG = "DisplayPowerState";
    private static final boolean mSupportAod = false;
    private FingerprintUnlockDataCollector fpDataCollector;
    private final DisplayBlanker mBlanker;
    private final Choreographer mChoreographer;
    private Runnable mCleanListener;
    private final ColorFade mColorFade;
    private boolean mColorFadeDrawPending;
    private final Runnable mColorFadeDrawRunnable;
    private float mColorFadeLevel;
    private boolean mColorFadePrepared;
    private boolean mColorFadeReady;
    private final Handler mHandler;
    private final PhotonicModulator mPhotonicModulator;
    private int mScreenBrightness;
    private boolean mScreenReady;
    private int mScreenState;
    private boolean mScreenUpdatePending;
    private final Runnable mScreenUpdateRunnable;

    /* renamed from: com.android.server.display.DisplayPowerState.1 */
    static class AnonymousClass1 extends FloatProperty<DisplayPowerState> {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(DisplayPowerState object, float value) {
            object.setColorFadeLevel(value);
        }

        public Float get(DisplayPowerState object) {
            return Float.valueOf(object.getColorFadeLevel());
        }
    }

    /* renamed from: com.android.server.display.DisplayPowerState.2 */
    static class AnonymousClass2 extends IntProperty<DisplayPowerState> {
        AnonymousClass2(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(DisplayPowerState object, int value) {
            object.setScreenBrightness(value);
        }

        public Integer get(DisplayPowerState object) {
            return Integer.valueOf(object.getScreenBrightness());
        }
    }

    private final class PhotonicModulator extends Thread {
        private static final int INITIAL_BACKLIGHT = -1;
        private static final int INITIAL_SCREEN_STATE = 1;
        private int mActualBacklight;
        private int mActualState;
        private boolean mBacklightChangeInProgress;
        private final Object mLock;
        private int mPendingBacklight;
        private int mPendingState;
        private boolean mStateChangeInProgress;

        public PhotonicModulator() {
            super("PhotonicModulator");
            this.mLock = new Object();
            this.mPendingState = INITIAL_SCREEN_STATE;
            this.mPendingBacklight = INITIAL_BACKLIGHT;
            this.mActualState = INITIAL_SCREEN_STATE;
            this.mActualBacklight = INITIAL_BACKLIGHT;
        }

        public boolean setState(int state, int backlight) {
            boolean z;
            synchronized (this.mLock) {
                boolean stateChanged = state != this.mPendingState;
                boolean backlightChanged = backlight != this.mPendingBacklight;
                if (stateChanged || backlightChanged) {
                    if (DisplayPowerState.DEBUG_Controller) {
                        Slog.d(DisplayPowerState.TAG, "Requesting new screen state: state=" + Display.stateToString(state) + ", backlight=" + backlight);
                    }
                    this.mPendingState = state;
                    this.mPendingBacklight = backlight;
                    boolean z2 = !this.mStateChangeInProgress ? this.mBacklightChangeInProgress : true;
                    this.mStateChangeInProgress = stateChanged;
                    this.mBacklightChangeInProgress = backlightChanged;
                    if (!z2) {
                        this.mLock.notifyAll();
                    }
                }
                if (this.mStateChangeInProgress) {
                    z = false;
                } else {
                    z = true;
                }
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    if (stateChanged || backlightChanged) {
                        this.mActualState = state;
                        boolean screenOnorOff = backlight == 0 || this.mActualBacklight == 0;
                        this.mActualBacklight = backlight;
                        if (screenOnorOff) {
                            Flog.i(NativeResponseCode.SERVICE_FOUND, "DisplayPowerState Updating screen state: state=" + Display.stateToString(state) + ", backlight=" + backlight);
                            if (2 == state && backlight >= 0 && DisplayPowerState.mSupportAod) {
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.DisplayPowerState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.DisplayPowerState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.DisplayPowerState.<clinit>():void");
    }

    public DisplayPowerState(DisplayBlanker blanker, ColorFade colorFade) {
        this.mScreenUpdateRunnable = new Runnable() {
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
        this.mColorFadeDrawRunnable = new Runnable() {
            public void run() {
                DisplayPowerState.this.mColorFadeDrawPending = false;
                if (DisplayPowerState.this.mColorFadePrepared) {
                    DisplayPowerState.this.mColorFade.draw(DisplayPowerState.this.mColorFadeLevel);
                }
                DisplayPowerState.this.mColorFadeReady = true;
                DisplayPowerState.this.invokeCleanListenerIfNeeded();
            }
        };
        this.mHandler = new Handler(true);
        this.mChoreographer = Choreographer.getInstance();
        this.mBlanker = blanker;
        this.mColorFade = colorFade;
        this.mPhotonicModulator = new PhotonicModulator();
        this.mPhotonicModulator.start();
        initialize(null);
    }

    public DisplayPowerState(Context context, DisplayBlanker blanker, ColorFade colorFade) {
        this.mScreenUpdateRunnable = new Runnable() {
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
        this.mColorFadeDrawRunnable = new Runnable() {
            public void run() {
                DisplayPowerState.this.mColorFadeDrawPending = false;
                if (DisplayPowerState.this.mColorFadePrepared) {
                    DisplayPowerState.this.mColorFade.draw(DisplayPowerState.this.mColorFadeLevel);
                }
                DisplayPowerState.this.mColorFadeReady = true;
                DisplayPowerState.this.invokeCleanListenerIfNeeded();
            }
        };
        this.mHandler = new Handler(true);
        this.mChoreographer = Choreographer.getInstance();
        this.mBlanker = blanker;
        this.mColorFade = colorFade;
        this.mPhotonicModulator = new PhotonicModulator();
        this.mPhotonicModulator.start();
        initialize(context);
    }

    private void initialize(Context context) {
        this.mScreenState = 2;
        this.mScreenBrightness = DEFAULT_MAX_BRIGHTNESS;
        if (context != null) {
            this.mScreenBrightness = ((PowerManager) context.getSystemService("power")).getDefaultScreenBrightnessSetting();
        }
        this.mScreenBrightness = (this.mScreenBrightness * HIGH_PRECISION_MAX_BRIGHTNESS) / DEFAULT_MAX_BRIGHTNESS;
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
        if (this.mScreenReady && this.mColorFadeReady) {
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
