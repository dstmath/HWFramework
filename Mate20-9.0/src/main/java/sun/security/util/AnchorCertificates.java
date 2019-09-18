package sun.security.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import sun.security.x509.X509CertImpl;

public class AnchorCertificates {
    private static final String HASH = "SHA-256";
    /* access modifiers changed from: private */
    public static HashSet<String> certs;
    /* access modifiers changed from: private */
    public static final Debug debug = Debug.getInstance("certpath");

    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            /* JADX WARNING: Code restructure failed: missing block: B:12:0x0055, code lost:
                r4 = th;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:13:0x0056, code lost:
                r5 = null;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:17:0x005a, code lost:
                r5 = move-exception;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:18:0x005b, code lost:
                r9 = r5;
                r5 = r4;
                r4 = r9;
             */
            public Void run() {
                FileInputStream fis;
                Throwable th;
                Throwable th2;
                File f = new File(System.getProperty("java.home"), "lib/security/cacerts");
                try {
                    KeyStore cacerts = KeyStore.getInstance("JKS");
                    fis = new FileInputStream(f);
                    cacerts.load(fis, null);
                    HashSet unused = AnchorCertificates.certs = new HashSet();
                    Enumeration<String> list = cacerts.aliases();
                    while (list.hasMoreElements()) {
                        String alias = list.nextElement();
                        if (alias.contains(" [jdk")) {
                            AnchorCertificates.certs.add(X509CertImpl.getFingerprint(AnchorCertificates.HASH, (X509Certificate) cacerts.getCertificate(alias)));
                        }
                    }
                    fis.close();
                } catch (Exception e) {
                    if (AnchorCertificates.debug != null) {
                        AnchorCertificates.debug.println("Error parsing cacerts");
                    }
                    e.printStackTrace();
                }
                return null;
                throw th2;
                if (th != null) {
                    try {
                        fis.close();
                    } catch (Throwable th3) {
                        th.addSuppressed(th3);
                    }
                } else {
                    fis.close();
                }
                throw th2;
            }
        });
    }

    public static boolean contains(X509Certificate cert) {
        boolean result = certs.contains(X509CertImpl.getFingerprint(HASH, cert));
        if (result && debug != null) {
            Debug debug2 = debug;
            debug2.println("AnchorCertificate.contains: matched " + cert.getSubjectDN());
        }
        return result;
    }

    private AnchorCertificates() {
    }
}
