package sun.security.provider.certpath;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.provider.certpath.PKIX;
import sun.security.provider.certpath.URICertStore;
import sun.security.util.Debug;
import sun.security.x509.AuthorityKeyIdentifierExtension;
import sun.security.x509.CRLDistributionPointsExtension;
import sun.security.x509.DistributionPoint;
import sun.security.x509.DistributionPointName;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.GeneralNames;
import sun.security.x509.IssuingDistributionPointExtension;
import sun.security.x509.KeyIdentifier;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.RDN;
import sun.security.x509.ReasonFlags;
import sun.security.x509.SerialNumber;
import sun.security.x509.URIName;
import sun.security.x509.X500Name;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;

public class DistributionPointFetcher {
    private static final boolean[] ALL_REASONS = {true, true, true, true, true, true, true, true, true};
    private static final Debug debug = Debug.getInstance("certpath");

    private DistributionPointFetcher() {
    }

    public static Collection<X509CRL> getCRLs(X509CRLSelector selector, boolean signFlag, PublicKey prevKey, String provider, List<CertStore> certStores, boolean[] reasonsMask, Set<TrustAnchor> trustAnchors, Date validity) throws CertStoreException {
        return getCRLs(selector, signFlag, prevKey, null, provider, certStores, reasonsMask, trustAnchors, validity);
    }

    public static Collection<X509CRL> getCRLs(X509CRLSelector selector, boolean signFlag, PublicKey prevKey, X509Certificate prevCert, String provider, List<CertStore> certStores, boolean[] reasonsMask, Set<TrustAnchor> trustAnchors, Date validity) throws CertStoreException {
        X509Certificate cert = selector.getCertificateChecking();
        if (cert == null) {
            return Collections.emptySet();
        }
        try {
            X509CertImpl certImpl = X509CertImpl.toImpl(cert);
            if (debug != null) {
                Debug debug2 = debug;
                debug2.println("DistributionPointFetcher.getCRLs: Checking CRLDPs for " + certImpl.getSubjectX500Principal());
            }
            CRLDistributionPointsExtension ext = certImpl.getCRLDistributionPointsExtension();
            if (ext == null) {
                if (debug != null) {
                    debug.println("No CRLDP ext");
                }
                return Collections.emptySet();
            }
            List<DistributionPoint> points = ext.get(CRLDistributionPointsExtension.POINTS);
            Set<X509CRL> results = new HashSet<>();
            Iterator<DistributionPoint> t = points.iterator();
            while (true) {
                Iterator<DistributionPoint> t2 = t;
                if (!t2.hasNext()) {
                    break;
                }
                boolean[] zArr = reasonsMask;
                if (Arrays.equals(zArr, ALL_REASONS)) {
                    break;
                }
                results.addAll(getCRLs(selector, certImpl, t2.next(), zArr, signFlag, prevKey, prevCert, provider, certStores, trustAnchors, validity));
                t = t2;
            }
            if (debug != null) {
                Debug debug3 = debug;
                debug3.println("Returning " + results.size() + " CRLs");
            }
            return results;
        } catch (IOException | CertificateException e) {
            return Collections.emptySet();
        }
    }

