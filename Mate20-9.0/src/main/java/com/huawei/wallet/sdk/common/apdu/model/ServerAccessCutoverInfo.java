package com.huawei.wallet.sdk.common.apdu.model;

import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessCutoverInfo {
    private String balance;
    private String cardName;
    private String cardNo;
    private String cityCode;
    private String fee;
    private String isAllow;
    private String orderNo;
    private String orderStatus;
    private String standardFee;
    private String times;

    public String getOrderNo() {
        return this.orderNo;
    }

    public void setOrderNo(String orderNo2) {
        this.orderNo = orderNo2;
    }

    public String getOrderStatus() {
        return this.orderStatus;
    }

    public void setOrderStatus(String orderStatus2) {
        this.orderStatus = orderStatus2;
    }

    public String getCardNo() {
        return this.cardNo;
    }

    public void setCardNo(String cardNo2) {
        this.cardNo = cardNo2;
    }

    public String getIsAllow() {
        return this.isAllow;
    }

    public void setIsAllow(String isAllow2) {
        this.isAllow = isAllow2;
    }

    public String getCityCode() {
        return this.cityCode;
    }

    public void setCityCode(String cityCode2) {
        this.cityCode = cityCode2;
    }

    public String getBalance() {
        return this.balance;
    }

    public void setBalance(String balance2) {
        this.balance = balance2;
    }

    public String getFee() {
        return this.fee;
    }

    public void setFee(String fee2) {
        this.fee = fee2;
    }

    public void setCardName(String cardName2) {
        this.cardName = cardName2;
    }

    public String getCardName() {
        return this.cardName;
    }

    public void setStandardFee(String standardFee2) {
        this.standardFee = standardFee2;
    }

    public String getStandardFee() {
        return this.standardFee;
    }

    public String getTimes() {
        return this.times;
    }

    public void setTimes(String times2) {
        this.times = times2;
    }

    public static ServerAccessCutoverInfo build(JSONObject cutoverInfoJSONObject) throws JSONException {
        ServerAccessCutoverInfo cutoverInfo = new ServerAccessCutoverInfo();
        if (cutoverInfoJSONObject.has("orderNo")) {
            cutoverInfo.setOrderNo(cutoverInfoJSONObject.getString("orderNo"));
        }
        if (cutoverInfoJSONObject.has("orderStatus")) {
            cutoverInfo.setOrderStatus(cutoverInfoJSONObject.getString("orderStatus"));
        }
        if (cutoverInfoJSONObject.has("cardNo")) {
            cutoverInfo.setCardNo(cutoverInfoJSONObject.getString("cardNo"));
        }
        if (cutoverInfoJSONObject.has("isAllow")) {
            cutoverInfo.setIsAllow(cutoverInfoJSONObject.getString("isAllow"));
        }
        if (cutoverInfoJSONObject.has("cityCode")) {
            cutoverInfo.setCityCode(cutoverInfoJSONObject.getString("cityCode"));
        }
        if (cutoverInfoJSONObject.has(HciConfigInfo.HCI_DATA_TYPE_AFTER_TRANSCTION_BALANCE)) {
            cutoverInfo.setBalance(cutoverInfoJSONObject.getString(HciConfigInfo.HCI_DATA_TYPE_AFTER_TRANSCTION_BALANCE));
        }
        if (cutoverInfoJSONObject.has("fee")) {
            cutoverInfo.setFee(cutoverInfoJSONObject.getString("fee"));
        }
        if (cutoverInfoJSONObject.has("cardName")) {
            cutoverInfo.setCardName(cutoverInfoJSONObject.getString("cardName"));
        }
        if (cutoverInfoJSONObject.has("standardFee")) {
            cutoverInfo.setStandardFee(cutoverInfoJSONObject.getString("standardFee"));
        }
        if (cutoverInfoJSONObject.has("times")) {
            cutoverInfo.setTimes(cutoverInfoJSONObject.getString("times"));
        }
        return cutoverInfo;
    }
}
