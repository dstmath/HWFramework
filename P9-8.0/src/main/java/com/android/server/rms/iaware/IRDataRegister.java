package com.android.server.rms.iaware;

import android.rms.iaware.AwareConstant.FeatureType;
import android.rms.iaware.AwareConstant.ResourceType;

public interface IRDataRegister {
    boolean subscribeData(ResourceType resourceType, FeatureType featureType);

    boolean unSubscribeData(ResourceType resourceType, FeatureType featureType);
}
