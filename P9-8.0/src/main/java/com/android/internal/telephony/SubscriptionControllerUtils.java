package com.android.internal.telephony;

public class SubscriptionControllerUtils {
    public void setDefaultFallbackSubId(SubscriptionController subscriptionController, int value) {
        subscriptionController.setDefaultFallbackSubIdHw(value);
    }

    public void updateAllDataConnectionTrackers(SubscriptionController subscriptionController) {
        subscriptionController.updateAllDataConnectionTrackersHw();
    }
}
