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
import sun.security.util.DerOutputStream;
import sun.security.validator.Validator;
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
import sun.security.x509.SubjectKeyIdentifierExtension;
import sun.security.x509.URIName;
import sun.security.x509.X500Name;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;

public class DistributionPointFetcher {
    private static final boolean[] ALL_REASONS = null;
    private static final Debug debug = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.DistributionPointFetcher.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.DistributionPointFetcher.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.DistributionPointFetcher.<clinit>():void");
    }

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
                if (Arrays.equals(reasonsMask, ALL_REASONS)) {
                    break;
                }
                Set<X509CRL> set = results;
                set.addAll(getCRLs(selector, certImpl, point, reasonsMask, signFlag, prevKey, prevCert, provider, certStores, trustAnchors, validity));
            }
            if (debug != null) {
                debug.println("Returning " + results.size() + " CRLs");
            }
            return results;
        } catch (CertificateException e) {
            return Collections.emptySet();
        }
    }

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
                    X500Name x500Name = (X500Name) name.getName();
                    possibleCRLs.addAll(getCRLs(x500Name, certImpl.getIssuerX500Principal(), certStores));
                } else if (name.getType() == 6) {
                    crl = getCRL((URIName) name.getName());
                    if (crl != null) {
                        possibleCRLs.add(crl);
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
                        crls.add(crl2);
                    }
                } catch (Exception e2) {
                    if (debug != null) {
                        debug.println("Exception verifying CRL: " + e2.getMessage());
                        e2.printStackTrace();
                    }
                }
            }
            return crls;
        }
        throw savedCSE;
    }

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
            if (debug != null) {
                debug.println("Can't create URICertStore: " + e.getMessage());
            }
            return null;
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
                    crls.add((X509CRL) crl);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static boolean verifyCRL(X509CertImpl certImpl, DistributionPoint point, X509CRL crl, boolean[] reasonsMask, boolean signFlag, PublicKey prevKey, X509Certificate prevCert, String provider, Set<TrustAnchor> trustAnchors, List<CertStore> certStores, Date validity) throws CRLException, IOException {
        boolean match;
        Iterator<GeneralName> t;
        boolean indirectCRL = false;
        X509CRLImpl crlImpl = X509CRLImpl.toImpl(crl);
        IssuingDistributionPointExtension idpExt = crlImpl.getIssuingDistributionPointExtension();
        X500Name certIssuer = (X500Name) certImpl.getIssuerDN();
        X500Name crlIssuer = (X500Name) crlImpl.getIssuerDN();
        GeneralNames pointCrlIssuers = point.getCRLIssuer();
        X500Name x500Name = null;
        if (pointCrlIssuers != null) {
            if (idpExt != null) {
                if (!((Boolean) idpExt.get(IssuingDistributionPointExtension.INDIRECT_CRL)).equals(Boolean.FALSE)) {
                    match = false;
                    t = pointCrlIssuers.iterator();
                    while (!match && t.hasNext()) {
                        GeneralNameInterface name = ((GeneralName) t.next()).getName();
                        if (crlIssuer.equals(name)) {
                            x500Name = (X500Name) name;
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
                }
            }
            return false;
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
                debug.println("crl issuer does not equal cert issuer");
            }
            return false;
        }
        if (!indirectCRL && !signFlag) {
            return false;
        }
        int i;
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
                            match = crlIssuerName.equals(((GeneralName) i2.next()).getName());
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
                        pointNames = getFullNames(x500Name, relativeName);
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
                        match = idpName.equals(pointName);
                    }
                }
                if (!match) {
                    if (debug != null) {
                        debug.println("IDP name does not match DP name");
                    }
                    return false;
                }
            }
            String str = IssuingDistributionPointExtension.ONLY_USER_CERTS;
            if (!((Boolean) idpExt.get(r50)).equals(Boolean.TRUE) || certImpl.getBasicConstraints() == -1) {
                str = IssuingDistributionPointExtension.ONLY_CA_CERTS;
                if (((Boolean) idpExt.get(r50)).equals(Boolean.TRUE) && certImpl.getBasicConstraints() == -1) {
                    if (debug != null) {
                        debug.println("cert must be a CA cert");
                    }
                    return false;
                }
                str = IssuingDistributionPointExtension.ONLY_ATTRIBUTE_CERTS;
                if (((Boolean) idpExt.get(r50)).equals(Boolean.TRUE)) {
                    if (debug != null) {
                        debug.println("cert must not be an AA cert");
                    }
                    return false;
                }
            }
            if (debug != null) {
                debug.println("cert must be a EE cert");
            }
            return false;
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
                while (i < idpReasonFlags.length) {
                    if (idpReasonFlags[i] && pointReasonFlags[i]) {
                        interimReasonsMask[i] = true;
                    }
                    i++;
                }
            } else {
                interimReasonsMask = (boolean[]) reasonFlags.getFlags().clone();
            }
        } else if (idpExt == null || reasonFlags == null) {
            if (pointReasonFlags != null) {
                interimReasonsMask = (boolean[]) pointReasonFlags.clone();
            } else {
                interimReasonsMask = new boolean[9];
                Arrays.fill(interimReasonsMask, true);
            }
        }
        boolean oneOrMore = false;
        i = 0;
        while (i < interimReasonsMask.length && !oneOrMore) {
            if (!reasonsMask[i] && interimReasonsMask[i]) {
                oneOrMore = true;
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
                KeyIdentifier akid = (KeyIdentifier) akidext.get(SubjectKeyIdentifierExtension.KEY_ID);
                if (akid != null) {
                    DerOutputStream derout = new DerOutputStream();
                    derout.putOctetString(akid.getIdentifier());
                    certSel.setSubjectKeyIdentifier(derout.toByteArray());
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
                hashSet.add(temporary);
            }
            try {
                PKIXBuilderParameters pKIXBuilderParameters = new PKIXBuilderParameters((Set) hashSet, certSel);
                pKIXBuilderParameters.setCertStores(certStores);
                pKIXBuilderParameters.setSigProvider(provider);
                pKIXBuilderParameters.setDate(validity);
                try {
                    prevKey = ((PKIXCertPathBuilderResult) CertPathBuilder.getInstance(Validator.TYPE_PKIX).build(pKIXBuilderParameters)).getPublicKey();
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
                while (i < interimReasonsMask.length) {
                    if (!reasonsMask[i] && interimReasonsMask[i]) {
                        reasonsMask[i] = true;
                    }
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
        rdns.add(rdn);
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
        if (crlAKID != null) {
            issuerSelector.parseAuthorityKeyIdentifierExtension(crlAKID);
        }
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
