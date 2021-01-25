package ohos.data.dataability;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class RemoteDataAbilityObserverProxy implements IRemoteDataAbilityObserver {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109536, "RemoteDataAbilityObserverProxy");
    private IRemoteObject mRemote;

    public RemoteDataAbilityObserverProxy(IRemoteObject iRemoteObject) {
        if (iRemoteObject != null) {
            this.mRemote = iRemoteObject;
            return;
        }
        throw new IllegalArgumentException("remote cannot be null.");
    }

    public IRemoteObject asObject() {
        return this.mRemote;
    }

    @Override // ohos.aafwk.ability.IDataAbilityObserver
    public void onChange() {
        HiLog.info(LABEL, "onChange start", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        messageOption.setFlags(1);
        try {
            if (!this.mRemote.sendRequest(2, obtain, obtain2, messageOption)) {
                HiLog.error(LABEL, "onChange transact fail", new Object[0]);
            }
        } catch (RemoteException e) {
            HiLog.error(LABEL, "onChange remote exception: %{public}s", new Object[]{e.getMessage()});
        } catch (Throwable th) {
            obtain2.reclaim();
            obtain.reclaim();
            throw th;
        }
        obtain2.reclaim();
        obtain.reclaim();
    }
}
