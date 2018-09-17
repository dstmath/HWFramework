package java.security;

import java.io.Serializable;
import java.util.Enumeration;

public final class Permissions extends PermissionCollection implements Serializable {
    public void add(Permission permission) {
    }

    public boolean implies(Permission permission) {
        return true;
    }

    public Enumeration<Permission> elements() {
        return null;
    }
}
