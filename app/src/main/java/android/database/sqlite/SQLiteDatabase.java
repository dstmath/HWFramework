package android.database.sqlite;

import android.bluetooth.BluetoothAssignedNumbers;
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
import android.rms.iaware.AwareConstant.Database.HwUserData;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

public final class SQLiteDatabase extends SQLiteClosable {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final int CONFLICT_ABORT = 2;
    public static final int CONFLICT_FAIL = 3;
    public static final int CONFLICT_IGNORE = 4;
    public static final int CONFLICT_NONE = 0;
    public static final int CONFLICT_REPLACE = 5;
    public static final int CONFLICT_ROLLBACK = 1;
    private static final String[] CONFLICT_VALUES = null;
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
    private static WeakHashMap<SQLiteDatabase, Object> sActiveDatabases;
    private boolean bJankMonitor;
    private String jank_dbname;
    private final CloseGuard mCloseGuardLocked;
    private final SQLiteDatabaseConfiguration mConfigurationLocked;
    private DatabaseConnectionExclusiveHandler mConnectionExclusiveHandler;
    private SQLiteConnectionPool mConnectionPoolLocked;
    private final CursorFactory mCursorFactory;
    private boolean mEnableExclusiveConnection;
    private final DatabaseErrorHandler mErrorHandler;
    private boolean mHasAttachedDbsLocked;
    private IndexSearchParser mIndexSearchParser;
    private JankSqlite mJankDBStats;
    private final Object mLock;
    private final ThreadLocal<SQLiteSession> mThreadSession;

    /* renamed from: android.database.sqlite.SQLiteDatabase.2 */
    static class AnonymousClass2 implements FileFilter {
        final /* synthetic */ String val$prefix;

        AnonymousClass2(String val$prefix) {
            this.val$prefix = val$prefix;
        }

        public boolean accept(File candidate) {
            return candidate.getName().startsWith(this.val$prefix);
        }
    }

    public interface CursorFactory {
        Cursor newCursor(SQLiteDatabase sQLiteDatabase, SQLiteCursorDriver sQLiteCursorDriver, String str, SQLiteQuery sQLiteQuery);
    }

    public interface CustomFunction {
        void callback(String[] strArr);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.database.sqlite.SQLiteDatabase.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.database.sqlite.SQLiteDatabase.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.database.sqlite.SQLiteDatabase.<clinit>():void");
    }

    private SQLiteDatabase(String path, int openFlags, CursorFactory cursorFactory, DatabaseErrorHandler errorHandler) {
        this.bJankMonitor = true;
        this.jank_dbname = ProxyInfo.LOCAL_EXCL_LIST;
        this.mJankDBStats = new JankSqlite();
        this.mIndexSearchParser = null;
        this.mThreadSession = new ThreadLocal<SQLiteSession>() {
            protected SQLiteSession initialValue() {
                return SQLiteDatabase.this.createSession();
            }
        };
        this.mLock = new Object();
        this.mCloseGuardLocked = CloseGuard.get();
        this.mEnableExclusiveConnection = -assertionsDisabled;
        this.mCursorFactory = cursorFactory;
        if (errorHandler == null) {
            errorHandler = new DefaultDatabaseErrorHandler();
        }
        this.mErrorHandler = errorHandler;
        this.mConfigurationLocked = new SQLiteDatabaseConfiguration(path, openFlags);
        this.jank_dbname = path;
        if (this.jank_dbname.equals("JankEventDb.db")) {
            this.bJankMonitor = -assertionsDisabled;
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
        dispose(-assertionsDisabled);
    }

    private void dispose(boolean finalized) {
        synchronized (this.mLock) {
            if (this.mCloseGuardLocked != null) {
                if (finalized) {
                    this.mCloseGuardLocked.warnIfOpen();
                }
                this.mCloseGuardLocked.close();
            }
            SQLiteConnectionPool pool = this.mConnectionPoolLocked;
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
            flags = OPEN_READ_MASK;
        } else {
            flags = CONFLICT_ABORT;
        }
        if (this.mEnableExclusiveConnection && checkConnectionExclusiveHandler()) {
            flags |= 8;
        }
        if (isMainThread()) {
            return flags | CONFLICT_IGNORE;
        }
        return flags;
    }

    private static boolean isMainThread() {
        Looper looper = Looper.myLooper();
        if (looper == null || looper != Looper.getMainLooper()) {
            return -assertionsDisabled;
        }
        return true;
    }

    public void beginTransaction() {
        beginTransaction(null, true);
    }

    public void beginTransactionNonExclusive() {
        beginTransaction(null, -assertionsDisabled);
    }

    public void beginTransactionWithListener(SQLiteTransactionListener transactionListener) {
        beginTransaction(transactionListener, true);
    }

    public void beginTransactionWithListenerNonExclusive(SQLiteTransactionListener transactionListener) {
        beginTransaction(transactionListener, -assertionsDisabled);
    }

    private void beginTransaction(SQLiteTransactionListener transactionListener, boolean exclusive) {
        acquireReference();
        try {
            int i;
            SQLiteSession threadSession = getThreadSession();
            if (exclusive) {
                i = CONFLICT_ABORT;
            } else {
                i = OPEN_READ_MASK;
            }
            threadSession.beginTransaction(i, transactionListener, getThreadDefaultConnectionFlags(-assertionsDisabled), null);
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
                        case OPEN_READWRITE /*0*/:
                            insertArgs.add(Long.valueOf(sqlinfo.getPrimaryKey()));
                            break;
                        case OPEN_READ_MASK /*1*/:
                            updateArgs.add(Long.valueOf(sqlinfo.getPrimaryKey()));
                            break;
                        case CONFLICT_ABORT /*2*/:
                            deleteArgs.add(Long.valueOf(sqlinfo.getPrimaryKey()));
                            break;
                        default:
                            break;
                    }
                }
            }
            if (insertArgs.size() >= OPEN_READ_MASK) {
                this.mIndexSearchParser.notifyIndexSearchService(insertArgs, OPEN_READWRITE);
            }
            if (updateArgs.size() >= OPEN_READ_MASK) {
                this.mIndexSearchParser.notifyIndexSearchService(updateArgs, OPEN_READ_MASK);
            }
            if (deleteArgs.size() >= OPEN_READ_MASK) {
                this.mIndexSearchParser.notifyIndexSearchService(deleteArgs, CONFLICT_ABORT);
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
        return -assertionsDisabled;
    }

