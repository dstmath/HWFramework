package ohos.account.app;

import java.util.List;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AppAccountAbility {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final int LOG_DOMAIN = 218110720;
    private static final String TAG = "AppAccountAbility";

    public AppAccountAbility(Context context) {
        AppAccountManager.getInstance().setContext(context);
    }

    public List<AppAccount> getAllAccounts(String str) {
        return AppAccountManager.getInstance().getAllAccounts(str);
    }

    public boolean subscribeAccountEvent(IAppAccountSubscriber iAppAccountSubscriber, List<String> list) {
        HiLog.debug(LABEL, "subscribe account event in", new Object[0]);
        return AppAccountManager.getInstance().subscribeAccountEvent(iAppAccountSubscriber, list);
    }

    public boolean unsubscribeAccountEvent(IAppAccountSubscriber iAppAccountSubscriber) {
        HiLog.debug(LABEL, "unsubscribe account event in", new Object[0]);
        return AppAccountManager.getInstance().unsubscribeAccountEvent(iAppAccountSubscriber);
    }
}
