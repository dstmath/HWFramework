package org.bouncycastle.asn1.pkcs;

import java.math.BigInteger;
import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;

public class RSAPrivateKeyStructure extends ASN1Object {
    private BigInteger coefficient;
    private BigInteger exponent1;
    private BigInteger exponent2;
    private BigInteger modulus;
    private ASN1Sequence otherPrimeInfos;
    private BigInteger prime1;
    private BigInteger prime2;
    private BigInteger privateExponent;
    private BigInteger publicExponent;
    private int version;

    public RSAPrivateKeyStructure(BigInteger bigInteger, BigInteger bigInteger2, BigInteger bigInteger3, BigInteger bigInteger4, BigInteger bigInteger5, BigInteger bigInteger6, BigInteger bigInteger7, BigInteger bigInteger8) {
        this.otherPrimeInfos = null;
        this.version = 0;
        this.modulus = bigInteger;
        this.publicExponent = bigInteger2;
        this.privateExponent = bigInteger3;
        this.prime1 = bigInteger4;
        this.prime2 = bigInteger5;
        this.exponent1 = bigInteger6;
        this.exponent2 = bigInteger7;
        this.coefficient = bigInteger8;
    }

    public RSAPrivateKeyStructure(ASN1Sequence aSN1Sequence) {
        this.otherPrimeInfos = null;
        Enumeration objects = aSN1Sequence.getObjects();
        BigInteger value = ((ASN1Integer) objects.nextElement()).getValue();
        if (value.intValue() == 0 || value.intValue() == 1) {
            this.version = value.intValue();
            this.modulus = ((ASN1Integer) objects.nextElement()).getValue();
            this.publicExponent = ((ASN1Integer) objects.nextElement()).getValue();
            this.privateExponent = ((ASN1Integer) objects.nextElement()).getValue();
            this.prime1 = ((ASN1Integer) objects.nextElement()).getValue();
            this.prime2 = ((ASN1Integer) objects.nextElement()).getValue();
            this.exponent1 = ((ASN1Integer) objects.nextElement()).getValue();
            this.exponent2 = ((ASN1Integer) objects.nextElement()).getValue();
            this.coefficient = ((ASN1Integer) objects.nextElement()).getValue();
            if (objects.hasMoreElements()) {
                this.otherPrimeInfos = (ASN1Sequence) objects.nextElement();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("wrong version for RSA private key");
    }

    public static RSAPrivateKeyStructure getInstance(Object obj) {
        if (obj instanceof RSAPrivateKeyStructure) {
            return (RSAPrivateKeyStructure) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new RSAPrivateKeyStructure((ASN1Sequence) obj);
        }
        throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
    }

    public static RSAPrivateKeyStructure getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public BigInteger getCoefficient() {
        return this.coefficient;
    }

    public BigInteger getExponent1() {
        return this.exponent1;
    }

    public BigInteger getExponent2() {
        return this.exponent2;
    }

    public BigInteger getModulus() {
        return this.modulus;
    }

    public BigInteger getPrime1() {
        return this.prime1;
    }

    public BigInteger getPrime2() {
        return this.prime2;
    }

    public BigInteger getPrivateExponent() {
        return this.privateExponent;
    }

    public BigInteger getPublicExponent() {
        return this.publicExponent;
    }

    public int getVersion() {
        return this.version;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(new ASN1Integer((long) this.version));
        aSN1EncodableVector.add(new ASN1Integer(getModulus()));
        aSN1EncodableVector.add(new ASN1Integer(getPublicExponent()));
        aSN1EncodableVector.add(new ASN1Integer(getPrivateExponent()));
        aSN1EncodableVector.add(new ASN1Integer(getPrime1()));
        aSN1EncodableVector.add(new ASN1Integer(getPrime2()));
        aSN1EncodableVector.add(new ASN1Integer(getExponent1()));
        aSN1EncodableVector.add(new ASN1Integer(getExponent2()));
        aSN1EncodableVector.add(new ASN1Integer(getCoefficient()));
        if (this.otherPrimeInfos != null) {
            aSN1EncodableVector.add(this.otherPrimeInfos);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
