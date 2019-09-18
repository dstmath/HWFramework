package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1String;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.x500.RDN;
import com.android.org.bouncycastle.asn1.x500.X500Name;
import com.android.org.bouncycastle.asn1.x500.style.BCStyle;
import com.android.org.bouncycastle.asn1.x509.BasicConstraints;
import com.android.org.bouncycastle.asn1.x509.CRLDistPoint;
import com.android.org.bouncycastle.asn1.x509.DistributionPoint;
import com.android.org.bouncycastle.asn1.x509.DistributionPointName;
import com.android.org.bouncycastle.asn1.x509.Extension;
import com.android.org.bouncycastle.asn1.x509.GeneralName;
import com.android.org.bouncycastle.asn1.x509.GeneralNames;
import com.android.org.bouncycastle.asn1.x509.GeneralSubtree;
import com.android.org.bouncycastle.asn1.x509.IssuingDistributionPoint;
import com.android.org.bouncycastle.asn1.x509.NameConstraints;
import com.android.org.bouncycastle.asn1.x509.PolicyInformation;
import com.android.org.bouncycastle.jcajce.PKIXCRLStore;
import com.android.org.bouncycastle.jcajce.PKIXCRLStoreSelector;
import com.android.org.bouncycastle.jcajce.PKIXCertStoreSelector;
import com.android.org.bouncycastle.jcajce.PKIXExtendedBuilderParameters;
import com.android.org.bouncycastle.jcajce.PKIXExtendedParameters;
import com.android.org.bouncycastle.jcajce.util.JcaJceHelper;
import com.android.org.bouncycastle.jce.exception.ExtCertPathValidatorException;
import com.android.org.bouncycastle.util.Arrays;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.cert.X509Extension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

class RFC3280CertPathUtilities {
    public static final String ANY_POLICY = "2.5.29.32.0";
    public static final String AUTHORITY_KEY_IDENTIFIER = Extension.authorityKeyIdentifier.getId();
    public static final String BASIC_CONSTRAINTS = Extension.basicConstraints.getId();
    public static final String CERTIFICATE_POLICIES = Extension.certificatePolicies.getId();
    public static final String CRL_DISTRIBUTION_POINTS = Extension.cRLDistributionPoints.getId();
    public static final String CRL_NUMBER = Extension.cRLNumber.getId();
    protected static final int CRL_SIGN = 6;
    private static final PKIXCRLUtil CRL_UTIL = new PKIXCRLUtil();
    public static final String DELTA_CRL_INDICATOR = Extension.deltaCRLIndicator.getId();
    public static final String FRESHEST_CRL = Extension.freshestCRL.getId();
    public static final String INHIBIT_ANY_POLICY = Extension.inhibitAnyPolicy.getId();
    public static final String ISSUING_DISTRIBUTION_POINT = Extension.issuingDistributionPoint.getId();
    protected static final int KEY_CERT_SIGN = 5;
    public static final String KEY_USAGE = Extension.keyUsage.getId();
    public static final String NAME_CONSTRAINTS = Extension.nameConstraints.getId();
    public static final String POLICY_CONSTRAINTS = Extension.policyConstraints.getId();
    public static final String POLICY_MAPPINGS = Extension.policyMappings.getId();
    public static final String SUBJECT_ALTERNATIVE_NAME = Extension.subjectAlternativeName.getId();
    protected static final String[] crlReasons = {"unspecified", "keyCompromise", "cACompromise", "affiliationChanged", "superseded", "cessationOfOperation", "certificateHold", "unknown", "removeFromCRL", "privilegeWithdrawn", "aACompromise"};

    RFC3280CertPathUtilities() {
    }

