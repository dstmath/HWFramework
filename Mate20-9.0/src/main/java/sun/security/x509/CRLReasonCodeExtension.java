package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CRLReason;
import java.util.Enumeration;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CRLReasonCodeExtension extends Extension implements CertAttrSet<String> {
    public static final String NAME = "CRLReasonCode";
    public static final String REASON = "reason";
    private static CRLReason[] values = CRLReason.values();
    private int reasonCode;

    private void encodeThis() throws IOException {
        if (this.reasonCode == 0) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream dos = new DerOutputStream();
        dos.putEnumerated(this.reasonCode);
        this.extensionValue = dos.toByteArray();
    }

    public CRLReasonCodeExtension(int reason) throws IOException {
        this(false, reason);
    }

    public CRLReasonCodeExtension(boolean critical, int reason) throws IOException {
        this.reasonCode = 0;
        this.extensionId = PKIXExtensions.ReasonCode_Id;
        this.critical = critical;
        this.reasonCode = reason;
        encodeThis();
    }

    public CRLReasonCodeExtension(Boolean critical, Object value) throws IOException {
        this.reasonCode = 0;
        this.extensionId = PKIXExtensions.ReasonCode_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        this.reasonCode = new DerValue(this.extensionValue).getEnumerated();
    }

    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof Integer)) {
            throw new IOException("Attribute must be of type Integer.");
        } else if (name.equalsIgnoreCase(REASON)) {
            this.reasonCode = ((Integer) obj).intValue();
            encodeThis();
        } else {
            throw new IOException("Name not supported by CRLReasonCodeExtension");
        }
    }

    public Integer get(String name) throws IOException {
        if (name.equalsIgnoreCase(REASON)) {
            return new Integer(this.reasonCode);
        }
        throw new IOException("Name not supported by CRLReasonCodeExtension");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(REASON)) {
            this.reasonCode = 0;
            encodeThis();
            return;
        }
        throw new IOException("Name not supported by CRLReasonCodeExtension");
    }

    public String toString() {
        return super.toString() + "    Reason Code: " + getReasonCode();
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.ReasonCode_Id;
            this.critical = false;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(REASON);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }

    public CRLReason getReasonCode() {
        if (this.reasonCode <= 0 || this.reasonCode >= values.length) {
            return CRLReason.UNSPECIFIED;
        }
        return values[this.reasonCode];
    }
}
