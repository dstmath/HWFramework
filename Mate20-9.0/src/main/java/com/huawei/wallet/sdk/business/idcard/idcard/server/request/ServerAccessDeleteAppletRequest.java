package com.huawei.wallet.sdk.business.idcard.idcard.server.request;

import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.unionpay.tsmservice.data.Constant;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessDeleteAppletRequest extends ServerAccessBaseRequest {
    public static final String REASON_LOST_CARD = "";
    public static final String REASON_OPEN_CARD_FAIL = "";
    public static final String REASON_REPAIRE_CARD = "";
    private static final String TAG = "IDCard:ServerAccessDeleteAppletRequest";
    private String cardType = "12";
    private String onlyDeleteApplet;
    private String reason = null;
    private String source;

    public ServerAccessDeleteAppletRequest(String issuerId, String cplc, String aid, String deviceModel, String seChipManuFacturer) {
        setIssuerId(issuerId);
        setCplc(cplc);
        setAid(aid);
        setDeviceModel(deviceModel);
        setSeChipManuFacturer(seChipManuFacturer);
        setSource("HandSet");
    }

    public String getCardType() {
        return this.cardType;
    }

    public void setCardType(String cardType2) {
        this.cardType = cardType2;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason2) {
        this.reason = reason2;
    }

    public String getOnlyDeleteApplet() {
        return this.onlyDeleteApplet;
    }

    public void setOnlyDeleteApplet(String onlyDeleteApplet2) {
        this.onlyDeleteApplet = onlyDeleteApplet2;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source2) {
        this.source = source2;
    }

    public JSONObject createRequestData(JSONObject headerObject) {
        JSONObject jObj;
        if (headerObject == null || StringUtil.isEmpty(this.source, true)) {
            LogX.e("ServerAccessDeleteAppletTask createDataStr, invalid param");
            return null;
        }
        try {
            jObj = new JSONObject();
            jObj.put("header", headerObject);
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, getIssuerId());
            jObj.put("cplc", getCplc());
            jObj.put("aid", getAid());
            jObj.put(ExecuteApduTask.DEVICE_MODEL, getDeviceModel());
            jObj.put("seChipManuFacturer", getSeChipManuFacturer());
            jObj.put("source", getSource());
            if (!StringUtil.isEmpty(getSn(), true)) {
                jObj.put("sn", getSn());
            }
            if (!StringUtil.isEmpty(getPhoneManufacturer(), true)) {
                jObj.put("phoneManufacturer", getPhoneManufacturer());
            }
            if (!StringUtil.isEmpty(getCardType(), true)) {
                jObj.put(Constant.KEY_CARD_TYPE, getCardType());
            }
            if (!StringUtil.isEmpty(getOnlyDeleteApplet(), true)) {
                jObj.put("onlyDeleteApplet", getOnlyDeleteApplet());
            }
            if (!StringUtil.isEmpty(getReason(), true)) {
                jObj.put("reason", getReason());
            }
            if (!StringUtil.isEmpty(getReserved(), true)) {
                jObj.put("reserved", getReserved());
            }
        } catch (JSONException e) {
            LogX.e("ServerAccessDeleteAppletTask createDataStr, JSONException");
            jObj = null;
        }
        return jObj;
    }

    public boolean valid() {
        if (!StringUtil.isEmpty(getIssuerId(), true) && !StringUtil.isEmpty(getCplc(), true) && !StringUtil.isEmpty(getAid(), true) && !StringUtil.isEmpty(getDeviceModel(), true) && !StringUtil.isEmpty(getSeChipManuFacturer(), true)) {
            return true;
        }
        LogC.e(TAG, "invalid param .", false);
        return false;
    }
}
