package com.android.server.content;

import android.content.Intent;

public abstract class AbsSyncManager {
    public boolean isAllow2Sync(int UID) {
        return true;
    }

    public boolean checkShouldFilterSync(Intent intent, int userId) {
        return false;
    }
}
