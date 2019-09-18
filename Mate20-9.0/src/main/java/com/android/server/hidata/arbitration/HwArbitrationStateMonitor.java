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
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;
import com.android.server.hidata.hiradio.HwHiRadioBoost;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.android.emcom.EmcomManagerEx;
import huawei.android.net.hwmplink.HwHiDataCommonUtils;

public class HwArbitrationStateMonitor {
    private static final String TAG = "HiData_HwArbitrationStateMonitor";
    private static HwArbitrationStateMonitor mHwArbitrationStateMonitor = null;
    private IntentFilter intentFilter = new IntentFilter();
    AirPlaneModeObserver mAirPlaneModeObserver;
    private BroadcastReceiver mBroadcastReceiver = new StateBroadcastReceiver();
    private ConnectivityManager mCM;
    /* access modifiers changed from: private */
    public Context mContext;
    private BroadcastReceiver mDsdsReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getAction() == null || !HwArbitrationDEFS.ACTION_HW_DSDS_MODE_STATE.equals(intent.getAction()))) {
                int state = intent.getIntExtra(HwArbitrationDEFS.DSDS_KEY, 0);
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "DSDReceiver:isdsds3.0 = " + state);
                HwArbitrationFunction.setDsDsState(state);
            }
        }
    };
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public ConnectivityManager.NetworkCallback mHwArbitrationNetworkCallback;
    private PhoneStateListener[] mPhoneStateListener;
    /* access modifiers changed from: private */
    public ContentResolver mResolver;
    UserDataEnableObserver mUserDataEnableObserver;

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

    private class AirPlaneModeObserver extends ContentObserver {
        public AirPlaneModeObserver(Handler handler) {
            super(handler);
            ContentResolver unused = HwArbitrationStateMonitor.this.mResolver = HwArbitrationStateMonitor.this.mContext.getContentResolver();
        }

        public void register() {
            HwArbitrationStateMonitor.this.mResolver.registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), false, this);
        }

        public void unregister() {
            HwArbitrationStateMonitor.this.mResolver.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            boolean airplaneMode = HwArbitrationStateMonitor.this.isAirplaneModeOn();
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "AirPlaneMode change to " + airplaneMode);
            if (airplaneMode) {
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_AIRPLANE_MODE_ON);
            } else {
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_AIRPLANE_MODE_OFF);
            }
        }
    }

    private class StateBroadcastReceiver extends BroadcastReceiver {
        private NetworkInfo mActiveNetworkInfo;
        private PhoneConstants.DataState[] mApnState;
        private int mConnectivityType;
        private int mCurrentActiveNetwork;
        private boolean mCurrentDataRoamingState;
        private int mCurrentDataTechType;
        private int mCurrentServiceState;
        private int mDefaultDataSubId;
        private boolean[] mFristTimeRecvApnStateFlag;
        private boolean mIsDataTechSuitable;
        private boolean mMobileConnectState;
        private boolean mWifiConnectState;

        private StateBroadcastReceiver() {
            this.mCurrentActiveNetwork = 802;
            this.mCurrentServiceState = 1;
            this.mCurrentDataTechType = 0;
            this.mIsDataTechSuitable = false;
            this.mCurrentDataRoamingState = false;
            this.mDefaultDataSubId = 0;
            this.mMobileConnectState = false;
            this.mWifiConnectState = false;
            this.mConnectivityType = 802;
            this.mFristTimeRecvApnStateFlag = new boolean[]{false, false};
            this.mApnState = new PhoneConstants.DataState[2];
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                int wifistate = intent.getIntExtra("wifi_state", 4);
                if (wifistate == 1) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "MSG_WIFI_STATE_DISABLE");
                } else if (wifistate == 3) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "MSG_WIFI_STATE_ENABLE");
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo == null) {
                    return;
                }
                if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "WifiManager:Wifi disconnected");
                    if (this.mWifiConnectState) {
                        this.mWifiConnectState = false;
                    }
                } else if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "WifiManager:Wifi connected");
                    if (!this.mWifiConnectState) {
                        this.mWifiConnectState = true;
                    }
                }
            } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "connectivity Changed");
                onConnectivityNetworkChange();
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "MSG_SCREEN_IS_TURNOFF");
                HwArbitrationFunction.setScreenState(false);
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_SCREEN_IS_TURNOFF);
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "MSG_SCREEN_IS_ON");
                HwArbitrationFunction.setScreenState(true);
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_SCREEN_IS_ON);
            } else if ("android.intent.action.SERVICE_STATE".equals(action)) {
                handleTelephonyServiceStateChanged(ServiceState.newFromBundle(intent.getExtras()), intent.getIntExtra("subscription", -1));
            } else if ("android.intent.action.ANY_DATA_STATE".equals(action)) {
                logD(HwArbitrationStateMonitor.TAG, "onReceive: ACTION_ANY_DATA_CONNECTION_STATE_CHANGED");
                handleTelephonyDataConnectionChanged(intent.getStringExtra("state"), intent.getIntExtra("subscription", -1));
            } else if ("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED".equals(action)) {
                handleDataSubChange(intent.getIntExtra("subscription", -1));
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "BOOT_COMPLETED");
                if (HwArbitrationStateMonitor.this.mHwArbitrationNetworkCallback == null) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "registerDefaultNetworkCallback have not been registered");
                    HwArbitrationStateMonitor.this.registerNetworkChangeCallback();
                }
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_DEVICE_BOOT_COMPLETED);
            } else if (HwArbitrationDEFS.ACTION_HiData_DATA_ROVE_IN.equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "Big Mobile Data Alert trigger closing MPLink");
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_Stop_MPLink_By_Notification);
                EmcomManagerEx.getInstance();
                EmcomManagerEx.notifySmartMp(0);
            }
        }

        private void onConnectivityNetworkChange() {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) HwArbitrationStateMonitor.this.mContext.getSystemService("connectivity");
            if (mConnectivityManager != null) {
                this.mActiveNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                int i = 801;
                if (this.mActiveNetworkInfo == null) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onConnectivityNetworkChange-prev_type is:" + this.mConnectivityType);
                    if (800 == this.mConnectivityType) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onConnectivityNetworkChange, MSG_WIFI_STATE_DISCONNECT");
                        if (802 != this.mCurrentActiveNetwork) {
                            if (!this.mMobileConnectState) {
                                i = 802;
                            }
                            this.mCurrentActiveNetwork = i;
                            HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(HwArbitrationDEFS.MSG_NOTIFY_CURRENT_NETWORK, this.mCurrentActiveNetwork, 0));
                        }
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_WIFI_STATE_DISCONNECT);
                    } else if (801 == this.mConnectivityType) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onConnectivityNetworkChange, MSG_CELL_STATE_DISCONNECT");
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT);
                    }
                    this.mConnectivityType = 802;
                } else if (1 == this.mActiveNetworkInfo.getType()) {
                    if (this.mActiveNetworkInfo.isConnected()) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onConnectivityNetworkChange, MSG_WIFI_STATE_CONNECTED");
                        if (800 != this.mCurrentActiveNetwork) {
                            this.mCurrentActiveNetwork = 800;
                            HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(HwArbitrationDEFS.MSG_NOTIFY_CURRENT_NETWORK, this.mCurrentActiveNetwork, 0));
                        }
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(1005);
                        this.mConnectivityType = 800;
                    } else {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onConnectivityNetworkChange-Wifi:" + this.mActiveNetworkInfo.getState());
                    }
                } else if (this.mActiveNetworkInfo.getType() == 0) {
                    if (this.mActiveNetworkInfo.isConnected()) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onConnectivityNetworkChange, MSG_CELL_STATE_CONNECTED");
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_CELL_STATE_CONNECTED);
                        this.mConnectivityType = 801;
                    } else {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onConnectivityNetworkChange-Cell:" + this.mActiveNetworkInfo.getState());
                    }
                }
            }
        }

        public PhoneConstants.DataState getApnState(int subId) {
            return this.mApnState[subId];
        }

        private void onDataConnected(int slotId) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onDataConnected Enter, slotId is " + slotId);
            HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(0, slotId, 0));
        }

        private void onDataConnectionDisconnected(PhoneConstants.DataState oldMobileDataState, int slotId) {
            if (PhoneConstants.DataState.CONNECTED.equals(oldMobileDataState) || PhoneConstants.DataState.SUSPENDED.equals(oldMobileDataState)) {
                logD(HwArbitrationStateMonitor.TAG, "onDataConnectionDisconnected: EVENT_DEFAULT_DATA_DISCONNECTED_FAILURE on slotId " + slotId);
                HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(2, slotId, 0));
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "MSG_DEFAULT_DATA_DISCONNECTED");
                return;
            }
            logD(HwArbitrationStateMonitor.TAG, "onDataConnectionDisconnected: EVENT_DEFAULT_DATA_SETUP_FAILURE on slotId " + slotId);
            HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(3, slotId, 0));
        }

        private boolean isDefaultApnType(String apnType) {
            return MemoryConstant.MEM_SCENE_DEFAULT.equals(apnType);
        }

        private void onReceiveDataStateChanged(Intent intent) {
            String apnType = intent.getStringExtra("apnType");
            if (isDefaultApnType(apnType)) {
                int slotId = intent.getIntExtra("subscription", -1);
                PhoneConstants.DataState state = Enum.valueOf(PhoneConstants.DataState.class, intent.getStringExtra("state"));
                if (!isSlotIdValid(slotId)) {
                    logE(HwArbitrationStateMonitor.TAG, "onReceiveStateChanged: param is invalid,slotId = " + slotId + ",state = " + state);
                    return;
                }
                logD(HwArbitrationStateMonitor.TAG, "onReceiveStateChanged: slotId = " + slotId + ",state = " + state + ",apnType = " + apnType);
                if (!this.mFristTimeRecvApnStateFlag[slotId]) {
                    logD(HwArbitrationStateMonitor.TAG, "mFristTimeRecvApnStateFlag ");
                    this.mApnState[slotId] = state;
                    this.mFristTimeRecvApnStateFlag[slotId] = true;
                } else if (!state.equals(this.mApnState[slotId])) {
                    logD(HwArbitrationStateMonitor.TAG, "apnState changed,oldMobileDataState = " + this.mApnState[slotId] + ",state = " + state);
                    switch (AnonymousClass6.$SwitchMap$com$android$internal$telephony$PhoneConstants$DataState[state.ordinal()]) {
                        case 1:
                            onDataConnectionDisconnected(this.mApnState[slotId], slotId);
                            break;
                        case 2:
                            onDataConnected(slotId);
                            break;
                    }
                    this.mApnState[slotId] = state;
                }
            }
        }

        public boolean isSlotIdValid(int slotId) {
            return slotId >= 0 && 2 > slotId;
        }

        private void handleTelephonyServiceStateChanged(ServiceState serviceState, int subId) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "ACTION_SERVICE_STATE_CHANGED subId:" + subId);
            if (subId != -1 && subId == this.mDefaultDataSubId && serviceState != null) {
                int newServiceState = serviceState.getState();
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "newServiceState:" + newServiceState);
                if (this.mCurrentServiceState != newServiceState) {
                    this.mCurrentServiceState = newServiceState;
                    HwArbitrationFunction.setServiceState(this.mCurrentServiceState);
                    if (this.mCurrentServiceState == 0) {
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_STATE_IN_SERVICE);
                    } else {
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_STATE_OUT_OF_SERVICE);
                    }
                }
                boolean newRoamingState = serviceState.getDataRoaming();
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "newRoammingState :" + newRoamingState);
                if (this.mCurrentDataRoamingState != newRoamingState) {
                    this.mCurrentDataRoamingState = newRoamingState;
                    HwArbitrationFunction.setDataRoamingState(this.mCurrentDataRoamingState);
                    if (this.mCurrentDataRoamingState) {
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_STATE_IS_ROAMING);
                    } else {
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(1021);
                    }
                }
                int newDataTechType = serviceState.getDataNetworkType();
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "newDataTechType:" + newDataTechType);
                if (this.mCurrentDataTechType != newDataTechType) {
                    this.mCurrentDataTechType = newDataTechType;
                    handleDataTechTypeChange(newDataTechType);
                }
            }
        }

        private void handleDataTechTypeChange(int dataTech) {
            HwArbitrationCommonUtils.logI(HwArbitrationStateMonitor.TAG, "handlerDataTechTypeChange dataTech :" + dataTech);
            HwArbitrationFunction.setDataTech(dataTech);
            boolean newDataTechSuitable = dataTech == 13 || dataTech == 19;
            if (this.mIsDataTechSuitable != newDataTechSuitable) {
                this.mIsDataTechSuitable = newDataTechSuitable;
                HwArbitrationFunction.setDataTechSuitable(newDataTechSuitable);
                if (newDataTechSuitable) {
                    HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(1022);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "MSG_DATA_TECHTYPE_SUITABLE");
                    HwHiRadioBoost.createInstance(HwArbitrationStateMonitor.this.mContext).setSwitchTo3GFlag(false);
                    return;
                }
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(1023);
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "MSG_DATA_TECHTYPE_NOT_SUITABLE");
            }
        }

        private void handleDataSubChange(int subId) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "DataSub Change, new subId:" + subId);
            if (subId != -1 && this.mDefaultDataSubId != subId) {
                this.mDefaultDataSubId = subId;
                HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(1024, subId, 0));
            }
        }

        private void handleTelephonyDataConnectionChanged(String state, int subId) {
            HwArbitrationCommonUtils.logI(HwArbitrationStateMonitor.TAG, "ACTION_ANY_DATA_CONNECTION_STATE_CHANGED subId:" + subId + ",state:" + state);
            if (subId != this.mDefaultDataSubId) {
                return;
            }
            if (AwareJobSchedulerConstants.SERVICES_STATUS_CONNECTED.equals(state)) {
                this.mMobileConnectState = true;
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "mobile_data_connected");
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(6);
                if (802 == this.mCurrentActiveNetwork) {
                    this.mCurrentActiveNetwork = 801;
                    HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(HwArbitrationDEFS.MSG_NOTIFY_CURRENT_NETWORK, this.mCurrentActiveNetwork, 0));
                }
            } else if ("DISCONNECTED".equals(state)) {
                this.mMobileConnectState = false;
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "mobile_data_disconnected");
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(5);
                if (801 == this.mCurrentActiveNetwork) {
                    this.mCurrentActiveNetwork = 802;
                    HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(HwArbitrationDEFS.MSG_NOTIFY_CURRENT_NETWORK, this.mCurrentActiveNetwork, 0));
                }
            }
        }

        private void logD(String TAG, String debugInfo) {
            Log.d(TAG, debugInfo);
        }

        private void logE(String TAG, String debugInfo) {
            Log.e(TAG, debugInfo);
        }
    }

    private class UserDataEnableObserver extends ContentObserver {
        public UserDataEnableObserver(Handler handler) {
            super(handler);
            ContentResolver unused = HwArbitrationStateMonitor.this.mResolver = HwArbitrationStateMonitor.this.mContext.getContentResolver();
        }

        public void register() {
            HwArbitrationStateMonitor.this.mResolver.registerContentObserver(Settings.Global.getUriFor("mobile_data"), false, this);
        }

        public void unregister() {
            HwArbitrationStateMonitor.this.mResolver.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            boolean state = HwArbitrationStateMonitor.this.isUserDataEnabled();
            if (state) {
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_CELL_STATE_ENABLE);
            } else {
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_CELL_STATE_DISABLE);
            }
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "User change Data service state = " + state);
        }
    }

    private HwArbitrationStateMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        HwHiDataCommonUtils.logD(TAG, "HwArbitrationStateMonitor create success!");
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
        startPhoneStateListener();
        registerNetworkChangeCallback();
        registerForDSDSChanges();
    }

    private void registerBroadcastReceiver() {
        HwArbitrationCommonUtils.logD(TAG, "start Monitoring intent");
        this.intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.intentFilter.addAction("android.intent.action.SCREEN_ON");
        this.intentFilter.addAction("android.intent.action.SERVICE_STATE");
        this.intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.intentFilter.addAction("android.intent.action.ANY_DATA_STATE");
        this.intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        this.intentFilter.addAction(HwArbitrationDEFS.ACTION_HiData_DATA_ROVE_IN);
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.intentFilter);
    }

    private void registerForVpnSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("wifipro_network_vpn_state"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                if (HwArbitrationStateMonitor.getSettingsSystemBoolean(HwArbitrationStateMonitor.this.mContext.getContentResolver(), "wifipro_network_vpn_state", false)) {
                    HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_VPN_STATE_OPEN);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "registerForVpnSettingsChanges VPN Open");
                    return;
                }
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_VPN_STATE_CLOSE);
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "registerForVpnSettingsChanges VPN Close");
            }
        });
    }

    private void registerForSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("smart_network_switching"), false, new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                boolean isWiFiProEnabled = HwArbitrationStateMonitor.getSettingsSystemBoolean(HwArbitrationStateMonitor.this.mContext.getContentResolver(), "smart_network_switching", false);
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "Wifi pro setting has changed, WiFiProEnabled == " + isWiFiProEnabled);
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
        return Settings.System.getInt(cr, name, def) == 1;
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

    private void observeUserDataEnableStatus() {
        this.mUserDataEnableObserver = new UserDataEnableObserver(this.mHandler);
        this.mUserDataEnableObserver.register();
    }

    public boolean isUserDataEnabled() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data", 0) != 0;
    }

    private void startPhoneStateListener() {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager == null) {
            HwArbitrationCommonUtils.logE(TAG, "SlotStateListener: mTelephonyManager is null, return!");
            return;
        }
        int numPhones = telephonyManager.getPhoneCount();
        if (numPhones != 2) {
            HwArbitrationCommonUtils.logE(TAG, "SlotStateListener numPhones = " + numPhones);
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, "SlotStateListener numPhones is " + numPhones);
        this.mPhoneStateListener = new PhoneStateListener[numPhones];
        for (int i = 0; i < numPhones; i++) {
            this.mPhoneStateListener[i] = getPhoneStateListener(i);
            telephonyManager.listen(this.mPhoneStateListener[i], 33);
        }
    }

    private PhoneStateListener getPhoneStateListener(int i) {
        return new PhoneStateListener(Integer.valueOf(i)) {
            public void onServiceStateChanged(ServiceState state) {
                if (state == null) {
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMonitor.TAG, "SlotStateListener onServiceStateChanged: state is null,return");
                } else if (!HwArbitrationCommonUtils.isSlotIdValid(this.mSubId.intValue())) {
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMonitor.TAG, "SlotStateListener onServiceStateChanged: invalid mSubId = " + this.mSubId);
                } else {
                    HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(4, this.mSubId.intValue(), 0, state));
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "SlotStateListener onServiceStateChanged on mSubId = " + this.mSubId + ", NetworkType is: " + state.getDataNetworkType());
                }
            }

            public void onCallStateChanged(int state, String incomingNumber) {
                if (!HwArbitrationCommonUtils.isSlotIdValid(this.mSubId.intValue())) {
                    HwArbitrationCommonUtils.logE(HwArbitrationStateMonitor.TAG, "SlotStateListener onCallStateChanged: invalid mSubId = " + this.mSubId);
                    return;
                }
                switch (state) {
                    case 0:
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "CALL_STATE_IDLE:mSubId:" + this.mSubId);
                        HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(7, this.mSubId.intValue(), 0));
                        break;
                    case 1:
                    case 2:
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "CALL_STATE_OFFHOOK or CALL_STATE_RINGING:mSubId:" + this.mSubId);
                        HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(8, this.mSubId.intValue(), 0));
                        break;
                }
            }
        };
    }

    /* access modifiers changed from: private */
    public void registerNetworkChangeCallback() {
        this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (this.mCM != null) {
            this.mHwArbitrationNetworkCallback = new ConnectivityManager.NetworkCallback() {
                private int mActiveNetworkType = 802;
                private Network mDefaultNetwork;
                private Network mLastNetwork;
                private NetworkCapabilities mLastNetworkCapabilities;

                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "ConnectivityManager.NetworkCallback : onCapabilitiesChanged");
                    if (network != null && networkCapabilities != null) {
                        boolean lastValidated = this.mLastNetworkCapabilities != null && this.mLastNetworkCapabilities.hasCapability(16);
                        boolean validated = networkCapabilities.hasCapability(16);
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "network:" + network.toString() + ", lastValidated:" + lastValidated + ", validated:" + validated);
                        if (!network.equals(this.mLastNetwork) || this.mLastNetworkCapabilities == null || !networkCapabilities.equalsTransportTypes(this.mLastNetworkCapabilities) || validated != lastValidated) {
                            this.mLastNetwork = network;
                            this.mLastNetworkCapabilities = networkCapabilities;
                            if (networkCapabilities.hasTransport(1) && validated) {
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "networkType:TRANSPORT_WIFI, network: " + network.toString());
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onNetworkCallback, MSG_WIFI_STATE_CONNECTED");
                                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(1005);
                                this.mDefaultNetwork = network;
                                this.mActiveNetworkType = 800;
                            } else if (networkCapabilities.hasTransport(0) && validated) {
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "networkType:TRANSPORT_CELLULAR, network: " + network.toString());
                                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onNetworkCallback, MSG_CELL_STATE_CONNECTED");
                                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_CELL_STATE_CONNECTED);
                                this.mDefaultNetwork = network;
                                this.mActiveNetworkType = 801;
                            }
                        }
                    }
                }

                public void onLost(Network network) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onNetworkCallback:onLost");
                    if (network != null) {
                        if (!network.equals(this.mDefaultNetwork)) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "lost network not equal to defaultNetwork");
                            return;
                        }
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onNetworkCallback-prev_type is:" + this.mActiveNetworkType + ", network:" + network.toString());
                        if (801 == this.mActiveNetworkType) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onNetworkCallback:MSG_CELL_STATE_DISCONNECT");
                            HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT);
                        } else if (800 == this.mActiveNetworkType) {
                            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, "onNetworkCallback:MSG_WIFI_STATE_DISCONNECT");
                            HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_WIFI_STATE_DISCONNECT);
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
        dsdsFilter.addAction(HwArbitrationDEFS.ACTION_HW_DSDS_MODE_STATE);
        this.mContext.registerReceiver(this.mDsdsReceiver, dsdsFilter, HwArbitrationDEFS.DSDSMODE_PERMISSION, this.mHandler);
    }
}
