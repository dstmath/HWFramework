package ohos.data.rdb.impl;

import ohos.data.resultset.ResultSet;
import ohos.data.resultset.ResultSetWrapper;
import ohos.data.resultset.SharedBlock;
import ohos.data.resultset.SharedResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class SharedResultSetWrapper extends ResultSetWrapper implements SharedResultSet {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "SharedResultSetWrapper");
    private SharedBlock mSharedBlock;

    public SharedResultSetWrapper(ResultSet resultSet) {
        super(resultSet);
        if (resultSet instanceof StepResultSet) {
            HiLog.info(LABEL, "SharedResultSetWrapper: inputResultSet can't be StepResultSet, please use query to obtain a SharedResultSet.", new Object[0]);
            throw new IllegalArgumentException("inputResultSet can't be StepResultSet, please use query to obtain a SharedResultSet.");
        }
    }

    @Override // ohos.data.resultset.SharedResultSet
    public SharedBlock getBlock() {
        if (this.mResultSet instanceof SharedResultSet) {
            return ((SharedResultSet) this.mResultSet).getBlock();
        }
        HiLog.info(LABEL, "SharedResultSetWrapper: doesn't exist SharedBlock", new Object[0]);
        return this.mSharedBlock;
    }

    @Override // ohos.data.resultset.SharedResultSet
    public void fillBlock(int i, SharedBlock sharedBlock) {
        if (this.mResultSet instanceof SharedResultSet) {
            ((SharedResultSet) this.mResultSet).fillBlock(i, sharedBlock);
        } else {
            resultSetFillBlock(this.mResultSet, i, sharedBlock);
        }
    }

    @Override // ohos.data.resultset.SharedResultSet
    public boolean onGo(int i, int i2) {
        if (this.mResultSet instanceof SharedResultSet) {
            return ((SharedResultSet) this.mResultSet).onGo(i, i2);
        }
        SharedBlock sharedBlock = this.mSharedBlock;
        if (sharedBlock != null && i2 >= sharedBlock.getStartRowIndex() && i2 < this.mSharedBlock.getStartRowIndex() + this.mSharedBlock.getRowCount()) {
            return true;
        }
        SharedBlock sharedBlock2 = new SharedBlock("");
        fillBlock(i2, sharedBlock2);
        this.mSharedBlock = sharedBlock2;
        return true;
    }

    public static void resultSetFillBlock(ResultSet resultSet, int i, SharedBlock sharedBlock) {
        if (i < 0 || i >= resultSet.getRowCount()) {
            HiLog.info(LABEL, "resultSetFillBlock: position is invalid.", new Object[0]);
            return;
        }
        int rowIndex = resultSet.getRowIndex();
        int columnCount = resultSet.getColumnCount();
        sharedBlock.clear();
        sharedBlock.setStartRowIndex(i);
        sharedBlock.setColumnCount(columnCount);
        if (resultSet.goToRow(i)) {
            resultSetFillBlockInner(i, sharedBlock, resultSet, columnCount);
        }
        resultSet.goToRow(rowIndex);
    }

    private static void resultSetFillBlockInner(int i, SharedBlock sharedBlock, ResultSet resultSet, int i2) {
        do {
            if (!sharedBlock.allocateRow()) {
                HiLog.info(LABEL, "resultSetFillBlockInner: block allocateRow failed.", new Object[0]);
                return;
            }
            for (int i3 = 0; i3 < i2; i3++) {
                if (!fillBlockUnit(i, sharedBlock, resultSet, i3)) {
                    sharedBlock.freeLastRow();
                    return;
                }
            }
            i++;
        } while (resultSet.goToNextRow());
    }

    private static boolean fillBlockUnit(int i, SharedBlock sharedBlock, ResultSet resultSet, int i2) {
        ResultSet.ColumnType columnTypeForIndex = resultSet.getColumnTypeForIndex(i2);
        if (columnTypeForIndex == null) {
            String string = resultSet.getString(i2);
            if (string != null) {
                return sharedBlock.putString(string, i, i2);
            }
            return sharedBlock.putNull(i, i2);
        }
        int i3 = AnonymousClass1.$SwitchMap$ohos$data$resultset$ResultSet$ColumnType[columnTypeForIndex.ordinal()];
        if (i3 == 1) {
            return sharedBlock.putLong(resultSet.getLong(i2), i, i2);
        }
        if (i3 == 2) {
            return sharedBlock.putDouble(resultSet.getDouble(i2), i, i2);
        }
        if (i3 == 3) {
            byte[] blob = resultSet.getBlob(i2);
            if (blob != null) {
                return sharedBlock.putBlob(blob, i, i2);
            }
            return sharedBlock.putNull(i, i2);
        } else if (i3 != 4) {
            return sharedBlock.putNull(i, i2);
        } else {
            String string2 = resultSet.getString(i2);
            if (string2 != null) {
                return sharedBlock.putString(string2, i, i2);
            }
            return sharedBlock.putNull(i, i2);
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.data.rdb.impl.SharedResultSetWrapper$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$data$resultset$ResultSet$ColumnType = new int[ResultSet.ColumnType.values().length];

        static {
            try {
                $SwitchMap$ohos$data$resultset$ResultSet$ColumnType[ResultSet.ColumnType.TYPE_INTEGER.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$data$resultset$ResultSet$ColumnType[ResultSet.ColumnType.TYPE_FLOAT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$data$resultset$ResultSet$ColumnType[ResultSet.ColumnType.TYPE_BLOB.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$data$resultset$ResultSet$ColumnType[ResultSet.ColumnType.TYPE_STRING.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    @Override // ohos.data.resultset.ResultSetWrapper, ohos.data.resultset.ResultSet
    public void close() {
        super.close();
        SharedBlock sharedBlock = this.mSharedBlock;
        if (sharedBlock != null) {
            sharedBlock.close();
            this.mSharedBlock = null;
        }
    }
}
