package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class IssuingDistributionPointExtension extends Extension implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.IssuingDistributionPoint";
    public static final String INDIRECT_CRL = "indirect_crl";
    public static final String NAME = "IssuingDistributionPoint";
    public static final String ONLY_ATTRIBUTE_CERTS = "only_attribute_certs";
    public static final String ONLY_CA_CERTS = "only_ca_certs";
    public static final String ONLY_USER_CERTS = "only_user_certs";
    public static final String POINT = "point";
    public static final String REASONS = "reasons";
    private static final byte TAG_DISTRIBUTION_POINT = 0;
    private static final byte TAG_INDIRECT_CRL = 4;
    private static final byte TAG_ONLY_ATTRIBUTE_CERTS = 5;
    private static final byte TAG_ONLY_CA_CERTS = 2;
    private static final byte TAG_ONLY_SOME_REASONS = 3;
    private static final byte TAG_ONLY_USER_CERTS = 1;
    private DistributionPointName distributionPoint = null;
    private boolean hasOnlyAttributeCerts = false;
    private boolean hasOnlyCACerts = false;
    private boolean hasOnlyUserCerts = false;
    private boolean isIndirectCRL = false;
    private ReasonFlags revocationReasons = null;

    public IssuingDistributionPointExtension(DistributionPointName distributionPoint2, ReasonFlags revocationReasons2, boolean hasOnlyUserCerts2, boolean hasOnlyCACerts2, boolean hasOnlyAttributeCerts2, boolean isIndirectCRL2) throws IOException {
        if ((!hasOnlyUserCerts2 || (!hasOnlyCACerts2 && !hasOnlyAttributeCerts2)) && ((!hasOnlyCACerts2 || (!hasOnlyUserCerts2 && !hasOnlyAttributeCerts2)) && (!hasOnlyAttributeCerts2 || (!hasOnlyUserCerts2 && !hasOnlyCACerts2)))) {
            this.extensionId = PKIXExtensions.IssuingDistributionPoint_Id;
            this.critical = true;
            this.distributionPoint = distributionPoint2;
            this.revocationReasons = revocationReasons2;
            this.hasOnlyUserCerts = hasOnlyUserCerts2;
            this.hasOnlyCACerts = hasOnlyCACerts2;
            this.hasOnlyAttributeCerts = hasOnlyAttributeCerts2;
            this.isIndirectCRL = isIndirectCRL2;
            encodeThis();
            return;
        }
        throw new IllegalArgumentException("Only one of hasOnlyUserCerts, hasOnlyCACerts, hasOnlyAttributeCerts may be set to true");
    }

    public IssuingDistributionPointExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.IssuingDistributionPoint_Id;
        this.critical = critical.booleanValue();
        if (value instanceof byte[]) {
            this.extensionValue = (byte[]) value;
            DerValue val = new DerValue(this.extensionValue);
            if (val.tag != 48) {
                throw new IOException("Invalid encoding for IssuingDistributionPointExtension.");
            } else if (val.data != null && val.data.available() != 0) {
                DerInputStream in = val.data;
                while (in != null && in.available() != 0) {
                    DerValue opt = in.getDerValue();
                    if (opt.isContextSpecific((byte) 0) && opt.isConstructed()) {
                        this.distributionPoint = new DistributionPointName(opt.data.getDerValue());
                    } else if (opt.isContextSpecific((byte) 1) && !opt.isConstructed()) {
                        opt.resetTag((byte) 1);
                        this.hasOnlyUserCerts = opt.getBoolean();
                    } else if (opt.isContextSpecific((byte) 2) && !opt.isConstructed()) {
                        opt.resetTag((byte) 1);
                        this.hasOnlyCACerts = opt.getBoolean();
                    } else if (opt.isContextSpecific((byte) 3) && !opt.isConstructed()) {
                        this.revocationReasons = new ReasonFlags(opt);
                    } else if (opt.isContextSpecific((byte) 4) && !opt.isConstructed()) {
                        opt.resetTag((byte) 1);
                        this.isIndirectCRL = opt.getBoolean();
                    } else if (!opt.isContextSpecific((byte) 5) || opt.isConstructed()) {
                        throw new IOException("Invalid encoding of IssuingDistributionPoint");
                    } else {
                        opt.resetTag((byte) 1);
                        this.hasOnlyAttributeCerts = opt.getBoolean();
                    }
                }
            }
        } else {
            throw new IOException("Illegal argument type");
        }
    }

    public String getName() {
        return NAME;
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.IssuingDistributionPoint_Id;
            this.critical = false;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(POINT)) {
            if (obj instanceof DistributionPointName) {
                this.distributionPoint = (DistributionPointName) obj;
            } else {
                throw new IOException("Attribute value should be of type DistributionPointName.");
            }
        } else if (name.equalsIgnoreCase(REASONS)) {
            if (obj instanceof ReasonFlags) {
                this.revocationReasons = (ReasonFlags) obj;
            } else {
                throw new IOException("Attribute value should be of type ReasonFlags.");
            }
        } else if (name.equalsIgnoreCase(INDIRECT_CRL)) {
            if (obj instanceof Boolean) {
                this.isIndirectCRL = ((Boolean) obj).booleanValue();
            } else {
                throw new IOException("Attribute value should be of type Boolean.");
            }
        } else if (name.equalsIgnoreCase(ONLY_USER_CERTS)) {
            if (obj instanceof Boolean) {
                this.hasOnlyUserCerts = ((Boolean) obj).booleanValue();
            } else {
                throw new IOException("Attribute value should be of type Boolean.");
            }
        } else if (name.equalsIgnoreCase(ONLY_CA_CERTS)) {
            if (obj instanceof Boolean) {
                this.hasOnlyCACerts = ((Boolean) obj).booleanValue();
            } else {
                throw new IOException("Attribute value should be of type Boolean.");
            }
        } else if (!name.equalsIgnoreCase(ONLY_ATTRIBUTE_CERTS)) {
            throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:IssuingDistributionPointExtension.");
        } else if (obj instanceof Boolean) {
            this.hasOnlyAttributeCerts = ((Boolean) obj).booleanValue();
        } else {
            throw new IOException("Attribute value should be of type Boolean.");
        }
        encodeThis();
    }

    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(POINT)) {
            return this.distributionPoint;
        }
        if (name.equalsIgnoreCase(INDIRECT_CRL)) {
            return Boolean.valueOf(this.isIndirectCRL);
        }
        if (name.equalsIgnoreCase(REASONS)) {
            return this.revocationReasons;
        }
        if (name.equalsIgnoreCase(ONLY_USER_CERTS)) {
            return Boolean.valueOf(this.hasOnlyUserCerts);
        }
        if (name.equalsIgnoreCase(ONLY_CA_CERTS)) {
            return Boolean.valueOf(this.hasOnlyCACerts);
        }
        if (name.equalsIgnoreCase(ONLY_ATTRIBUTE_CERTS)) {
            return Boolean.valueOf(this.hasOnlyAttributeCerts);
        }
        throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:IssuingDistributionPointExtension.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(POINT)) {
            this.distributionPoint = null;
        } else if (name.equalsIgnoreCase(INDIRECT_CRL)) {
            this.isIndirectCRL = false;
        } else if (name.equalsIgnoreCase(REASONS)) {
            this.revocationReasons = null;
        } else if (name.equalsIgnoreCase(ONLY_USER_CERTS)) {
            this.hasOnlyUserCerts = false;
        } else if (name.equalsIgnoreCase(ONLY_CA_CERTS)) {
            this.hasOnlyCACerts = false;
        } else if (name.equalsIgnoreCase(ONLY_ATTRIBUTE_CERTS)) {
            this.hasOnlyAttributeCerts = false;
        } else {
            throw new IOException("Attribute name [" + name + "] not recognized by CertAttrSet:IssuingDistributionPointExtension.");
        }
        encodeThis();
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(POINT);
        elements.addElement(REASONS);
        elements.addElement(ONLY_USER_CERTS);
        elements.addElement(ONLY_CA_CERTS);
        elements.addElement(ONLY_ATTRIBUTE_CERTS);
        elements.addElement(INDIRECT_CRL);
        return elements.elements();
    }

    private void encodeThis() throws IOException {
        if (this.distributionPoint != null || this.revocationReasons != null || this.hasOnlyUserCerts || this.hasOnlyCACerts || this.hasOnlyAttributeCerts || this.isIndirectCRL) {
            DerOutputStream tagged = new DerOutputStream();
            if (this.distributionPoint != null) {
                DerOutputStream tmp = new DerOutputStream();
                this.distributionPoint.encode(tmp);
                tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 0), tmp);
            }
            if (this.hasOnlyUserCerts) {
                DerOutputStream tmp2 = new DerOutputStream();
                tmp2.putBoolean(this.hasOnlyUserCerts);
                tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, false, (byte) 1), tmp2);
            }
            if (this.hasOnlyCACerts) {
                DerOutputStream tmp3 = new DerOutputStream();
                tmp3.putBoolean(this.hasOnlyCACerts);
                tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, false, (byte) 2), tmp3);
            }
            if (this.revocationReasons != null) {
                DerOutputStream tmp4 = new DerOutputStream();
                this.revocationReasons.encode(tmp4);
                tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, false, (byte) 3), tmp4);
            }
            if (this.isIndirectCRL) {
                DerOutputStream tmp5 = new DerOutputStream();
                tmp5.putBoolean(this.isIndirectCRL);
                tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, false, (byte) 4), tmp5);
            }
            if (this.hasOnlyAttributeCerts) {
                DerOutputStream tmp6 = new DerOutputStream();
                tmp6.putBoolean(this.hasOnlyAttributeCerts);
                tagged.writeImplicit(DerValue.createTag(Byte.MIN_VALUE, false, (byte) 5), tmp6);
            }
            DerOutputStream seq = new DerOutputStream();
            seq.write((byte) 48, tagged);
            this.extensionValue = seq.toByteArray();
            return;
        }
        this.extensionValue = null;
    }

    public String toString() {
        String str;
        String str2;
        String str3;
        String str4;
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("IssuingDistributionPoint [\n  ");
        if (this.distributionPoint != null) {
            sb.append((Object) this.distributionPoint);
        }
        if (this.revocationReasons != null) {
            sb.append((Object) this.revocationReasons);
        }
        if (this.hasOnlyUserCerts) {
            str = "  Only contains user certs: true";
        } else {
            str = "  Only contains user certs: false";
        }
        sb.append(str);
        sb.append("\n");
        if (this.hasOnlyCACerts) {
            str2 = "  Only contains CA certs: true";
        } else {
            str2 = "  Only contains CA certs: false";
        }
        sb.append(str2);
        sb.append("\n");
        if (this.hasOnlyAttributeCerts) {
            str3 = "  Only contains attribute certs: true";
        } else {
            str3 = "  Only contains attribute certs: false";
        }
        sb.append(str3);
        sb.append("\n");
        if (this.isIndirectCRL) {
            str4 = "  Indirect CRL: true";
        } else {
            str4 = "  Indirect CRL: false";
        }
        sb.append(str4);
        sb.append("\n");
        sb.append("]\n");
        return sb.toString();
    }
}
