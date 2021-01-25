package android.database.sqlite;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.database.CursorWindow;
import android.database.DatabaseUtils;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

public final class SQLiteSession {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int TRANSACTION_MODE_DEFERRED = 0;
    public static final int TRANSACTION_MODE_EXCLUSIVE = 2;
    public static final int TRANSACTION_MODE_IMMEDIATE = 1;
    private SQLiteConnection mConnection;
    private int mConnectionFlags;
    private final SQLiteConnectionPool mConnectionPool;
    private int mConnectionUseCount;
    private final IHwSQLiteSession mHwSQLiteSession;
    private Transaction mTransactionPool;
    private Transaction mTransactionStack;

    public SQLiteSession(SQLiteConnectionPool connectionPool) {
        if (connectionPool != null) {
            this.mConnectionPool = connectionPool;
            this.mHwSQLiteSession = HwFrameworkFactory.getHwSQLiteSession();
            return;
        }
        throw new IllegalArgumentException("connectionPool must not be null");
    }

    public boolean hasTransaction() {
        return this.mTransactionStack != null;
    }

    public boolean hasNestedTransaction() {
        Transaction transaction = this.mTransactionStack;
        return (transaction == null || transaction.mParent == null) ? false : true;
    }

    public boolean hasConnection() {
        return this.mConnection != null;
    }

    @UnsupportedAppUsage
    public void beginTransaction(int transactionMode, SQLiteTransactionListener transactionListener, int connectionFlags, CancellationSignal cancellationSignal) {
        throwIfTransactionMarkedSuccessful();
        beginTransactionUnchecked(transactionMode, transactionListener, connectionFlags, cancellationSignal);
    }

    private void beginTransactionUnchecked(int transactionMode, SQLiteTransactionListener transactionListener, int connectionFlags, CancellationSignal cancellationSignal) {
        if (cancellationSignal != null) {
            cancellationSignal.throwIfCanceled();
        }
        if (this.mTransactionStack == null) {
            acquireConnection(null, connectionFlags, cancellationSignal);
        }
        try {
            if (this.mTransactionStack == null) {
                if (transactionMode == 1) {
                    this.mConnection.execute("BEGIN IMMEDIATE;", null, cancellationSignal);
                } else if (transactionMode != 2) {
                    this.mConnection.execute("BEGIN;", null, cancellationSignal);
                } else {
                    this.mConnection.execute("BEGIN EXCLUSIVE;", null, cancellationSignal);
                }
            }
            if (transactionListener != null) {
                try {
                    transactionListener.onBegin();
                } catch (RuntimeException ex) {
                    if (this.mTransactionStack == null) {
                        this.mConnection.execute("ROLLBACK;", null, cancellationSignal);
                    }
                    throw ex;
                }
            }
            Transaction transaction = obtainTransaction(transactionMode, transactionListener);
            transaction.mParent = this.mTransactionStack;
            this.mTransactionStack = transaction;
        } finally {
            if (this.mTransactionStack == null) {
                releaseConnection();
            }
        }
    }

    public void setTransactionSuccessful() {
        throwIfNoTransaction();
        throwIfTransactionMarkedSuccessful();
        this.mTransactionStack.mMarkedSuccessful = true;
    }

    public void endTransaction(CancellationSignal cancellationSignal) {
        throwIfNoTransaction();
        endTransactionUnchecked(cancellationSignal, false);
    }

    private void endTransactionUnchecked(CancellationSignal cancellationSignal, boolean yielding) {
        if (cancellationSignal != null) {
            cancellationSignal.throwIfCanceled();
        }
        Transaction top = this.mTransactionStack;
        boolean successful = (top.mMarkedSuccessful || yielding) && !top.mChildFailed;
        RuntimeException listenerException = null;
        SQLiteTransactionListener listener = top.mListener;
        if (listener != null) {
            if (successful) {
                try {
                    listener.onCommit();
                } catch (RuntimeException ex) {
                    listenerException = ex;
                    successful = false;
                }
            } else {
                listener.onRollback();
            }
        }
        this.mTransactionStack = top.mParent;
        recycleTransaction(top);
        Transaction transaction = this.mTransactionStack;
        if (transaction == null) {
            if (successful) {
                try {
                    this.mConnection.execute("COMMIT;", null, cancellationSignal);
                    if (this.mHwSQLiteSession != null) {
                        this.mHwSQLiteSession.setCommitSuccessOrFail(true);
                    }
                } catch (Throwable th) {
                    releaseConnection();
                    throw th;
                }
            } else {
                this.mConnection.execute("ROLLBACK;", null, cancellationSignal);
                if (this.mHwSQLiteSession != null) {
                    this.mHwSQLiteSession.setCommitSuccessOrFail(false);
                }
            }
            releaseConnection();
        } else if (!successful) {
            transaction.mChildFailed = true;
        }
        if (listenerException != null) {
            throw listenerException;
        }
    }

