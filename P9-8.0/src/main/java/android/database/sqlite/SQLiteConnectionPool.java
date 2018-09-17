package android.database.sqlite;

import android.database.sqlite.SQLiteDebug.DbStats;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import android.os.SystemClock;
import android.util.Log;
import android.util.PrefixPrinter;
import android.util.Printer;
import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public final class SQLiteConnectionPool implements Closeable {
    static final /* synthetic */ boolean -assertionsDisabled = (SQLiteConnectionPool.class.desiredAssertionStatus() ^ 1);
    public static final int CONNECTION_FLAG_EXCLUSIVE = 8;
    public static final int CONNECTION_FLAG_INTERACTIVE = 4;
    public static final int CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY = 2;
    public static final int CONNECTION_FLAG_READ_ONLY = 1;
    private static final long CONNECTION_POOL_BUSY_MILLIS = 30000;
    private static final String TAG = "SQLiteConnectionPool";
    private final WeakHashMap<SQLiteConnection, AcquiredConnectionStatus> mAcquiredConnections = new WeakHashMap();
    private SQLiteConnection mAvailableExclusiveConnection;
    private final ArrayList<SQLiteConnection> mAvailableNonPrimaryConnections = new ArrayList();
    private SQLiteConnection mAvailablePrimaryConnection;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final SQLiteDatabaseConfiguration mConfiguration;
    private final AtomicBoolean mConnectionLeaked = new AtomicBoolean();
    private ConnectionWaiter mConnectionWaiterPool;
    private ConnectionWaiter mConnectionWaiterQueue;
    private boolean mEnableExclusiveConnection;
    private boolean mIsOpen;
    private final Object mLock = new Object();
    private int mMaxConnectionPoolSize;
    private int mNextConnectionId;

    enum AcquiredConnectionStatus {
        NORMAL,
        RECONFIGURE,
        DISCARD
    }

    private static final class ConnectionWaiter {
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

        /* synthetic */ ConnectionWaiter(ConnectionWaiter -this0) {
            this();
        }

        private ConnectionWaiter() {
        }
    }

    private SQLiteConnectionPool(SQLiteDatabaseConfiguration configuration) {
        this.mConfiguration = new SQLiteDatabaseConfiguration(configuration);
        setMaxConnectionPoolSizeLocked();
    }

    protected void finalize() throws Throwable {
        try {
            dispose(true);
        } finally {
            super.finalize();
        }
    }

    public static SQLiteConnectionPool open(SQLiteDatabaseConfiguration configuration) {
        return open(configuration, false);
    }

    public static SQLiteConnectionPool open(SQLiteDatabaseConfiguration configuration, boolean enableExclusiveConnection) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null.");
        }
        SQLiteConnectionPool pool = new SQLiteConnectionPool(configuration);
        pool.open();
        pool.setExclusiveConnectionEnabled(enableExclusiveConnection);
        return pool;
    }

    private void open() {
        this.mAvailablePrimaryConnection = openConnectionLocked(this.mConfiguration, true);
        this.mIsOpen = true;
        this.mCloseGuard.open("close");
    }

    public void close() {
        dispose(false);
    }

    private void dispose(boolean finalized) {
        if (this.mCloseGuard != null) {
            if (finalized) {
                this.mCloseGuard.warnIfOpen();
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
                    Log.i(TAG, "The connection pool for " + this.mConfiguration.label + " has been closed but there are still " + pendingCount + " connections in use.  They will be closed " + "as they are released back to the pool.");
                }
                wakeConnectionWaitersLocked();
            }
        }
    }

    public void reconfigure(SQLiteDatabaseConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null.");
        }
        synchronized (this.mLock) {
            throwIfClosedLocked();
            boolean walModeChanged = ((configuration.openFlags ^ this.mConfiguration.openFlags) & 536870912) != 0;
            if (configuration.configurationEnhancement) {
                walModeChanged = (walModeChanged || configuration.defaultWALEnabled != this.mConfiguration.defaultWALEnabled) ? true : configuration.explicitWALEnabled != this.mConfiguration.explicitWALEnabled;
            }
            if (walModeChanged) {
                if (this.mAcquiredConnections.isEmpty()) {
                    if (this.mAvailableExclusiveConnection != null) {
                        closeConnectionAndLogExceptionsLocked(this.mAvailableExclusiveConnection);
                        this.mAvailableExclusiveConnection = null;
                    }
                    closeAvailableNonPrimaryConnectionsAndLogExceptionsLocked();
                    if (!(-assertionsDisabled || this.mAvailableNonPrimaryConnections.isEmpty())) {
                        throw new AssertionError();
                    }
                }
                throw new IllegalStateException("Write Ahead Logging (WAL) mode cannot be enabled or disabled while there are transactions in progress.  Finish all transactions and release all active database connections first.");
            }
            if (!(configuration.foreignKeyConstraintsEnabled != this.mConfiguration.foreignKeyConstraintsEnabled) || this.mAcquiredConnections.isEmpty()) {
                boolean configEnhanceChanged;
                if (this.mConfiguration.configurationEnhancement) {
                    configEnhanceChanged = walModeChanged;
                } else {
                    configEnhanceChanged = false;
                }
                if (this.mConfiguration.openFlags != configuration.openFlags || configEnhanceChanged) {
                    if (walModeChanged) {
                        closeAvailableConnectionsAndLogExceptionsLocked();
                    }
                    SQLiteConnection newPrimaryConnection = openConnectionLocked(configuration, true);
                    closeAvailableConnectionsAndLogExceptionsLocked();
                    discardAcquiredConnectionsLocked();
                    this.mAvailablePrimaryConnection = newPrimaryConnection;
                    this.mConfiguration.updateParametersFrom(configuration);
                    setMaxConnectionPoolSizeLocked();
                } else {
                    this.mConfiguration.updateParametersFrom(configuration);
                    setMaxConnectionPoolSizeLocked();
                    closeExcessConnectionsAndLogExceptionsLocked();
                    reconfigureAllConnectionsLocked();
                }
                wakeConnectionWaitersLocked();
            } else {
                throw new IllegalStateException("Foreign Key Constraints cannot be enabled or disabled while there are transactions in progress.  Finish all transactions and release all active database connections first.");
            }
        }
    }

    public SQLiteConnection acquireConnection(String sql, int connectionFlags, CancellationSignal cancellationSignal) {
        return waitForConnection(sql, connectionFlags, cancellationSignal);
    }

    public void releaseConnection(SQLiteConnection connection) {
        synchronized (this.mLock) {
            AcquiredConnectionStatus status = (AcquiredConnectionStatus) this.mAcquiredConnections.remove(connection);
            if (status == null) {
                throw new IllegalStateException("Cannot perform this operation because the specified connection was not acquired from this pool or has already been released.");
            }
            if (!this.mIsOpen) {
                closeConnectionAndLogExceptionsLocked(connection);
            } else if (connection.isPrimaryConnection()) {
                if (recycleConnectionLocked(connection, status)) {
                    if (-assertionsDisabled || this.mAvailablePrimaryConnection == null) {
                        this.mAvailablePrimaryConnection = connection;
                    } else {
                        throw new AssertionError();
                    }
                }
                wakeConnectionWaitersLocked();
            } else if (connection.isExclusiveConnection()) {
                if (recycleConnectionLocked(connection, status)) {
                    if (-assertionsDisabled || this.mAvailableExclusiveConnection == null) {
                        this.mAvailableExclusiveConnection = connection;
                    } else {
                        throw new AssertionError();
                    }
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
            } else if (this.mIsOpen) {
                boolean isSessionBlockingImportantConnectionWaitersLocked = isSessionBlockingImportantConnectionWaitersLocked(connection.isPrimaryConnection(), connectionFlags);
                return isSessionBlockingImportantConnectionWaitersLocked;
            } else {
                return false;
            }
        }
    }

    public void collectDbStats(ArrayList<DbStats> dbStatsList) {
        synchronized (this.mLock) {
            if (this.mAvailablePrimaryConnection != null) {
                this.mAvailablePrimaryConnection.collectDbStats(dbStatsList);
            }
            for (SQLiteConnection connection : this.mAvailableNonPrimaryConnections) {
                connection.collectDbStats(dbStatsList);
            }
            if (this.mAvailableExclusiveConnection != null) {
                this.mAvailableExclusiveConnection.collectDbStats(dbStatsList);
            }
            for (SQLiteConnection connection2 : this.mAcquiredConnections.keySet()) {
                connection2.collectDbStatsUnsafe(dbStatsList);
            }
        }
    }

    private SQLiteConnection openConnectionLocked(SQLiteDatabaseConfiguration configuration, boolean primaryConnection) {
        int connectionId = this.mNextConnectionId;
        this.mNextConnectionId = connectionId + 1;
        return SQLiteConnection.open(this, configuration, connectionId, primaryConnection);
    }

    void onConnectionLeaked() {
        Log.w(TAG, "A SQLiteConnection object for database '" + this.mConfiguration.label + "' was leaked!  Please fix your application " + "to end transactions in progress properly and to close the database " + "when it is no longer needed.");
        this.mConnectionLeaked.set(true);
    }

    private void closeAvailableConnectionsAndLogExceptionsLocked() {
        closeAvailableNonPrimaryConnectionsAndLogExceptionsLocked();
        if (this.mAvailablePrimaryConnection != null) {
            closeConnectionAndLogExceptionsLocked(this.mAvailablePrimaryConnection);
            this.mAvailablePrimaryConnection = null;
        }
        if (this.mAvailableExclusiveConnection != null) {
            closeConnectionAndLogExceptionsLocked(this.mAvailableExclusiveConnection);
            this.mAvailableExclusiveConnection = null;
        }
    }

    private void closeAvailableNonPrimaryConnectionsAndLogExceptionsLocked() {
        int count = this.mAvailableNonPrimaryConnections.size();
        for (int i = 0; i < count; i++) {
            closeConnectionAndLogExceptionsLocked((SQLiteConnection) this.mAvailableNonPrimaryConnections.get(i));
        }
        this.mAvailableNonPrimaryConnections.clear();
    }

    private void closeExcessConnectionsAndLogExceptionsLocked() {
        int size = this.mAvailableNonPrimaryConnections.size();
        while (true) {
            int availableCount = size - 1;
            if (size > getMaxNonPrimaryConnectionSizeLocked()) {
                closeConnectionAndLogExceptionsLocked((SQLiteConnection) this.mAvailableNonPrimaryConnections.remove(availableCount));
                size = availableCount;
            } else {
                return;
            }
        }
    }

    private void closeConnectionAndLogExceptionsLocked(SQLiteConnection connection) {
        try {
            connection.close();
        } catch (RuntimeException ex) {
            Log.e(TAG, "Failed to close connection, its fate is now in the hands of the merciful GC: " + connection, ex);
        }
    }

    private void discardAcquiredConnectionsLocked() {
        markAcquiredConnectionsLocked(AcquiredConnectionStatus.DISCARD);
    }

    private void reconfigureAllConnectionsLocked() {
        if (this.mAvailablePrimaryConnection != null) {
            try {
                this.mAvailablePrimaryConnection.reconfigure(this.mConfiguration);
            } catch (RuntimeException ex) {
                Log.e(TAG, "Failed to reconfigure available primary connection, closing it: " + this.mAvailablePrimaryConnection, ex);
                closeConnectionAndLogExceptionsLocked(this.mAvailablePrimaryConnection);
                this.mAvailablePrimaryConnection = null;
            }
        }
        if (this.mAvailableExclusiveConnection != null) {
            try {
                this.mAvailableExclusiveConnection.reconfigure(this.mConfiguration);
            } catch (RuntimeException ex2) {
                Log.e(TAG, "Failed to reconfigure available exclusive connection, closing it: " + this.mAvailableExclusiveConnection, ex2);
                closeConnectionAndLogExceptionsLocked(this.mAvailableExclusiveConnection);
                this.mAvailableExclusiveConnection = null;
            }
        }
        int count = this.mAvailableNonPrimaryConnections.size();
        int i = 0;
        while (i < count) {
            int i2;
            SQLiteConnection connection = (SQLiteConnection) this.mAvailableNonPrimaryConnections.get(i);
            try {
                connection.reconfigure(this.mConfiguration);
                i2 = i;
            } catch (RuntimeException ex22) {
                Log.e(TAG, "Failed to reconfigure available non-primary connection, closing it: " + connection, ex22);
                closeConnectionAndLogExceptionsLocked(connection);
                i2 = i - 1;
                this.mAvailableNonPrimaryConnections.remove(i);
                count--;
            }
            i = i2 + 1;
        }
        markAcquiredConnectionsLocked(AcquiredConnectionStatus.RECONFIGURE);
    }

    private void markAcquiredConnectionsLocked(AcquiredConnectionStatus status) {
        if (!this.mAcquiredConnections.isEmpty()) {
            ArrayList<SQLiteConnection> keysToUpdate = new ArrayList(this.mAcquiredConnections.size());
            for (Entry<SQLiteConnection, AcquiredConnectionStatus> entry : this.mAcquiredConnections.entrySet()) {
                AcquiredConnectionStatus oldStatus = (AcquiredConnectionStatus) entry.getValue();
                if (!(status == oldStatus || oldStatus == AcquiredConnectionStatus.DISCARD)) {
                    keysToUpdate.add((SQLiteConnection) entry.getKey());
                }
            }
            int updateCount = keysToUpdate.size();
            for (int i = 0; i < updateCount; i++) {
                this.mAcquiredConnections.put((SQLiteConnection) keysToUpdate.get(i), status);
            }
        }
    }

    /* JADX WARNING: Missing block: B:44:0x0091, code:
            if (r31 == null) goto L_0x00a3;
     */
    /* JADX WARNING: Missing block: B:45:0x0093, code:
            r1 = r24;
            r2 = r17;
            r31.setOnCancelListener(new android.database.sqlite.SQLiteConnectionPool.AnonymousClass1(r28));
     */
    /* JADX WARNING: Missing block: B:46:0x00a3, code:
            r14 = 30000;
     */
    /* JADX WARNING: Missing block: B:48:?, code:
            r18 = r24.mStartTime + 30000;
     */
    /* JADX WARNING: Missing block: B:50:0x00b7, code:
            if (r28.mConnectionLeaked.compareAndSet(true, false) == false) goto L_0x00c2;
     */
    /* JADX WARNING: Missing block: B:51:0x00b9, code:
            r4 = r28.mLock;
     */
    /* JADX WARNING: Missing block: B:52:0x00bd, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:54:?, code:
            wakeConnectionWaitersLocked();
     */
    /* JADX WARNING: Missing block: B:56:?, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:57:0x00c2, code:
            java.util.concurrent.locks.LockSupport.parkNanos(r28, 1000000 * r14);
            java.lang.Thread.interrupted();
            r5 = r28.mLock;
     */
    /* JADX WARNING: Missing block: B:58:0x00d2, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:60:?, code:
            throwIfClosedLocked();
            r13 = r24.mAssignedConnection;
            r16 = r24.mException;
     */
    /* JADX WARNING: Missing block: B:61:0x00e0, code:
            if (r13 != null) goto L_0x00e4;
     */
    /* JADX WARNING: Missing block: B:62:0x00e2, code:
            if (r16 == null) goto L_0x011c;
     */
    /* JADX WARNING: Missing block: B:63:0x00e4, code:
            recycleConnectionWaiterLocked(r24);
     */
    /* JADX WARNING: Missing block: B:64:0x00eb, code:
            if (r13 == null) goto L_0x0118;
     */
    /* JADX WARNING: Missing block: B:66:?, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:67:0x00ee, code:
            if (r31 == null) goto L_0x00f6;
     */
    /* JADX WARNING: Missing block: B:68:0x00f0, code:
            r31.setOnCancelListener(null);
     */
    /* JADX WARNING: Missing block: B:69:0x00f6, code:
            return r13;
     */
    /* JADX WARNING: Missing block: B:82:0x010f, code:
            if (r31 != null) goto L_0x0111;
     */
    /* JADX WARNING: Missing block: B:83:0x0111, code:
            r31.setOnCancelListener(null);
     */
    /* JADX WARNING: Missing block: B:86:?, code:
            throw r16;
     */
    /* JADX WARNING: Missing block: B:92:?, code:
            r20 = android.os.SystemClock.uptimeMillis();
     */
    /* JADX WARNING: Missing block: B:94:0x0122, code:
            if (r20 >= r18) goto L_0x0128;
     */
    /* JADX WARNING: Missing block: B:95:0x0124, code:
            r14 = r20 - r18;
     */
    /* JADX WARNING: Missing block: B:97:?, code:
            monitor-exit(r5);
     */
    /* JADX WARNING: Missing block: B:100:?, code:
            logConnectionPoolBusyLocked(r20 - r24.mStartTime, r30);
     */
    /* JADX WARNING: Missing block: B:101:0x0139, code:
            r14 = 30000;
            r18 = r20 + 30000;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private SQLiteConnection waitForConnection(String sql, int connectionFlags, CancellationSignal cancellationSignal) {
        boolean wantPrimaryConnection = (connectionFlags & 2) != 0;
        boolean wantExclusiveConnection = (connectionFlags & 8) != 0;
        synchronized (this.mLock) {
            throwIfClosedLocked();
            if (cancellationSignal != null) {
                cancellationSignal.throwIfCanceled();
            }
            SQLiteConnection connection = null;
            if (!wantPrimaryConnection && wantExclusiveConnection && isExclusiveConnectionEnabled()) {
                connection = tryAcquireExclusiveConnectionLocked(connectionFlags);
            }
            if (connection == null && (wantPrimaryConnection ^ 1) != 0) {
                connection = tryAcquireNonPrimaryConnectionLocked(sql, connectionFlags);
            }
            if (connection == null && (!isExclusiveConnectionEnabled() || (isExclusiveConnectionEnabled() && wantPrimaryConnection))) {
                connection = tryAcquirePrimaryConnectionLocked(connectionFlags);
            }
            if (connection != null) {
                return connection;
            }
            int priority = getPriority(connectionFlags);
            ConnectionWaiter waiter = obtainConnectionWaiterLocked(Thread.currentThread(), SystemClock.uptimeMillis(), priority, wantPrimaryConnection, wantExclusiveConnection, sql, connectionFlags);
            ConnectionWaiter predecessor = null;
            for (ConnectionWaiter successor = this.mConnectionWaiterQueue; successor != null; successor = successor.mNext) {
                if (priority > successor.mPriority) {
                    waiter.mNext = successor;
                    break;
                }
                predecessor = successor;
            }
            if (predecessor != null) {
                predecessor.mNext = waiter;
            } else {
                this.mConnectionWaiterQueue = waiter;
            }
            int nonce = waiter.mNonce;
        }
    }

    private void cancelConnectionWaiterLocked(ConnectionWaiter waiter) {
        if (waiter.mAssignedConnection == null && waiter.mException == null) {
            ConnectionWaiter predecessor = null;
            ConnectionWaiter current = this.mConnectionWaiterQueue;
            while (current != waiter) {
                if (-assertionsDisabled || current != null) {
                    predecessor = current;
                    current = current.mNext;
                } else {
                    throw new AssertionError();
                }
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
        msg.append("The connection pool for database '").append(this.mConfiguration.label);
        msg.append("' has been unable to grant a connection to thread ");
        msg.append(thread.getId()).append(" (").append(thread.getName()).append(") ");
        msg.append("with flags 0x").append(Integer.toHexString(connectionFlags));
        msg.append(" for ").append(((float) waitMillis) * 0.001f).append(" seconds.\n");
        ArrayList<String> requests = new ArrayList();
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
        msg.append("Connections: ").append(activeConnections).append(" active, ");
        msg.append(idleConnections).append(" idle, ");
        msg.append(availableConnections).append(" available.\n");
        if (!requests.isEmpty()) {
            msg.append("\nRequests in progress:\n");
            for (String request : requests) {
                msg.append("  ").append(request).append("\n");
            }
        }
        Log.w(TAG, msg.toString());
    }

    private void wakeConnectionWaitersLocked() {
        ConnectionWaiter predecessor = null;
        ConnectionWaiter waiter = this.mConnectionWaiterQueue;
        boolean primaryConnectionNotAvailable = false;
        boolean nonPrimaryConnectionNotAvailable = false;
        boolean exclusiveConnectionNotAvailable = false;
        while (waiter != null) {
            boolean unpark = false;
            if (this.mIsOpen) {
                SQLiteConnection connection = null;
                try {
                    if (!(waiter.mWantPrimaryConnection || !waiter.mWantExclusiveConnection || (exclusiveConnectionNotAvailable ^ 1) == 0)) {
                        connection = tryAcquireExclusiveConnectionLocked(waiter.mConnectionFlags);
                        if (connection == null) {
                            exclusiveConnectionNotAvailable = true;
                        }
                    }
                    if (!(connection != null || (waiter.mWantPrimaryConnection ^ 1) == 0 || (nonPrimaryConnectionNotAvailable ^ 1) == 0)) {
                        connection = tryAcquireNonPrimaryConnectionLocked(waiter.mSql, waiter.mConnectionFlags);
                        if (connection == null) {
                            nonPrimaryConnectionNotAvailable = true;
                        }
                    }
                    if (connection == null && (((isExclusiveConnectionEnabled() && waiter.mWantPrimaryConnection) || (isExclusiveConnectionEnabled() ^ 1) != 0) && (primaryConnectionNotAvailable ^ 1) != 0)) {
                        connection = tryAcquirePrimaryConnectionLocked(waiter.mConnectionFlags);
                        if (connection == null) {
                            primaryConnectionNotAvailable = true;
                        }
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
            } else {
                unpark = true;
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
        connection = openConnectionLocked(this.mConfiguration, true);
        finishAcquireConnectionLocked(connection, connectionFlags);
        return connection;
    }

    private SQLiteConnection tryAcquireNonPrimaryConnectionLocked(String sql, int connectionFlags) {
        SQLiteConnection connection;
        int availableCount = this.mAvailableNonPrimaryConnections.size();
        if (availableCount > 1 && sql != null) {
            for (int i = 0; i < availableCount; i++) {
                connection = (SQLiteConnection) this.mAvailableNonPrimaryConnections.get(i);
                if (connection.isPreparedStatementInCache(sql)) {
                    this.mAvailableNonPrimaryConnections.remove(i);
                    finishAcquireConnectionLocked(connection, connectionFlags);
                    return connection;
                }
            }
        }
        if (availableCount > 0) {
            connection = (SQLiteConnection) this.mAvailableNonPrimaryConnections.remove(availableCount - 1);
            finishAcquireConnectionLocked(connection, connectionFlags);
            return connection;
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
        connection = openConnectionLocked(this.mConfiguration, false);
        finishAcquireConnectionLocked(connection, connectionFlags);
        return connection;
    }

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
        if (waiter != null) {
            int priority = getPriority(connectionFlags);
            while (priority <= waiter.mPriority) {
                if (!holdingPrimaryConnection && (waiter.mWantPrimaryConnection ^ 1) == 0) {
                    waiter = waiter.mNext;
                    if (waiter == null) {
                        break;
                    }
                } else {
                    return true;
                }
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
        if ((this.mConfiguration.openFlags & 536870912) == 0) {
            this.mMaxConnectionPoolSize = 1;
        } else if (!this.mConfiguration.configurationEnhancement) {
            this.mMaxConnectionPoolSize = SQLiteGlobal.getWALConnectionPoolSize();
        } else if (this.mConfiguration.explicitWALEnabled) {
            this.mMaxConnectionPoolSize = SQLiteGlobal.getWALConnectionPoolSize();
        } else {
            this.mMaxConnectionPoolSize = 1;
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

    public void dump(Printer printer, boolean verbose) {
        Printer indentedPrinter = PrefixPrinter.create(printer, "    ");
        synchronized (this.mLock) {
            int i;
            printer.println("Connection pool for " + this.mConfiguration.path + ":");
            printer.println("  Open: " + this.mIsOpen);
            printer.println("  Max connections: " + this.mMaxConnectionPoolSize);
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
                printer.println("  Available Exclusive connection:");
                this.mAvailableExclusiveConnection.dump(indentedPrinter, verbose);
            }
            printer.println("  Available non-primary connections:");
            if (this.mAvailableNonPrimaryConnections.isEmpty()) {
                indentedPrinter.println("<none>");
            } else {
                int count = this.mAvailableNonPrimaryConnections.size();
                for (i = 0; i < count; i++) {
                    ((SQLiteConnection) this.mAvailableNonPrimaryConnections.get(i)).dump(indentedPrinter, verbose);
                }
            }
            printer.println("  Acquired connections:");
            if (this.mAcquiredConnections.isEmpty()) {
                indentedPrinter.println("<none>");
            } else {
                for (Entry<SQLiteConnection, AcquiredConnectionStatus> entry : this.mAcquiredConnections.entrySet()) {
                    ((SQLiteConnection) entry.getKey()).dumpUnsafe(indentedPrinter, verbose);
                    indentedPrinter.println("  Status: " + entry.getValue());
                }
            }
            printer.println("  Connection waiters:");
            if (this.mConnectionWaiterQueue != null) {
                i = 0;
                long now = SystemClock.uptimeMillis();
                ConnectionWaiter waiter = this.mConnectionWaiterQueue;
                while (waiter != null) {
                    indentedPrinter.println(i + ": waited for " + (((float) (now - waiter.mStartTime)) * 0.001f) + " ms - thread=" + waiter.mThread + ", priority=" + waiter.mPriority + ", sql='" + waiter.mSql + "'");
                    waiter = waiter.mNext;
                    i++;
                }
            } else {
                indentedPrinter.println("<none>");
            }
        }
    }

    public String toString() {
        return "SQLiteConnectionPool: " + this.mConfiguration.path;
    }

    public void setExclusiveConnectionEnabled(boolean enabled) {
        this.mEnableExclusiveConnection = false;
        if (this.mConfiguration.configurationEnhancement) {
            if ((this.mConfiguration.openFlags & 536870912) != 0 && this.mConfiguration.explicitWALEnabled) {
                this.mEnableExclusiveConnection = enabled;
            }
        } else if ((this.mConfiguration.openFlags & 536870912) != 0) {
            this.mEnableExclusiveConnection = enabled;
        }
    }

    private boolean isExclusiveConnectionEnabled() {
        boolean z = false;
        if (this.mConfiguration.configurationEnhancement) {
            if ((this.mConfiguration.openFlags & 536870912) != 0 && this.mConfiguration.explicitWALEnabled) {
                z = this.mEnableExclusiveConnection;
            }
            return z;
        }
        if ((this.mConfiguration.openFlags & 536870912) != 0) {
            z = this.mEnableExclusiveConnection;
        }
        return z;
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
        connection = openExclusiveConnectionLocked(this.mConfiguration);
        finishAcquireConnectionLocked(connection, connectionFlags);
        return connection;
    }

    private SQLiteConnection openExclusiveConnectionLocked(SQLiteDatabaseConfiguration configuration) {
        int connectionId = this.mNextConnectionId;
        this.mNextConnectionId = connectionId + 1;
        return SQLiteConnection.openExclusive(this, configuration, connectionId);
    }
}
