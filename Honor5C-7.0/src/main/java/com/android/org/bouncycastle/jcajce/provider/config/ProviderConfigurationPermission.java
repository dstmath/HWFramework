package com.android.org.bouncycastle.jcajce.provider.config;

import com.android.org.bouncycastle.util.Strings;
import java.security.BasicPermission;
import java.security.Permission;
import java.util.StringTokenizer;

public class ProviderConfigurationPermission extends BasicPermission {
    private static final int ALL = 15;
    private static final String ALL_STR = "all";
    private static final int DH_DEFAULT_PARAMS = 8;
    private static final String DH_DEFAULT_PARAMS_STR = "dhdefaultparams";
    private static final int EC_IMPLICITLY_CA = 2;
    private static final String EC_IMPLICITLY_CA_STR = "ecimplicitlyca";
    private static final int THREAD_LOCAL_DH_DEFAULT_PARAMS = 4;
    private static final String THREAD_LOCAL_DH_DEFAULT_PARAMS_STR = "threadlocaldhdefaultparams";
    private static final int THREAD_LOCAL_EC_IMPLICITLY_CA = 1;
    private static final String THREAD_LOCAL_EC_IMPLICITLY_CA_STR = "threadlocalecimplicitlyca";
    private final String actions;
    private final int permissionMask;

    public ProviderConfigurationPermission(String name) {
        super(name);
        this.actions = ALL_STR;
        this.permissionMask = ALL;
    }

    public ProviderConfigurationPermission(String name, String actions) {
        super(name, actions);
        this.actions = actions;
        this.permissionMask = calculateMask(actions);
    }

    private int calculateMask(String actions) {
        StringTokenizer tok = new StringTokenizer(Strings.toLowerCase(actions), " ,");
        int mask = 0;
        while (tok.hasMoreTokens()) {
            String s = tok.nextToken();
            if (s.equals(THREAD_LOCAL_EC_IMPLICITLY_CA_STR)) {
                mask |= THREAD_LOCAL_EC_IMPLICITLY_CA;
            } else if (s.equals(EC_IMPLICITLY_CA_STR)) {
                mask |= EC_IMPLICITLY_CA;
            } else if (s.equals(THREAD_LOCAL_DH_DEFAULT_PARAMS_STR)) {
                mask |= THREAD_LOCAL_DH_DEFAULT_PARAMS;
            } else if (s.equals(DH_DEFAULT_PARAMS_STR)) {
                mask |= DH_DEFAULT_PARAMS;
            } else if (s.equals(ALL_STR)) {
                mask |= ALL;
            }
        }
        if (mask != 0) {
            return mask;
        }
        throw new IllegalArgumentException("unknown permissions passed to mask");
    }

    public String getActions() {
        return this.actions;
    }

    public boolean implies(Permission permission) {
        boolean z = false;
        if (!(permission instanceof ProviderConfigurationPermission) || !getName().equals(permission.getName())) {
            return false;
        }
        ProviderConfigurationPermission other = (ProviderConfigurationPermission) permission;
        if ((this.permissionMask & other.permissionMask) == other.permissionMask) {
            z = true;
        }
        return z;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ProviderConfigurationPermission)) {
            return false;
        }
        ProviderConfigurationPermission other = (ProviderConfigurationPermission) obj;
        if (this.permissionMask == other.permissionMask) {
            z = getName().equals(other.getName());
        }
        return z;
    }

    public int hashCode() {
        return getName().hashCode() + this.permissionMask;
    }
}
