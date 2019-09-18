package android.service.dreams;

import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.dreams.IDreamManager;
import android.service.dreams.IDreamService;
import android.util.MathUtils;
import android.util.Slog;
import android.view.ActionMode;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.policy.HwPolicyFactory;
import com.android.internal.util.DumpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class DreamService extends Service implements Window.Callback {
    public static final String DREAM_META_DATA = "android.service.dream";
    public static final String DREAM_SERVICE = "dreams";
    public static final String SERVICE_INTERFACE = "android.service.dreams.DreamService";
    /* access modifiers changed from: private */
    public final String TAG = (DreamService.class.getSimpleName() + "[" + getClass().getSimpleName() + "]");
    private boolean mCanDoze;
    /* access modifiers changed from: private */
    public boolean mDebug = false;
    private int mDozeScreenBrightness = -1;
    private int mDozeScreenState = 0;
    private boolean mDozing;
    private boolean mFinished;
    private boolean mFullscreen;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler();
    private boolean mInteractive;
    private boolean mLowProfile = true;
    private final IDreamManager mSandman = IDreamManager.Stub.asInterface(ServiceManager.getService(DREAM_SERVICE));
    private boolean mScreenBright = true;
    /* access modifiers changed from: private */
    public boolean mStarted;
    private boolean mWaking;
    /* access modifiers changed from: private */
    public Window mWindow;
    private IBinder mWindowToken;
    /* access modifiers changed from: private */
    public boolean mWindowless;

    private final class DreamServiceWrapper extends IDreamService.Stub {
        private DreamServiceWrapper() {
        }

        public void attach(final IBinder windowToken, final boolean canDoze, final IRemoteCallback started) {
            DreamService.this.mHandler.post(new Runnable() {
                public void run() {
                    DreamService.this.attach(windowToken, canDoze, started);
                }
            });
        }

        public void detach() {
            DreamService.this.mHandler.post(new Runnable() {
                public void run() {
                    DreamService.this.detach();
                }
            });
        }

        public void wakeUp() {
            DreamService.this.mHandler.post(new Runnable() {
                public void run() {
                    DreamService.this.wakeUp(true);
                }
            });
        }
    }

    public void setDebug(boolean dbg) {
        this.mDebug = dbg;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!this.mInteractive) {
            if (this.mDebug) {
                Slog.v(this.TAG, "Waking up on keyEvent");
            }
            wakeUp();
            return true;
        } else if (event.getKeyCode() != 4) {
            return this.mWindow.superDispatchKeyEvent(event);
        } else {
            if (this.mDebug) {
                Slog.v(this.TAG, "Waking up on back key");
            }
            wakeUp();
            return true;
        }
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        if (this.mInteractive) {
            return this.mWindow.superDispatchKeyShortcutEvent(event);
        }
        if (this.mDebug) {
            Slog.v(this.TAG, "Waking up on keyShortcutEvent");
        }
        wakeUp();
        return true;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (this.mInteractive) {
            return this.mWindow.superDispatchTouchEvent(event);
        }
        if (this.mDebug) {
            Slog.v(this.TAG, "Waking up on touchEvent");
        }
        wakeUp();
        return true;
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        if (this.mInteractive) {
            return this.mWindow.superDispatchTrackballEvent(event);
        }
        if (this.mDebug) {
            Slog.v(this.TAG, "Waking up on trackballEvent");
        }
        wakeUp();
        return true;
    }

    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (this.mInteractive) {
            return this.mWindow.superDispatchGenericMotionEvent(event);
        }
        if (this.mDebug) {
            Slog.v(this.TAG, "Waking up on genericMotionEvent");
        }
        wakeUp();
        return true;
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return false;
    }

    public View onCreatePanelView(int featureId) {
        return null;
    }

    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return false;
    }

    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return false;
    }

    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return false;
    }

    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
    }

    public void onContentChanged() {
    }

    public void onWindowFocusChanged(boolean hasFocus) {
    }

    public void onAttachedToWindow() {
    }

    public void onDetachedFromWindow() {
    }

    public void onPanelClosed(int featureId, Menu menu) {
    }

    public boolean onSearchRequested(SearchEvent event) {
        return onSearchRequested();
    }

    public boolean onSearchRequested() {
        return false;
    }

    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return null;
    }

    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        return null;
    }

    public void onActionModeStarted(ActionMode mode) {
    }

    public void onActionModeFinished(ActionMode mode) {
    }

    public WindowManager getWindowManager() {
        if (this.mWindow != null) {
            return this.mWindow.getWindowManager();
        }
        return null;
    }

    public Window getWindow() {
        return this.mWindow;
    }

    public void setContentView(int layoutResID) {
        getWindow().setContentView(layoutResID);
    }

    public void setContentView(View view) {
        getWindow().setContentView(view);
    }

    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getWindow().setContentView(view, params);
    }

    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getWindow().addContentView(view, params);
    }

    public <T extends View> T findViewById(int id) {
        return getWindow().findViewById(id);
    }

    public final <T extends View> T requireViewById(int id) {
        T view = findViewById(id);
        if (view != null) {
            return view;
        }
        throw new IllegalArgumentException("ID does not reference a View inside this DreamService");
    }

    public void setInteractive(boolean interactive) {
        this.mInteractive = interactive;
    }

    public boolean isInteractive() {
        return this.mInteractive;
    }

    public void setLowProfile(boolean lowProfile) {
        if (this.mLowProfile != lowProfile) {
            this.mLowProfile = lowProfile;
            applySystemUiVisibilityFlags(this.mLowProfile ? 1 : 0, 1);
        }
    }

    public boolean isLowProfile() {
        return getSystemUiVisibilityFlagValue(1, this.mLowProfile);
    }

    public void setFullscreen(boolean fullscreen) {
        if (this.mFullscreen != fullscreen) {
            this.mFullscreen = fullscreen;
            applyWindowFlags(this.mFullscreen ? 1024 : 0, 1024);
        }
    }

    public boolean isFullscreen() {
        return this.mFullscreen;
    }

    public void setScreenBright(boolean screenBright) {
        if (this.mScreenBright != screenBright) {
            this.mScreenBright = screenBright;
            applyWindowFlags(this.mScreenBright ? 128 : 0, 128);
        }
    }

    public boolean isScreenBright() {
        return getWindowFlagValue(128, this.mScreenBright);
    }

    public void setWindowless(boolean windowless) {
        this.mWindowless = windowless;
    }

    public boolean isWindowless() {
        return this.mWindowless;
    }

    public boolean canDoze() {
        return this.mCanDoze;
    }

    public void startDozing() {
        if (this.mCanDoze && !this.mDozing) {
            this.mDozing = true;
            updateDoze();
        }
    }

    private void updateDoze() {
        if (this.mWindowToken == null) {
            Slog.w(this.TAG, "Updating doze without a window token.");
            return;
        }
        if (this.mDozing) {
            try {
                this.mSandman.startDozing(this.mWindowToken, this.mDozeScreenState, this.mDozeScreenBrightness);
            } catch (RemoteException e) {
            }
        }
    }

    public void stopDozing() {
        if (this.mDozing) {
            this.mDozing = false;
            try {
                this.mSandman.stopDozing(this.mWindowToken);
            } catch (RemoteException e) {
            }
        }
    }

    public boolean isDozing() {
        return this.mDozing;
    }

    public int getDozeScreenState() {
        return this.mDozeScreenState;
    }

    public void setDozeScreenState(int state) {
        if (this.mDozeScreenState != state) {
            this.mDozeScreenState = state;
            updateDoze();
        }
    }

    public int getDozeScreenBrightness() {
        return this.mDozeScreenBrightness;
    }

    public void setDozeScreenBrightness(int brightness) {
        if (brightness != -1) {
            brightness = clampAbsoluteBrightness(brightness);
        }
        if (this.mDozeScreenBrightness != brightness) {
            this.mDozeScreenBrightness = brightness;
            updateDoze();
        }
    }

    public void onCreate() {
        if (this.mDebug) {
            Slog.v(this.TAG, "onCreate()");
        }
        super.onCreate();
    }

    public void onDreamingStarted() {
        if (this.mDebug) {
            Slog.v(this.TAG, "onDreamingStarted()");
        }
    }

    public void onDreamingStopped() {
        if (this.mDebug) {
            Slog.v(this.TAG, "onDreamingStopped()");
        }
    }

    public void onWakeUp() {
        finish();
    }

    public final IBinder onBind(Intent intent) {
        if (this.mDebug) {
            String str = this.TAG;
            Slog.v(str, "onBind() intent = " + intent);
        }
        return new DreamServiceWrapper();
    }

    public final void finish() {
        if (this.mDebug) {
            String str = this.TAG;
            Slog.v(str, "finish(): mFinished=" + this.mFinished);
        }
        if (!this.mFinished) {
            this.mFinished = true;
            if (this.mWindowToken == null) {
                Slog.w(this.TAG, "Finish was called before the dream was attached.");
            } else {
                try {
                    this.mSandman.finishSelf(this.mWindowToken, true);
                } catch (RemoteException e) {
                }
            }
            stopSelf();
        }
    }

    public final void wakeUp() {
        wakeUp(false);
    }

    /* access modifiers changed from: private */
    public void wakeUp(boolean fromSystem) {
        if (this.mDebug) {
            String str = this.TAG;
            Slog.v(str, "wakeUp(): fromSystem=" + fromSystem + ", mWaking=" + this.mWaking + ", mFinished=" + this.mFinished);
        }
        if (!this.mWaking && !this.mFinished) {
            this.mWaking = true;
            onWakeUp();
            if (!fromSystem && !this.mFinished) {
                if (this.mWindowToken == null) {
                    Slog.w(this.TAG, "WakeUp was called before the dream was attached.");
                    return;
                }
                try {
                    this.mSandman.finishSelf(this.mWindowToken, false);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public void onDestroy() {
        if (this.mDebug) {
            Slog.v(this.TAG, "onDestroy()");
        }
        detach();
        super.onDestroy();
    }

    /* access modifiers changed from: private */
    public final void detach() {
        if (this.mStarted) {
            if (this.mDebug) {
                Slog.v(this.TAG, "detach(): Calling onDreamingStopped()");
            }
            this.mStarted = false;
            onDreamingStopped();
        }
        if (this.mWindow != null) {
            if (this.mDebug) {
                Slog.v(this.TAG, "detach(): Removing window from window manager");
            }
            this.mWindow.getWindowManager().removeViewImmediate(this.mWindow.getDecorView());
            this.mWindow = null;
        }
        if (this.mWindowToken != null) {
            WindowManagerGlobal.getInstance().closeAll(this.mWindowToken, getClass().getName(), "Dream");
            this.mWindowToken = null;
            this.mCanDoze = false;
        }
    }

    /* access modifiers changed from: private */
    public final void attach(IBinder windowToken, boolean canDoze, final IRemoteCallback started) {
        if (this.mWindowToken != null) {
            String str = this.TAG;
            Slog.e(str, "attach() called when already attached with token=" + this.mWindowToken);
        } else if (this.mFinished || this.mWaking) {
            Slog.w(this.TAG, "attach() called after dream already finished");
            try {
                this.mSandman.finishSelf(windowToken, true);
            } catch (RemoteException e) {
                Slog.e(this.TAG, "find RemoteException in function attach");
            }
        } else {
            this.mWindowToken = windowToken;
            this.mCanDoze = canDoze;
            if (!this.mWindowless || this.mCanDoze) {
                if (!this.mWindowless) {
                    this.mWindow = HwPolicyFactory.getHwPhoneWindow(this);
                    this.mWindow.setCallback(this);
                    this.mWindow.requestFeature(1);
                    this.mWindow.setBackgroundDrawable(new ColorDrawable(-16777216));
                    this.mWindow.setFormat(-1);
                    int i = 0;
                    if (this.mDebug) {
                        Slog.v(this.TAG, String.format("Attaching window token: %s to window of type %s", new Object[]{windowToken, Integer.valueOf(WindowManager.LayoutParams.TYPE_DREAM)}));
                    }
                    WindowManager.LayoutParams lp = this.mWindow.getAttributes();
                    lp.type = WindowManager.LayoutParams.TYPE_DREAM;
                    lp.token = windowToken;
                    lp.windowAnimations = 16974574;
                    int i2 = lp.flags;
                    int i3 = 4784385 | (this.mFullscreen ? 1024 : 0);
                    if (this.mScreenBright) {
                        i = 128;
                    }
                    lp.flags = i2 | i | i3;
                    this.mWindow.setAttributes(lp);
                    this.mWindow.clearFlags(Integer.MIN_VALUE);
                    this.mWindow.setWindowManager(null, windowToken, "dream", true);
                    applySystemUiVisibilityFlags(this.mLowProfile ? 1 : 0, 1);
                    try {
                        getWindowManager().addView(this.mWindow.getDecorView(), this.mWindow.getAttributes());
                    } catch (WindowManager.BadTokenException e2) {
                        Slog.i(this.TAG, "attach() called after window token already removed, dream will finish soon");
                        this.mWindow = null;
                        return;
                    }
                }
                this.mHandler.post(new Runnable() {
                    public void run() {
                        if (DreamService.this.mWindow != null || DreamService.this.mWindowless) {
                            if (DreamService.this.mDebug) {
                                Slog.v(DreamService.this.TAG, "Calling onDreamingStarted()");
                            }
                            boolean unused = DreamService.this.mStarted = true;
                            try {
                                DreamService.this.onDreamingStarted();
                                try {
                                } catch (RemoteException e) {
                                    throw e.rethrowFromSystemServer();
                                }
                            } finally {
                                try {
                                    started.sendResult(null);
                                } catch (RemoteException e2) {
                                    throw e2.rethrowFromSystemServer();
                                }
                            }
                        }
                    }
                });
                return;
            }
            throw new IllegalStateException("Only doze dreams can be windowless");
        }
    }

    private boolean getWindowFlagValue(int flag, boolean defaultValue) {
        if (this.mWindow == null) {
            return defaultValue;
        }
        return (this.mWindow.getAttributes().flags & flag) != 0;
    }

    private void applyWindowFlags(int flags, int mask) {
        if (this.mWindow != null) {
            WindowManager.LayoutParams lp = this.mWindow.getAttributes();
            lp.flags = applyFlags(lp.flags, flags, mask);
            this.mWindow.setAttributes(lp);
            this.mWindow.getWindowManager().updateViewLayout(this.mWindow.getDecorView(), lp);
        }
    }

    private boolean getSystemUiVisibilityFlagValue(int flag, boolean defaultValue) {
        View v = this.mWindow == null ? null : this.mWindow.getDecorView();
        if (v == null) {
            return defaultValue;
        }
        return (v.getSystemUiVisibility() & flag) != 0;
    }

    private void applySystemUiVisibilityFlags(int flags, int mask) {
        View v = this.mWindow == null ? null : this.mWindow.getDecorView();
        if (v != null) {
            v.setSystemUiVisibility(applyFlags(v.getSystemUiVisibility(), flags, mask));
        }
    }

    private int applyFlags(int oldFlags, int flags, int mask) {
        return ((~mask) & oldFlags) | (flags & mask);
    }

    /* access modifiers changed from: protected */
    public void dump(final FileDescriptor fd, PrintWriter pw, final String[] args) {
        DumpUtils.dumpAsync(this.mHandler, new DumpUtils.Dump() {
            public void dump(PrintWriter pw, String prefix) {
                DreamService.this.dumpOnHandler(fd, pw, args);
            }
        }, pw, "", 1000);
    }

    /* access modifiers changed from: protected */
    public void dumpOnHandler(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print(this.TAG + ": ");
        if (this.mWindowToken == null) {
            pw.println("stopped");
        } else {
            pw.println("running (token=" + this.mWindowToken + ")");
        }
        pw.println("  window: " + this.mWindow);
        pw.print("  flags:");
        if (isInteractive()) {
            pw.print(" interactive");
        }
        if (isLowProfile()) {
            pw.print(" lowprofile");
        }
        if (isFullscreen()) {
            pw.print(" fullscreen");
        }
        if (isScreenBright()) {
            pw.print(" bright");
        }
        if (isWindowless()) {
            pw.print(" windowless");
        }
        if (isDozing()) {
            pw.print(" dozing");
        } else if (canDoze()) {
            pw.print(" candoze");
        }
        pw.println();
        if (canDoze()) {
            pw.println("  doze screen state: " + Display.stateToString(this.mDozeScreenState));
            pw.println("  doze screen brightness: " + this.mDozeScreenBrightness);
        }
    }

    private static int clampAbsoluteBrightness(int value) {
        return MathUtils.constrain(value, 0, 255);
    }
}
