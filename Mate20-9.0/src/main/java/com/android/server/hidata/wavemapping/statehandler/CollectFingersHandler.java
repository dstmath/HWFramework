package com.android.server.hidata.wavemapping.statehandler;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dao.EnterpriseApDAO;
import com.android.server.hidata.wavemapping.dao.MobileApDAO;
import com.android.server.hidata.wavemapping.dao.RegularPlaceDAO;
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
    private static final String TAG = ("WMapping." + CollectFingersHandler.class.getSimpleName());
    /* access modifiers changed from: private */
    public int PRINTCNT = 10;
    private String batch = "";
    private Context context;
    private String currLoc = Constant.NAME_FREQLOCATION_OTHER;
    private EnterpriseApDAO enterpriseApDAO;
    /* access modifiers changed from: private */
    public Handler handler = new Handler();
    private int lastBatch;
    /* access modifiers changed from: private */
    public int logCounter = 0;
    private Handler mMachineHandler;
    private String mainApCollectFileName = "";
    private int mainApLastBatch;
    private RegularPlaceInfo mainApPlaceInfo;
    private MobileApDAO mobileApDAO;
    private ParameterInfo param;
    /* access modifiers changed from: private */
    public ParameterInfo paramMainAp;
    private Runnable periodicToFileHandler = new Runnable() {
        public void run() {
            CollectFingersHandler.this.sendMainApInfoToFile();
            CollectFingersHandler.this.handler.postDelayed(this, (long) CollectFingersHandler.this.paramMainAp.getActiveSample());
            int unused = CollectFingersHandler.this.logCounter = CollectFingersHandler.this.logCounter + 1;
            if (CollectFingersHandler.this.logCounter > CollectFingersHandler.this.PRINTCNT) {
                LogUtil.d("periodic MainAp ToFileHandler write to file");
                int unused2 = CollectFingersHandler.this.logCounter = 0;
            }
        }
    };
    private RegularPlaceDAO regularPlaceDAO;
    private String resultWifiScan = "";
    private String strCollectFileName = "temp_file_wp_data.csv";
    private TimeUtil timeUtil = new TimeUtil();
    private long timeWiFiScan = 0;
    private UiInfo uiInfo;
    private UiService uiService;

    public CollectFingersHandler(Context context2, String currlocation, Handler handler2) {
        LogUtil.i(" ,new CollectFingersHandler ");
        try {
            this.context = context2;
            this.currLoc = currlocation;
            this.strCollectFileName = Constant.getRawDataPath() + this.currLoc + Constant.RAW_FILE_EXTENSION;
            this.mainApCollectFileName = Constant.getRawDataPath() + this.currLoc + Constant.MAINAP_RAW_FILE_EXTENSION;
            this.param = ParamManager.getInstance().getParameterInfo();
            this.paramMainAp = ParamManager.getInstance().getMainApParameterInfo();
            this.regularPlaceDAO = new RegularPlaceDAO();
            this.uiService = new UiService();
            UiService uiService2 = this.uiService;
            this.uiInfo = UiService.getUiInfo();
            this.mMachineHandler = handler2;
            this.mobileApDAO = new MobileApDAO();
            this.enterpriseApDAO = new EnterpriseApDAO();
        } catch (Exception e) {
            LogUtil.e(" CollectFingersHandler " + e.getMessage());
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
            LogUtil.d(" startCollect ");
            this.handler.postDelayed(this.periodicToFileHandler, (long) this.paramMainAp.getActiveSample());
        } catch (Exception e) {
            LogUtil.e(" CollectFingersHandler " + e.getMessage());
        }
    }

    public boolean stopCollect() {
        try {
            LogUtil.d(" stopCollect ");
            this.handler.removeCallbacks(this.periodicToFileHandler);
            return true;
        } catch (Exception e) {
            LogUtil.e(" stopCollect " + e.getMessage());
            return false;
        }
    }

    public boolean processWifiData(List<ScanResult> wifiList, RegularPlaceInfo placeInfo) {
        LogUtil.i("processWifiData ....");
        if (placeInfo == null) {
            LogUtil.i("processWifiData,failure, null == placeInfo");
            return false;
        }
        try {
            LogUtil.i(" collectFinger: current batch=" + BehaviorReceiver.getBatch() + ", last Batch=" + this.lastBatch + ", placeInfo=" + placeInfo.toString());
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
            boolean arStation = BehaviorReceiver.getArState();
            if (arStation) {
                this.resultWifiScan = getSendWiFiInfo(wifiList);
                if (!sendAllInfoToFile(this.batch, this.resultWifiScan)) {
                    LogUtil.d("sendAllInfoToFile failure.");
                    return false;
                } else if (!this.regularPlaceDAO.update(placeInfo)) {
                    LogUtil.d("processWifiData,update placeInfo failure." + placeInfo.toString());
                }
            }
            LogUtil.i("processWifiData placeInfo :" + placeInfo.toString() + ", station:" + arStation);
            if (this.uiInfo != null) {
                this.uiInfo.setFinger_batch_num(placeInfo.getBatch());
                this.uiInfo.setFg_fingers_num(placeInfo.getFingerNum());
                this.uiInfo.setStage(placeInfo.getState());
                this.uiInfo.setSsid(placeInfo.getPlace());
                this.uiInfo.setToast("processWifiData complete.");
            }
            UiService uiService2 = this.uiService;
            UiService.sendMsgToUi();
        } catch (Exception e) {
            LogUtil.e("processWifiData:" + e.getMessage());
        }
        return true;
    }

    public boolean checkDataSatisfiedToTraining(RegularPlaceInfo placeInfo) {
        LogUtil.i(" checkDataSatisfiedToTraining01, placeInfo:" + placeInfo.toString());
        if (this.param == null) {
            LogUtil.w(" param == null");
            return false;
        } else if (!isFitTrainModel(placeInfo, this.param)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isFitTrainModel(RegularPlaceInfo placeInfo, ParameterInfo param2) {
        LogUtil.d(" Batch number reach criteria:" + param2.getFg_batch_num() + ", internval=" + Constant.LIMIT_TRAINING_INTERVAL);
        if (placeInfo.getBatch() < param2.getFg_batch_num()) {
            LogUtil.d(" acc batch numbers are NOT enough:" + placeInfo.getBatch());
            return false;
        } else if (placeInfo.getDisNum() == 0 && placeInfo.getFingerNum() > param2.getTrainDatasSize() + param2.getTestDataSize()) {
            LogUtil.d("isFitTrainModel,return true2");
            return true;
        } else if (placeInfo.getDisNum() <= 0 || placeInfo.getTestDataNum() <= param2.getTestDataCnt()) {
            return false;
        } else {
            LogUtil.d("isFitTrainModel,return true3");
            return true;
        }
    }

    private String getSendWiFiInfo(List<ScanResult> wifiList) {
        if (wifiList == null) {
            return null;
        }
        StringBuilder fLine = new StringBuilder();
        HashMap<String, AtomicInteger> hpSsidCnt = new HashMap<>();
        try {
            int size = wifiList.size();
            for (int i = 0; i < size; i++) {
                String tempSsid = wifiList.get(i).SSID.replace(",", "").replace(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER, "");
                if (!tempSsid.equals("")) {
                    String tempBssid = wifiList.get(i).BSSID.replace(",", "").replace(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER, "");
                    if (!tempBssid.equals("")) {
                        if (this.mobileApDAO.findBySsidForUpdateTime(tempSsid, tempBssid) == null) {
                            if (this.enterpriseApDAO.findBySsidForUpdateTime(tempSsid) == null) {
                                if (hpSsidCnt.containsKey(tempSsid)) {
                                    hpSsidCnt.get(tempSsid).incrementAndGet();
                                } else {
                                    hpSsidCnt.put(tempSsid, new AtomicInteger(1));
                                }
                            }
                            fLine.append(tempSsid);
                            fLine.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                            fLine.append(tempBssid);
                            fLine.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                            fLine.append(wifiList.get(i).level);
                            fLine.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
                            fLine.append(wifiList.get(i).frequency);
                            fLine.append(",");
                        }
                    }
                }
            }
            int addEpApsCnt = addEnterpriseAps(hpSsidCnt);
            if (addEpApsCnt > 0) {
                LogUtil.d("addEnterpriseAps,cnt: " + addEpApsCnt);
            }
        } catch (RuntimeException e) {
            LogUtil.e(" CollectFingersHandler " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e("getSendWiFiInfo:" + e2.getMessage());
        }
        LogUtil.i(fLine.toString());
        return fLine.toString();
    }

    public int addEnterpriseAps(HashMap<String, AtomicInteger> apInfos) {
        int cnt = 0;
        if (apInfos == null || apInfos.size() == 0) {
            return 0;
        }
        for (Map.Entry entry : apInfos.entrySet()) {
            String key = (String) entry.getKey();
            if (((AtomicInteger) entry.getValue()).get() > 1) {
                ApInfo tempAp = new ApInfo(key, TimeUtil.getTime());
                if (this.enterpriseApDAO.insert(tempAp)) {
                    cnt++;
                } else {
                    LogUtil.d("enterpriseApDAO.insert failure");
                    LogUtil.i("                               ap:" + tempAp.toString());
                }
            }
        }
        return cnt;
    }

    public void mainApInfoToDb() {
        try {
            if (!(this.regularPlaceDAO == null || this.mainApPlaceInfo == null || this.regularPlaceDAO.update(this.mainApPlaceInfo))) {
                LogUtil.d("mainApInfoToDb,update mainApPlaceInfo failure.9999:" + this.mainApPlaceInfo.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void sendMainApInfoToFile() {
        try {
            StringBuilder fLine = new StringBuilder();
            Bundle wifiInfo = NetUtil.getWifiStateString(this.context);
            LogUtil.i("sendMainApInfoToFile,wifiInfo.wifiAp:" + wifiInfo.getString("wifiAp") + ",this.currLoc:" + this.currLoc);
            if (!wifiInfo.getString("wifiState", "UNKNOWN").equals("ENABLED")) {
                LogUtil.i("wifi not ENABLED");
                return;
            }
            String wifiSsid = wifiInfo.getString("wifiAp", "UNKNOWN");
            if (wifiSsid == null) {
                wifiSsid = "UNKNOWN";
            }
            String dataPath = NetUtil.getNetworkType(this.context);
            fLine.append(BehaviorReceiver.getBatch());
            fLine.append(",");
            if (this.mainApPlaceInfo == null && this.regularPlaceDAO != null) {
                this.mainApPlaceInfo = this.regularPlaceDAO.findBySsid(this.currLoc, true);
            }
            if (this.mainApPlaceInfo != null) {
                this.mainApPlaceInfo.setFingerNum(this.mainApPlaceInfo.getFingerNum() + 1);
                if (this.mainApLastBatch != BehaviorReceiver.getBatch()) {
                    this.mainApPlaceInfo.setBatch(this.mainApPlaceInfo.getBatch() + 1);
                }
            } else {
                LogUtil.d("sendMainApInfoToFile,update mainApPlaceInfo failure.mainApPlaceInfo==null");
            }
            this.mainApLastBatch = BehaviorReceiver.getBatch();
            fLine.append(BehaviorReceiver.getArState());
            fLine.append(",");
            TimeUtil timeUtil2 = this.timeUtil;
            fLine.append(TimeUtil.getTime());
            fLine.append(",");
            fLine.append(System.currentTimeMillis());
            fLine.append(",");
            fLine.append(this.currLoc);
            fLine.append(",");
            fLine.append(BehaviorReceiver.getScrnState());
            fLine.append(",");
            fLine.append(dataPath);
            fLine.append(",");
            fLine.append("0");
            fLine.append(",");
            fLine.append("0");
            fLine.append(",");
            fLine.append(wifiInfo.getString("wifiState", "UNKNOWN"));
            fLine.append(",");
            fLine.append(wifiSsid.replace(",", "").replace("\"", ""));
            fLine.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            fLine.append(wifiInfo.getString("wifiMAC", "UNKNOWN"));
            fLine.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            fLine.append(wifiInfo.getString("wifiRssi", "UNKNOWN"));
            fLine.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            fLine.append(wifiInfo.getString("wifiCh", "UNKNOWN"));
            fLine.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            fLine.append(wifiInfo.getString("wifiLS", "UNKNOWN"));
            fLine.append(",");
            fLine.append("0");
            fLine.append(",");
            fLine.append("0");
            fLine.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            fLine.append("0");
            fLine.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            fLine.append("0");
            fLine.append(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            fLine.append("0");
            fLine.append(",");
            fLine.append(Constant.getLineSeperate());
            FileUtils.writeFile(this.mainApCollectFileName, fLine.toString());
        } catch (Exception e) {
            LogUtil.e(" sendAllInfoToFile " + e.getMessage());
        }
    }

    private boolean sendAllInfoToFile(String batch2, String resultWifiScan2) {
        try {
            StringBuilder fLine = new StringBuilder();
            Bundle wifiInfo = NetUtil.getWifiStateString(this.context);
            String dataPath = NetUtil.getNetworkType(this.context);
            if (batch2 != null) {
                if (!batch2.equals("")) {
                    if (resultWifiScan2 != null) {
                        if (!resultWifiScan2.equals("")) {
                            String wifiSsid = wifiInfo.getString("wifiAp", "UNKNOWN");
                            if (wifiSsid == null) {
                                wifiSsid = "UNKNOWN";
                            }
                            LogUtil.i(" sendAllInfoToFile batch:" + batch2);
                            fLine.append(batch2);
                            fLine.append(",");
                            fLine.append(BehaviorReceiver.getArState());
                            fLine.append(",");
                            TimeUtil timeUtil2 = this.timeUtil;
                            fLine.append(TimeUtil.getTime());
                            fLine.append(",");
                            fLine.append(System.currentTimeMillis());
                            fLine.append(",");
                            fLine.append(this.currLoc);
                            fLine.append(",");
                            fLine.append(BehaviorReceiver.getScrnState());
                            fLine.append(",");
                            fLine.append(dataPath);
                            fLine.append(",");
                            fLine.append("0");
                            fLine.append(",");
                            fLine.append("0");
                            fLine.append(",");
                            fLine.append(wifiInfo.getString("wifiState", "UNKNOWN"));
                            fLine.append(",");
                            fLine.append(wifiSsid.replace(",", "").replace("\"", ""));
                            fLine.append(",");
                            fLine.append(wifiInfo.getString("wifiMAC", "UNKNOWN"));
                            fLine.append(",");
                            fLine.append(wifiInfo.getString("wifiCh", "UNKNOWN"));
                            fLine.append(",");
                            fLine.append(wifiInfo.getString("wifiLS", "UNKNOWN"));
                            fLine.append(",");
                            fLine.append(wifiInfo.getString("wifiRssi", "UNKNOWN"));
                            fLine.append(",");
                            fLine.append("0");
                            fLine.append(",");
                            fLine.append("0");
                            fLine.append(",");
                            fLine.append("0");
                            fLine.append(",");
                            fLine.append("0");
                            fLine.append(",");
                            fLine.append("0");
                            fLine.append(",");
                            fLine.append(resultWifiScan2);
                            fLine.append(Constant.getLineSeperate());
                            FileUtils.writeFile(this.strCollectFileName, fLine.toString());
                            String resultWifiScan3 = "";
                            return true;
                        }
                    }
                    LogUtil.d(" sendAllInfoToFile resultWifiScan == null");
                    return false;
                }
            }
            LogUtil.d(" sendAllInfoToFile batch == null");
            return false;
        } catch (Exception e) {
            LogUtil.e(" sendAllInfoToFile " + e.getMessage());
            return false;
        }
    }
}
