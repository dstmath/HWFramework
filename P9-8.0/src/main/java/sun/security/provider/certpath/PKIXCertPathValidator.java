package sun.security.provider.certpath;

import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertPath;
import java.security.cert.CertPathChecker;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertPathValidatorSpi;
import java.security.cert.CertificateException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXReason;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sun.security.util.Debug;
import sun.security.x509.X509CertImpl;

public final class PKIXCertPathValidator extends CertPathValidatorSpi {
    private static final Debug debug = Debug.getInstance("certpath");

    public CertPathChecker engineGetRevocationChecker() {
        return new RevocationChecker();
    }

    public CertPathValidatorResult engineValidate(CertPath cp, CertPathParameters params) throws CertPathValidatorException, InvalidAlgorithmParameterException {
        return validate(PKIX.checkParams(cp, params));
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00c3 A:{Splitter: B:6:0x0035, ExcHandler: java.security.cert.CertificateException (e java.security.cert.CertificateException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static PKIXCertPathValidatorResult validate(ValidatorParams params) throws CertPathValidatorException {
        if (debug != null) {
            debug.println("PKIXCertPathValidator.engineValidate()...");
        }
        AdaptableX509CertSelector selector = null;
        List<X509Certificate> certList = params.certificates();
        if (!certList.isEmpty()) {
            selector = new AdaptableX509CertSelector();
            X509Certificate firstCert = (X509Certificate) certList.get(0);
            selector.setSubject(firstCert.getIssuerX500Principal());
            selector.setValidityPeriod(firstCert.getNotBefore(), firstCert.getNotAfter());
            try {
                selector.setSkiAndSerialNumber(X509CertImpl.toImpl(firstCert).getAuthorityKeyIdentifierExtension());
            } catch (CertificateException e) {
            }
        }
        CertPathValidatorException lastException = null;
        for (TrustAnchor anchor : params.trustAnchors()) {
            X509Certificate trustedCert = anchor.getTrustedCert();
            if (trustedCert != null) {
                if (selector == null || (selector.match(trustedCert) ^ 1) == 0) {
                    if (debug != null) {
                        debug.println("YES - try this trustedCert");
                        debug.println("anchor.getTrustedCert().getSubjectX500Principal() = " + trustedCert.getSubjectX500Principal());
                    }
                } else if (debug != null) {
                    debug.println("NO - don't try this trustedCert");
                }
            } else if (debug != null) {
                debug.println("PKIXCertPathValidator.engineValidate(): anchor.getTrustedCert() == null");
            }
            try {
                return validate(anchor, params);
            } catch (CertPathValidatorException cpe) {
                lastException = cpe;
            }
        }
        if (lastException != null) {
            throw lastException;
        }
        throw new CertPathValidatorException("Path does not chain with any of the trust anchors", null, null, -1, PKIXReason.NO_TRUST_ANCHOR);
    }

    private static PKIXCertPathValidatorResult validate(TrustAnchor anchor, ValidatorParams params) throws CertPathValidatorException {
        int certPathLen = params.certificates().size();
        List<PKIXCertPathChecker> certPathCheckers = new ArrayList();
        certPathCheckers.-java_util_stream_Collectors-mthref-2(new AlgorithmChecker(anchor));
        certPathCheckers.-java_util_stream_Collectors-mthref-2(new KeyChecker(certPathLen, params.targetCertConstraints()));
        certPathCheckers.-java_util_stream_Collectors-mthref-2(new ConstraintsChecker(certPathLen));
        int i = certPathLen;
        PolicyChecker pc = new PolicyChecker(params.initialPolicies(), i, params.explicitPolicyRequired(), params.policyMappingInhibited(), params.anyPolicyInhibited(), params.policyQualifiersRejected(), new PolicyNodeImpl(null, "2.5.29.32.0", null, false, Collections.singleton("2.5.29.32.0"), false));
        certPathCheckers.-java_util_stream_Collectors-mthref-2(pc);
        BasicChecker bc = new BasicChecker(anchor, params.date(), params.sigProvider(), false);
        certPathCheckers.-java_util_stream_Collectors-mthref-2(bc);
        boolean revCheckerAdded = false;
        List<PKIXCertPathChecker> checkers = params.certPathCheckers();
        for (PKIXCertPathChecker checker : checkers) {
            if (checker instanceof PKIXRevocationChecker) {
                if (revCheckerAdded) {
                    throw new CertPathValidatorException("Only one PKIXRevocationChecker can be specified");
                }
                revCheckerAdded = true;
                if (checker instanceof RevocationChecker) {
                    ((RevocationChecker) checker).init(anchor, params);
                }
            }
        }
        if (params.revocationEnabled() && (revCheckerAdded ^ 1) != 0) {
            certPathCheckers.-java_util_stream_Collectors-mthref-2(new RevocationChecker(anchor, params));
        }
        certPathCheckers.addAll(checkers);
        PKIXMasterCertPathValidator.validate(params.certPath(), params.certificates(), certPathCheckers);
        return new PKIXCertPathValidatorResult(anchor, pc.getPolicyTree(), bc.getPublicKey());
    }
}
