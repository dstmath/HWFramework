package ohos.data.rdb.impl;

import java.io.File;
import ohos.data.resultset.AbsSharedResultSet;
import ohos.data.resultset.SharedBlock;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class SqliteSharedResultSet extends AbsSharedResultSet {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "SqliteBlockedResultSet");
    private static final int NO_COUNT = -1;
    private static final int PICK_POS = 3;
    private final String[] columns;
    private boolean isOnlyFillResulSetBlock;
    private final QueryStatement query;
    private int resultSetBlockCapacity;
    private int rowNum = -1;

    public SqliteSharedResultSet(QueryStatement queryStatement) {
        super(queryStatement.getRdbStore().getPath());
        this.query = queryStatement;
        this.columns = this.query.getColumnNames();
    }

    public RdbStoreImpl getRdbStore() {
        return this.query.getRdbStore();
    }

    @Override // ohos.data.resultset.AbsSharedResultSet, ohos.data.resultset.SharedResultSet
    public boolean onGo(int i, int i2) {
        if (this.sharedBlock == null) {
            fillBlock(i2);
            return true;
        }
        if (i2 < this.sharedBlock.getStartRowIndex() || i2 >= this.sharedBlock.getStartRowIndex() + this.sharedBlock.getRowCount()) {
            fillBlock(i2);
        }
        return true;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getRowCount() {
        if (this.rowNum == -1) {
            fillBlock(0);
        }
        return this.rowNum;
    }

    public int pickFillBlockStartPosition(int i, int i2) {
        return Math.max(i - (i2 / 3), 0);
    }

    private void fillBlock(int i) {
        int i2;
        clearBlock();
        try {
            if (this.rowNum == -1) {
                this.rowNum = this.query.fillBlock(this.sharedBlock, i, i, true);
                this.resultSetBlockCapacity = this.sharedBlock.getRowCount();
                HiLog.info(LABEL, "fillBlock received count(*) from native_fill_window: %{public}d", new Object[]{Integer.valueOf(this.rowNum)});
                return;
            }
            if (this.isOnlyFillResulSetBlock) {
                i2 = i;
            } else {
                i2 = pickFillBlockStartPosition(i, this.resultSetBlockCapacity);
            }
            this.query.fillBlock(this.sharedBlock, i2, i, false);
            HiLog.info(LABEL, "fillBlock startPos = %{public}d, requiredPos = %{public}d", new Object[]{Integer.valueOf(i2), Integer.valueOf(i)});
        } catch (RuntimeException e) {
            closeBlock();
            throw e;
        }
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public String[] getAllColumnNames() {
        return this.columns;
    }

    @Override // ohos.data.resultset.AbsSharedResultSet, ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public void close() {
        synchronized (this) {
            super.close();
            this.query.close();
        }
    }

    @Override // ohos.data.resultset.AbsSharedResultSet
    public void setBlock(SharedBlock sharedBlock) {
        super.setBlock(sharedBlock);
        this.rowNum = -1;
    }

    public void setFillWindowForwardOnly(boolean z) {
        this.isOnlyFillResulSetBlock = z;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.data.resultset.AbsSharedResultSet, ohos.data.resultset.AbsResultSet
    public void finalize() {
        try {
            if (!isClosed()) {
                RdbStoreImpl rdbStore = this.query.getRdbStore();
                HiLog.info(LABEL, "Finalizing a shared block that has not been closed, database name is %{public}s", new Object[]{rdbStore.isMemoryRdb() ? ":memory:" : new File(rdbStore.getPath()).getName()});
                close();
            }
        } finally {
            super.finalize();
        }
    }
}
