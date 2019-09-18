package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class BasicConstraintsExtension extends Extension implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.BasicConstraints";
    public static final String IS_CA = "is_ca";
    public static final String NAME = "BasicConstraints";
    public static final String PATH_LEN = "path_len";
    private boolean ca;
    private int pathLen;

    private void encodeThis() throws IOException {
        DerOutputStream out = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        if (this.ca) {
            tmp.putBoolean(this.ca);
            if (this.pathLen >= 0) {
                tmp.putInteger(this.pathLen);
            }
        }
        out.write((byte) 48, tmp);
        this.extensionValue = out.toByteArray();
    }

    public BasicConstraintsExtension(boolean ca2, int len) throws IOException {
        this(Boolean.valueOf(ca2), ca2, len);
    }

    public BasicConstraintsExtension(Boolean critical, boolean ca2, int len) throws IOException {
        this.ca = false;
        this.pathLen = -1;
        this.ca = ca2;
        this.pathLen = len;
        this.extensionId = PKIXExtensions.BasicConstraints_Id;
        this.critical = critical.booleanValue();
        encodeThis();
    }

    public BasicConstraintsExtension(Boolean critical, Object value) throws IOException {
        this.ca = false;
        this.pathLen = -1;
        this.extensionId = PKIXExtensions.BasicConstraints_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag != 48) {
            throw new IOException("Invalid encoding of BasicConstraints");
        } else if (val.data != null && val.data.available() != 0) {
            DerValue opt = val.data.getDerValue();
            if (opt.tag == 1) {
                this.ca = opt.getBoolean();
                if (val.data.available() == 0) {
                    this.pathLen = Integer.MAX_VALUE;
                    return;
                }
                DerValue opt2 = val.data.getDerValue();
                if (opt2.tag == 2) {
                    this.pathLen = opt2.getInteger();
                    return;
                }
                throw new IOException("Invalid encoding of BasicConstraints");
            }
        }
    }

    public String toString() {
        String s;
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString() + "BasicConstraints:[\n");
        sb.append(this.ca ? "  CA:true" : "  CA:false");
        sb.append("\n");
        String s2 = sb.toString();
        if (this.pathLen >= 0) {
            s = s2 + "  PathLen:" + this.pathLen + "\n";
        } else {
            s = s2 + "  PathLen: undefined\n";
        }
        return s + "]\n";
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.BasicConstraints_Id;
            if (this.ca) {
                this.critical = true;
            } else {
                this.critical = false;
            }
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(IS_CA)) {
            if (obj instanceof Boolean) {
                this.ca = ((Boolean) obj).booleanValue();
            } else {
                throw new IOException("Attribute value should be of type Boolean.");
            }
        } else if (!name.equalsIgnoreCase(PATH_LEN)) {
            throw new IOException("Attribute name not recognized by CertAttrSet:BasicConstraints.");
        } else if (obj instanceof Integer) {
            this.pathLen = ((Integer) obj).intValue();
        } else {
            throw new IOException("Attribute value should be of type Integer.");
        }
        encodeThis();
    }

    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(IS_CA)) {
            return Boolean.valueOf(this.ca);
        }
        if (name.equalsIgnoreCase(PATH_LEN)) {
            return Integer.valueOf(this.pathLen);
        }
        throw new IOException("Attribute name not recognized by CertAttrSet:BasicConstraints.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(IS_CA)) {
            this.ca = false;
        } else if (name.equalsIgnoreCase(PATH_LEN)) {
            this.pathLen = -1;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:BasicConstraints.");
        }
        encodeThis();
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(IS_CA);
        elements.addElement(PATH_LEN);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }
}
