package com.android.server.hidata.wavemapping.service;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ContextManager;
import com.android.server.hidata.wavemapping.entity.FingerInfo;
import com.android.server.hidata.wavemapping.entity.RecognizeResult;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.modelservice.ModelService5;
import com.android.server.hidata.wavemapping.util.FileUtils;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.NetUtil;
import com.android.server.hidata.wavemapping.util.ShowToast;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.HashMap;
import java.util.List;

public class RecognizeService {
    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss.SSS";
    private static final String TAG = ("WMapping." + RecognizeService.class.getSimpleName());
    private String identifyLog = "";
    private String identifyLogPath = "";
    private Context mCtx = ContextManager.getInstance().getContext();
    private ModelService5 modelService;
    private String resultFileHead = "time,preLable,place,model,fingerInfo \n";
    private TimeUtil timeUtil;
    private String toastInfo;

    public RecognizeService(ModelService5 modelService2) {
        this.modelService = modelService2;
        this.timeUtil = new TimeUtil();
    }

    public RecognizeResult identifyLocation(RegularPlaceInfo allPlaceInfo, RegularPlaceInfo mainApPlaceInfo, List<ScanResult> wifiList) {
        RecognizeResult recognizeResult = new RecognizeResult();
        RecognizeResult recognizeResultByAllAp = identifyLocationByAllAp(allPlaceInfo, wifiList);
        RecognizeResult recognizeResultByMainAp = identifyLocationByMainAp(mainApPlaceInfo);
        if (recognizeResultByMainAp != null) {
            recognizeResult.setMainApRgResult(recognizeResultByMainAp.getMainApRgResult());
            recognizeResult.setMainApModelName(recognizeResultByMainAp.getMainApModelName());
        }
        if (recognizeResultByAllAp != null) {
            recognizeResult.setRgResult(recognizeResultByAllAp.getRgResult());
            recognizeResult.setAllApModelName(recognizeResultByAllAp.getAllApModelName());
        }
        this.toastInfo = recognizeResult.printResults();
        ShowToast.showToast(this.toastInfo);
        return recognizeResult;
    }

    public RecognizeResult identifyLocationByAllAp(RegularPlaceInfo place, List<ScanResult> wifiList) {
        RecognizeResult recognizeResult = null;
        try {
            LogUtil.i(" identifyLocationByAllAp loadModel begin.");
            if (place == null) {
                LogUtil.d(" identifyLocationByAllAp loadModel failure. placeInfo == null");
                return null;
            }
            if (place.getPlace() != null) {
                if (!place.getPlace().equals("")) {
                    LogUtil.i(" identifyLocationByAllAp begin. place:" + place.getPlace());
                    if (!this.modelService.loadCommModels(place)) {
                        return null;
                    }
                    FingerInfo fingerInfo = getFinger(wifiList, place);
                    if (fingerInfo == null) {
                        LogUtil.d(" identifyLocationByAllAp getFinger failure. fingerInfo == null");
                        return null;
                    }
                    recognizeResult = new RecognizeResult();
                    recognizeResult.setRgResult(this.modelService.indentifyLocation(place.getPlace(), fingerInfo, this.modelService.getParameterInfo()));
                    recognizeResult.setAllApModelName(place.getModelName());
                    this.toastInfo = "AllAp detect space:" + recognizeResult.getRgResult();
                    LogUtil.d(this.toastInfo + ", model name:" + recognizeResult.getAllApModelName());
                    if (LogUtil.getDebug_flag()) {
                        this.identifyLogPath = Constant.getLogPath() + place.getPlace() + Constant.LOG_FILE_EXTENSION;
                        StringBuilder sb = new StringBuilder();
                        TimeUtil timeUtil2 = this.timeUtil;
                        sb.append(TimeUtil.getTime());
                        sb.append(",");
                        sb.append(recognizeResult.getRgResult());
                        sb.append(",");
                        sb.append(place.getPlace());
                        sb.append(",");
                        sb.append(place.getModelName());
                        sb.append(",");
                        sb.append(fingerInfo.toReString());
                        sb.append(Constant.getLineSeperate());
                        this.identifyLog = sb.toString();
                        if (!FileUtils.addFileHead(this.identifyLogPath, this.resultFileHead)) {
                            LogUtil.d(" identifyLocationByAllAp addFileHead failure.");
                        }
                        if (!FileUtils.writeFile(this.identifyLogPath, this.identifyLog)) {
                            LogUtil.d(" identifyLocationByAllAp log failure. identifyLogPath:" + this.identifyLogPath);
                            LogUtil.d(" identifyLocationByAllAp log failure. identifyLog:" + this.identifyLog);
                        }
                    }
                    return recognizeResult;
                }
            }
            LogUtil.d(" identifyLocationByAllAp loadModel failure. placeInfo.getPlace() == null || placeInfo.getPlace().equals(\"\")");
            return null;
        } catch (Exception e) {
            LogUtil.e("identifyLocationByAllAp:" + e.getMessage());
        }
    }

