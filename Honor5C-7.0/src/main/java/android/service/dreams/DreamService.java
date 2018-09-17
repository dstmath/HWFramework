package android.service.dreams;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ProxyInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.DocumentsContract.Document;
import android.security.keymaster.KeymasterDefs;
import android.service.dreams.IDreamService.Stub;
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
import android.view.Window.Callback;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.policy.HwPolicyFactory;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.DumpUtils.Dump;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class DreamService extends Service implements Callback {
    public static final String DREAM_META_DATA = "android.service.dream";
    public static final String DREAM_SERVICE = "dreams";
    public static final String SERVICE_INTERFACE = "android.service.dreams.DreamService";
    private final String TAG;
    private boolean mCanDoze;
    private HwCustDreamService mCust;
    private boolean mDebug;
    private int mDozeScreenBrightness;
    private int mDozeScreenState;
    private boolean mDozing;
    private boolean mFinished;
    private boolean mFullscreen;
    private final Handler mHandler;
    private boolean mInteractive;
    private boolean mLowProfile;
    private final IDreamManager mSandman;
    private boolean mScreenBright;
    private boolean mStarted;
    private boolean mWaking;
    private Window mWindow;
    private IBinder mWindowToken;
    private boolean mWindowless;

    /* renamed from: android.service.dreams.DreamService.2 */
    class AnonymousClass2 implements Dump {
        final /* synthetic */ String[] val$args;
        final /* synthetic */ FileDescriptor val$fd;

        AnonymousClass2(FileDescriptor val$fd, String[] val$args) {
            this.val$fd = val$fd;
            this.val$args = val$args;
        }

        public void dump(PrintWriter pw, String prefix) {
            DreamService.this.dumpOnHandler(this.val$fd, pw, this.val$args);
        }
    }

    private final class DreamServiceWrapper extends Stub {

        /* renamed from: android.service.dreams.DreamService.DreamServiceWrapper.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ boolean val$canDoze;
            final /* synthetic */ IBinder val$windowToken;

            AnonymousClass1(IBinder val$windowToken, boolean val$canDoze) {
                this.val$windowToken = val$windowToken;
                this.val$canDoze = val$canDoze;
            }

            public void run() {
                DreamService.this.attach(this.val$windowToken, this.val$canDoze);
            }
        }

        private DreamServiceWrapper() {
        }

        public void attach(IBinder windowToken, boolean canDoze) {
            DreamService.this.mHandler.post(new AnonymousClass1(windowToken, canDoze));
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

    private int applyFlags(int r1, int r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.service.dreams.DreamService.applyFlags(int, int, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.dreams.DreamService.applyFlags(int, int, int):int");
    }

    public DreamService() {
        this.TAG = DreamService.class.getSimpleName() + "[" + getClass().getSimpleName() + "]";
        this.mHandler = new Handler();
        this.mLowProfile = true;
        this.mScreenBright = true;
        this.mDozeScreenState = 0;
        this.mDozeScreenBrightness = -1;
        this.mDebug = false;
        this.mSandman = IDreamManager.Stub.asInterface(ServiceManager.getService(DREAM_SERVICE));
        this.mCust = (HwCustDreamService) HwCustUtils.createObj(HwCustDreamService.class, new Object[]{this});
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

    public void onWindowAttributesChanged(LayoutParams attrs) {
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
        return this.mWindow != null ? this.mWindow.getWindowManager() : null;
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

    public View findViewById(int id) {
        return getWindow().findViewById(id);
    }

    public void setInteractive(boolean interactive) {
        this.mInteractive = interactive;
    }

    public boolean isInteractive() {
        return this.mInteractive;
    }

    public void setLowProfile(boolean lowProfile) {
        if (this.mLowProfile != lowProfile) {
            int i;
            this.mLowProfile = lowProfile;
            if (this.mLowProfile) {
                i = 1;
            } else {
                i = 0;
            }
            applySystemUiVisibilityFlags(i, 1);
        }
    }

    public boolean isLowProfile() {
        return getSystemUiVisibilityFlagValue(1, this.mLowProfile);
    }

    public void setFullscreen(boolean fullscreen) {
        if (this.mFullscreen != fullscreen) {
            int i;
            this.mFullscreen = fullscreen;
            if (this.mFullscreen) {
                i = Document.FLAG_SUPPORTS_REMOVE;
            } else {
                i = 0;
            }
            applyWindowFlags(i, Document.FLAG_SUPPORTS_REMOVE);
        }
    }

    public boolean isFullscreen() {
        return this.mFullscreen;
    }

    public void setScreenBright(boolean screenBright) {
        if (this.mScreenBright != screenBright) {
            int i;
            this.mScreenBright = screenBright;
            if (this.mScreenBright) {
                i = KeymasterDefs.KM_ALGORITHM_HMAC;
            } else {
                i = 0;
            }
            applyWindowFlags(i, KeymasterDefs.KM_ALGORITHM_HMAC);
        }
    }

    public boolean isScreenBright() {
        return getWindowFlagValue(KeymasterDefs.KM_ALGORITHM_HMAC, this.mScreenBright);
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
            Slog.v(this.TAG, "onBind() intent = " + intent);
        }
        return new DreamServiceWrapper();
    }

    public final void finish() {
        if (this.mDebug) {
            Slog.v(this.TAG, "finish(): mFinished=" + this.mFinished);
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

    private void wakeUp(boolean fromSystem) {
        if (this.mDebug) {
            Slog.v(this.TAG, "wakeUp(): fromSystem=" + fromSystem + ", mWaking=" + this.mWaking + ", mFinished=" + this.mFinished);
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

    private final void detach() {
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

    private final void attach(IBinder windowToken, boolean canDoze) {
        int i = 0;
        if (this.mWindowToken != null) {
            Slog.e(this.TAG, "attach() called when already attached with token=" + this.mWindowToken);
        } else if (this.mFinished || this.mWaking) {
            Slog.w(this.TAG, "attach() called after dream already finished");
            try {
                this.mSandman.finishSelf(windowToken, true);
            } catch (RemoteException e) {
            }
        } else {
            if (this.mCust != null) {
                this.mCust.enableChargingAlbum();
            }
            this.mWindowToken = windowToken;
            this.mCanDoze = canDoze;
            if (!this.mWindowless || this.mCanDoze) {
                if (!this.mWindowless) {
                    this.mWindow = HwPolicyFactory.getHwPhoneWindow(this);
                    this.mWindow.setCallback(this);
                    this.mWindow.requestFeature(1);
                    this.mWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                    this.mWindow.setFormat(-1);
                    LayoutParams lp = this.mWindow.getAttributes();
                    if (this.mCust == null || !this.mCust.isChargingAlbumEnabled()) {
                        int i2;
                        if (this.mDebug) {
                            Slog.v(this.TAG, String.format("Attaching window token: %s to window of type %s", new Object[]{windowToken, Integer.valueOf(2023)}));
                        }
                        lp.type = 2023;
                        lp.token = windowToken;
                        lp.windowAnimations = 16974584;
                        int i3 = lp.flags;
                        int i4 = 4784385 | (this.mFullscreen ? Document.FLAG_SUPPORTS_REMOVE : 0);
                        if (this.mScreenBright) {
                            i2 = KeymasterDefs.KM_ALGORITHM_HMAC;
                        } else {
                            i2 = 0;
                        }
                        lp.flags = (i2 | i4) | i3;
                    } else {
                        this.mCust.setAlbumLayoutParams(lp, windowToken);
                    }
                    this.mWindow.setAttributes(lp);
                    this.mWindow.clearFlags(KeymasterDefs.KM_BIGNUM);
                    this.mWindow.setWindowManager(null, windowToken, "dream", true);
                    if (!(this.mCust == null || this.mCust.isChargingAlbumEnabled())) {
                        if (this.mLowProfile) {
                            i = 1;
                        }
                        applySystemUiVisibilityFlags(i, 1);
                    }
                    try {
                        getWindowManager().addView(this.mWindow.getDecorView(), this.mWindow.getAttributes());
                    } catch (BadTokenException e2) {
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
                            DreamService.this.mStarted = true;
                            DreamService.this.onDreamingStarted();
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
            LayoutParams lp = this.mWindow.getAttributes();
            lp.flags = applyFlags(lp.flags, flags, mask);
            this.mWindow.setAttributes(lp);
            this.mWindow.getWindowManager().updateViewLayout(this.mWindow.getDecorView(), lp);
        }
    }

    private boolean getSystemUiVisibilityFlagValue(int flag, boolean defaultValue) {
        View v = null;
        if (this.mWindow != null) {
            v = this.mWindow.getDecorView();
        }
        if (v == null) {
            return defaultValue;
        }
        return (v.getSystemUiVisibility() & flag) != 0;
    }

    private void applySystemUiVisibilityFlags(int flags, int mask) {
        View v = null;
        if (this.mWindow != null) {
            v = this.mWindow.getDecorView();
        }
        if (v != null) {
            v.setSystemUiVisibility(applyFlags(v.getSystemUiVisibility(), flags, mask));
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        DumpUtils.dumpAsync(this.mHandler, new AnonymousClass2(fd, args), pw, ProxyInfo.LOCAL_EXCL_LIST, 1000);
    }

    protected void dumpOnHandler(FileDescriptor fd, PrintWriter pw, String[] args) {
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
        return MathUtils.constrain(value, 0, Process.PROC_TERM_MASK);
    }
}
