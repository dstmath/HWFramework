package com.android.internal.app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.SuspendDialogInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Slog;
import com.android.internal.R;
import com.android.internal.app.AlertController;

public class SuspendedAppActivity extends AlertActivity implements DialogInterface.OnClickListener {
    public static final String EXTRA_DIALOG_INFO = "com.android.internal.app.extra.DIALOG_INFO";
    public static final String EXTRA_SUSPENDED_PACKAGE = "com.android.internal.app.extra.SUSPENDED_PACKAGE";
    public static final String EXTRA_SUSPENDING_PACKAGE = "com.android.internal.app.extra.SUSPENDING_PACKAGE";
    private static final String PACKAGE_NAME = "com.android.internal.app";
    private static final String TAG = SuspendedAppActivity.class.getSimpleName();
    private Intent mMoreDetailsIntent;
    private PackageManager mPm;
    private SuspendDialogInfo mSuppliedDialogInfo;
    private Resources mSuspendingAppResources;
    private int mUserId;

    private CharSequence getAppLabel(String packageName) {
        try {
            return this.mPm.getApplicationInfoAsUser(packageName, 0, this.mUserId).loadLabel(this.mPm);
        } catch (PackageManager.NameNotFoundException ne) {
            String str = TAG;
            Slog.e(str, "Package " + packageName + " not found", ne);
            return packageName;
        }
    }

    private Intent getMoreDetailsActivity(String suspendingPackage, String suspendedPackage, int userId) {
        Intent moreDetailsIntent = new Intent(Intent.ACTION_SHOW_SUSPENDED_APP_DETAILS).setPackage(suspendingPackage);
        ResolveInfo resolvedInfo = this.mPm.resolveActivityAsUser(moreDetailsIntent, 0, userId);
        if (resolvedInfo == null || resolvedInfo.activityInfo == null || !Manifest.permission.SEND_SHOW_SUSPENDED_APP_DETAILS.equals(resolvedInfo.activityInfo.permission)) {
            return null;
        }
        moreDetailsIntent.putExtra("android.intent.extra.PACKAGE_NAME", suspendedPackage).setFlags(335544320);
        return moreDetailsIntent;
    }

    private Drawable resolveIcon() {
        int iconId;
        Resources resources;
        SuspendDialogInfo suspendDialogInfo = this.mSuppliedDialogInfo;
        if (suspendDialogInfo != null) {
            iconId = suspendDialogInfo.getIconResId();
        } else {
            iconId = 0;
        }
        if (iconId == 0 || (resources = this.mSuspendingAppResources) == null) {
            return null;
        }
        try {
            return resources.getDrawable(iconId, getTheme());
        } catch (Resources.NotFoundException e) {
            String str = TAG;
            Slog.e(str, "Could not resolve drawable resource id " + iconId);
            return null;
        }
    }

    private String resolveTitle() {
        int titleId;
        Resources resources;
        SuspendDialogInfo suspendDialogInfo = this.mSuppliedDialogInfo;
        if (suspendDialogInfo != null) {
            titleId = suspendDialogInfo.getTitleResId();
        } else {
            titleId = 0;
        }
        if (!(titleId == 0 || (resources = this.mSuspendingAppResources) == null)) {
            try {
                return resources.getString(titleId);
            } catch (Resources.NotFoundException e) {
                String str = TAG;
                Slog.e(str, "Could not resolve string resource id " + titleId);
            }
        }
        return getString(R.string.app_suspended_title);
    }

    private String resolveDialogMessage(String suspendingPkg, String suspendedPkg) {
        Resources resources;
        CharSequence suspendedAppLabel = getAppLabel(suspendedPkg);
        SuspendDialogInfo suspendDialogInfo = this.mSuppliedDialogInfo;
        if (suspendDialogInfo != null) {
            int messageId = suspendDialogInfo.getDialogMessageResId();
            String message = this.mSuppliedDialogInfo.getDialogMessage();
            if (messageId != 0 && (resources = this.mSuspendingAppResources) != null) {
                try {
                    return resources.getString(messageId, suspendedAppLabel);
                } catch (Resources.NotFoundException e) {
                    String str = TAG;
                    Slog.e(str, "Could not resolve string resource id " + messageId);
                }
            } else if (message != null) {
                return String.format(getResources().getConfiguration().getLocales().get(0), message, suspendedAppLabel);
            }
        }
        return getString(R.string.app_suspended_default_message, suspendedAppLabel, getAppLabel(suspendingPkg));
    }

