package com.android.server.content;

import android.accounts.Account;
import android.accounts.AccountAndUser;
import android.accounts.AccountManager;
import android.accounts.AccountManagerInternal;
import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ISyncAdapter;
import android.content.ISyncContext.Stub;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.PeriodicSync;
import android.content.ServiceConnection;
import android.content.SyncActivityTooManyDeletes;
import android.content.SyncAdapterType;
import android.content.SyncAdaptersCache;
import android.content.SyncInfo;
import android.content.SyncResult;
import android.content.SyncStats;
import android.content.SyncStatusInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ProviderInfo;
import android.content.pm.RegisteredServicesCache.ServiceInfo;
import android.content.pm.RegisteredServicesCacheListener;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.BitmapFactory;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.EventLog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import com.android.internal.app.IBatteryStats;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.accounts.AccountManagerService;
import com.android.server.backup.AccountSyncSettingsBackupHelper;
import com.android.server.content.SyncStorageEngine.AuthorityInfo;
import com.android.server.content.SyncStorageEngine.DayStats;
import com.android.server.content.SyncStorageEngine.EndPoint;
import com.android.server.content.SyncStorageEngine.SyncHistoryItem;
import com.android.server.job.JobSchedulerInternal;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class SyncManager extends AbsSyncManager {
    private static final boolean DEBUG_ACCOUNT_ACCESS = false;
    private static final long DEFAULT_MAX_SYNC_RETRY_TIME_IN_SECONDS = 3600;
    private static final int DELAY_RETRY_SYNC_IN_PROGRESS_IN_SECONDS = 10;
    private static final String HANDLE_SYNC_ALARM_WAKE_LOCK = "SyncManagerHandleSyncAlarm";
    private static final AccountAndUser[] INITIAL_ACCOUNTS_ARRAY = new AccountAndUser[0];
    private static final long INITIAL_SYNC_RETRY_TIME_IN_MS = 30000;
    private static final long LOCAL_SYNC_DELAY = SystemProperties.getLong("sync.local_sync_delay", INITIAL_SYNC_RETRY_TIME_IN_MS);
    private static final int MAX_SYNC_JOB_ID = 110000;
    private static final int MIN_SYNC_JOB_ID = 100000;
    private static final long SYNC_DELAY_ON_CONFLICT = 10000;
    private static final long SYNC_DELAY_ON_LOW_STORAGE = 3600000;
    private static final long SYNC_DELAY_ON_PROVISION = 600000;
    private static final String SYNC_LOOP_WAKE_LOCK = "SyncLoopWakeLock";
    private static final int SYNC_MONITOR_PROGRESS_THRESHOLD_BYTES = 10;
    private static final long SYNC_MONITOR_WINDOW_LENGTH_MILLIS = 60000;
    private static final int SYNC_OP_STATE_INVALID = 1;
    private static final int SYNC_OP_STATE_INVALID_NO_ACCOUNT_ACCESS = 2;
    private static final int SYNC_OP_STATE_VALID = 0;
    private static final String SYNC_WAKE_LOCK_PREFIX = "*sync*/";
    static final String TAG = "SyncManager";
    private final AccountManager mAccountManager;
    private final AccountManagerInternal mAccountManagerInternal;
    private final BroadcastReceiver mAccountsUpdatedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            SyncManager.this.updateRunningAccounts(new EndPoint(null, null, context.getUserId()));
        }
    };
    protected final ArrayList<ActiveSyncContext> mActiveSyncContexts = Lists.newArrayList();
    private final IBatteryStats mBatteryStats;
    private volatile boolean mBootCompleted = false;
    private final BroadcastReceiver mBootCompletedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            SyncManager.this.mBootCompleted = true;
            SyncManager.this.verifyJobScheduler();
            SyncManager.this.mSyncHandler.onBootCompleted();
        }
    };
    private ConnectivityManager mConnManagerDoNotUseDirectly;
    private BroadcastReceiver mConnectivityIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean wasConnected = SyncManager.this.mDataConnectionIsConnected;
            SyncManager.this.mDataConnectionIsConnected = SyncManager.this.readDataConnectionState();
            if (SyncManager.this.mDataConnectionIsConnected) {
                if (!wasConnected && Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "Reconnection detected: clearing all backoffs");
                }
                SyncManager.this.clearAllBackoffs();
            }
        }
    };
    private Context mContext;
    private volatile boolean mDataConnectionIsConnected = false;
    private volatile boolean mDeviceIsIdle = false;
    private volatile WakeLock mHandleAlarmWakeLock;
    private JobScheduler mJobScheduler;
    private JobSchedulerInternal mJobSchedulerInternal;
    private volatile boolean mJobServiceReady = false;
    private final NotificationManager mNotificationMgr;
    private final PackageManagerInternal mPackageManagerInternal;
    private final PowerManager mPowerManager;
    private volatile boolean mProvisioned;
    private final Random mRand;
    private volatile boolean mReportedSyncActive = false;
    private volatile AccountAndUser[] mRunningAccounts = INITIAL_ACCOUNTS_ARRAY;
    private BroadcastReceiver mShutdownIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.w("SyncManager", "Writing sync state before shutdown...");
            SyncManager.this.getSyncStorageEngine().writeAllState();
        }
    };
    private BroadcastReceiver mStorageIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.DEVICE_STORAGE_LOW".equals(action)) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "Internal storage is low.");
                }
                SyncManager.this.mStorageIsLow = true;
                SyncManager.this.cancelActiveSync(EndPoint.USER_ALL_PROVIDER_ALL_ACCOUNTS_ALL, null);
            } else if ("android.intent.action.DEVICE_STORAGE_OK".equals(action)) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "Internal storage is ok.");
                }
                SyncManager.this.mStorageIsLow = false;
                SyncManager.this.rescheduleSyncs(EndPoint.USER_ALL_PROVIDER_ALL_ACCOUNTS_ALL);
            }
        }
    };
    private volatile boolean mStorageIsLow = false;
    protected SyncAdaptersCache mSyncAdapters;
    private final SyncHandler mSyncHandler;
    private SyncJobService mSyncJobService;
    private volatile WakeLock mSyncManagerWakeLock;
    private SyncStorageEngine mSyncStorageEngine;
    private BroadcastReceiver mUserIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            if (userId != -10000) {
                if ("android.intent.action.USER_REMOVED".equals(action)) {
                    SyncManager.this.onUserRemoved(userId);
                } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                    SyncManager.this.onUserUnlocked(userId);
                } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                    SyncManager.this.onUserStopped(userId);
                }
            }
        }
    };
    private final UserManager mUserManager;

    private static class AccountSyncStats {
        long elapsedTime;
        String name;
        int times;

        /* synthetic */ AccountSyncStats(String name, AccountSyncStats -this1) {
            this(name);
        }

        private AccountSyncStats(String name) {
            this.name = name;
        }
    }

    class ActiveSyncContext extends Stub implements ServiceConnection, DeathRecipient {
        private static final int SOURCE_USER = 3;
        boolean mBound;
        long mBytesTransferredAtLastPoll;
        String mEventName;
        final long mHistoryRowId;
        boolean mIsLinkedToDeath = false;
        long mLastPolledTimeElapsed;
        final long mStartTime;
        ISyncAdapter mSyncAdapter;
        final int mSyncAdapterUid;
        SyncInfo mSyncInfo;
        final SyncOperation mSyncOperation;
        final WakeLock mSyncWakeLock;
        long mTimeoutStartTime;

        public ActiveSyncContext(SyncOperation syncOperation, long historyRowId, int syncAdapterUid) {
            this.mSyncAdapterUid = syncAdapterUid;
            this.mSyncOperation = syncOperation;
            this.mHistoryRowId = historyRowId;
            this.mSyncAdapter = null;
            this.mStartTime = SystemClock.elapsedRealtime();
            this.mTimeoutStartTime = this.mStartTime;
            this.mSyncWakeLock = SyncManager.this.mSyncHandler.getSyncWakeLock(this.mSyncOperation);
            this.mSyncWakeLock.setWorkSource(new WorkSource(syncAdapterUid));
            this.mSyncWakeLock.acquire();
        }

        public void sendHeartbeat() {
        }

        public void onFinished(SyncResult result) {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "onFinished: " + this);
            }
            SyncManager.this.sendSyncFinishedOrCanceledMessage(this, result);
        }

        public void toString(StringBuilder sb) {
            sb.append("startTime ").append(this.mStartTime).append(", mTimeoutStartTime ").append(this.mTimeoutStartTime).append(", mHistoryRowId ").append(this.mHistoryRowId).append(", syncOperation ").append(this.mSyncOperation);
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Message msg = SyncManager.this.mSyncHandler.obtainMessage();
            msg.what = 4;
            msg.obj = new ServiceConnectionData(this, service);
            SyncManager.this.mSyncHandler.sendMessage(msg);
        }

        public void onServiceDisconnected(ComponentName name) {
            Message msg = SyncManager.this.mSyncHandler.obtainMessage();
            msg.what = 5;
            msg.obj = new ServiceConnectionData(this, null);
            SyncManager.this.mSyncHandler.sendMessage(msg);
        }

        boolean bindToSyncAdapter(ComponentName serviceComponent, int userId, SyncOperation op) {
            if (Log.isLoggable("SyncManager", 2)) {
                Log.d("SyncManager", "bindToSyncAdapter: " + serviceComponent + ", connection " + this);
            }
            Intent intent = new Intent();
            intent.setAction("android.content.SyncAdapter");
            intent.setComponent(serviceComponent);
            intent.putExtra("android.intent.extra.client_label", 17041094);
            intent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivityAsUser(SyncManager.this.mContext, 0, new Intent("android.settings.SYNC_SETTINGS"), 0, null, new UserHandle(userId)));
            this.mBound = true;
            intent.addHwFlags(64);
            if (op.syncSource == 3) {
                intent.addHwFlags(128);
            }
            boolean bindResult = SyncManager.this.mContext.bindServiceAsUser(intent, this, 21, new UserHandle(this.mSyncOperation.target.userId));
            if (bindResult) {
                try {
                    this.mEventName = this.mSyncOperation.wakeLockName();
                    SyncManager.this.mBatteryStats.noteSyncStart(this.mEventName, this.mSyncAdapterUid);
                } catch (RemoteException e) {
                }
            } else {
                this.mBound = false;
            }
            return bindResult;
        }

        protected void close() {
            if (Log.isLoggable("SyncManager", 2)) {
                Log.d("SyncManager", "unBindFromSyncAdapter: connection " + this);
            }
            if (this.mBound) {
                this.mBound = false;
                SyncManager.this.mContext.unbindService(this);
                try {
                    SyncManager.this.mBatteryStats.noteSyncFinish(this.mEventName, this.mSyncAdapterUid);
                } catch (RemoteException e) {
                }
            }
            this.mSyncWakeLock.release();
            this.mSyncWakeLock.setWorkSource(null);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb);
            return sb.toString();
        }

        public void binderDied() {
            SyncManager.this.sendSyncFinishedOrCanceledMessage(this, null);
        }
    }

    private static class AuthoritySyncStats {
        Map<String, AccountSyncStats> accountMap;
        long elapsedTime;
        String name;
        int times;

        /* synthetic */ AuthoritySyncStats(String name, AuthoritySyncStats -this1) {
            this(name);
        }

        private AuthoritySyncStats(String name) {
            this.accountMap = Maps.newHashMap();
            this.name = name;
        }
    }

    static class PrintTable {
        private final int mCols;
        private ArrayList<Object[]> mTable = Lists.newArrayList();

        PrintTable(int cols) {
            this.mCols = cols;
        }

        void set(int row, int col, Object... values) {
            if (values.length + col > this.mCols) {
                throw new IndexOutOfBoundsException("Table only has " + this.mCols + " columns. can't set " + values.length + " at column " + col);
            }
            for (int i = this.mTable.size(); i <= row; i++) {
                Object[] list = new Object[this.mCols];
                this.mTable.add(list);
                for (int j = 0; j < this.mCols; j++) {
                    list[j] = "";
                }
            }
            System.arraycopy(values, 0, this.mTable.get(row), col, values.length);
        }

        void writeTo(PrintWriter out) {
            int i;
            String[] formats = new String[this.mCols];
            int totalLength = 0;
            for (int col = 0; col < this.mCols; col++) {
                int maxLength = 0;
                for (Object[] row : this.mTable) {
                    int length = row[col].toString().length();
                    if (length > maxLength) {
                        maxLength = length;
                    }
                }
                totalLength += maxLength;
                formats[col] = String.format("%%-%ds", new Object[]{Integer.valueOf(maxLength)});
            }
            formats[this.mCols - 1] = "%s";
            printRow(out, formats, (Object[]) this.mTable.get(0));
            totalLength += (this.mCols - 1) * 2;
            for (i = 0; i < totalLength; i++) {
                out.print("-");
            }
            out.println();
            int mTableSize = this.mTable.size();
            for (i = 1; i < mTableSize; i++) {
                printRow(out, formats, (Object[]) this.mTable.get(i));
            }
        }

        private void printRow(PrintWriter out, String[] formats, Object[] row) {
            int rowLength = row.length;
            for (int j = 0; j < rowLength; j++) {
                out.printf(String.format(formats[j], new Object[]{row[j].toString()}), new Object[0]);
                out.print("  ");
            }
            out.println();
        }

        public int getNumRows() {
            return this.mTable.size();
        }
    }

    private static class ScheduleSyncMessagePayload {
        final long minDelayMillis;
        final SyncOperation syncOperation;

        ScheduleSyncMessagePayload(SyncOperation syncOperation, long minDelayMillis) {
            this.syncOperation = syncOperation;
            this.minDelayMillis = minDelayMillis;
        }
    }

    class ServiceConnectionData {
        public final ActiveSyncContext activeSyncContext;
        public final IBinder adapter;

        ServiceConnectionData(ActiveSyncContext activeSyncContext, IBinder adapter) {
            this.activeSyncContext = activeSyncContext;
            this.adapter = adapter;
        }
    }

    private class SyncFinishedOrCancelledMessagePayload {
        public final ActiveSyncContext activeSyncContext;
        public final SyncResult syncResult;

        SyncFinishedOrCancelledMessagePayload(ActiveSyncContext syncContext, SyncResult syncResult) {
            this.activeSyncContext = syncContext;
            this.syncResult = syncResult;
        }
    }

    class SyncHandler extends Handler {
        private static final int MESSAGE_ACCOUNTS_UPDATED = 9;
        private static final int MESSAGE_CANCEL = 6;
        static final int MESSAGE_JOBSERVICE_OBJECT = 7;
        private static final int MESSAGE_MONITOR_SYNC = 8;
        private static final int MESSAGE_RELEASE_MESSAGES_FROM_QUEUE = 2;
        static final int MESSAGE_REMOVE_PERIODIC_SYNC = 14;
        static final int MESSAGE_SCHEDULE_SYNC = 12;
        private static final int MESSAGE_SERVICE_CONNECTED = 4;
        private static final int MESSAGE_SERVICE_DISCONNECTED = 5;
        static final int MESSAGE_START_SYNC = 10;
        static final int MESSAGE_STOP_SYNC = 11;
        private static final int MESSAGE_SYNC_FINISHED = 1;
        static final int MESSAGE_UPDATE_PERIODIC_SYNC = 13;
        public final SyncTimeTracker mSyncTimeTracker = new SyncTimeTracker(SyncManager.this, null);
        private List<Message> mUnreadyQueue = new ArrayList();
        private final HashMap<String, WakeLock> mWakeLocks = Maps.newHashMap();

        void onBootCompleted() {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "Boot completed.");
            }
            checkIfDeviceReady();
        }

        void onDeviceProvisioned() {
            if (Log.isLoggable("SyncManager", 3)) {
                Log.d("SyncManager", "mProvisioned=" + SyncManager.this.mProvisioned);
            }
            checkIfDeviceReady();
        }

        void checkIfDeviceReady() {
            if (SyncManager.this.mProvisioned && SyncManager.this.mBootCompleted && SyncManager.this.mJobServiceReady) {
                synchronized (this) {
                    SyncManager.this.mSyncStorageEngine.restoreAllPeriodicSyncs();
                    obtainMessage(2).sendToTarget();
                }
            }
        }

        private boolean tryEnqueueMessageUntilReadyToRun(Message msg) {
            synchronized (this) {
                if (SyncManager.this.mBootCompleted && (SyncManager.this.mProvisioned ^ 1) == 0 && (SyncManager.this.mJobServiceReady ^ 1) == 0) {
                    return false;
                }
                if (SyncManager.this.mProvisioned || !(msg.obj instanceof SyncOperation)) {
                    this.mUnreadyQueue.add(Message.obtain(msg));
                } else {
                    deferSyncH((SyncOperation) msg.obj, 600000);
                }
                return true;
            }
        }

        public SyncHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            try {
                SyncManager.this.mSyncManagerWakeLock.acquire();
                if (msg.what == 7) {
                    Slog.i("SyncManager", "Got SyncJobService instance.");
                    SyncManager.this.mSyncJobService = (SyncJobService) msg.obj;
                    SyncManager.this.mJobServiceReady = true;
                    checkIfDeviceReady();
                } else if (msg.what == 9) {
                    if (Log.isLoggable("SyncManager", 2)) {
                        Slog.v("SyncManager", "handleSyncHandlerMessage: MESSAGE_ACCOUNTS_UPDATED");
                    }
                    updateRunningAccountsH(msg.obj);
                } else if (msg.what == 2) {
                    if (this.mUnreadyQueue != null) {
                        for (Message m : this.mUnreadyQueue) {
                            handleSyncMessage(m);
                        }
                        this.mUnreadyQueue = null;
                    }
                } else if (!tryEnqueueMessageUntilReadyToRun(msg)) {
                    handleSyncMessage(msg);
                }
                SyncManager.this.mSyncManagerWakeLock.release();
            } catch (Throwable th) {
                SyncManager.this.mSyncManagerWakeLock.release();
            }
        }

        private void handleSyncMessage(Message msg) {
            boolean isLoggable = Log.isLoggable("SyncManager", 2);
            try {
                SyncManager.this.mDataConnectionIsConnected = SyncManager.this.readDataConnectionState();
                switch (msg.what) {
                    case 1:
                        SyncFinishedOrCancelledMessagePayload payload = msg.obj;
                        if (!SyncManager.this.isSyncStillActiveH(payload.activeSyncContext)) {
                            Log.d("SyncManager", "handleSyncHandlerMessage: dropping since the sync is no longer active: " + payload.activeSyncContext);
                            break;
                        }
                        if (isLoggable) {
                            Slog.v("SyncManager", "syncFinished" + payload.activeSyncContext.mSyncOperation);
                        }
                        SyncManager.this.mSyncJobService.callJobFinished(payload.activeSyncContext.mSyncOperation.jobId, false);
                        runSyncFinishedOrCanceledH(payload.syncResult, payload.activeSyncContext);
                        break;
                    case 4:
                        ServiceConnectionData msgData = msg.obj;
                        if (Log.isLoggable("SyncManager", 2)) {
                            Log.d("SyncManager", "handleSyncHandlerMessage: MESSAGE_SERVICE_CONNECTED: " + msgData.activeSyncContext);
                        }
                        if (SyncManager.this.isSyncStillActiveH(msgData.activeSyncContext)) {
                            runBoundToAdapterH(msgData.activeSyncContext, msgData.adapter);
                            break;
                        }
                        break;
                    case 5:
                        ActiveSyncContext currentSyncContext = ((ServiceConnectionData) msg.obj).activeSyncContext;
                        if (Log.isLoggable("SyncManager", 2)) {
                            Log.d("SyncManager", "handleSyncHandlerMessage: MESSAGE_SERVICE_DISCONNECTED: " + currentSyncContext);
                        }
                        if (SyncManager.this.isSyncStillActiveH(currentSyncContext)) {
                            try {
                                if (currentSyncContext.mSyncAdapter != null) {
                                    currentSyncContext.mSyncAdapter.cancelSync(currentSyncContext);
                                }
                            } catch (RemoteException e) {
                            }
                            SyncResult syncResult = new SyncResult();
                            SyncStats syncStats = syncResult.stats;
                            syncStats.numIoExceptions++;
                            SyncManager.this.mSyncJobService.callJobFinished(currentSyncContext.mSyncOperation.jobId, false);
                            runSyncFinishedOrCanceledH(syncResult, currentSyncContext);
                            break;
                        }
                        break;
                    case 6:
                        EndPoint endpoint = msg.obj;
                        Bundle extras = msg.peekData();
                        if (Log.isLoggable("SyncManager", 3)) {
                            Log.d("SyncManager", "handleSyncHandlerMessage: MESSAGE_CANCEL: " + endpoint + " bundle: " + extras);
                        }
                        cancelActiveSyncH(endpoint, extras);
                        break;
                    case 8:
                        ActiveSyncContext monitoredSyncContext = msg.obj;
                        if (Log.isLoggable("SyncManager", 3)) {
                            Log.d("SyncManager", "handleSyncHandlerMessage: MESSAGE_MONITOR_SYNC: " + monitoredSyncContext.mSyncOperation.target);
                        }
                        if (!isSyncNotUsingNetworkH(monitoredSyncContext)) {
                            SyncManager.this.postMonitorSyncProgressMessage(monitoredSyncContext);
                            break;
                        }
                        Log.w("SyncManager", String.format("Detected sync making no progress for %s. cancelling.", new Object[]{monitoredSyncContext}));
                        SyncManager.this.mSyncJobService.callJobFinished(monitoredSyncContext.mSyncOperation.jobId, false);
                        runSyncFinishedOrCanceledH(null, monitoredSyncContext);
                        break;
                    case 10:
                        startSyncH((SyncOperation) msg.obj);
                        break;
                    case 11:
                        SyncOperation op = (SyncOperation) msg.obj;
                        if (isLoggable) {
                            Slog.v("SyncManager", "Stop sync received.");
                        }
                        ActiveSyncContext asc = findActiveSyncContextH(op.jobId);
                        if (asc != null) {
                            runSyncFinishedOrCanceledH(null, asc);
                            boolean reschedule = msg.arg1 != 0;
                            boolean applyBackoff = msg.arg2 != 0;
                            if (isLoggable) {
                                Slog.v("SyncManager", "Stopping sync. Reschedule: " + reschedule + "Backoff: " + applyBackoff);
                            }
                            if (applyBackoff) {
                                SyncManager.this.increaseBackoffSetting(op.target);
                            }
                            if (reschedule) {
                                deferStoppedSyncH(op, 0);
                                break;
                            }
                        }
                        break;
                    case 12:
                        ScheduleSyncMessagePayload syncPayload = msg.obj;
                        SyncManager.this.scheduleSyncOperationH(syncPayload.syncOperation, syncPayload.minDelayMillis);
                        break;
                    case 13:
                        UpdatePeriodicSyncMessagePayload data = msg.obj;
                        updateOrAddPeriodicSyncH(data.target, data.pollFrequency, data.flex, data.extras);
                        break;
                    case 14:
                        removePeriodicSyncH((EndPoint) msg.obj, msg.getData());
                        break;
                }
                this.mSyncTimeTracker.update();
            } catch (Throwable th) {
                this.mSyncTimeTracker.update();
            }
        }

        private WakeLock getSyncWakeLock(SyncOperation operation) {
            String wakeLockKey = operation.wakeLockName();
            WakeLock wakeLock = (WakeLock) this.mWakeLocks.get(wakeLockKey);
            if (wakeLock != null) {
                return wakeLock;
            }
            wakeLock = SyncManager.this.mPowerManager.newWakeLock(1, SyncManager.SYNC_WAKE_LOCK_PREFIX + wakeLockKey);
            wakeLock.setReferenceCounted(false);
            this.mWakeLocks.put(wakeLockKey, wakeLock);
            return wakeLock;
        }

        private void deferSyncH(SyncOperation op, long delay) {
            SyncManager.this.mSyncJobService.callJobFinished(op.jobId, false);
            if (op.isPeriodic) {
                SyncManager.this.scheduleSyncOperationH(op.createOneTimeSyncOperation(), delay);
                return;
            }
            SyncManager.this.getJobScheduler().cancel(op.jobId);
            SyncManager.this.scheduleSyncOperationH(op, delay);
        }

        private void deferStoppedSyncH(SyncOperation op, long delay) {
            if (op.isPeriodic) {
                SyncManager.this.scheduleSyncOperationH(op.createOneTimeSyncOperation(), delay);
            } else {
                SyncManager.this.scheduleSyncOperationH(op, delay);
            }
        }

        private void deferActiveSyncH(ActiveSyncContext asc) {
            SyncOperation op = asc.mSyncOperation;
            runSyncFinishedOrCanceledH(null, asc);
            deferSyncH(op, 10000);
        }

        /* JADX WARNING: Removed duplicated region for block: B:43:0x00eb  */
        /* JADX WARNING: Removed duplicated region for block: B:48:0x0104  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void startSyncH(SyncOperation op) {
            boolean isLoggable = Log.isLoggable("SyncManager", 2);
            if (isLoggable) {
                Slog.v("SyncManager", op.toString());
            }
            if (!SyncManager.this.mStorageIsLow) {
                if (op.isPeriodic) {
                    for (SyncOperation syncOperation : SyncManager.this.getAllPendingSyncs()) {
                        if (syncOperation.sourcePeriodicId == op.jobId) {
                            SyncManager.this.mSyncJobService.callJobFinished(op.jobId, false);
                            return;
                        }
                    }
                    for (ActiveSyncContext asc : SyncManager.this.mActiveSyncContexts) {
                        if (asc.mSyncOperation.sourcePeriodicId == op.jobId) {
                            SyncManager.this.mSyncJobService.callJobFinished(op.jobId, false);
                            return;
                        }
                    }
                    if (SyncManager.this.isAdapterDelayed(op.target)) {
                        deferSyncH(op, 0);
                        return;
                    }
                }
                for (ActiveSyncContext asc2 : SyncManager.this.mActiveSyncContexts) {
                    if (asc2.mSyncOperation.isConflict(op)) {
                        if (asc2.mSyncOperation.findPriority() >= op.findPriority()) {
                            if (isLoggable) {
                                Slog.v("SyncManager", "Rescheduling sync due to conflict " + op.toString());
                            }
                            deferSyncH(op, 10000);
                            return;
                        }
                        if (isLoggable) {
                            Slog.v("SyncManager", "Pushing back running sync due to a higher priority sync");
                        }
                        deferActiveSyncH(asc2);
                        switch (computeSyncOpState(op)) {
                            case 1:
                            case 2:
                                SyncManager.this.mSyncJobService.callJobFinished(op.jobId, false);
                                return;
                            default:
                                if (!dispatchSyncOperation(op)) {
                                    SyncManager.this.mSyncJobService.callJobFinished(op.jobId, false);
                                }
                                SyncManager.this.setAuthorityPendingState(op.target);
                                return;
                        }
                    }
                }
                switch (computeSyncOpState(op)) {
                    case 1:
                    case 2:
                        break;
                    default:
                        break;
                }
            }
            deferSyncH(op, SyncManager.SYNC_DELAY_ON_LOW_STORAGE);
        }

        private ActiveSyncContext findActiveSyncContextH(int jobId) {
            for (ActiveSyncContext asc : SyncManager.this.mActiveSyncContexts) {
                SyncOperation op = asc.mSyncOperation;
                if (op != null && op.jobId == jobId) {
                    return asc;
                }
            }
            return null;
        }

        /* JADX WARNING: Removed duplicated region for block: B:25:0x010f  */
        /* JADX WARNING: Removed duplicated region for block: B:43:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x0141  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void updateRunningAccountsH(EndPoint syncTargets) {
            AccountAndUser[] allAccounts;
            AccountAndUser[] oldAccounts = SyncManager.this.mRunningAccounts;
            SyncManager.this.mRunningAccounts = AccountManagerService.getSingleton().getRunningAccounts();
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "Accounts list: ");
                for (AccountAndUser acc : SyncManager.this.mRunningAccounts) {
                    Slog.v("SyncManager", acc.toString());
                }
            }
            if (SyncManager.this.mBootCompleted) {
                SyncManager.this.doDatabaseCleanup();
            }
            AccountAndUser[] accounts = SyncManager.this.mRunningAccounts;
            for (ActiveSyncContext currentSyncContext : new ArrayList(SyncManager.this.mActiveSyncContexts)) {
                if (!SyncManager.this.containsAccountAndUser(accounts, currentSyncContext.mSyncOperation.target.account, currentSyncContext.mSyncOperation.target.userId)) {
                    Log.d("SyncManager", "canceling sync since the account is no longer running");
                    SyncManager.this.sendSyncFinishedOrCanceledMessage(currentSyncContext, null);
                }
            }
            AccountAndUser[] -get11 = SyncManager.this.mRunningAccounts;
            int i = 0;
            int length = -get11.length;
            while (i < length) {
                AccountAndUser aau = -get11[i];
                if (SyncManager.this.containsAccountAndUser(oldAccounts, aau.account, aau.userId)) {
                    i++;
                } else {
                    if (Log.isLoggable("SyncManager", 3)) {
                        Log.d("SyncManager", "Account " + aau.account + " added, checking sync restore data");
                    }
                    AccountSyncSettingsBackupHelper.accountAdded(SyncManager.this.mContext);
                    allAccounts = AccountManagerService.getSingleton().getAllAccounts();
                    for (SyncOperation op : SyncManager.this.getAllPendingSyncs()) {
                        if (!SyncManager.this.containsAccountAndUser(allAccounts, op.target.account, op.target.userId)) {
                            SyncManager.this.getJobScheduler().cancel(op.jobId);
                        }
                    }
                    if (syncTargets == null) {
                        SyncManager.this.scheduleSync(syncTargets.account, syncTargets.userId, -2, syncTargets.provider, null, -1);
                        return;
                    }
                    return;
                }
            }
            allAccounts = AccountManagerService.getSingleton().getAllAccounts();
            for (SyncOperation op2 : SyncManager.this.getAllPendingSyncs()) {
            }
            if (syncTargets == null) {
            }
        }

        private void maybeUpdateSyncPeriodH(SyncOperation syncOperation, long pollFrequencyMillis, long flexMillis) {
            if (pollFrequencyMillis != syncOperation.periodMillis || flexMillis != syncOperation.flexMillis) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "updating period " + syncOperation + " to " + pollFrequencyMillis + " and flex to " + flexMillis);
                }
                SyncOperation newOp = new SyncOperation(syncOperation, pollFrequencyMillis, flexMillis);
                newOp.jobId = syncOperation.jobId;
                SyncManager.this.scheduleSyncOperationH(newOp);
            }
        }

        private void updateOrAddPeriodicSyncH(EndPoint target, long pollFrequency, long flex, Bundle extras) {
            if (target.account != null) {
                boolean isLoggable = Log.isLoggable("SyncManager", 2);
                SyncManager.this.verifyJobScheduler();
                long pollFrequencyMillis = pollFrequency * 1000;
                long flexMillis = flex * 1000;
                if (isLoggable) {
                    Slog.v("SyncManager", "Addition to periodic syncs requested: " + target + " period: " + pollFrequency + " flexMillis: " + flex + " extras: " + extras.toString());
                }
                for (SyncOperation op : SyncManager.this.getAllPendingSyncs()) {
                    if (op.isPeriodic && op.target.matchesSpec(target) && SyncManager.syncExtrasEquals(op.extras, extras, true)) {
                        maybeUpdateSyncPeriodH(op, pollFrequencyMillis, flexMillis);
                        return;
                    }
                }
                if (isLoggable) {
                    Slog.v("SyncManager", "Adding new periodic sync: " + target + " period: " + pollFrequency + " flexMillis: " + flex + " extras: " + extras.toString());
                }
                ServiceInfo<SyncAdapterType> syncAdapterInfo = SyncManager.this.mSyncAdapters.getServiceInfo(SyncAdapterType.newKey(target.provider, target.account.type), target.userId);
                if (syncAdapterInfo != null) {
                    SyncOperation syncOperation = new SyncOperation(target, syncAdapterInfo.uid, syncAdapterInfo.componentName.getPackageName(), -4, 4, extras, ((SyncAdapterType) syncAdapterInfo.type).allowParallelSyncs(), true, -1, pollFrequencyMillis, flexMillis);
                    switch (computeSyncOpState(syncOperation)) {
                        case 1:
                            return;
                        case 2:
                            String packageName = syncOperation.owningPackage;
                            int userId = UserHandle.getUserId(syncOperation.owningUid);
                            if (SyncManager.this.mPackageManagerInternal.wasPackageEverLaunched(packageName, userId)) {
                                SyncManager.this.mAccountManagerInternal.requestAccountAccess(syncOperation.target.account, packageName, userId, new RemoteCallback(new com.android.server.content.-$Lambda$doNli3wDRrwDz12cAoe6lOOQskA.AnonymousClass1(pollFrequency, flex, this, target, extras)));
                                return;
                            }
                            return;
                        default:
                            SyncManager.this.scheduleSyncOperationH(syncOperation);
                            SyncManager.this.mSyncStorageEngine.reportChange(1);
                            return;
                    }
                }
            }
        }

        /* synthetic */ void lambda$-com_android_server_content_SyncManager$SyncHandler_128357(EndPoint target, long pollFrequency, long flex, Bundle extras, Bundle result) {
            if (result != null && result.getBoolean("booleanResult")) {
                SyncManager.this.updateOrAddPeriodicSync(target, pollFrequency, flex, extras);
            }
        }

        private void removePeriodicSyncInternalH(SyncOperation syncOperation) {
            for (SyncOperation op : SyncManager.this.getAllPendingSyncs()) {
                if (op.sourcePeriodicId == syncOperation.jobId || op.jobId == syncOperation.jobId) {
                    ActiveSyncContext asc = findActiveSyncContextH(syncOperation.jobId);
                    if (asc != null) {
                        SyncManager.this.mSyncJobService.callJobFinished(syncOperation.jobId, false);
                        runSyncFinishedOrCanceledH(null, asc);
                    }
                    SyncManager.this.getJobScheduler().cancel(op.jobId);
                }
            }
        }

        private void removePeriodicSyncH(EndPoint target, Bundle extras) {
            SyncManager.this.verifyJobScheduler();
            for (SyncOperation op : SyncManager.this.getAllPendingSyncs()) {
                if (op.isPeriodic && op.target.matchesSpec(target) && SyncManager.syncExtrasEquals(op.extras, extras, true)) {
                    removePeriodicSyncInternalH(op);
                }
            }
        }

        private boolean isSyncNotUsingNetworkH(ActiveSyncContext activeSyncContext) {
            long deltaBytesTransferred = SyncManager.this.getTotalBytesTransferredByUid(activeSyncContext.mSyncAdapterUid) - activeSyncContext.mBytesTransferredAtLastPoll;
            if (Log.isLoggable("SyncManager", 3)) {
                long remainder = deltaBytesTransferred;
                long mb = deltaBytesTransferred / 1048576;
                remainder = deltaBytesTransferred % 1048576;
                long kb = remainder / 1024;
                long b = remainder % 1024;
                Log.d("SyncManager", String.format("Time since last update: %ds. Delta transferred: %dMBs,%dKBs,%dBs", new Object[]{Long.valueOf((SystemClock.elapsedRealtime() - activeSyncContext.mLastPolledTimeElapsed) / 1000), Long.valueOf(mb), Long.valueOf(kb), Long.valueOf(remainder % 1024)}));
            }
            return deltaBytesTransferred <= 10;
        }

        private int computeSyncOpState(SyncOperation op) {
            boolean isLoggable = Log.isLoggable("SyncManager", 2);
            EndPoint target = op.target;
            if (SyncManager.this.containsAccountAndUser(SyncManager.this.mRunningAccounts, target.account, target.userId)) {
                int state = SyncManager.this.computeSyncable(target.account, target.userId, target.provider);
                if (state == 3) {
                    if (isLoggable) {
                        Slog.v("SyncManager", "    Dropping sync operation: isSyncable == SYNCABLE_NO_ACCOUNT_ACCESS");
                    }
                    return 2;
                } else if (state == 0) {
                    if (isLoggable) {
                        Slog.v("SyncManager", "    Dropping sync operation: isSyncable == NOT_SYNCABLE");
                    }
                    return 1;
                } else {
                    boolean syncEnabled;
                    if (SyncManager.this.mSyncStorageEngine.getMasterSyncAutomatically(target.userId)) {
                        syncEnabled = SyncManager.this.mSyncStorageEngine.getSyncAutomatically(target.account, target.userId, target.provider);
                    } else {
                        syncEnabled = false;
                    }
                    boolean ignoreSystemConfiguration = op.isIgnoreSettings() || state < 0;
                    if (syncEnabled || (ignoreSystemConfiguration ^ 1) == 0) {
                        if (HwDeviceManager.disallowOp(42) && (op.isManual() ^ 1) != 0 && state >= 0) {
                            NetworkInfo networkInfo = SyncManager.this.getConnectivityManager().getActiveNetworkInfo();
                            if (networkInfo != null && networkInfo.isRoaming() && networkInfo.getType() == 0) {
                                Slog.v("SyncManager", "    Dropping auto sync operation for " + target.provider + ": disallowed by MDM disable auto sync when roaming.");
                                return 1;
                            }
                        }
                        if (target.account == null || !HwDeviceManager.disallowOp(25, target.account.type) || (op.isManual() ^ 1) == 0 || state < 0) {
                            return 0;
                        }
                        Slog.v("SyncManager", "    Dropping auto sync operation for " + target.provider + ": disallowed by MDM.");
                        return 1;
                    }
                    if (isLoggable) {
                        Slog.v("SyncManager", "    Dropping sync operation: disallowed by settings/network.");
                    }
                    return 1;
                }
            }
            if (isLoggable) {
                Slog.v("SyncManager", "    Dropping sync operation: account doesn't exist.");
            }
            return 1;
        }

        private boolean dispatchSyncOperation(SyncOperation op) {
            Slog.i("SyncManager", "dispatchSyncOperation:we are going to sync " + op);
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "num active syncs: " + SyncManager.this.mActiveSyncContexts.size());
                for (ActiveSyncContext syncContext : SyncManager.this.mActiveSyncContexts) {
                    Slog.v("SyncManager", syncContext.toString());
                }
            }
            EndPoint info = op.target;
            SyncAdapterType syncAdapterType = SyncAdapterType.newKey(info.provider, info.account.type);
            ServiceInfo<SyncAdapterType> syncAdapterInfo = SyncManager.this.mSyncAdapters.getServiceInfo(syncAdapterType, info.userId);
            if (syncAdapterInfo == null) {
                Log.i("SyncManager", "can't find a sync adapter for " + syncAdapterType + ", removing settings for it");
                SyncManager.this.mSyncStorageEngine.removeAuthority(info);
                return false;
            }
            int targetUid = syncAdapterInfo.uid;
            ComponentName targetComponent = syncAdapterInfo.componentName;
            ActiveSyncContext activeSyncContext = new ActiveSyncContext(op, insertStartSyncEvent(op), targetUid);
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "dispatchSyncOperation: starting " + activeSyncContext);
            }
            activeSyncContext.mSyncInfo = SyncManager.this.mSyncStorageEngine.addActiveSync(activeSyncContext);
            SyncManager.this.mActiveSyncContexts.add(activeSyncContext);
            SyncManager.this.postMonitorSyncProgressMessage(activeSyncContext);
            if (activeSyncContext.bindToSyncAdapter(targetComponent, info.userId, op)) {
                return true;
            }
            Slog.e("SyncManager", "Bind attempt failed - target: " + targetComponent);
            closeActiveSyncContext(activeSyncContext);
            return false;
        }

        private void runBoundToAdapterH(ActiveSyncContext activeSyncContext, IBinder syncAdapter) {
            SyncOperation syncOperation = activeSyncContext.mSyncOperation;
            try {
                activeSyncContext.mIsLinkedToDeath = true;
                syncAdapter.linkToDeath(activeSyncContext, 0);
                activeSyncContext.mSyncAdapter = ISyncAdapter.Stub.asInterface(syncAdapter);
                Slog.i("SyncManager", "run startSync:" + syncOperation);
                activeSyncContext.mSyncAdapter.startSync(activeSyncContext, syncOperation.target.provider, syncOperation.target.account, syncOperation.extras);
            } catch (RemoteException remoteExc) {
                Log.i("SyncManager", "maybeStartNextSync: caught a RemoteException, rescheduling", remoteExc);
                closeActiveSyncContext(activeSyncContext);
                SyncManager.this.increaseBackoffSetting(syncOperation.target);
                SyncManager.this.scheduleSyncOperationH(syncOperation);
            } catch (RuntimeException exc) {
                closeActiveSyncContext(activeSyncContext);
                Slog.e("SyncManager", "Caught RuntimeException while starting the sync " + syncOperation, exc);
            }
        }

        private void cancelActiveSyncH(EndPoint info, Bundle extras) {
            for (ActiveSyncContext activeSyncContext : new ArrayList(SyncManager.this.mActiveSyncContexts)) {
                if (activeSyncContext != null && activeSyncContext.mSyncOperation.target.matchesSpec(info)) {
                    if (extras == null || (SyncManager.syncExtrasEquals(activeSyncContext.mSyncOperation.extras, extras, false) ^ 1) == 0) {
                        SyncManager.this.mSyncJobService.callJobFinished(activeSyncContext.mSyncOperation.jobId, false);
                        runSyncFinishedOrCanceledH(null, activeSyncContext);
                    }
                }
            }
        }

        private void reschedulePeriodicSyncH(SyncOperation syncOperation) {
            SyncOperation periodicSync = null;
            for (SyncOperation op : SyncManager.this.getAllPendingSyncs()) {
                if (op.isPeriodic && syncOperation.matchesPeriodicOperation(op)) {
                    periodicSync = op;
                    break;
                }
            }
            if (periodicSync != null) {
                SyncManager.this.scheduleSyncOperationH(periodicSync);
            }
        }

        private void runSyncFinishedOrCanceledH(SyncResult syncResult, ActiveSyncContext activeSyncContext) {
            String historyMessage;
            int downstreamActivity;
            int upstreamActivity;
            boolean isLoggable = Log.isLoggable("SyncManager", 2);
            SyncOperation syncOperation = activeSyncContext.mSyncOperation;
            EndPoint info = syncOperation.target;
            if (activeSyncContext.mIsLinkedToDeath) {
                activeSyncContext.mSyncAdapter.asBinder().unlinkToDeath(activeSyncContext, 0);
                activeSyncContext.mIsLinkedToDeath = false;
            }
            closeActiveSyncContext(activeSyncContext);
            long elapsedTime = SystemClock.elapsedRealtime() - activeSyncContext.mStartTime;
            if (!syncOperation.isPeriodic) {
                SyncManager.this.getJobScheduler().cancel(syncOperation.jobId);
            }
            if (syncResult != null) {
                if (isLoggable) {
                    Slog.v("SyncManager", "runSyncFinishedOrCanceled [finished]: " + syncOperation + ", result " + syncResult);
                }
                if (syncResult.hasError()) {
                    Log.d("SyncManager", "failed sync operation ");
                    SyncManager.this.increaseBackoffSetting(syncOperation.target);
                    if (syncOperation.isPeriodic) {
                        SyncManager.this.postScheduleSyncMessage(syncOperation.createOneTimeSyncOperation(), 0);
                    } else {
                        SyncManager.this.maybeRescheduleSync(syncResult, syncOperation);
                    }
                    historyMessage = ContentResolver.syncErrorToString(syncResultToErrorNumber(syncResult));
                    downstreamActivity = 0;
                    upstreamActivity = 0;
                } else {
                    historyMessage = SyncStorageEngine.MESG_SUCCESS;
                    downstreamActivity = 0;
                    upstreamActivity = 0;
                    SyncManager.this.clearBackoffSetting(syncOperation.target);
                    if (syncOperation.isDerivedFromFailedPeriodicSync()) {
                        reschedulePeriodicSyncH(syncOperation);
                    }
                }
                SyncManager.this.setDelayUntilTime(syncOperation.target, syncResult.delayUntil);
            } else {
                if (isLoggable) {
                    Slog.v("SyncManager", "runSyncFinishedOrCanceled [canceled]: " + syncOperation);
                }
                if (activeSyncContext.mSyncAdapter != null) {
                    try {
                        activeSyncContext.mSyncAdapter.cancelSync(activeSyncContext);
                    } catch (RemoteException e) {
                    }
                }
                historyMessage = SyncStorageEngine.MESG_CANCELED;
                downstreamActivity = 0;
                upstreamActivity = 0;
            }
            stopSyncEvent(activeSyncContext.mHistoryRowId, syncOperation, historyMessage, upstreamActivity, downstreamActivity, elapsedTime);
            if (syncResult == null || !syncResult.tooManyDeletions) {
                SyncManager.this.mNotificationMgr.cancelAsUser(Integer.toString(info.account.hashCode() ^ info.provider.hashCode()), 18, new UserHandle(info.userId));
            } else {
                installHandleTooManyDeletesNotification(info.account, info.provider, syncResult.stats.numDeletes, info.userId);
            }
            if (syncResult != null && syncResult.fullSyncRequested) {
                SyncManager.this.scheduleSyncOperationH(new SyncOperation(info.account, info.userId, syncOperation.owningUid, syncOperation.owningPackage, syncOperation.reason, syncOperation.syncSource, info.provider, new Bundle(), syncOperation.allowParallelSyncs));
            }
        }

        private void closeActiveSyncContext(ActiveSyncContext activeSyncContext) {
            activeSyncContext.close();
            SyncManager.this.mActiveSyncContexts.remove(activeSyncContext);
            SyncManager.this.mSyncStorageEngine.removeActiveSync(activeSyncContext.mSyncInfo, activeSyncContext.mSyncOperation.target.userId);
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "removing all MESSAGE_MONITOR_SYNC & MESSAGE_SYNC_EXPIRED for " + activeSyncContext.toString());
            }
            SyncManager.this.mSyncHandler.removeMessages(8, activeSyncContext);
        }

        private int syncResultToErrorNumber(SyncResult syncResult) {
            if (syncResult.syncAlreadyInProgress) {
                return 1;
            }
            if (syncResult.stats.numAuthExceptions > 0) {
                return 2;
            }
            if (syncResult.stats.numIoExceptions > 0) {
                return 3;
            }
            if (syncResult.stats.numParseExceptions > 0) {
                return 4;
            }
            if (syncResult.stats.numConflictDetectedExceptions > 0) {
                return 5;
            }
            if (syncResult.tooManyDeletions) {
                return 6;
            }
            if (syncResult.tooManyRetries) {
                return 7;
            }
            if (syncResult.databaseError) {
                return 8;
            }
            throw new IllegalStateException("we are not in an error state, " + syncResult);
        }

        private void installHandleTooManyDeletesNotification(Account account, String authority, long numDeletes, int userId) {
            if (SyncManager.this.mNotificationMgr != null) {
                ProviderInfo providerInfo = SyncManager.this.mContext.getPackageManager().resolveContentProvider(authority, 0);
                if (providerInfo != null) {
                    CharSequence authorityName = providerInfo.loadLabel(SyncManager.this.mContext.getPackageManager());
                    Intent clickIntent = new Intent(SyncManager.this.mContext, SyncActivityTooManyDeletes.class);
                    clickIntent.putExtra("account", account);
                    clickIntent.putExtra("authority", authority);
                    clickIntent.putExtra("provider", authorityName.toString());
                    clickIntent.putExtra("numDeletes", numDeletes);
                    if (isActivityAvailable(clickIntent)) {
                        UserHandle user = new UserHandle(userId);
                        PendingIntent pendingIntent = PendingIntent.getActivityAsUser(SyncManager.this.mContext, 0, clickIntent, 268435456, null, user);
                        CharSequence tooManyDeletesDescFormat = SyncManager.this.mContext.getResources().getText(17039827);
                        Context contextForUser = SyncManager.this.getContextForUser(user);
                        Notification notification = new Builder(contextForUser, SystemNotificationChannels.ACCOUNT).setSmallIcon(17303327).setLargeIcon(BitmapFactory.decodeResource(SyncManager.this.mContext.getResources(), 33751680)).setTicker(SyncManager.this.mContext.getString(17039825)).setWhen(System.currentTimeMillis()).setColor(contextForUser.getColor(17170769)).setContentTitle(contextForUser.getString(17039826)).setContentText(String.format(tooManyDeletesDescFormat.toString(), new Object[]{authorityName})).setContentIntent(pendingIntent).build();
                        notification.flags |= 2;
                        SyncManager.this.mNotificationMgr.notifyAsUser(Integer.toString(account.hashCode() ^ authority.hashCode()), 18, notification, user);
                        return;
                    }
                    Log.w("SyncManager", "No activity found to handle too many deletes.");
                }
            }
        }

        private boolean isActivityAvailable(Intent intent) {
            List<ResolveInfo> list = SyncManager.this.mContext.getPackageManager().queryIntentActivities(intent, 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                if ((((ResolveInfo) list.get(i)).activityInfo.applicationInfo.flags & 1) != 0) {
                    return true;
                }
            }
            return false;
        }

        public long insertStartSyncEvent(SyncOperation syncOperation) {
            long now = System.currentTimeMillis();
            EventLog.writeEvent(2720, syncOperation.toEventLog(0));
            return SyncManager.this.mSyncStorageEngine.insertStartSyncEvent(syncOperation, now);
        }

        public void stopSyncEvent(long rowId, SyncOperation syncOperation, String resultMessage, int upstreamActivity, int downstreamActivity, long elapsedTime) {
            EventLog.writeEvent(2720, syncOperation.toEventLog(1));
            SyncManager.this.mSyncStorageEngine.stopSyncEvent(rowId, elapsedTime, resultMessage, (long) downstreamActivity, (long) upstreamActivity);
        }
    }

    private class SyncTimeTracker {
        boolean mLastWasSyncing;
        private long mTimeSpentSyncing;
        long mWhenSyncStarted;

        /* synthetic */ SyncTimeTracker(SyncManager this$0, SyncTimeTracker -this1) {
            this();
        }

        private SyncTimeTracker() {
            this.mLastWasSyncing = false;
            this.mWhenSyncStarted = 0;
        }

        public synchronized void update() {
            boolean isSyncInProgress = SyncManager.this.mActiveSyncContexts.isEmpty() ^ 1;
            if (isSyncInProgress != this.mLastWasSyncing) {
                long now = SystemClock.elapsedRealtime();
                if (isSyncInProgress) {
                    this.mWhenSyncStarted = now;
                } else {
                    this.mTimeSpentSyncing += now - this.mWhenSyncStarted;
                }
                this.mLastWasSyncing = isSyncInProgress;
            }
        }

        public synchronized long timeSpentSyncing() {
            if (this.mLastWasSyncing) {
                return this.mTimeSpentSyncing + (SystemClock.elapsedRealtime() - this.mWhenSyncStarted);
            }
            return this.mTimeSpentSyncing;
        }
    }

    private class UpdatePeriodicSyncMessagePayload {
        public final Bundle extras;
        public final long flex;
        public final long pollFrequency;
        public final EndPoint target;

        UpdatePeriodicSyncMessagePayload(EndPoint target, long pollFrequency, long flex, Bundle extras) {
            this.target = target;
            this.pollFrequency = pollFrequency;
            this.flex = flex;
            this.extras = extras;
        }
    }

    private boolean isJobIdInUseLockedH(int jobId, List<JobInfo> pendingJobs) {
        for (JobInfo job : pendingJobs) {
            if (job.getId() == jobId) {
                return true;
            }
        }
        for (ActiveSyncContext asc : this.mActiveSyncContexts) {
            if (asc.mSyncOperation.jobId == jobId) {
                return true;
            }
        }
        return false;
    }

    private int getUnusedJobIdH() {
        int newJobId;
        do {
            newJobId = MIN_SYNC_JOB_ID + this.mRand.nextInt(10000);
        } while (isJobIdInUseLockedH(newJobId, this.mJobSchedulerInternal.getSystemScheduledPendingJobs()));
        return newJobId;
    }

    private List<SyncOperation> getAllPendingSyncs() {
        verifyJobScheduler();
        List<JobInfo> pendingJobs = this.mJobSchedulerInternal.getSystemScheduledPendingJobs();
        List<SyncOperation> pendingSyncs = new ArrayList(pendingJobs.size());
        for (JobInfo job : pendingJobs) {
            SyncOperation op = SyncOperation.maybeCreateFromJobExtras(job.getExtras());
            if (op != null) {
                pendingSyncs.add(op);
            }
        }
        return pendingSyncs;
    }

    private List<UserInfo> getAllUsers() {
        return this.mUserManager.getUsers();
    }

    private boolean containsAccountAndUser(AccountAndUser[] accounts, Account account, int userId) {
        int i = 0;
        while (i < accounts.length) {
            if (accounts[i].userId == userId && accounts[i].account.equals(account)) {
                return true;
            }
            i++;
        }
        return false;
    }

    private void updateRunningAccounts(EndPoint target) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "sending MESSAGE_ACCOUNTS_UPDATED");
        }
        Message m = this.mSyncHandler.obtainMessage(9);
        m.obj = target;
        m.sendToTarget();
    }

    private void doDatabaseCleanup() {
        for (UserInfo user : this.mUserManager.getUsers(true)) {
            if (!user.partial) {
                this.mSyncStorageEngine.doDatabaseCleanup(AccountManagerService.getSingleton().getAccounts(user.id, this.mContext.getOpPackageName()), user.id);
            }
        }
    }

    private void clearAllBackoffs() {
        this.mSyncStorageEngine.clearAllBackoffsLocked();
        rescheduleSyncs(EndPoint.USER_ALL_PROVIDER_ALL_ACCOUNTS_ALL);
    }

    private boolean readDataConnectionState() {
        NetworkInfo networkInfo = getConnectivityManager().getActiveNetworkInfo();
        return networkInfo != null ? networkInfo.isConnected() : false;
    }

    private ConnectivityManager getConnectivityManager() {
        ConnectivityManager connectivityManager;
        synchronized (this) {
            if (this.mConnManagerDoNotUseDirectly == null) {
                this.mConnManagerDoNotUseDirectly = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            }
            connectivityManager = this.mConnManagerDoNotUseDirectly;
        }
        return connectivityManager;
    }

    private void cleanupJobs() {
        this.mSyncHandler.postAtFrontOfQueue(new Runnable() {
            public void run() {
                List<SyncOperation> ops = SyncManager.this.getAllPendingSyncs();
                Set<String> cleanedKeys = new HashSet();
                for (SyncOperation opx : ops) {
                    if (!cleanedKeys.contains(opx.key)) {
                        cleanedKeys.add(opx.key);
                        for (SyncOperation opy : ops) {
                            if (opx != opy && opx.key.equals(opy.key)) {
                                SyncManager.this.mJobScheduler.cancel(opy.jobId);
                            }
                        }
                    }
                }
            }
        });
    }

    private synchronized void verifyJobScheduler() {
        if (this.mJobScheduler == null) {
            if (Log.isLoggable("SyncManager", 2)) {
                Log.d("SyncManager", "initializing JobScheduler object.");
            }
            this.mJobScheduler = (JobScheduler) this.mContext.getSystemService("jobscheduler");
            this.mJobSchedulerInternal = (JobSchedulerInternal) LocalServices.getService(JobSchedulerInternal.class);
            for (JobInfo job : this.mJobScheduler.getAllPendingJobs()) {
                SyncOperation op = SyncOperation.maybeCreateFromJobExtras(job.getExtras());
                if (!(op == null || op.isPeriodic)) {
                    this.mSyncStorageEngine.markPending(op.target, true);
                }
            }
            cleanupJobs();
        }
    }

    private JobScheduler getJobScheduler() {
        verifyJobScheduler();
        return this.mJobScheduler;
    }

    public SyncManager(Context context, boolean factoryTest) {
        IntentFilter intentFilter;
        this.mContext = context;
        SyncStorageEngine.init(context);
        this.mSyncStorageEngine = SyncStorageEngine.getSingleton();
        this.mSyncStorageEngine.setOnSyncRequestListener(new OnSyncRequestListener() {
            public void onSyncRequest(EndPoint info, int reason, Bundle extras) {
                SyncManager.this.scheduleSync(info.account, info.userId, reason, info.provider, extras, -2);
            }
        });
        this.mSyncStorageEngine.setPeriodicSyncAddedListener(new PeriodicSyncAddedListener() {
            public void onPeriodicSyncAdded(EndPoint target, Bundle extras, long pollFrequency, long flex) {
                SyncManager.this.updateOrAddPeriodicSync(target, pollFrequency, flex, extras);
            }
        });
        this.mSyncStorageEngine.setOnAuthorityRemovedListener(new OnAuthorityRemovedListener() {
            public void onAuthorityRemoved(EndPoint removedAuthority) {
                SyncManager.this.removeSyncsForAuthority(removedAuthority);
            }
        });
        this.mSyncAdapters = new SyncAdaptersCache(this.mContext);
        this.mSyncHandler = new SyncHandler(BackgroundThread.get().getLooper());
        this.mSyncAdapters.setListener(new RegisteredServicesCacheListener<SyncAdapterType>() {
            public void onServiceChanged(SyncAdapterType type, int userId, boolean removed) {
                if (!removed) {
                    SyncManager.this.scheduleSync(null, -1, -3, type.authority, null, -2);
                }
            }
        }, this.mSyncHandler);
        this.mRand = new Random(System.currentTimeMillis());
        context.registerReceiver(this.mConnectivityIntentReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        if (!factoryTest) {
            intentFilter = new IntentFilter("android.intent.action.BOOT_COMPLETED");
            intentFilter.setPriority(1000);
            context.registerReceiver(this.mBootCompletedReceiver, intentFilter);
        }
        intentFilter = new IntentFilter("android.intent.action.DEVICE_STORAGE_LOW");
        intentFilter.addAction("android.intent.action.DEVICE_STORAGE_OK");
        context.registerReceiver(this.mStorageIntentReceiver, intentFilter);
        intentFilter = new IntentFilter("android.intent.action.ACTION_SHUTDOWN");
        intentFilter.setPriority(100);
        context.registerReceiver(this.mShutdownIntentReceiver, intentFilter);
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        intentFilter.addAction("android.intent.action.USER_STOPPED");
        this.mContext.registerReceiverAsUser(this.mUserIntentReceiver, UserHandle.ALL, intentFilter, null, null);
        if (factoryTest) {
            this.mNotificationMgr = null;
        } else {
            this.mNotificationMgr = (NotificationManager) context.getSystemService("notification");
        }
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mAccountManager = (AccountManager) this.mContext.getSystemService("account");
        this.mAccountManagerInternal = (AccountManagerInternal) LocalServices.getService(AccountManagerInternal.class);
        this.mPackageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mAccountManagerInternal.addOnAppPermissionChangeListener(new -$Lambda$doNli3wDRrwDz12cAoe6lOOQskA(this));
        this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mHandleAlarmWakeLock = this.mPowerManager.newWakeLock(1, HANDLE_SYNC_ALARM_WAKE_LOCK);
        this.mHandleAlarmWakeLock.setReferenceCounted(false);
        this.mSyncManagerWakeLock = this.mPowerManager.newWakeLock(1, SYNC_LOOP_WAKE_LOCK);
        this.mSyncManagerWakeLock.setReferenceCounted(false);
        this.mProvisioned = isDeviceProvisioned();
        if (!this.mProvisioned) {
            final ContentResolver resolver = context.getContentResolver();
            ContentObserver provisionedObserver = new ContentObserver(null) {
                public void onChange(boolean selfChange) {
                    SyncManager syncManager = SyncManager.this;
                    syncManager.mProvisioned = syncManager.mProvisioned | SyncManager.this.isDeviceProvisioned();
                    if (SyncManager.this.mProvisioned) {
                        SyncManager.this.mSyncHandler.onDeviceProvisioned();
                        resolver.unregisterContentObserver(this);
                    }
                }
            };
            synchronized (this.mSyncHandler) {
                resolver.registerContentObserver(Global.getUriFor("device_provisioned"), false, provisionedObserver);
                this.mProvisioned |= isDeviceProvisioned();
                if (this.mProvisioned) {
                    resolver.unregisterContentObserver(provisionedObserver);
                }
            }
        }
        if (!factoryTest) {
            this.mContext.registerReceiverAsUser(this.mAccountsUpdatedReceiver, UserHandle.ALL, new IntentFilter("android.accounts.LOGIN_ACCOUNTS_CHANGED"), null, null);
        }
        final Intent startServiceIntent = new Intent(this.mContext, SyncJobService.class);
        startServiceIntent.putExtra(SyncJobService.EXTRA_MESSENGER, new Messenger(this.mSyncHandler));
        new Handler(this.mContext.getMainLooper()).post(new Runnable() {
            public void run() {
                SyncManager.this.mContext.startService(startServiceIntent);
            }
        });
        whiteListExistingSyncAdaptersIfNeeded();
    }

    /* synthetic */ void lambda$-com_android_server_content_SyncManager_23978(Account account, int uid) {
        if (this.mAccountManagerInternal.hasAccountAccess(account, uid)) {
            scheduleSync(account, UserHandle.getUserId(uid), -2, null, null, 3);
        }
    }

    private void whiteListExistingSyncAdaptersIfNeeded() {
        if (this.mSyncStorageEngine.shouldGrantSyncAdaptersAccountAccess()) {
            List<UserInfo> users = this.mUserManager.getUsers(true);
            int userCount = users.size();
            for (int i = 0; i < userCount; i++) {
                UserHandle userHandle = ((UserInfo) users.get(i)).getUserHandle();
                int userId = userHandle.getIdentifier();
                for (ServiceInfo<SyncAdapterType> service : this.mSyncAdapters.getAllServices(userId)) {
                    String packageName = service.componentName.getPackageName();
                    for (Account account : this.mAccountManager.getAccountsByTypeAsUser(((SyncAdapterType) service.type).accountType, userHandle)) {
                        if (!canAccessAccount(account, packageName, userId)) {
                            this.mAccountManager.updateAppPermission(account, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", service.uid, true);
                        }
                    }
                }
            }
        }
    }

    private boolean isDeviceProvisioned() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return false;
    }

    private long jitterize(long minValue, long maxValue) {
        Random random = new Random(SystemClock.elapsedRealtime());
        long spread = maxValue - minValue;
        if (spread <= 2147483647L) {
            return ((long) random.nextInt((int) spread)) + minValue;
        }
        throw new IllegalArgumentException("the difference between the maxValue and the minValue must be less than 2147483647");
    }

    public SyncStorageEngine getSyncStorageEngine() {
        return this.mSyncStorageEngine;
    }

    private int getIsSyncable(Account account, int userId, String providerName) {
        int isSyncable = this.mSyncStorageEngine.getIsSyncable(account, userId, providerName);
        UserInfo userInfo = UserManager.get(this.mContext).getUserInfo(userId);
        if (userInfo == null || (userInfo.isRestricted() ^ 1) != 0) {
            return isSyncable;
        }
        ServiceInfo<SyncAdapterType> syncAdapterInfo = this.mSyncAdapters.getServiceInfo(SyncAdapterType.newKey(providerName, account.type), userId);
        if (syncAdapterInfo == null) {
            return 0;
        }
        try {
            PackageInfo pInfo = AppGlobals.getPackageManager().getPackageInfo(syncAdapterInfo.componentName.getPackageName(), 0, userId);
            if (pInfo == null || pInfo.restrictedAccountType == null || !pInfo.restrictedAccountType.equals(account.type)) {
                return 0;
            }
            return isSyncable;
        } catch (RemoteException e) {
            return 0;
        }
    }

    private void setAuthorityPendingState(EndPoint info) {
        for (SyncOperation op : getAllPendingSyncs()) {
            if (!op.isPeriodic && op.target.matchesSpec(info)) {
                getSyncStorageEngine().markPending(info, true);
                return;
            }
        }
        getSyncStorageEngine().markPending(info, false);
    }

    public void scheduleSync(Account requestedAccount, int userId, int reason, String requestedAuthority, Bundle extras, int targetSyncState) {
        scheduleSync(requestedAccount, userId, reason, requestedAuthority, extras, targetSyncState, 0);
    }

    private void scheduleSync(Account requestedAccount, int userId, int reason, String requestedAuthority, Bundle extras, int targetSyncState, long minDelayMillis) {
        int i;
        boolean isLoggable = Log.isLoggable("SyncManager", 2);
        if (extras == null) {
            extras = new Bundle();
        }
        if (isLoggable) {
            Log.d("SyncManager", "one-time sync for: " + requestedAccount + " " + extras.toString() + " " + requestedAuthority);
        }
        Object[] accounts = null;
        if (requestedAccount == null) {
            AccountAndUser[] accounts2 = this.mRunningAccounts;
        } else if (userId != -1) {
            accounts2 = new AccountAndUser[]{new AccountAndUser(requestedAccount, userId)};
        } else {
            for (AccountAndUser runningAccount : this.mRunningAccounts) {
                if (requestedAccount.equals(runningAccount.account)) {
                    accounts2 = (AccountAndUser[]) ArrayUtils.appendElement(AccountAndUser.class, accounts2, runningAccount);
                }
            }
        }
        if (ArrayUtils.isEmpty(accounts2)) {
            if (isLoggable) {
                Slog.v("SyncManager", "scheduleSync: no accounts configured, dropping");
            }
            return;
        }
        int source;
        boolean uploadOnly = extras.getBoolean("upload", false);
        boolean manualSync = extras.getBoolean("force", false);
        if (manualSync) {
            extras.putBoolean("ignore_backoff", true);
            extras.putBoolean("ignore_settings", true);
        }
        boolean ignoreSettings = extras.getBoolean("ignore_settings", false);
        if (uploadOnly) {
            source = 1;
        } else if (manualSync) {
            source = 3;
        } else if (requestedAuthority == null) {
            source = 2;
        } else {
            source = 0;
        }
        i = 0;
        int length = accounts2.length;
        while (true) {
            int i2 = i;
            if (i2 < length) {
                AccountAndUser account = accounts2[i2];
                if (userId < 0 || account.userId < 0 || userId == account.userId) {
                    HashSet<String> syncableAuthorities = new HashSet();
                    for (ServiceInfo<SyncAdapterType> syncAdapter : this.mSyncAdapters.getAllServices(account.userId)) {
                        syncableAuthorities.add(((SyncAdapterType) syncAdapter.type).authority);
                    }
                    if (requestedAuthority != null) {
                        boolean hasSyncAdapter = syncableAuthorities.contains(requestedAuthority);
                        syncableAuthorities.clear();
                        if (hasSyncAdapter) {
                            syncableAuthorities.add(requestedAuthority);
                        }
                    }
                    for (String authority : syncableAuthorities) {
                        int isSyncable = computeSyncable(account.account, account.userId, authority);
                        if (isSyncable != 0) {
                            ServiceInfo<SyncAdapterType> syncAdapterInfo = this.mSyncAdapters.getServiceInfo(SyncAdapterType.newKey(authority, account.account.type), account.userId);
                            if (syncAdapterInfo != null) {
                                int owningUid = syncAdapterInfo.uid;
                                if (isSyncable == 3) {
                                    if (isLoggable) {
                                        Slog.v("SyncManager", "    Not scheduling sync operation: isSyncable == SYNCABLE_NO_ACCOUNT_ACCESS");
                                    }
                                    Bundle finalExtras = new Bundle(extras);
                                    String packageName = syncAdapterInfo.componentName.getPackageName();
                                    try {
                                        if (this.mPackageManagerInternal.wasPackageEverLaunched(packageName, userId)) {
                                            this.mAccountManagerInternal.requestAccountAccess(account.account, packageName, userId, new RemoteCallback(new com.android.server.content.-$Lambda$doNli3wDRrwDz12cAoe6lOOQskA.AnonymousClass2(userId, reason, targetSyncState, minDelayMillis, this, account, authority, finalExtras)));
                                        }
                                    } catch (IllegalArgumentException e) {
                                    }
                                } else {
                                    boolean allowParallelSyncs = ((SyncAdapterType) syncAdapterInfo.type).allowParallelSyncs();
                                    boolean isAlwaysSyncable = ((SyncAdapterType) syncAdapterInfo.type).isAlwaysSyncable();
                                    if (isSyncable < 0 && isAlwaysSyncable) {
                                        this.mSyncStorageEngine.setIsSyncable(account.account, account.userId, authority, 1);
                                        isSyncable = 1;
                                    }
                                    if ((targetSyncState == -2 || targetSyncState == isSyncable) && (((SyncAdapterType) syncAdapterInfo.type).supportsUploading() || !uploadOnly)) {
                                        boolean syncAllowed;
                                        if (isSyncable < 0 || ignoreSettings) {
                                            syncAllowed = true;
                                        } else if (this.mSyncStorageEngine.getMasterSyncAutomatically(account.userId)) {
                                            syncAllowed = this.mSyncStorageEngine.getSyncAutomatically(account.account, account.userId, authority);
                                        } else {
                                            syncAllowed = false;
                                        }
                                        if (syncAllowed) {
                                            long delayUntil = this.mSyncStorageEngine.getDelayUntilTime(new EndPoint(account.account, authority, account.userId));
                                            String owningPackage = syncAdapterInfo.componentName.getPackageName();
                                            if (isSyncable == -1) {
                                                Bundle newExtras = new Bundle();
                                                newExtras.putBoolean("initialize", true);
                                                if (isLoggable) {
                                                    Slog.v("SyncManager", "schedule initialisation Sync:, delay until " + delayUntil + ", run by " + 0 + ", flexMillis " + 0 + ", source " + source + ", account " + ", authority " + authority + ", extras " + newExtras);
                                                }
                                                postScheduleSyncMessage(new SyncOperation(account.account, account.userId, owningUid, owningPackage, reason, source, authority, newExtras, allowParallelSyncs), minDelayMillis);
                                            } else if (targetSyncState == -2 || targetSyncState == isSyncable) {
                                                if (isLoggable) {
                                                    Slog.v("SyncManager", "scheduleSync: delay until " + delayUntil + ", source " + source + ", account " + ", authority " + authority + ", extras " + extras);
                                                }
                                                postScheduleSyncMessage(new SyncOperation(account.account, account.userId, owningUid, owningPackage, reason, source, authority, extras, allowParallelSyncs), minDelayMillis);
                                            }
                                        } else if (isLoggable) {
                                            Log.d("SyncManager", "scheduleSync: sync of , " + authority + " is not allowed, dropping request");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_content_SyncManager_40478(AccountAndUser account, int userId, int reason, String authority, Bundle finalExtras, int targetSyncState, long minDelayMillis, Bundle result) {
        if (result != null) {
            if (result.getBoolean("booleanResult")) {
                scheduleSync(account.account, userId, reason, authority, finalExtras, targetSyncState, minDelayMillis);
            }
        }
    }

    private int computeSyncable(Account account, int userId, String authority) {
        return computeSyncable(account, userId, authority, true);
    }

    public int computeSyncable(Account account, int userId, String authority, boolean checkAccountAccess) {
        int status = getIsSyncable(account, userId, authority);
        if (status == 0) {
            return 0;
        }
        ServiceInfo<SyncAdapterType> syncAdapterInfo = this.mSyncAdapters.getServiceInfo(SyncAdapterType.newKey(authority, account.type), userId);
        if (syncAdapterInfo == null) {
            return 0;
        }
        int owningUid = syncAdapterInfo.uid;
        String owningPackage = syncAdapterInfo.componentName.getPackageName();
        try {
            if (ActivityManager.getService().isAppStartModeDisabled(owningUid, owningPackage)) {
                Slog.w("SyncManager", "Not scheduling job " + syncAdapterInfo.uid + ":" + syncAdapterInfo.componentName + " -- package not allowed to start");
                return 0;
            }
        } catch (RemoteException e) {
        }
        if (!checkAccountAccess || (canAccessAccount(account, owningPackage, owningUid) ^ 1) == 0) {
            return status;
        }
        Log.w("SyncManager", "Access to " + account + " denied for package " + owningPackage + " in UID " + syncAdapterInfo.uid);
        return 3;
    }

    private boolean canAccessAccount(Account account, String packageName, int uid) {
        if (this.mAccountManager.hasAccountAccess(account, packageName, UserHandle.getUserHandleForUid(uid))) {
            return true;
        }
        try {
            this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, DumpState.DUMP_DEXOPT, UserHandle.getUserId(uid));
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private void removeSyncsForAuthority(EndPoint info) {
        verifyJobScheduler();
        for (SyncOperation op : getAllPendingSyncs()) {
            if (op.target.matchesSpec(info)) {
                getJobScheduler().cancel(op.jobId);
            }
        }
    }

    public void removePeriodicSync(EndPoint target, Bundle extras) {
        Message m = this.mSyncHandler.obtainMessage(14, target);
        m.setData(extras);
        m.sendToTarget();
    }

    public void updateOrAddPeriodicSync(EndPoint target, long pollFrequency, long flex, Bundle extras) {
        this.mSyncHandler.obtainMessage(13, new UpdatePeriodicSyncMessagePayload(target, pollFrequency, flex, extras)).sendToTarget();
    }

    public List<PeriodicSync> getPeriodicSyncs(EndPoint target) {
        List<SyncOperation> ops = getAllPendingSyncs();
        List<PeriodicSync> periodicSyncs = new ArrayList();
        for (SyncOperation op : ops) {
            if (op.isPeriodic && op.target.matchesSpec(target)) {
                periodicSyncs.add(new PeriodicSync(op.target.account, op.target.provider, op.extras, op.periodMillis / 1000, op.flexMillis / 1000));
            }
        }
        return periodicSyncs;
    }

    public void scheduleLocalSync(Account account, int userId, int reason, String authority) {
        Bundle extras = new Bundle();
        extras.putBoolean("upload", true);
        scheduleSync(account, userId, reason, authority, extras, -2, LOCAL_SYNC_DELAY);
    }

    public SyncAdapterType[] getSyncAdapterTypes(int userId) {
        Collection<ServiceInfo<SyncAdapterType>> serviceInfos = this.mSyncAdapters.getAllServices(userId);
        SyncAdapterType[] types = new SyncAdapterType[serviceInfos.size()];
        int i = 0;
        for (ServiceInfo<SyncAdapterType> serviceInfo : serviceInfos) {
            types[i] = (SyncAdapterType) serviceInfo.type;
            i++;
        }
        return types;
    }

    public String[] getSyncAdapterPackagesForAuthorityAsUser(String authority, int userId) {
        return this.mSyncAdapters.getSyncAdapterPackagesForAuthority(authority, userId);
    }

    private void sendSyncFinishedOrCanceledMessage(ActiveSyncContext syncContext, SyncResult syncResult) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "sending MESSAGE_SYNC_FINISHED");
        }
        Message msg = this.mSyncHandler.obtainMessage();
        msg.what = 1;
        msg.obj = new SyncFinishedOrCancelledMessagePayload(syncContext, syncResult);
        this.mSyncHandler.sendMessage(msg);
    }

    private void sendCancelSyncsMessage(EndPoint info, Bundle extras) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "sending MESSAGE_CANCEL");
        }
        Message msg = this.mSyncHandler.obtainMessage();
        msg.what = 6;
        msg.setData(extras);
        msg.obj = info;
        this.mSyncHandler.sendMessage(msg);
    }

    private void postMonitorSyncProgressMessage(ActiveSyncContext activeSyncContext) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "posting MESSAGE_SYNC_MONITOR in 60s");
        }
        activeSyncContext.mBytesTransferredAtLastPoll = getTotalBytesTransferredByUid(activeSyncContext.mSyncAdapterUid);
        activeSyncContext.mLastPolledTimeElapsed = SystemClock.elapsedRealtime();
        this.mSyncHandler.sendMessageDelayed(this.mSyncHandler.obtainMessage(8, activeSyncContext), 60000);
    }

    private void postScheduleSyncMessage(SyncOperation syncOperation, long minDelayMillis) {
        this.mSyncHandler.obtainMessage(12, new ScheduleSyncMessagePayload(syncOperation, minDelayMillis)).sendToTarget();
    }

    private long getTotalBytesTransferredByUid(int uid) {
        return TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);
    }

    private void clearBackoffSetting(EndPoint target) {
        Pair<Long, Long> backoff = this.mSyncStorageEngine.getBackoff(target);
        if (backoff == null || ((Long) backoff.first).longValue() != -1 || ((Long) backoff.second).longValue() != -1) {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "Clearing backoffs for " + target);
            }
            this.mSyncStorageEngine.setBackoff(target, -1, -1);
            rescheduleSyncs(target);
        }
    }

    private void increaseBackoffSetting(EndPoint target) {
        long now = SystemClock.elapsedRealtime();
        Pair<Long, Long> previousSettings = this.mSyncStorageEngine.getBackoff(target);
        long newDelayInMs = -1;
        if (previousSettings != null) {
            if (now < ((Long) previousSettings.first).longValue()) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "Still in backoff, do not increase it. Remaining: " + ((((Long) previousSettings.first).longValue() - now) / 1000) + " seconds.");
                }
                return;
            }
            newDelayInMs = ((Long) previousSettings.second).longValue() * 2;
        }
        if (newDelayInMs <= 0) {
            newDelayInMs = jitterize(INITIAL_SYNC_RETRY_TIME_IN_MS, 33000);
        }
        long maxSyncRetryTimeInSeconds = Global.getLong(this.mContext.getContentResolver(), "sync_max_retry_delay_in_seconds", DEFAULT_MAX_SYNC_RETRY_TIME_IN_SECONDS);
        if (newDelayInMs > 1000 * maxSyncRetryTimeInSeconds) {
            newDelayInMs = maxSyncRetryTimeInSeconds * 1000;
        }
        long backoff = now + newDelayInMs;
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "Backoff until: " + backoff + ", delayTime: " + newDelayInMs);
        }
        this.mSyncStorageEngine.setBackoff(target, backoff, newDelayInMs);
        rescheduleSyncs(target);
    }

    private void rescheduleSyncs(EndPoint target) {
        int count = 0;
        for (SyncOperation op : getAllPendingSyncs()) {
            if (!op.isPeriodic && op.target.matchesSpec(target)) {
                count++;
                getJobScheduler().cancel(op.jobId);
                postScheduleSyncMessage(op, 0);
            }
        }
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "Rescheduled " + count + " syncs for " + target);
        }
    }

    private void setDelayUntilTime(EndPoint target, long delayUntilSeconds) {
        long newDelayUntilTime;
        long delayUntil = delayUntilSeconds * 1000;
        long absoluteNow = System.currentTimeMillis();
        if (delayUntil > absoluteNow) {
            newDelayUntilTime = SystemClock.elapsedRealtime() + (delayUntil - absoluteNow);
        } else {
            newDelayUntilTime = 0;
        }
        this.mSyncStorageEngine.setDelayUntilTime(target, newDelayUntilTime);
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "Delay Until time set to " + newDelayUntilTime + " for " + target);
        }
        rescheduleSyncs(target);
    }

    private boolean isAdapterDelayed(EndPoint target) {
        long now = SystemClock.elapsedRealtime();
        Pair<Long, Long> backoff = this.mSyncStorageEngine.getBackoff(target);
        if ((backoff == null || ((Long) backoff.first).longValue() == -1 || ((Long) backoff.first).longValue() <= now) && this.mSyncStorageEngine.getDelayUntilTime(target) <= now) {
            return false;
        }
        return true;
    }

    public void cancelActiveSync(EndPoint info, Bundle extras) {
        sendCancelSyncsMessage(info, extras);
    }

    private void scheduleSyncOperationH(SyncOperation syncOperation) {
        scheduleSyncOperationH(syncOperation, 0);
    }

    private void scheduleSyncOperationH(SyncOperation syncOperation, long minDelay) {
        boolean isLoggable = Log.isLoggable("SyncManager", 2);
        if (syncOperation == null) {
            Slog.e("SyncManager", "Can't schedule null sync operation.");
            return;
        }
        if (!syncOperation.ignoreBackoff()) {
            long backoffDelay;
            Pair<Long, Long> backoff = this.mSyncStorageEngine.getBackoff(syncOperation.target);
            if (backoff == null) {
                Slog.e("SyncManager", "Couldn't find backoff values for " + syncOperation.target);
                backoff = new Pair(Long.valueOf(-1), Long.valueOf(-1));
            }
            long now = SystemClock.elapsedRealtime();
            if (((Long) backoff.first).longValue() == -1) {
                backoffDelay = 0;
            } else {
                backoffDelay = ((Long) backoff.first).longValue() - now;
            }
            long delayUntil = this.mSyncStorageEngine.getDelayUntilTime(syncOperation.target);
            long delayUntilDelay = delayUntil > now ? delayUntil - now : 0;
            if (isLoggable) {
                Slog.v("SyncManager", "backoff delay:" + backoffDelay + " delayUntil delay:" + delayUntilDelay);
            }
            minDelay = Math.max(minDelay, Math.max(backoffDelay, delayUntilDelay));
        }
        if (minDelay < 0) {
            minDelay = 0;
        }
        if (!syncOperation.isPeriodic) {
            for (ActiveSyncContext asc : this.mActiveSyncContexts) {
                if (asc.mSyncOperation.key.equals(syncOperation.key)) {
                    if (isLoggable) {
                        Log.v("SyncManager", "Duplicate sync is already running. Not scheduling " + syncOperation);
                    }
                    return;
                }
            }
            int duplicatesCount = 0;
            syncOperation.expectedRuntime = SystemClock.elapsedRealtime() + minDelay;
            List<SyncOperation> pending = getAllPendingSyncs();
            SyncOperation opWithLeastExpectedRuntime = syncOperation;
            for (SyncOperation op : pending) {
                if (!op.isPeriodic && op.key.equals(syncOperation.key)) {
                    if (opWithLeastExpectedRuntime.expectedRuntime > op.expectedRuntime) {
                        opWithLeastExpectedRuntime = op;
                    }
                    duplicatesCount++;
                }
            }
            if (duplicatesCount > 1) {
                Slog.e("SyncManager", "FATAL ERROR! File a bug if you see this.");
            }
            for (SyncOperation op2 : pending) {
                if (!(op2.isPeriodic || !op2.key.equals(syncOperation.key) || op2 == opWithLeastExpectedRuntime)) {
                    if (isLoggable) {
                        Slog.v("SyncManager", "Cancelling duplicate sync " + op2);
                    }
                    getJobScheduler().cancel(op2.jobId);
                }
            }
            if (opWithLeastExpectedRuntime != syncOperation) {
                Slog.i("SyncManager", "Not scheduling because a duplicate exists:" + syncOperation);
                return;
            }
        }
        if (syncOperation.jobId == -1) {
            syncOperation.jobId = getUnusedJobIdH();
        }
        if (isLoggable) {
            Slog.v("SyncManager", "scheduling sync operation " + syncOperation.toString());
        }
        JobInfo.Builder b = new JobInfo.Builder(syncOperation.jobId, new ComponentName(this.mContext, SyncJobService.class)).setExtras(syncOperation.toJobInfoExtras()).setRequiredNetworkType(syncOperation.isNotAllowedOnMetered() ? 2 : 1).setPersisted(true).setPriority(syncOperation.findPriority());
        if (syncOperation.isPeriodic) {
            b.setPeriodic(syncOperation.periodMillis, syncOperation.flexMillis);
        } else {
            if (minDelay > 0) {
                b.setMinimumLatency(minDelay);
            }
            getSyncStorageEngine().markPending(syncOperation.target, true);
        }
        if (syncOperation.extras.getBoolean("require_charging")) {
            b.setRequiresCharging(true);
        }
        getJobScheduler().scheduleAsPackage(b.build(), syncOperation.owningPackage, syncOperation.target.userId, syncOperation.wakeLockName());
    }

    public void clearScheduledSyncOperations(EndPoint info) {
        for (SyncOperation op : getAllPendingSyncs()) {
            if (!op.isPeriodic && op.target.matchesSpec(info)) {
                getJobScheduler().cancel(op.jobId);
                getSyncStorageEngine().markPending(op.target, false);
            }
        }
        this.mSyncStorageEngine.setBackoff(info, -1, -1);
    }

    public void cancelScheduledSyncOperation(EndPoint info, Bundle extras) {
        for (SyncOperation op : getAllPendingSyncs()) {
            if (!op.isPeriodic && op.target.matchesSpec(info) && syncExtrasEquals(extras, op.extras, false)) {
                getJobScheduler().cancel(op.jobId);
            }
        }
        setAuthorityPendingState(info);
        if (!this.mSyncStorageEngine.isSyncPending(info)) {
            this.mSyncStorageEngine.setBackoff(info, -1, -1);
        }
    }

    private void maybeRescheduleSync(SyncResult syncResult, SyncOperation operation) {
        boolean isLoggable = Log.isLoggable("SyncManager", 3);
        if (isLoggable) {
            Log.d("SyncManager", "encountered error(s) during the sync: " + syncResult + ", " + operation);
        }
        if (operation.extras.getBoolean("ignore_backoff", false)) {
            operation.extras.remove("ignore_backoff");
        }
        if (!operation.extras.getBoolean("do_not_retry", false) || (syncResult.syncAlreadyInProgress ^ 1) == 0) {
            if (operation.extras.getBoolean("upload", false) && (syncResult.syncAlreadyInProgress ^ 1) != 0) {
                operation.extras.remove("upload");
                if (isLoggable) {
                    Log.d("SyncManager", "retrying sync operation as a two-way sync because an upload-only sync encountered an error: " + operation);
                }
                scheduleSyncOperationH(operation);
            } else if (syncResult.tooManyRetries) {
                if (isLoggable) {
                    Log.d("SyncManager", "not retrying sync operation because it retried too many times: " + operation);
                }
            } else if (syncResult.madeSomeProgress()) {
                if (isLoggable) {
                    Log.d("SyncManager", "retrying sync operation because even though it had an error it achieved some success");
                }
                scheduleSyncOperationH(operation);
            } else if (syncResult.syncAlreadyInProgress) {
                if (isLoggable) {
                    Log.d("SyncManager", "retrying sync operation that failed because there was already a sync in progress: " + operation);
                }
                scheduleSyncOperationH(operation, 10000);
            } else if (syncResult.hasSoftError()) {
                if (isLoggable) {
                    Log.d("SyncManager", "retrying sync operation because it encountered a soft error: " + operation);
                }
                scheduleSyncOperationH(operation);
            } else {
                Log.d("SyncManager", "not retrying sync operation because the error is a hard error: " + operation);
            }
        } else if (isLoggable) {
            Log.d("SyncManager", "not retrying sync operation because SYNC_EXTRAS_DO_NOT_RETRY was specified " + operation);
        }
    }

    private void onUserUnlocked(int userId) {
        AccountManagerService.getSingleton().validateAccounts(userId);
        this.mSyncAdapters.invalidateCache(userId);
        updateRunningAccounts(new EndPoint(null, null, userId));
        for (Account account : AccountManagerService.getSingleton().getAccounts(userId, this.mContext.getOpPackageName())) {
            scheduleSync(account, userId, -8, null, null, -1);
        }
    }

    private void onUserStopped(int userId) {
        updateRunningAccounts(null);
        cancelActiveSync(new EndPoint(null, null, userId), null);
    }

    private void onUserRemoved(int userId) {
        updateRunningAccounts(null);
        this.mSyncStorageEngine.doDatabaseCleanup(new Account[0], userId);
        for (SyncOperation op : getAllPendingSyncs()) {
            if (op.target.userId == userId) {
                getJobScheduler().cancel(op.jobId);
            }
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        dumpPendingSyncs(pw);
        dumpPeriodicSyncs(pw);
        dumpSyncState(ipw);
        dumpSyncHistory(ipw);
        dumpSyncAdapters(ipw);
    }

    static String formatTime(long time) {
        Time tobj = new Time();
        tobj.set(time);
        return tobj.format("%Y-%m-%d %H:%M:%S");
    }

    protected void dumpPendingSyncs(PrintWriter pw) {
        pw.println("Pending Syncs:");
        int count = 0;
        for (SyncOperation op : getAllPendingSyncs()) {
            if (!op.isPeriodic) {
                pw.println(op.dump(null, false));
                count++;
            }
        }
        pw.println("Total: " + count);
        pw.println();
    }

    protected void dumpPeriodicSyncs(PrintWriter pw) {
        pw.println("Periodic Syncs:");
        int count = 0;
        for (SyncOperation op : getAllPendingSyncs()) {
            if (op.isPeriodic) {
                pw.println(op.dump(null, false));
                count++;
            }
        }
        pw.println("Total: " + count);
        pw.println();
    }

    protected void dumpSyncState(PrintWriter pw) {
        pw.print("data connected: ");
        pw.println(this.mDataConnectionIsConnected);
        pw.print("auto sync: ");
        List<UserInfo> users = getAllUsers();
        if (users != null) {
            for (UserInfo user : users) {
                pw.print("u" + user.id + "=" + this.mSyncStorageEngine.getMasterSyncAutomatically(user.id) + " ");
            }
            pw.println();
        }
        pw.print("memory low: ");
        pw.println(this.mStorageIsLow);
        pw.print("device idle: ");
        pw.println(this.mDeviceIsIdle);
        pw.print("reported active: ");
        pw.println(this.mReportedSyncActive);
        AccountAndUser[] accounts = AccountManagerService.getSingleton().getAllAccounts();
        pw.print("accounts: ");
        if (accounts != INITIAL_ACCOUNTS_ARRAY) {
            pw.println(accounts.length);
        } else {
            pw.println("not known yet");
        }
        long now = SystemClock.elapsedRealtime();
        pw.print("now: ");
        pw.print(now);
        pw.println(" (" + formatTime(System.currentTimeMillis()) + ")");
        pw.println(" (HH:MM:SS)");
        pw.print("uptime: ");
        pw.print(DateUtils.formatElapsedTime(now / 1000));
        pw.println(" (HH:MM:SS)");
        pw.print("time spent syncing: ");
        pw.print(DateUtils.formatElapsedTime(this.mSyncHandler.mSyncTimeTracker.timeSpentSyncing() / 1000));
        pw.print(" (HH:MM:SS), sync ");
        pw.print(this.mSyncHandler.mSyncTimeTracker.mLastWasSyncing ? "" : "not ");
        pw.println("in progress");
        pw.println();
        pw.println("Active Syncs: " + this.mActiveSyncContexts.size());
        PackageManager pm = this.mContext.getPackageManager();
        for (ActiveSyncContext activeSyncContext : this.mActiveSyncContexts) {
            long durationInSeconds = (now - activeSyncContext.mStartTime) / 1000;
            pw.print("  ");
            pw.print(DateUtils.formatElapsedTime(durationInSeconds));
            pw.print(" - ");
            pw.print(activeSyncContext.mSyncOperation.dump(pm, false));
            pw.println();
        }
        pw.println();
        pw.println("Sync Status");
        int i = 0;
        int length = accounts.length;
        while (true) {
            int i2 = i;
            if (i2 < length) {
                AccountAndUser account = accounts[i2];
                pw.printf("Account %s u%d %s\n", new Object[]{"XXXXXXXXX", Integer.valueOf(account.userId), account.account.type});
                pw.println("=======================================================================");
                PrintTable printTable = new PrintTable(13);
                printTable.set(0, 0, "Authority", "Syncable", "Enabled", "Delay", "Loc", "Poll", "Per", "Serv", "User", "Tot", "Time", "Last Sync", "Etc");
                List<ServiceInfo<SyncAdapterType>> sorted = Lists.newArrayList();
                sorted.addAll(this.mSyncAdapters.getAllServices(account.userId));
                Collections.sort(sorted, new Comparator<ServiceInfo<SyncAdapterType>>() {
                    public int compare(ServiceInfo<SyncAdapterType> lhs, ServiceInfo<SyncAdapterType> rhs) {
                        return ((SyncAdapterType) lhs.type).authority.compareTo(((SyncAdapterType) rhs.type).authority);
                    }
                });
                for (ServiceInfo<SyncAdapterType> syncAdapterType : sorted) {
                    if (((SyncAdapterType) syncAdapterType.type).accountType.equals(account.account.type)) {
                        int i3;
                        int row = printTable.getNumRows();
                        Pair<AuthorityInfo, SyncStatusInfo> syncAuthoritySyncStatus = this.mSyncStorageEngine.getCopyOfAuthorityWithSyncStatus(new EndPoint(account.account, ((SyncAdapterType) syncAdapterType.type).authority, account.userId));
                        AuthorityInfo settings = syncAuthoritySyncStatus.first;
                        SyncStatusInfo status = syncAuthoritySyncStatus.second;
                        String authority = settings.target.provider;
                        if (authority.length() > 50) {
                            authority = authority.substring(authority.length() - 50);
                        }
                        printTable.set(row, 0, authority, Integer.valueOf(settings.syncable), Boolean.valueOf(settings.enabled));
                        printTable.set(row, 4, Integer.valueOf(status.numSourceLocal), Integer.valueOf(status.numSourcePoll), Integer.valueOf(status.numSourcePeriodic), Integer.valueOf(status.numSourceServer), Integer.valueOf(status.numSourceUser), Integer.valueOf(status.numSyncs), DateUtils.formatElapsedTime(status.totalElapsedTime / 1000));
                        int row1 = row;
                        if (settings.delayUntil > now) {
                            row1 = row + 1;
                            String[] strArr = new Object[1];
                            strArr[0] = "D: " + ((settings.delayUntil - now) / 1000);
                            printTable.set(row, 12, strArr);
                            if (settings.backoffTime > now) {
                                i3 = row1 + 1;
                                strArr = new Object[1];
                                strArr[0] = "B: " + ((settings.backoffTime - now) / 1000);
                                printTable.set(row1, 12, strArr);
                                row1 = i3 + 1;
                                printTable.set(i3, 12, Long.valueOf(settings.backoffDelay / 1000));
                            }
                        }
                        row1 = row;
                        if (status.lastSuccessTime != 0) {
                            row1 = row + 1;
                            printTable.set(row, 11, SyncStorageEngine.SOURCES[status.lastSuccessSource] + " " + "SUCCESS");
                            i3 = row1 + 1;
                            printTable.set(row1, 11, formatTime(status.lastSuccessTime));
                            row1 = i3;
                        }
                        if (status.lastFailureTime != 0) {
                            i3 = row1 + 1;
                            printTable.set(row1, 11, SyncStorageEngine.SOURCES[status.lastFailureSource] + " " + "FAILURE");
                            row1 = i3 + 1;
                            printTable.set(i3, 11, formatTime(status.lastFailureTime));
                            i3 = row1 + 1;
                            printTable.set(row1, 11, status.lastFailureMesg);
                        }
                    }
                }
                printTable.writeTo(pw);
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    private void dumpTimeSec(PrintWriter pw, long time) {
        pw.print(time / 1000);
        pw.print('.');
        pw.print((time / 100) % 10);
        pw.print('s');
    }

    private void dumpDayStatistic(PrintWriter pw, DayStats ds) {
        pw.print("Success (");
        pw.print(ds.successCount);
        if (ds.successCount > 0) {
            pw.print(" for ");
            dumpTimeSec(pw, ds.successTime);
            pw.print(" avg=");
            dumpTimeSec(pw, ds.successTime / ((long) ds.successCount));
        }
        pw.print(") Failure (");
        pw.print(ds.failureCount);
        if (ds.failureCount > 0) {
            pw.print(" for ");
            dumpTimeSec(pw, ds.failureTime);
            pw.print(" avg=");
            dumpTimeSec(pw, ds.failureTime / ((long) ds.failureCount));
        }
        pw.println(")");
    }

    protected void dumpSyncHistory(PrintWriter pw) {
        dumpRecentHistory(pw);
        dumpDayStatistics(pw);
    }

    private void dumpRecentHistory(PrintWriter pw) {
        ArrayList<SyncHistoryItem> items = this.mSyncStorageEngine.getSyncHistory();
        if (items != null && items.size() > 0) {
            AuthorityInfo authorityInfo;
            SyncHistoryItem item;
            String authorityName;
            String accountKey;
            long elapsedTime;
            AuthoritySyncStats authoritySyncStats;
            int i;
            long eventTime;
            Map<String, AuthoritySyncStats> authorityMap = Maps.newHashMap();
            long totalElapsedTime = 0;
            long totalTimes = 0;
            int N = items.size();
            int maxAuthority = 0;
            int maxAccount = 0;
            for (SyncHistoryItem item2 : items) {
                authorityInfo = this.mSyncStorageEngine.getAuthority(item2.authorityId);
                if (authorityInfo != null) {
                    authorityName = authorityInfo.target.provider;
                    accountKey = authorityInfo.target.account.name + "/" + authorityInfo.target.account.type + " u" + authorityInfo.target.userId;
                } else {
                    authorityName = "Unknown";
                    accountKey = "Unknown";
                }
                int length = authorityName.length();
                if (length > maxAuthority) {
                    maxAuthority = length;
                }
                length = accountKey.length();
                if (length > maxAccount) {
                    maxAccount = length;
                }
                elapsedTime = item2.elapsedTime;
                totalElapsedTime += elapsedTime;
                totalTimes++;
                authoritySyncStats = (AuthoritySyncStats) authorityMap.get(authorityName);
                if (authoritySyncStats == null) {
                    authoritySyncStats = new AuthoritySyncStats(authorityName, null);
                    authorityMap.put(authorityName, authoritySyncStats);
                }
                authoritySyncStats.elapsedTime += elapsedTime;
                authoritySyncStats.times++;
                Map<String, AccountSyncStats> accountMap = authoritySyncStats.accountMap;
                AccountSyncStats accountSyncStats = (AccountSyncStats) accountMap.get(accountKey);
                if (accountSyncStats == null) {
                    accountSyncStats = new AccountSyncStats(accountKey, null);
                    accountMap.put(accountKey, accountSyncStats);
                }
                accountSyncStats.elapsedTime += elapsedTime;
                accountSyncStats.times++;
            }
            if (totalElapsedTime > 0) {
                pw.println();
                pw.printf("Detailed Statistics (Recent history):  %d (# of times) %ds (sync time)\n", new Object[]{Long.valueOf(totalTimes), Long.valueOf(totalElapsedTime / 1000)});
                List<AuthoritySyncStats> arrayList = new ArrayList(authorityMap.values());
                Collections.sort(arrayList, new Comparator<AuthoritySyncStats>() {
                    public int compare(AuthoritySyncStats lhs, AuthoritySyncStats rhs) {
                        int compare = Integer.compare(rhs.times, lhs.times);
                        if (compare == 0) {
                            return Long.compare(rhs.elapsedTime, lhs.elapsedTime);
                        }
                        return compare;
                    }
                });
                char[] chars = new char[((((Math.max(maxAuthority, maxAccount + 3) + 4) + 2) + 10) + 11)];
                Arrays.fill(chars, '-');
                String str = new String(chars);
                String authorityFormat = String.format("  %%-%ds: %%-9s  %%-11s\n", new Object[]{Integer.valueOf(maxLength + 2)});
                String accountFormat = String.format("    %%-%ds:   %%-9s  %%-11s\n", new Object[]{Integer.valueOf(maxLength)});
                pw.println(str);
                for (AuthoritySyncStats authoritySyncStats2 : arrayList) {
                    String name = authoritySyncStats2.name;
                    elapsedTime = authoritySyncStats2.elapsedTime;
                    int times = authoritySyncStats2.times;
                    String timeStr = String.format("%ds/%d%%", new Object[]{Long.valueOf(elapsedTime / 1000), Long.valueOf((100 * elapsedTime) / totalElapsedTime)});
                    String timesStr = String.format("%d/%d%%", new Object[]{Integer.valueOf(times), Long.valueOf(((long) (times * 100)) / totalTimes)});
                    pw.printf(authorityFormat, new Object[]{name, timesStr, timeStr});
                    List<AccountSyncStats> arrayList2 = new ArrayList(authoritySyncStats2.accountMap.values());
                    Collections.sort(arrayList2, new Comparator<AccountSyncStats>() {
                        public int compare(AccountSyncStats lhs, AccountSyncStats rhs) {
                            int compare = Integer.compare(rhs.times, lhs.times);
                            if (compare == 0) {
                                return Long.compare(rhs.elapsedTime, lhs.elapsedTime);
                            }
                            return compare;
                        }
                    });
                    for (AccountSyncStats stats : arrayList2) {
                        elapsedTime = stats.elapsedTime;
                        times = stats.times;
                        timeStr = String.format("%ds/%d%%", new Object[]{Long.valueOf(elapsedTime / 1000), Long.valueOf((100 * elapsedTime) / totalElapsedTime)});
                        timesStr = String.format("%d/%d%%", new Object[]{Integer.valueOf(times), Long.valueOf(((long) (times * 100)) / totalTimes)});
                        pw.printf(accountFormat, new Object[]{"XXXXXXXXX", timesStr, timeStr});
                    }
                    pw.println(str);
                }
            }
            pw.println();
            pw.println("Recent Sync History");
            String format = "  %-" + maxAccount + "s  %-" + maxAuthority + "s %s\n";
            Map<String, Long> lastTimeMap = Maps.newHashMap();
            PackageManager pm = this.mContext.getPackageManager();
            for (i = 0; i < N; i++) {
                String diffString;
                item2 = (SyncHistoryItem) items.get(i);
                authorityInfo = this.mSyncStorageEngine.getAuthority(item2.authorityId);
                if (authorityInfo != null) {
                    authorityName = authorityInfo.target.provider;
                    accountKey = authorityInfo.target.account.name + "/" + authorityInfo.target.account.type + " u" + authorityInfo.target.userId;
                } else {
                    authorityName = "Unknown";
                    accountKey = "Unknown";
                }
                elapsedTime = item2.elapsedTime;
                Time time = new Time();
                eventTime = item2.eventTime;
                time.set(eventTime);
                String key = authorityName + "/" + accountKey;
                Long lastEventTime = (Long) lastTimeMap.get(key);
                if (lastEventTime == null) {
                    diffString = "";
                } else {
                    long diff = (lastEventTime.longValue() - eventTime) / 1000;
                    if (diff < 60) {
                        diffString = String.valueOf(diff);
                    } else if (diff < DEFAULT_MAX_SYNC_RETRY_TIME_IN_SECONDS) {
                        diffString = String.format("%02d:%02d", new Object[]{Long.valueOf(diff / 60), Long.valueOf(diff % 60)});
                    } else {
                        long sec = diff % DEFAULT_MAX_SYNC_RETRY_TIME_IN_SECONDS;
                        diffString = String.format("%02d:%02d:%02d", new Object[]{Long.valueOf(diff / DEFAULT_MAX_SYNC_RETRY_TIME_IN_SECONDS), Long.valueOf(sec / 60), Long.valueOf(sec % 60)});
                    }
                }
                lastTimeMap.put(key, Long.valueOf(eventTime));
                pw.printf("  #%-3d: %s %8s  %5.1fs  %8s", new Object[]{Integer.valueOf(i + 1), formatTime(eventTime), SyncStorageEngine.SOURCES[item2.source], Float.valueOf(((float) elapsedTime) / 1000.0f), diffString});
                String[] strArr = new Object[3];
                strArr[0] = "XXXXXXXXX";
                strArr[1] = authorityName;
                strArr[2] = SyncOperation.reasonToString(pm, item2.reason);
                pw.printf(format, strArr);
                if (!(item2.event == 1 && item2.upstreamActivity == 0 && item2.downstreamActivity == 0)) {
                    pw.printf("    event=%d upstreamActivity=%d downstreamActivity=%d\n", new Object[]{Integer.valueOf(item2.event), Long.valueOf(item2.upstreamActivity), Long.valueOf(item2.downstreamActivity)});
                }
                if (!(item2.mesg == null || (SyncStorageEngine.MESG_SUCCESS.equals(item2.mesg) ^ 1) == 0)) {
                    pw.printf("    mesg=%s\n", new Object[]{item2.mesg});
                }
            }
            pw.println();
            pw.println("Recent Sync History Extras");
            for (i = 0; i < N; i++) {
                item2 = (SyncHistoryItem) items.get(i);
                Bundle extras = item2.extras;
                if (!(extras == null || extras.size() == 0)) {
                    authorityInfo = this.mSyncStorageEngine.getAuthority(item2.authorityId);
                    if (authorityInfo != null) {
                        authorityName = authorityInfo.target.provider;
                        accountKey = authorityInfo.target.account.name + "/" + authorityInfo.target.account.type + " u" + authorityInfo.target.userId;
                    } else {
                        authorityName = "Unknown";
                        accountKey = "Unknown";
                    }
                    new Time().set(item2.eventTime);
                    pw.printf("  #%-3d: %s %8s ", new Object[]{Integer.valueOf(i + 1), formatTime(eventTime), SyncStorageEngine.SOURCES[item2.source]});
                    pw.printf(format, new Object[]{"XXXXXXXXX", authorityName, extras});
                }
            }
        }
    }

    private void dumpDayStatistics(PrintWriter pw) {
        DayStats[] dses = this.mSyncStorageEngine.getDayStatistics();
        if (dses != null && dses[0] != null) {
            DayStats ds;
            pw.println();
            pw.println("Sync Statistics");
            pw.print("  Today:  ");
            dumpDayStatistic(pw, dses[0]);
            int today = dses[0].day;
            int i = 1;
            while (i <= 6 && i < dses.length) {
                ds = dses[i];
                if (ds != null) {
                    int delta = today - ds.day;
                    if (delta > 6) {
                        break;
                    }
                    pw.print("  Day-");
                    pw.print(delta);
                    pw.print(":  ");
                    dumpDayStatistic(pw, ds);
                    i++;
                } else {
                    break;
                }
            }
            int weekDay = today;
            while (i < dses.length) {
                DayStats aggr = null;
                weekDay -= 7;
                while (i < dses.length) {
                    ds = dses[i];
                    if (ds != null) {
                        if (weekDay - ds.day > 6) {
                            break;
                        }
                        i++;
                        if (aggr == null) {
                            aggr = new DayStats(weekDay);
                        }
                        aggr.successCount += ds.successCount;
                        aggr.successTime += ds.successTime;
                        aggr.failureCount += ds.failureCount;
                        aggr.failureTime += ds.failureTime;
                    } else {
                        i = dses.length;
                        break;
                    }
                }
                if (aggr != null) {
                    pw.print("  Week-");
                    pw.print((today - weekDay) / 7);
                    pw.print(": ");
                    dumpDayStatistic(pw, aggr);
                }
            }
        }
    }

    private void dumpSyncAdapters(IndentingPrintWriter pw) {
        pw.println();
        List<UserInfo> users = getAllUsers();
        if (users != null) {
            for (UserInfo user : users) {
                pw.println("Sync adapters for " + user + ":");
                pw.increaseIndent();
                for (ServiceInfo<?> info : this.mSyncAdapters.getAllServices(user.id)) {
                    pw.println(info);
                }
                pw.decreaseIndent();
                pw.println();
            }
        }
    }

    private boolean isSyncStillActiveH(ActiveSyncContext activeSyncContext) {
        for (ActiveSyncContext sync : this.mActiveSyncContexts) {
            if (sync == activeSyncContext) {
                return true;
            }
        }
        return false;
    }

    public static boolean syncExtrasEquals(Bundle b1, Bundle b2, boolean includeSyncSettings) {
        if (b1 == b2) {
            return true;
        }
        if (includeSyncSettings && b1.size() != b2.size()) {
            return false;
        }
        Bundle bigger = b1.size() > b2.size() ? b1 : b2;
        Bundle smaller = b1.size() > b2.size() ? b2 : b1;
        for (String key : bigger.keySet()) {
            if ((includeSyncSettings || !isSyncSetting(key)) && !(smaller.containsKey(key) && Objects.equals(bigger.get(key), smaller.get(key)))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSyncSetting(String key) {
        if (key.equals("expedited") || key.equals("ignore_settings") || key.equals("ignore_backoff") || key.equals("do_not_retry") || key.equals("force") || key.equals("upload") || key.equals("deletions_override") || key.equals("discard_deletions") || key.equals("expected_upload") || key.equals("expected_download") || key.equals("sync_priority") || key.equals("allow_metered") || key.equals("initialize")) {
            return true;
        }
        return false;
    }

    private Context getContextForUser(UserHandle user) {
        try {
            return this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, user);
        } catch (NameNotFoundException e) {
            return this.mContext;
        }
    }
}
