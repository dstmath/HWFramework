package com.android.server.hidata.wavemapping.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ModelInfo {
    private String[] bssidLst;
    private int dataLen;
    private int[][] datas;
    private ArrayList<HashMap<String, Integer>> hpDatas;
    private String modelName;
    private String place;
    private HashSet<String> setBssids;
    private String storePath;
    private String updateTime;

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

    public String[] getBssidLst() {
        if (this.bssidLst == null) {
            return new String[0];
        }
        return (String[]) this.bssidLst.clone();
    }

    public void setBssidLst(String[] bssidLst2) {
        if (bssidLst2 == null) {
            this.bssidLst = null;
        } else {
            this.bssidLst = (String[]) bssidLst2.clone();
        }
    }

    public int[][] getDatas() {
        if (this.datas == null) {
            return new int[0][];
        }
        return (int[][]) this.datas.clone();
    }

    public void setDatas(int[][] datas2) {
        if (datas2 == null) {
            this.datas = null;
        } else {
            this.datas = (int[][]) datas2.clone();
        }
    }

    public ModelInfo(String place2, String storePath2, String updateTime2) {
        this.place = place2;
        this.storePath = storePath2;
        this.updateTime = updateTime2;
    }

    public ModelInfo(String place2, int modelName2) {
        this.place = place2;
        this.modelName = Integer.toString(modelName2);
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
