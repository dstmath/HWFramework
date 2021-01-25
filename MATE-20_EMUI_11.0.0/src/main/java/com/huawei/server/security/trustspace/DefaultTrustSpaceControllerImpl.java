package com.huawei.server.security.trustspace;

import com.android.server.security.trustspace.ITrustSpaceController;

public class DefaultTrustSpaceControllerImpl implements ITrustSpaceController {
    public void initTrustSpace() {
    }

    public boolean checkIntent(int type, String calleePackage, int callerUid, int callerPid, String callingPackage, int userId) {
        return false;
    }

    public boolean isIntentProtectedApp(String s) {
        return false;
    }
}
