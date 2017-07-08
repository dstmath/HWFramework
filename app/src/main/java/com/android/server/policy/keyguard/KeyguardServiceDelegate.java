package com.android.server.policy.keyguard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Flog;
import android.util.Jlog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.OnKeyguardExitResult;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback.Stub;
import com.android.internal.policy.IKeyguardService;
import com.android.server.UiThread;
import java.io.PrintWriter;

public class KeyguardServiceDelegate {
    private static boolean DEBUG = false;
    private static final int INTERACTIVE_STATE_AWAKE = 1;
    private static final int INTERACTIVE_STATE_GOING_TO_SLEEP = 2;
    private static final int INTERACTIVE_STATE_SLEEP = 0;
    private static final int SCREEN_STATE_OFF = 0;
    private static final int SCREEN_STATE_ON = 2;
    private static final int SCREEN_STATE_TURNING_ON = 1;
    private static final String TAG = "KeyguardServiceDelegate";
    private final Context mContext;
    private DrawnListener mDrawnListenerWhenConnect;
    private final ServiceConnection mKeyguardConnection;
    protected KeyguardServiceWrapper mKeyguardService;
    private final KeyguardState mKeyguardState;
    private final View mScrim;
    private final Handler mScrimHandler;
    private DrawnListener mShowListener;

    public interface DrawnListener {
        void onDrawn();
    }

    /* renamed from: com.android.server.policy.keyguard.KeyguardServiceDelegate.2 */
    static class AnonymousClass2 implements Runnable {
        final /* synthetic */ LayoutParams val$lp;
        final /* synthetic */ View val$view;
        final /* synthetic */ WindowManager val$wm;

        AnonymousClass2(WindowManager val$wm, View val$view, LayoutParams val$lp) {
            this.val$wm = val$wm;
            this.val$view = val$view;
            this.val$lp = val$lp;
        }

        public void run() {
            this.val$wm.addView(this.val$view, this.val$lp);
        }
    }

    private final class KeyguardExitDelegate extends Stub {
        private OnKeyguardExitResult mOnKeyguardExitResult;

        KeyguardExitDelegate(OnKeyguardExitResult onKeyguardExitResult) {
            this.mOnKeyguardExitResult = onKeyguardExitResult;
        }

        public void onKeyguardExitResult(boolean success) throws RemoteException {
            Flog.i(305, "**** onKeyguardExitResult(" + success + ") CALLED ****");
            if (this.mOnKeyguardExitResult != null) {
                this.mOnKeyguardExitResult.onKeyguardExitResult(success);
            }
        }
    }

    private final class KeyguardShowDelegate extends IKeyguardDrawnCallback.Stub {
        private DrawnListener mDrawnListener;

        KeyguardShowDelegate(DrawnListener drawnListener) {
            this.mDrawnListener = drawnListener;
        }

        public void onDrawn() throws RemoteException {
            Jlog.d(76, "KeyguardServiceDelegate:onShown");
            Flog.i(305, "**** SHOWN CALLED ****");
            if (this.mDrawnListener != null) {
                this.mDrawnListener.onDrawn();
            }
            KeyguardServiceDelegate.this.hideScrim();
        }
    }

    private static final class KeyguardState {
        public boolean bootCompleted;
        public int currentUser;
        boolean deviceHasKeyguard;
        boolean dreaming;
        public boolean enabled;
        boolean inputRestricted;
        public int interactiveState;
        boolean occluded;
        public int offReason;
        public int screenState;
        boolean secure;
        boolean showing;
        boolean showingAndNotOccluded;
        boolean systemIsReady;

        KeyguardState() {
            this.showing = true;
            this.showingAndNotOccluded = true;
            this.secure = true;
            this.deviceHasKeyguard = false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.keyguard.KeyguardServiceDelegate.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.policy.keyguard.KeyguardServiceDelegate.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.keyguard.KeyguardServiceDelegate.<clinit>():void");
    }

