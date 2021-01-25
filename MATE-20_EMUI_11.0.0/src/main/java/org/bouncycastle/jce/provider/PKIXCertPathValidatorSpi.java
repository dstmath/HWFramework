package org.bouncycastle.jce.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertPathValidatorSpi;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.TBSCertificate;
import org.bouncycastle.jcajce.PKIXExtendedBuilderParameters;
import org.bouncycastle.jcajce.PKIXExtendedParameters;
import org.bouncycastle.jcajce.interfaces.BCX509Certificate;
import org.bouncycastle.jcajce.util.BCJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import org.bouncycastle.x509.ExtendedPKIXParameters;

public class PKIXCertPathValidatorSpi extends CertPathValidatorSpi {
    private final JcaJceHelper helper;
    private final boolean isForCRLCheck;

    public PKIXCertPathValidatorSpi() {
        this(false);
    }

    public PKIXCertPathValidatorSpi(boolean z) {
        this.helper = new BCJcaJceHelper();
        this.isForCRLCheck = z;
    }

    static void checkCertificate(X509Certificate x509Certificate) throws AnnotatedException {
        if (x509Certificate instanceof BCX509Certificate) {
            RuntimeException runtimeException = null;
            try {
                if (((BCX509Certificate) x509Certificate).getTBSCertificateNative() != null) {
                    return;
                }
            } catch (RuntimeException e) {
                runtimeException = e;
            }
            throw new AnnotatedException("unable to process TBSCertificate", runtimeException);
        }
        try {
            TBSCertificate.getInstance(x509Certificate.getTBSCertificate());
        } catch (CertificateEncodingException e2) {
            throw new AnnotatedException("unable to process TBSCertificate", e2);
        } catch (IllegalArgumentException e3) {
            throw new AnnotatedException(e3.getMessage());
        }
    }

