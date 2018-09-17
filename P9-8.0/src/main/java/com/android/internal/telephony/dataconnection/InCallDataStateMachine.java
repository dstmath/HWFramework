package com.android.internal.telephony.dataconnection;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneStateListener;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.android.ims.HwImsManagerInner;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;

public class InCallDataStateMachine extends StateMachine {
    private static final String ACTION_INCALL_SCREEN = "InCallScreenIsForegroundActivity";
    private static final int DUAL_SIM_NUM = 2;
    private static final int EVENT_DATA_CONNECTED = 5;
    private static final int EVENT_DATA_DISCONNECTED = 4;
    private static final int EVENT_INCALLUI_BACKGROUND = 7;
    private static final int EVENT_INCALL_DATA_SETTINGS_OFF = 1;
    private static final int EVENT_INCALL_DATA_SETTINGS_ON = 0;
    private static final int EVENT_USER_DISABLE_DATA = 6;
    private static final int EVENT_VOICE_CALL_ENDED = 3;
    private static final int EVENT_VOICE_CALL_STARTED = 2;
    private static final String LOG_TAG = "InCallDataSM";
    private static final int PHONE_ID_0 = 0;
    private static final int PHONE_ID_1 = 1;
    private static final String SETTINGS_INCALL_DATA_SWITCH = "incall_data_switch";
    private static final int SWITCH_OFF = 0;
    private static final int SWITCH_ON = 1;
    private boolean isInCallUIForeground = false;
    private ActivatedSlaveState mActivatedSlaveState = new ActivatedSlaveState(this, null);
    private ActivatingSlaveState mActivatingSlaveState = new ActivatingSlaveState(this, null);
    private Context mContext;
    private DataEnablerObserver mDataEnablerObserver;
    private DeactivatingSlaveDataState mDeactivatingSlaveDataState = new DeactivatingSlaveDataState(this, null);
    private IdleState mIdleState = new IdleState(this, null);
    private int mInCallPhoneId = -1;
    private InCallScreenBroadcastReveiver mInCallScreenBroadcastReveiver;
    private MyPhoneStateListener[] mPhoneStateListener;
    private Phone[] mPhones = null;
    private InCallDataSettingsChangeObserver mSettingsChangeObserver;
    private SlaveActiveState mSlaveActiveState = new SlaveActiveState(this, null);

    private class ActivatedSlaveState extends State {
        /* synthetic */ ActivatedSlaveState(InCallDataStateMachine this$0, ActivatedSlaveState -this1) {
            this();
        }

        private ActivatedSlaveState() {
        }

        public void enter() {
            int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            InCallDataStateMachine.this.log("ActivatedSlaveState enter notifyDataConnection disconnected phoneId = " + default4GSlotId);
            PhoneFactory.getPhone(default4GSlotId).notifyDataConnection("2GVoiceCallStarted", "default", DataState.DISCONNECTED);
        }

        public boolean processMessage(Message msg) {
            InCallDataStateMachine.this.log("ActivatedSlaveState: default msg.what=" + msg.what);
            return false;
        }
    }

    private class ActivatingSlaveState extends State {
        /* synthetic */ ActivatingSlaveState(InCallDataStateMachine this$0, ActivatingSlaveState -this1) {
            this();
        }

        private ActivatingSlaveState() {
        }

        public void enter() {
            InCallDataStateMachine.this.log("ActivatingSlaveState enter mInCallPhoneId is " + InCallDataStateMachine.this.mInCallPhoneId);
        }

