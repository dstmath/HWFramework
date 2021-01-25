package android.database.sqlite;

import android.database.CursorWindow;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDebug;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiScanLog;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.SettingsStringUtil;
import android.security.keymaster.KeymasterDefs;
import android.util.Log;
import android.util.LruCache;
import android.util.Printer;
import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public final class SQLiteConnection implements CancellationSignal.OnCancelListener {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final boolean DEBUG = false;
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final boolean ENABLE_SQL_AUDIT = SystemProperties.getBoolean("persist.sys.sql_audit_enable", false);
    private static final String TAG = "SQLiteConnection";
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

    private static native long nativeOpen(String str, int i, String str2, boolean z, boolean z2, int i2, int i3);

    private static native long nativePrepareStatement(long j, String str);

    private static native void nativeRegisterCustomFunction(long j, SQLiteCustomFunction sQLiteCustomFunction);

    private static native void nativeRegisterLocalizedCollators(long j, String str);

    private static native void nativeResetCancel(long j, boolean z);

    private static native void nativeResetStatementAndClearBindings(long j, long j2);

    private SQLiteConnection(SQLiteConnectionPool pool, SQLiteDatabaseConfiguration configuration, int connectionId, boolean primaryConnection) {
        this(pool, configuration, connectionId, primaryConnection, false);
    }

    private SQLiteConnection(SQLiteConnectionPool pool, SQLiteDatabaseConfiguration configuration, int connectionId, boolean primaryConnection, boolean exclusiveConnection) {
        this.mCloseGuard = CloseGuard.get();
        this.mAuditLog = null;
        this.mPool = pool;
        this.mRecentOperations = new OperationLog(this.mPool);
        this.mConfiguration = new SQLiteDatabaseConfiguration(configuration);
        this.mConnectionId = connectionId;
        this.mIsPrimaryConnection = primaryConnection;
        this.mIsReadOnlyConnection = (configuration.openFlags & 1) == 0 ? false : true;
        this.mPreparedStatementCache = new PreparedStatementCache(this.mConfiguration.maxSqlCacheSize);
        this.mIsExclusiveConnection = exclusiveConnection;
        this.mCloseGuard.open("close");
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
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

    /* access modifiers changed from: package-private */
    public void close() {
        dispose(false);
    }

    /* JADX INFO: finally extract failed */
    private void open() {
        int cookie = this.mRecentOperations.beginOperation("open", null, null);
        try {
            this.mConnectionPtr = nativeOpen(this.mConfiguration.path, this.mConfiguration.openFlags, this.mConfiguration.label, SQLiteDebug.NoPreloadHolder.DEBUG_SQL_STATEMENTS, SQLiteDebug.NoPreloadHolder.DEBUG_SQL_TIME, this.mConfiguration.lookasideSlotSize, this.mConfiguration.lookasideSlotCount);
            this.mRecentOperations.endOperation(cookie);
            setPageSize();
            setForeignKeyModeFromConfiguration();
            setWalModeFromConfiguration();
            setJournalSizeLimit();
            setAutoCheckpointInterval();
            setLocaleFromConfiguration();
            int functionCount = this.mConfiguration.customFunctions.size();
            for (int i = 0; i < functionCount; i++) {
                nativeRegisterCustomFunction(this.mConnectionPtr, this.mConfiguration.customFunctions.get(i));
            }
            if (ENABLE_SQL_AUDIT && this.mAuditLog == null) {
                this.mAuditLog = new SQLiteAuditLog(this.mConfiguration.path, this.mConnectionPtr);
            }
            SQLiteAuditLog sQLiteAuditLog = this.mAuditLog;
            if (sQLiteAuditLog != null) {
                sQLiteAuditLog.enableAudit();
            }
        } catch (Throwable th) {
            this.mRecentOperations.endOperation(cookie);
            throw th;
        }
    }

    private void dispose(boolean finalized) {
        CloseGuard closeGuard = this.mCloseGuard;
        if (closeGuard != null) {
            if (finalized) {
                closeGuard.warnIfOpen();
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
        if (!this.mConfiguration.isInMemoryDb() && !this.mIsReadOnlyConnection) {
            long newValue = (long) SQLiteGlobal.getDefaultPageSize();
            if (executeForLong("PRAGMA page_size", null, null) != newValue) {
                execute("PRAGMA page_size=" + newValue, null, null);
            }
        }
    }

    private void setAutoCheckpointInterval() {
        if (!this.mConfiguration.isInMemoryDb() && !this.mIsReadOnlyConnection) {
            long newValue = (long) SQLiteGlobal.getWALAutoCheckpoint();
            if (executeForLong("PRAGMA wal_autocheckpoint", null, null) != newValue) {
                executeForLong("PRAGMA wal_autocheckpoint=" + newValue, null, null);
            }
        }
    }

    private void setJournalSizeLimit() {
        if (!this.mConfiguration.isInMemoryDb() && !this.mIsReadOnlyConnection) {
            long newValue = (long) SQLiteGlobal.getJournalSizeLimit();
            if (executeForLong("PRAGMA journal_size_limit", null, null) != newValue) {
                executeForLong("PRAGMA journal_size_limit=" + newValue, null, null);
            }
        }
    }

    private void setForeignKeyModeFromConfiguration() {
        if (!this.mIsReadOnlyConnection) {
            long newValue = this.mConfiguration.foreignKeyConstraintsEnabled ? 1 : 0;
            if (executeForLong("PRAGMA foreign_keys", null, null) != newValue) {
                execute("PRAGMA foreign_keys=" + newValue, null, null);
            }
        }
    }

    private void setWalModeFromConfiguration() {
        if (!this.mConfiguration.isInMemoryDb() && !this.mIsReadOnlyConnection) {
            boolean walEnabled = (this.mConfiguration.openFlags & 536870912) != 0;
            boolean isCompatibilityWalEnabled = this.mConfiguration.isLegacyCompatibilityWalEnabled();
            if (walEnabled || isCompatibilityWalEnabled) {
                setJournalMode("WAL");
                if (this.mConfiguration.syncMode != null) {
                    setSyncMode(this.mConfiguration.syncMode);
                } else if (isCompatibilityWalEnabled) {
                    String sWALSyncMode = SQLiteCompatibilityWalFlags.getWALSyncMode();
                    if (!this.mConfiguration.configurationEnhancement || sWALSyncMode.equalsIgnoreCase("NORMAL")) {
                        setSyncMode(sWALSyncMode);
                    } else {
                        setSyncMode("NORMAL");
                    }
                } else {
                    String sWALSyncMode2 = SQLiteGlobal.getWALSyncMode();
                    if (!this.mConfiguration.configurationEnhancement || sWALSyncMode2.equalsIgnoreCase("NORMAL")) {
                        setSyncMode(sWALSyncMode2);
                    } else {
                        setSyncMode("NORMAL");
                    }
                }
                maybeTruncateWalFile();
                return;
            }
            setJournalMode(this.mConfiguration.journalMode == null ? SQLiteGlobal.getDefaultJournalMode() : this.mConfiguration.journalMode);
            setSyncMode(this.mConfiguration.syncMode == null ? SQLiteGlobal.getDefaultSyncMode() : this.mConfiguration.syncMode);
        }
    }

    private void maybeTruncateWalFile() {
        long threshold = SQLiteGlobal.getWALTruncateSize();
        if (threshold != 0) {
            File walFile = new File(this.mConfiguration.path + "-wal");
            if (walFile.isFile()) {
                long size = walFile.length();
                if (size >= threshold) {
                    Log.i(TAG, walFile.getAbsolutePath() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + size + " bytes: Bigger than " + threshold + "; truncating");
                    try {
                        executeForString("PRAGMA wal_checkpoint(TRUNCATE)", null, null);
                    } catch (SQLiteException e) {
                        Log.w(TAG, "Failed to truncate the -wal file", e);
                    }
                }
            }
        }
    }

    private void setSyncMode(String newValue) {
        if (!canonicalizeSyncMode(executeForString("PRAGMA synchronous", null, null)).equalsIgnoreCase(canonicalizeSyncMode(newValue))) {
            execute("PRAGMA synchronous=" + newValue, null, null);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static String canonicalizeSyncMode(String value) {
        char c;
        switch (value.hashCode()) {
            case 48:
                if (value.equals(WifiEnterpriseConfig.ENGINE_DISABLE)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 49:
                if (value.equals("1")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 50:
                if (value.equals(WifiScanLog.EVENT_KEY2)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return "OFF";
        }
        if (c == 1) {
            return "NORMAL";
        }
        if (c != 2) {
            return value;
        }
        return SQLiteGlobal.SYNC_MODE_FULL;
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
            Log.w(TAG, "Could not change the database journal mode of '" + this.mConfiguration.label + "' from '" + value + "' to '" + newValue + "' because the database is locked.  This usually means that there are other open connections to the database which prevents the database from enabling or disabling write-ahead logging mode.  Proceeding without changing the journal mode.");
        }
    }

    private void setLocaleFromConfiguration() {
        String str = "COMMIT";
        if ((this.mConfiguration.openFlags & 16) == 0) {
            String newLocale = this.mConfiguration.locale.toString();
            nativeRegisterLocalizedCollators(this.mConnectionPtr, newLocale);
            if (!this.mConfiguration.isInMemoryDb()) {
                checkDatabaseWiped();
            }
            if (!this.mIsReadOnlyConnection) {
                try {
                    execute("CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT)", null, null);
                    String oldLocale = executeForString("SELECT locale FROM android_metadata UNION SELECT NULL ORDER BY locale DESC LIMIT 1", null, null);
                    if (oldLocale == null || !oldLocale.equals(newLocale)) {
                        execute("BEGIN", null, null);
                        boolean success = false;
                        try {
                            execute("DELETE FROM android_metadata", null, null);
                            execute("INSERT INTO android_metadata (locale) VALUES(?)", new Object[]{newLocale}, null);
                            execute("REINDEX LOCALIZED", null, null);
                            success = true;
                        } finally {
                            if (!success) {
                                str = "ROLLBACK";
                            }
                            execute(str, null, null);
                        }
                    }
                } catch (SQLiteException ex) {
                    throw ex;
                } catch (RuntimeException ex2) {
                    throw new SQLiteException("Failed to change locale for db '" + this.mConfiguration.label + "' to '" + newLocale + "'.", ex2);
                }
            }
        }
    }

    private void checkDatabaseWiped() {
        if (SQLiteGlobal.checkDbWipe()) {
            try {
                File checkFile = new File(this.mConfiguration.path + "-wipecheck");
                boolean hasMetadataTable = executeForLong("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='android_metadata'", null, null) > 0;
                boolean hasCheckFile = checkFile.exists();
                if (!this.mIsReadOnlyConnection && !hasCheckFile) {
                    checkFile.createNewFile();
                }
                if (!hasMetadataTable && hasCheckFile) {
                    SQLiteDatabase.wipeDetected(this.mConfiguration.path, "unknown");
                }
            } catch (IOException | RuntimeException ex) {
                SQLiteDatabase.wtfAsSystemServer(TAG, "Unexpected exception while checking for wipe", ex);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reconfigure(SQLiteDatabaseConfiguration configuration) {
        boolean walModeChanged = false;
        this.mOnlyAllowReadOnlyOperations = false;
        int functionCount = configuration.customFunctions.size();
        for (int i = 0; i < functionCount; i++) {
            SQLiteCustomFunction function = configuration.customFunctions.get(i);
            if (!this.mConfiguration.customFunctions.contains(function)) {
                nativeRegisterCustomFunction(this.mConnectionPtr, function);
            }
        }
        boolean foreignKeyModeChanged = configuration.foreignKeyConstraintsEnabled != this.mConfiguration.foreignKeyConstraintsEnabled;
        if (((configuration.openFlags ^ this.mConfiguration.openFlags) & KeymasterDefs.KM_ULONG_REP) != 0) {
            walModeChanged = true;
        }
        boolean localeChanged = !configuration.locale.equals(this.mConfiguration.locale);
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

    /* access modifiers changed from: package-private */
    public void setOnlyAllowReadOnlyOperations(boolean readOnly) {
        this.mOnlyAllowReadOnlyOperations = readOnly;
    }

    /* access modifiers changed from: package-private */
    public boolean isPreparedStatementInCache(String sql) {
        return this.mPreparedStatementCache.get(sql) != null;
    }

    public int getConnectionId() {
        return this.mConnectionId;
    }

    public boolean isPrimaryConnection() {
        return this.mIsPrimaryConnection;
    }

    public void prepare(String sql, SQLiteStatementInfo outStatementInfo) {
        if (sql != null) {
            int cookie = this.mRecentOperations.beginOperation("prepare", sql, null);
            try {
                PreparedStatement statement = acquirePreparedStatement(sql);
                if (outStatementInfo != null) {
                    try {
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
                    } catch (Throwable th) {
                        releasePreparedStatement(statement);
                        throw th;
                    }
                }
                releasePreparedStatement(statement);
                this.mRecentOperations.endOperation(cookie);
            } catch (RuntimeException ex) {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th2) {
                this.mRecentOperations.endOperation(cookie);
                throw th2;
            }
        } else {
            throw new IllegalArgumentException("sql must not be null.");
        }
    }

    /* JADX INFO: finally extract failed */
    public void execute(String sql, Object[] bindArgs, CancellationSignal cancellationSignal) {
        if (sql != null) {
            int cookie = this.mRecentOperations.beginOperation("execute", sql, bindArgs);
            try {
                PreparedStatement statement = acquirePreparedStatement(sql);
                try {
                    throwIfStatementForbidden(statement);
                    bindArguments(statement, bindArgs);
                    applyBlockGuardPolicy(statement);
                    attachCancellationSignal(cancellationSignal);
                    try {
                        nativeExecute(this.mConnectionPtr, statement.mStatementPtr);
                        detachCancellationSignal(cancellationSignal);
                        releasePreparedStatement(statement);
                        this.mRecentOperations.endOperation(cookie);
                    } catch (Throwable th) {
                        detachCancellationSignal(cancellationSignal);
                        throw th;
                    }
                } catch (Throwable th2) {
                    releasePreparedStatement(statement);
                    throw th2;
                }
            } catch (RuntimeException ex) {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th3) {
                this.mRecentOperations.endOperation(cookie);
                throw th3;
            }
        } else {
            throw new IllegalArgumentException("sql must not be null.");
        }
    }

    /* JADX INFO: finally extract failed */
    public long executeForLong(String sql, Object[] bindArgs, CancellationSignal cancellationSignal) {
        if (sql != null) {
            int cookie = this.mRecentOperations.beginOperation("executeForLong", sql, bindArgs);
            try {
                PreparedStatement statement = acquirePreparedStatement(sql);
                try {
                    throwIfStatementForbidden(statement);
                    bindArguments(statement, bindArgs);
                    applyBlockGuardPolicy(statement);
                    attachCancellationSignal(cancellationSignal);
                    try {
                        long ret = nativeExecuteForLong(this.mConnectionPtr, statement.mStatementPtr);
                        this.mRecentOperations.setResult(ret);
                        detachCancellationSignal(cancellationSignal);
                        releasePreparedStatement(statement);
                        this.mRecentOperations.endOperation(cookie);
                        return ret;
                    } catch (Throwable th) {
                        detachCancellationSignal(cancellationSignal);
                        throw th;
                    }
                } catch (Throwable th2) {
                    releasePreparedStatement(statement);
                    throw th2;
                }
            } catch (RuntimeException ex) {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th3) {
                this.mRecentOperations.endOperation(cookie);
                throw th3;
            }
        } else {
            throw new IllegalArgumentException("sql must not be null.");
        }
    }

    /* JADX INFO: finally extract failed */
    public String executeForString(String sql, Object[] bindArgs, CancellationSignal cancellationSignal) {
        if (sql != null) {
            int cookie = this.mRecentOperations.beginOperation("executeForString", sql, bindArgs);
            try {
                PreparedStatement statement = acquirePreparedStatement(sql);
                try {
                    throwIfStatementForbidden(statement);
                    bindArguments(statement, bindArgs);
                    applyBlockGuardPolicy(statement);
                    attachCancellationSignal(cancellationSignal);
                    try {
                        String ret = nativeExecuteForString(this.mConnectionPtr, statement.mStatementPtr);
                        this.mRecentOperations.setResult(ret);
                        detachCancellationSignal(cancellationSignal);
                        releasePreparedStatement(statement);
                        this.mRecentOperations.endOperation(cookie);
                        return ret;
                    } catch (Throwable th) {
                        detachCancellationSignal(cancellationSignal);
                        throw th;
                    }
                } catch (Throwable th2) {
                    releasePreparedStatement(statement);
                    throw th2;
                }
            } catch (RuntimeException ex) {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th3) {
                this.mRecentOperations.endOperation(cookie);
                throw th3;
            }
        } else {
            throw new IllegalArgumentException("sql must not be null.");
        }
    }

    /* JADX INFO: finally extract failed */
    public ParcelFileDescriptor executeForBlobFileDescriptor(String sql, Object[] bindArgs, CancellationSignal cancellationSignal) {
        if (sql != null) {
            int cookie = this.mRecentOperations.beginOperation("executeForBlobFileDescriptor", sql, bindArgs);
            try {
                PreparedStatement statement = acquirePreparedStatement(sql);
                try {
                    throwIfStatementForbidden(statement);
                    bindArguments(statement, bindArgs);
                    applyBlockGuardPolicy(statement);
                    attachCancellationSignal(cancellationSignal);
                    try {
                        int fd = nativeExecuteForBlobFileDescriptor(this.mConnectionPtr, statement.mStatementPtr);
                        ParcelFileDescriptor adoptFd = fd >= 0 ? ParcelFileDescriptor.adoptFd(fd) : null;
                        detachCancellationSignal(cancellationSignal);
                        releasePreparedStatement(statement);
                        this.mRecentOperations.endOperation(cookie);
                        return adoptFd;
                    } catch (Throwable th) {
                        detachCancellationSignal(cancellationSignal);
                        throw th;
                    }
                } catch (Throwable th2) {
                    releasePreparedStatement(statement);
                    throw th2;
                }
            } catch (RuntimeException ex) {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th3) {
                this.mRecentOperations.endOperation(cookie);
                throw th3;
            }
        } else {
            throw new IllegalArgumentException("sql must not be null.");
        }
    }

    /* JADX INFO: finally extract failed */
    public int executeForChangedRowCount(String sql, Object[] bindArgs, CancellationSignal cancellationSignal) {
        if (sql != null) {
            int cookie = this.mRecentOperations.beginOperation("executeForChangedRowCount", sql, bindArgs);
            try {
                PreparedStatement statement = acquirePreparedStatement(sql);
                try {
                    throwIfStatementForbidden(statement);
                    bindArguments(statement, bindArgs);
                    applyBlockGuardPolicy(statement);
                    attachCancellationSignal(cancellationSignal);
                    try {
                        int changedRows = nativeExecuteForChangedRowCount(this.mConnectionPtr, statement.mStatementPtr);
                        detachCancellationSignal(cancellationSignal);
                        releasePreparedStatement(statement);
                        if (this.mRecentOperations.endOperationDeferLog(cookie)) {
                            OperationLog operationLog = this.mRecentOperations;
                            operationLog.logOperation(cookie, "changedRows=" + changedRows);
                        }
                        return changedRows;
                    } catch (Throwable th) {
                        detachCancellationSignal(cancellationSignal);
                        throw th;
                    }
                } catch (Throwable th2) {
                    releasePreparedStatement(statement);
                    throw th2;
                }
            } catch (RuntimeException ex) {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th3) {
                if (this.mRecentOperations.endOperationDeferLog(cookie)) {
                    OperationLog operationLog2 = this.mRecentOperations;
                    operationLog2.logOperation(cookie, "changedRows=0");
                }
                throw th3;
            }
        } else {
            throw new IllegalArgumentException("sql must not be null.");
        }
    }

    /* JADX INFO: finally extract failed */
    public long executeForLastInsertedRowId(String sql, Object[] bindArgs, CancellationSignal cancellationSignal) {
        if (sql != null) {
            int cookie = this.mRecentOperations.beginOperation("executeForLastInsertedRowId", sql, bindArgs);
            try {
                PreparedStatement statement = acquirePreparedStatement(sql);
                try {
                    throwIfStatementForbidden(statement);
                    bindArguments(statement, bindArgs);
                    applyBlockGuardPolicy(statement);
                    attachCancellationSignal(cancellationSignal);
                    try {
                        long nativeExecuteForLastInsertedRowId = nativeExecuteForLastInsertedRowId(this.mConnectionPtr, statement.mStatementPtr);
                        detachCancellationSignal(cancellationSignal);
                        releasePreparedStatement(statement);
                        this.mRecentOperations.endOperation(cookie);
                        return nativeExecuteForLastInsertedRowId;
                    } catch (Throwable th) {
                        detachCancellationSignal(cancellationSignal);
                        throw th;
                    }
                } catch (Throwable th2) {
                    releasePreparedStatement(statement);
                    throw th2;
                }
            } catch (RuntimeException ex) {
                this.mRecentOperations.failOperation(cookie, ex);
                throw ex;
            } catch (Throwable th3) {
                this.mRecentOperations.endOperation(cookie);
                throw th3;
            }
        } else {
            throw new IllegalArgumentException("sql must not be null.");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:80:0x0176  */
    public int executeForCursorWindow(String sql, Object[] bindArgs, CursorWindow window, int startPos, int requiredPos, boolean countAllRows, CancellationSignal cancellationSignal) {
        Throwable th;
        String str;
        int cookie;
        int i;
        int filledRows;
        int countedRows;
        int actualPos;
        String str2;
        String str3;
        String str4;
        String str5;
        RuntimeException ex;
        RuntimeException ex2;
        PreparedStatement statement;
        Throwable th2;
        Throwable th3;
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (window != null) {
            window.acquireReference();
            int actualPos2 = -1;
            int countedRows2 = -1;
            int filledRows2 = -1;
            try {
                int cookie2 = this.mRecentOperations.beginOperation("executeForCursorWindow", sql, bindArgs);
                try {
                    PreparedStatement statement2 = acquirePreparedStatement(sql);
                    try {
                        throwIfStatementForbidden(statement2);
                        bindArguments(statement2, bindArgs);
                        applyBlockGuardPolicy(statement2);
                        attachCancellationSignal(cancellationSignal);
                        try {
                            try {
                                statement = statement2;
                                str4 = "', startPos=";
                                str5 = "window='";
                                try {
                                    long result = nativeExecuteForCursorWindow(this.mConnectionPtr, statement2.mStatementPtr, window.mWindowPtr, startPos, requiredPos, countAllRows);
                                    actualPos = (int) (result >> 32);
                                    countedRows = (int) result;
                                } catch (Throwable th4) {
                                    th3 = th4;
                                    i = startPos;
                                    cookie = cookie2;
                                    str = ", actualPos=";
                                    str3 = ", filledRows=";
                                    str2 = ", countedRows=";
                                    try {
                                        detachCancellationSignal(cancellationSignal);
                                        throw th3;
                                    } catch (Throwable th5) {
                                        th2 = th5;
                                        try {
                                            releasePreparedStatement(statement);
                                            throw th2;
                                        } catch (RuntimeException e) {
                                            ex2 = e;
                                            try {
                                                this.mRecentOperations.failOperation(cookie, ex2);
                                                throw ex2;
                                            } catch (Throwable th6) {
                                                ex = th6;
                                                actualPos = actualPos2;
                                                countedRows = countedRows2;
                                                filledRows = filledRows2;
                                                if (this.mRecentOperations.endOperationDeferLog(cookie)) {
                                                    this.mRecentOperations.logOperation(cookie, str5 + window + str4 + i + str + actualPos + str3 + filledRows + str2 + countedRows);
                                                }
                                                throw ex;
                                            }
                                        }
                                    }
                                }
                                try {
                                    filledRows = window.getNumRows();
                                    try {
                                        window.setStartPosition(actualPos);
                                        try {
                                            detachCancellationSignal(cancellationSignal);
                                            try {
                                                releasePreparedStatement(statement);
                                                try {
                                                    if (this.mRecentOperations.endOperationDeferLog(cookie2)) {
                                                        OperationLog operationLog = this.mRecentOperations;
                                                        StringBuilder sb = new StringBuilder();
                                                        sb.append(str5);
                                                        sb.append(window);
                                                        sb.append(str4);
                                                        try {
                                                            sb.append(startPos);
                                                            sb.append(", actualPos=");
                                                            sb.append(actualPos);
                                                            sb.append(", filledRows=");
                                                            sb.append(filledRows);
                                                            sb.append(", countedRows=");
                                                            sb.append(countedRows);
                                                            operationLog.logOperation(cookie2, sb.toString());
                                                        } catch (Throwable th7) {
                                                            th = th7;
                                                        }
                                                    }
                                                    window.releaseReference();
                                                    return countedRows;
                                                } catch (Throwable th8) {
                                                    th = th8;
                                                    window.releaseReference();
                                                    throw th;
                                                }
                                            } catch (RuntimeException e2) {
                                                ex2 = e2;
                                                i = startPos;
                                                cookie = cookie2;
                                                str = ", actualPos=";
                                                str3 = ", filledRows=";
                                                str2 = ", countedRows=";
                                                actualPos2 = actualPos;
                                                countedRows2 = countedRows;
                                                filledRows2 = filledRows;
                                                this.mRecentOperations.failOperation(cookie, ex2);
                                                throw ex2;
                                            } catch (Throwable th9) {
                                                ex = th9;
                                                i = startPos;
                                                cookie = cookie2;
                                                str = ", actualPos=";
                                                str3 = ", filledRows=";
                                                str2 = ", countedRows=";
                                                if (this.mRecentOperations.endOperationDeferLog(cookie)) {
                                                }
                                                throw ex;
                                            }
                                        } catch (Throwable th10) {
                                            th2 = th10;
                                            i = startPos;
                                            cookie = cookie2;
                                            str = ", actualPos=";
                                            str3 = ", filledRows=";
                                            str2 = ", countedRows=";
                                            actualPos2 = actualPos;
                                            countedRows2 = countedRows;
                                            filledRows2 = filledRows;
                                            releasePreparedStatement(statement);
                                            throw th2;
                                        }
                                    } catch (Throwable th11) {
                                        th3 = th11;
                                        i = startPos;
                                        cookie = cookie2;
                                        str = ", actualPos=";
                                        str3 = ", filledRows=";
                                        str2 = ", countedRows=";
                                        actualPos2 = actualPos;
                                        countedRows2 = countedRows;
                                        filledRows2 = filledRows;
                                    }
                                } catch (Throwable th12) {
                                    th3 = th12;
                                    i = startPos;
                                    cookie = cookie2;
                                    str = ", actualPos=";
                                    str3 = ", filledRows=";
                                    str2 = ", countedRows=";
                                    actualPos2 = actualPos;
                                    countedRows2 = countedRows;
                                    detachCancellationSignal(cancellationSignal);
                                    throw th3;
                                }
                            } catch (Throwable th13) {
                                th3 = th13;
                                str3 = ", filledRows=";
                                str2 = ", countedRows=";
                                i = startPos;
                                str4 = "', startPos=";
                                str5 = "window='";
                                cookie = cookie2;
                                statement = statement2;
                                str = ", actualPos=";
                                detachCancellationSignal(cancellationSignal);
                                throw th3;
                            }
                        } catch (Throwable th14) {
                            th3 = th14;
                            str4 = "', startPos=";
                            str5 = "window='";
                            str3 = ", filledRows=";
                            str2 = ", countedRows=";
                            i = startPos;
                            cookie = cookie2;
                            statement = statement2;
                            str = ", actualPos=";
                            detachCancellationSignal(cancellationSignal);
                            throw th3;
                        }
                    } catch (Throwable th15) {
                        th2 = th15;
                        str4 = "', startPos=";
                        str5 = "window='";
                        str3 = ", filledRows=";
                        str2 = ", countedRows=";
                        i = startPos;
                        cookie = cookie2;
                        statement = statement2;
                        str = ", actualPos=";
                        releasePreparedStatement(statement);
                        throw th2;
                    }
                } catch (RuntimeException e3) {
                    ex2 = e3;
                    str4 = "', startPos=";
                    str5 = "window='";
                    str3 = ", filledRows=";
                    str2 = ", countedRows=";
                    i = startPos;
                    cookie = cookie2;
                    str = ", actualPos=";
                    this.mRecentOperations.failOperation(cookie, ex2);
                    throw ex2;
                } catch (Throwable th16) {
                    ex = th16;
                    str4 = "', startPos=";
                    str5 = "window='";
                    str3 = ", filledRows=";
                    str2 = ", countedRows=";
                    i = startPos;
                    cookie = cookie2;
                    str = ", actualPos=";
                    actualPos = -1;
                    countedRows = -1;
                    filledRows = -1;
                    if (this.mRecentOperations.endOperationDeferLog(cookie)) {
                    }
                    throw ex;
                }
            } catch (Throwable th17) {
                th = th17;
                window.releaseReference();
                throw th;
            }
        } else {
            throw new IllegalArgumentException("window must not be null.");
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
            PreparedStatement statement2 = obtainPreparedStatement(sql, statementPtr, numParameters, type, nativeIsReadOnly(this.mConnectionPtr, statementPtr));
            if (!skipCache && isCacheable(type)) {
                this.mPreparedStatementCache.put(sql, statement2);
                statement2.mInCache = true;
            }
            statement2.mInUse = true;
            return statement2;
        } catch (RuntimeException ex) {
            if (statement == null || !statement.mInCache) {
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
            } catch (SQLiteException e) {
                this.mPreparedStatementCache.remove(statement.mSql);
            }
        } else {
            finalizePreparedStatement(statement);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        if (cancellationSignal != null) {
            this.mCancellationSignalAttachCount--;
            if (this.mCancellationSignalAttachCount == 0) {
                cancellationSignal.setOnCancelListener(null);
                nativeResetCancel(this.mConnectionPtr, false);
            }
        }
    }

    @Override // android.os.CancellationSignal.OnCancelListener
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
                int typeOfObject = DatabaseUtils.getTypeOfObject(arg);
                if (typeOfObject == 0) {
                    nativeBindNull(this.mConnectionPtr, statementPtr, i + 1);
                } else if (typeOfObject == 1) {
                    nativeBindLong(this.mConnectionPtr, statementPtr, i + 1, ((Number) arg).longValue());
                } else if (typeOfObject == 2) {
                    nativeBindDouble(this.mConnectionPtr, statementPtr, i + 1, ((Number) arg).doubleValue());
                } else if (typeOfObject == 4) {
                    nativeBindBlob(this.mConnectionPtr, statementPtr, i + 1, (byte[]) arg);
                } else if (arg instanceof Boolean) {
                    nativeBindLong(this.mConnectionPtr, statementPtr, i + 1, ((Boolean) arg).booleanValue() ? 1 : 0);
                } else {
                    nativeBindString(this.mConnectionPtr, statementPtr, i + 1, arg.toString());
                }
            }
        }
    }

    private void throwIfStatementForbidden(PreparedStatement statement) {
        if (this.mOnlyAllowReadOnlyOperations && !statement.mReadOnly) {
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
        if (this.mConfiguration.isInMemoryDb()) {
            return;
        }
        if (statement.mReadOnly) {
            BlockGuard.getThreadPolicy().onReadFromDisk();
        } else {
            BlockGuard.getThreadPolicy().onWriteToDisk();
        }
    }

    public void dump(Printer printer, boolean verbose) {
        dumpUnsafe(printer, verbose);
    }

    /* access modifiers changed from: package-private */
    public void dumpUnsafe(Printer printer, boolean verbose) {
        printer.println("Connection #" + this.mConnectionId + SettingsStringUtil.DELIMITER);
        if (verbose) {
            printer.println("  connectionPtr: 0x" + Long.toHexString(this.mConnectionPtr));
        }
        printer.println("  isPrimaryConnection: " + this.mIsPrimaryConnection);
        printer.println("  onlyAllowReadOnlyOperations: " + this.mOnlyAllowReadOnlyOperations);
        this.mRecentOperations.dump(printer);
    }

    /* access modifiers changed from: package-private */
    public String describeCurrentOperationUnsafe() {
        return this.mRecentOperations.describeCurrentOperation();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00e6, code lost:
        r0 = th;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00e6 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:12:0x0057] */
    public void collectDbStats(ArrayList<SQLiteDebug.DbStats> dbStatsList) {
        long pageSize;
        long pageCount;
        CursorWindow window;
        String name;
        String path;
        long pageCount2;
        long pageSize2;
        int lookaside = nativeGetDbLookaside(this.mConnectionPtr);
        long pageCount3 = 0;
        try {
            pageCount3 = executeForLong("PRAGMA page_count;", null, null);
            pageCount = pageCount3;
            pageSize = executeForLong("PRAGMA page_size;", null, null);
        } catch (SQLiteException e) {
            pageCount = pageCount3;
            pageSize = 0;
        }
        dbStatsList.add(getMainDbStatsUnsafe(lookaside, pageCount, pageSize));
        CursorWindow window2 = new CursorWindow("collectDbStats");
        CursorWindow window3 = window2;
        try {
            executeForCursorWindow("PRAGMA database_list;", null, window2, 0, 0, false, null);
            int i = 1;
            while (i < window3.getNumRows()) {
                window = window3;
                try {
                    name = window.getString(i, 1);
                    path = window.getString(i, 2);
                    pageCount2 = 0;
                    pageSize2 = 0;
                    pageCount2 = executeForLong("PRAGMA " + name + ".page_count;", null, null);
                    pageSize2 = executeForLong("PRAGMA " + name + ".page_size;", null, null);
                } catch (SQLiteException e2) {
                } catch (Throwable th) {
                }
                String label = "  (attached) " + name;
                if (!path.isEmpty()) {
                    label = label + ": " + path;
                }
                dbStatsList.add(new SQLiteDebug.DbStats(label, pageCount2, pageSize2, 0, 0, 0, 0));
                i++;
                window3 = window;
            }
            window = window3;
        } catch (SQLiteException e3) {
            window = window3;
        } catch (Throwable th2) {
            Throwable th3 = th2;
            window = window3;
            window.close();
            throw th3;
        }
        window.close();
    }

    /* access modifiers changed from: package-private */
    public void collectDbStatsUnsafe(ArrayList<SQLiteDebug.DbStats> dbStatsList) {
        dbStatsList.add(getMainDbStatsUnsafe(0, 0, 0));
    }

    private SQLiteDebug.DbStats getMainDbStatsUnsafe(int lookaside, long pageCount, long pageSize) {
        String label = this.mConfiguration.path;
        if (!this.mIsPrimaryConnection) {
            label = label + " (" + this.mConnectionId + ")";
        }
        return new SQLiteDebug.DbStats(label, pageCount, pageSize, lookaside, this.mPreparedStatementCache.hitCount(), this.mPreparedStatementCache.missCount(), this.mPreparedStatementCache.size());
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

    /* access modifiers changed from: private */
    public static String trimSqlForDisplay(String sql) {
        return sql.replaceAll("[\\s]*\\n+[\\s]*", WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
    }

    /* access modifiers changed from: private */
    public static final class PreparedStatement {
        public boolean mInCache;
        public boolean mInUse;
        public int mNumParameters;
        public PreparedStatement mPoolNext;
        public boolean mReadOnly;
        public String mSql;
        public long mStatementPtr;
        public int mType;

        private PreparedStatement() {
        }
    }

    /* access modifiers changed from: private */
    public final class PreparedStatementCache extends LruCache<String, PreparedStatement> {
        public PreparedStatementCache(int size) {
            super(size);
        }

        /* access modifiers changed from: protected */
        public void entryRemoved(boolean evicted, String key, PreparedStatement oldValue, PreparedStatement newValue) {
            oldValue.mInCache = false;
            if (!oldValue.mInUse) {
                SQLiteConnection.this.finalizePreparedStatement(oldValue);
            }
        }

        public void dump(Printer printer) {
            printer.println("  Prepared statement cache:");
            Map<String, PreparedStatement> cache = snapshot();
            if (!cache.isEmpty()) {
                int i = 0;
                for (Map.Entry<String, PreparedStatement> entry : cache.entrySet()) {
                    PreparedStatement statement = entry.getValue();
                    if (statement.mInCache) {
                        printer.println("    " + i + ": statementPtr=0x" + Long.toHexString(statement.mStatementPtr) + ", numParameters=" + statement.mNumParameters + ", type=" + statement.mType + ", readOnly=" + statement.mReadOnly + ", sql=\"" + SQLiteConnection.trimSqlForDisplay(entry.getKey()) + "\"");
                    }
                    i++;
                }
                return;
            }
            printer.println("    <none>");
        }
    }

    /* access modifiers changed from: private */
    public static final class OperationLog {
        private static final int COOKIE_GENERATION_SHIFT = 8;
        private static final int COOKIE_INDEX_MASK = 255;
        private static final int MAX_RECENT_OPERATIONS = 20;
        private int mGeneration;
        private int mIndex;
        private final Operation[] mOperations = new Operation[20];
        private final SQLiteConnectionPool mPool;
        private long mResultLong = Long.MIN_VALUE;
        private String mResultString;

        OperationLog(SQLiteConnectionPool pool) {
            this.mPool = pool;
        }

        public int beginOperation(String kind, String sql, Object[] bindArgs) {
            int i;
            this.mResultLong = Long.MIN_VALUE;
            this.mResultString = null;
            synchronized (this.mOperations) {
                int index = (this.mIndex + 1) % 20;
                Operation operation = this.mOperations[index];
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
                operation.mPath = this.mPool.getPath();
                operation.mResultLong = Long.MIN_VALUE;
                operation.mResultString = null;
                if (bindArgs != null) {
                    if (operation.mBindArgs == null) {
                        operation.mBindArgs = new ArrayList<>();
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
                i = operation.mCookie;
            }
            return i;
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

        public void setResult(long longResult) {
            this.mResultLong = longResult;
        }

        public void setResult(String stringResult) {
            this.mResultString = stringResult;
        }

        private boolean endOperationDeferLogLocked(int cookie) {
            Operation operation = getOperationLocked(cookie);
            if (operation == null) {
                return false;
            }
            if (Trace.isTagEnabled(1048576)) {
                Trace.asyncTraceEnd(1048576, operation.getTraceMethodName(), operation.mCookie);
            }
            operation.mEndTime = SystemClock.uptimeMillis();
            operation.mFinished = true;
            long execTime = operation.mEndTime - operation.mStartTime;
            this.mPool.onStatementExecuted(execTime);
            if (!SQLiteDebug.NoPreloadHolder.DEBUG_LOG_SLOW_QUERIES || !SQLiteDebug.shouldLogSlowQuery(execTime)) {
                return false;
            }
            return true;
        }

        private void logOperationLocked(int cookie, String detail) {
            Operation operation = getOperationLocked(cookie);
            operation.mResultLong = this.mResultLong;
            operation.mResultString = this.mResultString;
            StringBuilder msg = new StringBuilder();
            operation.describe(msg, true);
            if (detail != null) {
                msg.append(", ");
                msg.append(detail);
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
            if (operation.mCookie == cookie) {
                return operation;
            }
            return null;
        }

        public String describeCurrentOperation() {
            synchronized (this.mOperations) {
                Operation operation = this.mOperations[this.mIndex];
                if (operation == null || operation.mFinished) {
                    return null;
                }
                StringBuilder msg = new StringBuilder();
                operation.describe(msg, false);
                return msg.toString();
            }
        }

        public void dump(Printer printer) {
            synchronized (this.mOperations) {
                printer.println("  Most recently executed operations:");
                int index = this.mIndex;
                Operation operation = this.mOperations[index];
                if (operation != null) {
                    SimpleDateFormat opDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    int n = 0;
                    do {
                        StringBuilder msg = new StringBuilder();
                        msg.append("    ");
                        msg.append(n);
                        msg.append(": [");
                        msg.append(opDF.format(new Date(operation.mStartWallTime)));
                        msg.append("] ");
                        operation.describe(msg, false);
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

    /* access modifiers changed from: private */
    public static final class Operation {
        private static final int MAX_TRACE_METHOD_NAME_LEN = 256;
        public ArrayList<Object> mBindArgs;
        public int mCookie;
        public long mEndTime;
        public Exception mException;
        public boolean mFinished;
        public String mKind;
        public String mPath;
        public long mResultLong;
        public String mResultString;
        public String mSql;
        public long mStartTime;
        public long mStartWallTime;

        private Operation() {
        }

        public void describe(StringBuilder msg, boolean allowDetailedLog) {
            ArrayList<Object> arrayList;
            msg.append(this.mKind);
            if (this.mFinished) {
                msg.append(" took ");
                msg.append(this.mEndTime - this.mStartTime);
                msg.append("ms");
            } else {
                msg.append(" started ");
                msg.append(System.currentTimeMillis() - this.mStartWallTime);
                msg.append("ms ago");
            }
            msg.append(" - ");
            msg.append(getStatus());
            if (this.mSql != null) {
                msg.append(", sql=\"");
                msg.append(SQLiteConnection.trimSqlForDisplay(this.mSql));
                msg.append("\"");
            }
            if (allowDetailedLog && SQLiteDebug.NoPreloadHolder.DEBUG_LOG_DETAILED && (arrayList = this.mBindArgs) != null && arrayList.size() != 0) {
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
                        msg.append("\"");
                        msg.append((String) arg);
                        msg.append("\"");
                    } else {
                        msg.append(arg);
                    }
                }
                msg.append("]");
            }
            msg.append(", path=");
            msg.append(this.mPath);
            if (this.mException != null) {
                msg.append(", exception=\"");
                msg.append(this.mException.getMessage());
                msg.append("\"");
            }
            if (this.mResultLong != Long.MIN_VALUE) {
                msg.append(", result=");
                msg.append(this.mResultLong);
            }
            if (this.mResultString != null) {
                msg.append(", result=\"");
                msg.append(this.mResultString);
                msg.append("\"");
            }
        }

        private String getStatus() {
            if (!this.mFinished) {
                return "running";
            }
            return this.mException != null ? "failed" : "succeeded";
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getTraceMethodName() {
            String methodName = this.mKind + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mSql;
            if (methodName.length() > 256) {
                return methodName.substring(0, 256);
            }
            return methodName;
        }
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
