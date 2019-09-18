package com.huawei.wallet.sdk.business.buscard.base.model;

import com.huawei.wallet.sdk.business.buscard.base.util.MoneyUtil;

public class TransactionRecord {
    public static final String RECORD_TYPE_CONSUME = "2";
    public static final String RECORD_TYPE_RECHARGE = "1";
    private int recordAmount;
    private String recordTime = "";
    private String recordType = "";

    public String getRecordTime() {
        return this.recordTime;
    }

    public void setRecordTime(String recordTime2) {
        this.recordTime = recordTime2;
    }

    public void setRecordAmount(int recordAmount2) {
        this.recordAmount = recordAmount2;
    }

    public int getRecordAmount() {
        return this.recordAmount;
    }

    public String getRecordType() {
        return this.recordType;
    }

    public void setRecordType(String recordType2) {
        this.recordType = recordType2;
    }

    public String getAmountByYuanUint() {
        return MoneyUtil.convertFenToYuan((long) this.recordAmount);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof TransactionRecord)) {
            return false;
        }
        TransactionRecord tr = (TransactionRecord) obj;
        if (this.recordAmount == tr.getRecordAmount() && this.recordTime.equals(tr.getRecordTime()) && this.recordType.equals(tr.getRecordType())) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return super.hashCode();
    }
}
