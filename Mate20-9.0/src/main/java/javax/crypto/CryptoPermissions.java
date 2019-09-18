package javax.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import javax.crypto.CryptoPolicyParser;

final class CryptoPermissions extends PermissionCollection implements Serializable {
    CryptoPermissions() {
    }

    /* access modifiers changed from: package-private */
    public void load(InputStream in) throws IOException, CryptoPolicyParser.ParsingException {
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
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

    /* access modifiers changed from: package-private */
    public CryptoPermissions getMinimum(CryptoPermissions other) {
        return null;
    }

    /* access modifiers changed from: package-private */
    public PermissionCollection getPermissionCollection(String alg) {
        return null;
    }
}
