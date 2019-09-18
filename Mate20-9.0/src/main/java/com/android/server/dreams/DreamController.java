package com.android.server.dreams;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.service.dreams.IDreamService;
import android.util.Slog;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.logging.MetricsLogger;
import com.android.server.NetworkManagementService;
import com.android.server.dreams.DreamController;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.DumpState;
import com.android.server.policy.PhoneWindowManager;
import java.io.PrintWriter;
import java.util.NoSuchElementException;

final class DreamController {
    private static final int DREAM_CONNECTION_TIMEOUT = 5000;
    private static final int DREAM_FINISH_TIMEOUT = 5000;
    private static final String TAG = "DreamController";
    private final Intent mCloseNotificationShadeIntent;
    private final Context mContext;
    /* access modifiers changed from: private */
    public DreamRecord mCurrentDream;
    private long mDreamStartTime;
    private final Intent mDreamingStartedIntent = new Intent("android.intent.action.DREAMING_STARTED").addFlags(1073741824);
    private final Intent mDreamingStoppedIntent = new Intent("android.intent.action.DREAMING_STOPPED").addFlags(1073741824);
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final IWindowManager mIWindowManager;
    /* access modifiers changed from: private */
    public final Listener mListener;
    private final Runnable mStopStubbornDreamRunnable = new Runnable() {
        public void run() {
            Slog.w(DreamController.TAG, "Stubborn dream did not finish itself in the time allotted");
            DreamController.this.stopDream(true);
        }
    };
    private final Runnable mStopUnconnectedDreamRunnable = new Runnable() {
        public void run() {
            if (DreamController.this.mCurrentDream != null && DreamController.this.mCurrentDream.mBound && !DreamController.this.mCurrentDream.mConnected) {
                Slog.w(DreamController.TAG, "Bound dream did not connect in the time allotted");
                DreamController.this.stopDream(true);
            }
        }
    };

    private final class DreamRecord implements IBinder.DeathRecipient, ServiceConnection {
        public boolean mBound;
        public final boolean mCanDoze;
        public boolean mConnected;
        final IRemoteCallback mDreamingStartedCallback = new IRemoteCallback.Stub() {
            public void sendResult(Bundle data) throws RemoteException {
                DreamController.this.mHandler.post(DreamRecord.this.mReleaseWakeLockIfNeeded);
            }
        };
        public final boolean mIsTest;
        public final ComponentName mName;
        final Runnable mReleaseWakeLockIfNeeded = new Runnable() {
            public final void run() {
                DreamController.DreamRecord.this.releaseWakeLockIfNeeded();
            }
        };
        public boolean mSentStartBroadcast;
        public IDreamService mService;
        public final Binder mToken;
        public final int mUserId;
        public PowerManager.WakeLock mWakeLock;
        public boolean mWakingGently;

