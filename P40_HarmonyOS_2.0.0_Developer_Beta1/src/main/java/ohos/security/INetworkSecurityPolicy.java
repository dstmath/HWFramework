package ohos.security;

import ohos.rpc.IRemoteBroker;

interface INetworkSecurityPolicy extends IRemoteBroker {
    boolean isPlaintextAccessAllowed();

    boolean isPlaintextAccessAllowed(String str);
}
