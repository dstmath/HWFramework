package com.android.server.policy.keyguard;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Flog;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.WindowManagerPolicyConstants;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardService;
import com.android.server.NsdService;
import com.android.server.UiThread;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.keyguard.KeyguardStateMonitor;
import java.io.PrintWriter;

public class KeyguardServiceDelegate {
    /* access modifiers changed from: private */
    public static boolean DEBUG = true;
    private static final int INTERACTIVE_STATE_AWAKE = 2;
    private static final int INTERACTIVE_STATE_GOING_TO_SLEEP = 3;
    private static final int INTERACTIVE_STATE_SLEEP = 0;
    private static final int INTERACTIVE_STATE_WAKING = 1;
    private static final int SCREEN_STATE_OFF = 0;
    private static final int SCREEN_STATE_ON = 2;
    private static final int SCREEN_STATE_TURNING_OFF = 3;
    private static final int SCREEN_STATE_TURNING_ON = 1;
    public static final String SINGLE_HAND_MODE = "single_hand_mode";
    private static final String TAG = "KeyguardServiceDelegate";
    /* access modifiers changed from: private */
    public final KeyguardStateMonitor.StateCallback mCallback;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public DrawnListener mDrawnListenerWhenConnect;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final ServiceConnection mKeyguardConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Flog.i(305, "*** Keyguard connected (yay!), systemIsReady " + KeyguardServiceDelegate.this.mKeyguardState.systemIsReady);
            DrawnListener unused = KeyguardServiceDelegate.this.mShowListener = null;
            KeyguardServiceDelegate.this.mKeyguardService = new KeyguardServiceWrapper(KeyguardServiceDelegate.this.mContext, IKeyguardService.Stub.asInterface(service), KeyguardServiceDelegate.this.mCallback);
            if (KeyguardServiceDelegate.this.mKeyguardState.systemIsReady) {
                KeyguardServiceDelegate.this.mKeyguardService.onSystemReady();
                if (KeyguardServiceDelegate.this.mKeyguardState.currentUser != -10000) {
                    KeyguardServiceDelegate.this.mKeyguardService.setCurrentUser(KeyguardServiceDelegate.this.mKeyguardState.currentUser);
                }
                if (KeyguardServiceDelegate.this.mKeyguardState.interactiveState == 2 || KeyguardServiceDelegate.this.mKeyguardState.interactiveState == 1) {
                    KeyguardServiceDelegate.this.mKeyguardService.onStartedWakingUp();
                }
                if (KeyguardServiceDelegate.this.mKeyguardState.interactiveState == 2) {
                    KeyguardServiceDelegate.this.mKeyguardService.onFinishedWakingUp();
                }
                if (KeyguardServiceDelegate.this.mKeyguardState.screenState == 2 || KeyguardServiceDelegate.this.mKeyguardState.screenState == 1) {
                    KeyguardServiceDelegate.this.mKeyguardService.onScreenTurningOn(new KeyguardShowDelegate(KeyguardServiceDelegate.this.mDrawnListenerWhenConnect));
                }
                if (KeyguardServiceDelegate.this.mKeyguardState.screenState == 2) {
                    KeyguardServiceDelegate.this.mKeyguardService.onScreenTurnedOn();
                }
                DrawnListener unused2 = KeyguardServiceDelegate.this.mDrawnListenerWhenConnect = null;
            }
            if (KeyguardServiceDelegate.this.mKeyguardState.bootCompleted) {
                KeyguardServiceDelegate.this.mKeyguardService.onBootCompleted();
            }
            if (KeyguardServiceDelegate.this.mKeyguardState.occluded) {
                KeyguardServiceDelegate.this.mKeyguardService.setOccluded(KeyguardServiceDelegate.this.mKeyguardState.occluded, false);
            }
            if (!KeyguardServiceDelegate.this.mKeyguardState.enabled) {
                KeyguardServiceDelegate.this.mKeyguardService.setKeyguardEnabled(KeyguardServiceDelegate.this.mKeyguardState.enabled);
            }
            KeyguardServiceDelegate.this.putHwPcKeyguardShowingCallbackWhenServiceConnected();
        }

        public void onServiceDisconnected(ComponentName name) {
            if (KeyguardServiceDelegate.DEBUG) {
                Log.v(KeyguardServiceDelegate.TAG, "*** Keyguard disconnected (boo!)");
            }
            KeyguardServiceDelegate.this.mKeyguardService = null;
            KeyguardServiceDelegate.this.mKeyguardState.reset();
            KeyguardServiceDelegate.this.mHandler.post($$Lambda$KeyguardServiceDelegate$1$ZQ5qG3EmC57J43br9oobeNISXyE.INSTANCE);
        }

        static /* synthetic */ void lambda$onServiceDisconnected$0() {
            try {
                ActivityManager.getService().setLockScreenShown(true, false, -1);
            } catch (RemoteException e) {
            }
        }
    };
    protected KeyguardServiceWrapper mKeyguardService;
    private KeyguardStateMonitor.HwPCKeyguardShowingCallback mKeyguardShowingCallback;
    /* access modifiers changed from: private */
    public final KeyguardState mKeyguardState = new KeyguardState();
    OverscanTimeout mOverscanTimeout = new OverscanTimeout();
    /* access modifiers changed from: private */
    public DrawnListener mShowListener;

    public interface DrawnListener {
        void onDrawn();
    }

    private final class KeyguardExitDelegate extends IKeyguardExitCallback.Stub {
        private WindowManagerPolicy.OnKeyguardExitResult mOnKeyguardExitResult;

        KeyguardExitDelegate(WindowManagerPolicy.OnKeyguardExitResult onKeyguardExitResult) {
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
            reset();
        }

        /* access modifiers changed from: private */
        public void reset() {
            this.showing = true;
            this.showingAndNotOccluded = true;
            this.secure = true;
            this.deviceHasKeyguard = true;
            this.enabled = true;
            this.currentUser = -10000;
        }
    }

    class OverscanTimeout implements Runnable {
        OverscanTimeout() {
        }

        public void run() {
            Slog.i(KeyguardServiceDelegate.TAG, "exitSingleHandMode run");
            Settings.Global.putString(KeyguardServiceDelegate.this.mContext.getContentResolver(), KeyguardServiceDelegate.SINGLE_HAND_MODE, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        }
    }

    public KeyguardServiceDelegate(Context context, KeyguardStateMonitor.StateCallback callback) {
        this.mContext = context;
        this.mHandler = UiThread.getHandler();
        this.mCallback = callback;
    }

    public void bindService(Context context) {
        Intent intent = new Intent();
        ComponentName keyguardComponent = ComponentName.unflattenFromString(context.getApplicationContext().getResources().getString(17039825));
        intent.addFlags(256);
        intent.setComponent(keyguardComponent);
        if (!context.bindServiceAsUser(intent, this.mKeyguardConnection, 1, this.mHandler, UserHandle.SYSTEM)) {
            Log.v(TAG, "*** Keyguard: can't bind to " + keyguardComponent);
            Flog.i(305, "*** Keyguard: can't bind to " + keyguardComponent);
            this.mKeyguardState.showing = false;
            this.mKeyguardState.showingAndNotOccluded = false;
            this.mKeyguardState.secure = false;
            synchronized (this.mKeyguardState) {
                this.mKeyguardState.deviceHasKeyguard = false;
            }
            return;
        }
        Flog.i(305, "*** Keyguard started");
    }

    public boolean isShowing() {
        if (this.mKeyguardService != null) {
            this.mKeyguardState.showing = this.mKeyguardService.isShowing();
        }
        return this.mKeyguardState.showing;
    }

    public boolean isTrusted() {
        if (this.mKeyguardService != null) {
            return this.mKeyguardService.isTrusted();
        }
        return false;
    }

    public boolean hasLockscreenWallpaper() {
        if (this.mKeyguardService != null) {
            return this.mKeyguardService.hasLockscreenWallpaper();
        }
        return false;
    }

    public boolean hasKeyguard() {
        return this.mKeyguardState.deviceHasKeyguard;
    }

    public boolean isInputRestricted() {
        if (this.mKeyguardService != null) {
            this.mKeyguardState.inputRestricted = this.mKeyguardService.isInputRestricted();
        }
        return this.mKeyguardState.inputRestricted;
    }

    public boolean isPendingLock() {
        if (this.mKeyguardService == null) {
            return false;
        }
        this.mKeyguardState.inputRestricted = this.mKeyguardService.isPendingLock();
        return this.mKeyguardState.inputRestricted;
    }

    public void verifyUnlock(WindowManagerPolicy.OnKeyguardExitResult onKeyguardExitResult) {
        if (this.mKeyguardService != null) {
            Flog.i(305, "verifyUnlock()");
            this.mKeyguardService.verifyUnlock(new KeyguardExitDelegate(onKeyguardExitResult));
        }
    }

    public void setOccluded(boolean isOccluded, boolean animate) {
        if (this.mKeyguardService != null) {
            if (DEBUG) {
                Log.v(TAG, "setOccluded(" + isOccluded + ") animate=" + animate);
            }
            this.mKeyguardService.setOccluded(isOccluded, animate);
        }
        this.mKeyguardState.occluded = isOccluded;
        if (!isOccluded && isShowing()) {
            this.mHandler.postDelayed(this.mOverscanTimeout, 200);
        }
    }

    public boolean isOccluded() {
        if (this.mKeyguardService != null) {
            return this.mKeyguardService.isOccluded();
        }
        return false;
    }

    public void dismiss(IKeyguardDismissCallback callback, CharSequence message) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.dismiss(callback, message);
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
        this.mKeyguardState.interactiveState = 1;
    }

    public void onFinishedWakingUp() {
        if (this.mKeyguardService != null) {
            if (DEBUG) {
                Log.v(TAG, "onFinishedWakingUp()");
            }
            this.mKeyguardService.onFinishedWakingUp();
        }
        this.mKeyguardState.interactiveState = 2;
    }

    public void onScreenTurningOff() {
        if (this.mKeyguardService != null) {
            if (DEBUG) {
                Log.v(TAG, "onScreenTurningOff()");
            }
            this.mKeyguardService.onScreenTurningOff();
        }
        this.mKeyguardState.screenState = 3;
    }

    public void onScreenTurnedOff() {
        if (this.mKeyguardService != null) {
            if (DEBUG) {
                Log.v(TAG, "onScreenTurnedOff()");
            }
            Flog.i(305, "onScreenTurnedOff()");
            this.mKeyguardService.onScreenTurnedOff();
        }
        this.mKeyguardState.screenState = 0;
    }

    public void onScreenTurningOn(DrawnListener drawnListener) {
        if (this.mKeyguardService != null) {
            Jlog.d(71, "KeyguardServiceDelegate:onScreenTurningOn");
            Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power  onScreenTurningOn(showListener = " + drawnListener + ")");
            this.mKeyguardService.onScreenTurningOn(new KeyguardShowDelegate(drawnListener));
        } else {
            Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power  onScreenTurningOn(): no keyguard service!");
            this.mDrawnListenerWhenConnect = drawnListener;
        }
        this.mKeyguardState.screenState = 1;
    }

    public void onScreenTurnedOn() {
        if (this.mKeyguardService != null) {
            if (DEBUG) {
                Log.v(TAG, "onScreenTurnedOn()");
            }
            Flog.i(305, "onScreenTurnedOn()");
            this.mKeyguardService.onScreenTurnedOn();
        }
        this.mKeyguardState.screenState = 2;
    }

    public void onStartedGoingToSleep(int why) {
        if (this.mKeyguardService != null) {
            Flog.i(305, "onStartedGoingToSleep(" + why + ")");
            this.mKeyguardService.onStartedGoingToSleep(why);
        }
        this.mKeyguardState.offReason = why;
        this.mKeyguardState.interactiveState = 3;
    }

    public void onFinishedGoingToSleep(int why, boolean cameraGestureTriggered) {
        if (this.mKeyguardService != null) {
            Flog.i(305, "onFinishedGoingToSleep(" + why + ")");
            this.mKeyguardService.onFinishedGoingToSleep(why, cameraGestureTriggered);
        }
        this.mKeyguardState.interactiveState = 0;
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

    public void setSwitchingUser(boolean switching) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.setSwitchingUser(switching);
        }
    }

    public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.startKeyguardExitAnimation(startTime, fadeoutDuration);
        }
    }

    public void onBootCompleted() {
        if (this.mKeyguardService != null) {
            Flog.i(305, "onBootCompleted()");
            this.mKeyguardService.onBootCompleted();
        }
        this.mKeyguardState.bootCompleted = true;
    }

    public void onShortPowerPressedGoHome() {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.onShortPowerPressedGoHome();
        }
    }

    public void doFaceRecognize(boolean detect, String reason) {
        if (this.mKeyguardService != null) {
            this.mKeyguardService.doFaceRecognize(detect, reason);
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1133871366145L, this.mKeyguardState.showing);
        proto.write(1133871366146L, this.mKeyguardState.occluded);
        proto.write(1133871366147L, this.mKeyguardState.secure);
        proto.write(1159641169924L, this.mKeyguardState.screenState);
        proto.write(1159641169925L, this.mKeyguardState.interactiveState);
        proto.end(token);
    }

    public void dump(String prefix, PrintWriter pw) {
        String prefix2;
        pw.println(prefix + TAG);
        pw.println(prefix2 + "showing=" + this.mKeyguardState.showing);
        pw.println(prefix2 + "showingAndNotOccluded=" + this.mKeyguardState.showingAndNotOccluded);
        pw.println(prefix2 + "inputRestricted=" + this.mKeyguardState.inputRestricted);
        pw.println(prefix2 + "occluded=" + this.mKeyguardState.occluded);
        pw.println(prefix2 + "secure=" + this.mKeyguardState.secure);
        pw.println(prefix2 + "dreaming=" + this.mKeyguardState.dreaming);
        pw.println(prefix2 + "systemIsReady=" + this.mKeyguardState.systemIsReady);
        pw.println(prefix2 + "deviceHasKeyguard=" + this.mKeyguardState.deviceHasKeyguard);
        pw.println(prefix2 + "enabled=" + this.mKeyguardState.enabled);
        pw.println(prefix2 + "offReason=" + WindowManagerPolicyConstants.offReasonToString(this.mKeyguardState.offReason));
        pw.println(prefix2 + "currentUser=" + this.mKeyguardState.currentUser);
        pw.println(prefix2 + "bootCompleted=" + this.mKeyguardState.bootCompleted);
        pw.println(prefix2 + "screenState=" + screenStateToString(this.mKeyguardState.screenState));
        pw.println(prefix2 + "interactiveState=" + interactiveStateToString(this.mKeyguardState.interactiveState));
        if (this.mKeyguardService != null) {
            this.mKeyguardService.dump(prefix2, pw);
        }
    }

    public void setHwPCKeyguardShowingCallback(KeyguardStateMonitor.HwPCKeyguardShowingCallback callback) {
        this.mKeyguardShowingCallback = callback;
    }

    /* access modifiers changed from: private */
    public void putHwPcKeyguardShowingCallbackWhenServiceConnected() {
        if (this.mKeyguardService != null && this.mKeyguardShowingCallback != null) {
            this.mKeyguardService.setHwPCKeyguardShowingCallback(this.mKeyguardShowingCallback);
        }
    }

    private static String screenStateToString(int screen) {
        switch (screen) {
            case 0:
                return "SCREEN_STATE_OFF";
            case 1:
                return "SCREEN_STATE_TURNING_ON";
            case 2:
                return "SCREEN_STATE_ON";
            case 3:
                return "SCREEN_STATE_TURNING_OFF";
            default:
                return Integer.toString(screen);
        }
    }

    private static String interactiveStateToString(int interactive) {
        switch (interactive) {
            case 0:
                return "INTERACTIVE_STATE_SLEEP";
            case 1:
                return "INTERACTIVE_STATE_WAKING";
            case 2:
                return "INTERACTIVE_STATE_AWAKE";
            case 3:
                return "INTERACTIVE_STATE_GOING_TO_SLEEP";
            default:
                return Integer.toString(interactive);
        }
    }
}
