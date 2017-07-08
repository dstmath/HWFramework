package com.android.server.am;

import android.content.Intent;

public abstract class AbsActivityRecord {
    protected void initSplitMode(Intent intent) {
    }

    protected boolean isSplitMode() {
        return false;
    }
}