    public boolean yieldTransaction(long sleepAfterYieldDelayMillis, boolean throwIfUnsafe, CancellationSignal cancellationSignal) {
        if (throwIfUnsafe) {
            throwIfNoTransaction();
            throwIfTransactionMarkedSuccessful();
            throwIfNestedTransaction();
        } else {
            Transaction transaction = this.mTransactionStack;
            if (transaction == null || transaction.mMarkedSuccessful || this.mTransactionStack.mParent != null) {
                return false;
            }
        }
        if (this.mTransactionStack.mChildFailed) {
            return false;
        }
        return yieldTransactionUnchecked(sleepAfterYieldDelayMillis, cancellationSignal);
    }

    private boolean yieldTransactionUnchecked(long sleepAfterYieldDelayMillis, CancellationSignal cancellationSignal) {
        if (cancellationSignal != null) {
            cancellationSignal.throwIfCanceled();
        }
        if (!this.mConnectionPool.shouldYieldConnection(this.mConnection, this.mConnectionFlags)) {
            return false;
        }
        int transactionMode = this.mTransactionStack.mMode;
        SQLiteTransactionListener listener = this.mTransactionStack.mListener;
        int connectionFlags = this.mConnectionFlags;
        endTransactionUnchecked(cancellationSignal, true);
        if (sleepAfterYieldDelayMillis > 0) {
            try {
                Thread.sleep(sleepAfterYieldDelayMillis);
            } catch (InterruptedException e) {
            }
        }
        beginTransactionUnchecked(transactionMode, listener, connectionFlags, cancellationSignal);
        return true;
    }

    public void prepare(String sql, int connectionFlags, CancellationSignal cancellationSignal, SQLiteStatementInfo outStatementInfo) {
        if (sql != null) {
            if (cancellationSignal != null) {
                cancellationSignal.throwIfCanceled();
            }
            acquireConnection(sql, connectionFlags, cancellationSignal);
            try {
                this.mConnection.prepare(sql, outStatementInfo);
            } finally {
                releaseConnection();
            }
        } else {
            throw new IllegalArgumentException("sql must not be null.");
        }
    }

    public void execute(String sql, Object[] bindArgs, int connectionFlags, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (!executeSpecial(sql, bindArgs, connectionFlags, cancellationSignal)) {
            acquireConnection(sql, connectionFlags, cancellationSignal);
            try {
                this.mConnection.execute(sql, bindArgs, cancellationSignal);
            } finally {
                releaseConnection();
            }
        }
    }

    public long executeForLong(String sql, Object[] bindArgs, int connectionFlags, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (executeSpecial(sql, bindArgs, connectionFlags, cancellationSignal)) {
            return 0;
        } else {
            acquireConnection(sql, connectionFlags, cancellationSignal);
            try {
                return this.mConnection.executeForLong(sql, bindArgs, cancellationSignal);
            } finally {
                releaseConnection();
            }
        }
    }

    public String executeForString(String sql, Object[] bindArgs, int connectionFlags, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (executeSpecial(sql, bindArgs, connectionFlags, cancellationSignal)) {
            return null;
        } else {
            acquireConnection(sql, connectionFlags, cancellationSignal);
            try {
                return this.mConnection.executeForString(sql, bindArgs, cancellationSignal);
            } finally {
                releaseConnection();
            }
        }
    }

    public ParcelFileDescriptor executeForBlobFileDescriptor(String sql, Object[] bindArgs, int connectionFlags, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (executeSpecial(sql, bindArgs, connectionFlags, cancellationSignal)) {
            return null;
        } else {
            acquireConnection(sql, connectionFlags, cancellationSignal);
            try {
                return this.mConnection.executeForBlobFileDescriptor(sql, bindArgs, cancellationSignal);
            } finally {
                releaseConnection();
            }
        }
    }

