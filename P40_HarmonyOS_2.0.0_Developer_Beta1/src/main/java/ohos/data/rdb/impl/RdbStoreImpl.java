package ohos.data.rdb.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import ohos.app.Context;
import ohos.data.DatabaseFileConfig;
import ohos.data.DatabaseFileType;
import ohos.data.rdb.AbsRdbPredicates;
import ohos.data.rdb.RdbConstraintException;
import ohos.data.rdb.RdbCorruptException;
import ohos.data.rdb.RdbException;
import ohos.data.rdb.RdbOpenCallback;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.RdbUtils;
import ohos.data.rdb.Statement;
import ohos.data.rdb.StoreConfig;
import ohos.data.rdb.TransactionObserver;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.data.resultset.ResultSetHook;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Pair;

public class RdbStoreImpl extends CoreCloseable implements RdbStore {
    private static final int DEFAULT_SQL_LENGTH = 120;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "RdbStoreImpl");
    private static final String[] ON_CONFLICT_CLAUSE = {"", " OR ROLLBACK", " OR ABORT", " OR FAIL", " OR IGNORE", " OR REPLACE"};
    private ConnectionPool connectionPool;
    private Context context;
    private RdbOpenCallback openCallback;
    private final Object parametersLock = new Object();
    private ResultSetHook resultSetHook;
    private final ThreadLocal<StoreSession> threadSession = ThreadLocal.withInitial(new Supplier() {
        /* class ohos.data.rdb.impl.$$Lambda$RdbStoreImpl$AWJbKkBloWskVVMYAR35sv1Ti0s */

        @Override // java.util.function.Supplier
        public final Object get() {
            return RdbStoreImpl.this.createSession();
        }
    });
    private int version;

    private RdbStoreImpl(Context context2, int i, RdbOpenCallback rdbOpenCallback, ResultSetHook resultSetHook2) {
        this.version = i;
        this.openCallback = rdbOpenCallback;
        this.context = context2;
        this.resultSetHook = resultSetHook2;
    }

    public static RdbStoreImpl open(Context context2, StoreConfig storeConfig, int i, RdbOpenCallback rdbOpenCallback, ResultSetHook resultSetHook2) {
        if (context2 == null) {
            throw new IllegalArgumentException("the context config cannot be null.");
        } else if (storeConfig == null) {
            throw new IllegalArgumentException("the store config cannot be null.");
        } else if (i >= 1) {
            RdbStoreImpl rdbStoreImpl = new RdbStoreImpl(context2, i, rdbOpenCallback, resultSetHook2);
            rdbStoreImpl.open(SqliteDatabaseConfig.create(context2, storeConfig));
            return rdbStoreImpl;
        } else {
            throw new IllegalArgumentException("the store version cannot less than 1.");
        }
    }

    private void open(SqliteDatabaseConfig sqliteDatabaseConfig) {
        try {
            this.connectionPool = ConnectionPool.createInstance(sqliteDatabaseConfig);
            sqliteDatabaseConfig.destroyEncryptKey();
            processHelper(sqliteDatabaseConfig);
            if ((sqliteDatabaseConfig.getOpenFlags() & 1) == 1) {
                HiLog.info(LABEL, "Opened %{public}s in read-only mode.", new Object[]{sqliteDatabaseConfig.getName()});
            }
        } catch (RdbCorruptException e) {
            HiLog.error(LABEL, "File is not a database or encrypt key is error.", new Object[0]);
            if (this.openCallback != null) {
                this.openCallback.onCorruption(new File(sqliteDatabaseConfig.getPath()));
            }
            throw e;
        } catch (Throwable th) {
            sqliteDatabaseConfig.destroyEncryptKey();
            throw th;
        }
    }

    private void processHelper(SqliteDatabaseConfig sqliteDatabaseConfig) {
        int version2 = getVersion();
        if (this.version == version2) {
            RdbOpenCallback rdbOpenCallback = this.openCallback;
            if (rdbOpenCallback != null) {
                rdbOpenCallback.onOpen(this);
            }
        } else if ((sqliteDatabaseConfig.getOpenFlags() & 1) == 1) {
            throw new IllegalStateException("Can't upgrade read-only store from version " + version2 + " to " + this.version + ": " + sqliteDatabaseConfig.getName());
        } else if (this.openCallback == null) {
            setVersion(this.version);
        } else {
            beginTransaction();
            if (version2 == 0) {
                try {
                    this.openCallback.onCreate(this);
                } catch (Throwable th) {
                    endTransaction();
                    throw th;
                }
            } else if (this.version > version2) {
                this.openCallback.onUpgrade(this, version2, this.version);
            } else {
                this.openCallback.onDowngrade(this, version2, this.version);
            }
            setVersion(this.version);
            markAsCommit();
            endTransaction();
            this.openCallback.onOpen(this);
        }
    }

    /* access modifiers changed from: private */
    public StoreSession createSession() {
        ConnectionPool connectionPool2;
        synchronized (this.parametersLock) {
            if (isOpen()) {
                connectionPool2 = this.connectionPool;
            } else {
                throw new IllegalStateException("The store of '" + this.connectionPool.getName() + "' is not open.");
            }
        }
        return new StoreSession(connectionPool2);
    }

    /* access modifiers changed from: package-private */
    public StoreSession getThreadSession() {
        return this.threadSession.get();
    }

    @Override // ohos.data.rdb.RdbStore
    public long insert(String str, ValuesBucket valuesBucket) {
        try {
            return insertWithConflictResolution(str, valuesBucket, RdbStore.ConflictResolution.ON_CONFLICT_NONE);
        } catch (RdbException unused) {
            return -1;
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public long insertOrThrowException(String str, ValuesBucket valuesBucket) {
        return insertWithConflictResolution(str, valuesBucket, RdbStore.ConflictResolution.ON_CONFLICT_NONE);
    }

    @Override // ohos.data.rdb.RdbStore
    public List<Long> batchInsertOrThrowException(String str, List<ValuesBucket> list, RdbStore.ConflictResolution conflictResolution) {
        if (isEmpty(str)) {
            throw new IllegalArgumentException("no tableName specified.");
        } else if (list == null) {
            throw new IllegalArgumentException("no values specified, if you want to insert an all null row, please specified a null column with no not null constraints.");
        } else if (list.size() > 1000 || list.size() == 0) {
            throw new IllegalArgumentException("size of initialValues should not be larger than 1000 and larger than 0.");
        } else if (!list.stream().anyMatch($$Lambda$RdbStoreImpl$BtIfp8rdbA_Hnsc_g80wMpA7gmE.INSTANCE)) {
            RdbStore.ConflictResolution analysisResolution = analysisResolution(conflictResolution);
            if (analysisResolution.equals(RdbStore.ConflictResolution.ON_CONFLICT_ROLLBACK)) {
                return innerBatchInsertOrRollback(str, list);
            }
            return innerBatchInsert(str, list, analysisResolution);
        } else {
            throw new IllegalArgumentException("any element in initialValues should not be null or empty.");
        }
    }

    static /* synthetic */ boolean lambda$batchInsertOrThrowException$0(ValuesBucket valuesBucket) {
        return valuesBucket == null || valuesBucket.isEmpty();
    }

    private List<Long> innerBatchInsert(String str, List<ValuesBucket> list, RdbStore.ConflictResolution conflictResolution) {
        ArrayList arrayList = new ArrayList(list.size());
        try {
            acquireRef();
            beginTransaction();
            for (ValuesBucket valuesBucket : list) {
                arrayList.add(Long.valueOf(insertWithConflictResolution(str, valuesBucket, conflictResolution)));
            }
            markAsCommit();
            endTransaction();
            releaseRef();
            return arrayList;
        } catch (RdbConstraintException e) {
            markAsCommit();
            throw e;
        } catch (Throwable th) {
            endTransaction();
            releaseRef();
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0056  */
    private List<Long> innerBatchInsertOrRollback(String str, List<ValuesBucket> list) {
        Throwable th;
        ArrayList arrayList = new ArrayList(list.size());
        acquireRef();
        boolean z = true;
        try {
            beginTransaction();
            for (ValuesBucket valuesBucket : list) {
                arrayList.add(Long.valueOf(insertWithConflictResolution(str, valuesBucket, RdbStore.ConflictResolution.ON_CONFLICT_ROLLBACK)));
            }
            markAsCommit();
            endTransaction();
            releaseRef();
            return arrayList;
        } catch (RdbConstraintException e) {
            try {
                endTransaction();
            } catch (RuntimeException e2) {
                HiLog.error(LABEL, "end transaction occur an error %{private}s.", new Object[]{e2.getMessage()});
            }
            throw e;
        } catch (Throwable th2) {
            th = th2;
            z = false;
            if (z) {
            }
            releaseRef();
            throw th;
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public long replace(String str, ValuesBucket valuesBucket) {
        try {
            return insertWithConflictResolution(str, valuesBucket, RdbStore.ConflictResolution.ON_CONFLICT_REPLACE);
        } catch (RdbException unused) {
            return -1;
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public long replaceOrThrowException(String str, ValuesBucket valuesBucket) {
        return insertWithConflictResolution(str, valuesBucket, RdbStore.ConflictResolution.ON_CONFLICT_REPLACE);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00a2, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a3, code lost:
        $closeResource(r7, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00a6, code lost:
        throw r8;
     */
    @Override // ohos.data.rdb.RdbStore
    public long insertWithConflictResolution(String str, ValuesBucket valuesBucket, RdbStore.ConflictResolution conflictResolution) {
        if (isEmpty(str)) {
            throw new IllegalArgumentException("no tableName specified.");
        } else if (valuesBucket == null || valuesBucket.size() == 0) {
            throw new IllegalArgumentException("no values specified, if you want to insert an all null row,please specified a null column with no not null constraints.");
        } else {
            acquireRef();
            RdbStore.ConflictResolution analysisResolution = analysisResolution(conflictResolution);
            try {
                StringBuilder sb = new StringBuilder(120);
                sb.append("INSERT");
                sb.append(ON_CONFLICT_CLAUSE[analysisResolution.getValue()]);
                sb.append(" INTO ");
                sb.append(str);
                sb.append('(');
                int size = valuesBucket.size();
                Object[] objArr = new Object[size];
                int i = 0;
                int i2 = 0;
                for (Map.Entry<String, Object> entry : valuesBucket.getAll()) {
                    sb.append(i2 > 0 ? "," : "");
                    sb.append(entry.getKey());
                    objArr[i2] = entry.getValue();
                    i2++;
                }
                sb.append(") VALUES (");
                while (i < size) {
                    sb.append(i > 0 ? ",?" : "?");
                    i++;
                }
                sb.append(')');
                GeneralStatement generalStatement = new GeneralStatement(this, sb.toString(), objArr);
                long executeAndGetLastInsertRowId = generalStatement.executeAndGetLastInsertRowId();
                $closeResource(null, generalStatement);
                return executeAndGetLastInsertRowId;
            } finally {
                releaseRef();
            }
        }
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th != null) {
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        } else {
            autoCloseable.close();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0028, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0029, code lost:
        $closeResource(r3, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002c, code lost:
        throw r0;
     */
    @Override // ohos.data.rdb.RdbStore
    public int update(ValuesBucket valuesBucket, AbsRdbPredicates absRdbPredicates) {
        checkParam(valuesBucket, absRdbPredicates);
        acquireRef();
        try {
            Object[] objArr = new Object[(valuesBucket.size() + getWhereArgsLength(absRdbPredicates))];
            GeneralStatement generalStatement = new GeneralStatement(this, SqliteSqlBuilder.buildUpdateString(valuesBucket, absRdbPredicates, objArr, null), objArr);
            int executeAndGetChanges = generalStatement.executeAndGetChanges();
            $closeResource(null, generalStatement);
            return executeAndGetChanges;
        } finally {
            releaseRef();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002c, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002d, code lost:
        $closeResource(r3, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0030, code lost:
        throw r5;
     */
    @Override // ohos.data.rdb.RdbStore
    public int updateWithConflictResolution(ValuesBucket valuesBucket, AbsRdbPredicates absRdbPredicates, RdbStore.ConflictResolution conflictResolution) {
        checkParam(valuesBucket, absRdbPredicates);
        RdbStore.ConflictResolution analysisResolution = analysisResolution(conflictResolution);
        acquireRef();
        try {
            Object[] objArr = new Object[(valuesBucket.size() + getWhereArgsLength(absRdbPredicates))];
            GeneralStatement generalStatement = new GeneralStatement(this, SqliteSqlBuilder.buildUpdateString(valuesBucket, absRdbPredicates, objArr, analysisResolution), objArr);
            int executeAndGetChanges = generalStatement.executeAndGetChanges();
            $closeResource(null, generalStatement);
            return executeAndGetChanges;
        } finally {
            releaseRef();
        }
    }

    private void checkParam(ValuesBucket valuesBucket, AbsRdbPredicates absRdbPredicates) {
        if (valuesBucket == null || valuesBucket.size() == 0) {
            throw new IllegalArgumentException("Empty values");
        } else if (absRdbPredicates == null) {
            throw new IllegalArgumentException("Execute update failed : The parameter rdbPredicates is null.");
        }
    }

    private int getWhereArgsLength(AbsRdbPredicates absRdbPredicates) {
        List<String> whereArgs = absRdbPredicates.getWhereArgs();
        if (whereArgs == null || whereArgs.isEmpty()) {
            return 0;
        }
        return whereArgs.size();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0034, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0035, code lost:
        $closeResource(r4, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0038, code lost:
        throw r0;
     */
    @Override // ohos.data.rdb.RdbStore
    public int delete(AbsRdbPredicates absRdbPredicates) {
        String[] strArr;
        if (absRdbPredicates != null) {
            acquireRef();
            try {
                String buildDeleteString = SqliteSqlBuilder.buildDeleteString(absRdbPredicates);
                List<String> whereArgs = absRdbPredicates.getWhereArgs();
                if (whereArgs != null) {
                    if (!whereArgs.isEmpty()) {
                        strArr = (String[]) whereArgs.toArray(new String[0]);
                        GeneralStatement generalStatement = new GeneralStatement(this, buildDeleteString, strArr);
                        int executeAndGetChanges = generalStatement.executeAndGetChanges();
                        $closeResource(null, generalStatement);
                        return executeAndGetChanges;
                    }
                }
                strArr = null;
                GeneralStatement generalStatement2 = new GeneralStatement(this, buildDeleteString, strArr);
                int executeAndGetChanges2 = generalStatement2.executeAndGetChanges();
                $closeResource(null, generalStatement2);
                return executeAndGetChanges2;
            } finally {
                releaseRef();
            }
        } else {
            throw new IllegalArgumentException("Execute delete failed : The parameter absRdbPredicates is null.");
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public ResultSet query(AbsRdbPredicates absRdbPredicates, String[] strArr) {
        return queryWithHook(absRdbPredicates, strArr, null);
    }

    @Override // ohos.data.rdb.RdbStore
    public ResultSet queryWithHook(AbsRdbPredicates absRdbPredicates, String[] strArr, ResultSetHook resultSetHook2) {
        if (absRdbPredicates != null) {
            acquireRef();
            try {
                return querySqlWithHook(SqliteSqlBuilder.buildQueryString(absRdbPredicates, strArr), (String[]) absRdbPredicates.getWhereArgs().toArray(new String[0]), resultSetHook2);
            } finally {
                releaseRef();
            }
        } else {
            throw new IllegalArgumentException("Execute query failed : The parameter absRdbPredicates is null.");
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public ResultSet querySql(String str, String[] strArr) {
        return querySqlWithHook(str, strArr, null);
    }

    @Override // ohos.data.rdb.RdbStore
    public ResultSet querySqlWithHook(String str, String[] strArr, ResultSetHook resultSetHook2) {
        acquireRef();
        try {
            QueryStatement queryStatement = new QueryStatement(this, str);
            try {
                queryStatement.setStrings(strArr);
                SqliteSharedResultSet sqliteSharedResultSet = new SqliteSharedResultSet(queryStatement);
                if (resultSetHook2 != null) {
                    resultSetHook2.createHook(str, strArr, sqliteSharedResultSet);
                } else if (this.resultSetHook != null) {
                    this.resultSetHook.createHook(str, strArr, sqliteSharedResultSet);
                }
                return sqliteSharedResultSet;
            } catch (RdbException e) {
                queryStatement.close();
                throw e;
            }
        } finally {
            releaseRef();
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public ResultSet queryByStep(AbsRdbPredicates absRdbPredicates, String[] strArr) {
        if (absRdbPredicates != null) {
            acquireRef();
            try {
                return querySqlByStep(SqliteSqlBuilder.buildQueryString(absRdbPredicates, strArr), (String[]) absRdbPredicates.getWhereArgs().toArray(new String[0]));
            } finally {
                releaseRef();
            }
        } else {
            throw new IllegalArgumentException("Execute queryByStep failed : The parameter absRdbPredicates is null.");
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public ResultSet querySqlByStep(String str, String[] strArr) {
        acquireRef();
        try {
            QueryStatement queryStatement = new QueryStatement(this, str);
            try {
                queryStatement.setStrings(strArr);
                StepResultSet stepResultSet = new StepResultSet(queryStatement);
                if (this.resultSetHook != null) {
                    this.resultSetHook.createHook(str, strArr, stepResultSet);
                }
                return stepResultSet;
            } catch (RdbException e) {
                queryStatement.close();
                throw e;
            }
        } finally {
            releaseRef();
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public void executeSql(String str) {
        executeSql(str, null);
    }

    @Override // ohos.data.rdb.RdbStore
    public void executeSql(String str, Object[] objArr) {
        if (SqliteDatabaseUtils.getSqlStatementType(str) != 3) {
            innerExecuteSql(str, objArr);
            return;
        }
        throw new IllegalStateException("The ATTACH statement is not supported in WAL mode");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0036, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0037, code lost:
        $closeResource(r5, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x003a, code lost:
        throw r2;
     */
    private void innerExecuteSql(String str, Object[] objArr) {
        acquireRef();
        try {
            GeneralStatement generalStatement = new GeneralStatement(this, str, objArr);
            generalStatement.executeAndGetChanges();
            $closeResource(null, generalStatement);
            try {
                if (SqliteDatabaseUtils.getSqlStatementType(str) == 9) {
                    synchronized (this.parametersLock) {
                        if (this.connectionPool != null) {
                            this.connectionPool.closeAllReadConnections();
                        } else {
                            throw new IllegalStateException("The RdbStore has been closed.");
                        }
                    }
                }
            } finally {
                releaseRef();
            }
        } catch (Throwable th) {
            if (SqliteDatabaseUtils.getSqlStatementType(str) == 9) {
                synchronized (this.parametersLock) {
                    if (this.connectionPool == null) {
                        throw new IllegalStateException("The RdbStore has been closed.");
                    }
                    this.connectionPool.closeAllReadConnections();
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0047, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0048, code lost:
        $closeResource(r3, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004b, code lost:
        throw r5;
     */
    @Override // ohos.data.rdb.RdbStore
    public long count(String str, String str2, String[] strArr) {
        if (!isEmpty(str)) {
            acquireRef();
            try {
                String str3 = "SELECT COUNT(*) FROM " + str;
                if (isNotEmpty(str2)) {
                    str3 = str3 + " WHERE " + str2;
                }
                GeneralStatement generalStatement = new GeneralStatement(this, str3, strArr);
                long executeAndGetLong = generalStatement.executeAndGetLong();
                $closeResource(null, generalStatement);
                return executeAndGetLong;
            } finally {
                releaseRef();
            }
        } else {
            throw new IllegalArgumentException("no tableName specified.");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0029, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002a, code lost:
        $closeResource(r5, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002d, code lost:
        throw r0;
     */
    @Override // ohos.data.rdb.RdbStore
    public long count(AbsRdbPredicates absRdbPredicates) {
        if (absRdbPredicates != null) {
            acquireRef();
            try {
                GeneralStatement generalStatement = new GeneralStatement(this, SqliteSqlBuilder.buildCountString(absRdbPredicates), (String[]) absRdbPredicates.getWhereArgs().toArray(new String[0]));
                long executeAndGetLong = generalStatement.executeAndGetLong();
                $closeResource(null, generalStatement);
                return executeAndGetLong;
            } finally {
                releaseRef();
            }
        } else {
            throw new IllegalArgumentException("Execute count failed : The parameter rdbPredicates is null.");
        }
    }

    private boolean isNotEmpty(String str) {
        return (str == null || str.length() == 0) ? false : true;
    }

    private boolean isEmpty(String str) {
        return !isNotEmpty(str);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0020, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0021, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
        throw r2;
     */
    @Override // ohos.data.rdb.RdbStore
    public int getVersion() {
        acquireRef();
        try {
            GeneralStatement generalStatement = new GeneralStatement(this, "PRAGMA user_version;", null);
            int intValue = Long.valueOf(generalStatement.executeAndGetLong()).intValue();
            $closeResource(null, generalStatement);
            return intValue;
        } finally {
            releaseRef();
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public void setVersion(int i) {
        executeSql("PRAGMA user_version = " + i);
    }

    @Override // ohos.data.rdb.RdbStore
    public Statement buildStatement(String str) {
        acquireRef();
        try {
            return new GeneralStatement(this, str, null);
        } finally {
            releaseRef();
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public void beginTransaction() {
        beginTransaction(null);
    }

    @Override // ohos.data.rdb.RdbStore
    public void beginTransactionWithObserver(TransactionObserver transactionObserver) {
        beginTransaction(transactionObserver);
    }

    private void beginTransaction(TransactionObserver transactionObserver) {
        acquireRef();
        try {
            getThreadSession().beginTransaction(transactionObserver);
        } finally {
            releaseRef();
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public void markAsCommit() {
        acquireRef();
        try {
            getThreadSession().markAsCommit();
        } finally {
            releaseRef();
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public void endTransaction() {
        acquireRef();
        try {
            getThreadSession().endTransaction();
        } finally {
            releaseRef();
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public boolean isInTransaction() {
        acquireRef();
        try {
            return getThreadSession().inTransaction();
        } finally {
            releaseRef();
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public void giveConnectionTemporarily(long j) {
        acquireRef();
        try {
            getThreadSession().giveConnectionTemporarily(j);
        } finally {
            releaseRef();
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public boolean isHoldingConnection() {
        acquireRef();
        try {
            return getThreadSession().isHoldingConnection();
        } finally {
            releaseRef();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.data.rdb.impl.CoreCloseable
    public void onAllRefReleased() {
        ConnectionPool connectionPool2;
        synchronized (this.parametersLock) {
            connectionPool2 = this.connectionPool;
            this.connectionPool = null;
        }
        if (connectionPool2 != null) {
            connectionPool2.close();
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public boolean isOpen() {
        boolean z;
        synchronized (this.parametersLock) {
            z = this.connectionPool != null;
        }
        return z;
    }

    @Override // ohos.data.rdb.RdbStore
    public final String getPath() {
        String path;
        synchronized (this.parametersLock) {
            if (this.connectionPool != null) {
                path = this.connectionPool.getPath();
            } else {
                throw new IllegalStateException("The RdbStore has been closed.");
            }
        }
        return path;
    }

    @Override // ohos.data.rdb.RdbStore
    public boolean isReadOnly() {
        boolean isReadOnly;
        synchronized (this.parametersLock) {
            if (this.connectionPool != null) {
                isReadOnly = this.connectionPool.isReadOnly();
            } else {
                throw new IllegalStateException("The RdbStore has been closed.");
            }
        }
        return isReadOnly;
    }

    @Override // ohos.data.rdb.RdbStore
    public boolean isMemoryRdb() {
        boolean isMemoryDb;
        synchronized (this.parametersLock) {
            if (this.connectionPool != null) {
                isMemoryDb = this.connectionPool.isMemoryDb();
            } else {
                throw new IllegalStateException("The RdbStore has been closed.");
            }
        }
        return isMemoryDb;
    }

    @Override // ohos.data.rdb.RdbStore
    public boolean checkIntegrity() {
        acquireRef();
        try {
            for (Pair<String, String> pair : listAttached()) {
                Statement statement = null;
                try {
                    Statement buildStatement = buildStatement("pragma " + ((String) pair.f) + " .integrity_check(1)");
                    String executeAndGetString = buildStatement.executeAndGetString();
                    if (!"ok".equalsIgnoreCase(executeAndGetString)) {
                        HiLog.error(LABEL, "Pragma integrity_check on %{private}s returned: %{private}s", new Object[]{pair.s, executeAndGetString});
                        buildStatement.close();
                        return false;
                    }
                    buildStatement.close();
                } catch (Throwable th) {
                    if (0 != 0) {
                        statement.close();
                    }
                    throw th;
                }
            }
            releaseRef();
            return true;
        } finally {
            releaseRef();
        }
    }

    @Override // java.lang.Object, ohos.data.rdb.RdbStore
    public String toString() {
        return "Relation database store: " + getPath();
    }

    /* access modifiers changed from: package-private */
    public void handleCorruption() {
        HiLog.error(LABEL, "File is not a database or encrypt key is error.", new Object[0]);
        RdbOpenCallback rdbOpenCallback = this.openCallback;
        if (rdbOpenCallback != null) {
            rdbOpenCallback.onCorruption(new File(getPath()));
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public boolean backup(String str) {
        return backup(str, null);
    }

    @Override // ohos.data.rdb.RdbStore
    public boolean backup(String str, byte[] bArr) {
        boolean z = (bArr == null || bArr.length == 0) ? false : true;
        if (z && bArr.length > 1024) {
            throw new IllegalArgumentException("Encrypt Key size exceeds maximum limit.");
        } else if (!isInTransaction()) {
            File databasePath = SqliteDatabaseUtils.getDatabasePath(this.context, new DatabaseFileConfig.Builder().setName(str).setEncrypted(z).setDatabaseFileType(DatabaseFileType.BACKUP).build());
            if (!databasePath.exists() || databasePath.delete()) {
                SqliteEncryptKeyLoader generate = SqliteEncryptKeyLoader.generate(this.context, bArr);
                try {
                    return executeBackup(databasePath.getPath(), generate);
                } finally {
                    generate.destroy();
                }
            } else {
                HiLog.error(LABEL, "Can not delete backed up file", new Object[0]);
                throw new IllegalArgumentException("Can not delete backed up file");
            }
        } else {
            HiLog.info(LABEL, "The rdb is in transaction. %{private}s", new Object[]{getPath()});
            throw new IllegalArgumentException("The rdb is in transaction.");
        }
    }

    private boolean executeBackup(String str, SqliteEncryptKeyLoader sqliteEncryptKeyLoader) {
        getThreadSession().backup(str, sqliteEncryptKeyLoader);
        return true;
    }

    private RdbStore.ConflictResolution analysisResolution(RdbStore.ConflictResolution conflictResolution) {
        return conflictResolution == null ? RdbStore.ConflictResolution.ON_CONFLICT_NONE : conflictResolution;
    }

    @Override // ohos.data.rdb.RdbStore
    public boolean restore(String str) {
        return restore(str, null, null);
    }

    @Override // ohos.data.rdb.RdbStore
    public boolean restore(String str, byte[] bArr, byte[] bArr2) {
        if ((bArr != null && bArr.length > 1024) || (bArr2 != null && bArr2.length > 1024)) {
            throw new IllegalArgumentException("Encrypt Key size exceeds maximum limit.");
        } else if (!isInTransaction()) {
            if (SqliteDatabaseUtils.getDatabasePath(this.context, new DatabaseFileConfig.Builder().setName(str).setEncrypted((bArr == null || bArr.length == 0) ? false : true).setDatabaseFileType(DatabaseFileType.BACKUP).build()).exists()) {
                return executeRestore(str, bArr, bArr2);
            }
            HiLog.error(LABEL, "File %{public}s doesn't exist.", new Object[]{str});
            throw new IllegalArgumentException("The file doesn't exist.");
        } else {
            HiLog.error(LABEL, "The rdb is in transaction. %{private}s", new Object[]{getPath()});
            throw new IllegalArgumentException("The rdb is in transaction.");
        }
    }

    private boolean executeRestore(String str, byte[] bArr, byte[] bArr2) {
        boolean changeDbFileForRestore;
        boolean z = true;
        try {
            RdbStoreImpl open = open(this.context, new StoreConfig.Builder().setName(str).setStorageMode(StoreConfig.StorageMode.MODE_DISK).setReadOnly(false).setEncryptKey(bArr).setDatabaseFileType(DatabaseFileType.BACKUP).build(), this.version, null, this.resultSetHook);
            String str2 = open.getPath() + "-temp-" + System.currentTimeMillis();
            SqliteEncryptKeyLoader generate = SqliteEncryptKeyLoader.generate(this.context, bArr2);
            try {
                if (!open.executeBackup(str2, generate)) {
                    HiLog.error(LABEL, "%{private}s backup failed.", new Object[]{str2});
                    open.close();
                    return false;
                }
                open.close();
                if (bArr2 == null || bArr2.length == 0) {
                    z = false;
                }
                synchronized (this.parametersLock) {
                    if (isOpen()) {
                        changeDbFileForRestore = this.connectionPool.changeDbFileForRestore(SqliteDatabaseUtils.getDatabasePath(this.context, new DatabaseFileConfig.Builder().setName(this.connectionPool.getName()).setEncrypted(z).setDatabaseFileType(this.connectionPool.getDatabaseFileType()).build()).getPath(), str2, generate);
                    } else {
                        throw new IllegalStateException("The RdbStore has been closed.");
                    }
                }
                generate.destroy();
                return changeDbFileForRestore;
            } finally {
                generate.destroy();
            }
        } catch (RuntimeException unused) {
            HiLog.error(LABEL, "Open Rdb store failed. %{public}s", new Object[]{str});
            return false;
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a3, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a4, code lost:
        $closeResource(r7, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a7, code lost:
        throw r8;
     */
    @Override // ohos.data.rdb.RdbStore
    public void addAttach(String str, String str2, byte[] bArr) {
        if (str == null || str.length() == 0) {
            throw new IllegalArgumentException("Attach alias should not be null.");
        }
        acquireRef();
        try {
            GeneralStatement generalStatement = new GeneralStatement(this, "PRAGMA journal_mode", null);
            String executeAndGetString = generalStatement.executeAndGetString();
            $closeResource(null, generalStatement);
            if (!"WAL".equalsIgnoreCase(executeAndGetString)) {
                releaseRef();
                boolean z = (bArr == null || bArr.length == 0) ? false : true;
                String str3 = SqliteDatabaseConfig.MEMORY_DB_PATH;
                if (str2 != null && !str2.isEmpty() && !str3.equals(str2)) {
                    str3 = SqliteDatabaseUtils.getDatabasePath(this.context, new DatabaseFileConfig.Builder().setName(str2).setEncrypted(z).setDatabaseFileType(DatabaseFileType.NORMAL).build()).getPath();
                }
                if (z) {
                    SqliteEncryptKeyLoader generate = SqliteEncryptKeyLoader.generate(this.context, bArr);
                    byte[] encryptKey = generate.getEncryptKey();
                    try {
                        innerExecuteSql("ATTACH DATABASE ? AS ? KEY ?", new Object[]{str3, str, encryptKey});
                    } finally {
                        Arrays.fill(encryptKey, (byte) 0);
                        generate.destroy();
                    }
                } else {
                    innerExecuteSql("ATTACH DATABASE ? AS ? KEY ?", new String[]{str3, str, ""});
                }
            } else {
                throw new IllegalStateException("The ATTACH statement is not supported in WAL mode");
            }
        } catch (Throwable th) {
            releaseRef();
            throw th;
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public List<Pair<String, String>> listAttached() {
        ArrayList arrayList = new ArrayList();
        acquireRef();
        ResultSet resultSet = null;
        try {
            ResultSet querySqlByStep = querySqlByStep("pragma database_list", null);
            while (querySqlByStep.goToNextRow()) {
                arrayList.add(new Pair(querySqlByStep.getString(1), querySqlByStep.getString(2)));
            }
            try {
                querySqlByStep.close();
                return arrayList;
            } finally {
                releaseRef();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                resultSet.close();
            }
            throw th;
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public void changeEncryptKey(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            throw new IllegalArgumentException("Empty new encrypt key.");
        } else if (bArr.length <= 1024) {
            synchronized (this.parametersLock) {
                if (isOpen()) {
                    try {
                        this.connectionPool.changeEncryptKey(SqliteEncryptKeyLoader.generate(this.context, bArr));
                    } catch (IllegalStateException | RdbException e) {
                        HiLog.error(LABEL, "Failed to change the encryption key of database %{public}s", new Object[]{this.connectionPool.getName()});
                        throw e;
                    }
                } else {
                    throw new IllegalStateException("Change encrypt key on a no-open database.");
                }
            }
        } else {
            throw new IllegalArgumentException("Encrypt Key size exceeds maximum limit.");
        }
    }

    public static int releaseRdbMemory() {
        return SqliteGlobalConfig.releaseRdbMemory();
    }

    public void verifySQl(String str) {
        acquireRef();
        try {
            getThreadSession().prepare(str, true);
        } finally {
            releaseRef();
        }
    }

    public void verifyPredicates(RdbUtils.OperationType operationType, AbsRdbPredicates absRdbPredicates) {
        acquireRef();
        int i = AnonymousClass1.$SwitchMap$ohos$data$rdb$RdbUtils$OperationType[operationType.ordinal()];
        String str = null;
        if (i == 1) {
            str = SqliteSqlBuilder.buildQueryString(absRdbPredicates, null);
        } else if (i == 2) {
            str = SqliteSqlBuilder.buildDeleteString(absRdbPredicates);
        } else if (i != 3) {
            HiLog.error(LABEL, "no type matched in verifyPredicates", new Object[0]);
        } else {
            str = SqliteSqlBuilder.buildCountString(absRdbPredicates);
        }
        try {
            getThreadSession().prepare(str, true);
        } finally {
            releaseRef();
        }
    }

    /* renamed from: ohos.data.rdb.impl.RdbStoreImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$data$rdb$RdbUtils$OperationType = new int[RdbUtils.OperationType.values().length];

        static {
            try {
                $SwitchMap$ohos$data$rdb$RdbUtils$OperationType[RdbUtils.OperationType.QUERY_TYPE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$data$rdb$RdbUtils$OperationType[RdbUtils.OperationType.DELETE_TYPE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$data$rdb$RdbUtils$OperationType[RdbUtils.OperationType.COUNT_TYPE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    @Override // ohos.data.rdb.RdbStore
    public void configLocale(Locale locale) {
        if (locale != null) {
            synchronized (this.parametersLock) {
                if (!isOpen()) {
                    throw new IllegalStateException("Config locale on a no-open database.");
                } else if (!isInTransaction()) {
                    this.connectionPool.configLocale(locale);
                } else {
                    HiLog.info(LABEL, "The rdb is in transaction. %{private}s", new Object[]{getPath()});
                    throw new IllegalArgumentException("The rdb is in transaction.");
                }
            }
            return;
        }
        throw new IllegalArgumentException("Locale must not be null.");
    }
}
