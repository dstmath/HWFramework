package com.android.server.policy;

import android.content.Intent;

public abstract class PermissionPolicyInternal {

    public interface OnInitializedCallback {
        void onInitialized(int i);
    }

    public abstract boolean checkStartActivity(Intent intent, int i, String str);

    public abstract boolean isInitialized(int i);

    public abstract void setOnInitializedCallback(OnInitializedCallback onInitializedCallback);
}
