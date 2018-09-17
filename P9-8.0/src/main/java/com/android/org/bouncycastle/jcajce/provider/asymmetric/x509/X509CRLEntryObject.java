package com.android.org.bouncycastle.jcajce.provider.asymmetric.x509;

import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1Enumerated;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.util.ASN1Dump;
import com.android.org.bouncycastle.asn1.x500.X500Name;
import com.android.org.bouncycastle.asn1.x509.CRLReason;
import com.android.org.bouncycastle.asn1.x509.Extension;
import com.android.org.bouncycastle.asn1.x509.Extensions;
import com.android.org.bouncycastle.asn1.x509.GeneralName;
import com.android.org.bouncycastle.asn1.x509.GeneralNames;
import com.android.org.bouncycastle.asn1.x509.TBSCertList.CRLEntry;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CRLException;
import java.security.cert.X509CRLEntry;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

class X509CRLEntryObject extends X509CRLEntry {
    private CRLEntry c;
    private X500Name certificateIssuer;
    private int hashValue;
    private boolean isHashValueSet;

    protected X509CRLEntryObject(CRLEntry c) {
        this.c = c;
        this.certificateIssuer = null;
    }

    protected X509CRLEntryObject(CRLEntry c, boolean isIndirect, X500Name previousCertificateIssuer) {
        this.c = c;
        this.certificateIssuer = loadCertificateIssuer(isIndirect, previousCertificateIssuer);
    }

    public boolean hasUnsupportedCriticalExtension() {
        Set extns = getCriticalExtensionOIDs();
        return extns != null ? extns.isEmpty() ^ 1 : false;
    }

    private X500Name loadCertificateIssuer(boolean isIndirect, X500Name previousCertificateIssuer) {
        if (!isIndirect) {
            return null;
        }
        Extension ext = getExtension(Extension.certificateIssuer);
        if (ext == null) {
            return previousCertificateIssuer;
        }
        try {
            GeneralName[] names = GeneralNames.getInstance(ext.getParsedValue()).getNames();
            for (int i = 0; i < names.length; i++) {
                if (names[i].getTagNo() == 4) {
                    return X500Name.getInstance(names[i].getName());
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public X500Principal getCertificateIssuer() {
        if (this.certificateIssuer == null) {
            return null;
        }
        try {
            return new X500Principal(this.certificateIssuer.getEncoded());
        } catch (IOException e) {
            return null;
        }
    }

    private Set getExtensionOIDs(boolean critical) {
        Extensions extensions = this.c.getExtensions();
        if (extensions == null) {
            return null;
        }
        Set set = new HashSet();
        Enumeration e = extensions.oids();
        while (e.hasMoreElements()) {
            ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) e.nextElement();
            if (critical == extensions.getExtension(oid).isCritical()) {
                set.add(oid.getId());
            }
        }
        return set;
    }

    public Set getCriticalExtensionOIDs() {
        return getExtensionOIDs(true);
    }

    public Set getNonCriticalExtensionOIDs() {
        return getExtensionOIDs(false);
    }

    private Extension getExtension(ASN1ObjectIdentifier oid) {
        Extensions exts = this.c.getExtensions();
        if (exts != null) {
            return exts.getExtension(oid);
        }
        return null;
    }

    public byte[] getExtensionValue(String oid) {
        Extension ext = getExtension(new ASN1ObjectIdentifier(oid));
        if (ext == null) {
            return null;
        }
        try {
            return ext.getExtnValue().getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Exception encoding: " + e.toString());
        }
    }

    public int hashCode() {
        if (!this.isHashValueSet) {
            this.hashValue = super.hashCode();
            this.isHashValueSet = true;
        }
        return this.hashValue;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof X509CRLEntryObject)) {
            return super.equals(this);
        }
        return this.c.equals(((X509CRLEntryObject) o).c);
    }

    public byte[] getEncoded() throws CRLException {
        try {
            return this.c.getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            throw new CRLException(e.toString());
        }
    }

    public BigInteger getSerialNumber() {
        return this.c.getUserCertificate().getValue();
    }

    public Date getRevocationDate() {
        return this.c.getRevocationDate().getDate();
    }

    public boolean hasExtensions() {
        return this.c.getExtensions() != null;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        String nl = Strings.lineSeparator();
        buf.append("      userCertificate: ").append(getSerialNumber()).append(nl);
        buf.append("       revocationDate: ").append(getRevocationDate()).append(nl);
        buf.append("       certificateIssuer: ").append(getCertificateIssuer()).append(nl);
        Extensions extensions = this.c.getExtensions();
        if (extensions != null) {
            Enumeration e = extensions.oids();
            if (e.hasMoreElements()) {
                buf.append("   crlEntryExtensions:").append(nl);
                while (e.hasMoreElements()) {
                    ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) e.nextElement();
                    Extension ext = extensions.getExtension(oid);
                    if (ext.getExtnValue() != null) {
                        ASN1InputStream dIn = new ASN1InputStream(ext.getExtnValue().getOctets());
                        buf.append("                       critical(").append(ext.isCritical()).append(") ");
                        try {
                            if (oid.equals(Extension.reasonCode)) {
                                buf.append(CRLReason.getInstance(ASN1Enumerated.getInstance(dIn.readObject()))).append(nl);
                            } else if (oid.equals(Extension.certificateIssuer)) {
                                buf.append("Certificate issuer: ").append(GeneralNames.getInstance(dIn.readObject())).append(nl);
                            } else {
                                buf.append(oid.getId());
                                buf.append(" value = ").append(ASN1Dump.dumpAsString(dIn.readObject())).append(nl);
                            }
                        } catch (Exception e2) {
                            buf.append(oid.getId());
                            buf.append(" value = ").append("*****").append(nl);
                        }
                    } else {
                        buf.append(nl);
                    }
                }
            }
        }
        return buf.toString();
    }
}
