package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.x500.X500Name;
import com.android.org.bouncycastle.asn1.x509.X509Name;
import java.math.BigInteger;

public class IssuerAndSerialNumber extends ASN1Object {
    ASN1Integer certSerialNumber;
    X500Name name;

    public static IssuerAndSerialNumber getInstance(Object obj) {
        if (obj instanceof IssuerAndSerialNumber) {
            return (IssuerAndSerialNumber) obj;
        }
        if (obj != null) {
            return new IssuerAndSerialNumber(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    private IssuerAndSerialNumber(ASN1Sequence seq) {
        this.name = X500Name.getInstance(seq.getObjectAt(0));
        this.certSerialNumber = (ASN1Integer) seq.getObjectAt(1);
    }

    public IssuerAndSerialNumber(X509Name name2, BigInteger certSerialNumber2) {
        this.name = X500Name.getInstance(name2.toASN1Primitive());
        this.certSerialNumber = new ASN1Integer(certSerialNumber2);
    }

    public IssuerAndSerialNumber(X509Name name2, ASN1Integer certSerialNumber2) {
        this.name = X500Name.getInstance(name2.toASN1Primitive());
        this.certSerialNumber = certSerialNumber2;
    }

    public IssuerAndSerialNumber(X500Name name2, BigInteger certSerialNumber2) {
        this.name = name2;
        this.certSerialNumber = new ASN1Integer(certSerialNumber2);
    }

    public X500Name getName() {
        return this.name;
    }

    public ASN1Integer getCertificateSerialNumber() {
        return this.certSerialNumber;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.name);
        v.add(this.certSerialNumber);
        return new DERSequence(v);
    }
}
