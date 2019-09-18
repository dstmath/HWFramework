package com.android.server.security.trustspace;

import android.util.Slog;
import com.android.server.LocalServices;

public class TrustSpaceControllerImpl implements ITrustSpaceController {
    private static final String TAG = "TrustSpaceControllerImpl";
    private TrustSpaceManagerInternal mTrustSpaceManagerInternal;

    public void initTrustSpace() {
        this.mTrustSpaceManagerInternal = (TrustSpaceManagerInternal) LocalServices.getService(TrustSpaceManagerInternal.class);
        if (this.mTrustSpaceManagerInternal != null) {
            this.mTrustSpaceManagerInternal.initTrustSpace();
        } else {
            Slog.e(TAG, "TrustSpaceManagerInternal not find !");
        }
    }

    public boolean checkIntent(int type, String calleePackage, int callerUid, int callerPid, String callingPackage, int userId) {
        if (this.mTrustSpaceManagerInternal != null) {
            return this.mTrustSpaceManagerInternal.checkIntent(type, calleePackage, callerUid, callerPid, callingPackage, userId);
        }
        return false;
    }

    public boolean isIntentProtectedApp(String pkg) {
        if (this.mTrustSpaceManagerInternal != null) {
            return this.mTrustSpaceManagerInternal.isIntentProtectedApp(pkg);
        }
        return false;
    }
}
