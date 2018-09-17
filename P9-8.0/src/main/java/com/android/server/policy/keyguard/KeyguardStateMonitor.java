package com.android.server.policy.keyguard;

import android.aft.HwAftPolicyManager;
import android.aft.IHwAftPolicyService;
import android.app.ActivityManager;
import android.content.Context;
import android.os.RemoteException;
import android.util.HwPCUtils;
import android.util.Slog;
import com.android.internal.policy.IKeyguardService;
import com.android.internal.policy.IKeyguardStateCallback.Stub;
import com.android.internal.widget.LockPatternUtils;
import java.io.PrintWriter;

public class KeyguardStateMonitor extends Stub {
    private static final String TAG = "KeyguardStateMonitor";
    private final StateCallback mCallback;
    private int mCurrentUserId;
    private volatile boolean mHasLockscreenWallpaper = false;
    private volatile boolean mInputRestricted = true;
    private volatile boolean mIsShowing = true;
    private HwPCKeyguardShowingCallback mKeyguardShowingCallback;
    private final LockPatternUtils mLockPatternUtils;
    private volatile boolean mSimSecure = true;
    private volatile boolean mTrusted = false;

    public interface StateCallback {
        void onTrustedChanged();
    }

    public interface HwPCKeyguardShowingCallback {
        void onShowingChanged(boolean z);
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
        return !this.mLockPatternUtils.isSecure(userId) ? this.mSimSecure : true;
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

    public void onShowingStateChanged(boolean showing) {
        this.mIsShowing = showing;
        if (HwPCUtils.isPcCastModeInServer() && this.mKeyguardShowingCallback != null) {
            this.mKeyguardShowingCallback.onShowingChanged(showing);
        }
        notifyKeyguardStateChange(showing);
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

    public void onTrustedChanged(boolean trusted) {
        this.mTrusted = trusted;
        this.mCallback.onTrustedChanged();
    }

    public void onHasLockscreenWallpaperChanged(boolean hasLockscreenWallpaper) {
        this.mHasLockscreenWallpaper = hasLockscreenWallpaper;
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + TAG);
        prefix = prefix + "  ";
        pw.println(prefix + "mIsShowing=" + this.mIsShowing);
        pw.println(prefix + "mSimSecure=" + this.mSimSecure);
        pw.println(prefix + "mInputRestricted=" + this.mInputRestricted);
        pw.println(prefix + "mTrusted=" + this.mTrusted);
        pw.println(prefix + "mCurrentUserId=" + this.mCurrentUserId);
    }

    public void setHwPCKeyguardShowingCallback(HwPCKeyguardShowingCallback callback) {
        this.mKeyguardShowingCallback = callback;
    }

    public void notifyKeyguardStateChange(boolean isShowing) {
        IHwAftPolicyService hwAft = HwAftPolicyManager.getService();
        if (hwAft != null) {
            try {
                hwAft.notifyKeyguardStateChange(isShowing);
            } catch (RemoteException e) {
                Slog.e(TAG, "notifyKeyguardStateChange throw exception");
            }
        }
    }
}
