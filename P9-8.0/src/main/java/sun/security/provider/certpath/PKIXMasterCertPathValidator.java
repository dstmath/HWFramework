package sun.security.provider.certpath;

import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import sun.security.util.Debug;

class PKIXMasterCertPathValidator {
    private static final Debug debug = Debug.getInstance("certpath");

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
            X509Certificate currCert = (X509Certificate) reversedCertList.get(i);
            if (debug != null) {
                debug.println("Checking cert" + (i + 1) + " - Subject: " + currCert.getSubjectX500Principal());
            }
            Set<String> unresCritExts = currCert.getCriticalExtensionOIDs();
            if (unresCritExts == null) {
                unresCritExts = Collections.emptySet();
            }
            if (!(debug == null || (unresCritExts.isEmpty() ^ 1) == 0)) {
                StringJoiner joiner = new StringJoiner(", ", "{", "}");
                for (String oid : unresCritExts) {
                    joiner.-java_util_stream_Collectors-mthref-8(oid);
                }
                debug.println("Set of critical extensions: " + joiner.-java_util_stream_Collectors-mthref-10());
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
                } catch (Throwable cpve) {
                    Throwable cause;
                    String message = cpve.getMessage();
                    if (cpve.getCause() != null) {
                        cause = cpve.getCause();
                    } else {
                        cause = cpve;
                    }
                    throw new CertPathValidatorException(message, cause, cpOriginal, cpSize - (i + 1), cpve.getReason());
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
