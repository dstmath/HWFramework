package ohos.ai.engine.cloudstrategy;

import java.util.Map;
import java.util.Set;
import ohos.ai.engine.cloudstrategy.grs.IGrsCallback;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class CloudStrategyProxy implements ICloudStrategy {
    private final IRemoteObject remote;

    CloudStrategyProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.ai.engine.cloudstrategy.ICloudStrategy
    public void grsInit() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICloudStrategy.DESCRIPTOR);
            this.remote.sendRequest(1, obtain, obtain2, messageOption);
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.cloudstrategy.ICloudStrategy
    public void grsAsyncQueryUrl(String str, String str2, IGrsCallback iGrsCallback) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICloudStrategy.DESCRIPTOR);
            obtain.writeString(str);
            obtain.writeString(str2);
            obtain.writeRemoteObject(iGrsCallback.asObject());
            this.remote.sendRequest(2, obtain, obtain2, messageOption);
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.cloudstrategy.ICloudStrategy
    public String grsSyncQueryUrl(String str, String str2) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICloudStrategy.DESCRIPTOR);
            obtain.writeString(str);
            obtain.writeString(str2);
            this.remote.sendRequest(3, obtain, obtain2, messageOption);
            return obtain2.readString();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.cloudstrategy.ICloudStrategy
    public void grsClear() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICloudStrategy.DESCRIPTOR);
            this.remote.sendRequest(4, obtain, obtain2, messageOption);
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.cloudstrategy.ICloudStrategy
    public void resetOkHttpClient() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICloudStrategy.DESCRIPTOR);
            this.remote.sendRequest(5, obtain, obtain2, messageOption);
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.cloudstrategy.ICloudStrategy
    public String postContainsMap(String str, String str2, Map<String, String> map) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICloudStrategy.DESCRIPTOR);
            obtain.writeString(str);
            obtain.writeString(str2);
            if (map == null) {
                obtain.writeInt(-1);
            } else {
                Set<Map.Entry<String, String>> entrySet = map.entrySet();
                obtain.writeInt(entrySet.size());
                for (Map.Entry<String, String> entry : entrySet) {
                    obtain.writeString(entry.getKey());
                    obtain.writeString(entry.getValue());
                }
            }
            this.remote.sendRequest(6, obtain, obtain2, messageOption);
            return obtain2.readString();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.cloudstrategy.ICloudStrategy
    public String post(String str, String str2) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICloudStrategy.DESCRIPTOR);
            obtain.writeString(str);
            obtain.writeString(str2);
            this.remote.sendRequest(7, obtain, obtain2, messageOption);
            return obtain2.readString();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }
}
