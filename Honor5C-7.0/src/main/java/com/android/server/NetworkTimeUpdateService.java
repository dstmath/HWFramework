package com.android.server;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.util.NtpTrustedTime;
import android.util.TimeUtils;
import android.util.TrustedTime;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class NetworkTimeUpdateService extends Binder {
    private static final String ACTION_POLL = "com.android.server.NetworkTimeUpdateService.action.POLL";
    private static final boolean DBG = false;
    private static final int EVENT_AUTO_TIME_CHANGED = 1;
    private static final int EVENT_NETWORK_CHANGED = 3;
    private static final int EVENT_POLL_NETWORK_TIME = 2;
    private static final int NETWORK_CHANGE_EVENT_DELAY_MS = 1000;
    private static final long NOT_SET = -1;
    private static int POLL_REQUEST = 0;
    private static final String TAG = "NetworkTimeUpdateService";
    private AlarmManager mAlarmManager;
    private BroadcastReceiver mConnectivityReceiver;
    private Context mContext;
    private Handler mHandler;
    private long mLastNtpFetchTime;
    private BroadcastReceiver mNitzReceiver;
    private long mNitzTimeSetTime;
    private long mNitzZoneSetTime;
    private PendingIntent mPendingPollIntent;
    private final long mPollingIntervalMs;
    private final long mPollingIntervalShorterMs;
    private SettingsObserver mSettingsObserver;
    private TrustedTime mTime;
    private final int mTimeErrorThresholdMs;
    private int mTryAgainCounter;
    private final int mTryAgainTimesMax;
    private final WakeLock mWakeLock;

    private class MyHandler extends Handler {
        public MyHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NetworkTimeUpdateService.EVENT_AUTO_TIME_CHANGED /*1*/:
                case NetworkTimeUpdateService.EVENT_POLL_NETWORK_TIME /*2*/:
                case NetworkTimeUpdateService.EVENT_NETWORK_CHANGED /*3*/:
                    NetworkTimeUpdateService.this.onPollNetworkTime(msg.what);
                default:
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

        void observe(Context context) {
            context.getContentResolver().registerContentObserver(Global.getUriFor("auto_time"), NetworkTimeUpdateService.DBG, this);
        }

        public void onChange(boolean selfChange) {
            this.mHandler.obtainMessage(this.mMsg).sendToTarget();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.NetworkTimeUpdateService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.NetworkTimeUpdateService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.NetworkTimeUpdateService.<clinit>():void");
    }

    public NetworkTimeUpdateService(Context context) {
        this.mNitzTimeSetTime = NOT_SET;
        this.mNitzZoneSetTime = NOT_SET;
        this.mLastNtpFetchTime = NOT_SET;
        this.mNitzReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.NETWORK_SET_TIME".equals(action)) {
                    NetworkTimeUpdateService.this.mNitzTimeSetTime = SystemClock.elapsedRealtime();
                } else if ("android.intent.action.NETWORK_SET_TIMEZONE".equals(action)) {
                    NetworkTimeUpdateService.this.mNitzZoneSetTime = SystemClock.elapsedRealtime();
                }
            }
        };
        this.mConnectivityReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                    NetworkTimeUpdateService.this.mHandler.sendMessageDelayed(NetworkTimeUpdateService.this.mHandler.obtainMessage(NetworkTimeUpdateService.EVENT_NETWORK_CHANGED), 1000);
                }
            }
        };
        this.mContext = context;
        this.mTime = NtpTrustedTime.getInstance(context);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mPendingPollIntent = PendingIntent.getBroadcast(this.mContext, POLL_REQUEST, new Intent(ACTION_POLL, null), 0);
        this.mPollingIntervalMs = (long) this.mContext.getResources().getInteger(17694849);
        this.mPollingIntervalShorterMs = (long) this.mContext.getResources().getInteger(17694850);
        this.mTryAgainTimesMax = this.mContext.getResources().getInteger(17694851);
        this.mTimeErrorThresholdMs = this.mContext.getResources().getInteger(17694852);
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(EVENT_AUTO_TIME_CHANGED, TAG);
    }

    public void systemRunning() {
        registerForTelephonyIntents();
        registerForAlarms();
        registerForConnectivityIntents();
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new MyHandler(thread.getLooper());
        this.mHandler.obtainMessage(EVENT_POLL_NETWORK_TIME).sendToTarget();
        this.mSettingsObserver = new SettingsObserver(this.mHandler, EVENT_AUTO_TIME_CHANGED);
        this.mSettingsObserver.observe(this.mContext);
    }

    private void registerForTelephonyIntents() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.NETWORK_SET_TIME");
        intentFilter.addAction("android.intent.action.NETWORK_SET_TIMEZONE");
        this.mContext.registerReceiver(this.mNitzReceiver, intentFilter);
    }

    private void registerForAlarms() {
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkTimeUpdateService.this.mHandler.obtainMessage(NetworkTimeUpdateService.EVENT_POLL_NETWORK_TIME).sendToTarget();
            }
        }, new IntentFilter(ACTION_POLL));
    }

    private void registerForConnectivityIntents() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mContext.registerReceiver(this.mConnectivityReceiver, intentFilter);
    }

    private void onPollNetworkTime(int event) {
        if (isAutomaticTimeRequested()) {
            this.mWakeLock.acquire();
            try {
                onPollNetworkTimeUnderWakeLock(event);
            } finally {
                this.mWakeLock.release();
            }
        }
    }

    private void onPollNetworkTimeUnderWakeLock(int event) {
        long refTime = SystemClock.elapsedRealtime();
        if (this.mNitzTimeSetTime == NOT_SET || refTime - this.mNitzTimeSetTime >= this.mPollingIntervalMs) {
            long currentTime = System.currentTimeMillis();
            if (this.mLastNtpFetchTime != NOT_SET && refTime < this.mLastNtpFetchTime + this.mPollingIntervalMs) {
                if (event == EVENT_AUTO_TIME_CHANGED) {
                }
                resetAlarm(this.mPollingIntervalMs);
                return;
            }
            if (this.mTime.getCacheAge() >= this.mPollingIntervalMs) {
                this.mTime.forceRefresh();
            }
            if (this.mTime.getCacheAge() < this.mPollingIntervalMs) {
                long ntp = this.mTime.currentTimeMillis();
                this.mTryAgainCounter = 0;
                if ((Math.abs(ntp - currentTime) > ((long) this.mTimeErrorThresholdMs) || this.mLastNtpFetchTime == NOT_SET) && ntp / 1000 < 2147483647L) {
                    SystemClock.setCurrentTimeMillis(ntp);
                }
                this.mLastNtpFetchTime = SystemClock.elapsedRealtime();
                resetAlarm(this.mPollingIntervalMs);
                return;
            }
            this.mTryAgainCounter += EVENT_AUTO_TIME_CHANGED;
            if (this.mTryAgainTimesMax < 0 || this.mTryAgainCounter <= this.mTryAgainTimesMax) {
                resetAlarm(this.mPollingIntervalShorterMs);
            } else {
                this.mTryAgainCounter = 0;
                resetAlarm(this.mPollingIntervalMs);
            }
            return;
        }
        resetAlarm(this.mPollingIntervalMs);
    }

    private void resetAlarm(long interval) {
        this.mAlarmManager.cancel(this.mPendingPollIntent);
        this.mAlarmManager.set(EVENT_NETWORK_CHANGED, SystemClock.elapsedRealtime() + interval, this.mPendingPollIntent);
    }

    private boolean isAutomaticTimeRequested() {
        if (Global.getInt(this.mContext.getContentResolver(), "auto_time", 0) != 0) {
            return true;
        }
        return DBG;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump NetworkTimeUpdateService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
        pw.print("PollingIntervalMs: ");
        TimeUtils.formatDuration(this.mPollingIntervalMs, pw);
        pw.print("\nPollingIntervalShorterMs: ");
        TimeUtils.formatDuration(this.mPollingIntervalShorterMs, pw);
        pw.println("\nTryAgainTimesMax: " + this.mTryAgainTimesMax);
        pw.print("TimeErrorThresholdMs: ");
        TimeUtils.formatDuration((long) this.mTimeErrorThresholdMs, pw);
        pw.println("\nTryAgainCounter: " + this.mTryAgainCounter);
        pw.print("LastNtpFetchTime: ");
        TimeUtils.formatDuration(this.mLastNtpFetchTime, pw);
        pw.println();
    }
}
