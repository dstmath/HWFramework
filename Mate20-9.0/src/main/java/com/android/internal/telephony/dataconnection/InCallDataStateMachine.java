package com.android.internal.telephony.dataconnection;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseCallState;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.ims.HwImsManagerInner;
import com.android.internal.telephony.DecisionUtil;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConfig;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

public class InCallDataStateMachine extends StateMachine {
    private static final String ACTION_CANCEL_NOTIFY = "huawei.intent.action.cancel_data_switch";
    private static final String ACTION_CONFIRM_SMART_DATA_SWITCH = "huawei.intent.action.confirm_data_switch";
    public static final String ACTION_HW_DSDS_MODE_STATE = "com.huawei.action.ACTION_HW_DSDS_MODE_STATE";
    private static final String ACTION_INCALL_SCREEN = "InCallScreenIsForegroundActivity";
    private static final String CARDMANAGER_AVTIVITY = "com.huawei.settings.intent.DUAL_CARD_SETTINGS";
    private static final String DATA_SMART_SWITCH_CHANNEL = "smart_switch_data_channel";
    private static final int DEFAULT_VALUE_STATE = 0;
    private static final String DISABLE_CS_INCALL_SWITCH = "ro.hw.sub1_disable_switch_cs";
    private static final boolean DISABLE_GW_PS_ATTACH = SystemProperties.getBoolean("ro.odm.disable_m1_gw_ps_attach", false);
    private static final int DSDS2 = 0;
    private static final int DSDS3 = 1;
    private static final String DSDS_KEY = "dsdsmode";
    private static final int DUAL_SIM_NUM = 2;
    private static final int EVENT_DATA_CONNECTED = 5;
    private static final int EVENT_DATA_DISCONNECTED = 4;
    private static final int EVENT_DSDS_MODE_CHANGE = 9;
    private static final int EVENT_INCALLUI_BACKGROUND = 7;
    private static final int EVENT_INCALL_DATA_SETTINGS_OFF = 1;
    private static final int EVENT_INCALL_DATA_SETTINGS_ON = 0;
    private static final int EVENT_USER_DISABLE_DATA = 6;
    private static final int EVENT_VOICE_CALL_ACTIVE = 8;
    private static final int EVENT_VOICE_CALL_ENDED = 3;
    private static final int EVENT_VOICE_CALL_STARTED = 2;
    private static final String LOG_TAG = "InCallDataSM";
    private static final int NOTIFICATION_ID_SMART_DATA_SWITCH = 100;
    private static final int PHONE_ID_0 = 0;
    private static final int PHONE_ID_1 = 1;
    /* access modifiers changed from: private */
    public static final boolean PROP_DEL_DEFAULT_LINK = SystemProperties.getBoolean("ro.config.del_default_link", false);
    private static final String PROP_SMART_DUAL_CARD_MODE = "persist.sys.smart_switch_enable";
    private static final String REC_DECISION_NAME = "com.huawei.android.dsdscardmanger.intent.action.Rec";
    private static final String SETTINGS_INCALL_DATA_SWITCH = "incall_data_switch";
    private static final String SETTINGS_INTELLIGENCE_CARD_SWITCH = "intelligence_card_switch";
    private static final int SWITCHING_STATE = 1;
    private static final int SWITCH_DONE = 2;
    private static final int SWITCH_OFF = 0;
    private static final int SWITCH_ON = 1;
    /* access modifiers changed from: private */
    public boolean isInCallUIForeground = false;
    /* access modifiers changed from: private */
    public ActivatedSlaveState mActivatedSlaveState = new ActivatedSlaveState();
    /* access modifiers changed from: private */
    public ActivatingSlaveState mActivatingSlaveState = new ActivatingSlaveState();
    /* access modifiers changed from: private */
    public Context mContext;
    private DataEnablerObserver mDataEnablerObserver;
    /* access modifiers changed from: private */
    public DeactivatingSlaveDataState mDeactivatingSlaveDataState = new DeactivatingSlaveDataState();
    /* access modifiers changed from: private */
    public DefaultLinkDeletedState mDefaultLinkDeletedState = new DefaultLinkDeletedState();
    /* access modifiers changed from: private */
    public int mDsdsMode = 0;
    /* access modifiers changed from: private */
    public int mForegroundCallState = -1;
    /* access modifiers changed from: private */
    public IdleState mIdleState = new IdleState();
    /* access modifiers changed from: private */
    public int mInCallPhoneId = -1;
    private InCallScreenBroadcastReveiver mInCallScreenBroadcastReveiver;
    private NotificationManager mNotificationManager;
    /* access modifiers changed from: private */
    public MyPhoneStateListener[] mPhoneStateListener;
    private Phone[] mPhones = null;
    private PhoneStateListener mPreciseCallStateListener = new PhoneStateListener() {
        public void onPreciseCallStateChanged(PreciseCallState callState) {
            if (callState != null) {
                int foregroundCallState = callState.getForegroundCallState();
                if (foregroundCallState != InCallDataStateMachine.this.mForegroundCallState) {
                    InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                    inCallDataStateMachine.log("onPreciseCallStateChanged foregroundCallState=" + foregroundCallState);
                    int unused = InCallDataStateMachine.this.mForegroundCallState = foregroundCallState;
                    if (1 == foregroundCallState) {
                        InCallDataStateMachine.this.sendMessage(InCallDataStateMachine.this.obtainMessage(8));
                    }
                }
            }
        }
    };
    private InCallDataSettingsChangeObserver mSettingsChangeObserver;
    private SlaveActiveState mSlaveActiveState = new SlaveActiveState();

