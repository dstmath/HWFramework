package com.android.server.location;

public class HwGnssDftGnssDailyParam {
    public int mDftGpsErrorUploadCnt;
    public int mDftGpsRqCnt;
    public int mDftNetworkReqCnt;
    public int mDftNetworkTimeoutCnt;

    public HwGnssDftGnssDailyParam() {
        this.mDftGpsRqCnt = 0;
        this.mDftGpsErrorUploadCnt = 0;
        this.mDftNetworkReqCnt = 0;
        this.mDftNetworkTimeoutCnt = 0;
    }
}
