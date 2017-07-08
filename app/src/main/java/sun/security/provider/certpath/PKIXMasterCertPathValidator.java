package sun.security.provider.certpath;

import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import sun.security.util.Debug;

class PKIXMasterCertPathValidator {
    private static final Debug debug = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.PKIXMasterCertPathValidator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.PKIXMasterCertPathValidator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.PKIXMasterCertPathValidator.<clinit>():void");
    }

    PKIXMasterCertPathValidator() {
    }

    static void validate(CertPath cpOriginal, List<X509Certificate> reversedCertList, List<PKIXCertPathChecker> certPathCheckers) throws CertPathValidatorException {
        int cpSize = reversedCertList.size();
        if (debug != null) {
            debug.println("--------------------------------------------------------------");
            debug.println("Executing PKIX certification path validation algorithm.");
        }
        int i = 0;
        while (i < cpSize) {
            if (debug != null) {
                debug.println("Checking cert" + (i + 1) + " ...");
            }
            X509Certificate currCert = (X509Certificate) reversedCertList.get(i);
            Set<String> unresCritExts = currCert.getCriticalExtensionOIDs();
            if (unresCritExts == null) {
                unresCritExts = Collections.emptySet();
            }
            if (!(debug == null || unresCritExts.isEmpty())) {
                debug.println("Set of critical extensions:");
                for (String oid : unresCritExts) {
                    debug.println(oid);
                }
            }
            int j = 0;
            while (j < certPathCheckers.size()) {
                PKIXCertPathChecker currChecker = (PKIXCertPathChecker) certPathCheckers.get(j);
                if (debug != null) {
                    debug.println("-Using checker" + (j + 1) + " ... [" + currChecker.getClass().getName() + "]");
                }
                if (i == 0) {
                    currChecker.init(false);
                }
                try {
                    currChecker.check(currCert, unresCritExts);
                    if (debug != null) {
                        debug.println("-checker" + (j + 1) + " validation succeeded");
                    }
                    j++;
                } catch (CertPathValidatorException cpve) {
                    throw new CertPathValidatorException(cpve.getMessage(), cpve.getCause(), cpOriginal, cpSize - (i + 1), cpve.getReason());
                }
            }
            if (unresCritExts.isEmpty()) {
                if (debug != null) {
                    debug.println("\ncert" + (i + 1) + " validation succeeded.\n");
                }
                i++;
            } else {
                throw new CertPathValidatorException("unrecognized critical extension(s)", null, cpOriginal, cpSize - (i + 1), PKIXReason.UNRECOGNIZED_CRIT_EXT);
            }
        }
        if (debug != null) {
            debug.println("Cert path validation succeeded. (PKIX validation algorithm)");
            debug.println("--------------------------------------------------------------");
        }
    }
}
