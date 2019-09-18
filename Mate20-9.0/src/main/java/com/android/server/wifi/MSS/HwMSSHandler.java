package com.android.server.wifi.MSS;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.wifi.HwArpUtils;
import com.android.server.wifi.HwMSSHandlerManager;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.MSS.HwMSSArbitrager;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.wificond.NativeMssResult;
import java.util.ArrayList;
import java.util.List;

@SuppressLint({"HandlerLeak"})
public class HwMSSHandler implements HwMSSHandlerManager, HwMSSArbitrager.IHwMSSObserver {
    private static final int KOG_MODE_HT40 = 99;
    private static final int MAX_FREQ_24G = 2484;
    private static final int MIN_FREQ_24G = 2412;
    private static final int MSG_HT_40 = 99;
    private static final int MSG_SET_RADIO_STATE = 100;
    private static final int MSG_SET_RADIO_STATE_FORCE = 101;
    private static final int MSS_CHECK_DISCONN_ERR = 3;
    private static final int MSS_CHECK_PING_ERR = 2;
    private static final int MSS_CHECK_STATE_ERR = 1;
    private static final int MSS_FORCE_TO_MIMO = 1;
    private static final int MSS_FORCE_TO_SISO = 3;
    private static final int MSS_MAX_SWITCH_OPERATION = 4;
    private static final int MSS_MIMO_CHAIN_NUM = 3;
    private static final int MSS_RSSI_SWITCH_COUNT = 5;
    private static final int MSS_RSSI_SWITCH_MIMO_TRESH_2G = -76;
    private static final int MSS_RSSI_SWITCH_MIMO_TRESH_5G = -76;
    private static final int MSS_RSSI_SWITCH_SISO_TRESH_2G = -65;
    private static final int MSS_RSSI_SWITCH_SISO_TRESH_5G = -65;
    private static final int MSS_SISO_CHAIN_NUM = 1;
    private static final int MSS_START = 0;
    private static final int MSS_TEMP_SWITCH_COUNT = 1;
    private static final int MSS_TEMP_SWITCH_SISO_TRESH = 100;
    private static final int MSS_TPUT_SWITCH_MIMO_COUNT = 3;
    private static final int MSS_TPUT_SWITCH_MIMO_TRESH_2G = 30;
    private static final int MSS_TPUT_SWITCH_MIMO_TRESH_5G = 90;
    private static final int MSS_TPUT_SWITCH_SISO_COUNT = 3;
    private static final int MSS_TPUT_SWITCH_SISO_TRESH_2G = 10;
    private static final int MSS_TPUT_SWITCH_SISO_TRESH_5G = 10;
    private static final int RESTORE_CURRENT_MSS_STATE = 2;
    private static final int SET_RADIO_STATE_DELAYED = 2000;
    public static final int SUPER_MODE = 4;
    private static final String TAG = "HwMSSHandler";
    private static final String THERMAL_TO_WIFI = "huawei.intent.action.THERMAL_TO_WIFI";
    private static final int WIFI_THERMAL_ACTION_COUNT = 3;
    private static final int WIFI_THERMAL_LEVEL1 = 1;
    private static final int WIFI_THERMAL_LEVEL5 = 5;
    private static final int WIFI_THERMAL_MIMO_TO_SISO = 1001;
    private static final int WIFI_THERMAL_MIMO_TO_SISO_INTERVAL = 3000;
    private static HwMSSHandler mInstance = null;
    private static int mRestoreCount = 0;
    private static int mRssiMIMOCount = 0;
    private static int mRssiSISOCount = 0;
    private static int mTputMIMOCount = 0;
    private static int mTputSISOCount = 0;
    private int MSS_STATE_RES_CYCLE = 20;
    private IHwMSSBlacklistMgr blackMgr = null;
    public ArrayList list;
    /* access modifiers changed from: private */
    public boolean m2GHT40Enabled = false;
    private ActivityManager mActivityManager;
    /* access modifiers changed from: private */
    public String mCellPhoneWIFIIface = "wlan0";
    /* access modifiers changed from: private */
    public int mCellPhoneWIFIMode = -1;
    /* access modifiers changed from: private */
    public int mCellPhoneWIFIOperation = -1;
    private Context mContext = null;
    private int mCurrentRssi = 0;
    private int mCurrentTemp = 0;
    private int mCurrentTput = 0;
    /* access modifiers changed from: private */
    public Handler mHandler = null;
    /* access modifiers changed from: private */
    public boolean mHasPerformanceApp = false;
    /* access modifiers changed from: private */
    public HisiMSSStateMachine mHisiMssStateMachine = null;
    private HwProcessObserver mHwProcessObserver;
    /* access modifiers changed from: private */
    public boolean mIs1103 = false;
    private int mIsDisconnectHappened = 0;
    private int mIsSuppCompleted = 0;
    private HwMSSBluetoothManager mMSSBluetoothManager = null;
    private int mMSSDirection = 0;
    /* access modifiers changed from: private */
    public int mMonitorUid = 0;
    private boolean mRadioActive = true;
    private int mTXGood = 0;
    private int mTXbad = 0;
    /* access modifiers changed from: private */
    public int mTempSISOCount = 0;
    /* access modifiers changed from: private */
    public int mThermalLevel = -1;
    /* access modifiers changed from: private */
    public int mThermalScene = -1;
    /* access modifiers changed from: private */
    public int mThermalTemp = -1;
    /* access modifiers changed from: private */
    public int mTriggerReason;
    private int mTxbad_Last = 0;
    private int mTxgood_Last = 0;
    /* access modifiers changed from: private */
    public boolean mWiFiApMode = false;
    /* access modifiers changed from: private */
    public boolean mWifiConnected = false;
    /* access modifiers changed from: private */
    public boolean mWifiEnabled = false;
    private WifiInfo mWifiInfo = null;
    /* access modifiers changed from: private */
    public WifiNative mWifiNative = null;
    private WifiStateReceiver mWifiStateReceiver = null;
    /* access modifiers changed from: private */
    public HwMSSArbitrager mssArbi = null;
    private boolean mssIsHighTput = false;
    private HwWifiCHRService wcsm = null;

