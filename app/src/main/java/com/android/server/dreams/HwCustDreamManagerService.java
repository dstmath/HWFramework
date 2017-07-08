package com.android.server.dreams;

import android.content.ComponentName;
import android.content.Context;

public class HwCustDreamManagerService {
    public HwCustDreamManagerService(Context context) {
    }

    public ComponentName[] getChargingAlbumForUser(int userId) {
        return null;
    }

    public boolean isCoverOpened() {
        return true;
    }

    public boolean isChargingAlbumEnabled() {
        return false;
    }

    public void systemReady() {
    }
}
