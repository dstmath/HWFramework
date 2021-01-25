package ohos.data.rdb.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.Stack;
import ohos.data.rdb.RdbException;
import ohos.data.rdb.StoreConfig;
import ohos.data.resultset.SharedBlock;
import ohos.data.utils.LruCache;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class SqliteConnection implements Connection {
    private static final int DEFAULT_POS = -1;
    static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "SQLiteConnection");
    private static final int POS_COUNT = 32;
    private final int connectionId;
    private long connectionPtr = 0;
    private final SqliteDatabaseConfig databaseConfig;
    private final boolean isReadOnly;
    private final boolean isWriteConnection;
    private PrecompiledStatementCache precompiledStatementCache;
    private Stack<PrecompiledStatement> precompiledStatementPool;
    private PrecompiledStatement stepQueryStatement;
    private boolean stepQueryStatementBindArgs = false;

    private static native void nativeBindBlob(long j, long j2, int i, byte[] bArr);

    private static native void nativeBindDouble(long j, long j2, int i, double d);

    private static native void nativeBindLong(long j, long j2, int i, long j3);

    private static native void nativeBindNull(long j, long j2, int i);

    private static native void nativeBindString(long j, long j2, int i, String str);

    private static native void nativeClose(long j);

    private static native void nativeExecute(long j, long j2);

    private static native int nativeExecuteForChanges(long j, long j2);

    private static native long nativeExecuteForLastInsertRowId(long j, long j2);

    private static native long nativeExecuteForSharedBlock(long j, long j2, long j3, int i, int i2, boolean z);

    private static native int nativeExecuteForStepQuery(long j, long j2);

    private static native long nativeExecuteGetLong(long j, long j2);

    private static native String nativeExecuteGetString(long j, long j2);

    private static native void nativeFinalizeStatement(long j, long j2);

    private static native void nativeKey(long j, byte[] bArr);

    private static native long nativeOpen(String str, int i);

    private static native long nativePrepareStatement(long j, String str, SqliteStatementInfo sqliteStatementInfo);

    private static native void nativeRekey(long j, byte[] bArr);

    private static native void nativeResetStatement(long j, long j2);

    private static native void nativeResetStatementAndClearBindings(long j, long j2);

    private static boolean needCache(int i) {
        return i == 2 || i == 1;
    }

    static {
        System.loadLibrary("appdatamgr_jni.z");
    }

    public SqliteConnection(SqliteDatabaseConfig sqliteDatabaseConfig, int i, boolean z) {
        this.databaseConfig = sqliteDatabaseConfig;
        this.connectionId = i;
        this.isWriteConnection = z;
        boolean z2 = true;
        if (z && (sqliteDatabaseConfig.getOpenFlags() & 1) == 0) {
            z2 = false;
        }
        this.isReadOnly = z2;
        this.precompiledStatementCache = new PrecompiledStatementCache();
        this.precompiledStatementPool = new Stack<>();
    }

    static Connection open(SqliteDatabaseConfig sqliteDatabaseConfig, int i, boolean z) {
        SqliteConnection sqliteConnection = new SqliteConnection(sqliteDatabaseConfig, i, z);
        sqliteConnection.open();
        return sqliteConnection;
    }

    private void open() {
        this.connectionPtr = nativeOpen(this.databaseConfig.getPath(), this.databaseConfig.getOpenFlags());
        setPageSize();
        setEncryptKey();
        setForeignKeyMode(true);
        setJournalMode();
        setJournalSizeLimit();
        setAutoCheckpoint();
        setSyncMode();
    }

    private void setAutoCheckpoint() {
        if (!this.databaseConfig.isMemoryDb() && !this.isReadOnly) {
            long executeGetLong = executeGetLong("PRAGMA wal_autocheckpoint=" + SqliteGlobalConfig.getWalAutoCheckpoint(), null);
            if (executeGetLong != ((long) SqliteGlobalConfig.getWalAutoCheckpoint())) {
                HiLog.info(LABEL, "set auto checkpoint failed.%{public}d.", new Object[]{Long.valueOf(executeGetLong)});
            }
        }
    }

    private void setJournalSizeLimit() {
        if (!this.databaseConfig.isMemoryDb() && !this.isReadOnly) {
            long executeGetLong = executeGetLong("PRAGMA journal_size_limit=" + SqliteGlobalConfig.getJournalFileSize(), null);
            if (executeGetLong != ((long) SqliteGlobalConfig.getJournalFileSize())) {
                HiLog.info(LABEL, "set journal file size failed.%{public}d.", new Object[]{Long.valueOf(executeGetLong)});
            }
        }
    }

    private void setEncryptKey() {
        byte[] encryptKey = this.databaseConfig.getEncryptKeyLoader().getEncryptKey();
        if (encryptKey != null && encryptKey.length != 0) {
            try {
                nativeKey(this.connectionPtr, encryptKey);
            } finally {
                Arrays.fill(encryptKey, (byte) 0);
            }
        }
    }

    private void setPageSize() {
        if (!this.databaseConfig.isMemoryDb() && !this.isReadOnly) {
            execute("PRAGMA page_size=" + SqliteGlobalConfig.getPageSize(), null);
        }
    }

    private void setForeignKeyMode(boolean z) {
        if (!this.isReadOnly) {
            execute("PRAGMA foreign_keys=" + (z ? 1 : 0), null);
        }
    }

    private void setJournalMode() {
        if (!this.databaseConfig.isMemoryDb() && !this.isReadOnly) {
            String executeGetString = executeGetString("PRAGMA journal_mode", null);
            String journalMode = this.databaseConfig.getJournalMode();
            if (!executeGetString.equalsIgnoreCase(journalMode)) {
                String executeGetString2 = executeGetString("PRAGMA journal_mode=" + journalMode, null);
                if (!journalMode.equalsIgnoreCase(executeGetString2)) {
                    HiLog.info(LABEL, "set journal mode failed.%{public}s.", new Object[]{executeGetString2});
                }
            }
        }
    }

    private void setSyncMode() {
        String str;
        int intValue = Integer.valueOf(executeGetString("PRAGMA synchronous", null)).intValue();
        if (intValue == 0) {
            str = StoreConfig.SyncMode.MODE_OFF.getValue();
        } else if (intValue == 1) {
            str = StoreConfig.SyncMode.MODE_NORMAL.getValue();
        } else if (intValue != 2) {
            str = intValue != 3 ? null : StoreConfig.SyncMode.MODE_EXTRA.getValue();
        } else {
            str = StoreConfig.SyncMode.MODE_FULL.getValue();
        }
        String syncMode = this.databaseConfig.getSyncMode();
        if (str != null && !str.equalsIgnoreCase(syncMode)) {
            execute("PRAGMA synchronous=" + syncMode, null);
        }
    }

    @Override // ohos.data.rdb.impl.Connection
    public boolean isWriteConnection() {
        return this.isWriteConnection;
    }

    @Override // ohos.data.rdb.impl.Connection
    public boolean isPrecompiledStatementInCache(String str) {
        return this.precompiledStatementCache.get(str) != null;
    }

    @Override // ohos.data.rdb.impl.Connection
    public long executeForLastInsertRowId(String str, Object[] objArr) {
        if (str != null) {
            PrecompiledStatement acquirePrecompiledStatement = acquirePrecompiledStatement(str, false);
            try {
                checkStatementAndConnectionStatus(acquirePrecompiledStatement);
                bindArguments(acquirePrecompiledStatement, objArr);
                return nativeExecuteForLastInsertRowId(this.connectionPtr, acquirePrecompiledStatement.getStatementPtr());
            } finally {
                releasePrecompiledStatement(acquirePrecompiledStatement);
            }
        } else {
            throw new IllegalArgumentException("sql must not be null.");
        }
    }

    private void bindArguments(PrecompiledStatement precompiledStatement, Object[] objArr) {
        int length = objArr != null ? objArr.length : 0;
        if (length != precompiledStatement.getNumParameters()) {
            throw new IllegalArgumentException("Expected " + precompiledStatement.getNumParameters() + " bind arguments but " + length + " were provided.");
        } else if (length != 0) {
            long statementPtr = precompiledStatement.getStatementPtr();
            for (int i = 0; i < length; i++) {
                Object obj = objArr[i];
                int objectType = SqliteDatabaseUtils.getObjectType(obj);
                if (objectType == 0) {
                    nativeBindNull(this.connectionPtr, statementPtr, i + 1);
                } else if (objectType == 1) {
                    nativeBindLong(this.connectionPtr, statementPtr, i + 1, ((Number) obj).longValue());
                } else if (objectType == 2) {
                    nativeBindDouble(this.connectionPtr, statementPtr, i + 1, ((Number) obj).doubleValue());
                } else if (objectType == 4) {
                    nativeBindBlob(this.connectionPtr, statementPtr, i + 1, (byte[]) obj);
                } else if (objectType != 5) {
                    nativeBindString(this.connectionPtr, statementPtr, i + 1, obj.toString());
                } else {
                    nativeBindLong(this.connectionPtr, statementPtr, i + 1, ((Boolean) obj).booleanValue() ? 1 : 0);
                }
            }
        }
    }

    private void checkStatementAndConnectionStatus(PrecompiledStatement precompiledStatement) {
        if (!this.isWriteConnection && !precompiledStatement.isReadOnly()) {
            throw new IllegalStateException("a read connection can not execute write operation");
        }
    }

    private PrecompiledStatement acquirePrecompiledStatement(String str, boolean z) {
        boolean z2;
        PrecompiledStatement precompiledStatement = (PrecompiledStatement) this.precompiledStatementCache.get(str);
        if (precompiledStatement == null) {
            z2 = false;
        } else if (!precompiledStatement.isInUse()) {
            return precompiledStatement;
        } else {
            z2 = true;
        }
        SqliteStatementInfo sqliteStatementInfo = new SqliteStatementInfo();
        long nativePrepareStatement = nativePrepareStatement(this.connectionPtr, str, sqliteStatementInfo);
        int sqlStatementType = SqliteDatabaseUtils.getSqlStatementType(str);
        PrecompiledStatement createPrecompiledStatement = createPrecompiledStatement(str, nativePrepareStatement, sqlStatementType, sqliteStatementInfo);
        if (!z2 && (z || needCache(sqlStatementType))) {
            createPrecompiledStatement.setInCache(true);
            this.precompiledStatementCache.put(str, createPrecompiledStatement);
        }
        createPrecompiledStatement.setInUse(true);
        return createPrecompiledStatement;
    }

    private void releasePrecompiledStatement(PrecompiledStatement precompiledStatement) {
        precompiledStatement.setInUse(false);
        if (precompiledStatement.isInCache()) {
            try {
                nativeResetStatementAndClearBindings(this.connectionPtr, precompiledStatement.getStatementPtr());
            } catch (RdbException unused) {
                this.precompiledStatementCache.remove(precompiledStatement.getSql());
                finalizePrecompiledStatement(precompiledStatement);
            }
        } else {
            finalizePrecompiledStatement(precompiledStatement);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finalizePrecompiledStatement(PrecompiledStatement precompiledStatement) {
        nativeFinalizeStatement(this.connectionPtr, precompiledStatement.getStatementPtr());
        precompiledStatement.setSql(null);
        this.precompiledStatementPool.push(precompiledStatement);
    }

    private PrecompiledStatement createPrecompiledStatement(String str, long j, int i, SqliteStatementInfo sqliteStatementInfo) {
        PrecompiledStatement precompiledStatement;
        if (this.precompiledStatementPool.isEmpty()) {
            precompiledStatement = new PrecompiledStatement();
        } else {
            precompiledStatement = this.precompiledStatementPool.pop();
        }
        precompiledStatement.setSql(str);
        precompiledStatement.setStatementPtr(j);
        precompiledStatement.setType(i);
        precompiledStatement.setInfo(sqliteStatementInfo);
        precompiledStatement.setInCache(false);
        return precompiledStatement;
    }

    @Override // ohos.data.rdb.impl.Connection
    public int executeForChanges(String str, Object[] objArr) {
        if (str != null) {
            PrecompiledStatement acquirePrecompiledStatement = acquirePrecompiledStatement(str, false);
            try {
                checkStatementAndConnectionStatus(acquirePrecompiledStatement);
                bindArguments(acquirePrecompiledStatement, objArr);
                return nativeExecuteForChanges(this.connectionPtr, acquirePrecompiledStatement.getStatementPtr());
            } finally {
                releasePrecompiledStatement(acquirePrecompiledStatement);
            }
        } else {
            throw new IllegalArgumentException("sql must not be null.");
        }
    }

    @Override // ohos.data.rdb.impl.Connection
    public void execute(String str, Object[] objArr) {
        if (str != null) {
            PrecompiledStatement acquirePrecompiledStatement = acquirePrecompiledStatement(str, false);
            try {
                checkStatementAndConnectionStatus(acquirePrecompiledStatement);
                bindArguments(acquirePrecompiledStatement, objArr);
                nativeExecute(this.connectionPtr, acquirePrecompiledStatement.getStatementPtr());
            } finally {
                releasePrecompiledStatement(acquirePrecompiledStatement);
            }
        } else {
            throw new IllegalArgumentException("sql must not be null.");
        }
    }

    @Override // ohos.data.rdb.impl.Connection
    public long executeGetLong(String str, Object[] objArr) {
        if (str != null) {
            PrecompiledStatement acquirePrecompiledStatement = acquirePrecompiledStatement(str, false);
            try {
                checkStatementAndConnectionStatus(acquirePrecompiledStatement);
                bindArguments(acquirePrecompiledStatement, objArr);
                return nativeExecuteGetLong(this.connectionPtr, acquirePrecompiledStatement.getStatementPtr());
            } finally {
                releasePrecompiledStatement(acquirePrecompiledStatement);
            }
        } else {
            throw new IllegalArgumentException("sql must not be null.");
        }
    }

    @Override // ohos.data.rdb.impl.Connection
    public String executeGetString(String str, Object[] objArr) {
        if (str != null) {
            PrecompiledStatement acquirePrecompiledStatement = acquirePrecompiledStatement(str, false);
            try {
                checkStatementAndConnectionStatus(acquirePrecompiledStatement);
                bindArguments(acquirePrecompiledStatement, objArr);
                return nativeExecuteGetString(this.connectionPtr, acquirePrecompiledStatement.getStatementPtr());
            } finally {
                releasePrecompiledStatement(acquirePrecompiledStatement);
            }
        } else {
            throw new IllegalArgumentException("sql must not be null.");
        }
    }

    @Override // ohos.data.rdb.impl.Connection
    public SqliteStatementInfo prepare(String str) {
        if (str != null) {
            PrecompiledStatement acquirePrecompiledStatement = acquirePrecompiledStatement(str, true);
            releasePrecompiledStatement(acquirePrecompiledStatement);
            return acquirePrecompiledStatement.getInfo();
        }
        throw new IllegalArgumentException("sql must not be null.");
    }

    @Override // ohos.data.rdb.impl.Connection
    public PrecompiledStatement beginStepQuery(String str) {
        if (this.stepQueryStatement == null) {
            this.stepQueryStatement = acquirePrecompiledStatement(str, false);
            this.stepQueryStatementBindArgs = false;
            return this.stepQueryStatement;
        }
        throw new IllegalStateException("Begin a step query on a connection more than once");
    }

    @Override // ohos.data.rdb.impl.Connection
    public void endStepQuery(PrecompiledStatement precompiledStatement) {
        PrecompiledStatement precompiledStatement2 = this.stepQueryStatement;
        if (precompiledStatement2 == null) {
            throw new IllegalStateException("End a step query on a connection that never begin");
        } else if (precompiledStatement2 == precompiledStatement) {
            releasePrecompiledStatement(precompiledStatement2);
            this.stepQueryStatement = null;
            this.stepQueryStatementBindArgs = false;
        } else {
            throw new IllegalArgumentException("End a step query with an unknown statement object");
        }
    }

    @Override // ohos.data.rdb.impl.Connection
    public void resetStatement(PrecompiledStatement precompiledStatement) {
        PrecompiledStatement precompiledStatement2 = this.stepQueryStatement;
        if (precompiledStatement2 == null) {
            throw new IllegalStateException("Reset a step query on a connection that never begin");
        } else if (precompiledStatement2 == precompiledStatement) {
            nativeResetStatement(this.connectionPtr, precompiledStatement2.getStatementPtr());
        } else {
            throw new IllegalArgumentException("Reset a step query with an unknown statement object");
        }
    }

    @Override // ohos.data.rdb.impl.Connection
    public int executeForStepQuery(String str, Object[] objArr) {
        if (str != null) {
            PrecompiledStatement precompiledStatement = this.stepQueryStatement;
            if (precompiledStatement != null) {
                if (!this.stepQueryStatementBindArgs) {
                    bindArguments(precompiledStatement, objArr);
                    this.stepQueryStatementBindArgs = true;
                }
                return nativeExecuteForStepQuery(this.connectionPtr, this.stepQueryStatement.getStatementPtr());
            }
            throw new IllegalArgumentException("Must begin a step query on a connection before executing step query");
        }
        throw new IllegalArgumentException("sql must not be null.");
    }

    @Override // ohos.data.rdb.impl.Connection
    public void close() {
        this.precompiledStatementCache.clear();
        nativeClose(this.connectionPtr);
    }

    /* access modifiers changed from: private */
    public class PrecompiledStatementCache extends LruCache<String, PrecompiledStatement> {
        private static final int CACHE_SIZE = 25;

        PrecompiledStatementCache() {
            super(25);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.data.utils.LruCache
        public void onRemove(Map.Entry<String, PrecompiledStatement> entry) {
            SqliteConnection.this.finalizePrecompiledStatement(entry.getValue());
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.data.rdb.impl.Connection
    public int executeForSharedBlock(String str, Object[] objArr, SharedBlock sharedBlock, int i, int i2, boolean z) {
        if (str == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (sharedBlock != null) {
            sharedBlock.acquireRef();
            try {
                PrecompiledStatement acquirePrecompiledStatement = acquirePrecompiledStatement(str, false);
                try {
                    checkStatementAndConnectionStatus(acquirePrecompiledStatement);
                    bindArguments(acquirePrecompiledStatement, objArr);
                    long nativeExecuteForSharedBlock = nativeExecuteForSharedBlock(this.connectionPtr, acquirePrecompiledStatement.getStatementPtr(), sharedBlock.getBlockPtr(), i, i2, z);
                    int i3 = (int) nativeExecuteForSharedBlock;
                    sharedBlock.setStartRowIndex((int) (nativeExecuteForSharedBlock >> 32));
                    releasePrecompiledStatement(acquirePrecompiledStatement);
                    sharedBlock.releaseRef();
                    return i3;
                } catch (Throwable th) {
                    releasePrecompiledStatement(acquirePrecompiledStatement);
                    throw th;
                }
            } catch (RuntimeException e) {
                HiLog.info(LABEL, "executeForSharedBlock has error. sql = %{private}s.", new Object[]{str});
                throw e;
            } catch (Throwable th2) {
                sharedBlock.releaseRef();
                throw th2;
            }
        } else {
            throw new IllegalArgumentException("sharedBlock must not be null.");
        }
    }

    @Override // ohos.data.rdb.impl.Connection
    public void changeEncryptKey(SqliteEncryptKeyLoader sqliteEncryptKeyLoader) {
        byte[] encryptKey = sqliteEncryptKeyLoader.getEncryptKey();
        try {
            nativeRekey(this.connectionPtr, encryptKey);
        } finally {
            Arrays.fill(encryptKey, (byte) 0);
        }
    }
}
