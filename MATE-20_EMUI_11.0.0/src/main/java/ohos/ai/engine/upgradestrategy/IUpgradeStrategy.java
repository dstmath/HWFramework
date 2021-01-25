package ohos.ai.engine.upgradestrategy;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IUpgradeStrategy extends IRemoteBroker {
    public static final int CHECK_HIAI_APP_UPDATE = 3;
    public static final String DESCRIPTOR = "com.huawei.hiai.pdk.upgradestrategy.IUpgradeStrategy";
    public static final int UPDATE_HIAI_APP = 4;

    void checkHiAiAppUpdate(IUpgradeIndicator iUpgradeIndicator) throws RemoteException;

    void updateHiAiApp() throws RemoteException;
}
