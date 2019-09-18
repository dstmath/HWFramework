package java.lang;

import java.security.BasicPermission;

public final class RuntimePermission extends BasicPermission {
    private static final long serialVersionUID = 7399184964622342223L;

    public RuntimePermission(String name) {
        super(name);
    }

    public RuntimePermission(String name, String actions) {
        super(name, actions);
    }
}
