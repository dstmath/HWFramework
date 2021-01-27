package org.bouncycastle.asn1.x509;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.util.encoders.Hex;

public abstract class X509NameEntryConverter {
    /* access modifiers changed from: protected */
    public boolean canBePrintable(String str) {
        return DERPrintableString.isPrintableString(str);
    }

    /* access modifiers changed from: protected */
    public ASN1Primitive convertHexEncoded(String str, int i) throws IOException {
        return ASN1Primitive.fromByteArray(Hex.decodeStrict(str, i, str.length() - i));
    }

    public abstract ASN1Primitive getConvertedValue(ASN1ObjectIdentifier aSN1ObjectIdentifier, String str);
}
