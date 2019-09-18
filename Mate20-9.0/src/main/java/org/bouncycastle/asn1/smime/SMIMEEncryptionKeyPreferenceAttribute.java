package org.bouncycastle.asn1.smime;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.cms.RecipientKeyIdentifier;

public class SMIMEEncryptionKeyPreferenceAttribute extends Attribute {
    public SMIMEEncryptionKeyPreferenceAttribute(ASN1OctetString aSN1OctetString) {
        super(SMIMEAttributes.encrypKeyPref, new DERSet((ASN1Encodable) new DERTaggedObject(false, 2, aSN1OctetString)));
    }

    public SMIMEEncryptionKeyPreferenceAttribute(IssuerAndSerialNumber issuerAndSerialNumber) {
        super(SMIMEAttributes.encrypKeyPref, new DERSet((ASN1Encodable) new DERTaggedObject(false, 0, issuerAndSerialNumber)));
    }

    public SMIMEEncryptionKeyPreferenceAttribute(RecipientKeyIdentifier recipientKeyIdentifier) {
        super(SMIMEAttributes.encrypKeyPref, new DERSet((ASN1Encodable) new DERTaggedObject(false, 1, recipientKeyIdentifier)));
    }
}
