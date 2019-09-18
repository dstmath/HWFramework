package com.huawei.wallet.sdk.business.idcard.accesscard.server.request;

import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.request.CardServerBaseRequest;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class OpenAccessCardRequest extends CardServerBaseRequest {
    private static final String TAG = "OpenAccessCardRequest";
    private String appletAid;
    private String cplc;
    private String deviceModel;
    private String issuerid;
    private String latitude;
    private String longitude;
    private String reserved;
    private String seChipManuFacturer;
    private String uid;

    public String getIssuerid() {
        return this.issuerid;
    }

    public void setIssuerid(String issuerid2) {
        this.issuerid = issuerid2;
    }

    public String getCplc() {
        return this.cplc;
    }

    public void setCplc(String cplc2) {
        this.cplc = cplc2;
    }

    public String getDeviceModel() {
        return this.deviceModel;
    }

    public void setDeviceModel(String deviceModel2) {
        this.deviceModel = deviceModel2;
    }

    public String getAppletAid() {
        return this.appletAid;
    }

    public void setAppletAid(String appletAid2) {
        this.appletAid = appletAid2;
    }

    public String getSeChipManuFacturer() {
        return this.seChipManuFacturer;
    }

    public void setSeChipManuFacturer(String seChipManuFacturer2) {
        this.seChipManuFacturer = seChipManuFacturer2;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid2) {
        this.uid = uid2;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public void setLongitude(String longitude2) {
        this.longitude = longitude2;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(String latitude2) {
        this.latitude = latitude2;
    }

    public String getReserved() {
        return this.reserved;
    }

    public void setReserved(String reserved2) {
        this.reserved = reserved2;
    }

    public JSONObject createRequestData(JSONObject headerObject) {
        JSONObject jObj;
        if (headerObject == null) {
            LogC.e("OpenAccessCardRequest createRequestData params error.", false);
            return null;
        }
        try {
            jObj = new JSONObject();
            jObj.put("header", headerObject);
            if (!StringUtil.isEmpty(getIssuerid(), false)) {
                jObj.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, getIssuerid());
            }
            if (!StringUtil.isEmpty(getCplc(), false)) {
                jObj.put("cplc", getCplc());
            }
            if (!StringUtil.isEmpty(getDeviceModel(), false)) {
                jObj.put(ExecuteApduTask.DEVICE_MODEL, getDeviceModel());
            }
            if (!StringUtil.isEmpty(this.appletAid, false)) {
                jObj.put(ServerAccessApplyAPDURequest.ReqKey.AID, this.appletAid);
            }
            if (!StringUtil.isEmpty(this.seChipManuFacturer, false)) {
                jObj.put("seChipManuFacturer", this.seChipManuFacturer);
            }
            if (!StringUtil.isEmpty(this.uid, false)) {
                jObj.put("uid", this.uid);
            }
            jObj.put(SNBConstant.FIELD_LOCATION_LONGITUDE, this.longitude);
            jObj.put(SNBConstant.FIELD_LOCATION_LATITUDE, this.latitude);
        } catch (JSONException e) {
            LogX.e("OpenAccessCardTask createDataStr, JSONException");
            jObj = null;
        }
        return jObj;
    }
}
