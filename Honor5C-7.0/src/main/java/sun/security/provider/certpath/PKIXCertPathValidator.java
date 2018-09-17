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
    private static final Debug debug = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.PKIXCertPathValidator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.PKIXCertPathValidator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.PKIXCertPathValidator.<clinit>():void");
    }

    public CertPathChecker engineGetRevocationChecker() {
        return new RevocationChecker();
    }

    public CertPathValidatorResult engineValidate(CertPath cp, CertPathParameters params) throws CertPathValidatorException, InvalidAlgorithmParameterException {
        return validate(PKIX.checkParams(cp, params));
    }

    private static PKIXCertPathValidatorResult validate(ValidatorParams params) throws CertPathValidatorException {
        if (debug != null) {
            debug.println("PKIXCertPathValidator.engineValidate()...");
        }
        AdaptableX509CertSelector adaptableX509CertSelector = null;
        List<X509Certificate> certList = params.certificates();
        if (!certList.isEmpty()) {
            adaptableX509CertSelector = new AdaptableX509CertSelector();
            X509Certificate firstCert = (X509Certificate) certList.get(0);
            adaptableX509CertSelector.setSubject(firstCert.getIssuerX500Principal());
            adaptableX509CertSelector.setValidityPeriod(firstCert.getNotBefore(), firstCert.getNotAfter());
            try {
                adaptableX509CertSelector.parseAuthorityKeyIdentifierExtension(X509CertImpl.toImpl(firstCert).getAuthorityKeyIdentifierExtension());
            } catch (CertificateException e) {
            }
        }
        CertPathValidatorException lastException = null;
        for (TrustAnchor anchor : params.trustAnchors()) {
            X509Certificate trustedCert = anchor.getTrustedCert();
            if (trustedCert != null) {
                if (adaptableX509CertSelector == null || adaptableX509CertSelector.match(trustedCert)) {
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
        certPathCheckers.add(new UntrustedChecker());
        certPathCheckers.add(new AlgorithmChecker(anchor));
        certPathCheckers.add(new KeyChecker(certPathLen, params.targetCertConstraints()));
        certPathCheckers.add(new ConstraintsChecker(certPathLen));
        int i = certPathLen;
        PolicyChecker pc = new PolicyChecker(params.initialPolicies(), i, params.explicitPolicyRequired(), params.policyMappingInhibited(), params.anyPolicyInhibited(), params.policyQualifiersRejected(), new PolicyNodeImpl(null, "2.5.29.32.0", null, false, Collections.singleton("2.5.29.32.0"), false));
        certPathCheckers.add(pc);
        BasicChecker bc = new BasicChecker(anchor, params.date(), params.sigProvider(), false);
        certPathCheckers.add(bc);
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
        if (params.revocationEnabled() && !revCheckerAdded) {
            certPathCheckers.add(new RevocationChecker(anchor, params));
        }
        certPathCheckers.addAll(checkers);
        PKIXMasterCertPathValidator.validate(params.certPath(), params.certificates(), certPathCheckers);
        return new PKIXCertPathValidatorResult(anchor, pc.getPolicyTree(), bc.getPublicKey());
    }
}
