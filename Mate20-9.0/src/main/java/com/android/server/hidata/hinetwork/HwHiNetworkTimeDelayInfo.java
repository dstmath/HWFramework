package com.android.server.hidata.hinetwork;

public class HwHiNetworkTimeDelayInfo {
    public static final String AFRACC = "aftAcc";
    public static final String APK = "apk";
    public static final String BEFACC = "befAcc";
    public static final String CELLACCRTT = "cellAccRtt";
    public static final String CELLBONRTT = "cellBonRtt";
    public static final String CELLTOTRTT = "cellTotRtt";
    public static final String NET = "net";
    public static final String WIFIACCRTT = "wifiAccRtt";
    public static final String WIFIBONRTT = "wifiBonRtt";
    public static final String WIFITOTRTT = "wifiTotRtt";
    public short aftAcc = -1;
    public String apk = "";
    public short befAcc = -1;
    public short cellAccRtt = -1;
    public short cellBonRtt = -1;
    public short cellTotRtt = -1;
    public short net = -1;
    public short wifiAccRtt = -1;
    public short wifiBonRtt = -1;
    public short wifiTotRtt = -1;

    public void reset() {
        this.apk = "";
        this.net = -1;
        this.cellAccRtt = -1;
        this.cellBonRtt = -1;
        this.cellTotRtt = -1;
        this.wifiAccRtt = -1;
        this.wifiBonRtt = -1;
        this.wifiTotRtt = -1;
        this.aftAcc = -1;
        this.befAcc = -1;
    }
}