    private static Collection<X509CRL> getCRLs(X509CRLSelector selector, X509CertImpl certImpl, DistributionPoint point, boolean[] reasonsMask, boolean signFlag, PublicKey prevKey, X509Certificate prevCert, String provider, List<CertStore> certStores, Set<TrustAnchor> trustAnchors, Date validity) throws CertStoreException {
        X509CRLSelector x509CRLSelector = selector;
        GeneralNames fullName = point.getFullName();
        if (fullName == null) {
            RDN relativeName = point.getRelativeName();
            if (relativeName == null) {
                return Collections.emptySet();
            }
            try {
                GeneralNames crlIssuers = point.getCRLIssuer();
                if (crlIssuers == null) {
                    fullName = getFullNames((X500Name) certImpl.getIssuerDN(), relativeName);
                } else if (crlIssuers.size() != 1) {
                    return Collections.emptySet();
                } else {
                    fullName = getFullNames((X500Name) crlIssuers.get(0).getName(), relativeName);
                }
            } catch (IOException e) {
                return Collections.emptySet();
            }
        }
        Collection<X509CRL> possibleCRLs = new ArrayList<>();
        Iterator<GeneralName> t = fullName.iterator();
        CertStoreException savedCSE = null;
        while (t.hasNext()) {
            try {
                GeneralName name = t.next();
                if (name.getType() == 4) {
                    try {
                        possibleCRLs.addAll(getCRLs((X500Name) name.getName(), certImpl.getIssuerX500Principal(), certStores));
                    } catch (CertStoreException e2) {
                        cse = e2;
                        savedCSE = cse;
                    }
                } else {
                    List<CertStore> list = certStores;
                    if (name.getType() == 6) {
                        X509CRL crl = getCRL((URIName) name.getName());
                        if (crl != null) {
                            possibleCRLs.add(crl);
                        }
                    }
                }
            } catch (CertStoreException e3) {
                cse = e3;
                List<CertStore> list2 = certStores;
                savedCSE = cse;
            }
        }
        List<CertStore> list3 = certStores;
        if (!possibleCRLs.isEmpty() || savedCSE == null) {
            Collection<X509CRL> crls = new ArrayList<>(2);
            for (X509CRL crl2 : possibleCRLs) {
                try {
                    x509CRLSelector.setIssuerNames(null);
                    if (x509CRLSelector.match(crl2) && verifyCRL(certImpl, point, crl2, reasonsMask, signFlag, prevKey, prevCert, provider, trustAnchors, certStores, validity)) {
                        crls.add(crl2);
                    }
                } catch (IOException | CRLException e4) {
                    if (debug != null) {
                        debug.println("Exception verifying CRL: " + e4.getMessage());
                        e4.printStackTrace();
                    }
                }
                List<CertStore> list4 = certStores;
            }
            return crls;
        }
        throw savedCSE;
    }

    private static X509CRL getCRL(URIName name) throws CertStoreException {
        URI uri = name.getURI();
        if (debug != null) {
            Debug debug2 = debug;
            debug2.println("Trying to fetch CRL from DP " + uri);
        }
        try {
            Collection<? extends CRL> crls = URICertStore.getInstance(new URICertStore.URICertStoreParameters(uri)).getCRLs(null);
            if (crls.isEmpty()) {
                return null;
            }
            return (X509CRL) crls.iterator().next();
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
            if (debug != null) {
                Debug debug3 = debug;
                debug3.println("Can't create URICertStore: " + e.getMessage());
            }
            return null;
        }
    }

