package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.vsim.HwVSimUtils;

class SubscriptionHelper extends Handler {
    private static final String APM_SIM_NOT_PWDN_PROPERTY = "persist.radio.apm_sim_not_pwdn";
    public static final byte[] C1 = {98, 94, -52, 117, -82, 28, -44, 66, 28, 61, -110, -119, -75, 70, 2, 85};
    private static final int EVENT_RADIO_AVAILABLE = 5;
    private static final int EVENT_RADIO_ON = 4;
    private static final int EVENT_REFRESH = 2;
    private static final int EVENT_SET_UICC_SUBSCRIPTION_DONE = 1;
    private static final boolean IS_SETUICCSUB_BY_SLOT = SystemProperties.getBoolean("ro.config.setuiccsub_by_slot", false);
    private static final String LOG_TAG = "SubHelper";
    private static final int SUB_1 = 1;
    public static final int SUB_INIT_STATE = -1;
    public static final int SUB_SET_UICC_FAIL = -100;
    public static final int SUB_SIM_NOT_INSERTED = -99;
    private static final int SUB_SIM_REFRESH = -101;
    /* access modifiers changed from: private */
    public static boolean mNwModeUpdated = false;
    protected static boolean[] mSubSwitching;
    private static final boolean sApmSIMNotPwdn;
    private static SubscriptionHelper sInstance;
    private static int sNumPhones;
    private static boolean sTriggerDds = false;
    private int INVALID_VALUE = -1;
    private CommandsInterface[] mCi;
    private Context mContext;
    private boolean[] mNeedResetSub;
    private int[] mNewSubState;
    private int[] mSubStatus;
    private final ContentObserver nwModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfUpdate) {
            SubscriptionHelper.logd("NwMode Observer onChange hit !!!");
            if (SubscriptionHelper.mNwModeUpdated) {
                SubscriptionHelper.this.updateNwModesInSubIdTable(true);
            }
        }
    };

    static {
        boolean z = true;
        if (SystemProperties.getInt(APM_SIM_NOT_PWDN_PROPERTY, 0) != 1) {
            z = false;
        }
        sApmSIMNotPwdn = z;
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
        this.mContext = c;
        this.mCi = ci;
        sNumPhones = TelephonyManager.getDefault().getPhoneCount();
        this.mSubStatus = new int[sNumPhones];
        this.mNewSubState = new int[sNumPhones];
        this.mNeedResetSub = new boolean[sNumPhones];
        mSubSwitching = new boolean[sNumPhones];
        for (int i = 0; i < sNumPhones; i++) {
            this.mSubStatus[i] = -1;
            mSubSwitching[i] = false;
            this.mNewSubState[i] = this.INVALID_VALUE;
            this.mNeedResetSub[i] = false;
            Integer index = Integer.valueOf(i);
            this.mCi[i].registerForIccRefresh(this, 2, index);
            this.mCi[i].registerForOn(this, 4, index);
            this.mCi[i].registerForAvailable(this, 5, index);
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("preferred_network_mode" + i), false, this.nwModeObserver);
        }
        logd("SubscriptionHelper init by Context, num phones = " + sNumPhones + " ApmSIMNotPwdn = " + sApmSIMNotPwdn);
    }

    /* access modifiers changed from: private */
    public void updateNwModesInSubIdTable(boolean override) {
        int nwModeinSubIdTable;
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        for (int i = 0; i < sNumPhones; i++) {
            int[] subIdList = subCtrlr.getSubId(i);
            if (subIdList != null && subIdList[0] >= 0) {
                int nwModeInDb = HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, i);
                logd("updateNwModesInSubIdTable: nwModeinSubIdTable: " + nwModeinSubIdTable + ", nwModeInDb: " + nwModeInDb);
                if (override || nwModeinSubIdTable == -1) {
                    subCtrlr.setNwMode(subIdList[0], nwModeInDb);
                }
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                logd("EVENT_SET_UICC_SUBSCRIPTION_DONE");
                processSetUiccSubscriptionDone(msg);
                return;
            case 2:
                logd("EVENT_REFRESH");
                processSimRefresh((AsyncResult) msg.obj);
                return;
            case 4:
            case 5:
                Integer Index = (Integer) ((AsyncResult) msg.obj).userObj;
                logd("[EVENT_RADIO_ON or EVENT_RADIO_AVAILABLE]: Index" + Index);
                if (Index.intValue() != this.INVALID_VALUE && this.mNewSubState[Index.intValue()] != this.INVALID_VALUE && true == this.mNeedResetSub[Index.intValue()]) {
                    logd("[EVENT_RADIO_ON or EVENT_RADIO_AVAILABLE]: Need to reset UICC Subscription,Index = " + Index + ";mNewSubState = " + this.mNewSubState[Index.intValue()]);
                    setUiccSubscription(Index.intValue(), this.mNewSubState[Index.intValue()]);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public boolean needSubActivationAfterRefresh(int slotId) {
        return sNumPhones > 1 && this.mSubStatus[slotId] == SUB_SIM_REFRESH;
    }

    public void updateSubActivation(int[] simStatus, boolean isStackReadyEvent) {
        int subState;
        boolean isPrimarySubFeatureEnable = SystemProperties.getBoolean("persist.radio.primarycard", false);
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        boolean isVSimSkipUpdateUserPref = true;
        if (isStackReadyEvent && !isPrimarySubFeatureEnable) {
            sTriggerDds = true;
        }
        boolean setUiccSent = false;
        int slotId = 0;
        while (slotId < sNumPhones) {
            logd("slot[" + slotId + "] simStatus = " + simStatus[slotId]);
            int[] subId = subCtrlr.getSubId(slotId);
            int i = simStatus[slotId];
            if (i != -99) {
                switch (i) {
                    case HwPhoneService.ERROR_INVALID_PARAM:
                    case -2:
                        if (HwVSimUtils.isVSimInProcess() != 0) {
                            break;
                        }
                        logd("vsim caused sim load, skip it.");
                        break;
                    default:
                        switch (i) {
                            case 0:
                                if (!this.mNeedResetSub[slotId]) {
                                    if (!HwVSimUtils.getIsWaitingSwitchCdmaModeSide()) {
                                        if (!HwVSimUtils.getIsWaitingNvMatchUnsol()) {
                                            if (!HwVSimUtils.prohibitSubUpdateSimNoChange(slotId)) {
                                                logd("slot[" + slotId + "], sim no change, subState should be " + subState);
                                                if (subState == 1) {
                                                    subCtrlr.activateSubId(subId[0]);
                                                } else {
                                                    subCtrlr.deactivateSubId(subId[0]);
                                                }
                                                setUiccSent = true;
                                                break;
                                            } else {
                                                logd("slot[" + slotId + "], sim no change, but vsim prohibit, skip it");
                                                setUiccSent = true;
                                                break;
                                            }
                                        } else {
                                            logd("slot[" + slotId + "], sim no change, but isWaitingNvMatchUnsol, skip it");
                                            setUiccSent = true;
                                            break;
                                        }
                                    } else {
                                        logd("slot[" + slotId + "], sim no change, but isWaitingSwitchCdmaModeSide, skip it");
                                        break;
                                    }
                                } else {
                                    logd("slot[" + slotId + "], sim no change, but mNeedResetSub, skip it");
                                    setUiccSent = true;
                                    continue;
                                }
                            case 1:
                            case 2:
                                break;
                            default:
                                loge(" slot [" + slotId + "], incorrect simStatus: " + simStatus[slotId]);
                                continue;
                        }
                        if (HwVSimUtils.isVSimInProcess() != 0 && !HwVSimUtils.isVSimCauseCardReload()) {
                            logd("slot[" + slotId + "] sim has changed, should activate it.");
                            if (!HwTelephonyManagerInner.getDefault().isDataConnectivityDisabled(slotId, "disable-sub")) {
                                subCtrlr.activateSubId(subId[0]);
                                HwVSimUtils.setSubActived(subId[0]);
                                setUiccSent = true;
                                this.mNewSubState[slotId] = 1;
                                break;
                            } else {
                                setUiccSubscription(1, 0);
                                return;
                            }
                        } else {
                            logd("vsim caused sim load, skip it.");
                            break;
                        }
                }
            } else {
                this.mSubStatus[slotId] = simStatus[slotId];
                logd("slot[" + slotId + "] sim is not insert.");
            }
            slotId++;
        }
        if (isAllSubsAvailable() != 0 && !setUiccSent) {
            logd("Received all sim info, update user pref subs, triggerDds= " + sTriggerDds);
            if ((!HwVSimUtils.isVSimDsdsVersionOne() || !HwVSimUtils.isVSimEnabled()) && !HwVSimUtils.isVSimInProcess() && !HwVSimUtils.isVSimCauseCardReload()) {
                isVSimSkipUpdateUserPref = false;
            }
            if (isVSimSkipUpdateUserPref) {
                logd("vsim skip updateUserPreferences");
            } else {
                HwTelephonyFactory.getHwUiccManager().updateUserPreferences(sTriggerDds);
            }
            sTriggerDds = false;
        }
    }

    public void updateNwMode() {
        updateNwModesInSubIdTable(false);
        mNwModeUpdated = true;
    }

    public void setUiccSubscription(int slotId, int subStatus) {
        int appType;
        int i = slotId;
        int i2 = subStatus;
        logd("setUiccSubscription: slotId:" + i + ", subStatus:" + i2);
        boolean set3GPPDone = false;
        boolean set3GPP2Done = false;
        UiccCard uiccCard = UiccController.getInstance().getUiccCard(i);
        int i3 = 0;
        int i4 = 1;
        if (HuaweiTelephonyConfigs.isQcomPlatform()) {
            int i5 = 2;
            if (uiccCard == null || uiccCard.getNumApplications() == 0) {
                logd("setUiccSubscription: slotId:" + i + " card info not available");
                PhoneFactory.getSubInfoRecordUpdater().resetIccid(i);
                Message msgSetUiccSubDone = Message.obtain(this, 1, i, i2);
                AsyncResult.forMessage(msgSetUiccSubDone, false, CommandException.fromRilErrno(2));
                msgSetUiccSubDone.sendToTarget();
            } else if (!IS_SETUICCSUB_BY_SLOT) {
                int numApplication = uiccCard.getNumApplications();
                while (true) {
                    int i6 = i3;
                    if (i6 >= numApplication) {
                        break;
                    }
                    int appType2 = uiccCard.getApplicationIndex(i6).getType().ordinal();
                    if (set3GPPDone) {
                        appType = appType2;
                    } else if (appType2 == i5 || appType2 == 1) {
                        int i7 = appType2;
                        this.mCi[i].setUiccSubscription(i, i6, i, i2, Message.obtain(this, 1, i, i2));
                        set3GPPDone = true;
                        if (!set3GPPDone && set3GPP2Done) {
                            break;
                        }
                        i3 = i6 + 1;
                        i5 = 2;
                    } else {
                        appType = appType2;
                    }
                    if (!set3GPP2Done && (appType == 4 || appType == 3)) {
                        this.mCi[i].setUiccSubscription(i, i6, i, i2, Message.obtain(this, 1, i, i2));
                        set3GPP2Done = true;
                    }
                    if (!set3GPPDone) {
                    }
                    i3 = i6 + 1;
                    i5 = 2;
                }
            } else {
                this.mCi[i].setUiccSubscription(i, 0, i, i2, Message.obtain(this, 1, i, i2));
            }
        } else {
            Message msgSetUiccSubDone2 = Message.obtain(this, 1, i, i2);
            if ((TelephonyManager.MultiSimVariants.DSDS == TelephonyManager.getDefault().getMultiSimConfiguration()) || SystemProperties.getBoolean("ro.hwpp.set_uicc_by_radiopower", false)) {
                if (HuaweiTelephonyConfigs.isMTKPlatform()) {
                    mSubSwitching[i] = true;
                }
                this.mNewSubState[i] = this.INVALID_VALUE;
                this.mNeedResetSub[i] = false;
                PhoneFactory.getPhone(slotId).setRadioPower(i2 != 0, msgSetUiccSubDone2);
                if (i2 == 0) {
                    i4 = 0;
                }
                HwVSimUtils.updateSubState(i, i4);
            } else {
                this.mCi[i].setUiccSubscription(i, 0, i, i2, msgSetUiccSubDone2);
            }
        }
    }

    private void processSetUiccSubscriptionDone(Message msg) {
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        AsyncResult ar = (AsyncResult) msg.obj;
        int slotId = msg.arg1;
        int newSubState = msg.arg2;
        int[] subId = subCtrlr.getSubId(slotId);
        mSubSwitching[slotId] = false;
        boolean isVSimSkipUpdateUserPref = true;
        if (ar.exception != null) {
            loge("Exception in SET_UICC_SUBSCRIPTION, slotId = " + slotId + " newSubState " + newSubState);
            this.mSubStatus[slotId] = -100;
            if ((ar.exception instanceof CommandException) && ar.exception.getCommandError() == CommandException.Error.RADIO_NOT_AVAILABLE) {
                this.mNewSubState[slotId] = newSubState;
                this.mNeedResetSub[slotId] = true;
                this.mSubStatus[slotId] = -1;
                logd("Store subinfo and set mNeedResetSub to true because of RADIO_NOT_AVAILABLE, mNeedResetSub[" + slotId + "]:" + this.mNeedResetSub[slotId]);
            }
            broadcastSetUiccResult(slotId, newSubState, 1);
            return;
        }
        if (newSubState != subCtrlr.getSubState(subId[0])) {
            subCtrlr.setSubState(subId[0], newSubState);
        }
        broadcastSetUiccResult(slotId, newSubState, 0);
        this.mSubStatus[slotId] = newSubState;
        if (isAllSubsAvailable()) {
            logd("Received all subs, now update user preferred subs, slotid = " + slotId + " newSubState = " + newSubState + " sTriggerDds = " + sTriggerDds);
            if ((!HwVSimUtils.isVSimDsdsVersionOne() || !HwVSimUtils.isVSimEnabled()) && !HwVSimUtils.isVSimInProcess() && !HwVSimUtils.isVSimCauseCardReload()) {
                isVSimSkipUpdateUserPref = false;
            }
            if (isVSimSkipUpdateUserPref) {
                logd("vsim skip updateUserPreferences");
            } else {
                HwTelephonyFactory.getHwUiccManager().updateUserPreferences(sTriggerDds);
            }
            if (sTriggerDds && !HuaweiTelephonyConfigs.isHisiPlatform()) {
                HwTelephonyFactory.getHwUiccManager().updateDataSlot();
            }
            sTriggerDds = false;
        }
        this.mNewSubState[slotId] = this.INVALID_VALUE;
        this.mNeedResetSub[slotId] = false;
    }

    private void processSimRefresh(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            loge("processSimRefresh received without input");
            return;
        }
        Integer index = (Integer) ar.userObj;
        IccRefreshResponse state = (IccRefreshResponse) ar.result;
        logi(" Received SIM refresh, reset sub state " + index + " old sub state " + this.mSubStatus[index.intValue()] + " refreshResult = " + state.refreshResult);
        if (state.refreshResult == 2) {
            this.mSubStatus[index.intValue()] = SUB_SIM_REFRESH;
        }
    }

    private void broadcastSetUiccResult(int slotId, int newSubState, int result) {
        int[] subId = SubscriptionController.getInstance().getSubId(slotId);
        Intent intent = new Intent("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        intent.addFlags(16777216);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, slotId, subId[0]);
        intent.putExtra("operationResult", result);
        intent.putExtra("newSubState", newSubState);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean isAllSubsAvailable() {
        boolean allSubsAvailable = true;
        for (int i = 0; i < sNumPhones; i++) {
            if (this.mSubStatus[i] == -1) {
                allSubsAvailable = false;
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
        int apmState = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
        if (!sApmSIMNotPwdn && (!isRadioOn(slotId) || apmState == 1)) {
            logi(" proceedToHandleIccEvent, radio off/unavailable, slotId = " + slotId);
            this.mSubStatus[slotId] = -1;
        }
        if (apmState == 1 && !sApmSIMNotPwdn) {
            logd(" proceedToHandleIccEvent, sApmSIMNotPwdn = " + sApmSIMNotPwdn);
            return false;
        } else if (isRadioAvailable(slotId)) {
            return true;
        } else {
            logi(" proceedToHandleIccEvent, radio not available, slotId = " + slotId);
            if (!HwVSimUtils.isPlatformTwoModems() || HwVSimUtils.isRadioAvailable(slotId)) {
                return false;
            }
            logi("proceedToHandleIccEvent, vsim pending sub");
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static void logd(String message) {
        Rlog.d(LOG_TAG, message);
    }

    private void logi(String msg) {
        Rlog.i(LOG_TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