    private class HwProcessObserver extends IProcessObserver.Stub {
        private HwProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (HwMSSHandler.this.matchPerformanceScene()) {
                String currentAppName = HwMSSHandler.this.getAppNameUid(uid);
                if (foregroundActivities) {
                    HwMSSUtils.loge(HwMSSHandler.TAG, "switch to foreground: " + currentAppName);
                    if (HwMSSUtils.PERFORMANCEAPP.equals(currentAppName)) {
                        int unused = HwMSSHandler.this.mMonitorUid = uid;
                        HwMSSHandler.this.handleRadioAction(0, 0);
                    } else {
                        int unused2 = HwMSSHandler.this.mMonitorUid = 0;
                        HwMSSHandler.this.handleRadioAction(1, 0);
                    }
                }
            }
        }

        public void onProcessDied(int pid, int uid) {
            if (uid > 0 && HwMSSHandler.this.mMonitorUid == uid) {
                HwMSSUtils.loge(HwMSSHandler.TAG, "onProcessDied: com.example.wptp.testapp");
                HwMSSHandler.this.handleRadioAction(1, 0);
            }
        }
    }

    private enum MSS_TRIG_ARBITRATE_TYPE {
        RSSI_TRIG,
        TPUT_TRIG,
        TEMP_TRIG
    }

    private class WifiStateReceiver extends BroadcastReceiver {
        private WifiStateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                    SupplicantState state = (SupplicantState) intent.getParcelableExtra("newState");
                    if (state != null) {
                        Log.d(HwMSSHandler.TAG, "WifiStateReceiver:" + action + " SupplicantState:" + state);
                    }
                    if (state == SupplicantState.DISCONNECTED) {
                        boolean unused = HwMSSHandler.this.mWifiConnected = false;
                        HwMSSHandler.this.setWifiDisconnectedMSSState();
                    }
                }
                if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo != null) {
                        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                            if (HwMSSHandler.this.mIs1103 && HwMSSHandler.this.mHisiMssStateMachine != null) {
                                HwMSSHandler.this.mHisiMssStateMachine.sendMessage(10);
                            }
                            boolean unused2 = HwMSSHandler.this.mWifiConnected = true;
                            HwMSSHandler.this.mHandler.sendMessage(HwMSSHandler.this.mHandler.obtainMessage(2, 0));
                            HwMSSHandler.this.handleWifiConnected();
                        } else if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED && HwMSSHandler.this.mIs1103 && HwMSSHandler.this.mHisiMssStateMachine != null) {
                            HwMSSHandler.this.mHisiMssStateMachine.sendMessage(11);
                        }
                        HwMSSHandler.this.clearMssCount();
                    } else {
                        return;
                    }
                }
                if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    Log.d(HwMSSHandler.TAG, "WifiStateReceiver:" + action);
                    HwMSSHandler.this.mHandler.sendMessage(HwMSSHandler.this.mHandler.obtainMessage(2, 0));
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    int state2 = intent.getIntExtra("wifi_state", 4);
                    if (3 == state2) {
                        boolean unused3 = HwMSSHandler.this.mWifiEnabled = true;
                        boolean unused4 = HwMSSHandler.this.mHasPerformanceApp = HwMSSHandler.this.mssArbi.isInstalledApp(HwMSSUtils.PERFORMANCEAPP);
                        HwMSSUtils.loge(HwMSSHandler.TAG, "hasPerformanceApp: " + HwMSSHandler.this.mHasPerformanceApp);
                        boolean unused5 = HwMSSHandler.this.m2GHT40Enabled = false;
                        if (HwMSSHandler.this.mssArbi.matchHT40List()) {
                            HwMSSHandler.this.mHandler.sendMessage(HwMSSHandler.this.mHandler.obtainMessage(99, 1));
                        }
                        if (HwMSSHandler.this.mIs1103 && HwMSSHandler.this.mHisiMssStateMachine != null) {
                            HwMSSHandler.this.mHisiMssStateMachine.sendMessage(12);
                        }
                    } else if (1 == state2) {
                        boolean unused6 = HwMSSHandler.this.mWifiEnabled = false;
                        if (HwMSSHandler.this.mIs1103 && HwMSSHandler.this.mHisiMssStateMachine != null) {
                            HwMSSHandler.this.mHisiMssStateMachine.sendMessage(13);
                        }
                    } else {
                        boolean unused7 = HwMSSHandler.this.mWifiEnabled = false;
                    }
                }
                if (HwMSSHandler.THERMAL_TO_WIFI.equals(action)) {
                    int unused8 = HwMSSHandler.this.mThermalLevel = intent.getIntExtra("level", -1);
                    int unused9 = HwMSSHandler.this.mThermalTemp = intent.getIntExtra("temp", -1);
                    int unused10 = HwMSSHandler.this.mThermalScene = intent.getIntExtra("scene", -1);
                    Log.d(HwMSSHandler.TAG, "receive thermal brocast temperature is:" + HwMSSHandler.this.mThermalTemp + " ,level is:" + HwMSSHandler.this.mThermalLevel + " ,Scene is:" + HwMSSHandler.this.mThermalScene);
                    if (HwMSSHandler.this.mThermalLevel == 0) {
                        HwMSSHandler.this.clearTempCount(true);
                    } else if (HwMSSHandler.this.mThermalLevel >= 1 && HwMSSHandler.this.mThermalLevel <= 5 && !HwMSSHandler.this.mHandler.hasMessages(HwMSSHandler.WIFI_THERMAL_MIMO_TO_SISO)) {
                        HwMSSHandler.this.mHandler.sendMessage(HwMSSHandler.this.mHandler.obtainMessage(HwMSSHandler.WIFI_THERMAL_MIMO_TO_SISO, 0));
                    }
                }
                if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                    int apState = intent.getIntExtra("wifi_state", 11);
                    if (apState == 11) {
                        boolean unused11 = HwMSSHandler.this.mWiFiApMode = false;
                        HwMSSHandler.this.mssArbi.setSisoFixFlag(false);
                    } else if (apState == 13) {
                        boolean unused12 = HwMSSHandler.this.mWiFiApMode = true;
                    }
                }
                if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                    HwMSSHandler.this.handleSimStateChanged(intent);
                }
                if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                    HwMSSHandler.this.handleAirplaneModeChanged();
                }
            }
        }
    }

    public static synchronized HwMSSHandlerManager getDefault(Context cxt, WifiNative wifinav, WifiInfo wifiinfo) {
        HwMSSHandler hwMSSHandler;
        synchronized (HwMSSHandler.class) {
            if (mInstance == null) {
                mInstance = new HwMSSHandler(cxt, wifinav, wifiinfo);
            }
            hwMSSHandler = mInstance;
        }
        return hwMSSHandler;
    }

    public static synchronized HwMSSHandler getInstance() {
        synchronized (HwMSSHandler.class) {
            if (mInstance == null) {
                return null;
            }
            HwMSSHandler hwMSSHandler = mInstance;
            return hwMSSHandler;
        }
    }

    private boolean isRssiBelowTwoCells() {
        int freq = this.mWifiInfo.getFrequency();
        if (freq < MIN_FREQ_24G || freq > MAX_FREQ_24G) {
            if (freq > MAX_FREQ_24G && this.mCurrentRssi <= -76) {
                return true;
            }
        } else if (this.mCurrentRssi <= -76) {
            return true;
        }
        return false;
    }

    private boolean isRssiFourCells() {
        int freq = this.mWifiInfo.getFrequency();
        if (freq < MIN_FREQ_24G || freq > MAX_FREQ_24G) {
            if (freq > MAX_FREQ_24G && this.mCurrentRssi >= -65) {
                return true;
            }
        } else if (this.mCurrentRssi >= -65) {
            return true;
        }
        return false;
    }

    private HwMSSHandler(Context cxt, WifiNative wifinav, WifiInfo wifiinfo) {
        this.mContext = cxt;
        this.mWifiNative = wifinav;
        this.mWifiInfo = wifiinfo;
        this.mssArbi = HwMSSArbitrager.getInstance(this.mContext);
        this.mIs1103 = HwMSSUtils.is1103();
        if (this.mIs1103) {
            this.MSS_STATE_RES_CYCLE = 120;
            HwMSSUtils.setAllowSwitch(false);
        } else {
            this.blackMgr = HwMSSBlackListManager.getInstance(this.mContext);
        }
        this.wcsm = HwWifiCHRServiceImpl.getInstance();
        HwMSSHandlerInit();
        mssCheckInit(this.mContext);
        this.mssArbi.registerMSSObserver(this);
        if (SystemProperties.getInt("ro.config.hw_wifi_btc_mss_en", 0) == 1) {
            this.mMSSBluetoothManager = HwMSSBluetoothManager.getInstance(cxt);
            this.mMSSBluetoothManager.init(wifinav, wifiinfo);
        }
    }

    private void HwMSSHandlerInit() {
        HandlerThread handlerThread = new HandlerThread("mss_handler_thread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                if (HwMSSHandler.this.mIs1103) {
                    handleMessageForHisi(msg);
                } else {
                    handleMessageForBrcm(msg);
                }
                super.handleMessage(msg);
            }

            private void handleMessageForBrcm(Message msg) {
                int i = msg.what;
                if (i != HwMSSHandler.WIFI_THERMAL_MIMO_TO_SISO) {
                    switch (i) {
                        case 0:
                            if (HwMSSHandler.this.doMssSwitch(msg.arg1)) {
                                int unused = HwMSSHandler.this.mTriggerReason = msg.arg2;
                                HwMSSHandler.this.handleMssResultForBrcm(HwMSSHandler.this.mssResultCheck());
                                return;
                            }
                            return;
                        case 1:
                            HwMSSHandler.this.mssArbi.setSisoFixFlag(false);
                            HwMSSHandler.this.mWifiNative.hwABSSoftHandover(2);
                            HwMSSHandler.this.mssArbi.setMSSCurrentState(HwMSSArbitrager.MSSState.MSSMIMO);
                            HwMSSHandler.this.clearMssCount();
                            return;
                        case 2:
                            HwMSSHandler.this.restoreCurrentChainState();
                            return;
                        case 3:
                            int result = HwMSSHandler.this.setWifiAntImpl(HwMSSHandler.this.mCellPhoneWIFIIface, HwMSSHandler.this.mCellPhoneWIFIMode, HwMSSHandler.this.mCellPhoneWIFIOperation);
                            Log.d(HwMSSHandler.TAG, "setWifiAntImpl result is " + result);
                            HwMSSHandler.this.clearMssCount();
                            return;
                        default:
                            switch (i) {
                                case 99:
                                    HwMSSHandler.this.enable2GHT40Band();
                                    return;
                                case 100:
                                case 101:
                                    HwMSSHandler.this.handleRadioMessage(msg.arg1, msg.arg2);
                                    return;
                                default:
                                    return;
                            }
                    }
                } else if (HwMSSHandler.this.mThermalLevel >= 1 && HwMSSHandler.this.mThermalLevel <= 5) {
                    int unused2 = HwMSSHandler.this.mTempSISOCount = HwMSSHandler.this.mTempSISOCount + 1;
                    if (HwMSSHandler.this.mTempSISOCount == 3) {
                        HwMSSHandler.this.tempChangeWiFiState(HwMSSHandler.WIFI_THERMAL_MIMO_TO_SISO);
                        HwMSSHandler.this.clearTempCount(false);
                    }
                    if (!HwMSSHandler.this.mHandler.hasMessages(HwMSSHandler.WIFI_THERMAL_MIMO_TO_SISO)) {
                        sendEmptyMessageDelayed(HwMSSHandler.WIFI_THERMAL_MIMO_TO_SISO, 3000);
                    }
                }
            }

            private void handleMessageForHisi(Message msg) {
                int i = msg.what;
                if (i != HwMSSHandler.WIFI_THERMAL_MIMO_TO_SISO) {
                    switch (i) {
                        case 0:
                            if (HwMSSHandler.this.doMssSwitchForHisi(msg.arg1, false)) {
                                int unused = HwMSSHandler.this.mTriggerReason = msg.arg2;
                                return;
                            }
                            return;
                        case 1:
                            HwMSSHandler.this.mssArbi.setSisoFixFlag(false);
                            boolean unused2 = HwMSSHandler.this.doMssSwitchForHisi(2, true);
                            return;
                        case 2:
                            HwMSSHandler.this.restoreCurrentChainStateForHisi();
                            return;
                        case 3:
                            boolean unused3 = HwMSSHandler.this.doMssSwitchForHisi(HwMSSHandler.this.mCellPhoneWIFIOperation, true);
                            return;
                        default:
                            switch (i) {
                                case 100:
                                case 101:
                                    HwMSSHandler.this.handleRadioMessage(msg.arg1, msg.arg2);
                                    return;
                                default:
                                    return;
                            }
                    }
                } else if (HwMSSHandler.this.mThermalLevel >= 1 && HwMSSHandler.this.mThermalLevel <= 5) {
                    int unused4 = HwMSSHandler.this.mTempSISOCount = HwMSSHandler.this.mTempSISOCount + 1;
                    if (HwMSSHandler.this.mTempSISOCount == 3) {
                        HwMSSHandler.this.tempChangeWiFiState(HwMSSHandler.WIFI_THERMAL_MIMO_TO_SISO);
                        HwMSSHandler.this.clearTempCount(false);
                    }
                    if (!HwMSSHandler.this.mHandler.hasMessages(HwMSSHandler.WIFI_THERMAL_MIMO_TO_SISO)) {
                        sendEmptyMessageDelayed(HwMSSHandler.WIFI_THERMAL_MIMO_TO_SISO, 3000);
                    }
                }
            }
        };
    }

    private boolean mssPreArbitrate(MSS_TRIG_ARBITRATE_TYPE type, int direction) {
        int tputSwitchMimoTresh = getTputSwitchMimoTresh();
        switch (type) {
            case RSSI_TRIG:
                if (1 == direction && ((tputSwitchMimoTresh != 0 && this.mCurrentTput >= tputSwitchMimoTresh) || this.mssIsHighTput)) {
                    Log.d(TAG, "throughput is over the threshold, do not switch");
                    return false;
                }
            case TPUT_TRIG:
                if (1 == direction && !isRssiFourCells()) {
                    Log.d(TAG, "rssi is below four sells, do not switch");
                    return false;
                } else if (2 == direction && this.mThermalLevel >= 1 && this.mThermalLevel <= 5) {
                    Log.d(TAG, "temperature is over the threshold, do not switch");
                    return false;
                }
                break;
            case TEMP_TRIG:
                if (1 == direction && !isRssiFourCells()) {
                    Log.d(TAG, "rssi is below four cells, do not switch");
                    return false;
                }
        }
        return true;
    }

    private boolean mssSwitchSupportCheck() {
        if (!this.mssArbi.isMSSSwitchBandSupport()) {
            return false;
        }
        return true;
    }

    private int getTputSwitchMimoTresh() {
        int freq = this.mWifiInfo.getFrequency();
        if (freq >= MIN_FREQ_24G && freq <= MAX_FREQ_24G) {
            return 30;
        }
        if (freq > MAX_FREQ_24G) {
            return MSS_TPUT_SWITCH_MIMO_TRESH_5G;
        }
        return 0;
    }

    private int getTputSwitchSisoTresh() {
        int freq = this.mWifiInfo.getFrequency();
        if ((freq < MIN_FREQ_24G || freq > MAX_FREQ_24G) && freq <= MAX_FREQ_24G) {
            return 0;
        }
        return 10;
    }

    public void mssSwitchCheck(int rssi) {
        int direction;
        if (mssSwitchSupportCheck() && allowMSSSwitch()) {
            this.mCurrentRssi = rssi;
            mRestoreCount++;
            if (mRestoreCount == this.MSS_STATE_RES_CYCLE) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(2, 0));
                mRestoreCount = 0;
            }
            if (isRssiFourCells() && this.mssArbi.getMSSCurrentState() == HwMSSArbitrager.MSSState.MSSMIMO) {
                mRssiSISOCount++;
                mRssiMIMOCount = 0;
            } else if (!isRssiBelowTwoCells() || this.mssArbi.getMSSCurrentState() != HwMSSArbitrager.MSSState.MSSSISO) {
                mRssiSISOCount = 0;
                mRssiMIMOCount = 0;
            } else {
                mRssiMIMOCount++;
                mRssiSISOCount = 0;
            }
            Log.d(TAG, "doMssSwitch rssi:" + rssi + " CurrentChainState:" + this.mssArbi.getMSSCurrentState() + " SISOCount:" + mRssiSISOCount + " MIMOCount:" + mRssiMIMOCount);
            if (mRssiMIMOCount >= 5 && this.mssArbi.getMSSCurrentState() == HwMSSArbitrager.MSSState.MSSSISO) {
                direction = 2;
            } else if (mRssiSISOCount >= 5 && this.mssArbi.getMSSCurrentState() == HwMSSArbitrager.MSSState.MSSMIMO) {
                direction = 1;
                if (!mssPreArbitrate(MSS_TRIG_ARBITRATE_TYPE.RSSI_TRIG, 1)) {
                    return;
                }
            } else {
                return;
            }
            this.mHandler.sendMessage(this.mHandler.obtainMessage(0, direction, MSS_TRIG_ARBITRATE_TYPE.RSSI_TRIG.ordinal()));
        }
    }

    public void mssSwitchCheckTPut(int tput) {
        int direction;
        if (mssSwitchSupportCheck() && allowMSSSwitch()) {
            this.mCurrentTput = tput;
            int tputSwitchToMimoTresh = getTputSwitchMimoTresh();
            int tputSwitchToSisoTresh = getTputSwitchSisoTresh();
            if (tputSwitchToMimoTresh != 0 && tputSwitchToSisoTresh != 0) {
                if (tput >= tputSwitchToMimoTresh) {
                    mTputMIMOCount++;
                    mTputSISOCount = 0;
                } else if (tput <= tputSwitchToSisoTresh) {
                    mTputSISOCount++;
                    mTputMIMOCount = 0;
                } else {
                    mTputSISOCount = 0;
                    mTputMIMOCount = 0;
                }
                Log.d(TAG, "doMssSwitch tput:" + tput + " CurrentChainState:" + this.mssArbi.getMSSCurrentState() + " SISOCount:" + mTputSISOCount + " MIMOCount:" + mTputMIMOCount + " mssIsHighTput:" + this.mssIsHighTput);
                if (mTputMIMOCount >= 3) {
                    direction = 2;
                    this.mssIsHighTput = true;
                    if (this.mssArbi.getMSSCurrentState() == HwMSSArbitrager.MSSState.MSSMIMO || !mssPreArbitrate(MSS_TRIG_ARBITRATE_TYPE.TPUT_TRIG, 2)) {
                        mTputSISOCount = 0;
                        mTputMIMOCount = 0;
                        return;
                    }
                } else if (mTputSISOCount >= 3) {
                    direction = 1;
                    this.mssIsHighTput = false;
                    if (this.mssArbi.getMSSCurrentState() == HwMSSArbitrager.MSSState.MSSSISO || !mssPreArbitrate(MSS_TRIG_ARBITRATE_TYPE.TPUT_TRIG, 1)) {
                        mTputSISOCount = 0;
                        mTputMIMOCount = 0;
                        return;
                    }
                } else {
                    return;
                }
                this.mHandler.sendMessage(this.mHandler.obtainMessage(0, direction, MSS_TRIG_ARBITRATE_TYPE.TPUT_TRIG.ordinal()));
            }
        }
    }

    /* access modifiers changed from: private */
    public void tempChangeWiFiState(int action) {
        if (WIFI_THERMAL_MIMO_TO_SISO == action && this.mssArbi.isWiFiConnected()) {
            if (!mssSwitchSupportCheck() || this.mssArbi.getMSSCurrentState() == HwMSSArbitrager.MSSState.MSSSISO || !allowMSSSwitch()) {
                Log.d(TAG, "NOT MIMO or NOT support this switch return");
            } else if (!mssPreArbitrate(MSS_TRIG_ARBITRATE_TYPE.TEMP_TRIG, 1)) {
                Log.d(TAG, "rssi do not allow this switch");
            } else {
                Log.d(TAG, "temperature is over the threshold, begin to switch,direction is MIMO --> SISO");
                this.mHandler.sendMessage(this.mHandler.obtainMessage(0, 1, MSS_TRIG_ARBITRATE_TYPE.TEMP_TRIG.ordinal()));
            }
        }
    }

    /* access modifiers changed from: private */
    public void clearTempCount(boolean isNeedRemoveMsg) {
        this.mTempSISOCount = 0;
        if (isNeedRemoveMsg) {
            this.mHandler.removeMessages(WIFI_THERMAL_MIMO_TO_SISO);
        }
    }

    /* access modifiers changed from: private */
    public void restoreCurrentChainState() {
        Log.d(TAG, "Start restoreCurrentChainState");
        int mssState = this.mWifiNative.getWifiAnt("wlan0", 0);
        if (mssState == -1 || changeAntReturnValueToMssState(mssState) == HwMSSArbitrager.MSSState.MSSUNKNOWN) {
            Log.d(TAG, "restore current Chain State wrong!");
            return;
        }
        this.mssArbi.setMSSCurrentState(changeAntReturnValueToMssState(mssState));
        clearMssCount();
        if (changeAntReturnValueToMssState(mssState) == HwMSSArbitrager.MSSState.MSSMIMO) {
            this.mssArbi.setSisoFixFlag(false);
        }
    }

    private void mssCheckInit(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.p2p.STATE_CHANGED");
        filter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        filter.addAction("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE");
        filter.addAction("android.net.wifi.p2p.PEERS_CHANGED");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction(THERMAL_TO_WIFI);
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        this.mWifiStateReceiver = new WifiStateReceiver();
        context.registerReceiver(this.mWifiStateReceiver, filter);
        registerProcessObserver();
    }

    /* access modifiers changed from: private */
    public void clearMssCount() {
        mRssiSISOCount = 0;
        mRssiMIMOCount = 0;
        mTputSISOCount = 0;
        mTputMIMOCount = 0;
    }

    private HwMSSArbitrager.MSSState changeAntReturnValueToMssState(int check) {
        if (check == 1) {
            return HwMSSArbitrager.MSSState.MSSMIMO;
        }
        if (check == 0) {
            return HwMSSArbitrager.MSSState.MSSSISO;
        }
        return HwMSSArbitrager.MSSState.MSSUNKNOWN;
    }

    public boolean doMssSwitch(int direction) {
        if (this.m2GHT40Enabled && direction == 1) {
            Log.d(TAG, "HT40 Enabled, not allowed to swtich");
            return false;
        } else if (!this.mssArbi.isMSSAllowed(direction, this.mWifiInfo.getFrequency(), HwMSSArbitrager.MSS_TRIG_TYPE.COMMON_TRIG)) {
            Log.d(TAG, "mss is not allowed!");
            clearMssCount();
            return false;
        } else if (!this.mWifiNative.hwABSSoftHandover(direction)) {
            Log.d(TAG, "hwABSSoftHandover fail,direction:" + direction);
            clearMssCount();
            return false;
        } else {
            if (direction == 2) {
                this.mssArbi.setMSSCurrentState(HwMSSArbitrager.MSSState.MSSMIMO);
            } else {
                this.mssArbi.setMSSCurrentState(HwMSSArbitrager.MSSState.MSSSISO);
            }
            this.mMSSDirection = direction;
            clearMssCount();
            return true;
        }
    }

    public int mssResultCheck() {
        WifiNative wifiNative = this.mWifiNative;
        Log.d(TAG, "mssResultCheck");
        if (1 == SystemProperties.getInt("runtime.hwmss.errtest", 0)) {
            Log.d(TAG, "mssResultCheck:error for test");
            return 1;
        }
        this.mIsDisconnectHappened = 0;
        if (!this.mWiFiApMode) {
            fetchPktcntNative();
            if (!new HwArpUtils(this.mContext).isGateWayReachable(3, 1000)) {
                Log.d(TAG, "mssGatewayVerifier fail");
                fetchPktcntNative();
                return 2;
            }
        }
        int mssState = this.mWifiNative.getWifiAnt("wlan0", 0);
        if (mssState == -1 || changeAntReturnValueToMssState(mssState) != this.mssArbi.getMSSCurrentState()) {
            Log.d(TAG, "mssResultCheck:Current Chain State error");
            return 1;
        } else if (this.mWiFiApMode || this.mIsDisconnectHappened != 1) {
            return 0;
        } else {
            Log.d(TAG, "mIsDisconnectHappened fail");
            this.mIsDisconnectHappened = 0;
            return 3;
        }
    }

    public void mssRecoverWifiLink() {
        this.mWifiNative.hwABSSoftHandover(2);
        this.mssArbi.setMSSCurrentState(HwMSSArbitrager.MSSState.MSSMIMO);
        clearMssCount();
        this.mWifiNative.reassociate(this.mWifiNative.getClientInterfaceName());
    }

    public void onMSSSwitchRequest(int direction) {
        if (allowMSSSwitch() && direction == 2 && HwMSSArbitrager.MSSState.MSSSISO == this.mssArbi.getMSSCurrentState() && !this.mssArbi.getSisoFixFlag()) {
            Log.d(TAG, "onMSSSwitchRequest MSS_SISO_TO_MIMO");
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, 0));
        }
    }

    private void fetchPktcntNative() {
        if (this.mWifiNative != null) {
            WifiNative.TxPacketCounters counters = this.mWifiNative.getTxPacketCounters(this.mWifiNative.getClientInterfaceName());
            if (counters != null) {
                int tx_Good = counters.txSucceeded;
                int tx_bad = counters.txFailed;
                this.mTXGood = tx_Good - this.mTxgood_Last;
                this.mTxgood_Last = tx_Good;
                this.mTXbad = tx_bad - this.mTxbad_Last;
                this.mTxbad_Last = tx_bad;
            }
        }
    }

    private ArrayList getParamList() {
        this.list = new ArrayList();
        this.list.add(Integer.valueOf(this.mTXGood));
        this.list.add(Integer.valueOf(this.mTXbad));
        this.list.add(Integer.valueOf(this.mThermalLevel));
        this.list.add(Integer.valueOf(this.mThermalTemp));
        this.list.add(Integer.valueOf(this.mThermalScene));
        this.list.add(Integer.valueOf(this.mTriggerReason));
        return this.list;
    }

    /* access modifiers changed from: private */
    public int setWifiAntImpl(String iface, int mode, int operation) {
        int reason;
        Log.d(TAG, "setWifiAnt, interface, mode ,operation:" + iface + "," + mode + "," + operation);
        if (!mssSwitchSupportCheck()) {
            Log.d(TAG, "setWifiAnt, mssSwitch not support");
            return -1;
        } else if (iface == null || iface.isEmpty()) {
            Log.d(TAG, "setWifiAnt, parameter iface error");
            return -1;
        } else if (operation > 4) {
            Log.d(TAG, "setWifiAnt, parameter operation error");
            return -1;
        } else if (operation == 2 && this.mssArbi.getMSSCurrentState() == HwMSSArbitrager.MSSState.MSSMIMO) {
            Log.d(TAG, "setWifiAnt, MIMO state, no need to change to MIMO");
            return -1;
        } else if (!this.mssArbi.isMSSAllowed(operation, this.mWifiInfo.getFrequency(), HwMSSArbitrager.MSS_TRIG_TYPE.CLONE_TRIG)) {
            Log.d(TAG, "mss is not allowed!");
            clearMssCount();
            return -1;
        } else if (this.mWifiNative.setWifiAnt(iface, mode, operation) == -1) {
            Log.d(TAG, "setWifiAnt fail, operation:" + operation);
            clearMssCount();
            return -1;
        } else {
            if (operation == 2) {
                this.mssArbi.setMSSCurrentState(HwMSSArbitrager.MSSState.MSSMIMO);
            } else {
                this.mssArbi.setMSSCurrentState(HwMSSArbitrager.MSSState.MSSSISO);
            }
            this.mMSSDirection = operation;
            clearMssCount();
            if (mssResultCheck() != 0) {
                mssRecoverWifiLink();
                this.wcsm.updateMSSCHR(this.mMSSDirection, this.mssArbi.getABSCurrentState().ordinal(), 0, getParamList());
                Log.d(TAG, "setWifiAnt fail, operation:" + 0);
                reason = -1;
            } else {
                this.wcsm.updateMSSCHR(this.mMSSDirection, this.mssArbi.getABSCurrentState().ordinal(), 0, getParamList());
                this.mssArbi.setSisoFixFlag(true);
                reason = 0;
            }
            Log.d(TAG, "setWifiAnt success");
            return reason;
        }
    }

    public void setWifiAnt(String iface, int mode, int operation) {
        Log.d(TAG, "before setWifiAnt sendmessage");
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 3;
            this.mCellPhoneWIFIIface = iface;
            this.mCellPhoneWIFIMode = mode;
            this.mCellPhoneWIFIOperation = operation;
            this.mHandler.sendMessage(msg);
            Log.d(TAG, "setWifiAnt sendmessage");
        }
    }

    public void notifyWifiDisconnected() {
        Log.d(TAG, "WLAN+ MSSWifiForceToMIMO success");
        setWifiDisconnectedMSSState();
    }

    /* access modifiers changed from: private */
    public void setWifiDisconnectedMSSState() {
        this.mIsDisconnectHappened = 1;
        this.mssArbi.setSisoFixFlag(false);
        if (this.mssArbi.getMSSCurrentState() == HwMSSArbitrager.MSSState.MSSSISO && !this.mssArbi.isP2PConnected() && !this.mWiFiApMode) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, 0));
        }
    }

    private boolean isHuaweiAp() {
        if (this.mWifiInfo.getSSID() == null || !this.mWifiInfo.getSSID().equals("\"Huawei-Employee\"")) {
            return false;
        }
        return true;
    }

    private boolean isMobileAP() {
        if (this.mContext != null) {
            return HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.mContext);
        }
        return false;
    }

    private boolean allowMSSSwitch() {
        if (SystemProperties.getInt("ro.config.hw_wifi_btc_mss_en", 0) == 1) {
            return false;
        }
        return isHuaweiAp() || isMobileAP() || this.mssArbi.matchAllowMSSApkList();
    }

    public void onHT40Request() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(99, 0));
    }

    public void onPackageChanged(String pkgName, boolean add) {
        if (!HwMSSUtils.PERFORMANCEAPP.equals(pkgName)) {
            return;
        }
        if (add) {
            this.mHasPerformanceApp = true;
            return;
        }
        this.mHasPerformanceApp = false;
        if (HwMSSUtils.isPerformanceProduct()) {
            handleRadioAction(1, 0);
        }
    }

    public void enable2GHT40Band() {
        boolean isSupportHT40 = this.mssArbi.isSupportHT40();
        Log.d(TAG, "enable2GHT40Band enter");
        if (!this.mWifiEnabled || !isSupportHT40 || this.m2GHT40Enabled) {
            Log.d(TAG, "ht40:" + this.m2GHT40Enabled + ",state:" + this.mWifiEnabled + ",support:" + isSupportHT40);
            return;
        }
        this.m2GHT40Enabled = true;
        this.mWifiNative.gameKOGAdjustSpeed(0, 99);
        if (this.mWifiConnected) {
            Log.d(TAG, "should reassociate after enalbe ht40 for 2.4G");
            this.mWifiNative.reassociate(this.mWifiNative.getClientInterfaceName());
        }
    }

    /* access modifiers changed from: private */
    public void handleMssResultForBrcm(int reason) {
        if (reason != 0) {
            if (this.blackMgr != null && isHuaweiAp()) {
                this.blackMgr.addToBlacklist(this.mWifiInfo.getSSID(), this.mWifiInfo.getBSSID(), reason);
            }
            mssRecoverWifiLink();
            this.wcsm.updateMSSCHR(this.mMSSDirection, this.mssArbi.getABSCurrentState().ordinal(), reason, getParamList());
            return;
        }
        this.wcsm.updateMSSCHR(this.mMSSDirection, this.mssArbi.getABSCurrentState().ordinal(), 0, getParamList());
    }

    /* access modifiers changed from: private */
    public boolean doMssSwitchForHisi(int direction, boolean force) {
        if (this.mHisiMssStateMachine == null) {
            HwMSSUtils.loge(TAG, "mHisiMssStateMachine is null");
            return false;
        } else if (force) {
            this.mHisiMssStateMachine.doMssSwitch(direction);
            this.mMSSDirection = direction;
            clearMssCount();
            return true;
        } else if (!this.mssArbi.isMSSAllowed(direction, this.mWifiInfo.getFrequency(), HwMSSArbitrager.MSS_TRIG_TYPE.COMMON_TRIG)) {
            Log.d(TAG, "mss is not allowed!");
            clearMssCount();
            return false;
        } else {
            this.mHisiMssStateMachine.doMssSwitch(direction);
            this.mMSSDirection = direction;
            clearMssCount();
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void restoreCurrentChainStateForHisi() {
        HwMSSUtils.logd(TAG, "Start restoreCurrentChainState");
        if (this.mHisiMssStateMachine != null) {
            this.mHisiMssStateMachine.sendMessage(5);
        }
    }

    public void callbackSyncMssState(HwMSSArbitrager.MSSState state) {
        this.mssArbi.setMSSCurrentState(state);
        if (state == HwMSSArbitrager.MSSState.MSSMIMO) {
            this.mssArbi.setSisoFixFlag(false);
        }
        clearMssCount();
    }

    public void callbackReportCHR(NativeMssResult mssstru) {
        if (mssstru != null && mssstru.vapNum > 0 && this.wcsm != null) {
            if (mssstru.mssResult == 1) {
                HwMSSUtils.logd(TAG, "report chr: mss succ");
                this.wcsm.updateMSSCHR(this.mMSSDirection, this.mssArbi.getABSCurrentState().ordinal(), 0, getParamList());
                return;
            }
            HwMSSUtils.logd(TAG, "report chr: mss fail, mode:" + mssstru.mssMode);
            this.wcsm.updateMSSCHR(this.mMSSDirection, this.mssArbi.getABSCurrentState().ordinal(), mssstru.mssMode, getParamList());
        }
    }

    public void onMssDrvEvent(NativeMssResult mssstru) {
        if (this.mHisiMssStateMachine != null && mssstru != null) {
            this.mHisiMssStateMachine.onMssDrvEvent(mssstru);
        }
    }

    /* access modifiers changed from: private */
    public void handleSimStateChanged(Intent intent) {
        if (intent != null) {
            String stateExtra = intent.getStringExtra("ss");
            HwMSSUtils.logd(TAG, "simState:" + stateExtra);
            if (stateExtra != null && matchPerformanceScene()) {
                if (!stateExtra.equals("ABSENT")) {
                    handleRadioAction(1, 1);
                } else if (HwMSSUtils.PERFORMANCEAPP.equals(getForegroundAppName())) {
                    handleRadioAction(0, 2);
                }
            }
        }
    }

    private void registerProcessObserver() {
        this.mHwProcessObserver = new HwProcessObserver();
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mHwProcessObserver);
        } catch (RemoteException e) {
            HwMSSUtils.loge(TAG, "register process observer failed," + e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public String getAppNameUid(int uid) {
        if (this.mActivityManager == null) {
            this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
            if (this.mActivityManager == null) {
                return null;
            }
        }
        List<ActivityManager.RunningAppProcessInfo> appProcessList = this.mActivityManager.getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.uid == uid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x003c, code lost:
        return null;
     */
    private synchronized String getForegroundAppName() {
        if (this.mActivityManager == null) {
            this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
            if (this.mActivityManager == null) {
                return null;
            }
        }
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = this.mActivityManager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            if (!runningTaskInfos.isEmpty()) {
                ActivityManager.RunningTaskInfo runningTask = runningTaskInfos.get(0);
                if (runningTask == null) {
                    return null;
                }
                return runningTask.topActivity.getPackageName();
            }
        }
    }

    private void setRadioPower(boolean on) {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            tm.setRadioPower(on);
            this.mRadioActive = on;
            HwMSSUtils.loge(TAG, "setRadioPower:" + on);
        }
    }

    private boolean isCardPresent() {
        boolean z = false;
        if (!HwTelephonyManager.isMultiSimEnabled()) {
            return HwTelephonyManager.getDefault().isCardPresent(0);
        }
        if (HwTelephonyManager.getDefault().isCardPresent(0) || HwTelephonyManager.getDefault().isCardPresent(1)) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void handleRadioMessage(int action, int type) {
        if ((action != 1 || !this.mRadioActive) && (action != 0 || this.mRadioActive)) {
            if (action != 1) {
                boolean cardState = isCardPresent();
                boolean airplaneState = getPersistedAirplaneModeOn();
                if (cardState || airplaneState) {
                    HwMSSUtils.loge(TAG, "skp deative radio power, simcard:" + cardState + ", airplane:" + airplaneState);
                } else {
                    setRadioPower(false);
                }
            } else if (type == 1 && !isCardPresent()) {
                HwMSSUtils.loge(TAG, "skip active radio power with no simcard in PLUGIN event");
                return;
            } else if (!getPersistedAirplaneModeOn()) {
                setRadioPower(true);
            } else {
                HwMSSUtils.loge(TAG, "skip active radio power in airplane mode");
            }
            return;
        }
        HwMSSUtils.loge(TAG, "action:" + action + " not match current state:" + this.mRadioActive);
    }

    /* access modifiers changed from: private */
    public void handleRadioAction(int action, int type) {
        if (this.mHandler.hasMessages(100)) {
            this.mHandler.removeMessages(100);
        }
        if (action == 1 && type == 1) {
            if (this.mHandler.hasMessages(101)) {
                this.mHandler.removeMessages(101);
            }
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101, action, type), 2000);
            return;
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100, action, type), 2000);
    }

    /* access modifiers changed from: private */
    public void handleWifiConnected() {
        if (matchPerformanceScene() && HwMSSUtils.PERFORMANCEAPP.equals(getForegroundAppName())) {
            HwMSSUtils.loge(TAG, "wifi connected with fground app: com.example.wptp.testapp");
            handleRadioAction(0, 0);
        }
    }

    private boolean getPersistedAirplaneModeOn() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    /* access modifiers changed from: private */
    public boolean matchPerformanceScene() {
        if (this.mHasPerformanceApp && HwMSSUtils.isPerformanceProduct()) {
            return true;
        }
        HwMSSUtils.logv(TAG, "PerformanceApp:" + this.mHasPerformanceApp);
        return false;
    }

    /* access modifiers changed from: private */
    public void handleAirplaneModeChanged() {
        if (matchPerformanceScene() && !getPersistedAirplaneModeOn()) {
            this.mRadioActive = true;
            if (HwMSSUtils.PERFORMANCEAPP.equals(getForegroundAppName())) {
                HwMSSUtils.loge(TAG, "airplane off with fground app: com.example.wptp.testapp");
                handleRadioAction(0, 4);
            }
        }
    }
}
