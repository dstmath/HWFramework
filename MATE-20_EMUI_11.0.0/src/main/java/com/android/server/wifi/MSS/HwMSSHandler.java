package com.android.server.wifi.MSS;

import android.annotation.SuppressLint;
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
import android.os.SystemProperties;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.HwArpUtils;
import com.android.server.wifi.HwMSSHandlerManager;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.MSS.HwMSSArbitrager;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.wificond.NativeMssResult;
import java.util.ArrayList;

@SuppressLint({"HandlerLeak"})
public class HwMSSHandler implements HwMSSHandlerManager, HwMSSArbitrager.IHwMSSObserver {
    private static final String HW_SIGNATURE_OR_SYSTEM = "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM";
    private static final int KOG_MODE_HT40 = 99;
    private static final int MAX_FREQ_24G = 2484;
    private static final int MIN_FREQ_24G = 2412;
    private static final int MSG_HT_40 = 99;
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
    private boolean m2GHT40Enabled = false;
    private String mCellPhoneWIFIIface = "wlan0";
    private int mCellPhoneWIFIMode = -1;
    private int mCellPhoneWIFIOperation = -1;
    private Context mContext = null;
    private int mCurrentRssi = 0;
    private int mCurrentTemp = 0;
    private int mCurrentTput = 0;
    private Handler mHandler = null;
    private HisiMSSStateMachine mHisiMssStateMachine = null;
    private boolean mIs1103 = false;
    private boolean mIs1105 = false;
    private int mIsDisconnectHappened = 0;
    private int mIsSuppCompleted = 0;
    private HwMSSBluetoothManager mMSSBluetoothManager = null;
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
    private boolean mWiFiApMode = false;
    private boolean mWifiConnected = false;
    private boolean mWifiEnabled = false;
    private WifiInfo mWifiInfo = null;
    private WifiNative mWifiNative = null;
    private WifiStateReceiver mWifiStateReceiver = null;
    private HwMSSArbitrager mssArbi = null;
    private boolean mssIsHighTput = false;
    private HwWifiCHRService wcsm = null;

    /* access modifiers changed from: private */
    public enum MSS_TRIG_ARBITRATE_TYPE {
        RSSI_TRIG,
        TPUT_TRIG,
        TEMP_TRIG
    }

