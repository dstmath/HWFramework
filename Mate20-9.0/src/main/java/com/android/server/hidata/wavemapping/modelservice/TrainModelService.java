package com.android.server.hidata.wavemapping.modelservice;

import android.util.Log;
import com.android.server.hidata.HwHidataJniAdapter;
import com.android.server.hidata.wavemapping.chr.entity.ApChrStatInfo;
import com.android.server.hidata.wavemapping.chr.entity.BuildModelChrInfo;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dao.EnterpriseApDAO;
import com.android.server.hidata.wavemapping.dao.MobileApDAO;
import com.android.server.hidata.wavemapping.entity.ApInfo;
import com.android.server.hidata.wavemapping.entity.CoreTrainData;
import com.android.server.hidata.wavemapping.entity.MobileApCheckParamInfo;
import com.android.server.hidata.wavemapping.entity.ModelInfo;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.entity.StdDataSet;
import com.android.server.hidata.wavemapping.entity.StdRecord;
import com.android.server.hidata.wavemapping.entity.TMapList;
import com.android.server.hidata.wavemapping.entity.TMapSet;
import com.android.server.hidata.wavemapping.util.FileUtils;
import com.android.server.hidata.wavemapping.util.GetStd;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TrainModelService extends ModelBaseService {
    public static final String TAG = ("WMapping." + TrainModelService.class.getSimpleName());
    private EnterpriseApDAO enterpriseApDAO = new EnterpriseApDAO();
    private MobileApDAO mobileApDAO = new MobileApDAO();

    public ModelInfo loadModel(ParameterInfo param, RegularPlaceInfo placeInfo) {
        String modelName;
        int lineLen;
        String fPath;
        int headerLen;
        String[] bssids;
        StringBuilder sb;
        String[] bssids2;
        ParameterInfo parameterInfo = param;
        RegularPlaceInfo regularPlaceInfo = placeInfo;
        if (parameterInfo == null) {
            LogUtil.d("param == null");
            return null;
        } else if (regularPlaceInfo == null) {
            LogUtil.d("placeInfo == null");
            return null;
        } else if (placeInfo.getPlace() == null) {
            LogUtil.d("place == null");
            return null;
        } else {
            String place = placeInfo.getPlace();
            LogUtil.i(" loadModel begin:" + modelName);
            String fPath2 = getModelFilePath(regularPlaceInfo, parameterInfo);
            String fileContent = FileUtils.getFileContent(fPath2);
            if (((long) fileContent.length()) > Constant.MAX_FILE_SIZE) {
                LogUtil.d("loadModel ,file content is too bigger than max_file_size.");
                return null;
            }
            ModelInfo modelInfo = new ModelInfo(place, placeInfo.getModelName());
            try {
                String[] lines = fileContent.split(Constant.getLineSeperate());
                if (lines.length < 1) {
                    try {
                        LogUtil.e("Failure loadModel " + modelName + ",lines length is zero.");
                        return null;
                    } catch (RuntimeException e) {
                        e = e;
                        String str = fPath2;
                        LogUtil.e("LocatingState,e" + e.getMessage());
                        LogUtil.d(" loadModel success, place :" + place + ",modelName:" + modelName);
                        return modelInfo;
                    } catch (Exception e2) {
                        e = e2;
                        String str2 = fPath2;
                        LogUtil.e("LocatingState,e" + e.getMessage());
                        return null;
                    }
                } else {
                    String[] headers = lines[0].split(",");
                    HashSet hashSet = new HashSet();
                    int bssidsLen = headers.length - 0;
                    String[] bssids3 = new String[bssidsLen];
                    int headerLen2 = headers.length;
                    if (0 >= headers.length) {
                        return null;
                    }
                    int i = 0;
                    while (true) {
                        int i2 = i;
                        if (i2 >= headers.length) {
                            break;
                        }
                        try {
                            hashSet.add(headers[i2]);
                            bssids2 = bssids3;
                            try {
                                bssids2[i2 - 0] = headers[i2];
                            } catch (RuntimeException e3) {
                                e = e3;
                            } catch (Exception e4) {
                                e = e4;
                                LogUtil.e("loadModel,e" + e.getMessage());
                                i = i2 + 1;
                                bssids3 = bssids2;
                                ParameterInfo parameterInfo2 = param;
                                RegularPlaceInfo regularPlaceInfo2 = placeInfo;
                            }
                        } catch (RuntimeException e5) {
                            e = e5;
                            bssids2 = bssids3;
                            LogUtil.e("loadModel,e" + e.getMessage());
                            i = i2 + 1;
                            bssids3 = bssids2;
                            ParameterInfo parameterInfo22 = param;
                            RegularPlaceInfo regularPlaceInfo22 = placeInfo;
                        } catch (Exception e6) {
                            e = e6;
                            bssids2 = bssids3;
                            LogUtil.e("loadModel,e" + e.getMessage());
                            i = i2 + 1;
                            bssids3 = bssids2;
                            ParameterInfo parameterInfo222 = param;
                            RegularPlaceInfo regularPlaceInfo222 = placeInfo;
                        }
                        i = i2 + 1;
                        bssids3 = bssids2;
                        ParameterInfo parameterInfo2222 = param;
                        RegularPlaceInfo regularPlaceInfo2222 = placeInfo;
                    }
                    String[] bssids4 = bssids3;
                    if (hashSet.size() != bssidsLen) {
                        LogUtil.e("loadModel,Failure loadModel " + modelName + ",has duplicate bssid.");
                        return null;
                    }
                    modelInfo.setSetBssids(hashSet);
                    modelInfo.setBssidLst(bssids4);
                    int[][] datas = (int[][]) Array.newInstance(int.class, new int[]{lines.length - 1, bssids4.length});
                    int i3 = 1;
                    while (true) {
                        int i4 = i3;
                        if (i4 >= lines.length) {
                            break;
                        }
                        try {
                            bssids = bssids4;
                            try {
                                String[] arrWords = lines[i4].split(",");
                                int headerLen3 = headerLen2;
                                if (arrWords.length < headerLen3) {
                                    try {
                                        sb = new StringBuilder();
                                        headerLen = headerLen3;
                                    } catch (NumberFormatException e7) {
                                        e = e7;
                                        headerLen = headerLen3;
                                        fPath = fPath2;
                                        try {
                                            LogUtil.e("LocatingState,e" + e.getMessage());
                                            i3 = i4 + 1;
                                            bssids4 = bssids;
                                            headerLen2 = headerLen;
                                            fPath2 = fPath;
                                        } catch (RuntimeException e8) {
                                            e = e8;
                                            LogUtil.e("LocatingState,e" + e.getMessage());
                                            LogUtil.d(" loadModel success, place :" + place + ",modelName:" + modelName);
                                            return modelInfo;
                                        } catch (Exception e9) {
                                            e = e9;
                                            LogUtil.e("LocatingState,e" + e.getMessage());
                                            return null;
                                        }
                                    }
                                    try {
                                        sb.append("loadModel,Load Model failure,place :");
                                        sb.append(place);
                                        sb.append(",modelName:");
                                        sb.append(modelName);
                                        sb.append(", line num:");
                                        sb.append(i4);
                                        LogUtil.e(sb.toString());
                                        fPath = fPath2;
                                    } catch (NumberFormatException e10) {
                                        e = e10;
                                        fPath = fPath2;
                                        LogUtil.e("LocatingState,e" + e.getMessage());
                                        i3 = i4 + 1;
                                        bssids4 = bssids;
                                        headerLen2 = headerLen;
                                        fPath2 = fPath;
                                    }
                                    i3 = i4 + 1;
                                    bssids4 = bssids;
                                    headerLen2 = headerLen;
                                    fPath2 = fPath;
                                } else {
                                    headerLen = headerLen3;
                                    int k = 0;
                                    while (k < arrWords.length) {
                                        try {
                                            fPath = fPath2;
                                        } catch (NumberFormatException e11) {
                                            e = e11;
                                            fPath = fPath2;
                                            LogUtil.e("LocatingState,e" + e.getMessage());
                                            i3 = i4 + 1;
                                            bssids4 = bssids;
                                            headerLen2 = headerLen;
                                            fPath2 = fPath;
                                        }
                                        try {
                                            datas[i4 - 1][k - 0] = Integer.parseInt(arrWords[k]);
                                            k++;
                                            fPath2 = fPath;
                                        } catch (NumberFormatException e12) {
                                            e = e12;
                                            LogUtil.e("LocatingState,e" + e.getMessage());
                                            i3 = i4 + 1;
                                            bssids4 = bssids;
                                            headerLen2 = headerLen;
                                            fPath2 = fPath;
                                        }
                                    }
                                    fPath = fPath2;
                                    i3 = i4 + 1;
                                    bssids4 = bssids;
                                    headerLen2 = headerLen;
                                    fPath2 = fPath;
                                }
                            } catch (NumberFormatException e13) {
                                e = e13;
                                fPath = fPath2;
                                headerLen = headerLen2;
                                LogUtil.e("LocatingState,e" + e.getMessage());
                                i3 = i4 + 1;
                                bssids4 = bssids;
                                headerLen2 = headerLen;
                                fPath2 = fPath;
                            }
                        } catch (NumberFormatException e14) {
                            e = e14;
                            bssids = bssids4;
                            fPath = fPath2;
                            headerLen = headerLen2;
                            LogUtil.e("LocatingState,e" + e.getMessage());
                            i3 = i4 + 1;
                            bssids4 = bssids;
                            headerLen2 = headerLen;
                            fPath2 = fPath;
                        }
                    }
                    String str3 = fPath2;
                    int i5 = headerLen2;
                    if (datas.length == 0) {
                        LogUtil.d(" loadModel failure:datas.length = 0");
                        return null;
                    }
                    LogUtil.d(" loadModel,place :" + place + ",modelName:" + modelName + ",datas.size : " + datas.length);
                    modelInfo.setDatas(datas);
                    modelInfo.setDataLen(lineLen + -1);
                    LogUtil.d(" loadModel success, place :" + place + ",modelName:" + modelName);
                    return modelInfo;
                }
            } catch (RuntimeException e15) {
                e = e15;
                String str4 = fPath2;
                LogUtil.e("LocatingState,e" + e.getMessage());
                LogUtil.d(" loadModel success, place :" + place + ",modelName:" + modelName);
                return modelInfo;
            } catch (Exception e16) {
                e = e16;
                String str5 = fPath2;
                LogUtil.e("LocatingState,e" + e.getMessage());
                return null;
            }
        }
    }

    public boolean saveModel(String place, String[] reLst, ParameterInfo param, RegularPlaceInfo placeInfo) {
        StringBuilder fContent = new StringBuilder();
        if (place == null) {
            LogUtil.d(" saveModel place == null");
            return false;
        } else if (reLst == null) {
            LogUtil.d(" saveModel reLst == null");
            return false;
        } else if (reLst.length == 0) {
            LogUtil.d(" saveModel reLst.length == 0");
            return false;
        } else {
            String fName = "";
            try {
                String fName2 = place.replace(":", "").replace("-", "") + "." + placeInfo.getModelName();
                if (!FileUtils.delFile(getModelFilePath(placeInfo, param))) {
                    LogUtil.i(" saveModel ,FileUtils.delFile(filePath),filePath:" + filePath);
                }
                placeInfo.setModelName(new TimeUtil().getTimePATTERN02());
                fName = place.replace(":", "").replace("-", "") + "." + placeInfo.getModelName();
                LogUtil.i(" saveModel begin:" + fName);
                String filePath = getModelFilePath(placeInfo, param);
                int length = reLst.length;
                for (int i = 0; i < length; i++) {
                    fContent.append(reLst[i]);
                    fContent.append(Constant.getLineSeperate());
                }
                if (!FileUtils.saveFile(filePath, fContent.toString())) {
                    LogUtil.d("Failure save model " + place);
                    return false;
                }
            } catch (RuntimeException e) {
                LogUtil.e("LocatingState,e" + e.getMessage());
            } catch (Exception e2) {
                LogUtil.e(" saveModel ," + e2.getMessage());
                return false;
            }
            LogUtil.d("Success save model " + fName);
            return true;
        }
    }

    public CoreTrainData getWmpCoreTrainData(String place, ParameterInfo param, RegularPlaceInfo placeInfo, BuildModelChrInfo buildModelChrInfo) {
        CoreTrainData coreTrainData = new CoreTrainData();
        if (place == null || place.equals("")) {
            return coreTrainData;
        }
        LogUtil.i(" trainModelRe begin,place:" + place + ",isMain:" + param.isMainAp());
        try {
            int spletRet = splitTrainTestFiles(place, param, buildModelChrInfo);
            if (spletRet < 0) {
                LogUtil.d("splitTrainTestFiles failure.");
                coreTrainData.setResult(spletRet);
                return coreTrainData;
            }
            int result = transformRawData(place, param, buildModelChrInfo);
            coreTrainData.setResult(result);
            if (result < 0) {
                LogUtil.d("getWmpCoreTrainData transformRawData failure");
                LogUtil.d(" getWmpCoreTrainData end,place:" + place + ",result=" + coreTrainData.getResult() + ",isMain:" + param.isMainAp());
                return coreTrainData;
            }
            if (getWmpCoreTrainDataByStdFile(place, param, coreTrainData)) {
                return coreTrainData;
            }
            LogUtil.d(" getWmpCoreTrainData end,place:" + place + ",result=" + coreTrainData.getResult() + ",isMain:" + param.isMainAp());
            return coreTrainData;
        } catch (RuntimeException e) {
            LogUtil.e("getWmpCoreTrainData,e" + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e(" getWmpCoreTrainData error,place:" + e2.getMessage());
        }
    }

    public boolean getWmpCoreTrainDataByStdFile(String place, ParameterInfo param, CoreTrainData coreTrainData) {
        String fileContent = FileUtils.getFileContent(getStdFilePath(place, param));
        if (fileContent.length() == 0) {
            coreTrainData.setResult(-15);
            return true;
        } else if (((long) fileContent.length()) > Constant.MAX_FILE_SIZE) {
            LogUtil.d("getWmpCoreTrainDataByStdFile ,file content is too bigger than max_file_size.");
            coreTrainData.setResult(-16);
            return true;
        } else {
            String[] fingerData = fileContent.split(Constant.getLineSeperate());
            LogUtil.d(" getWmpCoreTrainDataByStdFile fingerData.length:" + fingerData.length + ",isMain:" + param.isMainAp());
            if (fingerData.length < param.getTrainDatasSize()) {
                LogUtil.d(" getWmpCoreTrainDataByStdFile fingerData.load train file file line length less than MIN VAL." + fingerData.length + ",isMain:" + param.isMainAp());
                coreTrainData.setResult(-17);
                return true;
            }
            String[] lineParam = param.toLineStr();
            LogUtil.i("getWmpCoreTrainDataByStdFile:" + place + ",fingerData.size:" + fingerData.length + ",isMain:" + param.isMainAp());
            try {
                String[] reLst = HwHidataJniAdapter.getInstance().getWmpCoreTrainData(fingerData, lineParam).split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                int length = reLst.length;
                coreTrainData.setDatas(reLst);
            } catch (RuntimeException e) {
                LogUtil.e("getWmpCoreTrainDataByStdFile,e" + e.getMessage());
            } catch (Exception e2) {
                LogUtil.e("getWmpCoreTrainDataByStdFile, " + e2.getMessage() + ",isMain:" + param.isMainAp());
            }
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0136, code lost:
        r18 = r0;
        r22 = r4;
     */
    private int splitTrainTestFiles(String place, ParameterInfo param, BuildModelChrInfo buildModelChrInfo) {
        int size;
        int testDataCntbyRatio;
        String str = place;
        BuildModelChrInfo buildModelChrInfo2 = buildModelChrInfo;
        try {
            String randomKey = getRawFilePath(place, param);
            String fileContent = FileUtils.getFileContent(randomKey);
            if (fileContent.length() == 0) {
                return -7;
            }
            if (fileContent.equals(FileUtils.ERROR_RET)) {
                return -20;
            }
            if (((long) fileContent.length()) > Constant.MAX_FILE_SIZE) {
                LogUtil.d("splitTrainTestFiles ,raw data file content is too bigger than max_file_size.file lenght:" + fileContent.length());
                return -8;
            }
            String[] lines = fileContent.split(Constant.getLineSeperate());
            int len = lines.length > param.getMaxRawDataCnt() ? param.getMaxRawDataCnt() : lines.length;
            if (len < param.getTestDataSize() + param.getTrainDatasSize()) {
                LogUtil.d("splitTrainTestFiles ,raw data file lines length(" + len + ") is less than train(" + param.getTrainDatasSize() + ") and test(" + param.getTestDataSize() + ") data size.");
                return -9;
            }
            HashMap<String, AtomicInteger> hpBatchStat = new HashMap<>();
            List<String> batchLst = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                String[] wds = lines[i].split(",");
                if (wds.length >= param.getScanWifiStart()) {
                    if (wds[0] != null) {
                        if (!wds[0].equals("")) {
                            String strBatch = wds[param.getBatchID()];
                            if (hpBatchStat.containsKey(strBatch)) {
                                hpBatchStat.get(strBatch).incrementAndGet();
                            } else {
                                batchLst.add(strBatch);
                                hpBatchStat.put(strBatch, new AtomicInteger(1));
                            }
                        }
                    }
                }
            }
            int totalRdCnt = lines.length > param.getMaxRawDataCnt() ? param.getMaxRawDataCnt() : lines.length;
            int testDataCntbyRatio2 = (int) (((float) totalRdCnt) * param.getTestDataRatio());
            int testDataCnt = testDataCntbyRatio2 > param.getTestDataSize() ? testDataCntbyRatio2 : param.getTestDataSize();
            if (totalRdCnt <= testDataCnt) {
                LogUtil.d("splitTrainTestFiles ,total data size is less than test data size.");
                return -10;
            }
            Random random = new Random();
            Set<String> setTestBatch = new HashSet<>();
            int size2 = hpBatchStat.size();
            int testFetchDataCnt = 0;
            int i2 = 0;
            while (true) {
                size = size2;
                if (i2 < size) {
                    if (batchLst.isEmpty()) {
                        break;
                    } else if (testFetchDataCnt >= testDataCnt) {
                        break;
                    } else {
                        String filePath = randomKey;
                        int randomCnt = random.nextInt(batchLst.size());
                        int i3 = randomCnt;
                        String randomKey2 = (String) batchLst.get(randomCnt);
                        String fileContent2 = fileContent;
                        AtomicInteger randomValue = hpBatchStat.get(randomKey2);
                        if (randomValue != null) {
                            setTestBatch.add(randomKey2);
                            testFetchDataCnt += randomValue.intValue();
                            batchLst.remove(randomKey2);
                        }
                        i2++;
                        size2 = size;
                        randomKey = filePath;
                        fileContent = fileContent2;
                    }
                } else {
                    String filePath2 = randomKey;
                    String str2 = fileContent;
                    break;
                }
            }
            StringBuffer trainDataSb = new StringBuffer();
            StringBuffer testDataSb = new StringBuffer();
            boolean ifGetFirstDataTime = false;
            int i4 = testFetchDataCnt;
            TimeUtil timeUtil = new TimeUtil();
            int i5 = len;
            int testDataCount = 0;
            int i6 = 0;
            while (i6 < totalRdCnt) {
                HashMap<String, AtomicInteger> hpBatchStat2 = hpBatchStat;
                List<String> batchLst2 = batchLst;
                String[] wds2 = lines[i6].split(",");
                int size3 = size;
                if (wds2.length >= param.getScanWifiStart()) {
                    if (wds2[0] == null) {
                        testDataCntbyRatio = testDataCntbyRatio2;
                    } else if (!wds2[0].equals("")) {
                        String strBatch2 = wds2[param.getBatchID()];
                        if (!ifGetFirstDataTime) {
                            testDataCntbyRatio = testDataCntbyRatio2;
                            if (wds2.length > param.getTimestampID() && timeUtil.time2IntDate(wds2[param.getTimestampID()]) != 0) {
                                buildModelChrInfo2.setFirstTimeAll(timeUtil.time2IntDate(wds2[param.getTimestampID()]));
                                ifGetFirstDataTime = true;
                            }
                        } else {
                            testDataCntbyRatio = testDataCntbyRatio2;
                        }
                        if (setTestBatch.contains(strBatch2)) {
                            testDataSb.append(lines[i6]);
                            testDataSb.append(Constant.getLineSeperate());
                            testDataCount++;
                        } else {
                            trainDataSb.append(lines[i6]);
                            trainDataSb.append(Constant.getLineSeperate());
                        }
                    }
                    i6++;
                    hpBatchStat = hpBatchStat2;
                    batchLst = batchLst2;
                    size = size3;
                    testDataCntbyRatio2 = testDataCntbyRatio;
                }
                testDataCntbyRatio = testDataCntbyRatio2;
                i6++;
                hpBatchStat = hpBatchStat2;
                batchLst = batchLst2;
                size = size3;
                testDataCntbyRatio2 = testDataCntbyRatio;
            }
            List<String> list = batchLst;
            int i7 = size;
            int i8 = testDataCntbyRatio2;
            buildModelChrInfo2.setTestDataAll(testDataCount);
            buildModelChrInfo2.setTrainDataAll(totalRdCnt - testDataCount);
            LogUtil.d("chr trainData len:" + (totalRdCnt - testDataCount) + ",testData len:" + testDataCount);
            String trainDataFilePath = getTrainDataFilePath(place, param);
            String testDataFilePath = getTestDataFilePath(place, param);
            if (!FileUtils.delFile(trainDataFilePath)) {
                LogUtil.d(" splitTrainTestFiles failure ,FileUtils.delFile(trainDataFilePath),dataFilePath:" + trainDataFilePath);
                return -11;
            } else if (!FileUtils.saveFile(trainDataFilePath, trainDataSb.toString())) {
                Log.d(TAG, " splitTrainTestFiles save failure:" + str + ",trainDataFilePath:" + trainDataFilePath);
                return -12;
            } else if (!FileUtils.delFile(testDataFilePath)) {
                LogUtil.d(" splitTrainTestFiles failure ,FileUtils.delFile(testDataFilePath),testDataFilePath:" + testDataFilePath);
                return -13;
            } else {
                if (!FileUtils.saveFile(testDataFilePath, testDataSb.toString())) {
                    Log.d(TAG, " splitTrainTestFiles save failure:" + str + ",testDataFilePath:" + testDataFilePath);
                    return -14;
                }
                return 1;
            }
        } catch (RuntimeException e) {
            LogUtil.e("splitTrainTestFiles,e" + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("splitTrainTestFiles,e" + e2.getMessage());
        }
    }

    public int wmpCoreTrainData(CoreTrainData coreTrainData, String place, ParameterInfo param, RegularPlaceInfo placeInfo, BuildModelChrInfo buildModelChrInfo) {
        String[] lineParam;
        String str = place;
        ParameterInfo parameterInfo = param;
        RegularPlaceInfo regularPlaceInfo = placeInfo;
        if (coreTrainData == null) {
            Log.d(TAG, "wmpCoreTrainData coreTrainData == null");
            return -52;
        } else if (coreTrainData.getDatas() == null || coreTrainData.getDatas().length == 0) {
            Log.d(TAG, "wmpCoreTrainData coreTrainData == null");
            return -52;
        } else if (parameterInfo == null) {
            Log.d(TAG, "wmpCoreTrainData parameterInfo == null");
            return -1;
        } else if (regularPlaceInfo == null) {
            Log.d(TAG, "wmpCoreTrainData placeInfo == null");
            return -1;
        } else if (str == null || str.equals("")) {
            Log.d(TAG, "wmpCoreTrainData place == null || place.equals(\"\") ." + str);
            return -1;
        } else {
            String[] datas = param.toLineStr();
            LogUtil.i("wmpCoreTrainData:" + str + ",coreTrainData.size:" + coreTrainData.getDatas().length + ",isMain:" + param.isMainAp());
            Set<String> setPreLables = new HashSet<>();
            try {
                long startTime = System.currentTimeMillis();
                HwHidataJniAdapter hwHidataJniAdapter = HwHidataJniAdapter.getInstance();
                String[] reLst = hwHidataJniAdapter.wmpCoreTrainData(coreTrainData.getDatas(), datas).split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                int size = reLst.length;
                LogUtil.i("wmpCoreTrainData, size=" + size + ",spend(seconds) run time(s):" + ((System.currentTimeMillis() - startTime) / 1000));
                if (size < 2) {
                    return -17;
                }
                String[] headers = reLst[0].split(",");
                int macCnt = -1;
                int i = param.getBssidStart();
                while (true) {
                    HwHidataJniAdapter hwHidataJniAdapter2 = hwHidataJniAdapter;
                    if (i >= headers.length) {
                        break;
                    }
                    macCnt++;
                    i++;
                    hwHidataJniAdapter = hwHidataJniAdapter2;
                }
                LogUtil.i("macCnt=" + macCnt);
                buildModelChrInfo.getAPType().setFinalUsed(macCnt);
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 >= size) {
                        break;
                    }
                    String[] lineParam2 = datas;
                    try {
                        setPreLables.add(lineParam[reLst[i3].split(",").length - 1]);
                        i2 = i3 + 1;
                        datas = lineParam2;
                    } catch (RuntimeException e) {
                        e = e;
                        LogUtil.e("wmpCoreTrainData,e" + e.getMessage());
                        LogUtil.d("wMappingTrainData end:" + str + ",coreTrainData.size:" + coreTrainData.getDatas().length + ",setPreLables.size():" + setPreLables.size() + ",isMain:" + param.isMainAp());
                        return setPreLables.size();
                    } catch (Exception e2) {
                        e = e2;
                        LogUtil.e("wmpCoreTrainData, " + e.getMessage() + ",isMain:" + param.isMainAp());
                        LogUtil.d("wMappingTrainData end:" + str + ",coreTrainData.size:" + coreTrainData.getDatas().length + ",setPreLables.size():" + setPreLables.size() + ",isMain:" + param.isMainAp());
                        return setPreLables.size();
                    }
                }
                String[] lineParam3 = datas;
                setPreLables.remove("prelabel");
                try {
                    if (!saveModel(str, reLst, parameterInfo, regularPlaceInfo)) {
                        LogUtil.i("wmpCoreTrainData save model failure.");
                        return -18;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("train models wmpCoreTrainData spend(seconds) : ");
                    long j = startTime;
                    sb.append((System.currentTimeMillis() - startTime) / 1000);
                    LogUtil.d(sb.toString());
                    LogUtil.d("wMappingTrainData end:" + str + ",coreTrainData.size:" + coreTrainData.getDatas().length + ",setPreLables.size():" + setPreLables.size() + ",isMain:" + param.isMainAp());
                    return setPreLables.size();
                } catch (RuntimeException e3) {
                    e = e3;
                    LogUtil.e("wmpCoreTrainData,e" + e.getMessage());
                    LogUtil.d("wMappingTrainData end:" + str + ",coreTrainData.size:" + coreTrainData.getDatas().length + ",setPreLables.size():" + setPreLables.size() + ",isMain:" + param.isMainAp());
                    return setPreLables.size();
                } catch (Exception e4) {
                    e = e4;
                    LogUtil.e("wmpCoreTrainData, " + e.getMessage() + ",isMain:" + param.isMainAp());
                    LogUtil.d("wMappingTrainData end:" + str + ",coreTrainData.size:" + coreTrainData.getDatas().length + ",setPreLables.size():" + setPreLables.size() + ",isMain:" + param.isMainAp());
                    return setPreLables.size();
                }
            } catch (RuntimeException e5) {
                e = e5;
                String[] strArr = datas;
                LogUtil.e("wmpCoreTrainData,e" + e.getMessage());
                LogUtil.d("wMappingTrainData end:" + str + ",coreTrainData.size:" + coreTrainData.getDatas().length + ",setPreLables.size():" + setPreLables.size() + ",isMain:" + param.isMainAp());
                return setPreLables.size();
            } catch (Exception e6) {
                e = e6;
                String[] strArr2 = datas;
                LogUtil.e("wmpCoreTrainData, " + e.getMessage() + ",isMain:" + param.isMainAp());
                LogUtil.d("wMappingTrainData end:" + str + ",coreTrainData.size:" + coreTrainData.getDatas().length + ",setPreLables.size():" + setPreLables.size() + ",isMain:" + param.isMainAp());
                return setPreLables.size();
            }
        }
    }

    public String filterMobileAp(String place, ParameterInfo param) {
        String str = place;
        ParameterInfo parameterInfo = param;
        String filePath = getRawFilePath(place, param);
        String str2 = TAG;
        Log.d(str2, " filterMobileAp begin:" + str + ",filePath:" + filePath);
        ApChrStatInfo apChrStatInfo = new ApChrStatInfo();
        try {
            String fileContent = FileUtils.getFileContent(filePath);
            if (fileContent.equals("")) {
                try {
                    LogUtil.d("filterMobileAp ,null == fileContent.");
                    apChrStatInfo.setResult(-57);
                    return apChrStatInfo.toString();
                } catch (RuntimeException e) {
                    e = e;
                    String str3 = filePath;
                    LogUtil.e("filterMobileAp, " + e.getMessage() + ",isFilterMobileAp");
                    return apChrStatInfo.toString();
                } catch (Exception e2) {
                    e = e2;
                    String str4 = filePath;
                    LogUtil.e("filterMobileAp, " + e.getMessage() + ",isFilterMobileAp");
                    return apChrStatInfo.toString();
                }
            } else if (((long) fileContent.length()) > Constant.MAX_FILE_SIZE) {
                LogUtil.d("filterMobileAp ,file content is too bigger than max_file_size.");
                apChrStatInfo.setResult(-58);
                return apChrStatInfo.toString();
            } else {
                String[] lines = fileContent.split(Constant.getLineSeperate());
                if (lines.length < 2) {
                    LogUtil.d(" filterMobileAp read data,lines == null || lines.length < 2");
                    apChrStatInfo.setResult(-59);
                    return apChrStatInfo.toString();
                }
                Set<String> tempSetMacs = new HashSet<>();
                List<String> macLst = new ArrayList<>();
                int size = lines.length;
                Set<String> apInfoSet = this.enterpriseApDAO.findAll();
                TMapSet<String, String> tmap = new TMapSet<>();
                Object obj = "";
                String tempSsid = "";
                int i = 0;
                while (i < size) {
                    String filePath2 = filePath;
                    try {
                        String[] tempScanWifiInfo = lines[i].split(",");
                        String fileContent2 = fileContent;
                        if (tempScanWifiInfo.length >= param.getScanWifiStart()) {
                            if (tempScanWifiInfo[0] == null) {
                                String[] wds = tempScanWifiInfo;
                            } else if (!tempScanWifiInfo[0].equals("")) {
                                int tempSize = tempScanWifiInfo.length;
                                int k = param.getScanWifiStart();
                                while (k < tempSize) {
                                    int tempSize2 = tempSize;
                                    String[] wds2 = tempScanWifiInfo;
                                    String[] tempScanWifiInfo2 = tempScanWifiInfo[k].split(param.getWifiSeperate());
                                    String tempMac = tempSsid;
                                    if (tempScanWifiInfo2.length < 4) {
                                        tempSsid = tempMac;
                                    } else {
                                        String tempMac2 = tempScanWifiInfo2[param.getScanMAC()];
                                        tempSetMacs.add(tempMac2);
                                        String tempSsid2 = tempScanWifiInfo2[param.getScanSSID()];
                                        if (!apInfoSet.contains(tempSsid2)) {
                                            tmap.add(tempSsid2, tempMac2);
                                        }
                                        String str5 = tempSsid2;
                                        tempSsid = tempMac2;
                                    }
                                    k++;
                                    tempSize = tempSize2;
                                    tempScanWifiInfo = wds2;
                                }
                                String[] wds3 = tempScanWifiInfo;
                                String str6 = tempSsid;
                            }
                            i++;
                            filePath = filePath2;
                            fileContent = fileContent2;
                        }
                        String[] wds4 = tempScanWifiInfo;
                        i++;
                        filePath = filePath2;
                        fileContent = fileContent2;
                    } catch (RuntimeException e3) {
                        e = e3;
                        LogUtil.e("filterMobileAp, " + e.getMessage() + ",isFilterMobileAp");
                        return apChrStatInfo.toString();
                    } catch (Exception e4) {
                        e = e4;
                        LogUtil.e("filterMobileAp, " + e.getMessage() + ",isFilterMobileAp");
                        return apChrStatInfo.toString();
                    }
                }
                String str7 = fileContent;
                LogUtil.i("all macs:" + tempSetMacs);
                if (!param.isMainAp() && !param.isTest01()) {
                    apChrStatInfo.setMobileApSrc1(filterMobileAps1(parameterInfo, tempSetMacs, tmap));
                }
                LogUtil.i(" filterMobileAp tempSetMacs.length:" + tempSetMacs.size());
                Iterator iterator = tempSetMacs.iterator();
                while (true) {
                    Iterator iterator2 = iterator;
                    if (!iterator2.hasNext()) {
                        break;
                    }
                    try {
                        String tempMac3 = iterator2.next();
                        if (!checkMacFormat(tempMac3)) {
                            iterator = iterator2;
                        } else {
                            macLst.add(tempMac3);
                            iterator = iterator2;
                        }
                    } catch (RuntimeException e5) {
                        LogUtil.e("updateModel:" + e5);
                    } catch (Exception e6) {
                        LogUtil.e("updateModel:" + e6);
                    }
                }
                LogUtil.d(" filterMobileAp setMacs.length:" + macLst.size() + ",isFilterMobileAp");
                if (macLst.size() > param.getMaxBssidNum()) {
                    apChrStatInfo.setResult(-60);
                    return apChrStatInfo.toString();
                }
                StdDataSet stdDataSet = getFilterMobileAps2StdDataSet(str, parameterInfo, lines, macLst);
                apChrStatInfo.setMobileApSrc2(stdDataSet.getFilter2MobileApCnt());
                apChrStatInfo.setTotalFound(stdDataSet.getValidMacCnt());
                return apChrStatInfo.toString();
            }
        } catch (RuntimeException e7) {
            e = e7;
            String str8 = filePath;
            LogUtil.e("filterMobileAp, " + e.getMessage() + ",isFilterMobileAp");
            return apChrStatInfo.toString();
        } catch (Exception e8) {
            e = e8;
            String str9 = filePath;
            LogUtil.e("filterMobileAp, " + e.getMessage() + ",isFilterMobileAp");
            return apChrStatInfo.toString();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:104:0x023b, code lost:
        if (r3.contains(r1) != false) goto L_0x023e;
     */
    public int transformRawData(String place, ParameterInfo param, BuildModelChrInfo buildModelChrInfo) {
        List<ApInfo> mobileApLst;
        Iterator it;
        Set<Integer> batchSet;
        ApChrStatInfo apChrStatInfo;
        StdDataSet stdDataSet;
        int size;
        Integer tempBatch;
        String str = place;
        ParameterInfo parameterInfo = param;
        String filePath = getTrainDataFilePath(place, param);
        String str2 = TAG;
        Log.d(str2, " transformRawData begin:" + str + ",filePath:" + filePath);
        try {
            String fileContent = FileUtils.getFileContent(filePath);
            if (fileContent.equals("")) {
                try {
                    LogUtil.d("transformRawData ,null == fileContent.");
                    return -52;
                } catch (RuntimeException e) {
                    e = e;
                    String str3 = filePath;
                    LogUtil.e("transformRawData, " + e.getMessage());
                    return 1;
                } catch (Exception e2) {
                    e = e2;
                    String str4 = filePath;
                    LogUtil.e("transformRawData, " + e.getMessage());
                    return 1;
                }
            } else if (((long) fileContent.length()) > Constant.MAX_FILE_SIZE) {
                LogUtil.d("transformRawData ,file content is too bigger than max_file_size.");
                return -53;
            } else {
                String[] lines = fileContent.split(Constant.getLineSeperate());
                if (lines.length < 2) {
                    LogUtil.d(" transformRawData read data,lines == null || lines.length < 2");
                    return -54;
                }
                Set<String> rawApSetMacs = new HashSet<>();
                List<String> macLst = new ArrayList<>();
                String tempMac = "";
                String tempSsid = "";
                String tempSsidMac = "";
                int size2 = lines.length;
                MobileApDAO mobileApDao = new MobileApDAO();
                List<ApInfo> mobileApLst2 = new ArrayList<>();
                if (!param.isMainAp()) {
                    mobileApLst = mobileApDao.findAllAps();
                } else {
                    mobileApLst = mobileApLst2;
                }
                ArrayList arrayList = new ArrayList();
                if (!param.isMainAp() && mobileApLst != null) {
                    if (mobileApLst.size() != 0) {
                        for (ApInfo mAp : mobileApLst) {
                            String tempMac2 = tempMac;
                            if (mAp.getMac() == null) {
                                tempMac = tempMac2;
                            } else if (mAp.getSsid() == null) {
                                tempMac = tempMac2;
                            } else {
                                String tempSsid2 = tempSsid;
                                StringBuilder sb = new StringBuilder();
                                String tempSsidMac2 = tempSsidMac;
                                sb.append(mAp.getSsid());
                                sb.append("_");
                                sb.append(mAp.getMac());
                                arrayList.add(sb.toString());
                                tempMac = tempMac2;
                                tempSsid = tempSsid2;
                                tempSsidMac = tempSsidMac2;
                            }
                        }
                    }
                }
                String str5 = tempSsid;
                String tempSsidMac3 = tempSsidMac;
                new TMapSet();
                Set<Integer> batchSet2 = new HashSet<>();
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (i2 >= size2) {
                        break;
                    }
                    List<ApInfo> mobileApLst3 = mobileApLst;
                    String[] wds = lines[i2].split(",");
                    MobileApDAO mobileApDao2 = mobileApDao;
                    if (wds.length >= param.getScanWifiStart()) {
                        if (wds[0] == null) {
                            size = size2;
                        } else if (!wds[0].equals("")) {
                            try {
                                Integer tempBatch2 = Integer.valueOf(wds[0]);
                                batchSet2.add(tempBatch2);
                                int tempSize = wds.length;
                                int k = param.getScanWifiStart();
                                while (true) {
                                    tempBatch = tempBatch2;
                                    int k2 = k;
                                    if (k2 >= tempSize) {
                                        break;
                                    }
                                    int tempSize2 = tempSize;
                                    String[] wds2 = wds;
                                    String[] tempScanWifiInfo = wds[k2].split(param.getWifiSeperate());
                                    int size3 = size2;
                                    if (tempScanWifiInfo.length < 4) {
                                        String[] strArr = tempScanWifiInfo;
                                    } else {
                                        String tempMac3 = tempScanWifiInfo[param.getScanMAC()];
                                        String tempSsid3 = tempScanWifiInfo[param.getScanSSID()];
                                        String[] strArr2 = tempScanWifiInfo;
                                        StringBuilder sb2 = new StringBuilder();
                                        sb2.append(tempSsid3);
                                        String tempSsid4 = tempSsid3;
                                        sb2.append("_");
                                        sb2.append(tempMac3);
                                        rawApSetMacs.add(sb2.toString());
                                        String str6 = tempMac3;
                                        String str7 = tempSsid4;
                                    }
                                    k = k2 + 1;
                                    tempBatch2 = tempBatch;
                                    tempSize = tempSize2;
                                    wds = wds2;
                                    size2 = size3;
                                }
                                size = size2;
                                Integer num = tempBatch;
                            } catch (NumberFormatException e3) {
                                String[] strArr3 = wds;
                                size = size2;
                            }
                        }
                        i = i2 + 1;
                        mobileApLst = mobileApLst3;
                        mobileApDao = mobileApDao2;
                        size2 = size;
                    }
                    size = size2;
                    i = i2 + 1;
                    mobileApLst = mobileApLst3;
                    mobileApDao = mobileApDao2;
                    size2 = size;
                }
                List<ApInfo> mobileApLst4 = mobileApLst;
                MobileApDAO mobileApDao3 = mobileApDao;
                int size4 = size2;
                LogUtil.i("all macs:" + rawApSetMacs + ",batch cnt:" + batchSet2.size());
                int minBatch = getMinBatch(parameterInfo, batchSet2);
                ApChrStatInfo apChrStatInfo2 = buildModelChrInfo.getAPType();
                LogUtil.i(" transformRawData tempSetMacs.length:" + rawApSetMacs.size());
                Iterator iterator = rawApSetMacs.iterator();
                while (true) {
                    it = iterator;
                    if (!it.hasNext()) {
                        batchSet = batchSet2;
                        String str8 = tempSsidMac3;
                        break;
                    }
                    try {
                        String tempSsidMac4 = it.next();
                        try {
                            if (tempSsidMac4.contains("_")) {
                                if (!param.isMainAp()) {
                                    try {
                                    } catch (RuntimeException e4) {
                                        e = e4;
                                        tempSsidMac3 = tempSsidMac4;
                                        batchSet = batchSet2;
                                        LogUtil.e("updateModel:" + e);
                                        iterator = it;
                                        batchSet2 = batchSet;
                                    } catch (Exception e5) {
                                        e = e5;
                                        tempSsidMac3 = tempSsidMac4;
                                        batchSet = batchSet2;
                                        LogUtil.e("updateModel:" + e);
                                        iterator = it;
                                        batchSet2 = batchSet;
                                    }
                                }
                                String tempSsidMac5 = tempSsidMac4;
                                String tempMac4 = tempSsidMac4.split("_")[1];
                                try {
                                    if (!checkMacFormat(tempMac4)) {
                                        batchSet = batchSet2;
                                    } else {
                                        batchSet = batchSet2;
                                        try {
                                            if (macLst.size() >= param.getMaxBssidNum()) {
                                                String str9 = tempMac4;
                                                break;
                                            }
                                            macLst.add(tempMac4);
                                        } catch (RuntimeException e6) {
                                            e = e6;
                                            String str10 = tempMac4;
                                            tempSsidMac3 = tempSsidMac5;
                                            LogUtil.e("updateModel:" + e);
                                            iterator = it;
                                            batchSet2 = batchSet;
                                        } catch (Exception e7) {
                                            e = e7;
                                            String str11 = tempMac4;
                                            tempSsidMac3 = tempSsidMac5;
                                            LogUtil.e("updateModel:" + e);
                                            iterator = it;
                                            batchSet2 = batchSet;
                                        }
                                    }
                                    String str12 = tempMac4;
                                    iterator = it;
                                    tempSsidMac3 = tempSsidMac5;
                                    batchSet2 = batchSet;
                                } catch (RuntimeException e8) {
                                    e = e8;
                                    batchSet = batchSet2;
                                    String str13 = tempMac4;
                                    tempSsidMac3 = tempSsidMac5;
                                    LogUtil.e("updateModel:" + e);
                                    iterator = it;
                                    batchSet2 = batchSet;
                                } catch (Exception e9) {
                                    e = e9;
                                    batchSet = batchSet2;
                                    String str14 = tempMac4;
                                    tempSsidMac3 = tempSsidMac5;
                                    LogUtil.e("updateModel:" + e);
                                    iterator = it;
                                    batchSet2 = batchSet;
                                }
                            }
                            tempSsidMac3 = tempSsidMac4;
                            iterator = it;
                        } catch (RuntimeException e10) {
                            e = e10;
                            batchSet = batchSet2;
                            tempSsidMac3 = tempSsidMac4;
                            LogUtil.e("updateModel:" + e);
                            iterator = it;
                            batchSet2 = batchSet;
                        } catch (Exception e11) {
                            e = e11;
                            batchSet = batchSet2;
                            tempSsidMac3 = tempSsidMac4;
                            LogUtil.e("updateModel:" + e);
                            iterator = it;
                            batchSet2 = batchSet;
                        }
                    } catch (RuntimeException e12) {
                        e = e12;
                        batchSet = batchSet2;
                        LogUtil.e("updateModel:" + e);
                        iterator = it;
                        batchSet2 = batchSet;
                    } catch (Exception e13) {
                        e = e13;
                        batchSet = batchSet2;
                        LogUtil.e("updateModel:" + e);
                        iterator = it;
                        batchSet2 = batchSet;
                    }
                }
                LogUtil.d(" transformRawData setMacs.length:" + macLst.size());
                if (macLst.size() == 0) {
                    return -56;
                }
                if (param.isMainAp()) {
                    stdDataSet = getMainApStdDataSet(parameterInfo, lines, macLst, minBatch);
                    ArrayList arrayList2 = arrayList;
                    Iterator it2 = it;
                    int i3 = minBatch;
                    String str15 = filePath;
                    List<ApInfo> list = mobileApLst4;
                    MobileApDAO mobileApDAO2 = mobileApDao3;
                    int i4 = size4;
                    Set<Integer> set = batchSet;
                    apChrStatInfo = apChrStatInfo2;
                } else {
                    Set<Integer> set2 = batchSet;
                    ArrayList arrayList3 = arrayList;
                    Iterator it3 = it;
                    List<ApInfo> list2 = mobileApLst4;
                    String str16 = filePath;
                    MobileApDAO mobileApDAO3 = mobileApDao3;
                    apChrStatInfo = apChrStatInfo2;
                    int i5 = minBatch;
                    int i6 = size4;
                    try {
                        stdDataSet = getCommStdDataSet(str, parameterInfo, lines, macLst, minBatch);
                    } catch (RuntimeException e14) {
                        e = e14;
                        LogUtil.e("transformRawData, " + e.getMessage());
                        return 1;
                    } catch (Exception e15) {
                        e = e15;
                        LogUtil.e("transformRawData, " + e.getMessage());
                        return 1;
                    }
                }
                apChrStatInfo.setUpdate(new TimeUtil().getTimeIntPATTERN02());
                LogUtil.d(" transformRawData datas.length:" + lines.length);
                if (!saveStdDataSetToFile(stdDataSet, str, parameterInfo)) {
                    LogUtil.d(" saveStdDataSetToFile save failure:" + str);
                    return -55;
                }
                apChrStatInfo.setTotalFound(stdDataSet.getValidMacCnt());
                return 1;
            }
        } catch (RuntimeException e16) {
            e = e16;
            String str17 = filePath;
            LogUtil.e("transformRawData, " + e.getMessage());
            return 1;
        } catch (Exception e17) {
            e = e17;
            String str18 = filePath;
            LogUtil.e("transformRawData, " + e.getMessage());
            return 1;
        }
    }

    private int getMinBatch(ParameterInfo param, Set<Integer> batchSet) {
        if (param == null || batchSet == null || batchSet.size() <= param.getMaxTrainBatchCnt()) {
            return 0;
        }
        LinkedList<Integer> setSort = new LinkedList<>(batchSet);
        Collections.sort(setSort, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return o2.intValue() - o1.intValue();
            }
        });
        return setSort.get(param.getMaxTrainBatchCnt() - 1).intValue();
    }

    private int filterMobileAps1(ParameterInfo param, Set<String> tempSetMacs, TMapSet<String, String> tmap) {
        int mobileApCnt = 0;
        if (tempSetMacs == null || tempSetMacs.size() == 0 || tmap == null || tmap.size() == 0) {
            return 0;
        }
        try {
            for (Map.Entry<String, Set<String>> entry : tmap.entrySet()) {
                String tempSsid = entry.getKey();
                Set<String> tempMacs = entry.getValue();
                if (tempSsid != null) {
                    if (!tempSsid.equals("")) {
                        if (tempMacs.size() > param.getMobileApCheckLimit()) {
                            mobileApCnt += tempMacs.size();
                            if (addMobileAp(tempSsid, tempMacs.iterator(), 1).size() > 0) {
                                tempSetMacs.removeAll(tempMacs);
                            }
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            LogUtil.e("filterMobileAps1, " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("filterMobileAps1, " + e2.getMessage());
        }
        return mobileApCnt;
    }

    private List<String> addMobileAp(String ssid, Iterator<String> iterator, int srcType) {
        List<String> mobileMacs = new ArrayList<>();
        if (this.enterpriseApDAO.findBySsid(ssid) == null) {
            while (iterator.hasNext()) {
                try {
                    String tempMac = iterator.next();
                    mobileMacs.add(tempMac);
                    ApInfo mobileAp = new ApInfo(ssid, tempMac, TimeUtil.getTime(), srcType);
                    if (!this.mobileApDAO.insert(mobileAp)) {
                        LogUtil.i("addMobileAp,add mobile ap failure,:" + mobileAp.toString());
                    }
                } catch (RuntimeException e) {
                    LogUtil.e("addMobileAp, " + e.getMessage());
                } catch (Exception ex) {
                    LogUtil.e("addMobileAp, " + ex.getMessage());
                }
            }
        }
        return mobileMacs;
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x00cb A[Catch:{ RuntimeException -> 0x01e9, Exception -> 0x01e7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00d5 A[Catch:{ RuntimeException -> 0x01e9, Exception -> 0x01e7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00f5 A[Catch:{ NumberFormatException -> 0x013f }] */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00f7 A[Catch:{ NumberFormatException -> 0x013f }] */
    private StdDataSet getMainApStdDataSet(ParameterInfo param, String[] lines, List<String> macLst, int minBatch) {
        Iterator<String> it;
        String[] tempScanWifiInfo;
        String[] strArr = lines;
        List<String> list = macLst;
        StdDataSet stdDataSet = new StdDataSet();
        if (param == null || strArr == null || list == null) {
            int i = minBatch;
            return stdDataSet;
        } else if (strArr.length == 0 || macLst.size() == 0) {
            int i2 = minBatch;
            return stdDataSet;
        } else {
            HashMap hashMap = new HashMap();
            try {
                stdDataSet.setMacLst(list);
                String lastBatch = "";
                int standardBatch = 0;
                char c = 0;
                String tempMac = null;
                int rdCount = 0;
                int rdCount2 = 0;
                while (true) {
                    int i3 = rdCount2;
                    try {
                        if (i3 >= strArr.length) {
                            break;
                        }
                        try {
                            String[] wds = strArr[i3].split(",");
                            if (wds.length >= param.getScanWifiStart()) {
                                if (wds[c] != null) {
                                    if (!wds[c].equals("")) {
                                        if (wds.length > param.getServingWiFiMAC()) {
                                            try {
                                                String curBatch = wds[c];
                                                try {
                                                    if (Integer.valueOf(curBatch).intValue() < minBatch) {
                                                        rdCount2 = i3 + 1;
                                                        strArr = lines;
                                                        c = 0;
                                                    } else {
                                                        try {
                                                            if (!curBatch.equals(lastBatch)) {
                                                                standardBatch++;
                                                                lastBatch = curBatch;
                                                            }
                                                        } catch (RuntimeException e) {
                                                            e = e;
                                                            LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                                                            StdRecord tempStdRecord = new StdRecord(standardBatch);
                                                            if (param.getTimestamp() < wds.length) {
                                                            }
                                                            List<Integer> tempScanRssis = new ArrayList<>();
                                                            hashMap.clear();
                                                            tempScanWifiInfo = wds[param.getScanWifiStart()].split(param.getWifiSeperate());
                                                            if (tempScanWifiInfo.length >= 4) {
                                                            }
                                                            rdCount2 = i3 + 1;
                                                            strArr = lines;
                                                            c = 0;
                                                        } catch (Exception e2) {
                                                            e = e2;
                                                            try {
                                                                LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                                                            } catch (RuntimeException e3) {
                                                                e = e3;
                                                                LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                                                                rdCount2 = i3 + 1;
                                                                strArr = lines;
                                                                c = 0;
                                                            } catch (Exception e4) {
                                                                e = e4;
                                                                try {
                                                                    LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                                                                    rdCount2 = i3 + 1;
                                                                    strArr = lines;
                                                                    c = 0;
                                                                } catch (RuntimeException e5) {
                                                                    e = e5;
                                                                    LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                                                                    return stdDataSet;
                                                                } catch (Exception e6) {
                                                                    e = e6;
                                                                    LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                                                                    return stdDataSet;
                                                                }
                                                            }
                                                            rdCount2 = i3 + 1;
                                                            strArr = lines;
                                                            c = 0;
                                                        }
                                                        StdRecord tempStdRecord2 = new StdRecord(standardBatch);
                                                        if (param.getTimestamp() < wds.length) {
                                                            tempStdRecord2.setTimeStamp(wds[param.getTimestamp()]);
                                                        } else {
                                                            tempStdRecord2.setTimeStamp("0");
                                                        }
                                                        List<Integer> tempScanRssis2 = new ArrayList<>();
                                                        hashMap.clear();
                                                        try {
                                                            tempScanWifiInfo = wds[param.getScanWifiStart()].split(param.getWifiSeperate());
                                                            if (tempScanWifiInfo.length >= 4) {
                                                                tempMac = tempScanWifiInfo[param.getScanMAC()];
                                                                if (!stdDataSet.getMacRecords().containsKey(tempMac)) {
                                                                    if (checkMacFormat(tempMac)) {
                                                                        stdDataSet.getMacRecords().put(tempMac, new TMapList());
                                                                    }
                                                                }
                                                                hashMap.put(tempScanWifiInfo[param.getScanMAC()], Integer.valueOf(Integer.parseInt(tempScanWifiInfo[param.getScanRSSI()].split("\\.")[0])));
                                                                for (Iterator<String> it2 = macLst.iterator(); it2.hasNext(); it2 = it) {
                                                                    try {
                                                                        if (hashMap.containsKey(it2.next())) {
                                                                            tempScanRssis2.add(Integer.valueOf(((Integer) hashMap.get(tempMac)).intValue()));
                                                                            it = it2;
                                                                        } else {
                                                                            it = it2;
                                                                            try {
                                                                                tempScanRssis2.add(0);
                                                                            } catch (RuntimeException e7) {
                                                                                e = e7;
                                                                            } catch (Exception e8) {
                                                                                e = e8;
                                                                                LogUtil.e(" getMainApStdDataSet exception :" + e.getMessage());
                                                                            }
                                                                        }
                                                                    } catch (RuntimeException e9) {
                                                                        e = e9;
                                                                        it = it2;
                                                                        LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                                                                    } catch (Exception e10) {
                                                                        e = e10;
                                                                        it = it2;
                                                                        LogUtil.e(" getMainApStdDataSet exception :" + e.getMessage());
                                                                    }
                                                                }
                                                                tempStdRecord2.setScanRssis(tempScanRssis2);
                                                                stdDataSet.getMacRecords().get(tempMac).add(Integer.valueOf(standardBatch), tempStdRecord2);
                                                                rdCount++;
                                                            }
                                                        } catch (NumberFormatException e11) {
                                                            LogUtil.e("getMainApStdDataSet, " + e11.getMessage());
                                                        }
                                                        rdCount2 = i3 + 1;
                                                        strArr = lines;
                                                        c = 0;
                                                    }
                                                } catch (NumberFormatException e12) {
                                                    int i4 = minBatch;
                                                }
                                            } catch (RuntimeException e13) {
                                                e = e13;
                                                int i5 = minBatch;
                                                LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                                                StdRecord tempStdRecord22 = new StdRecord(standardBatch);
                                                if (param.getTimestamp() < wds.length) {
                                                }
                                                List<Integer> tempScanRssis22 = new ArrayList<>();
                                                hashMap.clear();
                                                tempScanWifiInfo = wds[param.getScanWifiStart()].split(param.getWifiSeperate());
                                                if (tempScanWifiInfo.length >= 4) {
                                                }
                                                rdCount2 = i3 + 1;
                                                strArr = lines;
                                                c = 0;
                                            } catch (Exception e14) {
                                                e = e14;
                                                int i6 = minBatch;
                                                LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                                                rdCount2 = i3 + 1;
                                                strArr = lines;
                                                c = 0;
                                            }
                                        }
                                    }
                                }
                                int i7 = minBatch;
                                rdCount2 = i3 + 1;
                                strArr = lines;
                                c = 0;
                            }
                            int i8 = minBatch;
                        } catch (RuntimeException e15) {
                            e = e15;
                            int i9 = minBatch;
                            LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                            rdCount2 = i3 + 1;
                            strArr = lines;
                            c = 0;
                        } catch (Exception e16) {
                            e = e16;
                            int i10 = minBatch;
                            LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                            rdCount2 = i3 + 1;
                            strArr = lines;
                            c = 0;
                        }
                        rdCount2 = i3 + 1;
                        strArr = lines;
                        c = 0;
                    } catch (RuntimeException e17) {
                        e = e17;
                        int i11 = minBatch;
                        LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                        return stdDataSet;
                    } catch (Exception e18) {
                        e = e18;
                        int i12 = minBatch;
                        LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                        return stdDataSet;
                    }
                }
                int i13 = minBatch;
                new ArrayList();
                List<Integer> macIndexLst = new ArrayList<>();
                int size = macLst.size();
                int i14 = 0;
                while (true) {
                    int i15 = i14;
                    if (i15 >= size) {
                        break;
                    }
                    macIndexLst.add(Integer.valueOf(i15));
                    i14 = i15 + 1;
                }
                stdDataSet.setMacIndexLst(macIndexLst);
                LogUtil.d("getMainApStdDataSet rdCount:" + rdCount);
            } catch (RuntimeException e19) {
                e = e19;
                int i16 = minBatch;
                LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                return stdDataSet;
            } catch (Exception e20) {
                e = e20;
                int i17 = minBatch;
                LogUtil.e("getMainApStdDataSet, " + e.getMessage());
                return stdDataSet;
            }
            return stdDataSet;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x01e5, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x01e6, code lost:
        com.android.server.hidata.wavemapping.util.LogUtil.e("getCommStdDataSet, " + r0.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x01ca, code lost:
        r0 = e;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x01e5 A[Catch:{ RuntimeException -> 0x0173, Exception -> 0x0170 }, ExcHandler: RuntimeException (r0v43 'e' java.lang.RuntimeException A[CUSTOM_DECLARE, Catch:{ RuntimeException -> 0x0173, Exception -> 0x0170 }]), Splitter:B:87:0x01a8] */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x019d A[EDGE_INSN: B:136:0x019d->B:83:0x019d ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x0202 A[EDGE_INSN: B:141:0x0202->B:103:0x0202 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00f1 A[SYNTHETIC, Splitter:B:56:0x00f1] */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00fb  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x011c A[SYNTHETIC, Splitter:B:64:0x011c] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01a8 A[SYNTHETIC, Splitter:B:87:0x01a8] */
    private StdDataSet getCommStdDataSet(String place, ParameterInfo param, String[] lines, List<String> macLst, int minBatch) {
        String lastBatch;
        int tempServeRssi;
        int tempServeRssi2;
        int tempSize;
        int k;
        int tempSize2;
        int k2;
        int size2;
        int ik;
        int ik2;
        int tempSize3;
        String[] strArr = lines;
        List<String> list = macLst;
        StdDataSet stdDataSet = new StdDataSet();
        if (param == null || strArr == null || list == null || strArr.length == 0 || macLst.size() == 0) {
            return stdDataSet;
        }
        HashMap hashMap = new HashMap();
        HashMap hashMap2 = new HashMap();
        try {
            stdDataSet.setMacLst(list);
            String lastBatch2 = "";
            char c = 0;
            int standardBatch = 0;
            int rdCount = 0;
            int tempServeRssi3 = 0;
            int tempServeRssi4 = 0;
            while (true) {
                int i = tempServeRssi4;
                if (i >= strArr.length) {
                    break;
                }
                try {
                    String[] wds = strArr[i].split(",");
                    String tempServeMac = null;
                    tempServeRssi3 = 0;
                    try {
                        if (wds.length >= param.getScanWifiStart()) {
                            if (wds[c] == null) {
                                tempServeRssi2 = 0;
                            } else if (wds[c].equals("")) {
                                tempServeRssi2 = 0;
                            } else if (wds.length > param.getServingWiFiMAC()) {
                                try {
                                    String curBatch = wds[c];
                                    try {
                                        if (Integer.valueOf(curBatch).intValue() >= minBatch) {
                                            try {
                                                tempServeMac = wds[param.getServingWiFiMAC()];
                                                if (!stdDataSet.getMacRecords().containsKey(tempServeMac)) {
                                                    stdDataSet.getMacRecords().put(tempServeMac, new TMapList());
                                                }
                                                if (!curBatch.equals(lastBatch2)) {
                                                    standardBatch++;
                                                    lastBatch2 = curBatch;
                                                }
                                            } catch (RuntimeException e) {
                                                e = e;
                                                LogUtil.e("getCommStdDataSet, " + e.getMessage());
                                                StdRecord tempStdRecord = new StdRecord(standardBatch);
                                                if (param.getTimestamp() < wds.length) {
                                                }
                                                List<Integer> tempScanRssis = new ArrayList<>();
                                                tempSize = wds.length;
                                                hashMap.clear();
                                                k = param.getScanWifiStart();
                                                while (true) {
                                                    tempServeRssi = tempServeRssi3;
                                                    lastBatch = lastBatch2;
                                                    tempSize2 = tempSize;
                                                    k2 = k;
                                                    if (k2 >= tempSize2) {
                                                    }
                                                    k = k2 + 1;
                                                    tempServeRssi3 = tempServeRssi;
                                                    lastBatch2 = lastBatch;
                                                    tempSize = tempSize3;
                                                    int i2 = minBatch;
                                                }
                                                size2 = macLst.size();
                                                ik = 0;
                                                while (true) {
                                                    ik2 = ik;
                                                    if (ik2 >= size2) {
                                                    }
                                                    ik = ik2 + 1;
                                                }
                                                tempStdRecord.setScanRssis(tempScanRssis);
                                                stdDataSet.getMacRecords().get(tempServeMac).add(Integer.valueOf(standardBatch), tempStdRecord);
                                                rdCount++;
                                                tempServeRssi3 = tempServeRssi;
                                                lastBatch2 = lastBatch;
                                                tempServeRssi4 = i + 1;
                                                strArr = lines;
                                                c = 0;
                                            } catch (Exception e2) {
                                                e = e2;
                                                LogUtil.e("getCommStdDataSet, " + e.getMessage());
                                                tempServeRssi2 = 0;
                                                tempServeRssi3 = tempServeRssi2;
                                                tempServeRssi4 = i + 1;
                                                strArr = lines;
                                                c = 0;
                                            }
                                            try {
                                                StdRecord tempStdRecord2 = new StdRecord(standardBatch);
                                                if (param.getTimestamp() < wds.length) {
                                                    tempStdRecord2.setTimeStamp(wds[param.getTimestamp()]);
                                                } else {
                                                    tempStdRecord2.setTimeStamp("0");
                                                }
                                                List<Integer> tempScanRssis2 = new ArrayList<>();
                                                tempSize = wds.length;
                                                hashMap.clear();
                                                k = param.getScanWifiStart();
                                                while (true) {
                                                    tempServeRssi = tempServeRssi3;
                                                    lastBatch = lastBatch2;
                                                    tempSize2 = tempSize;
                                                    k2 = k;
                                                    if (k2 >= tempSize2) {
                                                        break;
                                                    }
                                                    try {
                                                        tempSize3 = tempSize2;
                                                        try {
                                                            String[] tempScanWifiInfo = wds[k2].split(param.getWifiSeperate());
                                                            if (tempScanWifiInfo.length >= 4) {
                                                                String tempMac = tempScanWifiInfo[param.getScanMAC()];
                                                                if (checkMacFormat(tempMac)) {
                                                                    String str = tempMac;
                                                                    hashMap.put(tempScanWifiInfo[param.getScanMAC()], Integer.valueOf(Integer.parseInt(tempScanWifiInfo[param.getScanRSSI()].split("\\.")[0])));
                                                                    hashMap2.put(tempScanWifiInfo[param.getScanMAC()], tempScanWifiInfo[param.getScanSSID()]);
                                                                }
                                                            }
                                                        } catch (NumberFormatException e3) {
                                                            e = e3;
                                                            try {
                                                                LogUtil.e("getCommStdDataSet, " + e.getMessage());
                                                                k = k2 + 1;
                                                                tempServeRssi3 = tempServeRssi;
                                                                lastBatch2 = lastBatch;
                                                                tempSize = tempSize3;
                                                                int i22 = minBatch;
                                                            } catch (RuntimeException e4) {
                                                                e = e4;
                                                                LogUtil.e("getCommStdDataSet, " + e.getMessage());
                                                                tempServeRssi3 = tempServeRssi;
                                                                lastBatch2 = lastBatch;
                                                                tempServeRssi4 = i + 1;
                                                                strArr = lines;
                                                                c = 0;
                                                            } catch (Exception e5) {
                                                                e = e5;
                                                                LogUtil.e("getCommStdDataSet, " + e.getMessage());
                                                                tempServeRssi3 = tempServeRssi;
                                                                lastBatch2 = lastBatch;
                                                                tempServeRssi4 = i + 1;
                                                                strArr = lines;
                                                                c = 0;
                                                            }
                                                        }
                                                    } catch (NumberFormatException e6) {
                                                        e = e6;
                                                        tempSize3 = tempSize2;
                                                        LogUtil.e("getCommStdDataSet, " + e.getMessage());
                                                        k = k2 + 1;
                                                        tempServeRssi3 = tempServeRssi;
                                                        lastBatch2 = lastBatch;
                                                        tempSize = tempSize3;
                                                        int i222 = minBatch;
                                                    }
                                                    k = k2 + 1;
                                                    tempServeRssi3 = tempServeRssi;
                                                    lastBatch2 = lastBatch;
                                                    tempSize = tempSize3;
                                                    int i2222 = minBatch;
                                                }
                                                size2 = macLst.size();
                                                ik = 0;
                                                while (true) {
                                                    ik2 = ik;
                                                    if (ik2 >= size2) {
                                                        break;
                                                    }
                                                    try {
                                                        String mac = list.get(ik2);
                                                        if (hashMap.containsKey(mac)) {
                                                            tempScanRssis2.add((Integer) hashMap.get(mac));
                                                        } else {
                                                            tempScanRssis2.add(0);
                                                        }
                                                    } catch (RuntimeException e7) {
                                                    } catch (Exception e8) {
                                                        e = e8;
                                                        LogUtil.e(" getCommStdDataSet exception :" + e.getMessage());
                                                    }
                                                    ik = ik2 + 1;
                                                }
                                                tempStdRecord2.setScanRssis(tempScanRssis2);
                                                stdDataSet.getMacRecords().get(tempServeMac).add(Integer.valueOf(standardBatch), tempStdRecord2);
                                                rdCount++;
                                            } catch (RuntimeException e9) {
                                                e = e9;
                                                tempServeRssi = 0;
                                                lastBatch = lastBatch2;
                                                LogUtil.e("getCommStdDataSet, " + e.getMessage());
                                                tempServeRssi3 = tempServeRssi;
                                                lastBatch2 = lastBatch;
                                                tempServeRssi4 = i + 1;
                                                strArr = lines;
                                                c = 0;
                                            } catch (Exception e10) {
                                                e = e10;
                                                tempServeRssi = 0;
                                                lastBatch = lastBatch2;
                                                LogUtil.e("getCommStdDataSet, " + e.getMessage());
                                                tempServeRssi3 = tempServeRssi;
                                                lastBatch2 = lastBatch;
                                                tempServeRssi4 = i + 1;
                                                strArr = lines;
                                                c = 0;
                                            }
                                            tempServeRssi3 = tempServeRssi;
                                            lastBatch2 = lastBatch;
                                            tempServeRssi4 = i + 1;
                                            strArr = lines;
                                            c = 0;
                                        }
                                    } catch (NumberFormatException e11) {
                                        int i3 = minBatch;
                                    }
                                } catch (RuntimeException e12) {
                                    e = e12;
                                    int i4 = minBatch;
                                    LogUtil.e("getCommStdDataSet, " + e.getMessage());
                                    StdRecord tempStdRecord22 = new StdRecord(standardBatch);
                                    if (param.getTimestamp() < wds.length) {
                                    }
                                    List<Integer> tempScanRssis22 = new ArrayList<>();
                                    tempSize = wds.length;
                                    hashMap.clear();
                                    k = param.getScanWifiStart();
                                    while (true) {
                                        tempServeRssi = tempServeRssi3;
                                        lastBatch = lastBatch2;
                                        tempSize2 = tempSize;
                                        k2 = k;
                                        if (k2 >= tempSize2) {
                                        }
                                        k = k2 + 1;
                                        tempServeRssi3 = tempServeRssi;
                                        lastBatch2 = lastBatch;
                                        tempSize = tempSize3;
                                        int i22222 = minBatch;
                                    }
                                    size2 = macLst.size();
                                    ik = 0;
                                    while (true) {
                                        ik2 = ik;
                                        if (ik2 >= size2) {
                                        }
                                        ik = ik2 + 1;
                                    }
                                    tempStdRecord22.setScanRssis(tempScanRssis22);
                                    stdDataSet.getMacRecords().get(tempServeMac).add(Integer.valueOf(standardBatch), tempStdRecord22);
                                    rdCount++;
                                    tempServeRssi3 = tempServeRssi;
                                    lastBatch2 = lastBatch;
                                    tempServeRssi4 = i + 1;
                                    strArr = lines;
                                    c = 0;
                                } catch (Exception e13) {
                                    e = e13;
                                    int i5 = minBatch;
                                    LogUtil.e("getCommStdDataSet, " + e.getMessage());
                                    tempServeRssi2 = 0;
                                    tempServeRssi3 = tempServeRssi2;
                                    tempServeRssi4 = i + 1;
                                    strArr = lines;
                                    c = 0;
                                }
                            }
                            tempServeRssi3 = tempServeRssi2;
                            tempServeRssi4 = i + 1;
                            strArr = lines;
                            c = 0;
                        }
                        tempServeRssi2 = 0;
                        tempServeRssi3 = tempServeRssi2;
                    } catch (RuntimeException e14) {
                        e = e14;
                        tempServeRssi = 0;
                        lastBatch = lastBatch2;
                    } catch (Exception e15) {
                        e = e15;
                        tempServeRssi = 0;
                        lastBatch = lastBatch2;
                        LogUtil.e("getCommStdDataSet, " + e.getMessage());
                        tempServeRssi3 = tempServeRssi;
                        lastBatch2 = lastBatch;
                        tempServeRssi4 = i + 1;
                        strArr = lines;
                        c = 0;
                    }
                } catch (RuntimeException e16) {
                    e = e16;
                    tempServeRssi = tempServeRssi3;
                    lastBatch = lastBatch2;
                } catch (Exception e17) {
                    e = e17;
                    tempServeRssi = tempServeRssi3;
                    lastBatch = lastBatch2;
                    LogUtil.e("getCommStdDataSet, " + e.getMessage());
                    tempServeRssi3 = tempServeRssi;
                    lastBatch2 = lastBatch;
                    tempServeRssi4 = i + 1;
                    strArr = lines;
                    c = 0;
                }
                tempServeRssi4 = i + 1;
                strArr = lines;
                c = 0;
            }
            stdDataSet.setTotalCnt(rdCount);
            list.add(Constant.MAINAP_TAG);
            LogUtil.d("rdCount:" + rdCount);
        } catch (RuntimeException e18) {
            LogUtil.e("getCommStdDataSet, " + e18.getMessage());
        } catch (Exception e19) {
            LogUtil.e("getCommStdDataSet, " + e19.getMessage());
        }
        return stdDataSet;
    }

    /* JADX WARNING: type inference failed for: r4v0 */
    /* JADX WARNING: type inference failed for: r4v1 */
    /* JADX WARNING: type inference failed for: r4v3 */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x01fb, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x0211, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:134:0x0212, code lost:
        r28 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x0215, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x0216, code lost:
        r26 = r2;
        r2 = r18;
        r28 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:0x021d, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x021e, code lost:
        r26 = r2;
        r2 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x016b, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x016c, code lost:
        r2 = null;
        r28 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0171, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0172, code lost:
        r2 = null;
        r28 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x0177, code lost:
        r0 = e;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x0211 A[ExcHandler: RuntimeException (e java.lang.RuntimeException), PHI: r2 r14 r26 
      PHI: (r2v39 'tempScanRssis' java.util.List<java.lang.Integer>) = (r2v41 'tempScanRssis' java.util.List<java.lang.Integer>), (r2v41 'tempScanRssis' java.util.List<java.lang.Integer>), (r2v42 'tempScanRssis' java.util.List<java.lang.Integer>), (r2v42 'tempScanRssis' java.util.List<java.lang.Integer>) binds: [B:129:0x0204, B:130:?, B:124:0x01f4, B:125:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r14v8 'tempServeRssi' int) = (r14v2 'tempServeRssi' int), (r14v2 'tempServeRssi' int), (r14v10 'tempServeRssi' int), (r14v10 'tempServeRssi' int) binds: [B:129:0x0204, B:130:?, B:124:0x01f4, B:125:?] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r26v7 'size2' int) = (r26v9 'size2' int), (r26v9 'size2' int), (r26v10 'size2' int), (r26v10 'size2' int) binds: [B:129:0x0204, B:130:?, B:124:0x01f4, B:125:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:124:0x01f4] */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x021d A[ExcHandler: Exception (e java.lang.Exception), PHI: r14 
      PHI: (r14v6 'tempServeRssi' int) = (r14v2 'tempServeRssi' int), (r14v2 'tempServeRssi' int), (r14v10 'tempServeRssi' int), (r14v10 'tempServeRssi' int) binds: [B:106:0x01bd, B:108:0x01c1, B:121:0x01ea, B:122:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:106:0x01bd] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0177 A[ExcHandler: NumberFormatException (e java.lang.NumberFormatException), Splitter:B:74:0x0129] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private StdDataSet getFilterMobileAps2StdDataSet(String place, ParameterInfo param, String[] lines, List<String> macLst) {
        HashMap<String, Integer> tempHp;
        String[] wds;
        String lastBatch;
        int standardBatch;
        String[] wds2;
        HashMap<String, Integer> tempHp2;
        HashMap<String, Integer> tempHp3;
        String curBatch;
        StdRecord tempStdRecord;
        int tempSize;
        int size2;
        List<Integer> tempScanRssis;
        int tempSize2;
        String[] wds3;
        String[] strArr = lines;
        List<String> list = macLst;
        StdDataSet stdDataSet = new StdDataSet();
        if (param == null || strArr == null || list == null || strArr.length == 0 || macLst.size() == 0) {
            return stdDataSet;
        }
        HashMap<String, Integer> tempHp4 = new HashMap<>();
        HashMap<String, String> macSsidHp = new HashMap<>();
        try {
            stdDataSet.setMacLst(list);
            ? r4 = 0;
            int rdCount = 0;
            String lastBatch2 = "";
            int standardBatch2 = 0;
            int tempServeRssi = 0;
            while (true) {
                int i = tempServeRssi;
                if (i >= strArr.length) {
                    break;
                }
                try {
                    wds = strArr[i].split(",");
                    int tempServeRssi2 = 0;
                    if (wds.length >= param.getScanWifiStart()) {
                        if (wds[r4] == null) {
                            String[] strArr2 = wds;
                            wds = r4;
                            tempHp = tempHp4;
                        } else if (wds[r4].equals("")) {
                            String[] strArr3 = wds;
                            wds = r4;
                            tempHp = tempHp4;
                        } else if (wds.length > param.getServingWiFiMAC()) {
                            try {
                                String curBatch2 = wds[r4];
                                String tempServeMac = wds[param.getServingWiFiMAC()];
                                if (tempServeMac != null) {
                                    if (!stdDataSet.getMacRecords().containsKey(tempServeMac)) {
                                        try {
                                            if (checkMacFormat(tempServeMac)) {
                                                try {
                                                    stdDataSet.getMacRecords().put(tempServeMac, new TMapList());
                                                } catch (RuntimeException e) {
                                                    e = e;
                                                    String[] strArr4 = wds;
                                                    tempHp2 = tempHp4;
                                                    wds2 = null;
                                                } catch (Exception e2) {
                                                    e = e2;
                                                    String[] strArr5 = wds;
                                                    tempHp3 = tempHp4;
                                                    wds = null;
                                                    try {
                                                        LogUtil.e("getFilterMobileAps2StdDataSet 2 , " + e.getMessage());
                                                    } catch (RuntimeException e3) {
                                                        e = e3;
                                                        lastBatch = lastBatch2;
                                                        standardBatch = standardBatch2;
                                                        LogUtil.e("getFilterMobileAps2StdDataSet 6, " + e.getMessage());
                                                        standardBatch2 = standardBatch;
                                                        lastBatch2 = lastBatch;
                                                        tempServeRssi = i + 1;
                                                        r4 = wds;
                                                        tempHp4 = tempHp;
                                                        strArr = lines;
                                                    } catch (Exception e4) {
                                                        e = e4;
                                                        lastBatch = lastBatch2;
                                                        standardBatch = standardBatch2;
                                                        try {
                                                            LogUtil.e("getFilterMobileAps2StdDataSet 7, " + e.getMessage());
                                                            standardBatch2 = standardBatch;
                                                            lastBatch2 = lastBatch;
                                                            tempServeRssi = i + 1;
                                                            r4 = wds;
                                                            tempHp4 = tempHp;
                                                            strArr = lines;
                                                        } catch (RuntimeException e5) {
                                                            e = e5;
                                                            LogUtil.e("getFilterMobileAps2StdDataSet 8, " + e.getMessage());
                                                            return stdDataSet;
                                                        } catch (Exception e6) {
                                                            e = e6;
                                                            LogUtil.e("getFilterMobileAps2StdDataSet 9, " + e.getMessage());
                                                            return stdDataSet;
                                                        }
                                                    }
                                                    tempServeRssi = i + 1;
                                                    r4 = wds;
                                                    tempHp4 = tempHp;
                                                    strArr = lines;
                                                }
                                            }
                                        } catch (RuntimeException e7) {
                                            e = e7;
                                            String[] strArr6 = wds;
                                            wds2 = r4;
                                            tempHp2 = tempHp4;
                                            LogUtil.e("getFilterMobileAps2StdDataSet 1 , " + e.getMessage());
                                            tempServeRssi = i + 1;
                                            r4 = wds;
                                            tempHp4 = tempHp;
                                            strArr = lines;
                                        } catch (Exception e8) {
                                            e = e8;
                                            String[] strArr7 = wds;
                                            wds = r4;
                                            tempHp3 = tempHp4;
                                            LogUtil.e("getFilterMobileAps2StdDataSet 2 , " + e.getMessage());
                                            tempServeRssi = i + 1;
                                            r4 = wds;
                                            tempHp4 = tempHp;
                                            strArr = lines;
                                        }
                                    }
                                    try {
                                        if (!curBatch2.equals(lastBatch2)) {
                                            standardBatch2++;
                                            lastBatch2 = curBatch2;
                                        }
                                        standardBatch = standardBatch2;
                                        curBatch = curBatch2;
                                    } catch (RuntimeException e9) {
                                        e = e9;
                                        String[] strArr8 = wds;
                                        tempHp2 = tempHp4;
                                        wds2 = null;
                                        LogUtil.e("getFilterMobileAps2StdDataSet 1 , " + e.getMessage());
                                        tempServeRssi = i + 1;
                                        r4 = wds;
                                        tempHp4 = tempHp;
                                        strArr = lines;
                                    } catch (Exception e10) {
                                        e = e10;
                                        String[] strArr9 = wds;
                                        tempHp3 = tempHp4;
                                        wds = null;
                                        LogUtil.e("getFilterMobileAps2StdDataSet 2 , " + e.getMessage());
                                        tempServeRssi = i + 1;
                                        r4 = wds;
                                        tempHp4 = tempHp;
                                        strArr = lines;
                                    }
                                    try {
                                        StdRecord tempStdRecord2 = new StdRecord(standardBatch);
                                        String str = curBatch;
                                        if (param.getTimestamp() < wds.length) {
                                            try {
                                                tempStdRecord = tempStdRecord2;
                                                tempStdRecord.setTimeStamp(wds[param.getTimestamp()]);
                                            } catch (RuntimeException e11) {
                                                e = e11;
                                                tempHp = tempHp4;
                                                lastBatch = lastBatch2;
                                            } catch (Exception e12) {
                                                e = e12;
                                                tempHp = tempHp4;
                                                lastBatch = lastBatch2;
                                                wds = null;
                                                LogUtil.e("getFilterMobileAps2StdDataSet 7, " + e.getMessage());
                                                standardBatch2 = standardBatch;
                                                lastBatch2 = lastBatch;
                                                tempServeRssi = i + 1;
                                                r4 = wds;
                                                tempHp4 = tempHp;
                                                strArr = lines;
                                            }
                                        } else {
                                            tempStdRecord = tempStdRecord2;
                                            tempStdRecord.setTimeStamp("0");
                                        }
                                        List<Integer> tempScanRssis2 = new ArrayList<>();
                                        int tempSize3 = wds.length;
                                        tempHp4.clear();
                                        int k = param.getScanWifiStart();
                                        while (true) {
                                            lastBatch = lastBatch2;
                                            tempSize = tempSize3;
                                            int k2 = k;
                                            if (k2 >= tempSize) {
                                                break;
                                            }
                                            try {
                                                wds3 = wds;
                                                try {
                                                    String[] tempScanWifiInfo = wds[k2].split(param.getWifiSeperate());
                                                    tempSize2 = tempSize;
                                                    if (tempScanWifiInfo.length >= 4) {
                                                        try {
                                                            String tempMac = tempScanWifiInfo[param.getScanMAC()];
                                                            if (checkMacFormat(tempMac)) {
                                                                String str2 = tempMac;
                                                                Integer tempRssi = Integer.valueOf(Integer.parseInt(tempScanWifiInfo[param.getScanRSSI()].split("\\.")[0]));
                                                                tempHp4.put(tempScanWifiInfo[param.getScanMAC()], tempRssi);
                                                                Integer num = tempRssi;
                                                                macSsidHp.put(tempScanWifiInfo[param.getScanMAC()], tempScanWifiInfo[param.getScanSSID()]);
                                                            }
                                                        } catch (NumberFormatException e13) {
                                                        }
                                                    }
                                                } catch (NumberFormatException e14) {
                                                    e = e14;
                                                    tempSize2 = tempSize;
                                                    try {
                                                        LogUtil.e("getFilterMobileAps2StdDataSet 3, " + e.getMessage());
                                                        k = k2 + 1;
                                                        lastBatch2 = lastBatch;
                                                        wds = wds3;
                                                        tempSize3 = tempSize2;
                                                        String[] strArr10 = lines;
                                                    } catch (RuntimeException e15) {
                                                        e = e15;
                                                        tempHp = tempHp4;
                                                    } catch (Exception e16) {
                                                        e = e16;
                                                        tempHp = tempHp4;
                                                        wds = null;
                                                        LogUtil.e("getFilterMobileAps2StdDataSet 7, " + e.getMessage());
                                                        standardBatch2 = standardBatch;
                                                        lastBatch2 = lastBatch;
                                                        tempServeRssi = i + 1;
                                                        r4 = wds;
                                                        tempHp4 = tempHp;
                                                        strArr = lines;
                                                    }
                                                }
                                            } catch (NumberFormatException e17) {
                                                e = e17;
                                                wds3 = wds;
                                                tempSize2 = tempSize;
                                                LogUtil.e("getFilterMobileAps2StdDataSet 3, " + e.getMessage());
                                                k = k2 + 1;
                                                lastBatch2 = lastBatch;
                                                wds = wds3;
                                                tempSize3 = tempSize2;
                                                String[] strArr102 = lines;
                                            }
                                            k = k2 + 1;
                                            lastBatch2 = lastBatch;
                                            wds = wds3;
                                            tempSize3 = tempSize2;
                                            String[] strArr1022 = lines;
                                        }
                                        int i2 = tempSize;
                                        try {
                                            int size22 = macLst.size();
                                            int ik = 0;
                                            while (true) {
                                                int ik2 = ik;
                                                if (ik2 >= size22) {
                                                    break;
                                                }
                                                try {
                                                    String mac = list.get(ik2);
                                                    if (tempHp4.containsKey(mac)) {
                                                        if (mac.equals(tempServeMac)) {
                                                            try {
                                                                tempServeRssi2 = tempHp4.get(mac).intValue();
                                                            } catch (RuntimeException e18) {
                                                                e = e18;
                                                                size2 = size22;
                                                                tempHp = tempHp4;
                                                                tempScanRssis = tempScanRssis2;
                                                                LogUtil.e("getFilterMobileAps2StdDataSet 4, " + e.getMessage());
                                                                ik = ik2 + 1;
                                                                tempScanRssis2 = tempScanRssis;
                                                                size22 = size2;
                                                                tempHp4 = tempHp;
                                                            } catch (Exception e19) {
                                                                e = e19;
                                                                size2 = size22;
                                                                tempScanRssis = tempScanRssis2;
                                                                StringBuilder sb = new StringBuilder();
                                                                tempHp = tempHp4;
                                                                try {
                                                                    sb.append(" getFilterMobileAps2StdDataSet exception 5:");
                                                                    sb.append(e.getMessage());
                                                                    LogUtil.e(sb.toString());
                                                                    ik = ik2 + 1;
                                                                    tempScanRssis2 = tempScanRssis;
                                                                    size22 = size2;
                                                                    tempHp4 = tempHp;
                                                                } catch (RuntimeException e20) {
                                                                    e = e20;
                                                                    wds = null;
                                                                    LogUtil.e("getFilterMobileAps2StdDataSet 6, " + e.getMessage());
                                                                    standardBatch2 = standardBatch;
                                                                    lastBatch2 = lastBatch;
                                                                    tempServeRssi = i + 1;
                                                                    r4 = wds;
                                                                    tempHp4 = tempHp;
                                                                    strArr = lines;
                                                                } catch (Exception e21) {
                                                                    e = e21;
                                                                    wds = null;
                                                                    LogUtil.e("getFilterMobileAps2StdDataSet 7, " + e.getMessage());
                                                                    standardBatch2 = standardBatch;
                                                                    lastBatch2 = lastBatch;
                                                                    tempServeRssi = i + 1;
                                                                    r4 = wds;
                                                                    tempHp4 = tempHp;
                                                                    strArr = lines;
                                                                }
                                                            }
                                                        }
                                                        size2 = size22;
                                                        tempScanRssis = tempScanRssis2;
                                                        try {
                                                            tempScanRssis.add(tempHp4.get(mac));
                                                            String str3 = mac;
                                                        } catch (RuntimeException e22) {
                                                        } catch (Exception e23) {
                                                            e = e23;
                                                            StringBuilder sb2 = new StringBuilder();
                                                            tempHp = tempHp4;
                                                            sb2.append(" getFilterMobileAps2StdDataSet exception 5:");
                                                            sb2.append(e.getMessage());
                                                            LogUtil.e(sb2.toString());
                                                            ik = ik2 + 1;
                                                            tempScanRssis2 = tempScanRssis;
                                                            size22 = size2;
                                                            tempHp4 = tempHp;
                                                        }
                                                    } else {
                                                        size2 = size22;
                                                        tempScanRssis = tempScanRssis2;
                                                        String str4 = mac;
                                                        tempScanRssis.add(0);
                                                    }
                                                    tempHp = tempHp4;
                                                } catch (RuntimeException e24) {
                                                    e = e24;
                                                    size2 = size22;
                                                    tempHp = tempHp4;
                                                    tempScanRssis = tempScanRssis2;
                                                    LogUtil.e("getFilterMobileAps2StdDataSet 4, " + e.getMessage());
                                                    ik = ik2 + 1;
                                                    tempScanRssis2 = tempScanRssis;
                                                    size22 = size2;
                                                    tempHp4 = tempHp;
                                                } catch (Exception e25) {
                                                }
                                                ik = ik2 + 1;
                                                tempScanRssis2 = tempScanRssis;
                                                size22 = size2;
                                                tempHp4 = tempHp;
                                            }
                                            tempHp = tempHp4;
                                            List<Integer> tempScanRssis3 = tempScanRssis2;
                                            tempScanRssis3.add(Integer.valueOf(tempServeRssi2));
                                            tempStdRecord.setScanRssis(tempScanRssis3);
                                            tempStdRecord.setServeRssi(tempServeRssi2);
                                            stdDataSet.getMacRecords().get(tempServeMac).add(Integer.valueOf(standardBatch), tempStdRecord);
                                            rdCount++;
                                            standardBatch2 = standardBatch;
                                            lastBatch2 = lastBatch;
                                            wds = null;
                                        } catch (RuntimeException e26) {
                                            e = e26;
                                            tempHp = tempHp4;
                                            wds = null;
                                            LogUtil.e("getFilterMobileAps2StdDataSet 6, " + e.getMessage());
                                            standardBatch2 = standardBatch;
                                            lastBatch2 = lastBatch;
                                            tempServeRssi = i + 1;
                                            r4 = wds;
                                            tempHp4 = tempHp;
                                            strArr = lines;
                                        } catch (Exception e27) {
                                            e = e27;
                                            tempHp = tempHp4;
                                            wds = null;
                                            LogUtil.e("getFilterMobileAps2StdDataSet 7, " + e.getMessage());
                                            standardBatch2 = standardBatch;
                                            lastBatch2 = lastBatch;
                                            tempServeRssi = i + 1;
                                            r4 = wds;
                                            tempHp4 = tempHp;
                                            strArr = lines;
                                        }
                                    } catch (RuntimeException e28) {
                                        e = e28;
                                        tempHp = tempHp4;
                                        lastBatch = lastBatch2;
                                        wds = null;
                                        LogUtil.e("getFilterMobileAps2StdDataSet 6, " + e.getMessage());
                                        standardBatch2 = standardBatch;
                                        lastBatch2 = lastBatch;
                                        tempServeRssi = i + 1;
                                        r4 = wds;
                                        tempHp4 = tempHp;
                                        strArr = lines;
                                    } catch (Exception e29) {
                                        e = e29;
                                        tempHp = tempHp4;
                                        lastBatch = lastBatch2;
                                        wds = null;
                                        LogUtil.e("getFilterMobileAps2StdDataSet 7, " + e.getMessage());
                                        standardBatch2 = standardBatch;
                                        lastBatch2 = lastBatch;
                                        tempServeRssi = i + 1;
                                        r4 = wds;
                                        tempHp4 = tempHp;
                                        strArr = lines;
                                    }
                                }
                            } catch (RuntimeException e30) {
                                e = e30;
                                String[] strArr11 = wds;
                                wds2 = r4;
                                tempHp2 = tempHp4;
                                LogUtil.e("getFilterMobileAps2StdDataSet 1 , " + e.getMessage());
                                tempServeRssi = i + 1;
                                r4 = wds;
                                tempHp4 = tempHp;
                                strArr = lines;
                            } catch (Exception e31) {
                                e = e31;
                                String[] strArr12 = wds;
                                wds = r4;
                                tempHp3 = tempHp4;
                                LogUtil.e("getFilterMobileAps2StdDataSet 2 , " + e.getMessage());
                                tempServeRssi = i + 1;
                                r4 = wds;
                                tempHp4 = tempHp;
                                strArr = lines;
                            }
                        }
                        tempServeRssi = i + 1;
                        r4 = wds;
                        tempHp4 = tempHp;
                        strArr = lines;
                    }
                    wds = r4;
                    tempHp = tempHp4;
                } catch (RuntimeException e32) {
                    e = e32;
                    wds = r4;
                    tempHp = tempHp4;
                    lastBatch = lastBatch2;
                    standardBatch = standardBatch2;
                    LogUtil.e("getFilterMobileAps2StdDataSet 6, " + e.getMessage());
                    standardBatch2 = standardBatch;
                    lastBatch2 = lastBatch;
                    tempServeRssi = i + 1;
                    r4 = wds;
                    tempHp4 = tempHp;
                    strArr = lines;
                } catch (Exception e33) {
                    e = e33;
                    wds = r4;
                    tempHp = tempHp4;
                    lastBatch = lastBatch2;
                    standardBatch = standardBatch2;
                    LogUtil.e("getFilterMobileAps2StdDataSet 7, " + e.getMessage());
                    standardBatch2 = standardBatch;
                    lastBatch2 = lastBatch;
                    tempServeRssi = i + 1;
                    r4 = wds;
                    tempHp4 = tempHp;
                    strArr = lines;
                }
                tempServeRssi = i + 1;
                r4 = wds;
                tempHp4 = tempHp;
                strArr = lines;
            }
            stdDataSet.setTotalCnt(rdCount);
            list.add(Constant.MAINAP_TAG);
            LogUtil.d("rdCount:" + rdCount);
            int i3 = rdCount;
            stdDataSet = filterMobileAps2(place, stdDataSet, list, param, macSsidHp);
        } catch (RuntimeException e34) {
            e = e34;
            HashMap<String, Integer> hashMap = tempHp4;
            LogUtil.e("getFilterMobileAps2StdDataSet 8, " + e.getMessage());
            return stdDataSet;
        } catch (Exception e35) {
            e = e35;
            HashMap<String, Integer> hashMap2 = tempHp4;
            LogUtil.e("getFilterMobileAps2StdDataSet 9, " + e.getMessage());
            return stdDataSet;
        }
        return stdDataSet;
    }

    private boolean saveStdDataSetToFile(StdDataSet stdDataSet, String place, ParameterInfo param) {
        StdDataSet stdDataSet2 = stdDataSet;
        String str = place;
        ParameterInfo parameterInfo = param;
        if (stdDataSet2 == null) {
            LogUtil.d("saveStdDataSetToFile,null == stdDataSet");
            return false;
        } else if (stdDataSet.getMacLst() == null || stdDataSet.getMacLst().size() == 0) {
            LogUtil.d("saveStdDataSetToFile,null == getMacLst or getMacLst = 0");
            return false;
        } else if (str == null) {
            LogUtil.d("saveStdDataSetToFile,null == place ");
            return false;
        } else if (parameterInfo == null) {
            LogUtil.d("saveStdDataSetToFile,null == param ");
            return false;
        } else {
            String dataFilePath = null;
            try {
                dataFilePath = getStdFilePath(str, parameterInfo);
                LogUtil.i(" saveStdDataSetToFile save begin:" + str + ",dataFilePath:" + dataFilePath);
                if (!FileUtils.delFile(dataFilePath)) {
                    LogUtil.d(" saveStdDataSetToFile failure ,FileUtils.delFile(dataFilePath),dataFilePath:" + dataFilePath);
                    return false;
                }
                StringBuilder trainDataSb = new StringBuilder();
                int size = param.isMainAp() ? stdDataSet.getMacLst().size() : stdDataSet.getMacLst().size() - 1;
                trainDataSb.append("batch,label,timestamp,link_speed,");
                int validMacCnt = 0;
                int validMacCnt2 = 0;
                while (true) {
                    int i = validMacCnt2;
                    if (i >= size) {
                        break;
                    }
                    try {
                        String tempMac = stdDataSet.getMacLst().get(i);
                        if (!tempMac.equals("0")) {
                            validMacCnt++;
                            trainDataSb.append(tempMac);
                            trainDataSb.append(",");
                        }
                    } catch (RuntimeException e) {
                        LogUtil.e("saveStdDataSetToFile, " + e.getMessage());
                    } catch (Exception e2) {
                        LogUtil.e("saveStdDataSetToFile, " + e2.getMessage());
                    }
                    validMacCnt2 = i + 1;
                }
                stdDataSet2.setValidMacCnt(validMacCnt);
                List<Integer> macsIndexLst = stdDataSet.getMacIndexLst();
                if (macsIndexLst == null) {
                    macsIndexLst = new ArrayList<>();
                    for (int i2 = 0; i2 < size; i2++) {
                        macsIndexLst.add(Integer.valueOf(i2));
                    }
                }
                List<Integer> macsIndexLst2 = macsIndexLst;
                trainDataSb.deleteCharAt(trainDataSb.length() - 1);
                trainDataSb.append(Constant.getLineSeperate());
                ArrayList arrayList = new ArrayList();
                Iterator<Map.Entry<String, TMapList<Integer, StdRecord>>> it = stdDataSet.getMacRecords().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, TMapList<Integer, StdRecord>> entry = it.next();
                    String key = entry.getKey();
                    for (Map.Entry entry2 : entry.getValue().entrySet()) {
                        Integer num = (Integer) entry2.getKey();
                        Iterator<Map.Entry<String, TMapList<Integer, StdRecord>>> it2 = it;
                        List<StdRecord> tempStdRecords = (List) entry2.getValue();
                        for (StdRecord tempStdRecord : tempStdRecords) {
                            arrayList.add(tempStdRecord);
                            tempStdRecords = tempStdRecords;
                        }
                        it = it2;
                        StdDataSet stdDataSet3 = stdDataSet;
                    }
                    Iterator<Map.Entry<String, TMapList<Integer, StdRecord>>> it3 = it;
                    StdDataSet stdDataSet4 = stdDataSet;
                }
                Collections.sort(arrayList, new Comparator<StdRecord>() {
                    public int compare(StdRecord o1, StdRecord o2) {
                        return o1.getBatch() - o2.getBatch();
                    }
                });
                int size2 = arrayList.size();
                int i3 = 0;
                while (true) {
                    int i4 = i3;
                    if (i4 >= size2) {
                        break;
                    }
                    try {
                        StdRecord tempStdRecord2 = (StdRecord) arrayList.get(i4);
                        trainDataSb.append(String.valueOf(tempStdRecord2.getBatch()));
                        trainDataSb.append(",0,");
                        trainDataSb.append(tempStdRecord2.getTimeStamp());
                        trainDataSb.append(",0");
                        for (Integer index : macsIndexLst2) {
                            trainDataSb.append(",");
                            trainDataSb.append(tempStdRecord2.getScanRssis().get(index.intValue()));
                        }
                        trainDataSb.append(Constant.getLineSeperate());
                    } catch (RuntimeException e3) {
                        LogUtil.e("saveStdDataSetToFile, " + e3.getMessage());
                    } catch (Exception e4) {
                        LogUtil.e("saveStdDataSetToFile, " + e4.getMessage());
                    }
                    i3 = i4 + 1;
                }
                if (!FileUtils.saveFile(dataFilePath, trainDataSb.toString())) {
                    LogUtil.d(" saveStdDataSetToFile save failure:" + str + ",dataFilePath:" + dataFilePath);
                    return false;
                }
                LogUtil.i(" saveStdDataSetToFile save success:" + str + ",dataFilePath:" + dataFilePath);
                return true;
            } catch (RuntimeException e5) {
                LogUtil.e("saveStdDataSetToFile, " + e5.getMessage());
            } catch (Exception e6) {
                LogUtil.e("saveStdDataSetToFile, " + e6.getMessage());
            }
        }
    }

    public StdDataSet filterMobileAps2(String place, StdDataSet stdDataSet, List<String> macLst, ParameterInfo param, HashMap<String, String> macSsidHp) {
        int i;
        StdDataSet stdDataSet2 = stdDataSet;
        List<String> list = macLst;
        HashMap<String, String> hashMap = macSsidHp;
        if (stdDataSet2 == null) {
            LogUtil.d("filterMobileAps2,null == stdDataSet");
            return stdDataSet2;
        } else if (list == null || macLst.size() == 0) {
            String str = place;
            LogUtil.d("filterMobileAps2,null == macLst or macLst.size == 0");
            return stdDataSet2;
        } else if (hashMap == null || macSsidHp.size() == 0) {
            String str2 = place;
            LogUtil.d("filterMobileAps2,null == macSsidHp or macSsidHp.size == 0");
            return stdDataSet2;
        } else {
            HashMap hashMap2 = new HashMap();
            TMapSet tMapSet = new TMapSet();
            List<String> allMobielMacs = new ArrayList<>();
            List<Integer> macIndexLst = null;
            try {
                Iterator<Map.Entry<String, TMapList<Integer, StdRecord>>> it = stdDataSet.getMacRecords().entrySet().iterator();
                while (true) {
                    i = 0;
                    if (!it.hasNext()) {
                        break;
                    }
                    Map.Entry<String, TMapList<Integer, StdRecord>> entry = it.next();
                    try {
                        hashMap2.clear();
                        Iterator it2 = entry.getValue().entrySet().iterator();
                        while (it2.hasNext()) {
                            Map.Entry entry2 = (Map.Entry) it2.next();
                            Map.Entry entry3 = entry2;
                            Iterator it3 = it2;
                            Map.Entry<String, TMapList<Integer, StdRecord>> entry4 = entry;
                            try {
                                computeBatchFp(hashMap2, (Integer) entry2.getKey(), (List) entry2.getValue(), list, param);
                                it2 = it3;
                                entry = entry4;
                            } catch (RuntimeException e) {
                                e = e;
                                LogUtil.e("filterMobileAps2, " + e.getMessage());
                            } catch (Exception e2) {
                                e = e2;
                                LogUtil.e("filterMobileAps2, " + e.getMessage());
                            }
                        }
                        List<String> tempMobileMacs = filterBatchFp(hashMap2, list);
                        int size = tempMobileMacs.size();
                        while (true) {
                            int i2 = i;
                            if (i2 >= size) {
                                break;
                            }
                            String tempSsid = hashMap.get(tempMobileMacs.get(i2));
                            if (tempSsid != null) {
                                tMapSet.add(tempSsid, tempMobileMacs.get(i2));
                            }
                            i = i2 + 1;
                        }
                    } catch (RuntimeException e3) {
                        e = e3;
                        Map.Entry<String, TMapList<Integer, StdRecord>> entry5 = entry;
                        LogUtil.e("filterMobileAps2, " + e.getMessage());
                    } catch (Exception e4) {
                        e = e4;
                        Map.Entry<String, TMapList<Integer, StdRecord>> entry6 = entry;
                        LogUtil.e("filterMobileAps2, " + e.getMessage());
                    }
                }
                Set<String> testMobileMacs = new HashSet<>();
                for (Map.Entry<String, Set<String>> entry7 : tMapSet.entrySet()) {
                    List<String> tempFilterMacs = addMobileAp(entry7.getKey(), entry7.getValue().iterator(), 2);
                    testMobileMacs.addAll(tempFilterMacs);
                    if (tempFilterMacs.size() > 0) {
                        allMobielMacs.addAll(tempFilterMacs);
                        LogUtil.d("filterMobileAps,add mobileAp success." + tempFilterMacs.size());
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append("isMainAp:");
                sb.append(String.valueOf(param.isMainAp()));
                sb.append(",place:");
                try {
                    sb.append(place);
                    sb.append(",filterMobileAps2 mobiles Ap is,mac:");
                    sb.append(testMobileMacs.toString());
                    sb.append(Constant.getLineSeperate());
                    LogUtil.wtLogFile(sb.toString());
                    Set<String> setMobileMacs = new HashSet<>(allMobielMacs);
                    macIndexLst = new ArrayList<>();
                    int size2 = macLst.size() - 1;
                    while (true) {
                        int i3 = i;
                        if (i3 >= size2) {
                            break;
                        }
                        if (!setMobileMacs.contains(list.get(i3))) {
                            macIndexLst.add(Integer.valueOf(i3));
                        } else {
                            list.set(i3, "0");
                        }
                        i = i3 + 1;
                    }
                    stdDataSet2.setFilter2MobileApCnt(setMobileMacs.size());
                } catch (RuntimeException e5) {
                    e = e5;
                    LogUtil.e("filterMobileAps2, " + e.getMessage());
                    stdDataSet2.setMacIndexLst(macIndexLst);
                    return stdDataSet2;
                } catch (Exception e6) {
                    e = e6;
                    LogUtil.e("filterMobileAps2, " + e.getMessage());
                    stdDataSet2.setMacIndexLst(macIndexLst);
                    return stdDataSet2;
                }
            } catch (RuntimeException e7) {
                e = e7;
                String str3 = place;
                LogUtil.e("filterMobileAps2, " + e.getMessage());
                stdDataSet2.setMacIndexLst(macIndexLst);
                return stdDataSet2;
            } catch (Exception e8) {
                e = e8;
                String str4 = place;
                LogUtil.e("filterMobileAps2, " + e.getMessage());
                stdDataSet2.setMacIndexLst(macIndexLst);
                return stdDataSet2;
            }
            stdDataSet2.setMacIndexLst(macIndexLst);
            return stdDataSet2;
        }
    }

    public List<String> filterBatchFp(HashMap<String, ArrayList<Float>> colsRssis, List<String> macLst) {
        List<String> mobileSsids = new ArrayList<>();
        if (colsRssis == null || colsRssis.size() == 0) {
            LogUtil.d("filterBatchFp,null == colsRssis");
            return mobileSsids;
        } else if (macLst == null || macLst.size() == 0) {
            LogUtil.d("filterBatchFp,null == macLst or size == 0");
            return mobileSsids;
        } else {
            try {
                MobileApCheckParamInfo param = ParamManager.getInstance().getMobileApCheckParamInfo();
                List<Integer> topKMainApRssiIndexs = getTopKRssiIndexs(colsRssis.get(Constant.MAINAP_TAG), param);
                if (topKMainApRssiIndexs != null) {
                    if (topKMainApRssiIndexs.size() != 0) {
                        GetStd getStd = new GetStd();
                        int i = macLst.size();
                        while (true) {
                            i--;
                            if (i <= -1) {
                                break;
                            }
                            try {
                                ArrayList<Float> tempFitKColRssis = getFitKColRssiLst(topKMainApRssiIndexs, colsRssis.get(macLst.get(i)));
                                if (tempFitKColRssis.size() != 0) {
                                    if (((double) getStd.getStandardDevition(tempFitKColRssis)) > ((double) param.getMobileApMinStd())) {
                                        mobileSsids.add(macLst.get(i));
                                    }
                                }
                            } catch (RuntimeException e) {
                                LogUtil.e("filterBatchFp, " + e.getMessage());
                            } catch (Exception e2) {
                                LogUtil.e("filterBatchFp, " + e2.getMessage());
                            }
                        }
                        return mobileSsids;
                    }
                }
                return mobileSsids;
            } catch (RuntimeException e3) {
                LogUtil.e("filterBatchFp, " + e3.getMessage());
            } catch (Exception e4) {
                LogUtil.e("filterBatchFp, " + e4.getMessage());
            }
        }
    }

    private ArrayList<Float> getFitKColRssiLst(List<Integer> topKIndexs, ArrayList<Float> colRssis) {
        ArrayList<Float> tempFitKColRssis = new ArrayList<>();
        if (topKIndexs == null || topKIndexs.size() == 0 || colRssis == null || colRssis.size() == 0) {
            return tempFitKColRssis;
        }
        try {
            int size = colRssis.size() - 1;
            for (Integer index : topKIndexs) {
                if (index.intValue() <= size) {
                    tempFitKColRssis.add(colRssis.get(index.intValue()));
                }
            }
        } catch (RuntimeException e) {
            LogUtil.e("getFitKColRssiLst, " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("getFitKColRssiLst, " + e2.getMessage());
        }
        return tempFitKColRssis;
    }

    private List<Integer> getTopKRssiIndexs(ArrayList<Float> mainApRssiLst, MobileApCheckParamInfo param) {
        List<Integer> result = new ArrayList<>();
        if (mainApRssiLst == null || mainApRssiLst.size() == 0) {
            LogUtil.d("getTopKRssiIndexs,null == mainApRssiLst");
            return result;
        } else if (param == null) {
            LogUtil.d("getTopKRssiIndexs,null == param");
            return result;
        } else {
            try {
                Float maxVal = (Float) Collections.max(mainApRssiLst);
                int size = mainApRssiLst.size();
                for (int i = 0; i < size; i++) {
                    if (maxVal.floatValue() - mainApRssiLst.get(i).floatValue() <= ((float) param.getMobileApMinRange())) {
                        result.add(Integer.valueOf(i));
                    }
                }
            } catch (RuntimeException e) {
                LogUtil.e("getTopKRssiIndexs, " + e.getMessage());
            } catch (Exception e2) {
                LogUtil.e("getTopKRssiIndexs, " + e2.getMessage());
            }
            return result;
        }
    }

    public void computeBatchFp(HashMap<String, ArrayList<Float>> colsRssis, Integer batch, List<StdRecord> stdRecordLst, List<String> macLst, ParameterInfo param) {
        HashMap<String, ArrayList<Float>> hashMap = colsRssis;
        int batchNum = stdRecordLst.size();
        if (batchNum != 0) {
            int macsCnt = macLst.size();
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 < macsCnt) {
                    int nonzero_sum = 0;
                    int nonzero_num = 0;
                    int nonzero_num2 = 0;
                    while (true) {
                        int k = nonzero_num2;
                        if (k >= batchNum) {
                            break;
                        }
                        try {
                            int tempVal = stdRecordLst.get(k).getScanRssis().get(i2).intValue();
                            if (tempVal != 0) {
                                nonzero_num++;
                                nonzero_sum += tempVal;
                            }
                        } catch (RuntimeException e) {
                            LogUtil.e("computeBatchFp, " + e.getMessage());
                        } catch (Exception e2) {
                            try {
                                LogUtil.e("computeBatchFp, " + e2.getMessage());
                            } catch (RuntimeException e3) {
                                e = e3;
                                List<String> list = macLst;
                                LogUtil.e("computeBatchFp, " + e.getMessage());
                                i = i2 + 1;
                            } catch (Exception e4) {
                                e = e4;
                                List<String> list2 = macLst;
                                LogUtil.e("computeBatchFp, " + e.getMessage());
                                i = i2 + 1;
                            }
                        }
                        nonzero_num2 = k + 1;
                    }
                    List<StdRecord> list3 = stdRecordLst;
                    float avg = -100.0f;
                    if (((float) nonzero_num) / ((float) batchNum) > param.getWeightParam()) {
                        avg = ((float) nonzero_sum) / ((float) nonzero_num);
                    }
                    try {
                        String tempMac = macLst.get(i2);
                        if (!hashMap.containsKey(tempMac)) {
                            hashMap.put(tempMac, new ArrayList());
                        }
                        hashMap.get(tempMac).add(Float.valueOf(avg));
                    } catch (RuntimeException e5) {
                        e = e5;
                        LogUtil.e("computeBatchFp, " + e.getMessage());
                        i = i2 + 1;
                    } catch (Exception e6) {
                        e = e6;
                        LogUtil.e("computeBatchFp, " + e.getMessage());
                        i = i2 + 1;
                    }
                    i = i2 + 1;
                } else {
                    List<StdRecord> list4 = stdRecordLst;
                    List<String> list5 = macLst;
                    return;
                }
            }
        }
    }
}
