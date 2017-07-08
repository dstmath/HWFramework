package sun.security.util;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Set;

public final class UntrustedCertificates {
    private static final Set<X509Certificate> untrustedCerts = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.util.UntrustedCertificates.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.util.UntrustedCertificates.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.util.UntrustedCertificates.<clinit>():void");
    }

    public static boolean isUntrusted(X509Certificate cert) {
        return untrustedCerts.contains(cert);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void add(String alias, String pemCert) {
        Exception e;
        Throwable th;
        Throwable th2 = null;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(pemCert.getBytes());
            try {
                X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
                if (untrustedCerts.add(cert)) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (CertificateException e2) {
                            e = e2;
                        }
                    } else {
                        return;
                    }
                }
                throw new RuntimeException("Duplicate untrusted certificate: " + cert.getSubjectX500Principal());
            } catch (Throwable th4) {
                th = th4;
                byteArrayInputStream = is;
                if (byteArrayInputStream != null) {
                    try {
                        byteArrayInputStream.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    throw th;
                }
                try {
                    throw th2;
                } catch (CertificateException e3) {
                    e = e3;
                }
            }
        } catch (Throwable th6) {
            th = th6;
            if (byteArrayInputStream != null) {
                byteArrayInputStream.close();
            }
            if (th2 == null) {
                throw th2;
            } else {
                throw th;
            }
        }
    }
}
