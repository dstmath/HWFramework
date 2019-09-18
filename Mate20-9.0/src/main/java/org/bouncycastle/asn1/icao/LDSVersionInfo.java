package org.bouncycastle.asn1.icao;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;

public class LDSVersionInfo extends ASN1Object {
    private DERPrintableString ldsVersion;
    private DERPrintableString unicodeVersion;

    public LDSVersionInfo(String str, String str2) {
        this.ldsVersion = new DERPrintableString(str);
        this.unicodeVersion = new DERPrintableString(str2);
    }

    private LDSVersionInfo(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 2) {
            this.ldsVersion = DERPrintableString.getInstance(aSN1Sequence.getObjectAt(0));
            this.unicodeVersion = DERPrintableString.getInstance(aSN1Sequence.getObjectAt(1));
            return;
        }
        throw new IllegalArgumentException("sequence wrong size for LDSVersionInfo");
    }

    public static LDSVersionInfo getInstance(Object obj) {
        if (obj instanceof LDSVersionInfo) {
            return (LDSVersionInfo) obj;
        }
        if (obj != null) {
            return new LDSVersionInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public String getLdsVersion() {
        return this.ldsVersion.getString();
    }

    public String getUnicodeVersion() {
        return this.unicodeVersion.getString();
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.ldsVersion);
        aSN1EncodableVector.add(this.unicodeVersion);
        return new DERSequence(aSN1EncodableVector);
    }
}
