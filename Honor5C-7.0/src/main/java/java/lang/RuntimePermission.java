package java.lang;

import java.security.BasicPermission;

public final class RuntimePermission extends BasicPermission {
    public RuntimePermission(String name) {
        super("");
    }

    public RuntimePermission(String name, String actions) {
        super("", "");
    }
}
