package com.android.server.hidata.wavemapping.entity;

import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.util.LogUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class UiInfo {
    private static final float DEFAULT_DIST_NUMBER = 100.0f;
    private static final int DEFAULT_FINGER_NUMBER = 20;
    private static final int DEFAULT_KNN_DIST_NUMBER = 200;
    private static final String KEY_CLUSTER_NUM = "cluster_num";
    private static final String KEY_FG_FINGER_NUM = "fg_fingers_num";
    private static final String KEY_FINGER_BATCH_NUM = "finger_batch_num";
    private static final String KEY_KNN_MAX_DIST = "knnMaxDist";
    private static final String KEY_MAX_DIST = "maxDist";
    private static final String KEY_PRE_LABEL = "preLabel";
    private static final String KEY_SSID = "ssid";
    private static final String KEY_STAGE = "stage";
    private static final String KEY_TOAST = "toast";
    private String clusterNumber = "";
    private int fgFingersNumber = 20;
    private int fingerBatchNumber = 0;
    private String jsonResult = "";
    private int knnMaxDist = 200;
    private float maxDist = DEFAULT_DIST_NUMBER;
    private String preLabel = "";
    private String ssid = "";
    private int stage = 3;
    private String toast = "";

    public UiInfo() {
        ParameterInfo param = ParamManager.getInstance().getParameterInfo();
        this.fgFingersNumber = param.getFgBatchNum();
        this.maxDist = param.getMaxDist();
        this.knnMaxDist = param.getKnnMaxDist();
    }

    public String getToast() {
        return this.toast;
    }

    public void setToast(String toast2) {
        this.toast = toast2;
    }

    public String getPreLabel() {
        return this.preLabel;
    }

    public void setPreLabel(String preLabel2) {
        this.preLabel = preLabel2;
    }

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String ssid2) {
        this.ssid = ssid2;
    }

    public int getStage() {
        return this.stage;
    }

    public void setStage(int stage2) {
        this.stage = stage2;
    }

    public int getFingerBatchNumber() {
        return this.fingerBatchNumber;
    }

    public void setFingerBatchNumber(int fingerBatchNumber2) {
        this.fingerBatchNumber = fingerBatchNumber2;
    }

    public String getClusterNumber() {
        return this.clusterNumber;
    }

    public void setClusterNumber(String clusterNumber2) {
        this.clusterNumber = clusterNumber2;
    }

    public int getFgFingersNumber() {
        return this.fgFingersNumber;
    }

    public void setFgFingersNumber(int fgFingersNumber2) {
        this.fgFingersNumber = fgFingersNumber2;
    }

    public float getMaxDist() {
        return this.maxDist;
    }

    public void setMaxDist(float maxDist2) {
        this.maxDist = maxDist2;
    }

    public int getKnnMaxDist() {
        return this.knnMaxDist;
    }

    public void setKnnMaxDist(int knnMaxDist2) {
        this.knnMaxDist = knnMaxDist2;
    }

    public String toJsonStr() {
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put(KEY_STAGE, this.stage);
            jsonParam.put(KEY_FINGER_BATCH_NUM, this.fingerBatchNumber);
            jsonParam.put(KEY_CLUSTER_NUM, this.clusterNumber);
            jsonParam.put(KEY_FG_FINGER_NUM, this.fgFingersNumber);
            jsonParam.put(KEY_MAX_DIST, (double) this.maxDist);
            jsonParam.put(KEY_KNN_MAX_DIST, this.knnMaxDist);
            jsonParam.put(KEY_SSID, this.ssid);
            jsonParam.put(KEY_PRE_LABEL, this.preLabel);
            jsonParam.put(KEY_TOAST, this.toast);
        } catch (JSONException e) {
            LogUtil.e(false, "toJsonStr,e %{public}s", e.getMessage());
        }
        this.jsonResult = jsonParam.toString();
        if (this.jsonResult == null) {
            this.jsonResult = "";
        }
        return this.jsonResult;
    }
}
