package com.android.server.am;

import android.content.Intent;

public class AbsActivityStartInterceptor {
    protected boolean interceptStartActivityIfNeed(Intent intet) {
        return false;
    }

    protected void setSourceRecord(ActivityRecord sourceRecord) {
    }
}