    @Deprecated
    public boolean yieldIfContended() {
        return yieldIfContendedHelper(-assertionsDisabled, -1);
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
        return new HashMap(OPEN_READWRITE);
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
        return openDatabase(path, factory, CREATE_IF_NECESSARY, null);
    }

    public static SQLiteDatabase openOrCreateDatabase(String path, CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return openDatabase(path, factory, CREATE_IF_NECESSARY, errorHandler);
    }

    public static boolean deleteDatabase(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        boolean deleted = ((file.delete() | new File(file.getPath() + "-journal").delete()) | new File(file.getPath() + "-shm").delete()) | new File(file.getPath() + "-wal").delete();
        File dir = file.getParentFile();
        if (dir != null) {
            File[] files = dir.listFiles(new AnonymousClass2(file.getName() + "-mj"));
            if (files != null) {
                for (int i = OPEN_READWRITE; i < files.length; i += OPEN_READ_MASK) {
                    deleted |= files[i].delete();
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
                this.mConfigurationLocked.openFlags = (this.mConfigurationLocked.openFlags & -2) | OPEN_READWRITE;
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
            if (!-assertionsDisabled) {
                if ((this.mConnectionPoolLocked == null ? OPEN_READ_MASK : null) == null) {
                    throw new AssertionError();
                }
            }
            this.mConnectionPoolLocked = SQLiteConnectionPool.open(this.mConfigurationLocked, this.mEnableExclusiveConnection);
            this.mCloseGuardLocked.open("close");
        }
        synchronized (sActiveDatabases) {
            sActiveDatabases.put(this, null);
        }
    }

    public static SQLiteDatabase create(CursorFactory factory) {
        return openDatabase(SQLiteDatabaseConfiguration.MEMORY_DB_PATH, factory, CREATE_IF_NECESSARY);
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
            return tables.substring(OPEN_READWRITE, spacepos);
        }
        if (commapos <= 0 || (commapos >= spacepos && spacepos >= 0)) {
            return tables;
        }
        return tables.substring(OPEN_READWRITE, commapos);
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
        return query(-assertionsDisabled, table, columns, selection, selectionArgs, groupBy, having, orderBy, null);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return query(-assertionsDisabled, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
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
            if (whereArgs == null || whereArgs.length != OPEN_READ_MASK) {
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
            sql.append("SELECT _id FROM words WHERE ").append(whereClause.replace(HwUserData._ID, "source_id")).append(" AND ").append("table_to_use IS 8");
        }
        return rawQuery(sql.toString(), whereArgs);
    }

    private void triggerUpdatingOrDeletingIndex(String table, String whereClause, String[] whereArgs, int operation) {
        if (this.mIndexSearchParser != null) {
            IndexSearchParser indexSearchParser = this.mIndexSearchParser;
            if (IndexSearchParser.isValidTable(table)) {
                Cursor cursor = null;
                try {
                    cursor = queryForIndexSearch(table, whereClause, whereArgs);
                    if (cursor == null || cursor.getCount() == 0) {
                        Log.v(TAG, "triggerBuildingIndex(): cursor is null or count is 0, return.");
                        if (cursor != null) {
                            cursor.close();
                        }
                        return;
                    }
                    if (getThreadSession().hasTransaction()) {
                        while (cursor.moveToNext()) {
                            String realTable;
                            if (table.equals("Body")) {
                                realTable = "Message";
                            } else {
                                realTable = table;
                            }
                            getThreadSession().insertTransMap(realTable, cursor.getLong(OPEN_READWRITE), operation);
                        }
                    } else {
                        List<Long> ids = new ArrayList();
                        while (cursor.moveToNext()) {
                            ids.add(Long.valueOf(cursor.getLong(OPEN_READWRITE)));
                        }
                        this.mIndexSearchParser.notifyIndexSearchService(ids, operation);
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (RuntimeException e) {
                    Log.e(TAG, "triggerUpdatingOrDeletingIndex(): RuntimeException");
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e2) {
                    Log.e(TAG, "triggerUpdatingOrDeletingIndex(): Exception");
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    private void triggerDeletingCalendarAccounts(String sql, Object[] bindArgs) {
        if (this.mIndexSearchParser != null && "DELETE FROM Calendars WHERE account_name=? AND account_type=?".equals(sql)) {
            Cursor cursor = null;
            Cursor cursor2 = null;
            int i = OPEN_READWRITE;
            List<Long> eventsIds = new ArrayList();
            try {
                cursor = rawQuery("SELECT _id FROM Calendars where account_name=? AND account_type=?", (String[]) bindArgs);
                if (cursor != null && cursor.getCount() > 0) {
                    String[] calendarIds = new String[cursor.getCount()];
                    while (cursor.moveToNext()) {
                        calendarIds[i] = cursor.getString(OPEN_READWRITE);
                        i += OPEN_READ_MASK;
                    }
                    cursor2 = queryForIndexSearch("Events", "calendar_id IN (?)", calendarIds);
                    if (cursor2 != null && cursor2.getCount() > 0) {
                        while (cursor2.moveToNext()) {
                            eventsIds.add(Long.valueOf(cursor2.getLong(OPEN_READWRITE)));
                        }
                        this.mIndexSearchParser.notifyIndexSearchService(eventsIds, CONFLICT_ABORT);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (cursor2 != null) {
                    cursor2.close();
                }
                if (!eventsIds.isEmpty()) {
                    eventsIds.clear();
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "triggerDeletingCalendarAccounts(): RuntimeException");
                if (cursor != null) {
                    cursor.close();
                }
                if (cursor2 != null) {
                    cursor2.close();
                }
                if (!eventsIds.isEmpty()) {
                    eventsIds.clear();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                if (cursor2 != null) {
                    cursor2.close();
                }
                if (!eventsIds.isEmpty()) {
                    eventsIds.clear();
                }
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
                    getThreadSession().insertTransMap(realTable, id, OPEN_READWRITE);
                } else {
                    this.mIndexSearchParser.notifyIndexSearchService(id, OPEN_READWRITE);
                }
            }
        }
    }

    public long insert(String table, String nullColumnHack, ContentValues values) {
        try {
            return insertWithOnConflict(table, nullColumnHack, values, OPEN_READWRITE);
        } catch (SQLException e) {
            Log.e(TAG, "Error inserting " + values, e);
            return -1;
        }
    }

    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws SQLException {
        return insertWithOnConflict(table, nullColumnHack, values, OPEN_READWRITE);
    }

    public long replace(String table, String nullColumnHack, ContentValues initialValues) {
        try {
            return insertWithOnConflict(table, nullColumnHack, initialValues, CONFLICT_REPLACE);
        } catch (SQLException e) {
            Log.e(TAG, "Error inserting " + initialValues, e);
            return -1;
        }
    }

    public long replaceOrThrow(String table, String nullColumnHack, ContentValues initialValues) throws SQLException {
        return insertWithOnConflict(table, nullColumnHack, initialValues, CONFLICT_REPLACE);
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
            Object[] objArr = null;
            int size = (initialValues == null || initialValues.size() <= 0) ? OPEN_READWRITE : initialValues.size();
            if (size > 0) {
                int i;
                objArr = new Object[size];
                int i2 = OPEN_READWRITE;
                for (String colName : initialValues.keySet()) {
                    sql.append(i2 > 0 ? "," : ProxyInfo.LOCAL_EXCL_LIST);
                    sql.append(colName);
                    i = i2 + OPEN_READ_MASK;
                    objArr[i2] = initialValues.get(colName);
                    i2 = i;
                }
                sql.append(')');
                sql.append(" VALUES (");
                i = OPEN_READWRITE;
                while (i < size) {
                    sql.append(i > 0 ? ",?" : "?");
                    i += OPEN_READ_MASK;
                }
            } else {
                sql.append(nullColumnHack).append(") VALUES (NULL");
            }
            sql.append(')');
            sQLiteStatement = new SQLiteStatement(this, sql.toString(), objArr);
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
            triggerUpdatingOrDeletingIndex(table, whereClause, whereArgs, CONFLICT_ABORT);
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
        triggerUpdatingOrDeletingIndex(table, whereClause, whereArgs, OPEN_READ_MASK);
        return updateWithOnConflict(table, values, whereClause, whereArgs, OPEN_READWRITE);
    }

    public int updateWithOnConflict(String table, ContentValues values, String whereClause, String[] whereArgs, int conflictAlgorithm) {
        long begin = 0;
        if (this.bJankMonitor) {
            begin = SystemClock.uptimeMillis();
        }
        if (values == null || values.size() == 0) {
            throw new IllegalArgumentException("Empty values");
        }
        acquireReference();
        SQLiteStatement sQLiteStatement;
        try {
            int i;
            StringBuilder sql = new StringBuilder(BluetoothAssignedNumbers.NIKE);
            sql.append("UPDATE ");
            sql.append(CONFLICT_VALUES[conflictAlgorithm]);
            sql.append(table);
            sql.append(" SET ");
            int setValuesSize = values.size();
            int bindArgsSize = whereArgs == null ? setValuesSize : setValuesSize + whereArgs.length;
            Object[] bindArgs = new Object[bindArgsSize];
            int i2 = OPEN_READWRITE;
            for (String colName : values.keySet()) {
                sql.append(i2 > 0 ? "," : ProxyInfo.LOCAL_EXCL_LIST);
                sql.append(colName);
                i = i2 + OPEN_READ_MASK;
                bindArgs[i2] = values.get(colName);
                sql.append("=?");
                i2 = i;
            }
            if (whereArgs != null) {
                for (i = setValuesSize; i < bindArgsSize; i += OPEN_READ_MASK) {
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
            if (DatabaseUtils.getSqlStatementType(sql) == CONFLICT_FAIL) {
                boolean disableWal = -assertionsDisabled;
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
        return (this.mConfigurationLocked.openFlags & OPEN_READ_MASK) == OPEN_READ_MASK ? true : -assertionsDisabled;
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
            z = this.mConnectionPoolLocked != null ? true : -assertionsDisabled;
        }
        return z;
    }

    public boolean needUpgrade(int newVersion) {
        return newVersion > getVersion() ? true : -assertionsDisabled;
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
        if (cacheSize > MAX_SQL_CACHE_SIZE || cacheSize < 0) {
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
                this.mConfigurationLocked.foreignKeyConstraintsEnabled = enable ? -assertionsDisabled : true;
                throw ex;
            }
        }
    }

    public boolean enableWriteAheadLogging() {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            boolean defaultWALEnabled = this.mConfigurationLocked.defaultWALEnabled;
            boolean explicitWALEnabled = this.mConfigurationLocked.explicitWALEnabled;
            if (this.mConfigurationLocked.configurationEnhancement) {
                if ((this.mConfigurationLocked.openFlags & ENABLE_WRITE_AHEAD_LOGGING) != 0 && this.mConfigurationLocked.explicitWALEnabled) {
                    return true;
                }
            } else if ((this.mConfigurationLocked.openFlags & ENABLE_WRITE_AHEAD_LOGGING) != 0) {
                return true;
            }
            if (isReadOnlyLocked()) {
                return -assertionsDisabled;
            } else if (this.mConfigurationLocked.isInMemoryDb()) {
                Log.i(TAG, "can't enable WAL for memory databases.");
                return -assertionsDisabled;
            } else if (this.mHasAttachedDbsLocked) {
                if (Log.isLoggable(TAG, CONFLICT_FAIL)) {
                    Log.d(TAG, "this database: " + this.mConfigurationLocked.label + " has attached databases. can't  enable WAL.");
                }
                return -assertionsDisabled;
            } else {
                SQLiteDatabaseConfiguration sQLiteDatabaseConfiguration = this.mConfigurationLocked;
                sQLiteDatabaseConfiguration.openFlags |= ENABLE_WRITE_AHEAD_LOGGING;
                if (this.mConfigurationLocked.configurationEnhancement) {
                    this.mConfigurationLocked.defaultWALEnabled = -assertionsDisabled;
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
            }
        }
    }

    public void disableWriteAheadLogging() {
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            boolean defaultWALEnabled = this.mConfigurationLocked.defaultWALEnabled;
            boolean explicitWALEnabled = this.mConfigurationLocked.explicitWALEnabled;
            if ((this.mConfigurationLocked.openFlags & ENABLE_WRITE_AHEAD_LOGGING) == 0) {
                return;
            }
            SQLiteDatabaseConfiguration sQLiteDatabaseConfiguration = this.mConfigurationLocked;
            sQLiteDatabaseConfiguration.openFlags &= -536870913;
            if (this.mConfigurationLocked.configurationEnhancement) {
                this.mConfigurationLocked.defaultWALEnabled = -assertionsDisabled;
                this.mConfigurationLocked.explicitWALEnabled = -assertionsDisabled;
            }
            try {
                this.mConnectionPoolLocked.reconfigure(this.mConfigurationLocked);
            } catch (RuntimeException ex) {
                sQLiteDatabaseConfiguration = this.mConfigurationLocked;
                sQLiteDatabaseConfiguration.openFlags |= ENABLE_WRITE_AHEAD_LOGGING;
                if (this.mConfigurationLocked.configurationEnhancement) {
                    this.mConfigurationLocked.defaultWALEnabled = defaultWALEnabled;
                    this.mConfigurationLocked.explicitWALEnabled = explicitWALEnabled;
                }
                throw ex;
            }
        }
    }

    public boolean isWriteAheadLoggingEnabled() {
        boolean z = true;
        synchronized (this.mLock) {
            throwIfNotOpenLocked();
            if (this.mConfigurationLocked.configurationEnhancement) {
                if ((this.mConfigurationLocked.openFlags & ENABLE_WRITE_AHEAD_LOGGING) == 0 || !this.mConfigurationLocked.explicitWALEnabled) {
                    z = -assertionsDisabled;
                }
                return z;
            }
            if ((this.mConfigurationLocked.openFlags & ENABLE_WRITE_AHEAD_LOGGING) == 0) {
                z = -assertionsDisabled;
            }
            return z;
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

    public List<Pair<String, String>> getAttachedDbs() {
        ArrayList<Pair<String, String>> attachedDbs = new ArrayList();
        synchronized (this.mLock) {
            if (this.mConnectionPoolLocked == null) {
                return null;
            } else if (this.mHasAttachedDbsLocked) {
                acquireReference();
                Cursor cursor = null;
                try {
                    cursor = rawQuery("pragma database_list;", null);
                    while (cursor.moveToNext()) {
                        attachedDbs.add(new Pair(cursor.getString(OPEN_READ_MASK), cursor.getString(CONFLICT_ABORT)));
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    releaseReference();
                    return attachedDbs;
                } catch (Throwable th) {
                    releaseReference();
                }
            } else {
                attachedDbs.add(new Pair("main", this.mConfigurationLocked.path));
                return attachedDbs;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
        }
        int i = OPEN_READWRITE;
        while (i < attachedDbs.size()) {
            Pair<String, String> p = (Pair) attachedDbs.get(i);
            sQLiteStatement = null;
            sQLiteStatement = compileStatement("PRAGMA " + ((String) p.first) + ".integrity_check(1);");
            String rslt = sQLiteStatement.simpleQueryForString();
            if (rslt.equalsIgnoreCase("ok")) {
                if (sQLiteStatement != null) {
                    sQLiteStatement.close();
                }
                i += OPEN_READ_MASK;
            } else {
                Log.e(TAG, "PRAGMA integrity_check on " + ((String) p.second) + " returned: " + rslt);
                if (sQLiteStatement != null) {
                    sQLiteStatement.close();
                }
                releaseReference();
                return -assertionsDisabled;
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
        return -assertionsDisabled;
    }
}
