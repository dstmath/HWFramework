package com.android.internal.telephony;

public interface IHwSubscriptionControllerEx {
    default boolean isSMSPromptEnabled() {
        return false;
    }

    default void setSMSPromptEnabled(boolean isEnabled) {
    }

    default void activateSubId(int subId) {
    }

    default void deactivateSubId(int subId) {
    }

    default void setNwMode(int subId, int nwMode) {
    }

    default int getNwMode(int subId) {
        return -1;
    }

    default int setSubState(int slotId, int subStatus) {
        return 0;
    }

    default int getSubState(int slotId) {
        return 1;
    }

    default boolean isVoicePromptEnabled() {
        return false;
    }

    default void setVoicePromptEnabled(boolean isEnabled) {
    }

    default int getCurrentDds() {
        return PhoneFactory.getTopPrioritySubscriptionId();
    }

    default void setDataSubId(int subId) {
    }

    default int getPreferredDataSubscription() {
        return PhoneFactory.getTopPrioritySubscriptionId();
    }

    default int updateClatForMobile(int subId) {
        return 0;
    }

    default void setSubscriptionPropertyIntoSettingsGlobal(int subId, String propKey, String propValue) {
    }

    default String getSubscriptionPropertyFromSettingsGlobal(int subId, String propKey) {
        return null;
    }

    default void getQcRilHook() {
    }

    default void informDdsToQcril(int ddsPhoneId, int reason) {
    }

    default void checkNeedSetMainSlotByPid(int slotId, int pid) {
    }
}
