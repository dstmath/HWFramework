package ohos.abilityshell;

import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.content.Intent;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ShellInfo;
import ohos.hiviewdfx.HiLogLabel;
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
    private static final int UNREGISTER_ABILITY_TOKEN_TRANSACTION = 15;
    private static final int URI_LOCAL = 0;
    private static final int URI_REMOTE = 1;
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
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN)) {
            return -1;
        }
        obtain.writeSequenceable(intent);
        obtain.writeSequenceable(abilityInfo);
        if (!obtain.writeInt(i)) {
            return -1;
        }
        return sendRequest(1, obtain, obtain2, messageOption);
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int stopRemoteAbility(Intent intent, AbilityInfo abilityInfo) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN)) {
            return -1;
        }
        obtain.writeSequenceable(intent);
        obtain.writeSequenceable(abilityInfo);
        return sendRequest(3, obtain, obtain2, messageOption);
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int connectRemoteAbility(Intent intent, AbilityInfo abilityInfo, IRemoteObject iRemoteObject) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN)) {
            return -1;
        }
        obtain.writeSequenceable(intent);
        obtain.writeSequenceable(abilityInfo);
        if (!obtain.writeRemoteObject(iRemoteObject)) {
            return -1;
        }
        return sendRequest(6, obtain, obtain2, messageOption);
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int disconnectRemoteAbility(IRemoteObject iRemoteObject) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN) && obtain.writeRemoteObject(iRemoteObject)) {
            return sendRequest(7, obtain, obtain2, messageOption);
        }
        return -1;
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
        try {
            if (!this.remote.sendRequest(10, obtain, obtain2, messageOption)) {
                AppLog.e(SHELL_LABEL, "DistributedManager::selectAbility sendRequest failed", new Object[0]);
            } else {
                boolean readBoolean = obtain2.readBoolean();
                AbilityInfo abilityInfo = new AbilityInfo();
                if (obtain2.readSequenceable(abilityInfo)) {
                    ShellInfo shellInfo = new ShellInfo();
                    if (obtain2.readSequenceable(shellInfo)) {
                        AbilityShellData abilityShellData = new AbilityShellData(readBoolean, abilityInfo, shellInfo);
                        obtain.reclaim();
                        obtain2.reclaim();
                        return abilityShellData;
                    }
                }
            }
            return null;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public List<AbilityShellData> fetchAbilities(Intent intent) throws RemoteException {
        ArrayList arrayList = null;
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
        try {
            if (!this.remote.sendRequest(19, obtain, obtain2, messageOption)) {
                AppLog.e(SHELL_LABEL, "DistributedManager::fetchAbilities sendRequest failed", new Object[0]);
            } else {
                int readInt = obtain2.readInt();
                if (readInt <= 0 || readInt >= 200) {
                    AppLog.w(SHELL_LABEL, "DistributedManager::fetch Abilities list size is %d", Integer.valueOf(readInt));
                } else {
                    ArrayList arrayList2 = new ArrayList(readInt);
                    for (int i = 0; i < readInt; i++) {
                        boolean readBoolean = obtain2.readBoolean();
                        String readString = obtain2.readString();
                        AbilityInfo abilityInfo = new AbilityInfo();
                        if (obtain2.readSequenceable(abilityInfo)) {
                            ShellInfo shellInfo = new ShellInfo();
                            if (obtain2.readSequenceable(shellInfo)) {
                                AbilityShellData abilityShellData = new AbilityShellData(readBoolean, abilityInfo, shellInfo);
                                abilityShellData.setDeviceName(readString);
                                arrayList2.add(abilityShellData);
                            }
                        }
                    }
                    arrayList = arrayList2;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return arrayList;
            }
            return null;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public Integer registerAbilityToken(IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2) throws RemoteException {
        Integer num = null;
        if (this.remote == null) {
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN) || !obtain.writeRemoteObject(iRemoteObject) || !obtain.writeRemoteObject(iRemoteObject2)) {
            return null;
        }
        try {
            if (this.remote.sendRequest(14, obtain, obtain2, messageOption)) {
                num = Integer.valueOf(obtain2.readInt());
            } else {
                AppLog.e(SHELL_LABEL, "DistributedManager::registerAbilityToken sendRequest failed", new Object[0]);
            }
            return num;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public Integer unregisterAbilityToken(IRemoteObject iRemoteObject, IRemoteObject iRemoteObject2) throws RemoteException {
        Integer num = null;
        if (this.remote == null) {
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN) || !obtain.writeRemoteObject(iRemoteObject) || !obtain.writeRemoteObject(iRemoteObject2)) {
            return null;
        }
        try {
            if (this.remote.sendRequest(15, obtain, obtain2, messageOption)) {
                num = Integer.valueOf(obtain2.readInt());
            } else {
                AppLog.e(SHELL_LABEL, "DistributedManager::unregisterAbilityToken sendRequest failed", new Object[0]);
            }
            return num;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public Integer continueAbility(IRemoteObject iRemoteObject, String str, Intent intent) throws RemoteException {
        Integer num = null;
        if (this.remote == null) {
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        if (!obtain.writeInterfaceToken(DISTRIBUTE_SCHEDULE_TOKEN) || !obtain.writeRemoteObject(iRemoteObject) || !obtain.writeString(str)) {
            return null;
        }
        obtain.writeSequenceable(intent);
        try {
            if (this.remote.sendRequest(16, obtain, obtain2, messageOption)) {
                num = Integer.valueOf(obtain2.readInt());
            } else {
                AppLog.e(SHELL_LABEL, "DistributedManager::continueAbility sendRequest failed", new Object[0]);
            }
            return num;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public Integer startContinuation(Intent intent, AbilityInfo abilityInfo, IRemoteObject iRemoteObject) throws RemoteException {
        Integer num = null;
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
        obtain.writeSequenceable(abilityInfo);
        if (!obtain.writeRemoteObject(iRemoteObject)) {
            return null;
        }
        try {
            if (this.remote.sendRequest(11, obtain, obtain2, messageOption)) {
                num = Integer.valueOf(obtain2.readInt());
            } else {
                AppLog.e(SHELL_LABEL, "DistributedManager::startContinuation sendRequest failed", new Object[0]);
            }
            return num;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
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
                try {
                    if (!this.remote.sendRequest(12, obtain, obtain2, messageOption)) {
                        AppLog.e(SHELL_LABEL, "DistributedManager::startContinuation sendRequest failed", new Object[0]);
                    }
                } finally {
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            } else {
                AppLog.e(SHELL_LABEL, "DistributedManager: Write reverse scheduler to parcel fail.", new Object[0]);
            }
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
        try {
            if (!this.remote.sendRequest(30, obtain, obtain2, messageOption)) {
                AppLog.e(SHELL_LABEL, "DistributedManager::selectUri sendRequest failed", new Object[0]);
            } else {
                int readInt = obtain2.readInt();
                if (readInt != 0) {
                    AppLog.e(SHELL_LABEL, "DistributedManager::selectUri failed code is %{public}d", Integer.valueOf(readInt));
                } else {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return !readBoolean ? 1 : 0;
                }
            }
            return -1;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    @Override // ohos.abilityshell.IDistributedManager
    public int getRemoteDataAbility(Uri uri, IRemoteObject iRemoteObject) throws RemoteException {
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
        if (!obtain.writeRemoteObject(iRemoteObject)) {
            return -1;
        }
        try {
            if (!this.remote.sendRequest(31, obtain, obtain2, messageOption)) {
                AppLog.e(SHELL_LABEL, "DistributedManager::getRemoteDataAbility sendRequest failed", new Object[0]);
                return -1;
            }
            int readInt = obtain2.readInt();
            if (readInt != 0) {
                AppLog.e(SHELL_LABEL, "DistributedManager::getRemoteDataAbility failed code is %{public}d", Integer.valueOf(readInt));
            }
            obtain.reclaim();
            obtain2.reclaim();
            return readInt;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }

    private int sendRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        IRemoteObject iRemoteObject = this.remote;
        int i2 = -1;
        if (iRemoteObject == null) {
            return -1;
        }
        try {
            if (iRemoteObject.sendRequest(i, messageParcel, messageParcel2, messageOption)) {
                i2 = messageParcel2.readInt();
            } else {
                AppLog.e(SHELL_LABEL, "DistributedManager::sendRequest failed", new Object[0]);
            }
            return i2;
        } finally {
            messageParcel.reclaim();
            messageParcel2.reclaim();
        }
    }
}
