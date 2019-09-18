package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.x500.X500Name;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.Extension;
import com.android.org.bouncycastle.jcajce.PKIXExtendedBuilderParameters;
import com.android.org.bouncycastle.jcajce.PKIXExtendedParameters;
import com.android.org.bouncycastle.jcajce.util.BCJcaJceHelper;
import com.android.org.bouncycastle.jcajce.util.JcaJceHelper;
import com.android.org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import com.android.org.bouncycastle.x509.ExtendedPKIXParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertPathValidatorSpi;
import java.security.cert.Certificate;
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

public class PKIXCertPathValidatorSpi extends CertPathValidatorSpi {
    private final JcaJceHelper helper = new BCJcaJceHelper();

    private static class NoPreloadHolder {
        /* access modifiers changed from: private */
        public static final CertBlacklist blacklist = new CertBlacklist();

        private NoPreloadHolder() {
        }
    }

    public CertPathValidatorResult engineValidate(CertPath certPath, CertPathParameters params) throws CertPathValidatorException, InvalidAlgorithmParameterException {
        PKIXExtendedParameters paramsPKIX;
        int explicitPolicy;
        int inhibitAnyPolicy;
        int policyMapping;
        CertPath certPath2;
        int i;
        PublicKey workingPublicKey;
        X500Name workingIssuerName;
        PublicKey workingPublicKey2;
        PKIXPolicyNode validPolicyTree;
        Iterator certIter;
        HashSet hashSet;
        X509Certificate cert;
        int n;
        int explicitPolicy2;
        List[] policyNodes;
        PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi;
        List pathCheckers;
        PublicKey workingPublicKey3;
        PKIXPolicyNode validPolicyTree2;
        HashSet hashSet2;
        PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi2 = this;
        CertPath certPath3 = certPath;
        CertPathParameters certPathParameters = params;
        if (certPathParameters instanceof PKIXParameters) {
            PKIXExtendedParameters.Builder paramsPKIXBldr = new PKIXExtendedParameters.Builder((PKIXParameters) certPathParameters);
            if (certPathParameters instanceof ExtendedPKIXParameters) {
                ExtendedPKIXParameters extPKIX = (ExtendedPKIXParameters) certPathParameters;
                paramsPKIXBldr.setUseDeltasEnabled(extPKIX.isUseDeltasEnabled());
                paramsPKIXBldr.setValidityModel(extPKIX.getValidityModel());
            }
            paramsPKIX = paramsPKIXBldr.build();
        } else if (certPathParameters instanceof PKIXExtendedBuilderParameters) {
            paramsPKIX = ((PKIXExtendedBuilderParameters) certPathParameters).getBaseParameters();
        } else if (certPathParameters instanceof PKIXExtendedParameters) {
            paramsPKIX = (PKIXExtendedParameters) certPathParameters;
        } else {
            PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi3 = pKIXCertPathValidatorSpi2;
            CertPath certPath4 = certPath3;
            throw new InvalidAlgorithmParameterException("Parameters must be a " + PKIXParameters.class.getName() + " instance.");
        }
        PKIXExtendedParameters paramsPKIX2 = paramsPKIX;
        if (paramsPKIX2.getTrustAnchors() != null) {
            List certs = certPath.getCertificates();
            int n2 = certs.size();
            if (!certs.isEmpty()) {
                X509Certificate cert2 = (X509Certificate) certs.get(0);
                if (cert2 != null) {
                    if (NoPreloadHolder.blacklist.isSerialNumberBlackListed(cert2.getSerialNumber())) {
                        String message = "Certificate revocation of serial 0x" + serial.toString(16);
                        System.out.println(message);
                        AnnotatedException e = new AnnotatedException(message);
                        throw new CertPathValidatorException(e.getMessage(), e, certPath3, 0);
                    }
                }
                Set userInitialPolicySet = paramsPKIX2.getInitialPolicies();
                try {
                    TrustAnchor trust = CertPathValidatorUtilities.findTrustAnchor((X509Certificate) certs.get(certs.size() - 1), paramsPKIX2.getTrustAnchors(), paramsPKIX2.getSigProvider());
                    if (trust != null) {
                        PKIXExtendedParameters paramsPKIX3 = new PKIXExtendedParameters.Builder(paramsPKIX2).setTrustAnchor(trust).build();
                        List[] listArr = new ArrayList[(n2 + 1)];
                        for (int j = 0; j < listArr.length; j++) {
                            listArr[j] = new ArrayList();
                        }
                        Set hashSet3 = new HashSet();
                        hashSet3.add(RFC3280CertPathUtilities.ANY_POLICY);
                        PKIXPolicyNode pKIXPolicyNode = new PKIXPolicyNode(new ArrayList(), 0, hashSet3, null, new HashSet(), RFC3280CertPathUtilities.ANY_POLICY, false);
                        PKIXPolicyNode validPolicyTree3 = pKIXPolicyNode;
                        listArr[0].add(validPolicyTree3);
                        PKIXNameConstraintValidator nameConstraintValidator = new PKIXNameConstraintValidator();
                        Set acceptablePolicies = new HashSet();
                        if (paramsPKIX3.isExplicitPolicyRequired()) {
                            explicitPolicy = 0;
                        } else {
                            explicitPolicy = n2 + 1;
                        }
                        if (paramsPKIX3.isAnyPolicyInhibited()) {
                            inhibitAnyPolicy = 0;
                        } else {
                            inhibitAnyPolicy = n2 + 1;
                        }
                        if (paramsPKIX3.isPolicyMappingInhibited()) {
                            policyMapping = 0;
                        } else {
                            policyMapping = n2 + 1;
                        }
                        X509Certificate sign = trust.getTrustedCert();
                        if (sign != null) {
                            try {
                                workingIssuerName = PrincipalUtils.getSubjectPrincipal(sign);
                                workingPublicKey = sign.getPublicKey();
                            } catch (IllegalArgumentException e2) {
                                ex = e2;
                                PKIXPolicyNode pKIXPolicyNode2 = validPolicyTree3;
                                HashSet hashSet4 = hashSet3;
                                PKIXExtendedParameters pKIXExtendedParameters = paramsPKIX3;
                                int i2 = n2;
                                i = -1;
                                PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi4 = pKIXCertPathValidatorSpi2;
                                PKIXNameConstraintValidator pKIXNameConstraintValidator = nameConstraintValidator;
                                TrustAnchor trustAnchor = trust;
                                certPath2 = certPath3;
                                List<? extends Certificate> list = certs;
                                ArrayList[] arrayListArr = listArr;
                                List<? extends Certificate> list2 = list;
                                throw new ExtCertPathValidatorException("Subject of trust anchor could not be (re)encoded.", ex, certPath2, i);
                            }
                        } else {
                            try {
                                workingIssuerName = PrincipalUtils.getCA(trust);
                                workingPublicKey = trust.getCAPublicKey();
                            } catch (IllegalArgumentException e3) {
                                ex = e3;
                                PKIXPolicyNode pKIXPolicyNode3 = validPolicyTree3;
                                HashSet hashSet5 = hashSet3;
                                PKIXExtendedParameters pKIXExtendedParameters2 = paramsPKIX3;
                                int i3 = n2;
                                i = -1;
                                PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi5 = pKIXCertPathValidatorSpi2;
                                PKIXNameConstraintValidator pKIXNameConstraintValidator2 = nameConstraintValidator;
                                TrustAnchor trustAnchor2 = trust;
                                certPath2 = certPath3;
                                List<? extends Certificate> list3 = certs;
                                ArrayList[] arrayListArr2 = listArr;
                                List<? extends Certificate> list4 = list3;
                                throw new ExtCertPathValidatorException("Subject of trust anchor could not be (re)encoded.", ex, certPath2, i);
                            }
                        }
                        PublicKey workingPublicKey4 = workingPublicKey;
                        try {
                            AlgorithmIdentifier workingAlgId = CertPathValidatorUtilities.getAlgorithmIdentifier(workingPublicKey4);
                            ASN1ObjectIdentifier workingPublicKeyAlgorithm = workingAlgId.getAlgorithm();
                            ASN1Encodable workingPublicKeyParameters = workingAlgId.getParameters();
                            int maxPathLength = n2;
                            if (paramsPKIX3.getTargetConstraints() != null) {
                                AlgorithmIdentifier algorithmIdentifier = workingAlgId;
                                if (paramsPKIX3.getTargetConstraints().match((Certificate) (X509Certificate) certs.get(0))) {
                                    validPolicyTree = validPolicyTree3;
                                    workingPublicKey2 = workingPublicKey4;
                                } else {
                                    PKIXPolicyNode pKIXPolicyNode4 = validPolicyTree3;
                                    PublicKey publicKey = workingPublicKey4;
                                    throw new ExtCertPathValidatorException("Target certificate in certification path does not match targetConstraints.", null, certPath3, 0);
                                }
                            } else {
                                validPolicyTree = validPolicyTree3;
                                workingPublicKey2 = workingPublicKey4;
                            }
                            List pathCheckers2 = paramsPKIX3.getCertPathCheckers();
                            Iterator certIter2 = pathCheckers2.iterator();
                            while (true) {
                                certIter = certIter2;
                                if (!certIter.hasNext()) {
                                    break;
                                }
                                ((PKIXCertPathChecker) certIter.next()).init(false);
                                certIter2 = certIter;
                                acceptablePolicies = acceptablePolicies;
                            }
                            Set acceptablePolicies2 = acceptablePolicies;
                            X509Certificate cert3 = null;
                            int explicitPolicy3 = explicitPolicy;
                            int inhibitAnyPolicy2 = inhibitAnyPolicy;
                            int policyMapping2 = policyMapping;
                            ASN1ObjectIdentifier aSN1ObjectIdentifier = workingPublicKeyAlgorithm;
                            ASN1Encodable aSN1Encodable = workingPublicKeyParameters;
                            int maxPathLength2 = maxPathLength;
                            PublicKey workingPublicKey5 = workingPublicKey2;
                            X509Certificate sign2 = sign;
                            int index = certs.size() - 1;
                            X500Name workingIssuerName2 = workingIssuerName;
                            while (index >= 0) {
                                Iterator certIter3 = certIter;
                                if (!NoPreloadHolder.blacklist.isPublicKeyBlackListed(workingPublicKey5)) {
                                    X509Certificate cert4 = (X509Certificate) certs.get(index);
                                    int i4 = n2 - index;
                                    PKIXNameConstraintValidator nameConstraintValidator2 = nameConstraintValidator;
                                    boolean verificationAlreadyPerformed = index == certs.size() + -1;
                                    JcaJceHelper jcaJceHelper = pKIXCertPathValidatorSpi2.helper;
                                    Iterator certIter4 = certIter3;
                                    List pathCheckers3 = pathCheckers2;
                                    Set policySet = hashSet3;
                                    List[] listArr2 = listArr;
                                    PKIXExtendedParameters paramsPKIX4 = paramsPKIX3;
                                    TrustAnchor trust2 = trust;
                                    int index2 = index;
                                    RFC3280CertPathUtilities.processCertA(certPath3, paramsPKIX3, index, workingPublicKey5, verificationAlreadyPerformed, workingIssuerName2, sign2, jcaJceHelper);
                                    RFC3280CertPathUtilities.processCertBC(certPath3, index2, nameConstraintValidator2);
                                    X509Certificate sign3 = cert4;
                                    int n3 = n2;
                                    List<? extends Certificate> list5 = certs;
                                    PKIXNameConstraintValidator nameConstraintValidator3 = nameConstraintValidator2;
                                    CertPath certPath5 = certPath3;
                                    PublicKey workingPublicKey6 = workingPublicKey5;
                                    Set acceptablePolicies3 = acceptablePolicies2;
                                    PKIXPolicyNode validPolicyTree4 = RFC3280CertPathUtilities.processCertE(certPath5, index2, RFC3280CertPathUtilities.processCertD(certPath3, index2, acceptablePolicies3, validPolicyTree, listArr2, inhibitAnyPolicy2));
                                    RFC3280CertPathUtilities.processCertF(certPath5, index2, validPolicyTree4, explicitPolicy3);
                                    int i5 = i4;
                                    if (i5 != n3) {
                                        if (sign3 != null) {
                                            if (sign3.getVersion() == 1) {
                                                throw new CertPathValidatorException("Version 1 certificates can't be used as CA ones.", null, certPath5, index2);
                                            }
                                        }
                                        RFC3280CertPathUtilities.prepareNextCertA(certPath5, index2);
                                        int policyMapping3 = policyMapping2;
                                        policyNodes = listArr2;
                                        PKIXPolicyNode validPolicyTree5 = RFC3280CertPathUtilities.prepareCertB(certPath5, index2, policyNodes, validPolicyTree4, policyMapping3);
                                        RFC3280CertPathUtilities.prepareNextCertG(certPath5, index2, nameConstraintValidator3);
                                        int explicitPolicy4 = RFC3280CertPathUtilities.prepareNextCertH1(certPath5, index2, explicitPolicy3);
                                        int policyMapping4 = RFC3280CertPathUtilities.prepareNextCertH2(certPath5, index2, policyMapping3);
                                        int inhibitAnyPolicy3 = RFC3280CertPathUtilities.prepareNextCertH3(certPath5, index2, inhibitAnyPolicy2);
                                        explicitPolicy2 = RFC3280CertPathUtilities.prepareNextCertI1(certPath5, index2, explicitPolicy4);
                                        int policyMapping5 = RFC3280CertPathUtilities.prepareNextCertI2(certPath5, index2, policyMapping4);
                                        inhibitAnyPolicy2 = RFC3280CertPathUtilities.prepareNextCertJ(certPath5, index2, inhibitAnyPolicy3);
                                        RFC3280CertPathUtilities.prepareNextCertK(certPath5, index2);
                                        int maxPathLength3 = RFC3280CertPathUtilities.prepareNextCertM(certPath5, index2, RFC3280CertPathUtilities.prepareNextCertL(certPath5, index2, maxPathLength2));
                                        RFC3280CertPathUtilities.prepareNextCertN(certPath5, index2);
                                        Set criticalExtensions = sign3.getCriticalExtensionOIDs();
                                        if (criticalExtensions != null) {
                                            HashSet hashSet6 = new HashSet(criticalExtensions);
                                            hashSet6.remove(RFC3280CertPathUtilities.KEY_USAGE);
                                            hashSet6.remove(RFC3280CertPathUtilities.CERTIFICATE_POLICIES);
                                            hashSet6.remove(RFC3280CertPathUtilities.POLICY_MAPPINGS);
                                            hashSet6.remove(RFC3280CertPathUtilities.INHIBIT_ANY_POLICY);
                                            hashSet6.remove(RFC3280CertPathUtilities.ISSUING_DISTRIBUTION_POINT);
                                            hashSet6.remove(RFC3280CertPathUtilities.DELTA_CRL_INDICATOR);
                                            hashSet6.remove(RFC3280CertPathUtilities.POLICY_CONSTRAINTS);
                                            hashSet6.remove(RFC3280CertPathUtilities.BASIC_CONSTRAINTS);
                                            hashSet6.remove(RFC3280CertPathUtilities.SUBJECT_ALTERNATIVE_NAME);
                                            hashSet6.remove(RFC3280CertPathUtilities.NAME_CONSTRAINTS);
                                            hashSet2 = hashSet6;
                                        } else {
                                            hashSet2 = new HashSet();
                                            Set criticalExtensions2 = hashSet2;
                                        }
                                        n = n3;
                                        pathCheckers = pathCheckers3;
                                        RFC3280CertPathUtilities.prepareNextCertO(certPath5, index2, hashSet2, pathCheckers);
                                        X509Certificate x509Certificate = sign3;
                                        cert = sign3;
                                        workingIssuerName2 = PrincipalUtils.getSubjectPrincipal(sign3);
                                        try {
                                            X509Certificate sign4 = sign3;
                                            int i6 = i5;
                                            pKIXCertPathValidatorSpi = this;
                                            try {
                                                workingPublicKey3 = CertPathValidatorUtilities.getNextWorkingKey(certPath.getCertificates(), index2, pKIXCertPathValidatorSpi.helper);
                                                AlgorithmIdentifier workingAlgId2 = CertPathValidatorUtilities.getAlgorithmIdentifier(workingPublicKey3);
                                                ASN1ObjectIdentifier workingPublicKeyAlgorithm2 = workingAlgId2.getAlgorithm();
                                                AlgorithmIdentifier algorithmIdentifier2 = workingAlgId2;
                                                ASN1Encodable parameters = workingAlgId2.getParameters();
                                                ASN1ObjectIdentifier aSN1ObjectIdentifier2 = workingPublicKeyAlgorithm2;
                                                validPolicyTree2 = validPolicyTree5;
                                                maxPathLength2 = maxPathLength3;
                                                policyMapping2 = policyMapping5;
                                                sign2 = sign4;
                                            } catch (CertPathValidatorException e4) {
                                                e = e4;
                                                HashSet hashSet7 = hashSet2;
                                                throw new CertPathValidatorException("Next working key could not be retrieved.", e, certPath5, index2);
                                            }
                                        } catch (CertPathValidatorException e5) {
                                            e = e5;
                                            X509Certificate x509Certificate2 = sign3;
                                            int i7 = i5;
                                            HashSet hashSet72 = hashSet2;
                                            throw new CertPathValidatorException("Next working key could not be retrieved.", e, certPath5, index2);
                                        }
                                    } else {
                                        cert = sign3;
                                        n = n3;
                                        int i8 = i5;
                                        int i9 = inhibitAnyPolicy2;
                                        int i10 = policyMapping2;
                                        int i11 = maxPathLength2;
                                        pathCheckers = pathCheckers3;
                                        policyNodes = listArr2;
                                        pKIXCertPathValidatorSpi = this;
                                        explicitPolicy2 = explicitPolicy3;
                                        workingPublicKey3 = workingPublicKey6;
                                        validPolicyTree2 = validPolicyTree4;
                                    }
                                    index = index2 - 1;
                                    CertPathParameters certPathParameters2 = params;
                                    pathCheckers2 = pathCheckers;
                                    nameConstraintValidator = nameConstraintValidator3;
                                    certPath3 = certPath5;
                                    pKIXCertPathValidatorSpi2 = pKIXCertPathValidatorSpi;
                                    acceptablePolicies2 = acceptablePolicies3;
                                    certIter = certIter4;
                                    hashSet3 = policySet;
                                    paramsPKIX3 = paramsPKIX4;
                                    trust = trust2;
                                    n2 = n;
                                    cert3 = cert;
                                    workingPublicKey5 = workingPublicKey3;
                                    explicitPolicy3 = explicitPolicy2;
                                    List[] listArr3 = policyNodes;
                                    certs = list5;
                                    listArr = listArr3;
                                } else {
                                    Set policySet2 = hashSet3;
                                    PKIXExtendedParameters pKIXExtendedParameters3 = paramsPKIX3;
                                    TrustAnchor trustAnchor3 = trust;
                                    int i12 = n2;
                                    CertPath certPath6 = certPath3;
                                    int i13 = inhibitAnyPolicy2;
                                    int i14 = policyMapping2;
                                    Iterator it = certIter3;
                                    PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi6 = pKIXCertPathValidatorSpi2;
                                    List list6 = pathCheckers2;
                                    PKIXNameConstraintValidator pKIXNameConstraintValidator3 = nameConstraintValidator;
                                    int index3 = index;
                                    Set set = acceptablePolicies2;
                                    List<? extends Certificate> list7 = certs;
                                    List[] listArr4 = listArr;
                                    List<? extends Certificate> list8 = list7;
                                    String message2 = "Certificate revocation of public key " + workingPublicKey;
                                    System.out.println(message2);
                                    AnnotatedException e6 = new AnnotatedException(message2);
                                    int i15 = maxPathLength2;
                                    String str = message2;
                                    throw new CertPathValidatorException(e6.getMessage(), e6, certPath6, index3);
                                }
                            }
                            Set set2 = hashSet3;
                            PKIXExtendedParameters paramsPKIX5 = paramsPKIX3;
                            TrustAnchor trust3 = trust;
                            int i16 = n2;
                            CertPath certPath7 = certPath3;
                            int i17 = inhibitAnyPolicy2;
                            int i18 = policyMapping2;
                            int i19 = maxPathLength2;
                            PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi7 = pKIXCertPathValidatorSpi2;
                            List pathCheckers4 = pathCheckers2;
                            PKIXNameConstraintValidator pKIXNameConstraintValidator4 = nameConstraintValidator;
                            int index4 = index;
                            PublicKey publicKey2 = workingPublicKey5;
                            Set acceptablePolicies4 = acceptablePolicies2;
                            List<? extends Certificate> list9 = certs;
                            List[] policyNodes2 = listArr;
                            List<? extends Certificate> list10 = list9;
                            int explicitPolicy5 = RFC3280CertPathUtilities.wrapupCertB(certPath7, index4 + 1, RFC3280CertPathUtilities.wrapupCertA(explicitPolicy3, cert3));
                            Set criticalExtensions3 = cert3.getCriticalExtensionOIDs();
                            if (criticalExtensions3 != null) {
                                hashSet = new HashSet(criticalExtensions3);
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
                            RFC3280CertPathUtilities.wrapupCertF(certPath7, index4 + 1, pathCheckers4, hashSet);
                            PKIXPolicyNode intersection = RFC3280CertPathUtilities.wrapupCertG(certPath7, paramsPKIX5, userInitialPolicySet, index4 + 1, policyNodes2, validPolicyTree, acceptablePolicies4);
                            if (explicitPolicy5 > 0) {
                                HashSet hashSet8 = hashSet;
                            } else if (intersection != null) {
                                int i20 = explicitPolicy5;
                                HashSet hashSet9 = hashSet;
                            } else {
                                int i21 = explicitPolicy5;
                                HashSet hashSet10 = hashSet;
                                throw new CertPathValidatorException("Path processing failed on policy.", null, certPath7, index4);
                            }
                            return new PKIXCertPathValidatorResult(trust3, intersection, cert3.getPublicKey());
                        } catch (CertPathValidatorException e7) {
                            PKIXPolicyNode pKIXPolicyNode5 = validPolicyTree3;
                            Set set3 = hashSet3;
                            PKIXExtendedParameters pKIXExtendedParameters4 = paramsPKIX3;
                            PublicKey publicKey3 = workingPublicKey4;
                            int i22 = n2;
                            PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi8 = pKIXCertPathValidatorSpi2;
                            PKIXNameConstraintValidator pKIXNameConstraintValidator5 = nameConstraintValidator;
                            TrustAnchor trustAnchor4 = trust;
                            List<? extends Certificate> list11 = certs;
                            List[] listArr5 = listArr;
                            List<? extends Certificate> list12 = list11;
                            CertPathValidatorException certPathValidatorException = e7;
                            throw new ExtCertPathValidatorException("Algorithm identifier of public key of trust anchor could not be read.", e7, certPath3, -1);
                        }
                    } else {
                        int i23 = n2;
                        List<? extends Certificate> list13 = certs;
                        PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi9 = pKIXCertPathValidatorSpi2;
                        throw new CertPathValidatorException("Trust anchor for certification path not found.", null, certPath3, -1);
                    }
                } catch (AnnotatedException e8) {
                    int i24 = n2;
                    PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi10 = pKIXCertPathValidatorSpi2;
                    throw new CertPathValidatorException(e8.getMessage(), e8, certPath3, certs.size() - 1);
                }
            } else {
                List list14 = certs;
                PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi11 = pKIXCertPathValidatorSpi2;
                throw new CertPathValidatorException("Certification path is empty.", null, certPath3, -1);
            }
        } else {
            PKIXCertPathValidatorSpi pKIXCertPathValidatorSpi12 = pKIXCertPathValidatorSpi2;
            CertPath certPath8 = certPath3;
            throw new InvalidAlgorithmParameterException("trustAnchors is null, this is not allowed for certification path validation.");
        }
    }
}
