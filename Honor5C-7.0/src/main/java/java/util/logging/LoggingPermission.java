package java.util.logging;

import java.security.BasicPermission;

public final class LoggingPermission extends BasicPermission {
    public LoggingPermission(String name, String actions) throws IllegalArgumentException {
        super("", "");
    }
}
