package javax.net.ssl;

import java.security.BasicPermission;

public final class SSLPermission extends BasicPermission {
    public SSLPermission(String name) {
        super("");
    }

    public SSLPermission(String name, String actions) {
        super("", "");
    }
}
