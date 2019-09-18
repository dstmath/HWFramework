package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.app.AlarmManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.LocalServices;
import com.android.server.policy.AbsPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wifi.HwQoE.HidataWechatTraffic;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.wifipro.WifiProUIDisplayManager;
import com.android.server.wifi.wifipro.WifiproUtils;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.List;

public class HwIntelligenceStateMachine extends StateMachine {
    private static final String ACTION_WIFI_PRO_TIMER = "android.net.wifi.wifi_pro_timer";
    private static final String COUNTRY_CODE_CN = "460";
    private static final int INITIAL_CONNECT_WIFI_INTERVAL_TIME = 3600000;
    private static final int LOCATION_AVAILABLE_TIME = 30000;
    private static final int OPEN_CONNECT_WIFI_INTERVAL_TIME = 60000;
    private static final int PING_PONG_HOME_MAX_PUNISH_TIME = 60000;
    private static final int PING_PONG_INTERVAL_TIME = 1800000;
    private static final int PING_PONG_MAX_PUNISH_TIME = 300000;
    private static final int PING_PONG_PUNISH_TIME = 30000;
    private static final int PING_PONG_TIME = 5000;
    private static final int UPLOAD_AUTO_OPEN_WIFI_FAILED_INTERVAL_TIME = 43200000;
    private static final int UPLOAD_NO_HOMEADRESS_INTERVAL_TIME = 604800000;
    private static final int WIFI_PRO_TIMER = 0;
    private static HwIntelligenceStateMachine mHwIntelligenceStateMachine;
    private AlarmManager mAlarmManager;
    /* access modifiers changed from: private */
    public ApInfoManager mApInfoManager;
    /* access modifiers changed from: private */
    public int mAuthType = -1;
    /* access modifiers changed from: private */
    public int mAutoCloseMessage = 0;
    /* access modifiers changed from: private */
    public int mAutoCloseScanTimes = 0;
    AlarmManager.OnAlarmListener mAutoCloseTimeoutListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            Log.w(MessageUtil.TAG, "receive auto close message mAutoCloseMessage = " + HwIntelligenceStateMachine.this.mAutoCloseMessage);
            if (HwIntelligenceStateMachine.this.mAutoCloseMessage == 25) {
                HwIntelligenceStateMachine.this.mHandler.sendEmptyMessage(25);
            } else if (HwIntelligenceStateMachine.this.mAutoCloseMessage == 9) {
                HwIntelligenceStateMachine.this.mHandler.sendEmptyMessage(9);
            }
            int unused = HwIntelligenceStateMachine.this.mAutoCloseMessage = 0;
        }
    };
    /* access modifiers changed from: private */
    public boolean mAutoOpenWifiWaitLocation = false;
    private BroadcastReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public CellStateMonitor mCellStateMonitor;
    /* access modifiers changed from: private */
    public String mConnectFailedBssid = null;
    /* access modifiers changed from: private */
    public int mConnectFailedReason = -1;
    /* access modifiers changed from: private */
    public String mConnectFailedSsid = null;
    /* access modifiers changed from: private */
    public State mConnectedState = new ConnectedState();
    /* access modifiers changed from: private */
    public Context mContext;
    private State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public State mDisabledState = new DisabledState();
    /* access modifiers changed from: private */
    public State mDisconnectedState = new DisconnectedState();
    /* access modifiers changed from: private */
    public State mEnabledState = new EnabledState();
    /* access modifiers changed from: private */
    public long mEnabledStateTime = 0;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public LocationAddress mHomeAddress = null;
    /* access modifiers changed from: private */
    public HomeAddressDataManager mHomeAddressManager;
    /* access modifiers changed from: private */
    public HwintelligenceWiFiCHR mHwintelligenceWiFiCHR;
    /* access modifiers changed from: private */
    public State mInitialState = new InitialState();
    /* access modifiers changed from: private */
    public long mInitialStateTime = 0;
    private IntentFilter mIntentFilter;
    /* access modifiers changed from: private */
    public State mInternetReadyState = new InternetReadyState();
    /* access modifiers changed from: private */
    public long mInternetReadyStateTime = 0;
    /* access modifiers changed from: private */
    public boolean mIsAutoClose = false;
    /* access modifiers changed from: private */
    public boolean mIsAutoCloseSearch = false;
    /* access modifiers changed from: private */
    public boolean mIsAutoOpenSearch = false;
    /* access modifiers changed from: private */
    public boolean mIsInitialState = false;
    private boolean mIsMachineStared = false;
    /* access modifiers changed from: private */
    public boolean mIsOversea = false;
    /* access modifiers changed from: private */
    public boolean mIsScreenOn = false;
    /* access modifiers changed from: private */
    public boolean mIsWaittingAutoClose = false;
    /* access modifiers changed from: private */
    public boolean mIsWifiP2PConnected = false;
    private long mLastCellChangeScanTime = 0;
    /* access modifiers changed from: private */
    public LocationAddress mLastLocationAddress = null;
    /* access modifiers changed from: private */
    public long mLastScanPingpongTime = 0;
    /* access modifiers changed from: private */
    public long mLocationRequestFailed = 0;
    /* access modifiers changed from: private */
    public State mNoInternetState = new NoInternetState();
    /* access modifiers changed from: private */
    public boolean mNotInBlacklist = false;
    /* access modifiers changed from: private */
    public int mScanPingpongNum = 0;
    /* access modifiers changed from: private */
    public int mSmartSceneOn = 0;
    /* access modifiers changed from: private */
    public State mStopState = new StopState();
    /* access modifiers changed from: private */
    public List<APInfoData> mTargetApInfoDatas;
    /* access modifiers changed from: private */
    public String mTargetSsid = null;
    private long mUploadAutoOpenWifiFailedTime = 0;
    private WiFiStateMonitor mWiFiStateMonitor;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    private long uploadIntervalTime = 0;

    class ConnectedState extends State {
        ConnectedState() {
        }

        public void enter() {
            Log.e(MessageUtil.TAG, "ConnectedState");
            if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose) {
                boolean unused = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                Log.e(MessageUtil.TAG, "ConnectedState remove MSG_WIFI_HANDLE_DISABLE");
                HwintelligenceWiFiCHR access$2100 = HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR;
                HwintelligenceWiFiCHR unused2 = HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR;
                access$2100.uploadAutoCloseFailed(1);
            }
            if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                boolean unused3 = HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
                HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                if (HwIntelligenceStateMachine.this.mTargetApInfoDatas != null) {
                    HwIntelligenceStateMachine.this.mTargetApInfoDatas.clear();
                    List unused4 = HwIntelligenceStateMachine.this.mTargetApInfoDatas = null;
                }
            }
            updateConnectedInfo();
            HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.stopConnectTimer();
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 1) {
                switch (i) {
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
            } else {
                updateConnectedInfo();
            }
            return true;
        }

        private void updateConnectedInfo() {
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(HwIntelligenceStateMachine.this.mWifiManager);
            if (config != null) {
                String unused = HwIntelligenceStateMachine.this.mTargetSsid = config.SSID;
                if (config.allowedKeyManagement.cardinality() <= 1) {
                    int unused2 = HwIntelligenceStateMachine.this.mAuthType = config.getAuthType();
                } else {
                    int unused3 = HwIntelligenceStateMachine.this.mAuthType = -1;
                }
                Log.d(MessageUtil.TAG, "mTargetSsid is " + HwIntelligenceStateMachine.this.mTargetSsid + " mAuthType " + HwIntelligenceStateMachine.this.mAuthType);
            }
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            switch (i) {
                case 1:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mConnectedState);
                    break;
                case 2:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mDisconnectedState);
                    break;
                case 3:
                    int wifiEnableFlag = Settings.Global.getInt(HwIntelligenceStateMachine.this.mContext.getContentResolver(), "wifi_on", 0);
                    Log.e(MessageUtil.TAG, "MSG_WIFI_ENABLED wifiEnableFlag = " + wifiEnableFlag + " mIsAutoOpenSearch =" + HwIntelligenceStateMachine.this.mIsAutoOpenSearch);
                    if (wifiEnableFlag != 1 && wifiEnableFlag != 2) {
                        if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                            Log.e(MessageUtil.TAG, "MSG_WIFI_ENABLED start scan");
                            HwIntelligenceStateMachine.this.mWifiManager.startScan();
                            break;
                        }
                    } else {
                        HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mEnabledState);
                        break;
                    }
                    break;
                case 4:
                    HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mDisabledState);
                    break;
                case 5:
                    Log.e(MessageUtil.TAG, " DefaultState message.what = " + message.what);
                    break;
                default:
                    switch (i) {
                        case 7:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                            break;
                        case 8:
                            Bundle data = message.getData();
                            String bssid = data.getString("bssid");
                            String ssid = data.getString("ssid");
                            Log.e(MessageUtil.TAG, "MSG_WIFI_CONFIG_CHANGED ssid = " + ssid);
                            if (ssid == null) {
                                HwIntelligenceStateMachine.this.mApInfoManager.delectApInfoByBssid(bssid);
                                break;
                            } else {
                                HwIntelligenceStateMachine.this.mApInfoManager.delectApInfoBySsid(ssid);
                                break;
                            }
                        case 14:
                            boolean unused = HwIntelligenceStateMachine.this.mIsWifiP2PConnected = true;
                            break;
                        case 15:
                            boolean unused2 = HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
                            break;
                        default:
                            switch (i) {
                                case 20:
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
                                default:
                                    switch (i) {
                                        case 24:
                                            Bundle mData = message.getData();
                                            int unused3 = HwIntelligenceStateMachine.this.mConnectFailedReason = mData.getInt("reason");
                                            String unused4 = HwIntelligenceStateMachine.this.mConnectFailedBssid = mData.getString("bssid");
                                            String unused5 = HwIntelligenceStateMachine.this.mConnectFailedSsid = mData.getString("ssid");
                                            Log.e(MessageUtil.TAG, "MSG_CONNECT_FAILED ssid = " + HwIntelligenceStateMachine.this.mConnectFailedSsid + " mConnectFailedReason = " + HwIntelligenceStateMachine.this.mConnectFailedReason);
                                            break;
                                        case 25:
                                            break;
                                        default:
                                            switch (i) {
                                                case MessageUtil.MSG_UPDATE_LOCATION:
                                                case 30:
                                                case MessageUtil.MSG_GET_LOCATION_FAIL:
                                                case MessageUtil.MSG_UPDATE_TARGET_SSID:
                                                    break;
                                                default:
                                                    switch (i) {
                                                        case 100:
                                                            HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mInitialState);
                                                            break;
                                                        case 101:
                                                            HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mStopState);
                                                            break;
                                                    }
                                            }
                                    }
                            }
                    }
                    Log.e(MessageUtil.TAG, " DefaultState message.what = " + message.what);
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
            int unused = HwIntelligenceStateMachine.this.mSmartSceneOn = 0;
            if (HwIntelligenceStateMachine.this.mIsInitialState) {
                Log.e(MessageUtil.TAG, "mIsInitialState state is disable");
                boolean unused2 = HwIntelligenceStateMachine.this.mIsInitialState = false;
            } else if (!HwIntelligenceStateMachine.this.isClosedByUser()) {
                Log.e(MessageUtil.TAG, "MSG_WIFI_DISABLE by auto");
            } else {
                if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose) {
                    Log.e(MessageUtil.TAG, "DisabledState remove MSG_WIFI_HANDLE_DISABLE");
                    boolean unused3 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                    HwintelligenceWiFiCHR access$2100 = HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR;
                    HwintelligenceWiFiCHR unused4 = HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR;
                    access$2100.uploadAutoCloseFailed(2);
                }
                if (HwIntelligenceStateMachine.this.mIsAutoClose) {
                    boolean unused5 = HwIntelligenceStateMachine.this.mIsAutoClose = false;
                    HwIntelligenceStateMachine.this.setAutoOpenValue(false);
                    if (!HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                        boolean unused6 = HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
                        boolean unused7 = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                        HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                    }
                    HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.increaseAutoCloseCount();
                } else if (HwIntelligenceStateMachine.this.isScreenOn(HwIntelligenceStateMachine.this.mContext)) {
                    HwIntelligenceStateMachine.this.setAutoOpenValue(false);
                    boolean unused8 = HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
                    boolean unused9 = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                    HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                    List<ScanResult> mlist = null;
                    if (HwIntelligenceStateMachine.this.mWifiManager.isScanAlwaysAvailable()) {
                        mlist = WifiproUtils.getScanResultsFromWsm();
                    }
                    if (mlist == null || mlist.size() == 0) {
                        Log.d(MessageUtil.TAG, "getScanResultsFromWsm is null, get from WiFiProScanResultList.");
                        mlist = HwIntelligenceWiFiManager.getWiFiProScanResultList();
                    }
                    if ((mlist == null || mlist.size() == 0) && HwIntelligenceStateMachine.this.mTargetSsid != null) {
                        Log.d(MessageUtil.TAG, "WiFiProScanResultList is null, get from connected history. mTargetSsid is " + HwIntelligenceStateMachine.this.mTargetSsid + " mAuthType " + HwIntelligenceStateMachine.this.mAuthType);
                        HwIntelligenceStateMachine.this.mApInfoManager.setBlackListBySsid(HwIntelligenceStateMachine.this.mTargetSsid, HwIntelligenceStateMachine.this.mAuthType, true);
                    }
                    String unused10 = HwIntelligenceStateMachine.this.mTargetSsid = null;
                    int unused11 = HwIntelligenceStateMachine.this.mAuthType = -1;
                    HwIntelligenceStateMachine.this.mApInfoManager.resetBlackList(mlist, true);
                    HwIntelligenceWiFiManager.setWiFiProScanResultList(null);
                }
                HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.stopConnectTimer();
                HwIntelligenceStateMachine.this.initPunishParameter();
            }
        }

        public void exit() {
            Log.d(MessageUtil.TAG, "DisabledState exit");
            if (HwIntelligenceStateMachine.this.hasMessages(29)) {
                HwIntelligenceStateMachine.this.removeMessages(29);
            }
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0086, code lost:
            if (com.android.server.wifi.wifipro.hwintelligencewifi.HwIntelligenceStateMachine.access$5000(r0.this$0) != false) goto L_0x03c5;
         */
        public boolean processMessage(Message message) {
            Message message2 = message;
            int i = message2.what;
            if (i != 2) {
                if (i != 7) {
                    if (!(i == 20 || i == 23)) {
                        if (i != 26) {
                            if (i != 102) {
                                switch (i) {
                                    case 4:
                                        break;
                                    case 5:
                                        Log.e(MessageUtil.TAG, "DisabledState MSG_WIFI_FIND_TARGET");
                                        HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                                        if (!HwIntelligenceStateMachine.this.mIsOversea && !HwIntelligenceStateMachine.this.isLocationAvaliable(HwIntelligenceStateMachine.this.mLastLocationAddress)) {
                                            HwIntelligenceStateMachine.this.requestLocationInfo();
                                            boolean unused = HwIntelligenceStateMachine.this.mAutoOpenWifiWaitLocation = true;
                                            int unused2 = HwIntelligenceStateMachine.this.mScanPingpongNum = 1;
                                            HwIntelligenceStateMachine.this.sendMessageDelayed(29, 3000);
                                            break;
                                        } else {
                                            HwIntelligenceStateMachine.this.sendMessageDelayed(26, 3000);
                                            break;
                                        }
                                        break;
                                    default:
                                        switch (i) {
                                            case MessageUtil.MSG_CONFIGURATION_CHANGED:
                                                break;
                                            case MessageUtil.MSG_UPDATE_LOCATION:
                                            case MessageUtil.MSG_GET_LOCATION_FAIL:
                                                if (HwIntelligenceStateMachine.this.mAutoOpenWifiWaitLocation) {
                                                    LocationAddress unused3 = HwIntelligenceStateMachine.this.mLastLocationAddress = null;
                                                    boolean unused4 = HwIntelligenceStateMachine.this.mAutoOpenWifiWaitLocation = false;
                                                    HwIntelligenceStateMachine.this.sendMessage(26);
                                                    break;
                                                }
                                                break;
                                            case 30:
                                                Bundle data = message.getData();
                                                if (data != null) {
                                                    HwIntelligenceStateMachine hwIntelligenceStateMachine = HwIntelligenceStateMachine.this;
                                                    LocationAddress locationAddress = new LocationAddress(data.getDouble(HomeAddressDataManager.LATITUDE_KEY), data.getDouble(HomeAddressDataManager.LONGITUDE_KEY), data.getDouble(HomeAddressDataManager.DISTANCE_KEY), Long.valueOf(System.currentTimeMillis()));
                                                    LocationAddress unused5 = hwIntelligenceStateMachine.mLastLocationAddress = locationAddress;
                                                }
                                                if (HwIntelligenceStateMachine.this.mAutoOpenWifiWaitLocation) {
                                                    boolean unused6 = HwIntelligenceStateMachine.this.mAutoOpenWifiWaitLocation = false;
                                                    HwIntelligenceStateMachine.this.sendMessage(26);
                                                    break;
                                                }
                                                break;
                                            default:
                                                return false;
                                        }
                                }
                            } else {
                                Log.e(MessageUtil.TAG, "CMD_START_SCAN");
                                HwIntelligenceStateMachine.this.mWifiManager.startScan();
                            }
                        } else if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                            boolean unused7 = HwIntelligenceStateMachine.this.mIsScreenOn = HwIntelligenceStateMachine.this.isScreenOn(HwIntelligenceStateMachine.this.mContext);
                            Log.d(MessageUtil.TAG, "MSG_WIFI_HANDLE_OPEN mWifiManager.getWifiState() = " + HwIntelligenceStateMachine.this.mWifiManager.getWifiState() + "  mIsScreenOn = " + HwIntelligenceStateMachine.this.mIsScreenOn + "  mIsFullScreen  " + HwIntelligenceStateMachine.this.isFullScreen() + " mIsOversea = " + HwIntelligenceStateMachine.this.mIsOversea);
                            if (HwIntelligenceStateMachine.this.mLastLocationAddress != null) {
                                Log.d(MessageUtil.TAG, "mLastLocationAddress.isHome = " + HwIntelligenceStateMachine.this.mLastLocationAddress.isHome());
                            }
                            if (HwIntelligenceStateMachine.this.mWifiManager.getWifiState() == 1 && !HwIntelligenceStateMachine.this.mWifiManager.isWifiEnabled() && HwIntelligenceStateMachine.this.mIsScreenOn && !HwIntelligenceStateMachine.this.isFullScreen() && (HwIntelligenceStateMachine.this.mIsOversea || HwIntelligenceStateMachine.this.mLastLocationAddress == null || HwIntelligenceStateMachine.this.mLastLocationAddress.isHome())) {
                                HwIntelligenceStateMachine.this.setAutoOpenValue(true);
                                HwIntelligenceStateMachine.this.mWifiManager.setWifiEnabled(true);
                                HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.startConnectTimer();
                                HwIntelligenceStateMachine.this.mHwintelligenceWiFiCHR.increaseAutoOpenCount();
                            } else if (HwIntelligenceStateMachine.this.mWifiManager.getWifiState() == 0 && HwIntelligenceStateMachine.this.mIsScreenOn && !HwIntelligenceStateMachine.this.isFullScreen()) {
                                HwIntelligenceStateMachine.this.sendMessageDelayed(26, 3000);
                            }
                        }
                    }
                    boolean unused8 = HwIntelligenceStateMachine.this.mIsScreenOn = HwIntelligenceStateMachine.this.isScreenOn(HwIntelligenceStateMachine.this.mContext);
                    String cellid = HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid();
                    Log.e(MessageUtil.TAG, "DisabledState cellid = " + cellid);
                    if (cellid != null) {
                        if (HwIntelligenceStateMachine.this.mApInfoManager.isMonitorCellId(cellid)) {
                            if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose) {
                                boolean unused9 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                            }
                            boolean unused10 = HwIntelligenceStateMachine.this.mNotInBlacklist = false;
                            int unused11 = HwIntelligenceStateMachine.this.mSmartSceneOn = 0;
                            Log.d(MessageUtil.TAG, "DisabledState current cell id is monitor ..... cellid = " + cellid);
                            List unused12 = HwIntelligenceStateMachine.this.mTargetApInfoDatas = HwIntelligenceStateMachine.this.removeFromBlackList(HwIntelligenceStateMachine.this.mApInfoManager.getMonitorDatas(cellid));
                            if (!HwIntelligenceStateMachine.this.mNotInBlacklist) {
                                int unused13 = HwIntelligenceStateMachine.this.mSmartSceneOn = 3;
                            }
                            if (HwIntelligenceStateMachine.this.mTargetApInfoDatas.size() > 0) {
                                Log.d(MessageUtil.TAG, "DisabledState mTargetApInfoDatas.size() =" + HwIntelligenceStateMachine.this.mTargetApInfoDatas.size());
                                if (HwIntelligenceStateMachine.this.getSettingSwitchType() && HwIntelligenceStateMachine.this.mIsScreenOn && Settings.Global.getInt(HwIntelligenceStateMachine.this.mContext.getContentResolver(), "wifi_on", 0) == 0) {
                                    if (message2.what == 20) {
                                        HwIntelligenceStateMachine.this.setPingpongPunishTime();
                                        if (HwIntelligenceStateMachine.this.isInPingpongPunishTime()) {
                                            int unused14 = HwIntelligenceStateMachine.this.mSmartSceneOn = 2;
                                            Log.d(MessageUtil.TAG, "DisabledState in punish time can not scan");
                                        } else {
                                            long unused15 = HwIntelligenceStateMachine.this.mLastScanPingpongTime = System.currentTimeMillis();
                                        }
                                    }
                                    Log.d(MessageUtil.TAG, "DisabledState start auto open search");
                                    boolean unused16 = HwIntelligenceStateMachine.this.mIsAutoOpenSearch = true;
                                    HwIntelligenceStateMachine.this.mApInfoManager.startScanAp();
                                }
                            } else {
                                Log.d(MessageUtil.TAG, "DisabledState mTargetApInfoDatas.size() == 0");
                                boolean unused17 = HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
                                HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                            }
                        } else {
                            if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch && HwIntelligenceStateMachine.this.mTargetApInfoDatas != null && HwIntelligenceStateMachine.this.mTargetApInfoDatas.size() > 0) {
                                List<ScanResult> mLists = WifiproUtils.getScanResultsFromWsm();
                                if (mLists != null && mLists.size() > 0 && HwIntelligenceStateMachine.this.mApInfoManager.isHasTargetAp(mLists)) {
                                    Log.d(MessageUtil.TAG, "DisabledState Learn new Cell id");
                                    HwIntelligenceStateMachine.this.mApInfoManager.processScanResult(HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid());
                                    HwIntelligenceStateMachine.this.mApInfoManager.updateScanResult();
                                }
                            }
                            Log.d(MessageUtil.TAG, "current cell id is not monitor ..... cellid = " + cellid);
                            int unused18 = HwIntelligenceStateMachine.this.mSmartSceneOn = 0;
                            boolean unused19 = HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
                            HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                        }
                    }
                } else {
                    Log.e(MessageUtil.TAG, " DisabledState MSG_WIFI_UPDATE_SCAN_RESULT");
                    if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                        HwIntelligenceStateMachine.this.mApInfoManager.updateScanResult();
                    }
                }
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
                boolean unused = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = true;
                boolean unused2 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                HwIntelligenceStateMachine.this.mWifiManager.startScan();
            } else {
                boolean unused3 = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                boolean unused4 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
            }
            int unused5 = HwIntelligenceStateMachine.this.mAutoCloseScanTimes = 0;
            int unused6 = HwIntelligenceStateMachine.this.mAutoCloseMessage = 0;
            if (HwIntelligenceStateMachine.this.mTargetSsid != null) {
                HwIntelligenceStateMachine.this.sendMessageDelayed(32, 10000);
            }
        }

        public void exit() {
            Log.e(MessageUtil.TAG, "DisconnectedState exit");
            boolean unused = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
            boolean unused2 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
            int unused3 = HwIntelligenceStateMachine.this.mAutoCloseScanTimes = 0;
            HwIntelligenceStateMachine.this.releaseAutoTimer();
            HwIntelligenceStateMachine.this.removeMessages(32);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 2) {
                if (i == 7) {
                    Log.d(MessageUtil.TAG, "DisconnectedState MSG_WIFI_UPDATE_SCAN_RESULT mIsAutoCloseSearch = " + HwIntelligenceStateMachine.this.mIsAutoCloseSearch + " mIsWaittingAutoClose = " + HwIntelligenceStateMachine.this.mIsWaittingAutoClose);
                    if (HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                        List<ScanResult> mLists = HwIntelligenceStateMachine.this.mWifiManager.getScanResults();
                        if (mLists.size() <= 0) {
                            boolean unused = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                            boolean unused2 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = true;
                            Log.w(MessageUtil.TAG, "DisconnectedState send disable message mAutoCloseMessage =" + HwIntelligenceStateMachine.this.mAutoCloseMessage);
                            HwIntelligenceStateMachine.this.setAutoTimer(9);
                        } else if (!HwIntelligenceStateMachine.this.mApInfoManager.handleAutoScanResult(mLists)) {
                            boolean unused3 = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                            boolean unused4 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = true;
                            Log.w(MessageUtil.TAG, "DisconnectedState first send disable message mAutoCloseMessage = " + HwIntelligenceStateMachine.this.mAutoCloseMessage);
                            HwIntelligenceStateMachine.this.setAutoTimer(9);
                        } else {
                            Log.w(MessageUtil.TAG, "DisconnectedState learn new cell info");
                            HwIntelligenceStateMachine.this.mApInfoManager.processScanResult(HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid());
                            Log.e(MessageUtil.TAG, "DisconnectedState send MSG_WIFI_AUTO_CLOSE_SCAN message mAutoCloseMessage =" + HwIntelligenceStateMachine.this.mAutoCloseMessage);
                            HwIntelligenceStateMachine.this.setAutoTimer(25);
                        }
                    } else if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose) {
                        List<ScanResult> mLists2 = HwIntelligenceStateMachine.this.mWifiManager.getScanResults();
                        if (mLists2.size() > 0 && HwIntelligenceStateMachine.this.mApInfoManager.handleAutoScanResult(mLists2)) {
                            Log.d(MessageUtil.TAG, "DisconnectedState MSG_WIFI_UPDATE_SCAN_RESULT remove auto close message");
                            boolean unused5 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                            boolean unused6 = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = true;
                            HwIntelligenceStateMachine.this.mApInfoManager.processScanResult(HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid());
                            HwIntelligenceStateMachine.this.setAutoTimer(25);
                        }
                    }
                } else if (i == 9) {
                    Log.w(MessageUtil.TAG, "MessageUtil.MSG_WIFI_HANDLE_DISABLE mIsWifiP2PConnected = " + HwIntelligenceStateMachine.this.mIsWifiP2PConnected);
                    HwIntelligenceStateMachine.this.releaseAutoTimer();
                    boolean unused7 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                    if (!HwIntelligenceStateMachine.this.mIsWifiP2PConnected) {
                        HwIntelligenceStateMachine.this.autoDisbleWiFi();
                    }
                } else if (i == 23) {
                    Log.w(MessageUtil.TAG, "DisconnectedState MessageUtil.MSG_HANDLE_STATE_CHANGE mIsAutoCloseSearch = " + HwIntelligenceStateMachine.this.mIsAutoCloseSearch);
                    if (HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                        HwIntelligenceStateMachine.this.mWifiManager.startScan();
                    }
                } else if (i == 25) {
                    Log.e(MessageUtil.TAG, "DisconnectedState MSG_WIFI_AUTO_CLOSE_SCAN");
                    HwIntelligenceStateMachine.this.mWifiManager.startScan();
                } else if (i != 27) {
                    if (i != 32) {
                        switch (i) {
                            case 14:
                                Log.w(MessageUtil.TAG, "DisconnectedState MessageUtil.MSG_WIFI_P2P_CONNECTED");
                                boolean unused8 = HwIntelligenceStateMachine.this.mIsWifiP2PConnected = true;
                                if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose || HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                                    Log.e(MessageUtil.TAG, "DisconnectedState remove MSG_WIFI_HANDLE_DISABLE");
                                    boolean unused9 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                                    boolean unused10 = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                                    HwIntelligenceStateMachine.this.releaseAutoTimer();
                                    break;
                                }
                            case 15:
                                Log.w(MessageUtil.TAG, "MessageUtil.MSG_WIFI_P2P_DISCONNECTED");
                                boolean unused11 = HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
                                if (HwIntelligenceStateMachine.this.getAutoOpenValue()) {
                                    boolean unused12 = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = true;
                                    boolean unused13 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                                    HwIntelligenceStateMachine.this.mWifiManager.startScan();
                                    break;
                                }
                                break;
                            default:
                                return false;
                        }
                    } else {
                        Log.d(MessageUtil.TAG, "DisconnectedState MessageUtil.MSG_UPDATE_TARGET_SSID");
                        String unused14 = HwIntelligenceStateMachine.this.mTargetSsid = null;
                    }
                } else if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose || HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                    Log.e(MessageUtil.TAG, "DisconnectedState MSG_WIFI_CONNECTING");
                    boolean unused15 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                    boolean unused16 = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                    HwIntelligenceStateMachine.this.releaseAutoTimer();
                }
            } else if (HwIntelligenceStateMachine.this.getAutoOpenValue()) {
                boolean unused17 = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = true;
                boolean unused18 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                HwIntelligenceStateMachine.this.mWifiManager.startScan();
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
            long unused = HwIntelligenceStateMachine.this.mEnabledStateTime = SystemClock.elapsedRealtime();
        }

        public boolean processMessage(Message message) {
            if (message.what != 3) {
                return false;
            }
            return true;
        }
    }

    class InitialState extends State {
        InitialState() {
        }

        public void enter() {
            Log.e(MessageUtil.TAG, "InitialState");
            List unused = HwIntelligenceStateMachine.this.mTargetApInfoDatas = null;
            boolean unused2 = HwIntelligenceStateMachine.this.mIsAutoClose = false;
            boolean unused3 = HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
            boolean unused4 = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
            boolean unused5 = HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
            boolean unused6 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
            boolean unused7 = HwIntelligenceStateMachine.this.mIsInitialState = false;
            long unused8 = HwIntelligenceStateMachine.this.mInitialStateTime = SystemClock.elapsedRealtime();
            if (!HwIntelligenceStateMachine.this.mWifiManager.isWifiEnabled() && Settings.Global.getInt(HwIntelligenceStateMachine.this.mContext.getContentResolver(), "wifi_on", 0) == 0) {
                Log.e(MessageUtil.TAG, "InitialState wifi is disable");
                boolean unused9 = HwIntelligenceStateMachine.this.mIsInitialState = true;
                HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mDisabledState);
            }
        }

        public boolean processMessage(Message message) {
            return false;
        }
    }

    class InternetReadyState extends State {
        private boolean mIsMatchHomeScene = true;
        private boolean mUserOpenWifi = false;

        InternetReadyState() {
        }

        public void enter() {
            boolean bMobileAP = isMobileAP();
            Log.e(MessageUtil.TAG, "mInternetReadyState bMobileAP = " + bMobileAP);
            WifiInfo Info = HwIntelligenceStateMachine.this.mWifiManager.getConnectionInfo();
            if (Info != null && Info.getBSSID() != null && !bMobileAP) {
                HwIntelligenceStateMachine.this.mApInfoManager.addCurrentApInfo(HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid());
                if (HwIntelligenceStateMachine.this.mHomeAddress == null || System.currentTimeMillis() - HwIntelligenceStateMachine.this.mHomeAddress.getUpdateTime() >= 30000) {
                    Log.d(MessageUtil.TAG, "HomeAddress needs update");
                    LocationAddress unused = HwIntelligenceStateMachine.this.mHomeAddress = HwIntelligenceStateMachine.this.mHomeAddressManager.getLastHomeAddress();
                    if (HwIntelligenceStateMachine.this.mHomeAddress != null) {
                        Log.d(MessageUtil.TAG, "HomeAddress isOversea : " + HwIntelligenceStateMachine.this.mHomeAddress.isOversea() + " , isInvalid : " + HwIntelligenceStateMachine.this.mHomeAddress.isInvalid());
                        boolean unused2 = HwIntelligenceStateMachine.this.mIsOversea = HwIntelligenceStateMachine.this.mHomeAddress.isOversea();
                    }
                }
                if (!HwIntelligenceStateMachine.this.mIsOversea && !HwIntelligenceStateMachine.this.isLocationAvaliable(HwIntelligenceStateMachine.this.mLastLocationAddress)) {
                    long unused3 = HwIntelligenceStateMachine.this.mLocationRequestFailed = 0;
                    HwIntelligenceStateMachine.this.requestLocationInfo();
                }
                long unused4 = HwIntelligenceStateMachine.this.mInternetReadyStateTime = SystemClock.elapsedRealtime();
                this.mUserOpenWifi = false;
                this.mIsMatchHomeScene = true;
                if (HwIntelligenceStateMachine.this.mInternetReadyStateTime - HwIntelligenceStateMachine.this.mEnabledStateTime <= HidataWechatTraffic.MIN_VALID_TIME && HwIntelligenceStateMachine.this.mInternetReadyStateTime - HwIntelligenceStateMachine.this.mInitialStateTime >= 3600000) {
                    this.mUserOpenWifi = true;
                }
                if (HwIntelligenceStateMachine.this.mHomeAddress == null || (HwIntelligenceStateMachine.this.mHomeAddress != null && HwIntelligenceStateMachine.this.mHomeAddress.isInvalid())) {
                    this.mIsMatchHomeScene = false;
                }
                int value = Settings.System.getInt(HwIntelligenceStateMachine.this.mContext.getContentResolver(), MessageUtil.WIFIPRO_AUTO_OPEN_STATE, 0);
                Log.d(MessageUtil.TAG, "InternetReadyState mUserOpenWifi= " + this.mUserOpenWifi + ", value = " + value);
                if (this.mUserOpenWifi && value != 1) {
                    HwIntelligenceStateMachine.this.uploadAutoOpenWifiFailed(this.mIsMatchHomeScene);
                }
            }
        }

        public void exit() {
            Log.d(MessageUtil.TAG, "InternetReadyState exit");
            long unused = HwIntelligenceStateMachine.this.mLocationRequestFailed = 0;
            if (HwIntelligenceStateMachine.this.hasMessages(29)) {
                HwIntelligenceStateMachine.this.removeMessages(29);
            }
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 11) {
                switch (i) {
                    case 20:
                    case 21:
                        if (!isMobileAP()) {
                            String cellid = HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid();
                            if (cellid != null) {
                                HwIntelligenceStateMachine.this.mApInfoManager.updataApInfo(cellid);
                                break;
                            }
                        }
                        break;
                    default:
                        switch (i) {
                            case MessageUtil.MSG_UPDATE_LOCATION:
                                if (HwIntelligenceStateMachine.this.mLocationRequestFailed < 3) {
                                    HwIntelligenceStateMachine.this.requestLocationInfo();
                                    break;
                                }
                                break;
                            case 30:
                                Log.d(MessageUtil.TAG, "InternetReadyState ConnectedState MessageUtil.MSG_LACATION_READY");
                                long unused = HwIntelligenceStateMachine.this.mLocationRequestFailed = 0;
                                Bundle data = message.getData();
                                if (data != null) {
                                    HwIntelligenceStateMachine hwIntelligenceStateMachine = HwIntelligenceStateMachine.this;
                                    LocationAddress locationAddress = new LocationAddress(data.getDouble(HomeAddressDataManager.LATITUDE_KEY), data.getDouble(HomeAddressDataManager.LONGITUDE_KEY), data.getDouble(HomeAddressDataManager.DISTANCE_KEY), Long.valueOf(System.currentTimeMillis()));
                                    LocationAddress unused2 = hwIntelligenceStateMachine.mLastLocationAddress = locationAddress;
                                    if (HwIntelligenceStateMachine.this.isLocationAvaliable(HwIntelligenceStateMachine.this.mLastLocationAddress)) {
                                        HwIntelligenceStateMachine.this.mApInfoManager.updateCurrentApHomebySsid(HwIntelligenceStateMachine.this.mTargetSsid, HwIntelligenceStateMachine.this.mAuthType, HwIntelligenceStateMachine.this.mLastLocationAddress.isHome());
                                        break;
                                    }
                                }
                                break;
                            case MessageUtil.MSG_GET_LOCATION_FAIL:
                                HwIntelligenceStateMachine.access$3114(HwIntelligenceStateMachine.this, 1);
                                HwIntelligenceStateMachine.this.sendMessageDelayed(29, 3000);
                                break;
                            default:
                                return false;
                        }
                }
            } else {
                Log.d(MessageUtil.TAG, "InternetReadyState MessageUtil.MSG_WIFI_INTERNET_CONNECTED");
            }
            return true;
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
            List unused = HwIntelligenceStateMachine.this.mTargetApInfoDatas = null;
            HwIntelligenceStateMachine.this.setAutoOpenValue(false);
            boolean unused2 = HwIntelligenceStateMachine.this.mIsAutoClose = false;
            boolean unused3 = HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
            boolean unused4 = HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
            boolean unused5 = HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
            boolean unused6 = HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
        }

        public boolean processMessage(Message message) {
            if (message.what != 100) {
                return true;
            }
            HwIntelligenceStateMachine.this.transitionTo(HwIntelligenceStateMachine.this.mInitialState);
            return true;
        }
    }

    static /* synthetic */ long access$3114(HwIntelligenceStateMachine x0, long x1) {
        long j = x0.mLocationRequestFailed + x1;
        x0.mLocationRequestFailed = j;
        return j;
    }

    private void registerNetworkReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.CONFIGURATION_CHANGED".equals(intent.getAction())) {
                    HwIntelligenceStateMachine.this.sendMessageDelayed(28, 1000);
                }
            }
        };
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
    }

    /* access modifiers changed from: private */
    public boolean isFullScreen() {
        AbsPhoneWindowManager policy = (AbsPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        return policy != null && policy.isTopIsFullscreen();
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
        Context context2 = this.mContext;
        Context context3 = this.mContext;
        this.mWifiManager = (WifiManager) context2.getSystemService("wifi");
        this.mHwintelligenceWiFiCHR = HwintelligenceWiFiCHR.getInstance(this);
        this.mWiFiStateMonitor = new WiFiStateMonitor(context, getHandler());
        this.mCellStateMonitor = new CellStateMonitor(context, getHandler());
        this.mApInfoManager = new ApInfoManager(context, this, getHandler());
        this.mHomeAddressManager = new HomeAddressDataManager(context, getHandler());
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
        registerNetworkReceiver();
        start();
    }

    /* access modifiers changed from: private */
    public void setAutoTimer(int message) {
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
            this.mAutoCloseScanTimes = this.mAutoCloseScanTimes + 1;
        } else {
            this.mAutoCloseMessage = message;
            this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 120000, MessageUtil.TAG, this.mAutoCloseTimeoutListener, getHandler());
        }
    }

    /* access modifiers changed from: private */
    public void releaseAutoTimer() {
        Log.e(MessageUtil.TAG, "DisconnectedState releaseAutoTimer");
        this.mAutoCloseMessage = 0;
        this.mAlarmManager.cancel(this.mAutoCloseTimeoutListener);
    }

    /* access modifiers changed from: private */
    public boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService("power");
        if (pm == null || !pm.isScreenOn()) {
            return false;
        }
        return true;
    }

    private boolean isAirModeOn() {
        boolean z = false;
        if (this.mContext == null) {
            return false;
        }
        if (Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public boolean isClosedByUser() {
        if (isAirModeOn()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void setAutoOpenValue(boolean enable) {
        Log.w(MessageUtil.TAG, "setAutoOpenValue =" + enable);
        Settings.System.putInt(this.mContext.getContentResolver(), MessageUtil.WIFIPRO_AUTO_OPEN_STATE, enable);
    }

    /* access modifiers changed from: private */
    public boolean getAutoOpenValue() {
        int value = Settings.System.getInt(this.mContext.getContentResolver(), MessageUtil.WIFIPRO_AUTO_OPEN_STATE, 0);
        Log.w(MessageUtil.TAG, "getAutoOpenValue  value = " + value);
        return false;
    }

    /* access modifiers changed from: private */
    public List<APInfoData> removeFromBlackList(List<APInfoData> datas) {
        ArrayList<APInfoData> result = new ArrayList<>();
        for (APInfoData data : datas) {
            Log.d(MessageUtil.TAG, "removeFromBlackList ssid = " + data.getSsid() + ", isInBlackList = " + data.isInBlackList() + ", isHomeAp = " + data.isHomeAp() + ", mIsOversea = " + this.mIsOversea);
            if (!data.isInBlackList() && (data.isHomeAp() || this.mIsOversea)) {
                if (!data.isInBlackList()) {
                    this.mNotInBlacklist = true;
                }
                result.add(data);
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    public void uploadAutoOpenWifiFailed(boolean isMatchHomeScene) {
        Log.d(MessageUtil.TAG, "uploadAutoOpenWifiFailed isMatchHomeScene = " + isMatchHomeScene);
        if (SystemClock.elapsedRealtime() - this.mUploadAutoOpenWifiFailedTime >= this.uploadIntervalTime) {
            Context context = this.mContext;
            Context context2 = this.mContext;
            LocationManager locMgr = (LocationManager) context.getSystemService("location");
            boolean isGpsOn = false;
            if (locMgr != null) {
                isGpsOn = locMgr.isProviderEnabled("gps");
            }
            int i = 1;
            if (!this.mWifiManager.isScanAlwaysAvailable()) {
                this.mSmartSceneOn = 1;
            }
            Log.d(MessageUtil.TAG, "mIsOversea = " + this.mIsOversea + ", isMatchHomeScene = " + isMatchHomeScene + ", isGpsOn = " + isGpsOn + ", mSmartSceneOn= " + this.mSmartSceneOn);
            if (!this.mIsOversea || this.mSmartSceneOn != 0) {
                if (this.mIsOversea || isMatchHomeScene) {
                    this.uploadIntervalTime = 43200000;
                } else {
                    this.uploadIntervalTime = 604800000;
                }
                HwWifiCHRService mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
                if (mHwWifiCHRService != null) {
                    this.mUploadAutoOpenWifiFailedTime = SystemClock.elapsedRealtime();
                    Bundle data = new Bundle();
                    data.putInt("isMatchHomeScene", isMatchHomeScene);
                    if (!isGpsOn) {
                        i = 0;
                    }
                    data.putInt("isGPSOn", i);
                    data.putInt("isSmartSceneOn", this.mSmartSceneOn);
                    mHwWifiCHRService.uploadDFTEvent(909002064, data);
                }
            } else {
                Log.d(MessageUtil.TAG, "wifi always scan enable, and ap not in blacklist.");
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean getSettingSwitchType() {
        Log.w(MessageUtil.TAG, "getSettingSwitchType in");
        int select = Settings.System.getInt(this.mContext.getContentResolver(), MessageUtil.WIFI_CONNECT_TYPE, 0);
        Log.w(MessageUtil.TAG, "getSettingSwitchType select = " + select);
        if (select == 1) {
            return false;
        }
        return true;
    }

    public List<APInfoData> getTargetApInfoDatas() {
        return this.mTargetApInfoDatas;
    }

    /* access modifiers changed from: private */
    public void autoDisbleWiFi() {
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
            this.mIsOversea = useOperatorOverSea();
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

    /* access modifiers changed from: private */
    public void requestLocationInfo() {
        Log.d(MessageUtil.TAG, "requestLocationInfo enter");
        if (this.mHomeAddress == null) {
            Log.d(MessageUtil.TAG, "requestLocationInfo mHomeAddress is null , need getLastHomeAddress");
            this.mHomeAddress = this.mHomeAddressManager.getLastHomeAddress();
        }
        if (this.mHomeAddress != null) {
            Log.d(MessageUtil.TAG, "requestLocationInfo mHomeAddress isInvalid = " + this.mHomeAddress.isInvalid() + ", isOversea = " + this.mHomeAddress.isOversea());
            if (!this.mHomeAddress.isInvalid() && !this.mHomeAddress.isOversea()) {
                this.mHomeAddressManager.setHomeDistanceCallback(this.mHomeAddress);
            }
        }
        Log.d(MessageUtil.TAG, "requestLocationInfo exit");
    }

    public boolean isLocationAvaliable(LocationAddress location) {
        return location != null && !location.isInvalid() && !location.isOversea() && System.currentTimeMillis() - location.getUpdateTime() < 30000;
    }

    private boolean useOperatorOverSea() {
        String operator = TelephonyManager.getDefault().getNetworkOperator();
        if (operator == null || operator.length() <= 0) {
            if ("CN".equalsIgnoreCase(WifiProCommonUtils.getProductLocale())) {
                return false;
            }
        } else if (operator.startsWith(COUNTRY_CODE_CN)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void setPingpongPunishTime() {
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
                    if (System.currentTimeMillis() - this.mLastScanPingpongTime > 1800000) {
                        this.mScanPingpongNum = 1;
                    } else {
                        this.mScanPingpongNum++;
                    }
                    Log.e(MessageUtil.TAG, "setPingpongPunishTime mScanPingpongNum = " + this.mScanPingpongNum);
                }
            } else {
                Log.e(MessageUtil.TAG, "setPingpongPunishTime is not inPunish time");
            }
            this.mLastCellChangeScanTime = System.currentTimeMillis();
        }
    }

    /* access modifiers changed from: private */
    public boolean isInPingpongPunishTime() {
        Log.e(MessageUtil.TAG, "isInPingpongPunishTime mScanPingpongNum = " + this.mScanPingpongNum);
        int punishTime = this.mScanPingpongNum * 30000;
        if (!this.mIsOversea && punishTime > 60000) {
            punishTime = 60000;
        } else if (punishTime > 300000) {
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
        this.mScanPingpongNum = 1;
        this.mLastCellChangeScanTime = 0;
        this.mLastScanPingpongTime = 0;
    }

    public CellStateMonitor getCellStateMonitor() {
        return this.mCellStateMonitor;
    }
}
