package com.android.server.devicepolicy;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.app.admin.NetworkEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.LongSparseArray;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import java.util.ArrayList;
import java.util.List;

final class NetworkLoggingHandler extends Handler {
    private static final long BATCH_FINALIZATION_TIMEOUT_ALARM_INTERVAL_MS = 1800000;
    private static final long BATCH_FINALIZATION_TIMEOUT_MS = 5400000;
    static final int LOG_NETWORK_EVENT_MSG = 1;
    private static final int MAX_BATCHES = 5;
    private static final int MAX_EVENTS_PER_BATCH = 1200;
    static final String NETWORK_EVENT_KEY = "network_event";
    private static final String NETWORK_LOGGING_TIMEOUT_ALARM_TAG = "NetworkLogging.batchTimeout";
    private static final long RETRIEVED_BATCH_DISCARD_DELAY_MS = 300000;
    private static final String TAG = NetworkLoggingHandler.class.getSimpleName();
    private final AlarmManager mAlarmManager;
    private final OnAlarmListener mBatchTimeoutAlarmListener = new OnAlarmListener() {
        public void onAlarm() {
            Bundle notificationExtras;
            Slog.d(NetworkLoggingHandler.TAG, "Received a batch finalization timeout alarm, finalizing " + NetworkLoggingHandler.this.mNetworkEvents.size() + " pending events.");
            synchronized (NetworkLoggingHandler.this) {
                notificationExtras = NetworkLoggingHandler.this.finalizeBatchAndBuildDeviceOwnerMessageLocked();
            }
            if (notificationExtras != null) {
                NetworkLoggingHandler.this.notifyDeviceOwner(notificationExtras);
            }
        }
    };
    @GuardedBy("this")
    private final LongSparseArray<ArrayList<NetworkEvent>> mBatches = new LongSparseArray(5);
    @GuardedBy("this")
    private long mCurrentBatchToken;
    private final DevicePolicyManagerService mDpm;
    @GuardedBy("this")
    private long mLastRetrievedBatchToken;
    @GuardedBy("this")
    private ArrayList<NetworkEvent> mNetworkEvents = new ArrayList();
    @GuardedBy("this")
    private boolean mPaused = false;

    NetworkLoggingHandler(Looper looper, DevicePolicyManagerService dpm) {
        super(looper);
        this.mDpm = dpm;
        this.mAlarmManager = this.mDpm.mInjector.getAlarmManager();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                NetworkEvent networkEvent = (NetworkEvent) msg.getData().getParcelable(NETWORK_EVENT_KEY);
                if (networkEvent != null) {
                    Bundle notificationExtras = null;
                    synchronized (this) {
                        this.mNetworkEvents.add(networkEvent);
                        if (this.mNetworkEvents.size() >= MAX_EVENTS_PER_BATCH) {
                            notificationExtras = finalizeBatchAndBuildDeviceOwnerMessageLocked();
                        }
                    }
                    if (notificationExtras != null) {
                        notifyDeviceOwner(notificationExtras);
                        return;
                    }
                    return;
                }
                return;
            default:
                Slog.d(TAG, "NetworkLoggingHandler received an unknown of message.");
                return;
        }
    }

    void scheduleBatchFinalization() {
        this.mAlarmManager.setWindow(2, SystemClock.elapsedRealtime() + BATCH_FINALIZATION_TIMEOUT_MS, 1800000, NETWORK_LOGGING_TIMEOUT_ALARM_TAG, this.mBatchTimeoutAlarmListener, this);
        Slog.d(TAG, "Scheduled a new batch finalization alarm 5400000ms from now.");
    }

    synchronized void pause() {
        Slog.d(TAG, "Paused network logging");
        this.mPaused = true;
    }

    /* JADX WARNING: Missing block: B:15:0x0053, code:
            if (r0 == null) goto L_0x0058;
     */
    /* JADX WARNING: Missing block: B:16:0x0055, code:
            notifyDeviceOwner(r0);
     */
    /* JADX WARNING: Missing block: B:17:0x0058, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void resume() {
        Bundle notificationExtras = null;
        synchronized (this) {
            if (this.mPaused) {
                Slog.d(TAG, "Resumed network logging. Current batch=" + this.mCurrentBatchToken + ", LastRetrievedBatch=" + this.mLastRetrievedBatchToken);
                this.mPaused = false;
                if (this.mBatches.size() > 0 && this.mLastRetrievedBatchToken != this.mCurrentBatchToken) {
                    scheduleBatchFinalization();
                    notificationExtras = buildDeviceOwnerMessageLocked();
                }
            } else {
                Slog.d(TAG, "Attempted to resume network logging, but logging is not paused.");
            }
        }
    }

    synchronized void discardLogs() {
        this.mBatches.clear();
        this.mNetworkEvents = new ArrayList();
        Slog.d(TAG, "Discarded all network logs");
    }

    @GuardedBy("this")
    private Bundle finalizeBatchAndBuildDeviceOwnerMessageLocked() {
        Bundle notificationExtras = null;
        if (this.mNetworkEvents.size() > 0) {
            if (this.mBatches.size() >= 5) {
                this.mBatches.removeAt(0);
            }
            this.mCurrentBatchToken++;
            this.mBatches.append(this.mCurrentBatchToken, this.mNetworkEvents);
            this.mNetworkEvents = new ArrayList();
            if (!this.mPaused) {
                notificationExtras = buildDeviceOwnerMessageLocked();
            }
        } else {
            Slog.d(TAG, "Was about to finalize the batch, but there were no events to send to the DPC, the batchToken of last available batch: " + this.mCurrentBatchToken);
        }
        scheduleBatchFinalization();
        return notificationExtras;
    }

    @GuardedBy("this")
    private Bundle buildDeviceOwnerMessageLocked() {
        Bundle extras = new Bundle();
        int lastBatchSize = ((ArrayList) this.mBatches.valueAt(this.mBatches.size() - 1)).size();
        extras.putLong("android.app.extra.EXTRA_NETWORK_LOGS_TOKEN", this.mCurrentBatchToken);
        extras.putInt("android.app.extra.EXTRA_NETWORK_LOGS_COUNT", lastBatchSize);
        return extras;
    }

    private void notifyDeviceOwner(Bundle extras) {
        Slog.d(TAG, "Sending network logging batch broadcast to device owner, batchToken: " + extras.getLong("android.app.extra.EXTRA_NETWORK_LOGS_TOKEN", -1));
        if (Thread.holdsLock(this)) {
            Slog.wtfStack(TAG, "Shouldn't be called with NetworkLoggingHandler lock held");
        } else {
            this.mDpm.sendDeviceOwnerCommand("android.app.action.NETWORK_LOGS_AVAILABLE", extras);
        }
    }

    synchronized List<NetworkEvent> retrieveFullLogBatch(long batchToken) {
        int index = this.mBatches.indexOfKey(batchToken);
        if (index < 0) {
            return null;
        }
        postDelayed(new -$Lambda$JjZpgBdqG5MUfY-PfR5vrjsPutc(batchToken, this), RETRIEVED_BATCH_DISCARD_DELAY_MS);
        this.mLastRetrievedBatchToken = batchToken;
        return (List) this.mBatches.valueAt(index);
    }

    /* synthetic */ void lambda$-com_android_server_devicepolicy_NetworkLoggingHandler_9940(long batchToken) {
        synchronized (this) {
            while (this.mBatches.size() > 0 && this.mBatches.keyAt(0) <= batchToken) {
                this.mBatches.removeAt(0);
            }
        }
    }
}
