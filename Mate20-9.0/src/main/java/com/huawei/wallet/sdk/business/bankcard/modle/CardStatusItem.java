package com.huawei.wallet.sdk.business.bankcard.modle;

public class CardStatusItem {
    public static final String QUERIED_CARD_ACTION_AVAIABLE = "0";
    public static final String QUERIED_CARD_ACTION_DELETED = "6";
    public static final String QUERIED_CARD_ACTION_ENROLL = "9";
    public static final String QUERIED_CARD_ACTION_INSTALL = "8";
    public static final String QUERIED_CARD_ACTION_LOST = "2";
    public static final String QUERIED_CARD_ACTION_NULLIFY = "5";
    public static final String QUERIED_CARD_ACTION_PAUSE = "1";
    public static final String QUERIED_CARD_ACTION_RESUME = "00";
    public static final String QUERIED_CARD_ACTION_UMPS_ENROLL = "11";
    public static final String QUERIED_CARD_ACTION_UNPERSON = "7";
    private String aid;
    private String appletVersion;
    private String balance;
    private String cardName;
    private String cardNum;
    private String cplc;
    private String createtime;
    private String dpanid;
    private String eidCode;
    private String issuerId;
    private String mLastModified;
    private String metaDataModTime;
    private String orderId;
    private String panEnrollmentId;
    private String productId;
    private String refId;
    private String reserved;
    private String source;
    private String status;
    private String terminal;
    private int tsp;
    private int type;
    private String userId;
    private String vProvisionedTokenid;

    public String getBalance() {
        return this.balance;
    }

    public void setBalance(String balance2) {
        this.balance = balance2;
    }

    public String getTerminal() {
        return this.terminal;
    }

    public void setTerminal(String terminal2) {
        this.terminal = terminal2;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId2) {
        this.userId = userId2;
    }

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public String getAid() {
        return this.aid;
    }

    public void setAid(String aid2) {
        this.aid = aid2;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status2) {
        this.status = status2;
    }

    public String getDpanid() {
        return this.dpanid;
    }

    public void setDpanid(String dpanid2) {
        this.dpanid = dpanid2;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source2) {
        this.source = source2;
    }

    public String getCardNum() {
        return this.cardNum;
    }

    public void setCardNum(String cardNum2) {
        this.cardNum = cardNum2;
    }

    public String getCardName() {
        return this.cardName;
    }

    public void setCardName(String cardName2) {
        this.cardName = cardName2;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public String getEidCode() {
        return this.eidCode;
    }

    public void setEidCode(String eidCode2) {
        this.eidCode = eidCode2;
    }

    public String getCreatetime() {
        return this.createtime;
    }

    public void setCreatetime(String createtime2) {
        this.createtime = createtime2;
    }

    public String getReserved() {
        return this.reserved;
    }

    public void setReserved(String reserved2) {
        this.reserved = reserved2;
    }

    public String getPanEnrollmentId() {
        return this.panEnrollmentId;
    }

    public void setPanEnrollmentId(String panEnrollmentId2) {
        this.panEnrollmentId = panEnrollmentId2;
    }

    public void setOrderId(String orderId2) {
        this.orderId = orderId2;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public String getMetaDataModTime() {
        return this.metaDataModTime;
    }

    public void setMetaDataModTime(String metaDataModTime2) {
        this.metaDataModTime = metaDataModTime2;
    }

    public String getvProvisionedTokenid() {
        return this.vProvisionedTokenid;
    }

    public void setvProvisionedTokenid(String vProvisionedTokenid2) {
        this.vProvisionedTokenid = vProvisionedTokenid2;
    }

    public int getTsp() {
        return this.tsp;
    }

    public void setTsp(int tsp2) {
        this.tsp = tsp2;
    }

    public String getProductId() {
        return this.productId;
    }

    public void setProductId(String productId2) {
        this.productId = productId2;
    }

    public String getRefId() {
        return this.refId;
    }

    public void setRefId(String refId2) {
        this.refId = refId2;
    }

    public String getAppletVersion() {
        return this.appletVersion;
    }

    public void setAppletVersion(String appletVersion2) {
        this.appletVersion = appletVersion2;
    }

    public String getmLastModified() {
        return this.mLastModified;
    }

    public void setmLastModified(String mLastModified2) {
        this.mLastModified = mLastModified2;
    }
}
