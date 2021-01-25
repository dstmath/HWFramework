package ohos.data.resultset;

import java.lang.reflect.Array;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.data.rdb.impl.SqliteDatabaseUtils;
import ohos.data.resultset.ResultSet;

public class TableResultSet extends AbsResultSet {
    private static final int INITIAL_CAPACITY = 10;
    private final int columnCount;
    private final String[] columnNames;
    private Object[][] data;
    private int rowCount;

    public TableResultSet(String[] strArr, int i) {
        this.rowCount = 0;
        this.columnNames = strArr;
        this.columnCount = strArr.length;
        this.data = (Object[][]) Array.newInstance(Object.class, i >= 1 ? i : 1, this.columnCount);
    }

    public TableResultSet(String[] strArr) {
        this(strArr, 10);
    }

    public RowBuilder addRowByBuilder() {
        ensureCapacity();
        int i = this.rowCount;
        this.rowCount = i + 1;
        return new RowBuilder(i);
    }

    public void addRow(Object[] objArr) {
        if (objArr.length == this.columnCount) {
            ensureCapacity();
            System.arraycopy(objArr, 0, this.data[this.rowCount], 0, this.columnCount);
            this.rowCount++;
            return;
        }
        throw new IllegalArgumentException("Illegal column count.");
    }

    public void addRow(Iterable<?> iterable) {
        ensureCapacity();
        Object[] objArr = new Object[this.columnCount];
        int i = 0;
        for (Object obj : iterable) {
            if (i != this.columnCount) {
                objArr[i] = obj;
                i++;
            } else {
                throw new IllegalArgumentException("Illegal column count.");
            }
        }
        addRow(objArr);
    }

    private void ensureCapacity() {
        int i = this.rowCount;
        Object[][] objArr = this.data;
        if (i >= objArr.length) {
            this.data = (Object[][]) Array.newInstance(Object.class, objArr.length * 2, this.columnCount);
            for (int i2 = 0; i2 < objArr.length; i2++) {
                System.arraycopy(objArr[i2], 0, this.data[i2], 0, this.columnCount);
            }
        }
    }

    private Object getObject(int i) {
        if (i < 0 || i >= this.columnCount) {
            throw new ResultSetIndexOutOfRangeException("Column index: " + i + " out of column count: " + this.columnCount);
        } else if (this.pos < 0) {
            throw new ResultSetIndexOutOfRangeException("Before first row.");
        } else if (this.pos < this.rowCount) {
            return this.data[this.pos][i];
        } else {
            throw new ResultSetIndexOutOfRangeException("After last row.");
        }
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getRowCount() {
        return this.rowCount;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public String[] getAllColumnNames() {
        return this.columnNames;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getColumnCount() {
        return this.columnCount;
    }

    @Override // ohos.data.resultset.ResultSet
    public ResultSet.ColumnType getColumnTypeForIndex(int i) {
        return ResultSet.ColumnType.getByValue(SqliteDatabaseUtils.getObjectType(getObject(i)));
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public byte[] getBlob(int i) {
        if (getObject(i) instanceof byte[]) {
            return (byte[]) getObject(i);
        }
        return null;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public String getString(int i) {
        Object object = getObject(i);
        if (object == null) {
            return null;
        }
        return object.toString();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public short getShort(int i) {
        Object object = getObject(i);
        if (object == null) {
            return 0;
        }
        if (object instanceof Number) {
            return ((Number) object).shortValue();
        }
        return Short.parseShort(object.toString());
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getInt(int i) {
        Object object = getObject(i);
        if (object == null) {
            return 0;
        }
        if (object instanceof Number) {
            return ((Number) object).intValue();
        }
        return Integer.parseInt(object.toString());
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public long getLong(int i) {
        Object object = getObject(i);
        if (object == null) {
            return 0;
        }
        if (object instanceof Number) {
            return ((Number) object).longValue();
        }
        return Long.parseLong(object.toString());
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public float getFloat(int i) {
        Object object = getObject(i);
        if (object == null) {
            return 0.0f;
        }
        if (object instanceof Number) {
            return ((Number) object).floatValue();
        }
        return Float.parseFloat(object.toString());
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public double getDouble(int i) {
        Object object = getObject(i);
        if (object == null) {
            return XPath.MATCH_SCORE_QNAME;
        }
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        }
        return Double.parseDouble(object.toString());
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isColumnNull(int i) {
        return getObject(i) == null;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean goToRow(int i) {
        int rowCount2 = getRowCount();
        if (i >= rowCount2) {
            this.pos = rowCount2;
            return false;
        } else if (i < 0) {
            this.pos = -1;
            return false;
        } else if (i == this.pos) {
            return true;
        } else {
            this.pos = i;
            return true;
        }
    }

    public class RowBuilder {
        private int index = 0;
        private final int rowIndex;

        RowBuilder(int i) {
            this.rowIndex = i;
        }

        public RowBuilder setColumnValue(Object obj) {
            if (this.index != TableResultSet.this.columnCount) {
                Object[] objArr = TableResultSet.this.data[this.rowIndex];
                int i = this.index;
                this.index = i + 1;
                objArr[i] = obj;
                return this;
            }
            throw new ResultSetIndexOutOfRangeException("Columns count out of range");
        }

        public RowBuilder setColumnValue(String str, Object obj) {
            for (int i = 0; i < TableResultSet.this.columnNames.length; i++) {
                if (str.equals(TableResultSet.this.columnNames[i])) {
                    TableResultSet.this.data[this.rowIndex][i] = obj;
                }
            }
            return this;
        }
    }
}
