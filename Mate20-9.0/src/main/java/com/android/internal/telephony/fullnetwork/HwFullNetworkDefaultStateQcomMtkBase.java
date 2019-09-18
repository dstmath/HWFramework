package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.android.internal.telephony.PhoneFactory;

public class HwFullNetworkDefaultStateQcomMtkBase extends HwFullNetworkDefaultStateBase {
    private static final String LOG_TAG = "HwFullNetworkDefaultStateQcomMtkBase";
    private static final int OOS_DELAY_TIME = 20000;
    protected HwFullNetworkChipOther mChipOther = HwFullNetworkChipOther.getInstance();

    public HwFullNetworkDefaultStateQcomMtkBase(Context c, CommandsInterface[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkDefaultStateQcomMtkBase constructor");
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v12, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: android.os.AsyncResult} */
    /* JADX WARNING: Multi-variable type inference failed */
    public void handleMessage(Message msg) {
        if (msg == null || msg.obj == null) {
            loge("msg or msg.obj is null, return!");
            return;
        }
        AsyncResult ar = null;
        if (msg.obj instanceof AsyncResult) {
            ar = msg.obj;
        }
        switch (msg.what) {
            case HwFullNetworkConstants.EVENT_SET_LTE_SERVICE_ABILITY:
                setNetworkType(ar, msg.arg1);
                break;
            case HwFullNetworkConstants.EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE:
                logd("Received EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE");
                handleSetPrimaryStackLteSwitchDone(ar, msg.arg1);
                break;
            case HwFullNetworkConstants.EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE:
                logd("Received EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE");
                handleSetSecondaryStackLteSwitchDone(ar, msg.arg1);
                break;
            case HwFullNetworkConstants.EVENT_SET_PRIMARY_STACK_ROLL_BACK_DONE:
                logd("Received EVENT_SET_PRIMARY_STACK_ROLL_BACK_DONE");
                handleRollbackDone(ar);
                break;
            case HwFullNetworkConstants.EVENT_RESET_OOS_FLAG:
                int index = this.mChipCommon.getCiIndex(msg).intValue();
                logd("Received EVENT_RESET_OOS_FLAG on index " + index);
                PhoneFactory.getPhone(index).setOOSFlagOnSelectNetworkManually(false);
                break;
            default:
                super.handleMessage(msg);
                break;
        }
    }

    private void handleGetPrefNetworkModeDone(Message msg) {
        int subId = msg.arg1;
        int modemNetworkMode = -1;
        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar.exception == null) {
            modemNetworkMode = ((int[]) ar.result)[0];
        }
        logd("subId = " + subId + " modemNetworkMode = " + modemNetworkMode);
        if (this.mChipOther.mPrimaryStackPhoneId == subId) {
            Message response = obtainMessage(HwFullNetworkConstants.EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE, this.mChipOther.mPrimaryStackNetworkType, 0, Integer.valueOf(this.mChipOther.mPrimaryStackPhoneId));
            if (modemNetworkMode == -1 || modemNetworkMode != this.mChipOther.mPrimaryStackNetworkType) {
                this.mCis[this.mChipOther.mPrimaryStackPhoneId].setPreferredNetworkType(this.mChipOther.mPrimaryStackNetworkType, response);
                return;
            }
            AsyncResult.forMessage(response);
            response.sendToTarget();
            logd("The sub" + subId + " pref network mode is same with modem's ,don't set again");
        } else if (this.mChipOther.mSecondaryStackPhoneId == subId) {
            Message response2 = obtainMessage(HwFullNetworkConstants.EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE, this.mChipOther.mSecondaryStackNetworkType, 0, Integer.valueOf(this.mChipOther.mSecondaryStackPhoneId));
            if (modemNetworkMode == -1 || !(modemNetworkMode == this.mChipOther.mSecondaryStackNetworkType || -1 == this.mChipOther.mSecondaryStackNetworkType)) {
                this.mCis[this.mChipOther.mSecondaryStackPhoneId].setPreferredNetworkType(this.mChipOther.mSecondaryStackNetworkType, response2);
                return;
            }
            AsyncResult.forMessage(response2);
            response2.sendToTarget();
            logd("The sub" + subId + " pref network mode is same with modem's ,don't set again");
        }
    }

    /* access modifiers changed from: protected */
    public void onRadioUnavailable(Integer index) {
        this.mChipCommon.mIccIds[index.intValue()] = null;
    }

