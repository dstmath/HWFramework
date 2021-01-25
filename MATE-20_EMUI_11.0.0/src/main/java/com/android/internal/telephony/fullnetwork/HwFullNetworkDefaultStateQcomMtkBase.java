package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManager;
import android.telephony.ServiceState;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.ServiceStateEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneFactoryExt;

public class HwFullNetworkDefaultStateQcomMtkBase extends HwFullNetworkDefaultStateBase {
    private static final String LOG_TAG = "HwFullNetworkDefaultStateQcomMtkBase";
    private static final int OOS_DELAY_TIME = 20000;
    protected HwFullNetworkChipOther mChipOther = HwFullNetworkChipOther.getInstance();

    public HwFullNetworkDefaultStateQcomMtkBase(Context c, CommandsInterfaceEx[] ci, Handler h) {
        super(c, ci, h);
        logd("HwFullNetworkDefaultStateQcomMtkBase constructor");
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private static Boolean getCarrierConfig(Context context, String key, int subId) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        PersistableBundle persistTableBundle = null;
        if (configManager != null) {
            persistTableBundle = configManager.getConfigForSubId(subId);
        }
        boolean carrier = false;
        if (persistTableBundle != null) {
            carrier = persistTableBundle.getBoolean(key, false);
        }
        return Boolean.valueOf(carrier);
    }

    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase, android.os.Handler
    public void handleMessage(Message msg) {
        if (msg == null || msg.obj == null) {
            loge("msg or msg.obj is null, return!");
            return;
        }
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        switch (msg.what) {
            case HwFullNetworkConstantsInner.EVENT_SET_LTE_SERVICE_ABILITY /* 2004 */:
                setNetworkType(ar, msg.arg1);
                return;
            case HwFullNetworkConstantsInner.EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE /* 2005 */:
                logd("Received EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE");
                handleSetPrimaryStackLteSwitchDone(ar, msg.arg1);
                return;
            case HwFullNetworkConstantsInner.EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE /* 2006 */:
                logd("Received EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE");
                handleSetSecondaryStackLteSwitchDone(ar, msg.arg1);
                return;
            case HwFullNetworkConstantsInner.EVENT_SET_PRIMARY_STACK_ROLL_BACK_DONE /* 2007 */:
                logd("Received EVENT_SET_PRIMARY_STACK_ROLL_BACK_DONE");
                handleRollbackDone(ar);
                return;
            case HwFullNetworkConstantsInner.EVENT_RESET_OOS_FLAG /* 2008 */:
                int index = this.mChipCommon.getCiIndex(msg).intValue();
                logd("Received EVENT_RESET_OOS_FLAG on index " + index);
                PhoneFactoryExt.getPhone(index).setOOSFlagOnSelectNetworkManually(false);
                return;
            default:
                super.handleMessage(msg);
                return;
        }
    }

