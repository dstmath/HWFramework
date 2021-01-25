package com.android.server.hidata.wavemapping.statehandler;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dao.EnterpriseApDao;
import com.android.server.hidata.wavemapping.dao.MobileApDao;
import com.android.server.hidata.wavemapping.dao.RegularPlaceDao;
import com.android.server.hidata.wavemapping.dataprovider.BehaviorReceiver;
import com.android.server.hidata.wavemapping.entity.ApInfo;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.entity.UiInfo;
import com.android.server.hidata.wavemapping.service.UiService;
import com.android.server.hidata.wavemapping.util.FileUtils;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.NetUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CollectFingersHandler {
    private static final String BACK_SLASH = "\"";
    private static final String COMMA = ",";
    private static final int DEFAULT_CAPACITY = 16;
    private static final String DEFAULT_VALUE = "0";
    private static final String KEY_ENABLED = "ENABLED";
    private static final String KEY_WIFI_AP = "wifiAp";
    private static final String KEY_WIFI_CHANNEL = "wifiCh";
    private static final String KEY_WIFI_LIST = "wifiLS";
    private static final String KEY_WIFI_MAC = "wifiMAC";
    private static final String KEY_WIFI_RSSI = "wifiRssi";
    private static final String KEY_WIFI_STATE = "wifiState";
    private static final int PRINT_CNT = 10;
    private static final String SEMICOLON = ";";
    private static final String TAG = ("WMapping." + CollectFingersHandler.class.getSimpleName());
    private String batch = "";
    private Context context;
    private String currLoc = Constant.NAME_FREQLOCATION_OTHER;
    private EnterpriseApDao enterpriseApDao;
    private Handler handler = new Handler();
    private int lastBatch;
    private int logCounter = 0;
    private Handler mMachineHandler;
    private String mainApCollectFileName = "";
    private int mainApLastBatch;
    private RegularPlaceInfo mainApPlaceInfo;
    private MobileApDao mobileApDao;
    private ParameterInfo param;
    private ParameterInfo paramMainAp;
    private Runnable periodicToFileHandler = new Runnable() {
        /* class com.android.server.hidata.wavemapping.statehandler.CollectFingersHandler.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            CollectFingersHandler.this.sendMainApInfoToFile();
            CollectFingersHandler.this.handler.postDelayed(this, (long) CollectFingersHandler.this.paramMainAp.getActiveSample());
            CollectFingersHandler.access$308(CollectFingersHandler.this);
            if (CollectFingersHandler.this.logCounter > 10) {
                LogUtil.d(false, "periodic MainAp ToFileHandler write to file", new Object[0]);
                CollectFingersHandler.this.logCounter = 0;
            }
        }
    };
    private RegularPlaceDao regularPlaceDao;
    private String resultWifiScan = "";
    private String strCollectFileName = "temp_file_wp_data.csv";
    private TimeUtil timeUtil = new TimeUtil();
    private long timeWiFiScan = 0;
    private UiInfo uiInfo;
    private UiService uiService;

    static /* synthetic */ int access$308(CollectFingersHandler x0) {
        int i = x0.logCounter;
        x0.logCounter = i + 1;
        return i;
    }

    public CollectFingersHandler(Context context2, String currlocation, Handler handler2) {
        LogUtil.i(false, " ,new CollectFingersHandler ", new Object[0]);
        try {
            this.context = context2;
            this.currLoc = currlocation;
            this.strCollectFileName = Constant.getRawDataPath() + this.currLoc + Constant.RAW_FILE_EXTENSION;
            this.mainApCollectFileName = Constant.getRawDataPath() + this.currLoc + Constant.MAINAP_RAW_FILE_EXTENSION;
            this.param = ParamManager.getInstance().getParameterInfo();
            this.paramMainAp = ParamManager.getInstance().getMainApParameterInfo();
            this.regularPlaceDao = new RegularPlaceDao();
            this.uiService = new UiService();
            UiService uiService2 = this.uiService;
            this.uiInfo = UiService.getUiInfo();
            this.mMachineHandler = handler2;
            this.mobileApDao = new MobileApDao();
            this.enterpriseApDao = new EnterpriseApDao();
        } catch (Exception e) {
            LogUtil.e(false, "CollectFingersHandler failed by Exception", new Object[0]);
        }
    }

    public long getTimeWiFiScan() {
        return this.timeWiFiScan;
    }

    public void setTimeWiFiScan(long timeWiFiScan2) {
        this.timeWiFiScan = timeWiFiScan2;
    }

    public void startCollect() {
        try {
            LogUtil.d(false, " startCollect ", new Object[0]);
            this.handler.postDelayed(this.periodicToFileHandler, (long) this.paramMainAp.getActiveSample());
        } catch (Exception e) {
            LogUtil.e(false, "startCollect failed by Exception", new Object[0]);
        }
    }

    public boolean isStopCollect() {
        try {
            LogUtil.d(false, " isStopCollect ", new Object[0]);
            this.handler.removeCallbacks(this.periodicToFileHandler);
            return true;
        } catch (Exception e) {
            LogUtil.e(false, "isStopCollect failed by Exception", new Object[0]);
            return false;
        }
    }

    public boolean isProcessWifiData(List<ScanResult> wifiList, RegularPlaceInfo placeInfo) {
        LogUtil.i(false, "isProcessWifiData ....", new Object[0]);
        if (placeInfo == null) {
            LogUtil.i(false, "isProcessWifiData,failure, placeInfo == null", new Object[0]);
            return false;
        }
        try {
            LogUtil.i(false, "collectFinger: current batch=%{public}d, last Batch=%{public}d, placeInfo=%{public}s", Integer.valueOf(BehaviorReceiver.getBatch()), Integer.valueOf(this.lastBatch), placeInfo.toString());
            placeInfo.setFingerNum(placeInfo.getFingerNum() + 1);
            if (this.lastBatch != BehaviorReceiver.getBatch()) {
                placeInfo.setBatch(placeInfo.getBatch() + 1);
            }
            if (placeInfo.getDisNum() > 0) {
                placeInfo.setTestDataNum(placeInfo.getTestDataNum() + 1);
            }
            this.lastBatch = BehaviorReceiver.getBatch();
            this.timeWiFiScan = System.currentTimeMillis();
            this.batch = BehaviorReceiver.getBatch() + "";
            boolean isArOn = BehaviorReceiver.getArState();
            if (isArOn) {
                this.resultWifiScan = getSendWiFiInfo(wifiList);
                if (!isSendAllInfoToFile(this.batch, this.resultWifiScan)) {
                    LogUtil.d(false, "isSendAllInfoToFile failure.", new Object[0]);
                    return false;
                } else if (!this.regularPlaceDao.update(placeInfo)) {
                    LogUtil.d(false, "isProcessWifiData,update placeInfo failure.%{public}s", placeInfo.toString());
                }
            }
            LogUtil.i(false, "isProcessWifiData placeInfo :%{public}s, station:%{public}s", placeInfo.toString(), String.valueOf(isArOn));
            if (this.uiInfo != null) {
                this.uiInfo.setFingerBatchNumber(placeInfo.getBatch());
                this.uiInfo.setFgFingersNumber(placeInfo.getFingerNum());
                this.uiInfo.setStage(placeInfo.getState());
                this.uiInfo.setSsid(placeInfo.getPlace());
                this.uiInfo.setToast("isProcessWifiData complete.");
            }
            UiService uiService2 = this.uiService;
            UiService.sendMsgToUi();
        } catch (Exception e) {
            LogUtil.e(false, "isProcessWifiData failed by Exception", new Object[0]);
        }
        return true;
    }

    public boolean isCheckDataSatisfiedToTraining(RegularPlaceInfo placeInfo) {
        LogUtil.i(false, " checkDataSatisfiedToTraining01, placeInfo:%{private}s", placeInfo.toString());
        ParameterInfo parameterInfo = this.param;
        if (parameterInfo != null) {
            return isFitTrainModel(placeInfo, parameterInfo);
        }
        LogUtil.w(false, " param == null", new Object[0]);
        return false;
    }

    private boolean isFitTrainModel(RegularPlaceInfo placeInfo, ParameterInfo param2) {
        LogUtil.d(false, " Batch number reach criteria:%{public}d, interval=%{public}d", Integer.valueOf(param2.getFgBatchNum()), Integer.valueOf((int) Constant.LIMIT_TRAINING_INTERVAL));
        if (placeInfo.getBatch() < param2.getFgBatchNum()) {
            LogUtil.d(false, " acc batch numbers are NOT enough:%{public}d", Integer.valueOf(placeInfo.getBatch()));
            return false;
        } else if (placeInfo.getDisNum() == 0 && placeInfo.getFingerNum() > param2.getTrainDatasSize() + param2.getTestDataSize()) {
            LogUtil.d(false, "isFitTrainModel,return true2", new Object[0]);
            return true;
        } else if (placeInfo.getDisNum() <= 0 || placeInfo.getTestDataNum() <= param2.getTestDataCnt()) {
            return false;
        } else {
            LogUtil.d(false, "isFitTrainModel,return true3", new Object[0]);
            return true;
        }
    }

    private String getSendWiFiInfo(List<ScanResult> wifiList) {
        if (wifiList == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(16);
        HashMap<String, AtomicInteger> hpSsidCnt = new HashMap<>(16);
        try {
            for (ScanResult result : wifiList) {
                String tempSsid = result.SSID.replace(",", "").replace(";", "");
                if (!"".equals(tempSsid)) {
                    String tempBssid = result.BSSID.replace(",", "").replace(";", "");
                    if (!"".equals(tempBssid)) {
                        if (this.mobileApDao.findBySsidForUpdateTime(tempSsid, tempBssid) == null) {
                            if (this.enterpriseApDao.findBySsidForUpdateTime(tempSsid) == null) {
                                if (hpSsidCnt.containsKey(tempSsid)) {
                                    hpSsidCnt.get(tempSsid).incrementAndGet();
                                } else {
                                    hpSsidCnt.put(tempSsid, new AtomicInteger(1));
                                }
                            }
                            builder.append(tempSsid);
                            builder.append(";");
                            builder.append(tempBssid);
                            builder.append(";");
                            builder.append(result.level);
                            builder.append(";");
                            builder.append(result.frequency);
                            builder.append(",");
                        }
                    }
                }
            }
            int addEpApsCnt = addEnterpriseAps(hpSsidCnt);
            if (addEpApsCnt > 0) {
                LogUtil.d(false, "addEnterpriseAps,cnt: %{public}d", Integer.valueOf(addEpApsCnt));
            }
        } catch (RuntimeException e) {
            LogUtil.e(false, " CollectFingersHandler %{public}s", e.getMessage());
        } catch (Exception e2) {
            LogUtil.e(false, "getSendWiFiInfo failed by Exception", new Object[0]);
        }
        LogUtil.i(false, builder.toString(), new Object[0]);
        return builder.toString();
    }

    public int addEnterpriseAps(HashMap<String, AtomicInteger> apInfos) {
        if (apInfos == null || apInfos.size() == 0) {
            return 0;
        }
        int cnt = 0;
        for (Map.Entry<String, AtomicInteger> entry : apInfos.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue().get() > 1) {
                ApInfo tempAp = new ApInfo(key, TimeUtil.getTime());
                if (this.enterpriseApDao.insert(tempAp)) {
                    cnt++;
                } else {
                    LogUtil.d(false, "enterpriseApDao.insert failure", new Object[0]);
                    LogUtil.i(false, "ap:%{public}s", tempAp.toString());
                }
            }
        }
        return cnt;
    }

    public void mainApInfoToDb() {
        try {
            if (this.regularPlaceDao != null && this.mainApPlaceInfo != null && !this.regularPlaceDao.update(this.mainApPlaceInfo)) {
                LogUtil.d(false, "mainApInfoToDb,update mainApPlaceInfo failure.9999:%{private}s", this.mainApPlaceInfo.toString());
            }
        } catch (Exception e) {
            LogUtil.e(false, "mainApInfoToDb failed by Exception", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMainApInfoToFile() {
        try {
            StringBuilder builder = new StringBuilder(16);
            Bundle wifiInfo = NetUtil.getWifiStateString(this.context);
            if (wifiInfo == null) {
                LogUtil.e(false, "wifiInfo is null", new Object[0]);
                return;
            }
            LogUtil.i(false, "sendMainApInfoToFile,wifiInfo.wifiAp:%{public}s,this.currLoc:%{private}s", wifiInfo.getString(KEY_WIFI_AP), this.currLoc);
            if (!wifiInfo.getString(KEY_WIFI_STATE, "UNKNOWN").equals(KEY_ENABLED)) {
                LogUtil.i(false, "wifi not ENABLED", new Object[0]);
                return;
            }
            String wifiSsid = wifiInfo.getString(KEY_WIFI_AP, "UNKNOWN");
            if (wifiSsid == null) {
                wifiSsid = "UNKNOWN";
            }
            String dataPath = NetUtil.getNetworkType(this.context);
            builder.append(BehaviorReceiver.getBatch());
            builder.append(",");
            if (this.mainApPlaceInfo == null && this.regularPlaceDao != null) {
                this.mainApPlaceInfo = this.regularPlaceDao.findBySsid(this.currLoc, true);
            }
            if (this.mainApPlaceInfo != null) {
                this.mainApPlaceInfo.setFingerNum(this.mainApPlaceInfo.getFingerNum() + 1);
                if (this.mainApLastBatch != BehaviorReceiver.getBatch()) {
                    this.mainApPlaceInfo.setBatch(this.mainApPlaceInfo.getBatch() + 1);
                }
            } else {
                LogUtil.d(false, "sendMainApInfoToFile,update mainApPlaceInfo failure.mainApPlaceInfo==null", new Object[0]);
            }
            this.mainApLastBatch = BehaviorReceiver.getBatch();
            builder.append(BehaviorReceiver.getArState());
            builder.append(",");
            TimeUtil timeUtil2 = this.timeUtil;
            builder.append(TimeUtil.getTime());
            builder.append(",");
            builder.append(System.currentTimeMillis());
            builder.append(",");
            builder.append(this.currLoc);
            builder.append(",");
            builder.append(BehaviorReceiver.getScreenState());
            builder.append(",");
            builder.append(dataPath);
            builder.append(",");
            builder.append("0");
            builder.append(",");
            builder.append("0");
            builder.append(",");
            builder.append(wifiInfo.getString(KEY_WIFI_STATE, "UNKNOWN"));
            builder.append(",");
            builder.append(wifiSsid.replace(",", "").replace(BACK_SLASH, ""));
            builder.append(";");
            builder.append(wifiInfo.getString(KEY_WIFI_MAC, "UNKNOWN"));
            builder.append(";");
            builder.append(wifiInfo.getString(KEY_WIFI_RSSI, "UNKNOWN"));
            builder.append(";");
            builder.append(wifiInfo.getString(KEY_WIFI_CHANNEL, "UNKNOWN"));
            builder.append(";");
            builder.append(wifiInfo.getString(KEY_WIFI_LIST, "UNKNOWN"));
            builder.append(",");
            builder.append("0");
            builder.append(",");
            builder.append("0");
            builder.append(";");
            builder.append("0");
            builder.append(";");
            builder.append("0");
            builder.append(";");
            builder.append("0");
            builder.append(",");
            builder.append(Constant.getLineSeparator());
            FileUtils.writeFile(this.mainApCollectFileName, builder.toString());
        } catch (Exception e) {
            LogUtil.e(false, "isSendAllInfoToFile failed by Exception", new Object[0]);
        }
    }

    private boolean isSendAllInfoToFile(String batch2, String resultWifiScan2) {
        try {
            StringBuilder builder = new StringBuilder(16);
            Bundle wifiInfo = NetUtil.getWifiStateString(this.context);
            if (wifiInfo == null) {
                LogUtil.e(false, "wifiInfo is null", new Object[0]);
                return false;
            }
            String dataPath = NetUtil.getNetworkType(this.context);
            if (batch2 != null) {
                if (!"".equals(batch2)) {
                    if (resultWifiScan2 != null) {
                        if (!"".equals(resultWifiScan2)) {
                            String wifiSsid = wifiInfo.getString(KEY_WIFI_AP, "UNKNOWN");
                            if (wifiSsid == null) {
                                wifiSsid = "UNKNOWN";
                            }
                            LogUtil.i(false, " isSendAllInfoToFile batch:%{public}s", batch2);
                            builder.append(batch2);
                            builder.append(",");
                            builder.append(BehaviorReceiver.getArState());
                            builder.append(",");
                            TimeUtil timeUtil2 = this.timeUtil;
                            builder.append(TimeUtil.getTime());
                            builder.append(",");
                            builder.append(System.currentTimeMillis());
                            builder.append(",");
                            builder.append(this.currLoc);
                            builder.append(",");
                            builder.append(BehaviorReceiver.getScreenState());
                            builder.append(",");
                            builder.append(dataPath);
                            builder.append(",");
                            builder.append("0");
                            builder.append(",");
                            builder.append("0");
                            builder.append(",");
                            builder.append(wifiInfo.getString(KEY_WIFI_STATE, "UNKNOWN"));
                            builder.append(",");
                            builder.append(wifiSsid.replace(",", "").replace(BACK_SLASH, ""));
                            builder.append(",");
                            builder.append(wifiInfo.getString(KEY_WIFI_MAC, "UNKNOWN"));
                            builder.append(",");
                            builder.append(wifiInfo.getString(KEY_WIFI_CHANNEL, "UNKNOWN"));
                            builder.append(",");
                            builder.append(wifiInfo.getString(KEY_WIFI_LIST, "UNKNOWN"));
                            builder.append(",");
                            builder.append(wifiInfo.getString(KEY_WIFI_RSSI, "UNKNOWN"));
                            builder.append(",");
                            builder.append("0");
                            builder.append(",");
                            builder.append("0");
                            builder.append(",");
                            builder.append("0");
                            builder.append(",");
                            builder.append("0");
                            builder.append(",");
                            builder.append("0");
                            builder.append(",");
                            builder.append(resultWifiScan2);
                            builder.append(Constant.getLineSeparator());
                            FileUtils.writeFile(this.strCollectFileName, builder.toString());
                            return true;
                        }
                    }
                    LogUtil.d(false, " isSendAllInfoToFile resultWifiScan == null", new Object[0]);
                    return false;
                }
            }
            LogUtil.d(false, " isSendAllInfoToFile batch == null", new Object[0]);
            return false;
        } catch (Exception e) {
            LogUtil.e(false, "isSendAllInfoToFile failed by Exception", new Object[0]);
            return false;
        }
    }
}
