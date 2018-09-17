package com.android.server.wifi;

public class HwWifiDFTAPKAction {
    public String mAPSsid;
    public int mApkAction;
    public int mApkChangeCnt;
    public String mApkName;
    public int mcloseScanCnt;
    public int mclosewifiCnt;
    public int mdisableNetworkCnt;
    public int mdisconnectCnt;
    public int mforeGroundScanCnt;
    public int mopenwifiCnt;
    public int mreassocCnt;
    public int mreconnectCnt;
    public int mscanFreqL1Cnt;
    public int mscanFreqL2Cnt;
    public int mscanFreqL3Cnt;
    public int mscreenOnScanCnt;
    public int mtotalScanCnt;
    public int mtriggerscanCnt;
    public int mwifiScanModeCnt;

    public HwWifiDFTAPKAction() {
        resetRecord();
    }

    public boolean cmp(String APKName) {
        if (APKName == null || this.mApkName == null) {
            return false;
        }
        return this.mApkName.equals(APKName);
    }

    public void resetRecord() {
        this.mApkAction = 0;
        this.mApkChangeCnt = 0;
        this.mAPSsid = "";
        this.mApkName = "";
        this.mclosewifiCnt = 0;
        this.mopenwifiCnt = 0;
        this.mreconnectCnt = 0;
        this.mreassocCnt = 0;
        this.mdisconnectCnt = 0;
        this.mtriggerscanCnt = 0;
        this.mwifiScanModeCnt = 0;
        this.mdisableNetworkCnt = 0;
        this.mscreenOnScanCnt = 0;
        this.mforeGroundScanCnt = 0;
        this.mtotalScanCnt = 0;
        this.mscanFreqL1Cnt = 0;
        this.mscanFreqL2Cnt = 0;
        this.mscanFreqL3Cnt = 0;
        this.mcloseScanCnt = 0;
    }
}
