package com.android.internal.telephony;

public interface ISubscriptionControllerInner {
    void activateSubId(int i);

    void deactivateSubId(int i);

    int getCurrentDds();

    int getDefaultDataSubId();

    int getNwMode(int i);

    int getPreferredDataSubscription();

    int getSlotIndex(int i);

    int getSubIdUsingPhoneId(int i);

    int getSubState(int i);

    void informDdsToQcril(int i, int i2);

    boolean isSMSPromptEnabled();

    boolean isVoicePromptEnabled();

    void refreshCachedActiveSubscriptionInfoList();

    void resetDefaultFallbackSubId();

    void setDataSubId(int i);

    void setDefaultFallbackSubIdHw(int i);

    void setNwMode(int i, int i2);

    void setSMSPromptEnabled(boolean z);

    int setSubState(int i, int i2);

    void setVoicePromptEnabled(boolean z);

    void updateAllDataConnectionTrackersHw();
}
