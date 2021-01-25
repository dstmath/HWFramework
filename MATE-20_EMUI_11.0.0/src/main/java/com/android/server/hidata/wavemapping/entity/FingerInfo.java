package com.android.server.hidata.wavemapping.entity;

import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class FingerInfo {
    private static final String COMMA = ",";
    private static final String KEY_AR = "ar";
    private static final String KEY_BATCH = "batch";
    private static final String KEY_BSSID_DATA = "bissiddatas";
    private static final String KEY_LABEL_ID = "labelId";
    private static final String KEY_LINK_SPEED = "linkSpeed";
    private static final String KEY_SCREEN = "screen";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String SEMICOLON = ";";
    private int ar;
    private int batch;
    private HashMap<String, Integer> bssidDatas;
    private String jsonResult;
    private int labelId;
    private int linkSpeed;
    private int screen;
    private String serveMac;
    private String timestamp;

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

    public HashMap<String, Integer> getBssidDatas() {
        return this.bssidDatas;
    }

    public void setBssidDatas(HashMap<String, Integer> bssidDatas2) {
        this.bssidDatas = bssidDatas2;
    }

    public String toString() {
        return "FingerInfo{batch=" + this.batch + ", ar=" + this.ar + ", screen=" + this.screen + ", timestamp='" + this.timestamp + "', linkSpeed=" + this.linkSpeed + ", labelId=" + this.labelId + ", bssidDatas=" + this.bssidDatas + '}';
    }

    public String toReString() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(KEY_BATCH, this.batch);
            jsonObj.put(KEY_AR, this.ar);
            jsonObj.put(KEY_SCREEN, this.screen);
            jsonObj.put(KEY_TIMESTAMP, this.timestamp);
            jsonObj.put(KEY_LINK_SPEED, this.linkSpeed);
            jsonObj.put(KEY_LABEL_ID, this.labelId);
            jsonObj.put(KEY_BSSID_DATA, this.bssidDatas);
        } catch (JSONException e) {
            LogUtil.e(false, "LocatingState,e %{public}s", e.getMessage());
        }
        this.jsonResult = jsonObj.toString();
        return this.jsonResult.replace(",", ";");
    }
}
