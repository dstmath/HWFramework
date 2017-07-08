package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.x500.X500Name;
import java.math.BigInteger;

public class IssuerSerial extends ASN1Object {
    GeneralNames issuer;
    DERBitString issuerUID;
    ASN1Integer serial;

    public static IssuerSerial getInstance(Object obj) {
        if (obj instanceof IssuerSerial) {
            return (IssuerSerial) obj;
        }
        if (obj != null) {
            return new IssuerSerial(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static IssuerSerial getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    private IssuerSerial(ASN1Sequence seq) {
        if (seq.size() == 2 || seq.size() == 3) {
            this.issuer = GeneralNames.getInstance(seq.getObjectAt(0));
            this.serial = ASN1Integer.getInstance(seq.getObjectAt(1));
            if (seq.size() == 3) {
                this.issuerUID = DERBitString.getInstance(seq.getObjectAt(2));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Bad sequence size: " + seq.size());
    }

    public IssuerSerial(X500Name issuer, BigInteger serial) {
        this(new GeneralNames(new GeneralName(issuer)), new ASN1Integer(serial));
    }

    public IssuerSerial(GeneralNames issuer, BigInteger serial) {
        this(issuer, new ASN1Integer(serial));
    }

    public IssuerSerial(GeneralNames issuer, ASN1Integer serial) {
        this.issuer = issuer;
        this.serial = serial;
    }

    public GeneralNames getIssuer() {
        return this.issuer;
    }

    public ASN1Integer getSerial() {
        return this.serial;
    }

    public DERBitString getIssuerUID() {
        return this.issuerUID;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.issuer);
        v.add(this.serial);
        if (this.issuerUID != null) {
            v.add(this.issuerUID);
        }
        return new DERSequence(v);
    }
}
