package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifi.wifipro.PortalAutoFillManager;
import com.android.server.wifi.wifipro.WifiProUIDisplayManager;
import com.android.server.wifi.wifipro.WifiproUtils;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.List;

public class HwIntelligenceStateMachine extends StateMachine {
    private static final String ACTION_WIFI_PRO_TIMER = "android.net.wifi.wifi_pro_timer";
    private static final int PING_PONG_INTERVAL_TIME = 18000000;
    private static final int PING_PONG_MAX_PUNISH_TIME = 300000;
    private static final int PING_PONG_PUNISH_TIME = 30000;
    private static final int PING_PONG_TIME = 5000;
    private static final int WIFI_PRO_TIMER = 0;
    private static HwIntelligenceStateMachine mHwIntelligenceStateMachine;
    private AlarmManager mAlarmManager;
    private ApInfoManager mApInfoManager;
    private int mAuthType = -1;
    private int mAutoCloseMessage = 0;
    private int mAutoCloseScanTimes = 0;
    OnAlarmListener mAutoCloseTimeoutListener = new OnAlarmListener() {
        public void onAlarm() {
            Log.w(MessageUtil.TAG, "receive auto close message mAutoCloseMessage = " + HwIntelligenceStateMachine.this.mAutoCloseMessage);
            if (HwIntelligenceStateMachine.this.mAutoCloseMessage == 25) {
                HwIntelligenceStateMachine.this.mHandler.sendEmptyMessage(25);
            } else if (HwIntelligenceStateMachine.this.mAutoCloseMessage == 9) {
                HwIntelligenceStateMachine.this.mHandler.sendEmptyMessage(9);
            }
            HwIntelligenceStateMachine.this.mAutoCloseMessage = 0;
        }
    };
    private CellStateMonitor mCellStateMonitor;
    private String mConnectFailedBssid = null;
    private int mConnectFailedReason = -1;
    private String mConnectFailedSsid = null;
    private State mConnectedState = new ConnectedState();
    private Context mContext;
    private State mDefaultState = new DefaultState();
    private State mDisabledState = new DisabledState();
    private State mDisconnectedState = new DisconnectedState();
    private State mEnabledState = new EnabledState();
    private Handler mHandler;
    private HwintelligenceWiFiCHR mHwintelligenceWiFiCHR;
    private State mInitialState = new InitialState();
    private State mInternetReadyState = new InternetReadyState();
    private boolean mIsAutoClose = false;
    private boolean mIsAutoCloseSearch = false;
    private boolean mIsAutoOpenSearch = false;
    private boolean mIsInitialState = false;
    private boolean mIsMachineStared = false;
    private boolean mIsScreenOn = false;
    private boolean mIsWaittingAutoClose = false;
    private boolean mIsWifiP2PConnected = false;
    private long mLastCellChangeScanTime = 0;
    private long mLastScanPingpongTime = 0;
    private State mNoInternetState = new NoInternetState();
    private int mScanPingpongNum = 0;
    private State mStopState = new StopState();
    private List<APInfoData> mTargetApInfoDatas;
    private String mTargetSsid = null;
    private WiFiStateMonitor mWiFiStateMonitor;
    private WifiManager mWifiManager;

    class ConnectedState extends State {
        ConnectedState() {
        }

