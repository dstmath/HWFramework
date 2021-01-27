package ohos.account.app.adapter;

import android.accounts.Account;
import android.accounts.IAccountManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import ohos.account.app.AppAccount;
import ohos.account.app.AppAccountConst;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AppAccountManagerAdapter {
    private static final String ACCOUNT_SERVICE_NAME = "account";
    private static final List<AppAccount> FAIL_APP_ACCOUNTS_LIST = new ArrayList();
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final int LOG_DOMAIN = 218110720;
    private static final String TAG = "AppAccountManager";
    private final Object remoteLock = new Object();
    private volatile IAccountManager service = null;

    private IAccountManager getAccountService() {
        if (this.service != null) {
            return this.service;
        }
        synchronized (this.remoteLock) {
            if (this.service == null) {
                try {
                    this.service = IAccountManager.Stub.asInterface(ServiceManager.getServiceOrThrow(ACCOUNT_SERVICE_NAME));
                    if (this.service == null) {
                        HiLog.error(LABEL, "get account service failed", new Object[0]);
                        return this.service;
                    }
                } catch (ServiceManager.ServiceNotFoundException e) {
                    HiLog.error(LABEL, "get account service failed, %{public}s", new Object[]{e.getMessage()});
                    return this.service;
                }
            }
            return this.service;
        }
    }

    public List<AppAccount> getAllAccounts(String str, String str2) {
        if (str2 == null || str2.isEmpty()) {
            HiLog.error(LABEL, "get accounts bundle name error", new Object[0]);
            return FAIL_APP_ACCOUNTS_LIST;
        }
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "get accounts service failed", new Object[0]);
            return FAIL_APP_ACCOUNTS_LIST;
        }
        try {
            return transAccountsToAppAccounts(Arrays.asList(accountService.getAccounts(str, str2)));
        } catch (RemoteException e) {
            HiLog.error(LABEL, "get accounts failed, %{public}s", new Object[]{e.getMessage()});
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

    public boolean subscribeAccountEvent(List<String> list, String str) {
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "subscribe account event service failed", new Object[0]);
            return false;
        } else if (list == null || list.isEmpty()) {
            HiLog.error(LABEL, "subscribe account types is null", new Object[0]);
            return false;
        } else {
            try {
                accountService.registerAccountListener((String[]) list.toArray(new String[list.size()]), str);
                return true;
            } catch (RemoteException e) {
                HiLog.error(LABEL, "register account listener failed, %{public}s", new Object[]{e.getMessage()});
                return false;
            }
        }
    }

    public boolean unsubscribeAccountEvent(Set<String> set, String str) {
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "unsubscribe account event service failed", new Object[0]);
            return false;
        }
        String[] strArr = null;
        if (set != null) {
            try {
                strArr = (String[]) set.toArray(new String[set.size()]);
            } catch (RemoteException e) {
                HiLog.error(LABEL, "unregister account listener failed, %{public}s", new Object[]{e.getMessage()});
                return false;
            }
        }
        accountService.unregisterAccountListener(strArr, str);
        return true;
    }

    public boolean addAccount(String str, String str2, String str3) {
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "add account service failed", new Object[0]);
            return false;
        }
        try {
            return accountService.addAccountExplicitly(new Account(str, str2), str3, (Bundle) null);
        } catch (RemoteException e) {
            HiLog.error(LABEL, "add account failed, %{public}s", new Object[]{e.getMessage()});
            return false;
        }
    }

    public boolean deleteAccount(String str, String str2) {
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "delete account service failed", new Object[0]);
            return false;
        }
        try {
            return accountService.removeAccountExplicitly(new Account(str, str2));
        } catch (RemoteException e) {
            HiLog.error(LABEL, "delete account failed, %{public}s", new Object[]{e.getMessage()});
            return false;
        }
    }

    public boolean setAccountAccess(String str, String str2, String str3, boolean z) {
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "set account accessed service failed", new Object[0]);
            return false;
        }
        try {
            return accountService.setAccountVisibility(new Account(str, str2), str3, z ? 1 : 3);
        } catch (RemoteException e) {
            HiLog.error(LABEL, "set account accessed failed, %{public}s", new Object[]{e.getMessage()});
            return false;
        }
    }

    public boolean setAccountExtraInfo(String str, String str2, String str3) {
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "set account extraInfo service failed", new Object[0]);
            return false;
        }
        try {
            accountService.setPassword(new Account(str, str2), str3);
            return true;
        } catch (RemoteException e) {
            HiLog.error(LABEL, "set account extraInfo failed, %{public}s", new Object[]{e.getMessage()});
            return false;
        }
    }

    public String getAccountExtraInfo(String str, String str2) {
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "get account extraInfo service failed", new Object[0]);
            return AppAccountConst.INVALID_ACCOUNT_INFO;
        }
        try {
            return accountService.getPassword(new Account(str, str2));
        } catch (RemoteException e) {
            HiLog.error(LABEL, "get account extraInfo failed, %{public}s", new Object[]{e.getMessage()});
            return AppAccountConst.INVALID_ACCOUNT_INFO;
        }
    }

    public boolean setAccountCredential(String str, String str2, String str3, String str4) {
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "set credential service failed", new Object[0]);
            return false;
        }
        try {
            accountService.setAuthToken(new Account(str, str2), str3, str4);
            return true;
        } catch (RemoteException e) {
            HiLog.error(LABEL, "set credential failed, %{public}s", new Object[]{e.getMessage()});
            return false;
        }
    }

    public String getAccountCredential(String str, String str2, String str3) {
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "get credential service failed", new Object[0]);
            return AppAccountConst.INVALID_ACCOUNT_INFO;
        }
        try {
            return accountService.peekAuthToken(new Account(str, str2), str3);
        } catch (RemoteException e) {
            HiLog.error(LABEL, "get credential failed, %{public}s", new Object[]{e.getMessage()});
            return AppAccountConst.INVALID_ACCOUNT_INFO;
        }
    }

    public boolean setAssociatedData(String str, String str2, String str3, String str4) {
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "set associated service failed", new Object[0]);
            return false;
        }
        try {
            accountService.setUserData(new Account(str, str2), str3, str4);
            return true;
        } catch (RemoteException e) {
            HiLog.error(LABEL, "set associated failed, %{public}s", new Object[]{e.getMessage()});
            return false;
        }
    }

    public String getAssociatedData(String str, String str2, String str3) {
        IAccountManager accountService = getAccountService();
        if (accountService == null) {
            HiLog.error(LABEL, "get associated service failed", new Object[0]);
            return AppAccountConst.INVALID_ACCOUNT_INFO;
        }
        try {
            return accountService.getUserData(new Account(str, str2), str3);
        } catch (RemoteException e) {
            HiLog.error(LABEL, "get associated failed, %{public}s", new Object[]{e.getMessage()});
            return AppAccountConst.INVALID_ACCOUNT_INFO;
        }
    }
}
