package com.huawei.nearbysdk;

import com.huawei.nearbysdk.NearbyAdapter;

public interface NearbyAdapterCallback extends NearbyAdapter.NAdapterGetCallback {
    @Override // com.huawei.nearbysdk.NearbyAdapter.NAdapterGetCallback
    void onAdapterGet(NearbyAdapter nearbyAdapter);

    void onBinderDied();
}