    private String resolveNeutralButtonText() {
        Resources resources;
        SuspendDialogInfo suspendDialogInfo = this.mSuppliedDialogInfo;
        int buttonTextId = suspendDialogInfo != null ? suspendDialogInfo.getNeutralButtonTextResId() : 0;
        if (!(buttonTextId == 0 || (resources = this.mSuspendingAppResources) == null)) {
            try {
                return resources.getString(buttonTextId);
            } catch (Resources.NotFoundException e) {
                String str = TAG;
                Slog.e(str, "Could not resolve string resource id " + buttonTextId);
            }
        }
        return getString(R.string.app_suspended_more_details);
    }

    /* JADX INFO: Multiple debug info for r3v4 com.android.internal.app.AlertController$AlertParams: [D('ne' android.content.pm.PackageManager$NameNotFoundException), D('ap' com.android.internal.app.AlertController$AlertParams)] */
    @Override // com.android.internal.app.AlertActivity, android.app.Activity
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPm = getPackageManager();
        getWindow().setType(2008);
        Intent intent = getIntent();
        this.mUserId = intent.getIntExtra(Intent.EXTRA_USER_ID, -1);
        if (this.mUserId < 0) {
            String str = TAG;
            Slog.wtf(str, "Invalid user: " + this.mUserId);
            finish();
            return;
        }
        String suspendedPackage = intent.getStringExtra(EXTRA_SUSPENDED_PACKAGE);
        String suspendingPackage = intent.getStringExtra(EXTRA_SUSPENDING_PACKAGE);
        this.mSuppliedDialogInfo = (SuspendDialogInfo) intent.getParcelableExtra(EXTRA_DIALOG_INFO);
        if (this.mSuppliedDialogInfo != null) {
            try {
                this.mSuspendingAppResources = this.mPm.getResourcesForApplicationAsUser(suspendingPackage, this.mUserId);
            } catch (PackageManager.NameNotFoundException ne) {
                String str2 = TAG;
                Slog.e(str2, "Could not find resources for " + suspendingPackage, ne);
            }
        }
        AlertController.AlertParams ap = this.mAlertParams;
        ap.mIcon = resolveIcon();
        ap.mTitle = resolveTitle();
        ap.mMessage = resolveDialogMessage(suspendingPackage, suspendedPackage);
        ap.mPositiveButtonText = getString(17039370);
        this.mMoreDetailsIntent = getMoreDetailsActivity(suspendingPackage, suspendedPackage, this.mUserId);
        if (this.mMoreDetailsIntent != null) {
            ap.mNeutralButtonText = resolveNeutralButtonText();
        }
        ap.mNeutralButtonListener = this;
        ap.mPositiveButtonListener = this;
        setupAlert();
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialog, int which) {
        if (which == -3) {
            startActivityAsUser(this.mMoreDetailsIntent, UserHandle.of(this.mUserId));
            Slog.i(TAG, "Started more details activity");
        }
        finish();
    }

    public static Intent createSuspendedAppInterceptIntent(String suspendedPackage, String suspendingPackage, SuspendDialogInfo dialogInfo, int userId) {
        return new Intent().setClassName("android", SuspendedAppActivity.class.getName()).putExtra(EXTRA_SUSPENDED_PACKAGE, suspendedPackage).putExtra(EXTRA_DIALOG_INFO, dialogInfo).putExtra(EXTRA_SUSPENDING_PACKAGE, suspendingPackage).putExtra(Intent.EXTRA_USER_ID, userId).setFlags(276824064);
    }
}
