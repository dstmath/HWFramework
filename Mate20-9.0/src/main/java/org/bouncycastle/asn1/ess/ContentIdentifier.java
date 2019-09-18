package org.bouncycastle.asn1.ess;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;

public class ContentIdentifier extends ASN1Object {
    ASN1OctetString value;

    private ContentIdentifier(ASN1OctetString aSN1OctetString) {
        this.value = aSN1OctetString;
    }

    public ContentIdentifier(byte[] bArr) {
        this((ASN1OctetString) new DEROctetString(bArr));
    }

    public static ContentIdentifier getInstance(Object obj) {
        if (obj instanceof ContentIdentifier) {
            return (ContentIdentifier) obj;
        }
        if (obj != null) {
            return new ContentIdentifier(ASN1OctetString.getInstance(obj));
        }
        return null;
    }

    public ASN1OctetString getValue() {
        return this.value;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.value;
    }
}
