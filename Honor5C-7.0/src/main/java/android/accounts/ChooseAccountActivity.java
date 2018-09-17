package android.accounts;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract.Directory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.HashMap;

public class ChooseAccountActivity extends Activity {
    private static final String TAG = "AccountManager";
    private AccountManagerResponse mAccountManagerResponse;
    private Parcelable[] mAccounts;
    private Bundle mResult;
    private HashMap<String, AuthenticatorDescription> mTypeToAuthDescription;

    private static class AccountArrayAdapter extends ArrayAdapter<AccountInfo> {
        private AccountInfo[] mInfos;
        private LayoutInflater mLayoutInflater;

        public AccountArrayAdapter(Context context, int textViewResourceId, AccountInfo[] infos) {
            super(context, textViewResourceId, infos);
            this.mInfos = infos;
            this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = this.mLayoutInflater.inflate(17367105, null);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(16909124);
                holder.icon = (ImageView) convertView.findViewById(16909123);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.text.setText(this.mInfos[position].name);
            holder.icon.setImageDrawable(this.mInfos[position].drawable);
            return convertView;
        }
    }

    private static class AccountInfo {
        final Drawable drawable;
        final String name;

        AccountInfo(String name, Drawable drawable) {
            this.name = name;
            this.drawable = drawable;
        }
    }

    private static class ViewHolder {
        ImageView icon;
        TextView text;

        private ViewHolder() {
        }
    }

    public ChooseAccountActivity() {
        this.mAccounts = null;
        this.mAccountManagerResponse = null;
        this.mTypeToAuthDescription = new HashMap();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAccounts = getIntent().getParcelableArrayExtra(AccountManager.KEY_ACCOUNTS);
        this.mAccountManagerResponse = (AccountManagerResponse) getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_MANAGER_RESPONSE);
        if (this.mAccounts == null) {
            setResult(0);
            finish();
            return;
        }
        getAuthDescriptions();
        AccountInfo[] mAccountInfos = new AccountInfo[this.mAccounts.length];
        for (int i = 0; i < this.mAccounts.length; i++) {
            mAccountInfos[i] = new AccountInfo(((Account) this.mAccounts[i]).name, getDrawableForType(((Account) this.mAccounts[i]).type));
        }
        setContentView(17367104);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(new AccountArrayAdapter(this, R.layout.simple_list_item_1, mAccountInfos));
        list.setChoiceMode(1);
        list.setTextFilterEnabled(true);
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ChooseAccountActivity.this.onListItemClick((ListView) parent, v, position, id);
            }
        });
    }

    private void getAuthDescriptions() {
        for (AuthenticatorDescription desc : AccountManager.get(this).getAuthenticatorTypes()) {
            this.mTypeToAuthDescription.put(desc.type, desc);
        }
    }

    private Drawable getDrawableForType(String accountType) {
        Drawable icon = null;
        if (this.mTypeToAuthDescription.containsKey(accountType)) {
            try {
                AuthenticatorDescription desc = (AuthenticatorDescription) this.mTypeToAuthDescription.get(accountType);
                icon = createPackageContext(desc.packageName, 0).getDrawable(desc.iconId);
            } catch (NameNotFoundException e) {
                if (Log.isLoggable(TAG, 5)) {
                    Log.w(TAG, "No icon name for account type " + accountType);
                }
            } catch (NotFoundException e2) {
                if (Log.isLoggable(TAG, 5)) {
                    Log.w(TAG, "No icon resource for account type " + accountType);
                }
            }
        }
        return icon;
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        Account account = this.mAccounts[position];
        Log.d(TAG, "selected account " + account);
        Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        bundle.putString(Directory.ACCOUNT_TYPE, account.type);
        this.mResult = bundle;
        finish();
    }

    public void finish() {
        if (this.mAccountManagerResponse != null) {
            if (this.mResult != null) {
                this.mAccountManagerResponse.onResult(this.mResult);
            } else {
                this.mAccountManagerResponse.onError(4, "canceled");
            }
        }
        super.finish();
    }
}
