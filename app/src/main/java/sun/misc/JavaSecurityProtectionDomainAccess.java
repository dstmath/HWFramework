package sun.misc;

import java.security.PermissionCollection;
import java.security.ProtectionDomain;

public interface JavaSecurityProtectionDomainAccess {

    public interface ProtectionDomainCache {
        PermissionCollection get(ProtectionDomain protectionDomain);

        void put(ProtectionDomain protectionDomain, PermissionCollection permissionCollection);
    }

    ProtectionDomainCache getProtectionDomainCache();
}
