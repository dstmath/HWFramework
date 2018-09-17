package java.sql;

import java.security.BasicPermission;

public final class SQLPermission extends BasicPermission {
    public SQLPermission(String name) {
        super("");
    }

    public SQLPermission(String name, String actions) {
        super("", "");
    }
}
