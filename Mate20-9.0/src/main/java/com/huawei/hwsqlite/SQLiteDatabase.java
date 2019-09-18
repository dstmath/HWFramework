package com.huawei.hwsqlite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.SQLException;
import android.os.CancellationSignal;
import android.os.Looper;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.util.Pair;
import android.util.Printer;
import com.huawei.android.smcs.SmartTrimProcessEvent;
import com.huawei.android.util.JlogConstantsEx;
import com.huawei.hwsqlite.SQLiteDebug;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

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
    public static final int DETECT_DATABASE_FILE_DELETION = 16777216;
    public static final int ENABLE_DATABASE_ENCRYPTION = 1073741824;
    public static final int ENABLE_WRITE_AHEAD_LOGGING = 536870912;
    private static final int EVENT_DB_CORRUPT = 75004;
    public static final int MAX_SQL_CACHE_SIZE = 100;
    public static final int NO_LOCALIZED_COLLATORS = 16;
    public static final int OPEN_READONLY = 1;
    public static final int OPEN_READWRITE = 0;
    private static final int OPEN_READ_MASK = 1;
    public static final int SQLITE_MAX_LIKE_PATTERN_LENGTH = 50000;
    private static final String TAG = "SQLiteDatabase";
    public static final int THROW_EXCEPTION_ON_OPEN_CORRUPTION = Integer.MIN_VALUE;
    private static WeakHashMap<SQLiteDatabase, Object> sActiveDatabases = new WeakHashMap<>();
    private final SQLiteCloseGuard mCloseGuardLocked = SQLiteCloseGuard.get();
    private final SQLiteDatabaseConfiguration mConfigurationLocked;
    private SQLiteConnectionPool mConnectionPoolLocked;
    private final CursorFactory mCursorFactory;
    /* access modifiers changed from: private */
    public byte[] mEncryptKey;
    private final SQLiteErrorHandler mErrorHandler;
    private boolean mHasAttachedDbsLocked;
    private final Object mLock = new Object();
    private final CursorFactory mStepCursorFactory = new SQLiteStepCursorFactory();
    private final ThreadLocal<SQLiteSession> mThreadSession = new ThreadLocal<SQLiteSession>() {
        /* access modifiers changed from: protected */
        public SQLiteSession initialValue() {
            return SQLiteDatabase.this.createSession();
        }
    };

    public interface CursorFactory {
        Cursor newCursor(SQLiteDatabase sQLiteDatabase, SQLiteCursorDriver sQLiteCursorDriver, String str, SQLiteQuery sQLiteQuery);
    }

    public interface CustomFunction {
        void callback(String[] strArr);
    }

    private class EncryptKeyLoader implements SQLiteEncryptKeyLoader {
        private EncryptKeyLoader() {
        }

        public byte[] getEncryptKey() {
            if (SQLiteDatabase.this.mEncryptKey != null) {
                return Arrays.copyOf(SQLiteDatabase.this.mEncryptKey, SQLiteDatabase.this.mEncryptKey.length);
            }
            return new byte[0];
        }
    }

    static {
        CursorWindow window = null;
        try {
            System.loadLibrary("hwsqlite_jni");
            window = new CursorWindow("tmp");
            window.close();
            window.close();
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "loadLibrary hwsqlite_jni failed");
            throw e;
        } catch (Throwable th) {
            if (window != null) {
                window.close();
            }
            throw th;
        }
    }

    private SQLiteDatabase(String path, int openFlags, CursorFactory cursorFactory, SQLiteErrorHandler errorHandler, byte[] encryptKey) {
        this.mCursorFactory = cursorFactory;
        this.mErrorHandler = errorHandler != null ? errorHandler : new SQLiteDefaultErrorHandler();
        this.mConfigurationLocked = new SQLiteDatabaseConfiguration(path, openFlags);
        if ((1073741824 & openFlags) == 0) {
            return;
        }
        if (encryptKey != null) {
            this.mEncryptKey = Arrays.copyOf(encryptKey, encryptKey.length);
            this.mConfigurationLocked.encryptKeyLoader = new EncryptKeyLoader();
            return;
        }
        throw new IllegalArgumentException("Empty encrypt key");
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
        return openDatabase(path, factory, flags, null, null);
    }

    public static SQLiteDatabase openDatabase(String path, CursorFactory factory, int flags, SQLiteErrorHandler errorHandler) {
        return openDatabase(path, factory, flags, errorHandler, null);
    }

    public static SQLiteDatabase openDatabase(String path, CursorFactory factory, int flags, SQLiteErrorHandler errorHandler, byte[] encryptKey) {
        SQLiteDatabase db = new SQLiteDatabase(path, flags, factory, errorHandler, encryptKey);
        db.open();
        return db;
    }

    public static SQLiteDatabase openOrCreateDatabase(File file, CursorFactory factory) {
        return openOrCreateDatabase(file.getPath(), factory);
    }

    public static SQLiteDatabase openOrCreateDatabase(String path, CursorFactory factory) {
        return openDatabase(path, factory, 268435456, null, null);
    }

    public static SQLiteDatabase openOrCreateDatabase(String path, CursorFactory factory, SQLiteErrorHandler errorHandler) {
        return openDatabase(path, factory, 268435456, errorHandler, null);
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

    private boolean isDatabaseEncrypted() {
        if (this.mConfigurationLocked.encryptKeyLoader != null) {
            return true;
        }
        int length = this.mConfigurationLocked.attachedAlias.size();
        for (int i = 0; i < length; i++) {
            SQLiteAttached attached = this.mConfigurationLocked.attachedAlias.get(i);
            if (attached.encryptKey != null && attached.encryptKey.length > 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean reopenOnOpenCorruption() {
        if (!isDatabaseEncrypted() && (this.mConfigurationLocked.openFlags & Integer.MIN_VALUE) == 0) {
            return true;
        }
        return false;
    }

    private void open() {
        if (!reopenOnOpenCorruption()) {
            try {
                openInner();
            } catch (SQLiteDatabaseCorruptException ex) {
                onCorruption();
                throw ex;
            }
        } else {
            try {
                openInner();
            } catch (SQLiteDatabaseCorruptException e) {
                try {
                    onCorruption();
                    openInner();
                } catch (SQLiteException ex2) {
                    Log.e(TAG, "Failed to open database '" + getLabel() + "'.", ex2);
                    close();
                    throw ex2;
                }
            }
        }
    }

    /* JADX WARNING: CFG modification limit reached, blocks count: 120 */
    private void openInner() {
        synchronized (this.mLock) {
            this.mConnectionPoolLocked = SQLiteConnectionPool.open(this.mConfigurationLocked);
            this.mCloseGuardLocked.open("close");
        }
        synchronized (sActiveDatabases) {
            sActiveDatabases.put(this, null);
        }
    }

    public static SQLiteDatabase create(CursorFactory factory) {
        return openDatabase(SQLiteDatabaseConfiguration.MEMORY_DB_PATH, factory, 268435456);
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
        return Long.valueOf(SQLiteDatabaseUtils.longForQuery(this, "PRAGMA user_version;", null)).intValue();
    }

    public void setVersion(int version) {
        execSQL("PRAGMA user_version = " + version);
    }

    public long getMaximumSize() {
        return getPageSize() * SQLiteDatabaseUtils.longForQuery(this, "PRAGMA max_page_count;", null);
    }

    public long setMaximumSize(long numBytes) {
        long pageSize = getPageSize();
        long numPages = numBytes / pageSize;
        if (numBytes % pageSize != 0) {
            numPages++;
        }
        return SQLiteDatabaseUtils.longForQuery(this, "PRAGMA max_page_count = " + numPages, null) * pageSize;
    }

    public long getPageSize() {
        return SQLiteDatabaseUtils.longForQuery(this, "PRAGMA page_size;", null);
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

    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, boolean stepQuery) {
        SQLiteDatabase sQLiteDatabase;
        CursorFactory cursorFactory;
        if (stepQuery) {
            sQLiteDatabase = this;
            cursorFactory = sQLiteDatabase.mStepCursorFactory;
        } else {
            sQLiteDatabase = this;
            cursorFactory = null;
        }
        return sQLiteDatabase.queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, null);
    }

    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, CancellationSignal cancellationSignal) {
        return queryWithFactory(null, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal);
    }

    public Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, CancellationSignal cancellationSignal, boolean stepQuery) {
        SQLiteDatabase sQLiteDatabase;
        CursorFactory cursorFactory;
        if (stepQuery) {
            sQLiteDatabase = this;
            cursorFactory = sQLiteDatabase.mStepCursorFactory;
        } else {
            sQLiteDatabase = this;
            cursorFactory = null;
        }
        return sQLiteDatabase.queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal);
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
        return query(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, (String) null);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, boolean stepQuery) {
        return query(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, (String) null, stepQuery);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return query(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, boolean stepQuery) {
        return query(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, stepQuery);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return rawQueryWithFactory(null, sql, selectionArgs, null, null);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs, boolean stepQuery) {
        return rawQueryWithFactory(stepQuery ? this.mStepCursorFactory : null, sql, selectionArgs, null, null);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs, CancellationSignal cancellationSignal) {
        return rawQueryWithFactory(null, sql, selectionArgs, null, cancellationSignal);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs, CancellationSignal cancellationSignal, boolean stepQuery) {
        return rawQueryWithFactory(stepQuery ? this.mStepCursorFactory : null, sql, selectionArgs, null, cancellationSignal);
    }

    public Cursor rawSelect(String sql, String[] selectionArgs, CancellationSignal cancellationSignal, boolean stepQuery) {
        int type = SQLiteDatabaseUtils.getSqlStatementType(sql);
        if (type == 1 || type == 7) {
            return rawQueryWithFactory(stepQuery ? this.mStepCursorFactory : null, sql, selectionArgs, null, cancellationSignal);
        }
        throw new SQLiteNotSupportException("Only select/pragma statement is supported.");
    }

    public Cursor rawQueryWithFactory(CursorFactory cursorFactory, String sql, String[] selectionArgs, String editTable) {
        return rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable, null);
    }

    public Cursor rawQueryWithFactory(CursorFactory cursorFactory, String sql, String[] selectionArgs, String editTable, CancellationSignal cancellationSignal) {
        acquireReference();
        try {
            return new SQLiteDirectCursorDriver(this, sql, editTable, cancellationSignal).query(cursorFactory != null ? cursorFactory : this.mCursorFactory, selectionArgs);
        } finally {
            releaseReference();
        }
    }

    public long insert(String table, String nullColumnHack, ContentValues values) {
        try {
            return insertWithOnConflict(table, nullColumnHack, values, 0);
        } catch (SQLException e) {
            Log.e(TAG, "Error inserting " + values, e);
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
            Log.e(TAG, "Error inserting " + initialValues, e);
            return -1;
        }
    }

    public long replaceOrThrow(String table, String nullColumnHack, ContentValues initialValues) throws SQLException {
        return insertWithOnConflict(table, nullColumnHack, initialValues, 5);
    }

    @SuppressLint({"AvoidInHardConnectInStrings"})
    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm) {
        SQLiteStatement statement;
        acquireReference();
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT");
            sql.append(CONFLICT_VALUES[conflictAlgorithm]);
            sql.append(" INTO ");
            sql.append(table);
            sql.append('(');
            Object[] bindArgs = null;
            int size = (initialValues == null || initialValues.size() <= 0) ? 0 : initialValues.size();
            if (size > 0) {
                bindArgs = new Object[size];
                int i = 0;
                for (String colName : initialValues.keySet()) {
                    sql.append(i > 0 ? SmartTrimProcessEvent.ST_EVENT_STRING_TOKEN : "");
                    sql.append(colName);
                    bindArgs[i] = initialValues.get(colName);
                    i++;
                }
                sql.append(')');
                sql.append(" VALUES (");
                int i2 = 0;
                while (i2 < size) {
                    sql.append(i2 > 0 ? ",?" : "?");
                    i2++;
                }
            } else {
                sql.append(nullColumnHack + ") VALUES (NULL");
            }
            sql.append(')');
            statement = new SQLiteStatement(this, sql.toString(), bindArgs);
            long executeInsert = statement.executeInsert();
            statement.close();
            releaseReference();
            return executeInsert;
        } catch (Throwable th) {
            releaseReference();
            throw th;
        }
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        SQLiteStatement statement;
        String str;
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
            int executeUpdateDelete = statement.executeUpdateDelete();
            statement.close();
            releaseReference();
            return executeUpdateDelete;
        } catch (Throwable th) {
            releaseReference();
            throw th;
        }
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return updateWithOnConflict(table, values, whereClause, whereArgs, 0);
    }

    public int updateWithOnConflict(String table, ContentValues values, String whereClause, String[] whereArgs, int conflictAlgorithm) {
        SQLiteStatement statement;
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("Empty values");
        }
        acquireReference();
        try {
            StringBuilder sql = new StringBuilder(JlogConstantsEx.JLID_DIALPAD_ONTOUCH_NOT_FIRST_DOWN);
            sql.append("UPDATE ");
            sql.append(CONFLICT_VALUES[conflictAlgorithm]);
            sql.append(table);
            sql.append(" SET ");
            int setValuesSize = values.size();
            int bindArgsSize = whereArgs == null ? setValuesSize : whereArgs.length + setValuesSize;
            Object[] bindArgs = new Object[bindArgsSize];
            int i = 0;
            for (String colName : values.keySet()) {
                sql.append(i > 0 ? SmartTrimProcessEvent.ST_EVENT_STRING_TOKEN : "");
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
                sql.append(whereClause);
            }
            statement = new SQLiteStatement(this, sql.toString(), bindArgs);
            int executeUpdateDelete = statement.executeUpdateDelete();
            statement.close();
            releaseReference();
            return executeUpdateDelete;
        } catch (Throwable th) {
            releaseReference();
            throw th;
        }
    }

    public void execSQL(String sql) throws SQLException {
        executeSql(sql, null);
    }

    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        if (bindArgs != null) {
            executeSql(sql, bindArgs);
            return;
        }
        throw new IllegalArgumentException("Empty bindArgs");
    }

    private int executeSql(String sql, Object[] bindArgs) throws SQLException {
        SQLiteStatement statement;
        acquireReference();
        try {
            if (SQLiteDatabaseUtils.getSqlStatementType(sql) == 3) {
                boolean disableWal = false;
                synchronized (this.mLock) {
                    if (!this.mHasAttachedDbsLocked) {
                        this.mHasAttachedDbsLocked = true;
                        disableWal = true;
                    }
                }
                if (disableWal) {
                    disableWriteAheadLogging();
                }
            }
            statement = new SQLiteStatement(this, sql, bindArgs);
            int executeUpdateDelete = statement.executeUpdateDelete();
            statement.close();
            releaseReference();
            return executeUpdateDelete;
        } catch (Throwable th) {
            releaseReference();
            throw th;
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

    public void changeEncryptKey(byte[] oldEncryptKey, byte[] newEncryptKey) throws SQLException {
        if (oldEncryptKey == null) {
            throw new IllegalArgumentException("Empty old encrypt key");
        } else if (newEncryptKey != null) {
            synchronized (this.mLock) {
                if (this.mEncryptKey == null) {
                    throw new IllegalStateException("Change encrypt key on a unencrypted database");
                } else if (!Arrays.equals(oldEncryptKey, this.mEncryptKey)) {
                    throw new IllegalStateException("Old encrypt key isn't correct when change encrypt key");
                } else if (isOpen()) {
                    throwIfNotOpenLocked();
                    try {
                        this.mEncryptKey = Arrays.copyOf(newEncryptKey, newEncryptKey.length);
                        this.mConnectionPoolLocked.changeEncryptKey(this.mConfigurationLocked.encryptKeyLoader);
                    } catch (RuntimeException ex) {
                        Log.e(TAG, "Failed to change the encryption key of database (" + this.mConfigurationLocked.path + ").");
                        throw ex;
                    }
                } else {
                    throw new IllegalStateException("Change encrypt key on a no-open database");
                }
            }
        } else {
            throw new IllegalArgumentException("Empty new encrypt key");
        }
    }

    public void addAttachAlias(String alias, String path, byte[] key) throws SQLException {
        if (alias == null || alias.length() == 0) {
            throw new IllegalArgumentException("Alias name must not be empty");
        }
        if (path == null) {
            path = SQLiteDatabaseConfiguration.MEMORY_DB_PATH;
        }
        String path2 = path;
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            byte[] keyCopy = null;
            if (key != null) {
                keyCopy = Arrays.copyOf(key, key.length);
            }
            SQLiteAttached attached = new SQLiteAttached(path2, alias, keyCopy);
            this.mConnectionPoolLocked.addAttachAlias(attached);
            this.mConfigurationLocked.addAttachAlias(attached);
        }
    }

    public void removeAttachAlias(String alias) {
        if (alias == null || alias.length() == 0) {
            throw new IllegalArgumentException("Alias name must not be empty");
        }
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            this.mConnectionPoolLocked.removeAttachedAlias(alias);
            this.mConfigurationLocked.removeAttachAlias(alias);
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0059, code lost:
        return false;
     */
    public boolean enableWriteAheadLogging() {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            if ((this.mConfigurationLocked.openFlags & 536870912) != 0) {
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
                try {
                    this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
                    return true;
                } catch (RuntimeException ex) {
                    this.mConfigurationLocked.openFlags &= -536870913;
                    throw ex;
                }
            } else if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "this database: " + this.mConfigurationLocked.label + " has attached databases. can't  enable WAL.");
            }
        }
    }

    public boolean setWALConnections(int maxConnections) {
        if (maxConnections <= 1) {
            Log.i(TAG, "Connection count must be greater than 1 in WAL mode.");
            return false;
        }
        synchronized (this.mLock) {
            if ((this.mConfigurationLocked.openFlags & 536870912) == 0) {
                Log.i(TAG, "Multi-connections can only be enabled in WAL mode.");
                return false;
            }
            this.mConfigurationLocked.maxConnectionCount = maxConnections;
            this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
            return true;
        }
    }

    public void disableWriteAheadLogging() {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            if ((this.mConfigurationLocked.openFlags & 536870912) != 0) {
                this.mConfigurationLocked.openFlags &= -536870913;
                try {
                    this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
                } catch (RuntimeException ex) {
                    SQLiteDatabaseConfiguration sQLiteDatabaseConfiguration = this.mConfigurationLocked;
                    sQLiteDatabaseConfiguration.openFlags = 536870912 | sQLiteDatabaseConfiguration.openFlags;
                    throw ex;
                }
            }
        }
    }

    public boolean isWriteAheadLoggingEnabled() {
        boolean z;
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            z = (this.mConfigurationLocked.openFlags & 536870912) != 0;
        }
        return z;
    }

    @SuppressLint({"PreferForInArrayList"})
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

    @SuppressLint({"PreferForInArrayList"})
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

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0027, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        r1 = rawQuery("pragma database_list;", null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0033, code lost:
        if (r1.moveToNext() == false) goto L_0x0048;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0035, code lost:
        r0.add(new android.util.Pair(r1.getString(1), r1.getString(2)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0048, code lost:
        if (r1 == null) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004d, code lost:
        releaseReference();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0051, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0052, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0053, code lost:
        if (r1 != null) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0059, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x005b, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005c, code lost:
        releaseReference();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x005f, code lost:
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

    @SuppressLint({"AvoidMethodInForLoops"})
    public boolean isDatabaseIntegrityOk() {
        List<Pair<String, String>> attachedDbs;
        SQLiteStatement prog;
        acquireReference();
        try {
            attachedDbs = getAttachedDbs();
            if (attachedDbs != null) {
                int i = 0;
                while (i < attachedDbs.size()) {
                    Pair<String, String> p = attachedDbs.get(i);
                    prog = null;
                    prog = compileStatement("PRAGMA " + ((String) p.first) + ".integrity_check(1);");
                    String rslt = prog.simpleQueryForString();
                    if (rslt == null) {
                        Log.e(TAG, "PRAGMA integrity_check on " + ((String) p.second) + " failed");
                        if (prog != null) {
                            prog.close();
                        }
                        releaseReference();
                        return false;
                    } else if (!rslt.equalsIgnoreCase("ok")) {
                        Log.e(TAG, "PRAGMA integrity_check on " + ((String) p.second) + " returned: " + rslt);
                        if (prog != null) {
                            prog.close();
                        }
                        releaseReference();
                        return false;
                    } else {
                        if (prog != null) {
                            prog.close();
                        }
                        i++;
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
}
