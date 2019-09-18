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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.TBSCertificate;
import org.bouncycastle.jcajce.PKIXExtendedBuilderParameters;
import org.bouncycastle.jcajce.PKIXExtendedParameters;
import org.bouncycastle.jcajce.util.BCJcaJceHelper;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import org.bouncycastle.x509.ExtendedPKIXParameters;

public class PKIXCertPathValidatorSpi extends CertPathValidatorSpi {
    private final JcaJceHelper helper = new BCJcaJceHelper();

    static void checkCertificate(X509Certificate x509Certificate) throws AnnotatedException {
        try {
            TBSCertificate.getInstance(x509Certificate.getTBSCertificate());
        } catch (CertificateEncodingException e) {
            throw new AnnotatedException("unable to process TBSCertificate", e);
        } catch (IllegalArgumentException e2) {
            throw new AnnotatedException(e2.getMessage());
        }
    }

    public CertPathValidatorResult engineValidate(CertPath certPath, CertPathParameters certPathParameters) throws CertPathValidatorException, InvalidAlgorithmParameterException {
        PKIXExtendedParameters pKIXExtendedParameters;
        List<? extends Certificate> list;
        X500Name ca;
        PublicKey cAPublicKey;
        ArrayList[] arrayListArr;
        HashSet hashSet;
        PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi;
        TrustAnchor trustAnchor;
        List list2;
        ArrayList[] arrayListArr2;
        int i;
        int i2;
        int i3;
        HashSet hashSet2;
        PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi2 = this;
        CertPath certPath2 = certPath;
        CertPathParameters certPathParameters2 = certPathParameters;
        if (certPathParameters2 instanceof PKIXParameters) {
            PKIXExtendedParameters.Builder builder = new PKIXExtendedParameters.Builder((PKIXParameters) certPathParameters2);
            if (certPathParameters2 instanceof ExtendedPKIXParameters) {
                ExtendedPKIXParameters extendedPKIXParameters = (ExtendedPKIXParameters) certPathParameters2;
                builder.setUseDeltasEnabled(extendedPKIXParameters.isUseDeltasEnabled());
                builder.setValidityModel(extendedPKIXParameters.getValidityModel());
            }
            pKIXExtendedParameters = builder.build();
        } else if (certPathParameters2 instanceof PKIXExtendedBuilderParameters) {
            pKIXExtendedParameters = ((PKIXExtendedBuilderParameters) certPathParameters2).getBaseParameters();
        } else if (certPathParameters2 instanceof PKIXExtendedParameters) {
            pKIXExtendedParameters = (PKIXExtendedParameters) certPathParameters2;
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
                        int i4 = size + 1;
                        ArrayList[] arrayListArr3 = new ArrayList[i4];
                        for (int i5 = 0; i5 < arrayListArr3.length; i5++) {
                            arrayListArr3[i5] = new ArrayList();
                        }
                        HashSet hashSet3 = new HashSet();
                        hashSet3.add(RFC3280CertPathUtilities.ANY_POLICY);
                        PKIXPolicyNode pKIXPolicyNode = new PKIXPolicyNode(new ArrayList(), 0, hashSet3, null, new HashSet(), RFC3280CertPathUtilities.ANY_POLICY, false);
                        arrayListArr3[0].add(pKIXPolicyNode);
                        PKIXNameConstraintValidator pKIXNameConstraintValidator = new PKIXNameConstraintValidator();
                        HashSet hashSet4 = new HashSet();
                        int i6 = build.isExplicitPolicyRequired() ? 0 : i4;
                        int i7 = build.isAnyPolicyInhibited() ? 0 : i4;
                        if (build.isPolicyMappingInhibited()) {
                            i4 = 0;
                        }
                        X509Certificate trustedCert = findTrustAnchor.getTrustedCert();
                        if (trustedCert != null) {
                            try {
                                ca = PrincipalUtils.getSubjectPrincipal(trustedCert);
                                cAPublicKey = trustedCert.getPublicKey();
                            } catch (IllegalArgumentException e) {
                                throw new ExtCertPathValidatorException("Subject of trust anchor could not be (re)encoded.", e, certPath2, -1);
                            }
                        } else {
                            ca = PrincipalUtils.getCA(findTrustAnchor);
                            cAPublicKey = findTrustAnchor.getCAPublicKey();
                        }
                        PublicKey publicKey = cAPublicKey;
                        try {
                            AlgorithmIdentifier algorithmIdentifier = CertPathValidatorUtilities.getAlgorithmIdentifier(publicKey);
                            algorithmIdentifier.getAlgorithm();
                            algorithmIdentifier.getParameters();
                            if (build.getTargetConstraints() != null) {
                                arrayListArr = arrayListArr3;
                                if (!build.getTargetConstraints().match((Certificate) (X509Certificate) certificates.get(0))) {
                                    throw new ExtCertPathValidatorException("Target certificate in certification path does not match targetConstraints.", null, certPath2, 0);
                                }
                            } else {
                                arrayListArr = arrayListArr3;
                            }
                            List certPathCheckers = build.getCertPathCheckers();
                            for (Iterator it = certPathCheckers.iterator(); it.hasNext(); it = it) {
                                ((PKIXCertPathChecker) it.next()).init(false);
                                i4 = i4;
                            }
                            Set set = initialPolicies;
                            X509Certificate x509Certificate = trustedCert;
                            X500Name x500Name = ca;
                            int i8 = i6;
                            int i9 = size;
                            PublicKey publicKey2 = publicKey;
                            int i10 = i7;
                            int size2 = certificates.size() - 1;
                            PKIXPolicyNode pKIXPolicyNode2 = pKIXPolicyNode;
                            int i11 = i4;
                            X509Certificate x509Certificate2 = null;
                            while (size2 >= 0) {
                                int i12 = size - size2;
                                int i13 = i9;
                                X509Certificate x509Certificate3 = (X509Certificate) certificates.get(size2);
                                boolean z = size2 == certificates.size() + -1;
                                try {
                                    checkCertificate(x509Certificate3);
                                    List<? extends Certificate> list3 = certificates;
                                    JcaJceHelper jcaJceHelper = pKIXCertPathValidatorSpi2.helper;
                                    int i14 = i11;
                                    CertPath certPath3 = certPath2;
                                    int i15 = i10;
                                    HashSet hashSet5 = hashSet4;
                                    int i16 = i14;
                                    int i17 = i12;
                                    TrustAnchor trustAnchor2 = findTrustAnchor;
                                    PKIXNameConstraintValidator pKIXNameConstraintValidator2 = pKIXNameConstraintValidator;
                                    List list4 = certPathCheckers;
                                    ArrayList[] arrayListArr4 = arrayListArr;
                                    PKIXExtendedParameters pKIXExtendedParameters2 = build;
                                    RFC3280CertPathUtilities.processCertA(certPath3, build, size2, publicKey2, z, x500Name, x509Certificate, jcaJceHelper);
                                    RFC3280CertPathUtilities.processCertBC(certPath2, size2, pKIXNameConstraintValidator2);
                                    PKIXPolicyNode processCertE = RFC3280CertPathUtilities.processCertE(certPath2, size2, RFC3280CertPathUtilities.processCertD(certPath3, size2, hashSet5, pKIXPolicyNode2, arrayListArr4, i15));
                                    RFC3280CertPathUtilities.processCertF(certPath2, size2, processCertE, i8);
                                    if (i17 == size) {
                                        i = i13;
                                        i3 = i15;
                                        i2 = i16;
                                        trustAnchor = trustAnchor2;
                                    } else if (x509Certificate3 == null || x509Certificate3.getVersion() != 1) {
                                        trustAnchor = trustAnchor2;
                                        RFC3280CertPathUtilities.prepareNextCertA(certPath2, size2);
                                        int i18 = i16;
                                        arrayListArr2 = arrayListArr4;
                                        PKIXPolicyNode prepareCertB = RFC3280CertPathUtilities.prepareCertB(certPath2, size2, arrayListArr2, processCertE, i18);
                                        RFC3280CertPathUtilities.prepareNextCertG(certPath2, size2, pKIXNameConstraintValidator2);
                                        int prepareNextCertH1 = RFC3280CertPathUtilities.prepareNextCertH1(certPath2, size2, i8);
                                        int prepareNextCertH2 = RFC3280CertPathUtilities.prepareNextCertH2(certPath2, size2, i18);
                                        int prepareNextCertH3 = RFC3280CertPathUtilities.prepareNextCertH3(certPath2, size2, i15);
                                        int prepareNextCertI1 = RFC3280CertPathUtilities.prepareNextCertI1(certPath2, size2, prepareNextCertH1);
                                        int prepareNextCertI2 = RFC3280CertPathUtilities.prepareNextCertI2(certPath2, size2, prepareNextCertH2);
                                        int prepareNextCertJ = RFC3280CertPathUtilities.prepareNextCertJ(certPath2, size2, prepareNextCertH3);
                                        RFC3280CertPathUtilities.prepareNextCertK(certPath2, size2);
                                        i = RFC3280CertPathUtilities.prepareNextCertM(certPath2, size2, RFC3280CertPathUtilities.prepareNextCertL(certPath2, size2, i13));
                                        RFC3280CertPathUtilities.prepareNextCertN(certPath2, size2);
                                        Set criticalExtensionOIDs = x509Certificate3.getCriticalExtensionOIDs();
                                        if (criticalExtensionOIDs != null) {
                                            hashSet2 = new HashSet(criticalExtensionOIDs);
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
                                        list2 = list4;
                                        RFC3280CertPathUtilities.prepareNextCertO(certPath2, size2, hashSet2, list2);
                                        x500Name = PrincipalUtils.getSubjectPrincipal(x509Certificate3);
                                        try {
                                            pKIXCertPathValidatorSpi = this;
                                            PublicKey nextWorkingKey = CertPathValidatorUtilities.getNextWorkingKey(certPath.getCertificates(), size2, pKIXCertPathValidatorSpi.helper);
                                            AlgorithmIdentifier algorithmIdentifier2 = CertPathValidatorUtilities.getAlgorithmIdentifier(nextWorkingKey);
                                            algorithmIdentifier2.getAlgorithm();
                                            algorithmIdentifier2.getParameters();
                                            pKIXPolicyNode2 = prepareCertB;
                                            i8 = prepareNextCertI1;
                                            i10 = prepareNextCertJ;
                                            publicKey2 = nextWorkingKey;
                                            x509Certificate = x509Certificate3;
                                            i11 = prepareNextCertI2;
                                            size2--;
                                            arrayListArr = arrayListArr2;
                                            pKIXCertPathValidatorSpi2 = pKIXCertPathValidatorSpi;
                                            pKIXNameConstraintValidator = pKIXNameConstraintValidator2;
                                            hashSet4 = hashSet5;
                                            build = pKIXExtendedParameters2;
                                            certificates = list3;
                                            findTrustAnchor = trustAnchor;
                                            certPathCheckers = list2;
                                            X509Certificate x509Certificate4 = x509Certificate3;
                                            i9 = i;
                                            x509Certificate2 = x509Certificate4;
                                        } catch (CertPathValidatorException e2) {
                                            throw new CertPathValidatorException("Next working key could not be retrieved.", e2, certPath2, size2);
                                        }
                                    } else {
                                        if (i17 == 1) {
                                            trustAnchor = trustAnchor2;
                                            if (x509Certificate3.equals(trustAnchor.getTrustedCert())) {
                                                i = i13;
                                                i3 = i15;
                                                i2 = i16;
                                            }
                                        }
                                        throw new CertPathValidatorException("Version 1 certificates can't be used as CA ones.", null, certPath2, size2);
                                    }
                                    arrayListArr2 = arrayListArr4;
                                    list2 = list4;
                                    pKIXCertPathValidatorSpi = this;
                                    pKIXPolicyNode2 = processCertE;
                                    i10 = i3;
                                    i11 = i2;
                                    size2--;
                                    arrayListArr = arrayListArr2;
                                    pKIXCertPathValidatorSpi2 = pKIXCertPathValidatorSpi;
                                    pKIXNameConstraintValidator = pKIXNameConstraintValidator2;
                                    hashSet4 = hashSet5;
                                    build = pKIXExtendedParameters2;
                                    certificates = list3;
                                    findTrustAnchor = trustAnchor;
                                    certPathCheckers = list2;
                                    X509Certificate x509Certificate42 = x509Certificate3;
                                    i9 = i;
                                    x509Certificate2 = x509Certificate42;
                                } catch (AnnotatedException e3) {
                                    AnnotatedException annotatedException = e3;
                                    throw new CertPathValidatorException(annotatedException.getMessage(), annotatedException.getUnderlyingException(), certPath2, size2);
                                }
                            }
                            HashSet hashSet6 = hashSet4;
                            List list5 = certPathCheckers;
                            PKIXExtendedParameters pKIXExtendedParameters3 = build;
                            TrustAnchor trustAnchor3 = findTrustAnchor;
                            ArrayList[] arrayListArr5 = arrayListArr;
                            int i19 = size2 + 1;
                            int wrapupCertB = RFC3280CertPathUtilities.wrapupCertB(certPath2, i19, RFC3280CertPathUtilities.wrapupCertA(i8, x509Certificate2));
                            Set criticalExtensionOIDs2 = x509Certificate2.getCriticalExtensionOIDs();
                            if (criticalExtensionOIDs2 != null) {
                                hashSet = new HashSet(criticalExtensionOIDs2);
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
                            RFC3280CertPathUtilities.wrapupCertF(certPath2, i19, list5, hashSet);
                            X509Certificate x509Certificate5 = x509Certificate2;
                            PKIXPolicyNode wrapupCertG = RFC3280CertPathUtilities.wrapupCertG(certPath2, pKIXExtendedParameters3, set, i19, arrayListArr5, pKIXPolicyNode2, hashSet6);
                            if (wrapupCertB > 0 || wrapupCertG != null) {
                                return new PKIXCertPathValidatorResult(trustAnchor3, wrapupCertG, x509Certificate5.getPublicKey());
                            }
                            throw new CertPathValidatorException("Path processing failed on policy.", null, certPath2, size2);
                        } catch (CertPathValidatorException e4) {
                            throw new ExtCertPathValidatorException("Algorithm identifier of public key of trust anchor could not be read.", e4, certPath2, -1);
                        }
                    } else {
                        list = certificates;
                        try {
                            throw new CertPathValidatorException("Trust anchor for certification path not found.", null, certPath2, -1);
                        } catch (AnnotatedException e5) {
                            e = e5;
                            throw new CertPathValidatorException(e.getMessage(), e.getUnderlyingException(), certPath2, list.size() - 1);
                        }
                    }
                } catch (AnnotatedException e6) {
                    e = e6;
                    list = certificates;
                    throw new CertPathValidatorException(e.getMessage(), e.getUnderlyingException(), certPath2, list.size() - 1);
                }
            } else {
                throw new CertPathValidatorException("Certification path is empty.", null, certPath2, -1);
            }
        } else {
            throw new InvalidAlgorithmParameterException("trustAnchors is null, this is not allowed for certification path validation.");
        }
    }
}