    static /* synthetic */ int access$1008(HwMSSHandler x0) {
        int i = x0.mTempSISOCount;
        x0.mTempSISOCount = i + 1;
        return i;
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
            return mInstance;
        }
    }

    private boolean isRssiBelowTwoCells() {
        int freq = this.mWifiInfo.getFrequency();
        if (freq < MIN_FREQ_24G || freq > MAX_FREQ_24G) {
            if (freq <= MAX_FREQ_24G || this.mCurrentRssi > -76) {
                return false;
            }
            return true;
        } else if (this.mCurrentRssi <= -76) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isRssiFourCells() {
        int freq = this.mWifiInfo.getFrequency();
        if (freq < MIN_FREQ_24G || freq > MAX_FREQ_24G) {
            if (freq <= MAX_FREQ_24G || this.mCurrentRssi < -65) {
                return false;
            }
            return true;
        } else if (this.mCurrentRssi >= -65) {
            return true;
        } else {
            return false;
        }
    }

    private HwMSSHandler(Context cxt, WifiNative wifinav, WifiInfo wifiinfo) {
        this.mContext = cxt;
        this.mWifiNative = wifinav;
        this.mWifiInfo = wifiinfo;
        this.mssArbi = HwMSSArbitrager.getInstance(this.mContext);
        this.mIs1103 = HwMSSUtils.is1103();
        this.mIs1105 = HwMSSUtils.is1105();
        if (this.mIs1103 || this.mIs1105) {
            this.MSS_STATE_RES_CYCLE = HwQoEUtils.QOE_MSG_CAMERA_ON;
            HwMSSUtils.setAllowSwitch(false);
            this.mHisiMssStateMachine = HisiMSSStateMachine.createHisiMSSStateMachine(this, cxt, wifinav, wifiinfo);
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
            /* class com.android.server.wifi.MSS.HwMSSHandler.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (HwMSSHandler.this.mIs1103 || HwMSSHandler.this.mIs1105) {
                    handleMessageForHisi(msg);
                } else {
                    handleMessageForBrcm(msg);
                }
                super.handleMessage(msg);
            }

            private void handleMessageForBrcm(Message msg) {
                int i = msg.what;
                if (i != 0) {
                    if (i == 1) {
                        HwMSSHandler.this.mssArbi.setSisoFixFlag(false);
                        HwMSSHandler.this.mWifiNative.mHwWifiNativeEx.hwABSSoftHandover(2);
                        HwMSSHandler.this.mssArbi.setMSSCurrentState(HwMSSArbitrager.MSSState.MSSMIMO);
                        HwMSSHandler.this.clearMssCount();
                    } else if (i != 2) {
                        if (i == 3) {
                            HwMSSHandler hwMSSHandler = HwMSSHandler.this;
                            HwHiLog.d(HwMSSHandler.TAG, false, "setWifiAntImpl result is %{public}d", new Object[]{Integer.valueOf(hwMSSHandler.setWifiAntImpl(hwMSSHandler.mCellPhoneWIFIIface, HwMSSHandler.this.mCellPhoneWIFIMode, HwMSSHandler.this.mCellPhoneWIFIOperation))});
                            HwMSSHandler.this.clearMssCount();
                        } else if (i == 99) {
                            HwMSSHandler.this.enable2GHT40Band();
                        } else if (i == 1001 && HwMSSHandler.this.mThermalLevel >= 1 && HwMSSHandler.this.mThermalLevel <= 5) {
                            HwMSSHandler.access$1008(HwMSSHandler.this);
                            if (HwMSSHandler.this.mTempSISOCount == 3) {
                                HwMSSHandler.this.tempChangeWiFiState(1001);
                                HwMSSHandler.this.clearTempCount(false);
                            }
                            if (!HwMSSHandler.this.mHandler.hasMessages(1001)) {
                                sendEmptyMessageDelayed(1001, 3000);
                            }
                        }
                    } else if (!HwMSSHandler.this.mssSwitchSupportCheck()) {
                        HwHiLog.d(HwMSSHandler.TAG, false, "MSS switch is not support", new Object[0]);
                    } else {
                        HwMSSHandler.this.restoreCurrentChainState();
                    }
                } else if (HwMSSHandler.this.doMssSwitch(msg.arg1)) {
                    HwMSSHandler.this.mTriggerReason = msg.arg2;
                    HwMSSHandler.this.handleMssResultForBrcm(HwMSSHandler.this.mssResultCheck());
                }
            }

            private void handleMessageForHisi(Message msg) {
                int i = msg.what;
                if (i != 0) {
                    if (i == 1) {
                        HwMSSHandler.this.mssArbi.setSisoFixFlag(false);
                        HwMSSHandler.this.doMssSwitchForHisi(2, true);
                    } else if (i == 2) {
                        HwMSSHandler.this.restoreCurrentChainStateForHisi();
                    } else if (i == 3) {
                        HwMSSHandler hwMSSHandler = HwMSSHandler.this;
                        hwMSSHandler.doMssSwitchForHisi(hwMSSHandler.mCellPhoneWIFIOperation, true);
                    } else if (i != 99 && i == 1001 && HwMSSHandler.this.mThermalLevel >= 1 && HwMSSHandler.this.mThermalLevel <= 5) {
                        HwMSSHandler.access$1008(HwMSSHandler.this);
                        if (HwMSSHandler.this.mTempSISOCount == 3) {
                            HwMSSHandler.this.tempChangeWiFiState(1001);
                            HwMSSHandler.this.clearTempCount(false);
                        }
                        if (!HwMSSHandler.this.mHandler.hasMessages(1001)) {
                            sendEmptyMessageDelayed(1001, 3000);
                        }
                    }
                } else if (HwMSSHandler.this.doMssSwitchForHisi(msg.arg1, false)) {
                    HwMSSHandler.this.mTriggerReason = msg.arg2;
                }
            }
        };
    }

    private boolean mssPreArbitrate(MSS_TRIG_ARBITRATE_TYPE type, int direction) {
        int i;
        int tputSwitchMimoTresh = getTputSwitchMimoTresh();
        int i2 = AnonymousClass2.$SwitchMap$com$android$server$wifi$MSS$HwMSSHandler$MSS_TRIG_ARBITRATE_TYPE[type.ordinal()];
        if (i2 != 1) {
            if (i2 != 2) {
                if (i2 == 3 && 1 == direction && !isRssiFourCells()) {
                    HwHiLog.d(TAG, false, "rssi is below four cells, do not switch", new Object[0]);
                    return false;
                }
            } else if (1 == direction && !isRssiFourCells()) {
                HwHiLog.d(TAG, false, "rssi is below four sells, do not switch", new Object[0]);
                return false;
            } else if (2 == direction && (i = this.mThermalLevel) >= 1 && i <= 5) {
                HwHiLog.d(TAG, false, "temperature is over the threshold, do not switch", new Object[0]);
                return false;
            }
        } else if (1 == direction && ((tputSwitchMimoTresh != 0 && this.mCurrentTput >= tputSwitchMimoTresh) || this.mssIsHighTput)) {
            HwHiLog.d(TAG, false, "throughput is over the threshold, do not switch", new Object[0]);
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.wifi.MSS.HwMSSHandler$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$wifi$MSS$HwMSSHandler$MSS_TRIG_ARBITRATE_TYPE = new int[MSS_TRIG_ARBITRATE_TYPE.values().length];

        static {
            try {
                $SwitchMap$com$android$server$wifi$MSS$HwMSSHandler$MSS_TRIG_ARBITRATE_TYPE[MSS_TRIG_ARBITRATE_TYPE.RSSI_TRIG.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$wifi$MSS$HwMSSHandler$MSS_TRIG_ARBITRATE_TYPE[MSS_TRIG_ARBITRATE_TYPE.TPUT_TRIG.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$wifi$MSS$HwMSSHandler$MSS_TRIG_ARBITRATE_TYPE[MSS_TRIG_ARBITRATE_TYPE.TEMP_TRIG.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
            HwHiLog.d(TAG, false, "doMssSwitch rssi:%{public}d CurrentChainState:%{public}d SISOCount:%{public}d MIMOCount:%{public}d", new Object[]{Integer.valueOf(rssi), this.mssArbi.getMSSCurrentState(), Integer.valueOf(mRssiSISOCount), Integer.valueOf(mRssiMIMOCount)});
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
                HwHiLog.d(TAG, false, "doMssSwitch tput:%{public}d CurrentChainState:%{public}d SISOCount:%{public}d MIMOCount:%{public}d mssIsHighTput:%{public}s", new Object[]{Integer.valueOf(tput), this.mssArbi.getMSSCurrentState(), Integer.valueOf(mTputSISOCount), Integer.valueOf(mTputMIMOCount), String.valueOf(this.mssIsHighTput)});
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
    /* access modifiers changed from: public */
    private void tempChangeWiFiState(int action) {
        if (1001 == action && this.mssArbi.isWiFiConnected()) {
            if (!mssSwitchSupportCheck() || this.mssArbi.getMSSCurrentState() == HwMSSArbitrager.MSSState.MSSSISO || !allowMSSSwitch()) {
                HwHiLog.d(TAG, false, "NOT MIMO or NOT support this switch return", new Object[0]);
            } else if (!mssPreArbitrate(MSS_TRIG_ARBITRATE_TYPE.TEMP_TRIG, 1)) {
                HwHiLog.d(TAG, false, "rssi do not allow this switch", new Object[0]);
            } else {
                HwHiLog.d(TAG, false, "temperature is over the threshold, begin to switch,direction is MIMO --> SISO", new Object[0]);
                this.mHandler.sendMessage(this.mHandler.obtainMessage(0, 1, MSS_TRIG_ARBITRATE_TYPE.TEMP_TRIG.ordinal()));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearTempCount(boolean isNeedRemoveMsg) {
        this.mTempSISOCount = 0;
        if (isNeedRemoveMsg) {
            this.mHandler.removeMessages(1001);
        }
    }

    /* access modifiers changed from: private */
    public class WifiStateReceiver extends BroadcastReceiver {
        private WifiStateReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                    SupplicantState state = (SupplicantState) intent.getParcelableExtra("newState");
                    if (state != null) {
                        HwHiLog.d(HwMSSHandler.TAG, false, "WifiStateReceiver:%{public}s SupplicantState:%{public}s", new Object[]{action, state});
                    }
                    if (state == SupplicantState.DISCONNECTED) {
                        HwMSSHandler.this.mWifiConnected = false;
                        HwMSSHandler.this.setWifiDisconnectedMSSState();
                    }
                }
                if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo != null) {
                        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                            if (HwMSSHandler.this.mIs1103 || HwMSSHandler.this.mIs1105) {
                                HwMSSHandler.this.mHisiMssStateMachine.sendMessage(10);
                            }
                            HwMSSHandler.this.mWifiConnected = true;
                            HwMSSHandler.this.mHandler.sendMessage(HwMSSHandler.this.mHandler.obtainMessage(2, 0));
                        } else if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED && (HwMSSHandler.this.mIs1103 || HwMSSHandler.this.mIs1105)) {
                            HwMSSHandler.this.mHisiMssStateMachine.sendMessage(11);
                        }
                        HwMSSHandler.this.clearMssCount();
                    } else {
                        return;
                    }
                }
                if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    HwHiLog.d(HwMSSHandler.TAG, false, "WifiStateReceiver:%{public}s", new Object[]{action});
                    HwMSSHandler.this.mHandler.sendMessage(HwMSSHandler.this.mHandler.obtainMessage(2, 0));
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    int state2 = intent.getIntExtra("wifi_state", 4);
                    if (3 == state2) {
                        HwMSSHandler.this.mWifiEnabled = true;
                        HwMSSHandler.this.m2GHT40Enabled = false;
                        if (HwMSSHandler.this.mssArbi.matchHT40List()) {
                            HwMSSHandler.this.mHandler.sendMessage(HwMSSHandler.this.mHandler.obtainMessage(99, 1));
                        }
                        if (HwMSSHandler.this.mIs1103 || HwMSSHandler.this.mIs1105) {
                            HwMSSHandler.this.mHisiMssStateMachine.sendMessage(12);
                        }
                    } else if (1 == state2) {
                        HwMSSHandler.this.mWifiEnabled = false;
                        if (HwMSSHandler.this.mIs1103 || HwMSSHandler.this.mIs1105) {
                            HwMSSHandler.this.mHisiMssStateMachine.sendMessage(13);
                        }
                    } else {
                        HwMSSHandler.this.mWifiEnabled = false;
                    }
                }
                if (HwMSSHandler.THERMAL_TO_WIFI.equals(action)) {
                    HwMSSHandler.this.mThermalLevel = intent.getIntExtra("level", -1);
                    HwMSSHandler.this.mThermalTemp = intent.getIntExtra("temp", -1);
                    HwMSSHandler.this.mThermalScene = intent.getIntExtra("scene", -1);
                    HwHiLog.d(HwMSSHandler.TAG, false, "receive thermal brocast temperature is:%{public}d ,level is:%{public}d, Scene is:%{public}d", new Object[]{Integer.valueOf(HwMSSHandler.this.mThermalTemp), Integer.valueOf(HwMSSHandler.this.mThermalLevel), Integer.valueOf(HwMSSHandler.this.mThermalScene)});
                    if (HwMSSHandler.this.mThermalLevel == 0) {
                        HwMSSHandler.this.clearTempCount(true);
                    } else if (HwMSSHandler.this.mThermalLevel >= 1 && HwMSSHandler.this.mThermalLevel <= 5 && !HwMSSHandler.this.mHandler.hasMessages(1001)) {
                        HwMSSHandler.this.mHandler.sendMessage(HwMSSHandler.this.mHandler.obtainMessage(1001, 0));
                    }
                }
                if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                    int apState = intent.getIntExtra("wifi_state", 11);
                    if (apState == 11) {
                        HwMSSHandler.this.mWiFiApMode = false;
                        HwMSSHandler.this.mssArbi.setSisoFixFlag(false);
                    } else if (apState == 13) {
                        HwMSSHandler.this.mWiFiApMode = true;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restoreCurrentChainState() {
        HwHiLog.d(TAG, false, "Start restoreCurrentChainState", new Object[0]);
        int mssState = this.mWifiNative.mHwWifiNativeEx.getWifiAnt("wlan0", 0);
        if (mssState == -1 || changeAntReturnValueToMssState(mssState) == HwMSSArbitrager.MSSState.MSSUNKNOWN) {
            HwHiLog.d(TAG, false, "restore current Chain State wrong!", new Object[0]);
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
        filter.addAction("android.net.wifi.STATE_CHANGE");
        this.mWifiStateReceiver = new WifiStateReceiver();
        context.registerReceiver(this.mWifiStateReceiver, filter);
        IntentFilter thermalFilter = new IntentFilter();
        thermalFilter.addAction(THERMAL_TO_WIFI);
        context.registerReceiver(this.mWifiStateReceiver, thermalFilter, HW_SIGNATURE_OR_SYSTEM, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearMssCount() {
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
            HwHiLog.d(TAG, false, "HT40 Enabled, not allowed to swtich", new Object[0]);
            return false;
        } else if (!this.mssArbi.isMSSAllowed(direction, this.mWifiInfo.getFrequency(), HwMSSArbitrager.MSS_TRIG_TYPE.COMMON_TRIG)) {
            HwHiLog.d(TAG, false, "mss is not allowed!", new Object[0]);
            clearMssCount();
            return false;
        } else if (!this.mWifiNative.mHwWifiNativeEx.hwABSSoftHandover(direction)) {
            HwHiLog.d(TAG, false, "hwABSSoftHandover fail,direction:%{public}d", new Object[]{Integer.valueOf(direction)});
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
        HwHiLog.d(TAG, false, "mssResultCheck", new Object[0]);
        if (1 == SystemProperties.getInt("runtime.hwmss.errtest", 0)) {
            HwHiLog.d(TAG, false, "mssResultCheck:error for test", new Object[0]);
            return 1;
        }
        this.mIsDisconnectHappened = 0;
        if (!this.mWiFiApMode) {
            fetchPktcntNative();
            if (!new HwArpUtils(this.mContext).isGateWayReachable(3, 1000)) {
                HwHiLog.d(TAG, false, "mssGatewayVerifier fail", new Object[0]);
                fetchPktcntNative();
                return 2;
            }
        }
        int mssState = ((WifiNative) this.mWifiNative).mHwWifiNativeEx.getWifiAnt("wlan0", 0);
        if (mssState == -1 || changeAntReturnValueToMssState(mssState) != this.mssArbi.getMSSCurrentState()) {
            HwHiLog.d(TAG, false, "mssResultCheck:Current Chain State error", new Object[0]);
            return 1;
        } else if (this.mWiFiApMode || this.mIsDisconnectHappened != 1) {
            return 0;
        } else {
            HwHiLog.d(TAG, false, "mIsDisconnectHappened fail", new Object[0]);
            this.mIsDisconnectHappened = 0;
            return 3;
        }
    }

    public void mssRecoverWifiLink() {
        this.mWifiNative.mHwWifiNativeEx.hwABSSoftHandover(2);
        this.mssArbi.setMSSCurrentState(HwMSSArbitrager.MSSState.MSSMIMO);
        clearMssCount();
        WifiNative wifiNative = this.mWifiNative;
        wifiNative.reassociate(wifiNative.getClientInterfaceName());
    }

    @Override // com.android.server.wifi.MSS.HwMSSArbitrager.IHwMSSObserver
    public void onMSSSwitchRequest(int direction) {
        if (allowMSSSwitch() && direction == 2 && HwMSSArbitrager.MSSState.MSSSISO == this.mssArbi.getMSSCurrentState() && !this.mssArbi.getSisoFixFlag()) {
            HwHiLog.d(TAG, false, "onMSSSwitchRequest MSS_SISO_TO_MIMO", new Object[0]);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, 0));
        }
    }

    private void fetchPktcntNative() {
        WifiNative.TxPacketCounters counters;
        WifiNative wifiNative = this.mWifiNative;
        if (wifiNative != null && (counters = wifiNative.getTxPacketCounters(wifiNative.getClientInterfaceName())) != null) {
            int tx_Good = counters.txSucceeded;
            int tx_bad = counters.txFailed;
            this.mTXGood = tx_Good - this.mTxgood_Last;
            this.mTxgood_Last = tx_Good;
            this.mTXbad = tx_bad - this.mTxbad_Last;
            this.mTxbad_Last = tx_bad;
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
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x000b: APUT  (r1v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r2v0 java.lang.String) */
    /* access modifiers changed from: public */
    private int setWifiAntImpl(String iface, int mode, int operation) {
        int reason;
        Object[] objArr = new Object[3];
        objArr[0] = iface == null ? "null" : iface;
        objArr[1] = Integer.valueOf(mode);
        objArr[2] = Integer.valueOf(operation);
        HwHiLog.d(TAG, false, "setWifiAnt, interface, mode ,operation:%{public}s,%{public}d,%{public}d", objArr);
        if (!mssSwitchSupportCheck()) {
            HwHiLog.d(TAG, false, "setWifiAnt, mssSwitch not support", new Object[0]);
            return -1;
        } else if (iface == null || iface.isEmpty()) {
            HwHiLog.d(TAG, false, "setWifiAnt, parameter iface error", new Object[0]);
            return -1;
        } else if (operation > 4) {
            HwHiLog.d(TAG, false, "setWifiAnt, parameter operation error", new Object[0]);
            return -1;
        } else if (operation == 2 && this.mssArbi.getMSSCurrentState() == HwMSSArbitrager.MSSState.MSSMIMO) {
            HwHiLog.d(TAG, false, "setWifiAnt, MIMO state, no need to change to MIMO", new Object[0]);
            return -1;
        } else if (!this.mssArbi.isMSSAllowed(operation, this.mWifiInfo.getFrequency(), HwMSSArbitrager.MSS_TRIG_TYPE.CLONE_TRIG)) {
            HwHiLog.d(TAG, false, "mss is not allowed!", new Object[0]);
            clearMssCount();
            return -1;
        } else if (this.mWifiNative.mHwWifiNativeEx.setWifiAnt(iface, mode, operation) == -1) {
            HwHiLog.d(TAG, false, "setWifiAnt fail, operation:%{public}d", new Object[]{Integer.valueOf(operation)});
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
                HwHiLog.d(TAG, false, "setWifiAnt fail, operation:%{public}d", new Object[]{0});
                reason = -1;
            } else {
                this.wcsm.updateMSSCHR(this.mMSSDirection, this.mssArbi.getABSCurrentState().ordinal(), 0, getParamList());
                this.mssArbi.setSisoFixFlag(true);
                reason = 0;
            }
            HwHiLog.d(TAG, false, "setWifiAnt success", new Object[0]);
            return reason;
        }
    }

    public void setWifiAnt(String iface, int mode, int operation) {
        HwHiLog.d(TAG, false, "before setWifiAnt sendmessage", new Object[0]);
        Handler handler = this.mHandler;
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = 3;
            this.mCellPhoneWIFIIface = iface;
            this.mCellPhoneWIFIMode = mode;
            this.mCellPhoneWIFIOperation = operation;
            this.mHandler.sendMessage(msg);
            HwHiLog.d(TAG, false, "setWifiAnt sendmessage", new Object[0]);
        }
    }

    public void notifyWifiDisconnected() {
        HwHiLog.d(TAG, false, "WLAN+ MSSWifiForceToMIMO success", new Object[0]);
        setWifiDisconnectedMSSState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setWifiDisconnectedMSSState() {
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

    @Override // com.android.server.wifi.MSS.HwMSSArbitrager.IHwMSSObserver
    public void onHT40Request() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(99, 0));
    }

    public void enable2GHT40Band() {
        boolean isSupportHT40 = this.mssArbi.isSupportHT40();
        HwHiLog.d(TAG, false, "enable2GHT40Band enter", new Object[0]);
        if (!this.mWifiEnabled || !isSupportHT40 || this.m2GHT40Enabled) {
            HwHiLog.d(TAG, false, "ht40:%{public}s,state:%{public}s,support:%{public}s", new Object[]{String.valueOf(this.m2GHT40Enabled), String.valueOf(this.mWifiEnabled), String.valueOf(isSupportHT40)});
            return;
        }
        this.m2GHT40Enabled = true;
        this.mWifiNative.mHwWifiNativeEx.gameKOGAdjustSpeed(0, 99);
        if (this.mWifiConnected) {
            HwHiLog.d(TAG, false, "should reassociate after enalbe ht40 for 2.4G", new Object[0]);
            WifiNative wifiNative = this.mWifiNative;
            wifiNative.reassociate(wifiNative.getClientInterfaceName());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMssResultForBrcm(int reason) {
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
    /* access modifiers changed from: public */
    private boolean doMssSwitchForHisi(int direction, boolean force) {
        HisiMSSStateMachine hisiMSSStateMachine = this.mHisiMssStateMachine;
        if (hisiMSSStateMachine == null) {
            HwMSSUtils.loge(TAG, false, "mHisiMssStateMachine is null", new Object[0]);
            return false;
        } else if (force) {
            hisiMSSStateMachine.doMssSwitch(direction);
            this.mMSSDirection = direction;
            clearMssCount();
            return true;
        } else if (!this.mssArbi.isMSSAllowed(direction, this.mWifiInfo.getFrequency(), HwMSSArbitrager.MSS_TRIG_TYPE.COMMON_TRIG)) {
            HwHiLog.d(TAG, false, "mss is not allowed!", new Object[0]);
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
    /* access modifiers changed from: public */
    private void restoreCurrentChainStateForHisi() {
        HwMSSUtils.logd(TAG, false, "Start restoreCurrentChainState", new Object[0]);
        this.mHisiMssStateMachine.sendMessage(5);
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
                HwMSSUtils.logd(TAG, false, "report chr: mss succ", new Object[0]);
                this.wcsm.updateMSSCHR(this.mMSSDirection, this.mssArbi.getABSCurrentState().ordinal(), 0, getParamList());
                return;
            }
            HwMSSUtils.logd(TAG, false, "report chr: mss fail, mode:%{public}d", Byte.valueOf(mssstru.mssMode));
            this.wcsm.updateMSSCHR(this.mMSSDirection, this.mssArbi.getABSCurrentState().ordinal(), mssstru.mssMode, getParamList());
        }
    }

    public void onMssDrvEvent(NativeMssResult mssstru) {
        HisiMSSStateMachine hisiMSSStateMachine = this.mHisiMssStateMachine;
        if (hisiMSSStateMachine != null && mssstru != null) {
            hisiMSSStateMachine.onMssDrvEvent(mssstru);
        }
    }
}
