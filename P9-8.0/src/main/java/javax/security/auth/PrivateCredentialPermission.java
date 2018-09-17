package javax.security.auth;

import java.security.Permission;
import java.security.Principal;
import java.util.Set;

public final class PrivateCredentialPermission extends Permission {
    PrivateCredentialPermission(String credentialClass, Set<Principal> set) {
        super("");
    }

    public PrivateCredentialPermission(String name, String actions) {
        super("");
    }

    public String getCredentialClass() {
        return null;
    }

    public String[][] getPrincipals() {
        return null;
    }

    public boolean implies(Permission p) {
        return true;
    }

    public String getActions() {
        return null;
    }
}
