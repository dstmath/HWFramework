package java.security;

import java.io.Serializable;

public abstract class Permission implements Guard, Serializable {
    private String name;

    public abstract String getActions();

    public abstract boolean implies(Permission permission);

    public Permission(String name2) {
        this.name = name2;
    }

    public void checkGuard(Object object) throws SecurityException {
    }

    public final String getName() {
        return this.name;
    }

    public PermissionCollection newPermissionCollection() {
        return new Permissions();
    }
}
