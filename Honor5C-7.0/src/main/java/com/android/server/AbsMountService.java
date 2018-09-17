package com.android.server;

import android.os.storage.IMountService.Stub;
import java.util.ArrayList;

public abstract class AbsMountService extends Stub {
    protected boolean umsStabilityErrorCorrection(String umsPath, boolean umsAvailable, String path, String method) {
        return true;
    }

    protected ArrayList<String> getShareableVolumes() {
        return new ArrayList();
    }

    protected String getUmsStoragePath() {
        return null;
    }
}
