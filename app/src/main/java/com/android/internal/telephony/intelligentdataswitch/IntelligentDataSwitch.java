package com.android.internal.telephony.intelligentdataswitch;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings.Global;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.vsim.HwVSimSlotSwitchController;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class IntelligentDataSwitch {
    private static final String TAG = "IntelligentDataSwitch";
    AirPlaneModeObserver mAirPlaneModeObserver;
    private DataState[] mApnState;
    private Context mContext;
    private IDSManager mDataSwitchManager;
    private boolean[] mFristTimeReceApnState;
    private Handler mIdsHandler;
    private PhoneStateListener[] mPhoneStateListener;
    private ContentResolver mResolver;
    UserDataEnableObserver mUserDataEnableObserver;

    /* renamed from: com.android.internal.telephony.intelligentdataswitch.IntelligentDataSwitch.1 */
    class AnonymousClass1 extends PhoneStateListener {
        AnonymousClass1(int $anonymous0) {
            super($anonymous0);
        }

        public void onServiceStateChanged(ServiceState state) {
            if (state == null) {
                IntelligentDataSwitch.this.loge("SlotStateListener onServiceStateChanged: state is null,return");
            } else if (IntelligentDataSwitch.this.isSlotIdValid(this.mSubId)) {
                IntelligentDataSwitch.this.mIdsHandler.sendMessage(IntelligentDataSwitch.this.mIdsHandler.obtainMessage(6, this.mSubId, 0, state));
                IntelligentDataSwitch.this.logd("SlotStateListener onServiceStateChanged on mSubId = " + this.mSubId + ", ServiceState is: " + state);
            } else {
                IntelligentDataSwitch.this.loge("SlotStateListener onServiceStateChanged: invalid mSubId = " + this.mSubId);
            }
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            if (IntelligentDataSwitch.this.isSlotIdValid(this.mSubId)) {
                IntelligentDataSwitch.this.mIdsHandler.sendMessage(IntelligentDataSwitch.this.mIdsHandler.obtainMessage(17, this.mSubId, state));
                IntelligentDataSwitch.this.logd("SlotStateListener onCallStateChanged on mSubId = " + this.mSubId + ", call state change to " + state);
                return;
            }
            IntelligentDataSwitch.this.loge("SlotStateListener onCallStateChanged: invalid slotId id = " + this.mSubId);
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            if (IntelligentDataSwitch.this.isSlotIdValid(this.mSubId)) {
                IntelligentDataSwitch.this.mIdsHandler.sendMessage(IntelligentDataSwitch.this.mIdsHandler.obtainMessage(7, this.mSubId, 0, signalStrength));
                IntelligentDataSwitch.this.logd("SlotStateListener onSignalStrengthsChanged: send Signal Strength change event on mSubId " + this.mSubId + ",signalStrength :" + signalStrength);
                return;
            }
            IntelligentDataSwitch.this.loge("onSignalStrengthsChanged: invalid mSubId = " + this.mSubId);
        }
    }

    private class AirPlaneModeObserver extends ContentObserver {
        public AirPlaneModeObserver(Handler handler) {
            super(handler);
            IntelligentDataSwitch.this.mResolver = IntelligentDataSwitch.this.mContext.getContentResolver();
        }

        public void register() {
            IntelligentDataSwitch.this.mResolver.registerContentObserver(Global.getUriFor("airplane_mode_on"), false, this);
        }

        public void unregister() {
            IntelligentDataSwitch.this.mResolver.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            boolean airplaneMode = IntelligentDataSwitch.this.isAirplaneModeOn();
            IntelligentDataSwitch.this.logd("AirPlaneMode change to " + airplaneMode);
            IntelligentDataSwitch.this.mIdsHandler.sendMessage(IntelligentDataSwitch.this.mIdsHandler.obtainMessage(12, Boolean.valueOf(airplaneMode)));
        }
    }

    private class IDSBroadcastReceiver extends BroadcastReceiver {
        private static final /* synthetic */ int[] -com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$com$android$internal$telephony$PhoneConstants$DataState;

        private static /* synthetic */ int[] -getcom-android-internal-telephony-PhoneConstants$DataStateSwitchesValues() {
            if (-com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues != null) {
                return -com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues;
            }
            int[] iArr = new int[DataState.values().length];
            try {
                iArr[DataState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[DataState.CONNECTING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[DataState.DISCONNECTED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[DataState.SUSPENDED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            -com-android-internal-telephony-PhoneConstants$DataStateSwitchesValues = iArr;
            return iArr;
        }

        private IDSBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                IntelligentDataSwitch.this.loge("onReceive: intent = null or intent.getAction = null");
                return;
            }
            String action = intent.getAction();
            if (action.equals("android.intent.action.ANY_DATA_STATE")) {
                onReceiveDataStateChanged(intent);
            } else if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                onReceiveDefaultDataSubscriptionChange(intent);
            } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                onWifiConnectStateChange(intent);
            } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                onWifiStateChange(intent);
            } else if (action.equals("android.intent.action.INTELLGENT_DATA_SWITCH_IS_ON")) {
                onReceiveldsStateChange(true);
            } else if (action.equals("android.intent.action.INTELLGENT_DATA_SWITCH_IS_OFF")) {
                onReceiveldsStateChange(false);
            } else if (action.equals("android.intent.action.DATA_CONNECTION_STALL")) {
                onReceiveDataConnectionStalled(intent);
            } else {
                IntelligentDataSwitch.this.logd("IntelligentDataSwitchReceiver: Unknown Broadcast received, intent = " + action);
            }
        }

        private boolean isDefaultApnType(String apnType) {
            return "default".equals(apnType);
        }

        private void onWifiStateChange(Intent intent) {
            boolean enabled;
            boolean z = false;
            if (intent.getIntExtra("wifi_state", 4) == 3) {
                enabled = true;
            } else {
                enabled = false;
            }
            if (!enabled) {
                IntelligentDataSwitch.this.mIdsHandler.sendMessage(IntelligentDataSwitch.this.mIdsHandler.obtainMessage(13, Boolean.valueOf(enabled)));
                IntelligentDataSwitch intelligentDataSwitch = IntelligentDataSwitch.this;
                StringBuilder append = new StringBuilder().append("onWifiStateChange:  wifi state change to  ");
                if (!enabled) {
                    z = true;
                }
                intelligentDataSwitch.logd(append.append(z).toString());
            }
        }

        private void onDataConnected(int slotId, Intent intent) {
            IntelligentDataSwitch.this.logd("onDataConnected Enter, slotId is " + slotId);
            IntelligentDataSwitch.this.mIdsHandler.sendMessage(IntelligentDataSwitch.this.mIdsHandler.obtainMessage(0, slotId, 0));
        }

        private void onWifiConnectStateChange(Intent intent) {
            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
            boolean isConnected = networkInfo != null ? networkInfo.isConnected() : false;
            IntelligentDataSwitch.this.mIdsHandler.sendMessage(IntelligentDataSwitch.this.mIdsHandler.obtainMessage(13, Boolean.valueOf(isConnected)));
            IntelligentDataSwitch.this.logd("onWifiConnectStateChange: wifi state change to  " + isConnected);
        }

        private void onReceiveDataStateChanged(Intent intent) {
            String apnType = intent.getStringExtra("apnType");
            if (isDefaultApnType(apnType)) {
                int slotId = intent.getIntExtra("subscription", -1);
                DataState state = (DataState) Enum.valueOf(DataState.class, intent.getStringExtra("state"));
                if (IntelligentDataSwitch.this.isSlotIdValid(slotId)) {
                    IntelligentDataSwitch.this.logd("onReceiveStateChanged: slotId = " + slotId + ",state = " + state + ",apnType = " + apnType);
                    if (IntelligentDataSwitch.this.mFristTimeReceApnState[slotId]) {
                        IntelligentDataSwitch.this.logd("apnState changed,oldMobileDataState = " + IntelligentDataSwitch.this.mApnState[slotId] + ",state = " + state);
                        if (!state.equals(IntelligentDataSwitch.this.mApnState[slotId])) {
                            switch (-getcom-android-internal-telephony-PhoneConstants$DataStateSwitchesValues()[state.ordinal()]) {
                                case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                                    onDataConnected(slotId, intent);
                                    break;
                                case HwVSimSlotSwitchController.CARD_TYPE_DUAL_MODE /*3*/:
                                    onDataConnectionDisconnected(IntelligentDataSwitch.this.mApnState[slotId], slotId);
                                    break;
                            }
                            IntelligentDataSwitch.this.mApnState[slotId] = state;
                        }
                    } else {
                        IntelligentDataSwitch.this.mApnState[slotId] = state;
                        IntelligentDataSwitch.this.mFristTimeReceApnState[slotId] = true;
                    }
                    return;
                }
                IntelligentDataSwitch.this.loge("onReceiveStateChanged: param is invalid,slotId = " + slotId + ",state = " + state);
            }
        }

        private void onDataConnectionDisconnected(DataState oldMobileDataState, int slotId) {
            if (DataState.CONNECTED.equals(oldMobileDataState) || DataState.SUSPENDED.equals(oldMobileDataState)) {
                IntelligentDataSwitch.this.logd("onDataConnectionDisconnected: EVENT_DEFAULT_DATA_DISCONNECTED_FAILURE on slotId " + slotId);
                IntelligentDataSwitch.this.mIdsHandler.sendMessage(IntelligentDataSwitch.this.mIdsHandler.obtainMessage(3, slotId, 0));
                return;
            }
            IntelligentDataSwitch.this.logd("onDataConnectionDisconnected: EVENT_DEFAULT_DATA_SETUP_FAILURE on slotId " + slotId);
            IntelligentDataSwitch.this.mIdsHandler.sendMessage(IntelligentDataSwitch.this.mIdsHandler.obtainMessage(4, slotId, 0));
        }

        private void onReceiveDefaultDataSubscriptionChange(Intent intent) {
            int slotId = intent.getIntExtra("subscription", -1);
            if (IntelligentDataSwitch.this.isSlotIdValid(slotId)) {
                IntelligentDataSwitch.this.logd("onReceiveDefaultDataSubscriptionChange Enter, new slotId = " + slotId);
                IntelligentDataSwitch.this.mIdsHandler.sendMessage(IntelligentDataSwitch.this.mIdsHandler.obtainMessage(1, slotId, 0));
                return;
            }
            IntelligentDataSwitch.this.loge("onReceiveDefaultDataSubscriptionChange: param is invalid,slotId = " + slotId);
        }

        private void onReceiveDataConnectionStalled(Intent intent) {
            int slotId = intent.getIntExtra("subscription", -1);
            if (IntelligentDataSwitch.this.isSlotIdValid(slotId)) {
                IntelligentDataSwitch.this.mIdsHandler.sendMessage(IntelligentDataSwitch.this.mIdsHandler.obtainMessage(2, slotId, 0));
            } else {
                IntelligentDataSwitch.this.loge("onReceiveDataConnectionStalled: param is invalid,slotId = " + slotId);
            }
        }

        private void onReceiveldsStateChange(boolean state) {
            IntelligentDataSwitch.this.mIdsHandler.sendMessage(IntelligentDataSwitch.this.mIdsHandler.obtainMessage(10, Boolean.valueOf(state)));
            IntelligentDataSwitch.this.logd("Intellgent data switch is turned on");
        }
    }

    private class UserDataEnableObserver extends ContentObserver {
        public UserDataEnableObserver(Handler handler) {
            super(handler);
            IntelligentDataSwitch.this.mResolver = IntelligentDataSwitch.this.mContext.getContentResolver();
        }

        public void register() {
            IntelligentDataSwitch.this.mResolver.registerContentObserver(Global.getUriFor("mobile_data"), false, this);
        }

        public void unregister() {
            IntelligentDataSwitch.this.mResolver.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange) {
            boolean state = IntelligentDataSwitch.this.isUserDataEnabled();
            IntelligentDataSwitch.this.logd("User change Data service state = " + state);
            IntelligentDataSwitch.this.mIdsHandler.sendMessage(IntelligentDataSwitch.this.mIdsHandler.obtainMessage(14, Boolean.valueOf(state)));
        }
    }

    public IntelligentDataSwitch(Context context) {
        this.mContext = null;
        this.mIdsHandler = null;
        this.mDataSwitchManager = null;
        this.mFristTimeReceApnState = new boolean[]{false, false};
        this.mApnState = new DataState[2];
        this.mContext = context;
        startIdsManager();
        startIntentMonitoring();
        registDatabaseObserver();
        startPhoneStateListener();
    }

    private void startIdsManager() {
        String name = "IntelligentDataSwitchThread";
        HandlerThread handlerThread = new HandlerThread(name);
        handlerThread.start();
        this.mDataSwitchManager = new IDSManager(this.mContext, name, handlerThread.getLooper(), this);
        this.mDataSwitchManager.start();
        this.mIdsHandler = this.mDataSwitchManager.getHandler();
    }

    private void startIntentMonitoring() {
        logd("start Monitoring intent");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ANY_DATA_STATE");
        filter.addAction("android.intent.action.DATA_CONNECTION_STALL");
        filter.addAction("android.intent.action.INTELLGENT_DATA_SWITCH_IS_ON");
        filter.addAction("android.intent.action.INTELLGENT_DATA_SWITCH_IS_OFF");
        filter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mContext.registerReceiver(new IDSBroadcastReceiver(), filter);
    }

    private void registDatabaseObserver() {
        observeAirplaneMode();
        observeUserDataEnableStatus();
    }

    private void observeAirplaneMode() {
        this.mAirPlaneModeObserver = new AirPlaneModeObserver(this.mIdsHandler);
        this.mAirPlaneModeObserver.register();
    }

    public boolean isAirplaneModeOn() {
        return Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    private void observeUserDataEnableStatus() {
        this.mUserDataEnableObserver = new UserDataEnableObserver(this.mIdsHandler);
        this.mUserDataEnableObserver.register();
    }

    public boolean isUserDataEnabled() {
        return Global.getInt(this.mContext.getContentResolver(), "mobile_data", 0) != 0;
    }

    public DataState getApnState(int subId) {
        return this.mApnState[subId];
    }

    private void startPhoneStateListener() {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager == null) {
            loge("SlotStateListener: mTelephonyManager is null, return!");
            return;
        }
        int numPhones = telephonyManager.getPhoneCount();
        if (numPhones != 2) {
            loge("SlotStateListener numPhones = " + numPhones);
            return;
        }
        logd("SlotStateListener numPhones is " + numPhones);
        this.mPhoneStateListener = new PhoneStateListener[numPhones];
        for (int i = 0; i < numPhones; i++) {
            this.mPhoneStateListener[i] = getPhoneStateListener(i);
            telephonyManager.listen(this.mPhoneStateListener[i], 289);
        }
    }

    private PhoneStateListener getPhoneStateListener(int i) {
        return new AnonymousClass1(i);
    }

    public boolean isSlotIdValid(int slotId) {
        return slotId >= 0 && 2 > slotId;
    }

    protected void logd(String info) {
        IDSConstants.logd(TAG, info);
    }

    protected void loge(String info) {
        IDSConstants.loge(TAG, info);
    }
}
