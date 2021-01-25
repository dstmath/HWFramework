package ohos.data.rdb.impl;

import java.util.Arrays;
import java.util.Stack;
import ohos.data.rdb.TransactionObserver;
import ohos.data.resultset.SharedBlock;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class StoreSession {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final String ATTACH_BACKUP_SQL = "ATTACH ? AS backup KEY ?";
    private static final String DETACH_BACKUP_SQL = "detach backup";
    private static final String EXPORT_SQL = "SELECT export_database('backup')";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "StoreSession");
    private Connection connection;
    private final ConnectionPool connectionPool;
    private int connectionUseCount = 0;
    private Stack<Transaction> idleTransactions;
    private boolean isStepCursor = false;
    private Stack<Transaction> transactionStack;

    public StoreSession(ConnectionPool connectionPool2) {
        if (connectionPool2 != null) {
            this.connectionPool = connectionPool2;
            this.transactionStack = new Stack<>();
            this.idleTransactions = new Stack<>();
            return;
        }
        throw new IllegalArgumentException("connectionPool must not be null");
    }

    public SqliteStatementInfo prepare(String str, boolean z) {
        acquireConnection(str, z);
        try {
            return this.connection.prepare(str);
        } finally {
            releaseConnection();
        }
    }

    public void execute(String str, Object[] objArr, boolean z) {
        if (str == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (!executeSpecial(str)) {
            acquireConnection(str, z);
            try {
                this.connection.execute(str, objArr);
            } finally {
                releaseConnection();
            }
        }
    }

    public long executeGetLong(String str, Object[] objArr, boolean z) {
        if (str == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (executeSpecial(str)) {
            return 0;
        } else {
            acquireConnection(str, z);
            try {
                return this.connection.executeGetLong(str, objArr);
            } finally {
                releaseConnection();
            }
        }
    }

    public String executeGetString(String str, Object[] objArr, boolean z) {
        if (str == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (executeSpecial(str)) {
            return null;
        } else {
            acquireConnection(str, z);
            try {
                return this.connection.executeGetString(str, objArr);
            } finally {
                releaseConnection();
            }
        }
    }

    public int executeForChanges(String str, Object[] objArr, boolean z) {
        if (str == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (executeSpecial(str)) {
            return 0;
        } else {
            acquireConnection(str, z);
            try {
                return this.connection.executeForChanges(str, objArr);
            } finally {
                releaseConnection();
            }
        }
    }

    public long executeForLastInsertRowId(String str, Object[] objArr, boolean z) {
        if (str == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (executeSpecial(str)) {
            return 0;
        } else {
            acquireConnection(str, z);
            try {
                return this.connection.executeForLastInsertRowId(str, objArr);
            } finally {
                releaseConnection();
            }
        }
    }

    public int executeForSharedBlock(String str, Object[] objArr, SharedBlock sharedBlock, int i, int i2, boolean z, boolean z2) {
        if (str == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (sharedBlock == null) {
            throw new IllegalArgumentException("sharedBlock must not be null.");
        } else if (executeSpecial(str)) {
            sharedBlock.clear();
            return 0;
        } else {
            acquireConnection(str, z2);
            try {
                return this.connection.executeForSharedBlock(str, objArr, sharedBlock, i, i2, z);
            } finally {
                releaseConnection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public PrecompiledStatement beginStepQuery(String str, boolean z) {
        acquireConnection(str, z);
        this.isStepCursor = true;
        return this.connection.beginStepQuery(str);
    }

    /* access modifiers changed from: package-private */
    public int executeForStepQuery(String str, Object[] objArr) {
        if (str != null) {
            return this.connection.executeForStepQuery(str, objArr);
        }
        throw new IllegalArgumentException("sql must not be null.");
    }

    /* access modifiers changed from: package-private */
    public void endStepQuery(PrecompiledStatement precompiledStatement) {
        if (this.isStepCursor) {
            try {
                this.connection.endStepQuery(precompiledStatement);
                this.isStepCursor = false;
            } finally {
                releaseConnection();
            }
        } else {
            throw new IllegalStateException("end an unopened step query");
        }
    }

    /* access modifiers changed from: package-private */
    public void resetStatement(PrecompiledStatement precompiledStatement) {
        if (this.isStepCursor) {
            this.connection.resetStatement(precompiledStatement);
            return;
        }
        throw new IllegalStateException("end an unopened step query");
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void backup(String str, SqliteEncryptKeyLoader sqliteEncryptKeyLoader) {
        byte[] encryptKey = sqliteEncryptKeyLoader.getEncryptKey();
        try {
            acquireConnection(null, true);
            if (sqliteEncryptKeyLoader.isEmpty()) {
                this.connection.execute(ATTACH_BACKUP_SQL, new Object[]{str, ""});
            } else {
                this.connection.execute(ATTACH_BACKUP_SQL, new Object[]{str, encryptKey});
            }
            try {
                this.connection.executeGetLong(EXPORT_SQL, null);
                this.connection.execute(DETACH_BACKUP_SQL, null);
                if (!sqliteEncryptKeyLoader.isEmpty()) {
                    Arrays.fill(encryptKey, (byte) 0);
                }
                releaseConnection();
            } catch (Throwable th) {
                this.connection.execute(DETACH_BACKUP_SQL, null);
                throw th;
            }
        } finally {
            if (!sqliteEncryptKeyLoader.isEmpty()) {
                Arrays.fill(encryptKey, (byte) 0);
            }
            releaseConnection();
        }
    }

    /* access modifiers changed from: package-private */
    public void beginTransaction(TransactionObserver transactionObserver) {
        if (this.transactionStack.empty()) {
            acquireConnection(null, false);
        }
        try {
            if (this.transactionStack.empty()) {
                this.connection.execute("BEGIN EXCLUSIVE;", null);
            }
            if (transactionObserver != null) {
                try {
                    transactionObserver.onBegin();
                } catch (RuntimeException e) {
                    if (this.transactionStack.empty()) {
                        this.connection.execute("ROLLBACK;", null);
                    }
                    throw e;
                }
            }
            this.transactionStack.push(createTransaction(transactionObserver));
        } finally {
            if (this.transactionStack.empty()) {
                releaseConnection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void markAsCommit() {
        this.transactionStack.peek().setMarkedSuccessful(true);
    }

    /* access modifiers changed from: package-private */
    public void endTransaction() {
        endTransactionInternal();
    }

    private void endTransactionInternal() {
        RuntimeException runtimeException;
        checkNoTransaction();
        Transaction pop = this.transactionStack.pop();
        boolean z = pop.isAllBeforeSuccessful() && pop.isMarkedSuccessful();
        TransactionObserver observer = pop.getObserver();
        if (observer != null) {
            if (z) {
                try {
                    observer.onCommit();
                } catch (RuntimeException e) {
                    runtimeException = e;
                    z = false;
                }
            } else {
                observer.onRollback();
            }
        }
        runtimeException = null;
        recycleTransaction(pop);
        if (this.transactionStack.isEmpty()) {
            if (z) {
                try {
                    this.connection.execute("COMMIT;", null);
                } catch (Throwable th) {
                    releaseConnection();
                    throw th;
                }
            } else {
                this.connection.execute("ROLLBACK;", null);
            }
            releaseConnection();
        } else if (!z) {
            this.transactionStack.peek().setAllBeforeSuccessful(false);
        }
        if (runtimeException != null) {
            throw runtimeException;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean inTransaction() {
        return !this.transactionStack.empty();
    }

    /* access modifiers changed from: package-private */
    public void giveConnectionTemporarily(long j) {
        checkNoTransaction();
        Transaction peek = this.transactionStack.peek();
        if (peek.isMarkedSuccessful || this.transactionStack.size() > 1) {
            throw new IllegalStateException("Can't giveConnectionTemporarily, because the transaction has been marked as committed or there is nested transaction in the current process.");
        }
        markAsCommit();
        TransactionObserver observer = peek.getObserver();
        endTransaction();
        if (j > 0) {
            try {
                Thread.sleep(j);
            } catch (InterruptedException e) {
                HiLog.info(LABEL, "giveConnectionTemporarily has an exception during sleep: %{public}s", new Object[]{e.getMessage()});
            }
        }
        beginTransaction(observer);
    }

    /* access modifiers changed from: package-private */
    public boolean isHoldingConnection() {
        return this.connection != null;
    }

    private Transaction createTransaction(TransactionObserver transactionObserver) {
        Transaction transaction;
        if (this.idleTransactions.isEmpty()) {
            transaction = new Transaction();
        } else {
            transaction = this.idleTransactions.pop();
        }
        transaction.setAllBeforeSuccessful(true);
        transaction.setObserver(transactionObserver);
        return transaction;
    }

    private void recycleTransaction(Transaction transaction) {
        transaction.setAllBeforeSuccessful(false);
        transaction.setMarkedSuccessful(false);
        transaction.setObserver(null);
        this.idleTransactions.push(transaction);
    }

    private void checkNoTransaction() {
        if (this.transactionStack.empty()) {
            throw new IllegalStateException("Cannot do the transaction operation, because there is no current transaction.");
        }
    }

    private boolean executeSpecial(String str) {
        int sqlStatementType = SqliteDatabaseUtils.getSqlStatementType(str);
        if (sqlStatementType == 5) {
            beginTransaction(null);
            return true;
        } else if (sqlStatementType == 6) {
            markAsCommit();
            endTransactionInternal();
            return true;
        } else if (sqlStatementType != 7) {
            return false;
        } else {
            endTransactionInternal();
            return true;
        }
    }

    private void acquireConnection(String str, boolean z) {
        if (this.connection == null) {
            this.connection = this.connectionPool.acquireConnection(str, z);
        }
        this.connectionUseCount++;
    }

    private void releaseConnection() {
        int i;
        Connection connection2 = this.connection;
        if (connection2 == null || (i = this.connectionUseCount) <= 0) {
            HiLog.info(LABEL, "SQLiteSession releaseConnection faild.", new Object[0]);
            return;
        }
        int i2 = i - 1;
        this.connectionUseCount = i2;
        if (i2 == 0) {
            try {
                this.connectionPool.releaseConnection(connection2);
            } finally {
                this.connection = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class Transaction {
        private boolean isAllBeforeSuccessful;
        private boolean isMarkedSuccessful;
        private TransactionObserver observer;

        private Transaction() {
        }

        public boolean isAllBeforeSuccessful() {
            return this.isAllBeforeSuccessful;
        }

        public void setAllBeforeSuccessful(boolean z) {
            this.isAllBeforeSuccessful = z;
        }

        public TransactionObserver getObserver() {
            return this.observer;
        }

        public void setObserver(TransactionObserver transactionObserver) {
            this.observer = transactionObserver;
        }

        public boolean isMarkedSuccessful() {
            return this.isMarkedSuccessful;
        }

        public void setMarkedSuccessful(boolean z) {
            this.isMarkedSuccessful = z;
        }
    }
}
