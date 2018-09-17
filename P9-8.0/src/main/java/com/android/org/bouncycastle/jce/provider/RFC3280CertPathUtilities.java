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
import com.android.org.bouncycastle.jcajce.PKIXCertStoreSelector.Builder;
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
    protected static final String[] crlReasons = new String[]{"unspecified", "keyCompromise", "cACompromise", "affiliationChanged", "superseded", "cessationOfOperation", "certificateHold", "unknown", "removeFromCRL", "privilegeWithdrawn", "aACompromise"};

    RFC3280CertPathUtilities() {
    }

    protected static void processCRLB2(DistributionPoint dp, Object cert, X509CRL crl) throws AnnotatedException {
        try {
            IssuingDistributionPoint idp = IssuingDistributionPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(crl, ISSUING_DISTRIBUTION_POINT));
            if (idp != null) {
                if (idp.getDistributionPoint() != null) {
                    GeneralName[] genNames;
                    int j;
                    ASN1EncodableVector vec;
                    Enumeration e;
                    DistributionPointName dpName = IssuingDistributionPoint.getInstance(idp).getDistributionPoint();
                    List names = new ArrayList();
                    if (dpName.getType() == 0) {
                        genNames = GeneralNames.getInstance(dpName.getName()).getNames();
                        for (Object add : genNames) {
                            names.add(add);
                        }
                    }
                    if (dpName.getType() == 1) {
                        vec = new ASN1EncodableVector();
                        try {
                            e = ASN1Sequence.getInstance(PrincipalUtils.getIssuerPrincipal(crl)).getObjects();
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
                        dpName = dp.getDistributionPoint();
                        genNames = null;
                        if (dpName.getType() == 0) {
                            genNames = GeneralNames.getInstance(dpName.getName()).getNames();
                        }
                        if (dpName.getType() == 1) {
                            if (dp.getCRLIssuer() != null) {
                                genNames = dp.getCRLIssuer().getNames();
                            } else {
                                genNames = new GeneralName[1];
                                try {
                                    genNames[0] = new GeneralName(X500Name.getInstance(PrincipalUtils.getEncodedIssuerPrincipal(cert).getEncoded()));
                                } catch (Exception e22) {
                                    throw new AnnotatedException("Could not read certificate issuer.", e22);
                                }
                            }
                            for (j = 0; j < genNames.length; j++) {
                                e = ASN1Sequence.getInstance(genNames[j].getName().toASN1Primitive()).getObjects();
                                vec = new ASN1EncodableVector();
                                while (e.hasMoreElements()) {
                                    vec.add((ASN1Encodable) e.nextElement());
                                }
                                vec.add(dpName.getName());
                                genNames[j] = new GeneralName(X500Name.getInstance(new DERSequence(vec)));
                            }
                        }
                        if (genNames != null) {
                            for (Object add2 : genNames) {
                                if (names.contains(add2)) {
                                    matches = true;
                                    break;
                                }
                            }
                        }
                        if (!matches) {
                            throw new AnnotatedException("No match for certificate CRL issuing distribution point name to cRLIssuer CRL distribution point.");
                        }
                    } else if (dp.getCRLIssuer() == null) {
                        throw new AnnotatedException("Either the cRLIssuer or the distributionPoint field must be contained in DistributionPoint.");
                    } else {
                        genNames = dp.getCRLIssuer().getNames();
                        for (Object add22 : genNames) {
                            if (names.contains(add22)) {
                                matches = true;
                                break;
                            }
                        }
                        if (!matches) {
                            throw new AnnotatedException("No match for certificate CRL issuing distribution point name to cRLIssuer CRL distribution point.");
                        }
                    }
                }
                try {
                    BasicConstraints bc = BasicConstraints.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Extension) cert, BASIC_CONSTRAINTS));
                    if (cert instanceof X509Certificate) {
                        if (idp.onlyContainsUserCerts() && bc != null && bc.isCA()) {
                            throw new AnnotatedException("CA Cert CRL only contains user certificates.");
                        } else if (idp.onlyContainsCACerts() && (bc == null || (bc.isCA() ^ 1) != 0)) {
                            throw new AnnotatedException("End CRL only contains CA certificates.");
                        }
                    }
                    if (idp.onlyContainsAttributeCerts()) {
                        throw new AnnotatedException("onlyContainsAttributeCerts boolean is asserted.");
                    }
                } catch (Exception e222) {
                    throw new AnnotatedException("Basic constraints extension could not be decoded.", e222);
                }
            }
        } catch (Exception e2222) {
            throw new AnnotatedException("Issuing distribution point extension could not be decoded.", e2222);
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
                if (matchIssuer && (isIndirect ^ 1) != 0) {
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
        try {
            IssuingDistributionPoint idp = IssuingDistributionPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(crl, ISSUING_DISTRIBUTION_POINT));
            if (idp != null && idp.getOnlySomeReasons() != null && dp.getReasons() != null) {
                return new ReasonsMask(dp.getReasons()).intersect(new ReasonsMask(idp.getOnlySomeReasons()));
            }
            if ((idp == null || idp.getOnlySomeReasons() == null) && dp.getReasons() == null) {
                return ReasonsMask.allReasons;
            }
            ReasonsMask reasonsMask;
            ReasonsMask reasonsMask2;
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
        X509CertSelector certSelector = new X509CertSelector();
        try {
            certSelector.setSubject(PrincipalUtils.getIssuerPrincipal(crl).getEncoded());
            PKIXCertStoreSelector selector = new Builder(certSelector).build();
            try {
                Collection<X509Certificate> coll = CertPathValidatorUtilities.findCertificates(selector, paramsPKIX.getCertificateStores());
                coll.addAll(CertPathValidatorUtilities.findCertificates(selector, paramsPKIX.getCertStores()));
                coll.add(defaultCRLSignCert);
                List validCerts = new ArrayList();
                List validKeys = new ArrayList();
                for (X509Certificate signingCert : coll) {
                    if (signingCert.equals(defaultCRLSignCert)) {
                        validCerts.add(signingCert);
                        validKeys.add(defaultCRLSignKey);
                    } else {
                        try {
                            PKIXCertPathBuilderSpi builder = new PKIXCertPathBuilderSpi();
                            X509CertSelector tmpCertSelector = new X509CertSelector();
                            tmpCertSelector.setCertificate(signingCert);
                            PKIXExtendedParameters.Builder paramsBuilder = new PKIXExtendedParameters.Builder(paramsPKIX).setTargetConstraints(new Builder(tmpCertSelector).build());
                            if (certPathCerts.contains(signingCert)) {
                                paramsBuilder.setRevocationEnabled(false);
                            } else {
                                paramsBuilder.setRevocationEnabled(true);
                            }
                            List certs = builder.engineBuild(new PKIXExtendedBuilderParameters.Builder(paramsBuilder.build()).build()).getCertPath().getCertificates();
                            validCerts.add(signingCert);
                            validKeys.add(CertPathValidatorUtilities.getNextWorkingKey(certs, 0, helper));
                        } catch (CertPathBuilderException e) {
                            throw new AnnotatedException("CertPath for CRL signer failed to validate.", e);
                        } catch (CertPathValidatorException e2) {
                            throw new AnnotatedException("Public key of issuer certificate of CRL could not be retrieved.", e2);
                        } catch (Exception e3) {
                            throw new AnnotatedException(e3.getMessage());
                        }
                    }
                }
                Set checkKeys = new HashSet();
                AnnotatedException lastException = null;
                for (int i = 0; i < validCerts.size(); i++) {
                    boolean[] keyusage = ((X509Certificate) validCerts.get(i)).getKeyUsage();
                    if (keyusage == null || (keyusage.length >= 7 && (keyusage[6] ^ 1) == 0)) {
                        checkKeys.add(validKeys.get(i));
                    } else {
                        AnnotatedException annotatedException = new AnnotatedException("Issuer certificate key usage extension does not permit CRL signing.");
                    }
                }
                if (checkKeys.isEmpty() && lastException == null) {
                    throw new AnnotatedException("Cannot find a valid issuer certificate.");
                } else if (!checkKeys.isEmpty() || lastException == null) {
                    return checkKeys;
                } else {
                    throw lastException;
                }
            } catch (AnnotatedException e4) {
                throw new AnnotatedException("Issuer certificate for CRL cannot be searched.", e4);
            }
        } catch (IOException e5) {
            throw new AnnotatedException("Subject criteria for certificate selector to find issuer certificate for CRL could not be set.", e5);
        }
    }

    protected static PublicKey processCRLG(X509CRL crl, Set keys) throws AnnotatedException {
        Throwable lastException = null;
        for (PublicKey key : keys) {
            try {
                crl.verify(key);
                return key;
            } catch (Throwable e) {
                lastException = e;
            }
        }
        throw new AnnotatedException("Cannot verify CRL.", lastException);
    }

    protected static X509CRL processCRLH(Set deltacrls, PublicKey key) throws AnnotatedException {
        Throwable lastException = null;
        for (X509CRL crl : deltacrls) {
            try {
                crl.verify(key);
                return crl;
            } catch (Throwable e) {
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
                    } catch (AnnotatedException e22) {
                        throw new AnnotatedException("No new delta CRL locations could be added from Freshest CRL extension.", e22);
                    }
                }
            } catch (AnnotatedException e222) {
                throw new AnnotatedException("Freshest CRL extension could not be decoded from certificate.", e222);
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
                            }
                            throw new AnnotatedException("Issuing distribution point extension from delta CRL and complete CRL does not match.");
                        } catch (Exception e3) {
                            throw new AnnotatedException("Issuing distribution point extension from delta CRL could not be decoded.", e3);
                        }
                    }
                    throw new AnnotatedException("Complete CRL issuer does not match delta CRL issuer.");
                }
            } catch (Exception e32) {
                throw new AnnotatedException("Issuing distribution point extension could not be decoded.", e32);
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
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate) certs.get(index);
        int i = certs.size() - index;
        try {
            ASN1Sequence pm = ASN1Sequence.getInstance(CertPathValidatorUtilities.getExtensionValue(cert, POLICY_MAPPINGS));
            PKIXPolicyNode _validPolicyTree = validPolicyTree;
            if (pm != null) {
                String id_p;
                ASN1Sequence mappings = pm;
                Map m_idp = new HashMap();
                Set<String> s_idp = new HashSet();
                for (int j = 0; j < pm.size(); j++) {
                    ASN1Sequence mapping = (ASN1Sequence) pm.getObjectAt(j);
                    id_p = ((ASN1ObjectIdentifier) mapping.getObjectAt(0)).getId();
                    String sd_p = ((ASN1ObjectIdentifier) mapping.getObjectAt(1)).getId();
                    if (m_idp.containsKey(id_p)) {
                        ((Set) m_idp.get(id_p)).add(sd_p);
                    } else {
                        Set tmp = new HashSet();
                        tmp.add(sd_p);
                        m_idp.put(id_p, tmp);
                        s_idp.add(id_p);
                    }
                }
                for (String id_p2 : s_idp) {
                    Iterator nodes_i;
                    PKIXPolicyNode node;
                    if (policyMapping > 0) {
                        boolean idp_found = false;
                        for (PKIXPolicyNode node2 : policyNodes[i]) {
                            if (node2.getValidPolicy().equals(id_p2)) {
                                idp_found = true;
                                node2.expectedPolicies = (Set) m_idp.get(id_p2);
                                break;
                            }
                        }
                        if (idp_found) {
                            continue;
                        } else {
                            for (PKIXPolicyNode node22 : policyNodes[i]) {
                                if (ANY_POLICY.equals(node22.getValidPolicy())) {
                                    Set pq = null;
                                    try {
                                        Enumeration e = ((ASN1Sequence) CertPathValidatorUtilities.getExtensionValue(cert, CERTIFICATE_POLICIES)).getObjects();
                                        while (e.hasMoreElements()) {
                                            try {
                                                PolicyInformation pinfo = PolicyInformation.getInstance(e.nextElement());
                                                if (ANY_POLICY.equals(pinfo.getPolicyIdentifier().getId())) {
                                                    try {
                                                        pq = CertPathValidatorUtilities.getQualifierSet(pinfo.getPolicyQualifiers());
                                                        break;
                                                    } catch (Throwable ex) {
                                                        throw new ExtCertPathValidatorException("Policy qualifier info set could not be decoded.", ex, certPath, index);
                                                    }
                                                }
                                            } catch (Throwable ex2) {
                                                throw new CertPathValidatorException("Policy information could not be decoded.", ex2, certPath, index);
                                            }
                                        }
                                        boolean ci = false;
                                        if (cert.getCriticalExtensionOIDs() != null) {
                                            ci = cert.getCriticalExtensionOIDs().contains(CERTIFICATE_POLICIES);
                                        }
                                        PKIXPolicyNode p_node = (PKIXPolicyNode) node22.getParent();
                                        if (ANY_POLICY.equals(p_node.getValidPolicy())) {
                                            PKIXPolicyNode c_node = new PKIXPolicyNode(new ArrayList(), i, (Set) m_idp.get(id_p2), p_node, pq, id_p2, ci);
                                            p_node.addChild(c_node);
                                            policyNodes[i].add(c_node);
                                        }
                                    } catch (AnnotatedException e2) {
                                        throw new ExtCertPathValidatorException("Certificate policies extension could not be decoded.", e2, certPath, index);
                                    }
                                }
                            }
                            continue;
                        }
                    } else if (policyMapping <= 0) {
                        nodes_i = policyNodes[i].iterator();
                        while (nodes_i.hasNext()) {
                            node22 = (PKIXPolicyNode) nodes_i.next();
                            if (node22.getValidPolicy().equals(id_p2)) {
                                ((PKIXPolicyNode) node22.getParent()).removeChild(node22);
                                nodes_i.remove();
                                for (int k = i - 1; k >= 0; k--) {
                                    List nodes = policyNodes[k];
                                    for (int l = 0; l < nodes.size(); l++) {
                                        PKIXPolicyNode node23 = (PKIXPolicyNode) nodes.get(l);
                                        if (!node23.hasChildren()) {
                                            _validPolicyTree = CertPathValidatorUtilities.removePolicyNode(_validPolicyTree, policyNodes, node23);
                                            if (_validPolicyTree == null) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return _validPolicyTree;
        } catch (Throwable ex3) {
            throw new ExtCertPathValidatorException("Policy mappings extension could not be decoded.", ex3, certPath, index);
        }
    }

    protected static void prepareNextCertA(CertPath certPath, int index) throws CertPathValidatorException {
        try {
            ASN1Sequence pm = ASN1Sequence.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), POLICY_MAPPINGS));
            if (pm != null) {
                ASN1Sequence mappings = pm;
                int j = 0;
                while (j < pm.size()) {
                    try {
                        ASN1Sequence mapping = ASN1Sequence.getInstance(pm.getObjectAt(j));
                        ASN1ObjectIdentifier issuerDomainPolicy = ASN1ObjectIdentifier.getInstance(mapping.getObjectAt(0));
                        ASN1ObjectIdentifier subjectDomainPolicy = ASN1ObjectIdentifier.getInstance(mapping.getObjectAt(1));
                        if (ANY_POLICY.equals(issuerDomainPolicy.getId())) {
                            throw new CertPathValidatorException("IssuerDomainPolicy is anyPolicy", null, certPath, index);
                        } else if (ANY_POLICY.equals(subjectDomainPolicy.getId())) {
                            throw new CertPathValidatorException("SubjectDomainPolicy is anyPolicy,", null, certPath, index);
                        } else {
                            j++;
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
            if (ASN1Sequence.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), CERTIFICATE_POLICIES)) == null) {
                return null;
            }
            return validPolicyTree;
        } catch (AnnotatedException e) {
            throw new ExtCertPathValidatorException("Could not read certificate policies extension from certificate.", e, certPath, index);
        }
    }

    protected static void processCertBC(CertPath certPath, int index, PKIXNameConstraintValidator nameConstraintValidator) throws CertPathValidatorException {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate) certs.get(index);
        int n = certs.size();
        Object obj = (!CertPathValidatorUtilities.isSelfIssued(cert) || n - index >= n) ? null : 1;
        if (obj == null) {
            try {
                ASN1Sequence dns = ASN1Sequence.getInstance(PrincipalUtils.getSubjectPrincipal(cert).getEncoded());
                try {
                    nameConstraintValidator.checkPermittedDN(dns);
                    nameConstraintValidator.checkExcludedDN(dns);
                    try {
                        GeneralNames altName = GeneralNames.getInstance(CertPathValidatorUtilities.getExtensionValue(cert, SUBJECT_ALTERNATIVE_NAME));
                        RDN[] emails = X500Name.getInstance(dns).getRDNs(BCStyle.EmailAddress);
                        int eI = 0;
                        while (eI != emails.length) {
                            GeneralName emailAsGeneralName = new GeneralName(1, ((ASN1String) emails[eI].getFirst().getValue()).getString());
                            try {
                                nameConstraintValidator.checkPermitted(emailAsGeneralName);
                                nameConstraintValidator.checkExcluded(emailAsGeneralName);
                                eI++;
                            } catch (PKIXNameConstraintValidatorException ex) {
                                throw new CertPathValidatorException("Subtree check for certificate subject alternative email failed.", ex, certPath, index);
                            }
                        }
                        if (altName != null) {
                            try {
                                GeneralName[] genNames = altName.getNames();
                                int j = 0;
                                while (j < genNames.length) {
                                    try {
                                        nameConstraintValidator.checkPermitted(genNames[j]);
                                        nameConstraintValidator.checkExcluded(genNames[j]);
                                        j++;
                                    } catch (PKIXNameConstraintValidatorException e) {
                                        throw new CertPathValidatorException("Subtree check for certificate subject alternative name failed.", e, certPath, index);
                                    }
                                }
                            } catch (Exception e2) {
                                throw new CertPathValidatorException("Subject alternative name contents could not be decoded.", e2, certPath, index);
                            }
                        }
                    } catch (Exception e22) {
                        throw new CertPathValidatorException("Subject alternative name extension could not be decoded.", e22, certPath, index);
                    }
                } catch (PKIXNameConstraintValidatorException e3) {
                    throw new CertPathValidatorException("Subtree check for certificate subject failed.", e3, certPath, index);
                }
            } catch (Exception e222) {
                throw new CertPathValidatorException("Exception extracting subject name when checking subtrees.", e222, certPath, index);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x00c8  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x019f  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x01d3  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected static PKIXPolicyNode processCertD(CertPath certPath, int index, Set acceptablePolicies, PKIXPolicyNode validPolicyTree, List[] policyNodes, int inhibitAnyPolicy) throws CertPathValidatorException {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate) certs.get(index);
        int n = certs.size();
        int i = n - index;
        try {
            ASN1Sequence certPolicies = ASN1Sequence.getInstance(CertPathValidatorUtilities.getExtensionValue(cert, CERTIFICATE_POLICIES));
            if (certPolicies == null || validPolicyTree == null) {
                return null;
            }
            PolicyInformation pInfo;
            PKIXPolicyNode _validPolicyTree;
            int j;
            Set criticalExtensionOids;
            Enumeration e = certPolicies.getObjects();
            Set pols = new HashSet();
            while (e.hasMoreElements()) {
                pInfo = PolicyInformation.getInstance(e.nextElement());
                ASN1ObjectIdentifier pOid = pInfo.getPolicyIdentifier();
                pols.add(pOid.getId());
                if (!ANY_POLICY.equals(pOid.getId())) {
                    try {
                        Set pq = CertPathValidatorUtilities.getQualifierSet(pInfo.getPolicyQualifiers());
                        if (!CertPathValidatorUtilities.processCertD1i(i, policyNodes, pOid, pq)) {
                            CertPathValidatorUtilities.processCertD1ii(i, policyNodes, pOid, pq);
                        }
                    } catch (Throwable ex) {
                        throw new ExtCertPathValidatorException("Policy qualifier info set could not be build.", ex, certPath, index);
                    }
                }
            }
            if (!acceptablePolicies.isEmpty()) {
                if (!acceptablePolicies.contains(ANY_POLICY)) {
                    int k;
                    List nodes;
                    Set t1 = new HashSet();
                    for (Object o : acceptablePolicies) {
                        if (pols.contains(o)) {
                            t1.add(o);
                        }
                    }
                    acceptablePolicies.clear();
                    acceptablePolicies.addAll(t1);
                    if (inhibitAnyPolicy > 0 || (i < n && CertPathValidatorUtilities.isSelfIssued(cert))) {
                        e = certPolicies.getObjects();
                        while (e.hasMoreElements()) {
                            pInfo = PolicyInformation.getInstance(e.nextElement());
                            if (ANY_POLICY.equals(pInfo.getPolicyIdentifier().getId())) {
                                Set _apq = CertPathValidatorUtilities.getQualifierSet(pInfo.getPolicyQualifiers());
                                List _nodes = policyNodes[i - 1];
                                for (k = 0; k < _nodes.size(); k++) {
                                    PKIXPolicyNode _node = (PKIXPolicyNode) _nodes.get(k);
                                    for (String _tmp : _node.getExpectedPolicies()) {
                                        String _policy;
                                        if (_tmp instanceof String) {
                                            _policy = _tmp;
                                        } else if (_tmp instanceof ASN1ObjectIdentifier) {
                                            _policy = ((ASN1ObjectIdentifier) _tmp).getId();
                                        }
                                        boolean _found = false;
                                        Iterator _childrenIter = _node.getChildren();
                                        while (_childrenIter.hasNext()) {
                                            if (_policy.equals(((PKIXPolicyNode) _childrenIter.next()).getValidPolicy())) {
                                                _found = true;
                                            }
                                        }
                                        if (!_found) {
                                            Set _newChildExpectedPolicies = new HashSet();
                                            _newChildExpectedPolicies.add(_policy);
                                            PKIXPolicyNode _newChild = new PKIXPolicyNode(new ArrayList(), i, _newChildExpectedPolicies, _node, _apq, _policy, false);
                                            _node.addChild(_newChild);
                                            policyNodes[i].add(_newChild);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    _validPolicyTree = validPolicyTree;
                    for (j = i - 1; j >= 0; j--) {
                        nodes = policyNodes[j];
                        for (k = 0; k < nodes.size(); k++) {
                            PKIXPolicyNode node = (PKIXPolicyNode) nodes.get(k);
                            if (!node.hasChildren()) {
                                _validPolicyTree = CertPathValidatorUtilities.removePolicyNode(_validPolicyTree, policyNodes, node);
                                if (_validPolicyTree == null) {
                                    break;
                                }
                            }
                        }
                    }
                    criticalExtensionOids = cert.getCriticalExtensionOIDs();
                    if (criticalExtensionOids != null) {
                        boolean critical = criticalExtensionOids.contains(CERTIFICATE_POLICIES);
                        nodes = policyNodes[i];
                        for (j = 0; j < nodes.size(); j++) {
                            ((PKIXPolicyNode) nodes.get(j)).setCritical(critical);
                        }
                    }
                    return _validPolicyTree;
                }
            }
            acceptablePolicies.clear();
            acceptablePolicies.addAll(pols);
            e = certPolicies.getObjects();
            while (e.hasMoreElements()) {
            }
            _validPolicyTree = validPolicyTree;
            while (j >= 0) {
            }
            criticalExtensionOids = cert.getCriticalExtensionOIDs();
            if (criticalExtensionOids != null) {
            }
            return _validPolicyTree;
        } catch (Throwable e2) {
            throw new ExtCertPathValidatorException("Could not read certificate policies extension from certificate.", e2, certPath, index);
        }
    }

    protected static void processCertA(CertPath certPath, PKIXExtendedParameters paramsPKIX, int index, PublicKey workingPublicKey, boolean verificationAlreadyPerformed, X500Name workingIssuerName, X509Certificate sign, JcaJceHelper helper) throws ExtCertPathValidatorException {
        List certs = certPath.getCertificates();
        X509Certificate cert = (X509Certificate) certs.get(index);
        if (!verificationAlreadyPerformed) {
            try {
                CertPathValidatorUtilities.verifyX509Certificate(cert, workingPublicKey, paramsPKIX.getSigProvider());
            } catch (GeneralSecurityException e) {
                throw new ExtCertPathValidatorException("Could not validate certificate signature.", e, certPath, index);
            }
        }
        try {
            cert.checkValidity(CertPathValidatorUtilities.getValidCertDateFromValidityModel(paramsPKIX, certPath, index));
            if (paramsPKIX.isRevocationEnabled()) {
                try {
                    checkCRLs(paramsPKIX, cert, CertPathValidatorUtilities.getValidCertDateFromValidityModel(paramsPKIX, certPath, index), sign, workingPublicKey, certs, helper);
                } catch (Throwable e2) {
                    Throwable cause = e2;
                    if (e2.getCause() != null) {
                        cause = e2.getCause();
                    }
                    throw new ExtCertPathValidatorException(e2.getMessage(), cause, certPath, index);
                }
            }
            if (!PrincipalUtils.getEncodedIssuerPrincipal(cert).equals(workingIssuerName)) {
                throw new ExtCertPathValidatorException("IssuerName(" + PrincipalUtils.getEncodedIssuerPrincipal(cert) + ") does not match SubjectName(" + workingIssuerName + ") of signing certificate.", null, certPath, index);
            }
        } catch (CertificateExpiredException e3) {
            throw new ExtCertPathValidatorException("Could not validate certificate: " + e3.getMessage(), e3, certPath, index);
        } catch (CertificateNotYetValidException e4) {
            throw new ExtCertPathValidatorException("Could not validate certificate: " + e4.getMessage(), e4, certPath, index);
        } catch (AnnotatedException e5) {
            throw new ExtCertPathValidatorException("Could not validate time of certificate.", e5, certPath, index);
        }
    }

    protected static int prepareNextCertI1(CertPath certPath, int index, int explicitPolicy) throws CertPathValidatorException {
        try {
            ASN1Sequence pc = ASN1Sequence.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), POLICY_CONSTRAINTS));
            if (pc != null) {
                Enumeration policyConstraints = pc.getObjects();
                while (policyConstraints.hasMoreElements()) {
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
            ASN1Sequence pc = ASN1Sequence.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), POLICY_CONSTRAINTS));
            if (pc != null) {
                Enumeration policyConstraints = pc.getObjects();
                while (policyConstraints.hasMoreElements()) {
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
            ASN1Sequence ncSeq = ASN1Sequence.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), NAME_CONSTRAINTS));
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
        Date date = new Date(System.currentTimeMillis());
        if (validDate.getTime() > date.getTime()) {
            throw new AnnotatedException("Validation time is in future.");
        }
        boolean validCrlFound = false;
        AnnotatedException lastException = null;
        Iterator crl_iter = CertPathValidatorUtilities.getCompleteCRLs(dp, cert, date, paramsPKIX).iterator();
        while (crl_iter.hasNext() && certStatus.getCertStatus() == 11 && (reasonMask.isAllReasons() ^ 1) != 0) {
            try {
                X509CRL crl = (X509CRL) crl_iter.next();
                ReasonsMask interimReasonsMask = processCRLD(crl, dp);
                if (interimReasonsMask.hasNewReasons(reasonMask)) {
                    PublicKey key = processCRLG(crl, processCRLF(crl, cert, defaultCRLSignCert, defaultCRLSignKey, paramsPKIX, certPathCerts, helper));
                    X509CRL deltaCRL = null;
                    Date validityDate = date;
                    if (paramsPKIX.getDate() != null) {
                        validityDate = paramsPKIX.getDate();
                    }
                    if (paramsPKIX.isUseDeltasEnabled()) {
                        deltaCRL = processCRLH(CertPathValidatorUtilities.getDeltaCRLs(validityDate, crl, paramsPKIX.getCertStores(), paramsPKIX.getCRLStores()), key);
                    }
                    if (paramsPKIX.getValidityModel() == 1 || cert.getNotAfter().getTime() >= crl.getThisUpdate().getTime()) {
                        Set criticalExtensions;
                        processCRLB1(dp, cert, crl);
                        processCRLB2(dp, cert, crl);
                        processCRLC(deltaCRL, crl, paramsPKIX);
                        processCRLI(validDate, deltaCRL, cert, certStatus, paramsPKIX);
                        processCRLJ(validDate, crl, cert, certStatus);
                        if (certStatus.getCertStatus() == 8) {
                            certStatus.setCertStatus(11);
                        }
                        reasonMask.addReasons(interimReasonsMask);
                        Set criticalExtensions2 = crl.getCriticalExtensionOIDs();
                        if (criticalExtensions2 != null) {
                            criticalExtensions = new HashSet(criticalExtensions2);
                            criticalExtensions.remove(Extension.issuingDistributionPoint.getId());
                            criticalExtensions.remove(Extension.deltaCRLIndicator.getId());
                            if (criticalExtensions.isEmpty()) {
                                criticalExtensions2 = criticalExtensions;
                            } else {
                                throw new AnnotatedException("CRL contains unsupported critical extensions.");
                            }
                        }
                        if (deltaCRL != null) {
                            criticalExtensions2 = deltaCRL.getCriticalExtensionOIDs();
                            if (criticalExtensions2 != null) {
                                criticalExtensions = new HashSet(criticalExtensions2);
                                criticalExtensions.remove(Extension.issuingDistributionPoint.getId());
                                criticalExtensions.remove(Extension.deltaCRLIndicator.getId());
                                if (criticalExtensions.isEmpty()) {
                                    criticalExtensions2 = criticalExtensions;
                                } else {
                                    throw new AnnotatedException("Delta CRL contains unsupported critical extension.");
                                }
                            }
                        }
                        validCrlFound = true;
                    } else {
                        throw new AnnotatedException("No valid CRL for current time found.");
                    }
                }
                continue;
            } catch (AnnotatedException e) {
                lastException = e;
            }
        }
        if (!validCrlFound) {
            throw lastException;
        }
    }

    protected static void checkCRLs(PKIXExtendedParameters paramsPKIX, X509Certificate cert, Date validDate, X509Certificate sign, PublicKey workingPublicKey, List certPathCerts, JcaJceHelper helper) throws AnnotatedException {
        Throwable lastException = null;
        try {
            CRLDistPoint crldp = CRLDistPoint.getInstance(CertPathValidatorUtilities.getExtensionValue(cert, CRL_DISTRIBUTION_POINTS));
            PKIXExtendedParameters.Builder builder = new PKIXExtendedParameters.Builder(paramsPKIX);
            try {
                for (PKIXCRLStore addCRLStore : CertPathValidatorUtilities.getAdditionalStoresFromCRLDistributionPoint(crldp, paramsPKIX.getNamedCRLStoreMap())) {
                    builder.addCRLStore(addCRLStore);
                }
                CertStatus certStatus = new CertStatus();
                ReasonsMask reasonsMask = new ReasonsMask();
                PKIXExtendedParameters finalParams = builder.build();
                boolean validCrlFound = false;
                if (crldp != null) {
                    try {
                        DistributionPoint[] dps = crldp.getDistributionPoints();
                        if (dps != null) {
                            for (int i = 0; i < dps.length && certStatus.getCertStatus() == 11 && (reasonsMask.isAllReasons() ^ 1) != 0; i++) {
                                try {
                                    checkCRL(dps[i], finalParams, cert, validDate, sign, workingPublicKey, certStatus, reasonsMask, certPathCerts, helper);
                                    validCrlFound = true;
                                } catch (Throwable e) {
                                    lastException = e;
                                }
                            }
                        }
                    } catch (Throwable e2) {
                        throw new AnnotatedException("Distribution points could not be read.", e2);
                    }
                }
                if (certStatus.getCertStatus() == 11 && (reasonsMask.isAllReasons() ^ 1) != 0) {
                    try {
                        checkCRL(new DistributionPoint(new DistributionPointName(0, new GeneralNames(new GeneralName(4, (ASN1Encodable) new ASN1InputStream(PrincipalUtils.getEncodedIssuerPrincipal(cert).getEncoded()).readObject()))), null, null), (PKIXExtendedParameters) paramsPKIX.clone(), cert, validDate, sign, workingPublicKey, certStatus, reasonsMask, certPathCerts, helper);
                        validCrlFound = true;
                    } catch (Throwable e22) {
                        throw new AnnotatedException("Issuer from certificate for CRL could not be reencoded.", e22);
                    } catch (Throwable e3) {
                        lastException = e3;
                    }
                }
                if (validCrlFound) {
                    if (certStatus.getCertStatus() != 11) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        throw new AnnotatedException(("Certificate revocation after " + simpleDateFormat.format(certStatus.getRevocationDate())) + ", reason: " + crlReasons[certStatus.getCertStatus()]);
                    }
                    if (!reasonsMask.isAllReasons() && certStatus.getCertStatus() == 11) {
                        certStatus.setCertStatus(12);
                    }
                    if (certStatus.getCertStatus() == 12) {
                        throw new AnnotatedException("Certificate status could not be determined.");
                    }
                } else if (lastException instanceof AnnotatedException) {
                    throw lastException;
                } else {
                    throw new AnnotatedException("No valid CRL found.", lastException);
                }
            } catch (Throwable e32) {
                throw new AnnotatedException("No additional CRL locations could be decoded from CRL distribution point extension.", e32);
            }
        } catch (Throwable e222) {
            throw new AnnotatedException("CRL distribution point extension could not be read.", e222);
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
        if (_usage != null && (_usage[5] ^ 1) != 0) {
            throw new ExtCertPathValidatorException("Issuer certificate keyusage extension is critical and does not permit key signing.", null, certPath, index);
        }
    }

    protected static void prepareNextCertO(CertPath certPath, int index, Set criticalExtensions, List pathCheckers) throws CertPathValidatorException {
        X509Certificate cert = (X509Certificate) certPath.getCertificates().get(index);
        for (PKIXCertPathChecker check : pathCheckers) {
            try {
                check.check(cert, criticalExtensions);
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
            ASN1Sequence pc = ASN1Sequence.getInstance(CertPathValidatorUtilities.getExtensionValue((X509Certificate) certPath.getCertificates().get(index), POLICY_CONSTRAINTS));
            if (pc != null) {
                Enumeration policyConstraints = pc.getObjects();
                while (policyConstraints.hasMoreElements()) {
                    ASN1TaggedObject constraint = (ASN1TaggedObject) policyConstraints.nextElement();
                    switch (constraint.getTagNo()) {
                        case 0:
                            try {
                                if (ASN1Integer.getInstance(constraint, false).getValue().intValue() != 0) {
                                    break;
                                }
                                return 0;
                            } catch (Exception e) {
                                throw new ExtCertPathValidatorException("Policy constraints requireExplicitPolicy field could not be decoded.", e, certPath, index);
                            }
                        default:
                            break;
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
        for (PKIXCertPathChecker check : pathCheckers) {
            try {
                check.check(cert, criticalExtensions);
            } catch (CertPathValidatorException e) {
                throw new ExtCertPathValidatorException("Additional certificate path checker failed.", e, certPath, index);
            }
        }
        if (!criticalExtensions.isEmpty()) {
            throw new ExtCertPathValidatorException("Certificate has unsupported critical extension: " + criticalExtensions, null, certPath, index);
        }
    }

    protected static PKIXPolicyNode wrapupCertG(CertPath certPath, PKIXExtendedParameters paramsPKIX, Set userInitialPolicySet, int index, List[] policyNodes, PKIXPolicyNode validPolicyTree, Set acceptablePolicies) throws CertPathValidatorException {
        int n = certPath.getCertificates().size();
        Set<PKIXPolicyNode> _validPolicyNodeSet;
        int j;
        int k;
        PKIXPolicyNode _node;
        Iterator _iter;
        List nodes;
        PKIXPolicyNode node;
        if (validPolicyTree == null) {
            if (!paramsPKIX.isExplicitPolicyRequired()) {
                return null;
            }
            throw new ExtCertPathValidatorException("Explicit policy requested but none available.", null, certPath, index);
        } else if (CertPathValidatorUtilities.isAnyPolicy(userInitialPolicySet)) {
            if (paramsPKIX.isExplicitPolicyRequired()) {
                if (acceptablePolicies.isEmpty()) {
                    throw new ExtCertPathValidatorException("Explicit policy requested but none available.", null, certPath, index);
                }
                _validPolicyNodeSet = new HashSet();
                for (List _nodeDepth : policyNodes) {
                    for (k = 0; k < _nodeDepth.size(); k++) {
                        _node = (PKIXPolicyNode) _nodeDepth.get(k);
                        if (ANY_POLICY.equals(_node.getValidPolicy())) {
                            _iter = _node.getChildren();
                            while (_iter.hasNext()) {
                                _validPolicyNodeSet.add(_iter.next());
                            }
                        }
                    }
                }
                for (PKIXPolicyNode _node2 : _validPolicyNodeSet) {
                    boolean contains = acceptablePolicies.contains(_node2.getValidPolicy());
                }
                if (validPolicyTree != null) {
                    for (j = n - 1; j >= 0; j--) {
                        nodes = policyNodes[j];
                        for (k = 0; k < nodes.size(); k++) {
                            node = (PKIXPolicyNode) nodes.get(k);
                            if (!node.hasChildren()) {
                                validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, node);
                            }
                        }
                    }
                }
            }
            return validPolicyTree;
        } else {
            _validPolicyNodeSet = new HashSet();
            for (List _nodeDepth2 : policyNodes) {
                for (k = 0; k < _nodeDepth2.size(); k++) {
                    _node2 = (PKIXPolicyNode) _nodeDepth2.get(k);
                    if (ANY_POLICY.equals(_node2.getValidPolicy())) {
                        _iter = _node2.getChildren();
                        while (_iter.hasNext()) {
                            PKIXPolicyNode _c_node = (PKIXPolicyNode) _iter.next();
                            if (!ANY_POLICY.equals(_c_node.getValidPolicy())) {
                                _validPolicyNodeSet.add(_c_node);
                            }
                        }
                    }
                }
            }
            for (PKIXPolicyNode _node22 : _validPolicyNodeSet) {
                if (!userInitialPolicySet.contains(_node22.getValidPolicy())) {
                    validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, _node22);
                }
            }
            if (validPolicyTree != null) {
                for (j = n - 1; j >= 0; j--) {
                    nodes = policyNodes[j];
                    for (k = 0; k < nodes.size(); k++) {
                        node = (PKIXPolicyNode) nodes.get(k);
                        if (!node.hasChildren()) {
                            validPolicyTree = CertPathValidatorUtilities.removePolicyNode(validPolicyTree, policyNodes, node);
                        }
                    }
                }
            }
            return validPolicyTree;
        }
    }
}
