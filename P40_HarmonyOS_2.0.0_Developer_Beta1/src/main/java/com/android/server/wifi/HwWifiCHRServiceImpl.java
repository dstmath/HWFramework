package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.IpConfiguration;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.fastsleep.FsArbitration;
import com.android.server.wifi.wifi2.HwWifi2Manager;
import com.android.server.wifi.wifinearfind.HwWifiNearFindArbitration;
import com.android.server.wifi.wifinearfind.HwWifiNearFindUtils;
import com.huawei.ncdft.HwNcDftConnManager;
import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HwWifiCHRServiceImpl implements HwWifiCHRService {
    private static final int BYTE_OFFSET = 8;
    private static final int CMD_GET_LONGSLEEPCNT = 118;
    private static final int CMD_GET_RXLISTENINTERVAL = 124;
    private static final int CMD_GET_RXLISTENSTATE = 123;
    private static final int CMD_GET_SHORTSLEEPCNT = 117;
    private static final int CMD_NET_STATS_POLL = 2;
    private static final int CMD_REPORT_WIFI_DFT = 0;
    private static final int CMD_WIFI_NETWORK_STATE_CHANGE = 1;
    private static final String DEFAULT_ENCODE_TYPE = "UTF-8";
    private static final int E909002024_NO_INTERNET = 909002024;
    private static final int EID_80211AX = 255;
    private static final int EID_80211K = 70;
    private static final int EID_80211R = 54;
    private static final int EID_80211V = 127;
    private static final int HE_CAPABILITY_80211AX = 35;
    private static final int IE_BYTES_HESSID_LENGTH = 5;
    private static final String KEY_MGMT_EAP_SUITE_B_192 = "EAP_SUITE_B_192";
    private static final String KEY_MGMT_OWE = "OWE";
    private static final String KEY_MGMT_SAE = "SAE";
    private static final String KEY_OF_BEACON_IE = "beaconIe";
    private static final String KEY_OF_BSSID = "bssid";
    private static final String KEY_OF_CAPABILITIES = "wpa3Caps";
    private static final String KEY_OF_CHECK_REASON = "cCheckReason";
    private static final String KEY_OF_CONNECTED_STATE = "isConnected";
    private static final String KEY_OF_FIRST_CONNECT = "isFirstConnect";
    private static final String KEY_OF_FREQ = "frequency";
    private static final String KEY_OF_LOCAL_GENERATE = "local_gerenate";
    private static final String KEY_OF_LONG_IDLE = "longIdle";
    private static final String KEY_OF_NO_INTERNET_REASON = "errReason";
    private static final String KEY_OF_PASSPOINT = "passPoint";
    private static final String KEY_OF_PUBLIC_ESS_CNT = "publicEssCnt";
    private static final String KEY_OF_REASON_CODE = "reasoncode";
    private static final String KEY_OF_RXLISTEN_INTERVAL = "rxlistenInterval";
    private static final String KEY_OF_RXLISTEN_STATE = "rxlistenState";
    private static final String KEY_OF_SHORT_IDLE = "shortIdle";
    private static final String KEY_OF_SSID = "ssid";
    private static final String KEY_OF_SUPPLICANT_STATE = "supplicantState";
    private static final String KEY_OF_VENDOR_INFO = "vendorInfo";
    private static final String LOG_TAG = "HwNcChrServiceImpl";
    private static final int MSG_ROUTER_INFO_COLLECT = 909002029;
    private static final int ROUTER_INFO_COLLECT_DURA = 3600000;
    private static final String RSN_80211AX = "[11AX]";
    private static final String RSN_80211K = "[11K]";
    private static final String RSN_80211R = "[11R]";
    private static final String RSN_80211V = "[11V]";
    private static final int SECURITY_EAP = 3;
    private static final int SECURITY_NONE = 0;
    private static final int SECURITY_PSK = 2;
    private static final int SECURITY_WAPI_CERT = 5;
    private static final int SECURITY_WAPI_PSK = 4;
    private static final int SECURITY_WEP = 1;
    private static final int TIME_POLL_AFTER_CONNECT_DELAYED = 2000;
    private static final int TIME_POLL_TRAFFIC_STATS_INTERVAL = 10000;
    private static final int WIFI_NEAR_FIND_OFF = 1;
    private static final int WIFI_NEAR_FIND_ON = 0;
    private static final int WIFI_SECURITY_TYPE_UNKNOWN = -1;
    private static final short WPS_DEV_NAME_TYPE = 4368;
    private static final short WPS_MANUFACTURER_TYPE = 8464;
    private static final short WPS_MODEL_NAME_TYPE = 8976;
    private static final short WPS_MODEL_NUMBER_TYPE = 9232;
    private static final int WPS_VENDOR_OUI_TYPE = 82989056;
    private static HwWifiCHRService sInstance;
    private HwNcDftConnManager mClient;
    private Context mContext;
    private NCDFTExceptionHandler mNCDFTExceptionHandler;
    private HWNetstatManager mNetstatManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.HwWifiCHRServiceImpl.AnonymousClass1 */
        boolean isConnected = false;

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (!TextUtils.isEmpty(action)) {
                    if (action.equals("android.net.wifi.STATE_CHANGE")) {
                        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (networkInfo != null && this.isConnected != networkInfo.isConnected()) {
                            this.isConnected = networkInfo.isConnected();
                            HwWifiCHRServiceImpl.this.mNCDFTExceptionHandler.obtainMessage(1, this.isConnected ? 1 : 0, 0).sendToTarget();
                        }
                    } else if (action.equals("android.net.wifi.supplicant.STATE_CHANGE")) {
                        HwWifiCHRServiceImpl.this.handleSupplicantStateAction(intent);
                    } else {
                        HwHiLog.d(HwWifiCHRServiceImpl.LOG_TAG, false, "unhandled other broadcast action", new Object[0]);
                    }
                }
            }
        }
    };
    private WifiManager mWifiManager;

    public static synchronized void init(Context context) {
        synchronized (HwWifiCHRServiceImpl.class) {
            if (sInstance == null) {
                sInstance = new HwWifiCHRServiceImpl(context);
            }
        }
    }

    public static synchronized HwWifiCHRService getInstance() {
        HwWifiCHRService hwWifiCHRService;
        synchronized (HwWifiCHRServiceImpl.class) {
            hwWifiCHRService = sInstance;
        }
        return hwWifiCHRService;
    }

    public HwWifiCHRServiceImpl(Context context) {
        this.mContext = context;
        this.mClient = new HwNcDftConnManager(this.mContext);
        HandlerThread thread = new HandlerThread(LOG_TAG);
        thread.start();
        Looper looper = thread.getLooper();
        this.mNetstatManager = HWNetstatManager.getInstance(this.mContext);
        this.mNCDFTExceptionHandler = new NCDFTExceptionHandler(looper);
        this.mNCDFTExceptionHandler.sendMessageDelayed(this.mNCDFTExceptionHandler.obtainMessage(MSG_ROUTER_INFO_COLLECT), 3600000);
        registerForBroadcasts();
    }

    private void registerForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isStaticIpWifiConfig() {
        List<WifiConfiguration> configuredNetworks;
        WifiInfo info;
        int netId;
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager == null || (configuredNetworks = wifiManager.getConfiguredNetworks()) == null || (info = wifiManager.getConnectionInfo()) == null || (netId = info.getNetworkId()) == -1) {
            return false;
        }
        for (WifiConfiguration config : configuredNetworks) {
            if (config.networkId == netId && config.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public class NCDFTExceptionHandler extends Handler {
        int token = 0;

        public NCDFTExceptionHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            int i2 = 1;
            if (i != 0) {
                if (i == 1) {
                    HwWifiCHRServiceImpl.this.mNetstatManager.resetNetstats();
                    this.token++;
                    if (msg.arg1 == 1) {
                        HwWifiCHRServiceImpl hwWifiCHRServiceImpl = HwWifiCHRServiceImpl.this;
                        if (!hwWifiCHRServiceImpl.isStaticIpWifiConfig()) {
                            i2 = 2;
                        }
                        hwWifiCHRServiceImpl.setIpType(i2);
                        sendMessageDelayed(obtainMessage(2, this.token, 0), 2000);
                        HwWifiCHRServiceImpl.this.collectRouterInfoByConnectedAp();
                    }
                } else if (i != 2) {
                    if (i == HwWifiCHRServiceImpl.MSG_ROUTER_INFO_COLLECT) {
                        HwWifiCHRServiceImpl.this.collectRouterInfoByScanResult();
                    }
                } else if (msg.arg1 == this.token) {
                    HwWifiCHRServiceImpl.this.mNetstatManager.performPollAndLog();
                    sendMessageDelayed(obtainMessage(2, this.token, 0), 10000);
                }
            } else if (HwWifiCHRServiceImpl.this.mClient != null) {
                HwWifiCHRServiceImpl.this.mClient.reportToDft(1, msg.arg1, msg.getData());
            } else {
                HwHiLog.e(HwWifiCHRServiceImpl.LOG_TAG, false, "reportWifiDFTEvent,mClient is null", new Object[0]);
            }
        }
    }

    private void putRxListenState(Bundle msgData) {
        if (!HwWifiNearFindUtils.isWifiNearFindSwitchOn() || HwWifiNearFindUtils.getWifiNearFindLin() == 0 || !HwWifiNearFindUtils.isHiLinkInstalled(this.mContext)) {
            HwHiLog.i(LOG_TAG, false, "wifi near find switch is not allowed", new Object[0]);
            return;
        }
        HwWifiNearFindArbitration wifiNearFindArbitration = HwWifiNearFindArbitration.getInstance(this.mContext);
        if (wifiNearFindArbitration == null || msgData == null) {
            HwHiLog.i(LOG_TAG, false, "wifiNearFindArbitration == null or msgdata is null", new Object[0]);
            return;
        }
        int wifiNearFindState = wifiNearFindArbitration.getArbitrationCond() == 0 ? 0 : 1;
        HwHiLog.d(LOG_TAG, false, "wifi near find state is " + wifiNearFindState, new Object[0]);
        int startScanInterval = (int) wifiNearFindArbitration.getStartScanInterval();
        msgData.putInt(KEY_OF_RXLISTEN_STATE, wifiNearFindState);
        if (wifiNearFindState == 0) {
            msgData.putInt(KEY_OF_RXLISTEN_INTERVAL, startScanInterval);
        } else {
            HwHiLog.i(LOG_TAG, false, "wifi near find is closed, don't record the scan interval", new Object[0]);
        }
    }

    private void putFastsleepIdle(Bundle msgData) {
        int longIdle;
        int shortIdle;
        FsArbitration fsArbitration = FsArbitration.getInstance();
        if (fsArbitration == null || msgData == null) {
            HwHiLog.i(LOG_TAG, false, "create fsArbitration fail or msgdata is null", new Object[0]);
            return;
        }
        int shortIdle2 = fsArbitration.sendFastSleepCmdtoDriver(117);
        int longIdle2 = fsArbitration.sendFastSleepCmdtoDriver(118);
        int lastFsOnShortIdle = fsArbitration.getFastSleepShortIdle();
        int lastFsOnLongIdle = fsArbitration.getFastSleepLongIdle();
        if (shortIdle2 < 0 || longIdle2 < 0 || shortIdle2 < lastFsOnShortIdle || longIdle2 < lastFsOnLongIdle) {
            shortIdle = 0;
            longIdle = 0;
        } else {
            shortIdle = shortIdle2 - lastFsOnShortIdle;
            longIdle = longIdle2 - lastFsOnLongIdle;
        }
        msgData.putInt(KEY_OF_SHORT_IDLE, shortIdle);
        msgData.putInt(KEY_OF_LONG_IDLE, longIdle);
    }

    public void uploadDisconnectException(int code) {
        Bundle data = new Bundle();
        putFastsleepIdle(data);
        putRxListenState(data);
        data.putInt(KEY_OF_REASON_CODE, 65535 & code);
        data.putInt(KEY_OF_LOCAL_GENERATE, code >> 16);
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1006, 0);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void uploadAssocRejectException(int status) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1022, 0);
        Bundle data = new Bundle();
        data.putInt("status", status);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateWifiException(int ucErrorCode, String ucSubErrorCode) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1023, 0);
        Bundle data = new Bundle();
        putFastsleepIdle(data);
        putRxListenState(data);
        data.putInt("ucErrorCode", ucErrorCode);
        data.putString("ucSubErrorCode", ucSubErrorCode);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateWifiAuthFailEvent(String iface, int reason) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1016, 0);
        Bundle data = new Bundle();
        data.putInt("reason", reason);
        data.putString("iface", iface);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateWifiTriggerState(boolean enable) {
        HwHiLog.e(LOG_TAG, false, "updateWifiTriggerState,enable:%{public}s", new Object[]{String.valueOf(enable)});
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1021, 0);
        Bundle data = new Bundle();
        data.putBoolean("enable", enable);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void reportHwCHRAccessNetworkEventInfoList(int reasoncode) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1017, 0);
        Bundle data = new Bundle();
        data.putInt(KEY_OF_REASON_CODE, reasoncode);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateConnectType(String type) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1029, 0);
        Bundle data = new Bundle();
        data.putString("type", type);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateApkChangewWifiStatus(int apkAction, String apkName) {
        HwHiLog.i(LOG_TAG, false, "updateApkChangewWifiStatus apkAction:%{public}d, apkName:%{public}s", new Object[]{Integer.valueOf(apkAction), apkName});
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1030, 0);
        Bundle data = new Bundle();
        data.putString("apkName", apkName);
        data.putInt("apkAction", apkAction);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void connectFromUserByConfig(WifiConfiguration config) {
    }

    public void updateWIFIConfiguraionByConfig(WifiConfiguration config) {
    }

    public void setBackgroundScanReq(boolean isBackgroundReq) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1034, 0);
        Bundle data = new Bundle();
        data.putBoolean("isBackgroundReq", isBackgroundReq);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void handleSupplicantException() {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, HwWifi2Manager.CLOSE_WIFI2_UNWANTED_NETWORK, 0);
        msg.setData(new Bundle());
        msg.sendToTarget();
    }

    public void updateAccessWebException(int eventId, String reason) {
        updateWifiException(E909002024_NO_INTERNET, reason);
    }

    public void updateMultiGWCount(byte count) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1101, 0);
        Bundle data = new Bundle();
        data.putByte("count", count);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void uploadDFTEvent(int type, String ucSubErrorCode) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1102, 0);
        Bundle data = new Bundle();
        data.putString("ucSubErrorCode", ucSubErrorCode);
        data.putInt("type", type);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateMSSCHR(int switchType, int absState, int reasonCode, ArrayList list) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1103, 0);
        Bundle data = new Bundle();
        data.putSerializable("list", list);
        data.putInt("switchType", switchType);
        data.putInt("absState", absState);
        data.putInt("reasonCode", reasonCode);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateGameBoostLag(String reasoncode, String gameName, int gameRTT, int TcpRtt) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1104, 0);
        Bundle data = new Bundle();
        data.putString(KEY_OF_REASON_CODE, reasoncode);
        data.putString("gameName", gameName);
        data.putInt("gameRTT", gameRTT);
        data.putInt("TcpRtt", TcpRtt);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateMSSState(String state) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1105, 0);
        Bundle data = new Bundle();
        data.putString("state", state);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void handleWiFiDnsStats(int netid) {
    }

    public void updateScCHRCount(int type) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1107, 0);
        Bundle data = new Bundle();
        data.putInt("type", type);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateABSTime(String ssid, int associateTimes, int associateFailedTimes, long mimoTime, long sisoTime, long mimoScreenOnTime, long sisoScreenOnTime) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1108, 0);
        Bundle data = new Bundle();
        data.putString(KEY_OF_SSID, ssid);
        data.putInt("associateTimes", associateTimes);
        data.putInt("associateFailedTimes", associateFailedTimes);
        data.putLong("mimoTime", mimoTime);
        data.putLong("sisoTime", sisoTime);
        data.putLong("mimoScreenOnTime", mimoScreenOnTime);
        data.putLong("sisoScreenOnTime", sisoScreenOnTime);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateMssSucCont(int trigerReason, int reasonCode) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1109, 0);
        Bundle data = new Bundle();
        data.putInt("trigerReason", trigerReason);
        data.putInt("reasonCode", reasonCode);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void txPwrBoostChrStatic(Boolean txBoostEnable, int RTT, int RTTCnt, int txGood, int txBad, int TxRetry) {
        Bundle data = new Bundle();
        data.putBoolean("txBoostEnable", txBoostEnable.booleanValue());
        data.putInt("RTT", RTT);
        data.putInt("RTTCnt", RTTCnt);
        data.putInt("txGood", txGood);
        data.putInt("txBad", txBad);
        data.putInt("TxRetry", TxRetry);
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1110, 0);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateGameBoostStatic(String gameName, boolean gameCnt) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1111, 0);
        Bundle data = new Bundle();
        data.putBoolean("gameCnt", gameCnt);
        data.putString("gameName", gameName);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updataWeChartStatic(int weChartTimes, int lowRssiTimes, int disconnectTimes, int backGroundTimes, int videoTimes) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1112, 0);
        Bundle data = new Bundle();
        data.putInt("weChartTimes", weChartTimes);
        data.putInt("lowRssiTimes", lowRssiTimes);
        data.putInt("disconnectTimes", disconnectTimes);
        data.putInt("backGroundTimes", backGroundTimes);
        data.putInt("videoTimes", videoTimes);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void setGameKogScene(int gameKogScene) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1113, 0);
        Bundle data = new Bundle();
        data.putInt("gameKogScene", gameKogScene);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void setWeChatScene(int weChatScene) {
    }

    public void updateDhcpState(int state) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1020, 0);
        Bundle data = new Bundle();
        data.putInt("state", state);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void uploadDhcpException(String strDhcpError) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1004, 0);
        Bundle data = new Bundle();
        data.putString("strDhcpError", strDhcpError);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updatePortalStatus(int respCode) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1115, 0);
        Bundle data = new Bundle();
        data.putInt("respCode", respCode);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void setIpType(int type) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1116, 0);
        Bundle data = new Bundle();
        data.putInt("type", type);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateRepeaterOpenOrCloseError(int eventId, int openOrClose, String reason) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1117, 0);
        Bundle data = new Bundle();
        data.putInt("eventId", eventId);
        data.putInt("openOrClose", openOrClose);
        data.putString("reason", reason);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateAssocByABS() {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1118, 0);
        msg.setData(new Bundle());
        msg.sendToTarget();
    }

    public void incrAccessWebRecord(int reason, boolean succ, boolean isPortal) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1119, 0);
        Bundle data = new Bundle();
        data.putBoolean("isPortal", isPortal);
        data.putBoolean("succ", succ);
        data.putInt("reason", reason);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updatePortalConnection(int isPortalconnection) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1120, 0);
        Bundle data = new Bundle();
        data.putInt("isPortalconnection", isPortalconnection);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateArpSummery(boolean succ, int spendTime, int rssi) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1121, 0);
        Bundle data = new Bundle();
        data.putBoolean("succ", succ);
        data.putInt("spendTime", spendTime);
        data.putInt("rssi", rssi);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateAPOpenState() {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1035, 0);
        msg.setData(new Bundle());
        msg.sendToTarget();
    }

    public void addWifiRepeaterOpenedCount(int count) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1122, 0);
        Bundle data = new Bundle();
        data.putInt("count", count);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void setWifiRepeaterWorkingTime(long workingTime) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1123, 0);
        Bundle data = new Bundle();
        data.putLong("workingTime", workingTime);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void setRepeaterMaxClientCount(int maxCount) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1124, 0);
        Bundle data = new Bundle();
        data.putInt("maxCount", maxCount);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void addRepeaterConnFailedCount(int failed) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1125, 0);
        Bundle data = new Bundle();
        data.putInt("failed", failed);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void updateAPVendorInfo(String apvendorinfo) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1036, 0);
        Bundle data = new Bundle();
        data.putString("apvendorinfo", apvendorinfo);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void setWifiRepeaterFreq(int freq) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1126, 0);
        Bundle data = new Bundle();
        data.putInt("freq", freq);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void setWifiRepeaterStatus(boolean isopen) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1127, 0);
        Bundle data = new Bundle();
        data.putBoolean("isopen", isopen);
        msg.setData(data);
        msg.sendToTarget();
    }

    public void uploadDFTEvent(int eventId, Bundle bundle) {
        if (bundle != null) {
            Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1128, 0);
            bundle.putInt("WifiChrErrID", eventId);
            msg.setData(bundle);
            msg.sendToTarget();
        }
    }

    public void uploadWifi2DftEvent(int eventId, Bundle bundle) {
        if (bundle != null) {
            Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, eventId, 0);
            msg.setData(bundle);
            msg.sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void collectRouterInfoByScanResult() {
        List<ScanResult> scanResults = getScanResults();
        List<String> ssidList = new ArrayList<>();
        for (ScanResult result : scanResults) {
            String ssid = result.SSID;
            if (!TextUtils.isEmpty(ssid) && !ssidList.contains(ssid) && !TextUtils.isEmpty(result.capabilities)) {
                if (result.capabilities.contains(KEY_MGMT_OWE) || result.capabilities.contains(KEY_MGMT_EAP_SUITE_B_192) || result.capabilities.contains(KEY_MGMT_SAE) || isParse80211axEid(result.informationElements) || isParse80211kEid(result.informationElements) || isParse80211vEid(result.informationElements) || isParse80211rEid(result.informationElements)) {
                    ssidList.add(ssid);
                    uploadRouterInfo(scanResults, result, false);
                }
            }
        }
        this.mNCDFTExceptionHandler.sendMessageDelayed(this.mNCDFTExceptionHandler.obtainMessage(MSG_ROUTER_INFO_COLLECT), 3600000);
    }

    private String parseVendorInfo(ScanResult.InformationElement[] ies) {
        if (ies == null) {
            return "";
        }
        for (ScanResult.InformationElement ie : ies) {
            if (ie != null && ie.id == 221) {
                ByteBuffer data = ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN);
                try {
                    if (data.getInt() == WPS_VENDOR_OUI_TYPE) {
                        return parseWpsInformationElement(data);
                    }
                } catch (BufferUnderflowException e) {
                    HwHiLog.e(LOG_TAG, false, "parseVendorInfo:BufferUnderflowException happen", new Object[0]);
                }
            }
        }
        return "";
    }

    private String parseWpsInformationElement(ByteBuffer data) {
        String manufacturer = "";
        String modelName = "";
        String modelNumber = "";
        String deviceName = "";
        if (data == null) {
            return "";
        }
        while (true) {
            try {
                if (data.position() >= data.limit()) {
                    break;
                }
                int type = data.getShort();
                int length = data.getShort() >> 8;
                if (length <= 0) {
                    break;
                } else if (data.position() + length >= data.limit()) {
                    break;
                } else {
                    if (type == 8464) {
                        manufacturer = new String(data.array(), data.position(), length, DEFAULT_ENCODE_TYPE);
                    } else if (type == 8976) {
                        modelName = new String(data.array(), data.position(), length, DEFAULT_ENCODE_TYPE);
                    } else if (type == 9232) {
                        modelNumber = new String(data.array(), data.position(), length, DEFAULT_ENCODE_TYPE);
                    } else if (type == 4368) {
                        deviceName = new String(data.array(), data.position(), length, DEFAULT_ENCODE_TYPE);
                    } else {
                        HwHiLog.d(LOG_TAG, false, "unparse other wps information element", new Object[0]);
                    }
                    data.position(data.position() + length);
                }
            } catch (BufferUnderflowException e) {
                HwHiLog.e(LOG_TAG, false, "BufferUnderflowException position:%{public}d, limit:%{public}d", new Object[]{Integer.valueOf(data.position()), Integer.valueOf(data.limit())});
                return "";
            } catch (IndexOutOfBoundsException e2) {
                HwHiLog.e(LOG_TAG, false, "IndexOutOfBoundsException position:%{public}d, limit:%{public}d", new Object[]{Integer.valueOf(data.position()), Integer.valueOf(data.limit())});
                return "";
            } catch (UnsupportedEncodingException e3) {
                HwHiLog.e(LOG_TAG, false, "UnsupportedEncodingException, encodeType:%{public}s", new Object[]{DEFAULT_ENCODE_TYPE});
                return "";
            }
        }
        return manufacturer + ", " + modelName + ", " + modelNumber + ", " + deviceName;
    }

    private boolean isParse80211axEid(ScanResult.InformationElement[] ies) {
        if (ies == null) {
            return false;
        }
        for (ScanResult.InformationElement ie : ies) {
            if (ie != null && ie.id == EID_80211AX) {
                try {
                    if (ByteBuffer.wrap(ie.bytes).order(ByteOrder.LITTLE_ENDIAN).get() == 35) {
                        return true;
                    }
                } catch (BufferUnderflowException e) {
                    HwHiLog.e(LOG_TAG, false, "parse80211axEid:BufferUnderflowException happen", new Object[0]);
                }
            }
        }
        return false;
    }

    private boolean isParse80211kEid(ScanResult.InformationElement[] ies) {
        if (ies == null) {
            return false;
        }
        for (ScanResult.InformationElement ie : ies) {
            if (ie != null && ie.id == EID_80211K) {
                return true;
            }
        }
        return false;
    }

    private boolean isParse80211vEid(ScanResult.InformationElement[] ies) {
        if (ies == null) {
            return false;
        }
        for (ScanResult.InformationElement ie : ies) {
            if (ie != null && ie.id == 127) {
                return true;
            }
        }
        return false;
    }

    private boolean isParse80211rEid(ScanResult.InformationElement[] ies) {
        if (ies == null) {
            return false;
        }
        for (ScanResult.InformationElement ie : ies) {
            if (ie != null && ie.id == EID_80211R) {
                return true;
            }
        }
        return false;
    }

    private Bundle parseBeaconIeInfo(ScanResult.InformationElement[] ies) {
        if (ies == null) {
            return new Bundle();
        }
        Bundle ieData = new Bundle();
        for (ScanResult.InformationElement ie : ies) {
            if (ie != null && (ie.id == EID_80211AX || ie.id == EID_80211K || ie.id == 127 || ie.id == EID_80211R || ie.id == EID_80211K || ie.id == 107 || ie.id == 45 || ie.id == 191 || ie.id == 61 || ie.id == 192)) {
                byte[] ieBytes = ie.bytes;
                if (ie.id == 107 && ieBytes.length > 5) {
                    ieBytes = Arrays.copyOfRange(ieBytes, 0, 5);
                }
                ieData.putString(String.valueOf(ie.id), Arrays.toString(ieBytes));
            }
        }
        return ieData;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void collectRouterInfoByConnectedAp() {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            List<ScanResult> scanResults = getScanResults();
            ScanResult scanResult = getScanResultByBssid(scanResults, wifiInfo.getBSSID());
            if (!TextUtils.isEmpty(scanResult.SSID) && !TextUtils.isEmpty(scanResult.BSSID)) {
                uploadRouterInfo(scanResults, scanResult, true);
            }
        }
    }

    private void uploadRouterInfo(List<ScanResult> scanResults, ScanResult info, boolean isConnected) {
        Bundle data = new Bundle();
        data.putInt(KEY_OF_PUBLIC_ESS_CNT, getPublicEssCnt(scanResults, info));
        data.putInt(KEY_OF_PASSPOINT, info.isPasspointNetwork() ? 1 : 0);
        data.putString(KEY_OF_CAPABILITIES, getCapabilities(info));
        data.putString(KEY_OF_VENDOR_INFO, parseVendorInfo(info.informationElements));
        data.putBundle(KEY_OF_BEACON_IE, parseBeaconIeInfo(info.informationElements));
        data.putString(KEY_OF_BSSID, info.BSSID);
        data.putString(KEY_OF_SSID, info.SSID);
        data.putBoolean(KEY_OF_CONNECTED_STATE, isConnected);
        uploadDFTEvent(MSG_ROUTER_INFO_COLLECT, data);
    }

    private String getCapabilities(ScanResult scanResult) {
        String caps = scanResult.capabilities;
        if (isParse80211axEid(scanResult.informationElements)) {
            caps = caps + RSN_80211AX;
        }
        if (isParse80211kEid(scanResult.informationElements)) {
            caps = caps + RSN_80211K;
        }
        if (isParse80211vEid(scanResult.informationElements)) {
            caps = caps + RSN_80211V;
        }
        if (!isParse80211rEid(scanResult.informationElements)) {
            return caps;
        }
        return caps + RSN_80211R;
    }

    private int getPublicEssCnt(List<ScanResult> scanResults, ScanResult scanResult) {
        int publicEssCnt = 0;
        String bssid = scanResult.BSSID;
        String ssid = scanResult.SSID;
        if (TextUtils.isEmpty(bssid) || TextUtils.isEmpty(ssid)) {
            return 0;
        }
        int securityType = getSecurity(getScanResultByBssid(scanResults, bssid));
        for (ScanResult info : scanResults) {
            if (ssid.equals(info.SSID) && securityType == getSecurity(info)) {
                publicEssCnt++;
            }
        }
        return publicEssCnt;
    }

    private List<ScanResult> getScanResults() {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        if (scanResults == null || scanResults.size() <= 0) {
            return new ArrayList();
        }
        return scanResults;
    }

    private ScanResult getScanResultByBssid(List<ScanResult> scanResults, String bssid) {
        if (!TextUtils.isEmpty(bssid)) {
            for (ScanResult sr : scanResults) {
                if (bssid.equals(sr.BSSID)) {
                    return sr;
                }
            }
        }
        return new ScanResult();
    }

    private static int getSecurity(ScanResult result) {
        if (TextUtils.isEmpty(result.capabilities)) {
            return -1;
        }
        if (result.capabilities.contains("WEP")) {
            return 1;
        }
        if (result.capabilities.contains("WAPI-PSK")) {
            return 4;
        }
        if (result.capabilities.contains("WAPI-CERT")) {
            return 5;
        }
        if (result.capabilities.contains("PSK")) {
            return 2;
        }
        if (result.capabilities.contains("EAP")) {
            return 3;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSupplicantStateAction(Intent intent) {
        SupplicantState state = (SupplicantState) intent.getParcelableExtra("newState");
        if (state != null) {
            String ssid = "";
            int freq = 0;
            if (this.mWifiManager == null) {
                this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            }
            WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                ssid = wifiInfo.getSSID();
                freq = wifiInfo.getFrequency();
                if (state == SupplicantState.ASSOCIATING) {
                    freq = getScanResultByBssid(getScanResults(), wifiInfo.getBSSID()).frequency;
                }
            }
            uploadSupplicantState(state, ssid, freq);
        }
    }

    private void uploadSupplicantState(SupplicantState state, String ssid, int freq) {
        Message msg = this.mNCDFTExceptionHandler.obtainMessage(0, 1134, 0);
        Bundle data = new Bundle();
        data.putString(KEY_OF_SUPPLICANT_STATE, state.name());
        data.putString(KEY_OF_SSID, ssid);
        data.putInt(KEY_OF_FREQ, freq);
        msg.setData(data);
        msg.sendToTarget();
    }
}
