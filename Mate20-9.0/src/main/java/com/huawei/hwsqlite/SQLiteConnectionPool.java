package com.huawei.hwsqlite;

import android.annotation.SuppressLint;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import android.os.SystemClock;
import android.util.Log;
import android.util.Printer;
import com.huawei.hwsqlite.SQLiteDebug;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public final class SQLiteConnectionPool implements Closeable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int CONNECTION_FLAG_INTERACTIVE = 4;
    public static final int CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY = 2;
    public static final int CONNECTION_FLAG_READ_ONLY = 1;
    private static final long CONNECTION_POOL_BUSY_MILLIS = 30000;
    private static final String TAG = "SQLiteConnectionPool";
    private final WeakHashMap<SQLiteConnection, AcquiredConnectionStatus> mAcquiredConnections = new WeakHashMap<>();
    private final ArrayList<SQLiteConnection> mAvailableNonPrimaryConnections = new ArrayList<>();
    private SQLiteConnection mAvailablePrimaryConnection;
    private final SQLiteCloseGuard mCloseGuard = SQLiteCloseGuard.get();
    private final SQLiteDatabaseConfiguration mConfiguration;
    private final AtomicBoolean mConnectionLeaked = new AtomicBoolean();
    private ConnectionWaiter mConnectionWaiterPool;
    private ConnectionWaiter mConnectionWaiterQueue;
    private boolean mIsOpen;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
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
        public boolean mWantPrimaryConnection;

        private ConnectionWaiter() {
        }
    }

    private SQLiteConnectionPool(SQLiteDatabaseConfiguration configuration) {
        this.mConfiguration = new SQLiteDatabaseConfiguration(configuration);
        setMaxConnectionPoolSizeLocked();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            dispose(true);
        } finally {
            super.finalize();
        }
    }

    public static SQLiteConnectionPool open(SQLiteDatabaseConfiguration configuration) {
        if (configuration != null) {
            SQLiteConnectionPool pool = new SQLiteConnectionPool(configuration);
            pool.open();
            return pool;
        }
        throw new IllegalArgumentException("configuration must not be null.");
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
                    Log.i(TAG, "The connection pool for " + this.mConfiguration.label + " has been closed but there are still " + pendingCount + " connections in use.  They will be closed as they are released back to the pool.");
                }
                wakeConnectionWaitersLocked();
            }
        }
    }

    public void reconfigure(SQLiteDatabaseConfiguration configuration) {
        if (configuration != null) {
            synchronized (this.mLock) {
                throwIfClosedLocked();
                boolean foreignKeyModeChanged = false;
                boolean walModeChanged = ((configuration.openFlags ^ this.mConfiguration.openFlags) & 536870912) != 0;
                if (walModeChanged) {
                    if (this.mAcquiredConnections.isEmpty()) {
                        closeAvailableNonPrimaryConnectionsAndLogExceptionsLocked();
                    } else {
                        throw new IllegalStateException("Write Ahead Logging (WAL) mode cannot be enabled or disabled while there are transactions in progress.  Finish all transactions and release all active database connections first.");
                    }
                }
                if (configuration.foreignKeyConstraintsEnabled != this.mConfiguration.foreignKeyConstraintsEnabled) {
                    foreignKeyModeChanged = true;
                }
                if (foreignKeyModeChanged) {
                    if (!this.mAcquiredConnections.isEmpty()) {
                        throw new IllegalStateException("Foreign Key Constraints cannot be enabled or disabled while there are transactions in progress.  Finish all transactions and release all active database connections first.");
                    }
                }
                if (this.mConfiguration.openFlags != configuration.openFlags) {
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
            }
            return;
        }
        throw new IllegalArgumentException("configuration must not be null.");
    }

    public void changeEncryptKey(SQLiteEncryptKeyLoader newKeyLoader) throws SQLiteException {
        synchronized (this.mLock) {
            throwIfClosedLocked();
            if (this.mAcquiredConnections.isEmpty()) {
                closeAvailableNonPrimaryConnectionsAndLogExceptionsLocked();
                if (this.mAvailablePrimaryConnection == null) {
                    this.mAvailablePrimaryConnection = openConnectionLocked(this.mConfiguration, true);
                }
                this.mAvailablePrimaryConnection.changeEncryptKey(newKeyLoader);
                this.mConfiguration.updateEncryptKeyLoader(newKeyLoader);
                wakeConnectionWaitersLocked();
            } else {
                throw new SQLiteBusyException("The encryptKey cannot be changed while there are transactions in progress.Finish all transactions and release all active database connections first.");
            }
        }
    }

    public void addAttachAlias(SQLiteAttached attached) throws SQLiteException {
        if (attached != null) {
            synchronized (this.mLock) {
                throwIfClosedLocked();
                if (this.mAcquiredConnections.isEmpty()) {
                    closeAvailableNonPrimaryConnectionsAndLogExceptionsLocked();
                    if (this.mAvailablePrimaryConnection == null) {
                        this.mAvailablePrimaryConnection = openConnectionLocked(this.mConfiguration, true);
                    }
                    this.mAvailablePrimaryConnection.addAttachAlias(attached);
                    this.mConfiguration.addAttachAlias(attached);
                    wakeConnectionWaitersLocked();
                } else {
                    throw new SQLiteBusyException("Attached alias cannot be added while there are transactions in progress. Finish all transactions and release all active database connections first.");
                }
            }
            return;
        }
        throw new IllegalArgumentException("attached parameter must not be null");
    }

    public void removeAttachedAlias(String alias) {
        if (alias == null || alias.length() == 0) {
            throw new IllegalArgumentException("Alias name must not be empty");
        }
        synchronized (this.mLock) {
            throwIfClosedLocked();
            if (this.mAcquiredConnections.isEmpty()) {
                closeAvailableNonPrimaryConnectionsAndLogExceptionsLocked();
                if (this.mAvailablePrimaryConnection == null) {
                    this.mAvailablePrimaryConnection = openConnectionLocked(this.mConfiguration, true);
                }
                this.mAvailablePrimaryConnection.removeAttachAlias(alias);
                this.mConfiguration.removeAttachAlias(alias);
                wakeConnectionWaitersLocked();
            } else {
                throw new SQLiteBusyException("Detach cannot be done while there are transactions in progress. Finish all transactions and release all active database connections first.");
            }
        }
    }

    public SQLiteConnection acquireConnection(String sql, int connectionFlags, CancellationSignal cancellationSignal) {
        return waitForConnection(sql, connectionFlags, cancellationSignal);
    }

    public void releaseConnection(SQLiteConnection connection) {
        synchronized (this.mLock) {
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
            } else if (this.mAvailableNonPrimaryConnections.size() >= this.mMaxConnectionPoolSize - 1) {
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
            } else if (!this.mIsOpen) {
                return false;
            } else {
                boolean isSessionBlockingImportantConnectionWaitersLocked = isSessionBlockingImportantConnectionWaitersLocked(connection.isPrimaryConnection(), connectionFlags);
                return isSessionBlockingImportantConnectionWaitersLocked;
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

    private void closeAvailableConnectionsAndLogExceptionsLocked() {
        closeAvailableNonPrimaryConnectionsAndLogExceptionsLocked();
        if (this.mAvailablePrimaryConnection != null) {
            closeConnectionAndLogExceptionsLocked(this.mAvailablePrimaryConnection);
            this.mAvailablePrimaryConnection = null;
        }
    }

    private void closeAvailableNonPrimaryConnectionsAndLogExceptionsLocked() {
        int count = this.mAvailableNonPrimaryConnections.size();
        for (int i = 0; i < count; i++) {
            closeConnectionAndLogExceptionsLocked(this.mAvailableNonPrimaryConnections.get(i));
        }
        this.mAvailableNonPrimaryConnections.clear();
    }

    private void closeExcessConnectionsAndLogExceptionsLocked() {
        int availableCount = this.mAvailableNonPrimaryConnections.size();
        while (true) {
            int availableCount2 = availableCount - 1;
            if (availableCount > this.mMaxConnectionPoolSize - 1) {
                closeConnectionAndLogExceptionsLocked(this.mAvailableNonPrimaryConnections.remove(availableCount2));
                availableCount = availableCount2;
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
        int count = this.mAvailableNonPrimaryConnections.size();
        int i = 0;
        while (i < count) {
            SQLiteConnection connection = this.mAvailableNonPrimaryConnections.get(i);
            try {
                connection.reconfigure(this.mConfiguration);
            } catch (RuntimeException ex2) {
                Log.e(TAG, "Failed to reconfigure available non-primary connection, closing it: " + connection, ex2);
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

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0067, code lost:
        if (r11 == null) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0069, code lost:
        r11.setOnCancelListener(new com.huawei.hwsqlite.SQLiteConnectionPool.AnonymousClass1(r9));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0071, code lost:
        r3 = CONNECTION_POOL_BUSY_MILLIS;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        r6 = r1.mStartTime + CONNECTION_POOL_BUSY_MILLIS;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x007d, code lost:
        if (r9.mConnectionLeaked.compareAndSet(r13, false) == false) goto L_0x008e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        r12 = r9.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0081, code lost:
        monitor-enter(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        wakeConnectionWaitersLocked();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0085, code lost:
        monitor-exit(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x008a, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x008b, code lost:
        r17 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x008e, code lost:
        r17 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
        java.util.concurrent.locks.LockSupport.parkNanos(r9, r3 * 1000000);
        java.lang.Thread.interrupted();
        r12 = r9.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x009d, code lost:
        monitor-enter(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:?, code lost:
        throwIfClosedLocked();
        r0 = r1.mAssignedConnection;
        r13 = r1.mException;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00a5, code lost:
        if (r0 != null) goto L_0x00c9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00a7, code lost:
        if (r13 == null) goto L_0x00ac;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00a9, code lost:
        r18 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00ac, code lost:
        r14 = android.os.SystemClock.uptimeMillis();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00b2, code lost:
        if (r14 >= r6) goto L_0x00b7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00b4, code lost:
        r3 = r14 - r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00b7, code lost:
        r18 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:?, code lost:
        logConnectionPoolBusyLocked(r14 - r1.mStartTime, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x00c0, code lost:
        r3 = CONNECTION_POOL_BUSY_MILLIS;
        r6 = r14 + CONNECTION_POOL_BUSY_MILLIS;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:?, code lost:
        monitor-exit(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x00c5, code lost:
        r14 = r17;
        r13 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x00c9, code lost:
        r18 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:?, code lost:
        recycleConnectionWaiterLocked(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x00ce, code lost:
        if (r0 == null) goto L_0x00d8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x00d0, code lost:
        monitor-exit(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x00d1, code lost:
        if (r11 == null) goto L_0x00d7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x00d3, code lost:
        r11.setOnCancelListener(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x00d7, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:?, code lost:
        throw r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x00d9, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x00da, code lost:
        r6 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x00dd, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x00de, code lost:
        r18 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:?, code lost:
        monitor-exit(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:?, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x00e2, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x00e4, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x00e6, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x00e7, code lost:
        r17 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x00e9, code lost:
        if (r11 != null) goto L_0x00eb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x00eb, code lost:
        r11.setOnCancelListener(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x00ef, code lost:
        throw r0;
     */
    private SQLiteConnection waitForConnection(String sql, int connectionFlags, CancellationSignal cancellationSignal) {
        int priority = connectionFlags;
        CancellationSignal cancellationSignal2 = cancellationSignal;
        boolean z = true;
        boolean wantPrimaryConnection = (priority & 2) != 0;
        synchronized (this.mLock) {
            try {
                throwIfClosedLocked();
                if (cancellationSignal2 != null) {
                    try {
                        cancellationSignal.throwIfCanceled();
                    } catch (Throwable th) {
                        th = th;
                        boolean z2 = wantPrimaryConnection;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                        throw th;
                    }
                }
                SQLiteConnection connection = null;
                if (!wantPrimaryConnection) {
                    connection = tryAcquireNonPrimaryConnectionLocked(sql, connectionFlags);
                }
                if (connection == null) {
                    connection = tryAcquirePrimaryConnectionLocked(priority);
                }
                if (connection != null) {
                    return connection;
                }
                int priority2 = getPriority(connectionFlags);
                int priority3 = priority2;
                final ConnectionWaiter waiter = obtainConnectionWaiterLocked(Thread.currentThread(), SystemClock.uptimeMillis(), priority2, wantPrimaryConnection, sql, priority);
                ConnectionWaiter predecessor = null;
                ConnectionWaiter successor = this.mConnectionWaiterQueue;
                while (true) {
                    if (successor == null) {
                        break;
                    } else if (priority3 > successor.mPriority) {
                        waiter.mNext = successor;
                        break;
                    } else {
                        predecessor = successor;
                        successor = successor.mNext;
                    }
                }
                if (predecessor != null) {
                    predecessor.mNext = waiter;
                } else {
                    this.mConnectionWaiterQueue = waiter;
                }
                final int nonce = waiter.mNonce;
            } catch (Throwable th3) {
                th = th3;
                boolean z3 = wantPrimaryConnection;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public void cancelConnectionWaiterLocked(ConnectionWaiter waiter) {
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

    @SuppressLint({"PreferForInArrayList"})
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

    private void wakeConnectionWaitersLocked() {
        ConnectionWaiter predecessor = null;
        ConnectionWaiter waiter = this.mConnectionWaiterQueue;
        boolean primaryConnectionNotAvailable = false;
        boolean nonPrimaryConnectionNotAvailable = false;
        while (waiter != null) {
            boolean unpark = false;
            if (!this.mIsOpen) {
                unpark = true;
            } else {
                SQLiteConnection connection = null;
                try {
                    if (!waiter.mWantPrimaryConnection && !nonPrimaryConnectionNotAvailable) {
                        connection = tryAcquireNonPrimaryConnectionLocked(waiter.mSql, waiter.mConnectionFlags);
                        if (connection == null) {
                            nonPrimaryConnectionNotAvailable = true;
                        }
                    }
                    if (connection == null && !primaryConnectionNotAvailable) {
                        connection = tryAcquirePrimaryConnectionLocked(waiter.mConnectionFlags);
                        if (connection == null) {
                            primaryConnectionNotAvailable = true;
                        }
                    }
                    if (connection != null) {
                        waiter.mAssignedConnection = connection;
                        unpark = true;
                    } else if (nonPrimaryConnectionNotAvailable && primaryConnectionNotAvailable) {
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
        if (openConnections >= this.mMaxConnectionPoolSize) {
            return null;
        }
        SQLiteConnection connection3 = openConnectionLocked(this.mConfiguration, false);
        finishAcquireConnectionLocked(connection3, connectionFlags);
        return connection3;
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
                if (!holdingPrimaryConnection && waiter.mWantPrimaryConnection) {
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
        return (connectionFlags & 4) != 0 ? 1 : 0;
    }

    private void setMaxConnectionPoolSizeLocked() {
        if ((this.mConfiguration.openFlags & 536870912) == 0) {
            this.mMaxConnectionPoolSize = 1;
        } else if (this.mConfiguration.maxConnectionCount == 0) {
            this.mMaxConnectionPoolSize = SQLiteGlobal.getWALConnectionPoolSize();
        } else {
            this.mMaxConnectionPoolSize = this.mConfiguration.maxConnectionCount;
        }
    }

    private void throwIfClosedLocked() {
        if (!this.mIsOpen) {
            throw new IllegalStateException("Cannot perform this operation because the connection pool has been closed.");
        }
    }

    private ConnectionWaiter obtainConnectionWaiterLocked(Thread thread, long startTime, int priority, boolean wantPrimaryConnection, String sql, int connectionFlags) {
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
    }

    public String toString() {
        return "SQLiteConnectionPool: " + this.mConfiguration.path;
    }
}
