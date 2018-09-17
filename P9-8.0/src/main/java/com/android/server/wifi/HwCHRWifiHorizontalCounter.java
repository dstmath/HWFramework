package com.android.server.wifi;

public class HwCHRWifiHorizontalCounter extends HwCHRWifiCounterInfo {
    private String mSeporator = "\t";

    public HwCHRWifiHorizontalCounter(String colName) {
        super(colName);
    }

    public HwCHRWifiHorizontalCounter(String colName, String sep) {
        super(colName);
        this.mSeporator = sep;
    }

    public void parserValue(String valuesline, String cols) {
        int index = getIndexByColName(cols);
        if (-1 != index && valuesline != null) {
            String[] values = valuesline.split(this.mSeporator);
            if (values.length > index) {
                try {
                    this.mDelta = Long.parseLong(values[index]);
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    private int getIndexByColName(String colNames) {
        if (colNames == null) {
            return -1;
        }
        String[] cols = colNames.split(this.mSeporator);
        for (int i = 0; i < cols.length; i++) {
            if (this.mTag.equals(cols[i])) {
                return i;
            }
        }
        return -1;
    }
}
