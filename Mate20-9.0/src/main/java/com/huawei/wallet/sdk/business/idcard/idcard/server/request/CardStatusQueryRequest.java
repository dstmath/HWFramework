package com.huawei.wallet.sdk.business.idcard.idcard.server.request;

import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.request.CardServerBaseRequest;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class CardStatusQueryRequest extends CardServerBaseRequest {
    private static final String TAG = "IDCard:CardStatusQueryRequest";
    private String cplc;
    private String needQrCode;

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public String getNeedQrCode() {
        return this.needQrCode;
    }

    public void setNeedQrCode(String needQrCode2) {
        this.needQrCode = needQrCode2;
    }

    public JSONObject createRequestData(JSONObject headerObject) {
        JSONObject dataObject = null;
        if (headerObject == null) {
            LogC.e("IDCard:CardStatusQueryRequest createRequestData params error.", false);
            return null;
        }
        try {
            JSONObject dataObject2 = new JSONObject();
            dataObject2.put("header", headerObject);
            dataObject2.put("cplc", this.cplc);
            if (this.needQrCode != null && !StringUtil.isEmpty(this.needQrCode, true)) {
                dataObject2.put("needQrCode", this.needQrCode);
            }
            dataObject = dataObject2;
        } catch (JSONException e) {
            LogC.e("IDCard:CardStatusQueryRequest createDataStr JSONException.", false);
        }
        return dataObject;
    }

    public boolean valid() {
        if (!StringUtil.isEmpty(getSrcTransactionID(), true) && !StringUtil.isEmpty(getMerchantID(), true)) {
            return true;
        }
        LogC.e(TAG, "TransactionID or MerchantID is null .", false);
        return false;
    }
}
