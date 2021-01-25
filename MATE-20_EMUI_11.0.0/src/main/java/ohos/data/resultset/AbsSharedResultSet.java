package ohos.data.resultset;

import ohos.data.resultset.ResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class AbsSharedResultSet extends AbsResultSet implements SharedResultSet {
    private static final int DEFAULT_POS = -1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "AbsSharedResultSet");
    protected SharedBlock sharedBlock;

    @Override // ohos.data.resultset.SharedResultSet
    public void fillBlock(int i, SharedBlock sharedBlock2) {
    }

    @Override // ohos.data.resultset.SharedResultSet
    public boolean onGo(int i, int i2) {
        return true;
    }

    public AbsSharedResultSet(String str) {
        this.sharedBlock = new SharedBlock(str);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public byte[] getBlob(int i) {
        checkState(i);
        return this.sharedBlock.getBlob(this.pos, i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public String getString(int i) {
        checkState(i);
        return this.sharedBlock.getString(this.pos, i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public short getShort(int i) {
        checkState(i);
        return this.sharedBlock.getShort(this.pos, i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getInt(int i) {
        checkState(i);
        return this.sharedBlock.getInt(this.pos, i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public long getLong(int i) {
        checkState(i);
        return this.sharedBlock.getLong(this.pos, i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public float getFloat(int i) {
        checkState(i);
        return this.sharedBlock.getFloat(this.pos, i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public double getDouble(int i) {
        checkState(i);
        return this.sharedBlock.getDouble(this.pos, i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isColumnNull(int i) {
        checkState(i);
        return this.sharedBlock.getType(this.pos, i).equals(ResultSet.ColumnType.TYPE_NULL);
    }

    @Override // ohos.data.resultset.ResultSet
    public ResultSet.ColumnType getColumnTypeForIndex(int i) {
        checkState(i);
        return this.sharedBlock.getType(this.pos, i);
    }

    /* access modifiers changed from: protected */
    public void checkState(int i) {
        if (this.sharedBlock == null) {
            throw new IllegalStateException("Attempting to access a null or closed SharedBlock.");
        } else if (i >= getColumnCount() || i < 0) {
            throw new IllegalStateException("checkState :column index is illegal.");
        } else if (this.pos < 0 || this.pos >= getRowCount()) {
            throw new ResultSetIndexOutOfRangeException("checkState :row index is illegal.");
        }
    }

    @Override // ohos.data.resultset.SharedResultSet
    public SharedBlock getBlock() {
        return this.sharedBlock;
    }

    public void setBlock(SharedBlock sharedBlock2) {
        if (this.sharedBlock != sharedBlock2) {
            closeBlock();
            this.sharedBlock = sharedBlock2;
        }
    }

    public boolean hasBlock() {
        return this.sharedBlock != null;
    }

    /* access modifiers changed from: protected */
    public void closeBlock() {
        SharedBlock sharedBlock2 = this.sharedBlock;
        if (sharedBlock2 != null) {
            sharedBlock2.close();
            this.sharedBlock = null;
        }
    }

    /* access modifiers changed from: protected */
    public void clearBlock() {
        SharedBlock sharedBlock2 = this.sharedBlock;
        if (sharedBlock2 != null) {
            sharedBlock2.clear();
        }
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public final boolean goToRow(int i) {
        int rowCount = getRowCount();
        if (i >= rowCount) {
            this.pos = rowCount;
            return false;
        } else if (i < 0) {
            this.pos = -1;
            return false;
        } else if (i == this.pos) {
            return true;
        } else {
            boolean onGo = onGo(this.pos, i);
            if (!onGo) {
                this.pos = -1;
            } else {
                this.pos = i;
            }
            return onGo;
        }
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public void close() {
        super.close();
        closeBlock();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.data.resultset.AbsResultSet
    public void finalize() {
        try {
            if (this.sharedBlock != null) {
                HiLog.info(LABEL, "Finalizing a shared block that has not been closed. shared block name = %{public}s", new Object[]{this.sharedBlock.getName()});
                close();
            }
        } finally {
            super.finalize();
        }
    }
}
