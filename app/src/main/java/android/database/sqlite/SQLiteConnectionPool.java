package android.database.sqlite;

import android.database.sqlite.SQLiteDebug.DbStats;
import android.hardware.SensorManager;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.OperationCanceledException;
import android.os.SystemClock;
import android.security.keymaster.KeymasterDefs;
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
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final int CONNECTION_FLAG_EXCLUSIVE = 8;
    public static final int CONNECTION_FLAG_INTERACTIVE = 4;
    public static final int CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY = 2;
    public static final int CONNECTION_FLAG_READ_ONLY = 1;
    private static final long CONNECTION_POOL_BUSY_MILLIS = 30000;
    private static final String TAG = "SQLiteConnectionPool";
    private final WeakHashMap<SQLiteConnection, AcquiredConnectionStatus> mAcquiredConnections;
    private SQLiteConnection mAvailableExclusiveConnection;
    private final ArrayList<SQLiteConnection> mAvailableNonPrimaryConnections;
    private SQLiteConnection mAvailablePrimaryConnection;
    private final CloseGuard mCloseGuard;
    private final SQLiteDatabaseConfiguration mConfiguration;
    private final AtomicBoolean mConnectionLeaked;
    private ConnectionWaiter mConnectionWaiterPool;
    private ConnectionWaiter mConnectionWaiterQueue;
    private boolean mEnableExclusiveConnection;
    private boolean mIsOpen;
    private final Object mLock;
    private int mMaxConnectionPoolSize;
    private int mNextConnectionId;

    /* renamed from: android.database.sqlite.SQLiteConnectionPool.1 */
    class AnonymousClass1 implements OnCancelListener {
        final /* synthetic */ int val$nonce;
        final /* synthetic */ ConnectionWaiter val$waiter;

        AnonymousClass1(ConnectionWaiter val$waiter, int val$nonce) {
            this.val$waiter = val$waiter;
            this.val$nonce = val$nonce;
        }

        public void onCancel() {
            synchronized (SQLiteConnectionPool.this.mLock) {
                if (this.val$waiter.mNonce == this.val$nonce) {
                    SQLiteConnectionPool.this.cancelConnectionWaiterLocked(this.val$waiter);
                }
            }
        }
    }

    enum AcquiredConnectionStatus {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.database.sqlite.SQLiteConnectionPool.AcquiredConnectionStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.database.sqlite.SQLiteConnectionPool.AcquiredConnectionStatus.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.database.sqlite.SQLiteConnectionPool.AcquiredConnectionStatus.<clinit>():void");
        }
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

        /* synthetic */ ConnectionWaiter(ConnectionWaiter connectionWaiter) {
            this();
        }

        private ConnectionWaiter() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.database.sqlite.SQLiteConnectionPool.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.database.sqlite.SQLiteConnectionPool.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.database.sqlite.SQLiteConnectionPool.<clinit>():void");
    }

    private SQLiteConnectionPool(SQLiteDatabaseConfiguration configuration) {
        this.mCloseGuard = CloseGuard.get();
        this.mLock = new Object();
        this.mConnectionLeaked = new AtomicBoolean();
        this.mAvailableNonPrimaryConnections = new ArrayList();
        this.mAcquiredConnections = new WeakHashMap();
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
        return open(configuration, -assertionsDisabled);
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
        dispose(-assertionsDisabled);
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
                this.mIsOpen = -assertionsDisabled;
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
        boolean foreignKeyModeChanged = true;
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null.");
        }
        synchronized (this.mLock) {
            throwIfClosedLocked();
            boolean walModeChanged = ((configuration.openFlags ^ this.mConfiguration.openFlags) & KeymasterDefs.KM_ENUM_REP) != 0 ? true : -assertionsDisabled;
            if (configuration.configurationEnhancement) {
                walModeChanged = (walModeChanged || configuration.defaultWALEnabled != this.mConfiguration.defaultWALEnabled) ? true : configuration.explicitWALEnabled != this.mConfiguration.explicitWALEnabled ? true : -assertionsDisabled;
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
            if (configuration.foreignKeyConstraintsEnabled == this.mConfiguration.foreignKeyConstraintsEnabled) {
                foreignKeyModeChanged = -assertionsDisabled;
            }
            if (!foreignKeyModeChanged || this.mAcquiredConnections.isEmpty()) {
                boolean configEnhanceChanged;
                if (this.mConfiguration.configurationEnhancement) {
                    configEnhanceChanged = walModeChanged;
                } else {
                    configEnhanceChanged = -assertionsDisabled;
                }
                if (this.mConfiguration.openFlags != configuration.openFlags || r0) {
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
        Object obj = CONNECTION_FLAG_READ_ONLY;
        synchronized (this.mLock) {
            AcquiredConnectionStatus status = (AcquiredConnectionStatus) this.mAcquiredConnections.remove(connection);
            if (status == null) {
                throw new IllegalStateException("Cannot perform this operation because the specified connection was not acquired from this pool or has already been released.");
            }
            if (!this.mIsOpen) {
                closeConnectionAndLogExceptionsLocked(connection);
            } else if (connection.isPrimaryConnection()) {
                if (recycleConnectionLocked(connection, status)) {
                    if (!-assertionsDisabled) {
                        if (this.mAvailablePrimaryConnection != null) {
                            obj = null;
                        }
                        if (obj == null) {
                            throw new AssertionError();
                        }
                    }
                    this.mAvailablePrimaryConnection = connection;
                }
                wakeConnectionWaitersLocked();
            } else if (connection.isExclusiveConnection()) {
                if (recycleConnectionLocked(connection, status)) {
                    if (!-assertionsDisabled) {
                        if (this.mAvailableExclusiveConnection != null) {
                            obj = null;
                        }
                        if (obj == null) {
                            throw new AssertionError();
                        }
                    }
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
        return -assertionsDisabled;
    }

    public boolean shouldYieldConnection(SQLiteConnection connection, int connectionFlags) {
        synchronized (this.mLock) {
            if (!this.mAcquiredConnections.containsKey(connection)) {
                throw new IllegalStateException("Cannot perform this operation because the specified connection was not acquired from this pool or has already been released.");
            } else if (this.mIsOpen) {
                boolean isSessionBlockingImportantConnectionWaitersLocked = isSessionBlockingImportantConnectionWaitersLocked(connection.isPrimaryConnection(), connectionFlags);
                return isSessionBlockingImportantConnectionWaitersLocked;
            } else {
                return -assertionsDisabled;
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
        this.mNextConnectionId = connectionId + CONNECTION_FLAG_READ_ONLY;
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
        for (int i = 0; i < count; i += CONNECTION_FLAG_READ_ONLY) {
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
            i = i2 + CONNECTION_FLAG_READ_ONLY;
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
            for (int i = 0; i < updateCount; i += CONNECTION_FLAG_READ_ONLY) {
                this.mAcquiredConnections.put((SQLiteConnection) keysToUpdate.get(i), status);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private SQLiteConnection waitForConnection(String sql, int connectionFlags, CancellationSignal cancellationSignal) {
        boolean wantPrimaryConnection = (connectionFlags & CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY) != 0 ? true : -assertionsDisabled;
        boolean wantExclusiveConnection = (connectionFlags & CONNECTION_FLAG_EXCLUSIVE) != 0 ? true : -assertionsDisabled;
        synchronized (this.mLock) {
            throwIfClosedLocked();
            if (cancellationSignal != null) {
                cancellationSignal.throwIfCanceled();
            }
            SQLiteConnection connection = null;
            if (!wantPrimaryConnection && wantExclusiveConnection && isExclusiveConnectionEnabled()) {
                connection = tryAcquireExclusiveConnectionLocked(connectionFlags);
            }
            if (connection == null && !wantPrimaryConnection) {
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
            if (cancellationSignal != null) {
                cancellationSignal.setOnCancelListener(new AnonymousClass1(waiter, nonce));
            }
            long busyTimeoutMillis = CONNECTION_POOL_BUSY_MILLIS;
            long nextBusyTimeoutTime = waiter.mStartTime + CONNECTION_POOL_BUSY_MILLIS;
            while (true) {
                if (this.mConnectionLeaked.compareAndSet(true, -assertionsDisabled)) {
                    synchronized (this.mLock) {
                        wakeConnectionWaitersLocked();
                        try {
                        } catch (Throwable th) {
                            if (cancellationSignal != null) {
                                cancellationSignal.setOnCancelListener(null);
                            }
                        }
                    }
                }
                LockSupport.parkNanos(this, 1000000 * busyTimeoutMillis);
                Thread.interrupted();
                synchronized (this.mLock) {
                    throwIfClosedLocked();
                    connection = waiter.mAssignedConnection;
                    RuntimeException ex = waiter.mException;
                    if (connection == null && ex == null) {
                        long now = SystemClock.uptimeMillis();
                        if (now < nextBusyTimeoutTime) {
                            busyTimeoutMillis = now - nextBusyTimeoutTime;
                        } else {
                            logConnectionPoolBusyLocked(now - waiter.mStartTime, connectionFlags);
                            busyTimeoutMillis = CONNECTION_POOL_BUSY_MILLIS;
                            nextBusyTimeoutTime = now + CONNECTION_POOL_BUSY_MILLIS;
                        }
                    } else {
                        recycleConnectionWaiterLocked(waiter);
                    }
                }
            }
        }
    }

    private void cancelConnectionWaiterLocked(ConnectionWaiter waiter) {
        if (waiter.mAssignedConnection == null && waiter.mException == null) {
            ConnectionWaiter predecessor = null;
            ConnectionWaiter current = this.mConnectionWaiterQueue;
            while (current != waiter) {
                if (!-assertionsDisabled) {
                    if ((current != null ? CONNECTION_FLAG_READ_ONLY : null) == null) {
                        throw new AssertionError();
                    }
                }
                predecessor = current;
                current = current.mNext;
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
        msg.append(" for ").append(((float) waitMillis) * SensorManager.LIGHT_NO_MOON).append(" seconds.\n");
        ArrayList<String> requests = new ArrayList();
        int activeConnections = 0;
        int idleConnections = 0;
        if (!this.mAcquiredConnections.isEmpty()) {
            for (SQLiteConnection connection : this.mAcquiredConnections.keySet()) {
                String description = connection.describeCurrentOperationUnsafe();
                if (description != null) {
                    requests.add(description);
                    activeConnections += CONNECTION_FLAG_READ_ONLY;
                } else {
                    idleConnections += CONNECTION_FLAG_READ_ONLY;
                }
            }
        }
        int availableConnections = this.mAvailableNonPrimaryConnections.size();
        if (this.mAvailablePrimaryConnection != null) {
            availableConnections += CONNECTION_FLAG_READ_ONLY;
        }
        if (this.mAvailableExclusiveConnection != null) {
            availableConnections += CONNECTION_FLAG_READ_ONLY;
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
        boolean primaryConnectionNotAvailable = -assertionsDisabled;
        boolean nonPrimaryConnectionNotAvailable = -assertionsDisabled;
        boolean exclusiveConnectionNotAvailable = -assertionsDisabled;
        while (waiter != null) {
            boolean unpark = -assertionsDisabled;
            if (this.mIsOpen) {
                SQLiteConnection connection = null;
                try {
                    if (!(waiter.mWantPrimaryConnection || !waiter.mWantExclusiveConnection || exclusiveConnectionNotAvailable)) {
                        connection = tryAcquireExclusiveConnectionLocked(waiter.mConnectionFlags);
                        if (connection == null) {
                            exclusiveConnectionNotAvailable = true;
                        }
                    }
                    if (!(connection != null || waiter.mWantPrimaryConnection || nonPrimaryConnectionNotAvailable)) {
                        connection = tryAcquireNonPrimaryConnectionLocked(waiter.mSql, waiter.mConnectionFlags);
                        if (connection == null) {
                            nonPrimaryConnectionNotAvailable = true;
                        }
                    }
                    if (connection == null && (((isExclusiveConnectionEnabled() && waiter.mWantPrimaryConnection) || !isExclusiveConnectionEnabled()) && !primaryConnectionNotAvailable)) {
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
        if (availableCount > CONNECTION_FLAG_READ_ONLY && sql != null) {
            for (int i = 0; i < availableCount; i += CONNECTION_FLAG_READ_ONLY) {
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
            openConnections += CONNECTION_FLAG_READ_ONLY;
        }
        if (isExclusiveConnectionEnabled()) {
            openConnections += CONNECTION_FLAG_READ_ONLY;
        }
        if (openConnections >= this.mMaxConnectionPoolSize) {
            return null;
        }
        connection = openConnectionLocked(this.mConfiguration, -assertionsDisabled);
        finishAcquireConnectionLocked(connection, connectionFlags);
        return connection;
    }

    private void finishAcquireConnectionLocked(SQLiteConnection connection, int connectionFlags) {
        boolean readOnly = -assertionsDisabled;
        if ((connectionFlags & CONNECTION_FLAG_READ_ONLY) != 0) {
            readOnly = true;
        }
        try {
            connection.setOnlyAllowReadOnlyOperations(readOnly);
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
        return -assertionsDisabled;
    }

    private static int getPriority(int connectionFlags) {
        if ((connectionFlags & CONNECTION_FLAG_INTERACTIVE) != 0) {
            return CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY;
        }
        if ((connectionFlags & CONNECTION_FLAG_EXCLUSIVE) == 0 || (connectionFlags & CONNECTION_FLAG_PRIMARY_CONNECTION_AFFINITY) == 0) {
            return 0;
        }
        return CONNECTION_FLAG_READ_ONLY;
    }

    private void setMaxConnectionPoolSizeLocked() {
        if ((this.mConfiguration.openFlags & KeymasterDefs.KM_ENUM_REP) == 0) {
            this.mMaxConnectionPoolSize = CONNECTION_FLAG_READ_ONLY;
        } else if (!this.mConfiguration.configurationEnhancement) {
            this.mMaxConnectionPoolSize = SQLiteGlobal.getWALConnectionPoolSize();
        } else if (this.mConfiguration.explicitWALEnabled) {
            this.mMaxConnectionPoolSize = SQLiteGlobal.getWALConnectionPoolSize();
        } else {
            this.mMaxConnectionPoolSize = CONNECTION_FLAG_READ_ONLY;
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
        waiter.mNonce += CONNECTION_FLAG_READ_ONLY;
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
                for (i = 0; i < count; i += CONNECTION_FLAG_READ_ONLY) {
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
                    indentedPrinter.println(i + ": waited for " + (((float) (now - waiter.mStartTime)) * SensorManager.LIGHT_NO_MOON) + " ms - thread=" + waiter.mThread + ", priority=" + waiter.mPriority + ", sql='" + waiter.mSql + "'");
                    waiter = waiter.mNext;
                    i += CONNECTION_FLAG_READ_ONLY;
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
        this.mEnableExclusiveConnection = -assertionsDisabled;
        if (this.mConfiguration.configurationEnhancement) {
            if ((this.mConfiguration.openFlags & KeymasterDefs.KM_ENUM_REP) != 0 && this.mConfiguration.explicitWALEnabled) {
                this.mEnableExclusiveConnection = enabled;
            }
        } else if ((this.mConfiguration.openFlags & KeymasterDefs.KM_ENUM_REP) != 0) {
            this.mEnableExclusiveConnection = enabled;
        }
    }

    private boolean isExclusiveConnectionEnabled() {
        boolean z = -assertionsDisabled;
        if (this.mConfiguration.configurationEnhancement) {
            if ((this.mConfiguration.openFlags & KeymasterDefs.KM_ENUM_REP) != 0 && this.mConfiguration.explicitWALEnabled) {
                z = this.mEnableExclusiveConnection;
            }
            return z;
        }
        if ((this.mConfiguration.openFlags & KeymasterDefs.KM_ENUM_REP) != 0) {
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
        this.mNextConnectionId = connectionId + CONNECTION_FLAG_READ_ONLY;
        return SQLiteConnection.openExclusive(this, configuration, connectionId);
    }
}
