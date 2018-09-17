package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.util.Date;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PrivateKeyUsageExtension extends Extension implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.PrivateKeyUsage";
    public static final String NAME = "PrivateKeyUsage";
    public static final String NOT_AFTER = "not_after";
    public static final String NOT_BEFORE = "not_before";
    private static final byte TAG_AFTER = (byte) 1;
    private static final byte TAG_BEFORE = (byte) 0;
    private Date notAfter;
    private Date notBefore;

    private void encodeThis() throws IOException {
        if (this.notBefore == null && this.notAfter == null) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream tmp;
        DerOutputStream seq = new DerOutputStream();
        DerOutputStream tagged = new DerOutputStream();
        if (this.notBefore != null) {
            tmp = new DerOutputStream();
            tmp.putGeneralizedTime(this.notBefore);
            tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, false, (byte) 0), tmp);
        }
        if (this.notAfter != null) {
            tmp = new DerOutputStream();
            tmp.putGeneralizedTime(this.notAfter);
            tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, false, (byte) 1), tmp);
        }
        seq.write((byte) 48, tagged);
        this.extensionValue = seq.toByteArray();
    }

    public PrivateKeyUsageExtension(Date notBefore, Date notAfter) throws IOException {
        this.notBefore = null;
        this.notAfter = null;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
        this.critical = false;
        encodeThis();
    }

    public PrivateKeyUsageExtension(Boolean critical, Object value) throws CertificateException, IOException {
        this.notBefore = null;
        this.notAfter = null;
        this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue[] seq = new DerInputStream(this.extensionValue).getSequence(2);
        for (DerValue opt : seq) {
            if (!opt.isContextSpecific((byte) 0) || (opt.isConstructed() ^ 1) == 0) {
                if (!opt.isContextSpecific((byte) 1) || (opt.isConstructed() ^ 1) == 0) {
                    throw new IOException("Invalid encoding of PrivateKeyUsageExtension");
                } else if (this.notAfter != null) {
                    throw new CertificateParsingException("Duplicate notAfter in PrivateKeyUsage.");
                } else {
                    opt.resetTag((byte) 24);
                    this.notAfter = new DerInputStream(opt.toByteArray()).getGeneralizedTime();
                }
            } else if (this.notBefore != null) {
                throw new CertificateParsingException("Duplicate notBefore in PrivateKeyUsage.");
            } else {
                opt.resetTag((byte) 24);
                this.notBefore = new DerInputStream(opt.toByteArray()).getGeneralizedTime();
            }
        }
    }

    public String toString() {
        return super.toString() + "PrivateKeyUsage: [\n" + (this.notBefore == null ? "" : "From: " + this.notBefore.toString() + ", ") + (this.notAfter == null ? "" : "To: " + this.notAfter.toString()) + "]\n";
    }

    public void valid() throws CertificateNotYetValidException, CertificateExpiredException {
        valid(new Date());
    }

    public void valid(Date now) throws CertificateNotYetValidException, CertificateExpiredException {
        if (this.notBefore.after(now)) {
            throw new CertificateNotYetValidException("NotBefore: " + this.notBefore.toString());
        } else if (this.notAfter.before(now)) {
            throw new CertificateExpiredException("NotAfter: " + this.notAfter.toString());
        }
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.PrivateKeyUsage_Id;
            this.critical = false;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws CertificateException, IOException {
        if (obj instanceof Date) {
            if (name.equalsIgnoreCase(NOT_BEFORE)) {
                this.notBefore = (Date) obj;
            } else if (name.equalsIgnoreCase(NOT_AFTER)) {
                this.notAfter = (Date) obj;
            } else {
                throw new CertificateException("Attribute name not recognized by CertAttrSet:PrivateKeyUsage.");
            }
            encodeThis();
            return;
        }
        throw new CertificateException("Attribute must be of type Date.");
    }

    public Date get(String name) throws CertificateException {
        if (name.equalsIgnoreCase(NOT_BEFORE)) {
            return new Date(this.notBefore.getTime());
        }
        if (name.equalsIgnoreCase(NOT_AFTER)) {
            return new Date(this.notAfter.getTime());
        }
        throw new CertificateException("Attribute name not recognized by CertAttrSet:PrivateKeyUsage.");
    }

    public void delete(String name) throws CertificateException, IOException {
        if (name.equalsIgnoreCase(NOT_BEFORE)) {
            this.notBefore = null;
        } else if (name.equalsIgnoreCase(NOT_AFTER)) {
            this.notAfter = null;
        } else {
            throw new CertificateException("Attribute name not recognized by CertAttrSet:PrivateKeyUsage.");
        }
        encodeThis();
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(NOT_BEFORE);
        elements.addElement(NOT_AFTER);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }
}
