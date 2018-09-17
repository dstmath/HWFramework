package android.database.sqlite;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.DefaultDatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabaseEx.DatabaseConnectionExclusiveHandler;
import android.database.sqlite.SQLiteDebug.DbStats;
import android.net.ProxyInfo;
import android.os.CancellationSignal;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.util.Pair;
import android.util.Printer;
import com.huawei.indexsearch.IndexSearchParser;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

public final class SQLiteDatabase extends SQLiteClosable {
    static final /* synthetic */ boolean -assertionsDisabled = (SQLiteDatabase.class.desiredAssertionStatus() ^ 1);
    public static final int CONFLICT_ABORT = 2;
    public static final int CONFLICT_FAIL = 3;
    public static final int CONFLICT_IGNORE = 4;
    public static final int CONFLICT_NONE = 0;
    public static final int CONFLICT_REPLACE = 5;
    public static final int CONFLICT_ROLLBACK = 1;
    private static final String[] CONFLICT_VALUES = new String[]{ProxyInfo.LOCAL_EXCL_LIST, " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE "};
    public static final int CREATE_IF_NECESSARY = 268435456;
    public static final int ENABLE_WRITE_AHEAD_LOGGING = 536870912;
    private static final int EVENT_DB_CORRUPT = 75004;
    public static final int MAX_SQL_CACHE_SIZE = 100;
    public static final int NO_LOCALIZED_COLLATORS = 16;
    public static final int OPEN_READONLY = 1;
    public static final int OPEN_READWRITE = 0;
    private static final int OPEN_READ_MASK = 1;
    public static final int SQLITE_MAX_LIKE_PATTERN_LENGTH = 50000;
    private static final String TAG = "SQLiteDatabase";
    private static WeakHashMap<SQLiteDatabase, Object> sActiveDatabases = new WeakHashMap();
    private boolean bJankMonitor = true;
    private String jank_dbname = ProxyInfo.LOCAL_EXCL_LIST;
    private final CloseGuard mCloseGuardLocked = CloseGuard.get();
    private final SQLiteDatabaseConfiguration mConfigurationLocked;
    private DatabaseConnectionExclusiveHandler mConnectionExclusiveHandler;
    private SQLiteConnectionPool mConnectionPoolLocked;
    private final CursorFactory mCursorFactory;
    private boolean mEnableExclusiveConnection = false;
    private final DatabaseErrorHandler mErrorHandler;
    private boolean mHasAttachedDbsLocked;
    private IndexSearchParser mIndexSearchParser = null;
    private JankSqlite mJankDBStats = new JankSqlite();
    private final Object mLock = new Object();
    private final ThreadLocal<SQLiteSession> mThreadSession = new ThreadLocal<SQLiteSession>() {
        protected SQLiteSession initialValue() {
            return SQLiteDatabase.this.createSession();
        }
    };

    public interface CursorFactory {
        Cursor newCursor(SQLiteDatabase sQLiteDatabase, SQLiteCursorDriver sQLiteCursorDriver, String str, SQLiteQuery sQLiteQuery);
    }

    public interface CustomFunction {
        void callback(String[] strArr);
    }

    private SQLiteDatabase(String path, int openFlags, CursorFactory cursorFactory, DatabaseErrorHandler errorHandler) {
        this.mCursorFactory = cursorFactory;
        if (errorHandler == null) {
            errorHandler = new DefaultDatabaseErrorHandler();
        }
        this.mErrorHandler = errorHandler;
        this.mConfigurationLocked = new SQLiteDatabaseConfiguration(path, openFlags);
        this.jank_dbname = path;
        if (this.jank_dbname.equals("JankEventDb.db")) {
            this.bJankMonitor = false;
        }
        this.mIndexSearchParser = IndexSearchParser.getInstance();
    }

    protected void finalize() throws Throwable {
        try {
            dispose(true);
        } finally {
            super.finalize();
        }
    }

