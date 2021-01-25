package android.accounts;

import android.app.Activity;
import android.app.ActivityTaskManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.R;
import java.util.HashMap;

public class ChooseAccountActivity extends Activity {
    private static final String TAG = "AccountManager";
    private AccountManagerResponse mAccountManagerResponse = null;
    private Parcelable[] mAccounts = null;
    private String mCallingPackage;
    private int mCallingUid;
    private Bundle mResult;
    private HashMap<String, AuthenticatorDescription> mTypeToAuthDescription = new HashMap<>();

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAccounts = getIntent().getParcelableArrayExtra(AccountManager.KEY_ACCOUNTS);
        this.mAccountManagerResponse = (AccountManagerResponse) getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_MANAGER_RESPONSE);
        if (this.mAccounts == null) {
            setResult(0);
            finish();
            return;
        }
        try {
            IBinder activityToken = getActivityToken();
            this.mCallingUid = ActivityTaskManager.getService().getLaunchedFromUid(activityToken);
            this.mCallingPackage = ActivityTaskManager.getService().getLaunchedFromPackage(activityToken);
        } catch (RemoteException re) {
            String simpleName = getClass().getSimpleName();
            Log.w(simpleName, "Unable to get caller identity \n" + re);
        }
        if (UserHandle.isSameApp(this.mCallingUid, 1000) && getIntent().getStringExtra(AccountManager.KEY_ANDROID_PACKAGE_NAME) != null) {
            this.mCallingPackage = getIntent().getStringExtra(AccountManager.KEY_ANDROID_PACKAGE_NAME);
        }
        if (!UserHandle.isSameApp(this.mCallingUid, 1000) && getIntent().getStringExtra(AccountManager.KEY_ANDROID_PACKAGE_NAME) != null) {
            String simpleName2 = getClass().getSimpleName();
            Log.w(simpleName2, "Non-system Uid: " + this.mCallingUid + " tried to override packageName \n");
        }
        getAuthDescriptions();
        AccountInfo[] mAccountInfos = new AccountInfo[this.mAccounts.length];
        int i = 0;
        while (true) {
            Parcelable[] parcelableArr = this.mAccounts;
            if (i < parcelableArr.length) {
                mAccountInfos[i] = new AccountInfo(((Account) parcelableArr[i]).name, getDrawableForType(((Account) this.mAccounts[i]).type));
                i++;
            } else {
                setContentView(R.layout.choose_account);
                ListView list = (ListView) findViewById(16908298);
                list.setAdapter((ListAdapter) new AccountArrayAdapter(this, 17367043, mAccountInfos));
                list.setChoiceMode(1);
                list.setTextFilterEnabled(true);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    /* class android.accounts.ChooseAccountActivity.AnonymousClass1 */

                    @Override // android.widget.AdapterView.OnItemClickListener
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        ChooseAccountActivity.this.onListItemClick((ListView) parent, v, position, id);
                    }
                });
                return;
            }
        }
    }

    private void getAuthDescriptions() {
        AuthenticatorDescription[] authenticatorTypes = AccountManager.get(this).getAuthenticatorTypes();
        for (AuthenticatorDescription desc : authenticatorTypes) {
            this.mTypeToAuthDescription.put(desc.type, desc);
        }
    }

    private Drawable getDrawableForType(String accountType) {
        if (!this.mTypeToAuthDescription.containsKey(accountType)) {
            return null;
        }
        try {
            AuthenticatorDescription desc = this.mTypeToAuthDescription.get(accountType);
            return createPackageContext(desc.packageName, 0).getDrawable(desc.iconId);
        } catch (PackageManager.NameNotFoundException e) {
            if (!Log.isLoggable(TAG, 5)) {
                return null;
            }
            Log.w(TAG, "No icon name for account type " + accountType);
            return null;
        } catch (Resources.NotFoundException e2) {
            if (!Log.isLoggable(TAG, 5)) {
                return null;
            }
            Log.w(TAG, "No icon resource for account type " + accountType);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void onListItemClick(ListView l, View v, int position, long id) {
        Account account = (Account) this.mAccounts[position];
        AccountManager am = AccountManager.get(this);
        Integer oldVisibility = Integer.valueOf(am.getAccountVisibility(account, this.mCallingPackage));
        if (oldVisibility != null && oldVisibility.intValue() == 4) {
            am.setAccountVisibility(account, this.mCallingPackage, 2);
        }
        Log.d(TAG, "selected account " + account);
        Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        bundle.putString("accountType", account.type);
        this.mResult = bundle;
        finish();
    }

    @Override // android.app.Activity
    public void finish() {
        AccountManagerResponse accountManagerResponse = this.mAccountManagerResponse;
        if (accountManagerResponse != null) {
            Bundle bundle = this.mResult;
            if (bundle != null) {
                accountManagerResponse.onResult(bundle);
            } else {
                accountManagerResponse.onError(4, "canceled");
            }
        }
        super.finish();
    }

    private static class AccountInfo {
        final Drawable drawable;
        final String name;

        AccountInfo(String name2, Drawable drawable2) {
            this.name = name2;
            this.drawable = drawable2;
        }
    }

    private static class ViewHolder {
        ImageView icon;
        TextView text;

        private ViewHolder() {
        }
    }

    private static class AccountArrayAdapter extends ArrayAdapter<AccountInfo> {
        private AccountInfo[] mInfos;
        private LayoutInflater mLayoutInflater;

        public AccountArrayAdapter(Context context, int textViewResourceId, AccountInfo[] infos) {
            super(context, textViewResourceId, infos);
            this.mInfos = infos;
            this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = this.mLayoutInflater.inflate(R.layout.choose_account_row, (ViewGroup) null);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.account_row_text);
                holder.icon = (ImageView) convertView.findViewById(R.id.account_row_icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.text.setText(this.mInfos[position].name);
            holder.icon.setImageDrawable(this.mInfos[position].drawable);
            return convertView;
        }
    }
}
