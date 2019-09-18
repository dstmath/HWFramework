package com.android.server.hidata.wavemapping.modelservice;

import android.os.Handler;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.wavemapping.chr.ModelInvalidChrService;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.dao.IdentifyResultDAO;
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
    private byte AG_CHECK_RET_CODE_1 = -1;
    private byte AG_CHECK_RET_CODE_2 = -2;
    private byte AG_CHECK_RET_CODE_3 = -3;
    private byte AG_CHECK_RET_CODE_4 = -4;
    private byte AG_CHECK_RET_CODE_5 = -5;
    private byte AG_CHECK_RET_CODE_6 = -6;
    private byte AG_CHECK_SUCC_RET_CODE = 1;

    public RegularPlaceInfo agingAction(RegularPlaceInfo placeInfo, String place, ParameterInfo param, String[] modelBssids, Handler mMachineHandler) {
        RegularPlaceInfo placeInfo2;
        if (place == null || place.equals("") || param == null || placeInfo == null || placeInfo.getNoOcurBssids() == null) {
            return placeInfo;
        }
        byte checkAgNeedRet = checkAgingNeed(placeInfo, place, param);
        new ModelInvalidChrService().commitModelInvalidChrInfo(placeInfo, place, param, checkAgNeedRet);
        if (checkAgNeedRet < 0) {
            placeInfo2 = updateModel(placeInfo, place, param);
            if (!FileUtils.delFile(getModelFilePath(placeInfo2, param))) {
                LogUtil.d(" updateModel  ,FileUtils.delFile(modelFilePath) failure,modelFilePath:" + modelFilePath);
            }
            mMachineHandler.sendEmptyMessage(100);
        } else {
            placeInfo2 = cleanFingersByBssid(placeInfo, place, modelBssids, param);
        }
        FileUtils.writeFile(Constant.getLogPath() + Constant.LOG_FILE, TimeUtil.getTime() + ",place:" + place + ",isMainAp:" + String.valueOf(param.isMainAp()) + ",checkAgNeedRet :" + checkAgNeedRet + ",placeInfo:" + placeInfo2.toString() + Constant.getLineSeperate());
        return placeInfo2;
    }

    public byte checkAgingNeed(RegularPlaceInfo placeInfo, String place, ParameterInfo param) {
        float occUpd;
        int ukwnCount;
        float maxUkwnRatioUpd;
        String noOcurBssids;
        String str = place;
        try {
            LogUtil.d("AgingService,checkAgingNeed begin." + placeInfo.toString() + ",place:" + str);
            String noOcurBssids2 = placeInfo.getNoOcurBssids().replace("[", "").replace(" ", "").replace("]", "").trim();
            if (noOcurBssids2 != null && noOcurBssids2.split(",").length > 0) {
                return this.AG_CHECK_RET_CODE_1;
            }
            IdentifyResultDAO identifyResultDAO = new IdentifyResultDAO();
            try {
                String modelFilePath = getModelFilePath(placeInfo, param);
                String fileContent = FileUtils.getFileContent(modelFilePath);
                if (((long) fileContent.length()) > Constant.MAX_FILE_SIZE) {
                    LogUtil.d("checkAgingNeed ,file content is too bigger than max_file_size.");
                    return this.AG_CHECK_RET_CODE_2;
                }
                String[] headers = fileContent.split(Constant.getLineSeperate())[0].split(",");
                HashSet hashSet = new HashSet();
                int bssidsLen = headers.length - param.getBssidStart();
                if (bssidsLen < 0) {
                    return this.AG_CHECK_RET_CODE_3;
                }
                int bssidStart = param.getBssidStart();
                if (bssidStart > headers.length) {
                    return this.AG_CHECK_RET_CODE_4;
                }
                int i = bssidStart;
                while (true) {
                    int i2 = i;
                    if (i2 >= headers.length) {
                        break;
                    }
                    try {
                        hashSet.add(headers[i2]);
                        noOcurBssids = noOcurBssids2;
                    } catch (Exception e) {
                        StringBuilder sb = new StringBuilder();
                        noOcurBssids = noOcurBssids2;
                        sb.append("checkAgingNeed:");
                        sb.append(e);
                        LogUtil.e(sb.toString());
                    }
                    i = i2 + 1;
                    noOcurBssids2 = noOcurBssids;
                }
                List<IdentifyResult> identifyResultList = identifyResultDAO.findBySsid(str, param.isMainAp());
                int size = identifyResultList.size();
                if (size <= 0) {
                    return this.AG_CHECK_SUCC_RET_CODE;
                }
                HashMap<String, AtomicInteger> stats = new HashMap<>();
                List<String> testHomeAPLst = new ArrayList<>();
                float maxUkwnRatioUpd2 = ((float) size) * param.getMinUkwnRatioUpd();
                int ukwnCount2 = 0;
                int i3 = 0;
                while (true) {
                    IdentifyResultDAO identifyResultDAO2 = identifyResultDAO;
                    int i4 = i3;
                    if (i4 < size) {
                        List<IdentifyResult> identifyResultList2 = identifyResultList;
                        IdentifyResult identifyResult = identifyResultList.get(i4);
                        if (identifyResult.getPreLabel() < 0) {
                            ukwnCount2++;
                        }
                        int ukwnCount3 = ukwnCount2;
                        String modelFilePath2 = modelFilePath;
                        if (((float) ukwnCount3) > maxUkwnRatioUpd2) {
                            return this.AG_CHECK_RET_CODE_5;
                        }
                        if (!stats.containsKey(identifyResult.getServeMac())) {
                            maxUkwnRatioUpd = maxUkwnRatioUpd2;
                            if (!identifyResult.getServeMac().equals("UNKNOWN")) {
                                ukwnCount = ukwnCount3;
                                stats.put(identifyResult.getServeMac(), new AtomicInteger(1));
                                i3 = i4 + 1;
                                identifyResultDAO = identifyResultDAO2;
                                identifyResultList = identifyResultList2;
                                modelFilePath = modelFilePath2;
                                maxUkwnRatioUpd2 = maxUkwnRatioUpd;
                                ukwnCount2 = ukwnCount;
                                RegularPlaceInfo regularPlaceInfo = placeInfo;
                            } else {
                                ukwnCount = ukwnCount3;
                            }
                        } else {
                            maxUkwnRatioUpd = maxUkwnRatioUpd2;
                            ukwnCount = ukwnCount3;
                        }
                        stats.get(identifyResult.getServeMac()).incrementAndGet();
                        i3 = i4 + 1;
                        identifyResultDAO = identifyResultDAO2;
                        identifyResultList = identifyResultList2;
                        modelFilePath = modelFilePath2;
                        maxUkwnRatioUpd2 = maxUkwnRatioUpd;
                        ukwnCount2 = ukwnCount;
                        RegularPlaceInfo regularPlaceInfo2 = placeInfo;
                    } else {
                        float f = maxUkwnRatioUpd2;
                        String str2 = modelFilePath;
                        if (param.isMainAp()) {
                            return this.AG_CHECK_SUCC_RET_CODE;
                        }
                        float occUpd2 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                        for (Map.Entry<String, AtomicInteger> entry : stats.entrySet()) {
                            occUpd2 = ((float) entry.getValue().intValue()) / ((float) size);
                            if (occUpd2 > param.getMinMainAPOccUpd()) {
                                testHomeAPLst.add(entry.getKey());
                            }
                        }
                        float abnMacRatioAllow = bssidsLen >= param.getMinTrainBssiLstLenUpd() ? ((float) bssidsLen) * param.getAbnMacRatioAllowUpd() : GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                        int abnormalMac = 0;
                        for (String mac : testHomeAPLst) {
                            if (!hashSet.contains(mac)) {
                                abnormalMac++;
                                occUpd = occUpd2;
                                if (((float) abnormalMac) >= abnMacRatioAllow) {
                                    return this.AG_CHECK_RET_CODE_6;
                                }
                            } else {
                                occUpd = occUpd2;
                            }
                            occUpd2 = occUpd;
                        }
                    }
                }
                return this.AG_CHECK_SUCC_RET_CODE;
            } catch (Exception e2) {
                e = e2;
                LogUtil.e("checkAgingNeed:" + e);
                return this.AG_CHECK_SUCC_RET_CODE;
            }
        } catch (Exception e3) {
            e = e3;
            ParameterInfo parameterInfo = param;
            LogUtil.e("checkAgingNeed:" + e);
            return this.AG_CHECK_SUCC_RET_CODE;
        }
    }

    private RegularPlaceInfo cleanFingersByBssid(RegularPlaceInfo placeInfo, String place, String[] modelBssids, ParameterInfo param) {
        if (place == null || place.equals("") || placeInfo == null || modelBssids == null || modelBssids.length == 0) {
            return placeInfo;
        }
        LogUtil.d("AgingService,cleanFingersByBssid begin." + placeInfo.toString() + ",place:" + place);
        try {
            if (!new IdentifyResultDAO().remove(place, param.isMainAp())) {
                LogUtil.d("updateModel identifyResultDAO.deleteAll failure.");
            }
            placeInfo.setIdentifyNum(0);
            StringBuffer sb = new StringBuffer();
            for (String bssid : modelBssids) {
                sb.append(bssid);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            placeInfo.setNoOcurBssids(sb.toString().replace("[", "").replace("]", "").replace(" ", "").replace("prelabel", "").trim());
        } catch (Exception e) {
            LogUtil.e("cleanFingersByBssid:" + e);
        }
        return placeInfo;
    }
}
