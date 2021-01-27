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
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.MSS.HwMssArbitrager;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.wificond.NativeMssResult;
import java.util.ArrayList;

@SuppressLint({"HandlerLeak"})
public class HwMssHandler implements HwMSSHandlerManager, HwMssArbitrager.IHwMssObserver {
    private static final int GATE_WAY_REACH_TIMEOUT = 1000;
    private static final int GATE_WAY_REACH_TIMES = 3;
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
    private static final int MSS_STATE_RES_CYCLE = 120;
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
    private static final String TAG = "HwMssHandler";
    private static final String THERMAL_TO_WIFI = "huawei.intent.action.THERMAL_TO_WIFI";
    private static final int WIFI_THERMAL_ACTION_COUNT = 3;
    private static final int WIFI_THERMAL_LEVEL1 = 1;
    private static final int WIFI_THERMAL_LEVEL5 = 5;
    private static final int WIFI_THERMAL_MIMO_TO_SISO = 1001;
    private static final int WIFI_THERMAL_MIMO_TO_SISO_INTERVAL = 3000;
    private static HwMssHandler sInstance = null;
    private static int sRestoreCount = 0;
    private static int sRssiMimoCount = 0;
    private static int sRssiSisoCount = 0;
    private static int sTputMimoCount = 0;
    private static int sTputSisoCount = 0;
    private IHwMssBlacklistMgr blackMgr = null;
    private boolean is2gHt40Enabled = false;
    private boolean isWiFiApMode = false;
    private boolean isWifiConnected = false;
    private boolean isWifiEnabled = false;
    protected ArrayList list;
    private String mCellPhoneWifiIface = "wlan0";
    private int mCellPhoneWifiMode = -1;
    private int mCellPhoneWifiOperation = -1;
    private Context mContext = null;
    private int mCurrentRssi = 0;
    private int mCurrentTemp = 0;
    private int mCurrentTput = 0;
    private Handler mHandler = null;
    private HisiMssStateMachine mHisiMssStateMachine = null;
    private boolean mIs1103 = false;
    private boolean mIs1105 = false;
    private int mIsDisconnectHappened = 0;
    private int mIsSuppCompleted = 0;
    private HwMssBluetoothManager mMssBluetoothManager = null;
    private int mMssDirection = 0;
    private int mMssStateResCycle = 20;
    private int mTempSisoCount = 0;
    private int mThermalLevel = -1;
    private int mThermalScene = -1;
    private int mThermalTemp = -1;
    private int mTriggerReason;
    private int mTxGood = 0;
    private int mTxbad = 0;
    private int mTxbadLast = 0;
    private int mTxgoodLast = 0;
    private WifiInfo mWifiInfo = null;
    private WifiNative mWifiNative = null;
    private WifiStateReceiver mWifiStateReceiver = null;
    private HwMssArbitrager mssArbi = null;
    private boolean mssIsHighTput = false;
    private HwWifiCHRService wcsm = null;

    /* access modifiers changed from: private */
    public enum MssTrigArbitrateType {
        RSSI_TRIG,
        TPUT_TRIG,
        TEMP_TRIG
    }

    static /* synthetic */ int access$1608(HwMssHandler x0) {
        int i = x0.mTempSisoCount;
        x0.mTempSisoCount = i + 1;
        return i;
    }

    private HwMssHandler(Context cxt, WifiNative wifinav, WifiInfo wifiinfo) {
        this.mContext = cxt;
        this.mWifiNative = wifinav;
        this.mWifiInfo = wifiinfo;
        this.mssArbi = HwMssArbitrager.getInstance(this.mContext);
        this.mIs1103 = HwMssUtils.is1103();
        this.mIs1105 = HwMssUtils.is1105();
        if (this.mIs1103 || this.mIs1105) {
            this.mMssStateResCycle = 120;
            HwMssUtils.setAllowSwitch(false);
            this.mHisiMssStateMachine = HisiMssStateMachine.createHisiMssStateMachine(this, cxt, wifinav, wifiinfo);
        } else {
            this.blackMgr = HwMssBlackListManager.getInstance(this.mContext);
        }
        this.wcsm = HwWifiCHRServiceImpl.getInstance();
        hwMssHandlerInit();
        mssCheckInit(this.mContext);
        this.mssArbi.registerMssObserver(this);
        if (SystemProperties.getInt("ro.config.hw_wifi_btc_mss_en", 0) == 1) {
            this.mMssBluetoothManager = HwMssBluetoothManager.getInstance(cxt);
            this.mMssBluetoothManager.init(wifinav, wifiinfo);
        }
    }

