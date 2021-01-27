package ohos.security;

import ohos.sysability.samgr.SysAbilityManager;

public class NetworkSecurityPolicy {
    private static final int SA_ID = 3599;
    private static volatile NetworkSecurityPolicy sInstance;
    private final INetworkSecurityPolicy mProxy = new NetworkSecurityPolicyProxy(SysAbilityManager.getSysAbility(SA_ID));

    private NetworkSecurityPolicy() {
    }

    public static NetworkSecurityPolicy getInstance() {
        if (sInstance == null) {
            synchronized (NetworkSecurityPolicy.class) {
                if (sInstance == null) {
                    sInstance = new NetworkSecurityPolicy();
                }
            }
        }
        return sInstance;
    }

    public boolean isPlaintextAccessAllowed() {
        return this.mProxy.isPlaintextAccessAllowed();
    }

    public boolean isPlaintextAccessAllowed(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        return this.mProxy.isPlaintextAccessAllowed(str);
    }
}
