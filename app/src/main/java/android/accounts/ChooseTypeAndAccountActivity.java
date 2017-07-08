package android.accounts;

import android.R;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.ContactsContract.Directory;
import android.provider.DocumentsContract.Root;
import android.rms.iaware.DataContract.BaseAttr;
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
    private static final String KEY_INSTANCE_STATE_ACCOUNT_LIST = "accountList";
    private static final String KEY_INSTANCE_STATE_EXISTING_ACCOUNTS = "existingAccounts";
    private static final String KEY_INSTANCE_STATE_PENDING_REQUEST = "pendingRequest";
    private static final String KEY_INSTANCE_STATE_SELECTED_ACCOUNT_NAME = "selectedAccountName";
    private static final String KEY_INSTANCE_STATE_SELECTED_ADD_ACCOUNT = "selectedAddAccount";
    public static final int REQUEST_ADD_ACCOUNT = 2;
    public static final int REQUEST_CHOOSE_TYPE = 1;
    public static final int REQUEST_NULL = 0;
    private static final int SELECTED_ITEM_NONE = -1;
    private static final String TAG = "AccountChooser";
    private ArrayList<Account> mAccounts;
    private String mCallingPackage;
    private int mCallingUid;
    private String mDescriptionOverride;
    private boolean mDisallowAddAccounts;
    private boolean mDontShowPicker;
    private Parcelable[] mExistingAccounts;
    private Button mOkButton;
    private int mPendingRequest;
    private String mSelectedAccountName;
    private boolean mSelectedAddNewAccount;
    private int mSelectedItemIndex;
    private Set<Account> mSetOfAllowableAccounts;
    private Set<String> mSetOfRelevantAccountTypes;

    public ChooseTypeAndAccountActivity() {
        this.mSelectedAccountName = null;
        this.mSelectedAddNewAccount = false;
        this.mPendingRequest = REQUEST_NULL;
        this.mExistingAccounts = null;
    }

    public void onCreate(Bundle savedInstanceState) {
        if (Log.isLoggable(TAG, REQUEST_ADD_ACCOUNT)) {
            Log.v(TAG, "ChooseTypeAndAccountActivity.onCreate(savedInstanceState=" + savedInstanceState + ")");
        }
        try {
            IBinder activityToken = getActivityToken();
            this.mCallingUid = ActivityManagerNative.getDefault().getLaunchedFromUid(activityToken);
            this.mCallingPackage = ActivityManagerNative.getDefault().getLaunchedFromPackage(activityToken);
            if (!(this.mCallingUid == 0 || this.mCallingPackage == null)) {
                this.mDisallowAddAccounts = UserManager.get(this).getUserRestrictions(new UserHandle(UserHandle.getUserId(this.mCallingUid))).getBoolean(UserManager.DISALLOW_MODIFY_ACCOUNTS, false);
            }
        } catch (RemoteException re) {
            Log.w(getClass().getSimpleName(), "Unable to get caller identity \n" + re);
        }
        Intent intent = getIntent();
        if (savedInstanceState != null) {
            this.mPendingRequest = savedInstanceState.getInt(KEY_INSTANCE_STATE_PENDING_REQUEST);
            this.mExistingAccounts = savedInstanceState.getParcelableArray(KEY_INSTANCE_STATE_EXISTING_ACCOUNTS);
            this.mSelectedAccountName = savedInstanceState.getString(KEY_INSTANCE_STATE_SELECTED_ACCOUNT_NAME);
            this.mSelectedAddNewAccount = savedInstanceState.getBoolean(KEY_INSTANCE_STATE_SELECTED_ADD_ACCOUNT, false);
            this.mAccounts = savedInstanceState.getParcelableArrayList(KEY_INSTANCE_STATE_ACCOUNT_LIST);
        } else {
            this.mPendingRequest = REQUEST_NULL;
            this.mExistingAccounts = null;
            Account selectedAccount = (Account) intent.getParcelableExtra(EXTRA_SELECTED_ACCOUNT);
            if (selectedAccount != null) {
                this.mSelectedAccountName = selectedAccount.name;
            }
        }
        if (Log.isLoggable(TAG, REQUEST_ADD_ACCOUNT)) {
            Log.v(TAG, "selected account name is " + this.mSelectedAccountName);
        }
        this.mSetOfAllowableAccounts = getAllowableAccountSet(intent);
        this.mSetOfRelevantAccountTypes = getReleventAccountTypes(intent);
        this.mDescriptionOverride = intent.getStringExtra(EXTRA_DESCRIPTION_TEXT_OVERRIDE);
        this.mAccounts = getAcceptableAccountChoices(AccountManager.get(this));
        if (this.mAccounts.isEmpty() && this.mDisallowAddAccounts) {
            requestWindowFeature(REQUEST_CHOOSE_TYPE);
            setContentView(17367093);
            this.mDontShowPicker = true;
        }
        if (this.mDontShowPicker) {
            super.onCreate(savedInstanceState);
            return;
        }
        boolean z;
        if (this.mPendingRequest == 0 && this.mAccounts.isEmpty()) {
            setNonLabelThemeAndCallSuperCreate(savedInstanceState);
            if (this.mSetOfRelevantAccountTypes.size() == REQUEST_CHOOSE_TYPE) {
                runAddAccountForAuthenticator((String) this.mSetOfRelevantAccountTypes.iterator().next());
                finish();
            } else {
                startChooseAccountTypeActivity();
                return;
            }
        }
        String[] listItems = getListOfDisplayableOptions(this.mAccounts);
        this.mSelectedItemIndex = getItemIndexToSelect(this.mAccounts, this.mSelectedAccountName, this.mSelectedAddNewAccount);
        super.onCreate(savedInstanceState);
        setContentView(17367107);
        overrideDescriptionIfSupplied(this.mDescriptionOverride);
        populateUIAccountList(listItems);
        this.mOkButton = (Button) findViewById(R.id.button2);
        Button button = this.mOkButton;
        if (this.mSelectedItemIndex != SELECTED_ITEM_NONE) {
            z = true;
        } else {
            z = false;
        }
        button.setEnabled(z);
    }

    protected void onDestroy() {
        if (Log.isLoggable(TAG, REQUEST_ADD_ACCOUNT)) {
            Log.v(TAG, "ChooseTypeAndAccountActivity.onDestroy()");
        }
        super.onDestroy();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_INSTANCE_STATE_PENDING_REQUEST, this.mPendingRequest);
        if (this.mPendingRequest == REQUEST_ADD_ACCOUNT) {
            outState.putParcelableArray(KEY_INSTANCE_STATE_EXISTING_ACCOUNTS, this.mExistingAccounts);
        }
        if (this.mSelectedItemIndex != SELECTED_ITEM_NONE) {
            if (this.mSelectedItemIndex == this.mAccounts.size()) {
                outState.putBoolean(KEY_INSTANCE_STATE_SELECTED_ADD_ACCOUNT, true);
            } else {
                outState.putBoolean(KEY_INSTANCE_STATE_SELECTED_ADD_ACCOUNT, false);
                outState.putString(KEY_INSTANCE_STATE_SELECTED_ACCOUNT_NAME, ((Account) this.mAccounts.get(this.mSelectedItemIndex)).name);
            }
        }
        outState.putParcelableArrayList(KEY_INSTANCE_STATE_ACCOUNT_LIST, this.mAccounts);
    }

    public void onCancelButtonClicked(View view) {
        onBackPressed();
    }

    public void onOkButtonClicked(View view) {
        if (this.mSelectedItemIndex == this.mAccounts.size()) {
            startChooseAccountTypeActivity();
        } else if (this.mSelectedItemIndex != SELECTED_ITEM_NONE) {
            onAccountSelected((Account) this.mAccounts.get(this.mSelectedItemIndex));
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Log.isLoggable(TAG, REQUEST_ADD_ACCOUNT)) {
            if (!(data == null || data.getExtras() == null)) {
                data.getExtras().keySet();
            }
            Log.v(TAG, "ChooseTypeAndAccountActivity.onActivityResult(reqCode=" + requestCode + ", resCode=" + resultCode + ", extras=" + (data != null ? data.getExtras() : null) + ")");
        }
        this.mPendingRequest = REQUEST_NULL;
        if (resultCode == 0) {
            if (this.mAccounts == null || this.mAccounts.isEmpty()) {
                setResult(REQUEST_NULL);
                finish();
            }
            return;
        }
        if (resultCode == SELECTED_ITEM_NONE) {
            String accountType;
            if (requestCode == REQUEST_CHOOSE_TYPE) {
                if (data != null) {
                    accountType = data.getStringExtra(Directory.ACCOUNT_TYPE);
                    if (accountType != null) {
                        runAddAccountForAuthenticator(accountType);
                        finish();
                    }
                }
                Log.d(TAG, "ChooseTypeAndAccountActivity.onActivityResult: unable to find account type, pretending the request was canceled");
            } else if (requestCode == REQUEST_ADD_ACCOUNT) {
                String str = null;
                accountType = null;
                if (data != null) {
                    str = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    accountType = data.getStringExtra(Directory.ACCOUNT_TYPE);
                }
                if (str == null || r3 == null) {
                    int i;
                    Account[] currentAccounts = AccountManager.get(this).getAccountsForPackage(this.mCallingPackage, this.mCallingUid);
                    Set<Account> preExistingAccounts = new HashSet();
                    Parcelable[] parcelableArr = this.mExistingAccounts;
                    int length = parcelableArr.length;
                    for (i = REQUEST_NULL; i < length; i += REQUEST_CHOOSE_TYPE) {
                        preExistingAccounts.add((Account) parcelableArr[i]);
                    }
                    int length2 = currentAccounts.length;
                    for (i = REQUEST_NULL; i < length2; i += REQUEST_CHOOSE_TYPE) {
                        Account account = currentAccounts[i];
                        if (!preExistingAccounts.contains(account)) {
                            str = account.name;
                            accountType = account.type;
                            break;
                        }
                    }
                }
                if (!(str == null && accountType == null)) {
                    setResultAndFinish(str, accountType);
                    return;
                }
            }
            Log.d(TAG, "ChooseTypeAndAccountActivity.onActivityResult: unable to find added account, pretending the request was canceled");
        }
        if (Log.isLoggable(TAG, REQUEST_ADD_ACCOUNT)) {
            Log.v(TAG, "ChooseTypeAndAccountActivity.onActivityResult: canceled");
        }
        setResult(REQUEST_NULL);
        finish();
    }

    protected void runAddAccountForAuthenticator(String type) {
        if (Log.isLoggable(TAG, REQUEST_ADD_ACCOUNT)) {
            Log.v(TAG, "runAddAccountForAuthenticator: " + type);
        }
        Bundle options = getIntent().getBundleExtra(EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE);
        String[] requiredFeatures = getIntent().getStringArrayExtra(EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY);
        AccountManager.get(this).addAccount(type, getIntent().getStringExtra(EXTRA_ADD_ACCOUNT_AUTH_TOKEN_TYPE_STRING), requiredFeatures, options, null, this, null);
    }

    public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
        try {
            Intent intent = (Intent) ((Bundle) accountManagerFuture.getResult()).getParcelable(BaseAttr.INTENT);
            if (intent != null) {
                this.mPendingRequest = REQUEST_ADD_ACCOUNT;
                this.mExistingAccounts = AccountManager.get(this).getAccountsForPackage(this.mCallingPackage, this.mCallingUid);
                intent.setFlags(intent.getFlags() & -268435457);
                startActivityForResult(intent, REQUEST_ADD_ACCOUNT);
                return;
            }
        } catch (OperationCanceledException e) {
            setResult(REQUEST_NULL);
            finish();
            return;
        } catch (IOException e2) {
        } catch (AuthenticatorException e3) {
        }
        Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "error communicating with server");
        setResult(SELECTED_ITEM_NONE, new Intent().putExtras(bundle));
        finish();
    }

    private void setNonLabelThemeAndCallSuperCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Material_Light_Dialog_NoActionBar);
        super.onCreate(savedInstanceState);
    }

    private void onAccountSelected(Account account) {
        Log.d(TAG, "selected account " + account);
        setResultAndFinish(account.name, account.type);
    }

    private void setResultAndFinish(String accountName, String accountType) {
        Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
        bundle.putString(Directory.ACCOUNT_TYPE, accountType);
        setResult(SELECTED_ITEM_NONE, new Intent().putExtras(bundle));
        if (Log.isLoggable(TAG, REQUEST_ADD_ACCOUNT)) {
            Log.v(TAG, "ChooseTypeAndAccountActivity.setResultAndFinish: selected account " + accountName + ", " + accountType);
        }
        finish();
    }

    private void startChooseAccountTypeActivity() {
        if (Log.isLoggable(TAG, REQUEST_ADD_ACCOUNT)) {
            Log.v(TAG, "ChooseAccountTypeActivity.startChooseAccountTypeActivity()");
        }
        Intent intent = new Intent((Context) this, ChooseAccountTypeActivity.class);
        intent.setFlags(Root.FLAG_REMOVABLE_SD);
        intent.putExtra(EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY, getIntent().getStringArrayExtra(EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY));
        intent.putExtra(EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE, getIntent().getBundleExtra(EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE));
        intent.putExtra(EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY, getIntent().getStringArrayExtra(EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY));
        intent.putExtra(EXTRA_ADD_ACCOUNT_AUTH_TOKEN_TYPE_STRING, getIntent().getStringExtra(EXTRA_ADD_ACCOUNT_AUTH_TOKEN_TYPE_STRING));
        startActivityForResult(intent, REQUEST_CHOOSE_TYPE);
        this.mPendingRequest = REQUEST_CHOOSE_TYPE;
    }

    private int getItemIndexToSelect(ArrayList<Account> accounts, String selectedAccountName, boolean selectedAddNewAccount) {
        if (selectedAddNewAccount) {
            return accounts.size();
        }
        for (int i = REQUEST_NULL; i < accounts.size(); i += REQUEST_CHOOSE_TYPE) {
            if (((Account) accounts.get(i)).name.equals(selectedAccountName)) {
                return i;
            }
        }
        return SELECTED_ITEM_NONE;
    }

    private String[] getListOfDisplayableOptions(ArrayList<Account> accounts) {
        String[] listItems = new String[((this.mDisallowAddAccounts ? REQUEST_NULL : REQUEST_CHOOSE_TYPE) + accounts.size())];
        for (int i = REQUEST_NULL; i < accounts.size(); i += REQUEST_CHOOSE_TYPE) {
            listItems[i] = ((Account) accounts.get(i)).name;
        }
        if (!this.mDisallowAddAccounts) {
            listItems[accounts.size()] = getResources().getString(17040521);
        }
        return listItems;
    }

    private ArrayList<Account> getAcceptableAccountChoices(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsForPackage(this.mCallingPackage, this.mCallingUid);
        ArrayList<Account> accountsToPopulate = new ArrayList(accounts.length);
        int length = accounts.length;
        for (int i = REQUEST_NULL; i < length; i += REQUEST_CHOOSE_TYPE) {
            Account account = accounts[i];
            if ((this.mSetOfAllowableAccounts == null || this.mSetOfAllowableAccounts.contains(account)) && (this.mSetOfRelevantAccountTypes == null || this.mSetOfRelevantAccountTypes.contains(account.type))) {
                accountsToPopulate.add(account);
            }
        }
        return accountsToPopulate;
    }

    private Set<String> getReleventAccountTypes(Intent intent) {
        String[] allowedAccountTypes = intent.getStringArrayExtra(EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY);
        AuthenticatorDescription[] descs = AccountManager.get(this).getAuthenticatorTypes();
        Set<String> supportedAccountTypes = new HashSet(descs.length);
        int length = descs.length;
        for (int i = REQUEST_NULL; i < length; i += REQUEST_CHOOSE_TYPE) {
            supportedAccountTypes.add(descs[i].type);
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
        TextView descriptionView = (TextView) findViewById(16909109);
        if (TextUtils.isEmpty(descriptionOverride)) {
            descriptionView.setVisibility(8);
        } else {
            descriptionView.setText(descriptionOverride);
        }
    }

    private final void populateUIAccountList(String[] listItems) {
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(new ArrayAdapter(this, R.layout.simple_list_item_single_choice, listItems));
        list.setChoiceMode(REQUEST_CHOOSE_TYPE);
        list.setItemsCanFocus(false);
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                ChooseTypeAndAccountActivity.this.mSelectedItemIndex = position;
                ChooseTypeAndAccountActivity.this.mOkButton.setEnabled(true);
            }
        });
        if (this.mSelectedItemIndex != SELECTED_ITEM_NONE) {
            list.setItemChecked(this.mSelectedItemIndex, true);
            if (Log.isLoggable(TAG, REQUEST_ADD_ACCOUNT)) {
                Log.v(TAG, "List item " + this.mSelectedItemIndex + " should be selected");
            }
        }
    }
}
