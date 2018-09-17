package com.android.server.dreams;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.service.dreams.IDreamService;
import android.service.dreams.IDreamService.Stub;
import android.util.Slog;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.logging.MetricsLogger;
import com.android.server.policy.PhoneWindowManager;
import com.huawei.cust.HwCustUtils;
import java.io.PrintWriter;
import java.util.NoSuchElementException;

final class DreamController {
    private static final int DREAM_CONNECTION_TIMEOUT = 5000;
    private static final int DREAM_FINISH_TIMEOUT = 5000;
    private static final String TAG = "DreamController";
    private final Intent mCloseNotificationShadeIntent;
    private final Context mContext;
    DreamRecord mCurrentDream;
    private HwCustDreamController mCust;
    private long mDreamStartTime;
    private final Intent mDreamingStartedIntent;
    private final Intent mDreamingStoppedIntent;
    private final Handler mHandler;
    private final IWindowManager mIWindowManager;
    private final Listener mListener;
    private final Runnable mStopStubbornDreamRunnable;
    final Runnable mStopUnconnectedDreamRunnable;

    /* renamed from: com.android.server.dreams.DreamController.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ DreamRecord val$oldDream;

        AnonymousClass3(DreamRecord val$oldDream) {
            this.val$oldDream = val$oldDream;
        }

        public void run() {
            DreamController.this.mListener.onDreamStopped(this.val$oldDream.mToken);
        }
    }

    final class DreamRecord implements DeathRecipient, ServiceConnection {
        public boolean mBound;
        public final boolean mCanDoze;
        public boolean mConnected;
        public final boolean mIsTest;
        public final ComponentName mName;
        public boolean mSentStartBroadcast;
        public IDreamService mService;
        public final Binder mToken;
        public final int mUserId;
        public boolean mWakingGently;

        /* renamed from: com.android.server.dreams.DreamController.DreamRecord.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ IBinder val$service;

            AnonymousClass2(IBinder val$service) {
                this.val$service = val$service;
            }

            public void run() {
                DreamRecord.this.mConnected = true;
                if (DreamController.this.mCurrentDream == DreamRecord.this && DreamRecord.this.mService == null) {
                    DreamController.this.attach(Stub.asInterface(this.val$service));
                }
            }
        }

        public DreamRecord(Binder token, ComponentName name, boolean isTest, boolean canDoze, int userId) {
            this.mToken = token;
            this.mName = name;
            this.mIsTest = isTest;
            this.mCanDoze = canDoze;
            this.mUserId = userId;
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

        public void onServiceConnected(ComponentName name, IBinder service) {
            DreamController.this.mHandler.post(new AnonymousClass2(service));
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
    }

    public interface Listener {
        void onDreamStopped(Binder binder);
    }

    public HwCustDreamController getCust() {
        return this.mCust;
    }

    public DreamController(Context context, Handler handler, Listener listener) {
        this.mDreamingStartedIntent = new Intent("android.intent.action.DREAMING_STARTED").addFlags(1073741824);
        this.mDreamingStoppedIntent = new Intent("android.intent.action.DREAMING_STOPPED").addFlags(1073741824);
        this.mCust = null;
        this.mStopUnconnectedDreamRunnable = new Runnable() {
            public void run() {
                if (DreamController.this.mCurrentDream != null && DreamController.this.mCurrentDream.mBound && !DreamController.this.mCurrentDream.mConnected) {
                    Slog.w(DreamController.TAG, "Bound dream did not connect in the time allotted");
                    DreamController.this.stopDream(true);
                }
            }
        };
        this.mStopStubbornDreamRunnable = new Runnable() {
            public void run() {
                Slog.w(DreamController.TAG, "Stubborn dream did not finish itself in the time allotted");
                DreamController.this.stopDream(true);
            }
        };
        this.mContext = context;
        this.mHandler = handler;
        this.mListener = listener;
        this.mIWindowManager = WindowManagerGlobal.getWindowManagerService();
        this.mCloseNotificationShadeIntent = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        this.mCloseNotificationShadeIntent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, "dream");
        this.mCust = (HwCustDreamController) HwCustUtils.createObj(HwCustDreamController.class, new Object[]{this, this.mContext, this.mHandler});
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

    public void startDream(Binder token, ComponentName name, boolean isTest, boolean canDoze, int userId) {
        stopDream(true);
        Trace.traceBegin(131072, "startDream");
        try {
            this.mContext.sendBroadcastAsUser(this.mCloseNotificationShadeIntent, UserHandle.ALL);
            Slog.i(TAG, "Starting dream: name=" + name + ", isTest=" + isTest + ", canDoze=" + canDoze + ", userId=" + userId);
            this.mCurrentDream = new DreamRecord(token, name, isTest, canDoze, userId);
            this.mDreamStartTime = SystemClock.elapsedRealtime();
            MetricsLogger.visible(this.mContext, this.mCurrentDream.mCanDoze ? NetdResponseCode.ClatdStatusResult : NetdResponseCode.DnsProxyQueryResult);
            this.mIWindowManager.addWindowToken(token, 2023);
            Intent intent = new Intent("android.service.dreams.DreamService");
            intent.setComponent(name);
            intent.addFlags(8388608);
            try {
                if (this.mContext.bindServiceAsUser(intent, this.mCurrentDream, 67108865, new UserHandle(userId))) {
                    this.mCurrentDream.mBound = true;
                    this.mHandler.postDelayed(this.mStopUnconnectedDreamRunnable, 5000);
                    Trace.traceEnd(131072);
                    return;
                }
                Slog.e(TAG, "Unable to bind dream service: " + intent);
                stopDream(true);
            } catch (SecurityException ex) {
                Slog.e(TAG, "Unable to bind dream service: " + intent, ex);
                stopDream(true);
                Trace.traceEnd(131072);
            }
        } catch (RemoteException ex2) {
            Slog.e(TAG, "Unable to add window token for dream.", ex2);
            stopDream(true);
        } finally {
            Trace.traceEnd(131072);
        }
    }

    public void stopDream(boolean immediate) {
        if (this.mCurrentDream != null) {
            DreamRecord oldDream;
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
                MetricsLogger.hidden(this.mContext, oldDream.mCanDoze ? NetdResponseCode.ClatdStatusResult : NetdResponseCode.DnsProxyQueryResult);
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
                this.mIWindowManager.removeWindowToken(oldDream.mToken);
            } catch (RemoteException ex) {
                Slog.w(TAG, "Error removing window token for dream.", ex);
            } catch (Throwable th) {
                Trace.traceEnd(131072);
            }
            this.mHandler.post(new AnonymousClass3(oldDream));
            Trace.traceEnd(131072);
        }
    }

    private void attach(IDreamService service) {
        try {
            service.asBinder().linkToDeath(this.mCurrentDream, 0);
            service.attach(this.mCurrentDream.mToken, this.mCurrentDream.mCanDoze);
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
