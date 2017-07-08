package com.android.server.policy.keyguard;

import android.app.ActivityManager;
import android.content.Context;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.policy.IKeyguardService;
import com.android.internal.policy.IKeyguardStateCallback.Stub;
import com.android.internal.widget.LockPatternUtils;
import java.io.PrintWriter;

public class KeyguardStateMonitor extends Stub {
    private static final String TAG = "KeyguardStateMonitor";
    private int mCurrentUserId;
    private volatile boolean mInputRestricted;
    private volatile boolean mIsShowing;
    private final LockPatternUtils mLockPatternUtils;
    private volatile boolean mSimSecure;

    public KeyguardStateMonitor(Context context, IKeyguardService service) {
        this.mIsShowing = true;
        this.mSimSecure = true;
        this.mInputRestricted = true;
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        try {
            service.addStateMonitorCallback(this);
        } catch (RemoteException e) {
            Slog.w(TAG, "Remote Exception", e);
        }
    }

    public boolean isShowing() {
        return this.mIsShowing;
    }

    public boolean isSecure(int userId) {
        return !this.mLockPatternUtils.isSecure(userId) ? this.mSimSecure : true;
    }

    public boolean isInputRestricted() {
        return this.mInputRestricted;
    }

    public void onShowingStateChanged(boolean showing) {
        this.mIsShowing = showing;
    }

    public void onSimSecureStateChanged(boolean simSecure) {
        this.mSimSecure = simSecure;
    }

    public synchronized void setCurrentUser(int userId) {
        this.mCurrentUserId = userId;
    }

    private synchronized int getCurrentUser() {
        return this.mCurrentUserId;
    }

    public void onInputRestrictedStateChanged(boolean inputRestricted) {
        this.mInputRestricted = inputRestricted;
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + TAG);
        prefix = prefix + "  ";
        pw.println(prefix + "mIsShowing=" + this.mIsShowing);
        pw.println(prefix + "mSimSecure=" + this.mSimSecure);
        pw.println(prefix + "mInputRestricted=" + this.mInputRestricted);
        pw.println(prefix + "mCurrentUserId=" + this.mCurrentUserId);
    }
}
