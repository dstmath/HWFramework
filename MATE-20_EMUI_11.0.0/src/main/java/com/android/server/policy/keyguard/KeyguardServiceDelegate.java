package com.android.server.policy.keyguard;

import android.app.ActivityTaskManager;
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
    private static final boolean DEBUG = false;
    private static final int INTERACTIVE_STATE_AWAKE = 2;
    private static final int INTERACTIVE_STATE_GOING_TO_SLEEP = 3;
    private static final int INTERACTIVE_STATE_SLEEP = 0;
    private static final int INTERACTIVE_STATE_WAKING = 1;
    private static final int SCREEN_STATE_OFF = 0;
    private static final int SCREEN_STATE_ON = 2;
    private static final int SCREEN_STATE_TURNING_OFF = 3;
    private static final int SCREEN_STATE_TURNING_ON = 1;
    private static final String TAG = "KeyguardServiceDelegate";
    private final KeyguardStateMonitor.StateCallback mCallback;
    private final Context mContext;
    private DrawnListener mDrawnListenerWhenConnect;
    private final Handler mHandler;
    private final ServiceConnection mKeyguardConnection = new ServiceConnection() {
        /* class com.android.server.policy.keyguard.KeyguardServiceDelegate.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(KeyguardServiceDelegate.TAG, "*** Keyguard connected (yay!)");
            KeyguardServiceDelegate keyguardServiceDelegate = KeyguardServiceDelegate.this;
            keyguardServiceDelegate.mKeyguardService = new KeyguardServiceWrapper(keyguardServiceDelegate.mContext, IKeyguardService.Stub.asInterface(service), KeyguardServiceDelegate.this.mCallback);
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
                    KeyguardServiceWrapper keyguardServiceWrapper = KeyguardServiceDelegate.this.mKeyguardService;
                    KeyguardServiceDelegate keyguardServiceDelegate2 = KeyguardServiceDelegate.this;
                    keyguardServiceWrapper.onScreenTurningOn(new KeyguardShowDelegate(keyguardServiceDelegate2.mDrawnListenerWhenConnect));
                }
                if (KeyguardServiceDelegate.this.mKeyguardState.screenState == 2) {
                    KeyguardServiceDelegate.this.mKeyguardService.onScreenTurnedOn();
                }
                KeyguardServiceDelegate.this.mDrawnListenerWhenConnect = null;
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

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Log.i(KeyguardServiceDelegate.TAG, "*** Keyguard disconnected (boo!)");
            KeyguardServiceDelegate keyguardServiceDelegate = KeyguardServiceDelegate.this;
            keyguardServiceDelegate.mKeyguardService = null;
            keyguardServiceDelegate.mKeyguardState.reset();
            KeyguardServiceDelegate.this.mHandler.post($$Lambda$KeyguardServiceDelegate$1$ZQ5qG3EmC57J43br9oobeNISXyE.INSTANCE);
        }

        static /* synthetic */ void lambda$onServiceDisconnected$0() {
            try {
                ActivityTaskManager.getService().setLockScreenShown(true, false);
            } catch (RemoteException e) {
            }
        }
    };
    protected KeyguardServiceWrapper mKeyguardService;
    private KeyguardStateMonitor.HwPCKeyguardShowingCallback mKeyguardShowingCallback;
    private final KeyguardState mKeyguardState = new KeyguardState();
    OverscanTimeout mOverscanTimeout = new OverscanTimeout();

    public interface DrawnListener {
        void onDrawn();
    }

    /* access modifiers changed from: private */
    public static final class KeyguardState {
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
        /* access modifiers changed from: public */
        private void reset() {
            this.showing = true;
            this.showingAndNotOccluded = true;
            this.secure = true;
            this.deviceHasKeyguard = true;
            this.enabled = true;
            this.currentUser = -10000;
        }
    }

    /* access modifiers changed from: private */
    public final class KeyguardShowDelegate extends IKeyguardDrawnCallback.Stub {
        private DrawnListener mDrawnListener;

        KeyguardShowDelegate(DrawnListener drawnListener) {
            this.mDrawnListener = drawnListener;
        }

        public void onDrawn() throws RemoteException {
            DrawnListener drawnListener = this.mDrawnListener;
            if (drawnListener != null) {
                drawnListener.onDrawn();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class KeyguardExitDelegate extends IKeyguardExitCallback.Stub {
        private WindowManagerPolicy.OnKeyguardExitResult mOnKeyguardExitResult;

        KeyguardExitDelegate(WindowManagerPolicy.OnKeyguardExitResult onKeyguardExitResult) {
            this.mOnKeyguardExitResult = onKeyguardExitResult;
        }

        public void onKeyguardExitResult(boolean success) throws RemoteException {
            WindowManagerPolicy.OnKeyguardExitResult onKeyguardExitResult = this.mOnKeyguardExitResult;
            if (onKeyguardExitResult != null) {
                onKeyguardExitResult.onKeyguardExitResult(success);
            }
        }
    }

    public KeyguardServiceDelegate(Context context, KeyguardStateMonitor.StateCallback callback) {
        this.mContext = context;
        this.mHandler = UiThread.getHandler();
        this.mCallback = callback;
    }

    public void bindService(Context context) {
        Intent intent = new Intent();
        ComponentName keyguardComponent = ComponentName.unflattenFromString(context.getApplicationContext().getResources().getString(17039866));
        intent.addFlags(256);
        intent.setComponent(keyguardComponent);
        if (!context.bindServiceAsUser(intent, this.mKeyguardConnection, 1, this.mHandler, UserHandle.SYSTEM)) {
            Log.i(TAG, "*** Keyguard: can't bind to " + keyguardComponent);
            KeyguardState keyguardState = this.mKeyguardState;
            keyguardState.showing = false;
            keyguardState.showingAndNotOccluded = false;
            keyguardState.secure = false;
            synchronized (keyguardState) {
                this.mKeyguardState.deviceHasKeyguard = false;
            }
            return;
        }
        Log.i(TAG, "*** Keyguard started");
    }

    public boolean isShowing() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            this.mKeyguardState.showing = keyguardServiceWrapper.isShowing();
        }
        return this.mKeyguardState.showing;
    }

    public boolean isTrusted() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            return keyguardServiceWrapper.isTrusted();
        }
        return false;
    }

    public boolean hasLockscreenWallpaper() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            return keyguardServiceWrapper.hasLockscreenWallpaper();
        }
        return false;
    }

    public boolean hasKeyguard() {
        return this.mKeyguardState.deviceHasKeyguard;
    }

    public boolean isInputRestricted() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            this.mKeyguardState.inputRestricted = keyguardServiceWrapper.isInputRestricted();
        }
        return this.mKeyguardState.inputRestricted;
    }

    public void verifyUnlock(WindowManagerPolicy.OnKeyguardExitResult onKeyguardExitResult) {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.verifyUnlock(new KeyguardExitDelegate(onKeyguardExitResult));
        }
    }

    /* access modifiers changed from: package-private */
    public class OverscanTimeout implements Runnable {
        public static final String SINGLE_HAND_MODE = "single_hand_mode";

        OverscanTimeout() {
        }

        @Override // java.lang.Runnable
        public void run() {
            Slog.i(KeyguardServiceDelegate.TAG, "exitSingleHandMode run");
            Settings.Global.putString(KeyguardServiceDelegate.this.mContext.getContentResolver(), SINGLE_HAND_MODE, "");
        }
    }

    public void setOccluded(boolean isOccluded, boolean animate) {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.setOccluded(isOccluded, animate);
        }
        this.mKeyguardState.occluded = isOccluded;
        if (!isOccluded && isShowing()) {
            this.mHandler.postDelayed(this.mOverscanTimeout, 200);
        }
    }

    public boolean isOccluded() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            return keyguardServiceWrapper.isOccluded();
        }
        return false;
    }

    public void dismiss(IKeyguardDismissCallback callback, CharSequence message) {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.dismiss(callback, message);
        }
    }

    public boolean isSecure(int userId) {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            this.mKeyguardState.secure = keyguardServiceWrapper.isSecure(userId);
        }
        return this.mKeyguardState.secure;
    }

    public void onDreamingStarted() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.onDreamingStarted();
        }
        this.mKeyguardState.dreaming = true;
    }

    public void onDreamingStopped() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.onDreamingStopped();
        }
        this.mKeyguardState.dreaming = false;
    }

    public void onStartedWakingUp() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.onStartedWakingUp();
        }
        this.mKeyguardState.interactiveState = 1;
    }

    public void onFinishedWakingUp() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.onFinishedWakingUp();
        }
        this.mKeyguardState.interactiveState = 2;
    }

    public void onScreenTurningOff() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.onScreenTurningOff();
        }
        this.mKeyguardState.screenState = 3;
    }

    public void onScreenTurnedOff() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.onScreenTurnedOff();
        }
        this.mKeyguardState.screenState = 0;
    }

    public void onScreenTurningOn(DrawnListener drawnListener) {
        if (this.mKeyguardService != null) {
            Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power  onScreenTurningOn(showListener = " + drawnListener + ")");
            this.mKeyguardService.onScreenTurningOn(new KeyguardShowDelegate(drawnListener));
        } else {
            Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power  onScreenTurningOn(): no keyguard service!");
            this.mDrawnListenerWhenConnect = drawnListener;
        }
        this.mKeyguardState.screenState = 1;
    }

    public void onScreenTurnedOn() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.onScreenTurnedOn();
        }
        this.mKeyguardState.screenState = 2;
    }

    public void onStartedGoingToSleep(int why) {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.onStartedGoingToSleep(why);
        }
        KeyguardState keyguardState = this.mKeyguardState;
        keyguardState.offReason = why;
        keyguardState.interactiveState = 3;
    }

    public void onFinishedGoingToSleep(int why, boolean cameraGestureTriggered) {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.onFinishedGoingToSleep(why, cameraGestureTriggered);
        }
        this.mKeyguardState.interactiveState = 0;
    }

    public void setKeyguardEnabled(boolean enabled) {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.setKeyguardEnabled(enabled);
        }
        this.mKeyguardState.enabled = enabled;
    }

    public void onSystemReady() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.onSystemReady();
        } else {
            this.mKeyguardState.systemIsReady = true;
        }
    }

    public void doKeyguardTimeout(Bundle options) {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.doKeyguardTimeout(options);
        }
    }

    public void setCurrentUser(int newUserId) {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.setCurrentUser(newUserId);
        }
        this.mKeyguardState.currentUser = newUserId;
    }

    public void setSwitchingUser(boolean switching) {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.setSwitchingUser(switching);
        }
    }

    public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.startKeyguardExitAnimation(startTime, fadeoutDuration);
        }
    }

    public void onBootCompleted() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.onBootCompleted();
        }
        this.mKeyguardState.bootCompleted = true;
    }

    public void onShortPowerPressedGoHome() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.onShortPowerPressedGoHome();
        }
    }

    public void doFaceRecognize(boolean detect, String reason) {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.doFaceRecognize(detect, reason);
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
        pw.println(prefix + TAG);
        String prefix2 = prefix + "  ";
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
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null) {
            keyguardServiceWrapper.dump(prefix2, pw);
        }
    }

    private static String screenStateToString(int screen) {
        if (screen == 0) {
            return "SCREEN_STATE_OFF";
        }
        if (screen == 1) {
            return "SCREEN_STATE_TURNING_ON";
        }
        if (screen == 2) {
            return "SCREEN_STATE_ON";
        }
        if (screen != 3) {
            return Integer.toString(screen);
        }
        return "SCREEN_STATE_TURNING_OFF";
    }

    private static String interactiveStateToString(int interactive) {
        if (interactive == 0) {
            return "INTERACTIVE_STATE_SLEEP";
        }
        if (interactive == 1) {
            return "INTERACTIVE_STATE_WAKING";
        }
        if (interactive == 2) {
            return "INTERACTIVE_STATE_AWAKE";
        }
        if (interactive != 3) {
            return Integer.toString(interactive);
        }
        return "INTERACTIVE_STATE_GOING_TO_SLEEP";
    }

    public boolean isPendingLock() {
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper == null) {
            return false;
        }
        this.mKeyguardState.inputRestricted = keyguardServiceWrapper.isPendingLock();
        return this.mKeyguardState.inputRestricted;
    }

    public void setHwPCKeyguardShowingCallback(KeyguardStateMonitor.HwPCKeyguardShowingCallback callback) {
        this.mKeyguardShowingCallback = callback;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void putHwPcKeyguardShowingCallbackWhenServiceConnected() {
        KeyguardStateMonitor.HwPCKeyguardShowingCallback hwPCKeyguardShowingCallback;
        KeyguardServiceWrapper keyguardServiceWrapper = this.mKeyguardService;
        if (keyguardServiceWrapper != null && (hwPCKeyguardShowingCallback = this.mKeyguardShowingCallback) != null) {
            keyguardServiceWrapper.setHwPCKeyguardShowingCallback(hwPCKeyguardShowingCallback);
        }
    }
}
