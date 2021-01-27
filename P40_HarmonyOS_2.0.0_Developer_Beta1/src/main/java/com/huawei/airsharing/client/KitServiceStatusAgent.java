package com.huawei.airsharing.client;

import com.huawei.airsharing.api.IKitServiceStatusListener;
import com.huawei.airsharing.client.IAidlKitServiceStatusListener;

public class KitServiceStatusAgent extends IAidlKitServiceStatusListener.Stub {
    private IKitServiceStatusListener mListener;

    public KitServiceStatusAgent(IKitServiceStatusListener listener) {
        this.mListener = listener;
    }

    @Override // com.huawei.airsharing.client.IAidlKitServiceStatusListener
    public void onKitEvent(int eventId) {
        this.mListener.onKitEvent(eventId);
    }
}
