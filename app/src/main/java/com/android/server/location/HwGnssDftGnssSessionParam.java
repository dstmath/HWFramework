package com.android.server.location;

public class HwGnssDftGnssSessionParam {
    public long catchSvTime;
    public boolean isGpsdResart;
    public int lostPosCnt;
    public long startTime;
    public long stopTime;
    public int ttff;

    public HwGnssDftGnssSessionParam() {
        this.startTime = 0;
        this.ttff = 0;
        this.stopTime = 0;
        this.catchSvTime = 0;
        this.lostPosCnt = 0;
        this.isGpsdResart = false;
    }

    public void resetParam() {
        this.startTime = 0;
        this.ttff = 0;
        this.stopTime = 0;
        this.catchSvTime = 0;
        this.lostPosCnt = 0;
        this.isGpsdResart = false;
    }
}
