package com.android.server.hidata.wavemapping.modelservice;

import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.entity.FingerInfo;
import com.android.server.hidata.wavemapping.entity.IdentifyResult;
import com.android.server.hidata.wavemapping.entity.ModelInfo;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.util.FileUtils;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class IdentifyService extends ModelBaseService {
    public static final String TAG = ("WMapping." + IdentifyService.class.getSimpleName());

    private int getShareMacNum(int[] words, HashMap<String, Integer> noneZeroBssid, ModelInfo model) {
        int shareMacs = 0;
        if (words == null) {
            LogUtil.d(" getShareMacNum words == null");
            return 0;
        } else if (noneZeroBssid == null) {
            LogUtil.d(" getShareMacNum noneZeroBssid == null");
            return 0;
        } else if (noneZeroBssid.size() == 0) {
            return 0;
        } else {
            try {
                int size = words.length;
                for (int i = 0; i < size; i++) {
                    if (words[i] != 0 && noneZeroBssid.containsKey(model.getBssidLst()[i])) {
                        shareMacs++;
                    }
                }
            } catch (Exception e) {
                LogUtil.d(" getShareMacNum ," + e.getMessage());
            }
            return shareMacs;
        }
    }

    private HashMap<String, Integer> getNoneZeroBssids(HashMap<String, Integer> bissiddatas, ModelInfo model) {
        HashMap<String, Integer> noneZeroBssid = new HashMap<>();
        if (bissiddatas == null) {
            LogUtil.d(" getNoneZeroBssids bissiddatas == null");
            return noneZeroBssid;
        }
        try {
            HashSet<String> setBssids = model.getSetBssids();
            for (Map.Entry entry : bissiddatas.entrySet()) {
                String bssid = (String) entry.getKey();
                Integer val = (Integer) entry.getValue();
                if (setBssids.contains(bssid) && val.intValue() != 0) {
                    noneZeroBssid.put(bssid, val);
                }
            }
        } catch (Exception e) {
            LogUtil.e(" getNoneZeroBssids ," + e.getMessage());
        }
        return noneZeroBssid;
    }

    private float getXtrainBssidLen(int[] dats, ParameterInfo param) {
        int i = -2;
        if (dats == null) {
            try {
                LogUtil.d(" getXtrainBssidLen dats == null");
                return (float) -2;
            } catch (Exception e) {
                LogUtil.e(" getXtrainBssidLen ," + e.getMessage());
            }
        } else {
            for (int dat : dats) {
                if (dat != 0) {
                    i++;
                }
            }
            return param.getKnnShareMacRatio() * ((float) i);
        }
    }

    private int compute_dist_share(int[] trainBssidVals, HashMap<String, Integer> noneZeroTestBssid, ModelInfo model) {
        int maxDiff = 0;
        if (trainBssidVals == null) {
            LogUtil.d(" compute_dist_share trainBssidVals == null");
            return 0;
        } else if (trainBssidVals.length == 0) {
            LogUtil.d(" compute_dist_share trainBssidVals.length == 0");
            return 0;
        } else if (noneZeroTestBssid == null) {
            LogUtil.d(" compute_dist_share noneZeroTestBssid == null ");
            return 0;
        } else if (noneZeroTestBssid.size() == 0) {
            LogUtil.d(" compute_dist_share noneZeroTestBssid.size() == 0 ");
            return 0;
        } else {
            try {
                int size = model.getBssidLst().length;
                String[] trainBssids = model.getBssidLst();
                for (int i = 0; i < size; i++) {
                    if (trainBssidVals[i] != 0) {
                        if (noneZeroTestBssid.containsKey(trainBssids[i])) {
                            int diff = trainBssidVals[i] - noneZeroTestBssid.get(trainBssids[i]).intValue();
                            int diff2 = diff * diff;
                            if (diff2 > maxDiff) {
                                maxDiff = diff2;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.e(" compute_dist_share," + e.getMessage());
            }
            return maxDiff;
        }
    }

    public static String getHashMapString(HashMap hashMap) {
        String resultStr = "";
        if (hashMap != null) {
            try {
                if (hashMap.size() != 0) {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry entry : hashMap.entrySet()) {
                        Object key = entry.getKey();
                        Object value = entry.getValue();
                        sb.append(key);
                        sb.append("=");
                        sb.append(value);
                        sb.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                    }
                    if (sb.lastIndexOf(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER) > 0) {
                        resultStr = sb.substring(0, sb.lastIndexOf(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER)).toString();
                    }
                    return resultStr;
                }
            } catch (Exception e) {
                LogUtil.e(" getHashMapString," + e.getMessage());
            }
        }
        return resultStr;
    }

    public void logIdentifyResult(String testLog, String place, ParameterInfo param) {
        if (LogUtil.getDebug_flag()) {
            String dataFilePath = getIdentifyLogFilePath(place, param);
            LogUtil.i(" indentifyLocation log begin:" + place + ",dataFilePath:" + dataFilePath);
            if (!FileUtils.writeFile(dataFilePath, testLog.toString())) {
                LogUtil.d(" indentifyLocation log failure.");
            }
        }
    }

    public int indentifyLocation(String place, FingerInfo fingerInfo, ParameterInfo param, ModelInfo model) {
        int maxPrelabel;
        int neighborNum;
        HashMap<String, Integer> noneZeroBssidVals;
        int[][] trainDatas;
        String str = place;
        ParameterInfo parameterInfo = param;
        ModelInfo modelInfo = model;
        if (str == null) {
            LogUtil.d("indentifyLocation failure,place == null");
            return -2;
        } else if (fingerInfo == null) {
            LogUtil.d("indentifyLocation failure,fingerInfo == null ");
            return -3;
        } else if (parameterInfo == null) {
            LogUtil.d("indentifyLocation failure,param == null ");
            return -4;
        } else {
            String place2 = str.replace(":", "").replace("-", "");
            LogUtil.i(" indentifyLocation begin:" + place2);
            if (!model.getPlace().equals(place2)) {
                LogUtil.d("indentifyLocation failure,place :" + place2 + " , place of modelInfo is " + model.getPlace());
                return -5;
            }
            int[][] trainDatas2 = model.getDatas();
            HashMap<String, Integer> noneZeroBssidVals2 = getNoneZeroBssids(fingerInfo.getBissiddatas(), modelInfo);
            float xtestBssidLen = param.getKnnShareMacRatio() * ((float) noneZeroBssidVals2.size());
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            int maxPrelabel2 = 0;
            try {
                int prelabelIndex = model.getBssidLst().length - 2;
                int cntIndex = model.getBssidLst().length - 1;
                int neighborNum2 = param.getNeighborNum();
                StringBuilder testLog = new StringBuilder();
                if (LogUtil.getDebug_flag()) {
                    try {
                        testLog.append("isMain:");
                        testLog.append(param.isMainAp());
                        testLog.append(",");
                        testLog.append(model.getModelName());
                        testLog.append(",KnnMaxDist:");
                        testLog.append(param.getKnnMaxDist());
                        testLog.append(",NeighborNum=");
                        testLog.append(param.getNeighborNum());
                        testLog.append(",noneZeroBssidVals=");
                        testLog.append(getHashMapString(noneZeroBssidVals2));
                        testLog.append(",");
                    } catch (Exception e) {
                        e = e;
                        int[][] iArr = trainDatas2;
                        HashMap<String, Integer> hashMap = noneZeroBssidVals2;
                    }
                }
                int loopSize = model.getDataLen();
                int i = 0;
                while (i < loopSize) {
                    int loopSize2 = loopSize;
                    try {
                        int shareMacsNum = getShareMacNum(trainDatas2[i], noneZeroBssidVals2, modelInfo);
                        if (LogUtil.getDebug_flag()) {
                            try {
                                testLog.append("shareMacsNum=");
                                testLog.append(shareMacsNum);
                                testLog.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                            } catch (Exception e2) {
                                e = e2;
                                int[][] iArr2 = trainDatas2;
                                HashMap<String, Integer> hashMap2 = noneZeroBssidVals2;
                            }
                        }
                        maxPrelabel = maxPrelabel2;
                        try {
                            if (((float) shareMacsNum) < getXtrainBssidLen(trainDatas2[i], parameterInfo)) {
                                try {
                                    if (LogUtil.getDebug_flag()) {
                                        testLog.append("trainDatas[");
                                        testLog.append(i);
                                        testLog.append("]=");
                                        testLog.append(getXtrainBssidLen(trainDatas2[i], parameterInfo));
                                        testLog.append(",");
                                    }
                                } catch (Exception e3) {
                                    e = e3;
                                    maxPrelabel2 = maxPrelabel;
                                    LogUtil.e("indentifyLocation :" + e.getMessage());
                                    return maxPrelabel2;
                                }
                            } else if (((float) shareMacsNum) >= xtestBssidLen) {
                                int maxDist = compute_dist_share(trainDatas2[i], noneZeroBssidVals2, modelInfo);
                                try {
                                    if (LogUtil.getDebug_flag()) {
                                        try {
                                            testLog.append("maxDist=");
                                            testLog.append(maxDist);
                                            testLog.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                                        } catch (Exception e4) {
                                            e = e4;
                                            int i2 = maxDist;
                                        }
                                    }
                                    if (maxDist < param.getKnnMaxDist()) {
                                        trainDatas = trainDatas2;
                                        try {
                                            arrayList.add(new IdentifyResult(maxDist, trainDatas2[i][prelabelIndex], trainDatas2[i][cntIndex]));
                                        } catch (Exception e5) {
                                            e = e5;
                                            int i3 = maxDist;
                                        }
                                    } else {
                                        trainDatas = trainDatas2;
                                    }
                                    int i4 = maxDist;
                                    i++;
                                    loopSize = loopSize2;
                                    maxPrelabel2 = maxPrelabel;
                                    trainDatas2 = trainDatas;
                                    modelInfo = model;
                                } catch (Exception e6) {
                                    e = e6;
                                    int[][] iArr3 = trainDatas2;
                                    int i5 = maxDist;
                                    maxPrelabel2 = maxPrelabel;
                                    LogUtil.e("indentifyLocation :" + e.getMessage());
                                    return maxPrelabel2;
                                }
                            } else if (LogUtil.getDebug_flag()) {
                                testLog.append("xtestBssidLen=");
                                testLog.append(xtestBssidLen);
                                testLog.append(",");
                            }
                            trainDatas = trainDatas2;
                            i++;
                            loopSize = loopSize2;
                            maxPrelabel2 = maxPrelabel;
                            trainDatas2 = trainDatas;
                            modelInfo = model;
                        } catch (Exception e7) {
                            e = e7;
                            int[][] iArr4 = trainDatas2;
                            maxPrelabel2 = maxPrelabel;
                            LogUtil.e("indentifyLocation :" + e.getMessage());
                            return maxPrelabel2;
                        }
                    } catch (Exception e8) {
                        e = e8;
                        int[][] iArr5 = trainDatas2;
                        int i6 = maxPrelabel2;
                        HashMap<String, Integer> hashMap3 = noneZeroBssidVals2;
                        LogUtil.e("indentifyLocation :" + e.getMessage());
                        return maxPrelabel2;
                    }
                }
                int i7 = loopSize;
                int[][] iArr6 = trainDatas2;
                maxPrelabel = maxPrelabel2;
                try {
                    if (LogUtil.getDebug_flag()) {
                        try {
                            testLog.append(",identifyRes.size=");
                            testLog.append(arrayList.size());
                            testLog.append(",");
                        } catch (Exception e9) {
                            e = e9;
                        }
                    }
                    if (arrayList.size() == 0) {
                        if (LogUtil.getDebug_flag()) {
                            testLog.append("\"unknown;idRes.len=0\"");
                            testLog.append(Constant.getLineSeperate());
                            logIdentifyResult(TimeUtil.getTime() + ",result=unknown;idRes.len=0," + testLog.toString(), place2, parameterInfo);
                        }
                        return -1;
                    }
                    Collections.sort(arrayList);
                    int size = arrayList.size();
                    int allCnt = 0;
                    for (int i8 = 0; i8 < size; i8++) {
                        allCnt += ((IdentifyResult) arrayList.get(i8)).getCnt();
                    }
                    if (LogUtil.getDebug_flag() != 0) {
                        testLog.append(",neighborNum=");
                        neighborNum = neighborNum2;
                        testLog.append(neighborNum);
                        testLog.append(",");
                    } else {
                        neighborNum = neighborNum2;
                    }
                    int leftSize = allCnt <= neighborNum ? allCnt : neighborNum;
                    int i9 = neighborNum;
                    int leftCnt = 0;
                    int i10 = 0;
                    while (i10 < leftSize && leftCnt < leftSize) {
                        int size2 = size;
                        leftCnt += ((IdentifyResult) arrayList.get(i10)).getCnt();
                        arrayList2.add((IdentifyResult) arrayList.get(i10));
                        i10++;
                        size = size2;
                    }
                    int[] results = new int[arrayList2.size()];
                    int loopSize3 = arrayList2.size();
                    int i11 = 0;
                    while (true) {
                        int leftCnt2 = leftCnt;
                        int i12 = i11;
                        if (i12 >= loopSize3) {
                            break;
                        }
                        int allCnt2 = allCnt;
                        results[i12] = ((IdentifyResult) arrayList2.get(i12)).getPreLabel();
                        i11 = i12 + 1;
                        leftCnt = leftCnt2;
                        allCnt = allCnt2;
                    }
                    HashMap<Integer, Integer> mpResult = new HashMap<>();
                    int loopSize4 = arrayList2.size();
                    int i13 = 0;
                    while (i13 < loopSize4) {
                        int[] results2 = results;
                        int loopSize5 = loopSize4;
                        IdentifyResult identifyResult = (IdentifyResult) arrayList2.get(i13);
                        Integer tempPreLabel = Integer.valueOf(identifyResult.getPreLabel());
                        if (mpResult.containsKey(tempPreLabel)) {
                            noneZeroBssidVals = noneZeroBssidVals2;
                            try {
                                mpResult.put(tempPreLabel, Integer.valueOf(mpResult.get(tempPreLabel).intValue() + identifyResult.getCnt()));
                            } catch (Exception e10) {
                                e = e10;
                                maxPrelabel2 = maxPrelabel;
                                LogUtil.e("indentifyLocation :" + e.getMessage());
                                return maxPrelabel2;
                            }
                        } else {
                            noneZeroBssidVals = noneZeroBssidVals2;
                            mpResult.put(tempPreLabel, Integer.valueOf(identifyResult.getCnt()));
                        }
                        i13++;
                        Integer num = tempPreLabel;
                        results = results2;
                        loopSize4 = loopSize5;
                        noneZeroBssidVals2 = noneZeroBssidVals;
                    }
                    int i14 = loopSize4;
                    HashMap<String, Integer> hashMap4 = noneZeroBssidVals2;
                    int maxPrelabelCnt = 0;
                    maxPrelabel2 = maxPrelabel;
                    for (Map.Entry<Integer, Integer> entry : mpResult.entrySet()) {
                        try {
                            HashMap<Integer, Integer> mpResult2 = mpResult;
                            if (entry.getValue().intValue() > maxPrelabelCnt) {
                                maxPrelabel2 = entry.getKey().intValue();
                                maxPrelabelCnt = entry.getValue().intValue();
                            }
                            mpResult = mpResult2;
                        } catch (Exception e11) {
                            e = e11;
                            LogUtil.e("indentifyLocation :" + e.getMessage());
                            return maxPrelabel2;
                        }
                    }
                    if (LogUtil.getDebug_flag()) {
                        testLog.append(Constant.getLineSeperate());
                        logIdentifyResult(TimeUtil.getTime() + ",result=" + maxPrelabel2 + "," + testLog.toString(), place2, parameterInfo);
                    }
                    LogUtil.d("indentifyLocation, result:" + maxPrelabel2);
                    if (maxPrelabel2 == 0) {
                        return -6;
                    }
                    return maxPrelabel2;
                } catch (Exception e12) {
                    e = e12;
                    HashMap<String, Integer> hashMap5 = noneZeroBssidVals2;
                    maxPrelabel2 = maxPrelabel;
                    LogUtil.e("indentifyLocation :" + e.getMessage());
                    return maxPrelabel2;
                }
            } catch (Exception e13) {
                e = e13;
                int[][] iArr7 = trainDatas2;
                HashMap<String, Integer> hashMap6 = noneZeroBssidVals2;
                LogUtil.e("indentifyLocation :" + e.getMessage());
                return maxPrelabel2;
            }
        }
    }
}
