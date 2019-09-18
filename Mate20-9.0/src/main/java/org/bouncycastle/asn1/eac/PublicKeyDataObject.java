package org.bouncycastle.asn1.eac;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;

public abstract class PublicKeyDataObject extends ASN1Object {
    public static PublicKeyDataObject getInstance(Object obj) {
        if (obj instanceof PublicKeyDataObject) {
            return (PublicKeyDataObject) obj;
        }
        if (obj == null) {
            return null;
        }
        ASN1Sequence instance = ASN1Sequence.getInstance(obj);
        return ASN1ObjectIdentifier.getInstance(instance.getObjectAt(0)).on(EACObjectIdentifiers.id_TA_ECDSA) ? new ECDSAPublicKey(instance) : new RSAPublicKey(instance);
    }

    public abstract ASN1ObjectIdentifier getUsage();
}
