package com.android.server.hidata.wavemapping.modelservice;

import android.os.Handler;
import com.android.server.hidata.wavemapping.chr.ModelInvalidChrService;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.dao.IdentifyResultDao;
import com.android.server.hidata.wavemapping.entity.IdentifyResult;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.util.FileUtils;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AgingService extends ModelBaseService {
    private static final byte AG_CHECK_RET_CODE_1 = -1;
    private static final byte AG_CHECK_RET_CODE_2 = -2;
    private static final byte AG_CHECK_RET_CODE_3 = -3;
    private static final byte AG_CHECK_RET_CODE_4 = -4;
    private static final byte AG_CHECK_RET_CODE_5 = -5;
    private static final byte AG_CHECK_RET_CODE_6 = -6;
    private static final byte AG_CHECK_SUCC_RET_CODE = 1;
    private static final String COMMA = ",";
    private static final int DEFAULT_CAPACITY = 16;
    private static final String KEY_PRE_LABEL = "prelabel";
    private static final String LEFT_BRACKET = "[";
    private static final int LIST_DEFAULT_CAPACITY = 10;
    private static final String RIGHT_BRACKET = "]";
    private static final String SPACE = " ";

    public RegularPlaceInfo agingAction(RegularPlaceInfo placeInfo, String place, ParameterInfo param, String[] modelBssids, Handler machineHandler) {
        RegularPlaceInfo newPlaceInfo;
        if (place == null || "".equals(place) || param == null || placeInfo == null || placeInfo.getNoOcurBssids() == null) {
            return placeInfo;
        }
        byte checkAgNeedRet = checkAgingNeed(placeInfo, place, param);
        new ModelInvalidChrService().commitModelInvalidChrInfo(placeInfo, place, param, checkAgNeedRet);
        if (checkAgNeedRet < 0) {
            newPlaceInfo = updateModel(placeInfo, place, param);
            if (!FileUtils.delFile(getModelFilePath(newPlaceInfo, param))) {
                LogUtil.e(false, "updateModel,FileUtils.delFile(modelFilePath) failure", new Object[0]);
            }
            machineHandler.sendEmptyMessage(100);
        } else {
            newPlaceInfo = cleanFingersByBssid(placeInfo, place, modelBssids, param);
        }
        FileUtils.writeFile(Constant.getLogPath() + Constant.LOG_FILE, TimeUtil.getTime() + ",place:" + place + ",isMainAp:" + String.valueOf(param.isMainAp()) + ",checkAgNeedRet :" + ((int) checkAgNeedRet) + ",placeInfo:" + newPlaceInfo.toString() + Constant.getLineSeparator());
        return newPlaceInfo;
    }

    public byte checkAgingNeed(RegularPlaceInfo placeInfo, String place, ParameterInfo param) {
        try {
            LogUtil.d(false, "AgingService,checkAgingNeed begin. %{private}s,place:%{private}s", placeInfo.toString(), place);
            String noOcurBssids = placeInfo.getNoOcurBssids().replace(LEFT_BRACKET, "").replace(SPACE, "").replace(RIGHT_BRACKET, "").trim();
            if (noOcurBssids != null && noOcurBssids.split(",").length > 0) {
                return -1;
            }
            IdentifyResultDao identifyResultDao = new IdentifyResultDao();
            try {
                String fileContent = FileUtils.getFileContent(getModelFilePath(placeInfo, param));
                if (((long) fileContent.length()) > Constant.MAX_FILE_SIZE) {
                    LogUtil.d(false, "checkAgingNeed ,file content is too bigger than max_file_size.", new Object[0]);
                    return AG_CHECK_RET_CODE_2;
                }
                String[] headers = fileContent.split(Constant.getLineSeparator())[0].split(",");
                HashSet<String> setBssids = new HashSet<>(16);
                int bssidsLen = headers.length - param.getBssidStart();
                if (bssidsLen < 0) {
                    return AG_CHECK_RET_CODE_3;
                }
                int bssidStart = param.getBssidStart();
                if (bssidStart > headers.length) {
                    return AG_CHECK_RET_CODE_4;
                }
                for (int i = bssidStart; i < headers.length; i++) {
                    setBssids.add(headers[i]);
                }
                List<IdentifyResult> identifyResultList = identifyResultDao.findBySsid(place, param.isMainAp());
                int size = identifyResultList.size();
                if (size <= 0) {
                    return 1;
                }
                HashMap<String, AtomicInteger> stats = new HashMap<>(16);
                if (judgeIdentifyResults(identifyResultList, stats, param, size)) {
                    return AG_CHECK_RET_CODE_5;
                }
                if (!param.isMainAp() && judgeBssids(stats, setBssids, param, size, bssidsLen)) {
                    return AG_CHECK_RET_CODE_6;
                }
                return 1;
            } catch (Exception e) {
                LogUtil.e(false, "checkAgingNeed failed by Exception", new Object[0]);
                return 1;
            }
        } catch (Exception e2) {
            LogUtil.e(false, "checkAgingNeed failed by Exception", new Object[0]);
            return 1;
        }
    }

    private boolean judgeIdentifyResults(List<IdentifyResult> identifyResultList, HashMap<String, AtomicInteger> stats, ParameterInfo param, int size) {
        float maxUkwnRatioUpd = ((float) size) * param.getMinUnknownRatioUpd();
        int ukwnCount = 0;
        for (IdentifyResult identifyResult : identifyResultList) {
            if (identifyResult.getPreLabel() < 0) {
                ukwnCount++;
            }
            if (((float) ukwnCount) > maxUkwnRatioUpd) {
                return true;
            }
            if (stats.containsKey(identifyResult.getServeMac()) || identifyResult.getServeMac().equals("UNKNOWN")) {
                stats.get(identifyResult.getServeMac()).incrementAndGet();
            } else {
                stats.put(identifyResult.getServeMac(), new AtomicInteger(1));
            }
        }
        return false;
    }

    private boolean judgeBssids(HashMap<String, AtomicInteger> stats, HashSet<String> setBssids, ParameterInfo param, int size, int bssidsLen) {
        List<String> testHomeApList = new ArrayList<>(10);
        for (Map.Entry<String, AtomicInteger> entry : stats.entrySet()) {
            if (((float) entry.getValue().intValue()) / ((float) size) > param.getMinMainApOccUpd()) {
                testHomeApList.add(entry.getKey());
            }
        }
        float abnMacRatioAllow = bssidsLen >= param.getMinTrainBssiLstLenUpd() ? ((float) bssidsLen) * param.getAbnMacRatioAllowUpd() : 0.0f;
        int abnormalMac = 0;
        for (String mac : testHomeApList) {
            if (!setBssids.contains(mac)) {
                abnormalMac++;
                if (((float) abnormalMac) >= abnMacRatioAllow) {
                    return true;
                }
            }
        }
        return false;
    }

    private RegularPlaceInfo cleanFingersByBssid(RegularPlaceInfo placeInfo, String place, String[] modelBssids, ParameterInfo param) {
        if (place == null || "".equals(place) || placeInfo == null || modelBssids == null || modelBssids.length == 0) {
            return placeInfo;
        }
        LogUtil.d(false, "AgingService,cleanFingersByBssid begin.%{private}s,place:%{private}s", placeInfo.toString(), place);
        try {
            if (!new IdentifyResultDao().remove(place, param.isMainAp())) {
                LogUtil.d(false, "updateModel identifyResultDAO.deleteAll failure.", new Object[0]);
            }
            placeInfo.setIdentifyNum(0);
            StringBuffer sb = new StringBuffer(16);
            for (String bssid : modelBssids) {
                sb.append(bssid);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            placeInfo.setNoOcurBssids(sb.toString().replace(LEFT_BRACKET, "").replace(RIGHT_BRACKET, "").replace(SPACE, "").replace(KEY_PRE_LABEL, "").trim());
        } catch (Exception e) {
            LogUtil.e(false, "cleanFingersByBssid failed by Exception", new Object[0]);
        }
        return placeInfo;
    }
}
