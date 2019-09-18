package com.huawei.wallet.sdk.common.apdu.model;

import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.JSONHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessAPDU {
    private String apduContent = null;
    private String apduId = null;
    private String apduStatus = null;
    private String checker;
    private String command;

    public String getApduId() {
        return this.apduId;
    }

    public void setApduId(String apduId2) {
        this.apduId = apduId2;
    }

    public String getApduContent() {
        return this.apduContent;
    }

    public void setApduContent(String apduContent2) {
        this.apduContent = apduContent2;
    }

    public String getApduStatus() {
        return this.apduStatus;
    }

    public void setApduStatus(String apduStatus2) {
        this.apduStatus = apduStatus2;
    }

    public static ServerAccessAPDU buildFromJson(JSONObject jObject) {
        if (jObject == null) {
            return null;
        }
        ServerAccessAPDU apdu = new ServerAccessAPDU();
        try {
            apdu.apduId = JSONHelper.getStringValue(jObject, "apduNo");
            apdu.apduContent = JSONHelper.getStringValue(jObject, "apduContent");
            apdu.apduStatus = JSONHelper.getStringValue(jObject, "apduStatus");
            apdu.command = JSONHelper.getStringValue(jObject, "command");
            apdu.checker = JSONHelper.getStringValue(jObject, "checker");
        } catch (JSONException e) {
            LogC.e("ServerAccessAPDU buildFromJson, JSONException", false);
            apdu = null;
        }
        return apdu;
    }

    public static ServerAccessAPDU visaBuildFromJson(JSONObject jObject) {
        if (jObject == null) {
            return null;
        }
        ServerAccessAPDU apdu = new ServerAccessAPDU();
        try {
            apdu.apduId = JSONHelper.getStringValue(jObject, "apduID");
            apdu.apduContent = JSONHelper.getStringValue(jObject, "apdu");
            apdu.apduStatus = JSONHelper.getStringValue(jObject, "apduStatus");
            apdu.command = JSONHelper.getStringValue(jObject, "command");
            apdu.checker = JSONHelper.getStringValue(jObject, "checker");
        } catch (JSONException e) {
            LogC.e("ServerAccessAPDU VisaBuildFromJson, JSONException", false);
            apdu = null;
        }
        return apdu;
    }

    public String toString() {
        return "ServerAccessAPDU{apduId='" + this.apduId + '\'' + ", apduContent='" + this.apduContent + '\'' + ", apduStatus='" + this.apduStatus + '\'' + ", command='" + this.command + '\'' + ", checker='" + this.checker + '\'' + '}';
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command2) {
        this.command = command2;
    }

    public String getChecker() {
        return this.checker;
    }

    public void setChecker(String checker2) {
        this.checker = checker2;
    }
}