    private class ActivatedSlaveState extends State {
        private ActivatedSlaveState() {
        }

        public void enter() {
            int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
            inCallDataStateMachine.log("ActivatedSlaveState enter notifyDataConnection disconnected phoneId = " + default4GSlotId);
            PhoneFactory.getPhone(default4GSlotId).notifyDataConnection("2GVoiceCallStarted", "default", PhoneConstants.DataState.DISCONNECTED);
            int defaultDataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
            InCallDataStateMachine.this.reportSlaveActivedToChr((byte) defaultDataSubId, (byte) default4GSlotId, ((TelephonyManager) InCallDataStateMachine.this.mContext.getSystemService("phone")).getNetworkType(defaultDataSubId));
        }

        public boolean processMessage(Message msg) {
            InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
            inCallDataStateMachine.log("ActivatedSlaveState: default msg.what=" + msg.what);
            return false;
        }
    }

    private class ActivatingSlaveState extends State {
        private ActivatingSlaveState() {
        }

        public void enter() {
            InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
            inCallDataStateMachine.log("ActivatingSlaveState enter mInCallPhoneId is " + InCallDataStateMachine.this.mInCallPhoneId);
        }

        public void exit() {
            InCallDataStateMachine.this.log("ActivatingSlaveState exit");
        }

        public boolean processMessage(Message msg) {
            if (msg.what != 5) {
                InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                inCallDataStateMachine.log("ActivatingSlaveState: default msg.what=" + msg.what);
                return false;
            }
            int dataPhoneId = Integer.valueOf(msg.arg1).intValue();
            InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
            inCallDataStateMachine2.log("ActivatingSlaveState processMessage EVENT_DATA_CONNECTED phoneId = " + dataPhoneId);
            if (dataPhoneId != InCallDataStateMachine.this.mInCallPhoneId) {
                return true;
            }
            InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mActivatedSlaveState);
            return true;
        }
    }

    private class DataEnablerObserver extends ContentObserver {
        public DataEnablerObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            int retVal = Settings.Global.getInt(InCallDataStateMachine.this.mContext.getContentResolver(), "mobile_data", -1);
            InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
            inCallDataStateMachine.log("DataEnablerObserver onChange retVal = " + retVal);
            if (retVal == 0) {
                InCallDataStateMachine.this.sendMessage(InCallDataStateMachine.this.obtainMessage(6));
            }
        }
    }

    private class DeactivatingSlaveDataState extends State {
        private DeactivatingSlaveDataState() {
        }

        public void enter() {
            int defaultDataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
            int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
            inCallDataStateMachine.log("DeactivatingSlaveDataState enter defaultDataSubId = " + defaultDataSubId + "main 4G slotId = " + default4GSlotId);
            if (defaultDataSubId != default4GSlotId) {
                SubscriptionController.getInstance().setDefaultDataSubId(default4GSlotId);
            } else {
                InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mIdleState);
            }
            if (defaultDataSubId >= 0 && defaultDataSubId < 2 && InCallDataStateMachine.this.mPhoneStateListener[defaultDataSubId].currentDataState != 2 && InCallDataStateMachine.this.mPhoneStateListener[defaultDataSubId].currentDataState != 3) {
                InCallDataStateMachine.this.log("DeactivatingSlaveDataState enter slave already diconnected");
                InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mIdleState);
            }
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                InCallDataStateMachine.this.log("DeactivatingSlaveDataState processMessage EVENT_INCALL_DATA_SETTINGS_ON");
                return true;
            } else if (i == 4) {
                int phoneId = Integer.valueOf(msg.arg1).intValue();
                InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                inCallDataStateMachine.log("DeactivatingSlaveDataState processMessage EVENT_DATA_DISCONNECTED " + phoneId);
                int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
                inCallDataStateMachine2.log("main 4G slotId = " + default4GSlotId);
                if (phoneId == default4GSlotId) {
                    return true;
                }
                InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mIdleState);
                return true;
            } else if (i != 7) {
                InCallDataStateMachine inCallDataStateMachine3 = InCallDataStateMachine.this;
                inCallDataStateMachine3.log("DeactivatingSlaveDataState: default msg.what=" + msg.what);
                return false;
            } else {
                InCallDataStateMachine.this.log("DeactivatingSlaveDataState processMessage EVENT_INCALLUI_BACKGROUND");
                return true;
            }
        }

        public void exit() {
            int defaultDataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
            InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
            inCallDataStateMachine.log("DeactivatingSlaveDataState exit defaultDataSubId = " + defaultDataSubId);
            TelephonyNetworkFactory activeNetworkFactory = PhoneFactory.getTelephonyNetworkFactory(defaultDataSubId);
            if (activeNetworkFactory != null) {
                activeNetworkFactory.resumeDefaultLink();
            }
            int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
            inCallDataStateMachine2.log("DeactivatingSlaveDataState exit notifyDataConnection phoneId = " + default4GSlotId);
            PhoneFactory.getPhone(default4GSlotId).notifyDataConnection("2GVoiceCallEnded", "default");
            if (Settings.Global.getInt(InCallDataStateMachine.this.mContext.getContentResolver(), "mobile_data", 0) == 1) {
                TelephonyNetworkFactory networkFactory = PhoneFactory.getTelephonyNetworkFactory(default4GSlotId);
                if (networkFactory != null) {
                    DcTracker dcTracker = networkFactory.getDcTracker();
                    if (dcTracker != null && SystemProperties.getBoolean("sys.defaultapn.enabled", true)) {
                        InCallDataStateMachine.this.log("DeactivatingSlaveDataState exit setUserDataEnabled true");
                        dcTracker.setEnabledPublic(0, true);
                    }
                }
            }
        }
    }

    private class DefaultLinkDeletedState extends State {
        private DefaultLinkDeletedState() {
        }

        public void enter() {
            InCallDataStateMachine.this.log("DefaultLinkDeletedState enter");
            int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            TelephonyNetworkFactory networkFactory = PhoneFactory.getTelephonyNetworkFactory(default4GSlotId);
            if (networkFactory != null) {
                DcTracker dcTracker = networkFactory.getDcTracker();
                Phone default4GPhone = PhoneFactory.getPhone(default4GSlotId);
                InCallDataStateMachine.this.log("DefaultLinkDeletedState clearDefaultLink");
                if (dcTracker != null && default4GPhone != null) {
                    dcTracker.clearDefaultLink();
                    InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                    inCallDataStateMachine.log("DefaultLinkDeletedState notifyDataConnection disconnected phoneId = " + default4GSlotId);
                    default4GPhone.notifyDataConnection("2GVoiceCallStarted", "default", PhoneConstants.DataState.DISCONNECTED);
                }
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    InCallDataStateMachine.this.log("DefaultLinkDeletedState processMessage EVENT_INCALL_DATA_SETTINGS_OFF");
                    return true;
                case 2:
                    InCallDataStateMachine.this.log("DefaultLinkDeletedState processMessage EVENT_VOICE_CALL_STARTED");
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar == null || !(ar.userObj instanceof Integer)) {
                        InCallDataStateMachine.this.logd("EVENT_VOICE_CALL_STARTED error ar");
                        return true;
                    }
                    int unused = InCallDataStateMachine.this.mInCallPhoneId = ((Integer) ar.userObj).intValue();
                    return true;
                case 3:
                    int callEndPhoneId = -1;
                    int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                    InCallDataStateMachine.this.log("DefaultLinkDeletedState processMessage EVENT_VOICE_CALL_ENDED");
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2 == null || !(ar2.userObj instanceof Integer)) {
                        InCallDataStateMachine.this.logd("EVENT_VOICE_CALL_ENDED error ar");
                    } else {
                        callEndPhoneId = ((Integer) ar2.userObj).intValue();
                    }
                    if (InCallDataStateMachine.this.isPhoneStateIDLE()) {
                        InCallDataStateMachine.this.log("DefaultLinkDeletedState transitionTo IdleState");
                        InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mIdleState);
                        int unused2 = InCallDataStateMachine.this.mInCallPhoneId = -1;
                        boolean unused3 = InCallDataStateMachine.this.isInCallUIForeground = false;
                        return true;
                    } else if (callEndPhoneId < 0 || callEndPhoneId >= 2 || callEndPhoneId == default4GSlotId || !InCallDataStateMachine.this.is4GSlotCanActiveData()) {
                        return true;
                    } else {
                        InCallDataStateMachine.this.log("DefaultLinkDeletedState 4GSlotCanActiveData,transitionTo IdleState");
                        InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mIdleState);
                        return true;
                    }
                case 4:
                    InCallDataStateMachine.this.log("DefaultLinkDeletedState processMessage EVENT_DATA_DISCONNECTED");
                    return true;
                case 5:
                    InCallDataStateMachine.this.log("DefaultLinkDeletedState processMessage EVENT_DATA_CONNECTED");
                    return true;
                case 6:
                    InCallDataStateMachine.this.log("DefaultLinkDeletedState processMessage EVENT_USER_DISABLE_DATA,transitionTo(mIdleState)");
                    InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mIdleState);
                    return true;
                case 8:
                    InCallDataStateMachine.this.log("DefaultLinkDeletedState processMessage EVENT_VOICE_CALL_ACTIVE");
                    return true;
                default:
                    InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                    inCallDataStateMachine.log("DefaultLinkDeletedState: default msg.what=" + msg.what);
                    return false;
            }
        }

        public void exit() {
            InCallDataStateMachine.this.log("DefaultLinkDeletedState exit()");
            InCallDataStateMachine.this.resumeDefaultLink();
        }
    }

    private class IdleState extends State {
        private IdleState() {
        }

        public void enter() {
            InCallDataStateMachine.this.log("IdleState enter");
            if (true == InCallDataStateMachine.this.isNeedEnterDefaultLinkDeletedState()) {
                InCallDataStateMachine.this.log("transitionTo mDefaultLinkDeletedState");
                InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mDefaultLinkDeletedState);
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    AsyncResult ar = InCallDataStateMachine.this;
                    ar.log("IdleState processMessage EVENT_INCALL_DATA_SETTINGS_ON isInCallUIForeground = " + InCallDataStateMachine.this.isInCallUIForeground);
                    if (InCallDataStateMachine.this.isInCallDataSwitchOn() && InCallDataStateMachine.this.isSlaveCanActiveData()) {
                        int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                        InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                        inCallDataStateMachine.log("call phoneId = " + InCallDataStateMachine.this.mInCallPhoneId + "main 4G slotId = " + default4GSlotId);
                        if (InCallDataStateMachine.this.mInCallPhoneId != default4GSlotId) {
                            InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mActivatingSlaveState);
                            InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
                            inCallDataStateMachine2.log("IdleState setDefaultDataSubId to " + InCallDataStateMachine.this.mInCallPhoneId);
                            SubscriptionController.getInstance().setDefaultDataSubId(InCallDataStateMachine.this.mInCallPhoneId);
                            break;
                        }
                    }
                    break;
                case 2:
                    InCallDataStateMachine.this.log("IdleState processMessage EVENT_CALL_START");
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2 != null && (ar2.userObj instanceof Integer)) {
                        int unused = InCallDataStateMachine.this.mInCallPhoneId = ((Integer) ar2.userObj).intValue();
                        if (InCallDataStateMachine.this.isInCallDataSwitchOn() && InCallDataStateMachine.this.isSlaveCanActiveData()) {
                            int default4GSlotId2 = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                            InCallDataStateMachine inCallDataStateMachine3 = InCallDataStateMachine.this;
                            inCallDataStateMachine3.log("call phoneId = " + InCallDataStateMachine.this.mInCallPhoneId + "main 4G slotId = " + default4GSlotId2);
                            if (InCallDataStateMachine.this.mInCallPhoneId != default4GSlotId2) {
                                InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mActivatingSlaveState);
                                InCallDataStateMachine inCallDataStateMachine4 = InCallDataStateMachine.this;
                                inCallDataStateMachine4.log("IdleState setDefaultDataSubId to " + InCallDataStateMachine.this.mInCallPhoneId);
                                SubscriptionController.getInstance().setDefaultDataSubId(InCallDataStateMachine.this.mInCallPhoneId);
                                break;
                            }
                        }
                    } else {
                        InCallDataStateMachine.this.logd("EVENT_VOICE_CALL_STARTED error ar");
                        break;
                    }
                    break;
                case 3:
                    InCallDataStateMachine.this.log("IdleState processMessage EVENT_VOICE_CALL_ENDED");
                    if (InCallDataStateMachine.this.isPhoneStateIDLE()) {
                        InCallDataStateMachine.this.log("IdleState set mInCallPhoneId -1");
                        int unused2 = InCallDataStateMachine.this.mInCallPhoneId = -1;
                        boolean unused3 = InCallDataStateMachine.this.isInCallUIForeground = false;
                    }
                    if (InCallDataStateMachine.this.hasMessages(9)) {
                        InCallDataStateMachine.this.removeMessages(9);
                        break;
                    }
                    break;
                case 5:
                    int dataPhoneId = Integer.valueOf(msg.arg1).intValue();
                    int default4GSlotId3 = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                    InCallDataStateMachine inCallDataStateMachine5 = InCallDataStateMachine.this;
                    inCallDataStateMachine5.log("IdleState processMessage EVENT_DATA_CONNECTED phoneId = " + dataPhoneId + " default4GSlotId=" + default4GSlotId3);
                    if (true == InCallDataStateMachine.this.isNeedEnterDefaultLinkDeletedState() && dataPhoneId == default4GSlotId3) {
                        InCallDataStateMachine.this.log("transitionTo mDefaultLinkDeletedState");
                        InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mDefaultLinkDeletedState);
                        break;
                    }
                case 7:
                    InCallDataStateMachine.this.log("IdleState processMessage EVENT_INCALLUI_BACKGROUND");
                    if (InCallDataStateMachine.this.shouldShowDialog()) {
                        SystemProperties.set("persist.radio.incalldata", "true");
                        InCallDataStateMachine.this.notifyUserToEnableSmartSwitch();
                        break;
                    }
                    break;
                case 8:
                    InCallDataStateMachine.this.log("IdleState processMessage EVENT_VOICE_CALL_ACTIVE");
                    if (true == InCallDataStateMachine.this.isNeedEnterDefaultLinkDeletedState()) {
                        InCallDataStateMachine.this.log("transitionTo mDefaultLinkDeletedState");
                        InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mDefaultLinkDeletedState);
                        break;
                    }
                    break;
                case 9:
                    InCallDataStateMachine.this.log("IdleState processMessage EVENT_DSDS_MODE_CHANGE");
                    if (InCallDataStateMachine.this.isInCallDataSwitchOn() && InCallDataStateMachine.this.isSlaveCanActiveData()) {
                        int default4GSlotId4 = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                        if (InCallDataStateMachine.this.mInCallPhoneId != default4GSlotId4) {
                            InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mActivatingSlaveState);
                            InCallDataStateMachine inCallDataStateMachine6 = InCallDataStateMachine.this;
                            inCallDataStateMachine6.log("call phoneId = " + InCallDataStateMachine.this.mInCallPhoneId + "main 4G slotId = " + default4GSlotId4);
                            InCallDataStateMachine inCallDataStateMachine7 = InCallDataStateMachine.this;
                            StringBuilder sb = new StringBuilder();
                            sb.append("Found DSDS 2.0 state, setDefaultDataSubId to ");
                            sb.append(InCallDataStateMachine.this.mInCallPhoneId);
                            inCallDataStateMachine7.log(sb.toString());
                            SubscriptionController.getInstance().setDefaultDataSubId(InCallDataStateMachine.this.mInCallPhoneId);
                            break;
                        }
                    }
                    break;
                default:
                    InCallDataStateMachine inCallDataStateMachine8 = InCallDataStateMachine.this;
                    inCallDataStateMachine8.log("IdleState: default msg.what=" + msg.what);
                    break;
            }
            return true;
        }
    }

    private class InCallDataSettingsChangeObserver extends ContentObserver {
        public InCallDataSettingsChangeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            if (InCallDataStateMachine.this.isInCallDataSwitchOn()) {
                InCallDataStateMachine.this.sendMessage(InCallDataStateMachine.this.obtainMessage(0));
            } else {
                InCallDataStateMachine.this.sendMessage(InCallDataStateMachine.this.obtainMessage(1));
            }
        }
    }

    class InCallScreenBroadcastReveiver extends BroadcastReceiver {
        InCallScreenBroadcastReveiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (InCallDataStateMachine.ACTION_INCALL_SCREEN.equals(action)) {
                    boolean unused = InCallDataStateMachine.this.isInCallUIForeground = intent.getBooleanExtra("IsForegroundActivity", true);
                    InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                    inCallDataStateMachine.log("InCallScreenBroadcastReveiver onReceive isInCallUIForeground = " + InCallDataStateMachine.this.isInCallUIForeground);
                    if (!InCallDataStateMachine.this.isInCallUIForeground) {
                        InCallDataStateMachine.this.sendMessage(InCallDataStateMachine.this.obtainMessage(7));
                    }
                } else if (InCallDataStateMachine.ACTION_HW_DSDS_MODE_STATE.equals(action) && !InCallDataStateMachine.PROP_DEL_DEFAULT_LINK) {
                    int newDsdsMode = intent.getIntExtra(InCallDataStateMachine.DSDS_KEY, 0);
                    InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
                    inCallDataStateMachine2.log("BroadcastReveiver onReceive newDsdsMode = " + newDsdsMode);
                    if (InCallDataStateMachine.this.mDsdsMode != newDsdsMode) {
                        int unused2 = InCallDataStateMachine.this.mDsdsMode = newDsdsMode;
                        InCallDataStateMachine.this.sendMessage(InCallDataStateMachine.this.obtainMessage(9));
                    }
                }
            }
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        public int currentDataState = -1;
        private int mPhoneId;

        public MyPhoneStateListener(int phoneId) {
            super(Integer.valueOf(phoneId));
            this.mPhoneId = phoneId;
        }

        public void onDataConnectionStateChanged(int state) {
            InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
            inCallDataStateMachine.log("onDataConnectionStateChanged mPhoneId= " + this.mPhoneId + "  state = " + state);
            this.currentDataState = state;
            if (state == 0) {
                InCallDataStateMachine.this.sendMessage(InCallDataStateMachine.this.obtainMessage(4, this.mPhoneId));
            } else if (2 == state || 3 == state) {
                InCallDataStateMachine.this.sendMessage(InCallDataStateMachine.this.obtainMessage(5, this.mPhoneId));
            }
        }
    }

    private class SlaveActiveState extends State {
        private SlaveActiveState() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_INCALL_DATA_SETTINGS_ON");
                    return true;
                case 1:
                    InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_INCALL_DATA_SETTINGS_OFF");
                    if (InCallDataStateMachine.this.isInCallDataSwitchOn()) {
                        return true;
                    }
                    int defaultDataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
                    int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                    InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                    inCallDataStateMachine.log("defaultDataSubId = " + defaultDataSubId + "main 4G slotId = " + default4GSlotId);
                    if (defaultDataSubId != default4GSlotId) {
                        InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mDeactivatingSlaveDataState);
                        return true;
                    }
                    InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mIdleState);
                    return true;
                case 3:
                    InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_VOICE_CALL_ENDED");
                    int defaultDataSubId2 = SubscriptionController.getInstance().getDefaultDataSubId();
                    int default4GSlotId2 = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                    if (!InCallDataStateMachine.this.isDataPhoneStateIDLE(defaultDataSubId2)) {
                        return true;
                    }
                    InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
                    inCallDataStateMachine2.log("defaultDataSubId = " + defaultDataSubId2 + "main 4G slotId = " + default4GSlotId2);
                    if (defaultDataSubId2 != default4GSlotId2) {
                        InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mDeactivatingSlaveDataState);
                    } else {
                        InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mIdleState);
                    }
                    int unused = InCallDataStateMachine.this.mInCallPhoneId = -1;
                    boolean unused2 = InCallDataStateMachine.this.isInCallUIForeground = false;
                    return true;
                case 6:
                    InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_USER_DISABLE_DATA");
                    TelephonyNetworkFactory networkFactory = PhoneFactory.getTelephonyNetworkFactory(HwTelephonyManagerInner.getDefault().getDefault4GSlotId());
                    if (networkFactory == null) {
                        return true;
                    }
                    DcTracker dcTracker = networkFactory.getDcTracker();
                    if (dcTracker == null) {
                        return true;
                    }
                    dcTracker.setUserDataEnabled(false);
                    return true;
                case 7:
                    InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_INCALLUI_BACKGROUND");
                    return true;
                case 9:
                    InCallDataStateMachine.this.log("SlaveActiveState drop msg EVENT_DSDS_MODE_CHANGE");
                    return true;
                default:
                    InCallDataStateMachine inCallDataStateMachine3 = InCallDataStateMachine.this;
                    inCallDataStateMachine3.log("SlaveActiveState: default msg.what=" + msg.what);
                    return false;
            }
        }
    }

    public InCallDataStateMachine(Context context, Phone[] phones) {
        super(LOG_TAG, Looper.myLooper());
        this.mContext = context;
        boolean dualImsEnable = HwImsManagerInner.isDualImsAvailable();
        if (phones != null && phones.length == 2 && dualImsEnable) {
            this.mSettingsChangeObserver = new InCallDataSettingsChangeObserver(getHandler());
            this.mDataEnablerObserver = new DataEnablerObserver(getHandler());
            this.mInCallScreenBroadcastReveiver = new InCallScreenBroadcastReveiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_INCALL_SCREEN);
            filter.addAction(ACTION_HW_DSDS_MODE_STATE);
            context.registerReceiver(this.mInCallScreenBroadcastReveiver, filter);
            this.mPhones = new Phone[2];
            for (int i = 0; i < 2; i++) {
                this.mPhones[i] = phones[i];
                if (phones[i].getCallTracker() != null) {
                    phones[i].getCallTracker().registerForVoiceCallEnded(getHandler(), 3, Integer.valueOf(i));
                    phones[i].getCallTracker().registerForVoiceCallStarted(getHandler(), 2, Integer.valueOf(i));
                    if (!(phones[i].getImsPhone() == null || phones[i].getImsPhone().getCallTracker() == null)) {
                        log("registerImsCallStates phoneId = " + i);
                        phones[i].getImsPhone().getCallTracker().registerForVoiceCallEnded(getHandler(), 3, Integer.valueOf(i));
                        phones[i].getImsPhone().getCallTracker().registerForVoiceCallStarted(getHandler(), 2, Integer.valueOf(i));
                    }
                }
            }
            TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            this.mPhoneStateListener = new MyPhoneStateListener[2];
            if (telephonyManager == null) {
                loge("SlotStateListener: mTelephonyManager is null, return!");
            } else {
                for (int i2 = 0; i2 < 2; i2++) {
                    this.mPhoneStateListener[i2] = new MyPhoneStateListener(i2);
                    telephonyManager.listen(this.mPhoneStateListener[i2], 64);
                }
            }
            if (!(telephonyManager == null || this.mPreciseCallStateListener == null)) {
                telephonyManager.listen(this.mPreciseCallStateListener, 2048);
            }
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, this.mDataEnablerObserver);
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(SETTINGS_INCALL_DATA_SWITCH), true, this.mSettingsChangeObserver);
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(SETTINGS_INTELLIGENCE_CARD_SWITCH), true, this.mSettingsChangeObserver);
        }
        addState(this.mIdleState);
        addState(this.mSlaveActiveState, this.mIdleState);
        addState(this.mActivatingSlaveState, this.mSlaveActiveState);
        addState(this.mActivatedSlaveState, this.mSlaveActiveState);
        addState(this.mDeactivatingSlaveDataState, this.mIdleState);
        addState(this.mDefaultLinkDeletedState, this.mIdleState);
        setInitialState(this.mIdleState);
    }

    /* access modifiers changed from: private */
    public boolean isInCallDataSwitchOn() {
        int retVal;
        if (SystemProperties.getBoolean(PROP_SMART_DUAL_CARD_MODE, false)) {
            retVal = Settings.Global.getInt(this.mContext.getContentResolver(), SETTINGS_INTELLIGENCE_CARD_SWITCH, 0);
        } else {
            retVal = Settings.Global.getInt(this.mContext.getContentResolver(), SETTINGS_INCALL_DATA_SWITCH, 0);
        }
        if (retVal == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isSlaveCanActiveData() {
        if (HwVSimUtils.isVSimEnabled()) {
            return false;
        }
        log("isSlaveCanActiveData mInCallPhoneId = " + this.mInCallPhoneId);
        if (this.mInCallPhoneId < 0 || this.mInCallPhoneId >= 2) {
            return false;
        }
        if (PROP_DEL_DEFAULT_LINK || this.mDsdsMode != 1) {
            int switchState = SystemProperties.getInt("persist.sys.smart_switch_state", 0);
            if (switchState == 1 || switchState == 2) {
                log("intelligence state = " + switchState + " return false");
                return false;
            }
            boolean isCTCard = false;
            TelephonyNetworkFactory callingNetworkFactory = PhoneFactory.getTelephonyNetworkFactory(this.mInCallPhoneId);
            if (callingNetworkFactory != null) {
                DcTracker dcTracker = callingNetworkFactory.getDcTracker();
                if (dcTracker != null) {
                    isCTCard = dcTracker.isCTSimCard(this.mInCallPhoneId);
                }
            }
            log("isSlaveCanActiveData isCTCard = " + isCTCard);
            boolean disableCsSwitch = SystemProperties.getBoolean(DISABLE_CS_INCALL_SWITCH, false);
            if ((isCTCard || DISABLE_GW_PS_ATTACH || disableCsSwitch) && !HwTelephonyManagerInner.getDefault().isImsRegistered(this.mInCallPhoneId)) {
                log("isSlaveCanActiveData ct card not switch data when is not volte.");
                return false;
            }
            if (this.mPhones[this.mInCallPhoneId].getImsPhone() == null || this.mPhones[this.mInCallPhoneId].getImsPhone().getCallTracker() == null) {
                loge("isSlaveCanActiveData error happened ims phone is null.");
            } else {
                ImsPhoneCallTracker imsCallTracker = this.mPhones[this.mInCallPhoneId].getImsPhone().getCallTracker();
                if (disableCsSwitch && imsCallTracker.getState() == PhoneConstants.State.IDLE) {
                    log("isSlaveCanActiveData not switch data when is not volte calling.");
                    return false;
                }
            }
            if (!HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE || !HwFullNetworkManager.getInstance().isCMCCHybird()) {
                int networkType = ((TelephonyManager) this.mContext.getSystemService("phone")).getNetworkType(this.mInCallPhoneId);
                log("isSlaveCanActiveData networkType = " + networkType);
                return canActiveDataByNetworkType(networkType);
            }
            log("TL version cmcc hybird, do not allowed switch data.");
            return false;
        }
        log("isSlaveCanActiveData found DSDS MODE 3.1, no need to active slave.");
        return false;
    }

    private boolean canActiveDataByNetworkType(int networkType) {
        if (networkType != 3) {
            if (networkType != 13) {
                if (networkType != 15) {
                    if (networkType != 19) {
                        if (networkType != 30) {
                            switch (networkType) {
                                case 8:
                                case 9:
                                case 10:
                                    break;
                                default:
                                    return false;
                            }
                        }
                    }
                }
            }
            return true;
        }
        if (DISABLE_GW_PS_ATTACH) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean shouldShowDialog() {
        boolean shouldDialog = false;
        boolean isUserDataOn = 1 == Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data", -1);
        boolean hasShowDialog = SystemProperties.getBoolean("persist.radio.incalldata", false);
        boolean mIsWifiConnected = false;
        int default4GSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        ConnectivityManager mCm = (ConnectivityManager) PhoneFactory.getPhone(default4GSlot).getContext().getSystemService("connectivity");
        if (mCm != null) {
            NetworkInfo mWifiNetworkInfo = mCm.getNetworkInfo(1);
            if (mWifiNetworkInfo != null && mWifiNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                mIsWifiConnected = true;
            }
        }
        boolean isOverDsds3 = this.mDsdsMode == 1 && !PROP_DEL_DEFAULT_LINK;
        if (!isInCallDataSwitchOn() && isSlaveCanActiveData() && isUserDataOn && !mIsWifiConnected && !hasShowDialog && this.mInCallPhoneId != default4GSlot && !isOverDsds3) {
            shouldDialog = true;
        }
        log("shouldDialog is: " + shouldDialog + ",isUserDataOn:" + isUserDataOn + ",mIsWifiConnected:" + mIsWifiConnected + ",hasShowDialog:" + hasShowDialog + ",mInCallPhoneId:" + this.mInCallPhoneId + ",default4GSlot" + default4GSlot + ", isOverDsds3:" + isOverDsds3);
        return shouldDialog;
    }

    /* access modifiers changed from: private */
    public void notifyUserToEnableSmartSwitch() {
        if (SystemProperties.getBoolean(PROP_SMART_DUAL_CARD_MODE, false)) {
            DecisionUtil.bindService(this.mContext, "com.huawei.android.dsdscardmanger.intent.action.Rec");
        } else {
            showDialog();
        }
    }

    private void showDialog() {
        String toastString = String.format(this.mContext.getResources().getString(33686206), new Object[]{Integer.valueOf(this.mInCallPhoneId + 1)});
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, 33947691);
        builder.setTitle(33686207);
        builder.setMessage(toastString);
        builder.setPositiveButton(17039871, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Settings.Global.putInt(InCallDataStateMachine.this.mContext.getContentResolver(), InCallDataStateMachine.SETTINGS_INCALL_DATA_SWITCH, 1);
            }
        });
        builder.setNegativeButton(17039360, null);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(HwFullNetworkConstants.EVENT_GET_PREF_NETWORK_MODE_DONE);
        dialog.setCancelable(false);
        dialog.show();
    }

    /* access modifiers changed from: protected */
    public void log(String s) {
        Rlog.d(LOG_TAG, s);
    }

    public boolean isDeactivatingSlaveData() {
        return getCurrentState() == this.mDeactivatingSlaveDataState;
    }

    public boolean isSwitchingToSlave() {
        return getCurrentState() == this.mActivatingSlaveState;
    }

    public boolean isSlaveActive() {
        return getCurrentState() == this.mActivatingSlaveState || getCurrentState() == this.mActivatedSlaveState;
    }

    public void registerImsCallStates(boolean enable, int i) {
        if (!(i < 0 || i >= 2 || this.mPhones == null || this.mPhones[i].getImsPhone() == null || this.mPhones[i].getImsPhone().getCallTracker() == null)) {
            if (enable) {
                log("registerImsCallStates phoneId = " + i);
                this.mPhones[i].getImsPhone().getCallTracker().registerForVoiceCallEnded(getHandler(), 3, Integer.valueOf(i));
                this.mPhones[i].getImsPhone().getCallTracker().registerForVoiceCallStarted(getHandler(), 2, Integer.valueOf(i));
            } else {
                log("unregisterImsCallStates phoneId = " + i);
                this.mPhones[i].getImsPhone().getCallTracker().unregisterForVoiceCallEnded(getHandler());
                this.mPhones[i].getImsPhone().getCallTracker().unregisterForVoiceCallStarted(getHandler());
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isPhoneStateIDLE() {
        boolean isIdle = true;
        int i = 0;
        while (i < this.mPhones.length) {
            try {
                if (this.mPhones[i].getCallTracker().getState() != PhoneConstants.State.IDLE || this.mPhones[i].getImsPhone().getCallTracker().getState() != PhoneConstants.State.IDLE) {
                    isIdle = false;
                }
                i++;
            } catch (NullPointerException npe) {
                log(npe.toString());
            } catch (Exception e) {
                log(e.toString());
            }
        }
        log("isPhoneStateIDLE isIdle = " + isIdle);
        return isIdle;
    }

    /* access modifiers changed from: private */
    public boolean isDataPhoneStateIDLE(int dataPhoneId) {
        if (dataPhoneId < 0 || dataPhoneId >= 2) {
            return true;
        }
        boolean isIdle = true;
        if (!((this.mPhones[dataPhoneId] == null || this.mPhones[dataPhoneId].getCallTracker() == null || PhoneConstants.State.IDLE == this.mPhones[dataPhoneId].getCallTracker().getState()) && (this.mPhones[dataPhoneId] == null || this.mPhones[dataPhoneId].getImsPhone() == null || this.mPhones[dataPhoneId].getImsPhone().getCallTracker() == null || PhoneConstants.State.IDLE == this.mPhones[dataPhoneId].getImsPhone().getCallTracker().getState()))) {
            isIdle = false;
        }
        log("isDataPhoneStateIDLE isIdle = " + isIdle);
        return isIdle;
    }

    private boolean isCallStateActive() {
        log("isCallStateActive mForegroundCallState = " + this.mForegroundCallState);
        return 1 == this.mForegroundCallState;
    }

    /* access modifiers changed from: private */
    public void resumeDefaultLink() {
        int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        TelephonyNetworkFactory networkFactory = PhoneFactory.getTelephonyNetworkFactory(default4GSlotId);
        Phone default4GPhone = PhoneFactory.getPhone(default4GSlotId);
        log("resumeDefaultLink default4GSlotId = " + default4GSlotId);
        if (networkFactory != null && default4GPhone != null) {
            networkFactory.resumeDefaultLink();
            default4GPhone.notifyDataConnection("2GVoiceCallEnded", "default");
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.telephony.TelephonyManager} */
    /* JADX WARNING: Multi-variable type inference failed */
    private boolean isDefaultDataConnected() {
        TelephonyManager telephonyManager = null;
        if (this.mContext != null) {
            telephonyManager = this.mContext.getSystemService("phone");
        }
        if (telephonyManager == null || 2 != telephonyManager.getDataState()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean isNeedEnterDefaultLinkDeletedState() {
        int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        int switchState = SystemProperties.getInt("persist.sys.smart_switch_state", 0);
        if (TelephonyManager.MultiSimVariants.DSDS != TelephonyManager.getDefault().getMultiSimConfiguration()) {
            log("isNeedEnterDefaultLinkDeletedState getMultiSimConfiguration=DSDA");
            return false;
        } else if (getCurrentState() != this.mIdleState || this.mInCallPhoneId < 0 || this.mInCallPhoneId >= 2 || this.mInCallPhoneId == default4GSlotId || true != isCallStateActive() || true != isDefaultDataConnected() || true != PROP_DEL_DEFAULT_LINK || (isInCallDataSwitchOn() && (true != isInCallDataSwitchOn() || isSlaveCanActiveData() || switchState != 0))) {
            return false;
        } else {
            log("isNeedEnterDefaultLinkDeletedState true,mInCallPhoneId=" + this.mInCallPhoneId + " default4GSlotId=" + default4GSlotId);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public boolean is4GSlotCanActiveData() {
        int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        log("is4GSlotCanActiveData default4GSlotId = " + default4GSlotId);
        if (default4GSlotId < 0 || default4GSlotId >= 2) {
            return false;
        }
        boolean isCTCard = false;
        TelephonyNetworkFactory callingNetworkFactory = PhoneFactory.getTelephonyNetworkFactory(default4GSlotId);
        if (callingNetworkFactory != null) {
            DcTracker dcTracker = callingNetworkFactory.getDcTracker();
            if (dcTracker != null) {
                isCTCard = dcTracker.isCTSimCard(default4GSlotId);
            }
        }
        log("is4GSlotCanActiveData isCTCard = " + isCTCard);
        if (!isCTCard || HwTelephonyManagerInner.getDefault().isImsRegistered(default4GSlotId)) {
            int networkType = ((TelephonyManager) this.mContext.getSystemService("phone")).getNetworkType(default4GSlotId);
            log("is4GSlotCanActiveData networkType = " + networkType);
            return canActiveDataByNetworkType(networkType);
        }
        log("is4GSlotCanActiveData CT can not active data when is not volte ");
        return false;
    }

    /* access modifiers changed from: private */
    public void reportSlaveActivedToChr(byte defaultDataSubId, byte default4GSlotId, int networkType) {
        log("ActiviedSlaveState report to CHR, networkType = " + networkType);
        Bundle data = new Bundle();
        data.putString("EventScenario", "INCALLDATA");
        data.putInt("EventFailCause", 1001);
        data.putByte("DATACONN.INCALLDATA.InCallSubId", defaultDataSubId);
        data.putByte("DATACONN.INCALLDATA.default4gSubId", default4GSlotId);
        data.putInt("DATACONN.INCALLDATA.networkType", networkType);
        HwTelephonyFactory.getHwTelephonyChrManager().sendTelephonyChrBroadcast(data, defaultDataSubId);
    }
}
