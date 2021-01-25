package com.huawei.hwwifiproservice;

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
import android.util.wifi.HwHiLog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.List;

public class HwIntelligenceStateMachine extends StateMachine {
    private static final String ACTION_WIFI_PRO_TIMER = "android.net.wifi.wifi_pro_timer";
    private static final String AUTO_OPEN_EVENT = "autoOpenEvent";
    private static final String AUTO_OPEN_SUCC_EVENT = "autoOpenSuccCnt";
    private static final int AUTO_OPEN_WIFI_DELAY_TIME = 3000;
    private static final String COUNTRY_CODE_CN = "460";
    private static final int INITIAL_CONNECT_WIFI_INTERVAL_TIME = 3600000;
    private static final int LOCATION_AVAILABLE_TIME = 30000;
    private static final int OPEN_CONNECT_WIFI_INTERVAL_TIME = 60000;
    private static final int PING_PONG_HOME_MAX_PUNISH_TIME = 60000;
    private static final int PING_PONG_INTERVAL_TIME = 1800000;
    private static final int PING_PONG_MAX_PUNISH_TIME = 300000;
    private static final int PING_PONG_PUNISH_TIME = 30000;
    private static final int PING_PONG_TIME = 5000;
    private static final int UPLOAD_AUTO_OPEN_FAIL_INTERVAL_TIME = 43200000;
    private static final int UPLOAD_NO_HOMEADRESS_INTERVAL_TIME = 604800000;
    private static final int WIFI_PRO_TIMER = 0;
    private static HwIntelligenceStateMachine sHwIntelligenceStateMachine;
    private AlarmManager mAlarmManager = null;
    private ApInfoManager mApInfoManager = null;
    private int mAuthType = -1;
    private int mAutoCloseMessage = 0;
    private int mAutoCloseScanTimes = 0;
    private AlarmManager.OnAlarmListener mAutoCloseTimeoutListener = new AlarmManager.OnAlarmListener() {
        /* class com.huawei.hwwifiproservice.HwIntelligenceStateMachine.AnonymousClass1 */

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            HwHiLog.w(MessageUtil.TAG, false, "receive auto close message mAutoCloseMessage = %{public}d", new Object[]{Integer.valueOf(HwIntelligenceStateMachine.this.mAutoCloseMessage)});
            if (HwIntelligenceStateMachine.this.mAutoCloseMessage == 25) {
                HwIntelligenceStateMachine.this.mHandler.sendEmptyMessage(25);
            } else if (HwIntelligenceStateMachine.this.mAutoCloseMessage == 9) {
                HwIntelligenceStateMachine.this.mHandler.sendEmptyMessage(9);
            }
            HwIntelligenceStateMachine.this.mAutoCloseMessage = 0;
        }
    };
    private int mAutoOpenCnt = 0;
    private BroadcastReceiver mBroadcastReceiver = null;
    private CellStateMonitor mCellStateMonitor = null;
    private String mConnectFailedBssid = null;
    private int mConnectFailedReason = -1;
    private String mConnectFailedSsid = null;
    private State mConnectedState = new ConnectedState();
    private Context mContext = null;
    private State mDefaultState = new DefaultState();
    private State mDisabledState = new DisabledState();
    private State mDisconnectedState = new DisconnectedState();
    private State mEnabledState = new EnabledState();
    private long mEnabledStateTime = 0;
    private Handler mHandler = null;
    private HwintelligenceWiFiCHR mHwIntelligenceWifiChr = null;
    private State mInitialState = new InitialState();
    private long mInitialStateTime = 0;
    private IntentFilter mIntentFilter = null;
    private State mInternetReadyState = new InternetReadyState();
    private long mInternetReadyStateTime = 0;
    private boolean mIsAutoClose = false;
    private boolean mIsAutoCloseSearch = false;
    private boolean mIsAutoOpenSearch = false;
    private boolean mIsFoundInBlackList = true;
    private boolean mIsInitialState = false;
    private boolean mIsMachineStared = false;
    private boolean mIsScreenOn = false;
    private boolean mIsWaittingAutoClose = false;
    private boolean mIsWifiP2PConnected = false;
    private long mLastCellChangeScanTime = 0;
    private long mLastScanPingpongTime = 0;
    private State mNoInternetState = new NoInternetState();
    private int mScanPingpongNum = 0;
    private int mSmartSceneOn = 0;
    private State mStopState = new StopState();
    private List<APInfoData> mTargetApInfoDatas = null;
    private String mTargetCellId = null;
    private String mTargetSsid = null;
    private WifiInfo mTargetWifiInfo = null;
    private long mUploadAutoOpenWifiFailedTime = 0;
    private long mUploadIntervalTime = 0;
    private WifiProChrUploadManager mUploadManager = null;
    private WiFiStateMonitor mWiFiStateMonitor = null;
    private WifiManager mWifiManager = null;

    private void registerNetworkReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.huawei.hwwifiproservice.HwIntelligenceStateMachine.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
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
    /* access modifiers changed from: public */
    private boolean isFullScreen() {
        Bundle result = WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 9, new Bundle());
        if (result != null) {
            return result.getBoolean("isFullscreen");
        }
        return false;
    }

    class DefaultState extends State {
        DefaultState() {
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                HwIntelligenceStateMachine hwIntelligenceStateMachine = HwIntelligenceStateMachine.this;
                hwIntelligenceStateMachine.transitionTo(hwIntelligenceStateMachine.mConnectedState);
            } else if (i == 2) {
                HwIntelligenceStateMachine hwIntelligenceStateMachine2 = HwIntelligenceStateMachine.this;
                hwIntelligenceStateMachine2.transitionTo(hwIntelligenceStateMachine2.mDisconnectedState);
            } else if (i == 3) {
                int wifiEnableFlag = Settings.Global.getInt(HwIntelligenceStateMachine.this.mContext.getContentResolver(), "wifi_on", 0);
                HwHiLog.d(MessageUtil.TAG, false, "MSG_WIFI_ENABLED wifiEnableFlag = %{public}d mIsAutoOpenSearch =%{public}s", new Object[]{Integer.valueOf(wifiEnableFlag), String.valueOf(HwIntelligenceStateMachine.this.mIsAutoOpenSearch)});
                if (wifiEnableFlag == 1 || wifiEnableFlag == 2) {
                    HwIntelligenceStateMachine hwIntelligenceStateMachine3 = HwIntelligenceStateMachine.this;
                    hwIntelligenceStateMachine3.transitionTo(hwIntelligenceStateMachine3.mEnabledState);
                } else if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                    HwHiLog.i(MessageUtil.TAG, false, "MSG_WIFI_ENABLED start scan", new Object[0]);
                    HwIntelligenceStateMachine.this.mWifiManager.startScan();
                }
            } else if (i != 4) {
                if (i != 5) {
                    if (i == 24) {
                        Bundle mData = message.getData();
                        HwIntelligenceStateMachine.this.mConnectFailedReason = mData.getInt("reason");
                        HwIntelligenceStateMachine.this.mConnectFailedBssid = mData.getString("bssid", "");
                        HwIntelligenceStateMachine.this.mConnectFailedSsid = mData.getString("ssid", "");
                        HwHiLog.d(MessageUtil.TAG, false, "MSG_CONNECT_FAILED ssid = %{public}s mConnectFailedReason = %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(HwIntelligenceStateMachine.this.mConnectFailedSsid), Integer.valueOf(HwIntelligenceStateMachine.this.mConnectFailedReason)});
                    } else if (i != 25) {
                        if (i == 100) {
                            HwIntelligenceStateMachine hwIntelligenceStateMachine4 = HwIntelligenceStateMachine.this;
                            hwIntelligenceStateMachine4.transitionTo(hwIntelligenceStateMachine4.mInitialState);
                        } else if (i != 101) {
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
                                    HwHiLog.d(MessageUtil.TAG, false, "MSG_WIFI_CONFIG_CHANGED ssid = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid)});
                                    if (ssid == null) {
                                        if (bssid != null) {
                                            HwIntelligenceStateMachine.this.mApInfoManager.deleteApInfoByBssid(bssid);
                                            break;
                                        }
                                    } else {
                                        HwIntelligenceStateMachine.this.mApInfoManager.delectApInfoBySsid(ssid);
                                        break;
                                    }
                                    break;
                                case 14:
                                    HwIntelligenceStateMachine.this.mIsWifiP2PConnected = true;
                                    break;
                                case 15:
                                    HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
                                    break;
                                default:
                                    switch (i) {
                                        case 20:
                                            break;
                                        case 21:
                                            HwHiLog.d(MessageUtil.TAG, false, " DefaultState MSG_SCREEN_ON", new Object[0]);
                                            HwIntelligenceStateMachine.this.sendMessage(23);
                                            break;
                                        case 22:
                                            HwHiLog.d(MessageUtil.TAG, false, " DefaultState MSG_SCREEN_OFF mIsAutoOpenSearch = %{public}s", new Object[]{String.valueOf(HwIntelligenceStateMachine.this.mIsAutoOpenSearch)});
                                            if (HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                                                HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                                                break;
                                            }
                                            break;
                                        default:
                                            switch (i) {
                                            }
                                    }
                            }
                        } else {
                            HwIntelligenceStateMachine hwIntelligenceStateMachine5 = HwIntelligenceStateMachine.this;
                            hwIntelligenceStateMachine5.transitionTo(hwIntelligenceStateMachine5.mStopState);
                        }
                    }
                }
                HwHiLog.d(MessageUtil.TAG, false, " DefaultState message.what = %{public}d", new Object[]{Integer.valueOf(message.what)});
            } else {
                HwIntelligenceStateMachine hwIntelligenceStateMachine6 = HwIntelligenceStateMachine.this;
                hwIntelligenceStateMachine6.transitionTo(hwIntelligenceStateMachine6.mDisabledState);
            }
            return true;
        }
    }

    class InitialState extends State {
        InitialState() {
        }

        public void enter() {
            HwHiLog.d(MessageUtil.TAG, false, "InitialState", new Object[0]);
            HwIntelligenceStateMachine.this.mTargetApInfoDatas = null;
            HwIntelligenceStateMachine.this.mIsAutoClose = false;
            HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
            HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
            HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
            HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
            HwIntelligenceStateMachine.this.mIsInitialState = false;
            HwIntelligenceStateMachine.this.mInitialStateTime = SystemClock.elapsedRealtime();
            if (!HwIntelligenceStateMachine.this.mWifiManager.isWifiEnabled() && Settings.Global.getInt(HwIntelligenceStateMachine.this.mContext.getContentResolver(), "wifi_on", 0) == 0) {
                HwHiLog.d(MessageUtil.TAG, false, "InitialState wifi is disable", new Object[0]);
                HwIntelligenceStateMachine.this.mIsInitialState = true;
                HwIntelligenceStateMachine hwIntelligenceStateMachine = HwIntelligenceStateMachine.this;
                hwIntelligenceStateMachine.transitionTo(hwIntelligenceStateMachine.mDisabledState);
            }
        }

        public boolean processMessage(Message message) {
            return false;
        }
    }

    class EnabledState extends State {
        EnabledState() {
        }

        public void enter() {
            HwHiLog.d(MessageUtil.TAG, false, "EnabledState", new Object[0]);
            HwIntelligenceStateMachine.this.mApInfoManager.resetAllBlackList();
            HwIntelligenceStateMachine.this.mEnabledStateTime = SystemClock.elapsedRealtime();
        }

        public boolean processMessage(Message message) {
            if (message.what != 3) {
                return false;
            }
            return true;
        }
    }

    class ConnectedState extends State {
        ConnectedState() {
        }

        public void enter() {
            HwHiLog.d(MessageUtil.TAG, false, "ConnectedState", new Object[0]);
            if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose) {
                HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                HwHiLog.d(MessageUtil.TAG, false, "ConnectedState remove MSG_WIFI_HANDLE_DISABLE", new Object[0]);
                HwintelligenceWiFiCHR hwintelligenceWiFiCHR = HwIntelligenceStateMachine.this.mHwIntelligenceWifiChr;
                HwintelligenceWiFiCHR unused = HwIntelligenceStateMachine.this.mHwIntelligenceWifiChr;
                hwintelligenceWiFiCHR.uploadAutoCloseFailed(1);
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
            HwIntelligenceStateMachine.this.mHwIntelligenceWifiChr.stopConnectTimer();
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 1) {
                switch (i) {
                    case 11:
                        HwIntelligenceStateMachine hwIntelligenceStateMachine = HwIntelligenceStateMachine.this;
                        hwIntelligenceStateMachine.transitionTo(hwIntelligenceStateMachine.mInternetReadyState);
                        break;
                    case 12:
                        HwHiLog.d(MessageUtil.TAG, false, "ConnectedState MSG_WIFI_INTERNET_DISCONNECTED", new Object[0]);
                        HwIntelligenceStateMachine hwIntelligenceStateMachine2 = HwIntelligenceStateMachine.this;
                        hwIntelligenceStateMachine2.transitionTo(hwIntelligenceStateMachine2.mNoInternetState);
                        break;
                    case 13:
                        HwHiLog.d(MessageUtil.TAG, false, "MSG_WIFI_IS_PORTAL", new Object[0]);
                        WifiInfo mPortalInfo = HwIntelligenceStateMachine.this.mWifiManager.getConnectionInfo();
                        if (!(mPortalInfo == null || mPortalInfo.getSSID() == null)) {
                            if (HwIntelligenceStateMachine.this.mApInfoManager.getApInfoByBssid(mPortalInfo.getBSSID()) != null) {
                                HwIntelligenceStateMachine.this.mHwIntelligenceWifiChr.uploadPortalApInWhite(mPortalInfo.getBSSID(), mPortalInfo.getSSID());
                            }
                            HwIntelligenceStateMachine.this.mApInfoManager.delectApInfoBySsidForPortal(mPortalInfo);
                        }
                        HwIntelligenceStateMachine hwIntelligenceStateMachine3 = HwIntelligenceStateMachine.this;
                        hwIntelligenceStateMachine3.transitionTo(hwIntelligenceStateMachine3.mNoInternetState);
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
            WifiConfiguration config = WifiproUtils.getCurrentWifiConfig(HwIntelligenceStateMachine.this.mWifiManager);
            if (config != null) {
                HwIntelligenceStateMachine.this.mTargetSsid = config.SSID;
                if (config.allowedKeyManagement.cardinality() <= 1) {
                    HwIntelligenceStateMachine.this.mAuthType = config.getAuthType();
                } else {
                    HwIntelligenceStateMachine.this.mAuthType = -1;
                }
                HwHiLog.d(MessageUtil.TAG, false, "mTargetSsid is %{public}s mAuthType %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(HwIntelligenceStateMachine.this.mTargetSsid), Integer.valueOf(HwIntelligenceStateMachine.this.mAuthType)});
                if (HwIntelligenceStateMachine.this.getCurrentState() == HwIntelligenceStateMachine.this.mInternetReadyState) {
                    HwIntelligenceStateMachine hwIntelligenceStateMachine = HwIntelligenceStateMachine.this;
                    hwIntelligenceStateMachine.mTargetWifiInfo = hwIntelligenceStateMachine.mWifiManager.getConnectionInfo();
                    if (HwIntelligenceStateMachine.this.mTargetWifiInfo != null) {
                        HwIntelligenceStateMachine hwIntelligenceStateMachine2 = HwIntelligenceStateMachine.this;
                        hwIntelligenceStateMachine2.mTargetCellId = hwIntelligenceStateMachine2.mCellStateMonitor.getCurrentCellid();
                        HwHiLog.d(MessageUtil.TAG, false, "mTargetSsid %{public}s mTargetBssid %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(HwIntelligenceStateMachine.this.mTargetSsid), WifiProCommonUtils.safeDisplayBssid(HwIntelligenceStateMachine.this.mTargetWifiInfo.getBSSID())});
                    }
                }
            }
        }
    }

    class InternetReadyState extends State {
        private boolean mIsMatchHomeScene = false;
        private boolean mUserOpenWifi = false;

        InternetReadyState() {
        }

        public void enter() {
            boolean bMobileAP = isMobileAP();
            HwHiLog.d(MessageUtil.TAG, false, "mInternetReadyState bMobileAP = %{public}s", new Object[]{String.valueOf(bMobileAP)});
            WifiInfo info = HwIntelligenceStateMachine.this.mWifiManager.getConnectionInfo();
            if (info != null && info.getBSSID() != null && !bMobileAP) {
                HwIntelligenceStateMachine.this.mApInfoManager.addCurrentApInfo(HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid());
                HwIntelligenceStateMachine.this.mInternetReadyStateTime = SystemClock.elapsedRealtime();
                this.mUserOpenWifi = false;
                if (HwIntelligenceStateMachine.this.mInternetReadyStateTime - HwIntelligenceStateMachine.this.mEnabledStateTime <= 60000 && HwIntelligenceStateMachine.this.mInternetReadyStateTime - HwIntelligenceStateMachine.this.mInitialStateTime >= 3600000) {
                    this.mUserOpenWifi = true;
                }
                int value = Settings.System.getInt(HwIntelligenceStateMachine.this.mContext.getContentResolver(), MessageUtil.WIFIPRO_AUTO_OPEN_STATE, 0);
                HwHiLog.d(MessageUtil.TAG, false, "InternetReadyState mUserOpenWifi= %{public}s, value = %{public}d", new Object[]{String.valueOf(this.mUserOpenWifi), Integer.valueOf(value)});
                if (this.mUserOpenWifi && value != 1) {
                    HwIntelligenceStateMachine.this.uploadAutoOpenWifiFailed(this.mIsMatchHomeScene);
                }
            }
        }

        public void exit() {
            HwHiLog.d(MessageUtil.TAG, false, "InternetReadyState exit", new Object[0]);
        }

        public boolean processMessage(Message message) {
            String cellid;
            int i = message.what;
            if (i == 11) {
                HwHiLog.d(MessageUtil.TAG, false, "InternetReadyState MessageUtil.MSG_WIFI_INTERNET_CONNECTED", new Object[0]);
                return true;
            } else if (i != 20 && i != 21) {
                return false;
            } else {
                if (isMobileAP() || (cellid = HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid()) == null) {
                    return true;
                }
                HwIntelligenceStateMachine.this.mApInfoManager.updataApInfo(cellid);
                return true;
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
            HwHiLog.d(MessageUtil.TAG, false, "NoInternetState", new Object[0]);
            WifiInfo mConnectInfo = HwIntelligenceStateMachine.this.mWifiManager.getConnectionInfo();
            if (mConnectInfo != null && mConnectInfo.getBSSID() != null) {
                HwIntelligenceStateMachine.this.mApInfoManager.deleteApInfoByBssid(mConnectInfo.getBSSID());
            }
        }

        public boolean processMessage(Message message) {
            return false;
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState enter", new Object[0]);
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
            if (HwIntelligenceStateMachine.this.mTargetSsid != null) {
                HwIntelligenceStateMachine.this.sendMessageDelayed(32, 10000);
            }
        }

        public void exit() {
            HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState exit", new Object[0]);
            HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
            HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
            HwIntelligenceStateMachine.this.mAutoCloseScanTimes = 0;
            HwIntelligenceStateMachine.this.releaseAutoTimer();
            HwIntelligenceStateMachine.this.removeMessages(32);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 2) {
                autoOpenAllowScan();
            } else if (i == 7) {
                HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState MSG_WIFI_UPDATE_SCAN_RESULT mIsAutoCloseSearch = %{public}s mIsWaittingAutoClose = %{public}s", new Object[]{String.valueOf(HwIntelligenceStateMachine.this.mIsAutoCloseSearch), String.valueOf(HwIntelligenceStateMachine.this.mIsWaittingAutoClose)});
                if (HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                    List<ScanResult> mLists = HwIntelligenceStateMachine.this.mWifiManager.getScanResults();
                    if (mLists.size() <= 0) {
                        HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                        HwIntelligenceStateMachine.this.mIsWaittingAutoClose = true;
                        HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState send disable message mAutoCloseMessage =%{public}d", new Object[]{Integer.valueOf(HwIntelligenceStateMachine.this.mAutoCloseMessage)});
                        HwIntelligenceStateMachine.this.setAutoTimer(9);
                    } else if (!HwIntelligenceStateMachine.this.mApInfoManager.handleAutoScanResult(mLists)) {
                        HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                        HwIntelligenceStateMachine.this.mIsWaittingAutoClose = true;
                        HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState first send disable message mAutoCloseMessage = %{public}d", new Object[]{Integer.valueOf(HwIntelligenceStateMachine.this.mAutoCloseMessage)});
                        HwIntelligenceStateMachine.this.setAutoTimer(9);
                    } else {
                        HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState learn new cell info", new Object[0]);
                        HwIntelligenceStateMachine.this.mApInfoManager.processScanResult(HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid());
                        HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState send MSG_WIFI_AUTO_CLOSE_SCAN message mAutoCloseMessage =%{public}d", new Object[]{Integer.valueOf(HwIntelligenceStateMachine.this.mAutoCloseMessage)});
                        HwIntelligenceStateMachine.this.setAutoTimer(25);
                    }
                } else if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose) {
                    List<ScanResult> mLists2 = HwIntelligenceStateMachine.this.mWifiManager.getScanResults();
                    if (mLists2.size() > 0 && HwIntelligenceStateMachine.this.mApInfoManager.handleAutoScanResult(mLists2)) {
                        HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState MSG_WIFI_UPDATE_SCAN_RESULT remove auto close message", new Object[0]);
                        HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                        HwIntelligenceStateMachine.this.mIsAutoCloseSearch = true;
                        HwIntelligenceStateMachine.this.mApInfoManager.processScanResult(HwIntelligenceStateMachine.this.mCellStateMonitor.getCurrentCellid());
                        HwIntelligenceStateMachine.this.setAutoTimer(25);
                    }
                } else {
                    HwHiLog.d(MessageUtil.TAG, false, "is not mIsWaittingAutoClose or mIsAutoCloseSearch", new Object[0]);
                }
            } else if (i == 9) {
                HwHiLog.d(MessageUtil.TAG, false, "MessageUtil.MSG_WIFI_HANDLE_DISABLE mIsWifiP2PConnected = %{public}s", new Object[]{String.valueOf(HwIntelligenceStateMachine.this.mIsWifiP2PConnected)});
                HwIntelligenceStateMachine.this.releaseAutoTimer();
                HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                if (!HwIntelligenceStateMachine.this.mIsWifiP2PConnected) {
                    HwIntelligenceStateMachine.this.autoDisbleWiFi();
                }
            } else if (i == 23) {
                HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState MessageUtil.MSG_HANDLE_STATE_CHANGE mIsAutoCloseSearch = %{public}s", new Object[]{String.valueOf(HwIntelligenceStateMachine.this.mIsAutoCloseSearch)});
                if (HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                    HwIntelligenceStateMachine.this.mWifiManager.startScan();
                }
            } else if (i == 25) {
                HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState MSG_WIFI_AUTO_CLOSE_SCAN", new Object[0]);
                HwIntelligenceStateMachine.this.mWifiManager.startScan();
            } else if (i != 27) {
                if (i == 32) {
                    HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState MessageUtil.MSG_UPDATE_TARGET_SSID", new Object[0]);
                    HwIntelligenceStateMachine.this.mTargetSsid = null;
                    HwIntelligenceStateMachine.this.mTargetWifiInfo = null;
                    HwIntelligenceStateMachine.this.mAuthType = -1;
                    HwIntelligenceStateMachine.this.mTargetCellId = null;
                } else if (i == 14) {
                    HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState MessageUtil.MSG_WIFI_P2P_CONNECTED", new Object[0]);
                    HwIntelligenceStateMachine.this.mIsWifiP2PConnected = true;
                    if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose || HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                        HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState remove MSG_WIFI_HANDLE_DISABLE", new Object[0]);
                        HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                        HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                        HwIntelligenceStateMachine.this.releaseAutoTimer();
                    }
                } else if (i != 15) {
                    return false;
                } else {
                    HwHiLog.d(MessageUtil.TAG, false, "MessageUtil.MSG_WIFI_P2P_DISCONNECTED", new Object[0]);
                    HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
                    autoOpenAllowScan();
                }
            } else if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose || HwIntelligenceStateMachine.this.mIsAutoCloseSearch) {
                HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState MSG_WIFI_CONNECTING", new Object[0]);
                HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                HwIntelligenceStateMachine.this.releaseAutoTimer();
            }
            return true;
        }

        private void autoOpenAllowScan() {
            if (HwIntelligenceStateMachine.this.getAutoOpenValue()) {
                HwIntelligenceStateMachine.this.mIsAutoCloseSearch = true;
                HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                HwIntelligenceStateMachine.this.mWifiManager.startScan();
            }
        }
    }

    class DisabledState extends State {
        DisabledState() {
        }

        public void enter() {
            HwHiLog.d(MessageUtil.TAG, false, "DisabledState", new Object[0]);
            HwIntelligenceStateMachine.this.mSmartSceneOn = 0;
            if (HwIntelligenceStateMachine.this.mIsInitialState) {
                HwHiLog.d(MessageUtil.TAG, false, "mIsInitialState state is disable", new Object[0]);
                HwIntelligenceStateMachine.this.mIsInitialState = false;
            } else if (!HwIntelligenceStateMachine.this.isClosedByUser()) {
                HwHiLog.d(MessageUtil.TAG, false, "MSG_WIFI_DISABLE by auto", new Object[0]);
            } else {
                if (HwIntelligenceStateMachine.this.mIsWaittingAutoClose) {
                    HwHiLog.d(MessageUtil.TAG, false, "DisabledState remove MSG_WIFI_HANDLE_DISABLE", new Object[0]);
                    HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
                    HwintelligenceWiFiCHR hwintelligenceWiFiCHR = HwIntelligenceStateMachine.this.mHwIntelligenceWifiChr;
                    HwintelligenceWiFiCHR unused = HwIntelligenceStateMachine.this.mHwIntelligenceWifiChr;
                    hwintelligenceWiFiCHR.uploadAutoCloseFailed(2);
                }
                if (HwIntelligenceStateMachine.this.mIsAutoClose) {
                    HwIntelligenceStateMachine.this.mIsAutoClose = false;
                    HwIntelligenceStateMachine.this.setAutoOpenValue(false);
                    if (!HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                        HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
                        HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                        HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                    }
                    HwIntelligenceStateMachine.this.mHwIntelligenceWifiChr.increaseAutoCloseCount();
                } else {
                    HwIntelligenceStateMachine hwIntelligenceStateMachine = HwIntelligenceStateMachine.this;
                    if (hwIntelligenceStateMachine.isScreenOn(hwIntelligenceStateMachine.mContext)) {
                        HwIntelligenceStateMachine.this.setAutoOpenValue(false);
                        HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
                        HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
                        HwIntelligenceStateMachine.this.mApInfoManager.stopScanAp();
                        List<ScanResult> mlist = null;
                        if (HwIntelligenceStateMachine.this.mWifiManager.isScanAlwaysAvailable()) {
                            mlist = WifiproUtils.getScanResultsFromWsm();
                        }
                        if (mlist == null || mlist.size() == 0) {
                            HwHiLog.d(MessageUtil.TAG, false, "getScanResultsFromWsm is null, get from WiFiProScanResultList.", new Object[0]);
                            mlist = HwIntelligenceWiFiManager.getWiFiProScanResultList();
                        }
                        if (HwIntelligenceStateMachine.this.mTargetSsid != null) {
                            if (!(HwIntelligenceStateMachine.this.mTargetWifiInfo == null || HwIntelligenceStateMachine.this.mTargetCellId == null || HwIntelligenceStateMachine.this.mApInfoManager.getApInfoByBssid(HwIntelligenceStateMachine.this.mTargetWifiInfo.getBSSID()) != null)) {
                                HwIntelligenceStateMachine.this.mApInfoManager.addTargetApInfo(HwIntelligenceStateMachine.this.mTargetCellId, HwIntelligenceStateMachine.this.mTargetWifiInfo);
                            }
                            HwHiLog.d(MessageUtil.TAG, false, "processManualClose add into blacklist. mTargetSsid is %{public}s mAuthType = %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(HwIntelligenceStateMachine.this.mTargetSsid), Integer.valueOf(HwIntelligenceStateMachine.this.mAuthType)});
                            HwIntelligenceStateMachine.this.mApInfoManager.setBlackListBySsid(HwIntelligenceStateMachine.this.mTargetSsid, HwIntelligenceStateMachine.this.mAuthType, true);
                        }
                        HwIntelligenceStateMachine.this.mTargetSsid = null;
                        HwIntelligenceStateMachine.this.mTargetWifiInfo = null;
                        HwIntelligenceStateMachine.this.mTargetCellId = null;
                        HwIntelligenceStateMachine.this.mAuthType = -1;
                        HwIntelligenceStateMachine.this.mApInfoManager.resetBlackList(mlist, true);
                        HwIntelligenceWiFiManager.setWiFiProScanResultList(null);
                    } else {
                        HwHiLog.w(MessageUtil.TAG, false, " Enter DisabledState mIsAutoClose is false and ScreenOff", new Object[0]);
                    }
                }
                HwIntelligenceStateMachine.this.mHwIntelligenceWifiChr.stopConnectTimer();
                HwIntelligenceStateMachine.this.initPunishParameter();
            }
        }

        public void exit() {
            HwHiLog.d(MessageUtil.TAG, false, "DisabledState exit", new Object[0]);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 2) {
                return true;
            }
            if (i != 7) {
                if (!(i == 20 || i == 23)) {
                    if (i == 26) {
                        HwIntelligenceStateMachine.this.handleMsgWifiOpen();
                        return true;
                    } else if (i != 28) {
                        if (i == 102) {
                            HwHiLog.i(MessageUtil.TAG, false, "CMD_START_SCAN", new Object[0]);
                            HwIntelligenceStateMachine.this.mWifiManager.startScan();
                            return true;
                        } else if (i == 4) {
                            return true;
                        } else {
                            if (i != 5) {
                                return false;
                            }
                            HwIntelligenceStateMachine.this.handleMsgWifiFindTarget();
                            return true;
                        }
                    } else if (HwIntelligenceStateMachine.this.isFullScreen()) {
                        return true;
                    }
                }
                HwIntelligenceStateMachine.this.handleMsgStateChange(message);
                return true;
            }
            HwHiLog.d(MessageUtil.TAG, false, "DisabledState MSG_WIFI_UPDATE_SCAN_RESULT", new Object[0]);
            if (!HwIntelligenceStateMachine.this.mIsAutoOpenSearch) {
                return true;
            }
            HwIntelligenceStateMachine.this.mApInfoManager.updateScanResult();
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMsgWifiFindTarget() {
        HwHiLog.d(MessageUtil.TAG, false, "DisabledState MSG_WIFI_FIND_TARGET", new Object[0]);
        this.mApInfoManager.stopScanAp();
        sendMessageDelayed(26, 3000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMsgWifiOpen() {
        this.mAutoOpenCnt++;
        if (this.mIsAutoOpenSearch) {
            this.mIsScreenOn = isScreenOn(this.mContext);
            HwHiLog.d(MessageUtil.TAG, false, "MSG_WIFI_HANDLE_OPEN mWifiManager.getWifiState() = %{public}d ,mIsScreenOn = %{public}s ,mIsFullScreen = %{public}s ,isWifiApEnabled = %{public}s", new Object[]{Integer.valueOf(this.mWifiManager.getWifiState()), String.valueOf(this.mIsScreenOn), String.valueOf(isFullScreen()), String.valueOf(isWifiApEnablingOrEnabled())});
            if (this.mWifiManager.getWifiState() == 1 && this.mIsScreenOn && !isFullScreen() && !isWifiApEnablingOrEnabled()) {
                setAutoOpenValue(true);
                this.mWifiManager.setWifiEnabled(true);
                this.mHwIntelligenceWifiChr.startConnectTimer();
                this.mHwIntelligenceWifiChr.increaseAutoOpenCount();
                WifiProChrUploadManager wifiProChrUploadManager = this.mUploadManager;
                if (wifiProChrUploadManager != null) {
                    wifiProChrUploadManager.addChrCntStat(AUTO_OPEN_EVENT, AUTO_OPEN_SUCC_EVENT);
                }
            } else if (this.mWifiManager.getWifiState() == 0 && this.mIsScreenOn && !isFullScreen() && !isWifiApEnablingOrEnabled()) {
                sendMessageDelayed(26, 3000);
            }
        }
    }

    private boolean isWifiApEnablingOrEnabled() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            return false;
        }
        if (wifiManager.getWifiApState() == 12 || this.mWifiManager.getWifiApState() == 13) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMsgStateChange(Message message) {
        List<APInfoData> list;
        List<ScanResult> mLists;
        this.mIsScreenOn = isScreenOn(this.mContext);
        String cellid = this.mCellStateMonitor.getCurrentCellid();
        HwHiLog.d(MessageUtil.TAG, false, "DisabledState cellid = %{private}s", new Object[]{cellid});
        if (cellid != null) {
            if (this.mApInfoManager.isMonitorCellId(cellid)) {
                if (this.mIsWaittingAutoClose) {
                    this.mIsWaittingAutoClose = false;
                }
                this.mIsFoundInBlackList = true;
                this.mSmartSceneOn = 0;
                HwHiLog.d(MessageUtil.TAG, false, "DisabledState current cell id is monitor ..... cellid = %{private}s", new Object[]{cellid});
                this.mTargetApInfoDatas = removeFromBlackList(this.mApInfoManager.getMonitorDatas(cellid));
                if (this.mIsFoundInBlackList) {
                    this.mSmartSceneOn = 3;
                }
                if (this.mTargetApInfoDatas.size() > 0) {
                    HwHiLog.d(MessageUtil.TAG, false, "DisabledState mTargetApInfoDatas.size() =%{public}d", new Object[]{Integer.valueOf(this.mTargetApInfoDatas.size())});
                    if (getSettingSwitchType() && this.mIsScreenOn && Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_on", 0) == 0) {
                        if (message.what == 20) {
                            setPingpongPunishTime();
                            if (isInPingpongPunishTime()) {
                                this.mSmartSceneOn = 2;
                                HwHiLog.d(MessageUtil.TAG, false, "DisabledState in punish time can not scan", new Object[0]);
                                return;
                            }
                            this.mLastScanPingpongTime = System.currentTimeMillis();
                        }
                        HwHiLog.d(MessageUtil.TAG, false, "DisabledState start auto open search", new Object[0]);
                        this.mIsAutoOpenSearch = true;
                        this.mApInfoManager.startScanAp();
                        return;
                    }
                    return;
                }
                HwHiLog.d(MessageUtil.TAG, false, "DisabledState mTargetApInfoDatas.size() == 0", new Object[0]);
                this.mIsAutoOpenSearch = false;
                this.mApInfoManager.stopScanAp();
            } else if (!this.mIsAutoOpenSearch || (list = this.mTargetApInfoDatas) == null || list.size() <= 0 || (mLists = WifiproUtils.getScanResultsFromWsm()) == null || mLists.size() <= 0 || !this.mApInfoManager.isHasTargetAp(mLists)) {
                HwHiLog.d(MessageUtil.TAG, false, "current cell id is not monitor ..... cellid = %{private}s", new Object[]{cellid});
                this.mSmartSceneOn = 0;
                this.mIsAutoOpenSearch = false;
                this.mApInfoManager.stopScanAp();
            } else {
                HwHiLog.d(MessageUtil.TAG, false, "DisabledState Learn new Cell id", new Object[0]);
                this.mApInfoManager.processScanResult(this.mCellStateMonitor.getCurrentCellid());
                this.mApInfoManager.updateScanResult();
            }
        }
    }

    class StopState extends State {
        StopState() {
        }

        public void enter() {
            HwHiLog.d(MessageUtil.TAG, false, "StopState", new Object[0]);
            HwIntelligenceStateMachine.this.mTargetApInfoDatas = null;
            HwIntelligenceStateMachine.this.setAutoOpenValue(false);
            HwIntelligenceStateMachine.this.mIsAutoClose = false;
            HwIntelligenceStateMachine.this.mIsAutoOpenSearch = false;
            HwIntelligenceStateMachine.this.mIsAutoCloseSearch = false;
            HwIntelligenceStateMachine.this.mIsWifiP2PConnected = false;
            HwIntelligenceStateMachine.this.mIsWaittingAutoClose = false;
        }

        public boolean processMessage(Message message) {
            if (message.what != 100) {
                return true;
            }
            HwIntelligenceStateMachine hwIntelligenceStateMachine = HwIntelligenceStateMachine.this;
            hwIntelligenceStateMachine.transitionTo(hwIntelligenceStateMachine.mInitialState);
            return true;
        }
    }

    public static HwIntelligenceStateMachine createIntelligenceStateMachine(Context context) {
        if (sHwIntelligenceStateMachine == null) {
            sHwIntelligenceStateMachine = new HwIntelligenceStateMachine(context);
        }
        return sHwIntelligenceStateMachine;
    }

    public static HwIntelligenceStateMachine getIntelligenceStateMachine() {
        return sHwIntelligenceStateMachine;
    }

    public ApInfoManager getApInfoManager() {
        return this.mApInfoManager;
    }

    private HwIntelligenceStateMachine(Context context) {
        super("HwIntelligenceStateMachine");
        this.mContext = context;
        this.mHandler = getHandler();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mHwIntelligenceWifiChr = HwintelligenceWiFiCHR.getInstance(this);
        this.mWiFiStateMonitor = new WiFiStateMonitor(context, getHandler());
        this.mCellStateMonitor = new CellStateMonitor(context, getHandler());
        this.mApInfoManager = new ApInfoManager(context, this, getHandler());
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mUploadManager = WifiProChrUploadManager.getInstance(this.mContext);
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
    /* access modifiers changed from: public */
    private void setAutoTimer(int message) {
        HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState setAutoTimer message = %{public}d", new Object[]{Integer.valueOf(message)});
        if (this.mAutoCloseMessage == message) {
            HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState setAutoTimer mAutoCloseMessage == message", new Object[0]);
        } else if (message == 25) {
            this.mAutoCloseMessage = message;
            int i = this.mAutoCloseScanTimes;
            if (i >= 1) {
                HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState setAutoTimer mAutoCloseScanTimes >= 1", new Object[0]);
                return;
            }
            HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState setAutoTimer mAutoCloseScanTimes =%{public}d", new Object[]{Integer.valueOf(i)});
            this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 120000, MessageUtil.TAG, this.mAutoCloseTimeoutListener, getHandler());
            this.mAutoCloseScanTimes++;
        } else {
            this.mAutoCloseMessage = message;
            this.mAlarmManager.set(2, 120000 + SystemClock.elapsedRealtime(), MessageUtil.TAG, this.mAutoCloseTimeoutListener, getHandler());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseAutoTimer() {
        HwHiLog.d(MessageUtil.TAG, false, "DisconnectedState releaseAutoTimer", new Object[0]);
        this.mAutoCloseMessage = 0;
        this.mAlarmManager.cancel(this.mAutoCloseTimeoutListener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isScreenOn(Context context) {
        Object currentPowerManager = context.getSystemService("power");
        PowerManager pm = null;
        if (currentPowerManager instanceof PowerManager) {
            pm = (PowerManager) currentPowerManager;
        } else {
            HwHiLog.w(MessageUtil.TAG, false, "isScreenOn:class is not match", new Object[0]);
        }
        if (pm == null || !pm.isScreenOn()) {
            return false;
        }
        return true;
    }

    private boolean isAirModeOn() {
        Context context = this.mContext;
        if (context != null && Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", 0) == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isClosedByUser() {
        if (isAirModeOn()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAutoOpenValue(boolean enable) {
        HwHiLog.d(MessageUtil.TAG, false, "setAutoOpenValue =%{public}s", new Object[]{String.valueOf(enable)});
        Settings.System.putInt(this.mContext.getContentResolver(), MessageUtil.WIFIPRO_AUTO_OPEN_STATE, enable ? 1 : 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getAutoOpenValue() {
        HwHiLog.d(MessageUtil.TAG, false, "getAutoOpenValue  value = %{public}d", new Object[]{Integer.valueOf(Settings.System.getInt(this.mContext.getContentResolver(), MessageUtil.WIFIPRO_AUTO_OPEN_STATE, 0))});
        return false;
    }

    private List<APInfoData> removeFromBlackList(List<APInfoData> datas) {
        ArrayList<APInfoData> result = new ArrayList<>();
        for (APInfoData data : datas) {
            HwHiLog.d(MessageUtil.TAG, false, "removeFromBlackList ssid = %{public}s, isInBlackList = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(data.getSsid()), String.valueOf(data.isInBlackList())});
            if (!data.isInBlackList()) {
                this.mIsFoundInBlackList = false;
                result.add(data);
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadAutoOpenWifiFailed(boolean isMatchHomeScene) {
        int i = 1;
        HwHiLog.d(MessageUtil.TAG, false, "uploadAutoOpenWifiFailed isMatchHomeScene = %{public}s", new Object[]{String.valueOf(isMatchHomeScene)});
        if (SystemClock.elapsedRealtime() - this.mUploadAutoOpenWifiFailedTime >= this.mUploadIntervalTime) {
            Object currentLocMgr = this.mContext.getSystemService("location");
            LocationManager locMgr = null;
            if (currentLocMgr instanceof LocationManager) {
                locMgr = (LocationManager) currentLocMgr;
            }
            boolean isGpsOn = false;
            if (locMgr != null) {
                isGpsOn = locMgr.isProviderEnabled("gps");
            } else {
                HwHiLog.w(MessageUtil.TAG, false, "uploadAutoOpenWifiFailed:Class is not match", new Object[0]);
            }
            if (!this.mWifiManager.isScanAlwaysAvailable()) {
                this.mSmartSceneOn = 1;
            }
            HwHiLog.d(MessageUtil.TAG, false, "isMatchHomeScene = %{public}s, isGpsOn = %{public}s, mSmartSceneOn= %{public}d", new Object[]{String.valueOf(isMatchHomeScene), String.valueOf(isGpsOn), Integer.valueOf(this.mSmartSceneOn)});
            if (this.mSmartSceneOn == 0) {
                HwHiLog.d(MessageUtil.TAG, false, "wifi always scan enable, and ap not in blacklist.", new Object[0]);
                return;
            }
            if (!isMatchHomeScene) {
                this.mUploadIntervalTime = 604800000;
            } else {
                this.mUploadIntervalTime = 43200000;
            }
            this.mUploadAutoOpenWifiFailedTime = SystemClock.elapsedRealtime();
            Bundle data = new Bundle();
            data.putInt("isMatchHomeScene", isMatchHomeScene ? 1 : 0);
            if (!isGpsOn) {
                i = 0;
            }
            data.putInt("isGPSOn", i);
            data.putInt("isSmartSceneOn", this.mSmartSceneOn);
            Bundle dftEventData = new Bundle();
            dftEventData.putInt("eventId", 909002064);
            dftEventData.putBundle("eventData", data);
            WifiProManagerEx.ctrlHwWifiNetwork("WIFIPRO_SERVICE", 2, dftEventData);
        }
    }

    private boolean getSettingSwitchType() {
        HwHiLog.d(MessageUtil.TAG, false, "getSettingSwitchType in", new Object[0]);
        int select = Settings.System.getInt(this.mContext.getContentResolver(), MessageUtil.WIFI_CONNECT_TYPE, 0);
        HwHiLog.d(MessageUtil.TAG, false, "getSettingSwitchType select = %{public}d", new Object[]{Integer.valueOf(select)});
        if (select == 1) {
            return false;
        }
        return true;
    }

    public List<APInfoData> getTargetApInfoDatas() {
        return this.mTargetApInfoDatas;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void autoDisbleWiFi() {
        HwHiLog.d(MessageUtil.TAG, false, "autoDisbleWiFi close WIFI", new Object[0]);
        this.mIsAutoClose = true;
        setAutoOpenValue(false);
        this.mWifiManager.setWifiEnabled(false);
    }

    public synchronized void onStart() {
        HwHiLog.d(MessageUtil.TAG, false, "onStart mIsMachineStared = %{public}s", new Object[]{String.valueOf(this.mIsMachineStared)});
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
        HwHiLog.d(MessageUtil.TAG, false, "onStop mIsMachineStared = %{public}s", new Object[]{String.valueOf(this.mIsMachineStared)});
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
            HwHiLog.d(MessageUtil.TAG, false, "setPingpongPunishTime mLastCellChangeScanTime = %{public}s", new Object[]{String.valueOf(this.mLastCellChangeScanTime)});
            if (this.mLastCellChangeScanTime == 0) {
                this.mLastCellChangeScanTime = System.currentTimeMillis();
                return;
            }
            if (System.currentTimeMillis() - this.mLastCellChangeScanTime < 5000) {
                HwHiLog.d(MessageUtil.TAG, false, "setPingpongPunishTime is inPunish time", new Object[0]);
                if (this.mLastScanPingpongTime == 0) {
                    this.mScanPingpongNum = 1;
                    this.mLastScanPingpongTime = System.currentTimeMillis();
                } else {
                    if (System.currentTimeMillis() - this.mLastScanPingpongTime > 1800000) {
                        this.mScanPingpongNum = 1;
                    } else {
                        this.mScanPingpongNum++;
                    }
                    HwHiLog.d(MessageUtil.TAG, false, "setPingpongPunishTime mScanPingpongNum = %{public}d", new Object[]{Integer.valueOf(this.mScanPingpongNum)});
                }
            } else {
                HwHiLog.d(MessageUtil.TAG, false, "setPingpongPunishTime is not inPunish time", new Object[0]);
            }
            this.mLastCellChangeScanTime = System.currentTimeMillis();
        }
    }

    private boolean isInPingpongPunishTime() {
        HwHiLog.d(MessageUtil.TAG, false, "isInPingpongPunishTime mScanPingpongNum = %{public}d", new Object[]{Integer.valueOf(this.mScanPingpongNum)});
        int punishTime = this.mScanPingpongNum * 30000;
        if (punishTime > 60000) {
            punishTime = 60000;
        }
        if (System.currentTimeMillis() - this.mLastScanPingpongTime < ((long) punishTime)) {
            HwHiLog.d(MessageUtil.TAG, false, "isInPingpongPunishTime punishTime = %{public}d", new Object[]{Integer.valueOf(punishTime)});
            return true;
        }
        HwHiLog.d(MessageUtil.TAG, false, "isInPingpongPunishTime is not in punishTime", new Object[0]);
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

    public int getAutoOpenCnt() {
        return this.mAutoOpenCnt;
    }

    public void setAutoOpenCnt(int autoOpenCnt) {
        this.mAutoOpenCnt = autoOpenCnt;
    }
}
