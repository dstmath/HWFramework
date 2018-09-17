package sun.security.provider.certpath;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPathBuilder;
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
    private static final boolean[] ALL_REASONS = new boolean[]{true, true, true, true, true, true, true, true, true};
    private static final Debug debug = Debug.getInstance("certpath");

    private DistributionPointFetcher() {
    }

    public static Collection<X509CRL> getCRLs(X509CRLSelector selector, boolean signFlag, PublicKey prevKey, String provider, List<CertStore> certStores, boolean[] reasonsMask, Set<TrustAnchor> trustAnchors, Date validity) throws CertStoreException {
        return getCRLs(selector, signFlag, prevKey, null, provider, certStores, reasonsMask, trustAnchors, validity);
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x008b A:{Splitter: B:4:0x000b, ExcHandler: java.security.cert.CertificateException (e java.security.cert.CertificateException)} */
    /* JADX WARNING: Missing block: B:24:0x0090, code:
            return java.util.Collections.emptySet();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Collection<X509CRL> getCRLs(X509CRLSelector selector, boolean signFlag, PublicKey prevKey, X509Certificate prevCert, String provider, List<CertStore> certStores, boolean[] reasonsMask, Set<TrustAnchor> trustAnchors, Date validity) throws CertStoreException {
        X509Certificate cert = selector.getCertificateChecking();
        if (cert == null) {
            return Collections.emptySet();
        }
        try {
            X509CertImpl certImpl = X509CertImpl.toImpl(cert);
            if (debug != null) {
                debug.println("DistributionPointFetcher.getCRLs: Checking CRLDPs for " + certImpl.getSubjectX500Principal());
            }
            CRLDistributionPointsExtension ext = certImpl.getCRLDistributionPointsExtension();
            if (ext == null) {
                if (debug != null) {
                    debug.println("No CRLDP ext");
                }
                return Collections.emptySet();
            }
            List<DistributionPoint> points = ext.get(CRLDistributionPointsExtension.POINTS);
            Set<X509CRL> results = new HashSet();
            for (DistributionPoint point : points) {
                if ((Arrays.equals(reasonsMask, ALL_REASONS) ^ 1) != 0) {
                    Set<X509CRL> set = results;
                    set.addAll(getCRLs(selector, certImpl, point, reasonsMask, signFlag, prevKey, prevCert, provider, certStores, trustAnchors, validity));
                }
            }
            if (debug != null) {
                debug.println("Returning " + results.size() + " CRLs");
            }
            return results;
        } catch (CertificateException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x00e5 A:{Splitter: B:44:0x00ba, ExcHandler: java.io.IOException (r17_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:51:0x00e5, code:
            r17 = move-exception;
     */
    /* JADX WARNING: Missing block: B:53:0x00e8, code:
            if (debug != null) goto L_0x00ea;
     */
    /* JADX WARNING: Missing block: B:54:0x00ea, code:
            debug.println("Exception verifying CRL: " + r17.getMessage());
            r17.printStackTrace();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Collection<X509CRL> getCRLs(X509CRLSelector selector, X509CertImpl certImpl, DistributionPoint point, boolean[] reasonsMask, boolean signFlag, PublicKey prevKey, X509Certificate prevCert, String provider, List<CertStore> certStores, Set<TrustAnchor> trustAnchors, Date validity) throws CertStoreException {
        X509CRL crl;
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
        Collection<X509CRL> possibleCRLs = new ArrayList();
        CertStoreException savedCSE = null;
        Iterator<GeneralName> t = fullName.iterator();
        while (t.hasNext()) {
            try {
                GeneralName name = (GeneralName) t.next();
                if (name.getType() == 4) {
                    possibleCRLs.addAll(getCRLs((X500Name) name.getName(), certImpl.getIssuerX500Principal(), certStores));
                } else if (name.getType() == 6) {
                    crl = getCRL((URIName) name.getName());
                    if (crl != null) {
                        possibleCRLs.-java_util_stream_Collectors-mthref-0(crl);
                    }
                }
            } catch (CertStoreException cse) {
                savedCSE = cse;
            }
        }
        if (!possibleCRLs.isEmpty() || savedCSE == null) {
            Collection<X509CRL> crls = new ArrayList(2);
            for (X509CRL crl2 : possibleCRLs) {
                try {
                    selector.setIssuerNames(null);
                    if (selector.match(crl2) && verifyCRL(certImpl, point, crl2, reasonsMask, signFlag, prevKey, prevCert, provider, trustAnchors, certStores, validity)) {
                        crls.-java_util_stream_Collectors-mthref-0(crl2);
                    }
                } catch (Exception e2) {
                }
            }
            return crls;
        }
        throw savedCSE;
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x0037 A:{Splitter: B:4:0x0023, ExcHandler: java.security.InvalidAlgorithmParameterException (r1_0 'e' java.security.GeneralSecurityException)} */
    /* JADX WARNING: Missing block: B:9:0x0037, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x003a, code:
            if (debug != null) goto L_0x003c;
     */
    /* JADX WARNING: Missing block: B:12:0x003c, code:
            debug.println("Can't create URICertStore: " + r1.getMessage());
     */
    /* JADX WARNING: Missing block: B:13:0x0059, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static X509CRL getCRL(URIName name) throws CertStoreException {
        Object uri = name.getURI();
        if (debug != null) {
            debug.println("Trying to fetch CRL from DP " + uri);
        }
        try {
            Collection<? extends CRL> crls = URICertStore.getInstance(new URICertStoreParameters(uri)).getCRLs(null);
            if (crls.isEmpty()) {
                return null;
            }
            return (X509CRL) crls.iterator().next();
        } catch (GeneralSecurityException e) {
        }
    }

    private static Collection<X509CRL> getCRLs(X500Name name, X500Principal certIssuer, List<CertStore> certStores) throws CertStoreException {
        if (debug != null) {
            debug.println("Trying to fetch CRL from DP " + name);
        }
        X509CRLSelector xcs = new X509CRLSelector();
        xcs.addIssuer(name.asX500Principal());
        xcs.addIssuer(certIssuer);
        Collection<X509CRL> crls = new ArrayList();
        CertStoreException savedCSE = null;
        for (CertStore store : certStores) {
            try {
                for (CRL crl : store.getCRLs(xcs)) {
                    crls.-java_util_stream_Collectors-mthref-0((X509CRL) crl);
                }
            } catch (Object cse) {
                if (debug != null) {
                    debug.println("Exception while retrieving CRLs: " + cse);
                    cse.printStackTrace();
                }
                savedCSE = new CertStoreTypeException(store.getType(), cse);
            }
        }
        if (!crls.isEmpty() || savedCSE == null) {
            return crls;
        }
        throw savedCSE;
    }

    static boolean verifyCRL(X509CertImpl certImpl, DistributionPoint point, X509CRL crl, boolean[] reasonsMask, boolean signFlag, PublicKey prevKey, X509Certificate prevCert, String provider, Set<TrustAnchor> trustAnchors, List<CertStore> certStores, Date validity) throws CRLException, IOException {
        boolean match;
        Iterator<GeneralName> t;
        if (debug != null) {
            debug.println("DistributionPointFetcher.verifyCRL: checking revocation status for\n  SN: " + Debug.toHexString(certImpl.getSerialNumber()) + "\n  Subject: " + certImpl.getSubjectX500Principal() + "\n  Issuer: " + certImpl.getIssuerX500Principal());
        }
        boolean indirectCRL = false;
        X509CRLImpl crlImpl = X509CRLImpl.toImpl(crl);
        IssuingDistributionPointExtension idpExt = crlImpl.getIssuingDistributionPointExtension();
        Object certIssuer = (X500Name) certImpl.getIssuerDN();
        Object crlIssuer = (X500Name) crlImpl.getIssuerDN();
        GeneralNames pointCrlIssuers = point.getCRLIssuer();
        X500Name pointCrlIssuer = null;
        if (pointCrlIssuers != null) {
            if (idpExt == null || ((Boolean) idpExt.get(IssuingDistributionPointExtension.INDIRECT_CRL)).equals(Boolean.FALSE)) {
                return false;
            }
            match = false;
            t = pointCrlIssuers.iterator();
            while (!match && t.hasNext()) {
                GeneralNameInterface name = ((GeneralName) t.next()).getName();
                if (crlIssuer.equals(name)) {
                    pointCrlIssuer = (X500Name) name;
                    match = true;
                }
            }
            if (!match) {
                return false;
            }
            if (issues(certImpl, crlImpl, provider)) {
                prevKey = certImpl.getPublicKey();
            } else {
                indirectCRL = true;
            }
        } else if (crlIssuer.equals(certIssuer)) {
            KeyIdentifier certAKID = certImpl.getAuthKeyId();
            KeyIdentifier crlAKID = crlImpl.getAuthKeyId();
            if (certAKID == null || crlAKID == null) {
                if (issues(certImpl, crlImpl, provider)) {
                    prevKey = certImpl.getPublicKey();
                }
            } else if (!certAKID.equals(crlAKID)) {
                if (issues(certImpl, crlImpl, provider)) {
                    prevKey = certImpl.getPublicKey();
                } else {
                    indirectCRL = true;
                }
            }
        } else {
            if (debug != null) {
                debug.println("crl issuer does not equal cert issuer.\ncrl issuer: " + crlIssuer + "\n" + "cert issuer: " + certIssuer);
            }
            return false;
        }
        if (!indirectCRL && (signFlag ^ 1) != 0) {
            return false;
        }
        int i;
        boolean z;
        if (idpExt != null) {
            DistributionPointName idpPoint = (DistributionPointName) idpExt.get(IssuingDistributionPointExtension.POINT);
            if (idpPoint != null) {
                RDN relativeName;
                GeneralNames idpNames = idpPoint.getFullName();
                if (idpNames == null) {
                    relativeName = idpPoint.getRelativeName();
                    if (relativeName == null) {
                        if (debug != null) {
                            debug.println("IDP must be relative or full DN");
                        }
                        return false;
                    }
                    if (debug != null) {
                        debug.println("IDP relativeName:" + relativeName);
                    }
                    idpNames = getFullNames(crlIssuer, relativeName);
                }
                Iterator<GeneralName> i2;
                if (point.getFullName() == null && point.getRelativeName() == null) {
                    match = false;
                    t = pointCrlIssuers.iterator();
                    while (!match && t.hasNext()) {
                        GeneralNameInterface crlIssuerName = ((GeneralName) t.next()).getName();
                        i2 = idpNames.iterator();
                        while (!match && i2.hasNext()) {
                            match = crlIssuerName.lambda$-java_util_function_Predicate_4628(((GeneralName) i2.next()).getName());
                        }
                    }
                    if (!match) {
                        return false;
                    }
                }
                GeneralNames pointNames = point.getFullName();
                if (pointNames == null) {
                    relativeName = point.getRelativeName();
                    if (relativeName == null) {
                        if (debug != null) {
                            debug.println("DP must be relative or full DN");
                        }
                        return false;
                    }
                    if (debug != null) {
                        debug.println("DP relativeName:" + relativeName);
                    }
                    if (!indirectCRL) {
                        pointNames = getFullNames(certIssuer, relativeName);
                    } else if (pointCrlIssuers.size() != 1) {
                        if (debug != null) {
                            debug.println("must only be one CRL issuer when relative name present");
                        }
                        return false;
                    } else {
                        pointNames = getFullNames(pointCrlIssuer, relativeName);
                    }
                }
                match = false;
                i2 = idpNames.iterator();
                while (!match && i2.hasNext()) {
                    GeneralNameInterface idpName = ((GeneralName) i2.next()).getName();
                    if (debug != null) {
                        debug.println("idpName: " + idpName);
                    }
                    Iterator<GeneralName> p = pointNames.iterator();
                    while (!match && p.hasNext()) {
                        GeneralNameInterface pointName = ((GeneralName) p.next()).getName();
                        if (debug != null) {
                            debug.println("pointName: " + pointName);
                        }
                        match = idpName.lambda$-java_util_function_Predicate_4628(pointName);
                    }
                }
                if (!match) {
                    if (debug != null) {
                        debug.println("IDP name does not match DP name");
                    }
                    return false;
                }
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
        }
        boolean[] interimReasonsMask = new boolean[9];
        ReasonFlags reasonFlags = null;
        if (idpExt != null) {
            reasonFlags = (ReasonFlags) idpExt.get(IssuingDistributionPointExtension.REASONS);
        }
        boolean[] pointReasonFlags = point.getReasonFlags();
        if (reasonFlags != null) {
            if (pointReasonFlags != null) {
                boolean[] idpReasonFlags = reasonFlags.getFlags();
                i = 0;
                while (i < interimReasonsMask.length) {
                    if (i >= idpReasonFlags.length || !idpReasonFlags[i]) {
                        z = false;
                    } else {
                        z = i < pointReasonFlags.length ? pointReasonFlags[i] : false;
                    }
                    interimReasonsMask[i] = z;
                    i++;
                }
            } else {
                interimReasonsMask = (boolean[]) reasonFlags.getFlags().clone();
            }
        } else if (idpExt == null || reasonFlags == null) {
            if (pointReasonFlags != null) {
                interimReasonsMask = (boolean[]) pointReasonFlags.clone();
            } else {
                Arrays.fill(interimReasonsMask, true);
            }
        }
        boolean oneOrMore = false;
        i = 0;
        while (i < interimReasonsMask.length && (oneOrMore ^ 1) != 0) {
            if (interimReasonsMask[i]) {
                if (((i < reasonsMask.length ? reasonsMask[i] : 0) ^ 1) != 0) {
                    oneOrMore = true;
                }
            }
            i++;
        }
        if (!oneOrMore) {
            return false;
        }
        if (indirectCRL) {
            CertSelector certSel = new X509CertSelector();
            certSel.setSubject(crlIssuer.asX500Principal());
            boolean[] zArr = new boolean[7];
            certSel.setKeyUsage(new boolean[]{false, false, false, false, false, false, true});
            AuthorityKeyIdentifierExtension akidext = crlImpl.getAuthKeyIdExtension();
            if (akidext != null) {
                byte[] kid = akidext.getEncodedKeyIdentifier();
                if (kid != null) {
                    certSel.setSubjectKeyIdentifier(kid);
                }
                SerialNumber asn = (SerialNumber) akidext.get(AuthorityKeyIdentifierExtension.SERIAL_NUMBER);
                if (asn != null) {
                    certSel.setSerialNumber(asn.getNumber());
                }
            }
            Set<TrustAnchor> hashSet = new HashSet((Collection) trustAnchors);
            if (prevKey != null) {
                TrustAnchor trustAnchor;
                if (prevCert != null) {
                    trustAnchor = new TrustAnchor(prevCert, null);
                } else {
                    trustAnchor = new TrustAnchor(certImpl.getIssuerX500Principal(), prevKey, null);
                }
                hashSet.-java_util_stream_Collectors-mthref-4(temporary);
            }
            try {
                PKIXBuilderParameters pKIXBuilderParameters = new PKIXBuilderParameters((Set) hashSet, certSel);
                pKIXBuilderParameters.setCertStores(certStores);
                pKIXBuilderParameters.setSigProvider(provider);
                pKIXBuilderParameters.setDate(validity);
                try {
                    prevKey = ((PKIXCertPathBuilderResult) CertPathBuilder.getInstance("PKIX").build(pKIXBuilderParameters)).getPublicKey();
                } catch (Throwable e) {
                    throw new CRLException(e);
                }
            } catch (Throwable iape) {
                throw new CRLException(iape);
            }
        }
        try {
            AlgorithmChecker.check(prevKey, crl);
            try {
                crl.verify(prevKey, provider);
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
                i = 0;
                while (i < reasonsMask.length) {
                    if (reasonsMask[i]) {
                        z = true;
                    } else {
                        z = i < interimReasonsMask.length ? interimReasonsMask[i] : false;
                    }
                    reasonsMask[i] = z;
                    i++;
                }
                return true;
            } catch (GeneralSecurityException e2) {
                if (debug != null) {
                    debug.println("CRL signature failed to verify");
                }
                return false;
            }
        } catch (Object cpve) {
            if (debug != null) {
                debug.println("CRL signature algorithm check failed: " + cpve);
            }
            return false;
        }
    }

    private static GeneralNames getFullNames(X500Name issuer, RDN rdn) throws IOException {
        List<RDN> rdns = new ArrayList(issuer.rdns());
        rdns.-java_util_stream_Collectors-mthref-2(rdn);
        GeneralNameInterface fullName = new X500Name((RDN[]) rdns.toArray(new RDN[0]));
        GeneralNames fullNames = new GeneralNames();
        fullNames.add(new GeneralName(fullName));
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
