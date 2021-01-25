package org.bouncycastle.jce.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CRL;
import java.security.cert.CRLSelector;
import java.security.cert.CertSelector;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertStoreSpi;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRLSelector;
import java.security.cert.X509CertSelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.x509.CertificatePair;
import org.bouncycastle.jce.X509LDAPCertStoreParameters;

public class X509LDAPCertStoreSpi extends CertStoreSpi {
    private static String LDAP_PROVIDER = "com.sun.jndi.ldap.LdapCtxFactory";
    private static String REFERRALS_IGNORE = "ignore";
    private static final String SEARCH_SECURITY_LEVEL = "none";
    private static final String URL_CONTEXT_PREFIX = "com.sun.jndi.url";
    private X509LDAPCertStoreParameters params;

    public X509LDAPCertStoreSpi(CertStoreParameters certStoreParameters) throws InvalidAlgorithmParameterException {
        super(certStoreParameters);
        if (certStoreParameters instanceof X509LDAPCertStoreParameters) {
            this.params = (X509LDAPCertStoreParameters) certStoreParameters;
            return;
        }
        throw new InvalidAlgorithmParameterException(X509LDAPCertStoreSpi.class.getName() + ": parameter must be a " + X509LDAPCertStoreParameters.class.getName() + " object\n" + certStoreParameters.toString());
    }

    private Set certSubjectSerialSearch(X509CertSelector x509CertSelector, String[] strArr, String str, String str2) throws CertStoreException {
        String str3;
        Set search;
        HashSet hashSet = new HashSet();
        try {
            if (x509CertSelector.getSubjectAsBytes() == null && x509CertSelector.getSubjectAsString() == null && x509CertSelector.getCertificate() == null) {
                search = search(str, "*", strArr);
            } else {
                String str4 = null;
                if (x509CertSelector.getCertificate() != null) {
                    String name = x509CertSelector.getCertificate().getSubjectX500Principal().getName("RFC1779");
                    str4 = x509CertSelector.getCertificate().getSerialNumber().toString();
                    str3 = name;
                } else {
                    str3 = x509CertSelector.getSubjectAsBytes() != null ? new X500Principal(x509CertSelector.getSubjectAsBytes()).getName("RFC1779") : x509CertSelector.getSubjectAsString();
                }
                String parseDN = parseDN(str3, str2);
                hashSet.addAll(search(str, "*" + parseDN + "*", strArr));
                if (!(str4 == null || this.params.getSearchForSerialNumberIn() == null)) {
                    String searchForSerialNumberIn = this.params.getSearchForSerialNumberIn();
                    search = search(searchForSerialNumberIn, "*" + str4 + "*", strArr);
                }
                return hashSet;
            }
            hashSet.addAll(search);
            return hashSet;
        } catch (IOException e) {
            throw new CertStoreException("exception processing selector: " + e);
        }
    }

    private DirContext connectLDAP() throws NamingException {
        Properties properties = new Properties();
        properties.setProperty("java.naming.factory.initial", LDAP_PROVIDER);
        properties.setProperty("java.naming.batchsize", "0");
        properties.setProperty("java.naming.provider.url", this.params.getLdapURL());
        properties.setProperty("java.naming.factory.url.pkgs", URL_CONTEXT_PREFIX);
        properties.setProperty("java.naming.referral", REFERRALS_IGNORE);
        properties.setProperty("java.naming.security.authentication", SEARCH_SECURITY_LEVEL);
        return new InitialDirContext(properties);
    }

    private Set getCACertificates(X509CertSelector x509CertSelector) throws CertStoreException {
        String[] strArr = {this.params.getCACertificateAttribute()};
        Set certSubjectSerialSearch = certSubjectSerialSearch(x509CertSelector, strArr, this.params.getLdapCACertificateAttributeName(), this.params.getCACertificateSubjectAttributeName());
        if (certSubjectSerialSearch.isEmpty()) {
            certSubjectSerialSearch.addAll(search(null, "*", strArr));
        }
        return certSubjectSerialSearch;
    }

