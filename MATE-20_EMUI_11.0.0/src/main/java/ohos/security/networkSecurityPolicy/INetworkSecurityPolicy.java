package ohos.security.networkSecurityPolicy;

import ohos.rpc.IRemoteBroker;

public interface INetworkSecurityPolicy extends IRemoteBroker {
    boolean isPlaintextAccessAllowed();

    boolean isPlaintextAccessAllowed(String str);
}
