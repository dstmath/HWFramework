package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Enumeration;
import sun.security.util.Debug;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class CRLNumberExtension extends Extension implements CertAttrSet<String> {
    private static final String LABEL = "CRL Number";
    public static final String NAME = "CRLNumber";
    public static final String NUMBER = "value";
    private BigInteger crlNumber;
    private String extensionLabel;
    private String extensionName;

    private void encodeThis() throws IOException {
        if (this.crlNumber == null) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream os = new DerOutputStream();
        os.putInteger(this.crlNumber);
        this.extensionValue = os.toByteArray();
    }

    public CRLNumberExtension(int crlNum) throws IOException {
        this(PKIXExtensions.CRLNumber_Id, false, BigInteger.valueOf((long) crlNum), NAME, LABEL);
    }

    public CRLNumberExtension(BigInteger crlNum) throws IOException {
        this(PKIXExtensions.CRLNumber_Id, false, crlNum, NAME, LABEL);
    }

    protected CRLNumberExtension(ObjectIdentifier extensionId, boolean isCritical, BigInteger crlNum, String extensionName, String extensionLabel) throws IOException {
        this.crlNumber = null;
        this.extensionId = extensionId;
        this.critical = isCritical;
        this.crlNumber = crlNum;
        this.extensionName = extensionName;
        this.extensionLabel = extensionLabel;
        encodeThis();
    }

    public CRLNumberExtension(Boolean critical, Object value) throws IOException {
        this(PKIXExtensions.CRLNumber_Id, critical, value, NAME, LABEL);
    }

    protected CRLNumberExtension(ObjectIdentifier extensionId, Boolean critical, Object value, String extensionName, String extensionLabel) throws IOException {
        this.crlNumber = null;
        this.extensionId = extensionId;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        this.crlNumber = new DerValue(this.extensionValue).getBigInteger();
        this.extensionName = extensionName;
        this.extensionLabel = extensionLabel;
    }

    public void set(String name, Object obj) throws IOException {
        if (!name.equalsIgnoreCase("value")) {
            throw new IOException("Attribute name not recognized by CertAttrSet:" + this.extensionName + ".");
        } else if (obj instanceof BigInteger) {
            this.crlNumber = (BigInteger) obj;
            encodeThis();
        } else {
            throw new IOException("Attribute must be of type BigInteger.");
        }
    }

    public BigInteger get(String name) throws IOException {
        if (!name.equalsIgnoreCase("value")) {
            throw new IOException("Attribute name not recognized by CertAttrSet:" + this.extensionName + ".");
        } else if (this.crlNumber == null) {
            return null;
        } else {
            return this.crlNumber;
        }
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase("value")) {
            this.crlNumber = null;
            encodeThis();
            return;
        }
        throw new IOException("Attribute name not recognized by CertAttrSet:" + this.extensionName + ".");
    }

    public String toString() {
        return super.toString() + this.extensionLabel + ": " + (this.crlNumber == null ? "" : Debug.toHexString(this.crlNumber)) + "\n";
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        encode(out, PKIXExtensions.CRLNumber_Id, true);
    }

    protected void encode(OutputStream out, ObjectIdentifier extensionId, boolean isCritical) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = extensionId;
            this.critical = isCritical;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement("value");
        return elements.elements();
    }

    public String getName() {
        return this.extensionName;
    }
}