    public KeyguardServiceDelegate(Context context) {
        this.mKeyguardState = new KeyguardState();
        this.mKeyguardConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                Flog.i(305, "*** Keyguard connected (yay!), systemIsReady " + KeyguardServiceDelegate.this.mKeyguardState.systemIsReady);
                KeyguardServiceDelegate.this.mShowListener = null;
                KeyguardServiceDelegate.this.mKeyguardService = new KeyguardServiceWrapper(KeyguardServiceDelegate.this.mContext, IKeyguardService.Stub.asInterface(service));
                if (KeyguardServiceDelegate.this.mKeyguardState.systemIsReady) {
                    KeyguardServiceDelegate.this.mKeyguardService.onSystemReady();
                    if (KeyguardServiceDelegate.this.mKeyguardState.interactiveState == KeyguardServiceDelegate.SCREEN_STATE_TURNING_ON) {
                        KeyguardServiceDelegate.this.mKeyguardService.onStartedWakingUp();
                    }
                    if (KeyguardServiceDelegate.this.mKeyguardState.screenState == KeyguardServiceDelegate.SCREEN_STATE_ON || KeyguardServiceDelegate.this.mKeyguardState.screenState == KeyguardServiceDelegate.SCREEN_STATE_TURNING_ON) {
                        KeyguardServiceDelegate.this.mKeyguardService.onScreenTurningOn(new KeyguardShowDelegate(KeyguardServiceDelegate.this.mDrawnListenerWhenConnect));
                    }
                    if (KeyguardServiceDelegate.this.mKeyguardState.screenState == KeyguardServiceDelegate.SCREEN_STATE_ON) {
                        KeyguardServiceDelegate.this.mKeyguardService.onScreenTurnedOn();
                    }
                    KeyguardServiceDelegate.this.mDrawnListenerWhenConnect = null;
                }
                KeyguardServiceDelegate.this.mKeyguardState.deviceHasKeyguard = true;
                if (KeyguardServiceDelegate.this.mKeyguardState.bootCompleted) {
                    KeyguardServiceDelegate.this.mKeyguardService.onBootCompleted();
                }
                if (KeyguardServiceDelegate.this.mKeyguardState.occluded) {
                    KeyguardServiceDelegate.this.mKeyguardService.setOccluded(KeyguardServiceDelegate.this.mKeyguardState.occluded);
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                Flog.i(305, "*** Keyguard disconnected (boo!)");
                KeyguardServiceDelegate.this.mKeyguardService = null;
                KeyguardServiceDelegate.this.mKeyguardState.deviceHasKeyguard = false;
                if (KeyguardServiceDelegate.this.mShowListener != null) {
                    Flog.i(305, "*** Keyguard disconnected when screen on");
                    KeyguardServiceDelegate.this.mShowListener.onDrawn();
                    KeyguardServiceDelegate.this.mShowListener = null;
                }
            }
        };
        this.mContext = context;
        this.mScrimHandler = UiThread.getHandler();
        this.mScrim = createScrim(context, this.mScrimHandler);
    }

    public void bindService(Context context) {
        Intent intent = new Intent();
        ComponentName keyguardComponent = ComponentName.unflattenFromString(context.getApplicationContext().getResources().getString(17039463));
        intent.addFlags(DumpState.DUMP_SHARED_USERS);
        intent.setComponent(keyguardComponent);
        if (context.bindServiceAsUser(intent, this.mKeyguardConnection, SCREEN_STATE_TURNING_ON, this.mScrimHandler, UserHandle.SYSTEM)) {
            Flog.i(305, "*** Keyguard started");
            return;
        }
        Log.v(TAG, "*** Keyguard: can't bind to " + keyguardComponent);
        Flog.i(305, "*** Keyguard: can't bind to " + keyguardComponent);
        this.mKeyguardState.showing = false;
        this.mKeyguardState.showingAndNotOccluded = false;
        this.mKeyguardState.secure = false;
        synchronized (this.mKeyguardState) {
            this.mKeyguardState.deviceHasKeyguard = false;
            hideScrim();
        }
    }

    public boolean isShowing() {
        if (this.mKeyguardService != null) {
            this.mKeyguardState.showing = this.mKeyguardService.isShowing();
        }
        return this.mKeyguardState.showing;
    }

    public boolean isInputRestricted() {
        if (this.mKeyguardService != null) {
            this.mKeyguardState.inputRestricted = this.mKeyguardService.isInputRestricted();
        }
        return this.mKeyguardState.inputRestricted;
    }

    public void verifyUnlock(OnKeyguardExitResult onKeyguardExitResult) {
        if (this.mKeyguardService != null) {
            Flog.i(305, "verifyUnlock()");
            this.mKeyguardService.verifyUnlock(new KeyguardExitDelegate(onKeyguardExitResult));
        }
    }

