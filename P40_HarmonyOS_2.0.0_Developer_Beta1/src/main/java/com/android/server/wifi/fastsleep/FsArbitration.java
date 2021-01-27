package com.android.server.wifi.fastsleep;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.util.wifi.HwHiLog;
import com.android.server.hidata.appqoe.HwAppQoeResourceManager;
import com.android.server.wifi.HwArpUtils;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import java.util.ArrayList;
import java.util.List;

public class FsArbitration {
    private static final int BLACKLIST_APPLICATION_SHIFT = 4;
    private static final byte BOOL_FALSE_TO_BYTE = 0;
    private static final byte BOOL_TRUE_TO_BYTE = 1;
    private static final int CHARIOT_OFF = 0;
    private static final int CHARIOT_ON = 1;
    public static final String CHARIOT_STATUS = "chariotStatus";
    private static final int CMD_GET_LONGSLEEPCNT = 118;
    private static final int CMD_GET_SHORTSLEEPCNT = 117;
    private static final int CMD_SET_FASTSLEEP_PARAM = 115;
    private static final String CONNECTED_AP_TYPE = "apType";
    private static final int DEFAULT_ARP_DETECT_TIME = 5;
    private static final int DEFAULT_ARP_TIMEOUT_MS = 1000;
    private static final char FASTSLEEP_DISABLE = 'N';
    private static final char FASTSLEEP_ENABLE = 'Y';
    private static final char FASTSLEEP_LONGSLEEPCNT = 'L';
    private static final char FASTSLEEP_OPTIMIZATION = 'O';
    private static final char FASTSLEEP_SHORTSLEEPCNT = 'S';
    private static final String FAST_SLEEP_FEATURE_NUM = "fastsleepFeatureNum";
    private static final String FAST_SLEEP_OFF_RTT = "offRtt";
    private static final String FAST_SLEEP_OFF_TX_BAD = "offTxBad";
    private static final String FAST_SLEEP_OFF_TX_GOOD = "offTxGood";
    private static final String FAST_SLEEP_OFF_TX_RETRY = "offTxRetry";
    private static final String FAST_SLEEP_ON_RTT = "onRtt";
    private static final String FAST_SLEEP_ON_TX_BAD = "onTxBad";
    private static final String FAST_SLEEP_ON_TX_GOOD = "onTxGood";
    private static final String FAST_SLEEP_ON_TX_RETRY = "onTxRetry";
    private static final String FAST_SLEEP_SWITCH_STATUS = "isFastSleepOn";
    private static final int[] FORMAT_1103_IE = {172, 133, 61, 189};
    private static final int[] FORMAT_HUAWEI_ROUTER_IE = {0, 224, 252};
    private static final int FS_SUPPORTED_WIFI = 1;
    private static final int HI1103_CONNECT_SHIFT = 0;
    private static final int HI1103_IE_LEN = 3;
    private static final int HI1103_MASK = 255;
    private static final String IFACE = "wlan0";
    private static final int INITIAL_CONDITION_ONE = 9;
    private static final int INITIAL_CONDITION_TWO = 41;
    private static final int INVALID_ARP_RTT = -1;
    private static final int INVALID_CMD = -1;
    private static final int INVALID_FEATURE_NUM = -1;
    private static final boolean IS_FS2_ENABLE = SystemProperties.getBoolean("vendor.hw_mc.wifi_fastsleep_enable", false);
    private static final boolean IS_FS_ENABLE = SystemProperties.getBoolean("ro.config.wifi_fastsleep", true);
    private static final int LOW_LATENCY_APP_SHIFT = 2;
    private static final String MTK_CHIP_6889 = "mt6889";
    private static final int NETWORK_STATE_SHIFT = 3;
    private static final int NORMAL_WIFI = 0;
    private static final int P2P_CONNECT_SHIFT = 1;
    private static final int PG_FASTSLEEP_SHIFT = 6;
    private static final String PROP_WIFI_CHIP_TYPE = SystemProperties.get("ro.hardware", "");
    private static final int RUN_WITH_SCISSORS_TIMEOUT_MILLIS = 4000;
    private static final int SHIFT_BIT_UTIL = 1;
    private static final int STAT_QUERY_TASK_DELAY_SECOND = 30000;
    private static final String TAG = "FSArbitration";
    private static final int WHITELIST_APPLICATION_SHIFT = 5;
    public static final String WHITELIST_STATUS = "whiteListStatus";
    private static FsArbitration mFsArbitration = null;
    private int mArbitraCond;
    private byte mConnectedApType = BOOL_FALSE_TO_BYTE;
    private Context mContext;
    private int mFastSleepLongIdle = 0;
    private int mFastSleepOffRtt = 0;
    private int mFastSleepOnRtt = 0;
    private int mFastSleepShortIdle = 0;
    private int mFeatureNum = -1;
    private FsMonitor mFsMonitor = null;
    private Handler mHandler;
    private HwWifiCHRService mHwWifiChrService = null;
    private boolean mIsChangeFromChariot = false;
    private boolean mIsChariot = false;
    private boolean mIsConnectState = false;
    private boolean mIsFastSleepOn = false;
    private boolean mIsSceenOn = true;
    private WifiChipStat mWifiChipStat = null;
    private WifiNative mWifiNative;

