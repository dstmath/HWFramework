package com.huawei.nearbysdk;

import com.huawei.nearbysdk.NearbyAdapter;

public interface NearbyAdapterCallback extends NearbyAdapter.NAdapterGetCallback {
    void onAdapterGet(NearbyAdapter nearbyAdapter);

    void onBinderDied();
}
