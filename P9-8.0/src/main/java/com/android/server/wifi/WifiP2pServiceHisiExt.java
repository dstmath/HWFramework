package com.android.server.wifi;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.HiSiWifiComm;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Build;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.ContextThemeWrapper;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.p2p.WifiP2pNative;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;

public class WifiP2pServiceHisiExt {
    private static int AP_START_BEGIN = 0;
    private static int AP_START_END = 1;
    private static boolean DBG = HWFLOW;
    private static final String HUAWEI_WIFI_1101_P2P = "huawei.android.permission.WIFI_1101_P2P";
    protected static final boolean HWFLOW;
    private static final String TAG = "WifiP2pServiceHisiExt";
    public static final String WIFI_P2P_ENABLE_CHANGED_ACTION = "android.net.wifi.p2p.ENABLE_CHANGED";
    public boolean P2pFindDeviceUpdate = false;
    private boolean isAirplaneRegister = false;
    private IntentFilter mAirplaneStateFilter = new IntentFilter("android.intent.action.AIRPLANE_MODE");
    private final BroadcastReceiver mAirplaneStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.AIRPLANE_MODE".equals(intent.getAction())) {
                WifiP2pServiceHisiExt.this.handleAirplaneStateChanged();
            }
        }
    };
    private int mApFlag = AP_START_END;
    private Context mContext;
    public WifiP2pGroup mGroup;
    private HiSiWifiComm mHiSiWifiComm = null;
    private IntentFilter mIntentFilter;
    private boolean mIsDialogNeedShow;
    private boolean mIsNeedRecoveryWifi = false;
    private boolean mIsStaToP2pDialogExist = false;
    private NetworkInfo mNetworkInfo = null;
    private int mP2pFlag = 0;
    public StateMachine mP2pStateMachine = null;
    private WifiP2pDevice mThisDevice = null;
    private AsyncChannel mWifiChannel = null;
    private WifiManager mWifiManager = null;
    public WifiP2pInfo mWifiP2pInfo;
    private WifiP2pNative mWifiP2pNative = WifiInjector.getInstance().getWifiP2pNative();
    private IntentFilter mWifiStateFilter = new IntentFilter("android.net.wifi.p2p.hisi.SWITCH_TO_P2P_MODE");
    private final BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pServiceHisiExt.DBG) {
                Slog.d(WifiP2pServiceHisiExt.TAG, "onReceive action=" + action);
            }
            if ("android.net.wifi.p2p.hisi.SWITCH_TO_P2P_MODE".equals(action)) {
                WifiP2pServiceHisiExt.this.setWifiP2pEnabled(1);
                WifiP2pServiceHisiExt.this.unregisterWifiStateReceiver();
            }
        }
    };
    private boolean startWifiForP2p = false;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public WifiP2pServiceHisiExt(Context context, WifiP2pDevice thisDevice, AsyncChannel wifiChannel, NetworkInfo networkInfo) {
        this.mContext = context;
        this.mThisDevice = thisDevice;
        this.mWifiChannel = wifiChannel;
        this.mNetworkInfo = networkInfo;
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("hisi_ap_start_begin");
        this.mIntentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        this.mHiSiWifiComm = new HiSiWifiComm(this.mContext);
    }

    public static boolean hisiWifiEnabled() {
        return HiSiWifiComm.hisiWifiEnabled();
    }

    public void setRecoveryWifiFlag(boolean flag) {
        this.mIsNeedRecoveryWifi = flag;
    }

    public void setWifiP2pFlag(int p2pFlag) {
        this.mP2pFlag = p2pFlag;
    }

    public int getWifiP2pFlag() {
        return this.mP2pFlag;
    }

    public boolean isWifiP2pEnabled() {
        if (this.mP2pFlag == 1 || this.mP2pFlag == 2) {
            return true;
        }
        return false;
    }

    private void registerWifiStateReceiver() {
        if (!this.startWifiForP2p) {
            this.mContext.registerReceiver(this.mWifiStateReceiver, this.mWifiStateFilter);
            this.startWifiForP2p = true;
            setWifiEnableForP2p(this.startWifiForP2p);
        }
    }

    private void unregisterWifiStateReceiver() {
        if (this.startWifiForP2p) {
            this.mContext.unregisterReceiver(this.mWifiStateReceiver);
            this.startWifiForP2p = false;
            setWifiEnableForP2p(this.startWifiForP2p);
        }
    }

    private void setWifiEnableForP2p(boolean enable) {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        this.mWifiManager.setWifiEnableForP2p(enable);
    }

    private boolean isAirplaneSensitive() {
        String airplaneModeRadios = Global.getString(this.mContext.getContentResolver(), "airplane_mode_radios");
        if (airplaneModeRadios != null) {
            return airplaneModeRadios.contains("wifi");
        }
        return true;
    }

    private boolean isAirplaneModeOn() {
        return isAirplaneSensitive() && Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    private void handleAirplaneStateChanged() {
        boolean airplaneOn = isAirplaneModeOn();
        if (DBG) {
            Slog.d(TAG, "handleAirplaneStateChanged Airplane:" + airplaneOn);
        }
        if (airplaneOn) {
            this.mIsNeedRecoveryWifi = false;
            Slog.d(TAG, "handleAirplaneStateChanged Airplane is on, set wifi p2p off!");
            setWifiP2pEnabled(0);
        }
    }

    private void registerAirplaneStateReceiver() {
        if (!this.isAirplaneRegister) {
            this.mContext.registerReceiver(this.mAirplaneStateReceiver, this.mAirplaneStateFilter);
            this.isAirplaneRegister = true;
        }
    }

    private void unregisterAirplaneStateReceiver() {
        if (this.isAirplaneRegister) {
            this.mContext.unregisterReceiver(this.mAirplaneStateReceiver);
            this.isAirplaneRegister = false;
        }
    }

    public void showP2pEanbleDialog() {
        if (this.mHiSiWifiComm.getSettingsGlobalIntValue("show_p2p_dialog_flag") == 1) {
            Slog.d(TAG, "do not show dialog!");
            setWifiP2pEnabled(2);
            sendP2pEnableChangedBroadcast();
        } else if (this.mIsStaToP2pDialogExist) {
            Slog.d(TAG, "the dialog already exist return");
        } else {
            Slog.d(TAG, "showP2pEanbleDialog enter");
            Resources r = Resources.getSystem();
            CheckBox checkBox = new CheckBox(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)));
            checkBox.setChecked(false);
            checkBox.setText(r.getString(33685815));
            checkBox.setTextSize(14.0f);
            checkBox.setTextColor(-16777216);
            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    WifiP2pServiceHisiExt.this.mIsDialogNeedShow = isChecked;
                }
            });
            AlertDialog dialog = new Builder(this.mContext, 33947691).setCancelable(false).setTitle(r.getString(17041250)).setMessage(r.getString(17041261)).setView(checkBox).setNegativeButton(r.getString(17039360), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    WifiP2pServiceHisiExt.this.mIsStaToP2pDialogExist = false;
                    Slog.d(WifiP2pServiceHisiExt.TAG, "NegativeButton is click");
                    WifiP2pServiceHisiExt.this.sendP2pStateChangedBroadcast(false);
                }
            }).setPositiveButton(r.getString(17039370), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    WifiP2pServiceHisiExt.this.mIsStaToP2pDialogExist = false;
                    Slog.d(WifiP2pServiceHisiExt.TAG, "PositiveButton is click");
                    WifiP2pServiceHisiExt.this.mHiSiWifiComm.changeShowDialogFlag("show_p2p_dialog_flag", WifiP2pServiceHisiExt.this.mIsDialogNeedShow);
                    WifiP2pServiceHisiExt.this.setWifiP2pEnabled(2);
                    WifiP2pServiceHisiExt.this.sendP2pEnableChangedBroadcast();
                }
            }).create();
            dialog.getWindow().setType(2014);
            dialog.show();
            this.mIsStaToP2pDialogExist = true;
            Slog.d(TAG, "dialog showed");
        }
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", "WifiP2pService");
    }

    private void enforceChangePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", "WifiP2pService");
    }

    private void sendThisDeviceChangedBroadcast() {
        Intent intent = new Intent("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("wifiP2pDevice", new WifiP2pDevice(this.mThisDevice));
        try {
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        } catch (Exception e) {
            Slog.e(TAG, "sendThisDeviceChangedBroadcast exception:" + e);
        }
    }

    private void sendP2pStateChangedBroadcast(boolean enabled) {
        Intent intent = new Intent("android.net.wifi.p2p.STATE_CHANGED");
        intent.addFlags(67108864);
        if (enabled) {
            intent.putExtra("wifi_p2p_state", 2);
        } else {
            intent.putExtra("wifi_p2p_state", 1);
        }
        try {
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        } catch (Exception e) {
            Slog.e(TAG, "sendP2pStateChangedBroadcast exception:" + e);
        }
    }

    private void sendP2pEnableChangedBroadcast() {
        Slog.d(TAG, "sending p2p enable change broadcast");
        Intent intent = new Intent(WIFI_P2P_ENABLE_CHANGED_ACTION);
        intent.addFlags(67108864);
        this.mContext.sendBroadcast(intent, null);
    }

    public void sendP2pNetworkChangedBroadcast() {
        if (DBG) {
            Slog.d(TAG, "sending p2p network changed broadcast,mNetworkInfo is:" + this.mNetworkInfo);
        }
        Intent intent = new Intent("android.net.wifi.p2p.WIFI_P2P_NETWORK_CHANGED_ACTION");
        intent.putExtra("networkInfo", new NetworkInfo(this.mNetworkInfo));
        this.mContext.sendBroadcast(intent);
    }

    private void sendP2pConnectionChangedBroadcast() {
        if (DBG) {
            Slog.d(TAG, "sending p2p connection changed broadcast,mNetworkInfo is " + this.mNetworkInfo);
            Slog.d(TAG, "mWifiP2pInfo is " + this.mWifiP2pInfo);
        }
        Intent intent = new Intent("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        intent.addFlags(603979776);
        intent.putExtra("wifiP2pInfo", new WifiP2pInfo(this.mWifiP2pInfo));
        intent.putExtra("networkInfo", new NetworkInfo(this.mNetworkInfo));
        intent.putExtra("p2pGroupInfo", new WifiP2pGroup(this.mGroup));
        try {
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        } catch (Exception e) {
            Slog.e(TAG, "sendP2pConnectionChangedBroadcast exception:" + e);
        }
        if (this.mWifiChannel != null) {
            this.mWifiChannel.sendMessage(WifiP2pServiceImpl.P2P_CONNECTION_CHANGED, new NetworkInfo(this.mNetworkInfo));
        } else {
            Slog.d(TAG, "mWifiChannel is null.");
        }
    }

    private boolean setAndPersistDeviceName(String devName) {
        if (devName == null) {
            return false;
        }
        if (this.mWifiP2pNative.setP2pDeviceName(devName)) {
            this.mThisDevice.deviceName = devName;
            this.mWifiP2pNative.setP2pSsidPostfix("-" + this.mThisDevice.deviceName);
            Global.putString(this.mContext.getContentResolver(), "wifi_p2p_device_name", devName);
            sendThisDeviceChangedBroadcast();
            return true;
        }
        Slog.d(TAG, "Failed to set device name " + devName);
        return false;
    }

    public boolean setWifiP2pEnabled(int p2pFlag) {
        boolean processed = false;
        if (DBG) {
            Slog.d(TAG, "setWifiP2pEnabled p2pFlag = " + p2pFlag);
        }
        enforceAccessPermission();
        enforceChangePermission();
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        boolean wifiEnable = this.mWifiManager.isWifiEnabled();
        int wifiApEnable = this.mWifiManager.isWifiApEnabled();
        boolean p2pEnable = isWifiP2pEnabled();
        if (DBG) {
            Slog.d(TAG, "wifi enable:" + wifiEnable + ", p2p enable:" + p2pEnable + ", Ap Enabled:" + wifiApEnable + ", startWifiForP2p:" + this.startWifiForP2p);
        }
        int wifiApState = this.mWifiManager.getWifiApState();
        if (DBG) {
            Slog.d(TAG, "wifiApState:" + wifiApState + " ,mApFlag:" + this.mApFlag);
        }
        if (12 == wifiApState) {
            wifiApEnable = 1;
        }
        if (wifiApEnable == 0 && ((1 == p2pFlag || 2 == p2pFlag) && 11 == wifiApState && AP_START_BEGIN == this.mApFlag)) {
            Slog.d(TAG, "AP is starting now,wait for a moment to try again.");
            return false;
        }
        if (p2pFlag == 1) {
            registerAirplaneStateReceiver();
            if (!wifiEnable && (p2pEnable ^ 1) != 0 && (wifiApEnable ^ 1) != 0) {
                registerWifiStateReceiver();
                this.mWifiManager.setWifiEnabled(true);
                processed = true;
            } else if (wifiEnable && (p2pEnable ^ 1) != 0 && this.startWifiForP2p) {
                processed = false;
            } else if ((wifiEnable && (p2pEnable ^ 1) != 0) || (wifiApEnable != 0 && (p2pEnable ^ 1) != 0)) {
                this.mP2pStateMachine.sendMessage(WifiP2pServiceImpl.SHOW_USER_CONFIRM_DIALOG);
                processed = true;
            } else if ((!wifiEnable && p2pEnable) || (wifiApEnable == 0 && p2pEnable)) {
                processed = true;
            }
            if (!processed) {
                setWifiP2pFlag(p2pFlag);
                processed = switchWifiP2pEnabledState(p2pFlag);
            }
        } else if (p2pFlag == 2) {
            registerAirplaneStateReceiver();
            if (wifiEnable && (p2pEnable ^ 1) != 0) {
                this.mIsNeedRecoveryWifi = true;
                this.mWifiManager.disable(-1, null);
                processed = false;
            } else if (wifiApEnable != 0 && (p2pEnable ^ 1) != 0) {
                this.mWifiManager.setWifiApEnabled(null, false);
                registerWifiStateReceiver();
                this.mWifiManager.setWifiEnabled(true);
                processed = true;
            } else if (!(wifiEnable || (p2pEnable ^ 1) == 0)) {
                Slog.d(TAG, "wifi is disabled or enabling ,wo should jump top2p first,so that wifiSettings cann't receiver WIFI_STATE_CHANGED_ACTION broadcast");
                sendP2pEnableChangedBroadcast();
                setWifiP2pEnabled(1);
                processed = true;
            }
            if (!processed) {
                setWifiP2pFlag(p2pFlag);
                processed = switchWifiP2pEnabledState(1);
            }
        } else if (p2pFlag == 3) {
            unregisterAirplaneStateReceiver();
            this.mIsNeedRecoveryWifi = false;
            setWifiP2pFlag(p2pFlag);
            processed = p2pEnable ? switchWifiP2pEnabledState(0) : true;
        } else {
            unregisterAirplaneStateReceiver();
            processed = true;
            if (DBG) {
                Slog.d(TAG, "Disalbe P2P, need recovery WiFi:" + this.mIsNeedRecoveryWifi);
            }
            if (p2pEnable && this.mIsNeedRecoveryWifi) {
                this.mWifiManager.setWifiEnabled(true);
                this.mIsNeedRecoveryWifi = false;
            } else if (p2pEnable) {
                setWifiP2pFlag(p2pFlag);
                processed = switchWifiP2pEnabledState(p2pFlag);
                this.mWifiManager.setWifiEnabled(false);
            }
        }
        if (DBG) {
            Slog.d(TAG, "setWifiP2pEnabled(),return " + processed);
        }
        return processed;
    }

    private String getPersistedDeviceName() {
        String deviceName = Global.getString(this.mContext.getContentResolver(), "wifi_p2p_device_name");
        if (deviceName != null) {
            return deviceName;
        }
        deviceName = SystemProperties.get("ro.config.marketing_name");
        if (!TextUtils.isEmpty(deviceName)) {
            return deviceName;
        }
        String id = Secure.getString(this.mContext.getContentResolver(), "android_id");
        if (id == null || id.length() <= 4) {
            return Build.MODEL;
        }
        return Build.MODEL + "_" + id.substring(0, 4);
    }

    private boolean switchWifiP2pEnabledState(int p2pFlag) {
        boolean flag;
        if (p2pFlag == 0) {
            if (DBG) {
                Slog.d(TAG, "Enable P2p with arguments p2pFlag=" + p2pFlag);
            }
            this.P2pFindDeviceUpdate = false;
            flag = this.mWifiP2pNative.enableP2p(p2pFlag);
            if (DBG) {
                Slog.d(TAG, "Disable P2p result=" + flag);
            }
            sendP2pStateChangedBroadcast(false);
            sendP2pFlagChangedBroadcast(0);
            this.mNetworkInfo.setIsAvailable(false);
            this.mNetworkInfo.setDetailedState(DetailedState.DISCONNECTED, null, null);
            sendP2pNetworkChangedBroadcast();
            return flag;
        }
        disableWifiAndApIfNeed();
        this.P2pFindDeviceUpdate = true;
        if (DBG) {
            Slog.d(TAG, "Save wifi config data");
        }
        this.mWifiP2pNative.saveConfig();
        if (DBG) {
            Slog.d(TAG, "Enable P2p with arguments p2pFlag=" + p2pFlag);
        }
        flag = this.mWifiP2pNative.enableP2p(p2pFlag);
        this.mThisDevice.deviceName = getPersistedDeviceName();
        this.mThisDevice.deviceAddress = this.mWifiP2pNative.p2pGetDeviceAddress();
        if (DBG) {
            Slog.d(TAG, "This P2p DeviceAddress: " + this.mThisDevice.deviceAddress);
        }
        if (DBG) {
            Slog.d(TAG, "This P2p device name: " + this.mThisDevice.deviceName);
        }
        if (this.mThisDevice.deviceName == null || this.mThisDevice.deviceName.length() == 0) {
            setAndPersistDeviceName(this.mThisDevice.deviceAddress);
            if (DBG) {
                Slog.d(TAG, "mThisDevice.deviceName is null,set mThisDevice.deviceName " + this.mThisDevice.deviceName);
            }
        } else {
            this.mWifiP2pNative.setP2pDeviceName(this.mThisDevice.deviceName);
            this.mWifiP2pNative.setP2pSsidPostfix("-" + this.mThisDevice.deviceName);
        }
        this.mWifiP2pNative.setP2pDeviceType(this.mThisDevice.primaryDeviceType);
        if (DBG) {
            Slog.d(TAG, "Save p2p config data");
        }
        this.mWifiP2pNative.saveConfig();
        if (DBG) {
            Slog.d(TAG, "Enable P2p result=" + flag);
        }
        sendP2pStateChangedBroadcast(true);
        sendP2pConnectionChangedBroadcast();
        this.mNetworkInfo.setIsAvailable(true);
        sendP2pNetworkChangedBroadcast();
        sendP2pFlagChangedBroadcast(1);
        return flag;
    }

    private void disableWifiAndApIfNeed() {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        if (DBG) {
            Slog.d(TAG, "disableWifiAndApIfNeed,WiFiEnabled:" + this.mWifiManager.isWifiEnabled() + ", ApEnabled:" + this.mWifiManager.isWifiApEnabled());
        }
        if (this.mWifiManager.isWifiEnabled()) {
            this.mWifiManager.setWifiStateByManual(false);
        }
        if (this.mWifiManager.isWifiApEnabled()) {
            this.mWifiManager.setWifiApStateByManual(false);
        }
    }

    private void sendP2pFlagChangedBroadcast(int enabled) {
        if (DBG) {
            Slog.d(TAG, "sendP2pFlagChangedBroadcast p2p flag broadcast: " + enabled);
        }
        Intent intent = new Intent("android.net.wifi.p2p.WIFI_P2P_FLAG_CHANGED_ACTION");
        intent.putExtra("extra_p2p_flag", enabled);
        this.mContext.sendBroadcast(intent, HUAWEI_WIFI_1101_P2P);
    }
}
