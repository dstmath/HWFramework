package com.android.server.power;

import android.app.ActivityManager;
import android.app.SynchronousUserSwitchObserver;
import android.attention.AttentionManagerInternal;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Slog;
import android.util.StatsLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.LocalServices;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class AttentionDetector {
    private static final boolean DEBUG = false;
    private static final String TAG = "AttentionDetector";
    @VisibleForTesting
    protected AttentionManagerInternal mAttentionManager;
    @VisibleForTesting
    AttentionCallbackInternalImpl mCallback;
    private AtomicLong mConsecutiveTimeoutExtendedCount = new AtomicLong(0);
    @VisibleForTesting
    protected ContentResolver mContentResolver;
    private Context mContext;
    private boolean mIsSettingEnabled;
    private long mLastActedOnNextScreenDimming;
    private long mLastUserActivityTime;
    private final Object mLock;
    private long mMaxAttentionApiTimeoutMillis;
    @VisibleForTesting
    protected long mMaximumExtensionMillis;
    private final Runnable mOnUserAttention;
    @VisibleForTesting
    protected PackageManager mPackageManager;
    @VisibleForTesting
    protected int mRequestId;
    private final AtomicBoolean mRequested;
    private int mWakefulness;

    public AttentionDetector(Runnable onUserAttention, Object lock) {
        this.mOnUserAttention = onUserAttention;
        this.mLock = lock;
        this.mRequested = new AtomicBoolean(false);
        this.mRequestId = 0;
        this.mWakefulness = 1;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateEnabledFromSettings(Context context) {
        boolean z = false;
        if (Settings.System.getIntForUser(context.getContentResolver(), "adaptive_sleep", 0, -2) == 1) {
            z = true;
        }
        this.mIsSettingEnabled = z;
    }

    public void systemReady(Context context) {
        this.mContext = context;
        updateEnabledFromSettings(context);
        this.mPackageManager = context.getPackageManager();
        this.mContentResolver = context.getContentResolver();
        this.mAttentionManager = (AttentionManagerInternal) LocalServices.getService(AttentionManagerInternal.class);
        this.mMaximumExtensionMillis = (long) context.getResources().getInteger(17694736);
        this.mMaxAttentionApiTimeoutMillis = (long) context.getResources().getInteger(17694735);
        try {
            ActivityManager.getService().registerUserSwitchObserver(new UserSwitchObserver(), TAG);
        } catch (RemoteException e) {
        }
    }

    public void register() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("adaptive_sleep"), false, new ContentObserver(new Handler()) {
            /* class com.android.server.power.AttentionDetector.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                AttentionDetector attentionDetector = AttentionDetector.this;
                attentionDetector.updateEnabledFromSettings(attentionDetector.mContext);
            }
        }, -1);
    }

    public long updateUserActivity(long nextScreenDimming) {
        if (nextScreenDimming == this.mLastActedOnNextScreenDimming || !this.mIsSettingEnabled || !isAttentionServiceSupported()) {
            return nextScreenDimming;
        }
        if (!serviceHasSufficientPermissions()) {
            Settings.System.putInt(this.mContentResolver, "adaptive_sleep", 0);
            return nextScreenDimming;
        }
        long now = SystemClock.uptimeMillis();
        long whenToCheck = nextScreenDimming - getAttentionTimeout();
        long whenToStopExtending = this.mLastUserActivityTime + this.mMaximumExtensionMillis;
        if (now < whenToCheck) {
            return whenToCheck;
        }
        if (whenToStopExtending < whenToCheck) {
            return nextScreenDimming;
        }
        if (this.mRequested.get()) {
            return whenToCheck;
        }
        this.mRequested.set(true);
        this.mRequestId++;
        this.mLastActedOnNextScreenDimming = nextScreenDimming;
        this.mCallback = new AttentionCallbackInternalImpl(this.mRequestId);
        Slog.v(TAG, "Checking user attention, ID: " + this.mRequestId);
        if (!this.mAttentionManager.checkAttention(getAttentionTimeout(), this.mCallback)) {
            this.mRequested.set(false);
        }
        return whenToCheck;
    }

    public int onUserActivity(long eventTime, int event) {
        if (event == 0 || event == 1 || event == 2 || event == 3) {
            cancelCurrentRequestIfAny();
            this.mLastUserActivityTime = eventTime;
            resetConsecutiveExtensionCount();
            return 1;
        } else if (event != 4) {
            return -1;
        } else {
            this.mConsecutiveTimeoutExtendedCount.incrementAndGet();
            return 0;
        }
    }

    public void onWakefulnessChangeStarted(int wakefulness) {
        this.mWakefulness = wakefulness;
        if (wakefulness != 1) {
            cancelCurrentRequestIfAny();
            resetConsecutiveExtensionCount();
        }
    }

    private void cancelCurrentRequestIfAny() {
        if (this.mRequested.get()) {
            this.mAttentionManager.cancelAttentionCheck(this.mCallback);
            this.mRequested.set(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetConsecutiveExtensionCount() {
        long previousCount = this.mConsecutiveTimeoutExtendedCount.getAndSet(0);
        if (previousCount > 0) {
            StatsLog.write(168, previousCount);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public long getAttentionTimeout() {
        return this.mMaxAttentionApiTimeoutMillis;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isAttentionServiceSupported() {
        AttentionManagerInternal attentionManagerInternal = this.mAttentionManager;
        return attentionManagerInternal != null && attentionManagerInternal.isAttentionServiceSupported();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean serviceHasSufficientPermissions() {
        String attentionPackage = this.mPackageManager.getAttentionServicePackageName();
        return attentionPackage != null && this.mPackageManager.checkPermission("android.permission.CAMERA", attentionPackage) == 0;
    }

    public void dump(PrintWriter pw) {
        pw.println("AttentionDetector:");
        pw.println(" mMaximumExtensionMillis=" + this.mMaximumExtensionMillis);
        pw.println(" mMaxAttentionApiTimeoutMillis=" + this.mMaxAttentionApiTimeoutMillis);
        pw.println(" mLastUserActivityTime(excludingAttention)=" + this.mLastUserActivityTime);
        pw.println(" mAttentionServiceSupported=" + isAttentionServiceSupported());
        pw.println(" mRequested=" + this.mRequested);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public final class AttentionCallbackInternalImpl extends AttentionManagerInternal.AttentionCallbackInternal {
        private final int mId;

        AttentionCallbackInternalImpl(int id) {
            this.mId = id;
        }

        public void onSuccess(int result, long timestamp) {
            Slog.v(AttentionDetector.TAG, "onSuccess: " + result + ", ID: " + this.mId);
            if (this.mId == AttentionDetector.this.mRequestId && AttentionDetector.this.mRequested.getAndSet(false)) {
                synchronized (AttentionDetector.this.mLock) {
                    if (AttentionDetector.this.mWakefulness == 1) {
                        if (result == 1) {
                            AttentionDetector.this.mOnUserAttention.run();
                        } else {
                            AttentionDetector.this.resetConsecutiveExtensionCount();
                        }
                    }
                }
            }
        }

        public void onFailure(int error) {
            Slog.i(AttentionDetector.TAG, "Failed to check attention: " + error + ", ID: " + this.mId);
            AttentionDetector.this.mRequested.set(false);
        }
    }

    /* access modifiers changed from: private */
    public final class UserSwitchObserver extends SynchronousUserSwitchObserver {
        private UserSwitchObserver() {
        }

        public void onUserSwitching(int newUserId) throws RemoteException {
            AttentionDetector attentionDetector = AttentionDetector.this;
            attentionDetector.updateEnabledFromSettings(attentionDetector.mContext);
        }
    }
}
