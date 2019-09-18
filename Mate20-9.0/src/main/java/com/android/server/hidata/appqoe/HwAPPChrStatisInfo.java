package com.android.server.hidata.appqoe;

public class HwAPPChrStatisInfo {
    public static final int CHR_ADD_TYPE_BAD_AFTER_MP = 15;
    public static final int CHR_ADD_TYPE_CELL_PERIOD = 6;
    public static final int CHR_ADD_TYPE_CELL_STALL = 4;
    public static final int CHR_ADD_TYPE_CELL_START = 2;
    public static final int CHR_ADD_TYPE_CH_FAILED = 11;
    public static final int CHR_ADD_TYPE_CLOSE_CELL = 18;
    public static final int CHR_ADD_TYPE_CLOSE_WIFI = 19;
    public static final int CHR_ADD_TYPE_GOOD_AFTER_MP = 14;
    public static final int CHR_ADD_TYPE_HICURE_SUCCESS = 21;
    public static final int CHR_ADD_TYPE_IN_KQI = 16;
    public static final int CHR_ADD_TYPE_MP_FAILED = 12;
    public static final int CHR_ADD_TYPE_MP_SUCCESS = 13;
    public static final int CHR_ADD_TYPE_OUT_KQI = 17;
    public static final int CHR_ADD_TYPE_REASON_1 = 7;
    public static final int CHR_ADD_TYPE_REASON_2 = 8;
    public static final int CHR_ADD_TYPE_REASON_3 = 9;
    public static final int CHR_ADD_TYPE_REASON_4 = 10;
    public static final int CHR_ADD_TYPE_START_HICURE = 20;
    public static final int CHR_ADD_TYPE_TRAFFIC = 22;
    public static final int CHR_ADD_TYPE_WIFI_PERIOD = 5;
    public static final int CHR_ADD_TYPE_WIFI_STALL = 3;
    public static final int CHR_ADD_TYPE_WIFI_START = 1;
    public int afbNum;
    public int afgNum;
    public int appId;
    public int cellStallNum;
    public int cellStartNum;
    public int cellspNum;
    public int chfNum;
    public int closeCellNum;
    public int closeWiFiNum;
    public int hicsNum;
    public int inKQINum;
    public int mpfNum;
    public int mpsNum;
    public int overKQINum;
    public int rn1Num;
    public int rn2Num;
    public int rn3Num;
    public int rn4Num;
    public int scenceId;
    public int startHicNum;
    public int trffic;
    public int wifiStallNum;
    public int wifiStartNum;
    public int wifispNum;

    public void copyInfo(HwAPPChrStatisInfo info) {
        this.appId = info.appId;
        this.scenceId = info.scenceId;
        this.wifiStartNum = info.wifiStartNum;
        this.cellStartNum = info.cellStartNum;
        this.wifiStallNum = info.wifiStallNum;
        this.cellStallNum = info.cellStallNum;
        this.wifispNum = info.wifispNum;
        this.cellspNum = info.cellspNum;
        this.rn1Num = info.rn1Num;
        this.rn2Num = info.rn2Num;
        this.rn3Num = info.rn3Num;
        this.rn4Num = info.rn4Num;
        this.chfNum = info.chfNum;
        this.mpfNum = info.mpfNum;
        this.mpsNum = info.mpsNum;
        this.afgNum = info.afgNum;
        this.afbNum = info.afbNum;
        this.trffic = info.trffic;
        this.inKQINum = info.inKQINum;
        this.overKQINum = info.overKQINum;
        this.closeCellNum = info.closeCellNum;
        this.closeWiFiNum = info.closeWiFiNum;
        this.startHicNum = info.startHicNum;
        this.hicsNum = info.hicsNum;
    }

    public void printfInfo() {
        HwAPPQoEUtils.logD("HwAPPChrStatisInfo appId = " + this.appId + " scenceId = " + this.scenceId + " wifiStartNum = " + this.wifiStartNum + " cellStartNum = " + this.cellStartNum + " wifiStallNum = " + this.wifiStallNum + " cellStallNum = " + this.cellStallNum + " wifispNum = " + this.wifispNum + " cellspNum = " + this.cellspNum + " rn1Num = " + this.rn1Num + " rn2Num = " + this.rn2Num + " rn3Num = " + this.rn3Num + " rn4Num = " + this.rn4Num + " chfNum = " + this.chfNum + " mpfNum = " + this.mpfNum + " mpsNum = " + this.mpsNum + " afgNum = " + this.afgNum + " afbNum = " + this.afbNum + " trffic = " + this.trffic + " inKQINum = " + this.inKQINum + " overKQINum = " + this.overKQINum + " closeCellNum = " + this.closeCellNum + " closeWiFiNum = " + this.closeWiFiNum + " startHicNum = " + this.startHicNum + " hicsNum = " + this.hicsNum);
    }
}
