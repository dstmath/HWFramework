package com.huawei.hwwifiproservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WifiProChrUploadManager extends Handler {
    private static final String ACTIVE_DETEC_EVENT = "activeDetecEvent";
    private static final String AD_HAS_INTERNET = "hasInternet";
    private static final String AD_NO_INTERNET = "noInternet";
    private static final String AD_PORTAL = "portal";
    private static final String AP_EVALUATE_EVENT = "apEvaluateEvent";
    private static final String AP_EVAL_TRIG = "apEvaluateTrigCnt";
    private static final String AP_TYPE_COMMON = "CommonAp";
    private static final String AP_TYPE_EMPTY = "";
    private static final String AP_TYPE_PORTAL = "PortalAp";
    private static final String AUTO_OPEN_EVENT = "autoOpenEvent";
    private static final String AUTO_OPEN_FAIL = "autoOpenFailCnt";
    private static final String AUTO_OPEN_SUCC = "autoOpenSuccCnt";
    private static final String CHIP_CURE = "chipCureCnt";
    private static final String CHIP_CURE_SUCC = "chipCureSuccCnt";
    private static final String COMMON_AP = "commonAp";
    private static final long CONVERT_TO_SECONDS = 1000;
    private static final String DELAY_CONN = "delayConnCnt";
    private static final String DELAY_CONN_EVENT = "delayConnEvent";
    private static final String DELAY_DURATION = "delayDuration";
    private static final String DHCP_OFFER_CURE = "dhcpOfferCnt";
    private static final String DHCP_OFFER_CURE_SUCC = "dhcpOfferSuccCnt";
    private static final String DNS_CURE_RECOVERY_EVENT = "dnsCureRecoveryEvent";
    private static final String EVALUATE_CONN = "evaluateConnCnt";
    private static final int EVENT_ID_NOT_CONNECT = 1001;
    private static final int EVENT_ID_WEAK_SIGNAL = 1000;
    private static final int EVENT_UPLOAD_WIFIPRO_SSID_STAT = 1130;
    private static final int EVENT_UPLOAD_WIFIPRO_STAT = 1129;
    private static final String FORGET_AP = "forgetAp";
    private static final String INTERNET_AP = "internetAPCnt";
    private static final String INTERNET_ERROR = "internetError";
    private static final int MSG_DISCONNECT_EVENT = 1132;
    private static final int MSG_SEND_DELAY_DURATION = 1800000;
    private static final int MSG_SEND_DELAY_ID = 1000;
    private static final String MULTI_CURE_RECOVERY_EVENT = "multiCureRecoveryEvent";
    private static final String MULTI_DHCP_CURE = "multiDhcpCure";
    private static final String NETWORK_REJECTED = "rejected";
    private static final String NOT_CONN_EVENT = "notConnEvent";
    private static final String NO_INTERNET_AP = "noInternetAPCnt";
    private static final String NO_INTER_TO_CELL_EVENT = "noInterToCellEvent";
    private static final String OPEN_CLOSE_EVENT = "openCloseEvent";
    private static final String PING_PONG = "pingPong";
    private static final String PORTAL_AP = "portalAp";
    private static final String PORTAL_AP_CNT = "portalAPCnt";
    private static final String REPLACE_DNS = "replaceDnsCnt";
    private static final String REPLACE_DNS_SUCC = "replaceDnsSuccCnt";
    private static final String RE_ASSOCIATION = "reassocCnt";
    private static final String RE_ASSOCIATION_SUCC = "reassocSuccCnt";
    private static final String RE_DHCP_CURE = "reDhcpCnt";
    private static final String RE_DHCP_CURE_SUCC = "reDhcpSuccCnt";
    private static final String ROAM_CURE_RECOVERY_EVENT = "roamCureRecoveryEvent";
    private static final String SCAN_GENIE_ALL_CHANNEL_FAIL = "scanGenieFailCnt";
    private static final String SCAN_GENIE_EVENT = "scanGenieEvent";
    private static final String SCAN_GENIE_SUCCESS = "scanGenieSuccCnt";
    private static final String SCAN_GENIE_TOTAL = "scanGenieCnt";
    private static final String SELF_CURE_EVENT = "selfCureEvent";
    private static final String SELF_CURE_SUCC_EVENT = "selfCureSuccEvent";
    private static final String SLOW_INTER_TO_CELL_EVENT = "slowInterToCellEvent";
    private static final String STOP_USE = "stopUse";
    private static final String TAG = "WifiProChrUploadManager";
    private static final String TCP_CURE_RECOVERY_EVENT = "tcpCureRecoveryEvent";
    private static final String UNEXPECT_SWITCH_EVENT = "unExpectSwitchEvent";
    private static final String UNIQUE_CURE_EVENT = "uniqueCureEvent";
    private static final String USER_REJECT_SWITCH = "userRejectSwitch";
    private static final String USER_SELECT_OLD = "userSelectOld";
    private static final String USER_SELECT_OLD_EVENT = "UserSelectOld";
    private static final String WIFI_CLOSED = "closeWifi";
    private static final String WIFI_PRO_CLOSED = "closeWifiPro";
    private static final String WIFI_SWITCH_AP = "wifiSwitchCnt";
    private static final String WIFI_SWITCH_AP_SUCC = "wifiSwitchSuccCnt";
    private static final String WIFI_SWITCH_CNT_EVENT = "wifiSwitchCntEvent";
    private static final String WIFI_TO_CELL = "wifiToCellCnt";
    private static final String WIFI_TO_CELL_DURA = "wifiToCellDuation";
    private static final String WIFI_TO_CELL_FLOW = "wifiToCellFlow";
    private static final String WIFI_TO_CELL_SUCC = "wifiToCellSuccCnt";
    private static final String WRONG_PWD = "wrongPassword";
    private static WifiProChrUploadManager mUploadManager = null;
    private Map<String, WifiProChrSsidStatistics> chrSsidStatMap = new ConcurrentHashMap();
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext = null;
    private String mCurrentSsidAndFreq;
    private WifiProChrSsidStatistics mCurrentSsidStat;
    private WifiProChrStatistics mCurrentStat;
    private Handler mHandler;
    private boolean mInitialized = false;
    private IntentFilter mIntentFilter;
    private final Object mLock = new Object();
    private int mNetworkType;
    private Map<String, Long> mTimeRecordMap = new ConcurrentHashMap();
    private WifiManager mWifiManager;
    private Runnable runnable;

    private WifiProChrUploadManager(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mCurrentStat = WifiProChrStatistics.getInstance();
        this.mCurrentSsidStat = new WifiProChrSsidStatistics();
        sendMessage(Message.obtain(this, 1000));
        HwHiLog.d(TAG, false, "Constructor WifiProChrUploadManager finished, Message has been send", new Object[0]);
    }

    public synchronized void setup() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            HwHiLog.d(TAG, false, "setup DONE!", new Object[0]);
            registerSsidStatReceiver();
        }
    }

    public static synchronized WifiProChrUploadManager getInstance(Context context) {
        WifiProChrUploadManager wifiProChrUploadManager;
        synchronized (WifiProChrUploadManager.class) {
            if (mUploadManager == null) {
                mUploadManager = new WifiProChrUploadManager(context);
                HwHiLog.d(TAG, false, "createInstance upload Manager DONE!", new Object[0]);
            }
            wifiProChrUploadManager = mUploadManager;
        }
        return wifiProChrUploadManager;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void addChrCntStat(String event, String eventKey) {
        synchronized (this.mLock) {
            char c = 65535;
            switch (event.hashCode()) {
                case -590592021:
                    if (event.equals(SCAN_GENIE_EVENT)) {
                        c = 1;
                        break;
                    }
                    break;
                case -196446830:
                    if (event.equals("apEvaluateEvent")) {
                        c = 2;
                        break;
                    }
                    break;
                case 427749761:
                    if (event.equals(AUTO_OPEN_EVENT)) {
                        c = 3;
                        break;
                    }
                    break;
                case 1180421484:
                    if (event.equals(OPEN_CLOSE_EVENT)) {
                        c = 0;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                this.mCurrentStat.mWifiproOpenCloseCnt++;
            } else if (c == 1) {
                updateScanGenie(eventKey);
            } else if (c == 2) {
                updateApEval(eventKey);
            } else if (c != 3) {
                HwHiLog.w(TAG, false, "addChrStat has no event entered", new Object[0]);
            } else {
                updateAutoOpen(eventKey);
            }
        }
    }

    public void addChrBundleStat(String event, String eventKey, Bundle data) {
        synchronized (this.mLock) {
            char c = 65535;
            if (event.hashCode() == 1391268986 && event.equals("wifiSwitchCntEvent")) {
                c = 0;
            }
            if (c != 0) {
                HwHiLog.w(TAG, false, "addChrStat has no event entered", new Object[0]);
            } else {
                updateWifiSwitch(eventKey, data);
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void addChrSsidCntStat(String event, String eventKey) {
        synchronized (this.mLock) {
            char c = 65535;
            switch (event.hashCode()) {
                case -1362377521:
                    if (event.equals(ACTIVE_DETEC_EVENT)) {
                        c = 0;
                        break;
                    }
                    break;
                case -630812279:
                    if (event.equals(SELF_CURE_EVENT)) {
                        c = 2;
                        break;
                    }
                    break;
                case -503881413:
                    if (event.equals(NOT_CONN_EVENT)) {
                        c = 1;
                        break;
                    }
                    break;
                case -163054572:
                    if (event.equals("unExpectSwitchEvent")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1274092196:
                    if (event.equals(UNIQUE_CURE_EVENT)) {
                        c = 5;
                        break;
                    }
                    break;
                case 1865195751:
                    if (event.equals(SELF_CURE_SUCC_EVENT)) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                updateActiveDetect(eventKey);
            } else if (c == 1) {
                updateNotConn(eventKey);
            } else if (c == 2) {
                updateSelfCureEvent(eventKey);
            } else if (c == 3) {
                updateSelfCureSuccEvent(eventKey);
            } else if (c == 4) {
                updateUnexpSwitch(eventKey);
            } else if (c != 5) {
                HwHiLog.w(TAG, false, "addChrSsidStat has no event entered", new Object[0]);
            } else {
                updateUniqueCure(eventKey);
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void addChrSsidBundleStat(String event, String eventKey, Bundle data) {
        synchronized (this.mLock) {
            char c = 65535;
            switch (event.hashCode()) {
                case -1975768665:
                    if (event.equals(MULTI_CURE_RECOVERY_EVENT)) {
                        c = 6;
                        break;
                    }
                    break;
                case -1559716425:
                    if (event.equals(ROAM_CURE_RECOVERY_EVENT)) {
                        c = 5;
                        break;
                    }
                    break;
                case -942286709:
                    if (event.equals(DELAY_CONN_EVENT)) {
                        c = 0;
                        break;
                    }
                    break;
                case -594197630:
                    if (event.equals(NO_INTER_TO_CELL_EVENT)) {
                        c = 1;
                        break;
                    }
                    break;
                case -133521470:
                    if (event.equals(SLOW_INTER_TO_CELL_EVENT)) {
                        c = 2;
                        break;
                    }
                    break;
                case 537483191:
                    if (event.equals(DNS_CURE_RECOVERY_EVENT)) {
                        c = 3;
                        break;
                    }
                    break;
                case 1981735935:
                    if (event.equals(TCP_CURE_RECOVERY_EVENT)) {
                        c = 4;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    updateDelayConn(eventKey, data);
                    break;
                case 1:
                    updateNoInterSwitchEvent(eventKey, data);
                    break;
                case 2:
                    updateSlowInterSwitchEvent(eventKey, data);
                    break;
                case 3:
                case 4:
                case 5:
                case 6:
                    updateCureTypes(eventKey, data);
                    break;
                default:
                    HwHiLog.w(TAG, false, "addChrSsidBundleStat has no event entered", new Object[0]);
                    break;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0080  */
    private void updateActiveDetect(String eventKey) {
        char c;
        int hashCode = eventKey.hashCode();
        if (hashCode != -982480788) {
            if (hashCode != -850099205) {
                if (hashCode == 1729423394 && eventKey.equals(AD_NO_INTERNET)) {
                    c = 1;
                    if (c != 0) {
                        this.mCurrentSsidStat.mDetectApType.put("HasInternet", Integer.valueOf(this.mCurrentSsidStat.mDetectApType.get("HasInternet").intValue() + 1));
                        return;
                    } else if (c == 1) {
                        this.mCurrentSsidStat.mDetectApType.put("NoInternet", Integer.valueOf(this.mCurrentSsidStat.mDetectApType.get("NoInternet").intValue() + 1));
                        return;
                    } else if (c != 2) {
                        HwHiLog.w(TAG, false, "updateActiveDetect has no event entered", new Object[0]);
                        return;
                    } else {
                        this.mCurrentSsidStat.mDetectApType.put("Portal", Integer.valueOf(this.mCurrentSsidStat.mDetectApType.get("Portal").intValue() + 1));
                        return;
                    }
                }
            } else if (eventKey.equals(AD_HAS_INTERNET)) {
                c = 0;
                if (c != 0) {
                }
            }
        } else if (eventKey.equals(AD_PORTAL)) {
            c = 2;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    private void updateDelayConn(String eventKey, Bundle data) {
        if (data == null) {
            HwHiLog.e(TAG, false, "updateDelayConn, invalid parameter ", new Object[0]);
            return;
        }
        char c = 65535;
        int hashCode = eventKey.hashCode();
        if (hashCode != -1310478438) {
            if (hashCode == 906704951 && eventKey.equals(DELAY_DURATION)) {
                c = 1;
            }
        } else if (eventKey.equals(DELAY_CONN)) {
            c = 0;
        }
        if (c == 0) {
            this.mCurrentSsidStat.mDelayConnCnt++;
        } else if (c != 1) {
            HwHiLog.w(TAG, false, "updateDelayConn has no event entered", new Object[0]);
        } else {
            this.mCurrentSsidStat.mDelayDuration += data.getInt(DELAY_DURATION);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x004f  */
    private void updateNotConn(String eventKey) {
        char c;
        int hashCode = eventKey.hashCode();
        if (hashCode != -537475174) {
            if (hashCode == 793414107 && eventKey.equals(AP_TYPE_PORTAL)) {
                c = 1;
                if (c == 0) {
                    this.mCurrentSsidStat.mNotConnType.put(AP_TYPE_COMMON, Integer.valueOf(this.mCurrentSsidStat.mNotConnType.get(AP_TYPE_COMMON).intValue() + 1));
                    return;
                } else if (c != 1) {
                    HwHiLog.w(TAG, false, "updateNotConn has no event entered", new Object[0]);
                    return;
                } else {
                    this.mCurrentSsidStat.mNotConnType.put(AP_TYPE_PORTAL, Integer.valueOf(this.mCurrentSsidStat.mNotConnType.get(AP_TYPE_PORTAL).intValue() + 1));
                    return;
                }
            }
        } else if (eventKey.equals(AP_TYPE_COMMON)) {
            c = 0;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void updateSelfCureEvent(String eventKey) {
        char c;
        switch (eventKey.hashCode()) {
            case -1884344795:
                if (eventKey.equals(STOP_USE)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -962031768:
                if (eventKey.equals(WRONG_PWD)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -608496514:
                if (eventKey.equals(NETWORK_REJECTED)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1286699847:
                if (eventKey.equals(INTERNET_ERROR)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            this.mCurrentSsidStat.mSelfCureCnt.set(0, Integer.valueOf(this.mCurrentSsidStat.mSelfCureCnt.get(0).intValue() + 1));
        } else if (c == 1) {
            this.mCurrentSsidStat.mSelfCureCnt.set(1, Integer.valueOf(this.mCurrentSsidStat.mSelfCureCnt.get(1).intValue() + 1));
        } else if (c == 2) {
            this.mCurrentSsidStat.mSelfCureCnt.set(2, Integer.valueOf(this.mCurrentSsidStat.mSelfCureCnt.get(2).intValue() + 1));
        } else if (c != 3) {
            HwHiLog.w(TAG, false, "updateSelfCureEvent has no event entered", new Object[0]);
        } else {
            this.mCurrentSsidStat.mSelfCureCnt.set(3, Integer.valueOf(this.mCurrentSsidStat.mSelfCureCnt.get(3).intValue() + 1));
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void updateSelfCureSuccEvent(String eventKey) {
        char c;
        switch (eventKey.hashCode()) {
            case -1884344795:
                if (eventKey.equals(STOP_USE)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -962031768:
                if (eventKey.equals(WRONG_PWD)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -608496514:
                if (eventKey.equals(NETWORK_REJECTED)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1286699847:
                if (eventKey.equals(INTERNET_ERROR)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            this.mCurrentSsidStat.mSelfCureSuccCnt.set(0, Integer.valueOf(this.mCurrentSsidStat.mSelfCureSuccCnt.get(0).intValue() + 1));
        } else if (c == 1) {
            this.mCurrentSsidStat.mSelfCureSuccCnt.set(1, Integer.valueOf(this.mCurrentSsidStat.mSelfCureSuccCnt.get(1).intValue() + 1));
        } else if (c == 2) {
            this.mCurrentSsidStat.mSelfCureSuccCnt.set(2, Integer.valueOf(this.mCurrentSsidStat.mSelfCureSuccCnt.get(2).intValue() + 1));
        } else if (c != 3) {
            HwHiLog.w(TAG, false, "updateSelfCureSuccEvent has no event entered", new Object[0]);
        } else {
            this.mCurrentSsidStat.mSelfCureSuccCnt.set(3, Integer.valueOf(this.mCurrentSsidStat.mSelfCureSuccCnt.get(3).intValue() + 1));
        }
    }

    private void updateNoInterSwitchEvent(String eventKey, Bundle data) {
        if (data == null) {
            HwHiLog.e(TAG, false, "updateNoInterSwitchEvent,invalid parameter", new Object[0]);
            return;
        }
        char c = 65535;
        switch (eventKey.hashCode()) {
            case -1589561678:
                if (eventKey.equals(WIFI_TO_CELL_DURA)) {
                    c = 2;
                    break;
                }
                break;
            case -1160604299:
                if (eventKey.equals(WIFI_TO_CELL_SUCC)) {
                    c = 1;
                    break;
                }
                break;
            case -942055913:
                if (eventKey.equals(WIFI_TO_CELL)) {
                    c = 0;
                    break;
                }
                break;
            case 861125184:
                if (eventKey.equals(WIFI_TO_CELL_FLOW)) {
                    c = 3;
                    break;
                }
                break;
        }
        if (c == 0) {
            this.mCurrentSsidStat.mWifiToCellCnt.set(0, Integer.valueOf(this.mCurrentSsidStat.mWifiToCellCnt.get(0).intValue() + 1));
        } else if (c == 1) {
            this.mCurrentSsidStat.mWifiToCellSuccCnt.set(0, Integer.valueOf(this.mCurrentSsidStat.mWifiToCellSuccCnt.get(0).intValue() + 1));
        } else if (c == 2) {
            this.mCurrentSsidStat.mWifiToCellDuation.set(0, Integer.valueOf(this.mCurrentSsidStat.mWifiToCellDuation.get(0).intValue() + data.getInt("duration")));
        } else if (c != 3) {
            HwHiLog.w(TAG, false, "updateNoInterSwitchEvent has no event entered", new Object[0]);
        } else {
            this.mCurrentSsidStat.mWifiToCellFlow.set(0, Integer.valueOf(this.mCurrentSsidStat.mWifiToCellFlow.get(0).intValue() + data.getInt("flow")));
        }
    }

    private void updateSlowInterSwitchEvent(String eventKey, Bundle data) {
        if (data == null) {
            HwHiLog.e(TAG, false, "updateSlowInterSwitchEvent,invalid parameter", new Object[0]);
            return;
        }
        char c = 65535;
        switch (eventKey.hashCode()) {
            case -1589561678:
                if (eventKey.equals(WIFI_TO_CELL_DURA)) {
                    c = 2;
                    break;
                }
                break;
            case -1160604299:
                if (eventKey.equals(WIFI_TO_CELL_SUCC)) {
                    c = 1;
                    break;
                }
                break;
            case -942055913:
                if (eventKey.equals(WIFI_TO_CELL)) {
                    c = 0;
                    break;
                }
                break;
            case 861125184:
                if (eventKey.equals(WIFI_TO_CELL_FLOW)) {
                    c = 3;
                    break;
                }
                break;
        }
        if (c == 0) {
            this.mCurrentSsidStat.mWifiToCellCnt.set(1, Integer.valueOf(this.mCurrentSsidStat.mWifiToCellCnt.get(1).intValue() + 1));
        } else if (c == 1) {
            this.mCurrentSsidStat.mWifiToCellSuccCnt.set(1, Integer.valueOf(this.mCurrentSsidStat.mWifiToCellSuccCnt.get(1).intValue() + 1));
        } else if (c == 2) {
            this.mCurrentSsidStat.mWifiToCellDuation.set(1, Integer.valueOf(this.mCurrentSsidStat.mWifiToCellDuation.get(1).intValue() + data.getInt("duration")));
        } else if (c != 3) {
            HwHiLog.w(TAG, false, "updateSlowInterSwitchEvent has no event entered", new Object[0]);
        } else {
            this.mCurrentSsidStat.mWifiToCellFlow.set(1, Integer.valueOf(this.mCurrentSsidStat.mWifiToCellFlow.get(1).intValue() + data.getInt("flow")));
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void updateUnexpSwitch(String eventKey) {
        char c;
        switch (eventKey.hashCode()) {
            case -482392787:
                if (eventKey.equals(WIFI_CLOSED)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -428309366:
                if (eventKey.equals(PING_PONG)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -2864576:
                if (eventKey.equals(WIFI_PRO_CLOSED)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 248010752:
                if (eventKey.equals(USER_SELECT_OLD)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 468656892:
                if (eventKey.equals(FORGET_AP)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1533446942:
                if (eventKey.equals("userRejectSwitch")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            this.mCurrentSsidStat.mUnexpSwitchType.put("CloseWifi", Integer.valueOf(this.mCurrentSsidStat.mUnexpSwitchType.get("CloseWifi").intValue() + 1));
        } else if (c == 1) {
            this.mCurrentSsidStat.mUnexpSwitchType.put("CloseWifiPro", Integer.valueOf(this.mCurrentSsidStat.mUnexpSwitchType.get("CloseWifiPro").intValue() + 1));
        } else if (c == 2) {
            this.mCurrentSsidStat.mUnexpSwitchType.put(USER_SELECT_OLD_EVENT, Integer.valueOf(this.mCurrentSsidStat.mUnexpSwitchType.get(USER_SELECT_OLD_EVENT).intValue() + 1));
        } else if (c == 3) {
            this.mCurrentSsidStat.mUnexpSwitchType.put("ForgetAp", Integer.valueOf(this.mCurrentSsidStat.mUnexpSwitchType.get("ForgetAp").intValue() + 1));
        } else if (c == 4) {
            this.mCurrentSsidStat.mUnexpSwitchType.put("PingPong", Integer.valueOf(this.mCurrentSsidStat.mUnexpSwitchType.get("PingPong").intValue() + 1));
        } else if (c != 5) {
            HwHiLog.w(TAG, false, "updateUnexpSwitch has no event entered", new Object[0]);
        } else {
            this.mCurrentSsidStat.mUnexpSwitchType.put("UserRejectSwitch", Integer.valueOf(this.mCurrentSsidStat.mUnexpSwitchType.get("UserRejectSwitch").intValue() + 1));
        }
    }

    private void updateCureTypes(String eventKey, Bundle data) {
        if (data == null) {
            HwHiLog.e(TAG, false, "updateCureTypes,invalid parameter", new Object[0]);
            return;
        }
        int selfCureType = data.getInt("selfCureType");
        char c = 65535;
        switch (eventKey.hashCode()) {
            case -1431344002:
                if (eventKey.equals(DHCP_OFFER_CURE)) {
                    c = 0;
                    break;
                }
                break;
            case -1153815784:
                if (eventKey.equals(CHIP_CURE)) {
                    c = 2;
                    break;
                }
                break;
            case 228399196:
                if (eventKey.equals(DHCP_OFFER_CURE_SUCC)) {
                    c = 1;
                    break;
                }
                break;
            case 1192425974:
                if (eventKey.equals(CHIP_CURE_SUCC)) {
                    c = 3;
                    break;
                }
                break;
        }
        if (c != 0) {
            if (c != 1) {
                if (c != 2) {
                    if (c != 3) {
                        HwHiLog.w(TAG, false, "updateCureTypes has no event entered", new Object[0]);
                    } else if (selfCureType < 0 || selfCureType >= this.mCurrentSsidStat.mChipCureSuccCnt.size()) {
                        HwHiLog.e(TAG, false, "invalid index,eventKey = %{public}s", new Object[]{eventKey});
                    } else {
                        this.mCurrentSsidStat.mChipCureSuccCnt.set(selfCureType, Integer.valueOf(this.mCurrentSsidStat.mChipCureSuccCnt.get(selfCureType).intValue() + 1));
                    }
                } else if (selfCureType < 0 || selfCureType >= this.mCurrentSsidStat.mChipCureCnt.size()) {
                    HwHiLog.e(TAG, false, "invalid index,eventKey = %{public}s", new Object[]{eventKey});
                } else {
                    this.mCurrentSsidStat.mChipCureCnt.set(selfCureType, Integer.valueOf(this.mCurrentSsidStat.mChipCureCnt.get(selfCureType).intValue() + 1));
                }
            } else if (selfCureType < 0 || selfCureType >= this.mCurrentSsidStat.mDhcpOfferSuccCnt.size()) {
                HwHiLog.e(TAG, false, "invalid index,eventKey = %{public}s", new Object[]{eventKey});
            } else {
                this.mCurrentSsidStat.mDhcpOfferSuccCnt.set(selfCureType, Integer.valueOf(this.mCurrentSsidStat.mDhcpOfferSuccCnt.get(selfCureType).intValue() + 1));
            }
        } else if (selfCureType < 0 || selfCureType >= this.mCurrentSsidStat.mDhcpOfferCnt.size()) {
            HwHiLog.e(TAG, false, "invalid index,eventKey = %{public}s", new Object[]{eventKey});
        } else {
            this.mCurrentSsidStat.mDhcpOfferCnt.set(selfCureType, Integer.valueOf(this.mCurrentSsidStat.mDhcpOfferCnt.get(selfCureType).intValue() + 1));
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void updateUniqueCure(String eventKey) {
        char c;
        switch (eventKey.hashCode()) {
            case -1574112347:
                if (eventKey.equals(RE_ASSOCIATION_SUCC)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -963188219:
                if (eventKey.equals(RE_DHCP_CURE)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -957580701:
                if (eventKey.equals(RE_DHCP_CURE_SUCC)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -766369742:
                if (eventKey.equals(REPLACE_DNS_SUCC)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 631099220:
                if (eventKey.equals(REPLACE_DNS)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 677433871:
                if (eventKey.equals(MULTI_DHCP_CURE)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 838489159:
                if (eventKey.equals(RE_ASSOCIATION)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                this.mCurrentSsidStat.mReplaceDnsCnt++;
                return;
            case 1:
                this.mCurrentSsidStat.mReplaceDnsSuccCnt++;
                return;
            case 2:
                this.mCurrentSsidStat.mReassocCnt++;
                return;
            case 3:
                this.mCurrentSsidStat.mReassocSuccCnt++;
                return;
            case 4:
                this.mCurrentSsidStat.mReDhcpCnt++;
                return;
            case 5:
                this.mCurrentSsidStat.mReDhcpSuccCnt++;
                return;
            case 6:
                this.mCurrentSsidStat.mMultiDhcpCure++;
                return;
            default:
                HwHiLog.w(TAG, false, "updateUniqueCure has no event entered", new Object[0]);
                return;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0056  */
    private void updateScanGenie(String eventKey) {
        char c;
        int hashCode = eventKey.hashCode();
        if (hashCode != -1113946920) {
            if (hashCode != -1100056838) {
                if (hashCode == -333366660 && eventKey.equals(SCAN_GENIE_ALL_CHANNEL_FAIL)) {
                    c = 2;
                    if (c != 0) {
                        this.mCurrentStat.mScanGenieCnt++;
                        return;
                    } else if (c == 1) {
                        this.mCurrentStat.mScanGenieSuccCnt++;
                        return;
                    } else if (c != 2) {
                        HwHiLog.w(TAG, false, "updateScanGenie has no event entered", new Object[0]);
                        return;
                    } else {
                        this.mCurrentStat.mScanGenieFailCnt++;
                        return;
                    }
                }
            } else if (eventKey.equals(SCAN_GENIE_TOTAL)) {
                c = 0;
                if (c != 0) {
                }
            }
        } else if (eventKey.equals(SCAN_GENIE_SUCCESS)) {
            c = 1;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void updateApEval(String eventKey) {
        char c;
        switch (eventKey.hashCode()) {
            case -1853737020:
                if (eventKey.equals(EVALUATE_CONN)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -274122290:
                if (eventKey.equals("portalAPCnt")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 125451224:
                if (eventKey.equals("noInternetAPCnt")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 509736101:
                if (eventKey.equals("apEvaluateTrigCnt")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1281947673:
                if (eventKey.equals("internetAPCnt")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            this.mCurrentStat.mApEvaluateTrigCnt++;
        } else if (c == 1) {
            this.mCurrentStat.mInternetAPCnt++;
        } else if (c == 2) {
            this.mCurrentStat.mNoInternetAPCnt++;
        } else if (c == 3) {
            this.mCurrentStat.mPortalAPCnt++;
        } else if (c != 4) {
            HwHiLog.w(TAG, false, "updateApEval has no event entered", new Object[0]);
        } else {
            this.mCurrentStat.mEvaluateConnCnt++;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x003c  */
    private void updateAutoOpen(String eventKey) {
        char c;
        int hashCode = eventKey.hashCode();
        if (hashCode != -1740037906) {
            if (hashCode == -959457646 && eventKey.equals(AUTO_OPEN_FAIL)) {
                c = 1;
                if (c == 0) {
                    this.mCurrentStat.mAutoOpenSuccCnt++;
                    return;
                } else if (c != 1) {
                    HwHiLog.w(TAG, false, "updateAutoOpen has no event entered", new Object[0]);
                    return;
                } else {
                    this.mCurrentStat.mAutoOpenFailCnt++;
                    return;
                }
            }
        } else if (eventKey.equals(AUTO_OPEN_SUCC)) {
            c = 0;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }

    private void updateWifiSwitch(String eventKey, Bundle data) {
        if (data == null) {
            HwHiLog.e(TAG, false, "updateWifiSwitch,invalid parameter", new Object[0]);
            return;
        }
        char c = 65535;
        int hashCode = eventKey.hashCode();
        if (hashCode != -1641002786) {
            if (hashCode == 1709387264 && eventKey.equals("wifiSwitchCnt")) {
                c = 0;
            }
        } else if (eventKey.equals("wifiSwitchSuccCnt")) {
            c = 1;
        }
        if (c == 0) {
            int index = data.getInt(WifiproUtils.SWITCH_SUCCESS_INDEX);
            if (index < 0 || index >= this.mCurrentStat.mWifiSwitchCnt.size()) {
                HwHiLog.e(TAG, false, "invalid index, eventKey = %{public}s", new Object[]{eventKey});
            } else {
                this.mCurrentStat.mWifiSwitchCnt.set(index, Integer.valueOf(this.mCurrentStat.mWifiSwitchCnt.get(index).intValue() + 1));
            }
        } else if (c != 1) {
            HwHiLog.w(TAG, false, "updateAutoOpen has no event entered", new Object[0]);
        } else {
            int index2 = data.getInt(WifiproUtils.SWITCH_SUCCESS_INDEX);
            if (index2 < 0 || index2 >= this.mCurrentStat.mWifiSwitchSuccCnt.size()) {
                HwHiLog.e(TAG, false, "invalid index, eventKey = %{public}s", new Object[]{eventKey});
            } else {
                this.mCurrentStat.mWifiSwitchSuccCnt.set(index2, Integer.valueOf(this.mCurrentStat.mWifiSwitchSuccCnt.get(index2).intValue() + 1));
            }
        }
    }

    private boolean checkSsidExist(String currentSsid) {
        for (String key : this.chrSsidStatMap.keySet()) {
            if (currentSsid.equals(key)) {
                return true;
            }
        }
        return false;
    }

    private void updateSsidStat(String currentSsid) {
        if (this.chrSsidStatMap != null) {
            HwHiLog.d(TAG, false, "updateSsidStat enter", new Object[0]);
            if (!this.chrSsidStatMap.containsKey(currentSsid)) {
                this.chrSsidStatMap.put(currentSsid, new WifiProChrSsidStatistics());
                this.mCurrentSsidStat = this.chrSsidStatMap.get(currentSsid);
            } else {
                this.mCurrentSsidStat = this.chrSsidStatMap.get(currentSsid);
            }
            updateApType();
        }
    }

    private synchronized void uploadChrStat() {
        if (this.mCurrentStat != null) {
            Bundle chrStat = new Bundle();
            Bundle statRecord = this.mCurrentStat.getStatBundle();
            chrStat.putInt("eventId", EVENT_UPLOAD_WIFIPRO_STAT);
            chrStat.putBundle("eventData", statRecord);
            WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 2, chrStat);
            this.mCurrentStat.resetStatistics();
        }
    }

    private synchronized void uploadChrSsidStat() {
        if (this.chrSsidStatMap != null) {
            if (!this.chrSsidStatMap.isEmpty()) {
                Bundle chrSsidStat = new Bundle();
                Bundle ssidStatRecord = new Bundle();
                for (Map.Entry<String, WifiProChrSsidStatistics> entry : this.chrSsidStatMap.entrySet()) {
                    ssidStatRecord.putBundle(entry.getKey(), entry.getValue().getSsidStatBundle());
                }
                chrSsidStat.putInt("eventId", EVENT_UPLOAD_WIFIPRO_SSID_STAT);
                chrSsidStat.putBundle("eventData", ssidStatRecord);
                WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 2, chrSsidStat);
                Iterator<Map.Entry<String, WifiProChrSsidStatistics>> iterator = this.chrSsidStatMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, WifiProChrSsidStatistics> entry2 = iterator.next();
                    if (this.mCurrentSsidAndFreq == null || !this.mCurrentSsidAndFreq.equals(entry2.getKey()) || !(entry2.getValue() instanceof WifiProChrSsidStatistics)) {
                        iterator.remove();
                    } else {
                        entry2.getValue().resetSsidStatistics();
                    }
                }
                return;
            }
        }
        HwHiLog.e(TAG, false, "uploadChrSsidStat: chrSsidStatMap is null or empty", new Object[0]);
    }

    public void updateFwkEvent(int eventId, String apType, String ssid, int freq) {
        if (this.mCurrentSsidStat != null && apType != null && ssid != null) {
            this.mCurrentSsidAndFreq = ssid.concat(ScanResult.is24GHz(freq) ? "@24G" : "@5G");
            updateSsidStat(this.mCurrentSsidAndFreq);
            if (AP_TYPE_EMPTY.equals(this.mCurrentSsidStat.getApType())) {
                this.mCurrentSsidStat.setApType(apType);
            }
            if (eventId == 1000) {
                HwHiLog.d(TAG, false, "updateFwkEvent enter EVENT_ID_WEAK_SIGNAL", new Object[0]);
                long now = SystemClock.elapsedRealtime();
                this.mCurrentSsidStat.mDelayConnCnt++;
                if (this.mTimeRecordMap == null) {
                    this.mTimeRecordMap = new ConcurrentHashMap();
                }
                this.mTimeRecordMap.put(ssid, Long.valueOf(now));
            } else if (eventId == EVENT_ID_NOT_CONNECT) {
                HwHiLog.d(TAG, false, "updateFwkEvent enter EVENT_ID_NOT_CONNECT", new Object[0]);
                updateNotConn(apType);
            } else {
                HwHiLog.w(TAG, false, "Unknown event from framework", new Object[0]);
            }
        }
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        if (msg != null && msg.what == 1000) {
            HwWifiProPartManager wifiProPartManager = HwWifiProPartManager.getInstance();
            if (!(wifiProPartManager == null || this.mCurrentStat == null)) {
                int totalAutoOpenCnt = wifiProPartManager.getAutoOpenCnt();
                WifiProChrStatistics wifiProChrStatistics = this.mCurrentStat;
                wifiProChrStatistics.mAutoOpenFailCnt = totalAutoOpenCnt - wifiProChrStatistics.mAutoOpenSuccCnt;
            }
            uploadChrStat();
            uploadChrSsidStat();
            if (wifiProPartManager != null) {
                wifiProPartManager.setAutoOpenCnt(0);
            }
            sendEmptyMessageDelayed(1000, 1800000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCurrentSsidStat(Intent intent) {
        WifiInfo connInfo;
        if (intent != null && intent.getParcelableExtra("networkInfo") != null) {
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            if (networkInfo == null) {
                HwHiLog.e(TAG, false, "NetworkInfo or WifiConfiguration is null", new Object[0]);
            } else if (WifiProCommonUtils.isWifiConnectedOrConnecting(this.mWifiManager) && networkInfo.isConnected() && (connInfo = this.mWifiManager.getConnectionInfo()) != null) {
                String currentSsid = WifiInfo.removeDoubleQuotes(connInfo.getSSID());
                String freqStr = ScanResult.is24GHz(connInfo.getFrequency()) ? "@24G" : "@5G";
                HwHiLog.d(TAG, false, "currentSsid = %{public}s, currentFreq = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(currentSsid), freqStr});
                this.mCurrentSsidAndFreq = currentSsid.concat(freqStr);
                updateDelayDuration();
                updateSsidStat(this.mCurrentSsidAndFreq);
            }
        }
    }

    private void updateDelayDuration() {
        Map<String, Long> map = this.mTimeRecordMap;
        if (!(map == null || this.mCurrentSsidStat == null)) {
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    long now = SystemClock.elapsedRealtime();
                    updateSsidStat(entry.getKey());
                    this.mCurrentSsidStat.mDelayDuration = (int) ((now - entry.getValue().longValue()) / CONVERT_TO_SECONDS);
                } else {
                    return;
                }
            }
            this.mTimeRecordMap.clear();
        }
    }

    private void updateApType() {
        WifiProChrSsidStatistics wifiProChrSsidStatistics;
        WifiConfiguration wifiConfig = WifiproUtils.getCurrentWifiConfig(this.mWifiManager);
        if (wifiConfig != null && (wifiProChrSsidStatistics = this.mCurrentSsidStat) != null && AP_TYPE_EMPTY.equals(wifiProChrSsidStatistics.getApType())) {
            this.mCurrentSsidStat.setApType(wifiConfig.portalNetwork ? AP_TYPE_PORTAL : AP_TYPE_COMMON);
        }
    }

    private void registerSsidStatReceiver() {
        HwHiLog.d(TAG, false, "registerSsidStatReceiver: enter", new Object[0]);
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.huawei.hwwifiproservice.WifiProChrUploadManager.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                HwHiLog.d(WifiProChrUploadManager.TAG, false, "registerSsidStatReceiver: onReceive: enter", new Object[0]);
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    WifiProChrUploadManager.this.mNetworkType = intent.getIntExtra("networkType", 1);
                    WifiProChrUploadManager.this.setCurrentSsidStat(intent);
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
    }

    public static void uploadDisconnectedEvent(String eventInfo) {
        Bundle data = new Bundle();
        data.putString("EVENT", eventInfo);
        data.putLong("TIME", SystemClock.elapsedRealtime());
        Bundle dftEventData = new Bundle();
        dftEventData.putInt("eventId", MSG_DISCONNECT_EVENT);
        dftEventData.putBundle("eventData", data);
        WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 2, dftEventData);
    }
}
