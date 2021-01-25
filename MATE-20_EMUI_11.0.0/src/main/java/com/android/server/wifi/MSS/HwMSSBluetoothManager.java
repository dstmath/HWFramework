package com.android.server.wifi.MSS;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothCodecConfig;
import android.bluetooth.BluetoothCodecStatus;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.HwArpUtils;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.util.List;

/* access modifiers changed from: package-private */
public class HwMSSBluetoothManager {
    private static final int MSG_CHECK_AND_SWITCH_COEXT_MODE = 0;
    private static final int MSS_WIFI_BT_FULL_COEXT = 5;
    private static final int MSS_WIFI_BT_MIMO_AND_FULL_COEXT = 4;
    private static final int MSS_WIFI_BT_SISO_AND_LIGHT_COEXT = 3;
    private static final String TAG = "HwMSSBluetoothManager";
    private static final int WIFI_BT_COEXT_MIMO_MODE = 0;
    private static final long WIFI_BT_COEXT_MODE_DELAY_TIME = 5000;
    private static final int WIFI_BT_COEXT_SISO_MODE = 1;
    private static final int WIFI_BT_COEXT_SWITCH_LIMIT_FAILED_CNT = 3;
    private static HwMSSBluetoothManager mHwMSSBluetoothManager = null;
    private static String mIfname = "wlan0";
    private boolean mA2dpBinded = false;
    private BluetoothA2dp mA2dpService = null;
    private BluetoothProfile.ServiceListener mA2dpServiceListener = new BluetoothProfile.ServiceListener() {
        /* class com.android.server.wifi.MSS.HwMSSBluetoothManager.AnonymousClass3 */

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            HwHiLog.d(HwMSSBluetoothManager.TAG, false, "a2dp service connected", new Object[0]);
            HwMSSBluetoothManager.this.mA2dpService = (BluetoothA2dp) proxy;
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int profile) {
            HwHiLog.d(HwMSSBluetoothManager.TAG, false, "a2dp service disconnected", new Object[0]);
        }
    };
    private IHwMSSBlacklistMgr mBlackMgr = null;
    private int mCoextMode = 0;
    private Context mContext;
    private String mCurrentBssid = "";
    private String mCurrentSsid = "";
    private boolean mForceAdjustCoextMode = false;
    private Handler mHandler = new Handler() {
        /* class com.android.server.wifi.MSS.HwMSSBluetoothManager.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                HwMSSBluetoothManager.this.checkAndSwitchCoextMode();
            }
        }
    };
    private boolean mIsP2pConnected = false;
    private boolean mIsWiFi2GConnected = false;
    private HwMSSArbitrager mMssArbi = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.MSS.HwMSSBluetoothManager.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwHiLog.e(HwMSSBluetoothManager.TAG, false, "mReceiver, intent is null", new Object[0]);
                return;
            }
            String action = intent.getAction();
            if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action) || "android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(action)) {
                HwMSSBluetoothManager.this.wifiP2PStateChange(intent);
            } else if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
                HwMSSBluetoothManager.this.btAdapterStateChange(intent);
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                HwMSSBluetoothManager.this.wifiConnectionStateChange(intent);
            }
            if (HwMSSBluetoothManager.this.mHandler.hasMessages(0)) {
                HwMSSBluetoothManager.this.mHandler.removeMessages(0);
            }
            HwMSSBluetoothManager.this.mHandler.sendEmptyMessageDelayed(0, HwMSSBluetoothManager.WIFI_BT_COEXT_MODE_DELAY_TIME);
        }
    };
    private String mRecordSsid = "";
    private int mSwitchFailedCnt = 0;
    private WifiInfo mWifiInfo = null;
    private WifiNative mWifiNative = null;

    private HwMSSBluetoothManager(Context cxt) {
        this.mContext = cxt;
        this.mMssArbi = HwMSSArbitrager.getInstance(this.mContext);
        this.mBlackMgr = HwMSSBlackListManager.getInstance(this.mContext);
    }

    public static synchronized HwMSSBluetoothManager getInstance(Context context) {
        HwMSSBluetoothManager hwMSSBluetoothManager;
        synchronized (HwMSSBluetoothManager.class) {
            if (mHwMSSBluetoothManager == null) {
                mHwMSSBluetoothManager = new HwMSSBluetoothManager(context);
            }
            hwMSSBluetoothManager = mHwMSSBluetoothManager;
        }
        return hwMSSBluetoothManager;
    }

    public void init(WifiNative wifinav, WifiInfo wifiinfo) {
        if (wifinav == null || wifiinfo == null) {
            HwHiLog.w(TAG, false, "init, wifinav or wifiinfo is null", new Object[0]);
        } else if (this.mWifiNative == null || this.mWifiInfo == null) {
            this.mWifiNative = wifinav;
            this.mWifiInfo = wifiinfo;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
            filter.addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
            filter.addAction("android.bluetooth.a2dp.profile.action.CODEC_CONFIG_CHANGED");
            filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            filter.addAction("android.net.wifi.STATE_CHANGE");
            filter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
            filter.addAction("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
            this.mContext.registerReceiver(this.mReceiver, filter);
            HwHiLog.d(TAG, false, "init success", new Object[0]);
        } else {
            HwHiLog.w(TAG, false, "do not need init", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wifiP2PStateChange(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            boolean isCurrentP2pConnected = false;
            HwHiLog.d(TAG, false, "p2p receiver:%{public}s", new Object[]{action});
            if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                NetworkInfo p2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (p2pNetworkInfo != null) {
                    isCurrentP2pConnected = p2pNetworkInfo.isConnected();
                } else {
                    isCurrentP2pConnected = false;
                }
            } else if ("android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(action)) {
                if (intent.getIntExtra("extraState", -1) == 2) {
                    isCurrentP2pConnected = true;
                } else {
                    isCurrentP2pConnected = false;
                }
            }
            if (!this.mIsP2pConnected || isCurrentP2pConnected) {
                this.mForceAdjustCoextMode = false;
            } else {
                restoreChainMask();
                this.mForceAdjustCoextMode = true;
            }
            this.mIsP2pConnected = isCurrentP2pConnected;
            HwHiLog.d(TAG, false, "mIsP2pConnected : %{public}s, mForceCheck : %{public}s", new Object[]{String.valueOf(this.mIsP2pConnected), String.valueOf(this.mForceAdjustCoextMode)});
            restoreBtcMode();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void btAdapterStateChange(Intent intent) {
        if (intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1) == 12 && !this.mA2dpBinded) {
            bindBluetoothProfile();
        }
    }

    private void bindBluetoothProfile() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            HwHiLog.e(TAG, false, "bluetoothAdapter is null, bind fail", new Object[0]);
            return;
        }
        HwHiLog.d(TAG, false, "bindBluetoothProfile success", new Object[0]);
        this.mA2dpBinded = bluetoothAdapter.getProfileProxy(this.mContext, this.mA2dpServiceListener, 2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wifiConnectionStateChange(Intent intent) {
        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        boolean connected = networkInfo != null && networkInfo.isConnected();
        if (!connected) {
            restoreBtcMode();
            this.mCoextMode = 0;
        }
        WifiInfo info = (WifiInfo) intent.getParcelableExtra("wifiInfo");
        if (!connected || info == null || !info.is24GHz()) {
            this.mIsWiFi2GConnected = false;
            return;
        }
        this.mIsWiFi2GConnected = true;
        WifiInfo wifiInfo = this.mWifiInfo;
        if (wifiInfo != null) {
            this.mCurrentSsid = wifiInfo.getSSID();
            this.mCurrentBssid = this.mWifiInfo.getBSSID();
        }
    }

    private boolean appointedA2dpDeviceConnected() {
        BluetoothCodecConfig config = getBtCodecConfig();
        if (config == null) {
            HwHiLog.e(TAG, false, "appointedA2dpDeviceConnected, config is null", new Object[0]);
            return false;
        }
        HwHiLog.d(TAG, false, "BT code type : %{public}d", new Object[]{Integer.valueOf(config.getCodecType())});
        BluetoothA2dp bluetoothA2dp = this.mA2dpService;
        if (bluetoothA2dp == null) {
            HwHiLog.e(TAG, false, "appointedA2dpDeviceConnected, mA2dpService is null", new Object[0]);
            return false;
        }
        List<BluetoothDevice> mDevices = bluetoothA2dp.getConnectedDevices();
        if (mDevices == null || mDevices.size() == 0) {
            HwHiLog.d(TAG, false, "There is no a2dp device", new Object[0]);
            return false;
        } else if (config.getCodecType() == 4 || config.getCodecType() == 3) {
            return true;
        } else {
            return false;
        }
    }

    private BluetoothCodecConfig getBtCodecConfig() {
        BluetoothA2dp bluetoothA2dp = this.mA2dpService;
        if (bluetoothA2dp == null) {
            HwHiLog.e(TAG, false, "getBTCodecConfig, mA2dpService is null", new Object[0]);
            return null;
        }
        BluetoothCodecStatus configStatus = bluetoothA2dp.getCodecStatus(null);
        if (configStatus != null) {
            return configStatus.getCodecConfig();
        }
        return null;
    }

    private void setCoextMode(int coextMode) {
        int operation;
        if (this.mWifiNative == null || this.mWifiInfo == null || this.mMssArbi == null || this.mBlackMgr == null) {
            HwHiLog.e(TAG, false, "setCoextMode, class member is null", new Object[0]);
            return;
        }
        if (this.mForceAdjustCoextMode) {
            adjustCoextMode();
            this.mForceAdjustCoextMode = false;
        }
        int i = this.mCoextMode;
        if (coextMode != i) {
            HwHiLog.d(TAG, false, "current coext mode : %{public}d, set coext mode : %{public}d", new Object[]{Integer.valueOf(i), Integer.valueOf(coextMode)});
            if (isAntennaAllowedSwitch(coextMode)) {
                this.mCoextMode = coextMode;
                if (coextMode == 1) {
                    operation = 3;
                } else {
                    operation = 4;
                }
                if (this.mWifiNative.mHwWifiNativeEx.setWifiAnt(mIfname, 0, operation) == -1) {
                    HwHiLog.d(TAG, false, "set wifi ant fail, operation:%{public}d", new Object[]{Integer.valueOf(operation)});
                    restoreCoextMode();
                } else if (!isAntennaSwitchSuccess(coextMode)) {
                    HwHiLog.w(TAG, false, "switch fail, coextMode : %{public}d", new Object[]{Integer.valueOf(coextMode)});
                    if (isReachLimitFailedCnt()) {
                        HwHiLog.w(TAG, false, "add to the black list : %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(this.mRecordSsid)});
                        this.mBlackMgr.addToBlacklist(this.mCurrentSsid, this.mCurrentBssid, -1);
                        restoreCoextMode();
                        this.mSwitchFailedCnt = 0;
                    }
                } else {
                    this.mSwitchFailedCnt = 0;
                    HwHiLog.d(TAG, false, "set coext mode success, coext mode : %{public}d, operation : %{public}d", new Object[]{Integer.valueOf(this.mCoextMode), Integer.valueOf(operation)});
                }
            }
        }
    }

    private void adjustCoextMode() {
        WifiNative wifiNative = this.mWifiNative;
        if (wifiNative == null) {
            HwHiLog.e(TAG, false, "adjustCoextMode, mWifiNative is null", new Object[0]);
            return;
        }
        int mssValue = wifiNative.mHwWifiNativeEx.getWifiAnt(mIfname, 0);
        int coextMode = transAntReturnValueToCoextMode(mssValue);
        if (mssValue == -1 || coextMode == -1) {
            HwHiLog.d(TAG, false, "get wifi ant failed", new Object[0]);
            return;
        }
        this.mCoextMode = coextMode;
        HwHiLog.d(TAG, false, "adjust coext mode to : %{public}d", new Object[]{Integer.valueOf(this.mCoextMode)});
    }

    private boolean isReachLimitFailedCnt() {
        String str = this.mCurrentSsid;
        if (str == null || this.mRecordSsid == null) {
            HwHiLog.e(TAG, false, "isReachLimitFailedCnt, mCurrentSsid or mRecordSsid is null", new Object[0]);
            return false;
        }
        HwHiLog.d(TAG, false, "mCurrentSsid is : %{public}s, mRecordSsid is : %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(str), StringUtilEx.safeDisplaySsid(this.mRecordSsid)});
        if (this.mRecordSsid.equals(this.mCurrentSsid)) {
            this.mSwitchFailedCnt++;
        }
        this.mRecordSsid = this.mCurrentSsid;
        HwHiLog.d(TAG, false, "mSwitchFailedCnt is : %{public}d", new Object[]{Integer.valueOf(this.mSwitchFailedCnt)});
        if (this.mSwitchFailedCnt >= 3) {
            return true;
        }
        return false;
    }

    private boolean isAntennaAllowedSwitch(int coextMode) {
        if (this.mMssArbi == null) {
            HwHiLog.e(TAG, false, "isAntennaAllowedSwitch, mMssArbi is null", new Object[0]);
            return false;
        } else if (coextMode == 1 && SystemProperties.getInt("runtime.hwmss.blktest", 0) == 0 && this.mMssArbi.isInMSSBlacklist()) {
            HwHiLog.d(TAG, false, "the ap is in black list, do not allowed switch", new Object[0]);
            return false;
        } else if (!this.mIsP2pConnected) {
            return true;
        } else {
            HwHiLog.d(TAG, false, "p2p mode, do not allowed switch", new Object[0]);
            return false;
        }
    }

    public boolean isAntennaSwitchSuccess(int coextMode) {
        if (this.mWifiNative == null) {
            HwHiLog.e(TAG, false, "isAntennaSwitchSuccess, mWifiNative is null", new Object[0]);
            return false;
        } else if (1 == SystemProperties.getInt("runtime.hwmss.errtest", 0)) {
            HwHiLog.d(TAG, false, "switch check : error for test", new Object[0]);
            return false;
        } else if (!new HwArpUtils(this.mContext).isGateWayReachable(3, 1000)) {
            HwHiLog.d(TAG, false, "gateway verfier fail", new Object[0]);
            return false;
        } else {
            int mssValue = this.mWifiNative.mHwWifiNativeEx.getWifiAnt(mIfname, 0);
            if (mssValue != -1 && transAntReturnValueToCoextMode(mssValue) == coextMode) {
                return true;
            }
            HwHiLog.d(TAG, false, "current chain value error, mssValue : %{public}d", new Object[]{Integer.valueOf(mssValue)});
            return false;
        }
    }

    private int transAntReturnValueToCoextMode(int value) {
        if (value == 1) {
            return 0;
        }
        if (value == 2) {
            return 1;
        }
        return -1;
    }

    private void restoreCoextMode() {
        WifiNative wifiNative = this.mWifiNative;
        if (wifiNative == null) {
            HwHiLog.e(TAG, false, "restoreCoextMode, mWifiNative is null", new Object[0]);
        } else if (this.mCoextMode != 0) {
            this.mCoextMode = 0;
            if (wifiNative.mHwWifiNativeEx.setWifiAnt(mIfname, 0, 4) == -1) {
                HwHiLog.d(TAG, false, "restore coext mode fail", new Object[0]);
            }
        }
    }

    private void restoreChainMask() {
        WifiNative wifiNative = this.mWifiNative;
        if (wifiNative == null) {
            HwHiLog.e(TAG, false, "restoreTxRxchain, mWifiNative is null", new Object[0]);
        } else if (wifiNative.mHwWifiNativeEx.setWifiAnt(mIfname, 0, 4) == -1) {
            HwHiLog.d(TAG, false, "restore Tx Rx chain fail", new Object[0]);
        }
    }

    private void restoreBtcMode() {
        WifiNative wifiNative = this.mWifiNative;
        if (wifiNative == null) {
            HwHiLog.e(TAG, false, "restoreBtcMode, mWifiNative is null", new Object[0]);
        } else if (wifiNative.mHwWifiNativeEx.setWifiAnt(mIfname, 0, 5) == -1) {
            HwHiLog.d(TAG, false, "restore btc mode fail", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void checkAndSwitchCoextMode() {
        int coextMode = 0;
        if (this.mIsWiFi2GConnected && appointedA2dpDeviceConnected()) {
            coextMode = 1;
        }
        setCoextMode(coextMode);
    }
}
