package org.bouncycastle.pkix.jcajce;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CRL;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.cert.X509Extension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.IssuingDistributionPoint;
import org.bouncycastle.jcajce.PKIXCRLStoreSelector;
import org.bouncycastle.jcajce.PKIXCertStoreSelector;
import org.bouncycastle.jcajce.PKIXExtendedBuilderParameters;
import org.bouncycastle.jcajce.PKIXExtendedParameters;
import org.bouncycastle.jcajce.util.JcaJceHelper;
import org.bouncycastle.util.Arrays;

/* access modifiers changed from: package-private */
public class RFC3280CertPathUtilities {
    public static final String AUTHORITY_KEY_IDENTIFIER = Extension.authorityKeyIdentifier.getId();
    public static final String BASIC_CONSTRAINTS = Extension.basicConstraints.getId();
    protected static final int CRL_SIGN = 6;
    private static final PKIXCRLUtil CRL_UTIL = new PKIXCRLUtil();
    public static final String DELTA_CRL_INDICATOR = Extension.deltaCRLIndicator.getId();
    public static final String FRESHEST_CRL = Extension.freshestCRL.getId();
    public static final String ISSUING_DISTRIBUTION_POINT = Extension.issuingDistributionPoint.getId();
    protected static final int KEY_CERT_SIGN = 5;

    RFC3280CertPathUtilities() {
    }

    static void checkCRL(DistributionPoint distributionPoint, PKIXExtendedParameters pKIXExtendedParameters, X509Certificate x509Certificate, Date date, X509Certificate x509Certificate2, PublicKey publicKey, CertStatus certStatus, ReasonsMask reasonsMask, List list, JcaJceHelper jcaJceHelper) throws AnnotatedException, CRLNotFoundException {
        ReasonsMask reasonsMask2;
        Iterator it;
        int i;
        X509CRL processCRLH;
        Set<String> criticalExtensionOIDs;
        ReasonsMask reasonsMask3 = reasonsMask;
        Date date2 = new Date(System.currentTimeMillis());
        if (date.getTime() <= date2.getTime()) {
            if (pKIXExtendedParameters.getDate() != null) {
                date2 = pKIXExtendedParameters.getDate();
            }
            Iterator it2 = RevocationUtilities.getCompleteCRLs(distributionPoint, x509Certificate, date2, pKIXExtendedParameters.getCertStores(), pKIXExtendedParameters.getCRLStores()).iterator();
            int i2 = 1;
            int i3 = 0;
            AnnotatedException e = null;
            while (it2.hasNext() && certStatus.getCertStatus() == 11 && !reasonsMask.isAllReasons()) {
                try {
                    X509CRL x509crl = (X509CRL) it2.next();
                    ReasonsMask processCRLD = processCRLD(x509crl, distributionPoint);
                    if (!processCRLD.hasNewReasons(reasonsMask3)) {
                        continue;
                    } else {
                        it = it2;
                        i = i2;
                        try {
                            processCRLH = pKIXExtendedParameters.isUseDeltasEnabled() ? processCRLH(RevocationUtilities.getDeltaCRLs(date2, x509crl, pKIXExtendedParameters.getCertStores(), pKIXExtendedParameters.getCRLStores()), processCRLG(x509crl, processCRLF(x509crl, x509Certificate, x509Certificate2, publicKey, pKIXExtendedParameters, list, jcaJceHelper))) : null;
                            if (pKIXExtendedParameters.getValidityModel() != i) {
                                if (x509Certificate.getNotAfter().getTime() < x509crl.getThisUpdate().getTime()) {
                                    throw new AnnotatedException("No valid CRL for current time found.");
                                }
                            }
                            processCRLB1(distributionPoint, x509Certificate, x509crl);
                            processCRLB2(distributionPoint, x509Certificate, x509crl);
                            processCRLC(processCRLH, x509crl, pKIXExtendedParameters);
                            processCRLI(date, processCRLH, x509Certificate, certStatus, pKIXExtendedParameters);
                            processCRLJ(date, x509crl, x509Certificate, certStatus);
                            if (certStatus.getCertStatus() == 8) {
                                certStatus.setCertStatus(11);
                            }
                            reasonsMask2 = reasonsMask;
                        } catch (AnnotatedException e2) {
                            e = e2;
                            reasonsMask2 = reasonsMask;
                            i2 = i;
                            it2 = it;
                            reasonsMask3 = reasonsMask2;
                        }
                        try {
                            reasonsMask2.addReasons(processCRLD);
                            Set<String> criticalExtensionOIDs2 = x509crl.getCriticalExtensionOIDs();
                            if (criticalExtensionOIDs2 != null) {
                                HashSet hashSet = new HashSet(criticalExtensionOIDs2);
                                hashSet.remove(Extension.issuingDistributionPoint.getId());
                                hashSet.remove(Extension.deltaCRLIndicator.getId());
                                if (!hashSet.isEmpty()) {
                                    throw new AnnotatedException("CRL contains unsupported critical extensions.");
                                }
                            }
                            if (!(processCRLH == null || (criticalExtensionOIDs = processCRLH.getCriticalExtensionOIDs()) == null)) {
                                HashSet hashSet2 = new HashSet(criticalExtensionOIDs);
                                hashSet2.remove(Extension.issuingDistributionPoint.getId());
                                hashSet2.remove(Extension.deltaCRLIndicator.getId());
                                if (!hashSet2.isEmpty()) {
                                    throw new AnnotatedException("Delta CRL contains unsupported critical extension.");
                                }
                            }
                            i2 = i;
                            i3 = i2;
                            it2 = it;
                            e = e;
                        } catch (AnnotatedException e3) {
                            e = e3;
                            i2 = i;
                            it2 = it;
                            reasonsMask3 = reasonsMask2;
                        }
                        reasonsMask3 = reasonsMask2;
                    }
                } catch (AnnotatedException e4) {
                    e = e4;
                    reasonsMask2 = reasonsMask3;
                    it = it2;
                    i = i2;
                    i2 = i;
                    it2 = it;
                    reasonsMask3 = reasonsMask2;
                }
            }
            if (i3 == 0) {
                throw e;
            }
            return;
        }
        throw new AnnotatedException("Validation time is in future.");
    }

