package ohos.ai.engine.aimodel;

import java.util.ArrayList;
import java.util.Collections;
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
        obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
        this.remote.sendRequest(1, obtain, obtain2, messageOption);
        boolean readBoolean = obtain2.readBoolean();
        obtain2.reclaim();
        obtain.reclaim();
        return readBoolean;
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public void connect() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
        this.remote.sendRequest(2, obtain, obtain2, messageOption);
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public boolean insertResourceInformation(String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
        obtain.writeString(str);
        this.remote.sendRequest(3, obtain, obtain2, messageOption);
        boolean readBoolean = obtain2.readBoolean();
        obtain2.reclaim();
        obtain.reclaim();
        return readBoolean;
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public long getResourceVersionCode(String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
        obtain.writeString(str);
        this.remote.sendRequest(4, obtain, obtain2, messageOption);
        long readLong = obtain2.readLong();
        obtain2.reclaim();
        obtain.reclaim();
        return readLong;
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public String requestModelPath(long j) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
        obtain.writeLong(j);
        this.remote.sendRequest(5, obtain, obtain2, messageOption);
        String readString = obtain2.readString();
        obtain2.reclaim();
        obtain.reclaim();
        return readString;
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public byte[] requestModelBytes(long j) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
        obtain.writeLong(j);
        this.remote.sendRequest(6, obtain, obtain2, messageOption);
        byte[] readByteArray = obtain2.readByteArray();
        obtain2.reclaim();
        obtain.reclaim();
        return readByteArray;
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public List<AiModelBean> requestModelsByBusiDomain(String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
        obtain.writeString(str);
        ArrayList arrayList = new ArrayList();
        this.remote.sendRequest(7, obtain, obtain2, messageOption);
        int readInt = obtain2.readInt();
        for (int i = 0; i < readInt; i++) {
            AiModelBean aiModelBean = new AiModelBean();
            if (!obtain2.readSequenceable(aiModelBean)) {
                obtain2.reclaim();
                obtain.reclaim();
                return Collections.emptyList();
            }
            arrayList.add(aiModelBean);
        }
        obtain2.reclaim();
        obtain.reclaim();
        return arrayList;
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public boolean syncModel(String str, long j) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
        obtain.writeString(str);
        obtain.writeLong(j);
        MessageParcel obtain2 = MessageParcel.obtain();
        this.remote.sendRequest(8, obtain, obtain2, new MessageOption());
        boolean readBoolean = obtain2.readBoolean();
        obtain2.reclaim();
        obtain.reclaim();
        return readBoolean;
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public boolean isSupportModelManagement() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
        this.remote.sendRequest(9, obtain, obtain2, messageOption);
        boolean readBoolean = obtain2.readBoolean();
        obtain2.reclaim();
        obtain.reclaim();
        return readBoolean;
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public void subscribeModel(ModelUpInfo modelUpInfo, IRecordObserverCallback iRecordObserverCallback) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
        obtain.writeSequenceable(modelUpInfo);
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeRemoteObject(iRecordObserverCallback.asObject());
        this.remote.sendRequest(10, obtain, obtain2, messageOption);
        obtain2.reclaim();
        obtain.reclaim();
    }

    @Override // ohos.ai.engine.aimodel.IModelCore
    public void unsubscribeModel(ModelUpInfo modelUpInfo) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(IModelCore.DESCRIPTOR);
        obtain.writeSequenceable(modelUpInfo);
        this.remote.sendRequest(11, obtain, obtain2, messageOption);
        obtain2.reclaim();
        obtain.reclaim();
    }
}
