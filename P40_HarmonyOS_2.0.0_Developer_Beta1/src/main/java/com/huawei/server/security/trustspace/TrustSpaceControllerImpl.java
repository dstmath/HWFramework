package com.huawei.server.security.trustspace;

import android.util.Log;
import com.huawei.android.server.LocalServicesExt;

public class TrustSpaceControllerImpl extends DefaultTrustSpaceControllerImpl {
    private static final String TAG = "TrustSpaceControllerImpl";
    private TrustSpaceManagerInternal mTrustSpaceManagerInternal = ((TrustSpaceManagerInternal) LocalServicesExt.getService(TrustSpaceManagerInternal.class));

    public TrustSpaceControllerImpl() {
        if (this.mTrustSpaceManagerInternal == null) {
            Log.e(TAG, "TrustSpaceManagerInternal not find!");
        }
    }

    public void initTrustSpace() {
        TrustSpaceManagerInternal trustSpaceManagerInternal = this.mTrustSpaceManagerInternal;
        if (trustSpaceManagerInternal != null) {
            trustSpaceManagerInternal.initTrustSpace();
        }
    }

    public boolean checkIntent(int type, String calleePackage, int callerUid, int callerPid, String callerPackage, int userId) {
        TrustSpaceManagerInternal trustSpaceManagerInternal = this.mTrustSpaceManagerInternal;
        if (trustSpaceManagerInternal != null) {
            return trustSpaceManagerInternal.checkIntent(type, calleePackage, callerUid, callerPid, callerPackage, userId);
        }
        return false;
    }

    public boolean isIntentProtectedApp(String packageName) {
        TrustSpaceManagerInternal trustSpaceManagerInternal = this.mTrustSpaceManagerInternal;
        if (trustSpaceManagerInternal != null) {
            return trustSpaceManagerInternal.isIntentProtectedApp(packageName);
        }
        return false;
    }
}
