package com.huawei.internal.telephony;

import com.android.internal.telephony.ClientWakelockTracker;

public class ClientWakelockTrackerEx {
    ClientWakelockTracker mClientWakelockTracker;

    public ClientWakelockTrackerEx() {
        this.mClientWakelockTracker = null;
        this.mClientWakelockTracker = new ClientWakelockTracker();
    }

    public boolean isClientActive(String clientId) {
        return this.mClientWakelockTracker.isClientActive(clientId);
    }

    public void startTracking(String clientId, int requestId, int token, int numRequestsInQueue) {
        this.mClientWakelockTracker.startTracking(clientId, requestId, token, numRequestsInQueue);
    }

    public void stopTracking(String clientId, int requestId, int token, int numRequestsInQueue) {
        this.mClientWakelockTracker.stopTracking(clientId, requestId, token, numRequestsInQueue);
    }

    public void stopTrackingAll() {
        this.mClientWakelockTracker.stopTrackingAll();
    }
}
