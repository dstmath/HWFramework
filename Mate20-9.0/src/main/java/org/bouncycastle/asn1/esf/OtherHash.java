package org.bouncycastle.asn1.esf;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class OtherHash extends ASN1Object implements ASN1Choice {
    private OtherHashAlgAndValue otherHash;
    private ASN1OctetString sha1Hash;

    private OtherHash(ASN1OctetString aSN1OctetString) {
        this.sha1Hash = aSN1OctetString;
    }

    public OtherHash(OtherHashAlgAndValue otherHashAlgAndValue) {
        this.otherHash = otherHashAlgAndValue;
    }

    public OtherHash(byte[] bArr) {
        this.sha1Hash = new DEROctetString(bArr);
    }

    public static OtherHash getInstance(Object obj) {
        return obj instanceof OtherHash ? (OtherHash) obj : obj instanceof ASN1OctetString ? new OtherHash((ASN1OctetString) obj) : new OtherHash(OtherHashAlgAndValue.getInstance(obj));
    }

    public AlgorithmIdentifier getHashAlgorithm() {
        return this.otherHash == null ? new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1) : this.otherHash.getHashAlgorithm();
    }

    public byte[] getHashValue() {
        return (this.otherHash == null ? this.sha1Hash : this.otherHash.getHashValue()).getOctets();
    }

    public ASN1Primitive toASN1Primitive() {
        return this.otherHash == null ? this.sha1Hash : this.otherHash.toASN1Primitive();
    }
}
