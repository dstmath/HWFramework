package com.android.server.am;

import android.content.Intent;
import android.content.pm.ActivityInfo;

public class AbsActivityStarter {
    protected boolean startingCustomActivity(boolean abort, Intent intent, ActivityInfo aInfo) {
        return abort;
    }

    protected boolean isInSkipCancelResultList(String clsName) {
        return false;
    }
}
