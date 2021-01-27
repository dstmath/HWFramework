package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.Intent;

public class AbsActivityStartInterceptor {
    /* access modifiers changed from: protected */
    public boolean interceptStartActivityIfNeed(Intent intet, ActivityOptions activityOptions) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void setSourceRecord(ActivityRecord sourceRecord) {
    }
}
