package com.android.server.content;

import android.content.Intent;

public abstract class AbsSyncManager {
    public boolean checkShouldFilterSync(Intent intent, int userId) {
        return false;
    }
}
