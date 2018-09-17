package java.security;

import java.io.Serializable;

public abstract class BasicPermission extends Permission implements Serializable {
    public BasicPermission(String name) {
        super("");
    }

    public BasicPermission(String name, String actions) {
        super("");
    }

    public boolean implies(Permission p) {
        return true;
    }

    public String getActions() {
        return "";
    }
}
