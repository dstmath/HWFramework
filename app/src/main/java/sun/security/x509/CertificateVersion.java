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
            derVal = derVal.data.getDerValue();
            this.version = derVal.getInteger();
            if (derVal.data.available() != 0) {
                throw new IOException("X.509 version, bad format");
            }
        }
    }

    public CertificateVersion() {
        this.version = V1;
        this.version = V1;
    }

    public CertificateVersion(int version) throws IOException {
        this.version = V1;
        if (version == 0 || version == V2 || version == V3) {
            this.version = version;
            return;
        }
        throw new IOException("X.509 Certificate version " + version + " not supported.\n");
    }

    public CertificateVersion(DerInputStream in) throws IOException {
        this.version = V1;
        this.version = V1;
        construct(in.getDerValue());
    }

    public CertificateVersion(InputStream in) throws IOException {
        this.version = V1;
        this.version = V1;
        construct(new DerValue(in));
    }

    public CertificateVersion(DerValue val) throws IOException {
        this.version = V1;
        this.version = V1;
        construct(val);
    }

    public String toString() {
        return "Version: V" + (this.version + V2);
    }

    public void encode(OutputStream out) throws IOException {
        if (this.version != 0) {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putInteger(this.version);
            DerOutputStream seq = new DerOutputStream();
            seq.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0), tmp);
            out.write(seq.toByteArray());
        }
    }

    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof Integer)) {
            throw new IOException("Attribute must be of type Integer.");
        } else if (name.equalsIgnoreCase(VERSION)) {
            this.version = ((Integer) obj).intValue();
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet: CertificateVersion.");
        }
    }

    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(VERSION)) {
            return new Integer(getVersion());
        }
        throw new IOException("Attribute name not recognized by CertAttrSet: CertificateVersion.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(VERSION)) {
            this.version = V1;
            return;
        }
        throw new IOException("Attribute name not recognized by CertAttrSet: CertificateVersion.");
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(VERSION);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }

    public int compare(int vers) {
        return this.version - vers;
    }
}
