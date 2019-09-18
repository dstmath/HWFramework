package com.android.server.hidata.wavemapping.entity;

import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class FingerInfo {
    int ar;
    int batch;
    HashMap<String, Integer> bissiddatas;
    String jsonResult;
    int labelId;
    int linkSpeed;
    int screen;
    String serveMac;
    String timestamp;

    public String getServeMac() {
        return this.serveMac;
    }

    public void setServeMac(String serveMac2) {
        this.serveMac = serveMac2;
    }

    public int getLabelId() {
        return this.labelId;
    }

    public void setLabelId(int labelId2) {
        this.labelId = labelId2;
    }

    public int getBatch() {
        return this.batch;
    }

    public void setBatch(int batch2) {
        this.batch = batch2;
    }

    public int getAr() {
        return this.ar;
    }

    public void setAr(int ar2) {
        this.ar = ar2;
    }

    public int getScreen() {
        return this.screen;
    }

    public void setScreen(int screen2) {
        this.screen = screen2;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp2) {
        this.timestamp = timestamp2;
    }

    public int getLinkSpeed() {
        return this.linkSpeed;
    }

    public void setLinkSpeed(int linkSpeed2) {
        this.linkSpeed = linkSpeed2;
    }

    public HashMap<String, Integer> getBissiddatas() {
        return this.bissiddatas;
    }

    public void setBissiddatas(HashMap<String, Integer> bissiddatas2) {
        this.bissiddatas = bissiddatas2;
    }

    public String toString() {
        return "FingerInfo{batch=" + this.batch + ", ar=" + this.ar + ", screen=" + this.screen + ", timestamp='" + this.timestamp + '\'' + ", linkSpeed=" + this.linkSpeed + ", labelId=" + this.labelId + ", bissiddatas=" + this.bissiddatas + '}';
    }

    public String toReString() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("batch", this.batch);
            jsonObj.put("ar", this.ar);
            jsonObj.put("screen", this.screen);
            jsonObj.put("timestamp", this.timestamp);
            jsonObj.put("linkSpeed", this.linkSpeed);
            jsonObj.put("labelId", this.labelId);
            jsonObj.put("bissiddatas", this.bissiddatas);
        } catch (JSONException e) {
            LogUtil.e("LocatingState,e" + e.getMessage());
        }
        this.jsonResult = jsonObj.toString();
        return this.jsonResult.replace(",", CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
    }
}
