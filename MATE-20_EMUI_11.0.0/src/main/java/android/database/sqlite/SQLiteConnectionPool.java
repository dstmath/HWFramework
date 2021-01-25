package android.database.sqlite;

import android.database.sqlite.SQLiteDebug;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.OperationCanceledException;
import android.os.SystemClock;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.PrefixPrinter;
import android.util.Printer;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public final class SQLiteConnectionPool implements Closeable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int CONNECTION_FLAG_EXCLUSIVE = 8;
    public static final int CONNECTION_FLAG_INTERACTIVE = 4;
    public static final int CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY = 2;
    public static final int CONNECTION_FLAG_READ_ONLY = 1;
    private static final long CONNECTION_POOL_BUSY_MILLIS = 30000;
    private static final String TAG = "SQLiteConnectionPool";
    private final String GALLERY_PROVIDER_DB_PATH = "/data/user/0/com.android.gallery3d/databases/gallery.db";
    private final String HW_PHOTOS_PROVIDER_DB_PATH = "/data/user/0/com.huawei.photos/databases/gallery.db";
    private final String MEDIA_PROVIDER_DB_PATH = "/data/user/0/com.android.providers.media/databases/external.db";
    private final WeakHashMap<SQLiteConnection, AcquiredConnectionStatus> mAcquiredConnections = new WeakHashMap<>();
    private SQLiteConnection mAvailableExclusiveConnection;
    private final ArrayList<SQLiteConnection> mAvailableNonPrimaryConnections = new ArrayList<>();
    private SQLiteConnection mAvailablePrimaryConnection;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final SQLiteDatabaseConfiguration mConfiguration;
    private final AtomicBoolean mConnectionLeaked = new AtomicBoolean();
    private ConnectionWaiter mConnectionWaiterPool;
    private ConnectionWaiter mConnectionWaiterQueue;
    @GuardedBy({"mLock"})
    private IdleConnectionHandler mIdleConnectionHandler;
    private boolean mIsExclusiveConnectionEnable;
    private boolean mIsOpen;
    private final Object mLock = new Object();
    private int mMaxConnectionPoolSize;
    private int mNextConnectionId;
    private final AtomicLong mTotalExecutionTimeCounter = new AtomicLong(0);

    /* access modifiers changed from: package-private */
    public enum AcquiredConnectionStatus {
        NORMAL,
        RECONFIGURE,
        DISCARD
    }

    private SQLiteConnectionPool(SQLiteDatabaseConfiguration configuration) {
        this.mConfiguration = new SQLiteDatabaseConfiguration(configuration);
        setMaxConnectionPoolSizeLocked();
        if (this.mConfiguration.idleConnectionTimeoutMs != Long.MAX_VALUE) {
            setupIdleConnectionHandler(Looper.getMainLooper(), this.mConfiguration.idleConnectionTimeoutMs);
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            dispose(true);
        } finally {
            super.finalize();
        }
    }

    public static SQLiteConnectionPool open(SQLiteDatabaseConfiguration configuration) {
        return open(configuration, false);
    }

    public static SQLiteConnectionPool open(SQLiteDatabaseConfiguration configuration, boolean isExclusiveConnection) {
        if (configuration != null) {
            SQLiteConnectionPool pool = new SQLiteConnectionPool(configuration);
            pool.open();
            pool.setExclusiveConnectionEnabled(isExclusiveConnection);
            return pool;
        }
        throw new IllegalArgumentException("configuration must not be null.");
    }

    private void open() {
        this.mAvailablePrimaryConnection = openConnectionLocked(this.mConfiguration, true);
        synchronized (this.mLock) {
            if (this.mIdleConnectionHandler != null) {
                this.mIdleConnectionHandler.connectionReleased(this.mAvailablePrimaryConnection);
            }
        }
        this.mIsOpen = true;
        this.mCloseGuard.open("close");
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        dispose(false);
    }

    private void dispose(boolean finalized) {
        CloseGuard closeGuard = this.mCloseGuard;
        if (closeGuard != null) {
            if (finalized) {
                closeGuard.warnIfOpen();
            }
            this.mCloseGuard.close();
        }
        if (!finalized) {
            synchronized (this.mLock) {
                throwIfClosedLocked();
                this.mIsOpen = false;
                closeAvailableConnectionsAndLogExceptionsLocked();
                int pendingCount = this.mAcquiredConnections.size();
                if (pendingCount != 0) {
                    Log.i(TAG, "The connection pool for " + this.mConfiguration.label + " has been closed but there are still " + pendingCount + " connections in use.  They will be closed as they are released back to the pool.");
                }
                wakeConnectionWaitersLocked();
            }
        }
    }

    public void reconfigure(SQLiteDatabaseConfiguration configuration) {
        boolean z;
        if (configuration != null) {
            synchronized (this.mLock) {
                throwIfClosedLocked();
                boolean onlyCompatWalChanged = false;
                boolean walModeChanged = ((configuration.openFlags ^ this.mConfiguration.openFlags) & 536870912) != 0;
                boolean isConfigEnhanceChanged = false;
                if (configuration.configurationEnhancement) {
                    if (!walModeChanged && configuration.defaultWALEnabled == this.mConfiguration.defaultWALEnabled) {
                        if (configuration.explicitWALEnabled == this.mConfiguration.explicitWALEnabled) {
                            z = false;
                            walModeChanged = z;
                        }
                    }
                    z = true;
                    walModeChanged = z;
                }
                if (walModeChanged) {
                    if (this.mAcquiredConnections.isEmpty()) {
                        if (this.mAvailableExclusiveConnection != null) {
                            closeConnectionAndLogExceptionsLocked(this.mAvailableExclusiveConnection);
                            this.mAvailableExclusiveConnection = null;
                        }
                        closeAvailableNonPrimaryConnectionsAndLogExceptionsLocked();
                    } else {
                        throw new IllegalStateException("Write Ahead Logging (WAL) mode cannot be enabled or disabled while there are transactions in progress. Finish all transactions and release all active database connections first.");
                    }
                }
                if (configuration.foreignKeyConstraintsEnabled != this.mConfiguration.foreignKeyConstraintsEnabled) {
                    if (!this.mAcquiredConnections.isEmpty()) {
                        throw new IllegalStateException("Foreign Key Constraints cannot be enabled or disabled while there are transactions in progress.  Finish all transactions and release all active database connections first.");
                    }
                }
                if ((this.mConfiguration.openFlags ^ configuration.openFlags) == Integer.MIN_VALUE) {
                    onlyCompatWalChanged = true;
                }
                if (this.mConfiguration.configurationEnhancement) {
                    isConfigEnhanceChanged = walModeChanged;
                }
                if ((onlyCompatWalChanged || this.mConfiguration.openFlags == configuration.openFlags) && !isConfigEnhanceChanged) {
                    this.mConfiguration.updateParametersFrom(configuration);
                    setMaxConnectionPoolSizeLocked();
                    closeExcessConnectionsAndLogExceptionsLocked();
                    reconfigureAllConnectionsLocked();
                } else {
                    if (walModeChanged) {
                        closeAvailableConnectionsAndLogExceptionsLocked();
                    }
                    SQLiteConnection newPrimaryConnection = openConnectionLocked(configuration, true);
                    closeAvailableConnectionsAndLogExceptionsLocked();
                    discardAcquiredConnectionsLocked();
                    this.mAvailablePrimaryConnection = newPrimaryConnection;
                    this.mConfiguration.updateParametersFrom(configuration);
                    setMaxConnectionPoolSizeLocked();
                }
                wakeConnectionWaitersLocked();
            }
            return;
        }
        throw new IllegalArgumentException("configuration must not be null.");
    }

    public SQLiteConnection acquireConnection(String sql, int connectionFlags, CancellationSignal cancellationSignal) {
        SQLiteConnection con = waitForConnection(sql, connectionFlags, cancellationSignal);
        synchronized (this.mLock) {
            if (this.mIdleConnectionHandler != null) {
                this.mIdleConnectionHandler.connectionAcquired(con);
            }
        }
        return con;
    }

    public void releaseConnection(SQLiteConnection connection) {
        synchronized (this.mLock) {
            if (this.mIdleConnectionHandler != null) {
                this.mIdleConnectionHandler.connectionReleased(connection);
            }
            AcquiredConnectionStatus status = this.mAcquiredConnections.remove(connection);
            if (status == null) {
                throw new IllegalStateException("Cannot perform this operation because the specified connection was not acquired from this pool or has already been released.");
            } else if (!this.mIsOpen) {
                closeConnectionAndLogExceptionsLocked(connection);
            } else if (connection.isPrimaryConnection()) {
                if (recycleConnectionLocked(connection, status)) {
                    this.mAvailablePrimaryConnection = connection;
                }
                wakeConnectionWaitersLocked();
            } else if (connection.isExclusiveConnection()) {
                if (recycleConnectionLocked(connection, status)) {
                    this.mAvailableExclusiveConnection = connection;
                }
                wakeConnectionWaitersLocked();
            } else if (this.mAvailableNonPrimaryConnections.size() >= getMaxNonPrimaryConnectionSizeLocked()) {
                closeConnectionAndLogExceptionsLocked(connection);
            } else {
                if (recycleConnectionLocked(connection, status)) {
                    this.mAvailableNonPrimaryConnections.add(connection);
                }
                wakeConnectionWaitersLocked();
            }
        }
    }

    @GuardedBy({"mLock"})
    private boolean recycleConnectionLocked(SQLiteConnection connection, AcquiredConnectionStatus status) {
        if (status == AcquiredConnectionStatus.RECONFIGURE) {
            try {
                connection.reconfigure(this.mConfiguration);
            } catch (RuntimeException ex) {
                Log.e(TAG, "Failed to reconfigure released connection, closing it: " + connection, ex);
                status = AcquiredConnectionStatus.DISCARD;
            }
        }
        if (status != AcquiredConnectionStatus.DISCARD) {
            return true;
        }
        closeConnectionAndLogExceptionsLocked(connection);
        return false;
    }

    public boolean shouldYieldConnection(SQLiteConnection connection, int connectionFlags) {
        synchronized (this.mLock) {
            if (!this.mAcquiredConnections.containsKey(connection)) {
                throw new IllegalStateException("Cannot perform this operation because the specified connection was not acquired from this pool or has already been released.");
            } else if (!this.mIsOpen) {
                return false;
            } else {
                return isSessionBlockingImportantConnectionWaitersLocked(connection.isPrimaryConnection(), connectionFlags);
            }
        }
    }

    public void collectDbStats(ArrayList<SQLiteDebug.DbStats> dbStatsList) {
        synchronized (this.mLock) {
            if (this.mAvailablePrimaryConnection != null) {
                this.mAvailablePrimaryConnection.collectDbStats(dbStatsList);
            }
            Iterator<SQLiteConnection> it = this.mAvailableNonPrimaryConnections.iterator();
            while (it.hasNext()) {
                it.next().collectDbStats(dbStatsList);
            }
            if (this.mAvailableExclusiveConnection != null) {
                this.mAvailableExclusiveConnection.collectDbStats(dbStatsList);
            }
            for (SQLiteConnection connection : this.mAcquiredConnections.keySet()) {
                connection.collectDbStatsUnsafe(dbStatsList);
            }
        }
    }

    private SQLiteConnection openConnectionLocked(SQLiteDatabaseConfiguration configuration, boolean primaryConnection) {
        int connectionId = this.mNextConnectionId;
        this.mNextConnectionId = connectionId + 1;
        return SQLiteConnection.open(this, configuration, connectionId, primaryConnection);
    }

    /* access modifiers changed from: package-private */
    public void onConnectionLeaked() {
        Log.w(TAG, "A SQLiteConnection object for database '" + this.mConfiguration.label + "' was leaked!  Please fix your application to end transactions in progress properly and to close the database when it is no longer needed.");
        this.mConnectionLeaked.set(true);
    }

    /* access modifiers changed from: package-private */
    public void onStatementExecuted(long executionTimeMs) {
        this.mTotalExecutionTimeCounter.addAndGet(executionTimeMs);
    }

    @GuardedBy({"mLock"})
    private void closeAvailableConnectionsAndLogExceptionsLocked() {
        closeAvailableNonPrimaryConnectionsAndLogExceptionsLocked();
        SQLiteConnection sQLiteConnection = this.mAvailablePrimaryConnection;
        if (sQLiteConnection != null) {
            closeConnectionAndLogExceptionsLocked(sQLiteConnection);
            this.mAvailablePrimaryConnection = null;
        }
        SQLiteConnection sQLiteConnection2 = this.mAvailableExclusiveConnection;
        if (sQLiteConnection2 != null) {
            closeConnectionAndLogExceptionsLocked(sQLiteConnection2);
            this.mAvailableExclusiveConnection = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private boolean closeAvailableConnectionLocked(int connectionId) {
        for (int i = this.mAvailableNonPrimaryConnections.size() - 1; i >= 0; i--) {
            SQLiteConnection c = this.mAvailableNonPrimaryConnections.get(i);
            if (c.getConnectionId() == connectionId) {
                closeConnectionAndLogExceptionsLocked(c);
                this.mAvailableNonPrimaryConnections.remove(i);
                return true;
            }
        }
        SQLiteConnection sQLiteConnection = this.mAvailablePrimaryConnection;
        if (sQLiteConnection == null || sQLiteConnection.getConnectionId() != connectionId) {
            return false;
        }
        closeConnectionAndLogExceptionsLocked(this.mAvailablePrimaryConnection);
        this.mAvailablePrimaryConnection = null;
        return true;
    }

    @GuardedBy({"mLock"})
    private void closeAvailableNonPrimaryConnectionsAndLogExceptionsLocked() {
        int count = this.mAvailableNonPrimaryConnections.size();
        for (int i = 0; i < count; i++) {
            closeConnectionAndLogExceptionsLocked(this.mAvailableNonPrimaryConnections.get(i));
        }
        this.mAvailableNonPrimaryConnections.clear();
    }

    /* access modifiers changed from: package-private */
    public void closeAvailableNonPrimaryConnectionsAndLogExceptions() {
        synchronized (this.mLock) {
            closeAvailableNonPrimaryConnectionsAndLogExceptionsLocked();
        }
    }

    @GuardedBy({"mLock"})
    private void closeExcessConnectionsAndLogExceptionsLocked() {
        int availableCount = this.mAvailableNonPrimaryConnections.size();
        while (true) {
            int availableCount2 = availableCount - 1;
            if (availableCount > getMaxNonPrimaryConnectionSizeLocked()) {
                closeConnectionAndLogExceptionsLocked(this.mAvailableNonPrimaryConnections.remove(availableCount2));
                availableCount = availableCount2;
            } else {
                return;
            }
        }
    }

    @GuardedBy({"mLock"})
    private void closeConnectionAndLogExceptionsLocked(SQLiteConnection connection) {
        try {
            connection.close();
            if (this.mIdleConnectionHandler != null) {
                this.mIdleConnectionHandler.connectionClosed(connection);
            }
        } catch (RuntimeException ex) {
            Log.e(TAG, "Failed to close connection, its fate is now in the hands of the merciful GC: " + connection, ex);
        }
    }

    private void discardAcquiredConnectionsLocked() {
        markAcquiredConnectionsLocked(AcquiredConnectionStatus.DISCARD);
    }

    @GuardedBy({"mLock"})
    private void reconfigureAllConnectionsLocked() {
        SQLiteConnection sQLiteConnection = this.mAvailablePrimaryConnection;
        if (sQLiteConnection != null) {
            try {
                sQLiteConnection.reconfigure(this.mConfiguration);
            } catch (RuntimeException ex) {
                Log.e(TAG, "Failed to reconfigure available primary connection, closing it: " + this.mAvailablePrimaryConnection, ex);
                closeConnectionAndLogExceptionsLocked(this.mAvailablePrimaryConnection);
                this.mAvailablePrimaryConnection = null;
            }
        }
        SQLiteConnection sQLiteConnection2 = this.mAvailableExclusiveConnection;
        if (sQLiteConnection2 != null) {
            try {
                sQLiteConnection2.reconfigure(this.mConfiguration);
            } catch (RuntimeException ex2) {
                Log.e(TAG, "Failed to reconfigure available exclusive connection, closing it: " + this.mAvailableExclusiveConnection, ex2);
                closeConnectionAndLogExceptionsLocked(this.mAvailableExclusiveConnection);
                this.mAvailableExclusiveConnection = null;
            }
        }
        int count = this.mAvailableNonPrimaryConnections.size();
        int i = 0;
        while (i < count) {
            SQLiteConnection connection = this.mAvailableNonPrimaryConnections.get(i);
            try {
                connection.reconfigure(this.mConfiguration);
            } catch (RuntimeException ex3) {
                Log.e(TAG, "Failed to reconfigure available non-primary connection, closing it: " + connection, ex3);
                closeConnectionAndLogExceptionsLocked(connection);
                this.mAvailableNonPrimaryConnections.remove(i);
                count += -1;
                i--;
            }
            i++;
        }
        markAcquiredConnectionsLocked(AcquiredConnectionStatus.RECONFIGURE);
    }

    private void markAcquiredConnectionsLocked(AcquiredConnectionStatus status) {
        if (!this.mAcquiredConnections.isEmpty()) {
            ArrayList<SQLiteConnection> keysToUpdate = new ArrayList<>(this.mAcquiredConnections.size());
            for (Map.Entry<SQLiteConnection, AcquiredConnectionStatus> entry : this.mAcquiredConnections.entrySet()) {
                AcquiredConnectionStatus oldStatus = entry.getValue();
                if (!(status == oldStatus || oldStatus == AcquiredConnectionStatus.DISCARD)) {
                    keysToUpdate.add(entry.getKey());
                }
            }
            int updateCount = keysToUpdate.size();
            for (int i = 0; i < updateCount; i++) {
                this.mAcquiredConnections.put(keysToUpdate.get(i), status);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:106:0x011a  */
    private SQLiteConnection waitForConnection(String sql, int connectionFlags, CancellationSignal cancellationSignal) {
        Object obj;
        Throwable th;
        SQLiteConnection connection;
        Throwable th2;
        Throwable th3;
        SQLiteConnection connection2;
        RuntimeException ex;
        boolean wantPrimaryConnection;
        boolean z = false;
        boolean wantPrimaryConnection2 = (connectionFlags & 2) != 0;
        boolean wantExclusiveConnection = (connectionFlags & 8) != 0;
        Object obj2 = this.mLock;
        synchronized (obj2) {
            try {
                throwIfClosedLocked();
                if (cancellationSignal != null) {
                    try {
                        cancellationSignal.throwIfCanceled();
                    } catch (Throwable th4) {
                        th = th4;
                        obj = obj2;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th5) {
                                th = th5;
                            }
                        }
                        throw th;
                    }
                }
                connection = null;
                if (!wantPrimaryConnection2 && wantExclusiveConnection && isExclusiveConnectionEnabled()) {
                    connection = tryAcquireExclusiveConnectionLocked(connectionFlags);
                }
                if (connection == null && !wantPrimaryConnection2) {
                    connection = tryAcquireNonPrimaryConnectionLocked(sql, connectionFlags);
                }
                if (connection == null && (!isExclusiveConnectionEnabled() || (isExclusiveConnectionEnabled() && wantPrimaryConnection2))) {
                    connection = tryAcquirePrimaryConnectionLocked(connectionFlags);
                }
                if (connection == null) {
                    int priority = getPriority(connectionFlags);
                    obj = obj2;
                    try {
                        final ConnectionWaiter waiter = obtainConnectionWaiterLocked(Thread.currentThread(), SystemClock.uptimeMillis(), priority, wantPrimaryConnection2, wantExclusiveConnection, sql, connectionFlags);
                        ConnectionWaiter predecessor = null;
                        ConnectionWaiter successor = this.mConnectionWaiterQueue;
                        while (true) {
                            if (successor == null) {
                                break;
                            }
                            try {
                                if (priority > successor.mPriority) {
                                    waiter.mNext = successor;
                                    break;
                                }
                                predecessor = successor;
                                successor = successor.mNext;
                            } catch (Throwable th6) {
                                th = th6;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                        if (predecessor != null) {
                            predecessor.mNext = waiter;
                        } else {
                            this.mConnectionWaiterQueue = waiter;
                        }
                        final int nonce = waiter.mNonce;
                        if (cancellationSignal != null) {
                            cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
                                /* class android.database.sqlite.SQLiteConnectionPool.AnonymousClass1 */

                                @Override // android.os.CancellationSignal.OnCancelListener
                                public void onCancel() {
                                    synchronized (SQLiteConnectionPool.this.mLock) {
                                        if (waiter.mNonce == nonce) {
                                            SQLiteConnectionPool.this.cancelConnectionWaiterLocked(waiter);
                                        }
                                    }
                                }
                            });
                        }
                        long busyTimeoutMillis = 30000;
                        try {
                            long nextBusyTimeoutTime = waiter.mStartTime + 30000;
                            while (true) {
                                if (this.mConnectionLeaked.compareAndSet(true, z)) {
                                    try {
                                        synchronized (this.mLock) {
                                            wakeConnectionWaitersLocked();
                                        }
                                    } catch (Throwable th7) {
                                        th2 = th7;
                                        if (cancellationSignal != null) {
                                        }
                                        throw th2;
                                    }
                                }
                                LockSupport.parkNanos(this, busyTimeoutMillis * TimeUtils.NANOS_PER_MS);
                                Thread.interrupted();
                                synchronized (this.mLock) {
                                    try {
                                        throwIfClosedLocked();
                                        connection2 = waiter.mAssignedConnection;
                                        ex = waiter.mException;
                                        if (connection2 != null) {
                                            break;
                                        } else if (ex != null) {
                                            break;
                                        } else {
                                            long now = SystemClock.uptimeMillis();
                                            if (now < nextBusyTimeoutTime) {
                                                busyTimeoutMillis = now - nextBusyTimeoutTime;
                                                wantPrimaryConnection = wantPrimaryConnection2;
                                            } else {
                                                wantPrimaryConnection = wantPrimaryConnection2;
                                                try {
                                                    logConnectionPoolBusyLocked(now - waiter.mStartTime, connectionFlags);
                                                    busyTimeoutMillis = 30000;
                                                    nextBusyTimeoutTime = now + 30000;
                                                } catch (Throwable th8) {
                                                    th3 = th8;
                                                }
                                            }
                                        }
                                    } catch (Throwable th9) {
                                        th3 = th9;
                                    }
                                }
                                try {
                                    throw th3;
                                } catch (Throwable th10) {
                                    th2 = th10;
                                }
                                wantPrimaryConnection2 = wantPrimaryConnection;
                                z = false;
                            }
                            recycleConnectionWaiterLocked(waiter);
                            if (connection2 != null) {
                                if (cancellationSignal != null) {
                                    cancellationSignal.setOnCancelListener(null);
                                }
                                return connection2;
                            }
                            throw ex;
                        } catch (Throwable th11) {
                            th2 = th11;
                            if (cancellationSignal != null) {
                                cancellationSignal.setOnCancelListener(null);
                            }
                            throw th2;
                        }
                    } catch (Throwable th12) {
                        th = th12;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            } catch (Throwable th13) {
                th = th13;
                obj = obj2;
                while (true) {
                    break;
                }
                throw th;
            }
        }
        return connection;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void cancelConnectionWaiterLocked(ConnectionWaiter waiter) {
        if (waiter.mAssignedConnection == null && waiter.mException == null) {
            ConnectionWaiter predecessor = null;
            for (ConnectionWaiter current = this.mConnectionWaiterQueue; current != waiter; current = current.mNext) {
                predecessor = current;
            }
            if (predecessor != null) {
                predecessor.mNext = waiter.mNext;
            } else {
                this.mConnectionWaiterQueue = waiter.mNext;
            }
            waiter.mException = new OperationCanceledException();
            LockSupport.unpark(waiter.mThread);
            wakeConnectionWaitersLocked();
        }
    }

    private void logConnectionPoolBusyLocked(long waitMillis, int connectionFlags) {
        Thread thread = Thread.currentThread();
        StringBuilder msg = new StringBuilder();
        msg.append("The connection pool for database '");
        msg.append(this.mConfiguration.label);
        msg.append("' has been unable to grant a connection to thread ");
        msg.append(thread.getId());
        msg.append(" (");
        msg.append(thread.getName());
        msg.append(") ");
        msg.append("with flags 0x");
        msg.append(Integer.toHexString(connectionFlags));
        msg.append(" for ");
        msg.append(((float) waitMillis) * 0.001f);
        msg.append(" seconds.\n");
        ArrayList<String> requests = new ArrayList<>();
        int activeConnections = 0;
        int idleConnections = 0;
        if (!this.mAcquiredConnections.isEmpty()) {
            for (SQLiteConnection connection : this.mAcquiredConnections.keySet()) {
                String description = connection.describeCurrentOperationUnsafe();
                if (description != null) {
                    requests.add(description);
                    activeConnections++;
                } else {
                    idleConnections++;
                }
            }
        }
        int availableConnections = this.mAvailableNonPrimaryConnections.size();
        if (this.mAvailablePrimaryConnection != null) {
            availableConnections++;
        }
        if (this.mAvailableExclusiveConnection != null) {
            availableConnections++;
        }
        msg.append("Connections: ");
        msg.append(activeConnections);
        msg.append(" active, ");
        msg.append(idleConnections);
        msg.append(" idle, ");
        msg.append(availableConnections);
        msg.append(" available.\n");
        if (!requests.isEmpty()) {
            msg.append("\nRequests in progress:\n");
            Iterator<String> it = requests.iterator();
            while (it.hasNext()) {
                msg.append("  ");
                msg.append(it.next());
                msg.append("\n");
            }
        }
        Log.w(TAG, msg.toString());
    }

    /* JADX INFO: Multiple debug info for r6v1 android.database.sqlite.SQLiteConnectionPool$ConnectionWaiter: [D('ex' java.lang.RuntimeException), D('successor' android.database.sqlite.SQLiteConnectionPool$ConnectionWaiter)] */
    @GuardedBy({"mLock"})
    private void wakeConnectionWaitersLocked() {
        ConnectionWaiter predecessor = null;
        ConnectionWaiter waiter = this.mConnectionWaiterQueue;
        boolean primaryConnectionNotAvailable = false;
        boolean nonPrimaryConnectionNotAvailable = false;
        boolean exclusiveConnectionNotAvailable = false;
        while (waiter != null) {
            boolean unpark = false;
            if (!this.mIsOpen) {
                unpark = true;
            } else {
                SQLiteConnection connection = null;
                try {
                    if (!waiter.mWantPrimaryConnection && waiter.mWantExclusiveConnection && !exclusiveConnectionNotAvailable && (connection = tryAcquireExclusiveConnectionLocked(waiter.mConnectionFlags)) == null) {
                        exclusiveConnectionNotAvailable = true;
                    }
                    if (connection == null && !waiter.mWantPrimaryConnection && !nonPrimaryConnectionNotAvailable && (connection = tryAcquireNonPrimaryConnectionLocked(waiter.mSql, waiter.mConnectionFlags)) == null) {
                        nonPrimaryConnectionNotAvailable = true;
                    }
                    if (connection == null && ((!isExclusiveConnectionEnabled() || waiter.mWantPrimaryConnection) && !primaryConnectionNotAvailable && (connection = tryAcquirePrimaryConnectionLocked(waiter.mConnectionFlags)) == null)) {
                        primaryConnectionNotAvailable = true;
                    }
                    if (connection != null) {
                        waiter.mAssignedConnection = connection;
                        unpark = true;
                    } else if (nonPrimaryConnectionNotAvailable && primaryConnectionNotAvailable && exclusiveConnectionNotAvailable) {
                        return;
                    }
                } catch (RuntimeException ex) {
                    waiter.mException = ex;
                    unpark = true;
                }
            }
            ConnectionWaiter successor = waiter.mNext;
            if (unpark) {
                if (predecessor != null) {
                    predecessor.mNext = successor;
                } else {
                    this.mConnectionWaiterQueue = successor;
                }
                waiter.mNext = null;
                LockSupport.unpark(waiter.mThread);
            } else {
                predecessor = waiter;
            }
            waiter = successor;
        }
    }

    @GuardedBy({"mLock"})
    private SQLiteConnection tryAcquirePrimaryConnectionLocked(int connectionFlags) {
        SQLiteConnection connection = this.mAvailablePrimaryConnection;
        if (connection != null) {
            this.mAvailablePrimaryConnection = null;
            finishAcquireConnectionLocked(connection, connectionFlags);
            return connection;
        }
        for (SQLiteConnection acquiredConnection : this.mAcquiredConnections.keySet()) {
            if (acquiredConnection.isPrimaryConnection()) {
                return null;
            }
        }
        SQLiteConnection connection2 = openConnectionLocked(this.mConfiguration, true);
        finishAcquireConnectionLocked(connection2, connectionFlags);
        return connection2;
    }

    @GuardedBy({"mLock"})
    private SQLiteConnection tryAcquireNonPrimaryConnectionLocked(String sql, int connectionFlags) {
        int availableCount = this.mAvailableNonPrimaryConnections.size();
        if (availableCount > 1 && sql != null) {
            for (int i = 0; i < availableCount; i++) {
                SQLiteConnection connection = this.mAvailableNonPrimaryConnections.get(i);
                if (connection.isPreparedStatementInCache(sql)) {
                    this.mAvailableNonPrimaryConnections.remove(i);
                    finishAcquireConnectionLocked(connection, connectionFlags);
                    return connection;
                }
            }
        }
        if (availableCount > 0) {
            SQLiteConnection connection2 = this.mAvailableNonPrimaryConnections.remove(availableCount - 1);
            finishAcquireConnectionLocked(connection2, connectionFlags);
            return connection2;
        }
        int openConnections = this.mAcquiredConnections.size();
        if (this.mAvailablePrimaryConnection != null) {
            openConnections++;
        }
        if (isExclusiveConnectionEnabled()) {
            openConnections++;
        }
        if (openConnections >= this.mMaxConnectionPoolSize) {
            return null;
        }
        SQLiteConnection connection3 = openConnectionLocked(this.mConfiguration, false);
        finishAcquireConnectionLocked(connection3, connectionFlags);
        return connection3;
    }

    @GuardedBy({"mLock"})
    private void finishAcquireConnectionLocked(SQLiteConnection connection, int connectionFlags) {
        try {
            connection.setOnlyAllowReadOnlyOperations((connectionFlags & 1) != 0);
            this.mAcquiredConnections.put(connection, AcquiredConnectionStatus.NORMAL);
        } catch (RuntimeException ex) {
            Log.e(TAG, "Failed to prepare acquired connection for session, closing it: " + connection + ", connectionFlags=" + connectionFlags);
            closeConnectionAndLogExceptionsLocked(connection);
            throw ex;
        }
    }

    private boolean isSessionBlockingImportantConnectionWaitersLocked(boolean holdingPrimaryConnection, int connectionFlags) {
        ConnectionWaiter waiter = this.mConnectionWaiterQueue;
        if (waiter == null) {
            return false;
        }
        int priority = getPriority(connectionFlags);
        while (priority <= waiter.mPriority) {
            if (holdingPrimaryConnection || !waiter.mWantPrimaryConnection) {
                return true;
            }
            waiter = waiter.mNext;
            if (waiter == null) {
                return false;
            }
        }
        return false;
    }

    private static int getPriority(int connectionFlags) {
        if ((connectionFlags & 4) != 0) {
            return 2;
        }
        if ((connectionFlags & 8) == 0 || (connectionFlags & 2) == 0) {
            return 0;
        }
        return 1;
    }

    private void setMaxConnectionPoolSizeLocked() {
        if (this.mConfiguration.isInMemoryDb() || (this.mConfiguration.openFlags & 536870912) == 0) {
            this.mMaxConnectionPoolSize = 1;
            return;
        }
        int addedSize = getExtendConnectionCount(this.mConfiguration.path);
        if (!this.mConfiguration.configurationEnhancement) {
            this.mMaxConnectionPoolSize = SQLiteGlobal.getWALConnectionPoolSize();
            if (addedSize > 0) {
                this.mMaxConnectionPoolSize += addedSize;
            }
        } else if (this.mConfiguration.explicitWALEnabled) {
            this.mMaxConnectionPoolSize = SQLiteGlobal.getWALConnectionPoolSize();
            if (addedSize > 0) {
                this.mMaxConnectionPoolSize += addedSize;
            }
        } else {
            this.mMaxConnectionPoolSize = 1;
        }
    }

    @VisibleForTesting
    public void setupIdleConnectionHandler(Looper looper, long timeoutMs) {
        synchronized (this.mLock) {
            this.mIdleConnectionHandler = new IdleConnectionHandler(looper, timeoutMs);
        }
    }

    /* access modifiers changed from: package-private */
    public void disableIdleConnectionHandler() {
        synchronized (this.mLock) {
            this.mIdleConnectionHandler = null;
        }
    }

    private void throwIfClosedLocked() {
        if (!this.mIsOpen) {
            throw new IllegalStateException("Cannot perform this operation because the connection pool has been closed.");
        }
    }

    private ConnectionWaiter obtainConnectionWaiterLocked(Thread thread, long startTime, int priority, boolean wantPrimaryConnection, boolean wantExclusiveConnection, String sql, int connectionFlags) {
        ConnectionWaiter waiter = this.mConnectionWaiterPool;
        if (waiter != null) {
            this.mConnectionWaiterPool = waiter.mNext;
            waiter.mNext = null;
        } else {
            waiter = new ConnectionWaiter();
        }
        waiter.mThread = thread;
        waiter.mStartTime = startTime;
        waiter.mPriority = priority;
        waiter.mWantPrimaryConnection = wantPrimaryConnection;
        waiter.mSql = sql;
        waiter.mConnectionFlags = connectionFlags;
        waiter.mWantExclusiveConnection = wantExclusiveConnection;
        return waiter;
    }

    private void recycleConnectionWaiterLocked(ConnectionWaiter waiter) {
        waiter.mNext = this.mConnectionWaiterPool;
        waiter.mThread = null;
        waiter.mSql = null;
        waiter.mAssignedConnection = null;
        waiter.mException = null;
        waiter.mNonce++;
        this.mConnectionWaiterPool = waiter;
    }

    public void dump(Printer printer, boolean verbose, ArraySet<String> directories) {
        Printer indentedPrinter = PrefixPrinter.create(printer, "    ");
        synchronized (this.mLock) {
            if (directories != null) {
                directories.add(new File(this.mConfiguration.path).getParent());
            }
            boolean isCompatibilityWalEnabled = this.mConfiguration.isLegacyCompatibilityWalEnabled();
            printer.println("Connection pool for " + this.mConfiguration.path + SettingsStringUtil.DELIMITER);
            StringBuilder sb = new StringBuilder();
            sb.append("  Open: ");
            sb.append(this.mIsOpen);
            printer.println(sb.toString());
            printer.println("  Max connections: " + this.mMaxConnectionPoolSize);
            printer.println("  Total execution time: " + this.mTotalExecutionTimeCounter);
            printer.println("  Configuration: openFlags=" + this.mConfiguration.openFlags + ", isLegacyCompatibilityWalEnabled=" + isCompatibilityWalEnabled + ", journalMode=" + TextUtils.emptyIfNull(this.mConfiguration.journalMode) + ", syncMode=" + TextUtils.emptyIfNull(this.mConfiguration.syncMode));
            if (isCompatibilityWalEnabled) {
                printer.println("  Compatibility WAL enabled: wal_syncmode=" + SQLiteCompatibilityWalFlags.getWALSyncMode());
            }
            if (this.mConfiguration.isLookasideConfigSet()) {
                printer.println("  Lookaside config: sz=" + this.mConfiguration.lookasideSlotSize + " cnt=" + this.mConfiguration.lookasideSlotCount);
            }
            if (this.mConfiguration.idleConnectionTimeoutMs != Long.MAX_VALUE) {
                printer.println("  Idle connection timeout: " + this.mConfiguration.idleConnectionTimeoutMs);
            }
            printer.println("  Available primary connection:");
            printer.println("  configurationEnhancement:" + this.mConfiguration.configurationEnhancement);
            printer.println("  defaultWALEnabled:" + this.mConfiguration.defaultWALEnabled);
            printer.println("  explicitWALEnabled:" + this.mConfiguration.explicitWALEnabled);
            if (this.mAvailablePrimaryConnection != null) {
                this.mAvailablePrimaryConnection.dump(indentedPrinter, verbose);
            } else {
                indentedPrinter.println("<none>");
            }
            if (this.mAvailableExclusiveConnection != null) {
                printer.println(" Available Exclusive connection:");
                this.mAvailableExclusiveConnection.dump(indentedPrinter, verbose);
            }
            printer.println(" Available non-primary connections:");
            if (!this.mAvailableNonPrimaryConnections.isEmpty()) {
                int count = this.mAvailableNonPrimaryConnections.size();
                for (int i = 0; i < count; i++) {
                    this.mAvailableNonPrimaryConnections.get(i).dump(indentedPrinter, verbose);
                }
            } else {
                indentedPrinter.println("<none>");
            }
            printer.println("  Acquired connections:");
            if (!this.mAcquiredConnections.isEmpty()) {
                for (Map.Entry<SQLiteConnection, AcquiredConnectionStatus> entry : this.mAcquiredConnections.entrySet()) {
                    entry.getKey().dumpUnsafe(indentedPrinter, verbose);
                    indentedPrinter.println("  Status: " + entry.getValue());
                }
            } else {
                indentedPrinter.println("<none>");
            }
            printer.println("  Connection waiters:");
            if (this.mConnectionWaiterQueue != null) {
                int i2 = 0;
                long now = SystemClock.uptimeMillis();
                ConnectionWaiter waiter = this.mConnectionWaiterQueue;
                while (waiter != null) {
                    indentedPrinter.println(i2 + ": waited for " + (((float) (now - waiter.mStartTime)) * 0.001f) + " ms - thread=" + waiter.mThread + ", priority=" + waiter.mPriority + ", sql='" + waiter.mSql + "'");
                    waiter = waiter.mNext;
                    i2++;
                }
            } else {
                indentedPrinter.println("<none>");
            }
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return "SQLiteConnectionPool: " + this.mConfiguration.path;
    }

    public String getPath() {
        return this.mConfiguration.path;
    }

    /* access modifiers changed from: private */
    public static final class ConnectionWaiter {
        public SQLiteConnection mAssignedConnection;
        public int mConnectionFlags;
        public RuntimeException mException;
        public ConnectionWaiter mNext;
        public int mNonce;
        public int mPriority;
        public String mSql;
        public long mStartTime;
        public Thread mThread;
        public boolean mWantExclusiveConnection;
        public boolean mWantPrimaryConnection;

        private ConnectionWaiter() {
        }
    }

    private int getExtendConnectionCount(String databasePath) {
        if ("/data/user/0/com.android.providers.media/databases/external.db".equals(this.mConfiguration.path)) {
            return 2;
        }
        if ("/data/user/0/com.android.gallery3d/databases/gallery.db".equals(this.mConfiguration.path) || "/data/user/0/com.huawei.photos/databases/gallery.db".equals(this.mConfiguration.path)) {
            return 4;
        }
        return 0;
    }

    public void setExclusiveConnectionEnabled(boolean enabled) {
        this.mIsExclusiveConnectionEnable = false;
        if (this.mConfiguration.configurationEnhancement) {
            if ((this.mConfiguration.openFlags & 536870912) != 0 && this.mConfiguration.explicitWALEnabled) {
                this.mIsExclusiveConnectionEnable = enabled;
            }
        } else if ((this.mConfiguration.openFlags & 536870912) != 0) {
            this.mIsExclusiveConnectionEnable = enabled;
        }
    }

    private boolean isExclusiveConnectionEnabled() {
        return this.mConfiguration.configurationEnhancement ? (this.mConfiguration.openFlags & 536870912) != 0 && this.mConfiguration.explicitWALEnabled && this.mIsExclusiveConnectionEnable : (this.mConfiguration.openFlags & 536870912) != 0 && this.mIsExclusiveConnectionEnable;
    }

    private int getMaxNonPrimaryConnectionSizeLocked() {
        int maxCount = this.mMaxConnectionPoolSize - 1;
        return isExclusiveConnectionEnabled() ? maxCount - 1 : maxCount;
    }

    private SQLiteConnection tryAcquireExclusiveConnectionLocked(int connectionFlags) {
        SQLiteConnection connection = this.mAvailableExclusiveConnection;
        if (connection != null) {
            this.mAvailableExclusiveConnection = null;
            finishAcquireConnectionLocked(connection, connectionFlags);
            return connection;
        }
        for (SQLiteConnection acquiredConnection : this.mAcquiredConnections.keySet()) {
            if (acquiredConnection.isExclusiveConnection()) {
                return null;
            }
        }
        SQLiteConnection connection2 = openExclusiveConnectionLocked(this.mConfiguration);
        finishAcquireConnectionLocked(connection2, connectionFlags);
        return connection2;
    }

    private SQLiteConnection openExclusiveConnectionLocked(SQLiteDatabaseConfiguration configuration) {
        int connectionId = this.mNextConnectionId;
        this.mNextConnectionId = connectionId + 1;
        return SQLiteConnection.openExclusive(this, configuration, connectionId);
    }

    /* access modifiers changed from: private */
    public class IdleConnectionHandler extends Handler {
        private final long mTimeout;

        IdleConnectionHandler(Looper looper, long timeout) {
            super(looper);
            this.mTimeout = timeout;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            synchronized (SQLiteConnectionPool.this.mLock) {
                if (this == SQLiteConnectionPool.this.mIdleConnectionHandler) {
                    if (SQLiteConnectionPool.this.closeAvailableConnectionLocked(msg.what) && Log.isLoggable(SQLiteConnectionPool.TAG, 3)) {
                        Log.d(SQLiteConnectionPool.TAG, "Closed idle connection " + SQLiteConnectionPool.this.mConfiguration.label + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.what + " after " + this.mTimeout);
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void connectionReleased(SQLiteConnection con) {
            sendEmptyMessageDelayed(con.getConnectionId(), this.mTimeout);
        }

        /* access modifiers changed from: package-private */
        public void connectionAcquired(SQLiteConnection con) {
            removeMessages(con.getConnectionId());
        }

        /* access modifiers changed from: package-private */
        public void connectionClosed(SQLiteConnection con) {
            removeMessages(con.getConnectionId());
        }
    }
}
