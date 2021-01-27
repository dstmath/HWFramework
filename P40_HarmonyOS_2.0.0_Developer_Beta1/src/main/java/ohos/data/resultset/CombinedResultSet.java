package ohos.data.resultset;

import ohos.data.rdb.DataObserver;
import ohos.data.resultset.ResultSet;

public class CombinedResultSet extends AbsResultSet {
    private ResultSet currentResultSet;
    private ResultSet[] resultSets;

    public CombinedResultSet(ResultSet[] resultSetArr) {
        this.resultSets = resultSetArr;
        this.currentResultSet = resultSetArr[0];
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getRowCount() {
        ResultSet[] resultSetArr = this.resultSets;
        int i = 0;
        for (ResultSet resultSet : resultSetArr) {
            if (resultSet != null) {
                i += resultSet.getRowCount();
            }
        }
        return i;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public String[] getAllColumnNames() {
        ResultSet resultSet = this.currentResultSet;
        return resultSet == null ? new String[0] : resultSet.getAllColumnNames();
    }

    @Override // ohos.data.resultset.ResultSet
    public ResultSet.ColumnType getColumnTypeForIndex(int i) {
        return this.currentResultSet.getColumnTypeForIndex(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public byte[] getBlob(int i) {
        return this.currentResultSet.getBlob(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public String getString(int i) {
        return this.currentResultSet.getString(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public short getShort(int i) {
        return this.currentResultSet.getShort(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getInt(int i) {
        return this.currentResultSet.getInt(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public long getLong(int i) {
        return this.currentResultSet.getLong(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public float getFloat(int i) {
        return this.currentResultSet.getFloat(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public double getDouble(int i) {
        return this.currentResultSet.getDouble(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isColumnNull(int i) {
        return this.currentResultSet.isColumnNull(i);
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public void close() {
        ResultSet[] resultSetArr = this.resultSets;
        for (ResultSet resultSet : resultSetArr) {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        super.close();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean goToRow(int i) {
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
            boolean innerGo = innerGo(i);
            if (innerGo) {
                this.pos = i;
            } else {
                this.pos = -1;
            }
            return innerGo;
        }
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public void registerObserver(DataObserver dataObserver) {
        ResultSet[] resultSetArr = this.resultSets;
        for (ResultSet resultSet : resultSetArr) {
            if (resultSet != null) {
                resultSet.registerObserver(dataObserver);
            }
        }
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public void unregisterObserver(DataObserver dataObserver) {
        ResultSet[] resultSetArr = this.resultSets;
        for (ResultSet resultSet : resultSetArr) {
            if (resultSet != null) {
                resultSet.unregisterObserver(dataObserver);
            }
        }
    }

    private boolean innerGo(int i) {
        this.currentResultSet = null;
        ResultSet[] resultSetArr = this.resultSets;
        int length = resultSetArr.length;
        int i2 = 0;
        int i3 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            }
            ResultSet resultSet = resultSetArr[i2];
            if (resultSet != null) {
                if (i < resultSet.getRowCount() + i3) {
                    this.currentResultSet = resultSet;
                    break;
                }
                i3 += resultSet.getRowCount();
            }
            i2++;
        }
        ResultSet resultSet2 = this.currentResultSet;
        if (resultSet2 != null) {
            return resultSet2.goToRow(i - i3);
        }
        return false;
    }
}
