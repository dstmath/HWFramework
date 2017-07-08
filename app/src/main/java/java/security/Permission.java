package java.security;

import java.io.Serializable;

public abstract class Permission implements Guard, Serializable {
    public abstract String getActions();

    public abstract boolean implies(Permission permission);

    public Permission(String name) {
    }

    public void checkGuard(Object object) throws SecurityException {
    }

    public final String getName() {
        return null;
    }

    public PermissionCollection newPermissionCollection() {
        return new Permissions();
    }
}
