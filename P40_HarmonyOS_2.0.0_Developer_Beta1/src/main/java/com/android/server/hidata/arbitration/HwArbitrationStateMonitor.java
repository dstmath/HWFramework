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
import com.android.server.intellicom.common.SmartDualCardConsts;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HwArbitrationStateMonitor {
    private static final int INVALID_SUB_ID = -1;
    private static final String TAG = (HwArbitrationDefs.BASE_TAG + HwArbitrationStateMonitor.class.getSimpleName());
    private static HwArbitrationStateMonitor sHwArbitrationStateMonitor = null;
    private boolean isCurrentDataTechSuitable = false;
    private boolean isMobileConnectState = false;
    private AirplaneModeObserver mAirplaneModeObserver;
    private BroadcastReceiver mBroadcastReceiver = new StateBroadcastReceiver();
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private int mCurrentActiveNetwork = 802;
    private int mCurrentDataTechType = 0;
    private Handler mHandler;
    private ConnectivityManager.NetworkCallback mHwArbitrationNetworkCallback;
    private IntentFilter mIntentFilter = new IntentFilter();
    private Map<Integer, SignalStrength> mPhoneSignalStrength = new ArrayMap();
    private Map<Integer, PhoneStateListener> mPhoneStateListeners = new ArrayMap();
    private ContentResolver mResolver;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener = null;
    private SubscriptionManager mSubscriptionManager = null;
    private TelephonyManager mTelephonyManager = null;
    private UserDataEnableObserver mUserDataEnableObserver;

    private HwArbitrationStateMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        HwArbitrationCommonUtils.logD(TAG, false, "HwArbitrationStateMonitor create success!", new Object[0]);
    }

    public static HwArbitrationStateMonitor createHwArbitrationStateMonitor(Context context, Handler handler) {
        if (sHwArbitrationStateMonitor == null) {
            sHwArbitrationStateMonitor = new HwArbitrationStateMonitor(context, handler);
        }
        return sHwArbitrationStateMonitor;
    }

    public void startMonitor() {
        registerBroadcastReceiver();
        registerForVpnSettingsChanges();
        registerForSettingsChanges();
        registerDatabaseObserver();
        registerSubscriptionsChangedListeners();
        registerNetworkChangeCallback();
    }

    private void registerBroadcastReceiver() {
        this.mIntentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED);
        this.mIntentFilter.addAction(SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE);
        this.mIntentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        this.mIntentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
        this.mIntentFilter.addAction("android.intent.action.SERVICE_STATE");
        this.mIntentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mIntentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_ANY_DATA_CONNECTION_STATE_CHANGED);
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
    }

    public void handleTelephonyServiceStateChanged(ServiceState serviceState, int subId) {
        HwArbitrationCommonUtils.logD(TAG, false, "Before getDefaultDataSubscriptionId subId:%{public}d", Integer.valueOf(subId));
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (subId == -1 || subId != defaultDataSubId || serviceState == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "subId == INVALID_SUB_ID, or subId != mDefaultDataSubId, or serviceState == null, just return", new Object[0]);
            return;
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

    private void registerForVpnSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(HwArbitrationDefs.SETTING_SECURE_VPN_WORK_VALUE), false, new ContentObserver(null) {
            /* class com.android.server.hidata.arbitration.HwArbitrationStateMonitor.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HwArbitrationStateMonitor hwArbitrationStateMonitor = HwArbitrationStateMonitor.this;
                if (hwArbitrationStateMonitor.getSettingsSystemBoolean(hwArbitrationStateMonitor.mContext.getContentResolver(), HwArbitrationDefs.SETTING_SECURE_VPN_WORK_VALUE, false)) {
                    HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_VPN_STATE_OPEN);
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "registerForVpnSettingsChanges VPN Open", new Object[0]);
                    return;
                }
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_VPN_STATE_CLOSE);
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "registerForVpnSettingsChanges VPN Close", new Object[0]);
            }
        });
    }

    private void registerForSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("smart_network_switching"), false, new ContentObserver(new Handler()) {
            /* class com.android.server.hidata.arbitration.HwArbitrationStateMonitor.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HwArbitrationStateMonitor hwArbitrationStateMonitor = HwArbitrationStateMonitor.this;
                boolean isWiFiProEnabled = hwArbitrationStateMonitor.getSettingsSystemBoolean(hwArbitrationStateMonitor.mContext.getContentResolver(), "smart_network_switching", false);
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "Wifi pro setting has changed, WiFiProEnabled == %{public}s", String.valueOf(isWiFiProEnabled));
                if (isWiFiProEnabled) {
                    HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_WIFI_PLUS_ENABLE);
                } else {
                    HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_WIFI_PLUS_DISABLE);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean getSettingsSystemBoolean(ContentResolver cr, String name, boolean def) {
        return Settings.System.getInt(cr, name, def ? 1 : 0) == 1;
    }

    private void registerDatabaseObserver() {
        observeAirplaneMode();
        observeUserDataEnableStatus();
    }

    private void observeAirplaneMode() {
        this.mAirplaneModeObserver = new AirplaneModeObserver(this.mHandler);
        this.mAirplaneModeObserver.register();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAirplaneModeOn() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    private void observeUserDataEnableStatus() {
        this.mUserDataEnableObserver = new UserDataEnableObserver(this.mHandler);
        this.mUserDataEnableObserver.register();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isUserDataEnabled() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data", 0) != 0;
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
    private PhoneStateListener getPhoneStateListener(int slotId) {
        return new HwPhoneStateListener(slotId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerNetworkChangeCallback() {
        if (this.mConnectivityManager != null) {
            this.mHwArbitrationNetworkCallback = new HwArbitrationNetworkCallback();
            this.mConnectivityManager.registerDefaultNetworkCallback(this.mHwArbitrationNetworkCallback, this.mHandler);
        }
    }

    /* access modifiers changed from: private */
    public class HwPhoneStateListener extends PhoneStateListener {
        HwPhoneStateListener(int slotId) {
            super(Integer.valueOf(slotId));
        }

        @Override // android.telephony.PhoneStateListener
        public void onServiceStateChanged(ServiceState serviceState) {
            if (serviceState == null) {
                HwArbitrationCommonUtils.logE(HwArbitrationStateMonitor.TAG, false, "onServiceStateChanged, serviceState is null", new Object[0]);
            } else if (!HwArbitrationCommonUtils.isSubIdValid(this.mSubId.intValue())) {
                HwArbitrationCommonUtils.logE(HwArbitrationStateMonitor.TAG, false, "onServiceStateChanged, invalid mSubId = %{public}d", this.mSubId);
            } else {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "SlotStateListener onServiceStateChanged on mSubId = %{public}d, NetworkType is: %{public}d", this.mSubId, Integer.valueOf(serviceState.getDataNetworkType()));
                HwArbitrationStateMonitor.this.handleTelephonyServiceStateChanged(serviceState, this.mSubId.intValue());
            }
        }
    }

    /* access modifiers changed from: private */
    public class HwArbitrationNetworkCallback extends ConnectivityManager.NetworkCallback {
        private int mActiveNetworkType;
        private Network mDefaultNetwork;
        private Network mLastNetwork;
        private NetworkCapabilities mLastNetworkCapabilities;

        private HwArbitrationNetworkCallback() {
            this.mActiveNetworkType = 802;
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            sendMessageByNetworkCapabilities(network, networkCapabilities);
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            sendMessageByNetworkType(network, this.mActiveNetworkType);
        }

        private void sendMessageByNetworkType(Network network, int networkType) {
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onNetworkCallback, onLost", new Object[0]);
            if (network != null) {
                if (!network.equals(this.mDefaultNetwork)) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "lost network not equal to defaultNetwork", new Object[0]);
                    return;
                }
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onNetworkCallback, prev type is:%{public}d, network:%{public}s", Integer.valueOf(networkType), network.toString());
                if (networkType == 801) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onNetworkCallback, MSG_CELL_STATE_DISCONNECT", new Object[0]);
                    HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_CELL_STATE_DISCONNECT);
                } else if (networkType == 800) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onNetworkCallback, MSG_WIFI_STATE_DISCONNECT", new Object[0]);
                    HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(1006);
                }
                this.mActiveNetworkType = 802;
            }
        }

        private void sendMessageByNetworkCapabilities(Network network, NetworkCapabilities networkCapabilities) {
            if (network != null && networkCapabilities != null) {
                NetworkCapabilities networkCapabilities2 = this.mLastNetworkCapabilities;
                boolean lastValidated = networkCapabilities2 != null && networkCapabilities2.hasCapability(16);
                boolean validated = networkCapabilities.hasCapability(16);
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onCapabilitiesChanged, network:%{public}s, lastValidated:%{public}s, validated:%{public}s", network.toString(), String.valueOf(lastValidated), String.valueOf(validated));
                NetworkCapabilities networkCapabilities3 = this.mLastNetworkCapabilities;
                boolean isSameNetworkCapabilities = networkCapabilities3 != null && networkCapabilities.equalsTransportTypes(networkCapabilities3);
                if (!network.equals(this.mLastNetwork) || !isSameNetworkCapabilities || validated != lastValidated) {
                    this.mLastNetwork = network;
                    this.mLastNetworkCapabilities = networkCapabilities;
                    if (networkCapabilities.hasTransport(1) && validated) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "networkType:TRANSPORT_WIFI, network: %{public}s", network.toString());
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onCapabilitiesChanged, MSG_WIFI_STATE_CONNECTED", new Object[0]);
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(1005);
                        this.mDefaultNetwork = network;
                        this.mActiveNetworkType = 800;
                    } else if (networkCapabilities.hasTransport(0) && validated) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "networkType:TRANSPORT_CELLULAR, network: %{public}s", network.toString());
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onCapabilitiesChanged, MSG_CELL_STATE_CONNECTED", new Object[0]);
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_CELL_STATE_CONNECTED);
                        this.mDefaultNetwork = network;
                        this.mActiveNetworkType = 801;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class AirplaneModeObserver extends ContentObserver {
        public AirplaneModeObserver(Handler handler) {
            super(handler);
            HwArbitrationStateMonitor.this.mResolver = HwArbitrationStateMonitor.this.mContext.getContentResolver();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void register() {
            HwArbitrationStateMonitor.this.mResolver.registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), false, this);
        }

        private void unregister() {
            HwArbitrationStateMonitor.this.mResolver.unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            boolean airplaneMode = HwArbitrationStateMonitor.this.isAirplaneModeOn();
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "AirplaneMode change to %{public}s", String.valueOf(airplaneMode));
            if (airplaneMode) {
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_AIRPLANE_MODE_ON);
            } else {
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_AIRPLANE_MODE_OFF);
            }
        }
    }

    /* access modifiers changed from: private */
    public class UserDataEnableObserver extends ContentObserver {
        public UserDataEnableObserver(Handler handler) {
            super(handler);
            HwArbitrationStateMonitor.this.mResolver = HwArbitrationStateMonitor.this.mContext.getContentResolver();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void register() {
            HwArbitrationStateMonitor.this.mResolver.registerContentObserver(Settings.Global.getUriFor("mobile_data"), false, this);
        }

        private void unregister() {
            HwArbitrationStateMonitor.this.mResolver.unregisterContentObserver(this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            boolean state = HwArbitrationStateMonitor.this.isUserDataEnabled();
            if (state) {
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_CELL_STATE_ENABLE);
            } else {
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_CELL_STATE_DISABLE);
            }
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "User change Data service state = %{public}s", String.valueOf(state));
        }
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
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onSubscriptionsChanged num: " + subInfos.size(), new Object[0]);
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

    private class StateBroadcastReceiver extends BroadcastReceiver {
        private NetworkInfo mActiveNetworkInfo;
        private int mConnectivityType;
        private boolean mWifiConnectState;

        private StateBroadcastReceiver() {
            this.mWifiConnectState = false;
            this.mConnectivityType = 802;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwArbitrationCommonUtils.logE(HwArbitrationStateMonitor.TAG, false, "received intent is null, return", new Object[0]);
                return;
            }
            String action = intent.getAction();
            if (SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED.equals(action)) {
                handleWifiConnectState(intent);
            } else if (SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE.equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "Connectivity Changed", new Object[0]);
                onConnectivityNetworkChange();
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "MSG_SCREEN_IS_OFF", new Object[0]);
                HwArbitrationFunction.setScreenState(false);
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_SCREEN_IS_TURN_OFF);
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "MSG_SCREEN_IS_ON", new Object[0]);
                HwArbitrationFunction.setScreenState(true);
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_SCREEN_IS_ON);
            } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_ANY_DATA_CONNECTION_STATE_CHANGED.equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "ACTION_ANY_DATA_CONNECTION_STATE_CHANGED", new Object[0]);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "BOOT_COMPLETED", new Object[0]);
                if (HwArbitrationStateMonitor.this.mHwArbitrationNetworkCallback == null) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "registerDefaultNetworkCallback have not been registered", new Object[0]);
                    HwArbitrationStateMonitor.this.registerNetworkChangeCallback();
                }
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_DEVICE_BOOT_COMPLETED);
            } else {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "Alert trigger closing MPLink", new Object[0]);
            }
        }

        private void handleWifiConnectState(Intent intent) {
            NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            if (netInfo != null) {
                if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "WifiManager: Wifi disconnected", new Object[0]);
                    if (this.mWifiConnectState) {
                        this.mWifiConnectState = false;
                    }
                } else if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "WifiManager: Wifi connected", new Object[0]);
                    if (!this.mWifiConnectState) {
                        this.mWifiConnectState = true;
                    }
                }
            }
        }

        private void onConnectivityNetworkChange() {
            if (HwArbitrationStateMonitor.this.mConnectivityManager != null) {
                this.mActiveNetworkInfo = HwArbitrationStateMonitor.this.mConnectivityManager.getActiveNetworkInfo();
                if (this.mActiveNetworkInfo != null) {
                    sendMessageByCurrentActiveNetwork();
                } else {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange, previous network type is: %{public}d", Integer.valueOf(this.mConnectivityType));
                    int i = this.mConnectivityType;
                    int i2 = 801;
                    if (i == 800) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange, MSG_WIFI_STATE_DISCONNECT", new Object[0]);
                        if (HwArbitrationStateMonitor.this.mCurrentActiveNetwork != 802) {
                            HwArbitrationStateMonitor hwArbitrationStateMonitor = HwArbitrationStateMonitor.this;
                            if (!hwArbitrationStateMonitor.isMobileConnectState) {
                                i2 = 802;
                            }
                            hwArbitrationStateMonitor.mCurrentActiveNetwork = i2;
                            HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(HwArbitrationDefs.MSG_NOTIFY_CURRENT_NETWORK, HwArbitrationStateMonitor.this.mCurrentActiveNetwork, 0));
                        }
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(1006);
                    } else if (i == 801) {
                        HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange, MSG_CELL_STATE_DISCONNECT", new Object[0]);
                        HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_CELL_STATE_DISCONNECT);
                    }
                    this.mConnectivityType = 802;
                }
                HwAppTimeDetail.getInstance().notifyNetworkChange(this.mConnectivityType);
            }
        }

        private void sendMessageByCurrentActiveNetwork() {
            if (this.mActiveNetworkInfo.getType() == 1) {
                if (this.mActiveNetworkInfo.isConnected()) {
                    HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange, MSG_WIFI_STATE_CONNECTED", new Object[0]);
                    if (HwArbitrationStateMonitor.this.mCurrentActiveNetwork != 800) {
                        HwArbitrationStateMonitor.this.mCurrentActiveNetwork = 800;
                        HwArbitrationStateMonitor.this.mHandler.sendMessage(HwArbitrationStateMonitor.this.mHandler.obtainMessage(HwArbitrationDefs.MSG_NOTIFY_CURRENT_NETWORK, HwArbitrationStateMonitor.this.mCurrentActiveNetwork, 0));
                    }
                    HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(1005);
                    this.mConnectivityType = 800;
                    return;
                }
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange, Wifi: %{public}d", this.mActiveNetworkInfo.getState());
            } else if (this.mActiveNetworkInfo.getType() == 0) {
                activeNetworkInfo();
            }
        }

        private void activeNetworkInfo() {
            if (this.mActiveNetworkInfo.isConnected()) {
                HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange, MSG_CELL_STATE_CONNECTED", new Object[0]);
                HwArbitrationStateMonitor.this.mHandler.sendEmptyMessage(HwArbitrationDefs.MSG_CELL_STATE_CONNECTED);
                this.mConnectivityType = 801;
                return;
            }
            HwArbitrationCommonUtils.logD(HwArbitrationStateMonitor.TAG, false, "onConnectivityNetworkChange-Cell:%{public}d", this.mActiveNetworkInfo.getState());
        }
    }
}
