package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateVersion implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.version";
    public static final String NAME = "version";
    public static final int V1 = 0;
    public static final int V2 = 1;
    public static final int V3 = 2;
    public static final String VERSION = "number";
    int version;

    private int getVersion() {
        return this.version;
    }

    private void construct(DerValue derVal) throws IOException {
        if (derVal.isConstructed() && derVal.isContextSpecific()) {
            DerValue derVal2 = derVal.data.getDerValue();
            this.version = derVal2.getInteger();
            if (derVal2.data.available() != 0) {
                throw new IOException("X.509 version, bad format");
            }
        }
    }

    public CertificateVersion() {
        this.version = 0;
        this.version = 0;
    }

    public CertificateVersion(int version2) throws IOException {
        this.version = 0;
        if (version2 == 0 || version2 == 1 || version2 == 2) {
            this.version = version2;
            return;
        }
        throw new IOException("X.509 Certificate version " + version2 + " not supported.\n");
    }

    public CertificateVersion(DerInputStream in) throws IOException {
        this.version = 0;
        this.version = 0;
        construct(in.getDerValue());
    }

    public CertificateVersion(InputStream in) throws IOException {
        this.version = 0;
        this.version = 0;
        construct(new DerValue(in));
    }

    public CertificateVersion(DerValue val) throws IOException {
        this.version = 0;
        this.version = 0;
        construct(val);
    }

    public String toString() {
        return "Version: V" + (this.version + 1);
    }

    public void encode(OutputStream out) throws IOException {
        if (this.version != 0) {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putInteger(this.version);
            DerOutputStream seq = new DerOutputStream();
            seq.write(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 0), tmp);
            out.write(seq.toByteArray());
        }
    }

    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof Integer)) {
            throw new IOException("Attribute must be of type Integer.");
        } else if (name.equalsIgnoreCase("number")) {
            this.version = ((Integer) obj).intValue();
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet: CertificateVersion.");
        }
    }

    public Integer get(String name) throws IOException {
        if (name.equalsIgnoreCase("number")) {
            return new Integer(getVersion());
        }
        throw new IOException("Attribute name not recognized by CertAttrSet: CertificateVersion.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase("number")) {
            this.version = 0;
            return;
        }
        throw new IOException("Attribute name not recognized by CertAttrSet: CertificateVersion.");
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement("number");
        return elements.elements();
    }

    public String getName() {
        return "version";
    }

    public int compare(int vers) {
        return this.version - vers;
    }
}
