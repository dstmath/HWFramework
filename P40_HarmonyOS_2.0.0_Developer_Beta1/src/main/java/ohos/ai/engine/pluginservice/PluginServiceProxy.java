package ohos.ai.engine.pluginservice;

import java.util.ArrayList;
import java.util.List;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;
import ohos.utils.Parcel;

public class PluginServiceProxy implements IPluginService {
    private final IRemoteObject remote;

    public PluginServiceProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.ai.engine.pluginservice.IPluginService
    public int checkPluginInstalled(List<PluginRequest> list) throws RemoteException {
        Parcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IPluginService.DESCRIPTOR);
            if (list != null) {
                if (list.size() <= 128) {
                    obtain.writeInt(list.size());
                    for (PluginRequest pluginRequest : list) {
                        if (pluginRequest != null) {
                            obtain.writeInt(1);
                            pluginRequest.marshalling(obtain);
                        } else {
                            obtain.writeInt(0);
                        }
                    }
                    this.remote.sendRequest(1, obtain, obtain2, messageOption);
                    obtain2.readInt();
                    return obtain2.readInt();
                }
            }
            obtain.writeInt(-1);
            this.remote.sendRequest(1, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readInt();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x004b A[Catch:{ all -> 0x0064 }] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0050 A[Catch:{ all -> 0x0064 }] */
    @Override // ohos.ai.engine.pluginservice.IPluginService
    public void startInstallPlugin(List<PluginRequest> list, String str, ILoadPluginCallback iLoadPluginCallback) throws RemoteException {
        Parcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IPluginService.DESCRIPTOR);
            if (list != null) {
                if (list.size() <= 128) {
                    int size = list.size();
                    obtain.writeInt(size);
                    for (int i = 0; i < size; i++) {
                        if (list.get(i) != null) {
                            obtain.writeInt(1);
                            list.get(i).marshalling(obtain);
                        } else {
                            obtain.writeInt(0);
                        }
                    }
                    obtain.writeString(str);
                    obtain.writeRemoteObject(iLoadPluginCallback == null ? iLoadPluginCallback.asObject() : null);
                    this.remote.sendRequest(2, obtain, obtain2, messageOption);
                    obtain2.readInt();
                }
            }
            obtain.writeInt(-1);
            obtain.writeString(str);
            obtain.writeRemoteObject(iLoadPluginCallback == null ? iLoadPluginCallback.asObject() : null);
            this.remote.sendRequest(2, obtain, obtain2, messageOption);
            obtain2.readInt();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginservice.IPluginService
    public IRemoteObject getSplitRemoteObject(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IPluginService.DESCRIPTOR);
            obtain.writeInt(i);
            this.remote.sendRequest(3, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readRemoteObject();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginservice.IPluginService
    public IRemoteObject getHostRemoteObject() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IPluginService.DESCRIPTOR);
            this.remote.sendRequest(4, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readRemoteObject();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginservice.IPluginService
    public String getPluginName(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IPluginService.DESCRIPTOR);
            obtain.writeInt(i);
            this.remote.sendRequest(5, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readString();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginservice.IPluginService
    public List<String> getPluginNames(int[] iArr) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        ArrayList arrayList = new ArrayList();
        try {
            obtain.writeInterfaceToken(IPluginService.DESCRIPTOR);
            obtain.writeIntArray(iArr);
            this.remote.sendRequest(6, obtain, obtain2, messageOption);
            obtain2.readInt();
            int readInt = obtain2.readInt();
            if (readInt >= 0) {
                if (readInt <= 500) {
                    for (int i = 0; i < readInt; i++) {
                        arrayList.add(obtain2.readString());
                    }
                    obtain2.reclaim();
                    obtain.reclaim();
                    return arrayList;
                }
            }
            return arrayList;
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginservice.IPluginService
    public String getSplitRemoteObjectName(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IPluginService.DESCRIPTOR);
            obtain.writeInt(i);
            this.remote.sendRequest(7, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readString();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginservice.IPluginService
    public List<String> getSplitRemoteObjectNames(int[] iArr) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        ArrayList arrayList = new ArrayList();
        try {
            obtain.writeInterfaceToken(IPluginService.DESCRIPTOR);
            obtain.writeIntArray(iArr);
            this.remote.sendRequest(8, obtain, obtain2, messageOption);
            obtain2.readInt();
            int readInt = obtain2.readInt();
            if (readInt >= 0) {
                if (readInt <= 500) {
                    for (int i = 0; i < readInt; i++) {
                        arrayList.add(obtain2.readString());
                    }
                    obtain2.reclaim();
                    obtain.reclaim();
                    return arrayList;
                }
            }
            return arrayList;
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginservice.IPluginService
    public boolean isOpen(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IPluginService.DESCRIPTOR);
            obtain.writeInt(i);
            this.remote.sendRequest(9, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readInt() != 0;
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginservice.IPluginService
    public PacMap process(PacMap pacMap) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        PacMap pacMap2 = new PacMap();
        try {
            obtain.writeInterfaceToken(IPluginService.DESCRIPTOR);
            if (pacMap != null) {
                obtain.writeInt(1);
                pacMap.marshalling(obtain);
            } else {
                obtain.writeInt(0);
            }
            this.remote.sendRequest(10, obtain, obtain2, messageOption);
            obtain2.readInt();
            if (obtain2.readInt() != 0) {
                pacMap2.unmarshalling(obtain2);
            } else {
                pacMap2 = null;
            }
            return pacMap2;
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }
}
