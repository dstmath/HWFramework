package sun.security.provider.certpath;

import java.io.IOException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sun.security.util.Debug;
import sun.security.x509.NameConstraintsExtension;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.X509CertImpl;

class ConstraintsChecker extends PKIXCertPathChecker {
    private static final Debug debug = Debug.getInstance("certpath");
    private final int certPathLength;
    private int i;
    private int maxPathLength;
    private NameConstraintsExtension prevNC;
    private Set<String> supportedExts;

    ConstraintsChecker(int certPathLength2) {
        this.certPathLength = certPathLength2;
    }

    public void init(boolean forward) throws CertPathValidatorException {
        if (!forward) {
            this.i = 0;
            this.maxPathLength = this.certPathLength;
            this.prevNC = null;
            return;
        }
        throw new CertPathValidatorException("forward checking not supported");
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    public Set<String> getSupportedExtensions() {
        if (this.supportedExts == null) {
            this.supportedExts = new HashSet(2);
            this.supportedExts.add(PKIXExtensions.BasicConstraints_Id.toString());
            this.supportedExts.add(PKIXExtensions.NameConstraints_Id.toString());
            this.supportedExts = Collections.unmodifiableSet(this.supportedExts);
        }
        return this.supportedExts;
    }

    public void check(Certificate cert, Collection<String> unresCritExts) throws CertPathValidatorException {
        X509Certificate currCert = (X509Certificate) cert;
        this.i++;
        checkBasicConstraints(currCert);
        verifyNameConstraints(currCert);
        if (unresCritExts != null && !unresCritExts.isEmpty()) {
            unresCritExts.remove(PKIXExtensions.BasicConstraints_Id.toString());
            unresCritExts.remove(PKIXExtensions.NameConstraints_Id.toString());
        }
    }

    private void verifyNameConstraints(X509Certificate currCert) throws CertPathValidatorException {
        if (debug != null) {
            Debug debug2 = debug;
            debug2.println("---checking " + "name constraints" + "...");
        }
        if (this.prevNC != null && (this.i == this.certPathLength || !X509CertImpl.isSelfIssued(currCert))) {
            if (debug != null) {
                Debug debug3 = debug;
                debug3.println("prevNC = " + this.prevNC + ", currDN = " + currCert.getSubjectX500Principal());
            }
            try {
                if (!this.prevNC.verify(currCert)) {
                    CertPathValidatorException certPathValidatorException = new CertPathValidatorException("name constraints" + " check failed", null, null, -1, PKIXReason.INVALID_NAME);
                    throw certPathValidatorException;
                }
            } catch (IOException ioe) {
                throw new CertPathValidatorException((Throwable) ioe);
            }
        }
        this.prevNC = mergeNameConstraints(currCert, this.prevNC);
        if (debug != null) {
            Debug debug4 = debug;
            debug4.println("name constraints" + " verified.");
        }
    }

    static NameConstraintsExtension mergeNameConstraints(X509Certificate currCert, NameConstraintsExtension prevNC2) throws CertPathValidatorException {
        try {
            NameConstraintsExtension newConstraints = X509CertImpl.toImpl(currCert).getNameConstraintsExtension();
            if (debug != null) {
                Debug debug2 = debug;
                debug2.println("prevNC = " + prevNC2 + ", newNC = " + String.valueOf((Object) newConstraints));
            }
            if (prevNC2 == null) {
                if (debug != null) {
                    Debug debug3 = debug;
                    debug3.println("mergedNC = " + String.valueOf((Object) newConstraints));
                }
                if (newConstraints == null) {
                    return newConstraints;
                }
                return (NameConstraintsExtension) newConstraints.clone();
            }
            try {
                prevNC2.merge(newConstraints);
                if (debug != null) {
                    Debug debug4 = debug;
                    debug4.println("mergedNC = " + prevNC2);
                }
                return prevNC2;
            } catch (IOException ioe) {
                throw new CertPathValidatorException((Throwable) ioe);
            }
        } catch (CertificateException ce) {
            throw new CertPathValidatorException((Throwable) ce);
        }
    }

    private void checkBasicConstraints(X509Certificate currCert) throws CertPathValidatorException {
        if (debug != null) {
            debug.println("---checking " + "basic constraints" + "...");
            debug.println("i = " + this.i + ", maxPathLength = " + this.maxPathLength);
        }
        if (this.i < this.certPathLength) {
            int pathLenConstraint = -1;
            if (currCert.getVersion() >= 3) {
                pathLenConstraint = currCert.getBasicConstraints();
            } else if (this.i == 1 && X509CertImpl.isSelfIssued(currCert)) {
                pathLenConstraint = Integer.MAX_VALUE;
            }
            if (pathLenConstraint != -1) {
                if (!X509CertImpl.isSelfIssued(currCert)) {
                    if (this.maxPathLength > 0) {
                        this.maxPathLength--;
                    } else {
                        CertPathValidatorException certPathValidatorException = new CertPathValidatorException("basic constraints" + " check failed: pathLenConstraint violated - this cert must be the last cert in the certification path", null, null, -1, PKIXReason.PATH_TOO_LONG);
                        throw certPathValidatorException;
                    }
                }
                if (pathLenConstraint < this.maxPathLength) {
                    this.maxPathLength = pathLenConstraint;
                }
            } else {
                CertPathValidatorException certPathValidatorException2 = new CertPathValidatorException("basic constraints" + " check failed: this is not a CA certificate", null, null, -1, PKIXReason.NOT_CA_CERT);
                throw certPathValidatorException2;
            }
        }
        if (debug != null) {
            debug.println("after processing, maxPathLength = " + this.maxPathLength);
            debug.println("basic constraints" + " verified.");
        }
    }

    static int mergeBasicConstraints(X509Certificate cert, int maxPathLength2) {
        int pathLenConstraint = cert.getBasicConstraints();
        if (!X509CertImpl.isSelfIssued(cert)) {
            maxPathLength2--;
        }
        if (pathLenConstraint < maxPathLength2) {
            return pathLenConstraint;
        }
        return maxPathLength2;
    }
}
