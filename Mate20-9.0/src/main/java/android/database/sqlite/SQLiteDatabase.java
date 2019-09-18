package android.database.sqlite;

import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.DefaultDatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabaseEx;
import android.database.sqlite.SQLiteDebug;
import android.os.CancellationSignal;
import android.os.Looper;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.util.Pair;
import android.util.Printer;
import com.android.internal.util.Preconditions;
import com.huawei.indexsearch.IIndexSearchParser;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.FileFilter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public final class SQLiteDatabase extends SQLiteClosable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int CONFLICT_ABORT = 2;
    public static final int CONFLICT_FAIL = 3;
    public static final int CONFLICT_IGNORE = 4;
    public static final int CONFLICT_NONE = 0;
    public static final int CONFLICT_REPLACE = 5;
    public static final int CONFLICT_ROLLBACK = 1;
    private static final String[] CONFLICT_VALUES = {"", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE "};
    public static final int CREATE_IF_NECESSARY = 268435456;
    private static final boolean DEBUG_CLOSE_IDLE_CONNECTIONS = SystemProperties.getBoolean("persist.debug.sqlite.close_idle_connections", false);
    public static final int DISABLE_COMPATIBILITY_WAL = 1073741824;
    public static final int ENABLE_WRITE_AHEAD_LOGGING = 536870912;
    private static final int EVENT_DB_CORRUPT = 75004;
    public static final int MAX_SQL_CACHE_SIZE = 100;
    public static final int NO_LOCALIZED_COLLATORS = 16;
    public static final int OPEN_READONLY = 1;
    public static final int OPEN_READWRITE = 0;
    private static final int OPEN_READ_MASK = 1;
    public static final int SQLITE_MAX_LIKE_PATTERN_LENGTH = 50000;
    private static final String TAG = "SQLiteDatabase";
    private static WeakHashMap<SQLiteDatabase, Object> sActiveDatabases = new WeakHashMap<>();
    private boolean bJankMonitor = true;
    private String jank_dbname = "";
    private final CloseGuard mCloseGuardLocked = CloseGuard.get();
    private final SQLiteDatabaseConfiguration mConfigurationLocked;
    private SQLiteDatabaseEx.DatabaseConnectionExclusiveHandler mConnectionExclusiveHandler;
    private SQLiteConnectionPool mConnectionPoolLocked;
    private final CursorFactory mCursorFactory;
    private boolean mEnableExclusiveConnection = false;
    private final DatabaseErrorHandler mErrorHandler;
    private boolean mHasAttachedDbsLocked;
    private IIndexSearchParser mIndexSearchParser = null;
    private JankSqlite mJankDBStats = new JankSqlite();
    private final Object mLock = new Object();
    private final ThreadLocal<SQLiteSession> mThreadSession = ThreadLocal.withInitial(new Supplier() {
        public final Object get() {
            return SQLiteDatabase.this.createSession();
        }
    });

    public interface CursorFactory {
        Cursor newCursor(SQLiteDatabase sQLiteDatabase, SQLiteCursorDriver sQLiteCursorDriver, String str, SQLiteQuery sQLiteQuery);
    }

    public interface CustomFunction {
        void callback(String[] strArr);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DatabaseOpenFlags {
    }

    public static final class OpenParams {
        /* access modifiers changed from: private */
        public final CursorFactory mCursorFactory;
        /* access modifiers changed from: private */
        public final DatabaseErrorHandler mErrorHandler;
        /* access modifiers changed from: private */
        public final long mIdleConnectionTimeout;
        /* access modifiers changed from: private */
        public final String mJournalMode;
        /* access modifiers changed from: private */
        public final int mLookasideSlotCount;
        /* access modifiers changed from: private */
        public final int mLookasideSlotSize;
        /* access modifiers changed from: private */
        public final int mOpenFlags;
        /* access modifiers changed from: private */
        public final String mSyncMode;

        public static final class Builder {
            private CursorFactory mCursorFactory;
            private DatabaseErrorHandler mErrorHandler;
            private long mIdleConnectionTimeout = -1;
            private String mJournalMode;
            private int mLookasideSlotCount = -1;
            private int mLookasideSlotSize = -1;
            private int mOpenFlags;
            private String mSyncMode;

            public Builder() {
            }

            public Builder(OpenParams params) {
                this.mLookasideSlotSize = params.mLookasideSlotSize;
                this.mLookasideSlotCount = params.mLookasideSlotCount;
                this.mOpenFlags = params.mOpenFlags;
                this.mCursorFactory = params.mCursorFactory;
                this.mErrorHandler = params.mErrorHandler;
                this.mJournalMode = params.mJournalMode;
                this.mSyncMode = params.mSyncMode;
            }

            public Builder setLookasideConfig(int slotSize, int slotCount) {
                boolean z = false;
                Preconditions.checkArgument(slotSize >= 0, "lookasideSlotCount cannot be negative");
                Preconditions.checkArgument(slotCount >= 0, "lookasideSlotSize cannot be negative");
                if ((slotSize > 0 && slotCount > 0) || (slotCount == 0 && slotSize == 0)) {
                    z = true;
                }
                Preconditions.checkArgument(z, "Invalid configuration: " + slotSize + ", " + slotCount);
                this.mLookasideSlotSize = slotSize;
                this.mLookasideSlotCount = slotCount;
                return this;
            }

            public boolean isWriteAheadLoggingEnabled() {
                return (this.mOpenFlags & 536870912) != 0;
            }

            public Builder setOpenFlags(int openFlags) {
                this.mOpenFlags = openFlags;
                return this;
            }

            public Builder addOpenFlags(int openFlags) {
                this.mOpenFlags |= openFlags;
                return this;
            }

            public Builder removeOpenFlags(int openFlags) {
                this.mOpenFlags &= ~openFlags;
                return this;
            }

            public void setWriteAheadLoggingEnabled(boolean enabled) {
                if (enabled) {
                    addOpenFlags(536870912);
                } else {
                    removeOpenFlags(536870912);
                }
            }

            public Builder setCursorFactory(CursorFactory cursorFactory) {
                this.mCursorFactory = cursorFactory;
                return this;
            }

            public Builder setErrorHandler(DatabaseErrorHandler errorHandler) {
                this.mErrorHandler = errorHandler;
                return this;
            }

            public Builder setIdleConnectionTimeout(long idleConnectionTimeoutMs) {
                Preconditions.checkArgument(idleConnectionTimeoutMs >= 0, "idle connection timeout cannot be negative");
                this.mIdleConnectionTimeout = idleConnectionTimeoutMs;
                return this;
            }

            public Builder setJournalMode(String journalMode) {
                Preconditions.checkNotNull(journalMode);
                this.mJournalMode = journalMode;
                return this;
            }

            public Builder setSynchronousMode(String syncMode) {
                Preconditions.checkNotNull(syncMode);
                this.mSyncMode = syncMode;
                return this;
            }

            public OpenParams build() {
                OpenParams openParams = new OpenParams(this.mOpenFlags, this.mCursorFactory, this.mErrorHandler, this.mLookasideSlotSize, this.mLookasideSlotCount, this.mIdleConnectionTimeout, this.mJournalMode, this.mSyncMode);
                return openParams;
            }
        }

        private OpenParams(int openFlags, CursorFactory cursorFactory, DatabaseErrorHandler errorHandler, int lookasideSlotSize, int lookasideSlotCount, long idleConnectionTimeout, String journalMode, String syncMode) {
            this.mOpenFlags = openFlags;
            this.mCursorFactory = cursorFactory;
            this.mErrorHandler = errorHandler;
            this.mLookasideSlotSize = lookasideSlotSize;
            this.mLookasideSlotCount = lookasideSlotCount;
            this.mIdleConnectionTimeout = idleConnectionTimeout;
            this.mJournalMode = journalMode;
            this.mSyncMode = syncMode;
        }

        public int getLookasideSlotSize() {
            return this.mLookasideSlotSize;
        }

        public int getLookasideSlotCount() {
            return this.mLookasideSlotCount;
        }

        public int getOpenFlags() {
            return this.mOpenFlags;
        }

        public CursorFactory getCursorFactory() {
            return this.mCursorFactory;
        }

        public DatabaseErrorHandler getErrorHandler() {
            return this.mErrorHandler;
        }

        public long getIdleConnectionTimeout() {
            return this.mIdleConnectionTimeout;
        }

        public String getJournalMode() {
            return this.mJournalMode;
        }

        public String getSynchronousMode() {
            return this.mSyncMode;
        }

        public Builder toBuilder() {
            return new Builder(this);
        }
    }

    private SQLiteDatabase(String path, int openFlags, CursorFactory cursorFactory, DatabaseErrorHandler errorHandler, int lookasideSlotSize, int lookasideSlotCount, long idleConnectionTimeoutMs, String journalMode, String syncMode) {
        String str = path;
        this.mCursorFactory = cursorFactory;
        this.mErrorHandler = errorHandler != null ? errorHandler : new DefaultDatabaseErrorHandler();
        this.mConfigurationLocked = new SQLiteDatabaseConfiguration(str, openFlags);
        this.mConfigurationLocked.lookasideSlotSize = lookasideSlotSize;
        this.mConfigurationLocked.lookasideSlotCount = lookasideSlotCount;
        if (ActivityManager.isLowRamDeviceStatic()) {
            this.mConfigurationLocked.lookasideSlotCount = 0;
            this.mConfigurationLocked.lookasideSlotSize = 0;
        }
        long effectiveTimeoutMs = Long.MAX_VALUE;
        if (!this.mConfigurationLocked.isInMemoryDb()) {
            if (idleConnectionTimeoutMs >= 0) {
                effectiveTimeoutMs = idleConnectionTimeoutMs;
            } else if (DEBUG_CLOSE_IDLE_CONNECTIONS) {
                effectiveTimeoutMs = (long) SQLiteGlobal.getIdleConnectionTimeout();
            }
        }
        this.mConfigurationLocked.idleConnectionTimeoutMs = effectiveTimeoutMs;
        this.mConfigurationLocked.journalMode = journalMode;
        this.mConfigurationLocked.syncMode = syncMode;
        if (!SQLiteGlobal.isCompatibilityWalSupported() || (SQLiteCompatibilityWalFlags.areFlagsSet() && !SQLiteCompatibilityWalFlags.isCompatibilityWalSupported())) {
            this.mConfigurationLocked.openFlags |= 1073741824;
        }
        this.jank_dbname = str;
        if (this.jank_dbname.equals("JankEventDb.db")) {
            this.bJankMonitor = false;
        }
        this.mIndexSearchParser = HwFrameworkFactory.getIndexSearchParser();
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            dispose(true);
        } finally {
            super.finalize();
        }
    }

    /* access modifiers changed from: protected */
    public void onAllReferencesReleased() {
        dispose(false);
    }

    private void dispose(boolean finalized) {
        SQLiteConnectionPool pool;
        synchronized (this.mLock) {
            if (this.mCloseGuardLocked != null) {
                if (finalized) {
                    this.mCloseGuardLocked.warnIfOpen();
                }
                this.mCloseGuardLocked.close();
            }
            pool = this.mConnectionPoolLocked;
            this.mConnectionPoolLocked = null;
        }
        if (!finalized) {
            synchronized (sActiveDatabases) {
                sActiveDatabases.remove(this);
            }
            if (pool != null) {
                pool.close();
            }
        }
    }

    public static int releaseMemory() {
        return SQLiteGlobal.releaseMemory();
    }

    @Deprecated
    public void setLockingEnabled(boolean lockingEnabled) {
    }

    /* access modifiers changed from: package-private */
    public String getLabel() {
        String str;
        synchronized (this.mLock) {
            str = this.mConfigurationLocked.label;
        }
        return str;
    }

    /* access modifiers changed from: package-private */
    public void onCorruption() {
        EventLog.writeEvent(EVENT_DB_CORRUPT, getLabel());
        this.mErrorHandler.onCorruption(this);
    }

    /* access modifiers changed from: package-private */
    public SQLiteSession getThreadSession() {
        return this.mThreadSession.get();
    }

    /* access modifiers changed from: package-private */
    public SQLiteSession createSession() {
        SQLiteConnectionPool pool;
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            pool = this.mConnectionPoolLocked;
        }
        return new SQLiteSession(pool);
    }

    /* access modifiers changed from: package-private */
    public int getThreadDefaultConnectionFlags(boolean readOnly) {
        int flags;
        if (readOnly) {
            flags = 1;
        } else {
            flags = 2;
        }
        if (this.mEnableExclusiveConnection && checkConnectionExclusiveHandler()) {
            flags |= 8;
        }
        if (isMainThread()) {
            return flags | 4;
        }
        return flags;
    }

    private static boolean isMainThread() {
        Looper looper = Looper.myLooper();
        return looper != null && looper == Looper.getMainLooper();
    }

    public void beginTransaction() {
        beginTransaction(null, true);
    }

    public void beginTransactionNonExclusive() {
        beginTransaction(null, false);
    }

    public void beginTransactionWithListener(SQLiteTransactionListener transactionListener) {
        beginTransaction(transactionListener, true);
    }

    public void beginTransactionWithListenerNonExclusive(SQLiteTransactionListener transactionListener) {
        beginTransaction(transactionListener, false);
    }

    private void beginTransaction(SQLiteTransactionListener transactionListener, boolean exclusive) {
        int i;
        acquireReference();
        try {
            SQLiteSession threadSession = getThreadSession();
            if (exclusive) {
                i = 2;
            } else {
                i = 1;
            }
            threadSession.beginTransaction(i, transactionListener, getThreadDefaultConnectionFlags(false), null);
        } finally {
            releaseReference();
        }
    }

    public void endTransaction() {
        acquireReference();
        try {
            getThreadSession().endTransaction(null);
            if (getThreadSession().isCommitSuccess()) {
                handleTransMap();
                getThreadSession().clearTransMap();
            }
        } finally {
            releaseReference();
        }
    }

    private void handleTransMap() {
        if (this.mIndexSearchParser != null && getThreadSession().getTransMap().size() > 0) {
            ArrayList<Long> insertArgs = new ArrayList<>();
            ArrayList<Long> updateArgs = new ArrayList<>();
            ArrayList<Long> deleteArgs = new ArrayList<>();
            for (SQLInfo sqlinfo : getThreadSession().getTransMap().keySet()) {
                if (this.mIndexSearchParser.isValidTable(sqlinfo.getTable())) {
                    switch (getThreadSession().getTransMap().get(sqlinfo).intValue()) {
                        case 0:
                            insertArgs.add(Long.valueOf(sqlinfo.getPrimaryKey()));
                            break;
                        case 1:
                            updateArgs.add(Long.valueOf(sqlinfo.getPrimaryKey()));
                            break;
                        case 2:
                            deleteArgs.add(Long.valueOf(sqlinfo.getPrimaryKey()));
                            break;
                    }
                }
            }
            if (insertArgs.size() >= 1) {
                this.mIndexSearchParser.notifyIndexSearchService(insertArgs, 0);
            }
            if (updateArgs.size() >= 1) {
                this.mIndexSearchParser.notifyIndexSearchService(updateArgs, 1);
            }
            if (deleteArgs.size() >= 1) {
                this.mIndexSearchParser.notifyIndexSearchService(deleteArgs, 2);
            }
        }
    }

    public void setTransactionSuccessful() {
        acquireReference();
        try {
            getThreadSession().setTransactionSuccessful();
        } finally {
            releaseReference();
        }
    }

    public boolean inTransaction() {
        acquireReference();
        try {
            return getThreadSession().hasTransaction();
        } finally {
            releaseReference();
        }
    }

    public boolean isDbLockedByCurrentThread() {
        acquireReference();
        try {
            return getThreadSession().hasConnection();
        } finally {
            releaseReference();
        }
    }

    @Deprecated
    public boolean isDbLockedByOtherThreads() {
        return false;
    }

    @Deprecated
    public boolean yieldIfContended() {
        return yieldIfContendedHelper(false, -1);
    }

    public boolean yieldIfContendedSafely() {
        return yieldIfContendedHelper(true, -1);
    }

    public boolean yieldIfContendedSafely(long sleepAfterYieldDelay) {
        return yieldIfContendedHelper(true, sleepAfterYieldDelay);
    }

    private boolean yieldIfContendedHelper(boolean throwIfUnsafe, long sleepAfterYieldDelay) {
        acquireReference();
        try {
            return getThreadSession().yieldTransaction(sleepAfterYieldDelay, throwIfUnsafe, null);
        } finally {
            releaseReference();
        }
    }

    @Deprecated
    public Map<String, String> getSyncedTables() {
        return new HashMap(0);
    }

    public static SQLiteDatabase openDatabase(String path, CursorFactory factory, int flags) {
        return openDatabase(path, factory, flags, null);
    }

    public static SQLiteDatabase openDatabase(File path, OpenParams openParams) {
        return openDatabase(path.getPath(), openParams);
    }

    private static SQLiteDatabase openDatabase(String path, OpenParams openParams) {
        Preconditions.checkArgument(openParams != null, "OpenParams cannot be null");
        SQLiteDatabase sQLiteDatabase = new SQLiteDatabase(path, openParams.mOpenFlags, openParams.mCursorFactory, openParams.mErrorHandler, openParams.mLookasideSlotSize, openParams.mLookasideSlotCount, openParams.mIdleConnectionTimeout, openParams.mJournalMode, openParams.mSyncMode);
        sQLiteDatabase.open();
        return sQLiteDatabase;
    }

    public static SQLiteDatabase openDatabase(String path, CursorFactory factory, int flags, DatabaseErrorHandler errorHandler) {
        SQLiteDatabase db = new SQLiteDatabase(path, flags, factory, errorHandler, -1, -1, -1, null, null);
        db.open();
        return db;
    }

    public static SQLiteDatabase openOrCreateDatabase(File file, CursorFactory factory) {
        return openOrCreateDatabase(file.getPath(), factory);
    }

    public static SQLiteDatabase openOrCreateDatabase(String path, CursorFactory factory) {
        return openDatabase(path, factory, 268435456, null);
    }

    public static SQLiteDatabase openOrCreateDatabase(String path, CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return openDatabase(path, factory, 268435456, errorHandler);
    }

    public static boolean deleteDatabase(File file) {
        if (file != null) {
            boolean deleted = false | file.delete() | new File(file.getPath() + "-journal").delete() | new File(file.getPath() + "-shm").delete() | new File(file.getPath() + "-wal").delete();
            File dir = file.getParentFile();
            if (dir != null) {
                final String prefix = file.getName() + "-mj";
                File[] files = dir.listFiles(new FileFilter() {
                    public boolean accept(File candidate) {
                        return candidate.getName().startsWith(prefix);
                    }
                });
                if (files != null) {
                    for (File masterJournal : files) {
                        deleted |= masterJournal.delete();
                    }
                }
            }
            return deleted;
        }
        throw new IllegalArgumentException("file must not be null");
    }

    public void reopenReadWrite() {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            if (isReadOnlyLocked()) {
                int oldOpenFlags = this.mConfigurationLocked.openFlags;
                this.mConfigurationLocked.openFlags = (this.mConfigurationLocked.openFlags & -2) | 0;
                try {
                    this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
                } catch (RuntimeException ex) {
                    this.mConfigurationLocked.openFlags = oldOpenFlags;
                    throw ex;
                }
            }
        }
    }

    private void open() {
        try {
            openInner();
        } catch (SQLiteDatabaseCorruptException e) {
            try {
                onCorruption();
                openInner();
            } catch (SQLiteException ex) {
                Log.e(TAG, "Failed to open database '" + getLabel() + "'.", ex);
                close();
                throw ex;
            }
        }
    }

    /* JADX WARNING: CFG modification limit reached, blocks count: 120 */
    private void openInner() {
        synchronized (this.mLock) {
            this.mConnectionPoolLocked = SQLiteConnectionPool.open(this.mConfigurationLocked, this.mEnableExclusiveConnection);
            this.mCloseGuardLocked.open("close");
        }
        synchronized (sActiveDatabases) {
            sActiveDatabases.put(this, null);
        }
    }

    public static SQLiteDatabase create(CursorFactory factory) {
        return openDatabase(SQLiteDatabaseConfiguration.MEMORY_DB_PATH, factory, 268435456);
    }

    public static SQLiteDatabase createInMemory(OpenParams openParams) {
        return openDatabase(SQLiteDatabaseConfiguration.MEMORY_DB_PATH, openParams.toBuilder().addOpenFlags(268435456).build());
    }

    public void addCustomFunction(String name, int numArgs, CustomFunction function) {
        SQLiteCustomFunction wrapper = new SQLiteCustomFunction(name, numArgs, function);
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            this.mConfigurationLocked.customFunctions.add(wrapper);
            try {
                this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
            } catch (RuntimeException ex) {
                this.mConfigurationLocked.customFunctions.remove(wrapper);
                throw ex;
            }
        }
    }

    public int getVersion() {
        return Long.valueOf(DatabaseUtils.longForQuery(this, "PRAGMA user_version;", null)).intValue();
    }

    public void setVersion(int version) {
        execSQL("PRAGMA user_version = " + version);
    }

    public long getMaximumSize() {
        return getPageSize() * DatabaseUtils.longForQuery(this, "PRAGMA max_page_count;", null);
    }

    public long setMaximumSize(long numBytes) {
        long pageSize = getPageSize();
        long numPages = numBytes / pageSize;
        if (numBytes % pageSize != 0) {
            numPages++;
        }
        return DatabaseUtils.longForQuery(this, "PRAGMA max_page_count = " + numPages, null) * pageSize;
    }

    public long getPageSize() {
        return DatabaseUtils.longForQuery(this, "PRAGMA page_size;", null);
    }

    public void setPageSize(long numBytes) {
        execSQL("PRAGMA page_size = " + numBytes);
    }

    @Deprecated
    public void markTableSyncable(String table, String deletedTable) {
    }

    @Deprecated
    public void markTableSyncable(String table, String foreignKey, String updateTable) {
    }

    public static String findEditTable(String tables) {
        if (!TextUtils.isEmpty(tables)) {
            int spacepos = tables.indexOf(32);
            int commapos = tables.indexOf(44);
            if (spacepos > 0 && (spacepos < commapos || commapos < 0)) {
                return tables.substring(0, spacepos);
            }
            if (commapos <= 0 || (commapos >= spacepos && spacepos >= 0)) {
                return tables;
            }
            return tables.substring(0, commapos);
        }
        throw new IllegalStateException("Invalid tables");
    }

    public SQLiteStatement compileStatement(String sql) throws SQLException {
        acquireReference();
        try {
            return new SQLiteStatement(this, sql, null);
        } finally {
            releaseReference();
        }
    }

    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return queryWithFactory(null, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, null);
    }

    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, CancellationSignal cancellationSignal) {
        return queryWithFactory(null, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal);
    }

    public Cursor queryWithFactory(CursorFactory cursorFactory, boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, null);
    }

    public Cursor queryWithFactory(CursorFactory cursorFactory, boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, CancellationSignal cancellationSignal) {
        acquireReference();
        try {
            return rawQueryWithFactory(cursorFactory, SQLiteQueryBuilder.buildQueryString(distinct, table, columns, selection, groupBy, having, orderBy, limit), selectionArgs, findEditTable(table), cancellationSignal);
        } finally {
            releaseReference();
        }
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return query(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, null);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return query(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return rawQueryWithFactory(null, sql, selectionArgs, null, null);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs, CancellationSignal cancellationSignal) {
        return rawQueryWithFactory(null, sql, selectionArgs, null, cancellationSignal);
    }

    public Cursor rawQueryWithFactory(CursorFactory cursorFactory, String sql, String[] selectionArgs, String editTable) {
        return rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable, null);
    }

    public Cursor rawQueryWithFactory(CursorFactory cursorFactory, String sql, String[] selectionArgs, String editTable, CancellationSignal cancellationSignal) {
        long begin = 0;
        if (this.bJankMonitor) {
            begin = SystemClock.uptimeMillis();
        }
        acquireReference();
        try {
            return new SQLiteDirectCursorDriver(this, sql, editTable, cancellationSignal).query(cursorFactory != null ? cursorFactory : this.mCursorFactory, selectionArgs);
        } finally {
            releaseReference();
            if (this.bJankMonitor) {
                this.mJankDBStats.addQuery(SystemClock.uptimeMillis() - begin, editTable, this.jank_dbname);
            }
        }
    }

    private Cursor queryForIndexSearch(String table, String whereClause, String[] whereArgs, int operation) {
        StringBuilder sql = new StringBuilder();
        if ("files".equals(table)) {
            sql.append("SELECT _id FROM ");
            sql.append(table);
            sql.append(" WHERE ");
            sql.append(whereClause);
            sql.append(" AND ");
            sql.append("((mime_type='text/plain') OR (mime_type='text/html') OR (mime_type='text/htm') OR (mime_type = 'application/msword') OR (mime_type = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document') OR (mime_type = 'application/vnd.ms-excel') OR (mime_type = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') OR (mime_type = 'application/mspowerpoint') OR (mime_type = 'application/vnd.openxmlformats-officedocument.presentationml.presentation')) ");
        } else if ("Body".equals(table)) {
            sql.append("SELECT messageKey FROM Body WHERE ");
            sql.append(whereClause);
        } else if ("Events".equals(table)) {
            sql.append("SELECT _id FROM ");
            sql.append(table);
            sql.append(" WHERE ");
            sql.append(whereClause);
            sql.append(" AND ");
            sql.append("mutators IS NOT 'com.android.providers.contacts'");
        } else if ("Calendars".equals(table)) {
            if (whereArgs == null || whereArgs.length != 1) {
                sql.append("SELECT _id FROM ");
                sql.append(table);
                sql.append(" WHERE ");
                sql.append(whereClause);
            } else {
                sql.append("SELECT _id FROM Events WHERE calendar_id IN (?)");
            }
        } else if ("Mailbox".equals(table)) {
            if (2 != operation) {
                return null;
            }
            if (whereClause == null) {
                sql.append("SELECT _id FROM Message");
            } else {
                sql.append("SELECT _id FROM Message WHERE mailboxKey in (select _id FROM Mailbox WHERE ");
                sql.append(whereClause);
                sql.append(")");
            }
        } else if (!"fav_sms".equals(table)) {
            sql.append("SELECT _id FROM ");
            sql.append(table);
            sql.append(" WHERE ");
            sql.append(whereClause);
        } else if (whereClause == null) {
            sql.append("SELECT _id FROM words WHERE table_to_use IS 8");
        } else {
            sql.append("SELECT _id FROM words WHERE source_id in (select _id FROM fav_sms WHERE ");
            sql.append(whereClause);
            sql.append(" ) AND table_to_use IS 8");
        }
        return rawQuery(sql.toString(), whereArgs);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0088, code lost:
        if (r0 == null) goto L_0x0094;
     */
    private void triggerUpdatingOrDeletingIndex(String table, String whereClause, String[] whereArgs, int operation) {
        String realTable;
        if (this.mIndexSearchParser != null && this.mIndexSearchParser.isValidTable(table)) {
            Cursor c = null;
            try {
                c = queryForIndexSearch(table, whereClause, whereArgs, operation);
                if (c != null) {
                    if (c.getCount() != 0) {
                        if (getThreadSession().hasTransaction()) {
                            while (c.moveToNext()) {
                                if (table.equals("Body")) {
                                    realTable = "Message";
                                } else {
                                    realTable = table;
                                }
                                getThreadSession().insertTransMap(realTable, c.getLong(0), operation);
                            }
                        } else {
                            List<Long> ids = new ArrayList<>();
                            while (c.moveToNext()) {
                                ids.add(Long.valueOf(c.getLong(0)));
                            }
                            this.mIndexSearchParser.notifyIndexSearchService(ids, operation);
                        }
                        if (c != null) {
                            c.close();
                        }
                    }
                }
                Log.v(TAG, "triggerBuildingIndex(): cursor is null or count is 0, return.");
                if (c != null) {
                    c.close();
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "triggerUpdatingOrDeletingIndex(): RuntimeException");
            } catch (Exception e2) {
                Log.e(TAG, "triggerUpdatingOrDeletingIndex(): Exception");
                if (c != null) {
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
                throw th;
            }
        }
    }

    private void triggerDeletingCalendarAccounts(String sql, Object[] bindArgs) {
        if (this.mIndexSearchParser != null && "DELETE FROM Calendars WHERE account_name=? AND account_type=?".equals(sql)) {
            Cursor c1 = null;
            Cursor c2 = null;
            int i = 0;
            List<Long> eventsIds = new ArrayList<>();
            try {
                Cursor c12 = rawQuery("SELECT _id FROM Calendars where account_name=? AND account_type=?", (String[]) bindArgs);
                if (c12 != null && c12.getCount() > 0) {
                    String[] calendarIds = new String[c12.getCount()];
                    while (c12.moveToNext()) {
                        calendarIds[i] = c12.getString(0);
                        i++;
                    }
                    c2 = queryForIndexSearch("Events", "calendar_id IN (?)", calendarIds, 2);
                    if (c2 != null && c2.getCount() > 0) {
                        while (c2.moveToNext()) {
                            eventsIds.add(Long.valueOf(c2.getLong(0)));
                        }
                        this.mIndexSearchParser.notifyIndexSearchService(eventsIds, 2);
                    }
                }
                if (c12 != null) {
                    c12.close();
                }
                if (c2 != null) {
                    c2.close();
                }
                if (eventsIds.isEmpty()) {
                    return;
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "triggerDeletingCalendarAccounts(): RuntimeException");
                if (c1 != null) {
                    c1.close();
                }
                if (c2 != null) {
                    c2.close();
                }
                if (eventsIds.isEmpty()) {
                    return;
                }
            } catch (Throwable th) {
                if (c1 != null) {
                    c1.close();
                }
                if (c2 != null) {
                    c2.close();
                }
                if (!eventsIds.isEmpty()) {
                    eventsIds.clear();
                }
                throw th;
            }
            eventsIds.clear();
        }
    }

    private void triggerAddingIndex(String table, long id) {
        String realTable;
        if (id < 0) {
            Log.v(TAG, "triggerBuildingIndex(): invalid id, return.");
            return;
        }
        if (this.mIndexSearchParser != null && this.mIndexSearchParser.isValidTable(table)) {
            if (table.equals("Events")) {
                Cursor c = null;
                try {
                    c = rawQuery("SELECT _id FROM Events WHERE _id = " + id + " AND mutators IS NOT 'com.android.providers.contacts'", null);
                    if (c == null || c.getCount() == 0) {
                        if (c != null) {
                            c.close();
                        }
                        return;
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
            if (getThreadSession().hasTransaction()) {
                if (table.equals("Body")) {
                    realTable = "Message";
                } else {
                    realTable = table;
                }
                getThreadSession().insertTransMap(realTable, id, 0);
            } else {
                this.mIndexSearchParser.notifyIndexSearchService(id, 0);
            }
        }
    }

    public long insert(String table, String nullColumnHack, ContentValues values) {
        try {
            return insertWithOnConflict(table, nullColumnHack, values, 0);
        } catch (SQLException e) {
            Log.e(TAG, "Error inserting " + e.getMessage());
            return -1;
        }
    }

    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException {
        return insertWithOnConflict(table, nullColumnHack, values, 0);
    }

    public long replace(String table, String nullColumnHack, ContentValues initialValues) {
        try {
            return insertWithOnConflict(table, nullColumnHack, initialValues, 5);
        } catch (SQLException e) {
            Log.e(TAG, "Error inserting " + e.getMessage());
            return -1;
        }
    }

    public long replaceOrThrow(String table, String nullColumnHack, ContentValues initialValues) throws SQLException {
        return insertWithOnConflict(table, nullColumnHack, initialValues, 5);
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x00e5  */
    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm) {
        SQLiteStatement statement;
        String str = table;
        ContentValues contentValues = initialValues;
        long begin = 0;
        if (this.bJankMonitor) {
            begin = SystemClock.uptimeMillis();
        }
        acquireReference();
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT");
            sql.append(CONFLICT_VALUES[conflictAlgorithm]);
            sql.append(" INTO ");
            sql.append(str);
            sql.append('(');
            Object[] bindArgs = null;
            int size = (contentValues == null || initialValues.isEmpty()) ? 0 : initialValues.size();
            if (size > 0) {
                bindArgs = new Object[size];
                int i = 0;
                for (String colName : initialValues.keySet()) {
                    sql.append(i > 0 ? "," : "");
                    sql.append(colName);
                    bindArgs[i] = contentValues.get(colName);
                    i++;
                }
                sql.append(')');
                sql.append(" VALUES (");
                int i2 = 0;
                while (i2 < size) {
                    sql.append(i2 > 0 ? ",?" : "?");
                    i2++;
                }
                String str2 = nullColumnHack;
            } else {
                StringBuilder sb = new StringBuilder();
                try {
                    sb.append(nullColumnHack);
                    sb.append(") VALUES (NULL");
                    sql.append(sb.toString());
                } catch (Throwable th) {
                    th = th;
                    releaseReference();
                    if (this.bJankMonitor) {
                        this.mJankDBStats.addInsert(SystemClock.uptimeMillis() - begin, str, this.jank_dbname);
                    }
                    throw th;
                }
            }
            sql.append(')');
            statement = new SQLiteStatement(this, sql.toString(), bindArgs);
            long id = statement.executeInsert();
            triggerAddingIndex(str, id);
            statement.close();
            releaseReference();
            if (this.bJankMonitor) {
                this.mJankDBStats.addInsert(SystemClock.uptimeMillis() - begin, str, this.jank_dbname);
            }
            return id;
        } catch (Throwable th2) {
            th = th2;
            String str3 = nullColumnHack;
            releaseReference();
            if (this.bJankMonitor) {
            }
            throw th;
        }
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        SQLiteStatement statement;
        String str;
        long begin = 0;
        if (this.bJankMonitor) {
            begin = SystemClock.uptimeMillis();
        }
        acquireReference();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("DELETE FROM ");
            sb.append(table);
            if (!TextUtils.isEmpty(whereClause)) {
                str = " WHERE " + whereClause;
            } else {
                str = "";
            }
            sb.append(str);
            statement = new SQLiteStatement(this, sb.toString(), whereArgs);
            triggerUpdatingOrDeletingIndex(table, whereClause, whereArgs, 2);
            int executeUpdateDelete = statement.executeUpdateDelete();
            statement.close();
            releaseReference();
            if (this.bJankMonitor) {
                this.mJankDBStats.addDelete(SystemClock.uptimeMillis() - begin, table, this.jank_dbname);
            }
            return executeUpdateDelete;
        } catch (Throwable th) {
            releaseReference();
            if (this.bJankMonitor) {
                this.mJankDBStats.addDelete(SystemClock.uptimeMillis() - begin, table, this.jank_dbname);
            }
            throw th;
        }
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        triggerUpdatingOrDeletingIndex(table, whereClause, whereArgs, 1);
        return updateWithOnConflict(table, values, whereClause, whereArgs, 0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x00db  */
    public int updateWithOnConflict(String table, ContentValues values, String whereClause, String[] whereArgs, int conflictAlgorithm) {
        int i;
        SQLiteStatement statement;
        String str = table;
        ContentValues contentValues = values;
        String[] strArr = whereArgs;
        long begin = 0;
        if (this.bJankMonitor) {
            begin = SystemClock.uptimeMillis();
        }
        if (contentValues == null || values.isEmpty()) {
            String str2 = whereClause;
            throw new IllegalArgumentException("Empty values");
        }
        acquireReference();
        try {
            StringBuilder sql = new StringBuilder(120);
            sql.append("UPDATE ");
            sql.append(CONFLICT_VALUES[conflictAlgorithm]);
            sql.append(str);
            sql.append(" SET ");
            int setValuesSize = values.size();
            int bindArgsSize = strArr == null ? setValuesSize : strArr.length + setValuesSize;
            Object[] bindArgs = new Object[bindArgsSize];
            int i2 = 0;
            for (String colName : values.keySet()) {
                sql.append(i > 0 ? "," : "");
                sql.append(colName);
                bindArgs[i] = contentValues.get(colName);
                sql.append("=?");
                i2 = i + 1;
            }
            if (strArr != null) {
                i = setValuesSize;
                while (i < bindArgsSize) {
                    bindArgs[i] = strArr[i - setValuesSize];
                    i++;
                }
            }
            int i3 = i;
            if (TextUtils.isEmpty(whereClause) == 0) {
                sql.append(" WHERE ");
                try {
                    sql.append(whereClause);
                } catch (Throwable th) {
                    th = th;
                    releaseReference();
                    if (this.bJankMonitor) {
                    }
                    throw th;
                }
            } else {
                String str3 = whereClause;
            }
            statement = new SQLiteStatement(this, sql.toString(), bindArgs);
            int executeUpdateDelete = statement.executeUpdateDelete();
            statement.close();
            releaseReference();
            if (this.bJankMonitor) {
                StringBuilder sb = sql;
                this.mJankDBStats.addUpdate(SystemClock.uptimeMillis() - begin, str, this.jank_dbname);
            }
            return executeUpdateDelete;
        } catch (Throwable th2) {
            th = th2;
            String str4 = whereClause;
            releaseReference();
            if (this.bJankMonitor) {
                this.mJankDBStats.addUpdate(SystemClock.uptimeMillis() - begin, str, this.jank_dbname);
            }
            throw th;
        }
    }

    public void execSQL(String sql) throws SQLException {
        executeSql(sql, null);
    }

    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        if (bindArgs != null) {
            triggerDeletingCalendarAccounts(sql, bindArgs);
            executeSql(sql, bindArgs);
            return;
        }
        throw new IllegalArgumentException("Empty bindArgs");
    }

    public int executeSql(String sql, Object[] bindArgs) throws SQLException {
        SQLiteStatement statement;
        long begin = 0;
        if (this.bJankMonitor) {
            begin = SystemClock.uptimeMillis();
        }
        acquireReference();
        try {
            int statementType = DatabaseUtils.getSqlStatementType(sql);
            if (statementType == 3) {
                boolean disableWal = false;
                synchronized (this.mLock) {
                    if (!this.mHasAttachedDbsLocked) {
                        this.mHasAttachedDbsLocked = true;
                        disableWal = true;
                        this.mConnectionPoolLocked.disableIdleConnectionHandler();
                    }
                }
                if (disableWal) {
                    disableWriteAheadLogging();
                }
            }
            try {
                statement = new SQLiteStatement(this, sql, bindArgs);
                int executeUpdateDelete = statement.executeUpdateDelete();
                statement.close();
                if (statementType == 8) {
                    this.mConnectionPoolLocked.closeAvailableNonPrimaryConnectionsAndLogExceptions();
                }
                releaseReference();
                if (this.bJankMonitor) {
                    this.mJankDBStats.addExecsql(SystemClock.uptimeMillis() - begin, "", this.jank_dbname);
                }
                return executeUpdateDelete;
            } catch (Throwable th) {
                if (statementType == 8) {
                    this.mConnectionPoolLocked.closeAvailableNonPrimaryConnectionsAndLogExceptions();
                }
                throw th;
            }
            throw th;
        } catch (Throwable th2) {
            releaseReference();
            if (this.bJankMonitor) {
                this.mJankDBStats.addExecsql(SystemClock.uptimeMillis() - begin, "", this.jank_dbname);
            }
            throw th2;
        }
    }

    public void validateSql(String sql, CancellationSignal cancellationSignal) {
        getThreadSession().prepare(sql, getThreadDefaultConnectionFlags(true), cancellationSignal, null);
    }

    public boolean isReadOnly() {
        boolean isReadOnlyLocked;
        synchronized (this.mLock) {
            isReadOnlyLocked = isReadOnlyLocked();
        }
        return isReadOnlyLocked;
    }

    private boolean isReadOnlyLocked() {
        return (this.mConfigurationLocked.openFlags & 1) == 1;
    }

    public boolean isInMemoryDatabase() {
        boolean isInMemoryDb;
        synchronized (this.mLock) {
            isInMemoryDb = this.mConfigurationLocked.isInMemoryDb();
        }
        return isInMemoryDb;
    }

    public boolean isOpen() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mConnectionPoolLocked != null;
        }
        return z;
    }

    public boolean needUpgrade(int newVersion) {
        return newVersion > getVersion();
    }

    public final String getPath() {
        String str;
        synchronized (this.mLock) {
            str = this.mConfigurationLocked.path;
        }
        return str;
    }

    public void setLocale(Locale locale) {
        if (locale != null) {
            synchronized (this.mLock) {
                throwIfNotOpenLocked();
                Locale oldLocale = this.mConfigurationLocked.locale;
                this.mConfigurationLocked.locale = locale;
                try {
                    this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
                } catch (RuntimeException ex) {
                    this.mConfigurationLocked.locale = oldLocale;
                    throw ex;
                }
            }
            return;
        }
        throw new IllegalArgumentException("locale must not be null.");
    }

    public void setMaxSqlCacheSize(int cacheSize) {
        if (cacheSize > 100 || cacheSize < 0) {
            throw new IllegalStateException("expected value between 0 and 100");
        }
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            int oldMaxSqlCacheSize = this.mConfigurationLocked.maxSqlCacheSize;
            this.mConfigurationLocked.maxSqlCacheSize = cacheSize;
            try {
                this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
            } catch (RuntimeException ex) {
                this.mConfigurationLocked.maxSqlCacheSize = oldMaxSqlCacheSize;
                throw ex;
            }
        }
    }

    public void setForeignKeyConstraintsEnabled(boolean enable) {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            if (this.mConfigurationLocked.foreignKeyConstraintsEnabled != enable) {
                this.mConfigurationLocked.foreignKeyConstraintsEnabled = enable;
                try {
                    this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
                } catch (RuntimeException ex) {
                    this.mConfigurationLocked.foreignKeyConstraintsEnabled = !enable;
                    throw ex;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0077, code lost:
        return false;
     */
    public boolean enableWriteAheadLogging() {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            boolean defaultWALEnabled = this.mConfigurationLocked.defaultWALEnabled;
            boolean explicitWALEnabled = this.mConfigurationLocked.explicitWALEnabled;
            if (this.mConfigurationLocked.configurationEnhancement) {
                if ((this.mConfigurationLocked.openFlags & 536870912) != 0 && this.mConfigurationLocked.explicitWALEnabled) {
                    return true;
                }
            } else if ((this.mConfigurationLocked.openFlags & 536870912) != 0) {
                return true;
            }
            if (isReadOnlyLocked()) {
                return false;
            }
            if (this.mConfigurationLocked.isInMemoryDb()) {
                Log.i(TAG, "can't enable WAL for memory databases.");
                return false;
            } else if (!this.mHasAttachedDbsLocked) {
                SQLiteDatabaseConfiguration sQLiteDatabaseConfiguration = this.mConfigurationLocked;
                sQLiteDatabaseConfiguration.openFlags = 536870912 | sQLiteDatabaseConfiguration.openFlags;
                if (this.mConfigurationLocked.configurationEnhancement) {
                    this.mConfigurationLocked.defaultWALEnabled = false;
                    this.mConfigurationLocked.explicitWALEnabled = true;
                }
                try {
                    this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
                    return true;
                } catch (RuntimeException ex) {
                    this.mConfigurationLocked.openFlags &= -536870913;
                    if (this.mConfigurationLocked.configurationEnhancement) {
                        this.mConfigurationLocked.defaultWALEnabled = defaultWALEnabled;
                        this.mConfigurationLocked.explicitWALEnabled = explicitWALEnabled;
                    }
                    throw ex;
                }
            } else if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "this database: " + this.mConfigurationLocked.label + " has attached databases. can't  enable WAL.");
            }
        }
    }

    public void disableWriteAheadLogging() {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            boolean defaultWALEnabled = this.mConfigurationLocked.defaultWALEnabled;
            boolean explicitWALEnabled = this.mConfigurationLocked.explicitWALEnabled;
            int oldFlags = this.mConfigurationLocked.openFlags;
            boolean walDisabled = (536870912 & oldFlags) == 0;
            boolean compatibilityWalDisabled = (oldFlags & 1073741824) != 0;
            if (!walDisabled || !compatibilityWalDisabled) {
                this.mConfigurationLocked.openFlags &= -536870913;
                if (this.mConfigurationLocked.configurationEnhancement) {
                    this.mConfigurationLocked.defaultWALEnabled = false;
                    this.mConfigurationLocked.explicitWALEnabled = false;
                }
                SQLiteDatabaseConfiguration sQLiteDatabaseConfiguration = this.mConfigurationLocked;
                sQLiteDatabaseConfiguration.openFlags = 1073741824 | sQLiteDatabaseConfiguration.openFlags;
                try {
                    this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
                } catch (RuntimeException ex) {
                    this.mConfigurationLocked.openFlags = oldFlags;
                    if (this.mConfigurationLocked.configurationEnhancement) {
                        this.mConfigurationLocked.defaultWALEnabled = defaultWALEnabled;
                        this.mConfigurationLocked.explicitWALEnabled = explicitWALEnabled;
                    }
                    throw ex;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0020, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002b, code lost:
        return r2;
     */
    public boolean isWriteAheadLoggingEnabled() {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            boolean z = false;
            if (this.mConfigurationLocked.configurationEnhancement) {
                if ((this.mConfigurationLocked.openFlags & 536870912) != 0 && this.mConfigurationLocked.explicitWALEnabled) {
                    z = true;
                }
            } else if ((this.mConfigurationLocked.openFlags & 536870912) != 0) {
                z = true;
            }
        }
    }

    static ArrayList<SQLiteDebug.DbStats> getDbStats() {
        ArrayList<SQLiteDebug.DbStats> dbStatsList = new ArrayList<>();
        Iterator<SQLiteDatabase> it = getActiveDatabases().iterator();
        while (it.hasNext()) {
            it.next().collectDbStats(dbStatsList);
        }
        return dbStatsList;
    }

    private void collectDbStats(ArrayList<SQLiteDebug.DbStats> dbStatsList) {
        synchronized (this.mLock) {
            if (this.mConnectionPoolLocked != null) {
                this.mConnectionPoolLocked.collectDbStats(dbStatsList);
            }
        }
    }

    private static ArrayList<SQLiteDatabase> getActiveDatabases() {
        ArrayList<SQLiteDatabase> databases = new ArrayList<>();
        synchronized (sActiveDatabases) {
            databases.addAll(sActiveDatabases.keySet());
        }
        return databases;
    }

    static void dumpAll(Printer printer, boolean verbose) {
        Iterator<SQLiteDatabase> it = getActiveDatabases().iterator();
        while (it.hasNext()) {
            it.next().dump(printer, verbose);
        }
    }

    private void dump(Printer printer, boolean verbose) {
        synchronized (this.mLock) {
            if (this.mConnectionPoolLocked != null) {
                printer.println("");
                this.mConnectionPoolLocked.dump(printer, verbose);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0028, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        r1 = rawQuery("pragma database_list;", null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0035, code lost:
        if (r1.moveToNext() == false) goto L_0x004a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0037, code lost:
        r0.add(new android.util.Pair(r1.getString(1), r1.getString(2)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004a, code lost:
        if (r1 == null) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004f, code lost:
        releaseReference();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0053, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0054, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0055, code lost:
        if (r1 != null) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005b, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005d, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005e, code lost:
        releaseReference();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0061, code lost:
        throw r1;
     */
    public List<Pair<String, String>> getAttachedDbs() {
        ArrayList<Pair<String, String>> attachedDbs = new ArrayList<>();
        synchronized (this.mLock) {
            if (this.mConnectionPoolLocked == null) {
                return null;
            }
            if (!this.mHasAttachedDbsLocked) {
                attachedDbs.add(new Pair("main", this.mConfigurationLocked.path));
                return attachedDbs;
            }
            acquireReference();
        }
    }

    public boolean isDatabaseIntegrityOk() {
        List<Pair<String, String>> attachedDbs;
        SQLiteStatement prog;
        acquireReference();
        try {
            attachedDbs = getAttachedDbs();
            if (attachedDbs != null) {
                for (int i = 0; i < attachedDbs.size(); i++) {
                    Pair<String, String> p = attachedDbs.get(i);
                    prog = null;
                    SQLiteStatement prog2 = compileStatement("PRAGMA " + ((String) p.first) + ".integrity_check(1);");
                    if (!prog2.simpleQueryForString().equalsIgnoreCase("ok")) {
                        Log.e(TAG, "PRAGMA integrity_check on " + ((String) p.second) + " returned: " + rslt);
                        if (prog2 != null) {
                            prog2.close();
                        }
                        releaseReference();
                        return false;
                    }
                    if (prog2 != null) {
                        prog2.close();
                    }
                }
                releaseReference();
                return true;
            }
            throw new IllegalStateException("databaselist for: " + getPath() + " couldn't be retrieved. probably because the database is closed");
        } catch (SQLiteException e) {
            attachedDbs = new ArrayList<>();
            attachedDbs.add(new Pair("main", getPath()));
        } catch (Throwable th) {
            releaseReference();
            throw th;
        }
    }

    public String toString() {
        return "SQLiteDatabase: " + getPath();
    }

    private void throwIfNotOpenLocked() {
        if (this.mConnectionPoolLocked == null) {
            throw new IllegalStateException("The database '" + this.mConfigurationLocked.label + "' is not open.");
        }
    }

    public void enableExclusiveConnection(boolean enabled, SQLiteDatabaseEx.DatabaseConnectionExclusiveHandler connectionExclusiveHandler) {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            this.mEnableExclusiveConnection = enabled;
            this.mConnectionExclusiveHandler = connectionExclusiveHandler;
            if (this.mConnectionPoolLocked != null) {
                this.mConnectionPoolLocked.setExclusiveConnectionEnabled(this.mEnableExclusiveConnection);
            }
        }
    }

    private boolean checkConnectionExclusiveHandler() {
        if (this.mConnectionExclusiveHandler != null) {
            return this.mConnectionExclusiveHandler.onConnectionExclusive();
        }
        return false;
    }
}