        public void enter() {
            Log.e(MessageUtil.TAG, "ConnectedState");
            if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose) {
                HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                Log.e(MessageUtil.TAG, "ConnectedState remove MSG_WIFI_HANDLE_DISABLE");
                HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.uploadAutoCloseFailed(1);
            }
            if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
                HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                if (HwIntelligenceStateMachine.this.mTargetApInfoDatas != null) {
                    HwIntelligenceStateMachine.this.mTargetApInfoDatas.clear();
                    HwIntelligenceStateMachine.this.mTargetApInfoDatas = null;
                }
            }
            updateConnectedInfo();
            HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.stopConnectTimer();
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 1:
                    updateConnectedInfo();
                    break;
                case 11:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mInternetReadyState);
                    break;
                case 12:
                    Log.e(MessageUtil.TAG, "ConnectedState MSG_WIFI_INTERNET_DISCONNECTED");
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mNoInternetState);
                    break;
                case 13:
                    Log.e(MessageUtil.TAG, "MSG_WIFI_IS_PORTAL");
                    WifiInfo mPortalInfo = HwIntelligenceStateMachine.this.mWifiManager.getConnectionInfo();
                    if (!(mPortalInfo == null || mPortalInfo.getSSID() == null)) {
                        if (HwIntelligenceStateMachine.this.mApInfoManager.getApInfoByBssid(mPortalInfo.getBSSID()) != null) {
                            HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.uploadPortalApInWhite(mPortalInfo.getBSSID(), mPortalInfo.getSSID());
                        }
                        HwIntelligenceStateMachine.this.mApInfoManager.delectApInfoBySsidForPortal(mPortalInfo);
                    }
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mNoInternetState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void updateConnectedInfo() {
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(HwIntelligenceStateMachine.this.mWifiManager);
            if (config != null) {
                HwIntelligenceStateMachine.this.mTargetSsid = config.SSID;
                if (config.allowedKeyManagement.cardinality() <= 1) {
                    HwIntelligenceStateMachine.this.mAuthType = config.getAuthType();
                } else {
                    HwIntelligenceStateMachine.this.mAuthType = -1;
                }
                Log.d(MessageUtil.TAG, "mTargetSsid is " + HwIntelligenceStateMachine.this.mTargetSsid + " mAuthType " + HwIntelligenceStateMachine.this.mAuthType);
            }
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 1:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mConnectedState);
                    break;
                case 2:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mDisconnectedState);
                    break;
                case 3:
                    int wifiEnableFlag = Global.getInt(HwIntelligenceStateMachine.this.mContext.getContentResolver(), "wifi_on", 0);
                    Log.e(MessageUtil.TAG, "MSG_WIFI_ENABLED wifiEnableFlag = " + wifiEnableFlag + " mIsAutoOpenSearch =" + HwIntelligenceStateMachine.this.mIsAutoOpenSearch);
                    if (wifiEnableFlag != 1 && wifiEnableFlag != 2) {
                        if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                            Log.e(MessageUtil.TAG, "MSG_WIFI_ENABLED start scan");
                            HwIntelligenceStateMachine.this.mWifiManager.startScan();
                            break;
                        }
                    }
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mEnabledState);
                    break;
                    break;
                case 4:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mDisabledState);
                    break;
                case 5:
                case 7:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 20:
                case 25:
                    Log.e(MessageUtil.TAG, " DefaultState message.what = " + message.what);
                    break;
                case 8:
                    Bundle data = message.getData();
                    String bssid = data.getString("bssid");
                    String ssid = data.getString("ssid");
                    Log.e(MessageUtil.TAG, "MSG_WIFI_CONFIG_CHANGED ssid = " + ssid);
                    if (ssid == null) {
                        HwIntelligenceStateMachine.this.mApInfoManager.delectApInfoByBssid(bssid);
                        break;
                    }
                    HwIntelligenceStateMachine.this.mApInfoManager.delectApInfoBySsid(ssid);
                    break;
                case 14:
                    HwIntelligenceStateMachine.this.mIsWifiP2PConnected = true;
                    break;
                case 15:
                    HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
                    break;
                case 21:
                    Log.e(MessageUtil.TAG, " DefaultState MSG_SCREEN_ON");
                    HwIntelligenceStateMachine.this.sendMessage(23);
                    break;
                case 22:
                    Log.e(MessageUtil.TAG, " DefaultState MSG_SCREEN_OFF mIsAutoOpenSearch = " + HwIntelligenceStateMachine.this.mIsAutoOpenSearch);
                    if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                        HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                        break;
                    }
                    break;
                case 24:
                    Bundle mData = message.getData();
                    HwIntelligenceStateMachine.this.mConnectFailedReason = mData.getInt("reason");
                    HwIntelligenceStateMachine.this.mConnectFailedBssid = mData.getString("bssid");
                    HwIntelligenceStateMachine.this.mConnectFailedSsid = mData.getString("ssid");
                    Log.e(MessageUtil.TAG, "MSG_CONNECT_FAILED ssid = " + HwIntelligenceStateMachine.this.mConnectFailedSsid + " mConnectFailedReason = " + HwIntelligenceStateMachine.this.mConnectFailedReason);
                    break;
                case 100:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mInitialState);
                    break;
                case 101:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mStopState);
                    break;
            }
            return true;
        }
    }

    class DisabledState extends State {
        DisabledState() {
        }

        public void enter() {
            Log.e(MessageUtil.TAG, "DisabledState");
            if (HwIntelligenceStateMachine.this.mIsInitialState) {
                Log.e(MessageUtil.TAG, "mIsInitialState state is disable");
                HwIntelligenceStateMachine.this.mIsInitialState = false;
            } else if (HwIntelligenceStateMachine.this.isClosedByUser()) {
                if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose) {
                    Log.e(MessageUtil.TAG, "DisabledState remove MSG_WIFI_HANDLE_DISABLE");
                    HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                    HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.uploadAutoCloseFailed(2);
                }
                if (HwIntelligenceStateMachine.this.mIsAutoClose) {
                    HwIntelligenceStateMachine.this.mIsAutoClose = false;
                    HwIntelligenceStateMachine.this.setAutoOpenValue(false);
                    if (!HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                        HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
                        HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                        HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                    }
                    HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.increaseAutoCloseCount();
                } else {
                    HwIntelligenceStateMachine.this.setAutoOpenValue(false);
                    HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
                    HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                    HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                    List mlist = null;
                    if (HwIntelligenceStateMachine.this.mWifiManager.isScanAlwaysAvailable()) {
                        WifiStateMachine wsm = WifiInjector.getInstance().getWifiStateMachine();
                        if (wsm != null) {
                            mlist = wsm.syncGetScanResultsList();
                        }
                    }
                    if (mlist == null || mlist.size() == 0) {
                        Log.d(MessageUtil.TAG, "syncGetScanResultsList is null, get from WiFiProScanResultList.");
                        mlist = HwIntelligenceWiFiManager.getWiFiProScanResultList();
                    }
                    if ((mlist == null || mlist.size() == 0) && HwIntelligenceStateMachine.this.mTargetSsid != null) {
                        Log.d(MessageUtil.TAG, "WiFiProScanResultList is null, get from connected history. mTargetSsid is " + HwIntelligenceStateMachine.this.mTargetSsid + " mAuthType " + HwIntelligenceStateMachine.this.mAuthType);
                        HwIntelligenceStateMachine.this.mApInfoManager.setBlackListBySsid(HwIntelligenceStateMachine.this.mTargetSsid, HwIntelligenceStateMachine.this.mAuthType, true);
                    }
                    HwIntelligenceStateMachine.this.mTargetSsid = null;
                    HwIntelligenceStateMachine.this.mAuthType = -1;
                    HwIntelligenceStateMachine.this.mApInfoManager.resetBlackList(mlist, true);
                }
                HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.stopConnectTimer();
                HwIntelligenceStateMachine.this.initPunishParameter();
            } else {
                Log.e(MessageUtil.TAG, "MSG_WIFI_DISABLE by auto");
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 2:
                case 4:
                    break;
                case 5:
                    Log.e(MessageUtil.TAG, "DisabledState MSG_WIFI_FIND_TARGET");
                    HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                    HwIntelligenceStateMachine.this.sendMessageDelayed(26, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                    break;
                case 7:
                    Log.e(MessageUtil.TAG, " DisabledState MSG_WIFI_UPDATE_SCAN_RESULT");
                    if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                        HwIntelligenceStateMachine.this.mApInfoManager.updateScanResult();
                        break;
                    }
                    break;
                case 20:
                case 23:
                    HwIntelligenceStateMachine.this.mIsScreenOn = HwIntelligenceStateMachine.this.mIsScreenOn(HwIntelligenceStateMachine.this.mContext);
                    String cellid = HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid();
                    Log.e(MessageUtil.TAG, "DisabledState cellid = " + cellid);
                    if (cellid != null) {
                        if (!HwIntelligenceStateMachine.this.mApInfoManager.isMonitorCellId(cellid)) {
                            if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch && HwIntelligenceStateMachine.this.mTargetApInfoDatas != null && HwIntelligenceStateMachine.this.mTargetApInfoDatas.size() > 0) {
                                List<ScanResult> mLists = WifiproUtils.getScanResultsFromWsm();
                                if (mLists != null && mLists.size() > 0 && HwIntelligenceStateMachine.this.mApInfoManager.isHasTargetAp(mLists)) {
                                    Log.d(MessageUtil.TAG, "DisabledState Learn new Cell id");
                                    HwIntelligenceStateMachine.this.mApInfoManager.processScanResult(HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid());
                                    HwIntelligenceStateMachine.this.mApInfoManager.updateScanResult();
                                    break;
                                }
                            }
                            Log.d(MessageUtil.TAG, "current cell id is not monitor ..... cellid = " + cellid);
                            HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
                            HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                            break;
                        }
                        if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose) {
                            HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                        }
                        Log.d(MessageUtil.TAG, "DisabledState current cell id is monitor ..... cellid = " + cellid);
                        HwIntelligenceStateMachine.this.mTargetApInfoDatas = HwIntelligenceStateMachine.this.removeFromBlackList(HwIntelligenceStateMachine.this.mApInfoManager.getMonitorDatas(cellid));
                        if (HwIntelligenceStateMachine.this.mTargetApInfoDatas.size() <= 0) {
                            Log.d(MessageUtil.TAG, "DisabledState mTargetApInfoDatas.size() == 0");
                            HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
                            HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                            break;
                        }
                        Log.d(MessageUtil.TAG, "DisabledState mTargetApInfoDatas.size() =" + HwIntelligenceStateMachine.this.mTargetApInfoDatas.size());
                        if (HwIntelligenceStateMachine.this.getSettingSwitchType() && HwIntelligenceStateMachine.this.mIsScreenOn && Global.getInt(HwIntelligenceStateMachine.this.mContext.getContentResolver(), "wifi_on", 0) == 0) {
                            if (message.what == 20) {
                                HwIntelligenceStateMachine.this.setPingpongPunishTime();
                                if (HwIntelligenceStateMachine.this.isInPingpongPunishTime()) {
                                    Log.d(MessageUtil.TAG, "DisabledState in punish time can not scan");
                                    break;
                                }
                            }
                            Log.d(MessageUtil.TAG, "DisabledState start auto open search");
                            HwIntelligenceStateMachine.this.mIsAutoOpenSearch = true;
                            HwIntelligenceStateMachine.this.mApInfoManager.startScanAp();
                            break;
                        }
                    }
                    break;
                case 26:
                    if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                        HwIntelligenceStateMachine.this.mIsScreenOn = HwIntelligenceStateMachine.this.mIsScreenOn(HwIntelligenceStateMachine.this.mContext);
                        Log.e(MessageUtil.TAG, "MSG_WIFI_HANDLE_OPEN mWifiManager.getWifiState() = " + HwIntelligenceStateMachine.this.mWifiManager.getWifiState() + "  mIsScreenOn = " + HwIntelligenceStateMachine.this.mIsScreenOn);
                        if (HwIntelligenceStateMachine.this.mWifiManager.getWifiState() != 1 || (HwIntelligenceStateMachine.this.mWifiManager.isWifiEnabled() ^ 1) == 0 || !HwIntelligenceStateMachine.this.mIsScreenOn) {
                            if (HwIntelligenceStateMachine.this.mWifiManager.getWifiState() == 0 && HwIntelligenceStateMachine.this.mIsScreenOn) {
                                HwIntelligenceStateMachine.this.sendMessageDelayed(26, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                                break;
                            }
                        }
                        HwIntelligenceStateMachine.this.setAutoOpenValue(true);
                        HwIntelligenceStateMachine.this.mWifiManager.setWifiEnabled(true);
                        HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.startConnectTimer();
                        HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.increaseAutoOpenCount();
                        break;
                    }
                    break;
                case 102:
                    Log.e(MessageUtil.TAG, "CMD_START_SCAN");
                    HwIntelligenceStateMachine.this.mWifiManager.startScan();
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            Log.e(MessageUtil.TAG, "DisconnectedState enter");
            if (HwIntelligenceStateMachine.this.getAutoOpenValue()) {
                HwIntelligenceStateMachine.this.mIsAutoCloseSearch = true;
                HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                HwIntelligenceStateMachine.this.mWifiManager.startScan();
            } else {
                HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
            }
            HwIntelligenceStateMachine.this.mAutoCloseScanTimes = 0;
            HwIntelligenceStateMachine.this.mAutoCloseMessage = 0;
        }

        public void exit() {
            Log.e(MessageUtil.TAG, "DisconnectedState exit");
            HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
            HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
            HwIntelligenceStateMachine.this.mAutoCloseScanTimes = 0;
            HwIntelligenceStateMachine.this.releaseAutoTimer();
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 2:
                    if (HwIntelligenceStateMachine.this.getAutoOpenValue()) {
                        HwIntelligenceStateMachine.this.mIsAutoCloseSearch = true;
                        HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                        HwIntelligenceStateMachine.this.mWifiManager.startScan();
                        break;
                    }
                    break;
                case 7:
                    Log.d(MessageUtil.TAG, "DisconnectedState MSG_WIFI_UPDATE_SCAN_RESULT mIsAutoCloseSearch = " + HwIntelligenceStateMachine.this.mIsAutoCloseSearch + " mIsWaittingAutoClose = " + HwIntelligenceStateMachine.this.mIsWaittingAutoClose);
                    List<ScanResult> mLists;
                    if (!HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                        if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose) {
                            mLists = HwIntelligenceStateMachine.this.mWifiManager.getScanResults();
                            if (mLists.size() > 0 && HwIntelligenceStateMachine.this.mApInfoManager.handleAutoScanResult(mLists)) {
                                Log.d(MessageUtil.TAG, "DisconnectedState MSG_WIFI_UPDATE_SCAN_RESULT remove auto close message");
                                HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                                HwIntelligenceStateMachine.this.mIsAutoCloseSearch = true;
                                HwIntelligenceStateMachine.this.mApInfoManager.processScanResult(HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid());
                                HwIntelligenceStateMachine.this.setAutoTimer(25);
                                break;
                            }
                        }
                    }
                    mLists = HwIntelligenceStateMachine.this.mWifiManager.getScanResults();
                    if (mLists.size() > 0) {
                        if (!HwIntelligenceStateMachine.this.mApInfoManager.handleAutoScanResult(mLists)) {
                            HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                            HwIntelligenceStateMachine.this.mIsWaittingAutoClose = true;
                            Log.w(MessageUtil.TAG, "DisconnectedState first send disable message mAutoCloseMessage = " + HwIntelligenceStateMachine.this.mAutoCloseMessage);
                            HwIntelligenceStateMachine.this.setAutoTimer(9);
                            break;
                        }
                        Log.w(MessageUtil.TAG, "DisconnectedState learn new cell info");
                        HwIntelligenceStateMachine.this.mApInfoManager.processScanResult(HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid());
                        Log.e(MessageUtil.TAG, "DisconnectedState send MSG_WIFI_AUTO_CLOSE_SCAN message mAutoCloseMessage =" + HwIntelligenceStateMachine.this.mAutoCloseMessage);
                        HwIntelligenceStateMachine.this.setAutoTimer(25);
                        break;
                    }
                    HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                    HwIntelligenceStateMachine.this.mIsWaittingAutoClose = true;
                    Log.w(MessageUtil.TAG, "DisconnectedState send disable message mAutoCloseMessage =" + HwIntelligenceStateMachine.this.mAutoCloseMessage);
                    HwIntelligenceStateMachine.this.setAutoTimer(9);
                    break;
                    break;
                case 9:
                    Log.w(MessageUtil.TAG, "MessageUtil.MSG_WIFI_HANDLE_DISABLE mIsWifiP2PConnected = " + HwIntelligenceStateMachine.this.mIsWifiP2PConnected);
                    HwIntelligenceStateMachine.this.releaseAutoTimer();
                    HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                    if (!HwIntelligenceStateMachine.this.mIsWifiP2PConnected) {
                        HwIntelligenceStateMachine.this.autoDisbleWiFi();
                        break;
                    }
                    break;
                case 14:
                    Log.w(MessageUtil.TAG, "DisconnectedState MessageUtil.MSG_WIFI_P2P_CONNECTED");
                    HwIntelligenceStateMachine.this.mIsWifiP2PConnected = true;
                    if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose || HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                        Log.e(MessageUtil.TAG, "DisconnectedState remove MSG_WIFI_HANDLE_DISABLE");
                        HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                        HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                        HwIntelligenceStateMachine.this.releaseAutoTimer();
                        break;
                    }
                case 15:
                    Log.w(MessageUtil.TAG, "MessageUtil.MSG_WIFI_P2P_DISCONNECTED");
                    HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
                    if (HwIntelligenceStateMachine.this.getAutoOpenValue()) {
                        HwIntelligenceStateMachine.this.mIsAutoCloseSearch = true;
                        HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                        HwIntelligenceStateMachine.this.mWifiManager.startScan();
                        break;
                    }
                    break;
                case 23:
                    Log.w(MessageUtil.TAG, "DisconnectedState MessageUtil.MSG_HANDLE_STATE_CHANGE mIsAutoCloseSearch = " + HwIntelligenceStateMachine.this.mIsAutoCloseSearch);
                    if (HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                        HwIntelligenceStateMachine.this.mWifiManager.startScan();
                        break;
                    }
                    break;
                case 25:
                    Log.e(MessageUtil.TAG, "DisconnectedState MSG_WIFI_AUTO_CLOSE_SCAN");
                    HwIntelligenceStateMachine.this.mWifiManager.startScan();
                    break;
                case MessageUtil.MSG_WIFI_CONNECTING /*27*/:
                    if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose || HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                        Log.e(MessageUtil.TAG, "DisconnectedState MSG_WIFI_CONNECTING");
                        HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                        HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                        HwIntelligenceStateMachine.this.releaseAutoTimer();
                        break;
                    }
                default:
                    return false;
            }
            return true;
        }
    }

    class EnabledState extends State {
        EnabledState() {
        }

        public void enter() {
            Log.e(MessageUtil.TAG, "EnabledState");
            HwIntelligenceStateMachine.this.mApInfoManager.resetAllBlackList();
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 3:
                    return true;
                default:
                    return false;
            }
        }
    }

    class InitialState extends State {
        InitialState() {
        }

        public void enter() {
            Log.e(MessageUtil.TAG, "InitialState");
            HwIntelligenceStateMachine.this.mTargetApInfoDatas = null;
            HwIntelligenceStateMachine.this.mIsAutoClose = false;
            HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
            HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
            HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
            HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
            HwIntelligenceStateMachine.this.mIsInitialState = false;
            if (!HwIntelligenceStateMachine.this.mWifiManager.isWifiEnabled() && Global.getInt(HwIntelligenceStateMachine.this.mContext.getContentResolver(), "wifi_on", 0) == 0) {
                Log.e(MessageUtil.TAG, "InitialState wifi is disable");
                HwIntelligenceStateMachine.this.mIsInitialState = true;
                HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mDisabledState);
            }
        }

        public boolean processMessage(Message message) {
            return false;
        }
    }

    class InternetReadyState extends State {
        InternetReadyState() {
        }

        public void enter() {
            boolean bMobileAP = isMobileAP();
            Log.e(MessageUtil.TAG, "mInternetReadyState bMobileAP = " + bMobileAP);
            WifiInfo Info = HwIntelligenceStateMachine.this.mWifiManager.getConnectionInfo();
            if (Info != null && Info.getBSSID() != null && (bMobileAP ^ 1) != 0) {
                HwIntelligenceStateMachine.this.mApInfoManager.addCurrentApInfo(HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid());
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 20:
                case 21:
                    if (!isMobileAP()) {
                        String cellid = HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid();
                        if (cellid != null) {
                            HwIntelligenceStateMachine.this.mApInfoManager.updataApInfo(cellid);
                        }
                    }
                    return true;
                default:
                    return false;
            }
        }

        private boolean isMobileAP() {
            if (HwIntelligenceStateMachine.this.mContext != null) {
                return HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(HwIntelligenceStateMachine.this.mContext);
            }
            return false;
        }
    }

    class NoInternetState extends State {
        NoInternetState() {
        }

        public void enter() {
            Log.e(MessageUtil.TAG, "NoInternetState");
            WifiInfo mConnectInfo = HwIntelligenceStateMachine.this.mWifiManager.getConnectionInfo();
            if (mConnectInfo != null && mConnectInfo.getBSSID() != null) {
                HwIntelligenceStateMachine.this.mApInfoManager.resetBlackListByBssid(mConnectInfo.getBSSID(), true);
            }
        }

        public boolean processMessage(Message message) {
            return false;
        }
    }

    class StopState extends State {
        StopState() {
        }

        public void enter() {
            Log.e(MessageUtil.TAG, "StopState");
            HwIntelligenceStateMachine.this.mTargetApInfoDatas = null;
            HwIntelligenceStateMachine.this.setAutoOpenValue(false);
            HwIntelligenceStateMachine.this.mIsAutoClose = false;
            HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
            HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
            HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
            HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 100:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mInitialState);
                    return true;
                default:
                    return true;
            }
        }
    }

    public static HwIntelligenceStateMachine createIntelligenceStateMachine(Context context, WifiProUIDisplayManager UIManager) {
        if (mHwIntelligenceStateMachine == null) {
            mHwIntelligenceStateMachine = new HwIntelligenceStateMachine(context, UIManager);
        }
        return mHwIntelligenceStateMachine;
    }

    private HwIntelligenceStateMachine(Context context, WifiProUIDisplayManager UIManager) {
        super("HwIntelligenceStateMachine");
        this.mContext = context;
        this.mHandler = getHandler();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mHwintelligenceWiFiCHR = HwintelligenceWiFiCHR.getInstance(this);
        this.mWiFiStateMonitor = new WiFiStateMonitor(context, getHandler());
        this.mCellStateMonitor = new CellStateMonitor(context, getHandler());
        this.mApInfoManager = new ApInfoManager(context, this, getHandler());
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mEnabledState, this.mDefaultState);
        addState(this.mDisabledState, this.mDefaultState);
        addState(this.mConnectedState, this.mEnabledState);
        addState(this.mInternetReadyState, this.mConnectedState);
        addState(this.mNoInternetState, this.mConnectedState);
        addState(this.mDisconnectedState, this.mEnabledState);
        addState(this.mStopState, this.mDefaultState);
        setInitialState(this.mDefaultState);
        start();
    }

    private void setAutoTimer(int message) {
        Log.e(MessageUtil.TAG, "DisconnectedState setAutoTimer message = " + message);
        if (this.mAutoCloseMessage == message) {
            Log.e(MessageUtil.TAG, "DisconnectedState setAutoTimer mAutoCloseMessage == message");
            return;
        }
        if (message == 25) {
            this.mAutoCloseMessage = message;
            if (this.mAutoCloseScanTimes >= 1) {
                Log.e(MessageUtil.TAG, "DisconnectedState setAutoTimer mAutoCloseScanTimes >= 1");
                return;
            }
            Log.e(MessageUtil.TAG, "DisconnectedState setAutoTimer mAutoCloseScanTimes =" + this.mAutoCloseScanTimes);
            this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 120000, MessageUtil.TAG, this.mAutoCloseTimeoutListener, getHandler());
            this.mAutoCloseScanTimes++;
        } else {
            this.mAutoCloseMessage = message;
            this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 60000, MessageUtil.TAG, this.mAutoCloseTimeoutListener, getHandler());
        }
    }

    private void releaseAutoTimer() {
        Log.e(MessageUtil.TAG, "DisconnectedState releaseAutoTimer");
        this.mAutoCloseMessage = 0;
        this.mAlarmManager.cancel(this.mAutoCloseTimeoutListener);
    }

    private boolean mIsScreenOn(Context context) {
        if (((PowerManager) context.getSystemService("power")).isScreenOn()) {
            return true;
        }
        return false;
    }

    private boolean isAirModeOn() {
        boolean z = true;
        if (this.mContext == null) {
            return false;
        }
        if (System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
            z = false;
        }
        return z;
    }

    private boolean isClosedByUser() {
        if (isAirModeOn()) {
            return false;
        }
        return true;
    }

    private void setAutoOpenValue(boolean enable) {
        Log.w(MessageUtil.TAG, "setAutoOpenValue =" + enable);
        System.putInt(this.mContext.getContentResolver(), MessageUtil.WIFIPRO_AUTO_OPEN_STATE, enable ? 1 : 0);
    }

    private boolean getAutoOpenValue() {
        int value = System.getInt(this.mContext.getContentResolver(), MessageUtil.WIFIPRO_AUTO_OPEN_STATE, 0);
        Log.w(MessageUtil.TAG, "getAutoOpenValue  value = " + value);
        return value == 1;
    }

    private List<APInfoData> removeFromBlackList(List<APInfoData> datas) {
        ArrayList<APInfoData> result = new ArrayList();
        for (APInfoData data : datas) {
            if (!data.isInBlackList()) {
                result.add(data);
            }
        }
        return result;
    }

    private boolean getSettingSwitchType() {
        Log.w(MessageUtil.TAG, "getSettingSwitchType in");
        int select = System.getInt(this.mContext.getContentResolver(), MessageUtil.WIFI_CONNECT_TYPE, 0);
        Log.w(MessageUtil.TAG, "getSettingSwitchType select = " + select);
        return select != 1;
    }

    public List<APInfoData> getTargetApInfoDatas() {
        return this.mTargetApInfoDatas;
    }

    private void autoDisbleWiFi() {
        Log.e(MessageUtil.TAG, "autoDisbleWiFi close WIFI");
        this.mIsAutoClose = true;
        setAutoOpenValue(false);
        this.mWifiManager.setWifiEnabled(false);
    }

    public synchronized void onStart() {
        Log.e(MessageUtil.TAG, "onStart mIsMachineStared = " + this.mIsMachineStared);
        if (!this.mIsMachineStared) {
            initPunishParameter();
            this.mIsMachineStared = true;
            getHandler().sendEmptyMessage(100);
            this.mApInfoManager.start();
            this.mWiFiStateMonitor.startMonitor();
            this.mCellStateMonitor.startMonitor();
        }
    }

    public synchronized void onStop() {
        Log.e(MessageUtil.TAG, "onStop mIsMachineStared = " + this.mIsMachineStared);
        if (this.mIsMachineStared) {
            this.mIsMachineStared = false;
            this.mWiFiStateMonitor.stopMonitor();
            this.mCellStateMonitor.stopMonitor();
            this.mApInfoManager.stop();
            getHandler().sendEmptyMessage(101);
        }
    }

    public int getConnectFailedReason() {
        return this.mConnectFailedReason;
    }

    public String getConnectFailedBssid() {
        return this.mConnectFailedBssid;
    }

    public String getConnectFailedSsid() {
        return this.mConnectFailedSsid;
    }

    private void setPingpongPunishTime() {
        if (!this.mApInfoManager.isScaning()) {
            Log.e(MessageUtil.TAG, "setPingpongPunishTime mLastCellChangeScanTime = " + this.mLastCellChangeScanTime);
            if (this.mLastCellChangeScanTime == 0) {
                this.mLastCellChangeScanTime = System.currentTimeMillis();
                return;
            }
            if (System.currentTimeMillis() - this.mLastCellChangeScanTime < 5000) {
                Log.e(MessageUtil.TAG, "setPingpongPunishTime is inPunish time");
                if (this.mLastScanPingpongTime == 0) {
                    this.mScanPingpongNum = 1;
                    this.mLastScanPingpongTime = System.currentTimeMillis();
                } else {
                    if (System.currentTimeMillis() - this.mLastScanPingpongTime > 18000000) {
                        this.mScanPingpongNum = 1;
                        this.mLastScanPingpongTime = System.currentTimeMillis();
                    } else {
                        this.mScanPingpongNum++;
                        this.mLastScanPingpongTime = System.currentTimeMillis();
                    }
                    Log.e(MessageUtil.TAG, "setPingpongPunishTime mScanPingpongNum = " + this.mScanPingpongNum);
                }
            } else {
                Log.e(MessageUtil.TAG, "setPingpongPunishTime is not inPunish time");
            }
            this.mLastCellChangeScanTime = System.currentTimeMillis();
        }
    }

    private boolean isInPingpongPunishTime() {
        Log.e(MessageUtil.TAG, "isInPingpongPunishTime mScanPingpongNum = " + this.mScanPingpongNum);
        int punishTime = this.mScanPingpongNum * 30000;
        if (punishTime > 300000) {
            punishTime = 300000;
        }
        if (System.currentTimeMillis() - this.mLastScanPingpongTime < ((long) punishTime)) {
            Log.e(MessageUtil.TAG, "isInPingpongPunishTime punishTime = " + punishTime);
            return true;
        }
        Log.e(MessageUtil.TAG, "isInPingpongPunishTime is not in punishTime");
        return false;
    }

    public void initPunishParameter() {
        this.mScanPingpongNum = 0;
        this.mLastCellChangeScanTime = 0;
        this.mLastScanPingpongTime = 0;
    }

    public CellStateMonitor getCellStateMonitor() {
        return this.mCellStateMonitor;
    }
}
