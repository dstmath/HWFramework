package sun.security.pkcs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Hashtable;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class PKCS9Attributes {
    private final Hashtable<ObjectIdentifier, PKCS9Attribute> attributes;
    private final byte[] derEncoding;
    private boolean ignoreUnsupportedAttributes;
    private final Hashtable<ObjectIdentifier, ObjectIdentifier> permittedAttributes;

    public PKCS9Attributes(ObjectIdentifier[] permittedAttributes, DerInputStream in) throws IOException {
        this.attributes = new Hashtable(3);
        this.ignoreUnsupportedAttributes = false;
        if (permittedAttributes != null) {
            this.permittedAttributes = new Hashtable(permittedAttributes.length);
            for (int i = 0; i < permittedAttributes.length; i++) {
                this.permittedAttributes.put(permittedAttributes[i], permittedAttributes[i]);
            }
        } else {
            this.permittedAttributes = null;
        }
        this.derEncoding = decode(in);
    }

    public PKCS9Attributes(DerInputStream in) throws IOException {
        this(in, false);
    }

    public PKCS9Attributes(DerInputStream in, boolean ignoreUnsupportedAttributes) throws IOException {
        this.attributes = new Hashtable(3);
        this.ignoreUnsupportedAttributes = false;
        this.ignoreUnsupportedAttributes = ignoreUnsupportedAttributes;
        this.derEncoding = decode(in);
        this.permittedAttributes = null;
    }

    public PKCS9Attributes(PKCS9Attribute[] attribs) throws IllegalArgumentException, IOException {
        this.attributes = new Hashtable(3);
        this.ignoreUnsupportedAttributes = false;
        for (int i = 0; i < attribs.length; i++) {
            ObjectIdentifier oid = attribs[i].getOID();
            if (this.attributes.containsKey(oid)) {
                throw new IllegalArgumentException("PKCSAttribute " + attribs[i].getOID() + " duplicated while constructing " + "PKCS9Attributes.");
            }
            this.attributes.put(oid, attribs[i]);
        }
        this.derEncoding = generateDerEncoding();
        this.permittedAttributes = null;
    }

    private byte[] decode(DerInputStream in) throws IOException {
        byte[] derEncoding = in.getDerValue().toByteArray();
        derEncoding[0] = (byte) 49;
        DerValue[] derVals = new DerInputStream(derEncoding).getSet(3, true);
        boolean reuseEncoding = true;
        int i = 0;
        while (i < derVals.length) {
            try {
                PKCS9Attribute attrib = new PKCS9Attribute(derVals[i]);
                Object oid = attrib.getOID();
                if (this.attributes.get(oid) != null) {
                    throw new IOException("Duplicate PKCS9 attribute: " + oid);
                } else if (this.permittedAttributes == null || (this.permittedAttributes.containsKey(oid) ^ 1) == 0) {
                    this.attributes.put(oid, attrib);
                    i++;
                } else {
                    throw new IOException("Attribute " + oid + " not permitted in this attribute set");
                }
            } catch (ParsingException e) {
                if (this.ignoreUnsupportedAttributes) {
                    reuseEncoding = false;
                } else {
                    throw e;
                }
            }
        }
        return reuseEncoding ? derEncoding : generateDerEncoding();
    }

    public void encode(byte tag, OutputStream out) throws IOException {
        out.write((int) tag);
        out.write(this.derEncoding, 1, this.derEncoding.length - 1);
    }

    private byte[] generateDerEncoding() throws IOException {
        DerOutputStream out = new DerOutputStream();
        out.putOrderedSetOf((byte) 49, castToDerEncoder(this.attributes.values().toArray()));
        return out.toByteArray();
    }

    public byte[] getDerEncoding() throws IOException {
        return (byte[]) this.derEncoding.clone();
    }

    public PKCS9Attribute getAttribute(ObjectIdentifier oid) {
        return (PKCS9Attribute) this.attributes.get(oid);
    }

    public PKCS9Attribute getAttribute(String name) {
        return (PKCS9Attribute) this.attributes.get(PKCS9Attribute.getOID(name));
    }

    public PKCS9Attribute[] getAttributes() {
        PKCS9Attribute[] attribs = new PKCS9Attribute[this.attributes.size()];
        int j = 0;
        for (int i = 1; i < PKCS9Attribute.PKCS9_OIDS.length && j < attribs.length; i++) {
            attribs[j] = getAttribute(PKCS9Attribute.PKCS9_OIDS[i]);
            if (attribs[j] != null) {
                j++;
            }
        }
        return attribs;
    }

    public Object getAttributeValue(ObjectIdentifier oid) throws IOException {
        try {
            return getAttribute(oid).getValue();
        } catch (NullPointerException e) {
            throw new IOException("No value found for attribute " + oid);
        }
    }

    public Object getAttributeValue(String name) throws IOException {
        ObjectIdentifier oid = PKCS9Attribute.getOID(name);
        if (oid != null) {
            return getAttributeValue(oid);
        }
        throw new IOException("Attribute name " + name + " not recognized or not supported.");
    }

    public String toString() {
        StringBuffer buf = new StringBuffer((int) HttpURLConnection.HTTP_OK);
        buf.append("PKCS9 Attributes: [\n\t");
        boolean first = true;
        for (int i = 1; i < PKCS9Attribute.PKCS9_OIDS.length; i++) {
            PKCS9Attribute value = getAttribute(PKCS9Attribute.PKCS9_OIDS[i]);
            if (value != null) {
                if (first) {
                    first = false;
                } else {
                    buf.append(";\n\t");
                }
                buf.append(value.toString());
            }
        }
        buf.append("\n\t] (end PKCS9 Attributes)");
        return buf.toString();
    }

    static DerEncoder[] castToDerEncoder(Object[] objs) {
        DerEncoder[] encoders = new DerEncoder[objs.length];
        for (int i = 0; i < encoders.length; i++) {
            encoders[i] = (DerEncoder) objs[i];
        }
        return encoders;
    }
}
