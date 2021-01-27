package ohos.data.usage;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

/* access modifiers changed from: package-private */
public class DataUsageListenerStub extends RemoteObject implements IDataUsageListener {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109441, "DataUsageListenerStub");
    private static final int OBJECT_FLAG = 1;
    private IDataUsageCallback callback;

    public IRemoteObject asObject() {
        return this;
    }

    public DataUsageListenerStub(IDataUsageCallback iDataUsageCallback) {
        super(IDataUsageListener.descriptor);
        this.callback = iDataUsageCallback;
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        HiLog.info(LABEL, "call DataUsageListenerStub, code is %{public}d", new Object[]{Integer.valueOf(i)});
        if (i == 1) {
            onStorageStateChanged(messageParcel.readString(), messageParcel.readString(), messageParcel.readString());
        } else if (i != 2) {
            HiLog.error(LABEL, "unsupported DataUsageListenerStub code", new Object[0]);
            return DataUsageListenerStub.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else if (messageParcel.readInt() == 1) {
            onVolumeStateChanged(new VolumeView(messageParcel), messageParcel.readInt(), messageParcel.readInt());
        } else {
            HiLog.error(LABEL, "invalid object flag", new Object[0]);
        }
        return true;
    }

    @Override // ohos.data.usage.IDataUsageListener
    public void onStorageStateChanged(String str, String str2, String str3) {
        HiLog.info(LABEL, "path is %s, oldState is %s, newState is %s", new Object[]{str, str2, str3});
        MountState status = MountState.getStatus(str2);
        MountState status2 = MountState.getStatus(str3);
        if (status != status2) {
            this.callback.onStorageStateChanged(str, status, status2);
        }
    }

    @Override // ohos.data.usage.IDataUsageListener
    public void onVolumeStateChanged(VolumeView volumeView, int i, int i2) {
        HiLog.info(LABEL, "volume type is %d, state %d -> %d", new Object[]{Integer.valueOf(volumeView.getType()), Integer.valueOf(i), Integer.valueOf(i2)});
        VolumeState status = VolumeState.getStatus(i);
        VolumeState status2 = VolumeState.getStatus(i2);
        if (status != status2) {
            this.callback.onVolumeStateChanged(volumeView, status, status2);
        }
    }
}
