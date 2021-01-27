package ohos.account.app;

import java.util.List;
import ohos.app.Context;
import ohos.hiviewdfx.HiLogLabel;

public class AppAccountAbility {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final int LOG_DOMAIN = 218110720;
    private static final String TAG = "AppAccountAbility";

    public AppAccountAbility(Context context) {
        AppAccountManager.getInstance().setAccountOwner(context);
    }

    public List<AppAccount> getAllAccounts(String str) {
        return AppAccountManager.getInstance().getAllAccounts(str);
    }

    public boolean subscribeAccountEvent(IAppAccountSubscriber iAppAccountSubscriber, List<String> list) {
        return AppAccountManager.getInstance().subscribeAccountEvent(iAppAccountSubscriber, list);
    }

    public boolean unsubscribeAccountEvent(IAppAccountSubscriber iAppAccountSubscriber) {
        return AppAccountManager.getInstance().unsubscribeAccountEvent(iAppAccountSubscriber);
    }

    public boolean addAccount(String str, String str2) {
        return AppAccountManager.getInstance().addAccount(str, str2);
    }

    public boolean deleteAccount(String str) {
        return AppAccountManager.getInstance().deleteAccount(str);
    }

    public List<AppAccount> getAllAccessibleAccounts() {
        return AppAccountManager.getInstance().getAllAccessibleAccounts();
    }

    public boolean enableAppAccess(String str, String str2) {
        return AppAccountManager.getInstance().setAccountAccess(str, str2, true);
    }

    public boolean disableAppAccess(String str, String str2) {
        return AppAccountManager.getInstance().setAccountAccess(str, str2, false);
    }

    public boolean setAccountExtraInfo(String str, String str2) {
        return AppAccountManager.getInstance().setAccountExtraInfo(str, str2);
    }

    public String getAccountExtraInfo(String str) {
        return AppAccountManager.getInstance().getAccountExtraInfo(str);
    }

    public boolean setAccountCredential(String str, String str2, String str3) {
        return AppAccountManager.getInstance().setAccountCredential(str, str2, str3);
    }

    public String getAccountCredential(String str, String str2) {
        return AppAccountManager.getInstance().getAccountCredential(str, str2);
    }

    public boolean setAssociatedData(String str, String str2, String str3) {
        return AppAccountManager.getInstance().setAssociatedData(str, str2, str3);
    }

    public String getAssociatedData(String str, String str2) {
        return AppAccountManager.getInstance().getAssociatedData(str, str2);
    }
}
