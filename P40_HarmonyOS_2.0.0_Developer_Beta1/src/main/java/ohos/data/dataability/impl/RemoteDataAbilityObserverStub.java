package ohos.data.dataability.impl;

import ohos.aafwk.ability.IDataAbilityObserver;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public class RemoteDataAbilityObserverStub extends RemoteObject implements IRemoteDataAbilityObserver {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109536, "RemoteDataAbilityObserver");
    private IDataAbilityObserver observer;

    public RemoteDataAbilityObserverStub(String str, IDataAbilityObserver iDataAbilityObserver) {
        super(str);
        this.observer = iDataAbilityObserver;
    }

    @Override // ohos.aafwk.ability.IDataAbilityObserver
    public void onChange() {
        HiLog.info(LABEL, " onChange", new Object[0]);
        this.observer.onChange();
    }

    public IRemoteObject asObject() {
        HiLog.info(LABEL, "asObject", new Object[0]);
        return this;
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i != 2) {
            return RemoteDataAbilityObserverStub.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        HiLog.info(LABEL, "notify start", new Object[0]);
        onChange();
        return true;
    }
}
