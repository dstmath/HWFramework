package com.huawei.android.hidl;

import android.hidl.manager.V1_0.IServiceNotification;

public class IServiceNotificationHidlAdapter {
    private IServiceNotification mIServiceNotification = new IServiceNotification.Stub() {
        /* class com.huawei.android.hidl.IServiceNotificationHidlAdapter.AnonymousClass1 */

        public void onRegistration(String fqName, String name, boolean isPreexisting) {
            IServiceNotificationHidlAdapter.this.onRegistration(fqName, name, isPreexisting);
        }
    };

    public IServiceNotification getIServiceNotification() {
        return this.mIServiceNotification;
    }

    public void onRegistration(String fqName, String name, boolean isPreexisting) {
    }
}
