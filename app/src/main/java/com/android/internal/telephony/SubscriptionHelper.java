package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import android.util.Log;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

class SubscriptionHelper extends Handler {
    private static final String APM_SIM_NOT_PWDN_PROPERTY = "persist.radio.apm_sim_not_pwdn";
    public static final byte[] C1 = null;
    private static final int EVENT_RADIO_ON = 4;
    private static final int EVENT_REFRESH = 2;
    private static final int EVENT_SET_UICC_SUBSCRIPTION_DONE = 1;
    private static final boolean IS_SETUICCSUB_BY_SLOT = false;
    private static final String LOG_TAG = "SubHelper";
    public static final int SUB_INIT_STATE = -1;
    public static final int SUB_SET_UICC_FAIL = -100;
    public static final int SUB_SIM_NOT_INSERTED = -99;
    private static final int SUB_SIM_REFRESH = -101;
    private static boolean mNwModeUpdated;
    private static final boolean sApmSIMNotPwdn = false;
    private static SubscriptionHelper sInstance;
    private static int sNumPhones;
    private static boolean sTriggerDds;
    private int INVALID_VALUE;
    private CommandsInterface[] mCi;
    private Context mContext;
    private boolean[] mNeedResetSub;
    private int[] mNewSubState;
    private int[] mSubStatus;
    private final ContentObserver nwModeObserver;

