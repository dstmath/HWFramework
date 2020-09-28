package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.HwDualCardSwitcher;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;

public class HwFullNetworkManagerImpl extends DefaultHwFullNetworkManager {
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "HwFullNetworkManager";
    private static HwFullNetworkChipCommon mChipCommon;
    private static HwFullNetworkDefaultStateBase mDefaultStateBase;
    private static HwDualCardSwitcher mDualCardSwitcher;
    private static HwFullNetworkInitStateBase mInitStateBase;
    private static HwFullNetworkManagerImpl sInstance = null;
    private Handler mStateHandler;

    private HwFullNetworkManagerImpl() {
    }

    private HwFullNetworkManagerImpl(Context c, CommandsInterfaceEx[] ci) {
        HwFullNetworkChipFactory.make(c, ci);
        mChipCommon = HwFullNetworkChipFactory.getChipCommon();
        HwFullNetworkStateMachine stateMachine = HwFullNetworkStateMachine.make(c, ci);
        this.mStateHandler = stateMachine.getHandler();
        mDefaultStateBase = stateMachine.getDefaultStateBase();
        mInitStateBase = stateMachine.getInitStateBase();
        logd("HwFullNetworkManager construct finish!");
    }

    public void makeHwFullNetworkManager(Context c, CommandsInterfaceEx[] ci) {
        synchronized (LOCK) {
            sInstance = new HwFullNetworkManagerImpl(c, ci);
            if (HwFullNetworkConstantsInner.SIM_NUM > 1) {
                mDualCardSwitcher = HwDualCardSwitcher.make(c);
            }
        }
    }

    public static HwFullNetworkManagerImpl getInstance() {
        HwFullNetworkManagerImpl hwFullNetworkManagerImpl;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new HwFullNetworkManagerImpl();
            }
            hwFullNetworkManagerImpl = sInstance;
        }
        return hwFullNetworkManagerImpl;
    }

    public boolean isFullnetworkSupported() {
        return true;
    }

    public void onGetIccCardStatusDone(Object ar, Integer index) {
        if (mInitStateBase != null) {
            mInitStateBase.onGetIccCardStatusDone(ar, index);
        }
    }

    public void initUiccCard(UiccCardExt uiccCard, IccCardStatusExt status, Integer index) {
        mChipCommon.initUiccCard(uiccCard, status, index);
    }

    public void updateUiccCard(UiccCardExt uiccCard, IccCardStatusExt status, Integer index) {
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

    public void setWaitingSwitchBalongSlot(boolean iSetResult) {
        mChipCommon.setWaitingSwitchBalongSlot(iSetResult);
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

    public void setDefault4GSlotForMDM() {
        if (this.mStateHandler == null) {
            loge("setDefault4GSlotForMDM, not support called if mStateHandler is null");
            return;
        }
        this.mStateHandler.sendMessage(this.mStateHandler.obtainMessage(HwFullNetworkConstantsInner.EVENT_CHECK_MAIN_SLOT_FOR_MDM, 0));
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

    public void setServiceAbilityForQCOM(int slotId, int type, int ability, int serviceOnMappingMode) {
        if (mDefaultStateBase != null) {
            mDefaultStateBase.setServiceAbilityForQCOM(slotId, type, ability, serviceOnMappingMode);
        }
    }

    public void setMainSlot(int slotId, Message responseMsg) {
        Handler target;
        if (mDefaultStateBase != null) {
            mDefaultStateBase.setMainSlot(slotId, responseMsg);
            if (mDualCardSwitcher != null && responseMsg != null && (target = responseMsg.getTarget()) != null && !(target instanceof HwDualCardSwitcher)) {
                mDualCardSwitcher.stop();
            }
        }
    }

    public boolean isSwitchDualCardSlotsEnabled() {
        return mChipCommon.isSwitchDualCardSlotsEnabled();
    }

    public boolean isCMCCDsdxEnable() {
        return HwFullNetworkConfigInner.isCMCCDsdxEnable();
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

    public String getFullIccid(int slotId) {
        return mChipCommon.getFullIccid(slotId);
    }

    public void saveMainCardIccId(String iccId) {
        mChipCommon.saveMainCardIccId(iccId);
    }

    public boolean isCMCCDsdxDisable() {
        return HwFullNetworkConfigInner.isCMCCDsdxDisable();
    }

    private void logd(String s) {
        RlogEx.d(LOG_TAG, s);
    }

    private void loge(String s) {
        RlogEx.e(LOG_TAG, s);
    }
}
