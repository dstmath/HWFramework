package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.wifi.ScanResult;
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
import com.android.server.wifi.wifipro.PortalAutoFillManager;
import com.android.server.wifi.wifipro.WifiProUIDisplayManager;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieDataBaseImpl;
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
    private int mAutoCloseMessage;
    private int mAutoCloseScanTimes;
    OnAlarmListener mAutoCloseTimeoutListener;
    private CellStateMonitor mCellStateMonitor;
    private String mConnectFailedBssid;
    private int mConnectFailedReason;
    private String mConnectFailedSsid;
    private State mConnectedState;
    private Context mContext;
    private State mDefaultState;
    private State mDisabledState;
    private State mDisconnectedState;
    private State mEnabledState;
    private Handler mHandler;
    private HwintelligenceWiFiCHR mHwintelligenceWiFiCHR;
    private State mInitialState;
    private State mInternetReadyState;
    private boolean mIsAutoClose;
    private boolean mIsAutoCloseSearch;
    private boolean mIsAutoOpenSearch;
    private boolean mIsInitialState;
    private boolean mIsMachineStared;
    private boolean mIsScreenOn;
    private boolean mIsWaittingAutoClose;
    private boolean mIsWifiP2PConnected;
    private long mLastCellChangeScanTime;
    private long mLastScanPingpongTime;
    private State mNoInternetState;
    private int mScanPingpongNum;
    private State mStopState;
    private List<APInfoData> mTargetApInfoDatas;
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
            HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.stopConnectTimer();
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case WifiScanGenieController.MSG_CONFIGURED_CHANGED /*1*/:
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_CONNECTED /*11*/:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mInternetReadyState);
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_DISCONNECTED /*12*/:
                    Log.e(MessageUtil.TAG, "ConnectedState MSG_WIFI_INTERNET_DISCONNECTED");
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mNoInternetState);
                    break;
                case MessageUtil.MSG_WIFI_IS_PORTAL /*13*/:
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
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case WifiScanGenieController.MSG_CONFIGURED_CHANGED /*1*/:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mConnectedState);
                    break;
                case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mDisconnectedState);
                    break;
                case MessageUtil.MSG_WIFI_ENABLED /*3*/:
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
                case MessageUtil.MSG_WIFI_DISABLE /*4*/:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mDisabledState);
                    break;
                case MessageUtil.MSG_WIFI_FIND_TARGET /*5*/:
                case MessageUtil.MSG_WIFI_UPDATE_SCAN_RESULT /*7*/:
                case MessageUtil.MSG_WIFI_HANDLE_DISABLE /*9*/:
                case MessageUtil.MSG_WIFI_AUTO_OPEN /*10*/:
                case MessageUtil.MSG_WIFI_INTERNET_CONNECTED /*11*/:
                case MessageUtil.MSG_WIFI_INTERNET_DISCONNECTED /*12*/:
                case MessageUtil.MSG_WIFI_IS_PORTAL /*13*/:
                case MessageUtil.MSG_CELL_CHANGE /*20*/:
                case MessageUtil.MSG_WIFI_AUTO_CLOSE_SCAN /*25*/:
                    Log.e(MessageUtil.TAG, " DefaultState message.what = " + message.what);
                    break;
                case MessageUtil.MSG_WIFI_CONFIG_CHANGED /*8*/:
                    Bundle data = message.getData();
                    String bssid = data.getString(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_BSSID);
                    String ssid = data.getString(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_SSID);
                    Log.e(MessageUtil.TAG, "MSG_WIFI_CONFIG_CHANGED bssid = " + bssid + " ssid = " + ssid);
                    if (ssid == null) {
                        HwIntelligenceStateMachine.this.mApInfoManager.delectApInfoByBssid(bssid);
                        break;
                    }
                    HwIntelligenceStateMachine.this.mApInfoManager.delectApInfoBySsid(ssid);
                    break;
                case MessageUtil.MSG_WIFI_P2P_CONNECTED /*14*/:
                    HwIntelligenceStateMachine.this.mIsWifiP2PConnected = true;
                    break;
                case MessageUtil.MSG_WIFI_P2P_DISCONNECTED /*15*/:
                    HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
                    break;
                case MessageUtil.MSG_SCREEN_ON /*21*/:
                    Log.e(MessageUtil.TAG, " DefaultState MSG_SCREEN_ON");
                    HwIntelligenceStateMachine.this.sendMessage(23);
                    break;
                case MessageUtil.MSG_SCREEN_OFF /*22*/:
                    Log.e(MessageUtil.TAG, " DefaultState MSG_SCREEN_OFF mIsAutoOpenSearch = " + HwIntelligenceStateMachine.this.mIsAutoOpenSearch);
                    if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                        HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                        break;
                    }
                    break;
                case MessageUtil.MSG_CONNECT_FAILED /*24*/:
                    Bundle mData = message.getData();
                    HwIntelligenceStateMachine.this.mConnectFailedReason = mData.getInt(MessageUtil.MSG_KEY_REASON);
                    HwIntelligenceStateMachine.this.mConnectFailedBssid = mData.getString(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_BSSID);
                    HwIntelligenceStateMachine.this.mConnectFailedSsid = mData.getString(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_SSID);
                    Log.e(MessageUtil.TAG, "MSG_CONNECT_FAILED ssid = " + HwIntelligenceStateMachine.this.mConnectFailedSsid + " bssid = " + HwIntelligenceStateMachine.this.mConnectFailedBssid + " mConnectFailedReason = " + HwIntelligenceStateMachine.this.mConnectFailedReason);
                    break;
                case MessageUtil.CMD_ON_START /*100*/:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mInitialState);
                    break;
                case MessageUtil.CMD_ON_STOP /*101*/:
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
                    List<ScanResult> mlist;
                    HwIntelligenceStateMachine.this.setAutoOpenValue(false);
                    HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
                    HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                    HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                    if (HwIntelligenceStateMachine.this.mWifiManager.isScanAlwaysAvailable()) {
                        mlist = HwIntelligenceStateMachine.this.mWifiManager.getScanResults();
                    } else {
                        mlist = HwIntelligenceWiFiManager.getWiFiProScanResultList();
                    }
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
                case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                case MessageUtil.MSG_WIFI_DISABLE /*4*/:
                    break;
                case MessageUtil.MSG_WIFI_FIND_TARGET /*5*/:
                    Log.e(MessageUtil.TAG, "DisabledState MSG_WIFI_FIND_TARGET");
                    HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                    HwIntelligenceStateMachine.this.sendMessageDelayed(26, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                    break;
                case MessageUtil.MSG_WIFI_UPDATE_SCAN_RESULT /*7*/:
                    Log.e(MessageUtil.TAG, " DisabledState MSG_WIFI_UPDATE_SCAN_RESULT");
                    if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                        HwIntelligenceStateMachine.this.mApInfoManager.updateScanResult();
                        break;
                    }
                    break;
                case MessageUtil.MSG_CELL_CHANGE /*20*/:
                case MessageUtil.MSG_HANDLE_STATE_CHANGE /*23*/:
                    HwIntelligenceStateMachine.this.mIsScreenOn = HwIntelligenceStateMachine.this.mIsScreenOn(HwIntelligenceStateMachine.this.mContext);
                    String cellid = HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid();
                    Log.e(MessageUtil.TAG, "DisabledState cellid = " + cellid);
                    if (cellid != null) {
                        if (!HwIntelligenceStateMachine.this.mApInfoManager.isMonitorCellId(cellid)) {
                            if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch && HwIntelligenceStateMachine.this.mTargetApInfoDatas != null && HwIntelligenceStateMachine.this.mTargetApInfoDatas.size() > 0) {
                                List<ScanResult> mLists = HwIntelligenceStateMachine.this.mWifiManager.getScanResults();
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
                case MessageUtil.MSG_WIFI_HANDLE_OPEN /*26*/:
                    if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                        HwIntelligenceStateMachine.this.mIsScreenOn = HwIntelligenceStateMachine.this.mIsScreenOn(HwIntelligenceStateMachine.this.mContext);
                        Log.e(MessageUtil.TAG, "MSG_WIFI_HANDLE_OPEN mWifiManager.getWifiState() = " + HwIntelligenceStateMachine.this.mWifiManager.getWifiState() + "  mIsScreenOn = " + HwIntelligenceStateMachine.this.mIsScreenOn);
                        if (HwIntelligenceStateMachine.this.mWifiManager.getWifiState() != 1 || HwIntelligenceStateMachine.this.mWifiManager.isWifiEnabled() || !HwIntelligenceStateMachine.this.mIsScreenOn) {
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
                case MessageUtil.CMD_START_SCAN /*102*/:
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
                case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                    break;
                case MessageUtil.MSG_WIFI_UPDATE_SCAN_RESULT /*7*/:
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
                case MessageUtil.MSG_WIFI_HANDLE_DISABLE /*9*/:
                    Log.w(MessageUtil.TAG, "MessageUtil.MSG_WIFI_HANDLE_DISABLE mIsWifiP2PConnected = " + HwIntelligenceStateMachine.this.mIsWifiP2PConnected);
                    HwIntelligenceStateMachine.this.releaseAutoTimer();
                    HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                    if (!HwIntelligenceStateMachine.this.mIsWifiP2PConnected) {
                        HwIntelligenceStateMachine.this.autoDisbleWiFi();
                        break;
                    }
                    break;
                case MessageUtil.MSG_WIFI_P2P_CONNECTED /*14*/:
                    Log.w(MessageUtil.TAG, "DisconnectedState MessageUtil.MSG_WIFI_P2P_CONNECTED");
                    HwIntelligenceStateMachine.this.mIsWifiP2PConnected = true;
                    if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose || HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                        Log.e(MessageUtil.TAG, "DisconnectedState remove MSG_WIFI_HANDLE_DISABLE");
                        HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                        HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                        HwIntelligenceStateMachine.this.releaseAutoTimer();
                        break;
                    }
                case MessageUtil.MSG_WIFI_P2P_DISCONNECTED /*15*/:
                    Log.w(MessageUtil.TAG, "MessageUtil.MSG_WIFI_P2P_DISCONNECTED");
                    HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
                    if (HwIntelligenceStateMachine.this.getAutoOpenValue()) {
                        HwIntelligenceStateMachine.this.mIsAutoCloseSearch = true;
                        HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                        HwIntelligenceStateMachine.this.mWifiManager.startScan();
                        break;
                    }
                    break;
                case MessageUtil.MSG_HANDLE_STATE_CHANGE /*23*/:
                    Log.w(MessageUtil.TAG, "DisconnectedState MessageUtil.MSG_HANDLE_STATE_CHANGE mIsAutoCloseSearch = " + HwIntelligenceStateMachine.this.mIsAutoCloseSearch);
                    if (HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                        HwIntelligenceStateMachine.this.mWifiManager.startScan();
                        break;
                    }
                    break;
                case MessageUtil.MSG_WIFI_AUTO_CLOSE_SCAN /*25*/:
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
                case MessageUtil.MSG_WIFI_ENABLED /*3*/:
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
            if (Info != null && Info.getBSSID() != null && !bMobileAP) {
                HwIntelligenceStateMachine.this.mApInfoManager.addCurrentApInfo(HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid());
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case MessageUtil.MSG_CELL_CHANGE /*20*/:
                case MessageUtil.MSG_SCREEN_ON /*21*/:
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
                case MessageUtil.CMD_ON_START /*100*/:
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
        this.mAutoCloseMessage = 0;
        this.mAutoCloseScanTimes = 0;
        this.mIsAutoClose = false;
        this.mIsAutoOpenSearch = false;
        this.mIsAutoCloseSearch = false;
        this.mIsWaittingAutoClose = false;
        this.mIsScreenOn = false;
        this.mIsWifiP2PConnected = false;
        this.mIsMachineStared = false;
        this.mIsInitialState = false;
        this.mConnectFailedReason = -1;
        this.mConnectFailedBssid = null;
        this.mConnectFailedSsid = null;
        this.mDefaultState = new DefaultState();
        this.mInitialState = new InitialState();
        this.mConnectedState = new ConnectedState();
        this.mDisconnectedState = new DisconnectedState();
        this.mEnabledState = new EnabledState();
        this.mDisabledState = new DisabledState();
        this.mInternetReadyState = new InternetReadyState();
        this.mNoInternetState = new NoInternetState();
        this.mStopState = new StopState();
        this.mScanPingpongNum = 0;
        this.mLastCellChangeScanTime = 0;
        this.mLastScanPingpongTime = 0;
        this.mAutoCloseTimeoutListener = new OnAlarmListener() {
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
            getHandler().sendEmptyMessage(MessageUtil.CMD_ON_STOP);
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
        int punishTime = this.mScanPingpongNum * PING_PONG_PUNISH_TIME;
        if (punishTime > PING_PONG_MAX_PUNISH_TIME) {
            punishTime = PING_PONG_MAX_PUNISH_TIME;
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
}
