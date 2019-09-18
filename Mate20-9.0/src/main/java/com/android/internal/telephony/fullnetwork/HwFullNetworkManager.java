package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwDualCardSwitcher;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.UiccCard;

public class HwFullNetworkManager {
    private static final String LOG_TAG = "HwFullNetworkManager";
    private static HwFullNetworkChipCommon mChipCommon;
    private static HwFullNetworkDefaultStateBase mDefaultStateBase;
    private static HwDualCardSwitcher mDualCardSwitcher;
    private static HwFullNetworkInitStateBase mInitStateBase;
    private static HwFullNetworkManager mInstance = null;
    private static final Object mLock = new Object();
    private Handler mStateHandler;

    private HwFullNetworkManager(Context c, CommandsInterface[] ci) {
        HwFullNetworkChipFactory.make(c, ci);
        mChipCommon = HwFullNetworkChipFactory.getChipCommon();
        HwFullNetworkStateMachine stateMachine = HwFullNetworkStateMachine.make(c, ci);
        this.mStateHandler = stateMachine.getHandler();
        mDefaultStateBase = stateMachine.getDefaultStateBase();
        mInitStateBase = stateMachine.getInitStateBase();
        logd("HwFullNetworkManager construct finish!");
    }

    public static HwFullNetworkManager make(Context c, CommandsInterface[] ci) {
        HwFullNetworkManager hwFullNetworkManager;
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new HwFullNetworkManager(c, ci);
                if (HwFullNetworkConstants.SIM_NUM > 1) {
                    mDualCardSwitcher = HwDualCardSwitcher.make(c);
                }
                hwFullNetworkManager = mInstance;
            } else {
                throw new RuntimeException("HwFullNetworkManager.make() should only be called once");
            }
        }
        return hwFullNetworkManager;
    }

    public static HwFullNetworkManager getInstance() {
        HwFullNetworkManager hwFullNetworkManager;
        synchronized (mLock) {
            if (mInstance != null) {
                hwFullNetworkManager = mInstance;
            } else {
                throw new RuntimeException("HwFullNetworkManager.getInstance can't be called before make()");
            }
        }
        return hwFullNetworkManager;
    }

    public void onGetIccCardStatusDone(AsyncResult ar, Integer index) {
        if (mInitStateBase != null) {
            mInitStateBase.onGetIccCardStatusDone(ar, index);
        }
    }

    public void initUiccCard(UiccCard uiccCard, IccCardStatus status, Integer index) {
        mChipCommon.initUiccCard(uiccCard, status, index);
    }

    public void updateUiccCard(UiccCard uiccCard, IccCardStatus status, Integer index) {
        mChipCommon.updateUiccCard(uiccCard, status, index);
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
        mChipCommon.registerForIccChanged(h, what, obj);
    }

    public void unregisterForIccChanged(Handler h) {
        mChipCommon.unregisterForIccChanged(h);
    }

    public boolean getWaitingSwitchBalongSlot() {
        return mChipCommon.getWaitingSwitchBalongSlot();
    }

    public boolean get4GSlotInProgress() {
        return mChipCommon.isSet4GSlotInProgress;
    }

    public boolean get4GSlotInSwitchProgress() {
        return mChipCommon.isSet4GSlotInSwitchProgress;
    }

    public boolean isSet4GDoneAfterSimInsert() {
        return mChipCommon.isSet4GDoneAfterSimInsert();
    }

    public int getUserSwitchDualCardSlots() {
        return mChipCommon.getUserSwitchDualCardSlots();
    }

    public boolean isCMCCCard(String inn) {
        return mChipCommon.isCMCCCard(inn);
    }

    public boolean isCUCard(String inn) {
        return mChipCommon.isCUCard(inn);
    }

    public boolean isCTCard(String inn) {
        return mChipCommon.isCTCard(inn);
    }

    public boolean isCMCCCardBySlotId(int slotId) {
        return mChipCommon.isCMCCCardBySlotId(slotId);
    }

    public boolean isCMCCHybird() {
        return mChipCommon.isCMCCHybird();
    }

    public boolean isCTCardBySlotId(int slotId) {
        return mChipCommon.isCTCardBySlotId(slotId);
    }

    public boolean isCTHybird() {
        return mChipCommon.isCTHybird();
    }

    public boolean isSettingDefaultData() {
        return mChipCommon.isSettingDefaultData();
    }

    public boolean isSet4GSlotManuallyTriggered() {
        return mChipCommon.isSet4GSlotManuallyTriggered;
    }

    public void setDefault4GSlotForMDM() {
        this.mStateHandler.sendMessage(this.mStateHandler.obtainMessage(HwFullNetworkConstants.EVENT_CHECK_MAIN_SLOT_FOR_MDM, 0));
        if (mDualCardSwitcher != null) {
            mDualCardSwitcher.stop();
        }
    }

    public int getBalongSimSlot() {
        return mChipCommon.getBalongSimSlot();
    }

    public void setLteServiceAbilityForQCOM(int subId, int ability, int lteOnMappingMode) {
        if (mDefaultStateBase != null) {
            mDefaultStateBase.setLteServiceAbilityForQCOM(subId, ability, lteOnMappingMode);
        }
    }

    public void setMainSlot(int slotId, Message responseMsg) {
        if (mDefaultStateBase != null) {
            mDefaultStateBase.setMainSlot(slotId, responseMsg);
            if (mDualCardSwitcher != null && responseMsg != null) {
                Handler target = responseMsg.getTarget();
                if (target != null && !(target instanceof HwDualCardSwitcher)) {
                    mDualCardSwitcher.stop();
                }
            }
        }
    }

    public boolean isSwitchDualCardSlotsEnabled() {
        return mChipCommon.isSwitchDualCardSlotsEnabled();
    }

    public void setWaitingSwitchBalongSlot(boolean iSetResult) {
        mChipCommon.setWaitingSwitchBalongSlot(iSetResult);
    }

    public void resetUiccSubscriptionResultFlag(int slotId) {
        mChipCommon.resetUiccSubscriptionResultFlag(slotId);
    }

    public int getSpecCardType(int slotId) {
        return mChipCommon.getSpecCardType(slotId);
    }

    public boolean isUserPref4GSlot(int slotId) {
        return mChipCommon.isUserPref4GSlot(slotId);
    }

    public void setPreferredNetworkType(int networkType, int phoneId, Message response) {
        mChipCommon.setPreferredNetworkType(networkType, phoneId, response);
    }

    public boolean isRestartRildProgress() {
        return mChipCommon.isRestartRildProgress();
    }

    public int getDefaultMainSlotByIccId(int temSub) {
        return mChipCommon.getDefaultMainSlotByIccId(temSub);
    }

    public String getFullIccid(int subId) {
        return mChipCommon.getFullIccid(subId);
    }

    public void saveMainCardIccId(String iccId) {
        mChipCommon.saveMainCardIccId(iccId);
    }

    private void logd(String s) {
        Rlog.d(LOG_TAG, s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, s);
    }
}
