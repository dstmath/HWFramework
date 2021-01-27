package com.android.server.hidata.wavemapping.modelservice;

import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.entity.FingerInfo;
import com.android.server.hidata.wavemapping.entity.IdentifyResult;
import com.android.server.hidata.wavemapping.entity.ModelInfo;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.util.CommUtil;
import com.android.server.hidata.wavemapping.util.FileUtils;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class IdentifyService extends ModelBaseService {
    private static final String COLON = ":";
    private static final String COMMA = ",";
    private static final String DASH = "-";
    private static final int DEFAULT_VALUE = -1;
    private static final int LIST_DEFAULT_CAPACITY = 10;
    private static final int MAP_DEFAULT_CAPACITY = 16;
    private static final String SEMICOLON = ";";
    public static final String TAG = ("WMapping." + IdentifyService.class.getSimpleName());

    private int getShareMacNum(int[] words, HashMap<String, Integer> noneZeroBssid, ModelInfo model) {
        int shareMacs = 0;
        if (words == null) {
            LogUtil.d(false, " getShareMacNum words == null", new Object[0]);
            return 0;
        } else if (noneZeroBssid == null) {
            LogUtil.d(false, " getShareMacNum noneZeroBssid == null", new Object[0]);
            return 0;
        } else if (noneZeroBssid.size() == 0) {
            return 0;
        } else {
            try {
                int size = words.length;
                for (int i = 0; i < size; i++) {
                    if (words[i] != 0 && noneZeroBssid.containsKey(model.getBssidList()[i])) {
                        shareMacs++;
                    }
                }
            } catch (Exception e) {
                LogUtil.d(false, "getShareMacNum failed by Exception", new Object[0]);
            }
            return shareMacs;
        }
    }

    private HashMap<String, Integer> getNoneZeroBssids(HashMap<String, Integer> bissiddatas, ModelInfo model) {
        HashMap<String, Integer> noneZeroBssid = new HashMap<>(16);
        if (bissiddatas == null) {
            LogUtil.d(false, " getNoneZeroBssids bissiddatas == null", new Object[0]);
            return noneZeroBssid;
        }
        try {
            HashSet<String> setBssids = model.getSetBssids();
            for (Map.Entry<String, Integer> entry : bissiddatas.entrySet()) {
                String bssid = entry.getKey();
                Integer val = entry.getValue();
                if (setBssids.contains(bssid) && val.intValue() != 0) {
                    noneZeroBssid.put(bssid, val);
                }
            }
        } catch (Exception e) {
            LogUtil.e(false, "getNoneZeroBssids failed by Exception", new Object[0]);
        }
        return noneZeroBssid;
    }

    private float getXtrainBssidLen(int[] dats, ParameterInfo param) {
        int i;
        Exception e;
        if (dats == null) {
            try {
                LogUtil.d(false, " getXtrainBssidLen dats == null", new Object[0]);
                return (float) -1;
            } catch (Exception e2) {
                e = e2;
                i = -1;
                LogUtil.e(false, "getXtrainBssidLen failed by Exception", new Object[0]);
                return param.getKnnShareMacRatio() * ((float) i);
            }
        } else {
            i = -1;
            for (int dat : dats) {
                try {
                    if (dat != 0) {
                        i++;
                    }
                } catch (Exception e3) {
                    e = e3;
                    LogUtil.e(false, "getXtrainBssidLen failed by Exception", new Object[0]);
                    return param.getKnnShareMacRatio() * ((float) i);
                }
            }
            return param.getKnnShareMacRatio() * ((float) i);
        }
    }

    private int computeDistShare(int[] trainBssidVals, HashMap<String, Integer> noneZeroTestBssid, ModelInfo model) {
        int maxDiff = 0;
        if (trainBssidVals == null) {
            LogUtil.d(false, " computeDistShare trainBssidVals == null", new Object[0]);
            return 0;
        } else if (trainBssidVals.length == 0) {
            LogUtil.d(false, " computeDistShare trainBssidVals.length == 0", new Object[0]);
            return 0;
        } else if (noneZeroTestBssid == null) {
            LogUtil.d(false, " computeDistShare noneZeroTestBssid == null ", new Object[0]);
            return 0;
        } else if (noneZeroTestBssid.size() == 0) {
            LogUtil.d(false, " computeDistShare noneZeroTestBssid.size() == 0 ", new Object[0]);
            return 0;
        } else {
            try {
                int size = model.getBssidList().length;
                String[] trainBssids = model.getBssidList();
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
                LogUtil.e(false, "computeDistShare failed by Exception", new Object[0]);
            }
            return maxDiff;
        }
    }

    public static String getHashMapString(HashMap hashMap) {
        if (hashMap != null) {
            try {
                if (hashMap.size() != 0) {
                    StringBuilder sb = new StringBuilder(16);
                    for (Map.Entry entry : hashMap.entrySet()) {
                        Object key = entry.getKey();
                        Object value = entry.getValue();
                        sb.append(key);
                        sb.append(":");
                        sb.append(value);
                        sb.append(";");
                    }
                    if (sb.lastIndexOf(";") > 0) {
                        return sb.substring(0, sb.lastIndexOf(";")).toString();
                    }
                    return "";
                }
            } catch (Exception e) {
                LogUtil.e(false, "getHashMapString failed by Exception", new Object[0]);
                return "";
            }
        }
        return "";
    }

    public void logIdentifyResult(String testLog, String place, ParameterInfo param) {
        if (LogUtil.getDebugFlag()) {
            String dataFilePath = getIdentifyLogFilePath(place, param);
            LogUtil.i(false, " identifyLocation log begin:%{private}s,dataFilePath:%{public}s", place, dataFilePath);
            if (!FileUtils.writeFile(dataFilePath, testLog.toString())) {
                LogUtil.d(false, " identifyLocation log failure.", new Object[0]);
            }
        }
    }

    public int identifyLocation(String place, FingerInfo fingerInfo, ParameterInfo param, ModelInfo model) {
        if (place == null) {
            LogUtil.d(false, "identifyLocation failure,place == null", new Object[0]);
            return -2;
        } else if (fingerInfo == null) {
            LogUtil.d(false, "identifyLocation failure,fingerInfo == null ", new Object[0]);
            return -3;
        } else if (param == null) {
            LogUtil.d(false, "identifyLocation failure,param == null ", new Object[0]);
            return -4;
        } else {
            String tempPlace = place.replace(":", "").replace("-", "");
            if (judgeModelPlace(model, tempPlace)) {
                return -5;
            }
            ArrayList<IdentifyResult> identifyRes = new ArrayList<>(10);
            ArrayList<IdentifyResult> leftIdentifyRes = new ArrayList<>(10);
            int result = 0;
            try {
                int neighborNum = param.getNeighborNum();
                StringBuilder testLog = new StringBuilder(16);
                processShareMacs(fingerInfo, param, model, testLog, identifyRes);
                if (judgeIdentifyResults(identifyRes, param, testLog, tempPlace, neighborNum)) {
                    return -1;
                }
                result = CommUtil.getModalNums(processIdentifyRes(leftIdentifyRes, identifyRes, neighborNum));
                if (LogUtil.getDebugFlag()) {
                    testLog.append(Constant.getLineSeparator());
                    logIdentifyResult(TimeUtil.getTime() + ",result=" + result + "," + testLog.toString(), tempPlace, param);
                }
                LogUtil.d(false, "identifyLocation, result:%{public}d", Integer.valueOf(result));
                if (result == 0) {
                    return -6;
                }
                return result;
            } catch (Exception e) {
                LogUtil.e(false, "identifyLocation failed by Exception", new Object[0]);
            }
        }
    }

    private int[] processIdentifyRes(ArrayList<IdentifyResult> leftIdentifyRes, ArrayList<IdentifyResult> identifyRes, int neighborNum) {
        int leftSize = identifyRes.size() <= neighborNum ? identifyRes.size() : neighborNum;
        for (int i = 0; i < leftSize; i++) {
            leftIdentifyRes.add(identifyRes.get(i));
        }
        int[] results = new int[leftIdentifyRes.size()];
        int loopSize = leftIdentifyRes.size();
        for (int i2 = 0; i2 < loopSize; i2++) {
            results[i2] = leftIdentifyRes.get(i2).getPreLabel();
        }
        return results;
    }

    private void processShareMacs(FingerInfo fingerInfo, ParameterInfo param, ModelInfo model, StringBuilder testLog, ArrayList<IdentifyResult> identifyRes) {
        int loopSize = model.getDataLen();
        int modelBsssidLen = model.getBssidList().length - 1;
        int[][] trainDatas = model.getDatas();
        HashMap<String, Integer> noneZeroBssidVals = getNoneZeroBssids(fingerInfo.getBssidDatas(), model);
        float xtestBssidLen = ((float) noneZeroBssidVals.size()) * param.getKnnShareMacRatio();
        if (LogUtil.getDebugFlag()) {
            testLog.append("isMain:");
            testLog.append(param.isMainAp());
            testLog.append(",");
            testLog.append(model.getModelName());
            testLog.append(",KnnMaxDist:");
            testLog.append(param.getKnnMaxDist());
            testLog.append(",NeighborNum=");
            testLog.append(param.getNeighborNum());
            testLog.append(",noneZeroBssidVals=");
            testLog.append(getHashMapString(noneZeroBssidVals));
            testLog.append(",");
        }
        for (int i = 0; i < loopSize; i++) {
            int shareMacsNum = getShareMacNum(trainDatas[i], noneZeroBssidVals, model);
            if (LogUtil.getDebugFlag()) {
                testLog.append("shareMacsNum=");
                testLog.append(shareMacsNum);
                testLog.append(";");
            }
            if (((float) shareMacsNum) < getXtrainBssidLen(trainDatas[i], param)) {
                if (LogUtil.getDebugFlag()) {
                    testLog.append("trainDatas[");
                    testLog.append(i);
                    testLog.append("]=");
                    testLog.append(getXtrainBssidLen(trainDatas[i], param));
                    testLog.append(",");
                }
            } else if (((float) shareMacsNum) >= xtestBssidLen) {
                int maxDist = computeDistShare(trainDatas[i], noneZeroBssidVals, model);
                if (LogUtil.getDebugFlag()) {
                    testLog.append("maxDist=");
                    testLog.append(maxDist);
                    testLog.append(";");
                }
                if (maxDist < param.getKnnMaxDist()) {
                    identifyRes.add(new IdentifyResult(maxDist, trainDatas[i][modelBsssidLen]));
                }
            } else if (LogUtil.getDebugFlag()) {
                testLog.append("xtestBssidLen=");
                testLog.append(xtestBssidLen);
                testLog.append(",");
            }
        }
    }

    private boolean judgeIdentifyResults(ArrayList<IdentifyResult> identifyRes, ParameterInfo param, StringBuilder testLog, String tempPlace, int neighborNum) {
        if (LogUtil.getDebugFlag()) {
            testLog.append(",identifyRes.size=");
            testLog.append(identifyRes.size());
            testLog.append(",");
        }
        if (identifyRes.size() != 0) {
            Collections.sort(identifyRes);
            if (!LogUtil.getDebugFlag()) {
                return false;
            }
            testLog.append(",neighborNum=");
            testLog.append(neighborNum);
            testLog.append(",");
            return false;
        } else if (!LogUtil.getDebugFlag()) {
            return true;
        } else {
            testLog.append("\"unknown;idRes.len=0\"");
            testLog.append(Constant.getLineSeparator());
            logIdentifyResult(TimeUtil.getTime() + ",result=unknown;idRes.len=0," + testLog.toString(), tempPlace, param);
            return true;
        }
    }

    private boolean judgeModelPlace(ModelInfo model, String tempPlace) {
        LogUtil.i(false, " identifyLocation begin:%{private}s", tempPlace);
        if (model.getPlace().equals(tempPlace)) {
            return false;
        }
        LogUtil.d(false, "identifyLocation failure,place :%{private}s, place of modelInfo is %{private}s", tempPlace, model.getPlace());
        return true;
    }
}
