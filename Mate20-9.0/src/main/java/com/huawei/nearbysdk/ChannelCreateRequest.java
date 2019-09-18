package com.huawei.nearbysdk;

import com.huawei.nearbysdk.NearbyConfig;
import java.io.IOException;

public interface ChannelCreateRequest {
    NearbySocket accept() throws IOException;

    NearbySocket acceptTimer(int i) throws IOException;

    boolean equals(NearbyDevice nearbyDevice);

    int getBusinessId();

    NearbyConfig.BusinessTypeEnum getBusinessType();

    NearbyDevice getRemoteNearbyDevice();

    int getSecurityType();

    String getTag();

    void reject();
}
