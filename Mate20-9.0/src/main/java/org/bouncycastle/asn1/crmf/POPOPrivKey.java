package org.bouncycastle.asn1.crmf;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.cms.EnvelopedData;

public class POPOPrivKey extends ASN1Object implements ASN1Choice {
    public static final int agreeMAC = 3;
    public static final int dhMAC = 2;
    public static final int encryptedKey = 4;
    public static final int subsequentMessage = 1;
    public static final int thisMessage = 0;
    private ASN1Encodable obj;
    private int tagNo;

    private POPOPrivKey(ASN1TaggedObject aSN1TaggedObject) {
        ASN1Encodable aSN1Encodable;
        this.tagNo = aSN1TaggedObject.getTagNo();
        switch (this.tagNo) {
            case 0:
            case 2:
                aSN1Encodable = DERBitString.getInstance(aSN1TaggedObject, false);
                break;
            case 1:
                aSN1Encodable = SubsequentMessage.valueOf(ASN1Integer.getInstance(aSN1TaggedObject, false).getValue().intValue());
                break;
            case 3:
                aSN1Encodable = PKMACValue.getInstance(aSN1TaggedObject, false);
                break;
            case 4:
                aSN1Encodable = EnvelopedData.getInstance(aSN1TaggedObject, false);
                break;
            default:
                throw new IllegalArgumentException("unknown tag in POPOPrivKey");
        }
        this.obj = aSN1Encodable;
    }

    public POPOPrivKey(PKMACValue pKMACValue) {
        this.tagNo = 3;
        this.obj = pKMACValue;
    }

    public POPOPrivKey(SubsequentMessage subsequentMessage2) {
        this.tagNo = 1;
        this.obj = subsequentMessage2;
    }

    public static POPOPrivKey getInstance(Object obj2) {
        if (obj2 instanceof POPOPrivKey) {
            return (POPOPrivKey) obj2;
        }
        if (obj2 != null) {
            return new POPOPrivKey(ASN1TaggedObject.getInstance(obj2));
        }
        return null;
    }

    public static POPOPrivKey getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1TaggedObject.getInstance(aSN1TaggedObject, z));
    }

    public int getType() {
        return this.tagNo;
    }

    public ASN1Encodable getValue() {
        return this.obj;
    }

    public ASN1Primitive toASN1Primitive() {
        return new DERTaggedObject(false, this.tagNo, this.obj);
    }
}