    public static synchronized HwMSSHandlerManager getDefault(Context cxt, WifiNative wifinav, WifiInfo wifiinfo) {
        HwMssHandler hwMssHandler;
        synchronized (HwMssHandler.class) {
            if (sInstance == null) {
                sInstance = new HwMssHandler(cxt, wifinav, wifiinfo);
            }
            hwMssHandler = sInstance;
        }
        return hwMssHandler;
    }

    public static synchronized HwMssHandler getInstance() {
        synchronized (HwMssHandler.class) {
            if (sInstance == null) {
                return null;
            }
            return sInstance;
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

    private void hwMssHandlerInit() {
        HandlerThread handlerThread = new HandlerThread("mss_handler_thread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.wifi.MSS.HwMssHandler.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (HwMssHandler.this.mIs1103 || HwMssHandler.this.mIs1105) {
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
                        HwMssHandler.this.mssArbi.setSisoFixFlag(false);
                        HwMssHandler.this.mWifiNative.mHwWifiNativeEx.hwABSSoftHandover(2);
                        HwMssHandler.this.mssArbi.setMssCurrentState(HwMssArbitrager.MssState.MSSMIMO);
                        HwMssHandler.this.clearMssCount();
                    } else if (i != 2) {
                        if (i == 3) {
                            HwMssHandler hwMssHandler = HwMssHandler.this;
                            HwHiLog.d(HwMssHandler.TAG, false, "setWifiAntImpl result is %{public}d", new Object[]{Integer.valueOf(hwMssHandler.setWifiAntImpl(hwMssHandler.mCellPhoneWifiIface, HwMssHandler.this.mCellPhoneWifiMode, HwMssHandler.this.mCellPhoneWifiOperation))});
                            HwMssHandler.this.clearMssCount();
                        } else if (i == 99) {
                            HwMssHandler.this.enable2GHt40Band();
                        } else if (i == 1001) {
                            doMimoToSiso();
                        }
                    } else if (!HwMssHandler.this.isMssSwitchSupport()) {
                        HwHiLog.d(HwMssHandler.TAG, false, "MSS switch is not support", new Object[0]);
                    } else {
                        HwMssHandler.this.restoreCurrentChainState();
                    }
                } else if (HwMssHandler.this.doMssSwitch(msg.arg1)) {
                    HwMssHandler.this.mTriggerReason = msg.arg2;
                    HwMssHandler.this.handleMssResultForBrcm(HwMssHandler.this.mssResultCheck());
                }
            }

            private void handleMessageForHisi(Message msg) {
                int i = msg.what;
                if (i != 0) {
                    if (i == 1) {
                        HwMssHandler.this.mssArbi.setSisoFixFlag(false);
                        HwMssHandler.this.hasMssSwitchForHisi(2, true);
                    } else if (i == 2) {
                        HwMssHandler.this.restoreCurrentChainStateForHisi();
                    } else if (i == 3) {
                        HwMssHandler hwMssHandler = HwMssHandler.this;
                        hwMssHandler.hasMssSwitchForHisi(hwMssHandler.mCellPhoneWifiOperation, true);
                    } else if (i != 99 && i == 1001) {
                        doMimoToSiso();
                    }
                } else if (HwMssHandler.this.hasMssSwitchForHisi(msg.arg1, false)) {
                    HwMssHandler.this.mTriggerReason = msg.arg2;
                }
            }

            private void doMimoToSiso() {
                if (HwMssHandler.this.mThermalLevel >= 1 && HwMssHandler.this.mThermalLevel <= 5) {
                    HwMssHandler.access$1608(HwMssHandler.this);
                    if (HwMssHandler.this.mTempSisoCount == 3) {
                        HwMssHandler.this.tempChangeWiFiState(1001);
                        HwMssHandler.this.clearTempCount(false);
                    }
                    if (!HwMssHandler.this.mHandler.hasMessages(1001)) {
                        sendEmptyMessageDelayed(1001, 3000);
                    }
                }
            }
        };
    }

    private boolean isMssPreArbitrate(MssTrigArbitrateType type, int direction) {
        int i;
        int tputSwitchMimoTresh = getTputSwitchMimoTresh();
        int i2 = AnonymousClass2.$SwitchMap$com$android$server$wifi$MSS$HwMssHandler$MssTrigArbitrateType[type.ordinal()];
        if (i2 != 1) {
            if (i2 != 2) {
                if (i2 == 3 && direction == 1 && !isRssiFourCells()) {
                    HwHiLog.d(TAG, false, "rssi is below four cells, do not switch", new Object[0]);
                    return false;
                }
            } else if (direction == 1 && !isRssiFourCells()) {
                HwHiLog.d(TAG, false, "rssi is below four sells, do not switch", new Object[0]);
                return false;
            } else if (direction == 2 && (i = this.mThermalLevel) >= 1 && i <= 5) {
                HwHiLog.d(TAG, false, "temperature is over the threshold, do not switch", new Object[0]);
                return false;
            }
        } else if (direction == 1 && ((tputSwitchMimoTresh != 0 && this.mCurrentTput >= tputSwitchMimoTresh) || this.mssIsHighTput)) {
            HwHiLog.d(TAG, false, "throughput is over the threshold, do not switch", new Object[0]);
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.wifi.MSS.HwMssHandler$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$wifi$MSS$HwMssHandler$MssTrigArbitrateType = new int[MssTrigArbitrateType.values().length];

        static {
            try {
                $SwitchMap$com$android$server$wifi$MSS$HwMssHandler$MssTrigArbitrateType[MssTrigArbitrateType.RSSI_TRIG.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$wifi$MSS$HwMssHandler$MssTrigArbitrateType[MssTrigArbitrateType.TPUT_TRIG.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$wifi$MSS$HwMssHandler$MssTrigArbitrateType[MssTrigArbitrateType.TEMP_TRIG.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMssSwitchSupport() {
        if (!this.mssArbi.isMssSwitchBandSupport()) {
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
        if (isMssSwitchSupport() && allowMssSwitch()) {
            this.mCurrentRssi = rssi;
            sRestoreCount++;
            if (sRestoreCount == this.mMssStateResCycle) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(2, 0));
                sRestoreCount = 0;
            }
            if (isRssiFourCells() && this.mssArbi.getMssCurrentState() == HwMssArbitrager.MssState.MSSMIMO) {
                sRssiSisoCount++;
                sRssiMimoCount = 0;
            } else if (!isRssiBelowTwoCells() || this.mssArbi.getMssCurrentState() != HwMssArbitrager.MssState.MSSSISO) {
                sRssiSisoCount = 0;
                sRssiMimoCount = 0;
            } else {
                sRssiMimoCount++;
                sRssiSisoCount = 0;
            }
            HwHiLog.d(TAG, false, "doMssSwitch rssi:%{public}d CurrentChainState:%{public}d SISOCount:%{public}d MIMOCount:%{public}d", new Object[]{Integer.valueOf(rssi), this.mssArbi.getMssCurrentState(), Integer.valueOf(sRssiSisoCount), Integer.valueOf(sRssiMimoCount)});
            if (sRssiMimoCount >= 5 && this.mssArbi.getMssCurrentState() == HwMssArbitrager.MssState.MSSSISO) {
                direction = 2;
            } else if (sRssiSisoCount >= 5 && this.mssArbi.getMssCurrentState() == HwMssArbitrager.MssState.MSSMIMO) {
                direction = 1;
                if (!isMssPreArbitrate(MssTrigArbitrateType.RSSI_TRIG, 1)) {
                    return;
                }
            } else {
                return;
            }
            this.mHandler.sendMessage(this.mHandler.obtainMessage(0, direction, MssTrigArbitrateType.RSSI_TRIG.ordinal()));
        }
    }

    public void mssSwitchCheckTPut(int tput) {
        int direction;
        if (isMssSwitchSupport() && allowMssSwitch()) {
            this.mCurrentTput = tput;
            int tputSwitchToMimoTresh = getTputSwitchMimoTresh();
            int tputSwitchToSisoTresh = getTputSwitchSisoTresh();
            if (tputSwitchToMimoTresh != 0 && tputSwitchToSisoTresh != 0) {
                if (tput >= tputSwitchToMimoTresh) {
                    sTputMimoCount++;
                    sTputSisoCount = 0;
                } else if (tput <= tputSwitchToSisoTresh) {
                    sTputSisoCount++;
                    sTputMimoCount = 0;
                } else {
                    sTputSisoCount = 0;
                    sTputMimoCount = 0;
                }
                HwHiLog.d(TAG, false, "doMssSwitch tput:%{public}d CurrentChainState:%{public}d SISOCount:%{public}d MIMOCount:%{public}d mssIsHighTput:%{public}s", new Object[]{Integer.valueOf(tput), this.mssArbi.getMssCurrentState(), Integer.valueOf(sTputSisoCount), Integer.valueOf(sTputMimoCount), String.valueOf(this.mssIsHighTput)});
                if (sTputMimoCount >= 3) {
                    direction = 2;
                    this.mssIsHighTput = true;
                    if (this.mssArbi.getMssCurrentState() == HwMssArbitrager.MssState.MSSMIMO || !isMssPreArbitrate(MssTrigArbitrateType.TPUT_TRIG, 2)) {
                        sTputSisoCount = 0;
                        sTputMimoCount = 0;
                        return;
                    }
                } else if (sTputSisoCount >= 3) {
                    direction = 1;
                    this.mssIsHighTput = false;
                    if (this.mssArbi.getMssCurrentState() == HwMssArbitrager.MssState.MSSSISO || !isMssPreArbitrate(MssTrigArbitrateType.TPUT_TRIG, 1)) {
                        sTputSisoCount = 0;
                        sTputMimoCount = 0;
                        return;
                    }
                } else {
                    return;
                }
                this.mHandler.sendMessage(this.mHandler.obtainMessage(0, direction, MssTrigArbitrateType.TPUT_TRIG.ordinal()));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tempChangeWiFiState(int action) {
        if (action == 1001 && this.mssArbi.isWiFiConnected()) {
            if (!isMssSwitchSupport() || this.mssArbi.getMssCurrentState() == HwMssArbitrager.MssState.MSSSISO || !allowMssSwitch()) {
                HwHiLog.d(TAG, false, "NOT MIMO or NOT support this switch return", new Object[0]);
            } else if (!isMssPreArbitrate(MssTrigArbitrateType.TEMP_TRIG, 1)) {
                HwHiLog.d(TAG, false, "rssi do not allow this switch", new Object[0]);
            } else {
                HwHiLog.d(TAG, false, "temperature is over the threshold, begin to switch,direction is MIMO --> SISO", new Object[0]);
                this.mHandler.sendMessage(this.mHandler.obtainMessage(0, 1, MssTrigArbitrateType.TEMP_TRIG.ordinal()));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearTempCount(boolean isNeedRemoveMsg) {
        this.mTempSisoCount = 0;
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
                if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action) && isSupStateChangeHandled(intent, action)) {
                    return;
                }
                if (!"android.net.wifi.STATE_CHANGE".equals(action) || !handleNetworkStateChange(intent)) {
                    if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                        HwHiLog.d(HwMssHandler.TAG, false, "WifiStateReceiver:%{public}s", new Object[]{action});
                        HwMssHandler.this.mHandler.sendMessage(HwMssHandler.this.mHandler.obtainMessage(2, 0));
                    } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                        handleWifiStateChange(intent);
                    } else {
                        HwHiLog.d(HwMssHandler.TAG, false, "neither WIFI_P2P_CONNECTION_CHANGED_ACTIONor WIFI_STATE_CHANGED_ACTION", new Object[0]);
                    }
                    if (HwMssHandler.THERMAL_TO_WIFI.equals(action)) {
                        HwMssHandler.this.mThermalLevel = intent.getIntExtra("level", -1);
                        HwMssHandler.this.mThermalTemp = intent.getIntExtra("temp", -1);
                        HwMssHandler.this.mThermalScene = intent.getIntExtra("scene", -1);
                        HwHiLog.d(HwMssHandler.TAG, false, "receive thermal brocast temperature is:%{public}d ,level is:%{public}d, Scene is:%{public}d", new Object[]{Integer.valueOf(HwMssHandler.this.mThermalTemp), Integer.valueOf(HwMssHandler.this.mThermalLevel), Integer.valueOf(HwMssHandler.this.mThermalScene)});
                        if (HwMssHandler.this.mThermalLevel == 0) {
                            HwMssHandler.this.clearTempCount(true);
                        } else if (HwMssHandler.this.mThermalLevel >= 1 && HwMssHandler.this.mThermalLevel <= 5 && !HwMssHandler.this.mHandler.hasMessages(1001)) {
                            HwMssHandler.this.mHandler.sendMessage(HwMssHandler.this.mHandler.obtainMessage(1001, 0));
                        }
                    }
                    if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                        handleApStateChange(intent);
                    }
                }
            }
        }

        private void handleApStateChange(Intent intent) {
            int apState = intent.getIntExtra("wifi_state", 11);
            if (apState == 11) {
                HwMssHandler.this.isWiFiApMode = false;
                HwMssHandler.this.mssArbi.setSisoFixFlag(false);
            } else if (apState == 13) {
                HwMssHandler.this.isWiFiApMode = true;
            } else {
                HwHiLog.d(HwMssHandler.TAG, false, "the ap state is neither WIFI_AP_STATE_DISABLEDor WIFI_AP_STATE_ENABLED", new Object[0]);
            }
        }

        private void handleWifiStateChange(Intent intent) {
            int state = intent.getIntExtra("wifi_state", 4);
            if (state == 3) {
                HwMssHandler.this.isWifiEnabled = true;
                HwMssHandler.this.is2gHt40Enabled = false;
                if (HwMssHandler.this.mssArbi.isMatchHt40List()) {
                    HwMssHandler.this.mHandler.sendMessage(HwMssHandler.this.mHandler.obtainMessage(99, 1));
                }
                if (HwMssHandler.this.mIs1103 || HwMssHandler.this.mIs1105) {
                    HwMssHandler.this.mHisiMssStateMachine.sendMessage(12);
                }
            } else if (state == 1) {
                HwMssHandler.this.isWifiEnabled = false;
                if (HwMssHandler.this.mIs1103 || HwMssHandler.this.mIs1105) {
                    HwMssHandler.this.mHisiMssStateMachine.sendMessage(13);
                }
            } else {
                HwMssHandler.this.isWifiEnabled = false;
            }
        }

        private boolean handleNetworkStateChange(Intent intent) {
            NetworkInfo networkInfo;
            if (!(intent.getParcelableExtra("networkInfo") instanceof NetworkInfo) || (networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo")) == null) {
                return true;
            }
            if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                if (HwMssHandler.this.mIs1103 || HwMssHandler.this.mIs1105) {
                    HwMssHandler.this.mHisiMssStateMachine.sendMessage(10);
                }
                HwMssHandler.this.isWifiConnected = true;
                HwMssHandler.this.mHandler.sendMessage(HwMssHandler.this.mHandler.obtainMessage(2, 0));
            } else if (networkInfo.getDetailedState() != NetworkInfo.DetailedState.DISCONNECTED) {
                HwHiLog.d(HwMssHandler.TAG, false, "the detailed state of networkInfois neither CONNECTED or DISCONNECTED", new Object[0]);
            } else if (HwMssHandler.this.mIs1103 || HwMssHandler.this.mIs1105) {
                HwMssHandler.this.mHisiMssStateMachine.sendMessage(11);
            }
            HwMssHandler.this.clearMssCount();
            return false;
        }

        private boolean isSupStateChangeHandled(Intent intent, String action) {
            if (!(intent.getParcelableExtra("newState") instanceof SupplicantState)) {
                return true;
            }
            SupplicantState state = (SupplicantState) intent.getParcelableExtra("newState");
            if (state != null) {
                HwHiLog.d(HwMssHandler.TAG, false, "WifiStateReceiver:%{public}s SupplicantState:%{public}s", new Object[]{action, state});
            }
            if (state == SupplicantState.DISCONNECTED) {
                HwMssHandler.this.isWifiConnected = false;
                HwMssHandler.this.setWifiDisconnectedMssState();
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restoreCurrentChainState() {
        HwHiLog.d(TAG, false, "Start restoreCurrentChainState", new Object[0]);
        int mssState = this.mWifiNative.mHwWifiNativeEx.getWifiAnt("wlan0", 0);
        if (mssState == -1 || changeAntReturnValueToMssState(mssState) == HwMssArbitrager.MssState.MSSUNKNOWN) {
            HwHiLog.d(TAG, false, "restore current Chain State wrong!", new Object[0]);
            return;
        }
        this.mssArbi.setMssCurrentState(changeAntReturnValueToMssState(mssState));
        clearMssCount();
        if (changeAntReturnValueToMssState(mssState) == HwMssArbitrager.MssState.MSSMIMO) {
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
        sRssiSisoCount = 0;
        sRssiMimoCount = 0;
        sTputSisoCount = 0;
        sTputMimoCount = 0;
    }

    private HwMssArbitrager.MssState changeAntReturnValueToMssState(int check) {
        if (check == 1) {
            return HwMssArbitrager.MssState.MSSMIMO;
        }
        if (check == 0) {
            return HwMssArbitrager.MssState.MSSSISO;
        }
        return HwMssArbitrager.MssState.MSSUNKNOWN;
    }

    public boolean doMssSwitch(int direction) {
        if (this.is2gHt40Enabled && direction == 1) {
            HwHiLog.d(TAG, false, "HT40 Enabled, not allowed to swtich", new Object[0]);
            return false;
        } else if (!this.mssArbi.isMssAllowed(direction, this.mWifiInfo.getFrequency(), HwMssArbitrager.MssTrigType.COMMON_TRIG)) {
            HwHiLog.d(TAG, false, "mss is not allowed!", new Object[0]);
            clearMssCount();
            return false;
        } else if (!this.mWifiNative.mHwWifiNativeEx.hwABSSoftHandover(direction)) {
            HwHiLog.d(TAG, false, "hwABSSoftHandover fail,direction:%{public}d", new Object[]{Integer.valueOf(direction)});
            clearMssCount();
            return false;
        } else {
            if (direction == 2) {
                this.mssArbi.setMssCurrentState(HwMssArbitrager.MssState.MSSMIMO);
            } else {
                this.mssArbi.setMssCurrentState(HwMssArbitrager.MssState.MSSSISO);
            }
            this.mMssDirection = direction;
            clearMssCount();
            return true;
        }
    }

    public int mssResultCheck() {
        WifiNative wifiNative = this.mWifiNative;
        HwHiLog.d(TAG, false, "mssResultCheck", new Object[0]);
        if (SystemProperties.getInt("runtime.hwmss.errtest", 0) == 1) {
            HwHiLog.d(TAG, false, "mssResultCheck:error for test", new Object[0]);
            return 1;
        }
        this.mIsDisconnectHappened = 0;
        if (!this.isWiFiApMode) {
            fetchPktcntNative();
            if (!new HwArpUtils(this.mContext).isGateWayReachable(3, 1000)) {
                HwHiLog.d(TAG, false, "mssGatewayVerifier fail", new Object[0]);
                fetchPktcntNative();
                return 2;
            }
        }
        int mssState = this.mWifiNative.mHwWifiNativeEx.getWifiAnt("wlan0", 0);
        if (mssState == -1 || changeAntReturnValueToMssState(mssState) != this.mssArbi.getMssCurrentState()) {
            HwHiLog.d(TAG, false, "mssResultCheck:Current Chain State error", new Object[0]);
            return 1;
        } else if (this.isWiFiApMode || this.mIsDisconnectHappened != 1) {
            return 0;
        } else {
            HwHiLog.d(TAG, false, "mIsDisconnectHappened fail", new Object[0]);
            this.mIsDisconnectHappened = 0;
            return 3;
        }
    }

    public void mssRecoverWifiLink() {
        this.mWifiNative.mHwWifiNativeEx.hwABSSoftHandover(2);
        this.mssArbi.setMssCurrentState(HwMssArbitrager.MssState.MSSMIMO);
        clearMssCount();
        WifiNative wifiNative = this.mWifiNative;
        wifiNative.reassociate(wifiNative.getClientInterfaceName());
    }

    @Override // com.android.server.wifi.MSS.HwMssArbitrager.IHwMssObserver
    public void onMssSwitchRequest(int direction) {
        if (allowMssSwitch() && direction == 2 && this.mssArbi.getMssCurrentState() == HwMssArbitrager.MssState.MSSSISO && !this.mssArbi.getSisoFixFlag()) {
            HwHiLog.d(TAG, false, "onMssSwitchRequest MSS_SISO_TO_MIMO", new Object[0]);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, 0));
        }
    }

    private void fetchPktcntNative() {
        WifiNative.TxPacketCounters counters;
        WifiNative wifiNative = this.mWifiNative;
        if (wifiNative != null && (counters = wifiNative.getTxPacketCounters(wifiNative.getClientInterfaceName())) != null) {
            int txGood = counters.txSucceeded;
            int txbad = counters.txFailed;
            this.mTxGood = txGood - this.mTxgoodLast;
            this.mTxgoodLast = txGood;
            this.mTxbad = txbad - this.mTxbadLast;
            this.mTxbadLast = txbad;
        }
    }

    private ArrayList getParamList() {
        this.list = new ArrayList();
        this.list.add(Integer.valueOf(this.mTxGood));
        this.list.add(Integer.valueOf(this.mTxbad));
        this.list.add(Integer.valueOf(this.mThermalLevel));
        this.list.add(Integer.valueOf(this.mThermalTemp));
        this.list.add(Integer.valueOf(this.mThermalScene));
        this.list.add(Integer.valueOf(this.mTriggerReason));
        return this.list;
    }

    /* access modifiers changed from: private */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x000a: APUT  (r0v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r1v0 java.lang.String) */
    /* access modifiers changed from: public */
    private int setWifiAntImpl(String iface, int mode, int operation) {
        Object[] objArr = new Object[3];
        objArr[0] = iface == null ? "null" : iface;
        objArr[1] = Integer.valueOf(mode);
        objArr[2] = Integer.valueOf(operation);
        HwHiLog.d(TAG, false, "setWifiAnt, interface, mode ,operation:%{public}s,%{public}d,%{public}d", objArr);
        if (!isMssSwitchSupport()) {
            HwHiLog.d(TAG, false, "setWifiAnt, mssSwitch not support", new Object[0]);
            return -1;
        } else if (iface.isEmpty()) {
            HwHiLog.d(TAG, false, "setWifiAnt, parameter iface error", new Object[0]);
            return -1;
        } else if (operation > 4) {
            HwHiLog.d(TAG, false, "setWifiAnt, parameter operation error", new Object[0]);
            return -1;
        } else if (operation == 2 && this.mssArbi.getMssCurrentState() == HwMssArbitrager.MssState.MSSMIMO) {
            HwHiLog.d(TAG, false, "setWifiAnt, MIMO state, no need to change to MIMO", new Object[0]);
            return -1;
        } else if (!this.mssArbi.isMssAllowed(operation, this.mWifiInfo.getFrequency(), HwMssArbitrager.MssTrigType.CLONE_TRIG)) {
            HwHiLog.d(TAG, false, "mss is not allowed!", new Object[0]);
            clearMssCount();
            return -1;
        } else if (this.mWifiNative.mHwWifiNativeEx.setWifiAnt(iface, mode, operation) == -1) {
            HwHiLog.d(TAG, false, "setWifiAnt fail, operation:%{public}d", new Object[]{Integer.valueOf(operation)});
            clearMssCount();
            return -1;
        } else {
            if (operation == 2) {
                this.mssArbi.setMssCurrentState(HwMssArbitrager.MssState.MSSMIMO);
            } else {
                this.mssArbi.setMssCurrentState(HwMssArbitrager.MssState.MSSSISO);
            }
            this.mMssDirection = operation;
            clearMssCount();
            int reason = handleMssResultCheck(0);
            HwHiLog.d(TAG, false, "setWifiAnt success", new Object[0]);
            return reason;
        }
    }

    private int handleMssResultCheck(int reason) {
        if (mssResultCheck() != 0) {
            mssRecoverWifiLink();
            this.wcsm.updateMSSCHR(this.mMssDirection, this.mssArbi.getAbsCurrentState().ordinal(), reason, getParamList());
            HwHiLog.d(TAG, false, "setWifiAnt fail, operation:%{public}d", new Object[]{Integer.valueOf(reason)});
            return -1;
        }
        this.wcsm.updateMSSCHR(this.mMssDirection, this.mssArbi.getAbsCurrentState().ordinal(), 0, getParamList());
        this.mssArbi.setSisoFixFlag(true);
        return 0;
    }

    public void setWifiAnt(String iface, int mode, int operation) {
        HwHiLog.d(TAG, false, "before setWifiAnt sendmessage", new Object[0]);
        Handler handler = this.mHandler;
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = 3;
            this.mCellPhoneWifiIface = iface;
            this.mCellPhoneWifiMode = mode;
            this.mCellPhoneWifiOperation = operation;
            this.mHandler.sendMessage(msg);
            HwHiLog.d(TAG, false, "setWifiAnt sendmessage", new Object[0]);
        }
    }

    public void notifyWifiDisconnected() {
        HwHiLog.d(TAG, false, "WLAN+ MSSWifiForceToMIMO success", new Object[0]);
        setWifiDisconnectedMssState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setWifiDisconnectedMssState() {
        this.mIsDisconnectHappened = 1;
        this.mssArbi.setSisoFixFlag(false);
        if (this.mssArbi.getMssCurrentState() == HwMssArbitrager.MssState.MSSSISO && !this.mssArbi.isP2pConnected() && !this.isWiFiApMode) {
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

    private boolean allowMssSwitch() {
        if (SystemProperties.getInt("ro.config.hw_wifi_btc_mss_en", 0) == 1) {
            return false;
        }
        return isHuaweiAp() || isMobileAP() || this.mssArbi.matchAllowMssApkList();
    }

    @Override // com.android.server.wifi.MSS.HwMssArbitrager.IHwMssObserver
    public void onHt40Request() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(99, 0));
    }

    public void enable2GHt40Band() {
        boolean isSupportHt40 = this.mssArbi.isSupportHt40();
        HwHiLog.d(TAG, false, "enable2GHt40Band enter", new Object[0]);
        if (!this.isWifiEnabled || !isSupportHt40 || this.is2gHt40Enabled) {
            HwHiLog.d(TAG, false, "ht40:%{public}s,state:%{public}s,support:%{public}s", new Object[]{String.valueOf(this.is2gHt40Enabled), String.valueOf(this.isWifiEnabled), String.valueOf(isSupportHt40)});
            return;
        }
        this.is2gHt40Enabled = true;
        this.mWifiNative.mHwWifiNativeEx.gameKOGAdjustSpeed(0, 99);
        if (this.isWifiConnected) {
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
            this.wcsm.updateMSSCHR(this.mMssDirection, this.mssArbi.getAbsCurrentState().ordinal(), reason, getParamList());
            return;
        }
        this.wcsm.updateMSSCHR(this.mMssDirection, this.mssArbi.getAbsCurrentState().ordinal(), 0, getParamList());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasMssSwitchForHisi(int direction, boolean isForce) {
        HisiMssStateMachine hisiMssStateMachine = this.mHisiMssStateMachine;
        if (hisiMssStateMachine == null) {
            HwMssUtils.logE(TAG, false, "mHisiMssStateMachine is null", new Object[0]);
            return false;
        } else if (isForce) {
            hisiMssStateMachine.doMssSwitch(direction);
            this.mMssDirection = direction;
            clearMssCount();
            return true;
        } else if (!this.mssArbi.isMssAllowed(direction, this.mWifiInfo.getFrequency(), HwMssArbitrager.MssTrigType.COMMON_TRIG)) {
            HwHiLog.d(TAG, false, "mss is not allowed!", new Object[0]);
            clearMssCount();
            return false;
        } else {
            this.mHisiMssStateMachine.doMssSwitch(direction);
            this.mMssDirection = direction;
            clearMssCount();
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restoreCurrentChainStateForHisi() {
        HwMssUtils.logD(TAG, false, "Start restoreCurrentChainState", new Object[0]);
        this.mHisiMssStateMachine.sendMessage(5);
    }

    public void callbackSyncMssState(HwMssArbitrager.MssState state) {
        this.mssArbi.setMssCurrentState(state);
        if (state == HwMssArbitrager.MssState.MSSMIMO) {
            this.mssArbi.setSisoFixFlag(false);
        }
        clearMssCount();
    }

    public void callbackReportChr(NativeMssResult mssResult) {
        if (mssResult != null && mssResult.vapNum > 0 && this.wcsm != null) {
            if (mssResult.mssResult == 1) {
                HwMssUtils.logD(TAG, false, "report chr: mss succ", new Object[0]);
                this.wcsm.updateMSSCHR(this.mMssDirection, this.mssArbi.getAbsCurrentState().ordinal(), 0, getParamList());
                return;
            }
            HwMssUtils.logD(TAG, false, "report chr: mss fail, mode:%{public}d", Byte.valueOf(mssResult.mssMode));
            this.wcsm.updateMSSCHR(this.mMssDirection, this.mssArbi.getAbsCurrentState().ordinal(), mssResult.mssMode, getParamList());
        }
    }

    public void onMssDrvEvent(NativeMssResult mssResult) {
        HisiMssStateMachine hisiMssStateMachine = this.mHisiMssStateMachine;
        if (hisiMssStateMachine != null && mssResult != null) {
            hisiMssStateMachine.onMssDrvEvent(mssResult);
        }
    }
}
