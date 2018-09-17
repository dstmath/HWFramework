package com.android.internal.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.app.AlertController.AlertParams;

public class ConfirmUserCreationActivity extends AlertActivity implements OnClickListener {
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
        AlertParams ap = this.mAlertParams;
        ap.mMessage = message;
        ap.mPositiveButtonText = getString(R.string.ok);
        ap.mPositiveButtonListener = this;
        if (this.mCanProceed) {
            ap.mNegativeButtonText = getString(R.string.cancel);
            ap.mNegativeButtonListener = this;
        }
        setupAlert();
    }

    private String checkUserCreationRequirements() {
        String callingPackage = getCallingPackage();
        if (callingPackage == null) {
            throw new SecurityException("User Creation intent must be launched with startActivityForResult");
        }
        try {
            int cantCreateUser;
            int accountExists;
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(callingPackage, 0);
            if (this.mUserManager.hasUserRestriction("no_add_user")) {
                cantCreateUser = 1;
            } else {
                cantCreateUser = this.mUserManager.isAdminUser() ^ 1;
            }
            boolean cantCreateAnyMoreUsers = this.mUserManager.canAddMoreUsers() ^ 1;
            Account account = new Account(this.mAccountName, this.mAccountType);
            if (this.mAccountName == null || this.mAccountType == null) {
                accountExists = 0;
            } else {
                accountExists = AccountManager.get(this).someUserHasAccount(account) | this.mUserManager.someUserHasSeedAccount(this.mAccountName, this.mAccountType);
            }
            this.mCanProceed = true;
            String appName = appInfo.loadLabel(getPackageManager()).toString();
            if (cantCreateUser != 0) {
                setResult(1);
                return null;
            } else if (cantCreateAnyMoreUsers) {
                setResult(2);
                return null;
            } else {
                String message;
                if (accountExists != 0) {
                    message = getString(R.string.user_creation_account_exists, new Object[]{appName, this.mAccountName});
                } else {
                    message = getString(R.string.user_creation_adding, new Object[]{appName, this.mAccountName});
                }
                return message;
            }
        } catch (NameNotFoundException e) {
            throw new SecurityException("Cannot find the calling package");
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
