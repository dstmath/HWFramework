package com.android.internal.telephony;

public class HwHisiCsgNetworkInfo {
    public boolean isSelectedFail = false;
    private String mCSGId;
    private String mOper;
    private int mRat;

    public HwHisiCsgNetworkInfo(String oper, String CSGId, int rat) {
        this.mOper = oper;
        this.mCSGId = CSGId;
        this.mRat = rat;
    }

    public String getOper() {
        return this.mOper;
    }

    public void setOper(String oper) {
        this.mOper = oper;
    }

    public String getCSGId() {
        return this.mCSGId;
    }

    public void setCSGId(String CSGId) {
        this.mCSGId = CSGId;
    }

    public int getRat() {
        return this.mRat;
    }

    public void setRat(int rat) {
        this.mRat = rat;
    }

    public boolean isEmpty() {
        if (this.mRat != 0) {
            return false;
        }
        if (this.mOper == null || this.mOper.isEmpty()) {
            return this.mCSGId != null ? this.mCSGId.isEmpty() : true;
        } else {
            return false;
        }
    }

    public String toString() {
        return "HwHisiCsgNetworkInfo{mOper='" + this.mOper + '\'' + ", mCSGId='" + this.mCSGId + '\'' + ", mRat=" + this.mRat + ", isSelectedFail=" + this.isSelectedFail + '}';
    }
}
