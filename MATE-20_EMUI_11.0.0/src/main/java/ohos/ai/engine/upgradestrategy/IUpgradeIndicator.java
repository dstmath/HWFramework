package ohos.ai.engine.upgradestrategy;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IUpgradeIndicator extends IRemoteBroker {
    public static final String DESCRIPTOR = "com.huawei.hiai.pdk.upgradestrategy.IUpgradeIndicator";
    public static final int ON_UPDATE = 1;

    void onUpdate(boolean z) throws RemoteException;
}
