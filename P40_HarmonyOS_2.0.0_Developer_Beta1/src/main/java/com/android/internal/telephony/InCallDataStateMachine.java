package com.android.internal.telephony;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstantsInner;
import com.android.internal.telephony.fullnetwork.HwFullNetworkManager;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.PhoneConstantsExt;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.PhoneStateListenerEx;
import com.huawei.internal.telephony.PhoneStateListenerExt;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
import com.huawei.internal.telephony.dataconnection.TelephonyNetworkFactoryEx;
import com.huawei.internal.util.StateEx;
import com.huawei.internal.util.StateMachineEx;
import com.huawei.utils.HwPartResourceUtils;
import huawei.telephony.PreciseCallStateExt;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class InCallDataStateMachine extends StateMachineEx {
    private static final String ACTION_CANCEL_NOTIFY = "huawei.intent.action.cancel_data_switch";
    private static final String ACTION_CONFIRM_SMART_DATA_SWITCH = "huawei.intent.action.confirm_data_switch";
    public static final String ACTION_HW_DSDS_MODE_STATE = "com.huawei.action.ACTION_HW_DSDS_MODE_STATE";
    private static final String ACTION_INCALL_SCREEN = "InCallScreenIsForegroundActivity";
    private static final String CARDMANAGER_AVTIVITY = "com.huawei.settings.intent.DUAL_CARD_SETTINGS";
    private static final String DATA_SMART_SWITCH_CHANNEL = "smart_switch_data_channel";
    private static final int DEFAULT_VALUE_STATE = 0;
    private static final String DISABLE_CS_INCALL_SWITCH = "ro.hw.sub1_disable_switch_cs";
    private static final boolean DISABLE_GW_PS_ATTACH = SystemPropertiesEx.getBoolean("ro.odm.disable_m1_gw_ps_attach", false);
    private static final int DSDS2 = 0;
    private static final int DSDS3 = 1;
    private static final String DSDS_KEY = "dsdsmode";
    private static final long DSDS_STATE_UPDATE_DELAY_TIMER_MS = 1000;
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
    private static final int INVALID_PHONE_ID = -1;
    private static final String LOG_TAG = "InCallDataSM";
    private static final long MIN_INTERVAL_TIME = 86400000;
    private static final int NOTIFICATION_ID_SMART_DATA_SWITCH = 100;
    private static final int PHONE_ID_0 = 0;
    private static final int PHONE_ID_1 = 1;
    private static final boolean PROP_DEL_DEFAULT_LINK = SystemPropertiesEx.getBoolean("ro.config.del_default_link", false);
    private static final String PROP_SMART_DUAL_CARD_MODE = "persist.sys.smart_switch_enable";
    private static final String REC_DECISION_NAME = "com.huawei.dsdscardmanager.intent.action.Rec";
    private static final String SETTINGS_INCALL_DATA_SWITCH = "incall_data_switch";
    private static final String SETTINGS_INTELLIGENCE_CARD_SWITCH = "intelligence_card_switch";
    private static final int SWITCHING_STATE = 1;
    private static final int SWITCH_DEFAULT = -1;
    private static final int SWITCH_DONE = 2;
    private static final int SWITCH_OFF = 0;
    private static final int SWITCH_ON = 1;
    private boolean isInCallUIForeground = false;
    private ActivatedSlaveState mActivatedSlaveState = new ActivatedSlaveState();
    private ActivatingSlaveState mActivatingSlaveState = new ActivatingSlaveState();
    private Context mContext;
    private DataEnablerObserver mDataEnablerObserver;
    private DeactivatingSlaveDataState mDeactivatingSlaveDataState = new DeactivatingSlaveDataState();
    private DefaultLinkDeletedState mDefaultLinkDeletedState = new DefaultLinkDeletedState();
    private int mDsdsMode = 0;
    private int mForegroundCallState = -1;
    private IdleState mIdleState = new IdleState();
    private int mInCallPhoneId = -1;
    private InCallScreenBroadcastReveiver mInCallScreenBroadcastReveiver;
    private long mLastNotifyTime = 0;
    private final SparseArray<MyPhoneStateListener> mListeners = new SparseArray<>();
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        /* class com.android.internal.telephony.InCallDataStateMachine.AnonymousClass1 */

        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            InCallDataStateMachine.this.logd("onSubscriptionsChanged");
            InCallDataStateMachine.this.registerListeners();
        }
    };
    private PhoneExt[] mPhones = null;
    private PhoneStateListenerExt mPreciseCallStateListener = new PhoneStateListenerExt() {
        /* class com.android.internal.telephony.InCallDataStateMachine.AnonymousClass2 */

        public void onPreciseCallStateChangedHw(PreciseCallStateExt callState) {
            int foregroundCallState;
            if (callState != null && (foregroundCallState = callState.getForegroundCallState()) != InCallDataStateMachine.this.mForegroundCallState) {
                InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                inCallDataStateMachine.log("onPreciseCallStateChanged foregroundCallState=" + foregroundCallState);
                InCallDataStateMachine.this.mForegroundCallState = foregroundCallState;
                if (1 == foregroundCallState) {
                    InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
                    inCallDataStateMachine2.sendMessage(inCallDataStateMachine2.obtainMessage(InCallDataStateMachine.EVENT_VOICE_CALL_ACTIVE));
                }
            }
        }
    };
    private InCallDataSettingsChangeObserver mSettingsChangeObserver;
    private SlaveActiveState mSlaveActiveState = new SlaveActiveState();
    private TelephonyManager mTelephonyManager;

    public InCallDataStateMachine(Context context, PhoneExt[] phoneExts) {
        super(LOG_TAG, Looper.myLooper());
        this.mContext = context;
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        PhoneExt[] phones = new PhoneExt[phoneExts.length];
        for (int i = 0; i < phoneExts.length; i++) {
            phones[i] = phoneExts[i];
        }
        this.mSettingsChangeObserver = new InCallDataSettingsChangeObserver(getHandler());
        this.mDataEnablerObserver = new DataEnablerObserver(getHandler());
        this.mInCallScreenBroadcastReveiver = new InCallScreenBroadcastReveiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_INCALL_SCREEN);
        filter.addAction(ACTION_HW_DSDS_MODE_STATE);
        context.registerReceiver(this.mInCallScreenBroadcastReveiver, filter);
        this.mPhones = new PhoneExt[2];
        for (int i2 = 0; i2 < 2; i2++) {
            this.mPhones[i2] = phones[i2];
            if (phones[i2].getCallTracker() != null) {
                phones[i2].getCallTracker().registerForVoiceCallEnded(getHandler(), 3, Integer.valueOf(i2));
                phones[i2].getCallTracker().registerForVoiceCallStarted(getHandler(), 2, Integer.valueOf(i2));
                if (!(phones[i2].getImsPhone() == null || phones[i2].getImsPhone().getCallTracker() == null)) {
                    log("registerImsCallStates phoneId = " + i2);
                    phones[i2].getImsPhone().getCallTracker().registerForVoiceCallEnded(getHandler(), 3, Integer.valueOf(i2));
                    phones[i2].getImsPhone().getCallTracker().registerForVoiceCallStarted(getHandler(), 2, Integer.valueOf(i2));
                }
            }
        }
        SubscriptionManagerEx.addOnSubscriptionsChangedListener(this.mContext, this.mOnSubscriptionsChangeListener);
        registerListeners();
        this.mTelephonyManager.listen(this.mPreciseCallStateListener.getPhoneStateListener(), 2048);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mobile_data"), true, this.mDataEnablerObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(SETTINGS_INCALL_DATA_SWITCH), true, this.mSettingsChangeObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(SETTINGS_INTELLIGENCE_CARD_SWITCH), true, this.mSettingsChangeObserver);
        addState(this.mIdleState);
        addState(this.mSlaveActiveState, this.mIdleState);
        addState(this.mActivatingSlaveState, this.mSlaveActiveState);
        addState(this.mActivatedSlaveState, this.mSlaveActiveState);
        addState(this.mDeactivatingSlaveDataState, this.mIdleState);
        addState(this.mDefaultLinkDeletedState, this.mIdleState);
        setInitialState(this.mIdleState);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerListeners() {
        List<SubscriptionInfo> subscriptions = SubscriptionControllerEx.getInstance().getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        for (int i = this.mListeners.size() - 1; i >= 0; i--) {
            int subId = this.mListeners.keyAt(i);
            if (!containsSubId(subscriptions, subId)) {
                logd("cancel listen and revove = " + subId);
                this.mListeners.valueAt(i).cancelListen();
                this.mListeners.remove(subId);
            }
        }
        if (subscriptions != null) {
            int subscriptionsSize = subscriptions.size();
            for (int i2 = 0; i2 < subscriptionsSize; i2++) {
                int subId2 = subscriptions.get(i2).getSubscriptionId();
                if (this.mListeners.indexOfKey(subId2) < 0) {
                    MyPhoneStateListener listener = new MyPhoneStateListener(subId2);
                    listener.listen();
                    this.mListeners.put(subId2, listener);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isInCallDataSwitchOn() {
        int retVal;
        if (SystemPropertiesEx.getBoolean(PROP_SMART_DUAL_CARD_MODE, false)) {
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
    /* access modifiers changed from: public */
    private boolean isSlaveCanActiveData() {
        DcTrackerEx dcTracker;
        if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.isVSimEnabled()) {
            return false;
        }
        log("isSlaveCanActiveData mInCallPhoneId = " + this.mInCallPhoneId);
        int i = this.mInCallPhoneId;
        if (i < 0 || i >= 2) {
            return false;
        }
        if (PROP_DEL_DEFAULT_LINK || this.mDsdsMode != 1) {
            int switchState = SystemPropertiesEx.getInt("persist.sys.smart_switch_state", 0);
            if (switchState == 1 || switchState == 2) {
                log("intelligence state = " + switchState + " return false");
                return false;
            }
            boolean isCTCard = false;
            TelephonyNetworkFactoryEx callingNetworkFactory = PhoneFactoryExt.getTelephonyNetworkFactory(this.mInCallPhoneId);
            if (!(callingNetworkFactory == null || (dcTracker = callingNetworkFactory.getDcTracker()) == null || dcTracker.getHwDcTrackerEx() == null)) {
                isCTCard = dcTracker.getHwDcTrackerEx().isCTSimCard(this.mInCallPhoneId);
            }
            log("isSlaveCanActiveData isCTCard = " + isCTCard);
            boolean disableCsSwitch = SystemPropertiesEx.getBoolean(DISABLE_CS_INCALL_SWITCH, false);
            if ((isCTCard || DISABLE_GW_PS_ATTACH || disableCsSwitch) && !HwTelephonyManagerInner.getDefault().isImsRegistered(this.mInCallPhoneId)) {
                log("isSlaveCanActiveData ct card not switch data when is not volte.");
                return false;
            }
            if (this.mPhones[this.mInCallPhoneId].getImsPhone() == null || this.mPhones[this.mInCallPhoneId].getImsPhone().getCallTracker() == null) {
                loge("isSlaveCanActiveData error happened ims phone is null.");
            } else if (disableCsSwitch && this.mPhones[this.mInCallPhoneId].getImsPhone().getCallTracker().getState() == PhoneConstantsExt.StateEx.IDLE) {
                log("isSlaveCanActiveData not switch data when is not volte calling.");
                return false;
            }
            if (!HwFullNetworkManager.getInstance().isCMCCDsdxDisable() || !HwFullNetworkManager.getInstance().isCMCCHybird()) {
                int networkType = TelephonyManagerEx.getNetworkType(this.mContext, SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(this.mInCallPhoneId));
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
            if (networkType == 13) {
                return true;
            }
            if (!(networkType == 15 || networkType == 30)) {
                if (networkType == 19 || networkType == 20) {
                    return true;
                }
                switch (networkType) {
                    case EVENT_VOICE_CALL_ACTIVE /* 8 */:
                    case EVENT_DSDS_MODE_CHANGE /* 9 */:
                    case 10:
                        break;
                    default:
                        return false;
                }
            }
        }
        if (DISABLE_GW_PS_ATTACH) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldShowDialog() {
        NetworkInfo mWifiNetworkInfo;
        boolean shouldDialog = false;
        boolean isUserDataOn = 1 == Settings.Global.getInt(this.mContext.getContentResolver(), "mobile_data", -1);
        boolean mIsWifiConnected = false;
        int default4GSlot = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        ConnectivityManager mCm = (ConnectivityManager) PhoneFactoryExt.getPhone(default4GSlot).getContext().getSystemService("connectivity");
        if (!(mCm == null || (mWifiNetworkInfo = mCm.getNetworkInfo(1)) == null || mWifiNetworkInfo.getState() != NetworkInfo.State.CONNECTED)) {
            mIsWifiConnected = true;
        }
        boolean isOverDsds3 = this.mDsdsMode == 1 && !PROP_DEL_DEFAULT_LINK;
        if (!isInCallDataSwitchOn() && isSlaveCanActiveData() && isUserDataOn && !mIsWifiConnected && this.mInCallPhoneId != default4GSlot && !isOverDsds3) {
            shouldDialog = true;
        }
        log("shouldDialog is: " + shouldDialog + ",isUserDataOn:" + isUserDataOn + ",mIsWifiConnected:" + mIsWifiConnected + ",mInCallPhoneId:" + this.mInCallPhoneId + ",default4GSlot" + default4GSlot + ", isOverDsds3:" + isOverDsds3);
        return shouldDialog;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyUserToEnableSmartSwitch() {
        if (SystemPropertiesEx.getBoolean(PROP_SMART_DUAL_CARD_MODE, false)) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            long j = this.mLastNotifyTime;
            if (elapsedRealtime - j > MIN_INTERVAL_TIME || j == 0) {
                log("not operated or more then one day from last operate time, last = " + this.mLastNotifyTime);
                this.mLastNotifyTime = SystemClock.elapsedRealtime();
                DecisionUtil.bindService(this.mContext, REC_DECISION_NAME);
            }
        } else if (SystemPropertiesEx.getBoolean("persist.radio.incalldata", false)) {
            log("common tip dialog has showned, do nothing.");
        } else {
            SystemPropertiesEx.set("persist.radio.incalldata", "true");
            showDialog();
        }
    }

    private void showDialog() {
        String toastString = String.format(this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("popupwindow_incall_networking_prompt_text1")), Integer.valueOf(this.mInCallPhoneId + 1));
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, 33947691);
        builder.setTitle(HwPartResourceUtils.getResourceId("popupwindow_networking_prompt_001"));
        builder.setMessage(toastString);
        builder.setPositiveButton(HwPartResourceUtils.getResourceId("data_saver_enable_button"), new DialogInterface.OnClickListener() {
            /* class com.android.internal.telephony.InCallDataStateMachine.AnonymousClass3 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialoginterface, int i) {
                Settings.Global.putInt(InCallDataStateMachine.this.mContext.getContentResolver(), InCallDataStateMachine.SETTINGS_INCALL_DATA_SWITCH, 1);
            }
        });
        builder.setNegativeButton(HwPartResourceUtils.getResourceId("cancel"), (DialogInterface.OnClickListener) null);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(HwFullNetworkConstantsInner.EVENT_GET_PREF_NETWORK_MODE_DONE);
        dialog.setCancelable(false);
        dialog.show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        RlogEx.i(LOG_TAG, s);
    }

    public boolean isDeactivatingSlaveData() {
        return isInSpecificState(this.mDeactivatingSlaveDataState);
    }

    public boolean isSwitchingToSlave() {
        return isInSpecificState(this.mActivatingSlaveState);
    }

    public boolean isSlaveActive() {
        return isInSpecificState(this.mActivatingSlaveState) || isInSpecificState(this.mActivatedSlaveState);
    }

    public void registerImsCallStates(boolean enable, int i) {
        PhoneExt[] phoneExtArr;
        if (i >= 0 && i < 2 && (phoneExtArr = this.mPhones) != null && phoneExtArr[i].getImsPhone() != null && this.mPhones[i].getImsPhone().getCallTracker() != null) {
            if (enable) {
                log("registerImsCallStates phoneId = " + i);
                this.mPhones[i].getImsPhone().getCallTracker().registerForVoiceCallEnded(getHandler(), 3, Integer.valueOf(i));
                this.mPhones[i].getImsPhone().getCallTracker().registerForVoiceCallStarted(getHandler(), 2, Integer.valueOf(i));
                return;
            }
            log("unregisterImsCallStates phoneId = " + i);
            this.mPhones[i].getImsPhone().getCallTracker().unregisterForVoiceCallEnded(getHandler());
            this.mPhones[i].getImsPhone().getCallTracker().unregisterForVoiceCallStarted(getHandler());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPhoneStateIDLE() {
        boolean isIdle = true;
        int i = 0;
        while (true) {
            PhoneExt[] phoneExtArr = this.mPhones;
            if (i < phoneExtArr.length) {
                if (!(phoneExtArr[i] == null || phoneExtArr[i].getCallTracker() == null || this.mPhones[i].getCallTracker().getState() == PhoneConstantsExt.StateEx.IDLE)) {
                    isIdle = false;
                }
                PhoneExt[] phoneExtArr2 = this.mPhones;
                if (!(phoneExtArr2[i] == null || phoneExtArr2[i].getImsPhone() == null || this.mPhones[i].getImsPhone().getCallTracker() == null || this.mPhones[i].getImsPhone().getCallTracker().getState() == PhoneConstantsExt.StateEx.IDLE)) {
                    isIdle = false;
                }
                i++;
            } else {
                log("isPhoneStateIDLE isIdle = " + isIdle);
                return isIdle;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0053, code lost:
        if (com.huawei.internal.telephony.PhoneConstantsExt.StateEx.IDLE != r3.mPhones[r4].getImsPhone().getCallTracker().getState()) goto L_0x0055;
     */
    private boolean isDataPhoneStateIDLE(int dataPhoneId) {
        if (dataPhoneId < 0 || dataPhoneId >= 2) {
            return true;
        }
        boolean isIdle = true;
        PhoneExt[] phoneExtArr = this.mPhones;
        if (phoneExtArr[dataPhoneId] == null || phoneExtArr[dataPhoneId].getCallTracker() == null || PhoneConstantsExt.StateEx.IDLE == this.mPhones[dataPhoneId].getCallTracker().getState()) {
            PhoneExt[] phoneExtArr2 = this.mPhones;
            if (phoneExtArr2[dataPhoneId] != null) {
                if (phoneExtArr2[dataPhoneId].getImsPhone() != null) {
                    if (this.mPhones[dataPhoneId].getImsPhone().getCallTracker() != null) {
                    }
                }
            }
            log("isDataPhoneStateIDLE isIdle = " + isIdle);
            return isIdle;
        }
        isIdle = false;
        log("isDataPhoneStateIDLE isIdle = " + isIdle);
        return isIdle;
    }

    private boolean isCallStateActive() {
        log("isCallStateActive mForegroundCallState = " + this.mForegroundCallState);
        return 1 == this.mForegroundCallState;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resumeDefaultLink() {
        int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        TelephonyNetworkFactoryEx networkFactory = PhoneFactoryExt.getTelephonyNetworkFactory(default4GSlotId);
        DcTrackerEx dcTrackerEx = networkFactory != null ? networkFactory.getDcTracker() : null;
        PhoneExt default4GPhone = PhoneFactoryExt.getPhone(default4GSlotId);
        log("resumeDefaultLink default4GSlotId = " + default4GSlotId);
        if (dcTrackerEx != null && default4GPhone != null) {
            dcTrackerEx.resumeDefaultLink();
            default4GPhone.notifyDataConnection("default");
        }
    }

    private boolean isDefaultDataConnected() {
        TelephonyManager telephonyManager = null;
        Context context = this.mContext;
        if (context != null) {
            telephonyManager = (TelephonyManager) context.getSystemService("phone");
        }
        if (telephonyManager == null || 2 != telephonyManager.getDataState()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedEnterDefaultLinkDeletedState() {
        int i;
        int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        int switchState = SystemPropertiesEx.getInt("persist.sys.smart_switch_state", 0);
        if (TelephonyManagerEx.MultiSimVariantsExt.DSDS != TelephonyManagerEx.getMultiSimConfiguration()) {
            log("isNeedEnterDefaultLinkDeletedState getMultiSimConfiguration=DSDA");
            return false;
        } else if (!isInSpecificState(this.mIdleState) || (i = this.mInCallPhoneId) < 0 || i >= 2 || i == default4GSlotId || true != isCallStateActive() || true != isDefaultDataConnected() || true != PROP_DEL_DEFAULT_LINK || (isInCallDataSwitchOn() && (true != isInCallDataSwitchOn() || isSlaveCanActiveData() || switchState != 0))) {
            return false;
        } else {
            log("isNeedEnterDefaultLinkDeletedState true,mInCallPhoneId=" + this.mInCallPhoneId + " default4GSlotId=" + default4GSlotId);
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean is4GSlotCanActiveData() {
        DcTrackerEx dcTracker;
        int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        log("is4GSlotCanActiveData default4GSlotId = " + default4GSlotId);
        if (default4GSlotId < 0 || default4GSlotId >= 2) {
            return false;
        }
        boolean isCTCard = false;
        TelephonyNetworkFactoryEx callingNetworkFactory = PhoneFactoryExt.getTelephonyNetworkFactory(default4GSlotId);
        if (!(callingNetworkFactory == null || (dcTracker = callingNetworkFactory.getDcTracker()) == null || dcTracker.getHwDcTrackerEx() == null)) {
            isCTCard = dcTracker.getHwDcTrackerEx().isCTSimCard(default4GSlotId);
        }
        log("is4GSlotCanActiveData isCTCard = " + isCTCard);
        if (!isCTCard || HwTelephonyManagerInner.getDefault().isImsRegistered(default4GSlotId)) {
            int networkType = TelephonyManagerEx.getNetworkType(this.mContext, SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(default4GSlotId));
            log("is4GSlotCanActiveData networkType = " + networkType);
            return canActiveDataByNetworkType(networkType);
        }
        log("is4GSlotCanActiveData CT can not active data when is not volte ");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportSlaveActivedToChr(byte defaultDataSubId, byte default4GSlotId, int networkType) {
        log("ActiviedSlaveState report to CHR, networkType = " + networkType);
        Bundle data = new Bundle();
        data.putString("EventScenario", "INCALLDATA");
        data.putInt("EventFailCause", HwFullNetworkConstantsInner.EVENT_ICC_STATUS_CHANGED);
        data.putByte("DATACONN.INCALLDATA.InCallSubId", defaultDataSubId);
        data.putByte("DATACONN.INCALLDATA.default4gSubId", default4GSlotId);
        data.putInt("DATACONN.INCALLDATA.networkType", networkType);
        HwPartTelephonyFactory.loadFactory("android.telephony.HwPartTelephonyFactoryImpl").getHwTelephonyChrManager().sendTelephonyChrBroadcast(data, defaultDataSubId);
    }

    private boolean containsSubId(List<SubscriptionInfo> subInfos, int subId) {
        if (subInfos == null) {
            return false;
        }
        for (SubscriptionInfo subInfo : subInfos) {
            if (subInfo != null && subInfo.getSubscriptionId() == subId) {
                return true;
            }
        }
        return false;
    }

    private class InCallDataSettingsChangeObserver extends ContentObserver {
        InCallDataSettingsChangeObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            if (InCallDataStateMachine.this.isInCallDataSwitchOn()) {
                InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                inCallDataStateMachine.sendMessage(inCallDataStateMachine.obtainMessage(0));
                return;
            }
            InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
            inCallDataStateMachine2.sendMessage(inCallDataStateMachine2.obtainMessage(1));
        }
    }

    private class DataEnablerObserver extends ContentObserver {
        DataEnablerObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            int retVal = Settings.Global.getInt(InCallDataStateMachine.this.mContext.getContentResolver(), "mobile_data", -1);
            InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
            inCallDataStateMachine.log("DataEnablerObserver onChange retVal = " + retVal);
            if (retVal == 0) {
                InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
                inCallDataStateMachine2.sendMessage(inCallDataStateMachine2.obtainMessage(InCallDataStateMachine.EVENT_USER_DISABLE_DATA));
            }
        }
    }

    class InCallScreenBroadcastReveiver extends BroadcastReceiver {
        InCallScreenBroadcastReveiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (InCallDataStateMachine.ACTION_INCALL_SCREEN.equals(action)) {
                    InCallDataStateMachine.this.isInCallUIForeground = intent.getBooleanExtra("IsForegroundActivity", true);
                    InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                    inCallDataStateMachine.log("InCallScreenBroadcastReveiver onReceive isInCallUIForeground = " + InCallDataStateMachine.this.isInCallUIForeground);
                    if (!InCallDataStateMachine.this.isInCallUIForeground) {
                        InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
                        inCallDataStateMachine2.sendMessage(inCallDataStateMachine2.obtainMessage(7));
                    }
                } else if (InCallDataStateMachine.ACTION_HW_DSDS_MODE_STATE.equals(action) && !InCallDataStateMachine.PROP_DEL_DEFAULT_LINK) {
                    int newDsdsMode = intent.getIntExtra(InCallDataStateMachine.DSDS_KEY, 0);
                    boolean isNrSupport = HwTelephonyManager.getDefault().isNrSupported();
                    InCallDataStateMachine inCallDataStateMachine3 = InCallDataStateMachine.this;
                    inCallDataStateMachine3.log("BroadcastReveiver onReceive newDsdsMode = " + newDsdsMode + " isNrSupport = " + isNrSupport);
                    if (InCallDataStateMachine.this.hasMessages(InCallDataStateMachine.EVENT_DSDS_MODE_CHANGE) && InCallDataStateMachine.this.mDsdsMode == newDsdsMode) {
                        InCallDataStateMachine.this.removeMessages(InCallDataStateMachine.EVENT_DSDS_MODE_CHANGE);
                    }
                    if (InCallDataStateMachine.this.mDsdsMode == newDsdsMode) {
                        return;
                    }
                    if (isNrSupport) {
                        InCallDataStateMachine inCallDataStateMachine4 = InCallDataStateMachine.this;
                        inCallDataStateMachine4.sendMessageDelayed(inCallDataStateMachine4.obtainMessage(InCallDataStateMachine.EVENT_DSDS_MODE_CHANGE, newDsdsMode), InCallDataStateMachine.DSDS_STATE_UPDATE_DELAY_TIMER_MS);
                        return;
                    }
                    InCallDataStateMachine inCallDataStateMachine5 = InCallDataStateMachine.this;
                    inCallDataStateMachine5.sendMessage(inCallDataStateMachine5.obtainMessage(InCallDataStateMachine.EVENT_DSDS_MODE_CHANGE, newDsdsMode));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class IdleState extends StateEx {
        private IdleState() {
        }

        public void enter() {
            InCallDataStateMachine.this.log("IdleState enter");
            if (InCallDataStateMachine.this.isNeedEnterDefaultLinkDeletedState()) {
                InCallDataStateMachine.this.log("transitionTo mDefaultLinkDeletedState");
                InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                inCallDataStateMachine.transitionTo(inCallDataStateMachine.mDefaultLinkDeletedState);
            }
        }

        public boolean processMessage(Message msg) {
            int default4GSlotId;
            int i = msg.what;
            if (i == 0) {
                InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                inCallDataStateMachine.log("IdleState processMessage EVENT_INCALL_DATA_SETTINGS_ON isInCallUIForeground = " + InCallDataStateMachine.this.isInCallUIForeground);
                if (InCallDataStateMachine.this.isInCallDataSwitchOn() && InCallDataStateMachine.this.isSlaveCanActiveData()) {
                    int default4GSlotId2 = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                    InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
                    inCallDataStateMachine2.log("call phoneId = " + InCallDataStateMachine.this.mInCallPhoneId + "main 4G slotId = " + default4GSlotId2);
                    if (InCallDataStateMachine.this.mInCallPhoneId != default4GSlotId2) {
                        InCallDataStateMachine inCallDataStateMachine3 = InCallDataStateMachine.this;
                        inCallDataStateMachine3.transitionTo(inCallDataStateMachine3.mActivatingSlaveState);
                        InCallDataStateMachine inCallDataStateMachine4 = InCallDataStateMachine.this;
                        inCallDataStateMachine4.log("IdleState setDefaultDataSubId to " + InCallDataStateMachine.this.mInCallPhoneId);
                        HwSubscriptionManager.getInstance().setDefaultDataSubIdBySlotId(InCallDataStateMachine.this.mInCallPhoneId);
                    }
                }
            } else if (i == 5) {
                int dataPhoneId = Integer.valueOf(msg.arg1).intValue();
                int default4GSlotId3 = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                InCallDataStateMachine inCallDataStateMachine5 = InCallDataStateMachine.this;
                inCallDataStateMachine5.log("IdleState processMessage EVENT_DATA_CONNECTED phoneId = " + dataPhoneId + " default4GSlotId=" + default4GSlotId3);
                if (true == InCallDataStateMachine.this.isNeedEnterDefaultLinkDeletedState() && dataPhoneId == default4GSlotId3) {
                    InCallDataStateMachine.this.log("transitionTo mDefaultLinkDeletedState");
                    InCallDataStateMachine inCallDataStateMachine6 = InCallDataStateMachine.this;
                    inCallDataStateMachine6.transitionTo(inCallDataStateMachine6.mDefaultLinkDeletedState);
                }
            } else if (i == 2) {
                InCallDataStateMachine.this.log("IdleState processMessage EVENT_CALL_START");
                AsyncResultEx ar = AsyncResultEx.from(msg.obj);
                if (ar == null || !(ar.getUserObj() instanceof Integer)) {
                    InCallDataStateMachine.this.logd("EVENT_VOICE_CALL_STARTED error ar");
                } else {
                    InCallDataStateMachine.this.mInCallPhoneId = ((Integer) ar.getUserObj()).intValue();
                    if (InCallDataStateMachine.this.isInCallDataSwitchOn() && InCallDataStateMachine.this.isSlaveCanActiveData()) {
                        int default4GSlotId4 = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                        InCallDataStateMachine inCallDataStateMachine7 = InCallDataStateMachine.this;
                        inCallDataStateMachine7.log("call phoneId = " + InCallDataStateMachine.this.mInCallPhoneId + "main 4G slotId = " + default4GSlotId4);
                        if (InCallDataStateMachine.this.mInCallPhoneId != default4GSlotId4) {
                            InCallDataStateMachine inCallDataStateMachine8 = InCallDataStateMachine.this;
                            inCallDataStateMachine8.transitionTo(inCallDataStateMachine8.mActivatingSlaveState);
                            InCallDataStateMachine inCallDataStateMachine9 = InCallDataStateMachine.this;
                            inCallDataStateMachine9.log("IdleState setDefaultDataSubId to " + InCallDataStateMachine.this.mInCallPhoneId);
                            HwSubscriptionManager.getInstance().setDefaultDataSubIdBySlotId(InCallDataStateMachine.this.mInCallPhoneId);
                        }
                    }
                }
            } else if (i == 3) {
                InCallDataStateMachine.this.log("IdleState processMessage EVENT_VOICE_CALL_ENDED");
                if (InCallDataStateMachine.this.isPhoneStateIDLE()) {
                    InCallDataStateMachine.this.log("IdleState set mInCallPhoneId -1");
                    InCallDataStateMachine.this.mInCallPhoneId = -1;
                    InCallDataStateMachine.this.isInCallUIForeground = false;
                }
                if (InCallDataStateMachine.this.hasMessages(InCallDataStateMachine.EVENT_DSDS_MODE_CHANGE)) {
                    InCallDataStateMachine.this.removeMessages(InCallDataStateMachine.EVENT_DSDS_MODE_CHANGE);
                }
            } else if (i == 7) {
                InCallDataStateMachine.this.log("IdleState processMessage EVENT_INCALLUI_BACKGROUND");
                if (InCallDataStateMachine.this.shouldShowDialog()) {
                    InCallDataStateMachine.this.notifyUserToEnableSmartSwitch();
                }
            } else if (i == InCallDataStateMachine.EVENT_VOICE_CALL_ACTIVE) {
                InCallDataStateMachine.this.log("IdleState processMessage EVENT_VOICE_CALL_ACTIVE");
                if (true == InCallDataStateMachine.this.isNeedEnterDefaultLinkDeletedState()) {
                    InCallDataStateMachine.this.log("transitionTo mDefaultLinkDeletedState");
                    InCallDataStateMachine inCallDataStateMachine10 = InCallDataStateMachine.this;
                    inCallDataStateMachine10.transitionTo(inCallDataStateMachine10.mDefaultLinkDeletedState);
                }
            } else if (i != InCallDataStateMachine.EVENT_DSDS_MODE_CHANGE) {
                InCallDataStateMachine inCallDataStateMachine11 = InCallDataStateMachine.this;
                inCallDataStateMachine11.log("IdleState: default msg.what=" + msg.what);
            } else {
                InCallDataStateMachine.this.log("IdleState processMessage EVENT_DSDS_MODE_CHANGE");
                InCallDataStateMachine.this.mDsdsMode = msg.arg1;
                if (InCallDataStateMachine.this.isInCallDataSwitchOn() && InCallDataStateMachine.this.isSlaveCanActiveData() && InCallDataStateMachine.this.mInCallPhoneId != (default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId())) {
                    InCallDataStateMachine inCallDataStateMachine12 = InCallDataStateMachine.this;
                    inCallDataStateMachine12.transitionTo(inCallDataStateMachine12.mActivatingSlaveState);
                    InCallDataStateMachine inCallDataStateMachine13 = InCallDataStateMachine.this;
                    inCallDataStateMachine13.log("call phoneId = " + InCallDataStateMachine.this.mInCallPhoneId + "main 4G slotId = " + default4GSlotId);
                    InCallDataStateMachine inCallDataStateMachine14 = InCallDataStateMachine.this;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Found DSDS 2.0 state, setDefaultDataSubId to ");
                    sb.append(InCallDataStateMachine.this.mInCallPhoneId);
                    inCallDataStateMachine14.log(sb.toString());
                    HwSubscriptionManager.getInstance().setDefaultDataSubIdBySlotId(InCallDataStateMachine.this.mInCallPhoneId);
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class ActivatingSlaveState extends StateEx {
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
            InCallDataStateMachine inCallDataStateMachine3 = InCallDataStateMachine.this;
            inCallDataStateMachine3.transitionTo(inCallDataStateMachine3.mActivatedSlaveState);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class ActivatedSlaveState extends StateEx {
        private ActivatedSlaveState() {
        }

        public void enter() {
            int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
            inCallDataStateMachine.log("ActivatedSlaveState enter notifyDataConnection disconnected phoneId = " + default4GSlotId);
            PhoneFactoryExt.getPhone(default4GSlotId).notifyDataConnection("default", PhoneConstantsExt.DataStateEx.DISCONNECTED);
            int defaultDataSubId = SubscriptionControllerEx.getInstance().getDefaultDataSubId();
            InCallDataStateMachine.this.reportSlaveActivedToChr((byte) defaultDataSubId, (byte) default4GSlotId, TelephonyManagerEx.getNetworkType(InCallDataStateMachine.this.mContext, defaultDataSubId));
        }

        public boolean processMessage(Message msg) {
            InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
            inCallDataStateMachine.log("ActivatedSlaveState: default msg.what=" + msg.what);
            return false;
        }
    }

    private class SlaveActiveState extends StateEx {
        private SlaveActiveState() {
        }

        public boolean processMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_INCALL_DATA_SETTINGS_ON");
                return true;
            } else if (i == 1) {
                InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_INCALL_DATA_SETTINGS_OFF");
                if (InCallDataStateMachine.this.isInCallDataSwitchOn()) {
                    return true;
                }
                int defaultDataSlotId = SubscriptionControllerEx.getInstance().getSlotIndex(SubscriptionControllerEx.getInstance().getDefaultDataSubId());
                int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                inCallDataStateMachine.log("defaultDataSlotId = " + defaultDataSlotId + "main 4G slotId = " + default4GSlotId);
                if (defaultDataSlotId == default4GSlotId || !SubscriptionManagerEx.isValidSlotIndex(defaultDataSlotId)) {
                    InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
                    inCallDataStateMachine2.transitionTo(inCallDataStateMachine2.mIdleState);
                    return true;
                }
                InCallDataStateMachine inCallDataStateMachine3 = InCallDataStateMachine.this;
                inCallDataStateMachine3.transitionTo(inCallDataStateMachine3.mDeactivatingSlaveDataState);
                return true;
            } else if (i == 3) {
                InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_VOICE_CALL_ENDED");
                int defaultDataSlotId2 = SubscriptionControllerEx.getInstance().getSlotIndex(SubscriptionControllerEx.getInstance().getDefaultDataSubId());
                int default4GSlotId2 = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                if (!InCallDataStateMachine.this.isDataPhoneStateIDLE(defaultDataSlotId2)) {
                    return true;
                }
                InCallDataStateMachine inCallDataStateMachine4 = InCallDataStateMachine.this;
                inCallDataStateMachine4.log("defaultDataSlotId = " + defaultDataSlotId2 + "main 4G slotId = " + default4GSlotId2);
                if (defaultDataSlotId2 == default4GSlotId2 || !SubscriptionManagerEx.isValidSlotIndex(defaultDataSlotId2)) {
                    InCallDataStateMachine inCallDataStateMachine5 = InCallDataStateMachine.this;
                    inCallDataStateMachine5.transitionTo(inCallDataStateMachine5.mIdleState);
                } else {
                    InCallDataStateMachine inCallDataStateMachine6 = InCallDataStateMachine.this;
                    inCallDataStateMachine6.transitionTo(inCallDataStateMachine6.mDeactivatingSlaveDataState);
                }
                InCallDataStateMachine.this.mInCallPhoneId = -1;
                InCallDataStateMachine.this.isInCallUIForeground = false;
                return true;
            } else if (i == InCallDataStateMachine.EVENT_DSDS_MODE_CHANGE) {
                InCallDataStateMachine.this.mDsdsMode = msg.arg1;
                InCallDataStateMachine.this.log("SlaveActiveState drop msg EVENT_DSDS_MODE_CHANGE");
                return true;
            } else if (i == InCallDataStateMachine.EVENT_USER_DISABLE_DATA) {
                InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_USER_DISABLE_DATA");
                TelephonyNetworkFactoryEx networkFactory = PhoneFactoryExt.getTelephonyNetworkFactory(HwTelephonyManagerInner.getDefault().getDefault4GSlotId());
                if (networkFactory == null || networkFactory.getDcTracker() == null) {
                    return true;
                }
                networkFactory.getDcTracker().setUserDataEnabled(false);
                return true;
            } else if (i != 7) {
                InCallDataStateMachine inCallDataStateMachine7 = InCallDataStateMachine.this;
                inCallDataStateMachine7.log("SlaveActiveState: default msg.what=" + msg.what);
                return false;
            } else {
                InCallDataStateMachine.this.log("SlaveActiveState processMessage EVENT_INCALLUI_BACKGROUND");
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public class DeactivatingSlaveDataState extends StateEx {
        private DeactivatingSlaveDataState() {
        }

        public void enter() {
            int defaultDataSubId = SubscriptionControllerEx.getInstance().getDefaultDataSubId();
            int defaultDataSlotId = SubscriptionControllerEx.getInstance().getSlotIndex(defaultDataSubId);
            int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
            inCallDataStateMachine.log("DeactivatingSlaveDataState enter defaultDataSlotId = " + defaultDataSlotId + "main 4G slotId = " + default4GSlotId);
            if (defaultDataSlotId == default4GSlotId || !SubscriptionManagerEx.isValidSlotIndex(defaultDataSlotId)) {
                InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
                inCallDataStateMachine2.transitionTo(inCallDataStateMachine2.mIdleState);
            } else {
                HwSubscriptionManager.getInstance().setDefaultDataSubIdBySlotId(default4GSlotId);
            }
            if (SubscriptionManagerEx.isValidSubscriptionId(defaultDataSubId)) {
                int mListenersSize = InCallDataStateMachine.this.mListeners.size();
                for (int i = 0; i < mListenersSize; i++) {
                    if (InCallDataStateMachine.this.mListeners.keyAt(i) == defaultDataSubId) {
                        int currentDataState = ((MyPhoneStateListener) InCallDataStateMachine.this.mListeners.valueAt(i)).mCurrentDataState;
                        InCallDataStateMachine inCallDataStateMachine3 = InCallDataStateMachine.this;
                        inCallDataStateMachine3.log("mCurrentDataState = " + currentDataState);
                        if (!(currentDataState == 2 || currentDataState == 3)) {
                            TelephonyNetworkFactoryEx networkFactory = PhoneFactoryExt.getTelephonyNetworkFactory(default4GSlotId);
                            ApnContextEx apnContext = null;
                            DcTrackerEx dcTracker = networkFactory != null ? networkFactory.getDcTracker() : null;
                            ConcurrentHashMap<String, ApnContextEx> apnContextMaps = dcTracker != null ? dcTracker.getApnContextHw() : null;
                            if (apnContextMaps != null) {
                                apnContext = apnContextMaps.get("default");
                            }
                            if (!(apnContext == null || apnContext.getState() == ApnContextEx.StateEx.CONNECTED)) {
                                InCallDataStateMachine.this.log("DeactivatingSlaveDataState enter slave already diconnected");
                                InCallDataStateMachine inCallDataStateMachine4 = InCallDataStateMachine.this;
                                inCallDataStateMachine4.transitionTo(inCallDataStateMachine4.mIdleState);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                }
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
                InCallDataStateMachine inCallDataStateMachine3 = InCallDataStateMachine.this;
                inCallDataStateMachine3.transitionTo(inCallDataStateMachine3.mIdleState);
                return true;
            } else if (i != 7) {
                InCallDataStateMachine inCallDataStateMachine4 = InCallDataStateMachine.this;
                inCallDataStateMachine4.log("DeactivatingSlaveDataState: default msg.what=" + msg.what);
                return false;
            } else {
                InCallDataStateMachine.this.log("DeactivatingSlaveDataState processMessage EVENT_INCALLUI_BACKGROUND");
                return true;
            }
        }

        public void exit() {
            int defaultDataSlotId = SubscriptionControllerEx.getInstance().getSlotIndex(SubscriptionControllerEx.getInstance().getDefaultDataSubId());
            InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
            inCallDataStateMachine.log("DeactivatingSlaveDataState exit defaultDataSlotId = " + defaultDataSlotId);
            if (SubscriptionManagerEx.isValidPhoneId(defaultDataSlotId)) {
                TelephonyNetworkFactoryEx activeNetworkFactory = PhoneFactoryExt.getTelephonyNetworkFactory(defaultDataSlotId);
                DcTrackerEx dcTrackerEx = activeNetworkFactory != null ? activeNetworkFactory.getDcTracker() : null;
                if (dcTrackerEx != null) {
                    dcTrackerEx.resumeDefaultLink();
                }
            }
            int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
            inCallDataStateMachine2.log("DeactivatingSlaveDataState exit notifyDataConnection phoneId = " + default4GSlotId);
            PhoneFactoryExt.getPhone(default4GSlotId).notifyDataConnection("default");
        }
    }

    /* access modifiers changed from: private */
    public class DefaultLinkDeletedState extends StateEx {
        private DefaultLinkDeletedState() {
        }

        public void enter() {
            InCallDataStateMachine.this.log("DefaultLinkDeletedState enter");
            int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            TelephonyNetworkFactoryEx networkFactory = PhoneFactoryExt.getTelephonyNetworkFactory(default4GSlotId);
            if (networkFactory != null) {
                DcTrackerEx dcTracker = networkFactory.getDcTracker();
                PhoneExt default4GPhone = PhoneFactoryExt.getPhone(default4GSlotId);
                InCallDataStateMachine.this.log("DefaultLinkDeletedState clearDefaultLink");
                if (dcTracker != null && default4GPhone != null) {
                    dcTracker.clearDefaultLink();
                    InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                    inCallDataStateMachine.log("DefaultLinkDeletedState notifyDataConnection disconnected phoneId = " + default4GSlotId);
                    default4GPhone.notifyDataConnection("default", PhoneConstantsExt.DataStateEx.DISCONNECTED);
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
                    AsyncResultEx ar = AsyncResultEx.from(msg.obj);
                    if (ar == null || !(ar.getUserObj() instanceof Integer)) {
                        InCallDataStateMachine.this.logd("EVENT_VOICE_CALL_STARTED error ar");
                        return true;
                    }
                    InCallDataStateMachine.this.mInCallPhoneId = ((Integer) ar.getUserObj()).intValue();
                    return true;
                case 3:
                    int callEndPhoneId = -1;
                    int default4GSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
                    InCallDataStateMachine.this.log("DefaultLinkDeletedState processMessage EVENT_VOICE_CALL_ENDED");
                    AsyncResultEx ar2 = AsyncResultEx.from(msg.obj);
                    if (ar2 == null || !(ar2.getUserObj() instanceof Integer)) {
                        InCallDataStateMachine.this.logd("EVENT_VOICE_CALL_ENDED error ar");
                    } else {
                        callEndPhoneId = ((Integer) ar2.getUserObj()).intValue();
                    }
                    if (InCallDataStateMachine.this.isPhoneStateIDLE()) {
                        InCallDataStateMachine.this.log("DefaultLinkDeletedState transitionTo IdleState");
                        InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
                        inCallDataStateMachine.transitionTo(inCallDataStateMachine.mIdleState);
                        InCallDataStateMachine.this.mInCallPhoneId = -1;
                        InCallDataStateMachine.this.isInCallUIForeground = false;
                        return true;
                    } else if (callEndPhoneId < 0 || callEndPhoneId >= 2 || callEndPhoneId == default4GSlotId || !InCallDataStateMachine.this.is4GSlotCanActiveData()) {
                        return true;
                    } else {
                        InCallDataStateMachine.this.log("DefaultLinkDeletedState 4GSlotCanActiveData,transitionTo IdleState");
                        InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
                        inCallDataStateMachine2.transitionTo(inCallDataStateMachine2.mIdleState);
                        return true;
                    }
                case 4:
                    InCallDataStateMachine.this.log("DefaultLinkDeletedState processMessage EVENT_DATA_DISCONNECTED");
                    return true;
                case 5:
                    InCallDataStateMachine.this.log("DefaultLinkDeletedState processMessage EVENT_DATA_CONNECTED");
                    return true;
                case InCallDataStateMachine.EVENT_USER_DISABLE_DATA /* 6 */:
                    InCallDataStateMachine.this.log("DefaultLinkDeletedState processMessage EVENT_USER_DISABLE_DATA,transitionTo(mIdleState)");
                    InCallDataStateMachine inCallDataStateMachine3 = InCallDataStateMachine.this;
                    inCallDataStateMachine3.transitionTo(inCallDataStateMachine3.mIdleState);
                    return true;
                case 7:
                default:
                    InCallDataStateMachine inCallDataStateMachine4 = InCallDataStateMachine.this;
                    inCallDataStateMachine4.log("DefaultLinkDeletedState: default msg.what=" + msg.what);
                    return false;
                case InCallDataStateMachine.EVENT_VOICE_CALL_ACTIVE /* 8 */:
                    InCallDataStateMachine.this.log("DefaultLinkDeletedState processMessage EVENT_VOICE_CALL_ACTIVE");
                    return true;
            }
        }

        public void exit() {
            InCallDataStateMachine.this.log("DefaultLinkDeletedState exit()");
            InCallDataStateMachine.this.resumeDefaultLink();
        }
    }

    /* access modifiers changed from: private */
    public class MyPhoneStateListener extends PhoneStateListenerEx {
        int mCurrentDataState = -1;
        int mSubId;

        MyPhoneStateListener(int subId) {
            super(subId);
            this.mSubId = subId;
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.internal.telephony.InCallDataStateMachine$MyPhoneStateListener */
        /* JADX WARN: Multi-variable type inference failed */
        /* access modifiers changed from: package-private */
        public void listen() {
            InCallDataStateMachine.this.mTelephonyManager.listen(this, 64);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.internal.telephony.InCallDataStateMachine$MyPhoneStateListener */
        /* JADX WARN: Multi-variable type inference failed */
        /* access modifiers changed from: package-private */
        public void cancelListen() {
            InCallDataStateMachine.this.mTelephonyManager.listen(this, 0);
        }

        public void onDataConnectionStateChanged(int state) {
            InCallDataStateMachine inCallDataStateMachine = InCallDataStateMachine.this;
            inCallDataStateMachine.log("onDataConnectionStateChanged mSubId = " + this.mSubId + " state = " + state);
            this.mCurrentDataState = state;
            int slotId = SubscriptionControllerEx.getInstance().getSlotIndex(this.mSubId);
            if (!SubscriptionManagerEx.isValidSlotIndex(slotId)) {
                InCallDataStateMachine inCallDataStateMachine2 = InCallDataStateMachine.this;
                inCallDataStateMachine2.log("onDataConnectionStateChanged slotId = " + slotId + " is invalid, return.");
            } else if (state == 0) {
                InCallDataStateMachine inCallDataStateMachine3 = InCallDataStateMachine.this;
                inCallDataStateMachine3.sendMessage(inCallDataStateMachine3.obtainMessage(4, slotId));
            } else if (2 == state || 3 == state) {
                InCallDataStateMachine inCallDataStateMachine4 = InCallDataStateMachine.this;
                inCallDataStateMachine4.sendMessage(inCallDataStateMachine4.obtainMessage(5, slotId));
            }
        }
    }
}
