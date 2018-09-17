package com.android.server.wifi;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.WindowManagerPolicy;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.LocalServices;
import com.android.server.policy.AbsPhoneWindowManager;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.wifipro.PortalAutoFillManager;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import com.android.server.wifi.wifipro.WifiproUtils;
import com.android.server.wifipro.WifiProCommonUtils;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwWifiConnectivityMonitor extends StateMachine {
    public static final String ACTION_11v_ROAMING_NETWORK_FOUND = "com.huawei.wifi.action.11v_ROAMING_NETWORK_FOUND";
    private static final int BAD_AVE_RTT = 800;
    private static final int CMD_11v_ROAMING_TIMEOUT = 108;
    private static final int CMD_BG_WIFI_LINK_STATUS = 113;
    private static final int CMD_DISCONNECT_POOR_LINK = 105;
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
    private static final int[] DELAYED_MS_TABLE = new int[]{2000, 4000, 10000, HwQoEService.KOG_CHECK_FG_APP_PERIOD, 0};
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
    private static final int ROAMING_11v_NETWORK_TIMEOUT_MS = 8000;
    private static final int SIGNAL_LEVEL_0 = 0;
    private static final int SIGNAL_LEVEL_1 = 1;
    private static final int SIGNAL_LEVEL_2 = 2;
    private static final int SIGNAL_LEVEL_3 = 3;
    private static final int SIGNAL_LEVEL_4 = 4;
    private static final int STEP_INCREASE_THRESHOLD = 10;
    private static final String TAG = "HwWifiConnectivityMonitor";
    private static final float TX_GOOD_RATE = 0.3f;
    private static final String[] URGENT_APP_PKT_NAME = new String[]{PortalAutoFillManager.BROWSER_PACKET_NAME, "com.UCMobile", "com.tencent.mtt", "com.netease.newsreader.activity", "com.ss.android.article.news", "com.sina.news", "com.tencent.news", "com.sohu.newsclient", "com.ifeng.news2", "com.android.chrome", "com.myzaker.ZAKER_Phone", "com.sina.weibo", "com.hexin.plat.android", "com.android.email", "com.google.android.gm", "com.huawei.works"};
    private static final String[] URGENT_MINI_APP_PKT_NAME = new String[]{"com.tencent.mm", "com.tencent.mobileqq", "com.eg.android.AlipayGphone", "com.sdu.didi.psnger", "com.szzc.ucar.pilot", "com.ichinait.gbpassenger", "com.mobike.mobikeapp", "so.ofo.labofo", "com.baidu.BaiduMap", "com.autonavi.minimap", "com.google.android.apps.maps", "com.huawei.health", "com.huawei.espacev2", "com.baidu.searchbox", "com.whatsapp", "com.facebook.katana"};
    private static final int VERY_BAD_AVE_RTT = 1200;
    private static HwWifiConnectivityMonitor mWifiConnectivityMonitor = null;
    private AtomicBoolean mAccSensorRegistered = new AtomicBoolean(false);
    private State mConnectedMonitorState = new ConnectedMonitorState();
    private Context mContext;
    private State mDefaultState = new DefaultState();
    private State mDisconnectedMonitorState = new DisconnectedMonitorState();
    private boolean mInitialized = false;
    private PowerManager mPowerManager;
    private final StepSensorEventListener mSensorEventListener = new StepSensorEventListener();
    private SensorManager mSensorManager;
    private Sensor mStepCntSensor;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;

    class ConnectedMonitorState extends State {
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
                this.mLastSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(wifiInfo.getRssi());
                this.mCurrRssiVal = wifiInfo.getRssi();
                HwWifiConnectivityMonitor.this.LOGD("ConnectedMonitorState, network = " + wifiInfo.getSSID() + ", 802.11v = " + is11vNetworkConnected() + ", 2.4GHz = " + wifiInfo.is24GHz() + ", current level = " + this.mLastSignalLevel);
                if (!is11vNetworkConnected()) {
                    return;
                }
                if (wifiInfo.is24GHz() || this.mLastSignalLevel <= 2) {
                    HwWifiConnectivityMonitor.this.sendMessageDelayed(103, 5000);
                }
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 102:
                    HwWifiConnectivityMonitor.this.removeMessages(103);
                    HwWifiConnectivityMonitor.this.removeMessages(108);
                    HwWifiConnectivityMonitor.this.removeMessages(105);
                    HwWifiConnectivityMonitor.this.removeMessages(110);
                    HwWifiConnectivityMonitor.this.transitionTo(HwWifiConnectivityMonitor.this.mDisconnectedMonitorState);
                    break;
                case 103:
                    query11vRoamingNetowrk(16);
                    break;
                case 104:
                    handleNewRssiRcvd(message.arg1);
                    break;
                case 105:
                    disconnectPoorWifiConnection();
                    break;
                case 106:
                    if (HwWifiConnectivityMonitor.this.hasMessages(105)) {
                        HwWifiConnectivityMonitor.this.LOGD("CMD_DISCONNECT_POOR_LINK remove due to roaming received.");
                        HwWifiConnectivityMonitor.this.removeMessages(105);
                    }
                    this.mRoamingOnGoing = true;
                    break;
                case 107:
                    if (HwWifiConnectivityMonitor.this.hasMessages(108)) {
                        HwWifiConnectivityMonitor.this.LOGD("CMD_11v_ROAMING_TIMEOUT remove due to roaming completed received.");
                        HwWifiConnectivityMonitor.this.removeMessages(108);
                    }
                    this.mRoamingOnGoing = false;
                    this.m11vRoamingOnGoing = false;
                    this.m11vRoamingFailedCounter = 0;
                    this.mLast11vRoamingFailedTs = 0;
                    break;
                case 108:
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
                    if (is11vNetworkConnected() && this.m11vRoamingFailedCounter <= 1) {
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
                    if (!(newState && this.mEnterVerifyLinkState) && ((newState || (this.mEnterVerifyLinkState ^ 1) == 0) && this.mPoorLinkRssi != WifiHandover.INVALID_RSSI)) {
                        this.mEnterVerifyLinkState = newState;
                        this.mRssiGoodCnt = 0;
                        this.mStrongRssiCnt = 0;
                        if (!this.mEnterVerifyLinkState) {
                            this.mRssiBeforeSwitchWifi = WifiHandover.INVALID_RSSI;
                            this.mPoorLinkRssi = WifiHandover.INVALID_RSSI;
                            break;
                        }
                        this.mRssiBeforeSwitchWifi = this.mPoorLinkRssi;
                        break;
                    }
                default:
                    return false;
            }
            return true;
        }

        private void handleTopUidInternetStatusChanged(int uid, int status) {
            HwWifiConnectivityMonitor.this.LOGD("handleTopUidInternetStatusChanged, uid = " + uid + ", status = " + status);
            if (this.mCurrMonitorTopUid == -1 || uid == -1 || uid == this.mCurrMonitorTopUid) {
                this.mCurrMonitorTopUid = uid;
                if (status == 0) {
                    this.mCurrTopUidVeryBadCnt = 0;
                    this.mCurrTopUidBadCnt = 0;
                } else if (status == 1) {
                    this.mCurrTopUidBadCnt++;
                } else if (status == 2) {
                    this.mCurrTopUidVeryBadCnt++;
                }
                return;
            }
            this.mCurrMonitorTopUid = uid;
            if (status == 0) {
                this.mCurrTopUidVeryBadCnt = 0;
                this.mCurrTopUidBadCnt = 0;
            } else if (status == 1) {
                this.mCurrTopUidBadCnt = 1;
                this.mCurrTopUidVeryBadCnt = 0;
            } else if (status == 2) {
                this.mCurrTopUidVeryBadCnt = 1;
                this.mCurrTopUidBadCnt = 0;
            }
        }

        /* JADX WARNING: Missing block: B:4:0x0018, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void switchWifiNetworkQuickly() {
            if (this.mCurrMonitorTopUid != -1 && this.mCurrMonitorTopUid == WifiProCommonUtils.getForegroundAppUid(HwWifiConnectivityMonitor.this.mContext) && !HwWifiConnectivityMonitor.this.isMobileDataInactive() && (HwWifiConnectivityMonitor.this.mPowerManager.isScreenOn() ^ 1) == 0 && !HwWifiConnectivityMonitor.this.isFullScreen() && !WifiProCommonUtils.isLandscapeMode(HwWifiConnectivityMonitor.this.mContext)) {
                String pktName = WifiProCommonUtils.getPackageName(HwWifiConnectivityMonitor.this.mContext, this.mCurrMonitorTopUid);
                WifiInfo wifiInfo = HwWifiConnectivityMonitor.this.mWifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    this.mCurrRssiVal = wifiInfo.getRssi();
                    if (this.mLastSignalLevel <= 1) {
                        if (this.mCurrTopUidBadCnt >= 1 || this.mCurrTopUidVeryBadCnt >= 1) {
                            HwWifiConnectivityMonitor.this.LOGD("signal level = 1, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                            notifyWifiLinkPoor(true);
                        }
                    } else if (this.mLastSignalLevel == 2) {
                        if (WifiProCommonUtils.isInMonitorList(pktName, HwWifiConnectivityMonitor.URGENT_MINI_APP_PKT_NAME)) {
                            if (this.mCurrTopUidVeryBadCnt >= 1 || this.mCurrTopUidBadCnt >= 1) {
                                HwWifiConnectivityMonitor.this.LOGD("signal level = 2, URGENT_MINI, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                                notifyWifiLinkPoor(true);
                            }
                            return;
                        } else if (WifiProCommonUtils.isWpaOrWpa2(this.mConnectedConfig)) {
                            if (this.mCurrTopUidBadCnt >= 2 || this.mCurrTopUidVeryBadCnt >= 2 || (this.mCurrTopUidBadCnt == 1 && this.mCurrTopUidVeryBadCnt == 1)) {
                                HwWifiConnectivityMonitor.this.LOGD("signal level = 2, WPA2, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                                notifyWifiLinkPoor(true);
                            }
                        } else if (WifiProCommonUtils.isInMonitorList(pktName, HwWifiConnectivityMonitor.URGENT_APP_PKT_NAME)) {
                            if (this.mCurrTopUidBadCnt >= 1 || this.mCurrTopUidVeryBadCnt >= 1) {
                                HwWifiConnectivityMonitor.this.LOGD("signal level = 2, URGENT, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                                notifyWifiLinkPoor(true);
                            }
                        } else if (this.mCurrTopUidBadCnt >= 2 || this.mCurrTopUidVeryBadCnt >= 2 || (this.mCurrTopUidBadCnt == 1 && this.mCurrTopUidVeryBadCnt == 1)) {
                            HwWifiConnectivityMonitor.this.LOGD("signal level = 2, NORMAL, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt);
                            notifyWifiLinkPoor(true);
                        }
                    } else if (this.mLastSignalLevel != 3 || this.mCurrRssiVal > -70) {
                        if (this.mCurrRssiVal > -70 && WifiProCommonUtils.isOpenType(this.mConnectedConfig) && (this.mCurrTopUidVeryBadCnt >= 3 || (this.mCurrTopUidBadCnt == 1 && this.mCurrTopUidVeryBadCnt == 2))) {
                            HwWifiConnectivityMonitor.this.LOGD("signal level = 4, NORMAL, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt + ", rssi = " + this.mCurrRssiVal);
                            notifyWifiLinkPoor(true);
                        }
                    } else if ((this.mCurrTopUidBadCnt >= 2 && this.mCurrTopUidVeryBadCnt >= 1) || this.mCurrTopUidVeryBadCnt >= 2) {
                        HwWifiConnectivityMonitor.this.LOGD("signal level = 3, NORMAL, and bad cnt = " + this.mCurrTopUidBadCnt + ", very bad cnt = " + this.mCurrTopUidVeryBadCnt + ", rssi = " + this.mCurrRssiVal);
                        notifyWifiLinkPoor(true);
                    }
                    return;
                }
                HwWifiConnectivityMonitor.this.LOGD("switchWifiNetworkQuickly, can't get rssi from wifi info!");
            }
        }

        private void handleBgWifiLinkStatusChanged(int currentRssi, boolean txGood) {
            if (this.mEnterVerifyLinkState && this.mRssiBeforeSwitchWifi != WifiHandover.INVALID_RSSI) {
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
                    notifyWifiLinkPoor(false);
                    this.mRssiGoodCnt = 0;
                    this.mStrongRssiCnt = 0;
                }
            }
        }

        private void handleNewRssiRcvd(int newRssi) {
            this.mCurrRssiVal = newRssi;
            int currentSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(newRssi);
            if (currentSignalLevel >= 0 && currentSignalLevel != this.mLastSignalLevel) {
                HwWifiConnectivityMonitor.this.LOGD("signal level changed: " + this.mLastSignalLevel + " --> " + currentSignalLevel + ", 802.11v = " + is11vNetworkConnected());
                if (currentSignalLevel == 2) {
                    HwWifiConnectivityMonitor.this.registerStepCntSensor();
                } else if (currentSignalLevel == 1) {
                    HwWifiConnectivityMonitor.this.registerStepCntSensor();
                    HwWifiConnectivityMonitor.this.sendMessageDelayed(110, 4000);
                } else if (currentSignalLevel == 4) {
                    HwWifiConnectivityMonitor.this.unregisterStepCntSensor();
                }
                if (currentSignalLevel == 0 && (HwWifiConnectivityMonitor.this.hasMessages(105) ^ 1) != 0) {
                    HwWifiConnectivityMonitor.this.sendMessageDelayed(105, 4000);
                } else if (currentSignalLevel >= 2) {
                    HwWifiConnectivityMonitor.this.removeMessages(105);
                    HwWifiConnectivityMonitor.this.removeMessages(110);
                } else if (currentSignalLevel > 0) {
                    HwWifiConnectivityMonitor.this.removeMessages(105);
                }
                if (is11vNetworkConnected() && (this.m11vRoamingOnGoing ^ 1) != 0 && currentSignalLevel <= 2 && this.m11vRoamingFailedCounter <= 1) {
                    if (HwWifiConnectivityMonitor.this.hasMessages(103)) {
                        HwWifiConnectivityMonitor.this.removeMessages(103);
                    }
                    HwWifiConnectivityMonitor.this.LOGD("to delay " + HwWifiConnectivityMonitor.DELAYED_MS_TABLE[currentSignalLevel] + " ms to request roaming 802.11v network.");
                    HwWifiConnectivityMonitor.this.sendMessageDelayed(103, (long) HwWifiConnectivityMonitor.DELAYED_MS_TABLE[currentSignalLevel]);
                }
            }
            this.mLastSignalLevel = currentSignalLevel;
        }

        private void disconnectPoorWifiConnection() {
            boolean isRoaming;
            if (this.mRoamingOnGoing || this.m11vRoamingOnGoing) {
                isRoaming = true;
            } else {
                isRoaming = HwWifiConnectivityMonitor.this.hasMessages(103);
            }
            boolean disableAutoDisconnect = SystemProperties.getBoolean(HwWifiConnectivityMonitor.PROP_DISABLE_AUTO_DISC, false);
            HwWifiConnectivityMonitor.this.LOGD("disconnectPoorWifiConnection, isRoaming = " + isRoaming + ", isFullScreen = " + HwWifiConnectivityMonitor.this.isFullScreen());
            if (HwWifiConnectivityMonitor.this.mWifiManager != null && (disableAutoDisconnect ^ 1) != 0) {
                if (((!HwWifiConnectivityMonitor.this.isFullScreen() && (WifiProCommonUtils.isLandscapeMode(HwWifiConnectivityMonitor.this.mContext) ^ 1) != 0) || HwWifiConnectivityMonitor.this.isNeedDiscInGame()) && (HwWifiConnectivityMonitor.this.isMobileDataInactive() ^ 1) != 0 && (WifiProCommonUtils.isCalling(HwWifiConnectivityMonitor.this.mContext) ^ 1) != 0) {
                    HwWifiConnectivityMonitor.this.LOGD("WARN: to auto disconnect network quickly due to poor rssi and no roaming (signal level = 0)");
                    HwWifiConnectivityMonitor.this.mWifiManager.disconnect();
                }
            }
        }

        private void query11vRoamingNetowrk(int reason) {
            HwWifiConnectivityMonitor.this.LOGD("query11vRoamingNetowrk, mRoamingOnGoing = " + this.mRoamingOnGoing + ", m11vRoamingOnGoing = " + this.m11vRoamingOnGoing);
            if (!this.mRoamingOnGoing && (this.m11vRoamingOnGoing ^ 1) != 0) {
                HwWifiConnectivityMonitor.this.mWifiNative.query11vRoamingNetwork(reason);
                this.m11vRoamingOnGoing = true;
                if (HwWifiConnectivityMonitor.this.hasMessages(108)) {
                    HwWifiConnectivityMonitor.this.removeMessages(108);
                }
                HwWifiConnectivityMonitor.this.sendMessageDelayed(108, 8000);
            }
        }

        private boolean is11vNetworkConnected() {
            String currentBssid = WifiProCommonUtils.getCurrentBssid(HwWifiConnectivityMonitor.this.mWifiManager);
            if (!(HwWifiConnectivityMonitor.this.mWifiManager == null || currentBssid == null)) {
                List<ScanResult> scanResults = WifiproUtils.getScanResultsFromWsm();
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

        private void handleSignalPoorLevelOne() {
            if (this.mConnectedConfig != null && !WifiProCommonUtils.isWpaOrWpa2(this.mConnectedConfig) && (HwWifiConnectivityMonitor.this.isMobileDataInactive() ^ 1) != 0 && (WifiProCommonUtils.isCalling(HwWifiConnectivityMonitor.this.mContext) ^ 1) != 0 && HwWifiConnectivityMonitor.this.mPowerManager.isScreenOn()) {
                if (HwUidTcpMonitor.getInstance(HwWifiConnectivityMonitor.this.mContext).isAppAccessInternet(WifiProCommonUtils.getForegroundAppUid(HwWifiConnectivityMonitor.this.mContext))) {
                    notifyWifiLinkPoor(true);
                }
            }
        }

        private void handleUserMoveDetected() {
            HwWifiConnectivityMonitor.this.LOGD("handleUserMoveDetected, isScreenOn = " + HwWifiConnectivityMonitor.this.mPowerManager.isScreenOn() + ", isMobileDataInactive = " + HwWifiConnectivityMonitor.this.isMobileDataInactive() + ", isFullScreen = " + HwWifiConnectivityMonitor.this.isFullScreen());
            if (HwWifiConnectivityMonitor.this.mPowerManager.isScreenOn() && (HwWifiConnectivityMonitor.this.isMobileDataInactive() ^ 1) != 0 && (HwWifiConnectivityMonitor.this.isFullScreen() ^ 1) != 0 && (WifiProCommonUtils.isCalling(HwWifiConnectivityMonitor.this.mContext) ^ 1) != 0 && (WifiProCommonUtils.isLandscapeMode(HwWifiConnectivityMonitor.this.mContext) ^ 1) != 0) {
                if (HwUidTcpMonitor.getInstance(HwWifiConnectivityMonitor.this.mContext).isAppAccessInternet(WifiProCommonUtils.getForegroundAppUid(HwWifiConnectivityMonitor.this.mContext))) {
                    notifyWifiLinkPoor(true);
                    HwWifiConnectivityMonitor.this.unregisterStepCntSensor();
                }
            }
        }

        private void notifyWifiLinkPoor(boolean poorLink) {
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
            if (wifiProStateMachine != null) {
                if (poorLink) {
                    this.mPoorLinkRssi = this.mCurrRssiVal;
                }
                wifiProStateMachine.notifyWifiLinkPoor(poorLink);
            }
        }
    }

    static class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            int i = message.what;
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
            switch (message.what) {
                case 101:
                    HwWifiConnectivityMonitor.this.transitionTo(HwWifiConnectivityMonitor.this.mConnectedMonitorState);
                    return true;
                default:
                    return false;
            }
        }
    }

    class StepSensorEventListener implements SensorEventListener {
        private int mLastStepCnt = 0;
        private int mMotionDetectedCnt = 0;
        private long mSensorEventRcvdTs = -1;

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
                    if (this.mMotionDetectedCnt == 10) {
                        Log.d(HwWifiConnectivityMonitor.TAG, "SensorEventListener:: USER's MOVING......");
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
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction(ACTION_11v_ROAMING_NETWORK_FOUND);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && info.getDetailedState() == DetailedState.DISCONNECTED) {
                        HwWifiConnectivityMonitor.this.sendMessage(102);
                    } else if (info != null && info.getDetailedState() == DetailedState.CONNECTED) {
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

    private boolean isFullScreen() {
        AbsPhoneWindowManager policy = (AbsPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        return policy != null ? policy.isTopIsFullscreen() : false;
    }

    private boolean isNeedDiscInGame() {
        if (HwQoEService.getInstance() != null) {
            return HwQoEService.getInstance().isInGameAndNeedDisc();
        }
        return false;
    }

    private void registerStepCntSensor() {
        if (!this.mAccSensorRegistered.get()) {
            LOGD("registerStepCntSensor, mSensorEventListener");
            this.mSensorEventListener.reset();
            this.mSensorManager.registerListener(this.mSensorEventListener, this.mStepCntSensor, 3);
            this.mAccSensorRegistered.set(true);
        }
    }

    private void unregisterStepCntSensor() {
        if (this.mAccSensorRegistered.get() && this.mSensorEventListener != null) {
            LOGD("unregisterStepCntSensor, mSensorEventListener");
            this.mSensorManager.unregisterListener(this.mSensorEventListener);
            this.mAccSensorRegistered.set(false);
        }
    }

    public synchronized void notifyTopUidTcpInfo(int uid, int tx, int rx, int reTx, int rtt, int rttPkts) {
        if (this.mInitialized && uid != -1 && tx > 0) {
            float tr = ((float) reTx) / ((float) tx);
            LOGD("ENTER: notifyTopUidTcpInfo, tx = " + tx + ", rx = " + rx + ", reTx = " + reTx + ", uid = " + uid + ", tr = " + tr);
            float aveRtt = 0.0f;
            if (rtt > 0 && rttPkts > 0) {
                aveRtt = ((float) rtt) / ((float) rttPkts);
            }
            LOGD("ENTER: notifyTopUidTcpInfo, rtt = " + rtt + ", rttPkts = " + rttPkts + ", aveRtt = " + aveRtt + ", app = " + WifiProCommonUtils.getPackageName(this.mContext, uid));
            if ((tr >= 0.3f && tx >= 20 && rx <= 100) || (tr >= 0.4f && tx < 20 && tx >= 3 && rx <= 200)) {
                sendMessage(112, uid, 2);
            } else if ((tr >= MORE_PKTS_BAD_RATE && tx >= 20 && rx <= 100) || (tr >= 0.3f && tx < 20 && tx >= 3 && rx <= 200)) {
                sendMessage(112, uid, 1);
            } else if (aveRtt > 1200.0f) {
                sendMessage(112, uid, 2);
            } else if (aveRtt > 800.0f) {
                sendMessage(112, uid, 1);
            } else if (rx > 1) {
                sendMessage(112, uid, 0);
            }
        }
    }

    public synchronized void notifyBackgroundWifiLinkInfo(int rssi, int txgood, int txbad, int rxgood) {
        if (this.mInitialized && txgood > 0) {
            if (((float) txbad) / ((float) (txbad + txgood)) < 0.3f) {
                sendMessage(113, rssi, 0, Boolean.valueOf(true));
            } else {
                sendMessage(113, rssi, 0, Boolean.valueOf(false));
            }
        }
    }

    public synchronized void notifyWifiRoamingStarted() {
        LOGD("ENTER: notifyWifiRoamingStarted()");
        if (this.mInitialized) {
            sendMessage(106);
        }
    }

    public synchronized void notifyWifiRoamingCompleted() {
        LOGD("ENTER: notifyWifiRoamingCompleted()");
        if (this.mInitialized) {
            sendMessage(107);
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
            sendMessage(105);
        }
    }

    private boolean isMobileDataInactive() {
        return !WifiProCommonUtils.isMobileDataOff(this.mContext) ? WifiProCommonUtils.isNoSIMCard(this.mContext) : true;
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }
}