    @Override // java.security.cert.CertPathValidatorSpi
    public CertPathValidatorResult engineValidate(CertPath certPath, CertPathParameters certPathParameters) throws CertPathValidatorException, InvalidAlgorithmParameterException {
        PKIXExtendedParameters pKIXExtendedParameters;
        List<? extends Certificate> list;
        AnnotatedException e;
        PublicKey publicKey;
        X500Name x500Name;
        HashSet hashSet;
        List list2;
        int i;
        ArrayList[] arrayListArr;
        int i2;
        int i3;
        int i4;
        HashSet hashSet2;
        if (certPathParameters instanceof PKIXParameters) {
            PKIXExtendedParameters.Builder builder = new PKIXExtendedParameters.Builder((PKIXParameters) certPathParameters);
            if (certPathParameters instanceof ExtendedPKIXParameters) {
                ExtendedPKIXParameters extendedPKIXParameters = (ExtendedPKIXParameters) certPathParameters;
                builder.setUseDeltasEnabled(extendedPKIXParameters.isUseDeltasEnabled());
                builder.setValidityModel(extendedPKIXParameters.getValidityModel());
            }
            pKIXExtendedParameters = builder.build();
        } else if (certPathParameters instanceof PKIXExtendedBuilderParameters) {
            pKIXExtendedParameters = ((PKIXExtendedBuilderParameters) certPathParameters).getBaseParameters();
        } else if (certPathParameters instanceof PKIXExtendedParameters) {
            pKIXExtendedParameters = (PKIXExtendedParameters) certPathParameters;
        } else {
            throw new InvalidAlgorithmParameterException("Parameters must be a " + PKIXParameters.class.getName() + " instance.");
        }
        if (pKIXExtendedParameters.getTrustAnchors() != null) {
            List<? extends Certificate> certificates = certPath.getCertificates();
            int size = certificates.size();
            if (!certificates.isEmpty()) {
                Set initialPolicies = pKIXExtendedParameters.getInitialPolicies();
                try {
                    TrustAnchor findTrustAnchor = CertPathValidatorUtilities.findTrustAnchor((X509Certificate) certificates.get(certificates.size() - 1), pKIXExtendedParameters.getTrustAnchors(), pKIXExtendedParameters.getSigProvider());
                    if (findTrustAnchor != null) {
                        checkCertificate(findTrustAnchor.getTrustedCert());
                        PKIXExtendedParameters build = new PKIXExtendedParameters.Builder(pKIXExtendedParameters).setTrustAnchor(findTrustAnchor).build();
                        int i5 = size + 1;
                        ArrayList[] arrayListArr2 = new ArrayList[i5];
                        for (int i6 = 0; i6 < arrayListArr2.length; i6++) {
                            arrayListArr2[i6] = new ArrayList();
                        }
                        HashSet hashSet3 = new HashSet();
                        hashSet3.add(RFC3280CertPathUtilities.ANY_POLICY);
                        PKIXPolicyNode pKIXPolicyNode = new PKIXPolicyNode(new ArrayList(), 0, hashSet3, null, new HashSet(), RFC3280CertPathUtilities.ANY_POLICY, false);
                        arrayListArr2[0].add(pKIXPolicyNode);
                        PKIXNameConstraintValidator pKIXNameConstraintValidator = new PKIXNameConstraintValidator();
                        HashSet hashSet4 = new HashSet();
                        int i7 = build.isExplicitPolicyRequired() ? 0 : i5;
                        int i8 = build.isAnyPolicyInhibited() ? 0 : i5;
                        if (build.isPolicyMappingInhibited()) {
                            i5 = 0;
                        }
                        X509Certificate trustedCert = findTrustAnchor.getTrustedCert();
                        if (trustedCert != null) {
                            try {
                                x500Name = PrincipalUtils.getSubjectPrincipal(trustedCert);
                                publicKey = trustedCert.getPublicKey();
                            } catch (RuntimeException e2) {
                                throw new ExtCertPathValidatorException("Subject of trust anchor could not be (re)encoded.", e2, certPath, -1);
                            }
                        } else {
                            x500Name = PrincipalUtils.getCA(findTrustAnchor);
                            publicKey = findTrustAnchor.getCAPublicKey();
                        }
                        try {
                            AlgorithmIdentifier algorithmIdentifier = CertPathValidatorUtilities.getAlgorithmIdentifier(publicKey);
                            algorithmIdentifier.getAlgorithm();
                            algorithmIdentifier.getParameters();
                            if (build.getTargetConstraints() == null || build.getTargetConstraints().match((Certificate) ((X509Certificate) certificates.get(0)))) {
                                List<PKIXCertPathChecker> certPathCheckers = build.getCertPathCheckers();
                                for (PKIXCertPathChecker pKIXCertPathChecker : certPathCheckers) {
                                    pKIXCertPathChecker.init(false);
                                }
                                int i9 = size;
                                X509Certificate x509Certificate = trustedCert;
                                PKIXPolicyNode pKIXPolicyNode2 = pKIXPolicyNode;
                                int i10 = i7;
                                int i11 = i8;
                                int size2 = certificates.size() - 1;
                                X509Certificate x509Certificate2 = null;
                                while (size2 >= 0) {
                                    int i12 = size - size2;
                                    X509Certificate x509Certificate3 = (X509Certificate) certificates.get(size2);
                                    boolean z = size2 == certificates.size() + -1;
                                    try {
                                        checkCertificate(x509Certificate3);
                                        RFC3280CertPathUtilities.processCertA(certPath, build, size2, publicKey, z, x500Name, x509Certificate, this.helper);
                                        RFC3280CertPathUtilities.processCertBC(certPath, size2, pKIXNameConstraintValidator, this.isForCRLCheck);
                                        PKIXPolicyNode processCertE = RFC3280CertPathUtilities.processCertE(certPath, size2, RFC3280CertPathUtilities.processCertD(certPath, size2, hashSet4, pKIXPolicyNode2, arrayListArr2, i11, this.isForCRLCheck));
                                        RFC3280CertPathUtilities.processCertF(certPath, size2, processCertE, i10);
                                        if (i12 == size) {
                                            i2 = i9;
                                            i3 = i11;
                                            list2 = certPathCheckers;
                                            i4 = i5;
                                            x509Certificate2 = x509Certificate3;
                                        } else if (x509Certificate3 == null || x509Certificate3.getVersion() != 1) {
                                            x509Certificate2 = x509Certificate3;
                                            RFC3280CertPathUtilities.prepareNextCertA(certPath, size2);
                                            arrayListArr = arrayListArr2;
                                            PKIXPolicyNode prepareCertB = RFC3280CertPathUtilities.prepareCertB(certPath, size2, arrayListArr, processCertE, i5);
                                            RFC3280CertPathUtilities.prepareNextCertG(certPath, size2, pKIXNameConstraintValidator);
                                            int prepareNextCertH1 = RFC3280CertPathUtilities.prepareNextCertH1(certPath, size2, i10);
                                            int prepareNextCertH2 = RFC3280CertPathUtilities.prepareNextCertH2(certPath, size2, i5);
                                            int prepareNextCertH3 = RFC3280CertPathUtilities.prepareNextCertH3(certPath, size2, i11);
                                            i = RFC3280CertPathUtilities.prepareNextCertI1(certPath, size2, prepareNextCertH1);
                                            int prepareNextCertI2 = RFC3280CertPathUtilities.prepareNextCertI2(certPath, size2, prepareNextCertH2);
                                            int prepareNextCertJ = RFC3280CertPathUtilities.prepareNextCertJ(certPath, size2, prepareNextCertH3);
                                            RFC3280CertPathUtilities.prepareNextCertK(certPath, size2);
                                            int prepareNextCertM = RFC3280CertPathUtilities.prepareNextCertM(certPath, size2, RFC3280CertPathUtilities.prepareNextCertL(certPath, size2, i9));
                                            RFC3280CertPathUtilities.prepareNextCertN(certPath, size2);
                                            if (x509Certificate2.getCriticalExtensionOIDs() != null) {
                                                hashSet2.remove(RFC3280CertPathUtilities.KEY_USAGE);
                                                hashSet2.remove(RFC3280CertPathUtilities.CERTIFICATE_POLICIES);
                                                hashSet2.remove(RFC3280CertPathUtilities.POLICY_MAPPINGS);
                                                hashSet2.remove(RFC3280CertPathUtilities.INHIBIT_ANY_POLICY);
                                                hashSet2.remove(RFC3280CertPathUtilities.ISSUING_DISTRIBUTION_POINT);
                                                hashSet2.remove(RFC3280CertPathUtilities.DELTA_CRL_INDICATOR);
                                                hashSet2.remove(RFC3280CertPathUtilities.POLICY_CONSTRAINTS);
                                                hashSet2.remove(RFC3280CertPathUtilities.BASIC_CONSTRAINTS);
                                                hashSet2.remove(RFC3280CertPathUtilities.SUBJECT_ALTERNATIVE_NAME);
                                                hashSet2.remove(RFC3280CertPathUtilities.NAME_CONSTRAINTS);
                                            } else {
                                                hashSet2 = new HashSet();
                                            }
                                            list2 = certPathCheckers;
                                            RFC3280CertPathUtilities.prepareNextCertO(certPath, size2, hashSet2, list2);
                                            x500Name = PrincipalUtils.getSubjectPrincipal(x509Certificate2);
                                            try {
                                                publicKey = CertPathValidatorUtilities.getNextWorkingKey(certPath.getCertificates(), size2, this.helper);
                                                AlgorithmIdentifier algorithmIdentifier2 = CertPathValidatorUtilities.getAlgorithmIdentifier(publicKey);
                                                algorithmIdentifier2.getAlgorithm();
                                                algorithmIdentifier2.getParameters();
                                                pKIXPolicyNode2 = prepareCertB;
                                                i5 = prepareNextCertI2;
                                                x509Certificate = x509Certificate2;
                                                i11 = prepareNextCertJ;
                                                i9 = prepareNextCertM;
                                                certPathCheckers = list2;
                                                certificates = certificates;
                                                findTrustAnchor = findTrustAnchor;
                                                arrayListArr2 = arrayListArr;
                                                i10 = i;
                                                size2--;
                                                pKIXNameConstraintValidator = pKIXNameConstraintValidator;
                                                initialPolicies = initialPolicies;
                                            } catch (CertPathValidatorException e3) {
                                                throw new CertPathValidatorException("Next working key could not be retrieved.", e3, certPath, size2);
                                            }
                                        } else {
                                            if (i12 == 1) {
                                                x509Certificate2 = x509Certificate3;
                                                if (x509Certificate2.equals(findTrustAnchor.getTrustedCert())) {
                                                    i2 = i9;
                                                    i3 = i11;
                                                    list2 = certPathCheckers;
                                                    i4 = i5;
                                                }
                                            }
                                            throw new CertPathValidatorException("Version 1 certificates can't be used as CA ones.", null, certPath, size2);
                                        }
                                        arrayListArr = arrayListArr2;
                                        pKIXPolicyNode2 = processCertE;
                                        i5 = i4;
                                        i11 = i3;
                                        i9 = i2;
                                        i = i10;
                                        certPathCheckers = list2;
                                        certificates = certificates;
                                        findTrustAnchor = findTrustAnchor;
                                        arrayListArr2 = arrayListArr;
                                        i10 = i;
                                        size2--;
                                        pKIXNameConstraintValidator = pKIXNameConstraintValidator;
                                        initialPolicies = initialPolicies;
                                    } catch (AnnotatedException e4) {
                                        throw new CertPathValidatorException(e4.getMessage(), e4.getUnderlyingException(), certPath, size2);
                                    }
                                }
                                int i13 = size2 + 1;
                                int wrapupCertB = RFC3280CertPathUtilities.wrapupCertB(certPath, i13, RFC3280CertPathUtilities.wrapupCertA(i10, x509Certificate2));
                                if (x509Certificate2.getCriticalExtensionOIDs() != null) {
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
                                } else {
                                    hashSet = new HashSet();
                                }
                                RFC3280CertPathUtilities.wrapupCertF(certPath, i13, certPathCheckers, hashSet);
                                PKIXPolicyNode wrapupCertG = RFC3280CertPathUtilities.wrapupCertG(certPath, build, initialPolicies, i13, arrayListArr2, pKIXPolicyNode2, hashSet4);
                                if (wrapupCertB > 0 || wrapupCertG != null) {
                                    return new PKIXCertPathValidatorResult(findTrustAnchor, wrapupCertG, x509Certificate2.getPublicKey());
                                }
                                throw new CertPathValidatorException("Path processing failed on policy.", null, certPath, size2);
                            }
                            throw new ExtCertPathValidatorException("Target certificate in certification path does not match targetConstraints.", null, certPath, 0);
                        } catch (CertPathValidatorException e5) {
                            throw new ExtCertPathValidatorException("Algorithm identifier of public key of trust anchor could not be read.", e5, certPath, -1);
                        }
                    } else {
                        list = certificates;
                        try {
                            throw new CertPathValidatorException("Trust anchor for certification path not found.", null, certPath, -1);
                        } catch (AnnotatedException e6) {
                            e = e6;
                            throw new CertPathValidatorException(e.getMessage(), e.getUnderlyingException(), certPath, list.size() - 1);
                        }
                    }
                } catch (AnnotatedException e7) {
                    e = e7;
                    list = certificates;
                    throw new CertPathValidatorException(e.getMessage(), e.getUnderlyingException(), certPath, list.size() - 1);
                }
            } else {
                throw new CertPathValidatorException("Certification path is empty.", null, certPath, -1);
            }
        } else {
            throw new InvalidAlgorithmParameterException("trustAnchors is null, this is not allowed for certification path validation.");
        }
    }
}
