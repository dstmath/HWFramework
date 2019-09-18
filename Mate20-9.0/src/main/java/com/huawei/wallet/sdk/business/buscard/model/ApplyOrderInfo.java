package com.huawei.wallet.sdk.business.buscard.model;

public class ApplyOrderInfo {
    private int actualCardMovePayment;
    private int actualIssuePayment;
    private int actualPayment;
    private int actualRechargePayment;
    private String cardNo;
    private int currencyCode;
    private String eventId;
    private boolean isBeijingAppMode;
    private String moveCode;
    private int orderType;
    private int payType;
    private String phoneNum;
    private String reserved;
    private int theoreticalCardMovePayment;
    private int theoreticalIssuePayment;
    private int theoreticalPayment;
    private int theoreticalRechargePayment;

    public ApplyOrderInfo() {
        this(0, 0, 0);
    }

    public ApplyOrderInfo(int orderType2, int actualPayment2, int theoreticalPayment2) {
        this.isBeijingAppMode = false;
        this.orderType = orderType2;
        this.actualPayment = actualPayment2;
        this.theoreticalPayment = theoreticalPayment2;
    }

    public ApplyOrderInfo(int orderType2, RechargeMoney rechargeMoney) {
        this.isBeijingAppMode = false;
        this.orderType = orderType2;
        if (rechargeMoney != null) {
            this.actualPayment = rechargeMoney.getPayMoney();
            this.theoreticalPayment = rechargeMoney.getRechargeMoney();
            return;
        }
        this.actualPayment = 0;
        this.theoreticalPayment = 0;
    }

    public void setIssuePayment(int actualIssuePayment2, int theoreticalIssuePayment2) {
        this.actualIssuePayment = actualIssuePayment2;
        this.theoreticalIssuePayment = theoreticalIssuePayment2;
    }

    public void setRechargePayment(int actualRechagePayment, int theoreticalRechargePayment2) {
        this.actualRechargePayment = actualRechagePayment;
        this.theoreticalRechargePayment = theoreticalRechargePayment2;
    }

    public void setRechargePayment(RechargeMoney rechargeMoney) {
        if (rechargeMoney != null) {
            this.actualRechargePayment = rechargeMoney.getPayMoney();
            this.theoreticalRechargePayment = rechargeMoney.getRechargeMoney();
        }
    }

    public String getPhoneNum() {
        return this.phoneNum;
    }

    public void setPhoneNum(String phoneNum2) {
        this.phoneNum = phoneNum2;
    }

    public String getReserved() {
        return this.reserved;
    }

    public void setReserved(String reserved2) {
        this.reserved = reserved2;
    }

    public String getMoveCode() {
        return this.moveCode;
    }

    public void setMoveCode(String moveCode2) {
        this.moveCode = moveCode2;
    }

    public int getPayType() {
        return this.payType;
    }

    public void setPayType(int payType2) {
        this.payType = payType2;
    }

    public int getCurrencyCode() {
        return this.currencyCode;
    }

    public void setCurrencyCode(int currencyCode2) {
        this.currencyCode = currencyCode2;
    }

    public int getActualIssuePayment() {
        return this.actualIssuePayment;
    }

    public void setActualIssuePayment(int mActualIssuePayment) {
        this.actualIssuePayment = mActualIssuePayment;
    }

    public int getTheoreticalIssuePayment() {
        return this.theoreticalIssuePayment;
    }

    public void setTheoreticalIssuePayment(int mTheoreticalIssuePayment) {
        this.theoreticalIssuePayment = mTheoreticalIssuePayment;
    }

    public int getTheoreticalPayment() {
        return this.theoreticalPayment;
    }

    public int getActualPayment() {
        return this.actualPayment;
    }

    public int getOrderType() {
        return this.orderType;
    }

    public void setBeijingAppMode(boolean beijingAppMode) {
        this.isBeijingAppMode = beijingAppMode;
    }

    public boolean isBeijingAppMode() {
        return this.isBeijingAppMode;
    }

    public int getTheoreticalRechargePayment() {
        return this.theoreticalRechargePayment;
    }

    public void setTheoreticalRechargePayment(int mTheoreticalRechargePayment) {
        this.theoreticalRechargePayment = mTheoreticalRechargePayment;
    }

    public int getActualRechargePayment() {
        return this.actualRechargePayment;
    }

    public void setActualRechargePayment(int mActualRechargePayment) {
        this.actualRechargePayment = mActualRechargePayment;
    }

    public void setOrderType(int orderType2) {
        this.orderType = orderType2;
    }

    public void setActualPayment(int actualPayment2) {
        this.actualPayment = actualPayment2;
    }

    public void setTheoreticalPayment(int theoreticalPayment2) {
        this.theoreticalPayment = theoreticalPayment2;
    }

    public int getTheoreticalCardMovePayment() {
        return this.theoreticalCardMovePayment;
    }

    public void setTheoreticalCardMovePayment(int theoreticalCardMovePayment2) {
        this.theoreticalCardMovePayment = theoreticalCardMovePayment2;
    }

    public int getActualCardMovePayment() {
        return this.actualCardMovePayment;
    }

    public void setActualCardMovePayment(int actualCardMovePayment2) {
        this.actualCardMovePayment = actualCardMovePayment2;
    }

    public String getEventId() {
        return this.eventId;
    }

    public void setEventId(String eventId2) {
        this.eventId = eventId2;
    }

    public String getCardNo() {
        return this.cardNo;
    }

    public void setCardNo(String cardNo2) {
        this.cardNo = cardNo2;
    }
}
