package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.Debug;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class InhibitAnyPolicyExtension extends Extension implements CertAttrSet<String> {
    public static ObjectIdentifier AnyPolicy_Id = null;
    public static final String IDENT = "x509.info.extensions.InhibitAnyPolicy";
    public static final String NAME = "InhibitAnyPolicy";
    public static final String SKIP_CERTS = "skip_certs";
    private static final Debug debug = Debug.getInstance("certpath");
    private int skipCerts = Integer.MAX_VALUE;

    static {
        try {
            AnyPolicy_Id = new ObjectIdentifier("2.5.29.32.0");
        } catch (IOException e) {
        }
    }

    private void encodeThis() throws IOException {
        DerOutputStream out = new DerOutputStream();
        out.putInteger(this.skipCerts);
        this.extensionValue = out.toByteArray();
    }

    public InhibitAnyPolicyExtension(int skipCerts2) throws IOException {
        if (skipCerts2 >= -1) {
            if (skipCerts2 == -1) {
                this.skipCerts = Integer.MAX_VALUE;
            } else {
                this.skipCerts = skipCerts2;
            }
            this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
            this.critical = true;
            encodeThis();
            return;
        }
        throw new IOException("Invalid value for skipCerts");
    }

    public InhibitAnyPolicyExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
        if (critical.booleanValue()) {
            this.critical = critical.booleanValue();
            this.extensionValue = (byte[]) value;
            DerValue val = new DerValue(this.extensionValue);
            if (val.tag != 2) {
                throw new IOException("Invalid encoding of InhibitAnyPolicy: data not integer");
            } else if (val.data != null) {
                int skipCertsValue = val.getInteger();
                if (skipCertsValue < -1) {
                    throw new IOException("Invalid value for skipCerts");
                } else if (skipCertsValue == -1) {
                    this.skipCerts = Integer.MAX_VALUE;
                } else {
                    this.skipCerts = skipCertsValue;
                }
            } else {
                throw new IOException("Invalid encoding of InhibitAnyPolicy: null data");
            }
        } else {
            throw new IOException("Criticality cannot be false for InhibitAnyPolicy");
        }
    }

    public String toString() {
        return super.toString() + "InhibitAnyPolicy: " + this.skipCerts + "\n";
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
            this.critical = true;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (!name.equalsIgnoreCase(SKIP_CERTS)) {
            throw new IOException("Attribute name not recognized by CertAttrSet:InhibitAnyPolicy.");
        } else if (obj instanceof Integer) {
            int skipCertsValue = ((Integer) obj).intValue();
            if (skipCertsValue >= -1) {
                if (skipCertsValue == -1) {
                    this.skipCerts = Integer.MAX_VALUE;
                } else {
                    this.skipCerts = skipCertsValue;
                }
                encodeThis();
                return;
            }
            throw new IOException("Invalid value for skipCerts");
        } else {
            throw new IOException("Attribute value should be of type Integer.");
        }
    }

    public Integer get(String name) throws IOException {
        if (name.equalsIgnoreCase(SKIP_CERTS)) {
            return new Integer(this.skipCerts);
        }
        throw new IOException("Attribute name not recognized by CertAttrSet:InhibitAnyPolicy.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(SKIP_CERTS)) {
            throw new IOException("Attribute skip_certs may not be deleted.");
        }
        throw new IOException("Attribute name not recognized by CertAttrSet:InhibitAnyPolicy.");
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(SKIP_CERTS);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }
}
