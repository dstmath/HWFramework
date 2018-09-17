package sun.security.x509;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class CertificateSubjectUniqueIdentity implements CertAttrSet<String> {
    public static final String ID = "id";
    public static final String IDENT = "x509.info.subjectID";
    public static final String NAME = "subjectID";
    private UniqueIdentity id;

    public CertificateSubjectUniqueIdentity(UniqueIdentity id) {
        this.id = id;
    }

    public CertificateSubjectUniqueIdentity(DerInputStream in) throws IOException {
        this.id = new UniqueIdentity(in);
    }

    public CertificateSubjectUniqueIdentity(InputStream in) throws IOException {
        this.id = new UniqueIdentity(new DerValue(in));
    }

    public CertificateSubjectUniqueIdentity(DerValue val) throws IOException {
        this.id = new UniqueIdentity(val);
    }

    public String toString() {
        if (this.id == null) {
            return "";
        }
        return this.id.toString();
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        this.id.encode(tmp, DerValue.createTag(DerValue.TAG_CONTEXT, false, (byte) 2));
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof UniqueIdentity)) {
            throw new IOException("Attribute must be of type UniqueIdentity.");
        } else if (name.equalsIgnoreCase(ID)) {
            this.id = (UniqueIdentity) obj;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet: CertificateSubjectUniqueIdentity.");
        }
    }

    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(ID)) {
            return this.id;
        }
        throw new IOException("Attribute name not recognized by CertAttrSet: CertificateSubjectUniqueIdentity.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(ID)) {
            this.id = null;
            return;
        }
        throw new IOException("Attribute name not recognized by CertAttrSet: CertificateSubjectUniqueIdentity.");
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(ID);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }
}