    protected static Set processCRLA1i(Date date, PKIXExtendedParameters pKIXExtendedParameters, X509Certificate x509Certificate, X509CRL x509crl) throws AnnotatedException {
        HashSet hashSet = new HashSet();
        if (pKIXExtendedParameters.isUseDeltasEnabled()) {
            try {
                CRLDistPoint instance = CRLDistPoint.getInstance(RevocationUtilities.getExtensionValue(x509Certificate, Extension.freshestCRL));
                if (instance == null) {
                    try {
                        instance = CRLDistPoint.getInstance(RevocationUtilities.getExtensionValue(x509crl, Extension.freshestCRL));
                    } catch (AnnotatedException e) {
                        throw new AnnotatedException("Freshest CRL extension could not be decoded from CRL.", e);
                    }
                }
                if (instance != null) {
                    ArrayList arrayList = new ArrayList();
                    arrayList.addAll(pKIXExtendedParameters.getCRLStores());
                    try {
                        arrayList.addAll(RevocationUtilities.getAdditionalStoresFromCRLDistributionPoint(instance, pKIXExtendedParameters.getNamedCRLStoreMap()));
                        try {
                            hashSet.addAll(RevocationUtilities.getDeltaCRLs(date, x509crl, pKIXExtendedParameters.getCertStores(), arrayList));
                        } catch (AnnotatedException e2) {
                            throw new AnnotatedException("Exception obtaining delta CRLs.", e2);
                        }
                    } catch (AnnotatedException e3) {
                        throw new AnnotatedException("No new delta CRL locations could be added from Freshest CRL extension.", e3);
                    }
                }
            } catch (AnnotatedException e4) {
                throw new AnnotatedException("Freshest CRL extension could not be decoded from certificate.", e4);
            }
        }
        return hashSet;
    }

    protected static Set[] processCRLA1ii(Date date, PKIXExtendedParameters pKIXExtendedParameters, X509Certificate x509Certificate, X509CRL x509crl) throws AnnotatedException {
        HashSet hashSet = new HashSet();
        X509CRLSelector x509CRLSelector = new X509CRLSelector();
        x509CRLSelector.setCertificateChecking(x509Certificate);
        try {
            x509CRLSelector.addIssuerName(x509crl.getIssuerX500Principal().getEncoded());
            PKIXCRLStoreSelector<? extends CRL> build = new PKIXCRLStoreSelector.Builder(x509CRLSelector).setCompleteCRLEnabled(true).build();
            if (pKIXExtendedParameters.getDate() != null) {
                date = pKIXExtendedParameters.getDate();
            }
            Set findCRLs = CRL_UTIL.findCRLs(build, date, pKIXExtendedParameters.getCertStores(), pKIXExtendedParameters.getCRLStores());
            if (pKIXExtendedParameters.isUseDeltasEnabled()) {
                try {
                    hashSet.addAll(RevocationUtilities.getDeltaCRLs(date, x509crl, pKIXExtendedParameters.getCertStores(), pKIXExtendedParameters.getCRLStores()));
                } catch (AnnotatedException e) {
                    throw new AnnotatedException("Exception obtaining delta CRLs.", e);
                }
            }
            return new Set[]{findCRLs, hashSet};
        } catch (IOException e2) {
            throw new AnnotatedException("Cannot extract issuer from CRL." + e2, e2);
        }
    }

