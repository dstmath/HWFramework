package com.huawei.android.content.pm;

import android.content.pm.PackageManager;

public class OnPermissionsChangedListenerBridge implements PackageManager.OnPermissionsChangedListener {
    private OnPermissionsChangedListenerEx mListenerEx;

    public void setOnPermissionsChangedListenerEx(OnPermissionsChangedListenerEx listenerEx) {
        this.mListenerEx = listenerEx;
    }

    public void onPermissionsChanged(int uid) {
        OnPermissionsChangedListenerEx onPermissionsChangedListenerEx = this.mListenerEx;
        if (onPermissionsChangedListenerEx != null) {
            onPermissionsChangedListenerEx.onPermissionsChanged(uid);
        }
    }
}
