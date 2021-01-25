package ohos.account.app;

import java.util.List;

public interface IAppAccountSubscriber {
    void onAccountsChanged(List<AppAccount> list);
}
