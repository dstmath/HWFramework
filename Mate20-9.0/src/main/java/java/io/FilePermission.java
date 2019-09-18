package java.io;

import java.security.Permission;

public final class FilePermission extends Permission implements Serializable {
    public FilePermission(String path, String actions) {
        super(path);
    }

    public boolean implies(Permission p) {
        return true;
    }

    public String getActions() {
        return null;
    }
}
