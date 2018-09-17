package javax.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;

final class CryptoPermissions extends PermissionCollection implements Serializable {
    CryptoPermissions() {
    }

    void load(InputStream in) throws IOException, ParsingException {
    }

    boolean isEmpty() {
        return true;
    }

    public void add(Permission permission) {
    }

    public boolean implies(Permission permission) {
        return true;
    }

    public Enumeration elements() {
        return null;
    }

    CryptoPermissions getMinimum(CryptoPermissions other) {
        return null;
    }

    PermissionCollection getPermissionCollection(String alg) {
        return null;
    }
}
