package ohos.distributedschedule.scenario;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

/* access modifiers changed from: package-private */
public final class DistributedScenarioManagerClient {
    private static final int CODE_DTB_SCENARIO_MGR_BASE = 1;
    private static final int CODE_SUBSCRIBE = 1;
    private static final int CODE_UNSUBSCRIBE = 2;
    private static final int DEATH_RECIPIENT_FLAG = 0;
    private static final DistributedScenarioManagerClient INSTANCE = new DistributedScenarioManagerClient();
    private static final String IPC_DESCRIPTOR = "OHOS.DistributedSchedule.DtbContinueMgr";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109952, TAG);
    private static final int RESUME_DELAY_TIME = 2000;
    private static final int RETRY_TIME = 5;
    private static final String TAG = "DistributedScenarioManagerClient";
    private final IRemoteObject.DeathRecipient deathRecipient = new DistributedScenarioManagerDeathRecipient();
    private final Object remoteLock = new Object();
    private IRemoteObject remoteObj;
    private final Map<IScenarioSubscriber, SubscribeInfo> subscriberCache = new HashMap();

    static DistributedScenarioManagerClient getInstance() {
        return INSTANCE;
    }

    /* access modifiers changed from: package-private */
    public boolean subscribe(ScenarioSubscriber scenarioSubscriber) {
        if (scenarioSubscriber == null || scenarioSubscriber.getSubscribeInfo() == null) {
            HiLog.warn(LABEL, "subscribe with illegal subscriber.", new Object[0]);
            return false;
        }
        IRemoteObject remote = getRemote();
        if (remote != null) {
            return lambda$resumeSubscribers$0$DistributedScenarioManagerClient(remote, scenarioSubscriber.getSubscriber(), scenarioSubscriber.getSubscribeInfo());
        }
        HiLog.debug(LABEL, "subscribe remote is null.", new Object[0]);
        return false;
    }

    /* access modifiers changed from: package-private */
    public void unsubscribe(ScenarioSubscriber scenarioSubscriber) {
        if (scenarioSubscriber == null) {
            HiLog.warn(LABEL, "unsubscribe null subscriber.", new Object[0]);
            return;
        }
        removeSubscriberCacheInfo(scenarioSubscriber.getSubscriber());
        IRemoteObject remote = getRemote();
        if (remote == null) {
            HiLog.debug(LABEL, "unsubscribe remote is null.", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IPC_DESCRIPTOR)) {
                HiLog.debug(LABEL, "unsubscribe writeInterfaceToken failed.", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
            } else if (!obtain.writeRemoteObject(scenarioSubscriber.getSubscriber().asObject())) {
                HiLog.debug(LABEL, "unsubscribe write subscriber failed.", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
            } else {
                if (!remote.sendRequest(2, obtain, obtain2, messageOption)) {
                    HiLog.debug(LABEL, "unsubscribe sendRequest failed.", new Object[0]);
                }
                obtain.reclaim();
                obtain2.reclaim();
            }
        } catch (RemoteException unused) {
            HiLog.debug(LABEL, "unsubscribe remote exception", new Object[0]);
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: subscribeInner */
    public boolean lambda$resumeSubscribers$0$DistributedScenarioManagerClient(IRemoteObject iRemoteObject, IScenarioSubscriber iScenarioSubscriber, SubscribeInfo subscribeInfo) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            if (!obtain.writeInterfaceToken(IPC_DESCRIPTOR)) {
                HiLog.debug(LABEL, "subscribe writeInterfaceToken failed.", new Object[0]);
            } else if (!obtain.writeRemoteObject(iScenarioSubscriber.asObject())) {
                HiLog.debug(LABEL, "subscribe write subscriber failed.", new Object[0]);
            } else if (!subscribeInfo.marshalling(obtain)) {
                HiLog.debug(LABEL, "subscribe write info failed.", new Object[0]);
            } else if (!iRemoteObject.sendRequest(1, obtain, obtain2, messageOption)) {
                HiLog.debug(LABEL, "subscribe sendRequest failed.", new Object[0]);
            } else {
                boolean readBoolean = obtain2.readBoolean();
                if (readBoolean) {
                    updateSubscriberCacheInfo(iScenarioSubscriber, subscribeInfo);
                }
                obtain.reclaim();
                obtain2.reclaim();
                return readBoolean;
            }
        } catch (RemoteException unused) {
            HiLog.debug(LABEL, "subscribe remote exception", new Object[0]);
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
        obtain.reclaim();
        obtain2.reclaim();
        return false;
    }

    /* access modifiers changed from: package-private */
    public void resumeSubscribers() {
        resetRemote();
        int i = 5;
        while (true) {
            int i2 = i - 1;
            if (i > 0) {
                HiLog.debug(LABEL, "waiting dtb scenario mgr restart.", new Object[0]);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException unused) {
                    HiLog.warn(LABEL, "resume subscriber interrupted exception.", new Object[0]);
                }
                IRemoteObject remote = getRemote();
                if (remote != null) {
                    synchronized (this.subscriberCache) {
                        this.subscriberCache.forEach(new BiConsumer(remote) {
                            /* class ohos.distributedschedule.scenario.$$Lambda$DistributedScenarioManagerClient$IcmOXyHVWVhbckt7SSBhAnD7EU */
                            private final /* synthetic */ IRemoteObject f$1;

                            {
                                this.f$1 = r2;
                            }

                            @Override // java.util.function.BiConsumer
                            public final void accept(Object obj, Object obj2) {
                                DistributedScenarioManagerClient.this.lambda$resumeSubscribers$0$DistributedScenarioManagerClient(this.f$1, (IScenarioSubscriber) obj, (SubscribeInfo) obj2);
                            }
                        });
                    }
                    return;
                }
                i = i2;
            } else {
                HiLog.debug(LABEL, "resume subscriber finally failed.", new Object[0]);
                return;
            }
        }
    }

    private IRemoteObject getRemote() {
        synchronized (this.remoteLock) {
            if (this.remoteObj != null) {
                return this.remoteObj;
            }
            this.remoteObj = SysAbilityManager.getSysAbility(1403);
            if (this.remoteObj == null) {
                HiLog.warn(LABEL, "getSysAbility failed.", new Object[0]);
                return this.remoteObj;
            }
            this.remoteObj.addDeathRecipient(this.deathRecipient, 0);
            HiLog.info(LABEL, "getSysAbility successfully.", new Object[0]);
            return this.remoteObj;
        }
    }

    private void resetRemote() {
        synchronized (this.remoteLock) {
            this.remoteObj = null;
        }
    }

    private void updateSubscriberCacheInfo(IScenarioSubscriber iScenarioSubscriber, SubscribeInfo subscribeInfo) {
        synchronized (this.subscriberCache) {
            this.subscriberCache.put(iScenarioSubscriber, new SubscribeInfo(subscribeInfo));
        }
    }

    private void removeSubscriberCacheInfo(IScenarioSubscriber iScenarioSubscriber) {
        synchronized (this.subscriberCache) {
            this.subscriberCache.remove(iScenarioSubscriber);
        }
    }

    private DistributedScenarioManagerClient() {
    }
}
