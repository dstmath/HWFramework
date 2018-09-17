package com.android.server.dreams;

import android.content.Context;
import android.os.SystemProperties;

public class HwCustDreamManagerServiceImpl extends HwCustDreamManagerService {
    public static final boolean mChargingAlbumSupported = SystemProperties.getBoolean("ro.config.ChargingAlbum", false);

    public HwCustDreamManagerServiceImpl(Context context) {
        super(context);
    }
}
