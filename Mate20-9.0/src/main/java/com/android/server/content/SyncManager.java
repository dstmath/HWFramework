package com.android.server.content;

import android.accounts.Account;
import android.accounts.AccountAndUser;
import android.accounts.AccountManager;
import android.accounts.AccountManagerInternal;
import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ISyncAdapter;
import android.content.ISyncAdapterUnsyncableAccountCallback;
import android.content.ISyncContext;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.PeriodicSync;
import android.content.ServiceConnection;
import android.content.SyncActivityTooManyDeletes;
import android.content.SyncAdapterType;
import android.content.SyncAdaptersCache;
import android.content.SyncInfo;
import android.content.SyncResult;
import android.content.SyncStatusInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ProviderInfo;
import android.content.pm.RegisteredServicesCache;
import android.content.pm.RegisteredServicesCacheListener;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.BitmapFactory;
import android.hdm.HwDeviceManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings;
import android.text.format.Time;
import android.util.EventLog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.function.QuadConsumer;
import com.android.server.DeviceIdleController;
import com.android.server.LocalServices;
import com.android.server.accounts.AccountManagerService;
import com.android.server.backup.AccountSyncSettingsBackupHelper;
import com.android.server.content.SyncManager;
import com.android.server.content.SyncStorageEngine;
import com.android.server.job.JobSchedulerInternal;
import com.android.server.pm.DumpState;
import com.android.server.slice.SliceClientPermissions;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class SyncManager extends AbsSyncManager {
    private static final boolean DEBUG_ACCOUNT_ACCESS = false;
    private static final int DELAY_RETRY_SYNC_IN_PROGRESS_IN_SECONDS = 10;
    private static final boolean ENABLE_SUSPICIOUS_CHECK = Build.IS_DEBUGGABLE;
    private static final String HANDLE_SYNC_ALARM_WAKE_LOCK = "SyncManagerHandleSyncAlarm";
    private static final AccountAndUser[] INITIAL_ACCOUNTS_ARRAY = new AccountAndUser[0];
    private static final long LOCAL_SYNC_DELAY = SystemProperties.getLong("sync.local_sync_delay", 30000);
    private static final int MAX_SYNC_JOB_ID = 110000;
    private static final int MIN_SYNC_JOB_ID = 100000;
    private static final int SYNC_ADAPTER_CONNECTION_FLAGS = 21;
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
    @GuardedBy("SyncManager.class")
    private static SyncManager sInstance;
    private static final Comparator<SyncOperation> sOpDumpComparator = $$Lambda$SyncManager$bVs0A6OYdmGkOiq_lbp5MiBwelw.INSTANCE;
    private static final Comparator<SyncOperation> sOpRuntimeComparator = $$Lambda$SyncManager$68MEyNkTh36YmYoFlURJoRa_cY.INSTANCE;
    private final AccountManager mAccountManager;
    /* access modifiers changed from: private */
    public final AccountManagerInternal mAccountManagerInternal;
    private final BroadcastReceiver mAccountsUpdatedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            SyncManager.this.updateRunningAccounts(new SyncStorageEngine.EndPoint(null, null, getSendingUserId()));
        }
    };
    protected final ArrayList<ActiveSyncContext> mActiveSyncContexts = Lists.newArrayList();
    /* access modifiers changed from: private */
    public final IBatteryStats mBatteryStats;
    /* access modifiers changed from: private */
    public volatile boolean mBootCompleted = false;
    private final BroadcastReceiver mBootCompletedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean unused = SyncManager.this.mBootCompleted = true;
            SyncManager.this.verifyJobScheduler();
            SyncManager.this.mSyncHandler.onBootCompleted();
        }
    };
    private ConnectivityManager mConnManagerDoNotUseDirectly;
    private BroadcastReceiver mConnectivityIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean wasConnected = SyncManager.this.mDataConnectionIsConnected;
            boolean unused = SyncManager.this.mDataConnectionIsConnected = SyncManager.this.readDataConnectionState();
            if (SyncManager.this.mDataConnectionIsConnected && !wasConnected) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "Reconnection detected: clearing all backoffs");
                }
                SyncManager.this.clearAllBackoffs("network reconnect");
            }
        }
    };
    /* access modifiers changed from: private */
    public final SyncManagerConstants mConstants;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public volatile boolean mDataConnectionIsConnected = false;
    private volatile boolean mDeviceIsIdle = false;
    private volatile PowerManager.WakeLock mHandleAlarmWakeLock;
    private JobScheduler mJobScheduler;
    private JobSchedulerInternal mJobSchedulerInternal;
    /* access modifiers changed from: private */
    public volatile boolean mJobServiceReady = false;
    /* access modifiers changed from: private */
    public final SyncLogger mLogger;
    /* access modifiers changed from: private */
    public final NotificationManager mNotificationMgr;
    private final BroadcastReceiver mOtherIntentsReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.TIME_SET".equals(intent.getAction())) {
                SyncManager.this.mSyncStorageEngine.setClockValid();
            }
        }
    };
    /* access modifiers changed from: private */
    public final PackageManagerInternal mPackageManagerInternal;
    /* access modifiers changed from: private */
    public final PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public volatile boolean mProvisioned;
    private final Random mRand;
    private volatile boolean mReportedSyncActive = false;
    /* access modifiers changed from: private */
    public volatile AccountAndUser[] mRunningAccounts = INITIAL_ACCOUNTS_ARRAY;
    private BroadcastReceiver mShutdownIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.w("SyncManager", "Writing sync state before shutdown...");
            SyncManager.this.getSyncStorageEngine().writeAllState();
            SyncManager.this.mLogger.log(SyncManager.this.getJobStats());
            SyncManager.this.mLogger.log("Shutting down.");
        }
    };
    private BroadcastReceiver mStorageIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.DEVICE_STORAGE_LOW".equals(action)) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "Internal storage is low.");
                }
                boolean unused = SyncManager.this.mStorageIsLow = true;
                SyncManager.this.cancelActiveSync(SyncStorageEngine.EndPoint.USER_ALL_PROVIDER_ALL_ACCOUNTS_ALL, null, "storage low");
            } else if ("android.intent.action.DEVICE_STORAGE_OK".equals(action)) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "Internal storage is ok.");
                }
                boolean unused2 = SyncManager.this.mStorageIsLow = false;
                SyncManager.this.rescheduleSyncs(SyncStorageEngine.EndPoint.USER_ALL_PROVIDER_ALL_ACCOUNTS_ALL, "storage ok");
            }
        }
    };
    /* access modifiers changed from: private */
    public volatile boolean mStorageIsLow = false;
    protected final SyncAdaptersCache mSyncAdapters;
    /* access modifiers changed from: private */
    public final SyncHandler mSyncHandler;
    /* access modifiers changed from: private */
    public SyncJobService mSyncJobService;
    /* access modifiers changed from: private */
    public volatile PowerManager.WakeLock mSyncManagerWakeLock;
    /* access modifiers changed from: private */
    public SyncStorageEngine mSyncStorageEngine;
    private final HandlerThread mThread;
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

        private AccountSyncStats(String name2) {
            this.name = name2;
        }
    }

    class ActiveSyncContext extends ISyncContext.Stub implements ServiceConnection, IBinder.DeathRecipient {
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
        final PowerManager.WakeLock mSyncWakeLock;
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
            SyncLogger access$1000 = SyncManager.this.mLogger;
            Object[] objArr = new Object[4];
            objArr[0] = "onFinished result=";
            objArr[1] = result;
            objArr[2] = " endpoint=";
            objArr[3] = this.mSyncOperation == null ? "null" : this.mSyncOperation.target;
            access$1000.log(objArr);
            SyncManager.this.sendSyncFinishedOrCanceledMessage(this, result);
        }

        public void toString(StringBuilder sb) {
            sb.append("startTime ");
            sb.append(this.mStartTime);
            sb.append(", mTimeoutStartTime ");
            sb.append(this.mTimeoutStartTime);
            sb.append(", mHistoryRowId ");
            sb.append(this.mHistoryRowId);
            sb.append(", syncOperation ");
            sb.append(this.mSyncOperation);
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

        /* access modifiers changed from: package-private */
        public boolean bindToSyncAdapter(ComponentName serviceComponent, int userId, SyncOperation op) {
            if (Log.isLoggable("SyncManager", 2)) {
                Log.d("SyncManager", "bindToSyncAdapter: " + serviceComponent + ", connection " + this);
            }
            Intent intent = SyncManager.getAdapterBindIntent(SyncManager.this.mContext, serviceComponent, userId);
            this.mBound = true;
            intent.addHwFlags(64);
            if (op.syncSource == 3) {
                intent.addHwFlags(128);
            }
            boolean bindResult = SyncManager.this.mContext.bindServiceAsUser(intent, this, 21, new UserHandle(this.mSyncOperation.target.userId));
            SyncManager.this.mLogger.log("bindService() returned=", Boolean.valueOf(this.mBound), " for ", this);
            if (!bindResult) {
                this.mBound = false;
            } else {
                try {
                    this.mEventName = this.mSyncOperation.wakeLockName();
                    SyncManager.this.mBatteryStats.noteSyncStart(this.mEventName, this.mSyncAdapterUid);
                } catch (RemoteException e) {
                }
            }
            return bindResult;
        }

        /* access modifiers changed from: protected */
        public void close() {
            if (Log.isLoggable("SyncManager", 2)) {
                Log.d("SyncManager", "unBindFromSyncAdapter: connection " + this);
            }
            if (this.mBound) {
                this.mBound = false;
                SyncManager.this.mLogger.log("unbindService for ", this);
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

        private AuthoritySyncStats(String name2) {
            this.accountMap = Maps.newHashMap();
            this.name = name2;
        }
    }

    interface OnReadyCallback {
        void onReady();
    }

    private static class OnUnsyncableAccountCheck implements ServiceConnection {
        static final long SERVICE_BOUND_TIME_MILLIS = 5000;
        private final OnReadyCallback mOnReadyCallback;
        private final RegisteredServicesCache.ServiceInfo<SyncAdapterType> mSyncAdapterInfo;

        OnUnsyncableAccountCheck(RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo, OnReadyCallback onReadyCallback) {
            this.mSyncAdapterInfo = syncAdapterInfo;
            this.mOnReadyCallback = onReadyCallback;
        }

        /* access modifiers changed from: private */
        public void onReady() {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mOnReadyCallback.onReady();
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                ISyncAdapter.Stub.asInterface(service).onUnsyncableAccount(new ISyncAdapterUnsyncableAccountCallback.Stub() {
                    public void onUnsyncableAccountDone(boolean isReady) {
                        if (isReady) {
                            OnUnsyncableAccountCheck.this.onReady();
                        }
                    }
                });
            } catch (RemoteException e) {
                Slog.e("SyncManager", "Could not call onUnsyncableAccountDone " + this.mSyncAdapterInfo, e);
                onReady();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    static class PrintTable {
        private final int mCols;
        private ArrayList<String[]> mTable = Lists.newArrayList();

        PrintTable(int cols) {
            this.mCols = cols;
        }

        /* access modifiers changed from: package-private */
        public void set(int row, int col, Object... values) {
            int j;
            if (values.length + col <= this.mCols) {
                int i = this.mTable.size();
                while (true) {
                    j = 0;
                    if (i > row) {
                        break;
                    }
                    String[] list = new String[this.mCols];
                    this.mTable.add(list);
                    while (j < this.mCols) {
                        list[j] = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                        j++;
                    }
                    i++;
                }
                String[] rowArray = this.mTable.get(row);
                while (j < values.length) {
                    Object value = values[j];
                    rowArray[col + j] = value == null ? BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS : value.toString();
                    j++;
                }
                return;
            }
            throw new IndexOutOfBoundsException("Table only has " + this.mCols + " columns. can't set " + values.length + " at column " + col);
        }

        /* access modifiers changed from: package-private */
        public void writeTo(PrintWriter out) {
            String[] formats = new String[this.mCols];
            int i = 0;
            int totalLength = 0;
            for (int col = 0; col < this.mCols; col++) {
                int maxLength = 0;
                Iterator<String[]> it = this.mTable.iterator();
                while (it.hasNext()) {
                    int length = ((Object[]) it.next())[col].toString().length();
                    if (length > maxLength) {
                        maxLength = length;
                    }
                }
                totalLength += maxLength;
                formats[col] = String.format("%%-%ds", new Object[]{Integer.valueOf(maxLength)});
            }
            formats[this.mCols - 1] = "%s";
            printRow(out, formats, (Object[]) this.mTable.get(0));
            int totalLength2 = totalLength + ((this.mCols - 1) * 2);
            while (true) {
                int i2 = i;
                if (i2 >= totalLength2) {
                    break;
                }
                out.print("-");
                i = i2 + 1;
            }
            out.println();
            int mTableSize = this.mTable.size();
            for (int i3 = 1; i3 < mTableSize; i3++) {
                printRow(out, formats, (Object[]) this.mTable.get(i3));
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

        ScheduleSyncMessagePayload(SyncOperation syncOperation2, long minDelayMillis2) {
            this.syncOperation = syncOperation2;
            this.minDelayMillis = minDelayMillis2;
        }
    }

    class ServiceConnectionData {
        public final ActiveSyncContext activeSyncContext;
        public final IBinder adapter;

        ServiceConnectionData(ActiveSyncContext activeSyncContext2, IBinder adapter2) {
            this.activeSyncContext = activeSyncContext2;
            this.adapter = adapter2;
        }
    }

    private class SyncFinishedOrCancelledMessagePayload {
        public final ActiveSyncContext activeSyncContext;
        public final SyncResult syncResult;

        SyncFinishedOrCancelledMessagePayload(ActiveSyncContext syncContext, SyncResult syncResult2) {
            this.activeSyncContext = syncContext;
            this.syncResult = syncResult2;
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
        public final SyncTimeTracker mSyncTimeTracker = new SyncTimeTracker();
        private List<Message> mUnreadyQueue = new ArrayList();
        private final HashMap<String, PowerManager.WakeLock> mWakeLocks = Maps.newHashMap();

        /* access modifiers changed from: package-private */
        public void onBootCompleted() {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "Boot completed.");
            }
            checkIfDeviceReady();
        }

        /* access modifiers changed from: package-private */
        public void onDeviceProvisioned() {
            if (Log.isLoggable("SyncManager", 3)) {
                Log.d("SyncManager", "mProvisioned=" + SyncManager.this.mProvisioned);
            }
            checkIfDeviceReady();
        }

        /* access modifiers changed from: package-private */
        public void checkIfDeviceReady() {
            if (SyncManager.this.mProvisioned && SyncManager.this.mBootCompleted && SyncManager.this.mJobServiceReady) {
                synchronized (this) {
                    SyncManager.this.mSyncStorageEngine.restoreAllPeriodicSyncs();
                    obtainMessage(2).sendToTarget();
                }
            }
        }

        private boolean tryEnqueueMessageUntilReadyToRun(Message msg) {
            synchronized (this) {
                if (SyncManager.this.mBootCompleted && SyncManager.this.mProvisioned) {
                    if (SyncManager.this.mJobServiceReady) {
                        return false;
                    }
                }
                if (SyncManager.this.mProvisioned || !(msg.obj instanceof SyncOperation)) {
                    this.mUnreadyQueue.add(Message.obtain(msg));
                } else {
                    deferSyncH((SyncOperation) msg.obj, 600000, "delay on provision");
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
                    SyncJobService unused = SyncManager.this.mSyncJobService = (SyncJobService) msg.obj;
                    boolean unused2 = SyncManager.this.mJobServiceReady = true;
                    checkIfDeviceReady();
                } else if (msg.what == 9) {
                    if (Log.isLoggable("SyncManager", 2)) {
                        Slog.v("SyncManager", "handleSyncHandlerMessage: MESSAGE_ACCOUNTS_UPDATED");
                    }
                    updateRunningAccountsH((SyncStorageEngine.EndPoint) msg.obj);
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
            } finally {
                SyncManager.this.mSyncManagerWakeLock.release();
            }
        }

        private void handleSyncMessage(Message msg) {
            boolean isLoggable = Log.isLoggable("SyncManager", 2);
            try {
                boolean unused = SyncManager.this.mDataConnectionIsConnected = SyncManager.this.readDataConnectionState();
                boolean z = true;
                switch (msg.what) {
                    case 1:
                        SyncFinishedOrCancelledMessagePayload payload = (SyncFinishedOrCancelledMessagePayload) msg.obj;
                        if (SyncManager.this.isSyncStillActiveH(payload.activeSyncContext)) {
                            if (isLoggable) {
                                Slog.v("SyncManager", "syncFinished" + payload.activeSyncContext.mSyncOperation);
                            }
                            SyncManager.this.mSyncJobService.callJobFinished(payload.activeSyncContext.mSyncOperation.jobId, false, "sync finished");
                            runSyncFinishedOrCanceledH(payload.syncResult, payload.activeSyncContext);
                            break;
                        } else {
                            Log.d("SyncManager", "handleSyncHandlerMessage: dropping since the sync is no longer active: " + payload.activeSyncContext);
                            break;
                        }
                    case 4:
                        ServiceConnectionData msgData = (ServiceConnectionData) msg.obj;
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
                            if (currentSyncContext.mSyncAdapter != null) {
                                SyncManager.this.mLogger.log("Calling cancelSync for SERVICE_DISCONNECTED ", currentSyncContext, " adapter=", currentSyncContext.mSyncAdapter);
                                currentSyncContext.mSyncAdapter.cancelSync(currentSyncContext);
                                SyncManager.this.mLogger.log("Canceled");
                            }
                            SyncResult syncResult = new SyncResult();
                            syncResult.stats.numIoExceptions++;
                            SyncManager.this.mSyncJobService.callJobFinished(currentSyncContext.mSyncOperation.jobId, false, "service disconnected");
                            runSyncFinishedOrCanceledH(syncResult, currentSyncContext);
                            break;
                        }
                        break;
                    case 6:
                        SyncStorageEngine.EndPoint endpoint = (SyncStorageEngine.EndPoint) msg.obj;
                        Bundle extras = msg.peekData();
                        if (Log.isLoggable("SyncManager", 3)) {
                            Log.d("SyncManager", "handleSyncHandlerMessage: MESSAGE_CANCEL: " + endpoint + " bundle: " + extras);
                        }
                        cancelActiveSyncH(endpoint, extras, "MESSAGE_CANCEL");
                        break;
                    case 8:
                        ActiveSyncContext monitoredSyncContext = (ActiveSyncContext) msg.obj;
                        if (Log.isLoggable("SyncManager", 3)) {
                            Log.d("SyncManager", "handleSyncHandlerMessage: MESSAGE_MONITOR_SYNC: " + monitoredSyncContext.mSyncOperation.target);
                        }
                        if (!isSyncNotUsingNetworkH(monitoredSyncContext)) {
                            SyncManager.this.postMonitorSyncProgressMessage(monitoredSyncContext);
                            break;
                        } else {
                            Log.w("SyncManager", String.format("Detected sync making no progress for %s. cancelling.", new Object[]{monitoredSyncContext}));
                            SyncManager.this.mSyncJobService.callJobFinished(monitoredSyncContext.mSyncOperation.jobId, false, "no network activity");
                            runSyncFinishedOrCanceledH(null, monitoredSyncContext);
                            break;
                        }
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
                            if (msg.arg2 == 0) {
                                z = false;
                            }
                            boolean applyBackoff = z;
                            if (isLoggable) {
                                Slog.v("SyncManager", "Stopping sync. Reschedule: " + reschedule + "Backoff: " + applyBackoff);
                            }
                            if (applyBackoff) {
                                SyncManager.this.increaseBackoffSetting(op.target);
                            }
                            if (reschedule) {
                                deferStoppedSyncH(op, 0);
                            }
                            break;
                        }
                        break;
                    case 12:
                        ScheduleSyncMessagePayload syncPayload = (ScheduleSyncMessagePayload) msg.obj;
                        SyncManager.this.scheduleSyncOperationH(syncPayload.syncOperation, syncPayload.minDelayMillis);
                        break;
                    case 13:
                        UpdatePeriodicSyncMessagePayload data = (UpdatePeriodicSyncMessagePayload) msg.obj;
                        updateOrAddPeriodicSyncH(data.target, data.pollFrequency, data.flex, data.extras);
                        break;
                    case 14:
                        Pair<SyncStorageEngine.EndPoint, String> args = (Pair) msg.obj;
                        removePeriodicSyncH((SyncStorageEngine.EndPoint) args.first, msg.getData(), (String) args.second);
                        break;
                }
            } catch (RemoteException e) {
                SyncManager.this.mLogger.log("RemoteException ", Log.getStackTraceString(e));
            } catch (Throwable th) {
                this.mSyncTimeTracker.update();
                throw th;
            }
            this.mSyncTimeTracker.update();
        }

        /* access modifiers changed from: private */
        public PowerManager.WakeLock getSyncWakeLock(SyncOperation operation) {
            String wakeLockKey = operation.wakeLockName();
            PowerManager.WakeLock wakeLock = this.mWakeLocks.get(wakeLockKey);
            if (wakeLock != null) {
                return wakeLock;
            }
            PowerManager.WakeLock wakeLock2 = SyncManager.this.mPowerManager.newWakeLock(1, SyncManager.SYNC_WAKE_LOCK_PREFIX + wakeLockKey);
            wakeLock2.setReferenceCounted(false);
            this.mWakeLocks.put(wakeLockKey, wakeLock2);
            return wakeLock2;
        }

        private void deferSyncH(SyncOperation op, long delay, String why) {
            SyncLogger access$1000 = SyncManager.this.mLogger;
            Object[] objArr = new Object[8];
            objArr[0] = "deferSyncH() ";
            objArr[1] = op.isPeriodic ? "periodic " : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            objArr[2] = "sync.  op=";
            objArr[3] = op;
            objArr[4] = " delay=";
            objArr[5] = Long.valueOf(delay);
            objArr[6] = " why=";
            objArr[7] = why;
            access$1000.log(objArr);
            SyncManager.this.mSyncJobService.callJobFinished(op.jobId, false, why);
            if (op.isPeriodic) {
                SyncManager.this.scheduleSyncOperationH(op.createOneTimeSyncOperation(), delay);
                return;
            }
            SyncManager.this.cancelJob(op, "deferSyncH()");
            SyncManager.this.scheduleSyncOperationH(op, delay);
        }

        private void deferStoppedSyncH(SyncOperation op, long delay) {
            if (op.isPeriodic) {
                SyncManager.this.scheduleSyncOperationH(op.createOneTimeSyncOperation(), delay);
            } else {
                SyncManager.this.scheduleSyncOperationH(op, delay);
            }
        }

        private void deferActiveSyncH(ActiveSyncContext asc, String why) {
            SyncOperation op = asc.mSyncOperation;
            runSyncFinishedOrCanceledH(null, asc);
            deferSyncH(op, 10000, why);
        }

        private void startSyncH(SyncOperation op) {
            boolean isLoggable = Log.isLoggable("SyncManager", 2);
            if (isLoggable) {
                Slog.v("SyncManager", op.toString());
            }
            SyncManager.this.mSyncStorageEngine.setClockValid();
            SyncManager.this.mSyncJobService.markSyncStarted(op.jobId);
            if (SyncManager.this.mStorageIsLow) {
                deferSyncH(op, 3600000, "storage low");
                return;
            }
            if (op.isPeriodic) {
                for (SyncOperation syncOperation : SyncManager.this.getAllPendingSyncs()) {
                    if (syncOperation.sourcePeriodicId == op.jobId) {
                        SyncManager.this.mSyncJobService.callJobFinished(op.jobId, false, "periodic sync, pending");
                        return;
                    }
                }
                Iterator<ActiveSyncContext> it = SyncManager.this.mActiveSyncContexts.iterator();
                while (it.hasNext()) {
                    if (it.next().mSyncOperation.sourcePeriodicId == op.jobId) {
                        SyncManager.this.mSyncJobService.callJobFinished(op.jobId, false, "periodic sync, already running");
                        return;
                    }
                }
                if (SyncManager.this.isAdapterDelayed(op.target)) {
                    deferSyncH(op, 0, "backing off");
                    return;
                }
            }
            Iterator<ActiveSyncContext> it2 = SyncManager.this.mActiveSyncContexts.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                ActiveSyncContext asc = it2.next();
                if (asc.mSyncOperation.isConflict(op)) {
                    if (asc.mSyncOperation.findPriority() >= op.findPriority()) {
                        if (isLoggable) {
                            Slog.v("SyncManager", "Rescheduling sync due to conflict " + op.toString());
                        }
                        deferSyncH(op, 10000, "delay on conflict");
                        return;
                    }
                    if (isLoggable) {
                        Slog.v("SyncManager", "Pushing back running sync due to a higher priority sync");
                    }
                    deferActiveSyncH(asc, "preempted");
                }
            }
            int syncOpState = computeSyncOpState(op);
            switch (syncOpState) {
                case 1:
                case 2:
                    SyncJobService access$3000 = SyncManager.this.mSyncJobService;
                    int i = op.jobId;
                    access$3000.callJobFinished(i, false, "invalid op state: " + syncOpState);
                    return;
                default:
                    if (!dispatchSyncOperation(op)) {
                        SyncManager.this.mSyncJobService.callJobFinished(op.jobId, false, "dispatchSyncOperation() failed");
                    }
                    SyncManager.this.setAuthorityPendingState(op.target);
                    return;
            }
        }

        private ActiveSyncContext findActiveSyncContextH(int jobId) {
            Iterator<ActiveSyncContext> it = SyncManager.this.mActiveSyncContexts.iterator();
            while (it.hasNext()) {
                ActiveSyncContext asc = it.next();
                SyncOperation op = asc.mSyncOperation;
                if (op != null && op.jobId == jobId) {
                    return asc;
                }
            }
            return null;
        }

        private void updateRunningAccountsH(SyncStorageEngine.EndPoint syncTargets) {
            SyncStorageEngine.EndPoint endPoint = syncTargets;
            AccountAndUser[] oldAccounts = SyncManager.this.mRunningAccounts;
            AccountAndUser[] unused = SyncManager.this.mRunningAccounts = AccountManagerService.getSingleton().getRunningAccounts();
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "Accounts list: ");
                for (AccountAndUser acc : SyncManager.this.mRunningAccounts) {
                    Slog.v("SyncManager", acc.toString());
                }
            }
            if (SyncManager.this.mLogger.enabled()) {
                SyncManager.this.mLogger.log("updateRunningAccountsH: ", Arrays.toString(SyncManager.this.mRunningAccounts));
            }
            if (SyncManager.this.mBootCompleted) {
                SyncManager.this.doDatabaseCleanup();
            }
            AccountAndUser[] accounts = SyncManager.this.mRunningAccounts;
            Iterator<ActiveSyncContext> it = new ArrayList<>(SyncManager.this.mActiveSyncContexts).iterator();
            while (it.hasNext()) {
                ActiveSyncContext currentSyncContext = it.next();
                if (!SyncManager.this.containsAccountAndUser(accounts, currentSyncContext.mSyncOperation.target.account, currentSyncContext.mSyncOperation.target.userId)) {
                    Log.d("SyncManager", "canceling sync since the account is no longer running");
                    SyncManager.this.sendSyncFinishedOrCanceledMessage(currentSyncContext, null);
                }
            }
            AccountAndUser[] access$3800 = SyncManager.this.mRunningAccounts;
            int length = access$3800.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                AccountAndUser aau = access$3800[i];
                if (!SyncManager.this.containsAccountAndUser(oldAccounts, aau.account, aau.userId)) {
                    if (Log.isLoggable("SyncManager", 3)) {
                        Log.d("SyncManager", "Account " + aau.account + " added, checking sync restore data");
                    }
                    AccountSyncSettingsBackupHelper.accountAdded(SyncManager.this.mContext);
                } else {
                    i++;
                }
            }
            AccountAndUser[] allAccounts = AccountManagerService.getSingleton().getAllAccounts();
            for (SyncOperation op : SyncManager.this.getAllPendingSyncs()) {
                if (!SyncManager.this.containsAccountAndUser(allAccounts, op.target.account, op.target.userId)) {
                    SyncManager.this.mLogger.log("canceling: ", op);
                    SyncManager.this.cancelJob(op, "updateRunningAccountsH()");
                }
            }
            if (endPoint != null) {
                SyncManager.this.scheduleSync(endPoint.account, endPoint.userId, -2, endPoint.provider, null, -1, 0);
            }
        }

        private void maybeUpdateSyncPeriodH(SyncOperation syncOperation, long pollFrequencyMillis, long flexMillis) {
            if (pollFrequencyMillis != syncOperation.periodMillis || flexMillis != syncOperation.flexMillis) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "updating period " + syncOperation + " to " + pollFrequencyMillis + " and flex to " + flexMillis);
                }
                SyncOperation syncOperation2 = new SyncOperation(syncOperation, pollFrequencyMillis, flexMillis);
                syncOperation2.jobId = syncOperation.jobId;
                SyncManager.this.scheduleSyncOperationH(syncOperation2);
            }
        }

        private void updateOrAddPeriodicSyncH(SyncStorageEngine.EndPoint target, long pollFrequency, long flex, Bundle extras) {
            SyncStorageEngine.EndPoint endPoint = target;
            long j = pollFrequency;
            long j2 = flex;
            if (endPoint.account != null) {
                boolean isLoggable = Log.isLoggable("SyncManager", 2);
                SyncManager.this.verifyJobScheduler();
                long pollFrequencyMillis = j * 1000;
                long flexMillis = j2 * 1000;
                if (isLoggable) {
                    Slog.v("SyncManager", "Addition to periodic syncs requested: " + endPoint + " period: " + j + " flexMillis: " + j2 + " extras: " + extras.toString());
                }
                List<SyncOperation> ops = SyncManager.this.getAllPendingSyncs();
                for (SyncOperation op : ops) {
                    if (!op.isPeriodic || !op.target.matchesSpec(endPoint)) {
                        Bundle bundle = extras;
                    } else if (SyncManager.syncExtrasEquals(op.extras, extras, true)) {
                        maybeUpdateSyncPeriodH(op, pollFrequencyMillis, flexMillis);
                        return;
                    }
                }
                Bundle bundle2 = extras;
                if (isLoggable) {
                    Slog.v("SyncManager", "Adding new periodic sync: " + endPoint + " period: " + j + " flexMillis: " + j2 + " extras: " + extras.toString());
                }
                RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo = SyncManager.this.mSyncAdapters.getServiceInfo(SyncAdapterType.newKey(endPoint.provider, endPoint.account.type), endPoint.userId);
                if (syncAdapterInfo != null) {
                    SyncOperation op2 = new SyncOperation(endPoint, syncAdapterInfo.uid, syncAdapterInfo.componentName.getPackageName(), -4, 4, extras, ((SyncAdapterType) syncAdapterInfo.type).allowParallelSyncs(), true, -1, pollFrequencyMillis, flexMillis, 0);
                    int syncOpState = computeSyncOpState(op2);
                    switch (syncOpState) {
                        case 1:
                            List<SyncOperation> list = ops;
                            int i = syncOpState;
                            return;
                        case 2:
                            String packageName = op2.owningPackage;
                            int userId = UserHandle.getUserId(op2.owningUid);
                            if (SyncManager.this.mPackageManagerInternal.wasPackageEverLaunched(packageName, userId)) {
                                AccountManagerInternal access$4300 = SyncManager.this.mAccountManagerInternal;
                                Account account = op2.target.account;
                                int i2 = syncOpState;
                                RemoteCallback.OnResultListener onResultListener = r0;
                                RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo = syncAdapterInfo;
                                List<SyncOperation> list2 = ops;
                                RemoteCallback.OnResultListener r0 = new RemoteCallback.OnResultListener(endPoint, pollFrequency, flex, extras) {
                                    private final /* synthetic */ SyncStorageEngine.EndPoint f$1;
                                    private final /* synthetic */ long f$2;
                                    private final /* synthetic */ long f$3;
                                    private final /* synthetic */ Bundle f$4;

                                    {
                                        this.f$1 = r2;
                                        this.f$2 = r3;
                                        this.f$3 = r5;
                                        this.f$4 = r7;
                                    }

                                    public final void onResult(Bundle bundle) {
                                        SyncManager.SyncHandler.lambda$updateOrAddPeriodicSyncH$0(SyncManager.SyncHandler.this, this.f$1, this.f$2, this.f$3, this.f$4, bundle);
                                    }
                                };
                                access$4300.requestAccountAccess(account, packageName, userId, new RemoteCallback(onResultListener));
                                return;
                            }
                            return;
                        default:
                            RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo2 = syncAdapterInfo;
                            List<SyncOperation> list3 = ops;
                            int i3 = syncOpState;
                            SyncManager.this.scheduleSyncOperationH(op2);
                            SyncManager.this.mSyncStorageEngine.reportChange(1);
                            return;
                    }
                }
            }
        }

        public static /* synthetic */ void lambda$updateOrAddPeriodicSyncH$0(SyncHandler syncHandler, SyncStorageEngine.EndPoint target, long pollFrequency, long flex, Bundle extras, Bundle result) {
            if (result != null && result.getBoolean("booleanResult")) {
                SyncManager.this.updateOrAddPeriodicSync(target, pollFrequency, flex, extras);
            }
        }

        private void removePeriodicSyncInternalH(SyncOperation syncOperation, String why) {
            for (SyncOperation op : SyncManager.this.getAllPendingSyncs()) {
                if (op.sourcePeriodicId == syncOperation.jobId || op.jobId == syncOperation.jobId) {
                    ActiveSyncContext asc = findActiveSyncContextH(syncOperation.jobId);
                    if (asc != null) {
                        SyncManager.this.mSyncJobService.callJobFinished(syncOperation.jobId, false, "removePeriodicSyncInternalH");
                        runSyncFinishedOrCanceledH(null, asc);
                    }
                    SyncManager.this.mLogger.log("removePeriodicSyncInternalH-canceling: ", op);
                    SyncManager.this.cancelJob(op, why);
                }
            }
        }

        private void removePeriodicSyncH(SyncStorageEngine.EndPoint target, Bundle extras, String why) {
            SyncManager.this.verifyJobScheduler();
            for (SyncOperation op : SyncManager.this.getAllPendingSyncs()) {
                if (op.isPeriodic && op.target.matchesSpec(target) && SyncManager.syncExtrasEquals(op.extras, extras, true)) {
                    removePeriodicSyncInternalH(op, why);
                }
            }
        }

        private boolean isSyncNotUsingNetworkH(ActiveSyncContext activeSyncContext) {
            boolean z;
            boolean z2;
            ActiveSyncContext activeSyncContext2 = activeSyncContext;
            long bytesTransferredCurrent = SyncManager.this.getTotalBytesTransferredByUid(activeSyncContext2.mSyncAdapterUid);
            long deltaBytesTransferred = bytesTransferredCurrent - activeSyncContext2.mBytesTransferredAtLastPoll;
            if (Log.isLoggable("SyncManager", 3)) {
                long remainder = deltaBytesTransferred;
                long mb = remainder / 1048576;
                long remainder2 = remainder % 1048576;
                long j = bytesTransferredCurrent;
                z2 = false;
                z = true;
                Log.d("SyncManager", String.format("Time since last update: %ds. Delta transferred: %dMBs,%dKBs,%dBs", new Object[]{Long.valueOf((SystemClock.elapsedRealtime() - activeSyncContext2.mLastPolledTimeElapsed) / 1000), Long.valueOf(mb), Long.valueOf(remainder2 / 1024), Long.valueOf(remainder2 % 1024)}));
            } else {
                z2 = false;
                z = true;
            }
            return deltaBytesTransferred <= 10 ? z : z2;
        }

        private int computeSyncOpState(SyncOperation op) {
            boolean isLoggable = Log.isLoggable("SyncManager", 2);
            SyncStorageEngine.EndPoint target = op.target;
            if (!SyncManager.this.containsAccountAndUser(SyncManager.this.mRunningAccounts, target.account, target.userId)) {
                if (isLoggable) {
                    Slog.v("SyncManager", "    Dropping sync operation: account doesn't exist.");
                }
                return 1;
            }
            int state = SyncManager.this.computeSyncable(target.account, target.userId, target.provider, true);
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
                boolean syncEnabled = SyncManager.this.mSyncStorageEngine.getMasterSyncAutomatically(target.userId) && SyncManager.this.mSyncStorageEngine.getSyncAutomatically(target.account, target.userId, target.provider);
                boolean ignoreSystemConfiguration = op.isIgnoreSettings() || state < 0;
                if (syncEnabled || ignoreSystemConfiguration) {
                    if (HwDeviceManager.disallowOp(42) && !op.isManual() && state >= 0) {
                        NetworkInfo networkInfo = SyncManager.this.getConnectivityManager().getActiveNetworkInfo();
                        if (networkInfo != null && networkInfo.isRoaming() && networkInfo.getType() == 0) {
                            Slog.v("SyncManager", "    Dropping auto sync operation for " + target.provider + ": disallowed by MDM disable auto sync when roaming.");
                            return 1;
                        }
                    }
                    if (target.account == null || !HwDeviceManager.disallowOp(25, target.account.type) || op.isManual() || state < 0) {
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

        private boolean dispatchSyncOperation(SyncOperation op) {
            SyncOperation syncOperation = op;
            Slog.i("SyncManager", "dispatchSyncOperation:we are going to sync " + syncOperation);
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "num active syncs: " + SyncManager.this.mActiveSyncContexts.size());
                Iterator<ActiveSyncContext> it = SyncManager.this.mActiveSyncContexts.iterator();
                while (it.hasNext()) {
                    Slog.v("SyncManager", it.next().toString());
                }
            }
            if (op.isAppStandbyExempted()) {
                UsageStatsManagerInternal usmi = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
                if (usmi != null) {
                    usmi.reportExemptedSyncStart(syncOperation.owningPackage, UserHandle.getUserId(syncOperation.owningUid));
                }
            }
            SyncStorageEngine.EndPoint info = syncOperation.target;
            SyncAdapterType syncAdapterType = SyncAdapterType.newKey(info.provider, info.account.type);
            RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo = SyncManager.this.mSyncAdapters.getServiceInfo(syncAdapterType, info.userId);
            if (syncAdapterInfo == null) {
                SyncManager.this.mLogger.log("dispatchSyncOperation() failed: no sync adapter info for ", syncAdapterType);
                Log.i("SyncManager", "can't find a sync adapter for " + syncAdapterType + ", removing settings for it");
                SyncManager.this.mSyncStorageEngine.removeAuthority(info);
                return false;
            }
            int targetUid = syncAdapterInfo.uid;
            ComponentName targetComponent = syncAdapterInfo.componentName;
            ActiveSyncContext activeSyncContext = new ActiveSyncContext(syncOperation, insertStartSyncEvent(op), targetUid);
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "dispatchSyncOperation: starting " + activeSyncContext);
            }
            activeSyncContext.mSyncInfo = SyncManager.this.mSyncStorageEngine.addActiveSync(activeSyncContext);
            SyncManager.this.mActiveSyncContexts.add(activeSyncContext);
            SyncManager.this.postMonitorSyncProgressMessage(activeSyncContext);
            if (activeSyncContext.bindToSyncAdapter(targetComponent, info.userId, syncOperation)) {
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
                SyncLogger access$1000 = SyncManager.this.mLogger;
                access$1000.log("Sync start: account=" + syncOperation.target.account, " authority=", syncOperation.target.provider, " reason=", SyncOperation.reasonToString(null, syncOperation.reason), " extras=", SyncOperation.extrasToString(syncOperation.extras), " adapter=", activeSyncContext.mSyncAdapter);
                activeSyncContext.mSyncAdapter = ISyncAdapter.Stub.asInterface(syncAdapter);
                Slog.i("SyncManager", "run startSync:" + syncOperation);
                activeSyncContext.mSyncAdapter.startSync(activeSyncContext, syncOperation.target.provider, syncOperation.target.account, syncOperation.extras);
                SyncManager.this.mLogger.log("Sync is running now...");
            } catch (RemoteException remoteExc) {
                SyncManager.this.mLogger.log("Sync failed with RemoteException: ", remoteExc.toString());
                Log.d("SyncManager", "maybeStartNextSync: caught a RemoteException, rescheduling", remoteExc);
                Log.i("SyncManager", "maybeStartNextSync: caught a RemoteException, rescheduling", remoteExc);
                closeActiveSyncContext(activeSyncContext);
                SyncManager.this.increaseBackoffSetting(syncOperation.target);
                SyncManager.this.scheduleSyncOperationH(syncOperation);
            } catch (RuntimeException exc) {
                SyncManager.this.mLogger.log("Sync failed with RuntimeException: ", exc.toString());
                closeActiveSyncContext(activeSyncContext);
                Slog.e("SyncManager", "Caught RuntimeException while starting the sync " + syncOperation, exc);
            }
        }

        private void cancelActiveSyncH(SyncStorageEngine.EndPoint info, Bundle extras, String why) {
            Iterator<ActiveSyncContext> it = new ArrayList<>(SyncManager.this.mActiveSyncContexts).iterator();
            while (it.hasNext()) {
                ActiveSyncContext activeSyncContext = it.next();
                if (activeSyncContext != null && activeSyncContext.mSyncOperation.target.matchesSpec(info)) {
                    if (extras == null || SyncManager.syncExtrasEquals(activeSyncContext.mSyncOperation.extras, extras, false)) {
                        SyncManager.this.mSyncJobService.callJobFinished(activeSyncContext.mSyncOperation.jobId, false, why);
                        runSyncFinishedOrCanceledH(null, activeSyncContext);
                    }
                }
            }
        }

        private void reschedulePeriodicSyncH(SyncOperation syncOperation) {
            SyncOperation periodicSync = null;
            Iterator<SyncOperation> it = SyncManager.this.getAllPendingSyncs().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                SyncOperation op = it.next();
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
            int downstreamActivity;
            String historyMessage;
            SyncResult syncResult2 = syncResult;
            ActiveSyncContext activeSyncContext2 = activeSyncContext;
            boolean isLoggable = Log.isLoggable("SyncManager", 2);
            SyncOperation syncOperation = activeSyncContext2.mSyncOperation;
            SyncStorageEngine.EndPoint info = syncOperation.target;
            int upstreamActivity = 0;
            if (activeSyncContext2.mIsLinkedToDeath) {
                activeSyncContext2.mSyncAdapter.asBinder().unlinkToDeath(activeSyncContext2, 0);
                activeSyncContext2.mIsLinkedToDeath = false;
            }
            long elapsedTime = SystemClock.elapsedRealtime() - activeSyncContext2.mStartTime;
            SyncManager.this.mLogger.log("runSyncFinishedOrCanceledH() op=", syncOperation, " result=", syncResult2);
            if (syncResult2 != null) {
                if (isLoggable) {
                    Slog.v("SyncManager", "runSyncFinishedOrCanceled [finished]: " + syncOperation + ", result " + syncResult2);
                }
                closeActiveSyncContext(activeSyncContext2);
                if (!syncOperation.isPeriodic) {
                    SyncManager.this.cancelJob(syncOperation, "runSyncFinishedOrCanceledH()-finished");
                }
                if (!syncResult.hasError()) {
                    historyMessage = SyncStorageEngine.MESG_SUCCESS;
                    downstreamActivity = 0;
                    upstreamActivity = 0;
                    SyncManager.this.clearBackoffSetting(syncOperation.target, "sync success");
                    if (syncOperation.isDerivedFromFailedPeriodicSync()) {
                        reschedulePeriodicSyncH(syncOperation);
                    }
                } else {
                    Log.d("SyncManager", "failed sync operation ");
                    syncOperation.retries++;
                    if (syncOperation.retries > SyncManager.this.mConstants.getMaxRetriesWithAppStandbyExemption()) {
                        syncOperation.syncExemptionFlag = 0;
                    }
                    SyncManager.this.increaseBackoffSetting(syncOperation.target);
                    if (!syncOperation.isPeriodic) {
                        SyncManager.this.maybeRescheduleSync(syncResult2, syncOperation);
                    } else {
                        SyncManager.this.postScheduleSyncMessage(syncOperation.createOneTimeSyncOperation(), 0);
                    }
                    historyMessage = ContentResolver.syncErrorToString(syncResultToErrorNumber(syncResult));
                    downstreamActivity = 0;
                }
                SyncManager.this.setDelayUntilTime(syncOperation.target, syncResult2.delayUntil);
            } else {
                if (isLoggable) {
                    Slog.v("SyncManager", "runSyncFinishedOrCanceled [canceled]: " + syncOperation);
                }
                if (!syncOperation.isPeriodic) {
                    SyncManager.this.cancelJob(syncOperation, "runSyncFinishedOrCanceledH()-canceled");
                }
                if (activeSyncContext2.mSyncAdapter != null) {
                    try {
                        SyncManager.this.mLogger.log("Calling cancelSync for runSyncFinishedOrCanceled ", activeSyncContext2, "  adapter=", activeSyncContext2.mSyncAdapter);
                        activeSyncContext2.mSyncAdapter.cancelSync(activeSyncContext2);
                        SyncManager.this.mLogger.log("Canceled");
                    } catch (RemoteException e) {
                        SyncManager.this.mLogger.log("RemoteException ", Log.getStackTraceString(e));
                    }
                }
                historyMessage = SyncStorageEngine.MESG_CANCELED;
                downstreamActivity = 0;
                upstreamActivity = 0;
                closeActiveSyncContext(activeSyncContext2);
            }
            int downstreamActivity2 = downstreamActivity;
            stopSyncEvent(activeSyncContext2.mHistoryRowId, syncOperation, historyMessage, upstreamActivity, downstreamActivity2, elapsedTime);
            if (syncResult2 == null || !syncResult2.tooManyDeletions) {
                SyncManager.this.mNotificationMgr.cancelAsUser(Integer.toString(info.account.hashCode() ^ info.provider.hashCode()), 18, new UserHandle(info.userId));
            } else {
                installHandleTooManyDeletesNotification(info.account, info.provider, syncResult2.stats.numDeletes, info.userId);
            }
            if (syncResult2 == null || !syncResult2.fullSyncRequested) {
                return;
            }
            SyncManager syncManager = SyncManager.this;
            String str = historyMessage;
            SyncOperation syncOperation2 = new SyncOperation(info.account, info.userId, syncOperation.owningUid, syncOperation.owningPackage, syncOperation.reason, syncOperation.syncSource, info.provider, new Bundle(), syncOperation.allowParallelSyncs, syncOperation.syncExemptionFlag);
            syncManager.scheduleSyncOperationH(syncOperation2);
        }

        private void closeActiveSyncContext(ActiveSyncContext activeSyncContext) {
            activeSyncContext.close();
            SyncManager.this.mActiveSyncContexts.remove(activeSyncContext);
            SyncManager.this.mSyncStorageEngine.removeActiveSync(activeSyncContext.mSyncInfo, activeSyncContext.mSyncOperation.target.userId);
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "removing all MESSAGE_MONITOR_SYNC & MESSAGE_SYNC_EXPIRED for " + activeSyncContext.toString());
            }
            SyncManager.this.mSyncHandler.removeMessages(8, activeSyncContext);
            SyncManager.this.mLogger.log("closeActiveSyncContext: ", activeSyncContext);
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
            String str = authority;
            if (SyncManager.this.mNotificationMgr != null) {
                ProviderInfo providerInfo = SyncManager.this.mContext.getPackageManager().resolveContentProvider(str, 0);
                if (providerInfo != null) {
                    CharSequence authorityName = providerInfo.loadLabel(SyncManager.this.mContext.getPackageManager());
                    Intent clickIntent = new Intent(SyncManager.this.mContext, SyncActivityTooManyDeletes.class);
                    clickIntent.putExtra("account", account);
                    clickIntent.putExtra("authority", str);
                    clickIntent.putExtra("provider", authorityName.toString());
                    clickIntent.putExtra("numDeletes", numDeletes);
                    if (!isActivityAvailable(clickIntent)) {
                        Log.w("SyncManager", "No activity found to handle too many deletes.");
                        return;
                    }
                    UserHandle user = new UserHandle(userId);
                    PendingIntent pendingIntent = PendingIntent.getActivityAsUser(SyncManager.this.mContext, 0, clickIntent, 268435456, null, user);
                    CharSequence tooManyDeletesDescFormat = SyncManager.this.mContext.getResources().getText(17039865);
                    Context contextForUser = SyncManager.this.getContextForUser(user);
                    Notification notification = new Notification.Builder(contextForUser, SystemNotificationChannels.ACCOUNT).setSmallIcon(17303480).setLargeIcon(BitmapFactory.decodeResource(SyncManager.this.mContext.getResources(), 33751680)).setTicker(SyncManager.this.mContext.getString(17039863)).setWhen(System.currentTimeMillis()).setColor(contextForUser.getColor(17170784)).setContentTitle(contextForUser.getString(17039864)).setContentText(String.format(tooManyDeletesDescFormat.toString(), new Object[]{authorityName})).setContentIntent(pendingIntent).build();
                    notification.flags |= 2;
                    SyncManager.this.mNotificationMgr.notifyAsUser(Integer.toString(account.hashCode() ^ authority.hashCode()), 18, notification, user);
                }
            }
        }

        private boolean isActivityAvailable(Intent intent) {
            List<ResolveInfo> list = SyncManager.this.mContext.getPackageManager().queryIntentActivities(intent, 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                if ((list.get(i).activityInfo.applicationInfo.flags & 1) != 0) {
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

        private SyncTimeTracker() {
            this.mLastWasSyncing = false;
            this.mWhenSyncStarted = 0;
        }

        public synchronized void update() {
            boolean isSyncInProgress = !SyncManager.this.mActiveSyncContexts.isEmpty();
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
            if (!this.mLastWasSyncing) {
                return this.mTimeSpentSyncing;
            }
            long now = SystemClock.elapsedRealtime();
            return this.mTimeSpentSyncing + (now - this.mWhenSyncStarted);
        }
    }

    private class UpdatePeriodicSyncMessagePayload {
        public final Bundle extras;
        public final long flex;
        public final long pollFrequency;
        public final SyncStorageEngine.EndPoint target;

        UpdatePeriodicSyncMessagePayload(SyncStorageEngine.EndPoint target2, long pollFrequency2, long flex2, Bundle extras2) {
            this.target = target2;
            this.pollFrequency = pollFrequency2;
            this.flex = flex2;
            this.extras = extras2;
        }
    }

    /* JADX WARNING: type inference failed for: r0v2, types: [boolean, byte] */
    static /* synthetic */ boolean access$1876(SyncManager x0, int x1) {
        ? r0 = (byte) (x0.mProvisioned | x1);
        x0.mProvisioned = r0;
        return r0;
    }

    private boolean isJobIdInUseLockedH(int jobId, List<JobInfo> pendingJobs) {
        for (JobInfo job : pendingJobs) {
            if (job.getId() == jobId) {
                return true;
            }
        }
        Iterator<ActiveSyncContext> it = this.mActiveSyncContexts.iterator();
        while (it.hasNext()) {
            if (it.next().mSyncOperation.jobId == jobId) {
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

    /* access modifiers changed from: private */
    public List<SyncOperation> getAllPendingSyncs() {
        verifyJobScheduler();
        List<JobInfo> pendingJobs = this.mJobSchedulerInternal.getSystemScheduledPendingJobs();
        List<SyncOperation> pendingSyncs = new ArrayList<>(pendingJobs.size());
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

    /* access modifiers changed from: private */
    public boolean containsAccountAndUser(AccountAndUser[] accounts, Account account, int userId) {
        for (int i = 0; i < accounts.length; i++) {
            if (accounts[i].userId == userId && accounts[i].account.equals(account)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void updateRunningAccounts(SyncStorageEngine.EndPoint target) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "sending MESSAGE_ACCOUNTS_UPDATED");
        }
        Message m = this.mSyncHandler.obtainMessage(9);
        m.obj = target;
        m.sendToTarget();
    }

    /* access modifiers changed from: private */
    public void doDatabaseCleanup() {
        for (UserInfo user : this.mUserManager.getUsers(true)) {
            if (!user.partial) {
                this.mSyncStorageEngine.doDatabaseCleanup(AccountManagerService.getSingleton().getAccounts(user.id, this.mContext.getOpPackageName()), user.id);
            }
        }
    }

    /* access modifiers changed from: private */
    public void clearAllBackoffs(String why) {
        this.mSyncStorageEngine.clearAllBackoffsLocked();
        rescheduleSyncs(SyncStorageEngine.EndPoint.USER_ALL_PROVIDER_ALL_ACCOUNTS_ALL, why);
    }

    /* access modifiers changed from: private */
    public boolean readDataConnectionState() {
        NetworkInfo networkInfo = getConnectivityManager().getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /* access modifiers changed from: private */
    public String getJobStats() {
        String str;
        JobSchedulerInternal js = (JobSchedulerInternal) LocalServices.getService(JobSchedulerInternal.class);
        StringBuilder sb = new StringBuilder();
        sb.append("JobStats: ");
        if (js == null) {
            str = "(JobSchedulerInternal==null)";
        } else {
            str = js.getPersistStats().toString();
        }
        sb.append(str);
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public ConnectivityManager getConnectivityManager() {
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
                Set<String> cleanedKeys = new HashSet<>();
                for (SyncOperation opx : ops) {
                    if (!cleanedKeys.contains(opx.key)) {
                        cleanedKeys.add(opx.key);
                        for (SyncOperation opy : ops) {
                            if (opx != opy && opx.key.equals(opy.key)) {
                                SyncManager.this.mLogger.log("Removing duplicate sync: ", opy);
                                SyncManager syncManager = SyncManager.this;
                                syncManager.cancelJob(opy, "cleanupJobs() x=" + opx + " y=" + opy);
                            }
                        }
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public synchronized void verifyJobScheduler() {
        if (this.mJobScheduler == null) {
            long token = Binder.clearCallingIdentity();
            try {
                if (Log.isLoggable("SyncManager", 2)) {
                    Log.d("SyncManager", "initializing JobScheduler object.");
                }
                this.mJobScheduler = (JobScheduler) this.mContext.getSystemService("jobscheduler");
                this.mJobSchedulerInternal = (JobSchedulerInternal) LocalServices.getService(JobSchedulerInternal.class);
                int numPersistedPeriodicSyncs = 0;
                int numPersistedOneshotSyncs = 0;
                for (JobInfo job : this.mJobScheduler.getAllPendingJobs()) {
                    SyncOperation op = SyncOperation.maybeCreateFromJobExtras(job.getExtras());
                    if (op != null) {
                        if (op.isPeriodic) {
                            numPersistedPeriodicSyncs++;
                        } else {
                            numPersistedOneshotSyncs++;
                            this.mSyncStorageEngine.markPending(op.target, true);
                        }
                    }
                }
                String summary = "Loaded persisted syncs: " + numPersistedPeriodicSyncs + " periodic syncs, " + numPersistedOneshotSyncs + " oneshot syncs, " + pendingJobs.size() + " total system server jobs, " + getJobStats();
                Slog.i("SyncManager", summary);
                this.mLogger.log(summary);
                cleanupJobs();
                if (ENABLE_SUSPICIOUS_CHECK && numPersistedPeriodicSyncs == 0 && likelyHasPeriodicSyncs()) {
                    Slog.wtf("SyncManager", "Device booted with no persisted periodic syncs: " + summary);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    private boolean likelyHasPeriodicSyncs() {
        boolean z = false;
        try {
            if (this.mSyncStorageEngine.getAuthorityCount() >= 6) {
                z = true;
            }
            return z;
        } catch (Throwable th) {
            return false;
        }
    }

    private JobScheduler getJobScheduler() {
        verifyJobScheduler();
        return this.mJobScheduler;
    }

    public SyncManager(Context context, boolean factoryTest) {
        synchronized (SyncManager.class) {
            if (sInstance == null) {
                sInstance = this;
            } else {
                Slog.wtf("SyncManager", "SyncManager instantiated multiple times");
            }
        }
        this.mContext = context;
        this.mLogger = SyncLogger.getInstance();
        SyncStorageEngine.init(context, BackgroundThread.get().getLooper());
        this.mSyncStorageEngine = SyncStorageEngine.getSingleton();
        this.mSyncStorageEngine.setOnSyncRequestListener(new SyncStorageEngine.OnSyncRequestListener() {
            public void onSyncRequest(SyncStorageEngine.EndPoint info, int reason, Bundle extras, int syncExemptionFlag) {
                SyncManager.this.scheduleSync(info.account, info.userId, reason, info.provider, extras, -2, syncExemptionFlag);
            }
        });
        this.mSyncStorageEngine.setPeriodicSyncAddedListener(new SyncStorageEngine.PeriodicSyncAddedListener() {
            public void onPeriodicSyncAdded(SyncStorageEngine.EndPoint target, Bundle extras, long pollFrequency, long flex) {
                SyncManager.this.updateOrAddPeriodicSync(target, pollFrequency, flex, extras);
            }
        });
        this.mSyncStorageEngine.setOnAuthorityRemovedListener(new SyncStorageEngine.OnAuthorityRemovedListener() {
            public void onAuthorityRemoved(SyncStorageEngine.EndPoint removedAuthority) {
                SyncManager.this.removeSyncsForAuthority(removedAuthority, "onAuthorityRemoved");
            }
        });
        this.mSyncAdapters = new SyncAdaptersCache(this.mContext);
        this.mThread = new HandlerThread("SyncManager", 10);
        this.mThread.start();
        this.mSyncHandler = new SyncHandler(this.mThread.getLooper());
        this.mSyncAdapters.setListener(new RegisteredServicesCacheListener<SyncAdapterType>() {
            public void onServiceChanged(SyncAdapterType type, int userId, boolean removed) {
                if (!removed) {
                    SyncManager.this.scheduleSync(null, -1, -3, type.authority, null, -2, 0);
                }
            }
        }, this.mSyncHandler);
        this.mRand = new Random(System.currentTimeMillis());
        this.mConstants = new SyncManagerConstants(context);
        context.registerReceiver(this.mConnectivityIntentReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        if (!factoryTest) {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.BOOT_COMPLETED");
            intentFilter.setPriority(1000);
            context.registerReceiver(this.mBootCompletedReceiver, intentFilter);
        }
        IntentFilter intentFilter2 = new IntentFilter("android.intent.action.DEVICE_STORAGE_LOW");
        intentFilter2.addAction("android.intent.action.DEVICE_STORAGE_OK");
        context.registerReceiver(this.mStorageIntentReceiver, intentFilter2);
        IntentFilter intentFilter3 = new IntentFilter("android.intent.action.ACTION_SHUTDOWN");
        intentFilter3.setPriority(100);
        context.registerReceiver(this.mShutdownIntentReceiver, intentFilter3);
        IntentFilter intentFilter4 = new IntentFilter();
        intentFilter4.addAction("android.intent.action.USER_REMOVED");
        intentFilter4.addAction("android.intent.action.USER_UNLOCKED");
        intentFilter4.addAction("android.intent.action.USER_STOPPED");
        this.mContext.registerReceiverAsUser(this.mUserIntentReceiver, UserHandle.ALL, intentFilter4, null, null);
        context.registerReceiver(this.mOtherIntentsReceiver, new IntentFilter("android.intent.action.TIME_SET"));
        if (!factoryTest) {
            this.mNotificationMgr = (NotificationManager) context.getSystemService("notification");
        } else {
            this.mNotificationMgr = null;
        }
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mAccountManager = (AccountManager) this.mContext.getSystemService("account");
        this.mAccountManagerInternal = (AccountManagerInternal) LocalServices.getService(AccountManagerInternal.class);
        this.mPackageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mAccountManagerInternal.addOnAppPermissionChangeListener(new AccountManagerInternal.OnAppPermissionChangeListener() {
            public final void onAppPermissionChanged(Account account, int i) {
                SyncManager.lambda$new$0(SyncManager.this, account, i);
            }
        });
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
                    SyncManager.access$1876(SyncManager.this, SyncManager.this.isDeviceProvisioned() ? 1 : 0);
                    if (SyncManager.this.mProvisioned) {
                        SyncManager.this.mSyncHandler.onDeviceProvisioned();
                        resolver.unregisterContentObserver(this);
                    }
                }
            };
            synchronized (this.mSyncHandler) {
                resolver.registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, provisionedObserver);
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
        this.mLogger.log("Sync manager initialized: " + Build.FINGERPRINT);
    }

    public static /* synthetic */ void lambda$new$0(SyncManager syncManager, Account account, int uid) {
        if (syncManager.mAccountManagerInternal.hasAccountAccess(account, uid)) {
            syncManager.scheduleSync(account, UserHandle.getUserId(uid), -2, null, null, 3, 0);
        }
    }

    public void onStartUser(int userHandle) {
        this.mSyncHandler.post(new Runnable(userHandle) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                SyncManager.this.mLogger.log("onStartUser: user=", Integer.valueOf(this.f$1));
            }
        });
    }

    public void onUnlockUser(int userHandle) {
        this.mSyncHandler.post(new Runnable(userHandle) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                SyncManager.this.mLogger.log("onUnlockUser: user=", Integer.valueOf(this.f$1));
            }
        });
    }

    public void onStopUser(int userHandle) {
        this.mSyncHandler.post(new Runnable(userHandle) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                SyncManager.this.mLogger.log("onStopUser: user=", Integer.valueOf(this.f$1));
            }
        });
    }

    public void onBootPhase(int phase) {
        if (phase == 550) {
            this.mConstants.start();
        }
    }

    private void whiteListExistingSyncAdaptersIfNeeded() {
        SyncManager syncManager = this;
        if (syncManager.mSyncStorageEngine.shouldGrantSyncAdaptersAccountAccess()) {
            List<UserInfo> users = syncManager.mUserManager.getUsers(true);
            int userCount = users.size();
            int i = 0;
            while (i < userCount) {
                UserHandle userHandle = users.get(i).getUserHandle();
                int userId = userHandle.getIdentifier();
                for (RegisteredServicesCache.ServiceInfo<SyncAdapterType> service : syncManager.mSyncAdapters.getAllServices(userId)) {
                    String packageName = service.componentName.getPackageName();
                    Account[] accountsByTypeAsUser = syncManager.mAccountManager.getAccountsByTypeAsUser(((SyncAdapterType) service.type).accountType, userHandle);
                    int length = accountsByTypeAsUser.length;
                    int i2 = 0;
                    while (i2 < length) {
                        Account account = accountsByTypeAsUser[i2];
                        if (!syncManager.canAccessAccount(account, packageName, userId)) {
                            syncManager.mAccountManager.updateAppPermission(account, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", service.uid, true);
                        }
                        i2++;
                        syncManager = this;
                    }
                    syncManager = this;
                }
                i++;
                syncManager = this;
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isDeviceProvisioned() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
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
        if (userInfo == null || !userInfo.isRestricted()) {
            return isSyncable;
        }
        RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo = this.mSyncAdapters.getServiceInfo(SyncAdapterType.newKey(providerName, account.type), userId);
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

    /* access modifiers changed from: private */
    public void setAuthorityPendingState(SyncStorageEngine.EndPoint info) {
        for (SyncOperation op : getAllPendingSyncs()) {
            if (!op.isPeriodic && op.target.matchesSpec(info)) {
                getSyncStorageEngine().markPending(info, true);
                return;
            }
        }
        getSyncStorageEngine().markPending(info, false);
    }

    public void scheduleSync(Account requestedAccount, int userId, int reason, String requestedAuthority, Bundle extras, int targetSyncState, int syncExemptionFlag) {
        scheduleSync(requestedAccount, userId, reason, requestedAuthority, extras, targetSyncState, 0, true, syncExemptionFlag);
    }

    /* JADX WARNING: type inference failed for: r3v47, types: [java.lang.Object[]] */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x02f2, code lost:
        if (r12.mSyncStorageEngine.getSyncAutomatically(r11.account, r11.userId, r9) != false) goto L_0x02fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x0422, code lost:
        if (r11 == r57) goto L_0x0430;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0300  */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x031f  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00bb  */
    private void scheduleSync(Account requestedAccount, int userId, int reason, String requestedAuthority, Bundle extras, int targetSyncState, long minDelayMillis, boolean checkIfAccountReady, int syncExemptionFlag) {
        Bundle extras2;
        AccountAndUser[] accounts;
        int source;
        int i;
        int i2;
        boolean z;
        int source2;
        int i3;
        boolean z2;
        int i4;
        AccountAndUser[] accounts2;
        int source3;
        Bundle extras3;
        AccountAndUser account;
        int isSyncable;
        String authority;
        boolean syncAllowed;
        int source4;
        AccountAndUser account2;
        Bundle extras4;
        Bundle extras5;
        AccountAndUser[] accounts3;
        Bundle extras6;
        int source5;
        AccountAndUser account3;
        long j;
        Bundle extras7;
        String authority2;
        int owningUid;
        AccountAndUser account4;
        Bundle extras8;
        int source6;
        HashSet<String> syncableAuthorities;
        boolean z3;
        RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo;
        int i5;
        AccountAndUser[] accounts4;
        AccountAndUser[] accounts5;
        SyncManager syncManager = this;
        Account account5 = requestedAccount;
        int i6 = userId;
        String str = requestedAuthority;
        int i7 = targetSyncState;
        long j2 = minDelayMillis;
        boolean z4 = checkIfAccountReady;
        boolean isLoggable = Log.isLoggable("SyncManager", 2);
        if (extras == null) {
            extras2 = new Bundle();
        } else {
            extras2 = extras;
        }
        if (isLoggable) {
            Log.d("SyncManager", "one-time sync for: " + account5 + " " + extras2.toString() + " " + str + " reason=" + reason + " checkIfAccountReady=" + z4 + " syncExemptionFlag=" + syncExemptionFlag);
        } else {
            int i8 = reason;
            int i9 = syncExemptionFlag;
        }
        if (account5 == null) {
            accounts5 = syncManager.mRunningAccounts;
        } else if (i6 != -1) {
            accounts5 = new AccountAndUser[]{new AccountAndUser(account5, i6)};
        } else {
            AccountAndUser[] accountAndUserArr = syncManager.mRunningAccounts;
            int length = accountAndUserArr.length;
            AccountAndUser[] accounts6 = null;
            int i10 = 0;
            while (i10 < length) {
                AccountAndUser runningAccount = accountAndUserArr[i10];
                AccountAndUser[] accountAndUserArr2 = accountAndUserArr;
                if (account5.equals(runningAccount.account)) {
                    accounts6 = (AccountAndUser[]) ArrayUtils.appendElement(AccountAndUser.class, accounts6, runningAccount);
                }
                i10++;
                accountAndUserArr = accountAndUserArr2;
            }
            accounts = accounts6;
            if (!ArrayUtils.isEmpty(accounts)) {
                if (isLoggable) {
                    Slog.v("SyncManager", "scheduleSync: no accounts configured, dropping");
                }
                return;
            }
            boolean uploadOnly = extras2.getBoolean("upload", false);
            boolean manualSync = extras2.getBoolean("force", false);
            if (manualSync) {
                extras2.putBoolean("ignore_backoff", true);
                extras2.putBoolean("ignore_settings", true);
            }
            boolean ignoreSettings = extras2.getBoolean("ignore_settings", false);
            if (uploadOnly) {
                source = 1;
            } else if (manualSync) {
                source = 3;
            } else if (str == null) {
                source = 2;
            } else if (extras2.containsKey("feed")) {
                source = 5;
            } else {
                source = 0;
            }
            int source7 = source;
            int length2 = accounts.length;
            int i11 = 0;
            while (i2 < length2) {
                AccountAndUser account6 = accounts[i2];
                if (i6 < 0 || account6.userId < 0 || i6 == account6.userId) {
                    HashSet<String> syncableAuthorities2 = new HashSet<>();
                    for (RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapter : syncManager.mSyncAdapters.getAllServices(account6.userId)) {
                        syncableAuthorities2.add(((SyncAdapterType) syncAdapter.type).authority);
                        length2 = length2;
                    }
                    i3 = length2;
                    HashSet<String> syncableAuthorities3 = syncableAuthorities2;
                    if (str != null) {
                        boolean hasSyncAdapter = syncableAuthorities3.contains(str);
                        syncableAuthorities3.clear();
                        if (hasSyncAdapter) {
                            syncableAuthorities3.add(str);
                        }
                    }
                    RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo = syncableAuthorities3.iterator();
                    while (syncAdapterInfo.hasNext()) {
                        String authority3 = syncAdapterInfo.next();
                        RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo2 = syncAdapterInfo;
                        AccountAndUser[] accounts7 = accounts;
                        int isSyncable2 = syncManager.computeSyncable(account6.account, account6.userId, authority3, !z);
                        if (isSyncable2 != 0) {
                            String authority4 = authority3;
                            RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo2 = syncManager.mSyncAdapters.getServiceInfo(SyncAdapterType.newKey(authority3, account6.account.type), account6.userId);
                            if (syncAdapterInfo2 != null) {
                                int owningUid2 = syncAdapterInfo2.uid;
                                if (isSyncable2 == 3) {
                                    if (isLoggable) {
                                        owningUid = owningUid2;
                                        Slog.v("SyncManager", "    Not scheduling sync operation: isSyncable == SYNCABLE_NO_ACCOUNT_ACCESS");
                                    } else {
                                        owningUid = owningUid2;
                                    }
                                    int source8 = source2;
                                    Bundle finalExtras = new Bundle(extras2);
                                    String packageName = syncAdapterInfo2.componentName.getPackageName();
                                    try {
                                        syncableAuthorities = syncableAuthorities3;
                                        String packageName2 = packageName;
                                        try {
                                            if (!syncManager.mPackageManagerInternal.wasPackageEverLaunched(packageName2, i6)) {
                                                source2 = source8;
                                                syncAdapterInfo = serviceInfo2;
                                                accounts3 = accounts7;
                                                syncableAuthorities3 = syncableAuthorities;
                                            } else {
                                                AccountManagerInternal accountManagerInternal = syncManager.mAccountManagerInternal;
                                                Account account7 = account6.account;
                                                source6 = source8;
                                                int i12 = owningUid;
                                                RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo3 = syncAdapterInfo2;
                                                serviceInfo = serviceInfo2;
                                                int i13 = isSyncable2;
                                                accounts4 = accounts7;
                                                extras8 = extras2;
                                                z3 = z;
                                                i5 = i2;
                                                account4 = account6;
                                                $$Lambda$SyncManager$o7UdjgcI2E4HDw_2JMHWT1SJs r1 = new RemoteCallback.OnResultListener(account6, i6, reason, authority4, finalExtras, i, minDelayMillis, syncExemptionFlag) {
                                                    private final /* synthetic */ AccountAndUser f$1;
                                                    private final /* synthetic */ int f$2;
                                                    private final /* synthetic */ int f$3;
                                                    private final /* synthetic */ String f$4;
                                                    private final /* synthetic */ Bundle f$5;
                                                    private final /* synthetic */ int f$6;
                                                    private final /* synthetic */ long f$7;
                                                    private final /* synthetic */ int f$8;

                                                    {
                                                        this.f$1 = r2;
                                                        this.f$2 = r3;
                                                        this.f$3 = r4;
                                                        this.f$4 = r5;
                                                        this.f$5 = r6;
                                                        this.f$6 = r7;
                                                        this.f$7 = r8;
                                                        this.f$8 = r10;
                                                    }

                                                    public final void onResult(Bundle bundle) {
                                                        SyncManager.lambda$scheduleSync$4(SyncManager.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, bundle);
                                                    }
                                                };
                                                accountManagerInternal.requestAccountAccess(account7, packageName2, i6, new RemoteCallback(r1));
                                                int i14 = syncExemptionFlag;
                                                accounts3 = accounts4;
                                                i2 = i5;
                                                syncAdapterInfo = serviceInfo;
                                                z = z3;
                                                syncableAuthorities3 = syncableAuthorities;
                                                source2 = source6;
                                                extras5 = extras8;
                                                account6 = account4;
                                                i = targetSyncState;
                                            }
                                        } catch (IllegalArgumentException e) {
                                            source6 = source8;
                                            RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo4 = syncAdapterInfo2;
                                            int i15 = isSyncable2;
                                            extras8 = extras2;
                                            z3 = z;
                                            i5 = i2;
                                            account4 = account6;
                                            String str2 = packageName2;
                                            serviceInfo = serviceInfo2;
                                            accounts4 = accounts7;
                                            String str3 = authority4;
                                            int i16 = owningUid;
                                            int i17 = reason;
                                            int i142 = syncExemptionFlag;
                                            accounts3 = accounts4;
                                            i2 = i5;
                                            syncAdapterInfo = serviceInfo;
                                            z = z3;
                                            syncableAuthorities3 = syncableAuthorities;
                                            source2 = source6;
                                            extras5 = extras8;
                                            account6 = account4;
                                            i = targetSyncState;
                                            String str4 = requestedAuthority;
                                        }
                                    } catch (IllegalArgumentException e2) {
                                        source6 = source8;
                                        RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo5 = syncAdapterInfo2;
                                        int i18 = isSyncable2;
                                        extras8 = extras2;
                                        z3 = z;
                                        i5 = i2;
                                        account4 = account6;
                                        syncableAuthorities = syncableAuthorities3;
                                        serviceInfo = serviceInfo2;
                                        accounts4 = accounts7;
                                        String str5 = authority4;
                                        int i19 = owningUid;
                                        String str6 = packageName;
                                        int i172 = reason;
                                        int i1422 = syncExemptionFlag;
                                        accounts3 = accounts4;
                                        i2 = i5;
                                        syncAdapterInfo = serviceInfo;
                                        z = z3;
                                        syncableAuthorities3 = syncableAuthorities;
                                        source2 = source6;
                                        extras5 = extras8;
                                        account6 = account4;
                                        i = targetSyncState;
                                        String str42 = requestedAuthority;
                                    }
                                } else {
                                    int owningUid3 = owningUid2;
                                    int isSyncable3 = isSyncable2;
                                    Bundle extras9 = extras2;
                                    int source9 = source2;
                                    boolean z5 = z;
                                    int i20 = i2;
                                    AccountAndUser account8 = account6;
                                    HashSet<String> syncableAuthorities4 = syncableAuthorities3;
                                    RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo6 = serviceInfo2;
                                    AccountAndUser[] accounts8 = accounts7;
                                    String authority5 = authority4;
                                    RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo3 = syncAdapterInfo2;
                                    boolean allowParallelSyncs = ((SyncAdapterType) syncAdapterInfo3.type).allowParallelSyncs();
                                    boolean isAlwaysSyncable = ((SyncAdapterType) syncAdapterInfo3.type).isAlwaysSyncable();
                                    if (z5 || isSyncable3 >= 0 || !isAlwaysSyncable) {
                                        account = account8;
                                        isSyncable = isSyncable3;
                                    } else {
                                        account = account8;
                                        syncManager.mSyncStorageEngine.setIsSyncable(account.account, account.userId, authority5, 1, -1);
                                        isSyncable = 1;
                                    }
                                    int i21 = targetSyncState;
                                    if ((i21 == -2 || i21 == isSyncable) && (((SyncAdapterType) syncAdapterInfo3.type).supportsUploading() || !uploadOnly)) {
                                        if (isSyncable < 0 || ignoreSettings) {
                                            authority = authority5;
                                        } else {
                                            if (syncManager.mSyncStorageEngine.getMasterSyncAutomatically(account.userId)) {
                                                authority = authority5;
                                            } else {
                                                authority = authority5;
                                            }
                                            syncAllowed = false;
                                            if (!syncAllowed) {
                                                SyncStorageEngine.EndPoint info = new SyncStorageEngine.EndPoint(account.account, authority, account.userId);
                                                long delayUntil = syncManager.mSyncStorageEngine.getDelayUntilTime(info);
                                                String owningPackage = syncAdapterInfo3.componentName.getPackageName();
                                                if (isSyncable != -1) {
                                                    boolean allowParallelSyncs2 = allowParallelSyncs;
                                                    SyncStorageEngine.EndPoint endPoint = info;
                                                    int isSyncable4 = isSyncable;
                                                    account3 = account;
                                                    boolean z6 = isAlwaysSyncable;
                                                    source5 = source9;
                                                    extras6 = extras9;
                                                    j = minDelayMillis;
                                                    int i22 = targetSyncState;
                                                    if (i22 == -2) {
                                                        int i23 = isSyncable4;
                                                    }
                                                    if (isLoggable) {
                                                        StringBuilder sb = new StringBuilder();
                                                        sb.append("scheduleSync: delay until ");
                                                        sb.append(delayUntil);
                                                        sb.append(", source ");
                                                        sb.append(source5);
                                                        sb.append(", account , authority ");
                                                        sb.append(authority);
                                                        sb.append(", extras ");
                                                        extras7 = extras6;
                                                        sb.append(extras7);
                                                        Slog.v("SyncManager", sb.toString());
                                                    } else {
                                                        extras7 = extras6;
                                                    }
                                                    extras4 = extras7;
                                                    long j3 = delayUntil;
                                                    RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo7 = syncAdapterInfo3;
                                                    account2 = account3;
                                                    String str7 = authority;
                                                    source4 = source5;
                                                    SyncOperation syncOperation = new SyncOperation(account3.account, account3.userId, owningUid3, owningPackage, reason, source5, authority, extras4, allowParallelSyncs2, syncExemptionFlag);
                                                    syncManager.postScheduleSyncMessage(syncOperation, j);
                                                    int i24 = reason;
                                                    i = targetSyncState;
                                                    int i25 = syncExemptionFlag;
                                                    extras5 = extras4;
                                                    account6 = account2;
                                                    source2 = source4;
                                                    accounts3 = accounts8;
                                                    i2 = i20;
                                                    syncAdapterInfo = serviceInfo6;
                                                    z = z5;
                                                    syncableAuthorities3 = syncableAuthorities4;
                                                    i6 = userId;
                                                } else if (z5) {
                                                    Bundle extras10 = extras9;
                                                    Bundle finalExtras2 = new Bundle(extras10);
                                                    Context context = syncManager.mContext;
                                                    int i26 = account.userId;
                                                    boolean z7 = isAlwaysSyncable;
                                                    boolean z8 = allowParallelSyncs;
                                                    Context context2 = context;
                                                    AccountAndUser accountAndUser = account;
                                                    AccountAndUser account9 = account;
                                                    long j4 = delayUntil;
                                                    SyncStorageEngine.EndPoint endPoint2 = info;
                                                    String str8 = authority;
                                                    int i27 = isSyncable;
                                                    $$Lambda$SyncManager$Dly2yZUw2lCDXffoc_fe8npXe2U r12 = new OnReadyCallback(accountAndUser, reason, authority, finalExtras2, i21, minDelayMillis, syncExemptionFlag) {
                                                        private final /* synthetic */ AccountAndUser f$1;
                                                        private final /* synthetic */ int f$2;
                                                        private final /* synthetic */ String f$3;
                                                        private final /* synthetic */ Bundle f$4;
                                                        private final /* synthetic */ int f$5;
                                                        private final /* synthetic */ long f$6;
                                                        private final /* synthetic */ int f$7;

                                                        {
                                                            this.f$1 = r2;
                                                            this.f$2 = r3;
                                                            this.f$3 = r4;
                                                            this.f$4 = r5;
                                                            this.f$5 = r6;
                                                            this.f$6 = r7;
                                                            this.f$7 = r9;
                                                        }

                                                        public final void onReady() {
                                                            SyncManager.lambda$scheduleSync$5(SyncManager.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7);
                                                        }
                                                    };
                                                    sendOnUnsyncableAccount(context2, syncAdapterInfo3, i26, r12);
                                                    long j5 = minDelayMillis;
                                                    source4 = source9;
                                                    extras4 = extras10;
                                                    account2 = account9;
                                                    syncManager = this;
                                                    int i242 = reason;
                                                    i = targetSyncState;
                                                    int i252 = syncExemptionFlag;
                                                    extras5 = extras4;
                                                    account6 = account2;
                                                    source2 = source4;
                                                    accounts3 = accounts8;
                                                    i2 = i20;
                                                    syncAdapterInfo = serviceInfo6;
                                                    z = z5;
                                                    syncableAuthorities3 = syncableAuthorities4;
                                                    i6 = userId;
                                                } else {
                                                    boolean allowParallelSyncs3 = allowParallelSyncs;
                                                    SyncStorageEngine.EndPoint endPoint3 = info;
                                                    String authority6 = authority;
                                                    int i28 = isSyncable;
                                                    AccountAndUser account10 = account;
                                                    boolean z9 = isAlwaysSyncable;
                                                    extras6 = extras9;
                                                    long delayUntil2 = delayUntil;
                                                    Bundle newExtras = new Bundle();
                                                    newExtras.putBoolean("initialize", true);
                                                    if (isLoggable) {
                                                        StringBuilder sb2 = new StringBuilder();
                                                        sb2.append("schedule initialisation Sync:, delay until ");
                                                        sb2.append(delayUntil2);
                                                        sb2.append(", run by ");
                                                        sb2.append(0);
                                                        sb2.append(", flexMillis ");
                                                        sb2.append(0);
                                                        sb2.append(", source ");
                                                        source5 = source9;
                                                        sb2.append(source5);
                                                        sb2.append(", account , authority ");
                                                        authority2 = authority6;
                                                        sb2.append(authority2);
                                                        sb2.append(", extras ");
                                                        sb2.append(newExtras);
                                                        Slog.v("SyncManager", sb2.toString());
                                                    } else {
                                                        source5 = source9;
                                                        authority2 = authority6;
                                                    }
                                                    account3 = account10;
                                                    SyncOperation syncOperation2 = new SyncOperation(account3.account, account3.userId, owningUid3, owningPackage, reason, source5, authority2, newExtras, allowParallelSyncs3, syncExemptionFlag);
                                                    j = minDelayMillis;
                                                    long j6 = delayUntil2;
                                                    syncManager = this;
                                                    syncManager.postScheduleSyncMessage(syncOperation2, j);
                                                }
                                                long j7 = j;
                                                account2 = account3;
                                                source4 = source5;
                                                extras4 = extras6;
                                                int i2422 = reason;
                                                i = targetSyncState;
                                                int i2522 = syncExemptionFlag;
                                                extras5 = extras4;
                                                account6 = account2;
                                                source2 = source4;
                                                accounts3 = accounts8;
                                                i2 = i20;
                                                syncAdapterInfo = serviceInfo6;
                                                z = z5;
                                                syncableAuthorities3 = syncableAuthorities4;
                                                i6 = userId;
                                            } else if (isLoggable) {
                                                Log.d("SyncManager", "scheduleSync: sync of , " + authority + " is not allowed, dropping request");
                                            }
                                        }
                                        syncAllowed = true;
                                        if (!syncAllowed) {
                                        }
                                    }
                                    int i29 = reason;
                                    int i30 = syncExemptionFlag;
                                    account6 = account;
                                    accounts3 = accounts8;
                                    i2 = i20;
                                    syncAdapterInfo = serviceInfo6;
                                    syncableAuthorities3 = syncableAuthorities4;
                                    source2 = source9;
                                    extras2 = extras9;
                                    String str9 = requestedAuthority;
                                    i = i21;
                                    z = z5;
                                }
                                String str422 = requestedAuthority;
                            }
                        }
                        syncAdapterInfo = serviceInfo2;
                        accounts3 = accounts7;
                    }
                    accounts2 = accounts;
                    extras3 = extras2;
                    source3 = source2;
                    z2 = z;
                    i4 = i2;
                } else {
                    accounts2 = accounts;
                    extras3 = extras2;
                    source3 = source2;
                    z2 = z;
                    i4 = i2;
                    i3 = length2;
                }
                long j8 = minDelayMillis;
                i11 = i4 + 1;
                int i31 = reason;
                i7 = targetSyncState;
                int i32 = syncExemptionFlag;
                extras2 = extras3;
                source7 = source3;
                accounts = accounts2;
                z4 = z2;
                length2 = i3;
                i6 = userId;
                str = requestedAuthority;
            }
            Bundle bundle = extras2;
            int i33 = source2;
            boolean z10 = z;
            long j9 = minDelayMillis;
            return;
        }
        accounts = accounts5;
        if (!ArrayUtils.isEmpty(accounts)) {
        }
    }

    public static /* synthetic */ void lambda$scheduleSync$4(SyncManager syncManager, AccountAndUser account, int userId, int reason, String authority, Bundle finalExtras, int targetSyncState, long minDelayMillis, int syncExemptionFlag, Bundle result) {
        Bundle bundle = result;
        if (bundle == null || !bundle.getBoolean("booleanResult")) {
            AccountAndUser accountAndUser = account;
            return;
        }
        syncManager.scheduleSync(account.account, userId, reason, authority, finalExtras, targetSyncState, minDelayMillis, true, syncExemptionFlag);
    }

    public static /* synthetic */ void lambda$scheduleSync$5(SyncManager syncManager, AccountAndUser account, int reason, String authority, Bundle finalExtras, int targetSyncState, long minDelayMillis, int syncExemptionFlag) {
        AccountAndUser accountAndUser = account;
        syncManager.scheduleSync(accountAndUser.account, accountAndUser.userId, reason, authority, finalExtras, targetSyncState, minDelayMillis, false, syncExemptionFlag);
    }

    public int computeSyncable(Account account, int userId, String authority, boolean checkAccountAccess) {
        int status = getIsSyncable(account, userId, authority);
        if (status == 0) {
            return 0;
        }
        RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo = this.mSyncAdapters.getServiceInfo(SyncAdapterType.newKey(authority, account.type), userId);
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
        if (!checkAccountAccess || canAccessAccount(account, owningPackage, owningUid)) {
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
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void removeSyncsForAuthority(SyncStorageEngine.EndPoint info, String why) {
        this.mLogger.log("removeSyncsForAuthority: ", info);
        verifyJobScheduler();
        for (SyncOperation op : getAllPendingSyncs()) {
            if (op.target.matchesSpec(info)) {
                this.mLogger.log("canceling: ", op);
                cancelJob(op, why);
            }
        }
    }

    public void removePeriodicSync(SyncStorageEngine.EndPoint target, Bundle extras, String why) {
        SyncHandler syncHandler = this.mSyncHandler;
        SyncHandler syncHandler2 = this.mSyncHandler;
        Message m = syncHandler.obtainMessage(14, Pair.create(target, why));
        m.setData(extras);
        m.sendToTarget();
    }

    public void updateOrAddPeriodicSync(SyncStorageEngine.EndPoint target, long pollFrequency, long flex, Bundle extras) {
        UpdatePeriodicSyncMessagePayload payload = new UpdatePeriodicSyncMessagePayload(target, pollFrequency, flex, extras);
        this.mSyncHandler.obtainMessage(13, payload).sendToTarget();
    }

    public List<PeriodicSync> getPeriodicSyncs(SyncStorageEngine.EndPoint target) {
        List<SyncOperation> ops = getAllPendingSyncs();
        List<PeriodicSync> periodicSyncs = new ArrayList<>();
        for (SyncOperation op : ops) {
            if (!op.isPeriodic) {
                SyncStorageEngine.EndPoint endPoint = target;
            } else if (op.target.matchesSpec(target)) {
                PeriodicSync periodicSync = new PeriodicSync(op.target.account, op.target.provider, op.extras, op.periodMillis / 1000, op.flexMillis / 1000);
                periodicSyncs.add(periodicSync);
            }
        }
        SyncStorageEngine.EndPoint endPoint2 = target;
        return periodicSyncs;
    }

    public void scheduleLocalSync(Account account, int userId, int reason, String authority, int syncExemptionFlag) {
        Bundle extras = new Bundle();
        extras.putBoolean("upload", true);
        scheduleSync(account, userId, reason, authority, extras, -2, LOCAL_SYNC_DELAY, true, syncExemptionFlag);
    }

    public SyncAdapterType[] getSyncAdapterTypes(int userId) {
        Collection<RegisteredServicesCache.ServiceInfo<SyncAdapterType>> serviceInfos = this.mSyncAdapters.getAllServices(userId);
        SyncAdapterType[] types = new SyncAdapterType[serviceInfos.size()];
        int i = 0;
        for (RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo : serviceInfos) {
            types[i] = (SyncAdapterType) serviceInfo.type;
            i++;
        }
        return types;
    }

    public String[] getSyncAdapterPackagesForAuthorityAsUser(String authority, int userId) {
        return this.mSyncAdapters.getSyncAdapterPackagesForAuthority(authority, userId);
    }

    /* access modifiers changed from: private */
    public void sendSyncFinishedOrCanceledMessage(ActiveSyncContext syncContext, SyncResult syncResult) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "sending MESSAGE_SYNC_FINISHED");
        }
        Message msg = this.mSyncHandler.obtainMessage();
        msg.what = 1;
        msg.obj = new SyncFinishedOrCancelledMessagePayload(syncContext, syncResult);
        this.mSyncHandler.sendMessage(msg);
    }

    private void sendCancelSyncsMessage(SyncStorageEngine.EndPoint info, Bundle extras, String why) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "sending MESSAGE_CANCEL");
        }
        this.mLogger.log("sendCancelSyncsMessage() ep=", info, " why=", why);
        Message msg = this.mSyncHandler.obtainMessage();
        msg.what = 6;
        msg.setData(extras);
        msg.obj = info;
        this.mSyncHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    public void postMonitorSyncProgressMessage(ActiveSyncContext activeSyncContext) {
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "posting MESSAGE_SYNC_MONITOR in 60s");
        }
        activeSyncContext.mBytesTransferredAtLastPoll = getTotalBytesTransferredByUid(activeSyncContext.mSyncAdapterUid);
        activeSyncContext.mLastPolledTimeElapsed = SystemClock.elapsedRealtime();
        this.mSyncHandler.sendMessageDelayed(this.mSyncHandler.obtainMessage(8, activeSyncContext), 60000);
    }

    /* access modifiers changed from: private */
    public void postScheduleSyncMessage(SyncOperation syncOperation, long minDelayMillis) {
        ScheduleSyncMessagePayload payload = new ScheduleSyncMessagePayload(syncOperation, minDelayMillis);
        SyncHandler syncHandler = this.mSyncHandler;
        SyncHandler syncHandler2 = this.mSyncHandler;
        syncHandler.obtainMessage(12, payload).sendToTarget();
    }

    /* access modifiers changed from: private */
    public long getTotalBytesTransferredByUid(int uid) {
        return TrafficStats.getUidRxBytes(uid) + TrafficStats.getUidTxBytes(uid);
    }

    /* access modifiers changed from: private */
    public void clearBackoffSetting(SyncStorageEngine.EndPoint target, String why) {
        Pair<Long, Long> backoff = this.mSyncStorageEngine.getBackoff(target);
        if (backoff == null || ((Long) backoff.first).longValue() != -1 || ((Long) backoff.second).longValue() != -1) {
            if (Log.isLoggable("SyncManager", 2)) {
                Slog.v("SyncManager", "Clearing backoffs for " + target);
            }
            this.mSyncStorageEngine.setBackoff(target, -1, -1);
            rescheduleSyncs(target, why);
        }
    }

    /* access modifiers changed from: private */
    public void increaseBackoffSetting(SyncStorageEngine.EndPoint target) {
        long newDelayInMs;
        SyncStorageEngine.EndPoint endPoint = target;
        long now = SystemClock.elapsedRealtime();
        Pair<Long, Long> previousSettings = this.mSyncStorageEngine.getBackoff(endPoint);
        long newDelayInMs2 = -1;
        if (previousSettings != null) {
            if (now < ((Long) previousSettings.first).longValue()) {
                if (Log.isLoggable("SyncManager", 2)) {
                    Slog.v("SyncManager", "Still in backoff, do not increase it. Remaining: " + ((((Long) previousSettings.first).longValue() - now) / 1000) + " seconds.");
                }
                return;
            }
            newDelayInMs2 = (long) (((float) ((Long) previousSettings.second).longValue()) * this.mConstants.getRetryTimeIncreaseFactor());
        }
        if (newDelayInMs2 <= 0) {
            long initialRetryMs = (long) (this.mConstants.getInitialSyncRetryTimeInSeconds() * 1000);
            newDelayInMs2 = jitterize(initialRetryMs, (long) (((double) initialRetryMs) * 1.1d));
        }
        long maxSyncRetryTimeInSeconds = (long) this.mConstants.getMaxSyncRetryTimeInSeconds();
        if (newDelayInMs2 > maxSyncRetryTimeInSeconds * 1000) {
            newDelayInMs = 1000 * maxSyncRetryTimeInSeconds;
        } else {
            newDelayInMs = newDelayInMs2;
        }
        long backoff = now + newDelayInMs;
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "Backoff until: " + backoff + ", delayTime: " + newDelayInMs);
        }
        long j = backoff;
        this.mSyncStorageEngine.setBackoff(endPoint, backoff, newDelayInMs);
        rescheduleSyncs(endPoint, "increaseBackoffSetting");
    }

    /* access modifiers changed from: private */
    public void rescheduleSyncs(SyncStorageEngine.EndPoint target, String why) {
        this.mLogger.log("rescheduleSyncs() ep=", target, " why=", why);
        int count = 0;
        for (SyncOperation op : getAllPendingSyncs()) {
            if (!op.isPeriodic && op.target.matchesSpec(target)) {
                count++;
                cancelJob(op, why);
                postScheduleSyncMessage(op, 0);
            }
        }
        if (Log.isLoggable("SyncManager", 2)) {
            Slog.v("SyncManager", "Rescheduled " + count + " syncs for " + target);
        }
    }

    /* access modifiers changed from: private */
    public void setDelayUntilTime(SyncStorageEngine.EndPoint target, long delayUntilSeconds) {
        long newDelayUntilTime;
        long delayUntil = 1000 * delayUntilSeconds;
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
        rescheduleSyncs(target, "delayUntil newDelayUntilTime: " + newDelayUntilTime);
    }

    /* access modifiers changed from: private */
    public boolean isAdapterDelayed(SyncStorageEngine.EndPoint target) {
        long now = SystemClock.elapsedRealtime();
        Pair<Long, Long> backoff = this.mSyncStorageEngine.getBackoff(target);
        if ((backoff == null || ((Long) backoff.first).longValue() == -1 || ((Long) backoff.first).longValue() <= now) && this.mSyncStorageEngine.getDelayUntilTime(target) <= now) {
            return false;
        }
        return true;
    }

    public void cancelActiveSync(SyncStorageEngine.EndPoint info, Bundle extras, String why) {
        sendCancelSyncsMessage(info, extras, why);
    }

    /* access modifiers changed from: private */
    public void scheduleSyncOperationH(SyncOperation syncOperation) {
        scheduleSyncOperationH(syncOperation, 0);
    }

    /* access modifiers changed from: private */
    public void scheduleSyncOperationH(SyncOperation syncOperation, long minDelay) {
        long minDelay2;
        boolean z;
        SyncOperation syncOperation2 = syncOperation;
        boolean isLoggable = Log.isLoggable("SyncManager", 2);
        if (syncOperation2 == null) {
            Slog.e("SyncManager", "Can't schedule null sync operation.");
            return;
        }
        if (!syncOperation.ignoreBackoff()) {
            Pair<Long, Long> backoff = this.mSyncStorageEngine.getBackoff(syncOperation2.target);
            if (backoff == null) {
                Slog.e("SyncManager", "Couldn't find backoff values for " + syncOperation2.target);
                backoff = new Pair<>(-1L, -1L);
            }
            long now = SystemClock.elapsedRealtime();
            long backoffDelay = ((Long) backoff.first).longValue() == -1 ? 0 : ((Long) backoff.first).longValue() - now;
            long delayUntil = this.mSyncStorageEngine.getDelayUntilTime(syncOperation2.target);
            long delayUntilDelay = delayUntil > now ? delayUntil - now : 0;
            if (isLoggable) {
                Slog.v("SyncManager", "backoff delay:" + backoffDelay + " delayUntil delay:" + delayUntilDelay);
            }
            Pair<Long, Long> pair = backoff;
            minDelay2 = Math.max(minDelay, Math.max(backoffDelay, delayUntilDelay));
        } else {
            minDelay2 = minDelay;
        }
        if (minDelay2 < 0) {
            minDelay2 = 0;
        }
        if (!syncOperation2.isPeriodic) {
            int inheritedSyncExemptionFlag = 0;
            Iterator<ActiveSyncContext> it = this.mActiveSyncContexts.iterator();
            while (it.hasNext()) {
                if (it.next().mSyncOperation.key.equals(syncOperation2.key)) {
                    if (isLoggable) {
                        Log.v("SyncManager", "Duplicate sync is already running. Not scheduling " + syncOperation2);
                    }
                    return;
                }
            }
            int duplicatesCount = 0;
            syncOperation2.expectedRuntime = SystemClock.elapsedRealtime() + minDelay2;
            List<SyncOperation> pending = getAllPendingSyncs();
            SyncOperation syncToRun = syncOperation2;
            for (SyncOperation op : pending) {
                if (!op.isPeriodic) {
                    if (op.key.equals(syncOperation2.key)) {
                        int duplicatesCount2 = duplicatesCount;
                        if (syncToRun.expectedRuntime > op.expectedRuntime) {
                            syncToRun = op;
                        }
                        duplicatesCount = duplicatesCount2 + 1;
                    } else {
                        int i = duplicatesCount;
                    }
                }
            }
            int i2 = duplicatesCount;
            if (duplicatesCount > 1) {
                Slog.e("SyncManager", "FATAL ERROR! File a bug if you see this.");
            }
            if (syncOperation2 != syncToRun && minDelay2 == 0 && syncToRun.syncExemptionFlag < syncOperation2.syncExemptionFlag) {
                syncToRun = syncOperation2;
                inheritedSyncExemptionFlag = Math.max(0, syncToRun.syncExemptionFlag);
            }
            for (SyncOperation op2 : pending) {
                if (!op2.isPeriodic && op2.key.equals(syncOperation2.key) && op2 != syncToRun) {
                    if (isLoggable) {
                        Slog.v("SyncManager", "Cancelling duplicate sync " + op2);
                    }
                    inheritedSyncExemptionFlag = Math.max(inheritedSyncExemptionFlag, op2.syncExemptionFlag);
                    cancelJob(op2, "scheduleSyncOperationH-duplicate");
                }
            }
            if (syncToRun != syncOperation2) {
                Slog.i("SyncManager", "Not scheduling because a duplicate exists:" + syncOperation2);
                return;
            } else if (inheritedSyncExemptionFlag > 0) {
                syncOperation2.syncExemptionFlag = inheritedSyncExemptionFlag;
            }
        }
        if (syncOperation2.jobId == -1) {
            syncOperation2.jobId = getUnusedJobIdH();
        }
        if (isLoggable) {
            Slog.v("SyncManager", "scheduling sync operation " + syncOperation.toString());
        }
        JobInfo.Builder b = new JobInfo.Builder(syncOperation2.jobId, new ComponentName(this.mContext, SyncJobService.class)).setExtras(syncOperation.toJobInfoExtras()).setRequiredNetworkType(syncOperation.isNotAllowedOnMetered() ? 2 : 1).setPersisted(true).setPriority(syncOperation.findPriority()).setFlags(syncOperation.isAppStandbyExempted() ? 8 : 0);
        if (syncOperation2.isPeriodic) {
            b.setPeriodic(syncOperation2.periodMillis, syncOperation2.flexMillis);
            z = true;
        } else {
            if (minDelay2 > 0) {
                b.setMinimumLatency(minDelay2);
            }
            z = true;
            getSyncStorageEngine().markPending(syncOperation2.target, true);
        }
        if (syncOperation2.extras.getBoolean("require_charging")) {
            b.setRequiresCharging(z);
        }
        if (syncOperation2.syncExemptionFlag == 2) {
            DeviceIdleController.LocalService dic = (DeviceIdleController.LocalService) LocalServices.getService(DeviceIdleController.LocalService.class);
            if (dic != null) {
                dic.addPowerSaveTempWhitelistApp(1000, syncOperation2.owningPackage, (long) (this.mConstants.getKeyExemptionTempWhitelistDurationInSeconds() * 1000), UserHandle.getUserId(syncOperation2.owningUid), false, "sync by top app");
            }
        }
        if (syncOperation.isAppStandbyExempted()) {
            UsageStatsManagerInternal usmi = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
            if (usmi != null) {
                usmi.reportExemptedSyncScheduled(syncOperation2.owningPackage, UserHandle.getUserId(syncOperation2.owningUid));
            }
        }
        getJobScheduler().scheduleAsPackage(b.build(), syncOperation2.owningPackage, syncOperation2.target.userId, syncOperation.wakeLockName());
    }

    public void clearScheduledSyncOperations(SyncStorageEngine.EndPoint info) {
        for (SyncOperation op : getAllPendingSyncs()) {
            if (!op.isPeriodic && op.target.matchesSpec(info)) {
                cancelJob(op, "clearScheduledSyncOperations");
                getSyncStorageEngine().markPending(op.target, false);
            }
        }
        this.mSyncStorageEngine.setBackoff(info, -1, -1);
    }

    public void cancelScheduledSyncOperation(SyncStorageEngine.EndPoint info, Bundle extras) {
        for (SyncOperation op : getAllPendingSyncs()) {
            if (!op.isPeriodic && op.target.matchesSpec(info) && syncExtrasEquals(extras, op.extras, false)) {
                cancelJob(op, "cancelScheduledSyncOperation");
            }
        }
        setAuthorityPendingState(info);
        if (!this.mSyncStorageEngine.isSyncPending(info)) {
            this.mSyncStorageEngine.setBackoff(info, -1, -1);
        }
    }

    /* access modifiers changed from: private */
    public void maybeRescheduleSync(SyncResult syncResult, SyncOperation operation) {
        boolean isLoggable = Log.isLoggable("SyncManager", 3);
        if (isLoggable) {
            Log.d("SyncManager", "encountered error(s) during the sync: " + syncResult + ", " + operation);
        }
        if (operation.extras.getBoolean("ignore_backoff", false)) {
            operation.extras.remove("ignore_backoff");
        }
        if (!operation.extras.getBoolean("do_not_retry", false) || syncResult.syncAlreadyInProgress) {
            if (operation.extras.getBoolean("upload", false) && !syncResult.syncAlreadyInProgress) {
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

    /* access modifiers changed from: private */
    public void onUserUnlocked(int userId) {
        AccountManagerService.getSingleton().validateAccounts(userId);
        this.mSyncAdapters.invalidateCache(userId);
        updateRunningAccounts(new SyncStorageEngine.EndPoint(null, null, userId));
        for (Account account : AccountManagerService.getSingleton().getAccounts(userId, this.mContext.getOpPackageName())) {
            scheduleSync(account, userId, -8, null, null, -1, 0);
        }
    }

    /* access modifiers changed from: private */
    public void onUserStopped(int userId) {
        updateRunningAccounts(null);
        cancelActiveSync(new SyncStorageEngine.EndPoint(null, null, userId), null, "onUserStopped");
    }

    /* access modifiers changed from: private */
    public void onUserRemoved(int userId) {
        this.mLogger.log("onUserRemoved: u", Integer.valueOf(userId));
        updateRunningAccounts(null);
        this.mSyncStorageEngine.doDatabaseCleanup(new Account[0], userId);
        for (SyncOperation op : getAllPendingSyncs()) {
            if (op.target.userId == userId) {
                cancelJob(op, "user removed u" + userId);
            }
        }
    }

    static Intent getAdapterBindIntent(Context context, ComponentName syncAdapterComponent, int userId) {
        Intent intent = new Intent();
        intent.setAction("android.content.SyncAdapter");
        intent.setComponent(syncAdapterComponent);
        intent.putExtra("android.intent.extra.client_label", 17041226);
        intent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivityAsUser(context, 0, new Intent("android.settings.SYNC_SETTINGS"), 0, null, UserHandle.of(userId)));
        return intent;
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, boolean dumpAll) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        dumpSyncState(ipw, new SyncAdapterStateFetcher());
        this.mConstants.dump(pw, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        dumpSyncAdapters(ipw);
        if (dumpAll) {
            ipw.println("Detailed Sync History");
            this.mLogger.dumpAll(pw);
        }
    }

    static String formatTime(long time) {
        if (time == 0) {
            return "N/A";
        }
        Time tobj = new Time();
        tobj.set(time);
        return tobj.format("%Y-%m-%d %H:%M:%S");
    }

    static /* synthetic */ int lambda$static$6(SyncOperation op1, SyncOperation op2) {
        int res = Integer.compare(op1.target.userId, op2.target.userId);
        if (res != 0) {
            return res;
        }
        Comparator<String> stringComparator = String.CASE_INSENSITIVE_ORDER;
        int res2 = stringComparator.compare(op1.target.account.type, op2.target.account.type);
        if (res2 != 0) {
            return res2;
        }
        int res3 = stringComparator.compare(op1.target.account.name, op2.target.account.name);
        if (res3 != 0) {
            return res3;
        }
        int res4 = stringComparator.compare(op1.target.provider, op2.target.provider);
        if (res4 != 0) {
            return res4;
        }
        int res5 = Integer.compare(op1.reason, op2.reason);
        if (res5 != 0) {
            return res5;
        }
        int res6 = Long.compare(op1.periodMillis, op2.periodMillis);
        if (res6 != 0) {
            return res6;
        }
        int res7 = Long.compare(op1.expectedRuntime, op2.expectedRuntime);
        if (res7 != 0) {
            return res7;
        }
        int res8 = Long.compare((long) op1.jobId, (long) op2.jobId);
        if (res8 != 0) {
            return res8;
        }
        return 0;
    }

    static /* synthetic */ int lambda$static$7(SyncOperation op1, SyncOperation op2) {
        int res = Long.compare(op1.expectedRuntime, op2.expectedRuntime);
        if (res != 0) {
            return res;
        }
        return sOpDumpComparator.compare(op1, op2);
    }

    private static <T> int countIf(Collection<T> col, Predicate<T> p) {
        int ret = 0;
        for (T item : col) {
            if (p.test(item)) {
                ret++;
            }
        }
        return ret;
    }

    /* access modifiers changed from: protected */
    public void dumpPendingSyncs(PrintWriter pw, SyncAdapterStateFetcher buckets) {
        List<SyncOperation> pendingSyncs = getAllPendingSyncs();
        pw.print("Pending Syncs: ");
        pw.println(countIf(pendingSyncs, $$Lambda$SyncManager$rDUHWai3SU0BXk1TE0bLDap9gVc.INSTANCE));
        Collections.sort(pendingSyncs, sOpRuntimeComparator);
        int count = 0;
        for (SyncOperation op : pendingSyncs) {
            if (!op.isPeriodic) {
                pw.println(op.dump(null, false, buckets));
                count++;
            }
        }
        pw.println();
    }

    static /* synthetic */ boolean lambda$dumpPendingSyncs$8(SyncOperation op) {
        return !op.isPeriodic;
    }

    /* access modifiers changed from: protected */
    public void dumpPeriodicSyncs(PrintWriter pw, SyncAdapterStateFetcher buckets) {
        List<SyncOperation> pendingSyncs = getAllPendingSyncs();
        pw.print("Periodic Syncs: ");
        pw.println(countIf(pendingSyncs, $$Lambda$SyncManager$ag0YGuZ1oL06fytmNlyErbNyYcw.INSTANCE));
        Collections.sort(pendingSyncs, sOpDumpComparator);
        int count = 0;
        for (SyncOperation op : pendingSyncs) {
            if (op.isPeriodic) {
                pw.println(op.dump(null, false, buckets));
                count++;
            }
        }
        pw.println();
    }

    public static StringBuilder formatDurationHMS(StringBuilder sb, long duration) {
        long duration2 = duration / 1000;
        if (duration2 < 0) {
            sb.append('-');
            duration2 = -duration2;
        }
        long seconds = duration2 % 60;
        long duration3 = duration2 / 60;
        long minutes = duration3 % 60;
        long duration4 = duration3 / 60;
        long hours = duration4 % 24;
        long days = duration4 / 24;
        boolean print = false;
        if (days > 0) {
            sb.append(days);
            sb.append('d');
            print = true;
        }
        if (!printTwoDigitNumber(sb, seconds, 's', printTwoDigitNumber(sb, minutes, 'm', printTwoDigitNumber(sb, hours, 'h', print)))) {
            sb.append("0s");
        }
        return sb;
    }

    private static boolean printTwoDigitNumber(StringBuilder sb, long value, char unit, boolean always) {
        if (!always && value == 0) {
            return false;
        }
        if (always && value < 10) {
            sb.append('0');
        }
        sb.append(value);
        sb.append(unit);
        return true;
    }

    /* access modifiers changed from: protected */
    public void dumpSyncState(PrintWriter pw, SyncAdapterStateFetcher buckets) {
        int i;
        ArrayList<Pair<SyncStorageEngine.EndPoint, SyncStatusInfo>> statuses;
        PrintWriter printWriter = pw;
        StringBuilder sb = new StringBuilder();
        printWriter.print("Data connected: ");
        printWriter.println(this.mDataConnectionIsConnected);
        printWriter.print("Battery saver: ");
        char c = 0;
        printWriter.println(this.mPowerManager != null && this.mPowerManager.isPowerSaveMode());
        printWriter.print("Background network restriction: ");
        ConnectivityManager cm = getConnectivityManager();
        int status = cm == null ? -1 : cm.getRestrictBackgroundStatus();
        switch (status) {
            case 1:
                printWriter.println(" disabled");
                break;
            case 2:
                printWriter.println(" whitelisted");
                break;
            case 3:
                printWriter.println(" enabled");
                break;
            default:
                printWriter.print("Unknown(");
                printWriter.print(status);
                printWriter.println(")");
                break;
        }
        printWriter.print("Auto sync: ");
        List<UserInfo> users = getAllUsers();
        if (users != null) {
            for (UserInfo user : users) {
                printWriter.print("u" + user.id + "=" + this.mSyncStorageEngine.getMasterSyncAutomatically(user.id) + " ");
            }
            pw.println();
        }
        printWriter.print("Memory low: ");
        printWriter.println(this.mStorageIsLow);
        printWriter.print("Device idle: ");
        printWriter.println(this.mDeviceIsIdle);
        printWriter.print("Reported active: ");
        printWriter.println(this.mReportedSyncActive);
        printWriter.print("Clock valid: ");
        printWriter.println(this.mSyncStorageEngine.isClockValid());
        AccountAndUser[] accounts = AccountManagerService.getSingleton().getAllAccounts();
        printWriter.print("Accounts: ");
        if (accounts != INITIAL_ACCOUNTS_ARRAY) {
            printWriter.println(accounts.length);
        } else {
            printWriter.println("not known yet");
        }
        long now = SystemClock.elapsedRealtime();
        printWriter.print("Now: ");
        printWriter.print(now);
        printWriter.println(" (" + formatTime(System.currentTimeMillis()) + ")");
        sb.setLength(0);
        printWriter.print("Uptime: ");
        printWriter.print(formatDurationHMS(sb, now));
        pw.println();
        printWriter.print("Time spent syncing: ");
        sb.setLength(0);
        printWriter.print(formatDurationHMS(sb, this.mSyncHandler.mSyncTimeTracker.timeSpentSyncing()));
        printWriter.print(", sync ");
        printWriter.print(this.mSyncHandler.mSyncTimeTracker.mLastWasSyncing ? BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS : "not ");
        printWriter.println("in progress");
        pw.println();
        printWriter.println("Active Syncs: " + this.mActiveSyncContexts.size());
        PackageManager pm = this.mContext.getPackageManager();
        Iterator<ActiveSyncContext> it = this.mActiveSyncContexts.iterator();
        while (it.hasNext()) {
            ActiveSyncContext activeSyncContext = it.next();
            printWriter.print("  ");
            sb.setLength(0);
            printWriter.print(formatDurationHMS(sb, now - activeSyncContext.mStartTime));
            printWriter.print(" - ");
            printWriter.print(activeSyncContext.mSyncOperation.dump(pm, false, buckets));
            pw.println();
        }
        SyncAdapterStateFetcher syncAdapterStateFetcher = buckets;
        pw.println();
        dumpPendingSyncs(pw, buckets);
        dumpPeriodicSyncs(pw, buckets);
        printWriter.println("Sync Status");
        ArrayList<Pair<SyncStorageEngine.EndPoint, SyncStatusInfo>> statuses2 = new ArrayList<>();
        this.mSyncStorageEngine.resetTodayStats(false);
        int length = accounts.length;
        int i2 = 0;
        while (i2 < length) {
            AccountAndUser account = accounts[i2];
            List<UserInfo> users2 = users;
            Object[] objArr = new Object[3];
            objArr[c] = "XXXXXXXXX";
            objArr[1] = Integer.valueOf(account.userId);
            objArr[2] = account.account.type;
            printWriter.printf("Account %s u%d %s\n", objArr);
            printWriter.println("=======================================================================");
            PrintTable table = new PrintTable(16);
            Object[] objArr2 = new Object[16];
            objArr2[c] = "Authority";
            objArr2[1] = "Syncable";
            objArr2[2] = "Enabled";
            objArr2[3] = "Stats";
            objArr2[4] = "Loc";
            objArr2[5] = "Poll";
            objArr2[6] = "Per";
            objArr2[7] = "Feed";
            objArr2[8] = "User";
            objArr2[9] = "Othr";
            objArr2[10] = "Tot";
            objArr2[11] = "Fail";
            objArr2[12] = "Can";
            objArr2[13] = "Time";
            objArr2[14] = "Last Sync";
            objArr2[15] = "Backoff";
            table.set(0, 0, objArr2);
            List<RegisteredServicesCache.ServiceInfo<SyncAdapterType>> sorted = Lists.newArrayList();
            sorted.addAll(this.mSyncAdapters.getAllServices(account.userId));
            Collections.sort(sorted, new Comparator<RegisteredServicesCache.ServiceInfo<SyncAdapterType>>() {
                public int compare(RegisteredServicesCache.ServiceInfo<SyncAdapterType> lhs, RegisteredServicesCache.ServiceInfo<SyncAdapterType> rhs) {
                    return ((SyncAdapterType) lhs.type).authority.compareTo(((SyncAdapterType) rhs.type).authority);
                }
            });
            Iterator<RegisteredServicesCache.ServiceInfo<SyncAdapterType>> it2 = sorted.iterator();
            while (it2.hasNext()) {
                RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterType = (RegisteredServicesCache.ServiceInfo) it2.next();
                Iterator<RegisteredServicesCache.ServiceInfo<SyncAdapterType>> it3 = it2;
                List<RegisteredServicesCache.ServiceInfo<SyncAdapterType>> sorted2 = sorted;
                if (!((SyncAdapterType) syncAdapterType.type).accountType.equals(account.account.type)) {
                    it2 = it3;
                    sorted = sorted2;
                } else {
                    int row = table.getNumRows();
                    AccountAndUser[] accounts2 = accounts;
                    PackageManager pm2 = pm;
                    int i3 = length;
                    RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo = syncAdapterType;
                    Pair<SyncStorageEngine.AuthorityInfo, SyncStatusInfo> syncAuthoritySyncStatus = this.mSyncStorageEngine.getCopyOfAuthorityWithSyncStatus(new SyncStorageEngine.EndPoint(account.account, ((SyncAdapterType) syncAdapterType.type).authority, account.userId));
                    SyncStorageEngine.AuthorityInfo settings = (SyncStorageEngine.AuthorityInfo) syncAuthoritySyncStatus.first;
                    SyncStatusInfo status2 = (SyncStatusInfo) syncAuthoritySyncStatus.second;
                    statuses2.add(Pair.create(settings.target, status2));
                    String authority = settings.target.provider;
                    Pair<SyncStorageEngine.AuthorityInfo, SyncStatusInfo> pair = syncAuthoritySyncStatus;
                    if (authority.length() > 50) {
                        authority = authority.substring(authority.length() - 50);
                    }
                    table.set(row, 0, authority, Integer.valueOf(settings.syncable), Boolean.valueOf(settings.enabled));
                    $$Lambda$SyncManager$9EoLpTk5JrHZn9RuS0lqCVrpRw r5 = new QuadConsumer(sb, table) {
                        private final /* synthetic */ StringBuilder f$0;
                        private final /* synthetic */ SyncManager.PrintTable f$1;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                        }

                        public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
                            SyncManager.lambda$dumpSyncState$10(this.f$0, this.f$1, (String) obj, (SyncStatusInfo.Stats) obj2, (Function) obj3, (Integer) obj4);
                        }
                    };
                    StringBuilder sb2 = sb;
                    String str = authority;
                    AccountAndUser account2 = account;
                    r5.accept("Total", status2.totalStats, $$Lambda$SyncManager$pdoEVnuSkmOrvULQ9M7IclU5vw.INSTANCE, Integer.valueOf(row));
                    r5.accept("Today", status2.todayStats, new Function() {
                        public final Object apply(Object obj) {
                            return SyncManager.this.zeroToEmpty(((Integer) obj).intValue());
                        }
                    }, Integer.valueOf(row + 1));
                    r5.accept("Yestr", status2.yesterdayStats, new Function() {
                        public final Object apply(Object obj) {
                            return SyncManager.this.zeroToEmpty(((Integer) obj).intValue());
                        }
                    }, Integer.valueOf(row + 2));
                    int row1 = row;
                    if (settings.delayUntil > now) {
                        int row12 = row1 + 1;
                        StringBuilder sb3 = new StringBuilder();
                        $$Lambda$SyncManager$9EoLpTk5JrHZn9RuS0lqCVrpRw r32 = r5;
                        sb3.append("D: ");
                        statuses = statuses2;
                        sb3.append((settings.delayUntil - now) / 1000);
                        table.set(row1, 15, sb3.toString());
                        if (settings.backoffTime > now) {
                            int row13 = row12 + 1;
                            StringBuilder sb4 = new StringBuilder();
                            sb4.append("B: ");
                            i = i2;
                            sb4.append((settings.backoffTime - now) / 1000);
                            table.set(row12, 15, sb4.toString());
                            int i4 = row13 + 1;
                            table.set(row13, 15, Long.valueOf(settings.backoffDelay / 1000));
                        } else {
                            i = i2;
                            int i5 = row12;
                        }
                    } else {
                        $$Lambda$SyncManager$9EoLpTk5JrHZn9RuS0lqCVrpRw r322 = r5;
                        statuses = statuses2;
                        i = i2;
                    }
                    int row14 = row;
                    if (status2.lastSuccessTime != 0) {
                        int row15 = row14 + 1;
                        table.set(row14, 14, SyncStorageEngine.SOURCES[status2.lastSuccessSource] + " SUCCESS");
                        row14 = row15 + 1;
                        table.set(row15, 14, formatTime(status2.lastSuccessTime));
                    }
                    if (status2.lastFailureTime != 0) {
                        int row16 = row14 + 1;
                        table.set(row14, 14, SyncStorageEngine.SOURCES[status2.lastFailureSource] + " FAILURE");
                        int row17 = row16 + 1;
                        table.set(row16, 14, formatTime(status2.lastFailureTime));
                        int row18 = row17 + 1;
                        table.set(row17, 14, status2.lastFailureMesg);
                    }
                    it2 = it3;
                    sorted = sorted2;
                    accounts = accounts2;
                    pm = pm2;
                    length = i3;
                    sb = sb2;
                    account = account2;
                    statuses2 = statuses;
                    i2 = i;
                    SyncAdapterStateFetcher syncAdapterStateFetcher2 = buckets;
                }
            }
            List<RegisteredServicesCache.ServiceInfo<SyncAdapterType>> list = sorted;
            AccountAndUser[] accountAndUserArr = accounts;
            PackageManager packageManager = pm;
            ArrayList<Pair<SyncStorageEngine.EndPoint, SyncStatusInfo>> arrayList = statuses2;
            int i6 = length;
            AccountAndUser accountAndUser = account;
            table.writeTo(printWriter);
            i2++;
            users = users2;
            c = 0;
            SyncAdapterStateFetcher syncAdapterStateFetcher3 = buckets;
        }
        List<UserInfo> list2 = users;
        AccountAndUser[] accountAndUserArr2 = accounts;
        PackageManager packageManager2 = pm;
        ArrayList<Pair<SyncStorageEngine.EndPoint, SyncStatusInfo>> statuses3 = statuses2;
        dumpSyncHistory(pw);
        pw.println();
        printWriter.println("Per Adapter History");
        printWriter.println("(SERVER is now split up to FEED and OTHER)");
        int i7 = 0;
        while (true) {
            ArrayList<Pair<SyncStorageEngine.EndPoint, SyncStatusInfo>> statuses4 = statuses3;
            if (i7 < statuses4.size()) {
                Pair<SyncStorageEngine.EndPoint, SyncStatusInfo> event = statuses4.get(i7);
                printWriter.print("  ");
                printWriter.print("XXXXXXXXX");
                printWriter.print('/');
                printWriter.print(((SyncStorageEngine.EndPoint) event.first).account.type);
                printWriter.print(" u");
                printWriter.print(((SyncStorageEngine.EndPoint) event.first).userId);
                printWriter.print(" [");
                printWriter.print(((SyncStorageEngine.EndPoint) event.first).provider);
                printWriter.print("]");
                pw.println();
                printWriter.println("    Per source last syncs:");
                for (int j = 0; j < SyncStorageEngine.SOURCES.length; j++) {
                    printWriter.print("      ");
                    printWriter.print(String.format("%8s", new Object[]{SyncStorageEngine.SOURCES[j]}));
                    printWriter.print("  Success: ");
                    printWriter.print(formatTime(((SyncStatusInfo) event.second).perSourceLastSuccessTimes[j]));
                    printWriter.print("  Failure: ");
                    printWriter.println(formatTime(((SyncStatusInfo) event.second).perSourceLastFailureTimes[j]));
                }
                printWriter.println("    Last syncs:");
                for (int j2 = 0; j2 < ((SyncStatusInfo) event.second).getEventCount(); j2++) {
                    printWriter.print("      ");
                    printWriter.print(formatTime(((SyncStatusInfo) event.second).getEventTime(j2)));
                    printWriter.print(' ');
                    printWriter.print(((SyncStatusInfo) event.second).getEvent(j2));
                    pw.println();
                }
                if (((SyncStatusInfo) event.second).getEventCount() == 0) {
                    printWriter.println("      N/A");
                }
                i7++;
                statuses3 = statuses4;
            } else {
                return;
            }
        }
    }

    static /* synthetic */ void lambda$dumpSyncState$10(StringBuilder sb, PrintTable table, String label, SyncStatusInfo.Stats stats, Function filter, Integer r) {
        sb.setLength(0);
        table.set(r.intValue(), 3, label, filter.apply(Integer.valueOf(stats.numSourceLocal)), filter.apply(Integer.valueOf(stats.numSourcePoll)), filter.apply(Integer.valueOf(stats.numSourcePeriodic)), filter.apply(Integer.valueOf(stats.numSourceFeed)), filter.apply(Integer.valueOf(stats.numSourceUser)), filter.apply(Integer.valueOf(stats.numSourceOther)), filter.apply(Integer.valueOf(stats.numSyncs)), filter.apply(Integer.valueOf(stats.numFailures)), filter.apply(Integer.valueOf(stats.numCancels)), formatDurationHMS(sb, stats.totalElapsedTime));
    }

    /* access modifiers changed from: private */
    public String zeroToEmpty(int value) {
        return value != 0 ? Integer.toString(value) : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    }

    private void dumpTimeSec(PrintWriter pw, long time) {
        pw.print(time / 1000);
        pw.print('.');
        pw.print((time / 100) % 10);
        pw.print('s');
    }

    private void dumpDayStatistic(PrintWriter pw, SyncStorageEngine.DayStats ds) {
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

    /* access modifiers changed from: protected */
    public void dumpSyncHistory(PrintWriter pw) {
        dumpRecentHistory(pw);
        dumpDayStatistics(pw);
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x0438  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x044a  */
    private void dumpRecentHistory(PrintWriter pw) {
        int maxAccount;
        int maxAuthority;
        int N;
        ArrayList<SyncStorageEngine.SyncHistoryItem> items;
        SyncManager syncManager;
        PrintWriter printWriter;
        int N2;
        ArrayList<SyncStorageEngine.SyncHistoryItem> items2;
        int N3;
        PackageManager pm;
        Map<String, Long> lastTimeMap;
        String authorityName;
        long totalTimes;
        String authorityName2;
        String accountKey;
        String authorityName3;
        long elapsedTime;
        String format;
        int N4;
        ArrayList<SyncStorageEngine.SyncHistoryItem> items3;
        String diffString;
        Iterator<SyncStorageEngine.SyncHistoryItem> it;
        String authorityName4;
        String accountKey2;
        AuthoritySyncStats authoritySyncStats;
        SyncManager syncManager2 = this;
        PrintWriter printWriter2 = pw;
        ArrayList<SyncStorageEngine.SyncHistoryItem> items4 = syncManager2.mSyncStorageEngine.getSyncHistory();
        if (items4 == null || items4.size() <= 0) {
            SyncManager syncManager3 = syncManager2;
            PrintWriter printWriter3 = printWriter2;
            ArrayList<SyncStorageEngine.SyncHistoryItem> arrayList = items4;
            return;
        }
        HashMap newHashMap = Maps.newHashMap();
        long totalElapsedTime = 0;
        long totalTimes2 = 0;
        int N5 = items4.size();
        int maxAuthority2 = 0;
        int maxAccount2 = 0;
        Iterator<SyncStorageEngine.SyncHistoryItem> it2 = items4.iterator();
        while (it2.hasNext()) {
            SyncStorageEngine.SyncHistoryItem item = it2.next();
            SyncStorageEngine.AuthorityInfo authorityInfo = syncManager2.mSyncStorageEngine.getAuthority(item.authorityId);
            if (authorityInfo != null) {
                authorityName4 = authorityInfo.target.provider;
                StringBuilder sb = new StringBuilder();
                it = it2;
                sb.append(authorityInfo.target.account.name);
                sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
                sb.append(authorityInfo.target.account.type);
                sb.append(" u");
                sb.append(authorityInfo.target.userId);
                accountKey2 = sb.toString();
            } else {
                it = it2;
                authorityName4 = "Unknown";
                accountKey2 = "Unknown";
            }
            int length = authorityName4.length();
            if (length > maxAuthority2) {
                maxAuthority2 = length;
            }
            int length2 = accountKey2.length();
            if (length2 > maxAccount2) {
                maxAccount2 = length2;
            }
            int maxAuthority3 = maxAuthority2;
            int maxAccount3 = maxAccount2;
            long elapsedTime2 = item.elapsedTime;
            long totalTimes3 = totalTimes2 + 1;
            AuthoritySyncStats authoritySyncStats2 = (AuthoritySyncStats) newHashMap.get(authorityName4);
            long totalElapsedTime2 = totalElapsedTime + elapsedTime2;
            if (authoritySyncStats2 == null) {
                authoritySyncStats = new AuthoritySyncStats(authorityName4);
                newHashMap.put(authorityName4, authoritySyncStats);
            } else {
                authoritySyncStats = authoritySyncStats2;
            }
            long totalTimes4 = totalTimes3;
            authoritySyncStats.elapsedTime += elapsedTime2;
            authoritySyncStats.times++;
            Map<String, AccountSyncStats> accountMap = authoritySyncStats.accountMap;
            AccountSyncStats accountSyncStats = accountMap.get(accountKey2);
            if (accountSyncStats == null) {
                AuthoritySyncStats authoritySyncStats3 = authoritySyncStats;
                accountSyncStats = new AccountSyncStats(accountKey2);
                accountMap.put(accountKey2, accountSyncStats);
            }
            accountSyncStats.elapsedTime += elapsedTime2;
            accountSyncStats.times++;
            it2 = it;
            maxAuthority2 = maxAuthority3;
            maxAccount2 = maxAccount3;
            totalElapsedTime = totalElapsedTime2;
            totalTimes2 = totalTimes4;
        }
        if (totalElapsedTime > 0) {
            pw.println();
            printWriter2.printf("Detailed Statistics (Recent history):  %d (# of times) %ds (sync time)\n", new Object[]{Long.valueOf(totalTimes2), Long.valueOf(totalElapsedTime / 1000)});
            List<AuthoritySyncStats> sortedAuthorities = new ArrayList<>(newHashMap.values());
            Collections.sort(sortedAuthorities, new Comparator<AuthoritySyncStats>() {
                public int compare(AuthoritySyncStats lhs, AuthoritySyncStats rhs) {
                    int compare = Integer.compare(rhs.times, lhs.times);
                    if (compare == 0) {
                        return Long.compare(rhs.elapsedTime, lhs.elapsedTime);
                    }
                    return compare;
                }
            });
            int maxLength = Math.max(maxAuthority2, maxAccount2 + 3);
            int padLength = 4 + maxLength + 2 + 10 + 11;
            char[] chars = new char[padLength];
            Arrays.fill(chars, '-');
            String separator = new String(chars);
            HashMap hashMap = newHashMap;
            int i = padLength;
            char[] cArr = chars;
            String timesStr = String.format("  %%-%ds: %%-9s  %%-11s\n", new Object[]{Integer.valueOf(maxLength + 2)});
            items = items4;
            String accountFormat = String.format("    %%-%ds:   %%-9s  %%-11s\n", new Object[]{Integer.valueOf(maxLength)});
            printWriter2.println(separator);
            Iterator<AuthoritySyncStats> it3 = sortedAuthorities.iterator();
            while (it3.hasNext()) {
                AuthoritySyncStats authoritySyncStats4 = it3.next();
                List<AuthoritySyncStats> sortedAuthorities2 = sortedAuthorities;
                String name = authoritySyncStats4.name;
                int maxLength2 = maxLength;
                Iterator<AuthoritySyncStats> it4 = it3;
                long elapsedTime3 = authoritySyncStats4.elapsedTime;
                int N6 = N5;
                int times = authoritySyncStats4.times;
                int maxAuthority4 = maxAuthority2;
                int maxAccount4 = maxAccount2;
                String separator2 = separator;
                String timeStr = String.format("%ds/%d%%", new Object[]{Long.valueOf(elapsedTime3 / 1000), Long.valueOf((elapsedTime3 * 100) / totalElapsedTime)});
                int i2 = times;
                String timesStr2 = String.format("%d/%d%%", new Object[]{Integer.valueOf(times), Long.valueOf(((long) (times * 100)) / totalTimes2)});
                PrintWriter printWriter4 = pw;
                printWriter4.printf(timesStr, new Object[]{name, timesStr2, timeStr});
                List<AccountSyncStats> sortedAccounts = new ArrayList<>(authoritySyncStats4.accountMap.values());
                Collections.sort(sortedAccounts, new Comparator<AccountSyncStats>() {
                    public int compare(AccountSyncStats lhs, AccountSyncStats rhs) {
                        int compare = Integer.compare(rhs.times, lhs.times);
                        if (compare == 0) {
                            return Long.compare(rhs.elapsedTime, lhs.elapsedTime);
                        }
                        return compare;
                    }
                });
                Iterator<AccountSyncStats> it5 = sortedAccounts.iterator();
                while (it5.hasNext()) {
                    String str = timeStr;
                    AccountSyncStats stats = it5.next();
                    long elapsedTime4 = stats.elapsedTime;
                    String str2 = timesStr2;
                    int times2 = stats.times;
                    AccountSyncStats accountSyncStats2 = stats;
                    String authorityFormat = timesStr;
                    timeStr = String.format("%ds/%d%%", new Object[]{Long.valueOf(elapsedTime4 / 1000), Long.valueOf((elapsedTime4 * 100) / totalElapsedTime)});
                    String timesStr3 = String.format("%d/%d%%", new Object[]{Integer.valueOf(times2), Long.valueOf(((long) (times2 * 100)) / totalTimes2)});
                    printWriter4.printf(accountFormat, new Object[]{"XXXXXXXXX", timesStr3, timeStr});
                    int i3 = times2;
                    timesStr2 = timesStr3;
                    timesStr = authorityFormat;
                    sortedAccounts = sortedAccounts;
                    name = name;
                    it5 = it5;
                    totalElapsedTime = totalElapsedTime;
                }
                String str3 = timeStr;
                String str4 = timesStr2;
                String str5 = timesStr;
                long j = totalElapsedTime;
                List<AccountSyncStats> list = sortedAccounts;
                String str6 = name;
                String timeStr2 = separator2;
                printWriter4.println(timeStr2);
                printWriter2 = printWriter4;
                sortedAuthorities = sortedAuthorities2;
                maxLength = maxLength2;
                it3 = it4;
                N5 = N6;
                maxAuthority2 = maxAuthority4;
                maxAccount2 = maxAccount4;
                separator = timeStr2;
                syncManager2 = this;
            }
            syncManager = syncManager2;
            long j2 = totalElapsedTime;
            N = N5;
            maxAuthority = maxAuthority2;
            maxAccount = maxAccount2;
            printWriter = printWriter2;
        } else {
            syncManager = syncManager2;
            items = items4;
            HashMap hashMap2 = newHashMap;
            long j3 = totalElapsedTime;
            N = N5;
            maxAuthority = maxAuthority2;
            maxAccount = maxAccount2;
            printWriter = printWriter2;
        }
        pw.println();
        printWriter.println("Recent Sync History");
        printWriter.println("(SERVER is now split up to FEED and OTHER)");
        StringBuilder sb2 = new StringBuilder();
        sb2.append("  %-");
        int maxAccount5 = maxAccount;
        sb2.append(maxAccount5);
        sb2.append("s  %-");
        int maxAuthority5 = maxAuthority;
        sb2.append(maxAuthority5);
        sb2.append("s %s\n");
        String format2 = sb2.toString();
        Map<String, Long> lastTimeMap2 = Maps.newHashMap();
        PackageManager pm2 = syncManager.mContext.getPackageManager();
        int i4 = 0;
        while (true) {
            N2 = N;
            if (i4 >= N2) {
                break;
            }
            ArrayList<SyncStorageEngine.SyncHistoryItem> items5 = items;
            SyncStorageEngine.SyncHistoryItem item2 = items5.get(i4);
            SyncStorageEngine.AuthorityInfo authorityInfo2 = syncManager.mSyncStorageEngine.getAuthority(item2.authorityId);
            if (authorityInfo2 != null) {
                authorityName2 = authorityInfo2.target.provider;
                StringBuilder sb3 = new StringBuilder();
                totalTimes = totalTimes2;
                sb3.append(authorityInfo2.target.account.name);
                sb3.append(SliceClientPermissions.SliceAuthority.DELIMITER);
                sb3.append(authorityInfo2.target.account.type);
                sb3.append(" u");
                sb3.append(authorityInfo2.target.userId);
                accountKey = sb3.toString();
            } else {
                totalTimes = totalTimes2;
                authorityName2 = "Unknown";
                accountKey = "Unknown";
            }
            int maxAuthority6 = maxAuthority5;
            int maxAccount6 = maxAccount5;
            long elapsedTime5 = item2.elapsedTime;
            Time time = new Time();
            long eventTime = item2.eventTime;
            time.set(eventTime);
            Time time2 = time;
            StringBuilder sb4 = new StringBuilder();
            sb4.append(authorityName2);
            SyncStorageEngine.AuthorityInfo authorityInfo3 = authorityInfo2;
            sb4.append(SliceClientPermissions.SliceAuthority.DELIMITER);
            sb4.append(accountKey);
            String key = sb4.toString();
            Long lastEventTime = lastTimeMap2.get(key);
            if (lastEventTime == null) {
                diffString = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                format = format2;
                N4 = N2;
                items3 = items5;
                String str7 = accountKey;
            } else {
                items3 = items5;
                String str8 = accountKey;
                long diff = (lastEventTime.longValue() - eventTime) / 1000;
                if (diff < 60) {
                    diffString = String.valueOf(diff);
                    format = format2;
                    N4 = N2;
                } else if (diff < 3600) {
                    Long l = lastEventTime;
                    format = format2;
                    N4 = N2;
                    elapsedTime = elapsedTime5;
                    diffString = String.format("%02d:%02d", new Object[]{Long.valueOf(diff / 60), Long.valueOf(diff % 60)});
                    authorityName3 = authorityName2;
                    lastTimeMap2.put(key, Long.valueOf(eventTime));
                    PrintWriter printWriter5 = pw;
                    printWriter5.printf("  #%-3d: %s %8s  %5.1fs  %8s", new Object[]{Integer.valueOf(i4 + 1), formatTime(eventTime), SyncStorageEngine.SOURCES[item2.source], Float.valueOf(((float) elapsedTime) / 1000.0f), diffString});
                    String format3 = format;
                    printWriter5.printf(format3, new Object[]{"XXXXXXXXX", authorityName3, SyncOperation.reasonToString(pm2, item2.reason)});
                    if (item2.event == 1) {
                        if (item2.upstreamActivity == 0 && item2.downstreamActivity == 0) {
                            String str9 = key;
                            if (item2.mesg != null && !SyncStorageEngine.MESG_SUCCESS.equals(item2.mesg)) {
                                printWriter5.printf("    mesg=%s\n", new Object[]{item2.mesg});
                            }
                            i4++;
                            format2 = format3;
                            printWriter = printWriter5;
                            totalTimes2 = totalTimes;
                            maxAccount5 = maxAccount6;
                            maxAuthority5 = maxAuthority6;
                            items = items3;
                            N = N4;
                            syncManager = this;
                        }
                    }
                    String str10 = key;
                    printWriter5.printf("    event=%d upstreamActivity=%d downstreamActivity=%d\n", new Object[]{Integer.valueOf(item2.event), Long.valueOf(item2.upstreamActivity), Long.valueOf(item2.downstreamActivity)});
                    printWriter5.printf("    mesg=%s\n", new Object[]{item2.mesg});
                    i4++;
                    format2 = format3;
                    printWriter = printWriter5;
                    totalTimes2 = totalTimes;
                    maxAccount5 = maxAccount6;
                    maxAuthority5 = maxAuthority6;
                    items = items3;
                    N = N4;
                    syncManager = this;
                } else {
                    format = format2;
                    N4 = N2;
                    elapsedTime = elapsedTime5;
                    Long l2 = lastEventTime;
                    long sec = diff % 3600;
                    authorityName3 = authorityName2;
                    diffString = String.format("%02d:%02d:%02d", new Object[]{Long.valueOf(diff / 3600), Long.valueOf(sec / 60), Long.valueOf(sec % 60)});
                    lastTimeMap2.put(key, Long.valueOf(eventTime));
                    PrintWriter printWriter52 = pw;
                    printWriter52.printf("  #%-3d: %s %8s  %5.1fs  %8s", new Object[]{Integer.valueOf(i4 + 1), formatTime(eventTime), SyncStorageEngine.SOURCES[item2.source], Float.valueOf(((float) elapsedTime) / 1000.0f), diffString});
                    String format32 = format;
                    printWriter52.printf(format32, new Object[]{"XXXXXXXXX", authorityName3, SyncOperation.reasonToString(pm2, item2.reason)});
                    if (item2.event == 1) {
                    }
                    String str102 = key;
                    printWriter52.printf("    event=%d upstreamActivity=%d downstreamActivity=%d\n", new Object[]{Integer.valueOf(item2.event), Long.valueOf(item2.upstreamActivity), Long.valueOf(item2.downstreamActivity)});
                    printWriter52.printf("    mesg=%s\n", new Object[]{item2.mesg});
                    i4++;
                    format2 = format32;
                    printWriter = printWriter52;
                    totalTimes2 = totalTimes;
                    maxAccount5 = maxAccount6;
                    maxAuthority5 = maxAuthority6;
                    items = items3;
                    N = N4;
                    syncManager = this;
                }
            }
            elapsedTime = elapsedTime5;
            authorityName3 = authorityName2;
            lastTimeMap2.put(key, Long.valueOf(eventTime));
            PrintWriter printWriter522 = pw;
            printWriter522.printf("  #%-3d: %s %8s  %5.1fs  %8s", new Object[]{Integer.valueOf(i4 + 1), formatTime(eventTime), SyncStorageEngine.SOURCES[item2.source], Float.valueOf(((float) elapsedTime) / 1000.0f), diffString});
            String format322 = format;
            printWriter522.printf(format322, new Object[]{"XXXXXXXXX", authorityName3, SyncOperation.reasonToString(pm2, item2.reason)});
            if (item2.event == 1) {
            }
            String str1022 = key;
            printWriter522.printf("    event=%d upstreamActivity=%d downstreamActivity=%d\n", new Object[]{Integer.valueOf(item2.event), Long.valueOf(item2.upstreamActivity), Long.valueOf(item2.downstreamActivity)});
            printWriter522.printf("    mesg=%s\n", new Object[]{item2.mesg});
            i4++;
            format2 = format322;
            printWriter = printWriter522;
            totalTimes2 = totalTimes;
            maxAccount5 = maxAccount6;
            maxAuthority5 = maxAuthority6;
            items = items3;
            N = N4;
            syncManager = this;
        }
        int N7 = N2;
        long j4 = totalTimes2;
        PrintWriter printWriter6 = printWriter;
        int i5 = maxAuthority5;
        int i6 = maxAccount5;
        ArrayList<SyncStorageEngine.SyncHistoryItem> items6 = items;
        String format4 = format2;
        pw.println();
        printWriter6.println("Recent Sync History Extras");
        printWriter6.println("(SERVER is now split up to FEED and OTHER)");
        int i7 = 0;
        while (true) {
            int N8 = N7;
            if (i7 < N8) {
                ArrayList<SyncStorageEngine.SyncHistoryItem> items7 = items6;
                SyncStorageEngine.SyncHistoryItem item3 = items7.get(i7);
                Bundle extras = item3.extras;
                if (extras == null) {
                    lastTimeMap = lastTimeMap2;
                    pm = pm2;
                    N3 = N8;
                    items2 = items7;
                } else if (extras.size() == 0) {
                    lastTimeMap = lastTimeMap2;
                    pm = pm2;
                    N3 = N8;
                    items2 = items7;
                } else {
                    SyncStorageEngine.AuthorityInfo authorityInfo4 = this.mSyncStorageEngine.getAuthority(item3.authorityId);
                    if (authorityInfo4 != null) {
                        authorityName = authorityInfo4.target.provider;
                        String str11 = authorityInfo4.target.account.name + SliceClientPermissions.SliceAuthority.DELIMITER + authorityInfo4.target.account.type + " u" + authorityInfo4.target.userId;
                    } else {
                        authorityName = "Unknown";
                    }
                    Time time3 = new Time();
                    long eventTime2 = item3.eventTime;
                    time3.set(eventTime2);
                    lastTimeMap = lastTimeMap2;
                    pm = pm2;
                    N3 = N8;
                    items2 = items7;
                    printWriter6.printf("  #%-3d: %s %8s ", new Object[]{Integer.valueOf(i7 + 1), formatTime(eventTime2), SyncStorageEngine.SOURCES[item3.source]});
                    printWriter6.printf(format4, new Object[]{"XXXXXXXXX", authorityName, extras});
                }
                i7++;
                lastTimeMap2 = lastTimeMap;
                pm2 = pm;
                N7 = N3;
                items6 = items2;
            } else {
                return;
            }
        }
    }

    private void dumpDayStatistics(PrintWriter pw) {
        SyncStorageEngine.DayStats[] dses = this.mSyncStorageEngine.getDayStatistics();
        if (dses != null && dses[0] != null) {
            pw.println();
            pw.println("Sync Statistics");
            pw.print("  Today:  ");
            dumpDayStatistic(pw, dses[0]);
            int today = dses[0].day;
            int i = 1;
            while (i <= 6 && i < dses.length) {
                SyncStorageEngine.DayStats ds = dses[i];
                if (ds == null) {
                    break;
                }
                int delta = today - ds.day;
                if (delta > 6) {
                    break;
                }
                pw.print("  Day-");
                pw.print(delta);
                pw.print(":  ");
                dumpDayStatistic(pw, ds);
                i++;
            }
            int i2 = i;
            int weekDay = today;
            while (i2 < dses.length) {
                SyncStorageEngine.DayStats aggr = null;
                weekDay -= 7;
                while (true) {
                    if (i2 >= dses.length) {
                        break;
                    }
                    SyncStorageEngine.DayStats ds2 = dses[i2];
                    if (ds2 == null) {
                        i2 = dses.length;
                        break;
                    } else if (weekDay - ds2.day > 6) {
                        break;
                    } else {
                        i2++;
                        if (aggr == null) {
                            aggr = new SyncStorageEngine.DayStats(weekDay);
                        }
                        aggr.successCount += ds2.successCount;
                        aggr.successTime += ds2.successTime;
                        aggr.failureCount += ds2.failureCount;
                        aggr.failureTime += ds2.failureTime;
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
                for (RegisteredServicesCache.ServiceInfo<?> info : this.mSyncAdapters.getAllServices(user.id)) {
                    pw.println(info);
                }
                pw.decreaseIndent();
                pw.println();
            }
        }
    }

    static void sendOnUnsyncableAccount(Context context, RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo, int userId, OnReadyCallback onReadyCallback) {
        OnUnsyncableAccountCheck connection = new OnUnsyncableAccountCheck(syncAdapterInfo, onReadyCallback);
        if (context.bindServiceAsUser(getAdapterBindIntent(context, syncAdapterInfo.componentName, userId), connection, 21, UserHandle.of(userId))) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable(context, connection) {
                private final /* synthetic */ Context f$0;
                private final /* synthetic */ SyncManager.OnUnsyncableAccountCheck f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void run() {
                    this.f$0.unbindService(this.f$1);
                }
            }, 5000);
        } else {
            connection.onReady();
        }
    }

    public static boolean readyToSync() {
        boolean z;
        synchronized (SyncManager.class) {
            z = sInstance != null && sInstance.mProvisioned && sInstance.mBootCompleted && sInstance.mJobServiceReady;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public boolean isSyncStillActiveH(ActiveSyncContext activeSyncContext) {
        Iterator<ActiveSyncContext> it = this.mActiveSyncContexts.iterator();
        while (it.hasNext()) {
            if (it.next() == activeSyncContext) {
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
            if ((includeSyncSettings || !isSyncSetting(key)) && (!smaller.containsKey(key) || !Objects.equals(bigger.get(key), smaller.get(key)))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSyncSetting(String key) {
        if (!key.equals("expedited") && !key.equals("ignore_settings") && !key.equals("ignore_backoff") && !key.equals("do_not_retry") && !key.equals("force") && !key.equals("upload") && !key.equals("deletions_override") && !key.equals("discard_deletions") && !key.equals("expected_upload") && !key.equals("expected_download") && !key.equals("sync_priority") && !key.equals("allow_metered") && !key.equals("initialize")) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public Context getContextForUser(UserHandle user) {
        try {
            return this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, user);
        } catch (PackageManager.NameNotFoundException e) {
            return this.mContext;
        }
    }

    /* access modifiers changed from: private */
    public void cancelJob(SyncOperation op, String why) {
        if (op == null) {
            Slog.wtf("SyncManager", "Null sync operation detected.");
            return;
        }
        if (op.isPeriodic) {
            this.mLogger.log("Removing periodic sync ", op, " for ", why);
        }
        getJobScheduler().cancel(op.jobId);
    }

    private void wtfWithLog(String message) {
        Slog.wtf("SyncManager", message);
        this.mLogger.log("WTF: ", message);
    }

    public void resetTodayStats() {
        this.mSyncStorageEngine.resetTodayStats(true);
    }
}
