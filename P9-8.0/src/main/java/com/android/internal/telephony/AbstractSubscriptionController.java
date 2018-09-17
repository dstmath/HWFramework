package com.android.internal.telephony;

import android.net.NetworkRequest;
import com.android.internal.telephony.ISub.Stub;

public abstract class AbstractSubscriptionController extends Stub {
    SubscriptionControllerReference mReference = HwTelephonyFactory.getHwUiccManager().createHwSubscriptionControllerReference(this);

    public interface OnDemandDdsLockNotifier {
        void notifyOnDemandDdsLockGranted(NetworkRequest networkRequest);
    }

    public interface SubscriptionControllerReference {
        void activateSubId(int i);

        void deactivateSubId(int i);

        int getCurrentDds();

        int getHwPhoneId(int i);

        int getHwSlotId(int i);

        int[] getHwSubId(int i);

        int getNwMode(int i);

        int getOnDemandDataSubId();

        int getSubIdFromNetworkRequest(NetworkRequest networkRequest);

        int getSubState(int i);

        boolean isSMSPromptEnabled();

        boolean isVoicePromptEnabled();

        void notifyOnDemandDataSubIdChanged(NetworkRequest networkRequest);

        void registerForOnDemandDdsLockNotification(int i, OnDemandDdsLockNotifier onDemandDdsLockNotifier);

        void setDataSubId(int i);

        void setDefaultDataSubIdHw(int i);

        void setNwMode(int i, int i2);

        void setSMSPromptEnabled(boolean z);

        int setSubState(int i, int i2);

        void setVoicePromptEnabled(boolean z);

        void startOnDemandDataSubscriptionRequest(NetworkRequest networkRequest);

        void stopOnDemandDataSubscriptionRequest(NetworkRequest networkRequest);

        boolean supportHwDualDataSwitch();

        int updateClatForMobile(int i);
    }

    public int getHwSlotId(int subId) {
        return this.mReference.getHwSlotId(subId);
    }

    public int[] getHwSubId(int slotIdx) {
        return this.mReference.getHwSubId(slotIdx);
    }

    public int getHwPhoneId(int subId) {
        return this.mReference.getHwPhoneId(subId);
    }

    public boolean isSMSPromptEnabled() {
        return this.mReference.isSMSPromptEnabled();
    }

    public void setSMSPromptEnabled(boolean enabled) {
        this.mReference.setSMSPromptEnabled(enabled);
    }

    public void activateSubId(int subId) {
        this.mReference.activateSubId(subId);
    }

    public void deactivateSubId(int subId) {
        this.mReference.deactivateSubId(subId);
    }

    public void setNwMode(int subId, int nwMode) {
        this.mReference.setNwMode(subId, nwMode);
    }

    public int getNwMode(int subId) {
        return this.mReference.getNwMode(subId);
    }

    public int setSubState(int subId, int subStatus) {
        return this.mReference.setSubState(subId, subStatus);
    }

    public int getSubState(int subId) {
        return this.mReference.getSubState(subId);
    }

    public boolean isVoicePromptEnabled() {
        return this.mReference.isVoicePromptEnabled();
    }

    public void setVoicePromptEnabled(boolean enabled) {
        this.mReference.setVoicePromptEnabled(enabled);
    }

    public void startOnDemandDataSubscriptionRequest(NetworkRequest n) {
        this.mReference.startOnDemandDataSubscriptionRequest(n);
    }

    public void stopOnDemandDataSubscriptionRequest(NetworkRequest n) {
        this.mReference.stopOnDemandDataSubscriptionRequest(n);
    }

    public int getCurrentDds() {
        return this.mReference.getCurrentDds();
    }

    public void setDataSubId(int subId) {
        this.mReference.setDataSubId(subId);
    }

    public int getOnDemandDataSubId() {
        return this.mReference.getOnDemandDataSubId();
    }

    public void registerForOnDemandDdsLockNotification(int clientSubId, OnDemandDdsLockNotifier callback) {
        this.mReference.registerForOnDemandDdsLockNotification(clientSubId, callback);
    }

    public void notifyOnDemandDataSubIdChanged(NetworkRequest n) {
        this.mReference.notifyOnDemandDataSubIdChanged(n);
    }

    public boolean supportHwDualDataSwitch() {
        return this.mReference.supportHwDualDataSwitch();
    }

    public void setDefaultDataSubIdHw(int subId) {
        this.mReference.setDefaultDataSubIdHw(subId);
    }

    public int getSubIdFromNetworkRequest(NetworkRequest n) {
        return this.mReference.getSubIdFromNetworkRequest(n);
    }

    public int getPreferredDataSubscription() {
        return getOnDemandDataSubId();
    }

    public int updateClatForMobile(int subId) {
        return this.mReference.updateClatForMobile(subId);
    }
}
