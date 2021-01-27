package com.android.server.hidata.wavemapping.entity;

import android.os.SystemProperties;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class ParameterInfo {
    private static final String CHIP_PLATFORM_VALUE = SystemProperties.get("ro.board.platform", "UNDEFINED");
    private static final float DEFAULT_ABNORMAL_MAC_RATIO_ALLOW_UPD = 0.5f;
    private static final int DEFAULT_ACCUMULATE_COUNT = 10;
    private static final int DEFAULT_ACTIVE_SAMPLE = 60000;
    private static final int DEFAULT_APP_THRESHOLD_DURATION_MIN = 120000;
    private static final int DEFAULT_APP_THRESHOLD_POOR_CNT_MIN = 5;
    private static final float DEFAULT_APP_THRESHOLD_TARGET_RATION_MARGIN = 0.5f;
    private static final int DEFAULT_BATCH_INTERVAL = 3000;
    private static final int DEFAULT_BATCH_NUMBER = 20;
    private static final int DEFAULT_BSSID_START = 4;
    private static final int DEFAULT_CAPACITY = 10;
    private static final int DEFAULT_CELL_ID_START = 5;
    private static final int DEFAULT_CHECK_AGING_ACCUMULATE_COUNT = 2000000000;
    private static final int DEFAULT_CLUSTER_NUMBER = 7;
    private static final float DEFAULT_CONNECTED_STRENGTH = 0.2f;
    private static final int DEFAULT_DELETE_FLAG = 1;
    private static final double DEFAULT_DURATION_4G_RATIO = 0.5d;
    private static final int DEFAULT_DURATION_MIN = 300000;
    private static final int DEFAULT_GET_PSREG_STATUS = 30000;
    private static final int DEFAULT_KNN_MAX_DIST = 150;
    private static final float DEFAULT_KNN_SHARE_MAC_RATIO = 0.7f;
    private static final int DEFAULT_LABEL_ID = 1;
    private static final int DEFAULT_LIMIT_BG_SCAN_CNT = 40;
    private static final int DEFAULT_LIMIT_SCAN_INTERVAL_TIME = 25000;
    private static final int DEFAULT_LIMIT_STALL_SCAN_CNT = 80;
    private static final int DEFAULT_LIMIT_TOTAL_SCAN_CNT = 100;
    private static final int DEFAULT_LINKSPEED_ID = 3;
    private static final int DEFAULT_LOWER_BOUND = -70;
    private static final int DEFAULT_MAX_BSSID_NUMBER = 3000;
    private static final float DEFAULT_MAX_DIST = 150.0f;
    private static final float DEFAULT_MAX_DIST_BAK = 150.0f;
    private static final float DEFAULT_MAX_DIST_DECAY_RATIO = 0.8f;
    private static final float DEFAULT_MAX_DIST_MIN_LIMIT = 100.0f;
    private static final float DEFAULT_MAX_SHATTER_RATIO = 0.8f;
    private static final int DEFAULT_MIN_BSSID_COUNT = 3;
    private static final float DEFAULT_MIN_DISTINCT_VALUE = 5.0f;
    private static final int DEFAULT_MIN_FREQ = 1;
    private static final float DEFAULT_MIN_MAIN_AP_OCC_UPD = 0.3f;
    private static final int DEFAULT_MIN_MEAN_RSSI = -80;
    private static final int DEFAULT_MIN_MODEL_TYPE = 2;
    private static final float DEFAULT_MIN_NON_ZERO_PERCT = 0.2f;
    private static final float DEFAULT_MIN_SAMPLE_RATIO = 0.0f;
    private static final float DEFAULT_MIN_SEDEV = 2.0f;
    private static final int DEFAULT_MIN_TRAIN_BSSID_LIST_LEN_UPD = 20;
    private static final float DEFAULT_MIN_UNKNOWN_RATIO = 0.25f;
    private static final float DEFAULT_MIN_UNKNOWN_RATIO_UPD = 0.1f;
    private static final int DEFAULT_MOBILE_AP_CHECK_LIMIT = 5;
    private static final int DEFAULT_NEIGHBOR_NUMBER = 3;
    private static final int DEFAULT_OUT4G_INTERVAL = 15000;
    private static final int[] DEFAULT_PARAM_PERIOD_BG_SCAN = {30000, 30000};
    private static final int[] DEFAULT_PARAM_PERIOD_OUT4G_SCAN = {30000, 30000, 60000};
    private static final int[] DEFAULT_PARAM_PERIOD_STALL_SCAN = {20000, 30000, 40000, 60000};
    private static final double DEFAULT_POWER_SAVE_GAP = 0.9d;
    private static final double DEFAULT_PREF_DURATION_RATIO = 0.8d;
    private static final double DEFAULT_PREF_DURATION_RATIO_LEAVE = 0.7d;
    private static final float DEFAULT_PREF_FREQ_RATIO = 0.8f;
    private static final int DEFAULT_REPR_MIN_OCC = 5;
    private static final int DEFAULT_RESTART_LIMIT = 5;
    private static final int DEFAULT_SAMPLE_SIZE = 3000;
    private static final int DEFAULT_SCAN_CH = 3;
    private static final int DEFAULT_SCAN_MAC = 1;
    private static final int DEFAULT_SCAN_RSSI = 2;
    private static final int DEFAULT_SCAN_WIFI_START = 20;
    private static final int DEFAULT_SERVING_WIFI_LINK_SPEED = 13;
    private static final int DEFAULT_SERVING_WIFI_MAC = 11;
    private static final int DEFAULT_SERVING_WIFI_RSSI = 14;
    private static final float DEFAULT_SHARE_RATIO_PARAM = 0.5f;
    private static final int DEFAULT_SIGNAL_MIN = -110;
    private static final long DEFAULT_START_DURATION = 3600000;
    private static final int DEFAULT_START_TIME = 10;
    private static final int DEFAULT_TEST_DATA_CNT = 100;
    private static final float DEFAULT_TEST_DATA_RATIO = 0.2f;
    private static final int DEFAULT_TEST_DATA_SIZE = 40;
    private static final int DEFAULT_THRESHOLD = -80;
    private static final int DEFAULT_TIMESTAMP = 3;
    private static final int DEFAULT_TIMESTAMP_ID = 2;
    private static final int DEFAULT_TIME_UNIT = 1000;
    private static final float DEFAULT_TOTAL_SHATTER_RATIO = 0.2f;
    private static final int DEFAULT_TRAIN_DATA_SIZE = 300;
    private static final int DEFAULT_TRAIN_MAX_BATCH_COUNT = 500;
    private static final int DEFAULT_TRAIN_MAX_RAW_DATA_COUNT = 5000;
    private static final String DEFAULT_VALUE = "3";
    private static final float DEFAULT_WEIGHT_PARAM = 0.3f;
    private static final long DEFAULT_WRITE_FILE_SAMPLE = 3000;
    private static final String KEY_ACTIVE_SAMPLE = "activeSample=";
    private static final String KEY_BATCH_ID = "batchID=";
    private static final String KEY_BSSID_START = "bssidStart=";
    private static final String KEY_CELL_ID_START = "cellIDStart=";
    private static final String KEY_CLUSTER_NUMBER = "nClusters=";
    private static final String KEY_CONNECTED_STRENGTH = "connectedStrength=";
    private static final String KEY_DELETE_FLAG = "deleteFlag=";
    private static final String KEY_FG_FINGER_NUM = "fg_fingers_num=";
    private static final String KEY_FORMAT_PATTEN = "####.##";
    private static final String KEY_IS_MAIN_AP = "isMainAp=";
    private static final String KEY_KNN_MAX_DIST = "knnMaxDist=";
    private static final String KEY_KNN_SHARE_MAC_RATIO = "knnShareMacRatio=";
    private static final String KEY_LABEL_ID = "labelID=";
    private static final String KEY_LINK_SPEED_ID = "linkSpeedID=";
    private static final String KEY_LOWER_BOUND = "lowerBound=";
    private static final String KEY_MAX_BSSID_NUM = "maxBssidNum=";
    private static final String KEY_MAX_DIST = "maxDist=";
    private static final String KEY_MIN_BSSID_COUNT = "minBssidCount=";
    private static final String KEY_MIN_DISTINCT_VALUE = "minDistinctValue=";
    private static final String KEY_MIN_FREQ = "minFreq=";
    private static final String KEY_MIN_MEAN_RSSI = "minMeanRssi=";
    private static final String KEY_MIN_NONE_ZERO_PERCT = "minNonzeroPerct=";
    private static final String KEY_MIN_SAMPLE_RATIO = "minSampleRatio=";
    private static final String KEY_MIN_STDEV = "minStdev=";
    private static final String KEY_REPR_MIN_OCC = "reprMinOcc=";
    private static final String KEY_SHARE_RATIO_PARAM = "shareRatioParam=";
    private static final String KEY_THRESHOLD = "threshold=";
    private static final String KEY_TIMESTAMP_ID = "timestampID=";
    private static final String KEY_WEIGHT_PARAM = "weightParam=";
    private static final String LOG_SYS_INFO = SystemProperties.get("ro.logsystem.usertype", "0");
    private static final float MAIN_AP_ABNORMAL_MAC_RATIO_ALLOW_UPD = 0.8f;
    private static final int MAIN_AP_ACCUMULATE_COUNT = 5;
    private static final int MAIN_AP_BATCH_NUMBER = 100;
    private static final int MAIN_AP_CHECK_AGING_ACCUMULATE_COUNT = 800;
    private static final int MAIN_AP_KNN_MAX_DIST = 100;
    private static final float MAIN_AP_KNN_SHARE_MAC_RATIO = 1.0f;
    private static final int MAIN_AP_MAX_BSSID_NUMBER = 1000;
    private static final float MAIN_AP_MAX_DIST = 25.0f;
    private static final float MAIN_AP_MAX_DIST_MIN_LIMIT = 25.0f;
    private static final float MAIN_AP_MAX_SHATTER_RATIO = 0.5f;
    private static final int MAIN_AP_MIN_FREQ = 5;
    private static final float MAIN_AP_MIN_SEDEV = 5.0f;
    private static final float MAIN_AP_MIN_UNKNOWN_RATIO = 0.8f;
    private static final float MAIN_AP_MIN_UNKNOWN_RATIO_UPD = 0.8f;
    private static final int MAIN_AP_SCAN_WIFI_START = 10;
    private static final float MAIN_AP_SHARE_RATIO_PARAM = 0.99f;
    private static final int MAIN_AP_TRAIN_DATA_SIZE = 200;
    private static final int NEW_ACCUMULATE_COUNT = 5;
    private static final int NEW_CHECK_AGING_ACCUMULATE_COUNT = 500;
    private static final int NEW_KNN_MAX_DIST = 170;
    private static final float NEW_KNN_SHARE_MAC_RATIO = 0.66f;
    private static final int NEW_LOWER_BOUND = -100;
    private static final int NEW_MIN_FREQ = 5;
    private static final float NEW_MIN_UNKNOWN_RATIO = 0.6f;
    private static final float NEW_MIN_UNKNOWN_RATIO_UPD = 0.7f;
    private static final int NEW_NEIGHBOR_NUMBER = 100;
    private static final int NEW_REPR_MIN_OCC = 1;
    private static final float NEW_SHARE_RATIO_PARAM = 0.7f;
    private static final int NEW_TRAIN_DATA_SIZE = 200;
    private float abnMacRatioAllowUpd = 0.5f;
    private int accumulateCount = 10;
    private int activeSample = 60000;
    private int appThresholdDurationMin = DEFAULT_APP_THRESHOLD_DURATION_MIN;
    private int appThresholdGoodCntMin = 0;
    private int appThresholdPoorCntMin = 5;
    private float appThresholdTargetRationMargin = 0.5f;
    private boolean back4gEnabled = true;
    private double back4gThresholdDuration4gRatio = DEFAULT_DURATION_4G_RATIO;
    private int back4gThresholdDurationMin = DEFAULT_DURATION_MIN;
    private int back4gThresholdOut4gInterval = 15000;
    private int back4gThresholdRestartLimit = 5;
    private int back4gThresholdSignalMin = -110;
    private int batchId = 0;
    private int batchInterval = 3000;
    private int bssidStart = 4;
    private int cellIdStart = 5;
    private int checkAgingAccumulateCount = DEFAULT_CHECK_AGING_ACCUMULATE_COUNT;
    private int clusterNum = 7;
    private int configVer = 0;
    private float connectedStrength = 0.2f;
    private int deleteFlag = 1;
    private int fgBatchNum = 20;
    private String forceTesting = "";
    private boolean isMainAp = false;
    private boolean isTest01 = false;
    private int knnMaxDist = 150;
    private float knnShareMacRatio = 0.7f;
    private int labelId = 1;
    private int limitBgScanCnt = 40;
    private int limitScanIntervalTime = DEFAULT_LIMIT_SCAN_INTERVAL_TIME;
    private int limitStallScanCnt = 80;
    private int limitTotalScanCnt = 100;
    private int linkSpeedId = 3;
    private int lowerBound = -70;
    private int mMaxRawDataCount = 0;
    private int mMaxTrainBatchCount = 0;
    private int maxBssidNum = 3000;
    private float maxDist = 150.0f;
    private float maxDistBak = 150.0f;
    private float maxDistDecayRatio = 0.8f;
    private float maxDistMinLimit = DEFAULT_MAX_DIST_MIN_LIMIT;
    private float maxShatterRatio = 0.8f;
    private int minBssidCount = 3;
    private float minDistinctValue = 5.0f;
    private int minFreq = 1;
    private float minMainApOccUpd = 0.3f;
    private int minMeanRssi = -80;
    private int minModelTypes = 2;
    private float minNonzeroPerct = 0.2f;
    private float minSampleRatio = 0.0f;
    private float minStdev = 2.0f;
    private int minTrainBssiLstLenUpd = 20;
    private float minUnknownRatio = DEFAULT_MIN_UNKNOWN_RATIO;
    private float minUnknownRatioUpd = DEFAULT_MIN_UNKNOWN_RATIO_UPD;
    private int mobileApCheckLimit = 5;
    private int neighborNum = 3;
    private int[] paraPeriodBgScan = DEFAULT_PARAM_PERIOD_BG_SCAN;
    private int[] paraPeriodOut4gScan = DEFAULT_PARAM_PERIOD_OUT4G_SCAN;
    private int[] paraPeriodStallScan = DEFAULT_PARAM_PERIOD_STALL_SCAN;
    private boolean powerPreferEnabled = true;
    private double powerSaveGap = DEFAULT_POWER_SAVE_GAP;
    private int powerSaveType = 0;
    private double prefDurationRatio = DEFAULT_PREF_DURATION_RATIO;
    private double prefDurationRatioLeave = DEFAULT_PREF_DURATION_RATIO_LEAVE;
    private float prefFreqRatio = 0.8f;
    private int reGetPsRegStatus = 30000;
    private int reprMinOcc = 5;
    private int scanCh = 3;
    private int scanMac = 1;
    private int scanRssi = 2;
    private int scanSsid = 0;
    private int scanWifiStart = 20;
    private int servingWiFiLinkSpeed = 13;
    private int servingWiFiMac = 11;
    private int servingWiFiRssi = 14;
    private float shareRatioParam = 0.5f;
    private long startDuration = 3600000;
    private int startTimes = 10;
    private int testDataCnt = 100;
    private float testDataRatio = 0.2f;
    private int testDataSize = 40;
    private int threshold = -80;
    private int timestamp = 3;
    private int timestampId = 2;
    private float totalShatterRatio = 0.2f;
    private int trainDatasSize = 300;
    private boolean userPreferEnabled = true;
    private float weightParam = 0.3f;
    public int wifiDataSample = 3000;
    private String wifiSeperator = AwarenessInnerConstants.SEMI_COLON_KEY;
    private long writeFileSample = DEFAULT_WRITE_FILE_SAMPLE;

    public ParameterInfo(boolean isMainAp2) {
        if (isMainAp2) {
            setMainApParams();
        } else {
            this.configVer = 0;
            this.scanWifiStart = 20;
            this.maxDist = 150.0f;
            this.weightParam = 0.3f;
            this.shareRatioParam = 0.7f;
            this.connectedStrength = 0.2f;
            this.minFreq = 5;
            this.knnMaxDist = NEW_KNN_MAX_DIST;
            this.knnShareMacRatio = NEW_KNN_SHARE_MAC_RATIO;
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
            this.minUnknownRatio = 0.6f;
            this.testDataCnt = 100;
            this.maxShatterRatio = 0.8f;
            this.totalShatterRatio = 0.2f;
            this.maxDistDecayRatio = 0.8f;
            this.accumulateCount = 5;
            this.maxDistMinLimit = DEFAULT_MAX_DIST_MIN_LIMIT;
            this.checkAgingAccumulateCount = 500;
            this.minMainApOccUpd = 0.3f;
            this.minUnknownRatioUpd = 0.7f;
            this.minTrainBssiLstLenUpd = 20;
            this.abnMacRatioAllowUpd = 0.5f;
            this.fgBatchNum = 100;
            this.isMainAp = false;
            this.maxBssidNum = 1000;
        }
        this.mMaxTrainBatchCount = 500;
        this.mMaxRawDataCount = 5000;
    }

    private void setMainApParams() {
        this.configVer = 0;
        this.scanWifiStart = 10;
        this.maxDist = 25.0f;
        this.weightParam = 0.3f;
        this.shareRatioParam = MAIN_AP_SHARE_RATIO_PARAM;
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
        this.minUnknownRatio = 0.8f;
        this.testDataCnt = 100;
        this.maxShatterRatio = 0.5f;
        this.totalShatterRatio = 0.2f;
        this.maxDistDecayRatio = 0.8f;
        this.accumulateCount = 5;
        this.maxDistMinLimit = 25.0f;
        this.checkAgingAccumulateCount = 800;
        this.minMainApOccUpd = 0.3f;
        this.minUnknownRatioUpd = 0.8f;
        this.minTrainBssiLstLenUpd = 20;
        this.abnMacRatioAllowUpd = 0.8f;
        this.fgBatchNum = 100;
        this.activeSample = 60000;
        this.isMainAp = true;
        this.maxBssidNum = 1000;
    }

    public int getMaxTrainBatchCount() {
        return this.mMaxTrainBatchCount;
    }

    public void setMaxTrainBatchCount(int maxTrainBatchCount) {
        this.mMaxTrainBatchCount = maxTrainBatchCount;
    }

    public int getMaxRawDataCount() {
        return this.mMaxRawDataCount;
    }

    public void setMaxRawDataCount(int maxRawDataCount) {
        this.mMaxRawDataCount = maxRawDataCount;
    }

    public int getConfigVer() {
        return this.configVer;
    }

    public void setConfigVer(int configVer2) {
        this.configVer = configVer2;
    }

    public boolean isBetaUser() {
        if (LOG_SYS_INFO.equals("3")) {
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

    public String getWifiSeperator() {
        return this.wifiSeperator;
    }

    public void setWifiSeperator(String wifiSeperator2) {
        this.wifiSeperator = wifiSeperator2;
    }

    public int getMinModelTypes() {
        return this.minModelTypes;
    }

    public void setMinModelTypes(int minModelTypes2) {
        this.minModelTypes = minModelTypes2;
    }

    public float getMinMainApOccUpd() {
        return this.minMainApOccUpd;
    }

    public void setMinMainApOccUpd(float minMainApOccUpd2) {
        this.minMainApOccUpd = minMainApOccUpd2;
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

    public int getCheckAgingAccumulateCount() {
        return this.checkAgingAccumulateCount;
    }

    public void setCheckAgingAccumulateCount(int checkAgingAccumulateCount2) {
        this.checkAgingAccumulateCount = checkAgingAccumulateCount2;
    }

    public float getMinUnknownRatioUpd() {
        return this.minUnknownRatioUpd;
    }

    public void setMinUnknownRatioUpd(float minUnknownRatioUpd2) {
        this.minUnknownRatioUpd = minUnknownRatioUpd2;
    }

    public int getAccumulateCount() {
        return this.accumulateCount;
    }

    public void setAccumulateCount(int accumulateCount2) {
        this.accumulateCount = accumulateCount2;
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

    public float getMinUnknownRatio() {
        return this.minUnknownRatio;
    }

    public void setMinUnknownRatio(float minUnknownRatio2) {
        this.minUnknownRatio = minUnknownRatio2;
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

    public int getServingWiFiMac() {
        return this.servingWiFiMac;
    }

    public void setServingWiFiMac(int servingWiFiMac2) {
        this.servingWiFiMac = servingWiFiMac2;
    }

    public int getServingWiFiRssi() {
        return this.servingWiFiRssi;
    }

    public void setServingWiFiRssi(int servingWiFiRssi2) {
        this.servingWiFiRssi = servingWiFiRssi2;
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

    public int getScanSsid() {
        return this.scanSsid;
    }

    public void setScanSsid(int scanSsid2) {
        this.scanSsid = scanSsid2;
    }

    public int getScanMac() {
        return this.scanMac;
    }

    public void setScanMac(int scanMac2) {
        this.scanMac = scanMac2;
    }

    public int getScanRssi() {
        return this.scanRssi;
    }

    public void setScanRssi(int scanRssi2) {
        this.scanRssi = scanRssi2;
    }

    public int getScanCh() {
        return this.scanCh;
    }

    public void setScanCh(int scanCh2) {
        this.scanCh = scanCh2;
    }

    public int getWifiDataSample() {
        return this.wifiDataSample;
    }

    public void setWifiDataSample(int wifiDataSample2) {
        this.wifiDataSample = wifiDataSample2;
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

    public int getBatchId() {
        return this.batchId;
    }

    public void setBatchId(int batchId2) {
        this.batchId = batchId2;
    }

    public int getLabelId() {
        return this.labelId;
    }

    public void setLabelId(int labelId2) {
        this.labelId = labelId2;
    }

    public int getTimestampId() {
        return this.timestampId;
    }

    public void setTimestampId(int timestampId2) {
        this.timestampId = timestampId2;
    }

    public int getLinkSpeedId() {
        return this.linkSpeedId;
    }

    public void setLinkSpeedId(int linkSpeedId2) {
        this.linkSpeedId = linkSpeedId2;
    }

    public int getBssidStart() {
        return this.bssidStart;
    }

    public void setBssidStart(int bssidStart2) {
        this.bssidStart = bssidStart2;
    }

    public int getCellIdStart() {
        return this.cellIdStart;
    }

    public void setCellIdStart(int cellIdStart2) {
        this.cellIdStart = cellIdStart2;
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

    public int getClusterNum() {
        return this.clusterNum;
    }

    public void setClusterNum(int clusterNum2) {
        this.clusterNum = clusterNum2;
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

    public double getUserPrefDurationRatioLeave() {
        return this.prefDurationRatioLeave;
    }

    public void setUserPrefDurationRatioLeave(double prefDurationRatio2) {
        this.prefDurationRatioLeave = prefDurationRatio2;
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
        ArrayList<String> arrParams = new ArrayList<>(10);
        DecimalFormat df = new DecimalFormat(KEY_FORMAT_PATTEN);
        arrParams.add(KEY_FG_FINGER_NUM + this.fgBatchNum);
        arrParams.add(KEY_ACTIVE_SAMPLE + this.activeSample);
        arrParams.add(KEY_BATCH_ID + this.batchId);
        arrParams.add(KEY_LABEL_ID + this.labelId);
        arrParams.add(KEY_TIMESTAMP_ID + this.timestampId);
        arrParams.add(KEY_LINK_SPEED_ID + this.linkSpeedId);
        arrParams.add(KEY_BSSID_START + this.bssidStart);
        arrParams.add(KEY_CELL_ID_START + this.cellIdStart);
        arrParams.add(KEY_DELETE_FLAG + this.deleteFlag);
        arrParams.add(KEY_MIN_NONE_ZERO_PERCT + df.format((double) this.minNonzeroPerct));
        arrParams.add(KEY_MIN_DISTINCT_VALUE + df.format((double) this.minDistinctValue));
        arrParams.add(KEY_THRESHOLD + this.threshold);
        arrParams.add(KEY_MIN_BSSID_COUNT + this.minBssidCount);
        arrParams.add(KEY_MIN_MEAN_RSSI + this.minMeanRssi);
        arrParams.add(KEY_CLUSTER_NUMBER + this.clusterNum);
        arrParams.add(KEY_MAX_DIST + this.maxDist);
        arrParams.add(KEY_MIN_SAMPLE_RATIO + df.format((double) this.minSampleRatio));
        arrParams.add(KEY_WEIGHT_PARAM + df.format((double) this.weightParam));
        arrParams.add(KEY_SHARE_RATIO_PARAM + df.format((double) this.shareRatioParam));
        arrParams.add(KEY_MIN_STDEV + df.format((double) this.minStdev));
        arrParams.add(KEY_KNN_MAX_DIST + this.knnMaxDist);
        arrParams.add(KEY_KNN_SHARE_MAC_RATIO + df.format((double) this.knnShareMacRatio));
        arrParams.add(KEY_LOWER_BOUND + this.lowerBound);
        arrParams.add(KEY_REPR_MIN_OCC + this.reprMinOcc);
        arrParams.add(KEY_IS_MAIN_AP + this.isMainAp);
        arrParams.add(KEY_CONNECTED_STRENGTH + this.connectedStrength);
        arrParams.add(KEY_MIN_FREQ + this.minFreq);
        arrParams.add(KEY_MAX_BSSID_NUM + this.maxBssidNum);
        LogUtil.i(false, "ParameterInfo:%{public}d", Integer.valueOf(arrParams.size()));
        return (String[]) arrParams.toArray(new String[arrParams.size()]);
    }

    public int getActiveSample() {
        return this.activeSample;
    }

    public void setActiveSample(int activeSample2) {
        this.activeSample = activeSample2;
    }

    public int getFgBatchNum() {
        return this.fgBatchNum;
    }

    public void setFgBatchNum(int fgBatchNum2) {
        this.fgBatchNum = fgBatchNum2;
    }

    public void setActScanLimit(int bgScanCnt, int stallScanCnt, int totalScanCnt, int interval) {
        this.limitBgScanCnt = bgScanCnt;
        this.limitStallScanCnt = stallScanCnt;
        this.limitTotalScanCnt = totalScanCnt;
        this.limitScanIntervalTime = interval;
    }

    public int getActScanLimitBg() {
        return this.limitBgScanCnt;
    }

    public int getActScanLimitStall() {
        return this.limitStallScanCnt;
    }

    public int getActScanLimitTotal() {
        return this.limitTotalScanCnt;
    }

    public int getActScanLimitInterval() {
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

    public int getAppThresholdDurationMin() {
        return this.appThresholdDurationMin;
    }

    public int getAppThresholdPoorCntMin() {
        return this.appThresholdPoorCntMin;
    }

    public int getAppThresholdGoodCntMin() {
        return this.appThresholdGoodCntMin;
    }

    public float getAppThresholdTargetRationMargin() {
        return this.appThresholdTargetRationMargin;
    }

    public void setAppThresholdDurationMin(int duration) {
        this.appThresholdDurationMin = duration;
    }

    public void setAppThresholdPoorCntMin(int count) {
        this.appThresholdPoorCntMin = count;
    }

    public void setAppThresholdGoodCntMin(int count) {
        this.appThresholdGoodCntMin = count;
    }

    public void setAppThresholdTargetRationMargin(float margin) {
        this.appThresholdTargetRationMargin = margin;
    }

    public int getBack4gThresholdOut4gInterval() {
        return this.back4gThresholdOut4gInterval;
    }

    public int getBack4gThresholdRestartLimit() {
        return this.back4gThresholdRestartLimit;
    }

    public int getBack4gThresholdDurationMin() {
        return this.back4gThresholdDurationMin;
    }

    public int getBack4gThresholdSignalMin() {
        return this.back4gThresholdSignalMin;
    }

    public double getBack4gThresholdDuration4gRatio() {
        return this.back4gThresholdDuration4gRatio;
    }

    public void setBack4gThresholdOut4gInterval(int interval) {
        this.back4gThresholdOut4gInterval = interval;
    }

    public void setBack4gThresholdDurationMin(int duration) {
        this.back4gThresholdDurationMin = duration;
    }

    public void setBack4gThresholdSignalMin(int signal) {
        this.back4gThresholdSignalMin = signal;
    }

    public void setBack4gThresholdDuration4gRatio(double ratio) {
        this.back4gThresholdDuration4gRatio = ratio;
    }

    public boolean getBack4gEnabled() {
        if (CHIP_PLATFORM_VALUE.startsWith("kirin95") || CHIP_PLATFORM_VALUE.startsWith("kirin96") || CHIP_PLATFORM_VALUE.startsWith("kirin97") || CHIP_PLATFORM_VALUE.startsWith("kirin6") || CHIP_PLATFORM_VALUE.startsWith("hi") || CHIP_PLATFORM_VALUE.startsWith("sms")) {
            return false;
        }
        return this.back4gEnabled;
    }

    public boolean getUserPreferEnabled() {
        return this.userPreferEnabled;
    }

    public boolean getPowerPreferEnabled() {
        return this.powerPreferEnabled;
    }

    public void setBack4gEnable(boolean isEnabled) {
        this.back4gEnabled = isEnabled;
    }

    public void setUserPreferEnable(boolean isEnabled) {
        this.userPreferEnabled = isEnabled;
    }

    public void setPowerPreferEnable(boolean isEnabled) {
        this.powerPreferEnabled = isEnabled;
    }

    public int getReGetPsRegStatus() {
        return this.reGetPsRegStatus;
    }

    public String getChipPlatformValue() {
        return CHIP_PLATFORM_VALUE;
    }

    public String toString() {
        return "ParameterInfo{configVer=" + this.configVer + ", CHIP_PLATFORM_VALUE=" + CHIP_PLATFORM_VALUE + ", fg_fingers_num=" + this.fgBatchNum + ", activeSample=" + this.activeSample + ", batchInterval=" + this.batchInterval + ", deleteFlag=" + this.deleteFlag + ", minNonzeroPerct=" + this.minNonzeroPerct + ", minDistinctValue=" + this.minDistinctValue + ", minStdev=" + this.minStdev + ", threshold=" + this.threshold + ", minBssidCount=" + this.minBssidCount + ", minMeanRssi=" + this.minMeanRssi + ", clusterNum=" + this.clusterNum + ", maxDistBak=" + this.maxDistBak + ", maxDist=" + this.maxDist + ", maxDistMinLimit=" + this.maxDistMinLimit + ", trainDatasSize=" + this.trainDatasSize + ", minSampleRatio=" + this.minSampleRatio + ", weightParam=" + this.weightParam + ", shareRatioParam=" + this.shareRatioParam + ", neighborNum=" + this.neighborNum + ", knnMaxDist=" + this.knnMaxDist + ", knnShareMacRatio=" + this.knnShareMacRatio + ", writeFileSample=" + this.writeFileSample + ", lowerBound=" + this.lowerBound + ", reprMinOcc=" + this.reprMinOcc + ", isMainAp=" + this.isMainAp + ", paraPeriodBgScan=" + Arrays.toString(this.paraPeriodBgScan) + ", paraPeriodStallScan=" + Arrays.toString(this.paraPeriodStallScan) + ", limitBgScanCnt=" + this.limitBgScanCnt + ", limitStallScanCnt=" + this.limitStallScanCnt + ", limitTotalScanCnt=" + this.limitTotalScanCnt + ", limitScanIntervalTime=" + this.limitScanIntervalTime + ", wifiDataSample=" + this.wifiDataSample + ", mobileApCheckLimit=" + this.mobileApCheckLimit + ", servingWiFiRssi=" + this.servingWiFiRssi + ", forceTesting='" + this.forceTesting + "', startTimes=" + this.startTimes + ", startDuration=" + this.startDuration + ", appThresholdDurationMin=" + this.appThresholdDurationMin + ", appThresholdPoorCntMin=" + this.appThresholdPoorCntMin + ", appThresholdGoodCntMin=" + this.appThresholdGoodCntMin + ", appThresholdTargetRationMargin=" + this.appThresholdTargetRationMargin + ", minUnknownRatio=" + this.minUnknownRatio + ", testDataCnt=" + this.testDataCnt + ", maxShatterRatio=" + this.maxShatterRatio + ", totalShatterRatio=" + this.totalShatterRatio + ", maxDistDecayRatio=" + this.maxDistDecayRatio + ", minMainApOccUpd=" + this.minMainApOccUpd + ", accumulateCount=" + this.accumulateCount + ", minUnknownRatioUpd=" + this.minUnknownRatioUpd + ", minTrainBssiLstLenUpd=" + this.minTrainBssiLstLenUpd + ", abnMacRatioAllowUpd=" + this.abnMacRatioAllowUpd + ", checkAgingAccumulateCount=" + this.checkAgingAccumulateCount + ", minModelTypes=" + this.minModelTypes + '}';
    }
}
