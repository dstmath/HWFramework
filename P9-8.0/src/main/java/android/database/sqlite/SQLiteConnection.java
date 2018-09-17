package android.database.sqlite;

import android.database.CursorWindow;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDebug.DbStats;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiScanLog;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.util.LruCache;
import android.util.Printer;
import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

public final class SQLiteConnection implements OnCancelListener {
    static final /* synthetic */ boolean -assertionsDisabled = (SQLiteConnection.class.desiredAssertionStatus() ^ 1);
    private static final boolean DEBUG = false;
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String TAG = "SQLiteConnection";
    private static final boolean enableSqlAudit = SystemProperties.getBoolean("persist.sys.sql_audit_enable", false);
    private SQLiteAuditLog mAuditLog;
    private int mCancellationSignalAttachCount;
    private final CloseGuard mCloseGuard;
    private final SQLiteDatabaseConfiguration mConfiguration;
    private final int mConnectionId;
    private long mConnectionPtr;
    private final boolean mIsExclusiveConnection;
    private final boolean mIsPrimaryConnection;
    private final boolean mIsReadOnlyConnection;
    private boolean mOnlyAllowReadOnlyOperations;
    private final SQLiteConnectionPool mPool;
    private final PreparedStatementCache mPreparedStatementCache;
    private PreparedStatement mPreparedStatementPool;
    private final OperationLog mRecentOperations;

    private static final class Operation {
        private static final int MAX_TRACE_METHOD_NAME_LEN = 256;
        public ArrayList<Object> mBindArgs;
        public int mCookie;
        public long mEndTime;
        public Exception mException;
        public boolean mFinished;
        public String mKind;
        public String mSql;
        public long mStartTime;
        public long mStartWallTime;

        /* synthetic */ Operation(Operation -this0) {
            this();
        }

        private Operation() {
        }

        public void describe(StringBuilder msg, boolean verbose) {
            msg.append(this.mKind);
            if (this.mFinished) {
                msg.append(" took ").append(this.mEndTime - this.mStartTime).append("ms");
            } else {
                msg.append(" started ").append(System.currentTimeMillis() - this.mStartWallTime).append("ms ago");
            }
            msg.append(" - ").append(getStatus());
            if (this.mSql != null) {
                msg.append(", sql=\"").append(SQLiteConnection.trimSqlForDisplay(this.mSql)).append("\"");
            }
            if (!(!verbose || this.mBindArgs == null || this.mBindArgs.size() == 0)) {
                msg.append(", bindArgs=[");
                int count = this.mBindArgs.size();
                for (int i = 0; i < count; i++) {
                    Object arg = this.mBindArgs.get(i);
                    if (i != 0) {
                        msg.append(", ");
                    }
                    if (arg == null) {
                        msg.append("null");
                    } else if (arg instanceof byte[]) {
                        msg.append("<byte[]>");
                    } else if (arg instanceof String) {
                        msg.append("\"").append((String) arg).append("\"");
                    } else {
                        msg.append(arg);
                    }
                }
                msg.append("]");
            }
            if (this.mException != null) {
                msg.append(", exception=\"").append(this.mException.getMessage()).append("\"");
            }
        }

        private String getStatus() {
            if (!this.mFinished) {
                return "running";
            }
            return this.mException != null ? "failed" : "succeeded";
        }

        private String getTraceMethodName() {
            String methodName = this.mKind + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mSql;
            if (methodName.length() > 256) {
                return methodName.substring(0, 256);
            }
            return methodName;
        }

