package ohos.data.rdb.impl;

import java.util.Arrays;

public class SqliteStatementInfo {
    private String[] columnNames;
    private boolean isReadOnly;
    private int numParameters;

    public int getNumParameters() {
        return this.numParameters;
    }

    public void setNumParameters(int i) {
        this.numParameters = i;
    }

    public String[] getColumnNames() {
        String[] strArr = this.columnNames;
        if (strArr == null) {
            return new String[0];
        }
        return (String[]) Arrays.copyOf(strArr, strArr.length);
    }

    public void setColumnNames(String[] strArr) {
        if (strArr != null) {
            this.columnNames = (String[]) Arrays.copyOf(strArr, strArr.length);
        } else {
            this.columnNames = null;
        }
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    public void setReadOnly(boolean z) {
        this.isReadOnly = z;
    }
}
