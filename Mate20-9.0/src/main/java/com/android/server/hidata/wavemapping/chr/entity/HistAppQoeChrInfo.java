package com.android.server.hidata.wavemapping.chr.entity;

public class HistAppQoeChrInfo extends ChrInfo {
    public int AppName = 0;
    public int dataRx = 0;
    public int dataTx = 0;
    public int duration = 0;
    public int goodCnt = 0;
    public int modelVer_all = 0;
    public int modelVer_cell = 0;
    public int modelVer_main = 0;
    public short netFreq = 0;
    public int netIdCnt = 0;
    public String netName = "UNKNOWN";
    public byte netType = 0;
    public int poorCnt = 0;
    public short recordDays = 0;
    public short spaceId_all = 0;
    public short spaceId_cell = 0;
    public short spaceId_main = 0;

    public void setSpaceInfo(short spaceId_a, int modelVer_a, short spaceId_m, int modelVer_m, short spaceId_c, int modelVer_c) {
        this.spaceId_all = spaceId_a;
        this.modelVer_all = modelVer_a;
        this.spaceId_main = spaceId_m;
        this.modelVer_main = modelVer_m;
        this.spaceId_cell = spaceId_c;
        this.modelVer_cell = modelVer_c;
    }

    public void setNetInfo(int netId, String name, short freq, byte type) {
        this.netIdCnt = netId;
        this.netName = name;
        this.netFreq = freq;
        this.netType = type;
    }

    public void setRecords(short days, int dur, int good, int poor, int rx, int tx) {
        this.recordDays = days;
        this.duration = dur;
        this.goodCnt = good;
        this.poorCnt = poor;
        this.dataRx = rx;
        this.dataTx = tx;
    }

    public void setAppName(int app) {
        this.AppName = app;
    }

    public String toString() {
        return "HistAppQoeChrInfo{spaceId_all=" + this.spaceId_all + ", modelVer_all=" + this.modelVer_all + ", spaceId_main=" + this.spaceId_main + ", modelVer_main=" + this.modelVer_main + ", spaceId_cell=" + this.spaceId_cell + ", modelVer_cell=" + this.modelVer_cell + ", netIdCnt=" + this.netIdCnt + ", netName=" + this.netName + ", netFreq=" + this.netFreq + ", netType=" + this.netType + ", recordDays=" + this.recordDays + ", duration=" + this.duration + ", goodCnt=" + this.goodCnt + ", poorCnt=" + this.poorCnt + ", dataRx=" + this.dataRx + ", dataTx=" + this.dataTx + '}';
    }
}