    private void handleGetPrefNetworkModeDone(Message msg) {
        int subId = msg.arg1;
        int modemNetworkMode = -1;
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null && ar.getException() == null) {
            modemNetworkMode = ((int[]) ar.getResult())[0];
        }
        logd("subId = " + subId + " modemNetworkMode = " + modemNetworkMode);
        if (this.mChipOther.mPrimaryStackPhoneId == subId) {
            Message response = obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE, this.mChipOther.mPrimaryStackNetworkType, 0, Integer.valueOf(this.mChipOther.mPrimaryStackPhoneId));
            if (modemNetworkMode == -1 || modemNetworkMode != this.mChipOther.mPrimaryStackNetworkType) {
                this.mCis[this.mChipOther.mPrimaryStackPhoneId].setPreferredNetworkType(this.mChipOther.mPrimaryStackNetworkType, response);
                return;
            }
            AsyncResultEx.forMessage(response);
            response.sendToTarget();
            logd("The sub" + subId + " pref network mode is same with modem's ,don't set again");
        } else if (this.mChipOther.mSecondaryStackPhoneId == subId) {
            Message response2 = obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE, this.mChipOther.mSecondaryStackNetworkType, 0, Integer.valueOf(this.mChipOther.mSecondaryStackPhoneId));
            if (modemNetworkMode == -1 || modemNetworkMode != this.mChipOther.mSecondaryStackNetworkType) {
                this.mCis[this.mChipOther.mSecondaryStackPhoneId].setPreferredNetworkType(this.mChipOther.mSecondaryStackNetworkType, response2);
                return;
            }
            AsyncResultEx.forMessage(response2);
            response2.sendToTarget();
            logd("The sub" + subId + " pref network mode is same with modem's ,don't set again");
        } else {
            logd("subId is not normal.");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void onRadioUnavailable(Integer index) {
        this.mChipCommon.mIccIds[index.intValue()] = null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void onRadioAvailable(Integer index) {
        logd("onRadioAvailable, index " + index);
        if (HwFullNetworkConstantsInner.SIM_NUM == 2) {
            syncNetworkTypeFromDB(index.intValue());
        }
        super.onRadioAvailable(index);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void processSubSetUiccResult(Intent intent) {
        if (intent != null) {
            int slotId = intent.getIntExtra("phone", -1);
            int subState = intent.getIntExtra("newSubState", -1);
            int result = intent.getIntExtra("operationResult", -1);
            logd("received ACTION_SUBSCRIPTION_SET_UICC_RESULT,slotId:" + slotId + " subState:" + subState + " result:" + result);
            if (slotId >= 0 && slotId < HwFullNetworkConstantsInner.SIM_NUM) {
                if (result == 1) {
                    if (subState == 1) {
                        this.mChipOther.mSetUiccSubscriptionResult[slotId] = subState;
                    } else {
                        this.mChipOther.mSetUiccSubscriptionResult[slotId] = -1;
                    }
                } else if (result == 0) {
                    this.mChipOther.mSetUiccSubscriptionResult[slotId] = -1;
                } else {
                    logd("received ACTION_SUBSCRIPTION_SET_UICC_RESULT,result invalid.");
                }
            }
        }
    }

    private void syncNetworkTypeFromDB(int subId) {
        if (this.mChipCommon.isValidIndex(subId)) {
            int pefMode = this.mChipOther.getNetworkTypeFromDB(subId);
            boolean firstStart = Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) == 0 && Settings.Secure.getInt(this.mContext.getContentResolver(), SettingsEx.Secure.getUserSetupComplete(), 1) == 0;
            logd("syncNetworkTypeFromDB, sub = " + subId + ", pefMode = " + pefMode + ", firstStart =" + firstStart);
            if (pefMode == -1 || firstStart) {
                int defaultPrefMode = SystemPropertiesEx.getInt("ro.telephony.default_network", -1);
                int ability = getPrimaryAndSecondaryStackAbility(subId);
                if (HwTelephonyManager.getDefault().isNrSupported()) {
                    setServiceAbilityForQCOM(subId, 1, ability, defaultPrefMode);
                } else {
                    setLteServiceAbilityForQCOM(subId, ability, defaultPrefMode);
                }
            }
        }
    }

    private int getPrimaryAndSecondaryStackAbility(int subId) {
        return HwTelephonyManager.getDefault().isNrSupported() ? subId == this.mChipCommon.getUserSwitchDualCardSlots() ? 1 : 0 : (!this.mChipCommon.isDualImsSwitchOpened() && subId != this.mChipCommon.getUserSwitchDualCardSlots()) ? 0 : 1;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void processPreBootCompleted() {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void setMainSlot(int slotId, Message responseMsg) {
        if (!SystemPropertiesEx.getBoolean("persist.sys.dualcards", false)) {
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
                this.mStateHandler.obtainMessage(202, slotId, 0, responseMsg).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void setLteServiceAbilityForQCOM(int subId, int ability, int serviceOnMappingMode) {
        getStackPhoneId();
        if (this.mChipCommon.isDualImsSwitchOpened() || subId != this.mChipCommon.getUserSwitchDualCardSlots()) {
            logd("setLteServiceAbilityForQCOM, dual Ims new, sub =" + subId + ", ability =" + ability);
            if (this.mChipOther.mPrimaryStackPhoneId == subId) {
                this.mChipOther.mPrimaryStackNetworkType = getPrimaryStackNetworkType(0, ability, serviceOnMappingMode);
            } else {
                this.mChipOther.mSecondaryStackNetworkType = getSecondaryStackNetworkType(ability, serviceOnMappingMode);
            }
            recordPrimaryAndSecondaryStackNetworkTypeForCmcc(ability);
            this.mCis[subId].getPreferredNetworkType(obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_LTE_SERVICE_ABILITY, subId, 0));
            return;
        }
        logd("in setLteServiceAbilityForQCOM, single Ims.");
        recordPrimaryAndSecondaryStackNetworkType(0, ability, serviceOnMappingMode);
        this.mCis[this.mChipOther.mPrimaryStackPhoneId].getPreferredNetworkType(obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_LTE_SERVICE_ABILITY, this.mChipOther.mPrimaryStackPhoneId, 0));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void setServiceAbilityForQCOM(int sotId, int type, int ability, int serviceOnMappingMode) {
        getStackPhoneId();
        boolean isNeedSetSecondStackNr = HwTelephonyManager.getDefault().isNrSupported() && this.mChipOther.mNeedSetSecondStack;
        if ((!this.mChipCommon.isDualImsSwitchOpened() || isNeedSetSecondStackNr) && sotId == this.mChipCommon.getUserSwitchDualCardSlots()) {
            logd("in setServiceAbilityForQCOM, single Ims or nr supported");
            recordPrimaryAndSecondaryStackNetworkType(type, ability, serviceOnMappingMode);
            this.mCis[this.mChipOther.mPrimaryStackPhoneId].getPreferredNetworkType(obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_LTE_SERVICE_ABILITY, this.mChipOther.mPrimaryStackPhoneId, 0));
            return;
        }
        logd("setServiceAbilityForQCOM, dual Ims new, sub =" + sotId + ", type = " + type + ", ability =" + ability);
        if (this.mChipOther.mPrimaryStackPhoneId == sotId) {
            this.mChipOther.mPrimaryStackNetworkType = getPrimaryStackNetworkType(type, ability, serviceOnMappingMode);
        } else {
            this.mChipOther.mSecondaryStackNetworkType = getSecondaryStackNetworkType(ability, serviceOnMappingMode);
        }
        recordPrimaryAndSecondaryStackNetworkTypeForCmcc(ability);
        this.mCis[sotId].getPreferredNetworkType(obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_LTE_SERVICE_ABILITY, sotId, 0));
    }

    private void getStackPhoneId() {
        int i = 0;
        this.mChipOther.mPrimaryStackPhoneId = SystemPropertiesEx.getInt("persist.radio.msim.stackid_0", 0);
        HwFullNetworkChipOther hwFullNetworkChipOther = this.mChipOther;
        if (this.mChipOther.mPrimaryStackPhoneId == 0) {
            i = 1;
        }
        hwFullNetworkChipOther.mSecondaryStackPhoneId = i;
        logd("getStackPhoneId mPrimaryStackPhoneId:" + this.mChipOther.mPrimaryStackPhoneId + ", mSecondaryStackPhoneId:" + this.mChipOther.mSecondaryStackPhoneId);
    }

    private int getPrimaryStackNetworkType(int type, int ability, int serviceOnMappingMode) {
        int primaryServiceOffNetworkType;
        int primaryServiceOnNetworkType = serviceOnMappingMode;
        boolean isPrimaryMainSlot = this.mChipCommon.getUserSwitchDualCardSlots() == this.mChipOther.mPrimaryStackPhoneId;
        if (this.mChipCommon.isDualImsSwitchOpened() || isPrimaryMainSlot) {
            primaryServiceOffNetworkType = HwNetworkTypeUtils.getOffModeFromMapping(serviceOnMappingMode);
        } else {
            primaryServiceOffNetworkType = HwNetworkTypeUtils.getLteOffMappingMode();
        }
        if (serviceOnMappingMode == -1) {
            if (type == 0) {
                primaryServiceOnNetworkType = 22;
                primaryServiceOffNetworkType = 21;
            } else {
                primaryServiceOnNetworkType = 69;
                primaryServiceOffNetworkType = (this.mChipCommon.isDualImsSwitchOpened() || isPrimaryMainSlot) ? 22 : 21;
            }
        }
        return ability == 1 ? primaryServiceOnNetworkType : primaryServiceOffNetworkType;
    }

    private int getSecondaryStackNetworkType(int ability, int serviceOnMappingMode) {
        int secondaryNetworkType;
        int tempNetworkType;
        boolean isSecondaryMainSlot = true;
        if (ability == 1) {
            secondaryNetworkType = HwFullNetworkConfigInner.IS_QCOM_DUAL_LTE_STACK ? serviceOnMappingMode : 20;
            if (serviceOnMappingMode == 9 || serviceOnMappingMode == 10) {
                secondaryNetworkType = 9;
            }
        } else {
            if (this.mChipCommon.getUserSwitchDualCardSlots() != this.mChipOther.mSecondaryStackPhoneId) {
                isSecondaryMainSlot = false;
            }
            if (this.mChipCommon.isDualImsSwitchOpened() || isSecondaryMainSlot) {
                tempNetworkType = HwNetworkTypeUtils.getOffModeFromMapping(serviceOnMappingMode);
            } else {
                tempNetworkType = HwNetworkTypeUtils.getLteOffMappingMode();
            }
            secondaryNetworkType = HwFullNetworkConfigInner.IS_QCOM_DUAL_LTE_STACK ? tempNetworkType : 18;
            if (serviceOnMappingMode == 9 || serviceOnMappingMode == 10) {
                secondaryNetworkType = 3;
            }
        }
        logd("getSecondaryStackNetworkType secondaryNetworkType:" + secondaryNetworkType);
        return secondaryNetworkType;
    }

    private void recordPrimaryAndSecondaryStackNetworkType(int type, int ability, int serviceOnMappingMode) {
        this.mChipOther.mPrimaryStackNetworkType = getPrimaryStackNetworkType(type, ability, serviceOnMappingMode);
        this.mChipOther.mSecondaryStackNetworkType = getSecondaryStackNetworkType(ability, serviceOnMappingMode);
        int otherSub = 0;
        if (HwFullNetworkConfigInner.IS_QCOM_DUAL_LTE_STACK) {
            if (this.mChipOther.mPrimaryStackPhoneId == this.mChipCommon.getUserSwitchDualCardSlots()) {
                this.mChipOther.mSecondaryStackNetworkType = getSecondaryStackNetworkType(0, serviceOnMappingMode);
                boolean primaryCardHide = getCarrierConfig(this.mContext, HwFullNetworkConstantsInner.NET_WORK_MODE_HIDE, this.mChipOther.mPrimaryStackPhoneId).booleanValue();
                boolean nrHidePreMode = getCarrierConfig(this.mContext, HwFullNetworkConstantsInner.NR_HIDE_PREF_NETWORK_MODE, this.mChipOther.mPrimaryStackPhoneId).booleanValue();
                boolean nrSwitchAvailable = getCarrierConfig(this.mContext, HwFullNetworkConstantsInner.NR_SWITCH_AVAILABLE, this.mChipOther.mPrimaryStackPhoneId).booleanValue();
                boolean lteUserSwitchState = type == 0 && HwFullNetworkConfigInner.IS_NET_WORK_MODE_HIDE && primaryCardHide;
                boolean nrUserSwitchState = type == 1 && nrHidePreMode && !nrSwitchAvailable;
                if (lteUserSwitchState || nrUserSwitchState) {
                    this.mChipOther.mPrimaryStackNetworkType = getPrimaryStackNetworkType(type, 1, serviceOnMappingMode);
                }
            } else {
                this.mChipOther.mPrimaryStackNetworkType = getPrimaryStackNetworkType(type, 0, serviceOnMappingMode);
                boolean secondaryCardHide = getCarrierConfig(this.mContext, HwFullNetworkConstantsInner.NET_WORK_MODE_HIDE, this.mChipOther.mSecondaryStackPhoneId).booleanValue();
                boolean nrHidePreMode2 = getCarrierConfig(this.mContext, HwFullNetworkConstantsInner.NR_HIDE_PREF_NETWORK_MODE, this.mChipOther.mSecondaryStackPhoneId).booleanValue();
                boolean nrSwitchAvailable2 = getCarrierConfig(this.mContext, HwFullNetworkConstantsInner.NR_SWITCH_AVAILABLE, this.mChipOther.mSecondaryStackPhoneId).booleanValue();
                boolean lteUserSwitchState2 = type == 0 && HwFullNetworkConfigInner.IS_NET_WORK_MODE_HIDE && secondaryCardHide;
                boolean nrUserSwitchState2 = type == 1 && nrHidePreMode2 && !nrSwitchAvailable2;
                if (lteUserSwitchState2 || nrUserSwitchState2) {
                    this.mChipOther.mSecondaryStackNetworkType = getSecondaryStackNetworkType(1, serviceOnMappingMode);
                }
            }
        }
        if (this.mChipCommon.isCmccHybirdAndNoRoaming()) {
            recordNetworkTypeForCmccHybird(ability);
            if (this.mChipCommon.default4GSlot == 0) {
                otherSub = 1;
            }
            PhoneFactoryExt.getPhone(otherSub).setOOSFlagOnSelectNetworkManually(true);
            if (hasMessages(HwFullNetworkConstantsInner.EVENT_RESET_OOS_FLAG)) {
                removeMessages(HwFullNetworkConstantsInner.EVENT_RESET_OOS_FLAG);
            }
            Message msg = obtainMessage(HwFullNetworkConstantsInner.EVENT_RESET_OOS_FLAG, Integer.valueOf(otherSub));
            AsyncResultEx.forMessage(msg, (Object) null, (Throwable) null);
            sendMessageDelayed(msg, 20000);
        }
        logd("recordPrimaryAndSecondaryStackNetworkType mPrimaryStackNetworkType:" + this.mChipOther.mPrimaryStackNetworkType + ",mSecondaryStackNetworkType:" + this.mChipOther.mSecondaryStackNetworkType);
    }

    private void recordPrimaryAndSecondaryStackNetworkTypeForCmcc(int ability) {
        if (HwFullNetworkConfigInner.isCMCCDsdxDisable() && this.mChipCommon.isDualImsSwitchOpened() && this.mChipOther.mNeedSetSecondStack) {
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
            } else {
                logd("main slot is not normal.");
            }
            logd("recordPrimaryAndSecondaryStackNetworkTypeForCmcc: mPrimaryStackNetworkType = " + this.mChipOther.mPrimaryStackNetworkType + "   mSecondaryStackNetworkType=" + this.mChipOther.mSecondaryStackNetworkType);
        }
    }

    private boolean isCmccCardBySubCarrierType(int i) {
        return this.mChipCommon.subCarrierTypeArray[i].isCMCCCard();
    }

    private void recordNetworkTypeForCmccHybird(int ability) {
        int otherNetworkType = ability == 1 ? 22 : 21;
        if (HwFullNetworkConfigInner.IS_QCOM_DUAL_LTE_STACK) {
            otherNetworkType = 21;
        }
        this.mChipCommon.default4GSlot = this.mChipCommon.getUserSwitchDualCardSlots();
        if (this.mChipOther.mPrimaryStackPhoneId == this.mChipCommon.default4GSlot) {
            this.mChipOther.mSecondaryStackNetworkType = otherNetworkType;
        } else {
            this.mChipOther.mPrimaryStackNetworkType = otherNetworkType;
        }
    }

    private void setNetworkType(AsyncResultEx ar, int subId) {
        int modemNetworkMode = -1;
        if (ar != null && ar.getException() == null) {
            modemNetworkMode = ((int[]) ar.getResult())[0];
        }
        logd("subId = " + subId + " modemNetworkMode = " + modemNetworkMode);
        if (this.mChipOther.mPrimaryStackPhoneId == subId) {
            Message response = obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_PRIMARY_STACK_LTE_SWITCH_DONE, this.mChipOther.mPrimaryStackNetworkType, 0, Integer.valueOf(this.mChipOther.mPrimaryStackPhoneId));
            if (modemNetworkMode == -1 || modemNetworkMode != this.mChipOther.mPrimaryStackNetworkType) {
                this.mCis[this.mChipOther.mPrimaryStackPhoneId].setPreferredNetworkType(this.mChipOther.mPrimaryStackNetworkType, response);
                return;
            }
            AsyncResultEx.forMessage(response);
            response.sendToTarget();
            logd("The sub" + subId + " pref network mode is same with modem's, don't set again");
        } else if (this.mChipOther.mSecondaryStackPhoneId == subId) {
            if (this.mChipOther.mSecondaryStackNetworkType == -1) {
                this.mChipOther.mSecondaryStackNetworkType = this.mChipOther.getNetworkTypeFromDB(this.mChipOther.mSecondaryStackPhoneId);
            }
            Message response2 = obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_SECONDARY_STACK_LTE_SWITCH_DONE, this.mChipOther.mSecondaryStackNetworkType, 0, Integer.valueOf(this.mChipOther.mSecondaryStackPhoneId));
            if (modemNetworkMode == -1 || modemNetworkMode != this.mChipOther.mSecondaryStackNetworkType) {
                this.mCis[this.mChipOther.mSecondaryStackPhoneId].setPreferredNetworkType(this.mChipOther.mSecondaryStackNetworkType, response2);
                return;
            }
            AsyncResultEx.forMessage(response2);
            response2.sendToTarget();
            logd("The sub" + subId + " pref network mode is same with modem's, don't set again");
        } else {
            logd("subId is not normal ");
        }
    }

    private void handleSetPrimaryStackLteSwitchDone(AsyncResultEx ar, int networkType) {
        logd("in handleSetPrimaryStackLteSwitchDone");
        if (ar == null || ar.getException() != null) {
            loge("set prefer network mode failed!");
            if (!HwFullNetworkConfigInner.IS_DUAL_4G_SUPPORTED) {
                sendLteServiceSwitchResult(this.mChipCommon.getUserSwitchDualCardSlots(), false);
            } else {
                sendLteServiceSwitchResult(this.mChipOther.mPrimaryStackPhoneId, false);
            }
            this.mChipOther.mNeedSetSecondStack = false;
            return;
        }
        this.mChipOther.mSetPrimaryStackPrefMode = networkType;
        logd("setPrimaryStackPrefMode = " + this.mChipOther.mSetPrimaryStackPrefMode);
        boolean isNeedSetSecondStack = !this.mChipCommon.isDualImsSwitchOpened() || (this.mChipOther.mNeedSetSecondStack && (HwFullNetworkConfigInner.isCMCCDsdxDisable() || HwTelephonyManager.getDefault().isNrSupported()));
        if (!HwFullNetworkConfigInner.IS_DUAL_4G_SUPPORTED || (HwFullNetworkConfigInner.IS_DUAL_4G_SUPPORTED && isNeedSetSecondStack)) {
            this.mCis[this.mChipOther.mSecondaryStackPhoneId].getPreferredNetworkType(obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_LTE_SERVICE_ABILITY, this.mChipOther.mSecondaryStackPhoneId, 0));
            return;
        }
        saveNetworkTypeToDB(this.mChipOther.mPrimaryStackPhoneId);
        logd("set prefer network mode success!");
        sendLteServiceSwitchResult(this.mChipOther.mPrimaryStackPhoneId, true);
    }

    private void sendLteServiceSwitchResult(int subId, boolean result) {
        logd("LTE service Switch result is " + result + ". broadcast PREFERRED_4G_SWITCH_DONE");
        if (this.mContext == null) {
            loge("Context is null, return!");
            return;
        }
        Intent intent = new Intent("com.huawei.telephony.PREF_4G_SWITCH_DONE");
        intent.putExtra("subscription", subId);
        intent.putExtra("slot", subId);
        intent.putExtra("setting_result", result);
        this.mContext.sendOrderedBroadcast(intent, "android.permission.READ_PHONE_STATE");
    }

    private void handleSetSecondaryStackLteSwitchDone(AsyncResultEx ar, int networkType) {
        if (ar == null || ar.getException() != null) {
            loge("set prefer network mode failed!");
            if (!HwFullNetworkConfigInner.IS_DUAL_4G_SUPPORTED) {
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
        boolean isChipDualAndNeedSave = !this.mChipCommon.isDualImsSwitchOpened() || (this.mChipOther.mNeedSetSecondStack && (this.mChipCommon.isCmccHybirdAndNoRoaming() || HwTelephonyManager.getDefault().isNrSupported()));
        if (!HwFullNetworkConfigInner.IS_DUAL_4G_SUPPORTED || (HwFullNetworkConfigInner.IS_DUAL_4G_SUPPORTED && isChipDualAndNeedSave)) {
            saveNetworkTypeToDB();
            sendLteServiceSwitchResult(this.mChipCommon.getUserSwitchDualCardSlots(), true);
        } else {
            saveNetworkTypeToDB(this.mChipOther.mSecondaryStackPhoneId);
            sendLteServiceSwitchResult(this.mChipOther.mSecondaryStackPhoneId, true);
        }
        this.mChipOther.mNeedSetSecondStack = false;
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
            this.mCis[this.mChipOther.mPrimaryStackPhoneId].setPreferredNetworkType(curPrefMode, obtainMessage(HwFullNetworkConstantsInner.EVENT_SET_PRIMARY_STACK_ROLL_BACK_DONE, curPrefMode, 0, Integer.valueOf(this.mChipOther.mPrimaryStackPhoneId)));
        }
    }

    private void handleRollbackDone(AsyncResultEx ar) {
        logd("in rollbackDone");
        if (ar == null || ar.getException() != null) {
            loge("set prefer network mode failed!");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void onServiceStateChangedForCMCC(Intent intent) {
        int cmccSlotId = this.mChipCommon.getCMCCCardSlotId();
        if (HwFullNetworkConfigInner.isCMCCDsdxDisable() && cmccSlotId != -1 && intent != null) {
            int slotId = intent.getIntExtra("slot", -1);
            ServiceState serviceState = ServiceStateEx.newFromBundle(intent.getExtras());
            if (slotId == cmccSlotId && this.mChipCommon.getCombinedRegState(serviceState) == 0) {
                int cmccSubId = -1;
                int[] subIds = SubscriptionManagerEx.getSubId(cmccSlotId);
                if (subIds != null && subIds.length > 0) {
                    cmccSubId = subIds[0];
                }
                boolean newRoamingState = TelephonyManagerEx.isNetworkRoaming(cmccSubId);
                boolean oldRoamingState = this.mChipCommon.getLastRoamingStateFromSP();
                logd("mPhoneStateListener cmcccSlotId = " + cmccSlotId + " oldRoamingState=" + oldRoamingState + " newRoamingState=" + newRoamingState);
                if (oldRoamingState != newRoamingState) {
                    this.mChipCommon.saveLastRoamingStateToSP(newRoamingState);
                    if (this.mChipCommon.needForceSetDefaultSlot(newRoamingState, cmccSlotId)) {
                        forceSetDefault4GSlotForCMCC(cmccSlotId);
                        return;
                    } else {
                        this.mChipOther.mNeedSetLteServiceAbility = true;
                        this.mChipOther.setServiceAbility();
                    }
                }
            }
            if (slotId == cmccSlotId) {
                if (this.mChipCommon.mCmccSubIdOldState != 0 && this.mChipCommon.getCombinedRegState(serviceState) == 0) {
                    logd("OUT_OF_SERVICE -> IN_SERVICE, setPrefNW");
                    this.mChipOther.mNeedSetLteServiceAbility = true;
                    this.mChipOther.setServiceAbility();
                }
                this.mChipCommon.mCmccSubIdOldState = this.mChipCommon.getCombinedRegState(serviceState);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.telephony.fullnetwork.HwFullNetworkDefaultStateBase
    public void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }
}
