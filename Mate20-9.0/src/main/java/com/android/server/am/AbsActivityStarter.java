package com.android.server.am;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;

public class AbsActivityStarter {
    /* access modifiers changed from: protected */
    public boolean startingCustomActivity(boolean abort, Intent intent, ActivityInfo aInfo) {
        return abort;
    }

    /* access modifiers changed from: protected */
    public boolean isInSkipCancelResultList(String clsName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean standardizeHomeIntent(ResolveInfo rInfo, Intent intent) {
        return false;
    }
}
