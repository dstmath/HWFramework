package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class PolicyConstraintsExtension extends Extension implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.PolicyConstraints";
    public static final String INHIBIT = "inhibit";
    public static final String NAME = "PolicyConstraints";
    public static final String REQUIRE = "require";
    private static final byte TAG_INHIBIT = 1;
    private static final byte TAG_REQUIRE = 0;
    private int inhibit;
    private int require;

    private void encodeThis() throws IOException {
        if (this.require == -1 && this.inhibit == -1) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream tagged = new DerOutputStream();
        DerOutputStream seq = new DerOutputStream();
        if (this.require != -1) {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putInteger(this.require);
            tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, false, (byte) 0), tmp);
        }
        if (this.inhibit != -1) {
            DerOutputStream tmp2 = new DerOutputStream();
            tmp2.putInteger(this.inhibit);
            tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, false, (byte) 1), tmp2);
        }
        seq.write((byte) 48, tagged);
        this.extensionValue = seq.toByteArray();
    }

    public PolicyConstraintsExtension(int require2, int inhibit2) throws IOException {
        this(Boolean.FALSE, require2, inhibit2);
    }

    public PolicyConstraintsExtension(Boolean critical, int require2, int inhibit2) throws IOException {
        this.require = -1;
        this.inhibit = -1;
        this.require = require2;
        this.inhibit = inhibit2;
        this.extensionId = PKIXExtensions.PolicyConstraints_Id;
        this.critical = critical.booleanValue();
        encodeThis();
    }

    public PolicyConstraintsExtension(Boolean critical, Object value) throws IOException {
        this.require = -1;
        this.inhibit = -1;
        this.extensionId = PKIXExtensions.PolicyConstraints_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag == 48) {
            DerInputStream in = val.data;
            while (in != null && in.available() != 0) {
                DerValue next = in.getDerValue();
                if (!next.isContextSpecific((byte) 0) || next.isConstructed()) {
                    if (!next.isContextSpecific((byte) 1) || next.isConstructed()) {
                        throw new IOException("Invalid encoding of PolicyConstraint");
                    } else if (this.inhibit == -1) {
                        next.resetTag((byte) 2);
                        this.inhibit = next.getInteger();
                    } else {
                        throw new IOException("Duplicate inhibitPolicyMappingfound in the PolicyConstraintsExtension");
                    }
                } else if (this.require == -1) {
                    next.resetTag((byte) 2);
                    this.require = next.getInteger();
                } else {
                    throw new IOException("Duplicate requireExplicitPolicyfound in the PolicyConstraintsExtension");
                }
            }
            return;
        }
        throw new IOException("Sequence tag missing for PolicyConstraint.");
    }

    public String toString() {
        String s;
        String s2;
        String s3 = super.toString() + "PolicyConstraints: [  Require: ";
        if (this.require == -1) {
            s = s3 + "unspecified;";
        } else {
            s = s3 + this.require + ";";
        }
        String s4 = s + "\tInhibit: ";
        if (this.inhibit == -1) {
            s2 = s4 + "unspecified";
        } else {
            s2 = s4 + this.inhibit;
        }
        return s2 + " ]\n";
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.PolicyConstraints_Id;
            this.critical = false;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (obj instanceof Integer) {
            if (name.equalsIgnoreCase(REQUIRE)) {
                this.require = ((Integer) obj).intValue();
            } else if (name.equalsIgnoreCase(INHIBIT)) {
                this.inhibit = ((Integer) obj).intValue();
            } else {
                throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:PolicyConstraints.");
            }
            encodeThis();
            return;
        }
        throw new IOException("Attribute value should be of type Integer.");
    }

    public Integer get(String name) throws IOException {
        if (name.equalsIgnoreCase(REQUIRE)) {
            return new Integer(this.require);
        }
        if (name.equalsIgnoreCase(INHIBIT)) {
            return new Integer(this.inhibit);
        }
        throw new IOException("Attribute name not recognized by CertAttrSet:PolicyConstraints.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(REQUIRE)) {
            this.require = -1;
        } else if (name.equalsIgnoreCase(INHIBIT)) {
            this.inhibit = -1;
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:PolicyConstraints.");
        }
        encodeThis();
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(REQUIRE);
        elements.addElement(INHIBIT);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }
}
