package java.security.cert;

import java.util.Set;

public interface X509Extension {
    Set<String> getCriticalExtensionOIDs();

    byte[] getExtensionValue(String str);

    Set<String> getNonCriticalExtensionOIDs();

    boolean hasUnsupportedCriticalExtension();
}
