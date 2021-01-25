package android.content;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.IActivityManager;
import android.app.QueuedWork;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;

public abstract class BroadcastReceiver {
    private boolean mDebugUnregister;
    @UnsupportedAppUsage
    private PendingResult mPendingResult;

    public abstract void onReceive(Context context, Intent intent);

    public static class PendingResult {
        public static final int TYPE_COMPONENT = 0;
        public static final int TYPE_REGISTERED = 1;
        public static final int TYPE_UNREGISTERED = 2;
        @UnsupportedAppUsage
        boolean mAbortBroadcast;
        @UnsupportedAppUsage
        boolean mFinished;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        final int mFlags;
        @UnsupportedAppUsage
        final boolean mInitialStickyHint;
        @UnsupportedAppUsage
        final boolean mOrderedHint;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        int mResultCode;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        String mResultData;
        @UnsupportedAppUsage
        Bundle mResultExtras;
        @UnsupportedAppUsage
        final int mSendingUser;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        final IBinder mToken;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        final int mType;

        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public PendingResult(int resultCode, String resultData, Bundle resultExtras, int type, boolean ordered, boolean sticky, IBinder token, int userId, int flags) {
            this.mResultCode = resultCode;
            this.mResultData = resultData;
            this.mResultExtras = resultExtras;
            this.mType = type;
            this.mOrderedHint = ordered;
            this.mInitialStickyHint = sticky;
            this.mToken = token;
            this.mSendingUser = userId;
            this.mFlags = flags;
        }

        public final void setResultCode(int code) {
            checkSynchronousHint();
            this.mResultCode = code;
        }

        public final int getResultCode() {
            return this.mResultCode;
        }

        public final void setResultData(String data) {
            checkSynchronousHint();
            this.mResultData = data;
        }

        public final String getResultData() {
            return this.mResultData;
        }

        public final void setResultExtras(Bundle extras) {
            checkSynchronousHint();
            this.mResultExtras = extras;
        }

        public final Bundle getResultExtras(boolean makeMap) {
            Bundle e = this.mResultExtras;
            if (!makeMap || e != null) {
                return e;
            }
            Bundle e2 = new Bundle();
            this.mResultExtras = e2;
            return e2;
        }

        public final void setResult(int code, String data, Bundle extras) {
            checkSynchronousHint();
            this.mResultCode = code;
            this.mResultData = data;
            this.mResultExtras = extras;
        }

        public final boolean getAbortBroadcast() {
            return this.mAbortBroadcast;
        }

        public final void abortBroadcast() {
            checkSynchronousHint();
            this.mAbortBroadcast = true;
        }

        public final void clearAbortBroadcast() {
            this.mAbortBroadcast = false;
        }

