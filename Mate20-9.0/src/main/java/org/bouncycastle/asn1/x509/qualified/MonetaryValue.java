package org.bouncycastle.asn1.x509.qualified;

import java.math.BigInteger;
import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class MonetaryValue extends ASN1Object {
    private ASN1Integer amount;
    private Iso4217CurrencyCode currency;
    private ASN1Integer exponent;

    private MonetaryValue(ASN1Sequence aSN1Sequence) {
        Enumeration objects = aSN1Sequence.getObjects();
        this.currency = Iso4217CurrencyCode.getInstance(objects.nextElement());
        this.amount = ASN1Integer.getInstance(objects.nextElement());
        this.exponent = ASN1Integer.getInstance(objects.nextElement());
    }

    public MonetaryValue(Iso4217CurrencyCode iso4217CurrencyCode, int i, int i2) {
        this.currency = iso4217CurrencyCode;
        this.amount = new ASN1Integer((long) i);
        this.exponent = new ASN1Integer((long) i2);
    }

    public static MonetaryValue getInstance(Object obj) {
        if (obj instanceof MonetaryValue) {
            return (MonetaryValue) obj;
        }
        if (obj != null) {
            return new MonetaryValue(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public BigInteger getAmount() {
        return this.amount.getValue();
    }

    public Iso4217CurrencyCode getCurrency() {
        return this.currency;
    }

    public BigInteger getExponent() {
        return this.exponent.getValue();
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.currency);
        aSN1EncodableVector.add(this.amount);
        aSN1EncodableVector.add(this.exponent);
        return new DERSequence(aSN1EncodableVector);
    }
}
