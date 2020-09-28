package com.android.internal.telephony.uicc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

public class CarrierAppInstallReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "CarrierAppInstall";

    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction())) {
            Log.d(LOG_TAG, "Received package install intent");
            String intentPackageName = intent.getData().getSchemeSpecificPart();
            if (TextUtils.isEmpty(intentPackageName)) {
                Log.w(LOG_TAG, "Package is empty, ignoring");
                return;
            }
            InstallCarrierAppUtils.hideNotification(context, intentPackageName);
            if (!InstallCarrierAppUtils.isPackageInstallNotificationActive(context)) {
                InstallCarrierAppUtils.unregisterPackageInstallReceiver(context);
            }
        }
    }
}
