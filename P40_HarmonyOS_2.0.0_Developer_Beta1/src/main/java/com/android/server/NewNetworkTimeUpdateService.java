package com.android.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.util.NtpTrustedTime;
import android.util.TimeUtils;
import com.android.internal.util.DumpUtils;
import com.android.server.BatteryService;
import com.android.server.job.controllers.JobStatus;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class NewNetworkTimeUpdateService extends Binder implements NetworkTimeUpdateService {
    private static final String ACTION_POLL = "com.android.server.NetworkTimeUpdateService.action.POLL";
    private static final boolean DBG = false;
    private static final int EVENT_AUTO_TIME_CHANGED = 1;
    private static final int EVENT_NETWORK_CHANGED = 3;
    private static final int EVENT_POLL_NETWORK_TIME = 2;
    private static final long NOT_SET = -1;
    private static final int POLL_REQUEST = 0;
    private static final int RETRY_TIMER_FOR_TV = 20000;
    private static final String TAG = "NetworkTimeUpdateService";
    private static boolean isTv = "tv".equals(SystemProperties.get("ro.build.characteristics", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR));
    private final AlarmManager mAlarmManager;
    private final ConnectivityManager mCM;
    private final Context mContext;
    private Network mDefaultNetwork = null;
    private Handler mHandler;
    private NetworkTimeUpdateCallback mNetworkTimeUpdateCallback;
    private BroadcastReceiver mNitzReceiver = new BroadcastReceiver() {
        /* class com.android.server.NewNetworkTimeUpdateService.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.NETWORK_SET_TIME".equals(intent.getAction())) {
                NewNetworkTimeUpdateService.this.mNitzTimeSetTime = SystemClock.elapsedRealtime();
            }
        }
    };
    private long mNitzTimeSetTime = -1;
    private final PendingIntent mPendingPollIntent;
    private final long mPollingIntervalMs;
    private final long mPollingIntervalShorterMs;
    private SettingsObserver mSettingsObserver;
    private final NtpTrustedTime mTime;
    private final int mTimeErrorThresholdMs;
    private int mTryAgainCounter;
    private final int mTryAgainTimesMax;
    private final PowerManager.WakeLock mWakeLock;

    public NewNetworkTimeUpdateService(Context context) {
        this.mContext = context;
        this.mTime = NtpTrustedTime.getInstance(context);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        this.mCM = (ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class);
        this.mPendingPollIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_POLL, (Uri) null), 0);
        this.mPollingIntervalMs = (long) this.mContext.getResources().getInteger(17694868);
        this.mPollingIntervalShorterMs = (long) this.mContext.getResources().getInteger(17694869);
        this.mTryAgainTimesMax = this.mContext.getResources().getInteger(17694870);
        this.mTimeErrorThresholdMs = this.mContext.getResources().getInteger(17694871);
        this.mWakeLock = ((PowerManager) context.getSystemService(PowerManager.class)).newWakeLock(1, TAG);
    }

    @Override // com.android.server.NetworkTimeUpdateService
    public void systemRunning() {
        registerForTelephonyIntents();
        registerForAlarms();
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new MyHandler(thread.getLooper());
        this.mNetworkTimeUpdateCallback = new NetworkTimeUpdateCallback();
        this.mCM.registerDefaultNetworkCallback(this.mNetworkTimeUpdateCallback, this.mHandler);
        this.mSettingsObserver = new SettingsObserver(this.mHandler, 1);
        this.mSettingsObserver.observe(this.mContext);
    }

    private void registerForTelephonyIntents() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.NETWORK_SET_TIME");
        this.mContext.registerReceiver(this.mNitzReceiver, intentFilter);
    }

    private void registerForAlarms() {
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.NewNetworkTimeUpdateService.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                NewNetworkTimeUpdateService.this.mHandler.obtainMessage(2).sendToTarget();
            }
        }, new IntentFilter(ACTION_POLL));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPollNetworkTime(int event) {
        if (this.mDefaultNetwork != null) {
            this.mWakeLock.acquire();
            try {
                onPollNetworkTimeUnderWakeLock(event);
            } finally {
                this.mWakeLock.release();
            }
        }
    }

    private void onPollNetworkTimeUnderWakeLock(int event) {
        if (this.mTime.getCacheAge() >= this.mPollingIntervalMs) {
            Log.i(TAG, "Stale NTP fix; forcing refresh");
            this.mTime.forceRefresh();
        }
        long cacheAge = this.mTime.getCacheAge();
        long j = this.mPollingIntervalMs;
        if (cacheAge < j) {
            resetAlarm(j);
            if (isAutomaticTimeRequested()) {
                updateSystemClock(event);
                return;
            }
            return;
        }
        this.mTryAgainCounter++;
        int i = this.mTryAgainTimesMax;
        if (i >= 0 && this.mTryAgainCounter > i) {
            this.mTryAgainCounter = 0;
            resetAlarm(j);
        } else if (isTv) {
            this.mTryAgainCounter = 0;
            resetAlarm(20000);
        } else {
            resetAlarm(this.mPollingIntervalShorterMs);
        }
    }

    private long getNitzAge() {
        if (this.mNitzTimeSetTime == -1) {
            return JobStatus.NO_LATEST_RUNTIME;
        }
        return SystemClock.elapsedRealtime() - this.mNitzTimeSetTime;
    }

    private void updateSystemClock(int event) {
        boolean forceUpdate = true;
        if (event != 1) {
            forceUpdate = false;
        }
        if (forceUpdate || (getNitzAge() >= this.mPollingIntervalMs && Math.abs(this.mTime.currentTimeMillis() - System.currentTimeMillis()) >= ((long) this.mTimeErrorThresholdMs))) {
            SystemClock.setCurrentTimeMillis(this.mTime.currentTimeMillis());
        }
    }

    private void resetAlarm(long interval) {
        this.mAlarmManager.cancel(this.mPendingPollIntent);
        this.mAlarmManager.set(3, SystemClock.elapsedRealtime() + interval, this.mPendingPollIntent);
    }

    private boolean isAutomaticTimeRequested() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "auto_time", 0) != 0;
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper l) {
            super(l);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1 || i == 2 || i == 3) {
                NewNetworkTimeUpdateService.this.onPollNetworkTime(msg.what);
            }
        }
    }

    private class NetworkTimeUpdateCallback extends ConnectivityManager.NetworkCallback {
        private NetworkTimeUpdateCallback() {
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            Log.i(NewNetworkTimeUpdateService.TAG, String.format("New default network %s; checking time.", network));
            NewNetworkTimeUpdateService.this.mDefaultNetwork = network;
            NewNetworkTimeUpdateService.this.onPollNetworkTime(3);
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            if (network.equals(NewNetworkTimeUpdateService.this.mDefaultNetwork)) {
                NewNetworkTimeUpdateService.this.mDefaultNetwork = null;
            }
        }
    }

    private static class SettingsObserver extends ContentObserver {
        private Handler mHandler;
        private int mMsg;

        SettingsObserver(Handler handler, int msg) {
            super(handler);
            this.mHandler = handler;
            this.mMsg = msg;
        }

        /* access modifiers changed from: package-private */
        public void observe(Context context) {
            context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("auto_time"), false, this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            this.mHandler.obtainMessage(this.mMsg).sendToTarget();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.os.Binder
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.print("PollingIntervalMs: ");
            TimeUtils.formatDuration(this.mPollingIntervalMs, pw);
            pw.print("\nPollingIntervalShorterMs: ");
            TimeUtils.formatDuration(this.mPollingIntervalShorterMs, pw);
            pw.println("\nTryAgainTimesMax: " + this.mTryAgainTimesMax);
            pw.print("TimeErrorThresholdMs: ");
            TimeUtils.formatDuration((long) this.mTimeErrorThresholdMs, pw);
            pw.println("\nTryAgainCounter: " + this.mTryAgainCounter);
            pw.println("NTP cache age: " + this.mTime.getCacheAge());
            pw.println("NTP cache certainty: " + this.mTime.getCacheCertainty());
            pw.println();
        }
    }
}
