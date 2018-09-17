package android.accounts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.IOException;

public class GrantCredentialsPermissionActivity extends Activity implements OnClickListener {
    public static final String EXTRAS_ACCOUNT = "account";
    public static final String EXTRAS_AUTH_TOKEN_TYPE = "authTokenType";
    public static final String EXTRAS_REQUESTING_UID = "uid";
    public static final String EXTRAS_RESPONSE = "response";
    private Account mAccount;
    private String mAuthTokenType;
    protected LayoutInflater mInflater;
    private Bundle mResultBundle = null;
    private int mUid;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(17367146);
        setTitle(17040089);
        this.mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            setResult(0);
            finish();
            return;
        }
        this.mAccount = (Account) extras.getParcelable("account");
        this.mAuthTokenType = extras.getString("authTokenType");
        this.mUid = extras.getInt(EXTRAS_REQUESTING_UID);
        PackageManager pm = getPackageManager();
        String[] packages = pm.getPackagesForUid(this.mUid);
        if (this.mAccount == null || this.mAuthTokenType == null || packages == null) {
            setResult(0);
            finish();
            return;
        }
        try {
            String accountTypeLabel = getAccountLabel(this.mAccount);
            final TextView authTokenTypeView = (TextView) findViewById(16908733);
            authTokenTypeView.setVisibility(8);
            AccountManagerCallback<String> callback = new AccountManagerCallback<String>() {
                public void run(AccountManagerFuture<String> future) {
                    try {
                        final String authTokenLabel = (String) future.getResult();
                        if (!TextUtils.isEmpty(authTokenLabel)) {
                            GrantCredentialsPermissionActivity grantCredentialsPermissionActivity = GrantCredentialsPermissionActivity.this;
                            final TextView textView = authTokenTypeView;
                            grantCredentialsPermissionActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (!GrantCredentialsPermissionActivity.this.isFinishing()) {
                                        textView.setText(authTokenLabel);
                                        textView.setVisibility(0);
                                    }
                                }
                            });
                        }
                    } catch (OperationCanceledException e) {
                    } catch (IOException e2) {
                    } catch (AuthenticatorException e3) {
                    }
                }
            };
            if (!AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE.equals(this.mAuthTokenType)) {
                AccountManager.get(this).getAuthTokenLabel(this.mAccount.type, this.mAuthTokenType, callback, null);
            }
            findViewById(16908714).setOnClickListener(this);
            findViewById(16908840).setOnClickListener(this);
            LinearLayout packagesListView = (LinearLayout) findViewById(16909143);
            for (String pkg : packages) {
                String packageLabel;
                try {
                    packageLabel = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
                } catch (NameNotFoundException e) {
                    packageLabel = pkg;
                }
                packagesListView.addView(newPackageView(packageLabel));
            }
            ((TextView) findViewById(16908665)).setText(this.mAccount.name);
            ((TextView) findViewById(16908668)).setText(accountTypeLabel);
        } catch (IllegalArgumentException e2) {
            setResult(0);
            finish();
        }
    }

    private String getAccountLabel(Account account) {
        for (AuthenticatorDescription desc : AccountManager.get(this).getAuthenticatorTypes()) {
            if (desc.type.equals(account.type)) {
                try {
                    return createPackageContext(desc.packageName, 0).getString(desc.labelId);
                } catch (NameNotFoundException e) {
                    return account.type;
                } catch (NotFoundException e2) {
                    return account.type;
                }
            }
        }
        return account.type;
    }

    private View newPackageView(String packageLabel) {
        View view = this.mInflater.inflate(17367203, null);
        ((TextView) view.findViewById(16909142)).setText(packageLabel);
        return view;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 16908714:
                AccountManager.get(this).updateAppPermission(this.mAccount, this.mAuthTokenType, this.mUid, true);
                Intent result = new Intent();
                result.putExtra("retry", true);
                setResult(-1, result);
                setAccountAuthenticatorResult(result.getExtras());
                break;
            case 16908840:
                AccountManager.get(this).updateAppPermission(this.mAccount, this.mAuthTokenType, this.mUid, false);
                setResult(0);
                break;
        }
        finish();
    }

    public final void setAccountAuthenticatorResult(Bundle result) {
        this.mResultBundle = result;
    }

    public void finish() {
        AccountAuthenticatorResponse response = (AccountAuthenticatorResponse) getIntent().getParcelableExtra(EXTRAS_RESPONSE);
        if (response != null) {
            if (this.mResultBundle != null) {
                response.onResult(this.mResultBundle);
            } else {
                response.onError(4, "canceled");
            }
        }
        super.finish();
    }
}