    private static Collection<X509CRL> getCRLs(X500Name name, X500Principal certIssuer, List<CertStore> certStores) throws CertStoreException {
        if (debug != null) {
            Debug debug2 = debug;
            debug2.println("Trying to fetch CRL from DP " + name);
        }
        X509CRLSelector xcs = new X509CRLSelector();
        xcs.addIssuer(name.asX500Principal());
        xcs.addIssuer(certIssuer);
        Collection<X509CRL> crls = new ArrayList<>();
        CertStoreException savedCSE = null;
        for (CertStore store : certStores) {
            try {
                for (CRL crl : store.getCRLs(xcs)) {
                    crls.add((X509CRL) crl);
                }
            } catch (CertStoreException cse) {
                if (debug != null) {
                    Debug debug3 = debug;
                    debug3.println("Exception while retrieving CRLs: " + cse);
                    cse.printStackTrace();
                }
                savedCSE = new PKIX.CertStoreTypeException(store.getType(), cse);
            }
        }
        if (!crls.isEmpty() || savedCSE == null) {
            return crls;
        }
        throw savedCSE;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: boolean[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v30, resolved type: boolean[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v4, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v14, resolved type: sun.security.x509.ReasonFlags} */
    /* JADX WARNING: type inference failed for: r13v37, types: [sun.security.x509.GeneralNameInterface, java.lang.Object] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0408  */
    /* JADX WARNING: Removed duplicated region for block: B:226:0x0420  */
    /* JADX WARNING: Unknown variable types count: 1 */
    static boolean verifyCRL(X509CertImpl certImpl, DistributionPoint point, X509CRL crl, boolean[] reasonsMask, boolean signFlag, PublicKey prevKey, X509Certificate prevCert, String provider, Set<TrustAnchor> trustAnchors, List<CertStore> certStores, Date validity) throws CRLException, IOException {
        X500Name indirectCRL;
        PublicKey prevKey2;
        HashSet hashSet;
        PKIXBuilderParameters params;
        TrustAnchor temporary;
        X500Name pointCrlIssuer;
        GeneralNames pointCrlIssuers;
        X500Name certIssuer;
        GeneralNames pointNames;
        boolean match;
        PublicKey prevKey3;
        X509CertImpl x509CertImpl = certImpl;
        X509CRL x509crl = crl;
        boolean[] zArr = reasonsMask;
        X509Certificate x509Certificate = prevCert;
        String str = provider;
        if (debug != null) {
            debug.println("DistributionPointFetcher.verifyCRL: checking revocation status for\n  SN: " + Debug.toHexString(certImpl.getSerialNumber()) + "\n  Subject: " + certImpl.getSubjectX500Principal() + "\n  Issuer: " + certImpl.getIssuerX500Principal());
        }
        X500Name pointCrlIssuer2 = null;
        X509CRLImpl crlImpl = X509CRLImpl.toImpl(crl);
        IssuingDistributionPointExtension idpExt = crlImpl.getIssuingDistributionPointExtension();
        X500Name certIssuer2 = (X500Name) certImpl.getIssuerDN();
        X500Name crlIssuer = (X500Name) crlImpl.getIssuerDN();
        GeneralNames pointCrlIssuers2 = point.getCRLIssuer();
        X500Name pointCrlIssuer3 = null;
        if (pointCrlIssuers2 != null) {
            if (idpExt != null) {
                if (!((Boolean) idpExt.get(IssuingDistributionPointExtension.INDIRECT_CRL)).equals(Boolean.FALSE)) {
                    boolean match2 = false;
                    Iterator<GeneralName> t = pointCrlIssuers2.iterator();
                    while (!match2 && t.hasNext()) {
                        ? pointCrlIssuer4 = t.next().getName();
                        X500Name indirectCRL2 = pointCrlIssuer2;
                        if (crlIssuer.equals(pointCrlIssuer4)) {
                            match2 = true;
                            pointCrlIssuer3 = pointCrlIssuer4;
                        }
                        pointCrlIssuer2 = indirectCRL2;
                    }
                    indirectCRL = pointCrlIssuer2;
                    if (!match2) {
                        return false;
                    }
                    if (issues(x509CertImpl, crlImpl, str)) {
                        prevKey3 = certImpl.getPublicKey();
                    } else {
                        indirectCRL = 1;
                        prevKey3 = prevKey;
                    }
                    prevKey2 = prevKey3;
                }
            }
            return false;
        }
        indirectCRL = null;
        if (!crlIssuer.equals(certIssuer2)) {
            if (debug != null) {
                debug.println("crl issuer does not equal cert issuer.\ncrl issuer: " + crlIssuer + "\ncert issuer: " + certIssuer2);
            }
            return false;
        }
        KeyIdentifier certAKID = certImpl.getAuthKeyId();
        KeyIdentifier crlAKID = crlImpl.getAuthKeyId();
        if (certAKID == null || crlAKID == null) {
            if (issues(x509CertImpl, crlImpl, str)) {
                prevKey2 = certImpl.getPublicKey();
            }
        } else if (!certAKID.equals(crlAKID)) {
            if (issues(x509CertImpl, crlImpl, str)) {
                prevKey2 = certImpl.getPublicKey();
            } else {
                prevKey2 = prevKey;
                indirectCRL = 1;
            }
        }
        prevKey2 = prevKey;
        if (indirectCRL == null && !signFlag) {
            return false;
        }
        if (idpExt != null) {
            DistributionPointName idpPoint = (DistributionPointName) idpExt.get(IssuingDistributionPointExtension.POINT);
            if (idpPoint != null) {
                GeneralNames idpNames = idpPoint.getFullName();
                if (idpNames == null) {
                    RDN relativeName = idpPoint.getRelativeName();
                    if (relativeName == null) {
                        if (debug != null) {
                            DistributionPointName distributionPointName = idpPoint;
                            debug.println("IDP must be relative or full DN");
                        }
                        return false;
                    }
                    if (debug != null) {
                        Debug debug2 = debug;
                        StringBuilder sb = new StringBuilder();
                        GeneralNames generalNames = idpNames;
                        sb.append("IDP relativeName:");
                        sb.append((Object) relativeName);
                        debug2.println(sb.toString());
                    }
                    idpNames = getFullNames(crlIssuer, relativeName);
                } else {
                    GeneralNames generalNames2 = idpNames;
                }
                if (point.getFullName() == null && point.getRelativeName() == null) {
                    boolean match3 = false;
                    Iterator<GeneralName> t2 = pointCrlIssuers2.iterator();
                    while (!match3 && t2.hasNext()) {
                        GeneralNameInterface crlIssuerName = t2.next().getName();
                        Iterator<GeneralName> i = idpNames.iterator();
                        while (true) {
                            Iterator<GeneralName> i2 = i;
                            if (match3) {
                                match = match3;
                                break;
                            }
                            match = match3;
                            Iterator<GeneralName> i3 = i2;
                            if (!i3.hasNext()) {
                                break;
                            }
                            Iterator<GeneralName> i4 = i3;
                            match3 = crlIssuerName.equals(i3.next().getName());
                            i = i4;
                        }
                        match3 = match;
                    }
                    if (!match3) {
                        return false;
                    }
                    X500Name x500Name = certIssuer2;
                    GeneralNames generalNames3 = pointCrlIssuers2;
                    X500Name x500Name2 = pointCrlIssuer3;
                } else {
                    GeneralNames pointNames2 = point.getFullName();
                    if (pointNames2 == null) {
                        RDN relativeName2 = point.getRelativeName();
                        if (relativeName2 == null) {
                            if (debug != null) {
                                GeneralNames generalNames4 = pointNames2;
                                debug.println("DP must be relative or full DN");
                            }
                            return false;
                        }
                        if (debug != null) {
                            debug.println("DP relativeName:" + relativeName2);
                        }
                        if (indirectCRL == null) {
                            pointNames2 = getFullNames(certIssuer2, relativeName2);
                        } else if (pointCrlIssuers2.size() != 1) {
                            if (debug != null) {
                                debug.println("must only be one CRL issuer when relative name present");
                            }
                            return false;
                        } else {
                            pointNames2 = getFullNames(pointCrlIssuer3, relativeName2);
                        }
                    } else {
                        GeneralNames generalNames5 = pointNames2;
                    }
                    boolean match4 = false;
                    Iterator<GeneralName> i5 = idpNames.iterator();
                    while (!match4 && i5.hasNext()) {
                        GeneralNameInterface idpName = i5.next().getName();
                        if (debug != null) {
                            certIssuer = certIssuer2;
                            Debug debug3 = debug;
                            pointCrlIssuers = pointCrlIssuers2;
                            StringBuilder sb2 = new StringBuilder();
                            pointCrlIssuer = pointCrlIssuer3;
                            sb2.append("idpName: ");
                            sb2.append((Object) idpName);
                            debug3.println(sb2.toString());
                        } else {
                            certIssuer = certIssuer2;
                            pointCrlIssuers = pointCrlIssuers2;
                            pointCrlIssuer = pointCrlIssuer3;
                        }
                        Iterator<GeneralName> p = pointNames2.iterator();
                        while (!match4 && p.hasNext()) {
                            GeneralNameInterface pointName = p.next().getName();
                            if (debug != null) {
                                Debug debug4 = debug;
                                pointNames = pointNames2;
                                StringBuilder sb3 = new StringBuilder();
                                boolean z = match4;
                                sb3.append("pointName: ");
                                sb3.append((Object) pointName);
                                debug4.println(sb3.toString());
                            } else {
                                pointNames = pointNames2;
                                boolean z2 = match4;
                            }
                            match4 = idpName.equals(pointName);
                            pointNames2 = pointNames;
                        }
                        certIssuer2 = certIssuer;
                        pointCrlIssuers2 = pointCrlIssuers;
                        pointCrlIssuer3 = pointCrlIssuer;
                        pointNames2 = pointNames2;
                        match4 = match4;
                    }
                    X500Name x500Name3 = certIssuer2;
                    GeneralNames generalNames6 = pointCrlIssuers2;
                    X500Name x500Name4 = pointCrlIssuer3;
                    if (!match4) {
                        if (debug != null) {
                            debug.println("IDP name does not match DP name");
                        }
                        return false;
                    }
                }
            } else {
                X500Name x500Name5 = certIssuer2;
                GeneralNames generalNames7 = pointCrlIssuers2;
                X500Name x500Name6 = pointCrlIssuer3;
            }
            if (((Boolean) idpExt.get(IssuingDistributionPointExtension.ONLY_USER_CERTS)).equals(Boolean.TRUE) && certImpl.getBasicConstraints() != -1) {
                if (debug != null) {
                    debug.println("cert must be a EE cert");
                }
                return false;
            } else if (((Boolean) idpExt.get(IssuingDistributionPointExtension.ONLY_CA_CERTS)).equals(Boolean.TRUE) && certImpl.getBasicConstraints() == -1) {
                if (debug != null) {
                    debug.println("cert must be a CA cert");
                }
                return false;
            } else if (((Boolean) idpExt.get(IssuingDistributionPointExtension.ONLY_ATTRIBUTE_CERTS)).equals(Boolean.TRUE)) {
                if (debug != null) {
                    debug.println("cert must not be an AA cert");
                }
                return false;
            }
        } else {
            GeneralNames generalNames8 = pointCrlIssuers2;
            X500Name x500Name7 = pointCrlIssuer3;
        }
        boolean[] interimReasonsMask = new boolean[9];
        ReasonFlags reasons = null;
        if (idpExt != null) {
            reasons = idpExt.get(IssuingDistributionPointExtension.REASONS);
        }
        boolean[] pointReasonFlags = point.getReasonFlags();
        if (reasons != null) {
            if (pointReasonFlags != null) {
                boolean[] idpReasonFlags = reasons.getFlags();
                int i6 = 0;
                while (i6 < interimReasonsMask.length) {
                    interimReasonsMask[i6] = i6 < idpReasonFlags.length && idpReasonFlags[i6] && i6 < pointReasonFlags.length && pointReasonFlags[i6];
                    i6++;
                }
            } else {
                interimReasonsMask = reasons.getFlags().clone();
            }
        } else if (idpExt == null || reasons == null) {
            if (pointReasonFlags != null) {
                interimReasonsMask = pointReasonFlags.clone();
            } else {
                Arrays.fill(interimReasonsMask, true);
            }
        }
        boolean[] interimReasonsMask2 = interimReasonsMask;
        boolean oneOrMore = false;
        for (int i7 = 0; i7 < interimReasonsMask2.length && !oneOrMore; i7++) {
            if (interimReasonsMask2[i7] && (i7 >= zArr.length || !zArr[i7])) {
                oneOrMore = true;
            }
        }
        if (!oneOrMore) {
            return false;
        }
        if (indirectCRL != null) {
            X509CertSelector certSel = new X509CertSelector();
            certSel.setSubject(crlIssuer.asX500Principal());
            certSel.setKeyUsage(new boolean[]{false, false, false, false, false, false, true});
            AuthorityKeyIdentifierExtension akidext = crlImpl.getAuthKeyIdExtension();
            if (akidext != null) {
                byte[] kid = akidext.getEncodedKeyIdentifier();
                if (kid != null) {
                    certSel.setSubjectKeyIdentifier(kid);
                }
                byte[] bArr = kid;
                SerialNumber asn = (SerialNumber) akidext.get(AuthorityKeyIdentifierExtension.SERIAL_NUMBER);
                if (asn != null) {
                    ReasonFlags reasonFlags = reasons;
                    certSel.setSerialNumber(asn.getNumber());
                    HashSet hashSet2 = new HashSet(trustAnchors);
                    if (prevKey2 == null) {
                        if (x509Certificate != null) {
                            temporary = new TrustAnchor(x509Certificate, null);
                        } else {
                            temporary = new TrustAnchor(certImpl.getIssuerX500Principal(), prevKey2, (byte[]) null);
                        }
                        hashSet = hashSet2;
                        hashSet.add(temporary);
                    } else {
                        hashSet = hashSet2;
                    }
                    params = new PKIXBuilderParameters((Set<TrustAnchor>) hashSet, (CertSelector) certSel);
                    HashSet hashSet3 = hashSet;
                    params.setCertStores(certStores);
                    params.setSigProvider(str);
                    params.setDate(validity);
                    CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
                    CertPathBuilder certPathBuilder = builder;
                    prevKey2 = ((PKIXCertPathBuilderResult) builder.build(params)).getPublicKey();
                }
            }
            HashSet hashSet22 = new HashSet(trustAnchors);
            if (prevKey2 == null) {
            }
            try {
                params = new PKIXBuilderParameters((Set<TrustAnchor>) hashSet, (CertSelector) certSel);
                HashSet hashSet32 = hashSet;
                params.setCertStores(certStores);
                params.setSigProvider(str);
                params.setDate(validity);
                try {
                    CertPathBuilder builder2 = CertPathBuilder.getInstance("PKIX");
                    CertPathBuilder certPathBuilder2 = builder2;
                    prevKey2 = ((PKIXCertPathBuilderResult) builder2.build(params)).getPublicKey();
                } catch (GeneralSecurityException e) {
                    PKIXBuilderParameters pKIXBuilderParameters = params;
                    throw new CRLException((Throwable) e);
                }
            } catch (InvalidAlgorithmParameterException iape) {
                HashSet hashSet4 = hashSet;
                Date date = validity;
                throw new CRLException((Throwable) iape);
            }
        } else {
            Date date2 = validity;
        }
        X509CRL x509crl2 = crl;
        try {
            AlgorithmChecker.check(prevKey2, x509crl2);
            try {
                x509crl2.verify(prevKey2, str);
                Set<String> unresCritExts = crl.getCriticalExtensionOIDs();
                if (unresCritExts != null) {
                    unresCritExts.remove(PKIXExtensions.IssuingDistributionPoint_Id.toString());
                    if (!unresCritExts.isEmpty()) {
                        if (debug != null) {
                            debug.println("Unrecognized critical extension(s) in CRL: " + unresCritExts);
                            for (String ext : unresCritExts) {
                                debug.println(ext);
                            }
                        }
                        return false;
                    }
                }
                int i8 = 0;
                while (i8 < zArr.length) {
                    zArr[i8] = zArr[i8] || (i8 < interimReasonsMask2.length && interimReasonsMask2[i8]);
                    i8++;
                }
                return true;
            } catch (GeneralSecurityException e2) {
                GeneralSecurityException generalSecurityException = e2;
                if (debug != null) {
                    debug.println("CRL signature failed to verify");
                }
                return false;
            }
        } catch (CertPathValidatorException cpve) {
            CertPathValidatorException certPathValidatorException = cpve;
            if (debug != null) {
                debug.println("CRL signature algorithm check failed: " + cpve);
            }
            return false;
        }
    }

    private static GeneralNames getFullNames(X500Name issuer, RDN rdn) throws IOException {
        List<RDN> rdns = new ArrayList<>((Collection<? extends RDN>) issuer.rdns());
        rdns.add(rdn);
        X500Name fullName = new X500Name((RDN[]) rdns.toArray(new RDN[0]));
        GeneralNames fullNames = new GeneralNames();
        fullNames.add(new GeneralName((GeneralNameInterface) fullName));
        return fullNames;
    }

    private static boolean issues(X509CertImpl cert, X509CRLImpl crl, String provider) throws IOException {
        AdaptableX509CertSelector issuerSelector = new AdaptableX509CertSelector();
        boolean[] usages = cert.getKeyUsage();
        if (usages != null) {
            usages[6] = true;
            issuerSelector.setKeyUsage(usages);
        }
        issuerSelector.setSubject(crl.getIssuerX500Principal());
        AuthorityKeyIdentifierExtension crlAKID = crl.getAuthKeyIdExtension();
        issuerSelector.setSkiAndSerialNumber(crlAKID);
        boolean matched = issuerSelector.match(cert);
        if (!matched) {
            return matched;
        }
        if (crlAKID != null && cert.getAuthorityKeyIdentifierExtension() != null) {
            return matched;
        }
        try {
            crl.verify(cert.getPublicKey(), provider);
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
}
