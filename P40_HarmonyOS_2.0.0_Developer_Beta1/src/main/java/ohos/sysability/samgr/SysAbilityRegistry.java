package ohos.sysability.samgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

/* access modifiers changed from: package-private */
public class SysAbilityRegistry implements ISysAbilityRegistry {
    private static final int DEATH_RECIPIENT_FLAG = 0;
    private static final String DEFAULT_CAPABILITY = "";
    private static final boolean DEFAULT_DISTRIBUTED_FLAG = false;
    private static final int DEFAULT_DUMP_FLAG = 1;
    private static final int FLATTEN_ERROR = 1;
    private static final Object INSTANCE_LOCK = new Object();
    private static final String SAMGR_INTERFACE_TOKEN = "ohos.samgr.accessToken";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218109952, "SysAbilityRegistry");
    private static volatile SysAbilityRegistry registryInstance;
    private volatile IRemoteObject iRemoteObject;
    private final Object infoMapLock = new Object();
    private Map<Integer, SAInfo> saInfoMap = new HashMap();

    private SysAbilityRegistry(IRemoteObject iRemoteObject2) {
        this.iRemoteObject = iRemoteObject2;
    }

    static ISysAbilityRegistry getRegistry() {
        return getSystemAbilityManagerRegistry();
    }

    static ISysAbilityRegistry getSystemAbilityManagerRegistry() {
        if (registryInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (registryInstance == null) {
                    IRemoteObject systemAbilityManagerObject = SystemAbilityManagerClient.getSystemAbilityManagerObject();
                    if (systemAbilityManagerObject == null) {
                        HiLog.debug(TAG, "getSystemAbilityManagerObject failed", new Object[0]);
                    } else if (!systemAbilityManagerObject.addDeathRecipient(new SysAbilityManagerDeathRecipient(), 0)) {
                        HiLog.debug(TAG, "SystemAbilityManagerObject addDeathRecipient failed", new Object[0]);
                    }
                    registryInstance = new SysAbilityRegistry(systemAbilityManagerObject);
                }
            }
        }
        return registryInstance;
    }

    @Override // ohos.sysability.samgr.ISysAbilityRegistry
    public IRemoteObject getSysAbility(int i) throws RemoteException {
        return checkSysAbility(i);
    }

    @Override // ohos.sysability.samgr.ISysAbilityRegistry
    public int addSysAbility(int i, IRemoteObject iRemoteObject2) throws RemoteException {
        return addSysAbility(i, iRemoteObject2, false, 1, "");
    }

    @Override // ohos.sysability.samgr.ISysAbilityRegistry
    public int addSysAbility(int i, IRemoteObject iRemoteObject2, boolean z, int i2) throws RemoteException {
        return addSysAbility(i, iRemoteObject2, z, i2, "");
    }

    @Override // ohos.sysability.samgr.ISysAbilityRegistry
    public int removeSysAbility(int i) throws RemoteException {
        if (this.iRemoteObject != null) {
            synchronized (this.infoMapLock) {
                this.saInfoMap.remove(Integer.valueOf(i));
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (obtain.writeInterfaceToken(SAMGR_INTERFACE_TOKEN)) {
                    if (!obtain.writeInt(i)) {
                        HiLog.error(TAG, "removeSysAbility: write systemAbilityId error!", new Object[0]);
                    } else {
                        if (!this.iRemoteObject.sendRequest(4, obtain, obtain2, new MessageOption())) {
                            HiLog.error(TAG, "removeSysAbility: sendRequest failed!", new Object[0]);
                        } else {
                            int readInt = obtain2.readInt();
                            obtain2.reclaim();
                            obtain.reclaim();
                            return readInt;
                        }
                    }
                }
                return 1;
            } finally {
                obtain2.reclaim();
                obtain.reclaim();
            }
        } else {
            HiLog.error(TAG, "Remote Exception: native server is not ready!", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.sysability.samgr.ISysAbilityRegistry
    public IRemoteObject checkSysAbility(int i) throws RemoteException {
        if (this.iRemoteObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!obtain.writeInt(i)) {
                    HiLog.error(TAG, "checkSysAbility: write systemAbilityId error!", new Object[0]);
                } else {
                    if (!this.iRemoteObject.sendRequest(1, obtain, obtain2, new MessageOption())) {
                        HiLog.error(TAG, "checkSysAbility: sendRequest failed!", new Object[0]);
                    } else {
                        IRemoteObject readRemoteObject = obtain2.readRemoteObject();
                        obtain2.reclaim();
                        obtain.reclaim();
                        return readRemoteObject;
                    }
                }
                return null;
            } finally {
                obtain2.reclaim();
                obtain.reclaim();
            }
        } else {
            throw new RemoteException();
        }
    }

    @Override // ohos.sysability.samgr.ISysAbilityRegistry
    public String[] listSysAbilities(int i) throws RemoteException {
        if (this.iRemoteObject != null) {
            ArrayList<String> listAbilitiesListInner = listAbilitiesListInner(i);
            HiLog.debug(TAG, "listSysAbilities: get services size %{public}d!", Integer.valueOf(listAbilitiesListInner.size()));
            String[] strArr = new String[listAbilitiesListInner.size()];
            listAbilitiesListInner.toArray(strArr);
            return strArr;
        }
        HiLog.error(TAG, "Remote Exception: native server is not ready!", new Object[0]);
        throw new RemoteException();
    }

    @Override // ohos.sysability.samgr.ISysAbilityRegistry
    public IRemoteObject getSysAbility(int i, String str) throws RemoteException {
        if (this.iRemoteObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (obtain.writeInterfaceToken(SAMGR_INTERFACE_TOKEN)) {
                    if (!obtain.writeInt(i)) {
                        HiLog.error(TAG, "getRemoteSysAbility: write systemAbilityId failed!", new Object[0]);
                    } else if (!obtain.writeString(str)) {
                        HiLog.error(TAG, "getRemoteSysAbility: write deviceId failed!", new Object[0]);
                    } else {
                        if (!this.iRemoteObject.sendRequest(16, obtain, obtain2, new MessageOption())) {
                            HiLog.error(TAG, "getRemoteSysAbility: sendRequest failed!", new Object[0]);
                        } else {
                            IRemoteObject readRemoteObject = obtain2.readRemoteObject();
                            obtain2.reclaim();
                            obtain.reclaim();
                            return readRemoteObject;
                        }
                    }
                }
                return null;
            } finally {
                obtain2.reclaim();
                obtain.reclaim();
            }
        } else {
            HiLog.error(TAG, "getRemoteSysAbility: iRemoteObject is null", new Object[0]);
            throw new RemoteException();
        }
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.iRemoteObject;
    }

    @Override // ohos.sysability.samgr.ISysAbilityRegistry
    public int addSysAbility(int i, IRemoteObject iRemoteObject2, boolean z, int i2, String str) throws RemoteException {
        if (this.iRemoteObject != null) {
            SAInfo sAInfo = new SAInfo(i, iRemoteObject2, z, i2, str);
            synchronized (this.infoMapLock) {
                this.saInfoMap.put(Integer.valueOf(i), sAInfo);
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (obtain.writeInterfaceToken(SAMGR_INTERFACE_TOKEN)) {
                    if (!parcelSAInfo(obtain, sAInfo)) {
                        HiLog.error(TAG, "addSysAbility: parcelSAInfo error!", new Object[0]);
                    } else {
                        if (!this.iRemoteObject.sendRequest(3, obtain, obtain2, new MessageOption())) {
                            HiLog.error(TAG, "addSysAbility: sendRequest error!", new Object[0]);
                        } else {
                            int readInt = obtain2.readInt();
                            obtain2.reclaim();
                            obtain.reclaim();
                            return readInt;
                        }
                    }
                }
                return 1;
            } finally {
                obtain2.reclaim();
                obtain.reclaim();
            }
        } else {
            HiLog.error(TAG, "Remote Exception: native server is not ready!", new Object[0]);
            throw new RemoteException();
        }
    }

    private boolean parcelSAInfo(MessageParcel messageParcel, SAInfo sAInfo) {
        if (messageParcel == null || sAInfo == null) {
            HiLog.error(TAG, "parcelSAInfo: input null params!", new Object[0]);
            return false;
        } else if (!messageParcel.writeInt(sAInfo.getSaId())) {
            HiLog.error(TAG, "parcelSAInfo: write systemAbilityId error!", new Object[0]);
            return false;
        } else if (!messageParcel.writeRemoteObject(sAInfo.getService())) {
            HiLog.error(TAG, "parcelSAInfo: write object error!", new Object[0]);
            return false;
        } else if (!messageParcel.writeBoolean(sAInfo.isDistributed())) {
            HiLog.error(TAG, "parcelSAInfo: write distributed error!", new Object[0]);
            return false;
        } else if (!messageParcel.writeInt(sAInfo.getDumpFlags())) {
            HiLog.error(TAG, "parcelSAInfo: write flag error!", new Object[0]);
            return false;
        } else if (messageParcel.writeString(sAInfo.getCapability())) {
            return true;
        } else {
            HiLog.error(TAG, "parcelSAInfo: write capability error!", new Object[0]);
            return false;
        }
    }

    @Override // ohos.sysability.samgr.ISysAbilityRegistry
    public boolean reRegisterSysAbility() {
        this.iRemoteObject = SystemAbilityManagerClient.getSystemAbilityManagerObject();
        if (this.iRemoteObject == null) {
            HiLog.debug(TAG, "SystemAbilityManagerClient getSystemAbilityManagerObject is null", new Object[0]);
            return false;
        }
        synchronized (this.infoMapLock) {
            for (Map.Entry<Integer, SAInfo> entry : this.saInfoMap.entrySet()) {
                HiLog.debug(TAG, "ReRegisterSysAbility: addSA name: %{public}d", entry.getKey());
                SAInfo value = entry.getValue();
                try {
                    HiLog.debug(TAG, "ReRegisterSysAbility: addSA result: %{public}d", Integer.valueOf(addSysAbility(value.getSaId(), value.getService(), value.isDistributed(), value.getDumpFlags(), value.getCapability())));
                } catch (RemoteException unused) {
                    HiLog.debug(TAG, "ReRegisterSysAbility add exception", new Object[0]);
                }
            }
        }
        return true;
    }

    private ArrayList<String> listAbilitiesListInner(int i) {
        HiLog.debug(TAG, "listAbilitiesListInner called!", new Object[0]);
        ArrayList<String> arrayList = new ArrayList<>();
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            if (!obtain.writeInterfaceToken(SAMGR_INTERFACE_TOKEN)) {
                HiLog.error(TAG, "listAbilitiesListInner: write token error!", new Object[0]);
            } else if (!obtain.writeInt(i)) {
                HiLog.error(TAG, "listAbilitiesListInner: write dumpPriority error!", new Object[0]);
            } else if (!this.iRemoteObject.sendRequest(5, obtain, obtain2, new MessageOption())) {
                HiLog.error(TAG, "listAbilitiesListInner: sendRequest error!", new Object[0]);
            } else {
                int readInt = obtain2.readInt();
                HiLog.debug(TAG, "listAbilitiesListInner: get remote errorcode %{public}d!", Integer.valueOf(readInt));
                if (readInt <= 0) {
                    int readInt2 = obtain2.readInt();
                    while (true) {
                        int i2 = readInt2 - 1;
                        if (readInt2 <= 0) {
                            break;
                        }
                        String readString = obtain2.readString();
                        if (!readString.isEmpty()) {
                            arrayList.add(readString);
                        }
                        readInt2 = i2;
                    }
                    obtain2.reclaim();
                    obtain.reclaim();
                    return arrayList;
                }
            }
            obtain2.reclaim();
            obtain.reclaim();
            return arrayList;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "listAbilitiesListInner: runtime exception!", new Object[0]);
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public static class SAInfo {
        private String capability;
        private int dumpFlags;
        private boolean isDistributed;
        private IRemoteObject service;
        private int systemAbilityId;

        public SAInfo(int i, IRemoteObject iRemoteObject, boolean z, int i2, String str) {
            this.systemAbilityId = i;
            this.service = iRemoteObject;
            this.isDistributed = z;
            this.dumpFlags = i2;
            this.capability = str;
        }

        public int getSaId() {
            return this.systemAbilityId;
        }

        public IRemoteObject getService() {
            return this.service;
        }

        public boolean isDistributed() {
            return this.isDistributed;
        }

        public int getDumpFlags() {
            return this.dumpFlags;
        }

        public String getCapability() {
            return this.capability;
        }
    }
}
