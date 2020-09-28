package com.android.internal.telephony.fullnetwork;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import com.android.internal.telephony.HwAESCryptoUtil;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.PhoneFactoryExt;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.uicc.IccCardStatusExt;
import com.huawei.internal.telephony.uicc.UiccCardExt;

public class DefaultHwFullNetworkManager extends Handler {
    private static final byte[] C1 = {98, 94, -52, 117, -82, 28, -44, 66, 28, 61, -110, -119, -75, 70, 2, 85};
    private static final byte[] C2 = {-89, 82, 3, 85, -88, -104, 57, -10, -103, 108, -88, 122, -38, -12, -55, -2};
    private static final byte[] C3 = {-9, -86, 60, -113, 122, -7, -55, 69, 23, 119, 87, -83, 89, -1, -113, 29};
    private static final String MASTER_PASSWORD = HwAESCryptoUtil.getKey(C1, C2, C3);
    private static final String TAG = "DefaultHwFullNetworkManager";
    private static DefaultHwFullNetworkManager sInstance = new DefaultHwFullNetworkManager();

    public boolean isFullnetworkSupported() {
        return false;
    }

    public String getFullIccid(int slotId) {
        return null;
    }

    public int getDefaultMainSlotByIccId(int temSub) {
        return temSub;
    }

    public boolean isCMCCCardBySlotId(int slotId) {
        return false;
    }

    public boolean isCMCCHybird() {
        return false;
    }

    public void saveMainCardIccId(String iccId) {
    }

    public void setMainSlot(int slotId, Message responseMsg) {
        int subId = SubscriptionManagerEx.getSubIdUsingSlotId(slotId);
        RlogEx.d(TAG, "setMainSlot: subId = " + subId + ", slotId = " + slotId);
        SubscriptionControllerEx.getInstance().setDefaultDataSubId(subId);
        PhoneExt phone = PhoneFactoryExt.getPhone(0);
        if (phone != null) {
            Context context = phone.getContext();
            ContentResolver contentResolver = context != null ? context.getContentResolver() : null;
            if (contentResolver != null) {
                RlogEx.d(TAG, "setMainSlot: set switch_dual_card_slots to " + slotId);
                Settings.System.putInt(contentResolver, "switch_dual_card_slots", slotId);
            }
        }
        if (responseMsg != null && responseMsg.getTarget() != null) {
            AsyncResultEx.forMessage(responseMsg, (Object) null, (Throwable) null);
            try {
                responseMsg.sendToTarget();
            } catch (IllegalStateException e) {
                RlogEx.e(TAG, "response is sent, don't send again!!");
            }
        }
    }

    public static DefaultHwFullNetworkManager getInstance() {
        return sInstance;
    }

    public boolean isCMCCDsdxDisable() {
        return false;
    }

    public int getBalongSimSlot() {
        return 0;
    }

    public void setLteServiceAbilityForQCOM(int slotId, int ability, int lteOnMappingMode) {
    }

    public void setServiceAbilityForQCOM(int slotId, int type, int ability, int servicOnMappingMode) {
    }

    public void setWaitingSwitchBalongSlot(boolean isSetResult) {
    }

    public boolean isSwitchDualCardSlotsEnabled() {
        return false;
    }

    public boolean getWaitingSwitchBalongSlot() {
        return false;
    }

    public boolean isUserPref4GSlot(int slotId) {
        return false;
    }

    public boolean isRestartRildProgress() {
        return false;
    }

    public boolean get4GSlotInSwitchProgress() {
        return false;
    }

    public int getUserSwitchDualCardSlots() {
        return 0;
    }

    public boolean get4GSlotInProgress() {
        return false;
    }

    public void resetUiccSubscriptionResultFlag(int slotId) {
    }

    public boolean isCTCardBySlotId(int slotId) {
        return false;
    }

    public boolean isCTHybird() {
        return false;
    }

    public boolean isSettingDefaultData() {
        return false;
    }

    public boolean isSet4GDoneAfterSimInsert() {
        return false;
    }

    public void setDefault4GSlotForMDM() {
    }

    public void makeHwFullNetworkManager(Context c, CommandsInterfaceEx[] ci) {
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
    }

    public void unregisterForIccChanged(Handler h) {
    }

    public void initUiccCard(UiccCardExt uiccCard, IccCardStatusExt status, Integer index) {
    }

    public void updateUiccCard(UiccCardExt uiccCard, IccCardStatusExt status, Integer index) {
    }

    public void onGetIccCardStatusDone(Object ar, Integer index) {
    }

    public void setPreferredNetworkType(int networkType, int phoneId, Message response) {
    }

    public String getMasterPassword() {
        return MASTER_PASSWORD;
    }

    public boolean isCMCCDsdxEnable() {
        return false;
    }
}
