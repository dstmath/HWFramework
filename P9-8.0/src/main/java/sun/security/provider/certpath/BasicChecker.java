package sun.security.provider.certpath;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.Debug;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;

class BasicChecker extends PKIXCertPathChecker {
    private static final Debug debug = Debug.getInstance("certpath");
    private final X500Principal caName;
    private final Date date;
    private PublicKey prevPubKey;
    private X500Principal prevSubject;
    private final boolean sigOnly;
    private final String sigProvider;
    private final PublicKey trustedPubKey;

    BasicChecker(TrustAnchor anchor, Date date, String sigProvider, boolean sigOnly) {
        if (anchor.getTrustedCert() != null) {
            this.trustedPubKey = anchor.getTrustedCert().getPublicKey();
            this.caName = anchor.getTrustedCert().getSubjectX500Principal();
        } else {
            this.trustedPubKey = anchor.getCAPublicKey();
            this.caName = anchor.getCA();
        }
        this.date = date;
        this.sigProvider = sigProvider;
        this.sigOnly = sigOnly;
        this.prevPubKey = this.trustedPubKey;
    }

    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException("forward checking not supported");
        }
        this.prevPubKey = this.trustedPubKey;
        if (PKIX.isDSAPublicKeyWithoutParams(this.prevPubKey)) {
            throw new CertPathValidatorException("Key parameters missing");
        }
        this.prevSubject = this.caName;
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    public Set<String> getSupportedExtensions() {
        return null;
    }

    public void check(Certificate cert, Collection<String> collection) throws CertPathValidatorException {
        X509Certificate currCert = (X509Certificate) cert;
        if (!this.sigOnly) {
            verifyTimestamp(currCert);
            verifyNameChaining(currCert);
        }
        verifySignature(currCert);
        updateState(currCert);
    }

    private void verifySignature(X509Certificate cert) throws CertPathValidatorException {
        String msg = X509CertImpl.SIGNATURE;
        if (debug != null) {
            debug.println("---checking " + msg + "...");
        }
        try {
            if (this.sigProvider != null) {
                cert.verify(this.prevPubKey, this.sigProvider);
            } else {
                cert.verify(this.prevPubKey);
            }
            if (debug != null) {
                debug.println(msg + " verified.");
            }
        } catch (SignatureException e) {
            throw new CertPathValidatorException(msg + " check failed", e, null, -1, BasicReason.INVALID_SIGNATURE);
        } catch (GeneralSecurityException e2) {
            throw new CertPathValidatorException(msg + " check failed", e2);
        }
    }

    private void verifyTimestamp(X509Certificate cert) throws CertPathValidatorException {
        String msg = "timestamp";
        if (debug != null) {
            debug.println("---checking " + msg + ":" + this.date.toString() + "...");
        }
        try {
            cert.checkValidity(this.date);
            if (debug != null) {
                debug.println(msg + " verified.");
            }
        } catch (CertificateExpiredException e) {
            throw new CertPathValidatorException(msg + " check failed", e, null, -1, BasicReason.EXPIRED);
        } catch (CertificateNotYetValidException e2) {
            throw new CertPathValidatorException(msg + " check failed", e2, null, -1, BasicReason.NOT_YET_VALID);
        }
    }

    private void verifyNameChaining(X509Certificate cert) throws CertPathValidatorException {
        if (this.prevSubject != null) {
            String msg = "subject/issuer name chaining";
            if (debug != null) {
                debug.println("---checking " + msg + "...");
            }
            X500Principal currIssuer = cert.getIssuerX500Principal();
            if (X500Name.asX500Name(currIssuer).isEmpty()) {
                throw new CertPathValidatorException(msg + " check failed: " + "empty/null issuer DN in certificate is invalid", null, null, -1, PKIXReason.NAME_CHAINING);
            } else if (!currIssuer.equals(this.prevSubject)) {
                throw new CertPathValidatorException(msg + " check failed", null, null, -1, PKIXReason.NAME_CHAINING);
            } else if (debug != null) {
                debug.println(msg + " verified.");
            }
        }
    }

    private void updateState(X509Certificate currCert) throws CertPathValidatorException {
        PublicKey cKey = currCert.getPublicKey();
        if (debug != null) {
            debug.println("BasicChecker.updateState issuer: " + currCert.getIssuerX500Principal().toString() + "; subject: " + currCert.getSubjectX500Principal() + "; serial#: " + currCert.getSerialNumber().toString());
        }
        if (PKIX.isDSAPublicKeyWithoutParams(cKey)) {
            cKey = makeInheritedParamsKey(cKey, this.prevPubKey);
            if (debug != null) {
                debug.println("BasicChecker.updateState Made key with inherited params");
            }
        }
        this.prevPubKey = cKey;
        this.prevSubject = currCert.getSubjectX500Principal();
    }

    static PublicKey makeInheritedParamsKey(PublicKey keyValueKey, PublicKey keyParamsKey) throws CertPathValidatorException {
        if ((keyValueKey instanceof DSAPublicKey) && ((keyParamsKey instanceof DSAPublicKey) ^ 1) == 0) {
            DSAParams params = ((DSAPublicKey) keyParamsKey).getParams();
            if (params == null) {
                throw new CertPathValidatorException("Key parameters missing");
            }
            try {
                return KeyFactory.getInstance("DSA").generatePublic(new DSAPublicKeySpec(((DSAPublicKey) keyValueKey).getY(), params.getP(), params.getQ(), params.getG()));
            } catch (GeneralSecurityException e) {
                throw new CertPathValidatorException("Unable to generate key with inherited parameters: " + e.getMessage(), e);
            }
        }
        throw new CertPathValidatorException("Input key is not appropriate type for inheriting parameters");
    }

    PublicKey getPublicKey() {
        return this.prevPubKey;
    }
}
