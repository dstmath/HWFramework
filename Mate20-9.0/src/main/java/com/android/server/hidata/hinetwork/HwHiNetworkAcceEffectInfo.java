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
    public String apk = "";
    public short avgRtt = -1;
    public short boneFlag = -1;
    public short code = 255;
    public short dataUsg = -1;
    public short dualCHFlag = -1;
    public short gain = -1;
    public short lagCnt = -1;
    public short linkTime = -1;
    public short net = -1;
    public short pktLoss = -1;
    public short qosFlag = -1;

    public void reset() {
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
