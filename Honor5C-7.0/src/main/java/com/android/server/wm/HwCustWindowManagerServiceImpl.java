package com.android.server.wm;

import com.android.server.dreams.HwCustDreamManagerServiceImpl;

public class HwCustWindowManagerServiceImpl extends HwCustWindowManagerService {
    public boolean isChargingAlbumType(int type) {
        if (!HwCustDreamManagerServiceImpl.mChargingAlbumSupported) {
            return super.isChargingAlbumType(type);
        }
        return type == 2102;
    }
}