    public void keyguardDone(boolean authenticated, boolean wakeup) {
        if (this.mKeyguardService != null) {
            Flog.i(305, "keyguardDone(" + authenticated + ")");
            this.mKeyguardService.keyguardDone(authenticated, wakeup);
        }
    }

    public void setOccluded(boolean isOccluded) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.setOccluded(isOccluded);
        }
        this.mKeyguardState.occluded = isOccluded;
    }

    public boolean isOccluded() {
        if (this.mKeyguardService != null) {
            return this.mKeyguardService.isOccluded();
        }
        return false;
    }

    public void dismiss() {
        if (this.mKeyguardService != null) {
            Flog.i(305, "dismiss()");
            this.mKeyguardService.dismiss();
        }
    }

    public boolean isSecure(int userId) {
        if (this.mKeyguardService != null) {
            this.mKeyguardState.secure = this.mKeyguardService.isSecure(userId);
        }
        return this.mKeyguardState.secure;
    }

    public void onDreamingStarted() {
        if (this.mKeyguardService != null) {
            Flog.i(305, "onDreamingStarted()");
            this.mKeyguardService.onDreamingStarted();
        }
        this.mKeyguardState.dreaming = true;
    }

    public void onDreamingStopped() {
        if (this.mKeyguardService != null) {
            Flog.i(305, "onDreamingStopped()");
            this.mKeyguardService.onDreamingStopped();
        }
        this.mKeyguardState.dreaming = false;
    }

    public void onStartedWakingUp() {
        if (this.mKeyguardService != null) {
            Flog.i(305, "onStartedWakingUp()");
            this.mKeyguardService.onStartedWakingUp();
        }
        this.mKeyguardState.interactiveState = SCREEN_STATE_TURNING_ON;
    }

    public void onScreenTurnedOff() {
        if (this.mKeyguardService != null) {
            if (DEBUG) {
                Log.v(TAG, "onScreenTurnedOff()");
            }
            Flog.i(305, "onScreenTurnedOff()");
            this.mKeyguardService.onScreenTurnedOff();
        }
        this.mKeyguardState.screenState = SCREEN_STATE_OFF;
    }

    public void onScreenTurningOn(DrawnListener drawnListener) {
        if (this.mKeyguardService != null) {
            Jlog.d(71, "KeyguardServiceDelegate:onScreenTurnedOn");
            Flog.i(NativeResponseCode.SERVICE_FOUND, "KeyguardServiceDelegate  onScreenTurnedOn(showListener = " + drawnListener + ")");
            this.mKeyguardService.onScreenTurningOn(new KeyguardShowDelegate(drawnListener));
        } else {
            Flog.i(NativeResponseCode.SERVICE_FOUND, "KeyguardServiceDelegate  onScreenTurningOn(): no keyguard service!");
            this.mDrawnListenerWhenConnect = drawnListener;
            showScrim();
        }
        this.mKeyguardState.screenState = SCREEN_STATE_TURNING_ON;
    }

    public void onScreenTurnedOn() {
        if (this.mKeyguardService != null) {
            if (DEBUG) {
                Log.v(TAG, "onScreenTurnedOn()");
            }
            Flog.i(305, "onScreenTurnedOn()");
            this.mKeyguardService.onScreenTurnedOn();
        }
        this.mKeyguardState.screenState = SCREEN_STATE_ON;
    }

    public void onStartedGoingToSleep(int why) {
        if (this.mKeyguardService != null) {
            Flog.i(305, "onStartedGoingToSleep(" + why + ")");
            this.mKeyguardService.onStartedGoingToSleep(why);
        }
        this.mKeyguardState.offReason = why;
        this.mKeyguardState.interactiveState = SCREEN_STATE_ON;
    }

    public void onFinishedGoingToSleep(int why, boolean cameraGestureTriggered) {
        if (this.mKeyguardService != null) {
            Flog.i(305, "onFinishedGoingToSleep(" + why + ")");
            this.mKeyguardService.onFinishedGoingToSleep(why, cameraGestureTriggered);
        }
        this.mKeyguardState.interactiveState = SCREEN_STATE_OFF;
    }

    public void setKeyguardEnabled(boolean enabled) {
        if (this.mKeyguardService != null) {
            Flog.i(305, "setKeyguardEnabled(" + enabled + ")");
            this.mKeyguardService.setKeyguardEnabled(enabled);
        }
        this.mKeyguardState.enabled = enabled;
    }

    public void onSystemReady() {
        if (this.mKeyguardService != null) {
            Flog.i(305, "onSystemReady()");
            this.mKeyguardService.onSystemReady();
            return;
        }
        this.mKeyguardState.systemIsReady = true;
    }

    public void doKeyguardTimeout(Bundle options) {
        if (this.mKeyguardService != null) {
            Flog.i(305, "doKeyguardTimeout(), options " + options);
            this.mKeyguardService.doKeyguardTimeout(options);
        }
    }

    public void setCurrentUser(int newUserId) {
        if (this.mKeyguardService != null) {
            Flog.i(305, "setCurrentUser(" + newUserId + ")");
            this.mKeyguardService.setCurrentUser(newUserId);
        }
        this.mKeyguardState.currentUser = newUserId;
    }

    public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        if (this.mKeyguardService != null) {
            Flog.i(305, "startKeyguardExitAnimation, startTime " + startTime);
            this.mKeyguardService.startKeyguardExitAnimation(startTime, fadeoutDuration);
        }
    }

    private static View createScrim(Context context, Handler handler) {
        View view = new View(context);
        LayoutParams lp = new LayoutParams(-1, -1, 2029, 1116416, -3);
        lp.softInputMode = 16;
        lp.screenOrientation = 5;
        lp.privateFlags |= SCREEN_STATE_TURNING_ON;
        lp.setTitle("KeyguardScrim");
        WindowManager wm = (WindowManager) context.getSystemService("window");
        view.setSystemUiVisibility(56688640);
        handler.post(new AnonymousClass2(wm, view, lp));
        return view;
    }

    public void showScrim() {
        synchronized (this.mKeyguardState) {
            Flog.i(305, "showScrim(), deviceHasKeyguard " + this.mKeyguardState.deviceHasKeyguard);
            if (this.mKeyguardState.deviceHasKeyguard) {
                this.mScrimHandler.post(new Runnable() {
                    public void run() {
                        KeyguardServiceDelegate.this.mScrim.setVisibility(KeyguardServiceDelegate.SCREEN_STATE_OFF);
                    }
                });
                return;
            }
        }
    }

    public void hideScrim() {
        Flog.i(305, "hideScrim()");
        this.mScrimHandler.post(new Runnable() {
            public void run() {
                KeyguardServiceDelegate.this.mScrim.setVisibility(8);
            }
        });
    }

    public void onBootCompleted() {
        if (this.mKeyguardService != null) {
            Flog.i(305, "onBootCompleted()");
            this.mKeyguardService.onBootCompleted();
        }
        this.mKeyguardState.bootCompleted = true;
    }

    public void onActivityDrawn() {
        if (this.mKeyguardService != null) {
            Flog.i(305, "onActivityDrawn()");
            this.mKeyguardService.onActivityDrawn();
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + TAG);
        prefix = prefix + "  ";
        pw.println(prefix + "showing=" + this.mKeyguardState.showing);
        pw.println(prefix + "showingAndNotOccluded=" + this.mKeyguardState.showingAndNotOccluded);
        pw.println(prefix + "inputRestricted=" + this.mKeyguardState.inputRestricted);
        pw.println(prefix + "occluded=" + this.mKeyguardState.occluded);
        pw.println(prefix + "secure=" + this.mKeyguardState.secure);
        pw.println(prefix + "dreaming=" + this.mKeyguardState.dreaming);
        pw.println(prefix + "systemIsReady=" + this.mKeyguardState.systemIsReady);
        pw.println(prefix + "deviceHasKeyguard=" + this.mKeyguardState.deviceHasKeyguard);
        pw.println(prefix + "enabled=" + this.mKeyguardState.enabled);
        pw.println(prefix + "offReason=" + this.mKeyguardState.offReason);
        pw.println(prefix + "currentUser=" + this.mKeyguardState.currentUser);
        pw.println(prefix + "bootCompleted=" + this.mKeyguardState.bootCompleted);
        pw.println(prefix + "screenState=" + this.mKeyguardState.screenState);
        pw.println(prefix + "interactiveState=" + this.mKeyguardState.interactiveState);
        if (this.mKeyguardService != null) {
            this.mKeyguardService.dump(prefix, pw);
        }
    }
}
