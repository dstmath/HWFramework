package android.database.sqlite;

import android.database.CursorWindow;
import android.database.DatabaseUtils;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import java.util.HashMap;

public final class SQLiteSession {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int DELETE_STATUS = 2;
    public static final int INSERT_STATUS = 0;
    public static final int TRANSACTION_MODE_DEFERRED = 0;
    public static final int TRANSACTION_MODE_EXCLUSIVE = 2;
    public static final int TRANSACTION_MODE_IMMEDIATE = 1;
    public static final int UPDATE_STATUS = 1;
    private boolean mCommitSuccessful = false;
    private SQLiteConnection mConnection;
    private int mConnectionFlags;
    private final SQLiteConnectionPool mConnectionPool;
    private int mConnectionUseCount;
    private HashMap<SQLInfo, Integer> mTransactionMap;
    private Transaction mTransactionPool;
    private Transaction mTransactionStack;

    private static final class Transaction {
        public boolean mChildFailed;
        public SQLiteTransactionListener mListener;
        public boolean mMarkedSuccessful;
        public int mMode;
        public Transaction mParent;

        private Transaction() {
        }
    }

    public SQLiteSession(SQLiteConnectionPool connectionPool) {
        if (connectionPool != null) {
            this.mConnectionPool = connectionPool;
            this.mTransactionMap = new HashMap<>();
            return;
        }
        throw new IllegalArgumentException("connectionPool must not be null");
    }

    public boolean hasTransaction() {
        return this.mTransactionStack != null;
    }

    public boolean hasNestedTransaction() {
        return (this.mTransactionStack == null || this.mTransactionStack.mParent == null) ? false : true;
    }

    public boolean hasConnection() {
        return this.mConnection != null;
    }

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
                switch (transactionMode) {
                    case 1:
                        this.mConnection.execute("BEGIN IMMEDIATE;", null, cancellationSignal);
                        break;
                    case 2:
                        this.mConnection.execute("BEGIN EXCLUSIVE;", null, cancellationSignal);
                        break;
                    default:
                        this.mConnection.execute("BEGIN;", null, cancellationSignal);
                        break;
                }
            }
            if (transactionListener != null) {
                transactionListener.onBegin();
            }
            Transaction transaction = obtainTransaction(transactionMode, transactionListener);
            transaction.mParent = this.mTransactionStack;
            this.mTransactionStack = transaction;
            if (this.mTransactionStack == null) {
                releaseConnection();
            }
        } catch (RuntimeException ex) {
            if (this.mTransactionStack == null) {
                this.mConnection.execute("ROLLBACK;", null, cancellationSignal);
            }
            throw ex;
        } catch (Throwable th) {
            if (this.mTransactionStack == null) {
                releaseConnection();
            }
            throw th;
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
        if (this.mTransactionStack != null) {
            if (!successful) {
                this.mTransactionStack.mChildFailed = true;
            }
        } else if (successful) {
            try {
                this.mConnection.execute("COMMIT;", null, cancellationSignal);
                this.mCommitSuccessful = true;
            } finally {
                releaseConnection();
            }
        } else {
            this.mConnection.execute("ROLLBACK;", null, cancellationSignal);
            this.mCommitSuccessful = false;
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
        } else if (this.mTransactionStack == null || this.mTransactionStack.mMarkedSuccessful || this.mTransactionStack.mParent != null) {
            return false;
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
        String str = sql;
        int i = connectionFlags;
        CancellationSignal cancellationSignal2 = cancellationSignal;
        if (str == null) {
            Object[] objArr = bindArgs;
            throw new IllegalArgumentException("sql must not be null.");
        } else if (window != null) {
            Object[] objArr2 = bindArgs;
            if (executeSpecial(str, objArr2, i, cancellationSignal2)) {
                window.clear();
                return 0;
            }
            acquireConnection(str, i, cancellationSignal2);
            try {
                return this.mConnection.executeForCursorWindow(str, objArr2, window, startPos, requiredPos, countAllRows, cancellationSignal2);
            } finally {
                releaseConnection();
            }
        } else {
            Object[] objArr3 = bindArgs;
            throw new IllegalArgumentException("window must not be null.");
        }
    }

    private boolean executeSpecial(String sql, Object[] bindArgs, int connectionFlags, CancellationSignal cancellationSignal) {
        if (cancellationSignal != null) {
            cancellationSignal.throwIfCanceled();
        }
        switch (DatabaseUtils.getSqlStatementType(sql)) {
            case 4:
                beginTransaction(2, null, connectionFlags, cancellationSignal);
                return true;
            case 5:
                setTransactionSuccessful();
                endTransaction(cancellationSignal);
                return true;
            case 6:
                endTransaction(cancellationSignal);
                return true;
            default:
                return false;
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
        if (this.mTransactionStack != null && this.mTransactionStack.mMarkedSuccessful) {
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

    public boolean isCommitSuccess() {
        return this.mCommitSuccessful;
    }

    public void insertTransMap(String table, long primaryKey, int status) {
        this.mTransactionMap.put(new SQLInfo(table, primaryKey), Integer.valueOf(status));
    }

    public void clearTransMap() {
        this.mTransactionMap.clear();
    }

    public HashMap<SQLInfo, Integer> getTransMap() {
        return this.mTransactionMap;
    }
}
