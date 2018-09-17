package com.android.server.wifi.MSS;

import android.annotation.SuppressLint;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import com.android.server.wifi.HwArpVerifier;
import com.android.server.wifi.HwMSSHandlerManager;
import com.android.server.wifi.HwWifiCHRStateManager;
import com.android.server.wifi.HwWifiCHRStateManagerImpl;
import com.android.server.wifi.MSS.HwMSSArbitrager.IHwMSSObserver;
import com.android.server.wifi.MSS.HwMSSArbitrager.MSSState;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiNative.TxPacketCounters;
import com.android.server.wifi.wifipro.PortalAutoFillManager;
import java.util.ArrayList;

@SuppressLint({"HandlerLeak"})
public class HwMSSHandler implements HwMSSHandlerManager, IHwMSSObserver {
    private static final /* synthetic */ int[] -com-android-server-wifi-MSS-HwMSSHandler$MSS_TRIG_ARBITRATE_TYPESwitchesValues = null;
    private static final int KOG_MODE_HT40 = 99;
    private static final int MAX_FREQ_24G = 2484;
    private static final int MIN_FREQ_24G = 2412;
    private static final int MSG_HT_40 = 99;
    private static final int MSS_CHECK_DISCONN_ERR = 3;
    private static final int MSS_CHECK_PING_ERR = 2;
    private static final int MSS_CHECK_STATE_ERR = 1;
    private static final int MSS_FORCE_TO_MIMO = 1;
    private static final int MSS_MIMO_CHAIN_NUM = 3;
    private static final int MSS_RSSI_SWITCH_COUNT = 5;
    private static final int MSS_RSSI_SWITCH_MIMO_TRESH_2G = -76;
    private static final int MSS_RSSI_SWITCH_MIMO_TRESH_5G = -76;
    private static final int MSS_RSSI_SWITCH_SISO_TRESH_2G = -65;
    private static final int MSS_RSSI_SWITCH_SISO_TRESH_5G = -65;
    private static final int MSS_SISO_CHAIN_NUM = 1;
    private static final int MSS_START = 0;
    private static final int MSS_STATE_RES_CYCLE = 20;
    private static final int MSS_TEMP_SWITCH_COUNT = 1;
    private static final int MSS_TEMP_SWITCH_SISO_TRESH = 100;
    private static final int MSS_TPUT_SWITCH_MIMO_COUNT = 3;
    private static final int MSS_TPUT_SWITCH_MIMO_TRESH_2G = 30;
    private static final int MSS_TPUT_SWITCH_MIMO_TRESH_5G = 90;
    private static final int MSS_TPUT_SWITCH_SISO_COUNT = 3;
    private static final int MSS_TPUT_SWITCH_SISO_TRESH_2G = 10;
    private static final int MSS_TPUT_SWITCH_SISO_TRESH_5G = 10;
    private static final int RESTORE_CURRENT_MSS_STATE = 2;
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
    private HwMSSBlackListManager blackMgr = null;
    public ArrayList list;
    private boolean m2GHT40Enabled = false;
    private Context mContext = null;
    private int mCurrentRssi = 0;
    private int mCurrentTemp = 0;
    private int mCurrentTput = 0;
    private Handler mHandler = null;
    private int mIsDisconnectHappened = 0;
    private int mIsSuppCompleted = 0;
    private int mMSSDirection = 0;
    private int mTXGood = 0;
    private int mTXbad = 0;
    private int mTempSISOCount = 0;
    private int mThermalLevel = -1;
    private int mThermalScene = -1;
    private int mThermalTemp = -1;
    private int mTriggerReason;
    private int mTxbad_Last = 0;
    private int mTxgood_Last = 0;
    private boolean mWifiConnected = false;
    private boolean mWifiEnabled = false;
    private WifiInfo mWifiInfo = null;
    private WifiNative mWifiNative = null;
    private WifiStateReceiver mWifiStateReceiver = null;
    private HwMSSArbitrager mssArbi = null;
    private boolean mssIsHighTput = false;
    private HwWifiCHRStateManager wcsm = null;

    private enum MSS_TRIG_ARBITRATE_TYPE {
        RSSI_TRIG,
        TPUT_TRIG,
        TEMP_TRIG
    }