    public RecognizeResult identifyLocationByMainAp(RegularPlaceInfo place) {
        RecognizeResult recognizeResult = null;
        try {
            LogUtil.i(" identifyLocationByMainAp loadModel begin.");
            if (place == null) {
                LogUtil.d(" identifyLocationByMainAp loadModel failure. placeInfo == null");
                return null;
            }
            if (place.getPlace() != null) {
                if (!place.getPlace().equals("")) {
                    if (!NetUtil.isWifiEnabled(this.mCtx)) {
                        LogUtil.i(" identifyLocationByMainAp, wifi not enabled");
                        return null;
                    } else if (!this.modelService.loadMainApModel(place)) {
                        return null;
                    } else {
                        recognizeResult = new RecognizeResult();
                        FingerInfo mainApFingerInfo = getMainApFinger(place);
                        if (mainApFingerInfo == null) {
                            LogUtil.d(" identifyLocationByMainAp getMainApFinger failure. mainApFingerInfo == null");
                            return null;
                        }
                        recognizeResult.setMainApRgResult(this.modelService.indentifyLocation(place.getPlace(), mainApFingerInfo, this.modelService.getMainParameterInfo()));
                        recognizeResult.setMainApModelName(place.getModelName());
                        this.toastInfo = "MainAp detect space:" + recognizeResult.getMainApRgResult();
                        LogUtil.d(this.toastInfo + ", model name:" + recognizeResult.getMainApModelName());
                        if (LogUtil.getDebug_flag()) {
                            this.identifyLogPath = Constant.getLogPath() + place.getPlace() + Constant.MAINAP_LOG_FILE_EXTENSION;
                            StringBuilder sb = new StringBuilder();
                            TimeUtil timeUtil2 = this.timeUtil;
                            sb.append(TimeUtil.getTime());
                            sb.append(",");
                            sb.append(recognizeResult.getMainApRgResult());
                            sb.append(",");
                            sb.append(place.getPlace());
                            sb.append(",");
                            sb.append(place.getModelName());
                            sb.append(",");
                            sb.append(mainApFingerInfo.toReString());
                            sb.append(Constant.getLineSeperate());
                            this.identifyLog = sb.toString();
                            if (!FileUtils.addFileHead(this.identifyLogPath, this.resultFileHead)) {
                                LogUtil.d(" identifyLocationByMainAp addFileHead failure.");
                            }
                            if (!FileUtils.writeFile(this.identifyLogPath, this.identifyLog)) {
                                LogUtil.d(" identifyLocationByMainAp log failure. identifyLogPath:" + this.identifyLogPath);
                                LogUtil.d(" identifyLocationByMainAp log failure. identifyLog:" + this.identifyLog);
                            }
                        }
                        return recognizeResult;
                    }
                }
            }
            LogUtil.d(" identifyLocationByMainAp loadModel failure. placeInfo.getPlace() == null || placeInfo.getPlace().equals(\"\"),modelName:" + place.getModelName());
            return null;
        } catch (Exception e) {
            LogUtil.e("identifyLocationByMainAp:" + e.getMessage());
        }
    }

    private FingerInfo getMainApFinger(RegularPlaceInfo placeInfo) {
        if (this.mCtx == null || placeInfo == null) {
            return null;
        }
        FingerInfo finger = new FingerInfo();
        try {
            Bundle wifiInfo = NetUtil.getWifiStateString(this.mCtx);
            String wifiMAC = wifiInfo.getString("wifiMAC", "UNKNOWN");
            String wifiRssi = wifiInfo.getString("wifiRssi", "0");
            if (wifiMAC != null) {
                if (!wifiMAC.equals("UNKNOWN")) {
                    if (wifiRssi != null) {
                        if (!wifiRssi.equals("0")) {
                            HashMap<String, Integer> bissiddatas = new HashMap<>();
                            bissiddatas.put(wifiMAC, Integer.valueOf(wifiRssi));
                            finger.setBissiddatas(bissiddatas);
                            finger.setBatch(placeInfo.getBatch());
                            finger.setServeMac(wifiMAC);
                            finger.setTimestamp(TimeUtil.getTime());
                            return finger;
                        }
                    }
                    return null;
                }
            }
            LogUtil.d("getMainApFinger wifiMAC = null");
            return null;
        } catch (Exception e) {
            LogUtil.e("getMainApFinger:" + e.getMessage());
        }
    }

    private FingerInfo getFinger(List<ScanResult> wifiList, RegularPlaceInfo placeInfo) {
        if (wifiList == null || placeInfo == null || this.mCtx == null) {
            return null;
        }
        FingerInfo finger = new FingerInfo();
        HashMap<String, Integer> bissiddatas = new HashMap<>();
        try {
            int size = wifiList.size();
            for (int i = 0; i < size; i++) {
                bissiddatas.put(wifiList.get(i).BSSID.replace(",", "").replace(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER, ""), Integer.valueOf(wifiList.get(i).level));
            }
            finger.setBissiddatas(bissiddatas);
            finger.setBatch(placeInfo.getBatch());
            String wifiMac = NetUtil.getWifiStateString(this.mCtx).getString("wifiMAC", "UNKNOWN");
            if (wifiMac != null && !wifiMac.equals("UNKNOWN")) {
                finger.setServeMac(wifiMac);
            }
            finger.setTimestamp(TimeUtil.getTime());
        } catch (Exception e) {
            LogUtil.e("getSendWiFiInfo:" + e.getMessage());
        }
        return finger;
    }
}