    protected static void processCRLB1(DistributionPoint distributionPoint, Object obj, X509CRL x509crl) throws AnnotatedException {
        boolean z;
        ASN1Primitive extensionValue = RevocationUtilities.getExtensionValue(x509crl, Extension.issuingDistributionPoint);
        boolean z2 = extensionValue != null && IssuingDistributionPoint.getInstance(extensionValue).isIndirectCRL();
        byte[] encoded = x509crl.getIssuerX500Principal().getEncoded();
        if (distributionPoint.getCRLIssuer() != null) {
            GeneralName[] names = distributionPoint.getCRLIssuer().getNames();
            z = false;
            for (int i = 0; i < names.length; i++) {
                if (names[i].getTagNo() == 4) {
                    try {
                        if (Arrays.areEqual(names[i].getName().toASN1Primitive().getEncoded(), encoded)) {
                            z = true;
                        }
                    } catch (IOException e) {
                        throw new AnnotatedException("CRL issuer information from distribution point cannot be decoded.", e);
                    }
                }
            }
            if (z && !z2) {
                throw new AnnotatedException("Distribution point contains cRLIssuer field but CRL is not indirect.");
            } else if (!z) {
                throw new AnnotatedException("CRL issuer of CRL does not match CRL issuer of distribution point.");
            }
        } else {
            z = x509crl.getIssuerX500Principal().equals(((X509Certificate) obj).getIssuerX500Principal());
        }
        if (!z) {
            throw new AnnotatedException("Cannot find matching CRL issuer for certificate.");
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00d7: APUT  
      (r9v20 org.bouncycastle.asn1.x509.GeneralName[])
      (0 ??[int, short, byte, char])
      (wrap: org.bouncycastle.asn1.x509.GeneralName : 0x00d4: CONSTRUCTOR  (r3v9 org.bouncycastle.asn1.x509.GeneralName) = 
      (wrap: org.bouncycastle.asn1.x500.X500Name : 0x00d0: INVOKE  (r6v6 org.bouncycastle.asn1.x500.X500Name) = 
      (wrap: byte[] : 0x00cc: INVOKE  (r6v5 byte[]) = 
      (wrap: javax.security.auth.x500.X500Principal : 0x00c8: INVOKE  (r6v4 javax.security.auth.x500.X500Principal) = 
      (wrap: java.security.cert.X509Certificate : 0x00c6: CHECK_CAST (r6v3 java.security.cert.X509Certificate) = (java.security.cert.X509Certificate) (r10v0 java.lang.Object))
     type: VIRTUAL call: java.security.cert.X509Certificate.getIssuerX500Principal():javax.security.auth.x500.X500Principal)
     type: VIRTUAL call: javax.security.auth.x500.X500Principal.getEncoded():byte[])
     type: STATIC call: org.bouncycastle.asn1.x500.X500Name.getInstance(java.lang.Object):org.bouncycastle.asn1.x500.X500Name)
     call: org.bouncycastle.asn1.x509.GeneralName.<init>(org.bouncycastle.asn1.x500.X500Name):void type: CONSTRUCTOR)
     */
    protected static void processCRLB2(DistributionPoint distributionPoint, Object obj, X509CRL x509crl) throws AnnotatedException {
        GeneralName[] generalNameArr;
        GeneralName[] names;
        try {
            IssuingDistributionPoint instance = IssuingDistributionPoint.getInstance(RevocationUtilities.getExtensionValue(x509crl, Extension.issuingDistributionPoint));
            if (instance != null) {
                if (instance.getDistributionPoint() != null) {
                    DistributionPointName distributionPoint2 = IssuingDistributionPoint.getInstance(instance).getDistributionPoint();
                    ArrayList arrayList = new ArrayList();
                    boolean z = false;
                    if (distributionPoint2.getType() == 0) {
                        for (GeneralName generalName : GeneralNames.getInstance(distributionPoint2.getName()).getNames()) {
                            arrayList.add(generalName);
                        }
                    }
                    if (distributionPoint2.getType() == 1) {
                        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
                        try {
                            Enumeration objects = ASN1Sequence.getInstance(x509crl.getIssuerX500Principal().getEncoded()).getObjects();
                            while (objects.hasMoreElements()) {
                                aSN1EncodableVector.add((ASN1Encodable) objects.nextElement());
                            }
                            aSN1EncodableVector.add(distributionPoint2.getName());
                            arrayList.add(new GeneralName(X500Name.getInstance(new DERSequence(aSN1EncodableVector))));
                        } catch (Exception e) {
                            throw new AnnotatedException("Could not read CRL issuer.", e);
                        }
                    }
                    if (distributionPoint.getDistributionPoint() != null) {
                        DistributionPointName distributionPoint3 = distributionPoint.getDistributionPoint();
                        GeneralName[] generalNameArr2 = null;
                        if (distributionPoint3.getType() == 0) {
                            generalNameArr2 = GeneralNames.getInstance(distributionPoint3.getName()).getNames();
                        }
                        if (distributionPoint3.getType() == 1) {
                            if (distributionPoint.getCRLIssuer() != null) {
                                generalNameArr = distributionPoint.getCRLIssuer().getNames();
                            } else {
                                generalNameArr = new GeneralName[1];
                                try {
                                    generalNameArr[0] = new GeneralName(X500Name.getInstance(((X509Certificate) obj).getIssuerX500Principal().getEncoded()));
                                } catch (Exception e2) {
                                    throw new AnnotatedException("Could not read certificate issuer.", e2);
                                }
                            }
                            generalNameArr2 = generalNameArr;
                            for (int i = 0; i < generalNameArr2.length; i++) {
                                Enumeration objects2 = ASN1Sequence.getInstance(generalNameArr2[i].getName().toASN1Primitive()).getObjects();
                                ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
                                while (objects2.hasMoreElements()) {
                                    aSN1EncodableVector2.add((ASN1Encodable) objects2.nextElement());
                                }
                                aSN1EncodableVector2.add(distributionPoint3.getName());
                                generalNameArr2[i] = new GeneralName(X500Name.getInstance(new DERSequence(aSN1EncodableVector2)));
                            }
                        }
                        if (generalNameArr2 != null) {
                            int i2 = 0;
                            while (true) {
                                if (i2 >= generalNameArr2.length) {
                                    break;
                                } else if (arrayList.contains(generalNameArr2[i2])) {
                                    z = true;
                                    break;
                                } else {
                                    i2++;
                                }
                            }
                        }
                        if (!z) {
                            throw new AnnotatedException("No match for certificate CRL issuing distribution point name to cRLIssuer CRL distribution point.");
                        }
                    } else if (distributionPoint.getCRLIssuer() != null) {
                        GeneralName[] names2 = distributionPoint.getCRLIssuer().getNames();
                        int i3 = 0;
                        while (true) {
                            if (i3 >= names2.length) {
                                break;
                            } else if (arrayList.contains(names2[i3])) {
                                z = true;
                                break;
                            } else {
                                i3++;
                            }
                        }
                        if (!z) {
                            throw new AnnotatedException("No match for certificate CRL issuing distribution point name to cRLIssuer CRL distribution point.");
                        }
                    } else {
                        throw new AnnotatedException("Either the cRLIssuer or the distributionPoint field must be contained in DistributionPoint.");
                    }
                }
                try {
                    BasicConstraints instance2 = BasicConstraints.getInstance(RevocationUtilities.getExtensionValue((X509Extension) obj, Extension.basicConstraints));
                    if (obj instanceof X509Certificate) {
                        if (instance.onlyContainsUserCerts() && instance2 != null && instance2.isCA()) {
                            throw new AnnotatedException("CA Cert CRL only contains user certificates.");
                        } else if (instance.onlyContainsCACerts() && (instance2 == null || !instance2.isCA())) {
                            throw new AnnotatedException("End CRL only contains CA certificates.");
                        }
                    }
                    if (instance.onlyContainsAttributeCerts()) {
                        throw new AnnotatedException("onlyContainsAttributeCerts boolean is asserted.");
                    }
                } catch (Exception e3) {
                    throw new AnnotatedException("Basic constraints extension could not be decoded.", e3);
                }
            }
        } catch (Exception e4) {
            throw new AnnotatedException("Issuing distribution point extension could not be decoded.", e4);
        }
    }

    protected static void processCRLC(X509CRL x509crl, X509CRL x509crl2, PKIXExtendedParameters pKIXExtendedParameters) throws AnnotatedException {
        if (x509crl != null) {
            try {
                IssuingDistributionPoint instance = IssuingDistributionPoint.getInstance(RevocationUtilities.getExtensionValue(x509crl2, Extension.issuingDistributionPoint));
                if (!pKIXExtendedParameters.isUseDeltasEnabled()) {
                    return;
                }
                if (x509crl.getIssuerX500Principal().equals(x509crl2.getIssuerX500Principal())) {
                    try {
                        IssuingDistributionPoint instance2 = IssuingDistributionPoint.getInstance(RevocationUtilities.getExtensionValue(x509crl, Extension.issuingDistributionPoint));
                        boolean z = true;
                        if (instance != null ? !instance.equals(instance2) : instance2 != null) {
                            z = false;
                        }
                        if (z) {
                            try {
                                ASN1Primitive extensionValue = RevocationUtilities.getExtensionValue(x509crl2, Extension.authorityKeyIdentifier);
                                try {
                                    ASN1Primitive extensionValue2 = RevocationUtilities.getExtensionValue(x509crl, Extension.authorityKeyIdentifier);
                                    if (extensionValue == null) {
                                        throw new AnnotatedException("CRL authority key identifier is null.");
                                    } else if (extensionValue2 == null) {
                                        throw new AnnotatedException("Delta CRL authority key identifier is null.");
                                    } else if (!extensionValue.equals(extensionValue2)) {
                                        throw new AnnotatedException("Delta CRL authority key identifier does not match complete CRL authority key identifier.");
                                    }
                                } catch (AnnotatedException e) {
                                    throw new AnnotatedException("Authority key identifier extension could not be extracted from delta CRL.", e);
                                }
                            } catch (AnnotatedException e2) {
                                throw new AnnotatedException("Authority key identifier extension could not be extracted from complete CRL.", e2);
                            }
                        } else {
                            throw new AnnotatedException("Issuing distribution point extension from delta CRL and complete CRL does not match.");
                        }
                    } catch (Exception e3) {
                        throw new AnnotatedException("Issuing distribution point extension from delta CRL could not be decoded.", e3);
                    }
                } else {
                    throw new AnnotatedException("complete CRL issuer does not match delta CRL issuer");
                }
            } catch (Exception e4) {
                throw new AnnotatedException("issuing distribution point extension could not be decoded.", e4);
            }
        }
    }

    protected static ReasonsMask processCRLD(X509CRL x509crl, DistributionPoint distributionPoint) throws AnnotatedException {
        try {
            IssuingDistributionPoint instance = IssuingDistributionPoint.getInstance(RevocationUtilities.getExtensionValue(x509crl, Extension.issuingDistributionPoint));
            if (instance != null && instance.getOnlySomeReasons() != null && distributionPoint.getReasons() != null) {
                return new ReasonsMask(distributionPoint.getReasons()).intersect(new ReasonsMask(instance.getOnlySomeReasons()));
            }
            if ((instance == null || instance.getOnlySomeReasons() == null) && distributionPoint.getReasons() == null) {
                return ReasonsMask.allReasons;
            }
            return (distributionPoint.getReasons() == null ? ReasonsMask.allReasons : new ReasonsMask(distributionPoint.getReasons())).intersect(instance == null ? ReasonsMask.allReasons : new ReasonsMask(instance.getOnlySomeReasons()));
        } catch (Exception e) {
            throw new AnnotatedException("Issuing distribution point extension could not be decoded.", e);
        }
    }

    protected static Set processCRLF(X509CRL x509crl, Object obj, X509Certificate x509Certificate, PublicKey publicKey, PKIXExtendedParameters pKIXExtendedParameters, List list, JcaJceHelper jcaJceHelper) throws AnnotatedException {
        int i;
        X509CertSelector x509CertSelector = new X509CertSelector();
        try {
            x509CertSelector.setSubject(x509crl.getIssuerX500Principal().getEncoded());
            PKIXCertStoreSelector<? extends Certificate> build = new PKIXCertStoreSelector.Builder(x509CertSelector).build();
            try {
                Collection findCertificates = RevocationUtilities.findCertificates(build, pKIXExtendedParameters.getCertificateStores());
                findCertificates.addAll(RevocationUtilities.findCertificates(build, pKIXExtendedParameters.getCertStores()));
                findCertificates.add(x509Certificate);
                Iterator it = findCertificates.iterator();
                ArrayList arrayList = new ArrayList();
                ArrayList arrayList2 = new ArrayList();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    X509Certificate x509Certificate2 = (X509Certificate) it.next();
                    if (x509Certificate2.equals(x509Certificate)) {
                        arrayList.add(x509Certificate2);
                        arrayList2.add(publicKey);
                    } else {
                        try {
                            CertPathBuilder createCertPathBuilder = jcaJceHelper.createCertPathBuilder("PKIX");
                            X509CertSelector x509CertSelector2 = new X509CertSelector();
                            x509CertSelector2.setCertificate(x509Certificate2);
                            PKIXExtendedParameters.Builder targetConstraints = new PKIXExtendedParameters.Builder(pKIXExtendedParameters).setTargetConstraints(new PKIXCertStoreSelector.Builder(x509CertSelector2).build());
                            if (list.contains(x509Certificate2)) {
                                targetConstraints.setRevocationEnabled(false);
                            } else {
                                targetConstraints.setRevocationEnabled(true);
                            }
                            List<? extends Certificate> certificates = createCertPathBuilder.build(new PKIXExtendedBuilderParameters.Builder(targetConstraints.build()).build()).getCertPath().getCertificates();
                            arrayList.add(x509Certificate2);
                            arrayList2.add(RevocationUtilities.getNextWorkingKey(certificates, 0, jcaJceHelper));
                        } catch (CertPathBuilderException e) {
                            throw new AnnotatedException("CertPath for CRL signer failed to validate.", e);
                        } catch (CertPathValidatorException e2) {
                            throw new AnnotatedException("Public key of issuer certificate of CRL could not be retrieved.", e2);
                        } catch (Exception e3) {
                            throw new AnnotatedException(e3.getMessage());
                        }
                    }
                }
                HashSet hashSet = new HashSet();
                AnnotatedException annotatedException = null;
                for (i = 0; i < arrayList.size(); i++) {
                    boolean[] keyUsage = ((X509Certificate) arrayList.get(i)).getKeyUsage();
                    if (keyUsage == null || (keyUsage.length >= 7 && keyUsage[6])) {
                        hashSet.add(arrayList2.get(i));
                    } else {
                        annotatedException = new AnnotatedException("Issuer certificate key usage extension does not permit CRL signing.");
                    }
                }
                if (hashSet.isEmpty() && annotatedException == null) {
                    throw new AnnotatedException("Cannot find a valid issuer certificate.");
                } else if (!hashSet.isEmpty() || annotatedException == null) {
                    return hashSet;
                } else {
                    throw annotatedException;
                }
            } catch (AnnotatedException e4) {
                throw new AnnotatedException("Issuer certificate for CRL cannot be searched.", e4);
            }
        } catch (IOException e5) {
            throw new AnnotatedException("subject criteria for certificate selector to find issuer certificate for CRL could not be set", e5);
        }
    }

    protected static PublicKey processCRLG(X509CRL x509crl, Set set) throws AnnotatedException {
        Iterator it = set.iterator();
        Exception e = null;
        while (it.hasNext()) {
            PublicKey publicKey = (PublicKey) it.next();
            try {
                x509crl.verify(publicKey);
                return publicKey;
            } catch (Exception e2) {
                e = e2;
            }
        }
        throw new AnnotatedException("Cannot verify CRL.", e);
    }

    protected static X509CRL processCRLH(Set set, PublicKey publicKey) throws AnnotatedException {
        Iterator it = set.iterator();
        Exception e = null;
        while (it.hasNext()) {
            X509CRL x509crl = (X509CRL) it.next();
            try {
                x509crl.verify(publicKey);
                return x509crl;
            } catch (Exception e2) {
                e = e2;
            }
        }
        if (e == null) {
            return null;
        }
        throw new AnnotatedException("Cannot verify delta CRL.", e);
    }

    protected static void processCRLI(Date date, X509CRL x509crl, Object obj, CertStatus certStatus, PKIXExtendedParameters pKIXExtendedParameters) throws AnnotatedException {
        if (pKIXExtendedParameters.isUseDeltasEnabled() && x509crl != null) {
            RevocationUtilities.getCertStatus(date, x509crl, obj, certStatus);
        }
    }

    protected static void processCRLJ(Date date, X509CRL x509crl, Object obj, CertStatus certStatus) throws AnnotatedException {
        if (certStatus.getCertStatus() == 11) {
            RevocationUtilities.getCertStatus(date, x509crl, obj, certStatus);
        }
    }
}
