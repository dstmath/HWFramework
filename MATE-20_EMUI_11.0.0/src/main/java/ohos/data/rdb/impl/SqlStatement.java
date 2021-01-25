package ohos.data.rdb.impl;

import java.util.Arrays;
import ohos.hiviewdfx.HiLogLabel;

public class SqlStatement extends CoreCloseable {
    private static final long FALSE = 0;
    static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "SqlStatement");
    private static final long TRUE = 1;
    private final Object[] bindArgs;
    private final String[] columnNames;
    private final boolean isReadOnly;
    private final int numParameters;
    private final String sql;
    private final RdbStoreImpl store;

    public SqlStatement(RdbStoreImpl rdbStoreImpl, String str, Object[] objArr) {
        this.store = rdbStoreImpl;
        if (str != null) {
            this.sql = str.trim();
        } else {
            this.sql = null;
        }
        int sqlStatementType = SqliteDatabaseUtils.getSqlStatementType(this.sql);
        if (sqlStatementType == 5 || sqlStatementType == 6 || sqlStatementType == 7) {
            this.isReadOnly = false;
            this.columnNames = new String[0];
            this.numParameters = 0;
        } else {
            boolean z = true;
            if (!(sqlStatementType == 1 || sqlStatementType == 9)) {
                z = false;
            }
            SqliteStatementInfo prepare = rdbStoreImpl.getThreadSession().prepare(this.sql, z);
            this.isReadOnly = prepare.isReadOnly();
            this.columnNames = prepare.getColumnNames();
            this.numParameters = prepare.getNumParameters();
        }
        if (objArr == null || objArr.length <= this.numParameters) {
            int i = this.numParameters;
            if (i != 0) {
                this.bindArgs = new Object[i];
                if (objArr != null) {
                    System.arraycopy(objArr, 0, this.bindArgs, 0, objArr.length);
                    return;
                }
                return;
            }
            this.bindArgs = null;
            return;
        }
        throw new IllegalArgumentException("Too many bind arguments. the statement needs " + this.numParameters + " arguments, but" + objArr.length + " arguments were provided.");
    }

    /* access modifiers changed from: package-private */
    public final String getSql() {
        return this.sql;
    }

    /* access modifiers changed from: package-private */
    public final Object[] getBindArgs() {
        return this.bindArgs;
    }

    public RdbStoreImpl getRdbStore() {
        return this.store;
    }

    public void setNull(int i) {
        bindValue(i, null);
    }

    public void setLong(int i, long j) {
        bindValue(i, Long.valueOf(j));
    }

    public void setDouble(int i, double d) {
        bindValue(i, Double.valueOf(d));
    }

    public void setString(int i, String str) {
        if (str != null) {
            bindValue(i, str);
            return;
        }
        throw new IllegalArgumentException("the bind value at index " + i + " is null");
    }

    public void setBlob(int i, byte[] bArr) {
        if (bArr != null) {
            bindValue(i, bArr);
            return;
        }
        throw new IllegalArgumentException("the bind value at index " + i + " is null");
    }

    public void clearValues() {
        Object[] objArr = this.bindArgs;
        if (objArr != null) {
            Arrays.fill(objArr, (Object) null);
        }
    }

    public void setStrings(String[] strArr) {
        if (strArr != null) {
            for (int length = strArr.length; length != 0; length--) {
                setString(length, strArr[length - 1]);
            }
        }
    }

    public void setObject(int i, Object obj) {
        if (obj == null) {
            setNull(i);
        } else if (obj instanceof Double) {
            setDouble(i, ((Double) obj).doubleValue());
        } else if (obj instanceof Float) {
            setDouble(i, ((Float) obj).doubleValue());
        } else {
            if (obj instanceof Number) {
                setLong(i, ((Number) obj).longValue());
            }
            if (obj instanceof Boolean) {
                if (((Boolean) obj).booleanValue()) {
                    setLong(i, TRUE);
                } else {
                    setLong(i, FALSE);
                }
            } else if (obj instanceof byte[]) {
                setBlob(i, (byte[]) obj);
            } else {
                setString(i, obj.toString());
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    /* access modifiers changed from: protected */
    public final StoreSession getSession() {
        return this.store.getThreadSession();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.data.rdb.impl.CoreCloseable
    public void onAllRefReleased() {
        clearValues();
    }

    private void bindValue(int i, Object obj) {
        if (i < 1 || i > this.numParameters) {
            throw new IllegalArgumentException("Cannot bind argument at index " + i + " because the index is out of range.  The statement has " + this.numParameters + " parameters.");
        }
        this.bindArgs[i - 1] = obj;
    }

    /* access modifiers changed from: package-private */
    public final String[] getColumnNames() {
        return this.columnNames;
    }
}
