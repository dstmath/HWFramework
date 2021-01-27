package com.android.server.policy.keyguard;

import android.app.ActivityManager;
import android.content.Context;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.policy.IKeyguardService;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.internal.widget.LockPatternUtils;
import java.io.PrintWriter;

public class KeyguardStateMonitor extends IKeyguardStateCallback.Stub {
    private static final String TAG = "KeyguardStateMonitor";
    private final StateCallback mCallback;
    private int mCurrentUserId;
    private volatile boolean mHasLockscreenWallpaper = false;
    private volatile boolean mInputRestricted = true;
    private volatile boolean mIsShowing = true;
    private volatile boolean mKeyguardPendingLock = false;
    private HwPCKeyguardShowingCallback mKeyguardShowingCallback;
    private final LockPatternUtils mLockPatternUtils;
    private volatile boolean mSimSecure = true;
    private volatile boolean mTrusted = false;

    public interface HwPCKeyguardShowingCallback {
        void onShowingChanged(boolean z);
    }

    public interface StateCallback {
        void onShowingChanged();

        void onTrustedChanged();
    }

    public KeyguardStateMonitor(Context context, IKeyguardService service, StateCallback callback) {
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        this.mCallback = callback;
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
        return this.mLockPatternUtils.isSecure(userId) || this.mSimSecure;
    }

    public boolean isInputRestricted() {
        return this.mInputRestricted;
    }

    public boolean isTrusted() {
        return this.mTrusted;
    }

    public boolean hasLockscreenWallpaper() {
        return this.mHasLockscreenWallpaper;
    }

    public boolean isPendingLock() {
        return this.mKeyguardPendingLock;
    }

    public void onShowingStateChanged(boolean showing) {
        this.mIsShowing = showing;
        this.mCallback.onShowingChanged();
        HwPCKeyguardShowingCallback hwPCKeyguardShowingCallback = this.mKeyguardShowingCallback;
        if (hwPCKeyguardShowingCallback != null) {
            hwPCKeyguardShowingCallback.onShowingChanged(showing);
        }
    }

    public void onSimSecureStateChanged(boolean simSecure) {
        this.mSimSecure = simSecure;
    }

    public synchronized void setCurrentUser(int userId) {
        this.mCurrentUserId = userId;
    }

    public void onInputRestrictedStateChanged(boolean inputRestricted) {
        this.mInputRestricted = inputRestricted;
    }

    public void onTrustedChanged(boolean trusted) {
        this.mTrusted = trusted;
        this.mCallback.onTrustedChanged();
    }

    public void onHasLockscreenWallpaperChanged(boolean hasLockscreenWallpaper) {
        this.mHasLockscreenWallpaper = hasLockscreenWallpaper;
    }

    public void onPendingLock(boolean pendingLock) {
        this.mKeyguardPendingLock = pendingLock;
        Slog.i(TAG, "onPendingLock pendingLock: " + pendingLock);
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + TAG);
        String prefix2 = prefix + "  ";
        pw.println(prefix2 + "mIsShowing=" + this.mIsShowing);
        pw.println(prefix2 + "mSimSecure=" + this.mSimSecure);
        pw.println(prefix2 + "mInputRestricted=" + this.mInputRestricted);
        pw.println(prefix2 + "mTrusted=" + this.mTrusted);
        pw.println(prefix2 + "mCurrentUserId=" + this.mCurrentUserId);
    }

    public void setHwPCKeyguardShowingCallback(HwPCKeyguardShowingCallback callback) {
        this.mKeyguardShowingCallback = callback;
    }
}
