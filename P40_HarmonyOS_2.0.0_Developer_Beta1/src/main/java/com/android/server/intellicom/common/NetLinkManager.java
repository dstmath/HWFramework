package com.android.server.intellicom.common;

import android.app.ActivityManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.intellicom.smartdualcard.SmartDualCardRecommendNotify;
import java.util.ArrayList;

public class NetLinkManager extends Handler {
    private static final long DUAL_LINK_DSDA_POOR_COUNT_THRESHOLD = 2;
    private static final long DUAL_LINK_DSDA_POOR_PUNISH_TIME = 180000;
    private static final long DUAL_LINK_MIN_STABLY_TIME = 180000;
    private static final int EVENT_AIRPALNE_STATE_CHANGED = 0;
    private static final int EVENT_DSDA_POOR_PUNISH_TIMEOUT = 6;
    private static final int EVENT_INTELLIGENCE_CARD_STATE_CHANGED = 2;
    private static final int EVENT_MOBILE_DATA_STATE_CHANGED = 1;
    private static final int EVENT_RECHECK_PRIORITY_APP_PROVIDER = 7;
    private static final int EVENT_RESIDUE_TRAFFIC_NOT_ENOUGH = 0;
    private static final int EVENT_SETTING_SECURE_DUAL_CARD_FREE_APP_CHANGE = 3;
    private static final int EVENT_SETTING_SECURE_NETASSISTANT_MONTH_LIMIT_CHANGE = 4;
    private static final int EVENT_SHOW_POP_WINDOW = 1;
    private static final int EVENT_USER_CHOOSE_SWITCH_BACK_PUNISH_TIMER_TIMEOUT = 5;
    private static final Object LOCK = new Object();
    private static final int MASTER_CARD_INDEX = 0;
    private static final Object NETWORK_REQUEST_LOCK = new Object();
    private static final long PRIORITY_APP_RECHECK_TIME = 5000;
    private static final int SLAVE_CARD_INDEX = 1;
    private static final long SLAVE_CARD_TRAFFIC_LIMIT = 52428800;
    private static final int SLAVE_CARD_TRAFFIC_LIMIT_NOT_SET = -1;
    private static final String SWITCH_OFF = "off";
    private static final String SWITCH_ON = "on";
    private static final String TAG = "NetLinkManager";
    private static final long USER_CHOOSE_SWITCH_BACK_PUNISH_TIME = 86400000;
    private static int sDsdaBadCount = 0;
    private static int sMasterCardNetId = -1;
    private static NetLinkManager sNetLinkManager = null;
    private static int sSlaveCardNetId = -1;
    private IHwCommBoosterServiceManager boosterServiceManager = null;
    private boolean[] isAlReadyRequestNetwork = {false, false};
    private boolean isRegisterBoosterCallbackSuccess = false;
    private ConnectivityManager.NetworkCallback mBluetoothCallBack = new ConnectivityManager.NetworkCallback() {
        /* class com.android.server.intellicom.common.NetLinkManager.AnonymousClass3 */

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            NetworkCapabilities networkCapabilities;
            if (NetLinkManager.this.mConnectivityManager != null && (networkCapabilities = NetLinkManager.this.mConnectivityManager.getNetworkCapabilities(network)) != null) {
                boolean hasCapability = networkCapabilities.hasCapability(16);
                NetLinkManager netLinkManager = NetLinkManager.this;
                netLinkManager.log("bluetooth connect. " + hasCapability);
                NetLinkManager.this.onBluetoothNetworkStateChanged(hasCapability);
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            NetLinkManager.this.log("bluetooth connect lost. ");
            NetLinkManager.this.onBluetoothNetworkStateChanged(false);
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            if (networkCapabilities != null) {
                boolean hasCapability = networkCapabilities.hasCapability(16);
                NetLinkManager netLinkManager = NetLinkManager.this;
                netLinkManager.log("bluetooth capabilities changed. " + hasCapability);
                NetLinkManager.this.onBluetoothNetworkStateChanged(hasCapability);
            }
        }
    };
    private ConnectivityManager mConnectivityManager = null;
    private Context mContext = null;
    private IHwCommBoosterCallback mHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
        /* class com.android.server.intellicom.common.NetLinkManager.AnonymousClass5 */