    protected void onAllReferencesReleased() {
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

    String getLabel() {
        String str;
        synchronized (this.mLock) {
            str = this.mConfigurationLocked.label;
        }
        return str;
    }

    void onCorruption() {
        EventLog.writeEvent(EVENT_DB_CORRUPT, getLabel());
        this.mErrorHandler.onCorruption(this);
    }

    SQLiteSession getThreadSession() {
        return (SQLiteSession) this.mThreadSession.get();
    }

    SQLiteSession createSession() {
        SQLiteConnectionPool pool;
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            pool = this.mConnectionPoolLocked;
        }
        return new SQLiteSession(pool);
    }

    int getThreadDefaultConnectionFlags(boolean readOnly) {
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
        if (looper == null || looper != Looper.getMainLooper()) {
            return false;
        }
        return true;
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
        acquireReference();
        try {
            int i;
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
            releaseReference();
        } catch (Throwable th) {
            releaseReference();
        }
    }

    private void handleTransMap() {
        if (this.mIndexSearchParser != null && getThreadSession().getTransMap().size() > 0) {
            ArrayList<Long> insertArgs = new ArrayList();
            ArrayList<Long> updateArgs = new ArrayList();
            ArrayList<Long> deleteArgs = new ArrayList();
            for (SQLInfo sqlinfo : getThreadSession().getTransMap().keySet()) {
                IndexSearchParser indexSearchParser = this.mIndexSearchParser;
                if (IndexSearchParser.isValidTable(sqlinfo.getTable())) {
                    switch (((Integer) getThreadSession().getTransMap().get(sqlinfo)).intValue()) {
                        case 0:
                            insertArgs.add(Long.valueOf(sqlinfo.getPrimaryKey()));
                            break;
                        case 1:
                            updateArgs.add(Long.valueOf(sqlinfo.getPrimaryKey()));
                            break;
                        case 2:
                            deleteArgs.add(Long.valueOf(sqlinfo.getPrimaryKey()));
                            break;
                        default:
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
            boolean hasTransaction = getThreadSession().hasTransaction();
            return hasTransaction;
        } finally {
            releaseReference();
        }
    }

    public boolean isDbLockedByCurrentThread() {
        acquireReference();
        try {
            boolean hasConnection = getThreadSession().hasConnection();
            return hasConnection;
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
            boolean yieldTransaction = getThreadSession().yieldTransaction(sleepAfterYieldDelay, throwIfUnsafe, null);
            return yieldTransaction;
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

    public static SQLiteDatabase openDatabase(String path, CursorFactory factory, int flags, DatabaseErrorHandler errorHandler) {
        SQLiteDatabase db = new SQLiteDatabase(path, flags, factory, errorHandler);
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
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        boolean deleted = ((file.delete() | new File(file.getPath() + "-journal").delete()) | new File(file.getPath() + "-shm").delete()) | new File(file.getPath() + "-wal").delete();
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

    public void reopenReadWrite() {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            if (isReadOnlyLocked()) {
                int oldOpenFlags = this.mConfigurationLocked.openFlags;
                this.mConfigurationLocked.openFlags = (this.mConfigurationLocked.openFlags & -2) | 0;
                try {
                    this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
                    return;
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

    private void openInner() {
        synchronized (this.mLock) {
            if (-assertionsDisabled || this.mConnectionPoolLocked == null) {
                this.mConnectionPoolLocked = SQLiteConnectionPool.open(this.mConfigurationLocked, this.mEnableExclusiveConnection);
                this.mCloseGuardLocked.open("close");
            } else {
                throw new AssertionError();
            }
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
        if (TextUtils.isEmpty(tables)) {
            throw new IllegalStateException("Invalid tables");
        }
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

    public SQLiteStatement compileStatement(String sql) throws SQLException {
        acquireReference();
        try {
            SQLiteStatement sQLiteStatement = new SQLiteStatement(this, sql, null);
            return sQLiteStatement;
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
            Cursor rawQueryWithFactory = rawQueryWithFactory(cursorFactory, SQLiteQueryBuilder.buildQueryString(distinct, table, columns, selection, groupBy, having, orderBy, limit), selectionArgs, findEditTable(table), cancellationSignal);
            return rawQueryWithFactory;
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
            SQLiteCursorDriver driver = new SQLiteDirectCursorDriver(this, sql, editTable, cancellationSignal);
            if (cursorFactory == null) {
                cursorFactory = this.mCursorFactory;
            }
            Cursor query = driver.query(cursorFactory, selectionArgs);
            return query;
        } finally {
            releaseReference();
            if (this.bJankMonitor) {
                this.mJankDBStats.addQuery(SystemClock.uptimeMillis() - begin, editTable, this.jank_dbname);
            }
        }
    }

    private Cursor queryForIndexSearch(String table, String whereClause, String[] whereArgs) {
        StringBuilder sql = new StringBuilder();
        if ("files".equals(table)) {
            sql.append("SELECT _id FROM ").append(table).append(" WHERE ").append(whereClause).append(" AND ").append("((mime_type='text/plain') OR (mime_type='text/html') OR (mime_type='text/htm') OR (mime_type = 'application/msword') OR (mime_type = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document') OR (mime_type = 'application/vnd.ms-excel') OR (mime_type = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') OR (mime_type = 'application/mspowerpoint') OR (mime_type = 'application/vnd.openxmlformats-officedocument.presentationml.presentation')) ");
        } else if ("Body".equals(table)) {
            sql.append("SELECT messageKey FROM Body WHERE ").append(whereClause);
        } else if ("Events".equals(table)) {
            sql.append("SELECT _id FROM ").append(table).append(" WHERE ").append(whereClause).append(" AND ").append("mutators IS NOT 'com.android.providers.contacts'");
        } else if ("Calendars".equals(table)) {
            if (whereArgs == null || whereArgs.length != 1) {
                sql.append("SELECT _id FROM ").append(table).append(" WHERE ").append(whereClause);
            } else {
                sql.append("SELECT _id FROM Events WHERE calendar_id IN (?)");
            }
        } else if ("Mailbox".equals(table)) {
            sql.append("SELECT _id FROM Message WHERE ").append(whereClause);
        } else if (!"fav_sms".equals(table)) {
            sql.append("SELECT _id FROM ").append(table).append(" WHERE ").append(whereClause);
        } else if (whereClause == null) {
            sql.append("SELECT _id FROM words WHERE table_to_use IS 8");
        } else {
            sql.append("SELECT _id FROM words WHERE ").append(whereClause.replace(DownloadManager.COLUMN_ID, "source_id")).append(" AND ").append("table_to_use IS 8");
        }
        return rawQuery(sql.toString(), whereArgs);
    }

    private void triggerUpdatingOrDeletingIndex(String table, String whereClause, String[] whereArgs, int operation) {
        if (this.mIndexSearchParser != null) {
            IndexSearchParser indexSearchParser = this.mIndexSearchParser;
            if (IndexSearchParser.isValidTable(table)) {
                Cursor c = null;
                try {
                    c = queryForIndexSearch(table, whereClause, whereArgs);
                    if (c == null || c.getCount() == 0) {
                        Log.v(TAG, "triggerBuildingIndex(): cursor is null or count is 0, return.");
                        if (c != null) {
                            c.close();
                        }
                        return;
                    }
                    if (getThreadSession().hasTransaction()) {
                        while (c.moveToNext()) {
                            String realTable;
                            if (table.equals("Body")) {
                                realTable = "Message";
                            } else {
                                realTable = table;
                            }
                            getThreadSession().insertTransMap(realTable, c.getLong(0), operation);
                        }
                    } else {
                        List<Long> ids = new ArrayList();
                        while (c.moveToNext()) {
                            ids.add(Long.valueOf(c.getLong(0)));
                        }
                        this.mIndexSearchParser.notifyIndexSearchService(ids, operation);
                    }
                    if (c != null) {
                        c.close();
                    }
                } catch (RuntimeException e) {
                    Log.e(TAG, "triggerUpdatingOrDeletingIndex(): RuntimeException");
                    if (c != null) {
                        c.close();
                    }
                } catch (Exception e2) {
                    Log.e(TAG, "triggerUpdatingOrDeletingIndex(): Exception");
                    if (c != null) {
                        c.close();
                    }
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }

    private void triggerDeletingCalendarAccounts(String sql, Object[] bindArgs) {
        if (this.mIndexSearchParser != null && "DELETE FROM Calendars WHERE account_name=? AND account_type=?".equals(sql)) {
            Cursor c1 = null;
            Cursor cursor = null;
            int i = 0;
            List<Long> eventsIds = new ArrayList();
            try {
                c1 = rawQuery("SELECT _id FROM Calendars where account_name=? AND account_type=?", (String[]) bindArgs);
                if (c1 != null && c1.getCount() > 0) {
                    String[] calendarIds = new String[c1.getCount()];
                    while (c1.moveToNext()) {
                        calendarIds[i] = c1.getString(0);
                        i++;
                    }
                    cursor = queryForIndexSearch("Events", "calendar_id IN (?)", calendarIds);
                    if (cursor != null && cursor.getCount() > 0) {
                        while (cursor.moveToNext()) {
                            eventsIds.add(Long.valueOf(cursor.getLong(0)));
                        }
                        this.mIndexSearchParser.notifyIndexSearchService(eventsIds, 2);
                    }
                }
                if (c1 != null) {
                    c1.close();
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (!eventsIds.isEmpty()) {
                    eventsIds.clear();
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "triggerDeletingCalendarAccounts(): RuntimeException");
                if (c1 != null) {
                    c1.close();
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (!eventsIds.isEmpty()) {
                    eventsIds.clear();
                }
            } catch (Throwable th) {
                if (c1 != null) {
                    c1.close();
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (!eventsIds.isEmpty()) {
                    eventsIds.clear();
                }
                throw th;
            }
        }
    }

    private void triggerAddingIndex(String table, long id) {
        if (id < 0) {
            Log.v(TAG, "triggerBuildingIndex(): invalid id, return.");
            return;
        }
        if (this.mIndexSearchParser != null) {
            IndexSearchParser indexSearchParser = this.mIndexSearchParser;
            if (IndexSearchParser.isValidTable(table)) {
                if (table.equals("Events")) {
                    Cursor cursor = null;
                    try {
                        cursor = rawQuery("SELECT _id FROM Events WHERE _id = " + id + " AND mutators IS NOT 'com.android.providers.contacts'", null);
                        if (cursor == null || cursor.getCount() == 0) {
                            if (cursor != null) {
                                cursor.close();
                            }
                            return;
                        } else if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                if (getThreadSession().hasTransaction()) {
                    String realTable;
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

    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm) {
        long begin = 0;
        if (this.bJankMonitor) {
            begin = SystemClock.uptimeMillis();
        }
        acquireReference();
        SQLiteStatement sQLiteStatement;
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT");
            sql.append(CONFLICT_VALUES[conflictAlgorithm]);
            sql.append(" INTO ");
            sql.append(table);
            sql.append('(');
            Object[] bindArgs = null;
            int size = (initialValues == null || (initialValues.isEmpty() ^ 1) == 0) ? 0 : initialValues.size();
            if (size > 0) {
                bindArgs = new Object[size];
                int i = 0;
                Iterator colName$iterator = initialValues.keySet().iterator();
                while (true) {
                    int i2 = i;
                    if (!colName$iterator.hasNext()) {
                        break;
                    }
                    String colName = (String) colName$iterator.next();
                    sql.append(i2 > 0 ? "," : ProxyInfo.LOCAL_EXCL_LIST);
                    sql.append(colName);
                    i = i2 + 1;
                    bindArgs[i2] = initialValues.get(colName);
                }
                sql.append(')');
                sql.append(" VALUES (");
                i = 0;
                while (i < size) {
                    sql.append(i > 0 ? ",?" : "?");
                    i++;
                }
            } else {
                sql.append(nullColumnHack).append(") VALUES (NULL");
            }
            sql.append(')');
            sQLiteStatement = new SQLiteStatement(this, sql.toString(), bindArgs);
            long id = sQLiteStatement.executeInsert();
            triggerAddingIndex(table, id);
            sQLiteStatement.close();
            releaseReference();
            if (this.bJankMonitor) {
                this.mJankDBStats.addInsert(SystemClock.uptimeMillis() - begin, table, this.jank_dbname);
            }
            return id;
        } catch (Throwable th) {
            releaseReference();
            if (this.bJankMonitor) {
                this.mJankDBStats.addInsert(SystemClock.uptimeMillis() - begin, table, this.jank_dbname);
            }
        }
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        long begin = 0;
        if (this.bJankMonitor) {
            begin = SystemClock.uptimeMillis();
        }
        acquireReference();
        SQLiteStatement statement;
        try {
            statement = new SQLiteStatement(this, "DELETE FROM " + table + (!TextUtils.isEmpty(whereClause) ? " WHERE " + whereClause : ProxyInfo.LOCAL_EXCL_LIST), whereArgs);
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
        }
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        triggerUpdatingOrDeletingIndex(table, whereClause, whereArgs, 1);
        return updateWithOnConflict(table, values, whereClause, whereArgs, 0);
    }

    public int updateWithOnConflict(String table, ContentValues values, String whereClause, String[] whereArgs, int conflictAlgorithm) {
        long begin = 0;
        if (this.bJankMonitor) {
            begin = SystemClock.uptimeMillis();
        }
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Empty values");
        }
        acquireReference();
        SQLiteStatement sQLiteStatement;
        try {
            StringBuilder sql = new StringBuilder(120);
            sql.append("UPDATE ");
            sql.append(CONFLICT_VALUES[conflictAlgorithm]);
            sql.append(table);
            sql.append(" SET ");
            int setValuesSize = values.size();
            int bindArgsSize = whereArgs == null ? setValuesSize : setValuesSize + whereArgs.length;
            Object[] bindArgs = new Object[bindArgsSize];
            int i = 0;
            Iterator colName$iterator = values.keySet().iterator();
            while (true) {
                int i2 = i;
                if (!colName$iterator.hasNext()) {
                    break;
                }
                String colName = (String) colName$iterator.next();
                sql.append(i2 > 0 ? "," : ProxyInfo.LOCAL_EXCL_LIST);
                sql.append(colName);
                i = i2 + 1;
                bindArgs[i2] = values.get(colName);
                sql.append("=?");
            }
            if (whereArgs != null) {
                for (i = setValuesSize; i < bindArgsSize; i++) {
                    bindArgs[i] = whereArgs[i - setValuesSize];
                }
            }
            if (!TextUtils.isEmpty(whereClause)) {
                sql.append(" WHERE ");
                sql.append(whereClause);
            }
            sQLiteStatement = new SQLiteStatement(this, sql.toString(), bindArgs);
            int executeUpdateDelete = sQLiteStatement.executeUpdateDelete();
            sQLiteStatement.close();
            releaseReference();
            if (this.bJankMonitor) {
                this.mJankDBStats.addUpdate(SystemClock.uptimeMillis() - begin, table, this.jank_dbname);
            }
            return executeUpdateDelete;
        } catch (Throwable th) {
            releaseReference();
            if (this.bJankMonitor) {
                this.mJankDBStats.addUpdate(SystemClock.uptimeMillis() - begin, table, this.jank_dbname);
            }
        }
    }

    public void execSQL(String sql) throws SQLException {
        executeSql(sql, null);
    }

    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        if (bindArgs == null) {
            throw new IllegalArgumentException("Empty bindArgs");
        }
        triggerDeletingCalendarAccounts(sql, bindArgs);
        executeSql(sql, bindArgs);
    }

    private int executeSql(String sql, Object[] bindArgs) throws SQLException {
        long begin = 0;
        if (this.bJankMonitor) {
            begin = SystemClock.uptimeMillis();
        }
        acquireReference();
        SQLiteStatement statement;
        try {
            if (DatabaseUtils.getSqlStatementType(sql) == 3) {
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
            if (this.bJankMonitor) {
                this.mJankDBStats.addExecsql(SystemClock.uptimeMillis() - begin, ProxyInfo.LOCAL_EXCL_LIST, this.jank_dbname);
            }
            return executeUpdateDelete;
        } catch (Throwable th) {
            releaseReference();
            if (this.bJankMonitor) {
                this.mJankDBStats.addExecsql(SystemClock.uptimeMillis() - begin, ProxyInfo.LOCAL_EXCL_LIST, this.jank_dbname);
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
        if (locale == null) {
            throw new IllegalArgumentException("locale must not be null.");
        }
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
            if (this.mConfigurationLocked.foreignKeyConstraintsEnabled == enable) {
                return;
            }
            this.mConfigurationLocked.foreignKeyConstraintsEnabled = enable;
            try {
                this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
            } catch (RuntimeException ex) {
                this.mConfigurationLocked.foreignKeyConstraintsEnabled = enable ^ 1;
                throw ex;
            }
        }
    }

    /* JADX WARNING: Missing block: B:35:0x007f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            } else if (this.mConfigurationLocked.isInMemoryDb()) {
                Log.i(TAG, "can't enable WAL for memory databases.");
                return false;
            } else if (!this.mHasAttachedDbsLocked) {
                SQLiteDatabaseConfiguration sQLiteDatabaseConfiguration = this.mConfigurationLocked;
                sQLiteDatabaseConfiguration.openFlags |= 536870912;
                if (this.mConfigurationLocked.configurationEnhancement) {
                    this.mConfigurationLocked.defaultWALEnabled = false;
                    this.mConfigurationLocked.explicitWALEnabled = true;
                }
                try {
                    this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
                    return true;
                } catch (RuntimeException ex) {
                    sQLiteDatabaseConfiguration = this.mConfigurationLocked;
                    sQLiteDatabaseConfiguration.openFlags &= -536870913;
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
            if ((this.mConfigurationLocked.openFlags & 536870912) == 0) {
                return;
            }
            SQLiteDatabaseConfiguration sQLiteDatabaseConfiguration = this.mConfigurationLocked;
            sQLiteDatabaseConfiguration.openFlags &= -536870913;
            if (this.mConfigurationLocked.configurationEnhancement) {
                this.mConfigurationLocked.defaultWALEnabled = false;
                this.mConfigurationLocked.explicitWALEnabled = false;
            }
            try {
                this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
            } catch (RuntimeException ex) {
                sQLiteDatabaseConfiguration = this.mConfigurationLocked;
                sQLiteDatabaseConfiguration.openFlags |= 536870912;
                if (this.mConfigurationLocked.configurationEnhancement) {
                    this.mConfigurationLocked.defaultWALEnabled = defaultWALEnabled;
                    this.mConfigurationLocked.explicitWALEnabled = explicitWALEnabled;
                }
                throw ex;
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x001e, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:17:0x0029, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isWriteAheadLoggingEnabled() {
        boolean z = true;
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            if (this.mConfigurationLocked.configurationEnhancement) {
                if ((this.mConfigurationLocked.openFlags & 536870912) == 0 || !this.mConfigurationLocked.explicitWALEnabled) {
                    z = false;
                }
            } else if ((this.mConfigurationLocked.openFlags & 536870912) == 0) {
                z = false;
            }
        }
    }

    static ArrayList<DbStats> getDbStats() {
        ArrayList<DbStats> dbStatsList = new ArrayList();
        for (SQLiteDatabase db : getActiveDatabases()) {
            db.collectDbStats(dbStatsList);
        }
        return dbStatsList;
    }

    private void collectDbStats(ArrayList<DbStats> dbStatsList) {
        synchronized (this.mLock) {
            if (this.mConnectionPoolLocked != null) {
                this.mConnectionPoolLocked.collectDbStats(dbStatsList);
            }
        }
    }

    private static ArrayList<SQLiteDatabase> getActiveDatabases() {
        ArrayList<SQLiteDatabase> databases = new ArrayList();
        synchronized (sActiveDatabases) {
            databases.addAll(sActiveDatabases.keySet());
        }
        return databases;
    }

    static void dumpAll(Printer printer, boolean verbose) {
        for (SQLiteDatabase db : getActiveDatabases()) {
            db.dump(printer, verbose);
        }
    }

    private void dump(Printer printer, boolean verbose) {
        synchronized (this.mLock) {
            if (this.mConnectionPoolLocked != null) {
                printer.println(ProxyInfo.LOCAL_EXCL_LIST);
                this.mConnectionPoolLocked.dump(printer, verbose);
            }
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0028, code:
            r1 = null;
     */
    /* JADX WARNING: Missing block: B:18:?, code:
            r1 = rawQuery("pragma database_list;", null);
     */
    /* JADX WARNING: Missing block: B:20:0x0035, code:
            if (r1.moveToNext() == false) goto L_0x0059;
     */
    /* JADX WARNING: Missing block: B:21:0x0037, code:
            r0.add(new android.util.Pair(r1.getString(1), r1.getString(2)));
     */
    /* JADX WARNING: Missing block: B:24:0x004b, code:
            if (r1 != null) goto L_0x004d;
     */
    /* JADX WARNING: Missing block: B:26:?, code:
            r1.close();
     */
    /* JADX WARNING: Missing block: B:29:0x0052, code:
            releaseReference();
     */
    /* JADX WARNING: Missing block: B:34:0x0059, code:
            if (r1 == null) goto L_0x005e;
     */
    /* JADX WARNING: Missing block: B:36:?, code:
            r1.close();
     */
    /* JADX WARNING: Missing block: B:37:0x005e, code:
            releaseReference();
     */
    /* JADX WARNING: Missing block: B:38:0x0061, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<Pair<String, String>> getAttachedDbs() {
        ArrayList<Pair<String, String>> attachedDbs = new ArrayList();
        synchronized (this.mLock) {
            if (this.mConnectionPoolLocked == null) {
                return null;
            } else if (this.mHasAttachedDbsLocked) {
                acquireReference();
            } else {
                attachedDbs.add(new Pair("main", this.mConfigurationLocked.path));
                return attachedDbs;
            }
        }
    }

    public boolean isDatabaseIntegrityOk() {
        List<Pair<String, String>> attachedDbs;
        SQLiteStatement sQLiteStatement;
        List<Pair<String, String>> attachedDbs2;
        Throwable th;
        acquireReference();
        try {
            attachedDbs = getAttachedDbs();
            if (attachedDbs == null) {
                throw new IllegalStateException("databaselist for: " + getPath() + " couldn't " + "be retrieved. probably because the database is closed");
            }
        } catch (SQLiteException e) {
            attachedDbs2 = new ArrayList();
            attachedDbs2.add(new Pair("main", getPath()));
            attachedDbs = attachedDbs2;
        } catch (Throwable th2) {
            th = th2;
            attachedDbs = attachedDbs2;
            releaseReference();
            throw th;
        }
        int i = 0;
        while (i < attachedDbs.size()) {
            Pair<String, String> p = (Pair) attachedDbs.get(i);
            sQLiteStatement = null;
            sQLiteStatement = compileStatement("PRAGMA " + ((String) p.first) + ".integrity_check(1);");
            String rslt = sQLiteStatement.simpleQueryForString();
            if (rslt.equalsIgnoreCase("ok")) {
                if (sQLiteStatement != null) {
                    sQLiteStatement.close();
                }
                i++;
            } else {
                Log.e(TAG, "PRAGMA integrity_check on " + ((String) p.second) + " returned: " + rslt);
                if (sQLiteStatement != null) {
                    sQLiteStatement.close();
                }
                releaseReference();
                return false;
            }
        }
        releaseReference();
        return true;
    }

    public String toString() {
        return "SQLiteDatabase: " + getPath();
    }

    private void throwIfNotOpenLocked() {
        if (this.mConnectionPoolLocked == null) {
            throw new IllegalStateException("The database '" + this.mConfigurationLocked.label + "' is not open.");
        }
    }

    public void enableExclusiveConnection(boolean enabled, DatabaseConnectionExclusiveHandler connectionExclusiveHandler) {
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
