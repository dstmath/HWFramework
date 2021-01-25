package ohos.security.networkSecurityPolicy;

import android.security.NetworkSecurityPolicy;
import ohos.rpc.IRemoteObject;

class NetworkSecurityPolicyProxy implements INetworkSecurityPolicy {
    private final IRemoteObject mRemote;

    NetworkSecurityPolicyProxy(IRemoteObject iRemoteObject) {
        this.mRemote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.mRemote;
    }

    @Override // ohos.security.networkSecurityPolicy.INetworkSecurityPolicy
    public boolean isPlaintextAccessAllowed() {
        return NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
    }

    @Override // ohos.security.networkSecurityPolicy.INetworkSecurityPolicy
    public boolean isPlaintextAccessAllowed(String str) {
        return NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted(str);
    }
}
