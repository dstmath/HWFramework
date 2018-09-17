package jdk.net;

import java.security.BasicPermission;

public final class NetworkPermission extends BasicPermission {
    public NetworkPermission(String name) {
        super("");
    }

    public NetworkPermission(String name, String actions) {
        super("", "");
    }
}
