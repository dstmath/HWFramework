package com.android.server.hidata.wavemapping.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ModelInfo {
    private String[] bssidList;
    private int dataLen;
    private int[][] datas;
    private ArrayList<HashMap<String, Integer>> hpDatas;
    private String modelName;
    private String place;
    private HashSet<String> setBssids;
    private String storePath;
    private String updateTime;

    public ModelInfo(String place2, String storePath2, String updateTime2) {
        this.place = place2;
        this.storePath = storePath2;
        this.updateTime = updateTime2;
    }

    public ModelInfo(String place2, int modelName2) {
        this.place = place2;
        this.modelName = Integer.toString(modelName2);
    }

    public String getModelName() {
        return this.modelName;
    }

    public void setModelName(String modelName2) {
        this.modelName = modelName2;
    }

    public ArrayList<HashMap<String, Integer>> getHpDatas() {
        return this.hpDatas;
    }

    public void setHpDatas(ArrayList<HashMap<String, Integer>> hpDatas2) {
        this.hpDatas = hpDatas2;
    }

    public int getDataLen() {
        return this.dataLen;
    }

    public void setDataLen(int dataLen2) {
        this.dataLen = dataLen2;
    }

    public HashSet<String> getSetBssids() {
        return this.setBssids;
    }

    public void setSetBssids(HashSet<String> setBssids2) {
        this.setBssids = setBssids2;
    }

    public String[] getBssidList() {
        String[] strArr = this.bssidList;
        if (strArr == null) {
            return new String[0];
        }
        return (String[]) strArr.clone();
    }

    public void setBssidList(String[] bssidList2) {
        if (bssidList2 == null) {
            this.bssidList = null;
        } else {
            this.bssidList = (String[]) bssidList2.clone();
        }
    }

    public int[][] getDatas() {
        int[][] iArr = this.datas;
        if (iArr == null) {
            return new int[0][];
        }
        return (int[][]) iArr.clone();
    }

    public void setDatas(int[][] datas2) {
        if (datas2 == null) {
            this.datas = null;
        } else {
            this.datas = (int[][]) datas2.clone();
        }
    }

    public String getPlace() {
        return this.place;
    }

    public void setPlace(String place2) {
        this.place = place2;
    }

    public String getStorePath() {
        return this.storePath;
    }

    public void setStorePath(String storePath2) {
        this.storePath = storePath2;
    }

    public String getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(String updateTime2) {
        this.updateTime = updateTime2;
    }
}
