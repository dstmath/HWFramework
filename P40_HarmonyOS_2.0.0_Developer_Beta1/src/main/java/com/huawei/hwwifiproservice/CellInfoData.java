package com.huawei.hwwifiproservice;

public class CellInfoData {
    String mCellId;
    int mRssi;

    public CellInfoData(String cellid, int rssi) {
        this.mCellId = cellid;
        this.mRssi = rssi;
    }

    public String getCellid() {
        return this.mCellId;
    }

    public int getRssi() {
        return this.mRssi;
    }
}
