package ohos.account.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import ohos.aafwk.content.IntentFilter;
import ohos.account.app.AppAccountManager;
import ohos.account.app.adapter.AppAccountManagerAdapter;
import ohos.app.Context;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.event.commonevent.CommonEventSupport;
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
    private static final Object LOCK = new Object();
    private static final int LOG_DOMAIN = 218110720;
    private static final String TAG = "AppAccountManager";
    private final Object REMOTE_LOCK;
    private Context context;
    private final Set<IAppAccountSubscriber> mAccountsChangedListeners;
    private final Map<IAppAccountSubscriber, Set<String>> mAccountsChangedListenersTypes;
    private IRemoteObject remoteObj;
    private AccountChangeEventSubscriber subscriber;

    @FunctionalInterface
    private interface MarshallInterface {
        boolean marshalling(MessageParcel messageParcel);
    }

    @FunctionalInterface
    private interface UnmarshallingInterface {
        boolean unmarshalling(MessageParcel messageParcel);
    }

    private AppAccountManager() {
        this.mAccountsChangedListeners = new HashSet();
        this.mAccountsChangedListenersTypes = new HashMap();
        this.remoteObj = null;
        this.REMOTE_LOCK = new Object();
    }

    public IRemoteObject asObject() {
        synchronized (this.REMOTE_LOCK) {
            if (this.remoteObj == null) {
                this.remoteObj = SysAbilityManager.getSysAbility(200);
                if (this.remoteObj == null) {
                    HiLog.error(LABEL, "getSysAbility account failed", new Object[0]);
                    return this.remoteObj;
                }
                this.remoteObj.addDeathRecipient(new AppAccountManagerDeathRecipient(), 0);
                HiLog.info(LABEL, "get remote object completed", new Object[0]);
            }
            return this.remoteObj;
        }
    }

    /* access modifiers changed from: private */
    public static class AppAccountMgrSingleTon {
        private static AppAccountManager singleTon = new AppAccountManager();

        private AppAccountMgrSingleTon() {
        }
    }

    public static AppAccountManager getInstance() {
        return AppAccountMgrSingleTon.singleTon;
    }

    public void setContext(Context context2) {
        this.context = context2;
    }

    private boolean addSubcribeEvent() {
        HiLog.info(LABEL, "addSubcribeEvent start", new Object[0]);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CommonEventSupport.COMMON_EVENT_VISIBLE_ACCOUNTS_UPDATED);
        this.subscriber = new AccountChangeEventSubscriber(new CommonEventSubscribeInfo(intentFilter));
        try {
            CommonEventManager.subscribeCommonEvent(this.subscriber);
            HiLog.debug(LABEL, "CommonEventManager has subscribed CommonEvent", new Object[0]);
            HiLog.info(LABEL, "addSubcribeEvent end", new Object[0]);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "subscribeCommonEvent occur exception", new Object[0]);
            return false;
        }
    }

    public boolean subscribeAccountEvent(IAppAccountSubscriber iAppAccountSubscriber, List<String> list) {
        HiLog.info(LABEL, "subscribe app account event start", new Object[0]);
        if (iAppAccountSubscriber == null || this.context == null || list == null) {
            HiLog.error(LABEL, "subscribe event, context or types is null", new Object[0]);
            return false;
        }
        synchronized (LOCK) {
            if (this.mAccountsChangedListeners.contains(iAppAccountSubscriber)) {
                HiLog.error(LABEL, "this listener is already added", new Object[0]);
                return false;
            } else if (!this.mAccountsChangedListeners.isEmpty() || addSubcribeEvent()) {
                this.mAccountsChangedListeners.add(iAppAccountSubscriber);
                this.mAccountsChangedListenersTypes.put(iAppAccountSubscriber, new HashSet(list));
                return APP_ACCOUNT_ADAPTER.registerAccountListener(list, this.context.getBundleName());
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
        synchronized (LOCK) {
            if (!this.mAccountsChangedListeners.contains(iAppAccountSubscriber)) {
                HiLog.error(LABEL, "event was not previously added", new Object[0]);
                return false;
            }
            Set<String> set = this.mAccountsChangedListenersTypes.get(iAppAccountSubscriber);
            this.mAccountsChangedListeners.remove(iAppAccountSubscriber);
            this.mAccountsChangedListenersTypes.remove(iAppAccountSubscriber);
            if (this.mAccountsChangedListeners.isEmpty()) {
                try {
                    CommonEventManager.unsubscribeCommonEvent(this.subscriber);
                    HiLog.debug(LABEL, "CommonEventManager has unsubscribed CommonEvent", new Object[0]);
                } catch (RemoteException unused) {
                    HiLog.error(LABEL, "unsubscribeCommonEvent occur exception", new Object[0]);
                    return false;
                }
            }
            return APP_ACCOUNT_ADAPTER.unRegisterAccountListener(set, this.context.getBundleName());
        }
    }

    public List<AppAccount> getAllAccounts(String str) {
        HiLog.debug(LABEL, "getAllAccounts in", new Object[0]);
        Context context2 = this.context;
        if (context2 == null) {
            return new ArrayList();
        }
        return APP_ACCOUNT_ADAPTER.getAllAccounts(str, context2.getBundleName());
    }

    /* access modifiers changed from: private */
    public class AccountChangeEventSubscriber extends CommonEventSubscriber {
        AccountChangeEventSubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo) {
            super(commonEventSubscribeInfo);
        }

        @Override // ohos.event.commonevent.CommonEventSubscriber
        public void onReceiveEvent(CommonEventData commonEventData) {
            HiLog.info(AppAccountManager.LABEL, "onReceiveEvent in", new Object[0]);
            if (AppAccountManager.this.context == null || AppAccountManager.this.context.getBundleName() == null || AppAccountManager.this.context.getBundleName().isEmpty()) {
                HiLog.error(AppAccountManager.LABEL, "can't get calling package name", new Object[0]);
                return;
            }
            List<AppAccount> allAccounts = AppAccountManager.this.getAllAccounts(null);
            synchronized (AppAccountManager.LOCK) {
                for (IAppAccountSubscriber iAppAccountSubscriber : AppAccountManager.this.mAccountsChangedListeners) {
                    HiLog.info(AppAccountManager.LABEL, "before listener onAccountsChanged", new Object[0]);
                    iAppAccountSubscriber.onAccountsChanged((List) allAccounts.stream().filter(new Predicate((Set) AppAccountManager.this.mAccountsChangedListenersTypes.get(iAppAccountSubscriber)) {
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
                    HiLog.info(AppAccountManager.LABEL, "after listener onAccountsChanged", new Object[0]);
                }
            }
        }

        static /* synthetic */ boolean lambda$onReceiveEvent$0(Set set, AppAccount appAccount) {
            return set.contains(appAccount.getType());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRemoteObject(IRemoteObject iRemoteObject) {
        synchronized (this.REMOTE_LOCK) {
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
