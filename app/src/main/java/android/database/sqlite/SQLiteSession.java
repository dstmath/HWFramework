package android.database.sqlite;

import android.database.CursorWindow;
import android.database.DatabaseUtils;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.speech.SpeechRecognizer;
import android.telecom.AudioState;
import java.util.HashMap;

public final class SQLiteSession {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final int DELETE_STATUS = 2;
    public static final int INSERT_STATUS = 0;
    public static final int TRANSACTION_MODE_DEFERRED = 0;
    public static final int TRANSACTION_MODE_EXCLUSIVE = 2;
    public static final int TRANSACTION_MODE_IMMEDIATE = 1;
    public static final int UPDATE_STATUS = 1;
    private boolean mCommitSuccessful;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.database.sqlite.SQLiteSession.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.database.sqlite.SQLiteSession.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.database.sqlite.SQLiteSession.<clinit>():void");
    }

    public SQLiteSession(SQLiteConnectionPool connectionPool) {
        this.mCommitSuccessful = -assertionsDisabled;
        if (connectionPool == null) {
            throw new IllegalArgumentException("connectionPool must not be null");
        }
        this.mConnectionPool = connectionPool;
        this.mTransactionMap = new HashMap();
    }

    public boolean hasTransaction() {
        return this.mTransactionStack != null ? true : -assertionsDisabled;
    }

    public boolean hasNestedTransaction() {
        return (this.mTransactionStack == null || this.mTransactionStack.mParent == null) ? -assertionsDisabled : true;
    }

    public boolean hasConnection() {
        return this.mConnection != null ? true : -assertionsDisabled;
    }

    public void beginTransaction(int transactionMode, SQLiteTransactionListener transactionListener, int connectionFlags, CancellationSignal cancellationSignal) {
        throwIfTransactionMarkedSuccessful();
        beginTransactionUnchecked(transactionMode, transactionListener, connectionFlags, cancellationSignal);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    case UPDATE_STATUS /*1*/:
                        this.mConnection.execute("BEGIN IMMEDIATE;", null, cancellationSignal);
                    case TRANSACTION_MODE_EXCLUSIVE /*2*/:
                        this.mConnection.execute("BEGIN EXCLUSIVE;", null, cancellationSignal);
                    default:
                        this.mConnection.execute("BEGIN;", null, cancellationSignal);
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
        }
    }

    public void setTransactionSuccessful() {
        throwIfNoTransaction();
        throwIfTransactionMarkedSuccessful();
        this.mTransactionStack.mMarkedSuccessful = true;
    }

    public void endTransaction(CancellationSignal cancellationSignal) {
        throwIfNoTransaction();
        if (!-assertionsDisabled) {
            if (!(this.mConnection != null ? true : -assertionsDisabled)) {
                throw new AssertionError();
            }
        }
        endTransactionUnchecked(cancellationSignal, -assertionsDisabled);
    }

    private void endTransactionUnchecked(CancellationSignal cancellationSignal, boolean yielding) {
        if (cancellationSignal != null) {
            cancellationSignal.throwIfCanceled();
        }
        Transaction top = this.mTransactionStack;
        boolean successful = ((top.mMarkedSuccessful || yielding) && !top.mChildFailed) ? true : -assertionsDisabled;
        RuntimeException listenerException = null;
        SQLiteTransactionListener listener = top.mListener;
        if (listener != null) {
            if (successful) {
                try {
                    listener.onCommit();
                } catch (RuntimeException ex) {
                    listenerException = ex;
                    successful = -assertionsDisabled;
                }
            } else {
                listener.onRollback();
            }
        }
        this.mTransactionStack = top.mParent;
        recycleTransaction(top);
        if (this.mTransactionStack == null) {
            if (successful) {
                try {
                    this.mConnection.execute("COMMIT;", null, cancellationSignal);
                    this.mCommitSuccessful = true;
                } catch (Throwable th) {
                    releaseConnection();
                }
            } else {
                this.mConnection.execute("ROLLBACK;", null, cancellationSignal);
                this.mCommitSuccessful = -assertionsDisabled;
            }
            releaseConnection();
        } else if (!successful) {
            this.mTransactionStack.mChildFailed = true;
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
            return -assertionsDisabled;
        }
        if (!-assertionsDisabled) {
            boolean z;
            if (this.mConnection != null) {
                z = true;
            } else {
                z = -assertionsDisabled;
            }
            if (!z) {
                throw new AssertionError();
            }
        }
        if (this.mTransactionStack.mChildFailed) {
            return -assertionsDisabled;
        }
        return yieldTransactionUnchecked(sleepAfterYieldDelayMillis, cancellationSignal);
    }

    private boolean yieldTransactionUnchecked(long sleepAfterYieldDelayMillis, CancellationSignal cancellationSignal) {
        if (cancellationSignal != null) {
            cancellationSignal.throwIfCanceled();
        }
        if (!this.mConnectionPool.shouldYieldConnection(this.mConnection, this.mConnectionFlags)) {
            return -assertionsDisabled;
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
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        }
        if (cancellationSignal != null) {
            cancellationSignal.throwIfCanceled();
        }
        acquireConnection(sql, connectionFlags, cancellationSignal);
        try {
            this.mConnection.prepare(sql, outStatementInfo);
        } finally {
            releaseConnection();
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
                long executeForLong = this.mConnection.executeForLong(sql, bindArgs, cancellationSignal);
                return executeForLong;
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
                String executeForString = this.mConnection.executeForString(sql, bindArgs, cancellationSignal);
                return executeForString;
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
                ParcelFileDescriptor executeForBlobFileDescriptor = this.mConnection.executeForBlobFileDescriptor(sql, bindArgs, cancellationSignal);
                return executeForBlobFileDescriptor;
            } finally {
                releaseConnection();
            }
        }
    }

    public int executeForChangedRowCount(String sql, Object[] bindArgs, int connectionFlags, CancellationSignal cancellationSignal) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null.");
        } else if (executeSpecial(sql, bindArgs, connectionFlags, cancellationSignal)) {
            return TRANSACTION_MODE_DEFERRED;
        } else {
            acquireConnection(sql, connectionFlags, cancellationSignal);
            try {
                int executeForChangedRowCount = this.mConnection.executeForChangedRowCount(sql, bindArgs, cancellationSignal);
                return executeForChangedRowCount;
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
                long executeForLastInsertedRowId = this.mConnection.executeForLastInsertedRowId(sql, bindArgs, cancellationSignal);
                return executeForLastInsertedRowId;
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
            return TRANSACTION_MODE_DEFERRED;
        } else {
            acquireConnection(sql, connectionFlags, cancellationSignal);
            try {
                int executeForCursorWindow = this.mConnection.executeForCursorWindow(sql, bindArgs, window, startPos, requiredPos, countAllRows, cancellationSignal);
                return executeForCursorWindow;
            } finally {
                releaseConnection();
            }
        }
    }

    private boolean executeSpecial(String sql, Object[] bindArgs, int connectionFlags, CancellationSignal cancellationSignal) {
        if (cancellationSignal != null) {
            cancellationSignal.throwIfCanceled();
        }
        switch (DatabaseUtils.getSqlStatementType(sql)) {
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                beginTransaction(TRANSACTION_MODE_EXCLUSIVE, null, connectionFlags, cancellationSignal);
                return true;
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                setTransactionSuccessful();
                endTransaction(cancellationSignal);
                return true;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                endTransaction(cancellationSignal);
                return true;
            default:
                return -assertionsDisabled;
        }
    }

    private void acquireConnection(String sql, int connectionFlags, CancellationSignal cancellationSignal) {
        Object obj = null;
        if (this.mConnection == null) {
            if (!-assertionsDisabled) {
                if (this.mConnectionUseCount == 0) {
                    obj = UPDATE_STATUS;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            this.mConnection = this.mConnectionPool.acquireConnection(sql, connectionFlags, cancellationSignal);
            this.mConnectionFlags = connectionFlags;
        }
        this.mConnectionUseCount += UPDATE_STATUS;
    }

    private void releaseConnection() {
        Object obj = UPDATE_STATUS;
        if (!-assertionsDisabled) {
            if ((this.mConnection != null ? UPDATE_STATUS : TRANSACTION_MODE_DEFERRED) == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            if (this.mConnectionUseCount <= 0) {
                obj = TRANSACTION_MODE_DEFERRED;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
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
            transaction.mMarkedSuccessful = -assertionsDisabled;
            transaction.mChildFailed = -assertionsDisabled;
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
