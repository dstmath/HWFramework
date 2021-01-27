package ohos.account.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import ohos.account.app.AppAccountManager;
import ohos.account.app.adapter.AppAccountManagerAdapter;
import ohos.app.Context;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.event.commonevent.CommonEventSupport;
import ohos.event.commonevent.MatchingSkills;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

public class AppAccountManager implements IRemoteBroker {
    private static final AppAccountManagerAdapter APP_ACCOUNT_ADAPTER = new AppAccountManagerAdapter();
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, TAG);
    private static final int LOG_DOMAIN = 218110720;
    private static final String TAG = "AppAccountManager";
    private String accountOwner;
    private final Set<IAppAccountSubscriber> accountsChangedSubscribers;
    private final Map<IAppAccountSubscriber, Set<String>> accountsChangedSubscribersTypes;
    private final Object eventLock;
    private final Object remoteLock;
    private IRemoteObject remoteObj;
    private AccountChangeEventSubscriber subscriber;

    private interface MarshallInterface {
        boolean marshalling(MessageParcel messageParcel);
    }

    private interface UnmarshallingInterface {
        boolean unmarshalling(MessageParcel messageParcel);
    }

    private boolean isInvalidAccountParam(String str) {
        return str == null;
    }

    private AppAccountManager() {
        this.eventLock = new Object();
        this.accountsChangedSubscribers = new HashSet();
        this.accountsChangedSubscribersTypes = new HashMap();
        this.remoteObj = null;
        this.remoteLock = new Object();
    }

    public IRemoteObject asObject() {
        synchronized (this.remoteLock) {
            if (this.remoteObj == null) {
                this.remoteObj = SysAbilityManager.getSysAbility(200);
                if (this.remoteObj == null) {
                    HiLog.error(LABEL, "getSysAbility account failed", new Object[0]);
                    return this.remoteObj;
                }
                this.remoteObj.addDeathRecipient(new AppAccountManagerDeathRecipient(), 0);
                HiLog.debug(LABEL, "get remote object completed", new Object[0]);
            }
            return this.remoteObj;
        }
    }

    public static class AppAccountMgrSingleTon {
        private static AppAccountManager singleTon = new AppAccountManager();

        private AppAccountMgrSingleTon() {
        }
    }

    public static AppAccountManager getInstance() {
        return AppAccountMgrSingleTon.singleTon;
    }

    public void setAccountOwner(Context context) {
        if (context != null) {
            this.accountOwner = context.getBundleName();
        }
    }

    private boolean addSubcribeEvent() {
        HiLog.debug(LABEL, "addSubcribeEvent start", new Object[0]);
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent(CommonEventSupport.COMMON_EVENT_VISIBLE_ACCOUNTS_UPDATED);
        this.subscriber = new AccountChangeEventSubscriber(new CommonEventSubscribeInfo(matchingSkills));
        try {
            CommonEventManager.subscribeCommonEvent(this.subscriber);
            HiLog.debug(LABEL, "CommonEventManager has subscribed CommonEvent", new Object[0]);
            HiLog.debug(LABEL, "addSubcribeEvent end", new Object[0]);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "subscribeCommonEvent occur exception", new Object[0]);
            return false;
        }
    }

    public boolean subscribeAccountEvent(IAppAccountSubscriber iAppAccountSubscriber, List<String> list) {
        HiLog.debug(LABEL, "subscribe app account event start", new Object[0]);
        if (iAppAccountSubscriber == null || this.accountOwner == null || list == null) {
            HiLog.error(LABEL, "subscribe param null", new Object[0]);
            return false;
        }
        synchronized (this.eventLock) {
            if (this.accountsChangedSubscribers.contains(iAppAccountSubscriber)) {
                HiLog.error(LABEL, "this listener is already added", new Object[0]);
                return false;
            } else if (!this.accountsChangedSubscribers.isEmpty() || addSubcribeEvent()) {
                this.accountsChangedSubscribers.add(iAppAccountSubscriber);
                this.accountsChangedSubscribersTypes.put(iAppAccountSubscriber, new HashSet(list));
                return APP_ACCOUNT_ADAPTER.subscribeAccountEvent(list, this.accountOwner);
            } else {
                HiLog.error(LABEL, "subscribe event fail", new Object[0]);
                return false;
            }
        }
    }

    public boolean unsubscribeAccountEvent(IAppAccountSubscriber iAppAccountSubscriber) {
        HiLog.debug(LABEL, "unsubscribe app account event", new Object[0]);
        if (iAppAccountSubscriber == null) {
            HiLog.error(LABEL, "this event is null", new Object[0]);
            return false;
        }
        synchronized (this.eventLock) {
            if (!this.accountsChangedSubscribers.contains(iAppAccountSubscriber)) {
                HiLog.error(LABEL, "event was not previously added", new Object[0]);
                return false;
            }
            Set<String> set = this.accountsChangedSubscribersTypes.get(iAppAccountSubscriber);
            this.accountsChangedSubscribers.remove(iAppAccountSubscriber);
            this.accountsChangedSubscribersTypes.remove(iAppAccountSubscriber);
            if (this.accountsChangedSubscribers.isEmpty()) {
                try {
                    CommonEventManager.unsubscribeCommonEvent(this.subscriber);
                    HiLog.debug(LABEL, "CommonEventManager has unsubscribed CommonEvent", new Object[0]);
                } catch (RemoteException unused) {
                    HiLog.error(LABEL, "unsubscribeCommonEvent occur exception", new Object[0]);
                    return false;
                }
            }
            return APP_ACCOUNT_ADAPTER.unsubscribeAccountEvent(set, this.accountOwner);
        }
    }

    public List<AppAccount> getAllAccounts(String str) {
        HiLog.debug(LABEL, "get all accounts in", new Object[0]);
        String str2 = this.accountOwner;
        if (str2 == null || str == null) {
            return new ArrayList();
        }
        return APP_ACCOUNT_ADAPTER.getAllAccounts(str, str2);
    }

    public boolean addAccount(String str, String str2) {
        HiLog.debug(LABEL, "addAccount in", new Object[0]);
        if (this.accountOwner == null || isInvalidAccountParam(str)) {
            return false;
        }
        return APP_ACCOUNT_ADAPTER.addAccount(str, this.accountOwner, str2);
    }

    public boolean deleteAccount(String str) {
        HiLog.debug(LABEL, "deleteAccount in", new Object[0]);
        if (this.accountOwner == null || isInvalidAccountParam(str)) {
            return false;
        }
        return APP_ACCOUNT_ADAPTER.deleteAccount(str, this.accountOwner);
    }

    public List<AppAccount> getAllAccessibleAccounts() {
        HiLog.debug(LABEL, "get all accessible accounts in", new Object[0]);
        String str = this.accountOwner;
        if (str == null) {
            return new ArrayList();
        }
        return APP_ACCOUNT_ADAPTER.getAllAccounts(null, str);
    }

    public boolean setAccountExtraInfo(String str, String str2) {
        HiLog.debug(LABEL, "set account certification in", new Object[0]);
        if (this.accountOwner != null && !isInvalidAccountParam(str)) {
            return APP_ACCOUNT_ADAPTER.setAccountExtraInfo(str, this.accountOwner, str2);
        }
        HiLog.error(LABEL, "set account certification error", new Object[0]);
        return false;
    }

    public String getAccountExtraInfo(String str) {
        HiLog.debug(LABEL, "get account certification in", new Object[0]);
        if (this.accountOwner != null && !isInvalidAccountParam(str)) {
            return APP_ACCOUNT_ADAPTER.getAccountExtraInfo(str, this.accountOwner);
        }
        HiLog.error(LABEL, "get account certification error", new Object[0]);
        return AppAccountConst.INVALID_ACCOUNT_INFO;
    }

    public boolean setAccountCredential(String str, String str2, String str3) {
        HiLog.debug(LABEL, "set credential in", new Object[0]);
        if (this.accountOwner != null && !isInvalidAccountParam(str) && str2 != null && !str2.isEmpty() && str3 != null && !str3.isEmpty()) {
            return APP_ACCOUNT_ADAPTER.setAccountCredential(str, this.accountOwner, str2, str3);
        }
        HiLog.error(LABEL, "set credential param error", new Object[0]);
        return false;
    }

    public String getAccountCredential(String str, String str2) {
        HiLog.debug(LABEL, "get credential in", new Object[0]);
        if (this.accountOwner != null && !isInvalidAccountParam(str) && str2 != null && !str2.isEmpty()) {
            return APP_ACCOUNT_ADAPTER.getAccountCredential(str, this.accountOwner, str2);
        }
        HiLog.error(LABEL, "get credential param error", new Object[0]);
        return AppAccountConst.INVALID_ACCOUNT_INFO;
    }

    public boolean setAccountAccess(String str, String str2, boolean z) {
        HiLog.debug(LABEL, "set account access in", new Object[0]);
        if (this.accountOwner != null && !isInvalidAccountParam(str) && str2 != null && !str2.isEmpty()) {
            return APP_ACCOUNT_ADAPTER.setAccountAccess(str, this.accountOwner, str2, z);
        }
        HiLog.error(LABEL, "get account access error", new Object[0]);
        return false;
    }

    public boolean setAssociatedData(String str, String str2, String str3) {
        HiLog.debug(LABEL, "set associated in", new Object[0]);
        if (this.accountOwner != null && !isInvalidAccountParam(str) && str2 != null && !str2.isEmpty() && str3 != null && !str3.isEmpty()) {
            return APP_ACCOUNT_ADAPTER.setAssociatedData(str, this.accountOwner, str2, str3);
        }
        HiLog.error(LABEL, "set associated param error", new Object[0]);
        return false;
    }

    public String getAssociatedData(String str, String str2) {
        HiLog.debug(LABEL, "get associated in", new Object[0]);
        if (this.accountOwner != null && !isInvalidAccountParam(str) && str2 != null && !str2.isEmpty()) {
            return APP_ACCOUNT_ADAPTER.getAssociatedData(str, this.accountOwner, str2);
        }
        HiLog.error(LABEL, "get associated param error", new Object[0]);
        return AppAccountConst.INVALID_ACCOUNT_INFO;
    }

    public class AccountChangeEventSubscriber extends CommonEventSubscriber {
        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        AccountChangeEventSubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo) {
            super(commonEventSubscribeInfo);
            AppAccountManager.this = r1;
        }

        @Override // ohos.event.commonevent.CommonEventSubscriber
        public void onReceiveEvent(CommonEventData commonEventData) {
            HiLog.debug(AppAccountManager.LABEL, "onReceiveEvent in", new Object[0]);
            if (AppAccountManager.this.accountOwner == null || AppAccountManager.this.accountOwner.isEmpty()) {
                HiLog.error(AppAccountManager.LABEL, "can't get calling bundle name", new Object[0]);
                return;
            }
            List<AppAccount> allAccessibleAccounts = AppAccountManager.this.getAllAccessibleAccounts();
            synchronized (AppAccountManager.this.eventLock) {
                for (IAppAccountSubscriber iAppAccountSubscriber : AppAccountManager.this.accountsChangedSubscribers) {
                    HiLog.debug(AppAccountManager.LABEL, "before listener onAccountsChanged", new Object[0]);
                    iAppAccountSubscriber.onAccountsChanged((List) allAccessibleAccounts.stream().filter(new Predicate((Set) AppAccountManager.this.accountsChangedSubscribersTypes.get(iAppAccountSubscriber)) {
                        /* class ohos.account.app.$$Lambda$AppAccountManager$AccountChangeEventSubscriber$fwi4WhJpSAK8uGmTNDaqH_zyErI */
                        private final /* synthetic */ Set f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.util.function.Predicate
                        public final boolean test(Object obj) {
                            return AppAccountManager.AccountChangeEventSubscriber.lambda$onReceiveEvent$0(this.f$0, (AppAccount) obj);
                        }
                    }).collect(Collectors.toList()));
                    HiLog.debug(AppAccountManager.LABEL, "after listener onAccountsChanged", new Object[0]);
                }
            }
        }
    }

    private void setRemoteObject(IRemoteObject iRemoteObject) {
        synchronized (this.remoteLock) {
            getInstance().remoteObj = iRemoteObject;
        }
    }

    private static class AppAccountManagerDeathRecipient implements IRemoteObject.DeathRecipient {
        private AppAccountManagerDeathRecipient() {
        }

        public void onRemoteDied() {
            HiLog.warn(AppAccountManager.LABEL, "AppAccountManagerDeathRecipient::onRemoteDied", new Object[0]);
            AppAccountManager.getInstance().setRemoteObject(null);
        }
    }
}
