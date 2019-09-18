package com.huawei.wallet.sdk.business.buscard.base.model;

import com.huawei.wallet.sdk.business.buscard.base.util.MoneyUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.TimeUtil;
import java.util.ArrayList;
import java.util.List;

public class CardInfo {
    private int amount;
    private Integer c8FileStatus;
    private String cardNum;
    private String enableDate;
    private String expireDate;
    private Integer inOutStationStatus;
    private String logicCardNum;
    private int overdraftAmount;
    private List<TransactionRecord> records;
    private String rideMonth;
    private int rideTimes;

    public List<TransactionRecord> getRecords() {
        return new ArrayList(this.records);
    }

    public void setRecords(List<TransactionRecord> records2) {
        this.records = new ArrayList(records2);
    }

    public Integer getC8FileStatus() {
        return this.c8FileStatus;
    }

    public void setC8FileStatus(Integer c8FileStatus2) {
        this.c8FileStatus = c8FileStatus2;
    }

    public Integer getInOutStationStatus() {
        return this.inOutStationStatus;
    }

    public void setInOutStationStatus(Integer inOutStationStatus2) {
        this.inOutStationStatus = inOutStationStatus2;
    }

    public String getCardNum() {
        return this.cardNum;
    }

    public void setCardNum(String cardNum2) {
        this.cardNum = cardNum2;
    }

    public void setEnableDate(String enableDate2) {
        this.enableDate = enableDate2;
    }

    public String getEnableDate() {
        return this.enableDate;
    }

    public String getExpireDate() {
        return this.expireDate;
    }

    public void setExpireDate(String expireDate2) {
        this.expireDate = expireDate2;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount2) {
        this.amount = amount2;
    }

    public int getOverdraftAmount() {
        return this.overdraftAmount;
    }

    public void setOverdraftAmount(int overdraftAmount2) {
        this.overdraftAmount = overdraftAmount2;
    }

    public int getBalanceByFenUnit() {
        return this.amount - Math.abs(this.overdraftAmount);
    }

    public String getFormatedBalanceByYuanUnit() {
        return MoneyUtil.convertFenToYuan((long) getBalanceByFenUnit());
    }

    public String getFormatedExpireDate(String pattern) {
        if (StringUtil.isEmpty(this.expireDate, true)) {
            return null;
        }
        return TimeUtil.formatDate2String(TimeUtil.parseString2Date(this.expireDate, "yyyyMMdd"), pattern);
    }

    public void setRideMonth(String rideMonth2) {
        this.rideMonth = rideMonth2;
    }

    public String getRideMonth() {
        return this.rideMonth;
    }

    public String getLogicCardNum() {
        return this.logicCardNum;
    }

    public void setLogicCardNum(String logicCardNum2) {
        this.logicCardNum = logicCardNum2;
    }

    public void setRideTimes(int rideTimes2) {
        this.rideTimes = rideTimes2;
    }

    public int getRideTimes() {
        return this.rideTimes;
    }
}