    /* access modifiers changed from: protected */
    public void onRadioAvailable(Integer index) {
        logd("onRadioAvailable, index " + index);
        if (HwFullNetworkConstants.SIM_NUM == 2) {
            syncNetworkTypeFromDB(index.intValue());
        }
        super.onRadioAvailable(index);
    }

    /* access modifiers changed from: protected */
    public void processSubSetUiccResult(Intent intent) {
        int slotId = intent.getIntExtra("subscription", -1);
        int subState = intent.getIntExtra("newSubState", -1);
        int result = intent.getIntExtra("operationResult", -1);
        logd("received ACTION_SUBSCRIPTION_SET_UICC_RESULT,slotId:" + slotId + " subState:" + subState + " result:" + result);
        if (slotId >= 0 && HwFullNetworkConstants.SIM_NUM > slotId) {
            if (1 == result) {
                if (1 == subState) {
                    this.mChipOther.mSetUiccSubscriptionResult[slotId] = subState;
                } else {
                    this.mChipOther.mSetUiccSubscriptionResult[slotId] = -1;
                }
            } else if (result == 0) {
                this.mChipOther.mSetUiccSubscriptionResult[slotId] = -1;
            }
        }
    }

    private void syncNetworkTypeFromDB(int subId) {
        if (this.mChipCommon.isValidIndex(subId)) {
            int pefMode = this.mChipOther.getNetworkTypeFromDB(subId);
            boolean z = true;
            if (!(Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) == 0 && Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 1) == 0)) {
                z = false;
            }
            boolean firstStart = z;
            logd("syncNetworkTypeFromDB, sub = " + subId + ", pefMode = " + pefMode + ", firstStart =" + firstStart);
            if (pefMode == -1 || firstStart) {
                setLteServiceAbilityForQCOM(subId, getPrimaryAndSecondaryStackAbility(subId), SystemProperties.getInt("ro.telephony.default_network", -1));
            }
        }
    }

    private int getPrimaryAndSecondaryStackAbility(int subId) {
        if (!this.mChipCommon.isDualImsSwitchOpened() && subId != this.mChipCommon.getUserSwitchDualCardSlots()) {
            return 0;
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public void processPreBootCompleted() {
    }

    /* access modifiers changed from: protected */
    public void setMainSlot(int slotId, Message responseMsg) {
        if (!SystemProperties.getBoolean("persist.sys.dualcards", false)) {
            loge("setMainSlot: main slot switch disabled, return failure");
            this.mChipCommon.sendResponseToTarget(responseMsg, 2);
        } else if (!this.mChipCommon.isValidIndex(slotId)) {
            loge("setDefault4GSlot: invalid slotid, return failure");
            this.mChipCommon.sendResponseToTarget(responseMsg, 2);
        } else {
            this.mChipOther.is4GSlotReviewNeeded = 2;
            this.mChipOther.mUserPref4GSlot = slotId;
            this.mChipCommon.prefer4GSlot = slotId;
            if (slotId == this.mChipCommon.getUserSwitchDualCardSlots()) {
                loge("setDefault4GSlot: the default 4G slot is already " + slotId);
                this.mChipCommon.sendResponseToTarget(responseMsg, 0);
            } else if (this.mChipCommon.isSet4GSlotInProgress) {
                loge("setDefault4GSlot: The setting is in progress, return failure");
                this.mChipCommon.sendResponseToTarget(responseMsg, 2);
            } else {
                logd("setDefault4GSlot: target slot id is: " + slotId);
                this.mChipCommon.mSet4GSlotCompleteMsg = responseMsg;
                this.mChipOther.refreshCardState();
                this.mChipCommon.judgeSubCarrierType();
                this.mChipOther.judgeNwMode(slotId);
                this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_SET_MAIN_SLOT, slotId, 0, responseMsg).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setLteServiceAbilityForQCOM(int subId, int ability, int lteOnMappingMode) {
        getStackPhoneId();
        if (this.mChipCommon.isDualImsSwitchOpened() || subId != this.mChipCommon.getUserSwitchDualCardSlots()) {
            logd("setLteServiceAbilityForQCOM, dual Ims new, sub =" + subId + ", ability =" + ability);
            if (this.mChipOther.mPrimaryStackPhoneId == subId) {
                this.mChipOther.mPrimaryStackNetworkType = getPrimaryStackNetworkType(ability, lteOnMappingMode);
            } else {
                this.mChipOther.mSecondaryStackNetworkType = getSecondaryStackNetworkType(ability, lteOnMappingMode);
            }
            recordPrimaryAndSecondaryStackNetworkTypeForCmcc(ability);
            this.mCis[subId].getPreferredNetworkType(obtainMessage(HwFullNetworkConstants.EVENT_SET_LTE_SERVICE_ABILITY, subId, 0));
            return;
        }
        logd("in setLteServiceAbilityForQCOM, single Ims.");
        recordPrimaryAndSecondaryStackNetworkType(ability, lteOnMappingMode);
        this.mCis[this.mChipOther.mPrimaryStackPhoneId].getPreferredNetworkType(obtainMessage(HwFullNetworkConstants.EVENT_SET_LTE_SERVICE_ABILITY, this.mChipOther.mPrimaryStackPhoneId, 0));
    }

    private void getStackPhoneId() {
        int i = 0;
        this.mChipOther.mPrimaryStackPhoneId = SystemProperties.getInt("persist.radio.msim.stackid_0", 0);
        HwFullNetworkChipOther hwFullNetworkChipOther = this.mChipOther;
        if (this.mChipOther.mPrimaryStackPhoneId == 0) {
            i = 1;
        }
        hwFullNetworkChipOther.mSecondaryStackPhoneId = i;
        logd("getStackPhoneId mPrimaryStackPhoneId:" + this.mChipOther.mPrimaryStackPhoneId + ", mSecondaryStackPhoneId:" + this.mChipOther.mSecondaryStackPhoneId);
    }

    private int getPrimaryStackNetworkType(int ability, int lteOnMappingMode) {
        int primaryLteOnNetworkType = lteOnMappingMode;
        int primaryLteOffNetworkType = HwNetworkTypeUtils.getOffModeFromMapping(lteOnMappingMode);
        if (-1 == lteOnMappingMode) {
            primaryLteOnNetworkType = 22;
            primaryLteOffNetworkType = 21;
        }
        return ability == 1 ? primaryLteOnNetworkType : primaryLteOffNetworkType;
    }

    private int getSecondaryStackNetworkType(int ability, int lteOnMappingMode) {
        int i;
        if (ability == 1) {
            int secondaryNetworkType = HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK ? lteOnMappingMode : 20;
            if (lteOnMappingMode == 9 || lteOnMappingMode == 10) {
                return 9;
            }
            return secondaryNetworkType;
        }
        if (HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK) {
            i = HwNetworkTypeUtils.getOffModeFromMapping(lteOnMappingMode);
        } else {
            i = 18;
        }
        int secondaryNetworkType2 = i;
        if (lteOnMappingMode == 9 || lteOnMappingMode == 10) {
            return 3;
        }
        return secondaryNetworkType2;
    }

    private void recordPrimaryAndSecondaryStackNetworkType(int ability, int lteOnMappingMode) {
        this.mChipOther.mPrimaryStackNetworkType = getPrimaryStackNetworkType(ability, lteOnMappingMode);
        this.mChipOther.mSecondaryStackNetworkType = getSecondaryStackNetworkType(ability, lteOnMappingMode);
        int i = 0;
        if (HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK) {
            if (this.mChipOther.mPrimaryStackPhoneId == this.mChipCommon.getUserSwitchDualCardSlots()) {
                this.mChipOther.mSecondaryStackNetworkType = getSecondaryStackNetworkType(0, lteOnMappingMode);
                boolean primaryCardHide = getCarrierConfig(this.mContext, HwFullNetworkConstants.NET_WORK_MODE_HIDE, this.mChipOther.mPrimaryStackPhoneId).booleanValue();
                if (HwFullNetworkConfig.IS_NET_WORK_MODE_HIDE && primaryCardHide) {
                    this.mChipOther.mPrimaryStackNetworkType = getPrimaryStackNetworkType(1, lteOnMappingMode);
                }
            } else {
                this.mChipOther.mPrimaryStackNetworkType = getPrimaryStackNetworkType(0, lteOnMappingMode);
                boolean secondaryCardHide = getCarrierConfig(this.mContext, HwFullNetworkConstants.NET_WORK_MODE_HIDE, this.mChipOther.mSecondaryStackPhoneId).booleanValue();
                if (HwFullNetworkConfig.IS_NET_WORK_MODE_HIDE && secondaryCardHide) {
                    this.mChipOther.mSecondaryStackNetworkType = getSecondaryStackNetworkType(1, lteOnMappingMode);
                }
            }
        }
        if (this.mChipCommon.isCmccHybirdAndNoRoaming()) {
            recordNetworkTypeForCmccHybird(ability);
            if (this.mChipCommon.default4GSlot == 0) {
                i = 1;
            }
            int otherSub = i;
            PhoneFactory.getPhone(otherSub).setOOSFlagOnSelectNetworkManually(true);
            if (hasMessages(HwFullNetworkConstants.EVENT_RESET_OOS_FLAG)) {
                removeMessages(HwFullNetworkConstants.EVENT_RESET_OOS_FLAG);
            }
            Message msg = obtainMessage(HwFullNetworkConstants.EVENT_RESET_OOS_FLAG, Integer.valueOf(otherSub));
            AsyncResult.forMessage(msg, null, null);
            sendMessageDelayed(msg, 20000);
        }
        logd("recordPrimaryAndSecondaryStackNetworkType mPrimaryStackNetworkType:" + this.mChipOther.mPrimaryStackNetworkType + ",mSecondaryStackNetworkType:" + this.mChipOther.mSecondaryStackNetworkType);
    }

    private void recordPrimaryAndSecondaryStackNetworkTypeForCmcc(int ability) {
        if (HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE && this.mChipCommon.isDualImsSwitchOpened() && this.mChipOther.mNeedSetSecondStack) {
            int otherNetworkMode = 22;
            int mainSlot = this.mChipCommon.getUserSwitchDualCardSlots();
            int otherSlot = this.mChipCommon.getUserSwitchDualCardSlots() == 0 ? 1 : 0;
            int mainNetworkMode = (ability == 1 || isCmccCardBySubCarrierType(mainSlot)) ? 22 : 21;
            if (this.mChipCommon.isCmccHybirdAndNoRoaming() && !isCmccCardBySubCarrierType(otherSlot)) {
                otherNetworkMode = 21;
            }
            if (this.mChipOther.mPrimaryStackPhoneId == mainSlot) {
                this.mChipOther.mPrimaryStackNetworkType = mainNetworkMode;
                this.mChipOther.mSecondaryStackNetworkType = otherNetworkMode;
            } else if (this.mChipOther.mSecondaryStackPhoneId == mainSlot) {
                this.mChipOther.mSecondaryStackNetworkType = mainNetworkMode;
                this.mChipOther.mPrimaryStackNetworkType = otherNetworkMode;
            }
            logd("recordPrimaryAndSecondaryStackNetworkTypeForCmcc: mPrimaryStackNetworkType = " + this.mChipOther.mPrimaryStackNetworkType + "   mSecondaryStackNetworkType=" + this.mChipOther.mSecondaryStackNetworkType);
        }
    }

    private boolean isCmccCardBySubCarrierType(int i) {
        return this.mChipCommon.subCarrierTypeArray[i].isCMCCCard();
    }

    private void recordNetworkTypeForCmccHybird(int ability) {
        int otherNetworkType = ability == 1 ? 22 : 21;
        if (HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK) {
            otherNetworkType = 21;
        }
        this.mChipCommon.default4GSlot = this.mChipCommon.getUserSwitchDualCardSlots();
        if (this.mChipOther.mPrimaryStackPhoneId == this.mChipCommon.default4GSlot) {
            this.mChipOther.mSecondaryStackNetworkType = otherNetworkType;
        } else {
            this.mChipOther.mPrimaryStackNetworkType = otherNetworkType;
        }
    }

    private static Boolean getCarrierConfig(Context context, String key, int subId) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        boolean carrier = false;
        if (b != null) {
            carrier = b.getBoolean(key, false);
        }
        return Boolean.valueOf(carrier);
    }

    private void setNetworkType(AsyncResult ar, int subId) {
        int modemNetworkMode = -1;
        if (ar != null && ar.exception == null) {
            modemNetworkMode = ((int[]) ar.result)[0];
        }
        logd("subId = " + subId + " modemNetworkMode = " + modemNetworkMode);
        if (this.mChipOther.mPrimaryStackPhoneId == subId) {
            Message response = obtainMessage(HwFullNetworkConstants.EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE, this.mChipOther.mPrimaryStackNetworkType, 0, Integer.valueOf(this.mChipOther.mPrimaryStackPhoneId));
            if (modemNetworkMode == -1 || modemNetworkMode != this.mChipOther.mPrimaryStackNetworkType) {
                this.mCis[this.mChipOther.mPrimaryStackPhoneId].setPreferredNetworkType(this.mChipOther.mPrimaryStackNetworkType, response);
                return;
            }
            AsyncResult.forMessage(response);
            response.sendToTarget();
            logd("The sub" + subId + " pref network mode is same with modem's, don't set again");
        } else if (this.mChipOther.mSecondaryStackPhoneId == subId) {
            Message response2 = obtainMessage(HwFullNetworkConstants.EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE, this.mChipOther.mSecondaryStackNetworkType, 0, Integer.valueOf(this.mChipOther.mSecondaryStackPhoneId));
            if (modemNetworkMode == -1 || !(modemNetworkMode == this.mChipOther.mSecondaryStackNetworkType || -1 == this.mChipOther.mSecondaryStackNetworkType)) {
                this.mCis[this.mChipOther.mSecondaryStackPhoneId].setPreferredNetworkType(this.mChipOther.mSecondaryStackNetworkType, response2);
                return;
            }
            AsyncResult.forMessage(response2);
            response2.sendToTarget();
            logd("The sub" + subId + " pref network mode is same with modem's, don't set again");
        }
    }

    private void handleSetPrimaryStackLteSwitchDone(AsyncResult ar, int networkType) {
        logd("in handleSetPrimaryStackLteSwitchDone");
        if (ar == null || ar.exception != null) {
            loge("set prefer network mode failed!");
            if (!HwFullNetworkConfig.IS_DUAL_4G_SUPPORTED) {
                sendLteServiceSwitchResult(this.mChipCommon.getUserSwitchDualCardSlots(), false);
            } else {
                sendLteServiceSwitchResult(this.mChipOther.mPrimaryStackPhoneId, false);
            }
            return;
        }
        this.mChipOther.mSetPrimaryStackPrefMode = networkType;
        logd("setPrimaryStackPrefMode = " + this.mChipOther.mSetPrimaryStackPrefMode);
        if (needSetSecondStack()) {
            this.mCis[this.mChipOther.mSecondaryStackPhoneId].getPreferredNetworkType(obtainMessage(HwFullNetworkConstants.EVENT_SET_LTE_SERVICE_ABILITY, this.mChipOther.mSecondaryStackPhoneId, 0));
        } else {
            saveNetworkTypeToDB(this.mChipOther.mPrimaryStackPhoneId);
            logd("set prefer network mode success!");
            sendLteServiceSwitchResult(this.mChipOther.mPrimaryStackPhoneId, true);
        }
    }

    private void sendLteServiceSwitchResult(int subId, boolean result) {
        logd("LTE service Switch result is " + result + ". broadcast PREFERRED_4G_SWITCH_DONE");
        if (this.mContext == null) {
            loge("Context is null, return!");
            return;
        }
        Intent intent = new Intent("com.huawei.telephony.PREF_4G_SWITCH_DONE");
        intent.putExtra("subscription", subId);
        intent.putExtra("setting_result", result);
        this.mContext.sendOrderedBroadcast(intent, "android.permission.READ_PHONE_STATE");
    }

    private void handleSetSecondaryStackLteSwitchDone(AsyncResult ar, int networkType) {
        if (ar == null || ar.exception != null) {
            loge("set prefer network mode failed!");
            if (!HwFullNetworkConfig.IS_DUAL_4G_SUPPORTED) {
                sendLteServiceSwitchResult(this.mChipCommon.getUserSwitchDualCardSlots(), false);
                rollbackPrimaryStackPrefNetworkType();
            } else {
                sendLteServiceSwitchResult(this.mChipOther.mSecondaryStackPhoneId, false);
            }
            this.mChipOther.mNeedSetSecondStack = false;
            return;
        }
        this.mChipOther.mSetSecondaryStackPrefMode = networkType;
        logd("set prefer network mode success! setSecondaryStackPrefMode = " + this.mChipOther.mSetSecondaryStackPrefMode);
        if (needSetSecondStack()) {
            saveNetworkTypeToDB();
            sendLteServiceSwitchResult(this.mChipCommon.getUserSwitchDualCardSlots(), true);
        } else {
            saveNetworkTypeToDB(this.mChipOther.mSecondaryStackPhoneId);
            sendLteServiceSwitchResult(this.mChipOther.mSecondaryStackPhoneId, true);
        }
        this.mChipOther.mNeedSetSecondStack = false;
    }

    private boolean needSetSecondStack() {
        return !HwFullNetworkConfig.IS_DUAL_4G_SUPPORTED || (HwFullNetworkConfig.IS_DUAL_4G_SUPPORTED && (!this.mChipCommon.isDualImsSwitchOpened() || isNeedSetSecondStackForCmcc()));
    }

    private boolean isNeedSetSecondStackForCmcc() {
        return (this.mChipOther.mNeedSetSecondStack || this.mChipCommon.isCmccHybirdAndNoRoaming()) && HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE;
    }

    private void saveNetworkTypeToDB(int subId) {
        int curPrefMode = this.mChipOther.getNetworkTypeFromDB(subId);
        if (subId == this.mChipOther.mPrimaryStackPhoneId) {
            logd("curPrefMode = " + curPrefMode + ", mSetPrimaryStackPrefMode =" + this.mChipOther.mSetPrimaryStackPrefMode);
            if (curPrefMode != this.mChipOther.mSetPrimaryStackPrefMode && this.mChipOther.mSetPrimaryStackPrefMode != -1) {
                this.mChipOther.setNetworkTypeToDB(subId, this.mChipOther.mSetPrimaryStackPrefMode);
                return;
            }
            return;
        }
        logd("curPrefMode = " + curPrefMode + ", mSetSecondaryStackPrefMode =" + this.mChipOther.mSetSecondaryStackPrefMode);
        if (curPrefMode != this.mChipOther.mSetSecondaryStackPrefMode && this.mChipOther.mSetSecondaryStackPrefMode != -1) {
            this.mChipOther.setNetworkTypeToDB(subId, this.mChipOther.mSetSecondaryStackPrefMode);
        }
    }

    private void saveNetworkTypeToDB() {
        saveNetworkTypeToDB(this.mChipOther.mPrimaryStackPhoneId);
        saveNetworkTypeToDB(this.mChipOther.mSecondaryStackPhoneId);
    }

    private void rollbackPrimaryStackPrefNetworkType() {
        int curPrefMode = this.mChipOther.getNetworkTypeFromDB(this.mChipOther.mPrimaryStackPhoneId);
        logd("rollbackPrimaryStackPrefNetworkType, curPrefMode = " + curPrefMode + ", mSetPrimaryStackPrefMode =" + this.mChipOther.mSetPrimaryStackPrefMode);
        if (curPrefMode != this.mChipOther.mSetPrimaryStackPrefMode) {
            this.mCis[this.mChipOther.mPrimaryStackPhoneId].setPreferredNetworkType(curPrefMode, obtainMessage(HwFullNetworkConstants.EVENT_SET_PRIMARY_STACK_ROLL_BACK_DONE, curPrefMode, 0, Integer.valueOf(this.mChipOther.mPrimaryStackPhoneId)));
        }
    }

    private void handleRollbackDone(AsyncResult ar) {
        logd("in rollbackDone");
        if (ar == null || ar.exception != null) {
            loge("set prefer network mode failed!");
        }
    }

    /* access modifiers changed from: protected */
    public void onServiceStateChangedForCMCC(Intent intent) {
        int cmccSlotId = this.mChipCommon.getCMCCCardSlotId();
        if (HwFullNetworkConfig.IS_CMCC_4GSWITCH_DISABLE && cmccSlotId != -1) {
            int slotId = intent.getIntExtra("subscription", -1);
            ServiceState serviceState = ServiceState.newFromBundle(intent.getExtras());
            if (slotId == cmccSlotId && this.mChipCommon.getCombinedRegState(serviceState) == 0) {
                boolean newRoamingState = TelephonyManager.getDefault().isNetworkRoaming(cmccSlotId);
                boolean oldRoamingState = this.mChipCommon.getLastRoamingStateFromSP();
                logd("mPhoneStateListener cmcccSlotId = " + cmccSlotId + " oldRoamingState=" + oldRoamingState + " newRoamingState=" + newRoamingState);
                if (oldRoamingState != newRoamingState) {
                    this.mChipCommon.saveLastRoamingStateToSP(newRoamingState);
                    if (this.mChipCommon.needForceSetDefaultSlot(newRoamingState, cmccSlotId)) {
                        forceSetDefault4GSlotForCMCC(cmccSlotId);
                        return;
                    } else {
                        this.mChipOther.mNeedSetLteServiceAbility = true;
                        this.mChipOther.setLteServiceAbility();
                    }
                }
            }
            if (slotId == cmccSlotId) {
                if (this.mChipCommon.mCmccSubIdOldState != 0 && this.mChipCommon.getCombinedRegState(serviceState) == 0) {
                    logd("OUT_OF_SERVICE -> IN_SERVICE, setPrefNW");
                    this.mChipOther.mNeedSetLteServiceAbility = true;
                    this.mChipOther.setLteServiceAbility();
                }
                this.mChipCommon.mCmccSubIdOldState = this.mChipCommon.getCombinedRegState(serviceState);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void logd(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
