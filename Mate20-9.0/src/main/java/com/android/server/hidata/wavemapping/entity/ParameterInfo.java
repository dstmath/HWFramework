package com.android.server.hidata.wavemapping.entity;

import android.hardware.biometrics.fingerprint.V2_1.RequestStatus;
import android.os.SystemProperties;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class ParameterInfo {
    private static final String chipPlatformValue = SystemProperties.get("ro.board.platform", "UNDEFINED");
    private static final String logSysInfo = SystemProperties.get("ro.logsystem.usertype", "0");
    private static final String runMode = SystemProperties.get("ro.runmode", "normal");
    public int WIFI_DATA_SAMPLE = 3000;
    private float abnMacRatioAllowUpd = 0.5f;
    private int activeSample = 60000;
    private int acumlteCount = 10;
    private int appTH_Duration_min = AwareAppAssociate.ASSOC_DECAY_MIN_TIME;
    private int appTH_GoodCnt_min = 0;
    private int appTH_PoorCnt_min = 5;
    private float appTH_Target_Ration_margin = 0.2f;
    private boolean back4GEnabled = true;
    private double back4GTH_duration_4gRatio = 0.5d;
    private int back4GTH_duration_min = 300000;
    private int back4GTH_out4G_interval = Constant.SEND_STALL_MSG_DELAY;
    private int back4GTH_restart_limit = 5;
    private int back4GTH_signal_min = RequestStatus.SYS_ETIMEDOUT;
    private int batchID = 0;
    private int batchInterval = 3000;
    private int bssidStart = 4;
    private int cellIDStart = 5;
    private int checkAgingAcumlteCount = 2000000000;
    private int config_ver = 0;
    private float connectedStrength = 0.2f;
    private int deleteFlag = 1;
    private int fg_batch_num = 20;
    private String forceTesting = "";
    private boolean isMainAp = false;
    private boolean isTest01 = false;
    private int knnMaxDist = 150;
    private float knnShareMacRatio = 0.7f;
    private int labelID = 1;
    private int limitBgScanCnt = 40;
    private int limitScanIntervalTime = 25000;
    private int limitStallScanCnt = 80;
    private int limitTotalScanCnt = 100;
    private int linkSpeedID = 3;
    private int lowerBound = -70;
    private int maxBssidNum = 3000;
    private float maxDist = 150.0f;
    private float maxDistBak = 150.0f;
    private float maxDistDecayRatio = 0.8f;
    private float maxDistMinLimit = 100.0f;
    private int maxRawDataCnt = 5000;
    private float maxShatterRatio = 0.8f;
    private int maxTrainBatchCnt = 500;
    private int minBssidCount = 3;
    private float minDistinctValue = 5.0f;
    private int minFreq = 1;
    private float minMainAPOccUpd = 0.3f;
    private int minMeanRssi = -80;
    private int minModelTypes = 2;
    private float minNonzeroPerct = 0.2f;
    private float minSampleRatio = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float minStdev = 2.0f;
    private int minTrainBssiLstLenUpd = 20;
    private float minUkwnRatioUpd = 0.1f;
    private float minUnkwnRatio = 0.25f;
    private int mobileApCheckLimit = 5;
    private int nClusters = 7;
    private int neighborNum = 3;
    private int[] paraPeriodBgScan = {30000, 30000};
    private int[] paraPeriodOut4gScan = {30000, 30000, 60000};
    private int[] paraPeriodStallScan = {20000, 30000, 40000, 60000};
    private boolean powerPreferEnabled = true;
    private double powerSaveGap = 0.9d;
    private int powerSaveType = 0;
    private double prefDurationRatio = 0.8d;
    private double prefDurationRatio_leave = 0.7d;
    private float prefFreqRatio = 0.8f;
    private int reGetPsRegStatus = 30000;
    private int reprMinOcc = 5;
    private int scanCH = 3;
    private int scanMAC = 1;
    private int scanRSSI = 2;
    private int scanSSID = 0;
    private int scanWifiStart = 20;
    private int servingWiFiLinkSpeed = 13;
    private int servingWiFiMAC = 11;
    private int servingWiFiRSSI = 14;
    private float shareRatioParam = 0.5f;
    private long startDuration = 7200000;
    private int startTimes = 10;
    private int testDataCnt = 100;
    private float testDataRatio = 0.2f;
    private int testDataSize = 40;
    private int threshold = -80;
    private int timestamp = 3;
    private int timestampID = 2;
    private float totalShatterRatio = 0.2f;
    private int trainDatasSize = 300;
    private boolean userPreferEnabled = true;
    private float weightParam = 0.3f;
    private String wifiSeperate = CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER;
    private long writeFileSample = 3000;

    public ParameterInfo(boolean isMainAp2) {
        if (isMainAp2) {
            this.config_ver = 0;
            this.scanWifiStart = 10;
            this.maxDist = 25.0f;
            this.weightParam = 0.3f;
            this.shareRatioParam = 0.99f;
            this.connectedStrength = 0.2f;
            this.minFreq = 5;
            this.knnMaxDist = 100;
            this.knnShareMacRatio = 1.0f;
            this.neighborNum = 3;
            this.testDataRatio = 0.2f;
            this.trainDatasSize = 200;
            this.testDataSize = 40;
            this.deleteFlag = 1;
            this.minNonzeroPerct = 0.2f;
            this.minDistinctValue = 5.0f;
            this.minStdev = 5.0f;
            this.threshold = -80;
            this.minBssidCount = 3;
            this.minMeanRssi = -80;
            this.reprMinOcc = 5;
            this.lowerBound = -70;
            this.minUnkwnRatio = 0.8f;
            this.testDataCnt = 100;
            this.maxShatterRatio = 0.5f;
            this.totalShatterRatio = 0.2f;
            this.maxDistDecayRatio = 0.8f;
            this.acumlteCount = 5;
            this.maxDistMinLimit = 25.0f;
            this.checkAgingAcumlteCount = 800;
            this.minMainAPOccUpd = 0.3f;
            this.minUkwnRatioUpd = 0.8f;
            this.minTrainBssiLstLenUpd = 20;
            this.abnMacRatioAllowUpd = 0.8f;
            this.fg_batch_num = 100;
            this.activeSample = 60000;
            this.isMainAp = true;
            this.maxBssidNum = 2000;
            this.maxTrainBatchCnt = 500;
            return;
        }
        this.config_ver = 0;
        this.scanWifiStart = 20;
        this.maxDist = 150.0f;
        this.weightParam = 0.3f;
        this.shareRatioParam = 0.7f;
        this.connectedStrength = 0.2f;
        this.minFreq = 5;
        this.knnMaxDist = HwSecDiagnoseConstant.OEMINFO_ID_DEVICE_RENEW;
        this.knnShareMacRatio = 0.66f;
        this.neighborNum = 100;
        this.testDataRatio = 0.2f;
        this.trainDatasSize = 200;
        this.testDataSize = 40;
        this.deleteFlag = 1;
        this.minNonzeroPerct = 0.2f;
        this.minDistinctValue = 5.0f;
        this.minStdev = 2.0f;
        this.threshold = -80;
        this.minBssidCount = 3;
        this.minMeanRssi = -80;
        this.reprMinOcc = 1;
        this.lowerBound = -100;
        this.minUnkwnRatio = 0.6f;
        this.testDataCnt = 100;
        this.maxShatterRatio = 0.8f;
        this.totalShatterRatio = 0.2f;
        this.maxDistDecayRatio = 0.8f;
        this.acumlteCount = 5;
        this.maxDistMinLimit = 100.0f;
        this.checkAgingAcumlteCount = 500;
        this.minMainAPOccUpd = 0.3f;
        this.minUkwnRatioUpd = 0.7f;
        this.minTrainBssiLstLenUpd = 20;
        this.abnMacRatioAllowUpd = 0.5f;
        this.fg_batch_num = 100;
        this.isMainAp = false;
        this.maxBssidNum = 2000;
        this.maxTrainBatchCnt = 500;
    }

    public int getMaxTrainBatchCnt() {
        return this.maxTrainBatchCnt;
    }

    public void setMaxTrainBatchCnt(int maxTrainBatchCnt2) {
        this.maxTrainBatchCnt = maxTrainBatchCnt2;
    }

    public int getConfig_ver() {
        return this.config_ver;
    }

    public void setConfig_ver(int config_ver2) {
        this.config_ver = config_ver2;
    }

    public boolean isBetaUser() {
        if (logSysInfo.equals("3")) {
            return true;
        }
        return false;
    }

    public boolean isFactoryVer() {
        if (runMode.equals("factory")) {
            return true;
        }
        return false;
    }

    public int getTestDataSize() {
        return this.testDataSize;
    }

    public void setTestDataSize(int testDataSize2) {
        this.testDataSize = testDataSize2;
    }

    public float getTestDataRatio() {
        return this.testDataRatio;
    }

    public void setTestDataRatio(float testDataRatio2) {
        this.testDataRatio = testDataRatio2;
    }

    public int getMinFreq() {
        return this.minFreq;
    }

    public void setMinFreq(int minFreq2) {
        this.minFreq = minFreq2;
    }

    public float getConnectedStrength() {
        return this.connectedStrength;
    }

    public void setConnectedStrength(float connectedStrength2) {
        this.connectedStrength = connectedStrength2;
    }

    public int getMinTrainBssiLstLenUpd() {
        return this.minTrainBssiLstLenUpd;
    }

    public void setMinTrainBssiLstLenUpd(int minTrainBssiLstLenUpd2) {
        this.minTrainBssiLstLenUpd = minTrainBssiLstLenUpd2;
    }

    public float getAbnMacRatioAllowUpd() {
        return this.abnMacRatioAllowUpd;
    }

    public void setAbnMacRatioAllowUpd(float abnMacRatioAllowUpd2) {
        this.abnMacRatioAllowUpd = abnMacRatioAllowUpd2;
    }

    public boolean isTest01() {
        return this.isTest01;
    }

    public void setTest01(boolean test01) {
        this.isTest01 = test01;
    }

    public String getWifiSeperate() {
        return this.wifiSeperate;
    }

    public void setWifiSeperate(String wifiSeperate2) {
        this.wifiSeperate = wifiSeperate2;
    }

    public int getMinModelTypes() {
        return this.minModelTypes;
    }

    public void setMinModelTypes(int minModelTypes2) {
        this.minModelTypes = minModelTypes2;
    }

    public float getMinMainAPOccUpd() {
        return this.minMainAPOccUpd;
    }

    public void setMinMainAPOccUpd(float minMainAPOccUpd2) {
        this.minMainAPOccUpd = minMainAPOccUpd2;
    }

    public float getMaxDistMinLimit() {
        return this.maxDistMinLimit;
    }

    public void setMaxDistMinLimit(float maxDistMinLimit2) {
        this.maxDistMinLimit = maxDistMinLimit2;
    }

    public float getMaxDistBak() {
        return this.maxDistBak;
    }

    public void setMaxDistBak(float maxDistBak2) {
        this.maxDistBak = maxDistBak2;
    }

    public int getTrainDatasSize() {
        return this.trainDatasSize;
    }

    public void setTrainDatasSize(int trainDatasSize2) {
        this.trainDatasSize = trainDatasSize2;
    }

    public int getCheckAgingAcumlteCount() {
        return this.checkAgingAcumlteCount;
    }

    public void setCheckAgingAcumlteCount(int checkAgingAcumlteCount2) {
        this.checkAgingAcumlteCount = checkAgingAcumlteCount2;
    }

    public float getMinUkwnRatioUpd() {
        return this.minUkwnRatioUpd;
    }

    public void setMinUkwnRatioUpd(float minUkwnRatioUpd2) {
        this.minUkwnRatioUpd = minUkwnRatioUpd2;
    }

    public int getAcumlteCount() {
        return this.acumlteCount;
    }

    public void setAcumlteCount(int acumlteCount2) {
        this.acumlteCount = acumlteCount2;
    }

    public float getMaxDistDecayRatio() {
        return this.maxDistDecayRatio;
    }

    public void setMaxDistDecayRatio(float maxDistDecayRatio2) {
        this.maxDistDecayRatio = maxDistDecayRatio2;
    }

    public float getTotalShatterRatio() {
        return this.totalShatterRatio;
    }

    public void setTotalShatterRatio(float totalShatterRatio2) {
        this.totalShatterRatio = totalShatterRatio2;
    }

    public float getMinUnkwnRatio() {
        return this.minUnkwnRatio;
    }

    public void setMinUnkwnRatio(float minUnkwnRatio2) {
        this.minUnkwnRatio = minUnkwnRatio2;
    }

    public int getTestDataCnt() {
        return this.testDataCnt;
    }

    public void setTestDataCnt(int testDataCnt2) {
        this.testDataCnt = testDataCnt2;
    }

    public float getMaxShatterRatio() {
        return this.maxShatterRatio;
    }

    public void setMaxShatterRatio(float maxShatterRatio2) {
        this.maxShatterRatio = maxShatterRatio2;
    }

    public int getServingWiFiMAC() {
        return this.servingWiFiMAC;
    }

    public void setServingWiFiMAC(int servingWiFiMAC2) {
        this.servingWiFiMAC = servingWiFiMAC2;
    }

    public int getServingWiFiRSSI() {
        return this.servingWiFiRSSI;
    }

    public void setServingWiFiRSSI(int servingWiFiRSSI2) {
        this.servingWiFiRSSI = servingWiFiRSSI2;
    }

    public int getServingWiFiLinkSpeed() {
        return this.servingWiFiLinkSpeed;
    }

    public void setServingWiFiLinkSpeed(int servingWiFiLinkSpeed2) {
        this.servingWiFiLinkSpeed = servingWiFiLinkSpeed2;
    }

    public int getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(int timestamp2) {
        this.timestamp = timestamp2;
    }

    public int getScanWifiStart() {
        return this.scanWifiStart;
    }

    public void setScanWifiStart(int scanWifiStart2) {
        this.scanWifiStart = scanWifiStart2;
    }

    public int getScanSSID() {
        return this.scanSSID;
    }

    public void setScanSSID(int scanSSID2) {
        this.scanSSID = scanSSID2;
    }

    public int getScanMAC() {
        return this.scanMAC;
    }

    public void setScanMAC(int scanMAC2) {
        this.scanMAC = scanMAC2;
    }

    public int getScanRSSI() {
        return this.scanRSSI;
    }

    public void setScanRSSI(int scanRSSI2) {
        this.scanRSSI = scanRSSI2;
    }

    public int getScanCH() {
        return this.scanCH;
    }

    public void setScanCH(int scanCH2) {
        this.scanCH = scanCH2;
    }

    public int getWifiDataSample() {
        return this.WIFI_DATA_SAMPLE;
    }

    public void setWifiDataSample(int wifiDataSample) {
        this.WIFI_DATA_SAMPLE = wifiDataSample;
    }

    public boolean isMainAp() {
        return this.isMainAp;
    }

    public void setMainAp(boolean mainAp) {
        this.isMainAp = mainAp;
    }

    public int getLowerBound() {
        return this.lowerBound;
    }

    public void setLowerBound(int lowerBound2) {
        this.lowerBound = lowerBound2;
    }

    public int getReprMinOcc() {
        return this.reprMinOcc;
    }

    public void setReprMinOcc(int reprMinOcc2) {
        this.reprMinOcc = reprMinOcc2;
    }

    public long getWriteFileSample() {
        return this.writeFileSample;
    }

    public void setWriteFileSample(long writeFileSample2) {
        this.writeFileSample = writeFileSample2;
    }

    public int getBatchInterval() {
        return this.batchInterval;
    }

    public void setBatchInterval(int batchInterval2) {
        this.batchInterval = batchInterval2;
    }

    public int getBatchID() {
        return this.batchID;
    }

    public void setBatchID(int batchID2) {
        this.batchID = batchID2;
    }

    public int getLabelID() {
        return this.labelID;
    }

    public void setLabelID(int labelID2) {
        this.labelID = labelID2;
    }

    public int getTimestampID() {
        return this.timestampID;
    }

    public void setTimestampID(int timestampID2) {
        this.timestampID = timestampID2;
    }

    public int getLinkSpeedID() {
        return this.linkSpeedID;
    }

    public void setLinkSpeedID(int linkSpeedID2) {
        this.linkSpeedID = linkSpeedID2;
    }

    public int getBssidStart() {
        return this.bssidStart;
    }

    public void setBssidStart(int bssidStart2) {
        this.bssidStart = bssidStart2;
    }

    public int getCellIDStart() {
        return this.cellIDStart;
    }

    public void setCellIDStart(int cellIDStart2) {
        this.cellIDStart = cellIDStart2;
    }

    public int getDeleteFlag() {
        return this.deleteFlag;
    }

    public void setDeleteFlag(int deleteFlag2) {
        this.deleteFlag = deleteFlag2;
    }

    public float getMinNonzeroPerct() {
        return this.minNonzeroPerct;
    }

    public void setMinNonzeroPerct(float minNonzeroPerct2) {
        this.minNonzeroPerct = minNonzeroPerct2;
    }

    public float getMinDistinctValue() {
        return this.minDistinctValue;
    }

    public void setMinDistinctValue(float minDistinctValue2) {
        this.minDistinctValue = minDistinctValue2;
    }

    public float getMinStdev() {
        return this.minStdev;
    }

    public void setMinStdev(float minStdev2) {
        this.minStdev = minStdev2;
    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int threshold2) {
        this.threshold = threshold2;
    }

    public int getMinBssidCount() {
        return this.minBssidCount;
    }

    public void setMinBssidCount(int minBssidCount2) {
        this.minBssidCount = minBssidCount2;
    }

    public int getMinMeanRssi() {
        return this.minMeanRssi;
    }

    public void setMinMeanRssi(int minMeanRssi2) {
        this.minMeanRssi = minMeanRssi2;
    }

    public int getnClusters() {
        return this.nClusters;
    }

    public void setnClusters(int nClusters2) {
        this.nClusters = nClusters2;
    }

    public float getMaxDist() {
        return this.maxDist;
    }

    public void setMaxDist(float maxDist2) {
        this.maxDist = maxDist2;
    }

    public float getMinSampleRatio() {
        return this.minSampleRatio;
    }

    public void setMinSampleRatio(float minSampleRatio2) {
        this.minSampleRatio = minSampleRatio2;
    }

    public float getWeightParam() {
        return this.weightParam;
    }

    public void setWeightParam(float weightParam2) {
        this.weightParam = weightParam2;
    }

    public float getShareRatioParam() {
        return this.shareRatioParam;
    }

    public void setShareRatioParam(float shareRatioParam2) {
        this.shareRatioParam = shareRatioParam2;
    }

    public int getNeighborNum() {
        return this.neighborNum;
    }

    public void setNeighborNum(int neighborNum2) {
        this.neighborNum = neighborNum2;
    }

    public int getKnnMaxDist() {
        return this.knnMaxDist;
    }

    public void setKnnMaxDist(int knnMaxDist2) {
        this.knnMaxDist = knnMaxDist2;
    }

    public float getKnnShareMacRatio() {
        return this.knnShareMacRatio;
    }

    public void setKnnShareMacRatio(float knnShareMacRatio2) {
        this.knnShareMacRatio = knnShareMacRatio2;
    }

    public int getMobileApCheckLimit() {
        return this.mobileApCheckLimit;
    }

    public void setMobileApCheckLimit(int mobileApCheckLimit2) {
        this.mobileApCheckLimit = mobileApCheckLimit2;
    }

    public int getUserPrefStartTimes() {
        return this.startTimes;
    }

    public void setUserPrefStartTimes(int times) {
        this.startTimes = times;
    }

    public long getUserPrefStartDuration() {
        return this.startDuration;
    }

    public void setUserPrefStartDuration(long duration) {
        this.startDuration = 1000 * duration;
    }

    public float getUserPrefFreqRatio() {
        return this.prefFreqRatio;
    }

    public void setUserPrefFreqRatio(float prefFreqRatio2) {
        this.prefFreqRatio = prefFreqRatio2;
    }

    public double getUserPrefDurationRatio() {
        return this.prefDurationRatio;
    }

    public void setUserPrefDurationRatio(double prefDurationRatio2) {
        this.prefDurationRatio = prefDurationRatio2;
    }

    public double getUserPrefDurationRatio_Leave() {
        return this.prefDurationRatio_leave;
    }

    public void setUserPrefDurationRatio_Leave(double prefDurationRatio2) {
        this.prefDurationRatio_leave = prefDurationRatio2;
    }

    public int getPowerSaveType() {
        return this.powerSaveType;
    }

    public void setPowerSaveType(int powerSaveType2) {
        this.powerSaveType = powerSaveType2;
    }

    public double getPowerSaveGap() {
        return this.powerSaveGap;
    }

    public void setPowerSaveGap(double powerSaveGap2) {
        this.powerSaveGap = powerSaveGap2;
    }

    public int getMaxBssidNum() {
        return this.maxBssidNum;
    }

    public void setMaxBssidNum(int maxBssidNum2) {
        this.maxBssidNum = maxBssidNum2;
    }

    public String[] toLineStr() {
        ArrayList<String> arrParams = new ArrayList<>();
        new DecimalFormat("####.##");
        arrParams.add("fg_fingers_num=" + this.fg_batch_num);
        arrParams.add("activeSample=" + this.activeSample);
        arrParams.add("batchID=" + this.batchID);
        arrParams.add("labelID=" + this.labelID);
        arrParams.add("timestampID=" + this.timestampID);
        arrParams.add("linkSpeedID=" + this.linkSpeedID);
        arrParams.add("bssidStart=" + this.bssidStart);
        arrParams.add("cellIDStart=" + this.cellIDStart);
        arrParams.add("deleteFlag=" + this.deleteFlag);
        arrParams.add("minNonzeroPerct=" + df.format((double) this.minNonzeroPerct));
        arrParams.add("minDistinctValue=" + df.format((double) this.minDistinctValue));
        arrParams.add("threshold=" + this.threshold);
        arrParams.add("minBssidCount=" + this.minBssidCount);
        arrParams.add("minMeanRssi=" + this.minMeanRssi);
        arrParams.add("nClusters=" + this.nClusters);
        arrParams.add("maxDist=" + this.maxDist);
        arrParams.add("minSampleRatio=" + df.format((double) this.minSampleRatio));
        arrParams.add("weightParam=" + df.format((double) this.weightParam));
        arrParams.add("shareRatioParam=" + df.format((double) this.shareRatioParam));
        arrParams.add("minStdev=" + df.format((double) this.minStdev));
        arrParams.add("knnMaxDist=" + this.knnMaxDist);
        arrParams.add("knnShareMacRatio=" + df.format((double) this.knnShareMacRatio));
        arrParams.add("lowerBound=" + this.lowerBound);
        arrParams.add("reprMinOcc=" + this.reprMinOcc);
        arrParams.add("isMainAp=" + this.isMainAp);
        arrParams.add("connectedStrength=" + this.connectedStrength);
        arrParams.add("minFreq=" + this.minFreq);
        arrParams.add("maxBssidNum=" + this.maxBssidNum);
        LogUtil.i("ParameterInfo:" + arrParams.size());
        return (String[]) arrParams.toArray(new String[arrParams.size()]);
    }

    public int getActiveSample() {
        return this.activeSample;
    }

    public void setActiveSample(int activeSample2) {
        this.activeSample = activeSample2;
    }

    public int getFg_batch_num() {
        return this.fg_batch_num;
    }

    public void setFg_batch_num(int fg_batch_num2) {
        this.fg_batch_num = fg_batch_num2;
    }

    public void setActScanLimit(int BgScanCnt, int StallScanCnt, int TotalScanCnt, int Interval) {
        this.limitBgScanCnt = BgScanCnt;
        this.limitStallScanCnt = StallScanCnt;
        this.limitTotalScanCnt = TotalScanCnt;
        this.limitScanIntervalTime = Interval;
    }

    public int getActScanLimit_bg() {
        return this.limitBgScanCnt;
    }

    public int getActScanLimit_stall() {
        return this.limitStallScanCnt;
    }

    public int getActScanLimit_total() {
        return this.limitTotalScanCnt;
    }

    public int getActScanLimit_interval() {
        return this.limitScanIntervalTime;
    }

    public void setActScanPeriods(int[] bgPeriods, int[] stallPeriods, int[] out4gPeriods) {
        this.paraPeriodBgScan = (int[]) bgPeriods.clone();
        this.paraPeriodStallScan = (int[]) stallPeriods.clone();
        this.paraPeriodOut4gScan = (int[]) out4gPeriods.clone();
    }

    public int[] getActBgScanPeriods() {
        return (int[]) this.paraPeriodBgScan.clone();
    }

    public int[] getActStallScanPeriods() {
        return (int[]) this.paraPeriodStallScan.clone();
    }

    public int[] getActOut4gScanPeriods() {
        return (int[]) this.paraPeriodOut4gScan.clone();
    }

    public int getAppTH_duration_min() {
        return this.appTH_Duration_min;
    }

    public int getAppTH_poorCnt_min() {
        return this.appTH_PoorCnt_min;
    }

    public int getAppTH_goodCnt_min() {
        return this.appTH_GoodCnt_min;
    }

    public float getAppTH_Target_Ration_margin() {
        return this.appTH_Target_Ration_margin;
    }

    public void setAppTH_duration_min(int duration) {
        this.appTH_Duration_min = duration;
    }

    public void setAppTH_poorCnt_min(int count) {
        this.appTH_PoorCnt_min = count;
    }

    public void setAppTH_goodCnt_min(int count) {
        this.appTH_GoodCnt_min = count;
    }

    public void setAppTH_Target_Ration_margin(float margin) {
        this.appTH_Target_Ration_margin = margin;
    }

    public int getBack4GTH_out4G_interval() {
        return this.back4GTH_out4G_interval;
    }

    public int getBack4GTH_restart_limit() {
        return this.back4GTH_restart_limit;
    }

    public int getBack4GTH_duration_min() {
        return this.back4GTH_duration_min;
    }

    public int getBack4GTH_signal_min() {
        return this.back4GTH_signal_min;
    }

    public double getBack4GTH_duration_4gRatio() {
        return this.back4GTH_duration_4gRatio;
    }

    public int getMaxRawDataCnt() {
        return this.maxRawDataCnt;
    }

    public void setBack4GTH_out4G_interval(int interval) {
        this.back4GTH_out4G_interval = interval;
    }

    public void setBack4GTH_duration_min(int duration) {
        this.back4GTH_duration_min = duration;
    }

    public void setBack4GTH_signal_min(int signal) {
        this.back4GTH_signal_min = signal;
    }

    public void setBack4GTH_duration_4gRatio(double ratio) {
        this.back4GTH_duration_4gRatio = ratio;
    }

    public boolean getBack4GEnabled() {
        if (chipPlatformValue.startsWith("kirin95") || chipPlatformValue.startsWith("kirin96") || chipPlatformValue.startsWith("kirin97") || chipPlatformValue.startsWith("kirin6") || chipPlatformValue.startsWith("hi") || chipPlatformValue.startsWith("sms")) {
            return false;
        }
        return this.back4GEnabled;
    }

    public boolean getUserPreferEnabled() {
        return this.userPreferEnabled;
    }

    public boolean getPowerPreferEnabled() {
        return this.powerPreferEnabled;
    }

    public void setBack4GEnable(boolean enabled) {
        this.back4GEnabled = enabled;
    }

    public void setUserPreferEnable(boolean enabled) {
        this.userPreferEnabled = enabled;
    }

    public void setPowerPreferEnable(boolean enabled) {
        this.powerPreferEnabled = enabled;
    }

    public int getReGetPsRegStatus() {
        return this.reGetPsRegStatus;
    }

    public String getChipPlatformValue() {
        return chipPlatformValue;
    }

    public String toString() {
        return "ParameterInfo{config_ver=" + this.config_ver + ", chipPlatformValue=" + chipPlatformValue + ", fg_fingers_num=" + this.fg_batch_num + ", activeSample=" + this.activeSample + ", batchInterval=" + this.batchInterval + ", deleteFlag=" + this.deleteFlag + ", minNonzeroPerct=" + this.minNonzeroPerct + ", minDistinctValue=" + this.minDistinctValue + ", minStdev=" + this.minStdev + ", threshold=" + this.threshold + ", minBssidCount=" + this.minBssidCount + ", minMeanRssi=" + this.minMeanRssi + ", nClusters=" + this.nClusters + ", maxDistBak=" + this.maxDistBak + ", maxDist=" + this.maxDist + ", maxDistMinLimit=" + this.maxDistMinLimit + ", trainDatasSize=" + this.trainDatasSize + ", minSampleRatio=" + this.minSampleRatio + ", weightParam=" + this.weightParam + ", shareRatioParam=" + this.shareRatioParam + ", neighborNum=" + this.neighborNum + ", knnMaxDist=" + this.knnMaxDist + ", knnShareMacRatio=" + this.knnShareMacRatio + ", writeFileSample=" + this.writeFileSample + ", lowerBound=" + this.lowerBound + ", reprMinOcc=" + this.reprMinOcc + ", isMainAp=" + this.isMainAp + ", paraPeriodBgScan=" + Arrays.toString(this.paraPeriodBgScan) + ", paraPeriodStallScan=" + Arrays.toString(this.paraPeriodStallScan) + ", limitBgScanCnt=" + this.limitBgScanCnt + ", limitStallScanCnt=" + this.limitStallScanCnt + ", limitTotalScanCnt=" + this.limitTotalScanCnt + ", limitScanIntervalTime=" + this.limitScanIntervalTime + ", WIFI_DATA_SAMPLE=" + this.WIFI_DATA_SAMPLE + ", mobileApCheckLimit=" + this.mobileApCheckLimit + ", servingWiFiRSSI=" + this.servingWiFiRSSI + ", forceTesting='" + this.forceTesting + '\'' + ", startTimes=" + this.startTimes + ", startDuration=" + this.startDuration + ", appTH_Duration_min=" + this.appTH_Duration_min + ", appTH_PoorCnt_min=" + this.appTH_PoorCnt_min + ", appTH_GoodCnt_min=" + this.appTH_GoodCnt_min + ", appTH_Target_Ration_margin=" + this.appTH_Target_Ration_margin + ", minUnkwnRatio=" + this.minUnkwnRatio + ", testDataCnt=" + this.testDataCnt + ", maxShatterRatio=" + this.maxShatterRatio + ", totalShatterRatio=" + this.totalShatterRatio + ", maxDistDecayRatio=" + this.maxDistDecayRatio + ", minMainAPOccUpd=" + this.minMainAPOccUpd + ", acumlteCount=" + this.acumlteCount + ", minUkwnRatioUpd=" + this.minUkwnRatioUpd + ", minTrainBssiLstLenUpd=" + this.minTrainBssiLstLenUpd + ", abnMacRatioAllowUpd=" + this.abnMacRatioAllowUpd + ", checkAgingAcumlteCount=" + this.checkAgingAcumlteCount + ", minModelTypes=" + this.minModelTypes + '}';
    }
}
