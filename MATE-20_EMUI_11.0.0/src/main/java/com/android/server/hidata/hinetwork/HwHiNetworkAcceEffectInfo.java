package com.android.server.hidata.hinetwork;

public class HwHiNetworkAcceEffectInfo {
    public static final String APK = "apk";
    public static final String AVGRTT = "avgRtt";
    public static final String BONEFLAG = "boneFlag";
    public static final String CODE = "code";
    public static final String DATAUSG = "dataUsg";
    public static final String DUALCHFLAG = "dualCHFlag";
    public static final String GAIN = "gain";
    public static final String LAGCNT = "lagCnt";
    public static final String LINKTIME = "linkTime";
    public static final String NET = "net";
    public static final String PKRLOSS = "pktLoss";
    public static final String QOSFLAG = "QoSFlag4G";
    public String apk;
    public short avgRtt;
    public short boneFlag;
    public short code;
    public short dataUsg;
    public short dualCHFlag;
    public short gain;
    public short lagCnt;
    public short linkTime;
    public short net;
    public short pktLoss;
    public short qosFlag;

    public HwHiNetworkAcceEffectInfo() {
        init();
    }

    public void reset() {
        init();
    }

    private void init() {
        this.apk = "";
        this.gain = -1;
        this.avgRtt = -1;
        this.pktLoss = -1;
        this.net = -1;
        this.lagCnt = -1;
        this.dualCHFlag = -1;
        this.qosFlag = -1;
        this.boneFlag = -1;
        this.dataUsg = -1;
        this.linkTime = -1;
        this.code = 255;
    }
}
