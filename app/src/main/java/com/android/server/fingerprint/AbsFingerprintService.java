package com.android.server.fingerprint;

import android.content.Context;
import com.android.server.SystemService;

public abstract class AbsFingerprintService extends SystemService {
    public AbsFingerprintService(Context context) {
        super(context);
    }

    public void updateFingerprints(int userId) {
    }

    public boolean shouldAuthBothSpaceFingerprints(String opPackageName, int flags) {
        return true;
    }

    public int removeUserData(int groupId, byte[] path) {
        return 0;
    }
}
