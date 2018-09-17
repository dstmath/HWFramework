package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.x500.X500Name;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.Extension;
import com.android.org.bouncycastle.jcajce.PKIXExtendedBuilderParameters;
import com.android.org.bouncycastle.jcajce.PKIXExtendedParameters;
import com.android.org.bouncycastle.jcajce.PKIXExtendedParameters.Builder;
import com.android.org.bouncycastle.jcajce.util.BCJcaJceHelper;
import com.android.org.bouncycastle.jcajce.util.JcaJceHelper;
import com.android.org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import com.android.org.bouncycastle.x509.ExtendedPKIXParameters;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertPathValidatorSpi;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PKIXCertPathValidatorSpi extends CertPathValidatorSpi {
    private final JcaJceHelper helper = new BCJcaJceHelper();

    private static class NoPreloadHolder {
        private static final CertBlacklist blacklist = new CertBlacklist();

        private NoPreloadHolder() {
        }
    }

    public CertPathValidatorResult engineValidate(CertPath certPath, CertPathParameters params) throws CertPathValidatorException, InvalidAlgorithmParameterException {
        PKIXExtendedParameters paramsPKIX;
        if (params instanceof PKIXParameters) {
            Builder builder = new Builder((PKIXParameters) params);
            if (params instanceof ExtendedPKIXParameters) {
                ExtendedPKIXParameters extPKIX = (ExtendedPKIXParameters) params;
                builder.setUseDeltasEnabled(extPKIX.isUseDeltasEnabled());
                builder.setValidityModel(extPKIX.getValidityModel());
            }
            paramsPKIX = builder.build();
        } else if (params instanceof PKIXExtendedBuilderParameters) {
            paramsPKIX = ((PKIXExtendedBuilderParameters) params).getBaseParameters();
        } else if (params instanceof PKIXExtendedParameters) {
            paramsPKIX = (PKIXExtendedParameters) params;
        } else {
            throw new InvalidAlgorithmParameterException("Parameters must be a " + PKIXParameters.class.getName() + " instance.");
        }
        if (paramsPKIX.getTrustAnchors() == null) {
            throw new InvalidAlgorithmParameterException("trustAnchors is null, this is not allowed for certification path validation.");
        }
        List certs = certPath.getCertificates();
        int n = certs.size();
        if (certs.isEmpty()) {
            throw new CertPathValidatorException("Certification path is empty.", null, certPath, -1);
        }
        String message;
        Throwable annotatedException;
        X509Certificate cert = (X509Certificate) certs.get(0);
        if (cert != null) {
            BigInteger serial = cert.getSerialNumber();
            if (NoPreloadHolder.blacklist.isSerialNumberBlackListed(serial)) {
                message = "Certificate revocation of serial 0x" + serial.toString(16);
                System.out.println(message);
                annotatedException = new AnnotatedException(message);
                throw new CertPathValidatorException(annotatedException.getMessage(), annotatedException, certPath, 0);
            }
        }
        Set userInitialPolicySet = paramsPKIX.getInitialPolicies();
        try {
            TrustAnchor trust = CertPathValidatorUtilities.findTrustAnchor((X509Certificate) certs.get(certs.size() - 1), paramsPKIX.getTrustAnchors(), paramsPKIX.getSigProvider());
            if (trust == null) {
                throw new CertPathValidatorException("Trust anchor for certification path not found.", null, certPath, -1);
            }
            int explicitPolicy;
            int inhibitAnyPolicy;
            int policyMapping;
            X500Name workingIssuerName;
            PublicKey workingPublicKey;
            paramsPKIX = new Builder(paramsPKIX).setTrustAnchor(trust).build();
            List[] policyNodes = new ArrayList[(n + 1)];
            for (int j = 0; j < policyNodes.length; j++) {
                policyNodes[j] = new ArrayList();
            }
            Set policySet = new HashSet();
            policySet.add(RFC3280CertPathUtilities.ANY_POLICY);
            PKIXPolicyNode validPolicyTree = new PKIXPolicyNode(new ArrayList(), 0, policySet, null, new HashSet(), RFC3280CertPathUtilities.ANY_POLICY, false);
            policyNodes[0].add(validPolicyTree);
            PKIXNameConstraintValidator nameConstraintValidator = new PKIXNameConstraintValidator();
            Set acceptablePolicies = new HashSet();
            if (paramsPKIX.isExplicitPolicyRequired()) {
                explicitPolicy = 0;
            } else {
                explicitPolicy = n + 1;
            }
            if (paramsPKIX.isAnyPolicyInhibited()) {
                inhibitAnyPolicy = 0;
            } else {
                inhibitAnyPolicy = n + 1;
            }
            if (paramsPKIX.isPolicyMappingInhibited()) {
                policyMapping = 0;
            } else {
                policyMapping = n + 1;
            }
            X509Certificate sign = trust.getTrustedCert();
            if (sign != null) {
                try {
                    workingIssuerName = PrincipalUtils.getSubjectPrincipal(sign);
                    workingPublicKey = sign.getPublicKey();
                } catch (Throwable ex) {
                    throw new ExtCertPathValidatorException("Subject of trust anchor could not be (re)encoded.", ex, certPath, -1);
                }
            }
            workingIssuerName = PrincipalUtils.getCA(trust);
            workingPublicKey = trust.getCAPublicKey();
            try {
                AlgorithmIdentifier workingAlgId = CertPathValidatorUtilities.getAlgorithmIdentifier(workingPublicKey);
                ASN1ObjectIdentifier workingPublicKeyAlgorithm = workingAlgId.getAlgorithm();
                ASN1Encodable workingPublicKeyParameters = workingAlgId.getParameters();
                int maxPathLength = n;
                if (paramsPKIX.getTargetConstraints() == null || (paramsPKIX.getTargetConstraints().match((X509Certificate) certs.get(0)) ^ 1) == 0) {
                    Set criticalExtensions;
                    Set hashSet;
                    List<PKIXCertPathChecker> pathCheckers = paramsPKIX.getCertPathCheckers();
                    for (PKIXCertPathChecker init : pathCheckers) {
                        init.init(false);
                    }
                    cert = null;
                    int index = certs.size() - 1;
                    while (index >= 0) {
                        if (NoPreloadHolder.blacklist.isPublicKeyBlackListed(workingPublicKey)) {
                            message = "Certificate revocation of public key " + workingPublicKey;
                            System.out.println(message);
                            annotatedException = new AnnotatedException(message);
                            throw new CertPathValidatorException(annotatedException.getMessage(), annotatedException, certPath, index);
                        }
                        int i = n - index;
                        cert = (X509Certificate) certs.get(index);
                        RFC3280CertPathUtilities.processCertA(certPath, paramsPKIX, index, workingPublicKey, index == certs.size() + -1, workingIssuerName, sign, this.helper);
                        RFC3280CertPathUtilities.processCertBC(certPath, index, nameConstraintValidator);
                        validPolicyTree = RFC3280CertPathUtilities.processCertE(certPath, index, RFC3280CertPathUtilities.processCertD(certPath, index, acceptablePolicies, validPolicyTree, policyNodes, inhibitAnyPolicy));
                        RFC3280CertPathUtilities.processCertF(certPath, index, validPolicyTree, explicitPolicy);
                        if (i != n) {
                            if (cert == null || cert.getVersion() != 1) {
                                RFC3280CertPathUtilities.prepareNextCertA(certPath, index);
                                validPolicyTree = RFC3280CertPathUtilities.prepareCertB(certPath, index, policyNodes, validPolicyTree, policyMapping);
                                RFC3280CertPathUtilities.prepareNextCertG(certPath, index, nameConstraintValidator);
                                explicitPolicy = RFC3280CertPathUtilities.prepareNextCertH1(certPath, index, explicitPolicy);
                                policyMapping = RFC3280CertPathUtilities.prepareNextCertH2(certPath, index, policyMapping);
                                inhibitAnyPolicy = RFC3280CertPathUtilities.prepareNextCertH3(certPath, index, inhibitAnyPolicy);
                                explicitPolicy = RFC3280CertPathUtilities.prepareNextCertI1(certPath, index, explicitPolicy);
                                policyMapping = RFC3280CertPathUtilities.prepareNextCertI2(certPath, index, policyMapping);
                                inhibitAnyPolicy = RFC3280CertPathUtilities.prepareNextCertJ(certPath, index, inhibitAnyPolicy);
                                RFC3280CertPathUtilities.prepareNextCertK(certPath, index);
                                maxPathLength = RFC3280CertPathUtilities.prepareNextCertM(certPath, index, RFC3280CertPathUtilities.prepareNextCertL(certPath, index, maxPathLength));
                                RFC3280CertPathUtilities.prepareNextCertN(certPath, index);
                                criticalExtensions = cert.getCriticalExtensionOIDs();
                                if (criticalExtensions != null) {
                                    hashSet = new HashSet(criticalExtensions);
                                    hashSet.remove(RFC3280CertPathUtilities.KEY_USAGE);
                                    hashSet.remove(RFC3280CertPathUtilities.CERTIFICATE_POLICIES);
                                    hashSet.remove(RFC3280CertPathUtilities.POLICY_MAPPINGS);
                                    hashSet.remove(RFC3280CertPathUtilities.INHIBIT_ANY_POLICY);
                                    hashSet.remove(RFC3280CertPathUtilities.ISSUING_DISTRIBUTION_POINT);
                                    hashSet.remove(RFC3280CertPathUtilities.DELTA_CRL_INDICATOR);
                                    hashSet.remove(RFC3280CertPathUtilities.POLICY_CONSTRAINTS);
                                    hashSet.remove(RFC3280CertPathUtilities.BASIC_CONSTRAINTS);
                                    hashSet.remove(RFC3280CertPathUtilities.SUBJECT_ALTERNATIVE_NAME);
                                    hashSet.remove(RFC3280CertPathUtilities.NAME_CONSTRAINTS);
                                    criticalExtensions = hashSet;
                                } else {
                                    criticalExtensions = new HashSet();
                                }
                                RFC3280CertPathUtilities.prepareNextCertO(certPath, index, criticalExtensions, pathCheckers);
                                sign = cert;
                                workingIssuerName = PrincipalUtils.getSubjectPrincipal(cert);
                                try {
                                    workingPublicKey = CertPathValidatorUtilities.getNextWorkingKey(certPath.getCertificates(), index, this.helper);
                                    workingAlgId = CertPathValidatorUtilities.getAlgorithmIdentifier(workingPublicKey);
                                    workingPublicKeyAlgorithm = workingAlgId.getAlgorithm();
                                    workingPublicKeyParameters = workingAlgId.getParameters();
                                } catch (Throwable e) {
                                    throw new CertPathValidatorException("Next working key could not be retrieved.", e, certPath, index);
                                }
                            }
                            throw new CertPathValidatorException("Version 1 certificates can't be used as CA ones.", null, certPath, index);
                        }
                        index--;
                    }
                    explicitPolicy = RFC3280CertPathUtilities.wrapupCertB(certPath, index + 1, RFC3280CertPathUtilities.wrapupCertA(explicitPolicy, cert));
                    criticalExtensions = cert.getCriticalExtensionOIDs();
                    if (criticalExtensions != null) {
                        hashSet = new HashSet(criticalExtensions);
                        hashSet.remove(RFC3280CertPathUtilities.KEY_USAGE);
                        hashSet.remove(RFC3280CertPathUtilities.CERTIFICATE_POLICIES);
                        hashSet.remove(RFC3280CertPathUtilities.POLICY_MAPPINGS);
                        hashSet.remove(RFC3280CertPathUtilities.INHIBIT_ANY_POLICY);
                        hashSet.remove(RFC3280CertPathUtilities.ISSUING_DISTRIBUTION_POINT);
                        hashSet.remove(RFC3280CertPathUtilities.DELTA_CRL_INDICATOR);
                        hashSet.remove(RFC3280CertPathUtilities.POLICY_CONSTRAINTS);
                        hashSet.remove(RFC3280CertPathUtilities.BASIC_CONSTRAINTS);
                        hashSet.remove(RFC3280CertPathUtilities.SUBJECT_ALTERNATIVE_NAME);
                        hashSet.remove(RFC3280CertPathUtilities.NAME_CONSTRAINTS);
                        hashSet.remove(RFC3280CertPathUtilities.CRL_DISTRIBUTION_POINTS);
                        hashSet.remove(Extension.extendedKeyUsage.getId());
                        criticalExtensions = hashSet;
                    } else {
                        criticalExtensions = new HashSet();
                    }
                    RFC3280CertPathUtilities.wrapupCertF(certPath, index + 1, pathCheckers, criticalExtensions);
                    PKIXPolicyNode intersection = RFC3280CertPathUtilities.wrapupCertG(certPath, paramsPKIX, userInitialPolicySet, index + 1, policyNodes, validPolicyTree, acceptablePolicies);
                    if (explicitPolicy > 0 || intersection != null) {
                        return new PKIXCertPathValidatorResult(trust, intersection, cert.getPublicKey());
                    }
                    throw new CertPathValidatorException("Path processing failed on policy.", null, certPath, index);
                }
                throw new ExtCertPathValidatorException("Target certificate in certification path does not match targetConstraints.", null, certPath, 0);
            } catch (Throwable e2) {
                throw new ExtCertPathValidatorException("Algorithm identifier of public key of trust anchor could not be read.", e2, certPath, -1);
            }
        } catch (Throwable e3) {
            throw new CertPathValidatorException(e3.getMessage(), e3, certPath, certs.size() - 1);
        }
    }
}
