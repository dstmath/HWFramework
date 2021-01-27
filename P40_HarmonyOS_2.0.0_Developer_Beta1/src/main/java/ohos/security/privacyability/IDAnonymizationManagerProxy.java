package ohos.security.privacyability;

import com.huawei.privacyability.IDAnonymizationManager;
import ohos.rpc.IRemoteObject;

class IDAnonymizationManagerProxy implements IIDAnonymizationManager {
    private final IRemoteObject mRemote;

    IDAnonymizationManagerProxy(IRemoteObject iRemoteObject) {
        this.mRemote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.mRemote;
    }

    @Override // ohos.security.privacyability.IIDAnonymizationManager
    public String getCUID() {
        return IDAnonymizationManager.getInstance().getCUID();
    }

    @Override // ohos.security.privacyability.IIDAnonymizationManager
    public String getCFID(String str, String str2) {
        return IDAnonymizationManager.getInstance().getCFID(str, str2);
    }

    @Override // ohos.security.privacyability.IIDAnonymizationManager
    public int resetCUID() {
        return IDAnonymizationManager.getInstance().resetCUID();
    }
}
