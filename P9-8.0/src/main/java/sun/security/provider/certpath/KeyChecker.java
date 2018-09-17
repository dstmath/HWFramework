package sun.security.provider.certpath;

import java.security.cert.CertPathValidatorException;
import java.security.cert.CertSelector;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sun.security.util.Debug;
import sun.security.x509.PKIXExtensions;

class KeyChecker extends PKIXCertPathChecker {
    private static final int KEY_CERT_SIGN = 5;
    private static final Debug debug = Debug.getInstance("certpath");
    private final int certPathLen;
    private int remainingCerts;
    private Set<String> supportedExts;
    private final CertSelector targetConstraints;

    KeyChecker(int certPathLen, CertSelector targetCertSel) {
        this.certPathLen = certPathLen;
        this.targetConstraints = targetCertSel;
    }

    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException("forward checking not supported");
        }
        this.remainingCerts = this.certPathLen;
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    public Set<String> getSupportedExtensions() {
        if (this.supportedExts == null) {
            this.supportedExts = new HashSet(3);
            this.supportedExts.-java_util_stream_Collectors-mthref-4(PKIXExtensions.KeyUsage_Id.toString());
            this.supportedExts.-java_util_stream_Collectors-mthref-4(PKIXExtensions.ExtendedKeyUsage_Id.toString());
            this.supportedExts.-java_util_stream_Collectors-mthref-4(PKIXExtensions.SubjectAlternativeName_Id.toString());
            this.supportedExts = Collections.unmodifiableSet(this.supportedExts);
        }
        return this.supportedExts;
    }

    public void check(Certificate cert, Collection<String> unresCritExts) throws CertPathValidatorException {
        X509Certificate currCert = (X509Certificate) cert;
        this.remainingCerts--;
        if (this.remainingCerts != 0) {
            verifyCAKeyUsage(currCert);
        } else if (!(this.targetConstraints == null || this.targetConstraints.match(currCert))) {
            throw new CertPathValidatorException("target certificate constraints check failed");
        }
        if (unresCritExts != null && (unresCritExts.isEmpty() ^ 1) != 0) {
            unresCritExts.remove(PKIXExtensions.KeyUsage_Id.toString());
            unresCritExts.remove(PKIXExtensions.ExtendedKeyUsage_Id.toString());
            unresCritExts.remove(PKIXExtensions.SubjectAlternativeName_Id.toString());
        }
    }

    static void verifyCAKeyUsage(X509Certificate cert) throws CertPathValidatorException {
        String msg = "CA key usage";
        if (debug != null) {
            debug.println("KeyChecker.verifyCAKeyUsage() ---checking " + msg + "...");
        }
        boolean[] keyUsageBits = cert.getKeyUsage();
        if (keyUsageBits != null) {
            if (keyUsageBits[5]) {
                if (debug != null) {
                    debug.println("KeyChecker.verifyCAKeyUsage() " + msg + " verified.");
                }
                return;
            }
            throw new CertPathValidatorException(msg + " check failed: keyCertSign bit is not set", null, null, -1, PKIXReason.INVALID_KEY_USAGE);
        }
    }
}
