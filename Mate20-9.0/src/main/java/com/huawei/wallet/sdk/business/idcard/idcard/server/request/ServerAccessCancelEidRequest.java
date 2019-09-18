package com.huawei.wallet.sdk.business.idcard.idcard.server.request;

import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.request.CardServerBaseRequest;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessCancelEidRequest extends CardServerBaseRequest {
    private static final String TAG = "IDCard:CancelEidRequest";
    private String carrierSn = null;
    private String cplc = null;
    private String issuerId = null;

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public String getCarrierSn() {
        return this.carrierSn;
    }

    public void setCarrierSn(String carrierSn2) {
        this.carrierSn = carrierSn2;
    }

    public JSONObject createRequestData(JSONObject headerObject) {
        JSONObject jObj;
        if (headerObject == null || StringUtil.isEmpty(this.cplc, true)) {
            LogC.e(TAG, "createRequestData params error.", false);
            return null;
        }
        try {
            jObj = new JSONObject();
            jObj.put("header", headerObject);
            jObj.put("cplc", getCplc());
            jObj.put("issuerId", getIssuerId());
            jObj.put("carrierSn", getCarrierSn());
        } catch (JSONException e) {
            LogC.e(TAG, "ServerAccessCancelEidRequest createRequestData, JSONException", false);
            jObj = null;
        }
        return jObj;
    }

    public boolean valid() {
        if (!StringUtil.isEmpty(getCplc(), true)) {
            return true;
        }
        LogC.e(TAG, "invalid param.", false);
        return false;
    }
}
