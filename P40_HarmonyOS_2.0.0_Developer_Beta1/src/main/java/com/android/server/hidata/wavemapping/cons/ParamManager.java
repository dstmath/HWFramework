package com.android.server.hidata.wavemapping.cons;

import android.util.Log;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.wavemapping.entity.MobileApCheckParamInfo;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.ShowToast;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ParamManager {
    private static final String KEY_ABNORMAL_MAC_RATIO = "abnMacRatioAllowUpd";
    private static final String KEY_ACCUMULATE_COUNT = "acumlteCount";
    private static final String KEY_ACTIVE_SAMPLE = "activeSample";
    private static final String KEY_BACK_4G_DURATION_MIN = "back4GDurationMin";
    private static final String KEY_BACK_4G_ENABLED = "back4GEnabled";
    private static final String KEY_BACK_4G_RATIO = "back4GRatio";
    private static final String KEY_BACK_4G_SIGNAL_MIN = "back4GSignalMin";
    private static final String KEY_BATCH_ID = "batchID";
    private static final String KEY_BATCH_NUMBER = "fg_batch_num";
    private static final String KEY_BSSID_START = "bssidStart";
    private static final String KEY_CELL_ID_START = "cellIDStart";
    private static final String KEY_CHECK_AGING_ACCUMULATE_COUNT = "checkAgingAcumlteCount";
    private static final String KEY_CLUSTER_NUMBER = "nClusters";
    private static final String KEY_CONFIG_VERSION = "config_ver";
    private static final String KEY_CONNECTED_STRENGTH = "connectedStrength";
    private static final String KEY_DELETE_FLAG = "deleteFlag";
    private static final String KEY_KNN_MAX_DIST = "knnMaxDist";
    private static final String KEY_KNN_SHARE_MAC_RATIO = "knnShareMacRatio";
    private static final String KEY_LOWER_BOUND = "lowerBound";
    private static final String KEY_MAX_BSSID_COUNT = "maxBssidNum";
    private static final String KEY_MAX_DIST = "maxDist";
    private static final String KEY_MAX_DIST_DECAY_RATIO = "MaxDistDecayRatio";
    private static final String KEY_MAX_DIST_MIN_LIMIT = "maxDistMinLimit";
    private static final String KEY_MAX_SHATTER_RATIO = "maxShatterRatio";
    private static final String KEY_MIN_BSSID_COUNT = "minBssidCount";
    private static final String KEY_MIN_DISTINCT_VALUE = "minDistinctValue";
    private static final String KEY_MIN_FREQ = "minFreq";
    private static final String KEY_MIN_MAIN_AP_OCCUPATION = "minMainAPOccUpd";
    private static final String KEY_MIN_MEAN_RSSI = "minMeanRssi";
    private static final String KEY_MIN_MODEL_TYPES = "minModelTypes";
    private static final String KEY_MIN_NON_ZERO_PERCT = "minNonzeroPerct";
    private static final String KEY_MIN_SAMPLE_RATIO = "minSampleRatio";
    private static final String KEY_MIN_STD = "minStdev";
    private static final String KEY_MIN_TRAIN_BSSID_LIST_LENGTH = "minTrainBssiLstLenUpd";
    private static final String KEY_MIN_UNKNOWN_RATIO = "minUnkwnRatio";
    private static final String KEY_MIN_UNKNOWN_RATIO_OCCUPATION = "minUkwnRatioUpd";
    private static final String KEY_NEIGHBOR_NUMBER = "neighborNum";
    private static final String KEY_OUT_4G_INTERVAL = "out4GInterval";
    private static final String KEY_POWER_PREFER_ENABLED = "powerPreferEnabled";
    private static final String KEY_POWER_SAVE_GAP = "PowerSaveGap";
    private static final String KEY_POWER_SAVE_TYPE = "PowerSaveType";
    private static final String KEY_REPR_MIN_OCCUPATION = "reprMinOcc";
    private static final String KEY_SCAN_CHANNEL = "ScanCH";
    private static final String KEY_SCAN_MAC = "ScanMAC";
    private static final String KEY_SCAN_RSSI = "ScanRSSI";
    private static final String KEY_SCAN_SSID = "ScanSSID";
    private static final String KEY_SCAN_WIFI_START = "ScanWifiStart";
    private static final String KEY_SERVING_WIFI_MAC = "ServingWiFiMAC";
    private static final String KEY_SERVING_WIFI_RSSI = "ServingWiFiRSSI";
    private static final String KEY_SHARE_RATIO_PARAM = "shareRatioParam";
    private static final String KEY_SHOW_LOG = "ShowLog";
    private static final String KEY_TEST_DATA_COUNT = "TestDataCnt";
    private static final String KEY_TEST_DATA_RATIO = "testDataRatio";
    private static final String KEY_TEST_DATA_SIZE = "TestDataSize";
    private static final String KEY_THRESHOLD = "threshold";
    private static final String KEY_TIMESTAMP = "Timestamp";
    private static final String KEY_TOTAL_SHETTER_RATIO = "totalShatterRatio";
    private static final String KEY_TRAIN_DATA_SIZE = "TrainDatasSize";
    private static final String KEY_USER_PREFER_DURATION_RATIO = "UserPrefDurationRatio";
    private static final String KEY_USER_PREFER_ENABLED = "userPreferEnabled";
    private static final String KEY_USER_PREFER_FREQ_RATIO = "UserPrefFreqRatio";
    private static final String KEY_USER_PREFER_START_DURATION = "UserPrefStartDuration";
    private static final String KEY_USER_PREFER_START_TIMES = "UserPrefStartTimes";
    private static final String KEY_WEIGHT_PARAM = "weightParam";
    private static final String KEY_WIFI_DATA_SAMPLE = "wifiDataSample";
    private static final String KEY_WRITE_FILE_SAMPLE = "writeFileSample";
    private static final String SYMBOL_SPACE = " ";
    private static final String TAG = ("WMapping." + ParamManager.class.getSimpleName());
    private static ParamManager instance = null;
    private ParameterInfo mainApParameterInfo;
    private MobileApCheckParamInfo mobileApCheckParamInfo = new MobileApCheckParamInfo();
    private ParameterInfo parameterInfo;

    private ParamManager() {
        Properties prop = getPropertiesFromFile(Constant.getWaveMappingConfig());
        this.parameterInfo = readConfigFile(prop, false);
        readConfigFileForMachineLearning(this.parameterInfo, prop);
        readAgingConfigFile(this.parameterInfo, prop);
        readDiscrimitiveConfigFile(this.parameterInfo, prop);
        Properties mainProp = getPropertiesFromFile(Constant.getWaveMappingMainApConfig());
        this.mainApParameterInfo = readConfigFile(mainProp, true);
        readConfigFileForMachineLearning(this.mainApParameterInfo, mainProp);
        readAgingConfigFile(this.mainApParameterInfo, mainProp);
        readDiscrimitiveConfigFile(this.mainApParameterInfo, mainProp);
        LogUtil.d(false, "ParamManager init finish.", new Object[0]);
        LogUtil.d(false, "WMAPING_CONFIG:%{public}s", Constant.getWaveMappingConfig());
        LogUtil.d(false, "WMAPING_MAINAP_CONFIG:%{public}s", Constant.getWaveMappingMainApConfig());
        LogUtil.d(false, "param:%{public}s", this.parameterInfo.toString());
        LogUtil.d(false, "main Param:%{public}s", this.mainApParameterInfo.toString());
    }

    public static synchronized ParamManager getInstance() {
        ParamManager paramManager;
        synchronized (ParamManager.class) {
            if (instance == null) {
                instance = new ParamManager();
            }
            paramManager = instance;
        }
        return paramManager;
    }

    public ParameterInfo getParameterInfo() {
        return this.parameterInfo;
    }

    public ParameterInfo getMainApParameterInfo() {
        return this.mainApParameterInfo;
    }

    public void setParameterInfo(ParameterInfo parameterInfo2) {
        this.parameterInfo = parameterInfo2;
    }

    public MobileApCheckParamInfo getMobileApCheckParamInfo() {
        return this.mobileApCheckParamInfo;
    }

    public void setMobileApCheckParamInfo(MobileApCheckParamInfo mobileApCheckParamInfo2) {
        this.mobileApCheckParamInfo = mobileApCheckParamInfo2;
    }

    private Properties getPropertiesFromFile(String paraFilePath) {
        Object[] objArr;
        Properties props = new Properties();
        FileInputStream stream = null;
        File file = new File(paraFilePath);
        if (!file.exists() || !file.isFile()) {
            LogUtil.e(false, "%{public}s does not exist. ", paraFilePath);
            return null;
        }
        try {
            stream = new FileInputStream(file);
            props.load(stream);
            LogUtil.d(false, "%{public}s be read.", file.getCanonicalPath());
            try {
                stream.close();
            } catch (IOException e) {
                objArr = new Object[]{e.getMessage()};
            }
        } catch (IOException e2) {
            LogUtil.e(false, "Cold not open %{public}s", file.toString());
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    objArr = new Object[]{e3.getMessage()};
                }
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    LogUtil.e(false, "close stream error : %{public}s", e4.getMessage());
                }
            }
            throw th;
        }
        return props;
        LogUtil.e(false, "close stream error : %{public}s", objArr);
        return props;
    }

    private void readConfigFileForMachineLearning(ParameterInfo param, Properties props) {
        if (props != null) {
            try {
                setKnnParams(param, props);
                setJudgeParams(param, props);
                setOtherParams(param, props);
            } catch (NumberFormatException e) {
                LogUtil.e(false, "readConfigFileForMachineLearning:%{public}s", e.getMessage());
            } catch (Exception e2) {
                LogUtil.e(false, "readConfigFileForMachineLearning failed by Exception", new Object[0]);
            }
        }
    }

    private void setKnnParams(ParameterInfo param, Properties props) {
        String minNonzeroPerct = props.getProperty(KEY_MIN_NON_ZERO_PERCT, null);
        if (minNonzeroPerct != null) {
            LogUtil.d(false, " MINNONZEROPERCT :  %{public}s", minNonzeroPerct);
            param.setMinNonzeroPerct(Float.parseFloat(minNonzeroPerct.trim().replace(SYMBOL_SPACE, "")));
        }
        String minDistinctValue = props.getProperty(KEY_MIN_DISTINCT_VALUE, null);
        if (minDistinctValue != null) {
            LogUtil.d(false, " MINDISTINCTVALUE : %{public}s", minDistinctValue);
            param.setMinDistinctValue(Float.parseFloat(minDistinctValue.trim().replace(SYMBOL_SPACE, "")));
        }
        String minStdev = props.getProperty(KEY_MIN_STD, null);
        if (minStdev != null) {
            LogUtil.d(false, " MINSTDEV :  %{public}s", minStdev);
            param.setMinStdev(Float.parseFloat(minStdev.trim().replace(SYMBOL_SPACE, "")));
        }
        String threshold = props.getProperty(KEY_THRESHOLD, null);
        if (threshold != null) {
            LogUtil.d(false, " THRESHOLD : %{public}s", threshold);
            param.setThreshold(Integer.parseInt(threshold.trim().replace(SYMBOL_SPACE, "")));
        }
        String clusters = props.getProperty(KEY_CLUSTER_NUMBER, null);
        if (clusters != null) {
            LogUtil.d(false, " NCLUSTERS :  %{public}s", clusters);
            param.setClusterNum(Integer.parseInt(clusters.trim().replace(SYMBOL_SPACE, "")));
        }
        String neighborNum = props.getProperty(KEY_NEIGHBOR_NUMBER, null);
        if (neighborNum != null) {
            LogUtil.d(false, " NEIGHBORNUM :  %{public}s", neighborNum);
            param.setNeighborNum(Integer.parseInt(neighborNum.trim().replace(SYMBOL_SPACE, "")));
        }
        String knnMaxDist = props.getProperty(KEY_KNN_MAX_DIST, null);
        if (knnMaxDist != null) {
            LogUtil.d(false, " KNNMAXDIST :  %{public}s", knnMaxDist);
            param.setKnnMaxDist(Integer.parseInt(knnMaxDist.trim().replace(SYMBOL_SPACE, "")));
        }
        String knnShareMacRatio = props.getProperty(KEY_KNN_SHARE_MAC_RATIO, null);
        if (knnShareMacRatio != null) {
            LogUtil.d(false, " KNNSHAREMACRATIO :  %{public}s", knnShareMacRatio);
            param.setKnnShareMacRatio(Float.parseFloat(knnShareMacRatio.trim().replace(SYMBOL_SPACE, "")));
        }
    }

    private void setJudgeParams(ParameterInfo param, Properties props) {
        String minBssidCount = props.getProperty(KEY_MIN_BSSID_COUNT, null);
        if (minBssidCount != null) {
            LogUtil.d(false, " MINBSSIDCOUNT :  %{public}s", minBssidCount);
            param.setMinBssidCount(Integer.parseInt(minBssidCount.trim().replace(SYMBOL_SPACE, "")));
        }
        String minMeanRssi = props.getProperty(KEY_MIN_MEAN_RSSI, null);
        if (minMeanRssi != null) {
            LogUtil.d(false, " MINMEANRSSI :  %{public}s", minMeanRssi);
            param.setMinMeanRssi(Integer.parseInt(minMeanRssi.trim().replace(SYMBOL_SPACE, "")));
        }
        String maxDist = props.getProperty(KEY_MAX_DIST, null);
        if (maxDist != null) {
            LogUtil.d(false, " MAXDIST :  %{public}s", maxDist);
            param.setMaxDist(Float.parseFloat(maxDist.trim().replace(SYMBOL_SPACE, "")));
        }
        String minFreq = props.getProperty(KEY_MIN_FREQ, null);
        if (minFreq != null) {
            LogUtil.d(false, " MINFREQ :  %{public}s", minFreq);
            param.setMinFreq(Integer.parseInt(minFreq.trim().replace(SYMBOL_SPACE, "")));
        }
        String maxBssidNum = props.getProperty(KEY_MAX_BSSID_COUNT, null);
        if (maxBssidNum != null) {
            LogUtil.d(false, " MAXBSSIDNUM :  %{public}s", maxBssidNum);
            param.setMaxBssidNum(Integer.parseInt(maxBssidNum.trim().replace(SYMBOL_SPACE, "")));
        }
        String minSampleRatio = props.getProperty(KEY_MIN_SAMPLE_RATIO, null);
        if (minSampleRatio != null) {
            LogUtil.d(false, " MINSAMPLERATIO :  %{public}s", minSampleRatio);
            param.setMinSampleRatio((float) Integer.parseInt(minSampleRatio.trim().replace(SYMBOL_SPACE, "")));
        }
    }

    private void setOtherParams(ParameterInfo param, Properties props) {
        String deleteFlag = props.getProperty(KEY_DELETE_FLAG, null);
        if (deleteFlag != null) {
            LogUtil.d(false, " DELETEFLAG :  %{public}s", deleteFlag);
            param.setDeleteFlag(Integer.parseInt(deleteFlag.trim().replace(SYMBOL_SPACE, "")));
        }
        String weightParam = props.getProperty(KEY_WEIGHT_PARAM, null);
        if (weightParam != null) {
            LogUtil.d(false, " WEIGHTPARAM :  %{public}s", weightParam);
            param.setWeightParam(Float.parseFloat(weightParam.trim().replace(SYMBOL_SPACE, "")));
        }
        String shareRatioParam = props.getProperty(KEY_SHARE_RATIO_PARAM, null);
        if (shareRatioParam != null) {
            LogUtil.d(false, " SHARERATIOPARAM :  %{public}s", shareRatioParam);
            param.setShareRatioParam(Float.parseFloat(shareRatioParam.trim().replace(SYMBOL_SPACE, "")));
        }
        String lowerBound = props.getProperty(KEY_LOWER_BOUND, null);
        if (lowerBound != null) {
            LogUtil.d(false, " LOWERBOUND :  %{public}s", lowerBound);
            param.setLowerBound(Integer.parseInt(lowerBound.trim().replace(SYMBOL_SPACE, "")));
        }
        String reprMinOcc = props.getProperty(KEY_REPR_MIN_OCCUPATION, null);
        if (reprMinOcc != null) {
            LogUtil.d(false, " REPRMINOCC :  %{public}s", reprMinOcc);
            param.setReprMinOcc(Integer.parseInt(reprMinOcc.trim().replace(SYMBOL_SPACE, "")));
        }
        String connectedStrength = props.getProperty(KEY_CONNECTED_STRENGTH, null);
        if (connectedStrength != null) {
            LogUtil.d(false, " CONNECTEDSTRENGTH :  %{public}s", connectedStrength);
            param.setConnectedStrength(Float.parseFloat(connectedStrength.trim().replace(SYMBOL_SPACE, "")));
        }
    }

    private void readAgingConfigFile(ParameterInfo param, Properties props) {
        if (props != null) {
            try {
                String checkAgingAcumlteCount = props.getProperty(KEY_CHECK_AGING_ACCUMULATE_COUNT, null);
                if (checkAgingAcumlteCount != null) {
                    LogUtil.d(false, " checkAgingAcumlteCount :  %{public}s", checkAgingAcumlteCount);
                    param.setCheckAgingAccumulateCount(Integer.parseInt(checkAgingAcumlteCount.trim().replace(SYMBOL_SPACE, "")));
                }
                String minMainApOccUpd = props.getProperty(KEY_MIN_MAIN_AP_OCCUPATION, null);
                if (minMainApOccUpd != null) {
                    LogUtil.d(false, " minMainAPOccUpd :  %{public}s", minMainApOccUpd);
                    param.setMinMainApOccUpd(Float.parseFloat(minMainApOccUpd.trim().replace(SYMBOL_SPACE, "")));
                }
                String minUkwnRatioUpd = props.getProperty(KEY_MIN_UNKNOWN_RATIO_OCCUPATION, null);
                if (minUkwnRatioUpd != null) {
                    LogUtil.d(false, " minUkwnRatioUpd :  %{public}s", minUkwnRatioUpd);
                    param.setMinUnknownRatioUpd(Float.parseFloat(minUkwnRatioUpd.trim().replace(SYMBOL_SPACE, "")));
                }
                String minTrainBssiLstLenUpd = props.getProperty(KEY_MIN_TRAIN_BSSID_LIST_LENGTH, null);
                if (minTrainBssiLstLenUpd != null) {
                    LogUtil.d(false, " minTrainBssiLstLenUpd :  %{public}s", minTrainBssiLstLenUpd);
                    param.setMinTrainBssiLstLenUpd(Integer.parseInt(minTrainBssiLstLenUpd.trim().replace(SYMBOL_SPACE, "")));
                }
                String abnMacRatioAllowUpd = props.getProperty(KEY_ABNORMAL_MAC_RATIO, null);
                if (abnMacRatioAllowUpd != null) {
                    LogUtil.d(false, " abnMacRatioAllowUpd :  %{public}s", abnMacRatioAllowUpd);
                    param.setAbnMacRatioAllowUpd(Float.parseFloat(abnMacRatioAllowUpd.trim().replace(SYMBOL_SPACE, "")));
                }
            } catch (NumberFormatException e) {
                LogUtil.e(false, "readAgingConfigFile:%{public}s", e.getMessage());
            } catch (Exception e2) {
                LogUtil.e(false, "readAgingConfigFile failed by Exception", new Object[0]);
            }
        }
    }

    private void readDiscrimitiveConfigFile(ParameterInfo param, Properties props) {
        if (props != null) {
            try {
                setRatioParams(param, props);
                setTestAndTrainParams(param, props);
            } catch (NumberFormatException e) {
                LogUtil.e(false, "readDiscrimitiveConfigFile:%{public}s", e.getMessage());
            } catch (Exception e2) {
                LogUtil.e(false, "readDiscrimitiveConfigFile failed by Exception", new Object[0]);
            }
        }
    }

    private void setRatioParams(ParameterInfo param, Properties props) {
        String minUnkwnRatio = props.getProperty(KEY_MIN_UNKNOWN_RATIO, null);
        if (minUnkwnRatio != null) {
            LogUtil.d(false, " MINUNKWNRATIO :  %{public}s", minUnkwnRatio);
            param.setMinUnknownRatio(Float.parseFloat(minUnkwnRatio.trim().replace(SYMBOL_SPACE, "")));
        }
        String maxShatterRatio = props.getProperty(KEY_MAX_SHATTER_RATIO, null);
        if (maxShatterRatio != null) {
            LogUtil.d(false, " maxShatterRatio :  %{public}s", maxShatterRatio);
            param.setMaxShatterRatio(Float.parseFloat(maxShatterRatio.trim().replace(SYMBOL_SPACE, "")));
        }
        String totalShatterRatio = props.getProperty(KEY_TOTAL_SHETTER_RATIO, null);
        if (totalShatterRatio != null) {
            LogUtil.d(false, " totalShatterRatio :  %{public}s", totalShatterRatio);
            param.setTotalShatterRatio(Float.parseFloat(totalShatterRatio.trim().replace(SYMBOL_SPACE, "")));
        }
        String maxDistDecayRatio = props.getProperty(KEY_MAX_DIST_DECAY_RATIO, null);
        if (maxDistDecayRatio != null) {
            String str = TAG;
            Log.d(str, " MAXDISTDECAYRATIO :  " + maxDistDecayRatio);
            param.setMaxDistDecayRatio(Float.parseFloat(maxDistDecayRatio.trim().replace(SYMBOL_SPACE, "")));
        }
    }

    private void setTestAndTrainParams(ParameterInfo param, Properties props) {
        String accumulateCount = props.getProperty(KEY_ACCUMULATE_COUNT, null);
        if (accumulateCount != null) {
            LogUtil.d(false, " acumlteCount :  %{public}s", accumulateCount);
            param.setAccumulateCount(Integer.parseInt(accumulateCount.trim().replace(SYMBOL_SPACE, "")));
        }
        String maxDistMinLimit = props.getProperty(KEY_MAX_DIST_MIN_LIMIT, null);
        if (maxDistMinLimit != null) {
            LogUtil.d(false, " MAXDISTMINLIMIT :  %{public}s", maxDistMinLimit);
            param.setMaxDistMinLimit(Float.parseFloat(maxDistMinLimit.trim().replace(SYMBOL_SPACE, "")));
        }
        String testDataRatio = props.getProperty(KEY_TEST_DATA_RATIO, null);
        if (testDataRatio != null) {
            LogUtil.d(false, " TESTDATARATIO :  %{public}s", testDataRatio);
            param.setTestDataRatio(Float.parseFloat(testDataRatio.trim().replace(SYMBOL_SPACE, "")));
        }
        String minModelTypes = props.getProperty(KEY_MIN_MODEL_TYPES, null);
        if (minModelTypes != null) {
            LogUtil.d(false, " minModelTypes :  %{public}s", minModelTypes);
            param.setMinModelTypes(Integer.parseInt(minModelTypes.trim().replace(SYMBOL_SPACE, "")));
        }
        String trainDataSize = props.getProperty(KEY_TRAIN_DATA_SIZE, null);
        if (trainDataSize != null) {
            LogUtil.d(false, " TRAINDATASSIZE :  %{public}s", trainDataSize);
            param.setTrainDatasSize(Integer.parseInt(trainDataSize.trim().replace(SYMBOL_SPACE, "")));
        }
        String testDataSize = props.getProperty(KEY_TEST_DATA_SIZE, null);
        if (testDataSize != null) {
            LogUtil.d(false, " TESTDATASIZE :  %{public}s", testDataSize);
            param.setTestDataSize(Integer.parseInt(testDataSize.trim().replace(SYMBOL_SPACE, "")));
        }
        String testDataCnt = props.getProperty(KEY_TEST_DATA_COUNT, null);
        if (testDataCnt != null) {
            LogUtil.d(false, " TESTDATACNT :  %{public}s", testDataCnt);
            param.setTestDataCnt(Integer.parseInt(testDataCnt.trim().replace(SYMBOL_SPACE, "")));
        }
    }

    private ParameterInfo readConfigFile(Properties props, boolean isMainAp) {
        ParameterInfo parameterInfo2 = new ParameterInfo(isMainAp);
        if (props == null) {
            return parameterInfo2;
        }
        try {
            String showLog = props.getProperty(KEY_SHOW_LOG, null);
            if (showLog != null) {
                String str = TAG;
                Log.d(str, " SHOWLOG :  " + showLog);
                if (AppActConstant.VALUE_TRUE.equalsIgnoreCase(showLog.trim().replace(SYMBOL_SPACE, ""))) {
                    LogUtil.setShowI(true);
                    LogUtil.setShowV(true);
                    LogUtil.setDebugFlag(true);
                    ShowToast.setIsShow(true);
                } else {
                    if (parameterInfo2.isBetaUser()) {
                        LogUtil.setShowI(true);
                    } else {
                        LogUtil.setShowI(false);
                    }
                    LogUtil.setDebugFlag(false);
                    ShowToast.setIsShow(false);
                }
            }
            setInitParams(props);
            setScanParams(props);
            setUserPreferenceParams(props);
            setPowerPreferenceParams(props);
            setBack4gParams(props);
        } catch (NumberFormatException e) {
            LogUtil.e(false, "readConfigFile:%{public}s", e.getMessage());
        } catch (Exception e2) {
            LogUtil.e(false, "readConfigFile failed by Exception", new Object[0]);
        }
        return parameterInfo2;
    }

    private void setInitParams(Properties props) {
        String configVersion = props.getProperty(KEY_CONFIG_VERSION, null);
        if (configVersion != null) {
            LogUtil.d(false, " CONFIG_VER :  %{public}s", configVersion);
            this.parameterInfo.setConfigVer(Integer.parseInt(configVersion.trim().replace(SYMBOL_SPACE, "")));
        }
        String batchNumber = props.getProperty(KEY_BATCH_NUMBER, null);
        if (batchNumber != null) {
            LogUtil.d(false, " FG_BATCH_NUM :  %{public}s", batchNumber);
            batchNumber = batchNumber.trim().replace(SYMBOL_SPACE, "");
            this.parameterInfo.setFgBatchNum(Integer.parseInt(batchNumber));
        }
        String activeSample = props.getProperty(KEY_ACTIVE_SAMPLE, null);
        if (activeSample != null) {
            LogUtil.d(false, " ACTIVESAMPLE :  %{public}s", activeSample);
            this.parameterInfo.setActiveSample(Integer.parseInt(activeSample.trim().replace(SYMBOL_SPACE, "")));
        }
        String batchId = props.getProperty(KEY_BATCH_ID, null);
        if (batchId != null) {
            LogUtil.d(false, " BATCHID :  %{public}s", batchId);
            this.parameterInfo.setBatchId(Integer.parseInt(batchId.trim().replace(SYMBOL_SPACE, "")));
        }
        String bssidStart = props.getProperty(KEY_BSSID_START, null);
        if (bssidStart != null) {
            LogUtil.d(false, " BSSIDSTART :  %{public}s", bssidStart);
            this.parameterInfo.setBssidStart(Integer.parseInt(bssidStart.trim().replace(SYMBOL_SPACE, "")));
        }
        String cellIdStart = props.getProperty(KEY_CELL_ID_START, null);
        if (cellIdStart != null) {
            LogUtil.d(false, " CELLIDSTART :  %{private}s", cellIdStart);
            this.parameterInfo.setCellIdStart(Integer.parseInt(cellIdStart.trim().replace(SYMBOL_SPACE, "")));
        }
        String writeFileSample = props.getProperty(KEY_WRITE_FILE_SAMPLE, null);
        if (writeFileSample != null) {
            LogUtil.d(false, " WRITEFILESAMPLE :  %{public}s", writeFileSample);
            this.parameterInfo.setWriteFileSample(Long.parseLong(writeFileSample.trim().replace(SYMBOL_SPACE, "")));
        }
        String wifiDataSample = props.getProperty(KEY_WIFI_DATA_SAMPLE, null);
        if (wifiDataSample != null) {
            LogUtil.d(false, " wifiDataSample :  %{public}s", wifiDataSample);
            this.parameterInfo.setWifiDataSample(Integer.parseInt(wifiDataSample.trim().replace(SYMBOL_SPACE, "")));
        }
    }

    private void setScanParams(Properties props) {
        String timestamp = props.getProperty(KEY_TIMESTAMP, null);
        if (timestamp != null) {
            LogUtil.d(false, " TIMESTAMP :  %{public}s", timestamp);
            this.parameterInfo.setTimestamp(Integer.parseInt(timestamp.trim().replace(SYMBOL_SPACE, "")));
        }
        String scanWifiStart = props.getProperty(KEY_SCAN_WIFI_START, null);
        if (scanWifiStart != null) {
            LogUtil.d(false, " SCANWIFISTART :  %{public}s", scanWifiStart);
            this.parameterInfo.setScanWifiStart(Integer.parseInt(scanWifiStart.trim().replace(SYMBOL_SPACE, "")));
        }
        String scanSsid = props.getProperty(KEY_SCAN_SSID, null);
        if (scanSsid != null) {
            LogUtil.d(false, " SCANSSID :  %{private}s", scanSsid);
            this.parameterInfo.setScanSsid(Integer.parseInt(scanSsid.trim().replace(SYMBOL_SPACE, "")));
        }
        String scanMac = props.getProperty(KEY_SCAN_MAC, null);
        if (scanMac != null) {
            LogUtil.d(false, " SCANMAC :  %{private}s", scanMac);
            this.parameterInfo.setScanMac(Integer.parseInt(scanMac.trim().replace(SYMBOL_SPACE, "")));
        }
        String scanRssi = props.getProperty(KEY_SCAN_RSSI, null);
        if (scanRssi != null) {
            LogUtil.d(false, " SCANRSSI :  %{public}s", scanRssi);
            this.parameterInfo.setScanRssi(Integer.parseInt(scanRssi.trim().replace(SYMBOL_SPACE, "")));
        }
        String scanChannel = props.getProperty(KEY_SCAN_CHANNEL, null);
        if (scanChannel != null) {
            LogUtil.d(false, " SCANCH :  %{public}s", scanChannel);
            this.parameterInfo.setScanCh(Integer.parseInt(scanChannel.trim().replace(SYMBOL_SPACE, "")));
        }
        String servingWifiRssi = props.getProperty(KEY_SERVING_WIFI_RSSI, null);
        if (servingWifiRssi != null) {
            LogUtil.d(false, " SERVINGWIFIRSSI :  %{public}s", servingWifiRssi);
            this.parameterInfo.setServingWiFiRssi(Integer.parseInt(servingWifiRssi.trim().replace(SYMBOL_SPACE, "")));
        }
        String servingWifiMac = props.getProperty(KEY_SERVING_WIFI_MAC, null);
        if (servingWifiMac != null) {
            LogUtil.d(false, " SERVINGWIFIMAC :  %{private}s", servingWifiMac);
            this.parameterInfo.setServingWiFiMac(Integer.parseInt(servingWifiMac.trim().replace(SYMBOL_SPACE, "")));
        }
    }

    private void setUserPreferenceParams(Properties props) {
        String userPreferEnabled = props.getProperty(KEY_USER_PREFER_ENABLED, null);
        if (userPreferEnabled != null) {
            LogUtil.d(false, " userPreferEnabled :  %{public}s", userPreferEnabled);
            this.parameterInfo.setUserPreferEnable(Boolean.parseBoolean(userPreferEnabled.trim().replace(SYMBOL_SPACE, "")));
        }
        String userPrefStartTimes = props.getProperty(KEY_USER_PREFER_START_TIMES, null);
        if (userPrefStartTimes != null) {
            LogUtil.d(false, " USERPREFSTARTTIMES :  %{public}s", userPrefStartTimes);
            this.parameterInfo.setUserPrefStartTimes(Integer.parseInt(userPrefStartTimes.trim().replace(SYMBOL_SPACE, "")));
        }
        String userPrefStartDuration = props.getProperty(KEY_USER_PREFER_START_DURATION, null);
        if (userPrefStartDuration != null) {
            LogUtil.d(false, " USERPREFSTARTDURATION :  %{public}s", userPrefStartDuration);
            this.parameterInfo.setUserPrefStartDuration(Long.parseLong(userPrefStartDuration.trim().replace(SYMBOL_SPACE, "")));
        }
        String userPrefFreqRatio = props.getProperty(KEY_USER_PREFER_FREQ_RATIO, null);
        if (userPrefFreqRatio != null) {
            LogUtil.d(false, " USERPREFFREQRATIO :  %{public}s", userPrefFreqRatio);
            this.parameterInfo.setUserPrefFreqRatio(Float.parseFloat(userPrefFreqRatio.trim().replace(SYMBOL_SPACE, "")));
        }
        String userPrefDurationRatio = props.getProperty(KEY_USER_PREFER_DURATION_RATIO, null);
        if (userPrefDurationRatio != null) {
            LogUtil.d(false, " USERPREFDURATIONRATIO :  %{public}s", userPrefDurationRatio);
            this.parameterInfo.setUserPrefDurationRatio(Double.parseDouble(userPrefDurationRatio.trim().replace(SYMBOL_SPACE, "")));
        }
    }

    private void setPowerPreferenceParams(Properties props) {
        String powerPreferEnabled = props.getProperty(KEY_POWER_PREFER_ENABLED, null);
        if (powerPreferEnabled != null) {
            LogUtil.d(false, " powerPreferEnabled :  %{public}s", powerPreferEnabled);
            this.parameterInfo.setPowerPreferEnable(Boolean.parseBoolean(powerPreferEnabled.trim().replace(SYMBOL_SPACE, "")));
        }
        String powerSaveType = props.getProperty(KEY_POWER_SAVE_TYPE, null);
        if (powerSaveType != null) {
            LogUtil.d(false, " POWERSAVETYPE :  %{public}s", powerSaveType);
            this.parameterInfo.setPowerSaveType(Integer.parseInt(powerSaveType.trim().replace(SYMBOL_SPACE, "")));
        }
        String powerSaveGap = props.getProperty(KEY_POWER_SAVE_GAP, null);
        if (powerSaveGap != null) {
            LogUtil.d(false, " POWERSAVEGAP :  %{public}s", powerSaveGap);
            this.parameterInfo.setPowerSaveGap(Double.parseDouble(powerSaveGap.trim().replace(SYMBOL_SPACE, "")));
        }
    }

    private void setBack4gParams(Properties props) {
        String back4gEnabled = props.getProperty(KEY_BACK_4G_ENABLED, null);
        if (back4gEnabled != null) {
            LogUtil.d(false, " back4GEnabled :  %{public}s", back4gEnabled);
            this.parameterInfo.setBack4gEnable(Boolean.parseBoolean(back4gEnabled.trim().replace(SYMBOL_SPACE, "")));
        }
        String out4gInterval = props.getProperty(KEY_OUT_4G_INTERVAL, null);
        if (out4gInterval != null) {
            LogUtil.d(false, " out4GInterval :  %{public}s", out4gInterval);
            this.parameterInfo.setBack4gThresholdOut4gInterval(Integer.parseInt(out4gInterval.trim().replace(SYMBOL_SPACE, "")));
        }
        String back4gDurationMin = props.getProperty(KEY_BACK_4G_DURATION_MIN, null);
        if (back4gDurationMin != null) {
            LogUtil.d(false, " back4GDurationMin :  %{public}s", back4gDurationMin);
            this.parameterInfo.setBack4gThresholdDurationMin(Integer.parseInt(back4gDurationMin.trim().replace(SYMBOL_SPACE, "")));
        }
        String back4gSignalMin = props.getProperty(KEY_BACK_4G_SIGNAL_MIN, null);
        if (back4gSignalMin != null) {
            LogUtil.d(false, " back4GSignalMin :  %{public}s", back4gSignalMin);
            this.parameterInfo.setBack4gThresholdSignalMin(Integer.parseInt(back4gSignalMin.trim().replace(SYMBOL_SPACE, "")));
        }
        String back4gRatio = props.getProperty(KEY_BACK_4G_RATIO, null);
        if (back4gRatio != null) {
            LogUtil.d(false, " back4GRatio :  %{public}s", back4gRatio);
            this.parameterInfo.setBack4gThresholdDuration4gRatio(Double.parseDouble(back4gRatio.trim().replace(SYMBOL_SPACE, "")));
        }
    }
}
