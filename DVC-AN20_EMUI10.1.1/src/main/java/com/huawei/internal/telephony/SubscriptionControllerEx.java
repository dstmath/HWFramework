package com.huawei.internal.telephony;

import android.telephony.SubscriptionInfo;
import com.android.internal.telephony.SubscriptionController;
import com.huawei.android.telephony.RlogEx;
import com.huawei.annotation.HwSystemApi;
import java.util.List;

@HwSystemApi
public class SubscriptionControllerEx {
    private static final String TAG = "SubscriptionControllerEx";
    private SubscriptionController mSubscriptionController;

    public void setSubscriptionController(SubscriptionController subscriptionController) {
        this.mSubscriptionController = subscriptionController;
    }

    public static SubscriptionControllerEx getInstance() {
        SubscriptionController instance = SubscriptionController.getInstance();
        if (instance == null) {
            return null;
        }
        SubscriptionControllerEx ex = new SubscriptionControllerEx();
        ex.setSubscriptionController(instance);
        return ex;
    }

    public int getDefaultDataSubId() {
        return this.mSubscriptionController.getDefaultDataSubId();
    }

    public int getSubState(int subId) {
        return this.mSubscriptionController.getSubState(subId);
    }

    public List<SubscriptionInfo> getSubInfoUsingSlotIndexPrivileged(int slotIndex) {
        return this.mSubscriptionController.getSubInfoUsingSlotIndexPrivileged(slotIndex);
    }

    public void setDefaultDataSubIdBySlotId(int slotId) {
        int subId = this.mSubscriptionController.getSubIdUsingPhoneId(slotId);
        RlogEx.d(TAG, "setDefaultDataSubIdBySlotId, slotId: " + slotId + ", subId: " + subId);
        this.mSubscriptionController.setDefaultDataSubId(subId);
    }

    public int setSubState(int slotId, int subStatus) {
        return this.mSubscriptionController.setSubState(slotId, subStatus);
    }

    public void setDefaultDataSubId(int subId) {
        this.mSubscriptionController.setDefaultDataSubId(subId);
    }

    public int getPreferredDataSubscription() {
        return this.mSubscriptionController.getPreferredDataSubscription();
    }

    public void informDdsToQcril(int ddsPhoneId, int reason) {
        this.mSubscriptionController.informDdsToQcril(ddsPhoneId, reason);
    }

    public int getCurrentDds() {
        return this.mSubscriptionController.getCurrentDds();
    }

    public void activateSubId(int subId) {
        this.mSubscriptionController.activateSubId(subId);
    }

    public void setDefaultFallbackSubId(int value) {
        this.mSubscriptionController.setDefaultFallbackSubIdHw(value);
    }

    public void resetDefaultFallbackSubId() {
        this.mSubscriptionController.resetDefaultFallbackSubId();
    }

    public void deactivateSubId(int subId) {
        this.mSubscriptionController.deactivateSubId(subId);
    }

    public void setSMSPromptEnabled(boolean isEnabled) {
        this.mSubscriptionController.setSMSPromptEnabled(isEnabled);
    }

    public boolean isVoicePromptEnabled() {
        return this.mSubscriptionController.isVoicePromptEnabled();
    }

    public void setVoicePromptEnabled(boolean isEnabled) {
        this.mSubscriptionController.setVoicePromptEnabled(isEnabled);
    }

    public boolean isSMSPromptEnabled() {
        return this.mSubscriptionController.isSMSPromptEnabled();
    }

    public int getNwMode(int subId) {
        return this.mSubscriptionController.getNwMode(subId);
    }

    public void setNwMode(int subId, int nwMode) {
        this.mSubscriptionController.setNwMode(subId, nwMode);
    }

    public void setDefaultSmsSubId(int subId) {
        this.mSubscriptionController.setDefaultSmsSubId(subId);
    }

    public void setDefaultVoiceSubId(int subId) {
        this.mSubscriptionController.setDefaultVoiceSubId(subId);
    }

    public List<SubscriptionInfo> getActiveSubscriptionInfoList(String callingPackage) {
        return this.mSubscriptionController.getActiveSubscriptionInfoList(callingPackage);
    }

    public SubscriptionInfo getActiveSubscriptionInfo(int subId, String callingPackage) {
        return this.mSubscriptionController.getActiveSubscriptionInfo(subId, callingPackage);
    }
}