        public final void finish() {
            int i = this.mType;
            if (i == 0) {
                final IActivityManager mgr = ActivityManager.getService();
                if (QueuedWork.hasPendingWork()) {
                    QueuedWork.queue(new Runnable() {
                        /* class android.content.BroadcastReceiver.PendingResult.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            if (ActivityThread.DEBUG_BROADCAST) {
                                Slog.i(ActivityThread.TAG, "Finishing broadcast after work to component " + PendingResult.this.mToken);
                            }
                            PendingResult.this.sendFinished(mgr);
                        }
                    }, false);
                    return;
                }
                if (ActivityThread.DEBUG_BROADCAST) {
                    Slog.i(ActivityThread.TAG, "Finishing broadcast to component " + this.mToken);
                }
                sendFinished(mgr);
            } else if (this.mOrderedHint && i != 2) {
                if (ActivityThread.DEBUG_BROADCAST) {
                    Slog.i(ActivityThread.TAG, "Finishing broadcast to " + this.mToken);
                }
                sendFinished(ActivityManager.getService());
            }
        }

        public void setExtrasClassLoader(ClassLoader cl) {
            Bundle bundle = this.mResultExtras;
            if (bundle != null) {
                bundle.setClassLoader(cl);
            }
        }

        public void sendFinished(IActivityManager am) {
            synchronized (this) {
                if (!this.mFinished) {
                    this.mFinished = true;
                    try {
                        if (this.mResultExtras != null) {
                            this.mResultExtras.setAllowFds(false);
                        }
                        if (this.mOrderedHint) {
                            am.finishReceiver(this.mToken, this.mResultCode, this.mResultData, this.mResultExtras, this.mAbortBroadcast, this.mFlags);
                        } else {
                            am.finishReceiver(this.mToken, 0, null, null, false, this.mFlags);
                        }
                    } catch (RemoteException e) {
                    }
                } else {
                    throw new IllegalStateException("Broadcast already finished");
                }
            }
        }

        public int getSendingUserId() {
            return this.mSendingUser;
        }

        /* access modifiers changed from: package-private */
        public void checkSynchronousHint() {
            if (!this.mOrderedHint && !this.mInitialStickyHint) {
                RuntimeException e = new RuntimeException("BroadcastReceiver trying to return result during a non-ordered broadcast");
                e.fillInStackTrace();
                Log.e("BroadcastReceiver", e.getMessage(), e);
            }
        }
    }

    public final PendingResult goAsync() {
        PendingResult res = this.mPendingResult;
        this.mPendingResult = null;
        return res;
    }

    public IBinder peekService(Context myContext, Intent service) {
        IActivityManager am = ActivityManager.getService();
        try {
            service.prepareToLeaveProcess(myContext);
            return am.peekService(service, service.resolveTypeIfNeeded(myContext.getContentResolver()), myContext.getOpPackageName());
        } catch (RemoteException e) {
            return null;
        }
    }

    public final void setResultCode(int code) {
        checkSynchronousHint();
        this.mPendingResult.mResultCode = code;
    }

    public final int getResultCode() {
        PendingResult pendingResult = this.mPendingResult;
        if (pendingResult != null) {
            return pendingResult.mResultCode;
        }
        return 0;
    }

    public final void setResultData(String data) {
        checkSynchronousHint();
        this.mPendingResult.mResultData = data;
    }

    public final String getResultData() {
        PendingResult pendingResult = this.mPendingResult;
        if (pendingResult != null) {
            return pendingResult.mResultData;
        }
        return null;
    }

    public final void setResultExtras(Bundle extras) {
        checkSynchronousHint();
        this.mPendingResult.mResultExtras = extras;
    }

    public final Bundle getResultExtras(boolean makeMap) {
        PendingResult pendingResult = this.mPendingResult;
        if (pendingResult == null) {
            return null;
        }
        Bundle e = pendingResult.mResultExtras;
        if (!makeMap || e != null) {
            return e;
        }
        PendingResult pendingResult2 = this.mPendingResult;
        Bundle e2 = new Bundle();
        pendingResult2.mResultExtras = e2;
        return e2;
    }

    public final void setResult(int code, String data, Bundle extras) {
        checkSynchronousHint();
        PendingResult pendingResult = this.mPendingResult;
        pendingResult.mResultCode = code;
        pendingResult.mResultData = data;
        pendingResult.mResultExtras = extras;
    }

    public final boolean getAbortBroadcast() {
        PendingResult pendingResult = this.mPendingResult;
        if (pendingResult != null) {
            return pendingResult.mAbortBroadcast;
        }
        return false;
    }

    public final void abortBroadcast() {
        checkSynchronousHint();
        this.mPendingResult.mAbortBroadcast = true;
    }

    public final void clearAbortBroadcast() {
        PendingResult pendingResult = this.mPendingResult;
        if (pendingResult != null) {
            pendingResult.mAbortBroadcast = false;
        }
    }

    public final boolean isOrderedBroadcast() {
        PendingResult pendingResult = this.mPendingResult;
        if (pendingResult != null) {
            return pendingResult.mOrderedHint;
        }
        return false;
    }

    public final boolean isInitialStickyBroadcast() {
        PendingResult pendingResult = this.mPendingResult;
        if (pendingResult != null) {
            return pendingResult.mInitialStickyHint;
        }
        return false;
    }

    public final void setOrderedHint(boolean isOrdered) {
    }

    @UnsupportedAppUsage
    public final void setPendingResult(PendingResult result) {
        this.mPendingResult = result;
    }

    @UnsupportedAppUsage
    public final PendingResult getPendingResult() {
        return this.mPendingResult;
    }

    public int getSendingUserId() {
        return this.mPendingResult.mSendingUser;
    }

    public final void setDebugUnregister(boolean debug) {
        this.mDebugUnregister = debug;
    }

    public final boolean getDebugUnregister() {
        return this.mDebugUnregister;
    }

    /* access modifiers changed from: package-private */
    public void checkSynchronousHint() {
        PendingResult pendingResult = this.mPendingResult;
        if (pendingResult == null) {
            throw new IllegalStateException("Call while result is not pending");
        } else if (!pendingResult.mOrderedHint && !this.mPendingResult.mInitialStickyHint) {
            RuntimeException e = new RuntimeException("BroadcastReceiver trying to return result during a non-ordered broadcast");
            e.fillInStackTrace();
            Log.e("BroadcastReceiver", e.getMessage(), e);
        }
    }
}
