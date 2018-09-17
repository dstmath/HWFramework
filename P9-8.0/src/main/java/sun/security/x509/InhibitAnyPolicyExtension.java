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
    private int skipCerts;

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

    public InhibitAnyPolicyExtension(int skipCerts) throws IOException {
        this.skipCerts = Integer.MAX_VALUE;
        if (skipCerts < -1) {
            throw new IOException("Invalid value for skipCerts");
        }
        if (skipCerts == -1) {
            this.skipCerts = Integer.MAX_VALUE;
        } else {
            this.skipCerts = skipCerts;
        }
        this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
        this.critical = true;
        encodeThis();
    }

    public InhibitAnyPolicyExtension(Boolean critical, Object value) throws IOException {
        this.skipCerts = Integer.MAX_VALUE;
        this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
        if (critical.booleanValue()) {
            this.critical = critical.booleanValue();
            this.extensionValue = (byte[]) value;
            DerValue val = new DerValue(this.extensionValue);
            if (val.tag != (byte) 2) {
                throw new IOException("Invalid encoding of InhibitAnyPolicy: data not integer");
            } else if (val.data == null) {
                throw new IOException("Invalid encoding of InhibitAnyPolicy: null data");
            } else {
                int skipCertsValue = val.getInteger();
                if (skipCertsValue < -1) {
                    throw new IOException("Invalid value for skipCerts");
                } else if (skipCertsValue == -1) {
                    this.skipCerts = Integer.MAX_VALUE;
                    return;
                } else {
                    this.skipCerts = skipCertsValue;
                    return;
                }
            }
        }
        throw new IOException("Criticality cannot be false for InhibitAnyPolicy");
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
            int skipCertsValue = ((Integer) obj).lambda$-java_util_stream_IntPipeline_14709();
            if (skipCertsValue < -1) {
                throw new IOException("Invalid value for skipCerts");
            }
            if (skipCertsValue == -1) {
                this.skipCerts = Integer.MAX_VALUE;
            } else {
                this.skipCerts = skipCertsValue;
            }
            encodeThis();
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