    private FsArbitration(Context context, WifiNative wifiNative) {
        this.mContext = context;
        this.mWifiNative = wifiNative;
        if (IS_FS2_ENABLE) {
            this.mArbitraCond = 41;
        } else {
            this.mArbitraCond = 9;
        }
        this.mHwWifiChrService = HwWifiServiceFactory.getHwWifiCHRService();
        if (MTK_CHIP_6889.equals(PROP_WIFI_CHIP_TYPE)) {
            this.mWifiChipStat = WifiChipStat.createWifiChipStat(this.mWifiNative);
        }
        initFastSleepHandler();
    }

    public static synchronized FsArbitration createFsArbitration(Context context, WifiNative wifiNative) {
        FsArbitration fsArbitration;
        synchronized (FsArbitration.class) {
            if (mFsArbitration == null) {
                mFsArbitration = new FsArbitration(context, wifiNative);
            }
            fsArbitration = mFsArbitration;
        }
        return fsArbitration;
    }

    public static synchronized FsArbitration getInstance() {
        FsArbitration fsArbitration;
        synchronized (FsArbitration.class) {
            fsArbitration = mFsArbitration;
        }
        return fsArbitration;
    }

    public void notifyNetworkRoamingCompleted() {
        HwHiLog.d(TAG, false, "NotifyNetworkRoaming completed", new Object[0]);
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendEmptyMessage(10);
        } else {
            HwHiLog.e(TAG, false, "notifyNetworkRoamingCompleted, mhandler is null!", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initFsMonitor() {
        this.mFsMonitor = new FsMonitor(this.mContext, this.mHandler);
    }

    private int checkArpRtt() {
        int arpRtt = -1;
        HwArpUtils hwArpUtils = new HwArpUtils(this.mContext);
        for (int i = 0; i < 5 && (arpRtt = (int) hwArpUtils.getGateWayArpRTT(1000)) == -1; i++) {
        }
        return arpRtt;
    }

    public void setFastSleepLongIdle(int fastsleepLongIdle) {
        this.mFastSleepLongIdle = fastsleepLongIdle;
    }

    public void setFastSleepShortIdle(int fastsleepShortIdle) {
        this.mFastSleepShortIdle = fastsleepShortIdle;
    }

    public int getFastSleepShortIdle() {
        return this.mFastSleepShortIdle;
    }

    public int getFastSleepLongIdle() {
        return this.mFastSleepLongIdle;
    }

    public int sendFastSleepCmdtoDriver(int cmd) {
        if (cmd <= 0) {
            HwHiLog.e(TAG, false, "Invalid command", new Object[0]);
            return -1;
        } else if (cmd == 117) {
            return this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 117, new byte[]{83});
        } else {
            if (cmd != 118) {
                return -1;
            }
            return this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 118, new byte[]{76});
        }
    }

