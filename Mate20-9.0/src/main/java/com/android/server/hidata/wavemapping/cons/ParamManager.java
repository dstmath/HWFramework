package com.android.server.hidata.wavemapping.cons;

import android.util.Log;
import com.android.server.hidata.wavemapping.entity.MobileApCheckParamInfo;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.ShowToast;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ParamManager {
    private static final String TAG = ("WMapping." + ParamManager.class.getSimpleName());
    private static ParamManager instance = null;
    private ParameterInfo mainApParameterInfo;
    private MobileApCheckParamInfo mobileApCheckParamInfo = new MobileApCheckParamInfo();
    private ParameterInfo parameterInfo;

    private ParamManager() {
        Properties prop = getPropertiesFromFile(Constant.getWmapingConfig());
        this.parameterInfo = ParamManager_ReadConfigFile(prop, false);
        ParamManager_ReadConfigFile_MachineLearning(this.parameterInfo, prop);
        ParamManager_ReadConfigFile_Aging(this.parameterInfo, prop);
        ParamManager_ReadConfigFile_Discrimitive(this.parameterInfo, prop);
        Properties mainProp = getPropertiesFromFile(Constant.getWmapingMainapConfig());
        this.mainApParameterInfo = ParamManager_ReadConfigFile(mainProp, true);
        ParamManager_ReadConfigFile_MachineLearning(this.mainApParameterInfo, mainProp);
        ParamManager_ReadConfigFile_Aging(this.mainApParameterInfo, mainProp);
        ParamManager_ReadConfigFile_Discrimitive(this.mainApParameterInfo, mainProp);
        LogUtil.d("ParamManager init finish.");
        LogUtil.d("WMAPING_CONFIG:" + Constant.getWmapingConfig());
        LogUtil.d("WMAPING_MAINAP_CONFIG:" + Constant.getWmapingMainapConfig());
        LogUtil.d("param:" + this.parameterInfo.toString());
        LogUtil.d("main Param:" + this.mainApParameterInfo.toString());
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
        StringBuilder sb;
        Properties props = new Properties();
        FileInputStream stream = null;
        File file = new File(paraFilePath);
        if (!file.exists() || !file.isFile()) {
            LogUtil.e(paraFilePath + " is not exists. ");
            return null;
        }
        try {
            stream = new FileInputStream(file);
            props.load(stream);
            LogUtil.d(file.getAbsolutePath() + " be read.");
            try {
                stream.close();
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
        } catch (IOException e2) {
            LogUtil.e("Cold not open  " + file);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    LogUtil.e("close stream error : " + e4);
                }
            }
            throw th;
        }
        return props;
        sb.append("close stream error : ");
        sb.append(e);
        LogUtil.e(sb.toString());
        return props;
    }

    private void ParamManager_ReadConfigFile_MachineLearning(ParameterInfo param, Properties props) {
        ParameterInfo parameterInfo2 = param;
        Properties properties = props;
        if (properties != null) {
            try {
                String minNonzeroPerct = properties.getProperty("minNonzeroPerct", null);
                if (minNonzeroPerct != null) {
                    LogUtil.d(" MINNONZEROPERCT :  " + minNonzeroPerct);
                    minNonzeroPerct = minNonzeroPerct.trim().replace(" ", "");
                    parameterInfo2.setMinNonzeroPerct(Float.parseFloat(minNonzeroPerct));
                }
                String minDistinctValue = properties.getProperty("minDistinctValue", null);
                if (minDistinctValue != null) {
                    LogUtil.d(" MINDISTINCTVALUE :  " + minDistinctValue);
                    parameterInfo2.setMinDistinctValue(Float.parseFloat(minDistinctValue.trim().replace(" ", "")));
                }
                String minStdev = properties.getProperty("minStdev", null);
                if (minStdev != null) {
                    LogUtil.d(" MINSTDEV :  " + minStdev);
                    parameterInfo2.setMinStdev(Float.parseFloat(minStdev.trim().replace(" ", "")));
                }
                String threshold = properties.getProperty("threshold", null);
                if (threshold != null) {
                    LogUtil.d(" THRESHOLD :  " + threshold);
                    parameterInfo2.setThreshold(Integer.parseInt(threshold.trim().replace(" ", "")));
                }
                String minBssidCount = properties.getProperty("minBssidCount", null);
                if (minBssidCount != null) {
                    LogUtil.d(" MINBSSIDCOUNT :  " + minBssidCount);
                    parameterInfo2.setMinBssidCount(Integer.parseInt(minBssidCount.trim().replace(" ", "")));
                }
                String nClusters = properties.getProperty("nClusters", null);
                if (nClusters != null) {
                    LogUtil.d(" NCLUSTERS :  " + nClusters);
                    parameterInfo2.setnClusters(Integer.parseInt(nClusters.trim().replace(" ", "")));
                }
                String neighborNum = properties.getProperty("neighborNum", null);
                if (neighborNum != null) {
                    LogUtil.d(" NEIGHBORNUM :  " + neighborNum);
                    parameterInfo2.setNeighborNum(Integer.parseInt(neighborNum.trim().replace(" ", "")));
                }
                String knnMaxDist = properties.getProperty("knnMaxDist", null);
                if (knnMaxDist != null) {
                    LogUtil.d(" KNNMAXDIST :  " + knnMaxDist);
                    parameterInfo2.setKnnMaxDist(Integer.parseInt(knnMaxDist.trim().replace(" ", "")));
                }
                String knnShareMacRatio = properties.getProperty("knnShareMacRatio", null);
                if (knnShareMacRatio != null) {
                    LogUtil.d(" KNNSHAREMACRATIO :  " + knnShareMacRatio);
                    parameterInfo2.setKnnShareMacRatio(Float.parseFloat(knnShareMacRatio.trim().replace(" ", "")));
                }
                String minMeanRssi = properties.getProperty("minMeanRssi", null);
                if (minMeanRssi != null) {
                    LogUtil.d(" MINMEANRSSI :  " + minMeanRssi);
                    parameterInfo2.setMinMeanRssi(Integer.parseInt(minMeanRssi.trim().replace(" ", "")));
                }
                String maxDist = properties.getProperty("maxDist", null);
                if (maxDist != null) {
                    LogUtil.d(" MAXDIST :  " + maxDist);
                    parameterInfo2.setMaxDist(Float.parseFloat(maxDist.trim().replace(" ", "")));
                }
                String deleteFlag = properties.getProperty("deleteFlag", null);
                if (deleteFlag != null) {
                    LogUtil.d(" DELETEFLAG :  " + deleteFlag);
                    String str = minNonzeroPerct;
                    deleteFlag = deleteFlag.trim().replace(" ", "");
                    parameterInfo2.setDeleteFlag(Integer.parseInt(deleteFlag));
                }
                String minSampleRatio = properties.getProperty("minSampleRatio", null);
                if (minSampleRatio != null) {
                    LogUtil.d(" MINSAMPLERATIO :  " + minSampleRatio);
                    String str2 = minSampleRatio;
                    minSampleRatio = minSampleRatio.trim().replace(" ", "");
                    parameterInfo2.setMinSampleRatio((float) Integer.parseInt(minSampleRatio));
                } else {
                    String str3 = minSampleRatio;
                }
                String weightParam = properties.getProperty("weightParam", null);
                if (weightParam != null) {
                    StringBuilder sb = new StringBuilder();
                    String str4 = minSampleRatio;
                    sb.append(" WEIGHTPARAM :  ");
                    sb.append(weightParam);
                    LogUtil.d(sb.toString());
                    String str5 = deleteFlag;
                    parameterInfo2.setWeightParam(Float.parseFloat(weightParam.trim().replace(" ", "")));
                } else {
                    String str6 = deleteFlag;
                }
                String shareRatioParam = properties.getProperty("shareRatioParam", null);
                if (shareRatioParam != null) {
                    LogUtil.d(" SHARERATIOPARAM :  " + shareRatioParam);
                    String str7 = shareRatioParam;
                    shareRatioParam = shareRatioParam.trim().replace(" ", "");
                    parameterInfo2.setShareRatioParam(Float.parseFloat(shareRatioParam));
                } else {
                    String str8 = shareRatioParam;
                }
                String lowerBound = properties.getProperty("lowerBound", null);
                if (lowerBound != null) {
                    StringBuilder sb2 = new StringBuilder();
                    String str9 = shareRatioParam;
                    sb2.append(" LOWERBOUND :  ");
                    sb2.append(lowerBound);
                    LogUtil.d(sb2.toString());
                    String str10 = lowerBound;
                    lowerBound = lowerBound.trim().replace(" ", "");
                    parameterInfo2.setLowerBound(Integer.parseInt(lowerBound));
                } else {
                    String str11 = lowerBound;
                }
                String reprMinOcc = properties.getProperty("reprMinOcc", null);
                if (reprMinOcc != null) {
                    StringBuilder sb3 = new StringBuilder();
                    String str12 = lowerBound;
                    sb3.append(" REPRMINOCC :  ");
                    sb3.append(reprMinOcc);
                    LogUtil.d(sb3.toString());
                    String str13 = reprMinOcc;
                    reprMinOcc = reprMinOcc.trim().replace(" ", "");
                    parameterInfo2.setReprMinOcc(Integer.parseInt(reprMinOcc));
                } else {
                    String str14 = reprMinOcc;
                    String str15 = lowerBound;
                }
                String connectedStrength = properties.getProperty("connectedStrength", null);
                if (connectedStrength != null) {
                    StringBuilder sb4 = new StringBuilder();
                    String str16 = reprMinOcc;
                    sb4.append(" CONNECTEDSTRENGTH :  ");
                    sb4.append(connectedStrength);
                    LogUtil.d(sb4.toString());
                    String str17 = connectedStrength;
                    String connectedStrength2 = connectedStrength.trim().replace(" ", "");
                    parameterInfo2.setConnectedStrength(Float.parseFloat(connectedStrength2));
                    String str18 = connectedStrength2;
                } else {
                    String str19 = connectedStrength;
                }
                String minFreq = properties.getProperty("minFreq", null);
                if (minFreq != null) {
                    LogUtil.d(" MINFREQ :  " + minFreq);
                    String str20 = minFreq;
                    String minFreq2 = minFreq.trim().replace(" ", "");
                    parameterInfo2.setMinFreq(Integer.parseInt(minFreq2));
                    String str21 = minFreq2;
                } else {
                    String str22 = minFreq;
                }
                String maxBssidNum = properties.getProperty("maxBssidNum", null);
                if (maxBssidNum != null) {
                    LogUtil.d(" MAXBSSIDNUM :  " + maxBssidNum);
                    String str23 = maxBssidNum;
                    parameterInfo2.setMaxBssidNum(Integer.parseInt(maxBssidNum.trim().replace(" ", "")));
                }
            } catch (NumberFormatException e) {
                LogUtil.e("ParamManager_ReadConfigFile_MachineLearning:" + e.getMessage());
            } catch (Exception e2) {
                LogUtil.e("ParamManager_ReadConfigFile_MachineLearning:" + e2.getMessage());
            }
        }
    }

    private void ParamManager_ReadConfigFile_Aging(ParameterInfo param, Properties props) {
        if (props != null) {
            try {
                String checkAgingAcumlteCount = props.getProperty("checkAgingAcumlteCount", null);
                if (checkAgingAcumlteCount != null) {
                    LogUtil.d(" checkAgingAcumlteCount :  " + checkAgingAcumlteCount);
                    param.setCheckAgingAcumlteCount(Integer.parseInt(checkAgingAcumlteCount.trim().replace(" ", "")));
                }
                String minMainAPOccUpd = props.getProperty("minMainAPOccUpd", null);
                if (minMainAPOccUpd != null) {
                    LogUtil.d(" minMainAPOccUpd :  " + minMainAPOccUpd);
                    param.setMinMainAPOccUpd(Float.parseFloat(minMainAPOccUpd.trim().replace(" ", "")));
                }
                String minUkwnRatioUpd = props.getProperty("minUkwnRatioUpd", null);
                if (minUkwnRatioUpd != null) {
                    LogUtil.d(" minUkwnRatioUpd :  " + minUkwnRatioUpd);
                    param.setMinUkwnRatioUpd(Float.parseFloat(minUkwnRatioUpd.trim().replace(" ", "")));
                }
                String minTrainBssiLstLenUpd = props.getProperty("minTrainBssiLstLenUpd", null);
                if (minTrainBssiLstLenUpd != null) {
                    LogUtil.d(" minTrainBssiLstLenUpd :  " + minTrainBssiLstLenUpd);
                    param.setMinTrainBssiLstLenUpd(Integer.parseInt(minTrainBssiLstLenUpd.trim().replace(" ", "")));
                }
                String abnMacRatioAllowUpd = props.getProperty("abnMacRatioAllowUpd", null);
                if (abnMacRatioAllowUpd != null) {
                    LogUtil.d(" abnMacRatioAllowUpd :  " + abnMacRatioAllowUpd);
                    param.setAbnMacRatioAllowUpd(Float.parseFloat(abnMacRatioAllowUpd.trim().replace(" ", "")));
                }
            } catch (NumberFormatException e) {
                LogUtil.e("ParamManager_ReadConfigFile_Aging:" + e.getMessage());
            } catch (Exception e2) {
                LogUtil.e("ParamManager_ReadConfigFile_Aging:" + e2.getMessage());
            }
        }
    }

    private void ParamManager_ReadConfigFile_Discrimitive(ParameterInfo param, Properties props) {
        ParameterInfo parameterInfo2 = param;
        Properties properties = props;
        if (properties != null) {
            try {
                String minUnkwnRatio = properties.getProperty("minUnkwnRatio", null);
                if (minUnkwnRatio != null) {
                    LogUtil.d(" MINUNKWNRATIO :  " + minUnkwnRatio);
                    parameterInfo2.setMinUnkwnRatio(Float.parseFloat(minUnkwnRatio.trim().replace(" ", "")));
                }
                String maxShatterRatio = properties.getProperty("maxShatterRatio", null);
                if (maxShatterRatio != null) {
                    LogUtil.d(" maxShatterRatio :  " + maxShatterRatio);
                    parameterInfo2.setMaxShatterRatio(Float.parseFloat(maxShatterRatio.trim().replace(" ", "")));
                }
                String totalShatterRatio = properties.getProperty("totalShatterRatio", null);
                if (totalShatterRatio != null) {
                    LogUtil.d(" totalShatterRatio :  " + totalShatterRatio);
                    parameterInfo2.setTotalShatterRatio(Float.parseFloat(totalShatterRatio.trim().replace(" ", "")));
                }
                String MaxDistDecayRatio = properties.getProperty("MaxDistDecayRatio", null);
                if (MaxDistDecayRatio != null) {
                    String str = TAG;
                    Log.d(str, " MAXDISTDECAYRATIO :  " + MaxDistDecayRatio);
                    parameterInfo2.setMaxDistDecayRatio(Float.parseFloat(MaxDistDecayRatio.trim().replace(" ", "")));
                }
                String acumlteCount = properties.getProperty("acumlteCount", null);
                if (acumlteCount != null) {
                    LogUtil.d(" acumlteCount :  " + acumlteCount);
                    parameterInfo2.setAcumlteCount(Integer.parseInt(acumlteCount.trim().replace(" ", "")));
                }
                String maxDistMinLimit = properties.getProperty("maxDistMinLimit", null);
                if (maxDistMinLimit != null) {
                    LogUtil.d(" MAXDISTMINLIMIT :  " + maxDistMinLimit);
                    parameterInfo2.setMaxDistMinLimit(Float.parseFloat(maxDistMinLimit.trim().replace(" ", "")));
                }
                String testDataRatio = properties.getProperty("testDataRatio", null);
                if (testDataRatio != null) {
                    LogUtil.d(" TESTDATARATIO :  " + testDataRatio);
                    parameterInfo2.setTestDataRatio(Float.parseFloat(testDataRatio.trim().replace(" ", "")));
                }
                String minModelTypes = properties.getProperty("minModelTypes", null);
                if (minModelTypes != null) {
                    LogUtil.d(" minModelTypes :  " + minModelTypes);
                    parameterInfo2.setMinModelTypes(Integer.parseInt(minModelTypes.trim().replace(" ", "")));
                }
                String trainDatasSize = properties.getProperty("TrainDatasSize", null);
                if (trainDatasSize != null) {
                    LogUtil.d(" TRAINDATASSIZE :  " + trainDatasSize);
                    parameterInfo2.setTrainDatasSize(Integer.parseInt(trainDatasSize.trim().replace(" ", "")));
                }
                String testDataSize = properties.getProperty("TestDataSize", null);
                if (testDataSize != null) {
                    LogUtil.d(" TESTDATASIZE :  " + testDataSize);
                    parameterInfo2.setTestDataSize(Integer.parseInt(testDataSize.trim().replace(" ", "")));
                }
                String testDataCnt = properties.getProperty("TestDataCnt", null);
                if (testDataCnt != null) {
                    LogUtil.d(" TESTDATACNT :  " + testDataCnt);
                    parameterInfo2.setTestDataCnt(Integer.parseInt(testDataCnt.trim().replace(" ", "")));
                }
            } catch (NumberFormatException e) {
                LogUtil.e("ParamManager_ReadConfigFile_Discrimitive:" + e.getMessage());
            } catch (Exception e2) {
                LogUtil.e("ParamManager_ReadConfigFile_Discrimitive:" + e2.getMessage());
            }
        }
    }

    private ParameterInfo ParamManager_ReadConfigFile(Properties props, boolean isMainAp) {
        Properties properties = props;
        ParameterInfo parameterInfo2 = new ParameterInfo(isMainAp);
        if (properties == null) {
            return parameterInfo2;
        }
        try {
            String ShowLog = properties.getProperty("ShowLog", null);
            if (ShowLog != null) {
                String str = TAG;
                Log.d(str, " SHOWLOG :  " + ShowLog);
                ShowLog = ShowLog.trim().replace(" ", "");
                if (ShowLog.equalsIgnoreCase("true")) {
                    LogUtil.setShowI(true);
                    LogUtil.setShowV(true);
                    LogUtil.setDebug_flag(true);
                    ShowToast.setIsShow(true);
                } else {
                    if (parameterInfo2.isBetaUser()) {
                        LogUtil.setShowI(true);
                    } else {
                        LogUtil.setShowI(false);
                    }
                    LogUtil.setDebug_flag(false);
                    ShowToast.setIsShow(false);
                }
            }
            String config_ver = properties.getProperty("config_ver", null);
            if (config_ver != null) {
                LogUtil.d(" CONFIG_VER :  " + config_ver);
                config_ver = config_ver.trim().replace(" ", "");
                parameterInfo2.setConfig_ver(Integer.parseInt(config_ver));
            }
            String fg_batch_num = properties.getProperty("fg_batch_num", null);
            if (fg_batch_num != null) {
                LogUtil.d(" FG_BATCH_NUM :  " + fg_batch_num);
                fg_batch_num = fg_batch_num.trim().replace(" ", "");
                parameterInfo2.setFg_batch_num(Integer.parseInt(fg_batch_num));
            }
            String activeSample = properties.getProperty("activeSample", null);
            if (activeSample != null) {
                LogUtil.d(" ACTIVESAMPLE :  " + activeSample);
                activeSample = activeSample.trim().replace(" ", "");
                parameterInfo2.setActiveSample(Integer.parseInt(activeSample));
            }
            String batchID = properties.getProperty("batchID", null);
            if (batchID != null) {
                LogUtil.d(" BATCHID :  " + batchID);
                parameterInfo2.setBatchID(Integer.parseInt(batchID.trim().replace(" ", "")));
            }
            String bssidStart = properties.getProperty("bssidStart", null);
            if (bssidStart != null) {
                LogUtil.d(" BSSIDSTART :  " + bssidStart);
                parameterInfo2.setBssidStart(Integer.parseInt(bssidStart.trim().replace(" ", "")));
            }
            String cellIDStart = properties.getProperty("cellIDStart", null);
            if (cellIDStart != null) {
                LogUtil.d(" CELLIDSTART :  " + cellIDStart);
                parameterInfo2.setCellIDStart(Integer.parseInt(cellIDStart.trim().replace(" ", "")));
            }
            String writeFileSample = properties.getProperty("writeFileSample", null);
            if (writeFileSample != null) {
                LogUtil.d(" WRITEFILESAMPLE :  " + writeFileSample);
                parameterInfo2.setWriteFileSample(Long.parseLong(writeFileSample.trim().replace(" ", "")));
            }
            String WIFI_DATA_SAMPLE = properties.getProperty("WIFI_DATA_SAMPLE", null);
            if (WIFI_DATA_SAMPLE != null) {
                LogUtil.d(" WIFI_DATA_SAMPLE :  " + WIFI_DATA_SAMPLE);
                parameterInfo2.setWifiDataSample(Integer.parseInt(WIFI_DATA_SAMPLE.trim().replace(" ", "")));
            }
            String Timestamp = properties.getProperty("Timestamp", null);
            if (Timestamp != null) {
                LogUtil.d(" TIMESTAMP :  " + Timestamp);
                parameterInfo2.setTimestamp(Integer.parseInt(Timestamp.trim().replace(" ", "")));
            }
            String ScanWifiStart = properties.getProperty("ScanWifiStart", null);
            if (ScanWifiStart != null) {
                LogUtil.d(" SCANWIFISTART :  " + ScanWifiStart);
                String str2 = ShowLog;
                ScanWifiStart = ScanWifiStart.trim().replace(" ", "");
                parameterInfo2.setScanWifiStart(Integer.parseInt(ScanWifiStart));
            }
            String ScanSSID = properties.getProperty("ScanSSID", null);
            if (ScanSSID != null) {
                LogUtil.d(" SCANSSID :  " + ScanSSID);
                String str3 = ScanSSID;
                ScanSSID = ScanSSID.trim().replace(" ", "");
                parameterInfo2.setScanSSID(Integer.parseInt(ScanSSID));
            } else {
                String str4 = ScanSSID;
            }
            String ScanMAC = properties.getProperty("ScanMAC", null);
            if (ScanMAC != null) {
                StringBuilder sb = new StringBuilder();
                String str5 = ScanSSID;
                sb.append(" SCANMAC :  ");
                sb.append(ScanMAC);
                LogUtil.d(sb.toString());
                parameterInfo2.setScanMAC(Integer.parseInt(ScanMAC.trim().replace(" ", "")));
            }
            String ScanRSSI = properties.getProperty("ScanRSSI", null);
            if (ScanRSSI != null) {
                LogUtil.d(" SCANRSSI :  " + ScanRSSI);
                String str6 = ScanRSSI;
                ScanRSSI = ScanRSSI.trim().replace(" ", "");
                parameterInfo2.setScanRSSI(Integer.parseInt(ScanRSSI));
            } else {
                String str7 = ScanRSSI;
            }
            String ScanCH = properties.getProperty("ScanCH", null);
            if (ScanCH != null) {
                StringBuilder sb2 = new StringBuilder();
                String str8 = ScanRSSI;
                sb2.append(" SCANCH :  ");
                sb2.append(ScanCH);
                LogUtil.d(sb2.toString());
                String str9 = ScanCH;
                ScanCH = ScanCH.trim().replace(" ", "");
                parameterInfo2.setScanCH(Integer.parseInt(ScanCH));
            } else {
                String str10 = ScanCH;
            }
            String ServingWiFiRSSI = properties.getProperty("ServingWiFiRSSI", null);
            if (ServingWiFiRSSI != null) {
                StringBuilder sb3 = new StringBuilder();
                String str11 = ScanCH;
                sb3.append(" SERVINGWIFIRSSI :  ");
                sb3.append(ServingWiFiRSSI);
                LogUtil.d(sb3.toString());
                String str12 = ServingWiFiRSSI;
                ServingWiFiRSSI = ServingWiFiRSSI.trim().replace(" ", "");
                parameterInfo2.setServingWiFiRSSI(Integer.parseInt(ServingWiFiRSSI));
            } else {
                String str13 = ServingWiFiRSSI;
                String str14 = ScanCH;
            }
            String ServingWiFiMAC = properties.getProperty("ServingWiFiMAC", null);
            if (ServingWiFiMAC != null) {
                StringBuilder sb4 = new StringBuilder();
                String str15 = ServingWiFiRSSI;
                sb4.append(" SERVINGWIFIMAC :  ");
                sb4.append(ServingWiFiMAC);
                LogUtil.d(sb4.toString());
                String str16 = ServingWiFiMAC;
                String ServingWiFiMAC2 = ServingWiFiMAC.trim().replace(" ", "");
                parameterInfo2.setServingWiFiMAC(Integer.parseInt(ServingWiFiMAC2));
                String str17 = ServingWiFiMAC2;
            } else {
                String str18 = ServingWiFiMAC;
            }
            String userPreferEnabled = properties.getProperty("userPreferEnabled", null);
            if (userPreferEnabled != null) {
                LogUtil.d(" userPreferEnabled :  " + userPreferEnabled);
                String str19 = userPreferEnabled;
                String userPreferEnabled2 = userPreferEnabled.trim().replace(" ", "");
                parameterInfo2.setUserPreferEnable(Boolean.parseBoolean(userPreferEnabled2));
                String str20 = userPreferEnabled2;
            } else {
                String str21 = userPreferEnabled;
            }
            String userPrefStartTimes = properties.getProperty("UserPrefStartTimes", null);
            if (userPrefStartTimes != null) {
                LogUtil.d(" USERPREFSTARTTIMES :  " + userPrefStartTimes);
                String str22 = userPrefStartTimes;
                String userPrefStartTimes2 = userPrefStartTimes.trim().replace(" ", "");
                parameterInfo2.setUserPrefStartTimes(Integer.parseInt(userPrefStartTimes2));
                String str23 = userPrefStartTimes2;
            } else {
                String str24 = userPrefStartTimes;
            }
            String userPrefStartDuration = properties.getProperty("UserPrefStartDuration", null);
            if (userPrefStartDuration != null) {
                LogUtil.d(" USERPREFSTARTDURATION :  " + userPrefStartDuration);
                String str25 = userPrefStartDuration;
                String userPrefStartDuration2 = userPrefStartDuration.trim().replace(" ", "");
                String str26 = ScanWifiStart;
                String str27 = config_ver;
                parameterInfo2.setUserPrefStartDuration(Long.parseLong(userPrefStartDuration2));
                String str28 = userPrefStartDuration2;
            } else {
                String str29 = userPrefStartDuration;
                String str30 = ScanWifiStart;
                String str31 = config_ver;
            }
            String userPrefFreqRatio = properties.getProperty("UserPrefFreqRatio", null);
            if (userPrefFreqRatio != null) {
                LogUtil.d(" USERPREFFREQRATIO :  " + userPrefFreqRatio);
                userPrefFreqRatio = userPrefFreqRatio.trim().replace(" ", "");
                parameterInfo2.setUserPrefFreqRatio(Float.parseFloat(userPrefFreqRatio));
            }
            String userPrefDurationRatio = properties.getProperty("UserPrefDurationRatio", null);
            if (userPrefDurationRatio != null) {
                LogUtil.d(" USERPREFDURATIONRATIO :  " + userPrefDurationRatio);
                userPrefDurationRatio = userPrefDurationRatio.trim().replace(" ", "");
                parameterInfo2.setUserPrefDurationRatio(Double.parseDouble(userPrefDurationRatio));
            }
            String powerPreferEnabled = properties.getProperty("powerPreferEnabled", null);
            if (powerPreferEnabled != null) {
                LogUtil.d(" powerPreferEnabled :  " + powerPreferEnabled);
                String str32 = userPrefFreqRatio;
                parameterInfo2.setPowerPreferEnable(Boolean.parseBoolean(powerPreferEnabled.trim().replace(" ", "")));
            }
            String powerSaveType = properties.getProperty("PowerSaveType", null);
            if (powerSaveType != null) {
                LogUtil.d(" POWERSAVETYPE :  " + powerSaveType);
                String str33 = powerSaveType;
                powerSaveType = powerSaveType.trim().replace(" ", "");
                parameterInfo2.setPowerSaveType(Integer.parseInt(powerSaveType));
            } else {
                String str34 = powerSaveType;
            }
            String powerSaveGap = properties.getProperty("PowerSaveGap", null);
            if (powerSaveGap != null) {
                StringBuilder sb5 = new StringBuilder();
                String str35 = powerSaveType;
                sb5.append(" POWERSAVEGAP :  ");
                sb5.append(powerSaveGap);
                LogUtil.d(sb5.toString());
                String str36 = userPrefDurationRatio;
                String str37 = fg_batch_num;
                String str38 = activeSample;
                parameterInfo2.setPowerSaveGap(Double.parseDouble(powerSaveGap.trim().replace(" ", "")));
            } else {
                String str39 = userPrefDurationRatio;
                String str40 = fg_batch_num;
                String str41 = activeSample;
            }
            String back4GEnabled = properties.getProperty("back4GEnabled", null);
            if (back4GEnabled != null) {
                LogUtil.d(" back4GEnabled :  " + back4GEnabled);
                back4GEnabled = back4GEnabled.trim().replace(" ", "");
                parameterInfo2.setBack4GEnable(Boolean.parseBoolean(back4GEnabled));
            }
            String out4GInterval = properties.getProperty("out4GInterval", null);
            if (out4GInterval != null) {
                LogUtil.d(" out4GInterval :  " + out4GInterval);
                out4GInterval = out4GInterval.trim().replace(" ", "");
                parameterInfo2.setBack4GTH_out4G_interval(Integer.parseInt(out4GInterval));
            }
            String back4GDurationMin = properties.getProperty("back4GDurationMin", null);
            if (back4GDurationMin != null) {
                LogUtil.d(" back4GDurationMin :  " + back4GDurationMin);
                String str42 = back4GEnabled;
                parameterInfo2.setBack4GTH_duration_min(Integer.parseInt(back4GDurationMin.trim().replace(" ", "")));
            }
            String back4GSignalMin = properties.getProperty("back4GSignalMin", null);
            if (back4GSignalMin != null) {
                LogUtil.d(" back4GSignalMin :  " + back4GSignalMin);
                String str43 = back4GSignalMin;
                back4GSignalMin = back4GSignalMin.trim().replace(" ", "");
                parameterInfo2.setBack4GTH_signal_min(Integer.parseInt(back4GSignalMin));
            } else {
                String str44 = back4GSignalMin;
            }
            String back4GRatio = properties.getProperty("back4GRatio", null);
            if (back4GRatio != null) {
                StringBuilder sb6 = new StringBuilder();
                String str45 = back4GSignalMin;
                sb6.append(" back4GRatio :  ");
                sb6.append(back4GRatio);
                LogUtil.d(sb6.toString());
                String str46 = out4GInterval;
                parameterInfo2.setBack4GTH_duration_4gRatio(Double.parseDouble(back4GRatio.trim().replace(" ", "")));
            }
        } catch (NumberFormatException e) {
            LogUtil.e("ParamManager_ReadConfigFile:" + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("ParamManager_ReadConfigFile:" + e2.getMessage());
        }
        return parameterInfo2;
    }
}
