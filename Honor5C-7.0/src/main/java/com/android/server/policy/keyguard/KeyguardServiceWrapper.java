package com.android.server.policy.keyguard;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Flog;
import android.util.Slog;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardService;
import com.android.internal.policy.IKeyguardStateCallback;
import java.io.PrintWriter;

public class KeyguardServiceWrapper implements IKeyguardService {
    private String TAG;
    private KeyguardStateMonitor mKeyguardStateMonitor;
    private boolean mOccluded;
    private IKeyguardService mService;

    public KeyguardServiceWrapper(Context context, IKeyguardService service) {
        this.TAG = "KeyguardServiceWrapper";
        this.mOccluded = false;
        this.mService = service;
        this.mKeyguardStateMonitor = new KeyguardStateMonitor(context, service);
    }

    public void verifyUnlock(IKeyguardExitCallback callback) {
        try {
            this.mService.verifyUnlock(callback);
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void keyguardDone(boolean authenticated, boolean wakeup) {
        try {
            this.mService.keyguardDone(authenticated, wakeup);
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void setOccluded(boolean isOccluded) {
        try {
            if (this.mOccluded != isOccluded) {
                this.mOccluded = isOccluded;
                Flog.i(305, "setOccluded(" + isOccluded + ")");
            }
            this.mService.setOccluded(isOccluded);
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public boolean isOccluded() {
        return this.mOccluded;
    }

    public void addStateMonitorCallback(IKeyguardStateCallback callback) {
        try {
            this.mService.addStateMonitorCallback(callback);
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void dismiss() {
        try {
            this.mService.dismiss();
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void onDreamingStarted() {
        try {
            this.mService.onDreamingStarted();
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void onDreamingStopped() {
        try {
            this.mService.onDreamingStopped();
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void onStartedGoingToSleep(int reason) {
        try {
            this.mService.onStartedGoingToSleep(reason);
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void onFinishedGoingToSleep(int reason, boolean cameraGestureTriggered) {
        try {
            this.mService.onFinishedGoingToSleep(reason, cameraGestureTriggered);
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void onStartedWakingUp() {
        try {
            this.mService.onStartedWakingUp();
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void onScreenTurningOn(IKeyguardDrawnCallback callback) {
        try {
            this.mService.onScreenTurningOn(callback);
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void onScreenTurnedOn() {
        try {
            this.mService.onScreenTurnedOn();
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void onScreenTurnedOff() {
        try {
            this.mService.onScreenTurnedOff();
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void setKeyguardEnabled(boolean enabled) {
        try {
            this.mService.setKeyguardEnabled(enabled);
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void onSystemReady() {
        try {
            this.mService.onSystemReady();
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void doKeyguardTimeout(Bundle options) {
        try {
            this.mService.doKeyguardTimeout(options);
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void setCurrentUser(int userId) {
        this.mKeyguardStateMonitor.setCurrentUser(userId);
        try {
            this.mService.setCurrentUser(userId);
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void onBootCompleted() {
        try {
            this.mService.onBootCompleted();
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        try {
            this.mService.startKeyguardExitAnimation(startTime, fadeoutDuration);
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public void onActivityDrawn() {
        try {
            this.mService.onActivityDrawn();
        } catch (RemoteException e) {
            Slog.w(this.TAG, "Remote Exception", e);
        }
    }

    public IBinder asBinder() {
        return this.mService.asBinder();
    }

    public boolean isShowing() {
        return this.mKeyguardStateMonitor.isShowing();
    }

    public boolean isSecure(int userId) {
        return this.mKeyguardStateMonitor.isSecure(userId);
    }

    public boolean isInputRestricted() {
        return this.mKeyguardStateMonitor.isInputRestricted();
    }

    public void dump(String prefix, PrintWriter pw) {
        this.mKeyguardStateMonitor.dump(prefix, pw);
    }
}
