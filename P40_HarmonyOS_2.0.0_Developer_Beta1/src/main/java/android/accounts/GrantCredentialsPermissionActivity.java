package android.accounts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.R;
import java.io.IOException;

public class GrantCredentialsPermissionActivity extends Activity implements View.OnClickListener {
    public static final String EXTRAS_ACCOUNT = "account";
    public static final String EXTRAS_AUTH_TOKEN_TYPE = "authTokenType";
    public static final String EXTRAS_REQUESTING_UID = "uid";
    public static final String EXTRAS_RESPONSE = "response";
    private Account mAccount;
    private String mAuthTokenType;
    protected LayoutInflater mInflater;
    private Bundle mResultBundle = null;
    private int mUid;

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        String packageLabel;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grant_credentials_permission);
        setTitle(R.string.grant_permissions_header_text);
        this.mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            setResult(0);
            finish();
            return;
        }
        this.mAccount = (Account) extras.getParcelable("account");
        this.mAuthTokenType = extras.getString("authTokenType");
        this.mUid = extras.getInt("uid");
        PackageManager pm = getPackageManager();
        String[] packages = pm.getPackagesForUid(this.mUid);
        Account account = this.mAccount;
        if (account == null || this.mAuthTokenType == null || packages == null) {
            setResult(0);
            finish();
            return;
        }
        try {
            String accountTypeLabel = getAccountLabel(account);
            final TextView authTokenTypeView = (TextView) findViewById(R.id.authtoken_type);
            authTokenTypeView.setVisibility(8);
            AccountManagerCallback<String> callback = new AccountManagerCallback<String>() {
                /* class android.accounts.GrantCredentialsPermissionActivity.AnonymousClass1 */

                @Override // android.accounts.AccountManagerCallback
                public void run(AccountManagerFuture<String> future) {
                    try {
                        final String authTokenLabel = future.getResult();
                        if (!TextUtils.isEmpty(authTokenLabel)) {
                            GrantCredentialsPermissionActivity.this.runOnUiThread(new Runnable() {
                                /* class android.accounts.GrantCredentialsPermissionActivity.AnonymousClass1.AnonymousClass1 */

                                @Override // java.lang.Runnable
                                public void run() {
                                    if (!GrantCredentialsPermissionActivity.this.isFinishing()) {
                                        authTokenTypeView.setText(authTokenLabel);
                                        authTokenTypeView.setVisibility(0);
                                    }
                                }
                            });
                        }
                    } catch (AuthenticatorException | OperationCanceledException | IOException e) {
                    }
                }
            };
            if (!AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE.equals(this.mAuthTokenType)) {
                AccountManager.get(this).getAuthTokenLabel(this.mAccount.type, this.mAuthTokenType, callback, null);
            }
            findViewById(R.id.allow_button).setOnClickListener(this);
            findViewById(R.id.deny_button).setOnClickListener(this);
            LinearLayout packagesListView = (LinearLayout) findViewById(R.id.packages_list);
            for (String pkg : packages) {
                try {
                    packageLabel = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    packageLabel = pkg;
                }
                packagesListView.addView(newPackageView(packageLabel));
            }
            ((TextView) findViewById(R.id.account_name)).setText(this.mAccount.name);
            ((TextView) findViewById(R.id.account_type)).setText(accountTypeLabel);
        } catch (IllegalArgumentException e2) {
            setResult(0);
            finish();
        }
    }

    private String getAccountLabel(Account account) {
        AuthenticatorDescription[] authenticatorTypes = AccountManager.get(this).getAuthenticatorTypes();
        for (AuthenticatorDescription desc : authenticatorTypes) {
            if (desc.type.equals(account.type)) {
                try {
                    return createPackageContext(desc.packageName, 0).getString(desc.labelId);
                } catch (PackageManager.NameNotFoundException e) {
                    return account.type;
                } catch (Resources.NotFoundException e2) {
                    return account.type;
                }
            }
        }
        return account.type;
    }

    private View newPackageView(String packageLabel) {
        View view = this.mInflater.inflate(R.layout.permissions_package_list_item, (ViewGroup) null);
        ((TextView) view.findViewById(R.id.package_label)).setText(packageLabel);
        return view;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        int id = v.getId();
        if (id == 16908757) {
            AccountManager.get(this).updateAppPermission(this.mAccount, this.mAuthTokenType, this.mUid, true);
            Intent result = new Intent();
            result.putExtra("retry", true);
            setResult(-1, result);
            setAccountAuthenticatorResult(result.getExtras());
        } else if (id == 16908914) {
            AccountManager.get(this).updateAppPermission(this.mAccount, this.mAuthTokenType, this.mUid, false);
            setResult(0);
        }
        finish();
    }

    public final void setAccountAuthenticatorResult(Bundle result) {
        this.mResultBundle = result;
    }

    @Override // android.app.Activity
    public void finish() {
        AccountAuthenticatorResponse response = (AccountAuthenticatorResponse) getIntent().getParcelableExtra("response");
        if (response != null) {
            Bundle bundle = this.mResultBundle;
            if (bundle != null) {
                response.onResult(bundle);
            } else {
                response.onError(4, "canceled");
            }
        }
        super.finish();
    }
}
