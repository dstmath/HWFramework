package ohos.ai.engine.aimodel;

import java.util.ArrayList;
import java.util.List;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class ModelCoreProxy implements IModelCore {
    private final IRemoteObject remote;

    ModelCoreProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public boolean isConnect() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
            this.remote.sendRequest(1, obtain, obtain2, messageOption);
            return obtain2.readBoolean();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public void connect() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
            this.remote.sendRequest(2, obtain, obtain2, messageOption);
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public boolean insertResourceInformation(String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
            obtain.writeString(str);
            this.remote.sendRequest(3, obtain, obtain2, messageOption);
            return obtain2.readBoolean();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public long getResourceVersionCode(String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
            obtain.writeString(str);
            this.remote.sendRequest(4, obtain, obtain2, messageOption);
            return obtain2.readLong();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public String requestModelPath(long j) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
            obtain.writeLong(j);
            this.remote.sendRequest(5, obtain, obtain2, messageOption);
            return obtain2.readString();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public byte[] requestModelBytes(long j) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
            obtain.writeLong(j);
            this.remote.sendRequest(6, obtain, obtain2, messageOption);
            return obtain2.readByteArray();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public List<AiModelBean> requestModelsByBusiDomain(String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        new ArrayList();
        try {
            obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
            obtain.writeString(str);
            this.remote.sendRequest(7, obtain, obtain2, messageOption);
            return obtain2.readSequenceableList(AiModelBean.class);
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public boolean syncModel(String str, long j) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
            obtain.writeString(str);
            obtain.writeLong(j);
            this.remote.sendRequest(8, obtain, obtain2, messageOption);
            return obtain2.readBoolean();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public boolean isSupportModelManagement() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
            this.remote.sendRequest(9, obtain, obtain2, messageOption);
            return obtain2.readBoolean();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public void subscribeModel(ModelUpInfo modelUpInfo, IRecordObserverCallback iRecordObserverCallback) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
            obtain.writeSequenceable(modelUpInfo);
            obtain.writeRemoteObject(iRecordObserverCallback.asObject());
            this.remote.sendRequest(10, obtain, obtain2, messageOption);
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public void unsubscribeModel(ModelUpInfo modelUpInfo) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
            obtain.writeSequenceable(modelUpInfo);
            this.remote.sendRequest(11, obtain, obtain2, messageOption);
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }
}
