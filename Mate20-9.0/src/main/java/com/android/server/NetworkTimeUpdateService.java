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
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.util.NtpTrustedTime;
import android.util.TimeUtils;
import com.android.internal.util.DumpUtils;
import com.android.server.job.controllers.JobStatus;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class NetworkTimeUpdateService extends Binder {
    private static final String ACTION_POLL = "com.android.server.NetworkTimeUpdateService.action.POLL";
    private static final boolean DBG = false;
    private static final int EVENT_AUTO_TIME_CHANGED = 1;
    private static final int EVENT_NETWORK_CHANGED = 3;
    private static final int EVENT_POLL_NETWORK_TIME = 2;
    private static final long NOT_SET = -1;
    private static final long POLL_NETWORK_TIME_INTERVAL = 1800000;
    private static final int POLL_REQUEST = 0;
    private static final String TAG = "NetworkTimeUpdateService";
    private final AlarmManager mAlarmManager;
    private final ConnectivityManager mCM;
    private final Context mContext;
    /* access modifiers changed from: private */
    public Network mDefaultNetwork = null;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private NetworkTimeUpdateCallback mNetworkTimeUpdateCallback;
    private BroadcastReceiver mNitzReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.NETWORK_SET_TIME".equals(intent.getAction())) {
                long unused = NetworkTimeUpdateService.this.mNitzTimeSetTime = SystemClock.elapsedRealtime();
            }
        }
    };
    /* access modifiers changed from: private */
    public long mNitzTimeSetTime = -1;
    private final PendingIntent mPendingPollIntent;
    private final long mPollingIntervalMs;
    private final long mPollingIntervalShorterMs;
    private SettingsObserver mSettingsObserver;
    private final NtpTrustedTime mTime;
    private final int mTimeErrorThresholdMs;
    private int mTryAgainCounter;
    private final int mTryAgainTimesMax;
    private final PowerManager.WakeLock mWakeLock;

    private class MyHandler extends Handler {
        public MyHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            Log.d(NetworkTimeUpdateService.TAG, "msg.what " + msg.what);
            switch (msg.what) {
                case 1:
                case 2:
                case 3:
                    NetworkTimeUpdateService.this.onPollNetworkTime(msg.what);
                    return;
                default:
                    return;
            }
        }
    }

    private class NetworkTimeUpdateCallback extends ConnectivityManager.NetworkCallback {
        private NetworkTimeUpdateCallback() {
        }

        public void onAvailable(Network network) {
            Log.d(NetworkTimeUpdateService.TAG, String.format("New default network %s; checking time.", new Object[]{network}));
            Network unused = NetworkTimeUpdateService.this.mDefaultNetwork = network;
            NetworkTimeUpdateService.this.onPollNetworkTime(3);
        }

        public void onLost(Network network) {
            if (network.equals(NetworkTimeUpdateService.this.mDefaultNetwork)) {
                Network unused = NetworkTimeUpdateService.this.mDefaultNetwork = null;
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

        public void onChange(boolean selfChange) {
            this.mHandler.obtainMessage(this.mMsg).sendToTarget();
        }
    }

    public NetworkTimeUpdateService(Context context) {
        this.mContext = context;
        this.mTime = NtpTrustedTime.getInstance(context);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        this.mCM = (ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class);
        this.mPendingPollIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_POLL, null), 0);
        this.mPollingIntervalMs = (long) this.mContext.getResources().getInteger(17694842);
        this.mPollingIntervalShorterMs = (long) this.mContext.getResources().getInteger(17694843);
        this.mTryAgainTimesMax = this.mContext.getResources().getInteger(17694844);
        this.mTimeErrorThresholdMs = this.mContext.getResources().getInteger(17694845);
        this.mWakeLock = ((PowerManager) context.getSystemService(PowerManager.class)).newWakeLock(1, TAG);
    }

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
            public void onReceive(Context context, Intent intent) {
                NetworkTimeUpdateService.this.mHandler.obtainMessage(2).sendToTarget();
            }
        }, new IntentFilter(ACTION_POLL));
    }

    /* access modifiers changed from: private */
    public void onPollNetworkTime(int event) {
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
            this.mTime.forceRefresh();
        }
        if (this.mTime.getCacheAge() < this.mPollingIntervalMs) {
            resetAlarm(this.mPollingIntervalMs);
            if (isAutomaticTimeRequested()) {
                updateSystemClock(event);
                return;
            }
            return;
        }
        this.mTryAgainCounter++;
        if (this.mTryAgainTimesMax < 0 || this.mTryAgainCounter <= this.mTryAgainTimesMax) {
            resetAlarm(1800000 * this.mPollingIntervalShorterMs);
            return;
        }
        this.mTryAgainCounter = 0;
        resetAlarm(this.mPollingIntervalMs);
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

    /* access modifiers changed from: protected */
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
