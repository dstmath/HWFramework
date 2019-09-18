package com.huawei.wallet.sdk.business.idcard.idcard.server.request;

import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessApplyAPDURequest extends ServerAccessBaseRequest {
    private static final String TAG = "IDCard:ServerAccessApplyAPDURequest";
    private int apduCount = 0;
    private List<ServerAccessAPDU> apduList = null;
    private String currentStep;
    private String transactionId = null;

    public ServerAccessApplyAPDURequest(String issuerId, String aid, String cplc, String transactionId2, int apduCount2, List<ServerAccessAPDU> apduList2, String deviceModel, String seChipManuFacturer) {
        setIssuerId(issuerId);
        setAid(aid);
        setCplc(cplc);
        setDeviceModel(deviceModel);
        setSeChipManuFacturer(seChipManuFacturer);
        this.transactionId = transactionId2;
        this.apduCount = apduCount2;
        this.apduList = apduList2;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId2) {
        this.transactionId = transactionId2;
    }

    public int getApduCount() {
        return this.apduCount;
    }

    public void setApduCount(int apduCount2) {
        this.apduCount = apduCount2;
    }

    public List<ServerAccessAPDU> getApduList() {
        return this.apduList;
    }

    public void setApduList(List<ServerAccessAPDU> apduList2) {
        this.apduList = apduList2;
    }

    public String getCurrentStep() {
        return this.currentStep;
    }

    public void setCurrentStep(String currentStep2) {
        this.currentStep = currentStep2;
    }

    public JSONObject createRequestData(JSONObject headerObject) {
        JSONObject jObj;
        if (headerObject == null || StringUtil.isEmpty(this.transactionId, true) || StringUtil.isEmpty(this.currentStep, true)) {
            LogC.e("IDCard:ServerAccessApplyAPDURequest createRequestData params error.", false);
            return null;
        }
        try {
            jObj = new JSONObject();
            jObj.put("header", headerObject);
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, getIssuerId());
            jObj.put("aid", getAid());
            jObj.put("cplc", getCplc());
            jObj.put("transactionid", getTransactionId());
            jObj.put("apduCount", getApduCount());
            jObj.put("seChipManuFacturer", getSeChipManuFacturer());
            jObj.put(ExecuteApduTask.DEVICE_MODEL, getDeviceModel());
            jObj.put("currentStep", getCurrentStep());
            JSONArray jArray = new JSONArray();
            for (ServerAccessAPDU apdu : getApduList()) {
                JSONObject jObject = new JSONObject();
                jObject.put("apduNo", apdu.getApduId());
                jObject.put("apduContent", apdu.getApduContent());
                if (!StringUtil.isEmpty(apdu.getApduStatus(), true)) {
                    jObject.put("apduStatus", apdu.getApduStatus());
                }
                if (!StringUtil.isEmpty(apdu.getCommand(), true)) {
                    jObject.put("command", apdu.getCommand());
                }
                if (!StringUtil.isEmpty(apdu.getChecker(), true)) {
                    jObject.put("checker", apdu.getChecker());
                }
                jArray.put(jObject);
            }
            jObj.put("apduList", jArray);
            if (!StringUtil.isEmpty(getReserved(), true)) {
                jObj.put("reserved", getReserved());
            }
        } catch (JSONException e) {
            LogX.e("ServerAccessApplyAPDUTask createDataStr, JSONException");
            jObj = null;
        }
        return jObj;
    }

    public boolean valid() {
        if (!StringUtil.isEmpty(getIssuerId(), true) && !StringUtil.isEmpty(getAid(), true) && !StringUtil.isEmpty(getCplc(), true) && !StringUtil.isEmpty(getTransactionId(), true) && getApduList() != null && !getApduList().isEmpty() && getApduCount() == getApduList().size() && !StringUtil.isEmpty(getDeviceModel(), true) && !StringUtil.isEmpty(getSeChipManuFacturer(), true)) {
            return true;
        }
        LogC.e(TAG, "invalid param .", false);
        return false;
    }
}