    public int executeForChangedRowCount(String sql, Object[] bindArgs, int connectionFlags, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (executeSpecial(sql, bindArgs, connectionFlags, cancellationSignal)) {
            return 0;
        } else {
            acquireConnection(sql, connectionFlags, cancellationSignal);
            try {
                return this.mConnection.executeForChangedRowCount(sql, bindArgs, cancellationSignal);
            } finally {
                releaseConnection();
            }
        }
    }

    public long executeForLastInsertedRowId(String sql, Object[] bindArgs, int connectionFlags, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (executeSpecial(sql, bindArgs, connectionFlags, cancellationSignal)) {
            return 0;
        } else {
            acquireConnection(sql, connectionFlags, cancellationSignal);
            try {
                return this.mConnection.executeForLastInsertedRowId(sql, bindArgs, cancellationSignal);
            } finally {
                releaseConnection();
            }
        }
    }

    public int executeForCursorWindow(String sql, Object[] bindArgs, CursorWindow window, int startPos, int requiredPos, boolean countAllRows, int connectionFlags, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (window == null) {
            throw new IllegalArgumentException("window must not be null.");
        } else if (executeSpecial(sql, bindArgs, connectionFlags, cancellationSignal)) {
            window.clear();
            return 0;
        } else {
            acquireConnection(sql, connectionFlags, cancellationSignal);
            try {
                return this.mConnection.executeForCursorWindow(sql, bindArgs, window, startPos, requiredPos, countAllRows, cancellationSignal);
            } finally {
                releaseConnection();
            }
        }
    }

    private boolean executeSpecial(String sql, Object[] bindArgs, int connectionFlags, CancellationSignal cancellationSignal) {
        if (cancellationSignal != null) {
            cancellationSignal.throwIfCanceled();
        }
        int type = DatabaseUtils.getSqlStatementType(sql);
        if (type == 4) {
            beginTransaction(2, null, connectionFlags, cancellationSignal);
            return true;
        } else if (type == 5) {
            setTransactionSuccessful();
            endTransaction(cancellationSignal);
            return true;
        } else if (type != 6) {
            return false;
        } else {
            endTransaction(cancellationSignal);
            return true;
        }
    }

    private void acquireConnection(String sql, int connectionFlags, CancellationSignal cancellationSignal) {
        if (this.mConnection == null) {
            this.mConnection = this.mConnectionPool.acquireConnection(sql, connectionFlags, cancellationSignal);
            this.mConnectionFlags = connectionFlags;
        }
        this.mConnectionUseCount++;
    }

    private void releaseConnection() {
        int i = this.mConnectionUseCount - 1;
        this.mConnectionUseCount = i;
        if (i == 0) {
            try {
                this.mConnectionPool.releaseConnection(this.mConnection);
            } finally {
                this.mConnection = null;
            }
        }
    }

    private void throwIfNoTransaction() {
        if (this.mTransactionStack == null) {
            throw new IllegalStateException("Cannot perform this operation because there is no current transaction.");
        }
    }

    private void throwIfTransactionMarkedSuccessful() {
        Transaction transaction = this.mTransactionStack;
        if (transaction != null && transaction.mMarkedSuccessful) {
            throw new IllegalStateException("Cannot perform this operation because the transaction has already been marked successful.  The only thing you can do now is call endTransaction().");
        }
    }

    private void throwIfNestedTransaction() {
        if (hasNestedTransaction()) {
            throw new IllegalStateException("Cannot perform this operation because a nested transaction is in progress.");
        }
    }

    private Transaction obtainTransaction(int mode, SQLiteTransactionListener listener) {
        Transaction transaction = this.mTransactionPool;
        if (transaction != null) {
            this.mTransactionPool = transaction.mParent;
            transaction.mParent = null;
            transaction.mMarkedSuccessful = false;
            transaction.mChildFailed = false;
        } else {
            transaction = new Transaction();
        }
        transaction.mMode = mode;
        transaction.mListener = listener;
        return transaction;
    }

    private void recycleTransaction(Transaction transaction) {
        transaction.mParent = this.mTransactionPool;
        transaction.mListener = null;
        this.mTransactionPool = transaction;
    }

    /* access modifiers changed from: private */
    public static final class Transaction {
        public boolean mChildFailed;
        public SQLiteTransactionListener mListener;
        public boolean mMarkedSuccessful;
        public int mMode;
        public Transaction mParent;

        private Transaction() {
        }
    }

    public IHwSQLiteSession getHwSQLiteSession() {
        return this.mHwSQLiteSession;
    }
}
