package java.net;

import java.security.BasicPermission;

public final class NetPermission extends BasicPermission {
    public NetPermission(String name) {
        super("");
    }

    public NetPermission(String name, String actions) {
        super("", "");
    }
}
