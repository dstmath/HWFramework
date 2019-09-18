package com.huawei.wallet.sdk.common.ta;

import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class TACardInfo implements Cloneable {
    private static final String TA_JSON_KEY_AID = "aid";
    private static final String TA_JSON_KEY_AID_2 = "aid2";
    private static final String TA_JSON_KEY_BUSCITYCD = "bus_city_cd";
    private static final String TA_JSON_KEY_CARDCLASSFYTYPE = "card_type";
    private static final String TA_JSON_KEY_CARDGROUPTYPE = "card_group_type";
    private static final String TA_JSON_KEY_CARDSTATUS = "card_status";
    private static final String TA_JSON_KEY_DPANDIGEST = "dpan_digest";
    private static final String TA_JSON_KEY_DPANFOUR = "dpan_four";
    private static final String TA_JSON_KEY_FPANDIGEST = "fpan_digest";
    private static final String TA_JSON_KEY_FPANFOUR = "fpan_four";
    private static final String TA_JSON_KEY_ISDEFAULTCARD = "is_default_card";
    private static final String TA_JSON_KEY_ISNEEDEID = "isNeedEID";
    private static final String TA_JSON_KEY_ISNEWAPPLET = "card_aplt_v";
    private static final String TA_JSON_KEY_ISOPENUNIONPAYMENTCODE = "isopen_upcode";
    private static final String TA_JSON_KEY_ISSUERID = "issuerId";
    private static final String TA_JSON_KEY_OPENCARDORDERID = "open_card_order_id";
    private static final String TA_JSON_KEY_PASSTYPEID = "passTypeId";
    private static final String TA_JSON_KEY_PRODUCTID = "productId";
    private static final String TA_JSON_KEY_STATUSUPDATETIME = "status_update_time";
    private static final String TA_JSON_KEY_USERID = "userid";
    private String aid;
    private String aid2;
    private String balance;
    private String busCityCd;
    private int cardGroupType;
    private int cardStatus;
    private int cardType;
    private String dpanDigest;
    private String dpanFour;
    private String fpanDigest;
    private String fpanFour;
    private boolean isActivated;
    private boolean isDefaultCard;
    private String isNeedEID;
    private boolean isNewApplet;
    private boolean isOpenUpCode;
    private String issuerId;
    private String openCardOrderId;
    private String passTypeId;
    private String productId;
    private String provisionedTokenID;
    private long statusUpdateTime;
    private String terminal;
    private String termsAndConditionsID;
    private String userid;

    public String getTaBalance() {
        return this.balance;
    }

    public void setTaBalance(String balance2) {
        this.balance = balance2;
    }

    public String getTaTerminal() {
        return this.terminal;
    }

    public void setTaTerminal(String terminal2) {
        this.terminal = terminal2;
    }

    public TACardInfo() {
    }

    public TACardInfo(String aid3, String productId2, String issuerId2, int cardGroupType2, boolean isDefaultCard2, String fpanDigest2, String fpanFour2, String dpanDigest2, String dpanFour2, int cardStatus2, long statusUpdateTime2, int cardType2) {
        setInfo(aid3, null, productId2, issuerId2, cardGroupType2, isDefaultCard2, fpanDigest2, fpanFour2, dpanDigest2, dpanFour2, cardStatus2, statusUpdateTime2, cardType2);
    }

    public TACardInfo(String aid3, String productId2, String issuerId2, int cardGroupType2, boolean isDefaultCard2, String fpanDigest2, String fpanFour2, String dpanDigest2, String dpanFour2, int cardStatus2, long statusUpdateTime2, int cardType2, String userid2) {
        setInfo(aid3, null, productId2, issuerId2, cardGroupType2, isDefaultCard2, fpanDigest2, fpanFour2, dpanDigest2, dpanFour2, cardStatus2, statusUpdateTime2, cardType2);
        this.userid = userid2;
    }

    public TACardInfo(String aid3, String productId2, String issuerId2, int cardGroupType2, boolean isDefaultCard2, String fpanDigest2, String fpanFour2, String dpanDigest2, String dpanFour2, int cardStatus2, long statusUpdateTime2, int cardType2, String userid2, String passTypeId2, String isNeedEID2) {
        setInfo(aid3, null, productId2, issuerId2, cardGroupType2, isDefaultCard2, fpanDigest2, fpanFour2, dpanDigest2, dpanFour2, cardStatus2, statusUpdateTime2, cardType2);
        this.userid = userid2;
        this.passTypeId = passTypeId2;
        this.isNeedEID = isNeedEID2;
    }

    public TACardInfo(String aid3, String aid22, String productId2, String issuerId2, int cardGroupType2, boolean isDefaultCard2, String fpanDigest2, String fpanFour2, String dpanDigest2, String dpanFour2, int cardStatus2, long statusUpdateTime2, int cardType2) {
        setInfo(aid3, aid22, productId2, issuerId2, cardGroupType2, isDefaultCard2, fpanDigest2, fpanFour2, dpanDigest2, dpanFour2, cardStatus2, statusUpdateTime2, cardType2);
    }

    public TACardInfo(String aid3, String aid22, String productId2, String issuerId2, int cardGroupType2, boolean isDefaultCard2, String fpanDigest2, String fpanFour2, String dpanDigest2, String dpanFour2, int cardStatus2, long statusUpdateTime2, int cardType2, String userid2) {
        setInfo(aid3, aid22, productId2, issuerId2, cardGroupType2, isDefaultCard2, fpanDigest2, fpanFour2, dpanDigest2, dpanFour2, cardStatus2, statusUpdateTime2, cardType2);
        this.userid = userid2;
    }

    private void setInfo(String aid3, String aid22, String productId2, String issuerId2, int cardGroupType2, boolean isDefaultCard2, String fpanDigest2, String fpanFour2, String dpanDigest2, String dpanFour2, int cardStatus2, long statusUpdateTime2, int cardType2) {
        this.aid = aid3;
        this.aid2 = aid22;
        this.productId = productId2;
        this.issuerId = issuerId2;
        this.cardGroupType = cardGroupType2;
        this.isDefaultCard = isDefaultCard2;
        this.fpanDigest = fpanDigest2;
        this.fpanFour = fpanFour2;
        this.dpanDigest = dpanDigest2;
        this.dpanFour = dpanFour2;
        this.cardStatus = cardStatus2;
        this.statusUpdateTime = statusUpdateTime2;
        this.cardType = cardType2;
        this.openCardOrderId = null;
        this.busCityCd = null;
    }

    TACardInfo(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            splitTACardInfoJsonMode1(json);
            splitTACardInfoJsonMode2(json);
        } catch (JSONException ex) {
            LogX.e("create ta card info failed: " + ex.getMessage(), true);
        }
    }

    private void splitTACardInfoJsonMode1(JSONObject json) {
        try {
            if (json.has("aid")) {
                this.aid = json.getString("aid");
            }
            if (json.has(TA_JSON_KEY_AID_2)) {
                this.aid2 = json.getString(TA_JSON_KEY_AID_2);
            }
            if (json.has(TA_JSON_KEY_PRODUCTID)) {
                this.productId = json.getString(TA_JSON_KEY_PRODUCTID);
            }
            if (json.has("issuerId")) {
                this.issuerId = json.getString("issuerId");
            }
            if (json.has(TA_JSON_KEY_CARDGROUPTYPE)) {
                this.cardGroupType = json.getInt(TA_JSON_KEY_CARDGROUPTYPE);
            }
            if (json.has(TA_JSON_KEY_ISDEFAULTCARD)) {
                this.isDefaultCard = json.getBoolean(TA_JSON_KEY_ISDEFAULTCARD);
            }
            if (json.has(TA_JSON_KEY_FPANDIGEST)) {
                this.fpanDigest = json.getString(TA_JSON_KEY_FPANDIGEST);
            }
            if (json.has(TA_JSON_KEY_FPANFOUR)) {
                this.fpanFour = json.getString(TA_JSON_KEY_FPANFOUR);
            }
            if (json.has(TA_JSON_KEY_DPANDIGEST)) {
                this.dpanDigest = json.getString(TA_JSON_KEY_DPANDIGEST);
            }
            if (json.has(TA_JSON_KEY_DPANFOUR)) {
                this.dpanFour = json.getString(TA_JSON_KEY_DPANFOUR);
            }
            if (json.has(TA_JSON_KEY_CARDSTATUS)) {
                this.cardStatus = json.getInt(TA_JSON_KEY_CARDSTATUS);
            }
        } catch (JSONException e) {
            LogX.e("create ta card info failed: " + e.getMessage(), true);
        }
    }

    private void splitTACardInfoJsonMode2(JSONObject json) {
        try {
            if (json.has(TA_JSON_KEY_STATUSUPDATETIME)) {
                this.statusUpdateTime = json.getLong(TA_JSON_KEY_STATUSUPDATETIME);
            }
            if (json.has(TA_JSON_KEY_CARDCLASSFYTYPE)) {
                this.cardType = json.getInt(TA_JSON_KEY_CARDCLASSFYTYPE);
            }
            if (json.has(TA_JSON_KEY_ISNEWAPPLET)) {
                this.isNewApplet = json.getBoolean(TA_JSON_KEY_ISNEWAPPLET);
            }
            if (json.has(TA_JSON_KEY_OPENCARDORDERID)) {
                this.openCardOrderId = json.getString(TA_JSON_KEY_OPENCARDORDERID);
            }
            if (json.has(TA_JSON_KEY_BUSCITYCD)) {
                this.busCityCd = json.getString(TA_JSON_KEY_BUSCITYCD);
            }
            if (json.has(TA_JSON_KEY_USERID)) {
                this.userid = json.getString(TA_JSON_KEY_USERID);
            }
            if (json.has(TA_JSON_KEY_PASSTYPEID)) {
                this.passTypeId = json.getString(TA_JSON_KEY_PASSTYPEID);
            }
            if (json.has(TA_JSON_KEY_ISNEEDEID)) {
                this.isNeedEID = json.getString(TA_JSON_KEY_ISNEEDEID);
            }
            if (json.has(TA_JSON_KEY_ISOPENUNIONPAYMENTCODE)) {
                this.isOpenUpCode = json.getBoolean(TA_JSON_KEY_ISOPENUNIONPAYMENTCODE);
            }
        } catch (JSONException e) {
            LogX.e("create ta card info failed: " + e.getMessage(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public String getTaCardInfoJsonStr() {
        JSONObject json = new JSONObject();
        try {
            json.put("aid", this.aid);
            json.put(TA_JSON_KEY_AID_2, this.aid2);
            json.put(TA_JSON_KEY_PRODUCTID, this.productId);
            json.put("issuerId", this.issuerId);
            json.put(TA_JSON_KEY_CARDGROUPTYPE, this.cardGroupType);
            json.put(TA_JSON_KEY_ISDEFAULTCARD, this.isDefaultCard);
            json.put(TA_JSON_KEY_FPANDIGEST, this.fpanDigest);
            json.put(TA_JSON_KEY_FPANFOUR, this.fpanFour);
            json.put(TA_JSON_KEY_DPANDIGEST, this.dpanDigest);
            json.put(TA_JSON_KEY_DPANFOUR, this.dpanFour);
            json.put(TA_JSON_KEY_CARDSTATUS, this.cardStatus);
            json.put(TA_JSON_KEY_STATUSUPDATETIME, this.statusUpdateTime);
            json.put(TA_JSON_KEY_CARDCLASSFYTYPE, this.cardType);
            json.put(TA_JSON_KEY_OPENCARDORDERID, this.openCardOrderId);
            json.put(TA_JSON_KEY_BUSCITYCD, this.busCityCd);
            json.put(TA_JSON_KEY_ISNEWAPPLET, this.isNewApplet);
            json.put(TA_JSON_KEY_USERID, this.userid);
            json.put(TA_JSON_KEY_PASSTYPEID, this.passTypeId);
            json.put(TA_JSON_KEY_ISNEEDEID, this.isNeedEID);
            json.put(TA_JSON_KEY_ISOPENUNIONPAYMENTCODE, this.isOpenUpCode);
        } catch (JSONException e) {
            LogX.e("getTaCardInfoJsonStr, json exception: " + e.getMessage(), true);
            json = null;
        }
        if (json == null) {
            return null;
        }
        return json.toString();
    }

    /* access modifiers changed from: protected */
    public TACardInfo clone() {
        try {
            return (TACardInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("cardType=");
        sb.append(this.cardGroupType);
        sb.append("\n");
        sb.append("isDefaultCard=");
        sb.append(this.isDefaultCard);
        sb.append("\n");
        sb.append("imeiDigest=");
        sb.append(this.fpanDigest);
        sb.append("\n");
        sb.append("imeiFour=");
        sb.append(this.fpanFour);
        sb.append("\n");
        sb.append("numberDigest=");
        sb.append(this.dpanDigest);
        sb.append("\n");
        sb.append("numberFour=");
        sb.append(this.dpanFour);
        sb.append("\n");
        sb.append("aid=");
        sb.append(this.aid);
        sb.append("\n");
        sb.append("aid2=");
        sb.append(this.aid2);
        sb.append("\n");
        sb.append("productId=");
        sb.append(this.productId);
        sb.append("\n");
        sb.append("issuerId=");
        sb.append(this.issuerId);
        sb.append("\n");
        sb.append("cardStatus=");
        sb.append(this.cardStatus);
        sb.append("\n");
        sb.append("statusUpdateTime=");
        sb.append(this.statusUpdateTime);
        sb.append("\n");
        sb.append("cardClassfyType=");
        sb.append(this.cardType);
        sb.append("\n");
        sb.append("openCardOrderId=");
        sb.append(this.openCardOrderId);
        sb.append("\n");
        sb.append("busCityCd=");
        sb.append(this.busCityCd);
        sb.append("\n");
        sb.append("isNewApplet=");
        sb.append(this.isNewApplet);
        sb.append("\n");
        sb.append("isOpenUpCode=");
        sb.append(this.isOpenUpCode);
        sb.append("\n");
        return sb.toString();
    }

    public String getAid() {
        return this.aid;
    }

    public String getAid2() {
        return this.aid2;
    }

    public String getProductId() {
        return this.productId;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public int getCardGroupType() {
        return this.cardGroupType;
    }

    public boolean isDefaultCard() {
        return this.isDefaultCard;
    }

    public boolean isActivated() {
        return this.isActivated;
    }

    public String getFpanDigest() {
        return this.fpanDigest;
    }

    public String getFpanFour() {
        return this.fpanFour;
    }

    public String getDpanDigest() {
        return this.dpanDigest;
    }

    public String getDpanFour() {
        return this.dpanFour;
    }

    public int getCardStatus() {
        return this.cardStatus;
    }

    public long getStatusUpdateTime() {
        return this.statusUpdateTime;
    }

    public int getCardType() {
        return this.cardType;
    }

    public String getOpenCardOrderId() {
        return this.openCardOrderId;
    }

    public String getBusCityCd() {
        return this.busCityCd;
    }

    public void setAid(String aid3) {
        this.aid = aid3;
    }

    public void setAid2(String aid22) {
        this.aid2 = aid22;
    }

    public void setProductId(String productId2) {
        this.productId = productId2;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public void setCardGroupType(int cardGroupType2) {
        this.cardGroupType = cardGroupType2;
    }

    public void setDefaultCard(boolean isDefaultCard2) {
        this.isDefaultCard = isDefaultCard2;
    }

    public void setActivatedStatus(boolean isActivated2) {
        this.isActivated = isActivated2;
    }

    public void setFpanDigest(String fpanDigest2) {
        this.fpanDigest = fpanDigest2;
    }

    public void setFpanFour(String fpanFour2) {
        this.fpanFour = fpanFour2;
    }

    public void setDpanDigest(String dpanDigest2) {
        this.dpanDigest = dpanDigest2;
    }

    public void setDpanFour(String dpanFour2) {
        this.dpanFour = dpanFour2;
    }

    public void setCardStatus(int cardStatus2) {
        this.cardStatus = cardStatus2;
    }

    public String getProvisionedTokenID() {
        return this.provisionedTokenID;
    }

    public void setProvisionedTokenID(String provisionedTokenID2) {
        this.provisionedTokenID = provisionedTokenID2;
    }

    public String getTermsAndConditionsID() {
        return this.termsAndConditionsID;
    }

    public void setTermsAndConditionsID(String termsAndConditionsID2) {
        this.termsAndConditionsID = termsAndConditionsID2;
    }

    public void setStatusUpdateTime(long statusUpdateTime2) {
        this.statusUpdateTime = statusUpdateTime2;
    }

    public void setOpenCardOrderId(String openCardOrderId2) {
        this.openCardOrderId = openCardOrderId2;
    }

    public void setCardType(int cardType2) {
        this.cardType = cardType2;
    }

    public void setBusCityCd(String busCityCd2) {
        this.busCityCd = busCityCd2;
    }

    public boolean isCardStatusPayedNotOpened() {
        if (this.cardStatus == 11 || this.cardStatus == 12 || this.cardStatus == 14) {
            return true;
        }
        return false;
    }

    public boolean compareIssuseId(String otherIssuerId) {
        return this.issuerId.equals(otherIssuerId);
    }

    public boolean isNewApplet() {
        return this.isNewApplet;
    }

    public void setNewApplet(boolean newApplet) {
        this.isNewApplet = newApplet;
    }

    public int getNewStatus(int flag, int substatus) {
        int cardstatus = getCardStatus();
        LogX.i("getBankCardStatus begin from " + cardstatus + ",flag is :" + flag + ",subStatus is :" + substatus);
        char[] statusChars = String.valueOf(cardstatus).toCharArray();
        if (flag > 0 && flag < 5 && substatus >= 0 && substatus <= 3) {
            statusChars[flag] = String.valueOf(substatus).toCharArray()[0];
        }
        String newStatus = String.valueOf(statusChars);
        if (StringUtil.isNumeric(newStatus)) {
            return Integer.parseInt(newStatus);
        }
        LogX.e("getBankCardStatus newStauts is not number " + cardstatus + ",flag is :" + flag + ",subStatus is :" + substatus);
        return cardstatus;
    }

    public String getUserid() {
        return this.userid;
    }

    public void setUserId(String userid2) {
        this.userid = userid2;
    }

    public String getPassTypeId() {
        return this.passTypeId;
    }

    public void setPassTypeId(String passTypeId2) {
        this.passTypeId = passTypeId2;
    }

    public String getIsNeedEID() {
        return this.isNeedEID;
    }

    public void setIsNeedEID(String isNeedEID2) {
        this.isNeedEID = isNeedEID2;
    }

    public boolean isOpenUpCode() {
        return this.isOpenUpCode;
    }

    public void setOpenUpCode(boolean openUpCode) {
        this.isOpenUpCode = openUpCode;
    }
}