    /* renamed from: com.android.internal.telephony.SubscriptionHelper.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfUpdate) {
            SubscriptionHelper.logd("NwMode Observer onChange hit !!!");
            if (SubscriptionHelper.mNwModeUpdated) {
                SubscriptionHelper.this.updateNwModesInSubIdTable(true);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.SubscriptionHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.SubscriptionHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SubscriptionHelper.<clinit>():void");
    }

    public static SubscriptionHelper init(Context c, CommandsInterface[] ci) {
        SubscriptionHelper subscriptionHelper;
        synchronized (SubscriptionHelper.class) {
            if (sInstance == null) {
                sInstance = new SubscriptionHelper(c, ci);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            subscriptionHelper = sInstance;
        }
        return subscriptionHelper;
    }

    public static SubscriptionHelper getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return sInstance;
    }

    private SubscriptionHelper(Context c, CommandsInterface[] ci) {
        this.INVALID_VALUE = SUB_INIT_STATE;
        this.nwModeObserver = new AnonymousClass1(new Handler());
        this.mContext = c;
        this.mCi = ci;
        sNumPhones = TelephonyManager.getDefault().getPhoneCount();
        this.mSubStatus = new int[sNumPhones];
        this.mNewSubState = new int[sNumPhones];
        this.mNeedResetSub = new boolean[sNumPhones];
        for (int i = 0; i < sNumPhones; i += EVENT_SET_UICC_SUBSCRIPTION_DONE) {
            this.mSubStatus[i] = SUB_INIT_STATE;
            this.mNewSubState[i] = this.INVALID_VALUE;
            this.mNeedResetSub[i] = IS_SETUICCSUB_BY_SLOT;
            Integer index = Integer.valueOf(i);
            this.mCi[i].registerForIccRefresh(this, EVENT_REFRESH, index);
            this.mCi[i].registerForOn(this, EVENT_RADIO_ON, index);
        }
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("preferred_network_mode"), IS_SETUICCSUB_BY_SLOT, this.nwModeObserver);
        logd("SubscriptionHelper init by Context, num phones = " + sNumPhones + " ApmSIMNotPwdn = " + sApmSIMNotPwdn);
    }

    private void updateNwModesInSubIdTable(boolean override) {
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        for (int i = 0; i < sNumPhones; i += EVENT_SET_UICC_SUBSCRIPTION_DONE) {
            int[] subIdList = subCtrlr.getSubId(i);
            if (subIdList != null && subIdList[0] >= 0) {
                int nwModeInDb;
                try {
                    nwModeInDb = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", i);
                } catch (SettingNotFoundException e) {
                    loge("Settings Exception Reading Value At Index[" + i + "] Settings.Global.PREFERRED_NETWORK_MODE");
                    nwModeInDb = RILConstants.PREFERRED_NETWORK_MODE;
                }
                int nwModeinSubIdTable = subCtrlr.getNwMode(subIdList[0]);
                logd("updateNwModesInSubIdTable: nwModeinSubIdTable: " + nwModeinSubIdTable + ", nwModeInDb: " + nwModeInDb);
                if (override || nwModeinSubIdTable == SUB_INIT_STATE) {
                    subCtrlr.setNwMode(subIdList[0], nwModeInDb);
                }
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_SET_UICC_SUBSCRIPTION_DONE /*1*/:
                logd("EVENT_SET_UICC_SUBSCRIPTION_DONE");
                processSetUiccSubscriptionDone(msg);
            case EVENT_REFRESH /*2*/:
                logd("EVENT_REFRESH");
                processSimRefresh((AsyncResult) msg.obj);
            case EVENT_RADIO_ON /*4*/:
                Integer Index = msg.obj.userObj;
                logd("EVENT_RADIO_ON: Index" + Index);
                if (Index.intValue() != this.INVALID_VALUE && this.mNewSubState[Index.intValue()] != this.INVALID_VALUE && this.mNeedResetSub[Index.intValue()]) {
                    logd("EVENT_RADIO_ON: Need to reset UICC Subscription,Index = " + Index + ";mNewSubState = " + this.mNewSubState[Index.intValue()]);
                    setUiccSubscription(Index.intValue(), this.mNewSubState[Index.intValue()]);
                }
            default:
        }
    }

    public boolean needSubActivationAfterRefresh(int slotId) {
        return (sNumPhones <= EVENT_SET_UICC_SUBSCRIPTION_DONE || this.mSubStatus[slotId] != SUB_SIM_REFRESH) ? IS_SETUICCSUB_BY_SLOT : true;
    }

    public void updateSubActivation(int[] simStatus, boolean isStackReadyEvent) {
        boolean isPrimarySubFeatureEnable = SystemProperties.getBoolean("persist.radio.primarycard", IS_SETUICCSUB_BY_SLOT);
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        boolean setUiccSent = IS_SETUICCSUB_BY_SLOT;
        if (isStackReadyEvent && !isPrimarySubFeatureEnable) {
            sTriggerDds = true;
        }
        for (int slotId = 0; slotId < sNumPhones; slotId += EVENT_SET_UICC_SUBSCRIPTION_DONE) {
            logd("slot[" + slotId + "] simStatus = " + simStatus[slotId]);
            int[] subId = subCtrlr.getSubId(slotId);
            switch (simStatus[slotId]) {
                case SUB_SIM_NOT_INSERTED /*-99*/:
                    this.mSubStatus[slotId] = simStatus[slotId];
                    logd("slot[" + slotId + "] sim is not insert.");
                    break;
                case -3:
                case HwVSimConstants.ERR_GET_VSIM_VER_NOT_SUPPORT /*-2*/:
                case EVENT_SET_UICC_SUBSCRIPTION_DONE /*1*/:
                case EVENT_REFRESH /*2*/:
                    if (!HwVSimUtils.isVSimInProcess() && !HwVSimUtils.isVSimCauseCardReload()) {
                        logd("slot[" + slotId + "] sim has changed, should activate it.");
                        subCtrlr.activateSubId(subId[0]);
                        HwVSimUtils.setSubActived(subId[0]);
                        setUiccSent = true;
                        break;
                    }
                    logd("vsim caused sim load, skip it.");
                    break;
                    break;
                case HwVSimUtilsInner.UE_OPERATION_MODE_VOICE_CENTRIC /*0*/:
                    if (!this.mNeedResetSub[slotId]) {
                        if (!HwVSimUtils.getIsWaitingSwitchCdmaModeSide()) {
                            if (!HwVSimUtils.prohibitSubUpdateSimNoChange(slotId)) {
                                int subState = subCtrlr.getSubState(subId[0]);
                                logd("slot[" + slotId + "], sim no change, subState should be " + subState);
                                if (subState == EVENT_SET_UICC_SUBSCRIPTION_DONE) {
                                    subCtrlr.activateSubId(subId[0]);
                                } else {
                                    subCtrlr.deactivateSubId(subId[0]);
                                }
                                setUiccSent = true;
                                break;
                            }
                            logd("slot[" + slotId + "], sim no change, but vsim prohibit, skip it");
                            setUiccSent = true;
                            break;
                        }
                        logd("slot[" + slotId + "], sim no change, but isWaitingSwitchCdmaModeSide, skip it");
                        break;
                    }
                    logd("slot[" + slotId + "], sim no change, but mNeedResetSub, skip it");
                    setUiccSent = true;
                    break;
                default:
                    loge(" slot [" + slotId + "], incorrect simStatus: " + simStatus[slotId]);
                    break;
            }
        }
        if (isAllSubsAvailable() && !setUiccSent) {
            logd("Received all sim info, update user pref subs, triggerDds= " + sTriggerDds);
            if (HwVSimUtils.isVSimInProcess() || HwVSimUtils.isVSimCauseCardReload()) {
                logd("vsim skip updateUserPreferences");
            } else {
                HwTelephonyFactory.getHwUiccManager().updateUserPreferences(sTriggerDds);
            }
            sTriggerDds = IS_SETUICCSUB_BY_SLOT;
        }
    }

    public void updateNwMode() {
        updateNwModesInSubIdTable(IS_SETUICCSUB_BY_SLOT);
        HwModemBindingPolicyHandler.getInstance().updatePrefNwTypeIfRequired();
        mNwModeUpdated = true;
    }

    public void setUiccSubscription(int slotId, int subStatus) {
        logd("setUiccSubscription: slotId:" + slotId + ", subStatus:" + subStatus);
        boolean set3GPPDone = IS_SETUICCSUB_BY_SLOT;
        boolean set3GPP2Done = IS_SETUICCSUB_BY_SLOT;
        UiccCard uiccCard = UiccController.getInstance().getUiccCard(slotId);
        Message msgSetUiccSubDone;
        if (!HwModemCapability.isCapabilitySupport(9)) {
            msgSetUiccSubDone = Message.obtain(this, EVENT_SET_UICC_SUBSCRIPTION_DONE, slotId, subStatus);
            if ((MultiSimVariants.DSDS == TelephonyManager.getDefault().getMultiSimConfiguration() ? true : IS_SETUICCSUB_BY_SLOT) || SystemProperties.getBoolean("ro.hwpp.set_uicc_by_radiopower", IS_SETUICCSUB_BY_SLOT)) {
                PhoneFactory.getPhone(slotId).setRadioPower(subStatus == 0 ? IS_SETUICCSUB_BY_SLOT : true, msgSetUiccSubDone);
                HwVSimUtils.updateSubState(slotId, subStatus == 0 ? 0 : EVENT_SET_UICC_SUBSCRIPTION_DONE);
            } else {
                this.mCi[slotId].setUiccSubscription(slotId, 0, slotId, subStatus, msgSetUiccSubDone);
            }
        } else if (uiccCard == null || uiccCard.getNumApplications() == 0) {
            logd("setUiccSubscription: slotId:" + slotId + " card info not available");
            PhoneFactory.getSubInfoRecordUpdater().resetIccid(slotId);
            msgSetUiccSubDone = Message.obtain(this, EVENT_SET_UICC_SUBSCRIPTION_DONE, slotId, subStatus);
            AsyncResult.forMessage(msgSetUiccSubDone, Boolean.valueOf(IS_SETUICCSUB_BY_SLOT), CommandException.fromRilErrno(EVENT_REFRESH));
            msgSetUiccSubDone.sendToTarget();
        } else if (!IS_SETUICCSUB_BY_SLOT) {
            for (int i = 0; i < uiccCard.getNumApplications(); i += EVENT_SET_UICC_SUBSCRIPTION_DONE) {
                int appType = uiccCard.getApplicationIndex(i).getType().ordinal();
                if (!set3GPPDone && (appType == EVENT_REFRESH || appType == EVENT_SET_UICC_SUBSCRIPTION_DONE)) {
                    this.mCi[slotId].setUiccSubscription(slotId, i, slotId, subStatus, Message.obtain(this, EVENT_SET_UICC_SUBSCRIPTION_DONE, slotId, subStatus));
                    set3GPPDone = true;
                } else if (!set3GPP2Done && (appType == EVENT_RADIO_ON || appType == 3)) {
                    this.mCi[slotId].setUiccSubscription(slotId, i, slotId, subStatus, Message.obtain(this, EVENT_SET_UICC_SUBSCRIPTION_DONE, slotId, subStatus));
                    set3GPP2Done = true;
                }
                if (set3GPPDone && set3GPP2Done) {
                    break;
                }
            }
        } else {
            this.mCi[slotId].setUiccSubscription(slotId, 0, slotId, subStatus, Message.obtain(this, EVENT_SET_UICC_SUBSCRIPTION_DONE, slotId, subStatus));
        }
    }

    private void processSetUiccSubscriptionDone(Message msg) {
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        AsyncResult ar = msg.obj;
        int slotId = msg.arg1;
        int newSubState = msg.arg2;
        int[] subId = subCtrlr.getSubIdUsingSlotId(slotId);
        if (ar.exception != null) {
            loge("Exception in SET_UICC_SUBSCRIPTION, slotId = " + slotId + " newSubState " + newSubState);
            this.mSubStatus[slotId] = SUB_SET_UICC_FAIL;
            if ((ar.exception instanceof CommandException) && ((CommandException) ar.exception).getCommandError() == Error.RADIO_NOT_AVAILABLE) {
                this.mNewSubState[slotId] = newSubState;
                this.mNeedResetSub[slotId] = true;
                this.mSubStatus[slotId] = SUB_INIT_STATE;
                logd("Store subinfo and set mNeedResetSub to true because of RADIO_NOT_AVAILABLE, mNeedResetSub[" + slotId + "]:" + this.mNeedResetSub[slotId]);
            }
            broadcastSetUiccResult(slotId, newSubState, EVENT_SET_UICC_SUBSCRIPTION_DONE);
            return;
        }
        if (newSubState != subCtrlr.getSubState(subId[0])) {
            subCtrlr.setSubState(subId[0], newSubState);
        }
        broadcastSetUiccResult(slotId, newSubState, 0);
        this.mSubStatus[slotId] = newSubState;
        if (isAllSubsAvailable()) {
            logd("Received all subs, now update user preferred subs, slotid = " + slotId + " newSubState = " + newSubState + " sTriggerDds = " + sTriggerDds);
            if (HwVSimUtils.isVSimInProcess() || HwVSimUtils.isVSimCauseCardReload()) {
                logd("vsim skip updateUserPreferences");
            } else {
                HwTelephonyFactory.getHwUiccManager().updateUserPreferences(sTriggerDds);
            }
            if (sTriggerDds && HwModemCapability.isCapabilitySupport(9)) {
                HwTelephonyFactory.getHwUiccManager().updateDataSlot();
            }
            sTriggerDds = IS_SETUICCSUB_BY_SLOT;
        }
        this.mNewSubState[slotId] = this.INVALID_VALUE;
        this.mNeedResetSub[slotId] = IS_SETUICCSUB_BY_SLOT;
    }

    private void processSimRefresh(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            loge("processSimRefresh received without input");
            return;
        }
        Integer index = ar.userObj;
        IccRefreshResponse state = ar.result;
        logi(" Received SIM refresh, reset sub state " + index + " old sub state " + this.mSubStatus[index.intValue()] + " refreshResult = " + state.refreshResult);
        if (state.refreshResult == EVENT_REFRESH) {
            this.mSubStatus[index.intValue()] = SUB_SIM_REFRESH;
        }
    }

    private void broadcastSetUiccResult(int slotId, int newSubState, int result) {
        int[] subId = SubscriptionController.getInstance().getSubIdUsingSlotId(slotId);
        Intent intent = new Intent("android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, slotId, subId[0]);
        intent.putExtra("operationResult", result);
        intent.putExtra("newSubState", newSubState);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean isAllSubsAvailable() {
        boolean allSubsAvailable = true;
        for (int i = 0; i < sNumPhones; i += EVENT_SET_UICC_SUBSCRIPTION_DONE) {
            if (this.mSubStatus[i] == SUB_INIT_STATE) {
                allSubsAvailable = IS_SETUICCSUB_BY_SLOT;
            }
        }
        return allSubsAvailable;
    }

    public boolean isRadioOn(int phoneId) {
        return this.mCi[phoneId].getRadioState().isOn();
    }

    public boolean isRadioAvailable(int phoneId) {
        return this.mCi[phoneId].getRadioState().isAvailable();
    }

    public boolean isApmSIMNotPwdn() {
        return sApmSIMNotPwdn;
    }

    public boolean proceedToHandleIccEvent(int slotId) {
        int apmState = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
        if (!sApmSIMNotPwdn && (!isRadioOn(slotId) || apmState == EVENT_SET_UICC_SUBSCRIPTION_DONE)) {
            logi(" proceedToHandleIccEvent, radio off/unavailable, slotId = " + slotId);
            this.mSubStatus[slotId] = SUB_INIT_STATE;
        }
        if (apmState == EVENT_SET_UICC_SUBSCRIPTION_DONE && !sApmSIMNotPwdn) {
            logd(" proceedToHandleIccEvent, sApmSIMNotPwdn = " + sApmSIMNotPwdn);
            return IS_SETUICCSUB_BY_SLOT;
        } else if (isRadioAvailable(slotId)) {
            return true;
        } else {
            logi(" proceedToHandleIccEvent, radio not available, slotId = " + slotId);
            if (!HwVSimUtils.isPlatformTwoModems() || HwVSimUtils.isRadioAvailable(slotId)) {
                return IS_SETUICCSUB_BY_SLOT;
            }
            logi("proceedToHandleIccEvent, vsim pending sub");
            return true;
        }
    }

    private static void logd(String message) {
        Rlog.d(LOG_TAG, message);
    }

    private void logi(String msg) {
        Rlog.i(LOG_TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
