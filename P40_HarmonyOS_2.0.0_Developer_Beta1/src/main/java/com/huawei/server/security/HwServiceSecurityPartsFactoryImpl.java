package com.huawei.server.security;

import com.android.server.security.securityprofile.ISecurityProfileController;
import com.android.server.security.trustspace.ITrustSpaceController;
import com.huawei.server.security.securityprofile.SecurityProfileControllerImpl;
import com.huawei.server.security.trustspace.TrustSpaceControllerImpl;

public class HwServiceSecurityPartsFactoryImpl extends HwServiceSecurityPartsFactory {
    public ITrustSpaceController getTrustSpaceController() {
        return new TrustSpaceControllerImpl();
    }

    public ISecurityProfileController getSecurityProfileController() {
        return new SecurityProfileControllerImpl();
    }
}
