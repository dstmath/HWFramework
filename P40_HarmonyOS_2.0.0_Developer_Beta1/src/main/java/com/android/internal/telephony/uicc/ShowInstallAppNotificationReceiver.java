package com.android.internal.telephony.uicc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShowInstallAppNotificationReceiver extends BroadcastReceiver {
    private static final String EXTRA_PACKAGE_NAME = "package_name";

    public static Intent get(Context context, String pkgName) {
        Intent intent = new Intent(context, ShowInstallAppNotificationReceiver.class);
        intent.putExtra(EXTRA_PACKAGE_NAME, pkgName);
        return intent;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getStringExtra(EXTRA_PACKAGE_NAME) != null) {
            String pkgName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
            if (!UiccProfile.isPackageInstalled(context, pkgName)) {
                InstallCarrierAppUtils.showNotification(context, pkgName);
                InstallCarrierAppUtils.registerPackageInstallReceiver(context);
            }
        }
    }
}
