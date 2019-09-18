package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.LocalServices;
import com.android.server.policy.AbsPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.wifipro.HwAutoConnectManager;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import com.android.server.wifipro.WifiProCommonUtils;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwWifiConnectivityMonitor extends StateMachine {
    public static final String ACTION_11v_ROAMING_NETWORK_FOUND = "com.huawei.wifi.action.11v_ROAMING_NETWORK_FOUND";
    private static final int BAD_AVE_RTT = 800;
    private static final int CIRCLE_MAX_SIZE = 5;
    private static final int CMD_11v_ROAMING_PENALIZE_TIMEOUT = 117;
    private static final int CMD_11v_ROAMING_TIMEOUT = 108;
    private static final int CMD_BG_WIFI_LINK_STATUS = 113;
    private static final int CMD_DISCONNECT_POOR_LINK = 105;
    private static final int CMD_FOREGROUND_APP_CHANGED = 116;
    private static final int CMD_LEAVE_POOR_WIFI_LINK = 110;
    private static final int CMD_NETWORK_CONNECTED_RCVD = 101;
    private static final int CMD_NETWORK_DISCONNECTED_RCVD = 102;
    private static final int CMD_NEW_RSSI_RCVD = 104;
    private static final int CMD_QUERY_11v_ROAMING_NETWORK = 103;
    private static final int CMD_REQUEST_ROAMING_NETWORK = 109;
    private static final int CMD_ROAMING_COMPLETED_RCVD = 107;
    private static final int CMD_ROAMING_STARTED_RCVD = 106;
    private static final int CMD_TOP_UID_INTERNET_STATUS = 112;
    private static final int CMD_USER_MOVE_DETECTED = 111;
    private static final int CMD_VERIFY_WIFI_LINK_STATE = 114;
    private static final int CURR_UID_INTERNET_BAD = 1;
    private static final int CURR_UID_INTERNET_GOOD = 0;
    private static final int CURR_UID_INTERNET_VERY_BAD = 2;
    /* access modifiers changed from: private */
    public static final int[] DELAYED_MS_TABLE = {2000, 4000, 10000, 30000, 0};
    private static final int GOOD_LINK_MONITOR_MS = 8000;
    private static final float LESS_PKTS_BAD_RATE = 0.3f;
    private static final float LESS_PKTS_VERY_BAD_RATE = 0.4f;
    private static final int MIN_RX_PKTS = 100;
    private static final int MIN_TX_PKTS = 3;
    private static final float MORE_PKTS_BAD_RATE = 0.2f;
    private static final float MORE_PKTS_VERY_BAD_RATE = 0.3f;
    private static final int MORE_TX_PKTS = 20;
    private static final int POOR_LINK_MONITOR_MS = 4000;
    private static final String PROP_DISABLE_AUTO_DISC = "hw.wifi.disable_auto_disc";
    private static final int QUERY_11v_ROAMING_NETWORK_DELAYED_MS = 5000;
    private static final int QUERY_REASON_LOW_RSSI = 16;
    private static final int QUERY_REASON_PREFERRED_BSS = 19;
    private static final int REASON_FULL_SCREEN = 203;
    private static final int REASON_LANDSCAPE_MODE = 204;
    private static final int REASON_MOBILE_DATA_INACTIVE = 201;
    private static final int REASON_SCREEN_OFF = 202;
    private static final int REASON_SIGNAL_LEVEL_1_NON_WPA_ACCESS_INTERNET = 107;
    private static final int REASON_SIGNAL_LEVEL_1_TOP_UID_BAD = 101;
    private static final int REASON_SIGNAL_LEVEL_1_URGENT_MINI_APP = 108;
    private static final int REASON_SIGNAL_LEVEL_1_USER_MOVE_AND_ACCESS_INTERNET = 108;
    private static final int REASON_SIGNAL_LEVEL_2_NON_WPA_NORMAL_APP = 105;
    private static final int REASON_SIGNAL_LEVEL_2_NON_WPA_URGENT_APP = 104;
    private static final int REASON_SIGNAL_LEVEL_2_URGENT_MINI_APP = 102;
    private static final int REASON_SIGNAL_LEVEL_2_WPA = 103;
    private static final int REASON_SIGNAL_LEVEL_3_PORTAL = 106;
    private static final int REASON_SWITCH_BACK_TO_WIFI = 100;
    private static final int ROAMING_11v_NETWORK_TIMEOUT_MS = 8000;
    private static final int ROAMING_PENALIZE_TIMEOUT_MS = 1800000;
    private static final int SIGNAL_LEVEL_0 = 0;
    private static final int SIGNAL_LEVEL_1 = 1;
    private static final int SIGNAL_LEVEL_2 = 2;
    private static final int SIGNAL_LEVEL_3 = 3;
    private static final int SIGNAL_LEVEL_4 = 4;
    private static final int STEP_INCREASE_THRESHOLD = 8;
    private static final String TAG = "HwWifiConnectivityMonitor";
    private static final float TX_GOOD_RATE = 0.3f;
    /* access modifiers changed from: private */
    public static final String[] URGENT_APP_PKT_NAME = {"com.android.browser", HwPortalExceptionManager.BROWSER_PACKET_NAME, "com.UCMobile", "com.tencent.mtt", "com.netease.newsreader.activity", "com.ss.android.article.news", "com.sina.news", "com.tencent.news", "com.sohu.newsclient", "com.ifeng.news2", "com.android.chrome", "com.myzaker.ZAKER_Phone", "com.sina.weibo", "com.hexin.plat.android", "com.android.email", "com.google.android.gm"};
    /* access modifiers changed from: private */
    public static final String[] URGENT_MINI_APP_PKT_NAME = {"com.tencent.mm", "com.tencent.mobileqq", "com.eg.android.AlipayGphone", "com.sdu.didi.psnger", "com.didi.es.psngr", "com.meituan.qcs.c.android", "com.didapinche.booking", "com.jingyao.easybike", "cn.caocaokeji.user", "com.szzc.ucar.pilot", "com.ichinait.gbpassenger", "com.mobike.mobikeapp", "so.ofo.labofo", "com.baidu.BaiduMap", "com.autonavi.minimap", "com.google.android.apps.maps", "com.huawei.health", "com.huawei.espacev2", "com.baidu.searchbox", "com.whatsapp", "com.facebook.katana", "com.ichinait.gbpassenger", "com.huawei.works", "huawei.w3", "com.ss.android.ugc.aweme", "com.ss.android.ugc.live", "com.smile.gifmaker"};
    private static final int VERY_BAD_AVE_RTT = 1200;
    private static HwWifiConnectivityMonitor mWifiConnectivityMonitor = null;
    private List<String> circleStat = new ArrayList();
    /* access modifiers changed from: private */
    public int m11vRoamingDisconectedCounter = 0;
    private AtomicBoolean mAccSensorRegistered = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public Object mCircleStatLock = new Object();
    /* access modifiers changed from: private */
    public State mConnectedMonitorState = new ConnectedMonitorState();
    /* access modifiers changed from: private */
    public Context mContext;
    private State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public State mDisconnectedMonitorState = new DisconnectedMonitorState();
    private boolean mInitialized = false;
    /* access modifiers changed from: private */
    public int mNotifyWifiLinkPoorReason = -1;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    private final StepSensorEventListener mSensorEventListener = new StepSensorEventListener();
    private SensorManager mSensorManager;
    private Sensor mStepCntSensor;
    private int mStopWifiSwitchReason = -1;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    /* access modifiers changed from: private */
    public WifiNative mWifiNative;

    class ConnectedMonitorState extends State {
        private boolean m11vBssidSupported = false;
        private int m11vRoamingFailedCounter;
        private boolean m11vRoamingOnGoing;
        private WifiConfiguration mConnectedConfig = null;
        private int mCurrMonitorTopUid = -1;
        private int mCurrRssiVal;
        private int mCurrTopUidBadCnt = 0;
        private int mCurrTopUidVeryBadCnt = 0;
        private boolean mEnterVerifyLinkState = false;
        private long mLast11vRoamingFailedTs;
        private int mLastSignalLevel;
        private int mPoorLinkRssi = WifiHandover.INVALID_RSSI;
        private boolean mRoamingOnGoing;
        private int mRssiBeforeSwitchWifi = WifiHandover.INVALID_RSSI;
        private int mRssiGoodCnt = 0;
        private int mStrongRssiCnt = 0;

        ConnectedMonitorState() {
        }

        public void enter() {
            HwWifiConnectivityMonitor.this.LOGD("###ConnectedMonitorState, enter()");
            this.mRoamingOnGoing = false;
            this.m11vRoamingOnGoing = false;
            this.m11vRoamingFailedCounter = 0;
            this.mLast11vRoamingFailedTs = 0;
            this.mConnectedConfig = WifiProCommonUtils.getCurrentWifiConfig(HwWifiConnectivityMonitor.this.mWifiManager);
            this.mEnterVerifyLinkState = false;
            this.mRssiGoodCnt = 0;
            this.mStrongRssiCnt = 0;
            this.mRssiBeforeSwitchWifi = WifiHandover.INVALID_RSSI;
            this.mPoorLinkRssi = WifiHandover.INVALID_RSSI;
            this.mCurrTopUidBadCnt = 0;
            this.mCurrTopUidVeryBadCnt = 0;
            this.mCurrMonitorTopUid = -1;
            WifiInfo wifiInfo = HwWifiConnectivityMonitor.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mLastSignalLevel = WifiProCommonUtils.getCurrenSignalLevel(wifiInfo);
                this.mCurrRssiVal = wifiInfo.getRssi();
                this.m11vBssidSupported = is11vNetworkConnected();
                HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
                hwWifiConnectivityMonitor.LOGD("ConnectedMonitorState, network = " + wifiInfo.getSSID() + ", 802.11v = " + this.m11vBssidSupported + ", 2.4GHz = " + wifiInfo.is24GHz() + ", current level = " + this.mLastSignalLevel);
                if (!this.m11vBssidSupported) {
                    return;
                }
                if (wifiInfo.is24GHz() || this.mLastSignalLevel <= 2) {
                    HwWifiConnectivityMonitor.this.sendMessageDelayed(109, 5000);
                }
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 102:
                    HwWifiConnectivityMonitor.this.removeMessages(103);
                    if (HwWifiConnectivityMonitor.this.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DISABLE)) {
                        HwWifiConnectivityMonitor.this.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DISABLE);
                        HwWifiConnectivityMonitor.access$012(HwWifiConnectivityMonitor.this, 1);
                        HwWifiConnectivityMonitor.this.LOGD("Has messages CMD_11v_ROAMING_TIMEOUT, m11vRoamingDisconectedCounter = " + HwWifiConnectivityMonitor.this.m11vRoamingDisconectedCounter);
                        if (HwWifiConnectivityMonitor.this.m11vRoamingDisconectedCounter >= 2) {
                            HwWifiConnectivityMonitor.this.sendMessageDelayed(117, 1800000);
                        }
                    }
                    HwWifiConnectivityMonitor.this.removeMessages(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO);
                    HwWifiConnectivityMonitor.this.removeMessages(110);
                    HwWifiConnectivityMonitor.this.transitionTo(HwWifiConnectivityMonitor.this.mDisconnectedMonitorState);
                    break;
                case 103:
                    if (this.m11vBssidSupported && this.m11vRoamingFailedCounter <= 1 && HwWifiConnectivityMonitor.this.m11vRoamingDisconectedCounter <= 1) {
                        query11vRoamingNetowrk(16);
                        break;
                    }
                case 104:
                    handleNewRssiRcvd(message.arg1);
                    break;
                case HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO:
                    disconnectPoorWifiConnection();
                    break;
                case HwQoEUtils.QOE_MSG_EVALUATE_OTA_INFO:
                    if (HwWifiConnectivityMonitor.this.hasMessages(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO)) {
                        HwWifiConnectivityMonitor.this.LOGD("CMD_DISCONNECT_POOR_LINK remove due to roaming received.");
                        HwWifiConnectivityMonitor.this.removeMessages(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO);
                    }
                    this.mRoamingOnGoing = true;
                    break;
                case HwQoEUtils.QOE_MSG_WIFI_ENABLED:
                    if (HwWifiConnectivityMonitor.this.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DISABLE)) {
                        HwWifiConnectivityMonitor.this.LOGD("CMD_11v_ROAMING_TIMEOUT remove due to roaming completed received.");
                        HwWifiConnectivityMonitor.this.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DISABLE);
                    }
                    if (this.mLastSignalLevel >= 3) {
                        HwWifiConnectivityMonitor.this.removeMessages(103);
                    }
                    this.m11vBssidSupported = is11vNetworkConnected();
                    this.mRoamingOnGoing = false;
                    this.m11vRoamingOnGoing = false;
                    this.m11vRoamingFailedCounter = 0;
                    this.mLast11vRoamingFailedTs = 0;
                    break;
                case HwQoEUtils.QOE_MSG_WIFI_DISABLE:
                    if (HwWifiConnectivityMonitor.this.hasMessages(103)) {
                        HwWifiConnectivityMonitor.this.removeMessages(103);
                    }
                    this.m11vRoamingOnGoing = false;
                    this.m11vRoamingFailedCounter++;
                    this.mLast11vRoamingFailedTs = System.currentTimeMillis();
                    HwWifiConnectivityMonitor.this.LOGD("CMD_11v_ROAMING_TIMEOUT received, counter = " + this.m11vRoamingFailedCounter + ", ts = " + DateFormat.getDateTimeInstance().format(new Date(this.mLast11vRoamingFailedTs)));
                    if (this.mLastSignalLevel == 0) {
                        disconnectPoorWifiConnection();
                        break;
                    }
                    break;
                case 109:
                    if (this.m11vBssidSupported && this.m11vRoamingFailedCounter <= 1 && HwWifiConnectivityMonitor.this.m11vRoamingDisconectedCounter <= 1) {
                        if (HwWifiConnectivityMonitor.this.hasMessages(103)) {
                            HwWifiConnectivityMonitor.this.removeMessages(103);
                        }
                        query11vRoamingNetowrk(16);
                        break;
                    }
                case 110:
                    handleSignalPoorLevelOne();
                    break;
                case 111:
                    if (this.mLastSignalLevel <= 1) {
                        handleUserMoveDetected();
                        break;
                    }
                    break;
                case 112:
                    handleTopUidInternetStatusChanged(message.arg1, message.arg2);
                    switchWifiNetworkQuickly();
                    break;
                case 113:
                    handleBgWifiLinkStatusChanged(message.arg1, ((Boolean) message.obj).booleanValue());
                    break;
                case 114:
                    boolean newState = ((Boolean) message.obj).booleanValue();
                    HwWifiConnectivityMonitor.this.LOGD("CMD_VERIFY_WIFI_LINK_STATE, newState = " + newState + ", oldState = " + this.mEnterVerifyLinkState + ", mPoorLinkRssi = " + this.mPoorLinkRssi);
                    if ((!newState || !this.mEnterVerifyLinkState) && ((newState || this.mEnterVerifyLinkState) && this.mPoorLinkRssi != -200)) {
                        this.mEnterVerifyLinkState = newState;
                        this.mRssiGoodCnt = 0;
                        this.mStrongRssiCnt = 0;
                        if (!this.mEnterVerifyLinkState) {
                            this.mRssiBeforeSwitchWifi = WifiHandover.INVALID_RSSI;
                            this.mPoorLinkRssi = WifiHandover.INVALID_RSSI;
                            break;
                        } else {
                            this.mRssiBeforeSwitchWifi = this.mPoorLinkRssi;
                            break;
                        }
                    }
                case 116:
                    handleForegroundAppChanged(message);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void handleForegroundAppChanged(Message message) {
            if (this.mLastSignalLevel == 1) {
                HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
                hwWifiConnectivityMonitor.LOGD("handleForegroundAppChanged, current app : " + message.obj);
                if (HwWifiConnectivityMonitor.this.hasMessages(110)) {
                    HwWifiConnectivityMonitor.this.removeMessages(110);
                }
                HwWifiConnectivityMonitor.this.sendMessageDelayed(110, 3000);
            }
        }

        private void handleTopUidInternetStatusChanged(int uid, int status) {
            HwWifiConnectivityMonitor.this.LOGD("handleTopUidInternetStatusChanged, uid = " + uid + ", status = " + status);
            this.mCurrMonitorTopUid = uid;
            if (status == 0) {
                this.mCurrTopUidVeryBadCnt = 0;
                this.mCurrTopUidBadCnt = 0;
            } else if (status == 1) {
                this.mCurrTopUidBadCnt++;
            } else if (status == 2) {
                this.mCurrTopUidVeryBadCnt++;
            }
        }

        private void switchWifiNetworkQuickly() {
            int topUid = -1;
            String pktName = "";
            HwAutoConnectManager autoConnectManager = HwAutoConnectManager.getInstance();
            if (autoConnectManager != null) {
                topUid = autoConnectManager.getCurrentTopUid();
                pktName = autoConnectManager.getCurrentPackageName();
            }
            if (this.mCurrMonitorTopUid != -1 && this.mCurrMonitorTopUid == topUid && !HwWifiConnectivityMonitor.this.isStopWifiSwitch(pktName)) {
                WifiInfo wifiInfo = HwWifiConnectivityMonitor.this.mWifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    this.mCurrRssiVal = wifiInfo.getRssi();
                    if (this.mLastSignalLevel <= 1) {
                        if (this.mCurrTopUidBadCnt >= 1 || this.mCurrTopUidVeryBadCnt >= 1) {
                            HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
                            hwWifiConnectivityMonitor.LOGD("signal level = 1, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                            notifyWifiLinkPoor(true, 101);
                        }
                    } else if (this.mLastSignalLevel == 2) {
                        if (WifiProCommonUtils.isInMonitorList(pktName, HwWifiConnectivityMonitor.URGENT_MINI_APP_PKT_NAME)) {
                            if (this.mCurrTopUidVeryBadCnt >= 1 || this.mCurrTopUidBadCnt >= 1) {
                                HwWifiConnectivityMonitor hwWifiConnectivityMonitor2 = HwWifiConnectivityMonitor.this;
                                hwWifiConnectivityMonitor2.LOGD("signal level = 2, URGENT_MINI, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                                notifyWifiLinkPoor(true, 102);
                            }
                            return;
                        } else if (WifiProCommonUtils.isWpaOrWpa2(this.mConnectedConfig)) {
                            if (this.mCurrTopUidBadCnt >= 2 || this.mCurrTopUidVeryBadCnt >= 2 || (this.mCurrTopUidBadCnt == 1 && this.mCurrTopUidVeryBadCnt == 1)) {
                                HwWifiConnectivityMonitor hwWifiConnectivityMonitor3 = HwWifiConnectivityMonitor.this;
                                hwWifiConnectivityMonitor3.LOGD("signal level = 2, WPA2, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                                notifyWifiLinkPoor(true, 103);
                            }
                        } else if (WifiProCommonUtils.isInMonitorList(pktName, HwWifiConnectivityMonitor.URGENT_APP_PKT_NAME)) {
                            if (this.mCurrTopUidBadCnt >= 1 || this.mCurrTopUidVeryBadCnt >= 1) {
                                HwWifiConnectivityMonitor hwWifiConnectivityMonitor4 = HwWifiConnectivityMonitor.this;
                                hwWifiConnectivityMonitor4.LOGD("signal level = 2, URGENT, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                                notifyWifiLinkPoor(true, 104);
                            }
                        } else if (this.mCurrTopUidBadCnt >= 2 || this.mCurrTopUidVeryBadCnt >= 2 || (this.mCurrTopUidBadCnt == 1 && this.mCurrTopUidVeryBadCnt == 1)) {
                            HwWifiConnectivityMonitor hwWifiConnectivityMonitor5 = HwWifiConnectivityMonitor.this;
                            hwWifiConnectivityMonitor5.LOGD("signal level = 2, NORMAL, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                            notifyWifiLinkPoor(true, HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO);
                        }
                    } else if (this.mLastSignalLevel >= 3) {
                        if (!WifiProCommonUtils.isOpenAndPortal(this.mConnectedConfig)) {
                            this.mCurrTopUidVeryBadCnt = 0;
                            this.mCurrTopUidBadCnt = 0;
                            return;
                        } else if (this.mCurrTopUidVeryBadCnt >= 4 || (this.mCurrTopUidBadCnt >= 1 && this.mCurrTopUidVeryBadCnt == 3)) {
                            HwWifiConnectivityMonitor hwWifiConnectivityMonitor6 = HwWifiConnectivityMonitor.this;
                            hwWifiConnectivityMonitor6.LOGD("signal level = 4, NORMAL, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt + ", rssi = " + this.mCurrRssiVal);
                            notifyWifiLinkPoor(true, HwQoEUtils.QOE_MSG_EVALUATE_OTA_INFO);
                        }
                    }
                    return;
                }
                HwWifiConnectivityMonitor.this.LOGD("switchWifiNetworkQuickly, can't get rssi from wifi info!");
            }
        }

        private void handleBgWifiLinkStatusChanged(int currentRssi, boolean txGood) {
            if (this.mEnterVerifyLinkState && this.mRssiBeforeSwitchWifi != -200) {
                if (!txGood) {
                    this.mRssiGoodCnt = 0;
                    this.mStrongRssiCnt = 0;
                }
                if (this.mRssiBeforeSwitchWifi >= -65 || currentRssi < -65) {
                    this.mStrongRssiCnt = 0;
                } else if (currentRssi - this.mRssiBeforeSwitchWifi >= 5) {
                    this.mStrongRssiCnt++;
                } else {
                    this.mStrongRssiCnt = 0;
                }
                if (currentRssi - this.mRssiBeforeSwitchWifi >= 8) {
                    this.mRssiGoodCnt++;
                } else {
                    this.mRssiGoodCnt = 0;
                }
                if (this.mStrongRssiCnt == 6 || this.mRssiGoodCnt == 16) {
                    HwWifiConnectivityMonitor.this.LOGD("handleBgWifiLinkStatusChanged, notify switch back to stable wifi, curr rssi = " + currentRssi + ", last rssi = " + this.mRssiBeforeSwitchWifi + ", strong cnt = " + this.mStrongRssiCnt + ", good cnt = " + this.mRssiGoodCnt);
                    notifyWifiLinkPoor(false, 100);
                    this.mRssiGoodCnt = 0;
                    this.mStrongRssiCnt = 0;
                }
            }
        }

        private void handleNewRssiRcvd(int newRssi) {
            this.mCurrRssiVal = newRssi;
            int currentSignalLevel = WifiProCommonUtils.getCurrenSignalLevel(HwWifiConnectivityMonitor.this.mWifiManager.getConnectionInfo());
            if (currentSignalLevel >= 0 && currentSignalLevel != this.mLastSignalLevel) {
                HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
                hwWifiConnectivityMonitor.LOGD("signal level changed: " + this.mLastSignalLevel + " --> " + currentSignalLevel + ", 802.11v = " + this.m11vBssidSupported);
                if (currentSignalLevel == 2) {
                    HwWifiConnectivityMonitor.this.registerStepCntSensor();
                } else if (currentSignalLevel == 1) {
                    HwWifiConnectivityMonitor.this.registerStepCntSensor();
                    HwWifiConnectivityMonitor.this.sendMessageDelayed(110, WifiHandover.HANDOVER_WAIT_SCAN_TIME_OUT);
                } else if (currentSignalLevel == 4) {
                    HwWifiConnectivityMonitor.this.unregisterStepCntSensor();
                }
                if (currentSignalLevel == 0 && !HwWifiConnectivityMonitor.this.hasMessages(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO)) {
                    HwWifiConnectivityMonitor.this.sendMessageDelayed(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO, WifiHandover.HANDOVER_WAIT_SCAN_TIME_OUT);
                } else if (currentSignalLevel >= 2) {
                    HwWifiConnectivityMonitor.this.removeMessages(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO);
                    HwWifiConnectivityMonitor.this.removeMessages(110);
                } else if (currentSignalLevel > 0) {
                    HwWifiConnectivityMonitor.this.removeMessages(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO);
                }
                if (this.m11vBssidSupported && !this.m11vRoamingOnGoing) {
                    if (currentSignalLevel > 2 || this.m11vRoamingFailedCounter > 1 || HwWifiConnectivityMonitor.this.m11vRoamingDisconectedCounter > 1) {
                        if (currentSignalLevel >= 3) {
                            HwWifiConnectivityMonitor.this.removeMessages(103);
                        }
                    } else if (!HwWifiConnectivityMonitor.this.hasMessages(103)) {
                        HwWifiConnectivityMonitor hwWifiConnectivityMonitor2 = HwWifiConnectivityMonitor.this;
                        hwWifiConnectivityMonitor2.LOGD("to delay " + HwWifiConnectivityMonitor.DELAYED_MS_TABLE[currentSignalLevel] + " ms to request roaming 802.11v network.");
                        HwWifiConnectivityMonitor.this.sendMessageDelayed(103, (long) HwWifiConnectivityMonitor.DELAYED_MS_TABLE[currentSignalLevel]);
                    }
                }
            }
            this.mLastSignalLevel = currentSignalLevel;
        }

        private void disconnectPoorWifiConnection() {
            boolean isRoaming = this.mRoamingOnGoing || this.m11vRoamingOnGoing || HwWifiConnectivityMonitor.this.hasMessages(103);
            boolean disableAutoDisconnect = SystemProperties.getBoolean(HwWifiConnectivityMonitor.PROP_DISABLE_AUTO_DISC, false);
            HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
            hwWifiConnectivityMonitor.LOGD("disconnectPoorWifiConnection, isRoaming = " + isRoaming + ", isFullScreen = " + HwWifiConnectivityMonitor.this.isFullScreen());
            if (HwWifiConnectivityMonitor.this.mWifiManager != null && !disableAutoDisconnect && (((!HwWifiConnectivityMonitor.this.isFullScreen() && !WifiProCommonUtils.isLandscapeMode(HwWifiConnectivityMonitor.this.mContext)) || HwWifiConnectivityMonitor.this.isNeedDiscInGame()) && !HwWifiConnectivityMonitor.this.isMobileDataInactive() && !WifiProCommonUtils.isCalling(HwWifiConnectivityMonitor.this.mContext))) {
                HwWifiConnectivityMonitor.this.LOGD("WARN: to auto disconnect network quickly due to poor rssi and no roaming (signal level = 0)");
                HwWifiConnectivityMonitor.this.mWifiManager.disconnect();
            } else if (this.mLastSignalLevel == 0 && !HwWifiConnectivityMonitor.this.hasMessages(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO)) {
                HwWifiConnectivityMonitor.this.sendMessageDelayed(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO, WifiHandover.HANDOVER_WAIT_SCAN_TIME_OUT);
            }
        }

        private void query11vRoamingNetowrk(int reason) {
            HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
            hwWifiConnectivityMonitor.LOGD("query11vRoamingNetowrk, mRoamingOnGoing = " + this.mRoamingOnGoing + ", m11vRoamingOnGoing = " + this.m11vRoamingOnGoing);
            if (!this.mRoamingOnGoing && !this.m11vRoamingOnGoing) {
                HwWifiConnectivityMonitor.this.mWifiNative.query11vRoamingNetwork(reason);
                this.m11vRoamingOnGoing = true;
                if (HwWifiConnectivityMonitor.this.hasMessages(HwQoEUtils.QOE_MSG_WIFI_DISABLE)) {
                    HwWifiConnectivityMonitor.this.removeMessages(HwQoEUtils.QOE_MSG_WIFI_DISABLE);
                }
                HwWifiConnectivityMonitor.this.sendMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_DISABLE, 8000);
            }
        }

        private boolean is11vNetworkConnected() {
            String currentBssid = WifiProCommonUtils.getCurrentBssid(HwWifiConnectivityMonitor.this.mWifiManager);
            if (!(HwWifiConnectivityMonitor.this.mWifiManager == null || currentBssid == null)) {
                List<ScanResult> scanResults = getScanResults();
                if (scanResults != null) {
                    for (ScanResult scanResult : scanResults) {
                        if (currentBssid.equals(scanResult.BSSID) && scanResult.dot11vNetwork) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private List<ScanResult> getScanResults() {
            List<ScanResult> scanResults = new ArrayList<>();
            WifiScanner wifiScanner = WifiInjector.getInstance().getWifiScanner();
            if (wifiScanner != null) {
                synchronized (wifiScanner) {
                    for (ScanResult result : wifiScanner.getSingleScanResults()) {
                        scanResults.add(new ScanResult(result));
                    }
                }
            }
            return scanResults;
        }

        private void handleSignalPoorLevelOne() {
            if (this.mConnectedConfig != null && !HwWifiConnectivityMonitor.this.isMobileDataInactive()) {
                int foregroundUid = -1;
                String pktName = "";
                HwAutoConnectManager autoConnectManager = HwAutoConnectManager.getInstance();
                if (autoConnectManager != null) {
                    foregroundUid = autoConnectManager.getCurrentTopUid();
                    pktName = autoConnectManager.getCurrentPackageName();
                }
                if (!WifiProCommonUtils.isWpaOrWpa2(this.mConnectedConfig) && HwUidTcpMonitor.getInstance(HwWifiConnectivityMonitor.this.mContext).isAppAccessInternet(foregroundUid)) {
                    notifyWifiLinkPoor(true, HwQoEUtils.QOE_MSG_WIFI_ENABLED);
                } else if (WifiProCommonUtils.isInMonitorList(pktName, HwWifiConnectivityMonitor.URGENT_MINI_APP_PKT_NAME)) {
                    HwWifiConnectivityMonitor.this.LOGD("handleSignalPoorLevelOne, URGENT_MINI_APP_PKT_NAME matched.");
                    notifyWifiLinkPoor(true, HwQoEUtils.QOE_MSG_WIFI_DISABLE);
                }
            }
        }

        private void handleUserMoveDetected() {
            HwWifiConnectivityMonitor hwWifiConnectivityMonitor = HwWifiConnectivityMonitor.this;
            hwWifiConnectivityMonitor.LOGD("handleUserMoveDetected, isScreenOn = " + HwWifiConnectivityMonitor.this.mPowerManager.isScreenOn() + ", isMobileDataInactive = " + HwWifiConnectivityMonitor.this.isMobileDataInactive() + ", isFullScreen = " + HwWifiConnectivityMonitor.this.isFullScreen());
            int foregroundUid = -1;
            String pktName = "";
            HwAutoConnectManager autoConnectManager = HwAutoConnectManager.getInstance();
            if (autoConnectManager != null) {
                foregroundUid = autoConnectManager.getCurrentTopUid();
                pktName = autoConnectManager.getCurrentPackageName();
            }
            boolean fullScreen = HwWifiConnectivityMonitor.this.isFullScreen() && !WifiProCommonUtils.isInMonitorList(pktName, HwWifiConnectivityMonitor.URGENT_MINI_APP_PKT_NAME);
            if (HwWifiConnectivityMonitor.this.mPowerManager.isScreenOn() && !HwWifiConnectivityMonitor.this.isMobileDataInactive() && !fullScreen && !WifiProCommonUtils.isCalling(HwWifiConnectivityMonitor.this.mContext) && !WifiProCommonUtils.isLandscapeMode(HwWifiConnectivityMonitor.this.mContext) && HwUidTcpMonitor.getInstance(HwWifiConnectivityMonitor.this.mContext).isAppAccessInternet(foregroundUid)) {
                notifyWifiLinkPoor(true, HwQoEUtils.QOE_MSG_WIFI_DISABLE);
                HwWifiConnectivityMonitor.this.unregisterStepCntSensor();
            }
        }

        private void notifyWifiLinkPoor(boolean poorLink, int reason) {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
            if (wifiProStateMachine != null) {
                if (poorLink) {
                    this.mPoorLinkRssi = this.mCurrRssiVal;
                }
                synchronized (HwWifiConnectivityMonitor.this.mCircleStatLock) {
                    int unused = HwWifiConnectivityMonitor.this.mNotifyWifiLinkPoorReason = reason;
                }
                wifiProStateMachine.notifyWifiLinkPoor(poorLink);
            }
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            if (message.what == 117) {
                HwWifiConnectivityMonitor.this.LOGD("DefaultState receive CMD_11v_ROAMING_PENALIZE_TIMEOUT");
                int unused = HwWifiConnectivityMonitor.this.m11vRoamingDisconectedCounter = 0;
            }
            return true;
        }
    }

    class DisconnectedMonitorState extends State {
        DisconnectedMonitorState() {
        }

        public void enter() {
            HwWifiConnectivityMonitor.this.LOGD("###DisconnectedMonitorState, enter()");
            HwWifiConnectivityMonitor.this.unregisterStepCntSensor();
        }

        public boolean processMessage(Message message) {
            if (message.what != 101) {
                return false;
            }
            HwWifiConnectivityMonitor.this.transitionTo(HwWifiConnectivityMonitor.this.mConnectedMonitorState);
            return true;
        }
    }

    class StepSensorEventListener implements SensorEventListener {
        private int mLastStepCnt = 0;
        private int mMotionDetectedCnt = 0;
        private long mSensorEventRcvdTs = -1;

        public StepSensorEventListener() {
        }

        public void reset() {
            this.mLastStepCnt = 0;
            this.mMotionDetectedCnt = 0;
            this.mSensorEventRcvdTs = -1;
        }

        public void onSensorChanged(SensorEvent event) {
            if (event != null && event.sensor != null && event.sensor.getType() == 19) {
                long currentTimestamp = System.currentTimeMillis();
                int currentStepCnt = (int) event.values[0];
                if (currentStepCnt - this.mLastStepCnt > 0) {
                    this.mMotionDetectedCnt++;
                    if (this.mMotionDetectedCnt == 8) {
                        this.mMotionDetectedCnt = 0;
                        HwWifiConnectivityMonitor.this.sendMessage(111);
                    }
                } else if (this.mSensorEventRcvdTs > 0 && currentTimestamp - this.mSensorEventRcvdTs > 2000) {
                    this.mMotionDetectedCnt = 0;
                }
                this.mLastStepCnt = currentStepCnt;
                this.mSensorEventRcvdTs = currentTimestamp;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(HwWifiConnectivityMonitor.TAG, "SensorEventListener::onAccuracyChanged, accuracy = " + accuracy);
        }
    }

    static /* synthetic */ int access$012(HwWifiConnectivityMonitor x0, int x1) {
        int i = x0.m11vRoamingDisconectedCounter + x1;
        x0.m11vRoamingDisconectedCounter = i;
        return i;
    }

    public static synchronized HwWifiConnectivityMonitor getInstance(Context context, WifiStateMachine wsm) {
        HwWifiConnectivityMonitor hwWifiConnectivityMonitor;
        synchronized (HwWifiConnectivityMonitor.class) {
            if (mWifiConnectivityMonitor == null) {
                mWifiConnectivityMonitor = new HwWifiConnectivityMonitor(context, wsm);
            }
            hwWifiConnectivityMonitor = mWifiConnectivityMonitor;
        }
        return hwWifiConnectivityMonitor;
    }

    public static synchronized HwWifiConnectivityMonitor getInstance() {
        HwWifiConnectivityMonitor hwWifiConnectivityMonitor;
        synchronized (HwWifiConnectivityMonitor.class) {
            hwWifiConnectivityMonitor = mWifiConnectivityMonitor;
        }
        return hwWifiConnectivityMonitor;
    }

    private HwWifiConnectivityMonitor(Context context, WifiStateMachine wsm) {
        super(TAG);
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mStepCntSensor = this.mSensorManager.getDefaultSensor(19);
        addState(this.mDefaultState);
        addState(this.mConnectedMonitorState, this.mDefaultState);
        addState(this.mDisconnectedMonitorState, this.mDefaultState);
        setInitialState(this.mDisconnectedMonitorState);
        start();
    }

    public synchronized void setup() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            LOGD("setup DONE!");
            registerReceivers();
        }
    }

    public void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                        HwWifiConnectivityMonitor.this.sendMessage(102);
                    } else if (info != null && info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                        HwWifiConnectivityMonitor.this.sendMessage(101);
                    }
                } else if ("android.net.wifi.RSSI_CHANGED".equals(intent.getAction())) {
                    int newRssi = intent.getIntExtra("newRssi", -127);
                    if (newRssi != -127) {
                        HwWifiConnectivityMonitor.this.sendMessage(104, newRssi, 0);
                    }
                }
            }
        }, intentFilter);
    }

    /* access modifiers changed from: private */
    public boolean isFullScreen() {
        AbsPhoneWindowManager policy = (AbsPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        return policy != null && policy.isTopIsFullscreen();
    }

    /* access modifiers changed from: private */
    public boolean isNeedDiscInGame() {
        if (HwQoEService.getInstance() != null) {
            return HwQoEService.getInstance().isInGameAndNeedDisc();
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void registerStepCntSensor() {
        if (!this.mAccSensorRegistered.get()) {
            LOGD("registerStepCntSensor, mSensorEventListener");
            this.mSensorEventListener.reset();
            this.mSensorManager.registerListener(this.mSensorEventListener, this.mStepCntSensor, 3);
            this.mAccSensorRegistered.set(true);
        }
    }

    /* access modifiers changed from: private */
    public void unregisterStepCntSensor() {
        if (this.mAccSensorRegistered.get() && this.mSensorEventListener != null) {
            LOGD("unregisterStepCntSensor, mSensorEventListener");
            this.mSensorManager.unregisterListener(this.mSensorEventListener);
            this.mAccSensorRegistered.set(false);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0102, code lost:
        return false;
     */
    public synchronized boolean notifyTopUidTcpInfo(int uid, int tx, int rx, int reTx, int rtt, int rttPkts) {
        int i = uid;
        int i2 = tx;
        int i3 = rx;
        int i4 = reTx;
        int i5 = rtt;
        int i6 = rttPkts;
        synchronized (this) {
            if (this.mInitialized && i != -1 && i2 > 0) {
                float tr = ((float) i4) / ((float) i2);
                LOGD("ENTER: notifyTopUidTcpInfo, tx = " + i2 + ", rx = " + i3 + ", reTx = " + i4 + ", uid = " + i + ", tr = " + tr);
                float aveRtt = 0.0f;
                if (i5 > 0 && i6 > 0) {
                    aveRtt = ((float) i5) / ((float) i6);
                }
                float aveRtt2 = aveRtt;
                LOGD("ENTER: notifyTopUidTcpInfo, rtt = " + i5 + ", rttPkts = " + i6 + ", aveRtt = " + aveRtt2 + ", app = " + HwAutoConnectManager.getInstance().getCurrentPackageName());
                float aveRtt3 = aveRtt2;
                updateTopUidInfoStatistics(i2, i4, i3, i5, i6, 0);
                if ((tr >= 0.3f && i2 >= 20 && i3 <= 100) || (tr >= 0.4f && i2 < 20 && i2 >= 3 && i3 <= 200)) {
                    sendMessage(112, i, 2);
                    return true;
                } else if ((tr >= MORE_PKTS_BAD_RATE && i2 >= 20 && i3 <= 100) || (tr >= 0.3f && i2 < 20 && i2 >= 3 && i3 <= 200)) {
                    sendMessage(112, i, 1);
                    return true;
                } else if (aveRtt3 > 1200.0f) {
                    sendMessage(112, i, 2);
                    return true;
                } else if (aveRtt3 > 800.0f) {
                    sendMessage(112, i, 1);
                    return true;
                } else if (i3 > 1) {
                    sendMessage(112, i, 0);
                    return true;
                }
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004f, code lost:
        return;
     */
    public synchronized void notifyTopUidDnsInfo(int uid, int dnsFailCount) {
        if (uid > 0 && dnsFailCount > 0) {
            updateTopUidInfoStatistics(0, 0, 0, 0, 0, dnsFailCount);
            if (dnsFailCount > 5) {
                LOGD("ENTER: notifyTopUidDnsInfo, dnsFailCount = " + dnsFailCount);
                sendMessage(112, uid, 2);
            } else if (dnsFailCount >= 3) {
                LOGD("ENTER: notifyTopUidDnsInfo, dnsFailCount = " + dnsFailCount);
                sendMessage(112, uid, 1);
            }
        }
    }

    public synchronized void notifyBackgroundWifiLinkInfo(int rssi, int txgood, int txbad, int rxgood) {
        if (!this.mInitialized || txgood <= 0) {
            if (this.mInitialized && txgood <= 0) {
                LOGD("ENTER: notifyBackgroundWifiLinkInfo tx was bad, rssi = " + rssi + ", txgood = " + txgood + ", txbad = " + txbad + ", rxgood = " + rxgood);
            }
        } else if (((float) txbad) / ((float) (txbad + txgood)) < 0.3f) {
            sendMessage(113, rssi, 0, true);
        } else {
            sendMessage(113, rssi, 0, false);
        }
    }

    public synchronized void notifyWifiRoamingStarted() {
        LOGD("ENTER: notifyWifiRoamingStarted()");
        if (this.mInitialized) {
            sendMessage(HwQoEUtils.QOE_MSG_EVALUATE_OTA_INFO);
        }
    }

    public synchronized void notifyWifiRoamingCompleted() {
        LOGD("ENTER: notifyWifiRoamingCompleted()");
        if (this.mInitialized) {
            sendMessage(HwQoEUtils.QOE_MSG_WIFI_ENABLED);
        }
    }

    public synchronized void requestRoamingByNoInternet() {
        LOGD("ENTER: requestRoamingByNoInternet()");
        if (this.mInitialized) {
            sendMessage(109);
        }
    }

    public synchronized void notifyWifiDisconnected() {
        if (this.mInitialized) {
            sendMessage(102);
        }
    }

    public synchronized void notifyVerifyingLinkState(boolean enterVerifyingLinkState) {
        if (this.mInitialized) {
            sendMessage(114, Boolean.valueOf(enterVerifyingLinkState));
        }
    }

    public synchronized void disconnectePoorWifi() {
        if (this.mInitialized) {
            sendMessage(HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO);
        }
    }

    /* access modifiers changed from: private */
    public boolean isMobileDataInactive() {
        return WifiProCommonUtils.isMobileDataOff(this.mContext) || WifiProCommonUtils.isNoSIMCard(this.mContext);
    }

    public synchronized void notifyForegroundAppChanged(String appPackageName) {
        if (this.mInitialized) {
            sendMessage(116, appPackageName);
        }
    }

    private void updateTopUidInfoStatistics(int tx, int reTx, int rx, int rtt, int rttPkts, int dnsFailCount) {
        String pktName = HwAutoConnectManager.getInstance().getCurrentPackageName();
        String newCircleStat = tx + "|" + reTx + "|" + rx + "|" + rtt + "|" + rttPkts + "|" + dnsFailCount + "|" + pktName;
        synchronized (this.mCircleStatLock) {
            if (this.circleStat.size() < 5) {
                this.circleStat.add(newCircleStat);
            } else {
                this.circleStat.remove(0);
                this.circleStat.add(newCircleStat);
            }
        }
    }

    public List<String> getCircleStat() {
        List<String> list;
        synchronized (this.mCircleStatLock) {
            list = this.circleStat;
        }
        return list;
    }

    public int getNotifyWifiLinkPoorReason() {
        int i;
        synchronized (this.mCircleStatLock) {
            i = this.mNotifyWifiLinkPoorReason;
        }
        return i;
    }

    public int getStopWifiSwitchReason() {
        int i;
        synchronized (this.mCircleStatLock) {
            i = this.mStopWifiSwitchReason;
        }
        return i;
    }

    public void resetCircleStat() {
        synchronized (this.mCircleStatLock) {
            this.circleStat.clear();
            this.mNotifyWifiLinkPoorReason = -1;
            this.mStopWifiSwitchReason = -1;
        }
    }

    public boolean isStopWifiSwitch(String pktName) {
        synchronized (this.mCircleStatLock) {
            if (isMobileDataInactive()) {
                this.mStopWifiSwitchReason = 201;
                return true;
            } else if (!this.mPowerManager.isScreenOn()) {
                this.mStopWifiSwitchReason = 202;
                return true;
            } else if (isFullScreen() && !WifiProCommonUtils.isInMonitorList(pktName, URGENT_MINI_APP_PKT_NAME)) {
                this.mStopWifiSwitchReason = 203;
                return true;
            } else if (WifiProCommonUtils.isLandscapeMode(this.mContext)) {
                this.mStopWifiSwitchReason = 204;
                return true;
            } else {
                this.mStopWifiSwitchReason = -1;
                return false;
            }
        }
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }
}
