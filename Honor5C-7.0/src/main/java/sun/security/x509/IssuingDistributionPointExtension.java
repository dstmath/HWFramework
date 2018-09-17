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
    private static final byte TAG_DISTRIBUTION_POINT = (byte) 0;
    private static final byte TAG_INDIRECT_CRL = (byte) 4;
    private static final byte TAG_ONLY_ATTRIBUTE_CERTS = (byte) 5;
    private static final byte TAG_ONLY_CA_CERTS = (byte) 2;
    private static final byte TAG_ONLY_SOME_REASONS = (byte) 3;
    private static final byte TAG_ONLY_USER_CERTS = (byte) 1;
    private DistributionPointName distributionPoint;
    private boolean hasOnlyAttributeCerts;
    private boolean hasOnlyCACerts;
    private boolean hasOnlyUserCerts;
    private boolean isIndirectCRL;
    private ReasonFlags revocationReasons;

    public IssuingDistributionPointExtension(DistributionPointName distributionPoint, ReasonFlags revocationReasons, boolean hasOnlyUserCerts, boolean hasOnlyCACerts, boolean hasOnlyAttributeCerts, boolean isIndirectCRL) throws IOException {
        this.distributionPoint = null;
        this.revocationReasons = null;
        this.hasOnlyUserCerts = false;
        this.hasOnlyCACerts = false;
        this.hasOnlyAttributeCerts = false;
        this.isIndirectCRL = false;
        if ((hasOnlyUserCerts && (hasOnlyCACerts || hasOnlyAttributeCerts)) || ((hasOnlyCACerts && (hasOnlyUserCerts || hasOnlyAttributeCerts)) || (hasOnlyAttributeCerts && (hasOnlyUserCerts || hasOnlyCACerts)))) {
            throw new IllegalArgumentException("Only one of hasOnlyUserCerts, hasOnlyCACerts, hasOnlyAttributeCerts may be set to true");
        }
        this.extensionId = PKIXExtensions.IssuingDistributionPoint_Id;
        this.critical = true;
        this.distributionPoint = distributionPoint;
        this.revocationReasons = revocationReasons;
        this.hasOnlyUserCerts = hasOnlyUserCerts;
        this.hasOnlyCACerts = hasOnlyCACerts;
        this.hasOnlyAttributeCerts = hasOnlyAttributeCerts;
        this.isIndirectCRL = isIndirectCRL;
        encodeThis();
    }

    public IssuingDistributionPointExtension(Boolean critical, Object value) throws IOException {
        this.distributionPoint = null;
        this.revocationReasons = null;
        this.hasOnlyUserCerts = false;
        this.hasOnlyCACerts = false;
        this.hasOnlyAttributeCerts = false;
        this.isIndirectCRL = false;
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
                    if (opt.isContextSpecific(TAG_DISTRIBUTION_POINT) && opt.isConstructed()) {
                        this.distributionPoint = new DistributionPointName(opt.data.getDerValue());
                    } else if (opt.isContextSpecific(TAG_ONLY_USER_CERTS) && !opt.isConstructed()) {
                        opt.resetTag(TAG_ONLY_USER_CERTS);
                        this.hasOnlyUserCerts = opt.getBoolean();
                    } else if (opt.isContextSpecific(TAG_ONLY_CA_CERTS) && !opt.isConstructed()) {
                        opt.resetTag(TAG_ONLY_USER_CERTS);
                        this.hasOnlyCACerts = opt.getBoolean();
                    } else if (opt.isContextSpecific(TAG_ONLY_SOME_REASONS) && !opt.isConstructed()) {
                        this.revocationReasons = new ReasonFlags(opt);
                    } else if (opt.isContextSpecific(TAG_INDIRECT_CRL) && !opt.isConstructed()) {
                        opt.resetTag(TAG_ONLY_USER_CERTS);
                        this.isIndirectCRL = opt.getBoolean();
                    } else if (!opt.isContextSpecific(TAG_ONLY_ATTRIBUTE_CERTS) || opt.isConstructed()) {
                        throw new IOException("Invalid encoding of IssuingDistributionPoint");
                    } else {
                        opt.resetTag(TAG_ONLY_USER_CERTS);
                        this.hasOnlyAttributeCerts = opt.getBoolean();
                    }
                }
                return;
            } else {
                return;
            }
        }
        throw new IOException("Illegal argument type");
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
            if (!(obj instanceof ReasonFlags)) {
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
            throw new IOException("Attribute name [" + name + "] not recognized by " + "CertAttrSet:IssuingDistributionPointExtension.");
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
        throw new IOException("Attribute name [" + name + "] not recognized by " + "CertAttrSet:IssuingDistributionPointExtension.");
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
            throw new IOException("Attribute name [" + name + "] not recognized by " + "CertAttrSet:IssuingDistributionPointExtension.");
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
            DerOutputStream tmp;
            DerOutputStream tagged = new DerOutputStream();
            if (this.distributionPoint != null) {
                tmp = new DerOutputStream();
                this.distributionPoint.encode(tmp);
                tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, true, TAG_DISTRIBUTION_POINT), tmp);
            }
            if (this.hasOnlyUserCerts) {
                tmp = new DerOutputStream();
                tmp.putBoolean(this.hasOnlyUserCerts);
                tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, false, TAG_ONLY_USER_CERTS), tmp);
            }
            if (this.hasOnlyCACerts) {
                tmp = new DerOutputStream();
                tmp.putBoolean(this.hasOnlyCACerts);
                tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, false, TAG_ONLY_CA_CERTS), tmp);
            }
            if (this.revocationReasons != null) {
                tmp = new DerOutputStream();
                this.revocationReasons.encode(tmp);
                tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, false, TAG_ONLY_SOME_REASONS), tmp);
            }
            if (this.isIndirectCRL) {
                tmp = new DerOutputStream();
                tmp.putBoolean(this.isIndirectCRL);
                tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, false, TAG_INDIRECT_CRL), tmp);
            }
            if (this.hasOnlyAttributeCerts) {
                tmp = new DerOutputStream();
                tmp.putBoolean(this.hasOnlyAttributeCerts);
                tagged.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT, false, TAG_ONLY_ATTRIBUTE_CERTS), tmp);
            }
            DerOutputStream seq = new DerOutputStream();
            seq.write((byte) DerValue.tag_SequenceOf, tagged);
            this.extensionValue = seq.toByteArray();
            return;
        }
        this.extensionValue = null;
    }

    public String toString() {
        String str;
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("IssuingDistributionPoint [\n  ");
        if (this.distributionPoint != null) {
            sb.append(this.distributionPoint);
        }
        if (this.revocationReasons != null) {
            sb.append(this.revocationReasons);
        }
        if (this.hasOnlyUserCerts) {
            str = "  Only contains user certs: true";
        } else {
            str = "  Only contains user certs: false";
        }
        sb.append(str).append("\n");
        if (this.hasOnlyCACerts) {
            str = "  Only contains CA certs: true";
        } else {
            str = "  Only contains CA certs: false";
        }
        sb.append(str).append("\n");
        if (this.hasOnlyAttributeCerts) {
            str = "  Only contains attribute certs: true";
        } else {
            str = "  Only contains attribute certs: false";
        }
        sb.append(str).append("\n");
        if (this.isIndirectCRL) {
            str = "  Indirect CRL: true";
        } else {
            str = "  Indirect CRL: false";
        }
        sb.append(str).append("\n");
        sb.append("]\n");
        return sb.toString();
    }
}
