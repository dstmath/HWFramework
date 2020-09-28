package com.android.internal.telephony.uicc;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import java.util.concurrent.TimeUnit;

public class InstallCarrierAppTrampolineActivity extends Activity {
    private static final String BUNDLE_KEY_PACKAGE_NAME = "package_name";
    private static final String CARRIER_NAME = "carrier_name";
    private static final int DOWNLOAD_RESULT = 2;
    private static final int INSTALL_CARRIER_APP_DIALOG_REQUEST = 1;
    private static final String LOG_TAG = "CarrierAppInstall";
    private String mPackageName;

    public static Intent get(Context context, String packageName) {
        Intent intent = new Intent(context, InstallCarrierAppTrampolineActivity.class);
        intent.putExtra(BUNDLE_KEY_PACKAGE_NAME, packageName);
        return intent;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            this.mPackageName = intent.getStringExtra(BUNDLE_KEY_PACKAGE_NAME);
        }
        if (savedInstanceState == null) {
            long sleepTimeMillis = Settings.Global.getLong(getContentResolver(), "install_carrier_app_notification_sleep_millis", TimeUnit.HOURS.toMillis(24));
            Log.d(LOG_TAG, "Sleeping carrier app install notification for : " + sleepTimeMillis + " millis");
            InstallCarrierAppUtils.showNotificationIfNotInstalledDelayed(this, this.mPackageName, sleepTimeMillis);
        }
        Intent showDialogIntent = new Intent();
        showDialogIntent.setComponent(ComponentName.unflattenFromString(Resources.getSystem().getString(17039803)));
        String appName = InstallCarrierAppUtils.getAppNameFromPackageName(this, this.mPackageName);
        if (!TextUtils.isEmpty(appName)) {
            showDialogIntent.putExtra(CARRIER_NAME, appName);
        }
        if (showDialogIntent.resolveActivity(getPackageManager()) == null) {
            Log.d(LOG_TAG, "Could not resolve activity for installing the carrier app");
            finishNoAnimation();
            return;
        }
        try {
            startActivityForResult(showDialogIntent, 1);
        } catch (ActivityNotFoundException e) {
            Log.e(LOG_TAG, "Could not find activity for installing the carrier app");
            finishNoAnimation();
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 2) {
                startActivity(InstallCarrierAppUtils.getPlayStoreIntent(this.mPackageName));
            }
            finishNoAnimation();
        }
    }

    private void finishNoAnimation() {
        finish();
        overridePendingTransition(0, 0);
    }
}
