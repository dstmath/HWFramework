package com.android.internal.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.internal.app.AlertController;

public class HarmfulAppWarningActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private static final String EXTRA_HARMFUL_APP_WARNING = "harmful_app_warning";
    private static final String TAG = HarmfulAppWarningActivity.class.getSimpleName();
    private String mHarmfulAppWarning;
    private String mPackageName;
    private IntentSender mTarget;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        this.mPackageName = intent.getStringExtra("android.intent.extra.PACKAGE_NAME");
        this.mTarget = (IntentSender) intent.getParcelableExtra("android.intent.extra.INTENT");
        this.mHarmfulAppWarning = intent.getStringExtra(EXTRA_HARMFUL_APP_WARNING);
        if (this.mPackageName == null || this.mTarget == null || this.mHarmfulAppWarning == null) {
            String str = TAG;
            Log.wtf(str, "Invalid intent: " + intent.toString());
            finish();
        }
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(this.mPackageName, 0);
            AlertController.AlertParams p = this.mAlertParams;
            p.mTitle = getString(17040163);
            p.mView = createView(applicationInfo);
            p.mPositiveButtonText = getString(17040164);
            p.mPositiveButtonListener = this;
            p.mNegativeButtonText = getString(17040162);
            p.mNegativeButtonListener = this;
            this.mAlert.installContent(this.mAlertParams);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not show warning because package does not exist ", e);
            finish();
        }
    }

    private View createView(ApplicationInfo applicationInfo) {
        View view = getLayoutInflater().inflate(17367152, null);
        ((TextView) view.findViewById(16908739)).setText(applicationInfo.loadSafeLabel(getPackageManager()));
        ((TextView) view.findViewById(16908299)).setText(this.mHarmfulAppWarning);
        return view;
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                getPackageManager().setHarmfulAppWarning(this.mPackageName, null);
                try {
                    startIntentSenderForResult((IntentSender) getIntent().getParcelableExtra("android.intent.extra.INTENT"), -1, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Error while starting intent sender", e);
                }
                EventLogTags.writeHarmfulAppWarningLaunchAnyway(this.mPackageName);
                finish();
                return;
            case -1:
                getPackageManager().deletePackage(this.mPackageName, null, 0);
                EventLogTags.writeHarmfulAppWarningUninstall(this.mPackageName);
                finish();
                return;
            default:
                return;
        }
    }

    public static Intent createHarmfulAppWarningIntent(Context context, String targetPackageName, IntentSender target, CharSequence harmfulAppWarning) {
        Intent intent = new Intent();
        intent.setClass(context, HarmfulAppWarningActivity.class);
        intent.putExtra("android.intent.extra.PACKAGE_NAME", targetPackageName);
        intent.putExtra("android.intent.extra.INTENT", target);
        intent.putExtra(EXTRA_HARMFUL_APP_WARNING, harmfulAppWarning);
        return intent;
    }
}
