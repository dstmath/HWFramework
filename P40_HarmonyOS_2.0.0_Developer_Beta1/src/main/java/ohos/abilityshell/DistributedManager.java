package ohos.abilityshell;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import ohos.aafwk.content.Intent;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ShellInfo;
import ohos.hiviewdfx.HiLogLabel;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.net.Uri;

public class DistributedManager implements IDistributedManager, IRemoteBroker {
    private static final int CONNECT_ABILITY_TRANSACTION = 6;
    private static final int CONTINUE_ABILITY_TRANSACTION = 16;
    private static final int DISCONNECT_ABILITY_TRANSACTION = 7;
    private static final String DISTRIBUTE_SCHEDULE_TOKEN = "ohos.distributedschedule.accessToken";
    private static final int ERROR = -1;
    private static final int FETCH_ABILITIES = 19;
    private static final int GET_REMOTE_DATA_ABILITY_TRANSACTION = 31;
    private static final int MAX_FETCH_ABILITIES = 200;
    private static final int NOTIFY_COMPLETE_CONTINUATION_TRANSACTION = 12;
    private static final int REGISTER_ABILITY_TOKEN_TRANSACTION = 14;
    private static final int SELECT_ABILITY_TRANSACTION = 10;
    private static final int SELECT_URI_TRANSACTION = 30;
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private static final int START_ABILITY_TRANSACTION = 1;
    private static final int START_CONTINUATION_TRANSACTION = 11;
    private static final int STOP_ABILITY_TRANSACTION = 3;
    private static final long TIMEOUT_MS_WAIT_SEND_REQUEST = 5000;
    private static final int UNREGISTER_ABILITY_TOKEN_TRANSACTION = 15;
    private static final int URI_LOCAL = 0;
    private static final int URI_REMOTE = 1;
    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private IRemoteObject remote = null;

