package sun.security.provider.certpath;

import java.security.AlgorithmConstraints;
import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CRLException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import sun.security.util.AnchorCertificates;
import sun.security.util.CertConstraintParameters;
import sun.security.util.Debug;
import sun.security.util.DisabledAlgorithmConstraints;
import sun.security.util.KeyUtil;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;

public final class AlgorithmChecker extends PKIXCertPathChecker {
    private static final Set<CryptoPrimitive> KU_PRIMITIVE_SET = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.SIGNATURE, CryptoPrimitive.KEY_ENCAPSULATION, CryptoPrimitive.PUBLIC_KEY_ENCRYPTION, CryptoPrimitive.KEY_AGREEMENT));
    private static final Set<CryptoPrimitive> SIGNATURE_PRIMITIVE_SET = Collections.unmodifiableSet(EnumSet.of(CryptoPrimitive.SIGNATURE));
    private static final DisabledAlgorithmConstraints certPathDefaultConstraints = new DisabledAlgorithmConstraints(DisabledAlgorithmConstraints.PROPERTY_CERTPATH_DISABLED_ALGS);
    private static final Debug debug = Debug.getInstance("certpath");
    private static final boolean publicCALimits = certPathDefaultConstraints.checkProperty("jdkCA");
    private final AlgorithmConstraints constraints;
    private PublicKey prevPubKey;
    private boolean trustedMatch;
    private final PublicKey trustedPubKey;

    public AlgorithmChecker(TrustAnchor anchor) {
        this(anchor, certPathDefaultConstraints);
    }

    public AlgorithmChecker(AlgorithmConstraints constraints2) {
        this.trustedMatch = false;
        this.prevPubKey = null;
        this.trustedPubKey = null;
        this.constraints = constraints2;
    }

    public AlgorithmChecker(TrustAnchor anchor, AlgorithmConstraints constraints2) {
        this.trustedMatch = false;
        if (anchor != null) {
            if (anchor.getTrustedCert() != null) {
                this.trustedPubKey = anchor.getTrustedCert().getPublicKey();
                this.trustedMatch = checkFingerprint(anchor.getTrustedCert());
                if (this.trustedMatch && debug != null) {
                    debug.println("trustedMatch = true");
                }
            } else {
                this.trustedPubKey = anchor.getCAPublicKey();
            }
            this.prevPubKey = this.trustedPubKey;
            this.constraints = constraints2;
            return;
        }
        throw new IllegalArgumentException("The trust anchor cannot be null");
    }

    private static boolean checkFingerprint(X509Certificate cert) {
        if (!publicCALimits) {
            return false;
        }
        if (debug != null) {
            Debug debug2 = debug;
            debug2.println("AlgorithmChecker.contains: " + cert.getSigAlgName());
        }
        return AnchorCertificates.contains(cert);
    }

    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException("forward checking not supported");
        } else if (this.trustedPubKey != null) {
            this.prevPubKey = this.trustedPubKey;
        } else {
            this.prevPubKey = null;
        }
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    public Set<String> getSupportedExtensions() {
        return null;
    }

    public void check(Certificate cert, Collection<String> collection) throws CertPathValidatorException {
        Certificate certificate = cert;
        if ((certificate instanceof X509Certificate) && this.constraints != null) {
            boolean[] keyUsage = ((X509Certificate) certificate).getKeyUsage();
            if (keyUsage == null || keyUsage.length >= 9) {
                Set<CryptoPrimitive> primitives = KU_PRIMITIVE_SET;
                if (keyUsage != null) {
                    primitives = EnumSet.noneOf(CryptoPrimitive.class);
                    if (keyUsage[0] || keyUsage[1] || keyUsage[5] || keyUsage[6]) {
                        primitives.add(CryptoPrimitive.SIGNATURE);
                    }
                    if (keyUsage[2]) {
                        primitives.add(CryptoPrimitive.KEY_ENCAPSULATION);
                    }
                    if (keyUsage[3]) {
                        primitives.add(CryptoPrimitive.PUBLIC_KEY_ENCRYPTION);
                    }
                    if (keyUsage[4]) {
                        primitives.add(CryptoPrimitive.KEY_AGREEMENT);
                    }
                    if (primitives.isEmpty()) {
                        CertPathValidatorException certPathValidatorException = new CertPathValidatorException("incorrect KeyUsage extension bits", null, null, -1, PKIXReason.INVALID_KEY_USAGE);
                        throw certPathValidatorException;
                    }
                }
                Set<CryptoPrimitive> primitives2 = primitives;
                PublicKey currPubKey = cert.getPublicKey();
                if (this.constraints instanceof DisabledAlgorithmConstraints) {
                    ((DisabledAlgorithmConstraints) this.constraints).permits(primitives2, new CertConstraintParameters((X509Certificate) certificate, this.trustedMatch));
                    if (this.prevPubKey == null) {
                        this.prevPubKey = currPubKey;
                        return;
                    }
                }
                try {
                    X509CertImpl x509Cert = X509CertImpl.toImpl((X509Certificate) certificate);
                    AlgorithmParameters currSigAlgParams = ((AlgorithmId) x509Cert.get(X509CertImpl.SIG_ALG)).getParameters();
                    String currSigAlg = x509Cert.getSigAlgName();
                    if (!(this.constraints instanceof DisabledAlgorithmConstraints)) {
                        if (!this.constraints.permits(SIGNATURE_PRIMITIVE_SET, currSigAlg, currSigAlgParams)) {
                            CertPathValidatorException certPathValidatorException2 = new CertPathValidatorException("Algorithm constraints check failed on signature algorithm: " + currSigAlg, null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
                            throw certPathValidatorException2;
                        } else if (!this.constraints.permits(primitives2, currPubKey)) {
                            CertPathValidatorException certPathValidatorException3 = new CertPathValidatorException("Algorithm constraints check failed on keysize: " + KeyUtil.getKeySize(currPubKey), null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
                            throw certPathValidatorException3;
                        }
                    }
                    if (this.prevPubKey != null) {
                        if (!this.constraints.permits(SIGNATURE_PRIMITIVE_SET, currSigAlg, this.prevPubKey, currSigAlgParams)) {
                            CertPathValidatorException certPathValidatorException4 = new CertPathValidatorException("Algorithm constraints check failed on signature algorithm: " + currSigAlg, null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
                            throw certPathValidatorException4;
                        } else if (PKIX.isDSAPublicKeyWithoutParams(currPubKey)) {
                            if (this.prevPubKey instanceof DSAPublicKey) {
                                DSAParams params = ((DSAPublicKey) this.prevPubKey).getParams();
                                if (params != null) {
                                    try {
                                        currPubKey = KeyFactory.getInstance("DSA").generatePublic(new DSAPublicKeySpec(((DSAPublicKey) currPubKey).getY(), params.getP(), params.getQ(), params.getG()));
                                    } catch (GeneralSecurityException e) {
                                        throw new CertPathValidatorException("Unable to generate key with inherited parameters: " + e.getMessage(), e);
                                    }
                                } else {
                                    throw new CertPathValidatorException("Key parameters missing from public key.");
                                }
                            } else {
                                throw new CertPathValidatorException("Input key is not of a appropriate type for inheriting parameters");
                            }
                        }
                    }
                    this.prevPubKey = currPubKey;
                } catch (CertificateException ce) {
                    throw new CertPathValidatorException((Throwable) ce);
                }
            } else {
                CertPathValidatorException certPathValidatorException5 = new CertPathValidatorException("incorrect KeyUsage extension", null, null, -1, PKIXReason.INVALID_KEY_USAGE);
                throw certPathValidatorException5;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void trySetTrustAnchor(TrustAnchor anchor) {
        if (this.prevPubKey != null) {
            return;
        }
        if (anchor == null) {
            throw new IllegalArgumentException("The trust anchor cannot be null");
        } else if (anchor.getTrustedCert() != null) {
            this.prevPubKey = anchor.getTrustedCert().getPublicKey();
            this.trustedMatch = checkFingerprint(anchor.getTrustedCert());
            if (this.trustedMatch && debug != null) {
                debug.println("trustedMatch = true");
            }
        } else {
            this.prevPubKey = anchor.getCAPublicKey();
        }
    }

    static void check(PublicKey key, X509CRL crl) throws CertPathValidatorException {
        try {
            check(key, X509CRLImpl.toImpl(crl).getSigAlgId());
        } catch (CRLException ce) {
            throw new CertPathValidatorException((Throwable) ce);
        }
    }

    static void check(PublicKey key, AlgorithmId algorithmId) throws CertPathValidatorException {
        String sigAlgName = algorithmId.getName();
        if (!certPathDefaultConstraints.permits(SIGNATURE_PRIMITIVE_SET, sigAlgName, key, algorithmId.getParameters())) {
            CertPathValidatorException certPathValidatorException = new CertPathValidatorException("Algorithm constraints check failed on signature algorithm: " + sigAlgName + " is disabled", null, null, -1, CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED);
            throw certPathValidatorException;
        }
    }
}
