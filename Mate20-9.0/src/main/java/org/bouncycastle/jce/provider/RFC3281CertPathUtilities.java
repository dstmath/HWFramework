package org.bouncycastle.jce.provider;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.TargetInformation;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jcajce.PKIXCRLStore;
import org.bouncycastle.jcajce.PKIXCertStoreSelector;
import org.bouncycastle.jcajce.PKIXExtendedBuilderParameters;
import org.bouncycastle.jcajce.PKIXExtendedParameters;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import org.bouncycastle.x509.PKIXAttrCertChecker;
import org.bouncycastle.x509.X509AttributeCertificate;
import org.bouncycastle.x509.X509CertStoreSelector;

class RFC3281CertPathUtilities {
    private static final String AUTHORITY_INFO_ACCESS = Extension.authorityInfoAccess.getId();
    private static final String CRL_DISTRIBUTION_POINTS = Extension.cRLDistributionPoints.getId();
    private static final String NO_REV_AVAIL = Extension.noRevAvail.getId();
    private static final String TARGET_INFORMATION = Extension.targetInformation.getId();

    RFC3281CertPathUtilities() {
    }

    protected static void additionalChecks(X509AttributeCertificate x509AttributeCertificate, Set set, Set set2) throws CertPathValidatorException {
        Iterator it = set.iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            if (x509AttributeCertificate.getAttributes(str) != null) {
                throw new CertPathValidatorException("Attribute certificate contains prohibited attribute: " + str + ".");
            }
        }
        Iterator it2 = set2.iterator();
        while (it2.hasNext()) {
            String str2 = (String) it2.next();
            if (x509AttributeCertificate.getAttributes(str2) == null) {
                throw new CertPathValidatorException("Attribute certificate does not contain necessary attribute: " + str2 + ".");
            }
        }
    }

    private static void checkCRL(DistributionPoint distributionPoint, X509AttributeCertificate x509AttributeCertificate, PKIXExtendedParameters pKIXExtendedParameters, Date date, X509Certificate x509Certificate, CertStatus certStatus, ReasonsMask reasonsMask, List list, JcaJceHelper jcaJceHelper) throws AnnotatedException {
        Iterator it;
        int i;
        DistributionPoint distributionPoint2 = distributionPoint;
        X509AttributeCertificate x509AttributeCertificate2 = x509AttributeCertificate;
        PKIXExtendedParameters pKIXExtendedParameters2 = pKIXExtendedParameters;
        Date date2 = date;
        CertStatus certStatus2 = certStatus;
        ReasonsMask reasonsMask2 = reasonsMask;
        if (x509AttributeCertificate2.getExtensionValue(X509Extensions.NoRevAvail.getId()) == null) {
            Date date3 = new Date(System.currentTimeMillis());
            if (date.getTime() <= date3.getTime()) {
                Iterator it2 = CertPathValidatorUtilities.getCompleteCRLs(distributionPoint2, x509AttributeCertificate2, date3, pKIXExtendedParameters2).iterator();
                int i2 = 1;
                int i3 = 0;
                e = null;
                while (it2.hasNext() && certStatus.getCertStatus() == 11 && !reasonsMask.isAllReasons()) {
                    try {
                        X509CRL x509crl = (X509CRL) it2.next();
                        ReasonsMask processCRLD = RFC3280CertPathUtilities.processCRLD(x509crl, distributionPoint2);
                        if (!processCRLD.hasNewReasons(reasonsMask2)) {
                            continue;
                        } else {
                            ReasonsMask reasonsMask3 = processCRLD;
                            it = it2;
                            i = i2;
                            try {
                                X509CRL x509crl2 = x509crl;
                                X509CRL processCRLH = pKIXExtendedParameters.isUseDeltasEnabled() ? RFC3280CertPathUtilities.processCRLH(CertPathValidatorUtilities.getDeltaCRLs(date3, x509crl2, pKIXExtendedParameters.getCertStores(), pKIXExtendedParameters.getCRLStores()), RFC3280CertPathUtilities.processCRLG(x509crl2, RFC3280CertPathUtilities.processCRLF(x509crl, x509AttributeCertificate2, null, null, pKIXExtendedParameters2, list, jcaJceHelper))) : null;
                                if (pKIXExtendedParameters.getValidityModel() != i) {
                                    if (x509AttributeCertificate.getNotAfter().getTime() < x509crl2.getThisUpdate().getTime()) {
                                        throw new AnnotatedException("No valid CRL for current time found.");
                                    }
                                }
                                RFC3280CertPathUtilities.processCRLB1(distributionPoint2, x509AttributeCertificate2, x509crl2);
                                RFC3280CertPathUtilities.processCRLB2(distributionPoint2, x509AttributeCertificate2, x509crl2);
                                RFC3280CertPathUtilities.processCRLC(processCRLH, x509crl2, pKIXExtendedParameters2);
                                RFC3280CertPathUtilities.processCRLI(date2, processCRLH, x509AttributeCertificate2, certStatus2, pKIXExtendedParameters2);
                                RFC3280CertPathUtilities.processCRLJ(date2, x509crl2, x509AttributeCertificate2, certStatus2);
                                if (certStatus.getCertStatus() == 8) {
                                    certStatus2.setCertStatus(11);
                                }
                                reasonsMask2.addReasons(reasonsMask3);
                                i2 = i;
                                i3 = i2;
                            } catch (AnnotatedException e) {
                                e = e;
                                i2 = i;
                                it2 = it;
                            }
                            it2 = it;
                        }
                    } catch (AnnotatedException e2) {
                        e = e2;
                        it = it2;
                        i = i2;
                        i2 = i;
                        it2 = it;
                    }
                }
                if (i3 == 0) {
                    throw e;
                }
                return;
            }
            throw new AnnotatedException("Validation time is in future.");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x0116  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0173  */
    protected static void checkCRLs(X509AttributeCertificate x509AttributeCertificate, PKIXExtendedParameters pKIXExtendedParameters, X509Certificate x509Certificate, Date date, List list, JcaJceHelper jcaJceHelper) throws CertPathValidatorException {
        boolean z;
        int i;
        int i2;
        AnnotatedException annotatedException;
        int i3;
        int i4;
        X509AttributeCertificate x509AttributeCertificate2 = x509AttributeCertificate;
        if (!pKIXExtendedParameters.isRevocationEnabled()) {
            return;
        }
        if (x509AttributeCertificate2.getExtensionValue(NO_REV_AVAIL) == null) {
            try {
                CRLDistPoint instance = CRLDistPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(x509AttributeCertificate2, CRL_DISTRIBUTION_POINTS));
                ArrayList arrayList = new ArrayList();
                try {
                    arrayList.addAll(CertPathValidatorUtilities.getAdditionalStoresFromCRLDistributionPoint(instance, pKIXExtendedParameters.getNamedCRLStoreMap()));
                    PKIXExtendedParameters.Builder builder = new PKIXExtendedParameters.Builder(pKIXExtendedParameters);
                    Iterator it = arrayList.iterator();
                    while (it.hasNext()) {
                        builder.addCRLStore((PKIXCRLStore) arrayList);
                    }
                    PKIXExtendedParameters build = builder.build();
                    CertStatus certStatus = new CertStatus();
                    ReasonsMask reasonsMask = new ReasonsMask();
                    int i5 = 11;
                    int i6 = 0;
                    if (instance != null) {
                        try {
                            DistributionPoint[] distributionPoints = instance.getDistributionPoints();
                            int i7 = 0;
                            z = false;
                            while (i7 < distributionPoints.length && certStatus.getCertStatus() == i5 && !reasonsMask.isAllReasons()) {
                                try {
                                    int i8 = i7;
                                    i2 = i6;
                                    i = i5;
                                    try {
                                        checkCRL(distributionPoints[i7], x509AttributeCertificate2, (PKIXExtendedParameters) build.clone(), date, x509Certificate, certStatus, reasonsMask, list, jcaJceHelper);
                                        i7 = i8 + 1;
                                        i6 = i2;
                                        i5 = i;
                                        z = true;
                                    } catch (AnnotatedException e) {
                                        e = e;
                                        annotatedException = new AnnotatedException("No valid CRL for distribution point found.", e);
                                        try {
                                            checkCRL(new DistributionPoint(new DistributionPointName(i2, new GeneralNames(new GeneralName(4, (ASN1Encodable) new ASN1InputStream(((X500Principal) x509AttributeCertificate.getIssuer().getPrincipals()[i2]).getEncoded()).readObject()))), null, null), x509AttributeCertificate2, (PKIXExtendedParameters) build.clone(), date, x509Certificate, certStatus, reasonsMask, list, jcaJceHelper);
                                            z = true;
                                        } catch (Exception e2) {
                                            throw new AnnotatedException("Issuer from certificate for CRL could not be reencoded.", e2);
                                        } catch (AnnotatedException e3) {
                                            annotatedException = new AnnotatedException("No valid CRL for distribution point found.", e3);
                                        }
                                        if (!z) {
                                        }
                                    }
                                } catch (AnnotatedException e4) {
                                    e = e4;
                                    i2 = i6;
                                    i = i5;
                                    annotatedException = new AnnotatedException("No valid CRL for distribution point found.", e);
                                    checkCRL(new DistributionPoint(new DistributionPointName(i2, new GeneralNames(new GeneralName(4, (ASN1Encodable) new ASN1InputStream(((X500Principal) x509AttributeCertificate.getIssuer().getPrincipals()[i2]).getEncoded()).readObject()))), null, null), x509AttributeCertificate2, (PKIXExtendedParameters) build.clone(), date, x509Certificate, certStatus, reasonsMask, list, jcaJceHelper);
                                    z = true;
                                    if (!z) {
                                    }
                                }
                            }
                            i3 = i6;
                            i4 = i5;
                        } catch (Exception e5) {
                            throw new ExtCertPathValidatorException("Distribution points could not be read.", e5);
                        }
                    } else {
                        i3 = 0;
                        i4 = 11;
                        z = false;
                    }
                    annotatedException = null;
                    if (certStatus.getCertStatus() == i && !reasonsMask.isAllReasons()) {
                        checkCRL(new DistributionPoint(new DistributionPointName(i2, new GeneralNames(new GeneralName(4, (ASN1Encodable) new ASN1InputStream(((X500Principal) x509AttributeCertificate.getIssuer().getPrincipals()[i2]).getEncoded()).readObject()))), null, null), x509AttributeCertificate2, (PKIXExtendedParameters) build.clone(), date, x509Certificate, certStatus, reasonsMask, list, jcaJceHelper);
                        z = true;
                    }
                    if (!z) {
                        throw new ExtCertPathValidatorException("No valid CRL found.", annotatedException);
                    } else if (certStatus.getCertStatus() == i) {
                        if (!reasonsMask.isAllReasons() && certStatus.getCertStatus() == i) {
                            certStatus.setCertStatus(12);
                        }
                        if (certStatus.getCertStatus() == 12) {
                            throw new CertPathValidatorException("Attribute certificate status could not be determined.");
                        }
                    } else {
                        String str = "Attribute certificate revocation after " + certStatus.getRevocationDate();
                        throw new CertPathValidatorException(str + ", reason: " + RFC3280CertPathUtilities.crlReasons[certStatus.getCertStatus()]);
                    }
                } catch (AnnotatedException e6) {
                    throw new CertPathValidatorException("No additional CRL locations could be decoded from CRL distribution point extension.", e6);
                }
            } catch (AnnotatedException e7) {
                throw new CertPathValidatorException("CRL distribution point extension could not be read.", e7);
            }
        } else if (x509AttributeCertificate2.getExtensionValue(CRL_DISTRIBUTION_POINTS) != null || x509AttributeCertificate2.getExtensionValue(AUTHORITY_INFO_ACCESS) != null) {
            throw new CertPathValidatorException("No rev avail extension is set, but also an AC revocation pointer.");
        }
    }

    protected static CertPath processAttrCert1(X509AttributeCertificate x509AttributeCertificate, PKIXExtendedParameters pKIXExtendedParameters) throws CertPathValidatorException {
        HashSet<X509Certificate> hashSet = new HashSet<>();
        int i = 0;
        if (x509AttributeCertificate.getHolder().getIssuer() != null) {
            X509CertSelector x509CertSelector = new X509CertSelector();
            x509CertSelector.setSerialNumber(x509AttributeCertificate.getHolder().getSerialNumber());
            Principal[] issuer = x509AttributeCertificate.getHolder().getIssuer();
            int i2 = 0;
            while (i2 < issuer.length) {
                try {
                    if (issuer[i2] instanceof X500Principal) {
                        x509CertSelector.setIssuer(((X500Principal) issuer[i2]).getEncoded());
                    }
                    hashSet.addAll(CertPathValidatorUtilities.findCertificates(new PKIXCertStoreSelector.Builder(x509CertSelector).build(), pKIXExtendedParameters.getCertStores()));
                    i2++;
                } catch (AnnotatedException e) {
                    throw new ExtCertPathValidatorException("Public key certificate for attribute certificate cannot be searched.", e);
                } catch (IOException e2) {
                    throw new ExtCertPathValidatorException("Unable to encode X500 principal.", e2);
                }
            }
            if (hashSet.isEmpty()) {
                throw new CertPathValidatorException("Public key certificate specified in base certificate ID for attribute certificate cannot be found.");
            }
        }
        if (x509AttributeCertificate.getHolder().getEntityNames() != null) {
            X509CertStoreSelector x509CertStoreSelector = new X509CertStoreSelector();
            Principal[] entityNames = x509AttributeCertificate.getHolder().getEntityNames();
            while (i < entityNames.length) {
                try {
                    if (entityNames[i] instanceof X500Principal) {
                        x509CertStoreSelector.setIssuer(((X500Principal) entityNames[i]).getEncoded());
                    }
                    hashSet.addAll(CertPathValidatorUtilities.findCertificates(new PKIXCertStoreSelector.Builder(x509CertStoreSelector).build(), pKIXExtendedParameters.getCertStores()));
                    i++;
                } catch (AnnotatedException e3) {
                    throw new ExtCertPathValidatorException("Public key certificate for attribute certificate cannot be searched.", e3);
                } catch (IOException e4) {
                    throw new ExtCertPathValidatorException("Unable to encode X500 principal.", e4);
                }
            }
            if (hashSet.isEmpty()) {
                throw new CertPathValidatorException("Public key certificate specified in entity name for attribute certificate cannot be found.");
            }
        }
        PKIXExtendedParameters.Builder builder = new PKIXExtendedParameters.Builder(pKIXExtendedParameters);
        ExtCertPathValidatorException extCertPathValidatorException = null;
        CertPathBuilderResult certPathBuilderResult = null;
        for (X509Certificate certificate : hashSet) {
            X509CertStoreSelector x509CertStoreSelector2 = new X509CertStoreSelector();
            x509CertStoreSelector2.setCertificate(certificate);
            builder.setTargetConstraints(new PKIXCertStoreSelector.Builder(x509CertStoreSelector2).build());
            try {
                try {
                    certPathBuilderResult = CertPathBuilder.getInstance("PKIX", "BC").build(new PKIXExtendedBuilderParameters.Builder(builder.build()).build());
                } catch (CertPathBuilderException e5) {
                    extCertPathValidatorException = new ExtCertPathValidatorException("Certification path for public key certificate of attribute certificate could not be build.", e5);
                } catch (InvalidAlgorithmParameterException e6) {
                    throw new RuntimeException(e6.getMessage());
                }
            } catch (NoSuchProviderException e7) {
                throw new ExtCertPathValidatorException("Support class could not be created.", e7);
            } catch (NoSuchAlgorithmException e8) {
                throw new ExtCertPathValidatorException("Support class could not be created.", e8);
            }
        }
        if (extCertPathValidatorException == null) {
            return certPathBuilderResult.getCertPath();
        }
        throw extCertPathValidatorException;
    }

    protected static CertPathValidatorResult processAttrCert2(CertPath certPath, PKIXExtendedParameters pKIXExtendedParameters) throws CertPathValidatorException {
        try {
            try {
                return CertPathValidator.getInstance("PKIX", "BC").validate(certPath, pKIXExtendedParameters);
            } catch (CertPathValidatorException e) {
                throw new ExtCertPathValidatorException("Certification path for issuer certificate of attribute certificate could not be validated.", e);
            } catch (InvalidAlgorithmParameterException e2) {
                throw new RuntimeException(e2.getMessage());
            }
        } catch (NoSuchProviderException e3) {
            throw new ExtCertPathValidatorException("Support class could not be created.", e3);
        } catch (NoSuchAlgorithmException e4) {
            throw new ExtCertPathValidatorException("Support class could not be created.", e4);
        }
    }

    protected static void processAttrCert3(X509Certificate x509Certificate, PKIXExtendedParameters pKIXExtendedParameters) throws CertPathValidatorException {
        if (x509Certificate.getKeyUsage() != null && !x509Certificate.getKeyUsage()[0] && !x509Certificate.getKeyUsage()[1]) {
            throw new CertPathValidatorException("Attribute certificate issuer public key cannot be used to validate digital signatures.");
        } else if (x509Certificate.getBasicConstraints() != -1) {
            throw new CertPathValidatorException("Attribute certificate issuer is also a public key certificate issuer.");
        }
    }

    protected static void processAttrCert4(X509Certificate x509Certificate, Set set) throws CertPathValidatorException {
        Iterator it = set.iterator();
        boolean z = false;
        while (it.hasNext()) {
            TrustAnchor trustAnchor = (TrustAnchor) it.next();
            if (x509Certificate.getSubjectX500Principal().getName("RFC2253").equals(trustAnchor.getCAName()) || x509Certificate.equals(trustAnchor.getTrustedCert())) {
                z = true;
            }
        }
        if (!z) {
            throw new CertPathValidatorException("Attribute certificate issuer is not directly trusted.");
        }
    }

    protected static void processAttrCert5(X509AttributeCertificate x509AttributeCertificate, PKIXExtendedParameters pKIXExtendedParameters) throws CertPathValidatorException {
        try {
            x509AttributeCertificate.checkValidity(CertPathValidatorUtilities.getValidDate(pKIXExtendedParameters));
        } catch (CertificateExpiredException e) {
            throw new ExtCertPathValidatorException("Attribute certificate is not valid.", e);
        } catch (CertificateNotYetValidException e2) {
            throw new ExtCertPathValidatorException("Attribute certificate is not valid.", e2);
        }
    }

    protected static void processAttrCert7(X509AttributeCertificate x509AttributeCertificate, CertPath certPath, CertPath certPath2, PKIXExtendedParameters pKIXExtendedParameters, Set set) throws CertPathValidatorException {
        Set criticalExtensionOIDs = x509AttributeCertificate.getCriticalExtensionOIDs();
        if (criticalExtensionOIDs.contains(TARGET_INFORMATION)) {
            try {
                TargetInformation.getInstance(CertPathValidatorUtilities.getExtensionValue(x509AttributeCertificate, TARGET_INFORMATION));
            } catch (AnnotatedException e) {
                throw new ExtCertPathValidatorException("Target information extension could not be read.", e);
            } catch (IllegalArgumentException e2) {
                throw new ExtCertPathValidatorException("Target information extension could not be read.", e2);
            }
        }
        criticalExtensionOIDs.remove(TARGET_INFORMATION);
        Iterator it = set.iterator();
        while (it.hasNext()) {
            ((PKIXAttrCertChecker) it.next()).check(x509AttributeCertificate, certPath, certPath2, criticalExtensionOIDs);
        }
        if (!criticalExtensionOIDs.isEmpty()) {
            throw new CertPathValidatorException("Attribute certificate contains unsupported critical extensions: " + criticalExtensionOIDs);
        }
    }
}
