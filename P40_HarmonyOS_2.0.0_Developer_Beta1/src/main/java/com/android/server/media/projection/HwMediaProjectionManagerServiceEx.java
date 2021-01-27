package com.android.server.media.projection;

import android.content.Context;
import com.android.server.HwServiceFactory;
import com.android.server.security.securityprofile.ISecurityProfileController;
import com.huawei.server.media.projection.IHwMediaProjectionManagerServiceEx;

public final class HwMediaProjectionManagerServiceEx implements IHwMediaProjectionManagerServiceEx {
    private static final String TAG = "HwMediaProjectionManagerServiceEx";

    public HwMediaProjectionManagerServiceEx(IHwMediaProjectionManagerServiceInner mpms, Context context) {
    }

    public boolean shouldPreventMediaProjection(int callingUid) {
        ISecurityProfileController spc = HwServiceFactory.getSecurityProfileController();
        if (spc == null || !spc.shouldPreventMediaProjection(callingUid)) {
            return false;
        }
        return true;
    }
}