    private class WifiStateReceiver extends BroadcastReceiver {
        /* synthetic */ WifiStateReceiver(HwMSSHandler this$0, WifiStateReceiver -this1) {
            this();
        }

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
                        HwMSSHandler.this.mWifiConnected = false;
                        HwMSSHandler.this.mIsDisconnectHappened = 1;
                    }
                    if (state == SupplicantState.COMPLETED) {
                        HwMSSHandler.this.mWifiConnected = true;
                        HwMSSHandler.this.mHandler.sendMessage(HwMSSHandler.this.mHandler.obtainMessage(2, Integer.valueOf(0)));
                    }
                    HwMSSHandler.this.clearMssCount();
                }
                if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    Log.d(HwMSSHandler.TAG, "WifiStateReceiver:" + action);
                    HwMSSHandler.this.mHandler.sendMessage(HwMSSHandler.this.mHandler.obtainMessage(2, Integer.valueOf(0)));
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    if (3 == intent.getIntExtra("wifi_state", 4)) {
                        HwMSSHandler.this.mWifiEnabled = true;
                        HwMSSHandler.this.m2GHT40Enabled = false;
                        if (HwMSSHandler.this.mssArbi.matchHT40List()) {
                            HwMSSHandler.this.mHandler.sendMessage(HwMSSHandler.this.mHandler.obtainMessage(99, Integer.valueOf(1)));
                        }
                    } else {
                        HwMSSHandler.this.mWifiEnabled = false;
                    }
                }
                if (HwMSSHandler.THERMAL_TO_WIFI.equals(action)) {
                    HwMSSHandler.this.mThermalLevel = intent.getIntExtra("level", -1);
                    HwMSSHandler.this.mThermalTemp = intent.getIntExtra("temp", -1);
                    HwMSSHandler.this.mThermalScene = intent.getIntExtra("scene", -1);
                    Log.d(HwMSSHandler.TAG, "receive thermal brocast temperature is:" + HwMSSHandler.this.mThermalTemp + " ,level is:" + HwMSSHandler.this.mThermalLevel + " ,Scene is:" + HwMSSHandler.this.mThermalScene);
                    if (HwMSSHandler.this.mThermalLevel == 0) {
                        HwMSSHandler.this.clearTempCount(true);
                    } else if (HwMSSHandler.this.mThermalLevel >= 1 && HwMSSHandler.this.mThermalLevel <= 5 && (HwMSSHandler.this.mHandler.hasMessages(1001) ^ 1) != 0) {
                        HwMSSHandler.this.mHandler.sendMessage(HwMSSHandler.this.mHandler.obtainMessage(1001, Integer.valueOf(0)));
                    }
                }
            }
        }
    }

    private static /* synthetic */ int[] -getcom-android-server-wifi-MSS-HwMSSHandler$MSS_TRIG_ARBITRATE_TYPESwitchesValues() {
        if (-com-android-server-wifi-MSS-HwMSSHandler$MSS_TRIG_ARBITRATE_TYPESwitchesValues != null) {
            return -com-android-server-wifi-MSS-HwMSSHandler$MSS_TRIG_ARBITRATE_TYPESwitchesValues;
        }
        int[] iArr = new int[MSS_TRIG_ARBITRATE_TYPE.values().length];
        try {
            iArr[MSS_TRIG_ARBITRATE_TYPE.RSSI_TRIG.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[MSS_TRIG_ARBITRATE_TYPE.TEMP_TRIG.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[MSS_TRIG_ARBITRATE_TYPE.TPUT_TRIG.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -com-android-server-wifi-MSS-HwMSSHandler$MSS_TRIG_ARBITRATE_TYPESwitchesValues = iArr;
        return iArr;
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
        if (freq < 2412 || freq > MAX_FREQ_24G) {
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
        if (freq < 2412 || freq > MAX_FREQ_24G) {
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
        this.blackMgr = HwMSSBlackListManager.getInstance(this.mContext);
        this.wcsm = HwWifiCHRStateManagerImpl.getDefault();
        HwMSSHandlerInit();
        mssCheckInit(this.mContext);
        this.mssArbi.registerMSSObserver(this);
    }

    private void HwMSSHandlerInit() {
        HandlerThread handlerThread = new HandlerThread("mss_handler_thread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                String ssid = "\"Huawei-Employee\"";
                switch (msg.what) {
                    case 0:
                        if (HwMSSHandler.this.doMssSwitch(msg.arg1)) {
                            HwMSSHandler.this.mTriggerReason = msg.arg2;
                            int reason = HwMSSHandler.this.mssResultCheck();
                            if (reason == 0) {
                                HwMSSHandler.this.wcsm.updateMSSCHR(HwMSSHandler.this.mMSSDirection, HwMSSHandler.this.mssArbi.getABSCurrentState().ordinal(), 0, HwMSSHandler.this.getParamList());
                                break;
                            }
                            if (!(HwMSSHandler.this.mWifiInfo.getSSID() == null || HwMSSHandler.this.mWifiInfo.getSSID().equals(ssid))) {
                                HwMSSHandler.this.blackMgr.addToBlacklist(HwMSSHandler.this.mWifiInfo.getSSID(), HwMSSHandler.this.mWifiInfo.getBSSID(), reason);
                            }
                            HwMSSHandler.this.mssRecoverWifiLink();
                            HwMSSHandler.this.wcsm.updateMSSCHR(HwMSSHandler.this.mMSSDirection, HwMSSHandler.this.mssArbi.getABSCurrentState().ordinal(), reason, HwMSSHandler.this.getParamList());
                            break;
                        }
                        break;
                    case 1:
                        HwMSSHandler.this.mWifiNative.hwABSSoftHandover(2);
                        HwMSSHandler.this.mssArbi.setMSSCurrentState(MSSState.MSSMIMO);
                        HwMSSHandler.this.clearMssCount();
                        break;
                    case 2:
                        HwMSSHandler.this.restoreCurrentChainState();
                        break;
                    case 99:
                        HwMSSHandler.this.enable2GHT40Band();
                        break;
                    case 1001:
                        if (HwMSSHandler.this.mThermalLevel >= 1 && HwMSSHandler.this.mThermalLevel <= 5) {
                            HwMSSHandler hwMSSHandler = HwMSSHandler.this;
                            hwMSSHandler.mTempSISOCount = hwMSSHandler.mTempSISOCount + 1;
                            if (HwMSSHandler.this.mTempSISOCount == 3) {
                                HwMSSHandler.this.tempChangeWiFiState(1001);
                                HwMSSHandler.this.clearTempCount(false);
                            }
                            if (!HwMSSHandler.this.mHandler.hasMessages(1001)) {
                                sendEmptyMessageDelayed(1001, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                                break;
                            }
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private boolean mssPreArbitrate(MSS_TRIG_ARBITRATE_TYPE type, int direction) {
        int tputSwitchMimoTresh = getTputSwitchMimoTresh();
        switch (-getcom-android-server-wifi-MSS-HwMSSHandler$MSS_TRIG_ARBITRATE_TYPESwitchesValues()[type.ordinal()]) {
            case 1:
                if (1 == direction && ((tputSwitchMimoTresh != 0 && this.mCurrentTput >= tputSwitchMimoTresh) || this.mssIsHighTput)) {
                    Log.d(TAG, "throughput is over the threshold, do not switch");
                    return false;
                }
            case 2:
                if (1 == direction && isRssiBelowTwoCells()) {
                    Log.d(TAG, "rssi is below two sells, do not switch");
                    return false;
                }
            case 3:
                if (1 == direction && isRssiBelowTwoCells()) {
                    Log.d(TAG, "rssi is below two sells, do not switch");
                    return false;
                } else if (2 == direction && this.mThermalLevel >= 1 && this.mThermalLevel <= 5) {
                    Log.d(TAG, "temperature is over the threshold, do not switch");
                    return false;
                }
                break;
        }
        return true;
    }

    private boolean mssSwitchSupportCheck() {
        if (this.mssArbi.isMSSSwitchBandSupport()) {
            return true;
        }
        return false;
    }

    private int getTputSwitchMimoTresh() {
        int freq = this.mWifiInfo.getFrequency();
        if (freq >= 2412 && freq <= MAX_FREQ_24G) {
            return 30;
        }
        if (freq > MAX_FREQ_24G) {
            return 90;
        }
        return 0;
    }

    private int getTputSwitchSisoTresh() {
        int freq = this.mWifiInfo.getFrequency();
        if ((freq < 2412 || freq > MAX_FREQ_24G) && freq <= MAX_FREQ_24G) {
            return 0;
        }
        return 10;
    }

    public void mssSwitchCheck(int rssi) {
        if (mssSwitchSupportCheck() && (allowMSSSwitch() ^ 1) == 0) {
            int direction;
            this.mCurrentRssi = rssi;
            mRestoreCount++;
            if (mRestoreCount == 20) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(2, Integer.valueOf(0)));
                mRestoreCount = 0;
            }
            if (isRssiFourCells() && this.mssArbi.getMSSCurrentState() == MSSState.MSSMIMO) {
                mRssiSISOCount++;
                mRssiMIMOCount = 0;
            } else if (isRssiBelowTwoCells() && this.mssArbi.getMSSCurrentState() == MSSState.MSSSISO) {
                mRssiMIMOCount++;
                mRssiSISOCount = 0;
            } else {
                mRssiSISOCount = 0;
                mRssiMIMOCount = 0;
            }
            Log.d(TAG, "doMssSwitch rssi:" + rssi + " CurrentChainState:" + this.mssArbi.getMSSCurrentState() + " SISOCount:" + mRssiSISOCount + " MIMOCount:" + mRssiMIMOCount);
            if (mRssiMIMOCount >= 5 && this.mssArbi.getMSSCurrentState() == MSSState.MSSSISO) {
                direction = 2;
            } else if (mRssiSISOCount >= 5 && this.mssArbi.getMSSCurrentState() == MSSState.MSSMIMO) {
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
        if (mssSwitchSupportCheck() && (allowMSSSwitch() ^ 1) == 0) {
            this.mCurrentTput = tput;
            int tputSwitchToMimoTresh = getTputSwitchMimoTresh();
            int tputSwitchToSisoTresh = getTputSwitchSisoTresh();
            if (tputSwitchToMimoTresh != 0 && tputSwitchToSisoTresh != 0) {
                int direction;
                if (tput >= tputSwitchToMimoTresh && this.mssArbi.getMSSCurrentState() == MSSState.MSSSISO) {
                    mTputMIMOCount++;
                    mTputSISOCount = 0;
                } else if (tput > tputSwitchToSisoTresh || this.mssArbi.getMSSCurrentState() != MSSState.MSSMIMO) {
                    mTputSISOCount = 0;
                    mTputMIMOCount = 0;
                } else {
                    mTputSISOCount++;
                    mTputMIMOCount = 0;
                }
                Log.d(TAG, "doMssSwitch tput:" + tput + " CurrentChainState:" + this.mssArbi.getMSSCurrentState() + " SISOCount:" + mTputSISOCount + " MIMOCount:" + mTputMIMOCount);
                if (mTputMIMOCount >= 3) {
                    direction = 2;
                    this.mssIsHighTput = true;
                    if (!mssPreArbitrate(MSS_TRIG_ARBITRATE_TYPE.TPUT_TRIG, 2)) {
                        return;
                    }
                } else if (mTputSISOCount >= 3) {
                    direction = 1;
                    this.mssIsHighTput = false;
                    if (!mssPreArbitrate(MSS_TRIG_ARBITRATE_TYPE.TPUT_TRIG, 1)) {
                        return;
                    }
                } else {
                    return;
                }
                this.mHandler.sendMessage(this.mHandler.obtainMessage(0, direction, MSS_TRIG_ARBITRATE_TYPE.TPUT_TRIG.ordinal()));
            }
        }
    }

    private void tempChangeWiFiState(int action) {
        if (1001 == action && this.mssArbi.isWiFiConnected()) {
            if (!mssSwitchSupportCheck() || this.mssArbi.getMSSCurrentState() == MSSState.MSSSISO || (allowMSSSwitch() ^ 1) != 0) {
                Log.d(TAG, "NOT MIMO or NOT support this switch return");
            } else if (mssPreArbitrate(MSS_TRIG_ARBITRATE_TYPE.TEMP_TRIG, 1)) {
                Log.d(TAG, "temperature is over the threshold, begin to switch,direction is MIMO --> SISO");
                this.mHandler.sendMessage(this.mHandler.obtainMessage(0, 1, MSS_TRIG_ARBITRATE_TYPE.TEMP_TRIG.ordinal()));
            } else {
                Log.d(TAG, "rssi do not allow this switch");
            }
        }
    }

    private void clearTempCount(boolean isNeedRemoveMsg) {
        this.mTempSISOCount = 0;
        if (isNeedRemoveMsg) {
            this.mHandler.removeMessages(1001);
        }
    }

    private void restoreCurrentChainState() {
        String mssState = this.mWifiNative.getMssState();
        if (mssState != null && mssState.matches("[0-9]+")) {
            int txchain = Integer.parseInt(mssState.substring(0, 1));
            if (txchain == Integer.parseInt(mssState.substring(1)) && (txchain == 3 || txchain == 1)) {
                this.mssArbi.setMSSCurrentState(chainToState(txchain));
                clearMssCount();
                return;
            }
            Log.d(TAG, "restore current Chain State wrong!");
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
        filter.addAction(THERMAL_TO_WIFI);
        this.mWifiStateReceiver = new WifiStateReceiver(this, null);
        context.registerReceiver(this.mWifiStateReceiver, filter);
    }

    private void clearMssCount() {
        mRssiSISOCount = 0;
        mRssiMIMOCount = 0;
        mTputSISOCount = 0;
        mTputMIMOCount = 0;
    }

    private MSSState chainToState(int chain) {
        if (chain == 3) {
            return MSSState.MSSMIMO;
        }
        if (chain == 1) {
            return MSSState.MSSSISO;
        }
        return MSSState.MSSUNKNOWN;
    }

    public boolean doMssSwitch(int direction) {
        if (this.m2GHT40Enabled && direction == 1) {
            Log.d(TAG, "HT40 Enabled, not allowed to swtich");
            return false;
        } else if (!this.mssArbi.isMSSAllowed(direction, this.mWifiInfo.getFrequency())) {
            Log.d(TAG, "mss is not allowed!");
            clearMssCount();
            return false;
        } else if (this.mWifiNative.hwABSSoftHandover(direction)) {
            if (direction == 2) {
                this.mssArbi.setMSSCurrentState(MSSState.MSSMIMO);
            } else {
                this.mssArbi.setMSSCurrentState(MSSState.MSSSISO);
            }
            this.mMSSDirection = direction;
            clearMssCount();
            return true;
        } else {
            Log.d(TAG, "hwABSSoftHandover fail,direction:" + direction);
            clearMssCount();
            return false;
        }
    }

    public int mssResultCheck() {
        WifiNative wifiNative = this.mWifiNative;
        int txchain = 0;
        int rxchain = 0;
        Log.d(TAG, "mssResultCheck");
        HwArpVerifier arpVerifier = HwArpVerifier.getDefault();
        this.mIsDisconnectHappened = 0;
        String mssState = this.mWifiNative.getMssState();
        if (mssState != null && mssState.matches("[0-9]+")) {
            txchain = Integer.parseInt(mssState.substring(0, 1));
            rxchain = Integer.parseInt(mssState.substring(1));
        }
        if (txchain == rxchain && chainToState(txchain) == this.mssArbi.getMSSCurrentState()) {
            fetchPktcntNative();
            if (!arpVerifier.mssGatewayVerifier()) {
                Log.d(TAG, "mssGatewayVerifier fail");
                fetchPktcntNative();
                return 2;
            } else if (this.mIsDisconnectHappened != 1) {
                return 0;
            } else {
                Log.d(TAG, "mIsDisconnectHappened fail");
                this.mIsDisconnectHappened = 0;
                return 3;
            }
        }
        Log.d(TAG, "mssResultCheck:Current Chain State wrong, txchain:" + txchain + "rxchain:" + rxchain);
        return 1;
    }

    public void mssRecoverWifiLink() {
        this.mWifiNative.hwABSSoftHandover(2);
        this.mssArbi.setMSSCurrentState(MSSState.MSSMIMO);
        clearMssCount();
        this.mWifiNative.reassociate();
    }

    public void onMSSSwitchRequest(int direction) {
        if (allowMSSSwitch() && direction == 2 && MSSState.MSSSISO == this.mssArbi.getMSSCurrentState()) {
            Log.d(TAG, "onMSSSwitchRequest MSS_SISO_TO_MIMO");
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, Integer.valueOf(0)));
        }
    }

    private void fetchPktcntNative() {
        if (this.mWifiNative != null) {
            TxPacketCounters counters = this.mWifiNative.getTxPacketCounters();
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

    public void onHT40Request() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(99, Integer.valueOf(0)));
    }

    public void enable2GHT40Band() {
        boolean isSupportHT40 = this.mssArbi.isSupportHT40();
        Log.d(TAG, "enable2GHT40Band enter");
        if (this.mWifiEnabled && (isSupportHT40 ^ 1) == 0 && !this.m2GHT40Enabled) {
            this.m2GHT40Enabled = true;
            this.mWifiNative.gameKOGAdjustSpeed(0, 99);
            if (this.mWifiConnected) {
                Log.d(TAG, "should reassociate after enalbe ht40 for 2.4G");
                this.mWifiNative.reassociate();
            }
            return;
        }
        Log.d(TAG, "ht40:" + this.m2GHT40Enabled + ",state:" + this.mWifiEnabled + ",support:" + isSupportHT40);
    }

    private boolean isHuaweiAp() {
        String hwSsid = "\"Huawei-Employee\"";
        if (this.mWifiInfo.getSSID() == null || !this.mWifiInfo.getSSID().equals(hwSsid)) {
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
        if (isHuaweiAp() || isMobileAP() || this.mssArbi.matchAllowMSSApkList()) {
            return true;
        }
        return false;
    }
}
