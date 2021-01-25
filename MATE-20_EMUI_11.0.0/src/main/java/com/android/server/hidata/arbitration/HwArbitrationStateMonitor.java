package com.android.server.hidata.arbitration;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.wifi.HwHiLog;
import com.android.internal.telephony.PhoneConstants;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.huawei.android.emcom.EmcomManagerEx;
import huawei.android.net.hwmplink.HwHiDataCommonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HwArbitrationStateMonitor {
    private static final int INVALID_SUB_ID = -1;
    private static final String TAG = "HiData_HwArbitrationStateMonitor";
    private static HwArbitrationStateMonitor mHwArbitrationStateMonitor = null;
    private IntentFilter intentFilter = new IntentFilter();
    private boolean isCurrentDataRoamingState = false;
    private boolean isCurrentDataTechSuitable = false;
    private boolean isMobileConnectState = false;
    AirPlaneModeObserver mAirPlaneModeObserver;
    private BroadcastReceiver mBroadcastReceiver = new StateBroadcastReceiver();
    private ConnectivityManager mCM;
    private Context mContext;
    private int mCurrentActiveNetwork = 802;
    private int mCurrentDataTechType = 0;
    private int mCurrentServiceState = 1;
    private BroadcastReceiver mDsdsReceiver = new BroadcastReceiver() {
        /* class com.android.server.hidata.arbitration.HwArbitrationStateMonitor.AnonymousClass5 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && "com.huawei.action.ACTION_HW_DSDS_MODE_STATE".equals(intent.getAction())) {
                int state = intent.getIntExtra(HwArbitrationDEFS.DSDS_KEY, 0);
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "DSDReceiver:isdsds3.0 = %{public}d", Integer.valueOf(state));
                HwArbitrationFunction.setDsDsState(state);
            }
        }
    };
    private Handler mHandler;
    private ConnectivityManager.NetworkCallback mHwArbitrationNetworkCallback;
    private Map<Integer, SignalStrength> mPhoneSignalStrength = new ArrayMap();
    private Map<Integer, PhoneStateListener> mPhoneStateListeners = new ArrayMap();
    private ContentResolver mResolver;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener = null;
    private SubscriptionManager mSubscriptionManager = null;
    private TelephonyManager mTelephonyManager = null;
    UserDataEnableObserver mUserDataEnableObserver;

    private HwArbitrationStateMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        HwHiDataCommonUtils.logD(TAG, false, "HwArbitrationStateMonitor create success!", new Object[0]);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
    }

    public static HwArbitrationStateMonitor createHwArbitrationStateMonitor(Context context, Handler handler) {
        if (mHwArbitrationStateMonitor == null) {
            mHwArbitrationStateMonitor = new HwArbitrationStateMonitor(context, handler);
        }
        return mHwArbitrationStateMonitor;
    }

    public void startMonitor() {
        registerBroadcastReceiver();
        registerForVpnSettingsChanges();
        registerForSettingsChanges();
        registDatabaseObserver();
        registerSubscriptionsChangedListeners();
        registerNetworkChangeCallback();
        registerForDSDSChanges();
    }

    private void registerBroadcastReceiver() {
        HwArbitrationCommonUtils.logD(TAG, false, "start Monitoring intent", new Object[0]);
        this.intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.intentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED);
        this.intentFilter.addAction(SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE);
        this.intentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        this.intentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
        this.intentFilter.addAction("android.intent.action.SERVICE_STATE");
        this.intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.intentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_ANY_DATA_CONNECTION_STATE_CHANGED);
        this.intentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        this.intentFilter.addAction(HwArbitrationDEFS.ACTION_HiData_DATA_ROVE_IN);
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.intentFilter);
    }

    public void handleTelephonyServiceStateChanged(ServiceState serviceState, int subId) {
        HwArbitrationCommonUtils.logD(TAG, false, "Before getDefaultDataSubscriptionId subId:%{public}d", Integer.valueOf(subId));
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (subId == -1 || subId != defaultDataSubId || serviceState == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "subId == INVALID_SUB_ID, or subId != mDefaultDataSubId, or serviceState == null, just return", new Object[0]);
            return;
        }
        int newServiceState = serviceState.getDataRegState();
        HwArbitrationCommonUtils.logD(TAG, false, "newServiceState: %{public}d, mCurrentServiceState: %{public}d", Integer.valueOf(newServiceState), Integer.valueOf(this.mCurrentServiceState));
        if (this.mCurrentServiceState != newServiceState) {
            this.mCurrentServiceState = newServiceState;
            HwArbitrationFunction.setServiceState(this.mCurrentServiceState);
            if (this.mCurrentServiceState == 0) {
                this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_STATE_IN_SERVICE);
            } else {
                this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_STATE_OUT_OF_SERVICE);
            }
        }
        boolean isRoamingState = serviceState.getDataRoaming();
        HwArbitrationCommonUtils.logD(TAG, false, "newRoammingState :%{public}s", String.valueOf(isRoamingState));
        if (this.isCurrentDataRoamingState != isRoamingState) {
            this.isCurrentDataRoamingState = isRoamingState;
            HwArbitrationFunction.setDataRoamingState(this.isCurrentDataRoamingState);
            if (this.isCurrentDataRoamingState) {
                this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_STATE_IS_ROAMING);
            } else {
                this.mHandler.sendEmptyMessage(1021);
            }
        }
        int newDataTechType = serviceState.getDataNetworkType();
        HwArbitrationCommonUtils.logD(TAG, false, "newDataTechType:%{public}d", Integer.valueOf(newDataTechType));
        if (this.mCurrentDataTechType != newDataTechType) {
            this.mCurrentDataTechType = newDataTechType;
            handleDataTechTypeChange(newDataTechType);
        }
    }

    private void handleDataTechTypeChange(int dataTech) {
        HwArbitrationCommonUtils.logI(TAG, false, "handlerDataTechTypeChange dataTech :%{public}d", Integer.valueOf(dataTech));
        HwArbitrationFunction.setDataTech(dataTech);
        boolean isNewDataTechSuitable = false;
        if (dataTech == 13 || dataTech == 19 || dataTech == 20) {
            isNewDataTechSuitable = true;
        }
        if (this.isCurrentDataTechSuitable != isNewDataTechSuitable) {
            this.isCurrentDataTechSuitable = isNewDataTechSuitable;
            HwArbitrationFunction.setDataTechSuitable(isNewDataTechSuitable);
            if (isNewDataTechSuitable) {
                this.mHandler.sendEmptyMessage(1022);
                HwArbitrationCommonUtils.logD(TAG, false, "MSG_DATA_TECHTYPE_SUITABLE", new Object[0]);
                return;
            }
            this.mHandler.sendEmptyMessage(1023);
            HwArbitrationCommonUtils.logD(TAG, false, "MSG_DATA_TECHTYPE_NOT_SUITABLE", new Object[0]);
        }
    }

    private class StateBroadcastReceiver extends BroadcastReceiver {
        private NetworkInfo mActiveNetworkInfo;
        private PhoneConstants.DataState[] mApnState;
        private int mConnectivityType;
        private boolean[] mFristTimeRecvApnStateFlag;
        private boolean mWifiConnectState;

        private StateBroadcastReceiver() {
            this.mWifiConnectState = false;
            this.mConnectivityType = 802;
            this.mFristTimeRecvApnStateFlag = new boolean[]{false, false};
            this.mApnState = new PhoneConstants.DataState[2];
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwArbitrationCommonUtils.logE(HwArbitrationStateMonitor.TAG, false, "received intent is null, return", new Object[0]);
                return;
            }
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                int wifistate = intent.getIntExtra("wifi_state", 4);
                if (wifistate == 1) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "MSG_WIFI_STATE_DISABLE", new Object[0]);
                } else if (wifistate == 3) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "MSG_WIFI_STATE_ENABLE", new Object[0]);
                }
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED.equals(action)) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo == null) {
                    return;
                }
                if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "WifiManager:Wifi disconnected", new Object[0]);
                    if (this.mWifiConnectState) {
                        this.mWifiConnectState = false;
                    }
                } else if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "WifiManager:Wifi connected", new Object[0]);
                    if (!this.mWifiConnectState) {
                        this.mWifiConnectState = true;
                    }
                }
            } else if (SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE.equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "connectivity Changed", new Object[0]);
                onConnectivityNetworkChange();
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "MSG_SCREEN_IS_TURNOFF", new Object[0]);
                HwArbitrationFunction.setScreenState(false);
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_SCREEN_IS_TURNOFF);
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "MSG_SCREEN_IS_ON", new Object[0]);
                HwArbitrationFunction.setScreenState(true);
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_SCREEN_IS_ON);
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_ANY_DATA_CONNECTION_STATE_CHANGED.equals(action)) {
                logD(HwArbitrationStateMonitor.TAG, false, "onReceive: ACTION_ANY_DATA_CONNECTION_STATE_CHANGED", new Object[0]);
                handleTelephonyDataConnectionChanged(intent.getStringExtra("state"), intent.getIntExtra("subscription", -1));
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_DEFAULT_DATA_SUBSCRIPTION_CHANGED.equals(action)) {
                handleDataSubChange(intent.getIntExtra("subscription", -1));
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "BOOT_COMPLETED", new Object[0]);
                if (HwArbitrationStateMonitor.this.mHwArbitrationNetworkCallback == null) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "registerDefaultNetworkCallback have not been registered", new Object[0]);
                    HwArbitrationStateMonitor.this.registerNetworkChangeCallback();
                }
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_DEVICE_BOOT_COMPLETED);
            } else if (HwArbitrationDEFS.ACTION_HiData_DATA_ROVE_IN.equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "Big Mobile Data Alert trigger closing MPLink", new Object[0]);
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_Stop_MPLink_By_Notification);
                EmcomManagerEx.getInstance();
                EmcomManagerEx.notifySmartMp(0);
            }
        }

        private void onConnectivityNetworkChange() {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) HwArbitrationStateMonitor.this.mContext.getSystemService("connectivity");
            if (mConnectivityManager != null) {
                this.mActiveNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                NetworkInfo networkInfo = this.mActiveNetworkInfo;
                int i = 801;
                if (networkInfo == null) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange-prev_type is:%{public}d", Integer.valueOf(this.mConnectivityType));
                    int i2 = this.mConnectivityType;
                    if (800 == i2) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange, MSG_WIFI_STATE_DISCONNECT", new Object[0]);
                        if (HwArbitrationStateMonitor.this.mCurrentActiveNetwork != 802) {
                            HwArbitrationStateMonitor hwArbitrationStateMonitor = HwArbitrationStateMonitor.this;
                            if (!hwArbitrationStateMonitor.isMobileConnectState) {
                                i = 802;
                            }
                            hwArbitrationStateMonitor.mCurrentActiveNetwork = i;
                            HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(HwArbitrationDEFS.MSG_NOTIFY_CURRENT_NETWORK, HwArbitrationStateMonitor.this.mCurrentActiveNetwork, 0));
                        }
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(1006);
                    } else if (801 == i2) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange, MSG_CELL_STATE_DISCONNECT", new Object[0]);
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT);
                    }
                    this.mConnectivityType = 802;
                } else if (1 == networkInfo.getType()) {
                    if (this.mActiveNetworkInfo.isConnected()) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange, MSG_WIFI_STATE_CONNECTED", new Object[0]);
                        if (800 != HwArbitrationStateMonitor.this.mCurrentActiveNetwork) {
                            HwArbitrationStateMonitor.this.mCurrentActiveNetwork = 800;
                            HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(HwArbitrationDEFS.MSG_NOTIFY_CURRENT_NETWORK, HwArbitrationStateMonitor.this.mCurrentActiveNetwork, 0));
                        }
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(1005);
                        this.mConnectivityType = 800;
                    } else {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange-Wifi:%{public}d", this.mActiveNetworkInfo.getState());
                    }
                } else if (this.mActiveNetworkInfo.getType() == 0) {
                    if (this.mActiveNetworkInfo.isConnected()) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange, MSG_CELL_STATE_CONNECTED", new Object[0]);
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_CELL_STATE_CONNECTED);
                        this.mConnectivityType = 801;
                    } else {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange-Cell:%{public}d", this.mActiveNetworkInfo.getState());
                    }
                }
                HwAppTimeDetail.getInstance().notifyNetworkChange(this.mConnectivityType);
            }
        }

        public PhoneConstants.DataState getApnState(int subId) {
            return this.mApnState[subId];
        }

        private void onDataConnected(int slotId) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onDataConnected Enter, slotId is %{public}d", Integer.valueOf(slotId));
            HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(0, slotId, 0));
        }

        private void onDataConnectionDisconnected(PhoneConstants.DataState oldMobileDataState, int slotId) {
            if (PhoneConstants.DataState.CONNECTED.equals(oldMobileDataState) || PhoneConstants.DataState.SUSPENDED.equals(oldMobileDataState)) {
                logD(HwArbitrationStateMonitor.TAG, false, "onDataConnectionDisconnected: EVENT_DEFAULT_DATA_DISCONNECTED_FAILURE on slotId %{public}d", Integer.valueOf(slotId));
                HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(2, slotId, 0));
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "MSG_DEFAULT_DATA_DISCONNECTED", new Object[0]);
                return;
            }
            logD(HwArbitrationStateMonitor.TAG, false, "onDataConnectionDisconnected: EVENT_DEFAULT_DATA_SETUP_FAILURE on slotId %{public}d", Integer.valueOf(slotId));
            HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(3, slotId, 0));
        }

        private boolean isDefaultApnType(String apnType) {
            return AppActConstant.VALUE_DEFAULT.equals(apnType);
        }

        private void onReceiveDataStateChanged(Intent intent) {
            String apnType = intent.getStringExtra("apnType");
            if (isDefaultApnType(apnType)) {
                int slotId = intent.getIntExtra("subscription", -1);
                PhoneConstants.DataState state = Enum.valueOf(PhoneConstants.DataState.class, intent.getStringExtra("state"));
                if (!isSlotIdValid(slotId)) {
                    logE(HwArbitrationStateMonitor.TAG, false, "onReceiveStateChanged: param is invalid,slotId = %{public}d,state = %{public}d", Integer.valueOf(slotId), state);
                    return;
                }
                logD(HwArbitrationStateMonitor.TAG, false, "onReceiveStateChanged: slotId = %{public}d,state = %{public}d,apnType = %{public}s", Integer.valueOf(slotId), state, apnType);
                if (!this.mFristTimeRecvApnStateFlag[slotId]) {
                    logD(HwArbitrationStateMonitor.TAG, false, "mFristTimeRecvApnStateFlag ", new Object[0]);
                    this.mApnState[slotId] = state;
                    this.mFristTimeRecvApnStateFlag[slotId] = true;
                } else if (!state.equals(this.mApnState[slotId])) {
                    logD(HwArbitrationStateMonitor.TAG, false, "apnState changed,oldMobileDataState = %{public}d,state = %{public}d", this.mApnState[slotId], state);
                    int i = AnonymousClass6.$SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[state.ordinal()];
                    if (i == 1) {
                        onDataConnectionDisconnected(this.mApnState[slotId], slotId);
                    } else if (i == 2) {
                        onDataConnected(slotId);
                    }
                    this.mApnState[slotId] = state;
                }
            }
        }

        public boolean isSlotIdValid(int slotId) {
            return slotId >= 0 && 2 > slotId;
        }

        private void handleDataSubChange(int subId) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "DataSub Change, new subId:%{public}d", Integer.valueOf(subId));
            if (subId != -1) {
                HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(1024, subId, 0));
            }
        }

        private void handleTelephonyDataConnectionChanged(String state, int subId) {
            HwArbitrationCommonUtils.logI(HwArbitrationStateMonitor.TAG, false, "ACTION_ANY_DATA_CONNECTION_STATE_CHANGED subId:%{public}d,state:%{public}s", Integer.valueOf(subId), state);
            if (subId != SubscriptionManager.getDefaultDataSubscriptionId()) {
                return;
            }
            if ("CONNECTED".equals(state)) {
                HwArbitrationStateMonitor.this.isMobileConnectState = true;
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "mobile_data_connected", new Object[0]);
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(6);
                if (802 == HwArbitrationStateMonitor.this.mCurrentActiveNetwork) {
                    HwArbitrationStateMonitor.this.mCurrentActiveNetwork = 801;
                    HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(HwArbitrationDEFS.MSG_NOTIFY_CURRENT_NETWORK, HwArbitrationStateMonitor.this.mCurrentActiveNetwork, 0));
                }
            } else if ("DISCONNECTED".equals(state)) {
                HwArbitrationStateMonitor.this.isMobileConnectState = false;
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "mobile_data_disconnected", new Object[0]);
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(5);
                if (801 == HwArbitrationStateMonitor.this.mCurrentActiveNetwork) {
                    HwArbitrationStateMonitor.this.mCurrentActiveNetwork = 802;
                    HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(HwArbitrationDEFS.MSG_NOTIFY_CURRENT_NETWORK, HwArbitrationStateMonitor.this.mCurrentActiveNetwork, 0));
                }
            }
        }

        private void logD(String TAG, boolean isFmtStrPrivate, String debugInfo, Object... args) {
            HwHiLog.d(TAG, isFmtStrPrivate, debugInfo, args);
        }

        private void logE(String TAG, boolean isFmtStrPrivate, String debugInfo, Object... args) {
            HwHiLog.e(TAG, isFmtStrPrivate, debugInfo, args);
        }
    }

    /* renamed from: com.android.server.hidata.arbitration.HwArbitrationStateMonitor$6  reason: invalid class name */
    static /* synthetic */ class AnonymousClass6 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState = new int[PhoneConstants.DataState.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[PhoneConstants.DataState.DISCONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[PhoneConstants.DataState.CONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    private void registerForVpnSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("wifipro_network_vpn_state"), false, new ContentObserver(null) {
            /* class com.android.server.hidata.arbitration.HwArbitrationStateMonitor.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                if (HwArbitrationStateMonitor.getSettingsSystemBoolean(HwArbitrationStateMonitor.this.mContext.getContentResolver(), "wifipro_network_vpn_state", false)) {
                    HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_VPN_STATE_OPEN);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "registerForVpnSettingsChanges VPN Open", new Object[0]);
                    return;
                }
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_VPN_STATE_CLOSE);
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "registerForVpnSettingsChanges VPN Close", new Object[0]);
            }
        });
    }

    private void registerForSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("smart_network_switching"), false, new ContentObserver(new Handler()) {
            /* class com.android.server.hidata.arbitration.HwArbitrationStateMonitor.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                boolean isWiFiProEnabled = HwArbitrationStateMonitor.getSettingsSystemBoolean(HwArbitrationStateMonitor.this.mContext.getContentResolver(), "smart_network_switching", false);
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "Wifi pro setting has changed, WiFiProEnabled == %{public}s", String.valueOf(isWiFiProEnabled));
                if (isWiFiProEnabled) {
                    HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_WIFI_PLUS_ENABLE);
                } else {
                    HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_WIFI_PLUS_DISABLE);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public static boolean getSettingsSystemBoolean(ContentResolver cr, String name, boolean def) {
        return Settings.System.getInt(cr, name, def ? 1 : 0) == 1;
    }

    private void registDatabaseObserver() {
        observeAirplaneMode();
        observeUserDataEnableStatus();
    }

    private void observeAirplaneMode() {
        this.mAirPlaneModeObserver = new AirPlaneModeObserver(this.mHandler);
        this.mAirPlaneModeObserver.register();
    }

    public boolean isAirplaneModeOn() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    /* access modifiers changed from: private */
    public class AirPlaneModeObserver extends ContentObserver {
        public AirPlaneModeObserver(Handler handler) {
            super(handler);
            HwArbitrationStateMonitor.this.mResolver = HwArbitrationStateMonitor.this.mContext.getContentResolver();
        }

        public void register() {
            HwArbitrationStateMonitor.this.mResolver.registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), false, this);
        }

        public void unregister() {
            HwArbitrationStateMonitor.this.mResolver.unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            boolean airplaneMode = HwArbitrationStateMonitor.this.isAirplaneModeOn();
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "AirPlaneMode change to %{public}s", String.valueOf(airplaneMode));
            if (airplaneMode) {
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_AIRPLANE_MODE_ON);
            } else {
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_AIRPLANE_MODE_OFF);
            }
        }
    }

    private void observeUserDataEnableStatus() {
        this.mUserDataEnableObserver = new UserDataEnableObserver(this.mHandler);
        this.mUserDataEnableObserver.register();
    }

    public boolean isUserDataEnabled() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data", 0) != 0;
    }

    /* access modifiers changed from: private */
    public class UserDataEnableObserver extends ContentObserver {
        public UserDataEnableObserver(Handler handler) {
            super(handler);
            HwArbitrationStateMonitor.this.mResolver = HwArbitrationStateMonitor.this.mContext.getContentResolver();
        }

        public void register() {
            HwArbitrationStateMonitor.this.mResolver.registerContentObserver(Settings.Global.getUriFor("mobile_data"), false, this);
        }

        public void unregister() {
            HwArbitrationStateMonitor.this.mResolver.unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            boolean state = HwArbitrationStateMonitor.this.isUserDataEnabled();
            if (state) {
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_CELL_STATE_ENABLE);
            } else {
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_CELL_STATE_DISABLE);
            }
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "User change Data service state = %{public}s", String.valueOf(state));
        }
    }

    private void registerSubscriptionsChangedListeners() {
        if (this.mSubscriptionManager == null) {
            HwArbitrationCommonUtils.logE(TAG, false, "registerSubscriptionsChangedListeners: mSubscriptionManager is null, return!", new Object[0]);
            return;
        }
        if (this.mSubscriptionListener == null) {
            this.mSubscriptionListener = new SubscriptionListener();
        }
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
    }

    /* access modifiers changed from: private */
    public class SubscriptionListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        private SubscriptionListener() {
        }

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            if (HwArbitrationStateMonitor.this.mPhoneStateListeners == null) {
                HwArbitrationCommonUtils.logE(HwArbitrationStateMonitor.TAG, false, "onSubscriptionsChanged failed, mPhoneStateListeners is null", new Object[0]);
                return;
            }
            List<SubscriptionInfo> subInfos = HwArbitrationStateMonitor.this.mSubscriptionManager.getActiveSubscriptionInfoList();
            List<Integer> subIdList = new ArrayList<>(HwArbitrationStateMonitor.this.mPhoneStateListeners.keySet());
            for (int subIdCounter = subIdList.size() - 1; subIdCounter >= 0; subIdCounter--) {
                int subId = subIdList.get(subIdCounter).intValue();
                if (!HwArbitrationStateMonitor.this.containSubId(subInfos, subId)) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onSubscriptionsChanged, remove deactive subId listener:%{public}d", Integer.valueOf(subId));
                    HwArbitrationStateMonitor.this.mTelephonyManager.listen((PhoneStateListener) HwArbitrationStateMonitor.this.mPhoneStateListeners.get(Integer.valueOf(subId)), 0);
                    HwArbitrationStateMonitor.this.mPhoneStateListeners.remove(Integer.valueOf(subId));
                    HwArbitrationStateMonitor.this.mPhoneSignalStrength.remove(Integer.valueOf(subId));
                }
            }
            if (subInfos == null) {
                HwArbitrationCommonUtils.logE(HwArbitrationStateMonitor.TAG, false, "onSubscriptionsChanged, subscriptions is null", new Object[0]);
                return;
            }
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onSubscriptionsChanged num:" + subInfos.size(), new Object[0]);
            for (SubscriptionInfo subInfo : subInfos) {
                int subId2 = subInfo.getSubscriptionId();
                if (!HwArbitrationCommonUtils.isActiveSubId(subId2)) {
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMonitor.TAG, false, "onSubscriptionsChanged failed, invalid subId:%{public}d, check next subId", Integer.valueOf(subId2));
                } else if (!HwArbitrationStateMonitor.this.mPhoneStateListeners.containsKey(Integer.valueOf(subId2))) {
                    PhoneStateListener listener = HwArbitrationStateMonitor.this.getPhoneStateListener(subId2);
                    HwArbitrationStateMonitor.this.mTelephonyManager.listen(listener, 289);
                    HwArbitrationStateMonitor.this.mPhoneStateListeners.put(Integer.valueOf(subId2), listener);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean containSubId(List<SubscriptionInfo> subInfos, int subId) {
        if (subInfos == null) {
            HwArbitrationCommonUtils.logE(TAG, false, "containSubId fail, subInfos is null", new Object[0]);
            return false;
        } else if (!HwArbitrationCommonUtils.isSubIdValid(subId)) {
            HwArbitrationCommonUtils.logE(TAG, false, "containSubId fail, invalid subId:%{public}d", Integer.valueOf(subId));
            return false;
        } else {
            for (SubscriptionInfo subInfo : subInfos) {
                if (subInfo.getSubscriptionId() == subId) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private PhoneStateListener getPhoneStateListener(int i) {
        return new PhoneStateListener(Integer.valueOf(i)) {
            /* class com.android.server.hidata.arbitration.HwArbitrationStateMonitor.AnonymousClass3 */

            @Override // android.telephony.PhoneStateListener
            public void onServiceStateChanged(ServiceState state) {
                if (state == null) {
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMonitor.TAG, false, "SlotStateListener onServiceStateChanged: state is null,return", new Object[0]);
                } else if (!HwArbitrationCommonUtils.isSubIdValid(this.mSubId.intValue())) {
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMonitor.TAG, false, "SlotStateListener onServiceStateChanged: invalid mSubId = %{public}d", this.mSubId);
                } else {
                    HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(4, this.mSubId.intValue(), 0, state));
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "SlotStateListener onServiceStateChanged on mSubId = %{public}d, NetworkType is: %{public}d", this.mSubId, Integer.valueOf(state.getDataNetworkType()));
                    HwArbitrationStateMonitor.this.handleTelephonyServiceStateChanged(state, this.mSubId.intValue());
                }
            }

            @Override // android.telephony.PhoneStateListener
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                if (!HwArbitrationCommonUtils.isSubIdValid(this.mSubId.intValue())) {
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMonitor.TAG, false, "SlotStateListener onCallStateChanged: invalid mSubId = %{public}d", this.mSubId);
                } else if (state == 0) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "CALL_STATE_IDLE:mSubId:%{public}d", this.mSubId);
                    HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(7, this.mSubId.intValue(), 0));
                } else if (state == 1 || state == 2) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "CALL_STATE_OFFHOOK or CALL_STATE_RINGING:mSubId:%{public}d", this.mSubId);
                    HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(8, this.mSubId.intValue(), 0));
                }
            }

            @Override // android.telephony.PhoneStateListener
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                if (signalStrength == null) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onSignalStrengthsChanged is null", new Object[0]);
                }
                if (HwArbitrationStateMonitor.this.mPhoneSignalStrength.containsKey(this.mSubId)) {
                    HwArbitrationStateMonitor.this.mPhoneSignalStrength.remove(this.mSubId);
                }
                HwArbitrationStateMonitor.this.mPhoneSignalStrength.put(this.mSubId, signalStrength);
            }
        };
    }

    public SignalStrength getSignalStrength(int subId) {
        HwArbitrationCommonUtils.logD(TAG, false, "getSignalStrength:" + subId, new Object[0]);
        if (this.mPhoneSignalStrength.containsKey(Integer.valueOf(subId))) {
            return this.mPhoneSignalStrength.get(Integer.valueOf(subId));
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerNetworkChangeCallback() {
        this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (this.mCM != null) {
            this.mHwArbitrationNetworkCallback = new ConnectivityManager.NetworkCallback() {
                /* class com.android.server.hidata.arbitration.HwArbitrationStateMonitor.AnonymousClass4 */
                private int mActiveNetworkType = 802;
                private Network mDefaultNetwork;
                private Network mLastNetwork;
                private NetworkCapabilities mLastNetworkCapabilities;

                @Override // android.net.ConnectivityManager.NetworkCallback
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "ConnectivityManager.NetworkCallback : onCapabilitiesChanged", new Object[0]);
                    if (network != null && networkCapabilities != null) {
                        NetworkCapabilities networkCapabilities2 = this.mLastNetworkCapabilities;
                        boolean lastValidated = networkCapabilities2 != null && networkCapabilities2.hasCapability(16);
                        boolean validated = networkCapabilities.hasCapability(16);
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "network:%{public}s, lastValidated:%{public}s, validated:%{public}s", network.toString(), String.valueOf(lastValidated), String.valueOf(validated));
                        NetworkCapabilities networkCapabilities3 = this.mLastNetworkCapabilities;
                        boolean isSameNetworkCapabilities = networkCapabilities3 != null && networkCapabilities.equalsTransportTypes(networkCapabilities3);
                        if (!network.equals(this.mLastNetwork) || !isSameNetworkCapabilities || validated != lastValidated) {
                            this.mLastNetwork = network;
                            this.mLastNetworkCapabilities = networkCapabilities;
                            if (networkCapabilities.hasTransport(1) && validated) {
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "networkType:TRANSPORT_WIFI, network: %{public}s", network.toString());
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onNetworkCallback, MSG_WIFI_STATE_CONNECTED", new Object[0]);
                                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(1005);
                                this.mDefaultNetwork = network;
                                this.mActiveNetworkType = 800;
                            } else if (networkCapabilities.hasTransport(0) && validated) {
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "networkType:TRANSPORT_CELLULAR, network: %{public}s", network.toString());
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onNetworkCallback, MSG_CELL_STATE_CONNECTED", new Object[0]);
                                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_CELL_STATE_CONNECTED);
                                this.mDefaultNetwork = network;
                                this.mActiveNetworkType = 801;
                            }
                        }
                    }
                }

                @Override // android.net.ConnectivityManager.NetworkCallback
                public void onLost(Network network) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onNetworkCallback:onLost", new Object[0]);
                    if (network != null) {
                        if (!network.equals(this.mDefaultNetwork)) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "lost network not equal to defaultNetwork", new Object[0]);
                            return;
                        }
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onNetworkCallback-prev_type is:%{public}d, network:%{public}s", Integer.valueOf(this.mActiveNetworkType), network.toString());
                        int i = this.mActiveNetworkType;
                        if (801 == i) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onNetworkCallback:MSG_CELL_STATE_DISCONNECT", new Object[0]);
                            HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT);
                        } else if (800 == i) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onNetworkCallback:MSG_WIFI_STATE_DISCONNECT", new Object[0]);
                            HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(1006);
                        }
                        this.mActiveNetworkType = 802;
                    }
                }
            };
            this.mCM.registerDefaultNetworkCallback(this.mHwArbitrationNetworkCallback, this.mHandler);
        }
    }

    private void registerForDSDSChanges() {
        IntentFilter dsdsFilter = new IntentFilter();
        dsdsFilter.addAction("com.huawei.action.ACTION_HW_DSDS_MODE_STATE");
        this.mContext.registerReceiver(this.mDsdsReceiver, dsdsFilter, HwArbitrationDEFS.DSDSMODE_PERMISSION, this.mHandler);
    }
}
