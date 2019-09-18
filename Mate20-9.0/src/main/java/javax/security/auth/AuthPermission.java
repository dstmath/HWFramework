package javax.security.auth;

import java.security.BasicPermission;

public final class AuthPermission extends BasicPermission {
    public AuthPermission(String name) {
        super("");
    }

    public AuthPermission(String name, String actions) {
        super("", "");
    }
}
