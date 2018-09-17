package sun.security.validator;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class KeyStores {
    private KeyStores() {
    }

    public static Set<X509Certificate> getTrustedCerts(KeyStore ks) {
        Set<X509Certificate> set = new HashSet();
        try {
            Enumeration<String> e = ks.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                if (ks.isCertificateEntry(alias)) {
                    Certificate cert = ks.getCertificate(alias);
                    if (cert instanceof X509Certificate) {
                        set.add((X509Certificate) cert);
                    }
                } else if (ks.isKeyEntry(alias)) {
                    Certificate[] certs = ks.getCertificateChain(alias);
                    if (certs != null && certs.length > 0 && (certs[0] instanceof X509Certificate)) {
                        set.add((X509Certificate) certs[0]);
                    }
                }
            }
        } catch (KeyStoreException e2) {
        }
        return set;
    }
}
