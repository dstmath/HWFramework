package com.android.internal.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.app.AlertController;

public class ConfirmUserCreationActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private static final String TAG = "CreateUser";
    private String mAccountName;
    private PersistableBundle mAccountOptions;
    private String mAccountType;
    private boolean mCanProceed;
    private UserManager mUserManager;
    private String mUserName;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        if (intent == null || intent.getAction() == null) {
            Log.d(TAG, "null intent");
            finish();
            return;
        }
        this.mUserName = intent.getStringExtra("android.os.extra.USER_NAME");
        this.mAccountName = intent.getStringExtra("android.os.extra.USER_ACCOUNT_NAME");
        this.mAccountType = intent.getStringExtra("android.os.extra.USER_ACCOUNT_TYPE");
        this.mAccountOptions = (PersistableBundle) intent.getParcelableExtra("android.os.extra.USER_ACCOUNT_OPTIONS");
        this.mUserManager = (UserManager) getSystemService(UserManager.class);
        String message = checkUserCreationRequirements();
        if (message == null) {
            finish();
            return;
        }
        AlertController.AlertParams ap = this.mAlertParams;
        ap.mMessage = message;
        ap.mPositiveButtonText = getString(17039370);
        ap.mPositiveButtonListener = this;
        if (this.mCanProceed) {
            ap.mNegativeButtonText = getString(17039360);
            ap.mNegativeButtonListener = this;
        }
        setupAlert();
    }

    private String checkUserCreationRequirements() {
        String message;
        String callingPackage = getCallingPackage();
        if (callingPackage != null) {
            try {
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(callingPackage, 0);
                boolean cantCreateUser = this.mUserManager.hasUserRestriction("no_add_user") || !this.mUserManager.isAdminUser();
                boolean cantCreateAnyMoreUsers = !this.mUserManager.canAddMoreUsers();
                boolean accountExists = (this.mAccountName == null || this.mAccountType == null || (!AccountManager.get(this).someUserHasAccount(new Account(this.mAccountName, this.mAccountType)) && !this.mUserManager.someUserHasSeedAccount(this.mAccountName, this.mAccountType))) ? false : true;
                this.mCanProceed = true;
                String appName = appInfo.loadLabel(getPackageManager()).toString();
                if (cantCreateUser) {
                    setResult(1);
                    return null;
                } else if (cantCreateAnyMoreUsers) {
                    setResult(2);
                    return null;
                } else {
                    if (accountExists) {
                        message = getString(17041307, new Object[]{appName, this.mAccountName});
                    } else {
                        message = getString(17041308, new Object[]{appName, this.mAccountName});
                    }
                    return message;
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new SecurityException("Cannot find the calling package");
            }
        } else {
            throw new SecurityException("User Creation intent must be launched with startActivityForResult");
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        setResult(0);
        if (which == -1 && this.mCanProceed) {
            Log.i(TAG, "Ok, creating user");
            UserInfo user = this.mUserManager.createUser(this.mUserName, 0);
            if (user == null) {
                Log.e(TAG, "Couldn't create user");
                finish();
                return;
            }
            this.mUserManager.setSeedAccountData(user.id, this.mAccountName, this.mAccountType, this.mAccountOptions);
            setResult(-1);
        }
        finish();
    }
}
