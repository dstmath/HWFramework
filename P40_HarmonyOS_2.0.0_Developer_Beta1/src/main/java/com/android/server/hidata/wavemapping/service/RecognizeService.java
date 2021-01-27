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
    private static final String COMMA = ",";
    private static final int DEFAULT_CAPACITY = 16;
    private static final String DEFAULT_VALUE = "0";
    private static final String KEY_WIFI_MAC = "wifiMAC";
    private static final String KEY_WIFI_RSSI = "wifiRssi";
    private static final String SEMICOLON = ";";
    private static final String TAG = ("WMapping." + RecognizeService.class.getSimpleName());
    private String identifyLog = "";
    private String identifyLogPath = "";
    private Context mCtx = ContextManager.getInstance().getContext();
    private ModelService5 modelService;
    private String resultFileHead = ("time,preLable,place,model,fingerInfo " + Constant.getLineSeparator());
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
            LogUtil.i(false, " identifyLocationByAllAp loadModel begin.", new Object[0]);
            if (place == null) {
                LogUtil.d(false, " identifyLocationByAllAp loadModel failure. placeInfo == null", new Object[0]);
                return null;
            }
            if (place.getPlace() != null) {
                if (!place.getPlace().equals("")) {
                    LogUtil.i(false, " identifyLocationByAllAp begin. place:%{private}s", place.getPlace());
                    if (!this.modelService.loadCommModels(place)) {
                        return null;
                    }
                    FingerInfo fingerInfo = getFinger(wifiList, place);
                    if (fingerInfo == null) {
                        LogUtil.d(false, " identifyLocationByAllAp getFinger failure. fingerInfo == null", new Object[0]);
                        return null;
                    }
                    recognizeResult = new RecognizeResult();
                    recognizeResult.setRgResult(this.modelService.identifyLocation(place.getPlace(), fingerInfo, this.modelService.getParameterInfo()));
                    recognizeResult.setAllApModelName(place.getModelName());
                    this.toastInfo = "AllAp detect space:" + recognizeResult.getRgResult();
                    LogUtil.d(false, "%{public}s, model name:%{public}d", this.toastInfo, Integer.valueOf(recognizeResult.getAllApModelName()));
                    if (LogUtil.getDebugFlag()) {
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
                        sb.append(Constant.getLineSeparator());
                        this.identifyLog = sb.toString();
                        if (!FileUtils.addFileHead(this.identifyLogPath, this.resultFileHead)) {
                            LogUtil.d(false, " identifyLocationByAllAp addFileHead failure.", new Object[0]);
                        }
                        if (!FileUtils.writeFile(this.identifyLogPath, this.identifyLog)) {
                            LogUtil.d(false, " identifyLocationByAllAp log failure. identifyLogPath:%{public}s", this.identifyLogPath);
                            LogUtil.d(false, " identifyLocationByAllAp log failure. identifyLog:%{public}s", this.identifyLog);
                        }
                    }
                    return recognizeResult;
                }
            }
            LogUtil.d(false, " identifyLocationByAllAp loadModel failure. placeInfo.getPlace() == null || placeInfo.getPlace().equals(\"\")", new Object[0]);
            return null;
        } catch (Exception e) {
            LogUtil.e(false, "identifyLocationByAllAp failed by Exception", new Object[0]);
        }
    }

    public RecognizeResult identifyLocationByMainAp(RegularPlaceInfo place) {
        RecognizeResult recognizeResult = null;
        try {
            LogUtil.i(false, " identifyLocationByMainAp loadModel begin.", new Object[0]);
            if (place == null) {
                LogUtil.d(false, " identifyLocationByMainAp loadModel failure. placeInfo == null", new Object[0]);
                return null;
            }
            if (place.getPlace() != null) {
                if (!"".equals(place.getPlace())) {
                    if (!NetUtil.isWifiEnabled(this.mCtx)) {
                        LogUtil.i(false, " identifyLocationByMainAp, wifi not enabled", new Object[0]);
                        return null;
                    } else if (!this.modelService.loadMainApModel(place)) {
                        return null;
                    } else {
                        recognizeResult = new RecognizeResult();
                        FingerInfo mainApFingerInfo = getMainApFinger(place);
                        if (mainApFingerInfo == null) {
                            LogUtil.d(false, " identifyLocationByMainAp getMainApFinger failure. mainApFingerInfo == null", new Object[0]);
                            return null;
                        }
                        recognizeResult.setMainApRgResult(this.modelService.identifyLocation(place.getPlace(), mainApFingerInfo, this.modelService.getMainParameterInfo()));
                        recognizeResult.setMainApModelName(place.getModelName());
                        this.toastInfo = "MainAp detect space:" + recognizeResult.getMainApRgResult();
                        LogUtil.d(false, "%{public}s, model name:%{public}d", this.toastInfo, Integer.valueOf(recognizeResult.getMainApModelName()));
                        logToFile(place, recognizeResult, mainApFingerInfo);
                        return recognizeResult;
                    }
                }
            }
            LogUtil.d(false, " identifyLocationByMainAp loadModel failure. placeInfo.getPlace() == null || placeInfo.getPlace().equals(\"\"),modelName:%{public}d", Integer.valueOf(place.getModelName()));
            return null;
        } catch (Exception e) {
            LogUtil.e(false, "identifyLocationByMainAp failed by Exception", new Object[0]);
        }
    }

    private void logToFile(RegularPlaceInfo place, RecognizeResult recognizeResult, FingerInfo mainApFingerInfo) {
        if (LogUtil.getDebugFlag()) {
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
            sb.append(Constant.getLineSeparator());
            this.identifyLog = sb.toString();
            if (!FileUtils.addFileHead(this.identifyLogPath, this.resultFileHead)) {
                LogUtil.d(false, " identifyLocationByMainAp addFileHead failure.", new Object[0]);
            }
            if (!FileUtils.writeFile(this.identifyLogPath, this.identifyLog)) {
                LogUtil.d(false, " identifyLocationByMainAp log failure. identifyLogPath:%{public}s", this.identifyLogPath);
                LogUtil.d(false, " identifyLocationByMainAp log failure. identifyLog:%{public}s", this.identifyLog);
            }
        }
    }

    private FingerInfo getMainApFinger(RegularPlaceInfo placeInfo) {
        if (this.mCtx == null || placeInfo == null) {
            return null;
        }
        FingerInfo finger = new FingerInfo();
        try {
            Bundle wifiInfo = NetUtil.getWifiStateString(this.mCtx);
            if (wifiInfo == null) {
                LogUtil.e(false, "wifiInfo is null", new Object[0]);
                return null;
            }
            String wifiMac = wifiInfo.getString(KEY_WIFI_MAC, "UNKNOWN");
            String wifiRssi = wifiInfo.getString(KEY_WIFI_RSSI, "0");
            if (wifiMac != null) {
                if (!"UNKNOWN".equals(wifiMac)) {
                    if (wifiRssi != null) {
                        if (!"0".equals(wifiRssi)) {
                            HashMap<String, Integer> bssidDatas = new HashMap<>(16);
                            bssidDatas.put(wifiMac, Integer.valueOf(wifiRssi));
                            finger.setBssidDatas(bssidDatas);
                            finger.setBatch(placeInfo.getBatch());
                            finger.setServeMac(wifiMac);
                            finger.setTimestamp(TimeUtil.getTime());
                            return finger;
                        }
                    }
                    return null;
                }
            }
            LogUtil.d(false, "getMainApFinger wifiMac = null", new Object[0]);
            return null;
        } catch (Exception e) {
            LogUtil.e(false, "getMainApFinger failed by Exception", new Object[0]);
        }
    }

    private FingerInfo getFinger(List<ScanResult> wifiList, RegularPlaceInfo placeInfo) {
        if (wifiList == null || placeInfo == null || this.mCtx == null) {
            return null;
        }
        FingerInfo finger = new FingerInfo();
        HashMap<String, Integer> bssidDatas = new HashMap<>(16);
        try {
            for (ScanResult result : wifiList) {
                bssidDatas.put(result.BSSID.replace(",", "").replace(";", ""), Integer.valueOf(result.level));
            }
            finger.setBssidDatas(bssidDatas);
            finger.setBatch(placeInfo.getBatch());
            Bundle wifiInfo = NetUtil.getWifiStateString(this.mCtx);
            if (wifiInfo == null) {
                LogUtil.e(false, "wifiInfo is null", new Object[0]);
                return null;
            }
            String wifiMac = wifiInfo.getString(KEY_WIFI_MAC, "UNKNOWN");
            if (wifiMac != null && !"UNKNOWN".equals(wifiMac)) {
                finger.setServeMac(wifiMac);
            }
            finger.setTimestamp(TimeUtil.getTime());
            return finger;
        } catch (Exception e) {
            LogUtil.e(false, "getSendWiFiInfo failed by Exception", new Object[0]);
        }
    }
}
