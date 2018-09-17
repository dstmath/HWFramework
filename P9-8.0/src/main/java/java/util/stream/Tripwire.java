package java.util.stream;

import java.security.AccessController;
import sun.util.logging.PlatformLogger;

final class Tripwire {
    static final boolean ENABLED = ((Boolean) AccessController.doPrivileged(new -$Lambda$3nPY9eh0W9QxXLTyO6D6E2Hbj1I())).booleanValue();
    private static final String TRIPWIRE_PROPERTY = "org.openjdk.java.util.stream.tripwire";

    private Tripwire() {
    }

    static void trip(Class<?> trippingClass, String msg) {
        PlatformLogger.getLogger(trippingClass.getName()).warning(msg, trippingClass.getName());
    }
}
