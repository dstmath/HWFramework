package ohos.data.rdb.impl;

import java.util.Arrays;
import ohos.data.rdb.RdbException;
import ohos.data.resultset.AbsResultSet;
import ohos.data.resultset.ResultSet;

public class StepResultSet extends AbsResultSet {
    private static final int INIT_POS = -1;
    private static final int SQLITE_DONE = 1;
    private final String[] mColumns;
    private boolean mIsEnded = false;
    private QueryStatement mQuery;
    private int mRowCount = -1;
    private StoreSession mSession = null;
    private PrecompiledStatement mStatement = null;

    private static native byte[] nativeGetBlob(long j, int i);

    private static native double nativeGetDouble(long j, int i);

    private static native long nativeGetLong(long j, int i);

    private static native String nativeGetString(long j, int i);

    private static native int nativeGetType(long j, int i);

    private static native void nativeInit();

    static {
        nativeInit();
    }

    public StepResultSet(QueryStatement queryStatement) {
        if (queryStatement != null) {
            this.mQuery = queryStatement;
            this.mColumns = queryStatement.getColumnNames();
            return;
        }
        throw new IllegalArgumentException("query object cannot be null");
    }

    private void checkState() {
        if (this.mStatement == null) {
            throw new IllegalStateException("StepResultSet query hasn't been executed!");
        }
    }

    private void checkSession() {
        StoreSession storeSession = this.mSession;
        if (storeSession != null && storeSession != this.mQuery.getSession()) {
            throw new IllegalStateException("StepResultSet is passed cross threads!");
        }
    }

    private void checkClosed() {
        if (this.mQuery == null) {
            throw new IllegalStateException("StepResultSet has been closed!");
        }
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public String[] getAllColumnNames() {
        String[] strArr = this.mColumns;
        return (String[]) Arrays.copyOf(strArr, strArr.length);
    }

    @Override // ohos.data.resultset.ResultSet
    public ResultSet.ColumnType getColumnTypeForIndex(int i) {
        checkState();
        return ResultSet.ColumnType.getByValue(nativeGetType(this.mStatement.getStatementPtr(), i));
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getRowCount() {
        int i = this.mRowCount;
        if (i >= 0) {
            return i;
        }
        int rowIndex = getRowIndex();
        do {
        } while (goToNextRow());
        goToRow(rowIndex);
        return this.mRowCount;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean goToRow(int i) {
        checkClosed();
        if (i < 0) {
            reset();
            return false;
        } else if (i == this.pos) {
            return !isEnded();
        } else {
            if (i < this.pos) {
                reset();
            }
            while (i != this.pos) {
                if (!goToNextRow()) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean goToNextRow() {
        checkClosed();
        if (this.mIsEnded) {
            return false;
        }
        prepareStep();
        try {
            this.pos++;
            if (this.mQuery.step() != 1) {
                return true;
            }
            this.mIsEnded = true;
            this.mRowCount = this.pos;
            finishStep();
            return false;
        } catch (RdbException e) {
            finishStep();
            throw e;
        }
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isStarted() {
        if (this.pos == -1) {
            return false;
        }
        if (this.pos != 0 || !this.mIsEnded) {
            return true;
        }
        return false;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isAtFirstRow() {
        if (this.pos != 0) {
            return false;
        }
        return !this.mIsEnded;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isEnded() {
        return this.mIsEnded;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public byte[] getBlob(int i) {
        checkState();
        return nativeGetBlob(this.mStatement.getStatementPtr(), i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public String getString(int i) {
        checkState();
        return nativeGetString(this.mStatement.getStatementPtr(), i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public short getShort(int i) {
        return (short) ((int) getLong(i));
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getInt(int i) {
        return (int) getLong(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public long getLong(int i) {
        checkState();
        return nativeGetLong(this.mStatement.getStatementPtr(), i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public float getFloat(int i) {
        return (float) getDouble(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public double getDouble(int i) {
        checkState();
        return nativeGetDouble(this.mStatement.getStatementPtr(), i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isColumnNull(int i) {
        return getColumnTypeForIndex(i) == ResultSet.ColumnType.TYPE_NULL;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isClosed() {
        return this.mQuery == null;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public void close() {
        if (this.mQuery != null) {
            finishStep();
            this.mQuery.close();
            this.mQuery = null;
            super.close();
        }
    }

    private void prepareStep() {
        if (this.mSession == null) {
            this.mSession = this.mQuery.getSession();
            this.mStatement = this.mQuery.beginStepQuery();
            return;
        }
        checkSession();
    }

    private void finishStep() {
        if (this.mSession != null) {
            this.mQuery.endStepQuery(this.mStatement);
            this.mSession = null;
            this.mStatement = null;
        }
    }

    private void reset() {
        if (this.mSession != null) {
            this.mQuery.resetStatement(this.mStatement);
        }
        this.pos = -1;
        this.mIsEnded = false;
    }
}
