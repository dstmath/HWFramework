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
import android.util.Log;
import com.android.server.wifi.HwArpUtils;
import com.android.server.wifi.WifiNative;
import java.util.List;

class HwMSSBluetoothManager {
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
    /* access modifiers changed from: private */
    public BluetoothA2dp mA2dpService = null;
    private BluetoothProfile.ServiceListener mA2dpServiceListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d(HwMSSBluetoothManager.TAG, "a2dp service connected");
            BluetoothA2dp unused = HwMSSBluetoothManager.this.mA2dpService = (BluetoothA2dp) proxy;
        }

        public void onServiceDisconnected(int profile) {
            Log.d(HwMSSBluetoothManager.TAG, "a2dp service disconnected");
        }
    };
    private IHwMSSBlacklistMgr mBlackMgr = null;
    private int mCoextMode = 0;
    private Context mContext;
    private String mCurrentBssid = "";
    private String mCurrentSsid = "";
    private boolean mForceAdjustCoextMode = false;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
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
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.e(HwMSSBluetoothManager.TAG, "mReceiver, intent is null");
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
            Log.w(TAG, "init, wifinav or wifiinfo is null");
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
            Log.d(TAG, "init success");
        } else {
            Log.w(TAG, "do not need init");
        }
    }

    /* access modifiers changed from: private */
    public void wifiP2PStateChange(Intent intent) {
        String action = intent.getAction();
        boolean isCurrentP2pConnected = false;
        Log.d(TAG, "p2p receiver:" + action);
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
        if (this.mIsP2pConnected != 1 || isCurrentP2pConnected) {
            this.mForceAdjustCoextMode = false;
        } else {
            restoreChainMask();
            this.mForceAdjustCoextMode = true;
        }
        this.mIsP2pConnected = isCurrentP2pConnected;
        Log.d(TAG, "mIsP2pConnected : " + this.mIsP2pConnected + ", mForceCheck : " + this.mForceAdjustCoextMode);
        restoreBtcMode();
    }

    /* access modifiers changed from: private */
    public void btAdapterStateChange(Intent intent) {
        if (intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1) == 12 && !this.mA2dpBinded) {
            bindBluetoothProfile();
        }
    }

    private void bindBluetoothProfile() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "bluetoothAdapter is null, bind fail");
            return;
        }
        Log.d(TAG, "bindBluetoothProfile success");
        this.mA2dpBinded = bluetoothAdapter.getProfileProxy(this.mContext, this.mA2dpServiceListener, 2);
    }

    /* access modifiers changed from: private */
    public void wifiConnectionStateChange(Intent intent) {
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
        if (this.mWifiInfo != null) {
            this.mCurrentSsid = this.mWifiInfo.getSSID();
            this.mCurrentBssid = this.mWifiInfo.getBSSID();
        }
    }

    private boolean appointedA2dpDeviceConnected() {
        BluetoothCodecConfig config = getBtCodecConfig();
        if (config == null) {
            Log.e(TAG, "appointedA2dpDeviceConnected, config is null");
            return false;
        }
        Log.d(TAG, "BT code type : " + config.getCodecType());
        if (this.mA2dpService == null) {
            Log.e(TAG, "appointedA2dpDeviceConnected, mA2dpService is null");
            return false;
        }
        List<BluetoothDevice> mDevices = this.mA2dpService.getConnectedDevices();
        if (mDevices == null || mDevices.size() == 0) {
            Log.d(TAG, "There is no a2dp device");
            return false;
        } else if (config.getCodecType() == 4 || config.getCodecType() == 3) {
            return true;
        } else {
            return false;
        }
    }

    private BluetoothCodecConfig getBtCodecConfig() {
        if (this.mA2dpService == null) {
            Log.e(TAG, "getBTCodecConfig, mA2dpService is null");
            return null;
        }
        BluetoothCodecStatus configStatus = this.mA2dpService.getCodecStatus(null);
        if (configStatus != null) {
            return configStatus.getCodecConfig();
        }
        return null;
    }

    private void setCoextMode(int coextMode) {
        int operation;
        if (this.mWifiNative == null || this.mWifiInfo == null || this.mMssArbi == null || this.mBlackMgr == null) {
            Log.e(TAG, "setCoextMode, class member is null");
            return;
        }
        if (this.mForceAdjustCoextMode) {
            adjustCoextMode();
            this.mForceAdjustCoextMode = false;
        }
        if (coextMode != this.mCoextMode) {
            Log.d(TAG, "current coext mode : " + this.mCoextMode + ", set coext mode : " + coextMode);
            if (isAntennaAllowedSwitch(coextMode)) {
                this.mCoextMode = coextMode;
                if (coextMode == 1) {
                    operation = 3;
                } else {
                    operation = 4;
                }
                if (this.mWifiNative.setWifiAnt(mIfname, 0, operation) == -1) {
                    Log.d(TAG, "set wifi ant fail, operation:" + operation);
                    restoreCoextMode();
                } else if (!isAntennaSwitchSuccess(coextMode)) {
                    Log.w(TAG, "switch fail, coextMode : " + coextMode);
                    if (isReachLimitFailedCnt()) {
                        Log.w(TAG, "add to the black list : " + this.mRecordSsid);
                        this.mBlackMgr.addToBlacklist(this.mCurrentSsid, this.mCurrentBssid, -1);
                        restoreCoextMode();
                        this.mSwitchFailedCnt = 0;
                    }
                } else {
                    this.mSwitchFailedCnt = 0;
                    Log.d(TAG, "set coext mode success, coext mode : " + this.mCoextMode + ", operation : " + operation);
                }
            }
        }
    }

    private void adjustCoextMode() {
        if (this.mWifiNative == null) {
            Log.e(TAG, "adjustCoextMode, mWifiNative is null");
            return;
        }
        int mssValue = this.mWifiNative.getWifiAnt(mIfname, 0);
        int coextMode = transAntReturnValueToCoextMode(mssValue);
        if (mssValue == -1 || coextMode == -1) {
            Log.d(TAG, "get wifi ant failed");
            return;
        }
        this.mCoextMode = coextMode;
        Log.d(TAG, "adjust coext mode to : " + this.mCoextMode);
    }

    private boolean isReachLimitFailedCnt() {
        if (this.mCurrentSsid == null || this.mRecordSsid == null) {
            Log.e(TAG, "isReachLimitFailedCnt, mCurrentSsid or mRecordSsid is null");
            return false;
        }
        Log.d(TAG, "mCurrentSsid is : " + this.mCurrentSsid + ", mRecordSsid is : " + this.mRecordSsid);
        if (this.mRecordSsid.equals(this.mCurrentSsid)) {
            this.mSwitchFailedCnt++;
        }
        this.mRecordSsid = this.mCurrentSsid;
        Log.d(TAG, "mSwitchFailedCnt is : " + this.mSwitchFailedCnt);
        if (this.mSwitchFailedCnt >= 3) {
            return true;
        }
        return false;
    }

    private boolean isAntennaAllowedSwitch(int coextMode) {
        if (this.mMssArbi == null) {
            Log.e(TAG, "isAntennaAllowedSwitch, mMssArbi is null");
            return false;
        } else if (coextMode == 1 && SystemProperties.getInt("runtime.hwmss.blktest", 0) == 0 && this.mMssArbi.isInMSSBlacklist()) {
            Log.d(TAG, "the ap is in black list, do not allowed switch");
            return false;
        } else if (!this.mIsP2pConnected) {
            return true;
        } else {
            Log.d(TAG, "p2p mode, do not allowed switch");
            return false;
        }
    }

    public boolean isAntennaSwitchSuccess(int coextMode) {
        if (this.mWifiNative == null) {
            Log.e(TAG, "isAntennaSwitchSuccess, mWifiNative is null");
            return false;
        } else if (1 == SystemProperties.getInt("runtime.hwmss.errtest", 0)) {
            Log.d(TAG, "switch check : error for test");
            return false;
        } else if (!new HwArpUtils(this.mContext).isGateWayReachable(3, 1000)) {
            Log.d(TAG, "gateway verfier fail");
            return false;
        } else {
            int mssValue = this.mWifiNative.getWifiAnt(mIfname, 0);
            if (mssValue != -1 && transAntReturnValueToCoextMode(mssValue) == coextMode) {
                return true;
            }
            Log.d(TAG, "current chain value error, mssValue : " + mssValue);
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
        if (this.mWifiNative == null) {
            Log.e(TAG, "restoreCoextMode, mWifiNative is null");
        } else if (this.mCoextMode != 0) {
            this.mCoextMode = 0;
            if (this.mWifiNative.setWifiAnt(mIfname, 0, 4) == -1) {
                Log.d(TAG, "restore coext mode fail");
            }
        }
    }

    private void restoreChainMask() {
        if (this.mWifiNative == null) {
            Log.e(TAG, "restoreTxRxchain, mWifiNative is null");
            return;
        }
        if (this.mWifiNative.setWifiAnt(mIfname, 0, 4) == -1) {
            Log.d(TAG, "restore Tx Rx chain fail");
        }
    }

    private void restoreBtcMode() {
        if (this.mWifiNative == null) {
            Log.e(TAG, "restoreBtcMode, mWifiNative is null");
            return;
        }
        if (this.mWifiNative.setWifiAnt(mIfname, 0, 5) == -1) {
            Log.d(TAG, "restore btc mode fail");
        }
    }

    /* access modifiers changed from: private */
    public synchronized void checkAndSwitchCoextMode() {
        int coextMode = 0;
        if (this.mIsWiFi2GConnected && appointedA2dpDeviceConnected()) {
            coextMode = 1;
        }
        setCoextMode(coextMode);
    }
}
