package org.bouncycastle.asn1.smime;

import java.util.Enumeration;
import java.util.Vector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cms.CMSEnvelopedGenerator;

public class SMIMECapabilities extends ASN1Object {
    public static final ASN1ObjectIdentifier aes128_CBC = NISTObjectIdentifiers.id_aes128_CBC;
    public static final ASN1ObjectIdentifier aes192_CBC = NISTObjectIdentifiers.id_aes192_CBC;
    public static final ASN1ObjectIdentifier aes256_CBC = NISTObjectIdentifiers.id_aes256_CBC;
    public static final ASN1ObjectIdentifier canNotDecryptAny = PKCSObjectIdentifiers.canNotDecryptAny;
    public static final ASN1ObjectIdentifier cast5_CBC = new ASN1ObjectIdentifier(CMSEnvelopedGenerator.CAST5_CBC);
    public static final ASN1ObjectIdentifier dES_CBC = new ASN1ObjectIdentifier("1.3.14.3.2.7");
    public static final ASN1ObjectIdentifier dES_EDE3_CBC = PKCSObjectIdentifiers.des_EDE3_CBC;
    public static final ASN1ObjectIdentifier idea_CBC = new ASN1ObjectIdentifier(CMSEnvelopedGenerator.IDEA_CBC);
    public static final ASN1ObjectIdentifier preferSignedData = PKCSObjectIdentifiers.preferSignedData;
    public static final ASN1ObjectIdentifier rC2_CBC = PKCSObjectIdentifiers.RC2_CBC;
    public static final ASN1ObjectIdentifier sMIMECapabilitesVersions = PKCSObjectIdentifiers.sMIMECapabilitiesVersions;
    private ASN1Sequence capabilities;

    public SMIMECapabilities(ASN1Sequence aSN1Sequence) {
        this.capabilities = aSN1Sequence;
    }

    public static SMIMECapabilities getInstance(Object obj) {
        if (obj == null || (obj instanceof SMIMECapabilities)) {
            return (SMIMECapabilities) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new SMIMECapabilities((ASN1Sequence) obj);
        }
        if (obj instanceof Attribute) {
            return new SMIMECapabilities((ASN1Sequence) ((Attribute) obj).getAttrValues().getObjectAt(0));
        }
        throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
    }

    public Vector getCapabilities(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        Enumeration objects = this.capabilities.getObjects();
        Vector vector = new Vector();
        if (aSN1ObjectIdentifier == null) {
            while (objects.hasMoreElements()) {
                vector.addElement(SMIMECapability.getInstance(objects.nextElement()));
            }
        } else {
            while (objects.hasMoreElements()) {
                SMIMECapability instance = SMIMECapability.getInstance(objects.nextElement());
                if (aSN1ObjectIdentifier.equals(instance.getCapabilityID())) {
                    vector.addElement(instance);
                }
            }
        }
        return vector;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.capabilities;
    }
}