        public DreamRecord(Binder token, ComponentName name, boolean isTest, boolean canDoze, int userId, PowerManager.WakeLock wakeLock) {
            this.mToken = token;
            this.mName = name;
            this.mIsTest = isTest;
            this.mCanDoze = canDoze;
            this.mUserId = userId;
            this.mWakeLock = wakeLock;
            this.mWakeLock.acquire();
            DreamController.this.mHandler.postDelayed(this.mReleaseWakeLockIfNeeded, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        }

        public void binderDied() {
            DreamController.this.mHandler.post(new Runnable() {
                public void run() {
                    DreamRecord.this.mService = null;
                    if (DreamController.this.mCurrentDream == DreamRecord.this) {
                        DreamController.this.stopDream(true);
                    }
                }
            });
        }

        public void onServiceConnected(ComponentName name, final IBinder service) {
            DreamController.this.mHandler.post(new Runnable() {
                public void run() {
                    DreamRecord.this.mConnected = true;
                    if (DreamController.this.mCurrentDream == DreamRecord.this && DreamRecord.this.mService == null) {
                        DreamController.this.attach(IDreamService.Stub.asInterface(service));
                    } else {
                        DreamRecord.this.releaseWakeLockIfNeeded();
                    }
                }
            });
        }

        public void onServiceDisconnected(ComponentName name) {
            DreamController.this.mHandler.post(new Runnable() {
                public void run() {
                    DreamRecord.this.mService = null;
                    if (DreamController.this.mCurrentDream == DreamRecord.this) {
                        DreamController.this.stopDream(true);
                    }
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void releaseWakeLockIfNeeded() {
            if (this.mWakeLock != null) {
                this.mWakeLock.release();
                this.mWakeLock = null;
                DreamController.this.mHandler.removeCallbacks(this.mReleaseWakeLockIfNeeded);
            }
        }
    }

    public interface Listener {
        void onDreamStopped(Binder binder);
    }

    public DreamController(Context context, Handler handler, Listener listener) {
        this.mContext = context;
        this.mHandler = handler;
        this.mListener = listener;
        this.mIWindowManager = WindowManagerGlobal.getWindowManagerService();
        this.mCloseNotificationShadeIntent = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        this.mCloseNotificationShadeIntent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, "dream");
    }

    public void dump(PrintWriter pw) {
        pw.println("Dreamland:");
        if (this.mCurrentDream != null) {
            pw.println("  mCurrentDream:");
            pw.println("    mToken=" + this.mCurrentDream.mToken);
            pw.println("    mName=" + this.mCurrentDream.mName);
            pw.println("    mIsTest=" + this.mCurrentDream.mIsTest);
            pw.println("    mCanDoze=" + this.mCurrentDream.mCanDoze);
            pw.println("    mUserId=" + this.mCurrentDream.mUserId);
            pw.println("    mBound=" + this.mCurrentDream.mBound);
            pw.println("    mService=" + this.mCurrentDream.mService);
            pw.println("    mSentStartBroadcast=" + this.mCurrentDream.mSentStartBroadcast);
            pw.println("    mWakingGently=" + this.mCurrentDream.mWakingGently);
            return;
        }
        pw.println("  mCurrentDream: null");
    }

    public void startDream(Binder token, ComponentName name, boolean isTest, boolean canDoze, int userId, PowerManager.WakeLock wakeLock) {
        Intent intent;
        ComponentName componentName = name;
        int i = userId;
        stopDream(true);
        Trace.traceBegin(131072, "startDream");
        try {
            this.mContext.sendBroadcastAsUser(this.mCloseNotificationShadeIntent, UserHandle.ALL);
            StringBuilder sb = new StringBuilder();
            sb.append("Starting dream: name=");
            sb.append(componentName);
            sb.append(", isTest=");
            boolean z = isTest;
            try {
                sb.append(z);
                sb.append(", canDoze=");
                boolean z2 = canDoze;
                sb.append(z2);
                sb.append(", userId=");
                sb.append(i);
                Slog.i(TAG, sb.toString());
                DreamRecord dreamRecord = new DreamRecord(token, componentName, z, z2, i, wakeLock);
                this.mCurrentDream = dreamRecord;
                this.mDreamStartTime = SystemClock.elapsedRealtime();
                MetricsLogger.visible(this.mContext, this.mCurrentDream.mCanDoze ? NetworkManagementService.NetdResponseCode.ClatdStatusResult : NetworkManagementService.NetdResponseCode.DnsProxyQueryResult);
                try {
                    try {
                        this.mIWindowManager.addWindowToken(token, 2023, 0);
                    } catch (RemoteException e) {
                        ex = e;
                    }
                } catch (RemoteException e2) {
                    ex = e2;
                    Binder binder = token;
                    Slog.e(TAG, "Unable to add window token for dream.", ex);
                    stopDream(true);
                    Trace.traceEnd(131072);
                    return;
                }
            } catch (Throwable th) {
                ex = th;
                Binder binder2 = token;
                Trace.traceEnd(131072);
                throw ex;
            }
            try {
                intent = new Intent("android.service.dreams.DreamService");
                intent.setComponent(componentName);
                intent.addFlags(DumpState.DUMP_VOLUMES);
                if (!this.mContext.bindServiceAsUser(intent, this.mCurrentDream, 67108865, new UserHandle(i))) {
                    Slog.e(TAG, "Unable to bind dream service: " + intent);
                    stopDream(true);
                    Trace.traceEnd(131072);
                    return;
                }
                this.mCurrentDream.mBound = true;
                this.mHandler.postDelayed(this.mStopUnconnectedDreamRunnable, 5000);
                Trace.traceEnd(131072);
            } catch (SecurityException ex) {
                Slog.e(TAG, "Unable to bind dream service: " + intent, ex);
                stopDream(true);
                Trace.traceEnd(131072);
            } catch (Throwable th2) {
                ex = th2;
                Trace.traceEnd(131072);
                throw ex;
            }
        } catch (Throwable th3) {
            ex = th3;
            Binder binder3 = token;
            boolean z3 = isTest;
            Trace.traceEnd(131072);
            throw ex;
        }
    }

    public void stopDream(boolean immediate) {
        final DreamRecord oldDream;
        if (this.mCurrentDream != null) {
            Trace.traceBegin(131072, "stopDream");
            if (!immediate) {
                if (this.mCurrentDream.mWakingGently) {
                    Trace.traceEnd(131072);
                    return;
                } else if (this.mCurrentDream.mService != null) {
                    this.mCurrentDream.mWakingGently = true;
                    try {
                        this.mCurrentDream.mService.wakeUp();
                        this.mHandler.postDelayed(this.mStopStubbornDreamRunnable, 5000);
                        Trace.traceEnd(131072);
                        return;
                    } catch (RemoteException e) {
                    }
                }
            }
            try {
                oldDream = this.mCurrentDream;
                this.mCurrentDream = null;
                Slog.i(TAG, "Stopping dream: name=" + oldDream.mName + ", isTest=" + oldDream.mIsTest + ", canDoze=" + oldDream.mCanDoze + ", userId=" + oldDream.mUserId);
                MetricsLogger.hidden(this.mContext, oldDream.mCanDoze ? NetworkManagementService.NetdResponseCode.ClatdStatusResult : NetworkManagementService.NetdResponseCode.DnsProxyQueryResult);
                MetricsLogger.histogram(this.mContext, oldDream.mCanDoze ? "dozing_minutes" : "dreaming_minutes", (int) ((SystemClock.elapsedRealtime() - this.mDreamStartTime) / 60000));
                this.mHandler.removeCallbacks(this.mStopUnconnectedDreamRunnable);
                this.mHandler.removeCallbacks(this.mStopStubbornDreamRunnable);
                if (oldDream.mSentStartBroadcast) {
                    this.mContext.sendBroadcastAsUser(this.mDreamingStoppedIntent, UserHandle.ALL);
                }
                if (oldDream.mService != null) {
                    try {
                        oldDream.mService.detach();
                    } catch (RemoteException e2) {
                    }
                    try {
                        oldDream.mService.asBinder().unlinkToDeath(oldDream, 0);
                    } catch (NoSuchElementException e3) {
                    }
                    oldDream.mService = null;
                }
                if (oldDream.mBound) {
                    this.mContext.unbindService(oldDream);
                }
                oldDream.releaseWakeLockIfNeeded();
                this.mIWindowManager.removeWindowToken(oldDream.mToken, 0);
            } catch (RemoteException ex) {
                Slog.w(TAG, "Error removing window token for dream.", ex);
            } catch (Throwable th) {
                Trace.traceEnd(131072);
                throw th;
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    DreamController.this.mListener.onDreamStopped(oldDream.mToken);
                }
            });
            Trace.traceEnd(131072);
        }
    }

    /* access modifiers changed from: private */
    public void attach(IDreamService service) {
        try {
            service.asBinder().linkToDeath(this.mCurrentDream, 0);
            service.attach(this.mCurrentDream.mToken, this.mCurrentDream.mCanDoze, this.mCurrentDream.mDreamingStartedCallback);
            this.mCurrentDream.mService = service;
            if (!this.mCurrentDream.mIsTest) {
                this.mContext.sendBroadcastAsUser(this.mDreamingStartedIntent, UserHandle.ALL);
                this.mCurrentDream.mSentStartBroadcast = true;
            }
        } catch (RemoteException ex) {
            Slog.e(TAG, "The dream service died unexpectedly.", ex);
            stopDream(true);
        }
    }
}