        public void exit() {
            InCallDataStateMachine.this.log("ActivatingSlaveState exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 5:
                    int dataPhoneId = Integer.valueOf(msg.arg1).intValue();
                    InCallDataStateMachine.this.log("ActivatingSlaveState processMessage EVENT_DATA_CONNECTED phoneId = " + dataPhoneId);
                    if (dataPhoneId != InCallDataStateMachine.this.mInCallPhoneId) {
                        return true;
                    }
                    InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mActivatedSlaveState);
                    return true;
                default:
                    InCallDataStateMachine.this.log("ActivatingSlaveState: default msg.what=" + msg.what);
                    return false;
            }
        }
    }

    private class DataEnablerObserver extends ContentObserver {
        public DataEnablerObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            int retVal = Global.getInt(InCallDataStateMachine.this.mContext.getContentResolver(), "mobile_data", -1);
            InCallDataStateMachine.this.log("DataEnablerObserver onChange retVal = " + retVal);
            if (retVal == 0) {
                InCallDataStateMachine.this.sendMessage(InCallDataStateMachine.this.obtainMessage(6));
            }
        }
    }

    private class DeactivatingSlaveDataState extends State {
        /* synthetic */ DeactivatingSlaveDataState(InCallDataStateMachine this$0, DeactivatingSlaveDataState -this1) {
            this();
        }

        private DeactivatingSlaveDataState() {
        }

        public void enter() {
            int defaultDataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
            int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            InCallDataStateMachine.this.log("DeactivatingSlaveDataState enter defaultDataSubId = " + defaultDataSubId + "main 4G slotId = " + default4GSlotId);
            if (defaultDataSubId != default4GSlotId) {
                SubscriptionController.getInstance().setDefaultDataSubId(default4GSlotId);
            } else {
                InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mIdleState);
            }
            if (defaultDataSubId >= 0 && defaultDataSubId < 2 && InCallDataStateMachine.this.mPhoneStateListener[defaultDataSubId].currentDataState != 2) {
                InCallDataStateMachine.this.log("DeactivatingSlaveDataState enter slave already diconnected");
                InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mIdleState);
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    InCallDataStateMachine.this.log("DeactivatingSlaveDataState processMessage EVENT_INCALL_DATA_SETTINGS_ON");
                    return true;
                case 4:
                    int phoneId = Integer.valueOf(msg.arg1).intValue();
                    InCallDataStateMachine.this.log("DeactivatingSlaveDataState processMessage EVENT_DATA_DISCONNECTED " + phoneId);
                    int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                    InCallDataStateMachine.this.log("main 4G slotId = " + default4GSlotId);
                    if (phoneId == default4GSlotId) {
                        return true;
                    }
                    InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mIdleState);
                    return true;
                case 7:
                    InCallDataStateMachine.this.log("DeactivatingSlaveDataState processMessage EVENT_INCALLUI_BACKGROUND");
                    return true;
                default:
                    InCallDataStateMachine.this.log("DeactivatingSlaveDataState: default msg.what=" + msg.what);
                    return false;
            }
        }

        public void exit() {
            int defaultDataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
            InCallDataStateMachine.this.log("DeactivatingSlaveDataState exit defaultDataSubId = " + defaultDataSubId);
            TelephonyNetworkFactory activeNetworkFactory = PhoneFactory.getTelephonyNetworkFactory(defaultDataSubId);
            if (activeNetworkFactory != null) {
                activeNetworkFactory.resumeDefaultLink();
            }
            int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            InCallDataStateMachine.this.log("DeactivatingSlaveDataState exit notifyDataConnection phoneId = " + default4GSlotId);
            PhoneFactory.getPhone(default4GSlotId).notifyDataConnection("2GVoiceCallEnded", "default");
            if (Global.getInt(InCallDataStateMachine.this.mContext.getContentResolver(), "mobile_data", 0) == 1) {
                TelephonyNetworkFactory networkFactory = PhoneFactory.getTelephonyNetworkFactory(default4GSlotId);
                if (networkFactory != null) {
                    DcTracker dcTracker = networkFactory.getDcTracker();
                    if (dcTracker != null && SystemProperties.getBoolean("sys.defaultapn.enabled", true)) {
                        InCallDataStateMachine.this.log("DeactivatingSlaveDataState exit setDataEnabled true");
                        dcTracker.setEnabledPublic(0, true);
                    }
                }
            }
        }
    }

    private class IdleState extends State {
        /* synthetic */ IdleState(InCallDataStateMachine this$0, IdleState -this1) {
            this();
        }

        private IdleState() {
        }

        public void enter() {
            InCallDataStateMachine.this.log("IdleState enter");
        }

        public boolean processMessage(Message msg) {
            int default4GSlotId;
            switch (msg.what) {
                case 0:
                    InCallDataStateMachine.this.log("IdleState processMessage EVENT_INCALL_DATA_SETTINGS_ON isInCallUIForeground = " + InCallDataStateMachine.this.isInCallUIForeground);
                    if (InCallDataStateMachine.this.isInCallDataSwitchOn() && InCallDataStateMachine.this.isSlaveCanActiveData()) {
                        default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                        InCallDataStateMachine.this.log("call phoneId = " + InCallDataStateMachine.this.mInCallPhoneId + "main 4G slotId = " + default4GSlotId);
                        if (InCallDataStateMachine.this.mInCallPhoneId != default4GSlotId) {
                            InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mActivatingSlaveState);
                            InCallDataStateMachine.this.log("IdleState setDefaultDataSubId to " + InCallDataStateMachine.this.mInCallPhoneId);
                            SubscriptionController.getInstance().setDefaultDataSubId(InCallDataStateMachine.this.mInCallPhoneId);
                            break;
                        }
                    }
                    break;
                case 2:
                    InCallDataStateMachine.this.log("IdleState processMessage EVENT_CALL_START");
                    AsyncResult ar = msg.obj;
                    if (ar != null && ((ar.userObj instanceof Integer) ^ 1) == 0) {
                        InCallDataStateMachine.this.mInCallPhoneId = ((Integer) ar.userObj).intValue();
                        if (InCallDataStateMachine.this.isInCallDataSwitchOn() && InCallDataStateMachine.this.isSlaveCanActiveData()) {
                            default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                            InCallDataStateMachine.this.log("call phoneId = " + InCallDataStateMachine.this.mInCallPhoneId + "main 4G slotId = " + default4GSlotId);
                            if (InCallDataStateMachine.this.mInCallPhoneId != default4GSlotId) {
                                InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mActivatingSlaveState);
                                InCallDataStateMachine.this.log("IdleState setDefaultDataSubId to " + InCallDataStateMachine.this.mInCallPhoneId);
                                SubscriptionController.getInstance().setDefaultDataSubId(InCallDataStateMachine.this.mInCallPhoneId);
                                break;
                            }
                        }
                    }
                    InCallDataStateMachine.this.logd("EVENT_VOICE_CALL_STARTED error ar");
                    break;
                    break;
                case 3:
                    InCallDataStateMachine.this.log("IdleState processMessage EVENT_VOICE_CALL_ENDED");
                    InCallDataStateMachine.this.mInCallPhoneId = -1;
                    InCallDataStateMachine.this.isInCallUIForeground = false;
                    break;
                case 7:
                    InCallDataStateMachine.this.log("IdleState processMessage EVENT_INCALLUI_BACKGROUND");
                    if (InCallDataStateMachine.this.shouldShowDialog()) {
                        SystemProperties.set("persist.radio.incalldata", "true");
                        InCallDataStateMachine.this.showDialog();
                        break;
                    }
                    break;
                default:
                    InCallDataStateMachine.this.log("IdleState: default msg.what=" + msg.what);
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
                if (InCallDataStateMachine.ACTION_INCALL_SCREEN.equals(intent.getAction())) {
                    InCallDataStateMachine.this.isInCallUIForeground = intent.getBooleanExtra("IsForegroundActivity", true);
                    InCallDataStateMachine.this.log("InCallScreenBroadcastReveiver onReceive isInCallUIForeground = " + InCallDataStateMachine.this.isInCallUIForeground);
                    if (!InCallDataStateMachine.this.isInCallUIForeground) {
                        InCallDataStateMachine.this.sendMessage(InCallDataStateMachine.this.obtainMessage(7));
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
            InCallDataStateMachine.this.log("onDataConnectionStateChanged mPhoneId= " + this.mPhoneId + "  state = " + state);
            this.currentDataState = state;
            if (state == 0) {
                InCallDataStateMachine.this.sendMessage(InCallDataStateMachine.this.obtainMessage(4, this.mPhoneId));
            } else if (2 == state || 3 == state) {
                InCallDataStateMachine.this.sendMessage(InCallDataStateMachine.this.obtainMessage(5, this.mPhoneId));
            }
        }
    }

    private class SlaveActiveState extends State {
        /* synthetic */ SlaveActiveState(InCallDataStateMachine this$0, SlaveActiveState -this1) {
            this();
        }

        private SlaveActiveState() {
        }

        public boolean processMessage(Message msg) {
            int defaultDataSubId;
            int default4GSlotId;
            switch (msg.what) {
                case 0:
                    InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_INCALL_DATA_SETTINGS_ON");
                    return true;
                case 1:
                    InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_INCALL_DATA_SETTINGS_OFF");
                    if (InCallDataStateMachine.this.isInCallDataSwitchOn()) {
                        return true;
                    }
                    defaultDataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
                    default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                    InCallDataStateMachine.this.log("defaultDataSubId = " + defaultDataSubId + "main 4G slotId = " + default4GSlotId);
                    if (defaultDataSubId != default4GSlotId) {
                        InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mDeactivatingSlaveDataState);
                        return true;
                    }
                    InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mIdleState);
                    return true;
                case 3:
                    InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_VOICE_CALL_ENDED");
                    defaultDataSubId = SubscriptionController.getInstance().getDefaultDataSubId();
                    default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                    InCallDataStateMachine.this.log("defaultDataSubId = " + defaultDataSubId + "main 4G slotId = " + default4GSlotId);
                    if (defaultDataSubId != default4GSlotId) {
                        InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mDeactivatingSlaveDataState);
                    } else {
                        InCallDataStateMachine.this.transitionTo(InCallDataStateMachine.this.mIdleState);
                    }
                    InCallDataStateMachine.this.mInCallPhoneId = -1;
                    InCallDataStateMachine.this.isInCallUIForeground = false;
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
                    dcTracker.setDataEnabled(false);
                    return true;
                case 7:
                    InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_INCALLUI_BACKGROUND");
                    return true;
                default:
                    InCallDataStateMachine.this.log("SlaveActiveState: default msg.what=" + msg.what);
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
            context.registerReceiver(this.mInCallScreenBroadcastReveiver, filter);
            this.mPhones = new Phone[2];
            int i = 0;
            while (i < 2) {
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
                i++;
            }
            TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            this.mPhoneStateListener = new MyPhoneStateListener[2];
            if (telephonyManager == null) {
                loge("SlotStateListener: mTelephonyManager is null, return!");
            } else {
                for (i = 0; i < 2; i++) {
                    this.mPhoneStateListener[i] = new MyPhoneStateListener(i);
                    telephonyManager.listen(this.mPhoneStateListener[i], 64);
                }
            }
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("mobile_data"), true, this.mDataEnablerObserver);
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(SETTINGS_INCALL_DATA_SWITCH), true, this.mSettingsChangeObserver);
        }
        addState(this.mIdleState);
        addState(this.mSlaveActiveState, this.mIdleState);
        addState(this.mActivatingSlaveState, this.mSlaveActiveState);
        addState(this.mActivatedSlaveState, this.mSlaveActiveState);
        addState(this.mDeactivatingSlaveDataState, this.mIdleState);
        setInitialState(this.mIdleState);
    }

    private boolean isInCallDataSwitchOn() {
        if (Global.getInt(this.mContext.getContentResolver(), SETTINGS_INCALL_DATA_SWITCH, 0) == 1) {
            return true;
        }
        return false;
    }

    private boolean isSlaveCanActiveData() {
        if (HwVSimUtils.isVSimEnabled()) {
            return false;
        }
        log("isSlaveCanActiveData mInCallPhoneId = " + this.mInCallPhoneId);
        if (this.mInCallPhoneId < 0 || this.mInCallPhoneId >= 2) {
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
        if (!isCTCard || (HwTelephonyManagerInner.getDefault().isImsRegistered(this.mInCallPhoneId) ^ 1) == 0) {
            int networkType = ((TelephonyManager) this.mContext.getSystemService("phone")).getNetworkType(this.mInCallPhoneId);
            log("isSlaveCanActiveData networkType = " + networkType);
            return canActiveDataByNetworkType(networkType);
        }
        log("isSlaveCanActiveData CT not switch data when is not volte ");
        return false;
    }

    private boolean canActiveDataByNetworkType(int networkType) {
        switch (networkType) {
            case 3:
            case 8:
            case 9:
            case 10:
            case 13:
            case 15:
            case 19:
            case 30:
                return true;
            default:
                return false;
        }
    }

    private boolean shouldShowDialog() {
        boolean isUserDataOn = 1 == Global.getInt(this.mContext.getContentResolver(), "mobile_data", -1);
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
        boolean shouldDialog = (isInCallDataSwitchOn() || !isSlaveCanActiveData() || !isUserDataOn || (mIsWifiConnected ^ 1) == 0 || (hasShowDialog ^ 1) == 0 || this.mInCallPhoneId == default4GSlot) ? false : true;
        log("shouldDialog is: " + shouldDialog + ",isUserDataOn:" + isUserDataOn + ",mIsWifiConnected:" + mIsWifiConnected + ",hasShowDialog:" + hasShowDialog + ",mInCallPhoneId:" + this.mInCallPhoneId + ",default4GSlot" + default4GSlot);
        return shouldDialog;
    }

    private void showDialog() {
        String toastString = String.format(this.mContext.getResources().getString(33686056), new Object[]{Integer.valueOf(this.mInCallPhoneId + 1)});
        Builder builder = new Builder(this.mContext, 33947691);
        builder.setTitle(33686055);
        builder.setMessage(toastString);
        builder.setPositiveButton(17039833, new OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                Global.putInt(InCallDataStateMachine.this.mContext.getContentResolver(), InCallDataStateMachine.SETTINGS_INCALL_DATA_SWITCH, 1);
            }
        });
        builder.setNegativeButton(17039360, null);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(2003);
        dialog.setCancelable(false);
        dialog.show();
    }

    protected void log(String s) {
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

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
}
