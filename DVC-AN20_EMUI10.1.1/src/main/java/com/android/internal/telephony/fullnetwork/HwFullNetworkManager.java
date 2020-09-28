package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.internal.telephony.HwPartOptTelephonyFactory;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;

public class HwFullNetworkManager {
    private static final String TAG = "HwFullNetworkManager";
    private static HwFullNetworkManager sInstance;
    private DefaultHwFullNetworkManager mHwFullNetworkManager = HwPartOptTelephonyFactory.getTelephonyFactory().getFullnetworkFactory().getHwFullnetworkManager();

    private HwFullNetworkManager() {
        Log.d(TAG, "add " + this.mHwFullNetworkManager + " to memory");
    }

    public static HwFullNetworkManager getInstance() {
        if (sInstance == null) {
            sInstance = new HwFullNetworkManager();
        }
        return sInstance;
    }

    public boolean isFullnetworkSupported() {
        return this.mHwFullNetworkManager.isFullnetworkSupported();
    }

    public String getFullIccid(int slotId) {
        return this.mHwFullNetworkManager.getFullIccid(slotId);
    }

    public int getDefaultMainSlotByIccId(int temSub) {
        return this.mHwFullNetworkManager.getDefaultMainSlotByIccId(temSub);
    }

    public boolean isCMCCCardBySlotId(int slotId) {
        return this.mHwFullNetworkManager.isCMCCCardBySlotId(slotId);
    }

    public boolean isCMCCHybird() {
        return this.mHwFullNetworkManager.isCMCCHybird();
    }

    public void saveMainCardIccId(String iccId) {
        this.mHwFullNetworkManager.saveMainCardIccId(iccId);
    }

    public void setMainSlot(int slotId, Message responseMsg) {
        this.mHwFullNetworkManager.setMainSlot(slotId, responseMsg);
    }

    public boolean isCMCCDsdxDisable() {
        return this.mHwFullNetworkManager.isCMCCDsdxDisable();
    }

    public int getBalongSimSlot() {
        return this.mHwFullNetworkManager.getBalongSimSlot();
    }

    public void setLteServiceAbilityForQCOM(int slotId, int ability, int lteOnMappingMode) {
        this.mHwFullNetworkManager.setLteServiceAbilityForQCOM(slotId, ability, lteOnMappingMode);
    }

    public void setServiceAbilityForQCOM(int slotId, int type, int ability, int servicOnMappingMode) {
        this.mHwFullNetworkManager.setServiceAbilityForQCOM(slotId, type, ability, servicOnMappingMode);
    }

    public void setWaitingSwitchBalongSlot(boolean isSetResult) {
        this.mHwFullNetworkManager.setWaitingSwitchBalongSlot(isSetResult);
    }

    public boolean isSwitchDualCardSlotsEnabled() {
        return this.mHwFullNetworkManager.isSwitchDualCardSlotsEnabled();
    }

    public boolean getWaitingSwitchBalongSlot() {
        return this.mHwFullNetworkManager.getWaitingSwitchBalongSlot();
    }

    public boolean isUserPref4GSlot(int slotId) {
        return this.mHwFullNetworkManager.isUserPref4GSlot(slotId);
    }

    public boolean isRestartRildProgress() {
        return this.mHwFullNetworkManager.isRestartRildProgress();
    }

    public boolean get4GSlotInSwitchProgress() {
        return this.mHwFullNetworkManager.get4GSlotInSwitchProgress();
    }

    public int getUserSwitchDualCardSlots() {
        return this.mHwFullNetworkManager.getUserSwitchDualCardSlots();
    }

    public boolean get4GSlotInProgress() {
        return this.mHwFullNetworkManager.get4GSlotInProgress();
    }

    public void resetUiccSubscriptionResultFlag(int slotId) {
        this.mHwFullNetworkManager.resetUiccSubscriptionResultFlag(slotId);
    }

    public boolean isCTCardBySlotId(int slotId) {
        return this.mHwFullNetworkManager.isCTCardBySlotId(slotId);
    }

    public boolean isCTHybird() {
        return this.mHwFullNetworkManager.isCTHybird();
    }

    public boolean isSettingDefaultData() {
        return this.mHwFullNetworkManager.isSettingDefaultData();
    }

    public boolean isSet4GDoneAfterSimInsert() {
        return this.mHwFullNetworkManager.isSet4GDoneAfterSimInsert();
    }

    public void setDefault4GSlotForMDM() {
        this.mHwFullNetworkManager.setDefault4GSlotForMDM();
    }

    public void makeHwFullNetworkManager(Context c, CommandsInterfaceEx[] ci) {
        this.mHwFullNetworkManager.makeHwFullNetworkManager(c, ci);
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
        this.mHwFullNetworkManager.registerForIccChanged(h, what, obj);
    }

    public void unregisterForIccChanged(Handler h) {
        this.mHwFullNetworkManager.unregisterForIccChanged(h);
    }

    public void initUiccCard(UiccCardExt uiccCard, IccCardStatusExt status, Integer index) {
        this.mHwFullNetworkManager.initUiccCard(uiccCard, status, index);
    }

    public void updateUiccCard(UiccCardExt uiccCard, IccCardStatusExt status, Integer index) {
        this.mHwFullNetworkManager.updateUiccCard(uiccCard, status, index);
    }

    public void onGetIccCardStatusDone(Object ar, Integer index) {
        this.mHwFullNetworkManager.onGetIccCardStatusDone(ar, index);
    }

    public void setPreferredNetworkType(int networkType, int phoneId, Message response) {
        this.mHwFullNetworkManager.setPreferredNetworkType(networkType, phoneId, response);
    }

    public String getMasterPassword() {
        return this.mHwFullNetworkManager.getMasterPassword();
    }

    public boolean isCMCCDsdxEnable() {
        return this.mHwFullNetworkManager.isCMCCDsdxEnable();
    }
}
