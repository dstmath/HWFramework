package com.android.server.am;

import android.app.ResultInfo;
import android.content.Intent;

final class ActivityResult extends ResultInfo {
    final ActivityRecord mFrom;

    public ActivityResult(ActivityRecord from, String resultWho, int requestCode, int resultCode, Intent data) {
        super(resultWho, requestCode, resultCode, data);
        this.mFrom = from;
    }
}
