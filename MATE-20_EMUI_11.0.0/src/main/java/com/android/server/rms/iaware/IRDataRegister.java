package com.android.server.rms.iaware;

import android.rms.iaware.AwareConstant;

public interface IRDataRegister {
    boolean subscribeData(AwareConstant.ResourceType resourceType, AwareConstant.FeatureType featureType);

    boolean unSubscribeData(AwareConstant.ResourceType resourceType, AwareConstant.FeatureType featureType);
}