    protected static void processCRLB2(DistributionPoint dp, Object cert, X509CRL crl) throws AnnotatedException {
        GeneralName[] genNames;
        try {
            IssuingDistributionPoint idp = IssuingDistributionPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(crl, ISSUING_DISTRIBUTION_POINT));
            if (idp != null) {
                if (idp.getDistributionPoint() != null) {
                    DistributionPointName dpName = IssuingDistributionPoint.getInstance(idp).getDistributionPoint();
                    List names = new ArrayList();
                    int j = 0;
                    if (dpName.getType() == 0) {
                        GeneralName[] genNames2 = GeneralNames.getInstance(dpName.getName()).getNames();
                        for (GeneralName add : genNames2) {
                            names.add(add);
                        }
                    }
                    if (dpName.getType() == 1) {
                        ASN1EncodableVector vec = new ASN1EncodableVector();
                        try {
                            Enumeration e = ASN1Sequence.getInstance(PrincipalUtils.getIssuerPrincipal(crl)).getObjects();
                            while (e.hasMoreElements()) {
                                vec.add((ASN1Encodable) e.nextElement());
                            }
                            vec.add(dpName.getName());
                            names.add(new GeneralName(X500Name.getInstance(new DERSequence(vec))));
                        } catch (Exception e2) {
                            throw new AnnotatedException("Could not read CRL issuer.", e2);
                        }
                    }
                    boolean matches = false;
                    if (dp.getDistributionPoint() != null) {
                        DistributionPointName dpName2 = dp.getDistributionPoint();
                        GeneralName[] genNames3 = null;
                        if (dpName2.getType() == 0) {
                            genNames3 = GeneralNames.getInstance(dpName2.getName()).getNames();
                        }
                        if (dpName2.getType() == 1) {
                            if (dp.getCRLIssuer() != null) {
                                genNames = dp.getCRLIssuer().getNames();
                            } else {
                                genNames = new GeneralName[1];
                                try {
                                    genNames[0] = new GeneralName(X500Name.getInstance(PrincipalUtils.getEncodedIssuerPrincipal(cert).getEncoded()));
                                } catch (Exception e3) {
                                    throw new AnnotatedException("Could not read certificate issuer.", e3);
                                }
                            }
                            genNames3 = genNames;
                            for (int j2 = 0; j2 < genNames3.length; j2++) {
                                Enumeration e4 = ASN1Sequence.getInstance(genNames3[j2].getName().toASN1Primitive()).getObjects();
                                ASN1EncodableVector vec2 = new ASN1EncodableVector();
                                while (e4.hasMoreElements()) {
                                    vec2.add((ASN1Encodable) e4.nextElement());
                                }
                                vec2.add(dpName2.getName());
                                genNames3[j2] = new GeneralName(X500Name.getInstance(new DERSequence(vec2)));
                            }
                        }
                        if (genNames3 != null) {
                            while (true) {
                                if (j >= genNames3.length) {
                                    break;
                                } else if (names.contains(genNames3[j])) {
                                    matches = true;
                                    break;
                                } else {
                                    j++;
                                }
                            }
                        }
                        if (!matches) {
                            throw new AnnotatedException("No match for certificate CRL issuing distribution point name to cRLIssuer CRL distribution point.");
                        }
                    } else if (dp.getCRLIssuer() != null) {
                        GeneralName[] genNames4 = dp.getCRLIssuer().getNames();
                        while (true) {
                            if (j >= genNames4.length) {
                                break;
                            } else if (names.contains(genNames4[j])) {
                                matches = true;
                                break;
                            } else {
                                j++;
                            }
                        }
                        if (!matches) {
                            throw new AnnotatedException("No match for certificate CRL issuing distribution point name to cRLIssuer CRL distribution point.");
                        }
                    } else {
                        throw new AnnotatedException("Either the cRLIssuer or the distributionPoint field must be contained in DistributionPoint.");
                    }
                }
                try {
                    BasicConstraints bc = BasicConstraints.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Extension) cert, BASIC_CONSTRAINTS));
                    if (cert instanceof X509Certificate) {
                        if (idp.onlyContainsUserCerts() && bc != null && bc.isCA()) {
                            throw new AnnotatedException("CA Cert CRL only contains user certificates.");
                        } else if (idp.onlyContainsCACerts() && (bc == null || !bc.isCA())) {
                            throw new AnnotatedException("End CRL only contains CA certificates.");
                        }
                    }
                    if (idp.onlyContainsAttributeCerts()) {
                        throw new AnnotatedException("onlyContainsAttributeCerts boolean is asserted.");
                    }
                } catch (Exception e5) {
                    throw new AnnotatedException("Basic constraints extension could not be decoded.", e5);
                }
            }
        } catch (Exception e6) {
            throw new AnnotatedException("Issuing distribution point extension could not be decoded.", e6);
        }
    }

    protected static void processCRLB1(DistributionPoint dp, Object cert, X509CRL crl) throws AnnotatedException {
        ASN1Primitive idp = CertPathValidatorUtilities.getExtensionValue(crl, ISSUING_DISTRIBUTION_POINT);
        boolean isIndirect = false;
        if (idp != null && IssuingDistributionPoint.getInstance(idp).isIndirectCRL()) {
            isIndirect = true;
        }
        try {
            byte[] issuerBytes = PrincipalUtils.getIssuerPrincipal(crl).getEncoded();
            boolean matchIssuer = false;
            if (dp.getCRLIssuer() != null) {
                GeneralName[] genNames = dp.getCRLIssuer().getNames();
                for (int j = 0; j < genNames.length; j++) {
                    if (genNames[j].getTagNo() == 4) {
                        try {
                            if (Arrays.areEqual(genNames[j].getName().toASN1Primitive().getEncoded(), issuerBytes)) {
                                matchIssuer = true;
                            }
                        } catch (IOException e) {
                            throw new AnnotatedException("CRL issuer information from distribution point cannot be decoded.", e);
                        }
                    }
                }
                if (matchIssuer && !isIndirect) {
                    throw new AnnotatedException("Distribution point contains cRLIssuer field but CRL is not indirect.");
                } else if (!matchIssuer) {
                    throw new AnnotatedException("CRL issuer of CRL does not match CRL issuer of distribution point.");
                }
            } else if (PrincipalUtils.getIssuerPrincipal(crl).equals(PrincipalUtils.getEncodedIssuerPrincipal(cert))) {
                matchIssuer = true;
            }
            if (!matchIssuer) {
                throw new AnnotatedException("Cannot find matching CRL issuer for certificate.");
            }
        } catch (IOException e2) {
            throw new AnnotatedException("Exception encoding CRL issuer: " + e2.getMessage(), e2);
        }
    }

    protected static ReasonsMask processCRLD(X509CRL crl, DistributionPoint dp) throws AnnotatedException {
        ReasonsMask reasonsMask;
        ReasonsMask reasonsMask2;
        try {
            IssuingDistributionPoint idp = IssuingDistributionPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(crl, ISSUING_DISTRIBUTION_POINT));
            if (idp != null && idp.getOnlySomeReasons() != null && dp.getReasons() != null) {
                return new ReasonsMask(dp.getReasons()).intersect(new ReasonsMask(idp.getOnlySomeReasons()));
            }
            if ((idp == null || idp.getOnlySomeReasons() == null) && dp.getReasons() == null) {
                return ReasonsMask.allReasons;
            }
            if (dp.getReasons() == null) {
                reasonsMask = ReasonsMask.allReasons;
            } else {
                reasonsMask = new ReasonsMask(dp.getReasons());
            }
            if (idp == null) {
                reasonsMask2 = ReasonsMask.allReasons;
            } else {
                reasonsMask2 = new ReasonsMask(idp.getOnlySomeReasons());
            }
            return reasonsMask.intersect(reasonsMask2);
        } catch (Exception e) {
            throw new AnnotatedException("Issuing distribution point extension could not be decoded.", e);
        }
    }

    protected static Set processCRLF(X509CRL crl, Object cert, X509Certificate defaultCRLSignCert, PublicKey defaultCRLSignKey, PKIXExtendedParameters paramsPKIX, List certPathCerts, JcaJceHelper helper) throws AnnotatedException {
        List validKeys;
        PKIXExtendedParameters.Builder paramsBuilder;
        X509Certificate x509Certificate = defaultCRLSignCert;
        X509CertSelector certSelector = new X509CertSelector();
        try {
            certSelector.setSubject(PrincipalUtils.getIssuerPrincipal(crl).getEncoded());
            PKIXCertStoreSelector selector = new PKIXCertStoreSelector.Builder(certSelector).build();
            try {
                Collection coll = CertPathValidatorUtilities.findCertificates(selector, paramsPKIX.getCertificateStores());
                coll.addAll(CertPathValidatorUtilities.findCertificates(selector, paramsPKIX.getCertStores()));
                coll.add(x509Certificate);
                Iterator cert_it = coll.iterator();
                List validCerts = new ArrayList();
                List validKeys2 = new ArrayList();
                while (true) {
                    validKeys = validKeys2;
                    if (!cert_it.hasNext()) {
                        break;
                    }
                    X509Certificate signingCert = (X509Certificate) cert_it.next();
                    if (signingCert.equals(x509Certificate)) {
                        validCerts.add(signingCert);
                        validKeys.add(defaultCRLSignKey);
                        PKIXExtendedParameters pKIXExtendedParameters = paramsPKIX;
                        List list = certPathCerts;
                        JcaJceHelper jcaJceHelper = helper;
                    } else {
                        PublicKey publicKey = defaultCRLSignKey;
                        try {
                            PKIXCertPathBuilderSpi builder = new PKIXCertPathBuilderSpi();
                            X509CertSelector tmpCertSelector = new X509CertSelector();
                            tmpCertSelector.setCertificate(signingCert);
                            try {
                                paramsBuilder = new PKIXExtendedParameters.Builder(paramsPKIX).setTargetConstraints(new PKIXCertStoreSelector.Builder(tmpCertSelector).build());
                            } catch (CertPathBuilderException e) {
                                e = e;
                                List list2 = certPathCerts;
                                JcaJceHelper jcaJceHelper2 = helper;
                                throw new AnnotatedException("CertPath for CRL signer failed to validate.", e);
                            } catch (CertPathValidatorException e2) {
                                e = e2;
                                List list3 = certPathCerts;
                                JcaJceHelper jcaJceHelper3 = helper;
                                throw new AnnotatedException("Public key of issuer certificate of CRL could not be retrieved.", e);
                            } catch (Exception e3) {
                                e = e3;
                                List list4 = certPathCerts;
                                JcaJceHelper jcaJceHelper4 = helper;
                                throw new AnnotatedException(e.getMessage());
                            }
                            try {
                                if (certPathCerts.contains(signingCert)) {
                                    paramsBuilder.setRevocationEnabled(false);
                                } else {
                                    paramsBuilder.setRevocationEnabled(true);
                                }
                                List certs = builder.engineBuild(new PKIXExtendedBuilderParameters.Builder(paramsBuilder.build()).build()).getCertPath().getCertificates();
                                validCerts.add(signingCert);
                                PKIXCertPathBuilderSpi pKIXCertPathBuilderSpi = builder;
                                try {
                                    validKeys.add(CertPathValidatorUtilities.getNextWorkingKey(certs, 0, helper));
                                } catch (CertPathBuilderException e4) {
                                    e = e4;
                                } catch (CertPathValidatorException e5) {
                                    e = e5;
                                    throw new AnnotatedException("Public key of issuer certificate of CRL could not be retrieved.", e);
                                } catch (Exception e6) {
                                    e = e6;
                                    throw new AnnotatedException(e.getMessage());
                                }
                            } catch (CertPathBuilderException e7) {
                                e = e7;
                                JcaJceHelper jcaJceHelper22 = helper;
                                throw new AnnotatedException("CertPath for CRL signer failed to validate.", e);
                            } catch (CertPathValidatorException e8) {
                                e = e8;
                                JcaJceHelper jcaJceHelper32 = helper;
                                throw new AnnotatedException("Public key of issuer certificate of CRL could not be retrieved.", e);
                            } catch (Exception e9) {
                                e = e9;
                                JcaJceHelper jcaJceHelper42 = helper;
                                throw new AnnotatedException(e.getMessage());
                            }
                        } catch (CertPathBuilderException e10) {
                            e = e10;
                            PKIXExtendedParameters pKIXExtendedParameters2 = paramsPKIX;
                            List list22 = certPathCerts;
                            JcaJceHelper jcaJceHelper222 = helper;
                            throw new AnnotatedException("CertPath for CRL signer failed to validate.", e);
                        } catch (CertPathValidatorException e11) {
                            e = e11;
                            PKIXExtendedParameters pKIXExtendedParameters3 = paramsPKIX;
                            List list32 = certPathCerts;
                            JcaJceHelper jcaJceHelper322 = helper;
                            throw new AnnotatedException("Public key of issuer certificate of CRL could not be retrieved.", e);
                        } catch (Exception e12) {
                            e = e12;
                            PKIXExtendedParameters pKIXExtendedParameters4 = paramsPKIX;
                            List list42 = certPathCerts;
                            JcaJceHelper jcaJceHelper422 = helper;
                            throw new AnnotatedException(e.getMessage());
                        }
                    }
                    validKeys2 = validKeys;
                    x509Certificate = defaultCRLSignCert;
                }
                PublicKey publicKey2 = defaultCRLSignKey;
                PKIXExtendedParameters pKIXExtendedParameters5 = paramsPKIX;
                List list5 = certPathCerts;
                JcaJceHelper jcaJceHelper5 = helper;
                int i = 0;
                Set checkKeys = new HashSet();
                AnnotatedException lastException = null;
                while (i < validCerts.size()) {
                    boolean[] keyusage = ((X509Certificate) validCerts.get(i)).getKeyUsage();
                    if (keyusage == null || (keyusage.length >= 7 && keyusage[6])) {
                        checkKeys.add(validKeys.get(i));
                    } else {
                        lastException = new AnnotatedException("Issuer certificate key usage extension does not permit CRL signing.");
                    }
                    i++;
                    JcaJceHelper jcaJceHelper6 = helper;
                }
                if (checkKeys.isEmpty() != 0 && lastException == null) {
                    throw new AnnotatedException("Cannot find a valid issuer certificate.");
                } else if (!checkKeys.isEmpty() || lastException == null) {
                    return checkKeys;
                } else {
                    throw lastException;
                }
            } catch (AnnotatedException e13) {
                PublicKey publicKey3 = defaultCRLSignKey;
                PKIXExtendedParameters pKIXExtendedParameters6 = paramsPKIX;
                List list6 = certPathCerts;
                throw new AnnotatedException("Issuer certificate for CRL cannot be searched.", e13);
            }
        } catch (IOException e14) {
            PublicKey publicKey4 = defaultCRLSignKey;
            PKIXExtendedParameters pKIXExtendedParameters7 = paramsPKIX;
            List list7 = certPathCerts;
            throw new AnnotatedException("Subject criteria for certificate selector to find issuer certificate for CRL could not be set.", e14);
        }
    }

    protected static PublicKey processCRLG(X509CRL crl, Set keys) throws AnnotatedException {
        Exception lastException = null;
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            PublicKey key = (PublicKey) it.next();
            try {
                crl.verify(key);
                return key;
            } catch (Exception e) {
                lastException = e;
            }
        }
        throw new AnnotatedException("Cannot verify CRL.", lastException);
    }

    protected static X509CRL processCRLH(Set deltacrls, PublicKey key) throws AnnotatedException {
        Exception lastException = null;
        Iterator it = deltacrls.iterator();
        while (it.hasNext()) {
            X509CRL crl = (X509CRL) it.next();
            try {
                crl.verify(key);
                return crl;
            } catch (Exception e) {
                lastException = e;
            }
        }
        if (lastException == null) {
            return null;
        }
        throw new AnnotatedException("Cannot verify delta CRL.", lastException);
    }

    protected static Set processCRLA1i(Date currentDate, PKIXExtendedParameters paramsPKIX, X509Certificate cert, X509CRL crl) throws AnnotatedException {
        Set set = new HashSet();
        if (paramsPKIX.isUseDeltasEnabled()) {
            try {
                CRLDistPoint freshestCRL = CRLDistPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(cert, FRESHEST_CRL));
                if (freshestCRL == null) {
                    try {
                        freshestCRL = CRLDistPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(crl, FRESHEST_CRL));
                    } catch (AnnotatedException e) {
                        throw new AnnotatedException("Freshest CRL extension could not be decoded from CRL.", e);
                    }
                }
                if (freshestCRL != null) {
                    List crlStores = new ArrayList();
                    crlStores.addAll(paramsPKIX.getCRLStores());
                    try {
                        crlStores.addAll(CertPathValidatorUtilities.getAdditionalStoresFromCRLDistributionPoint(freshestCRL, paramsPKIX.getNamedCRLStoreMap()));
                        try {
                            set.addAll(CertPathValidatorUtilities.getDeltaCRLs(currentDate, crl, paramsPKIX.getCertStores(), crlStores));
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
        return set;
    }

    protected static Set[] processCRLA1ii(Date currentDate, PKIXExtendedParameters paramsPKIX, X509Certificate cert, X509CRL crl) throws AnnotatedException {
        Set deltaSet = new HashSet();
        X509CRLSelector crlselect = new X509CRLSelector();
        crlselect.setCertificateChecking(cert);
        try {
            crlselect.addIssuerName(PrincipalUtils.getIssuerPrincipal(crl).getEncoded());
            PKIXCRLStoreSelector extSelect = new PKIXCRLStoreSelector.Builder(crlselect).setCompleteCRLEnabled(true).build();
            Date validityDate = currentDate;
            if (paramsPKIX.getDate() != null) {
                validityDate = paramsPKIX.getDate();
            }
            Set completeSet = CRL_UTIL.findCRLs(extSelect, validityDate, paramsPKIX.getCertStores(), paramsPKIX.getCRLStores());
            if (paramsPKIX.isUseDeltasEnabled()) {
                try {
                    deltaSet.addAll(CertPathValidatorUtilities.getDeltaCRLs(validityDate, crl, paramsPKIX.getCertStores(), paramsPKIX.getCRLStores()));
                } catch (AnnotatedException e) {
                    throw new AnnotatedException("Exception obtaining delta CRLs.", e);
                }
            }
            return new Set[]{completeSet, deltaSet};
        } catch (IOException e2) {
            throw new AnnotatedException("Cannot extract issuer from CRL." + e2, e2);
        }
    }

    protected static void processCRLC(X509CRL deltaCRL, X509CRL completeCRL, PKIXExtendedParameters pkixParams) throws AnnotatedException {
        if (deltaCRL != null) {
            try {
                IssuingDistributionPoint completeidp = IssuingDistributionPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(completeCRL, ISSUING_DISTRIBUTION_POINT));
                if (pkixParams.isUseDeltasEnabled()) {
                    if (PrincipalUtils.getIssuerPrincipal(deltaCRL).equals(PrincipalUtils.getIssuerPrincipal(completeCRL))) {
                        try {
                            IssuingDistributionPoint deltaidp = IssuingDistributionPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(deltaCRL, ISSUING_DISTRIBUTION_POINT));
                            boolean match = false;
                            if (completeidp == null) {
                                if (deltaidp == null) {
                                    match = true;
                                }
                            } else if (completeidp.equals(deltaidp)) {
                                match = true;
                            }
                            if (match) {
                                try {
                                    ASN1Primitive completeKeyIdentifier = CertPathValidatorUtilities.getExtensionValue(completeCRL, AUTHORITY_KEY_IDENTIFIER);
                                    try {
                                        ASN1Primitive deltaKeyIdentifier = CertPathValidatorUtilities.getExtensionValue(deltaCRL, AUTHORITY_KEY_IDENTIFIER);
                                        if (completeKeyIdentifier == null) {
                                            throw new AnnotatedException("CRL authority key identifier is null.");
                                        } else if (deltaKeyIdentifier == null) {
                                            throw new AnnotatedException("Delta CRL authority key identifier is null.");
                                        } else if (!completeKeyIdentifier.equals(deltaKeyIdentifier)) {
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
                        throw new AnnotatedException("Complete CRL issuer does not match delta CRL issuer.");
                    }
                }
            } catch (Exception e4) {
                throw new AnnotatedException("Issuing distribution point extension could not be decoded.", e4);
            }
        }
    }

    protected static void processCRLI(Date validDate, X509CRL deltacrl, Object cert, CertStatus certStatus, PKIXExtendedParameters pkixParams) throws AnnotatedException {
        if (pkixParams.isUseDeltasEnabled() && deltacrl != null) {
            CertPathValidatorUtilities.getCertStatus(validDate, deltacrl, cert, certStatus);
        }
    }

    protected static void processCRLJ(Date validDate, X509CRL completecrl, Object cert, CertStatus certStatus) throws AnnotatedException {
        if (certStatus.getCertStatus() == 11) {
            CertPathValidatorUtilities.getCertStatus(validDate, completecrl, cert, certStatus);
        }
    }

    protected static PKIXPolicyNode prepareCertB(CertPath certPath, int index, List[] policyNodes, PKIXPolicyNode validPolicyTree, int policyMapping) throws CertPathValidatorException {
        ASN1Sequence mappings;
        X509Certificate cert;
        Map m_idp;
        int n;
        Set s_idp;
        List certs;
        Iterator it_idp;
        Set pq;
        CertPath certPath2 = certPath;
        int i = index;
        List[] listArr = policyNodes;
        List certificates = certPath.getCertificates();
        X509Certificate cert2 = (X509Certificate) certificates.get(i);
        int n2 = certificates.size();
        int i2 = n2 - i;
        try {
            ASN1Sequence pm = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue(cert2, POLICY_MAPPINGS));
            PKIXPolicyNode _validPolicyTree = validPolicyTree;
            if (pm != null) {
                ASN1Sequence mappings2 = pm;
                Map hashMap = new HashMap();
                Set hashSet = new HashSet();
                int i3 = 0;
                int j = 0;
                while (j < mappings2.size()) {
                    ASN1Sequence mapping = (ASN1Sequence) mappings2.getObjectAt(j);
                    String id_p = ((ASN1ObjectIdentifier) mapping.getObjectAt(i3)).getId();
                    String sd_p = ((ASN1ObjectIdentifier) mapping.getObjectAt(1)).getId();
                    if (!hashMap.containsKey(id_p)) {
                        Set tmp = new HashSet();
                        tmp.add(sd_p);
                        hashMap.put(id_p, tmp);
                        hashSet.add(id_p);
                    } else {
                        ((Set) hashMap.get(id_p)).add(sd_p);
                    }
                    j++;
                    i3 = 0;
                }
                Iterator it_idp2 = hashSet.iterator();
                PKIXPolicyNode _validPolicyTree2 = _validPolicyTree;
                while (true) {
                    Iterator it_idp3 = it_idp2;
                    if (it_idp3.hasNext()) {
                        String id_p2 = (String) it_idp3.next();
                        if (policyMapping > 0) {
                            boolean idp_found = false;
                            Iterator nodes_i = listArr[i2].iterator();
                            while (true) {
                                if (!nodes_i.hasNext()) {
                                    break;
                                }
                                PKIXPolicyNode node = (PKIXPolicyNode) nodes_i.next();
                                if (node.getValidPolicy().equals(id_p2)) {
                                    idp_found = true;
                                    node.expectedPolicies = (Set) hashMap.get(id_p2);
                                    break;
                                }
                            }
                            if (!idp_found) {
                                Iterator nodes_i2 = listArr[i2].iterator();
                                while (true) {
                                    if (!nodes_i2.hasNext()) {
                                        break;
                                    }
                                    PKIXPolicyNode node2 = (PKIXPolicyNode) nodes_i2.next();
                                    if (ANY_POLICY.equals(node2.getValidPolicy())) {
                                        try {
                                            certs = certificates;
                                            ASN1Sequence policies = (ASN1Sequence) CertPathValidatorUtilities.getExtensionValue(cert2, CERTIFICATE_POLICIES);
                                            Enumeration e = policies.getObjects();
                                            while (true) {
                                                ASN1Sequence policies2 = policies;
                                                Enumeration e2 = e;
                                                if (!e2.hasMoreElements()) {
                                                    n = n2;
                                                    pq = null;
                                                    break;
                                                }
                                                try {
                                                    Enumeration e3 = e2;
                                                    n = n2;
                                                    PolicyInformation pinfo = PolicyInformation.getInstance(e2.nextElement());
                                                    if (ANY_POLICY.equals(pinfo.getPolicyIdentifier().getId())) {
                                                        try {
                                                            pq = CertPathValidatorUtilities.getQualifierSet(pinfo.getPolicyQualifiers());
                                                            break;
                                                        } catch (CertPathValidatorException ex) {
                                                            PolicyInformation policyInformation = pinfo;
                                                            throw new ExtCertPathValidatorException("Policy qualifier info set could not be decoded.", ex, certPath2, i);
                                                        }
                                                    } else {
                                                        policies = policies2;
                                                        e = e3;
                                                        n2 = n;
                                                    }
                                                } catch (Exception ex2) {
                                                    Enumeration enumeration = e2;
                                                    int i4 = n2;
                                                    throw new CertPathValidatorException("Policy information could not be decoded.", ex2, certPath2, i);
                                                }
                                            }
                                            boolean ci = false;
                                            if (cert2.getCriticalExtensionOIDs() != null) {
                                                ci = cert2.getCriticalExtensionOIDs().contains(CERTIFICATE_POLICIES);
                                            }
                                            PKIXPolicyNode p_node = (PKIXPolicyNode) node2.getParent();
                                            cert = cert2;
                                            if (ANY_POLICY.equals(p_node.getValidPolicy())) {
                                                PKIXPolicyNode pKIXPolicyNode = node2;
                                                Iterator it = nodes_i2;
                                                it_idp = it_idp3;
                                                s_idp = hashSet;
                                                m_idp = hashMap;
                                                mappings = mappings2;
                                                PKIXPolicyNode pKIXPolicyNode2 = new PKIXPolicyNode(new ArrayList(), i2, (Set) hashMap.get(id_p2), p_node, pq, id_p2, ci);
                                                p_node.addChild(pKIXPolicyNode2);
                                                listArr[i2].add(pKIXPolicyNode2);
                                            } else {
                                                it_idp = it_idp3;
                                                s_idp = hashSet;
                                                m_idp = hashMap;
                                                mappings = mappings2;
                                            }
                                        } catch (AnnotatedException e4) {
                                            List list = certificates;
                                            X509Certificate x509Certificate = cert2;
                                            int i5 = n2;
                                            PKIXPolicyNode pKIXPolicyNode3 = node2;
                                            Iterator it2 = nodes_i2;
                                            String str = id_p2;
                                            Iterator it3 = it_idp3;
                                            Set set = hashSet;
                                            Map map = hashMap;
                                            ASN1Sequence aSN1Sequence = mappings2;
                                            throw new ExtCertPathValidatorException("Certificate policies extension could not be decoded.", e4, certPath2, i);
                                        }
                                    } else {
                                        List certs2 = certificates;
                                        X509Certificate x509Certificate2 = cert2;
                                        int i6 = n2;
                                        Iterator it4 = nodes_i2;
                                        String str2 = id_p2;
                                        Iterator it5 = it_idp3;
                                        Set set2 = hashSet;
                                        Map map2 = hashMap;
                                        ASN1Sequence aSN1Sequence2 = mappings2;
                                    }
                                }
                            }
                            certs = certificates;
                            cert = cert2;
                            n = n2;
                            String str3 = id_p2;
                            it_idp = it_idp3;
                            s_idp = hashSet;
                            m_idp = hashMap;
                            mappings = mappings2;
                        } else {
                            certs = certificates;
                            cert = cert2;
                            n = n2;
                            String id_p3 = id_p2;
                            it_idp = it_idp3;
                            s_idp = hashSet;
                            m_idp = hashMap;
                            mappings = mappings2;
                            if (policyMapping <= 0) {
                                Iterator nodes_i3 = listArr[i2].iterator();
                                while (nodes_i3.hasNext()) {
                                    PKIXPolicyNode node3 = (PKIXPolicyNode) nodes_i3.next();
                                    String id_p4 = id_p3;
                                    if (node3.getValidPolicy().equals(id_p4)) {
                                        ((PKIXPolicyNode) node3.getParent()).removeChild(node3);
                                        nodes_i3.remove();
                                        for (int k = i2 - 1; k >= 0; k--) {
                                            List nodes = listArr[k];
                                            PKIXPolicyNode _validPolicyTree3 = _validPolicyTree2;
                                            for (int l = 0; l < nodes.size(); l++) {
                                                PKIXPolicyNode node22 = (PKIXPolicyNode) nodes.get(l);
                                                if (!node22.hasChildren()) {
                                                    _validPolicyTree3 = CertPathValidatorUtilities.removePolicyNode(_validPolicyTree3, listArr, node22);
                                                    if (_validPolicyTree3 == null) {
                                                        break;
                                                    }
                                                }
                                            }
                                            _validPolicyTree2 = _validPolicyTree3;
                                        }
                                    }
                                    id_p3 = id_p4;
                                }
                            }
                        }
                        it_idp2 = it_idp;
                        certificates = certs;
                        hashSet = s_idp;
                        n2 = n;
                        hashMap = m_idp;
                        cert2 = cert;
                        mappings2 = mappings;
                    } else {
                        List certs3 = certificates;
                        X509Certificate x509Certificate3 = cert2;
                        int i7 = n2;
                        return _validPolicyTree2;
                    }
                }
            } else {
                List list2 = certificates;
                X509Certificate x509Certificate4 = cert2;
                int i8 = n2;
                return _validPolicyTree;
            }
        } catch (AnnotatedException ex3) {
            List list3 = certificates;
            X509Certificate x509Certificate5 = cert2;
            int i9 = n2;
            throw new ExtCertPathValidatorException("Policy mappings extension could not be decoded.", ex3, certPath2, i);
        }
    }

    protected static void prepareNextCertA(CertPath certPath, int index) throws CertPathValidatorException {
        try {
            ASN1Sequence pm = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), POLICY_MAPPINGS));
            if (pm != null) {
                ASN1Sequence mappings = pm;
                int j = 0;
                while (j < mappings.size()) {
                    try {
                        ASN1Sequence mapping = DERSequence.getInstance(mappings.getObjectAt(j));
                        ASN1ObjectIdentifier issuerDomainPolicy = ASN1ObjectIdentifier.getInstance(mapping.getObjectAt(0));
                        ASN1ObjectIdentifier subjectDomainPolicy = ASN1ObjectIdentifier.getInstance(mapping.getObjectAt(1));
                        if (ANY_POLICY.equals(issuerDomainPolicy.getId())) {
                            throw new CertPathValidatorException("IssuerDomainPolicy is anyPolicy", null, certPath, index);
                        } else if (!ANY_POLICY.equals(subjectDomainPolicy.getId())) {
                            j++;
                        } else {
                            throw new CertPathValidatorException("SubjectDomainPolicy is anyPolicy,", null, certPath, index);
                        }
                    } catch (Exception e) {
                        throw new ExtCertPathValidatorException("Policy mappings extension contents could not be decoded.", e, certPath, index);
                    }
                }
            }
        } catch (AnnotatedException ex) {
            throw new ExtCertPathValidatorException("Policy mappings extension could not be decoded.", ex, certPath, index);
        }
    }

    protected static void processCertF(CertPath certPath, int index, PKIXPolicyNode validPolicyTree, int explicitPolicy) throws CertPathValidatorException {
        if (explicitPolicy <= 0 && validPolicyTree == null) {
            throw new ExtCertPathValidatorException("No valid policy tree found when one expected.", null, certPath, index);
        }
    }

    protected static PKIXPolicyNode processCertE(CertPath certPath, int index, PKIXPolicyNode validPolicyTree) throws CertPathValidatorException {
        try {
            if (DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), CERTIFICATE_POLICIES)) == null) {
                return null;
            }
            return validPolicyTree;
        } catch (AnnotatedException e) {
            throw new ExtCertPathValidatorException("Could not read certificate policies extension from certificate.", e, certPath, index);
        }
    }

    protected static void processCertBC(CertPath certPath, int index, PKIXNameConstraintValidator nameConstraintValidator) throws CertPathValidatorException {
        CertPath certPath2 = certPath;
        int i = index;
        PKIXNameConstraintValidator pKIXNameConstraintValidator = nameConstraintValidator;
        List certificates = certPath.getCertificates();
        X509Certificate cert = (X509Certificate) certificates.get(i);
        int n = certificates.size();
        int i2 = n - i;
        if (!CertPathValidatorUtilities.isSelfIssued(cert) || i2 >= n) {
            try {
                ASN1Sequence dns = DERSequence.getInstance(PrincipalUtils.getSubjectPrincipal(cert).getEncoded());
                try {
                    pKIXNameConstraintValidator.checkPermittedDN(dns);
                    pKIXNameConstraintValidator.checkExcludedDN(dns);
                    try {
                        GeneralNames altName = GeneralNames.getInstance(CertPathValidatorUtilities.getExtensionValue(cert, SUBJECT_ALTERNATIVE_NAME));
                        RDN[] emails = X500Name.getInstance(dns).getRDNs(BCStyle.EmailAddress);
                        int eI = 0;
                        while (eI != emails.length) {
                            GeneralName emailAsGeneralName = new GeneralName(1, ((ASN1String) emails[eI].getFirst().getValue()).getString());
                            try {
                                pKIXNameConstraintValidator.checkPermitted(emailAsGeneralName);
                                pKIXNameConstraintValidator.checkExcluded(emailAsGeneralName);
                                eI++;
                            } catch (PKIXNameConstraintValidatorException ex) {
                                List<? extends Certificate> list = certificates;
                                throw new CertPathValidatorException("Subtree check for certificate subject alternative email failed.", ex, certPath2, i);
                            }
                        }
                        List certs = certificates;
                        if (altName != null) {
                            try {
                                GeneralName[] genNames = altName.getNames();
                                int j = 0;
                                while (true) {
                                    int j2 = j;
                                    if (j2 < genNames.length) {
                                        try {
                                            pKIXNameConstraintValidator.checkPermitted(genNames[j2]);
                                            pKIXNameConstraintValidator.checkExcluded(genNames[j2]);
                                            j = j2 + 1;
                                        } catch (PKIXNameConstraintValidatorException e) {
                                            throw new CertPathValidatorException("Subtree check for certificate subject alternative name failed.", e, certPath2, i);
                                        }
                                    } else {
                                        return;
                                    }
                                }
                            } catch (Exception e2) {
                                Exception exc = e2;
                                throw new CertPathValidatorException("Subject alternative name contents could not be decoded.", e2, certPath2, i);
                            }
                        }
                    } catch (Exception e3) {
                        List list2 = certificates;
                        throw new CertPathValidatorException("Subject alternative name extension could not be decoded.", e3, certPath2, i);
                    }
                } catch (PKIXNameConstraintValidatorException e4) {
                    List list3 = certificates;
                    throw new CertPathValidatorException("Subtree check for certificate subject failed.", e4, certPath2, i);
                }
            } catch (Exception e5) {
                List list4 = certificates;
                throw new CertPathValidatorException("Exception extracting subject name when checking subtrees.", e5, certPath2, i);
            }
        } else {
            List<? extends Certificate> list5 = certificates;
        }
    }

    protected static PKIXPolicyNode processCertD(CertPath certPath, int index, Set acceptablePolicies, PKIXPolicyNode validPolicyTree, List[] policyNodes, int inhibitAnyPolicy) throws CertPathValidatorException {
        Set pols;
        int i;
        Enumeration e;
        int i2;
        ASN1Sequence certPolicies;
        Set pols2;
        Enumeration e2;
        PolicyInformation pInfo;
        int n;
        int k;
        List _nodes;
        String _policy;
        Iterator _childrenIter;
        CertPath certPath2 = certPath;
        int i3 = index;
        Set set = acceptablePolicies;
        List[] listArr = policyNodes;
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate) certs.get(i3);
        int n2 = certs.size();
        int i4 = n2 - i3;
        try {
            ASN1Sequence certPolicies2 = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue(cert, CERTIFICATE_POLICIES));
            if (certPolicies2 == null || validPolicyTree == null) {
                int i5 = n2;
                ASN1Sequence aSN1Sequence = certPolicies2;
                int i6 = i4;
                return null;
            }
            Enumeration e3 = certPolicies2.getObjects();
            Set pols3 = new HashSet();
            while (true) {
                pols = pols3;
                if (!e3.hasMoreElements()) {
                    break;
                }
                PolicyInformation pInfo2 = PolicyInformation.getInstance(e3.nextElement());
                ASN1ObjectIdentifier pOid = pInfo2.getPolicyIdentifier();
                pols.add(pOid.getId());
                if (!ANY_POLICY.equals(pOid.getId())) {
                    try {
                        Set pq = CertPathValidatorUtilities.getQualifierSet(pInfo2.getPolicyQualifiers());
                        if (!CertPathValidatorUtilities.processCertD1i(i4, listArr, pOid, pq)) {
                            CertPathValidatorUtilities.processCertD1ii(i4, listArr, pOid, pq);
                        }
                    } catch (CertPathValidatorException ex) {
                        List<? extends Certificate> list = certs;
                        throw new ExtCertPathValidatorException("Policy qualifier info set could not be build.", ex, certPath2, i3);
                    }
                }
                pols3 = pols;
                certs = certs;
            }
            if (acceptablePolicies.isEmpty() || set.contains(ANY_POLICY)) {
                acceptablePolicies.clear();
                set.addAll(pols);
            } else {
                Set t1 = new HashSet();
                for (Object o : acceptablePolicies) {
                    if (pols.contains(o)) {
                        t1.add(o);
                    }
                }
                acceptablePolicies.clear();
                set.addAll(t1);
            }
            if (inhibitAnyPolicy > 0 || (i4 < n2 && CertPathValidatorUtilities.isSelfIssued(cert))) {
                Enumeration e4 = certPolicies2.getObjects();
                while (true) {
                    if (!e4.hasMoreElements()) {
                        e = e4;
                        Set set2 = pols;
                        ASN1Sequence aSN1Sequence2 = certPolicies2;
                        i = i4;
                        break;
                    }
                    PolicyInformation pInfo3 = PolicyInformation.getInstance(e4.nextElement());
                    if (ANY_POLICY.equals(pInfo3.getPolicyIdentifier().getId())) {
                        Set _apq = CertPathValidatorUtilities.getQualifierSet(pInfo3.getPolicyQualifiers());
                        List _nodes2 = listArr[i4 - 1];
                        int k2 = 0;
                        while (true) {
                            int k3 = k2;
                            if (k3 >= _nodes2.size()) {
                                break;
                            }
                            PKIXPolicyNode _node = (PKIXPolicyNode) _nodes2.get(k3);
                            Iterator _policySetIter = _node.getExpectedPolicies().iterator();
                            while (_policySetIter.hasNext()) {
                                Object _tmp = _policySetIter.next();
                                Iterator _policySetIter2 = _policySetIter;
                                if (_tmp instanceof String) {
                                    _policy = (String) _tmp;
                                } else if (_tmp instanceof ASN1ObjectIdentifier) {
                                    _policy = ((ASN1ObjectIdentifier) _tmp).getId();
                                } else {
                                    n = n2;
                                    k = k3;
                                    _nodes = _nodes2;
                                    pInfo = pInfo3;
                                    e2 = e4;
                                    pols2 = pols;
                                    certPolicies = certPolicies2;
                                    i2 = i4;
                                    _nodes2 = _nodes;
                                    _policySetIter = _policySetIter2;
                                    k3 = k;
                                    n2 = n;
                                    pInfo3 = pInfo;
                                    e4 = e2;
                                    pols = pols2;
                                    certPolicies2 = certPolicies;
                                    i4 = i2;
                                    Set set3 = acceptablePolicies;
                                }
                                boolean _found = false;
                                Iterator _childrenIter2 = _node.getChildren();
                                while (true) {
                                    Object _tmp2 = _tmp;
                                    _childrenIter = _childrenIter2;
                                    if (!_childrenIter.hasNext()) {
                                        break;
                                    }
                                    Iterator _childrenIter3 = _childrenIter;
                                    if (_policy.equals(((PKIXPolicyNode) _childrenIter.next()).getValidPolicy())) {
                                        _found = true;
                                    }
                                    _tmp = _tmp2;
                                    _childrenIter2 = _childrenIter3;
                                }
                                if (!_found) {
                                    Set _newChildExpectedPolicies = new HashSet();
                                    _newChildExpectedPolicies.add(_policy);
                                    n = n2;
                                    PKIXPolicyNode _node2 = _node;
                                    k = k3;
                                    ArrayList arrayList = new ArrayList();
                                    _nodes = _nodes2;
                                    pInfo = pInfo3;
                                    e2 = e4;
                                    pols2 = pols;
                                    certPolicies = certPolicies2;
                                    i2 = i4;
                                    PKIXPolicyNode _node3 = new PKIXPolicyNode(arrayList, i4, _newChildExpectedPolicies, _node2, _apq, _policy, false);
                                    _node = _node2;
                                    _node.addChild(_node3);
                                    listArr[i2].add(_node3);
                                } else {
                                    n = n2;
                                    k = k3;
                                    _nodes = _nodes2;
                                    pInfo = pInfo3;
                                    e2 = e4;
                                    pols2 = pols;
                                    certPolicies = certPolicies2;
                                    i2 = i4;
                                }
                                _nodes2 = _nodes;
                                _policySetIter = _policySetIter2;
                                k3 = k;
                                n2 = n;
                                pInfo3 = pInfo;
                                e4 = e2;
                                pols = pols2;
                                certPolicies2 = certPolicies;
                                i4 = i2;
                                Set set32 = acceptablePolicies;
                            }
                            int n3 = n2;
                            List list2 = _nodes2;
                            PolicyInformation policyInformation = pInfo3;
                            Enumeration enumeration = e4;
                            Set set4 = pols;
                            ASN1Sequence aSN1Sequence3 = certPolicies2;
                            int i7 = i4;
                            k2 = k3 + 1;
                            n2 = n3;
                            Set set5 = acceptablePolicies;
                        }
                        List list3 = _nodes2;
                        PolicyInformation policyInformation2 = pInfo3;
                        e = e4;
                        Set set6 = pols;
                        ASN1Sequence aSN1Sequence4 = certPolicies2;
                        i = i4;
                    } else {
                        Enumeration enumeration2 = e4;
                        HashSet hashSet = pols;
                        ASN1Sequence aSN1Sequence5 = certPolicies2;
                        int i8 = i4;
                        Set set7 = acceptablePolicies;
                    }
                }
                Enumeration enumeration3 = e;
            } else {
                int i9 = n2;
                HashSet hashSet2 = pols;
                ASN1Sequence aSN1Sequence6 = certPolicies2;
                i = i4;
            }
            PKIXPolicyNode _validPolicyTree = validPolicyTree;
            int j = i - 1;
            while (true) {
                int j2 = j;
                if (j2 < 0) {
                    break;
                }
                List nodes = listArr[j2];
                PKIXPolicyNode _validPolicyTree2 = _validPolicyTree;
                for (int k4 = 0; k4 < nodes.size(); k4++) {
                    PKIXPolicyNode node = (PKIXPolicyNode) nodes.get(k4);
                    if (!node.hasChildren()) {
                        _validPolicyTree2 = CertPathValidatorUtilities.removePolicyNode(_validPolicyTree2, listArr, node);
                        if (_validPolicyTree2 == null) {
                            break;
                        }
                    }
                }
                _validPolicyTree = _validPolicyTree2;
                j = j2 - 1;
            }
            Set criticalExtensionOids = cert.getCriticalExtensionOIDs();
            if (criticalExtensionOids != null) {
                boolean critical = criticalExtensionOids.contains(CERTIFICATE_POLICIES);
                List nodes2 = listArr[i];
                int j3 = 0;
                while (true) {
                    int j4 = j3;
                    if (j4 >= nodes2.size()) {
                        break;
                    }
                    ((PKIXPolicyNode) nodes2.get(j4)).setCritical(critical);
                    j3 = j4 + 1;
                }
            }
            return _validPolicyTree;
        } catch (AnnotatedException e5) {
            List list4 = certs;
            int i10 = n2;
            int i11 = i4;
            throw new ExtCertPathValidatorException("Could not read certificate policies extension from certificate.", e5, certPath2, i3);
        }
    }

    protected static void processCertA(CertPath certPath, PKIXExtendedParameters paramsPKIX, int index, PublicKey workingPublicKey, boolean verificationAlreadyPerformed, X500Name workingIssuerName, X509Certificate sign, JcaJceHelper helper) throws ExtCertPathValidatorException {
        PublicKey publicKey;
        CertPath certPath2 = certPath;
        PKIXExtendedParameters pKIXExtendedParameters = paramsPKIX;
        int i = index;
        X500Name x500Name = workingIssuerName;
        List<? extends Certificate> certificates = certPath2.getCertificates();
        X509Certificate cert = (X509Certificate) certificates.get(i);
        if (!verificationAlreadyPerformed) {
            try {
                publicKey = workingPublicKey;
                try {
                    CertPathValidatorUtilities.verifyX509Certificate(cert, publicKey, paramsPKIX.getSigProvider());
                } catch (GeneralSecurityException e) {
                    e = e;
                }
            } catch (GeneralSecurityException e2) {
                e = e2;
                PublicKey publicKey2 = workingPublicKey;
                throw new ExtCertPathValidatorException("Could not validate certificate signature.", e, certPath2, i);
            }
        } else {
            publicKey = workingPublicKey;
        }
        try {
            cert.checkValidity(CertPathValidatorUtilities.getValidCertDateFromValidityModel(pKIXExtendedParameters, certPath2, i));
            if (paramsPKIX.isRevocationEnabled()) {
                try {
                    checkCRLs(pKIXExtendedParameters, cert, CertPathValidatorUtilities.getValidCertDateFromValidityModel(pKIXExtendedParameters, certPath2, i), sign, publicKey, certificates, helper);
                } catch (AnnotatedException e3) {
                    Throwable cause = e3;
                    if (e3.getCause() != null) {
                        cause = e3.getCause();
                    }
                    throw new ExtCertPathValidatorException(e3.getMessage(), cause, certPath2, i);
                }
            }
            if (!PrincipalUtils.getEncodedIssuerPrincipal(cert).equals(x500Name)) {
                throw new ExtCertPathValidatorException("IssuerName(" + PrincipalUtils.getEncodedIssuerPrincipal(cert) + ") does not match SubjectName(" + x500Name + ") of signing certificate.", null, certPath2, i);
            }
        } catch (CertificateExpiredException e4) {
            throw new ExtCertPathValidatorException("Could not validate certificate: " + e4.getMessage(), e4, certPath2, i);
        } catch (CertificateNotYetValidException e5) {
            throw new ExtCertPathValidatorException("Could not validate certificate: " + e5.getMessage(), e5, certPath2, i);
        } catch (AnnotatedException e6) {
            throw new ExtCertPathValidatorException("Could not validate time of certificate.", e6, certPath2, i);
        }
    }

    protected static int prepareNextCertI1(CertPath certPath, int index, int explicitPolicy) throws CertPathValidatorException {
        try {
            ASN1Sequence pc = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), POLICY_CONSTRAINTS));
            if (pc != null) {
                Enumeration policyConstraints = pc.getObjects();
                while (true) {
                    if (!policyConstraints.hasMoreElements()) {
                        break;
                    }
                    try {
                        ASN1TaggedObject constraint = ASN1TaggedObject.getInstance(policyConstraints.nextElement());
                        if (constraint.getTagNo() == 0) {
                            int tmpInt = ASN1Integer.getInstance(constraint, false).getValue().intValue();
                            if (tmpInt < explicitPolicy) {
                                return tmpInt;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        throw new ExtCertPathValidatorException("Policy constraints extension contents cannot be decoded.", e, certPath, index);
                    }
                }
            }
            return explicitPolicy;
        } catch (Exception e2) {
            throw new ExtCertPathValidatorException("Policy constraints extension cannot be decoded.", e2, certPath, index);
        }
    }

    protected static int prepareNextCertI2(CertPath certPath, int index, int policyMapping) throws CertPathValidatorException {
        try {
            ASN1Sequence pc = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), POLICY_CONSTRAINTS));
            if (pc != null) {
                Enumeration policyConstraints = pc.getObjects();
                while (true) {
                    if (!policyConstraints.hasMoreElements()) {
                        break;
                    }
                    try {
                        ASN1TaggedObject constraint = ASN1TaggedObject.getInstance(policyConstraints.nextElement());
                        if (constraint.getTagNo() == 1) {
                            int tmpInt = ASN1Integer.getInstance(constraint, false).getValue().intValue();
                            if (tmpInt < policyMapping) {
                                return tmpInt;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        throw new ExtCertPathValidatorException("Policy constraints extension contents cannot be decoded.", e, certPath, index);
                    }
                }
            }
            return policyMapping;
        } catch (Exception e2) {
            throw new ExtCertPathValidatorException("Policy constraints extension cannot be decoded.", e2, certPath, index);
        }
    }

    protected static void prepareNextCertG(CertPath certPath, int index, PKIXNameConstraintValidator nameConstraintValidator) throws CertPathValidatorException {
        NameConstraints nc = null;
        try {
            ASN1Sequence ncSeq = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), NAME_CONSTRAINTS));
            if (ncSeq != null) {
                nc = NameConstraints.getInstance(ncSeq);
            }
            if (nc != null) {
                GeneralSubtree[] permitted = nc.getPermittedSubtrees();
                if (permitted != null) {
                    try {
                        nameConstraintValidator.intersectPermittedSubtree(permitted);
                    } catch (Exception ex) {
                        throw new ExtCertPathValidatorException("Permitted subtrees cannot be build from name constraints extension.", ex, certPath, index);
                    }
                }
                GeneralSubtree[] excluded = nc.getExcludedSubtrees();
                if (excluded != null) {
                    int i = 0;
                    while (i != excluded.length) {
                        try {
                            nameConstraintValidator.addExcludedSubtree(excluded[i]);
                            i++;
                        } catch (Exception ex2) {
                            throw new ExtCertPathValidatorException("Excluded subtrees cannot be build from name constraints extension.", ex2, certPath, index);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ExtCertPathValidatorException("Name constraints extension could not be decoded.", e, certPath, index);
        }
    }

    private static void checkCRL(DistributionPoint dp, PKIXExtendedParameters paramsPKIX, X509Certificate cert, Date validDate, X509Certificate defaultCRLSignCert, PublicKey defaultCRLSignKey, CertStatus certStatus, ReasonsMask reasonMask, List certPathCerts, JcaJceHelper helper) throws AnnotatedException {
        Iterator crl_iter;
        Iterator crl_iter2;
        Set crls;
        ReasonsMask reasonsMask;
        DistributionPoint distributionPoint = dp;
        PKIXExtendedParameters pKIXExtendedParameters = paramsPKIX;
        X509Certificate x509Certificate = cert;
        Date date = validDate;
        CertStatus certStatus2 = certStatus;
        ReasonsMask reasonsMask2 = reasonMask;
        Date currentDate = new Date(System.currentTimeMillis());
        if (validDate.getTime() <= currentDate.getTime()) {
            Set crls2 = CertPathValidatorUtilities.getCompleteCRLs(distributionPoint, x509Certificate, currentDate, pKIXExtendedParameters);
            Iterator crl_iter3 = crls2.iterator();
            boolean validCrlFound = false;
            AnnotatedException lastException = null;
            while (true) {
                crl_iter = crl_iter3;
                if (!crl_iter.hasNext() || certStatus.getCertStatus() != 11 || reasonMask.isAllReasons()) {
                    Iterator it = crl_iter;
                    ReasonsMask reasonsMask3 = reasonsMask2;
                    Set set = crls2;
                } else {
                    try {
                        X509CRL crl = (X509CRL) crl_iter.next();
                        ReasonsMask interimReasonsMask = processCRLD(crl, distributionPoint);
                        if (!interimReasonsMask.hasNewReasons(reasonsMask2)) {
                            crl_iter3 = crl_iter;
                        } else {
                            crls = crls2;
                            ReasonsMask interimReasonsMask2 = interimReasonsMask;
                            crl_iter2 = crl_iter;
                            try {
                                PublicKey key = processCRLG(crl, processCRLF(crl, x509Certificate, defaultCRLSignCert, defaultCRLSignKey, pKIXExtendedParameters, certPathCerts, helper));
                                X509CRL deltaCRL = null;
                                Date validityDate = currentDate;
                                if (paramsPKIX.getDate() != null) {
                                    validityDate = paramsPKIX.getDate();
                                }
                                if (paramsPKIX.isUseDeltasEnabled()) {
                                    deltaCRL = processCRLH(CertPathValidatorUtilities.getDeltaCRLs(validityDate, crl, paramsPKIX.getCertStores(), paramsPKIX.getCRLStores()), key);
                                }
                                if (paramsPKIX.getValidityModel() != 1) {
                                    if (cert.getNotAfter().getTime() < crl.getThisUpdate().getTime()) {
                                        throw new AnnotatedException("No valid CRL for current time found.");
                                    }
                                }
                                processCRLB1(distributionPoint, x509Certificate, crl);
                                processCRLB2(distributionPoint, x509Certificate, crl);
                                processCRLC(deltaCRL, crl, pKIXExtendedParameters);
                                processCRLI(date, deltaCRL, x509Certificate, certStatus2, pKIXExtendedParameters);
                                processCRLJ(date, crl, x509Certificate, certStatus2);
                                if (certStatus.getCertStatus() == 8) {
                                    certStatus2.setCertStatus(11);
                                }
                                reasonsMask = reasonMask;
                                try {
                                    reasonsMask.addReasons(interimReasonsMask2);
                                    Set criticalExtensions = crl.getCriticalExtensionOIDs();
                                    if (criticalExtensions != null) {
                                        Set criticalExtensions2 = new HashSet(criticalExtensions);
                                        criticalExtensions2.remove(Extension.issuingDistributionPoint.getId());
                                        criticalExtensions2.remove(Extension.deltaCRLIndicator.getId());
                                        if (!criticalExtensions2.isEmpty()) {
                                            throw new AnnotatedException("CRL contains unsupported critical extensions.");
                                        }
                                    }
                                    if (deltaCRL != null) {
                                        Set criticalExtensions3 = deltaCRL.getCriticalExtensionOIDs();
                                        if (criticalExtensions3 != null) {
                                            Set criticalExtensions4 = new HashSet(criticalExtensions3);
                                            criticalExtensions4.remove(Extension.issuingDistributionPoint.getId());
                                            criticalExtensions4.remove(Extension.deltaCRLIndicator.getId());
                                            if (!criticalExtensions4.isEmpty()) {
                                                throw new AnnotatedException("Delta CRL contains unsupported critical extension.");
                                            }
                                        }
                                    }
                                    validCrlFound = true;
                                } catch (AnnotatedException e) {
                                    e = e;
                                    lastException = e;
                                    reasonsMask2 = reasonsMask;
                                    crls2 = crls;
                                    crl_iter3 = crl_iter2;
                                }
                            } catch (AnnotatedException e2) {
                                e = e2;
                                reasonsMask = reasonMask;
                                lastException = e;
                                reasonsMask2 = reasonsMask;
                                crls2 = crls;
                                crl_iter3 = crl_iter2;
                            }
                            reasonsMask2 = reasonsMask;
                            crls2 = crls;
                            crl_iter3 = crl_iter2;
                        }
                    } catch (AnnotatedException e3) {
                        e = e3;
                        crl_iter2 = crl_iter;
                        reasonsMask = reasonsMask2;
                        crls = crls2;
                        lastException = e;
                        reasonsMask2 = reasonsMask;
                        crls2 = crls;
                        crl_iter3 = crl_iter2;
                    }
                }
            }
            Iterator it2 = crl_iter;
            ReasonsMask reasonsMask32 = reasonsMask2;
            Set set2 = crls2;
            if (!validCrlFound) {
                throw lastException;
            }
            return;
        }
        ReasonsMask reasonsMask4 = reasonsMask2;
        throw new AnnotatedException("Validation time is in future.");
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v35, resolved type: com.android.org.bouncycastle.jce.provider.AnnotatedException} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v36, resolved type: com.android.org.bouncycastle.jce.provider.AnnotatedException} */
    /* JADX WARNING: type inference failed for: r0v14, types: [java.lang.Throwable] */
    /* JADX WARNING: type inference failed for: r0v15 */
    /* JADX WARNING: type inference failed for: r0v31, types: [com.android.org.bouncycastle.jce.provider.AnnotatedException] */
    /* JADX WARNING: type inference failed for: r0v32, types: [com.android.org.bouncycastle.jce.provider.AnnotatedException] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00cf  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0136  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x013e  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x014b  */
    /* JADX WARNING: Unknown variable types count: 2 */
    protected static void checkCRLs(PKIXExtendedParameters paramsPKIX, X509Certificate cert, Date validDate, X509Certificate sign, PublicKey workingPublicKey, List certPathCerts, JcaJceHelper helper) throws AnnotatedException {
        ReasonsMask reasonsMask;
        boolean validCrlFound;
        CertStatus certStatus;
        AnnotatedException lastException;
        ReasonsMask reasonsMask2;
        PKIXExtendedParameters.Builder paramsBldr;
        ReasonsMask reasonsMask3;
        DistributionPoint[] dps;
        int i;
        CertStatus certStatus2;
        ReasonsMask reasonsMask4;
        ReasonsMask reasonsMask5 = null;
        try {
            X509Certificate x509Certificate = cert;
            try {
                CRLDistPoint crldp = CRLDistPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(x509Certificate, CRL_DISTRIBUTION_POINTS));
                PKIXExtendedParameters.Builder paramsBldr2 = new PKIXExtendedParameters.Builder(paramsPKIX);
                try {
                    for (PKIXCRLStore addCRLStore : CertPathValidatorUtilities.getAdditionalStoresFromCRLDistributionPoint(crldp, paramsPKIX.getNamedCRLStoreMap())) {
                        try {
                            paramsBldr2.addCRLStore(addCRLStore);
                        } catch (AnnotatedException e) {
                            lastException = e;
                            PKIXExtendedParameters.Builder builder = paramsBldr2;
                        }
                    }
                    CertStatus certStatus3 = new CertStatus();
                    ReasonsMask reasonsMask6 = new ReasonsMask();
                    PKIXExtendedParameters finalParams = paramsBldr2.build();
                    int i2 = 11;
                    if (crldp != null) {
                        try {
                            DistributionPoint[] dps2 = crldp.getDistributionPoints();
                            if (dps2 != null) {
                                validCrlFound = false;
                                int i3 = 0;
                                while (true) {
                                    int i4 = i3;
                                    if (i4 >= dps2.length || certStatus3.getCertStatus() != i2 || reasonsMask6.isAllReasons()) {
                                        reasonsMask = reasonsMask6;
                                        certStatus = certStatus3;
                                        PKIXExtendedParameters.Builder builder2 = paramsBldr2;
                                    } else {
                                        try {
                                            i = i4;
                                            dps = dps2;
                                            int i5 = i2;
                                            reasonsMask3 = reasonsMask6;
                                            certStatus2 = certStatus3;
                                            paramsBldr = paramsBldr2;
                                            try {
                                                checkCRL(dps2[i4], finalParams, x509Certificate, validDate, sign, workingPublicKey, certStatus3, reasonsMask6, certPathCerts, helper);
                                                validCrlFound = true;
                                            } catch (AnnotatedException e2) {
                                                reasonsMask4 = e2;
                                                reasonsMask5 = reasonsMask4;
                                                i3 = i + 1;
                                                certStatus3 = certStatus2;
                                                dps2 = dps;
                                                reasonsMask6 = reasonsMask3;
                                                paramsBldr2 = paramsBldr;
                                                i2 = 11;
                                            }
                                        } catch (AnnotatedException e3) {
                                            i = i4;
                                            dps = dps2;
                                            reasonsMask3 = reasonsMask6;
                                            certStatus2 = certStatus3;
                                            paramsBldr = paramsBldr2;
                                            reasonsMask4 = e3;
                                            reasonsMask5 = reasonsMask4;
                                            i3 = i + 1;
                                            certStatus3 = certStatus2;
                                            dps2 = dps;
                                            reasonsMask6 = reasonsMask3;
                                            paramsBldr2 = paramsBldr;
                                            i2 = 11;
                                        }
                                        i3 = i + 1;
                                        certStatus3 = certStatus2;
                                        dps2 = dps;
                                        reasonsMask6 = reasonsMask3;
                                        paramsBldr2 = paramsBldr;
                                        i2 = 11;
                                    }
                                }
                                reasonsMask = reasonsMask6;
                                certStatus = certStatus3;
                                PKIXExtendedParameters.Builder builder22 = paramsBldr2;
                                if (certStatus.getCertStatus() != 11) {
                                    ReasonsMask reasonsMask7 = reasonsMask;
                                    if (!reasonsMask7.isAllReasons()) {
                                        try {
                                            try {
                                                reasonsMask2 = reasonsMask5;
                                                reasonsMask5 = reasonsMask7;
                                                checkCRL(new DistributionPoint(new DistributionPointName(0, new GeneralNames(new GeneralName(4, (ASN1Encodable) new ASN1InputStream(PrincipalUtils.getEncodedIssuerPrincipal(cert).getEncoded()).readObject()))), null, null), (PKIXExtendedParameters) paramsPKIX.clone(), x509Certificate, validDate, sign, workingPublicKey, certStatus, reasonsMask7, certPathCerts, helper);
                                                validCrlFound = true;
                                            } catch (AnnotatedException e4) {
                                                ReasonsMask reasonsMask8 = reasonsMask5;
                                                reasonsMask5 = reasonsMask7;
                                                lastException = e4;
                                            }
                                        } catch (Exception e5) {
                                            ReasonsMask reasonsMask9 = reasonsMask5;
                                            ReasonsMask reasonsMask10 = reasonsMask7;
                                            throw new AnnotatedException("Issuer from certificate for CRL could not be reencoded.", e5);
                                        } catch (AnnotatedException e6) {
                                            lastException = e6;
                                        }
                                    } else {
                                        reasonsMask2 = reasonsMask5;
                                        reasonsMask5 = reasonsMask7;
                                    }
                                } else {
                                    reasonsMask2 = reasonsMask5;
                                    reasonsMask5 = reasonsMask;
                                }
                                lastException = reasonsMask2;
                                if (validCrlFound) {
                                    if (lastException instanceof AnnotatedException) {
                                        throw lastException;
                                    }
                                    throw new AnnotatedException("No valid CRL found.", lastException);
                                } else if (certStatus.getCertStatus() == 11) {
                                    if (!reasonsMask5.isAllReasons() && certStatus.getCertStatus() == 11) {
                                        certStatus.setCertStatus(12);
                                    }
                                    if (certStatus.getCertStatus() == 12) {
                                        throw new AnnotatedException("Certificate status could not be determined.");
                                    }
                                    return;
                                } else {
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").setTimeZone(TimeZone.getTimeZone("UTC"));
                                    String message = "Certificate revocation after " + df.format(certStatus.getRevocationDate());
                                    throw new AnnotatedException(message + ", reason: " + crlReasons[certStatus.getCertStatus()]);
                                }
                            }
                        } catch (Exception e7) {
                            ReasonsMask reasonsMask11 = reasonsMask6;
                            CertStatus certStatus4 = certStatus3;
                            PKIXExtendedParameters.Builder builder3 = paramsBldr2;
                            Exception exc = e7;
                            throw new AnnotatedException("Distribution points could not be read.", e7);
                        }
                    }
                    reasonsMask = reasonsMask6;
                    certStatus = certStatus3;
                    PKIXExtendedParameters.Builder builder4 = paramsBldr2;
                    validCrlFound = false;
                    if (certStatus.getCertStatus() != 11) {
                    }
                    lastException = reasonsMask2;
                    if (validCrlFound) {
                    }
                } catch (AnnotatedException e8) {
                    lastException = e8;
                    PKIXExtendedParameters.Builder builder5 = paramsBldr2;
                    throw new AnnotatedException("No additional CRL locations could be decoded from CRL distribution point extension.", lastException);
                }
            } catch (Exception e9) {
                e = e9;
                PKIXExtendedParameters pKIXExtendedParameters = paramsPKIX;
                throw new AnnotatedException("CRL distribution point extension could not be read.", e);
            }
        } catch (Exception e10) {
            e = e10;
            PKIXExtendedParameters pKIXExtendedParameters2 = paramsPKIX;
            X509Certificate x509Certificate2 = cert;
            throw new AnnotatedException("CRL distribution point extension could not be read.", e);
        }
    }

    protected static int prepareNextCertJ(CertPath certPath, int index, int inhibitAnyPolicy) throws CertPathValidatorException {
        try {
            ASN1Integer iap = ASN1Integer.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), INHIBIT_ANY_POLICY));
            if (iap != null) {
                int _inhibitAnyPolicy = iap.getValue().intValue();
                if (_inhibitAnyPolicy < inhibitAnyPolicy) {
                    return _inhibitAnyPolicy;
                }
            }
            return inhibitAnyPolicy;
        } catch (Exception e) {
            throw new ExtCertPathValidatorException("Inhibit any-policy extension cannot be decoded.", e, certPath, index);
        }
    }

    protected static void prepareNextCertK(CertPath certPath, int index) throws CertPathValidatorException {
        try {
            BasicConstraints bc = BasicConstraints.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), BASIC_CONSTRAINTS));
            if (bc == null) {
                throw new CertPathValidatorException("Intermediate certificate lacks BasicConstraints");
            } else if (!bc.isCA()) {
                throw new CertPathValidatorException("Not a CA certificate");
            }
        } catch (Exception e) {
            throw new ExtCertPathValidatorException("Basic constraints extension cannot be decoded.", e, certPath, index);
        }
    }

    protected static int prepareNextCertL(CertPath certPath, int index, int maxPathLength) throws CertPathValidatorException {
        if (CertPathValidatorUtilities.isSelfIssued((X509Certificate) certPath.getCertificates().get(index))) {
            return maxPathLength;
        }
        if (maxPathLength > 0) {
            return maxPathLength - 1;
        }
        throw new ExtCertPathValidatorException("Max path length not greater than zero", null, certPath, index);
    }

    protected static int prepareNextCertM(CertPath certPath, int index, int maxPathLength) throws CertPathValidatorException {
        try {
            BasicConstraints bc = BasicConstraints.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), BASIC_CONSTRAINTS));
            if (bc != null) {
                BigInteger _pathLengthConstraint = bc.getPathLenConstraint();
                if (_pathLengthConstraint != null) {
                    int _plc = _pathLengthConstraint.intValue();
                    if (_plc < maxPathLength) {
                        return _plc;
                    }
                }
            }
            return maxPathLength;
        } catch (Exception e) {
            throw new ExtCertPathValidatorException("Basic constraints extension cannot be decoded.", e, certPath, index);
        }
    }

    protected static void prepareNextCertN(CertPath certPath, int index) throws CertPathValidatorException {
        boolean[] _usage = ((X509Certificate) certPath.getCertificates().get(index)).getKeyUsage();
        if (_usage != null && !_usage[5]) {
            throw new ExtCertPathValidatorException("Issuer certificate keyusage extension is critical and does not permit key signing.", null, certPath, index);
        }
    }

    protected static void prepareNextCertO(CertPath certPath, int index, Set criticalExtensions, List pathCheckers) throws CertPathValidatorException {
        X509Certificate cert = (X509Certificate) certPath.getCertificates().get(index);
        Iterator tmpIter = pathCheckers.iterator();
        while (tmpIter.hasNext()) {
            try {
                ((PKIXCertPathChecker) tmpIter.next()).check(cert, criticalExtensions);
            } catch (CertPathValidatorException e) {
                throw new CertPathValidatorException(e.getMessage(), e.getCause(), certPath, index);
            }
        }
        if (!criticalExtensions.isEmpty()) {
            throw new ExtCertPathValidatorException("Certificate has unsupported critical extension: " + criticalExtensions, null, certPath, index);
        }
    }

    protected static int prepareNextCertH1(CertPath certPath, int index, int explicitPolicy) {
        if (CertPathValidatorUtilities.isSelfIssued((X509Certificate) certPath.getCertificates().get(index)) || explicitPolicy == 0) {
            return explicitPolicy;
        }
        return explicitPolicy - 1;
    }

    protected static int prepareNextCertH2(CertPath certPath, int index, int policyMapping) {
        if (CertPathValidatorUtilities.isSelfIssued((X509Certificate) certPath.getCertificates().get(index)) || policyMapping == 0) {
            return policyMapping;
        }
        return policyMapping - 1;
    }

    protected static int prepareNextCertH3(CertPath certPath, int index, int inhibitAnyPolicy) {
        if (CertPathValidatorUtilities.isSelfIssued((X509Certificate) certPath.getCertificates().get(index)) || inhibitAnyPolicy == 0) {
            return inhibitAnyPolicy;
        }
        return inhibitAnyPolicy - 1;
    }

    protected static int wrapupCertA(int explicitPolicy, X509Certificate cert) {
        if (CertPathValidatorUtilities.isSelfIssued(cert) || explicitPolicy == 0) {
            return explicitPolicy;
        }
        return explicitPolicy - 1;
    }

    protected static int wrapupCertB(CertPath certPath, int index, int explicitPolicy) throws CertPathValidatorException {
        try {
            ASN1Sequence pc = DERSequence.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), POLICY_CONSTRAINTS));
            if (pc != null) {
                Enumeration policyConstraints = pc.getObjects();
                while (policyConstraints.hasMoreElements()) {
                    ASN1TaggedObject constraint = (ASN1TaggedObject) policyConstraints.nextElement();
                    if (constraint.getTagNo() == 0) {
                        try {
                            if (ASN1Integer.getInstance(constraint, false).getValue().intValue() == 0) {
                                return 0;
                            }
                        } catch (Exception e) {
                            throw new ExtCertPathValidatorException("Policy constraints requireExplicitPolicy field could not be decoded.", e, certPath, index);
                        }
                    }
                }
            }
            return explicitPolicy;
        } catch (AnnotatedException e2) {
            throw new ExtCertPathValidatorException("Policy constraints could not be decoded.", e2, certPath, index);
        }
    }

    protected static void wrapupCertF(CertPath certPath, int index, List pathCheckers, Set criticalExtensions) throws CertPathValidatorException {
        X509Certificate cert = (X509Certificate) certPath.getCertificates().get(index);
        Iterator tmpIter = pathCheckers.iterator();
        while (tmpIter.hasNext()) {
            try {
                ((PKIXCertPathChecker) tmpIter.next()).check(cert, criticalExtensions);
            } catch (CertPathValidatorException e) {
                throw new ExtCertPathValidatorException("Additional certificate path checker failed.", e, certPath, index);
            }
        }
        if (!criticalExtensions.isEmpty()) {
            throw new ExtCertPathValidatorException("Certificate has unsupported critical extension: " + criticalExtensions, null, certPath, index);
        }
    }

    protected static PKIXPolicyNode wrapupCertG(CertPath certPath, PKIXExtendedParameters paramsPKIX, Set userInitialPolicySet, int index, List[] policyNodes, PKIXPolicyNode validPolicyTree, Set acceptablePolicies) throws CertPathValidatorException {
        PKIXPolicyNode validPolicyTree2;
        CertPath certPath2 = certPath;
        int i = index;
        List[] listArr = policyNodes;
        int n = certPath.getCertificates().size();
        if (validPolicyTree == null) {
            if (!paramsPKIX.isExplicitPolicyRequired()) {
                Set set = userInitialPolicySet;
                PKIXPolicyNode pKIXPolicyNode = validPolicyTree;
                Set set2 = acceptablePolicies;
                return null;
            }
            throw new ExtCertPathValidatorException("Explicit policy requested but none available.", null, certPath2, i);
        } else if (CertPathValidatorUtilities.isAnyPolicy(userInitialPolicySet)) {
            if (!paramsPKIX.isExplicitPolicyRequired()) {
                Set set3 = acceptablePolicies;
            } else if (!acceptablePolicies.isEmpty()) {
                Set<PKIXPolicyNode> _validPolicyNodeSet = new HashSet<>();
                for (List _nodeDepth : listArr) {
                    for (int k = 0; k < _nodeDepth.size(); k++) {
                        PKIXPolicyNode _node = (PKIXPolicyNode) _nodeDepth.get(k);
                        if (ANY_POLICY.equals(_node.getValidPolicy())) {
                            Iterator _iter = _node.getChildren();
                            while (_iter.hasNext()) {
                                _validPolicyNodeSet.add(_iter.next());
                            }
                        }
                    }
                }
                for (PKIXPolicyNode _node2 : _validPolicyNodeSet) {
                    acceptablePolicies.contains(_node2.getValidPolicy());
                }
                Set set4 = acceptablePolicies;
                if (validPolicyTree != null) {
                    int j = n - 1;
                    validPolicyTree2 = validPolicyTree;
                    while (j >= 0) {
                        List nodes = listArr[j];
                        PKIXPolicyNode validPolicyTree3 = validPolicyTree2;
                        for (int k2 = 0; k2 < nodes.size(); k2++) {
                            PKIXPolicyNode node = (PKIXPolicyNode) nodes.get(k2);
                            if (!node.hasChildren()) {
                                validPolicyTree3 = CertPathValidatorUtilities.removePolicyNode(validPolicyTree3, listArr, node);
                            }
                        }
                        j--;
                        validPolicyTree2 = validPolicyTree3;
                    }
                    Set set5 = userInitialPolicySet;
                    return validPolicyTree2;
                }
            } else {
                Set set6 = acceptablePolicies;
                throw new ExtCertPathValidatorException("Explicit policy requested but none available.", null, certPath2, i);
            }
            validPolicyTree2 = validPolicyTree;
            Set set52 = userInitialPolicySet;
            return validPolicyTree2;
        } else {
            Set set7 = acceptablePolicies;
            Set<PKIXPolicyNode> _validPolicyNodeSet2 = new HashSet<>();
            for (List _nodeDepth2 : listArr) {
                for (int k3 = 0; k3 < _nodeDepth2.size(); k3++) {
                    PKIXPolicyNode _node3 = (PKIXPolicyNode) _nodeDepth2.get(k3);
                    if (ANY_POLICY.equals(_node3.getValidPolicy())) {
                        Iterator _iter2 = _node3.getChildren();
                        while (_iter2.hasNext()) {
                            PKIXPolicyNode _c_node = (PKIXPolicyNode) _iter2.next();
                            if (!ANY_POLICY.equals(_c_node.getValidPolicy())) {
                                _validPolicyNodeSet2.add(_c_node);
                            }
                        }
                    }
                }
            }
            PKIXPolicyNode validPolicyTree4 = validPolicyTree;
            for (PKIXPolicyNode _node4 : _validPolicyNodeSet2) {
                if (!userInitialPolicySet.contains(_node4.getValidPolicy())) {
                    validPolicyTree4 = CertPathValidatorUtilities.removePolicyNode(validPolicyTree4, listArr, _node4);
                }
            }
            Set set8 = userInitialPolicySet;
            if (validPolicyTree4 != null) {
                int j2 = n - 1;
                while (j2 >= 0) {
                    List nodes2 = listArr[j2];
                    PKIXPolicyNode validPolicyTree5 = validPolicyTree4;
                    for (int k4 = 0; k4 < nodes2.size(); k4++) {
                        PKIXPolicyNode node2 = (PKIXPolicyNode) nodes2.get(k4);
                        if (!node2.hasChildren()) {
                            validPolicyTree5 = CertPathValidatorUtilities.removePolicyNode(validPolicyTree5, listArr, node2);
                        }
                    }
                    j2--;
                    validPolicyTree4 = validPolicyTree5;
                }
            }
            return validPolicyTree4;
        }
    }
}