        private String getFormattedStartTime() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(this.mStartWallTime));
        }
    }

    private static final class OperationLog {
        private static final int COOKIE_GENERATION_SHIFT = 8;
        private static final int COOKIE_INDEX_MASK = 255;
        private static final int MAX_RECENT_OPERATIONS = 20;
        private int mGeneration;
        private int mIndex;
        private final Operation[] mOperations;

        /* synthetic */ OperationLog(OperationLog -this0) {
            this();
        }

        private OperationLog() {
            this.mOperations = new Operation[20];
        }

        public int beginOperation(String kind, String sql, Object[] bindArgs) {
            Operation operation;
            synchronized (this.mOperations) {
                int index = (this.mIndex + 1) % 20;
                operation = this.mOperations[index];
                if (operation == null) {
                    operation = new Operation();
                    this.mOperations[index] = operation;
                } else {
                    operation.mFinished = false;
                    operation.mException = null;
                    if (operation.mBindArgs != null) {
                        operation.mBindArgs.clear();
                    }
                }
                operation.mStartWallTime = System.currentTimeMillis();
                operation.mStartTime = SystemClock.uptimeMillis();
                operation.mKind = kind;
                operation.mSql = sql;
                if (bindArgs != null) {
                    if (operation.mBindArgs == null) {
                        operation.mBindArgs = new ArrayList();
                    } else {
                        operation.mBindArgs.clear();
                    }
                    for (Object arg : bindArgs) {
                        if (arg == null || !(arg instanceof byte[])) {
                            operation.mBindArgs.add(arg);
                        } else {
                            operation.mBindArgs.add(SQLiteConnection.EMPTY_BYTE_ARRAY);
                        }
                    }
                }
                operation.mCookie = newOperationCookieLocked(index);
                if (Trace.isTagEnabled(1048576)) {
                    Trace.asyncTraceBegin(1048576, operation.getTraceMethodName(), operation.mCookie);
                }
                this.mIndex = index;
            }
            return operation.mCookie;
        }

        public void failOperation(int cookie, Exception ex) {
            synchronized (this.mOperations) {
                Operation operation = getOperationLocked(cookie);
                if (operation != null) {
                    operation.mException = ex;
                }
            }
        }

        public void endOperation(int cookie) {
            synchronized (this.mOperations) {
                if (endOperationDeferLogLocked(cookie)) {
                    logOperationLocked(cookie, null);
                }
            }
        }

        public boolean endOperationDeferLog(int cookie) {
            boolean endOperationDeferLogLocked;
            synchronized (this.mOperations) {
                endOperationDeferLogLocked = endOperationDeferLogLocked(cookie);
            }
            return endOperationDeferLogLocked;
        }

        public void logOperation(int cookie, String detail) {
            synchronized (this.mOperations) {
                logOperationLocked(cookie, detail);
            }
        }

        private boolean endOperationDeferLogLocked(int cookie) {
            boolean z = false;
            Operation operation = getOperationLocked(cookie);
            if (operation == null) {
                return false;
            }
            if (Trace.isTagEnabled(1048576)) {
                Trace.asyncTraceEnd(1048576, operation.getTraceMethodName(), operation.mCookie);
            }
            operation.mEndTime = SystemClock.uptimeMillis();
            operation.mFinished = true;
            if (SQLiteDebug.DEBUG_LOG_SLOW_QUERIES) {
                z = SQLiteDebug.shouldLogSlowQuery(operation.mEndTime - operation.mStartTime);
            }
            return z;
        }

        private void logOperationLocked(int cookie, String detail) {
            Operation operation = getOperationLocked(cookie);
            StringBuilder msg = new StringBuilder();
            operation.describe(msg, false);
            if (detail != null) {
                msg.append(", ").append(detail);
            }
            Log.d(SQLiteConnection.TAG, msg.toString());
        }

        private int newOperationCookieLocked(int index) {
            int generation = this.mGeneration;
            this.mGeneration = generation + 1;
            return (generation << 8) | index;
        }

        private Operation getOperationLocked(int cookie) {
            Operation operation = this.mOperations[cookie & 255];
            return operation.mCookie == cookie ? operation : null;
        }

        /* JADX WARNING: Missing block: B:11:0x0022, code:
            return null;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public String describeCurrentOperation() {
            synchronized (this.mOperations) {
                Operation operation = this.mOperations[this.mIndex];
                if (operation == null || (operation.mFinished ^ 1) == 0) {
                } else {
                    StringBuilder msg = new StringBuilder();
                    operation.describe(msg, false);
                    String stringBuilder = msg.toString();
                    return stringBuilder;
                }
            }
        }

        public void dump(Printer printer, boolean verbose) {
            synchronized (this.mOperations) {
                printer.println("  Most recently executed operations:");
                int index = this.mIndex;
                Operation operation = this.mOperations[index];
                if (operation != null) {
                    int n = 0;
                    do {
                        StringBuilder msg = new StringBuilder();
                        msg.append("    ").append(n).append(": [");
                        msg.append(operation.getFormattedStartTime());
                        msg.append("] ");
                        operation.describe(msg, verbose);
                        printer.println(msg.toString());
                        if (index > 0) {
                            index--;
                        } else {
                            index = 19;
                        }
                        n++;
                        operation = this.mOperations[index];
                        if (operation == null) {
                            break;
                        }
                    } while (n < 20);
                } else {
                    printer.println("    <none>");
                }
            }
        }
    }

    private static final class PreparedStatement {
        public boolean mInCache;
        public boolean mInUse;
        public int mNumParameters;
        public PreparedStatement mPoolNext;
        public boolean mReadOnly;
        public String mSql;
        public long mStatementPtr;
        public int mType;

        /* synthetic */ PreparedStatement(PreparedStatement -this0) {
            this();
        }

        private PreparedStatement() {
        }
    }

    private final class PreparedStatementCache extends LruCache<String, PreparedStatement> {
        public PreparedStatementCache(int size) {
            super(size);
        }

        protected void entryRemoved(boolean evicted, String key, PreparedStatement oldValue, PreparedStatement newValue) {
            oldValue.mInCache = false;
            if (!oldValue.mInUse) {
                SQLiteConnection.this.finalizePreparedStatement(oldValue);
            }
        }

        public void dump(Printer printer) {
            printer.println("  Prepared statement cache:");
            Map<String, PreparedStatement> cache = snapshot();
            if (cache.isEmpty()) {
                printer.println("    <none>");
                return;
            }
            int i = 0;
            for (Entry<String, PreparedStatement> entry : cache.entrySet()) {
                PreparedStatement statement = (PreparedStatement) entry.getValue();
                if (statement.mInCache) {
                    printer.println("    " + i + ": statementPtr=0x" + Long.toHexString(statement.mStatementPtr) + ", numParameters=" + statement.mNumParameters + ", type=" + statement.mType + ", readOnly=" + statement.mReadOnly + ", sql=\"" + SQLiteConnection.trimSqlForDisplay((String) entry.getKey()) + "\"");
                }
                i++;
            }
        }
    }

    private static native void nativeBindBlob(long j, long j2, int i, byte[] bArr);

    private static native void nativeBindDouble(long j, long j2, int i, double d);

    private static native void nativeBindLong(long j, long j2, int i, long j3);

    private static native void nativeBindNull(long j, long j2, int i);

    private static native void nativeBindString(long j, long j2, int i, String str);

    private static native void nativeCancel(long j);

    private static native void nativeClose(long j);

    private static native void nativeExecute(long j, long j2);

    private static native int nativeExecuteForBlobFileDescriptor(long j, long j2);

    private static native int nativeExecuteForChangedRowCount(long j, long j2);

    private static native long nativeExecuteForCursorWindow(long j, long j2, long j3, int i, int i2, boolean z);

    private static native long nativeExecuteForLastInsertedRowId(long j, long j2);

    private static native long nativeExecuteForLong(long j, long j2);

    private static native String nativeExecuteForString(long j, long j2);

    private static native void nativeFinalizeStatement(long j, long j2);

    private static native int nativeGetColumnCount(long j, long j2);

    private static native String nativeGetColumnName(long j, long j2, int i);

    private static native int nativeGetDbLookaside(long j);

    private static native int nativeGetParameterCount(long j, long j2);

    private static native boolean nativeIsReadOnly(long j, long j2);

    private static native long nativeOpen(String str, int i, String str2, boolean z, boolean z2);

    private static native long nativePrepareStatement(long j, String str);

    private static native void nativeRegisterCustomFunction(long j, SQLiteCustomFunction sQLiteCustomFunction);

    private static native void nativeRegisterLocalizedCollators(long j, String str);

    private static native void nativeResetCancel(long j, boolean z);

    private static native void nativeResetStatementAndClearBindings(long j, long j2);

    private SQLiteConnection(SQLiteConnectionPool pool, SQLiteDatabaseConfiguration configuration, int connectionId, boolean primaryConnection) {
        this(pool, configuration, connectionId, primaryConnection, false);
    }

    private SQLiteConnection(SQLiteConnectionPool pool, SQLiteDatabaseConfiguration configuration, int connectionId, boolean primaryConnection, boolean exclusiveConnection) {
        boolean z = false;
        this.mCloseGuard = CloseGuard.get();
        this.mRecentOperations = new OperationLog();
        this.mAuditLog = null;
        this.mPool = pool;
        this.mConfiguration = new SQLiteDatabaseConfiguration(configuration);
        this.mConnectionId = connectionId;
        this.mIsPrimaryConnection = primaryConnection;
        if ((configuration.openFlags & 1) != 0) {
            z = true;
        }
        this.mIsReadOnlyConnection = z;
        this.mPreparedStatementCache = new PreparedStatementCache(this.mConfiguration.maxSqlCacheSize);
        this.mIsExclusiveConnection = exclusiveConnection;
        this.mCloseGuard.open("close");
    }

    protected void finalize() throws Throwable {
        try {
            if (!(this.mPool == null || this.mConnectionPtr == 0)) {
                this.mPool.onConnectionLeaked();
            }
            dispose(true);
        } finally {
            super.finalize();
        }
    }

    static SQLiteConnection open(SQLiteConnectionPool pool, SQLiteDatabaseConfiguration configuration, int connectionId, boolean primaryConnection) {
        SQLiteConnection connection = new SQLiteConnection(pool, configuration, connectionId, primaryConnection);
        try {
            connection.open();
            return connection;
        } catch (SQLiteException ex) {
            connection.dispose(false);
            throw ex;
        }
    }

    void close() {
        dispose(false);
    }

    private void open() {
        this.mConnectionPtr = nativeOpen(this.mConfiguration.path, this.mConfiguration.openFlags, this.mConfiguration.label, SQLiteDebug.DEBUG_SQL_STATEMENTS, SQLiteDebug.DEBUG_SQL_TIME);
        setPageSize();
        setForeignKeyModeFromConfiguration();
        setWalModeFromConfiguration();
        setJournalSizeLimit();
        setAutoCheckpointInterval();
        setLocaleFromConfiguration();
        int functionCount = this.mConfiguration.customFunctions.size();
        for (int i = 0; i < functionCount; i++) {
            nativeRegisterCustomFunction(this.mConnectionPtr, (SQLiteCustomFunction) this.mConfiguration.customFunctions.get(i));
        }
        if (enableSqlAudit && this.mAuditLog == null) {
            this.mAuditLog = new SQLiteAuditLog(this.mConfiguration.path, this.mConnectionPtr);
        }
        if (this.mAuditLog != null) {
            this.mAuditLog.enableAudit();
        }
    }

    private void dispose(boolean finalized) {
        if (this.mCloseGuard != null) {
            if (finalized) {
                this.mCloseGuard.warnIfOpen();
            }
            this.mCloseGuard.close();
        }
        if (this.mConnectionPtr != 0) {
            int cookie = this.mRecentOperations.beginOperation("close", null, null);
            try {
                this.mPreparedStatementCache.evictAll();
                if (this.mAuditLog != null) {
                    this.mAuditLog.disableAudit();
                }
                nativeClose(this.mConnectionPtr);
                this.mConnectionPtr = 0;
            } finally {
                this.mRecentOperations.endOperation(cookie);
            }
        }
    }

    private void setPageSize() {
        if (!this.mConfiguration.isInMemoryDb() && (this.mIsReadOnlyConnection ^ 1) != 0) {
            long newValue = (long) SQLiteGlobal.getDefaultPageSize();
            if (executeForLong("PRAGMA page_size", null, null) != newValue) {
                execute("PRAGMA page_size=" + newValue, null, null);
            }
        }
    }

    private void setAutoCheckpointInterval() {
        if (!this.mConfiguration.isInMemoryDb() && (this.mIsReadOnlyConnection ^ 1) != 0) {
            long newValue = (long) SQLiteGlobal.getWALAutoCheckpoint();
            if (executeForLong("PRAGMA wal_autocheckpoint", null, null) != newValue) {
                executeForLong("PRAGMA wal_autocheckpoint=" + newValue, null, null);
            }
        }
    }

    private void setJournalSizeLimit() {
        if (!this.mConfiguration.isInMemoryDb() && (this.mIsReadOnlyConnection ^ 1) != 0) {
            long newValue = (long) SQLiteGlobal.getJournalSizeLimit();
            if (executeForLong("PRAGMA journal_size_limit", null, null) != newValue) {
                executeForLong("PRAGMA journal_size_limit=" + newValue, null, null);
            }
        }
    }

    private void setForeignKeyModeFromConfiguration() {
        if (!this.mIsReadOnlyConnection) {
            long newValue = (long) (this.mConfiguration.foreignKeyConstraintsEnabled ? 1 : 0);
            if (executeForLong("PRAGMA foreign_keys", null, null) != newValue) {
                execute("PRAGMA foreign_keys=" + newValue, null, null);
            }
        }
    }

    private void setWalModeFromConfiguration() {
        if (!this.mConfiguration.isInMemoryDb() && (this.mIsReadOnlyConnection ^ 1) != 0) {
            if ((this.mConfiguration.openFlags & 536870912) != 0) {
                setJournalMode("WAL");
                String sWALSyncMode = SQLiteGlobal.getWALSyncMode();
                if (!this.mConfiguration.configurationEnhancement || sWALSyncMode.equalsIgnoreCase("NORMAL")) {
                    setSyncMode(sWALSyncMode);
                    return;
                } else {
                    setSyncMode("NORMAL");
                    return;
                }
            }
            setJournalMode(SQLiteGlobal.getDefaultJournalMode());
            setSyncMode(SQLiteGlobal.getDefaultSyncMode());
        }
    }

    private void setSyncMode(String newValue) {
        if (!canonicalizeSyncMode(executeForString("PRAGMA synchronous", null, null)).equalsIgnoreCase(canonicalizeSyncMode(newValue))) {
            execute("PRAGMA synchronous=" + newValue, null, null);
        }
    }

    private static String canonicalizeSyncMode(String value) {
        if (value.equals(WifiEnterpriseConfig.ENGINE_DISABLE)) {
            return "OFF";
        }
        if (value.equals("1")) {
            return "NORMAL";
        }
        if (value.equals(WifiScanLog.EVENT_KEY2)) {
            return "FULL";
        }
        return value;
    }

    private void setJournalMode(String newValue) {
        String value = executeForString("PRAGMA journal_mode", null, null);
        if (!value.equalsIgnoreCase(newValue)) {
            try {
                if (executeForString("PRAGMA journal_mode=" + newValue, null, null).equalsIgnoreCase(newValue)) {
                    return;
                }
            } catch (SQLiteDatabaseLockedException e) {
            }
            Log.w(TAG, "Could not change the database journal mode of '" + this.mConfiguration.label + "' from '" + value + "' to '" + newValue + "' because the database is locked.  This usually means that " + "there are other open connections to the database which prevents " + "the database from enabling or disabling write-ahead logging mode.  " + "Proceeding without changing the journal mode.");
        }
    }

    private void setLocaleFromConfiguration() {
        if ((this.mConfiguration.openFlags & 16) == 0) {
            String newLocale = this.mConfiguration.locale.toString();
            nativeRegisterLocalizedCollators(this.mConnectionPtr, newLocale);
            if (!this.mIsReadOnlyConnection) {
                try {
                    execute("CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT)", null, null);
                    String oldLocale = executeForString("SELECT locale FROM android_metadata UNION SELECT NULL ORDER BY locale DESC LIMIT 1", null, null);
                    if (oldLocale == null || !oldLocale.equals(newLocale)) {
                        execute("BEGIN", null, null);
                        execute("DELETE FROM android_metadata", null, null);
                        execute("INSERT INTO android_metadata (locale) VALUES(?)", new Object[]{newLocale}, null);
                        execute("REINDEX LOCALIZED", null, null);
                        execute(true ? "COMMIT" : "ROLLBACK", null, null);
                    }
                } catch (RuntimeException ex) {
                    Log.w(TAG, "Failed to change locale for db '" + this.mConfiguration.label + "' to '" + newLocale + "'.", ex);
                } catch (Throwable th) {
                    execute(false ? "COMMIT" : "ROLLBACK", null, null);
                }
            }
        }
    }

    void reconfigure(SQLiteDatabaseConfiguration configuration) {
        this.mOnlyAllowReadOnlyOperations = false;
        int functionCount = configuration.customFunctions.size();
        for (int i = 0; i < functionCount; i++) {
            SQLiteCustomFunction function = (SQLiteCustomFunction) configuration.customFunctions.get(i);
            if (!this.mConfiguration.customFunctions.contains(function)) {
                nativeRegisterCustomFunction(this.mConnectionPtr, function);
            }
        }
        boolean foreignKeyModeChanged = configuration.foreignKeyConstraintsEnabled != this.mConfiguration.foreignKeyConstraintsEnabled;
        boolean walModeChanged = ((configuration.openFlags ^ this.mConfiguration.openFlags) & 536870912) != 0;
        boolean localeChanged = configuration.locale.equals(this.mConfiguration.locale) ^ 1;
        this.mConfiguration.updateParametersFrom(configuration);
        this.mPreparedStatementCache.resize(configuration.maxSqlCacheSize);
        if (foreignKeyModeChanged) {
            setForeignKeyModeFromConfiguration();
        }
        if (walModeChanged) {
            setWalModeFromConfiguration();
        }
        if (localeChanged) {
            setLocaleFromConfiguration();
        }
    }

    void setOnlyAllowReadOnlyOperations(boolean readOnly) {
        this.mOnlyAllowReadOnlyOperations = readOnly;
    }

    boolean isPreparedStatementInCache(String sql) {
        return this.mPreparedStatementCache.get(sql) != null;
    }

    public int getConnectionId() {
        return this.mConnectionId;
    }

    public boolean isPrimaryConnection() {
        return this.mIsPrimaryConnection;
    }

    public void prepare(String sql, SQLiteStatementInfo outStatementInfo) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        }
        int cookie = this.mRecentOperations.beginOperation("prepare", sql, null);
        PreparedStatement statement;
        try {
            statement = acquirePreparedStatement(sql);
            if (outStatementInfo != null) {
                outStatementInfo.numParameters = statement.mNumParameters;
                outStatementInfo.readOnly = statement.mReadOnly;
                int columnCount = nativeGetColumnCount(this.mConnectionPtr, statement.mStatementPtr);
                if (columnCount == 0) {
                    outStatementInfo.columnNames = EMPTY_STRING_ARRAY;
                } else {
                    outStatementInfo.columnNames = new String[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        outStatementInfo.columnNames[i] = nativeGetColumnName(this.mConnectionPtr, statement.mStatementPtr, i);
                    }
                }
            }
            releasePreparedStatement(statement);
            this.mRecentOperations.endOperation(cookie);
        } catch (RuntimeException ex) {
            try {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th) {
                this.mRecentOperations.endOperation(cookie);
            }
        } catch (Throwable th2) {
            releasePreparedStatement(statement);
        }
    }

    public void execute(String sql, Object[] bindArgs, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        }
        int cookie = this.mRecentOperations.beginOperation("execute", sql, bindArgs);
        try {
            PreparedStatement statement = acquirePreparedStatement(sql);
            try {
                throwIfStatementForbidden(statement);
                bindArguments(statement, bindArgs);
                applyBlockGuardPolicy(statement);
                attachCancellationSignal(cancellationSignal);
                nativeExecute(this.mConnectionPtr, statement.mStatementPtr);
                detachCancellationSignal(cancellationSignal);
                releasePreparedStatement(statement);
                this.mRecentOperations.endOperation(cookie);
            } catch (Throwable th) {
                releasePreparedStatement(statement);
            }
        } catch (RuntimeException ex) {
            try {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th2) {
                this.mRecentOperations.endOperation(cookie);
            }
        }
    }

    public long executeForLong(String sql, Object[] bindArgs, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        }
        int cookie = this.mRecentOperations.beginOperation("executeForLong", sql, bindArgs);
        try {
            PreparedStatement statement = acquirePreparedStatement(sql);
            try {
                throwIfStatementForbidden(statement);
                bindArguments(statement, bindArgs);
                applyBlockGuardPolicy(statement);
                attachCancellationSignal(cancellationSignal);
                long nativeExecuteForLong = nativeExecuteForLong(this.mConnectionPtr, statement.mStatementPtr);
                detachCancellationSignal(cancellationSignal);
                releasePreparedStatement(statement);
                this.mRecentOperations.endOperation(cookie);
                return nativeExecuteForLong;
            } catch (Throwable th) {
                releasePreparedStatement(statement);
            }
        } catch (RuntimeException ex) {
            try {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th2) {
                this.mRecentOperations.endOperation(cookie);
            }
        }
    }

    public String executeForString(String sql, Object[] bindArgs, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        }
        int cookie = this.mRecentOperations.beginOperation("executeForString", sql, bindArgs);
        try {
            PreparedStatement statement = acquirePreparedStatement(sql);
            try {
                throwIfStatementForbidden(statement);
                bindArguments(statement, bindArgs);
                applyBlockGuardPolicy(statement);
                attachCancellationSignal(cancellationSignal);
                String nativeExecuteForString = nativeExecuteForString(this.mConnectionPtr, statement.mStatementPtr);
                detachCancellationSignal(cancellationSignal);
                releasePreparedStatement(statement);
                this.mRecentOperations.endOperation(cookie);
                return nativeExecuteForString;
            } catch (Throwable th) {
                releasePreparedStatement(statement);
            }
        } catch (RuntimeException ex) {
            try {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th2) {
                this.mRecentOperations.endOperation(cookie);
            }
        }
    }

    public ParcelFileDescriptor executeForBlobFileDescriptor(String sql, Object[] bindArgs, CancellationSignal cancellationSignal) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        }
        int cookie = this.mRecentOperations.beginOperation("executeForBlobFileDescriptor", sql, bindArgs);
        try {
            PreparedStatement statement = acquirePreparedStatement(sql);
            try {
                throwIfStatementForbidden(statement);
                bindArguments(statement, bindArgs);
                applyBlockGuardPolicy(statement);
                attachCancellationSignal(cancellationSignal);
                int fd = nativeExecuteForBlobFileDescriptor(this.mConnectionPtr, statement.mStatementPtr);
                if (fd >= 0) {
                    parcelFileDescriptor = ParcelFileDescriptor.adoptFd(fd);
                }
                detachCancellationSignal(cancellationSignal);
                releasePreparedStatement(statement);
                this.mRecentOperations.endOperation(cookie);
                return parcelFileDescriptor;
            } catch (Throwable th) {
                releasePreparedStatement(statement);
            }
        } catch (RuntimeException ex) {
            try {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th2) {
                this.mRecentOperations.endOperation(cookie);
            }
        }
    }

    public int executeForChangedRowCount(String sql, Object[] bindArgs, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        }
        int changedRows = 0;
        int cookie = this.mRecentOperations.beginOperation("executeForChangedRowCount", sql, bindArgs);
        try {
            PreparedStatement statement = acquirePreparedStatement(sql);
            try {
                throwIfStatementForbidden(statement);
                bindArguments(statement, bindArgs);
                applyBlockGuardPolicy(statement);
                attachCancellationSignal(cancellationSignal);
                changedRows = nativeExecuteForChangedRowCount(this.mConnectionPtr, statement.mStatementPtr);
                detachCancellationSignal(cancellationSignal);
                releasePreparedStatement(statement);
                if (this.mRecentOperations.endOperationDeferLog(cookie)) {
                    this.mRecentOperations.logOperation(cookie, "changedRows=" + changedRows);
                }
                return changedRows;
            } catch (Throwable th) {
                releasePreparedStatement(statement);
            }
        } catch (RuntimeException ex) {
            try {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th2) {
                if (this.mRecentOperations.endOperationDeferLog(cookie)) {
                    this.mRecentOperations.logOperation(cookie, "changedRows=" + changedRows);
                }
            }
        }
    }

    public long executeForLastInsertedRowId(String sql, Object[] bindArgs, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        }
        int cookie = this.mRecentOperations.beginOperation("executeForLastInsertedRowId", sql, bindArgs);
        try {
            PreparedStatement statement = acquirePreparedStatement(sql);
            try {
                throwIfStatementForbidden(statement);
                bindArguments(statement, bindArgs);
                applyBlockGuardPolicy(statement);
                attachCancellationSignal(cancellationSignal);
                long nativeExecuteForLastInsertedRowId = nativeExecuteForLastInsertedRowId(this.mConnectionPtr, statement.mStatementPtr);
                detachCancellationSignal(cancellationSignal);
                releasePreparedStatement(statement);
                this.mRecentOperations.endOperation(cookie);
                return nativeExecuteForLastInsertedRowId;
            } catch (Throwable th) {
                releasePreparedStatement(statement);
            }
        } catch (RuntimeException ex) {
            try {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th2) {
                this.mRecentOperations.endOperation(cookie);
            }
        }
    }

    public int executeForCursorWindow(String sql, Object[] bindArgs, CursorWindow window, int startPos, int requiredPos, boolean countAllRows, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (window == null) {
            throw new IllegalArgumentException("window must not be null.");
        } else {
            window.acquireReference();
            int actualPos = -1;
            int countedRows = -1;
            int filledRows = -1;
            try {
                int cookie = this.mRecentOperations.beginOperation("executeForCursorWindow", sql, bindArgs);
                try {
                    PreparedStatement statement = acquirePreparedStatement(sql);
                    try {
                        throwIfStatementForbidden(statement);
                        bindArguments(statement, bindArgs);
                        applyBlockGuardPolicy(statement);
                        attachCancellationSignal(cancellationSignal);
                        long result = nativeExecuteForCursorWindow(this.mConnectionPtr, statement.mStatementPtr, window.mWindowPtr, startPos, requiredPos, countAllRows);
                        actualPos = (int) (result >> 32);
                        countedRows = (int) result;
                        filledRows = window.getNumRows();
                        window.setStartPosition(actualPos);
                        detachCancellationSignal(cancellationSignal);
                        releasePreparedStatement(statement);
                        if (this.mRecentOperations.endOperationDeferLog(cookie)) {
                            this.mRecentOperations.logOperation(cookie, "window='" + window + "', startPos=" + startPos + ", actualPos=" + actualPos + ", filledRows=" + filledRows + ", countedRows=" + countedRows);
                        }
                        window.releaseReference();
                        return countedRows;
                    } catch (Throwable th) {
                        releasePreparedStatement(statement);
                    }
                } catch (Exception ex) {
                    try {
                        this.mRecentOperations.failOperation(cookie, ex);
                        throw ex;
                    } catch (Throwable th2) {
                        if (this.mRecentOperations.endOperationDeferLog(cookie)) {
                            this.mRecentOperations.logOperation(cookie, "window='" + window + "', startPos=" + startPos + ", actualPos=" + actualPos + ", filledRows=" + filledRows + ", countedRows=" + countedRows);
                        }
                    }
                }
            } catch (Throwable th3) {
                window.releaseReference();
            }
        }
    }

    private PreparedStatement acquirePreparedStatement(String sql) {
        PreparedStatement statement = (PreparedStatement) this.mPreparedStatementCache.get(sql);
        boolean skipCache = false;
        if (statement != null) {
            if (!statement.mInUse) {
                return statement;
            }
            skipCache = true;
        }
        long statementPtr = nativePrepareStatement(this.mConnectionPtr, sql);
        try {
            int numParameters = nativeGetParameterCount(this.mConnectionPtr, statementPtr);
            int type = DatabaseUtils.getSqlStatementType(sql);
            statement = obtainPreparedStatement(sql, statementPtr, numParameters, type, nativeIsReadOnly(this.mConnectionPtr, statementPtr));
            if (!skipCache && isCacheable(type)) {
                this.mPreparedStatementCache.put(sql, statement);
                statement.mInCache = true;
            }
            statement.mInUse = true;
            return statement;
        } catch (RuntimeException ex) {
            if (statement == null || (statement.mInCache ^ 1) != 0) {
                nativeFinalizeStatement(this.mConnectionPtr, statementPtr);
            }
            throw ex;
        }
    }

    private void releasePreparedStatement(PreparedStatement statement) {
        statement.mInUse = false;
        if (statement.mInCache) {
            try {
                nativeResetStatementAndClearBindings(this.mConnectionPtr, statement.mStatementPtr);
                return;
            } catch (SQLiteException e) {
                this.mPreparedStatementCache.remove(statement.mSql);
                return;
            }
        }
        finalizePreparedStatement(statement);
    }

    private void finalizePreparedStatement(PreparedStatement statement) {
        nativeFinalizeStatement(this.mConnectionPtr, statement.mStatementPtr);
        recyclePreparedStatement(statement);
    }

    private void attachCancellationSignal(CancellationSignal cancellationSignal) {
        if (cancellationSignal != null) {
            cancellationSignal.throwIfCanceled();
            this.mCancellationSignalAttachCount++;
            if (this.mCancellationSignalAttachCount == 1) {
                nativeResetCancel(this.mConnectionPtr, true);
                cancellationSignal.setOnCancelListener(this);
            }
        }
    }

    private void detachCancellationSignal(CancellationSignal cancellationSignal) {
        if (cancellationSignal == null) {
            return;
        }
        if (-assertionsDisabled || this.mCancellationSignalAttachCount > 0) {
            this.mCancellationSignalAttachCount--;
            if (this.mCancellationSignalAttachCount == 0) {
                cancellationSignal.setOnCancelListener(null);
                nativeResetCancel(this.mConnectionPtr, false);
                return;
            }
            return;
        }
        throw new AssertionError();
    }

    public void onCancel() {
        nativeCancel(this.mConnectionPtr);
    }

    private void bindArguments(PreparedStatement statement, Object[] bindArgs) {
        int count = bindArgs != null ? bindArgs.length : 0;
        if (count != statement.mNumParameters) {
            throw new SQLiteBindOrColumnIndexOutOfRangeException("Expected " + statement.mNumParameters + " bind arguments but " + count + " were provided.");
        } else if (count != 0) {
            long statementPtr = statement.mStatementPtr;
            for (int i = 0; i < count; i++) {
                Object arg = bindArgs[i];
                switch (DatabaseUtils.getTypeOfObject(arg)) {
                    case 0:
                        nativeBindNull(this.mConnectionPtr, statementPtr, i + 1);
                        break;
                    case 1:
                        nativeBindLong(this.mConnectionPtr, statementPtr, i + 1, ((Number) arg).longValue());
                        break;
                    case 2:
                        nativeBindDouble(this.mConnectionPtr, statementPtr, i + 1, ((Number) arg).doubleValue());
                        break;
                    case 4:
                        nativeBindBlob(this.mConnectionPtr, statementPtr, i + 1, (byte[]) arg);
                        break;
                    default:
                        if (!(arg instanceof Boolean)) {
                            nativeBindString(this.mConnectionPtr, statementPtr, i + 1, arg.toString());
                            break;
                        }
                        int i2;
                        long j = this.mConnectionPtr;
                        int i3 = i + 1;
                        if (((Boolean) arg).booleanValue()) {
                            i2 = 1;
                        } else {
                            i2 = 0;
                        }
                        nativeBindLong(j, statementPtr, i3, (long) i2);
                        break;
                }
            }
        }
    }

    private void throwIfStatementForbidden(PreparedStatement statement) {
        if (this.mOnlyAllowReadOnlyOperations && (statement.mReadOnly ^ 1) != 0) {
            throw new SQLiteException("Cannot execute this statement because it might modify the database but the connection is read-only.");
        }
    }

    private static boolean isCacheable(int statementType) {
        if (statementType == 2 || statementType == 1) {
            return true;
        }
        return false;
    }

    private void applyBlockGuardPolicy(PreparedStatement statement) {
        if (!this.mConfiguration.isInMemoryDb()) {
            if (statement.mReadOnly) {
                BlockGuard.getThreadPolicy().onReadFromDisk();
            } else {
                BlockGuard.getThreadPolicy().onWriteToDisk();
            }
        }
    }

    public void dump(Printer printer, boolean verbose) {
        dumpUnsafe(printer, verbose);
    }

    void dumpUnsafe(Printer printer, boolean verbose) {
        printer.println("Connection #" + this.mConnectionId + ":");
        if (verbose) {
            printer.println("  connectionPtr: 0x" + Long.toHexString(this.mConnectionPtr));
        }
        printer.println("  isPrimaryConnection: " + this.mIsPrimaryConnection);
        printer.println("  onlyAllowReadOnlyOperations: " + this.mOnlyAllowReadOnlyOperations);
        this.mRecentOperations.dump(printer, verbose);
        if (verbose) {
            this.mPreparedStatementCache.dump(printer);
        }
    }

    String describeCurrentOperationUnsafe() {
        return this.mRecentOperations.describeCurrentOperation();
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0100 A:{ExcHandler: all (th java.lang.Throwable), Splitter: B:4:0x0035} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:22:0x0101, code:
            r11.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void collectDbStats(ArrayList<DbStats> dbStatsList) {
        int lookaside = nativeGetDbLookaside(this.mConnectionPtr);
        long pageCount = 0;
        long pageSize = 0;
        try {
            pageCount = executeForLong("PRAGMA page_count;", null, null);
            pageSize = executeForLong("PRAGMA page_size;", null, null);
        } catch (SQLiteException e) {
        }
        dbStatsList.add(getMainDbStatsUnsafe(lookaside, pageCount, pageSize));
        CursorWindow window = new CursorWindow("collectDbStats");
        try {
            executeForCursorWindow("PRAGMA database_list;", null, window, 0, 0, false, null);
            for (int i = 1; i < window.getNumRows(); i++) {
                String name = window.getString(i, 1);
                String path = window.getString(i, 2);
                pageCount = 0;
                pageSize = 0;
                pageCount = executeForLong("PRAGMA " + name + ".page_count;", null, null);
                pageSize = executeForLong("PRAGMA " + name + ".page_size;", null, null);
                String label = "  (attached) " + name;
                if (!path.isEmpty()) {
                    label = label + ": " + path;
                }
                dbStatsList.add(new DbStats(label, pageCount, pageSize, 0, 0, 0, 0));
            }
            window.close();
        } catch (SQLiteException e2) {
            window.close();
        } catch (Throwable th) {
        }
    }

    void collectDbStatsUnsafe(ArrayList<DbStats> dbStatsList) {
        dbStatsList.add(getMainDbStatsUnsafe(0, 0, 0));
    }

    private DbStats getMainDbStatsUnsafe(int lookaside, long pageCount, long pageSize) {
        String label = this.mConfiguration.path;
        if (!this.mIsPrimaryConnection) {
            label = label + " (" + this.mConnectionId + ")";
        }
        return new DbStats(label, pageCount, pageSize, lookaside, this.mPreparedStatementCache.hitCount(), this.mPreparedStatementCache.missCount(), this.mPreparedStatementCache.size());
    }

    public String toString() {
        return "SQLiteConnection: " + this.mConfiguration.path + " (" + this.mConnectionId + ")";
    }

    private PreparedStatement obtainPreparedStatement(String sql, long statementPtr, int numParameters, int type, boolean readOnly) {
        PreparedStatement statement = this.mPreparedStatementPool;
        if (statement != null) {
            this.mPreparedStatementPool = statement.mPoolNext;
            statement.mPoolNext = null;
            statement.mInCache = false;
        } else {
            statement = new PreparedStatement();
        }
        statement.mSql = sql;
        statement.mStatementPtr = statementPtr;
        statement.mNumParameters = numParameters;
        statement.mType = type;
        statement.mReadOnly = readOnly;
        return statement;
    }

    private void recyclePreparedStatement(PreparedStatement statement) {
        statement.mSql = null;
        statement.mPoolNext = this.mPreparedStatementPool;
        this.mPreparedStatementPool = statement;
    }

    private static String trimSqlForDisplay(String sql) {
        return sql.replaceAll("[\\s]*\\n+[\\s]*", WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
    }

    static SQLiteConnection openExclusive(SQLiteConnectionPool pool, SQLiteDatabaseConfiguration configuration, int connectionId) {
        SQLiteConnection connection = new SQLiteConnection(pool, configuration, connectionId, false, true);
        try {
            connection.open();
            return connection;
        } catch (SQLiteException ex) {
            connection.dispose(false);
            throw ex;
        }
    }

    public boolean isExclusiveConnection() {
        return this.mIsExclusiveConnection;
    }
}
