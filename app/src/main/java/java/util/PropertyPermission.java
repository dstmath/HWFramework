package java.util;

import java.security.BasicPermission;

public final class PropertyPermission extends BasicPermission {
    public PropertyPermission(String name, String actions) {
        super("", "");
    }
}
