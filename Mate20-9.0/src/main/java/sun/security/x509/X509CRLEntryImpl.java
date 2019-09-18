package sun.security.x509;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CRLException;
import java.security.cert.CRLReason;
import java.security.cert.Extension;
import java.security.cert.X509CRLEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.security.auth.x500.X500Principal;
import sun.misc.HexDumpEncoder;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class X509CRLEntryImpl extends X509CRLEntry implements Comparable<X509CRLEntryImpl> {
    private static final long YR_2050 = 2524636800000L;
    private static final boolean isExplicit = false;
    private X500Principal certIssuer;
    private CRLExtensions extensions = null;
    private Date revocationDate = null;
    private byte[] revokedCert = null;
    private SerialNumber serialNumber = null;

    public X509CRLEntryImpl(BigInteger num, Date date) {
        this.serialNumber = new SerialNumber(num);
        this.revocationDate = date;
    }

    public X509CRLEntryImpl(BigInteger num, Date date, CRLExtensions crlEntryExts) {
        this.serialNumber = new SerialNumber(num);
        this.revocationDate = date;
        this.extensions = crlEntryExts;
    }

    public X509CRLEntryImpl(byte[] revokedCert2) throws CRLException {
        try {
            parse(new DerValue(revokedCert2));
        } catch (IOException e) {
            this.revokedCert = null;
            throw new CRLException("Parsing error: " + e.toString());
        }
    }

    public X509CRLEntryImpl(DerValue derValue) throws CRLException {
        try {
            parse(derValue);
        } catch (IOException e) {
            this.revokedCert = null;
            throw new CRLException("Parsing error: " + e.toString());
        }
    }

    public boolean hasExtensions() {
        if (this.extensions != null) {
            return true;
        }
        return isExplicit;
    }

    public void encode(DerOutputStream outStrm) throws CRLException {
        try {
            if (this.revokedCert == null) {
                DerOutputStream tmp = new DerOutputStream();
                this.serialNumber.encode(tmp);
                if (this.revocationDate.getTime() < YR_2050) {
                    tmp.putUTCTime(this.revocationDate);
                } else {
                    tmp.putGeneralizedTime(this.revocationDate);
                }
                if (this.extensions != null) {
                    this.extensions.encode(tmp, isExplicit);
                }
                DerOutputStream seq = new DerOutputStream();
                seq.write((byte) 48, tmp);
                this.revokedCert = seq.toByteArray();
            }
            outStrm.write(this.revokedCert);
        } catch (IOException e) {
            throw new CRLException("Encoding error: " + e.toString());
        }
    }

    public byte[] getEncoded() throws CRLException {
        return (byte[]) getEncoded0().clone();
    }

    private byte[] getEncoded0() throws CRLException {
        if (this.revokedCert == null) {
            encode(new DerOutputStream());
        }
        return this.revokedCert;
    }

    public X500Principal getCertificateIssuer() {
        return this.certIssuer;
    }

    /* access modifiers changed from: package-private */
    public void setCertificateIssuer(X500Principal crlIssuer, X500Principal certIssuer2) {
        if (crlIssuer.equals(certIssuer2)) {
            this.certIssuer = null;
        } else {
            this.certIssuer = certIssuer2;
        }
    }

    public BigInteger getSerialNumber() {
        return this.serialNumber.getNumber();
    }

    public Date getRevocationDate() {
        return new Date(this.revocationDate.getTime());
    }

    public CRLReason getRevocationReason() {
        Extension ext = getExtension(PKIXExtensions.ReasonCode_Id);
        if (ext == null) {
            return null;
        }
        return ((CRLReasonCodeExtension) ext).getReasonCode();
    }

    public static CRLReason getRevocationReason(X509CRLEntry crlEntry) {
        try {
            byte[] ext = crlEntry.getExtensionValue("2.5.29.21");
            if (ext == null) {
                return null;
            }
            return new CRLReasonCodeExtension(Boolean.FALSE, (Object) new DerValue(ext).getOctetString()).getReasonCode();
        } catch (IOException e) {
            return null;
        }
    }

    public Integer getReasonCode() throws IOException {
        Object obj = getExtension(PKIXExtensions.ReasonCode_Id);
        if (obj == null) {
            return null;
        }
        return ((CRLReasonCodeExtension) obj).get(CRLReasonCodeExtension.REASON);
    }

    public String toString() {
        Extension[] exts;
        StringBuilder sb = new StringBuilder();
        sb.append(this.serialNumber.toString());
        sb.append("  On: " + this.revocationDate.toString());
        if (this.certIssuer != null) {
            sb.append("\n    Certificate issuer: " + this.certIssuer);
        }
        if (this.extensions != null) {
            sb.append("\n    CRL Entry Extensions: " + ((Extension[]) this.extensions.getAllExtensions().toArray(new Extension[0])).length);
            for (Extension ext : exts) {
                sb.append("\n    [" + (i + 1) + "]: ");
                try {
                    if (OIDMap.getClass(ext.getExtensionId()) == null) {
                        sb.append(ext.toString());
                        byte[] extValue = ext.getExtensionValue();
                        if (extValue != null) {
                            DerOutputStream out = new DerOutputStream();
                            out.putOctetString(extValue);
                            byte[] extValue2 = out.toByteArray();
                            new HexDumpEncoder();
                            sb.append("Extension unknown: DER encoded OCTET string =\n" + enc.encodeBuffer(extValue2) + "\n");
                        }
                    } else {
                        sb.append(ext.toString());
                    }
                } catch (Exception e) {
                    sb.append(", Error parsing this extension");
                }
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    public boolean hasUnsupportedCriticalExtension() {
        if (this.extensions == null) {
            return isExplicit;
        }
        return this.extensions.hasUnsupportedCriticalExtension();
    }

    public Set<String> getCriticalExtensionOIDs() {
        if (this.extensions == null) {
            return null;
        }
        Set<String> extSet = new TreeSet<>();
        for (Extension ex : this.extensions.getAllExtensions()) {
            if (ex.isCritical()) {
                extSet.add(ex.getExtensionId().toString());
            }
        }
        return extSet;
    }

    public Set<String> getNonCriticalExtensionOIDs() {
        if (this.extensions == null) {
            return null;
        }
        Set<String> extSet = new TreeSet<>();
        for (Extension ex : this.extensions.getAllExtensions()) {
            if (!ex.isCritical()) {
                extSet.add(ex.getExtensionId().toString());
            }
        }
        return extSet;
    }

    public byte[] getExtensionValue(String oid) {
        if (this.extensions == null) {
            return null;
        }
        try {
            String extAlias = OIDMap.getName(new ObjectIdentifier(oid));
            Extension crlExt = null;
            if (extAlias == null) {
                ObjectIdentifier findOID = new ObjectIdentifier(oid);
                Enumeration<Extension> e = this.extensions.getElements();
                while (true) {
                    if (!e.hasMoreElements()) {
                        break;
                    }
                    Extension ex = e.nextElement();
                    if (ex.getExtensionId().equals((Object) findOID)) {
                        crlExt = ex;
                        break;
                    }
                }
            } else {
                crlExt = this.extensions.get(extAlias);
            }
            if (crlExt == null) {
                return null;
            }
            byte[] extData = crlExt.getExtensionValue();
            if (extData == null) {
                return null;
            }
            DerOutputStream out = new DerOutputStream();
            out.putOctetString(extData);
            return out.toByteArray();
        } catch (Exception e2) {
            return null;
        }
    }

    public Extension getExtension(ObjectIdentifier oid) {
        if (this.extensions == null) {
            return null;
        }
        return this.extensions.get(OIDMap.getName(oid));
    }

    private void parse(DerValue derVal) throws CRLException, IOException {
        if (derVal.tag != 48) {
            throw new CRLException("Invalid encoded RevokedCertificate, starting sequence tag missing.");
        } else if (derVal.data.available() != 0) {
            this.revokedCert = derVal.toByteArray();
            this.serialNumber = new SerialNumber(derVal.toDerInputStream().getDerValue());
            int nextByte = derVal.data.peekByte();
            if (((byte) nextByte) == 23) {
                this.revocationDate = derVal.data.getUTCTime();
            } else if (((byte) nextByte) == 24) {
                this.revocationDate = derVal.data.getGeneralizedTime();
            } else {
                throw new CRLException("Invalid encoding for revocation date");
            }
            if (derVal.data.available() != 0) {
                this.extensions = new CRLExtensions(derVal.toDerInputStream());
            }
        } else {
            throw new CRLException("No data encoded for RevokedCertificates");
        }
    }

    public static X509CRLEntryImpl toImpl(X509CRLEntry entry) throws CRLException {
        if (entry instanceof X509CRLEntryImpl) {
            return (X509CRLEntryImpl) entry;
        }
        return new X509CRLEntryImpl(entry.getEncoded());
    }

    /* access modifiers changed from: package-private */
    public CertificateIssuerExtension getCertificateIssuerExtension() {
        return (CertificateIssuerExtension) getExtension(PKIXExtensions.CertificateIssuer_Id);
    }

    public Map<String, Extension> getExtensions() {
        if (this.extensions == null) {
            return Collections.emptyMap();
        }
        Collection<Extension> exts = this.extensions.getAllExtensions();
        Map<String, Extension> map = new TreeMap<>();
        for (Extension ext : exts) {
            map.put(ext.getId(), ext);
        }
        return map;
    }

    public int compareTo(X509CRLEntryImpl that) {
        int compSerial = getSerialNumber().compareTo(that.getSerialNumber());
        if (compSerial != 0) {
            return compSerial;
        }
        try {
            byte[] thisEncoded = getEncoded0();
            byte[] thatEncoded = that.getEncoded0();
            int i = 0;
            while (i < thisEncoded.length && i < thatEncoded.length) {
                int a = thisEncoded[i] & 255;
                int b = thatEncoded[i] & 255;
                if (a != b) {
                    return a - b;
                }
                i++;
            }
            return thisEncoded.length - thatEncoded.length;
        } catch (CRLException e) {
            return -1;
        }
    }
}
