package android.database.sqlite;

import android.annotation.UnsupportedAppUsage;
import android.database.DatabaseUtils;
import android.os.CancellationSignal;
import java.util.Arrays;

public abstract class SQLiteProgram extends SQLiteClosable {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    @UnsupportedAppUsage
    private final Object[] mBindArgs;
    private final String[] mColumnNames;
    private final SQLiteDatabase mDatabase;
    private final int mNumParameters;
    private final boolean mReadOnly;
    @UnsupportedAppUsage
    private final String mSql;

    SQLiteProgram(SQLiteDatabase db, String sql, Object[] bindArgs, CancellationSignal cancellationSignalForPrepare) {
        this.mDatabase = db;
        if (sql != null) {
            this.mSql = sql.trim();
        } else {
            this.mSql = null;
        }
        int n = DatabaseUtils.getSqlStatementType(this.mSql);
        if (n == 4 || n == 5 || n == 6) {
            this.mReadOnly = false;
            this.mColumnNames = EMPTY_STRING_ARRAY;
            this.mNumParameters = 0;
        } else {
            boolean assumeReadOnly = n != 1 ? false : true;
            SQLiteStatementInfo info = new SQLiteStatementInfo();
            db.getThreadSession().prepare(this.mSql, db.getThreadDefaultConnectionFlags(assumeReadOnly), cancellationSignalForPrepare, info);
            this.mReadOnly = info.readOnly;
            this.mColumnNames = info.columnNames;
            this.mNumParameters = info.numParameters;
        }
        if (bindArgs == null || bindArgs.length <= this.mNumParameters) {
            int i = this.mNumParameters;
            if (i != 0) {
                this.mBindArgs = new Object[i];
                if (bindArgs != null) {
                    System.arraycopy(bindArgs, 0, this.mBindArgs, 0, bindArgs.length);
                    return;
                }
                return;
            }
            this.mBindArgs = null;
            return;
        }
        throw new IllegalArgumentException("Too many bind arguments.  " + bindArgs.length + " arguments were provided but the statement needs " + this.mNumParameters + " arguments.");
    }

    /* access modifiers changed from: package-private */
    public final SQLiteDatabase getDatabase() {
        return this.mDatabase;
    }

    /* access modifiers changed from: package-private */
    public final String getSql() {
        return this.mSql;
    }

    /* access modifiers changed from: package-private */
    public final Object[] getBindArgs() {
        return this.mBindArgs;
    }

    /* access modifiers changed from: package-private */
    public final String[] getColumnNames() {
        return this.mColumnNames;
    }

    /* access modifiers changed from: protected */
    public final SQLiteSession getSession() {
        return this.mDatabase.getThreadSession();
    }

    /* access modifiers changed from: protected */
    public final int getConnectionFlags() {
        return this.mDatabase.getThreadDefaultConnectionFlags(this.mReadOnly);
    }

    /* access modifiers changed from: protected */
    public final void onCorruption() {
        this.mDatabase.onCorruption();
    }

    @Deprecated
    public final int getUniqueId() {
        return -1;
    }

    public void bindNull(int index) {
        bind(index, null);
    }

    public void bindLong(int index, long value) {
        bind(index, Long.valueOf(value));
    }

    public void bindDouble(int index, double value) {
        bind(index, Double.valueOf(value));
    }

    public void bindString(int index, String value) {
        if (value != null) {
            bind(index, value);
            return;
        }
        throw new IllegalArgumentException("the bind value at index " + index + " is null");
    }

    public void bindBlob(int index, byte[] value) {
        if (value != null) {
            bind(index, value);
            return;
        }
        throw new IllegalArgumentException("the bind value at index " + index + " is null");
    }

    public void clearBindings() {
        Object[] objArr = this.mBindArgs;
        if (objArr != null) {
            Arrays.fill(objArr, (Object) null);
        }
    }

    public void bindAllArgsAsStrings(String[] bindArgs) {
        if (bindArgs != null) {
            for (int i = bindArgs.length; i != 0; i--) {
                bindString(i, bindArgs[i - 1]);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.database.sqlite.SQLiteClosable
    public void onAllReferencesReleased() {
        clearBindings();
    }

    private void bind(int index, Object value) {
        if (index < 1 || index > this.mNumParameters) {
            throw new IllegalArgumentException("Cannot bind argument at index " + index + " because the index is out of range.  The statement has " + this.mNumParameters + " parameters.");
        }
        this.mBindArgs[index - 1] = value;
    }
}