    private Set getCrossCertificates(X509CertSelector x509CertSelector) throws CertStoreException {
        String[] strArr = {this.params.getCrossCertificateAttribute()};
        Set certSubjectSerialSearch = certSubjectSerialSearch(x509CertSelector, strArr, this.params.getLdapCrossCertificateAttributeName(), this.params.getCrossCertificateSubjectAttributeName());
        if (certSubjectSerialSearch.isEmpty()) {
            certSubjectSerialSearch.addAll(search(null, "*", strArr));
        }
        return certSubjectSerialSearch;
    }

    private Set getEndCertificates(X509CertSelector x509CertSelector) throws CertStoreException {
        return certSubjectSerialSearch(x509CertSelector, new String[]{this.params.getUserCertificateAttribute()}, this.params.getLdapUserCertificateAttributeName(), this.params.getUserCertificateSubjectAttributeName());
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x005a  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0064  */
    /* JADX WARNING: Removed duplicated region for block: B:20:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x004e  */
    private java.lang.String parseDN(java.lang.String r5, java.lang.String r6) {
        /*
            r4 = this;
            java.lang.String r0 = r5.toLowerCase()
            java.lang.String r1 = r6.toLowerCase()
            int r0 = r0.indexOf(r1)
            int r6 = r6.length()
            int r0 = r0 + r6
            java.lang.String r5 = r5.substring(r0)
            r6 = 44
            int r0 = r5.indexOf(r6)
            r1 = -1
            if (r0 != r1) goto L_0x0022
        L_0x001e:
            int r0 = r5.length()
        L_0x0022:
            int r2 = r0 + -1
            char r2 = r5.charAt(r2)
            r3 = 92
            if (r2 != r3) goto L_0x0035
            int r0 = r0 + 1
            int r0 = r5.indexOf(r6, r0)
            if (r0 != r1) goto L_0x0022
            goto L_0x001e
        L_0x0035:
            r6 = 0
            java.lang.String r5 = r5.substring(r6, r0)
            r0 = 61
            int r0 = r5.indexOf(r0)
            r1 = 1
            int r0 = r0 + r1
            java.lang.String r5 = r5.substring(r0)
            char r0 = r5.charAt(r6)
            r2 = 32
            if (r0 != r2) goto L_0x0052
            java.lang.String r5 = r5.substring(r1)
        L_0x0052:
            java.lang.String r0 = "\""
            boolean r2 = r5.startsWith(r0)
            if (r2 == 0) goto L_0x005e
            java.lang.String r5 = r5.substring(r1)
        L_0x005e:
            boolean r0 = r5.endsWith(r0)
            if (r0 == 0) goto L_0x006d
            int r0 = r5.length()
            int r0 = r0 - r1
            java.lang.String r5 = r5.substring(r6, r0)
        L_0x006d:
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: org.bouncycastle.jce.provider.X509LDAPCertStoreSpi.parseDN(java.lang.String, java.lang.String):java.lang.String");
    }

    private Set search(String str, String str2, String[] strArr) throws CertStoreException {
        String str3 = str + "=" + str2;
        DirContext dirContext = null;
        if (str == null) {
            str3 = null;
        }
        HashSet hashSet = new HashSet();
        try {
            DirContext connectLDAP = connectLDAP();
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(2);
            searchControls.setCountLimit(0);
            for (int i = 0; i < strArr.length; i++) {
                String[] strArr2 = {strArr[i]};
                searchControls.setReturningAttributes(strArr2);
                String str4 = "(&(" + str3 + ")(" + strArr2[0] + "=*))";
                if (str3 == null) {
                    str4 = "(" + strArr2[0] + "=*)";
                }
                NamingEnumeration search = connectLDAP.search(this.params.getBaseDN(), str4, searchControls);
                while (search.hasMoreElements()) {
                    NamingEnumeration all = ((Attribute) ((SearchResult) search.next()).getAttributes().getAll().next()).getAll();
                    while (all.hasMore()) {
                        hashSet.add(all.next());
                    }
                }
            }
            if (connectLDAP != null) {
                try {
                    connectLDAP.close();
                } catch (Exception e) {
                }
            }
            return hashSet;
        } catch (Exception e2) {
            throw new CertStoreException("Error getting results from LDAP directory " + e2);
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    dirContext.close();
                } catch (Exception e3) {
                }
            }
            throw th;
        }
    }

    @Override // java.security.cert.CertStoreSpi
    public Collection engineGetCRLs(CRLSelector cRLSelector) throws CertStoreException {
        String str;
        String str2;
        String[] strArr = {this.params.getCertificateRevocationListAttribute()};
        if (cRLSelector instanceof X509CRLSelector) {
            X509CRLSelector x509CRLSelector = (X509CRLSelector) cRLSelector;
            HashSet hashSet = new HashSet();
            String ldapCertificateRevocationListAttributeName = this.params.getLdapCertificateRevocationListAttributeName();
            HashSet<byte[]> hashSet2 = new HashSet();
            if (x509CRLSelector.getIssuerNames() != null) {
                for (Object obj : x509CRLSelector.getIssuerNames()) {
                    if (obj instanceof String) {
                        str = this.params.getCertificateRevocationListIssuerAttributeName();
                        str2 = (String) obj;
                    } else {
                        str = this.params.getCertificateRevocationListIssuerAttributeName();
                        str2 = new X500Principal((byte[]) obj).getName("RFC1779");
                    }
                    String parseDN = parseDN(str2, str);
                    hashSet2.addAll(search(ldapCertificateRevocationListAttributeName, "*" + parseDN + "*", strArr));
                }
            } else {
                hashSet2.addAll(search(ldapCertificateRevocationListAttributeName, "*", strArr));
            }
            hashSet2.addAll(search(null, "*", strArr));
            try {
                CertificateFactory instance = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
                for (byte[] bArr : hashSet2) {
                    CRL generateCRL = instance.generateCRL(new ByteArrayInputStream(bArr));
                    if (x509CRLSelector.match(generateCRL)) {
                        hashSet.add(generateCRL);
                    }
                }
                return hashSet;
            } catch (Exception e) {
                throw new CertStoreException("CRL cannot be constructed from LDAP result " + e);
            }
        } else {
            throw new CertStoreException("selector is not a X509CRLSelector");
        }
    }

    @Override // java.security.cert.CertStoreSpi
    public Collection engineGetCertificates(CertSelector certSelector) throws CertStoreException {
        if (certSelector instanceof X509CertSelector) {
            X509CertSelector x509CertSelector = (X509CertSelector) certSelector;
            HashSet hashSet = new HashSet();
            Set<byte[]> endCertificates = getEndCertificates(x509CertSelector);
            endCertificates.addAll(getCACertificates(x509CertSelector));
            endCertificates.addAll(getCrossCertificates(x509CertSelector));
            try {
                CertificateFactory instance = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
                for (byte[] bArr : endCertificates) {
                    if (bArr != null) {
                        if (bArr.length != 0) {
                            ArrayList<byte[]> arrayList = new ArrayList();
                            arrayList.add(bArr);
                            try {
                                CertificatePair instance2 = CertificatePair.getInstance(new ASN1InputStream(bArr).readObject());
                                arrayList.clear();
                                if (instance2.getForward() != null) {
                                    arrayList.add(instance2.getForward().getEncoded());
                                }
                                if (instance2.getReverse() != null) {
                                    arrayList.add(instance2.getReverse().getEncoded());
                                }
                            } catch (IOException | IllegalArgumentException e) {
                            }
                            for (byte[] bArr2 : arrayList) {
                                try {
                                    Certificate generateCertificate = instance.generateCertificate(new ByteArrayInputStream(bArr2));
                                    if (x509CertSelector.match(generateCertificate)) {
                                        hashSet.add(generateCertificate);
                                    }
                                } catch (Exception e2) {
                                }
                            }
                        }
                    }
                }
                return hashSet;
            } catch (Exception e3) {
                throw new CertStoreException("certificate cannot be constructed from LDAP result: " + e3);
            }
        } else {
            throw new CertStoreException("selector is not a X509CertSelector");
        }
    }
}
