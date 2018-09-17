package java.util;

import java.security.AccessController;
import sun.util.logging.PlatformLogger;

final class Tripwire {
    static final boolean ENABLED = ((Boolean) AccessController.doPrivileged(new -$Lambda$3UFkonVPLR5NaHEH6a4Hvn535JY())).booleanValue();
    private static final String TRIPWIRE_PROPERTY = "org.openjdk.java.util.stream.tripwire";

    private Tripwire() {
    }

    static void trip(Class<?> trippingClass, String msg) {
        PlatformLogger.getLogger(trippingClass.getName()).warning(msg, trippingClass.getName());
    }
}