    private void uploadFastSleepChr(boolean isFastSleepOn) {
        WifiNative wifiNative;
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager == null) {
            HwHiLog.e(TAG, false, "wifiManager is null", new Object[0]);
            return;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null || (wifiNative = this.mWifiNative) == null) {
            HwHiLog.e(TAG, false, "wifiInfo or mWifiNative is null", new Object[0]);
            return;
        }
        WifiNative.TxPacketCounters counters = wifiNative.getTxPacketCounters(wifiNative.getClientInterfaceName());
        if (counters != null) {
            int fastSleepOnTxGood = 0;
            int fastSleepOnTxBad = 0;
            int fastSleepOnTxRetry = 0;
            int fastSleepOffTxGood = 0;
            int fastSleepOffTxBad = 0;
            int fastSleepOffTxRetry = 0;
            if (isFastSleepOn) {
                fastSleepOffTxGood = counters.txSucceeded;
                fastSleepOffTxBad = counters.txFailed;
                fastSleepOffTxRetry = (int) wifiInfo.txRetries;
            } else {
                fastSleepOnTxGood = counters.txSucceeded;
                fastSleepOnTxBad = counters.txFailed;
                fastSleepOnTxRetry = (int) wifiInfo.txRetries;
            }
            if (this.mHwWifiChrService != null) {
                Bundle chrData = new Bundle();
                chrData.putBoolean(FAST_SLEEP_SWITCH_STATUS, isFastSleepOn);
                chrData.putInt(FAST_SLEEP_ON_RTT, this.mFastSleepOnRtt);
                chrData.putInt(FAST_SLEEP_ON_TX_GOOD, fastSleepOnTxGood);
                chrData.putInt(FAST_SLEEP_ON_TX_BAD, fastSleepOnTxBad);
                chrData.putInt(FAST_SLEEP_ON_TX_RETRY, fastSleepOnTxRetry);
                chrData.putInt(FAST_SLEEP_OFF_RTT, this.mFastSleepOffRtt);
                chrData.putInt(FAST_SLEEP_OFF_TX_GOOD, fastSleepOffTxGood);
                chrData.putInt(FAST_SLEEP_OFF_TX_BAD, fastSleepOffTxBad);
                chrData.putInt(FAST_SLEEP_OFF_TX_RETRY, fastSleepOffTxRetry);
                chrData.putInt(FAST_SLEEP_FEATURE_NUM, this.mFeatureNum);
                chrData.putByte(CONNECTED_AP_TYPE, this.mConnectedApType);
                this.mHwWifiChrService.uploadDFTEvent(25, chrData);
            }
        }
    }

    private int updateFastSleepArbitraCond(int arbitraCond) {
        int fsArbitraCond;
        if (this.mIsSceenOn) {
            fsArbitraCond = arbitraCond & -65;
        } else {
            fsArbitraCond = arbitraCond & -33;
        }
        HwHiLog.d(TAG, false, "updateFastSleepArbitraCond, fsArbitraCond: %{public}d", new Object[]{Integer.valueOf(fsArbitraCond)});
        return fsArbitraCond;
    }

    private synchronized void checkFastSleepSwitch(int arbitraCond, boolean isFastSleepOn, boolean isConnectState) {
        if (!IS_FS_ENABLE) {
            HwHiLog.d(TAG, false, "IS_FS_ENABLE: %{public}s", new Object[]{Boolean.valueOf(IS_FS_ENABLE)});
            return;
        }
        int fsArbitraCond = updateFastSleepArbitraCond(arbitraCond);
        HwHiLog.d(TAG, false, "checkFastSleepSwitch, mArbitraCond: %{public}d", new Object[]{Integer.valueOf(this.mArbitraCond)});
        if (fsArbitraCond == 0 && !isFastSleepOn) {
            HwHiLog.d(TAG, false, "fastsleep on", new Object[0]);
            this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 115, new byte[]{89});
            setFastSleepShortIdle(this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 117, new byte[]{83}));
            setFastSleepLongIdle(this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 118, new byte[]{76}));
            this.mIsFastSleepOn = true;
            this.mFastSleepOnRtt = checkArpRtt();
            uploadFastSleepChr(this.mIsFastSleepOn);
        } else if (this.mIsChangeFromChariot) {
            HwHiLog.d(TAG, false, "because of change from chariot, fastsleep resume normal", new Object[0]);
            this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 115, new byte[]{89});
        } else {
            if (fsArbitraCond != 0 && isFastSleepOn && !this.mIsChariot) {
                HwHiLog.d(TAG, false, "fastsleep off", new Object[0]);
                this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 115, new byte[]{78});
                this.mIsFastSleepOn = false;
                uploadFastSleepChr(this.mIsFastSleepOn);
            }
            if (fsArbitraCond != 0 && !isFastSleepOn && isConnectState) {
                HwHiLog.d(TAG, false, "when connect ap, need to update featureNum and connectedApType", new Object[0]);
                this.mIsFastSleepOn = false;
                uploadFastSleepChr(this.mIsFastSleepOn);
            }
            if (this.mIsConnectState) {
                this.mIsConnectState = false;
            }
        }
    }

    private boolean isSupportFs(ScanResult.InformationElement ie) {
        if (ie.bytes == null || ie.bytes.length < FORMAT_1103_IE.length) {
            HwHiLog.d(TAG, false, "ie.bytes.length is invalid!", new Object[0]);
            this.mConnectedApType = BOOL_FALSE_TO_BYTE;
            this.mFeatureNum = -1;
            return false;
        }
        for (int index = 0; index < FORMAT_1103_IE.length; index++) {
            int element = ie.bytes[index] & 255;
            int[] iArr = FORMAT_1103_IE;
            if (element != iArr[index]) {
                this.mConnectedApType = BOOL_FALSE_TO_BYTE;
                this.mFeatureNum = -1;
                return false;
            } else if (index == 3) {
                this.mConnectedApType = BOOL_TRUE_TO_BYTE;
                this.mFeatureNum = iArr[3];
                return true;
            }
        }
        this.mConnectedApType = BOOL_FALSE_TO_BYTE;
        this.mFeatureNum = -1;
        return false;
    }

    private boolean isSupportHuaweiRouter(ScanResult.InformationElement ie) {
        if (ie.bytes == null || ie.bytes.length < FORMAT_1103_IE.length) {
            HwHiLog.d(TAG, false, "ie.bytes.length is invalid!", new Object[0]);
            this.mConnectedApType = BOOL_FALSE_TO_BYTE;
            this.mFeatureNum = -1;
            return false;
        }
        for (int index = 0; index < FORMAT_HUAWEI_ROUTER_IE.length; index++) {
            if ((ie.bytes[index] & 255) != FORMAT_HUAWEI_ROUTER_IE[index]) {
                this.mConnectedApType = BOOL_FALSE_TO_BYTE;
                this.mFeatureNum = -1;
                return false;
            }
        }
        this.mConnectedApType = BOOL_TRUE_TO_BYTE;
        this.mFeatureNum = ie.bytes[3] & 255;
        return true;
    }

    private boolean isInOuiBlackList(ScanResult.InformationElement ie) {
        if (ie.bytes != null && ie.bytes.length >= FORMAT_1103_IE.length) {
            return HwAppQoeResourceManager.getInstance().isInRouterBlackList(ie);
        }
        HwHiLog.d(TAG, false, "ie.bytes.length is invalid!", new Object[0]);
        this.mConnectedApType = BOOL_FALSE_TO_BYTE;
        this.mFeatureNum = -1;
        return false;
    }

    private int parseTag(ScanResult.InformationElement[] ies) {
        if (ies == null) {
            return 0;
        }
        int fastsleepFlag = 0;
        for (ScanResult.InformationElement ie : ies) {
            if (ie.id == 221) {
                if (!IS_FS2_ENABLE && isSupportFs(ie) && isMobileAP()) {
                    return 1;
                }
                if (IS_FS2_ENABLE && ((isSupportFs(ie) && isMobileAP()) || isSupportHuaweiRouter(ie))) {
                    fastsleepFlag = 1;
                    if (isInOuiBlackList(ie)) {
                        this.mConnectedApType = BOOL_FALSE_TO_BYTE;
                        this.mFeatureNum = -1;
                        return 0;
                    }
                }
            }
        }
        return fastsleepFlag;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleForegroundActivityChanged(Message msg) {
        Bundle extraParam = (Bundle) msg.obj;
        if (extraParam == null) {
            HwHiLog.d(TAG, false, "extraParam is null", new Object[0]);
            return;
        }
        int chariotFlag = extraParam.getInt(CHARIOT_STATUS);
        HwHiLog.d(TAG, false, "Now is chariot application?: %{public}d", new Object[]{Integer.valueOf(chariotFlag)});
        if (chariotFlag != 1 || this.mIsChariot) {
            if (chariotFlag != 0 || !this.mIsChariot) {
                this.mIsChangeFromChariot = false;
            } else {
                this.mIsChariot = false;
                this.mIsChangeFromChariot = true;
            }
            if (IS_FS2_ENABLE) {
                HwHiLog.d(TAG, false, "fastsleep 2.0 judge application", new Object[0]);
                if (msg.arg1 > 0) {
                    this.mArbitraCond |= 16;
                } else {
                    this.mArbitraCond &= -17;
                }
                if (extraParam.getInt(WHITELIST_STATUS) > 0) {
                    this.mArbitraCond &= -33;
                } else {
                    this.mArbitraCond |= 32;
                }
            } else {
                HwHiLog.d(TAG, false, "fastsleep 2.0 don't open", new Object[0]);
            }
            if (msg.arg2 > 0) {
                this.mArbitraCond |= 4;
            } else {
                this.mArbitraCond &= -5;
            }
            HwHiLog.d(TAG, false, "Foreground SCENE judgement, mArbitraCond: %{public}d", new Object[]{Integer.valueOf(this.mArbitraCond)});
            checkFastSleepSwitch(this.mArbitraCond, this.mIsFastSleepOn, this.mIsConnectState);
            return;
        }
        HwHiLog.d(TAG, false, "in chariot application, set new param", new Object[0]);
        this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 115, new byte[]{79});
        this.mIsFastSleepOn = true;
        this.mIsChariot = true;
        this.mIsChangeFromChariot = false;
    }

    private void checkConnectedAp() {
        WifiInfo wifiInfo = ((WifiManager) this.mContext.getSystemService("wifi")).getConnectionInfo();
        if (wifiInfo == null) {
            HwHiLog.d(TAG, false, "wifiinfo is null", new Object[0]);
            return;
        }
        String currentBssid = wifiInfo.getBSSID();
        if (currentBssid == null) {
            HwHiLog.d(TAG, false, "currentBssid is null", new Object[0]);
            return;
        }
        List<ScanResult> scanResults = new ArrayList<>();
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (!wifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(scanResults, wifiInjector) {
            /* class com.android.server.wifi.fastsleep.$$Lambda$FsArbitration$2jLwlJA77AIcCb40R1tmVt0iU */
            private final /* synthetic */ List f$0;
            private final /* synthetic */ WifiInjector f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.addAll(this.f$1.getScanRequestProxy().getScanResults());
            }
        }, 4000)) {
            HwHiLog.e(TAG, false, "Failed to get scan results", new Object[0]);
            return;
        }
        for (ScanResult result : scanResults) {
            if (currentBssid.equals(result.BSSID)) {
                if (parseTag(result.informationElements) == 1) {
                    this.mArbitraCond &= -2;
                    HwHiLog.d(TAG, false, "1103 wifi, mArbitraCond: %{public}d", new Object[]{Integer.valueOf(this.mArbitraCond)});
                } else {
                    this.mArbitraCond |= 1;
                    HwHiLog.d(TAG, false, "not 1103 wifi, mArbitraCond: %{public}d", new Object[]{Integer.valueOf(this.mArbitraCond)});
                }
            }
        }
    }

    private boolean isMobileAP() {
        if (this.mContext != null) {
            return HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.mContext);
        }
        return false;
    }

    private void resetFastsleepIdle() {
        setFastSleepLongIdle(0);
        setFastSleepShortIdle(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiDisconnectMsg() {
        this.mArbitraCond |= 1;
        this.mArbitraCond |= 8;
        checkFastSleepSwitch(this.mArbitraCond, this.mIsFastSleepOn, this.mIsConnectState);
        if (MTK_CHIP_6889.equals(PROP_WIFI_CHIP_TYPE) && this.mHandler.hasMessages(11)) {
            this.mHandler.removeMessages(11);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiConnectedMsg() {
        if (MTK_CHIP_6889.equals(PROP_WIFI_CHIP_TYPE)) {
            this.mHandler.sendEmptyMessageDelayed(11, 30000);
        }
        if (!this.mIsFastSleepOn) {
            this.mFastSleepOffRtt = checkArpRtt();
        }
        resetFastsleepIdle();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiStatQuery() {
        this.mWifiChipStat.getWifiChipData();
        this.mWifiChipStat.reportWifiChipData();
        this.mHandler.sendEmptyMessageDelayed(11, 30000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pConnectdMsg(Message msg) {
        if (msg.arg1 > 0) {
            this.mArbitraCond |= 2;
            HwHiLog.d(TAG, false, "P2P Enabled", new Object[0]);
        } else {
            this.mArbitraCond &= -3;
            HwHiLog.d(TAG, false, "P2P Disabled", new Object[0]);
        }
        checkFastSleepSwitch(this.mArbitraCond, this.mIsFastSleepOn, this.mIsConnectState);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleInternetAccessMsg() {
        this.mArbitraCond &= -9;
        checkConnectedAp();
        this.mIsConnectState = true;
        checkFastSleepSwitch(this.mArbitraCond, this.mIsFastSleepOn, this.mIsConnectState);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleInternetDiscMsg() {
        this.mArbitraCond |= 8;
        checkFastSleepSwitch(this.mArbitraCond, this.mIsFastSleepOn, this.mIsConnectState);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRoamingCompletedMsg() {
        checkConnectedAp();
        checkFastSleepSwitch(this.mArbitraCond, this.mIsFastSleepOn, this.mIsConnectState);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePgFastSleepStatusMsg(Message msg) {
        if (msg.arg1 > 0) {
            this.mArbitraCond &= -65;
            HwHiLog.d(TAG, false, "PG FastSleep Enabled", new Object[0]);
        } else {
            this.mArbitraCond |= 64;
            HwHiLog.d(TAG, false, "PG FastSleep Disabled", new Object[0]);
        }
        checkFastSleepSwitch(this.mArbitraCond, this.mIsFastSleepOn, this.mIsConnectState);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIsScreenOnMsg(Message msg) {
        if (msg.arg1 > 0) {
            this.mIsSceenOn = true;
            HwHiLog.d(TAG, false, "FS Screen ON", new Object[0]);
        } else {
            this.mIsSceenOn = false;
            HwHiLog.d(TAG, false, "FS Screen OFF", new Object[0]);
        }
        checkFastSleepSwitch(this.mArbitraCond, this.mIsFastSleepOn, this.mIsConnectState);
    }

    private void initFastSleepHandler() {
        HandlerThread handlerThread = new HandlerThread("FSArbitration_handler_thread");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.wifi.fastsleep.FsArbitration.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        FsArbitration.this.initFsMonitor();
                        return;
                    case 2:
                        FsArbitration.this.handleWifiConnectedMsg();
                        return;
                    case 3:
                        FsArbitration.this.handleWifiDisconnectMsg();
                        return;
                    case 4:
                        FsArbitration.this.handleP2pConnectdMsg(msg);
                        return;
                    case 5:
                    case 6:
                    default:
                        return;
                    case 7:
                        FsArbitration.this.handleForegroundActivityChanged(msg);
                        return;
                    case 8:
                        FsArbitration.this.handleInternetAccessMsg();
                        return;
                    case 9:
                        FsArbitration.this.handleInternetDiscMsg();
                        return;
                    case 10:
                        FsArbitration.this.handleRoamingCompletedMsg();
                        return;
                    case 11:
                        FsArbitration.this.handleWifiStatQuery();
                        return;
                    case 12:
                        FsArbitration.this.handlePgFastSleepStatusMsg(msg);
                        return;
                    case 13:
                        FsArbitration.this.handleIsScreenOnMsg(msg);
                        return;
                }
            }
        };
        this.mHandler.sendEmptyMessage(1);
    }
}
