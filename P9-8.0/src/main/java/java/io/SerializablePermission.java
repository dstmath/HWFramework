package java.io;

import java.security.BasicPermission;

public final class SerializablePermission extends BasicPermission {
    public SerializablePermission(String name) {
        super("");
    }

    public SerializablePermission(String name, String actions) {
        super("", "");
    }
}
