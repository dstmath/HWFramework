package ohos.account.app.adapter;

import android.accounts.Account;
import android.accounts.IAccountManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import ohos.account.app.AppAccount;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AppAccountManagerAdapter {
    private static final String ACCOUNT_SERVICE_NAME = "account";
    private static final List<AppAccount> FAIL_APP_ACCOUNTS_LIST = new ArrayList();
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final int LOG_DOMAIN = 218110720;
    private static final String TAG = "AppAccountManager";
    private IAccountManager mService = null;

    private IAccountManager getAccountService() {
        if (this.mService == null) {
            try {
                this.mService = IAccountManager.Stub.asInterface(ServiceManager.getServiceOrThrow(ACCOUNT_SERVICE_NAME));
                if (this.mService == null) {
                    HiLog.error(LABEL, "ServiceManager get failed", new Object[0]);
                }
            } catch (ServiceManager.ServiceNotFoundException unused) {
                HiLog.error(LABEL, "get account service not found exception", new Object[0]);
            }
        }
        return this.mService;
    }

    public List<AppAccount> getAllAccounts(String str, String str2) {
        if (str2 == null || str2.isEmpty()) {
            HiLog.error(LABEL, "getAllAccounts callingPackageName error", new Object[0]);
            return FAIL_APP_ACCOUNTS_LIST;
        }
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "getAllAccounts getAccountService failed", new Object[0]);
            return FAIL_APP_ACCOUNTS_LIST;
        }
        try {
            return transAccountsToAppAccounts(Arrays.asList(accountService.getAccounts(str, str2)));
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "getAllAccounts RemoteException", new Object[0]);
            return FAIL_APP_ACCOUNTS_LIST;
        }
    }

    private List<AppAccount> transAccountsToAppAccounts(List<Account> list) {
        ArrayList arrayList = new ArrayList();
        for (Account account : list) {
            arrayList.add(new AppAccount(account.name, account.type));
        }
        return arrayList;
    }

    public boolean registerAccountListener(List<String> list, String str) {
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "registerAccountListener getAccountService failed", new Object[0]);
            return false;
        }
        try {
            accountService.registerAccountListener((String[]) list.toArray(new String[list.size()]), str);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "registerAccountListener RemoteException", new Object[0]);
            return false;
        }
    }

    public boolean unRegisterAccountListener(Set<String> set, String str) {
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "unRegisterAccountListener getAccountService failed", new Object[0]);
            return false;
        }
        String[] strArr = null;
        if (set != null) {
            try {
                strArr = (String[]) set.toArray(new String[set.size()]);
            } catch (RemoteException unused) {
                HiLog.error(LABEL, "registerAccountListener RemoteException", new Object[0]);
                return false;
            }
        }
        accountService.unregisterAccountListener(strArr, str);
        return true;
    }
}
