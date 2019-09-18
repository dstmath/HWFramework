package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.util.Arrays;

public class SubjectKeyIdentifier extends ASN1Object {
    private byte[] keyidentifier;

    protected SubjectKeyIdentifier(ASN1OctetString aSN1OctetString) {
        this(aSN1OctetString.getOctets());
    }

    public SubjectKeyIdentifier(byte[] bArr) {
        this.keyidentifier = Arrays.clone(bArr);
    }

    public static SubjectKeyIdentifier fromExtensions(Extensions extensions) {
        return getInstance(extensions.getExtensionParsedValue(Extension.subjectKeyIdentifier));
    }

    public static SubjectKeyIdentifier getInstance(Object obj) {
        if (obj instanceof SubjectKeyIdentifier) {
            return (SubjectKeyIdentifier) obj;
        }
        if (obj != null) {
            return new SubjectKeyIdentifier(ASN1OctetString.getInstance(obj));
        }
        return null;
    }

    public static SubjectKeyIdentifier getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1OctetString.getInstance(aSN1TaggedObject, z));
    }

    public byte[] getKeyIdentifier() {
        return Arrays.clone(this.keyidentifier);
    }

    public ASN1Primitive toASN1Primitive() {
        return new DEROctetString(getKeyIdentifier());
    }
}