        public void callBack(int type, Bundle bundle) throws RemoteException {
            if (type == 0) {
                NetLinkManager.this.log("slave card residue traffic is not enough");
                NetLinkManager.this.mIsMonthTrafficLimitOk = false;
                NetLinkManager.this.onSystemStateChanged();
            }
            if (type == 1 && bundle != null) {
                int uid = bundle.getInt("arg1");
                int appListSize = bundle.getInt("arg2");
                NetLinkManager netLinkManager = NetLinkManager.this;
                netLinkManager.log("show pop window. uid=" + uid + " priorityAppListSize=" + appListSize);
                SmartDualCardRecommendNotify notify = SmartDualCardRecommendNotify.getInstance();
                notify.sendMessage(notify.obtainMessage(0, uid, appListSize));
            }
        }
    };
    private HwSettingsObserver mHwSettingsObserver = null;
    private boolean mIsAirPlaneModeOn = false;
    private boolean mIsBluetoothConnect = false;
    private boolean mIsDsdaModeOn = false;
    private boolean mIsIntelligenceCardOn = false;
    private boolean mIsMobileDataOn = false;
    private boolean mIsMonthTrafficLimitOk = false;
    private boolean mIsNeedCheckMonthTrafficLimit = true;
    private boolean mIsNeedCheckPriorityAppDb = true;
    private boolean mIsOwnerUser = false;
    private boolean mIsPriorityAppListEmpty = false;
    private boolean mIsSecureVpnOn = false;
    private boolean mIsSlotOnCard0 = false;
    private boolean mIsSlotOnCard1 = false;
    private boolean mIsTetheringOn = false;
    private boolean mIsUnlimitedDataSet = false;
    private boolean mIsUserChooseSwitchBack = false;
    private boolean mIsWifiConnect = false;
    private long mLastBuildDualLinkTime = 0;
    private ConnectivityManager.NetworkCallback mMasterCardCallback = new ConnectivityManager.NetworkCallback() {
        /* class com.android.server.intellicom.common.NetLinkManager.AnonymousClass2 */

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            int unused = NetLinkManager.sMasterCardNetId = network.netId;
            NetLinkManager netLinkManager = NetLinkManager.this;
            netLinkManager.log("mMasterCardCallback onAvailable, sMasterCardNetId: " + NetLinkManager.sMasterCardNetId);
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            int unused = NetLinkManager.sMasterCardNetId = -1;
            NetLinkManager netLinkManager = NetLinkManager.this;
            netLinkManager.log("mMasterCardCallback onLost, sMasterCardNetId: " + NetLinkManager.sMasterCardNetId);
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onUnavailable() {
            int unused = NetLinkManager.sMasterCardNetId = -1;
            NetLinkManager netLinkManager = NetLinkManager.this;
            netLinkManager.log("mMasterCardCallback onUnavailable, sMasterCardNetId: " + NetLinkManager.sMasterCardNetId);
        }
    };
    private long mMonthTrafficLimit = 0;
    private ConnectivityManager.NetworkCallback mSlaveCardCallback = new ConnectivityManager.NetworkCallback() {
        /* class com.android.server.intellicom.common.NetLinkManager.AnonymousClass1 */

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            int unused = NetLinkManager.sSlaveCardNetId = network.netId;
            NetLinkManager netLinkManager = NetLinkManager.this;
            netLinkManager.log("mSubCardCallback onAvailable, sSlaveCardNetId: " + NetLinkManager.sSlaveCardNetId);
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            int unused = NetLinkManager.sSlaveCardNetId = -1;
            NetLinkManager netLinkManager = NetLinkManager.this;
            netLinkManager.log("mSubCardCallback onLost, sSlaveCardNetId: " + NetLinkManager.sSlaveCardNetId);
            NetLinkManager.this.onLostStateChanged();
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onUnavailable() {
            int unused = NetLinkManager.sSlaveCardNetId = -1;
            NetLinkManager netLinkManager = NetLinkManager.this;
            netLinkManager.log("mSubCardCallback onUnavailable, sSlaveCardNetId: " + NetLinkManager.sSlaveCardNetId);
        }
    };
    private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        /* class com.android.server.intellicom.common.NetLinkManager.AnonymousClass4 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                Log.w(NetLinkManager.TAG, "intent or intent.getAction() is null.");
                return;
            }
            String action = intent.getAction();
            char c = 65535;
            switch (action.hashCode()) {
                case -1825033773:
                    if (action.equals(SmartDualCardConsts.ACTION_HW_DUAL_PS_STATE)) {
                        c = 3;
                        break;
                    }
                    break;
                case -1754841973:
                    if (action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_TETHER_STATE_CHANGED)) {
                        c = 2;
                        break;
                    }
                    break;
                case -1172645946:
                    if (action.equals(SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE)) {
                        c = 4;
                        break;
                    }
                    break;
                case -1040815769:
                    if (action.equals(SmartDualCardConsts.ACTION_USER_CHOOSE_SWITCH_BACK)) {
                        c = 7;
                        break;
                    }
                    break;
                case -374985024:
                    if (action.equals(SmartDualCardConsts.HW_SYSTEM_SERVER_START)) {
                        c = '\b';
                        break;
                    }
                    break;
                case -343630553:
                    if (action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED)) {
                        c = 1;
                        break;
                    }
                    break;
                case -229777127:
                    if (action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_SIM_STATE_CHANGED)) {
                        c = 0;
                        break;
                    }
                    break;
                case -25388475:
                    if (action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_DEFAULT_DATA_SUBSCRIPTION_CHANGED)) {
                        c = 5;
                        break;
                    }
                    break;
                case 959232034:
                    if (action.equals(SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED)) {
                        c = 6;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    NetLinkManager.this.onSimStateChanged(intent);
                    return;
                case 1:
                    NetLinkManager.this.onWifiNetworkStateChanged(intent);
                    return;
                case 2:
                    NetLinkManager.this.onTetheringStateChanged(intent);
                    return;
                case 3:
                    NetLinkManager.this.onDsdaStateChange(intent);
                    return;
                case 4:
                    NetLinkManager.this.onConnectivityChange(intent);
                    return;
                case 5:
                    NetLinkManager.this.onSystemStateChanged();
                    return;
                case 6:
                    NetLinkManager.this.onUserSwitched();
                    return;
                case 7:
                    NetLinkManager.this.onUserChooseSwitchback(true);
                    NetLinkManager netLinkManager = NetLinkManager.this;
                    netLinkManager.sendMessageDelayed(netLinkManager.obtainMessage(5), 86400000);
                    return;
                case '\b':
                    NetLinkManager.this.log("hw system server start");
                    NetLinkManager netLinkManager2 = NetLinkManager.this;
                    netLinkManager2.isRegisterBoosterCallbackSuccess = netLinkManager2.registerBoosterCallback();
                    return;
                default:
                    NetLinkManager.this.log("BroadcastReceiver error: " + intent.getAction());
                    return;
            }
        }
    };

    private NetLinkManager() {
    }

    public static NetLinkManager getInstance() {
        NetLinkManager netLinkManager;
        synchronized (LOCK) {
            if (sNetLinkManager == null) {
                sNetLinkManager = new NetLinkManager();
            }
            netLinkManager = sNetLinkManager;
        }
        return netLinkManager;
    }

    public IHwCommBoosterServiceManager getBoosterServiceManager() {
        return this.boosterServiceManager;
    }

    private void initIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_TETHER_STATE_CHANGED);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SIM_STATE_CHANGED);
        filter.addAction(SmartDualCardConsts.ACTION_HW_DUAL_PS_STATE);
        filter.addAction(SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_ACTION_USER_SWITCHED);
        filter.addAction(SmartDualCardConsts.ACTION_USER_CHOOSE_SWITCH_BACK);
        filter.addAction(SmartDualCardConsts.HW_SYSTEM_SERVER_START);
        this.mContext.registerReceiver(this.mStateReceiver, filter);
    }

    private void registerSettingsObserver() {
        this.mHwSettingsObserver = HwSettingsObserver.getInstance();
        this.mHwSettingsObserver.registerForSettingDbChange(0, this, 0, null);
        this.mHwSettingsObserver.registerForSettingDbChange(1, this, 1, null);
        this.mHwSettingsObserver.registerForSettingDbChange(2, this, 2, null);
        this.mHwSettingsObserver.registerForSettingDbChange(5, this, 3, null);
        this.mHwSettingsObserver.registerForSettingDbChange(6, this, 4, null);
    }

    private boolean getPriorityAppState() {
        boolean isPriorityAppListEmpty;
        TelephonyManager telephonyManager = TelephonyManager.from(this.mContext);
        if (telephonyManager == null) {
            log("getPriorityAppState:mTelephonyManager is null, start timer");
            this.mIsNeedCheckPriorityAppDb = true;
            removeMessages(7);
            sendMessageDelayed(obtainMessage(7), 5000);
            return this.mIsPriorityAppListEmpty;
        }
        int slaveSlotId = SmartDualCardUtil.getSlaveCardSlotId();
        String slaveIccid = HwTelephonyManager.getDefault().getSimSerialNumber(telephonyManager, slaveSlotId);
        if (slaveIccid == null) {
            log("getPriorityAppState, slaveIccid is null, start timer");
            this.mIsNeedCheckPriorityAppDb = true;
            removeMessages(7);
            sendMessageDelayed(obtainMessage(7), 5000);
            return this.mIsPriorityAppListEmpty;
        }
        int lengthOfTrafficAppList = this.mHwSettingsObserver.getPriorityAppListLength(slaveIccid);
        if (lengthOfTrafficAppList > 0) {
            isPriorityAppListEmpty = false;
            this.mIsNeedCheckPriorityAppDb = false;
        } else if (lengthOfTrafficAppList == 0) {
            isPriorityAppListEmpty = true;
            this.mIsNeedCheckPriorityAppDb = false;
        } else {
            this.mIsNeedCheckPriorityAppDb = true;
            boolean isPriorityAppListEmpty2 = this.mIsPriorityAppListEmpty;
            removeMessages(7);
            sendMessageDelayed(obtainMessage(7), 5000);
            log("getPriorityAppState fail, start timer");
            isPriorityAppListEmpty = isPriorityAppListEmpty2;
        }
        log("getPriorityAppState, slaveSlotId = " + slaveSlotId + " isPriorityAppListEmpty:" + isPriorityAppListEmpty);
        if (!this.mIsNeedCheckPriorityAppDb && hasMessages(7)) {
            removeMessages(7);
        }
        return isPriorityAppListEmpty;
    }

    private void initLocalState() {
        this.mIsTetheringOn = false;
        this.mIsSlotOnCard0 = false;
        this.mIsSlotOnCard1 = false;
        this.mIsDsdaModeOn = false;
        this.mIsWifiConnect = isDataConnected(1);
        this.mIsBluetoothConnect = isDataConnected(7);
        this.mIsAirPlaneModeOn = this.mHwSettingsObserver.isAirPlaneModeSwitchOn();
        this.mIsMobileDataOn = this.mHwSettingsObserver.isMobileDataSwitchOn();
        this.mIsIntelligenceCardOn = this.mHwSettingsObserver.isIntelligenceCardSwitchOn();
        this.mIsSecureVpnOn = isDataConnected(17);
        this.mIsOwnerUser = isOwnerUser();
        this.mIsMonthTrafficLimitOk = isMonthLimitTrafficOk();
        this.mIsPriorityAppListEmpty = getPriorityAppState();
        log("initLocalState, mIsTetheringOn:" + this.mIsTetheringOn + ", mIsWifiConnect:" + this.mIsWifiConnect + ", mIsAirPlaneModeOn:" + this.mIsAirPlaneModeOn + ", mIsMobileDataOn:" + this.mIsMobileDataOn + ", mIsIntelligenceCardOn:" + this.mIsIntelligenceCardOn + ", mIsSecureVpnOn:" + this.mIsSecureVpnOn + ", mIsDsdaModeOn:" + this.mIsDsdaModeOn + ", isDualCardMode:" + isDualCardMode() + ", mIsPriorityAppListEmpty:" + this.mIsPriorityAppListEmpty + ", mIsOwnerUser:" + this.mIsOwnerUser + ", mIsMonthTrafficLimitOk:" + this.mIsMonthTrafficLimitOk + ", mIsBluetoothConnect:" + this.mIsBluetoothConnect);
    }

    public void init(Context context) {
        log("init NetLinkManager");
        this.mContext = context;
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.isRegisterBoosterCallbackSuccess = registerBoosterCallback();
        registerBluetoothCallback();
        registerSettingsObserver();
        initIntentFilter();
        initLocalState();
    }

    public int getNetIdBySlotId(int slotId) {
        int masterCardSlotId = SmartDualCardUtil.getMasterCardSlotId();
        int slaveCardSlotId = SmartDualCardUtil.getSlaveCardSlotId();
        if (slotId == masterCardSlotId) {
            return sMasterCardNetId;
        }
        if (slotId == slaveCardSlotId) {
            return sSlaveCardNetId;
        }
        return -1;
    }

    private void activePdnBySubId(int subId, ConnectivityManager.NetworkCallback callBack, int cardIndex) {
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            if (this.mConnectivityManager == null) {
                log("activePdnBySubId, Can not get ConnectivityManager");
                return;
            }
        }
        log("activePdnBySubId, subId: " + subId);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(12);
        builder.addTransportType(0);
        builder.removeCapability(13);
        builder.setNetworkSpecifier(String.valueOf(subId));
        this.mConnectivityManager.requestNetwork(builder.build(), callBack);
        this.isAlReadyRequestNetwork[cardIndex] = true;
    }

    private void activeSlaveCardPdn() {
        int slaveCardSubId = SmartDualCardUtil.getSlaveCardSubId();
        if (!SmartDualCardUtil.isValidSubId(slaveCardSubId)) {
            log("activeSubCardPdn, invalid subId: " + slaveCardSubId);
            return;
        }
        log("activeSubCardPdn, slaveCardSubId: " + slaveCardSubId);
        activePdnBySubId(slaveCardSubId, this.mSlaveCardCallback, 1);
    }

    private void activeMasterCardPdn() {
        int masterCardSubId = SmartDualCardUtil.getMasterCardSubId();
        if (!SmartDualCardUtil.isValidSubId(masterCardSubId)) {
            log("activeSubCardPdn, invalid masterCardSubId: " + masterCardSubId);
            return;
        }
        log("activeSubCardPdn, masterCardSubId: " + masterCardSubId);
        activePdnBySubId(masterCardSubId, this.mMasterCardCallback, 0);
    }

    private void activeDualCardPdn() {
        synchronized (NETWORK_REQUEST_LOCK) {
            if (!this.isAlReadyRequestNetwork[0]) {
                activeMasterCardPdn();
            }
            if (!this.isAlReadyRequestNetwork[1]) {
                activeSlaveCardPdn();
            }
        }
        long nowTimeStamp = SystemClock.elapsedRealtime();
        if (nowTimeStamp - this.mLastBuildDualLinkTime > 180000) {
            sDsdaBadCount = 0;
            log("activeDualCardPdn, set sDsdsBadCount to 0. mLastBuildDualLinkTime=" + this.mLastBuildDualLinkTime + " nowTimeStamp=" + nowTimeStamp);
        }
        this.mLastBuildDualLinkTime = nowTimeStamp;
    }

    private void inactiveDualCardPdn() {
        if (this.mConnectivityManager == null) {
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            if (this.mConnectivityManager == null) {
                log("inactiveDualCardPdn, Can not get ConnectivityManager");
                return;
            }
        }
        synchronized (NETWORK_REQUEST_LOCK) {
            this.mConnectivityManager.unregisterNetworkCallback(this.mSlaveCardCallback);
            sSlaveCardNetId = -1;
            this.mConnectivityManager.unregisterNetworkCallback(this.mMasterCardCallback);
            sMasterCardNetId = -1;
            this.isAlReadyRequestNetwork[0] = false;
            this.isAlReadyRequestNetwork[1] = false;
            log("inactiveDualCardPdn, sSlaveCardNetId=-1 sMasterCardNetId=-1");
        }
        long nowTimeStamp = SystemClock.elapsedRealtime();
        if (nowTimeStamp - this.mLastBuildDualLinkTime > 180000) {
            sDsdaBadCount = 0;
            log("inactiveDualCardPdn, set sDsdsBadCount to 0. mLastBuildDualLinkTime=" + this.mLastBuildDualLinkTime + " nowTimeStamp=" + nowTimeStamp);
        }
    }

    public boolean isIntelligenceCardOk() {
        if (!getSmartCardDisplayProp()) {
            log("isIntelligenceCardOk, false, smart card display prop is close.");
            return false;
        } else if (!this.mIsIntelligenceCardOn) {
            log("isIntelligenceCardOk, false, IntelligenceCard is close.");
            return false;
        } else if (!this.mIsOwnerUser) {
            log("isIntelligenceCardOk, false, mIsOwnerUser:" + this.mIsOwnerUser);
            return false;
        } else if (isPriorityAppListEmpty()) {
            log("isIntelligenceCardOk, false, priority appList is empty.");
            return false;
        } else if (!this.mIsUserChooseSwitchBack) {
            return true;
        } else {
            log("isIntelligenceCardOk, false, user choose switch back.");
            return false;
        }
    }

    public boolean isDataConnectionOk() {
        if (!this.mIsMobileDataOn) {
            log("isDataConnectionOk, false, mobile data is close.");
            return false;
        } else if (this.mIsWifiConnect) {
            log("isDataConnectionOk, false, wifi is connect.");
            return false;
        } else if (this.mIsBluetoothConnect) {
            log("isDataConnectionOk, false, mIsBluetoothConnect is connect.");
            return false;
        } else if (this.mIsAirPlaneModeOn) {
            log("isDataConnectionOk, false, air plane mode on.");
            return false;
        } else if (!isDualCardMode()) {
            log("isDataConnectionOk, false, is not dual card mode.");
            return false;
        } else if (this.mIsTetheringOn) {
            log("isDataConnectionOk, false, tethering on.");
            return false;
        } else if (this.mIsSecureVpnOn) {
            log("isDataConnectionOk, false, vpn on.");
            return false;
        } else if (hasMessages(6)) {
            log("isDataConnectionOk, false, is in dsda poor punish.");
            return false;
        } else if (!isMonthLimitTrafficOk()) {
            log("isDataConnectionOk, false, month traffic limit.");
            return false;
        } else if (this.mIsDsdaModeOn) {
            return true;
        } else {
            log("isDataConnectionOk, false, not dsda mode.");
            return false;
        }
    }

    public boolean isRoamingAndVsimOk() {
        if (isSlaveCardRoaming()) {
            log("isRoamingAndVsimOk, false, sub card is roaming.");
            return false;
        } else if (!isVsimEnabled()) {
            return true;
        } else {
            log("isRoamingAndVsimOk, false, VSim is enabled.");
            return false;
        }
    }

    private boolean canActiveDualCardPdn() {
        if (isIntelligenceCardOk() && isDataConnectionOk() && isRoamingAndVsimOk()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSystemStateChanged() {
        if (canActiveDualCardPdn()) {
            if (!this.isRegisterBoosterCallbackSuccess) {
                this.isRegisterBoosterCallbackSuccess = registerBoosterCallback();
            }
            activeDualCardPdn();
            return;
        }
        inactiveDualCardPdn();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onLostStateChanged() {
        if (canActiveDualCardPdn()) {
            inactiveDualCardPdn();
            activeDualCardPdn();
            return;
        }
        inactiveDualCardPdn();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onWifiNetworkStateChanged(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "onWifiNetworkStateChanged failed, null intent");
            return;
        }
        boolean isWifiConnected = false;
        NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (netInfo != null) {
            if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                isWifiConnected = false;
            }
            if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                isWifiConnected = true;
            }
            if (this.mIsWifiConnect != isWifiConnected) {
                this.mIsWifiConnect = isWifiConnected;
                StringBuilder sb = new StringBuilder();
                sb.append("onWifiNetworkStateChanged, mIsWifiConnect: ");
                sb.append(this.mIsWifiConnect ? SWITCH_ON : SWITCH_OFF);
                log(sb.toString());
                onSystemStateChanged();
            }
            log("onWifiNetworkStateChanged, netInfo.getState():" + netInfo.getState());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onBluetoothNetworkStateChanged(boolean isBluetoothConnected) {
        if (this.mIsBluetoothConnect != isBluetoothConnected) {
            this.mIsBluetoothConnect = isBluetoothConnected;
            StringBuilder sb = new StringBuilder();
            sb.append("mIsBluetoothConnect: ");
            sb.append(this.mIsBluetoothConnect ? SWITCH_ON : SWITCH_OFF);
            log(sb.toString());
            onSystemStateChanged();
        }
    }

    private boolean isSimOn(String iccState) {
        if ("IMSI".equals(iccState) || "READY".equals(iccState) || "LOADED".equals(iccState)) {
            return true;
        }
        return false;
    }

    private void refreshSimState(int slotId, String iccState) {
        if (!SmartDualCardUtil.isValidSlotId(slotId) || iccState == null) {
            log("slotId is invalid or iccState is null");
            return;
        }
        boolean newSimState = isSimOn(iccState);
        if (slotId == 0 && this.mIsSlotOnCard0 != newSimState) {
            this.mIsSlotOnCard0 = newSimState;
            log("refreshSimState, slot 0 state change. mIsSlotOnCard0 = " + this.mIsSlotOnCard0);
        } else if (slotId == 1 && this.mIsSlotOnCard1 != newSimState) {
            this.mIsSlotOnCard1 = newSimState;
            log("refreshSimState, slot 1 state change. mIsSlotOnCard1 = " + this.mIsSlotOnCard1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSimStateChanged(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "onSimStateChanged, failed, null intent");
            return;
        }
        int subId = intent.getIntExtra("subscription", -1);
        int slotId = SmartDualCardUtil.convertSubIdToSlotId(subId);
        String iccState = intent.getStringExtra("ss");
        log("onSimStateChanged, subId:" + subId + " iccState:" + iccState + " slotId:" + slotId);
        this.mIsNeedCheckMonthTrafficLimit = true;
        this.mIsNeedCheckPriorityAppDb = true;
        refreshSimState(slotId, iccState);
        removeMessages(6);
        onSystemStateChanged();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTetheringStateChanged(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "onTetheringStateChanged failed, null intent");
            return;
        }
        ArrayList<String> activeTetherIfaces = intent.getStringArrayListExtra("tetherArray");
        boolean isTetheringOn = activeTetherIfaces != null && activeTetherIfaces.size() > 0;
        if (this.mIsTetheringOn != isTetheringOn) {
            this.mIsTetheringOn = isTetheringOn;
            StringBuilder sb = new StringBuilder();
            sb.append("onTetheringStateChanged, mIsTetheringOn: ");
            sb.append(this.mIsTetheringOn ? SWITCH_ON : SWITCH_OFF);
            log(sb.toString());
            onSystemStateChanged();
        }
    }

    private void sendPunishMsgForDsdaOff() {
        if (this.mIsDsdaModeOn) {
            log("sendPunishMsgForDsdaOff, mIsDsdaModeOn=" + this.mIsDsdaModeOn);
        } else if (hasMessages(6)) {
            log("sendPunishMsgForDsdaOff, is in dsda punish and dsda state change.");
            removeMessages(6);
            sendMessageDelayed(obtainMessage(6), 180000);
        } else {
            long nowTimeStamp = SystemClock.elapsedRealtime();
            if (nowTimeStamp - this.mLastBuildDualLinkTime > 180000) {
                log("sendPunishMsgForDsdaOff, nowTimeStamp=" + nowTimeStamp + " mLastBuildDualLinkTime=" + this.mLastBuildDualLinkTime + " sDsdaBadCount=" + sDsdaBadCount);
                sDsdaBadCount = 0;
            } else if (sSlaveCardNetId != -1) {
                sDsdaBadCount++;
                log("sendPunishMsgForDsdaOff. nowTimeStamp=" + nowTimeStamp + ", mLastBuildDualLinkTime=" + this.mLastBuildDualLinkTime + "sDsdaBadCount=" + sDsdaBadCount);
                if (((long) sDsdaBadCount) >= DUAL_LINK_DSDA_POOR_COUNT_THRESHOLD) {
                    sendMessageDelayed(obtainMessage(6), 180000);
                    sDsdaBadCount = 0;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDsdaStateChange(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "onDsdaStateChange failed, null intent");
            return;
        }
        boolean isDsdaOn = intent.getBooleanExtra(SmartDualCardConsts.DUAL_PS_ALLOWED, false);
        if (this.mIsDsdaModeOn != isDsdaOn) {
            this.mIsDsdaModeOn = isDsdaOn;
            StringBuilder sb = new StringBuilder();
            sb.append("onDsdaStateChange, mIsDsdaModeOn: ");
            sb.append(this.mIsDsdaModeOn ? SWITCH_ON : SWITCH_OFF);
            log(sb.toString());
            sendPunishMsgForDsdaOff();
            onSystemStateChanged();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onConnectivityChange(Intent intent) {
        boolean isVpnOn;
        if (intent == null) {
            Log.e(TAG, "onConnectivityChange failed, null intent");
            return;
        }
        NetworkInfo info = (NetworkInfo) intent.getExtra("networkInfo", null);
        if (info != null && info.getType() == 17) {
            if (info.getState() == NetworkInfo.State.CONNECTED) {
                isVpnOn = true;
            } else {
                isVpnOn = false;
            }
            if (isVpnOn != this.mIsSecureVpnOn) {
                this.mIsSecureVpnOn = isVpnOn;
                StringBuilder sb = new StringBuilder();
                sb.append("onConnectivityChange, vpn state change. mIsSecureVpnOn: ");
                sb.append(this.mIsSecureVpnOn ? SWITCH_ON : SWITCH_OFF);
                log(sb.toString());
                onSystemStateChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserSwitched() {
        boolean isOwnerUser = isOwnerUser();
        if (isOwnerUser != this.mIsOwnerUser) {
            this.mIsOwnerUser = isOwnerUser;
            log("onUserSwitched, mIsOwnerUser: " + this.mIsOwnerUser);
            onSystemStateChanged();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserChooseSwitchback(boolean isUserChooseSwitchBack) {
        if (this.mIsUserChooseSwitchBack != isUserChooseSwitchBack) {
            this.mIsUserChooseSwitchBack = isUserChooseSwitchBack;
            log("onUserChooseSwitchback, mIsUserChooseSwitchBack: " + this.mIsUserChooseSwitchBack);
            onSystemStateChanged();
        }
    }

    private void onAirPlaneModeChanged() {
        boolean isAirPlaneModeOn = this.mHwSettingsObserver.isAirPlaneModeSwitchOn();
        if (this.mIsAirPlaneModeOn != isAirPlaneModeOn) {
            this.mIsAirPlaneModeOn = isAirPlaneModeOn;
            StringBuilder sb = new StringBuilder();
            sb.append("onAirPlaneModeChanged, mIsAirPlaneModeOn: ");
            sb.append(this.mIsAirPlaneModeOn ? SWITCH_ON : SWITCH_OFF);
            log(sb.toString());
            onSystemStateChanged();
            removeMessages(6);
        }
    }

    private void onMobileDataChanged() {
        boolean isMobileDataOn = this.mHwSettingsObserver.isMobileDataSwitchOn();
        if (this.mIsMobileDataOn != isMobileDataOn) {
            this.mIsMobileDataOn = isMobileDataOn;
            StringBuilder sb = new StringBuilder();
            sb.append("onMobileDataChanged, mIsMobileDataOn: ");
            sb.append(this.mIsMobileDataOn ? SWITCH_ON : SWITCH_OFF);
            log(sb.toString());
            onSystemStateChanged();
            removeMessages(6);
        }
    }

    private void onIntelligenceCardChanged() {
        boolean isIntelligenceCardOn = this.mHwSettingsObserver.isIntelligenceCardSwitchOn();
        if (this.mIsIntelligenceCardOn != isIntelligenceCardOn) {
            this.mIsIntelligenceCardOn = isIntelligenceCardOn;
            StringBuilder sb = new StringBuilder();
            sb.append("onIntelligenceCardChanged, mIsIntelligenceCardOn: ");
            sb.append(this.mIsIntelligenceCardOn ? SWITCH_ON : SWITCH_OFF);
            log(sb.toString());
            onSystemStateChanged();
            removeMessages(6);
        }
    }

    private void onSettingFreeAppChange() {
        boolean isPriorityAppListEmpty = getPriorityAppState();
        if (this.mIsPriorityAppListEmpty != isPriorityAppListEmpty) {
            this.mIsPriorityAppListEmpty = isPriorityAppListEmpty;
            StringBuilder sb = new StringBuilder();
            sb.append("onSettingFreeAppChange, mIsPriorityAppListEmpty: ");
            sb.append(this.mIsPriorityAppListEmpty ? "empty" : "not empty");
            log(sb.toString());
            onSystemStateChanged();
        }
    }

    private void onMonthLimitChange() {
        boolean isMonthTrafficLimitOk = initMonthLimitTrafficState();
        if (this.mIsMonthTrafficLimitOk != isMonthTrafficLimitOk) {
            this.mIsMonthTrafficLimitOk = isMonthTrafficLimitOk;
            log("onMonthLimitChange, mIsMonthTrafficLimitOk: " + this.mIsMonthTrafficLimitOk);
            onSystemStateChanged();
        }
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        if (msg == null) {
            log("handle msg is null");
            return;
        }
        switch (msg.what) {
            case 0:
                onAirPlaneModeChanged();
                return;
            case 1:
                onMobileDataChanged();
                return;
            case 2:
                onIntelligenceCardChanged();
                return;
            case 3:
            case 7:
                onSettingFreeAppChange();
                return;
            case 4:
                onMonthLimitChange();
                return;
            case 5:
                onUserChooseSwitchback(false);
                return;
            case 6:
                log("handleMessage, EVENT_DSDA_POOR_PUNISH_TIMEOUT");
                onSystemStateChanged();
                return;
            default:
                log("handleMessage, can not deal this type, msg: " + msg);
                return;
        }
    }

    private boolean isDualCardMode() {
        return this.mIsSlotOnCard0 && this.mIsSlotOnCard1;
    }

    private boolean isPriorityAppListEmpty() {
        if (this.mIsNeedCheckPriorityAppDb) {
            log("need check free traffic app from db");
            this.mIsPriorityAppListEmpty = getPriorityAppState();
        }
        return this.mIsPriorityAppListEmpty;
    }

    private boolean isSlaveCardRoaming() {
        return TelephonyManager.getDefault().isNetworkRoaming(SmartDualCardUtil.getSlaveCardSubId());
    }

    private boolean isVsimEnabled() {
        return HwTelephonyManager.getDefault().isVSimOn();
    }

    private boolean isDataConnected(int networkType) {
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        if (connectivityManager == null) {
            log("mConnectivityManager is null");
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(networkType);
        if (networkInfo != null) {
            return networkInfo.isConnected();
        }
        log("networkInfo is null");
        return false;
    }

    private boolean isOwnerUser() {
        if (UserManager.get(this.mContext) != null && ActivityManager.getCurrentUser() == 0) {
            return true;
        }
        return false;
    }

    private boolean initMonthLimitTrafficState() {
        TelephonyManager telephonyManager = TelephonyManager.from(this.mContext);
        if (telephonyManager == null) {
            Log.e(TAG, "onMonthLimitChange:mTelephonyManager is null");
            return false;
        }
        String slaveImsi = telephonyManager.getSubscriberId(SmartDualCardUtil.getSlaveCardSubId());
        long monthLimit = this.mHwSettingsObserver.getMonthLimitTraffic(slaveImsi);
        if (monthLimit != -1 && monthLimit - this.mHwSettingsObserver.getMonthTrafficConsumption(slaveImsi) <= 52428800) {
            return false;
        }
        return true;
    }

    private boolean isMonthLimitTrafficOk() {
        if (this.mIsNeedCheckMonthTrafficLimit) {
            this.mIsMonthTrafficLimitOk = initMonthLimitTrafficState();
            this.mIsNeedCheckMonthTrafficLimit = false;
        }
        return this.mIsMonthTrafficLimitOk;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean registerBoosterCallback() {
        if (this.boosterServiceManager == null) {
            this.boosterServiceManager = HwFrameworkFactory.getHwCommBoosterServiceManager();
        }
        IHwCommBoosterServiceManager iHwCommBoosterServiceManager = this.boosterServiceManager;
        if (iHwCommBoosterServiceManager != null) {
            int ret = iHwCommBoosterServiceManager.registerCallBack("com.android.server.intellicom.common", this.mHwCommBoosterCallback);
            if (ret == 0) {
                return true;
            }
            log("registerBoosterCallback:registerCallBack failed, ret=" + ret);
            return false;
        }
        log("registerBoosterCallback:null HwCommBoosterServiceManager");
        return false;
    }

    private void registerBluetoothCallback() {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addTransportType(2);
        this.mConnectivityManager.registerNetworkCallback(builder.build(), this.mBluetoothCallBack);
    }

    private boolean getSmartCardDisplayProp() {
        return SystemProperties.getBoolean("hw_mc.smartcard2.display", false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String msg) {
        Log.i(TAG, msg);
    }
}
