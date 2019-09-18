package org.bouncycastle.crypto;

import java.security.Permission;
import java.util.HashSet;
import java.util.Set;

public class CryptoServicesPermission extends Permission {
    public static final String DEFAULT_RANDOM = "defaultRandomConfig";
    public static final String GLOBAL_CONFIG = "globalConfig";
    public static final String THREAD_LOCAL_CONFIG = "threadLocalConfig";
    private final Set<String> actions = new HashSet();

    public CryptoServicesPermission(String str) {
        super(str);
        this.actions.add(str);
    }

    public boolean equals(Object obj) {
        return (obj instanceof CryptoServicesPermission) && this.actions.equals(((CryptoServicesPermission) obj).actions);
    }

    public String getActions() {
        return this.actions.toString();
    }

    public int hashCode() {
        return this.actions.hashCode();
    }

    public boolean implies(Permission permission) {
        if (permission instanceof CryptoServicesPermission) {
            CryptoServicesPermission cryptoServicesPermission = (CryptoServicesPermission) permission;
            if (getName().equals(cryptoServicesPermission.getName()) || this.actions.containsAll(cryptoServicesPermission.actions)) {
                return true;
            }
        }
        return false;
    }
}
