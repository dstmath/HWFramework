package com.huawei.wallet.sdk.business.bankcard.modle;

import com.huawei.wallet.sdk.common.log.LogC;
import org.json.JSONException;
import org.json.JSONObject;

public class TATrustedStorageInfo {
    private static final String TA_JSON_KEY_CERT_UPLOAD_FLAG = "cert_upload_flag";
    private static final String TA_JSON_KEY_COUNTRY_CODE = "country_code";
    private static final String TA_JSON_KEY_ESE_UNLOCK_TIMES = "ese_unlock_times";
    private static final String TA_JSON_KEY_FACT_RESET_FLAG = "fact_reset_flag";
    private static final String TA_JSON_KEY_ROUTER_INFO = "router_info";
    private int certUploadFlag;
    private String countryCode;
    private int eSEUnlockTimes;
    private int resetFactFlag;
    private String routerInfo;

    public void setESEUnlockTimes(int eSEUnlockTimesValue) {
        this.eSEUnlockTimes = eSEUnlockTimesValue;
    }

    public void setCertUploadFlag(int flagCertUpload) {
        this.certUploadFlag = flagCertUpload;
    }

    public void setResetFactoryFlag(int flagFactReset) {
        this.resetFactFlag = flagFactReset;
    }

    public void setRouterInfo(String strRouterInfo) {
        this.routerInfo = strRouterInfo;
    }

    public void setCountryCode(String strCountryCode) {
        this.countryCode = strCountryCode;
    }

    public int getESEUnlockTimes() {
        return this.eSEUnlockTimes;
    }

    public int getCertUploadFlag() {
        return this.certUploadFlag;
    }

    public int getResetFactoryFlag() {
        return this.resetFactFlag;
    }

    public String getRouterInfo() {
        return this.routerInfo;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public TATrustedStorageInfo() {
    }

    public TATrustedStorageInfo(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            if (json.has(TA_JSON_KEY_ESE_UNLOCK_TIMES)) {
                this.eSEUnlockTimes = json.getInt(TA_JSON_KEY_ESE_UNLOCK_TIMES);
            }
            if (json.has(TA_JSON_KEY_CERT_UPLOAD_FLAG)) {
                this.certUploadFlag = json.getInt(TA_JSON_KEY_CERT_UPLOAD_FLAG);
            }
            if (json.has(TA_JSON_KEY_FACT_RESET_FLAG)) {
                this.resetFactFlag = json.getInt(TA_JSON_KEY_FACT_RESET_FLAG);
            }
            if (json.has(TA_JSON_KEY_ROUTER_INFO)) {
                this.routerInfo = json.getString(TA_JSON_KEY_ROUTER_INFO);
            }
            if (json.has(TA_JSON_KEY_COUNTRY_CODE)) {
                this.countryCode = json.getString(TA_JSON_KEY_COUNTRY_CODE);
            }
        } catch (JSONException ex) {
            LogC.e("create trusted storage info failed: " + ex.getMessage(), true);
        }
    }

    public String getTaTsInfoJsonStr() {
        JSONObject json = new JSONObject();
        try {
            json.put(TA_JSON_KEY_ESE_UNLOCK_TIMES, this.eSEUnlockTimes);
            json.put(TA_JSON_KEY_CERT_UPLOAD_FLAG, this.certUploadFlag);
            json.put(TA_JSON_KEY_FACT_RESET_FLAG, this.resetFactFlag);
            json.put(TA_JSON_KEY_ROUTER_INFO, this.routerInfo);
            json.put(TA_JSON_KEY_COUNTRY_CODE, this.countryCode);
        } catch (JSONException e) {
            LogC.e("getTaTsInfoJsonStr, json exception: " + e.getMessage(), true);
            json = null;
        }
        if (json == null) {
            return null;
        }
        return json.toString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("eSEUnlockTimes=");
        sb.append(this.eSEUnlockTimes);
        sb.append("\n");
        sb.append("isCertUploadFlag=");
        sb.append(this.certUploadFlag);
        sb.append("\n");
        sb.append("isFactResetFlag=");
        sb.append(this.resetFactFlag);
        sb.append("\n");
        sb.append("routerInfo=");
        sb.append(this.routerInfo);
        sb.append("\n");
        sb.append("countryCode=");
        sb.append(this.countryCode);
        sb.append("\n");
        return super.toString();
    }
}
