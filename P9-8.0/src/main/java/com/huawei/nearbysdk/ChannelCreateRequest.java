package com.huawei.nearbysdk;

import com.huawei.nearbysdk.NearbyConfig.BusinessTypeEnum;
import java.io.IOException;

public interface ChannelCreateRequest {
    NearbySocket accept() throws IOException;

    NearbySocket acceptTimer(int i) throws IOException;

    boolean equals(NearbyDevice nearbyDevice);

    int getBusinessId();

    BusinessTypeEnum getBusinessType();

    NearbyDevice getRemoteNearbyDevice();

    int getSecurityType();

    String getTag();

    void reject();
}
