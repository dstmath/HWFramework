package android.accounts;

import android.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.collect.Sets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ChooseTypeAndAccountActivity extends Activity implements AccountManagerCallback<Bundle> {
    public static final String EXTRA_ADD_ACCOUNT_AUTH_TOKEN_TYPE_STRING = "authTokenType";
    public static final String EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE = "addAccountOptions";
    public static final String EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY = "addAccountRequiredFeatures";
    public static final String EXTRA_ALLOWABLE_ACCOUNTS_ARRAYLIST = "allowableAccounts";
    public static final String EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY = "allowableAccountTypes";
    @Deprecated
    public static final String EXTRA_ALWAYS_PROMPT_FOR_ACCOUNT = "alwaysPromptForAccount";
    public static final String EXTRA_DESCRIPTION_TEXT_OVERRIDE = "descriptionTextOverride";
    public static final String EXTRA_SELECTED_ACCOUNT = "selectedAccount";
    private static final String KEY_INSTANCE_STATE_ACCOUNTS_LIST = "accountsList";
    private static final String KEY_INSTANCE_STATE_EXISTING_ACCOUNTS = "existingAccounts";
    private static final String KEY_INSTANCE_STATE_PENDING_REQUEST = "pendingRequest";
    private static final String KEY_INSTANCE_STATE_SELECTED_ACCOUNT_NAME = "selectedAccountName";
    private static final String KEY_INSTANCE_STATE_SELECTED_ADD_ACCOUNT = "selectedAddAccount";
    private static final String KEY_INSTANCE_STATE_VISIBILITY_LIST = "visibilityList";
    public static final int REQUEST_ADD_ACCOUNT = 2;
    public static final int REQUEST_CHOOSE_TYPE = 1;
    public static final int REQUEST_NULL = 0;
    private static final int SELECTED_ITEM_NONE = -1;
    private static final String TAG = "AccountChooser";
    private LinkedHashMap<Account, Integer> mAccounts;
    private String mCallingPackage;
    private int mCallingUid;
    private String mDescriptionOverride;
    private boolean mDisallowAddAccounts;
    private boolean mDontShowPicker;
    private Parcelable[] mExistingAccounts = null;
    private Button mOkButton;
    private int mPendingRequest = 0;
    private ArrayList<Account> mPossiblyVisibleAccounts;
    private String mSelectedAccountName = null;
    private boolean mSelectedAddNewAccount = false;
    private int mSelectedItemIndex;
    private Set<Account> mSetOfAllowableAccounts;
    private Set<String> mSetOfRelevantAccountTypes;

    public void onCreate(Bundle savedInstanceState) {
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "ChooseTypeAndAccountActivity.onCreate(savedInstanceState=" + savedInstanceState + ")");
        }
        try {
            IBinder activityToken = getActivityToken();
            this.mCallingUid = ActivityManager.getService().getLaunchedFromUid(activityToken);
            this.mCallingPackage = ActivityManager.getService().getLaunchedFromPackage(activityToken);
            if (!(this.mCallingUid == 0 || this.mCallingPackage == null)) {
                this.mDisallowAddAccounts = UserManager.get(this).getUserRestrictions(new UserHandle(UserHandle.getUserId(this.mCallingUid))).getBoolean(UserManager.DISALLOW_MODIFY_ACCOUNTS, false);
            }
        } catch (RemoteException re) {
            Log.w(getClass().getSimpleName(), "Unable to get caller identity \n" + re);
        }
        Intent intent = getIntent();
        this.mSetOfAllowableAccounts = getAllowableAccountSet(intent);
        this.mSetOfRelevantAccountTypes = getReleventAccountTypes(intent);
        this.mDescriptionOverride = intent.getStringExtra(EXTRA_DESCRIPTION_TEXT_OVERRIDE);
        if (savedInstanceState != null) {
            this.mPendingRequest = savedInstanceState.getInt(KEY_INSTANCE_STATE_PENDING_REQUEST);
            this.mExistingAccounts = savedInstanceState.getParcelableArray(KEY_INSTANCE_STATE_EXISTING_ACCOUNTS);
            this.mSelectedAccountName = savedInstanceState.getString(KEY_INSTANCE_STATE_SELECTED_ACCOUNT_NAME);
            this.mSelectedAddNewAccount = savedInstanceState.getBoolean(KEY_INSTANCE_STATE_SELECTED_ADD_ACCOUNT, false);
            Parcelable[] accounts = savedInstanceState.getParcelableArray(KEY_INSTANCE_STATE_ACCOUNTS_LIST);
            ArrayList<Integer> visibility = savedInstanceState.getIntegerArrayList(KEY_INSTANCE_STATE_VISIBILITY_LIST);
            this.mAccounts = new LinkedHashMap();
            for (int i = 0; i < accounts.length; i++) {
                this.mAccounts.put((Account) accounts[i], (Integer) visibility.get(i));
            }
        } else {
            this.mPendingRequest = 0;
            this.mExistingAccounts = null;
            Account selectedAccount = (Account) intent.getParcelableExtra(EXTRA_SELECTED_ACCOUNT);
            if (selectedAccount != null) {
                this.mSelectedAccountName = selectedAccount.name;
            }
            this.mAccounts = getAcceptableAccountChoices(AccountManager.get(this));
        }
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "selected account name is " + this.mSelectedAccountName);
        }
        this.mPossiblyVisibleAccounts = new ArrayList(this.mAccounts.size());
        for (Entry<Account, Integer> entry : this.mAccounts.entrySet()) {
            if (3 != ((Integer) entry.getValue()).intValue()) {
                this.mPossiblyVisibleAccounts.add((Account) entry.getKey());
            }
        }
        if (this.mPossiblyVisibleAccounts.isEmpty() && this.mDisallowAddAccounts) {
            requestWindowFeature(1);
            setContentView(17367094);
            this.mDontShowPicker = true;
        }
        if (this.mDontShowPicker) {
            super.onCreate(savedInstanceState);
            return;
        }
        if (this.mPendingRequest == 0 && this.mPossiblyVisibleAccounts.isEmpty()) {
            setNonLabelThemeAndCallSuperCreate(savedInstanceState);
            if (this.mSetOfRelevantAccountTypes.size() == 1) {
                runAddAccountForAuthenticator((String) this.mSetOfRelevantAccountTypes.iterator().next());
                finish();
            } else {
                startChooseAccountTypeActivity();
                return;
            }
        }
        String[] listItems = getListOfDisplayableOptions(this.mPossiblyVisibleAccounts);
        this.mSelectedItemIndex = getItemIndexToSelect(this.mPossiblyVisibleAccounts, this.mSelectedAccountName, this.mSelectedAddNewAccount);
        super.onCreate(savedInstanceState);
        setContentView(17367110);
        overrideDescriptionIfSupplied(this.mDescriptionOverride);
        populateUIAccountList(listItems);
        this.mOkButton = (Button) findViewById(R.id.button2);
        this.mOkButton.setEnabled(this.mSelectedItemIndex != -1);
    }

    protected void onDestroy() {
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "ChooseTypeAndAccountActivity.onDestroy()");
        }
        super.onDestroy();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_INSTANCE_STATE_PENDING_REQUEST, this.mPendingRequest);
        if (this.mPendingRequest == 2) {
            outState.putParcelableArray(KEY_INSTANCE_STATE_EXISTING_ACCOUNTS, this.mExistingAccounts);
        }
        if (this.mSelectedItemIndex != -1) {
            if (this.mSelectedItemIndex == this.mPossiblyVisibleAccounts.size()) {
                outState.putBoolean(KEY_INSTANCE_STATE_SELECTED_ADD_ACCOUNT, true);
            } else {
                outState.putBoolean(KEY_INSTANCE_STATE_SELECTED_ADD_ACCOUNT, false);
                outState.putString(KEY_INSTANCE_STATE_SELECTED_ACCOUNT_NAME, ((Account) this.mPossiblyVisibleAccounts.get(this.mSelectedItemIndex)).name);
            }
        }
        Parcelable[] accounts = new Parcelable[this.mAccounts.size()];
        ArrayList<Integer> visibility = new ArrayList(this.mAccounts.size());
        int i = 0;
        for (Entry<Account, Integer> e : this.mAccounts.entrySet()) {
            int i2 = i + 1;
            accounts[i] = (Parcelable) e.getKey();
            visibility.add((Integer) e.getValue());
            i = i2;
        }
        outState.putParcelableArray(KEY_INSTANCE_STATE_ACCOUNTS_LIST, accounts);
        outState.putIntegerArrayList(KEY_INSTANCE_STATE_VISIBILITY_LIST, visibility);
    }

    public void onCancelButtonClicked(View view) {
        onBackPressed();
    }

    public void onOkButtonClicked(View view) {
        if (this.mSelectedItemIndex == this.mPossiblyVisibleAccounts.size()) {
            startChooseAccountTypeActivity();
        } else if (this.mSelectedItemIndex != -1) {
            onAccountSelected((Account) this.mPossiblyVisibleAccounts.get(this.mSelectedItemIndex));
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Log.isLoggable(TAG, 2)) {
            if (!(data == null || data.getExtras() == null)) {
                data.getExtras().keySet();
            }
            Log.v(TAG, "ChooseTypeAndAccountActivity.onActivityResult(reqCode=" + requestCode + ", resCode=" + resultCode + ", extras=" + (data != null ? data.getExtras() : null) + ")");
        }
        this.mPendingRequest = 0;
        if (resultCode == 0) {
            if (this.mPossiblyVisibleAccounts.isEmpty()) {
                setResult(0);
                finish();
            }
            return;
        }
        if (resultCode == -1) {
            String accountType;
            if (requestCode == 1) {
                if (data != null) {
                    accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                    if (accountType != null) {
                        runAddAccountForAuthenticator(accountType);
                        finish();
                    }
                }
                Log.d(TAG, "ChooseTypeAndAccountActivity.onActivityResult: unable to find account type, pretending the request was canceled");
            } else if (requestCode == 2) {
                String accountName = null;
                accountType = null;
                if (data != null) {
                    accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                }
                if (accountName == null || accountType == null) {
                    Account[] currentAccounts = AccountManager.get(this).getAccountsForPackage(this.mCallingPackage, this.mCallingUid);
                    Set<Account> preExistingAccounts = new HashSet();
                    for (Parcelable accountParcel : this.mExistingAccounts) {
                        preExistingAccounts.add((Account) accountParcel);
                    }
                    for (Account account : currentAccounts) {
                        if (!preExistingAccounts.contains(account)) {
                            accountName = account.name;
                            accountType = account.type;
                            break;
                        }
                    }
                }
                if (!(accountName == null && accountType == null)) {
                    setResultAndFinish(accountName, accountType);
                    return;
                }
            }
            Log.d(TAG, "ChooseTypeAndAccountActivity.onActivityResult: unable to find added account, pretending the request was canceled");
        }
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "ChooseTypeAndAccountActivity.onActivityResult: canceled");
        }
        setResult(0);
        finish();
    }

    protected void runAddAccountForAuthenticator(String type) {
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "runAddAccountForAuthenticator: " + type);
        }
        Bundle options = getIntent().getBundleExtra(EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE);
        String[] requiredFeatures = getIntent().getStringArrayExtra(EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY);
        AccountManager.get(this).addAccount(type, getIntent().getStringExtra("authTokenType"), requiredFeatures, options, null, this, null);
    }

    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
        try {
            Intent intent = (Intent) ((Bundle) accountManagerFuture.getResult()).getParcelable("intent");
            if (intent != null) {
                this.mPendingRequest = 2;
                this.mExistingAccounts = AccountManager.get(this).getAccountsForPackage(this.mCallingPackage, this.mCallingUid);
                intent.setFlags(intent.getFlags() & -268435457);
                startActivityForResult(intent, 2);
                return;
            }
        } catch (OperationCanceledException e) {
            setResult(0);
            finish();
            return;
        } catch (IOException e2) {
        } catch (AuthenticatorException e3) {
        }
        Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "error communicating with server");
        setResult(-1, new Intent().putExtras(bundle));
        finish();
    }

    private void setNonLabelThemeAndCallSuperCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        super.onCreate(savedInstanceState);
    }

    private void onAccountSelected(Account account) {
        Log.d(TAG, "selected account " + account);
        setResultAndFinish(account.name, account.type);
    }

    private void setResultAndFinish(String accountName, String accountType) {
        Account account = new Account(accountName, accountType);
        Integer oldVisibility = Integer.valueOf(AccountManager.get(this).getAccountVisibility(account, this.mCallingPackage));
        if (oldVisibility != null && oldVisibility.intValue() == 4) {
            AccountManager.get(this).setAccountVisibility(account, this.mCallingPackage, 2);
        }
        if (oldVisibility == null || oldVisibility.intValue() != 3) {
            Bundle bundle = new Bundle();
            bundle.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
            bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
            setResult(-1, new Intent().putExtras(bundle));
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "ChooseTypeAndAccountActivity.setResultAndFinish: selected account " + accountName + ", " + accountType);
            }
            finish();
            return;
        }
        setResult(0);
        finish();
    }

    private void startChooseAccountTypeActivity() {
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "ChooseAccountTypeActivity.startChooseAccountTypeActivity()");
        }
        Intent intent = new Intent((Context) this, ChooseAccountTypeActivity.class);
        intent.setFlags(524288);
        intent.putExtra(EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY, getIntent().getStringArrayExtra(EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY));
        intent.putExtra(EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE, getIntent().getBundleExtra(EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE));
        intent.putExtra(EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY, getIntent().getStringArrayExtra(EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY));
        intent.putExtra("authTokenType", getIntent().getStringExtra("authTokenType"));
        startActivityForResult(intent, 1);
        this.mPendingRequest = 1;
    }

    private int getItemIndexToSelect(ArrayList<Account> accounts, String selectedAccountName, boolean selectedAddNewAccount) {
        if (selectedAddNewAccount) {
            return accounts.size();
        }
        for (int i = 0; i < accounts.size(); i++) {
            if (((Account) accounts.get(i)).name.equals(selectedAccountName)) {
                return i;
            }
        }
        return -1;
    }

    private String[] getListOfDisplayableOptions(ArrayList<Account> accounts) {
        String[] listItems = new String[((this.mDisallowAddAccounts ? 0 : 1) + accounts.size())];
        for (int i = 0; i < accounts.size(); i++) {
            listItems[i] = ((Account) accounts.get(i)).name;
        }
        if (!this.mDisallowAddAccounts) {
            listItems[accounts.size()] = getResources().getString(17039549);
        }
        return listItems;
    }

    private LinkedHashMap<Account, Integer> getAcceptableAccountChoices(AccountManager accountManager) {
        Map<Account, Integer> accountsAndVisibilityForCaller = accountManager.getAccountsAndVisibilityForPackage(this.mCallingPackage, null);
        Account[] allAccounts = accountManager.getAccounts();
        LinkedHashMap<Account, Integer> accountsToPopulate = new LinkedHashMap(accountsAndVisibilityForCaller.size());
        for (Account account : allAccounts) {
            if ((this.mSetOfAllowableAccounts == null || (this.mSetOfAllowableAccounts.contains(account) ^ 1) == 0) && ((this.mSetOfRelevantAccountTypes == null || (this.mSetOfRelevantAccountTypes.contains(account.type) ^ 1) == 0) && accountsAndVisibilityForCaller.get(account) != null)) {
                accountsToPopulate.put(account, (Integer) accountsAndVisibilityForCaller.get(account));
            }
        }
        return accountsToPopulate;
    }

    private Set<String> getReleventAccountTypes(Intent intent) {
        String[] allowedAccountTypes = intent.getStringArrayExtra(EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY);
        AuthenticatorDescription[] descs = AccountManager.get(this).getAuthenticatorTypes();
        Set<String> supportedAccountTypes = new HashSet(descs.length);
        for (AuthenticatorDescription desc : descs) {
            supportedAccountTypes.add(desc.type);
        }
        if (allowedAccountTypes == null) {
            return supportedAccountTypes;
        }
        Set<String> setOfRelevantAccountTypes = Sets.newHashSet(allowedAccountTypes);
        setOfRelevantAccountTypes.retainAll(supportedAccountTypes);
        return setOfRelevantAccountTypes;
    }

    private Set<Account> getAllowableAccountSet(Intent intent) {
        Set<Account> setOfAllowableAccounts = null;
        ArrayList<Parcelable> validAccounts = intent.getParcelableArrayListExtra(EXTRA_ALLOWABLE_ACCOUNTS_ARRAYLIST);
        if (validAccounts != null) {
            setOfAllowableAccounts = new HashSet(validAccounts.size());
            for (Parcelable parcelable : validAccounts) {
                setOfAllowableAccounts.add((Account) parcelable);
            }
        }
        return setOfAllowableAccounts;
    }

    private void overrideDescriptionIfSupplied(String descriptionOverride) {
        TextView descriptionView = (TextView) findViewById(16908841);
        if (TextUtils.isEmpty(descriptionOverride)) {
            descriptionView.setVisibility(8);
        } else {
            descriptionView.setText(descriptionOverride);
        }
    }

    private final void populateUIAccountList(String[] listItems) {
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(new ArrayAdapter(this, R.layout.simple_list_item_single_choice, listItems));
        list.setChoiceMode(1);
        list.setItemsCanFocus(false);
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                ChooseTypeAndAccountActivity.this.mSelectedItemIndex = position;
                ChooseTypeAndAccountActivity.this.mOkButton.setEnabled(true);
            }
        });
        if (this.mSelectedItemIndex != -1) {
            list.setItemChecked(this.mSelectedItemIndex, true);
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "List item " + this.mSelectedItemIndex + " should be selected");
            }
        }
    }
}