    public DistributedManager(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int startRemoteAbility(Intent intent, AbilityInfo abilityInfo, int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN)) {
            return -1;
        }
        obtain.writeSequenceable(intent);
        obtain.writeSequenceable(abilityInfo);
        if (!obtain.writeInt(i)) {
            return -1;
        }
        AppLog.d(SHELL_LABEL, "DistributedManager::startRemoteAbility sendRequest", new Object[0]);
        Integer sendRequest = sendRequest(1, obtain);
        if (sendRequest == null) {
            return -1;
        }
        return sendRequest.intValue();
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int stopRemoteAbility(Intent intent, AbilityInfo abilityInfo) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN)) {
            return -1;
        }
        obtain.writeSequenceable(intent);
        obtain.writeSequenceable(abilityInfo);
        AppLog.d(SHELL_LABEL, "DistributedManager::stopRemoteAbility sendRequest", new Object[0]);
        Integer sendRequest = sendRequest(3, obtain);
        if (sendRequest == null) {
            return -1;
        }
        return sendRequest.intValue();
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int connectRemoteAbility(Intent intent, AbilityInfo abilityInfo, IRemoteObject iRemoteObject) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN)) {
            return -1;
        }
        obtain.writeSequenceable(intent);
        obtain.writeSequenceable(abilityInfo);
        if (!obtain.writeRemoteObject(iRemoteObject)) {
            return -1;
        }
        AppLog.d(SHELL_LABEL, "DistributedManager::connectRemoteAbility sendRequest", new Object[0]);
        Integer sendRequest = sendRequest(6, obtain);
        if (sendRequest == null) {
            return -1;
        }
        return sendRequest.intValue();
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int disconnectRemoteAbility(IRemoteObject iRemoteObject) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN) || !obtain.writeRemoteObject(iRemoteObject)) {
            return -1;
        }
        AppLog.d(SHELL_LABEL, "DistributedManager::disconnectRemoteAbility sendRequest", new Object[0]);
        Integer sendRequest = sendRequest(7, obtain);
        if (sendRequest == null) {
            return -1;
        }
        return sendRequest.intValue();
    }

    @Override // ohos.abilityshell.IDistributedManager
    public AbilityShellData selectAbility(Intent intent) throws RemoteException {
        if (this.remote == null) {
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN)) {
            return null;
        }
        obtain.writeSequenceable(intent);
        AppLog.d(SHELL_LABEL, "DistributedManager::selectAbility sendRequest", new Object[0]);
        FutureTask futureTask = new FutureTask(new Callable(obtain, obtain2, messageOption) {
            /* class ohos.abilityshell.$$Lambda$DistributedManager$BpRJQA572Uwd_v0IR38qr7m6n4A */
            private final /* synthetic */ MessageParcel f$1;
            private final /* synthetic */ MessageParcel f$2;
            private final /* synthetic */ MessageOption f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return DistributedManager.this.lambda$selectAbility$0$DistributedManager(this.f$1, this.f$2, this.f$3);
            }
        });
        this.cachedThreadPool.execute(futureTask);
        try {
            return (AbilityShellData) futureTask.get(TIMEOUT_MS_WAIT_SEND_REQUEST, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException unused) {
            AppLog.e(SHELL_LABEL, "DistributedManager::selectAbility sendRequest failed", new Object[0]);
            return null;
        }
    }

    public /* synthetic */ AbilityShellData lambda$selectAbility$0$DistributedManager(MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws Exception {
        try {
            if (!this.remote.sendRequest(10, messageParcel, messageParcel2, messageOption)) {
                AppLog.e(SHELL_LABEL, "DistributedManager::selectAbility sendRequest failed", new Object[0]);
            } else {
                boolean readBoolean = messageParcel2.readBoolean();
                AbilityInfo abilityInfo = new AbilityInfo();
                if (messageParcel2.readSequenceable(abilityInfo)) {
                    ShellInfo shellInfo = new ShellInfo();
                    if (messageParcel2.readSequenceable(shellInfo)) {
                        AbilityShellData abilityShellData = new AbilityShellData(readBoolean, abilityInfo, shellInfo);
                        messageParcel.reclaim();
                        messageParcel2.reclaim();
                        return abilityShellData;
                    }
                }
            }
            return null;
        } finally {
            messageParcel.reclaim();
            messageParcel2.reclaim();
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public List<AbilityShellData> fetchAbilities(Intent intent) throws RemoteException {
        if (this.remote == null) {
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN)) {
            return null;
        }
        obtain.writeSequenceable(intent);
        AppLog.d(SHELL_LABEL, "DistributedManager::fetchAbilities sendRequest", new Object[0]);
        FutureTask futureTask = new FutureTask(new Callable(obtain, obtain2, messageOption) {
            /* class ohos.abilityshell.$$Lambda$DistributedManager$XIMGMXdjWn8EamMEkU_13HZyZJs */
            private final /* synthetic */ MessageParcel f$1;
            private final /* synthetic */ MessageParcel f$2;
            private final /* synthetic */ MessageOption f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return DistributedManager.this.lambda$fetchAbilities$1$DistributedManager(this.f$1, this.f$2, this.f$3);
            }
        });
        this.cachedThreadPool.execute(futureTask);
        try {
            return (List) futureTask.get(TIMEOUT_MS_WAIT_SEND_REQUEST, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException unused) {
            AppLog.e(SHELL_LABEL, "DistributedManager::fetchAbilities sendRequest failed", new Object[0]);
            return null;
        }
    }

    public /* synthetic */ List lambda$fetchAbilities$1$DistributedManager(MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws Exception {
        try {
            boolean sendRequest = this.remote.sendRequest(19, messageParcel, messageParcel2, messageOption);
            ArrayList arrayList = null;
            if (!sendRequest) {
                AppLog.e(SHELL_LABEL, "DistributedManager::fetchAbilities sendRequest failed", new Object[0]);
            } else {
                int readInt = messageParcel2.readInt();
                if (readInt <= 0 || readInt >= 200) {
                    AppLog.w(SHELL_LABEL, "DistributedManager::fetch Abilities list size is %d", Integer.valueOf(readInt));
                } else {
                    ArrayList arrayList2 = new ArrayList(readInt);
                    for (int i = 0; i < readInt; i++) {
                        boolean readBoolean = messageParcel2.readBoolean();
                        String readString = messageParcel2.readString();
                        AbilityInfo abilityInfo = new AbilityInfo();
                        if (messageParcel2.readSequenceable(abilityInfo)) {
                            ShellInfo shellInfo = new ShellInfo();
                            if (messageParcel2.readSequenceable(shellInfo)) {
                                AbilityShellData abilityShellData = new AbilityShellData(readBoolean, abilityInfo, shellInfo);
                                abilityShellData.setDeviceName(readString);
                                arrayList2.add(abilityShellData);
                            }
                        }
                    }
                    arrayList = arrayList2;
                }
                messageParcel.reclaim();
                messageParcel2.reclaim();
                return arrayList;
            }
            return null;
        } finally {
            messageParcel.reclaim();
            messageParcel2.reclaim();
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public Integer registerAbilityToken(IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2) throws RemoteException {
        if (this.remote == null) {
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN) || !obtain.writeRemoteObject(iRemoteObject) || !obtain.writeRemoteObject(iRemoteObject2)) {
            return null;
        }
        AppLog.d(SHELL_LABEL, "DistributedManager::registerAbilityToken sendRequest", new Object[0]);
        return sendRequest(14, obtain);
    }

    @Override // ohos.abilityshell.IDistributedManager
    public Integer unregisterAbilityToken(IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2) throws RemoteException {
        if (this.remote == null) {
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN) || !obtain.writeRemoteObject(iRemoteObject) || !obtain.writeRemoteObject(iRemoteObject2)) {
            return null;
        }
        AppLog.d(SHELL_LABEL, "DistributedManager::unregisterAbilityToken sendRequest", new Object[0]);
        return sendRequest(15, obtain);
    }

    @Override // ohos.abilityshell.IDistributedManager
    public Integer continueAbility(IRemoteObject iRemoteObject, String str, Intent intent) throws RemoteException {
        if (this.remote == null) {
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN) || !obtain.writeRemoteObject(iRemoteObject) || !obtain.writeString(str)) {
            return null;
        }
        obtain.writeSequenceable(intent);
        AppLog.d(SHELL_LABEL, "DistributedManager::continueAbility sendRequest", new Object[0]);
        return sendRequest(16, obtain);
    }

    @Override // ohos.abilityshell.IDistributedManager
    public Integer startContinuation(Intent intent, AbilityInfo abilityInfo, IRemoteObject iRemoteObject) throws RemoteException {
        if (this.remote == null) {
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN)) {
            return null;
        }
        obtain.writeSequenceable(intent);
        obtain.writeSequenceable(abilityInfo);
        if (!obtain.writeRemoteObject(iRemoteObject)) {
            return null;
        }
        AppLog.d(SHELL_LABEL, "DistributedManager::startContinuation sendRequest", new Object[0]);
        return sendRequest(11, obtain);
    }

    @Override // ohos.abilityshell.IDistributedManager
    public void notifyCompleteContinuation(String str, int i, boolean z, IRemoteObject iRemoteObject) throws RemoteException {
        if (this.remote != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN) || !obtain.writeString(str) || !obtain.writeInt(i) || !obtain.writeBoolean(z)) {
                return;
            }
            if (iRemoteObject == null || obtain.writeRemoteObject(iRemoteObject)) {
                AppLog.d(SHELL_LABEL, "DistributedManager::notifyCompleteContinuation sendRequest", new Object[0]);
                FutureTask futureTask = new FutureTask(new Callable(obtain, obtain2, messageOption) {
                    /* class ohos.abilityshell.$$Lambda$DistributedManager$UUT23_y2MFVRK1TS_SyEVZDNjrA */
                    private final /* synthetic */ MessageParcel f$1;
                    private final /* synthetic */ MessageParcel f$2;
                    private final /* synthetic */ MessageOption f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    @Override // java.util.concurrent.Callable
                    public final Object call() {
                        return DistributedManager.this.lambda$notifyCompleteContinuation$2$DistributedManager(this.f$1, this.f$2, this.f$3);
                    }
                });
                this.cachedThreadPool.execute(futureTask);
                try {
                    futureTask.get(TIMEOUT_MS_WAIT_SEND_REQUEST, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException unused) {
                    AppLog.e(SHELL_LABEL, "DistributedManager::notifyCompleteContinuation sendRequest failed", new Object[0]);
                }
            } else {
                AppLog.e(SHELL_LABEL, "DistributedManager: Write reverse scheduler to parcel fail.", new Object[0]);
            }
        }
    }

    public /* synthetic */ Boolean lambda$notifyCompleteContinuation$2$DistributedManager(MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws Exception {
        Boolean.valueOf(false);
        try {
            Boolean valueOf = Boolean.valueOf(this.remote.sendRequest(12, messageParcel, messageParcel2, messageOption));
            if (!valueOf.booleanValue()) {
                AppLog.e(SHELL_LABEL, "DistributedManager::notifyCompleteContinuation sendRequest failed", new Object[0]);
            }
            return valueOf;
        } finally {
            messageParcel.reclaim();
            messageParcel2.reclaim();
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int selectUri(Uri uri) throws RemoteException {
        if (this.remote == null) {
            return -1;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN)) {
            return -1;
        }
        obtain.writeString(uri.toString());
        AppLog.d(SHELL_LABEL, "DistributedManager::selectUri sendRequest", new Object[0]);
        FutureTask futureTask = new FutureTask(new Callable(obtain, obtain2, messageOption) {
            /* class ohos.abilityshell.$$Lambda$DistributedManager$vfY8a10s6a_xe8MnuBqX8E_vg */
            private final /* synthetic */ MessageParcel f$1;
            private final /* synthetic */ MessageParcel f$2;
            private final /* synthetic */ MessageOption f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return DistributedManager.this.lambda$selectUri$3$DistributedManager(this.f$1, this.f$2, this.f$3);
            }
        });
        this.cachedThreadPool.execute(futureTask);
        try {
            return ((Integer) futureTask.get(TIMEOUT_MS_WAIT_SEND_REQUEST, TimeUnit.MILLISECONDS)).intValue();
        } catch (InterruptedException | ExecutionException | TimeoutException unused) {
            AppLog.e(SHELL_LABEL, "DistributedManager::selectUri sendRequest failed", new Object[0]);
            return -1;
        }
    }

    public /* synthetic */ Integer lambda$selectUri$3$DistributedManager(MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws Exception {
        int i;
        try {
            if (!this.remote.sendRequest(30, messageParcel, messageParcel2, messageOption)) {
                AppLog.e(SHELL_LABEL, "DistributedManager::selectUri sendRequest failed", new Object[0]);
                i = -1;
            } else {
                int readInt = messageParcel2.readInt();
                if (readInt != 0) {
                    AppLog.e(SHELL_LABEL, "DistributedManager::selectUri failed code is %{public}d", Integer.valueOf(readInt));
                    i = -1;
                } else {
                    boolean readBoolean = messageParcel2.readBoolean();
                    messageParcel.reclaim();
                    messageParcel2.reclaim();
                    return readBoolean ? 0 : 1;
                }
            }
            return i;
        } finally {
            messageParcel.reclaim();
            messageParcel2.reclaim();
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int getRemoteDataAbility(Uri uri, IRemoteObject iRemoteObject) throws RemoteException {
        if (this.remote == null) {
            return -1;
        }
        MessageParcel obtain = MessageParcel.obtain();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN)) {
            return -1;
        }
        obtain.writeString(uri.toString());
        if (!obtain.writeRemoteObject(iRemoteObject)) {
            return -1;
        }
        AppLog.d(SHELL_LABEL, "DistributedManager::getRemoteDataAbility sendRequest", new Object[0]);
        Integer sendRequest = sendRequest(31, obtain);
        if (sendRequest == null) {
            return -1;
        }
        return sendRequest.intValue();
    }

    private Integer sendRequest(int i, MessageParcel messageParcel) throws RemoteException {
        if (this.remote == null) {
            return null;
        }
        FutureTask futureTask = new FutureTask(new Callable(HiTrace.getId(), i, messageParcel, MessageParcel.obtain(), new MessageOption()) {
            /* class ohos.abilityshell.$$Lambda$DistributedManager$zGD7eNm3t6gufE0giJywgUbP7Ww */
            private final /* synthetic */ HiTraceId f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ MessageParcel f$3;
            private final /* synthetic */ MessageParcel f$4;
            private final /* synthetic */ MessageOption f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return DistributedManager.this.lambda$sendRequest$4$DistributedManager(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        });
        this.cachedThreadPool.execute(futureTask);
        try {
            return (Integer) futureTask.get(TIMEOUT_MS_WAIT_SEND_REQUEST, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException unused) {
            AppLog.e(SHELL_LABEL, "DistributedManager::sendRequest failed", new Object[0]);
            return null;
        }
    }

    public /* synthetic */ Integer lambda$sendRequest$4$DistributedManager(HiTraceId hiTraceId, int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws Exception {
        Integer num;
        try {
            HiTrace.setId(hiTraceId);
            if (this.remote.sendRequest(i, messageParcel, messageParcel2, messageOption)) {
                num = Integer.valueOf(messageParcel2.readInt());
                if (num.intValue() != 0) {
                    AppLog.w(SHELL_LABEL, "DistributedManager::sendRequest result code is %{public}d", num);
                }
            } else {
                AppLog.e(SHELL_LABEL, "DistributedManager::sendRequest failed", new Object[0]);
                num = null;
            }
            return num;
        } finally {
            messageParcel.reclaim();
            messageParcel2.reclaim();
            HiTrace.clearId();
        }
    }
}
