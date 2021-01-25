package com.android.server.wifi.hwcoex;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.WifiDisplayStatus;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.os.Handler;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import com.android.server.hidata.appqoe.HwAPPQoEResourceManger;
import com.android.server.wifi.HiCoexManager;
import com.android.server.wifi.hwcoex.HiCoexReceiver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class HiCoexReceiver {
    private static final int CHAN_INITIAL_CAPACITY = 3;
    private static final String MPLINK_STATE_CHANGED_BROADCAST = "com.huawei.systemserver.emcom.policycenter.policycenterservice.decisionsystem.MpLinkDecisionSystem";
    private static final int STATE_INITIAL_CAPACITY = 5;
    private static final String TAG = "HiCoexRecevier";
    private ActivityManager mAM = null;
    private int mActiveNetworkType = HiCoexUtils.NETWORK_UNKNOWN;
    private ConnectivityManager mCM = null;
    private CoexBcastReceiver mCoexBcastReceiver = null;
    private Context mContext;
    private int mDefaultSubId = -1;
    private int mForegroundNetworkType = HiCoexUtils.NETWORK_UNKNOWN;
    private Handler mHandler;
    private List<HiCoexCellularState> mHiCoexCellStates = new ArrayList(5);
    private ConnectivityManager.NetworkCallback mHiCoexNetworkCallback;
    private HwProcessObserver mHwProcessObserver;
    private boolean mIsWifiConnected = false;
    private final Object mLockObj = new Object();
    private Map<Integer, PhoneStateListener> mPhoneStateListeners = null;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener = null;
    private SubscriptionManager mSubscriptionManager = null;
    private TelephonyManager mTelephonyManager = null;

    public HiCoexReceiver(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mPhoneStateListeners = new ArrayMap(5);
        this.mAM = (ActivityManager) this.mContext.getSystemService("activity");
        this.mCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
    }

    public void startMonitor() {
        registerBroadcastReceiver();
        registerSubscriptionsChangedListener();
        registerNetworkChangeCallback();
        registerProcessObserver();
    }

    public void stopMonitor() {
        unregisterProcessObserver();
        unregisterNetworkChangeCallback();
        unregisterSubscriptionsChangedListener();
        unregisterReceiver();
    }

    public List<HiCoexCellularState> getHiCoexCellStates() {
        List<HiCoexCellularState> list;
        synchronized (this.mLockObj) {
            list = new ArrayList<>(this.mHiCoexCellStates.size());
            list.addAll(this.mHiCoexCellStates);
        }
        return list;
    }

    public List<Integer> getRecommendWiFiChannel() {
        int channel;
        if (!HiCoexUtils.isDebugEnable() || (channel = HiCoexDebugger.getRecommendWiFiChannel()) <= 0) {
            synchronized (this.mLockObj) {
                for (HiCoexCellularState cellularState : this.mHiCoexCellStates) {
                    List<Integer> channels = cellularState.getRecommendWiFiChannel();
                    if (channels != null && channels.size() > 0) {
                        return channels;
                    }
                }
                return null;
            }
        }
        List<Integer> channels2 = new ArrayList<>(3);
        channels2.add(new Integer(channel));
        return channels2;
    }

    public List<Integer> getDeprecatedWiFiChannel() {
        synchronized (this.mLockObj) {
            for (HiCoexCellularState cellularState : this.mHiCoexCellStates) {
                List<Integer> channels = cellularState.getDeprecatedWiFiChannel();
                if (channels != null && channels.size() > 0) {
                    return channels;
                }
            }
            return Collections.emptyList();
        }
    }

    public boolean isWifiConnected() {
        return this.mIsWifiConnected;
    }

    public int getActiveNetworkType() {
        return this.mActiveNetworkType;
    }

    public int getForegroundNetworkType() {
        return this.mForegroundNetworkType;
    }

    public boolean isNrNetwork() {
        return isNrNetwork(this.mDefaultSubId);
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        filter.addAction("com.huawei.android.net.wifi.p2p.action.WIFI_RPT_STATE_CHANGED");
        filter.addAction("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED");
        filter.addAction("com.android.server.hidata.arbitration.HwArbitrationStateMachine");
        filter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        filter.addAction(MPLINK_STATE_CHANGED_BROADCAST);
        this.mCoexBcastReceiver = new CoexBcastReceiver();
        this.mContext.registerReceiver(this.mCoexBcastReceiver, filter, "com.huawei.hidata.permission.MPLINK_START_CHECK", null);
    }

    private void unregisterReceiver() {
        CoexBcastReceiver coexBcastReceiver = this.mCoexBcastReceiver;
        if (coexBcastReceiver != null) {
            this.mContext.unregisterReceiver(coexBcastReceiver);
            this.mCoexBcastReceiver = null;
        }
    }

    /* access modifiers changed from: private */
    public class CoexBcastReceiver extends BroadcastReceiver {
        private boolean mIsDisplayEnabled;
        private boolean mIsP2PConnected;
        private boolean mIsRPTEnabled;

        private CoexBcastReceiver() {
            this.mIsP2PConnected = false;
            this.mIsRPTEnabled = false;
            this.mIsDisplayEnabled = false;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    handleP2pConnectionChanged(intent);
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    handleNetworkStateChanged(intent);
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    handleWiFiStateChanged(intent);
                } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                    handleWiFiAPStateChanged(intent);
                } else if ("com.huawei.android.net.wifi.p2p.action.WIFI_RPT_STATE_CHANGED".equals(action)) {
                    handleWifiBridgeStateChanged(intent);
                } else if ("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED".equals(action)) {
                    handleWifiDisplayStateChanged(intent);
                } else if ("com.android.server.hidata.arbitration.HwArbitrationStateMachine".equals(action)) {
                    handleMplinkStateChanged(intent);
                } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                    handleSupplicantStateChanged(intent);
                } else if (HiCoexReceiver.MPLINK_STATE_CHANGED_BROADCAST.equals(action)) {
                    HiCoexUtils.logD(HiCoexReceiver.TAG, "MPLINK_STATE_CHANGED_BROADCAST received");
                    handleMplinkStateChanged(intent);
                } else {
                    HiCoexUtils.logE(HiCoexReceiver.TAG, "CoexBcastReceiver unknown action:" + action);
                }
            }
        }

        private void handleP2pConnectionChanged(Intent intent) {
            NetworkInfo p2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            if (p2pNetworkInfo != null) {
                boolean isP2pConnected = p2pNetworkInfo.isConnected();
                HiCoexUtils.logD(HiCoexReceiver.TAG, "handleP2pConnectionChanged: " + isP2pConnected);
                if (this.mIsP2PConnected != isP2pConnected) {
                    this.mIsP2PConnected = isP2pConnected;
                    HiCoexReceiver.this.mHandler.sendMessage(HiCoexReceiver.this.mHandler.obtainMessage(16, HiCoexUtils.booleanToInt(isP2pConnected), 0));
                }
            }
        }

        private void handleWifiBridgeStateChanged(Intent intent) {
            int bridgeState = intent.getIntExtra("wifi_rpt_state", -1);
            HiCoexUtils.logD(HiCoexReceiver.TAG, "handleWifiBridgeStateChanged state = " + bridgeState);
            boolean isRptEnabled = true;
            if (bridgeState != 1) {
                isRptEnabled = false;
            }
            if (this.mIsRPTEnabled != isRptEnabled) {
                this.mIsRPTEnabled = isRptEnabled;
                HiCoexReceiver.this.mHandler.sendMessage(HiCoexReceiver.this.mHandler.obtainMessage(17, HiCoexUtils.booleanToInt(isRptEnabled), 0));
            }
        }

        private void handleWifiDisplayStateChanged(Intent intent) {
            WifiDisplayStatus status = intent.getParcelableExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS");
            if (status != null) {
                int state = status.getActiveDisplayState();
                boolean isDisplayEnabled = state == 2;
                HiCoexUtils.logD(HiCoexReceiver.TAG, "handleWifiDisplayStateChanged state = " + state);
                if (this.mIsDisplayEnabled != isDisplayEnabled) {
                    this.mIsDisplayEnabled = isDisplayEnabled;
                    HiCoexReceiver.this.mHandler.sendMessage(HiCoexReceiver.this.mHandler.obtainMessage(18, HiCoexUtils.booleanToInt(isDisplayEnabled), 0));
                }
            }
        }

        private void handleNetworkStateChanged(Intent intent) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            NetworkInfo.DetailedState state = info == null ? NetworkInfo.DetailedState.IDLE : info.getDetailedState();
            if (state == NetworkInfo.DetailedState.CONNECTED) {
                HiCoexManager hiCoexManager = HiCoexManagerImpl.getInstance();
                if (hiCoexManager != null) {
                    hiCoexManager.notifyWifiConnecting(false);
                }
                if (!HiCoexReceiver.this.mIsWifiConnected) {
                    HiCoexUtils.logD(HiCoexReceiver.TAG, "handleNetworkStateChanged:MSG_WIFI_CONNECTED");
                    HiCoexReceiver.this.mHandler.sendEmptyMessage(4);
                }
                HiCoexReceiver.this.mIsWifiConnected = true;
            } else if (state == NetworkInfo.DetailedState.DISCONNECTED) {
                if (HiCoexReceiver.this.mIsWifiConnected) {
                    HiCoexUtils.logD(HiCoexReceiver.TAG, "handleNetworkStateChanged:MSG_WIFI_DISCONNECTED");
                    HiCoexReceiver.this.mHandler.sendEmptyMessage(7);
                }
                HiCoexReceiver.this.mIsWifiConnected = false;
            } else {
                HiCoexUtils.logD(HiCoexReceiver.TAG, "handleNetworkStateChanged state:" + state);
            }
        }

        private void handleWiFiStateChanged(Intent intent) {
            int state = intent.getIntExtra("wifi_state", 4);
            if (state == 3) {
                HiCoexReceiver.this.mIsWifiConnected = false;
                HiCoexReceiver.this.mHandler.sendEmptyMessage(2);
            } else if (state == 1) {
                HiCoexReceiver.this.mIsWifiConnected = false;
                HiCoexReceiver.this.mHandler.sendEmptyMessage(3);
            } else {
                HiCoexUtils.logV(HiCoexReceiver.TAG, "handleWiFiStateChanged state:" + state);
            }
        }

        private void handleWiFiAPStateChanged(Intent intent) {
            int apState = intent.getIntExtra("wifi_state", 11);
            if (apState == 13) {
                HiCoexReceiver.this.mHandler.sendEmptyMessage(5);
            } else if (apState == 11) {
                HiCoexReceiver.this.mHandler.sendEmptyMessage(6);
            } else {
                HiCoexUtils.logV(HiCoexReceiver.TAG, "handleWiFiAPStateChanged state:" + apState);
            }
        }

        private void handleMplinkStateChanged(Intent intent) {
            int network = intent.getIntExtra("MPLinkSuccessNetworkKey", HiCoexUtils.NETWORK_UNKNOWN);
            if (network == 801) {
                if (HiCoexReceiver.this.mActiveNetworkType == 800) {
                    HiCoexReceiver.this.mForegroundNetworkType = network;
                } else if (HiCoexReceiver.this.mActiveNetworkType == 801) {
                    HiCoexReceiver.this.mForegroundNetworkType = HiCoexUtils.NETWORK_UNKNOWN;
                } else {
                    HiCoexUtils.logE(HiCoexReceiver.TAG, "handleMplinkStateChanged active network:" + HiCoexReceiver.this.mActiveNetworkType);
                }
            } else if (network != 800) {
                HiCoexUtils.logE(HiCoexReceiver.TAG, "handleMplinkStateChanged network:" + network);
            } else if (HiCoexReceiver.this.mActiveNetworkType == 800) {
                HiCoexReceiver.this.mForegroundNetworkType = HiCoexUtils.NETWORK_UNKNOWN;
            } else if (HiCoexReceiver.this.mActiveNetworkType == 801) {
                HiCoexReceiver.this.mForegroundNetworkType = network;
            } else {
                HiCoexUtils.logE(HiCoexReceiver.TAG, "handleMplinkStateChanged active network:" + HiCoexReceiver.this.mActiveNetworkType);
            }
            HiCoexReceiver.this.mHandler.sendEmptyMessage(19);
        }

        private void handleSupplicantStateChanged(Intent intent) {
            SupplicantState state = (SupplicantState) intent.getParcelableExtra("newState");
            if (state == null) {
                HiCoexUtils.logE(HiCoexReceiver.TAG, "handleSupplicantStateChanged SupplicantState:null");
            } else if (state == SupplicantState.DISCONNECTED) {
                HiCoexManager hiCoexManager = HiCoexManagerImpl.getInstance();
                if (hiCoexManager != null) {
                    hiCoexManager.notifyWifiConnecting(false);
                }
            } else if (state == SupplicantState.ASSOCIATING || state == SupplicantState.ASSOCIATED) {
                HiCoexManager hiCoexManager2 = HiCoexManagerImpl.getInstance();
                if (hiCoexManager2 != null) {
                    hiCoexManager2.notifyWifiConnecting(true);
                }
            } else {
                HiCoexUtils.logD(HiCoexReceiver.TAG, "SupplicantState:" + state);
            }
        }
    }

    /* access modifiers changed from: private */
    public class HiCoexPhoneStateListener extends PhoneStateListener {
        HiCoexPhoneStateListener(int subId) {
            super(Integer.valueOf(subId));
        }

        @Override // android.telephony.PhoneStateListener
        public void onServiceStateChanged(ServiceState state) {
            int subId = this.mSubId.intValue();
            if (!HiCoexUtils.isSubIdValid(subId)) {
                HiCoexUtils.logE(HiCoexReceiver.TAG, "onServiceStateChanged: invalid mSubId = " + subId);
                return;
            }
            synchronized (HiCoexReceiver.this.mLockObj) {
                for (HiCoexCellularState coexCellState : HiCoexReceiver.this.mHiCoexCellStates) {
                    if (coexCellState.getSubId() == subId) {
                        coexCellState.updateServiceState(state);
                        if (coexCellState.hasSceneChanged()) {
                            HiCoexReceiver.this.mHandler.sendEmptyMessage(8);
                        }
                    }
                }
            }
        }

        @Override // android.telephony.PhoneStateListener
        public void onDataConnectionStateChanged(int state, int networkType) {
            if (state == 0) {
                HiCoexUtils.logD(HiCoexReceiver.TAG, "DATA_DISCONNECTED:subid:" + this.mSubId + ", mDefaultSubId:" + HiCoexReceiver.this.mDefaultSubId);
                if (HiCoexReceiver.this.mDefaultSubId == this.mSubId.intValue()) {
                    if (HiCoexReceiver.this.mActiveNetworkType == 801) {
                        HiCoexReceiver.this.mActiveNetworkType = HiCoexUtils.NETWORK_UNKNOWN;
                    }
                    if (HiCoexReceiver.this.mForegroundNetworkType == 801) {
                        HiCoexReceiver.this.mForegroundNetworkType = HiCoexUtils.NETWORK_UNKNOWN;
                    }
                }
                HiCoexReceiver.this.updateDataConnection(this.mSubId.intValue(), 2);
            }
        }

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (state == 0) {
                HiCoexUtils.logD(HiCoexReceiver.TAG, "onCallStateChanged: " + this.mSubId + " CALL_STATE_IDLE");
                HiCoexReceiver.this.mHandler.sendEmptyMessage(22);
            } else if (state == 2) {
                HiCoexUtils.logD(HiCoexReceiver.TAG, "onCallStateChanged: " + this.mSubId + " CALL_STATE_OFFHOOK");
                if (HiCoexReceiver.this.isSaNetwork(this.mSubId.intValue())) {
                    HiCoexReceiver.this.mHandler.sendEmptyMessage(23);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class HwProcessObserver extends IProcessObserver.Stub {
        boolean mIsLowLatencyActivity;

        private HwProcessObserver() {
            this.mIsLowLatencyActivity = false;
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (!HiCoexReceiver.this.isConnected()) {
                HiCoexUtils.logV(HiCoexReceiver.TAG, "network disconnected, ignore FGActivitiesChanged");
            } else if (foregroundActivities) {
                boolean isLowLatencyActivity = false;
                String currentAppName = HiCoexReceiver.this.getAppNameUid(uid);
                HwAPPQoEResourceManger qoeResourceManger = HwAPPQoEResourceManger.getInstance();
                if (!(qoeResourceManger == null || qoeResourceManger.checkIsMonitorGameScence(currentAppName) == null)) {
                    HiCoexUtils.logE(HiCoexReceiver.TAG, "switch to foreground: " + currentAppName);
                    isLowLatencyActivity = true;
                }
                if (isLowLatencyActivity != this.mIsLowLatencyActivity) {
                    this.mIsLowLatencyActivity = isLowLatencyActivity;
                    HiCoexUtils.logE(HiCoexReceiver.TAG, "MSG_LOWLATENCY_CHANGED: " + isLowLatencyActivity);
                    HiCoexReceiver.this.mHandler.sendMessage(HiCoexReceiver.this.mHandler.obtainMessage(21, HiCoexUtils.booleanToInt(isLowLatencyActivity), 0));
                }
            }
        }

        public void onProcessDied(int pid, int uid) {
        }

        public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) {
        }
    }

    private void registerSubscriptionsChangedListener() {
        if (this.mTelephonyManager == null || this.mSubscriptionManager == null) {
            HiCoexUtils.logE(TAG, "register: mTelephonyManager or mSubscriptionManager is null");
            return;
        }
        if (this.mSubscriptionListener == null) {
            this.mSubscriptionListener = new CoexSubscriptionListener();
        }
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
    }

    private void unregisterSubscriptionsChangedListener() {
        SubscriptionManager subscriptionManager;
        if (this.mTelephonyManager == null || (subscriptionManager = this.mSubscriptionManager) == null) {
            HiCoexUtils.logE(TAG, "unregister: mTelephonyManager or mSubscriptionManager is null");
            return;
        }
        SubscriptionManager.OnSubscriptionsChangedListener onSubscriptionsChangedListener = this.mSubscriptionListener;
        if (onSubscriptionsChangedListener != null) {
            subscriptionManager.removeOnSubscriptionsChangedListener(onSubscriptionsChangedListener);
        }
        for (Map.Entry<Integer, PhoneStateListener> entry : this.mPhoneStateListeners.entrySet()) {
            this.mTelephonyManager.listen(entry.getValue(), 0);
        }
        this.mPhoneStateListeners.clear();
        synchronized (this.mLockObj) {
            this.mHiCoexCellStates.clear();
        }
    }

    /* access modifiers changed from: private */
    public class CoexSubscriptionListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        private CoexSubscriptionListener() {
        }

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            List<SubscriptionInfo> subInfos = HiCoexReceiver.this.mSubscriptionManager.getActiveSubscriptionInfoList();
            removeDeactivateSubIdListeners(subInfos);
            addActiveSubIdListeners(subInfos);
        }

        private void removeDeactivateSubIdListeners(List<SubscriptionInfo> subInfos) {
            List<Integer> subIdList = new ArrayList<>(HiCoexReceiver.this.mPhoneStateListeners.keySet());
            for (int subIdIdx = subIdList.size() - 1; subIdIdx >= 0; subIdIdx--) {
                int subId = subIdList.get(subIdIdx).intValue();
                if (!containSubId(subInfos, subId)) {
                    HiCoexUtils.logD(HiCoexReceiver.TAG, "removeDeactivateSubIdListeners, remove subId:" + subId);
                    HiCoexReceiver.this.mTelephonyManager.listen((PhoneStateListener) HiCoexReceiver.this.mPhoneStateListeners.get(Integer.valueOf(subId)), 0);
                    HiCoexReceiver.this.mPhoneStateListeners.remove(Integer.valueOf(subId));
                    synchronized (HiCoexReceiver.this.mLockObj) {
                        HiCoexReceiver.this.mHiCoexCellStates.removeIf(new Predicate(subId) {
                            /* class com.android.server.wifi.hwcoex.$$Lambda$HiCoexReceiver$CoexSubscriptionListener$wACObINjq94F6TwSK1C4n8iMg4 */
                            private final /* synthetic */ int f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.util.function.Predicate
                            public final boolean test(Object obj) {
                                return HiCoexReceiver.CoexSubscriptionListener.lambda$removeDeactivateSubIdListeners$0(this.f$0, (HiCoexCellularState) obj);
                            }
                        });
                    }
                }
            }
        }

        static /* synthetic */ boolean lambda$removeDeactivateSubIdListeners$0(int subId, HiCoexCellularState cellularState) {
            return cellularState.getSubId() == subId;
        }

        private void addActiveSubIdListeners(List<SubscriptionInfo> subInfos) {
            if (subInfos == null) {
                HiCoexUtils.logE(HiCoexReceiver.TAG, "addActiveSubIdListeners, subscriptions is null");
                return;
            }
            for (SubscriptionInfo subInfo : subInfos) {
                int subId = subInfo.getSubscriptionId();
                if (!HiCoexReceiver.this.mPhoneStateListeners.containsKey(Integer.valueOf(subId))) {
                    if (!HiCoexUtils.isActiveSubId(subId)) {
                        HiCoexUtils.logE(HiCoexReceiver.TAG, "addActiveSubIdListeners invalid subId:" + subId);
                    } else {
                        HiCoexUtils.logD(HiCoexReceiver.TAG, "addActiveSubIdListeners, add subId:" + subId);
                        synchronized (HiCoexReceiver.this.mLockObj) {
                            HiCoexReceiver.this.mHiCoexCellStates.add(new HiCoexCellularState(subId, HiCoexReceiver.this.mContext));
                        }
                        PhoneStateListener listener = HiCoexReceiver.this.getPhoneStateListener(subId);
                        HiCoexReceiver.this.mTelephonyManager.listen(listener, 97);
                        HiCoexReceiver.this.mPhoneStateListeners.put(Integer.valueOf(subId), listener);
                    }
                }
            }
        }

        private boolean containSubId(List<SubscriptionInfo> subInfos, int subId) {
            if (subInfos == null) {
                HiCoexUtils.logE(HiCoexReceiver.TAG, "containSubId fail, subInfos is null");
                return false;
            } else if (!HiCoexUtils.isSubIdValid(subId)) {
                HiCoexUtils.logE(HiCoexReceiver.TAG, "containSubId fail, invalid subId:" + subId);
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
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private PhoneStateListener getPhoneStateListener(int i) {
        return new HiCoexPhoneStateListener(i);
    }

    private void registerProcessObserver() {
        this.mHwProcessObserver = new HwProcessObserver();
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mHwProcessObserver);
        } catch (RemoteException e) {
            HiCoexUtils.logE(TAG, "register process observer failed");
        }
    }

    private void unregisterProcessObserver() {
        if (this.mHwProcessObserver == null) {
            HiCoexUtils.logE(TAG, "mHwProcessObserver is null, return");
            return;
        }
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.mHwProcessObserver);
        } catch (RemoteException e) {
            HiCoexUtils.logE(TAG, "unregister process observer failed");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getAppNameUid(int uid) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList;
        ActivityManager activityManager = this.mAM;
        if (activityManager == null || (appProcessList = activityManager.getRunningAppProcesses()) == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.uid == uid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isConnected() {
        return this.mActiveNetworkType != 802;
    }

    private void registerNetworkChangeCallback() {
        if (this.mCM != null) {
            this.mHiCoexNetworkCallback = new NrNetworkCallback();
            this.mCM.registerDefaultNetworkCallback(this.mHiCoexNetworkCallback, this.mHandler);
        }
    }

    private void unregisterNetworkChangeCallback() {
        ConnectivityManager.NetworkCallback networkCallback;
        ConnectivityManager connectivityManager = this.mCM;
        if (connectivityManager != null && (networkCallback = this.mHiCoexNetworkCallback) != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    /* access modifiers changed from: private */
    public class NrNetworkCallback extends ConnectivityManager.NetworkCallback {
        private Network mDefaultNetwork;
        private Network mLastNetwork;
        private NetworkCapabilities mLastNetworkCapabilities;

        private NrNetworkCallback() {
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            if (network == null || networkCapabilities == null) {
                HiCoexUtils.logE(HiCoexReceiver.TAG, "network or networkCapabilities is null");
                return;
            }
            boolean isValidated = networkCapabilities.hasCapability(16);
            if (this.mLastNetworkCapabilities != null && network.equals(this.mLastNetwork)) {
                boolean isLastValidated = this.mLastNetworkCapabilities.hasCapability(16);
                if (networkCapabilities.equalsTransportTypes(this.mLastNetworkCapabilities) && isValidated == isLastValidated) {
                    HiCoexUtils.logD(HiCoexReceiver.TAG, "capabilities and validated equal last network");
                    return;
                }
            }
            this.mLastNetwork = network;
            this.mLastNetworkCapabilities = networkCapabilities;
            if (networkCapabilities.hasTransport(1) && isValidated) {
                HiCoexUtils.logD(HiCoexReceiver.TAG, "networkType:TRANSPORT_WIFI, network: " + network.toString());
                if (HiCoexReceiver.this.mActiveNetworkType == 801) {
                    HiCoexReceiver hiCoexReceiver = HiCoexReceiver.this;
                    hiCoexReceiver.updateDataConnection(hiCoexReceiver.mDefaultSubId, 2);
                }
                this.mDefaultNetwork = network;
                HiCoexReceiver.this.mActiveNetworkType = HiCoexUtils.NETWORK_WIFI;
            } else if (networkCapabilities.hasTransport(0) && isValidated) {
                HiCoexReceiver hiCoexReceiver2 = HiCoexReceiver.this;
                hiCoexReceiver2.mDefaultSubId = hiCoexReceiver2.getDataSubscriptionId();
                HiCoexUtils.logD(HiCoexReceiver.TAG, "networkType:TRANSPORT_CELLULAR, network: " + network.toString());
                this.mDefaultNetwork = network;
                HiCoexReceiver.this.mActiveNetworkType = HiCoexUtils.NETWORK_CELL;
                HiCoexReceiver hiCoexReceiver3 = HiCoexReceiver.this;
                hiCoexReceiver3.updateDataConnection(hiCoexReceiver3.mDefaultSubId, 1);
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            HiCoexUtils.logD(HiCoexReceiver.TAG, "onLost");
            if (network == null || !network.equals(this.mDefaultNetwork)) {
                HiCoexUtils.logD(HiCoexReceiver.TAG, "lost network is null or not equal to defaultNetwork");
                return;
            }
            HiCoexUtils.logD(HiCoexReceiver.TAG, "onLost preType is:" + HiCoexReceiver.this.mActiveNetworkType + ", network:" + network.toString());
            if (HiCoexReceiver.this.mActiveNetworkType == 801) {
                HiCoexUtils.logD(HiCoexReceiver.TAG, "onNetworkCallback:MSG_CELL_STATE_DISCONNECT");
                HiCoexReceiver hiCoexReceiver = HiCoexReceiver.this;
                hiCoexReceiver.updateDataConnection(hiCoexReceiver.mDefaultSubId, 2);
            } else if (HiCoexReceiver.this.mActiveNetworkType == 800) {
                HiCoexUtils.logD(HiCoexReceiver.TAG, "onNetworkCallback:MSG_WIFI_STATE_DISCONNECT");
            }
            HiCoexReceiver.this.mActiveNetworkType = HiCoexUtils.NETWORK_UNKNOWN;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDataConnection(int subId, int state) {
        boolean isChanged = false;
        synchronized (this.mLockObj) {
            Iterator<HiCoexCellularState> it = this.mHiCoexCellStates.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                HiCoexCellularState cellularState = it.next();
                if (cellularState.getSubId() == subId) {
                    cellularState.updateDataConnection(state);
                    HiCoexUtils.logD(TAG, "updateDataConnection:" + state);
                    if (cellularState.hasSceneChanged()) {
                        isChanged = true;
                        break;
                    }
                }
            }
        }
        if (isChanged) {
            this.mHandler.sendEmptyMessage(8);
            this.mHandler.sendEmptyMessage(19);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDataSubscriptionId() {
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (subId == -1) {
            subId = SubscriptionManager.getDefaultSubscriptionId();
            HiCoexUtils.logD(TAG, " no default DATA sub: " + subId);
        }
        if (subId == -1) {
            HiCoexUtils.logD(TAG, " no default sub: " + subId);
        }
        return subId;
    }

    private boolean isNrNetwork(int subId) {
        synchronized (this.mLockObj) {
            for (HiCoexCellularState cellularState : this.mHiCoexCellStates) {
                if (cellularState.getSubId() == subId) {
                    return cellularState.isNrNetwork();
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSaNetwork(int subId) {
        synchronized (this.mLockObj) {
            Iterator<HiCoexCellularState> it = this.mHiCoexCellStates.iterator();
            while (true) {
                boolean z = false;
                if (!it.hasNext()) {
                    return false;
                }
                HiCoexCellularState cellularState = it.next();
                if (cellularState.getSubId() == subId) {
                    if (cellularState.getNetworkType() == 2) {
                        z = true;
                    }
                    return z;
                }
            }
        }
    }
}
