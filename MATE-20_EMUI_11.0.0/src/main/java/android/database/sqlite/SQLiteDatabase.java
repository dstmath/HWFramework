package android.database.sqlite;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.DefaultDatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabaseEx;
import android.database.sqlite.SQLiteDebug;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.CancellationSignal;
import android.os.Looper;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Log;
import android.util.Pair;
import android.util.Printer;
import com.android.internal.util.Preconditions;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
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
    @UnsupportedAppUsage
    public static final String[] CONFLICT_VALUES = {"", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE "};
    public static final int CREATE_IF_NECESSARY = 268435456;
    private static final boolean DEBUG_CLOSE_IDLE_CONNECTIONS = SystemProperties.getBoolean("persist.debug.sqlite.close_idle_connections", false);
    public static final int ENABLE_LEGACY_COMPATIBILITY_WAL = Integer.MIN_VALUE;
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
    private boolean isJankMonitor = true;
    private String jankDbname = "";
    private final CloseGuard mCloseGuardLocked = CloseGuard.get();
    @UnsupportedAppUsage
    private final SQLiteDatabaseConfiguration mConfigurationLocked;
    private SQLiteDatabaseEx.DatabaseConnectionExclusiveHandler mConnectionExclusiveHandler;
    @UnsupportedAppUsage
    private SQLiteConnectionPool mConnectionPoolLocked;
    private final CursorFactory mCursorFactory;
    private boolean mEnableExclusiveConnection = false;
    private final DatabaseErrorHandler mErrorHandler;
    private boolean mHasAttachedDbsLocked;
    private final IHwSQLiteDatabase mHwSQLiteDatabase;
    private JankSqlite mJankDbStats = new JankSqlite();
    private final Object mLock = new Object();
    @UnsupportedAppUsage
    private final ThreadLocal<SQLiteSession> mThreadSession = ThreadLocal.withInitial(new Supplier() {
        /* class android.database.sqlite.$$Lambda$RBWjWVyGrOTsQrLCYzJ_G8Uk25Q */

        @Override // java.util.function.Supplier
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

    private SQLiteDatabase(String path, int openFlags, CursorFactory cursorFactory, DatabaseErrorHandler errorHandler, int lookasideSlotSize, int lookasideSlotCount, long idleConnectionTimeoutMs, String journalMode, String syncMode) {
        this.mCursorFactory = cursorFactory;
        this.mErrorHandler = errorHandler != null ? errorHandler : new DefaultDatabaseErrorHandler();
        this.mConfigurationLocked = new SQLiteDatabaseConfiguration(path, openFlags);
        SQLiteDatabaseConfiguration sQLiteDatabaseConfiguration = this.mConfigurationLocked;
        sQLiteDatabaseConfiguration.lookasideSlotSize = lookasideSlotSize;
        sQLiteDatabaseConfiguration.lookasideSlotCount = lookasideSlotCount;
        if (ActivityManager.isLowRamDeviceStatic()) {
            SQLiteDatabaseConfiguration sQLiteDatabaseConfiguration2 = this.mConfigurationLocked;
            sQLiteDatabaseConfiguration2.lookasideSlotCount = 0;
            sQLiteDatabaseConfiguration2.lookasideSlotSize = 0;
        }
        long effectiveTimeoutMs = Long.MAX_VALUE;
        if (!this.mConfigurationLocked.isInMemoryDb()) {
            if (idleConnectionTimeoutMs >= 0) {
                effectiveTimeoutMs = idleConnectionTimeoutMs;
            } else if (DEBUG_CLOSE_IDLE_CONNECTIONS) {
                effectiveTimeoutMs = (long) SQLiteGlobal.getIdleConnectionTimeout();
            }
        }
        SQLiteDatabaseConfiguration sQLiteDatabaseConfiguration3 = this.mConfigurationLocked;
        sQLiteDatabaseConfiguration3.idleConnectionTimeoutMs = effectiveTimeoutMs;
        sQLiteDatabaseConfiguration3.journalMode = journalMode;
        sQLiteDatabaseConfiguration3.syncMode = syncMode;
        if (SQLiteCompatibilityWalFlags.isLegacyCompatibilityWalEnabled()) {
            this.mConfigurationLocked.openFlags |= Integer.MIN_VALUE;
        }
        this.jankDbname = path;
        if (this.jankDbname.equals("JankEventDb.db")) {
            this.isJankMonitor = false;
        }
        this.mHwSQLiteDatabase = HwFrameworkFactory.getHwSQLiteDatabase();
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

    /* access modifiers changed from: protected */
    @Override // android.database.sqlite.SQLiteClosable
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
        EventLog.writeEvent((int) EVENT_DB_CORRUPT, getLabel());
        this.mErrorHandler.onCorruption(this);
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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
            if (this.mHwSQLiteDatabase != null) {
                this.mHwSQLiteDatabase.triggerTransactionIndex(this);
            }
        } finally {
            releaseReference();
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

    @UnsupportedAppUsage
    private static SQLiteDatabase openDatabase(String path, OpenParams openParams) {
        Preconditions.checkArgument(openParams != null, "OpenParams cannot be null");
        SQLiteDatabase db = new SQLiteDatabase(path, openParams.mOpenFlags, openParams.mCursorFactory, openParams.mErrorHandler, openParams.mLookasideSlotSize, openParams.mLookasideSlotCount, openParams.mIdleConnectionTimeout, openParams.mJournalMode, openParams.mSyncMode);
        db.open();
        return db;
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
        return deleteDatabase(file, true);
    }

    public static boolean deleteDatabase(File file, boolean removeCheckFile) {
        if (file != null) {
            boolean deleted = false | file.delete() | new File(file.getPath() + "-journal").delete() | new File(file.getPath() + "-shm").delete() | new File(file.getPath() + "-wal").delete();
            StringBuilder sb = new StringBuilder();
            sb.append(file.getPath());
            sb.append("-wipecheck");
            new File(sb.toString()).delete();
            File dir = file.getParentFile();
            if (dir != null) {
                final String prefix = file.getName() + "-mj";
                File[] files = dir.listFiles(new FileFilter() {
                    /* class android.database.sqlite.SQLiteDatabase.AnonymousClass1 */

                    @Override // java.io.FileFilter
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

    @UnsupportedAppUsage
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
        } catch (RuntimeException ex) {
            if (SQLiteDatabaseCorruptException.isCorruptException(ex)) {
                Log.e(TAG, "Database corruption detected in open()", ex);
                onCorruption();
                openInner();
                return;
            }
            throw ex;
        } catch (SQLiteException ex2) {
            Log.e(TAG, "Failed to open database '" + getLabel() + "'.", ex2);
            close();
            throw ex2;
        }
    }

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
        if (this.isJankMonitor) {
            begin = SystemClock.uptimeMillis();
        }
        acquireReference();
        try {
            return new SQLiteDirectCursorDriver(this, sql, editTable, cancellationSignal).query(cursorFactory != null ? cursorFactory : this.mCursorFactory, selectionArgs);
        } finally {
            releaseReference();
            if (this.isJankMonitor) {
                this.mJankDbStats.addQuery(SystemClock.uptimeMillis() - begin, editTable, this.jankDbname);
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

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00ea  */
    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm) {
        Throwable th;
        Object[] bindArgs;
        long begin = 0;
        if (this.isJankMonitor) {
            begin = SystemClock.uptimeMillis();
        }
        acquireReference();
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT");
            sql.append(CONFLICT_VALUES[conflictAlgorithm]);
            sql.append(" INTO ");
            sql.append(table);
            sql.append('(');
            int size = (initialValues == null || initialValues.isEmpty()) ? 0 : initialValues.size();
            if (size > 0) {
                Object[] bindArgs2 = new Object[size];
                int i = 0;
                for (String colName : initialValues.keySet()) {
                    sql.append(i > 0 ? SmsManager.REGEX_PREFIX_DELIMITER : "");
                    sql.append(colName);
                    bindArgs2[i] = initialValues.get(colName);
                    i++;
                }
                sql.append(')');
                sql.append(" VALUES (");
                int i2 = 0;
                while (i2 < size) {
                    sql.append(i2 > 0 ? ",?" : "?");
                    i2++;
                }
                bindArgs = bindArgs2;
            } else {
                StringBuilder sb = new StringBuilder();
                try {
                    sb.append(nullColumnHack);
                    sb.append(") VALUES (NULL");
                    sql.append(sb.toString());
                    bindArgs = null;
                } catch (Throwable th2) {
                    th = th2;
                    releaseReference();
                    if (this.isJankMonitor) {
                    }
                    throw th;
                }
            }
            sql.append(')');
            SQLiteStatement statement = new SQLiteStatement(this, sql.toString(), bindArgs);
            try {
                long id = statement.executeInsert();
                if (this.mHwSQLiteDatabase != null) {
                    this.mHwSQLiteDatabase.triggerAddingIndex(this, table, id);
                }
                statement.close();
                releaseReference();
                if (this.isJankMonitor) {
                    this.mJankDbStats.addInsert(SystemClock.uptimeMillis() - begin, table, this.jankDbname);
                }
                return id;
            } catch (Throwable th3) {
                statement.close();
                throw th3;
            }
        } catch (Throwable th4) {
            th = th4;
            releaseReference();
            if (this.isJankMonitor) {
                this.mJankDbStats.addInsert(SystemClock.uptimeMillis() - begin, table, this.jankDbname);
            }
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public int delete(String table, String whereClause, String[] whereArgs) {
        String str;
        long begin = 0;
        if (this.isJankMonitor) {
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
            SQLiteStatement statement = new SQLiteStatement(this, sb.toString(), whereArgs);
            try {
                if (this.mHwSQLiteDatabase != null) {
                    this.mHwSQLiteDatabase.triggerUpdatingOrDeletingIndex(this, table, whereClause, whereArgs, 2);
                }
                int executeUpdateDelete = statement.executeUpdateDelete();
                statement.close();
                return executeUpdateDelete;
            } catch (Throwable th) {
                statement.close();
                throw th;
            }
        } finally {
            releaseReference();
            if (this.isJankMonitor) {
                this.mJankDbStats.addDelete(SystemClock.uptimeMillis() - begin, table, this.jankDbname);
            }
        }
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        IHwSQLiteDatabase iHwSQLiteDatabase = this.mHwSQLiteDatabase;
        if (iHwSQLiteDatabase != null) {
            iHwSQLiteDatabase.triggerUpdatingOrDeletingIndex(this, table, whereClause, whereArgs, 1);
        }
        return updateWithOnConflict(table, values, whereClause, whereArgs, 0);
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00d5  */
    public int updateWithOnConflict(String table, ContentValues values, String whereClause, String[] whereArgs, int conflictAlgorithm) {
        Throwable th;
        long begin = 0;
        if (this.isJankMonitor) {
            begin = SystemClock.uptimeMillis();
        }
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Empty values");
        }
        acquireReference();
        try {
            StringBuilder sql = new StringBuilder(120);
            sql.append("UPDATE ");
            sql.append(CONFLICT_VALUES[conflictAlgorithm]);
            sql.append(table);
            sql.append(" SET ");
            int setValuesSize = values.size();
            int bindArgsSize = whereArgs == null ? setValuesSize : whereArgs.length + setValuesSize;
            Object[] bindArgs = new Object[bindArgsSize];
            int i = 0;
            for (String colName : values.keySet()) {
                sql.append(i > 0 ? SmsManager.REGEX_PREFIX_DELIMITER : "");
                sql.append(colName);
                bindArgs[i] = values.get(colName);
                sql.append("=?");
                i++;
            }
            if (whereArgs != null) {
                for (int i2 = setValuesSize; i2 < bindArgsSize; i2++) {
                    bindArgs[i2] = whereArgs[i2 - setValuesSize];
                }
            }
            if (!TextUtils.isEmpty(whereClause)) {
                sql.append(" WHERE ");
                try {
                    sql.append(whereClause);
                } catch (Throwable th2) {
                    th = th2;
                    releaseReference();
                    if (this.isJankMonitor) {
                        this.mJankDbStats.addUpdate(SystemClock.uptimeMillis() - begin, table, this.jankDbname);
                    }
                    throw th;
                }
            }
            SQLiteStatement statement = new SQLiteStatement(this, sql.toString(), bindArgs);
            try {
                int executeUpdateDelete = statement.executeUpdateDelete();
                statement.close();
                releaseReference();
                if (this.isJankMonitor) {
                    this.mJankDbStats.addUpdate(SystemClock.uptimeMillis() - begin, table, this.jankDbname);
                }
                return executeUpdateDelete;
            } catch (Throwable th3) {
                statement.close();
                throw th3;
            }
        } catch (Throwable th4) {
            th = th4;
            releaseReference();
            if (this.isJankMonitor) {
            }
            throw th;
        }
    }

    public void execSQL(String sql) throws SQLException {
        executeSql(sql, null);
    }

    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        if (bindArgs != null) {
            IHwSQLiteDatabase iHwSQLiteDatabase = this.mHwSQLiteDatabase;
            if (iHwSQLiteDatabase != null) {
                iHwSQLiteDatabase.triggerSQLIndex(this, sql, bindArgs);
            }
            executeSql(sql, bindArgs);
            return;
        }
        throw new IllegalArgumentException("Empty bindArgs");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005c, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0061, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0062, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0065, code lost:
        throw r6;
     */
    public int executeSql(String sql, Object[] bindArgs) throws SQLException {
        long begin = 0;
        if (this.isJankMonitor) {
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
                SQLiteStatement statement = new SQLiteStatement(this, sql, bindArgs);
                int executeUpdateDelete = statement.executeUpdateDelete();
                statement.close();
                return executeUpdateDelete;
            } finally {
                if (statementType == 8) {
                    this.mConnectionPoolLocked.closeAvailableNonPrimaryConnectionsAndLogExceptions();
                }
            }
        } finally {
            releaseReference();
            if (this.isJankMonitor) {
                this.mJankDbStats.addExecsql(SystemClock.uptimeMillis() - begin, "", this.jankDbname);
            }
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

    public boolean enableWriteAheadLogging() {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            boolean isDefaultWalEnabled = this.mConfigurationLocked.defaultWALEnabled;
            boolean isExplicitWalEnabled = this.mConfigurationLocked.explicitWALEnabled;
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
            } else if (this.mHasAttachedDbsLocked) {
                if (Log.isLoggable(TAG, 3)) {
                    Log.d(TAG, "this database: " + this.mConfigurationLocked.label + " has attached databases. can't  enable WAL.");
                }
                return false;
            } else {
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
                        this.mConfigurationLocked.defaultWALEnabled = isDefaultWalEnabled;
                        this.mConfigurationLocked.explicitWALEnabled = isExplicitWalEnabled;
                    }
                    throw ex;
                }
            }
        }
    }

    public void disableWriteAheadLogging() {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            boolean isDefaultWalEnabled = this.mConfigurationLocked.defaultWALEnabled;
            boolean isExplicitWalEnabled = this.mConfigurationLocked.explicitWALEnabled;
            int oldFlags = this.mConfigurationLocked.openFlags;
            boolean compatibilityWalEnabled = true;
            boolean walEnabled = (536870912 & oldFlags) != 0;
            if ((Integer.MIN_VALUE & oldFlags) == 0) {
                compatibilityWalEnabled = false;
            }
            if (walEnabled || compatibilityWalEnabled) {
                this.mConfigurationLocked.openFlags &= -536870913;
                if (this.mConfigurationLocked.configurationEnhancement) {
                    this.mConfigurationLocked.defaultWALEnabled = false;
                    this.mConfigurationLocked.explicitWALEnabled = false;
                }
                this.mConfigurationLocked.openFlags &= Integer.MAX_VALUE;
                try {
                    this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
                } catch (RuntimeException ex) {
                    this.mConfigurationLocked.openFlags = oldFlags;
                    if (this.mConfigurationLocked.configurationEnhancement) {
                        this.mConfigurationLocked.defaultWALEnabled = isDefaultWalEnabled;
                        this.mConfigurationLocked.explicitWALEnabled = isExplicitWalEnabled;
                    }
                    throw ex;
                }
            }
        }
    }

    public boolean isWriteAheadLoggingEnabled() {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            boolean z = true;
            if (this.mConfigurationLocked.configurationEnhancement) {
                if ((this.mConfigurationLocked.openFlags & 536870912) == 0 || !this.mConfigurationLocked.explicitWALEnabled) {
                    z = false;
                }
                return z;
            }
            if ((this.mConfigurationLocked.openFlags & 536870912) == 0) {
                z = false;
            }
            return z;
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

    @UnsupportedAppUsage
    private void collectDbStats(ArrayList<SQLiteDebug.DbStats> dbStatsList) {
        synchronized (this.mLock) {
            if (this.mConnectionPoolLocked != null) {
                this.mConnectionPoolLocked.collectDbStats(dbStatsList);
            }
        }
    }

    @UnsupportedAppUsage
    private static ArrayList<SQLiteDatabase> getActiveDatabases() {
        ArrayList<SQLiteDatabase> databases = new ArrayList<>();
        synchronized (sActiveDatabases) {
            databases.addAll(sActiveDatabases.keySet());
        }
        return databases;
    }

    static void dumpAll(Printer printer, boolean verbose, boolean isSystem) {
        ArraySet<String> directories = new ArraySet<>();
        Iterator<SQLiteDatabase> it = getActiveDatabases().iterator();
        while (it.hasNext()) {
            it.next().dump(printer, verbose, isSystem, directories);
        }
        if (directories.size() > 0) {
            String[] dirs = (String[]) directories.toArray(new String[directories.size()]);
            Arrays.sort(dirs);
            for (String dir : dirs) {
                dumpDatabaseDirectory(printer, new File(dir), isSystem);
            }
        }
    }

    private void dump(Printer printer, boolean verbose, boolean isSystem, ArraySet directories) {
        synchronized (this.mLock) {
            if (this.mConnectionPoolLocked != null) {
                printer.println("");
                this.mConnectionPoolLocked.dump(printer, verbose, directories);
            }
        }
    }

    private static void dumpDatabaseDirectory(Printer pw, File dir, boolean isSystem) {
        pw.println("");
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            pw.println("  [none]");
            return;
        }
        Arrays.sort(files, $$Lambda$SQLiteDatabase$1FsSJH2q7x3eeDFXCAu9l4piDsE.INSTANCE);
        for (File f : files) {
            if (isSystem) {
                String name = f.getName();
                if (!name.endsWith(".db") && !name.endsWith(".db-wal") && !name.endsWith(".db-journal") && name.endsWith("-wipecheck")) {
                }
            }
        }
    }

    public List<Pair<String, String>> getAttachedDbs() {
        ArrayList<Pair<String, String>> attachedDbs = new ArrayList<>();
        synchronized (this.mLock) {
            if (this.mConnectionPoolLocked == null) {
                return null;
            }
            if (!this.mHasAttachedDbsLocked) {
                attachedDbs.add(new Pair<>("main", this.mConfigurationLocked.path));
                return attachedDbs;
            }
            acquireReference();
        }
        Cursor c = null;
        try {
            Cursor c2 = rawQuery("pragma database_list;", null);
            while (c2.moveToNext()) {
                attachedDbs.add(new Pair<>(c2.getString(1), c2.getString(2)));
            }
            try {
                c2.close();
                return attachedDbs;
            } finally {
                releaseReference();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    public boolean isDatabaseIntegrityOk() {
        List<Pair<String, String>> attachedDbs;
        acquireReference();
        try {
            attachedDbs = getAttachedDbs();
            if (attachedDbs != null) {
                for (int i = 0; i < attachedDbs.size(); i++) {
                    Pair<String, String> p = attachedDbs.get(i);
                    SQLiteStatement prog = null;
                    try {
                        SQLiteStatement prog2 = compileStatement("PRAGMA " + ((String) p.first) + ".integrity_check(1);");
                        String rslt = prog2.simpleQueryForString();
                        if (!rslt.equalsIgnoreCase("ok")) {
                            Log.e(TAG, "PRAGMA integrity_check on " + ((String) p.second) + " returned: " + rslt);
                            prog2.close();
                            releaseReference();
                            return false;
                        }
                        prog2.close();
                    } catch (Throwable th) {
                        if (0 != 0) {
                            prog.close();
                        }
                        throw th;
                    }
                }
                releaseReference();
                return true;
            }
            throw new IllegalStateException("databaselist for: " + getPath() + " couldn't be retrieved. probably because the database is closed");
        } catch (SQLiteException e) {
            attachedDbs = new ArrayList<>();
            attachedDbs.add(new Pair<>("main", getPath()));
        } catch (Throwable th2) {
            releaseReference();
            throw th2;
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return "SQLiteDatabase: " + getPath();
    }

    private void throwIfNotOpenLocked() {
        if (this.mConnectionPoolLocked == null) {
            throw new IllegalStateException("The database '" + this.mConfigurationLocked.label + "' is not open.");
        }
    }

    public static final class OpenParams {
        private final CursorFactory mCursorFactory;
        private final DatabaseErrorHandler mErrorHandler;
        private final long mIdleConnectionTimeout;
        private final String mJournalMode;
        private final int mLookasideSlotCount;
        private final int mLookasideSlotSize;
        private final int mOpenFlags;
        private final String mSyncMode;

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
                boolean z = true;
                Preconditions.checkArgument(slotSize >= 0, "lookasideSlotCount cannot be negative");
                Preconditions.checkArgument(slotCount >= 0, "lookasideSlotSize cannot be negative");
                if ((slotSize <= 0 || slotCount <= 0) && !(slotCount == 0 && slotSize == 0)) {
                    z = false;
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

            @Deprecated
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
                return new OpenParams(this.mOpenFlags, this.mCursorFactory, this.mErrorHandler, this.mLookasideSlotSize, this.mLookasideSlotCount, this.mIdleConnectionTimeout, this.mJournalMode, this.mSyncMode);
            }
        }
    }

    public static void wipeDetected(String filename, String reason) {
        StringBuilder sb = new StringBuilder();
        sb.append("DB wipe detected: package=");
        sb.append(ActivityThread.currentPackageName());
        sb.append(" reason=");
        sb.append(reason);
        sb.append(" file=");
        sb.append(filename);
        sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        sb.append(getFileTimestamps(filename));
        sb.append(" checkfile ");
        sb.append(getFileTimestamps(filename + "-wipecheck"));
        wtfAsSystemServer(TAG, sb.toString(), new Throwable("STACKTRACE"));
    }

    public static String getFileTimestamps(String path) {
        try {
            BasicFileAttributes attr = Files.readAttributes(FileSystems.getDefault().getPath(path, new String[0]), BasicFileAttributes.class, new LinkOption[0]);
            return "ctime=" + attr.creationTime() + " mtime=" + attr.lastModifiedTime() + " atime=" + attr.lastAccessTime();
        } catch (IOException e) {
            return "[unable to obtain timestamp]";
        }
    }

    static void wtfAsSystemServer(String tag, String message, Throwable stacktrace) {
        Log.e(tag, message, stacktrace);
        ContentResolver.onDbCorruption(tag, message, stacktrace);
    }

    public void enableExclusiveConnection(boolean isEnabled, SQLiteDatabaseEx.DatabaseConnectionExclusiveHandler connectionExclusiveHandler) {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            this.mEnableExclusiveConnection = isEnabled;
            this.mConnectionExclusiveHandler = connectionExclusiveHandler;
            if (this.mConnectionPoolLocked != null) {
                this.mConnectionPoolLocked.setExclusiveConnectionEnabled(this.mEnableExclusiveConnection);
            }
        }
    }

    private boolean checkConnectionExclusiveHandler() {
        SQLiteDatabaseEx.DatabaseConnectionExclusiveHandler databaseConnectionExclusiveHandler = this.mConnectionExclusiveHandler;
        if (databaseConnectionExclusiveHandler != null) {
            return databaseConnectionExclusiveHandler.onConnectionExclusive();
        }
        return false;
    }
}
