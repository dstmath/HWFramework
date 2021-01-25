package com.huawei.android.content.pm;

import android.content.pm.PackageManager;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class OnPermissionsChangedListenerEx {
    private OnPermissionsChangedListenerBridge mBridge = new OnPermissionsChangedListenerBridge();

    public OnPermissionsChangedListenerEx() {
        this.mBridge.setOnPermissionsChangedListenerEx(this);
    }

    public PackageManager.OnPermissionsChangedListener getListenerBridge() {
        return this.mBridge;
    }

    public void onPermissionsChanged(int uid) {
    }
}
