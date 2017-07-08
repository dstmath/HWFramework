package com.android.server.am;

import android.app.IApplicationThread;
import android.content.Intent;

public class AbsActivityStackSupervisor {
    protected void recognitionMaliciousApp(IApplicationThread caller, Intent intet) {
    }

    protected boolean isInMultiWinBlackList(String pkgName) {
        return false;
    }
}
