package com.huawei.wallet.sdk.common.apdu.request;

public class DeleteAppletRequest extends BaseRequest {
    public static final String ADD_CARD_CONFLICT = "3";
    public static final String ADD_CARD_FAIL_NO_UNFINISH_ORDER = "6";
    public static final String ADD_CARD_FAIL_REFUND = "8";
    public static final String ADD_CARD_PAY_FAIL_OR_CANCEL = "4";
    public static final String CHIP_NEED_CLEAN = "7";
    public static final String PRE_LOAD_CLEAN = "5";
    public static final String REASON_LOST_CARD = "";
    public static final String REASON_OPEN_CARD_FAIL = "";
    public static final String REASON_REPAIRE_CARD = "";
    public static final String SERVER_DELETE = "2";
    public static final String TA_NO_RECORD = "9";
    public static final String THIRD_DELETE = "13";
    public static final String USER_DELETE = "1";
    public static final String VIRTUAL_CARD_CANCEL = "11";
    public static final String VIRTUAL_CARD_RECEIVE_PUSH_CLEAN = "12";
    public static final String VIRTUAL_CARD_RETRY_DELETE_APPLET = "10";
    private String appCode;
    private String cardBalance;
    private String flag;
    private boolean onlyDeleteApplet;
    private String orderNo;
    private String partnerId;
    private String reason = null;
    private String refundAccountNumber;
    private String refundAccountType;
    private String refundTicketNum = null;
    private String source = null;
    private String type = null;

    public String getFlag() {
        return this.flag;
    }

    public void setFlag(String flag2) {
        this.flag = flag2;
    }

    public String getOrderNo() {
        return this.orderNo;
    }

    public void setOrderNo(String orderNo2) {
        this.orderNo = orderNo2;
    }

    public DeleteAppletRequest(String issuerId, String cplc, String appletAid, String deviceModel, String seChipManuFacturer) {
        setIssuerId(issuerId);
        setCplc(cplc);
        setAppletAid(appletAid);
        setDeviceModel(deviceModel);
        setSeChipManuFacturer(seChipManuFacturer);
    }

    public String getRefundTicketNum() {
        return this.refundTicketNum;
    }

    public void setRefundTicketNum(String refundTicket) {
        this.refundTicketNum = refundTicket;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason2) {
        this.reason = reason2;
    }

    public String getAppCode() {
        return this.appCode;
    }

    public void setAppCode(String appCode2) {
        this.appCode = appCode2;
    }

    public String getPartnerId() {
        return this.partnerId;
    }

    public void setPartnerId(String partnerId2) {
        this.partnerId = partnerId2;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source2) {
        this.source = source2;
    }

    public boolean isOnlyDeleteApplet() {
        return this.onlyDeleteApplet;
    }

    public void setOnlyDeleteApplet(boolean onlyDeleteApplet2) {
        this.onlyDeleteApplet = onlyDeleteApplet2;
    }

    public void setCardBalance(String cardBalance2) {
        this.cardBalance = cardBalance2;
    }

    public String getCardBalance() {
        return this.cardBalance;
    }

    public String getRefundAccountType() {
        return this.refundAccountType;
    }

    public void setRefundAccountType(String refundAccountType2) {
        this.refundAccountType = refundAccountType2;
    }

    public String getRefundAccountNumber() {
        return this.refundAccountNumber;
    }

    public void setRefundAccountNumber(String refundAccountNumber2) {
        this.refundAccountNumber = refundAccountNumber2;
    }
}
