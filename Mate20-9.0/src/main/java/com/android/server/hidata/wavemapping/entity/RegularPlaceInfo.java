package com.android.server.hidata.wavemapping.entity;

import com.android.server.hidata.wavemapping.cons.Constant;

public class RegularPlaceInfo {
    private int batch;
    private int beginTime;
    private int disNum;
    private int fingerNum;
    private int identifyNum;
    private boolean isMainAp;
    private int modelName;
    private String noOcurBssids;
    private String place;
    private int screenState;
    private int state;
    private int testDataNum;

    public boolean isMainAp() {
        return this.isMainAp;
    }

    public void setMainAp(boolean mainAp) {
        this.isMainAp = mainAp;
    }

    public String getNoOcurBssids() {
        return this.noOcurBssids;
    }

    public void setNoOcurBssids(String noOcurBssids2) {
        this.noOcurBssids = noOcurBssids2;
    }

    public int getIdentifyNum() {
        return this.identifyNum;
    }

    public void setIdentifyNum(int identifyNum2) {
        this.identifyNum = identifyNum2;
    }

    public int getTestDataNum() {
        return this.testDataNum;
    }

    public void setTestDataNum(int testDataNum2) {
        this.testDataNum = testDataNum2;
    }

    public int getDisNum() {
        return this.disNum;
    }

    public void setDisNum(int disNum2) {
        this.disNum = disNum2;
    }

    public int getScreenState() {
        return this.screenState;
    }

    public void setScreenState(int screenState2) {
        this.screenState = screenState2;
    }

    public int getBatch() {
        return this.batch;
    }

    public void setBatch(int batch2) {
        this.batch = batch2;
    }

    public RegularPlaceInfo(String place2, int state2, int batch2, int fingerNum2, int testDataNum2, int disNum2, int identifyNum2, String noOcurBssids2, int isMainAp2) {
        this.place = place2;
        this.state = state2;
        this.batch = batch2;
        this.fingerNum = fingerNum2;
        this.testDataNum = testDataNum2;
        this.disNum = disNum2;
        this.identifyNum = identifyNum2;
        this.noOcurBssids = noOcurBssids2;
        this.isMainAp = isMainAp2 != 1 ? false : true;
    }

    public RegularPlaceInfo(String place2, int state2, int batch2, int fingerNum2, int testDataNum2, int disNum2, int identifyNum2, String noOcurBssids2, boolean isMainAp2) {
        this.place = place2;
        this.state = state2;
        this.batch = batch2;
        this.fingerNum = fingerNum2;
        this.testDataNum = testDataNum2;
        this.disNum = disNum2;
        this.identifyNum = identifyNum2;
        this.noOcurBssids = noOcurBssids2;
        this.isMainAp = isMainAp2;
    }

    public int getFingerNum() {
        return this.fingerNum;
    }

    public void setFingerNum(int fingerNum2) {
        this.fingerNum = fingerNum2;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state2) {
        this.state = state2;
    }

    public String getPlace() {
        return this.place;
    }

    public void setPlace(String place2) {
        this.place = place2;
    }

    public int getModelName() {
        return this.modelName;
    }

    public void setModelName(String modelName2) {
        if (modelName2 != null) {
            int model = -1;
            if (Constant.PATTERN_STR2INT.matcher(modelName2).matches()) {
                try {
                    model = Integer.parseInt(modelName2);
                } catch (NumberFormatException e) {
                    return;
                }
            }
            this.modelName = model;
        }
    }

    public int getBeginTime() {
        return this.beginTime;
    }

    public void setBeginTime(int beginTime2) {
        this.beginTime = beginTime2;
    }

    public String toString() {
        return "RegularPlaceInfo{place='" + this.place + '\'' + ", state=" + this.state + ", batch=" + this.batch + ", fingerNum=" + this.fingerNum + ", screenState=" + this.screenState + ", testDataNum=" + this.testDataNum + ", disNum=" + this.disNum + ", identifyNum=" + this.identifyNum + ", isMainAp=" + this.isMainAp + ", modelName='" + this.modelName + '\'' + '}';
    }
}
