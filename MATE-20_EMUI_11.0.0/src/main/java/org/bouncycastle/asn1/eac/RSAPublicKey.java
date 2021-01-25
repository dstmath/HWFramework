package org.bouncycastle.asn1.eac;

import java.math.BigInteger;
import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class RSAPublicKey extends PublicKeyDataObject {
    private static int exponentValid = 2;
    private static int modulusValid = 1;
    private BigInteger exponent;
    private BigInteger modulus;
    private ASN1ObjectIdentifier usage;
    private int valid = 0;

    public RSAPublicKey(ASN1ObjectIdentifier aSN1ObjectIdentifier, BigInteger bigInteger, BigInteger bigInteger2) {
        this.usage = aSN1ObjectIdentifier;
        this.modulus = bigInteger;
        this.exponent = bigInteger2;
    }

    RSAPublicKey(ASN1Sequence aSN1Sequence) {
        Enumeration objects = aSN1Sequence.getObjects();
        this.usage = ASN1ObjectIdentifier.getInstance(objects.nextElement());
        while (objects.hasMoreElements()) {
            UnsignedInteger instance = UnsignedInteger.getInstance(objects.nextElement());
            int tagNo = instance.getTagNo();
            if (tagNo == 1) {
                setModulus(instance);
            } else if (tagNo == 2) {
                setExponent(instance);
            } else {
                throw new IllegalArgumentException("Unknown DERTaggedObject :" + instance.getTagNo() + "-> not an Iso7816RSAPublicKeyStructure");
            }
        }
        if (this.valid != 3) {
            throw new IllegalArgumentException("missing argument -> not an Iso7816RSAPublicKeyStructure");
        }
    }

    private void setExponent(UnsignedInteger unsignedInteger) {
        int i = this.valid;
        int i2 = exponentValid;
        if ((i & i2) == 0) {
            this.valid = i | i2;
            this.exponent = unsignedInteger.getValue();
            return;
        }
        throw new IllegalArgumentException("Exponent already set");
    }

    private void setModulus(UnsignedInteger unsignedInteger) {
        int i = this.valid;
        int i2 = modulusValid;
        if ((i & i2) == 0) {
            this.valid = i | i2;
            this.modulus = unsignedInteger.getValue();
            return;
        }
        throw new IllegalArgumentException("Modulus already set");
    }

    public BigInteger getModulus() {
        return this.modulus;
    }

    public BigInteger getPublicExponent() {
        return this.exponent;
    }

    @Override // org.bouncycastle.asn1.eac.PublicKeyDataObject
    public ASN1ObjectIdentifier getUsage() {
        return this.usage;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(3);
        aSN1EncodableVector.add(this.usage);
        aSN1EncodableVector.add(new UnsignedInteger(1, getModulus()));
        aSN1EncodableVector.add(new UnsignedInteger(2, getPublicExponent()));
        return new DERSequence(aSN1EncodableVector);
    }
}
